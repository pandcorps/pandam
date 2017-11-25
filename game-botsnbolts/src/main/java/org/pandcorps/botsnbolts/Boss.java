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
import org.pandcorps.pandax.tile.*;

public abstract class Boss extends Enemy {
    protected final static String RES_BOSS = BotsnBoltsGame.RES + "boss/";
    protected final static byte STATE_STILL = 0;
    
    private boolean initializationNeeded = true;
    protected int waitTimer = 0;
    protected byte state = 0;
    protected Queue<Jump> pendingJumps = null;
    private boolean jumping = false;
    protected int moves = -1;
    
    protected Boss(int offX, int h, int x, int y) {
        super(offX, h, x, y, HudMeter.MAX_VALUE);
        init();
        startStill();
        setMirror(true);
    }
    
    protected void init() {
    }
    
    private final void onFirstStep() {
        addHealthMeter();
    }
    
    @Override
    protected final boolean onStepCustom() {
        if (initializationNeeded) {
            onFirstStep();
            initializationNeeded = false;
        }
        if (waitTimer > 0) {
            waitTimer--;
            return onWaiting();
        } else if (state == STATE_STILL) {
            if (pollPendingJumps()) {
                return false;
            }
            moves++;
            return pickState();
        }
        return continueState();
    }
    
    @Override
    protected final void onLanded() {
        if (hasPendingJumps()) {
            startStill(5);
        } else if (!onBossLanded()) {
            startStill();
        }
    }
    
    protected boolean onBossLanded() {
        return false;
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
    
    protected static Panmage getImage(final Panmage img, final String name, final Panmage ref) {
        if (img != null) {
            return img;
        }
        return getImage(img, name, ref.getOrigin(), ref.getBoundingMinimum(), ref.getBoundingMaximum());
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
            if (img != null) {
                return img;
            }
            return getImage(img, name, BotsnBoltsGame.fireballEnemy[0]);
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
        
        protected final void onShatter() {
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
    
    protected final static int ROCKSLIDE_OFF_X = 22, ROCKSLIDE_H = 48;
    protected final static Panple ROCKSLIDE_O = new FinPanple2(28, 1);
    protected final static Panple ROCKSLIDE_MIN = getMin(ROCKSLIDE_OFF_X);
    protected final static Panple ROCKSLIDE_MAX = getMax(ROCKSLIDE_OFF_X, ROCKSLIDE_H);
    protected final static int ROCKSLIDE_ROLL_OFF_X = 13, ROCKSLIDE_ROLL_H = 38;
    protected final static Panple ROCKSLIDE_ROLL_O = new FinPanple2(19, 1);
    protected final static Panple ROCKSLIDE_ROLL_MIN = getMin(ROCKSLIDE_ROLL_OFF_X);
    protected final static Panple ROCKSLIDE_ROLL_MAX = getMax(ROCKSLIDE_ROLL_OFF_X, ROCKSLIDE_ROLL_H);
    
    protected final static class RockslideBot extends Boss {
        protected final static byte STATE_SHOOT = 1;
        protected final static byte STATE_CROUCH = 2;
        protected final static byte STATE_CURL = 3;
        protected final static byte STATE_ROLL = 4;
        protected final static byte STATE_JUMP = 5;
        protected final static int WAIT_SHOOT = 30;
        protected static Panmage still = null;
        protected static Panmage aim = null;
        protected static Panmage crouch = null;
        protected static Panmage curl = null;
        protected static Panmage roll1 = null;
        protected static Panmage roll2 = null;
        protected static Panmage jump = null;
        private final static Rotator rots = new RollRotator();
        private final static Panframe[] frames = new Panframe[Rotator.numFrames];
        
        protected RockslideBot(final int x, final int y) {
            super(ROCKSLIDE_OFF_X, ROCKSLIDE_H, x, y);
        }
        
        @Override
        protected final boolean onWaiting() {
            if (state == STATE_SHOOT) {
                if (waitTimer == (WAIT_SHOOT - 1)) {
                    new Rock(this, 36, 3);
                }
            } else if (state == STATE_ROLL) {
                if (getPosition().getX() <= 40) {
                    startJump();
                } else {
                    rots.onStep(this, frames);
                }
            }
            return false;
        }

        @Override
        protected final boolean pickState() {
            turnTowardPlayer();
            if (isMirror()) {
                if (Mathtil.rand()) {
                    startShoot();
                } else {
                    startCrouch();
                }
            } else {
                startShoot();
            }
            return false;
        }

        @Override
        protected final boolean continueState() {
            switch (state) {
                case STATE_CROUCH :
                    startCurl();
                    break;
                case STATE_CURL :
                    startRoll();
                    break;
                case STATE_ROLL :
                    setOffX(ROCKSLIDE_OFF_X);
                    setH(ROCKSLIDE_H);
                case STATE_SHOOT :
                    startStill();
                    break;
                default :
                    throw new IllegalStateException("Unexpected state " + state);
            }
            return false;
        }
        
        protected final void startShoot() {
            startState(STATE_SHOOT, WAIT_SHOOT, getAim());
        }
        
        protected final void startCrouch() {
            startState(STATE_CROUCH, 2, getCrouch());
        }
        
        protected final void startCurl() {
            startState(STATE_CURL, 2, getCurl());
        }
        
        protected final void startRoll() {
            startStateIndefinite(STATE_ROLL, getRoll1());
            rots.init();
            setView(rots.getFrame(frames));
            setOffX(ROCKSLIDE_ROLL_OFF_X);
            setH(ROCKSLIDE_ROLL_H);
            hv = 3 * getMirrorMultiplier();
        }
        
        protected final void startJump() {
            final Panmage img = getJump();
            startJump(STATE_JUMP, img, 11, 3);
            addPendingJump(STATE_JUMP, img, 13, 6);
            addPendingJump(STATE_JUMP, img, 4, 1);
        }

        @Override
        protected final Panmage getStill() {
            return (still = getRockslideImage(still, "rockslidebot/RockslideBot"));
        }
        
        protected final static Panmage getAim() {
            return (aim = getRockslideImage(aim, "rockslidebot/RockslideBotAim"));
        }
        
        protected final static Panmage getCrouch() {
            return (crouch = getRockslideImage(crouch, "rockslidebot/RockslideBotCrouch"));
        }
        
        protected final static Panmage getCurl() {
            return (curl = getRockslideImage(curl, "rockslidebot/RockslideBotCurl"));
        }
        
        protected final static Panmage getRoll1() {
            return (roll1 = getRockslideRollImage(roll1, "rockslidebot/RockslideBotRoll1"));
        }
        
        protected final static Panmage getRoll2() {
            return (roll2 = getRockslideRollImage(roll2, "rockslidebot/RockslideBotRoll2"));
        }
        
        protected final static Panmage getJump() {
            return (jump = getRockslideImage(jump, "rockslidebot/RockslideBotJump"));
        }
        
        protected final static Panmage getRockslideImage(final Panmage img, final String name) {
            return getImage(img, name, ROCKSLIDE_O, ROCKSLIDE_MIN, ROCKSLIDE_MAX);
        }
        
        protected final static Panmage getRockslideRollImage(final Panmage img, final String name) {
            return getImage(img, name, ROCKSLIDE_ROLL_O, ROCKSLIDE_ROLL_MIN, ROCKSLIDE_ROLL_MAX);
        }
        
        private final static class RollRotator extends Rotator {
            private RollRotator() {
                super(4);
            }
            
            @Override
            protected final int getDim(final Panmage img) {
                return 41;
            }
            
            @Override
            protected final Panmage getImage1() {
                return getRoll1();
            }
            
            @Override
            protected final Panmage getImage2() {
                return getRoll2();
            }
        }
    }
    
    protected final static class Rock extends Enemy {
        protected static Panmage rock1 = null;
        protected static Panmage rock2 = null;
        protected static Panmage rockShatter = null;
        private final static Rotator rots = new RockRotator();
        private final static Panframe[] frames = new Panframe[Rotator.numFrames];
        private final static int speed = 3;
        
        protected Rock(final RockslideBot src, final int ox, final int oy) {
            super(PROP_OFF_X, PROP_H, 0, 0, 1);
            EnemyProjectile.addBySource(this, getRock1(), src, ox, oy);
            setView(rots.getFrame(frames));
            hv = src.getMirrorMultiplier() * speed;
            v = -speed;
        }
        
        @Override
        protected final boolean onStepCustom() {
            rots.onStep(this, frames);
            return super.onStepCustom();
        }
        
        @Override
        protected final void onWall(final byte xResult) {
            shatter();
        }
        
        protected final void shatter() {
            destroy();
        }
        
        @Override
        protected final void onEnemyDestroy() {
            onShatter();
        }
        
        protected final void onShatter() {
            Player.shatter(this, getRockShatter());
        }

        @Override
        protected final void award(final PowerUp powerUp) {
            
        }
        
        protected final static Panframe getFrame(final Panframe[] frames, final Rotator rots) {
            final int frameIndex = rots.frameIndex;
            Panframe frame = frames[frameIndex];
            if (frame == null) {
                final boolean basedOnImg1 = ((frameIndex % 2) == 0);
                final Panmage img = basedOnImg1 ? rots.getImage1() : rots.getImage2();
                final int rot = (4 - (frameIndex / 2)) % 4;
                final Panple o, min, max;
                if (basedOnImg1) {
                    final Panple oBase = img.getOrigin();
                    final Panple minBase = img.getBoundingMinimum();
                    final Panple maxBase = img.getBoundingMaximum();
                    final int end = rots.getDim(img) - 1;
                    if (rot == 0) {
                        o = oBase;
                        min = minBase;
                        max = maxBase;
                    } else if (rot == 3) {
                        o = new FinPanple2(end - oBase.getY(), oBase.getX());
                        min = new FinPanple2(-maxBase.getY(), minBase.getX());
                        max = new FinPanple2(-minBase.getY(), maxBase.getX());
                    } else if (rot == 2) {
                        o = new FinPanple2(end - oBase.getX(), end - oBase.getY());
                        min = new FinPanple2(-maxBase.getX(), -maxBase.getY());
                        max = new FinPanple2(-minBase.getX(), -minBase.getY());
                    } else if (rot == 1) {
                        o = new FinPanple2(oBase.getY(), end - oBase.getX());
                        min = new FinPanple2(minBase.getY(), -maxBase.getX());
                        max = new FinPanple2(maxBase.getY(), -minBase.getX());
                    } else {
                        throw new IllegalStateException("Unexpected rotation " + rot);
                    }
                } else {
                    final Panframe prev = frames[frameIndex - 1];
                    o = prev.getOrigin();
                    min = prev.getBoundingMinimum();
                    max = prev.getBoundingMaximum();
                }
                frame = Pangine.getEngine().createFrame(BotsnBoltsGame.PRE_FRM + rots.getClass().getSimpleName() + "." + frameIndex, img, rots.frameDuration, rot, false, false, o, min, max);
                frames[frameIndex] = frame;
            }
            return frame;
        }
        
        protected final static Panmage getRock1() {
            return (rock1 = getRockImage(rock1, "rockslidebot/Rock1"));
        }
        
        protected final static Panmage getRock2() {
            return (rock2 = getRockImage(rock2, "rockslidebot/Rock2"));
        }
        
        protected final static Panmage getRockShatter() {
            return (rockShatter = Boss.getImage(rockShatter, "rockslidebot/RockShatter", BotsnBoltsGame.CENTER_8, null, null));
        }
        
        private final static Panmage getRockImage(final Panmage img, final String name) {
            if (img != null) {
                return img;
            }
            return Boss.getImage(img, name, BotsnBoltsGame.fireballEnemy[0]);
        }
        
        private final static class RockRotator extends Rotator {
            private RockRotator() {
                super(2);
            }
            
            @Override
            protected final Panmage getImage1() {
                return getRock1();
            }
            
            @Override
            protected final Panmage getImage2() {
                return getRock2();
            }
        }
    }
    
    protected final static int LIGHTNING_OFF_X = 6, LIGHTNING_H = 24; //TODO
    protected final static Panple LIGHTNING_O = new FinPanple2(14, 1);
    protected final static Panple LIGHTNING_MIN = getMin(LIGHTNING_OFF_X);
    protected final static Panple LIGHTNING_MAX = getMax(LIGHTNING_OFF_X, LIGHTNING_H);
    
    protected final static class LightningBot extends Boss {
        protected final static byte STATE_JUMP = 1;
        protected final static byte STATE_STRIKE = 2;
        protected final static int WAIT_STRIKE = 15;
        protected static Panmage still = null;
        protected static Panmage jump = null;
        protected static Panmage strike = null;
        protected static Panmage fall = null;
        
        protected LightningBot(final int x, final int y) {
            super(LIGHTNING_OFF_X, LIGHTNING_H, x, y);
        }
        
        @Override
        protected final boolean onWaiting() {
            if (state == STATE_STRIKE) {
                if (waitTimer == (WAIT_STRIKE - 1)) {
                    new Lightning(this);
                }
                return true;
            } else if (state == STATE_JUMP) {
                if (v > 0) {
                    if (getPosition().getY() >= 166) {
                        startStrike();
                    }
                } else if (v < 0) {
                    changeView(getFall());
                }
            }
            return false;
        }

        @Override
        protected final boolean pickState() {
            startJump();
            return false;
        }

        @Override
        protected final boolean continueState() {
            if (state == STATE_STRIKE) {
                finishJump();
            } else {
                startStill();
            }
            return false;
        }
        
        protected final void startJump() {
            startJump(13);
        }
        
        protected final void finishJump() {
            startJump(0);
        }
        
        protected final void startJump(final int v) {
            startJump(STATE_JUMP, getJump(), v, 4 * getMirrorMultiplier());
        }
        
        protected final void startStrike() {
            startState(STATE_STRIKE, WAIT_STRIKE, getStrike());
        }

        @Override
        protected final Panmage getStill() {
            return (still = getLightningImage(still, "lightningbot/LightningBot"));
        }
        
        protected final Panmage getJump() {
            return (jump = getLightningImage(jump, "lightningbot/LightningBotJump"));
        }
        
        protected final Panmage getStrike() {
            return (strike = getLightningImage(strike, "lightningbot/LightningBotStrike"));
        }
        
        protected final Panmage getFall() {
            return (fall = getLightningImage(fall, "lightningbot/LightningBotFall"));
        }
        
        protected final static Panmage getLightningImage(final Panmage img, final String name) {
            return getImage(img, name, LIGHTNING_O, LIGHTNING_MIN, LIGHTNING_MAX);
        }
    }
    
    protected final static class Lightning extends TimedEnemyProjectile {
        private final static int DURATION_LIGHTNING = 20;
        private final static int ROOT_MAX = 10;
        private final static int ROOT_BASE = 2;
        private final static int[] forkScratch = { 5, 6, 7, 8, 9 };
        private static Panmage lightning1 = null;
        private static Panmage lightning2 = null;
        private static Panmage lightning3 = null;
        private final LightningBot src;
        private final int x;
        private final int jMax;
        private final int jBase;
        private final int jLeft;
        private final int jRight;
        private Lightning lightningLeft = null;
        private Lightning lightningRight = null;
        private final int[] verticalScratch = { 0, 1, 2, 4, 5, 6, 8, 9, 14 };
        private final int bottom;
        private final boolean mirrorFlag;
        private final int mirrorBase;
        private final Pansplay display = new OriginPansplay(new LightningMinimum(), new LightningMaximum());
        
        protected Lightning(final LightningBot src) {
            this(src, Math.round(src.getPosition().getX()) - (src.isMirror() ? 11 : 4), ROOT_MAX, ROOT_BASE, DURATION_LIGHTNING);
        }
        
        protected Lightning(final LightningBot src, final int x, final int jMax, final int jBase, final int timer) {
            super(src, 10, 0, timer);
            this.src = src;
            this.x = x;
            this.jMax = jMax;
            this.jBase = jBase;
            if (isRoot()) {
                Mathtil.shuffle(forkScratch);
                jLeft = forkScratch[0];
                jRight = forkScratch[1];
            } else {
                jLeft = jRight = -1;
            }
            Mathtil.shuffle(verticalScratch);
            final int r = Mathtil.randi(0, 299);
            if (r < 100) {
                bottom = 7;
            } else if (r < 200) {
                bottom = 12;
            } else {
                bottom = 13;
            }
            mirrorFlag = Mathtil.rand();
            mirrorBase = Mathtil.randi(1, 3);
        }
        
        private final boolean isRoot() {
            return jMax == ROOT_MAX;
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panmage img = getCurrentImage();
            int jMin;
            if (timer == DURATION_LIGHTNING) {
                jMin = 8;
            } else if (timer == (DURATION_LIGHTNING - 1)) {
                jMin = 5;
            } else {
                jMin = jBase;
            }
            if (jMin > jMax) {
                jMin = jMax;
            }
            boolean firstFork = true;
            for (int j = jMax; j >= jMin; j--) {
                final int index;
                final boolean mirror;
                boolean fork = false;
                Lightning childLightning = null;
                if ((j == jMax) && isRoot()) {
                    index = getTop();
                    mirror = isMirror(); // Top mirror shouldn't be random; based on this object (which is based on src)
                } else if ((j == jMin) && (!isRoot() || (j != jBase))) {
                    index = getBottom();
                    mirror = isMirror(j);
                } else if (j == jLeft) {
                    index = getFork();
                    mirror = true;
                    fork = true;
                    childLightning = lightningLeft;
                } else if (j == jRight) {
                    index = getFork();
                    mirror = false;
                    fork = true;
                    childLightning = lightningRight;
                } else {
                    index = getVertical(j);
                    mirror = isMirror(j);
                }
                renderIndex(renderer, x, j, index, img, mirror);
                if (fork) {
                    final int mult = (mirror ? -1 : 1) * 16;
                    final int w = (j == jMin) ? 1 : 2;
                    for (int i = 1; i <= w; i++) {
                        final int i1 = i - 1, xFork = x + (mult * i), jFork = j - i1;;
                        if (i > 1) {
                            renderIndex(renderer, x + (mult * i1), jFork, getDiagonalTop(), img, mirror);
                        }
                        renderIndex(renderer, xFork, jFork, getDiagonalBottom(), img, mirror);
                        if ((i == w) && (childLightning == null)) {
                            final int jNext = jFork - 1;
                            final int baseNext = jBase + (firstFork ? 2 : 1);
                            childLightning = new Lightning(src, xFork, jNext, baseNext, timer - 1);
                            if (mirror) {
                                lightningLeft = childLightning;
                            } else {
                                lightningRight = childLightning;
                            }
                            renderIndex(renderer, xFork, jNext, childLightning.getBottom(), img, childLightning.isMirror(jNext)); // Actor created during renderView won't be displayed till next frame
                        }
                    }
                    firstFork = false;
                }
            }
        }
        
        @Override
        public Pansplay getCurrentDisplay() {
            return display;
        }
        
        private final class LightningMinimum extends UnmodPanple2 {
            @Override
            public final float getX() {
                return 2;
            }

            @Override
            public final float getY() {
                return 0; //TODO
                //return min * 16;
            }
        }
        
        private final class LightningMaximum extends UnmodPanple2 {
            @Override
            public final float getX() {
                return 14;
            }

            @Override
            public final float getY() {
                return 0; //TODO
                //return max * 16;
            }
        }
        
        private final void renderIndex(final Panderer renderer, final int x, final int j, final int index, final Panmage img, final boolean mirror) {
            final int d = 16;
            final int ix = (index % 4) * d, iy = (index / 4) * d;
            renderer.render(getLayer(), img, x, j * d, BotsnBoltsGame.DEPTH_PROJECTILE, ix, iy, d, d, 0, mirror, false);
        }
        
        protected final boolean isMirror(final int y) {
            final boolean b = (y % (2 * mirrorBase)) < mirrorBase;
            return mirrorFlag ? b : !b;
        }
        
        protected final static int getTop() {
            return 3;
        }
        
        protected final int getVertical(final int j) {
            return verticalScratch[(j - ROOT_BASE) % verticalScratch.length];
        }
        
        protected final int getBottom() {
            return bottom;
        }
        
        protected final static int getFork() {
            return 10;
        }
        
        protected final static int getDiagonalBottom() {
            return 11;
        }
        
        protected final static int getDiagonalTop() {
            return 15;
        }
        
        protected final Panmage getCurrentImage() {
            if (timer < 2) {
                return getLightning3();
            } else if (timer < 4) {
                return getLightning2();
            } else {
                return getLightning1();
            }
        }
        
        protected final static Panmage getLightning1() {
            return (lightning1 = getLightningImage(lightning1, "lightningbot/Lightning1"));
        }
        
        protected final static Panmage getLightning2() {
            return (lightning2 = getLightningImage(lightning2, "lightningbot/Lightning2"));
        }
        
        protected final static Panmage getLightning3() {
            return (lightning3 = getLightningImage(lightning3, "lightningbot/Lightning3"));
        }
        
        protected final static Panmage getLightningImage(final Panmage img, final String name) {
            return getImage(img, name, null, null, null);
        }
    }
    
    protected final static int EARTHQUAKE_OFF_X = 12, EARTHQUAKE_H = 30;
    protected final static Panple EARTHQUAKE_O = new FinPanple2(16, 1);
    protected final static Panple EARTHQUAKE_MIN = getMin(EARTHQUAKE_OFF_X);
    protected final static Panple EARTHQUAKE_MAX = getMax(EARTHQUAKE_OFF_X, EARTHQUAKE_H);
    
    protected final static class EarthquakeBot extends Boss {
        protected final static byte STATE_JUMP = 1;
        protected final static byte STATE_JUMP_DRILL = 2;
        protected final static byte STATE_JUMP_DRILL_IMPACT = 3;
        protected final static byte STATE_DRILL1 = 4;
        protected final static byte STATE_DRILL2 = 5;
        protected final static byte STATE_DRILL3 = 6;
        protected final static byte STATE_DRILL4 = 7;
        protected final static int WAIT_JUMP_DRILL = 24;
        protected static Panmage still = null;
        protected static Panmage jump = null;
        protected static Panmage jumpDrillStart = null;
        protected static Panmage jumpDrill1 = null;
        protected static Panmage jumpDrill2 = null;
        protected static Panmage jumpDrill3 = null;
        protected static Panmage jumpDrillImpact = null;
        protected static Panmage drill1 = null;
        protected static Panmage drill2 = null;
        protected static Panmage drill3 = null;
        protected static Panmage drill3b = null;
        protected static Panmage drill4 = null;
        private int drillTimer = -1;
        
        protected EarthquakeBot(final int x, final int y) {
            super(EARTHQUAKE_OFF_X, EARTHQUAKE_H, x, y);
        }
        
        @Override
        protected final boolean onWaiting() {
            if (state == STATE_JUMP_DRILL) {
                drillTimer++;
                if (drillTimer < 2) {
                    return true;
                }
                final Panmage img;
                final int m = drillTimer % 6;
                if (m < 2) {
                    img = getJumpDrill3();
                } else if (m < 4) {
                    img = getJumpDrill1();
                } else {
                    img = getJumpDrill2();
                }
                changeView(img);
                if (drillTimer < WAIT_JUMP_DRILL) {
                    return true;
                }
            } else if (state == STATE_JUMP) {
                if ((v > 0) && (getPosition().getY() >= 133)) {
                    startJumpDrill();
                }
            } else if (state == STATE_DRILL3) {
                drillTimer++;
                if (drillTimer < 12) {
                    return false;
                }
                changeView(((drillTimer % 4) < 2) ? getDrill3b() : getDrill3());
            } else if (state == STATE_DRILL4) {
                drillTimer++;
                if (drillTimer == 0) {
                    startEarthquake(-17, 2, 1);
                } else if (drillTimer == 1) {
                    startDirtShatter();
                }
            }
            return false;
        }
        
        private final void startEarthquake(final int backOx, final int size, final int remaining) {
            new Earthquake(this, backOx, 0, size, remaining);
            new Earthquake(this, 12, 0, size, remaining).setMirror(!isMirror());
        }
        
        private final void startDirtShatter() {
            Player.shatter(this, DrillEnemy.getDirtShatter());
        }
        
        @Override
        protected final boolean onBossLanded() {
            if (state == STATE_JUMP_DRILL) {
                startJumpDrillImpact();
                startEarthquake(-11, 8, 3);
                startDirtShatter();
                return true;
            }
            return false;
        }

        @Override
        protected final boolean pickState() {
            if (moves == 0) {
                startDrill1(); // Start with this; loads images needed for jump impact
                return false;
            } else {
                startJump();
                moves = -1;
            }
            return false;
        }

        @Override
        protected final boolean continueState() {
            switch (state) {
                case STATE_DRILL1 :
                    startDrill2();
                    break;
                case STATE_DRILL2 :
                    startDrill3();
                    break;
                case STATE_DRILL3 :
                    startDrill4();
                    break;
                case STATE_JUMP_DRILL_IMPACT :
                    startJump(2);
                    break;
                default :
                    turnTowardPlayer();
                    startStill();
                    break;
            }
            return false;
        }
        
        protected final void startJump() {
            startJump(12);
        }
        
        protected final void startJump(final int v) {
            startJump(STATE_JUMP, getJump(), v, 0);
        }
        
        protected final void startJumpDrill() {
            startStateIndefinite(STATE_JUMP_DRILL, getJumpDrillStart());
            v = 0;
            drillTimer = -1;
        }
        
        protected final void startJumpDrillImpact() {
            startState(STATE_JUMP_DRILL_IMPACT, 20, getJumpDrillImpact());
        }
        
        protected final void startDrill1() {
            startState(STATE_DRILL1, 2, getDrill1());
        }
        
        protected final void startDrill2() {
            startState(STATE_DRILL2, 2, getDrill2());
        }
        
        protected final void startDrill3() {
            startState(STATE_DRILL3, 24, getDrill3());
            drillTimer = -1;
        }
        
        protected final void startDrill4() {
            startState(STATE_DRILL4, 30, getDrill4());
            drillTimer = -1;
        }

        @Override
        protected final Panmage getStill() {
            return (still = getEarthquakeImage(still, "earthquakebot/EarthquakeBot"));
        }
        
        protected final static Panmage getJump() {
            return (jump = getEarthquakeImage(jump, "earthquakebot/EarthquakeBotJump"));
        }
        
        protected final static Panmage getJumpDrillStart() {
            return (jumpDrillStart = getEarthquakeImage(jumpDrillStart, "earthquakebot/EarthquakeBotJumpDrillStart"));
        }
        
        protected final static Panmage getJumpDrill1() {
            return (jumpDrill1 = getEarthquakeImage(jumpDrill1, "earthquakebot/EarthquakeBotJumpDrill1"));
        }
        
        protected final static Panmage getJumpDrill2() {
            return (jumpDrill2 = getEarthquakeImage(jumpDrill2, "earthquakebot/EarthquakeBotJumpDrill2"));
        }
        
        protected final static Panmage getJumpDrill3() {
            return (jumpDrill3 = getEarthquakeImage(jumpDrill3, "earthquakebot/EarthquakeBotJumpDrill3"));
        }
        
        protected final static Panmage getJumpDrillImpact() {
            if (jumpDrillImpact != null) {
                return jumpDrillImpact;
            }
            return (jumpDrillImpact = getEarthquakeImage(jumpDrillImpact, "earthquakebot/EarthquakeBotJumpDrillImpact", new FinPanple2(16, 11)));
        }
        
        protected final static Panmage getDrill1() {
            return (drill1 = getEarthquakeImage(drill1, "earthquakebot/EarthquakeBotDrill1"));
        }
        
        protected final static Panmage getDrill2() {
            return (drill2 = getEarthquakeImage(drill2, "earthquakebot/EarthquakeBotDrill2"));
        }
        
        protected final static Panmage getDrill3() {
            return (drill3 = getEarthquakeImage(drill3, "earthquakebot/EarthquakeBotDrill3"));
        }
        
        protected final static Panmage getDrill3b() {
            return (drill3b = getEarthquakeImage(drill3b, "earthquakebot/EarthquakeBotDrill3b"));
        }
        
        protected final static Panmage getDrill4() {
            if (drill4 != null) {
                return drill4;
            }
            return (drill4 = getEarthquakeImage(drill4, "earthquakebot/EarthquakeBotDrill4", new FinPanple2(15, 1)));
        }
        
        protected final static Panmage getEarthquakeImage(final Panmage img, final String name) {
            return getEarthquakeImage(img, name, EARTHQUAKE_O);
        }
        
        protected final static Panmage getEarthquakeImage(final Panmage img, final String name, final Panple o) {
            return getImage(img, name, o, EARTHQUAKE_MIN, EARTHQUAKE_MAX);
        }
    }
    
    protected final static class Earthquake extends EnemyProjectile {
        private static Panmage fullImage = null;
        private final static Panmage[] images = new Panmage[3];
        private final static int positionDuration = 1;
        private final static int frameDuration = positionDuration * 4;
        private final int velX;
        private final int size;
        private final int remaining;
        private final int maxIndex;
        private int timer = 0;
        private int index = 0;
        private int distance = 0;
        
        protected Earthquake(final Panctor src, final int ox, final int oy, final int size, final int remaining) {
            this(src, ox, oy, size, remaining, 2, 0);
        }
        
        protected Earthquake(final Panctor src, final int ox, final int oy, final int size, final int remaining, final int maxIndex, final int velX) {
            super(getSubImage(0), src, ox, oy, 0, 0);
            this.velX = (velX == 0) ? (8 * getMirrorMultiplier() * ox / Math.abs(ox)) : velX;
            this.size = size;
            this.remaining = remaining;
            this.maxIndex = maxIndex;
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            super.onStep(event);
            timer++;
            if ((timer % positionDuration) == 0) {
                getPosition().addX(velX);
                distance += Math.abs(velX);
                if ((distance == 16) && (remaining > 0)) {
                    new Earthquake(this, 16, 0, size, remaining - 1, Math.max(0, maxIndex - 1), velX);
                }
                if ((timer % frameDuration) == 0) {
                    if (index < size) {
                        index++;
                        if (index <= maxIndex) {
                            changeView(getSubImage(index));
                        }
                    } else {
                        destroy();
                    }
                }
            }
        }
        
        protected final static Panmage getSubImage(final int i) {
            Panmage image = images[i];
            if (image == null) {
                final float subX, subY;
                final Panple o, min, max, size;
                if (i == 0) {
                    subX = 17;
                    subY = 0;
                    o = new FinPanple2(7, 1);
                    min = new FinPanple2(-5, 0);
                    max = new FinPanple2(5, 5);
                    size = new FinPanple2(15, 15);
                } else {
                    final Panmage image0 = getSubImage(0);
                    o = image0.getOrigin();
                    min = image0.getBoundingMinimum();
                    if (i == 1) {
                        subX = 17;
                        subY = 16;
                        max = new FinPanple2(5, 10);
                        size = image0.getSize();
                    } else {
                        subX = 0;
                        subY = 0;
                        max = new FinPanple2(5, 24);
                        size = new FinPanple2(15, 32);
                    }
                }
                image = new SubPanmage("Earthquake." + i, o, min, max, getFullImage(), subX, subY, size);
                images[i] = image;
            }
            return image;
        }
        
        protected final static Panmage getFullImage() {
            return (fullImage = getImage(fullImage, "earthquakebot/Earthquake", null, null, null));
        }
    }
    
    protected final static int CYCLONE_OFF_X = 6, CYCLONE_H = 24; //TODO
    protected final static Panple CYCLONE_O = new FinPanple2(14, 1);
    protected final static Panple CYCLONE_MIN = getMin(CYCLONE_OFF_X);
    protected final static Panple CYCLONE_MAX = getMax(CYCLONE_OFF_X, CYCLONE_H);
    protected final static Panple CYCLONE_SPIN_O = new FinPanple2(30, 1);
    protected final static Panple CYCLONE_SPIN_MIN = getMin(CYCLONE_OFF_X);
    protected final static Panple CYCLONE_SPIN_MAX = getMax(CYCLONE_OFF_X, CYCLONE_H);
    
    protected final static class CycloneBot extends Boss {
        protected final static byte STATE_LAUNCH = 1;
        protected final static byte STATE_LAUNCH_END = 2;
        protected final static byte STATE_SPIN = 3;
        protected final static int WAIT_LAUNCH = 19;
        protected final static int WAIT_SPIN = 171;
        protected static Panmage still = null;
        protected static Panmage whirlStart1 = null;
        protected static Panmage whirlStart2 = null;
        protected static Panmage whirl1 = null;
        protected static Panmage whirl2 = null;
        protected static Panmage whirl3 = null;
        protected static Panmage launchStart = null;
        protected static Panmage launch1 = null;
        protected static Panmage launch2 = null;
        protected static Panmage spinStart1 = null;
        protected static Panmage spinStart2 = null;
        protected static Panmage spinStart3 = null;
        protected static Panmage spin1 = null;
        protected static Panmage spin2 = null;
        protected static Panmage spin3 = null;
        private long age = 0;
        
        protected CycloneBot(final int x, final int y) {
            super(CYCLONE_OFF_X, CYCLONE_H, x, y);
        }
        
        @Override
        protected final int getInitialOffsetX() {
            return 0;
        }
        
        @Override
        protected final boolean onWaiting() {
            age++;
            if (age < 10) {
                return false;
            } else if (age < 12) {
                changeView(getWhirlStart1());
                return false;
            } else if (age < 14) {
                changeView(getWhirlStart2());
                return false;
            } else if (state == STATE_STILL) {
                final long i = age % 6;
                final Panmage img;
                if (i < 2) {
                    img = getWhirl1();
                } else if (i < 4) {
                    img = getWhirl2();
                } else {
                    img = getWhirl3();
                }
                changeView(img);
            } else if (state == STATE_LAUNCH) {
                final int index = WAIT_LAUNCH - waitTimer;
                if (index == 1) {
                    new Whirlwind(this);
                } else if (index > 1) {
                    changeView(((index % 4) < 2) ? getLaunch1() : getLaunch2());
                }
            } else if (state == STATE_LAUNCH_END) {
                if (waitTimer == 2) {
                    changeView(getWhirlStart1());
                } else if (waitTimer == 0) {
                    age = 0;
                    startStill();
                }
            } else if (state == STATE_SPIN) {
                final int index = WAIT_SPIN - waitTimer;
                final Panmage img;
                if ((index < 2) || (waitTimer < 2)) {
                    img = getSpinStart1();
                } else if ((index < 4) || (waitTimer < 4)) {
                    img = getSpinStart2();
                } else if ((index < 6) || (waitTimer < 6)) {
                    img = getSpinStart3();
                } else {
                    final int i = index % 6;
                    if (i < 2) {
                        img = getSpin1();
                    } else if (i < 4) {
                        img = getSpin2();
                    } else {
                        img = getSpin3();
                    }
                    final int vi = (index - 6) % 48;
                    final int v;
                    if (vi < 8) {
                        v = 1;
                    } else if (vi < 16) {
                        v = 2;
                    } else if (vi < 24) {
                        v = 1;
                    } else if (vi < 32) {
                        v = -1;
                    } else if (vi < 40) {
                        v = -2;
                    } else {
                        v = -1;
                    }
                    getPosition().add(2 * getMirrorMultiplier(), ((index < 150) || (v < 0)) ? v : 0);
                }
                changeView(img);
                return true;
            }
            return false;
        }

        @Override
        protected final boolean pickState() {
            if (Mathtil.rand()) {
                startLaunch();
            } else {
                startSpin();
            }
            return false;
        }

        @Override
        protected final boolean continueState() {
            if (state == STATE_LAUNCH) {
                startLaunchEnd();
            } else if (state == STATE_SPIN) {
                getPosition().addX(getMirrorMultiplier());
                setMirror(!isMirror());
                startStill();
            } else {
                startStill();
            }
            return false;
        }
        
        private final void startLaunch() {
            startState(STATE_LAUNCH, WAIT_LAUNCH, getLaunchStart());
        }
        
        private final void startLaunchEnd() {
            startState(STATE_LAUNCH_END, 4, getWhirlStart2());
        }
        
        private final void startSpin() {
            startState(STATE_SPIN, WAIT_SPIN, getSpinStart1());
        }

        @Override
        protected final Panmage getStill() {
            return (still = getCycloneImage(still, "cyclonebot/CycloneBot"));
        }
        
        protected final static Panmage getWhirl1() {
            return (whirl1 = getCycloneImage(whirl1, "cyclonebot/CycloneBotWhirl1"));
        }
        
        protected final static Panmage getWhirl2() {
            return (whirl2 = getCycloneImage(whirl2, "cyclonebot/CycloneBotWhirl2"));
        }
        
        protected final static Panmage getWhirl3() {
            return (whirl3 = getCycloneImage(whirl3, "cyclonebot/CycloneBotWhirl3"));
        }
        
        protected final static Panmage getWhirlStart1() {
            return (whirlStart1 = getCycloneImage(whirlStart1, "cyclonebot/CycloneBotWhirlStart1"));
        }
        
        protected final static Panmage getWhirlStart2() {
            return (whirlStart2 = getCycloneImage(whirlStart2, "cyclonebot/CycloneBotWhirlStart2"));
        }
        
        protected final static Panmage getLaunch1() {
            return (launch1 = getCycloneImage(launch1, "cyclonebot/CycloneBotLaunch1"));
        }
        
        protected final static Panmage getLaunch2() {
            return (launch2 = getCycloneImage(launch2, "cyclonebot/CycloneBotLaunch2"));
        }
        
        protected final static Panmage getLaunchStart() {
            return (launchStart = getCycloneImage(launchStart, "cyclonebot/CycloneBotLaunchStart"));
        }
        
        protected final static Panmage getSpin1() {
            return (spin1 = getCycloneSpinImage(spin1, "cyclonebot/CycloneBotSpin1"));
        }
        
        protected final static Panmage getSpin2() {
            return (spin2 = getCycloneSpinImage(spin2, "cyclonebot/CycloneBotSpin2"));
        }
        
        protected final static Panmage getSpin3() {
            return (spin3 = getCycloneSpinImage(spin3, "cyclonebot/CycloneBotSpin3"));
        }
        
        protected final static Panmage getSpinStart1() {
            return (spinStart1 = getCycloneImage(spinStart1, "cyclonebot/CycloneBotSpinStart1"));
        }
        
        protected final static Panmage getSpinStart2() {
            return (spinStart2 = getCycloneSpinStartImage(spinStart2, "cyclonebot/CycloneBotSpinStart2"));
        }
        
        protected final static Panmage getSpinStart3() {
            return (spinStart3 = getCycloneSpinStartImage(spinStart3, "cyclonebot/CycloneBotSpinStart3"));
        }
        
        protected final static Panmage getCycloneImage(final Panmage img, final String name) {
            return getImage(img, name, CYCLONE_O, CYCLONE_MIN, CYCLONE_MAX);
        }
        
        protected final static Panmage getCycloneSpinImage(final Panmage img, final String name) {
            return getImage(img, name, CYCLONE_SPIN_O, CYCLONE_SPIN_MIN, CYCLONE_SPIN_MAX);
        }
        
        protected final static Panmage getCycloneSpinStartImage(final Panmage img, final String name) {
            return getImage(img, name, CYCLONE_SPIN_O, CYCLONE_MIN, CYCLONE_MAX);
        }
    }
    
    protected final static class Whirlwind extends TimedEnemyProjectile {
        protected final static int duration = 300;
        protected final static int speed = 2;
        protected static Panmage wind1 = null;
        protected static Panmage wind2 = null;
        protected static Panmage wind3 = null;
        
        protected Whirlwind(final Panctor src) {
            super(getWind(duration), src, -2, 20, speed * src.getMirrorMultiplier(), 6, gTuple, duration);
        }
        
        @Override
        public void onStep(final StepEvent event) {
            super.onStep(event);
            final boolean m = isMirror();
            final Panple pos = getPosition();
            final float x = pos.getX();
            boolean reverse = false;
            if (m) {
                if (x < 20) {
                    reverse = true;
                }
            } else {
                if (x >= (Pangine.getEngine().getEffectiveWidth() - 20)) {
                    reverse = true;
                }
            }
            if (reverse) {
                setMirror(!m);
                getVelocity().setX(speed * getMirrorMultiplier());
            }
            if (pos.getY() < 52) {
                getVelocity().setY(4);
            }
            changeView(getWind(timer));
        }
        
        @Override
        protected final void onExpire() {
            Player.puff(this, 0, 0);
        }
        
        protected final static Panmage getWind(final int timer) {
            final int m = timer % 6;
            if (m < 2) {
                return getWind1();
            } else if (m < 4) {
                return getWind2();
            } else {
                return getWind3();
            }
        }
        
        protected final static Panmage getWind1() {
            return (wind1 = getWindImage(wind1, "cyclonebot/Whirlwind1"));
        }
        
        protected final static Panmage getWind2() {
            return (wind2 = getWindImage(wind2, "cyclonebot/Whirlwind2"));
        }
        
        protected final static Panmage getWind3() {
            return (wind3 = getWindImage(wind3, "cyclonebot/Whirlwind3"));
        }
        
        private final static Panmage getWindImage(final Panmage img, final String name) {
            if (img != null) {
                return img;
            }
            return Boss.getImage(img, name, BotsnBoltsGame.fireballEnemy[0]);
        }
    }
    
    protected final static int FLOOD_OFF_X = 6, FLOOD_H = 24; //TODO
    protected final static Panple FLOOD_O = new FinPanple2(14, 1);
    protected final static Panple FLOOD_MIN = getMin(FLOOD_OFF_X);
    protected final static Panple FLOOD_MAX = getMax(FLOOD_OFF_X, FLOOD_H);
    
    protected final static class FloodBot extends Boss {
        protected final static byte STATE_FILL = 1;
        protected final static byte STATE_RAISE = 2;
        protected final static int FILL_FRAME_DURATION = 3;
        protected final static int WAIT_FILL = 4 * FILL_FRAME_DURATION;
        protected final static int RAISE_FRAMES = 28;
        protected final static int RAISE_FRAME_DURATION = 3;
        protected final static int WAIT_RAISE = RAISE_FRAMES * RAISE_FRAME_DURATION;
        protected static Panmage still = null;
        protected static Panmage start1 = null;
        protected static Panmage start2 = null;
        protected static Panmage start3 = null;
        private final Valve valve;
        private boolean fillNeeded = true; // Called after super constructor
        private Tile flowTile = null;
        private Tile brickTile = null;
        
        protected FloodBot(final int x, final int y) {
            super(FLOOD_OFF_X, FLOOD_H, x, y);
            valve = new Valve(this);
        }
        
        @Override
        protected final void init() {
            this.fillNeeded = true; // Called by super constructor
        }
        
        @Override
        protected final boolean onWaiting() {
            if (state == STATE_RAISE) {
                onRaising();
            } else if (state == STATE_FILL) {
                onFilling();
            }
            return false;
        }
        
        protected final void onFilling() {
            final int index = (WAIT_FILL - waitTimer - 1) / FILL_FRAME_DURATION;
            if (index < 1) {
                changeView(getStart1());
            } else if (index < 2) {
                changeView(getStart2());
            } else if (index < 3) {
                changeView(getStart3());
            } else {
                changeView(getStillNormal());
            }
        }
        
        protected final void onRaising() {
            final int temp = WAIT_RAISE - waitTimer - 1;
            if ((temp % RAISE_FRAME_DURATION) != 0) {
                return;
            }
            final int index = temp / RAISE_FRAME_DURATION;
            if (index < 10) {
                setTiles(index, 0, getFlowTile());
            } else if (index < 18) {
                if (((index % 2) == 0) && ((index < 16) || (RoomLoader.getWaterTile() < 6))) {
                    RoomLoader.raiseWaterTile();
                    if (RoomLoader.getWaterTile() == 12) {
                        valve.destroy();
                    }
                }
            } else if (index < RAISE_FRAMES) {
                setTiles(index, 18, brickTile);
            }
        }
        
        private final Tile getFlowTile() {
            if (flowTile == null) {
                flowTile = RoomLoader.getAnimator(BotsnBoltsGame.imgMap[0][3], true).tile;
            }
            return flowTile;
        }
        
        private final void setTiles(final int index, final int timerOffset, final Tile tile) {
            final int y = 11 - (index - timerOffset);
            if (y < RoomLoader.getWaterTile()) {
                return;
            }
            final TileMap tm = BotsnBoltsGame.tm;
            final int tileIndex = tm.getIndex(3, y);
            if (brickTile == null) {
                brickTile = tm.getTile(tileIndex);
            }
            tm.setTile(tileIndex, tile);
            tm.setTile(4, y, tile);
            tm.setTile(19, y, tile);
            tm.setTile(20, y, tile);
        }

        @Override
        protected final boolean pickState() {
            if (fillNeeded) {
                startFill();
                fillNeeded = false;
            } else if (RoomLoader.getWaterTile() < 12) {
                startRaise();
            }
            return false;
        }

        @Override
        protected final boolean continueState() {
            startStill();
            return false;
        }
        
        protected final void startFill() {
            startState(STATE_FILL, WAIT_FILL, getStart1());
        }
        
        protected final void startRaise() {
            startState(STATE_RAISE, WAIT_RAISE, getStill());
        }

        @Override
        protected final Panmage getStill() {
            return fillNeeded ? getStart1() : getStillNormal();
        }
        
        protected final static Panmage getStillNormal() {
            return (still = getFloodImage(still, "floodbot/FloodBot"));
        }
        
        protected final static Panmage getStart1() {
            return (start1 = getFloodImage(start1, "floodbot/FloodBotStart1"));
        }
        
        protected final static Panmage getStart2() {
            return (start2 = getFloodImage(start2, "floodbot/FloodBotStart2"));
        }
        
        protected final static Panmage getStart3() {
            return (start3 = getFloodImage(start3, "floodbot/FloodBotStart3"));
        }
        
        protected final static Panmage getFloodImage(final Panmage img, final String name) {
            return getImage(img, name, FLOOD_O, FLOOD_MIN, FLOOD_MAX);
        }
    }
    
    protected final static class Valve extends Panctor {
        private static Panmage image = null;
        
        protected Valve(final FloodBot src) {
            setView(getValveImage());
            getPosition().set(184, 168, BotsnBoltsGame.DEPTH_BETWEEN);
            BotsnBoltsGame.tm.getLayer().addActor(this);
        }
        
        protected final static Panmage getValveImage() {
            return (image = getImage(image, "floodbot/Valve", null, null, null));
        }
    }
    
    protected abstract static class Rotator {
        private final static int numFrames = 8;
        private final int frameDuration;
        private int frameIndex = 0;
        private int frameTimer = 0;
        
        protected Rotator(final int frameDuration) {
            this.frameDuration = frameDuration;
        }
        
        protected void init() {
            frameIndex = 0;
            frameTimer = 0;
        }
        
        protected final void onStep(final Panctor actor, final Panframe[] frames) {
            frameTimer++;
            if (frameTimer >= frameDuration) {
                frameTimer = 0;
                frameIndex++;
                if (frameIndex >= numFrames) {
                    frameIndex = 0;
                }
                actor.setView(getFrame(frames));
            }
        }
        
        protected Panframe getFrame(final Panframe[] frames) {
            return Rock.getFrame(frames, this);
        }
        
        protected int getDim(final Panmage img) {
            return Math.round(img.getSize().getX());
        }
        
        protected abstract Panmage getImage1();
        
        protected abstract Panmage getImage2();
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
