/*
Copyright (c) 2009-2017, Andrew M. Martin
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
    
    private int health;
    
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
            award();
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
    
    protected final void award() {
        final PowerUp powerUp = pickAward();
        if (powerUp != null) {
            award(powerUp);
        }
    }
    
    protected PowerUp pickAward() {
        /*
        TODO
        Non-100 probability
        Make probability higher as health decreases
        Pick stronger power-ups as health decreases
        */
        if (Mathtil.rand(100)) {
            return new BigBattery();
        }
        return null;
    }
    
    protected abstract void award(final PowerUp powerUp);
    
    protected final void updateMirror() {
        if (hv < 0) {
            setMirror(true);
        } else if (hv > 0) {
            setMirror(false);
        }
    }
    
    protected final void turnTowardPlayer() {
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
    
    protected final PlayerContext getPlayerContext() {
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
    
    protected final boolean isSolidIndex(final int index) {
        return isSolidTile(BotsnBoltsGame.tm.getTile(index));
    }
    
    protected final boolean isSolidTile(final int i, final int j) {
        return isSolidTile(BotsnBoltsGame.tm.getTile(i, j));
    }
    
    protected final boolean isSolidTile(final Tile tile) {
        final byte b = Tile.getBehavior(tile);
        return (b == Tile.BEHAVIOR_SOLID) || isSolidBehavior(b);
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
        tm.setForeground(x, y, BotsnBoltsGame.cube[2], Tile.BEHAVIOR_SOLID);
        tm.setForeground(x1, y, BotsnBoltsGame.cube[3], Tile.BEHAVIOR_SOLID);
        tm.setForeground(x, y1, BotsnBoltsGame.cube[0], Tile.BEHAVIOR_SOLID);
        tm.setForeground(x1, y1, BotsnBoltsGame.cube[1], Tile.BEHAVIOR_SOLID);
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
    
    protected abstract static class CubeEnemy extends TileUnawareEnemy {
        protected final float baseX;
        protected final float baseY;
        
        protected CubeEnemy(final int x, final int y, final int health) {
            super(x, y, health);
            newCube(x, y);
            final Panple pos = getPosition();
            pos.add(16, 16);
            baseX = pos.getX();
            baseY = pos.getY();
        }
        
        @Override
        protected final int getInitialOffsetX() {
            return 0;
        }
        
        @Override
        protected final void award(final PowerUp powerUp) {
            PowerUp.addPowerUp(powerUp, baseX, baseY, 6);
        }
    }
    
    protected final static class PowerBox extends CubeEnemy {
        protected PowerBox(final int x, final int y) {
            super(x, y, 1);
            setView(getPlayerContext().pi.powerBox);
        }
        
        @Override
        protected final PowerUp pickAward() {
            return new BigBattery();
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
    
    protected final static class WallCannon {
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

        @Override
        protected final void award(final PowerUp powerUp) {
            
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
        
        @Override
        protected final void award(final PowerUp powerUp) {
            
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

        @Override
        protected final void award(final PowerUp powerUp) {
            
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

        @Override
        protected final void award(final PowerUp powerUp) {
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
                final Panmage ref = BotsnBoltsGame.crawlEnemy.getFrames()[0].getImage();
                final Panple o = ref.getOrigin();
                final Panple min = ref.getBoundingMinimum();
                final Panple max = ref.getBoundingMaximum();
                img = getImage(null, "DrillEnemy" + (i + 1), o, min, max);
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
        
        @Override
        protected final void award(final PowerUp powerUp) {
        }
        
        protected final static Panmage getDirtShatter() {
            if (dirtShatter == null) {
                dirtShatter = Pangine.getEngine().createImage("dirt.shatter", BotsnBoltsGame.CENTER_8, null, null, BotsnBoltsGame.RES + "misc/DirtShatter.png");
            }
            return dirtShatter;
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
        
        @Override
        protected final void award(final PowerUp powerUp) {
        }
    }
    
    protected final static int SLIDE_H = 14;
    
    // Slides back and forth along the ground
    protected final static class SlideEnemy extends Enemy {
        private final static int SLIDE_VELOCITY = 1;
        
        protected SlideEnemy(final int x, final int y) {
            super(PROP_OFF_X, SLIDE_H, x, y, PROP_HEALTH);
            hv = -SLIDE_VELOCITY;
        }
        
        @Override
        protected final void onWall(final byte xResult) {
            hv *= -1;
        }

        @Override
        protected final void award(final PowerUp powerUp) {
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

        @Override
        protected final void award(final PowerUp powerUp) {
        }
        
        private final static Panmage getImage() {
            if (img != null) {
                return img;
            }
            return (img = getImage(img, "BoulderEnemy", BotsnBoltsGame.fireballEnemy[0]));
        }
    }
    
    protected final static int HENCHBOT_OFF_X = 6, HENCHBOT_H = 21, HENCHBOT_HEALTH = 5;
    
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
            Pangine.getEngine().addTimer(this, 30, new TimerListener() {
                @Override public final void onTimer(final TimerEvent event) {
                    schedule();
                }});
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
        
        @Override
        protected final void award(final PowerUp powerUp) {
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

        @Override
        protected final void award(final PowerUp powerUp) {
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
        
        @Override
        protected final void award(final PowerUp powerUp) {
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
        
        @Override
        protected final void award(final PowerUp powerUp) {
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
