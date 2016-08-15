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
import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.boundary.*;
import org.pandcorps.pandax.*;

public final class Projectile extends Pandy implements Collidable, AllOobListener {
    private final Player src;
    protected final ShootMode shootMode;
    
    protected Projectile(final Player src, final float vx, final float vy) {
        this.src = src;
        shootMode = src.prf.shootMode;
        final Panple srcPos = src.getPosition();
        final boolean mirror = src.isMirror();
        setMirror(mirror);
        final int xm = mirror ? -1 : 1;
        getPosition().set(srcPos.getX() + (xm * 15), srcPos.getY() + 13, BotsnBoltsGame.DEPTH_PROJECTILE);
        getVelocity().set(xm * vx, vy);
        src.getLayer().addActor(this);
    }
    
    protected final void burst() {
        burst(this);
    }
    
    protected final void burst(final Panctor target) {
        burst(target.getPosition());
    }
    
    protected final void burst(final Panple loc) {
        burst(this, src.pi.burst, loc);
    }
    
    protected final static void burst(final Panctor src, final Panimation anm, final Panple loc) {
        final Burst burst = new Burst(anm);
        final Panple pos = burst.getPosition();
        pos.set(loc);
        pos.setZ(BotsnBoltsGame.DEPTH_BURST);
        burst.setMirror(src.isMirror());
        src.getLayer().addActor(burst);
    }

    @Override
    public final void onAllOob(final AllOobEvent event) {
        destroy();
    }
}
