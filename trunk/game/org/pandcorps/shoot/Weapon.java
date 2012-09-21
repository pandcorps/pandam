package org.pandcorps.shoot;

import java.util.HashSet;

import org.pandcorps.core.Pantil;
import org.pandcorps.game.actor.Burst;
import org.pandcorps.pandam.*;

public class Weapon extends Panctor {
	public final static class WeaponDefinition {
		private final Panmage image;
		private final Panimation flashAnm;
		protected final Emitter[] attackEmitters;
		protected final Emitter[] attackingEmitters;
		
		public WeaponDefinition(final Panmage image, final Panimation flashAnm, final Emitter[] attackEmitters, final Emitter[] attackingEmitters) {
			this.image = image;
			this.flashAnm = flashAnm;
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
	}
}
