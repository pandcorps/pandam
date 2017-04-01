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

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.boundary.*;
import org.pandcorps.pandam.impl.*;

public abstract class Boss extends Enemy {
    protected final static String RES_BOSS = BotsnBoltsGame.RES + "boss/";
    protected final static byte STATE_STILL = 0;
    
    protected int waitTimer = 0;
    protected byte state = 0;
    
    protected Boss(int offX, int h, int x, int y) {
        super(offX, h, x, y, HudMeter.MAX_VALUE);
        startStill();
        setMirror(true);
    }
    
    @Override
    protected final boolean onStepCustom() {
        if (waitTimer > 0) {
            waitTimer--;
            return onWaiting();
        } else if (state == STATE_STILL) {
            return pickState();
        }
        return continueState();
    }
    
    protected boolean onWaiting() {
        return false;
    }
    
    protected abstract boolean pickState();
    
    protected abstract boolean continueState();

    @Override
    protected final void award(final PowerUp powerUp) {
    }
    
    protected final void startState(final byte state, final int waitTimer, final Panmage img) {
        this.state = state;
        this.waitTimer = waitTimer;
        setView(img);
    }
    
    protected void startStill() {
        startState(STATE_STILL, Mathtil.randi(15, 30), getStill());
    }
    
    protected final static Panmage getImage(final Panmage img, final String name, final Panple o, final Panple min, final Panple max) {
        if (img != null) {
            return img;
        }
        return Pangine.getEngine().createImage("boss." + name, o, min, max, RES_BOSS + name + ".png");
    }
    
    protected abstract Panmage getStill();
    
    protected final static int VOLCANO_OFF_X = 20, VOLCANO_H = 48; //TODO
    protected final static Panple VOLCANO_O = new FinPanple2(26, 1);
    
    protected final static class VolcanoBot extends Boss {
        protected final static byte STATE_LIFT = 1;
        protected final static byte STATE_RAISED = 2;
        protected final static byte STATE_CROUCH = 3;
        protected static Panmage still = null;
        protected static Panmage lift = null;
        protected static Panmage raised = null;
        protected static Panmage crouch = null;
        protected static Panmage jump = null;
        
        protected VolcanoBot(int x, int y) {
            super(VOLCANO_OFF_X, VOLCANO_H, x, y);
        }
        
        @Override
        protected final boolean onWaiting() {
            if (state == STATE_CROUCH) {
                if (waitTimer == 15) {
                    new LavaBall(this, 11, 34);
                }
            }
            return false;
        }
        
        @Override
        protected final boolean pickState() {
            if (Mathtil.rand()) {
                startLift();
            } else {
                startJump();
            }
            return false;
        }
        
        @Override
        protected final boolean continueState() {
            switch (state) {
                case STATE_LIFT :
                    startRaised();
                    break;
                case STATE_RAISED :
                    startCrouch();
                    break;
                case STATE_CROUCH :
                    startStill();
                    break;
                default :
                    throw new IllegalStateException("Unexpected state " + state);
            }
            return false;
        }
        
        @Override
        protected final void onGrounded() {
            hv = 0;
        }
        
        protected final void startLift() {
            startState(STATE_LIFT, 5, getLift());
        }
        
        protected final void startJump() {
        }
        
        protected final void startRaised() {
            startState(STATE_RAISED, 10, getRaised());
        }
        
        protected final void startCrouch() {
            startState(STATE_CROUCH, 30, getCrouch());
        }
        
        @Override
        protected final Panmage getStill() {
            return (still = getVolcanoImage(still, "volcanobot/VolcanoBot"));
        }
        
        protected final static Panmage getLift() {
            return (lift = getVolcanoImage(lift, "volcanobot/VolcanoBotLift"));
        }
        
        protected final static Panmage getRaised() {
            return (raised = getVolcanoImage(raised, "volcanobot/VolcanoBotRaised"));
        }
        
        protected final static Panmage getCrouch() {
            return (crouch = getVolcanoImage(crouch, "volcanobot/VolcanoBotCrouch"));
        }
        
        protected final static Panmage getJump() {
            return (jump = getVolcanoImage(jump, "volcanobot/VolcanoBotJump"));
        }
        
        protected final static Panmage getVolcanoImage(final Panmage img, final String name) {
            return getImage(img, name, VOLCANO_O, null, null);
        }
    }
    
    protected final static class LavaBall extends EnemyProjectile {
        protected static Panmage lava1 = null;
        protected static Panmage lava2 = null;
        
        protected LavaBall(Enemy src, int ox, int oy) {
            super(getLava1(), src, ox, oy, 0, 16);
            getAcceleration().setY(g);
            
        }
        
        @Override
        public void onStep(final StepEvent event) {
            super.onStep(event);
            setView(FireballEnemy.isFirstImageActive() ? getLava1() : getLava2());
            if (getVelocity().getY() < 0) {
                if (!isFlip()) {
                    setFlip(true);
                    final int m = isMirror() ? -1 : 1;
                    getPosition().addX(m * 48);
                }
            }
        }

        @Override
        public void onAllOob(final AllOobEvent event) {
            if (getPosition().getY() < 0) {
                super.onAllOob(event);
            }
        }
        
        protected final static Panmage getLava1() {
            return (lava1 = getLavaImage(lava1, "volcanobot/LavaBall1"));
        }
        
        protected final static Panmage getLava2() {
            return (lava2 = getLavaImage(lava2, "volcanobot/LavaBall2"));
        }
        
        protected final static Panmage getLavaImage(final Panmage img, final String name) {
            final Panmage ref = BotsnBoltsGame.fireballEnemy[0];
            return getImage(img, name, ref.getOrigin(), ref.getBoundingMinimum(), ref.getBoundingMaximum());
        }
    }
}
