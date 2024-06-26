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

import org.pandcorps.botsnbolts.Enemy.*;
import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.botsnbolts.ShootableDoor.*;
import org.pandcorps.core.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.boundary.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.*;

public class Projectile extends Pandy implements AllOobListener, SpecPlayerProjectile, StepEndListener {
    protected final static int POWER_BOMB = 1;
    protected final static int POWER_MEDIUM = 3;
    protected final static int POWER_MAXIMUM = 5;
    protected final static int POWER_SHIELD = 2;
    protected final static int POWER_IMPOSSIBLE = Integer.MAX_VALUE;
    protected final static int OFF_X = 15;
    protected final static int OFF_Y = 13;
    protected final static Set<Projectile> currentProjectiles = new HashSet<Projectile>();
    private static Panple scratch = new ImplPanple();
    
    protected final Player src;
    protected final PlayerImages pi;
    protected final ShootMode shootMode;
    protected int power;
    private boolean stopped = false;
    
    protected Projectile(final Player src, final float vx, final float vy, final int power) {
        this(src, src.pi, src.prf.shootMode, src, vx, vy, power);
    }
    
    protected Projectile(final Player src, final PlayerImages pi, final ShootMode shootMode, final Panctor ref, final float vx, final float vy, final int power) {
        currentProjectiles.add(this);
        this.src = src;
        this.pi = pi;
        this.shootMode = shootMode;
        init(this, this, src, pi, shootMode, ref, vx, vy, power);
        if (power > POWER_MEDIUM) {
            BotsnBoltsGame.fxSuperChargedAttack.startSound();
        } else if (power > 1) {
            BotsnBoltsGame.fxChargedAttack.startSound();
        } else {
            BotsnBoltsGame.fxAttack.startSound();
        }
    }
    
    protected static boolean autoAddProjectile = true;
    
    protected final static void init(final SpecProjectile sp, final Pandy prj, final Player src, final PlayerImages pi, final ShootMode shootMode, final Panctor ref, final float vx, final float vy, final int power) {
        prj.setView(pi.basicProjectile);
        setPower(sp, power);
        initPosition(prj, src, ref.getPosition(), src.isAimMirrorReversed() ? !ref.isMirror() : ref.isMirror(), vx, vy);
        if (autoAddProjectile) {
            ref.getLayer().addActor(prj);
        }
    }
    
    protected final static void initPosition(final Pandy prj, final Player src, final Panple srcPos, final boolean mirror, final float vx, final float vy) {
        prj.setMirror(mirror);
        final int xm = prj.getMirrorMultiplier();
        prj.getPosition().set(srcPos.getX() + (xm * src.getAimOffsetX()), srcPos.getY() + src.getAimOffsetY(), BotsnBoltsGame.DEPTH_PROJECTILE);
        prj.getVelocity().set(xm * vx, vy);
    }
    
    @Override
    public final Player getSource() {
        return src;
    }
    
    @Override
    public final int getPower() {
        return power;
    }
    
    protected final void setPower(final int power) {
        setPower(this, power);
    }
    
    protected final static void setPower(final SpecProjectile prj, final int power) {
        prj.assignPower(power);
        if (power > 0) {
            prj.getShootMode().onAssignPower(prj, power);
        } else {
            prj.burst();
        }
    }
    
    @Override
    public final void assignPower(final int power) {
        this.power = power;
    }
    
    @Override
    public final PlayerImages getPlayerImages() {
        return pi;
    }
    
    @Override
    public final ShootMode getShootMode() {
        return shootMode;
    }
    
    @Override
    public void burst() {
        burst(this);
        destroy();
    }
    
    protected final void burst(final SpecPanctor target) {
        burst(target.getPosition());
    }
    
    protected final void burst(final Panple loc) {
        burst(this, pi.burst, loc);
    }
    
    protected final static void burst(final SpecPanctor src, final Panimation anm, final Panple loc) {
        burst(src, anm, loc.getX(), loc.getY());
    }
    
    protected final static void burst(final SpecPanctor src, final Panimation anm, final float x, final float y) {
        final Burst burst = new Burst(anm);
        final Panple pos = burst.getPosition();
        pos.set(x, y, BotsnBoltsGame.DEPTH_BURST);
        burst.setMirror(src.isMirror());
        BotsnBoltsGame.addActor(burst);
    }
    
    @Override
    public void bounce() {
        new Bounce(this);
    }
    
    @Override
    public float getVelocityX() {
        return getVelocity().getX();
    }
    
    @Override
    public boolean isEnemyBurstAlwaysNeeded() {
        return false;
    }

    @Override
    public void onAllOob(final AllOobEvent event) {
        if (isDestructionWhenOobNeeded()) {
            destroy();
        }
    }
    
    @Override
    public final void onStep(final StepEvent event) {
        if (!stopped) {
            super.onStep(event);
        }
        if (isDestructionWhenOobNeeded() && !isInView()) { // onAllOob above checks the whole room, not just the current view
            destroy();
        }
    }
    
    protected boolean isDestructionWhenOobNeeded() {
        return true;
    }
    
    @Override
    public final void onStepEnd(final StepEndEvent event) {
        stopped = src.isStopped();
        onStepEndProjectile();
    }
    
    //@OverrideMe
    protected void onStepEndProjectile() {
    }
    
    @Override
    public final void onDestroy() {
        currentProjectiles.remove(this);
        super.onDestroy();
    }
    
    public static class Bomb extends Panctor implements StepListener {
        protected final Player src;
        private int timer = 30;
        
        protected Bomb(final Player src) {
            this.src = src;
            final Panple srcPos = src.getPosition();
            getPosition().set(srcPos.getX(), srcPos.getY() + 7, BotsnBoltsGame.DEPTH_PROJECTILE);
            setMirror(src.isMirror());
            setView(src.pi.bomb);
            src.getLayer().addActor(this);
        }

        @Override
        public final void onStep(final StepEvent event) {
            if (src.isStopped()) {
                return;
            }
            timer--;
            if (timer <= 0) {
                newExplosion();
                destroy();
            }
        }
        
        protected void newExplosion() {
            new Explosion(this);
        }
    }
    
    public static class Explosion extends Projectile implements AnimationEndListener {
        protected Explosion(final Bomb bomb) {
            this(bomb.src, bomb);
        }
        
        protected Explosion(final Player src, final Panctor ref) {
            super(src, src.pi, Player.SHOOT_BOMB, ref, 0, 0, POWER_BOMB);
            init(this, src, ref);
        }
        
        protected final static void init(final Panctor exp, final Player src, final Panctor ref) {
            final Panple pos = exp.getPosition(), bombPos = ref.getPosition();
            pos.set(bombPos.getX(), bombPos.getY(), BotsnBoltsGame.DEPTH_BURST);
            exp.setView(src.pi.burst);
            final Player player = src;
            if (player.stateHandler != Player.BALL_HANDLER) {
                return;
            } else if (player.v >= Player.VEL_BOUNCE_BOMB) {
                return;
            } else if (pos.getDistance2(player.getPosition()) > 9) {
                return;
            }
            player.v = Player.VEL_BOUNCE_BOMB;
        }

        @Override
        public final void onAllOob(final AllOobEvent event) {
        }
        
        @Override
        public final void onAnimationEnd(final AnimationEndEvent event) {
            destroy();
        }
    }
    
    public abstract static class ChrProjectile extends Chr implements SpecProjectile {
        protected final Player src;
        private int power;
        private Panple velocity = null;
        
        protected ChrProjectile(final Player src, final int offX, final int h) {
            super(offX, h);
            this.src = src;
            getPosition().setZ(BotsnBoltsGame.DEPTH_PROJECTILE);
        }
        
        @Override
        public final Player getSource() {
            return src;
        }
        
        public int getPower() {
            return power;
        }
        
        @Override
        public void assignPower(final int power) {
            this.power = power;
        }

        @Override
        public PlayerImages getPlayerImages() {
            return src.pi;
        }
        
        public float getVelocityX() {
            return hv;
        }
        
        @Override
        public Panple getVelocity() {
            if (velocity == null) {
                velocity = new ImplPanple();
            }
            velocity.set(hv, v);
            return velocity;
        }

        @Override
        public ShootMode getShootMode() {
            return Player.SHOOT_BOMB;
        }
    }
    
    public abstract static class FallingBomb extends ChrProjectile {
        protected FallingBomb(final Player src) {
            super(src, 3, 3);
            setMirror(src.isMirror());
            setView(src.pi.bomb);
            src.getLayer().addActor(this);
            assignPower(POWER_BOMB);
        }
        
        @Override
        protected final boolean onStepCustom() {
            if (!isJumpPossible()) {
                burst();
            }
            return false;
        }
        
        @Override
        protected final void onLanded() {
            burst();
        }
        
        @Override
        public final void burst() {
            BotsnBoltsGame.fxAttack.startSound();
            Projectile.burst(this, src.pi.burst, getPosition());
            destroy();
        }
        
        public final boolean isEnemyBurstAlwaysNeeded() {
            return false;
        }
    }
    
    public final static class PlayerFallingBomb extends FallingBomb implements SpecPlayerProjectile {
        protected PlayerFallingBomb(final Player src) {
            super(src);
        }
        
        @Override
        public final void bounce() {
            burst();
        }
    }
    
    public static class StreamProjectile extends Projectile implements SpecStreamProjectile {
        private final int ox;
        protected boolean srcMirror;
        
        protected StreamProjectile(final Player src, final int ox) {
            super(src, src.pi, Player.SHOOT_STREAM, src, 0, 0, 1);
            this.ox = ox;
        }
        
        @Override
        public final int getOffsetX() {
            return ox;
        }
        
        @Override
        public final void initSourceMirror() {
            srcMirror = src.getAimMirror();
        }
        
        @Override
        public final boolean getSourceMirror() {
            return srcMirror;
        }
        
        @Override
        public final void bounce() {
            bounce(this);
        }
        
        public final static void bounce(final SpecStreamProjectile bounced) {
            boolean detaching = false;
            bounced.getSource().lastStreamBounce = Pangine.getEngine().getClock();
            for (final SpecStreamProjectile prj : bounced.getSource().streamProjectiles) {
                if (prj == bounced) {
                    detaching = true;
                }
                if (detaching) {
                    detach(prj);
                }
            }
        }
        
        @Override
        public final void onStepEnd(final float y) {
            onStepEnd(this, y);
        }
        
        public final static void onStepEnd(final SpecStreamProjectile prj, final float y) {
            final boolean mirror = Mathtil.rand(20) ? !prj.isMirror() : prj.isMirror();
            if (y == Player.NULL_COORD) {
                prj.initSourceMirror();
            }
            final Player src = prj.getSource();
            final boolean srcMirror = prj.getSourceMirror();
            initPosition((Pandy) prj, src, src.getPosition(), srcMirror, 0, 0);
            final int m = prj.getMirrorMultiplier();
            final Panple pos = prj.getPosition();
            pos.addX(m * (prj.getOffsetX() + src.getStreamOffsetX() + ((srcMirror == mirror) ? 0 : (1 + Math.round(prj.getCurrentDisplay().getBoundingMaximum().getX())))));
            if (y == Player.NULL_COORD) {
                pos.addY(src.getStreamOffsetY());
            } else {
                pos.setY(y);
            }
            prj.setMirror(mirror);
        }
        
        @Override
        protected final boolean isDestructionWhenOobNeeded() {
            return false;
        }
    }
    
    public static class ShieldProjectile extends Projectile implements SpecShieldProjectile, CollisionListener {
        private final static byte TARGET_NONE = -1;
        private final static byte TARGET_ENEMY = 0;
        private final static byte TARGET_POWER_UP = 1;
        private final static byte TARGET_AI_SHIELD = 2;
        private final static byte TARGET_BUTTON = 3;
        private final static int VEL_SHIELD = Player.VEL_PROJECTILE;
        private Panctor target = null;
        private float targetOffsetY = 0;
        private int vel = 0;
        private float initialHv = 0;
        private List<PowerUp> collectedPowerUps = null;
        
        protected ShieldProjectile(final Player src) {
            super(src, src.pi, Player.SHOOT_SHIELD, src, 0, 0, POWER_SHIELD);
            initShieldProjectile(this, src);
        }
        
        protected final static void initShieldProjectile(final SpecShieldProjectile prj, final Player src) {
            src.lastShieldProjectile = prj;
            prj.getPosition().addY(-12);
            prj.setView(src.pi.shieldCircle);
            if (!prj.pickTarget()) {
                setVelocityWithoutTarget(prj);
            }
        }
        
        @Override
        public final boolean pickTarget() {
            Panctor nearestEnemy = null, nearestPowerUp = null, aiShield = null, shootableButton = null;
            double nearestEnemyDistance = Float.MAX_VALUE, nearestPowerUpDistance = Float.MAX_VALUE;
            final Panple pos = src.getPosition();
            for (final Panctor actor : src.getLayerRequired().getActors()) {
                final byte targetType;
                if (actor instanceof Enemy) {
                    targetType = TARGET_ENEMY;
                } else if (actor instanceof PowerUp) {
                    targetType = TARGET_POWER_UP;
                } else if (actor instanceof AiShieldProjectile) {
                    targetType = TARGET_AI_SHIELD;
                } else if (actor instanceof ShootableButton){
                    targetType = TARGET_BUTTON;
                } else {
                    targetType = TARGET_NONE;
                }
                if (targetType != TARGET_NONE) {
                    if (!actor.isInView()) {
                        continue;
                    }
                    final Panple tpos = actor.getPosition();
                    //TODO If shield projectile is still in air when changing room, then change mode back to shield
                    //TODO Don't track nearest enemy/powerup/button separately, just track nearest target, but weight by type.
                    // Will shoot enemy if a powerup is a little closer. Will shoot powerup if if it's much closer.
                    final double distance = tpos.getDistance2(pos); //TODO Maybe average total distance with y distance to prioritize enemies directly in front of Player
                    if (isTargetable(src, actor)) {
                        if (targetType == TARGET_ENEMY) {
                            if ((nearestEnemy == null) || (distance < nearestEnemyDistance)) {
                                nearestEnemy = actor;
                                nearestEnemyDistance = distance;
                            }
                        } else if (targetType == TARGET_POWER_UP) {
                            if ((nearestPowerUp == null) || (distance < nearestPowerUpDistance)) {
                                nearestPowerUp = actor;
                                nearestPowerUpDistance = distance;
                            }
                        } else if (targetType == TARGET_AI_SHIELD) {
                            aiShield = actor;
                        } else {
                            shootableButton = actor;
                        }
                    }
                }
            }
            if (aiShield != null) {
                setTarget(aiShield, 0);
            } else if (nearestEnemy != null) {
                setTarget(nearestEnemy, 0);
            } else if (nearestPowerUp != null) {
                setTarget(nearestPowerUp, 0);
            } else if (shootableButton != null) {
                setTarget(shootableButton, 0);
            } else {
                return false;
            }
            return true;
        }
        
        protected final static boolean isTargetable(final Player src, final Panctor target) {
            if (src.getAimMirror()) {
                return target.getPosition().getX() < src.getPosition().getX();
            } else {
                return target.getPosition().getX() > src.getPosition().getX();
            }
        }
        
        protected final static void setVelocityWithoutTarget(final SpecShieldProjectile prj) {
            prj.setVel(getMirrorMultiplier(prj.getSource().getAimMirror()) * VEL_SHIELD);
        }
        
        @Override
        public final int getVel() {
            return vel;
        }
        
        @Override
        public final void setVel(final int vel) {
            this.vel = vel;
        }
        
        @Override
        public final Panctor getTarget() {
            return target;
        }
        
        @Override
        public final float getTargetOffsetY() {
            return targetOffsetY;
        }
        
        @Override
        public final void setTarget(final Panctor target, final float targetOffsetY) {
            this.target = target;
            this.targetOffsetY = targetOffsetY;
        }
        
        @Override
        public final float getInitialHv() {
            return initialHv;
        }
        
        @Override
        public final void setInitialHv(final float initialHv) {
            this.initialHv = initialHv;
        }
        
        @Override
        protected final void onStepEndProjectile() {
            onStepEndShieldProjectile(this);
        }
        
        protected final static void onStepEndShieldProjectile(final SpecShieldProjectile prj) {
            final Panple pos = prj.getPosition();
            final Player src = prj.getSource();
            final Panctor target = prj.getTarget();
            if (!prj.isInView() && (target != src)) {
                startReturnToPlayer(prj);
            } else if (target == null) {
                pos.addX(prj.getVel());
            } else if (target.isDestroyed()) {
                if (target == src) {
                    onCaught(prj);
                    prj.destroy();
                } else {
                    startReturnToPlayer(prj);
                }
            } else {
                setToTarget(prj, scratch);
                Panple.subtract(scratch, scratch, pos);
                final double mag = scratch.getMagnitude2();
                if (mag < VEL_SHIELD) {
                    if (target == src) {
                        src.setShootMode(Player.SHOOT_SHIELD);
                        prj.destroy();
                    } else {
                        setToTarget(prj, pos);
                        startReturnToPlayer(prj);
                    }
                } else {
                    scratch.setMagnitude2(VEL_SHIELD);
                    pos.add2(scratch);
                    final float hv = scratch.getX();
                    if (hv != 0) {
                        final float initial = prj.getInitialHv();
                        if (initial == 0) {
                            prj.setInitialHv(hv);
                        } else if ((hv < 0 && initial > 0) || (hv > 0 && initial < 0)) {
                            startReturnToPlayer(prj);
                        }
                    }
                }
            }
            if (prj.getClass() != ShieldProjectile.class) {
                return;
            }
            final ShieldProjectile sprj = (ShieldProjectile) prj;
            for (final PowerUp powerUp : Coltil.unnull(sprj.collectedPowerUps)) {
                powerUp.getPosition().set2(pos);
            }
        }
        
        protected final static void setToTarget(final SpecShieldProjectile prj, final Panple p) {
            final Panple tpos = prj.getTarget().getPosition();
            p.set(tpos.getX(), tpos.getY() + prj.getTargetOffsetY());
        }
        
        private final void startReturnToPlayer() {
            startReturnToPlayer(this);
        }
        
        protected final static void startReturnToPlayer(final SpecShieldProjectile prj) {
            prj.setTarget(prj.getSource(), 1);
        }
        
        protected final static void onCaught(final SpecShieldProjectile prj) {
            //TODO If didn't travel far, Player could still be in the throw image (which does not have Extra for rendering shield); end throw image or render shield
            if (prj.getClass() != ShieldProjectile.class) {
                return;
            }
            final ShieldProjectile sprj = (ShieldProjectile) prj;
            for (final PowerUp powerUp : Coltil.unnull(sprj.collectedPowerUps)) {
                powerUp.getPosition().set2(sprj.src.getPosition());
            }
        }
        
        protected final void addPowerUp(final PowerUp powerUp) {
            if (collectedPowerUps == null) {
                collectedPowerUps = new LinkedList<PowerUp>();
            }
            collectedPowerUps.add(powerUp);
        }
        
        @Override
        public final void burst() {
            startReturnToPlayer();
        }
        
        @Override
        public final void bounce() {
            startReturnToPlayer();
        }
        
        @Override
        public void onCollision(final CollisionEvent event) {
            final Collidable collider = event.getCollider();
            if (collider instanceof AiProjectile) {
                ((AiProjectile) collider).burst();
                startReturnToPlayer();
            }
        }
        
        @Override
        protected final boolean isDestructionWhenOobNeeded() {
            return false;
        }
    }
    
    public static class SwordProjectile extends Projectile implements SpecSwordProjectile {
        private boolean firstStep = true;
        
        protected SwordProjectile(final Player src) {
            super(src, src.pi, Player.SHOOT_SWORD, src, 0, 0, POWER_MEDIUM);
            initSwordProjectile(this, src);
        }
        
        protected final static void initSwordProjectile(final Panctor prj, final Player src) {
            prj.setView(BotsnBoltsGame.getSwordHitBox());
            final Panple pos = src.getPosition();
            prj.getPosition().set(pos.getX(), pos.getY() - 7);
        }
        
        @Override
        protected final void onStepEndProjectile() {
            onStepEndSword(this, firstStep);
            firstStep = false;
        }
        
        protected final static void onStepEndSword(final Panctor prj, final boolean firstStep) {
            //TODO Keep for a couple more frames (but destroy immediately if it hits something)
            if (!firstStep) {
                prj.destroy();
            }
        }
        
        @Override
        public final boolean isEnemyBurstAlwaysNeeded() {
            return true;
        }
        
        @Override
        public final void burst() {
            // Don't create a Projectile burst; it covers a wide range, don't know where it connects with Enemy; an Enemy burst is drawn separately
            destroy();
        }
    }
    
    public final static class Bounce extends Pandy implements AllOobListener {
        protected Bounce(final Pandy prj) {
            getPosition().set(prj.getPosition());
            final Panple vel = getVelocity();
            final boolean mirror = prj.getVelocity().getX() > 0;
            vel.set(mirror ? -1 : 1, 1);
            vel.setMagnitude2(Player.VEL_PROJECTILE);
            setMirror(mirror);
            setView(prj);
            BotsnBoltsGame.addActor(this);
            BotsnBoltsGame.fxRicochet.startSound();
            prj.destroy();
        }
        
        @Override
        public final void onAllOob(final AllOobEvent event) {
            destroy();
        }
    }
}
