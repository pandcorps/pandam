/*
Copyright (c) 2009-2014, Andrew M. Martin
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
import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.shoot.Weapon.*;

public class Shooter extends Guy2 implements CollisionListener {
	/*
	10 regular upgrades, 50400 to max each.
	504000 for all regular maximums.
	4 infinite ammo upgrades (magnums already infinite), 50400 each.
	705600 for all weapon upgrades.
	*/
	/*package*/ final static int MAX_MONEY = 999999;
	/*package*/ final static int MAX_LEVEL = 12;
	/*package*/ final static int OFF_ADD_Y = 6;
	
	public final static class ShooterDefinition {
	    private final int baseConstitution;
	    protected final int melee;
	    protected final WeaponDefinition weapon;
		private final Panimation still;
		protected final Panimation walk;
		
		public final static ShooterDefinition create(final String name, final int constitution, final Img... imgs) {
			return create(name, constitution, 0, null, imgs);
		}
		
		public final static ShooterDefinition create(final String name, final int constitution, final int melee, final WeaponDefinition weapon, final Img... imgs) {
			final Pangine engine = Pangine.getEngine();
			final String pre = name + '.';
			final String ipre = pre + "img.", fpre = pre + "frm.", apre = pre + "anm.";
			final FinPanple o = new FinPanple(8, 1, 0);
			final Panmage stillImg = engine.createImage(ipre + "still", o, null, null, imgs[0]);
			final Panmage leftImg = engine.createImage(ipre + "left", o, null, null, imgs[1]);
			final Panmage rightImg = engine.createImage(ipre + "right", o, null, null, imgs[2]);
			final Panframe stillFrm = engine.createFrame(fpre + "still", stillImg, 4);
			final Panframe leftFrm = engine.createFrame(fpre + "left", leftImg, 4);
			final Panframe rightFrm = engine.createFrame(fpre + "right", rightImg, 4);
			final Panimation stillAnm = engine.createAnimation(apre + "still", stillFrm);
			final Panimation walkAnm = engine.createAnimation(apre + "walk", leftFrm, stillFrm, rightFrm, stillFrm);
			return new ShooterDefinition(constitution, melee, weapon, stillAnm, walkAnm);
		}
		
		public ShooterDefinition(final int constitution, final int melee, final WeaponDefinition weapon, final Panimation still, final Panimation walk) {
		    this.baseConstitution = constitution;
		    this.melee = melee;
		    this.weapon = weapon;
			this.still = still;
			this.walk = walk;
		}
		
		public boolean isBoss() {
			return baseConstitution >= 300;
		}
	}
	
	/*package*/ final ShooterDefinition def;
	/*package*/ ArrayList<Weapon> weapons = null;
	/*package*/ Weapon weapon = null;
	private Attribute health;
	private int constitution;
	private int money;
	private int experience = 0;
	/*package*/ Spawner spawner = null;
	
	protected Shooter(final String id, final Panlayer room, final ShooterDefinition def) {
		super(id, room, ShootGame.type);
		this.def = def;
		constitution = def.baseConstitution;
		health = new Attribute(constitution) {@Override public int max() {return constitution;}};
		setView(def.still);
	}
	
	@Override
    public final void onStep(final StepEvent event) {
		super.onStep(event);
		if (weapon != null) {
			updateView(); // Make sure current frame is accurate for synchronization
			final Panple pos = getPosition();
			final int off = (getView() == def.walk && getCurrentFrame() % 2 == 0) ? 1 : 0;
			// Similar to shadow, except for depth and offset
			weapon.getPosition().set(pos.getX(), pos.getY() + off, pos.getZ() + 1);
			weapon.setMirror(isMirror());
			weapon.step();
		}
	}
	
	@Override
    public void onCollision(final CollisionEvent event) {
		final Collidable c = event.getCollider();
		if (c instanceof Collidee) {
			((Collidee) c).onCollision(this, event);
		} else if (controller != null && c instanceof Shooter) {
			((ShooterController) controller).onCollision((Shooter) c);
		}
	}
	
	/*package*/ void onHurt(final Projectile p) {
		onHurt(p.weapon.getPower().getValue());
		if (controller != null) {
			((ShooterController) controller).onHurt(p);
		}
	}
	
	/*package*/ void onHurt(final int damage) {
	    if (health.isInfinite()) {
	        return; // Skip burst/destroy, not just dec
	    }
		health.dec(damage);
		if (getHealth() <= 0) {
			add(new Burst(ShootGame.puff), 0, 0, 0);
			if (controller != null) {
				((ShooterController) controller).onDestroy();
			}
			destroy();
		}
	}
	
	/*package*/ int getHealth() {
		return health.get();
	}
	
	/*package*/ boolean addHealth(final int health) {
		return this.health.inc(health);
	}
	
	/*package*/ int getConstitution() {
	    return constitution;
	}
	
	/*package*/ int getMoney() {
		return money;
	}
	
	/*package*/ void addMoney(final int money) {
		if (money <= 0) {
			throw new IllegalArgumentException("Cannot add " + money + " money");
		}
		this.money = Math.min(this.money + money, MAX_MONEY);
	}
	
	/*package*/ boolean subtractMoney(final int money) {
		if (money <= 0) {
			throw new IllegalArgumentException("Cannot subtract " + money + " money");
		} else if (this.money < money) {
			return false;
		}
		this.money -= money;
		return true;
	}
	
	/*package*/ void addExperience(final int experience) {
		if (experience <= 0) {
			throw new IllegalArgumentException("Cannot add " + experience + " experience");
		}
		final int oldLevel = getLevel();
		this.experience += experience;
		final int newLevel = getLevel();
		for (int i = oldLevel; i < newLevel; i++) {
		    if (i == (MAX_LEVEL - 1)) {
		        constitution = Weapon.INF;
		    } else {
		        constitution += 20;
		    }
		    health.set(constitution);
		}
	}
	
	/*package*/ int getLevel() {
	    /*
	    1    2    3    4    5    6    7    8    9   10   11   12
	    0 2000 2100 2200 2300 2400 2500 2600 2700 2800 2900 3000
	    */
	    int level = 1, total = 2000, next = 2100;
	    while (experience < total) {
	        level++;
	        if (level == MAX_LEVEL) {
	            break;
	        }
	        total += next;
	        next += 100;
	    }
	    return level;
	}
	
	public boolean isBoss() {
		return def.isBoss();
	}
	
	@Override
	protected final void onDestroy() {
		super.onDestroy();
		Panctor.destroy(weapon);
		for (final Weapon w : Coltil.unnull(weapons)) {
			Panctor.destroy(w);
		}
		if (spawner != null) {
			spawner.remove();
		}
	}
	
	@Override
	protected final void onDetach() {
	    super.onDetach();
	    detach(weapon);
	}
	
	@Override
	public void attach(final Panlayer room) {
	    super.attach(room);
	    final Weapon w = weapon;
	    weapon = null;
	    setWeapon(w);
	}
	
	protected boolean onInteract(final Shooter initiator) {
		if (controller != null) {
			return ((ShooterController) controller).onInteract(initiator);
		}
		return false;
	}
	
	protected void attack() {
		if (weapon == null) {
			return;
		}
		weapon.attack(this, weapon.def.attackEmitters);
	}
	
	protected void attacking() {
		if (weapon == null) {
			return;
		}
		weapon.attack(this, weapon.def.attackingEmitters);
	}
	
	protected void interact() {
		add(new Interactor(this), 4, 0, 0);
	}
	
	protected void weapon1() {
	    weapon(0);
	}
	
	protected void weapon2() {
	    weapon(1);
	}
	
	protected void weapon3() {
	    weapon(2);
	}
	
	protected void weapon4() {
	    weapon(3);
	}
	
	protected void weapon5() {
	    weapon(4);
	}
	
	protected void weapon6() {
	    weapon(5);
	}
	
	protected final void weapon(final int i) {
	    //setWeapon(ShootGame.weaponDefs[i]);
        setWeapon(Coltil.get(weapons, i));
	}
	
	/*protected void setWeapon(final WeaponDefinition wdef) {
		Panctor.destroy(weapon);
		weapon = new Weapon(wdef);
		Pangame.getGame().getCurrentRoom().addActor(weapon);
	}*/
	
	protected void setWeapon(final Weapon weapon) {
	    if (weapon == null) {
	        return;
	    }
	    setWeaponIntern(weapon);
	}
	
	private void setWeaponIntern(final Weapon weapon) {
	    if (weapon == this.weapon) {
	        return;
	    } else if (this.weapon != null) {
            this.weapon.getLayer().removeActor(this.weapon);
        }
        this.weapon = weapon;
        if (weapon != null) {
        	Pangame.getGame().getCurrentRoom().addActor(weapon);
        }
    }
	
	protected Weapon addWeapon(final int i) {
		Weapon w = Coltil.get(weapons, i);
	    if (w != null) {
	        return w;
	    } else if (weapons == null) {
	        weapons = new ArrayList<Weapon>(ShootGame.weaponDefs.length);
	    }
	    w = new Weapon(ShootGame.weaponDefs[i]);
	    Coltil.set(weapons, i, w);
	    return w;
	}
	
	protected void chooseWeapon() {
	    if (weapon != null && weapon.getAmmo() > 0) {
	        return;
	    }
	    for (final Weapon w : Coltil.unnull(weapons)) {
	        if (w != null && w.getAmmo() > 0) {
	            setWeapon(w);
	            return;
	        }
	    }
	    setWeaponIntern(null);
	}
	
	protected Weapon getWeapon(final WeaponDefinition def) {
		for (final Weapon w : Coltil.unnull(weapons)) {
			if (w.def.equals(def)) {
				return w;
			}
		}
		return null;
	}

	@Override
	protected Panimation getStill() {
		return def.still;
	}

	@Override
	protected Panimation getWalk() {
		return def.walk;
	}

	@Override
	protected Panple getMin() {
		return ShootGame.min;
	}

	@Override
	protected Panple getMax() {
		return ShootGame.max;
	}
}
