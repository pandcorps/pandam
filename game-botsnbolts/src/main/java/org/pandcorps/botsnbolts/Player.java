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

import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.tile.*;

public final class Player extends GuyPlatform {
    protected final static int PLAYER_X = 6;
    protected final static int PLAYER_H = 23;
    protected final static int BALL_H = 15;
    private final static int SHOOT_DELAY_DEFAULT = 5;
    private final static int SHOOT_DELAY_RAPID = 3;
    private final static int SHOOT_DELAY_SPREAD = 15;
    private final static int SHOOT_TIME = 12;
    private final static int INVINCIBLE_TIME = 60;
    private final static int HURT_TIME = 20;
    private final static int RUN_TIME = 5;
    private final static int VEL_JUMP = 8;
    private final static int VEL_WALK = 3;
    private final static int VEL_PROJECTILE = 8;
    private final static float VX_SPREAD1;
    private final static float VY_SPREAD1;
    private final static float VX_SPREAD2;
    private final static float VY_SPREAD2;
    
    private final Profile prf;
    protected final PlayerImages pi;
    private StateHandler stateHandler = NORMAL_HANDLER;
    private boolean running = false;
    private int runIndex = 0;
    private int runTimer = 0;
    private int blinkTimer = 0;
    private long lastShot = -1000;
    private long lastHurt = -1000;
    private int wallTimer = 0;
    private boolean wallMirror = false;
    private int health = 28; //TODO HUD meter
    
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
        this.prf = pc.prf;
        this.pi = pc.pi;
        registerInputs(pc.ctrl);
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
    }
    
    private final void registerPause(final Panput input) {
        register(input, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { togglePause(); }});
    }
    
    private final void togglePause() {
        Pangine.getEngine().togglePause();
    }
    
    private final void toggleShootMode() {
        if (prf.shootMode == SHOOT_NORMAL) {
            prf.shootMode = SHOOT_RAPID;
        } else if (prf.shootMode == SHOOT_RAPID) {
            prf.shootMode = SHOOT_SPREAD;
        } else if (prf.shootMode == SHOOT_SPREAD) {
            prf.shootMode = SHOOT_CHARGE;
        } else {
            prf.shootMode = SHOOT_NORMAL;
        }
    }
    
    private final void jump() {
        if (isGrounded()) {
            v = VEL_JUMP;
        }
    }
    
    private final void releaseJump() {
        if (v > 0) {
            v = 0;
        }
    }
    
    private final void shoot() {
        stateHandler.onShootStart(this);
    }
    
    private final void shooting() {
        stateHandler.onShooting(this);
    }
    
    private final void releaseShoot() {
        stateHandler.onShootEnd(this);
    }
    
    private final void right() {
        stateHandler.onRight(this);
    }
    
    private final void onRightNormal() {
        hv = VEL_WALK;
    }
    
    private final void left() {
        stateHandler.onLeft(this);
    }
    
    private final void onLeftNormal() {
        hv = -VEL_WALK;
    }
    
    private final void up() {
        stateHandler.onUp(this);
    }
    
    private final void onUpNormal() {
        //TODO Switch to LADDER_HANDLER if on ladder
    }
    
    private final void down() {
        stateHandler.onDown(this);
    }
    
    private final boolean isHurt() {
        return (Pangine.getEngine().getClock() - lastHurt) < HURT_TIME;
    }
    
    private final boolean isInvincible() {
        return (Pangine.getEngine().getClock() - lastHurt) < INVINCIBLE_TIME;
    }
    
    private final PlayerImagesSubSet getCurrentImagesSubSet() {
        return ((Pangine.getEngine().getClock() - lastShot) < SHOOT_TIME) ? pi.shootSet : pi.basicSet;
    }
    
    private final void clearRun() {
        runIndex = 0;
        runTimer = 0;
    }
    
    @Override
    protected final boolean onStepCustom() {
        if (isInvincible()) {
            setVisible(Pangine.getEngine().isOn(4));
        } else {
            setVisible(true);
        }
        return false;
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
            if (wallTimer > 0 && set.crouch != null) { //TODO && room for ball
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
    protected final void onWall() {
        stateHandler.onWall(this);
    }
    
    @Override
    protected final boolean onFell() {
        return false;
    }

    @Override
    protected final void onBump(final int t) {
    }

    @Override
    protected final TileMap getTileMap() {
        return BotsnBoltsGame.tm;
    }

    @Override
    protected boolean isSolidBehavior(final byte b) {
        return false;
    }
    
    protected abstract static class StateHandler {
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
        
        protected abstract void onGrounded(final Player player);
        
        protected abstract boolean onAir(final Player player);
        
        //@OverrideMe
        protected void onWall(final Player player) {
        }
    }
    
    protected final static StateHandler NORMAL_HANDLER = new StateHandler() {
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
        protected final void onGrounded(final Player player) {
            player.onGroundedNormal();
        }
        
        @Override
        protected final boolean onAir(final Player player) {
            return player.onAirNormal();
        }
        
        @Override
        protected final void onWall(final Player player) {
            if (player.wallTimer == 0) {
                player.wallTimer = 1;
                player.wallMirror = player.isMirror();
            }
        }
    };
    
    protected final static StateHandler LADDER_HANDLER = new StateHandler() {
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
            //TODO Aim right
        }
        
        @Override
        protected final void onLeft(final Player player) {
            //TODO Aim left
        }
        
        @Override
        protected final void onUp(final Player player) {
            //TODO Climb up
        }
        
        @Override
        protected final void onDown(final Player player) {
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
            player.endBall();
            return false;
        }
    };
    
    protected abstract static class ShootMode {
        protected final int delay;
        
        protected ShootMode(final int delay) {
            this.delay = delay;
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
            new Projectile(player, vx, vy).setView(player.pi.basicProjectile);
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
    }
    
    protected final static class PlayerImages {
        private final PlayerImagesSubSet basicSet;
        private final PlayerImagesSubSet shootSet;
        private final Panmage hurt;
        private final Panmage basicProjectile;
        protected final Panimation burst;
        private final Panmage[] ball;
        
        protected PlayerImages(final PlayerImagesSubSet basicSet, final PlayerImagesSubSet shootSet, final Panmage hurt,
                               final Panmage basicProjectile, final Panimation burst, final Panmage[] ball) {
            this.basicSet = basicSet;
            this.shootSet = shootSet;
            this.hurt = hurt;
            this.basicProjectile = basicProjectile;
            this.burst = burst;
            this.ball = ball;
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
        protected final void onShootStart(final Player player) {
            shoot(player);
        }
        
        @Override
        protected final void onShootEnd(final Player player) {
            //TODO Shoot charged shot if ready
        }
        
        @Override
        protected final void createProjectile(final Player player) {
            createDefaultProjectile(player);
        }
    };
}
