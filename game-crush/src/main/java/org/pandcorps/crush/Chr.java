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
package org.pandcorps.crush;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandax.in.*;

public final class Chr extends Panctor implements StepListener, Collidable {
    private final static int VEL = 3;
    private final static int NULL_INDEX = -1;
    private final static int WALK_TIME = 4;
    private final static int ATTACK_TIME = 6;
    private final static int ATTACK_DELAY_TIME = 1;
    private final static int COMBO_TIME = 3;
    private final static int AFTER_ATTACK_TIME = ATTACK_DELAY_TIME + COMBO_TIME;
    private final static int FULL_ATTACK_TIME = ATTACK_TIME + AFTER_ATTACK_TIME;
    private final static int INDEFINITE_TIME = Integer.MAX_VALUE;
    private ChrImages images = null;
    private int teamIndex = NULL_INDEX;
    private int hv = 0;
    private int v = 0;
    private int comboIndex = NULL_INDEX;
    private ChrMode mode = MODE_NONE;
    private int timer = 0;
    private int walkTimer = 0;
    private int walkIndex = 0;
    private Chr carried = null;
    
    protected Chr() {
        images = CrushGame.chrImages;
    }
    
    protected final void onLeft() {
        hv = -VEL;
    }
    
    protected final void onRight() {
        hv = VEL;
    }
    
    protected final void onUp() {
        v = VEL;
    }
    
    protected final void onDown() {
        v = -VEL;
    }
    
    protected final void onAttack() {
        if (!isFree()) {
            return;
        } else if (handleSlam()) {
            return;
        } else if (mode != MODE_ATTACK) {
            comboIndex = 0;
        } else {
            comboIndex++;
            if (comboIndex >= images.attack.length) {
                comboIndex = 0;
            }
        }
        startMode(MODE_ATTACK);
        new AttackBox(this);
    }
    
    protected final void onHit(final Chr enemy) {
        //comboIndex++;
    }
    
    protected final void onGrab() {
        if (handleSlam()) { // Could maybe slam and lift again if grab pressed instead of attack while carrying 
            return;
        }
        startMode(MODE_GRAB);
        new GrabBox(this);
    }
    
    protected final void onGrab(final Chr enemy) {
        if (enemy.carried != null) {
            if (enemy.carried == this) {
                // Two characters must have grabbed each other at same time
                onHurt();
                enemy.onHurt();
            }
            return; // Can't grab a character already carrying someone
        }
        carried = enemy;
        enemy.startMode(MODE_CARRIED);
    }
    
    protected final void onHurt() {
        startMode(MODE_HURT);
        clearAttack();
    }
    
    protected final boolean handleSlam() {
        if (carried != null) {
            onSlam();
            return true;
        }
        return false;
    }
    
    protected final void onSlam() {
        startMode(MODE_SLAM);
    }
    
    private final void startMode(final ChrMode mode) {
        this.mode = mode;
        timer = mode.time;
    }
    
    protected void registerPlayer(final ControlScheme cs) {
        register(cs.getLeft(), new ActionListener() {
            @Override public final void onAction(final ActionEvent event) { onLeft(); }});
        register(cs.getRight(), new ActionListener() {
            @Override public final void onAction(final ActionEvent event) { onRight(); }});
        register(cs.getUp(), new ActionListener() {
            @Override public final void onAction(final ActionEvent event) { onUp(); }});
        register(cs.getDown(), new ActionListener() {
            @Override public final void onAction(final ActionEvent event) { onDown(); }});
        register(cs.get1(), new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { onAttack(); }});
        register(cs.get2(), new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { onGrab(); }});
    }
    
    protected final void clearWalk() {
        walkIndex = 0;
        walkTimer = 0;
    }
    
    protected final void clearAttack() {
        comboIndex = NULL_INDEX;
        startMode(MODE_NONE);
    }
    
    protected final boolean isFree() {
        return mode.isFree(this);
    }

    @Override
    public final void onStep(final StepEvent event) {
        if (timer == 0) {
            startMode(MODE_NONE);
        }
        mode.step(this);
    }
    
    protected final void stepNone() {
        final Panple pos = getPosition();
        pos.add(hv, v); //TODO bounds
        if ((hv == 0) && (v == 0)) {
            clearWalk();
            changeView((carried == null) ? images.still : images.walkCarrying[1]);
        } else {
            if (hv != 0) {
                setMirror(hv < 0);
            }
            final Panmage[] walk = (carried == null) ? images.walk : images.walkCarrying;
            changeView(walk[walkIndex]);
            walkTimer++;
            if (walkTimer >= WALK_TIME) {
                walkIndex++;
                if (walkIndex >= walk.length) {
                    walkIndex = 0;
                }
                walkTimer = 0;
            }
            hv = 0;
            v = 0;
        }
    }
    
    protected abstract static class HitBox extends Panctor implements StepListener, CollisionListener {
        protected final Chr hitter;
        
        protected HitBox(final Chr hitter) {
            this.hitter = hitter;
            setView(CrushGame.hitBox);
            //TODO position
            hitter.getLayer().addActor(this);
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            destroy(); //TODO wait until 2nd step?
        }
        
        @Override
        public void onCollision(final CollisionEvent event) {
            final Collidable collider = event.getCollider();
            final Chr enemy;
            if (collider instanceof Chr) {
                enemy = (Chr) collider;
            } else if (collider instanceof HitBox) {
                enemy = ((HitBox) collider).hitter;
            } else {
                return;
            }
            if (hitter.teamIndex == enemy.teamIndex) {
                return;
            }
            onHit(enemy);
        }
        
        protected abstract void onHit(final Chr enemy);
    }
    
    protected final static class AttackBox extends HitBox {
        protected AttackBox(final Chr hitter) {
            super(hitter);
        }
        
        @Override
        protected final void onHit(final Chr enemy) {
            hitter.onHit(enemy);
        }
    }
    
    protected final static class GrabBox extends HitBox {
        protected GrabBox(final Chr hitter) {
            super(hitter);
        }
        
        @Override
        protected final void onHit(final Chr enemy) {
            hitter.onGrab(enemy);
        }
    }
    
    protected final static class ChrImages {
        protected final Panmage still;
        protected final Panmage[] walk;
        protected final Panmage[] attack;
        protected final Panmage hurt;
        protected final Panmage stomp;
        protected final Panmage grab;
        protected final Panmage[] walkCarrying;
        protected final Panmage down;
        protected final Panmage stomped;
        
        protected ChrImages(final Panmage still, final Panmage[] walk, final Panmage[] attack, final Panmage hurt, final Panmage stomp, final Panmage grab,
                final Panmage[] walkCarrying, final Panmage down, final Panmage stomped) {
            this.still = still;
            this.walk = walk;
            this.attack = attack;
            this.hurt = hurt;
            this.stomp = stomp;
            this.grab = grab;
            this.walkCarrying = walkCarrying;
            this.down = down;
            this.stomped = stomped;
        }
    }
    
    protected abstract static class ChrMode {
        private final int time;
        
        protected ChrMode(final int time) {
            this.time = time;
        }
        
        protected boolean isFree(final Chr chr) {
            return false;
        }
        
        protected abstract void step(final Chr chr);
    }
    
    protected final static ChrMode MODE_NONE = new ChrMode(0) {
        @Override
        protected final boolean isFree(final Chr chr) {
            return true;
        }
        
        @Override
        protected final void step(final Chr chr) {
            chr.stepNone();
        }
    };
    
    protected final static ChrMode MODE_ATTACK = new ChrMode(FULL_ATTACK_TIME) {
        @Override
        protected final boolean isFree(final Chr chr) {
            return chr.timer <= COMBO_TIME;
        }
        
        @Override
        protected final void step(final Chr chr) {
            chr.timer--;
            if (isFree(chr)) {
                MODE_NONE.step(chr);
            } else if (chr.timer <= AFTER_ATTACK_TIME) {
                chr.changeView(chr.images.still);
            } else {
                chr.changeView(chr.images.attack[chr.comboIndex]);
            }
        }
    };
    
    protected final static ChrMode MODE_SLAM = new ChrMode(ATTACK_TIME) {
        @Override
        protected final void step(final Chr chr) {
            chr.timer--;
            chr.changeView(chr.images.grab); //TODO
        }
    };
    
    protected final static ChrMode MODE_GRAB = new ChrMode(3) {
        @Override
        protected final void step(final Chr chr) {
            chr.timer--;
            chr.changeView(chr.images.grab);
        }
    };
    
    protected final static ChrMode MODE_HURT = new ChrMode(FULL_ATTACK_TIME) {
        @Override
        protected final void step(final Chr chr) {
            chr.timer--;
            chr.changeView(chr.images.hurt);
        }
    };
    
    protected final static ChrMode MODE_CARRIED = new ChrMode(INDEFINITE_TIME) {
        @Override
        protected final void step(final Chr chr) {
            chr.changeView(chr.images.down);
        }
    };
}
