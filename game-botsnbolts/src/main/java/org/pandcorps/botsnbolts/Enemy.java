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
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.boundary.*;
import org.pandcorps.pandax.*;

public class Enemy extends Panctor implements CollisionListener {
    private int health;
    
    protected Enemy(final int health) {
        this.health = health;
        BotsnBoltsGame.tm.getLayer().addActor(this);
    }
    
    @Override
    public void onCollision(final CollisionEvent event) {
        final Collidable collider = event.getCollider();
        if (collider.getClass() == Projectile.class) {
            onShot((Projectile) collider);
        }
    }
    
    protected void onShot(final Projectile prj) {
        health--;
        if (health <= 0) {
            prj.burst(this);
            destroy();
        }
        prj.burst();
        prj.destroy();
    }
    
    protected final Player getNearestPlayer() {
        return PlayerContext.getPlayer(BotsnBoltsGame.pc);
    }
    
    protected final static class EnemyProjectile extends Pandy implements CollisionListener, AllOobListener {
        protected EnemyProjectile(final Enemy src, final float vx, final float vy) {
            final Panple srcPos = src.getPosition();
            getPosition().set(srcPos.getX(), srcPos.getY(), BotsnBoltsGame.DEPTH_PROJECTILE);
            getVelocity().set(vx, vy);
            final boolean srcMirror = src.isMirror();
            final boolean src180 = src.getRot() == 2;
            setMirror((srcMirror && !src180) || (!srcMirror && src180));
        }
        
        @Override
        public void onCollision(final CollisionEvent event) {
            final Collidable collider = event.getCollider();
            if (collider.getClass() == Player.class) {
                //TODO hurt Player
                //TODO burst
                destroy();
            }
        }
        
        @Override
        public final void onAllOob(final AllOobEvent event) {
            destroy();
        }
    }
    
    protected final static class SentryGun extends Enemy implements StepListener {
        private final float baseX;
        private final float baseY;
        
        protected SentryGun(final int x, final int y) {
            super(5);
            Cube.newCube(x, y);
            final Panple pos = getPosition();
            BotsnBoltsGame.tm.savePosition(pos, x, y);
            pos.add(16, 16);
            baseX = pos.getX();
            baseY = pos.getY();
            pos.setZ(BotsnBoltsGame.DEPTH_ENEMY);
            setView(BotsnBoltsGame.sentryGun);
        }

        @Override
        public final void onStep(final StepEvent event) {
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
        }
        
        private final void setRight() {
            setDirection(false, 0, 2, 0);
        }
        
        private final void setLeft() {
            setDirection(true, 0, -3, 0);
        }
        
        private final void setUp() {
            setDirection(false, 1, 0, 1);
        }
        
        private final void setDown() {
            setDirection(false, 3, -1, -2);
        }
        
        private final void setDirection(final boolean mirror, final int rot, final int offX, final int offY) {
            setMirror(mirror);
            setRot(rot);
            getPosition().set(baseX + offX, baseY + offY);
        }
    }
}
