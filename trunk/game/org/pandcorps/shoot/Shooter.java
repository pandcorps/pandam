package org.pandcorps.shoot;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.pandcorps.core.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.FinPanple;

public class Shooter extends Guy2 implements CollisionListener {
	/*package*/ final static int OFF_ADD_Y = 6;
	
	public final static class ShooterDefinition {
	    private final int constitution;
	    protected final int melee;
		private final Panimation still;
		protected final Panimation walk;
		
		public final static ShooterDefinition create(final String name, final int constitution, final int melee, final BufferedImage... imgs) {
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
			return new ShooterDefinition(constitution, melee, stillAnm, walkAnm);
		}
		
		public ShooterDefinition(final int constitution, final int melee, final Panimation still, final Panimation walk) {
		    this.constitution = constitution;
		    this.melee = melee;
			this.still = still;
			this.walk = walk;
		}
	}
	
	/*package*/ final ShooterDefinition def;
	/*package*/ ArrayList<Weapon> weapons = null;
	/*package*/ Weapon weapon = null;
	private int health;
	
	protected Shooter(final String id, final Panroom room, final ShooterDefinition def) {
		super(id, room, ShootGame.type);
		this.def = def;
		health = def.constitution;
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
	    if (health == Weapon.INF) {
	        return;
	    }
		health -= damage;
		if (health <= 0) {
			add(new Burst(ShootGame.puff), 0, 0, 0);
			destroy();
		}
	}
	
	@Override
	protected final void onDestroy() {
		Panctor.destroy(weapon);
		for (final Weapon w : Coltil.unnull(weapons)) {
			Panctor.destroy(w);
		}
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
	
	private final void weapon(final int i) {
	    //setWeapon(ShootGame.weaponDefs[i]);
        setWeapon(Coltil.get(weapons, i));
	}
	
	/*protected void setWeapon(final WeaponDefinition wdef) {
		Panctor.destroy(weapon);
		weapon = new Weapon(wdef);
		Pangame.getGame().getCurrentRoom().addActor(weapon);
	}*/
	
	protected void setWeapon(final Weapon weapon) {
	    if (weapon == null || weapon == this.weapon) {
	        return;
	    } else if (this.weapon != null) {
            this.weapon.getLayer().removeActor(this.weapon);
        }
        this.weapon = weapon;
        Pangame.getGame().getCurrentRoom().addActor(weapon);
    }
	
	protected void addWeapon(final int i) {
	    if (Coltil.get(weapons, i) != null) {
	        return;
	    } else if (weapons == null) {
	        weapons = new ArrayList<Weapon>(ShootGame.weaponDefs.length);
	    }
	    Coltil.set(weapons, i, new Weapon(ShootGame.weaponDefs[i]));
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
		return FinPanple.ORIGIN;
	}

	@Override
	protected Panple getMax() {
		return ShootGame.max;
	}
}
