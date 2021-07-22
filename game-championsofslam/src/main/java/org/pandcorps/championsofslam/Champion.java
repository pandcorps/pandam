/*
Copyright (c) 2009-2021, Andrew M. Martin
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
   disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
   disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of Pandam nor the names of its contributors may be used to endorse or promote products derived from this
   software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/
package org.pandcorps.championsofslam;

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.col.*;
import org.pandcorps.core.img.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.text.*;

public abstract class Champion extends Panctor implements StepListener, Collidable {
    protected final static int VEL = 2;
    protected final static float INC_COLOR = 0.125f;
    protected static int NUM_EYES = -1;
    protected static int NUM_HAIR = -1;
    protected final static int DEPTH_TEXT = 1900;
    private final static int INITIAL_HEALTH = 40;
    protected final static Set<Champion> champions = new IdentityHashSet<Champion>();
    private final static int iw = 32, ih = 32;
    private final static String FRAME_STILL = "still";
    private final static String FRAME_WALK1 = "walk1";
    private final static String FRAME_WALK2 = "walk2";
    private final static String FRAME_HURT = "hurt";
    private final static String FRAME_JAB = "jab";
    private final static String FRAME_UPPERCUT = "uppercut";
    private final static ChampionFrame frmStill = new ChampionFrame(0, 0, FRAME_STILL, FRAME_STILL);
    private final static ChampionFrame frmWalk1 = new ChampionFrame(0, 0, FRAME_STILL, FRAME_WALK1);
    private final static ChampionFrame frmWalk2 = new ChampionFrame(0, 0, FRAME_STILL, FRAME_WALK2);
    private final static ChampionFrame frmHurt = new ChampionFrame(0, 0, FRAME_HURT, FRAME_STILL);
    private final static ChampionAttack atkJab = new ChampionAttack(new ChampionFrame(-2, 0, FRAME_JAB, FRAME_WALK1), 4, 1, 4);
    protected final static ChampionAttack atkUppercut = new ChampionAttack(new ChampionFrame(-2, 0, FRAME_UPPERCUT, FRAME_WALK1), 8, 2, 1);
    private final static ChampionAttack[] atkCombo = { atkJab, atkJab, atkUppercut };
    protected final static float minX = 40, minY = 36;
    protected final static float maxX = 344, maxY = 186;
    private static Pantext victoryText = null;
    
    protected final ChampionDefinition def;
    protected final Set<Champion> team;
    private ChampionFrame frm = frmStill;
    protected int hv = 0, v = 0;
    private int walkTimer = -1;
    protected ChampionAttack atk = null;
    protected int atkTimer = 0;
    private long atkLast = -1000;
    private int atkIndex = 0;
    private int hurtTimer = 0;
    private int health = INITIAL_HEALTH;
    private int defeatX = 0;
    private float defeatZ = 0;
    
    public Champion(final ChampionDefinition def, final Set<Champion> team) {
        this.def = def;
        this.team = team;
        setView(ChampionsOfSlamGame.boundingBox);
        champions.add(this);
    }
    
    @Override
    protected void onDestroy() {
        champions.remove(this);
        for (final Champion champion : champions) {
            if (champion.getClass() != Player.class) {
                return;
            }
        }
        victoryText = new Pantext(Pantil.vmid(), ChampionsOfSlamGame.font, "YOU WIN!");
        victoryText.getPosition().set(ChampionsOfSlamGame.GAME_W / 2, ChampionsOfSlamGame.GAME_H - 9, DEPTH_TEXT);
        victoryText.centerX();
        ChampionsOfSlamGame.room.addActor(victoryText);
        Pangine.getEngine().addTimer(ChampionsOfSlamGame.arena, 90, new TimerListener() {
            @Override public final void onTimer(final TimerEvent event) {
                Panctor.destroy(victoryText);
                victoryText = null;
                for (final Player player : Player.players) {
                    if (!player.isPaused()) {
                        player.onPause();
                    }
                }
                ChampionsOfSlamGame.initOpponents();
            }});
        for (final Player player : Player.players) {
            player.onVictory();
        }
    }
    
    protected final void onAttack() {
        if (atkTimer > 0) {
            return;
        }
        final long clock = Pangine.getEngine().getClock();
        if (atkIndex >= (atkCombo.length - 1)) {
            atkIndex = 0;
        } else {
            atkIndex = ((clock - atkLast) < 18) ? (atkIndex + 1) : 0;
        }
        atk = atkCombo[atkIndex];
        atkTimer = atk.duration;
        ChampionsOfSlamGame.soundJab.startSound();
        new HitBox(this, atk);
    }
    
    private final void onSuccessfulAttack() {
        atkLast = Pangine.getEngine().getClock();
    }
    
    @Override
    public final void onStep(final StepEvent event) {
        if (isDefeated()) {
            onStepDefeated();
            return;
        }
        onStepStart();
        if (isHurt()) {
            onStepHurt();
        } else if (atkTimer > 0) {
            onStepAttack();
        } else {
            onStepMotion();
        }
    }
    
    protected void onStepStart() {
    }
    
    private final void onStepDefeated() {
        clearWalk();
        frm = frmHurt;
        final Panple pos = getPosition();
        pos.add(defeatX, 0, Math.round(defeatZ));
        if (!isInView() || (pos.getZ() > ChampionsOfSlamGame.GAME_H)) {
            destroy();
        } else if (defeatZ > 1.5f) {
            defeatZ -= 0.5f;
        }
    }
    
    private final void onStepHurt() {
        clearWalk();
        atkTimer = 0;
        frm = frmHurt;
        hurtTimer--;
    }
    
    private final void onStepAttack() {
        clearWalk();
        frm = (atkTimer > 1) ? atk.frm : frmStill;
        atkTimer--;
    }
    
    protected final void clearWalk() {
        hv = v = 0;
        walkTimer = -1;
    }
    
    private final void onStepMotion() {
        if ((hv == 0) && (v == 0)) {
            walkTimer = -1;
            frm = frmStill;
        } else {
            walkTimer++;
            if (walkTimer > 15) {
                walkTimer = 0;
            }
            if (walkTimer < 4) {
                frm = frmWalk1;
            } else if (walkTimer < 8) {
                frm = frmStill;
            } else if (walkTimer < 12) {
                frm = frmWalk2;
            } else {
                frm = frmStill;
            }
        }
        if (hv > 0) {
            setMirror(false);
        } else if (hv < 0) {
            setMirror(true);
        }
        final Panple pos = getPosition();
        if (hv != 0) {
            float x = pos.getX() + hv;
            if (x >= maxX) {
                x = maxX;
                onMaxX();
            } else if (x <= minX) {
                x = minX;
                onMinX();
            }
            pos.setX(x);
            hv = 0;
        }
        if (v != 0) {
            float y = pos.getY() + v;
            if (y >= maxY) {
                y = maxY;
                onMaxY();
            } else if (y <= minY) {
                y = minY;
                onMinY();
            }
            pos.setY(y);
            v = 0;
        }
    }
    
    protected void onMinX() {
    }
    
    protected void onMaxX() {
    }
    
    protected void onMinY() {
    }
    
    protected void onMaxY() {
    }
    
    protected boolean isPaused() {
        return false;
    }
    
    protected boolean isInvincible() {
        return false;
    }
    
    protected final boolean isDefeated() {
        return defeatZ > 0;
    }
    
    private final boolean isHurt() {
        return hurtTimer > 0;
    }
    
    private final void onAttacked(final HitBox hitBox) {
        if (isHurt() || isPaused()) {
            return;
        }
        final ChampionAttack attack = hitBox.attack;
        hurtTimer = attack.duration - 1;
        if (isInvincible()) {
            return;
        }
        health -= attack.damage;
        final boolean uppercut = attack == atkUppercut;
        if (uppercut) {
            ChampionsOfSlamGame.soundUppercut.startSound();
        }
        if (uppercut && (health <= 0)) {
            onDefeated(hitBox);
        } else {
            onHurt(hitBox);
        }
    }
    
    protected void onHurt(final HitBox hitBox) {
    }
    
    private final void onDefeated(final HitBox hitBox) {
        if (isInvincible()) {
            return;
        }
        defeatX = (hitBox.src.getPosition().getX() < getPosition().getX()) ? 12 : -12;
        defeatZ = 12;
    }
    
    protected Panple getChampionPosition() {
        return getPosition();
    }
    
    private final float getX(final int _offset) {
        final int offset = _offset + frm.headX;
        return getPosition().getX() - (isMirror() ? (32 - offset) : offset);
    }
    
    @Override
    protected final void renderView(final Panderer renderer) {
        final Panple pos = getChampionPosition();
        // Don't need to consider whole range from 0 to GAME_H if parts of screen are inaccessible
        // If characters always move up/down with a minimum velocity v > 1, then can divide y by v
        final float _y = pos.getY(), z = (ChampionsOfSlamGame.GAME_H - _y) * 8, y = _y + pos.getZ();
        final float hx = getX(Images.headX), hy = y - frm.headY;
        final FloatColor hairColor = def.hairColor;
        final int mouthIndex = nvl(frm.mouthIndex, def.mouthIndex), eyesIndex = nvl(frm.eyesIndex, def.eyesIndex), hairIndex = def.hairIndex;
        final Clothing shirt = def.shirt;
        renderBodyComponent(renderer, "fg." + frm.bodyFrameName, y, z, shirt);
        if (hairIndex >= 0) {
            renderFrameComponent(renderer, Images.hairComponents.get(hairIndex), hx, hy, z, hairColor);
        }
        renderFaceComponent(renderer, Images.eyesComponents, eyesIndex, hx, hy, z);
        renderFaceComponent(renderer, Images.mouthComponents, mouthIndex, hx, hy, z);
        renderBodyComponent(renderer, "head", y, z);
        renderBodyComponent(renderer, "legs." + frm.legsFrameName, y, z, def.boots, def.pants);
        renderBodyComponent(renderer, "bg." + frm.bodyFrameName, y, z, shirt);
    }
    
    private final static int nvl(final int n1, final int n2) {
        return (n1 < 0) ? n2 : n1;
    }
    
    private final void renderBodyComponent(final Panderer renderer, final String frameName, final float y, final float z, final Clothing... clothings) {
        renderFrameComponent(renderer, Images.bodyComponents.get(frameName), frameName, y, z, clothings);
    }
    
    private final void renderFaceComponent(final Panderer renderer, final List<ChampionFrameComponent> cmps, final int i, final float x, final float y, final float z) {
        renderFrameComponent(renderer, cmps.get(i), x, y, z, COLOR_WHITE);
    }
    
    private final void renderFrameComponent(final Panderer renderer, final ChampionFrameComponent cmp, final String frameName,
            final float y, final float z, final Clothing... clothings) {
        if (cmp == null) {
            return;
        }
        final float x = getX(cmp.x);
        for (final Clothing clothing : clothings) {
            final ChampionFrameComponent cmpClothing = clothing.style.frames.get(frameName);
            if (cmpClothing != null) {
                renderFrameComponent(renderer, cmpClothing, x, y, z, clothing.color);
            }
        }
        renderFrameComponent(renderer, cmp, x, y, z, def.bodyColor);
    }
    
    private final void renderFrameComponent(final Panderer renderer, final ChampionFrameComponent cmp, final float x, final float y, final float z, final FloatColor color) {
        renderer.render(getLayer(), ChampionsOfSlamGame.imgChampion, x, y, z, cmp.ix, cmp.iy, iw, ih, 0, isMirror(), false, color.getR(), color.getG(), color.getB());
    }
    
    private final static float randomColorComponent() {
        return INC_COLOR * Mathtil.randi(0, 8);
    }
    
    protected final static int getColorInt(final float color) {
        return Math.round(color / INC_COLOR);
    }
    
    public final static ChampionDefinition randomChampionDefinition() {
        final ChampionDefinition def = new ChampionDefinition();
        randomize(def);
        return def;
    }
    
    public final static void randomize(final ChampionDefinition def) {
        final Clothing shirt = def.shirt, pants = def.pants, boots = def.boots;
        final FloatColor bodyColor = def.bodyColor, hairColor = def.hairColor, shirtColor = shirt.color, pantsColor = pants.color, bootsColor = boots.color;
        final int bodyColorR = Mathtil.randi(4, 8), bodyColorG = Mathtil.randi(bodyColorR / 2, bodyColorR - 1);
        bodyColor.setR(INC_COLOR * bodyColorR);
        bodyColor.setG(INC_COLOR * bodyColorG);
        bodyColor.setB(INC_COLOR * Mathtil.randi(bodyColorG / 2, bodyColorG - 1));
        final int hairColorR = Mathtil.randi(1, 8), hairColorG = Mathtil.randi(hairColorR / 4, hairColorR);
        hairColor.setR(INC_COLOR * hairColorR);
        hairColor.setG(INC_COLOR * hairColorG);
        hairColor.setB(INC_COLOR * Mathtil.randi(hairColorG / 4, hairColorG));
        float shirtR = randomColorComponent(), shirtG = randomColorComponent(), shirtB = randomColorComponent();
        if ((shirtR < 0.05f) && (shirtR < 0.05f) && (shirtR < 0.05f)) {
            shirtR = shirtG = shirtB = INC_COLOR;
        }
        shirtColor.setR(shirtR);
        shirtColor.setG(shirtG);
        shirtColor.setB(shirtB);
        final FloatColor greyColor, copyColor;
        if (Mathtil.rand()) {
            greyColor = bootsColor;
            copyColor = pantsColor;
        } else {
            greyColor = pantsColor;
            copyColor = bootsColor;
        }
        copyColor.set(shirtColor);
        final float grey = INC_COLOR * Mathtil.randi(1, 7);
        greyColor.setGrey(grey);
        def.eyesIndex = Mathtil.randi(0, NUM_EYES - 1);
        def.hairIndex = Mathtil.randi(-1, NUM_HAIR - 1);
        //shirt.style = Mathtil.rand(ChampionsOfSlamGame.shirtStyles);
        //pants.style = Mathtil.rand(ChampionsOfSlamGame.pantsStyles);
    }
    
    public final static class ChampionDefinition {
        protected final FloatColor bodyColor = new FloatColor();
        protected int mouthIndex = 0;
        protected int eyesIndex = 0;
        protected int hairIndex = -1;
        protected final FloatColor hairColor = new FloatColor();
        protected final Clothing shirt = new Clothing(Images.shirtStyles.get("gloves"));
        protected final Clothing pants = new Clothing(Images.pantsStyles.get("shorts"));
        protected final Clothing boots = new Clothing(Images.bootsStyles.get("boots"));
        
        public final void load(final String s) {
            final Segment seg = Segment.parse(s);
            Champion.load(bodyColor, seg.getField(0));
            eyesIndex = seg.intValue(1);
            hairIndex = seg.intValue(2);
            Champion.load(hairColor, seg.getField(3));
            shirt.load(seg, Images.shirtStyles, 4);
            pants.load(seg, Images.pantsStyles, 6);
            boots.load(seg, Images.bootsStyles, 8);
            mouthIndex = seg.intValue(10);
        }
        
        @Override
        public final String toString() {
            final StringBuilder b = new StringBuilder();
            b.append("CHM").append('|');
            append(b, bodyColor).append('|');
            b.append(eyesIndex).append('|');
            b.append(hairIndex).append('|');
            append(b, hairColor).append('|');
            shirt.append(b);
            pants.append(b);
            boots.append(b);
            b.append(mouthIndex);
            return b.toString();
        }
    }
    
    public static class ClothingStyle {
        protected final int styleIndex;
        protected final String styleName;
        protected final Map<String, ChampionFrameComponent> frames = new HashMap<String, ChampionFrameComponent>();
        
        public ClothingStyle(final int styleIndex, final String styleName) {
            this.styleIndex = styleIndex;
            this.styleName = styleName;
        }
        
        @Override
        public final String toString() {
            return styleIndex + " - " + styleName;
        }
    }
    
    public final static class Clothing {
        protected ClothingStyle style;
        protected final FloatColor color = new FloatColor();
        
        public Clothing(final ClothingStyle style) {
            this.style = style;
        }
        
        private final void load(final Segment seg, final Map<Object, ClothingStyle> styles, final int fieldIndex) {
            style = styles.get(seg.getValue(fieldIndex));
            Champion.load(color, seg.getField(fieldIndex + 1));
        }
        
        private final void append(final StringBuilder b) {
            b.append(style.styleName).append('|');
            Champion.append(b, color).append('|');
        }
    }
    
    public final static void load(final FloatColor color, final Piped rec) {
        color.setR(INC_COLOR * rec.intValue(0));
        color.setG(INC_COLOR * rec.intValue(1));
        color.setB(INC_COLOR * rec.intValue(2));
    }
    
    public final static StringBuilder append(final StringBuilder sb, final FloatColor color) {
        return sb.append(getColorInt(color.getR())).append('^').append(getColorInt(color.getG())).append('^').append(getColorInt(color.getB()));
    }
    
    public final static FloatColor COLOR_WHITE = new FloatColor();
    
    public final static class ChampionFrame {
        private final int headX; //TODO Currently using for all components; rename? Add a separate baseX?
        private final int headY;
        private final int eyesIndex;
        private final int mouthIndex;
        private final String bodyFrameName;
        private final String legsFrameName;
        
        public ChampionFrame(final int headX, final int headY, final String bodyFrameName, final String legsFrameName) {
            this(headX, headY, -1, -1, bodyFrameName, legsFrameName);
        }
        
        public ChampionFrame(final int headX, final int headY, final int eyesIndex, final int mouthIndex,
                final String bodyFrameName, final String legsFrameName) {
            this.headX = headX;
            this.headY = headY;
            this.eyesIndex = eyesIndex;
            this.mouthIndex = mouthIndex;
            this.bodyFrameName = bodyFrameName;
            this.legsFrameName = legsFrameName;
        }
    }
    
    public final static class ChampionFrameComponent {
        private final int ix;
        private final int iy;
        protected final int x;
        
        public ChampionFrameComponent(final int ix, final int iy, final int x) {
            this.ix = ix;
            this.iy = iy;
            this.x = x;
        }
    }
    
    public final static class ChampionAttack {
        private final ChampionFrame frm;
        private final int duration;
        private final int damage;
        private final Panmage boundingBox;
        
        public ChampionAttack(final ChampionFrame frm, final int duration, final int damage, final int reach) {
            this.frm = frm;
            this.duration = duration;
            this.damage = damage;
            boundingBox = Pangine.getEngine().createEmptyImage(Pantil.vmid(), null, null, new FinPanple2(reach, 3));
        }
    }
    
    public final static class HitBox extends Panctor implements StepListener, CollisionListener {
        protected final Champion src;
        private final ChampionAttack attack;
        private boolean active = true;
        
        public HitBox(final Champion src, final ChampionAttack attack) {
            this.src = src;
            this.attack = attack;
            final Panple srcPos = src.getPosition();
            getPosition().set(srcPos.getX() + (12 * src.getMirrorMultiplier()), srcPos.getY());
            setMirror(src.isMirror());
            src.getLayer().addActor(this);
            setView(attack.boundingBox);
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            if (active) {
                active = false;
            } else {
                destroy();
            }
        }
        
        @Override
        public final void onCollision(final CollisionEvent event) {
            final Collidable collider = event.getCollider();
            if (collider == src) {
                return;
            }
            final Champion target;
            if (collider instanceof Champion) {
                target = (Champion) collider;
            } else if (collider.getClass() == HitBox.class) {
                target = ((HitBox) collider).src;
            } else {
                return;
            }
            if (Coltil.contains(src.team, target)) {
                return;
            }
            src.onSuccessfulAttack();
            target.onAttacked(this);
        }
    }
}
