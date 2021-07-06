/*
Copyright (c) 2009-2021, Andrew M. Martin
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
package org.pandcorps.game.actor;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.boundary.*;

public abstract class Projectile extends Panctor implements StepListener, StepEndListener, AllOobListener, Collidable /*Or CollisionListener if we want two Projectiles to collide with each other*/ {
    
    /*package*/ Emitter emitter = null;
    protected Panple vel = null;
    protected byte time;
    protected byte age = 0;
    
    protected void init(final Guy2 guy, final Emitter emitter, final boolean mirror) {
        this.emitter = emitter;
        this.vel = getVelocity(mirror);
        this.time = emitter.time;
        setView(emitter.projView);
        final Panple pos = guy.getPosition();
        setMirror(mirror);
        final int mult = getMirrorMultiplier();
        getPosition().set(pos.getX() + (emitter.xoff * mult), pos.getY() + emitter.yoff, pos.getZ() + 1);
        guy.getLayer().addActor(this);
    }
    
    protected Panple getVelocity(final boolean mirror) {
    	return mirror ? emitter.mirVel : emitter.vel;
    }
    
    private final void setView(final Panview view) {
    	if (view instanceof Panmage) {
            setView((Panmage) view);
        } else {
            setView((Panimation) view);
        }
    }
    
    @Override
    public void onStep(final StepEvent event) {
        age++;
        if (time > 0) {
            time--;
        }
        if (vel != null) {
            getPosition().add(vel);
        }
    }
    
    @Override
    public void onStepEnd(final StepEndEvent event) {
        if (time == 0) {
            die();
        }
    }
    
    @Override
    public final void onAllOob(final AllOobEvent event) {
        die();
    }
    
    public abstract void die();
}
