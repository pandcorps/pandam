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

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.game.actor.Burst;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandax.Pandy;

public class Weapon extends Panctor implements Upgradeable {
    /*package*/ final static int INF = Integer.MAX_VALUE;
    /*package*/ final static int DELAY = 50;
    
    public final static class WeaponParameter {
        public final String name;
        public final int min;
        public final int max;
        
        public WeaponParameter(final String name, final int min, final int max) {
            this.name = name;
            this.min = min;
            this.max = max;
        }
        
        public final boolean isUpgradeApplicable() {
            return min < max;
        }
    }
    
	public final static class WeaponDefinition {
		/*package*/ final String name;
		private final Panmage image;
		private final Panimation flashAnm;
		private final Panimation casingAnm;
		private final Panimation smokeAnm;
		private final Panimation attackAnm;
		protected final Emitter[] attackEmitters;
		protected final Emitter[] attackingEmitters;
		/*package*/ final WeaponParameter power;
		/*package*/ final WeaponParameter capacity;
		/*package*/ final WeaponParameter rate;
		/*package*/ final WeaponParameter pierce;
		/*package*/ final WeaponParameter spray;
		/*package*/ final WeaponParameter range;
		/*package*/ final WeaponParameter blast;
		
		public WeaponDefinition(final String name, final Panmage image, final Panimation flashAnm,
				final Panimation casingAnm, final Panimation smokeAnm, final Panimation attackAnm,
				final Emitter[] attackEmitters, final Emitter[] attackingEmitters,
				final int minPower, final int maxPower,
				final int minCapacity, final int maxCapacity,
				final int minRate, final int maxRate,
				final int minPierce, final int maxPierce,
				final int minSpray, final int maxSpray,
				final int minRange, final int maxRange,
				final int minBlast, final int maxBlast) {
			this.name = name;
			this.image = image;
			this.flashAnm = flashAnm;
			this.casingAnm = casingAnm;
			this.smokeAnm = smokeAnm;
			this.attackAnm = attackAnm;
			this.attackEmitters = attackEmitters;
			this.attackingEmitters = attackingEmitters;
			this.power = new WeaponParameter("Power", minPower, maxPower);
			this.capacity = new WeaponParameter("Capacity", minCapacity, maxCapacity);
			this.rate = new WeaponParameter("Rate", DELAY - minRate, DELAY - maxRate);
			this.pierce = new WeaponParameter("Pierce", minPierce, maxPierce);
			this.spray = new WeaponParameter("Spray", minSpray, maxSpray);
			this.range = new WeaponParameter("Range", minRange, maxRange);
			this.blast = new WeaponParameter("Blast", minBlast, maxBlast);
		}
	}
	
	public final static class WeaponArgument implements Upgradeable {
	    public final WeaponParameter parm;
	    
	    private int val;
	    
	    public WeaponArgument(final WeaponParameter parm) {
	        this.parm = parm;
	        val = parm.min;
	    }
	    
	    @Override
	    public final String getName() {
	        return parm.name;
	    }
	        
	    @Override
	    public final boolean isUpgradeApplicable() {
	        return parm.isUpgradeApplicable();
	    }
	    
	    @Override
	    public boolean isUpgradePossible() {
	        return val < parm.max;
	    }
	    
	    public boolean upgrade() {
	        if (!isUpgradePossible()) {
	            return false;
	        }
	        this.val++;
	        return true;
	    }
	    
	    public final int getValue() {
	        return val;
	    }
	}
	
	protected final WeaponDefinition def;
	private final WeaponArgument power;
	private final WeaponArgument capacity;
	private final WeaponArgument rate;
	private final WeaponArgument pierce;
	private final WeaponArgument spray;
	private final WeaponArgument range;
	private final WeaponArgument blast;
	private final List<WeaponArgument> args;
	private boolean attacking = false;
	private int timer = 0;
	private int smoke = 0;
	
	protected Weapon(final WeaponDefinition def) {
		super(Pantil.vmid());
		this.def = def;
		power = new WeaponArgument(def.power);
		capacity = new WeaponArgument(def.capacity);
		rate = new WeaponArgument(def.rate);
		pierce = new WeaponArgument(def.pierce);
		spray = new WeaponArgument(def.spray);
		range = new WeaponArgument(def.range);
		blast = new WeaponArgument(def.blast);
		args = Coltil.unmodifiableList(Coltil.asList(power, capacity, rate, pierce, spray, range, blast));
		setView(def.image);
	}
	
	protected final void step() {
		if (attacking) {
			attacking = false;
		} else {
			changeView(def.image);
		}
		if (timer > 0) {
			timer--;
		}
		if (smoke > 0) {
		    smoke--;
		}
	}
	
	protected final void attack(final Shooter shooter, final Emitter[] emitters) {
		if (emitters == null) {
			return;
		}
		attacking = true;
		if (def.attackAnm != null) {
			changeView(def.attackAnm);
		}
		if (timer > 0) {
			return;
		}
		timer = getDelay();
		final boolean mirror = isMirror();
		final HashSet<Panple> projPositions = new HashSet<Panple>();
		final int spray = this.spray.val;
		for (int i = 0; i < spray; i++) {
    		for (final Emitter em : emitters) {
    			final Projectile p = (Projectile) em.emit(shooter, mirror);
    			projPositions.add(p.getPosition());
    			p.weapon = this;
    		}
		}
		if (def.flashAnm != null) {
			for (final Panple projPos : projPositions) {
				final Burst flash = new Burst(def.flashAnm);
				flash.setMirror(mirror);
				flash.getPosition().set(projPos);
				getLayer().addActor(flash);
			}
		}
		final int mult = mirror ? -1 : 1;
		if (def.casingAnm != null) {
		    for (final Panple projPos : projPositions) {
		        final Casing casing = new Casing(def.casingAnm);
		        casing.setMirror(mirror);
		        casing.getPosition().set(projPos.getX() - (7 * mult), projPos.getY(), projPos.getZ());
		        final float rx = Mathtil.randf(1, 2.5f), ry = Mathtil.randf(2.5f, 4.5f);
		        casing.getVelocity().set(-mult * rx, ry);
		        casing.getAcceleration().setY(-.75f);
                getLayer().addActor(casing);
		    }
		}
		if (def.smokeAnm != null && smoke <= 0) {
		    for (final Panple projPos : projPositions) {
		        final Casing casing = new Casing(def.smokeAnm);
		        casing.setMirror(mirror);
		        casing.getPosition().set(projPos.getX() - (7 * mult), projPos.getY(), projPos.getZ());
		        final float rx = Mathtil.randf(-1.25f, 1.25f), ry = Mathtil.randf(0.1f, 0.5f);
		        casing.getVelocity().set(rx, ry);
		        casing.getAcceleration().setY(.25f);
                getLayer().addActor(casing);
                smoke = 5;
		    }
		}
	}
	
	@Override
    public final String getName() {
        return def.name;
    }
	
	public WeaponArgument getPower() {
        return power;
    }
	
	public WeaponArgument getCapacity() {
        return capacity;
    }
	
	public WeaponArgument getRate() {
        return capacity;
    }
	
	public WeaponArgument getPierce() {
		return pierce;
	}
	
	public WeaponArgument getSpray() {
	    return spray;
	}
	
	public WeaponArgument getRange() {
	    return range;
	}
	
	public WeaponArgument getBlast() {
	    return blast;
	}
	
	private final int getDelay() {
	    final int delay = DELAY - rate.getValue();
	    // step occurs in same cycle after setting delay, 1 acts like 0 if we don't add 1
	    return delay <= 0 ? 0 : delay + 1;
	}
	
	public List<WeaponArgument> getArguments() {
	    return args;
	}
	
	@Override
	public boolean isUpgradeApplicable() {
        for (final WeaponArgument arg : args) {
            if (arg.parm.isUpgradeApplicable()) {
                return true;
            }
        }
        return false;
    }
	
	@Override
	public boolean isUpgradePossible() {
	    for (final WeaponArgument arg : args) {
	        if (arg.isUpgradePossible()) {
	            return true;
	        }
	    }
	    return false;
	}
	
	private final static class Casing extends Pandy implements AnimationEndListener {
        public Casing(final Panimation anm) {
            super(Pantil.vmid());
            setView(anm);
        }

        @Override
        public final void onAnimationEnd(final AnimationEndEvent event) {
            destroy();
        }
	}
}
