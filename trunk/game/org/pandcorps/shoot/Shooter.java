package org.pandcorps.shoot;

import java.awt.image.BufferedImage;

import org.pandcorps.core.*;
import org.pandcorps.core.col.IdentityHashSet;
import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.FinPanple;
import org.pandcorps.shoot.Weapon.WeaponDefinition;

public class Shooter extends Guy2 implements CollisionListener {
	public final static class ShooterDefinition {
		private final Panimation still;
		protected final Panimation walk;
		
		public final static ShooterDefinition create(final String name, final BufferedImage... imgs) {
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
			return new ShooterDefinition(stillAnm, walkAnm);
		}
		
		public ShooterDefinition(final Panimation still, final Panimation walk) {
			this.still = still;
			this.walk = walk;
		}
	}
	
	/*package*/ final ShooterDefinition def;
	private Weapon weapon = null;
	
	protected Shooter(final String id, final Panroom room, final ShooterDefinition def) {
		super(id, room, ShootGame.type);
		this.def = def;
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
		if (c.getClass() == Projectile.class) {
			final Projectile p = (Projectile) c;
			if (this == p.shooter) {
				return;
			} else if (Coltil.contains(p.victims, this)) {
				return;
			}
			if (p.victims == null) {
				p.victims = new IdentityHashSet<Shooter>();
			}
			p.victims.add(this);
			add(new Burst(ShootGame.blood), Mathtil.randf(-4, 4), Mathtil.randf(2, 10), 1f);
		}
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
	
	protected void weapon1() {
		setWeapon(ShootGame.weaponDefs[0]);
	}
	
	protected void weapon2() {
		setWeapon(ShootGame.weaponDefs[1]);
	}
	
	protected void weapon3() {
		setWeapon(ShootGame.weaponDefs[2]);
	}
	
	protected void weapon4() {
		setWeapon(ShootGame.weaponDefs[3]);
	}
	
	protected void weapon5() {
		setWeapon(ShootGame.weaponDefs[4]);
	}
	
	protected void weapon6() {
		setWeapon(ShootGame.weaponDefs[5]);
	}
	
	protected void setWeapon(final WeaponDefinition wdef) {
		Panctor.destroy(weapon);
		weapon = new Weapon(wdef);
		Pangame.getGame().getCurrentRoom().addActor(weapon);
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
