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

import org.pandcorps.botsnbolts.Chr.*;
import org.pandcorps.botsnbolts.Extra.*;
import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.botsnbolts.PowerUp.*;
import org.pandcorps.botsnbolts.Profile.*;
import org.pandcorps.core.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.boundary.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.visual.*;

public abstract class Boss extends Enemy implements SpecBoss {
    protected final static String RES_BOSS = BotsnBoltsGame.RES + "boss/";
    protected final static byte STATE_STILL = 0;
    private final static int DAMAGE = 4;
    private final static float DROP_X = 192;
    private final static byte TAUNT_NEEDED = 0;
    private final static byte TAUNT_STARTED = 1;
    private final static byte TAUNT_WAITING = 2;
    private final static byte TAUNT_FINISHED = 3;
    private final static int WAIT_INDEFINITE = Integer.MAX_VALUE;
    
    private boolean initializationNeeded = true;
    protected int waitTimer = 0; // Will be assigned by startStillBeforeTaunt()
    protected byte state = 0;
    protected Queue<Jump> pendingJumps = null;
    private boolean jumping = false;
    protected byte tauntState = TAUNT_NEEDED;
    protected int moves = -1;
    protected static boolean clipping = true;
    private static boolean delaying = false;
    protected static boolean dropping = false;
    protected HudMeter healthMeter = null;
    private static int prevRand = -1;
    private static int prevPrevRand = -1;
    private final boolean launchPossible;
    protected static AiBoss aiBoss = null;
    
    protected Boss(final int offX, final int h, final Segment seg) {
        super(offX, h, seg, 0);
        construct();
        launchPossible = seg.getBoolean(3, true);
    }
    
    protected Boss(final int offX, final int h, final int x, final int y) {
        super(offX, h, x, y, 0);
        construct();
        launchPossible = true;
    }
    
    private final void construct() {
        if (!isConstructNeeded()) {
            return;
        }
        init();
        startStillBeforeTaunt();
        setMirror(true);
        if (isDropNeeded()) {
            getPosition().setY(getDropY());
            clipping = false;
            delaying = true;
            dropping = true;
            setVisible(false);
        } else {
            clipping = true;
            delaying = false;
            dropping = false;
        }
    }
    
    protected boolean isConstructNeeded() {
        return true;
    }
    
    private final static int getDropY() {
        return Pangine.getEngine().getEffectiveHeight() - 1;
    }
    
    protected void init() {
    }
    
    private final void onFirstStep() {
        if (isHealthMeterNeeded() && isDuringGameplay()) {
            healthMeter = addHealthMeter();
        }
    }
    
    protected final static boolean isDuringGameplay() {
        return Panscreen.get() instanceof BotsnBoltsGame.BotsnBoltsScreen;
    }
    
    protected boolean isHealthMeterNeeded() {
        return true;
    }
    
    protected boolean isDropNeeded() {
        return true;
    }
    
    @Override
    protected final int getSolid(final int off) {
        return getSolid(this, off);
    }
    
    protected final static int getSolid(final Chr chr, final int off) {
        final int s = chr.getIdentitySolid(off);
        if (clipping) {
            return s;
        } else if ((s == -1) && (chr.getPosition().getY() < (Pangine.getEngine().getEffectiveHeight() - 48))) {
            clipping = true;
        }
        return -1;
    }
    
    @Override
    protected final boolean onStepCustom() {
        if (delaying) {
            if (RoomLoader.isBossDoorClosing()) {
                return true;
            } else if (isConstructNeeded()) {
                delaying = false;
                setVisible(true);
            }
        }
        if (initializationNeeded) {
            onFirstStep();
            initializationNeeded = false;
        }
        if (dropping) {
            if (clipping && isGrounded()) {
                dropping = false;
                setPlayerActive(false); // Will activate Player after health bar fills
            } else {
                return false;
            }
        }
        if (onStepBoss()) {
            return false;
        } else if (waitTimer > 0) {
            waitTimer--;
            return onWaiting();
        } else if (isStill()) {
            if (taunting()) {
                return false;
            } else if (pollPendingJumps()) {
                return false;
            }
            moves++;
            return pickState();
        }
        return continueState();
    }
    
    private final boolean taunting() {
        if (tauntState == TAUNT_FINISHED) {
            return false;
        } else if (tauntState == TAUNT_NEEDED) {
            tauntState = TAUNT_STARTED;
            taunt();
        } else if (tauntState == TAUNT_STARTED) { // Called when picking state after taunt done (if sub-class doesn't call finishTaunt directly)
            finishTaunt();
        } else if (tauntState != TAUNT_WAITING) {
            throw new IllegalStateException("Unexpected tauntState " + tauntState);
        }
        return true;
    }
    
    protected void taunt() {
        waitTimer = 0;
    }
    
    protected final boolean isTauntFinished() {
        return tauntState == TAUNT_FINISHED;
    }
    
    protected final boolean finishTaunt() {
        if (tauntState == TAUNT_FINISHED) {
            return false;
        }
        health = HudMeter.MAX_VALUE;
        tauntState = TAUNT_WAITING; // TAUNT_FINISHED will be set when health bar is full
        return true;
    }
    
    @Override
    public final void onHealthMaxDisplayReached() {
        tauntState = TAUNT_FINISHED;
        waitTimer = 0;
        setPlayerActive(true);
    }
    
    protected boolean isStill() {
        return state == STATE_STILL;
    }
    
    protected boolean onStepBoss() {
        return false;
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
    protected PowerUp pickAward(final Player player) {
        return new VictoryDisk(player, this);
    }
    
    @Override
    public void onAward(final Player player) {
        onAwardBoss(this, player);
    }
    
    protected final static void onAwardBoss(final SpecBoss boss, final Player player) {
        final String launchReturnX = boss.isLaunchPossible() ? RoomLoader.levelVariables.get(Extra.VAR_LAUNCH_RETURN_ROOM_X) : null;
        if (launchReturnX == null) {
            player.dematerialize(Player.levelSelectHandler);
        } else {
            player.launch(Integer.parseInt(launchReturnX), Integer.parseInt(RoomLoader.levelVariables.get(Extra.VAR_LAUNCH_RETURN_ROOM_Y)));
        }
    }
    
    @Override
    public final boolean isLaunchPossible() {
        return launchPossible;
    }

    @Override
    protected final void award(final PowerUp powerUp) {
        award(powerUp, getDropX());
    }
    
    protected final static void award(final PowerUp powerUp, final float dropX) {
        PowerUp.addPowerUp(powerUp, dropX, getDropY(), 0);
    }
    
    protected float getDropX() {
        return DROP_X;
    }
    
    @Override
    protected int getDamage() {
        return DAMAGE;
    }
    
    protected final static int rand(final int max) {
        if (max <= 1) {
            throw new IllegalArgumentException("Called rand(" + max + "), input must be at least 2");
        }
        while (true) {
            final int r = (Mathtil.randi(0, (max * 1000) - 1)) / 1000;
            if ((r != prevRand) || (r != prevPrevRand)) {
                prevPrevRand = prevRand;
                prevRand = r;
                return r;
            }
        }
    }
    
    protected final static <E> E rand(final List<E> list) {
        return list.get(rand(list.size()));
    }
    
    protected final void startState(final byte state, final int waitTimer, final Panmage img) {
        this.state = state;
        this.waitTimer = waitTimer;
        setView(img);
    }
    
    protected final void startStateIndefinite(final byte state, final Panmage img) {
        startState(state, WAIT_INDEFINITE, img);
    }
    
    protected final void startJump(final Jump jump) {
        startJump(jump.state, jump.img, jump.v, jump.hv);
    }
    
    protected final void startJump(final byte state, final Panmage img, final float v, final int hv) {
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
    
    protected final void addPendingJump(final byte state, final Panmage img, final float v, final int hv) {
        addPendingJump(new Jump(state, img, v, hv));
    }
    
    protected boolean hasPendingJumps() {
        return hasPendingJumpsDefault();
    }
    
    private final boolean hasPendingJumpsDefault() {
        return Coltil.isValued(pendingJumps);
    }
    
    protected boolean hasPendingOrContinuedJumps() {
        if (hasPendingJumpsDefault()) {
            return true;
        } else if (state != getStateJumps()) {
            return false;
        }
        final float x = getPosition().getX(), px = getPlayerX();
        if (isMirror()) {
            if (x > (px + 4)) {
                continueJumps();
                return true;
            }
        } else if (x < (px - 4)) {
            continueJumps();
            return true;
        }
        return false;
    }
    
    protected final void startJumps() {
        startJump(getStateJumps(), getJump(), getJumpsV(), getJumpsH());
    }
    
    protected final void continueJumps() {
        addPendingJump(getStateJumps(), getJump(), getJumpsV(), getJumpsH());
    }
    
    protected final int getJumpsH() {
        return getJumpsHv() * getMirrorMultiplier();
    }
    
    //@OverrideMe
    protected byte getStateJumps() {
        return 0;
    }
    
    //@OverrideMe
    protected Panmage getJump() {
        return null;
    }
    
    //@OverrideMe
    protected float getJumpsV() {
        return 0;
    }
    
    //@OverrideMe
    protected int getJumpsHv() {
        return 0;
    }
    
    @Override
    protected final void onGrounded() {
        if (jumping) {
            hv = 0;
            jumping = false; // Clear right away in case below methods start a new jump for a sub-class like VolcanoBot
            onJumpLanded();
            if (isTurnTowardPlayerNeeded()) {
                turnTowardPlayer(); // Don't do in onLanded; hv still needed at that point, which overrides this
            }
        }
    }
    
    protected void onJumpLanded() {
    }
    
    protected boolean isTurnTowardPlayerNeeded() {
        return !hasPendingJumps();
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
    
    protected final int getX() {
        return Math.round(getPosition().getX());
    }
    
    protected final int getMirroredX(final int x) {
        return BotsnBoltsGame.GAME_W - x - 1;
    }
    
    protected final boolean addBoundedX(final int xLeft, final int xRight) {
        addX(hv);
        final Panple pos = getPosition();
        final int x = Math.round(pos.getX());
        if (hv < 0) {
            if (x <= xLeft) {
                pos.setX(xLeft);
                return false;
            }
        } else if (x >= xRight) {
            pos.setX(xRight);
            return false;
        }
        return true;
    }
    
    protected final static int initStillTimer() {
        return Mathtil.randi(15, 30);
    }
    
    protected void startStill() {
        startStill(initStillTimer());
    }
    
    protected void startStillBeforeTaunt() {
        startStill(15);
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
    
    @Override
    protected void onHurt(final Projectile prj) {
        final int oldHealth = health;
        super.onHurt(prj);
        if (state == STATE_STILL) {
            final int damage = oldHealth - health;
            if (damage > 0) {
                waitTimer = adjustWaitTimerOnHurt(waitTimer, damage);
            }
        }
    }
    
    protected final static int adjustWaitTimerOnHurt(final int waitTimer, final int damage) {
        return Math.max(1, waitTimer - (damage * 10));
    }
    
    @Override
    public final void onDefeat() {
        onBossDefeat();
        if (isDefeatOrbNeeded()) {
            Player.defeatOrbs(this, BotsnBoltsGame.defeatOrbBoss);
        }
        if (isDestroyEnemiesNeeded()) {
            destroyEnemies();
        }
        RoomLoader.levelVariables.put(getClass().getSimpleName(), "");
    }
    
    protected void onBossDefeat() {
    }
    
    protected boolean isDefeatOrbNeeded() {
        return true;
    }
    
    protected boolean isDestroyEnemiesNeeded() {
        // Might eventually have player fight two bosses simultaneously; might want to return !isOtherBossPresent() then
        return true;
    }
    
    protected final boolean isOtherBossPresent() {
        for (final Panctor actor : getActors()) {
            if ((actor != this) && (actor instanceof Boss)) {
                return true;
            }
        }
        return false;
    }
    
    protected final static void destroyEnemies() {
        for (final Panctor actor : getActors()) {
            if (((actor instanceof Enemy) && !(actor instanceof Boss)) || (actor instanceof EnemyProjectile) || (actor instanceof EnemySpawner)
                    || (actor instanceof AiBomb) || (actor instanceof Flare)) {
                actor.destroy();
            }
        }
    }
    
    protected final static Player getPlayer() {
        return PlayerContext.getPlayer(getPlayerContext());
    }
    
    protected final static void setPlayerActive(final boolean active) {
        final Player player = getPlayer();
        if (player != null) {
            player.active = active;
            if (!active) {
                player.prepareForScript();
            }
        }
    }
    
    protected final static float getPlayerX() {
        final Player player = getPlayer();
        return (player == null) ? 192 : player.getPosition().getX();
    }
    
    protected final static void clearPlayerExtras() {
        final Player player = getPlayer();
        if (player != null) {
            Panctor.destroy(player.spring);
        }
    }
    
    protected final static void addActor(final Panctor actor) {
        getLayerRequired().addActor(actor);
    }
    
    protected final static Panlayer getLayerRequired() {
        return BotsnBoltsGame.getLayer();
    }
    
    protected final static Set<Panctor> getActors() {
        return getLayerRequired().getActors();
    }
    
    protected static class Fort extends Boss {
        private static Panmage still = null;
        private int spawnTimer = 0;
        private CyanEnemy bot = null;
        
        protected Fort(final Segment seg) {
            super(0, 0, seg);
            setView(getStill());
            setMirror(false);
        }
        
        @Override
        protected final int getInitialOffsetX() {
            return 0;
        }
        
        @Override
        protected final boolean isHealthMeterNeeded() {
            return false;
        }
        
        @Override
        protected final boolean isDropNeeded() {
            return false;
        }
        
        @Override
        protected final void onBossDefeat() {
            Panctor.destroy(bot);
            burst(10);
        }
        
        private final void burst(final int n) {
            final Panctor src = BotsnBoltsGame.tm;
            EnemyProjectile.burstEnemy(src, Mathtil.randi(342, 382), Mathtil.randi(56, 184));
            if (n > 1) {
                Pangine.getEngine().addTimer(src, 3, new TimerListener() {
                    @Override public final void onTimer(final TimerEvent event) {
                        burst(n - 1);
                    }});
            }
        }
        
        @Override
        protected final boolean isDefeatOrbNeeded() {
            return false;
        }
        
        @Override
        public final void onAward(final Player player) {
            player.startScript(new LeftAi(32.0f), new Runnable() {
                @Override public final void run() {
                    new Warp(getNextBoss(20, 3));
                }});
        }
        
        protected AiBoss getNextBoss(final int x, final int y) {
            return new Volatile(x, y);
        }
        
        @Override
        protected final boolean onWaiting() {
            stepFort();
            return true;
        }
        
        @Override
        protected final boolean pickState() {
            stepFort();
            return true;
        }

        @Override
        protected final boolean continueState() {
            stepFort();
            return true;
        }
        
        private final void stepFort() {
            if (Panctor.isDestroyed(bot)) {
                spawnTimer++;
                if (spawnTimer >= 60) {
                    spawn();
                }
            }
        }
        
        private final void spawn() {
            if (health < 1) {
                return;
            }
            spawnTimer = 0;
            bot = new CyanEnemy(22, 14);
            bot.getPosition().addX(-3);
            BotsnBoltsGame.tm.getLayer().addActor(bot);
        }

        @Override
        protected final Panmage getStill() {
            if (still != null) {
                return still;
            }
            return (still = getImage(still, "fort/FortPower", null, new FinPanple2(5, 5), new FinPanple2(26, 30)));
        }
    }
    
    protected final static class Fort2 extends Fort {
        protected Fort2(final Segment seg) {
            super(seg);
        }
        
        @Override
        protected final AiBoss getNextBoss(final int x, final int y) {
            return new Volatile2(x, y);
        }
    }
    
    protected final static int VOLCANO_OFF_X = 20, VOLCANO_H = 40;
    protected final static Panple VOLCANO_O = new FinPanple2(26, 1);
    protected final static Panple VOLCANO_MIN = getMin(VOLCANO_OFF_X);
    protected final static Panple VOLCANO_MAX = getMax(VOLCANO_OFF_X, VOLCANO_H);
    
    protected final static class VolcanoBot extends Boss {
        protected final static byte STATE_LIFT = 1;
        protected final static byte STATE_RAISED = 2;
        protected final static byte STATE_CROUCH = 3;
        protected final static byte STATE_JUMP = 4;
        protected final static byte STATE_JUMP_DIVE = 5;
        protected final static byte STATE_WAIT_DIVE = 6;
        protected final static byte STATE_DIVE = 7;
        private final static int SPEED_JUMP_Y = 10;
        private final static int NUM_DIVES = 4;
        protected static Panmage still = null;
        protected static Panmage lift = null;
        protected static Panmage raised = null;
        protected static Panmage crouch = null;
        protected static Panmage jump = null;
        protected static Panmage dive = null;
        protected final static Panmage[] burns = new Panmage[2];
        
        protected float targetX = -1;
        private int pendingDives = 0;
        
        protected VolcanoBot(final Segment seg) {
            super(VOLCANO_OFF_X, VOLCANO_H, seg);
        }
        
        @Override
        protected final int getInitialOffsetX() {
            return 0;
        }
        
        @Override
        protected final void taunt() {
            startLift();
        }
        
        @Override
        protected final int getDamage() {
            return (state == STATE_DIVE) ? 6 : super.getDamage();
        }
        
        @Override
        protected final boolean onWaiting() {
            if (state == STATE_CROUCH) {
                if (!isTauntFinished()) {
                    return false;
                }
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
            } else if (state == STATE_DIVE) {
                changeView(getBurn());
            } else if (state == STATE_WAIT_DIVE) {
                return true;
            } else if ((state == STATE_JUMP_DIVE) && (v < g)) {
                startWaitDive();
            }
            return false;
        }
        
        @Override
        protected final boolean pickState() {
            final int r = rand(3);
            if (r == 0) {
                startLift();
            } else if (r == 1) {
                startJumpDive();
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
                case STATE_WAIT_DIVE :
                    startDive();
                    break;
                default :
                    throw new IllegalStateException("Unexpected state " + state);
            }
            return false;
        }
        
        @Override
        protected final boolean isTurnTowardPlayerNeeded() {
            if (pendingDives > 0) {
                pendingDives--;
                hv = 0;
                if (pendingDives > 0) {
                    if (pendingDives == (NUM_DIVES / 2)) {
                        setMirror(!isMirror());
                    }
                    startJumpDive();
                    return false;
                }
            }
            return true;
        }
        
        protected final void startLift() {
            startState(STATE_LIFT, 5, getLift());
        }
        
        protected final void startJump() {
            final Panmage img = getJump();
            final int dir = getDirection();
            if (Mathtil.rand()) {
                startJump(STATE_JUMP, img, SPEED_JUMP_Y, dir * 9);
            } else {
                startJump(STATE_JUMP, img, SPEED_JUMP_Y, dir * 7);
                addPendingJump(STATE_JUMP, img, SPEED_JUMP_Y, -dir * 5);
                addPendingJump(STATE_JUMP, img, SPEED_JUMP_Y, dir * 7);
            }
        }
        
        protected final void startJumpDive() {
            if (pendingDives == 0) {
                pendingDives = NUM_DIVES;
            }
            startJump(STATE_JUMP_DIVE, getJump(), SPEED_JUMP_Y, getVelDiveX());
        }
        
        private final int getVelDiveX() {
            return getMirrorMultiplier() * 4;
        }
        
        protected final void startWaitDive() {
            v = 0;
            startState(STATE_WAIT_DIVE, 5, getDive());
        }
        
        protected final void startDive() {
            hv = getVelDiveX();
            startStateIndefinite(STATE_DIVE, getBurn());
        }
        
        protected final void startRaised() {
            startState(STATE_RAISED, 10, getRaised());
        }
        
        protected final void startCrouch() {
            targetX = -1;
            startState(STATE_CROUCH, finishTaunt() ? WAIT_INDEFINITE : 50, getCrouch());
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
        
        @Override
        protected final Panmage getJump() {
            return (jump = getVolcanoImage(jump, "volcanobot/VolcanoBotJump"));
        }
        
        protected final static Panmage getDive() {
            return (dive = getVolcanoImage(dive, "volcanobot/VolcanoBotDive"));
        }
        
        protected final static Panmage getBurn() {
            return getBurn(((int) (Pangine.getEngine().getClock() % 6)) / 3);
        }
        
        protected final static Panmage getBurn(final int i) {
            Panmage img = burns[i];
            if (img == null) {
                img = getVolcanoImage(img, "volcanobot/VolcanoBotBurn" + (i + 1));
                burns[i] = img;
            }
            return img;
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
        public final void onStep(final StepEvent event) {
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
        public final void onAllOob(final AllOobEvent event) {
            if (getPosition().getY() < 0) {
                super.onAllOob(event);
            }
        }
        
        @Override
        protected final void onOutOfView() {
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
        
        protected HailBot(final Segment seg) {
            super(HAIL_OFF_X, HAIL_H, seg);
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
        
        @Override
        protected final Panmage getJump() {
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
        
        @Override
        protected final int getDamage() {
            return 4;
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
            return new EnemyProjectile(getChunk(), this, 0, 0, vx + randVel(), vy + randVel(), gTuple) {
                @Override protected final int getDamage() {
                    return 2;
                }
            };
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
        protected final static byte STATE_SHOOT_HORIZONTAL = 2;
        protected final static byte STATE_CROUCH = 3;
        protected final static byte STATE_CURL = 4;
        protected final static byte STATE_ROLL = 5;
        protected final static byte STATE_JUMP = 6;
        protected final static int WAIT_SHOOT = 30;
        protected static Panmage still = null;
        protected static Panmage aim = null;
        protected static Panmage aimHorizontal = null;
        protected static Panmage crouch = null;
        protected static Panmage curl = null;
        protected static Panmage roll1 = null;
        protected static Panmage roll2 = null;
        protected static Panmage jump = null;
        private final static Rotator rots = new RollRotator();
        private final static Panframe[] frames = new Panframe[Rotator.numFrames];
        
        protected RockslideBot(final Segment seg) {
            super(ROCKSLIDE_OFF_X, ROCKSLIDE_H, seg);
        }
        
        @Override
        protected final boolean onWaiting() {
            if (state == STATE_SHOOT) {
                if (waitTimer == (WAIT_SHOOT - 1)) {
                    new Rock(this, 36, 3);
                }
            } else if (state == STATE_SHOOT_HORIZONTAL) {
                if (waitTimer == (WAIT_SHOOT - 1)) {
                    new RockHorizontal(this, 45, 28);
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
                final int r = Mathtil.randi(0, 3499);
                if (r < 1000) {
                    startShoot();
                } else if (r < 2000) {
                    startShootHorizontal();
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
                case STATE_SHOOT_HORIZONTAL :
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
        
        protected final void startShootHorizontal() {
            startState(STATE_SHOOT_HORIZONTAL, WAIT_SHOOT, getAimHorizontal());
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
            if (Mathtil.rand(60)) {
                startJump(STATE_JUMP, img, 11, 3);
            } else {
                startJump(STATE_JUMP, img, 11, 0);
                addPendingJump(STATE_JUMP, img, 11, 3);
            }
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
        
        protected final static Panmage getAimHorizontal() {
            if (aimHorizontal != null) {
                return aimHorizontal;
            }
            return (aimHorizontal = getRockslideImage(aimHorizontal, "rockslidebot/RockslideBotAimHorizontal", new FinPanple2(21, 1)));
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
        
        @Override
        protected final Panmage getJump() {
            return (jump = getRockslideImage(jump, "rockslidebot/RockslideBotJump"));
        }
        
        protected final static Panmage getRockslideImage(final Panmage img, final String name) {
            return getRockslideImage(img, name, ROCKSLIDE_O);
        }
        
        protected final static Panmage getRockslideImage(final Panmage img, final String name, final Panple o) {
            return getImage(img, name, o, ROCKSLIDE_MIN, ROCKSLIDE_MAX);
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
    
    protected static class Rock extends Enemy {
        protected static Panmage rock1 = null;
        protected static Panmage rock2 = null;
        protected static Panmage rockShatter = null;
        private final Rotator rots = new RockRotator(); // Can't be static; keeps state for each Rock on screen
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
        protected boolean onStepCustom() {
            rots.onStep(this, frames);
            return super.onStepCustom();
        }
        
        @Override
        protected final int getDamage() {
            return 2;
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
        
        protected final static Panframe getFrame(final Panframe[] frames, final Rotator rots, final int frameIndex) {
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
    
    protected final static class RockHorizontal extends Rock {
        private boolean flying = true;
        
        protected RockHorizontal(final RockslideBot src, final int ox, final int oy) {
            super(src, ox, oy);
            hv = src.getMirrorMultiplier() * 6;
            v = 0;
        }
        
        @Override
        protected final boolean onStepCustom() {
            if (flying) {
                if (addX(hv) != X_NORMAL) {
                    bounce();
                    return super.onStepCustom();
                }
                return true;
            } else {
                return super.onStepCustom();
            }
        }
        
        protected final void bounce() {
            flying = false;
            setMirrorEnemy(!isMirror());
            hv = 2 * getMirrorMultiplier();
        }
        
        @Override
        protected final void onGrounded() {
            shatter();
        }
    }
    
    protected final static int LIGHTNING_OFF_X = 6, LIGHTNING_H = 24;
    protected final static Panple LIGHTNING_O = new FinPanple2(14, 1);
    protected final static Panple LIGHTNING_MIN = getMin(LIGHTNING_OFF_X);
    protected final static Panple LIGHTNING_MAX = getMax(LIGHTNING_OFF_X, LIGHTNING_H);
    
    protected final static class LightningBot extends Boss {
        protected final static byte STATE_JUMP = 1;
        protected final static byte STATE_STRIKE = 2;
        protected final static byte STATE_BURST = 3;
        protected final static byte STATE_JUMPS = 4;
        protected final static int WAIT_STRIKE = 15;
        protected final static int WAIT_BURST = 31;
        private final static int VEL_JUMPS = 10;
        protected static Panmage still = null;
        protected static Panmage jump = null;
        protected static Panmage strike = null;
        protected static Panmage fall = null;
        protected static Panmage burst = null;
        
        protected LightningBot(final Segment seg) {
            super(LIGHTNING_OFF_X, LIGHTNING_H, seg);
        }
        
        @Override
        protected final boolean onWaiting() {
            if (state == STATE_STRIKE) {
                if (waitTimer == (WAIT_STRIKE - 1)) {
                    new Lightning(this);
                }
                return true;
            } else if (state == STATE_BURST) {
                if (waitTimer == (WAIT_BURST - 1)) {
                    final Panmage burst = LightningBurst.getBurst();
                    LightningBurst.start = Pangine.getEngine().getClock();
                    new LightningBurst(burst, this, -32, 15, 29, 0);
                    new LightningBurst(burst, this, 30, 14, 29, 2);
                    new LightningBurst(burst, this, 0, 44, 29, 3);
                } else if (waitTimer == (WAIT_BURST - 2)) {
                    final Panmage burst2 = LightningBurst.getBurst2();
                    new LightningBurst(burst2, this, -19, 34, 27, 0);
                    new LightningBurst(burst2, this, 18, 31, 27, 3);
                }
            } else if (state == STATE_JUMP) {
                if (v > 0) {
                    if (getPosition().getY() >= 166) {
                        startStrike();
                    }
                } else {
                    checkFall();
                }
            } else if (state == STATE_JUMPS) {
                checkFall();
            }
            return false;
        }
        
        private final boolean checkFall() {
            if (v < 0) {
                changeView(getFall());
                return true;
            }
            return false;
        }

        @Override
        protected final boolean pickState() {
            final int r = rand(3);
            if (r == 0) {
                startJump();
            } else if (r == 1) {
                startBurst();
            } else {
                startJumps();
            }
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
        
        @Override
        protected final boolean hasPendingJumps() {
            return hasPendingOrContinuedJumps();
        }
        
        @Override
        protected final byte getStateJumps() {
            return STATE_JUMPS;
        }
        
        @Override
        protected final float getJumpsV() {
            return VEL_JUMPS;
        }
        
        @Override
        protected final int getJumpsHv() {
            return 3;
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
        
        protected final void startBurst() {
            startState(STATE_BURST, WAIT_BURST, getBurst());
        }

        @Override
        protected final Panmage getStill() {
            return (still = getLightningImage(still, "lightningbot/LightningBot"));
        }
        
        @Override
        protected final Panmage getJump() {
            return (jump = getLightningImage(jump, "lightningbot/LightningBotJump"));
        }
        
        protected final Panmage getStrike() {
            return (strike = getLightningImage(strike, "lightningbot/LightningBotStrike"));
        }
        
        protected final Panmage getFall() {
            return (fall = getLightningImage(fall, "lightningbot/LightningBotFall"));
        }
        
        protected final Panmage getBurst() {
            if (burst != null) {
                return burst;
            }
            return (burst = getLightningImage(burst, "lightningbot/LightningBotBurst", new FinPanple2(LIGHTNING_O.getX() + 2, LIGHTNING_O.getY())));
        }
        
        protected final static Panmage getLightningImage(final Panmage img, final String name) {
            return getLightningImage(img, name, LIGHTNING_O);
        }
        
        protected final static Panmage getLightningImage(final Panmage img, final String name, final Panple o) {
            return getImage(img, name, o, LIGHTNING_MIN, LIGHTNING_MAX);
        }
    }
    
    protected final static class LightningBurst extends TimedEnemyProjectile {
        private static Panmage burst = null;
        private static Panmage burst2 = null;
        private static long start = 0;
        
        protected LightningBurst(final Panmage img, final LightningBot src, final int ox, final int oy, final int timer, final int rot) {
            super(img, src, ox, oy, timer);
            setRot(rot);
        }
        
        @Override
        protected final int getDamage() {
            return 4;
        }
        
        @Override
        protected final boolean isDestroyedOnImpact() {
            return false;
        }
        
        @Override
        protected final void onCollisionWithPlayerProjectile(final Projectile prj) {
            prj.burst();
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            super.onStep(event);
            final long age = Pangine.getEngine().getClock() - start;
            final long m = age % 4;
            setVisible(m < 2);
        }
        
        private final static Panmage getBurst() {
            if (burst != null) {
                return burst;
            }
            return (burst = getImage(null, "lightningbot/LightningBurst", BotsnBoltsGame.CENTER_32, new FinPanple2(-14, -14), new FinPanple2(14, 14)));
        }
        
        private final static Panmage getBurst2() {
            if (burst2 != null) {
                return burst2;
            }
            return (burst2 = getImage(null, "lightningbot/LightningBurst2", BotsnBoltsGame.CENTER_16, BotsnBoltsGame.MIN_16, BotsnBoltsGame.MAX_16));
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
        private int jMin;
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
            getPosition().set(x, 0);
            setMirror(false);
            this.src = src;
            this.x = x;
            this.jMax = jMax;
            this.jBase = jBase;
            jMin = jBase;
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
            step();
        }
        
        private final boolean isRoot() {
            return jMax == ROOT_MAX;
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            super.onStep(event);
            step();
        }
        
        private final void step() {
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
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panmage img = getCurrentImage();
            boolean firstFork = true;
            for (int j = jMax; j >= jMin; j--) {
                final int index;
                final boolean mirror;
                boolean fork = false;
                Lightning childLightning = null;
                if ((j == jMax) && isRoot()) {
                    index = getTop();
                    mirror = src.isMirror(); // Top mirror shouldn't be random; based on src
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
        protected final int getDamage() {
            return 4;
        }
        
        @Override
        protected final boolean isDestroyedOnImpact() {
            return false;
        }
        
        @Override
        public final void onAllOob(final AllOobEvent event) {
        }
        
        @Override
        protected final void onOutOfView() {
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
                return jMin * BotsnBoltsGame.DIM;
            }
        }
        
        private final class LightningMaximum extends UnmodPanple2 {
            @Override
            public final float getX() {
                return 14;
            }

            @Override
            public final float getY() {
                return jMax * BotsnBoltsGame.DIM;
            }
        }
        
        private final void renderIndex(final Panderer renderer, final int x, final int j, final int index, final Panmage img, final boolean mirror) {
            final int d = BotsnBoltsGame.DIM;
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
        protected final static byte STATE_JUMPS = 8;
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
        
        protected EarthquakeBot(final Segment seg) {
            super(EARTHQUAKE_OFF_X, EARTHQUAKE_H, seg);
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
                if ((v > 0) && (getPosition().getY() >= 165)) {
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
                    startEarthquake(-17, 1, 1);
                } else if (drillTimer == 1) {
                    startDirtShatter();
                }
            }
            return false;
        }
        
        private final void startEarthquake(final int backOx, final int remaining, final int maxIndex) {
            new Earthquake(this, backOx, 0, remaining, maxIndex);
            new Earthquake(this, 12, 0, remaining, maxIndex).setMirror(!isMirror());
        }
        
        private final void startDirtShatter() {
            Player.shatter(this, DrillEnemy.getDirtShatter());
        }
        
        @Override
        protected final boolean onBossLanded() {
            if (state == STATE_JUMP_DRILL) {
                startJumpDrillImpact();
                startEarthquake(-11, 3, 2);
                startDirtShatter();
                return true;
            }
            return false;
        }

        @Override
        protected final boolean pickState() {
            if (moves == 0) {
                startDrill1(); // Start with this; loads images needed for jump impact
            } else if (moves == 1) {
                startJump();
            } else {
                final int r = rand((moves == 2) ? 2 : 3);
                if (r == 0) {
                    startDrill1();
                } else if (r == 1) {
                    startJumps();
                } else {
                    startJump();
                }
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
                    startJump(2, 0);
                    break;
                default :
                    turnTowardPlayer();
                    startStill();
                    break;
            }
            return false;
        }
        
        @Override
        protected final boolean hasPendingJumps() {
            return hasPendingOrContinuedJumps();
        }
        
        @Override
        protected final byte getStateJumps() {
            return STATE_JUMPS;
        }
        
        @Override
        protected final float getJumpsV() {
            return 9;
        }
        
        @Override
        protected final int getJumpsHv() {
            return 5;
        }
        
        protected final void startJump() {
            startJump(12, Mathtil.rand() ? 0 : (getMirrorMultiplier() * 10));
        }
        
        protected final void startJump(final int v, final int hv) {
            startJump(STATE_JUMP, getJump(), v, hv);
        }
        
        protected final void startJumpDrill() {
            startStateIndefinite(STATE_JUMP_DRILL, getJumpDrillStart());
            v = 0;
            hv = 0;
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
        
        @Override
        protected final Panmage getJump() {
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
        private final int remaining;
        private final int maxIndex;
        private int timer = 0;
        private int index = 0;
        private int distance = 0;
        
        protected Earthquake(final Panctor src, final int ox, final int oy, final int remaining, final int maxIndex) {
            this(src, ox, oy, remaining, maxIndex, 0);
        }
        
        protected Earthquake(final Panctor src, final int ox, final int oy, final int remaining, final int maxIndex, final int velX) {
            super(getSubImage(0), src, ox, oy, 0, 0);
            this.velX = (velX == 0) ? (8 * getMirrorMultiplier() * ox / Math.abs(ox)) : velX;
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
                    new Earthquake(this, 16, 0, remaining - 1, Math.max(0, maxIndex - 1), velX);
                }
                if ((timer % frameDuration) == 0) {
                    index++;
                    if (index <= maxIndex) {
                        changeView(getSubImage(index));
                    }
                }
            }
        }
        
        @Override
        protected final int getDamage() {
            return 3;
        }
        
        @Override
        protected final void onCollisionWithPlayerProjectile(final Projectile prj) {
            prj.bounce();
        }
        
        protected final static Panmage getSubImage(final int i) {
            Panmage image = images[i];
            if (image == null) {
                final float subX, subY;
                final Panple o, min, max, size;
                if (i == 0) {
                    subX = 17; //TODO sub-coordinates and sizes for SubPanmage and/or renderView should be powers of 2; adjust origin/position to put in right place
                    subY = 0;
                    o = new FinPanple2(7, 1);
                    min = new FinPanple2(-5, 0);
                    max = new FinPanple2(5, 5);
                    size = new FinPanple2(15, 15); //TODO see above
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
    
    protected final static int CYCLONE_OFF_X = 6, CYCLONE_H = 24, CYCLONE_TOP_OFF_Y = 18;
    protected final static Panple CYCLONE_O = new FinPanple2(14, 1);
    protected final static Panple CYCLONE_MIN = getMin(CYCLONE_OFF_X);
    protected final static Panple CYCLONE_MAX = getMax(CYCLONE_OFF_X, CYCLONE_H);
    protected final static Panple CYCLONE_JUMP_O = new FinPanple2(14, 3);
    protected final static Panple CYCLONE_SPIN_O = new FinPanple2(30, 1);
    protected final static Panple CYCLONE_SPIN_MIN = new FinPanple2(CYCLONE_MIN.getX(), CYCLONE_TOP_OFF_Y + 20);
    protected final static Panple CYCLONE_SPIN_MAX = getMax(CYCLONE_OFF_X, 58);
    
    protected final static class CycloneBot extends Boss implements StepEndListener {
        protected final static byte STATE_LAUNCH = 1;
        protected final static byte STATE_LAUNCH_END = 2;
        protected final static byte STATE_SPIN = 3;
        protected final static byte STATE_JUMP = 4;
        protected final static int WAIT_LAUNCH = 19;
        protected final static int WAIT_SPIN = 171;
        protected static Panmage still = null;
        protected static Panmage whirlStart1 = null;
        protected static Panmage whirlStart2 = null;
        protected final static Panmage[] whirls = new Panmage[3];
        protected final static Panmage[] jumps = new Panmage[3];
        protected static Panmage launchStart = null;
        protected static Panmage launch1 = null;
        protected static Panmage launch2 = null;
        protected static Panmage spinStart1 = null;
        protected static Panmage spinStart2 = null;
        protected static Panmage spinStart3 = null;
        protected static Panmage spin1 = null;
        protected static Panmage spin2 = null;
        protected static Panmage spin3 = null;
        protected static Panmage spinBoundBoxTop = null;
        protected static Panmage spinBoundBoxBottom = null;
        private final int xRight;
        private final int xLeft;
        private long age = 0;
        private DamageBox spinDamageBoxTop = null;
        private DamageBox spinDamageBoxBottom = null;
        
        protected CycloneBot(final Segment seg) {
            super(CYCLONE_OFF_X, CYCLONE_H, seg);
            xRight = getX(); // 352
            xLeft = getMirroredX(xRight); // 31
        }
        
        @Override
        protected final int getInitialOffsetX() {
            return 0;
        }
        
        @Override
        protected final boolean onWaiting() {
            age++;
            if (state == STATE_JUMP) {
                changeView(getJump());
            } else if (age < 10) {
                return false;
            } else if (age < 12) {
                changeView(getWhirlStart1());
                return false;
            } else if (age < 14) {
                changeView(getWhirlStart2());
                return false;
            } else if (state == STATE_STILL) {
                changeView(getWhirl());
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
                    Panctor.destroy(spinDamageBoxTop);
                    spinDamageBoxTop = null;
                    Panctor.destroy(spinDamageBoxBottom);
                    spinDamageBoxBottom = null;
                } else {
                    if (spinDamageBoxTop == null) {
                        spinDamageBoxTop = newDamageBox(getSpinBoundBoxTop());
                        spinDamageBoxBottom = newDamageBox(getSpinBoundBoxBottom());
                    }
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
        
        private final DamageBox newDamageBox(final Panmage img) {
            return new DamageBox(img, this) {
                @Override protected final int getDamage() {
                    return 4;
                }
                @Override protected final boolean isDestroyedOnImpact() {
                    return false;
                }
                @Override protected final void onCollisionWithPlayerProjectile(final Projectile prj) {
                    prj.bounce();
                }
            };
        }

        @Override
        protected final boolean pickState() {
            final int r = rand(3);
            if (r == 0) {
                startLaunch();
            } else if (r == 1) {
                startSpin();
            } else {
                startJump();
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
        
        @Override
        public final void onStepEnd(final StepEndEvent event) {
            if (spinDamageBoxTop != null) {
                spinDamageBoxTop.getPosition().set(getPosition());
                spinDamageBoxBottom.getPosition().set(getPosition());
            }
        }
        
        @Override
        protected final void onEnemyDestroy() {
            Panctor.destroy(spinDamageBoxTop);
            Panctor.destroy(spinDamageBoxBottom);
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
        
        private final void startJump() {
            final Panmage jump = getJump();
            final int hv = 5 * getMirrorMultiplier();
            startJump(STATE_JUMP, jump, 10, hv);
            addPendingJump(STATE_JUMP, jump, 10, hv);
        }
        
        @Override
        protected final void onJumpLanded() {
            final int x = getX();
            if (Math.abs(x - xLeft) < 5) {
                getPosition().setX(xLeft);
            } else if (Math.abs(xRight - x) < 5) {
                getPosition().setX(xRight);
            }
        }

        @Override
        protected final Panmage getStill() {
            return (still = getCycloneImage(still, "cyclonebot/CycloneBot"));
        }
        
        protected final Panmage getWhirl() {
            return getCurrent(whirls, "Whirl", CYCLONE_O);
        }
        
        @Override
        protected final Panmage getJump() {
            return getCurrent(jumps, "Jump", CYCLONE_JUMP_O);
        }
        
        protected final Panmage getCurrent(final Panmage[] imgs, final String name, final Panple o) {
            final int i = ((int) (age % 6)) / 2;
            Panmage img = imgs[i];
            if (img == null) {
                img = getCycloneImage(null, "cyclonebot/CycloneBot" + name + (i + 1), o);
                imgs[i] = img;
            }
            return img;
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
            return getCycloneImage(img, name, CYCLONE_O);
        }
        
        protected final static Panmage getCycloneImage(final Panmage img, final String name, final Panple o) {
            return getImage(img, name, o, CYCLONE_MIN, CYCLONE_MAX);
        }
        
        protected final static Panmage getCycloneSpinImage(final Panmage img, final String name) {
            return getImage(img, name, CYCLONE_SPIN_O, CYCLONE_SPIN_MIN, CYCLONE_SPIN_MAX);
        }
        
        protected final static Panmage getCycloneSpinStartImage(final Panmage img, final String name) {
            return getCycloneImage(img, name, CYCLONE_SPIN_O);
        }
        
        protected final static Panmage getSpinBoundBoxTop() {
            if (spinBoundBoxTop == null) {
                spinBoundBoxTop = Pangine.getEngine().createEmptyImage(Pantil.vmid(), CYCLONE_SPIN_O,
                    new FinPanple2(CYCLONE_SPIN_MIN.getX() - Player.VEL_PROJECTILE - 1, CYCLONE_TOP_OFF_Y - 1),
                    new FinPanple2(CYCLONE_SPIN_MAX.getX() + Player.VEL_PROJECTILE + 1, 39));
            }
            return spinBoundBoxTop;
        }
        
        protected final static Panmage getSpinBoundBoxBottom() {
            if (spinBoundBoxBottom == null) {
                spinBoundBoxBottom = Pangine.getEngine().createEmptyImage(Pantil.vmid(), CYCLONE_SPIN_O, CYCLONE_MIN, new FinPanple2(CYCLONE_MAX.getX(), CYCLONE_TOP_OFF_Y + 1));
            }
            return spinBoundBoxBottom;
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
        
        @Override
        protected final int getDamage() {
            return 3;
        }
        
        @Override
        protected final void onCollisionWithPlayerProjectile(final Projectile prj) {
            prj.burst();
            onExpire();
            destroy();
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
    
    protected final static int FLOOD_OFF_X = 6, FLOOD_H = 28;
    protected final static Panple FLOOD_O = new FinPanple2(14, 1);
    protected final static Panple FLOOD_MIN = getMin(FLOOD_OFF_X);
    protected final static Panple FLOOD_MAX = getMax(FLOOD_OFF_X, FLOOD_H);
    
    protected final static class FloodBot extends Boss {
        protected final static byte STATE_FILL = 1;
        protected final static byte STATE_JUMP = 2;
        protected final static byte STATE_RAISE = 3;
        protected final static byte STATE_FALL = 4;
        protected final static byte STATE_SWIM = 5;
        protected final static byte STATE_TORPEDO = 6;
        protected final static byte STATE_SWIM_UP = 7;
        protected final static byte STATE_SWIM_DOWN = 8;
        protected final static byte STATE_SWIM_STILL = 9;
        protected final static int FILL_FRAME_DURATION = 3;
        protected final static int WAIT_FILL = 4 * FILL_FRAME_DURATION;
        protected final static int RAISE_FRAMES = 28;
        protected final static int RAISE_FRAME_DURATION = 3;
        protected final static int WAIT_RAISE = RAISE_FRAMES * RAISE_FRAME_DURATION;
        protected final static int WAIT_TORPEDO = 30;
        protected static Panmage still = null;
        protected static Panmage start1 = null;
        protected static Panmage start2 = null;
        protected static Panmage start3 = null;
        protected static Panmage jump = null;
        protected static Panmage open = null;
        protected static Panmage close = null;
        protected static Panmage fall = null;
        protected static Panmage swim1 = null;
        protected static Panmage swim2 = null;
        protected static Panmage swim3 = null;
        protected static Panmage launch = null;
        protected static Panmage whoosh = null;
        private final Valve valve;
        private final int xRight;
        private final int xLeft;
        private boolean fillNeeded = true; // Called after super constructor
        private Tile flowTile = null;
        private float prevY = 0;
        private boolean prevUnderwater = false;
        
        protected FloodBot(final Segment seg) {
            super(FLOOD_OFF_X, FLOOD_H, seg);
            valve = isDuringGameplay() ? new Valve(this) : null;
            xRight = getX();
            xLeft = getMirroredX(xRight);
        }
        
        @Override
        protected final void init() {
            this.fillNeeded = true; // Called by super constructor
        }
        
        @Override
        protected float getG() {
            return gWater;
        }
        
        @Override
        protected final boolean isStill() {
            return super.isStill() || (state == STATE_SWIM_STILL);
        }
        
        @Override
        protected final boolean onWaiting() {
            prevUnderwater = Player.splashIfNeeded(this, prevUnderwater, null);
            if (state == STATE_SWIM) {
                onSwimming();
                return true;
            } else if (state == STATE_RAISE) {
                onRaising();
                return true;
            } else if (state == STATE_JUMP) {
                onJumping();
            } else if (state == STATE_FILL) {
                onFilling();
            } else if (state == STATE_SWIM_UP) {
                onSwimmingUp();
                return true;
            } else if (state == STATE_SWIM_DOWN) {
                onSwimmingDown();
                return true;
            } else if (state == STATE_SWIM_STILL) {
                onSwimmingStill();
                return true;
            } else if (state == STATE_TORPEDO) {
                if (waitTimer == (WAIT_TORPEDO - 1)) {
                    new Torpedo(this);
                }
                return true;
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
        
        protected final void onJumping() {
            final float y = getPosition().getY();
            if (y < prevY) {
                getPosition().set(isMirror() ? 224 : 159, 161);
                v = 0;
                startRaise();
            }
            prevY = y;
        }
        
        protected final void onRaising() {
            final int temp = WAIT_RAISE - waitTimer - 1;
            if (temp == 0) {
                newWhoosh(!isMirror());
            } else if (temp == ((9 * RAISE_FRAME_DURATION) + 1)) {
                newWhoosh(isMirror());
            }
            if ((temp % RAISE_FRAME_DURATION) != 0) {
                return;
            }
            final int index = temp / RAISE_FRAME_DURATION;
            if (index < 10) {
                if (index == 0) {
                    valve.setDirection(-1);
                } else if (index == 9) {
                    valve.setDirection(1);
                    setView(getCurrentClose());
                }
                setTiles(index, 0, getFlowTile());
            } else if (index < 18) {
                if (((index % 2) == 0) && ((index < 16) || (RoomLoader.getWaterTile() < 6))) {
                    RoomLoader.raiseWaterTile();
                    if (RoomLoader.getWaterTile() == 12) {
                        valve.destroy();
                    }
                }
            } else if (index < RAISE_FRAMES) {
                if (index == 18) {
                    valve.setDirection(0);
                }
                setTiles(index, 18, null);
            }
        }
        
        protected final void onSwimming() {
            setCurrentSwim();
            if (!addBoundedX(xLeft, xRight)) {
                endSwim();
            }
        }
        
        private final void endSwim() {
            hv = 0;
            setMirror(!isMirror());
            startStill();
        }
        
        private final void onSwimmingUp() {
            setCurrentSwim();
            if ((addY(2) != Y_NORMAL) || isAtWaterLevel()) {
                startStill();
            }
        }
        
        private final void onSwimmingDown() {
            setCurrentSwim();
            if (addY(-2) != Y_NORMAL) {
                startStill();
            }
        }
        
        private final void onSwimmingStill() {
            setCurrentSwim();
        }
        
        private final boolean isAtWaterLevel() {
            return getPosition().getY() >= (RoomLoader.waterLevel - 32);
        }
        
        private final boolean isOnGround() {
            return isGrounded() || (getPosition().getY() < 33);
        }
        
        private final void newWhoosh(final boolean flip) {
            newWhoosh(flip ? 10 : 22, flip);
        }
        
        private final void newWhoosh(final int offY, final boolean flip) {
            new TimedDecoration(this, getWhoosh(), 6, 23, offY, BotsnBoltsGame.DEPTH_BURST).setFlip(flip);
        }
        
        private final Tile getFlowTile() {
            if (flowTile == null) {
                flowTile = RoomLoader.getAnimator(BotsnBoltsGame.imgMap[0][3], true).tile;
            }
            return flowTile;
        }
        
        private final void setTiles(final int index, final int timerOffset, Tile tile) {
            final int y = 11 - (index - timerOffset);
            if (y < RoomLoader.getWaterTile()) {
                return;
            }
            final TileMap tm = BotsnBoltsGame.tm;
            if (tile == null) {
                tile = tm.getTile(2, y);
            }
            tm.setTile(3, y, tile);
            tm.setTile(4, y, tile);
            tm.setTile(19, y, tile);
            tm.setTile(20, y, tile);
        }

        @Override
        protected final boolean pickState() {
            if (fillNeeded) {
                startFill();
                fillNeeded = false;
                return false;
            }
            final int waterTile = RoomLoader.getWaterTile();
            if (waterTile < 6) {
                startJump();
            } else if (isInMiddle()) {
                startSwim();
                return true;
            } else if ((health <= (HudMeter.MAX_VALUE - 7)) && (waterTile < 9)) {
                pickRaiseWaterLevel();
            } else if ((health <= (HudMeter.MAX_VALUE - 14)) && (waterTile < 12)) {
                pickRaiseWaterLevel();
            } else {
                pickBasic();
                return true;
            }
            return false;
        }
        
        private final boolean isInMiddle() {
            final int x = getX();
            return (x > xLeft) && (x < xRight);
        }
        
        private final void pickRaiseWaterLevel() {
            if (isOnGround()) {
                startJump();
            } else {
                startSwimSink();
            }
        }
        
        private final void pickBasic() {
            final int waterTile = RoomLoader.getWaterTile();
            if (waterTile < 9) {
                if (rand(2) == 0) {
                    startTorpedo();
                } else {
                    startSwim();
                }
                return;
            }
            final int r = rand(3);
            if (r == 0) {
                startSwimVert();
            } else if (r == 1) {
                startTorpedo();
            } else {
                startSwim();
            }
        }

        @Override
        protected final boolean continueState() {
            if (state == STATE_RAISE) {
                startFall();
            } else {
                startStill();
                return true;
            }
            return false;
        }
        
        @Override
        protected final boolean isTurnTowardPlayerNeeded() {
            return !isInMiddle();
        }
        
        @Override
        protected final void startStill() {
            super.startStill();
            if (!isOnGround()) {
                state = STATE_SWIM_STILL;
                setCurrentSwim();
            }
        }
        
        protected final void startFill() {
            startState(STATE_FILL, WAIT_FILL, getStart1());
        }
        
        protected final void startJump() {
            prevY = 0;
            startJump(STATE_JUMP, getJump(), 8.75f, 4 * getMirrorMultiplier());
        }
        
        protected final void startRaise() {
            startState(STATE_RAISE, WAIT_RAISE, getCurrentOpen());
        }
        
        protected final void startFall() {
            startJump(STATE_FALL, getFall(), 0, 4 * getMirrorMultiplier());
        }
        
        protected final void startSwim() {
            hv = 3 * getMirrorMultiplier();
            startStateIndefinite(STATE_SWIM, getCurrentSwim());
        }
        
        protected final void startTorpedo() {
            startState(STATE_TORPEDO, WAIT_TORPEDO, getLaunch());
        }
        
        private final void startSwimVert() {
            if (isOnGround()) {
                startSwimUp();
            } else if (isAtWaterLevel()) {
                startSwimDown();
            } else if (Mathtil.rand()) {
                startSwimUp();
            } else {
                startSwimDown();
            }
        }
        
        private final void startSwimUp() {
            startState(STATE_SWIM_UP, Mathtil.randi(15, 75), getCurrentSwim());
        }
        
        private final void startSwimDown() {
            startState(STATE_SWIM_DOWN, Mathtil.randi(15, 75), getCurrentSwim());
        }
        
        private final void startSwimSink() {
            startStateIndefinite(STATE_SWIM_DOWN, getCurrentSwim());
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
        
        @Override
        protected final Panmage getJump() {
            return (jump = getFloodImage(jump, "floodbot/FloodBotJump"));
        }
        
        protected final static Panmage getOpen() {
            return (open = getFloodImage(open, "floodbot/FloodBotOpen"));
        }
        
        protected final static Panmage getClose() {
            return (close = getFloodImage(close, "floodbot/FloodBotClose"));
        }
        
        protected final static Panmage getFall() {
            return (fall = getFloodImage(fall, "floodbot/FloodBotFall"));
        }
        
        protected final static Panmage getSwim1() {
            return (swim1 = getFloodImage(swim1, "floodbot/FloodBotSwim1"));
        }
        
        protected final static Panmage getSwim2() {
            return (swim2 = getFloodImage(swim2, "floodbot/FloodBotSwim2"));
        }
        
        protected final static Panmage getSwim3() {
            return (swim3 = getFloodImage(swim3, "floodbot/FloodBotSwim3"));
        }
        
        protected final static Panmage getLaunch() {
            return (launch = getFloodImage(launch, "floodbot/FloodBotLaunch"));
        }
        
        protected final static Panmage getWhoosh() {
            if (whoosh == null) {
                whoosh = Pangine.getEngine().createImage("whoosh", BotsnBoltsGame.CENTER_16, null, null, BotsnBoltsGame.RES + "misc/Whoosh.png");
            }
            return whoosh;
        }
        
        protected final Panmage getCurrentOpen() {
            return isMirror() ? getOpen() : getClose();
        }
        
        protected final Panmage getCurrentClose() {
            return isMirror() ? getClose() : getOpen();
        }
        
        private final void setCurrentSwim() {
            changeView(getCurrentSwim());
        }
        
        protected final Panmage getCurrentSwim() {
            final int frameDuration = 4;
            final long i = Pangine.getEngine().getClock() % (4 * frameDuration);
            if (i < frameDuration) {
                return getSwim1();
            } else if (i < (frameDuration * 2)) {
                return getSwim2();
            } else if (i < (frameDuration * 3)) {
                return getSwim3();
            }
            return getSwim2();
        }
        
        protected final static Panmage getFloodImage(final Panmage img, final String name) {
            return getImage(img, name, FLOOD_O, FLOOD_MIN, FLOOD_MAX);
        }
    }
    
    private final static int TORPEDO_OFF_X = 6;
    private final static int TORPEDO_H = 4;
    private final static Panple TORPEDO_O = new FinPanple2(9, 6);
    private final static Panple TORPEDO_MIN = getMin(TORPEDO_OFF_X);
    private final static Panple TORPEDO_MAX = getMax(TORPEDO_OFF_X, TORPEDO_H);
    
    protected final static class Torpedo extends Enemy {
        private final static Panmage[] imgs = new Panmage[2];
        private int timer = 0;
        
        protected Torpedo(final FloodBot src) {
            super(TORPEDO_OFF_X, TORPEDO_H, 0, 0, 2);
            EnemyProjectile.addBySource(this, getTorpedoImage(0), src, 14, 8);
        }
        
        @Override
        public boolean onStepCustom() {
            final int t = timer / 2;
            changeView(getTorpedoImage(t % 2));
            final int v = Math.min(t, 8) * getMirrorMultiplier();
            if (addX(v) != X_NORMAL) {
                EnemyProjectile.burstEnemy(this);
                destroy();
            } else if ((timer % 10) == 9) {
                new Bubble(this, 0);
            }
            timer++;
            return true;
        }
        
        @Override
        protected final int getDamage() {
            return 3;
        }
        
        @Override
        protected final void award(final PowerUp powerUp) {
        }
        
        private final static Panmage getTorpedoImage(final int i) {
            Panmage img = imgs[i];
            if (img == null) {
                img = Boss.getImage(null, "floodbot/Torpedo" + (i + 1), TORPEDO_O, TORPEDO_MIN, TORPEDO_MAX);
                imgs[i] = img;
            }
            return img;
        }
    }
    
    protected final static class Valve extends Panctor implements StepListener {
        private final static int BASE_X = 184;
        private final static int BASE_Y = 168;
        private static Panmage image = null;
        private int dir = 0;
        private boolean waiting = false;
        
        protected Valve(final FloodBot src) {
            setView(getValveImage());
            getPosition().set(BASE_X, BASE_Y, BotsnBoltsGame.DEPTH_BETWEEN);
            addActor(this);
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            waiting = !waiting;
            if (waiting) {
                setRotation(getRot() + dir);
            }
        }
        
        protected void setRotation(final int rot) {
            final int r;
            if (rot < 0) {
                r = rot + 4;
            } else if (rot > 3) {
                r = rot - 4;
            } else {
                r = rot;
            }
            final int offX, offY;
            switch (r) {
                case 0 :
                    offX = 0;
                    offY = 0;
                    break;
                case 1 :
                    offX = 15;
                    offY = 0;
                    break;
                case 2 :
                    offX = 15;
                    offY = 15;
                    break;
                default :
                    offX = 0;
                    offY = 15;
            }
            getPosition().set(BASE_X + offX, BASE_Y + offY);
            setRot(r);
        }
        
        protected void setDirection(final int dir) {
            this.dir = dir;
            waiting = false;
        }
        
        protected final static Panmage getValveImage() {
            return (image = getImage(image, "floodbot/Valve", null, null, null));
        }
    }
    
    protected final static int DROUGHT_OFF_X = 6, DROUGHT_H = 24;
    protected final static Panple DROUGHT_O = new FinPanple2(14, 1);
    protected final static Panple DROUGHT_MIN = getMin(DROUGHT_OFF_X);
    protected final static Panple DROUGHT_MAX = getMax(DROUGHT_OFF_X, DROUGHT_H);
    protected final static Panple DROUGHT_SAND_MAX = getMax(DROUGHT_OFF_X, 4);
    
    protected final static class DroughtBot extends Boss implements Wrapper {
        private final static int NUM_MORPHS = 7;
        protected final static byte STATE_MORPH = 1;
        protected final static byte STATE_SAND = 2;
        protected final static byte STATE_WRAP = 3;
        protected final static byte STATE_UNMORPH = 4;
        protected final static byte STATE_HOLD = 5;
        protected final static byte STATE_JUMP = 6;
        protected final static byte STATE_FLARE = 7;
        protected final static byte STATE_FADE = 8;
        protected final static int WAIT_MORPH = NUM_MORPHS * 2 - 1;
        protected final static int WAIT_HOLD = 60;
        protected final static int WAIT_FLARE = 30;
        protected final static int WAIT_FADE = 30;
        protected static Panmage still = null;
        protected final static Panmage[] morphs = new Panmage[NUM_MORPHS];
        protected static Panmage sand = null;
        protected final static Panmage[] wraps = new Panmage[2];
        protected static Panmage hold = null;
        protected static Panmage launch = null;
        protected static Panmage jump = null;
        protected static Panmage flare = null;
        protected static Panmage light = null;
        protected final static Panmage[] fades = new Panmage[3];
        private final int xRight;
        private final int xLeft;
        private Pantexture tex = null;
        
        protected DroughtBot(final Segment seg) {
            super(DROUGHT_OFF_X, DROUGHT_H, seg);
            xRight = getX();
            xLeft = getMirroredX(xRight);
        }
        
        @Override
        protected final boolean onBossLanded() {
            if (Panctor.isVisible(tex)) {
                startFade();
                return true;
            }
            return false;
        }
        
        @Override
        protected final void onStepEnd() {
            super.onStepEnd();
            if (state == STATE_FADE && waitTimer == WAIT_FADE) {
                final Panple pos = getPosition();
                final float x = pos.getX();
                if (Math.abs(x - xLeft) < 3.0f) {
                    pos.setX(xLeft);
                } else if (Math.abs(x - xRight) < 3.0f) {
                    pos.setX(xRight);
                }
            }
        }
        
        @Override
        protected final boolean onWaiting() {
            if (state == STATE_SAND) {
                return onSanding();
            } else if (state == STATE_MORPH) {
                onMorphing(true);
            } else if (state == STATE_UNMORPH) {
                onMorphing(false);
            } else if (state == STATE_WRAP) {
                return onWrapping();
            } else if (state == STATE_FLARE) {
                return onFlaring();
            } else if (state == STATE_FADE) {
                onFading();
            } else if ((state == STATE_HOLD) && (waitTimer == (WAIT_HOLD - 1))) {
                new Scythe(this);
            } else if ((state == STATE_JUMP) && (Math.abs(getPosition().getX() + hv - 192.0f) < 4.0f)) {
                startFlare();
            }
            return false;
        }
        
        private final void onMorphing(final boolean morphingToSand) {
            final int i = WAIT_MORPH - waitTimer;
            if ((i % 2) == 0) {
                final int num = morphingToSand ? i : waitTimer;
                changeView(getMorph(num / 2));
            }
        }
        
        private final boolean onSanding() {
            if (!addBoundedX(xLeft, xRight)) {
                setMirror(!isMirror());
                if (Mathtil.rand()) {
                    hv *= -1;
                } else {
                    startUnmorph();
                }
            }
            return true;
        }
        
        private final boolean onWrapping() {
            changeView(getCurrentWrap());
            return true;
        }
        
        private final boolean onFlaring() {
            final int i = WAIT_FLARE - waitTimer;
            if ((i > 4) && (i < 16)) {
                if (tex == null) {
                    tex = new Flare(getLight());
                    addActor(tex);
                } else {
                    tex.setImage(getLight());
                    tex.setVisible(true);
                }
                final int i0 = i - 5, i16 = i0 * 16, i32 = i16 * 2;
                final int z = (i0 < 8) ? BotsnBoltsGame.DEPTH_ENEMY_BACK : BotsnBoltsGame.DEPTH_OVERLAY;
                tex.getPosition().set(160 - i16, 112 - i16, z);
                final int s = 64 + i32;
                tex.setSize(s, s);
            }
            return true;
        }
        
        private final void onFading() {
            switch (waitTimer) {
                case 10 :
                case 7 :
                case 4 :
                    tex.setImage(getFade(2 - ((waitTimer - 4) / 3)));
                    break;
                case 1 :
                    Panctor.setInvisible(tex);
                    break;
            }
        }
        
        @Override
        public void onAttack(final Player player) {
            if ((state == STATE_SAND) && !player.isInvincible()) { // Check invincibility before hurting Player
                startWrap(player);
            }
            super.onAttack(player);
        }
        
        @Override
        protected final boolean pickState() {
            final int r = rand(3);
            if (r == 0) {
                startMorph();
            } else if (r == 1) {
                startHold();
            } else {
                startJump();
            }
            return false;
        }

        @Override
        protected final boolean continueState() {
            if (state == STATE_MORPH) {
                startSand();
            } else if (state == STATE_FLARE) {
                finishJump();
            } else {
                startStill();
            }
            return false;
        }
        
        protected final void startMorph() {
            startState(STATE_MORPH, WAIT_MORPH, getMorph(0));
        }
        
        protected final void startSand() {
            startStateIndefinite(STATE_SAND, getSand());
            hv = getMirrorMultiplier() * 6;
        }
        
        protected final void startWrap(final Player player) {
            hv = 0;
            startStateIndefinite(STATE_WRAP, getCurrentWrap());
            player.startWrapped(this);
        }
        
        @Override
        public final void endWrap(final Player player) {
            startSand();
        }
        
        protected final void startUnmorph() {
            hv = 0;
            startState(STATE_UNMORPH, WAIT_MORPH, getMorph(NUM_MORPHS - 1));
        }
        
        protected final void startHold() {
            startState(STATE_HOLD, WAIT_HOLD, getHold());
        }
        
        protected final void startJump() {
            startJump(11.7f);
        }
        
        protected final void finishJump() {
            startJump(v);
        }
        
        protected final void startJump(final float v) {
            startJump(STATE_JUMP, getJump(), v, 8 * getMirrorMultiplier());
        }
        
        protected final void startFlare() {
            startState(STATE_FLARE, WAIT_FLARE, getFlare());
        }
        
        protected final void startFade() {
            startState(STATE_FADE, WAIT_FADE, getStill());
        }

        @Override
        protected final Panmage getStill() {
            return (still = getDroughtImage(still, "droughtbot/DroughtBot"));
        }
        
        protected final static Panmage getMorph(final int i) {
            Panmage image = morphs[i];
            if (image != null) {
                return image;
            }
            final Panple max = (i >= 5) ? DROUGHT_SAND_MAX : DROUGHT_MAX;
            image = getDroughtImage(null, "droughtbot/DroughtBotMorph" + (i + 1), max);
            morphs[i] = image;
            return image;
        }
        
        protected final static Panmage getSand() {
            return (sand = getDroughtImage(sand, "droughtbot/DroughtBotSand", DROUGHT_SAND_MAX));
        }
        
        protected final static Panmage getWrap(final int i) {
            Panmage image = wraps[i];
            if (image != null) {
                return image;
            }
            image = getImage(null, "droughtbot/DroughtBotWrap" + (i + 1), BotsnBoltsGame.oj, BotsnBoltsGame.ng, BotsnBoltsGame.xg);
            wraps[i] = image;
            return image;
        }
        
        protected final static Panmage getHold() {
            return (hold = getDroughtImage(hold, "droughtbot/DroughtBotHold"));
        }
        
        protected final static Panmage getLaunch() {
            return (launch = getDroughtImage(launch, "droughtbot/DroughtBotLaunch"));
        }
        
        @Override
        protected final Panmage getJump() {
            return (jump = getDroughtImage(jump, "droughtbot/DroughtBotJump"));
        }
        
        protected final static Panmage getFlare() {
            return (flare = getDroughtImage(flare, "droughtbot/DroughtBotFlare"));
        }
        
        protected final static Panmage getLight() {
            return (light = getImage(light, "droughtbot/Light", null, null, null));
        }
        
        protected final static Panmage getFade(final int i) {
            Panmage image = fades[i];
            if (image != null) {
                return image;
            }
            image = getImage(null, "droughtbot/Fade" + (i + 1), null, null, null);
            fades[i] = image;
            return image;
        }
        
        protected final static Panmage getCurrentWrap() {
            return getWrap((Pangine.getEngine().getClock() % 12) < 3 ? 0 : 1);
        }
        
        private final static Panmage getDroughtImage(final Panmage img, final String name) {
            return getDroughtImage(img, name, DROUGHT_MAX);
        }
        
        private final static Panmage getDroughtImage(final Panmage img, final String name, final Panple max) {
            return getImage(img, name, DROUGHT_O, DROUGHT_MIN, max);
        }
    }
    
    protected final static class Flare extends Pantexture {
        protected Flare(final Panmage img) {
            super(img);
        }
    }
    
    private final static Panple SCYTHE_O = new FinPanple2(11, 16);
    private final static Panple SCYTHE_MIN = new FinPanple2(-2, -4);
    private final static Panple SCYTHE_MAX = new FinPanple2(16, 13);
    private final static Panple SCYTHE_SUB_O = new FinPanple2(1, 9);
    private final static Panple SCYTHE_SUB_SIZE = new FinPanple2(3, 16);
    
    protected final static class Scythe extends EnemyProjectile {
        private static Panmage grow = null;
        private static Panmage grow1 = null;
        private static Panmage grow2 = null;
        private static Panmage grow3 = null;
        private static Panmage grow4 = null;
        private static Panmage scythe1 = null;
        private static Panmage scythe2 = null;
        private final DroughtBot src;
        private int timer = 0;
        private boolean launched = false;
        
        protected Scythe(final DroughtBot src) {
            super(src, -11, 16, 0, 0);
            this.src = src;
            setView(getGrow1());
            setMirror(!src.isMirror());
            getPosition().setZ(BotsnBoltsGame.DEPTH_ENEMY_BACK);
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            super.onStep(event);
            timer++;
            if (launched) {
                onLaunched();
            } else {
                onHeld();
            }
        }
        
        private final void onHeld() {
            if (timer == 2) {
                changeView(getGrow2());
            } else if (timer == 4) {
                changeView(getGrow3());
            } else if (timer == 6) {
                changeView(getGrow4());
            } else if (timer == 16) {
                changeView(getScythe1());
            } else if (timer == 26) {
                launch();
            }
        }
        
        private final void onLaunched() {
            final int f = timer % 4;
            if (f == 0) {
                changeView(getScythe2());
            } else if (f == 2) {
                changeView(getScythe1());
                setRot(getRot() - 1);
            }
            if (timer >= 16) {
                timer = 0;
            }
        }
        
        private final void launch() {
            setMirror(!isMirror());
            final Panple pos = getPosition();
            pos.setZ(BotsnBoltsGame.DEPTH_PROJECTILE);
            final int mm = getMirrorMultiplier();
            pos.addX(12 * mm);
            getVelocity().setX(6 * mm);
            timer = 0;
            launched = true;
            src.setView(DroughtBot.getLaunch());
        }
        
        @Override
        protected final void onCollisionWithPlayerProjectile(final Projectile prj) {
            if (launched) {
                prj.bounce();
            }
        }
        
        @Override
        protected final int getDamage() {
            return 3;
        }
        
        private final static Panmage getGrow() {
            return (grow = getImage(grow, "droughtbot/ScytheGrow", null, null, null));
        }
        
        private final static Panmage getGrow1() {
            return (grow1 = getGrowSubImage(grow1, 1));
        }
        
        private final static Panmage getGrow2() {
            return (grow2 = getGrowSubImage(grow2, 12));
        }
        
        private final static Panmage getGrowSubImage(Panmage img, final int x) {
            if (img != null) {
                return img;
            }
            final Panmage grow = getGrow();
            return new SubPanmage("droughtbot/ScytheGrow." + x, SCYTHE_SUB_O, FinPanple.ORIGIN, FinPanple.ORIGIN, grow, x, 0, SCYTHE_SUB_SIZE);
        }
        
        private final static Panmage getGrow3() {
            return (grow3 = getScytheImage(grow3, "droughtbot/ScytheGrow3"));
        }
        
        private final static Panmage getGrow4() {
            return (grow4 = getScytheImage(grow4, "droughtbot/ScytheGrow4"));
        }
        
        private final static Panmage getScythe1() {
            return (scythe1 = getScytheImage(scythe1, "droughtbot/Scythe1"));
        }
        
        private final static Panmage getScythe2() {
            if (scythe2 != null) {
                return scythe2;
            }
            return (scythe2 = getScytheImage(scythe2, "droughtbot/Scythe2", new FinPanple2(0, -12), new FinPanple2(18, 10)));
        }
        
        private final static Panmage getScytheImage(final Panmage img, final String name) {
            return getScytheImage(img, name, SCYTHE_MIN, SCYTHE_MAX);
        }
        
        private final static Panmage getScytheImage(final Panmage img, final String name, final Panple min, final Panple max) {
            return getImage(img, name, SCYTHE_O, min, max);
        }
    }
    
    protected final static PlayerContext newPlayerContext(final PlayerImages pi) {
        final Profile prf = new Profile(true);
        prf.autoCharge = true;
        return new PlayerContext(prf, pi);
    }
    
    protected abstract static class AiBoss extends Player implements SpecBoss, RoomAddListener {
        protected final List<AiHandler> handlers = new ArrayList<AiHandler>();
        protected AiHandler handler = null;
        protected AiHandler nextHandler = null;
        protected boolean defeated = false;
        protected boolean needMirror = false;
        protected int waitTimer = 0;
        protected int shootTimer = 0;
        protected int extra = 0;
        private float lastV = 0;
        
        protected AiBoss(final PlayerImages pi, final int x, final int y) {
            super(newPlayerContext(pi));
            addUpgrades(prf.upgrades);
            final Panple pos = getPosition();
            BotsnBoltsGame.tm.savePositionXy(pos, x, y);
            pos.setZ(BotsnBoltsGame.DEPTH_ENEMY);
            setMirror(true);
            BotsnBoltsGame.addActor(this);
            ai = new BossAi();
            health = 0;
            aiBoss = this;
        }
        
        protected void addUpgrades(final Set<Upgrade> upgrades) {
        }
        
        @Override
        public final void onRoomAdd(final RoomAddEvent event) {
            BotsnBoltsGame.initHealthMeter(healthMeter = Enemy.newHealthMeter(this), false);
        }
        
        @Override
        public final void onHealthMaxDisplayReached() {
        }
        
        @Override
        public final void onMaterialized() {
            super.onMaterialized();
            health = HudMeter.MAX_VALUE;
        }
        
        protected final void onStepAi() {
            if (stateHandler == WARP_HANDLER) {
                return;
            } else if (needMirror) {
                mirror();
                needMirror = false;
                return;
            } else if (shootTimer > 0) {
                shooting();
                shootTimer--;
                if (shootTimer == 0) {
                    releaseShoot();
                }
            }
            if (nextHandler != null) {
                startHandler(nextHandler);
            } if (handler == null) {
                startHandler(rand(handlers));
            }
            if ((v <= 0) && (lastV > 0)) {
                handler.onJumpPeak(this);
            }
            lastV = v;
            handler.onStep(this);
            if (!isIndefinite()) {
                waitTimer--;
                if (waitTimer < 0) {
                    if (isStill()) {
                        if (isGrounded()) {
                            startHandler(null);
                        } else {
                            waitTimer++;
                        }
                    } else {
                        startHandler(stillHandler);
                    }
                }
            }
        }
        
        protected final boolean isStill() {
            return handler == stillHandler;
        }
        
        protected abstract int initStillTimer();
        
        protected final void moveX() {
            if (hv < 0) {
                left();
            } else if (hv > 0) {
                right();
            } else if (isMirror()) {
                left();
            } else {
                right();
            }
        }
        
        protected final boolean isIndefinite() {
            return waitTimer == WAIT_INDEFINITE;
        }
        
        protected final void startHandler(final AiHandler handler) {
            if (this.handler != null) {
                if (!this.handler.finish(this)) {
                    nextHandler = handler;
                    return;
                }
            }
            nextHandler = null;
            shootTimer = 0;
            extra = 0;
            turnTowardPlayer();
            prf.shootMode = SHOOT_NORMAL;
            prf.jumpMode = JUMP_NORMAL;
            this.handler = handler;
            if (handler != null) {
                handler.init(this);
                waitTimer = handler.initTimer(this);
            }
        }
        
        protected final void mirror() {
            if (isMirror()) {
                right();
            } else {
                left();
            }
        }
        
        protected final void turnTowardPlayer() {
            if (!isFacingPlayer()) {
                mirror();
            }
        }
        
        protected final boolean isFacingPlayer() {
            final boolean mirrorNeeded = getPlayerX() < getPosition().getX();
            return mirrorNeeded == isMirror();
        }
        
        protected final void attack() {
            shoot();
            shooting();
            releaseShoot();
        }
        
        protected final void startAttacking(final int attackTimer) {
            shootTimer = attackTimer;
            shoot();
        }
        
        @Override
        protected final void newProjectile(final float vx, final float vy, final int power) {
            new AiProjectile(this, Projectile.OFF_X, Projectile.OFF_Y, getMirrorMultiplier() * vx, vy, pi, power);
        }
        
        @Override
        protected final void newBomb() {
            new AiBomb(this);
        }
        
        protected final void startStill() {
            startHandler(stillHandler);
        }
        
        @Override
        protected final void onWall(final byte xResult) {
            super.onWall(xResult);
            needMirror = true;
            startStill();
        }
        
        @Override
        protected final void onLanded() {
            super.onLanded();
            if (isIndefinite() && handler.isDoneWhenLanded(this)) {
                startStill();
            }
        }
        
        @Override
        protected final boolean onFell() {
            return false;
        }
        
        @Override
        public final void onCollision(final CollisionEvent event) {
            Enemy.onCollision(this, event);
        }
        
        @Override
        public final boolean isVulnerable() {
            return true;
        }
        
        @Override
        public final void onShot(final Projectile prj) {
            Enemy.onHurt(this, prj);
        }
        
        @Override
        public final boolean isHarmful() {
            return !defeated;
        }
        
        @Override
        public final void onAttack(final Player player) {
            player.hurt(getDamage());
        }
        
        public int getDamage() {
            return DAMAGE;
        }
        
        @Override
        public final void setHealth(final int health) {
            if ((health < this.health) && isStill()) {
                final int damage = this.health - health;
                waitTimer = adjustWaitTimerOnHurt(waitTimer, damage);
            }
            this.health = health;
        }
        
        @Override
        public final boolean isBurstNeeded() {
            return false;
        }
        
        @Override
        public final void award(final Player player) {
        }
        
        @Override
        public final void onDefeat() {
            if (defeated) {
                return;
            }
            defeated = true;
            destroyEnemies();
            if (isGrounded()) {
                afterDefeat();
            } else {
                startHandler(new DefeatedHandler());
            }
        }
        
        private final void afterDefeat() {
            exit();
        }
        
        protected final void exit() {
            destroy();
            dematerialize(new Runnable() {
                @Override public final void run() {
                    Boss.award(new VictoryDisk(getPlayer(), AiBoss.this), DROP_X);
                }});
        }
        
        @Override
        public final void onAward(final Player player) {
            onAwardBoss(this, player);
        }
        
        @Override
        public final boolean isLaunchPossible() {
            return true;
        }
        
        @Override
        public final boolean isDestroyedAfterDefeat() {
            return false;
        }
    }
    
    protected final static class Volatile extends AiBoss {
        protected Volatile(final int x, final int y) {
            super(BotsnBoltsGame.volatileImages, x, y);
            handlers.add(new AttackRunHandler());
            handlers.add(new AttackHandler());
            handlers.add(new AttackJumpHandler());
            handlers.add(new JumpsHandler());
        }
        
        @Override
        protected final int initStillTimer() {
            return Mathtil.randi(30, 45);
        }
    }
    
    protected final static class Volatile2 extends AiBoss {
        protected Volatile2(final int x, final int y) {
            super(BotsnBoltsGame.volatileImages, x, y);
            handlers.add(new SpreadAttackRunHandler());
            handlers.add(new ChargeAttackJumpsHandler());
            handlers.add(new RapidAttackHandler()); // If he has a turret attack, will be similar to this; then move this to Final
            handlers.add(new RapidAttackJumpHandler());
            handlers.add(new BombRollHandler());
        }
        
        @Override
        protected final void addUpgrades(final Set<Upgrade> upgrades) {
            upgrades.add(Profile.UPGRADE_SPREAD);
            upgrades.add(Profile.UPGRADE_CHARGE);
            upgrades.add(Profile.UPGRADE_RAPID);
            upgrades.add(Profile.UPGRADE_BALL);
            upgrades.add(Profile.UPGRADE_BOMB);
        }
        
        @Override
        protected final int initStillTimer() {
            return Mathtil.randi(15, 30);
        }
    }
    
    protected final static class BossAi implements Ai {
        @Override
        public final void onStep(final Player player) {
            ((AiBoss) player).onStepAi();
        }
    }
    
    private abstract static class AiHandler {
        protected void init(final AiBoss boss) {
        }
        
        protected int initTimer(final AiBoss boss) {
            return WAIT_INDEFINITE;
        }
        
        protected abstract void onStep(final AiBoss boss);
        
        protected void onJumpPeak(final AiBoss boss) {
        }
        
        protected boolean isDoneWhenLanded(final AiBoss boss) {
            return true;
        }
        
        protected boolean finish(final AiBoss boss) {
            return true;
        }
    }
    
    private final static StillHandler stillHandler = new StillHandler();
    
    private final static class StillHandler extends AiHandler {
        @Override
        protected final int initTimer(final AiBoss boss) {
            return boss.initStillTimer();
        }
        
        @Override
        protected final void onStep(final AiBoss boss) {
        }
    }
    
    private final static class DefeatedHandler extends AiHandler {
        @Override
        protected final void onStep(final AiBoss boss) {
        }
        
        @Override
        protected final boolean isDoneWhenLanded(final AiBoss boss) {
            boss.afterDefeat();
            return false;
        }
    }
    
    private static class RunHandler extends AiHandler {
        @Override
        protected final void onStep(final AiBoss boss) {
            boss.moveX();
            if (!boss.isFacingPlayer() && (Math.abs(boss.getPosition().getX() - getPlayerX()) > 64)) {
                boss.startStill();
                return;
            }
            onRun(boss);
        }
        
        protected void onRun(final AiBoss boss) {
        }
    }
    
    private static class AttackRunHandler extends RunHandler {
        @Override
        protected final void init(final AiBoss boss) {
            boss.prf.shootMode = getShootMode();
            startAttack(boss);
        }
        
        protected ShootMode getShootMode() {
            return Player.SHOOT_NORMAL;
        }
        
        @Override
        protected final void onRun(final AiBoss boss) {
            if (boss.shootTimer <= 0) {
                boss.shootTimer--;
                if (boss.shootTimer <= -22) {
                    startAttack(boss);
                }
            }
        }
        
        protected final void startAttack(final AiBoss boss) {
            boss.shootTimer = initShootTimer();
            attack(boss);
        }
        
        protected int initShootTimer() {
            return 16;
        }
        
        protected void attack(final AiBoss boss) {
            boss.attack();
        }
    }
    
    private final static class SpreadAttackRunHandler extends AttackRunHandler {
        @Override
        protected final ShootMode getShootMode() {
            return Player.SHOOT_SPREAD;
        }
        
        @Override
        protected final int initShootTimer() {
            return 32;
        }
    }
    
    private final static class RapidAttackRunHandler extends AttackRunHandler {
        @Override
        protected final ShootMode getShootMode() {
            return Player.SHOOT_RAPID;
        }
        
        @Override
        protected final void attack(final AiBoss boss) {
            boss.shoot();
        }
    }
    
    private static class RollHandler extends RunHandler {
        @Override
        protected final void init(final AiBoss boss) {
            boss.prf.jumpMode = Player.JUMP_BALL;
            boss.startBall();
        }
        
        @Override
        protected final boolean finish(final AiBoss boss) {
            boss.endBall();
            return true;
        }
    }
    
    private final static class BombRollHandler extends RollHandler {
        @Override
        protected final void onRun(final AiBoss boss) {
            if (boss.extra <= 0) {
                boss.extra = 25;
            }
            boss.extra--;
            if (boss.extra == 5) {
                boss.shoot();
            }
        }
    }
    
    private static class JumpHandler extends AiHandler {
        @Override
        protected void onStep(final AiBoss boss) {
            boss.jump();
        }
    }
    
    private static class WaitHandler extends AiHandler {
        private final Runnable stepHandler;
        private final Runnable landedHandler;
        
        private WaitHandler(final Runnable stepHandler, final Runnable landedHandler) {
            this.stepHandler = stepHandler;
            this.landedHandler = landedHandler;
        }
        
        @Override
        protected void onStep(final AiBoss boss) {
            if (stepHandler != null) {
                stepHandler.run();
            }
        }
        
        @Override
        protected final boolean isDoneWhenLanded(final AiBoss boss) {
            if (landedHandler != null) {
                landedHandler.run();
            }
            return false;
        }
    }
    
    private static class JumpAndWaitHandler extends WaitHandler {
        private JumpAndWaitHandler(final Runnable landedHandler) {
            super(null, landedHandler);
        }
        
        @Override
        protected void init(final AiBoss boss) {
            boss.jump();
        }
    }
    
    private static class AttackJumpHandler extends JumpHandler {
        @Override
        protected final void onJumpPeak(final AiBoss boss) {
            boss.attack();
        }
    }
    
    private final static class SpreadAttackJumpHandler extends AttackJumpHandler {
        @Override
        protected final void init(final AiBoss boss) {
            boss.prf.shootMode = Player.SHOOT_SPREAD;
        }
    }
    
    private static class RapidAttackJumpHandler extends JumpHandler {
        @Override
        protected final void init(final AiBoss boss) {
            boss.prf.shootMode = Player.SHOOT_RAPID;
        }
        
        @Override
        protected final void onStep(final AiBoss boss) {
            super.onStep(boss);
            if ((boss.shootTimer <= 0) && (boss.v > 0) && (boss.v < 5)) {
                boss.startAttacking(20);
            }
        }
    }
    
    private static class JumpsHandler extends AiHandler {
        @Override
        protected final void onStep(final AiBoss boss) {
            if (boss.nextHandler != null) {
                return;
            }
            boss.moveX();
            if (boss.extra > 0) {
                boss.extra--;
                return;
            }
            boss.jump();
        }
        
        @Override
        protected boolean isDoneWhenLanded(final AiBoss boss) {
            if (boss.isFacingPlayer()) {
                boss.extra = 3;
                return false;
            }
            return true;
        }
    }
    
    private final static class ChargeAttackJumpsHandler extends JumpsHandler {
        @Override
        protected final void init(final AiBoss boss) {
            boss.prf.shootMode = Player.SHOOT_CHARGE;
        }
        
        @Override
        protected final boolean finish(final AiBoss boss) {
            if (boss.isFacingPlayer()) {
                boss.attack(); // AiBoss uses autoCharge, so don't need to start charging; just attack when ready
                return true;
            } else {
                boss.turnTowardPlayer();
                return false;
            }
        }
    }
    
    private final static class AttackHandler extends AiHandler {
        @Override
        protected final int initTimer(final AiBoss boss) {
            return Player.SHOOT_TIME + 2;
        }
        
        @Override
        protected final void onStep(final AiBoss boss) {
            if (boss.waitTimer == (Player.SHOOT_TIME + 1)) {
                boss.attack();
            }
        }
    }
    
    private final static class RapidAttackHandler extends AiHandler {
        @Override
        protected final int initTimer(final AiBoss boss) {
            boss.prf.shootMode = Player.SHOOT_RAPID;
            boss.startAttacking(16);
            return 22;
        }
        
        @Override
        protected final void onStep(final AiBoss boss) {
            // AiBoss handles everything based on shootTimer
        }
    }
    
    private final static class GrappleHandler extends AiHandler {
        @Override
        protected final void init(final AiBoss boss) {
            boss.prf.jumpMode = Player.JUMP_GRAPPLING_HOOK;
            boss.jump();
        }
        
        @Override
        protected final void onStep(final AiBoss boss) {
            boss.moveX();
            boss.extra++;
            if (boss.stateHandler == Player.GRAPPLING_HANDLER) {
                onStepGrappling(boss);
            } else {
                onStepNormal(boss);
            }
        }
        
        private final void onStepNormal(final AiBoss boss) {
            if (boss.extra == 20) {
                boss.releaseJump();
                boss.jump();
                boss.extra = 0;
            }
        }
        
        private final void onStepGrappling(final AiBoss boss) {
            if (boss.extra == 1) {
                boss.releaseJump();
            } else if (boss.extra == 39) {
                boss.jump();
                boss.extra = 19;
                boss.needMirror = true;
            }
        }
    }
    
    protected final static int TITAN_OFF_X = 32, TITAN_H = 92, TITAN_JUMP_H = 72;
    protected final static Panple TITAN_O = new FinPanple2(44, 1);
    protected final static Panple TITAN_MIN = getMin(TITAN_OFF_X);
    protected final static Panple TITAN_MAX = getMax(TITAN_OFF_X, TITAN_H);
    
    protected final static class CyanTitan extends Boss {
        protected final static byte STATE_ATTACK = 1;
        protected final static byte STATE_JUMPS = 2;
        protected static Panmage still = null;
        protected static Panmage jump = null;
        
        protected CyanTitan(final Segment seg) {
            super(TITAN_OFF_X, TITAN_H, seg);
        }

        @Override
        protected final boolean pickState() {
            final int r = Mathtil.randi(0, 999);
            final int attackThreshold = scaleByHealthInt(100, 500);
            if (r < attackThreshold) {
                startAttack();
            } else {
                setH(TITAN_JUMP_H);
                startJumps();
            }
            return false;
        }

        @Override
        protected final boolean continueState() {
            setH(TITAN_H);
            startStill();
            return false;
        }
        
        @Override
        protected final boolean onWaiting() {
            if (state == STATE_ATTACK) {
                onAttacking();
            }
            return false;
        }
        
        private final void onAttacking() {
            if (waitTimer == 1) {
                CyanEnemy.shoot(this, 3, 78, false);
            }
        }
        
        @Override
        protected float getG() {
            return -0.4f;
        }
        
        @Override
        protected final boolean hasPendingJumps() {
            return hasPendingOrContinuedJumps();
        }
        
        @Override
        protected final byte getStateJumps() {
            return STATE_JUMPS;
        }
        
        @Override
        protected final float getJumpsV() {
            return scaleByHealthFloat(5.9f, 8.1f);
        }
        
        @Override
        protected final int getJumpsHv() {
            return scaleByHealthInt(3, 7);
        }
        
        protected final void startAttack() {
            startState(STATE_ATTACK, 2, getStill());
        }
        
        @Override
        protected final void startStill() {
            startStill(scaleByHealthInt(2, 30)); // randomly 15 - 30 for other Bosses
        }
        
        private final int scaleByHealthInt(final float min, final float max) {
            return Math.round(scaleByHealthFloat(min, max));
        }
        
        private final float scaleByHealthFloat(final float min, final float max) {
            if (health < 2) {
                return min;
            }
            final float healthNumerator = health - 1, healthDenominator = HudMeter.MAX_VALUE - 1;
            final float healthScore = healthNumerator / healthDenominator; // health = 1, score = 0.0; health = max, score = 1.0
            return min + (healthScore * (max - min));
        }

        @Override
        protected final Panmage getStill() {
            if (still == null) {
                still = getImage(null, "cyantitan/CyanTitan", TITAN_O, TITAN_MIN, TITAN_MAX);
            }
            return still;
        }
        
        @Override
        protected final Panmage getJump() {
            if (jump == null) {
                final int jumpOffX = 29;
                jump = getImage(null, "cyantitan/CyanTitanJump", new FinPanple2(44, 11), getMin(jumpOffX), getMax(jumpOffX, TITAN_JUMP_H));
            }
            return jump;
        }
    }
    
    protected final static class FinalWagon extends Boss implements RoomAddListener, StepEndListener {
        private final static byte STATE_UNCOVER = 1;
        private final static byte STATE_COVER = 2;
        private final static byte STATE_SPIKE_REVEAL = 3;
        private final static byte STATE_SPIKE_RETRACT = 4;
        private final static byte STATE_ADVANCE = 5;
        private final static byte STATE_RETREAT = 6;
        private final static byte STATE_HATCH_OPEN = 7;
        private final static byte STATE_HATCH_FIRE = 8;
        private final static byte STATE_HATCH_CLOSE = 9;
        private final static byte STATE_HOOD_OPEN = 10;
        private final static byte STATE_HOOD_CLOSE = 11;
        private final static byte STATE_TUBE_REVEAL = 12;
        private final static byte STATE_TUBE_FIRE = 13;
        private final static byte STATE_TUBE_RETRACT = 14;
        private final static byte STATE_DEFEATED = 15;
        private final static int COVER_MAX = 6;
        private final static int SPIKE_MAX = 2;
        private final static int HATCH_MAX = 4;
        private final static int HOOD_MAX = 4;
        private final static int TUBE_MAX = 8;
        private static Panmage box = null;
        private static Panmage hull = null;
        private static Panmage wheel = null;
        private static Panmage plate = null;
        private static Panmage plateTilt = null;
        private static Panmage tube = null;
        private final static Panmage[] hatches = new Panmage[6];
        private final static Panmage[] hoods = new Panmage[8];
        private int counter = 0;
        private int advanceIndex = 0;
        private int coverIndex = COVER_MAX;
        private int hatchIndex = HATCH_MAX;
        private int hoodIndex = 0;
        private int tubeIndex = 0;
        private int animTimer = 0;
        private int rot = 0;
        private WagonSaucer saucer;
        private WagonSpikes spikes = null;
        private int selfDestructCounter = 0;
        private FinalSaucer nextSaucer = null;
        
        protected FinalWagon(final Segment seg) {
            super(0, 0, seg);
            setMirror(false);
            saucer = new WagonSaucer(this);
        }
        
        @Override
        public final void onRoomAdd(final RoomAddEvent event) {
            addActor(saucer);
        }
        
        @Override
        protected final boolean pickState() {
            if (advanceIndex > 0) {
                startRetreat();
            } else {
                if (counter > 2) {
                    final int r = rand((counter == 3) ? 2 : 3);
                    if (r == 0) {
                        startUncover();
                    } else if (r == 1) {
                        startHatchOpen();
                    } else {
                        startHoodOpen();
                    }
                } else if (counter == 0) {
                    startHatchOpen();
                } else if (counter == 1) {
                    startUncover();
                } else if (counter == 2) {
                    startHoodOpen();
                }
                counter++;
            }
            return false;
        }

        @Override
        protected final boolean continueState() {
            return false;
        }
        
        @Override
        protected final boolean onWaiting() {
            switch (state) {
                case STATE_UNCOVER :
                    onUncovering();
                    break;
                case STATE_COVER :
                    onCovering();
                    break;
                case STATE_SPIKE_REVEAL :
                    onSpikeRevealing();
                    break;
                case STATE_SPIKE_RETRACT :
                    onSpikeRetracting();
                    break;
                case STATE_ADVANCE :
                    onAdvancing();
                    break;
                case STATE_RETREAT :
                    onRetreating();
                    break;
                case STATE_HATCH_OPEN :
                    onHatchOpening();
                    break;
                case STATE_HATCH_FIRE :
                    onHatchFiring();
                    break;
                case STATE_HATCH_CLOSE :
                    onHatchClosing();
                    break;
                case STATE_HOOD_OPEN :
                    onHoodOpening();
                    break;
                case STATE_HOOD_CLOSE :
                    onHoodClosing();
                    break;
                case STATE_TUBE_REVEAL :
                    onTubeRevealing();
                    break;
                case STATE_TUBE_FIRE :
                    onTubeFiring();
                    break;
                case STATE_TUBE_RETRACT :
                    onTubeRetracting();
                    break;
            }
            return false;
        }
        
        private final void startUncover() {
            startState(STATE_UNCOVER);
        }
        
        private final void startCover() {
            startState(STATE_COVER);
        }
        
        private final void startSpikeReveal() {
            startState(STATE_SPIKE_REVEAL);
            spikes = new WagonSpikes(this);
            addActor(spikes);
        }
        
        private final void startSpikeRetract() {
            startState(STATE_SPIKE_RETRACT);
        }
        
        private final void startAdvance() {
            startState(STATE_ADVANCE);
        }
        
        private final void startRetreat() {
            startState(STATE_RETREAT);
        }
        
        private final void startHatchOpen() {
            startState(STATE_HATCH_OPEN);
        }
        
        private final void startHatchFire() {
            startState(STATE_HATCH_FIRE);
        }
        
        private final void startHatchClose() {
            startState(STATE_HATCH_CLOSE);
        }
        
        private final void startHoodOpen() {
            startState(STATE_HOOD_OPEN);
        }
        
        private final void startHoodClose() {
            startState(STATE_HOOD_CLOSE);
        }
        
        private final void startTubeReveal() {
            startState(STATE_TUBE_REVEAL);
        }
        
        private final void startTubeFire() {
            startState(STATE_TUBE_FIRE);
        }
        
        private final void startTubeRetract() {
            startState(STATE_TUBE_RETRACT);
        }
        
        private final void startDefeated() {
            startState(STATE_DEFEATED);
        }
        
        private final void startState(final byte state) {
            startStateIndefinite(state, getStill());
        }
        
        private final void onUncovering() {
            animTimer++;
            if (animTimer > 2) {
                animTimer = 0;
                if (coverIndex <= 0) {
                    startSpikeReveal();
                } else {
                    coverIndex--;
                }
            }
        }
        
        private final void onCovering() {
            animTimer++;
            if (animTimer > 2) {
                animTimer = 0;
                if (coverIndex >= COVER_MAX) {
                    startStill();
                } else {
                    coverIndex++;
                }
            }
        }
        
        private final void onSpikeRevealing() {
            animTimer++;
            if (animTimer > 2) {
                animTimer = 0;
                if (spikes.index >= SPIKE_MAX) {
                    startAdvance();
                } else {
                    spikes.index++;
                    spikes.changeView();
                }
            }
        }
        
        private final void onSpikeRetracting() {
            if (isSpikeDestroyed()) {
                startCover();
                return;
            }
            animTimer++;
            if (animTimer > 2) {
                animTimer = 0;
                if (spikes.index <= 0) {
                    spikes.destroy();
                    startCover();
                } else {
                    spikes.index--;
                    spikes.changeView();
                }
            }
        }
        
        private final void onAdvancing() {
            // Wheel diameter: 63; circumference: 198; rotation: 30 degrees (1/12 of wheel, 16 pixels)
            if (isSpikeDestroyed()) {
                startStill();
                return;
            }
            advanceIndex++;
            getPosition().addX(-4);
            if ((advanceIndex % 4) == 1) {
                rot--;
                if (rot < 0) {
                    rot = 3;
                }
                if (advanceIndex == 49) {
                    startStill();
                }
            }
        }
        
        private final void onRetreating() {
            advanceIndex--;
            getPosition().addX(4);
            if ((advanceIndex % 4) == 0) {
                rot++;
                if (rot > 3) {
                    rot = 0;
                }
                if (advanceIndex == 0) {
                    if (isSpikeDestroyed()) {
                        startCover();
                    } else {
                        startSpikeRetract();
                    }
                }
            }
        }
        
        private final void onHatchOpening() {
            animTimer++;
            if (animTimer > 2) {
                animTimer = 0;
                if (hatchIndex > 0) {
                    hatchIndex--;
                } else {
                    startHatchFire();
                }
            }
        }
        
        private final void onHatchFiring() {
            if (hatchIndex > -2) {
                hatchIndex--;
            } else {
                hatchIndex = 0;
                final WagonRocket rocket = new WagonRocket();
                rocket.getPosition().set(304, 100, BotsnBoltsGame.DEPTH_PROJECTILE);
                addActor(rocket);
                startHatchClose();
            }
        }
        
        private final void onHatchClosing() {
            animTimer++;
            if (animTimer > 2) {
                animTimer = 0;
                if (hatchIndex < HATCH_MAX) {
                    hatchIndex++;
                } else {
                    startStill();
                }
            }
        }
        
        private final void onHoodOpening() {
            animTimer++;
            if (animTimer > 2) {
                animTimer = 0;
                if (hoodIndex < HOOD_MAX) {
                    hoodIndex++;
                } else {
                    startTubeReveal();
                }
            }
        }
        
        private final void onHoodClosing() {
            animTimer++;
            if (animTimer > 2) {
                animTimer = 0;
                if (hoodIndex > 0) {
                    hoodIndex--;
                } else {
                    startStill();
                }
            }
        }
        
        private final void onTubeRevealing() {
            if (tubeIndex < TUBE_MAX) {
                tubeIndex++;
            } else {
                startTubeFire();
            }
        }
        
        private final void onTubeFiring() {
            animTimer++;
            if ((animTimer == 8) || (animTimer == 16) || (animTimer == 24)) {
                new WagonMortar(this, 224 - (animTimer * 8));
            } else if (animTimer == 32) {
                animTimer = 0;
                startTubeRetract();
            }
        }

        private final void onTubeRetracting() {
            if (tubeIndex > 0) {
                tubeIndex--;
            } else {
                startHoodClose();
            }
        }
        
        private final boolean isSpikeDestroyed() {
            return Panctor.isDestroyed(spikes);
        }
        
        @Override
        public final void onStepEnd(final StepEndEvent event) {
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY();
            if (saucer != null) {
                saucer.getPosition().set(x + 75, y + 45);
            }
            if (spikes != null) {
                spikes.getPosition().set(x + 14, y + 22);
            }
        }
        
        @Override
        public final void onShot(final Projectile prj) {
            if (state == STATE_DEFEATED) {
                if (prj.getVelocity().getX() == 0f) {
                    final Panple pos = prj.getPosition();
                    burst(pos.getX() - 1, pos.getY());
                    prj.destroy();
                    selfDestructCounter++;
                    if (selfDestructCounter >= 6) {
                        clear();
                        burst();
                        nextSaucer.startBattle();
                        destroy();
                    }
                }
            } else {
                super.onShot(prj);
            }
        }
        
        @Override
        protected final boolean isVulnerableToProjectile(final Projectile prj) {
            return state == STATE_DEFEATED;
        }
        
        @Override
        public final boolean isBurstNeeded() {
            return false;
        }
        
        @Override
        public final boolean isHarmful() {
            return state != STATE_DEFEATED;
        }
        
        @Override
        protected final void onBossDefeat() {
            if (state == STATE_DEFEATED) {
                return;
            } else if (saucer != null) {
                saucer.separate();
            }
            saucer = null;
            startDefeated();
            setPlayerActive(false);
        }
        
        private final void clear() {
            Panctor.destroy(spikes);
            spikes = null;
            Panctor.destroy(healthMeter);
            healthMeter = null;
        }
        
        private final void burst() {
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY();
            for (int i = 0; i < 8; i++) {
                burst(x + Mathtil.randi(16, 108), y + Mathtil.randi(20, 68));
            }
        }
        
        private final static void burst(final float x, final float y) {
            final Burst burst = new Burst(BotsnBoltsGame.finalImages.burst);
            burst.getPosition().set(x, y, BotsnBoltsGame.DEPTH_BURST);
            addActor(burst);
        }
        
        @Override
        protected final boolean isDefeatOrbNeeded() {
            return false;
        }
        
        @Override
        protected PowerUp pickAward(final Player player) {
            return (state == STATE_DEFEATED) ? null : new Disk(player, "FinalWagon");
        }
        
        @Override
        protected final float getDropX() {
            return getPlayerX();
        }
        
        @Override
        public final boolean isDestroyedAfterDefeat() {
            return false;
        }

        @Override
        protected final Panmage getStill() {
            if (box == null) {
                box = Pangine.getEngine().createEmptyImage(BotsnBoltsGame.PRE_IMG + "final.wagon", FinPanple.ORIGIN, new FinPanple2(6, 6), new FinPanple2(135, 63));
            }
            return box;
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panlayer layer = getLayer();
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY();
            renderer.render(layer, getHull(), x, y + 20, BotsnBoltsGame.DEPTH_ENEMY, 0, 0, 128, 128, 0, true, false);
            final Panmage wheel = getWheel();
            final float wx = x - (((rot == 1) || (rot == 2)) ? 1 : 0), wy = y - ((rot > 1) ? 2 : 1);
            final float wrz = (y > 64) ? BotsnBoltsGame.DEPTH_ABOVE : BotsnBoltsGame.DEPTH_BEHIND;
            renderer.render(layer, wheel, wx + 22, wy, BotsnBoltsGame.DEPTH_ENEMY_FRONT, 0, 0, 64, 64, rot, false, false);
            renderer.render(layer, wheel, wx - 2, wy, wrz, 0, 0, 64, 64, rot, false, false);
            renderer.render(layer, wheel, wx + 86, wy, BotsnBoltsGame.DEPTH_ENEMY_FRONT, 0, 0, 64, 64, rot, false, false);
            renderer.render(layer, wheel, wx + 62, wy, wrz, 0, 0, 64, 64, rot, false, false);
            final Panmage plate = getPlate();
            final int plateAmount = coverIndex / 2;
            final boolean tilted = (coverIndex % 2) == 1;
            final float px = x - 1, py = y + (tilted ? 34 : 40);
            if (tilted) {
                renderer.render(layer, getPlateTilt(), px, y + 43, BotsnBoltsGame.DEPTH_ENEMY_FRONT, 0, 0, 16, 16, 0, true, false);
            }
            for (int i = 0; i < plateAmount; i++) {
                renderer.render(layer, plate, px, py - (9 * i), BotsnBoltsGame.DEPTH_ENEMY_FRONT, 0, 0, 16, 16, 0, true, false);
            }
            if (hatchIndex != 0) {
                final int hi = HATCH_MAX - hatchIndex - ((hatchIndex > 0) ? 0 : 1);
                renderer.render(layer, getHatch(hi), x + 76, y + 51, BotsnBoltsGame.DEPTH_ENEMY_FRONT, 0, 0, 16, 16, 0, true, false);
            }
            if (hoodIndex > 0) {
                final int hi = (hoodIndex - 1) * 2;
                final float hy = y + 68, hx = x + 80, hfx;
                final int df;
                if (hoodIndex < 3) {
                    hfx = hx + 15;
                    df = 32;
                } else {
                    hfx = hx + 31;
                    df = 16;
                }
                renderer.render(layer, getHood(hi), hx + 18, hy, BotsnBoltsGame.DEPTH_ENEMY_BACK_2, 0, 0, 16, 16, 0, true, false);
                renderer.render(layer, getHood(hi + 1), hfx, hy, BotsnBoltsGame.DEPTH_ENEMY_FRONT, 0, 0, df, df, 0, true, false);
                if (tubeIndex > 0) {
                    renderer.render(layer, getTube(), hx + 23, hy - 16 + (tubeIndex * 2), BotsnBoltsGame.DEPTH_ENEMY, 0, 0, 16, 16, 0, true, false);
                }
            }
        }
        
        private final static Panmage getHull() {
            return (hull = getImage(hull, "final/WagonHull", null, null, null));
        }
        
        private final static Panmage getWheel() {
            return (wheel = getImage(wheel, "final/WagonWheel", null, null, null));
        }
        
        private final static Panmage getPlate() {
            return (plate = getImage(plate, "final/WagonPlate", null, null, null));
        }
        
        private final static Panmage getPlateTilt() {
            return (plateTilt = getImage(plateTilt, "final/WagonPlateTilt", null, null, null));
        }
        
        private final static Panmage getTube() {
            return (tube = getImage(tube, "final/WagonTube", null, null, null));
        }
        
        private final static Panmage getHatch(final int i) {
            Panmage img = hatches[i];
            if (img == null) {
                img = getImage(img, "final/WagonHatch" + (i + 1), null, null, null);
                hatches[i] = img;
            }
            return img;
        }
        
        private final static Panmage getHood(final int i) {
            Panmage img = hoods[i];
            if (img == null) {
                img = getImage(img, "final/WagonHood" + (i + 1), null, null, null);
                hoods[i] = img;
            }
            return img;
        }
    }
    
    private final static class WagonSpikes extends TileUnawareEnemy {
        private final FinalWagon wagon;
        private int index = 0;
        
        protected WagonSpikes(final FinalWagon wagon) {
            super(0, 0, 5);
            this.wagon = wagon;
            getPosition().setZ(BotsnBoltsGame.DEPTH_ENEMY_FRONT);
            setMirror(true);
            changeView();
        }
        
        @Override
        public final boolean isHarmful() {
            return wagon.isHarmful();
        }
        
        @Override
        protected final int getDamage() {
            return wagon.getDamage();
        }
        
        @Override
        protected final void award(final PowerUp powerUp) {
        }
        
        private final void changeView() {
            setView(getSpikes(index));
        }

        private final static Panmage[] spikes = new Panmage[3];
        
        private final static Panmage getSpikes(final int i) {
            Panmage img = spikes[i];
            if (img == null) {
                img = Boss.getImage(img, "final/WagonSpikes" + (i + 1), null, null, null);
                spikes[i] = img;
            }
            return img;
        }
    }
    
    private final static class WagonRocket extends TileUnawareEnemy {
        private static Panmage img = null;
        private int timer = 0;
        
        private WagonRocket() {
            super(0, 0, 1);
            setView(getRocket());
            setMirror(true);
            hv = -6;
        }
        
        @Override
        protected final void onStepEnemy() {
            timer++;
            if (timer > 10) {
                Player.puff(this, -1, 4);
                timer = 0;
            }
            final Player player = getPlayer();
            if (player == null) {
                return;
            }
            final Panple pos = getPosition();
            final float y = pos.getY(), py = player.getPosition().getY() + Player.CENTER_Y;
            if (y > (py + 3)) {
                pos.addY(-1);
            } else if (y < (py - 3)) {
                pos.addY(1);
            }
            pos.addX(hv);
            if (pos.getX() < -16) {
                destroy();
            }
        }
        
        @Override
        public void onAttack(final Player player) {
            super.onAttack(player);
            EnemyProjectile.burstEnemy(this, 4);
            destroy();
        }
        
        @Override
        protected final int getDamage() {
            return Projectile.POWER_MEDIUM;
        }
        
        private final static Panmage getRocket() {
            return (img = Boss.getImage(img, "final/Rocket", null, null, null));
        }
    }
    
    private final static class WagonMortar extends AiProjectile {
        private static Panimation anim = null;
        private int dropX;
        
        private WagonMortar(final Panctor src, final int dropX) {
            super(src, 111, 83, 0, Player.VEL_PROJECTILE, BotsnBoltsGame.finalImages, Projectile.POWER_MEDIUM);
            setRot(1);
            getPosition().setZ(BotsnBoltsGame.DEPTH_ENEMY_BACK_2);
            setView(getAnim());
            this.dropX = dropX;
        }
        
        @Override
        public final void onAllOob(final AllOobEvent event) {
            if (dropX < 0) {
                return;
            } else if (getPosition().getY() < 0) {
                super.onAllOob(event);
            } else {
                final int x = dropX;
                dropX = -1;
                Pangine.getEngine().addTimer(this, 15, new TimerListener() {
                    @Override public final void onTimer(final TimerEvent event) {
                        setRot(3);
                        getVelocity().setY(-Player.VEL_PROJECTILE);
                        final Panple pos = getPosition();
                        pos.setX(x);
                        pos.setZ(BotsnBoltsGame.DEPTH_PROJECTILE);
                    }});
            }
        }
        
        @Override
        protected final void onOutOfView() {
        }
        
        private final Panimation getAnim() {
            if (anim != null) {
                return anim;
            }
            final Pangine engine = Pangine.getEngine();
            final Panimation base = (Panimation) getView();
            final Panframe frames[] = base.getFrames(), frame1 = frames[1];
            final Panframe flip = engine.createFrame(frame1.getId() + ".rot", frame1.getImage(), frame1.getDuration(), 0, true, false);
            anim = engine.createAnimation(base.getId() + ".rot", frames[0], flip);
            return anim;
        }
    }
    
    protected final static int SAUCER_OFF_X = 16, SAUCER_H = 43;
    protected final static Panple SAUCER_O = new FinPanple2(32, 0);
    protected final static Panple SAUCER_MIN = getMin(SAUCER_OFF_X);
    protected final static Panple SAUCER_MAX = getMax(SAUCER_OFF_X, SAUCER_H);
    
    protected abstract static class BaseSaucer extends Boss {
        private final static Panmage[] imgs = new Panmage[4];
        
        protected BaseSaucer() {
            super(SAUCER_OFF_X, SAUCER_H, 0, 0);
            setView(getSaucer());
            setMirror(true);
        }
        
        @Override
        protected final boolean isConstructNeeded() {
            return false;
        }
        
        @Override
        protected final boolean isMirrorable() {
            return false;
        }
        
        protected final void startState(final byte state, final int waitTimer) {
            startState(state, waitTimer, getStill());
        }
        
        protected final void startStateIndefinite(final byte state) {
            startStateIndefinite(state, getStill());
        }
        
        @Override
        protected void renderView(final Panderer renderer) {
            final Panlayer layer = getLayer();
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY();
            final int zoff = getZoff();
            final boolean mirror = isMirror();
            renderer.render(layer, getStill(), x, y, BotsnBoltsGame.DEPTH_ENEMY_BACK + zoff, 0, mirror, false);
            renderer.render(layer, Final.getCoat(), x, y + 14, BotsnBoltsGame.DEPTH_ENEMY_BACK_2 + zoff, 0, mirror, false);
        }
        
        protected int getZoff() {
            return 0;
        }
        
        protected final static Panmage getSaucer() {
            return getSaucer(0);
        }
        
        protected final static Panmage getSaucer(final int i) {
            Panmage img = imgs[i];
            if (img == null) {
                img = getImage(img, "final/Saucer" + (i + 1), SAUCER_O, SAUCER_MIN, SAUCER_MAX);
                imgs[i] = img;
            }
            return img;
        }
        
        protected final static Panmage getSaucerAnimated() {
            final int frameDuration = 5;
            return getSaucer(((int) (Pangine.getEngine().getClock() % (4 * frameDuration))) / frameDuration);
        }
    }
    
    protected final static class WagonSaucer extends BaseSaucer {
        private final FinalWagon wagon;
        
        protected WagonSaucer(final FinalWagon wagon) {
            this.wagon = wagon;
        }
        
        @Override
        protected final boolean isHealthMeterNeeded() {
            return false;
        }
        
        @Override
        protected final boolean pickState() {
            return true;
        }

        @Override
        protected final boolean continueState() {
            return true;
        }
        
        protected final void separate() {
            wagon.saucer = null;
            final FinalSaucer saucer = new FinalSaucer();
            wagon.nextSaucer = saucer;
            saucer.getPosition().set(getPosition());
            saucer.setMirror(isMirror());
            addActor(saucer);
            destroy();
            clearPlayerExtras();
        }
        
        @Override
        protected final void onHurt(final Projectile prj) {
            wagon.onHurt(prj);
        }
        
        @Override
        protected final Panmage getStill() {
            return getSaucer();
        }
    }
    
    protected final static class FinalSaucer extends BaseSaucer {
        private final static byte STATE_INTRO_RISE = 1;
        private final static byte STATE_INTRO_DESTROY = 2; // isVulnerable depends on intro states being less than later states
        private final static byte STATE_TRACTOR_BEAM_SEEK = 3;
        private final static byte STATE_TRACTOR_BEAM = 4;
        private final static byte STATE_BLAST = 5;
        private final static byte STATE_FLY_U = 6;
        private final static byte STATE_FLY_W = 7;
        private final static byte STATE_DEFEATED = 8;
        private final static byte STATE_COAT = 9;
        private final static byte STATE_ARMOR = 10;
        private final static byte STATE_FLOOR_OPEN = 11;
        private final static byte STATE_CEILING_CLOSE = 12;
        private final static int TRACTOR_BEAM_TOP = 11;
        private final static int SPEED = 4;
        private final static int FINAL_BOSS_X = 22;
        private final static int FINAL_BOSS_Y = 3;
        private final static Panmage[] tractorBeams = new Panmage[3];
        private int introCount = 0;
        private float maxY;
        private Panctor lastProjectile = null;
        private int tractorBeamX = 0;
        private int tractorBeamSize = 0;
        private boolean edge = false;
        private boolean readyForFinalBattle = false;
        private int floorIndex = 0;
        private Panctor finalActor = null;
        private Final finalBoss = null;
        
        @Override
        protected final boolean pickState() {
            if (introCount == 0) {
                startIntroRise();
            } else if (introCount == 1) {
                startTractorBeamSeek();
                introCount = 2;
            } else if (introCount == 2) {
                startBlast();
                introCount = 3;
            } else if (introCount == 3) {
                startFlyU();
                introCount = 4;
            } else {
                if (edge) {
                    final int r = rand(2);
                    if (r == 0) {
                        pickWeapon();
                    } else {
                        pickFlight();
                    }
                } else {
                    pickWeapon();
                }
            }
            return true;
        }
        
        private final void pickWeapon() {
            final int r = Mathtil.randi(0, 1999);
            if (r < 1000) {
                startTractorBeamSeek();
            } else {
                startBlast();
            }
        }
        
        private final void pickFlight() {
            if (introCount == 4) {
                startFlyW();
                introCount = 5;
                return;
            }
            final int r = Mathtil.randi(0, 1999);
            if (r < 1000) {
                startFlyU();
            } else {
                startFlyW();
            }
        }
        
        private final void startIntroRise() {
            startState(STATE_INTRO_RISE, 80);
            introCount = 1;
        }
        
        private final void startIntroDestroy() {
            startState(STATE_INTRO_DESTROY, 90);
        }
        
        private final void pickDirection() {
            final float x = getPosition().getX();
            if (x < 40) {
                hv = 1;
            } else if (x > 344) {
                hv = -1;
            } else {
                hv = (getPlayerX() < x) ? -1 : 1;
            }
        }
        
        private final void startTractorBeamSeek() {
            pickDirection();
            startStateIndefinite(STATE_TRACTOR_BEAM_SEEK);
        }
        
        private final void startTractorBeam() {
            tractorBeamX = BotsnBoltsGame.tm.getContainerColumn(getPosition().getX());
            startState(STATE_TRACTOR_BEAM, 90);
        }
        
        private final void startBlast() {
            pickDirection();
            startStateIndefinite(STATE_BLAST);
            final float x = getPosition().getX();
            if ((x < 40) || (x > 343)) {
                shootCharged();
            }
        }
        
        private final void pickFlyDirection() {
            pickDirection();
            hv *= 4;
        }
        
        private final void startFlyU() {
            pickFlyDirection();
            startStateIndefinite(STATE_FLY_U);
        }
        
        private final void startFlyW() {
            pickFlyDirection();
            startStateIndefinite(STATE_FLY_W);
        }
        
        private final void startDefeated() {
            startStateIndefinite(STATE_DEFEATED);
            clearPlayerExtras();
        }

        @Override
        protected final boolean continueState() {
            switch (state) {
                case STATE_INTRO_RISE :
                    startIntroDestroy();
                    break;
                case STATE_INTRO_DESTROY :
                    setPlayerActive(true);
                    break;
                case STATE_COAT :
                    startArmor();
                    break;
                default :
                    startStill();
            }
            return true;
        }
        
        @Override
        protected final boolean onWaiting() {
            updateLastProjectile();
            switch (state) {
                case STATE_INTRO_RISE :
                    onIntroRising();
                    break;
                case STATE_INTRO_DESTROY :
                    onIntroDestroying();
                    break;
                case STATE_TRACTOR_BEAM_SEEK :
                    onTractorBeamSeeking();
                    break;
                case STATE_TRACTOR_BEAM :
                    onTractorBeaming();
                    break;
                case STATE_BLAST :
                    onBlasting();
                    break;
                case STATE_FLY_U :
                    onFlyingU();
                    break;
                case STATE_FLY_W :
                    onFlyingW();
                    break;
                case STATE_DEFEATED :
                    onDefeated();
                    break;
                case STATE_COAT :
                    break;
                case STATE_ARMOR :
                    break;
                case STATE_FLOOR_OPEN :
                    onFloorOpen();
                    break;
                case STATE_CEILING_CLOSE :
                    onCeilingClose();
                    break;
            }
            return true;
        }
        
        private final void onIntroRising() {
            health = 0;
            getPosition().addY(1);
        }
        
        private final void onIntroDestroying() {
            health = 0;
            if ((waitTimer % 15) == 0) {
                shootSelfDestruct();
            }
        }
        
        private final void onTractorBeamSeeking() {
            final float x = getPosition().getX();
            final int rx = Math.round(x);
            if (((rx % 16) == 15) && (Math.abs(x - getPlayerX()) < 16)) {
                hv = 0;
                startTractorBeam();
            } else {
                seek();
            }
        }
        
        private final boolean seek() {
            final boolean ret;
            if (Math.abs(hv) == 1) {
                final int m = getX() % SPEED;
                if (m != 3) {
                    if (hv < 0) {
                        ret = seek(-(m + 1));
                    } else {
                        ret = seek(3 - m);
                    }
                } else {
                    ret = true;
                }
                hv *= SPEED;
            } else {
                ret = seek(hv);
            }
            return ret;
        }
        
        private final boolean seek(final int hv) {
            if (addX(hv) != X_NORMAL) {
                edge = true;
                startStill();
                return false;
            }
            edge = false;
            return true;
        }
        
        private final void onTractorBeaming() {
            if (waitTimer > 81) {
                tractorBeamSize++;
                setBehavior(BotsnBoltsGame.TILE_TRACTOR_BEAM);
            } else if (waitTimer < 8) {
                setBehavior(Tile.BEHAVIOR_OPEN);
                tractorBeamSize--;
            }
        }
        
        private final void onBlasting() {
            if (seek()) {
                final int rx = getX();
                if ((rx > 32) && (rx < 336) && ((rx % 48) == 23)) {
                    shootCharged();
                }
            } else {
                shootCharged();
            }
        }
        
        private final void onFlyingU() {
            final int i = WAIT_INDEFINITE - waitTimer - 1; // 0 - 79
            final int divisor = 5, maxSpeed = 6;
            final int v = (i < 40) ? -Math.max(maxSpeed - (i / divisor), 0) : Math.max(maxSpeed - ((79 - i) / divisor), 0);
            getPosition().addY(v);
            fly();
        }
        
        private final void onFlyingW() {
            final int i = WAIT_INDEFINITE - waitTimer - 1; // 0 - 79
            final int baseSpeed = 18, maxSpeed = 8;
            final int v;
            if (i < 20) {
                v = -Math.min(maxSpeed, Math.max(baseSpeed - i, 0));
            } else if (i < 30) {
                v = i - 20;
            } else if (i < 40) {
                v = 39 - i;
            } else if (i < 50) {
                v = -(i - 40);
            } else if (i < 60) {
                v = -(59 - i);
            } else {
                v = Math.min(maxSpeed, Math.max(baseSpeed - (79 - i), 0));
            }
            getPosition().addY(v);
            fly();
        }
        
        private final void fly() {
            if (!seek(hv)) {
                getPosition().setY(maxY);
                startStill();
            }
        }
        
        private final void onDefeated() {
            final Panple pos = getPosition();
            final float dstX = 352, dstY = 34;
            float x = pos.getX(), y = pos.getY();
            boolean readyX = false, readyY = false;
            if (Math.abs(x - dstX) < 3) {
                x = dstX;
                readyX = true;
            } else if (x < dstX) {
                x += 3;
            } else if (x > dstX) {
                x -= 3;
            }
            if (Math.abs(y - dstY) < 3) {
                y = dstY;
                readyY = true;
            } else if (y < dstY) {
                y += 3;
            } else if (y > dstY) {
                y -= 3;
            }
            pos.set(x, y);
            if (readyX && readyY && readyForFinalBattle) {
                startCoat();
            } else {
                burst();
            }
        }
        
        private final void startCoat() {
            startState(STATE_COAT, 30);
            for (int i = 0; i < 10; i++) {
                burst();
            }
            finalActor = Final.newFinalActor(FINAL_BOSS_X, FINAL_BOSS_Y, true);
            setVisible(false);
        }
        
        private final void startArmor() {
            startStateIndefinite(STATE_ARMOR);
            healthMeter.destroy();
            finalBoss = new Final(FINAL_BOSS_X, FINAL_BOSS_Y);
            finalBoss.setView(BotsnBoltsGame.finalImages.basicSet.jump);
            Final.newCoatThrown(finalActor);
            finalBoss.startHandler(new JumpAndWaitHandler(new Runnable() {
                @Override public final void run() {
                    startFloorOpen();
                }}));
            finalActor.destroy();
        }
        
        protected final void startFloorOpen() {
            finalBoss.startHandler(new WaitHandler(
                new Runnable() {
                    @Override final public void run() {
                        onCeilingClose();
                    }},
                new Runnable() {
                    @Override final public void run() {
                        startCeilingClose();
                    }
                }));
            startStateIndefinite(STATE_FLOOR_OPEN);
        }
        
        private final void onFloorOpen() {
            if (floorIndex == 22) {
                destroy();
                final TileMap tm = BotsnBoltsGame.tm;
                for (int i = 1; i < 23; i++) {
                    for (int j = 0; j < 3; j++) {
                        tm.setBehavior(i, j, Tile.BEHAVIOR_OPEN);
                    }
                }
            } else if ((floorIndex % 2) == 1) {
                final TileMap tm = BotsnBoltsGame.tm;
                final int fi = floorIndex / 2;
                final int xLeft = 11 - fi, xRight = 12 + fi;
                for (int j = 0; j < 3; j++) {
                    tm.setBackground(xLeft, j, null);
                    tm.setBackground(xRight, j, null);
                }
            }
            floorIndex++;
        }
        
        private final void startCeilingClose() {
            floorIndex = 0;
            startStateIndefinite(STATE_CEILING_CLOSE);
        }
        
        private final void onCeilingClose() {
            if (state != STATE_CEILING_CLOSE) {
                return;
            } else if (floorIndex == 22) {
                startFinalBattle();
            } else if ((floorIndex % 2) == 1) {
                final TileMap tm = BotsnBoltsGame.tm;
                final int fi = floorIndex / 2;
                final int xLeft = 1 + fi, xRight = 22 - fi;
                final Tile tile = RoomLoader.getTile(((fi % 5) == 3) ? 'b' : '@');
                final boolean shadow = (xLeft % 2) == 0;
                tm.setTile(xLeft, 13, tile);
                tm.setTile(xRight, 13, tile);
                tm.setTile(xLeft, 12, RoomLoader.getTile(shadow ? 'D' : 'd'));
                tm.setTile(xRight, 12, RoomLoader.getTile(shadow ? 'd' : 'D'));
            }
            floorIndex++;
        }
        
        private final void startFinalBattle() {
            finalBoss.health = HudMeter.MAX_VALUE;
            finalBoss.startStill();
        }
        
        private final void burst() {
            final Panple pos = getPosition();
            FinalWagon.burst(pos.getX() + Mathtil.randi(-31, 31), pos.getY() + Mathtil.randi(1, 40));
        }
        
        private final void setBehavior(final byte b) {
            final int y = TRACTOR_BEAM_TOP - tractorBeamSize;
            setBehavior(tractorBeamX, y, b);
            setBehavior(tractorBeamX + 1, y, b);
        }
        
        private final void setBehavior(final int x, final int y, final byte b) {
            final TileMap tm = BotsnBoltsGame.tm;
            if (tm.isBad(x, y)) {
                return;
            } else if (Tile.getBehavior(tm.getTile(x, y)) == Tile.BEHAVIOR_SOLID) {
                return;
            }
            tm.setBehavior(x, y, b);
        }
        
        private final void clearTractorBeam() {
            while (tractorBeamSize > 0) {
                setBehavior(Tile.BEHAVIOR_OPEN);
                tractorBeamSize--;
            }
        }
        
        private final void startBattle() {
            health = HudMeter.MAX_VALUE;
            maxY = getPosition().getY();
            startStill();
        }
        
        private final void shootCharged() {
            initCharged(new AiProjectile(this, 0, 0, 0, -VEL_PROJECTILE, BotsnBoltsGame.finalImages, Projectile.POWER_MAXIMUM));
        }
        
        private final void shootSelfDestruct() {
            initCharged(new Projectile(getPlayer(), BotsnBoltsGame.finalImages, Player.SHOOT_CHARGE, this, 0, -VEL_PROJECTILE, Projectile.POWER_MAXIMUM));
        }
        
        private final void initCharged(final Panctor prj) {
            final Panple pos = getPosition();
            prj.getPosition().set(pos.getX() + 1, pos.getY(), BotsnBoltsGame.DEPTH_CARRIER);
            prj.setRot(3);
            if (lastProjectile != null) {
                lastProjectile.getPosition().setZ(BotsnBoltsGame.DEPTH_PROJECTILE);
            }
            lastProjectile = prj;
        }
        
        private final void updateLastProjectile() {
            if (lastProjectile == null) {
                return;
            }
            final Panple pos = lastProjectile.getPosition();
            if (pos.getY() > 140) {
                return;
            }
            pos.setZ(BotsnBoltsGame.DEPTH_PROJECTILE);
        }
        
        @Override
        public final boolean isVulnerable() {
            return (state > STATE_INTRO_DESTROY) || (state == STATE_STILL);
        }
        
        @Override
        public final boolean isHarmful() {
            return state != STATE_DEFEATED;
        }
        
        @Override
        protected final void onBossDefeat() {
            if (state == STATE_DEFEATED) {
                return;
            }
            clearTractorBeam();
            startDefeated();
            setPlayerActive(false);
        }
        
        @Override
        protected final boolean isDefeatOrbNeeded() {
            return false;
        }
        
        @Override
        protected PowerUp pickAward(final Player player) {
            return (state == STATE_DEFEATED) ? null : new VictoryDisk(player, this);
        }
        
        @Override
        public final void onAward(final Player player) {
            player.startScript(new LeftAi(32.0f), new Runnable() {
                @Override public final void run() {
                    readyForFinalBattle = true;
                }});
        }
        
        @Override
        protected final float getDropX() {
            return getPlayerX();
        }
        
        @Override
        public final boolean isDestroyedAfterDefeat() {
            return false;
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            super.renderView(renderer);
            final Panlayer layer = getLayer();
            final Panmage timg = getTractorBeam();
            final float tx = tractorBeamX * BotsnBoltsGame.DIM;
            for (int j = 0; j < tractorBeamSize; j++) {
                final float ty = (TRACTOR_BEAM_TOP - j - 1) * BotsnBoltsGame.DIM;
                renderer.render(layer, timg, tx, ty, BotsnBoltsGame.DEPTH_ENEMY_BACK_2);
                renderer.render(layer, timg, tx + 16, ty, BotsnBoltsGame.DEPTH_ENEMY_BACK_2, 0, 0, 16, 16, 0, true, true);
            }
        }
        
        @Override
        protected final int getZoff() {
            return (state == STATE_INTRO_RISE) ? 0 : 2;
        }
        
        @Override
        protected final Panmage getStill() {
            return getSaucerAnimated();
        }
        
        private final static Panmage getTractorBeam() {
            final int frameDuration = 3;
            final int frameIndex = ((int) (Pangine.getEngine().getClock() % (4 * frameDuration))) / frameDuration;
            final int imgIndex = (frameIndex < 3) ? frameIndex : 1;
            return getTractorBeam(imgIndex);
        }
        
        private final static Panmage getTractorBeam(final int i) {
            Panmage img = tractorBeams[i];
            if (img == null) {
                img = getImage(null, "final/TractorBeam" + (i + 1), null, null, null);
                tractorBeams[i] = img;
            }
            return img;
        }
    }
    
    protected final static class Final extends AiBoss {
        private static Panmage coat = null;
        private final static Panmage[] coatThrown = new Panmage[3];
        
        protected Final(final int x, final int y) {
            super(BotsnBoltsGame.finalImages, x, y);
            handlers.add(new SpreadAttackJumpHandler());
            handlers.add(new ChargeAttackJumpsHandler());
            handlers.add(new RapidAttackRunHandler());
            handlers.add(new GrappleHandler());
        }
        
        @Override
        protected final void addUpgrades(final Set<Upgrade> upgrades) {
            upgrades.add(Profile.UPGRADE_SPREAD);
            upgrades.add(Profile.UPGRADE_CHARGE);
            upgrades.add(Profile.UPGRADE_RAPID);
            upgrades.add(Profile.UPGRADE_GRAPPLING_BEAM);
        }
        
        @Override
        protected final int initStillTimer() {
            return Mathtil.randi(12, 15);
        }
        
        private final static Panmage getCoat() {
            return (coat = getImage(coat, "final/FinalCoat", BotsnBoltsGame.og, null, null));
        }
        
        private final static Panmage getCoatThrown(final int i) {
            Panmage img = coatThrown[i];
            if (img == null) {
                img = getImage(img, "final/CoatThrown" + (i + 1), BotsnBoltsGame.og, null, null);
                coatThrown[i] = img;
            }
            return img;
        }
        
        protected final static Panctor newFinalActor(final int x, final int y, final boolean mirror) {
            final Panctor actor = new Panctor();
            actor.setView(getCoat());
            final Panple pos = actor.getPosition();
            BotsnBoltsGame.tm.savePositionXy(pos, x, y);
            pos.setZ(BotsnBoltsGame.DEPTH_ENEMY);
            actor.setMirror(mirror);
            BotsnBoltsGame.addActor(actor);
            return actor;
        }
        
        protected final static Pandy newCoatThrown(final Panctor src) {
            final Pandy coat = new Pandy(gTuple) {
                private int timer = 1;
                @Override public final void onStep(final StepEvent event) {
                    super.onStep(event);
                    if (!isInView()) {
                        destroy();
                        return;
                    }
                    changeView(getCoatThrown(timer / 3));
                    timer++;
                    if (timer > 8) {
                        timer = 0;
                    }
                }
            };
            coat.setView(getCoatThrown(0));
            coat.setMirror(src.isMirror());
            final Panple pos = coat.getPosition(), srcPos = src.getPosition();
            pos.set(srcPos.getX() + 2, srcPos.getY() - 3, BotsnBoltsGame.DEPTH_ENEMY_BACK);
            coat.getVelocity().set(2, 1);
            BotsnBoltsGame.addActor(coat);
            return coat;
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
        
        protected final Panframe getFrame(final Panframe[] frames) {
            return getFrame(frames, frameIndex);
        }
        
        protected final Panframe getFrame(final Panframe[] frames, final int frameIndex) {
            return Rock.getFrame(frames, this, frameIndex);
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
        protected final float v;
        protected final int hv;
        
        protected Jump(final byte state, final Panmage img, final float v, final int hv) {
            this.state = state;
            this.img = img;
            this.v = v;
            this.hv = hv;
        }
    }
}
