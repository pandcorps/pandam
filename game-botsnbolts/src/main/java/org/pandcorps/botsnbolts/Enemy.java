/*
Copyright (c) 2009-2016, Andrew M. Martin
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
        BotsnBoltsGame.tm.getLayer().addActor(this);
        final Panple pos = getPosition();
        BotsnBoltsGame.tm.savePosition(pos, x, y);
        pos.setZ(BotsnBoltsGame.DEPTH_ENEMY);
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
        final int oldHealth = health, oldPower = prj.power;
        if (oldPower <= 0) {
            return;
        }
        health -= oldPower;
        if (health <= 0) {
            prj.burst(this);
            award();
            destroy();
        }
        prj.setPower(oldPower - oldHealth);
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
    
    protected final static class EnemyProjectile extends Pandy implements CollisionListener, AllOobListener {
        protected EnemyProjectile(final Enemy src, final int ox, final int oy, final float vx, final float vy) {
            final Panple srcPos = src.getPosition();
            getPosition().set(srcPos.getX() + ox, srcPos.getY() + oy, BotsnBoltsGame.DEPTH_PROJECTILE);
            getVelocity().set(vx, vy);
            final boolean srcMirror = src.isMirror();
            final boolean src180 = src.getRot() == 2;
            setMirror((srcMirror && !src180) || (!srcMirror && src180));
            setView(BotsnBoltsGame.enemyProjectile);
            BotsnBoltsGame.tm.getLayer().addActor(this);
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
                fire();
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
                    new EnemyProjectile(this, -8, 0, -VEL_PROJECTILE, 0);
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
    
    protected final static int PROP_OFF_X = 4, PROP_H = 10;
    
    protected final static class PropEnemy extends Enemy {
        protected PropEnemy(final int x, final int y) {
            super(PROP_OFF_X, PROP_H, x, y, 2);
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
    
    protected final static class SpringEnemy extends Enemy {
        protected SpringEnemy(final int x, final int y) {
            super(PROP_OFF_X, PROP_H, x, y, 2);
            setView(BotsnBoltsGame.springEnemy[0]);
        }
        
        @Override
        protected final void award(final PowerUp powerUp) {
            
        }
    }
}
