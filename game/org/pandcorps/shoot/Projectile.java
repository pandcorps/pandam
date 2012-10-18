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

import org.pandcorps.core.*;
import org.pandcorps.core.col.IdentityHashSet;
import org.pandcorps.game.actor.*;
import org.pandcorps.game.actor.Emitter;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.FinPanple;

public class Projectile extends org.pandcorps.game.actor.Projectile implements Collidee /*Or CollisionListener if we want two Projectiles to collide with each other*/ {
    
    /*package*/ Shooter shooter = null;
    ///*package*/ Emitter emitter = null;
    /*package*/ Weapon weapon = null;
    /*package*/ IdentityHashSet<Shooter> victims = null;
    
	@Override
	protected void init(final Guy2 guy, final Emitter emitter, final boolean mirror) {
		super.init(guy, emitter, mirror);
		this.shooter = (Shooter) guy;
		//this.emitter = emitter;
	}
	
	protected void impact(final Shooter collider) {
	    burst(collider, ShootGame.blood, 4);
	}
	
	protected final void burst(final Shooter collider, final Panimation anm, final float off) {
	    add(collider, new Burst(anm), off);
	}
	
	protected final void add(final Shooter collider, final Panctor actor, final float off) {
        collider.add(actor, Mathtil.randf(-off, off), Mathtil.randf(6 - off, 6 + off), 1f);
    }
	
	@Override
	public final void onCollision(final Shooter collider, final CollisionEvent event) {
		if (collider == shooter) {
			return;
		} else if (Coltil.contains(victims, collider)) {
			return;
		}
		if (victims == null) {
			victims = new IdentityHashSet<Shooter>();
		}
		victims.add(collider);
		if (victims.size() >= weapon.getPierce().getValue()) {
			die();
		}
		impact(collider);
	}
    
    @Override
    public void die() {
    	destroy();
    }
    
    private abstract static class RandProjectile extends Projectile {
    	protected abstract float getR();
    	
    	@Override
    	protected final Panple getVelocity(final boolean mirror) {
    		final Panple base = super.getVelocity(mirror);
    		final float r = getR();
    		return FinPanple.newMagnitudeDirection(base.getMagnitude2(), base.getDirection2() + Mathtil.randf(-r, r), base.getZ());
    	}
    }
    
    public final static class ShotProjectile extends RandProjectile {
    	private final static float r = (float) (Math.PI / 6.0);
    	
    	@Override
    	protected final float getR() {
    		return r;
    	}
    }
    
    public final static class MiniProjectile extends RandProjectile {
    	private final static float r = (float) (Math.PI / 48.0);
    	
    	@Override
    	protected final float getR() {
    		return r;
    	}
    }
    
    public final static class FlameProjectile extends Projectile implements AnimationEndListener {
        @Override
        public final void onAnimationEnd(final AnimationEndEvent event) {
            changeView(ShootGame.flameLoopAnm);
        }
        
        @Override
        protected void impact(final Shooter collider) {
            burst(collider, ShootGame.puff, 7.5f);
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
    	
    	@Override
    	protected void impact(final Shooter collider) {
    	    final int blast = weapon.getBlast().getValue();
    	    final float off = blast * 2.5f;
    	    for (int i = 0; i < blast; i++) {
    	        add(collider, new Blast(), off);
    	    }
        }
    }
    
    private final static class Blast extends Burst implements Collidee {
        private Blast() {
            super(ShootGame.explosion);
        }
        
        @Override
        public final void onCollision(final Shooter collider, final CollisionEvent event) {
System.err.println("Blast.onCollision not implemented"); //TODO Share Projectile's victims, don't hurt same one twice
        }
    }
}
