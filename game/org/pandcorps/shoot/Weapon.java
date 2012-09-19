package org.pandcorps.shoot;

import org.pandcorps.core.Pantil;
import org.pandcorps.pandam.*;

public class Weapon extends Panctor {
	public final static class WeaponDefinition {
		private final Panmage image;
		protected final Emitter[] emitters;
		
		public WeaponDefinition(final Panmage image, final Emitter[] emitters) {
			this.image = image;
			this.emitters = emitters;
		}
	}
	
	protected final WeaponDefinition def;
	
	protected Weapon(final WeaponDefinition def) {
		super(Pantil.vmid());
		this.def = def;
		setView(def.image);
	}
}
