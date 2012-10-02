/*
Copyright (c) 2009-2011, Andrew M. Martin
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
package org.pandcorps.shoot;

import org.pandcorps.core.Mathtil;
import org.pandcorps.core.Pantil;
import org.pandcorps.core.col.IdentityHashSet;
import org.pandcorps.game.actor.*;
import org.pandcorps.game.actor.Emitter;
import org.pandcorps.pandam.Panple;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.boundary.*;

public class Projectile extends org.pandcorps.game.actor.Projectile implements StepListener, AllOobListener, Collidable /*Or CollisionListener if we want two Projectiles to collide with each other*/ {
    
    /*package*/ Shooter shooter = null;
    ///*package*/ Emitter emitter = null;
    /*package*/ IdentityHashSet<Shooter> victims = null;
    
	@Override
	protected void init(final Guy2 guy, final Emitter emitter, final boolean mirror) {
		super.init(guy, emitter, mirror);
		this.shooter = (Shooter) guy;
		//this.emitter = emitter;
	}
    
    @Override
    public void die() {
    	destroy();
    }
    
    public final static class FlameProjectile extends Projectile implements AnimationEndListener {
        @Override
        public final void onAnimationEnd(final AnimationEndEvent event) {
            changeView(ShootGame.flameLoopAnm);
        }
    }
    
    public final static class RocketProjectile extends Projectile {
    	private Decoration fire = null;
    	
    	@Override
    	protected void init(final Guy2 guy, final Emitter emitter, final boolean mirror) {
    		super.init(guy, emitter, mirror);
    		fire = new Decoration(Pantil.vmid());
    		fire.setView(ShootGame.rocketFireAnm);
    		fire.setMirror(isMirror());
    		getLayer().addActor(fire);
    	}
    	
    	@Override
    	public void onStep(final StepEvent event) {
    		super.onStep(event);
    		final Panple pos = getPosition();
    		final int m = 5;
    		final float x = pos.getX() + (isMirror() ? m : -m), y = pos.getY(), z = pos.getZ();
    		fire.getPosition().set(x, y, z);
    		if ((age % 2) == 1) {
    			final Burst smoke = new Burst(ShootGame.smokeBigAnm);
    			smoke.getPosition().set(x + Mathtil.randi(-2, 2), y + Mathtil.randi(-2, 2), z - 1);
    			getLayer().addActor(smoke);
    		}
    	}
    	
    	@Override
    	public void die() {
    		super.die();
    		fire.destroy();
    	}
    }
}
