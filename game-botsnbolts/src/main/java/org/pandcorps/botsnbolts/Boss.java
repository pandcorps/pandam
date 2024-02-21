/*
Copyright (c) 2009-2024, Andrew M. Martin
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

import org.pandcorps.botsnbolts.BotsnBoltsGame.*;
import org.pandcorps.botsnbolts.Chr.*;
import org.pandcorps.botsnbolts.Extra.*;
import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.botsnbolts.PowerUp.*;
import org.pandcorps.botsnbolts.Profile.*;
import org.pandcorps.botsnbolts.RoomLoader.*;
import org.pandcorps.botsnbolts.Story.*;
import org.pandcorps.core.*;
import org.pandcorps.core.col.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.game.BaseGame.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.boundary.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.pandax.visual.*;

public abstract class Boss extends Enemy implements SpecBoss {
    protected final static String RES_BOSS = BotsnBoltsGame.RES + "boss/";
    protected final static String RES_CHR = BotsnBoltsGame.RES + "chr/";
    protected final static byte STATE_STILL = 0;
    private final static int DAMAGE = 4;
    private final static int LEFT_X = 16;
    private final static int RIGHT_X = 367;
    private final static float DROP_X = MID_X;
    private final static byte TAUNT_NEEDED = 0;
    private final static byte TAUNT_STARTED = 1;
    private final static byte TAUNT_WAITING = 2;
    private final static byte TAUNT_FINISHED = 3;
    private final static int WAIT_INDEFINITE = Integer.MAX_VALUE;
    private final static int DEFAULT_STILL_MIN = 15;
    private final static int DEFAULT_STILL_MAX = 30;
    private final static Panple scratchPanple = new ImplPanple(0, 0, 0);
    private final static List<Integer> scratchInts = new ArrayList<Integer>(3);
    
    private boolean initializationNeeded = true;
    protected int waitTimer = 0; // Will be assigned by startStillBeforeTaunt()
    protected int waitCounter = 0;
    protected byte state = 0;
    protected Queue<Jump> pendingJumps = null;
    private boolean jumping = false;
    protected byte tauntState = isTauntNeeded() ? TAUNT_NEEDED : TAUNT_FINISHED;
    protected Runnable tauntFinishHandler = null;
    protected int moves = -1;
    protected static boolean clipping = true;
    private static boolean delaying = false;
    protected static boolean dropping = false;
    protected HudMeter healthMeter = null;
    private static int prevRand = -1;
    private static int prevPrevRand = -1;
    private static int overrideRand = -1;
    protected static boolean endangeredDuringMove = false;
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
    
    protected static void initStatic() {
        overrideRand = -1;
        endangeredDuringMove = false;
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
        initStatic();
        stopMusic();
        if (isHealthMeterNeeded() && isDuringGameplay()) {
            healthMeter = addHealthMeter();
            if (isHealthMeterInitiallyHidden()) {
                healthMeter.setVisible(false);
            }
        }
    }
    
    protected final static boolean isDuringGameplay() {
        return Panscreen.get() instanceof BotsnBoltsGame.BotsnBoltsScreen;
    }
    
    protected boolean isHealthMeterNeeded() {
        return true;
    }
    
    protected boolean isHealthMeterInitiallyHidden() {
        return false;
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
            waitCounter++;
            return onWaiting();
        } else if (isStill()) {
            if (taunting()) {
                return getTauntingReturnValue();
            } else if (pollPendingJumps()) {
                return false;
            }
            return runPickState();
        }
        return continueState();
    }
    
    protected boolean runPickState() {
        moves++;
        return pickState();
    }
    
    protected boolean isTauntNeeded() {
        return true;
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
    
    protected boolean getTauntingReturnValue() {
        return false;
    }
    
    protected void taunt() {
        waitTimer = 0;
    }
    
    protected final boolean isTauntFinished() {
        return tauntState == TAUNT_FINISHED;
    }
    
    protected final boolean isTauntFinishedOrWaiting() {
        return (tauntState == TAUNT_FINISHED) || (tauntState == TAUNT_WAITING); // Waiting is done with the taunt, waiting for the health bar to fill
    }
    
    protected final boolean finishTaunt() {
        if (tauntState == TAUNT_FINISHED) {
            return false;
        }
        startIntro();
        tauntState = TAUNT_WAITING; // TAUNT_FINISHED will be set when health bar is full
        if (tauntFinishHandler != null) {
            tauntFinishHandler.run();
            tauntFinishHandler = null;
        }
        return true;
    }
    
    protected void onTauntFinished() {
    }
    
    private final void startIntro() {
        final String[] introMessages;
        if (isFirstEncounter()) {
            introMessages = getIntroMessages();
        } else {
            final String[] rematchMessages = getRematchMessages();
            introMessages = (rematchMessages == null) ? getIntroMessages() : rematchMessages;
        }
        if ((introMessages == null) || !isDuringGameplay()) {
            finishIntro();
        } else {
            onDialogueStart();
            dialogue(getPortrait(), getDialogueYOff(), new Runnable() { @Override public final void run() {
                finishIntro();
            }}, introMessages);
        }
    }
    
    private final boolean isFirstEncounter() {
        final BotLevel level = RoomLoader.level;
        return (level != null) && getClass().getSimpleName().equals(level.bossClassName);
    }
    
    protected String[] getIntroMessages() {
        return null;
    }
    
    protected String[] getRematchMessages() {
        return null;
    }
    
    protected void onDialogueStart() {
    }
    
    protected int getDialogueYOff() {
        return 0;
    }
    
    private final void finishIntro() {
        health = HudMeter.MAX_VALUE;
        finishIntroStatic();
    }
    
    protected final static void finishIntroStatic() {
        if (isDuringGameplay() && !isLevelMusicPlayedDuringBoss()) {
            BotsnBoltsGame.musicBoss.startMusic();
        }
    }
    
    protected final static boolean isLevelMusicPlayedDuringBoss() {
        return RoomLoader.variables.containsKey("levelMusicPlayedDuringBoss");
    }
    
    @Override
    public final void onHealthMaxDisplayReached() {
        tauntState = TAUNT_FINISHED;
        onTauntFinished();
        waitTimer = 0;
        if (isPlayerActivationAfterHealthMaxNeeded()) {
            setPlayerActive(true);
        }
    }
    
    protected boolean isPlayerActivationAfterHealthMaxNeeded() {
        return true;
    }
    
    protected boolean isStill() {
        return state == STATE_STILL;
    }
    
    protected boolean onStepBoss() {
        return false;
    }
    
    @Override
    protected final void onLanded() {
        onBossLandedAny();
        if (hasPendingJumps()) {
            startStill(5);
        } else if (!onBossLanded()) {
            startStill();
        }
    }
    
    //@OverrideMe
    protected void onBossLandedAny() {
    }
    
    //@OverrideMe
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
            BotsnBoltsGame.musicVictory.startSound();
            Pangine.getEngine().addTimer(player, 150, new TimerListener() {
                @Override public final void onTimer(final TimerEvent event) {
                    player.dematerialize(Player.levelSelectHandler);
                }});
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
        if (isDefeatOrbNeeded()) {
            return;
        }
        spawnAward(powerUp);
    }
    
    protected final void spawnAward(final PowerUp powerUp) {
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
    
    protected final static int randForceDifferent(final int max) {
        prevPrevRand = prevRand;
        return rand(max);
    }
    
    protected final static int rand(final int max) {
        if (max <= 1) {
            throw new IllegalArgumentException("Called rand(" + max + "), input must be at least 2");
        } else if (overrideRand >= 0) {
            try {
                return addToRandHistory(overrideRand);
            } finally {
                overrideRand = -1;
            }
        }
        while (true) {
            final int r = (Mathtil.randi(0, (max * 1000) - 1)) / 1000;
            if ((r != prevRand) || (r != prevPrevRand)) {
                return addToRandHistory(r);
            }
        }
    }
    
    protected final static int addToRandHistory(final int r) {
        prevPrevRand = prevRand;
        prevRand = r;
        return r;
    }
    
    protected final static <E> E rand(final List<E> list) {
        return list.get(rand(list.size()));
    }
    
    protected final void startState(final byte state, final int waitTimer, final Panmage img) {
        this.state = state;
        this.waitTimer = waitTimer;
        waitCounter = 0;
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
        final float x = getPosition().getX(), px = getNearestPlayerX();
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
        onContinueJumps();
        addPendingJump(getStateJumps(), getJump(), getJumpsV(), getJumpsH());
    }
    
    //@OverrideMe
    protected void onContinueJumps() {
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
    
    protected final int getY() {
        return Math.round(getPosition().getY());
    }
    
    @Override
    public int pickResponseToDanger() {
        return -1;
    }
    
    protected final static boolean isPlayerDangerous() {
        for (final PlayerContext pc : BotsnBoltsGame.pcs) {
            final Profile prf = PlayerContext.getProfile(pc);
            if ((prf != null) && (prf.shootMode == Player.SHOOT_RAPID)) {
                return true;
            }
        }
        return false;
    }
    
    protected final static void handleHurtIfDangerous(final SpecBoss boss) {
        if (isPlayerDangerous()) {
            overrideRand = boss.pickResponseToDanger();
            endangeredDuringMove = true;
        }
    }
    
    protected final static boolean isPlayerInvincible() {
        for (final PlayerContext pc : BotsnBoltsGame.pcs) {
            final Player player = PlayerContext.getPlayer(pc);
            if ((player != null) && player.isInvincible()) {
                return true;
            }
        }
        return false;
    }
    
    protected final static int initStillTimer() {
        return initStillTimer(DEFAULT_STILL_MIN, DEFAULT_STILL_MAX);
    }
    
    protected final static int initStillTimer(int min, int max) {
        if (endangeredDuringMove) {
            endangeredDuringMove = false;
            return Math.min(2, min);
        }
        if (isPlayerDangerous()) {
            min = Math.min(min, 3);
            max = Math.min(max, 10);
        }
        return Mathtil.randi(min, max);
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
    
    protected void startStill(final Panmage image) {
        startState(STATE_STILL, initStillTimer(), image);
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
    
    protected final static Panmage getImageChr(final Panmage img, final String name, final Panple o, final Panple min, final Panple max) {
        return getImage(img, "boss.", RES_CHR, name, o, min, max);
    }
    
    protected Panmage getPortrait() {
        return RoomLoader.getPortrait(RoomLoader.levelMap.get(getClass().getSimpleName()));
    }
    
    protected abstract Panmage getStill();
    
    @Override
    protected void onHurt(final SpecPlayerProjectile prj) {
        final int oldHealth = health;
        super.onHurt(prj);
        handleHurtIfDangerous(this);
        if (state == STATE_STILL) {
            final int damage = oldHealth - health;
            if (damage > 0) {
                waitTimer = adjustWaitTimerOnHurt(waitTimer, damage);
            }
        }
    }
    
    protected final static int adjustWaitTimerOnHurt(final int waitTimer, final int damage) {
        if (waitTimer < 1) {
            return waitTimer;
        } else if (isPlayerDangerous()) {
            return 1;
        }
        return Math.max(1, waitTimer - (damage * 10));
    }
    
    @Override
    public final void onDefeat(final Player player) {
        onBossDefeat();
        if (isDefeatOrbNeeded()) {
            Player.defeatOrbs(this, BotsnBoltsGame.defeatOrbBoss);
            Player.startDefeatTimer(new Runnable() {
                @Override public final void run() {
                    spawnAward(pickAward(player));
                }});
        }
        stopMusic();
        if (isDestroyEnemiesNeeded()) {
            destroyEnemies();
        }
        RoomLoader.levelVariables.put(getClass().getSimpleName(), "");
    }
    
    protected final static void stopMusic() {
        if (!isLevelMusicPlayedDuringBoss()) {
            Pangine.getEngine().getAudio().stopMusic();
        }
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
            // Turtle release enemies that can drop batteries; don't leave them around for the battle with Transient
            if (((actor instanceof Enemy) && !(actor instanceof Boss)) || (actor instanceof EnemyProjectile) || (actor instanceof EnemySpawner)
                    || (actor instanceof AiBomb) || (actor instanceof Flare) || (actor instanceof Battery)) {
                actor.destroy();
            }
        }
    }
    
    protected final static void setPlayerActive(final boolean active) {
        BotsnBoltsGame.runPlayers(new PlayerRunnable() {
            @Override
            public final void run(final Player player) {
                player.active = active;
                if (!active) {
                    player.prepareForScript();
                }
            }
        });
    }
    
    protected final static boolean isPlayerBetween(final int xMin, final int yMin, final int xMax, final int yMax) {
        for (final PlayerContext pc : BotsnBoltsGame.pcs) {
            final Player player = PlayerContext.getPlayer(pc);
            if (player != null) {
                final Panple pos = player.getPosition();
                final float x = pos.getX(), y = pos.getY();
                if ((x < xMin) || (x >= xMax) || (y < yMin) || (y >= yMax)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    protected final static boolean isPlayerXBetween(final int xMin, final int xMax) {
        for (final PlayerContext pc : BotsnBoltsGame.pcs) {
            final Player player = PlayerContext.getPlayer(pc);
            if (player != null) {
                final float x = player.getPosition().getX();
                if ((x < xMin) || (x >= xMax)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    protected final static boolean isPlayerStopped() {
        for (final PlayerContext pc : BotsnBoltsGame.pcs) {
            final Player player = PlayerContext.getPlayer(pc);
            if ((player != null) && player.isStopped()) {
                return true;
            }
        }
        return false;
    }
    
    protected final static void stopPlayers() {
        for (final PlayerContext pc : BotsnBoltsGame.pcs) {
            pc.player.stop();
        }
    }
    
    protected final static void resumePlayers() {
        for (final PlayerContext pc : BotsnBoltsGame.pcs) {
            pc.player.resume();
        }
    }
    
    protected final static void clearPlayerExtras() {
        BotsnBoltsGame.runPlayers(new PlayerRunnable() {
            @Override
            public final void run(final Player player) {
                Panctor.destroy(player.spring);
            }
        });
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
    
    protected final static Panmage getPlayerPortrait() {
        return BotsnBoltsGame.getActivePlayerContext().pi.portrait;
    }
    
    protected final static void dialogue(final Panmage bossPortrait, final int yOff, final Runnable finishHandler, final String... msgs) {
        setPlayerActive(false);
        final Panmage playerPortrait = getPlayerPortrait();
        DialogueBox box = null;
        boolean portraitLeft = false;
        for (final String msg : msgs) {
            final Panmage portrait = portraitLeft ? playerPortrait : bossPortrait;
            if (box == null) {
                box = Story.dialogue(portrait, portraitLeft, yOff, msg).add();
            } else {
                box = box.setNext(portrait, portraitLeft, msg);
            }
            portraitLeft = !portraitLeft;
        }
        box.setFinishHandler(finishHandler);
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
            Player.startDefeatTimer(null);
            burst(BotsnBoltsGame.enemyBurst, 342, 382, 56, 184, 10);
        }
        
        private final static void burst(final Panimation anm, final int minX, final int maxX, final int minY, final int maxY, final int n) {
            final Panctor src = BotsnBoltsGame.tm;
            Projectile.burst(src, anm, Mathtil.randi(minX, maxX), Mathtil.randi(minY, maxY));
            if (n > 1) {
                Pangine.getEngine().addTimer(src, 3, new TimerListener() {
                    @Override public final void onTimer(final TimerEvent event) {
                        burst(anm, minX, maxX, minY, maxY, n - 1);
                    }});
            }
        }
        
        @Override
        protected final boolean isDefeatOrbNeeded() {
            return false;
        }
        
        @Override
        public final void onAward(final Player player) {
            clearPlayerExtras();
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
            return true;
        }
        
        @Override
        protected final boolean pickState() {
            return true;
        }

        @Override
        protected final boolean continueState() {
            return true;
        }
        
        @Override
        protected boolean onStepBoss() {
            if (Panctor.isDestroyed(bot)) {
                spawnTimer++;
                if (spawnTimer >= 60) {
                    spawn();
                }
            }
            return false;
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
    
    protected final static class Turtle extends Boss implements StepEndListener, RoomAddListener {
        protected final static byte STATE_DRAG = 1;
        protected final static byte STATE_STEP_FAR = 2;
        protected final static byte STATE_STEP_NEAR = 3;
        //protected final static byte STATE_EMIT_FROM_POWER_SOURCE = 4; // Emit a simple projectile from the underside's power source, happens randomly during any state
        protected final static byte STATE_SPAWN_PESTS = 5; // Spawn enemies to distract Player
        protected final static byte STATE_LOB_TO_LURE = 6; // Lob projectiles to far side of screen, slower each time, to lure Player closer
        protected final static byte STATE_BITE = 7; // Lunge forward to bite if Player is in range
        protected final static byte STATE_CRUSH = 8; // Drop down if Player is underneath
        protected final static byte STATE_BLOCK = 8; // Lower head to defend underside
        protected final static byte STATE_CHARGE = 9; // Eyes start to glow while gathering energy to charge beam attack
        protected final static byte STATE_BEAM = 10;
        protected final static int WAIT_BEAM = 48;
        protected final static int WAIT_WALK = 24;
        protected final static int SPEED_WALK = 3;
        protected final static int SPEED_BITE = 6;
        protected final static int BITE_X_MIN = -86;
        protected final static int BITE_Y_MIN = -32;
        protected final static int BITE_Y_MAX = 56;
        
        private static Panmage still = null;
        private static Panmage head = null;
        private static Panmage headAttack = null;
        private static Panmage sphere = null;
        private static Panmage foot = null;
        private final static Panmage[] eyes = new Panmage[9];
        private static Panmage beamStart = null;
        private static Panmage beam = null;
        private static Panmage shatter = null;
        //bodyPosition = Panctor.getPosition();
        final Panple farFootPosition = new ImplPanple(); // Can just render feet
        final Panple nearFootPosition = new ImplPanple();
        final Panple headOffsets = new ImplPanple(); // Don't just render, use separate actors with own hit boxes to block shots to chest
        final List<TurtleHeadComponent> headComponents = new ArrayList<TurtleHeadComponent>(3); // Head and neck
        private float floorY;
        private float defaultY;
        private final float defaultYSpeed = 2;
        private final float defaultHeadOffsetX = -32;
        private final float defaultHeadOffsetY = 16;
        private final float defaultHeadSpeed = 3;
        private Panple stepPosition = null;
        private float stepMinY = 0;
        private float stepVerticalVelocity = 0;
        private final float stepVerticalAcceleration = -0.325f;
        private int initialStepsRequired = 2;
        private float targetY;
        private float ySpeed = defaultYSpeed;
        private float targetHeadOffsetX = defaultHeadOffsetX;
        private float targetHeadOffsetY = defaultHeadOffsetY;
        private float headSpeed = defaultHeadSpeed;
        private int emitTimer = 60;
        private int damageScore = 0;
        
        protected Turtle(final Segment seg) {
            super(0, 0, seg);
            setView(getStill());
            setMirror(false);
            final Panple pos = getPosition();
            pos.addX(-8);
            pos.setZ(BotsnBoltsGame.DEPTH_ENEMY);
            addHeadComponent(false, BotsnBoltsGame.DEPTH_ENEMY_FRONT); // Neck sphere closest to body
            addHeadComponent(false, BotsnBoltsGame.DEPTH_ENEMY_FRONT_2);
            addHeadComponent(true, BotsnBoltsGame.DEPTH_ENEMY_FRONT_3); // Head
            floorY = pos.getY();
            final float x = pos.getX() + 144, footY = floorY - 1;
            farFootPosition.set(x - 21, footY);
            nearFootPosition.set(x + 16, footY);
            headOffsets.set(defaultHeadOffsetX, defaultHeadOffsetY);
            defaultY = floorY + 25;
            pos.set(x, defaultY);
            targetY = defaultY;
            setVisible(false);
        }
        
        @Override
        public final void onRoomAdd(final RoomAddEvent event) {
            enableOverlay();
        }
        
        private final void addHeadComponent(final boolean head, final float z) {
            final TurtleHeadComponent headComponent = new TurtleHeadComponent(head);
            headComponent.getPosition().setZ(z);
            addActor(headComponent);
            headComponents.add(headComponent);
        }
        
        private final TurtleHeadComponent getHeadActor() {
            return headComponents.get(headComponents.size() - 1);
        }
        
        private final void setHeadAttack() {
            final TurtleHeadComponent head = getHeadActor();
            head.changeView(getHeadAttack());
            head.attacked = false;
            head.eye = null;
        }
        
        private final void setHeadMouthClosed() {
            final TurtleHeadComponent head = getHeadActor();
            head.changeView(getHead());
            head.eye = null;
        }
        
        private final static int getOverlayOffZ() {
            return BotsnBoltsGame.DEPTH_OVERLAY - BotsnBoltsGame.DEPTH_FG;
        }
        
        private final void burst() {
            addBurstShatterEffects();
            setBurstBuildingTiles();
        }
        
        private final void addBurstShatterEffects() {
            BotsnBoltsGame.fxCrumble.startSound();
            final Panlayer layer = getLayer();
            final Panmage img = getShatter();
            for (int y = 2; y <= 10; y++) {
                final int xStart, xStop;
                if (y < 9) {
                    xStart = 19; xStop = 22;
                } else {
                    xStart = 20; xStop = 21;
                }
                for (int x = xStart; x <= xStop; x++) {
                    Player.newDiver(layer, false, img, x * 16, y * 16, -6, 6, false, false, true);
                }
            }
        }
        
        private final void setBurstBuildingTiles() {
            setVisible(true);
            final TileMap tm = BotsnBoltsGame.tm;
            final Tile leftEdge = getWallTile(1, 2);
            final Tile rightEdge = getOverlayTile(4, 2);
            final Tile black = tm.getTile(BotsnBoltsGame.imgMap[1][0], null, Tile.BEHAVIOR_SOLID);
            final int vertLimit = 9;
            for (int y = 2; y < vertLimit; y++) {
                tm.setTile(19, y, leftEdge);
                tm.setTile(20, y, black);
                tm.setTile(21, y, black);
                if (y > 4) {
                    tm.setTile(22, y, rightEdge);
                }
            }
            tm.setTile(21, vertLimit, black);
            tm.setTile(22, vertLimit, getOverlayTile(4, 1));
            tm.setTile(21, vertLimit + 1, getOverlayTile(3, 2));
            tm.setTile(21, vertLimit + 2, getOverlayTile(3, 1));
            tm.setTile(20, vertLimit + 2, getWallTile(2, 1));
            tm.setTile(20, vertLimit + 1, getWallTile(2, 2));
            tm.setTile(19, vertLimit, getWallTile(1, 1));
            tm.setTile(20, vertLimit, black);
            tm.setTile(22, 4, getOverlayTile(4, 3));
            tm.setTile(23, 4, getOverlayTile(5, 3));
            tm.setTile(23, 3, getOverlayTile(5, 4));
            tm.setTile(23, 2, getOverlayTile(5, 5));
            tm.setTile(22, 3, black);
            tm.setTile(22, 2, black);
        }
        
        private final static Tile getWallTile(final int fgX, final int fgY) {
            return BotsnBoltsGame.tm.getTile(null, BotsnBoltsGame.imgMap[fgY][fgX], Tile.BEHAVIOR_SOLID);
        }
        
        private final static Tile getOverlayTile(final int fgX, final int fgY) {
            return BotsnBoltsGame.tm.getTile(BotsnBoltsGame.imgMap[1][0], new AdjustedTileMapImage(BotsnBoltsGame.imgMap[fgY][fgX], getOverlayOffZ(), 0, false, false), Tile.BEHAVIOR_SOLID);
        }
        
        private final void enableOverlay() {
            final TileMap tm = BotsnBoltsGame.tm;
            final int overlayOffZ = getOverlayOffZ();
            for (int i = 0; i < 3; i++) {
                final int x = 23 - i;
                final Object fg = DynamicTileMap.getRawForeground(tm.getTile(x, 3));
                final Tile tile = tm.getTile(null, new AdjustedTileMapImage((TileMapImage) fg, overlayOffZ, 0, false, false), Tile.BEHAVIOR_SOLID);
                for (int y = 2; y < 14; y++) {
                    tm.setTile(x, y, tile);
                }
            }
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
        protected final void taunt() {
            setPlayerActive(false);
            burst();
            startDrag();
        }
        
        protected final void startWalk(final byte state, final Panple stepPosition) {
            startState(state, WAIT_WALK, getStill());
            this.stepPosition = stepPosition;
            stepMinY = stepPosition.getY();
            stepVerticalVelocity = 4;
        }
        
        protected final void onWalking() {
            stepPosition.addX(-SPEED_WALK);
            float y = stepPosition.getY() + stepVerticalVelocity;
            if (y < stepMinY) {
                y = stepMinY;
            } else {
                stepVerticalVelocity += stepVerticalAcceleration;
            }
            stepPosition.setY(y);
        }
        
        protected final void startDrag() {
            startWalk(STATE_DRAG, getPosition());
        }
        
        protected final void onDragging() {
            onWalking();
        }
        
        protected final void startStepFar() {
            startWalk(STATE_STEP_FAR, farFootPosition);
        }
        
        protected final void onSteppingFar() {
            onWalking();
        }
        
        protected final void startStepNear() {
            startWalk(STATE_STEP_NEAR, nearFootPosition);
        }
        
        protected final void onSteppingNear() {
            onWalking();
        }
        
        protected final void finishWalkingStep() {
            stepPosition.setY(stepMinY);
        }
        
        @Override
        protected final void onBossDefeat() {
            //Player.startDefeatTimer(null);
            Fort.burst(BotsnBoltsGame.enemyBurst, 342, 382, 56, 184, 10); //TODO range
        }
        
        @Override
        protected final boolean isDefeatOrbNeeded() {
            return false;
        }
        
        @Override
        public final void onAward(final Player player) {
            clearPlayerExtras();
            player.startScript(new LeftAi(32.0f), new Runnable() {
                @Override public final void run() {
                    //new Warp(getNextBoss(20, 3));
                }});
        }
        
        /*
        protected AiBoss getNextBoss(final int x, final int y) {
            return new Transient(x, y);
        }
        */
        
        @Override
        protected final boolean getTauntingReturnValue() {
            return true;
        }
        
        @Override
        protected final boolean onWaiting() {
            adjustY();
            adjustHead();
            if (state == STATE_DRAG) {
                onDragging();
                return true;
            } else if (state == STATE_STEP_FAR) {
                onSteppingFar();
                return true;
            } else if (state == STATE_STEP_NEAR) {
                onSteppingNear();
                return true;
            } else if (state == STATE_BITE) {
                onBiting();
            } else if (state == STATE_BLOCK) {
                onBlocking();
            } else if (state == STATE_SPAWN_PESTS) {
                onSpawningPests();
            } else if (state == STATE_LOB_TO_LURE) {
                onLobbingToLure();
            } else if (state == STATE_CHARGE) {
                onCharging();
                return true; // Don't emit during this attack
            } else if (state == STATE_BEAM) {
                onBeaming();
                return true; // Don't emit during this attack
            }
            emitTimer--;
            if (emitTimer <= 0) {
                CyanEnemy.shoot(this, 17, 24, false);
                emitTimer = Mathtil.randi(15, 60);
            }
            return true;
        }
        
        private final void adjustY() {
            if (state <= STATE_STEP_NEAR) {
                return;
            }
            final Panple pos = getPosition();
            final float y = pos.getY();
            if (targetY > y) {
                pos.setY(Math.min(targetY, y + ySpeed));
            } else if (targetY < y) {
                pos.setY(Math.max(targetY, y - ySpeed));
            }
        }
        
        private final void adjustHead() {
            if (state == STATE_BITE) {
                return;
            }
            headOffsets.setX(adjustHeadOffset(headOffsets.getX(), targetHeadOffsetX));
            headOffsets.setY(adjustHeadOffset(headOffsets.getY(), targetHeadOffsetY));
        }
        
        private final float adjustHeadOffset(final float curr, final float target) {
            if (curr < target) {
                return Math.min(curr + headSpeed, target);
            } else if (curr > target) {
                return Math.max(curr - headSpeed, target);
            }
            return curr;
        }
        
        protected final void reset() {
            targetY = defaultY;
            ySpeed = defaultYSpeed;
            targetHeadOffsetX = defaultHeadOffsetX;
            targetHeadOffsetY = defaultHeadOffsetY;
            headSpeed = defaultHeadSpeed;
            setHeadMouthClosed();
        }
        
        @Override
        protected final boolean pickState() {
            reset();
            final float x = getPosition().getX(), nearestPlayerX = getNearestPlayerX();
            if (nearestPlayerX > x) {
                startCrush();
                return true;
            } else if (nearestPlayerX > (x + BITE_X_MIN + 4)) {
                startBite();
                return true;
            } else if (damageScore > 60) {
                startBlock();
                return true;
            }
            final int r = rand(3);
            if (r == 0) {
                startSpawnPests();
            } else if (r == 1) {
                startLobToLure();
            } else if (r == 2) {
                startCharge();
            }
            return true;
        }
        
        protected final void startSpawnPests() {
            startState(STATE_SPAWN_PESTS, 60, getStill());
            targetHeadOffsetX = -24;
            targetHeadOffsetY = 24;
        }
        
        protected final void onSpawningPests() {
            setHeadAttack();
            if ((waitCounter % 20) == 1) {
                addActorFromMouth(new WingedEnemy());
                if (waitCounter > 10) {
                    targetHeadOffsetX = Mathtil.randi(-32, -16);
                    targetHeadOffsetY = Mathtil.randi(16, 32);
                    headSpeed = 1;
                }
            }
        }
        
        private final void addActorFromMouth(final Panctor actor) {
            final Panple pos = getHeadActor().getPosition();
            actor.getPosition().set(pos.getX() - 8, pos.getY() - 1, BotsnBoltsGame.DEPTH_ENEMY_FRONT_4);
            addActor(actor);
        }
        
        protected final void startLobToLure() {
            startState(STATE_LOB_TO_LURE, 60, getStill());
            targetHeadOffsetX = -38;
            targetHeadOffsetY = 10;
            headSpeed = 5;
        }
        
        protected final void onLobbingToLure() {
            setHeadAttack();
            if (waitCounter == 10) {
                headSpeed = 1;
                lob(-6, 5);
            } else if (waitCounter == 25) {
                lob(-5, 5.5f);
            } else if (waitCounter == 40) {
                lob(-4, 6);
            } else if (waitCounter == 55) {
                lob(-3, 7);
            }
        }
        
        private final void lob(final int hv, final float v) {
            addActorFromMouth(new BounceEnemy(hv, v));
            targetHeadOffsetX += 6;
            targetHeadOffsetY += 6;
        }
        
        protected final void startBite() {
            startState(STATE_BITE, 30, getStill());
            targetY = floorY;
            ySpeed = 3;
            setHeadAttack();
        }
        
        protected final void onBiting() {
            final TurtleHeadComponent head = getHeadActor();
            if (head.attacked) {
                startStill();
                return;
            }
            final Player player = getNearestPlayer();
            if (player == null) {
                return;
            }
            final Panple playerPos = player.getPosition(), headPos = head.getPosition();
            if (playerPos.getX() < headPos.getX()) {
                headOffsets.setX(Math.max(BITE_X_MIN, headOffsets.getX() - SPEED_BITE));
            }
            final float playerY = playerPos.getY(), headY = headPos.getY();
            if (playerY < (headY - 16)) {
                headOffsets.setY(Math.max(BITE_Y_MIN, headOffsets.getY() - SPEED_BITE));
            } else if (playerY > (headY + 16)) {
                headOffsets.setY(Math.min(BITE_Y_MAX, headOffsets.getY() + SPEED_BITE));
            }
        }
        
        protected final void startCrush() {
            startState(STATE_CRUSH, 15, getStill());
            targetY = floorY;
            ySpeed = 5;
        }
        
        protected final void startBlock() {
            startState(STATE_BLOCK, 60, getStill());
            targetY = floorY + 8;
            ySpeed = 3;
            targetHeadOffsetX = -40;
            targetHeadOffsetY = -56;
            headSpeed = SPEED_BITE;
            damageScore = 0;
        }
        
        protected final void onBlocking() {
            damageScore = 0;
        }
        
        protected final void startCharge() {
            startState(STATE_CHARGE, 64, getStill());
            targetHeadOffsetX = -46;
            targetHeadOffsetY = -50;
        }
        
        protected final void onCharging() {
            final int i = waitCounter - 1;
            final TurtleHeadComponent head = getHeadActor();
            if (i < 32) {
                head.eye = getEye(i / 4);
            } else if (i < 48) {
                head.eye = getEye(7);
            } else {
                head.eye = getEye((i % 2) < 1 ? 8 : 7);
            }
        }
        
        protected final void startBeam() {
            startState(STATE_BEAM, WAIT_BEAM, getStill());
            setHeadAttack();
        }
        
        protected final void onBeaming() {
            if (waitCounter == 1) {
                final TurtleHeadComponent head = getHeadActor();
                head.setMirror(true);
                new TurtleRayProjectile(head, 11, -9);
                head.setMirror(false);
            }
        }

        @Override
        protected final boolean continueState() {
            switch (state) {
                case STATE_DRAG :
                    finishWalkingStep();
                    startStepNear();
                    break;
                case STATE_STEP_NEAR :
                    finishWalkingStep();
                    startStepFar();
                    break;
                case STATE_STEP_FAR :
                    finishWalkingStep();
                    initialStepsRequired--;
                    if (initialStepsRequired > 0) {
                        startDrag();
                    } else {
                        setPlayerActive(true);
                        tauntState = TAUNT_FINISHED;
                        health = HudMeter.MAX_VALUE;
                        startStill();
                    }
                    break;
                case STATE_CHARGE :
                    startBeam();
                    break;
                default :
                    reset();
                    startStill();
            }
            return true;
        }
        
        @Override
        protected final void startStill() {
            startStill(initStillTimer(30, 45));
        }
        
        @Override
        protected int getDamage() {
            return (state == STATE_CRUSH) ? 8 : DAMAGE;
        }
        
        @Override
        protected void onHurt(final SpecPlayerProjectile prj) {
            final int oldHealth = health;
            super.onHurt(prj);
            final int damage = oldHealth - health;
            damageScore += (damage * 20);
        }
        
        @Override
        protected boolean onStepBoss() {
            return false;
        }
        
        @Override
        public final void onStepEnd(final StepEndEvent event) {
            if (damageScore > 0) {
                damageScore--;
            }
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY();
            final float hbx = x + 22, hby = y + 64;
            final int numHeadComponents = headComponents.size();
            final float hox = headOffsets.getX(), hoy = headOffsets.getY();
            //TODO Pieces move relative to each other while walking, tweak order of floating point operations and use rounding to get consistent offsets
            for (int i = 0; i < numHeadComponents; i++) {
                float m = (i + 1);
                m /= numHeadComponents;
                headComponents.get(i).getPosition().set(hbx + (hox * m), hby + (hoy * m));
            }
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            super.renderView(renderer);
            final Panlayer layer = getLayer();
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY();
            final Panmage foot = getFoot();
            final Panmage sphere = getSphere();
            
            final float farFootX = farFootPosition.getX(), farFootY = farFootPosition.getY();
            final float farShoulderX = x + 4, shoulderY = y + 12;
            for (int i = 0; i < 2; i++) {
                final float mx = (i + 1.0f) / 3.0f;
                final float my = (i + 1.0f) * 2.0f / 5.0f;
                renderer.render(layer, sphere, farShoulderX + ((farFootX - farShoulderX) * mx), shoulderY + ((farFootY - shoulderY) * my),
                        BotsnBoltsGame.DEPTH_ENEMY_BG - (2 * i), 0, 0, 32, 32, 1, false, true);
            }
            renderer.render(layer, foot, farFootX, farFootY, BotsnBoltsGame.DEPTH_ENEMY_BG_3, 0, 0, 32, 32, 0, false, false);
            
            final float nearFootX = nearFootPosition.getX(), nearFootY = nearFootPosition.getY();
            final float nearShoulderX = x + 41;
            for (int i = 0; i < 2; i++) {
                final float mx = (i + 1.0f) / 3.0f;
                final float my = (i + 1.0f) * 2.0f / 5.0f;
                renderer.render(layer, sphere, nearShoulderX + ((nearFootX - nearShoulderX) * mx), shoulderY + ((nearFootY - shoulderY) * my),
                        BotsnBoltsGame.DEPTH_ENEMY_FRONT + (2 * i), 0, 0, 32, 32, 1, false, true);
            }
            renderer.render(layer, foot, nearFootX, nearFootY, BotsnBoltsGame.DEPTH_ENEMY_FRONT_3, 0, 0, 32, 32, 0, false, false);
        }
        
        @Override
        protected final boolean isVulnerableToProjectile(final SpecPlayerProjectile prj) {
            return prj.getPosition().getY() < (getPosition().getY() + 48.0f);
        }
        
        @Override
        protected final Panmage getStill() {
            if (still != null) {
                return still;
            }
            return (still = getImage(still, "turtle/TurtleBody", null, null, new FinPanple2(128, 88)));
        }
        
        protected final static Panmage getHead() {
            return (head = getImage(head, "turtle/TurtleHead", BotsnBoltsGame.CENTER_32, BotsnBoltsGame.MIN_32, BotsnBoltsGame.MAX_32));
        }
        
        protected final static Panmage getHeadAttack() {
            return (headAttack = getImage(headAttack, "turtle/TurtleHeadAttack", BotsnBoltsGame.CENTER_32, BotsnBoltsGame.MIN_32, BotsnBoltsGame.MAX_32));
        }
        
        protected final static Panmage getSphere() {
            return (sphere = getImage(sphere, "turtle/TurtleSphere", BotsnBoltsGame.CENTER_32, BotsnBoltsGame.MIN_32, BotsnBoltsGame.MAX_32));
        }
        
        protected final static Panmage getFoot() {
            return (foot = getImage(foot, "turtle/TurtleFoot", BotsnBoltsGame.CENTER_32, BotsnBoltsGame.MIN_32, BotsnBoltsGame.MAX_32)); // Only rendered, not an actor, so max doesn't matter
        }
        
        protected final static Panmage getEye(final int i) {
            Panmage img = eyes[i];
            if (img == null) {
                img = getImage(img, "turtle/TurtleEye" + (i + 1), BotsnBoltsGame.CENTER_8, BotsnBoltsGame.MIN_8, BotsnBoltsGame.MAX_8);
                eyes[i] = img;
            }
            return img;
        }
        
        protected final static Panmage getBeamStart() {
            return (beamStart = getImage(beamStart, "turtle/TurtleBeamStart", BotsnBoltsGame.CENTER_16, BotsnBoltsGame.MIN_16, BotsnBoltsGame.MAX_16));
        }
        
        protected final static Panmage getBeam() {
            return (beam = getImage(beam, "turtle/TurtleBeam", BotsnBoltsGame.CENTER_16, BotsnBoltsGame.MIN_16, BotsnBoltsGame.MAX_16));
        }
        
        protected final static Panmage getShatter() {
            return (shatter = getImage(shatter, "turtle/TurtleShatter", BotsnBoltsGame.CENTER_16, BotsnBoltsGame.MIN_16, BotsnBoltsGame.MAX_16));
        }
    }
    
    protected final static class TurtleHeadComponent extends Enemy {
        private final boolean head;
        private boolean attacked = false;
        private Panmage eye = null;
        
        protected TurtleHeadComponent(final boolean head) {
            super(0, 32, 0, 0, 1);
            setView(head ? Turtle.getHead() : Turtle.getSphere());
            this.head = head;
        }
        
        @Override
        protected final boolean onStepCustom() {
            return true;
        }
        
        @Override
        protected final boolean isVulnerableToProjectile(final SpecPlayerProjectile prj) {
            return false;
        }
        
        @Override
        protected int getDamage() {
            return DAMAGE;
        }
        
        @Override
        public boolean onAttack(final Player player) {
            if (!super.onAttack(player)) {
                return false;
            }
            if (head && (eye == null)) {
                changeView(Turtle.getHead());
                attacked = true;
            }
            return true;
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            super.renderView(renderer);
            if (eye == null) {
                return;
            }
            final Panple pos = getPosition();
            renderer.render(getLayer(), eye, pos.getX() - 4, pos.getY() + 1, BotsnBoltsGame.DEPTH_ENEMY_FRONT_4, 0, 0, 8, 8, 0, false, false);
        }
    }
    
    protected final static class TurtleRayProjectile extends RayProjectile {
        private static Panmage head1 = null, head2 = null;
        private static Panmage tail1 = null, tail2 = null;
        
        protected TurtleRayProjectile(final Enemy src, final int ox, final int oy) {
            super(src, ox, oy, Turtle.WAIT_BEAM - 2, 16);
            BotsnBoltsGame.fxEnemyAttack.startSound();
        }
        
        @Override
        protected final int getDamage() {
            return 7;
        }
        
        private final boolean isFrame1() {
            return (index % 4) < 2;
        }
        
        @Override
        protected final Panmage getHead() {
            if (isFrame1()) {
                return (head1 = getImage(head1, "turtle/TurtleBeam1", null, null, null));
            } else {
                return (head2 = getImage(head2, "turtle/TurtleBeam2", null, null, null));
            }
        }
        
        @Override
        protected final Panmage getTail() {
            if (isFrame1()) {
                return (tail1 = getImage(tail1, "turtle/TurtleBeamStart1", null, null, null));
            } else {
                return (tail2 = getImage(tail2, "turtle/TurtleBeamStart2", null, null, null));
            }
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
        protected final String[] getIntroMessages() {
            return new String[] {
                "You shouldn't be here.  You know what they say.  If you play with lava, you get burned.",
                "I think they say fire.",
                "Fine.  If you play with lava, you catch on fire.",
                "No, that's still not-",
                "I'll incinerate you!"
            };
        }
        
        @Override
        protected final String[] getRematchMessages() {
            return new String[] { "I'll incinerate you!" };
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
            } else { // 2 (also response to danger)
                startJump();
            }
            return false;
        }
        
        @Override
        public final int pickResponseToDanger() {
            return 2;
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
            BotsnBoltsGame.fxDefeat.startSound();
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
            return getImage();
        }
        
        protected final static Panmage getImage() {
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
        private boolean soundNeeded = true;
        
        protected LavaBall(final VolcanoBot src, final float t) {
            super(getCurrentImage(), src, 11, 34, 0, 16, gTuple);
            this.src = src;
            this.t = t;
            BotsnBoltsGame.fxDefeat.startSound();
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
                } else if (soundNeeded && isInView()) {
                    BotsnBoltsGame.fxDefeat.startSound();
                    soundNeeded = false;
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
        protected final static byte STATE_SHOOT_UP = 6;
        protected final static int WAIT_SHOOT = 30;
        protected final static int WAIT_SLIDE = 20;
        protected static Panmage still = null;
        protected static Panmage aim = null;
        protected static Panmage aimDiag = null;
        protected static Panmage aimUp = null;
        protected static Panmage jump = null;
        protected static Panmage fall = null;
        protected static Panmage slide1 = null;
        protected static Panmage slide2 = null;
        protected static Panmage trail = null;
        
        protected HailBot(final Segment seg) {
            super(HAIL_OFF_X, HAIL_H, seg);
        }
        
        @Override
        protected final void taunt() {
            startShootUp();
        }
        
        @Override
        protected final String[] getIntroMessages() {
            return new String[] {
                    "Stones of ice will fall from the sky.  Do you think that you can survive such a plague?",
                    "I will try to stop you, even if my survival is not certain.",
                    "Then you are a fool.  The clouds are gathering.  And so it begins!"
                };
        }
        
        @Override
        protected final String[] getRematchMessages() {
            return new String[] { "It begins again!" };
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
                } else if (state == STATE_SHOOT_UP) {
                    new HailCluster(this, 7, 32, 0, VEL_PROJECTILE);
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
            final int r = rand(4);
            if (r == 0) {
                startShoot();
            } else if (r == 1) {
                startShootDiag();
            } else if (r == 2) {
                startJump();
            } else { // 3 (also response to danger)
                startSlide();
            }
            return false;
        }
        
        @Override
        public final int pickResponseToDanger() {
            return 3;
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
        
        protected final void startShootUp() {
            startState(STATE_SHOOT_UP, WAIT_SHOOT, getAimUp());
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
            return getImage();
        }
        
        protected final static Panmage getImage() {
            return (still = getHailImage(still, "hailbot/HailBot"));
        }
        
        protected final static Panmage getAim() {
            return (aim = getHailImage(aim, "hailbot/HailBotAim"));
        }
        
        protected final static Panmage getAimDiag() {
            return (aimDiag = getHailImage(aimDiag, "hailbot/HailBotAimDiag"));
        }
        
        protected final static Panmage getAimUp() {
            return (aimUp = getHailImage(aimUp, "hailbot/HailBotAimUp"));
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
            BotsnBoltsGame.fxEnemyAttack.startSound();
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
            BotsnBoltsGame.fxCrumble.startSound();
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
        protected final static byte STATE_TAUNT = 7;
        protected final static int WAIT_SHOOT = 30;
        protected static Panmage still = null;
        protected static Panmage aim = null;
        protected static Panmage aimHorizontal = null;
        protected static Panmage crouch = null;
        protected static Panmage curl = null;
        protected static Panmage roll1 = null;
        protected static Panmage roll2 = null;
        protected static Panmage jump = null;
        protected static Panmage taunt1 = null;
        protected static Panmage taunt2 = null;
        private final static Rotator rots = new RollRotator();
        private final static Panframe[] frames = new Panframe[Rotator.numFrames];
        
        protected RockslideBot(final Segment seg) {
            super(ROCKSLIDE_OFF_X, ROCKSLIDE_H, seg);
        }
        
        @Override
        protected final void taunt() {
            BotsnBoltsGame.fxCrumble.startSound();
            startState(STATE_TAUNT, 32, getTaunt1());
        }
        
        @Override
        protected final String[] getIntroMessages() {
            return new String[] {
                "So Dr. Root thinks that a little runt like you can defeat me?",
                "That's right.",
                "Pathetic.  I will throw your broken body off the side of this mountain.  I'll destroy you!"
            };
        }
        
        @Override
        protected final String[] getRematchMessages() {
            return new String[] { "I'll destroy you!" };
        }
        
        @Override
        protected final int getDialogueYOff() {
            return -80;
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
            } else if (state == STATE_TAUNT) {
                onTaunting();
            }
            return false;
        }
        
        private final void onTaunting() {
            if (waitCounter == 13) {
                setView(getStill());
            } else if (waitCounter == 19) {
                BotsnBoltsGame.fxCrumble.startSound();
                setView(getTaunt2());
            }
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
                case STATE_TAUNT :
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
            return getImage();
        }
        
        protected final static Panmage getImage() {
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
        
        protected final static Panmage getTaunt1() {
            return (taunt1 = getRockslideImage(taunt1, "rockslidebot/RockslideBotTaunt1"));
        }
        
        protected final static Panmage getTaunt2() {
            return (taunt2 = getRockslideImage(taunt2, "rockslidebot/RockslideBotTaunt2"));
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
            BotsnBoltsGame.fxEnemyAttack.startSound();
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
        protected final boolean onWall(final byte xResult) {
            shatter();
            return true;
        }
        
        protected final void shatter() {
            BotsnBoltsGame.fxCrumble.startSound();
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
        protected final static byte STATE_TAUNT_WAIT = 5;
        protected final static int WAIT_STRIKE = 15;
        protected final static int WAIT_BURST = 31;
        private final static int VEL_JUMPS = 10;
        protected static Panmage still = null;
        protected static Panmage jump = null;
        protected static Panmage strike = null;
        protected static Panmage fall = null;
        protected static Panmage burst = null;
        protected static Panmage taunt = null;
        
        protected LightningBot(final Segment seg) {
            super(LIGHTNING_OFF_X, LIGHTNING_H, seg);
        }
        
        @Override
        protected final void taunt() {
            startBurst();
        }
        
        @Override
        protected final String[] getIntroMessages() {
            return new String[] {
                "Hey!  Let's play a game.  Let's see who can absorb the most electricity without frying a circuit board.",
                "I have a surge suppression rating of 10 billion joules.",
                "That's... actually pretty good.  But not good enough.  It's time to play!"
            };
        }
        
        @Override
        protected final String[] getRematchMessages() {
            return new String[] { "It's time to play!" };
        }
        
        @Override
        protected final boolean onWaiting() {
            if (state == STATE_STRIKE) {
                if (waitTimer == (WAIT_STRIKE - 1)) {
                    new Lightning(this);
                    BotsnBoltsGame.fxDefeat.startSound();
                }
                return true;
            } else if (state == STATE_BURST) {
                if (waitTimer == (WAIT_BURST - 1)) {
                    final Panmage burst = LightningBurst.getBurst();
                    LightningBurst.start = Pangine.getEngine().getClock();
                    new LightningBurst(burst, this, -32, 15, 29, 0);
                    new LightningBurst(burst, this, 30, 14, 29, 2);
                    new LightningBurst(burst, this, 0, 44, 29, 3);
                    BotsnBoltsGame.fxDefeat.startSound();
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
            final int r = (moves == 0) ? addToRandHistory(0) : rand(3);
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
            } else if ((state == STATE_BURST) && !isTauntFinished()) {
                startState(STATE_TAUNT_WAIT, 15, getStill());
            } else if ((state == STATE_TAUNT_WAIT) && finishTaunt()) {
                startStateIndefinite(STATE_STILL, getTaunt());
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
            return getImage();
        }
        
        protected final static Panmage getImage() {
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
        
        protected final static Panmage getTaunt() {
            return (taunt = getLightningImage(taunt, "lightningbot/LightningBotTaunt"));
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
            super(BotsnBoltsGame.black, src, 10, 0, timer);
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
        protected final void taunt() {
            startDrill1();
        }
        
        @Override
        protected final String[] getIntroMessages() {
            return new String[] {
                    "The rifts that I tear through the surface of the earth will swallow its cities.  Nothing will remain standing.",
                    "I'm still standing, and I stand against you.",
                    "Then I'll crack the ground beneath you!"
                };
        }
        
        @Override
        protected final String[] getRematchMessages() {
            return new String[] { "I'll crack the ground beneath you!" };
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
            BotsnBoltsGame.fxCrumble.startSound();
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
                } else { // 2 (also response to danger)
                    startJump();
                }
            }
            return false;
        }
        
        @Override
        public final int pickResponseToDanger() {
            return 2;
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
            return getImage();
        }
        
        protected final static Panmage getImage() {
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
        private final boolean taunt;
        private int timer = 0;
        private int index = 0;
        private int distance = 0;
        
        protected Earthquake(final Boss src, final int ox, final int oy, final int remaining, final int maxIndex) {
            this(src, ox, oy, remaining, maxIndex, 0, !src.isTauntFinished());
        }
        
        protected Earthquake(final Panctor src, final int ox, final int oy, final int remaining, final int maxIndex, final int velX, final boolean taunt) {
            super(getSubImage(0), src, ox, oy, 0, 0);
            this.velX = (velX == 0) ? (8 * getMirrorMultiplier() * ox / Math.abs(ox)) : velX;
            this.remaining = remaining;
            this.maxIndex = maxIndex;
            this.taunt = taunt;
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            super.onStep(event);
            timer++;
            if ((timer % positionDuration) == 0) {
                getPosition().addX(velX);
                distance += Math.abs(velX);
                if ((distance == 16) && (remaining > 0)) {
                    new Earthquake(this, 16, 0, remaining - 1, Math.max(0, maxIndex - 1), velX, taunt);
                }
                if ((timer % frameDuration) == 0) {
                    index++;
                    if (index <= maxIndex) {
                        changeView(getSubImage(index));
                    } else if (taunt) {
                        destroy();
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
        protected final static byte STATE_TAUNT = 5;
        protected final static byte STATE_TAUNT_END = 6;
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
        protected final void taunt() {
            age = 0;
            startState(STATE_TAUNT, 15, getStill());
        }
        
        @Override
        protected final void onTauntFinished() {
            age = 0;
        }
        
        @Override
        protected final String[] getIntroMessages() {
            return new String[] {
                "Hello there.",
                "I'm going to stop you and all of Dr. Final's minions.",
                "Oh please.  Dr. Final might have built me, but I don't care about his agenda.  I go wherever the wind takes me.  I'm only helping him now because it's fun.",
                "How can you talk like that when lives are at stake?",
                "You're boring.  I want to fight you now!"
            };
        }
        
        @Override
        protected final String[] getRematchMessages() {
            return new String[] { "You again?  Fine.  Let's fight!" };
        }
        
        @Override
        protected final boolean onWaiting() {
            age++;
            if (state == STATE_JUMP) {
                changeView(getJump());
            } else if (((state == STATE_STILL) && isTauntFinished()) || (state == STATE_TAUNT)) {
                if (age < 2) {
                    return false;
                } else if (age < 4) {
                    changeView(getWhirlStart1());
                    return false;
                } else if (age < 6) {
                    changeView(getWhirlStart2());
                    return false;
                } else {
                    changeView(getWhirl());
                }
            } else if (state == STATE_LAUNCH) {
                final int index = WAIT_LAUNCH - waitTimer;
                if (index == 1) {
                    new Whirlwind(this, isTauntFinished() ? 300 : 45);
                } else if (index > 1) {
                    changeView(((index % 4) < 2) ? getLaunch1() : getLaunch2());
                }
            } else if (state == STATE_LAUNCH_END) {
                if (waitTimer == 2) {
                    changeView(getWhirlStart1());
                } else if (waitTimer == 0) {
                    age = 0;
                    if (finishTaunt()) {
                        startStateIndefinite(STATE_TAUNT_END, getStill());
                    } else {
                        startStill();
                    }
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
            } else if (r == 1) { // (also response to danger)
                startSpin();
            } else {
                startJump();
            }
            return false;
        }
        
        @Override
        public final int pickResponseToDanger() {
            return 1;
        }

        @Override
        protected final boolean continueState() {
            if (state == STATE_LAUNCH) {
                startLaunchEnd();
            } else if (state == STATE_SPIN) {
                getPosition().addX(getMirrorMultiplier());
                setMirror(!isMirror());
                startStill();
            } else if (state == STATE_TAUNT) {
                startLaunch();
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
            BotsnBoltsGame.fxDefeat.startSound();
            // If facing wrong direction, Cyclone Bot flies through the wall, never comes back, and player is stuck
            pickMirror();
            startState(STATE_SPIN, WAIT_SPIN, getSpinStart1());
        }
        
        private final void pickMirror() {
            setMirror(getX() > (BotsnBoltsGame.GAME_W / 2));
        }
        
        private final void startJump() {
            pickMirror();
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
                setMirror(false);
            } else if (Math.abs(xRight - x) < 5) {
                getPosition().setX(xRight);
                setMirror(true);
            }
        }

        @Override
        protected final Panmage getStill() {
            return getImage();
        }
        
        protected final static Panmage getImage() {
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
        protected final static int speed = 2;
        protected static Panmage wind1 = null;
        protected static Panmage wind2 = null;
        protected static Panmage wind3 = null;
        private final int minY;
        private int health = 3;
        
        protected Whirlwind(final Panctor src, final int duration) {
            super(getWind(duration), src, -2, 20, speed * src.getMirrorMultiplier(), 6, gTuple, duration);
            minY = isDuringGameplay() ? 52 : 84;
            BotsnBoltsGame.fxDefeat.startSound();
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
            if (pos.getY() < minY) {
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
            BotsnBoltsGame.fxDefeat.startSound();
            final int oldHealth = health, oldPower = prj.power;
            health -= oldPower;
            if (health <= 0) {
                onExpire();
                destroy();
            }
            prj.setPower(oldPower - oldHealth);
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
        protected final static byte STATE_TORPEDO_UP = 10;
        protected final static byte STATE_TORPEDO_DOWN = 11;
        protected final static int FILL_FRAME_DURATION = 3;
        protected final static int WAIT_FILL = (5 * FILL_FRAME_DURATION) + 15;
        protected final static int RAISE_FRAMES = 28;
        protected final static int RAISE_FRAME_DURATION = 3;
        protected final static int WAIT_RAISE = RAISE_FRAMES * RAISE_FRAME_DURATION;
        protected final static int WAIT_TORPEDO = 30;
        protected final static int WAIT_TORPEDO_VERTICAL = 6;
        protected final static int WATER_THRESHOLD_MAX = 12;
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
        protected static Panmage launchUp = null;
        protected static Panmage launchDown = null;
        protected static Panmage taunt = null;
        protected static Panmage whoosh = null;
        private final Valve valve;
        private final int xRight;
        private final int xLeft;
        private boolean fillNeeded = true; // Called after super constructor
        private Tile flowTile = null;
        private float prevY = 0;
        private boolean prevUnderwater = false;
        private int torpedoCount = 0;
        private int torpedoWait = 0;
        
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
        protected final void taunt() {
            startFill();
        }
        
        @Override
        protected final String[] getIntroMessages() {
            return new String[] {
                "You poor thing.  You have the weight of the world on your shoulders.  What a burden.  I could help you drown your sorrows.",
                "I'm putting an end to this.",
                "Give it your best shot!"
            };
        }
        
        @Override
        protected final String[] getRematchMessages() {
            return new String[] { "I'll give you a burial at sea!" };
        }
        
        @Override
        protected final void onDialogueStart() {
            setView(getStill());
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
                    new Torpedo(this, 14, 8, 0);
                }
                return true;
            } else if (state == STATE_TORPEDO_UP) {
                if (waitTimer == (WAIT_TORPEDO_VERTICAL - 1)) {
                    new Torpedo(this, -1, 32, 1);
                }
                return true;
            } else if (state == STATE_TORPEDO_DOWN) {
                if (waitTimer == (WAIT_TORPEDO_VERTICAL - 1)) {
                    new Torpedo(this, -4, -3, 3);
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
            } else if (index < 4) {
                changeView(getStillNormal());
            } else {
                changeView(getTaunt());
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
                    if (RoomLoader.getWaterTile() >= WATER_THRESHOLD_MAX) {
                        Panctor.destroy(valve);
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
            torpedoWait--;
            if ((torpedoCount > 0) && (torpedoWait <= 0)) {
                final Panple pos = getPosition();
                final float x = pos.getX(), y = pos.getY();
                for (final PlayerContext pc : BotsnBoltsGame.pcs) {
                    final Player player = PlayerContext.getPlayer(pc);
                    if (isDestroyed(player)) {
                        continue;
                    }
                    final Panple ppos = player.getPosition();
                    if (Math.abs(ppos.getX() - x) < 40) {
                        torpedoCount--;
                        torpedoWait = 8;
                        if (ppos.getY() < y) {
                            startTorpedoDown();
                        } else if (RoomLoader.getWaterTile() >= WATER_THRESHOLD_MAX) {
                            startTorpedoUp();
                        }
                        return;
                    }
                }
            }
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
            } else if ((health <= (HudMeter.MAX_VALUE - 14)) && (waterTile < WATER_THRESHOLD_MAX)) {
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
            if (r == 0) { // (also response to danger)
                startSwimVert();
            } else if (r == 1) {
                startTorpedo();
            } else {
                startSwim();
            }
        }
        
        @Override
        public final int pickResponseToDanger() {
            return 0;
        }

        @Override
        protected final boolean continueState() {
            if (state == STATE_RAISE) {
                startFall();
            } else if ((state == STATE_TORPEDO_UP) || (state == STATE_TORPEDO_DOWN)) {
                startSwim();
                return true;
            } else if (getView() == getTaunt()) {
                startStill(getTaunt());
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
            if (!isOnGround() && isDuringGameplay()) {
                state = STATE_SWIM_STILL;
                setCurrentSwim();
            }
        }
        
        protected final void startFill() {
            startState(STATE_FILL, WAIT_FILL, getStart1());
            fillNeeded = false;
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
            if (!isInMiddle()) {
                torpedoCount = 3;
                torpedoWait = 0;
            }
            startStateIndefinite(STATE_SWIM, getCurrentSwim());
        }
        
        protected final void startTorpedo() {
            startState(STATE_TORPEDO, WAIT_TORPEDO, getLaunch());
        }
        
        protected final void startTorpedoUp() {
            startState(STATE_TORPEDO_UP, WAIT_TORPEDO_VERTICAL, getLaunchUp());
        }
        
        protected final void startTorpedoDown() {
            startState(STATE_TORPEDO_DOWN, WAIT_TORPEDO_VERTICAL, getLaunchDown());
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
        
        protected final static Panmage getLaunchUp() {
            return (launchUp = getFloodImage(launchUp, "floodbot/FloodBotLaunchUp"));
        }
        
        protected final static Panmage getLaunchDown() {
            return (launchDown = getFloodImage(launchDown, "floodbot/FloodBotLaunchDown"));
        }
        
        protected final static Panmage getTaunt() {
            return (taunt = getFloodImage(taunt, "floodbot/FloodBotTaunt"));
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
        
        protected Torpedo(final FloodBot src, final int ox, final int oy, final int rot) {
            super(TORPEDO_OFF_X, TORPEDO_H, 0, 0, 2);
            EnemyProjectile.addBySource(this, getTorpedoImage(0), src, ox, oy);
            BotsnBoltsGame.fxEnemyAttack.startSound();
            setRot(rot);
        }
        
        @Override
        public boolean onStepCustom() {
            final int t = timer / 2;
            changeView(getTorpedoImage(t % 2));
            final int speed = Math.min(t, 8), rot = getRot();
            if (rot == 0) {
                if (addX(speed * getMirrorMultiplier()) != X_NORMAL) {
                    EnemyProjectile.burstEnemy(this);
                    destroy();
                } else if ((timer % 10) == 9) {
                    new Bubble(this, 0);
                }
            } else if (addY(speed * ((rot == 1) ? 1 : -1)) != Y_NORMAL) {
                EnemyProjectile.burstEnemy(this);
                destroy();
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
        protected static Panmage taunt = null;
        protected static Panmage launch = null;
        protected static Panmage jump = null;
        protected static Panmage flare = null;
        protected static Panmage light = null;
        protected final static Panmage[] fades = new Panmage[3];
        private final int xRight;
        private final int xLeft;
        private Pantexture tex = null;
        private Scythe scythe = null;
        
        protected DroughtBot(final Segment seg) {
            super(DROUGHT_OFF_X, DROUGHT_H, seg);
            xRight = getX();
            xLeft = getMirroredX(xRight);
        }
        
        @Override
        protected final void taunt() {
            startHold(WAIT_INDEFINITE);
        }
        
        private final boolean finishDroughtTaunt() {
            if (finishTaunt()) {
                changeView(getTaunt());
                return true;
            }
            return false;
        }
        
        @Override
        protected final String[] getIntroMessages() {
            return new String[] {
                "There can be no life without water.  I will not rest until the dried, empty husk of all that once lived has crumbled into dust.",
                "I am a protector of life, and I won't be intimidated by mere words.",
                "Then let us battle.  Prepare yourself!"
            };
        }
        
        @Override
        protected final String[] getRematchMessages() {
            return new String[] { "Prepare yourself!" };
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
            } else if ((state == STATE_HOLD) && (waitCounter == 1)) {
                scythe = new Scythe(this);
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
            if (!isTauntFinished()) {
                startUnmorph();
            } else if (!addBoundedX(xLeft, xRight)) {
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
        public boolean onAttack(final Player player) {
            if ((state == STATE_SAND) && !player.isInvincible()) { // Check invincibility before hurting Player
                startWrap(player);
            }
            return super.onAttack(player);
        }
        
        private final boolean isFirstLaunchNeeded() {
            return (moves <= 0) && (scythe != null);
        }
        
        @Override
        protected final boolean pickState() {
            if (isFirstLaunchNeeded()) {
                addToRandHistory(1);
                state = STATE_HOLD;
                scythe.launch();
                waitTimer = WAIT_HOLD - Scythe.TIME_LAUNCH;
                scythe = null;
                return false;
            }
            final int r = rand(3);
            if (r == 0) { // (also response to danger)
                startMorph();
            } else if (r == 1) {
                startHold();
            } else {
                startJump();
            }
            return false;
        }
        
        @Override
        public final int pickResponseToDanger() {
            return 0;
        }

        @Override
        protected final boolean continueState() {
            if (state == STATE_MORPH) {
                startSand();
            } else if (state == STATE_FLARE) {
                finishJump();
            } else if (isFirstLaunchNeeded()) {
                startStill(getHold());
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
            hv = isTauntFinished() ? (getMirrorMultiplier() * 6) : 0;
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
            startHold(WAIT_HOLD);
        }
        
        protected final void startHold(final int waitTimer) {
            startState(STATE_HOLD, waitTimer, getHold());
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
            BotsnBoltsGame.fxDefeat.startSound();
            startState(STATE_FLARE, WAIT_FLARE, getFlare());
        }
        
        protected final void startFade() {
            startState(STATE_FADE, WAIT_FADE, getStill());
        }

        @Override
        protected final Panmage getStill() {
            return getImage();
        }
        
        protected final static Panmage getImage() {
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
        
        protected final static Panmage getTaunt() {
            return (taunt = getDroughtImage(taunt, "droughtbot/DroughtBotTaunt"));
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
        protected final static int TIME_LAUNCH = 26;
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
            } else if ((timer == TIME_LAUNCH) && !src.finishDroughtTaunt()) {
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
            BotsnBoltsGame.fxDefeat.startSound();
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
    
    protected final static int PYRO_OFF_X = 6, PYRO_H = 24;
    protected final static Panple PYRO_O = new FinPanple2(13, 1);
    protected final static Panple PYRO_MIN = getMin(PYRO_OFF_X);
    protected final static Panple PYRO_MAX = getMax(PYRO_OFF_X, PYRO_H);
    protected final static Panple PYRO_DIVE_O = new FinPanple2(16, 1);
    protected final static Panple PYRO_FLY_O = new FinPanple2(16, 7);
    protected final static Panple PYRO_FLY_MIN = getMin(13);
    protected final static Panple PYRO_FLY_MAX = getMax(13, 12);
    
    protected final static class PyroBot extends Boss {
        protected final static byte STATE_FIRE = 1;
        protected final static byte STATE_JUMPS = 2;
        protected final static byte STATE_TAUNT = 3;
        protected final static byte STATE_DIVE = 4;
        protected final static byte STATE_FLY = 5;
        protected final static int WAIT_BEFORE_FIRE = 8; // 16;
        protected final static int WAIT_FIRE = 48 + WAIT_BEFORE_FIRE;
        protected final static Panmage[] stills = new Panmage[3];
        protected final static Panmage[] aims = new Panmage[3];
        protected final static Panmage[] taunts = new Panmage[3];
        protected final static Panmage[] jumps = new Panmage[2];
        protected final static Panmage[] falls = new Panmage[2];
        protected static Panmage dive = null;
        protected static Panmage fly = null;
        private final int xRight;
        private final int xLeft;
        private int yStart = 0;
        private int yFly = 0;
        
        protected PyroBot(final Segment seg) {
            super(PYRO_OFF_X, PYRO_H, seg);
            xRight = getX();
            xLeft = getMirroredX(xRight);
        }
        
        @Override
        protected final void taunt() {
            startFire();
            yStart = getY();
            yFly = yStart + 14;
        }
        
        @Override
        protected final String[] getIntroMessages() {
            return new String[] {
                    "TODO Pyro's line.",
                    "Null's line.",
                    "Pyro's line."
                };
        }
        
        @Override
        protected final String[] getRematchMessages() {
            return new String[] { "Pyro's line." };
        }
        
        @Override
        protected final boolean onWaiting() {
            setView();
            if (state == STATE_JUMPS) {
                setJumpImage();
            } else if (state == STATE_FLY) {
                if (addBoundedX(xLeft, xRight)) {
                    setFlip(Pangine.getEngine().isOn(3));
                    if ((waitTimer % 10) == 0) {
                        new PyroFlicker(this, -8, 1, 2);
                    }
                    return true;
                } else {
                    setFlip(false);
                    turnTowardPlayer();
                    getPosition().setY(yStart);
                    hv = 0;
                    startStill();
                }
            } else if ((state == STATE_FIRE) && (waitTimer > 16) && (waitTimer <= (WAIT_FIRE - WAIT_BEFORE_FIRE)) && ((waitTimer % 2) == 1)) {
                new PyroFire(this);
            }
            return false;
        }
        
        private final void setView() {
            final Panmage[] imgs;
            final String name;
            if (state == STATE_STILL) {
                imgs = stills;
                name = "";
            } else if (state == STATE_FIRE) {
                imgs = aims;
                name = "Aim";
            } else if (state == STATE_TAUNT) {
                imgs = taunts;
                name = "Taunt";
            } else {
                return;
            }
            changeView(getCurrent(imgs, name));
        }
        
        @Override
        protected final boolean pickState() {
            final int r = rand(3);
            if (r == 0) {
                startFire();
            } else if (r == 1) {
                startDive();
            } else { // 2 (also response to danger)
                new PyroFlicker(this);
                startJumps();
            }
            return false;
        }
        
        @Override
        public final int pickResponseToDanger() {
            return 2;
        }
        
        @Override
        protected final boolean continueState() {
            if ((state == STATE_FIRE) && finishTaunt()) {
                startStateIndefinite(STATE_TAUNT, getCurrent(taunts, "Taunt"));
            } else if (state == STATE_DIVE) {
                startFly();
            } else {
                startStill();
            }
            return false;
        }
        
        @Override
        protected final boolean hasPendingJumps() {
            if (hasPendingOrContinuedJumps()) {
                return true;
            }
            return false;
        }
        
        @Override
        protected final void onContinueJumps() {
            new PyroFlicker(this);
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
            return 4;
        }
        
        protected final void startFire() {
            startState(STATE_FIRE, WAIT_FIRE, getCurrent(aims, "Aim"));
        }
        
        protected final void startDive() {
            startState(STATE_DIVE, 5, getDive());
        }

        protected final void startFly() {
            hv = getMirrorMultiplier() * 5;
            getPosition().setY(yFly);
            startStateIndefinite(STATE_FLY, getFly());
        }
        
        private final void setJumpImage() {
            if (v < 0) {
                changeView(getFall());
            } else {
                changeView(getJump());
            }
        }
        
        @Override
        protected final Panmage getStill() {
            return getCurrent(stills, "");
        }
        
        @Override
        protected final Panmage getJump() {
            return getCurrentJump(jumps, "Jump");
        }
        
        protected final static Panmage getFall() {
            return getCurrentJump(falls, "Fall");
        }
        
        protected final static Panmage getCurrentJump(final Panmage[] imgs, final String name) {
            final int i = ((int) (Pangine.getEngine().getClock() % 8)) / 4;
            Panmage img = imgs[i];
            if (img == null) {
                img = getPyroImage(null, "pyrobot/PyroBot" + name + (i + 1));
                imgs[i] = img;
            }
            return img;
        }
        
        protected final static Panmage getDive() {
            return (dive = getImage(dive, "pyrobot/PyroBotDive", PYRO_DIVE_O, PYRO_MIN, PYRO_MAX));
        }
        
        protected final static Panmage getFly() {
            return (fly = getImage(fly, "pyrobot/PyroBotFly", PYRO_FLY_O, PYRO_FLY_MIN, PYRO_FLY_MAX));
        }
        
        protected final Panmage getCurrent(final Panmage[] imgs, final String name) {
            final int i = ((int) (Pangine.getEngine().getClock() % 12)) / 4;
            Panmage img = imgs[i];
            if (img == null) {
                img = getPyroImage(null, "pyrobot/PyroBot" + name + (i + 1));
                imgs[i] = img;
            }
            return img;
        }
        
        protected final static Panmage getPyroImage(final Panmage img, final String name) {
            return getImage(img, name, PYRO_O, PYRO_MIN, PYRO_MAX);
        }
    }
    
    protected final static class PyroFire extends EnemyProjectile {
        protected final static Panmage[] fires = new Panmage[7];
        private int age = 0;
        
        protected PyroFire(final PyroBot src) {
            super(src, 18, 12, 4 * src.getMirrorMultiplier(), 0);
            changeView(getFire(0));
        }
        
        @Override
        public void onStep(final StepEvent event) {
            super.onStep(event);
            age++;
            final int h = age / 2;
            if (h > 6) {
                destroy();
                return;
            }
            if (changeView(getFire(h))) {
                final Panple v = getVelocity();
                final int vx = Math.round(v.getX());
                if (vx != 0) {
                    v.setX((Math.abs(vx) + 2) * Mathtil.getSign(vx));
                }
            }
            BotsnBoltsGame.fxWarp.startSound();
        }
        
        @Override
        protected final int getDamage() {
            return 4;
        }
        
        @Override
        protected final void onCollisionWithPlayerProjectile(final Projectile prj) {
            prj.burst();
        }
        
        protected final static Panmage getFire(final int i) {
            Panmage img = fires[i];
            if (img == null) {
                final Panple o, n, x;
                if (i == 0) {
                    o = BotsnBoltsGame.CENTER_8; n = BotsnBoltsGame.MIN_8; x = BotsnBoltsGame.MAX_8;
                } else if (i == 1) {
                    o = BotsnBoltsGame.CENTER_16; n = BotsnBoltsGame.MIN_8; x = BotsnBoltsGame.MAX_8;
                } else if (i == 2) {
                    o = BotsnBoltsGame.CENTER_16; n = BotsnBoltsGame.MIN_16; x = BotsnBoltsGame.MAX_16;
                } else if (i == 3) {
                    o = BotsnBoltsGame.CENTER_32; n = BotsnBoltsGame.MIN_16; x = BotsnBoltsGame.MAX_16;
                } else {
                    o = BotsnBoltsGame.CENTER_32; n = BotsnBoltsGame.MIN_32; x = BotsnBoltsGame.MAX_32;
                }
                img = getImage(null, "pyrobot/Fire" + (i + 1), o, n, x);
                fires[i] = img;
            }
            return img;
        }
    }
    
    protected final static class PyroFlicker extends EnemyProjectile {
        protected final static Panmage[] flickers = new Panmage[7];
        private final int divisor;
        private int age = 0;
        
        protected PyroFlicker(final PyroBot src) {
            this(src, 3, 6, 4);
        }
        
        protected PyroFlicker(final PyroBot src, final int ox, final int oy, final int divisor) {
            super(src, ox, oy, 0, 0);
            this.divisor = divisor;
            changeView(getFlicker(0));
        }
        
        @Override
        public void onStep(final StepEvent event) {
            super.onStep(event);
            age++;
            final int h = age / divisor;
            if (h > 3) {
                destroy();
                return;
            }
            changeView(getFlicker(h));
            BotsnBoltsGame.fxWarp.startSound();
        }
        
        @Override
        protected final int getDamage() {
            return 4;
        }
        
        protected final static Panmage getFlicker(final int i) {
            Panmage img = flickers[i];
            if (img == null) {
                final Panple n, x;
                if (i < 2) {
                    n = BotsnBoltsGame.MIN_8; x = BotsnBoltsGame.MAX_8;
                } else {
                    n = BotsnBoltsGame.MIN_16; x = BotsnBoltsGame.MAX_16;
                }
                img = getImage(null, "pyrobot/Flicker" + (i + 1), BotsnBoltsGame.CENTER_16, n, x);
                flickers[i] = img;
            }
            return img;
        }
    }
    
    protected final static int GEO_OFF_X = 11, GEO_H = 28;
    protected final static Panple GEO_O = new FinPanple2(15, 1);
    protected final static Panple GEO_HANG_O = new FinPanple2(18, 1);
    protected final static Panple GEO_MIN = getMin(GEO_OFF_X);
    protected final static Panple GEO_MAX = getMax(GEO_OFF_X, GEO_H);
    
    protected final static class GeoBot extends Boss {
        protected final static byte STATE_STOMP = 1;
        protected final static byte STATE_STOMP2 = 2;
        protected final static byte STATE_JUMPS = 3;
        protected final static byte STATE_JUMP_TO_WALL = 4;
        protected final static byte STATE_HANG = 5;
        protected final static byte STATE_DIVE = 6;
        protected final static byte STATE_IMPACT = 7;
        protected final static byte STATE_PRE_TAUNT = 8;
        protected final static byte STATE_TAUNT1 = 9;
        protected final static byte STATE_TAUNT2 = 10;
        protected final static byte STATE_POST_TAUNT = 11;
        protected final static Panmage[] stills = new Panmage[6];
        protected static Panmage stomp1 = null;
        protected static Panmage stomp2 = null;
        protected static Panmage taunt1 = null;
        protected static Panmage taunt2 = null;
        protected static Panmage jump = null;
        protected static Panmage land = null;
        protected static Panmage hang = null;
        protected static Panmage dive = null;
        
        protected GeoBot(final Segment seg) {
            super(GEO_OFF_X, GEO_H, seg);
        }
        
        @Override
        protected final void taunt() {
            startState(STATE_PRE_TAUNT, 30, getStill());
        }
        
        @Override
        protected final String[] getIntroMessages() {
            return new String[] {
                    "TODO Geo's line.",
                    "Null's line.",
                    "Geo's line."
                };
        }
        
        @Override
        protected final String[] getRematchMessages() {
            return new String[] { "Geo's line." };
        }
        
        @Override
        protected final boolean onWaiting() {
            setView();
            if ((state == STATE_STOMP2) && ((waitTimer % 6) == 0)) {
                new GeoBurst(this, Mathtil.randi(4, 15));
            } else if ((state == STATE_JUMPS) && (v <= 0)) {
                changeView(getLand());
                hv = 2 * getMirrorMultiplier();
            } else if ((state == STATE_JUMP_TO_WALL) && (waitCounter == 19)) {
                startHang();
                return true;
            } else if (state == STATE_HANG) {
                return true;
            } else if (state == STATE_DIVE) {
                onDiving();
                return true;
            } else if ((state == STATE_IMPACT) && ((waitTimer % 8) == 0)) {
                onImpacting();
            }
            return false;
        }
        
        private final void setView() {
            if ((state == STATE_STILL) || (state == STATE_POST_TAUNT) || (state == STATE_PRE_TAUNT)) {
                changeView(getStill());
            }
        }
        
        @Override
        protected final boolean pickState() {
            final int r = rand(3);
            if (r == 0) {
                startStomp();
            } else if (r == 1) {
                startJumps();
            } else { // 2 (also response to danger)
                startJumpToWall();
            }
            return false;
        }
        
        @Override
        public final int pickResponseToDanger() {
            return 2;
        }
        
        @Override
        protected final boolean continueState() {
            if (state == STATE_STOMP) {
                finishStomp();
            } else if (state == STATE_HANG) {
                startDive();
            } else if (state == STATE_PRE_TAUNT) {
                startState(STATE_TAUNT1, 3, getTaunt1());
            } else if (state == STATE_TAUNT1) {
                startState(STATE_TAUNT2, 32, getTaunt2());
            } else if (state == STATE_TAUNT2) {
                finishTaunt();
                startStateIndefinite(STATE_POST_TAUNT, getStill());
            } else if (state == STATE_IMPACT) {
                turnTowardPlayer();
                startStill();
            } else {
                startStill();
            }
            return false;
        }
        
        private final void startStomp() {
            startState(STATE_STOMP, 8, getStomp1());
        }
        
        private final void finishStomp() {
            startState(STATE_STOMP2, 16, getStomp2());
            BotsnBoltsGame.fxEnemyAttack.startSound();
            new GeoSpike(this, true);
        }
        
        private final void startJumpToWall() {
            final int x = getX();
            final int dst = (x >= MID_X) ? 368 - x : 16 - x;
            startJump(STATE_JUMP_TO_WALL, getJump(), 9.5f, Math.round(dst / 15.0f));
        }
        
        private final void startHang() {
            startState(STATE_HANG, 16, getHang());
            final boolean mirror = isMirror();
            final int m = getMirrorMultiplier(mirror);
            while (addX(m) == X_NORMAL);
            addX(-m);
            setMirror(!mirror);
            hv = 0;
        }
        
        private final void startDive() {
            startStateIndefinite(STATE_DIVE, getDive());
            hv = 10 * getMirrorMultiplier();
        }
        
        private final void onDiving() {
            final Panple pos = getPosition();
            final float oldY = pos.getY();
            addY(-5);
            hv = Mathtil.getSign(hv) * Math.round(oldY - pos.getY()) * 2;
            if (hv != 0) {
                addX(hv);
            }
        }
        
        private final void startImpact() {
            startState(STATE_IMPACT, 32, getDive());
            onImpacting();
        }
        
        private final void onImpacting() {
            new GeoSpike(this, (waitTimer % 16) == 0);
            new GeoBurst(this, Mathtil.randi(4, 15));
        }
        
        @Override
        protected final boolean hasPendingJumps() {
            if (hasPendingOrContinuedJumps()) {
                return true;
            }
            return false;
        }
        
        @Override
        protected final byte getStateJumps() {
            return STATE_JUMPS;
        }
        
        @Override
        protected final float getJumpsV() {
            return 11;
        }
        
        @Override
        protected final int getJumpsHv() {
            return 3;
        }
        
        @Override
        protected final void onBossLandedAny() {
            if (!isTauntFinished()) {
                return;
            } else if (state == STATE_DIVE) {
                startImpact();
                return;
            } else if (state == STATE_IMPACT) {
                return;
            }
            new GeoSpike(this, true);
            for (int i = 0; i < 4; i++) {
                new GeoBurst(this, Mathtil.randi(-4, 7));
            }
        }
        
        @Override
        protected final boolean onBossLanded() {
            if ((state == STATE_DIVE) || (state == STATE_IMPACT)) {
                return true;
            }
            return false;
        }
        
        @Override
        protected final boolean isTurnTowardPlayerNeeded() {
            if (state == STATE_DIVE) {
                return false;
            } else if ((state == STATE_IMPACT) && (waitCounter < 2)) {
                return false;
            }
            return true;
        }
        
        @Override
        protected final Panmage getStill() {
            final int i = ((int) (Pangine.getEngine().getClock() % 18)) / 3;
            Panmage img = stills[i];
            if (img == null) {
                img = getGeoImage(null, "geobot/GeoBot" + (i + 1));
                stills[i] = img;
            }
            return img;
        }
        
        @Override
        protected final Panmage getJump() {
            return (jump = getGeoImage(jump, "geobot/GeoBotJump"));
        }
        
        protected final static Panmage getLand() {
            return (land = getGeoImage(land, "geobot/GeoBotLand"));
        }
        
        protected final static Panmage getHang() {
            return (hang = getGeoImage(hang, "geobot/GeoBotHang", GEO_HANG_O));
        }
        
        protected final static Panmage getDive() {
            return (dive = getGeoImage(dive, "geobot/GeoBotDive"));
        }
        
        protected final static Panmage getStomp1() {
            return (stomp1 = getGeoImage(stomp1, "geobot/GeoBotStomp1"));
        }
        
        protected final static Panmage getStomp2() {
            return (stomp2 = getGeoImage(stomp2, "geobot/GeoBotStomp2"));
        }
        
        protected final static Panmage getTaunt1() {
            return (taunt1 = getGeoImage(taunt1, "geobot/GeoBotTaunt1"));
        }
        
        protected final static Panmage getTaunt2() {
            return (taunt2 = getGeoImage(taunt2, "geobot/GeoBotTaunt2"));
        }
        
        protected final static Panmage getGeoImage(final Panmage img, final String name) {
            return getGeoImage(img, name, GEO_O);
        }
        
        protected final static Panmage getGeoImage(final Panmage img, final String name, final Panple o) {
            return getImage(img, name, o, GEO_MIN, GEO_MAX);
        }
    }
    
    private abstract static class BossBurst extends Panctor implements StepListener {
        private int age = 0;
        
        private BossBurst(final Panctor src, final int ox, final int oy) {
            final Panple spos = src.getPosition();
            getPosition().set(spos.getX() + (src.getMirrorMultiplier() * ox), spos.getY() + oy, BotsnBoltsGame.DEPTH_BURST);
            setMirror(src.isMirror());
            setView(getBurst1());
            addActor(this);
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            age++;
            if (age >= 3) {
                if (age >= 6) {
                    destroy();
                    return;
                }
                changeView(getBurst2());
            }
        }
        
        protected abstract Panmage getBurst1();
        
        protected abstract Panmage getBurst2();
    }
    
    private final static class GeoBurst extends BossBurst {
        protected static Panmage burst1 = null;
        protected static Panmage burst2 = null;
        
        private GeoBurst(final Panctor src, final int ox) {
            super(src, ox, Mathtil.randi(-2, 7));
        }
        
        @Override
        protected final Panmage getBurst1() {
            return (burst1 = getImage(burst1, "geobot/Burst1", BotsnBoltsGame.CENTER_8, BotsnBoltsGame.MIN_8, BotsnBoltsGame.MAX_8));
        }
        
        @Override
        protected final Panmage getBurst2() {
            return (burst2 = getImage(burst2, "geobot/Burst2", BotsnBoltsGame.CENTER_16, BotsnBoltsGame.MIN_16, BotsnBoltsGame.MAX_16));
        }
    }
    
    private final static class GeoSpike extends Enemy {
        protected static Panmage spike = null;
        protected static Panmage shatter = null;
        
        protected GeoSpike(final Boss src, final boolean targeted) {
            super(PROP_OFF_X, PROP_H, 0, 0, 1);
            float x = -1;
            if (targeted) {
                final Player player = src.getNearestPlayer();
                if (player != null) {
                    x = player.getPosition().getX();
                }
            }
            if (x < 0) {
                x = Mathtil.randi(32, 352);
            }
            getPosition().set(x, 224.0f);
            setView(getSpike());
            addActor(this);
            hv = 0;
            v = 0;
        }
        
        @Override
        protected final int getDamage() {
            return 2;
        }
        
        @Override
        protected final int getSolid(final int off) {
            return (getPosition().getY() > 112) ? -1 : getIdentitySolid(off);
        }
        
        @Override
        protected final void onGrounded() {
            BotsnBoltsGame.fxCrumble.startSound();
            Player.shatter(this, getShatter());
            destroy();
        }
        
        protected final static Panmage getSpike() {
            return (spike = Boss.getImage(spike, "geobot/Spike", BotsnBoltsGame.fireballEnemy[0]));
        }
        
        protected final static Panmage getShatter() {
            return (shatter = Boss.getImage(shatter, "geobot/Shatter", BotsnBoltsGame.CENTER_8, BotsnBoltsGame.MIN_8, BotsnBoltsGame.MAX_8));
        }
    }
    
    protected final static int CRYO_OFF_X = 7, CRYO_H = 25;
    protected final static Panple CRYO_O = new FinPanple2(14, 1);
    protected final static Panple CRYO_FLIP_O = new FinPanple2(15, 15);
    protected final static Panple CRYO_TAUNT_O = new FinPanple2(16, 1);
    protected final static Panple CRYO_MIN = getMin(CRYO_OFF_X);
    protected final static Panple CRYO_MAX = getMax(CRYO_OFF_X, CRYO_H);
    
    protected final static class CryoBot extends Boss {
        protected final static byte STATE_PRE_TAUNT = 1;
        protected final static byte STATE_POST_TAUNT = 2;
        protected final static byte STATE_AIM = 3;
        protected final static byte STATE_PUMP = 4;
        protected final static byte STATE_AFTER_PUMP = 5;
        protected final static byte STATE_WAIT_FOR_PLAYER_TO_LAND = 6;
        protected final static byte STATE_DASH = 7;
        protected final static byte STATE_WAIT_AFTER_DASH = 8;
        protected final static byte STATE_JAB1 = 9;
        protected final static byte STATE_WAIT_AFTER_JAB1 = 10;
        protected final static byte STATE_JAB2 = 11;
        protected final static byte STATE_WAIT_AFTER_JAB2 = 12;
        protected final static byte STATE_KICK = 13;
        protected final static byte STATE_WAIT_AFTER_KICK = 14;
        protected final static byte STATE_UPPERCUT = 15;
        protected final static byte STATE_FLIP = 16;
        protected final static int TIME_JAB = 5;
        protected final static int TIME_WAIT_AFTER_JAB = 2;
        protected final static int TIME_KICK = 7;
        protected final static int TIME_UPPERCUT = 8;
        protected final static int VEL_PROJECTILE = 8;
        protected final static float VX_SPREAD;
        protected final static float VY_SPREAD;
        protected final static float INNER_ANGLE_LIMIT;
        protected final static Panmage[] taunts = new Panmage[3];
        protected static Panmage still = null;
        protected static Panmage aim = null;
        protected static Panmage pump = null;
        protected static Panmage jab = null;
        protected static Panmage kick = null;
        protected static Panmage uppercut = null;
        protected static Panmage flip = null;
        protected final static Panmage[] dashes = new Panmage[2];
        private final int xRight;
        private final int xLeft;
        private Player target = null;
        
        static {
            final double angleLimit = Math.PI / 24;
            scratchPanple.setMagnitudeDirection(VEL_PROJECTILE, angleLimit);
            VX_SPREAD = scratchPanple.getX();
            VY_SPREAD = scratchPanple.getY();
            INNER_ANGLE_LIMIT = (float) (angleLimit * 0.9);
        }
        
        protected CryoBot(final Segment seg) {
            super(CRYO_OFF_X, CRYO_H, seg);
            xRight = getX();
            xLeft = getMirroredX(xRight);
        }
        
        @Override
        protected final void taunt() {
            startJab1();
        }
        
        @Override
        protected final String[] getIntroMessages() {
            return new String[] {
                    "TODO Cryo's line.",
                    "Null's line.",
                    "Cryo's line."
                };
        }
        
        @Override
        protected final String[] getRematchMessages() {
            return new String[] { "Cryo's line." };
        }
        
        @Override
        protected final boolean onWaiting() {
            if (state == STATE_DASH) {
                onDashing();
            } else if (state == STATE_FLIP) {
                onFlipping();
            } else if ((state == STATE_AIM) && (waitCounter == 1)) {
                new CryoProjectile(this, VX_SPREAD, VY_SPREAD);
                new CryoProjectile(this, VX_SPREAD, -VY_SPREAD);
                for (int i = 0; i < 3; i++) {
                    scratchPanple.setMagnitudeDirection(VEL_PROJECTILE, Mathtil.randf(0, INNER_ANGLE_LIMIT));
                    new CryoProjectile(this, scratchPanple.getX(), scratchPanple.getY() * (Mathtil.rand() ? -1 : 1));
                }
            } else if ((state == STATE_PUMP) && (waitCounter == 1)) {
                //TODO sound?
                new CryoShell(this);
            } else if (state == STATE_WAIT_FOR_PLAYER_TO_LAND) {
                if (target.isGrounded()) {
                    startDash();
                }
            } else if (((state == STATE_JAB1) || (state == STATE_JAB2)) && (waitCounter == 1)) {
                impact(20, 18);
            } else if ((state == STATE_KICK) && (waitCounter == 1)) {
                impact(20, 17);
            } else if ((state == STATE_UPPERCUT) && (waitCounter == 1)) {
                impact(18, 25);
            } else if ((state == STATE_PRE_TAUNT) || (state == STATE_POST_TAUNT)) {
                changeView(getTaunt());
            }
            return false;
        }
        
        private final void impact(final int ox, final int oy) {
            if (isTauntFinished() && !isDestroyed(target)) {
                new CryoBurst(this, ox, oy);
            }
        }
        
        @Override
        protected final boolean pickState() {
            turnTowardPlayer();
            if (target != null) {
                if (target.isGrounded()) {
                    startDash();
                } else {
                    startStateIndefinite(STATE_WAIT_FOR_PLAYER_TO_LAND, getStill());
                }
                return false;
            }
            final int r = rand(3);
            if (r == 0) {
                startAim();
            } else if (r == 1) {
                startDash();
            } else { // 2 (also response to danger)
                startJumps();
            }
            return false;
        }
        
        @Override
        public final int pickResponseToDanger() {
            return 2;
        }
        
        @Override
        protected final boolean continueState() {
            if (state == STATE_AIM) {
                startPump();
            } else if (state == STATE_PUMP) {
                startAfterPump();
            } else if (state == STATE_WAIT_AFTER_DASH) {
                startJab1();
                hurt(false);
            } else if (state == STATE_JAB1) {
                startState(STATE_WAIT_AFTER_JAB1, TIME_WAIT_AFTER_JAB, getStill());
            } else if (state == STATE_WAIT_AFTER_JAB1) {
                startState(STATE_JAB2, TIME_JAB, getJab());
                hurt(false);
            } else if (state == STATE_JAB2) {
                startState(STATE_WAIT_AFTER_JAB2, TIME_WAIT_AFTER_JAB, getStill());
            } else if (state == STATE_WAIT_AFTER_JAB2) {
                startState(STATE_KICK, TIME_KICK, getKick());
                hurt(false);
            } else if (state == STATE_KICK) {
                startState(STATE_WAIT_AFTER_KICK, TIME_WAIT_AFTER_JAB, getStill());
            } else if (state == STATE_WAIT_AFTER_KICK) {
                startState(STATE_UPPERCUT, TIME_UPPERCUT, getUppercut());
                if (target != null) {
                    target.unfreeze(0);
                    hurt(true);
                }
            } else if (state == STATE_UPPERCUT) {
                target = null;
                if (isTauntFinished()) {
                    startStill();
                } else {
                    startState(STATE_PRE_TAUNT, 30, getTaunt());
                }
            } else if (state == STATE_PRE_TAUNT) {
                finishTaunt();
                startStateIndefinite(STATE_POST_TAUNT, getTaunt());
            } else {
                turnTowardPlayer();
                startStill();
            }
            return false;
        }
        
        private final void startJab1() {
            startState(STATE_JAB1, TIME_JAB, getJab());
        }
        
        private final void hurt(final boolean effectsNeeded) {
            if (target != null) {
                target.hurtForce(1, effectsNeeded);
            }
        }
        
        private final void startAim() {
            turnTowardPlayer();
            startState(STATE_AIM, 16, getAim());
        }
        
        private final void startPump() {
            startState(STATE_PUMP, 6, getPump());
        }
        
        private final void startAfterPump() {
            startState(STATE_AFTER_PUMP, 10, getAim());
        }
        
        private final void startDash() {
            if (target != null) {
                turnTowardPlayer(target);
            } else {
                turnTowardPlayer();
            }
            hv = getMirrorMultiplier() * 6;
            startStateIndefinite(STATE_DASH, getDash());
        }
        
        private final void onDashing() {
            changeView(getDash());
            if (!addBoundedX(xLeft, xRight)) {
                hv = 0;
                turnTowardPlayer();
                startStill(Mathtil.randi(DEFAULT_STILL_MIN, DEFAULT_STILL_MAX));
                return;
            }
            if ((waitCounter % 4) == 1) {
                new CryoBurst(this, -5, 4);
            }
        }
        
        @Override
        public final boolean onAttack(final Player player) {
            if ((state == STATE_DASH) && (player == target)) {
                final int tx = Math.round(target.getPosition().getX());
                final int dx = tx + (22 * ((hv < 0) ? 1 : -1));
                moveTo(dx, getY());
                hv = 0;
                hurt(false);
                startState(STATE_WAIT_AFTER_DASH, 3, getStill());
                return true;
            } else {
                return super.onAttack(player);
            }
        }
        
        private final void onFlipping() {
            final int frameDuration = 4, m = frameDuration * 4;
            setRot(4 - ((waitCounter % m) / frameDuration));
        }
        
        @Override
        protected final boolean hasPendingJumps() {
            return hasPendingOrContinuedJumps();
        }
        
        @Override
        protected final byte getStateJumps() {
            return STATE_FLIP;
        }
        
        @Override
        protected final float getJumpsV() {
            return 11;
        }
        
        @Override
        protected final int getJumpsHv() {
            return 4;
        }
        
        @Override
        protected final void onBossLandedAny() {
            setRot(0);
        }
        
        @Override
        protected final void onJumpLanded() {
            setRot(0);
        }
        
        @Override
        protected final Panmage getStill() {
            return (still = getCryoImage(still, "cryobot/CryoBot"));
        }
        
        @Override
        protected final Panmage getJump() {
            return (flip = getCryoImage(flip, "cryobot/CryoBotFlip", CRYO_FLIP_O));
        }
        
        protected final static Panmage getAim() {
            return (aim = getCryoImage(aim, "cryobot/CryoBotAim"));
        }
        
        protected final static Panmage getPump() {
            return (pump = getCryoImage(pump, "cryobot/CryoBotPump"));
        }
        
        protected final static Panmage getJab() {
            return (jab = getCryoImage(jab, "cryobot/CryoBotJab"));
        }
        
        protected final static Panmage getKick() {
            return (kick = getCryoImage(kick, "cryobot/CryoBotKick"));
        }
        
        protected final static Panmage getUppercut() {
            return (uppercut = getCryoImage(uppercut, "cryobot/CryoBotUppercut"));
        }
        
        protected final static Panmage getDash() {
            final int i = ((int) (Pangine.getEngine().getClock() % 6)) / 3;
            Panmage img = dashes[i];
            if (img == null) {
                img = getCryoImage(null, "cryobot/CryoBotDash" + (i + 1));
                dashes[i] = img;
            }
            return img;
        }
        
        protected final static Panmage getTaunt() {
            final int i = ((int) (Pangine.getEngine().getClock() % 12)) / 4;
            Panmage img = taunts[i];
            if (img == null) {
                img = getCryoImage(null, "cryobot/CryoBotTaunt" + (i + 1), CRYO_TAUNT_O);
                taunts[i] = img;
            }
            return img;
        }
        
        protected final static Panmage getCryoImage(final Panmage img, final String name) {
            return getCryoImage(img, name, CRYO_O);
        }
        
        protected final static Panmage getCryoImage(final Panmage img, final String name, final Panple o) {
            return getImage(img, name, o, CRYO_MIN, CRYO_MAX);
        }
    }
    
    private final static class CryoShell extends Pandy {
        protected static Panmage shell = null;
        private int age = 0;
        
        private CryoShell(final Panctor src) {
            super(gTuple);
            final Panple spos = src.getPosition();
            final boolean mirror = src.isMirror();
            final int mult = getMirrorMultiplier(mirror);
            getPosition().set(spos.getX() + (6 * mult), spos.getY() + 17, BotsnBoltsGame.DEPTH_OVERLAY);
            getVelocity().set(2 * -mult, 4);
            setMirror(mirror);
            addActor(this);
            setView(getShell());
        }
        
        @Override
        public void onStep(final StepEvent event) {
            super.onStep(event);
            if (!isInView()) {
                destroy();
                return;
            }
            age++;
            if (age == 8) {
                set(true, 3);
            } else if (age == 16) {
                set(false, 1);
            } else if (age == 24) {
                set(true, 2);
            } else if (age == 32) {
                set(false, 2);
            } else if (age == 40) {
                set(true, 1);
            } else if (age == 48) {
                set(false, 3);
            } else if (age == 56) {
                set(true, 0);
            } else if (age == 64) {
                set(false, 0);
                age = 0;
            }
        }
        
        private final void set(final boolean flip, final int rot) {
            setFlip(flip);
            setRot(rot);
        }
        
        protected final static Panmage getShell() {
            return (shell = Boss.getImage(shell, "cryobot/shell", BotsnBoltsGame.CENTER_8, BotsnBoltsGame.MIN_8, BotsnBoltsGame.MAX_8));
        }
    }
    
    private final static class CryoProjectile extends EnemyProjectile {
        protected static Panmage img = null;
        private final CryoBot src;
        
        private CryoProjectile(final CryoBot src, final float vx, final float vy) {
            super(getImage(), src, 15, 15, vx * src.getMirrorMultiplier(), vy);
            getVelocity().multiply(Mathtil.randf(0.9f, 1.1f));
            setView(getImage());
            this.src = src;
        }
        
        @Override
        protected final boolean hurt(final Player player) {
            if (player.freezeIndefinite()) {
                src.target = player;
                return true;
            }
            return false;
        }
        
        protected final static Panmage getImage() {
            return (img = Boss.getImage(img, "cryobot/Projectile", BotsnBoltsGame.CENTER_4, BotsnBoltsGame.MIN_4, BotsnBoltsGame.MAX_4));
        }
    }
    
    private final static class CryoBurst extends BossBurst {
        protected static Panmage burst1 = null;
        protected static Panmage burst2 = null;
        
        private CryoBurst(final Panctor src, final int ox, final int oy) {
            super(src, ox, oy);
        }
        
        @Override
        protected final Panmage getBurst1() {
            return (burst1 = getImage(burst1, "cryobot/Burst1", BotsnBoltsGame.CENTER_8, BotsnBoltsGame.MIN_8, BotsnBoltsGame.MAX_8));
        }
        
        @Override
        protected final Panmage getBurst2() {
            return (burst2 = getImage(burst2, "cryobot/Burst2", BotsnBoltsGame.CENTER_8, BotsnBoltsGame.MIN_8, BotsnBoltsGame.MAX_8));
        }
    }
    
    protected final static int ELECTRO_OFF_X = 6, ELECTRO_H = 24;
    protected final static Panple ELECTRO_O = new FinPanple2(14, 1);
    protected final static Panple ELECTRO_CHARGE_O = new FinPanple2(16, 1);
    protected final static Panple ELECTRO_MIN = getMin(ELECTRO_OFF_X);
    protected final static Panple ELECTRO_MAX = getMax(ELECTRO_OFF_X, ELECTRO_H);
    
    protected final static class ElectroBot extends Boss {
        protected final static byte STATE_CHEW = 1;
        protected final static byte STATE_BUBBLE = 2;
        protected final static byte STATE_WAIT = 3;
        protected final static byte STATE_AIM = 4;
        protected final static byte STATE_CHARGE = 5;
        protected final static byte STATE_DISCHARGE = 6;
        protected final static byte STATE_JUMP = 7;
        protected final static int DAMAGE_FIELD = 3;
        protected final static Panmage[] taunts = new Panmage[4];
        protected final static Panmage[] chews = new Panmage[2];
        protected static Panmage still = null;
        protected static Panmage jump = null;
        protected static Panmage fall = null;
        protected static Panmage aim = null;
        protected static Panmage charge = null;
        protected static Panmage discharge = null;
        private ElectroProjectile chargeBall = null;
        private ElectroField field = null;
        private int yStart = 0;
        
        protected ElectroBot(final Segment seg) {
            super(ELECTRO_OFF_X, ELECTRO_H, seg);
        }
        
        @Override
        protected final void taunt() {
            yStart = getY();
            startState(STATE_CHEW, 84, getStill());
        }
        
        @Override
        protected final String[] getIntroMessages() {
            return new String[] {
                    "TODO Electro's line.",
                    "Null's line.",
                    "Electro's line."
                };
        }
        
        @Override
        protected final String[] getRematchMessages() {
            return new String[] { "Electro's line." };
        }
        
        @Override
        protected final boolean onWaiting() {
            if ((state == STATE_AIM) && (waitCounter == 1)) {
                new ElectroProjectile(this, 17, 15, 3);
            } else if ((state == STATE_CHARGE) && (waitCounter == 8)) {
                destroy(chargeBall);
                chargeBall = new ElectroProjectile(this, -11, 22, 0);
            } else if (state == STATE_DISCHARGE) {
                if (waitCounter == 1) {
                    field = new ElectroField(this);
                }
                if (waitCounter >= 1) {
                    for (final PlayerContext pc : BotsnBoltsGame.pcs) {
                        final Player player = pc.player;
                        final Panple pos = player.getPosition();
                        final float x = pos.getX(), y = pos.getY();
                        if (y < (yStart + 4)) {
                            player.hurt(DAMAGE_FIELD);
                        } else if (x < 28) {
                            player.hurt(DAMAGE_FIELD);
                        } else if (x > 355) {
                            player.hurt(DAMAGE_FIELD);
                        } else if (y > 161) {
                            player.hurt(DAMAGE_FIELD);
                        }
                    }
                }
            } else if ((state == STATE_JUMP) && (v < 0)) {
                changeView(getFall());
            } else if (state == STATE_CHEW) {
                changeView(getChew((waitCounter / 12) % 3));
            } else if (state == STATE_BUBBLE) {
                if (waitCounter >= 24) {
                    changeView(getStill());
                } else {
                    final int tauntIndex = waitCounter / 6;
                    final boolean changed = changeView(getTaunt(tauntIndex));
                    if (changed && (tauntIndex == 3)) {
                        //TODO sound
                    }
                }
            }
            return false;
        }
        
        @Override
        protected final boolean pickState() {
            final int r = rand(3);
            if (r == 0) {
                startState(STATE_AIM, 24, getAim());
            } else if (r == 1) {
                startState(STATE_CHARGE, 48, getCharge());
            } else {
                startJumps();
            }
            return false;
        }
        
        @Override
        protected final boolean continueState() {
            destroy(field);
            if (state == STATE_CHEW) {
                startState(STATE_BUBBLE, 24, getStill());
            } else if (state == STATE_BUBBLE) {
                startState(STATE_WAIT, 12, getStill());
            } else if (state == STATE_WAIT) {
                finishTaunt();
                startStill();
            } else if (state == STATE_CHARGE) {
                destroy(chargeBall);
                startState(STATE_DISCHARGE, 8, getDischarge());
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
            return STATE_JUMP;
        }
        
        @Override
        protected final float getJumpsV() {
            return 10;
        }
        
        @Override
        protected final int getJumpsHv() {
            return 5;
        }
        
        @Override
        protected final Panmage getStill() {
            return (still = getElectroImage(still, "electrobot/ElectroBot"));
        }
        
        @Override
        protected final Panmage getJump() {
            return (jump = getElectroImage(jump, "electrobot/ElectroBotJump"));
        }
        
        protected final static Panmage getFall() {
            return (fall = getElectroImage(fall, "electrobot/ElectroBotFall"));
        }
        
        protected final static Panmage getAim() {
            return (aim = getElectroImage(aim, "electrobot/ElectroBotAim"));
        }
        
        protected final static Panmage getCharge() {
            return (charge = getElectroImage(charge, "electrobot/ElectroBotCharge", ELECTRO_CHARGE_O));
        }
        
        protected final static Panmage getDischarge() {
            return (discharge = getElectroImage(discharge, "electrobot/ElectroBotDischarge", ELECTRO_CHARGE_O));
        }
        
        protected final static Panmage getTaunt(final int i) {
            Panmage img = taunts[i];
            if (img == null) {
                img = getElectroImage(null, "electrobot/ElectroBotTaunt" + (i + 1));
                taunts[i] = img;
            }
            return img;
        }
        
        protected final Panmage getChew(final int i) {
            if (i == 0) {
                return getStill();
            }
            Panmage img = chews[i - 1];
            if (img == null) {
                img = getElectroImage(null, "electrobot/ElectroBotChew" + i);
                chews[i - 1] = img;
            }
            return img;
        }
        
        protected final static Panmage getElectroImage(final Panmage img, final String name) {
            return getElectroImage(img, name, ELECTRO_O);
        }
        
        protected final static Panmage getElectroImage(final Panmage img, final String name, final Panple o) {
            return getImage(img, name, o, ELECTRO_MIN, ELECTRO_MAX);
        }
    }
    
    private final static class ElectroProjectile extends EnemyProjectile {
        protected static Panmage img1 = null;
        protected static Panmage img2 = null;
        private int timer = -5;
        
        private ElectroProjectile(final ElectroBot src, final int ox, final int oy, final float vx) {
            super(getImage1(), src, ox, oy, vx * src.getMirrorMultiplier(), 0);
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            super.onStep(event);
            timer++;
            if (timer >= 1) {
                timer = 0;
                changeView(getImage2());
                setMirror(Mathtil.rand());
                setFlip(Mathtil.rand());
                setRot(Mathtil.randi(0, 3));
            }
        }
        
        @Override
        protected final void onCollisionWithPlayerProjectile(final Projectile prj) {
            prj.burst();
        }
        
        protected final static Panmage getImage1() {
            return (img1 = Boss.getImage(img1, "electrobot/Ball1", BotsnBoltsGame.CENTER_8, BotsnBoltsGame.MIN_8, BotsnBoltsGame.MAX_8));
        }
        
        protected final static Panmage getImage2() {
            return (img2 = Boss.getImage(img2, "electrobot/Ball2", BotsnBoltsGame.CENTER_16, BotsnBoltsGame.MIN_16, BotsnBoltsGame.MAX_16));
        }
    }
    
    private final static class ElectroField extends Panctor {
        private static Panmage img = null;
        private final ElectroBot src;
        
        private ElectroField(final ElectroBot src) {
            if (img == null) {
                img = Boss.getImage(null, "electrobot/Electricity", null, null, null);
            }
            addActor(this);
            this.src = src;
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panlayer layer = getLayer();
            final int yBottom = src.getY(), yTop = yBottom + 152;
            final int xLeft = 16, xRight = BotsnBoltsGame.GAME_W - 24;
            for (int x = 16; x < xRight; x += 32) {
                renderer.render(layer, img, x, yBottom, BotsnBoltsGame.DEPTH_PROJECTILE, 0, Mathtil.rand() ? 4 : 20, 32, 8, 0, Mathtil.rand(), false);
                renderer.render(layer, img, x, yTop, BotsnBoltsGame.DEPTH_PROJECTILE, 0, Mathtil.rand() ? 4 : 20, 32, 8, 0, Mathtil.rand(), true);
            }
            for (int j = 0; j < 5; j++) {
                final int y = yBottom + (j * 32);
                renderer.render(layer, img, xLeft, y, BotsnBoltsGame.DEPTH_PROJECTILE, 0, Mathtil.rand() ? 4 : 20, 32, 8, 3, false, Mathtil.rand());
                renderer.render(layer, img, xRight, y, BotsnBoltsGame.DEPTH_PROJECTILE, 0, Mathtil.rand() ? 4 : 20, 32, 8, 1, false, Mathtil.rand());
            }
        }
    }
    
    protected final static int CHRONO_OFF_X = 6, CHRONO_H = 26;
    protected final static Panple CHRONO_O = new FinPanple2(13, 1);
    protected final static Panple CHRONO_JUMP_O = new FinPanple2(15, 2);
    protected final static Panple CHRONO_FALL_O = new FinPanple2(12, 2);
    protected final static Panple CHRONO_MIN = getMin(CHRONO_OFF_X);
    protected final static Panple CHRONO_MAX = getMax(CHRONO_OFF_X, CHRONO_H);
    
    protected final static class ChronoBot extends Boss {
        protected final static byte STATE_SNAP1 = 1;
        protected final static byte STATE_SNAP2 = 2;
        protected final static byte STATE_AIM = 3;
        protected final static byte STATE_WAIT_FOR_PROJECTILE = 4;
        protected final static byte STATE_WAIT_FOR_RESUME = 5;
        protected final static byte STATE_JUMP_TO_TARGET_HEIGHT = 6;
        protected final static byte STATE_JUMP_AIM = 7;
        protected final static byte STATE_FALL = 8;
        protected final static byte STATE_JUMP = 9;
        private final static int SPEED_JUMP_Y = 10;
        protected static Panmage still = null;
        protected static Panmage jump = null;
        protected static Panmage jumpAim = null;
        protected static Panmage fall = null;
        protected static Panmage aim = null;
        protected static Panmage snap1 = null;
        protected static Panmage snap2 = null;
        private final static Set<ChronoProjectile> projectilesOnScreen = new IdentityHashSet<ChronoProjectile>();
        private final int xRight;
        private final int xLeft;
        private int yStart;
        private int yProjectile;
        private int yMine;
        private int yJumpReverse;
        private Player target = null;
        private float targetY = -1;
        private boolean projectileForced = false;
        private int reverseCount = 0;
        private boolean reverseNeeded = false;
        
        protected ChronoBot(final Segment seg) {
            super(CHRONO_OFF_X, CHRONO_H, seg);
            projectilesOnScreen.clear();
            xRight = getX();
            xLeft = getMirroredX(xRight);
        }
        
        @Override
        protected final int getInitialOffsetX() {
            return 0;
        }
        
        @Override
        protected final void taunt() {
            yStart = getY();
            yProjectile = yStart + 16; // Full jump reaches height of 52 pixels (above yStart)
            yMine = yStart + 43;
            yJumpReverse = yStart + 64;
            startSnap();
        }
        
        @Override
        protected final String[] getIntroMessages() {
            return new String[] {
                    "You're late!  Punctuality is very important to me.  I don't like falling behind schedule.",
                    "You'll like what happens next even less.",
                    "We'll see about that.  It's time to battle!"
                };
        }
        
        @Override
        protected final String[] getRematchMessages() {
            return new String[] { "It's time to battle!" };
        }
        
        @Override
        protected final boolean onWaiting() {
            if (state == STATE_WAIT_FOR_PROJECTILE) {
                if (isSnapAllowed()) {
                    startSnap();
                }
            } else if (state == STATE_WAIT_FOR_RESUME) {
                final Player player = getNearestPlayer();
                if ((player == null) || !player.isStopped()) {
                    startStill(); // Could maybe just call pickState
                }
            } else if (state == STATE_JUMP_AIM) {
                if (waitCounter == 1) {
                    new ChronoProjectile(this, CHRONO_PROJECTILE_OX, CHRONO_PROJECTILE_OY + 2, CHRONO_PROJECTILE_V * getMirrorMultiplier(), 0);
                }
                return true;
            } else if ((state == STATE_AIM) && (waitCounter == 1)) {
                if (projectileForced || (targetY <= yProjectile)) {
                    new ChronoProjectile(this, CHRONO_PROJECTILE_OX, CHRONO_PROJECTILE_OY, CHRONO_PROJECTILE_V * getMirrorMultiplier(), 0);
                    projectileForced = false;
                } else {
                    new ChronoMine(this, getPlayerX(target));
                }
            } else if ((state == STATE_JUMP_TO_TARGET_HEIGHT) && (getPosition().getY() >= targetY)) {
                getPosition().setY(targetY);
                startState(STATE_JUMP_AIM, 16, getJumpAim());
            } else if ((state == STATE_JUMP) && (v < 0)) {
                if (reverseNeeded && (reverseCount < 2) && (getPosition().getY() < yJumpReverse)) {
                    v = -v + g;
                    hv = -hv;
                    reverseCount++;
                } else {
                    changeView((reverseCount == 1) ? getJump() : getFall());
                }
            }
            return false;
        }
        
        @Override
        protected final boolean isMirrorAdjustedByAddX() {
            return reverseCount == 0;
        }
        
        @Override
        protected final boolean pickState() {
            reverseNeeded = false;
            if (isPlayerStopped()) {
                return false;
            } else if (isPlayerBehindBoss()) {
                startJump();
                return false;
            }
            final int r = rand(4);
            if (r == 0) {
                projectileForced = true;
                startAim();
            } else if (r == 1) {
                startJump();
            } else if (!isSnapAllowed()) { // 2 or 3, snap twice as likely as other options
                startStateIndefinite(STATE_WAIT_FOR_PROJECTILE, getStill());
            } else {
                startSnap();
            }
            return false;
        }
        
        private final boolean isPlayerBehindBoss() {
            final int dir = getDirection();
            if ((dir < 0) && isPlayerXBetween(getX(), BotsnBoltsGame.GAME_W)) {
                return true;
            } else if ((dir > 0) && isPlayerXBetween(0, getX() + 1)) {
                return true;
            }
            return false;
        }
        
        private final boolean isPlayerNearBoss() {
            final int x = getX();
            return isPlayerXBetween(x - 64, x + 65);
        }
        
        private final boolean isSnapAllowed() {
            return projectilesOnScreen.isEmpty() && !isPlayerInvincible();
        }
        
        @Override
        protected final boolean continueState() {
            if (state == STATE_SNAP1) {
                startSnap2();
            } else if (state == STATE_SNAP2) {
                if (finishTaunt()) {
                    startStill();
                    return false;
                } else if (isPlayerNearBoss()) {
                    startJump1();
                    return false;
                }
                target = getNearestPlayer();
                targetY = getPlayerY(target);
                if (targetY <= yProjectile) {
                    if (isPlayerBetween(128, 0, 256, yProjectile + 1) && Mathtil.rand()) {
                        startJump1();
                    } else {
                        startAim();
                    }
                } else if (targetY >= yMine) {
                    startAim();
                } else {
                    startJumpToTargetHeight();
                }
            } else if (state == STATE_AIM) {
                startStateIndefinite(STATE_WAIT_FOR_RESUME, getStill());
            } else if (state == STATE_JUMP_AIM) {
                startJump(STATE_FALL, getFall(), 0, 0);
            } else {
                startStill();
            }
            return false;
        }
        
        @Override
        protected final void onBossLandedAny() {
            reverseCount = 0;
            if (state == STATE_JUMP) {
                resumePlayers();
            }
        }
        
        @Override
        protected final void onJumpLanded() {
            reverseCount = 0;
            adjustX();
        }
        
        private final void adjustX() {
            final int x = getX();
            if (Math.abs(x - xLeft) < 3) {
                getPosition().setX(xLeft);
            } else if (Math.abs(x - xRight) < 3) {
                getPosition().setX(xRight);
            }
        }
        
        private final int getSnapFrameDuration() {
            return isTauntFinished() ? 6 : 12;
        }
        
        private final void startSnap() {
            turnTowardPlayer();
            startState(STATE_SNAP1, getSnapFrameDuration(), getSnap1());
        }
        
        private final void startSnap2() {
            startState(STATE_SNAP2, getSnapFrameDuration(), getSnap2());
            //TODO sound
            if (!isTauntFinished()) {
                return;
            }
            stopPlayers();
        }
        
        private final void startAim() {
            turnTowardPlayer();
            startState(STATE_AIM, 16, getAim());
        }
        
        private final void startJumpToTargetHeight() {
            startJump(STATE_JUMP_TO_TARGET_HEIGHT, getJump(), Player.VEL_JUMP, 0);
        }
        
        private final void startJump() {
            reverseNeeded = true;
            if (Mathtil.rand()) {
                startJump1();
            } else {
                final Panmage img = getJump();
                final int hv = getDirection() * 3;
                startJump(STATE_JUMP, img, SPEED_JUMP_Y, hv);
                addPendingJump(STATE_JUMP, img, SPEED_JUMP_Y, hv);
                addPendingJump(STATE_JUMP, img, SPEED_JUMP_Y, hv);
            }
        }
        
        private final void startJump1() {
            startJump(STATE_JUMP, getJump(), SPEED_JUMP_Y, getDirection() * 9);
        }
        
        @Override
        protected final Panmage getStill() {
            return (still = getChronoImage(still, "chronobot/ChronoBot"));
        }
        
        @Override
        protected final Panmage getJump() {
            return (jump = getChronoImage(jump, "chronobot/ChronoBotJump", CHRONO_JUMP_O));
        }
        
        protected final static Panmage getJumpAim() {
            return (jumpAim = getChronoImage(jumpAim, "chronobot/ChronoBotJumpAim", CHRONO_FALL_O));
        }
        
        protected final static Panmage getFall() {
            return (fall = getChronoImage(fall, "chronobot/ChronoBotFall", CHRONO_FALL_O));
        }
        
        protected final static Panmage getAim() {
            return (aim = getChronoImage(aim, "chronobot/ChronoBotAim"));
        }
        
        protected final static Panmage getSnap1() {
            return (snap1 = getChronoImage(snap1, "chronobot/ChronoBotSnap1"));
        }
        
        protected final static Panmage getSnap2() {
            return (snap2 = getChronoImage(snap2, "chronobot/ChronoBotSnap2"));
        }
        
        protected final static Panmage getChronoImage(final Panmage img, final String name) {
            return getChronoImage(img, name, CHRONO_O);
        }
        
        protected final static Panmage getChronoImage(final Panmage img, final String name, final Panple o) {
            return getImage(img, name, o, CHRONO_MIN, CHRONO_MAX);
        }
    }
    
    private final static int CHRONO_PROJECTILE_OX = 17;
    private final static int CHRONO_PROJECTILE_OY = 16;
    private final static int CHRONO_PROJECTILE_V = 4;
    
    private final static class ChronoProjectile extends EnemyProjectile {
        protected static Panmage img1 = null;
        protected static Panmage img2 = null;
        private int timer = 0;
        
        private ChronoProjectile(final Panctor src, final int ox, final int oy, final float vx, final float vy) {
            super(getImage1(), src, ox, oy, vx, vy);
            ChronoBot.projectilesOnScreen.add(this);
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            super.onStep(event);
            final Player target = getNearestPlayer(this);
            if ((target != null) && target.isStopped() && (Math.abs(target.getPosition().getX() - getPosition().getX()) < 64)) {
                resumePlayers();
            }
            timer++;
            if (timer >= 8) {
                timer = 0;
                changeView(getImage1());
            } else if (timer >= 4) {
                changeView(getImage2());
            }
        }
        
        @Override
        protected final int getDamage() {
            return 3;
        }
        
        @Override
        protected final boolean hurt(final Player player) {
            resumePlayers();
            return super.hurt(player);
        }
        
        @Override
        protected final void onCollisionWithPlayerProjectile(final Projectile prj) {
            prj.burst();
        }
        
        @Override
        protected final void onDestroy() {
            resumePlayers();
            ChronoBot.projectilesOnScreen.remove(this);
        }
        
        protected final static Panmage getImage1() {
            return (img1 = Boss.getImage(img1, "chronobot/Projectile1", BotsnBoltsGame.CENTER_8, BotsnBoltsGame.MIN_8, BotsnBoltsGame.MAX_8));
        }
        
        protected final static Panmage getImage2() {
            return (img2 = Boss.getImage(img2, "chronobot/Projectile2", BotsnBoltsGame.CENTER_16, BotsnBoltsGame.MIN_8, BotsnBoltsGame.MAX_8));
        }
    }
    
    private final static Panple CHRONO_MINE_O = new FinPanple2(16, 1);
    
    private final static class ChronoMine extends Panctor implements StepListener {
        private final static float VX_SPREAD = Player.VX_SPREAD2 * CHRONO_PROJECTILE_V / Player.VEL_PROJECTILE;
        private final static float VY_SPREAD = Player.VY_SPREAD2 * CHRONO_PROJECTILE_V / Player.VEL_PROJECTILE;
        private final static float distanceMultiplier = 0.0875f;
        protected static Panmage img1 = null;
        protected static Panmage img2 = null;
        private final ChronoBot src;
        private final float dstX;
        
        private ChronoMine(final ChronoBot src, final float dstX) {
            EnemyProjectile.addBySource(this, getImage2(), src, CHRONO_PROJECTILE_OX, CHRONO_PROJECTILE_OY);
            this.src = src;
            this.dstX = dstX;
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            final Panple pos = getPosition();
            final float oldX = pos.getX();
            final float distance = dstX - oldX;
            float offX = distanceMultiplier * distance;
            if (isMirror()) {
                if (offX > -1) {
                    offX = -1;
                }
            } else if (offX < 1) {
                offX = 1;
            }
            if (Math.abs(offX) <= 1) {
                setView((getView() == getImage2()) ? getImage1() : getImage2());
            }
            float newX = oldX + offX;
            if (Math.abs(dstX - newX) <= 1.0f) {
                newX = dstX;
                resumePlayers();
                destroy();
                new ChronoProjectile(this, 0, 0, 0, CHRONO_PROJECTILE_V);
                new ChronoProjectile(this, 0, 0, VX_SPREAD, VY_SPREAD);
                new ChronoProjectile(this, 0, 0, -VX_SPREAD, VY_SPREAD);
            }
            pos.set(
                    newX,
                    Math.max(pos.getY() + g, src.yStart));
        }
        
        protected final static Panmage getImage1() {
            return (img1 = Boss.getImage(img1, "chronobot/Mine1", CHRONO_MINE_O, BotsnBoltsGame.MIN_8, BotsnBoltsGame.MAX_8));
        }
        
        protected final static Panmage getImage2() {
            return (img2 = Boss.getImage(img2, "chronobot/Mine2", CHRONO_MINE_O, BotsnBoltsGame.MIN_8, BotsnBoltsGame.MAX_8));
        }
    }
    
    protected final static int MICRO_OFF_X = 5, MICRO_H = 23;
    protected final static Panple MICRO_O = new FinPanple2(12, 1);
    protected final static Panple MICRO_SMALL_O = new FinPanple2(4, 1);
    protected final static Panple MICRO_MIN = getMin(MICRO_OFF_X);
    protected final static Panple MICRO_MAX = getMax(MICRO_OFF_X, MICRO_H);
    
    protected final static class MicroBot extends Boss {
        protected final static byte STATE_SHRINK = 1;
        protected final static byte STATE_ZOOM = 2;
        protected final static byte STATE_ZOOM_FINISH = 3;
        protected final static byte STATE_UNSHRINK = 4;
        protected final static byte STATE_JUMP_AIM = 5;
        protected final static byte STATE_FALL = 6;
        protected final static byte STATE_AIM = 7;
        protected final static byte STATE_JUMPS = 8;
        protected final static byte STATE_WAIT_SHRUNK = 9;
        protected final static int DURATION_AIM = 16;
        protected final static int SPEED_ZOOM = 12;
        protected final static float SPEED_PROJECTILE = 9.0f;
        protected final static float SPEED_PROJECTILE_DIAGONAL = Player.VX_SPREAD1 * SPEED_PROJECTILE / Player.VEL_PROJECTILE;
        protected final static int SURFACE_LEFT = 1;
        protected final static int SURFACE_RIGHT = 2;
        protected final static int SURFACE_TOP = 3;
        protected final static Integer KEY_LEFT = Integer.valueOf(SURFACE_LEFT);
        protected final static Integer KEY_RIGHT = Integer.valueOf(SURFACE_RIGHT);
        protected final static Integer KEY_TOP = Integer.valueOf(SURFACE_TOP);
        protected static Panmage still = null;
        protected static Panmage jump = null;
        protected static Panmage aim = null;
        protected static Panmage jumpAim = null;
        protected static Panmage projectile = null;
        protected static Panmage projectile2 = null;
        protected final static Panmage[] shrinks = new Panmage[4];
        private final Panple dst = new ImplPanple();
        private final static int xLow = LEFT_X + 40;
        private final static int xHigh = RIGHT_X - 40;
        private final int xRight;
        private final int xLeft;
        private int yBottom = -1;
        private int yLow = -1;
        private int yHigh = -1;
        private int yTop = -1;
        private int zoomCount = 0;
        private int shrinkLevel = 0;
        private final MicroOrbit[] orbits = new MicroOrbit[3];
        
        protected MicroBot(final Segment seg) {
            super(MICRO_OFF_X, MICRO_H, seg);
            xRight = getX();
            xLeft = getMirroredX(xRight);
            getProjectile();
            final double limit3 = MicroOrbit.limit / 3.0;
            for (int i = 0; i < 3; i++) {
                orbits[i] = new MicroOrbit(this, i * limit3, i == 1, projectile);
            }
        }
        
        @Override
        protected final void taunt() {
            yBottom = getY();
            yLow = yBottom + 40;
            yHigh = yLow + 80;
            yTop = yHigh + 40;
            startShrink();
        }
        
        @Override
        protected final String[] getIntroMessages() {
            return new String[] {
                    "TODO MICRO",
                    "NULL",
                    "MICRO"
                };
        }
        
        @Override
        protected final String[] getRematchMessages() {
            return new String[] { "TODO" };
        }
        
        @Override
        protected final boolean onWaiting() {
            if ((state == STATE_ZOOM) || (state == STATE_ZOOM_FINISH)) {
                final Panple pos = getPosition();
                Panple.subtract(scratchPanple, dst, pos);
                final double distance = scratchPanple.getMagnitude2();
                if (distance < SPEED_ZOOM) {
                    pos.set2(dst);
                    if (state == STATE_ZOOM_FINISH) {
                        startUnshrink();
                    } else if ((zoomCount < 4) && Mathtil.rand(65)) {
                        continueZoom();
                    } else {
                        finishZoom();
                    }
                } else {
                    final float mag = (float) (SPEED_ZOOM / distance);
                    scratchPanple.set(scratchPanple.getX() * mag, scratchPanple.getY() * mag);
                    pos.add2(scratchPanple);
                }
                return true;
            } else if (state == STATE_JUMP_AIM) {
                newProjectileIfNeeded(14, 6, SPEED_PROJECTILE_DIAGONAL, -SPEED_PROJECTILE_DIAGONAL);
                return true;
            } else if (state == STATE_SHRINK) {
                changeView(pickShrink(waitCounter / 4));
            } else if (state == STATE_UNSHRINK) {
                changeView(pickShrink(waitTimer / 4));
                return true;
            } else if (state == STATE_AIM) {
                newProjectileIfNeeded(18, 12, SPEED_PROJECTILE, 0);
            } else if (state == STATE_JUMPS) {
                final int y = getY();
                if (y > 137) {
                    changeView(pickShrink(3));
                } else if (y > 129) {
                    changeView(pickShrink(2));
                } else if (y > 119) {
                    changeView(pickShrink(1));
                } else if (y > 105) {
                    changeView(pickShrink(0));
                } else {
                    shrinkLevel = 0;
                    changeView(getJump());
                }
            }
            return false;
        }
        
        private final void newProjectileIfNeeded(final int ox, final int oy, final float vx, final float vy) {
            if ((waitCounter == 1) || (waitCounter == 4) || (waitCounter == 7)) {
                newProjectile(ox, oy, vx * getMirrorMultiplier(), vy);
            }
        }
        
        @Override
        protected final boolean pickState() {
            final int r = rand(3);
            if (r == 0) {
                startShrink();
            } else if (r == 1) {
                startAim();
            } else {
                startJumps();
            }
            return false;
        }
        
        @Override
        protected final boolean continueState() {
            if (state == STATE_SHRINK) {
                if (isTauntFinished()) {
                    startZoom();
                } else {
                    startState(STATE_WAIT_SHRUNK, 24, getShrink(3));
                }
            } else if (state == STATE_UNSHRINK) {
                shrinkLevel = 0;
                if (finishTaunt()) {
                    startStill();
                } else if (isGrounded()) {
                    startAim();
                } else {
                    startJumpAim();
                }
            } else if (state == STATE_JUMP_AIM) {
                startJump(STATE_FALL, getJump(), 0, 0);
            } else if (state == STATE_WAIT_SHRUNK) {
                startUnshrink();
            } else {
                startStill();
            }
            return false;
        }
        
        private final void startShrink() {
            startState(STATE_SHRINK, 8, pickShrink(0));
            //TODO sound
        }
        
        private final void startZoom() {
            zoomCount = 1;
            dst.set(getPosition().getX(), yTop);
            startStateZoom();
        }
        
        private final void continueZoom() {
            zoomCount++;
            scratchInts.clear();
            scratchInts.add(KEY_LEFT);
            scratchInts.add(KEY_RIGHT);
            scratchInts.add(KEY_TOP);
            if (getY() > yHigh) {
                scratchInts.remove(KEY_TOP);
            }
            if (getX() < MID_X) {
                scratchInts.remove(KEY_LEFT);
            } else {
                scratchInts.remove(KEY_RIGHT);
            }
            final int surface = Mathtil.rand(scratchInts).intValue();
            if (surface == SURFACE_LEFT) {
                dst.set(LEFT_X, Mathtil.randi(yLow, yHigh));
            } else if (surface == SURFACE_RIGHT) {
                dst.set(RIGHT_X, Mathtil.randi(yLow, yHigh));
            } else if (surface == SURFACE_TOP) {
                dst.set(Mathtil.randi(xLow, xHigh), yTop);
            } else {
                throw new IllegalStateException("Unexpected surface " + surface);
            }
            startStateZoom();
        }
        
        private final void finishZoom() {
            final float targetX = getNearestPlayerX(), dstX, dstY;
            if (targetX < MID_X) {
                if (Mathtil.rand()) {
                    dstX = targetX + 120;
                    dstY = yBottom + 120;
                } else {
                    dstX = xRight;
                    dstY = yBottom;
                }
                setMirror(true);
            } else {
                if (Mathtil.rand()) {
                    dstX = targetX - 120;
                    dstY = yBottom + 120;
                } else {
                    dstX = xLeft;
                    dstY = yBottom;
                }
                setMirror(false);
            }
            dst.set(dstX, dstY);
            startStateIndefinite(STATE_ZOOM_FINISH, pickShrink(3));
        }
        
        private final void startStateZoom() {
            hv = 0;
            startStateIndefinite(STATE_ZOOM, pickShrink(3));
            //TODO sound
        }
        
        private final void startUnshrink() {
            startState(STATE_UNSHRINK, 8, pickShrink(3));
            //TODO sound
        }
        
        private final void startJumpAim() {
            startState(STATE_JUMP_AIM, DURATION_AIM, getJumpAim());
        }
        
        private final void startAim() {
            turnTowardPlayer();
            startState(STATE_AIM, DURATION_AIM, getAim());
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
            return 11.8f;
        }
        
        @Override
        protected final int getJumpsHv() {
            return 4;
        }
        
        private final void newProjectile(final int ox, final int oy, final float vx, final float vy) {
            new EnemyProjectile(getProjectile(), this, ox, oy, vx, vy);
            BotsnBoltsGame.fxEnemyAttack.startSound();
        }
        
        @Override
        protected final Panmage getStill() {
            return (still = getMicroImage(still, "microbot/MicroBot"));
        }
        
        @Override
        protected final Panmage getJump() {
            return (jump = getMicroImage(jump, "microbot/MicroBotJump"));
        }
        
        protected final static Panmage getAim() {
            return (aim = getMicroImage(aim, "microbot/MicroBotAim"));
        }
        
        protected final static Panmage getJumpAim() {
            return (jumpAim = getMicroImage(jumpAim, "microbot/MicroBotJumpAim"));
        }
        
        protected final static Panmage getProjectile() {
            return (projectile = getImage(projectile, "microbot/Projectile", BotsnBoltsGame.CENTER_8, BotsnBoltsGame.MIN_4, BotsnBoltsGame.MAX_4));
        }
        
        protected final static Panmage getProjectile2() {
            return (projectile2 = getImage(projectile2, "microbot/Projectile2", BotsnBoltsGame.CENTER_4, BotsnBoltsGame.MIN_4, BotsnBoltsGame.MAX_4));
        }
        
        protected final Panmage pickShrink(final int i) {
            shrinkLevel = i + 1;
            return getShrink(i);
        }
        
        protected final static Panmage getShrink(final int i) {
            Panmage img = shrinks[i];
            if (img == null) {
                final Panple o, n, x;
                if (i == 0) {
                    o = MICRO_O;
                    n = getMin(4);
                    x = getMax(4, 17);
                } else if (i == 1) {
                    o = MICRO_SMALL_O;
                    n = getMin(3);
                    x = getMax(3, 9);
                } else if (i == 2) {
                    o = MICRO_SMALL_O;
                    n = getMin(2);
                    x = getMax(2, 6);
                } else if (i == 3) {
                    o = new FinPanple2(1, 1);
                    n = getMin(1);
                    x = getMax(1, 2);
                } else {
                    throw new IllegalStateException("Unexpected shrink index " + i);
                }
                img = getImage(null, "microbot/MicroBotShrink" + (i + 1), o, n, x);
                shrinks[i] = img;
            }
            return img;
        }
        
        protected final static Panmage getMicroImage(final Panmage img, final String name) {
            return getImage(img, name, MICRO_O, MICRO_MIN, MICRO_MAX);
        }
        
        @Override
        protected final void onEnemyDestroy() {
            for (final MicroOrbit orbit : orbits) {
                destroy(orbit);
            }
        }
    }
    
    protected final static class MicroOrbit extends Panctor implements StepEndListener {
        private final static double a = 16.0;
        private final static double b = 8.0;
        private final static double limit = 2.0 * Math.PI;
        private final static double delta = limit / 32.0;
        private final MicroBot src;
        private final double aCosTheta;
        private final double bSinTheta;
        private final double aSinTheta;
        private final double bCosTheta;
        private final boolean opposite;
        private double t = 0; // 0 <= t < 2 * pi
        
        private MicroOrbit(final MicroBot src, final double theta, final boolean opposite, final Panmage img) {
            this.src = src;
            final double cosTheta = Math.cos(theta);
            final double sinTheta = Math.sin(theta);
            aCosTheta = a * cosTheta;
            bSinTheta = b * sinTheta;
            aSinTheta = a * sinTheta;
            bCosTheta = b * cosTheta;
            this.opposite = opposite;
            setView(img);
            addActor(this);
        }

        @Override
        public final void onStepEnd(final StepEndEvent event) {
            final int shrinkLevel = src.shrinkLevel;
            if (shrinkLevel > 2) {
                setVisible(false);
                return;
            }
            final double sizeMult;
            if (shrinkLevel == 2) {
                sizeMult = 1.0 / 3.0;
                changeView(MicroBot.getShrink(3));
            } else if (shrinkLevel == 1) {
                sizeMult = 2.0 / 3.0;
                changeView(MicroBot.getProjectile2());
            } else {
                sizeMult = 1.0;
                changeView(MicroBot.getProjectile());
            }
            setVisible(true);
            final Panple spos = src.getPosition();
            final double cx = spos.getX(), cy = spos.getY() + (sizeMult * 8);
            final double cosT = Math.cos(t), sinT = Math.sin(t);
            setMirror(!src.isMirror());
            final double ox = ((sizeMult * aCosTheta * cosT) - (sizeMult * bSinTheta * sinT)) * -src.getMirrorMultiplier();
            final double oy = (sizeMult * aSinTheta * cosT) + (sizeMult * bCosTheta * sinT);
            boolean front = t >= Math.PI;
            if (opposite) {
                front = !front;
            }
            final float z = front ? BotsnBoltsGame.DEPTH_ENEMY_FRONT : BotsnBoltsGame.DEPTH_ENEMY_BACK;
            getPosition().set((float) (cx + ox), (float) (cy + oy), z);
            t += delta;
            if (t >= limit) {
                t -= limit;
            }
        }
    }
    
    protected final static int AERO_OFF_X = 15, AERO_H = 14;
    protected final static Panple AERO_O = new FinPanple2(30, 11);
    protected final static Panple AERO_MIN = getMin(AERO_OFF_X);
    protected final static Panple AERO_MAX = getMax(AERO_OFF_X, AERO_H);
    
    protected final static class AeroBot extends Boss {
        protected final static byte STATE_START = 1;
        protected final static byte STATE_VERT_START = 2;
        protected final static byte STATE_VERT = 3;
        protected final static byte STATE_WAIT = 4;
        protected final static byte STATE_GLIDE = 5;
        protected final static byte STATE_AIM = 6;
        protected final static byte STATE_DIVE = 7;
        protected final static byte STATE_VERT_AIM = 8;
        protected final static int SPEED = 6;
        protected final static float V_STRAIGHT = Player.VEL_PROJECTILE;
        protected final static float V_DIAGONAL = Player.VX_SPREAD1;
        protected static Panmage still = null;
        protected static Panmage aimForward = null;
        protected static Panmage aimDownward = null;
        protected static Panmage aimBackward = null;
        protected static Panmage vert = null;
        protected static Panmage vertStart = null;
        protected static Panmage vertAim = null;
        protected final static Panmage[] starts = new Panmage[7];
        private int yStart;
        private int yGlide;
        private int yAim;
        private boolean reachedScreenYet = false;
        private boolean launchedProjectileYet = false;
        
        protected AeroBot(final Segment seg) {
            super(AERO_OFF_X, AERO_H, seg);
        }
        
        @Override
        protected final void taunt() {
            yStart = getY();
            yGlide = yStart + 20;
            yAim = yStart + 120;
            startState(STATE_START, 30, getStart(0));
        }
        
        @Override
        protected final String[] getIntroMessages() {
            return new String[] {
                    "TODO AERO",
                    "NULL",
                    "AERO"
                };
        }
        
        @Override
        protected final String[] getRematchMessages() {
            return new String[] { "TODO" };
        }
        
        @Override
        protected final boolean onWaiting() {
            if ((state == STATE_GLIDE) || (state == STATE_AIM)) {
                getPosition().addX(isMirror() ? -SPEED : SPEED);
                if (state == STATE_AIM) {
                    if (waitCounter == 12) {
                        setView(getAimForward());
                    } else if (waitCounter == 13) {
                        new AeroProjectile(this, 11, -5, V_DIAGONAL * getMirrorMultiplier(), -V_DIAGONAL);
                    } else if (waitCounter == 36) {
                        setView(getAimDownward());
                    } else if (waitCounter == 37) {
                        new AeroProjectile(this, 4, -9, 0, -V_STRAIGHT);
                    } else if (waitCounter == 60) {
                        setView(getAimBackward());
                    } else if (waitCounter == 61) {
                        new AeroProjectile(this, -3, -5, V_DIAGONAL * -getMirrorMultiplier(), -V_DIAGONAL);
                    }
                }
                checkScreen();
                return true;
            } else if (state == STATE_DIVE) {
                getPosition().addY(-SPEED);
                checkScreen();
                return true;
            } else if (state == STATE_WAIT) {
                return true;
            } else if ((state == STATE_VERT) || (state == STATE_VERT_AIM)) {
                final Panple pos = getPosition();
                pos.addY(SPEED);
                if (state == STATE_VERT_AIM) {
                    final int y = Math.round(pos.getY());
                    if (y > (yStart + 54)) {
                        changeView(getVert());
                    } else if (y >= (yStart - 6)) {
                        if (!changeView(getVertAim()) && !launchedProjectileYet) {
                            new AeroProjectile(this, 19, 17, V_STRAIGHT * getMirrorMultiplier(), 0);
                            launchedProjectileYet = true;
                        }
                    }
                }
                checkScreen();
                return true;
            } else if (state == STATE_START) {
                switch (waitCounter) {
                    case 6 :
                        setView(getStart(1));
                        break;
                    case 9 :
                        setView(getStart(2));
                        //TODO sound
                        break;
                    case 15 :
                        setView(getStart(3));
                        break;
                    case 18 :
                        setView(getStart(4));
                        //TODO sound
                        break;
                    case 24 :
                        setView(getStart(5));
                        break;
                    case 27 :
                        setView(getStart(6));
                        //TODO sound
                        break;
                }
            }
            return false;
        }
        
        private final void checkScreen() {
            final boolean inView = isInView();
            if (reachedScreenYet) {
                if (!inView) {
                    startWaitIfNeeded();
                }
            } else {
                reachedScreenYet = inView;
            }
        }
        
        private final void startWaitIfNeeded() {
            final Panple pos = getPosition();
            final float x = pos.getX();
            if ((x < -64.0f) || (x >= 448.0f)) {
                startWait();
            }
            final float y = pos.getY();
            if ((y < -64.0f) || (y >= 288.0f)) {
                startWait();
            }
        }
        
        @Override
        protected final boolean pickState() {
            setRot(0);
            if (moves == 0) {
                startState(STATE_VERT_START, 5, getVertStart());
                return false;
            }
            final int r = rand(3);
            if (r == 0) {
                startGlide();
                return true;
            } else if (r == 1) {
                startAim();
                return true;
            } else {
                if (getY() > 112) {
                    startDive();
                } else {
                    startVertAim();
                }
                return true;
            }
        }
        
        @Override
        protected final boolean continueState() {
            if (state == STATE_WAIT) {
                return runPickState();
            } else if (state == STATE_VERT) {
                startWait();
            } else if (state == STATE_VERT_START) {
                startStateIndefinite(STATE_VERT, getVert());
            } else if (state == STATE_START) {
                startStill(getStart(6));
            } else {
                startStill();
            }
            return false;
        }
        
        private final void startWait() {
            setRot(0);
            startState(STATE_WAIT, Mathtil.randi(20, 60), getStill());
        }
        
        private final void startGlide() {
            startFly(STATE_GLIDE, yGlide);
        }
        
        private final void startAim() {
            startFly(STATE_AIM, yAim);
        }
        
        private final void startFly(final byte state, final int y) {
            final float x;
            if (getX() > MID_X) {
                x = RIGHT_X + 48;
                setMirror(true);
            } else {
                x = LEFT_X - 48;
                setMirror(false);
            }
            getPosition().set(x, y);
            reachedScreenYet = false;
            startStateIndefinite(state, getStill());
        }
        
        private final void startDive() {
            final Panple pos = getPosition();
            pos.set(getNearestPlayerX(), BotsnBoltsGame.GAME_H + 32);
            if (pos.getX() < MID_X) {
                setMirror(true);
                setRot(3);
            } else {
                setMirror(false);
                setRot(3);
            }
            reachedScreenYet = false;
            startStateIndefinite(STATE_DIVE, getStill());
        }
        
        private final void startVertAim() {
            final float x;
            if (getNearestPlayerX() < MID_X) {
                x = RIGHT_X - 48;
                setMirror(true);
            } else {
                x = LEFT_X + 48;
                setMirror(false);
            }
            getPosition().set(x, -32);
            reachedScreenYet = false;
            launchedProjectileYet = false;
            startStateIndefinite(STATE_VERT_AIM, getVert());
        }
        
        @Override
        protected final Panmage getStill() {
            if (isTauntFinished()) {
                return (still = getAeroImage(still, "aerobot/AeroBot"));
            }
            return getStart(0);
        }
        
        protected final static Panmage getAimForward() {
            return (aimForward = getAeroImage(aimForward, "aerobot/AeroBotAimForward"));
        }
        
        protected final static Panmage getAimDownward() {
            return (aimDownward = getAeroImage(aimDownward, "aerobot/AeroBotAimDownward"));
        }
        
        protected final static Panmage getAimBackward() {
            return (aimBackward = getAeroImage(aimBackward, "aerobot/AeroBotAimBackward"));
        }
        
        protected final static Panmage getVert() {
            return (vert = getAeroImage(vert, "aerobot/AeroBotVert"));
        }
        
        protected final static Panmage getVertStart() {
            return (vertStart = getAeroImage(vertStart, "aerobot/AeroBotVertStart"));
        }
        
        protected final static Panmage getVertAim() {
            return (vertAim = getAeroImage(vertAim, "aerobot/AeroBotVertAim"));
        }
        
        protected final static Panmage getStart(final int i) {
            Panmage img = starts[i];
            if (img == null) {
                img = getAeroImage(null, "aerobot/AeroBotStart" + (i + 1));
                starts[i] = img;
            }
            return img;
        }
        
        protected final static Panmage getAeroImage(final Panmage img, final String name) {
            return getImage(img, name, AERO_O, AERO_MIN, AERO_MAX);
        }
    }
    
    private final static class AeroProjectile extends EnemyProjectile {
        protected static Panmage img1 = null;
        protected static Panmage img2 = null;
        private int timer = 0;
        
        private AeroProjectile(final Panctor src, final int ox, final int oy, final float vx, final float vy) {
            super(getImage1(), src, ox, oy, vx, vy);
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            super.onStep(event);
            timer++;
            if (timer >= 8) {
                timer = 0;
                changeView(getImage1());
            } else if (timer >= 4) {
                changeView(getImage2());
            }
        }
        
        @Override
        protected final int getDamage() {
            return 3;
        }
        
        protected final static Panmage getImage1() {
            return (img1 = Boss.getImage(img1, "aerobot/Projectile1", BotsnBoltsGame.CENTER_8, BotsnBoltsGame.MIN_8, BotsnBoltsGame.MAX_8));
        }
        
        protected final static Panmage getImage2() {
            return (img2 = Boss.getImage(img2, "aerobot/Projectile2", BotsnBoltsGame.CENTER_8, BotsnBoltsGame.MIN_8, BotsnBoltsGame.MAX_8));
        }
    }
    
    protected final static int HYDRO_OFF_X = 7, HYDRO_H = 25;
    protected final static Panple HYDRO_O = new FinPanple2(14, 1);
    protected final static Panple HYDRO_MIN = getMin(HYDRO_OFF_X);
    protected final static Panple HYDRO_MAX = getMax(HYDRO_OFF_X, HYDRO_H);
    
    protected final static class HydroBot extends Boss {
        protected final static byte STATE_TAUNT = 1;
        protected final static byte STATE_WAIT = 2;
        //protected static Panmage still = null;
        protected final static Panmage[] stills = new Panmage[4];
        protected final static Panmage[] taunts = new Panmage[6];
        
        protected HydroBot(final Segment seg) {
            super(HYDRO_OFF_X, HYDRO_H, seg);
        }
        
        @Override
        protected final void taunt() {
            startStateIndefinite(STATE_TAUNT, getTaunt(0));
        }
        
        @Override
        protected final String[] getIntroMessages() {
            return new String[] {
                    "TODO HYDRO",
                    "NULL",
                    "HYDRO"
                };
        }
        
        @Override
        protected final String[] getRematchMessages() {
            return new String[] { "TODO" };
        }
        
        @Override
        protected final boolean onWaiting() {
            if ((state == STATE_STILL) || (state == STATE_WAIT)) {
                changeView(getStill());
            } else if (state == STATE_TAUNT) {
                if (waitCounter == 8) {
                    setView(getTaunt(1));
                } else if (waitCounter == 12) {
                    setView(getTaunt(2));
                } else if (waitCounter == 20) {
                    setView(getTaunt(3));
                } else if (waitCounter == 24) {
                    setView(getTaunt(4));
                } else if (waitCounter == 40) {
                    setView(getTaunt(5));
                } else if (waitCounter == 44) {
                    finishTaunt();
                    startStateIndefinite(STATE_WAIT, getStill());
                }
            }
            return false;
        }
        
        @Override
        protected final boolean pickState() {
            final int r = rand(3);
            if (r == 0) {
                //startGlide();
            } else if (r == 1) {
                //startAim();
            } else {
                //startDive();
            }
            return false;
        }
        
        @Override
        protected final boolean continueState() {
            /*if (state == STATE_TAUNT) {
            } else*/ {
                startStill();
            }
            return false;
        }
        
        private int stillTimer = -1;
        
        @Override
        protected final Panmage getStill() {
            if (!isTauntFinishedOrWaiting()) {
                return getTaunt(0);
            }
            stillTimer++;
            if (stillTimer >= 48) {
                stillTimer = 0;
            }
            return get(stills, "", stillTimer / 12);
        }
        
        //protected final static Panmage getAimForward() {
        //    return (still = getHydroImage(still, "hydrobot/HydroBot"));
        //}
        
        protected final static Panmage getTaunt(final int i) {
            return get(taunts, "Taunt", i);
        }
        
        protected final static Panmage get(final Panmage[] images, final String name, final int i) {
            Panmage img = images[i];
            if (img == null) {
                img = getHydoImage(null, "hydrobot/HydroBot" + name + (i + 1));
                images[i] = img;
            }
            return img;
        }
        
        protected final static Panmage getHydoImage(final Panmage img, final String name) {
            return getImage(img, name, HYDRO_O, HYDRO_MIN, HYDRO_MAX);
        }
    }
    
    protected final static PlayerContext newPlayerContext(final PlayerImages pi) {
        final Profile prf = new Profile(true);
        prf.autoCharge = true;
        prf.infiniteStamina = true;
        return new PlayerContext(0, prf, pi);
    }
    
    protected abstract static class AiBoss extends Player implements SpecBoss, RoomAddListener {
        protected final static byte DEFEATED_NO = 0;
        protected final static byte DEFEATED_YES = 1;
        protected final static byte DEFEATED_AFTER = 2;
        protected final List<AiHandler> handlers = new ArrayList<AiHandler>();
        protected AiHandler handler = null;
        protected AiHandler nextHandler = null;
        protected byte defeated = DEFEATED_NO;
        protected boolean needMirror = false;
        protected int waitTimer = 0;
        protected int shootTimer = 0;
        protected int extra = 0;
        private float lastV = 0;
        private long lastStreamCollision = NULL_CLOCK;
        
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
            if (!isHealthMeterNeeded()) {
                return;
            }
            initStatic();
            deactivateCharacters();
            BotsnBoltsGame.initEnemyHealthMeter(healthMeter = Enemy.newHealthMeter(this));
        }
        
        protected boolean isHealthMeterNeeded() {
            return true;
        }
        
        @Override
        public void onHealthMaxDisplayReached() {
            activateCharacters();
        }
        
        protected final void dialogue(final Runnable finishHandler, final String... msgs) {
            deactivateCharacters();
            Boss.dialogue(getPortrait(), 0, finishHandler, msgs);
        }
        
        protected final Panmage getPortrait() {
            return pi.portrait;
        }
        
        protected final Runnable newDialogueFinishExitHandler() {
            return new Runnable() {
                @Override public final void run() {
                    activateCharacters();
                    exit();
                }};
        }
        
        protected final void activateCharacters() {
            setPlayerActive(true);
            active = true;
        }
        
        protected final void deactivateCharacters() {
            setPlayerActive(false);
            active = false;
        }
        
        @Override
        public final void onMaterialized() {
            super.onMaterialized();
            final String[] introMsgs = getIntroMessages();
            if (introMsgs == null) {
                finishIntro();
            } else {
                dialogue(new Runnable() { @Override public final void run() {
                    finishIntro();
                }}, introMsgs);
            }
            onBossMaterialized();
        }
        
        protected String[] getIntroMessages() {
            return null;
        }
        
        private final void finishIntro() {
            health = HudMeter.MAX_VALUE;
            finishIntroStatic();
        }
        
        //@OverrideMe
        protected void onBossMaterialized() {
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
        
        @Override
        public int pickResponseToDanger() {
            return -1;
        }
        
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
            if ((isPermanent(this.handler)) || (isPermanent(nextHandler))) {
                return;
            }
            if (this.handler != null) {
                if (!this.handler.finish(this) && ((health > 0))) {
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
        
        protected final void setMirrorTowardPlayer() {
            if (!isFacingPlayer()) {
                setMirror(!isMirror());
            }
        }
        
        protected final boolean isFacingPlayer() {
            final boolean mirrorNeeded = getNearestPlayerX() < getPosition().getX();
            return mirrorNeeded == isMirror();
        }
        
        protected final Player getNearestPlayer() {
            return Enemy.getNearestPlayer(this);
        }
        
        protected final float getNearestPlayerX() {
            return getPlayerX(getNearestPlayer());
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
        protected final Panctor newProjectile(final float vx, final float vy, final int power) {
            return new AiProjectile(this, Projectile.OFF_X, Projectile.OFF_Y, getMirrorMultiplier() * vx, vy, pi, prf.shootMode, power);
        }
        
        @Override
        protected final void newBomb() {
            new AiBomb(this);
        }
        
        protected final void startStill() {
            startHandler(stillHandler);
        }
        
        @Override
        protected final boolean onWall(final byte xResult) {
            super.onWall(xResult);
            needMirror = true;
            startStill();
            return true;
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
        public final void onShot(final SpecPlayerProjectile prj) {
            Enemy.onHurt(this, prj);
        }
        
        @Override
        public final boolean isHarmful() {
            return defeated == DEFEATED_NO;
        }
        
        @Override
        public final boolean onAttack(final Player player) {
            return player.hurt(getDamage());
        }
        
        public int getDamage() {
            return DAMAGE;
        }
        
        @Override
        public final void setHealth(final int health) {
            if ((health < this.health)) {
                handleHurtIfDangerous(this);
                if (isStill()) {
                    final int damage = this.health - health;
                    waitTimer = adjustWaitTimerOnHurt(waitTimer, damage);
                }
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
        public final void onDefeat(final Player player) {
            if (defeated >= DEFEATED_YES) {
                return;
            }
            defeated = DEFEATED_YES;
            destroyEnemies();
            Pangine.getEngine().getAudio().stopMusic();
            startHandler(new DefeatedHandler());
        }
        
        private final void afterDefeat() {
            if (defeated >= DEFEATED_AFTER) {
                return;
            }
            defeated = DEFEATED_AFTER;
            deactivateCharacters();
            final Panple pos = getPosition();
            final int x = Math.round(pos.getX()), y = Math.round(pos.getY());
            Fort.burst(pi.burst, x - 15, x + 15, y, y + 30, 10);
            Player.startDefeatTimer(new Runnable() {
                @Override public final void run() {
                    onAfterDefeat();
                }});
        }
        
        protected void onAfterDefeat() {
            final String[] defeatMsgs = getDefeatMessages();
            if (defeatMsgs == null) {
                if (isExitAfterDefeatNeeded()) {
                    exit();
                }
            } else {
                dialogue(newDialogueFinishExitHandler(), defeatMsgs);
            }
        }
        
        protected String[] getDefeatMessages() {
            return null;
        }
        
        protected boolean isExitAfterDefeatNeeded() {
            return true;
        }
        
        protected final void exit() {
            destroy();
            dematerialize(new Runnable() {
                @Override public final void run() {
                    if (isAwardNeeded()) {
                        Boss.award(new VictoryDisk(BotsnBoltsGame.getActivePlayer(), AiBoss.this), DROP_X);
                    }
                }});
        }
        
        protected boolean isAwardNeeded() {
            return true;
        }
        
        @Override
        public void onAward(final Player player) {
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
        
        @Override
        public final long getLastStreamCollision() {
            return lastStreamCollision;
        }
        
        @Override
        public final void setLastStreamCollision(final long lastStreamCollision) {
            this.lastStreamCollision = lastStreamCollision;
        }
    }
    
    protected final static class Volatile extends AiBoss {
        protected Volatile(final int x, final int y) {
            super(BotsnBoltsGame.volatileImages, x, y);
            handlers.add(new AttackRunHandler());
            handlers.add(new AttackHandler());
            handlers.add(new AttackJumpHandler());
            handlers.add(new JumpsHandler()); // 3 (also response to danger)
            deactivateCharacters();
        }
        
        @Override
        public final int pickResponseToDanger() {
            return 3;
        }
        
        @Override
        protected final String[] getIntroMessages() {
            return new String[] {
                "Hello, Void.  I've been expecting you.",
                "Who are you?",
                "My name is Volatile.  If you want to face Dr. Final, then you'll need to go through me first!"
            };
        }
        
        @Override
        protected final int initStillTimer() {
            return Boss.initStillTimer(30, 45);
        }
        
        @Override
        protected final String[] getDefeatMessages() {
            return new String[] { "You're more powerful than I thought you'd be, but you're too late.  Fighting Dr. Final won't help now.  He has already unleashed his array of robots!  I suggest you stop them before people get hurt." };
        }
    }
    
    protected final static class Volatile2 extends AiBoss {
        protected Volatile2(final int x, final int y) {
            super(BotsnBoltsGame.volatileImages, x, y);
            handlers.add(new SpreadAttackRunHandler());
            handlers.add(new ChargeAttackJumpsHandler()); // 1 (also a response to danger)
            handlers.add(new RapidAttackHandler()); // If he has a turret attack, will be similar to this; then move this to Final
            handlers.add(new RapidAttackJumpHandler()); // 3 (also a response to danger)
            handlers.add(new BombRollHandler());
            deactivateCharacters();
        }
        
        @Override
        public final int pickResponseToDanger() {
            return Mathtil.rand() ? 1 : 3;
        }
        
        @Override
        protected final String[] getIntroMessages() {
            return new String[] {
                "Hello again, Void.",
                "Volatile!  I've grown stronger since we last met.",
                "So have I.  Allow me to demonstrate.  You will never reach Dr. Final.  I will destroy you!"
            };
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
            return Boss.initStillTimer(15, 30);
        }
        
        @Override
        protected final String[] getDefeatMessages() {
            return new String[] { "I'm impressed.  But you still won't be able to defeat Dr. Final.  Go ahead and try.  It will be the end of you!" };
        }
    }
    
    protected final static class VolatileActor extends AiBoss {
        private Runnable materializedDialogueFinishHandler = null;
        private String[] materializedDialogueMessages = null;
        
        protected VolatileActor(final int x, final int y) {
            super(BotsnBoltsGame.volatileImages, x, y);
            handlers.add(new StillHandler());
        }
        
        @Override
        protected final void onBossMaterialized() {
            dialogue(materializedDialogueFinishHandler, materializedDialogueMessages);
        }
        
        @Override
        protected final boolean isHealthMeterNeeded() {
            return false;
        }
        
        @Override
        protected final int initStillTimer() {
            return Integer.MAX_VALUE;
        }
        
        @Override
        protected final boolean isAwardNeeded() {
            return false;
        }
    }
    
    protected final static class Transient extends AiBoss {
        protected final static Warp newWarp(final Transient boss, final float x, final float y) {
            boss.getPosition().set(x, y);
            boss.setMirror(1);
            boss.stateHandler = WALL_GRAB_HANDLER;
            return new Warp(boss);
        }
        
        protected Transient() {
            super(BotsnBoltsGame.transientImages, 12, 7);
            /*
            handlers.add(new JumpsHandler()); // 0 (also response to danger)
            */
            deactivateCharacters();
        }
        
        @Override
        public final int pickResponseToDanger() {
            return 0;
        }
        
        @Override
        protected final String[] getIntroMessages() {
            return new String[] {
                "TODO Transient line",
                "Null line",
                "Transient line"
            };
        }
        
        @Override
        protected final int initStillTimer() {
            return Boss.initStillTimer(30, 45);
        }
        
        @Override
        protected final String[] getDefeatMessages() {
            return new String[] { "TODO" };
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
        
        protected boolean isPermanent() {
            return false;
        }
    }
    
    protected final static boolean isPermanent(final AiHandler handler) {
        return (handler != null) && handler.isPermanent();
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
            if (!boss.isGrounded()) {
                return;
            }
            boolean playerReady = true;
            for (final PlayerContext pc : BotsnBoltsGame.pcs) {
                final Player player = PlayerContext.getPlayer(pc);
                if (!isDestroyed(player) && !player.isGrounded()) {
                    playerReady = false;
                    break;
                }
            }
            if (playerReady) {
                boss.afterDefeat();
            }
        }
        
        @Override
        protected final boolean isPermanent() {
            return true;
        }
    }
    
    private static class RunHandler extends AiHandler {
        @Override
        protected final void onStep(final AiBoss boss) {
            boss.moveX();
            if (!boss.isFacingPlayer() && (Math.abs(boss.getPosition().getX() - boss.getNearestPlayerX()) > 64)) {
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
            if (boss.health <= 0) {
                return true;
            } else if (boss.isFacingPlayer()) {
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
    
    protected static class CyanTitan extends Boss {
        protected final static byte STATE_ATTACK = 1;
        protected final static byte STATE_JUMPS = 2;
        protected final static byte STATE_HAMMER_CHARGE = 3;
        protected final static byte STATE_HAMMER_SMASH = 4;
        protected static Panmage still = null;
        protected static Panmage jump = null;
        
        protected CyanTitan(final Segment seg) {
            super(TITAN_OFF_X, TITAN_H, seg);
        }

        @Override
        protected final boolean pickState() {
            final int r = Mathtil.randi(0, 999);
            final int attackThreshold = isPlayerDangerous() ? scaleByHealthInt(10, 200) : scaleByHealthInt(100, 500);
            if ((r < attackThreshold)) {
                startRandomAttack();
            } else {
                setH(TITAN_JUMP_H);
                startJumps();
            }
            return false;
        }

        @Override
        protected final boolean continueState() {
            setH(TITAN_H);
            return pickNextState();
        }
        
        protected boolean pickNextState() {
            startStill();
            return false;
        }
        
        @Override
        protected boolean onWaiting() {
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
        
        protected void startRandomAttack() {
            startAttack();
        }
        
        protected final void startAttack() {
            startState(STATE_ATTACK, 2, getStill());
        }
        
        @Override
        protected final void startStill() {
            int waitTimer = scaleByHealthInt(2, 30); // randomly 15 - 30 for other Bosses
            if (isPlayerDangerous()) {
                waitTimer = Math.min(waitTimer, initStillTimer());
            }
            startStill(waitTimer);
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
            return getImage();
        }
        
        protected final static Panmage getImage() {
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
    
    protected static class CyanTitan2 extends CyanTitan {
        protected final static int WAIT_HAMMER_SMASH = 30;
        protected final static int HAMMER_TRAIL_SIZE = 10;
        protected final static double HAMMER_TRAIL_MULTIPLIER = (Math.PI / 2.0) / (HAMMER_TRAIL_SIZE - 1.0);
        protected final static double HAMMER_TRAIL_RADIUS = 70;
        
        protected static Panmage wield = null;
        protected static Panmage hammer = null;
        
        protected CyanTitan2(final Segment seg) {
            super(seg);
            getHammer();
        }
        
        @Override
        protected final boolean onWaiting() {
            if (state == STATE_HAMMER_CHARGE) {
                final PlayerImages pi = BotsnBoltsGame.finalImages;
                final Panimation charge, chargeVert;
                if (waitCounter < 30) {
                    charge = pi.charge; chargeVert = pi.chargeVert;
                } else {
                    charge = pi.charge2; chargeVert = pi.chargeVert2;
                }
                Player.charge(this, -40, 121, charge, chargeVert, waitCounter, BotsnBoltsGame.fxCharge);
                Player.charge(this, -38, 153, charge, chargeVert, waitCounter, BotsnBoltsGame.fxCharge);
                Player.charge(this, -12, 128, charge, chargeVert, waitCounter, BotsnBoltsGame.fxCharge);
                Player.charge(this, 15, 104, charge, chargeVert, waitCounter, BotsnBoltsGame.fxCharge);
                Player.charge(this, 17, 136, charge, chargeVert, waitCounter, BotsnBoltsGame.fxCharge);
            } else if (state == STATE_HAMMER_SMASH) {
                addTrailBursts();
            }
            return super.onWaiting();
        }
        
        private final void addTrailBursts() {
            final int n = waitTimer - (WAIT_HAMMER_SMASH - HAMMER_TRAIL_SIZE);
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY();
            final int m = getMirrorMultiplier();
            for (int i = 0; i < n; i++) {
                final double theta = i * HAMMER_TRAIL_MULTIPLIER;
                final double ox = ((Math.cos(theta) * HAMMER_TRAIL_RADIUS) + 96) * m;
                final double oy = (Math.sin(theta) * HAMMER_TRAIL_RADIUS) + 64;
                final float rx = Mathtil.randf(-8.0f, 8.0f);
                final float ry = Mathtil.randf(-8.0f, 8.0f);
                FinalWagon.burst(x + (float) ox + rx, y + (float) oy + ry);
            }
        }
        
        @Override
        protected final boolean pickNextState() {
            if (state == STATE_HAMMER_CHARGE) {
                new TitanHammerImpact(this);
                startState(STATE_HAMMER_SMASH, WAIT_HAMMER_SMASH, getWield());
                return false;
            }
            return super.pickNextState();
        }
        
        @Override
        protected final void startRandomAttack() {
            if (Mathtil.rand()) {
                startAttack();
            } else {
                startHammer();
            }
        }
        
        protected final void startHammer() {
            startState(STATE_HAMMER_CHARGE, 60, getStill());
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            super.renderView(renderer);
            final Panlayer layer = getLayer();
            final Pansplay display = getCurrentDisplay();
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY();
            final boolean mirror = isMirror();
            final int m = getMirrorMultiplier(mirror);
            if (display == still) {
                renderer.render(layer, hammer, x - (m * 46) - (mirror ? 127 : 0), y + 40, BotsnBoltsGame.DEPTH_ENEMY_FRONT, 0, 0, 128, 128, 0, mirror, false);
            } else if (display == jump) {
                renderer.render(layer, hammer, x - (m * 41) - (mirror ? 127 : 0), y + 16, BotsnBoltsGame.DEPTH_ENEMY_FRONT, 0, 0, 128, 128, 0, mirror, false);
            } else if (display == wield) {
                renderer.render(layer, hammer, x + (m * 67) - (mirror ? 127 : 0), y - 69, BotsnBoltsGame.DEPTH_ENEMY_FRONT, 0, 0, 128, 128, 3, mirror, false);
            }
        }
        
        protected final static Panmage getWield() {
            if (wield == null) {
                wield = getImage(null, "cyantitan/CyanTitanWield", new FinPanple2(5, 1),
                        new FinPanple2(TITAN_MIN.getX() + 39, TITAN_MIN.getY()),
                        new FinPanple2(TITAN_MAX.getX() + 39, TITAN_MAX.getY()));
            }
            return wield;
        }
        
        protected final static Panmage getHammer() {
            if (hammer == null) {
                hammer = getImage(null, "cyantitan/CyanTitanHammer", FinPanple.ORIGIN, FinPanple.ORIGIN, new FinPanple2(60, 96));
            }
            return hammer;
        }
    }
    
    protected final static class TitanHammerImpact extends EnemyProjectile {
        private final static float R = 72.0f;
        private final static float H = 36.0f;
        private static Panmage box = null;
        private int timer = 0;
        
        protected TitanHammerImpact(final Panctor src) {
            super(src, 150, 0, 0, 0);
            if (box == null) {
                box = Pangine.getEngine().createEmptyImage("titan.hammer.impact", null, new FinPanple2(-R, 0), new FinPanple2(R, H));
            }
            setView(box);
        }
        
        @Override
        protected final int getDamage() {
            return 8;
        }
        
        @Override
        protected final void burst(final Player player) {
        }
        
        @Override
        protected final boolean isDestroyedOnImpact() {
            return false;
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            super.onStep(event);
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY();
            if (timer >= 8) {
                destroy();
                return;
            }
            for (int i = timer; i < 8; i++) {
                for (int m = -1; m <= 1; m += 2) {
                    final float ry = Mathtil.randf(0.0f, H);
                    final float rx = Mathtil.randf(-4.0f, 4.0f) + (R * (i + 1.0f) / 8.0f);
                    FinalWagon.burst(x + (m * rx), y + ry);
                }
            }
            timer++;
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
            BotsnBoltsGame.runPlayers(new PlayerRunnable() {
                @Override
                public final void run(final Player player) {
                    player.setMirror(false);
                }
            });
            addActor(saucer);
        }
        
        @Override
        protected final String[] getIntroMessages() {
            return new String[] {
                "You must feel very confident after defeating my Array of underlings, but I won't be stopped so easily.  You think that you've reached the end of your mission?  You fool!  You've only reached your own destruction.",
                "I am not afraid.",
                "You will be nothing but a pile of bolts and batteries when I'm done with you.",
                "Don't keep me waiting.",
                "I'll disconnect your drives.  I'll terminate your processes.  I'll crash your system.  I'll ruin you!"
            };
        }
        
        @Override
        protected final Panmage getPortrait() {
            return BotsnBoltsGame.finalImages.portrait;
        }
        
        private final boolean isRetreatNeeded() {
            return advanceIndex > 0;
        }
        
        @Override
        protected final boolean pickState() {
            if (isRetreatNeeded()) {
                startRetreat();
            } else if (health <= 0) {
                separate();
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
        public final void onShot(final SpecPlayerProjectile prj) {
            if (state == STATE_DEFEATED) {
                if (prj.getVelocityX() == 0f) {
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
        protected final boolean isVulnerableToProjectile(final SpecPlayerProjectile prj) {
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
            }
            setPlayerActive(false);
            if (isRetreatNeeded()) {
                startRetreat();
            } else {
                separate();
            }
        }
        
        private final void separate() {
            if (saucer != null) {
                saucer.separate();
            }
            saucer = null;
            startDefeated();
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
        
        protected final static void burst(final float x, final float y) {
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
            return getNearestPlayerX();
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
            super(0, 0, 3);
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
            BotsnBoltsGame.fxEnemyAttack.startSound();
        }
        
        @Override
        protected final void onStepEnemy() {
            timer++;
            if (timer > 10) {
                Player.puff(this, -1, 4);
                timer = 0;
            }
            final Player player = getNearestPlayer();
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
        public boolean onAttack(final Player player) {
            final boolean ret = super.onAttack(player);
            EnemyProjectile.burstEnemy(this, 4);
            destroy();
            return ret;
        }
        
        @Override
        protected final int getDamage() {
            return Projectile.POWER_MEDIUM;
        }
        
        @Override
        protected final void award(final PowerUp powerUp) {
        }
        
        private final static Panmage getRocket() {
            return (img = Boss.getImage(img, "final/Rocket", null, null, null));
        }
    }
    
    private final static class WagonMortar extends AiProjectile {
        private static Panimation anim = null;
        private int dropX;
        
        private WagonMortar(final Panctor src, final int dropX) {
            super(src, 111, 83, 0, Player.VEL_PROJECTILE, BotsnBoltsGame.finalImages, Player.SHOOT_CHARGE, Projectile.POWER_MEDIUM);
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
        
        protected final static Panimation getAnim() {
            if (anim != null) {
                return anim;
            }
            final Pangine engine = Pangine.getEngine();
            final Panimation base = BotsnBoltsGame.finalImages.projectile2;
            final Panframe frames[] = base.getFrames(), frame1 = frames[1];
            final Panframe flip = engine.createFrame(frame1.getId() + ".rot", frame1.getImage(), frame1.getDuration(), 0, true, false);
            anim = engine.createAnimation(base.getId() + ".rot", frames[0], flip);
            return anim;
        }
    }
    
    protected final static FinPanple2 HEAD_HAND_MIN = new FinPanple2(-16, -16);
    protected final static FinPanple2 HEAD_HAND_MAX = new FinPanple2(16, 16);
    
    protected final static class FinalHead extends Boss {
        private final static byte STATE_OPEN_DOORS = 1;
        private final static byte STATE_MOVE_ARMS_TO_INITIAL_POSITIONS = 2;
        private final static byte STATE_OPEN_MOUTH = 3;
        private final static byte STATE_SPAWN = 4;
        private final static byte STATE_CLOSE_MOUTH = 5;
        private final static byte STATE_CHARGE = 6;
        private final static byte STATE_ENERGY_BALL = 7;
        private final static byte STATE_RETRACT_BEFORE_BASIC_PROJECTILE = 8;
        private final static byte STATE_BASIC_PROJECTILE = 9;
        private final static byte STATE_RETRACT_BEFORE_REACH = 10;
        private final static byte STATE_REACH_FOR_PLAYER = 11;
        private final static byte STATE_DELAY_BEFORE_RETRACT_HANDS = 12;
        private final static byte STATE_RETRACT_HANDS = 13;
        private final static byte STATE_RETRACT_BEFORE_SWIPE = 14;
        private final static byte STATE_SWIPE = 15;
        
        protected final static int BASE_Y = 96;
        
        private static Panmage still = null;
        private static Panmage corner = null;
        private static Panmage shoulder = null;
        private static Panmage[] mouths = new Panmage[3];
        private static Panmage eyeChargingLeft = null;
        private static Panmage eyeChargingRight = null;
        private static Panmage eyeCharged = null;
        private static Panmage energyBall = null;
        private static Panmage arm = null;
        private static Panmage hand = null;
        private static Panmage claw = null;
        private static Panmage clawRip = null;
        private static Panmage wall = null;
        private static Panmage shatter = null;
        
        protected int visibleSize = 0;
        private boolean facingMirror = true;
        private Panmage currentMouth = null;
        private Panmage currentEyeLeft = null;
        private Panmage currentEyeRight = null;
        private boolean currentEyeRightMirror = false;
        private int currentEyeRightOffset = 0;
        private HeadHand handLeft = null;
        private HeadHand handRight = null;
        private int maxFollowerDepth = Integer.MAX_VALUE;

        protected FinalHead(final Segment seg) {
            super(56, 96, 0, 0);
            setMirror(false);
            getPosition().set(128, BASE_Y, BotsnBoltsGame.DEPTH_ENEMY_BG);
        }
        
        @Override
        protected final boolean isHealthMeterInitiallyHidden() {
            return true;
        }
        
        @Override
        protected final boolean isDropNeeded() {
            return false;
        }
        
        @Override
        protected final void taunt() {
            super.taunt();
            startStateIndefinite(STATE_OPEN_DOORS, getStill());
        }
        
        protected final void startMovingArmsToInitialPositions() {
            startStateIndefinite(STATE_MOVE_ARMS_TO_INITIAL_POSITIONS, getStill());
        }
        
        protected final void finishOpen() {
            for (int y = 0; y < BotsnBoltsGame.GAME_ROWS; y++) {
                ShootableDoor.disableOverlay(0, y);
                ShootableDoor.disableOverlay(23, y);
            }
            Pangine.getEngine().addTimer(this, 30, new TimerListener() {
                @Override public final void onTimer(final TimerEvent event) {
                    finishTaunt();
                    startStill();
                    healthMeter.setVisible(true);
                }});
        }
        
        @Override
        protected final void onTauntFinished() {
            startRoam();
        }
        
        @Override
        protected final boolean isDefeatOrbNeeded() {
            return false;
        }
        
        @Override
        public final void onAward(final Player player) {
            clearPlayerExtras();
            /*player.startScript(new LeftAi(32.0f), new Runnable() {
                @Override public final void run() {
                    //new Warp(getNextBoss(20, 3));
                }});*/
        }
        
        @Override
        protected final boolean getTauntingReturnValue() {
            return true;
        }
        
        @Override
        protected final boolean onWaiting() {
            facingMirror = getNearestPlayerX() < MID_X;
            if (visibleSize == 6) {
                final float handY = 36 + getPosition().getY();
                handLeft = new HeadHand(this, -8, handY, BotsnBoltsGame.DEPTH_ENEMY_BG, false);
                handRight = new HeadHand(this, 391, handY, BotsnBoltsGame.DEPTH_ENEMY_BG, true);
            } else if (visibleSize == 7) {
                final float armY = 52 + getPosition().getY();
                handLeft.setPrevious(new HeadArm(handLeft, 8, armY, BotsnBoltsGame.DEPTH_ENEMY_BG_2, false));
                handRight.setPrevious(new HeadArm(handRight, 375, armY, BotsnBoltsGame.DEPTH_ENEMY_BG_2, true));
            }
            if (state == STATE_MOVE_ARMS_TO_INITIAL_POSITIONS) {
                final Panple handLeftPos = handLeft.getPosition(), handRightPos = handRight.getPosition();
                final float handLeftX = handLeftPos.getX() + 2;
                handLeft.previous.getPosition().addX(1);
                handRight.previous.getPosition().addX(-1);
                if (handLeftX >= 56) {
                    handLeftPos.setX(56);
                    handLeftPos.setZ(BotsnBoltsGame.DEPTH_ENEMY_FRONT_4);
                    handRightPos.setX(327);
                    handRightPos.setZ(BotsnBoltsGame.DEPTH_ENEMY_FRONT_4);
                    handLeft.init();
                    handRight.init();
                    state = STATE_OPEN_DOORS;
                    finishOpen();
                } else {
                    handLeftPos.setX(handLeftX);
                    handRightPos.addX(-2);
                }
            } else if ((state == STATE_REACH_FOR_PLAYER) || (state == STATE_SWIPE)) {
                if (!isAdvancing()) {
                    startDelayBeforeRetractHands();
                }
            } else if (state == STATE_RETRACT_HANDS) {
                if (isRetractFinished()) {
                    if (maxFollowerDepth == 1) {
                        startReachForPlayer(2);
                    } else if (maxFollowerDepth == 2) {
                        startReachForPlayer(Integer.MAX_VALUE);
                    } else {
                        startRoam();
                    }
                }
            } else if (state == STATE_RETRACT_BEFORE_REACH) {
                if (isRetractFinished()) {
                    startReachForPlayer(1);
                }
            } else if (state == STATE_RETRACT_BEFORE_SWIPE) {
                if (isRetractFinished()) {
                    startSwipe();
                }
            } else if (state == STATE_RETRACT_BEFORE_BASIC_PROJECTILE) {
                if (isRetractFinished()) {
                    startState(STATE_BASIC_PROJECTILE, 135, getStill());
                }
            } else if (state == STATE_OPEN_MOUTH) {
                if (waitCounter == 1) {
                    currentMouth = getMouth(0);
                } else if (waitCounter == 11) {
                    currentMouth = getMouth(1);
                } else if (waitCounter == 21) {
                    currentMouth = getMouth(2);
                }
            } else if (state == STATE_CLOSE_MOUTH) {
                if (waitCounter == 1) {
                    currentMouth = getMouth(1);
                } else if (waitCounter == 11) {
                    currentMouth = getMouth(0);
                } else if (waitCounter == 21) {
                    currentMouth = null;
                }
            } else if ((state == STATE_SPAWN) && ((waitCounter % 20) == 1)) {
                final BounceEnemy bounceEnemy = new BounceEnemy(getMirrorMultiplier(facingMirror) * Mathtil.randi(1, 4), Mathtil.randi(2, 7));
                bounceEnemy.getPosition().set(191, 15 + getPosition().getY(), BotsnBoltsGame.DEPTH_ENEMY_FRONT_4);
                addActor(bounceEnemy);
            } else if (state == STATE_CHARGE) {
                final PlayerImages pi = BotsnBoltsGame.finalImages;
                Player.charge(this, 46, 39, pi.charge, pi.chargeVert, waitCounter, BotsnBoltsGame.fxCharge);
                Player.charge(this, 82, 39, pi.charge, pi.chargeVert, waitCounter, BotsnBoltsGame.fxCharge);
                if (waitCounter == 1) {
                    getEyeChargingLeft();
                } else if (waitCounter == 2) {
                    currentEyeLeft = getEyeChargingLeft();
                    currentEyeRight = getEyeChargingRight();
                    currentEyeRightMirror = false;
                    currentEyeRightOffset = 69;
                } else if (waitCounter == 5) {
                    currentEyeLeft = currentEyeRight = getEyeCharged();
                    currentEyeRightMirror = true;
                    currentEyeRightOffset = 94;
                }
            } else if ((state == STATE_ENERGY_BALL) && (waitCounter == 2)) {
                BotsnBoltsGame.fxEnemyAttack.startSound();
                final int energyBallHv = HeadEnergyBall.ENERGY_BALL_SPEED * getMirrorMultiplier(facingMirror);
                final float energyBallY = 36 + getPosition().getY();
                new HeadEnergyBall(174, energyBallY, energyBallHv);
                new HeadEnergyBall(210, energyBallY, energyBallHv);
                currentEyeLeft = null;
                currentEyeRight = null;
            } else if (state == STATE_BASIC_PROJECTILE) {
                final int chargeTime = waitCounter % 45;
                if (chargeTime > 15 && chargeTime < 43) {
                    handLeft.charge(waitCounter);
                    handRight.charge(waitCounter);
                } else if (chargeTime == 43) {
                    handLeft.emitBasicProjectiles();
                    handRight.emitBasicProjectiles();
                }
            }
            return true;
        }

        @Override
        protected final boolean pickState() {
            final int r = randForceDifferent(isEitherArmRemaining() ? 3 : 2);
            if (r == 0) {
                startState(STATE_OPEN_MOUTH, 30, getStill());
            } else if (r == 1) {
                startState(STATE_CHARGE, 60, getStill());
            } else if (r == 2) {
                if (Mathtil.rand()) {
                    startRetractHands(STATE_RETRACT_BEFORE_BASIC_PROJECTILE);
                } else {
                    if (Mathtil.rand()) {
                        startRetractHands(STATE_RETRACT_BEFORE_REACH);
                    } else {
                        startRetractHands(STATE_RETRACT_BEFORE_SWIPE);
                    }
                }
            }
            return true;
        }
        
        protected final boolean isAdvancing() {
            return handLeft.isAdvancing() || handRight.isAdvancing();
        }
        
        protected final boolean isRetractFinished() {
            return handLeft.isRetracted() && handRight.isRetracted();
        }
        
        protected final boolean isEitherArmRemaining() {
            return !(Panctor.isDestroyed(handLeft) && Panctor.isDestroyed(handRight));
        }
        
        protected final void startReachForPlayer(final int maxFollowerDepth) {
            this.maxFollowerDepth = maxFollowerDepth;
            startStateIndefinite(STATE_REACH_FOR_PLAYER, getStill());
            final float targetX = getNearestPlayerX(), targetY = getNearestPlayerY();
            handLeft.startAdvance(targetX - 22, targetY, HeadHand.SPEED_REACH);
            handRight.startAdvance(targetX + 22, targetY, HeadHand.SPEED_REACH);
        }
        
        protected final void startSwipe() {
            this.maxFollowerDepth = Integer.MAX_VALUE;
            startStateIndefinite(STATE_SWIPE, getStill());
            final float y = getPosition().getY();
            final HeadHand handUpper, handLower;
            final int m;
            if (Mathtil.rand()) {
                handUpper = handLeft;
                handLower = handRight;
                m = -1;
            } else {
                handUpper = handRight;
                handLower = handLeft;
                m = 1;
            }
            handLower.startAdvance(MID_X - (m * 144), y - 60, HeadHand.SPEED_SWIPE); handLower.nextTarget = new FinPanple2(MID_X + (m * 144), y - 60);
            handUpper.startAdvance(MID_X + (m * 64), y - 20, HeadHand.SPEED_SWIPE); handUpper.nextTarget = new FinPanple2(MID_X - (m * 64), y - 20);
        }
        
        protected final void startDelayBeforeRetractHands() {
            startState(STATE_DELAY_BEFORE_RETRACT_HANDS, 30, getStill());
        }
        
        protected final void startRetractHands(final byte state) {
            startStateIndefinite(state, getStill());
            handLeft.startRetractWholeArm();
            handRight.startRetractWholeArm();
        }

        @Override
        protected final boolean continueState() {
            if (state == STATE_OPEN_MOUTH) {
                startState(STATE_SPAWN, 60, getStill());
            } else if (state == STATE_SPAWN) {
                startState(STATE_CLOSE_MOUTH, 30, getStill());
            } else if (state == STATE_CHARGE) {
                startState(STATE_ENERGY_BALL, 60, getStill());
            } else if (state == STATE_DELAY_BEFORE_RETRACT_HANDS) {
                startRetractHands(STATE_RETRACT_HANDS);
            } else {
                startRoam();
            }
            return true;
        }
        
        protected final void startRoam() {
            maxFollowerDepth = Integer.MAX_VALUE;
            handLeft.state = HeadArm.STATE_ROAM;
            handRight.state = HeadArm.STATE_ROAM;
            startStill();
        }

        @Override
        protected final Panmage getStill() {
            if (still != null) {
                return still;
            }
            return (still = getImage(still, "final/Head", null, new FinPanple2(24, 8), new FinPanple2(104, 88)));
        }
        
        protected final static Panmage getCorner() {
            return (corner = getImage(corner, "final/HeadCorner", null, null, null));
        }
        
        protected final static Panmage getShoulder() {
            return (shoulder = getImage(shoulder, "final/HeadShoulder", null, null, null));
        }
        
        protected final static Panmage getMouth(final int i) {
            Panmage img = mouths[i];
            if (img == null) {
                img = getImage(img, "final/HeadMouth" + (i + 1), null, null, null);
                mouths[i] = img;
            }
            return img;
        }
        
        protected final static Panmage getEyeChargingLeft() {
            return (eyeChargingLeft = getImage(eyeChargingLeft, "final/HeadEyeChargingLeft", null, null, null));
        }
        
        protected final static Panmage getEyeChargingRight() {
            return (eyeChargingRight = getImage(eyeChargingRight, "final/HeadEyeChargingRight", null, null, null));
        }
        
        protected final static Panmage getEyeCharged() {
            return (eyeCharged = getImage(eyeCharged, "final/HeadEyeCharged", null, null, null));
        }
        
        protected final static Panmage getEnergyBall() {
            return (energyBall = getImage(energyBall, "final/HeadEnergyBall", BotsnBoltsGame.CENTER_16, BotsnBoltsGame.MIN_16, BotsnBoltsGame.MAX_16));
        }
        
        protected final static Panmage getArm() {
            return (arm = getImage(arm, "final/HeadArm", BotsnBoltsGame.CENTER_32, BotsnBoltsGame.MIN_32, BotsnBoltsGame.MAX_32));
        }
        
        protected final static Panmage getHand() {
            return (hand = getImage(hand, "final/HeadHand", BotsnBoltsGame.CENTER_32, HEAD_HAND_MIN, HEAD_HAND_MAX));
        }
        
        protected final static Panmage getClaw() {
            return (claw = getImage(claw, "final/HeadClaw", null, null, null));
        }
        
        protected final static Panmage getClawRip() {
            return (clawRip = getImage(clawRip, "final/HeadClawRip", null, null, null));
        }
        
        protected final static Panmage getWall() {
            return (wall = getImage(wall, "final/HeadWall", null, null, null));
        }
        
        protected final static Panmage getShatter() {
            return (shatter = getImage(shatter, "final/HeadShatter", BotsnBoltsGame.CENTER_8, null, null));
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            if (visibleSize <= 0) {
                return;
            }
            super.renderView(renderer);
            final Panlayer layer = getLayer();
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY();
            renderer.render(layer, Final.getCoat(), x + (facingMirror ? 63 : 65), y + 51, BotsnBoltsGame.DEPTH_ENEMY_BG_2, 0, facingMirror, false);
            renderer.render(layer, BotsnBoltsGame.grey64, x + 32, y + 48, BotsnBoltsGame.DEPTH_ENEMY_BG_3, 0, 0, 64, 40, 0, false, false);
            if (currentMouth != null) {
                renderer.render(layer, currentMouth, x + 56, y + 7, BotsnBoltsGame.DEPTH_ENEMY_BACK_3, 0, false, false);
            }
            if (currentEyeLeft != null) {
                renderer.render(layer, currentEyeLeft, x + 33, y + 29, BotsnBoltsGame.DEPTH_ENEMY_BACK_3, 0, false, false);
                renderer.render(layer, currentEyeRight, x + currentEyeRightOffset, y + 29, BotsnBoltsGame.DEPTH_ENEMY_BACK_3, 0, currentEyeRightMirror, false);
            }
            if (visibleSize <= 1) {
                return;
            }
            getCorner();
            renderer.render(layer, corner, x - 16, y + 96, BotsnBoltsGame.DEPTH_ENEMY_BG_3, 0, 0, 16, 16, 0, false, false);
            renderer.render(layer, corner, x + 128, y + 96, BotsnBoltsGame.DEPTH_ENEMY_BG_3, 0, 0, 16, 16, 0, true, false);
            if (visibleSize <= 3) {
                return;
            }
            renderer.render(layer, corner, x - 48, y + 96, BotsnBoltsGame.DEPTH_ENEMY_BG_3, 0, 0, 16, 16, 0, true, false);
            renderer.render(layer, corner, x + 160, y + 96, BotsnBoltsGame.DEPTH_ENEMY_BG_3, 0, 0, 16, 16, 0, false, false);
            if (visibleSize <= 4) {
                return;
            }
            getShoulder();
            renderer.render(layer, shoulder, x - 112, y + 48, BotsnBoltsGame.DEPTH_ENEMY_BG_3, 0, 0, 64, 64, 0, false, false);
            renderer.render(layer, shoulder, x + 176, y + 48, BotsnBoltsGame.DEPTH_ENEMY_BG_3, 0, 0, 64, 64, 0, true, false);
        }
    }
    
    protected final static class HeadEnergyBall extends Enemy {
        protected final static int ENERGY_BALL_SPEED = 3;
        
        private int bounces = 0;
        private int timer = 0;
        
        protected HeadEnergyBall(final float x, final float y, final int hv) {
            super(7, 15, 0, 0, 1);
            setView(FinalHead.getEnergyBall());
            getPosition().set(x, y, BotsnBoltsGame.DEPTH_PROJECTILE);
            this.hv = hv;
            v = -ENERGY_BALL_SPEED;
            addActor(this);
        }
        
        @Override
        protected final boolean onStepCustom() {
            timer++;
            if (timer > 3) {
                setRot(Mathtil.randi(0, 3));
                setMirror(Mathtil.rand());
                setFlip(Mathtil.rand());
                timer = 0;
            }
            if (addX(hv) != X_NORMAL) {
                hv *= -1;
                bounces++;
            }
            if (addY(v) != Y_NORMAL) {
                v *= -1;
                bounces++;
            }
            if (bounces >= 5) {
                Projectile.burst(this, BotsnBoltsGame.finalImages.burst, getPosition());
                destroy();
            }
            return true;
        }
        
        @Override
        protected final void onLanded() {
            // Skip parent logic of clearing v
        }
        
        @Override
        protected final int getDamage() {
            return Projectile.POWER_MEDIUM;
        }
        
        @Override
        protected final void award(final PowerUp powerUp) {
        }
    }
    
    protected static class HeadArm extends Enemy {
        protected final static byte STATE_NONE = 0;
        protected final static byte STATE_ADVANCE = 1;
        protected final static byte STATE_RETRACT = 2;
        protected final static byte STATE_ROAM = 3;
        protected final static float SPEED_REACH = 4.0f;
        protected final static float SPEED_SWIPE = 2.0f;
        protected final static float FOLLOW_THRESHOLD = 28.0f;
        
        protected byte state = STATE_NONE;
        protected final ImplPanple target = new ImplPanple();
        protected Panple nextTarget = null;
        protected float speed = SPEED_REACH;
        protected float vx, vy;
        
        protected final HeadHand hand;
        protected HeadArm previous = null;
        protected HeadArm next = null;
        
        protected HeadArm(final HeadHand hand, final float x, final float y, final int z, final boolean mirror) {
            this(hand, x, y, z, mirror, FinalHead.getArm());
        }
        
        protected HeadArm(final HeadHand hand, final float x, final float y, final int z, final boolean mirror, final Panmage image) {
            super(15, 31, 0, 0, HudMeter.MAX_VALUE);
            this.hand = (hand == null) ? ((HeadHand) this) : hand;
            setView(image);
            getPosition().set(x, y, z);
            setMirror(mirror);
            addActor(this);
        }
        
        @Override
        protected final void onHurt(final SpecPlayerProjectile prj) {
            if (this == hand) {
                super.onHurt(prj);
            } else {
                hand.onHurt(prj);
            }
        }
        
        @Override
        protected int getDamage() {
            return DAMAGE;
        }
        
        @Override
        protected final boolean onStepCustom() {
            if (state == STATE_NONE) {
                return true;
            } else if (state == STATE_ROAM) {
                roam();
                return true;
            }
            final Panple pos = getPosition();
            if (pos.getDistance2(target) < (speed + 0.2f)) {
                pos.set2(target);
                adjustFollowers();
                vx = vy = 0;
                if ((state == STATE_RETRACT) && (next != null)) {
                    next.startRetractComponent();
                }
                finishMove();
                return true;
            }
            pos.add(vx, vy);
            if (!adjustFollowers()) {
                finishMove();
            }
            return true;
        }
        
        protected void finishMove() {
            if ((state == STATE_ADVANCE) && (nextTarget != null)) {
                startMove(STATE_ADVANCE, nextTarget.getX(), nextTarget.getY());
                nextTarget = null;
                return;
            }
            state = STATE_NONE;
        }
        
        //@OverrideMe
        protected void roam() {
        }
        
        protected final boolean adjustFollowers() {
            if (state == STATE_ADVANCE) {
                return adjustPrevious(FOLLOW_THRESHOLD, speed, 0);
            } else if (state == STATE_RETRACT) {
                adjustNext();
                return true;
            }
            return true;
        }
        
        protected final boolean adjustPrevious(final float followThreshold, final float speed, final int depth) {
            if ((previous == null) || (depth >= hand.head.maxFollowerDepth)) {
                return getPosition().getDistance2(hand.shoulderX, hand.shoulderY) < (followThreshold - speed);
            } else if (previous.follow(this, followThreshold)) {
                return previous.adjustPrevious(followThreshold, speed, depth + 1);
            }
            return true;
        }
        
        protected final void adjustNext() {
            if (next == null) {
                return;
            } else if (next.follow(this, FOLLOW_THRESHOLD)) {
                next.adjustNext();
            }
        }
        
        protected final boolean follow(final HeadArm leader, final float followThreshold) {
            final Panple pos = getPosition(), leaderPos = leader.getPosition();
            Panple.subtract(scratchPanple, leaderPos, pos);
            final float distance = (float) scratchPanple.getMagnitude2();
            if (distance <= followThreshold) {
                return false;
            }
            scratchPanple.multiply(followThreshold / distance);
            pos.set(leaderPos.getX() - scratchPanple.getX(), leaderPos.getY() - scratchPanple.getY());
            return true;
        }
        
        protected final void startRetractComponent() {
            startMove(STATE_RETRACT, hand.shoulderX, hand.shoulderY);
        }
        
        protected final void startMove(final byte state, final float targetX, final float targetY) {
            this.state = state;
            target.set(targetX, targetY);
            Panple.subtract(scratchPanple, target, getPosition());
            final float distance = (float) scratchPanple.getMagnitude2();
            if (distance < 0) {
                return; // The move process will see that the target has been reached
            }
            scratchPanple.multiply(speed / distance);
            vx = scratchPanple.getX();
            vy = scratchPanple.getY();
        }
        
        protected final boolean isRetracted() {
            if (isDestroyed()) {
                return true;
            } else if (state != STATE_NONE) {
                return false;
            }
            final Panple pos = getPosition();
            return ((pos.getX() == hand.shoulderX) && (pos.getY() == hand.shoulderY));
        }
        
        protected final boolean isAdvancing() {
            return (state == STATE_ADVANCE) && !isDestroyed();
        }
        
        protected final boolean isRetracting() {
            HeadArm arm = hand;
            while (arm != null) {
                if (arm.state == STATE_RETRACT) {
                    return true;
                }
                arm = arm.previous;
            }
            return false;
        }
        
        protected final HeadArm getShoulder() {
            HeadArm shoulder = this;
            while (shoulder.previous != null) {
                shoulder = shoulder.previous;
            }
            return shoulder;
        }
        
        protected final void setPrevious(final HeadArm previous) {
            this.previous = previous;
            previous.next = this;
        }
    }
    
    protected final static class HeadHand extends HeadArm {
        private final static float ROAM_SPEED = 1.0f;
        private final FinalHead head;
        private float startX;
        private float startY;
        private float roamX;
        private float roamY;
        private float shoulderX = 0;
        private float shoulderY = 0;
        private boolean attacked = false;
        
        protected HeadHand(final FinalHead head, final float x, final float y, final int z, final boolean mirror) {
            super(null, x, y, z, mirror, FinalHead.getHand());
            this.head = head;
        }
        
        protected final void init() {
            final boolean mirror = isMirror();
            final Panple pos = getPosition();
            startX = pos.getX();
            startY = pos.getY();
            shoulderX = startX - (24 * getMirrorMultiplier(mirror));
            shoulderY = startY + 28;
            roamX = startX + Mathtil.randi(6, 16);
            roamY = startY + Mathtil.randi(6, 16);
            final int[] zs = { BotsnBoltsGame.DEPTH_ENEMY_FRONT_2, BotsnBoltsGame.DEPTH_ENEMY_FRONT, BotsnBoltsGame.DEPTH_ENEMY,
                    BotsnBoltsGame.DEPTH_ENEMY_BACK, BotsnBoltsGame.DEPTH_ENEMY_BACK_2, BotsnBoltsGame.DEPTH_ENEMY_BACK_3, BotsnBoltsGame.DEPTH_CARRIER,
                    BotsnBoltsGame.DEPTH_ENEMY_BG, BotsnBoltsGame.DEPTH_ENEMY_BG_2 }; // ENEMY_BG_3 used by shoulder
            previous.getPosition().setZ(BotsnBoltsGame.DEPTH_ENEMY_FRONT_3);
            HeadArm curr = previous;
            for (final int z : zs) {
                curr.setPrevious(new HeadArm(this, shoulderX, shoulderY, z, mirror));
                curr = curr.previous;
            }
        }
        
        @Override
        public boolean onAttack(final Player player) {
            if (!super.onAttack(player)) {
                return false;
            } else if ((state == STATE_ADVANCE) || ((state == STATE_NONE) && !isRetracting())) {
                attacked = true;
            }
            return true;
        }
        
        @Override
        protected final void roam() {
            final Panple pos = getPosition();
            float x = pos.getX(), y = pos.getY();
            if (roamX < x) {
                x -= ROAM_SPEED;
                if (x <= roamX) {
                    roamX = startX + Mathtil.randi(8, isMirror() ? 20 : 56);
                }
            } else {
                x += ROAM_SPEED;
                if (x >= roamX) {
                    roamX = startX - Mathtil.randi(8, isMirror() ? 56 : 20);
                }
            }
            if (roamY < y) {
                y -= ROAM_SPEED;
                if (y <= roamY) {
                    roamY = startY + Mathtil.randi(8, 56);
                }
            } else {
                y += ROAM_SPEED;
                if (y >= roamY) {
                    roamY = startY - Mathtil.randi(8, 20);
                }
            }
            pos.set(x, y);
            adjustPrevious(21.0f, ROAM_SPEED, 0);
        }
        
        protected final void charge(final int waitCounter) {
            final PlayerImages pi = BotsnBoltsGame.finalImages;
            Player.charge(this, 7, 7, pi.charge, pi.chargeVert, waitCounter, BotsnBoltsGame.fxCharge);
        }
        
        protected final void emitBasicProjectiles() {
            BotsnBoltsGame.fxChargedAttack.startSound();
            emitBasicProjectile(-VEL_PROJECTILE_45, -VEL_PROJECTILE_45);
            emitBasicProjectile(0, -VEL_PROJECTILE);
            emitBasicProjectile(VEL_PROJECTILE_45, -VEL_PROJECTILE_45);
        }
        
        protected final void emitBasicProjectile(final float vx, final float vy) {
            new HeadBasicProjectile(this, vx, vy);
        }
        
        protected final void startAdvance(final float targetX, final float targetY, final float speed) {
            this.speed = speed;
            startMove(STATE_ADVANCE, targetX, targetY);
        }
        
        protected final void startRetractWholeArm() {
            if (state == STATE_ROAM) {
                state = STATE_NONE;
            }
            attacked = false;
            speed = SPEED_REACH;
            getShoulder().startRetractComponent();
        }
        
        @Override
        public final void onDefeat(final Player player) {
            HeadArm arm = this;
            final Panimation burst = BotsnBoltsGame.finalImages.burst;
            while (arm != null) {
                Projectile.burst(arm, burst, arm.getPosition());
                arm.destroy();
                arm = arm.previous;
            }
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            super.renderView(renderer);
            final Panlayer layer = getLayer();
            final Panmage claw = FinalHead.getClaw();
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY(), z = pos.getZ();
            final boolean mirror = isMirror();
            final int m = getMirrorMultiplier(mirror), moff = mirror ? -15 : 0;
            if (attacked) {
                renderer.render(layer, claw, x + (m * -15) + moff, y - 12, z + 2, 0, 0, 16, 16, 0, !mirror, true);
                renderer.render(layer, claw, x + (m * -12) + moff, y - 15, z + 2, 0, 0, 16, 16, 1, mirror, true);
                renderer.render(layer, claw, x + (m * -2) + moff, y, z + 2, 0, 0, 16, 16, 0, mirror, false);
                return;
            }
            renderer.render(layer, claw, x + (m * -26) + moff, y - 11, z + 2, 0, 0, 16, 16, 0, mirror, false);
            renderer.render(layer, claw, x + (m * -11) + moff, y - 26, z + 2, 0, 0, 16, 16, 1, !mirror, false);
            renderer.render(layer, claw, x + (m * 8) + moff, y - 5, z - 2, 0, 0, 16, 16, 0, !mirror, true);
        }
    }
    
    protected final static class HeadBasicProjectile extends EnemyProjectile {
        protected HeadBasicProjectile(final Panctor src, final float vx, final float vy) {
            super(null, src, 7, 7, vx, vy);
            setView(WagonMortar.getAnim());
            setRot(3);
        }
        
        @Override
        protected final int getDamage() {
            return Projectile.POWER_MEDIUM;
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
        
        @Override
        protected boolean onWaiting() {
            return true;
        }
        
        @Override
        protected boolean pickState() {
            return true;
        }

        @Override
        protected boolean continueState() {
            return true;
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
        protected final void onHurt(final SpecPlayerProjectile prj) {
            wagon.onHurt(prj);
        }
        
        @Override
        protected final Panmage getStill() {
            return getSaucer();
        }
    }
    
    protected final static class FinalSaucer extends BaseSaucer implements RoomChangeListener {
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
        protected final boolean isTauntNeeded() {
            return false;
        }
        
        @Override
        protected final boolean getTauntingReturnValue() {
            return true;
        }
        
        @Override
        protected final boolean isPlayerActivationAfterHealthMaxNeeded() {
            return false;
        }
        
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
                hv = (getNearestPlayerX() < x) ? -1 : 1;
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
            if (((rx % 16) == 15) && (Math.abs(x - getNearestPlayerX()) < 16)) {
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
            startStateIndefinite(STATE_COAT);
            for (int i = 0; i < 10; i++) {
                burst();
            }
            finalActor = Final.newFinalActor(FINAL_BOSS_X, FINAL_BOSS_Y, true);
            setVisible(false);
            dialogue(BotsnBoltsGame.finalImages.portrait, 0, new Runnable() {
                @Override public final void run() {
                    startArmor();
                }},
                "Do you really think you've defeated me?  You fool!  Our battle has only begun.  I don't need any wheeled monstrosity or spinning saucer to destroy you?  I've created armor for myself that matches all of the capabilties of your robotic body!");
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
            if (floorIndex > 22) {
                return;
            } else if (floorIndex == 22) {
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
        
        @Override
        public final int getRoomChangeStatus() {
            return ROOM_CHANGE_KEEP;
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
            destroy();
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
            initCharged(new AiProjectile(this, 0, 0, 0, -VEL_PROJECTILE, BotsnBoltsGame.finalImages, Player.SHOOT_CHARGE, Projectile.POWER_MAXIMUM));
        }
        
        private final void shootSelfDestruct() {
            initCharged(new Projectile(getNearestPlayer(), BotsnBoltsGame.finalImages, Player.SHOOT_CHARGE, this, 0, -VEL_PROJECTILE, Projectile.POWER_MAXIMUM));
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
            return getNearestPlayerX();
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
        private static Panmage coatTalk = null;
        private final static Panmage[] coatThrown = new Panmage[3];
        private static Panmage wounded = null;
        
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
            return Boss.initStillTimer(12, 15);
        }
        
        @Override
        protected void onAfterDefeat() {
            setMirrorTowardPlayer();
            setHidden(true);
            final Panctor finalActor = newFinalActor(getWounded(), this);
            finalActor.getPosition().setZ(BotsnBoltsGame.DEPTH_ENEMY_FRONT);
            final Pangine engine = Pangine.getEngine();
            final Player player = getNearestPlayer();
            final boolean leftSide = isOnLeftSide();
            final float playerDstX = leftSide ? 256 : 128;
            player.startScript(player.getWalkAi(playerDstX), new Runnable() {
                @Override public final void run() {
                    final float finalX = getPosition().getX();
                    final int volatileDirection = leftSide ? 1 : -1;
                    final VolatileActor volatileActor = new VolatileActor(BotsnBoltsGame.tm.getContainerColumn(finalX) + volatileDirection, 3);
                    volatileActor.getPosition().setX(finalX + (volatileDirection * 16));
                    volatileActor.setMirrorTowardPlayer();
                    finalActor.setMirror(volatileActor.isMirror());
                    new Warp(volatileActor);
                    volatileActor.materializedDialogueMessages = new String[] {
                            "I'm impressed.  You're far stronger than I realized.",
                            "Why are you helping Dr. Final?  You can choose to fight against his awful plans.",
                            "One day he will win, and I will stand at his right hand when he does.  Dr. Final will never stop trying to conquer your world." };
                    volatileActor.materializedDialogueFinishHandler = new Runnable() {
                        @Override public final void run() {
                            volatileActor.exit();
                            finalActor.destroy();
                            Final.this.setHidden(false);
                            Final.this.exit();
                            engine.addTimer(BotsnBoltsGame.tm, 30, new TimerListener() {
                                @Override public final void onTimer(final TimerEvent event) {
                                    Story.dialogue(getPlayerPortrait(), true, 0, "And I will never stop defending it.").add().setFinishHandler(new Runnable() {
                                        @Override public final void run() {
                                            Boss.award(new VictoryDisk(player, Final.this), getPlayerX(player));
                                        }});
                                }});
                        }};
                }});
        }
        
        @Override
        protected final boolean isExitAfterDefeatNeeded() {
            return false;
        }
        
        @Override
        protected final boolean isAwardNeeded() {
            return false;
        }
        
        @Override
        public final void onAward(final Player player) {
            player.dematerialize(Story.newScreenRunner(new LabScreenEnding1()));
        }
        
        protected final static Panmage getCoat() {
            return (coat = getImage(coat, "final/FinalCoat", BotsnBoltsGame.og, null, null));
        }
        
        protected final static Panmage getCoatTalk() {
            return (coatTalk = getImage(coatTalk, "final/FinalCoatTalk", BotsnBoltsGame.og, null, null));
        }
        
        private final static Panmage getCoatThrown(final int i) {
            Panmage img = coatThrown[i];
            if (img == null) {
                img = getImage(img, "final/CoatThrown" + (i + 1), BotsnBoltsGame.og, null, null);
                coatThrown[i] = img;
            }
            return img;
        }
        
        protected final static Panmage getWounded() {
            return (wounded = getImageChr(wounded, "final/FinalWounded", BotsnBoltsGame.og, null, null));
        }
        
        protected final static Panctor newFinalActor(final int x, final int y, final boolean mirror) {
            return newFinalActor(getCoat(), x, y, mirror);
        }
        
        protected final static Panctor newFinalActor(final Panmage img, final int x, final int y, final boolean mirror) {
            final Panctor actor = newFinalActor(img, mirror);
            BotsnBoltsGame.tm.savePositionXy(actor.getPosition(), x, y);
            return actor;
        }
        
        protected final static Panctor newFinalActor(final Panmage img, final boolean mirror) {
            final Panctor actor = new Panctor();
            actor.setView(img);
            actor.getPosition().setZ(BotsnBoltsGame.DEPTH_ENEMY);
            actor.setMirror(mirror);
            BotsnBoltsGame.addActor(actor);
            return actor;
        }
        
        protected final static Panctor newFinalActor(final Panmage img, final Panctor src) {
            final Panctor actor = newFinalActor(img, src.isMirror());
            actor.getPosition().set(src.getPosition());
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
