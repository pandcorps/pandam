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

import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.boundary.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.*;

public class Projectile extends Pandy implements Collidable, AllOobListener, SpecProjectile {
    protected final static int POWER_MEDIUM = 3;
    protected final static int POWER_MAXIMUM = 5;
    protected final static int POWER_IMPOSSIBLE = Integer.MAX_VALUE;
    protected final static int OFF_X = 15;
    protected final static int OFF_Y = 13;
    protected final static Set<Projectile> currentProjectiles = new HashSet<Projectile>();
    
    protected final Player src;
    protected final PlayerImages pi;
    protected final ShootMode shootMode;
    protected int power;
    
    protected Projectile(final Player src, final float vx, final float vy, final int power) {
        this(src, src.pi, src.prf.shootMode, src, vx, vy, power);
    }
    
    protected Projectile(final Player src, final PlayerImages pi, final ShootMode shootMode, final Panctor ref, final float vx, final float vy, final int power) {
        currentProjectiles.add(this);
        this.src = src;
        this.pi = pi;
        this.shootMode = shootMode;
        setPower(power);
        final Panple srcPos = ref.getPosition();
        setMirror(ref.isMirror());
        final int xm = getMirrorMultiplier();
        getPosition().set(srcPos.getX() + (xm * OFF_X), srcPos.getY() + OFF_Y, BotsnBoltsGame.DEPTH_PROJECTILE);
        getVelocity().set(xm * vx, vy);
        ref.getLayer().addActor(this);
    }
    
    protected final void setPower(final int power) {
        setPower(this, power);
    }
    
    protected final static void setPower(final SpecProjectile prj, final int power) {
        prj.assignPower(power);
        if (power > POWER_MEDIUM) {
            prj.changeView(prj.getPlayerImages().projectile3);
        } else if (power > 1) {
            prj.changeView(prj.getPlayerImages().projectile2);
        } else if (power > 0) {
            prj.changeView(prj.getPlayerImages().basicProjectile);
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
    public final void burst() {
        burst(this);
        destroy();
    }
    
    protected final void burst(final SpecPanctor target) {
        burst(target.getPosition());
    }
    
    protected final void burst(final Panple loc) {
        burst(this, pi.burst, loc);
    }
    
    protected final static void burst(final Panctor src, final Panimation anm, final Panple loc) {
        burst(src, anm, loc.getX(), loc.getY());
    }
    
    protected final static void burst(final Panctor src, final Panimation anm, final float x, final float y) {
        final Burst burst = new Burst(anm);
        final Panple pos = burst.getPosition();
        pos.set(x, y, BotsnBoltsGame.DEPTH_BURST);
        burst.setMirror(src.isMirror());
        BotsnBoltsGame.addActor(burst);
    }
    
    protected final void bounce() {
        new Bounce(this);
        destroy();
    }

    @Override
    public void onAllOob(final AllOobEvent event) {
        destroy();
    }
    
    @Override
    public final void onStep(final StepEvent event) {
        super.onStep(event);
        if (!isInView()) { // onAllOob above checks the whole room, not just the current view
            destroy();
        }
    }
    
    @Override
    public final void onDestroy() {
        currentProjectiles.remove(this);
        super.onDestroy();
    }
    
    public static class Bomb extends Panctor implements StepListener {
        private final Player src;
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
    
    public final static class Explosion extends Projectile implements AnimationEndListener {
        protected Explosion(final Bomb bomb) {
            super(bomb.src, bomb.src.pi, Player.SHOOT_BOMB, bomb, 0, 0, 1);
            final Panple bombPos = bomb.getPosition();
            getPosition().set(bombPos.getX(), bombPos.getY(), BotsnBoltsGame.DEPTH_BURST);
            setView(bomb.src.pi.burst);
            bounceIfNeeded();
        }
        
        private final void bounceIfNeeded() {
            bounceIfNeeded(BotsnBoltsGame.pc);
        }
        
        private final void bounceIfNeeded(final PlayerContext pc) {
            final Player player = PlayerContext.getPlayer(pc);
            if (player == null) {
                return;
            } else if (player.stateHandler != Player.BALL_HANDLER) {
                return;
            } else if (player.v >= Player.VEL_BOUNCE_BOMB) {
                return;
            } else if (getPosition().getDistance2(player.getPosition()) > 9) {
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
    
    public final static class Bounce extends Pandy implements AllOobListener {
        protected Bounce(final Projectile prj) {
            getPosition().set(prj.getPosition());
            final Panple vel = getVelocity();
            final boolean mirror = prj.getVelocity().getX() > 0;
            vel.set(mirror ? -1 : 1, 1);
            vel.setMagnitude2(Player.VEL_PROJECTILE);
            setMirror(mirror);
            setView(prj);
            BotsnBoltsGame.addActor(this);
        }
        
        @Override
        public final void onAllOob(final AllOobEvent event) {
            destroy();
        }
    }
}
