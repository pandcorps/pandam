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

import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.boundary.*;

public abstract class PowerUp extends Panctor implements CollisionListener {
    @Override
    public final void onCollision(final CollisionEvent event) {
        final Collidable collider = event.getCollider();
        if (collider.getClass() == Player.class) {
            award((Player) collider);
            destroy();
        }
    }
    
    protected abstract void award(final Player player);
    
    protected final static PlayerContext getRandomPlayerContext() {
        return BotsnBoltsGame.pc;
    }
    
    public abstract static class Battery extends PowerUp implements AllOobListener {
        protected Battery() {
            setView(getImage());
        }
        
        protected abstract Panmage getImage();
        
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
    
    public final static class BigBattery extends Battery {
        @Override
        protected final Panmage getImage() {
            return getRandomPlayerContext().pi.batteryBig;
        }
        
        @Override
        protected final int getAmount() {
            return 7;
        }
    }
    
    public final static class Bolt extends PowerUp {
        @Override
        protected final void award(final Player player) {
            
        }
    }
}
