package org.pandcorps.shoot;

import org.pandcorps.core.Pantil;
import org.pandcorps.pandam.*;

public class Weapon extends Panctor {
	public final static class WeaponDefinition {
		private final Panmage image;
		
		public WeaponDefinition(final Panmage image) {
			this.image = image;
		}
	}
	
	private final WeaponDefinition def;
	
	protected Weapon(final WeaponDefinition def) {
		super(Pantil.vmid());
		this.def = def;
		setView(def.image);
	}
}
