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
		protected final Emitter[] attackEmitters;
		protected final Emitter[] attackingEmitters;
		
		public WeaponDefinition(final Panmage image, final Panimation flashAnm, final Panimation casingAnm, final Emitter[] attackEmitters, final Emitter[] attackingEmitters) {
			this.image = image;
			this.flashAnm = flashAnm;
			this.casingAnm = casingAnm;
			this.attackEmitters = attackEmitters;
			this.attackingEmitters = attackingEmitters;
		}
	}
	
	protected final WeaponDefinition def;
	
	protected Weapon(final WeaponDefinition def) {
		super(Pantil.vmid());
		this.def = def;
		setView(def.image);
	}
	
	protected final void attack(final Shooter shooter, final Emitter[] emitters) {
		if (emitters == null) {
			return;
		}
		final boolean mirror = isMirror();
		final HashSet<Panple> projPositions = new HashSet<Panple>();
		for (final Emitter em : emitters) {
			projPositions.add(em.emit(shooter, mirror).getPosition());
		}
		if (def.flashAnm != null) {
			for (final Panple projPos : projPositions) {
				final Burst flash = new Burst(def.flashAnm);
				flash.getPosition().set(projPos);
				getLayer().addActor(flash);
			}
		}
		if (def.casingAnm != null) {
		    for (final Panple projPos : projPositions) {
		        final Casing casing = new Casing(def.casingAnm);
		        casing.getPosition().set(projPos.getX() - 7, projPos.getY(), projPos.getZ());
		        final float rx = Mathtil.randf(1, 2.5f), ry = Mathtil.randf(2.5f, 4.5f);
		        casing.getVelocity().set(mirror ? rx : -rx, ry);
		        casing.getAcceleration().setY(-.75f);
                getLayer().addActor(casing);
		    }
		}
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
