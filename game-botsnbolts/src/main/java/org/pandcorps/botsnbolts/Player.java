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
import java.util.concurrent.*;

import org.pandcorps.botsnbolts.Animal.*;
import org.pandcorps.botsnbolts.BotsnBoltsGame.*;
import org.pandcorps.botsnbolts.Enemy.*;
import org.pandcorps.botsnbolts.Extra.*;
import org.pandcorps.botsnbolts.HudMeter.*;
import org.pandcorps.botsnbolts.Profile.*;
import org.pandcorps.botsnbolts.Projectile.*;
import org.pandcorps.botsnbolts.RoomLoader.*;
import org.pandcorps.botsnbolts.ShootableDoor.*;
import org.pandcorps.core.*;
import org.pandcorps.core.chr.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.event.boundary.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.visual.*;

public class Player extends Chr implements Warpable, StepEndListener {
    protected final static int PLAYER_X = 6;
    protected final static int PLAYER_H = 23;
    protected final static int BALL_H = 15;
    protected final static int BOARD_Y_OFF = 11;
    protected final static int BOARD_H = PLAYER_H + BOARD_Y_OFF;
    protected final static int MECH_X = 8;
    protected final static int MECH_DIFF = 33;
    protected final static int MECH_H = PLAYER_H + MECH_DIFF;
    protected final static int MECH_WALK_START_INDEX = 2;
    protected final static int CENTER_Y = 11;
    private final static int SHOOT_DELAY_DEFAULT = 5;
    private final static int SHOOT_DELAY_RAPID = 3;
    private final static int SHOOT_DELAY_SPREAD = 15;
    private final static int SHOOT_DELAY_WIELD_SHORT = 9;
    private final static int SHOOT_DELAY_WIELD_MEDIUM = 21;
    private final static int SHOOT_DELAY_WIELD_LONG = 28;
    private final static int SHOOT_DELAY_STREAM = 0;
    private final static int SHOOT_STAMINA_TIMER_STREAM = 4;
    protected final static int SHOOT_TIME = 12;
    protected final static int WIELD_TIME_SHORT = 16;
    protected final static int WIELD_TIME_MEDIUM = 18;
    protected final static int WIELD_TIME_LONG = 25;
    private final static int CHARGE_TIME_MEDIUM = 30;
    private final static int CHARGE_TIME_BIG = 60;
    protected final static int STREAM_SIZE = 8;
    private final static int INVINCIBLE_TIME = 60;
    private final static int HURT_TIME = 15;
    private final static int FROZEN_TIME = 60;
    private final static int BUBBLE_TIME = 60;
    private final static int RUN_TIME = 5;
    private final static int BOARD_START_TIME = 5;
    private final static int KICKFLIP_FRAME1 = 3;
    private final static int KICKFLIP_FRAME2 = KICKFLIP_FRAME1 * 2;
    private final static int KICKFLIP_FRAME3 = KICKFLIP_FRAME1 * 3;
    protected final static int VEL_JUMP = 8;
    protected final static float VEL_BOUNCE_BOMB = 7.5f;
    protected final static float VEL_BOARD_JUMP = 8.75f;
    protected final static int VEL_SPRING = 10;
    protected final static int VEL_BOARD_BUMP = 5;
    private final static int VEL_FALL_PROTECTION = 15;
    private final static int VEL_WALK = 3;
    private final static int VEL_BOARD = 5;
    private final static int VEL_GLIDE_START = 4;
    private final static int VEL_GLIDE_DIVE = 0;
    private final static int GLIDE_MAX_SPEED = 5;
    private final static float GLIDE_ACCELERATION = 0.1875f;
    private final static float GLIDE_DECELERATION = 0.125f;
    private final static float GLIDE_BOOST_THRESHOLD = 7;
    private final static float GLIDE_DIVE_INITIAL_ACCELERATION_BOOST = gGlide / 4.0f;
    private final static float GLIDE_DIVE_ACCELERATION_BOOST = gGlide / 2.0f;
    private final static float GLIDE_PULL_UP_ACCELERATION_BOOST = -gGlide * 3.0f / 8.0f;
    private final static byte GLIDE_HORIZONTAL = 0;
    private final static byte GLIDE_UP = 1;
    private final static byte GLIDE_DOWN = -1;
    private final static int VEL_SLIDE = 6;
    private final static float VEL_DASH = 8.5f;
    private final static float DASH_SLOWDOWN = 0.1875f;
    private final static float DASH_BRAKE = 0.25f;
    private final static int VEL_MECH = 4;
    protected final static float VEL_MECH_JUMP = VEL_BOARD_JUMP;
    protected final static int VEL_PROJECTILE = 8;
    protected final static float VX_SPREAD1;
    protected final static float VY_SPREAD1;
    protected final static float VX_SPREAD2;
    protected final static float VY_SPREAD2;
    private final static double GRAPPLING_BOOST = 0.01;
    private final static double GRAPPLING_BOOST_MAX = 0.75;
    private final static double GRAPPLING_ANGLE_MIRROR_THRESHOLD = 0.01;
    private final static double GRAPPLING_ANGLE_MAX_UP = Math.PI / 8.0;
    private final static double GRAPPLING_ANGLE_MAX_DIAG = 3.0 * GRAPPLING_ANGLE_MAX_UP;
    private final static int GRAPPLING_OFF_Y = 12;
    private final static int DAMAGE_FREEZE = 1;
    private final static int VEL_ROOM_CHANGE = 10;
    protected final static float NULL_COORD = -2000;
    
    protected final PlayerContext pc;
    protected final Profile prf;
    protected final PlayerImages pi;
    private PlayerImagesSubSet currentShootSet = null;
    private int currentShootTime = SHOOT_TIME;
    protected StateHandler stateHandler = NORMAL_HANDLER;
    private boolean running = false;
    private int runIndex = 0;
    private int runTimer = 0;
    private int blinkTimer = 0;
    private long lastShotFired = NULL_CLOCK;
    private int lastShotDelay = 0;
    private long lastShotPosed = NULL_CLOCK;
    protected static long lastShotByAnyPlayer = NULL_CLOCK;
    private long startCharge = NULL_CLOCK;
    private long lastCharge = NULL_CLOCK;
    private long lastHurt = NULL_CLOCK;
    private long lastStamina = NULL_CLOCK;
    private long lastFrozen = NULL_CLOCK;
    private long lastBubble = NULL_CLOCK;
    private long lastJump = NULL_CLOCK;
    private long lastLift = NULL_CLOCK;
    private long lastBall = NULL_CLOCK;
    private long lastRightStart = NULL_CLOCK;
    private long lastLeftStart = NULL_CLOCK;
    private long lastBoardStart = NULL_CLOCK;
    private long lastBoardJump = NULL_CLOCK;
    private long lastGlideStart = NULL_CLOCK;
    private long lastGlideDirectionChange = NULL_CLOCK;
    private byte glideAngle = GLIDE_UP;
    private boolean pulledUpDuringThisGlide = false;
    private boolean anyGlidingDuringThisJump = false;
    private float hvForced = 0;
    private int wrappedJumps = 0;
    protected Carrier jumpStartedOnCarrier = null;
    protected Carrier walkedOffCarrier = null;
    private boolean jumpAllowed = true;
    private float minX = Integer.MIN_VALUE;
    private float maxX = Integer.MAX_VALUE;
    private float prevV = 0;
    private boolean prevUnderwater = false;
    private boolean sanded = false;
    private long lastSanded = NULL_CLOCK;
    private int queuedX = 0;
    private boolean air = false;
    private boolean jumping = false;
    private int wallTimer = 0;
    private int slideTimer = 0;
    private boolean wallMirror = false;
    protected boolean movedDuringJump = false;
    private byte bossDoorStatus = 0;
    protected int health = HudMeter.MAX_VALUE;
    protected HudMeter healthMeter = null;
    protected int stamina = HudMeter.MAX_VALUE;
    protected HudMeter staminaMeter = null;
    protected Pantext lifeCounter = null;
    protected GrapplingHook grapplingHook = null;
    protected double grapplingR = 0;
    private double grapplingT = 0;
    private double grapplingV = 0;
    private boolean grapplingBoostAllowed = true;
    private boolean grapplingRetractAllowed = false;
    private boolean grapplingAllowed = true;
    private final ImplPanple grapplingPosition = new ImplPanple();
    protected final SpecStreamProjectile[] streamProjectiles = new SpecStreamProjectile[STREAM_SIZE];
    private int streamStaminaCounter = 0;
    private SwordProjectile lastSwordProjectile = null;
    protected Spring spring = null;
    private SlideKick slideKick = null;
    protected Carrier carrier = null;
    protected float carrierOff = 0;
    protected float carrierX = NULL_COORD;
    private Wrapper wrapper = null;
    private int ladderColumn = -1;
    protected boolean startRoomNeeded = true;
    protected BotRoom startRoom = null;
    private float startX = NULL_COORD;
    private float startY = NULL_COORD;
    private boolean startMirror = false;
    private Pansound startMusic = null;
    private int availableRescues = 0;
    protected Rescue rescue = null;
    private float safeX = NULL_COORD;
    private float safeY = NULL_COORD;
    private boolean safeMirror = false;
    private List<Follower> followers = null;
    protected int offY = 0;
    private int boardSlope = SLOPE_NONE;
    private int boardX = 0;
    private int boardY = 0;
    private HeldShield shield = null;
    private HeldSword sword = null;
    protected ShieldProjectile lastShieldProjectile = null;
    protected boolean mechReceivingInput = false;
    protected boolean mechWalking = false;
    private int mechDir = 1;
    private int mechCounter = -1;
    private int mechIndex = MECH_WALK_START_INDEX;
    protected Panmage mechCurrentImage = null;
    private boolean hidden = false;
    protected boolean active = true;
    private boolean scripted = false;
    private boolean stopped = false;
    protected Ai ai = null;
    private Runnable scriptFinisher = null;
    
    static {
        final Panple tmp = new ImplPanple(VEL_PROJECTILE, 0, 0);
        tmp.setMagnitudeDirection(VEL_PROJECTILE, Math.PI / 4);
        VX_SPREAD1 = tmp.getX();
        VY_SPREAD1 = tmp.getY();
        tmp.setMagnitudeDirection(VEL_PROJECTILE, Math.PI / 8);
        VX_SPREAD2 = tmp.getX();
        VY_SPREAD2 = tmp.getY();
    }
    
    protected Player(final PlayerContext pc) {
        super(PLAYER_X, PLAYER_H);
        pc.player = this;
        this.pc = pc;
        prf = pc.prf;
        pi = pc.pi;
        setView(pi.basicSet.stand);
        destroyTimgPrev();
        initAvailableRescues();
        if (pc.srcPlayer != null) {
            health = pc.srcPlayer.health;
            pc.srcPlayer = null;
        }
    }
    
    private final static Panput[] getInputArray(final Panput... inputs) {
        int size = 0;
        for (final Panput input : inputs) {
            if (input != null) {
                size++;
            }
        }
        final Panput[] a = new Panput[size];
        size = 0;
        for (final Panput input : inputs) {
            if (input != null) {
                a[size] = input;
                size++;
            }
        }
        return a;
    }
    
    protected final void registerInputs(final ControlScheme ctrl) {
        final Panput[] jumpInput = getInputArray(ctrl.getJump(), Menu.jump);
        final Panput[] shootInput = getInputArray(ctrl.getAttack(), Menu.attack);
        final Panput[] rightInput = getInputArray(ctrl.getRight(), Menu.right);
        final Panput[] leftInput = getInputArray(ctrl.getLeft(), Menu.left);
        final Panput[] upInput = getInputArray(ctrl.getUp(), Menu.up);
        final Panput[] downInput = getInputArray(ctrl.getDown(), Menu.down);
        register(jumpInput, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { jump(); }});
        register(jumpInput, new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) { releaseJump(); }});
        register(shootInput, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { shoot(); }});
        register(shootInput, new ActionListener() {
            @Override public final void onAction(final ActionEvent event) { shooting(); }});
        register(shootInput, new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) { releaseShoot(); }});
        register(rightInput, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { rightStart(); }});
        register(rightInput, new ActionListener() {
            @Override public final void onAction(final ActionEvent event) { right(); }});
        register(leftInput, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { leftStart(); }});
        register(leftInput, new ActionListener() {
            @Override public final void onAction(final ActionEvent event) { left(); }});
        register(upInput, new ActionListener() {
            @Override public final void onAction(final ActionEvent event) { up(); }});
        register(downInput, new ActionListener() {
            @Override public final void onAction(final ActionEvent event) { down(); }});
        registerPause(ctrl.getSubmit());
        registerPause(ctrl.getMenu());
        if (Menu.pause != null) {
            registerPause(Menu.pause);
        }
        final Pangine engine = Pangine.getEngine();
        final Panteraction interaction = engine.getInteraction();
        registerPause(interaction.BACK);
        register(getInputArray(interaction.KEY_SHIFT_LEFT, Menu.toggleJump, interaction.KEY_PERIOD, ctrl.getTogglePositive2()), new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { toggleJumpMode(1); }});
        register(getInputArray(interaction.KEY_TAB, Menu.toggleAttack, interaction.KEY_BRACKET_RIGHT, ctrl.getTogglePositive1()), new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { toggleShootMode(1); }});
        register(getInputArray(interaction.KEY_COMMA, ctrl.getToggleNegative2()), new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { toggleJumpMode(-1); }});
        register(getInputArray(interaction.KEY_BRACKET_LEFT, ctrl.getToggleNegative1()), new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { toggleShootMode(-1); }});
        registerCapture(this);
    }
    
    protected final void registerPause(final Panput input) {
        healthMeter.register(input, new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) { togglePause(); }});
    }
    
    private final void togglePause() {
        final boolean newPaused;
        if (Menu.isCursorNeeded()) {
            final boolean oldPaused = Menu.isPauseMenuEnabled();
            newPaused = !oldPaused;
            Panlayer.setActive(getLayer(), oldPaused);
        } else {
            final Pangine engine = Pangine.getEngine();
            engine.togglePause();
            newPaused = engine.isPaused();
        }
        if (newPaused) {
            Menu.addPauseMenu(this);
        } else {
            Menu.destroyPauseMenu();
        }
    }
    
    protected final static boolean isPaused() {
        return Menu.isCursorNeeded() ? Menu.isPauseMenuEnabled() : Pangine.getEngine().isPaused(); // Handle same as BotsnBoltsGame.isClockRunning
    }
    
    private final void toggleJumpMode(final int dir) {
        prf.jumpMode.onDeselect(this);
        prf.jumpMode = toggleInputMode(JUMP_MODES, prf.jumpMode, dir);
    }
    
    private final void toggleShootMode(final int dir) {
        setShootMode(toggleInputMode(SHOOT_MODES, prf.shootMode, dir));
    }
    
    protected final void setShootMode(final ShootMode shootMode) {
        if (prf.shootMode != null) {
            prf.shootMode.onDeselect(this);
        }
        prf.shootMode = shootMode;
        shootMode.onSelect(this);
    }
    
    private final <T extends InputMode> T toggleInputMode(final T[] modes, final T currentMode, final int dir) {
        final int last = modes.length - 1;
        int index = getIndex(modes, currentMode);
        while(true) {
            index += dir;
            if (index > last) {
                index = 0;
            } else if (index < 0) {
                index = last;
            }
            final T newMode = modes[index];
            if (newMode.isAvailable(this) && newMode.isCurrentlyAllowed(this)) {
                return newMode;
            }
        }
    }
    
    private final static int getIndex(final Object[] a, final Object toFind) {
        final int size = a.length;
        for (int i = 0; i < size; i++) {
            final Object o = a[i];
            if (toFind.equals(o)) {
                return i;
            }
        }
        return 0;
    }
    
    protected final void prepareForScript() {
        lastHurt = NULL_CLOCK;
        if (!hidden) {
            setVisible(true);
        }
        if ((stateHandler == BALL_HANDLER) || (stateHandler == SLIDE_HANDLER)) {
            endSlide(); // Also calls endBall
        }
        endStatesIncompatibleWithRoomChange();
    }
    
    protected final void startScript(final Ai ai, final Runnable scriptFinisher) {
        prepareForScript();
        this.ai = ai;
        scripted = true;
        active = true;
        this.scriptFinisher = scriptFinisher;
    }
    
    protected final void finishScript() {
        ai = null;
        scripted = false;
        if (scriptFinisher != null) {
            scriptFinisher.run();
            scriptFinisher = null;
        }
    }
    
    protected final boolean isFree() {
        return !stopped && isFreeOrStopped();
    }
    
    protected final boolean isFreeOrStopped() {
        final boolean free = active && !scripted && !(isHurt() || isFrozen() || Boss.dropping || RoomChanger.isChanging() || RoomLoader.isBossDoorClosing() || Pangine.getEngine().isPaused());
        if (free) {
            onFree();
        }
        return free;
    }
    
    private final void onFree() {
        if (!isGrounded()) {
            return;
        } else if (!startRoomNeeded && (availableRescues == 0)) {
            return;
        }
        updateSafeCoordinates();
    }
    
    private final void updateSafeCoordinates() {
        if (sanded) {
            return;
        } else if (!stateHandler.isSafePositionAllowed(this)) {
            return;
        }
        final Panple pos = getPosition();
        safeX = pos.getX();
        safeY = pos.getY();
        safeMirror = isMirror();
        if (startRoomNeeded) {
            startRoom = RoomLoader.getCurrentRoom();
            startX = safeX;
            startY = safeY;
            startMirror = safeMirror;
            startMusic = Pangine.getEngine().getAudio().getMusic();
            startRoomNeeded = false;
            warpOthers();
        }
    }
    
    protected final void jump() {
        if (jumpAllowed && isFree()) {
            stateHandler.onJump(this);
        }
    }
    
    private final boolean onJumpNormal() {
        return onJumpNormal(VEL_JUMP);
    }
    
    private final boolean onJumpNormal(final float velJump) {
        if (isGrounded()) {
            startJump(null, velJump);
            return true;
        } else {
            stateHandler.onAirJump(this);
            return false;
        }
    }
    
    private final void onAirJumpNormal() {
        if (prf.airJump) {
            startJump();
        }
    }
    
    private final void startJump() {
        startJump(null);
    }
    
    private final void startJump(final Carrier jumpStartedOnCarrier) {
        startJump(jumpStartedOnCarrier, VEL_JUMP);
    }
    
    private final void startJump(final Carrier jumpStartedOnCarrier, final float velJump) {
        final boolean wasGrounded = isGrounded();
        this.jumpStartedOnCarrier = jumpStartedOnCarrier;
        if (isOnFallProtectionRow()) {
            v = VEL_FALL_PROTECTION;
        } else {
            v = velJump;
            if (sanded) {
                v -= 2;
            }
        }
        lastJump = getClock();
        jumping = true;
        if (wasGrounded || (jumpStartedOnCarrier != null)) {
            anyGlidingDuringThisJump = false;
        }
        BotsnBoltsGame.fxJump.startSound();
    }
    
    protected final void releaseJump() {
        stateHandler.releaseJump(this);
    }
    
    protected final void releaseJumpNormal() {
        if ((v > 0) && (getClock() > (lastLift + 1))) {
            v = 0;
        }
    }
    
    protected final void setJumpAllowed(final boolean jumpAllowed) {
        this.jumpAllowed = jumpAllowed;
    }
    
    @Override
    protected final boolean isFloorBehavior(final byte b) {
        return super.isFloorBehavior(b) || stateHandler.isFloorBehavior(b);
    }
    
    @Override
    protected final boolean isUpslopeFloorBehavior(final byte b) {
        return stateHandler.isUpslopeFloorBehavior(b);
    }
    
    @Override
    protected final boolean isDownslopeFloorBehavior(final byte b) {
        return stateHandler.isDownslopeFloorBehavior(b);
    }
    
    @Override
    protected final float getG() {
        return stateHandler.getG(this);
    }
    
    protected final float getGNormal() {
        return super.getG();
    }
    
    protected final void shoot() {
        if (isFree()) {
            stateHandler.onShootStart(this);
        }
    }
    
    protected final void shooting() {
        if (isFree()) {
            stateHandler.onShooting(this);
        }
    }
    
    protected final void releaseShoot() {
        if (isFree()) {
            stateHandler.onShootEnd(this);
        }
    }
    
    protected Panctor newProjectile(final float vx, final float vy, final int power) {
        return new Projectile(this, vx, vy, power);
    }
    
    protected void newBomb() {
        new Bomb(this);
    }
    
    protected SpecStreamProjectile newStreamProjectile(final int ox) {
        return new StreamProjectile(this, ox);
    }
    
    protected final int getStreamOffsetX() {
        if (stateHandler == WALL_GRAB_HANDLER) {
            return 3;
        } else if (!isGrounded()) {
            return 2;
        } else if (running) {
            return 2;
        } else if (isDashing()) {
            return 2;
        }
        return 4;
    }
    
    protected final int getStreamOffsetY() {
        if (stateHandler == WALL_GRAB_HANDLER) {
            return -1;
        } else if (stateHandler == LADDER_HANDLER) {
            return -2;
        } else if (!isGrounded()) {
            return 0;
        } else if (running) {
            return ((runIndex == 0) || (runIndex == 2)) ? -4 : -2;
        }
        return -3;
    }
    
    protected final void resetStream() {
        detach(streamProjectiles[1]);
        detach(streamProjectiles[2]);
    }
    
    protected final void clearStream() {
        detach(streamProjectiles);
    }
    
    private final void afterShoot(final long clock) {
        lastShotFired = clock;
        lastShotPosed = clock;
        lastShotByAnyPlayer = clock;
        blinkTimer = 0;
    }
    
    protected final boolean isAimMirrorReversed() {
        return stateHandler.isAimMirrorReversed(this);
    }
    
    protected final boolean getAimMirror() {
        return isAimMirrorReversed() ? !isMirror() : isMirror();
    }
    
    protected final int getAimOffsetX() {
        return stateHandler.getAimOffsetX(this);
    }
    
    protected final int getAimOffsetXNormal() {
        if ((prf.shootMode != SHOOT_STREAM) && isDashing()) {
            final int off = Math.round(Math.abs(hvForced)) - VEL_WALK;
            if (off > 0) {
                return Projectile.OFF_X + (2 * off);
            }
        } else if (stateHandler == LADDER_HANDLER && isMirror()) {
            return Projectile.OFF_X - 1;
        }
        return Projectile.OFF_X;
    }
    
    protected final int getAimOffsetY() {
        return stateHandler.getAimOffsetY(this);
    }
    
    protected final void rightStart() {
        if (isFreeOrStopped()) {
            stateHandler.onRightStart(this);
        }
    }
    
    private final void onRightStartNormal() {
        lastRightStart = startDashIfNeeded(lastRightStart, 1);
    }
    
    protected final void right() {
        if (isFreeOrStopped()) {
            lastLeftStart = NULL_CLOCK;
            stateHandler.onRight(this);
        }
    }
    
    private final void onRightNormal() {
        moveHorizontal(VEL_WALK);
    }
    
    private final void moveHorizontal(final int vel) {
        if (hvForced > VEL_WALK) {
            if (vel < 0) {
                hvForced -= DASH_BRAKE;
            }
        } else if (hvForced < -VEL_WALK) {
            if (vel > 0) {
                hvForced += DASH_BRAKE;
            }
        } else {
            hv = vel;
        }
        if (!isGrounded()) {
            movedDuringJump = true;
        }
    }
    
    protected final void initMirror() {
        setMirror(isOnLeftSide() ? 1 : -1);
    }
    
    @Override
    protected final void setMirror(final int v) {
        setMirror(v, true);
    }
    
    protected final void setMirror(final int v, final boolean onMirrorNeeded) {
        final boolean oldMirror = isMirror();
        super.setMirror(v);
        if (oldMirror != isMirror()) {
            if (onMirrorNeeded) {
                onMirror();
            }
            fixX();
        }
    }
    
    private final void onMirror() {
        lastShotPosed = NULL_CLOCK;
    }
    
    protected final void leftStart() {
        if (isFreeOrStopped()) {
            stateHandler.onLeftStart(this);
        }
    }
    
    private final void onLeftStartNormal() {
        lastLeftStart = startDashIfNeeded(lastLeftStart, -1);
    }
    
    protected final void left() {
        if (isFreeOrStopped()) {
            lastRightStart = NULL_CLOCK;
            stateHandler.onLeft(this);
        }
    }
    
    private final void onLeftNormal() {
        moveHorizontal(-VEL_WALK);
    }
    
    protected final void up() {
        if (isFree()) {
            stateHandler.onUp(this);
        }
    }
    
    private final void onUpNormal() {
        if (isTouchingLadder()) {
            startLadder();
        }
    }
    
    private final void startLadder() {
        final Panple pos = getPosition();
        pos.setX((Math.round(pos.getX()) / 16) * 16 + 7);
        stateHandler = LADDER_HANDLER;
        clearDash();
        endBoardIfNeeded();
        endGlider();
    }
    
    private final static int OFF_LADDER_TOP = 20;
    private final static int OFF_LADDER_BOTTOM = 11;
    
    private final boolean isTouchingLadder() {
        return isTouchingLadder(OFF_LADDER_TOP);
    }
    
    private final boolean isTouchingLadder(final int yoff) {
        final Panple pos = getPosition();
        final int tileIndex = BotsnBoltsGame.tm.getContainer(pos.getX(), pos.getY() + yoff);
        final byte b = getBehavior(tileIndex);
        return b == BotsnBoltsGame.TILE_LADDER || b == BotsnBoltsGame.TILE_LADDER_TOP;
    }
    
    protected final void down() {
        if (isFree()) {
            stateHandler.onDown(this);
        }
    }
    
    private final void onDownNormal() {
        if (isAboveLadder()) {
            startLadder();
            getPosition().addY(-OFF_LADDER_BOTTOM);
        }
    }
    
    private final boolean isAboveLadder() {
        return isGrounded() && isTouchingLadder(-1);
    }
    
    @Override
    public void onMaterialized() {
        stateHandler = NORMAL_HANDLER;
    }
    
    private Runnable unwarpHandler = null;
    
    protected final void dematerialize(final Runnable unwarpHandler) {
        this.unwarpHandler = unwarpHandler;
        active = false;
        new Dematerialize(this);
    }
    
    protected final void dematerializeOthers(final Runnable unwarpHandler) {
        BotsnBoltsGame.runPlayers(new PlayerRunnable() {
            @Override
            public final void run(final Player player) {
                if ((player == Player.this) || isDestroyed(player)) {
                    return;
                }
                player.dematerialize(unwarpHandler);
            }
        });
    }
    
    @Override
    public final void onUnwarped() {
        if (unwarpHandler == null) {
            return;
        }
        unwarpHandler.run();
    }
    
    protected final void warpOthers() {
        BotsnBoltsGame.runPlayerContexts(new PlayerContextRunnable() {
            @Override
            public final void run(final PlayerContext pc) {
                if (pc.isLifeCounterEmpty()) {
                    return;
                }
                final Player oldPlayer = PlayerContext.getPlayer(pc);
                if (oldPlayer == Player.this) {
                    return;
                } else if (!isDestroyed(oldPlayer)) {
                    if (oldPlayer.isVisible()) {
                        return;
                    }
                    oldPlayer.destroy();
                }
                final Player newPlayer = new Player(pc);
                if (oldPlayer != null) {
                    newPlayer.healthMeter = oldPlayer.healthMeter;
                    newPlayer.staminaMeter = oldPlayer.staminaMeter;
                    newPlayer.lifeCounter = oldPlayer.lifeCounter;
                    if (oldPlayer.health > 0) {
                        newPlayer.health = oldPlayer.health;
                        newPlayer.stamina = oldPlayer.stamina;
                    }
                }
                newPlayer.getPosition().set(Player.this.getPosition());
                BotsnBoltsGame.addActor(newPlayer);
                new Warp(newPlayer);
                newPlayer.registerInputs(pc.ctrl);
            }
        });
    }
    
    protected final void launch(final int dstX, final int dstY) {
        RoomLoader.startX = dstX;
        RoomLoader.startY = dstY;
        dematerialize(getLaunchHandler());
    }
    
    protected final Runnable getLaunchHandler() {
        return new Runnable() {
            @Override
            public final void run() {
                pc.srcPlayer = Player.this;
                Panscreen.set(new BotsnBoltsGame.BotsnBoltsScreen());
            }
        };
    }
    
    protected final static Runnable levelSelectHandler = new Runnable() {
        @Override
        public final void run() {
            Menu.goLevelSelect();
        }
    };
    
    protected final boolean hurt(final int damage) {
        if (isInvincible()) {
            return false;
        }
        hurtForce(damage);
        if (!prf.stunProtection) {
            BotsnBoltsGame.fxHurt.startSound();
        }
        return true;
    }
    
    private final void hurtForce(final int damage) {
        hurtForce(damage, true);
    }
    
    protected final void hurtForce(final int damage, final boolean effectsNeeded) {
        final boolean effectsAllowed = effectsNeeded && !prf.stunProtection;
        if (effectsAllowed) {
            stateHandler.onHurt(this);
            lastHurt = getClock();
            isFree(); // Calls onFree(); do after setting lastHurt to avoid loop
        }
        blinkTimer = 0;
        if (!prf.infiniteHealth) {
            health -= damage;
        }
        if (effectsAllowed) {
            if ((v > 0) && !isGrounded()) {
                v = 0;
            }
            startCharge = NULL_CLOCK;
            lastCharge = NULL_CLOCK;
        }
        if (health <= 0) {
            defeat();
        } else if (effectsAllowed) {
            addFollower(burst(BotsnBoltsGame.flash, 0, CENTER_Y, BotsnBoltsGame.DEPTH_POWER_UP));
            puff(-12, 25);
            puff(0, 30);
            puff(12, 25);
        }
    }
    
    private final boolean useStamina(final int amount) {
        if (prf.infiniteStamina) {
            return true;
        } else if (amount > stamina) {
            return false;
        }
        stamina -= amount;
        lastStamina = getClock();
        return true;
    }
    
    private final boolean useAttackStamina(final int amount) {
        if (useStamina(amount)) {
            return true;
        }
        setShootMode(SHOOT_NORMAL);
        return false;
    }
    
    protected final boolean freeze() {
        return freeze(getClock());
    }
    
    protected final boolean freezeIndefinite() {
        return freeze(Long.MAX_VALUE);
    }
    
    private final boolean freeze(final long freezeTime) {
        if (prf.stunProtection) {
            hurt(DAMAGE_FREEZE);
            return false;
        } else if (isInvincible()) {
            return false;
        }
        stateHandler.onHurt(this);
        lastFrozen = freezeTime;
        BotsnBoltsGame.fxRicochet.startSound();
        return true;
    }
    
    private final void puff(final int offX, final int offY) {
        addFollower(puff(this, offX, offY));
    }
    
    protected final static BurstFollower puff(final Panctor src, final int offX, final int offY) {
        return burst(src, BotsnBoltsGame.puff, offX, offY, BotsnBoltsGame.DEPTH_BURST);
    }
    
    private final BurstFollower burst(final Panimation anm, final int offX, final int offY, final int z) {
        return burst(this, anm, offX, offY, z);
    }
    
    private final static BurstFollower burst(final Panctor src, final Panimation anm, final int offX, final int offY, final int z) {
        final BurstFollower puff = new BurstFollower(anm, offX, offY);
        final Panple playerPos = src.getPosition();
        puff.getPosition().set(playerPos.getX() + offX, playerPos.getY() + offY, z);
        puff.setMirror(src.isMirror());
        addActor(src, puff);
        return puff;
    }
    
    private final void puffStill(final int offX, final int offY) {
        burstStill(this, BotsnBoltsGame.puff, offX, offY);
    }
    
    private final static void burstStill(final Panctor src, final Panimation anm, final int offX, final int offY) {
        final Burst burst = new Burst(anm);
        final Panple playerPos = src.getPosition();
        final boolean mirror = src.isMirror();
        burst.getPosition().set(playerPos.getX() + (getMirrorMultiplier(mirror) * offX), playerPos.getY() + offY, BotsnBoltsGame.DEPTH_BURST);
        burst.setMirror(mirror);
        addActor(src, burst);
    }
    
    protected final static void defeatOrbs(final Panctor src, final Panimation defeat) {
        final float baseVelDiag = (float) Math.sqrt(0.5);
        for (int m = 1; m < 3; m++) {
            final float velDiag = m * baseVelDiag, vel = m;
            defeatOrb(src, defeat, 0, vel);
            defeatOrb(src, defeat, velDiag, velDiag);
            defeatOrb(src, defeat, vel, 0);
            defeatOrb(src, defeat, velDiag, -velDiag);
            defeatOrb(src, defeat, 0, -vel);
            defeatOrb(src, defeat, -velDiag, -velDiag);
            defeatOrb(src, defeat, -vel, 0);
            defeatOrb(src, defeat, -velDiag, velDiag);
        }
    }
    
    protected final void defeat() {
        if (isDestroyed()) {
            return;
        }
        defeatOrbs(this, pi.defeat);
        destroy();
        if (isAnyPlayerRemaining()) {
            onOnePlayerDefeated();
        } else {
            onLastPlayerDefeated();
        }
    }
    
    private final void onOnePlayerDefeated() {
        decrementLifeCounter();
        startDefeatTimer(null);
    }
    
    private final void onLastPlayerDefeated() {
        Pangine.getEngine().getAudio().stopMusic();
        startDefeatTimer(new Runnable() {
            @Override public final void run() {
                finishDefeat();
            }});
    }
    
    protected final static boolean isAnyPlayerRemaining() {
        for (final PlayerContext pc : BotsnBoltsGame.pcs) {
            if (!isDestroyed(PlayerContext.getPlayer(pc))) {
                return true;
            }
        }
        return false;
    }
    
    protected final static void startDefeatTimer(final Runnable finisher) {
        startDefeatTimer(5, finisher);
    }
    
    private final static void startDefeatTimer(final int i, final Runnable finisher) {
        BotsnBoltsGame.fxDefeat.startSound();
        Pangine.getEngine().addTimer(BotsnBoltsGame.tm, (i == 0) ? 70 : 10, new TimerListener() {
            @Override public final void onTimer(final TimerEvent event) {
                if (i > 0) {
                    startDefeatTimer(i - 1, finisher);
                    return;
                } else if (finisher != null) {
                    finisher.run();
                }
            }});
    }
    
    private final void finishDefeat() {
        healthMeter.destroy();
        Panctor.destroy(staminaMeter);
        Panctor.destroy(lifeCounter);
        decrementLifeCounter();
        resumeAfterDefeat();
    }
    
    private final void decrementLifeCounter() {
        if (!prf.infiniteLives) {
            pc.lives--;
            if (pc.isLifeCounterEmpty() && !isAnyPlayerRemaining()) {
                BotsnBoltsGame.startGame();
                return;
            }
        }
    }
    
    private final void resumeAfterDefeat() {
        if (startRoom == null) {
            RoomLoader.reloadCurrentRoom();
        } else {
            BotsnBoltsGame.playerStartX = startX;
            BotsnBoltsGame.playerStartY = startY;
            BotsnBoltsGame.playerStartMirror = startMirror;
            RoomLoader.loadRoom(startRoom);
            Pansound.changeMusic(startMusic);
        }
    }
    
    @Override
    protected final void onDestroy() {
        destroyGrapplingHook();
        freeWrapper();
        destroy(shield);
        destroy(sword);
        super.onDestroy();
    }
    
    private final static void defeatOrb(final Panctor src, final Panimation defeat, final float velX, final float velY) {
        final DefeatOrb orb = new DefeatOrb();
        orb.setView(defeat);
        final Panple playerPos = src.getPosition();
        orb.getPosition().set(playerPos.getX(), playerPos.getY() + 12, BotsnBoltsGame.DEPTH_BURST);
        orb.getVelocity().set(velX, velY);
        addActor(src, orb);
    }
    
    protected final Panlayer getLayerRequired() {
        return getLayerRequired(this);
    }
    
    protected final static Panlayer getLayerRequired(final SpecPanctor src) {
        final Panlayer layer = (src == null) ? null : src.getLayer();
        return (layer == null) ? BotsnBoltsGame.getLayer() : layer;
    }
    
    protected final static Profile getProfile(final Player player) {
        return (player == null) ? null : player.prf;
    }
    
    protected final void addActor(final Panctor actor) {
        addActor(this, actor);
    }
    
    protected final static void addActor(final SpecPanctor src, final Panctor actor) {
        getLayerRequired(src).addActor(actor);
    }
    
    public final int getHealth() {
        return health;
    }
    
    protected final void addHealth(final int amount) {
        if (health >= HudMeter.MAX_VALUE) {
            return;
        }
        health += amount;
        if (health > HudMeter.MAX_VALUE) {
            health = HudMeter.MAX_VALUE;
        }
    }
    
    private final boolean isHurt() {
        return (stateHandler == RESCUED_HANDLER) || ((getClock() - lastHurt) < HURT_TIME);
    }
    
    private final boolean isFrozen() {
        return (getClock() - lastFrozen) < FROZEN_TIME;
    }
    
    private final boolean onHurting() {
        if (isHurt()) {
            changeView(pi.hurt);
            return true;
        } else if (isFrozen()) {
            changeView(pi.frozen);
            return true;
        } else if (getCurrentDisplay() == pi.frozen) {
            unfreeze();
        }
        return false;
    }
    
    private final void unfreeze() {
        unfreeze(DAMAGE_FREEZE);
    }
    
    protected final void unfreeze(final int damage) {
        if (damage > 0) {
            hurtForce(damage);
        } else {
            lastFrozen = NULL_CLOCK;
        }
        BotsnBoltsGame.fxCrumble.startSound();
        shatter(this, BotsnBoltsGame.getIceShatter());
    }
    
    protected final static void shatter(final Panctor src, final Panmage img) {
        shatter(src, img, CENTER_Y, true, true, true, true);
    }
    
    protected final static void shatter(final Panctor src, final Panmage img, final int offY,
                                        final boolean topLeft, final boolean topRight, final boolean bottomLeft, final boolean bottomRight) {
        shatter(src.getLayer(), src.getPosition(), src.isMirror(), img, offY, topLeft, topRight, bottomLeft, bottomRight);
    }
    
    protected final static void shatter(final Panple pos, final Panmage img) {
        shatter(BotsnBoltsGame.getLayer(), pos, false, img, CENTER_Y, true, true, true, true);
    }
    
    protected final static void shatter(final Panlayer layer, final Panple pos, final boolean mirror, final Panmage img, final int offY,
                                        final boolean topLeft, final boolean topRight, final boolean bottomLeft, final boolean bottomRight) {
        final int m = Panctor.getMirrorMultiplier(mirror);
        final float x = pos.getX(), y = pos.getY() + offY;
        newDiver(layer, mirror, img, x - m * 4, y + 4, -m, 3, false, false, topLeft);
        newDiver(layer, mirror, img, x + m * 4, y + 4, m, 3, true, false, topRight);
        newDiver(layer, mirror, img, x - m * 4, y - 4, -m * 2, 2, false, true, bottomLeft);
        newDiver(layer, mirror, img, x + m * 4, y - 4, m * 2, 2, true, true, bottomRight);
    }
    
    protected final static void newDiver(final Panlayer layer, final boolean srcMirror, final Panmage img, final float x, final float y, final float xv, final float yv,
                                       final boolean mirror, final boolean flip, final boolean needed) {
        if (!needed || (layer == null)) {
            return;
        }
        final Diver diver = new Diver(layer, img, x, y, BotsnBoltsGame.DEPTH_BURST, xv * newDiveMultiplier(), yv * newDiveMultiplier(), gTuple);
        diver.setMirror(mirror ^ srcMirror);
        diver.setFlip(flip);
    }
    
    private final static float newDiveMultiplier() {
        return Mathtil.randf(0.7f, 1.3f);
    }
    
    protected final boolean isInvincible() {
        return isInvincible(true);
    }
    
    private final boolean isInvincible(final boolean frozenConsidered) {
        final long clock = getClock();
        return (stateHandler == RESCUED_HANDLER)
                || ((clock - lastHurt) < INVINCIBLE_TIME)
                || (frozenConsidered && (clock - lastFrozen) < (INVINCIBLE_TIME + FROZEN_TIME - HURT_TIME));
    }
    
    private final boolean isShootPoseNeeded() {
        if ((getClock() - lastShotPosed) < currentShootTime) {
            return true;
        }
        currentShootTime = SHOOT_TIME;
        return false;
    }
    
    private final PlayerImagesSubSet getCurrentImagesSubSet() {
        if (isShootPoseNeeded()) {
            return getCurrentAimImagesSubSet();
        }
        currentShootSet = null;
        return pi.basicSet;
    }
    
    private final PlayerImagesSubSet getCurrentAimImagesSubSet() {
        if (currentShootSet != null) {
            return currentShootSet;
        }
        return pi.shootSet;
    }
    
    private final void clearRun() {
        runIndex = 0;
        runTimer = 0;
    }
    
    private final int getDirection(final int v) {
        return (v == 0) ? 0 : v / Math.abs(v);
    }
    
    @Override
    protected final void renderView(final Panderer renderer) {
        stateHandler.renderView(this, renderer);
    }
    
    protected final void renderViewNormal(final Panderer renderer) {
        if (isMirror()) {
            final Pansplay display = getCurrentDisplay();
            final Object o = Panmage.getExtra(display);
            if (o != null) {
                final PlayerImageExtra ext = (PlayerImageExtra) o;
                final int mirrorX = ext.mirrorX;
                if (mirrorX != 0) {
                    final Panple pos = getPosition();
                    renderer.render(getLayer(), (Panmage) display, pos.getX() + mirrorX, pos.getY(), pos.getZ(), getRot(), true, isFlip());
                    return;
                }
            }
        }
        super.renderView(renderer);
    }
    
    private final static long getExhaustIndex() {
        return (getClock() % 6) / 2;
    }
    
    protected final void renderViewBoard(final Panderer renderer) {
        final Panlayer layer = getLayer();
        final Panple pos = getPosition();
        final float x = pos.getX(), y = pos.getY();
        final boolean mirror = isMirror();
        final int m = getMirrorMultiplier(mirror);
        renderer.render(layer, (Panmage) getCurrentDisplay(), x, y + BOARD_Y_OFF, pos.getZ(), 0, mirror, false);
        final long exhaustIndex = getExhaustIndex();
        if (boardSlope == SLOPE_NONE) {
            final long boardTime = getBoardTime();
            if (boardTime == 0) {
                Warp.renderWarp(renderer, layer, pi, x + (m * (3 + boardX)) + (mirror ? 3 : 0), y + 43, BotsnBoltsGame.DEPTH_PLAYER_FRONT);
            } else if (boardTime == 1) {
                Warp.renderWarp(renderer, layer, pi, x + (m * (3 + boardX)) + (mirror ? 3 : 0), y + 11, BotsnBoltsGame.DEPTH_PLAYER_FRONT);
            }
            if (boardTime < BOARD_START_TIME) {
                renderer.render(layer, pi.materialize.getFrames()[0].getImage(),
                        x - (m * (14 - boardX)) - (mirror ? 31 : 0), y + 3 + boardY, BotsnBoltsGame.DEPTH_PLAYER_FRONT,
                        0, 0, 32, 32, 0, mirror, false);
            } else {
                renderer.render(layer, pi.boardImage = Animal.getAnimalImage(pi.boardImage, pi, "Board"),
                        x - (m * (17 - boardX)) - (mirror ? 31 : 0), y + 1 + boardY, BotsnBoltsGame.DEPTH_PLAYER_FRONT,
                        0, 0, 32, 32, 0, mirror, false);
                if (exhaustIndex == 1) {
                    renderer.render(layer, pi.exhaust1, x - (m * (20 - boardX)) - (mirror ? 3 : 0), y + 3 + boardY, BotsnBoltsGame.DEPTH_PLAYER_BACK, 0, 0, 4, 4, 0, mirror, false);
                } else if (exhaustIndex == 2) {
                    renderer.render(layer, pi.exhaust2, x - (m * (22 - boardX)) - (mirror ? 7 : 0), y + 1 + boardY, BotsnBoltsGame.DEPTH_PLAYER_BACK, 0, 0, 8, 8, 0, mirror, false);
                }
            }
        } else if (boardSlope == SLOPE_UP ) {
            final long boardJumpTime = getBoardJumpTime();
            if (boardJumpTime < KICKFLIP_FRAME1) {
                renderer.render(layer, pi.boardDiagImageBottom = Animal.getAnimalImage(pi.boardDiagImageBottom, pi, "BoardDiagBottom"),
                        x - (m * 7) - (mirror ? 31 : 0), y - 4, BotsnBoltsGame.DEPTH_PLAYER_FRONT,
                        0, 0, 32, 32, 0, mirror, false);
                renderDoubleExhaust(renderer, layer, exhaustIndex, x, y, m, mirror);
            } else if (boardJumpTime < KICKFLIP_FRAME2) {
                renderer.render(layer, pi.boardDiagImage = Animal.getAnimalImage(pi.boardDiagImage, pi, "BoardDiag"),
                        x - (m * 6) - (mirror ? 31 : 0), y - 3, BotsnBoltsGame.DEPTH_PLAYER_FRONT,
                        0, 0, 32, 32, 3, mirror, true);
                if (exhaustIndex == 1) {
                    renderer.render(layer, pi.exhaustDiag1, x - (m * (8 - boardX)) - (mirror ? 3 : 0), y - 2, BotsnBoltsGame.DEPTH_PLAYER_BACK, 0, 0, 4, 4, 0, mirror, false);
                } else if (exhaustIndex == 2) {
                    renderer.render(layer, pi.exhaustDiag2, x - (m * (11 - boardX)) - (mirror ? 7 : 0), y - 4, BotsnBoltsGame.DEPTH_PLAYER_BACK, 0, 0, 8, 8, 0, mirror, false);
                }
            } else if (boardJumpTime < KICKFLIP_FRAME3) {
                renderer.render(layer, pi.boardDiagImageTop = Animal.getAnimalImage(pi.boardDiagImageTop, pi, "BoardDiagTop"),
                        x - (m * 7) - (mirror ? 31 : 0), y - 4, BotsnBoltsGame.DEPTH_PLAYER_FRONT,
                        0, 0, 32, 32, 0, mirror, false);
                renderDoubleExhaust(renderer, layer, exhaustIndex, x, y, m, mirror);
            } else {
                renderer.render(layer, pi.boardDiagImage = Animal.getAnimalImage(pi.boardDiagImage, pi, "BoardDiag"),
                        x - (m * 6) - (mirror ? 31 : 0), y - 3, BotsnBoltsGame.DEPTH_PLAYER_FRONT,
                        0, 0, 32, 32, 0, mirror, false);
                if (exhaustIndex == 1) {
                    renderer.render(layer, pi.exhaustDiag1, x - (m * (5 - boardX)) - (mirror ? 3 : 0), y - 5, BotsnBoltsGame.DEPTH_PLAYER_BACK, 0, 0, 4, 4, 0, mirror, false);
                } else if (exhaustIndex == 2) {
                    renderer.render(layer, pi.exhaustDiag2, x - (m * (7 - boardX)) - (mirror ? 7 : 0), y - 8, BotsnBoltsGame.DEPTH_PLAYER_BACK, 0, 0, 8, 8, 0, mirror, false);
                }
            }
        } else if (boardSlope == SLOPE_DOWN ) {
            renderer.render(layer, pi.boardDiagImage = Animal.getAnimalImage(pi.boardDiagImage, pi, "BoardDiag"),
                    x - (m * 18) - (mirror ? 31 : 0), y - 11, BotsnBoltsGame.DEPTH_PLAYER_FRONT,
                    0, 0, 32, 32, 3, mirror, false);
            if (exhaustIndex == 1) {
                renderer.render(layer, pi.exhaustDiag1, x - (m * (20 - boardX)) - (mirror ? 3 : 0), y + 16, BotsnBoltsGame.DEPTH_PLAYER_BACK, 0, 0, 4, 4, 3, mirror, false);
            } else if (exhaustIndex == 2) {
                renderer.render(layer, pi.exhaustDiag2, x - (m * (23 - boardX)) - (mirror ? 7 : 0), y + 14, BotsnBoltsGame.DEPTH_PLAYER_BACK, 0, 0, 8, 8, 3, mirror, false);
            }
        }
    }
    
    private final void renderDoubleExhaust(final Panderer renderer, final Panlayer layer, final long exhaustIndex, final float x, final float y, final int m, final boolean mirror) {
        if (exhaustIndex == 1) {
            renderer.render(layer, pi.exhaustDiag1, x - (m * (9 - boardX)) - (mirror ? 3 : 0), y - 1, BotsnBoltsGame.DEPTH_PLAYER_BACK, 0, 0, 4, 4, 0, mirror, false);
            renderer.render(layer, pi.exhaustDiag1, x - (m * (4 - boardX)) - (mirror ? 3 : 0), y - 6, BotsnBoltsGame.DEPTH_PLAYER_BACK, 0, 0, 4, 4, 0, mirror, false);
        } else if (exhaustIndex == 2) {
            renderer.render(layer, pi.exhaustDiag2, x - (m * (12 - boardX)) - (mirror ? 7 : 0), y - 3, BotsnBoltsGame.DEPTH_PLAYER_BACK, 0, 0, 8, 8, 0, mirror, false);
            renderer.render(layer, pi.exhaustDiag2, x - (m * (6 - boardX)) - (mirror ? 7 : 0), y - 9, BotsnBoltsGame.DEPTH_PLAYER_BACK, 0, 0, 8, 8, 0, mirror, false);
        }
    }
    
    protected final void renderViewGlider(final Panderer renderer) {
        final Panlayer layer = getLayer();
        final Panple pos = getPosition();
        final float x = pos.getX(), y = pos.getY();
        final boolean mirror = isMirror();
        final int m = getMirrorMultiplier(mirror);
        final long glideTime = getClock() - lastGlideStart;
        final long exhaustIndex = getExhaustIndex();
        if (glideTime == 0) {
            renderer.render(layer, Animal.getBirdImage(pi),
                    x - (m * 80) - (mirror ? 31 : 0), y - 64, BotsnBoltsGame.DEPTH_PLAYER_BACK, 0, 0, 32, 32, 0, mirror, false);
        } else if (glideTime == 1) {
            renderer.render(layer, Animal.getBirdImage(pi),
                    x - (m * 64) - (mirror ? 31 : 0), y - 48, BotsnBoltsGame.DEPTH_PLAYER_BACK, 0, 0, 32, 32, 0, mirror, false);
        } else if (glideTime == 2) {
            renderer.render(layer, pi.gliderUpImage = Animal.getBirdImage(pi.gliderUpImage, pi, "GlideUp"),
                    x - (m * 66) - (mirror ? 63 : 0), y - 43, BotsnBoltsGame.DEPTH_PLAYER_BACK, 0, 0, 64, 64, 0, mirror, false);
        } else if (glideTime == 3) {
            renderer.render(layer, pi.gliderUpImage = Animal.getBirdImage(pi.gliderUpImage, pi, "GlideUp"),
                    x - (m * 50) - (mirror ? 63 : 0), y - 27, BotsnBoltsGame.DEPTH_PLAYER_BACK, 0, 0, 64, 64, 0, mirror, false);
        } else if (glideAngle == GLIDE_HORIZONTAL) {
            renderer.render(layer, pi.gliderHorizImage = Animal.getBirdImage(pi.gliderHorizImage, pi, "GlideHoriz"),
                    x - (m * 35) - (mirror ? 63 : 0), y - 10, BotsnBoltsGame.DEPTH_PLAYER_FRONT, 0, 0, 64, 64, 0, mirror, false);
        } else if (glideAngle == GLIDE_DOWN) {
            renderer.render(layer, pi.gliderDownImage = Animal.getBirdImage(pi.gliderDownImage, pi, "GlideDown"),
                    x - (m * 35) - (mirror ? 63 : 0), y - 14, BotsnBoltsGame.DEPTH_PLAYER_FRONT_2, 0, 0, 64, 64, 0, mirror, false);
            if (exhaustIndex == 1) {
                renderer.render(layer, pi.exhaustDiag1, x - (m * 5) - (mirror ? 3 : 0), y + 27, BotsnBoltsGame.DEPTH_PLAYER_FRONT, 0, 0, 4, 4, 0, mirror, true);
            } else if (exhaustIndex == 2) {
                renderer.render(layer, pi.exhaustDiag2, x - (m * 7) - (mirror ? 7 : 0), y + 26, BotsnBoltsGame.DEPTH_PLAYER_FRONT, 0, 0, 8, 8, 0, mirror, true);
            }
        } else {
            renderer.render(layer, pi.gliderUpImage = Animal.getBirdImage(pi.gliderUpImage, pi, "GlideUp"),
                    x - (m * 34) - (mirror ? 63 : 0), y - 11, BotsnBoltsGame.DEPTH_PLAYER_BACK, 0, 0, 64, 64, 0, mirror, false);
            if (exhaustIndex == 1) {
                // pi.exhaustDiag1 // Blocked by Player
            } else if (exhaustIndex == 2) {
                renderer.render(layer, pi.exhaustDiag2, x - (m * 7) - (mirror ? 7 : 0), y + 5, BotsnBoltsGame.DEPTH_PLAYER_BACK_2, 0, 0, 8, 8, 0, mirror, false);
            }
        }
        renderViewNormal(renderer);
    }
    
    protected final void renderViewMech(final Panderer renderer) {
        final Panlayer layer = getLayer();
        final Panple pos = getPosition();
        final float x = pos.getX(), y = pos.getY();
        final boolean mirror = isMirror();
        final int m = getMirrorMultiplier(mirror);
        final int walkOffY = mechWalking && ((mechIndex == 0) || (mechIndex == 2)) ? -2 : 0;
        renderer.render(layer, pi.basicSet.stand, x, y + MECH_DIFF + walkOffY, pos.getZ(), 0, mirror, false);
        renderer.render(layer, mechCurrentImage, x - (22 * m), y - 1, getDepthFront(), 0, mirror, false);
    }
    
    protected int getDepthFront() {
        return BotsnBoltsGame.DEPTH_PLAYER_FRONT;
    }
    
    protected final void setHidden(final boolean hidden) {
        this.hidden = hidden;
        if (hidden) {
            setVisible(false);
        }
    }
    
    private final void updateVisibility() {
        if (hidden) {
            setVisible(false);
        } else if (isInvincible(false)) {
            setVisible(Pangine.getEngine().isOn(4));
        } else {
            setVisible(true);
        }
    }
    
    protected final void stop() {
        stopped = true;
    }
    
    protected final void resume() {
        stopped = false;
    }
    
    protected final boolean isStopped() {
        return stopped;
    }
    
    @Override
    protected final boolean onStepCustom() {
        if (stopped) {
            return true;
        }
        updateVisibility();
        if (RoomChanger.isChanging()) {
            final RoomChanger changer = RoomChanger.getActiveChanger();
            if (changer.getAge() <= 28) {
                hv = getDirection(changer.getVelocityX());
                v = getDirection(changer.getVelocityY());
            } else {
                clearDash();
                hv = 0;
                v = 0;
            }
        } else if (bossDoorStatus == 1) {
            hv = 0;
            v = 0;
        } else if (bossDoorStatus == 2) {
            hv = VEL_WALK;
            v = 0;
        } else if (bossDoorStatus == 3) {
            hv = -VEL_WALK;
            v = 0;
        }
        if (!active) {
            return false;
        }
        prevUnderwater = splashIfNeeded(this, prevUnderwater, this);
        if (stamina < HudMeter.MAX_VALUE) {
            final long clock = getClock();
            if ((clock - lastStamina) >= 30) {
                lastStamina = clock;
                stamina++;
            }
        }
        final boolean ret = onStepState();
        queuedX = 0;
        return ret;
    }
    
    protected final static boolean splashIfNeeded(final Panctor src, final boolean prevUnderwater, final Player player) {
        if (RoomLoader.waterLevel > 0) {
            final float y = src.getPosition().getY();
            final boolean underwater = y < RoomLoader.waterLevel;
            if (underwater != prevUnderwater) {
                splash(src);
            }
            if ((player != null) && underwater && ((y + 48) < RoomLoader.waterLevel)) {
                player.onStepUnderwater();
            }
            return underwater;
        }
        return false;
    }
    
    private final boolean onStepState() {
        if (ai != null) {
            ai.onStep(this);
        } else if (!stateHandler.isLadderPossible()) {
            Menu.hideUpDown();
        }
        if (stateHandler.onStep(this)) {
            return true;
        }
        return false;
    }
    
    private final boolean onStepNormal() {
        if (isAboveLadder()) {
            Menu.showUpDown();
        } else if (isTouchingLadder()) {
            Menu.showUpDown();
            if (prf.autoClimb && !isHurt() && !isFrozen() && ((v > 0) || ((ladderColumn != getColumn()) && !isGrounded()))) {
                startLadder();
                return true;
            }
        } else {
            Menu.hideUpDown();
        }
        prf.shootMode.onStep(this);
        addQueuedX();
        return false;
    }
    
    private final void addQueuedX() {
        if (queuedX != 0) {
            addX(queuedX, false, false); // queuedX used by Conveyor belt, which shouldn't change mirror
        }
    }
    
    private final static void splash(final Panctor src) {
        final Burst burst = new Burst(BotsnBoltsGame.splash);
        burst.getPosition().set(src.getPosition().getX(), RoomLoader.waterLevel, BotsnBoltsGame.DEPTH_CARRIER);
        addActor(src, burst);
    }
    
    private final void onStepUnderwater() {
        final long clock = getClock();
        if ((clock > (lastBubble + BUBBLE_TIME))) {
            new Bubble(this, 32);
            lastBubble = clock;
        }
    }
    
    private final boolean checkGrapplingFinished() {
        if (grapplingR <= 0) {
            endGrapple();
            return true;
        }
        return false;
    }
    
    protected final void onStepGrappling() {
        if (checkGrapplingFinished()) {
            return;
        }
        final double grapplingA = -getG() * Math.sin(grapplingT) / grapplingR;
        final double oldV = grapplingV;
        grapplingV += grapplingA;
        if (grapplingV == 0 || (grapplingV * oldV) < 0) {
            grapplingBoostAllowed = true;
        }
        grapplingT += grapplingV;
        final float oldX = getPosition().getX();
        final Panple gPos = grapplingHook.getPosition();
        final double offT = grapplingT + (Math.PI / 2);
        final double grapplingX = gPos.getX() + (Math.cos(offT) * grapplingR);
        final double grapplingY = gPos.getY() + (Math.sin(offT) * grapplingR);
        final int yStatus = moveTo((int) Math.round(grapplingX), (int) Math.round(grapplingY));
        switch (yStatus) {
            case Y_BUMP :
            case Y_CEILING :
                grapplingV = 0;
                break;
            case Y_WALL :
                if (grapplingX < oldX) {
                    addX(1);
                } else if (grapplingX > oldX) {
                    addX(-1);
                }
                grapplingAllowed = false;
                // Fall through
            case Y_LANDED :
            case Y_FLOOR :
                endGrapple();
                return;
        }
        final double baseT = grapplingT - Math.PI, magT = Math.abs(baseT);
        if (baseT > GRAPPLING_ANGLE_MIRROR_THRESHOLD) {
            if (!isMirror()) {
                grapplingAllowed = true;
            }
            setMirror(true);
        } else if (baseT < -GRAPPLING_ANGLE_MIRROR_THRESHOLD) {
            if (isMirror()) {
                grapplingAllowed = true;
            }
            setMirror(false);
        }
        if (magT < GRAPPLING_ANGLE_MAX_UP) {
            changeView(pi.jumpAimUp);
        } else if (magT < GRAPPLING_ANGLE_MAX_DIAG) {
            changeView(pi.jumpAimDiag);
        } else {
            changeView(pi.shootSet.jump);
        }
    }
    
    protected final boolean onSpring() {
        if (v >= 0) {
            return false;
        } else if (isGrounded()) {
            return false;
        }
        v = VEL_SPRING;
        return true;
    }
    
    private final boolean isLiftAllowed() {
        final long clock = getClock();
        if (clock > lastLift) {
            lastLift = clock;
            return true;
        }
        return false;
    }
    
    @Override
    protected final void onCollide(final int index) {
        final byte b = getBehavior(index);
        switch (b) {
            case BotsnBoltsGame.TILE_LIFT :
                if (isLiftAllowed()) {
                    addV(-1.5f * getG());
                }
                break;
            case BotsnBoltsGame.TILE_DEFEAT :
                if (prf.infiniteHealth) {
                    v = VEL_FALL_PROTECTION;
                } else {
                    defeat();
                }
                break;
            case BotsnBoltsGame.TILE_HURT :
                if (!isCollisionStandingOnTile(index)) {
                    hurt(BlockPuzzle.getDamageSpike());
                }
                break;
            case BotsnBoltsGame.TILE_CRUMBLE :
                if (isCollisionStandingOnTile(index)) {
                    BlockPuzzle.crumble(index);
                }
                break;
            case BotsnBoltsGame.TILE_CONVEYOR_LEFT :
                if (isCollisionStandingOnTile(index)) {
                    queuedX = -2;
                }
                break;
            case BotsnBoltsGame.TILE_CONVEYOR_RIGHT :
                if (isCollisionStandingOnTile(index)) {
                    queuedX = 2;
                }
                break;
            case BotsnBoltsGame.TILE_PRESSURE_FIRE :
                if (isCollisionStandingOnTile(index)) {
                    BlockPuzzle.FirePressureBlock.activate(index);
                }
                break;
            case BotsnBoltsGame.TILE_ACTIVATE :
                RoomLoader.activate();
                break;
            case BotsnBoltsGame.TILE_TRACTOR_BEAM :
                if (isLiftAllowed() && !isInvincible()) {
                    v = 0;
                    getPosition().addY(4);
                }
                break;
        }
    }
    
    private final static byte getBehavior(final int index) {
        return Tile.getBehavior(BotsnBoltsGame.tm.getTile(index));
    }
    
    @Override
    protected final int initCurrentHorizontalVelocity() {
        return stateHandler.initCurrentHorizontalVelocity(this);
    }
    
    private final int initCurrentHorizontalVelocityNormal() {
        sanded = false;
        final int thv;
        if (v == 0) {
            final Panple pos = getPosition();
            final float px = pos.getX(), py = pos.getY(), py1 = py + OFF_GROUNDED;
            final float pl = px + getOffLeft(), pr = px + getOffRight();
            final byte left = Tile.getBehavior(BotsnBoltsGame.tm.getTile(BotsnBoltsGame.tm.getContainer(pl, py)));
            final byte right = Tile.getBehavior(BotsnBoltsGame.tm.getTile(BotsnBoltsGame.tm.getContainer(pr, py)));
            final byte belowLeft = getBehavior(BotsnBoltsGame.tm.getContainer(pl, py1));
            final byte belowRight = getBehavior(BotsnBoltsGame.tm.getContainer(pr, py1));
            final boolean sand = left == TILE_SAND || right == TILE_SAND;
            final boolean belowSand = belowLeft == TILE_SAND || belowRight == TILE_SAND;
            if ((sand || belowSand) && (!prf.hazardProtection || "Y".equals(RoomLoader.variables.get("sandForced")))) {
                if (belowSand || !isAnySolidBehavior(belowLeft) || !isAnySolidBehavior(belowRight)) {
                    pos.addY(-1);
                }
                thv = initCurrentHorizontalVelocitySand();
                sanded = true;
                lastSanded = getClock();
            } else if ((belowLeft == TILE_ICE || belowRight == TILE_ICE) && !prf.hazardProtection) {
                thv = initCurrentHorizontalVelocityIce();
            } else if (hv != 0 && isGrounded()) {
                thv = initCurrentHorizontalVelocityAccelerating();
            } else {
                chv = hv;
                thv = hv;
            }
        } else {
            chv = hv;
            thv = hv;
        }
        return thv;
    }
    
    private final int initCurrentHorizontalVelocityGlider() {
        return Math.round(chv);
    }
    
    protected final float getMinX() {
        return minX;
    }
    
    protected final void setMinX(final float minX) {
        this.minX = minX;
    }
    
    protected final float getMaxX() {
        return maxX;
    }
    
    protected final void setMaxX(final float maxX) {
        this.maxX = maxX;
    }
    
    @Override
    public final void onStepEnd(final StepEndEvent event) {
        if (carrier != null) {
            carrier.onStepCarried(this);
        }
        if (isDashing()) {
            if (hvForced > 0) {
                hvForced -= DASH_SLOWDOWN;
            } else {
                hvForced += DASH_SLOWDOWN;
            }
            hv = Math.round(hvForced);
        } else {
            hvForced = 0.0f;
            hv = 0;
        }
        final Panple pos = getPosition();
        final float x = pos.getX();
        if (x < minX) {
            pos.setX(minX);
        } else if (x > maxX) {
            pos.setX(maxX);
        }
        updateWrapper();
        updateFollowers();
        stateHandler.onStepEnd(this);
        prf.shootMode.onStepEnd(this);
        if ((prf.shootMode != SHOOT_SHIELD) && isPassiveShieldEnabled()) {
            SHOOT_SHIELD.onStepEnd(this);
        }
    }
    
    private final void updateWrapper() {
        if (wrapper != null) {
            final Panple pos = getPosition();
            wrapper.getPosition().set(pos.getX(), pos.getY());
            wrapper.setMirror(isMirror());
        }
    }
    
    private final void updateFollowers() {
        if (Coltil.isValued(followers)) {
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY();
            final boolean mirror = isMirror();
            final Iterator<Follower> iter = followers.iterator();
            while (iter.hasNext()) {
                final Follower follower = iter.next();
                if (follower.isDestroyed()) {
                    iter.remove();
                    continue;
                }
                follower.setMirror(mirror); // Allow getOffsetX() to use new mirror value if needed
                follower.getPosition().set(x + follower.getOffsetX(), y + follower.getOffsetY());
            }
        }
    }
    
    private final void addFollower(final Follower follower) {
        followers = Coltil.add(followers, follower);
    }
    
    @Override
    protected final void onGrounded() {
        if (onHurting()) {
            return;
        }
        movedDuringJump = false;
        this.stateHandler.onGrounded(this);
        grapplingAllowed = true;
        jumpStartedOnCarrier = null;
        walkedOffCarrier = null;
        air = false;
        jumping = false;
        anyGlidingDuringThisJump = false;
    }
    
    private final void onGroundedNormal() {
        ladderColumn = -1;
        final PlayerImagesSubSet set = getCurrentImagesSubSet();
        if (hv == 0) {
            wallTimer = 0;
            final Panmage stand;
            if (set.blink == null) {
                stand = set.stand;
                blinkTimer = 0;
            } else {
                blinkTimer++;
                if (blinkTimer > 120) {
                    blinkTimer = 0;
                }
                stand = (blinkTimer > 115) ? set.blink : set.stand;
            }
            changeView(stand);
            clearRun();
            running = false;
        } else {
            blinkTimer = 0;
            if (wallTimer > 0 && set.crouch != null && !RoomChanger.isChanging() && wallMirror == isMirror() && isRoomForBall()) {
                final boolean ballAvailable = isBallAvailable();
                if (isSlideAvailable() && (!ballAvailable || isDashing())) {
                    startSlide();
                    return;
                } else if (ballAvailable) {
                    wallTimer++;
                    if (wallTimer > 6) {
                        startBall();
                    } else if (wallTimer > 3) {
                        changeView(set.crouch[1]);
                    } else {
                        changeView(set.crouch[0]);
                    }
                    return;
                }
            }
            wallTimer = 0;
            if (isDashing()) {
                slidePuff(-16, 5);
                changeView(set.dash);
                return;
            }
            final boolean wasRunning = running;
            running = true;
            if (!wasRunning && set.start != null) {
                changeView(set.start);
                return;
            }
            runTimer++;
            if (runTimer > RUN_TIME) {
                runTimer = 0;
                runIndex++;
                if (runIndex > 3) {
                    runIndex = 0;
                }
            }
            changeView(set.run[runIndex == 3 ? 1 : runIndex]);
        }
    }
    
    private final boolean isCrouching() {
        final Panmage[] crouch = pi.basicSet.crouch;
        final Pansplay display = getCurrentDisplay();
        return (display == crouch[0]) || (display == crouch[1]);
    }
    
    private final void onGroundedBall() {
        if (isCrouching()) {
            return;
        }
        changeView(pi.ball[runIndex]);
        if (hv != 0) {
            if (runTimer < 1) {
                runTimer++;
            } else {
                runTimer = 0;
                if (runIndex < 7) {
                    runIndex++;
                } else {
                    runIndex = 0;
                }
            }
        }
    }
    
    private final boolean isRoomForBall() {
        final TileMap tm = BotsnBoltsGame.tm;
        final int currIndex = tm.getContainer(this);
        final int neighborIndex = tm.getNeighbor(currIndex, isMirror() ? Direction.West : Direction.East);
        final byte b = Tile.getBehavior(tm.getTile(neighborIndex));
        return !isAnySolidBehavior(b);
    }
    
    private final boolean isBallAvailable() {
        return isUpgradeAvailable(Profile.UPGRADE_BALL);
    }
    
    protected final void startBall() {
        if (!isBallAvailable()) {
            return;
        }
        clearRun();
        clearDash();
        clearStream();
        stateHandler = BALL_HANDLER;
        lastBall = getClock();
        final Panmage[] crouch = pi.basicSet.crouch;
        if (getCurrentDisplay() == crouch[1]) {
            setView(pi.ball[0]);
        } else {
            changeView(crouch[0]);
        }
        setH(BALL_H);
        wallTimer = 0;
    }
    
    protected final void endBall() {
        // Also used by endSlide
        clearRun();
        stateHandler = NORMAL_HANDLER;
        setH(PLAYER_H);
    }
    
    private final boolean isSlideAvailable() {
        return isUpgradeAvailable(Profile.UPGRADE_SLIDE);
    }
    
    protected final void startSlide() {
        if (!isSlideAvailable()) {
            return;
        }
        clearRun();
        slideTimer = 0;
        stateHandler = SLIDE_HANDLER;
        lastBall = getClock();
        changeView(pi.slide);
        setH(BALL_H);
        wallTimer = 0;
        slideKick = new SlideKick(this);
        addActor(slideKick);
        addFollower(slideKick);
    }
    
    protected final void slidePuff(final int offX, final int offY) {
        slideTimer++;
        if (slideTimer == 12) {
            puffStill(offX, offY);
            slideTimer = 0;
        }
    }
    
    protected final void endSlide() {
        endBall();
        Panctor.destroy(slideKick); // updateFollowers will remove from followers List
    }
    
    private final boolean isWallToGrab() {
        final TileMap tm = BotsnBoltsGame.tm;
        final Panple pos = getPosition();
        final float x = pos.getX() + (isMirror() ? -16 : 16), y = pos.getY();
        return isAnySolidBehavior(Tile.getBehavior(tm.getTile(tm.getContainer(x, y))))
                && isAnySolidBehavior(Tile.getBehavior(tm.getTile(tm.getContainer(x, y + 11.0f))))
                && isAnySolidBehavior(Tile.getBehavior(tm.getTile(tm.getContainer(x, y + 23.0f))));
    }
    
    private final boolean isReadyToGrabWall() {
        return v <= 0.0f;
    }
    
    private final boolean isWallGrabAvailable() {
        return isUpgradeAvailable(Profile.UPGRADE_WALL_GRAB);
    }
    
    protected final boolean startWallGrab() {
        if (!isWallGrabAvailable()) {
            return false;
        }
        forceWallGrab();
        return true;
    }
    
    protected final void forceWallGrab() {
        clearRun();
        v = 0.0f;
        slideTimer = 0;
        stateHandler = WALL_GRAB_HANDLER;
        resetStream();
        changeView(pi.basicSet.wallGrab);
    }
    
    protected final boolean startWallGrabIfPossible() {
        if (isWallGrabAvailable() && !isGrounded() && isWallToGrab()) {
            return startWallGrab();
        }
        return false;
    }
    
    protected float getWallGrabGravity() {
        return isUnderWater() ? gWallSlideWater : gWallSlide;
    }
    
    protected final void endWallGrab() {
        setMirror(-getMirrorMultiplier(), false);
        setView(getCurrentImagesSubSet().stand);
        stateHandler = NORMAL_HANDLER;
    }
    
    protected final void clearDash() {
        lastRightStart = NULL_CLOCK;
        lastLeftStart = NULL_CLOCK;
        hvForced = 0;
    }
    
    private final boolean isDashAvailable() {
        return isUpgradeAvailable(Profile.UPGRADE_DASH);
    }
    
    protected final long startDashIfNeeded(final long lastStart, final int dir) {
        if (!isGrounded()) {
            return NULL_CLOCK;
        } else if (!stateHandler.isDashAvailable(this)) {
            return NULL_CLOCK;
        }
        final long clock = getClock();
        if (((clock - lastStart) < 8) && useStamina(1)) {
            slideTimer = 0;
            hvForced = (dir * VEL_DASH);
            hv = Math.round(hvForced);
            return NULL_CLOCK;
        } else {
            return clock;
        }
    }
    
    protected final boolean isDashing() {
        return Math.abs(hvForced) > VEL_WALK;
    }
    
    protected final boolean isPassiveShieldEnabled() {
        return false; //TODO
    }
    
    protected final boolean isShieldEnabled() {
        return (prf.shootMode == SHOOT_SHIELD) || isPassiveShieldEnabled();
    }
    
    protected final boolean isBlocking(final EnemyProjectile prj) {
        if (!isShieldEnabled()) {
            return false;
        } else if (!isInBlockingPose()) {
            return false;
        }
        final float prjVelX = prj.getVelocity().getX(), prjX = prj.getPosition().getX(), x = getPosition().getX();
        if ((prjVelX < 0) && !getAimMirror() && (prjX > x)) {
            return true;
        } else if ((prjVelX > 0) && getAimMirror() && (prjX < x)) {
            return true;
        }
        return false;
    }
    
    private final boolean isInBlockingPose() {
        return Panctor.isAttached(shield) && shield.isVisible() && (shield.getView() == pi.shieldVert) && (shield.getPosition().getZ() > BotsnBoltsGame.DEPTH_PLAYER)
                && (shield.getRot() == 0);
    }
    
    private final void endLadder() {
        clearRun();
        stateHandler = NORMAL_HANDLER;
        ladderColumn = getColumn();
    }
    
    private final int getColumn() {
        return BotsnBoltsGame.tm.getContainerColumn(getPosition().getX());
    }
    
    private final void startGrapple() {
        //destroyGrapplingHook(); // Allows Player to float, constantly starting/stopping grapple
        if (grapplingHook != null) {
            return;
        } else if (!grapplingAllowed) {
            return;
        }
        clearDash();
        clearStream();
        grapplingHook = new GrapplingHook(this);
        v = Math.max(v, VEL_JUMP / 3);
        grapplingV = 0;
        grapplingBoostAllowed = true;
        grapplingRetractAllowed = false;
        grapplingAllowed = false;
        stateHandler = GRAPPLING_HANDLER;
    }
    
    protected final Panple getGrapplingPosition() {
        if (grapplingHook != null) { // Could have been destroyed during this step, and grapplingPosition could be used by rescue logic at this point
            grapplingPosition.set(getPosition());
            grapplingPosition.addY(GRAPPLING_OFF_Y);
        }
        return grapplingPosition;
    }
    
    protected final void onGrappleConnected() {
        if (grapplingHook == null) {
            return;
        }
        grapplingV = 0;
        final Panple pos = getPosition(), gPos = grapplingHook.getPosition();
        final Panple dir = Panple.subtract(pos, gPos);
        dir.set(dir.getY(), dir.getX());
        final double mag = dir.getMagnitude2();
        if (mag <= 0) {
            destroyGrapplingHook();
            return;
        }
        grapplingR = mag;
        dir.multiply((float) (1.0 / mag));
        grapplingT = Math.acos(dir.getX());
        if (gPos.getX() < pos.getX()) {
            //grapplingT = -grapplingT;
            grapplingT = (2 * Math.PI) - grapplingT;
        }
        //grapplingT = Math.PI / 2; // Player is straight left of hook
        //grapplingT = 3 * Math.PI / 4; // 2.355; Player is 45 degrees left of hook
        //grapplingT = Math.PI; // Player is straight below hook
        //grapplingT = 5 * Math.PI / 4; // 3.925; Player is 45 degrees right of hook
        //grapplingT = 3 * Math.PI / 2; // Player is straight right of hook
    }
    
    private final void grappleBoost(final int dir) {
        if (!grapplingBoostAllowed) {
            return;
        } else if (Math.abs(grapplingV) >= GRAPPLING_BOOST_MAX) {
            return;
        }
        final double magT = Math.abs(grapplingT - Math.PI);
        if ((dir < 0) && (grapplingV >= 0) && (grapplingV < GRAPPLING_BOOST) && (magT > GRAPPLING_BOOST)) {
            return;
        } else if ((dir > 0) && (grapplingV <= 0) && (grapplingV > -GRAPPLING_BOOST) && (magT > GRAPPLING_BOOST)) {
            return;
        }
        grapplingV += (dir * GRAPPLING_BOOST);
        if (grapplingV > GRAPPLING_BOOST_MAX) {
            grapplingV = GRAPPLING_BOOST_MAX;
        } else if (grapplingV < -GRAPPLING_BOOST_MAX) {
            grapplingV = -GRAPPLING_BOOST_MAX;
        }
        grapplingBoostAllowed = false;
    }
    
    private final void grappleRetract() {
        if (!grapplingRetractAllowed) {
            return;
        }
        grapplingR--;
        checkGrapplingFinished();
    }
    
    private final void endGrapplingState() {
        if (stateHandler == GRAPPLING_HANDLER) {
            stateHandler = NORMAL_HANDLER;
        }
    }
    
    protected final void endGrapple() {
        clearRun();
        endGrapplingState();
        if (!destroyGrapplingHook()) {
            return;
        } else if (v <= 0) {
            v = VEL_JUMP / 4;
        } else if (v < VEL_JUMP) {
            v = (v + (3 * VEL_JUMP)) / 4;
        }
        clearDash();
        hv = 0;
    }
    
    private final boolean isGrapplingHookConnected() {
        return (grapplingHook != null) && grapplingHook.finished;
    }
    
    private final boolean destroyGrapplingHook() {
        if (grapplingHook != null) {
            grapplingHook.destroy();
            grapplingHook = null;
            return true;
        }
        return false;
    }
    
    private final void startSpring() {
        if (!Panctor.isDestroyed(spring) && !spring.isVisible()) {
            // If spring is invisible, then it's still warping into place; let that finish before allowing another
            return;
        }
        destroySpring();
        spring = new Spring(this);
    }
    
    private final void destroySpring() {
        Panctor.destroy(spring);
    }
    
    private final void startBoard() {
        destroySpring();
        clearDash();
        clearStream();
        offY = BOARD_Y_OFF;
        boardSlope = SLOPE_NONE;
        lastBoardStart = getClock() + 1;
        stateHandler = BOARD_HANDLER;
    }
    
    private final long getBoardTime() {
        return getClock() - lastBoardStart;
    }
    
    private final long getBoardJumpTime() {
        return getClock() - lastBoardJump;
    }
    
    // Called when isGrounded()
    private final boolean isOnHorizontalRail() {
        final TileMap tm = BotsnBoltsGame.tm;
        final Panple pos = getPosition();
        final float x = pos.getX(), y = pos.getY() - 15;
        return (Tile.getBehavior(tm.getTile(tm.getContainer(x + getOffLeft(), y))) == BotsnBoltsGame.TILE_RAIL)
                || (Tile.getBehavior(tm.getTile(tm.getContainer(x + getOffRight(), y))) == BotsnBoltsGame.TILE_RAIL);
    }
    
    protected final void endBoard() {
        clearRun();
        offY = 0;
        lastBoardJump = NULL_CLOCK;
        setH(PLAYER_H);
        if (stateHandler == BOARD_HANDLER) {
            stateHandler = NORMAL_HANDLER;
        }
    }
    
    protected final void endBoardIfNeeded() {
        if (stateHandler == BOARD_HANDLER) {
            endBoard();
        }
    }
    
    private final boolean startGlider() {
        if (isUnderWater()) {
            return false;
        }
        //TODO Only allow if falling at least some high speed? Show player in glide/dive position once that speed is reached? Also allow if anyGlidingDuringThisJump?
        // Auto-start if falling fast enough when reach bottom of screen if glider jump mode enabled?
        destroySpring();
        clearDash();
        clearStream();
        prevV = 0;
        v = VEL_GLIDE_START;
        glideAngle = GLIDE_UP;
        pulledUpDuringThisGlide = false;
        anyGlidingDuringThisJump = true;
        stateHandler = GLIDER_HANDLER;
        lastGlideStart = getClock() + 1;
        return true;
    }
    
    private final void onGlidePullUp() {
        onGlidePullUp(false);
    }
    
    private final boolean onGlidePullUp(final boolean forced) {
        if (isMirror()) {
            //chv = Math.min(chv + GLIDE_DECELERATION, 0);
            chv += GLIDE_DECELERATION;
            if (chv > 0) {
                setMirror(false);
            }
        } else {
            //chv = Math.max(chv - GLIDE_DECELERATION, 0);
            chv -= GLIDE_DECELERATION;
            if (chv < 0) {
                setMirror(true);
            }
        }
        if (!forced && (v > -4.0f)) {
            if (v > 0) {
                addV(GLIDE_PULL_UP_ACCELERATION_BOOST);
            }
            return false;
        }
        prevV = 0;
        v = -v;
        if (v < GLIDE_BOOST_THRESHOLD) {
            v = Math.min(v * 1.2f, GLIDE_BOOST_THRESHOLD);
        }
        lastGlideDirectionChange = getClock() + 1;
        pulledUpDuringThisGlide = true;
        return true;
    }
    
    private final void onGlideDive() {
        if (isMirror()) {
            chv = Math.max(chv - GLIDE_ACCELERATION, -GLIDE_MAX_SPEED);
        } else {
            chv = Math.min(chv + GLIDE_ACCELERATION, GLIDE_MAX_SPEED);
        }
        if (!pulledUpDuringThisGlide) {
            addV(GLIDE_DIVE_INITIAL_ACCELERATION_BOOST);
            return;
        } else if (v < 0) {
            addV(GLIDE_DIVE_ACCELERATION_BOOST); // In addition to gravity applied elsewhere
            return;
        }
        v = VEL_GLIDE_DIVE;
        lastGlideDirectionChange = getClock();
    }
    
    protected final void endGlider() {
        if (stateHandler == GLIDER_HANDLER) {
            chv = 0;
            clearRun();
            lastShotPosed = NULL_CLOCK;
            stateHandler = NORMAL_HANDLER;
        }
    }
    
    protected final void startMech() {
        stateHandler = MECH_HANDLER;
        setOffX(MECH_X);
        setH(MECH_H);
        mechReceivingInput = false;
        mechWalking = false;
        mechCounter = -1;
        mechIndex = MECH_WALK_START_INDEX;
        mechCurrentImage = pi.mechBasicSet.mech;
    }
    
    protected Panmage getMechHitBox() {
        return BotsnBoltsGame.getMechHitBox();
    }
    
    private final void endMech() {
        stateHandler = NORMAL_HANDLER;
        setOffX(PLAYER_X);
        setH(PLAYER_H);
    }
    
    private final void startState(final StateHandler stateHandler) {
        destroyGrapplingHook();
        if (this.stateHandler == BALL_HANDLER) {
            endBall();
        } else if (this.stateHandler == BOARD_HANDLER) {
            endBoard();
        }
        endGlider();
        this.stateHandler = stateHandler;
    }
    
    protected final void startCarried(final Carrier carrier) {
        carrierX = NULL_COORD;
        carrierOff = getPosition().getX() - carrier.getPosition().getX();
        this.carrier = carrier;
        jumpStartedOnCarrier = null;
        walkedOffCarrier = null;
        startState(CARRIED_HANDLER);
    }
    
    protected final void endCarried() {
        stateHandler = NORMAL_HANDLER;
        carrier = null;
    }
    
    protected final void startWrapped(final Wrapper wrapper) {
        changeView(pi.hurt);
        this.wrapper = wrapper;
        updateWrapper();
        stateHandler = WRAPPED_HANDLER;
        wrappedJumps = 0;
    }
    
    private final void endWrapped() {
        stateHandler = NORMAL_HANDLER;
        lastFrozen = getClock() - FROZEN_TIME + 1;
        freeWrapper();
    }
    
    private final void freeWrapper() {
        if (wrapper == null) {
            return;
        }
        wrapper.endWrap(this);
        wrapper = null;
    }
    
    @Override
    protected void onLanded() {
        super.onLanded();
        blinkTimer = 0;
    }
    
    @Override
    protected final boolean onAir() {
        try {
            return stateHandler.onAir(this);
        } finally {
            air = true;
        }
    }
    
    private final boolean onAirNormal() {
        wallTimer = 0;
        clearRun();
        if (onHurting()) {
            return false;
        }
        changeView(stateHandler.getJumpView(this));
        if (!air) {
            BotsnBoltsGame.fxJump.startSound();
        }
        return false;
    }
    
    private final Panmage getJumpViewNormal() {
        return getCurrentImagesSubSet().jump;
    }
    
    @Override
    protected boolean onWall(final byte xResult) {
        return stateHandler.onWall(this, xResult);
    }
    
    protected final boolean triggerBossDoor() {
        final BossDoor bossDoor = RoomLoader.getBossDoorExit();
        if (bossDoor == null) {
            return false;
        } else if (bossDoor.isOpening()) {
            return false;
        }
        final Panple doorPos = bossDoor.getPosition(), pos = getPosition();
        final float doorX = doorPos.getX(), playerX = pos.getX();
        if (bossDoor.isLeftToRight()) {
            if (doorX < playerX) {
                return false;
            } else if ((doorX - playerX) > 12.0f) {
                return false;
            }
        } else {
            if (doorX > playerX) {
                return false;
            } else if ((playerX - doorX) > 28.0f) {
                return false;
            }
        }
        final float doorY = doorPos.getY(), playerY = pos.getY();
        if (playerY < doorY) {
            return false;
        } else if ((playerY + PLAYER_H) >= (doorY + 63)) {
            return false;
        }
        bossDoor.open();
        bossDoorStatus = 1;
        dematerializeOthers(null);
        return true;
    }
    
    protected final void onBossDoorOpened(final BossDoor door) {
        if (door.isLeftToRight()) {
            bossDoorStatus = 2;
        } else {
            bossDoorStatus = 3;
        }
    }
    
    @Override
    protected final void onStart() {
        changeRoom(-1, 0);
    }
    
    @Override
    protected final void onEnd() {
        changeRoom(1, 0);
    }
    
    @Override
    protected final void onCeiling() {
        if (stateHandler == LADDER_HANDLER) {
            changeRoom(0, 1);
        }
    }
    
    @Override
    protected final boolean isSolid(final int index, final boolean floor, final float left, final float right, final float y) {
        if (super.isSolid(index, floor, left, right, y)) {
            return true;
        } else if (!BotsnBoltsGame.tm.isBad(index) && isFallProtectionRow(BotsnBoltsGame.tm.getRow(index))) {
            return true;
        }
        return false;
    }
    
    private final boolean isFallProtectionRow(final int row) {
        return !isAdjacentRoomBelow() && prf.fallProtection && (row == 0);
    }
    
    private final boolean isOnFallProtectionRow() {
        if (!prf.fallProtection) {
            return false;
        }
        try {
            prf.fallProtection = false;
            if (getSolid(-1, false) != -1) {
                return false; // On normal solid
            }
        } finally {
            prf.fallProtection = true;
        }
        return isFallProtectionRow(BotsnBoltsGame.tm.getContainerRow(getPosition().getY() - 1));
    }
    
    @Override
    protected boolean onFell() {
        endGrapplingState();
        destroyGrapplingHook();
        endBoardIfNeeded();
        if (changeRoom(0, -1)) {
            v = 0;
            return true;
        } else if (stateHandler.preventFall(this)) {
            return true;
        } else if ((availableRescues > 0) && (safeX != NULL_COORD) && ((getClock() - lastSanded) > 7)) {
            final String rescueDisabled = RoomLoader.variables.get("rescueDisabled");
            if (!"Y".equals(rescueDisabled)) {
                if ("Start".equals(rescueDisabled)) {
                    safeX = startX;
                    safeY = startY;
                    safeMirror = startMirror;
                }
                availableRescues--;
                BotsnBoltsGame.fxHurt.startSound();
                startRescue();
                return true;
            }
        }
        defeat();
        return true;
    }
    
    protected final boolean preventFallNormal() {
        if (anyGlidingDuringThisJump && (prf.jumpMode == JUMP_GLIDER)) {
            return startGlider();
        }
        return false;
    }
    
    private final void startRescue() {
        clearDash();
        hv = 0;
        chv = 0;
        startState(RESCUED_HANDLER);
        new Rescue(this);
    }
    
    private final void initAvailableRescues() {
        availableRescues = prf.upgrades.contains(Profile.UPGRADE_RESCUE) ? 1 : 0;
    }
    
    protected final void startRescued(final Rescue rescue) {
        this.rescue = rescue;
        grapplingPosition.set(safeX, safeY);
        Panple.subtract(grapplingPosition, grapplingPosition, getPosition());
        grapplingPosition.setMagnitude2(Rescue.SPEED_FLAP);
        final float vx = grapplingPosition.getX();
        if (vx < 0) {
            setMirror(true);
        } else if (vx > 0) {
            setMirror(false);
        }
        rescue.setMirror(isMirror());
    }
    
    private final void onStepRescued() {
        changeView(pi.hurt);
        if (rescue == null) {
            return;
        }
        final Panple pos = getPosition();
        pos.add(grapplingPosition.getX(), grapplingPosition.getY());
        if (pos.getDistance2(safeX, safeY) <= Rescue.SPEED_FLAP) {
            final float oldX = pos.getX(), oldY = pos.getY();
            final boolean oldMirror = isMirror();
            setEndRescuedPosition();
            v = 0;
            if (isGrounded()) {
                endRescued();
            } else {
                pos.set(oldX, oldY);
                setMirror(oldMirror);
                safeX = startX;
                safeY = startY;
                safeMirror = startMirror;
                startRescued(rescue);
                rescue.onCarrying();
            }
        } else {
            rescue.onCarrying();
        }
    }
    
    private final void setEndRescuedPosition() {
        getPosition().set(safeX, safeY);
        setMirror(safeMirror);
    }
    
    private final void endRescued() {
        rescue.startExit();
        rescue = null;
        stateHandler = NORMAL_HANDLER;
        lastHurt = getClock();
    }
    
    private final void endStatesIncompatibleWithRoomChange() {
        endGrapple();
        endBoardIfNeeded();
        endGlider();
    }
    
    private final boolean changeRoom(final int dirX, final int dirY) {
        final BotRoomCell roomCell = RoomLoader.getAdjacentRoom(this, dirX, dirY);
        if (roomCell == null) {
            return false;
        }
        dematerializeOthers(null);
        lastShotByAnyPlayer = NULL_CLOCK;
        initAvailableRescues();
        endStatesIncompatibleWithRoomChange();
        safeX = safeY = NULL_COORD;
        final BotRoom room = roomCell.room;
        final int nextX = (roomCell.cell.x - room.x) * BotsnBoltsGame.GAME_W;
        final BoltDoor boltDoor = RoomLoader.boltDoor; // Save this before RoomLoader clears it
        RoomLoader.onChangeStarted();
        final List<Panlayer> layersToKeepBeneath = Coltil.singletonList(BotsnBoltsGame.bgLayer);
        final List<Panlayer> layersToKeepAbove = Arrays.asList(BotsnBoltsGame.hud);
        final List<Panctor> actorsToKeep = new ArrayList<Panctor>();
        final List<Panctor> actorsToDestroy = new ArrayList<Panctor>();
        actorsToKeep.add(this);
        actorsToKeep.add(BotsnBoltsGame.tm);
        if (BotsnBoltsGame.tracked instanceof PlayerMean) {
            actorsToKeep.add(BotsnBoltsGame.tracked);
        }
        if (!Panctor.isDestroyed(Boss.aiBoss) && (Boss.aiBoss.getLayer() == getLayer())) {
            Boss.aiBoss.getPosition().setY(getPosition().getY());
            Boss.aiBoss.v = 0;
            actorsToKeep.add(Boss.aiBoss);
        }
        Coltil.addIfValued(actorsToKeep, boltDoor); // Keep Player and old TileMap while scrolling
        actorsToDestroy.add(BotsnBoltsGame.tm);
        Coltil.addIfValued(actorsToDestroy, boltDoor); // Destroy old TileMap after scrolling
        for (final Panctor actor : getLayer().getActors()) {
            if ((actor instanceof Pantexture) || ((actor instanceof Extra) && ((Extra) actor).isVisibleWhileRoomChanging())) {
                actorsToKeep.add(actor);
                actorsToDestroy.add(actor);
            } else if (actor instanceof RoomChangeListener) {
                final int status = ((RoomChangeListener) actor).getRoomChangeStatus();
                if ((status & RoomChangeListener.ROOM_CHANGE_KEEP) != 0) {
                    actorsToKeep.add(actor);
                }
                if ((status & RoomChangeListener.ROOM_CHANGE_DESTROY) != 0) {
                    actorsToDestroy.add(actor);
                }
            }
        }
        final int velX = VEL_ROOM_CHANGE * dirX, velY = VEL_ROOM_CHANGE * dirY;
        new RoomChanger(nextX, 0, velX, velY, layersToKeepBeneath, layersToKeepAbove, actorsToKeep, actorsToDestroy) {
            @Override
            protected final Panroom createRoom() {
                return loadRoom(room);
            }
            
            @Override
            protected final void onFinished() {
                destroyTimgPrev();
                RoomLoader.onChangeFinished();
            }
        };
        bossDoorStatus = 0;
        return true;
    }
    
    private final void destroyTimgPrev() {
        if (BotsnBoltsGame.timgPrev != null) {
            BotsnBoltsGame.timgPrev.destroy();
            BotsnBoltsGame.timgPrev = null;
        }
    }
    
    protected final static Panroom loadRoom(final BotRoom room) {
        final RoomLoader loader = new ScriptRoomLoader();
        RoomLoader.setRoom(room);
        return loader.newRoom();
    }
    
    private final void normalizeY(final int offBefore, final int offAfter) {
        final Panple pos = getPosition();
        pos.setY(offAfter + (((((int) pos.getY()) + offBefore) / 16) * 16));
    }
    
    private final boolean isUpgradeAvailable(final Upgrade upgrade) {
        return (upgrade == null) || prf.isUpgradeAvailable(upgrade);
    }
    
    protected final HudMeter newHealthMeter() {
        healthMeter = new HudMeter(pi.hudMeterImages) {
            @Override protected final int getValue() {
                return health;
            }};
        return healthMeter;
    }
    
    protected final HudMeter newStaminaMeter() {
        staminaMeter = new HudMeter(BotsnBoltsGame.hudMeterBoss) {
            @Override protected final int getValue() {
                return stamina;
            }
            @Override protected final boolean isNeeded() {
                return prf.isAttackUpgradeAvailable() || prf.isJumpUpgradeAvailable();
            }};
        return staminaMeter.setSoundAlwaysRequired(false);
    }
    
    protected final Pantext newLifeCounter() {
        lifeCounter = new Pantext(Pantil.vmid(), BotsnBoltsGame.font, new CallSequence(new Callable<String>() {
            @Override public final String call() {
                final int livesRemaining = pc.lives - 1; // "lives" is the Player's current number of lives; display lives remaining after the current life
                return "X" + livesRemaining;
            }}));
        return lifeCounter;
    }
    
    protected abstract static class StateHandler {
        protected abstract void onJump(final Player player);
        
        //@OverrideMe
        protected void onAirJump(final Player player) {
        }
        
        //@OverrideMe
        protected void releaseJump(final Player player) {
            player.releaseJumpNormal();
        }
        
        //@OverrideMe
        protected boolean isFloorBehavior(final byte b) {
            return false;
        }
        
        //@OverrideMe
        protected boolean isUpslopeFloorBehavior(final byte b) {
            return false;
        }
        
        //@OverrideMe
        protected boolean isDownslopeFloorBehavior(final byte b) {
            return false;
        }
        
        protected float getG(final Player player) {
            return player.getGNormal();
        }
        
        protected abstract void onShootStart(final Player player);
        
        protected abstract void onShooting(final Player player);
        
        protected abstract void onShootEnd(final Player player);
        
        protected boolean isAimMirrorReversed(final Player player) {
            return false;
        }
        
        protected int getAimOffsetX(final Player player) {
            return player.getAimOffsetXNormal();
        }
        
        protected int getAimOffsetY(final Player player) {
            return Projectile.OFF_Y;
        }
        
        protected boolean isDashAvailable(final Player player) {
            return player.isDashAvailable();
        }
        
        //@OverrideMe
        protected void onRightStart(final Player player) {
        }
        
        protected abstract void onRight(final Player player);
        
        //@OverrideMe
        protected void onLeftStart(final Player player) {
        }
        
        protected abstract void onLeft(final Player player);
        
        //@OverrideMe
        protected void onUp(final Player player) {
        }
        
        //@OverrideMe
        protected void onDown(final Player player) {
        }
        
        //@OverrideMe
        protected void onHurt(final Player player) {
        }
        
        //@OverrideMe
        protected void renderView(final Player player, final Panderer renderer) {
            player.renderViewNormal(renderer);
        }
        
        //@OverrideMe
        protected boolean isPlayerRendered() {
            return true;
        }
        
        //@OverrideMe
        protected boolean onStep(final Player player) {
            return false;
        }
        
        //@OverrideMe
        protected void onStepEnd(final Player player) {
        }
        
        //@OverrideMe
        protected boolean isLadderPossible() {
            return false;
        }
        
        protected abstract void onGrounded(final Player player);
        
        protected abstract boolean onAir(final Player player);
        
        //@OverrideMe
        protected Panmage getJumpView(final Player player) {
            return player.getJumpViewNormal();
        }
        
        //@OverrideMe
        protected boolean onWall(final Player player, final byte xResult) {
            return true;
        }
        
        //@OverrideMe
        protected boolean preventFall(final Player player) {
            return false;
        }
        
        //@OverrideMe
        protected int initCurrentHorizontalVelocity(final Player player) {
            return player.initCurrentHorizontalVelocityNormal();
        }
        
        //@OverrideMe
        protected boolean isSafePositionAllowed(final Player player) {
            return true;
        }
    }
    
    protected final static StateHandler NORMAL_HANDLER = new StateHandler() {
        @Override
        protected final void onJump(final Player player) {
            player.onJumpNormal();
        }
        
        @Override
        protected final void onAirJump(final Player player) {
            player.prf.jumpMode.onAirJump(player);
        }
        
        @Override
        protected final void onShootStart(final Player player) {
            player.prf.shootMode.onShootStart(player);
        }
        
        @Override
        protected final void onShooting(final Player player) {
            player.prf.shootMode.onShooting(player);
        }
        
        @Override
        protected final void onShootEnd(final Player player) {
            player.prf.shootMode.onShootEnd(player);
        }
        
        @Override
        protected final void onRightStart(final Player player) {
            player.onRightStartNormal();
        }
        
        @Override
        protected final void onRight(final Player player) {
            player.onRightNormal();
        }
        
        @Override
        protected final void onLeftStart(final Player player) {
            player.onLeftStartNormal();
        }
        
        @Override
        protected final void onLeft(final Player player) {
            player.onLeftNormal();
        }
        
        @Override
        protected final void onUp(final Player player) {
            player.onUpNormal();
        }
        
        @Override
        protected final void onDown(final Player player) {
            player.onDownNormal();
        }
        
        @Override
        protected final boolean onStep(final Player player) {
            return player.onStepNormal();
        }
        
        @Override
        protected final boolean isLadderPossible() {
            return true;
        }
        
        @Override
        protected final void onGrounded(final Player player) {
            player.onGroundedNormal();
        }
        
        @Override
        protected final boolean onAir(final Player player) {
            return player.onAirNormal();
        }
        
        @Override
        protected final boolean onWall(final Player player, final byte xResult) {
            if (player.triggerBossDoor()) {
                return true;
            } else if (player.wallTimer == 0 && xResult == X_WALL) {
                if (player.isReadyToGrabWall() && !player.isTouchingLadder() && player.startWallGrabIfPossible()) {
                    return true;
                }
                player.wallTimer = 1;
                if (player.chv > 0) { // Player could be sliding opposite of direction Player is facing; movement direction is what matters
                    player.wallMirror = false;
                } else if (player.chv < 0) {
                    player.wallMirror = true;
                } else if (player.hv > 0) {
                    player.wallMirror = false;
                } else if (player.hv < 0) {
                    player.wallMirror = true;
                } else {
                    player.wallMirror = player.isMirror();
                }
            }
            return true;
        }
        
        @Override
        protected final boolean preventFall(final Player player) {
            return player.preventFallNormal();
        }
    };
    
    protected final static StateHandler LADDER_HANDLER = new StateHandler() {
        @Override
        protected final void onJump(final Player player) {
            player.endLadder();
        }
        
        @Override
        protected final void onShootStart(final Player player) {
            player.prf.shootMode.onShootStart(player);
        }
        
        @Override
        protected final void onShooting(final Player player) {
            player.prf.shootMode.onShooting(player);
        }
        
        @Override
        protected final void onShootEnd(final Player player) {
            player.prf.shootMode.onShootEnd(player);
        }
        
        @Override
        protected final void onRight(final Player player) {
            if (!player.isShootPoseNeeded()) {
                player.setMirror(false);
            }
        }
        
        @Override
        protected final void onLeft(final Player player) {
            if (!player.isShootPoseNeeded()) {
                player.setMirror(true);
            }
        }
        
        @Override
        protected final void onUp(final Player player) {
            if (!player.isShootPoseNeeded()) {
                player.v = VEL_WALK;
            }
        }
        
        @Override
        protected final void onDown(final Player player) {
            if (!player.isShootPoseNeeded()) {
                player.v = -VEL_WALK;
            }
        }
        
        @Override
        protected final void onHurt(final Player player) {
            player.stateHandler = NORMAL_HANDLER;
        }
        
        @Override
        protected final boolean isLadderPossible() {
            return true;
        }
        
        @Override
        protected final boolean onStep(final Player player) {
            player.ladderColumn = -1;
            final float v = player.v;
            Menu.showUpDown();
            player.prf.shootMode.onStep(player);
            if (v != 0) {
                final byte yStatus = player.addY();
                player.v = 0;
                if (yStatus == Y_LANDED) {
                    player.endLadder();
                } else if (!(player.isTouchingLadder() || player.isTouchingLadder(OFF_LADDER_BOTTOM))) {
                    if (v > 0) {
                        player.normalizeY(OFF_LADDER_BOTTOM, 0);
                    }
                    player.endLadder();
                    player.changeView(player.pi.basicSet.stand);
                    player.initMirror();
                    return true;
                }
                final int frameLength = VEL_WALK * RUN_TIME, animLength = frameLength * 2;
                player.setMirror((Math.round(player.getPosition().getY()) % animLength) < frameLength);
            }
            final Panmage view;
            if (player.isShootPoseNeeded()) {
                view = player.getCurrentAimImagesSubSet().climb;
            } else if (!(player.isTouchingLadder() || player.isTouchingLadder(OFF_LADDER_BOTTOM + VEL_WALK))) {
                view = player.pi.climbTop;
                player.normalizeY(0, 2);
            } else {
                view = player.pi.basicSet.climb;
            }
            player.changeView(view);
            return true;
        }
        
        @Override
        protected final void onGrounded(final Player player) {
        }
        
        @Override
        protected final boolean onAir(final Player player) {
            return false;
        }
    };
    
    protected final static StateHandler BALL_HANDLER = new StateHandler() {
        @Override
        protected final void onJump(final Player player) {
            player.onJumpNormal();
        }
        
        @Override
        protected final void onShootStart(final Player player) {
            if (!SHOOT_BOMB.isAvailable(player)) {
                return;
            }
            SHOOT_BOMB.onShootStart(player);
        }
        
        @Override
        protected final void onShooting(final Player player) {
        }
        
        @Override
        protected final void onShootEnd(final Player player) {
        }
        
        private final boolean isCrouching(final Player player) {
            return player.isCrouching() && player.isGrounded();
        }
        
        @Override
        protected final void onRight(final Player player) {
            if (isCrouching(player)) {
                return;
            }
            player.onRightNormal();
        }
        
        @Override
        protected final void onLeft(final Player player) {
            if (isCrouching(player)) {
                return;
            }
            player.onLeftNormal();
        }
        
        @Override
        protected final boolean onStep(final Player player) {
            final Pansplay currentDisplay = player.getCurrentDisplay();
            final PlayerImages pi = player.pi;
            final Panmage[] crouch = pi.basicSet.crouch;
            final long clock = getClock(), lastBall = player.lastBall;
            if ((currentDisplay == crouch[0]) && (clock > (lastBall + 3))) {
                player.setView(crouch[1]);
            } else if ((currentDisplay == crouch[1]) && (clock > (lastBall + 6))) {
                player.setView(pi.ball[0]);
            }
            player.addQueuedX();
            return false;
        }
        
        @Override
        protected final void onGrounded(final Player player) {
            player.onGroundedBall();
        }
        
        @Override
        protected final boolean onAir(final Player player) {
            if ((getClock() - player.lastJump) <= 1) {
                player.endBall();
            }
            return false;
        }
    };
    
    protected final static StateHandler SLIDE_HANDLER = new StateHandler() {
        @Override
        protected final void onJump(final Player player) {
            player.onJumpNormal();
        }
        
        @Override
        protected final void onShootStart(final Player player) {
        }
        
        @Override
        protected final void onShooting(final Player player) {
        }
        
        @Override
        protected final void onShootEnd(final Player player) {
        }
        
        @Override
        protected final void onRight(final Player player) {
            player.setMirror(VEL_SLIDE);
        }
        
        @Override
        protected final void onLeft(final Player player) {
            player.setMirror(-VEL_SLIDE);
        }
        
        private final boolean isRoomToStand(final Player player) {
            /*
            try {
                player.setH(PLAYER_H);
                return player.isJumpPossible();
            } finally {
                player.setH(BALL_H);
            }
            */
            final Panple pos = player.getPosition();
            final float x = pos.getX(), y = pos.getY() + PLAYER_H;
            final TileMap tm = BotsnBoltsGame.tm;
            if (Chr.isAnySolidBehavior(Tile.getBehavior(tm.getTile(tm.getContainer(x + player.getOffLeft(), y))))) {
                return false;
            } else if (Chr.isAnySolidBehavior(Tile.getBehavior(tm.getTile(tm.getContainer(x + player.getOffRight(), y))))) {
                return false;
            }
            return true;
        }
        
        @Override
        protected final boolean onStep(final Player player) {
            if ((getClock() > (player.lastBall + 1)) && isRoomToStand(player)) {
                player.endSlide();
                return false;
            }
            player.hv = player.isMirror() ? -VEL_SLIDE : VEL_SLIDE;
            player.slidePuff(-8, 5);
            return false;
        }
        
        @Override
        protected final void onGrounded(final Player player) {
        }
        
        @Override
        protected final boolean onAir(final Player player) {
            return false;
        }
    };
    
    protected final static StateHandler WALL_GRAB_HANDLER = new StateHandler() {
        @Override
        protected final void onJump(final Player player) {
            player.endWallGrab();
            player.startJump(); // onJumpNormal checks grounded, not necessary here
        }
        
        @Override
        protected final float getG(final Player player) {
            return player.getWallGrabGravity();
        }
        
        @Override
        protected final void onShootStart(final Player player) {
            player.prf.shootMode.onShootStart(player);
        }
        
        @Override
        protected final void onShooting(final Player player) {
            player.prf.shootMode.onShooting(player);
        }
        
        @Override
        protected final void onShootEnd(final Player player) {
            player.prf.shootMode.onShootEnd(player);
        }
        
        @Override
        protected final boolean isAimMirrorReversed(final Player player) {
            return true;
        }
        
        @Override
        protected final int getAimOffsetY(final Player player) {
            return Projectile.OFF_Y - 4;
        }
        
        @Override
        protected final void onRight(final Player player) {
        }
        
        @Override
        protected final void onLeft(final Player player) {
        }
        
        @Override
        protected final void onHurt(final Player player) {
            player.endWallGrab();
        }
        
        @Override
        protected final boolean onStep(final Player player) {
            player.prf.shootMode.onStep(player);
            player.slidePuff(4, 24);
            player.changeView(player.getCurrentImagesSubSet().wallGrab);
            return false;
        }
        
        @Override
        protected final void onGrounded(final Player player) {
            player.endWallGrab();
        }
        
        @Override
        protected final boolean onAir(final Player player) {
            return false;
        }
        
        @Override
        protected final boolean preventFall(final Player player) {
            player.endWallGrab();
            return player.preventFallNormal();
        }
    };
    
    protected final static StateHandler CARRIED_HANDLER = new StateHandler() {
        @Override
        protected final void onJump(final Player player) {
            player.startJump(player.carrier);
            player.endCarried();
        }
        
        @Override
        protected final void onShootStart(final Player player) {
            player.prf.shootMode.onShootStart(player);
        }
        
        @Override
        protected final void onShooting(final Player player) {
            player.prf.shootMode.onShooting(player);
        }
        
        @Override
        protected final void onShootEnd(final Player player) {
            player.prf.shootMode.onShootEnd(player);
        }
        
        @Override
        protected final void onRight(final Player player) {
            player.onRightNormal();
        }
        
        @Override
        protected final void onLeft(final Player player) {
            player.onLeftNormal();
        }
        
        @Override
        protected final boolean onStep(final Player player) {
            // The Carrier moves the Player, so don't need to do that here
            player.ladderColumn = -1;
            return player.onStepNormal();
        }
        
        @Override
        protected final void onGrounded(final Player player) {
            player.onGroundedNormal();
        }
        
        @Override
        protected final boolean onAir(final Player player) {
            player.onGroundedNormal();
            return false;
        }
    };
    
    protected final static StateHandler RESCUED_HANDLER = new StateHandler() {
        @Override
        protected final void onJump(final Player player) {
        }

        @Override
        protected final void onShootStart(final Player player) {
        }

        @Override
        protected final void onShooting(final Player player) {
        }

        @Override
        protected final void onShootEnd(final Player player) {
        }

        @Override
        protected final void onRight(final Player player) {
        }

        @Override
        protected final void onLeft(final Player player) {
        }
        
        @Override
        protected final boolean onStep(final Player player) {
            player.onStepRescued();
            return true;
        }

        @Override
        protected final void onGrounded(final Player player) {
        }

        @Override
        protected final boolean onAir(final Player player) {
            return false;
        }
    };
    
    protected final static StateHandler GRAPPLING_HANDLER = new StateHandler() {
        @Override
        protected final void onJump(final Player player) {
            player.onJumpNormal();
        }
        
        @Override
        protected final void onAirJump(final Player player) {
            if (!isConnected(player)) {
                return; // If we don't return here, the Player can "float" by rapidly starting/stopping grapple
            } else if (!player.grapplingAllowed && (player.grapplingHook.hv != 0)) {
                return; // Don't let the Player end a grapple so soon that another grapple isn't allowed
            }
            player.endGrapple();
            BotsnBoltsGame.fxJump.startSound();
        }
        
        @Override
        protected final void onShootStart(final Player player) {
            player.grapplingRetractAllowed = true;
        }
        
        @Override
        protected final void onShooting(final Player player) {
            player.grappleRetract();
        }
        
        @Override
        protected final void onShootEnd(final Player player) {
        }
        
        @Override
        protected final void onRight(final Player player) {
            if (!isConnected(player)) {
                player.onRightNormal();
                return;
            }
            player.grappleBoost(1);
        }
        
        @Override
        protected final void onLeft(final Player player) {
            if (!isConnected(player)) {
                player.onLeftNormal();
                return;
            }
            player.grappleBoost(-1);
        }
        
        @Override
        protected final void onHurt(final Player player) {
            player.endGrapple();
        }
        
        private final boolean isConnected(final Player player) {
            return player.isGrapplingHookConnected();
        }
        
        @Override
        protected final boolean onStep(final Player player) {
            if (!isConnected(player)) {
                if (player.v < 0) {
                    player.v /= 2.0f;
                }
                return false;
            }
            player.onStepGrappling();
            return true;
        }
        
        @Override
        protected final void onGrounded(final Player player) {
            player.endGrapple();
            player.grapplingAllowed = true;
        }
        
        @Override
        protected final boolean onAir(final Player player) {
            return player.onAirNormal();
        }
        
        @Override
        protected final Panmage getJumpView(final Player player) {
            final GrapplingHook grapplingHook = player.grapplingHook;
            if (grapplingHook == null) {
                return super.getJumpView(player);
            } else if (grapplingHook.hv == 0) {
                return player.pi.jumpAimUp;
            }
            return player.pi.jumpAimDiag;
        }
    };
    
    protected final static StateHandler BOARD_HANDLER = new StateHandler() {
        @Override
        protected final void onJump(final Player player) {
            if (player.onJumpNormal(VEL_BOARD_JUMP)) {
                player.lastBoardJump = getClock() + 1;
            }
        }
        
        @Override
        protected final void onAirJump(final Player player) {
            if (player.jumping) { // Player intentionally jumped and pressed jump again to stop using the board
                player.endBoard();
            } else { // Player fell off edge, hasn't jumped yet, so allow the jump
                player.startJump(null, VEL_BOARD_JUMP);
            }
        }
        
        @Override
        protected final boolean isFloorBehavior(final byte b) {
            return b == BotsnBoltsGame.TILE_RAIL;
        }
        
        @Override
        protected final boolean isUpslopeFloorBehavior(final byte b) {
            return b == BotsnBoltsGame.TILE_UPSLOPE_RAIL;
        }
        
        @Override
        protected final boolean isDownslopeFloorBehavior(final byte b) {
            return b == BotsnBoltsGame.TILE_DOWNSLOPE_RAIL;
        }
        
        @Override
        protected final void onShootStart(final Player player) {
            player.prf.shootMode.onShootStart(player);
        }
        
        @Override
        protected final void onShooting(final Player player) {
            player.prf.shootMode.onShooting(player);
        }
        
        @Override
        protected final void onShootEnd(final Player player) {
            player.prf.shootMode.onShootEnd(player);
        }
        
        @Override
        protected final int getAimOffsetY(final Player player) {
            return Projectile.OFF_Y + BOARD_Y_OFF;
        }
        
        @Override
        protected final void onRight(final Player player) {
            // Nothing
        }
        
        @Override
        protected final void onLeft(final Player player) {
            // Nothing
        }
        
        @Override
        protected final void onUp(final Player player) {
            player.onUpNormal();
        }
        
        @Override
        protected final void renderView(final Player player, final Panderer renderer) {
            player.renderViewBoard(renderer);
        }
        
        @Override
        protected final boolean onStep(final Player player) {
            player.setHIfPossible(BOARD_H);
            player.moveHorizontal(player.getMirrorMultiplier() * VEL_BOARD);
            player.prf.shootMode.onStep(player);
            final PlayerImagesSubSet set;
            if (player.isShootPoseNeeded()) {
                set = player.getCurrentAimImagesSubSet();
            } else {
                set = player.pi.basicSet;
            }
            final boolean grounded = player.isGrounded();
            player.boardX = player.boardY = 0;
            final Panmage view;
            final int slope;
            if (player.getBoardTime() < BOARD_START_TIME) {
                slope = SLOPE_NONE;
            } else if (player.getBoardJumpTime() < KICKFLIP_FRAME3) {
                slope = SLOPE_UP;
            } else if (grounded) {
                slope = player.getMirrorMultiplier() * player.getCurrentSlope();
            } else {
                if (player.v > 2.75f) {
                    slope = SLOPE_UP;
                } else if (player.v < -2.75f) {
                    slope = SLOPE_DOWN;
                } else {
                    slope = SLOPE_NONE;
                    player.boardX = 3;
                    player.boardY = 2;
                }
            }
            if (slope == SLOPE_UP) {
                view = set.jump;
            } else if (slope == SLOPE_DOWN ) {
                view = set.descend;
            } else if (grounded) {
                if (player.isOnHorizontalRail()) {
                    view = set.jump;
                    player.boardX = 3;
                    player.boardY = 2;
                } else {
                    view = set.stand;
                }
            } else {
                view = set.jump;
            }
            player.boardSlope = slope;
            player.changeView(view);
            return false;
        }
        
        @Override
        protected final void onGrounded(final Player player) {
        }
        
        @Override
        protected final boolean onAir(final Player player) {
            return false;
        }
        
        @Override
        protected final boolean onWall(final Player player, final byte xResult) {
            player.endBoard();
            if (player.startWallGrabIfPossible()) {
                return true;
            } else if (player.isGrounded()) {
                return true;
            }
            player.v = Math.max(player.v, VEL_BOARD_BUMP);
            return true;
        }
    };
    
    protected final static StateHandler GLIDER_HANDLER = new StateHandler() {
        @Override
        protected final void onJump(final Player player) {
            player.endGlider();
        }
        
        @Override
        protected final void onAirJump(final Player player) {
            player.endGlider();
        }
        
        @Override
        protected final void releaseJump(final Player player) {
        }
        
        @Override
        protected float getG(final Player player) {
            return gGlide;
        }
        
        @Override
        protected final void onShootStart(final Player player) {
            final long clock = getClock();
            if (clock - player.lastShotFired > SHOOT_DELAY_DEFAULT) {
                final Panple pos = player.getPosition();
                new PlayerFallingBomb(player).getPosition().set(pos.getX() + (12 * player.getMirrorMultiplier()), pos.getY() + 8);
                player.afterShoot(clock);
                player.lastShotPosed = NULL_CLOCK;
            }
        }
        
        @Override
        protected final void onShooting(final Player player) {
        }
        
        @Override
        protected final void onShootEnd(final Player player) {
        }
        
        @Override
        protected final void onRight(final Player player) {
            if (player.isMirror()) {
                player.onGlidePullUp();
            } else {
                player.onGlideDive();
            }
        }
        
        @Override
        protected final void onLeft(final Player player) {
            if (player.isMirror()) {
                player.onGlideDive();
            } else {
                player.onGlidePullUp();
            }
        }
        
        @Override
        protected final void onUp(final Player player) {
            player.onUpNormal();
        }
        
        @Override
        protected final void renderView(final Player player, final Panderer renderer) {
            player.renderViewGlider(renderer);
        }
        
        @Override
        protected final boolean onStep(final Player player) {
            if (player.isUnderWater()) {
                player.endGlider();
                return false;
            } else if ((player.prevV > 0) && (player.v <= 0)) {
                player.lastGlideDirectionChange = getClock();
                if (player.isMirror()) {
                    if (player.chv > -1.25f) {
                        player.chv = -1.25f;
                    }
                } else {
                    if (player.chv < 1.25f) {
                        player.chv = 1.25f;
                    }
                }
            //} else if ((player.v > 0) && (player.prevV <= 0)) {
            //    player.lastGlideDirectionChange = getClock(); // Handled in onGlidePullUp, never happens automatically
            }
            player.prevV = player.v;
            if ((getClock() - player.lastGlideDirectionChange) < 3) {
                player.setView(player.pi.glideHoriz);
                player.glideAngle = GLIDE_HORIZONTAL;
            } else if (player.v < 0) {
                player.setView(player.pi.glideDown);
                player.glideAngle = GLIDE_DOWN;
            } else {
                player.setView(player.pi.glideUp);
                player.glideAngle = GLIDE_UP;
            }
            return false;
        }
        
        @Override
        protected final void onGrounded(final Player player) {
            player.endGlider();
        }
        
        @Override
        protected final boolean onAir(final Player player) {
            return false;
        }
        
        @Override
        protected final boolean onWall(final Player player, final byte xResult) {
            if (player.v > 0) {
                return false;
            } else if (player.startWallGrabIfPossible()) {
                player.endGlider();
                return true;
            }
            return false;
        }
        
        @Override
        protected final boolean preventFall(final Player player) {
            player.onGlidePullUp(true);
            player.pulledUpDuringThisGlide = false;
            return true;
        }
        
        @Override
        protected final int initCurrentHorizontalVelocity(final Player player) {
            return player.initCurrentHorizontalVelocityGlider();
        }
        
        @Override
        protected final boolean isSafePositionAllowed(final Player player) {
            return false;
        }
    };
    
    protected final static StateHandler MECH_HANDLER = new StateHandler() {
        @Override
        protected final void onJump(final Player player) {
            player.onJumpNormal(VEL_MECH_JUMP);
        }
        
        @Override
        protected final void onAirJump(final Player player) {
        }
        
        @Override
        protected final void releaseJump(final Player player) {
            player.releaseJumpNormal();
        }
        
        @Override
        protected final void onShootStart(final Player player) {
            Player.SHOOT_MECH.onShootStart(player);
        }
        
        @Override
        protected final void onShooting(final Player player) {
            Player.SHOOT_MECH.onShooting(player);
        }
        
        @Override
        protected final void onShootEnd(final Player player) {
            Player.SHOOT_MECH.onShootEnd(player);
        }
        
        @Override
        protected final int getAimOffsetX(final Player player) {
            return 30;
        }
        
        @Override
        protected final int getAimOffsetY(final Player player) {
            return 35;
        }
        
        @Override
        protected final boolean isDashAvailable(final Player player) {
            return true;
        }
        
        @Override
        protected final void onRightStart(final Player player) {
            player.onRightStartNormal();
        }
        
        @Override
        protected final void onRight(final Player player) {
            onMove(player, 1);
        }
        
        @Override
        protected final void onLeftStart(final Player player) {
            player.onLeftStartNormal();
        }
        
        @Override
        protected final void onLeft(final Player player) {
            onMove(player, -1);
        }
        
        private final void onMove(final Player player, final int dir) {
            player.mechReceivingInput = true;
            player.mechDir = dir;
            player.mechCounter = Math.max(0, player.mechCounter);
        }
        
        @Override
        protected final void onUp(final Player player) {
        }
        
        @Override
        protected final void renderView(final Player player, final Panderer renderer) {
            player.renderViewMech(renderer);
        }
        
        @Override
        protected final boolean onStep(final Player player) {
            final boolean dashing = player.isDashing();
            if (player.mechReceivingInput && !dashing) {
                player.setMirror(player.mechDir);
                player.mechReceivingInput = false;
                player.mechCounter++;
                player.addX(VEL_MECH * player.mechDir);
                if (player.mechCounter >= 5) {
                    player.mechCounter = 0;
                    player.mechIndex++;
                    //player.addX(21 * player.mechDir);
                    if (player.mechIndex >= 4) {
                        player.mechIndex = 0;
                    }
                }
            } else {
                player.mechCounter = -1;
                player.mechIndex = MECH_WALK_START_INDEX;
            }
            final MechImagesSubSet mi = player.isShootPoseNeeded() ? player.pi.mechAimSet : player.pi.mechBasicSet;
            player.mechWalking = false;
            if (dashing) {
                player.mechCurrentImage = mi.mechJump;
            } else if (!player.isGrounded()) {
                player.mechCurrentImage = (player.v > 0) ? mi.mechJump : mi.mechFall;
            } else {
                player.mechWalking = (player.mechCounter >= 0);
                player.mechCurrentImage = player.mechWalking ? mi.mechWalks[player.mechIndex] : mi.mech;
            }
            return false;
        }
        
        @Override
        protected final void onStepEnd(final Player player) {
            player.setView(player.getMechHitBox());
        }
        
        @Override
        protected final void onGrounded(final Player player) {
        }
        
        @Override
        protected final boolean onAir(final Player player) {
            return false;
        }
        
        @Override
        protected final boolean onWall(final Player player, final byte xResult) {
            return false;
        }
    };
    
    /*
    As Player enters level, will warp through ceiling.
    So don't use a Player object that could get stuck.
    Use a separate Warp object.
    Some parts of code might always expect to find a Player object.
    So this handler makes the Player invisible and immovable.
    When the Warp reaches the Player, it will destroy itself and change the handler.
    */
    protected final static StateHandler WARP_HANDLER = new StateHandler() {
        @Override
        protected final void onJump(final Player player) {
        }
        
        @Override
        protected float getG(final Player player) {
            return 0;
        }
        
        @Override
        protected final void onShootStart(final Player player) {
        }
        
        @Override
        protected final void onShooting(final Player player) {
        }
        
        @Override
        protected final void onShootEnd(final Player player) {
        }
        
        @Override
        protected final void onRight(final Player player) {
        }
        
        @Override
        protected final void onLeft(final Player player) {
        }
        
        @Override
        protected final void renderView(final Player player, final Panderer renderer) {
        }
        
        @Override
        protected final boolean isPlayerRendered() {
            return false;
        }
        
        @Override
        protected final void onGrounded(final Player player) {
        }
        
        @Override
        protected final boolean onAir(final Player player) {
            return player.onAirNormal();
        }
    };
    
    protected final static StateHandler WRAPPED_HANDLER = new StateHandler() {
        @Override
        protected final void onJump(final Player player) {
            player.wrappedJumps++;
            if (player.wrappedJumps >= 5) {
                player.endWrapped();
                player.onJumpNormal();
            }
        }

        @Override
        protected final void onShootStart(final Player player) {
        }

        @Override
        protected final void onShooting(final Player player) {
        }

        @Override
        protected final void onShootEnd(final Player player) {
        }

        @Override
        protected final void onRight(final Player player) {
        }

        @Override
        protected final void onLeft(final Player player) {
        }

        @Override
        protected final void onGrounded(final Player player) {
        }

        @Override
        protected final boolean onAir(final Player player) {
            return player.onAirNormal();
        }
    };
    
    protected abstract static class InputMode {
        private final Upgrade requiredUpgrade;
        
        protected InputMode(final Upgrade requiredUpgrade) {
            this.requiredUpgrade = requiredUpgrade;
        }
        
        protected final String getName() {
            return (requiredUpgrade == null) ? null : requiredUpgrade.name;
        }
        
        protected final Upgrade getRequiredUpgrade() {
            return requiredUpgrade;
        }
        
        protected final boolean isAvailable(final Player player) {
            return player.isUpgradeAvailable(getRequiredUpgrade());
        }
        
        //@OverrideMe
        protected boolean isCurrentlyAllowed(final Player player) {
            return true;
        }
        
        protected abstract int getRequiredStamina(final Player player);
    }
    
    protected abstract static class ShootMode extends InputMode {
        protected final int delay;
        
        protected ShootMode(final Upgrade requiredUpgrade, final int delay) {
            super(requiredUpgrade);
            this.delay = delay;
        }
        
        //@OverrideMe
        protected void onSelect(final Player player) {
        }
        
        //@OverrideMe
        protected void onDeselect(final Player player) {
        }
        
        protected abstract void onShootStart(final Player player);
        
        //@OverrideMe
        protected void onShooting(final Player player) {
        }
        
        //@OverrideMe
        protected void onStep(final Player player) {
        }
        
        //@OverrideMe
        protected void onStepEnd(final Player player) {
        }
        
        //@OverrideMe
        protected void onShootEnd(final Player player) {
        }
        
        protected final boolean shoot(final Player player) {
            final long clock = getClock();
            if (clock - player.lastShotFired > Math.max(delay, player.lastShotDelay)) {
                if (!player.useAttackStamina(getRequiredStamina(player))) {
                    SHOOT_NORMAL.shoot(player);
                    return false;
                };
                player.afterShoot(clock);
                createProjectile(player);
                player.currentShootSet = null;
                player.lastShotDelay = delay;
                if (isAllowedFallbackOption()) {
                    player.prf.lastUsedFallbackShootMode = this;
                }
                return true;
            }
            return false;
        }
        
        protected abstract void createProjectile(final Player player);
        
        protected final void createDefaultProjectile(final Player player) {
            createBasicProjectile(player, VEL_PROJECTILE, 0);
        }
        
        protected final void createBasicProjectile(final Player player, final float vx, final float vy) {
            player.newProjectile(vx, vy, 1);
        }
        
        protected final void shootSpecial(final Player player, final int power) {
            if (!player.useAttackStamina(power)) {
                SHOOT_NORMAL.shoot(player);
                return;
            }
            player.afterShoot(getClock());
            player.newProjectile(VEL_PROJECTILE, 0, power);
        }
        
        //@OverrideMe
        protected boolean isContinuous() {
            return false;
        }
        
        //@OverrideMe
        protected boolean isAllowedFallbackOption() {
            return true;
        }
        
        //@OverrideMe
        protected void onAssignPower(final SpecProjectile prj, final int power) {
        }
    }
    
    protected final static class PlayerContext {
        protected final int index;
        protected final Profile prf;
        protected ControlScheme ctrl;
        protected final PlayerImages pi;
        protected Player player = null;
        protected int lives = 5;
        private Player srcPlayer = null;
        
        protected PlayerContext(final int index, final Profile prf, final PlayerImages pi) {
            this.index = index;
            this.prf = prf;
            this.pi = pi;
        }
        
        protected final void setControlScheme(final ControlScheme ctrl) {
            this.ctrl = ctrl;
        }
        
        protected final int getHudX() {
            return index * 40;
        }
        
        protected final boolean isLifeCounterEmpty() {
            return lives <= 0;
        }
        
        protected final static Player getPlayer(final PlayerContext pc) {
            return (pc == null) ? null : pc.player;
        }
        
        protected final static Profile getProfile(final PlayerContext pc) {
            return (pc == null) ? null : pc.prf;
        }
    }
    
    protected final static class PlayerImages {
        protected final PlayerImagesSubSet basicSet;
        protected final PlayerImagesSubSet shootSet;
        protected final PlayerImagesSubSet throwSet;
        protected final PlayerImagesSubSet[] wieldSets;
        private final Panmage hurt;
        private final Panmage frozen;
        protected final Panimation defeat;
        private final Panmage climbTop;
        private final Panmage jumpAimDiag;
        private final Panmage jumpAimUp;
        private final Panmage glideUp;
        private final Panmage glideHoriz;
        private final Panmage glideDown;
        protected final Panmage talk;
        protected final Panmage basicProjectile;
        protected final Panimation projectile2;
        protected final Panimation projectile3;
        protected final Panimation charge;
        protected final Panimation chargeVert;
        protected final Panimation charge2;
        protected final Panimation chargeVert2;
        protected final Panmage[] plasma;
        protected final Panmage shieldVert;
        protected final Panmage shieldDiag;
        protected final Panmage shieldCircle;
        protected final Panmage swordHoriz;
        protected final Panmage swordDiag;
        protected final Panmage swordBack;
        protected final Panmage[] swordTrails;
        protected final Panmage exhaust1;
        protected final Panmage exhaust2;
        protected final Panmage exhaustDiag1;
        protected final Panmage exhaustDiag2;
        protected final Panimation burst;
        private final Panframe[] ball;
        protected final Panmage slide;
        protected final MechImagesSubSet mechBasicSet;
        protected final MechImagesSubSet mechAimSet;
        protected final Panmage warp;
        protected final Panimation materialize;
        protected final Panimation dematerialize;
        protected final Panimation bomb;
        protected final Panmage link;
        protected final Panimation batterySmall;
        protected final Panimation batteryMedium;
        protected final Panimation batteryBig;
        protected final Panmage doorBolt;
        protected final Panmage bolt;
        protected final Panmage disk;
        protected final Panmage powerBox;
        protected final Map<String, Panmage> boltBoxes; // Each bolt has a unique box image
        protected final Panmage diskBox;
        protected final Panmage highlightBox;
        protected final Panmage portrait;
        protected final HudMeterImages hudMeterImages;
        protected final String name;
        protected final String animalName;
        protected final String birdName;
        protected final MeleeMode meleeMode;
        protected Panmage boardImage = null;
        protected Panmage boardDiagImage = null;
        protected Panmage boardDiagImageTop = null;
        protected Panmage boardDiagImageBottom = null;
        protected Panmage gliderUpImage = null;
        protected Panmage gliderHorizImage = null;
        protected Panmage gliderDownImage = null;
        
        protected PlayerImages(final PlayerImagesSubSet basicSet, final PlayerImagesSubSet shootSet, final PlayerImagesSubSet throwSet,
                               final PlayerImagesSubSet[] wieldSets,
                               final Panmage hurt, final Panmage frozen, final Panimation defeat,
                               final Panmage climbTop,
                               final Panmage jumpAimDiag, final Panmage jumpAimUp,
                               final Panmage glideUp, final Panmage glideHoriz, final Panmage glideDown, final Panmage talk,
                               final Panmage basicProjectile, final Panimation projectile2, final Panimation projectile3,
                               final Panimation charge, final Panimation chargeVert, final Panimation charge2, final Panimation chargeVert2,
                               final Panmage[] plasma, Panmage shieldVert, Panmage shieldDiag, Panmage shieldCircle,
                               final Panmage swordHoriz, final Panmage swordDiag, final Panmage swordBack, final Panmage[] swordTrails,
                               final Panmage exhaust1, final Panmage exhaust2, final Panmage exhaustDiag1, final Panmage exhaustDiag2,
                               final Panimation burst, final Panframe[] ball, final Panmage slide,
                               final MechImagesSubSet mechBasicSet, final MechImagesSubSet mechAimSet,
                               final Panmage warp, final Panimation materialize, final Panimation bomb,
                               final Panmage link, final Panimation batterySmall, final Panimation batteryMedium, final Panimation batteryBig,
                               final Panmage doorBolt, final Panmage bolt, final Panmage disk,
                               final Panmage powerBox, final Map<String, Panmage> boltBoxes, final Panmage diskBox, final Panmage highlightBox,
                               final Panmage portrait, final HudMeterImages hudMeterImages,
                               final String name, final String animalName, final String birdName,
                               final MeleeMode meleeMode) {
            this.basicSet = basicSet;
            this.shootSet = shootSet;
            this.throwSet = throwSet;
            this.wieldSets = wieldSets;
            this.hurt = hurt;
            this.frozen = frozen;
            this.defeat = defeat;
            this.climbTop = climbTop;
            this.jumpAimDiag = jumpAimDiag;
            this.jumpAimUp = jumpAimUp;
            this.glideUp = glideUp;
            this.glideHoriz = glideHoriz;
            this.glideDown = glideDown;
            this.talk = talk;
            this.basicProjectile = basicProjectile;
            this.projectile2 = projectile2;
            this.projectile3 = projectile3;
            this.charge = charge;
            this.chargeVert = chargeVert;
            this.charge2 = charge2;
            this.chargeVert2 = chargeVert2;
            this.plasma = plasma;
            this.shieldVert = shieldVert;
            this.shieldDiag = shieldDiag;
            this.shieldCircle = shieldCircle;
            this.swordHoriz = swordHoriz;
            this.swordDiag = swordDiag;
            this.swordBack = swordBack;
            this.swordTrails = swordTrails;
            this.exhaust1 = exhaust1;
            this.exhaust2 = exhaust2;
            this.exhaustDiag1 = exhaustDiag1;
            this.exhaustDiag2 = exhaustDiag2;
            this.burst = burst;
            this.ball = ball;
            this.slide = slide;
            this.mechBasicSet = mechBasicSet;
            this.mechAimSet = mechAimSet;
            this.warp = warp;
            this.materialize = materialize;
            dematerialize = Pangine.getEngine().createReverseAnimation(materialize.getId() + ".reverse", materialize);
            this.bomb = bomb;
            this.link = link;
            this.batterySmall = batterySmall;
            this.batteryMedium = batteryMedium;
            this.batteryBig = batteryBig;
            this.doorBolt = doorBolt;
            this.bolt = bolt;
            this.disk = disk;
            this.powerBox = powerBox;
            this.boltBoxes = boltBoxes;
            this.diskBox = diskBox;
            this.highlightBox = highlightBox;
            this.portrait = portrait;
            this.hudMeterImages = hudMeterImages;
            this.name = name;
            this.animalName = animalName;
            this.birdName = birdName;
            this.meleeMode = meleeMode;
        }
    }
    
    protected final static class PlayerImagesSubSet {
        protected final Panmage stand;
        protected final Panmage jump;
        protected final Panmage[] run;
        protected final Panmage start;
        protected final Panmage blink;
        protected final Panmage[] crouch;
        protected final Panmage climb;
        protected final Panmage wallGrab;
        protected final Panmage dash;
        protected final Panmage descend;
        
        protected PlayerImagesSubSet(final Panmage stand, final Panmage jump, final Panmage[] run, final Panmage start, final Panmage blink, final Panmage[] crouch,
                final Panmage climb, final Panmage wallGrab, final Panmage dash, final Panmage descend) {
            this.stand = stand;
            this.jump = jump;
            this.run = run;
            this.start = start;
            this.blink = blink;
            this.crouch = crouch;
            this.climb = climb;
            this.wallGrab = wallGrab;
            this.dash = dash;
            this.descend = descend;
        }
    }
    
    protected final static class MechImagesSubSet {
        protected final Panmage mech;
        protected final Panmage mechJump;
        protected final Panmage mechFall;
        protected final Panmage[] mechWalks;
        
        protected MechImagesSubSet(final Panmage mech, final Panmage mechJump, final Panmage mechFall, final Panmage[] mechWalks) {
            this.mech = mech;
            this.mechJump = mechJump;
            this.mechFall = mechFall;
            this.mechWalks = mechWalks;
        }
    }
    
    protected final static ShootMode SHOOT_NORMAL = new ShootMode(null, SHOOT_DELAY_DEFAULT) {
        @Override
        protected final void onShootStart(final Player player) {
            shoot(player);
        }
        
        @Override
        protected final int getRequiredStamina(final Player player) {
            return 0;
        }
        
        @Override
        protected final void createProjectile(final Player player) {
            createDefaultProjectile(player);
        }
    };
    
    protected final static ShootMode SHOOT_RAPID = new ShootMode(Profile.UPGRADE_RAPID, SHOOT_DELAY_RAPID) {
        @Override
        protected final void onShootStart(final Player player) {
        }
        
        @Override
        protected final void onShooting(final Player player) {
            shoot(player);
        }
        
        @Override
        protected final int getRequiredStamina(final Player player) {
            return 1;
        }
        
        @Override
        protected final void createProjectile(final Player player) {
            createDefaultProjectile(player);
        }
        
        @Override
        protected final boolean isContinuous() {
            return true;
        }
    };
    
    protected final static ShootMode SHOOT_SPREAD = new ShootMode(Profile.UPGRADE_SPREAD, SHOOT_DELAY_SPREAD) {
        @Override
        protected final void onShootStart(final Player player) {
            shoot(player);
        }
        
        @Override
        protected final int getRequiredStamina(final Player player) {
            return 5;
        }
        
        @Override
        protected final void createProjectile(final Player player) {
            createDefaultProjectile(player);
            createBasicProjectile(player, VX_SPREAD1, VY_SPREAD1);
            createBasicProjectile(player, VX_SPREAD1, -VY_SPREAD1);
            createBasicProjectile(player, VX_SPREAD2, VY_SPREAD2);
            createBasicProjectile(player, VX_SPREAD2, -VY_SPREAD2);
        }
    };
    
    protected final static ShootMode SHOOT_CHARGE = new ShootMode(Profile.UPGRADE_CHARGE, SHOOT_DELAY_DEFAULT) {
        @Override
        protected final void onSelect(final Player player) {
            if (player.prf.autoCharge) {
                startCharge(player);
            }
        }
        
        @Override
        protected final void onShootStart(final Player player) {
            if (player.prf.autoCharge) {
                if (!shootChargedIfNeeded(player)) {
                    shoot(player);
                }
            } else {
                shoot(player);
            }
            startCharge(player);
        }
        
        private final void startCharge(final Player player) {
            final long clock = getClock();
            player.startCharge = clock;
            player.lastCharge = clock;
        }
        
        @Override
        protected final void onShooting(final Player player) {
            if (!player.prf.autoCharge) {
                onCharging(player);
            }
        }
        
        private final void onCharging(final Player player) {
            final long clock = getClock();
            if (clock - player.lastCharge > 2) {
                player.startCharge = clock;
            }
            player.lastCharge = clock;
            final long diff = clock - player.startCharge;
            if (diff > CHARGE_TIME_MEDIUM) {
                player.blinkTimer = 0;
                final PlayerImages pi = player.pi;
                final int ox = 0, oy = 12;
                if (diff > CHARGE_TIME_BIG) {
                    charge(player, ox, oy, pi.charge2, pi.chargeVert2, diff - CHARGE_TIME_BIG, BotsnBoltsGame.fxSuperCharge);
                } else {
                    charge(player, ox, oy, pi.charge, pi.chargeVert, diff - CHARGE_TIME_MEDIUM, BotsnBoltsGame.fxCharge);
                }
            }
        }
        
        @Override
        protected final void onStep(final Player player) {
            if (player.prf.autoCharge) {
                onCharging(player);
            }
        }
        
        @Override
        protected final void onShootEnd(final Player player) {
            if (!player.prf.autoCharge) {
                shootChargedIfNeeded(player);
            }
        }
        
        private final boolean shootChargedIfNeeded(final Player player) {
            final long diff = getClock() - player.startCharge;
            if ((diff > CHARGE_TIME_BIG) && (player.stamina >= Projectile.POWER_MAXIMUM)) {
                shootSpecial(player, Projectile.POWER_MAXIMUM);
                return true;
            } else if (diff > CHARGE_TIME_MEDIUM) {
                shootSpecial(player, Projectile.POWER_MEDIUM);
                return true;
            }
            return false;
        }
        
        @Override
        protected final int getRequiredStamina(final Player player) {
            return 0;
        }
        
        @Override
        protected final void createProjectile(final Player player) {
            createDefaultProjectile(player);
        }
        
        @Override
        protected final void onAssignPower(final SpecProjectile prj, final int power) {
            if (power > Projectile.POWER_MEDIUM) {
                prj.changeView(prj.getPlayerImages().projectile3);
            } else if (power > 1) {
                prj.changeView(prj.getPlayerImages().projectile2);
            } else {
                prj.changeView(prj.getPlayerImages().basicProjectile);
            }
        }
    };
    
    protected final static void charge(final Panctor player, final int ox, final int oy, final Panimation diag, final Panimation vert, final long i, final Pansound sound) {
        final long c = getClock() % 8;
        if (c == 0) {
            chargeDiag(player, ox, oy, diag, 1, 1, 0);
        } else if (c == 1) {
            chargeDiag(player, ox, oy, diag, -1, -1, 2);
        } else if (c == 2) {
            charge(player, ox, oy, vert, 1, -4, 4, 1, 8, 16, 0);
        } else if (c == 3) {
            charge(player, ox, oy, vert, -1, 8, 16, 1, -4, 4, 1);
        } else if (c == 4) {
            chargeDiag(player, ox, oy, diag, -1, 1, 1);
        } else if (c == 5) {
            chargeDiag(player, ox, oy, diag, 1, -1, 3);
        } else if (c == 6) {
            charge(player, ox, oy, vert, 1, -4, 4, -1, 8, 16, 2);
        } else {
            charge(player, ox, oy, vert, 1, 8, 16, 1, -4, 4, 3);
        }
        if ((i < 30) && ((i % 10) == 1)) {
            sound.startSound();
        }
    }
    
    private final static void chargeDiag(final Panctor player, final int ox, final int oy, final Panimation anm, final int xdir, final int ydir, final int rot) {
        charge(player, ox, oy, anm, xdir, 4, 12, ydir, 4, 12, rot);
    }
    
    private final static void charge(final Panctor player, final int ox, final int oy, final Panimation anm,
            final int xdir, final int xmin, final int xmax, final int ydir, final int ymin, final int ymax, final int rot) {
        final Burst burst = new Burst(anm);
        final Panple ppos = player.getPosition();
        final int oxm = ox * player.getMirrorMultiplier();
        burst.getPosition().set(ppos.getX() + oxm + (xdir * Mathtil.randi(xmin, xmax)), ppos.getY() + oy + (ydir * Mathtil.randi(ymin, ymax)), BotsnBoltsGame.DEPTH_BURST);
        burst.setRot(rot);
        addActor(player, burst);
    }
    
    protected final static ShootMode SHOOT_STREAM = new ShootMode(Profile.UPGRADE_STREAM, SHOOT_DELAY_STREAM) {
        @Override
        protected final void onDeselect(final Player player) {
            player.clearStream();
        }
        
        @Override
        protected final void onShootStart(final Player player) {
        }
        
        @Override
        protected final void onShooting(final Player player) {
            shoot(player);
        }
        
        @Override
        protected final void onStep(final Player player) {
            boolean lastWasAttached = true;
            for (int i = 0; i < STREAM_SIZE; i++) {
                final SpecStreamProjectile streamProjectile = player.streamProjectiles[i];
                final boolean attached = isAttached(streamProjectile);
                if (attached && !lastWasAttached) {
                    player.streamProjectiles[i].detach();
                }
                lastWasAttached = attached;
            }
            final SpecStreamProjectile second = player.streamProjectiles[1];
            if ((second != null) && (player.getAimMirror() != second.getSourceMirror())) {
                player.resetStream();
            }
            if ((player.lastShotFired + 1) < getClock()) {
                detach(player.streamProjectiles[0]);
            }
        }
        
        @Override
        protected final void onStepEnd(final Player player) {
            for (int i = STREAM_SIZE - 1; i >= 0; i--) {
                final SpecStreamProjectile streamProjectile = player.streamProjectiles[i];
                if (streamProjectile == null) {
                    continue;
                }
                final float y = (i > 0) ? player.streamProjectiles[i - 1].getPosition().getY() : NULL_COORD;
                streamProjectile.onStepEnd(y);
            }
        }
        
        @Override
        protected final int getRequiredStamina(final Player player) {
            if (player.stamina <= 0) {
                player.streamStaminaCounter = 0;
                return 1;
            }
            final int requiredStamina = (player.streamStaminaCounter == 0) ? 1 : 0;
            player.streamStaminaCounter++;
            if (player.streamStaminaCounter >= SHOOT_STAMINA_TIMER_STREAM) {
                player.streamStaminaCounter = 0;
            }
            return requiredStamina;
        }
        
        @Override
        protected final void createProjectile(final Player player) {
            for (int i = 0; i < STREAM_SIZE; i++) {
                SpecStreamProjectile streamProjectile = player.streamProjectiles[i];
                if (isDestroyed(streamProjectile)) {
                    final int ox;
                    final Panmage image;
                    switch (i) {
                        case 0 :
                            ox = 0;
                            image = player.pi.plasma[0];
                            break;
                        case 1 :
                            ox = 8;
                            image = player.pi.plasma[0];
                            break;
                        case 2 :
                            ox = 16;
                            image = player.pi.plasma[1];
                            break;
                        case 3 :
                            ox = 28;
                            image = player.pi.plasma[1];
                            break;
                        case 4 :
                            ox = 40;
                            image = player.pi.plasma[2];
                            break;
                        case 5 :
                            ox = 56;
                            image = player.pi.plasma[2];
                            break;
                        case 6 :
                            ox = 72;
                            image = player.pi.plasma[3];
                            break;
                        case 7 :
                            ox = 88;
                            image = player.pi.plasma[3];
                            break;
                        default :
                            throw new IllegalStateException("Maximum stream size exceeded");
                    }
                    streamProjectile = player.newStreamProjectile(ox);
                    streamProjectile.setView(image);
                    player.streamProjectiles[i] = streamProjectile;
                    streamProjectile.initSourceMirror();
                    break;
                } else if (streamProjectile.getLayer() == null) {
                    player.addActor((Panctor) streamProjectile);
                    streamProjectile.initSourceMirror();
                    break;
                }
            }
        }
        
        @Override
        protected final boolean isContinuous() {
            return true;
        }
    };
    
    protected final static ShootMode SHOOT_SHIELD = new ShootMode(Profile.UPGRADE_SHIELD, SHOOT_DELAY_SPREAD) {
        @Override
        protected final boolean isCurrentlyAllowed(final Player player) {
            return Panctor.isDestroyed(player.lastShieldProjectile);
        }
        
        @Override
        protected final void onDeselect(final Player player) {
            Panctor.detach(player.shield);
        }
        
        @Override
        protected final void onShootStart(final Player player) {
            if (!shoot(player)) {
                return;
            }
            player.currentShootSet = player.pi.throwSet;
            player.setShootMode(player.prf.lastUsedFallbackShootMode);
        }
        
        @Override
        protected final void onShooting(final Player player) {
            if (player.prf.lastUsedFallbackShootMode.isContinuous()) {
                onShootStart(player);
            }
        }
        
        @Override
        protected final int getRequiredStamina(final Player player) {
            return 5;
        }
        
        @Override
        protected final void onStepEnd(final Player player) {
            final Object o = player.getCurrentDisplayExtra();
            final PlayerImageExtra pext = (o == null) ? null : (PlayerImageExtra) o;
            final HeldExtra ext = (pext == null) ? null : pext.shield;
            if (ext == null) {
                Panctor.detach(player.shield);
                return;
            }
            HeldShield shield = player.shield;
            if (Panctor.isDestroyed(shield)) {
                player.shield = shield = new HeldShield(player);
            }
            onStepHeld(player, shield, pext, ext);
        }
        
        @Override
        protected final void createProjectile(final Player player) {
            new ShieldProjectile(player);
        }
        
        @Override
        protected final boolean isAllowedFallbackOption() {
            return false;
        }
    };
    
    protected final static void onStepHeld(final Player player, final Panctor held, final PlayerImageExtra pext, final HeldExtra ext) {
        final Panple pos = player.getPosition();
        if (held.getLayer() == null) {
            player.addActor(held);
        }
        held.setVisible(Panctor.isAttached(player) && player.isVisible() && player.stateHandler.isPlayerRendered());
        held.changeView(ext.heldImage);
        final boolean playerMirror = player.getAimMirror();
        held.getPosition().set(pos.getX() + (Player.getMirrorMultiplier(playerMirror) * ext.heldX) + (playerMirror ? pext.mirrorX : 0), pos.getY() + ext.heldY + player.offY, ext.heldZ);
        held.setMirror(ext.heldMirror ^ playerMirror);
        held.setFlip(ext.heldFlip);
        held.setRot(ext.heldRot);
        if (ext.heldReplacement != null) {
            player.changeView(ext.heldReplacement);
        }
    }
    
    protected final static ShootMode SHOOT_SWORD = new ShootMode(Profile.UPGRADE_SWORD, SHOOT_DELAY_WIELD_SHORT) {
        @Override
        protected final void onDeselect(final Player player) {
            Panctor.detach(player.sword);
            player.lastShotDelay = 0;
        }
        
        @Override
        protected final void onShootStart(final Player player) {
            if (player.pi.meleeMode != null) {
                player.pi.meleeMode.onShootStart(this, player);
                return;
            }
            final PlayerImagesSubSet currentShootSet = player.getCurrentImagesSubSet(); // Cleared by shoot method below, so read it first
            if (shoot(player)) {
                final PlayerImagesSubSet[] wieldSets = player.pi.wieldSets;
                if (currentShootSet == wieldSets[0]) {
                    player.currentShootSet = wieldSets[2];
                    player.currentShootTime = WIELD_TIME_LONG;
                    player.lastShotDelay = SHOOT_DELAY_WIELD_LONG;
                    player.lastSwordProjectile.power = Projectile.POWER_MAXIMUM;
                    player.lastSwordProjectile.setView(BotsnBoltsGame.getSwordFullHitBox());
                } else if (currentShootSet == wieldSets[1]) {
                    player.currentShootSet = wieldSets[0];
                    player.currentShootTime = WIELD_TIME_SHORT;
                } else {
                    player.currentShootSet = wieldSets[1];
                    player.currentShootTime = WIELD_TIME_SHORT;
                }
            }
        }
        
        @Override
        protected final int getRequiredStamina(final Player player) {
            return 5;
        }
        
        @Override
        protected final void onStepEnd(final Player player) {
            final Object o = player.getCurrentDisplayExtra();
            final PlayerImageExtra pext = (o == null) ? null : (PlayerImageExtra) o;
            final HeldExtra ext = (pext == null) ? null : pext.sword;
            if (ext == null) {
                Panctor.detach(player.sword);
                return;
            }
            HeldSword sword = player.sword;
            if (Panctor.isDestroyed(sword)) {
                player.sword = sword = new HeldSword(player);
            }
            onStepHeld(player, sword, pext, ext);
            sword.pext = pext;
        }

        @Override
        protected final void createProjectile(final Player player) {
            player.lastSwordProjectile = new SwordProjectile(player);
        }
        
        @Override
        protected final boolean isAllowedFallbackOption() {
            return false;
        }
    };
    
    protected final static ShootMode SHOOT_MECH = new ShootMode(null, SHOOT_DELAY_SPREAD) {
        @Override
        protected final void onShootStart(final Player player) {
            shoot(player);
        }
        
        @Override
        protected final int getRequiredStamina(final Player player) {
            return 0;
        }
        
        @Override
        protected final void createProjectile(final Player player) {
            player.afterShoot(getClock());
            newProjectile(player, VEL_PROJECTILE, 0);
            newProjectile(player, VX_SPREAD1, VY_SPREAD1);
            newProjectile(player, VX_SPREAD1, -VY_SPREAD1);
            newProjectile(player, VX_SPREAD2, VY_SPREAD2);
            newProjectile(player, VX_SPREAD2, -VY_SPREAD2);
        }
        
        protected final void newProjectile(final Player player, final float vx, final float vy) {
            final Panctor prj = player.newProjectile(vx, vy, Projectile.POWER_MEDIUM);
            prj.changeView(player.pi.projectile2);
        }
    };
    
    protected final static ShootMode SHOOT_BOMB = new ShootMode(Profile.UPGRADE_BOMB, SHOOT_DELAY_DEFAULT) {
        @Override
        protected final void onShootStart(final Player player) {
            shoot(player);
        }
        
        @Override
        protected final int getRequiredStamina(final Player player) {
            return 0;
        }
        
        @Override
        protected final void createProjectile(final Player player) {
            player.newBomb();
        }
    };
    
    protected final static ShootMode[] SHOOT_MODES = { SHOOT_NORMAL, SHOOT_CHARGE, SHOOT_SPREAD, SHOOT_RAPID, SHOOT_STREAM, SHOOT_SHIELD, SHOOT_SWORD }; //TODO separate list per episode
    
    protected abstract static class MeleeMode {
        protected abstract String getName();
        
        protected abstract int getHorizontalOffsetX();
        
        protected abstract int getHorizontalOffsetY();
        
        protected abstract int getBackOffsetX();
        
        protected abstract int getBackOffsetY();
        
        protected abstract String getAttackImageSuffix();
        
        protected abstract int getAttackOffsetX();
        
        protected abstract int getAttackOffsetY();
        
        protected abstract void onShootStart(final ShootMode shootMode, final Player player);
    }
    
    protected final static MeleeMode MELEE_WHIP = new MeleeMode() {
        @Override
        protected final String getName() {
            return "Whip";
        }
        
        @Override
        protected final int getHorizontalOffsetX() {
            return -1;
        }
        
        @Override
        protected final int getHorizontalOffsetY() {
            return -4;
        }
        
        @Override
        protected final int getBackOffsetX() {
            return 4;
        }
        
        @Override
        protected final int getBackOffsetY() {
            return 12;
        }
        
        @Override
        protected final String getAttackImageSuffix() {
            return "WhipAttack.png";
        }
        
        @Override
        protected final int getAttackOffsetX() {
            return 5;
        }
        
        @Override
        protected final int getAttackOffsetY() {
            return 1;
        }
        
        @Override
        protected final void onShootStart(final ShootMode shootMode, final Player player) {
            if (shootMode.shoot(player)) {
                final PlayerImagesSubSet[] wieldSets = player.pi.wieldSets;
                player.currentShootSet = wieldSets[3];
                player.currentShootTime = WIELD_TIME_MEDIUM;
                player.lastShotDelay = SHOOT_DELAY_WIELD_MEDIUM;
                player.lastSwordProjectile.setView(BotsnBoltsGame.getWhipHitBox());
            }
        }};
        
    
    protected abstract static class JumpMode extends InputMode {
        protected JumpMode(final Upgrade requiredUpgrade) {
            super(requiredUpgrade);
        }
        
        protected final void onAirJump(final Player player) {
            if (player.useStamina(getRequiredStamina(player))) {
                airJump(player);
            }
        }
        
        protected abstract void airJump(final Player player);
        
        //@OverrideMe
        protected void onDeselect(final Player player) {
        }
    }
    
    protected final static JumpMode JUMP_NORMAL = new JumpMode(null) {
        @Override
        protected final int getRequiredStamina(final Player player) {
            return 0;
        }
        
        @Override
        protected final void airJump(final Player player) {
            player.onAirJumpNormal();
        }
    };
    
    protected final static JumpMode JUMP_BALL = new JumpMode(Profile.UPGRADE_BALL) {
        @Override
        protected final int getRequiredStamina(final Player player) {
            return 0;
        }
        
        @Override
        protected final void airJump(final Player player) {
            player.startBall();
        }
    };
    
    protected final static JumpMode JUMP_GRAPPLING_HOOK = new JumpMode(Profile.UPGRADE_GRAPPLING_BEAM) {
        @Override
        protected final int getRequiredStamina(final Player player) {
            return 1;
        }
        
        @Override
        protected final void airJump(final Player player) {
            player.startGrapple();
            //TODO Maybe holding jump until highest point could also trigger grappling
        }
        
        @Override
        protected final void onDeselect(final Player player) {
            player.endGrapple();
        }
    };
    
    protected final static JumpMode JUMP_SPRING = new JumpMode(Profile.UPGRADE_SPRING) {
        @Override
        protected final int getRequiredStamina(final Player player) {
            return 1;
        }
        
        @Override
        protected final void airJump(final Player player) {
            player.startSpring();
        }
    };
    
    protected final static JumpMode JUMP_BOARD = new JumpMode(Profile.UPGRADE_BOARD) {
        @Override
        protected final int getRequiredStamina(final Player player) {
            return 1;
        }
        
        @Override
        protected final void airJump(final Player player) {
            player.startBoard();
        }
        
        @Override
        protected final void onDeselect(final Player player) {
            player.endBoard();
        }
    };
    
    protected final static JumpMode JUMP_GLIDER = new JumpMode(Profile.UPGRADE_GLIDER) {
        @Override
        protected final int getRequiredStamina(final Player player) {
            return 1;
        }
        
        @Override
        protected final void airJump(final Player player) {
            player.startGlider();
        }
        
        @Override
        protected final void onDeselect(final Player player) {
            player.endGlider();
        }
    };
    
    private final static JumpMode[] JUMP_MODES = { JUMP_NORMAL, JUMP_BALL, JUMP_SPRING, JUMP_GRAPPLING_HOOK, JUMP_BOARD, JUMP_GLIDER };
    
    protected final static class Bubble extends Panctor implements StepListener {
        private int dir;
        private int timer = 0;
        
        protected Bubble(final Chr src, final int offY) {
            this(src, 0, offY);
        }
        
        protected Bubble(final Chr src, final int offX, final int offY) {
            final Panple pos = src.getPosition();
            dir = src.getMirrorMultiplier();
            getPosition().set(pos.getX() + (dir * offX), pos.getY() + offY, BotsnBoltsGame.DEPTH_CARRIER);
            if (isSolidIndex(BotsnBoltsGame.tm.getContainer(this))) {
                destroy();
                return;
            }
            setView(BotsnBoltsGame.bubble[0]);
            src.getLayer().addActor(this);
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            final Panple pos = getPosition();
            if (pos.getY() >= (RoomLoader.waterLevel - 16)) {
                destroy();
                return;
            }
            final TileMap tm = BotsnBoltsGame.tm;
            final int x = tm.getContainerColumn(pos.getX()), y = tm.getContainerRow(pos.getY());
            final boolean solidLeft = isSolidTile(x - 1, y), solidRight = isSolidTile(x + 1, y);
            if (!solidLeft && isSolidTile(x - 1, y + 1)) {
                destroy();
                return;
            } else if (!solidRight && isSolidTile(x + 1, y + 1)) {
                destroy();
                return;
            } else if (isSolidTile(x, y + 1)) {
                destroy();
                return;
            } else if ((x < 2) || solidLeft) {
                dir = 1;
            } else if ((x > (tm.getWidth() - 3)) || solidRight) {
                dir = -1;
            } else if (Mathtil.rand(10)) {
                dir *= -1;
            }
            pos.add(dir, 1);
            timer++;
            if (timer > 30) {
                changeView(BotsnBoltsGame.bubble[2]);
            } else if (timer > 15) {
                changeView(BotsnBoltsGame.bubble[1]);
            }
        }
    }
    
    protected final static class SlideKick extends Panctor implements Collidable, Follower {
        protected final Projectile pseudoProjectile;
        
        protected SlideKick(final Player player) {
            Projectile.autoAddProjectile = false;
            this.pseudoProjectile = new Explosion(player, player) {
                @Override
                public final void burst() {
                    // Don't let super method destroy this
                }
            };
            Projectile.autoAddProjectile = true;
            setView(BotsnBoltsGame.getEmpty16());
        }

        @Override
        public final int getOffsetX() {
            return 14 * getMirrorMultiplier();
        }

        @Override
        public final int getOffsetY() {
            return 0;
        }
    }
    
    protected static class Warp extends Panctor implements StepListener {
        protected final Warpable actor;
        private final PlayerImages pi;
        private final boolean success;
        
        protected Warp(final Player player) {
            this(player, player.pi, true);
            player.stateHandler = WARP_HANDLER;
        }
        
        protected Warp(final Warpable actor, final PlayerImages pi) {
            this(actor, pi, true);
        }
        
        protected Warp(final Warpable actor, final PlayerImages pi, final boolean success) {
            this.actor = actor;
            this.pi = pi;
            this.success = success;
            final Panple ppos = actor.getPosition();
            getPosition().set(ppos.getX(), BotsnBoltsGame.SCREEN_H, ppos.getZ());
            addActor(actor, this);
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panple pos = getPosition();
            renderWarp(renderer, getLayer(), pi, pos.getX(), pos.getY(), pos.getZ());
        }
        
        protected final static void renderWarp(final Panderer renderer, final Panlayer layer, final PlayerImages pi, final float x, final float y, final float z) {
            if (layer == null) {
                return;
            }
            final Panmage img = pi.warp;
            for (int i = 0; i < 4; i++) {
                renderer.render(layer, img, x, y + (i * 8), z);
            }
        }

        @Override
        public void onStep(final StepEvent event) {
            final Panple pos = getPosition();
            pos.addY(-16);
            final float py = actor.getPosition().getY();
            if (pos.getY() <= py) {
                pos.setY(py);
                finish();
            }
        }
        
        protected final void finish() {
            if (success) {
                new Materialize(actor, pi.materialize);
            } else {
                actor.destroy();
            }
            destroy();
        }
    }
    
    protected static class Unwarp extends Warp {
        protected Unwarp(final Warpable actor, final PlayerImages pi) {
            super(actor, pi);
            getPosition().set(actor.getPosition());
        }
        
        @Override
        public void onStep(final StepEvent event) {
            final Panple pos = getPosition();
            pos.addY(16);
            if (pos.getY() >= BotsnBoltsGame.GAME_H) {
                actor.onUnwarped();
                destroy();
            }
        }
    }
    
    protected static class Materialize extends Panctor implements AnimationEndListener {
        protected final Warpable actor;
        
        protected Materialize(final Warpable actor, final Panimation anm) {
            this.actor = actor;
            setView(anm);
            getPosition().set(actor.getPosition());
            addActor(actor, this);
            BotsnBoltsGame.fxWarp.startSound();
        }
        
        @Override
        public final void onAnimationEnd(final AnimationEndEvent event) {
            finish();
        }
        
        protected void finish() {
            actor.onMaterialized();
            destroy();
        }
    }
    
    protected final static class Dematerialize extends Materialize {
        private final Player player;
        
        protected Dematerialize(final Player actor) {
            super(actor, actor.pi.dematerialize);
            actor.setHidden(true);
            this.player = actor;
        }
        
        @Override
        protected final void finish() {
            new Unwarp(player, player.pi);
            destroy();
        }
    }
    
    protected final static class DefeatOrb extends Pandy implements AllOobListener {
        private int age = 0;
        
        @Override
        public final void onStep(final StepEvent event) {
            super.onStep(event);
            age++;
            if (age > 180) {
                destroy();
            }
        }
        
        @Override
        public final void onAllOob(final AllOobEvent event) {
            destroy();
        }
    }
    
    protected static interface Wrapper extends SpecPanctor {
        public void endWrap(final Player player);
    }
    
    protected static interface Follower extends SpecPanctor {
        public int getOffsetX();
        
        public int getOffsetY();
    }
    
    protected final static class BurstFollower extends Burst implements Follower {
        private final int offsetX;
        private final int offsetY;
        
        public BurstFollower(final Panimation anim, final int offsetX, final int offsetY) {
            super(anim);
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
        
        @Override
        public final int getOffsetX() {
            return offsetX;
        }
        
        @Override
        public final int getOffsetY() {
            return offsetY;
        }
    }
    
    protected final static class HeldShield extends Panctor {
        protected HeldShield(final Player src) {
        }
    }
    
    protected final static class HeldSword extends Panctor {
        private final Player src;
        private PlayerImageExtra pext = null;
        
        protected HeldSword(final Player src) {
            this.src = src;
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            super.renderView(renderer);
            if (pext == null) {
                return;
            }
            final int trail = pext.trail;
            if (trail == 0) {
                return;
            }
            final Panlayer layer = getLayer();
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY();
            final Panmage[] swordTrails = src.pi.swordTrails;
            final boolean mirror = src.getAimMirror();
            final int m = getMirrorMultiplier(mirror), o = mirror ? -15 : 0;
            final long t = getClock() - src.lastShotFired;
            if (trail == 1) {
                if (t > 6) {
                    return;
                }
                renderer.render(layer, swordTrails[1], x + (m * 13) + o, y, BotsnBoltsGame.DEPTH_PLAYER_FRONT, 0, 0, 16, 16, 0, mirror, false);
                if (t > 4) {
                    return;
                }
                renderer.render(layer, swordTrails[0], x + (m * 17) + o, y - 8, BotsnBoltsGame.DEPTH_PLAYER_FRONT, 0, 0, 16, 16, 0, mirror, false);
                if (t > 2) {
                    return;
                }
                renderer.render(layer, swordTrails[1], x + (m * 13) + o, y - 24, BotsnBoltsGame.DEPTH_PLAYER_FRONT, 0, 0, 16, 16, 0, mirror, true);
            } else if (trail == 2) {
                if (t > 6) {
                    return;
                }
                renderer.render(layer, swordTrails[1], x + (m * 13) + o, y - 15, BotsnBoltsGame.DEPTH_PLAYER_FRONT, 0, 0, 16, 16, 0, mirror, true);
                if (t > 4) {
                    return;
                }
                renderer.render(layer, swordTrails[0], x + (m * 17) + o, y + 1, BotsnBoltsGame.DEPTH_PLAYER_FRONT, 0, 0, 16, 16, 0, mirror, false);
                if (t > 2) {
                    return;
                }
                renderer.render(layer, swordTrails[1], x + (m * 13) + o, y + 9, BotsnBoltsGame.DEPTH_PLAYER_FRONT, 0, 0, 16, 16, 0, mirror, false);
            } else if (trail == 3) {
                if (t > 22) {
                    return;
                }
                renderer.render(layer, swordTrails[1], x + (m * -23) + o, y - 2, BotsnBoltsGame.DEPTH_PLAYER_FRONT, 0, 0, 16, 16, 0, !mirror, false);
                if (t > 20) {
                    return;
                }
                renderer.render(layer, swordTrails[0], x + (m * -22) + o, y - 9, BotsnBoltsGame.DEPTH_PLAYER_FRONT, 0, 0, 16, 16, 0, !mirror, false);
                if (t > 18) {
                    return;
                }
                renderer.render(layer, swordTrails[1], x + (m * -16) + o, y - 23, BotsnBoltsGame.DEPTH_PLAYER_FRONT, 0, 0, 16, 16, 0, !mirror, true);
                if (t > 16) {
                    return;
                }
                renderer.render(layer, swordTrails[2], x + (m * -9) + o, y - 27, BotsnBoltsGame.DEPTH_PLAYER_FRONT, 0, 0, 16, 16, 0, !mirror, true);
                if (t > 14) {
                    return;
                }
                renderer.render(layer, swordTrails[1], x + (m * -2) + o, y - 30, BotsnBoltsGame.DEPTH_PLAYER_FRONT, 0, 0, 16, 16, 1, mirror, true);
                if (t > 12) {
                    return;
                }
                renderer.render(layer, swordTrails[0], x + (m * 13) + o, y - 31, BotsnBoltsGame.DEPTH_PLAYER_FRONT, 0, 0, 16, 16, 3, mirror, false);
                if (t > 10) {
                    return;
                }
                renderer.render(layer, swordTrails[1], x + (m * 20) + o, y - 29, BotsnBoltsGame.DEPTH_PLAYER_FRONT, 0, 0, 16, 16, 1, !mirror, true);
                if (t > 8) {
                    return;
                }
                renderer.render(layer, swordTrails[2], x + (m * 24) + o, y - 22, BotsnBoltsGame.DEPTH_PLAYER_FRONT, 0, 0, 16, 16, 0, mirror, true);
                if (t > 6) {
                    return;
                }
                renderer.render(layer, swordTrails[1], x + (m * 28) + o, y - 16, BotsnBoltsGame.DEPTH_PLAYER_FRONT, 0, 0, 16, 16, 0, mirror, true);
                if (t > 4) {
                    return;
                }
                renderer.render(layer, swordTrails[0], x + (m * 27) + o, y - 1, BotsnBoltsGame.DEPTH_PLAYER_FRONT, 0, 0, 16, 16, 0, mirror, false);
                if (t > 2) {
                    return;
                }
                renderer.render(layer, swordTrails[1], x + (m * 23) + o, y + 5, BotsnBoltsGame.DEPTH_PLAYER_FRONT, 0, 0, 16, 16, 0, mirror, false);
            }
        }
    }
    
    protected final static class PlayerImageExtra {
        private final int mirrorX;
        private final int trail;
        private final HeldExtra shield;
        private final HeldExtra sword;
        
        protected PlayerImageExtra(final int mirrorX, final int trail, final HeldExtra shield, final HeldExtra sword) {
            this.mirrorX = mirrorX;
            this.trail = trail;
            this.shield = shield;
            this.sword = sword;
        }
    }
    
    protected final static class HeldExtra {
        private final Panmage heldImage;
        private final float heldX;
        private final float heldY;
        private final float heldZ;
        private final boolean heldMirror;
        private final boolean heldFlip;
        private final int heldRot;
        private final Panmage heldReplacement;
        
        protected HeldExtra(final Panmage heldImage, final int heldX, final int heldY, final int heldZ,
                final boolean heldMirror, final boolean heldFlip, final int heldRot, final Panmage heldReplacement) {
            this.heldImage = heldImage;
            this.heldX = heldX;
            this.heldY = heldY;
            this.heldZ = heldZ;
            this.heldMirror = heldMirror;
            this.heldFlip = heldFlip;
            this.heldRot = heldRot;
            this.heldReplacement = heldReplacement;
        }
    }
    
    // A Player-like projectile (from an actual Player or an AiBoss)
    public static interface SpecProjectile extends SpecPanctor, Collidable {
        public void assignPower(final int power);
        
        public Player getSource();
        
        public PlayerImages getPlayerImages();
        
        public ShootMode getShootMode();
        
        public void burst();
    }
    
    // A Player-like stream projectile (from an actual Player or an AiBoss)
    public static interface SpecStreamProjectile extends SpecProjectile {
        public int getOffsetX();
        public void initSourceMirror();
        
        public boolean getSourceMirror();
        
        public void onStepEnd(final float y);
    }
    
    // A projectile from an actual Player that could hurt an Enemy
    public static interface SpecPlayerProjectile extends SpecProjectile {
        public int getPower();
        
        public void bounce();
        
        public float getVelocityX();
    }
    
    protected static interface RoomChangeListener extends SpecPanctor {
        public final static int ROOM_CHANGE_KEEP = 1;
        public final static int ROOM_CHANGE_DESTROY = 2;
        
        public int getRoomChangeStatus();
    }
    
    protected static interface Ai {
        public void onStep(final Player player);
    }
    
    protected final static class StillAi implements Ai {
        @Override
        public final void onStep(final Player player) {
        }
    }
    
    protected final static class LeftAi implements Ai {
        private final float dstX;
        
        protected LeftAi(final float dstX) {
            this.dstX = dstX;
        }
        
        @Override
        public final void onStep(final Player player) {
            if (player.getPosition().getX() <= dstX) {
                player.pickMirror();
                player.finishScript();
                return;
            }
            player.stateHandler.onLeft(player);
        }
    }
    
    protected final static class RightAi implements Ai {
        private final float dstX;
        
        protected RightAi(final float dstX) {
            this.dstX = dstX;
        }
        
        @Override
        public final void onStep(final Player player) {
            if (player.getPosition().getX() >= dstX) {
                player.pickMirror();
                player.finishScript();
                return;
            }
            player.stateHandler.onRight(player);
        }
    }
    
    protected final Ai getWalkAi(final float dstX) {
        if (getPosition().getX() < dstX) {
            return new RightAi(dstX);
        }
        return new LeftAi(dstX);
    }
    
    protected final void pickMirror() {
        setMirror(!isOnLeftSide());
    }
    
    protected final boolean isOnLeftSide() {
        final Panlayer layer = getLayerRequired();
        final float w = (layer == null) ? BotsnBoltsGame.GAME_W : layer.getSize().getX();
        return getPosition().getX() < (w / 2);
    }
}
