/*
Copyright (c) 2009-2018, Andrew M. Martin
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
import org.pandcorps.core.seg.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.text.*;

public abstract class Champion extends Panctor implements StepListener, Collidable {
    protected final static int VEL = 2;
    protected final static float INC_COLOR = 0.125f;
    protected final static int NUM_EYES = 19;
    protected final static int NUM_HAIR = 9;
    protected final static int DEPTH_TEXT = 1900;
    private final static int INITIAL_HEALTH = 40;
    protected final static Set<Champion> champions = new IdentityHashSet<Champion>();
    private static int numShirts = 0;
    private static int numPants = 0;
    private final static int iw = 32, ih = 32;
    private final static ChampionFrame frmStill = new ChampionFrame(0, 11);
    private final static ChampionFrame frmWalk1 = new ChampionFrame(32, 11);
    private final static ChampionFrame frmWalk2 = new ChampionFrame(64, 11);
    private final static ChampionFrame frmHurt = new ChampionFrame(96, 13);
    private final static ChampionAttack atkJab = new ChampionAttack(new ChampionFrame(160, 10), 4, 1, 4);
    protected final static ChampionAttack atkUppercut = new ChampionAttack(new ChampionFrame(128, 10), 8, 2, 1);
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
    
    protected final static int getNumShirts() {
        return numShirts;
    }
    
    protected final static int getNumPants() {
        return numPants;
    }
    
    @Override
    protected final void renderView(final Panderer renderer) {
        final Panlayer layer = getLayer();
        final Panple pos = getChampionPosition();
        final boolean mirror = isMirror();
        final int ox = frm.ox;
        // Don't need to consider whole range from 0 to GAME_H if parts of screen are inaccessible
        // If characters always move up/down with a minimum velocity v > 1, then can divide y by v
        final float x = pos.getX() - (mirror ? (32 - ox) : ox), _y = pos.getY(), z = (ChampionsOfSlamGame.GAME_H - _y) * 8, y = _y + pos.getZ();
        final float ix = frm.ix;
        final FloatColor bodyColor = def.bodyColor, hairColor = def.hairColor, shirtColor = def.shirtColor, pantsColor = def.pantsColor, bootsColor = def.bootsColor;
        final int eyesIndex = def.eyesIndex, hairIndex = def.hairIndex;
        final int iyPants = def.pantsStyle.iy;
        renderer.render(layer, ChampionsOfSlamGame.imgChampion, x, y, z, ix, 96, iw, ih, 0, mirror, false, bootsColor.r, bootsColor.g, bootsColor.b);
        // Hair, z + 6 (or 8 if allow Paint or Facial Hair at 6)
        if (hairIndex >= 0) {
            renderer.render(layer, ChampionsOfSlamGame.imgChampion, x, y, z, hairIndex * 32, 960, iw, ih, 0, mirror, false, hairColor.r, hairColor.g, hairColor.b);
        }
        final ShirtStyle shirtStyle = def.shirtStyle;
        final int iyShirt = shirtStyle.iy;
        final int shirtIndex = shirtStyle.renderIndex;
        for (int i = 0; i < 2; i++) {
            if (i == shirtIndex) {
                if (iyShirt >= 0) {
                    renderer.render(layer, ChampionsOfSlamGame.imgChampion, x, y, z, ix, iyShirt, iw, ih, 0, mirror, false, shirtColor.r, shirtColor.g, shirtColor.b);
                }
            } else {
                if (iyPants >= 0) {
                    renderer.render(layer, ChampionsOfSlamGame.imgChampion, x, y, z, ix, iyPants, iw, ih, 0, mirror, false, pantsColor.r, pantsColor.g, pantsColor.b);
                }
            }
        }
        renderer.render(layer, ChampionsOfSlamGame.imgChampion, x, y, z, eyesIndex * 32, 992, iw, ih, 0, mirror, false, 1.0f, 1.0f, 1.0f);
        renderer.render(layer, ChampionsOfSlamGame.imgChampion, x, y, z, ix, 0, iw, ih, 0, mirror, false, bodyColor.r, bodyColor.g, bodyColor.b);
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
        final FloatColor bodyColor = def.bodyColor, hairColor = def.hairColor, shirtColor = def.shirtColor, pantsColor = def.pantsColor, bootsColor = def.bootsColor;
        final int bodyColorR = Mathtil.randi(4, 8), bodyColorG = Mathtil.randi(bodyColorR / 2, bodyColorR - 1);
        bodyColor.r = INC_COLOR * bodyColorR;
        bodyColor.g = INC_COLOR * bodyColorG;
        bodyColor.b = INC_COLOR * Mathtil.randi(bodyColorG / 2, bodyColorG - 1);
        final int hairColorR = Mathtil.randi(1, 8), hairColorG = Mathtil.randi(hairColorR / 4, hairColorR);
        hairColor.r = INC_COLOR * hairColorR;
        hairColor.g = INC_COLOR * hairColorG;
        hairColor.b = INC_COLOR * Mathtil.randi(hairColorG / 4, hairColorG);
        float shirtR = randomColorComponent(), shirtG = randomColorComponent(), shirtB = randomColorComponent();
        if ((shirtR < 0.05f) && (shirtR < 0.05f) && (shirtR < 0.05f)) {
            shirtR = shirtG = shirtB = INC_COLOR;
        }
        shirtColor.r = shirtR;
        shirtColor.g = shirtG;
        shirtColor.b = shirtB;
        final FloatColor greyColor, copyColor;
        if (Mathtil.rand()) {
            greyColor = bootsColor;
            copyColor = pantsColor;
        } else {
            greyColor = pantsColor;
            copyColor = bootsColor;
        }
        copyColor.r = shirtColor.r;
        copyColor.g = shirtColor.g;
        copyColor.b = shirtColor.b;
        final float grey = INC_COLOR * Mathtil.randi(1, 7);
        greyColor.r = greyColor.g = greyColor.b = grey;
        def.eyesIndex = Mathtil.randi(0, NUM_EYES - 1);
        def.hairIndex = Mathtil.randi(-1, NUM_HAIR - 1);
        def.shirtStyle = Mathtil.rand(ChampionsOfSlamGame.shirtStyles);
        def.pantsStyle = ChampionsOfSlamGame.pantsStyles[Mathtil.randi(def.shirtStyle.pantsRequired ? 1 : 0, ChampionsOfSlamGame.pantsStyles.length - 1)];
    }
    
    public final static class ChampionDefinition {
        protected final FloatColor bodyColor = new FloatColor();
        protected int eyesIndex = 0;
        protected int hairIndex = -1;
        protected final FloatColor hairColor = new FloatColor();
        protected ShirtStyle shirtStyle = ChampionsOfSlamGame.shirtStyles[1];
        protected final FloatColor shirtColor = new FloatColor();
        protected PantsStyle pantsStyle = ChampionsOfSlamGame.pantsStyles[1];
        protected final FloatColor pantsColor = new FloatColor();
        protected final FloatColor bootsColor = new FloatColor();
        
        protected final boolean isInvalid() {
            return shirtStyle.pantsRequired && (pantsStyle.iy < 0);
        }
        
        public final void load(final String s) {
            final Segment seg = Segment.parse(s);
            bodyColor.load(seg.getField(0));
            eyesIndex = seg.intValue(1);
            hairIndex = seg.intValue(2);
            hairColor.load(seg.getField(3));
            shirtStyle = ChampionsOfSlamGame.shirtStyles[seg.intValue(4)];
            shirtColor.load(seg.getField(5));
            pantsStyle = ChampionsOfSlamGame.pantsStyles[seg.intValue(6)];
            pantsColor.load(seg.getField(7));
            bootsColor.load(seg.getField(8));
        }
        
        @Override
        public final String toString() {
            final StringBuilder b = new StringBuilder();
            b.append("CHM").append('|');
            bodyColor.append(b).append('|');
            b.append(eyesIndex).append('|');
            b.append(hairIndex).append('|');
            hairColor.append(b).append('|');
            b.append(shirtStyle.shirtIndex).append('|');
            shirtColor.append(b).append('|');
            b.append(pantsStyle.pantsIndex).append('|');
            pantsColor.append(b).append('|');
            bootsColor.append(b);
            return b.toString();
        }
    }
    
    public final static class ShirtStyle {
        protected final int shirtIndex;
        private final int iy;
        private final int renderIndex;
        private final boolean pantsRequired;
        
        public ShirtStyle(final int iy, final int renderIndex, final boolean pantsRequired) {
            shirtIndex = numShirts;
            numShirts++;
            this.iy = iy;
            this.renderIndex = renderIndex;
            this.pantsRequired = pantsRequired;
        }
    }
    
    public final static class PantsStyle {
        protected final int pantsIndex;
        private final int iy;
        
        public PantsStyle(final int iy) {
            pantsIndex = numPants;
            numPants++;
            this.iy = iy;
        }
    }
    
    public final static class FloatColor {
        protected float r = 1.0f;
        protected float g = 1.0f;
        protected float b = 1.0f;
        
        public final void load(final Record rec) {
            r = INC_COLOR * rec.intValue(0);
            g = INC_COLOR * rec.intValue(1);
            b = INC_COLOR * rec.intValue(2);
        }
        
        public final StringBuilder append(final StringBuilder sb) {
            return sb.append(getColorInt(r)).append('^').append(getColorInt(g)).append('^').append(getColorInt(b));
        }
    }
    
    public final static class ChampionFrame {
        private final int ix;
        private final int ox;
        
        public ChampionFrame(final int ix, final int ox) {
            this.ix = ix;
            this.ox = ox;
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
