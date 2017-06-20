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

import java.util.*;
import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.boundary.*;
import org.pandcorps.pandam.impl.*;

public abstract class Boss extends Enemy {
    protected final static String RES_BOSS = BotsnBoltsGame.RES + "boss/";
    protected final static byte STATE_STILL = 0;
    
    private boolean initializationNeeded = true;
    protected int waitTimer = 0;
    protected byte state = 0;
    protected Queue<Jump> pendingJumps = null;
    private boolean jumping = false;
    
    protected Boss(int offX, int h, int x, int y) {
        super(offX, h, x, y, HudMeter.MAX_VALUE);
        startStill();
        setMirror(true);
    }
    
    private final void init() {
        addHealthMeter();
    }
    
    @Override
    protected final boolean onStepCustom() {
        if (initializationNeeded) {
            init();
            initializationNeeded = false;
        }
        if (waitTimer > 0) {
            waitTimer--;
            return onWaiting();
        } else if (state == STATE_STILL) {
            if (pollPendingJumps()) {
                return false;
            }
            return pickState();
        }
        return continueState();
    }
    
    @Override
    protected final void onLanded() {
        if (hasPendingJumps()) {
            startStill(5);
        } else {
            startStill();
        }
    }
    
    protected final boolean pollPendingJumps() {
        final Jump nextJump = (pendingJumps == null) ? null : pendingJumps.poll();
        if (nextJump == null) {
            return false;
        }
        startJump(nextJump);
        return true;
    }
    
    protected boolean onWaiting() {
        return false;
    }
    
    protected abstract boolean pickState();
    
    protected abstract boolean continueState();

    @Override
    protected final void award(final PowerUp powerUp) {
    }
    
    @Override
    protected final int getDamage() {
        return 4;
    }
    
    protected final void startState(final byte state, final int waitTimer, final Panmage img) {
        this.state = state;
        this.waitTimer = waitTimer;
        setView(img);
    }
    
    protected final void startStateIndefinite(final byte state, final Panmage img) {
        startState(state, Integer.MAX_VALUE, img);
    }
    
    protected final void startJump(final Jump jump) {
        startJump(jump.state, jump.img, jump.v, jump.hv);
    }
    
    protected final void startJump(final byte state, final Panmage img, final int v, final int hv) {
        startStateIndefinite(state, img);
        this.v = v;
        this.hv = hv;
        jumping = true;
    }
    
    protected final void addPendingJump(final Jump jump) {
        if (pendingJumps == null) {
            pendingJumps = new LinkedList<Jump>();
        }
        pendingJumps.add(jump);
    }
    
    protected final void addPendingJump(final byte state, final Panmage img, final int v, final int hv) {
        addPendingJump(new Jump(state, img, v, hv));
    }
    
    protected final boolean hasPendingJumps() {
        return Coltil.isValued(pendingJumps);
    }
    
    @Override
    protected final void onGrounded() {
        if (jumping) {
            hv = 0;
            if (!hasPendingJumps()) {
                turnTowardPlayer(); // Don't do in onLanded; hv still needed at that point, which overrides this
            }
            jumping = false;
        }
    }
    
    protected final int getDirection() {
        final Panlayer layer = getLayer();
        if (layer == null) {
            return -1;
        } else if (getPosition().getX() < (layer.getSize().getX() / 2)) {
            return 1;
        }
        return -1;
    }
    
    protected void startStill() {
        startStill(Mathtil.randi(15, 30));
    }
    
    protected void startStill(final int waitTimer) {
        startState(STATE_STILL, waitTimer, getStill());
    }
    
    protected final static Panmage getImage(final Panmage img, final String name, final Panple o, final Panple min, final Panple max) {
        return getImage(img, "boss.", RES_BOSS, name, o, min, max);
    }
    
    protected abstract Panmage getStill();
    
    protected final static int VOLCANO_OFF_X = 20, VOLCANO_H = 40;
    protected final static Panple VOLCANO_O = new FinPanple2(26, 1);
    protected final static Panple VOLCANO_MIN = getMin(VOLCANO_OFF_X);
    protected final static Panple VOLCANO_MAX = getMax(VOLCANO_OFF_X, VOLCANO_H);
    
    protected final static class VolcanoBot extends Boss {
        protected final static byte STATE_LIFT = 1;
        protected final static byte STATE_RAISED = 2;
        protected final static byte STATE_CROUCH = 3;
        protected final static byte STATE_JUMP = 4;
        protected static Panmage still = null;
        protected static Panmage lift = null;
        protected static Panmage raised = null;
        protected static Panmage crouch = null;
        protected static Panmage jump = null;
        
        protected float targetX = -1;
        
        protected VolcanoBot(int x, int y) {
            super(VOLCANO_OFF_X, VOLCANO_H, x, y);
        }
        
        @Override
        protected final int getInitialOffsetX() {
            return 0;
        }
        
        @Override
        protected final boolean onWaiting() {
            if (state == STATE_CROUCH) {
                final float t;
                switch (waitTimer) {
                    case 15 :
                        t = 1.0f;
                        break;
                    case 25 :
                        t = 0.67f;
                        break;
                    case 35 :
                        t = 0.33f;
                        break;
                    default :
                        t = -1.0f;
                        break;
                }
                if (t > 0) {
                    new LavaBall(this, t);
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
        
        protected final void startLift() {
            startState(STATE_LIFT, 5, getLift());
        }
        
        protected final void startJump() {
            final Panmage img = getJump();
            final int dir = getDirection();
            final int v = 10;
            if (Mathtil.rand()) {
                startJump(STATE_JUMP, img, v, dir * 9);
            } else {
                startJump(STATE_JUMP, img, v, dir * 7);
                addPendingJump(STATE_JUMP, img, v, -dir * 5);
                addPendingJump(STATE_JUMP, img, v, dir * 7);
            }
        }
        
        protected final void startRaised() {
            startState(STATE_RAISED, 10, getRaised());
        }
        
        protected final void startCrouch() {
            targetX = -1;
            startState(STATE_CROUCH, 50, getCrouch());
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
            return getImage(img, name, VOLCANO_O, VOLCANO_MIN, VOLCANO_MAX);
        }
    }
    
    protected final static class LavaBall extends EnemyProjectile {
        protected static Panmage lava1 = null;
        protected static Panmage lava2 = null;
        
        protected final VolcanoBot src;
        protected final float t;
        
        protected LavaBall(final VolcanoBot src, final float t) {
            super(getCurrentImage(), src, 11, 34, 0, 16, gTuple);
            this.src = src;
            this.t = t;
        }
        
        @Override
        public void onStep(final StepEvent event) {
            super.onStep(event);
            changeView(getCurrentImage());
            if (getVelocity().getY() < 0) {
                if (!isFlip()) {
                    setFlip(true);
                    final float sourceX = src.getPosition().getX();
                    float targetX = src.targetX;
                    if (targetX == -1) {
                        final Player player = src.getNearestPlayer();
                        if (player == null) {
                            final int m = getMirrorMultiplier();
                            targetX = sourceX + (m * 48);
                        } else {
                            targetX = player.getPosition().getX();
                        }
                        src.targetX = targetX;
                    }
                    getPosition().setX(sourceX + (t * (targetX - sourceX)));
                }
            }
        }

        @Override
        public void onAllOob(final AllOobEvent event) {
            if (getPosition().getY() < 0) {
                super.onAllOob(event);
            }
        }
        
        @Override
        protected final int getDamage() {
            return 3;
        }
        
        protected final static Panmage getCurrentImage() {
            return FireballEnemy.isFirstImageActive() ? getLava1() : getLava2();
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
    
    protected final static int HAIL_OFF_X = 6, HAIL_H = 24;
    protected final static Panple HAIL_O = new FinPanple2(14, 1);
    protected final static Panple HAIL_MIN = getMin(HAIL_OFF_X);
    protected final static Panple HAIL_MAX = getMax(HAIL_OFF_X, HAIL_H);
    
    protected final static class HailBot extends Boss {
        protected final static byte STATE_SHOOT = 1;
        protected final static byte STATE_SHOOT_DIAG = 2;
        protected final static byte STATE_JUMP = 3;
        protected final static byte STATE_SLIDE = 4;
        protected final static byte STATE_SLIDE_JUMP = 5;
        protected final static int WAIT_SHOOT = 30;
        protected final static int WAIT_SLIDE = 20;
        protected static Panmage still = null;
        protected static Panmage aim = null;
        protected static Panmage aimDiag = null;
        protected static Panmage jump = null;
        protected static Panmage fall = null;
        protected static Panmage slide1 = null;
        protected static Panmage slide2 = null;
        protected static Panmage trail = null;
        
        protected HailBot(int x, int y) {
            super(HAIL_OFF_X, HAIL_H, x, y);
        }
        
        @Override
        protected final boolean onWaiting() {
            if (state == STATE_SLIDE) {
                new TimedDecoration(this, getTrail(), WAIT_SLIDE, -14, -1, BotsnBoltsGame.DEPTH_CARRIER);
                addX(4 * getMirrorMultiplier());
                addY(4);
                final Panmage img;
                if (((WAIT_SLIDE - waitTimer) % 8) < 4) {
                    img = getSlide1();
                } else {
                    img = getSlide2();
                }
                changeView(img);
                return true;
            } else if (state == STATE_JUMP) {
                setJumpImage();
                if ((v >= 0) && (v < -g)) {
                    shootJump();
                }
            } else if (state == STATE_SLIDE_JUMP) {
                setJumpImage();
                if ((v > 7.6) && (v < 7.8)) {
                    shootJump();
                } else if (v < 0) {
                    final float y = getPosition().getY();
                    if ((y > 57) && (y < 59)) {
                        shootJump();
                    }
                }
            } else if (waitTimer == (WAIT_SHOOT - 1)) {
                if (state == STATE_SHOOT) {
                    new HailCluster(this, 21, 13, VEL_PROJECTILE, 0);
                } else if (state == STATE_SHOOT_DIAG) {
                    shootDiag(15, 24);
                }
            }
            return false;
        }
        
        private final void setJumpImage() {
            if (v < 0) {
                changeView(getFall());
            }
        }
        
        private final void shootDiag(final int ox, final int oy) {
            new HailCluster(this, ox, oy, VEL_PROJECTILE_45, VEL_PROJECTILE_45);
        }
        
        private final void shootJump() {
            shootDiag(17, 28);
        }
        
        @Override
        protected final boolean pickState() {
            final int r = Mathtil.randi(0, 99);
            if (r < 25) {
                startShoot();
            } else if (r < 50) {
                startShootDiag();
            } else if (r < 75) {
                startJump();
            } else {
                startSlide();
            }
            return false;
        }
        
        @Override
        protected final boolean continueState() {
            if (state == STATE_SLIDE) {
                startSlideJump();
            } else {
                startStill();
            }
            return false;
        }
        
        protected final void startShoot() {
            startState(STATE_SHOOT, WAIT_SHOOT, getAim());
        }
        
        protected final void startShootDiag() {
            startState(STATE_SHOOT_DIAG, WAIT_SHOOT, getAimDiag());
        }
        
        protected final void startJump() {
            startJump(STATE_JUMP, getJump(), 9, 0);
        }
        
        protected final void startSlide() {
            startState(STATE_SLIDE, WAIT_SLIDE, getSlide1());
        }
        
        protected final void startSlideJump() {
            startJump(STATE_SLIDE_JUMP, getJump(), 9, 4 * getMirrorMultiplier());
        }
        
        @Override
        protected final Panmage getStill() {
            return (still = getHailImage(still, "hailbot/HailBot"));
        }
        
        protected final static Panmage getAim() {
            return (aim = getHailImage(aim, "hailbot/HailBotAim"));
        }
        
        protected final static Panmage getAimDiag() {
            return (aimDiag = getHailImage(aimDiag, "hailbot/HailBotAimDiag"));
        }
        
        protected final static Panmage getJump() {
            return (jump = getHailImage(jump, "hailbot/HailBotJump"));
        }
        
        protected final static Panmage getFall() {
            return (fall = getHailImage(fall, "hailbot/HailBotFall"));
        }
        
        protected final static Panmage getSlide1() {
            return (slide1 = getHailImage(slide1, "hailbot/HailBotSlide1"));
        }
        
        protected final static Panmage getSlide2() {
            return (slide2 = getHailImage(slide2, "hailbot/HailBotSlide2"));
        }
        
        protected final static Panmage getHailImage(final Panmage img, final String name) {
            return getImage(img, name, HAIL_O, HAIL_MIN, HAIL_MAX);
        }
        
        protected final static Panmage getTrail() {
            return (trail = getImage(trail, "hailbot/IceTrail", null, null, null));
        }
    }
    
    protected final static class HailCluster extends EnemyProjectile {
        protected final static int OFF_WALL = 20;
        protected static Panmage cluster1 = null;
        protected static Panmage cluster2 = null;
        protected static Panmage chunk = null;
        
        protected HailCluster(final HailBot src, final int ox, final int oy, final float vx, final float vy) {
            super(getCurrentImage(), src, ox, oy, vx * src.getMirrorMultiplier(), vy);
        }
        
        @Override
        protected final void burst(final Player player) {
            onShatter();
        }
        
        @Override
        public void onStep(final StepEvent event) {
            super.onStep(event);
            changeView(getCurrentImage());
            final Panple pos = getPosition();
            final float x = pos.getX();
            if (isLeft(x)) {
                shatter();
            } else if (isRight(x)) {
                shatter();
            } else if (pos.getY() >= (BotsnBoltsGame.GAME_H - (16 + OFF_WALL))) {
                shatter();
            }
        }
        
        private final boolean isLeft(final float x) {
            return x < OFF_WALL;
        }
        
        private final boolean isRight(final float x) {
            return x >= (BotsnBoltsGame.GAME_W - OFF_WALL);
        }
        
        protected final void shatter() {
            onShatter();
            destroy();
        }
        
        protected void onShatter() {
            final float x = getPosition().getX();
            final boolean leftNeeded = !isLeft(x), rightNeeded = !isRight(x);
            for (int i = 1; i < 3; i++) {
                if (leftNeeded) {
                    newHailChunk(-i, i);
                }
                if (rightNeeded) {
                    newHailChunk(i, i);
                }
            }
            newHailChunk(0, 0);
        }
        
        protected final EnemyProjectile newHailChunk(final float vx, final float vy) {
            return new EnemyProjectile(getChunk(), this, 0, 0, vx + randVel(), vy + randVel(), gTuple);
        }
        
        protected final static float randVel() {
            return Mathtil.randf(-0.3f, 0.3f);
        }
        
        protected final static Panmage getCurrentImage() {
            return Pangine.getEngine().isOn(4) ? getCluster1() : getCluster2();
        }
        
        protected final static Panmage getCluster1() {
            return (cluster1 = getClusterImage(cluster1, "hailbot/HailCluster1"));
        }
        
        protected final static Panmage getCluster2() {
            return (cluster2 = getClusterImage(cluster2, "hailbot/HailCluster2"));
        }
        
        protected final static Panmage getClusterImage(final Panmage img, final String name) {
            return getImage(img, name, BotsnBoltsGame.CENTER_16, BotsnBoltsGame.MIN_16, BotsnBoltsGame.MAX_16);
        }
        
        protected final static Panmage getChunk() {
            return (chunk = getImage(chunk, "hailbot/HailChunk", BotsnBoltsGame.CENTER_8, BotsnBoltsGame.MIN_8, BotsnBoltsGame.MAX_8));
        }
    }
    
    protected final static int ROCKSLIDE_OFF_X = 20, ROCKSLIDE_H = 40; //TODO
    protected final static Panple ROCKSLIDE_O = new FinPanple2(26, 1);
    protected final static Panple ROCKSLIDE_MIN = getMin(ROCKSLIDE_OFF_X);
    protected final static Panple ROCKSLIDE_MAX = getMax(ROCKSLIDE_OFF_X, ROCKSLIDE_H);
    
    protected final static class RockslideBot extends Boss {
        protected RockslideBot(final int x, final int y) {
            super(0, 0, x, y);
        }

        @Override
        protected final boolean pickState() {
            return false;
        }

        @Override
        protected final boolean continueState() {
            return false;
        }

        @Override
        protected final Panmage getStill() {
            return null;
        }
    }
    
    protected final static class TimedDecoration extends Panctor implements StepListener {
        private int timer;
        
        protected TimedDecoration(final Panctor src, final Panmage img, final int timer, final int offX, final int offY, final int depth) {
            this.timer = timer;
            setView(img);
            final Panple pos = getPosition();
            if (src != null) {
                setMirror(src.isMirror());
                pos.set(src.getPosition());
                pos.add(src.getMirrorMultiplier() * offX, offY);
                final Panlayer layer = src.getLayer();
                if (layer != null) {
                    layer.addActor(this);
                }
            }
            pos.setZ(depth);
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            timer--;
            if (timer <= 0) {
                destroy();
            }
        }
    }
    
    protected final static class Jump {
        protected final byte state;
        protected final Panmage img;
        protected final int v;
        protected final int hv;
        
        protected Jump(final byte state, final Panmage img, final int v, final int hv) {
            this.state = state;
            this.img = img;
            this.v = v;
            this.hv = hv;
        }
    }
}
