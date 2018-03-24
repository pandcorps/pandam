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

import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.boundary.*;

public abstract class PowerUp extends Chr implements CollisionListener {
    protected PowerUp() {
        super(4, 8);
    }

    @Override
    public final void onCollision(final CollisionEvent event) {
        final Collidable collider = event.getCollider();
        if (collider.getClass() == Player.class) {
            final Player player = (Player) collider;
            award(player);
            final Panple pos = getPosition();
            Projectile.burst(player, player.pi.burst, pos.getX(), pos.getY() + getCurrentDisplay().getBoundingMaximum().getY() / 2);
            destroy();
        }
    }
    
    protected abstract void award(final Player player);
    
    @Override
    protected final int getSolid(final int off) {
        return (off > 0) ? -1 : super.getSolid(off);
    }
    
    protected final static PlayerContext getRandomPlayerContext() {
        return BotsnBoltsGame.pc;
    }
    
    protected final static PowerUp addPowerUp(final PowerUp powerUp, final float x, final float y, final float v) {
        powerUp.getPosition().set(x, y, BotsnBoltsGame.DEPTH_POWER_UP);
        powerUp.v = v;
        BotsnBoltsGame.addActor(powerUp);
        return powerUp;
    }
    
    public abstract static class Battery extends PowerUp implements AllOobListener {
        protected Battery() {
            setView(getImage());
        }
        
        protected abstract Panimation getImage();
        
        @Override
        protected final void award(final Player player) {
            player.addHealth(getAmount());
        }
        
        protected abstract int getAmount();

        @Override
        public final void onAllOob(final AllOobEvent event) {
            destroy();
        }
    }
    
    public final static class SmallBattery extends Battery {
        @Override
        protected final Panimation getImage() {
            return getRandomPlayerContext().pi.batterySmall;
        }
        
        @Override
        protected final int getAmount() {
            return 2;
        }
    }
    
    public final static class MediumBattery extends Battery {
        @Override
        protected final Panimation getImage() {
            return getRandomPlayerContext().pi.batteryMedium;
        }
        
        @Override
        protected final int getAmount() {
            return 4;
        }
    }
    
    public final static class BigBattery extends Battery {
        @Override
        protected final Panimation getImage() {
            return getRandomPlayerContext().pi.batteryBig;
        }
        
        @Override
        protected final int getAmount() {
            return 7;
        }
    }
    
    public final static class Bolt extends PowerUp {
        {
            setView(getRandomPlayerContext().pi.bolt);
        }
        
        @Override
        protected final void award(final Player player) {
            
        }
    }
}
