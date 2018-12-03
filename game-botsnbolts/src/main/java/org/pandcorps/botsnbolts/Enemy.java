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
package org.pandcorps.botsnbolts;

import java.util.*;

import org.pandcorps.botsnbolts.BlockPuzzle.*;
import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.botsnbolts.PowerUp.*;
import org.pandcorps.botsnbolts.Profile.*;
import org.pandcorps.botsnbolts.RoomLoader.*;
import org.pandcorps.core.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.boundary.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.pandax.visual.*;

public abstract class Enemy extends Chr implements CollisionListener {
    protected final static String RES_ENEMY = BotsnBoltsGame.RES + "enemy/";
    protected final static int VEL_PROJECTILE = 6;
    protected final static float VEL_PROJECTILE_45;
    
    protected int health;
    
    static {
        final Panple tmp = new ImplPanple(VEL_PROJECTILE, 0, 0);
        tmp.setMagnitudeDirection(VEL_PROJECTILE, Math.PI / 4);
        VEL_PROJECTILE_45 = tmp.getX();
    }
    
    protected Enemy(final int offX, final int h, final Segment seg, final int health) {
        this(offX, h, getX(seg), getY(seg), health);
    }
    
    protected Enemy(final int offX, final int h, final int x, final int y, final int health) {
        super(offX, h);
        this.health = health;
        final Panple pos = getPosition();
        BotsnBoltsGame.tm.savePosition(pos, x, y);
        pos.addX(getInitialOffsetX());
        pos.setZ(BotsnBoltsGame.DEPTH_ENEMY);
        initTileCoordinates(x, y);
    }
    
    protected final static int getX(final Segment seg) {
        return seg.intValue(0);
    }
    
    protected final static int getY(final Segment seg) {
        return seg.intValue(1);
    }
    
    //@OverrideMe
    protected void initTileCoordinates(final int x, final int y) {
    }
    
    protected int getInitialOffsetX() {
        return 8;
    }
    
    @Override
    protected boolean onFell() {
        destroy();
        return true;
    }
    
    @Override
    public void onCollision(final CollisionEvent event) {
        final Collidable collider = event.getCollider();
        if (collider instanceof Projectile) { // Projectile can have sub-classes like Explosion
            onShot((Projectile) collider);
        } else if (collider.getClass() == Player.class) {
            onAttack((Player) collider);
        }
    }
    
    protected void onShot(final Projectile prj) {
        if (prj.power <= 0) {
            return;
        } else if (!isVulnerableToProjectile(prj)) {
            prj.bounce();
            return;
        }
        onHurt(prj);
    }
    
    protected void onHurt(final Projectile prj) {
        final int oldHealth = health, oldPower = prj.power;
        health -= oldPower;
        if (health <= 0) {
            prj.burst(this);
            award(prj.src);
            onDefeat();
            destroy();
        }
        prj.setPower(oldPower - oldHealth);
    }
    
    //@OverrideMe
    protected void onDefeat() {
    }
    
    protected boolean isVulnerableToProjectile(final Projectile prj) {
        return true;
    }
    
    @Override
    protected final void onDestroy() {
        RoomLoader.onEnemyDefeated();
        super.onDestroy();
        onEnemyDestroy();
    }
    
    //@OverrideMe
    protected void onEnemyDestroy() {
    }
    
    protected void onAttack(final Player player) {
        player.hurt(getDamage());
    }
    
    protected int getDamage() {
        return 1;
    }
    
    protected final void award(final Player player) {
        final PowerUp powerUp = pickAward(player);
        if (powerUp != null) {
            award(powerUp);
        }
    }
    
    protected PowerUp pickAward(final Player player) {
        final int health = player.getHealth(); // 1 to MAX
        final float damage = HudMeter.MAX_VALUE - health; // 0 to (MAX - 1)
        final float damageNormalized = damage / (HudMeter.MAX_VALUE - 1); // 0 to 1
        final int rewardPercentage = Math.round(damageNormalized * 80) + 10; // 10 to 90
        if (Mathtil.rand(rewardPercentage)) { // MAX health -> 10%; 1 health -> 90%
            final int r = Mathtil.randi(0, 99);
            final int bigThreshold = Math.round(damageNormalized * 70); // 0 to 70
            final int mediumThreshold = bigThreshold + 30; // 30 to 100
            if (r < bigThreshold) { // MAX health -> 0%; 1 health -> 70%
                return new BigBattery(player);
            } else if (r < mediumThreshold) {
                return new MediumBattery(player);
            } else {
                return new SmallBattery(player);
            }
        }
        return null;
    }
    
    protected void award(final PowerUp powerUp) {
        award(powerUp, 0, 0);
    }
    
    protected final void award(final PowerUp powerUp, final int offX, final int offY) {
        final Panple pos = getPosition();
        PowerUp.addPowerUp(powerUp, pos.getX() + offX, pos.getY() + offY, 6);
    }
    
    protected final void bubbleRandom(final int offX, final int offY) {
        if (Mathtil.randi(0, 5999) < 100) {
            new Bubble(this, offX, offY);
        }
    }
    
    protected final void updateMirror() {
        if (hv < 0) {
            setMirrorEnemy(true);
        } else if (hv > 0) {
            setMirrorEnemy(false);
        }
    }
    
    protected final void turnTowardPlayer() {
        turnTowardPlayer(getNearestPlayer());
    }
    
    protected final void setMirrorEnemy(final boolean mirror) {
        setMirror(mirror);
        fixX();
    }
    
    protected final void turnTowardPlayer(final Player player) {
        if (player == null) {
            return;
        }
        setMirrorEnemy(getPosition().getX() > player.getPosition().getX());
    }
    
    protected final Player getNearestPlayer() {
        return PlayerContext.getPlayer(BotsnBoltsGame.pc);
    }
    
    protected final float getDistanceX(final Panctor other) {
        return Math.abs(getPosition().getX() - other.getPosition().getX());
    }
    
    protected final static PlayerContext getPlayerContext() {
        return BotsnBoltsGame.pc;
    }
    
    protected final int getDirection(final float p, final float op, final int negThresh, final int posThresh) {
        final float diff = op - p;
        if (diff >= posThresh) {
            return 1;
        } else if (diff <= negThresh) {
            return -1;
        }
        return 0;
    }
    
    protected static Panmage getImage(final Panmage img, final String name, final Panmage ref) {
        if (img != null) {
            return img;
        }
        return getImage(img, name, ref.getOrigin(), ref.getBoundingMinimum(), ref.getBoundingMaximum());
    }
    
    protected static Panmage getImage(final Panmage img, final String name, final Panple o, final Panple min, final Panple max) {
        return getImage(img, "enemy.", RES_ENEMY, name, o, min, max);
    }
    
    protected static Panmage getImage(final Panmage img, final String pre, final String path, final String name, final Panple o, final Panple min, final Panple max) {
        if (img != null) {
            return img;
        }
        return Pangine.getEngine().createImage(pre + name, o, min, max, path + name + ".png");
    }
    
    protected EnemyProjectile newEnemyProjectile(final Panctor src, final int ox, final int oy, final float vx, final float vy) {
        if (!src.isInView()) {
            return null;
        }
        return new EnemyProjectile(src, ox, oy, vx, vy);
    }
    
    protected static class EnemyProjectile extends Pandy implements CollisionListener, AllOobListener {
        protected EnemyProjectile(final Panctor src, final int ox, final int oy, final float vx, final float vy) {
            this(BotsnBoltsGame.getEnemyProjectile(), src, ox, oy, vx, vy);
        }
        
        protected EnemyProjectile(final Panmage img, final Panctor src, final int ox, final int oy, final float vx, final float vy) {
            this(img, src, ox, oy, vx, vy, FinPanple.ORIGIN);
        }
        
        protected EnemyProjectile(final Panmage img, final Panctor src, final int ox, final int oy, final float vx, final float vy, final Panple g) {
            super(g);
            addBySource(this, img, src, ox, oy);
            getVelocity().set(vx, vy);
        }
        
        protected EnemyProjectile(final Panmage img, final float srcX, final float srcY, final boolean srcMirror, final int srcRot, final int ox, final int oy, final float vx, final float vy) {
            super(FinPanple.ORIGIN);
            addBySource(this, img, srcX, srcY, srcMirror, srcRot, ox, oy);
            getVelocity().set(vx, vy);
        }
        
        protected final static void addBySource(final Panctor toAdd, final Panmage img, final Panctor src, final int ox, final int oy) {
            final Panple srcPos = src.getPosition();
            addBySource(toAdd, img, srcPos.getX(), srcPos.getY(), src.isMirror(), src.getRot(), ox, oy);
        }
        
        protected final static void addBySource(final Panctor toAdd, final Panmage img, final float srcX, final float srcY, final boolean srcMirror, final int srcRot, final int ox, final int oy) {
            final boolean src180 = srcRot == 2;
            final boolean mirror = (srcMirror && !src180) || (!srcMirror && src180);
            final int mx = mirror ? -1 : 1;
            toAdd.getPosition().set(srcX + (mx * ox), srcY + oy, BotsnBoltsGame.DEPTH_PROJECTILE);
            toAdd.setMirror(mirror);
            toAdd.setView(img);
            BotsnBoltsGame.addActor(toAdd);
        }
        
        @Override
        public void onCollision(final CollisionEvent event) {
            final Collidable collider = event.getCollider();
            if (collider.getClass() == Player.class) {
                final Player player = (Player) collider;
                if (hurt(player)) {
                    burst(player);
                    if (isDestroyedOnImpact()) {
                        destroy();
                    }
                }
            }
        }
        
        protected boolean hurt(final Player player) {
            return player.hurt(getDamage());
        }
        
        protected int getDamage() {
            return 1;
        }
        
        protected void burst(final Player player) {
            burstEnemy(this);
        }
        
        protected final static void burstEnemy(final Panctor src) {
            burstEnemy(src, 0);
        }
        
        protected final static void burstEnemy(final Panctor src, final float offY) {
            final Panple loc = src.getPosition();
            Projectile.burst(src, BotsnBoltsGame.enemyBurst, loc.getX(), loc.getY() + offY);
        }
        
        protected boolean isDestroyedOnImpact() {
            return true;
        }
        
        @Override
        public void onAllOob(final AllOobEvent event) {
            destroy();
        }
        
        @Override
        public void onStep(final StepEvent event) {
            super.onStep(event);
            if (!isInView()) {
                onOutOfView();
            }
        }
        
        protected void onOutOfView() {
            destroy();
        }
    }
    
    protected static class TimedEnemyProjectile extends EnemyProjectile {
        int timer;
        
        protected TimedEnemyProjectile(final Panctor src, final int ox, final int oy, final int timer) {
            super(src, ox, oy, 0, 0);
            this.timer = timer;
        }
        
        protected TimedEnemyProjectile(final Panmage img, final Panctor src, final int ox, final int oy, final int timer) {
            this(img, src, ox, oy, 0, 0, timer);
        }
        
        protected TimedEnemyProjectile(final Panmage img, final Panctor src, final int ox, final int oy, final float vx, final float vy, final int timer) {
            super(img, src, ox, oy, vx, vy);
            this.timer = timer;
        }
        
        protected TimedEnemyProjectile(final Panmage img, final Panctor src, final int ox, final int oy, final float vx, final float vy, final Panple g, final int timer) {
            super(img, src, ox, oy, vx, vy, g);
            this.timer = timer;
        }
        
        protected TimedEnemyProjectile(final Panmage img, final float srcX, final float srcY, final boolean srcMirror, final int srcRot,
                                       final int ox, final int oy, final float vx, final float vy, final int timer) {
            super(img, srcX, srcY, srcMirror, srcRot, ox, oy, vx, vy);
            this.timer = timer;
        }
        
        @Override
        public void onStep(final StepEvent event) {
            super.onStep(event);
            if (timer <= 0) {
                onExpire();
                destroy();
            }
            timer--;
        }
        
        protected void onExpire() {
        }
    }
    
    protected final static class AnimationEnemyProjectile extends EnemyProjectile implements AnimationEndListener {
        protected AnimationEnemyProjectile(final Panimation anm, final Enemy src, final int ox, final int oy) {
            this(anm, src, ox, oy, 0, 0);
        }
        
        protected AnimationEnemyProjectile(final Panimation anm, final Enemy src, final int ox, final int oy, final float vx, final float vy) {
            super(src, ox, oy, vx, vy);
            setView(anm);
        }
        
        @Override
        public void onAnimationEnd(final AnimationEndEvent event) {
            destroy();
        }
    }
    
    protected final static class FreezeRayProjectile extends TimedEnemyProjectile {
        private final static int DURATION_FREEZE = 20;
        private final static int MAX_FADE = 6;
        private static Panmage freezeRayHead = null;
        private static Panmage freezeRayTail = null;
        
        private int index = 0;
        private int start = 0;
        private int end = 0;
        private final Pansplay display = new OriginPansplay(new FreezeRayMinimum(), new FreezeRayMaximum());
        
        protected FreezeRayProjectile(final Enemy src, final int ox, final int oy) {
            super(src, ox, oy, DURATION_FREEZE);
        }
        
        @Override
        protected final boolean hurt(final Player player) {
            return player.freeze();
        }
        
        @Override
        protected final void burst(final Player player) {
            final Panple pos = player.getPosition();
            Projectile.burst(this, BotsnBoltsGame.enemyBurst, pos.getX(), pos.getY() + Player.CENTER_Y);
        }
        
        @Override
        protected final boolean isDestroyedOnImpact() {
            return false;
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            super.onStep(event);
            index = DURATION_FREEZE - timer;
            start = getOffset(index - (DURATION_FREEZE - MAX_FADE));
            end = getOffset(index - 2);
        }
        
        private final static int getOffset(final int index) {
            if (index < 0) {
                return 0;
            }
            switch (index) {
                case 0 :
                    return 1;
                case 1 :
                    return 2;
                case 2 :
                    return 4;
                case 3 :
                    return 7;
                case 4 :
                    return 14;
                case 5 :
                    return 28;
                case MAX_FADE :
                    return 56;
                default :
                    return 96;
            }
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panlayer layer = getLayer();
            final Panple pos = getPosition();
            float x = pos.getX();
            final float y = pos.getY(), z = pos.getZ();
            final boolean mirror = isMirror();
            if (timer > (MAX_FADE + 1)) {
                renderer.render(layer, getTail(), x, y, z, 0, mirror, false);
            }
            if (index < 2) {
                return;
            }
            final Panmage head = getHead();
            final int off = getMirrorMultiplier() * 4;
            for (int i = 0; i < end; i++) {
                x += off;
                if (i < start) {
                    continue;
                }
                renderer.render(layer, head, x, y, z, 0, mirror, false);
            }
        }
        
        @Override
        public Pansplay getCurrentDisplay() {
            return display;
        }
        
        private final class FreezeRayMinimum extends UnmodPanple2 {
            @Override
            public final float getX() {
                return start * 4;
            }

            @Override
            public final float getY() {
                return 0;
            }
        }
        
        private final class FreezeRayMaximum extends UnmodPanple2 {
            @Override
            public final float getX() {
                return (end + 1) * 4;
            }

            @Override
            public final float getY() {
                return 4;
            }
        }
        
        protected final static Panmage getHead() {
            return (freezeRayHead = getFreezeRayImage(freezeRayHead, "FreezeRayHead"));
        }
        
        protected final static Panmage getTail() {
            return (freezeRayTail = getFreezeRayImage(freezeRayTail, "FreezeRayTail"));
        }
        
        protected final static Panmage getFreezeRayImage(final Panmage img, final String name) {
            return getImage(img, name, null, null, null);
        }
    }
    
    protected final static void newCube(final int x, final int y) {
        final TileMap tm = BotsnBoltsGame.tm;
        final int x1 = x + 1, y1 = y + 1;
        if (!ShootableDoor.isBigTileMode()) {
            final Tile solid = tm.getTile(null, null, Tile.BEHAVIOR_SOLID);
            tm.setTile(x1, y, solid);
            tm.setTile(x, y1, solid);
            tm.setTile(x1, y1, solid);
        }
        tm.setTile(x, y, tm.getTile(null, BotsnBoltsGame.getBox(), Tile.BEHAVIOR_SOLID));
    }
    
    protected abstract static class TileUnawareEnemy extends Enemy {
        protected TileUnawareEnemy(final int x, final int y, final int health) {
            super(0, 0, x, y, health);
        }
        
        protected TileUnawareEnemy(final Segment seg, final int health) {
            super(0, 0, seg, health);
        }
        
        @Override
        protected final boolean onStepCustom() {
            onStepEnemy();
            return true;
        }
        
        //@OverrideMe
        protected void onStepEnemy() {
        }
    }
    
    private final static int OFF_CUBE = 16;
    
    protected abstract static class CubeEnemy extends TileUnawareEnemy {
        protected final float baseX;
        protected final float baseY;
        
        protected CubeEnemy(final Segment seg, final int health) {
            super(seg, health);
            final Panple pos = getPosition();
            pos.add(OFF_CUBE, OFF_CUBE);
            baseX = pos.getX();
            baseY = pos.getY();
        }
        
        @Override
        protected final void initTileCoordinates(final int x, final int y) {
            newCube(x, y);
        }
        
        @Override
        protected final int getInitialOffsetX() {
            return 0;
        }
        
        @Override
        protected final void award(final PowerUp powerUp) {
            powerUp.liftRequired = true;
            PowerUp.addPowerUp(powerUp, baseX, baseY, 6);
        }
    }
    
    protected final static class PowerBox extends CubeEnemy {
        protected PowerBox(final Segment seg) {
            super(seg, 1);
            setView(getPlayerContext().pi.powerBox);
        }
        
        @Override
        protected final PowerUp pickAward(final Player player) {
            return new BigBattery(player);
        }
    }
    
    protected final static class DiskBox extends CubeEnemy {
        protected DiskBox(final Segment seg) {
            super(seg, 1);
            setView(getPlayerContext().pi.diskBox);
        }
        
        @Override
        protected final PowerUp pickAward(final Player player) {
            return new Disk(player, RoomLoader.getCurrentRoom().roomId);
        }
    }
    
    protected final static class BoltBox extends CubeEnemy {
        private final Upgrade upgrade;
        
        protected BoltBox(final Segment seg, final Upgrade upgrade) {
            super(seg, 1);
            final PlayerContext pc = getPlayerContext();
            if (pc.prf.upgrades.contains(upgrade)) {
                setView(pc.pi.powerBox);
                this.upgrade = null;
            } else {
                setView(upgrade.getBoxImage(pc));
                this.upgrade = upgrade;
            }
        }
        
        @Override
        protected final PowerUp pickAward(final Player player) {
            return (upgrade == null) ? new BigBattery(player) : new Bolt(player, upgrade);
        }
    }
    
    protected final static class SentryGun extends CubeEnemy {
        private int timer = 0;
        private int dir;
        
        protected SentryGun(final Segment seg) {
            super(seg, 5);
            setView(BotsnBoltsGame.sentryGun[0]);
            setLeft();
        }

        @Override
        public final void onStepEnemy() {
            final boolean shoot = charge();
            track(shoot);
        }
        
        private final boolean charge() {
            timer++;
            boolean shoot = false;
            final int imgIndex;
            if (timer >= 120) {
                shoot = true;
                timer = 120;
                imgIndex = 3;
            } else if (timer >= 90) {
                imgIndex = 3;
            } else if (timer >= 60) {
                imgIndex = 2;
            } else if (timer >= 30) {
                imgIndex = 1;
            } else {
                imgIndex = 0;
            }
            changeView(BotsnBoltsGame.sentryGun[imgIndex]);
            return shoot;
        }
        
        private final void track(final boolean shoot) {
            final Player target = getNearestPlayer();
            if (target == null) {
                return;
            }
            final Panple tarPos = target.getPosition();
            final float diffX = baseX - tarPos.getX(), diffY = baseY - tarPos.getY() - 14;
            if (Math.abs(diffX) >= (Math.abs(diffY) - 2)) {
                if (diffX > 5) {
                    setLeft();
                } else if (diffX < -5) {
                    setRight();
                }
            } else {
                if (diffY > 5) {
                    setDown();
                } else if (diffY < -5) {
                    setUp();
                }
            }
            if (shoot) {
                fire();
                timer = 0;
            }
        }
        
        private final void fire() {
            switch (dir) {
                case 0 :
                    newEnemyProjectile(this, 8, 0, VEL_PROJECTILE, 0);
                    break;
                case 1 :
                    newEnemyProjectile(this, 0, 9, 0, VEL_PROJECTILE);
                    break;
                case 3 :
                    newEnemyProjectile(this, 1, -8, 0, -VEL_PROJECTILE);
                    break;
                default :
                    newEnemyProjectile(this, 8, 0, -VEL_PROJECTILE, 0);
                    break;
            }
        }
        
        private final void setRight() {
            setDirection(0, false, 0, 2, 0);
        }
        
        private final void setLeft() {
            setDirection(2, true, 0, -3, 0);
        }
        
        private final void setUp() {
            setDirection(1, false, 1, -1, 2);
        }
        
        private final void setDown() {
            setDirection(3, false, 3, 0, -3);
        }
        
        private final void setDirection(final int dir, final boolean mirror, final int rot, final int offX, final int offY) {
            this.dir = dir;
            setMirror(mirror);
            setRot(rot);
            getPosition().set(baseX + offX, baseY + offY);
        }
    }
    
    protected static class WallCannon extends TileUnawareEnemy {
        private final static int DURATION = 60;
        private int timer;
        
        protected WallCannon(final int x, final int y, final int health) {
            super(x, y, health);
        }
        
        protected WallCannon(final Segment seg) {
            super(seg, 3);
        }
        
        @Override
        protected final void initTileCoordinates(final int x, final int y) {
            timer = isEarly(x, y) ? (DURATION / 2) : DURATION;
            setView(0);
            initCannon(x, y);
        }
        
        protected boolean isEarly(final int x, final int y) {
            return (y % 2) == 0;
        }
        
        protected void initCannon(final int x, final int y) {
            final Panple pos = getPosition();
            if (isSolidTile(x + 1, y)) {
                setMirror(true);
                pos.addX(7);
            } else {
                pos.addX(-8);
            }
        }
        
        @Override
        protected final void onStepEnemy() {
            timer--;
            if (timer == 6) {
                fire();
                setView(1);
            } else if (timer == 4) {
                setView(2);
            } else if (timer == 2) {
                setView(3);
            } else if (timer <= 0) {
                setView(0);
                timer = DURATION;
            }
        }
        
        protected void fire() {
            newEnemyProjectile(this, 12, 8, getMirrorMultiplier() * VEL_PROJECTILE, 0);
        }
        
        private final void setView(final int i) {
            setView(getView(i));
        }
        
        protected Panmage getView(final int i) {
            return BotsnBoltsGame.wallCannon[i];
        }
        
        @Override
        protected final void award(final PowerUp powerUp) {
            award(powerUp, isMirror() ? -7 : 8, 0);
        }
    }
    
    private final static Panple CEILING_CANNON_MIN = new FinPanple2(2, 3);
    private final static Panple CEILING_CANNON_MAX = new FinPanple2(14, 15);
    
    protected final static class CeilingCannon extends TileUnawareEnemy {
        private final static int DURATION = 120;
        private final static Panmage[] images = new Panmage[3];
        private int timer;
        
        protected CeilingCannon(final Segment seg) {
            super(seg, 3);
            setDown();
        }
        
        @Override
        protected final int getInitialOffsetX() {
            return 0;
        }
        
        @Override
        protected final void onStepEnemy() {
            timer--;
            switch (timer) {
                case 105 :
                    fireDown();
                    break;
                case 90 :
                    setView(0); // Left
                    break;
                case 75 :
                    fire45(1, -1); // Left
                    break;
                case 60 :
                    setDown();
                    break;
                case 45 :
                    fireDown();
                    break;
                case 30 :
                    setView(2); // Right
                    break;
                case 15 :
                    fire45(15, 1); // Right
                    break;
            }
            if (timer <= 0) {
                setDown();
                timer = DURATION;
            }
        }
        
        private final void setView(final int i) {
            changeView(getImage(i));
        }
        
        private final void setDown() {
            setView(1);
        }
        
        private final void fire(final int ox, final int oy, final float vx, final float vy) {
            newEnemyProjectile(this, ox, oy, vx, vy);
        }
        
        private final void fireDown() {
            fire(8, 1, 0, -VEL_PROJECTILE);
        }
        
        private final void fire45(final int ox, final int vxMult) {
            fire(ox, 3, vxMult * VEL_PROJECTILE_45, -VEL_PROJECTILE_45);
        }
        
        private final static Panmage getImage(final int i) {
            Panmage image = images[i];
            if (image != null) {
                return image;
            }
            image = getImage(image, "CeilingCannon" + (i + 1), null, CEILING_CANNON_MIN, CEILING_CANNON_MAX);
            images[i] = image;
            return image;
        }
    }
    
    private final static int ROCKET_OFF_X = 12;
    private final static int ROCKET_H = 14;
    
    protected final static class Rocket extends Enemy {
        private static Panmage img = null;
        private final boolean vertical;
        private int timer = 0;
        
        protected Rocket(final int x, final int y) {
            super(ROCKET_OFF_X, ROCKET_H, x, y, 1);
            setView(getRocketImage());
            vertical = getPosition().getY() < 1;
            setMirror(true);
            if (vertical) {
                setRot(1);
                getPosition().add(-6, -18);
                hv = VEL_PROJECTILE;
            } else {
                if (isInView()) {
                    setVisible(false);
                    destroy();
                    return;
                }
                hv = (x < 0) ? VEL_PROJECTILE : -VEL_PROJECTILE;
            }
        }
        
        @Override
        protected final boolean onStepCustom() {
            if (vertical) {
                final Panple pos = getPosition();
                final float y = pos.getY() + hv;
                if (y > (BotsnBoltsGame.GAME_H + 16)) {
                    destroy();
                } else {
                    pos.setY(y);
                }
            } else if (addX(hv) != X_NORMAL) {
                burst();
            }
            timer++;
            if (timer >= 8) {
                if (vertical) {
                    Player.puff(this, Mathtil.randi(3, 8), -hv * 2);
                } else {
                    Player.puff(this, -hv * 2, Mathtil.randi(4, 9));
                }
                timer = 0;
            }
            return true;
        }
        
        private final void burst() {
            EnemyProjectile.burstEnemy(this, 8);
            destroy();
        }
        
        private final static Panmage getRocketImage() {
            if (img != null) {
                return img;
            }
            return (img = getImage(img, "Rocket", new FinPanple2(14, 5), getMin(ROCKET_OFF_X), getMax(ROCKET_OFF_X, ROCKET_H)));
        }
    }
    
    protected final static int PROP_HEALTH = 2, PROP_OFF_X = 4, PROP_H = 12, CRAWL_H = 14;
    
    protected final static class PropEnemy extends Enemy {
        protected PropEnemy(final Segment seg) {
            super(PROP_OFF_X, PROP_H, seg, PROP_HEALTH);
            setView(BotsnBoltsGame.propEnemy);
        }
        
        @Override
        protected final boolean onStepCustom() {
            final Player target = getNearestPlayer();
            if (target == null) {
                return true;
            }
            final Panple pos = getPosition(), targetPos = target.getPosition();
            final float x = pos.getX(), targetX = targetPos.getX();
            if (!isInView() && (Math.abs(targetX - x) > 224)) {
                return true;
            }
            hv = getDirection(x, targetX, -2, 2);
            updateMirror();
            v = getDirection(pos.getY(), targetPos.getY(), -7, -3);
            addX(hv);
            addY();
            return true;
        }
    }
    
    private abstract static class JumpEnemy extends Enemy implements RoomAddListener {
        protected TimerListener timer = null;
        
        protected JumpEnemy(final int offX, final int h, final int x, final int y, final int health) {
            super(offX, h, x, y, health);
        }
        
        protected JumpEnemy(final int offX, final int h, final Segment seg, final int health) {
            super(offX, h, seg, health);
        }
        
        @Override
        public final void onRoomAdd(final RoomAddEvent event) {
            if (isMirrorable()) {
                turnTowardPlayer();
            }
            schedule();
        }
        
        protected final void schedule() {
            if (timer != null) {
                return;
            }
            onSchedule();
            clearSchedule();
            Pangine.getEngine().addTimer(this, getDelay(), timer = new TimerListener() {
                @Override
                public final void onTimer(final TimerEvent event) {
                    appointment();
                }});
        }
        
        protected void onSchedule() {
        }
        
        protected final void clearSchedule() {
            if (timer == null) {
                return;
            }
            Pangine.getEngine().removeTimer(timer);
            timer = null;
        }
        
        protected int getDelay() {
            return 30;
        }
        
        protected final void appointment() {
            if ((timer == null) || !canJump()) {
                schedule();
                return;
            }
            clearSchedule();
            if (isAppointmentAllowed()) {
                onAppointment();
            } else {
                schedule();
            }
        }
        
        protected boolean isAppointmentAllowed() {
            return isInView();
        }
        
        protected void onAppointment() {
            jump();
        }
        
        protected final void jump() {
            if (!canJump()) {
                schedule();
                return;
            }
            onJump();
        }
        
        protected boolean canJump() {
            return isGrounded();
        }
        
        protected abstract void onJump();
        
        protected final void hold(final int delay) {
            Pangine.getEngine().addTimer(this, delay, new TimerListener() {
                @Override public final void onTimer(final TimerEvent event) {
                    schedule();
                }});
        }
    }
    
    protected final static class SpringEnemy extends JumpEnemy {
        protected SpringEnemy(final Segment seg) {
            super(PROP_OFF_X, PROP_H, seg, PROP_HEALTH);
            endSpring();
        }
        
        @Override
        protected final void onJump() {
            turnTowardPlayer();
            hv = getMirrorMultiplier();
            v = 2;
            addY();
            v = 8;
            setView(BotsnBoltsGame.springEnemy[1]);
        }
        
        private final void endSpring() {
            setView(BotsnBoltsGame.springEnemy[0]);
        }
        
        @Override
        protected final boolean onStepCustom() {
            if (v < 0) {
                endSpring();
            }
            return false;
        }
        
        @Override
        protected final void onLanded() {
            super.onLanded();
            hv = 0;
            endSpring();
            schedule();
        }
        
        @Override
        protected final void onBump(final int t) {
            endSpring();
        }
    }
    
    protected final static class CrawlEnemy extends TileUnawareEnemy {
        private final static int VEL = 1;
        private final static int DURATION = 16 / VEL;
        private int tileIndex;
        private Direction surfaceDirection;
        private int velX;
        private int velY;
        private int timer = 8;
        
        protected CrawlEnemy(final Segment seg) {
            super(seg, PROP_HEALTH);
        }
        
        @Override
        protected final void initTileCoordinates(final int x, final int y) {
            final TileMap tm = BotsnBoltsGame.tm;
            tileIndex = tm.getIndexRequired(x, y);
            for (final Direction dir : Direction.values()) {
                final int surfaceTileIndex = tm.getNeighbor(tileIndex, dir);
                if (isSolidIndex(surfaceTileIndex)) {
                    initSurfaceDirection(dir);
                    break;
                }
            }
            setView(BotsnBoltsGame.crawlEnemy);
            setMirror(true);
        }
        
        private final void initSurfaceDirection(final Direction surfaceDirection) {
            final Panple pos = getPosition();
            if (surfaceDirection == Direction.West) {
                pos.add(-8, 8);
            } else if (surfaceDirection == Direction.North) {
                pos.addY(16);
            } else if (surfaceDirection == Direction.East) {
                pos.add(8, 8);
            }
            setSurfaceDirection(surfaceDirection);
        }
        
        private final void setSurfaceDirection(final Direction surfaceDirection) {
            final Panple pos = getPosition();
            final Direction oldDirection = this.surfaceDirection;
            this.surfaceDirection = surfaceDirection;
            if (oldDirection == Direction.North) {
                pos.addY(1);
            } else if (oldDirection == Direction.East) {
                pos.addX(1);
            }
            if (surfaceDirection == Direction.South) {
                velX = -1;
                velY = 0;
                setRot(0);
            } else if (surfaceDirection == Direction.West) {
                velX = 0;
                velY = 1;
                setRot(1);
            } else if (surfaceDirection == Direction.North) {
                velX = 1;
                velY = 0;
                setRot(2);
                pos.addY(-1);
            } else if (surfaceDirection == Direction.East) {
                velX = 0;
                velY = -1;
                setRot(3);
                pos.addX(-1);
            } else {
                throw new IllegalStateException("Unexpected Direction: " + surfaceDirection);
            }
        }
        
        private final void updateSurfaceDirection() {
            final TileMap tm = BotsnBoltsGame.tm;
            final Direction velocityDirection = surfaceDirection.getClockwise();
            final int oldIndex = tileIndex;
            tileIndex = tm.getNeighbor(tileIndex, velocityDirection);
            final int surfaceTileIndex = tm.getNeighbor(tileIndex, surfaceDirection);
            if (!isSolidIndex(surfaceTileIndex)) {
                tileIndex = BotsnBoltsGame.tm.getNeighbor(tileIndex, surfaceDirection);
                setSurfaceDirection(surfaceDirection.getCounterclockwise());
            } else if (isSolidIndex(tileIndex)) {
                tileIndex = oldIndex;
                setSurfaceDirection(velocityDirection);
            }
        }
        
        @Override
        protected final void onStepEnemy() {
            getPosition().add(velX, velY);
            timer++;
            if (timer >= DURATION) {
                updateSurfaceDirection();
                timer = 0;
            }
        }
    }
    
    // Shield covers enemy's face; can only shoot enemy's back
    protected final static class ShieldedEnemy extends Enemy {
        private final static int SPEED = 2;
        private boolean shielded = true;
        
        protected ShieldedEnemy(final Segment seg) {
            super(PROP_OFF_X, PROP_H, seg, PROP_HEALTH);
            getPosition().addY(2);
            hv = 0;
            setView(BotsnBoltsGame.shieldedEnemy);
        }
        
        private final void initDirection() {
            turnTowardPlayer();
            hv = isMirror() ? -SPEED : SPEED;
        }

        @Override
        protected final boolean isVulnerableToProjectile(final Projectile prj) {
            return (prj.power >= Projectile.POWER_MAXIMUM) || isExposedToProjectile(prj);
        }
        
        protected final boolean isExposedToProjectile(final Projectile prj) {
            if (!shielded) {
                return true;
            }
            final float pvx = prj.getVelocity().getX();
            if (pvx < 0) {
                return isMirror();
            } else if (pvx > 0) {
                return !isMirror();
            }
            return true;
        }
        
        @Override
        protected void onHurt(final Projectile prj) {
            if (shielded && !isExposedToProjectile(prj)) {
                shielded = false;
                prj.burst(this);
                prj.setPower(0);
                setView(BotsnBoltsGame.unshieldedEnemy);
            } else {
                super.onHurt(prj);
            }
        }
        
        @Override
        protected final boolean onStepCustom() {
            if (hv == 0) {
                initDirection();
            }
            if (addX(hv) != X_NORMAL) {
                hv *= -1;
                updateMirror();
            }
            return true;
        }
    }
    
    private final static int DRILL_HEALTH = 1;
    
    protected static class DrillEnemy extends Enemy {
        private final static int NUM_IMAGES = 8;
        private final static int FRAME_DURATION = 4;
        private final static int TOTAL_DURATION = FRAME_DURATION * NUM_IMAGES;
        private final static Panmage[] drillImgs = new Panmage[NUM_IMAGES];
        private static Panmage dirtShatter = null;
        private static TileMapImage edgeLeft = null;
        private static TileMapImage edgeRight = null;
        private static TileMapImage edgeBottom = null;
        private int animTimer = 0;
        private int digTimer = 0;
        private Panctor partialTileLeft = null;
        private Panctor partialTileRight = null;
        private final boolean respawnable;
        private int x;
        
        protected DrillEnemy(final Segment seg) {
            super(PROP_OFF_X, CRAWL_H, seg, DRILL_HEALTH);
            respawnable = seg.getBoolean(3, false);
        }
        
        protected DrillEnemy(final int x, final int y) {
            super(PROP_OFF_X, CRAWL_H, x, y, DRILL_HEALTH);
            respawnable = true;
        }
        
        {
            getPosition().setZ(BotsnBoltsGame.DEPTH_BETWEEN);
            hv = 0;
            setCurrentView();
        }
        
        @Override
        protected final void initTileCoordinates(final int x, final int y) {
            this.x = x;
        }
        
        private final void setCurrentView() {
            final int i = animTimer / FRAME_DURATION;
            Panmage img = drillImgs[i];
            if (img == null) {
                img = getImage(null, "DrillEnemy" + (i + 1), BotsnBoltsGame.crawlEnemy.getFrames()[0].getImage());
                drillImgs[i] = img;
            } else {
                getDirtShatter(); // Load the image if needed
            }
            changeView(img);
        }
        
        @Override
        protected final boolean onStepCustom() {
            animate();
            return dig();
        }
        
        private final void animate() {
            animTimer++;
            if (animTimer >= TOTAL_DURATION) {
                animTimer = 0;
            }
            setCurrentView();
        }
        
        private final boolean dig() {
            if (digTimer <= 0 && isGrounded()) {
                digTimer = 16;
            }
            if (digTimer > 0) {
                digTimer--;
                if (digTimer == 10) {
                    final TileMap tm = BotsnBoltsGame.tm;
                    final int index = tm.getContainer(this);
                    tm.setTile(index, null);
                    if (edgeLeft == null) {
                        final TileMapImage edgeRaw = BotsnBoltsGame.imgMap[1][3];
                        final int offZ = BotsnBoltsGame.DEPTH_ABOVE - BotsnBoltsGame.DEPTH_BG;
                        edgeLeft = new AdjustedTileMapImage(edgeRaw, offZ, 0, false, false);
                        edgeRight = new AdjustedTileMapImage(edgeRaw, offZ, 0, true, false);
                        edgeBottom = new AdjustedTileMapImage(edgeRaw, offZ, 1, false, false);
                    }
                    replaceEdge(tm, index, Direction.West, edgeLeft);
                    replaceEdge(tm, index, Direction.East, edgeRight);
                    replaceEdge(tm, index, Direction.South, edgeBottom);
                    shatter(-2);
                    partialTileLeft = newPartialTile(true);
                    partialTileRight = newPartialTile(false);
                } else if (digTimer == 2) {
                    destroyPartialTiles();
                }
                getPosition().addY(-1);
                return true;
            }
            return false;
        }
        
        private final void shatter(final int offY) {
            final boolean left = Mathtil.rand();
            Player.shatter(this, getDirtShatter(), offY, left, !left, false, false);
        }
        
        private final Panctor newPartialTile(final boolean left) {
            final Panlayer layer = getLayer();
            if (layer == null) {
                return null;
            }
            final Panctor actor = new Panctor();
            actor.setView(getDirtShatter());
            final Panple pos = getPosition();
            final int xoff = left ? -4 : 4;
            actor.getPosition().set(pos.getX() + xoff, pos.getY() - 7, BotsnBoltsGame.DEPTH_BURST);
            layer.addActor(actor);
            return actor;
        }
        
        private final void replaceEdge(final TileMap tm, final int index, final Direction dir, final TileMapImage edgeImg) {
            final int edgeIndex = tm.getNeighbor(index, dir);
            if (!isSolidIndex(edgeIndex)) {
                return;
            }
            tm.setBackground(edgeIndex, edgeImg, Tile.BEHAVIOR_SOLID);
        }
        
        private final void destroyPartialTiles() {
            if (partialTileLeft != null) {
                shatter(-2);
                partialTileLeft.destroy();
                partialTileRight.destroy();
                partialTileLeft = null;
                partialTileRight = null;
                final TileMap tm = BotsnBoltsGame.tm;
                removeShadow(tm, tm.getContainer(this));
            }
        }
        
        protected final static void removeShadow(final TileMap tm, final int index) {
            final int edgeIndex = tm.getNeighbor(index, Direction.South);
            if (tm.getTile(edgeIndex) == RoomLoader.getTile('D')) {
                tm.setTile(edgeIndex, null);
            }
        }
        
        @Override
        protected final void onEnemyDestroy() {
            destroyPartialTiles();
        }
        
        protected final static Panmage getDirtShatter() {
            if (dirtShatter == null) {
                dirtShatter = Pangine.getEngine().createImage("dirt.shatter", BotsnBoltsGame.CENTER_8, null, null, BotsnBoltsGame.RES + "misc/DirtShatter.png");
            }
            return dirtShatter;
        }
        
        @Override
        public final void onDefeat() {
            if (!respawnable) {
                return;
            }
            addRoomTimer(30, new RoomTimerListener() {
                @Override public final void onTimer() {
                    BotsnBoltsGame.addActor(new DrillEnemy(x, 14));
            }});
        }
    }
    
    protected final static void addRoomTimer(final long duration, final RoomTimerListener listener) {
        Pangine.getEngine().addTimer(BotsnBoltsGame.tm, duration, listener);
    }
    
    protected abstract static class RoomTimerListener implements TimerListener {
        @Override
        public final void onTimer(final TimerEvent event) {
            if (RoomChanger.isChanging()) {
                return;
            }
            onTimer();
        }
        
        protected abstract void onTimer();
    }
    
    protected final static class WalkerEnemy extends Enemy {
        private final static int numImages = 6;
        private final static Panmage[] images = new Panmage[numImages];
        private int timer = -1;
        private int i = 0;
        
        protected WalkerEnemy(final Segment seg) {
            super(PROP_OFF_X, CRAWL_H, seg, PROP_HEALTH);
            setView(getImage(0));
            setMirror(true);
        }
        
        @Override
        protected final boolean onStepCustom() {
            timer++;
            final int frameDuration = ((i % 3) == 0) ? 6 : 3;
            if (timer < frameDuration) {
                return false;
            }
            timer = 0;
            if (addX(2 * getMirrorMultiplier()) != X_NORMAL) {
                turn();
            }
            i++;
            if (i >= numImages) {
                i = 0;
            }
            setView(getImage(i));
            return false;
        }
        
        @Override
        protected final boolean onHorizontal(final int off) {
            if (isOnEdge(off)) {
                turn();
                addX(getMirrorMultiplier());
                return true;
            }
            return false;
        }
        
        private final void turn() {
            setMirror(!isMirror());
        }
        
        private final static Panmage getImage(final int i) {
            Panmage image = images[i];
            if (image != null) {
                return image;
            }
            image = getImage(image, "WalkerEnemy" + (i + 1), BotsnBoltsGame.crawlEnemy.getFrames()[0].getImage());
            images[i] = image;
            return image;
        }
    }
    
    private final static Panple GLIDER_MIN = new FinPanple2(1, 0);
    private final static Panple GLIDER_MAX = new FinPanple2(15, 16);
    
    protected final static class GliderEnemy extends TileUnawareEnemy {
        private final static Panmage[] images = new Panmage[3];
        private final int velX;
        private final int velY;
        private int timer = 60;
        private int dir = 0;
        private int sleepTimer = 0;
        
        protected GliderEnemy(final Segment seg) {
            super(seg, PROP_HEALTH);
            setView(getImage(0));
            this.velX = seg.intValue(3);
            this.velY = seg.intValue(4);
        }
        
        @Override
        protected final int getInitialOffsetX() {
            return 0;
        }
        
        @Override
        protected final boolean isMirrorable() {
            return false;
        }
        
        @Override
        public final void onStepEnemy() {
            sleepIfNeeded();
            handleTimer();
            if (dir != 0) {
                move();
            }
        }
        
        private final void sleepIfNeeded() {
            if (sleepTimer > 0) {
                sleepTimer--;
                if (sleepTimer == 0) {
                    changeView(0);
                }
            }
        }
        
        private final void handleTimer() {
            timer++;
            if (timer >= 120) {
                startMove();
                timer = 0;
            } else if (timer >= 115) {
                changeView(2);
            } else if (timer >= 110) {
                changeView(1);
            }
        }
        
        private final void move() {
            final int speed;
            if (timer < 2) {
                speed = 1;
            } else if (timer < 3) {
                speed = 2;
            } else {
                speed = 4;
            }
            final int mult = dir * speed;
            final int vx = velX * mult, vy = velY * mult;
            final Panple pos = getPosition();
            final int offX = (vx > 0) ? 15 : 0, offY = (vy > 0) ? 15 : 0;
            if (isSolidIndex(BotsnBoltsGame.tm.getContainer(pos.getX() + vx + offX, pos.getY() + vy + offY))) {
                dir = 0;
                changeView(1);
                sleepTimer = 5;
            } else {
                getPosition().add(vx, vy);
            }
        }
        
        private final void startMove() {
            final TileMap tm = BotsnBoltsGame.tm;
            final int index = tm.getContainer(this);
            final int row = tm.getRow(index), col = tm.getColumn(index);
            dir = isSolidTile(col + velX, row + velY) ? -1 : 1;
        }
        
        private final void changeView(final int i) {
            changeView(getImage(i));
        }
        
        private final static Panmage getImage(final int i) {
            Panmage image = images[i];
            if (image != null) {
                return image;
            }
            image = getImage(image, "GliderEnemy" + (i + 1), null, GLIDER_MIN, GLIDER_MAX);
            images[i] = image;
            return image;
        }
    }
    
    protected final static class SubEnemy extends Enemy {
        private final static Panmage[] images = new Panmage[2];
        
        protected SubEnemy(final Segment seg) {
            super(PROP_OFF_X, PROP_H, seg, PROP_HEALTH);
            setMirror(false);
            hv = -1;
            setView(getCurrentImage());
        }
        
        @Override
        protected final boolean onStepCustom() {
            changeView(getCurrentImage());
            moveX();
            moveY();
            bubbleRandom(8, 3);
            return true;
        }
        
        private final void moveX() {
            if (addX(hv) != X_NORMAL){
                hv *= -1;
                updateMirror();
            }
        }
        
        private final void moveY() {
            final Player p = getNearestPlayer();
            if (p == null) {
                return;
            }
            final float py = p.getPosition().getY() + Player.CENTER_Y;
            final float y = getPosition().getY() + 6;
            if (py > (y + 4)) {
                if (y < (RoomLoader.waterLevel - 10)) {
                    addY(1);
                }
            } else if (py < (y - 4)) {
                addY(-1);
            }
        }
        
        private final static Panmage getCurrentImage() {
            return getImage(Pangine.getEngine().isOn(4) ? 0 : 1);
        }
        
        private final static Panmage getImage(final int i) {
            Panmage image = images[i];
            if (image != null) {
                return image;
            }
            image = getImage(image, "SubEnemy" + (i + 1), BotsnBoltsGame.unshieldedEnemy.getFrames()[0].getImage());
            images[i] = image;
            return image;
        }
    }
    
    protected final static class SaucerEnemy extends Enemy {
        private final static Panmage[] images = new Panmage[3];
        private final static int speedMultiplier = 3;
        private int speedMultiplied = speedMultiplier;
        
        protected SaucerEnemy(final Segment seg) {
            super(PROP_OFF_X, CRAWL_H, seg, PROP_HEALTH);
            setView(getCurrentImage());
            setMirror(true);
            hv = -1;
            v = -1;
        }
        
        @Override
        protected final boolean onStepCustom() {
            changeView(getCurrentImage());
            final long sinceLastShot = Pangine.getEngine().getClock() - Player.lastShotByAnyPlayer;
            final int desiredSpeedMultiplied = ((sinceLastShot < 45) ? 4 : 1) * speedMultiplier;
            if (speedMultiplied < desiredSpeedMultiplied) {
                speedMultiplied++;
            } else if (speedMultiplied > desiredSpeedMultiplied) {
                speedMultiplied--;
            }
            final int speed = speedMultiplied / speedMultiplier;
            for (int i = 0; i < speed; i++) {
                move();
            }
            return true;
        }
        
        private final void move() {
            if (addX(hv) != X_NORMAL) {
                hv *= -1;
                setMirror(!isMirror());
            }
            if (addY(v) != Y_NORMAL) {
                v *= -1;
            }
        }
        
        @Override
        protected final void onLanded() {
            // Skip parent logic of clearing v
        }
        
        private final static Panmage getImage(final int i) {
            Panmage image = images[i];
            if (image != null) {
                return image;
            }
            image = getImage(image, "SaucerEnemy" + (i + 1), BotsnBoltsGame.crawlEnemy.getFrames()[0].getImage());
            images[i] = image;
            return image;
        }
        
        private final static Panmage getCurrentImage() {
            return getImage(((int) (Pangine.getEngine().getClock() % 9)) / 3);
        }
    }
    
    private final static Panple GUARDED_MIN = new FinPanple2(4, 0);
    private final static Panple GUARDED_MAX = new FinPanple2(12, 16);
    
    // Guards itself for a while; then lowers guard to attack
    protected final static class GuardedEnemy extends Enemy {
        private final static int DURATION_MOVE = 64;
        private final static int MULT_JET = 2;
        private final static int DURATION_JET = MULT_JET * 3;
        private final static byte MODE_MOVE = 0;
        private final static byte MODE_OPEN = 1;
        private final static byte MODE_ATTACK = 2;
        private final static byte MODE_CLOSE = 3;
        private final static Panmage[] images = new Panmage[4];
        private final Segment seg;
        private final int vv;
        private int timer;
        private int frame = 0;
        private byte mode;
        private int jetTimer = (MULT_JET * 2) - 1;
        private List<Segment> siblings = null;
        
        protected GuardedEnemy(final Segment seg) {
            super(7, 15, seg, 1);
            this.seg = seg;
            vv = seg.getInt(3, 0);
            final Panple pos = getPosition();
            final float x = pos.getX();
            if (vv != 0) {
                setMirror(true);
                pos.setX(x + 7);
            } else if (x > 192) {
                setMirror(true);
                pos.setX(x - 1);
            }
            startMove();
            setView();
        }
        
        private final void startMode(final byte mode) {
            timer = 0;
            this.mode = mode;
        }
        
        private final void startMove() {
            startMode(MODE_MOVE);
            if (vv == 0) {
                hv = 2 * getMirrorMultiplier();
            } else {
                v = vv;
            }
        }
        
        @Override
        protected final boolean onStepCustom() {
            timer++;
            jetTimer++;
            if (jetTimer >= DURATION_JET) {
                jetTimer = 0;
            }
            switch (mode) {
                case MODE_MOVE :
                    onStepMove();
                    break;
                case MODE_OPEN :
                    onStepOpen();
                    break;
                case MODE_ATTACK :
                    onStepAttack();
                    break;
                case MODE_CLOSE :
                    onStepClose();
                    break;
            }
            return true;
        }
        
        private final void onStepMove() {
            if (hv != 0) {
                if (onStepX()) {
                    return;
                }
            }
            if (v != 0) {
                if (onStepY()) {
                    return;
                }
            }
            startOpenIfNeeded();
        }
        
        private final boolean onStepX() {
            final int xStatus = addX(hv);
            if ((xStatus == X_START) || (xStatus == X_END)) {
                respawnAndDestroy();
                return true;
            }
            return false;
        }
        
        private final boolean onStepY() {
            final Panple pos = getPosition();
            pos.addY(v);
            final float y = pos.getY();
            if ((v < 0) && (y < -16)) {
                respawnAndDestroy();
                return true;
            } else if ((v > 0) && (y > BotsnBoltsGame.GAME_H)) {
                respawnAndDestroy();
                return true;
            }
            return false;
        }
        
        private final void startOpenIfNeeded() {
            if (timer >= DURATION_MOVE) {
                hv = 0;
                v = 0;
                startMode(MODE_OPEN);
            }
        }
        
        private final void onStepOpen() {
            if (animate(1, 3)) {
                startMode(MODE_ATTACK);
            }
        }
        
        private final void onStepAttack() {
            if (timer == 8) {
                if (!isInView()) {
                    return;
                }
                final int m = getMirrorMultiplier(), ox = 10, oy = 8; // EnemyProjectile applies mirror multiplier to ox
                final float m45 = m * VEL_PROJECTILE_45;
                new EnemyProjectile(this, ox, oy, m45, VEL_PROJECTILE_45);
                new EnemyProjectile(this, ox, oy, m * VEL_PROJECTILE, 0);
                new EnemyProjectile(this, ox, oy, m45, -VEL_PROJECTILE_45);
            } else if (timer >= 16) {
                startMode(MODE_CLOSE);
            }
        }
        
        private final void onStepClose() {
            if (animate(-1, 0)) {
                startMove();
            }
        }
        
        private final boolean animate(final int frameDir, final int lastFrame) {
            if (timer >= 2) {
                timer = 0;
                if (frame == lastFrame) {
                    return true;
                } else {
                    frame += frameDir;
                    setView();
                }
            }
            return false;
        }
        
        @Override
        protected final boolean isVulnerableToProjectile(final Projectile prj) {
            return (frame > 1) && (prj.isMirror() != isMirror());
        }
        
        private final void respawn() {
            addRoomTimer(32, new RoomTimerListener() {
                @Override public final void onTimer() {
                    BotsnBoltsGame.addActor(new GuardedEnemy(seg));
                    if (siblings != null) {
                        for (final Segment sibling : siblings) {
                            BotsnBoltsGame.addActor(new GuardedEnemy(sibling));
                        }
                    }
            }});
        }
        
        private final void respawnAndDestroy() {
            respawn();
            destroy();
        }
        
        @Override
        public final void onDefeat() {
            final GuardedEnemy sibling = getSibling();
            if (sibling == null) {
                respawn();
            } else {
                if (sibling.siblings == null) {
                    sibling.siblings = (siblings == null) ? new ArrayList<Segment>(2) : siblings;
                } else if (siblings != null) {
                    sibling.siblings.addAll(siblings);
                }
                sibling.siblings.add(seg);
            }
        }
        
        private final GuardedEnemy getSibling() {
            for (final Panctor actor : getLayer().getActors()) {
                if ((actor == this) || actor.isDestroyed() || (actor.getClass() != GuardedEnemy.class)) {
                    continue;
                }
                return (GuardedEnemy) actor;
            }
            return null;
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            super.renderView(renderer);
            final int jetIndex = jetTimer / MULT_JET;
            if (jetIndex == 2) {
                return;
            }
            final Panple pos = getPosition();
            renderer.render(getLayer(), getJetImage(jetIndex), pos.getX() + (6 * getMirrorMultiplier()), pos.getY() - 2, pos.getZ(), 0, isMirror(), false);
        }
        
        private final void setView() {
            changeView(getImage(frame));
        }
        
        private final static Panmage getImage(final int i) {
            Panmage image = images[i];
            if (image != null) {
                return image;
            }
            image = getImage(image, "GuardedEnemy" + (i + 1), null, GUARDED_MIN, GUARDED_MAX);
            images[i] = image;
            return image;
        }
    }
    
    private final static Panmage[] jetImages = new Panmage[2];
    
    private final static Panmage getJetImage(final int i) {
        Panmage image = jetImages[i];
        if (image != null) {
            return image;
        }
        image = getImage(image, "Jet" + (i + 1), null, null, null);
        jetImages[i] = image;
        return image;
    }
    
    protected final static int SLIDE_H = 12;
    protected final static Panple SLIDE_MAX = getMax(PROP_OFF_X, SLIDE_H);
    protected final static Panple SLIDE_O = new FinPanple2(8, 0);
    protected final static Panple SLIDE_O_MIRROR = new FinPanple2(7, 0);
    
    // Slides back and forth along the ground
    protected final static class SlideEnemy extends Enemy {
        private final static int SLIDE_VELOCITY = 1;
        private final static int SLIDE_VELOCITY_NEAR = 4;
        private final static long DURATION_FREEZE = 30;
        private final static Panframe[] frames = new Panframe[5];
        private final static Panmage[] images = new Panmage[3];
        private long lastHurt = NULL_CLOCK;
        private int oldHv;
        
        protected SlideEnemy(final Segment seg) {
            super(PROP_OFF_X, SLIDE_H, seg, 5);
            hv = -SLIDE_VELOCITY;
            oldHv = hv;
        }
        
        @Override
        protected final boolean onStepCustom() {
            final long clock = Pangine.getEngine().getClock();
            if ((clock - lastHurt) <= DURATION_FREEZE) {
                if (hv != 0) {
                    oldHv = hv;
                }
                hv = 0;
                return false;
            }
            final int frameDuration = 3;
            setView(getFrame((int) (clock % (5 * frameDuration)) / frameDuration));
            setVelocity();
            return false;
        }
        
        private final void setVelocity() {
            final Player p = getNearestPlayer();
            if (p == null) {
                return;
            }
            final float diff = Math.abs(p.getPosition().getY() - getPosition().getY());
            final int speed = (diff < 4) ? SLIDE_VELOCITY_NEAR : SLIDE_VELOCITY;
            if (hv == 0) {
                hv = oldHv;
                if (hv == 0) {
                    hv = -SLIDE_VELOCITY;
                }
            }
            hv = speed * hv / Math.abs(hv);
        }
        
        @Override
        protected final void onStepEnd() {
            setMirror(false);
        }
        
        @Override
        protected final void onWall(final byte xResult) {
            hv *= -1;
        }
        
        @Override
        protected final boolean onHorizontal(final int off) {
            return onHorizontalEdgeTurn(8 * off) || onHorizontalEdgeTurn(off);
        }
        
        @Override
        protected final void onHurt(final Projectile prj) {
            super.onHurt(prj);
            lastHurt = Pangine.getEngine().getClock();
        }
        
        private final static Panframe getFrame(final int i) {
            Panframe frame = frames[i];
            if (frame != null) {
                return frame;
            }
            final int ii;
            final boolean mirror;
            final Panple o;
            if (i < 3) {
                ii = i;
                mirror = false;
                o = null;
            } else {
                ii = (4 - i);
                mirror = true;
                o = SLIDE_O_MIRROR;
            }
            final Panmage image = getImage(ii);
            frame = Pangine.getEngine().createFrame(BotsnBoltsGame.PRE_FRM + "slide." + i, image, 3, 0, mirror, false, o, null, null);
            frames[i] = frame;
            return frame;
        }
        
        private final static Panmage getImage(final int i) {
            Panmage image = images[i];
            if (image != null) {
                return image;
            }
            final Panmage ref = BotsnBoltsGame.propEnemy.getFrames()[0].getImage();
            image = getImage(image, "SlideEnemy" + (i + 1), SLIDE_O, ref.getBoundingMinimum(), SLIDE_MAX);
            images[i] = image;
            return image;
        }
    }
    
    protected final static class GearEnemy extends Enemy {
        private final static Panmage[] images = new Panmage[4];
        private static Panmage display = null;
        private int timer = 0;
        private int frm = 0;
        private int rot = 0;
        
        protected GearEnemy(final Segment seg) {
            super(PROP_OFF_X, CRAWL_H, seg, 2);
            if (display == null) {
                final Panmage ref = BotsnBoltsGame.fireballEnemy[0];
                display = Pangine.getEngine().createEmptyImage(Pantil.vmid(), ref.getOrigin(), ref.getBoundingMinimum(), ref.getBoundingMaximum());
            }
            setView(display);
            setMirror(true);
            hv = -2;
        }
        
        @Override
        protected boolean onStepCustom() {
            timer++;
            if (timer > 1) {
                timer = 0;
                frm++;
                if (frm > 3) {
                    frm = 0;
                    rot--;
                    if (rot < 0) {
                        rot = 3;
                    }
                }
            }
            return false;
        }
        
        @Override
        protected final void onWall(final byte xResult) {
            setMirror(!isMirror());
            hv *= -1;
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panlayer layer = getLayer();
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY(), z = pos.getZ();
            final Panmage img = getImage(frm);
            final boolean mirror = isMirror();
            renderer.render(layer, img, x - getCurrentDisplay().getOrigin().getX() + (mirror ? 1 : 0), y, z, 0, 0, 16, 16, rot, mirror, false);
        }
        
        private final static Panmage getImage(final int i) {
            Panmage image = images[i];
            if (image != null) {
                return image;
            }
            image = getImage(image, "GearEnemy" + (i + 1), null, null, null);
            images[i] = image;
            return image;
        }
    }
    
    protected final static class FireballEnemy extends JumpEnemy {
        private final static int DURATION_PERIOD = BlockPuzzle.FireTimedBlock.DURATION_PERIOD;
        private int delay = DURATION_PERIOD;
        
        protected FireballEnemy(final Segment seg) {
            super(PROP_OFF_X, PROP_H, seg, 1);
            delay -= (seg.getInt(3, 0) * 16);
            setView(BotsnBoltsGame.fireballEnemy[0]);
            setVisible(false);
        }
        
        @Override
        protected final int getDelay() {
            final int r = delay;
            delay = DURATION_PERIOD;
            return r;
        }
        
        @Override
        protected final boolean isAppointmentAllowed() {
            return true;
        }
        
        @Override
        protected final boolean canJump() {
            return super.canJump() || (getPosition().getY() <= 0);
        }

        @Override
        protected final void onJump() {
            v = 12;
        }
        
        @Override
        protected boolean onFell() {
            schedule();
            return true;
        }
        
        @Override
        protected final void onLanded() {
            super.onLanded();
            setVisible(false);
            schedule();
        }
        
        @Override
        protected final boolean onStepCustom() {
            final int imgIndex;
            if (v > 1) {
                setVisible(true);
                imgIndex = getIndex(0, 1);
            } else if (v < -1) {
                imgIndex = getIndex(3, 4);
            } else {
                imgIndex = 2;
            }
            setView(BotsnBoltsGame.fireballEnemy[imgIndex]);
            return false;
        }
        
        @Override
        protected final boolean isMirrorable() {
            return false;
        }
        
        protected final static int getIndex(final int index1, final int index2) {
            return isFirstImageActive() ? index1 : index2;
        }
        
        protected final static boolean isFirstImageActive() {
            return Pangine.getEngine().isOn(4);
        }
    }
    
    protected static class Icicle extends Enemy {
        private static Panmage img = null;
        
        protected Icicle(final Segment seg) {
            super(PROP_OFF_X, PROP_H, seg, 1);
            getPosition().addY(1);
            setView(getIcicleImage());
        }
        
        @Override
        protected boolean onStepCustom() {
            return true;
        }
        
        @Override
        protected final int getDamage() {
            return BlockPuzzle.DAMAGE_SPIKE;
        }
        
        private final static Panmage getIcicleImage() {
            return getIcicleImage(img, "Icicle");
        }
        
        protected final static Panmage getIcicleImage(final Panmage img, final String name) {
            return getImage(img, name, BotsnBoltsGame.fireballEnemy[0]);
        }
    }
    
    protected final static class IceSpike extends Icicle {
        protected IceSpike(final Segment seg) {
            super(seg);
            setFlip(true);
            getPosition().addY(13);
        }
    }
    
    protected final static class IcicleEnemy extends Icicle {
        private final static byte STATE_WAITING = 0;
        private final static byte STATE_WAKING = 1;
        private final static byte STATE_FALLING = 2;
        private final static Panmage[] imgs = new Panmage[3];
        private byte state = 0;
        private int timer = 10;
        
        protected IcicleEnemy(final Segment seg) {
            super(seg);
            setView(getIcicleImage(0));
        }
        
        @Override
        protected final boolean onStepCustom() {
            if (state == STATE_WAITING) {
                onWaiting();
                return true;
            } else if (state == STATE_WAKING) {
                onWaking();
                return true;
            } else {
                return false;
            }
        }
        
        private final void onWaiting() {
            final Player p = getNearestPlayer();
            if (p == null) {
                return;
            } else if (Math.abs(p.getPosition().getX() - getPosition().getX()) < 5) {
                startWake();
            }
        }
        
        private final void onWaking() {
            timer--;
            if (timer <= 0) {
                startFall();
            } else if (timer <= 5) {
                changeView(2);
            }
        }
        
        private final void startWake() {
            this.state = STATE_WAKING;
            changeView(1);
        }
        
        private final void startFall() {
            this.state = STATE_FALLING;
            timer = 1;
        }
        
        private final void changeView(final int imgIndex) {
            changeView(getIcicleImage(imgIndex));
        }
        
        @Override
        protected final void onGrounded() {
            if (timer <= 0) {
                shatter();
            }
            timer--;
        }
        
        private final void shatter() {
            Player.shatter(this, BotsnBoltsGame.getIceShatter());
            destroy();
        }
        
        private final static Panmage getIcicleImage(final int i) {
            Panmage img = imgs[i];
            if (img != null) {
                return img;
            }
            img = getIcicleImage(null, "IcicleEnemy" + (i + 1));
            imgs[i] = img;
            return img;
        }
    }
    
    protected final static class BoulderEnemy extends Enemy {
        private static Panmage img = null;
        private boolean held = false;
        
        protected BoulderEnemy(final int x, final int y) {
            super(PROP_OFF_X, PROP_H, x, y, 1);
            setView(getImage());
        }
        
        @Override
        protected final boolean onStepCustom() {
            return held;
        }
        
        private final static Panmage getImage() {
            if (img != null) {
                return img;
            }
            return (img = getImage(img, "BoulderEnemy", BotsnBoltsGame.fireballEnemy[0]));
        }
    }
    
    protected final static int HENCHBOT_OFF_X = 6, HENCHBOT_H = 21, HENCHBOT_HEALTH = 5;
    protected final static int HENCHBOT_SHOOT_OFF_X = 14, HENCHBOT_SHOOT_OFF_Y = 11;
    
    protected abstract static class HenchbotEnemy extends JumpEnemy {
        private final Panmage[] imgs;
        protected boolean shooting = false;
        private long lastJump = NULL_CLOCK;
        
        protected HenchbotEnemy(final Panmage[] imgs, final int x, final int y) {
            super(HENCHBOT_OFF_X, HENCHBOT_H, x, y, HENCHBOT_HEALTH);
            this.imgs = imgs;
        }
        
        protected HenchbotEnemy(final Panmage[] imgs, final Segment seg) {
            super(HENCHBOT_OFF_X, HENCHBOT_H, seg, HENCHBOT_HEALTH);
            this.imgs = imgs;
        }
        
        {
            turnTowardPlayer();
        }
        
        @Override
        public boolean onStepCustom() {
            if (shooting || (timer == null) || !isGrounded() || ((Pangine.getEngine().getClock() - lastJump) < (getDelay() / 2))) {
                return false;
            }
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY();
            for (final Projectile prj : Projectile.currentProjectiles) {
                final Panple prjPos = prj.getPosition();
                final float prjX = prjPos.getX(), prjY = prjPos.getY();
                if ((prjY > (y - 6)) && (prjY < (y + 26)) && (prjX > (x - 44)) && (prjX < (x + 44))) {
                    clearSchedule();
                    jump();
                    break;
                }
            }
            return false;
        }
        
        @Override
        protected final void onSchedule() {
            turnTowardPlayer(getNearestPlayer());
            shooting = false;
        }

        @Override
        protected final void onAppointment() {
            final Player player = getNearestPlayer();
            if ((player == null) || (getDistanceX(player) > getMaxDistance())) {
                schedule();
                return;
            }
            turnTowardPlayer(player);
            // Now jumping only happens in onStepCustom when a Projectile is near
            //if (Mathtil.rand(20)) {
            //    jump();
            //} else {
                shoot();
            //}
        }
        
        protected final int getMaxDistance() {
            final BotRoom room = RoomLoader.getCurrentRoom();
            return ((room == null) || room.w > 1) ? getLongRoomMaxDistance() : Pangine.getEngine().getEffectiveWidth();
        }
        
        protected int getLongRoomMaxDistance() {
            return Pangine.getEngine().getEffectiveWidth();
        }
        
        protected final void shoot() {
            shooting = true;
            changeViewGrounded();
            onShoot();
            // Shoot 3 fairly quickly; then schedule random after normal delay
        }

        @Override
        protected void onJump() {
            v = Player.VEL_JUMP;
        }
        
        protected abstract void onShoot();
        
        @Override
        protected final void onLanded() {
            super.onLanded();
            lastJump = Pangine.getEngine().getClock();
            schedule();
        }
        
        @Override
        protected final void onGrounded() {
            changeViewGrounded();
        }
        
        protected final void changeViewGrounded() {
            changeView(shooting ? 1 : 0);
        }
        
        @Override
        protected final boolean onAir() {
            changeView(2);
            return false;
        }
        
        protected final void changeView(final int i) {
            changeView(imgs[i]);
        }
    }
    
    protected final static class CyanEnemy extends HenchbotEnemy {
        private final static Panple scratch = new ImplPanple();
        
        protected CyanEnemy(final int x, final int y) {
            super(BotsnBoltsGame.henchbotEnemy, x, y);
        }
        
        protected CyanEnemy(final Segment seg) {
            super(BotsnBoltsGame.henchbotEnemy, seg);
        }
        
        @Override
        protected final void onShoot() {
            final Player player = getNearestPlayer();
            final float vx, vy;
            if (player == null) {
                vx = getMirrorMultiplier() * VEL_PROJECTILE;
                vy = 0;
            } else {
                scratch.set(getPosition());
                scratch.add(getMirrorMultiplier() * HENCHBOT_SHOOT_OFF_X, HENCHBOT_SHOOT_OFF_Y);
                Panple.subtract(scratch, player.getPosition(), scratch);
                scratch.multiply((float) (VEL_PROJECTILE / scratch.getMagnitude2()));
                vx = scratch.getX();
                vy = scratch.getY();
            }
            newEnemyProjectile(this, HENCHBOT_SHOOT_OFF_X, HENCHBOT_SHOOT_OFF_Y, vx, vy);
            hold(30);
        }
    }
    
    protected final static class FlamethrowerEnemy extends HenchbotEnemy {
        private final static int DURATION_FLAME = 8;
        private final static int LENGTH_STREAM = 8;
        private final static int LENGTH_BURST = 5;
        
        protected FlamethrowerEnemy(final Segment seg) {
            super(BotsnBoltsGame.flamethrowerEnemy, seg);
        }
        
        @Override
        protected final void onShoot() {
            flame(0);
        }
        
        private final void flame(final int i) {
            if (i < LENGTH_STREAM) {
                stream(i);
            } else {
                burst(i - LENGTH_STREAM);
            }
            next(i);
        }
        
        private final void stream(final int i) {
            final Panmage img;
            final int ox, oy;
            if (i < 2) {
                img = BotsnBoltsGame.flame4[0];
                ox = (i == 0) ? 13 : 17;
                oy = 9;
            } else if (i < 4) {
                img = BotsnBoltsGame.flame4[1];
                ox = (i == 2) ? 21 : 25;
                oy = 9;
            } else if (i < 5) {
                img = BotsnBoltsGame.flame4[0];
                ox = 29;
                oy = 9;
            } else if (i < 6) {
                img = BotsnBoltsGame.flame4[1];
                ox = 33;
                oy = 9;
            } else if (i < 7) {
                img = BotsnBoltsGame.flame8[0];
                ox = 36;
                oy = 9;
            } else {
                img = BotsnBoltsGame.flame8[1];
                ox = 42;
                oy = 10;
            }
            new TimedEnemyProjectile(img, this, ox, oy, DURATION_FLAME);
        }
        
        private final void burst(final int i) {
            final int ox, oy;
            switch (i) {
                case 0 :
                    ox = 50;
                    oy = 13;
                    break;
                case 1 :
                    ox = 66;
                    oy = 8;
                    break;
                case 2 :
                    ox = 58;
                    oy = 17;
                    break;
                case 3 :
                    ox = 76;
                    oy = 14;
                    break;
                default :
                    ox = 70;
                    oy = 23;
                    break;
            }
            new AnimationEnemyProjectile(BotsnBoltsGame.flame16, this, ox, oy);
        }
        
        private final void next(final int i) {
            if (i < (LENGTH_STREAM + LENGTH_BURST - 1)) {
                Pangine.getEngine().addTimer(this, 1, new TimerListener() {
                    @Override public final void onTimer(final TimerEvent event) {
                        flame(i + 1);
                    }});
            } else {
                Pangine.getEngine().addTimer(this, DURATION_FLAME, new TimerListener() {
                    @Override public final void onTimer(final TimerEvent event) {
                        schedule();
                    }});
            }
        }
    }
    
    protected final static class FreezeRayEnemy extends HenchbotEnemy {
        protected FreezeRayEnemy(final Segment seg) {
            super(BotsnBoltsGame.freezeRayEnemy, seg);
        }

        @Override
        protected final void onShoot() {
            new FreezeRayProjectile(this, 13, 9);
            hold(30);
        }
        
        @Override
        protected final int getLongRoomMaxDistance() {
            return 336;
        }
    }
    
    protected final static class RockEnemy extends Enemy {
        private static Panmage rockCatch = null;
        private static Panmage rockThrow = null;
        private int x;
        private int boulderTimer = Extra.TIMER_SPAWNER;
        private int holdTimer = 0;
        private int throwTimer = 0;
        private BoulderEnemy boulder = null;
        
        protected RockEnemy(final Segment seg) {
            super(HENCHBOT_OFF_X, HENCHBOT_H, seg, HENCHBOT_HEALTH);
            setStillImage();
            //turnTowardPlayer();
            setMirror(true); // Boulders designed to land in specific place
        }
        
        @Override
        protected final void initTileCoordinates(final int x, final int y) {
            this.x = x;
        }
        
        private final void setStillImage() {
            changeView(BotsnBoltsGame.rockEnemy);
        }
        
        @Override
        protected final boolean onStepCustom() {
            if (boulder != null) {
                if (boulder.isDestroyed()) {
                    boulder = null;
                    setStillImage();
                } else if (boulder.held) {
                    holdTimer--;
                    if (holdTimer < 0) {
                        changeView(getRockThrow());
                        boulder.hv = getMirrorMultiplier() * 5;
                        boulder.v = 5;
                        boulder.held = false;
                        boulder = null;
                        throwTimer = 20;
                    }
                } else {
                    final float y = getPosition().getY();
                    final float boulderY = boulder.getPosition().getY();
                    final int holdThreshold = 19;
                    final float heldY = y + holdThreshold;
                    if (boulderY <= (heldY + 32)) {
                        changeView(getRockCatch());
                    }
                    if (boulderY <= (heldY + 16)) {
                        boulder.held = true;
                        holdTimer = 15;
                        boulder.getPosition().setY(heldY);
                    }
                }
            }
            if (throwTimer > 0) {
                throwTimer--;
                if (throwTimer <= 0) {
                    setStillImage();
                }
            }
            boulderTimer--;
            if (boulderTimer <= 0) {
                //turnTowardPlayer(); // This enemy can't turn around; otherwise boulders won't land where intended
                boulder = new BoulderEnemy(x, BotsnBoltsGame.tm.getHeight());
                boulder.setMirror(isMirror()); // Make sure shading matches
                final Panple boulderPos = boulder.getPosition();
                boulderPos.setZ(BotsnBoltsGame.DEPTH_ENEMY_BACK);
                //getPosition().setZ(BotsnBoltsGame.DEPTH_ENEMY_BACK);
                boulderPos.addX(-1);
                boulderTimer = Extra.TIMER_SPAWNER;
                getLayer().addActor(boulder);
            }
            return false;
        }
        
        @Override
        protected final void onEnemyDestroy() {
            Panctor.destroy(boulder);
        }
        
        private final static Panmage getRockCatch() {
            return (rockCatch = getRockImage(rockCatch, "RockEnemyCatch"));
        }
        
        private final static Panmage getRockThrow() {
            return (rockThrow = getRockImage(rockThrow, "RockEnemyThrow"));
        }
        
        private final static Panmage getRockImage(final Panmage img, final String name) {
            if (img != null) {
                return img;
            }
            final Panmage ref = BotsnBoltsGame.flamethrowerEnemy[0];
            return getImage(img, name, ref.getOrigin(), ref.getBoundingMinimum(), ref.getBoundingMaximum());
        }
    }
    
    protected final static class JackhammerEnemy extends Enemy implements RoomAddListener {
        private final static Panmage[] imgs = new Panmage[2];
        private int x;
        private int yCluster;
        private int timer = 0;
        private boolean active = true;
        
        protected JackhammerEnemy(final Segment seg) {
            super(HENCHBOT_OFF_X, HENCHBOT_H, seg, HENCHBOT_HEALTH);
            setStillImage();
            turnTowardPlayer();
        }
        
        @Override
        protected final void initTileCoordinates(final int x, final int y) {
            this.x = x;
            int j = y - 2;
            while (j > 0) {
                if (!isSolidTile(x, j)) {
                    break;
                }
                j--;
            }
            yCluster = j;
        }
        
        @Override
        public final void onRoomAdd(final RoomAddEvent event) {
            turnTowardPlayer();
        }
        
        @Override
        protected final boolean onStepCustom() {
            if (timer <= 0) {
                timer = 30;
                active = !active;
                if (active) {
                    turnTowardPlayer();
                } else {
                    setStillImage();
                }
            }
            if (active) {
                //changeView(getImage(timer % 2));
                changeView(getImage(((timer + 3) % 4) / 2));
                if ((timer % 6) == 1) {
                    Player.puff(this, Mathtil.randi(-4, 4), Mathtil.randi(-8, 0));
                }
                if (timer == 2) {
                    Player.addActor(this, new DirtCluster(x, yCluster));
                }
            }
            timer--;
            return false;
        }
        
        private final void setStillImage() {
            changeView(getImage(0));
        }
        
        private final static Panmage getImage(final int i) {
            final Panmage img = imgs[i];
            if (img != null) {
                return img;
            }
            final Panmage ref = BotsnBoltsGame.flamethrowerEnemy[0];
            return (imgs[i] = getImage(img, "JackhammerEnemy" + (i + 1), ref.getOrigin(), ref.getBoundingMinimum(), ref.getBoundingMaximum()));
        }
    }
    
    protected final static class DirtCluster extends Enemy {
        private static Panmage img = null;
        private int timer = 1;
        
        protected DirtCluster(final int x, final int y) {
            super(PROP_OFF_X, PROP_H, x, y, 1);
            setView(getImage());
            getPosition().addY(8);
        }
        
        @Override
        protected final void onGrounded() {
            if (timer <= 0) {
                shatter();
            }
            hv = 0;
            timer--;
        }
        
        @Override
        protected final void onLanded() {
            super.onLanded();
            hv = 0;
        }
        
        private final void shatter() {
            Player.shatter(this, DrillEnemy.getDirtShatter());
            destroy();
        }
        
        private final static Panmage getImage() {
            if (img != null) {
                return img;
            }
            return (img = getImage(img, "DirtCluster", BotsnBoltsGame.fireballEnemy[0]));
        }
    }
    
    private final static Panple SHOVEL_O = new FinPanple2(BotsnBoltsGame.flamethrowerEnemy[0].getOrigin().getX() - 1, 1);
    
    protected final static class ShovelEnemy extends Enemy {
        private final static Panmage[] imgs = new Panmage[3];
        private int x;
        private int y;
        private int timer = 0;
        
        protected ShovelEnemy(final Segment seg) {
            super(HENCHBOT_OFF_X, HENCHBOT_H, seg, HENCHBOT_HEALTH);
            setView(0);
        }
        
        @Override
        protected final void initTileCoordinates(final int x, final int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        protected final boolean onStepCustom() {
            final Panlayer layer = getLayer();
            if (layer == null) {
                return false;
            }
            timer++;
            if (timer == 5) {
                setView(1);
            } else if (timer == 6) {
                final Panple pos = getPosition();
                final int m = getMirrorMultiplier();
                Player.newDiver(layer, true, DrillEnemy.getDirtShatter(), pos.getX() + (12 * m), pos.getY(), 2 * m, 2, Mathtil.rand(), Mathtil.rand(), true);
            } else if (timer == 30) {
                setView(2);
            } else if (timer == 31) {
                final DirtCluster dirtCluster = new DirtCluster(x, y);
                dirtCluster.hv = getMirrorMultiplier() * -6;
                dirtCluster.v = 6;
                Player.addActor(this, dirtCluster);
            } else if (timer == 60) {
                setView(0);
                timer = 0;
            }
            return false;
        }
        
        private final void setView(final int i) {
            setView(getImage(i));
        }
        
        private final static Panmage getImage(final int i) {
            final Panmage img = imgs[i];
            if (img != null) {
                return img;
            }
            final Panmage ref = BotsnBoltsGame.flamethrowerEnemy[0];
            return (imgs[i] = getImage(img, "ShovelEnemy" + (i + 1), SHOVEL_O, ref.getBoundingMinimum(), ref.getBoundingMaximum()));
        }
    }
    
    protected final static class ElectricityEnemy extends Enemy {
        private final static int DURATION_WAIT = BlockPuzzle.ElectricityBlock.DURATION_PERIOD - BlockPuzzle.Electricity.DURATION_ELECTRICITY;
        private final static int DURATION_STRIKE = BlockPuzzle.Electricity.DURATION_ELECTRICITY;
        private static Panmage strike = null;
        private int timer = DURATION_WAIT;
        private boolean striking = false;
        private ElectricityEnemy other = null;
        private Electricity electricity = null;
        private boolean flip = false;
        
        protected ElectricityEnemy(final Segment seg) {
            this(getX(seg), getY(seg), seg.getInt(3, 0));
        }
        
        protected ElectricityEnemy(final int x, final int y, final int timerOffset) {
            this(x, y, false, -3);
            other = new ElectricityEnemy(x + 6, y, true, 2);
            other.other = this;
            RoomLoader.addActor(other);
            timer -= (timerOffset * 16);
        }
        
        protected ElectricityEnemy(final int x, final int y, final boolean mirror, final int offX) {
            super(HENCHBOT_OFF_X, HENCHBOT_H, x, y, HENCHBOT_HEALTH);
            setStill();
            setMirror(mirror);
            getPosition().addX(offX);
        }
        
        protected final void setStill() {
            setBoth(BotsnBoltsGame.electricityEnemy);
        }
        
        protected final void setBoth(final Panmage img) {
            changeView(img);
            if (!Panctor.isDestroyed(other)) {
                other.changeView(img);
            }
        }
        
        @Override
        public final boolean onStepCustom() {
            if (isMirror()) {
                return false;
            } if (Panctor.isDestroyed(other)) {
                return false;
            }
            timer--;
            if (timer <= 0) {
                striking = !striking;
                if (striking) {
                    setBoth(getStrike());
                    timer = DURATION_STRIKE;
                } else {
                    setStill();
                    timer = DURATION_WAIT;
                }
            } else if (striking && (timer == (DURATION_STRIKE - 1))) {
                electricity = new Electricity(this, 11, 2, 5, false, flip);
                flip = !flip;
            }
            return false;
        }
        
        @Override
        protected final void onEnemyDestroy() {
            Panctor.destroy(electricity);
            if (!Panctor.isDestroyed(other)) {
                Panctor.destroy(other.electricity);
                other.setStill();
            }
        }
        
        private final static Panmage getStrike() {
            return (strike = getElectricityImage(strike, "ElectricityEnemyStrike"));
        }
        
        private final static Panmage getElectricityImage(final Panmage img, final String name) {
            if (img != null) {
                return img;
            }
            return getImage(img, name, BotsnBoltsGame.flamethrowerEnemy[0]);
        }
    }
    
    protected final static class SwimEnemy extends Enemy {
        private final static Panmage[] imgs = new Panmage[3];
        
        protected SwimEnemy(final Segment seg) {
            super(HENCHBOT_OFF_X, HENCHBOT_H, seg, HENCHBOT_HEALTH);
            setMirror(false);
            hv = -2;
            setView(getCurrentSwim());
        }
        
        @Override
        protected final boolean onStepCustom() {
            changeView(getCurrentSwim());
            if (addX(hv) != X_NORMAL){
                hv *= -1;
                updateMirror();
            }
            bubbleRandom(16, 10);
            return true;
        }
        
        private final static Panmage getCurrentSwim() {
            final int frameDuration = 5;
            final long c = (Pangine.getEngine().getClock() % (4 * frameDuration)) / frameDuration;
            return getSwimImage((c < 3) ? ((int) c) : 1);
        }
        
        private final static Panmage getSwimImage(final int i) {
            Panmage img = imgs[i];
            if (img != null) {
                return img;
            }
            img = getImage(img, "SwimEnemy" + (i + 1), BotsnBoltsGame.flamethrowerEnemy[0]);
            imgs[i] = img;
            return img;
        }
    }
    
    protected final static class JetpackEnemy extends Enemy {
        private final static int DURATION_FRAME = 5;
        private final static int DURATION_ANIM = DURATION_FRAME * 2;
        private final static Panmage[] flyImgs = new Panmage[2];
        private final static Panmage[] attackImgs = new Panmage[2];
        private int vy = 1;
        private int attackTimer = 0;
        
        protected JetpackEnemy(final Segment seg) {
            super(HENCHBOT_OFF_X, HENCHBOT_H, seg, HENCHBOT_HEALTH);
            getPosition().addY(-2);
            vy = seg.getInt(3, vy);
            setMirror(true);
        }
        
        @Override
        protected final boolean onStepCustom() {
            turnTowardPlayer();
            move();
            changeView();
            return true;
        }
        
        private final void changeView() {
            final Panmage[] imgs;
            if (attackTimer <= 0) {
                imgs = flyImgs;
            } else {
                attackTimer--;
                imgs = attackImgs;
            }
            changeView(getJetpackImage(imgs, (int) ((Pangine.getEngine().getClock() % DURATION_ANIM) / DURATION_FRAME)));
        }
        
        private final void move() {
            if ((addY(vy) != Y_NORMAL) || ((vy > 0) && (getPosition().getY() > 202.5f)) || ((vy < 0) && (getPosition().getY() < 1.5f))) {
                vy *= -1;
                shoot();
            }
        }
        
        private final void shoot() {
            newEnemyProjectile(this, HENCHBOT_SHOOT_OFF_X, HENCHBOT_SHOOT_OFF_Y, getMirrorMultiplier() * VEL_PROJECTILE, 0);
            attackTimer = 16;
        }
        
        @Override
        protected final boolean onFell() {
            return false;
        }
        
        private final static Panmage getJetpackImage(final Panmage[] imgs, final int i) {
            Panmage img = imgs[i];
            if (img != null) {
                return img;
            }
            final String mode = (imgs == attackImgs) ? "Attack" : "";
            img = getImage(img, "JetpackEnemy" + mode + (i + 1), BotsnBoltsGame.flamethrowerEnemy[0]);
            imgs[i] = img;
            return img;
        }
    }
    
    protected final static class QuicksandEnemy extends Enemy {
        private static Panmage attack = null;
        private final float maxY;
        private final float minY;
        private byte mode = 0;
        private int timer = 0;
        
        protected QuicksandEnemy(final Segment seg) {
            super(HENCHBOT_OFF_X, HENCHBOT_H, seg, HENCHBOT_HEALTH);
            setMirror(true);
            maxY = getPosition().getY();
            minY = maxY - 28;
            setView(BotsnBoltsGame.quicksandEnemy);
        }
        
        @Override
        protected final boolean onStepCustom() {
            turnTowardPlayer();
            if (mode == 0) {
                onFalling();
            } else if (mode == 1) {
                onWaiting();
            } else if (mode == 2) {
                onRising();
            } else {
                onAttacking();
            }
            return true;
        }
        
        private final void onFalling() {
            final Panple pos = getPosition();
            if (pos.getY() <= minY) {
                incMode();
                return;
            }
            pos.addY(-1);
        }
        
        private final void onWaiting() {
            timer++;
            if (timer >= 30) {
                mode++;
            }
        }
        
        private final void onRising() {
            final Panple pos = getPosition();
            if (pos.getY() >= maxY) {
                incMode();
                return;
            }
            pos.addY(1);
        }
        
        private final void onAttacking() {
            timer++;
            if (timer == 15) {
                newEnemyProjectile(this, HENCHBOT_SHOOT_OFF_X, HENCHBOT_SHOOT_OFF_Y, getMirrorMultiplier() * VEL_PROJECTILE, 0);
                setView(getAttack());
            } else if (timer == 45) {
                setView(BotsnBoltsGame.quicksandEnemy);
            } else if (timer >= 60) {
                mode = 0;
            }
        }
        
        private final void incMode() {
            mode++;
            timer = 0;
        }
        
        private final static Panmage getAttack() {
            return (attack == null) ? (attack = getImage(attack, "QuicksandEnemyAttack", BotsnBoltsGame.flamethrowerEnemy[0])) : attack;
        }
    }
    
    protected final static Panple FORT_CANNON_O = new FinPanple2(4, 0);
    protected final static Panple FORT_CANNON_MIN = new FinPanple2(0, 4);
    protected final static Panple FORT_CANNON_MAX = new FinPanple2(7, 12);
    
    protected final static class FortCannon extends WallCannon {
        private final static Panmage[] images = new Panmage[5];
        
        protected FortCannon(final int x, final int y) {
            super(x, y, 5);
        }
        
        @Override
        protected final boolean isEarly(final int x, final int y) {
            return x == 22;
        }
        
        @Override
        protected final void initCannon(final int x, final int y) {
            setMirror(true);
            getPosition().addX(6);
        }
        
        @Override
        protected final void fire() {
            new EnemyProjectile(BotsnBoltsGame.getEnemyProjectile(), this, 7, 8, getMirrorMultiplier() * VEL_PROJECTILE, 0, gTuple);
        }
        
        @Override
        protected final void onEnemyDestroy() {
            super.onEnemyDestroy();
            final Panctor remains = new Panctor();
            remains.setView(getView(4));
            remains.getPosition().set(getPosition());
            remains.setMirror(isMirror());
            BotsnBoltsGame.addActor(remains);
        }

        @Override
        protected final Panmage getView(final int i) {
            return getFortCannonImage(i);
        }
        
        protected final static Panmage getFortCannonImage(final int i) {
            Panmage image = images[i];
            if (image != null) {
                return image;
            }
            image = Boss.getImage(null, "fort/FortCannon" + (i + 1), FORT_CANNON_O, FORT_CANNON_MIN, FORT_CANNON_MAX);
            images[i] = image;
            return image;
        }
    }
    
    protected final void addHealthMeter() {
        BotsnBoltsGame.initHealthMeter(newHealthMeter(), false);
    }
    
    protected final HudMeter newHealthMeter() {
        return new HudMeter(BotsnBoltsGame.hudMeterBoss) {
            @Override protected final int getValue() {
                return health;
            }};
    }
}
