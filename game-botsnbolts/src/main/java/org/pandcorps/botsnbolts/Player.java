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

public final class Player extends Chr {
    protected final static int PLAYER_X = 6;
    protected final static int PLAYER_H = 23;
    protected final static int BALL_H = 15;
    private final static int SHOOT_DELAY_DEFAULT = 5;
    private final static int SHOOT_DELAY_RAPID = 3;
    private final static int SHOOT_DELAY_SPREAD = 15;
    private final static int SHOOT_TIME = 12;
    private final static int CHARGE_TIME_MEDIUM = 30;
    private final static int CHARGE_TIME_BIG = 60;
    private final static int INVINCIBLE_TIME = 60;
    private final static int HURT_TIME = 15;
    private final static int RUN_TIME = 5;
    protected final static int VEL_JUMP = 8;
    protected final static int VEL_BOUNCE_BOMB = 7;
    private final static int VEL_WALK = 3;
    protected final static int VEL_PROJECTILE = 8;
    private final static float VX_SPREAD1;
    private final static float VY_SPREAD1;
    private final static float VX_SPREAD2;
    private final static float VY_SPREAD2;
    
    protected final PlayerContext pc;
    protected final Profile prf;
    protected final PlayerImages pi;
    protected StateHandler stateHandler = NORMAL_HANDLER;
    private boolean running = false;
    private int runIndex = 0;
    private int runTimer = 0;
    private int blinkTimer = 0;
    private long lastShot = -1000;
    private long startCharge = -1000;
    private long lastCharge = -1000;
    private long lastHurt = -1000;
    private long lastJump = -1000;
    private int wallTimer = 0;
    private boolean wallMirror = false;
    private int health = HudMeter.MAX_VALUE;
    private GrapplingHook grapplingHook = null;
    private double grapplingR = 0;
    private double grapplingT = 0;
    private double grapplingV = 0;
    
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
        registerInputs(pc.ctrl);
        setView(pi.basicSet.stand);
    }
    
    private final void registerInputs(final ControlScheme ctrl) {
        final Panput jumpInput = ctrl.get1();
        final Panput shootInput = ctrl.get2();
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
        register(ctrl.getRight(), new ActionListener() {
            @Override public final void onAction(final ActionEvent event) { right(); }});
        register(ctrl.getLeft(), new ActionListener() {
            @Override public final void onAction(final ActionEvent event) { left(); }});
        register(ctrl.getUp(), new ActionListener() { //TODO Display up/down touch buttons when near ladder, hide otherwise
            @Override public final void onAction(final ActionEvent event) { up(); }});
        register(ctrl.getDown(), new ActionListener() {
            @Override public final void onAction(final ActionEvent event) { down(); }});
        registerPause(ctrl.getSubmit());
        registerPause(ctrl.getMenu());
        final Pangine engine = Pangine.getEngine();
        final Panteraction interaction = engine.getInteraction();
        register(interaction.KEY_TAB, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { toggleShootMode(); }});
        register(interaction.KEY_F1, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { engine.captureScreen(); }});
        register(interaction.KEY_F2, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { engine.startCaptureFrames(); }});
        register(interaction.KEY_F3, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { engine.stopCaptureFrames(); }});
        register(interaction.KEY_U, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { up(); }});
        register(interaction.KEY_D, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { down(); }});
    }
    
    private final void registerPause(final Panput input) {
        register(input, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { togglePause(); }});
    }
    
    private final void togglePause() {
        Pangine.getEngine().togglePause();
    }
    
    private final void toggleShootMode() {
        do {
            if (prf.shootMode == SHOOT_NORMAL) {
                prf.shootMode = SHOOT_RAPID;
            } else if (prf.shootMode == SHOOT_RAPID) {
                prf.shootMode = SHOOT_SPREAD;
            } else if (prf.shootMode == SHOOT_SPREAD) {
                prf.shootMode = SHOOT_CHARGE;
            } else {
                prf.shootMode = SHOOT_NORMAL;
            }
        } while (!prf.shootMode.isAvailable(this));
    }
    
    private final boolean isFree() {
        return !(isHurt() || RoomChanger.isChanging());
    }
    
    private final void jump() {
        if (isFree()) {
            stateHandler.onJump(this);
        }
    }
    
    private final void onJumpNormal() {
        if (isGrounded()) {
            v = VEL_JUMP;
            lastJump = Pangine.getEngine().getClock();
        } else {
            stateHandler.onAirJump(this);
        }
    }
    
    private final void releaseJump() {
        if (v > 0) {
            v = 0;
        }
    }
    
    private final void shoot() {
        if (isFree()) {
            stateHandler.onShootStart(this);
        }
    }
    
    private final void shooting() {
        if (isFree()) {
            stateHandler.onShooting(this);
        }
    }
    
    private final void releaseShoot() {
        if (isFree()) {
            stateHandler.onShootEnd(this);
        }
    }
    
    private final void right() {
        if (isFree()) {
            stateHandler.onRight(this);
        }
    }
    
    private final void onRightNormal() {
        hv = VEL_WALK;
    }
    
    private final void left() {
        if (isFree()) {
            stateHandler.onLeft(this);
        }
    }
    
    private final void onLeftNormal() {
        hv = -VEL_WALK;
    }
    
    private final void up() {
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
        final TileMap tm = BotsnBoltsGame.tm;
        final int tileIndex = tm.getContainer(pos.getX(), pos.getY() + yoff);
        final byte b = Tile.getBehavior(tm.getTile(tileIndex));
        return b == BotsnBoltsGame.TILE_LADDER || b == BotsnBoltsGame.TILE_LADDER_TOP;
    }
    
    private final void down() {
        if (isFree()) {
            stateHandler.onDown(this);
        }
    }
    
    private final void onDownNormal() {
        if (isGrounded() && isTouchingLadder(-1)) {
            startLadder();
            getPosition().addY(-OFF_LADDER_BOTTOM);
        }
    }
    
    protected final void hurt(final int damage) {
        if (isInvincible()) {
            return;
        }
        stateHandler.onHurt(this);
        lastHurt = Pangine.getEngine().getClock();
        health -= damage;
        if ((v > 0) && !isGrounded()) {
            v = 0;
        }
        startCharge = -1000;
        lastCharge = -1000;
        if (health <= 0) {
            defeat();
        } else {
            burst(BotsnBoltsGame.flash, 0, 11, BotsnBoltsGame.DEPTH_POWER_UP);
            puff(-12, 25);
            puff(0, 30);
            puff(12, 25);
        }
    }
    
    protected final void puff(final int offX, final int offY) {
        burst(BotsnBoltsGame.puff, offX, offY, BotsnBoltsGame.DEPTH_BURST);
    }
    
    protected final void burst(final Panimation anm, final int offX, final int offY, final int z) {
        final Burst puff = new Burst(anm);
        final Panple playerPos = getPosition();
        puff.getPosition().set(playerPos.getX() + offX, playerPos.getY() + offY, z);
        addActor(puff);
    }
    
    protected final void defeat() {
        final float baseVelDiag = (float) Math.sqrt(0.5);
        for (int m = 1; m < 3; m++) {
            final float velDiag = m * baseVelDiag, vel = m;
            defeatOrb(0, vel);
            defeatOrb(velDiag, velDiag);
            defeatOrb(vel, 0);
            defeatOrb(velDiag, -velDiag);
            defeatOrb(0, -vel);
            defeatOrb(-velDiag, -velDiag);
            defeatOrb(-vel, 0);
            defeatOrb(-velDiag, velDiag);
        }
        Pangine.getEngine().addTimer(BotsnBoltsGame.tm, 120, new TimerListener() {
            @Override public final void onTimer(final TimerEvent event) {
                RoomLoader.reloadCurrentRoom();
            }});
        destroy();
    }
    
    @Override
    protected final void onDestroy() {
        destroyGrapplingHook();
        super.onDestroy();
    }
    
    private final void defeatOrb(final float velX, final float velY) {
        final DefeatOrb orb = new DefeatOrb();
        orb.setView(pi.defeat);
        final Panple playerPos = getPosition();
        orb.getPosition().set(playerPos.getX(), playerPos.getY() + 12, BotsnBoltsGame.DEPTH_BURST);
        orb.getVelocity().set(velX, velY);
        addActor(orb);
    }
    
    protected final Panlayer getLayerRequired() {
        final Panlayer layer = getLayer();
        return (layer == null) ? BotsnBoltsGame.getLayer() : layer;
    }
    
    protected final void addActor(final Panctor actor) {
        getLayerRequired().addActor(actor);
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
        return (Pangine.getEngine().getClock() - lastHurt) < HURT_TIME;
    }
    
    private final boolean isInvincible() {
        return (Pangine.getEngine().getClock() - lastHurt) < INVINCIBLE_TIME;
    }
    
    private final boolean isShootPoseNeeded() {
        return (Pangine.getEngine().getClock() - lastShot) < SHOOT_TIME;
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
    
    @Override
    protected final boolean onStepCustom() {
        if (isInvincible()) {
            setVisible(Pangine.getEngine().isOn(4));
        } else {
            setVisible(true);
        }
        if (RoomChanger.isChanging()) {
            final RoomChanger changer = RoomChanger.getActiveChanger();
            if (changer.getAge() <= 28) {
                hv = getDirection(changer.getVelocityX());
                v = getDirection(changer.getVelocityY());
            } else {
                hv = 0;
                v = 0;
            }
        }
        if (stateHandler.onStep(this)) {
            return true;
        }
        return false;
    }
    
    protected final void onStepGrappling() {
        if (grapplingR <= 0) {
            destroyGrapplingHook();
            return;
        }
        final double grapplingA = -getG() * Math.sin(grapplingT) / grapplingR;
        grapplingV += grapplingA;
        grapplingT += grapplingV;
        final Panple gPos = grapplingHook.getPosition();
        final double offT = grapplingT + (Math.PI / 2);
        final double grapplingX = gPos.getX() + (Math.cos(offT) * grapplingR);
        final double grapplingY = gPos.getY() + (Math.sin(offT) * grapplingR);
        moveTo((int) Math.round(grapplingX), (int) Math.round(grapplingY));
    }
    
    @Override
    protected final void onStepEnd() {
        hv = 0;
    }
    
    @Override
    protected final void onGrounded() {
        if (isHurt()) {
            changeView(pi.hurt);
            return;
        }
        this.stateHandler.onGrounded(this);
    }
    
    private final void onGroundedNormal() {
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
            if (wallTimer > 0 && set.crouch != null && !RoomChanger.isChanging()) { //TODO && room for ball
                if (wallMirror == isMirror()) {
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
    
    private final void onGroundedBall() {
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
    
    private final void startBall() {
        if (!isUpgradeAvailable(Profile.UPGRADE_BALL)) {
            return;
        }
        clearRun();
        stateHandler = BALL_HANDLER;
        changeView(pi.ball[0]);
        setH(BALL_H);
        wallTimer = 0;
    }
    
    private final void endBall() {
        clearRun();
        stateHandler = NORMAL_HANDLER;
        setH(PLAYER_H);
    }
    
    private final void endLadder() {
        clearRun();
        stateHandler = NORMAL_HANDLER;
    }
    
    private final void startGrapple() {
        destroyGrapplingHook();
        grapplingHook = new GrapplingHook(this);
        grapplingV = 0;
        stateHandler = GRAPPLING_HANDLER;
    }
    
    protected final void onGrappleConnected() {
        if (grapplingHook == null) {
            return;
        }
        grapplingV = 0;
        final Panple pos = getPosition();
        final Panple dir = Panple.subtract(pos, grapplingHook.getPosition());
        dir.set(dir.getY(), dir.getX());
        final double mag = dir.getMagnitude2();
        if (mag <= 0) {
            destroyGrapplingHook();
            return;
        }
        grapplingR = mag;
        dir.multiply((float) (1.0 / mag));
        grapplingT = Math.acos(dir.getX());
        if (isMirror()) {
            grapplingT = -grapplingT;
        }
    }
    
    private final void endGrapple() {
        clearRun();
        stateHandler = NORMAL_HANDLER;
        destroyGrapplingHook();
    }
    
    private final void destroyGrapplingHook() {
        if (grapplingHook != null) {
            grapplingHook.destroy();
            grapplingHook = null;
        }
    }
    
    @Override
    protected final void onLanded() {
        super.onLanded();
        blinkTimer = 0;
    }
    
    @Override
    protected final boolean onAir() {
        return stateHandler.onAir(this);
    }
    
    private final boolean onAirNormal() {
        wallTimer = 0;
        clearRun();
        if (isHurt()) {
            changeView(pi.hurt);
            return false;
        }
        changeView(getCurrentImagesSubSet().jump);
        return false;
    }
    
    @Override
    protected final void onWall(final byte xResult) {
        stateHandler.onWall(this, xResult);
    }
    
    protected final boolean triggerBossDoor() {
        final BossDoor bossDoor = RoomLoader.bossDoor;
        if (bossDoor == null) {
            return false;
        } else if (bossDoor.isOpening()) {
            return false;
        }
        bossDoor.open(); //TODO Make sure Player is adjacent to it
        return true;
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
        changeRoom(0, 1);
    }
    
    @Override
    protected final boolean onFell() {
        if (changeRoom(0, -1)) {
            return true;
        }
        defeat();
        return true;
    }
    
    private final boolean changeRoom(final int dirX, final int dirY) {
        final BotRoomCell roomCell = RoomLoader.getAdjacentRoom(this, dirX, dirY);
        if (roomCell == null) {
            return false;
        }
        final BotRoom room = roomCell.room;
        final int nextX = (roomCell.cell.x - room.x) * BotsnBoltsGame.GAME_W;
        RoomLoader.clear();
        new RoomChanger(nextX, 0, 10 * dirX, 10 * dirY, null, Arrays.asList(BotsnBoltsGame.hud), Arrays.asList(this, BotsnBoltsGame.tm), Arrays.asList(BotsnBoltsGame.tm)) {
            @Override
            protected final Panroom createRoom() {
                return loadRoom(room);
            }
            
            @Override
            protected final void onFinished() {
                if (BotsnBoltsGame.timgPrev != null) {
                    BotsnBoltsGame.timgPrev.destroy();
                    BotsnBoltsGame.timgPrev = null;
                }
                RoomLoader.onChangeFinished();
            }
        };
        return true;
    }
    
    protected final static Panroom loadRoom(final BotRoom room) {
        //final RoomLoader loader = new DemoRoomLoader();
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
        return new HudMeter(pi.hudMeterImages) {
            @Override protected final int getValue() {
                return health;
            }};
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
        
        protected abstract void onGrounded(final Player player);
        
        protected abstract boolean onAir(final Player player);
        
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
        protected final boolean onStep(final Player player) {
            final float v = player.v;
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
            SHOOT_BOMB.onShootStart(player);
        }
        
        @Override
        protected final void onShooting(final Player player) {
        }
        
        @Override
        protected final void onShootEnd(final Player player) {
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
        protected final void onGrounded(final Player player) {
            player.onGroundedBall();
        }
        
        @Override
        protected final boolean onAir(final Player player) {
            if ((Pangine.getEngine().getClock() - player.lastJump) <= 1) {
                player.endBall();
            }
            return false;
        }
    };
    
    protected final static StateHandler CARRIED_HANDLER = new StateHandler() {
        @Override
        protected final void onJump(final Player player) {
            player.onJumpNormal();
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
            player.setMirror(false);
        }
        
        @Override
        protected final void onLeft(final Player player) {
            player.setMirror(true);
        }
        
        @Override
        protected final boolean onStep(final Player player) {
            // The Carrier moves the Player, so don't need to do that here
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
    };
    
    protected final static StateHandler GRAPPLING_HANDLER = new StateHandler() {
        @Override
        protected final void onJump(final Player player) {
            player.onJumpNormal();
        }
        
        @Override
        protected final void onAirJump(final Player player) {
            player.endGrapple();
        }
        
        @Override
        protected final void onShootStart(final Player player) {
            //TODO Retract
        }
        
        @Override
        protected final void onShooting(final Player player) {
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
            //TODO Change speed
        }
        
        @Override
        protected final void onLeft(final Player player) {
            if (!isConnected(player)) {
                player.onLeftNormal();
                return;
            }
        }
        
        private final boolean isConnected(final Player player) {
            return (player.grapplingHook != null) && player.grapplingHook.finished;
        }
        
        @Override
        protected final boolean onStep(final Player player) {
            if (!isConnected(player)) {
                return false;
            }
            player.onStepGrappling();
            return true;
        }
        
        @Override
        protected final void onGrounded(final Player player) {
            player.endGrapple();
        }
        
        @Override
        protected final boolean onAir(final Player player) {
            return player.onAirNormal();
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
    
    protected abstract static class ShootMode {
        protected final int delay;
        
        protected ShootMode(final int delay) {
            this.delay = delay;
        }
        
        protected abstract Upgrade getRequiredUpgrade();
        
        protected final boolean isAvailable(final Player player) {
            return player.isUpgradeAvailable(getRequiredUpgrade());
        }
        
        protected abstract void onShootStart(final Player player);
        
        //@OverrideMe
        protected void onShooting(final Player player) {
        }
        
        //@OverrideMe
        protected void onShootEnd(final Player player) {
        }
        
        protected final void shoot(final Player player) {
            final long clock = Pangine.getEngine().getClock();
            if (clock - player.lastShot > delay) {
                player.lastShot = clock;
                createProjectile(player);
                player.blinkTimer = 0;
            }
        }
        
        protected abstract void createProjectile(final Player player);
        
        protected final void createDefaultProjectile(final Player player) {
            createBasicProjectile(player, VEL_PROJECTILE, 0);
        }
        
        protected final void createBasicProjectile(final Player player, final float vx, final float vy) {
            new Projectile(player, vx, vy, 1);
        }
        
        protected final void shootSpecial(final Player player, final int power) {
            player.lastShot = Pangine.getEngine().getClock();
            new Projectile(player, VEL_PROJECTILE, 0, power);
            player.blinkTimer = 0;
        }
    }
    
    protected final static class PlayerContext {
        protected final Profile prf;
        protected final ControlScheme ctrl;
        protected final PlayerImages pi;
        protected Player player = null;
        
        protected PlayerContext(final Profile prf, final ControlScheme ctrl, final PlayerImages pi) {
            this.prf = prf;
            this.ctrl = ctrl;
            this.pi = pi;
        }
        
        protected final static Player getPlayer(final PlayerContext pc) {
            return (pc == null) ? null : pc.player;
        }
    }
    
    protected final static class PlayerImages {
        private final PlayerImagesSubSet basicSet;
        private final PlayerImagesSubSet shootSet;
        private final Panmage hurt;
        private final Panimation defeat;
        private final Panmage climb;
        private final Panmage climbShoot;
        private final Panmage climbTop;
        protected final Panmage basicProjectile;
        protected final Panimation projectile2;
        protected final Panimation projectile3;
        private final Panimation charge;
        private final Panimation chargeVert;
        private final Panimation charge2;
        private final Panimation chargeVert2;
        protected final Panimation burst;
        private final Panmage[] ball;
        private final Panmage warp;
        private final Panimation materialize;
        protected final Panimation bomb;
        protected final Panmage link;
        protected final Panimation batterySmall;
        protected final Panimation batteryMedium;
        protected final Panimation batteryBig;
        protected final Panmage bolt;
        protected final Panmage byteDisk;
        protected final Panmage powerBox;
        //protected final Panmage boltBox; // Each bolt has a unique box image
        protected final Panmage byteBox;
        private final HudMeterImages hudMeterImages;
        
        protected PlayerImages(final PlayerImagesSubSet basicSet, final PlayerImagesSubSet shootSet, final Panmage hurt, final Panimation defeat,
                               final Panmage climb, final Panmage climbShoot, final Panmage climbTop,
                               final Panmage basicProjectile, final Panimation projectile2, final Panimation projectile3,
                               final Panimation charge, final Panimation chargeVert, final Panimation charge2, final Panimation chargeVert2,
                               final Panimation burst, final Panmage[] ball, final Panmage warp, final Panimation materialize, final Panimation bomb,
                               final Panmage link, final Panimation batterySmall, final Panimation batteryMedium, final Panimation batteryBig,
                               final Panmage bolt, final Panmage byteDisk,
                               final Panmage powerBox, final Panmage byteBox, final HudMeterImages hudMeterImages) {
            this.basicSet = basicSet;
            this.shootSet = shootSet;
            this.hurt = hurt;
            this.defeat = defeat;
            this.climb = climb;
            this.climbShoot = climbShoot;
            this.climbTop = climbTop;
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
            this.bomb = bomb;
            this.link = link;
            this.batterySmall = batterySmall;
            this.batteryMedium = batteryMedium;
            this.batteryBig = batteryBig;
            this.bolt = bolt;
            this.byteDisk = byteDisk;
            this.powerBox = powerBox;
            this.byteBox = byteBox;
            this.hudMeterImages = hudMeterImages;
        }
    }
    
    protected final static class PlayerImagesSubSet {
        private final Panmage stand;
        private final Panmage jump;
        private final Panmage[] run;
        private final Panmage start;
        private final Panmage blink;
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
    
    protected final static ShootMode SHOOT_NORMAL = new ShootMode(SHOOT_DELAY_DEFAULT) {
        @Override
        protected final Upgrade getRequiredUpgrade() {
            return null;
        }
        
        @Override
        protected final void onShootStart(final Player player) {
            shoot(player);
        }
        
        @Override
        protected final void createProjectile(final Player player) {
            createDefaultProjectile(player);
        }
    };
    
    protected final static ShootMode SHOOT_RAPID = new ShootMode(SHOOT_DELAY_RAPID) {
        @Override
        protected final Upgrade getRequiredUpgrade() {
            return Profile.UPGRADE_RAPID;
        }
        
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
    
    protected final static ShootMode SHOOT_SPREAD = new ShootMode(SHOOT_DELAY_SPREAD) {
        @Override
        protected final Upgrade getRequiredUpgrade() {
            return Profile.UPGRADE_SPREAD;
        }
        
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
    
    protected final static ShootMode SHOOT_CHARGE = new ShootMode(SHOOT_DELAY_DEFAULT) {
        @Override
        protected final Upgrade getRequiredUpgrade() {
            return Profile.UPGRADE_CHARGE;
        }
        
        @Override
        protected final void onShootStart(final Player player) {
            shoot(player);
            final long clock = Pangine.getEngine().getClock();
            player.startCharge = clock;
            player.lastCharge = clock;
        }
        
        @Override
        protected final void onShooting(final Player player) {
            final long clock = Pangine.getEngine().getClock();
            if (clock - player.lastCharge > 2) {
                player.startCharge = clock;
            }
            player.lastCharge = clock;
            final long diff = clock - player.startCharge;
            if (diff > CHARGE_TIME_MEDIUM) {
                player.blinkTimer = 0;
                final PlayerImages pi = player.pi;
                if (diff > CHARGE_TIME_BIG) {
                    charge(player, pi.charge2, pi.chargeVert2);
                } else {
                    charge(player, pi.charge, pi.chargeVert);
                }
            }
        }
        
        private final void charge(final Player player, final Panimation diag, final Panimation vert) {
            final long c = Pangine.getEngine().getClock() % 8;
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
            final long diff = Pangine.getEngine().getClock() - player.startCharge;
            if (diff > CHARGE_TIME_BIG) {
                shootSpecial(player, Projectile.POWER_MAXIMUM);
            } else if (diff > CHARGE_TIME_MEDIUM) {
                shootSpecial(player, Projectile.POWER_MEDIUM);
            }
        }
        
        @Override
        protected final void createProjectile(final Player player) {
            createDefaultProjectile(player);
        }
    };
    
    protected final static ShootMode SHOOT_BOMB = new ShootMode(SHOOT_DELAY_DEFAULT) {
        @Override
        protected final Upgrade getRequiredUpgrade() {
            return Profile.UPGRADE_BOMB;
        }
        
        @Override
        protected final void onShootStart(final Player player) {
            shoot(player);
        }
        
        @Override
        protected final void createProjectile(final Player player) {
            new Bomb(player);
        }
    };
    
    protected abstract static class JumpMode {
        protected abstract void onAirJump(final Player player);
    }
    
    protected final static JumpMode JUMP_NORMAL = new JumpMode() {
        @Override
        protected final void onAirJump(final Player player) {
        }
    };
    
    protected final static JumpMode JUMP_GRAPPLING_HOOK = new JumpMode() {
        @Override
        protected final void onAirJump(final Player player) {
            player.startGrapple();
            //TODO Maybe holding jump until highest point could also trigger grappling
        }
    };
    
    protected final static class Warp extends Panctor implements StepListener {
        protected final Player player;
        
        protected Warp(final Player player) {
            this.player = player;
            final Panple ppos = player.getPosition();
            getPosition().set(ppos.getX(), BotsnBoltsGame.SCREEN_H, BotsnBoltsGame.DEPTH_PLAYER);
            player.addActor(this);
            player.stateHandler = WARP_HANDLER;
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panmage img = player.pi.warp;
            final Panlayer layer = getLayer();
            if (layer == null) {
                return;
            }
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY(), z = pos.getZ();
            for (int i = 0; i < 4; i++) {
                renderer.render(layer, img, x, y + (i * 8), z);
            }
        }

        @Override
        public final void onStep(final StepEvent event) {
            final Panple pos = getPosition();
            pos.addY(-16);
            final float py = player.getPosition().getY();
            if (pos.getY() <= py) {
                pos.setY(py);
                finish();
            }
        }
        
        protected final void finish() {
            new Materialize(player);
            destroy();
        }
    }
    
    protected final static class Materialize extends Panctor implements AnimationEndListener {
        protected final Player player;
        
        protected Materialize(final Player player) {
            this.player = player;
            setView(player.pi.materialize);
            getPosition().set(player.getPosition());
            player.addActor(this);
        }
        
        @Override
        public final void onAnimationEnd(final AnimationEndEvent event) {
            finish();
        }
        
        protected final void finish() {
            player.stateHandler = NORMAL_HANDLER;
            destroy();
        }
    }
    
    protected final static class DefeatOrb extends Pandy implements AllOobListener {
        @Override
        public final void onAllOob(final AllOobEvent event) {
            destroy();
        }
    }
}
