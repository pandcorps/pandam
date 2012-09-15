package org.pandcorps.shoot;

import java.awt.image.BufferedImage;

import org.pandcorps.game.actor.Guy2;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.FinPanple;

public class Shooter extends Guy2 {
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
	
	protected Shooter(final String id, final Panroom room, final ShooterDefinition def) {
		super(id, room, ShootGame.type);
		this.def = def;
		setView(def.still);
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
		return Pangame.getGame().getCurrentRoom().getSize();
	}
}
