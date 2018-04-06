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

import org.pandcorps.botsnbolts.BlockPuzzle.*;
import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.botsnbolts.PowerUp.*;
import org.pandcorps.botsnbolts.Profile.*;
import org.pandcorps.botsnbolts.RoomLoader.*;
import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.boundary.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;

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
    
    protected Enemy(final int offX, final int h, final int x, final int y, final int health) {
        super(offX, h);
        this.health = health;
        final Panple pos = getPosition();
        BotsnBoltsGame.tm.savePosition(pos, x, y);
        pos.addX(getInitialOffsetX());
        pos.setZ(BotsnBoltsGame.DEPTH_ENEMY);
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
            destroy();
        }
        prj.setPower(oldPower - oldHealth);
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
        final Panple pos = getPosition();
        PowerUp.addPowerUp(powerUp, pos.getX(), pos.getY(), 6);
    }
    
    protected final void updateMirror() {
        if (hv < 0) {
            setMirror(true);
        } else if (hv > 0) {
            setMirror(false);
        }
    }
    
    protected void turnTowardPlayer() {
        turnTowardPlayer(getNearestPlayer());
    }
    
    protected final void turnTowardPlayer(final Player player) {
        if (player == null) {
            return;
        }
        setMirror(getPosition().getX() > player.getPosition().getX());
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
    
    protected static class EnemyProjectile extends Pandy implements CollisionListener, AllOobListener {
        protected EnemyProjectile(final Panctor src, final int ox, final int oy, final float vx, final float vy) {
            this(BotsnBoltsGame.getEnemyProjectile(), src, ox, oy, vx, vy);
        }
        
        protected EnemyProjectile(final Panmage img, final Panctor src, final int ox, final int oy, final float vx, final float vy) {
            this(img, src, ox, oy, vx, vy, new ImplPanple());
        }
        
        protected EnemyProjectile(final Panmage img, final Panctor src, final int ox, final int oy, final float vx, final float vy, final Panple g) {
            super(g);
            addBySource(this, img, src, ox, oy);
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
            Projectile.burst(this, BotsnBoltsGame.enemyBurst, getPosition());
        }
        
        protected boolean isDestroyedOnImpact() {
            return true;
        }
        
        @Override
        public void onAllOob(final AllOobEvent event) {
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
        
        protected CubeEnemy(final int x, final int y, final int health) {
            super(x, y, health);
            newCube(x, y);
            final Panple pos = getPosition();
            pos.add(OFF_CUBE, OFF_CUBE);
            baseX = pos.getX();
            baseY = pos.getY();
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
        protected PowerBox(final int x, final int y) {
            super(x, y, 1);
            setView(getPlayerContext().pi.powerBox);
        }
        
        @Override
        protected final PowerUp pickAward(final Player player) {
            return new BigBattery(player);
        }
    }
    
    protected final static class DiskBox extends CubeEnemy {
        protected DiskBox(final int x, final int y) {
            super(x, y, 1);
            setView(getPlayerContext().pi.diskBox);
        }
        
        @Override
        protected final PowerUp pickAward(final Player player) {
            return new Disk(player, RoomLoader.getCurrentRoom().roomId);
        }
    }
    
    protected final static class BoltBox extends CubeEnemy {
        private final Upgrade upgrade;
        
        protected BoltBox(final int x, final int y, final Upgrade upgrade) {
            super(x, y, 1);
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
        
        protected SentryGun(final int x, final int y) {
            super(x, y, 5);
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
                if (this.isInView()) {
                    fire();
                }
                timer = 0;
            }
        }
        
        private final void fire() {
            switch (dir) {
                case 0 :
                    new EnemyProjectile(this, 8, 0, VEL_PROJECTILE, 0);
                    break;
                case 1 :
                    new EnemyProjectile(this, 0, 9, 0, VEL_PROJECTILE);
                    break;
                case 3 :
                    new EnemyProjectile(this, 1, -8, 0, -VEL_PROJECTILE);
                    break;
                default :
                    new EnemyProjectile(this, 8, 0, -VEL_PROJECTILE, 0);
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
        
        protected WallCannon(final int x, final int y) {
            super(x, y, 3);
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
            new EnemyProjectile(this, 12, 8, getMirrorMultiplier() * VEL_PROJECTILE, 0);
        }
        
        private final void setView(final int i) {
            setView(getView(i));
        }
        
        protected Panmage getView(final int i) {
            return BotsnBoltsGame.wallCannon[i];
        }
    }
    
    private final static Panple CEILING_CANNON_MIN = new FinPanple2(2, 3);
    private final static Panple CEILING_CANNON_MAX = new FinPanple2(14, 15);
    
    protected final static class CeilingCannon extends TileUnawareEnemy {
        private final static int DURATION = 120;
        private final static Panmage[] images = new Panmage[3];
        private int timer;
        
        protected CeilingCannon(final int x, final int y) {
            super(x, y, 3);
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
            new EnemyProjectile(this, ox, oy, vx, vy);
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
    
    protected final static int PROP_HEALTH = 2, PROP_OFF_X = 4, PROP_H = 12, CRAWL_H = 14;
    
    protected final static class PropEnemy extends Enemy {
        protected PropEnemy(final int x, final int y) {
            super(PROP_OFF_X, PROP_H, x, y, PROP_HEALTH);
            setView(BotsnBoltsGame.propEnemy);
        }
        
        @Override
        protected final boolean onStepCustom() {
            final Player target = getNearestPlayer();
            if (target == null) {
                return true;
            }
            final Panple pos = getPosition(), targetPos = target.getPosition();
            hv = getDirection(pos.getX(), targetPos.getX(), -2, 2);
            updateMirror();
            v = getDirection(pos.getY(), targetPos.getY(), -7, -3);
            addX(hv);
            addY();
            return true;
        }
    }
    
    private abstract static class JumpEnemy extends Enemy implements RoomAddListener {
        private boolean scheduled = false;
        
        protected JumpEnemy(final int offX, final int h, final int x, final int y, final int health) {
            super(offX, h, x, y, health);
        }
        
        @Override
        public final void onRoomAdd(final RoomAddEvent event) {
            schedule();
        }
        
        protected final void schedule() {
            if (scheduled) {
                return;
            }
            onSchedule();
            scheduled = true;
            Pangine.getEngine().addTimer(this, getDelay(), new TimerListener() {
                @Override
                public final void onTimer(final TimerEvent event) {
                    appointment();
                }});
        }
        
        protected void onSchedule() {
        }
        
        protected int getDelay() {
            return 30;
        }
        
        protected final void appointment() {
            scheduled = false;
            onAppointment();
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
        protected SpringEnemy(final int x, final int y) {
            super(PROP_OFF_X, PROP_H, x, y, PROP_HEALTH);
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
        private Direction surfaceDirection = null;
        private int velX;
        private int velY;
        private int timer = 8;
        
        protected CrawlEnemy(final int x, final int y) {
            super(x, y, PROP_HEALTH);
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
        
        protected ShieldedEnemy(final int x, final int y) {
            super(PROP_OFF_X, PROP_H, x, y, PROP_HEALTH);
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
    
    protected final static class DrillEnemy extends Enemy {
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
        
        protected DrillEnemy(final int x, final int y) {
            super(PROP_OFF_X, CRAWL_H, x, y, PROP_HEALTH);
            hv = 0;
            setCurrentView();
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
                getPosition().setZ(BotsnBoltsGame.DEPTH_BEHIND);
            }
            if (digTimer > 0) {
                digTimer--;
                if (digTimer == 10) {
                    final TileMap tm = BotsnBoltsGame.tm;
                    final int index = tm.getContainer(this);
                    tm.setTile(index, null);
                    if (edgeLeft == null) {
                        edgeLeft = BotsnBoltsGame.imgMap[1][3];
                        edgeRight = new AdjustedTileMapImage(edgeLeft, 0, true, false);
                        edgeBottom = new AdjustedTileMapImage(edgeLeft, 1, false, false);
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
            tm.setOverlay(edgeIndex, edgeImg, Tile.BEHAVIOR_SOLID);
        }
        
        private final void destroyPartialTiles() {
            if (partialTileLeft != null) {
                shatter(-2);
                partialTileLeft.destroy();
                partialTileRight.destroy();
                partialTileLeft = null;
                partialTileRight = null;
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
    }
    
    protected final static class SaucerEnemy extends Enemy {
        private final static Panmage[] images = new Panmage[3];
        private final static int speedMultiplier = 3;
        private int speedMultiplied = speedMultiplier;
        
        protected SaucerEnemy(final int x, final int y) {
            super(PROP_OFF_X, CRAWL_H, x, y, PROP_HEALTH);
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
    
    // Guards itself for a while; then lowers guard to attack
    protected final static class GuardedEnemy extends Enemy {
        protected GuardedEnemy(final int x, final int y) {
            super(-1, -1, x, y, -1); //TODO
        }
        
        @Override
        protected final boolean isVulnerableToProjectile(final Projectile prj) {
            return true; //TODO
        }
    }
    
    protected final static int SLIDE_H = 12;
    protected final static Panple SLIDE_MAX = getMax(PROP_OFF_X, SLIDE_H);
    protected final static Panple SLIDE_O = new FinPanple2(8, 0);
    protected final static Panple SLIDE_O_MIRROR = new FinPanple2(7, 0);
    
    // Slides back and forth along the ground
    protected final static class SlideEnemy extends Enemy {
        private final static int SLIDE_VELOCITY = 1;
        private final static int SLIDE_VELOCITY_NEAR = 4;
        private final static Panframe[] frames = new Panframe[5];
        private final static Panmage[] images = new Panmage[3];
        
        protected SlideEnemy(final int x, final int y) {
            super(PROP_OFF_X, SLIDE_H, x, y, PROP_HEALTH);
            hv = -SLIDE_VELOCITY;
        }
        
        @Override
        protected final boolean onStepCustom() {
            final int frameDuration = 3;
            setView(getFrame((int) (Pangine.getEngine().getClock() % (5 * frameDuration)) / frameDuration));
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
            return onHorizontalEdgeTurn(off);
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
    
    protected final static class FireballEnemy extends JumpEnemy {
        protected FireballEnemy(final int x, final int y) {
            super(PROP_OFF_X, PROP_H, x, y, 1);
            setView(BotsnBoltsGame.fireballEnemy[0]);
        }
        
        @Override
        protected final int getDelay() {
            return 60;
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
        protected final boolean onStepCustom() {
            final int imgIndex;
            if (v > 1) {
                imgIndex = getIndex(0, 1);
            } else if (v < -1) {
                imgIndex = getIndex(3, 4);
            } else {
                imgIndex = 2;
            }
            setView(BotsnBoltsGame.fireballEnemy[imgIndex]);
            return false;
        }
        
        protected final static int getIndex(final int index1, final int index2) {
            return isFirstImageActive() ? index1 : index2;
        }
        
        protected final static boolean isFirstImageActive() {
            return Pangine.getEngine().isOn(4);
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
        
        protected HenchbotEnemy(final Panmage[] imgs, final int x, final int y) {
            super(HENCHBOT_OFF_X, HENCHBOT_H, x, y, HENCHBOT_HEALTH);
            turnTowardPlayer();
            this.imgs = imgs;
        }
        
        @Override
        protected final void onSchedule() {
            shooting = false;
        }

        @Override
        protected final void onAppointment() {
            final Player player = getNearestPlayer();
            if (player == null) {
                return;
            } else if (getDistanceX(player) > getMaxDistance()) {
                schedule();
                return;
            }
            turnTowardPlayer(player);
            if (Mathtil.rand()) {
                jump();
            } else {
                shoot();
            }
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
            new EnemyProjectile(this, HENCHBOT_SHOOT_OFF_X, HENCHBOT_SHOOT_OFF_Y, vx, vy);
            hold(30);
        }
    }
    
    protected final static class FlamethrowerEnemy extends HenchbotEnemy {
        private final static int DURATION_FLAME = 8;
        private final static int LENGTH_STREAM = 8;
        private final static int LENGTH_BURST = 5;
        
        protected FlamethrowerEnemy(final int x, final int y) {
            super(BotsnBoltsGame.flamethrowerEnemy, x, y);
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
        protected FreezeRayEnemy(final int x, final int y) {
            super(BotsnBoltsGame.freezeRayEnemy, x, y);
        }

        @Override
        protected final void onShoot() {
            new FreezeRayProjectile(this, 13, 9);
            hold(30);
        }
        
        @Override
        protected final int getLongRoomMaxDistance() {
            return 192;
        }
    }
    
    protected final static class RockEnemy extends Enemy {
        private static Panmage rockCatch = null;
        private static Panmage rockThrow = null;
        private final int x;
        private int boulderTimer = Extra.TIMER_SPAWNER;
        private int holdTimer = 0;
        private int throwTimer = 0;
        private BoulderEnemy boulder = null;
        
        protected RockEnemy(final int x, final int y) {
            super(HENCHBOT_OFF_X, HENCHBOT_H, x, y, HENCHBOT_HEALTH);
            setStillImage();
            //turnTowardPlayer();
            setMirror(true); // Boulders designed to land in specific place
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
    
    protected final static class JackhammerEnemy extends Enemy {
        private final static Panmage[] imgs = new Panmage[2];
        private final int x;
        private final int yCluster;
        private int timer = 0;
        private boolean active = true;
        
        protected JackhammerEnemy(final int x, final int y) {
            super(HENCHBOT_OFF_X, HENCHBOT_H, x, y, HENCHBOT_HEALTH);
            setStillImage();
            turnTowardPlayer();
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
            timer--;
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
    
    protected final static class ElectricityEnemy extends Enemy {
        private final static int DURATION_WAIT = 45;
        private final static int DURATION_STRIKE = 15;
        private static Panmage still = null;
        private static Panmage strike = null;
        private int timer = DURATION_WAIT;
        private boolean striking = false;
        private ElectricityEnemy other = null;
        private Electricity electricity = null;
        private boolean flip = false;
        
        protected ElectricityEnemy(final int x, final int y) {
            this(x, y, false, -3);
            other = new ElectricityEnemy(x + 6, y, true, 2);
            other.other = this;
            RoomLoader.addActor(other);
        }
        
        protected ElectricityEnemy(final int x, final int y, final boolean mirror, final int offX) {
            super(HENCHBOT_OFF_X, HENCHBOT_H, x, y, HENCHBOT_HEALTH);
            setStill();
            setMirror(mirror);
            getPosition().addX(offX);
        }
        
        protected final void setStill() {
            setBoth(getStill());
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
        
        private final static Panmage getStill() {
            return (still = getElectricityImage(still, "ElectricityEnemy"));
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
        
        protected SwimEnemy(final int x, final int y) {
            super(HENCHBOT_OFF_X, HENCHBOT_H, x, y, HENCHBOT_HEALTH);
            setMirror(false);
            hv = -2;
        }
        
        @Override
        protected final boolean onStepCustom() {
            changeView(getCurrentSwim());
            if (addX(hv) != X_NORMAL){
                hv *= -1;
                updateMirror();
            }
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
        
        protected JetpackEnemy(final int x, final int y) {
            super(HENCHBOT_OFF_X, HENCHBOT_H, x, y, HENCHBOT_HEALTH);
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
            if (addY(vy) != Y_NORMAL) {
                vy *= -1;
                shoot();
            }
        }
        
        private final void shoot() {
            new EnemyProjectile(this, HENCHBOT_SHOOT_OFF_X, HENCHBOT_SHOOT_OFF_Y, getMirrorMultiplier() * VEL_PROJECTILE, 0);
            attackTimer = 30;
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
        protected QuicksandEnemy(final int x, final int y) {
            super(HENCHBOT_OFF_X, HENCHBOT_H, x, y, HENCHBOT_HEALTH);
            setMirror(true);
        }
    }
    
    protected final static Panple FORT_CANNON_O = new FinPanple2(4, 0);
    protected final static Panple FORT_CANNON_MIN = new FinPanple2(0, 4);
    protected final static Panple FORT_CANNON_MAX = new FinPanple2(7, 12);
    
    protected final static class FortCannon extends WallCannon {
        private final static Panmage[] images = new Panmage[5];
        
        protected FortCannon(final int x, final int y) {
            super(x, y);
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
            BotsnBoltsGame.tm.getLayer().addActor(remains);
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
