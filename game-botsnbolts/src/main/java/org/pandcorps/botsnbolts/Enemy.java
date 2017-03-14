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

import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.botsnbolts.PowerUp.*;
import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.boundary.*;
import org.pandcorps.pandax.*;
import org.pandcorps.pandax.tile.*;

public abstract class Enemy extends Chr implements CollisionListener {
    private final static int VEL_PROJECTILE = 6;
    
    private int health;
    
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
    }
    
    protected void onAttack(final Player player) {
        player.hurt(1);
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
        final Player player = getNearestPlayer();
        if (player == null) {
            return;
        }
        setMirror(getPosition().getX() > player.getPosition().getX());
    }
    
    protected final Player getNearestPlayer() {
        return PlayerContext.getPlayer(BotsnBoltsGame.pc);
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
        final byte b = Tile.getBehavior(BotsnBoltsGame.tm.getTile(index));
        return (b == Tile.BEHAVIOR_SOLID) || isSolidBehavior(b);
    }
    
    protected static class EnemyProjectile extends Pandy implements CollisionListener, AllOobListener {
        protected EnemyProjectile(final Enemy src, final int ox, final int oy, final float vx, final float vy) {
            this(BotsnBoltsGame.enemyProjectile, src, ox, oy, vx, vy);
        }
        
        protected EnemyProjectile(final Panmage img, final Enemy src, final int ox, final int oy, final float vx, final float vy) {
            final Panple srcPos = src.getPosition();
            final boolean srcMirror = src.isMirror();
            final boolean src180 = src.getRot() == 2;
            final boolean mirror = (srcMirror && !src180) || (!srcMirror && src180);
            final int mx = mirror ? -1 : 1;
            getPosition().set(srcPos.getX() + (mx * ox), srcPos.getY() + oy, BotsnBoltsGame.DEPTH_PROJECTILE);
            getVelocity().set(vx, vy);
            setMirror(mirror);
            setView(img);
            BotsnBoltsGame.addActor(this);
        }
        
        @Override
        public void onCollision(final CollisionEvent event) {
            final Collidable collider = event.getCollider();
            if (collider.getClass() == Player.class) {
                final Player player = (Player) collider;
                player.hurt(1);
                Projectile.burst(this, BotsnBoltsGame.enemyBurst, getPosition());
                destroy();
            }
        }
        
        @Override
        public final void onAllOob(final AllOobEvent event) {
            destroy();
        }
    }
    
    protected final static class TimedEnemyProjectile extends EnemyProjectile {
        int timer;
        
        protected TimedEnemyProjectile(final Panmage img, final Enemy src, final int ox, final int oy, final int timer) {
            this(img, src, ox, oy, 0, 0, timer);
        }
        
        protected TimedEnemyProjectile(final Panmage img, final Enemy src, final int ox, final int oy, final float vx, final float vy, final int timer) {
            super(img, src, ox, oy, vx, vy);
            this.timer = timer;
        }
        
        @Override
        public void onStep(final StepEvent event) {
            super.onStep(event);
            if (timer <= 0) {
                destroy();
            }
            timer--;
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
            hv = isMirror() ? -1 : 1;
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
        private boolean shielded = true;
        
        protected ShieldedEnemy(int x, int y) {
            super(-1, -1, x, y, -1); //TODO
            //setView(); //TODO
        }

        @Override
        protected final boolean isVulnerableToProjectile(final Projectile prj) {
            return (prj.power >= Projectile.POWER_MAXIMUM) && isExposedToProjectile(prj);
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
            } else {
                super.onHurt(prj);
            }
        }
        
        @Override
        protected final boolean onStepCustom() {
            //hv = ; //TODO
            updateMirror();
            addX(hv);
            return true;
        }

        @Override
        protected final void award(final PowerUp powerUp) {
        }
    }
    
    // Guards itself for a while; then lowers guard to attack
    protected final static class GuardedEnemy extends Enemy {
        protected GuardedEnemy(int x, int y) {
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
        
        protected SlideEnemy(int x, int y) {
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
        protected FireballEnemy(int x, int y) {
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
                imgIndex = Pangine.getEngine().isOn(4) ? 0 : 1;
            } else if (v < -1) {
                imgIndex = Pangine.getEngine().isOn(4) ? 3 : 4;
            } else {
                imgIndex = 2;
            }
            setView(BotsnBoltsGame.fireballEnemy[imgIndex]);
            return false;
        }
    }
    
    protected final static int HENCHBOT_OFF_X = 6, HENCHBOT_H = 21, HENCHBOT_HEALTH = 5;
    
    protected abstract static class HenchbotEnemy extends JumpEnemy {
        private final Panmage[] imgs;
        protected boolean shooting = false;
        
        protected HenchbotEnemy(final Panmage[] imgs, int x, int y) {
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
            turnTowardPlayer();
            if (Mathtil.rand()) {
                jump();
            } else {
                shoot();
            }
        }
        
        protected final void shoot() {
            shooting = true;
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
        
        protected FlamethrowerEnemy(int x, int y) {
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
}
