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

import org.pandcorps.core.Coltil;
import org.pandcorps.core.Mathtil;
import org.pandcorps.game.actor.Guy2;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.CollisionEvent;
import org.pandcorps.shoot.Weapon.WeaponDefinition;

public abstract class PowerUp extends Panctor implements Collidee {
	protected PowerUp(final Panmage view, final float x, final float y) {
		setView(view);
		ShootGame.room.addActor(this);
		final Panple pos = getPosition();
		pos.set(x, y);
		Guy2.setZ(pos);
	}
	
	@Override
	public void onCollision(final Shooter collider, final CollisionEvent event) {
		if (collider.getController().getClass() != Player.class) {
			return;
		} else if (!give(collider)) {
			return;
		}
		destroy();
	}
	
	protected abstract boolean give(final Shooter shooter);
	
	public final static class Money extends PowerUp {
		private final int amount;
		
		public Money(final Shooter defeated, final float x, final float y) {
			this(defeated.def.constitution * 5, x, y);
		}
		
		public Money(final int amount, final float x, final float y) {
			super(ShootGame.money, x, y);
			this.amount = amount;
		}
		
		@Override
		protected final boolean give(final Shooter shooter) {
			shooter.addMoney(amount);
			return true;
		}
	}
	
	public final static class Ammo extends PowerUp {
		private final WeaponDefinition def;
		
		public Ammo(final WeaponDefinition def, final float x, final float y) {
			super(def.ammo, x, y);
			this.def = def;
		}
		
		@Override
		protected final boolean give(final Shooter shooter) {
			final Weapon w = shooter.getWeapon(def);
			if (w == null) {
				return false;
			}
			return w.addAmmo(Math.max(1, def.capacity.min / 2));
		}
	}
	
	public final static class Health extends PowerUp {
		private final int amount;
		
		public Health(final Shooter defeated, final float x, final float y) {
			this(defeated.def.constitution / 2, x, y);
		}
		
		public Health(final int amount, final float x, final float y) {
			super(ShootGame.health, x, y);
			this.amount = amount;
		}
		
		@Override
		protected final boolean give(final Shooter shooter) {
			return shooter.addHealth(amount);
		}
	}
	
	public final static PowerUp newPowerUp(final Shooter defeated) {
		final Shooter victor = ShootGame.shooter;
		final Panple pos = defeated.getPosition();
		final float x = pos.getX(), y = pos.getY();
		final int health = victor.getHealth(), constitution = victor.def.constitution;
	    if (health < constitution / 5) {
	    	return new Health(defeated, x, y);
	    } else if (Mathtil.rand()) {
			return null;
		} else if (health < constitution / 3) {
			return new Health(defeated, x, y);
		} else if (health < constitution && Mathtil.rand(30)) {
			return new Health(defeated, x, y);
		} else if (Mathtil.rand(30)) {
			return new Money(defeated, x, y);
		}
		final Weapon defeatedWeapon = defeated.weapon;
		if (defeatedWeapon != null && defeatedWeapon.getAmmo() > 0) {
			WeaponDefinition defeatedDef = defeatedWeapon.def;
			if (defeatedDef.capacity.min != Weapon.INF) {
				final Weapon victorWeapon = victor.getWeapon(defeatedDef);
				if (victorWeapon != null && victorWeapon.getAmmo() < victorWeapon.getCapacity().getValue()) {
					return new Ammo(defeatedDef, x, y);
				}
			}
		} else if (Mathtil.rand(40)) {
			return new Money(defeated, x, y);
		} else {
			Weapon chosenWeapon = null;
			float ratio = -1;
			for (final Weapon victorWeapon : Coltil.unnull(victor.weapons)) {
				final int ammo = victorWeapon.getAmmo();
				final int capacity = victorWeapon.getCapacity().getValue();
				if (ammo == Weapon.INF || ammo >= capacity) {
					continue;
				}
				final float currRatio = (float) ammo / (float) capacity;
				if (chosenWeapon == null || currRatio < ratio) {
					chosenWeapon = victorWeapon;
					ratio = currRatio;
				}
			}
			if (chosenWeapon != null) {
				return new Ammo(chosenWeapon.def, x, y);
			}
		}
		return new Money(defeated, x, y);
	}
}
