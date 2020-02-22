/*
Copyright (c) 2009-2020, Andrew M. Martin
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

import org.pandcorps.botsnbolts.Animal.*;
import org.pandcorps.botsnbolts.Extra.*;
import org.pandcorps.botsnbolts.HudMeter.*;
import org.pandcorps.botsnbolts.Profile.*;
import org.pandcorps.botsnbolts.Projectile.*;
import org.pandcorps.botsnbolts.RoomLoader.*;
import org.pandcorps.botsnbolts.ShootableDoor.*;
import org.pandcorps.core.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.event.boundary.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.visual.*;

public class Player extends Chr implements Warpable, StepEndListener {
    protected final static int PLAYER_X = 6;
    protected final static int PLAYER_H = 23;
    protected final static int BALL_H = 15;
    protected final static int CENTER_Y = 11;
    private final static int SHOOT_DELAY_DEFAULT = 5;
    private final static int SHOOT_DELAY_RAPID = 3;
    private final static int SHOOT_DELAY_SPREAD = 15;
    protected final static int SHOOT_TIME = 12;
    private final static int CHARGE_TIME_MEDIUM = 30;
    private final static int CHARGE_TIME_BIG = 60;
    private final static int INVINCIBLE_TIME = 60;
    private final static int HURT_TIME = 15;
    private final static int FROZEN_TIME = 60;
    private final static int BUBBLE_TIME = 60;
    private final static int RUN_TIME = 5;
    protected final static int VEL_JUMP = 8;
    protected final static float VEL_BOUNCE_BOMB = 7.5f;
    protected final static int VEL_SPRING = 10;
    private final static int VEL_WALK = 3;
    protected final static int VEL_PROJECTILE = 8;
    private final static float VX_SPREAD1;
    private final static float VY_SPREAD1;
    private final static float VX_SPREAD2;
    private final static float VY_SPREAD2;
    private final static double GRAPPLING_BOOST = 0.01;
    private final static double GRAPPLING_BOOST_MAX = 0.75;
    private final static double GRAPPLING_ANGLE_MIRROR_THRESHOLD = 0.01;
    private final static double GRAPPLING_ANGLE_MAX_UP = Math.PI / 8.0;
    private final static double GRAPPLING_ANGLE_MAX_DIAG = 3.0 * GRAPPLING_ANGLE_MAX_UP;
    private final static int GRAPPLING_OFF_Y = 12;
    private final static int VEL_ROOM_CHANGE = 10;
    protected final static float NULL_COORD = -2000;
    
    protected final PlayerContext pc;
    protected final Profile prf;
    protected final PlayerImages pi;
    protected StateHandler stateHandler = NORMAL_HANDLER;
    private boolean running = false;
    private int runIndex = 0;
    private int runTimer = 0;
    private int blinkTimer = 0;
    private long lastShotFired = NULL_CLOCK;
    private long lastShotPosed = NULL_CLOCK;
    protected static long lastShotByAnyPlayer = NULL_CLOCK;
    private long startCharge = NULL_CLOCK;
    private long lastCharge = NULL_CLOCK;
    private long lastHurt = NULL_CLOCK;
    private long lastFrozen = NULL_CLOCK;
    private long lastBubble = NULL_CLOCK;
    private long lastJump = NULL_CLOCK;
    private long lastLift = NULL_CLOCK;
    private long lastBall = NULL_CLOCK;
    private int wrappedJumps = 0;
    protected Carrier jumpStartedOnCarrier = null;
    protected Carrier walkedOffCarrier = null;
    private boolean jumpAllowed = true;
    private float minX = Integer.MIN_VALUE;
    private float maxX = Integer.MAX_VALUE;
    private boolean prevUnderwater = false;
    private boolean sanded = false;
    private long lastSanded = NULL_CLOCK;
    private int queuedX = 0;
    private boolean air = false;
    private int wallTimer = 0;
    private boolean wallMirror = false;
    protected boolean movedDuringJump = false;
    private byte bossDoorStatus = 0;
    protected int health = HudMeter.MAX_VALUE;
    protected HudMeter healthMeter = null;
    private GrapplingHook grapplingHook = null;
    protected double grapplingR = 0;
    private double grapplingT = 0;
    private double grapplingV = 0;
    private boolean grapplingBoostAllowed = true;
    private boolean grapplingRetractAllowed = false;
    private boolean grapplingAllowed = true;
    private final ImplPanple grapplingPosition = new ImplPanple();
    protected Spring spring = null;
    protected Carrier carrier = null;
    protected float carrierOff = 0;
    protected float carrierX = NULL_COORD;
    private Wrapper wrapper = null;
    private int ladderColumn = -1;
    protected boolean startRoomNeeded = true;
    private BotRoom startRoom = null;
    private float startX = NULL_COORD;
    private float startY = NULL_COORD;
    private boolean startMirror = false;
    private int availableRescues = 0;
    protected Rescue rescue = null;
    private float safeX = NULL_COORD;
    private float safeY = NULL_COORD;
    private boolean safeMirror = false;
    private List<Follower> followers = null;
    private boolean hidden = false;
    protected boolean active = true;
    private boolean scripted = false;
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
    
    private final static Panput[] getInputArray(final Panput key, final Panput touchButton) {
        return (touchButton == null) ? new Panput[] { key } : new Panput[] {key, touchButton};
    }
    
    protected final void registerInputs(final ControlScheme ctrl) {
        final Panput[] jumpInput = getInputArray(ctrl.get1(), Menu.jump);
        final Panput[] shootInput = getInputArray(ctrl.get2(), Menu.attack);
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
        register(rightInput, new ActionListener() {
            @Override public final void onAction(final ActionEvent event) { right(); }});
        register(leftInput, new ActionListener() {
            @Override public final void onAction(final ActionEvent event) { left(); }});
        register(upInput, new ActionListener() { //TODO Display up/down touch buttons when near ladder, hide otherwise
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
        final Panput[] toggleJumpInput = getInputArray(interaction.KEY_SHIFT_LEFT, Menu.toggleJump);
        final Panput[] toggleAttackInput = getInputArray(interaction.KEY_TAB, Menu.toggleAttack);
        final ActionStartListener toggleJumpListener = new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { toggleJumpMode(1); }};
        final ActionStartListener toggleShootListener = new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { toggleShootMode(1); }};
        register(toggleJumpInput, toggleJumpListener);
        register(toggleAttackInput, toggleShootListener);
        register(interaction.KEY_COMMA, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { toggleJumpMode(-1); }});
        register(interaction.KEY_BRACKET_LEFT, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { toggleShootMode(-1); }});
        register(interaction.KEY_PERIOD, toggleJumpListener);
        register(interaction.KEY_BRACKET_RIGHT, toggleShootListener);
        registerCapture(this);
    }
    
    protected final static void registerCapture(final Panctor actor) {
        final Pangine engine = Pangine.getEngine();
        final Panteraction interaction = engine.getInteraction();
        actor.register(interaction.KEY_F1, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { engine.captureScreen(); }});
        actor.register(interaction.KEY_F2, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { engine.startCaptureFrames(); }});
        actor.register(interaction.KEY_F3, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { engine.stopCaptureFrames(); }});
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
        prf.shootMode = toggleInputMode(SHOOT_MODES, prf.shootMode, dir);
        prf.shootMode.onSelect(this);
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
            if (newMode.isAvailable(this)) {
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
        }
        final Panple pos = getPosition();
        safeX = pos.getX();
        safeY = pos.getY();
        safeMirror = isMirror();
        if (startRoomNeeded) {
            final BotRoom currentRoom = RoomLoader.getCurrentRoom();
            if (currentRoom == startRoom) {
                return;
            }
            startRoom = currentRoom;
            startX = safeX;
            startY = safeY;
            startMirror = safeMirror;
            startRoomNeeded = false;
        }
    }
    
    protected final void jump() {
        if (jumpAllowed && isFree()) {
            stateHandler.onJump(this);
        }
    }
    
    private final void onJumpNormal() {
        if (isGrounded()) {
            startJump();
        } else {
            stateHandler.onAirJump(this);
        }
    }
    
    private final void startJump() {
        startJump(null);
    }
    
    private final void startJump(final Carrier jumpStartedOnCarrier) {
        this.jumpStartedOnCarrier = jumpStartedOnCarrier;
        v = VEL_JUMP;
        if (sanded) {
            v -= 2;
        }
        lastJump = getClock();
        BotsnBoltsGame.fxJump.startSound();
    }
    
    protected final void releaseJump() {
        if ((v > 0) && (getClock() > (lastLift + 1))) {
            v = 0;
        }
    }
    
    protected final void setJumpAllowed(final boolean jumpAllowed) {
        this.jumpAllowed = jumpAllowed;
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
    
    protected void newProjectile(final float vx, final float vy, final int power) {
        new Projectile(this, vx, vy, power);
    }
    
    protected void newBomb() {
        new Bomb(this);
    }
    
    private final void afterShoot(final long clock) {
        lastShotFired = clock;
        lastShotPosed = clock;
        lastShotByAnyPlayer = clock;
        blinkTimer = 0;
    }
    
    protected final void right() {
        if (isFree()) {
            stateHandler.onRight(this);
        }
    }
    
    private final void onRightNormal() {
        moveHorizontal(VEL_WALK);
    }
    
    private final void moveHorizontal(final int vel) {
        hv = vel;
        if (!isGrounded()) {
            movedDuringJump = true;
        }
    }
    
    @Override
    protected final void setMirror(final int v) {
        final boolean oldMirror = isMirror();
        super.setMirror(v);
        if (oldMirror != isMirror()) {
            onMirror();
            fixX();
        }
    }
    
    private final void onMirror() {
        lastShotPosed = NULL_CLOCK;
    }
    
    protected final void left() {
        if (isFree()) {
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
    
    @Override
    public final void onUnwarped() {
        unwarpHandler.run();
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
        BotsnBoltsGame.fxHurt.startSound();
        return true;
    }
    
    private final void hurtForce(final int damage) {
        stateHandler.onHurt(this);
        lastHurt = getClock();
        isFree(); // Calls onFree(); do after setting lastHurt to avoid loop
        blinkTimer = 0;
        health -= damage;
        if ((v > 0) && !isGrounded()) {
            v = 0;
        }
        startCharge = NULL_CLOCK;
        lastCharge = NULL_CLOCK;
        if (health <= 0) {
            defeat();
        } else {
            addFollower(burst(BotsnBoltsGame.flash, 0, CENTER_Y, BotsnBoltsGame.DEPTH_POWER_UP));
            puff(-12, 25);
            puff(0, 30);
            puff(12, 25);
        }
    }
    
    protected final boolean freeze() {
        if (isInvincible()) {
            return false;
        }
        stateHandler.onHurt(this);
        lastFrozen = getClock();
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
        final Pansound oldMusic = Pangine.getEngine().getAudio().stopMusic();
        startDefeatTimer(new Runnable() {
            @Override public final void run() {
                finishDefeat(oldMusic);
            }});
        destroy();
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
    
    private final void finishDefeat(final Pansound music) {
        healthMeter.destroy();
        if (startRoom == null) {
            RoomLoader.reloadCurrentRoom();
        } else {
            BotsnBoltsGame.playerStartX = startX;
            BotsnBoltsGame.playerStartY = startY;
            BotsnBoltsGame.playerStartMirror = startMirror;
            RoomLoader.loadRoom(startRoom);
            Pansound.changeMusic(music);
        }
    }
    
    @Override
    protected final void onDestroy() {
        destroyGrapplingHook();
        freeWrapper();
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
        hurtForce(1);
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
        if (!needed) {
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
        return (getClock() - lastShotPosed) < SHOOT_TIME;
    }
    
    private final PlayerImagesSubSet getCurrentImagesSubSet() {
        return isShootPoseNeeded() ? pi.shootSet : pi.basicSet;
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
        super.renderView(renderer);
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
    
    @Override
    protected final boolean onStepCustom() {
        updateVisibility();
        if (RoomChanger.isChanging()) {
            final RoomChanger changer = RoomChanger.getActiveChanger();
            if (changer.getAge() <= 28) {
                hv = getDirection(changer.getVelocityX());
                v = getDirection(changer.getVelocityY());
            } else {
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
            if (prf.autoClimb && ((v > 0) || ((ladderColumn != getColumn()) && !isGrounded()))) {
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
                defeat();
                break;
            case BotsnBoltsGame.TILE_HURT :
                if (!isCollisionStandingOnTile(index)) {
                    hurt(BlockPuzzle.DAMAGE_SPIKE);
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
            if (sand || belowSand) {
                if (belowSand || !isAnySolidBehavior(belowLeft) || !isAnySolidBehavior(belowRight)) {
                    pos.addY(-1);
                }
                thv = initCurrentHorizontalVelocitySand();
                sanded = true;
                lastSanded = getClock();
            } else if (belowLeft == TILE_ICE || belowRight == TILE_ICE) {
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
        hv = 0;
        final Panple pos = getPosition();
        final float x = pos.getX();
        if (x < minX) {
            pos.setX(minX);
        } else if (x > maxX) {
            pos.setX(maxX);
        }
        updateWrapper();
        updateFollowers();
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
                follower.getPosition().set(x + follower.getOffsetX(), y + follower.getOffsetY());
                follower.setMirror(mirror);
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
            if (wallTimer > 0 && set.crouch != null && !RoomChanger.isChanging() && wallMirror == isMirror() && isBallAvailable() && isRoomForBall()) {
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
            wallTimer = 0;
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
        clearRun();
        stateHandler = NORMAL_HANDLER;
        setH(PLAYER_H);
    }
    
    private final void endLadder() {
        clearRun();
        stateHandler = NORMAL_HANDLER;
        ladderColumn = getColumn();
    }
    
    private final int getColumn() {
        final TileMap tm = BotsnBoltsGame.tm;
        return tm.getColumn(tm.getContainer(this));
    }
    
    private final void startGrapple() {
        //destroyGrapplingHook(); // Allows Player to float, constantly starting/stopping grapple
        if (grapplingHook != null) {
            return;
        } else if (!grapplingAllowed) {
            return;
        }
        grapplingHook = new GrapplingHook(this);
        v = Math.max(v, VEL_JUMP / 3);
        grapplingV = 0;
        grapplingBoostAllowed = true;
        grapplingRetractAllowed = false;
        grapplingAllowed = false;
        stateHandler = GRAPPLING_HANDLER;
    }
    
    protected final Panple getGrapplingPosition() {
        grapplingPosition.set(getPosition());
        grapplingPosition.addY(GRAPPLING_OFF_Y);
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
    
    protected final void endGrapple() {
        clearRun();
        if (stateHandler == GRAPPLING_HANDLER) {
            stateHandler = NORMAL_HANDLER;
        }
        if (!destroyGrapplingHook()) {
            return;
        } else if (v <= 0) {
            v = VEL_JUMP / 4;
        } else if (v < VEL_JUMP) {
            v = (v + (3 * VEL_JUMP)) / 4;
        }
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
    
    private final void startState(final StateHandler stateHandler) {
        destroyGrapplingHook();
        if (this.stateHandler == BALL_HANDLER) {
            endBall();
        }
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
    protected void onWall(final byte xResult) {
        stateHandler.onWall(this, xResult);
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
    protected boolean onFell() {
        if (changeRoom(0, -1)) {
            v = 0;
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
    
    private final void startRescue() {
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
    
    private final boolean changeRoom(final int dirX, final int dirY) {
        final BotRoomCell roomCell = RoomLoader.getAdjacentRoom(this, dirX, dirY);
        if (roomCell == null) {
            return false;
        }
        lastShotByAnyPlayer = NULL_CLOCK;
        startRoomNeeded = true; // Can also be initialized in RoomLoader, but only if a ROM segment is present
        initAvailableRescues();
        endGrapple();
        safeX = safeY = NULL_COORD;
        final BotRoom room = roomCell.room;
        final int nextX = (roomCell.cell.x - room.x) * BotsnBoltsGame.GAME_W;
        final BoltDoor boltDoor = RoomLoader.boltDoor; // Save this before RoomLoader clears it
        RoomLoader.onChangeStarted();
        final List<Panlayer> layersToKeepBeneath = Coltil.singletonList(BotsnBoltsGame.bgLayer);
        final List<Panlayer> layersToKeepAbove = Arrays.asList(BotsnBoltsGame.hud);
        final List<Panctor> actorsToKeep = new ArrayList<Panctor>();
        actorsToKeep.add(this);
        actorsToKeep.add(BotsnBoltsGame.tm);
        if (!Panctor.isDestroyed(Boss.aiBoss) && (Boss.aiBoss.getLayer() == getLayer())) {
            Boss.aiBoss.getPosition().setY(getPosition().getY());
            Boss.aiBoss.v = 0;
            actorsToKeep.add(Boss.aiBoss);
        }
        Coltil.addIfValued(actorsToKeep, boltDoor); // Keep Player and old TileMap while scrolling
        final List<Panctor> actorsToDestroy = new ArrayList<Panctor>();
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
    
    protected abstract static class StateHandler {
        protected abstract void onJump(final Player player);
        
        //@OverrideMe
        protected void onAirJump(final Player player) {
        }
        
        protected abstract void onShootStart(final Player player);
        
        protected abstract void onShooting(final Player player);
        
        protected abstract void onShootEnd(final Player player);
        
        protected abstract void onRight(final Player player);
        
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
        protected boolean onStep(final Player player) {
            return false;
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
        protected void onWall(final Player player, final byte xResult) {
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
        protected final void onRight(final Player player) {
            player.onRightNormal();
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
        protected final void onWall(final Player player, final byte xResult) {
            if (player.triggerBossDoor()) {
                return;
            } else if (player.wallTimer == 0 && xResult == X_WALL) {
                player.wallTimer = 1;
                player.wallMirror = player.isMirror();
            }
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
                    return true;
                }
                final int frameLength = VEL_WALK * RUN_TIME, animLength = frameLength * 2;
                player.setMirror((Math.round(player.getPosition().getY()) % animLength) < frameLength);
            }
            final Panmage view;
            if (player.isShootPoseNeeded()) {
                view = player.pi.climbShoot;
            } else if (!(player.isTouchingLadder() || player.isTouchingLadder(OFF_LADDER_BOTTOM + VEL_WALK))) {
                view = player.pi.climbTop;
                player.normalizeY(0, 2);
            } else {
                view = player.pi.climb;
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
        
        protected abstract void onShootStart(final Player player);
        
        //@OverrideMe
        protected void onShooting(final Player player) {
        }
        
        //@OverrideMe
        protected void onStep(final Player player) {
        }
        
        //@OverrideMe
        protected void onShootEnd(final Player player) {
        }
        
        protected final void shoot(final Player player) {
            final long clock = getClock();
            if (clock - player.lastShotFired > delay) {
                player.afterShoot(clock);
                createProjectile(player);
            }
        }
        
        protected abstract void createProjectile(final Player player);
        
        protected final void createDefaultProjectile(final Player player) {
            createBasicProjectile(player, VEL_PROJECTILE, 0);
        }
        
        protected final void createBasicProjectile(final Player player, final float vx, final float vy) {
            player.newProjectile(vx, vy, 1);
        }
        
        protected final void shootSpecial(final Player player, final int power) {
            player.afterShoot(getClock());
            player.newProjectile(VEL_PROJECTILE, 0, power);
        }
    }
    
    protected final static class PlayerContext {
        protected final Profile prf;
        protected ControlScheme ctrl;
        protected final PlayerImages pi;
        protected Player player = null;
        private Player srcPlayer = null;
        
        protected PlayerContext(final Profile prf, final PlayerImages pi) {
            this.prf = prf;
            this.pi = pi;
        }
        
        protected final void setControlScheme(final ControlScheme ctrl) {
            this.ctrl = ctrl;
        }
        
        protected final static Player getPlayer(final PlayerContext pc) {
            return (pc == null) ? null : pc.player;
        }
    }
    
    protected final static class PlayerImages {
        protected final PlayerImagesSubSet basicSet;
        protected final PlayerImagesSubSet shootSet;
        private final Panmage hurt;
        private final Panmage frozen;
        protected final Panimation defeat;
        private final Panmage climb;
        private final Panmage climbShoot;
        private final Panmage climbTop;
        private final Panmage jumpAimDiag;
        private final Panmage jumpAimUp;
        protected final Panmage talk;
        protected final Panmage basicProjectile;
        protected final Panimation projectile2;
        protected final Panimation projectile3;
        protected final Panimation charge;
        protected final Panimation chargeVert;
        protected final Panimation charge2;
        protected final Panimation chargeVert2;
        protected final Panimation burst;
        private final Panframe[] ball;
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
        protected final String animalName;
        protected final String birdName;
        
        protected PlayerImages(final PlayerImagesSubSet basicSet, final PlayerImagesSubSet shootSet,
                               final Panmage hurt, final Panmage frozen, final Panimation defeat,
                               final Panmage climb, final Panmage climbShoot, final Panmage climbTop,
                               final Panmage jumpAimDiag, final Panmage jumpAimUp, final Panmage talk,
                               final Panmage basicProjectile, final Panimation projectile2, final Panimation projectile3,
                               final Panimation charge, final Panimation chargeVert, final Panimation charge2, final Panimation chargeVert2,
                               final Panimation burst, final Panframe[] ball, final Panmage warp, final Panimation materialize, final Panimation bomb,
                               final Panmage link, final Panimation batterySmall, final Panimation batteryMedium, final Panimation batteryBig,
                               final Panmage doorBolt, final Panmage bolt, final Panmage disk,
                               final Panmage powerBox, final Map<String, Panmage> boltBoxes, final Panmage diskBox, final Panmage highlightBox,
                               final Panmage portrait, final HudMeterImages hudMeterImages, final String animalName, final String birdName) {
            this.basicSet = basicSet;
            this.shootSet = shootSet;
            this.hurt = hurt;
            this.frozen = frozen;
            this.defeat = defeat;
            this.climb = climb;
            this.climbShoot = climbShoot;
            this.climbTop = climbTop;
            this.jumpAimDiag = jumpAimDiag;
            this.jumpAimUp = jumpAimUp;
            this.talk = talk;
            this.basicProjectile = basicProjectile;
            this.projectile2 = projectile2;
            this.projectile3 = projectile3;
            this.charge = charge;
            this.chargeVert = chargeVert;
            this.charge2 = charge2;
            this.chargeVert2 = chargeVert2;
            this.burst = burst;
            this.ball = ball;
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
            this.animalName = animalName;
            this.birdName = birdName;
        }
    }
    
    protected final static class PlayerImagesSubSet {
        protected final Panmage stand;
        protected final Panmage jump;
        private final Panmage[] run;
        private final Panmage start;
        protected final Panmage blink;
        private final Panmage[] crouch;
        
        protected PlayerImagesSubSet(final Panmage stand, final Panmage jump, final Panmage[] run, final Panmage start, final Panmage blink, final Panmage[] crouch) {
            this.stand = stand;
            this.jump = jump;
            this.run = run;
            this.start = start;
            this.blink = blink;
            this.crouch = crouch;
        }
    }
    
    protected final static ShootMode SHOOT_NORMAL = new ShootMode(null, SHOOT_DELAY_DEFAULT) {
        @Override
        protected final void onShootStart(final Player player) {
            shoot(player);
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
        protected final void createProjectile(final Player player) {
            createDefaultProjectile(player);
        }
    };
    
    protected final static ShootMode SHOOT_SPREAD = new ShootMode(Profile.UPGRADE_SPREAD, SHOOT_DELAY_SPREAD) {
        @Override
        protected final void onShootStart(final Player player) {
            shoot(player);
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
                if (diff > CHARGE_TIME_BIG) {
                    charge(player, pi.charge2, pi.chargeVert2, diff - CHARGE_TIME_BIG, BotsnBoltsGame.fxSuperCharge);
                } else {
                    charge(player, pi.charge, pi.chargeVert, diff - CHARGE_TIME_MEDIUM, BotsnBoltsGame.fxCharge);
                }
            }
        }
        
        @Override
        protected final void onStep(final Player player) {
            if (player.prf.autoCharge) {
                onCharging(player);
            }
        }
        
        private final void charge(final Player player, final Panimation diag, final Panimation vert, final long i, final Pansound sound) {
            final long c = getClock() % 8;
            if (c == 0) {
                chargeDiag(player, diag, 1, 1, 0);
            } else if (c == 1) {
                chargeDiag(player, diag, -1, -1, 2);
            } else if (c == 2) {
                charge(player, vert, 1, -4, 4, 1, 8, 16, 0);
            } else if (c == 3) {
                charge(player, vert, -1, 8, 16, 1, -4, 4, 1);
            } else if (c == 4) {
                chargeDiag(player, diag, -1, 1, 1);
            } else if (c == 5) {
                chargeDiag(player, diag, 1, -1, 3);
            } else if (c == 6) {
                charge(player, vert, 1, -4, 4, -1, 8, 16, 2);
            } else {
                charge(player, vert, 1, 8, 16, 1, -4, 4, 3);
            }
            if ((i < 30) && ((i % 10) == 1)) {
                sound.startSound();
            }
        }
        
        private final void chargeDiag(final Player player, final Panimation anm, final int xdir, final int ydir, final int rot) {
            charge(player, anm, xdir, 4, 12, ydir, 4, 12, rot);
        }
        
        private final void charge(final Player player, final Panimation anm, final int xdir, final int xmin, final int xmax, final int ydir, final int ymin, final int ymax, final int rot) {
            final Burst burst = new Burst(anm);
            final Panple ppos = player.getPosition();
            burst.getPosition().set(ppos.getX() + (xdir * Mathtil.randi(xmin, xmax)), ppos.getY() + 12 + (ydir * Mathtil.randi(ymin, ymax)), BotsnBoltsGame.DEPTH_BURST);
            burst.setRot(rot);
            player.addActor(burst);
        }
        
        @Override
        protected final void onShootEnd(final Player player) {
            if (!player.prf.autoCharge) {
                shootChargedIfNeeded(player);
            }
        }
        
        private final boolean shootChargedIfNeeded(final Player player) {
            final long diff = getClock() - player.startCharge;
            if (diff > CHARGE_TIME_BIG) {
                shootSpecial(player, Projectile.POWER_MAXIMUM);
                return true;
            } else if (diff > CHARGE_TIME_MEDIUM) {
                shootSpecial(player, Projectile.POWER_MEDIUM);
                return true;
            }
            return false;
        }
        
        @Override
        protected final void createProjectile(final Player player) {
            createDefaultProjectile(player);
        }
    };
    
    protected final static ShootMode SHOOT_BOMB = new ShootMode(Profile.UPGRADE_BOMB, SHOOT_DELAY_DEFAULT) {
        @Override
        protected final void onShootStart(final Player player) {
            shoot(player);
        }
        
        @Override
        protected final void createProjectile(final Player player) {
            player.newBomb();
        }
    };
    
    private final static ShootMode[] SHOOT_MODES = { SHOOT_NORMAL, SHOOT_CHARGE, SHOOT_SPREAD, SHOOT_RAPID };
    
    protected abstract static class JumpMode extends InputMode {
        protected JumpMode(final Upgrade requiredUpgrade) {
            super(requiredUpgrade);
        }
        
        protected abstract void onAirJump(final Player player);
        
        //@OverrideMe
        protected void onDeselect(final Player player) {
        }
    }
    
    protected final static JumpMode JUMP_NORMAL = new JumpMode(null) {
        @Override
        protected final void onAirJump(final Player player) {
        }
    };
    
    protected final static JumpMode JUMP_BALL = new JumpMode(Profile.UPGRADE_BALL) {
        @Override
        protected final void onAirJump(final Player player) {
            player.startBall();
        }
    };
    
    protected final static JumpMode JUMP_GRAPPLING_HOOK = new JumpMode(Profile.UPGRADE_GRAPPLING_BEAM) {
        @Override
        protected final void onAirJump(final Player player) {
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
        protected final void onAirJump(final Player player) {
            player.startSpring();
        }
    };
    
    private final static JumpMode[] JUMP_MODES = { JUMP_NORMAL, JUMP_BALL, JUMP_SPRING, JUMP_GRAPPLING_HOOK };
    
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
            final int index = tm.getContainer(this);
            final int x = tm.getColumn(index), y = tm.getRow(index);
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
            final Panlayer layer = getLayer();
            if (layer == null) {
                return;
            }
            final Panmage img = pi.warp;
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY(), z = pos.getZ();
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
    
    public static interface SpecProjectile extends SpecPanctor {
        public void assignPower(final int power);
        
        public PlayerImages getPlayerImages();
        
        public void burst();
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
        return getPosition().getX() < (BotsnBoltsGame.GAME_W / 2);
    }
}
