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
import org.pandcorps.pandax.tile.*;

public final class Player extends GuyPlatform {
    private final static int SHOOT_DELAY_DEFAULT = 10;
    private final static int SHOOT_DELAY_RAPID = 3;
    private final static int SHOOT_DELAY_SPREAD = 15;
    private final static int SHOOT_TIME = 12;
    private final static int HURT_TIME = 20;
    private final static int RUN_TIME = 5;
    
    private final Profile prf;
    private final PlayerImages pi;
    private int runIndex = 0;
    private int runTimer = 0;
    private long lastShot = -1000;
    private long lastHurt = -1000;
    
    protected Player(final Profile prf, final PlayerImages pi) {
        super(6, 23);
        this.prf = prf;
        this.pi = pi;
    }
    
    private final boolean isHurt() {
        return (Pangine.getEngine().getClock() - lastHurt) < HURT_TIME;
    }
    
    private final PlayerImagesSubSet getCurrentImagesSubSet() {
        return ((Pangine.getEngine().getClock() - lastShot) < SHOOT_TIME) ? pi.shootSet : pi.basicSet;
    }
    
    @Override
    protected final void onGrounded() {
        if (isHurt()) {
            changeView(pi.hurt);
            return;
        }
        final PlayerImagesSubSet set = getCurrentImagesSubSet();
        if (hv == 0) {
            changeView(set.stand);
            runIndex = 0;
        } else {
            runTimer++;
            if (runTimer > RUN_TIME) {
                runTimer = 0;
                runIndex++;
                if (runIndex > 3) {
                    runIndex = 0;
                }
            }
            changeView(set.run[runIndex]);
        }
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
        return null;
    }

    @Override
    protected boolean isSolidBehavior(final byte b) {
        return false;
    }
    
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
                //player.changeView(view);
            }
        }
        
        protected abstract void createProjectile(final Player player);
        
        protected final void createDefaultProjectile(final Player player) {
            new Projectile(player, 4, 0);
        }
    }
    
    protected final static class PlayerImages {
        private final PlayerImagesSubSet basicSet;
        private final PlayerImagesSubSet shootSet;
        private final Panmage hurt;
        
        protected PlayerImages(final Panimation stand, final Panmage jump, final Panmage[] run,
                               final Panimation shootStand, final Panmage shootJump, final Panmage[] shootRun,
                               final Panmage hurt) {
            basicSet = new PlayerImagesSubSet(stand, jump, run);
            shootSet = new PlayerImagesSubSet(shootStand, shootJump, shootRun);
            this.hurt = hurt;
        }
    }
    
    protected final static class PlayerImagesSubSet {
        private final Panimation stand;
        private final Panmage jump;
        private final Panmage[] run;
        
        protected PlayerImagesSubSet(final Panimation stand, final Panmage jump, final Panmage[] run) {
            this.stand = stand;
            this.jump = jump;
            this.run = run;
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
            //TODO More shots
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
