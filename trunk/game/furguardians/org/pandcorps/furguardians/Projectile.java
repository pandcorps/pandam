/*
Copyright (c) 2009-2014, Andrew M. Martin
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
package org.pandcorps.furguardians;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.boundary.*;
import org.pandcorps.pandax.*;

public final class Projectile extends Pandy implements Collidable, AllOobListener {
    public Projectile(final Panimation anm, final Panctor src, final Panctor dst) {
        setView(anm);
        final Panple spos = src.getPosition();
        final float x = spos.getX() + (5 * (src.isMirror() ? -1 : 1)), y = spos.getY() + 6;
        FurGuardiansGame.setPosition(this, x, y, FurGuardiansGame.DEPTH_SPARK);
        setVelocity(this, dst, getVelocity(), 2f);
    }
    
    protected final static void setVelocity(final Panctor prj, final Panctor dst, final Panple vel, final float mag) {
    	final Panple pos = prj.getPosition(), dpos = dst.getPosition();
    	vel.set(dpos.getX() - pos.getX(), dpos.getY() + 6 - pos.getY(), 0);
        vel.multiply(mag / ((float) vel.getMagnitude2()));
        prj.setMirror(vel.getX() < 0);
    }
    
    @Override
    public final void onStep(final StepEvent event) {
    	super.onStep(event);
    	Enemy.destroyIfOffScreen(this, 40);
    }
    
    @Override
    public final void onAllOob(final AllOobEvent event) {
        destroy();
    }
}
