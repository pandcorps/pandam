package org.pandcorps.shoot;

import org.pandcorps.core.Pantil;
import org.pandcorps.pandam.*;

public class Weapon extends Panctor {
	public final static class WeaponDefinition {
		private final Panmage image;
		protected final Emitter[] attackEmitters;
		protected final Emitter[] attackingEmitters;
		
		public WeaponDefinition(final Panmage image, final Emitter[] attackEmitters, final Emitter[] attackingEmitters) {
			this.image = image;
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
}
