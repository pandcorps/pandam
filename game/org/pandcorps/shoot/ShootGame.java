package org.pandcorps.shoot;

import java.awt.image.BufferedImage;

import org.pandcorps.core.img.Pancolor;
import org.pandcorps.game.*;
import org.pandcorps.game.actor.Guy2.*;
import org.pandcorps.game.core.ImtilX;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.FinPanple;
import org.pandcorps.shoot.Shooter.ShooterDefinition;
import org.pandcorps.shoot.Weapon.WeaponDefinition;

public class ShootGame extends Guy2Game {
	/*package*/ static Guy2Type type = null;
	private static ShooterDefinition playerDef = null;
	/*package*/ static WeaponDefinition[] weaponDefs = null;
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
		chrs = loadChrStrip("Will");
		playerDef = ShooterDefinition.create("Will", chrs);
		loadWeapons();
	}
	
	private final static void loadWeapons() {
		final BufferedImage[] strip = loadStrip("misc/Weapons");
		weaponDefs = new WeaponDefinition[6];
		loadWeapon(0, "Chainsaw", 1, 1, strip, 0);
		loadWeapon(1, "Magnums", 2, 1, strip, 2);
		loadWeapon(2, "Shotgun", 7, 1, strip, 3);
		loadWeapon(3, "Minigun", 2, 1, strip, 4);
		loadWeapon(4, "Flamethrower", 5, 1, strip, 6);
		loadWeapon(5, "RocketLauncher", 6, 1, strip, 7);
	}
	
	private final static void loadWeapon(final int wpnIdx, final String name, final int x, final int y, final BufferedImage[] strip, final int imgIdx) {
		final Panmage img = Pangine.getEngine().createImage("img.wpn." + name, new FinPanple(x, y, 0), null, null, strip[imgIdx]);
		weaponDefs[wpnIdx] = new WeaponDefinition(img);
	}
	
	protected final static BufferedImage[] loadStrip(final String loc) {
		return ImtilX.loadStrip("org/pandcorps/shoot/res/" + loc + ".png");
	}
	
	protected final static BufferedImage[] loadChrStrip(final String name) {
		return loadStrip("chr/" + name);
	}
	
	protected final static class ShootScreen extends Panscreen {
		@Override
        protected final void load() throws Exception {
			Pangine.getEngine().setBgColor(Pancolor.GREEN);
			final Shooter shooter = new Shooter("STR.1", room, playerDef);
			shooter.setWeapon(weaponDefs[1]);
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
