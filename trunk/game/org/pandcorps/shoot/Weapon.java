package org.pandcorps.shoot;

import java.util.HashSet;

import org.pandcorps.core.Mathtil;
import org.pandcorps.core.Pantil;
import org.pandcorps.game.actor.Burst;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandax.Pandy;

public class Weapon extends Panctor {
	public final static class WeaponDefinition {
		private final Panmage image;
		private final Panimation flashAnm;
		private final Panimation casingAnm;
		private final Panimation smokeAnm;
		private final Panimation attackAnm;
		protected final Emitter[] attackEmitters;
		protected final Emitter[] attackingEmitters;
		private final int delay;
		private final int minPierce;
		
		public WeaponDefinition(final Panmage image, final Panimation flashAnm,
				final Panimation casingAnm, final Panimation smokeAnm, final Panimation attackAnm,
				final Emitter[] attackEmitters, final Emitter[] attackingEmitters,
				final int delay, final int minPierce) {
			this.image = image;
			this.flashAnm = flashAnm;
			this.casingAnm = casingAnm;
			this.smokeAnm = smokeAnm;
			this.attackAnm = attackAnm;
			this.attackEmitters = attackEmitters;
			this.attackingEmitters = attackingEmitters;
			// step occurs in same cycle after setting delay, 1 acts like 0 if we don't add 1
			this.delay = delay <= 0 ? 0 : delay + 1;
			this.minPierce = minPierce;
		}
	}
	
	protected final WeaponDefinition def;
	private boolean attacking = false;
	private int timer = 0;
	
	protected Weapon(final WeaponDefinition def) {
		super(Pantil.vmid());
		this.def = def;
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
		timer = def.delay;
		final boolean mirror = isMirror();
		final HashSet<Panple> projPositions = new HashSet<Panple>();
		for (final Emitter em : emitters) {
			final Projectile p = (Projectile) em.emit(shooter, mirror);
			projPositions.add(p.getPosition());
			p.weapon = this;
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
		if (def.smokeAnm != null) {
		    for (final Panple projPos : projPositions) {
		        final Casing casing = new Casing(def.smokeAnm);
		        casing.setMirror(mirror);
		        casing.getPosition().set(projPos.getX() - (7 * mult), projPos.getY(), projPos.getZ());
		        final float rx = Mathtil.randf(-1.25f, 1.25f), ry = Mathtil.randf(0.1f, 0.5f);
		        casing.getVelocity().set(rx, ry);
		        casing.getAcceleration().setY(.25f);
                getLayer().addActor(casing);
		    }
		}
	}
	
	public int getPierce() {
		return def.minPierce;
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
