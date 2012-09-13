package org.pandcorps.shoot;

import java.awt.image.BufferedImage;

import org.pandcorps.game.*;
import org.pandcorps.game.actor.Guy2.*;
import org.pandcorps.game.core.ImtilX;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.FinPanple;
import org.pandcorps.shoot.Shooter.ShooterDefinition;

public class ShootGame extends Guy2Game {
	/*package*/ static Guy2Type type = null;
	private static ShooterDefinition playerDef = null;
	private static Panroom room = null;

	@Override
	protected void init(final Panroom room) throws Exception {
		loadConstants();
		ShootGame.room = room;
		Story.playIntro();
	}
	
	private final static void loadConstants() {
		final Pangine engine = Pangine.getEngine();
		final BufferedImage[] constantImgs = ImtilX.loadStrip("org/pandcorps/game/res/misc/Constants.png");
		final Panmage shadowImage = engine.createImage("Shadow", new FinPanple(8, 4, 0), null, null, constantImgs[0]);
        type = new Guy2Type(shadowImage, -480);
		final BufferedImage[] chrs;
		chrs = ImtilX.loadStrip("org/pandcorps/shoot/res/chr/Will.png");
		playerDef = ShooterDefinition.create("Will", chrs);
	}
	
	protected final static class ShootScreen extends Panscreen {
		@Override
        protected final void load() throws Exception {
			final Shooter shooter = new Shooter("STR.1", room, playerDef);
			new Player(shooter).setShooter(shooter);
		}
	}
	
	public final static void main(final String[] args) {
        try {
            new ShootGame().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
