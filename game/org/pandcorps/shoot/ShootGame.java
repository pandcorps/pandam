package org.pandcorps.shoot;

import java.awt.image.BufferedImage;

import org.pandcorps.core.img.Pancolor;
import org.pandcorps.game.*;
import org.pandcorps.game.actor.Guy2.*;
import org.pandcorps.game.core.ImtilX;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.FinPanple;
import org.pandcorps.shoot.Projectile.*;
import org.pandcorps.shoot.Shooter.ShooterDefinition;
import org.pandcorps.shoot.Weapon.WeaponDefinition;

public class ShootGame extends Guy2Game {
	/*package*/ static Guy2Type type = null;
	private static ShooterDefinition playerDef = null;
	/*package*/ static WeaponDefinition[] weaponDefs = null;
	private static Panroom room = null;
	/*package*/ static Panimation flameLoopAnm = null;

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
		final Pangine engine = Pangine.getEngine();
		final BufferedImage[] strip = loadStrip("misc/Weapons");
		final BufferedImage[] strip4 = loadStrip("misc/Weapons4", 4);
		final BufferedImage[] strip8 = loadStrip("misc/Weapons8", 8);
		final Panmage projMagImg = engine.createImage("img.proj.mag", strip4[0]);
		final Panmage projShotImg = engine.createImage("img.proj.shot", strip4[1]);
		final Panmage projFlame1Img = engine.createImage("img.proj.flame.1", strip4[3]);
		final Panmage projFlame2Img = engine.createImage("img.proj.flame.2", strip8[0]);
		final Panmage projFlame3Img = engine.createImage("img.proj.flame.3", strip8[1]);
		final Panmage projFlame4Img = engine.createImage("img.proj.flame.4", strip8[2]);
		final Panframe projFlame1Frm = engine.createFrame("frm.proj.flame.1", projFlame1Img, 4);
		final Panframe projFlame2Frm = engine.createFrame("frm.proj.flame.2", projFlame2Img, 4);
		final Panframe projFlame3Frm = engine.createFrame("frm.proj.flame.3", projFlame3Img, 4);
		final Panframe projFlame4Frm = engine.createFrame("frm.proj.flame.4", projFlame4Img, 4);
		final Panimation projFlame1Anm = engine.createAnimation("anm.proj.flame.1", projFlame1Frm, projFlame2Frm);
		flameLoopAnm = engine.createAnimation("anm.proj.flame.2", projFlame3Frm, projFlame4Frm);
		final Panmage projRocketImg = engine.createImage("img.proj.rocket", strip8[3]);
		final Panple velBullet = new FinPanple(3, 0, 0);
		final Emitter magEmit1 = new Emitter(0, 4, velBullet, (byte) -1, projMagImg);
		final Emitter magEmit2 = new Emitter(10, 4, velBullet, (byte) -1, projMagImg);
		final Emitter shotEmit1 = new Emitter(10, 4, new FinPanple(3, 0, 0), (byte) -1, projShotImg);
		final Emitter shotEmit2 = new Emitter(10, 4, new FinPanple(2.8f, .2f, 0), (byte) -1, projShotImg);
		final Emitter shotEmit3 = new Emitter(10, 4, new FinPanple(2.8f, -.2f, 0), (byte) -1, projShotImg);
		final Emitter shotEmit4 = new Emitter(10, 4, new FinPanple(2.6f, .4f, 0), (byte) -1, projShotImg);
		final Emitter shotEmit5 = new Emitter(10, 4, new FinPanple(2.6f, -.4f, 0), (byte) -1, projShotImg);
		final Emitter flameEmit = new Emitter(FlameProjectile.class, 10, 4, velBullet, (byte) 28, projFlame1Anm);
		final Emitter rocketEmit = new Emitter(10, 4, velBullet, (byte) -1, projRocketImg);
		weaponDefs = new WeaponDefinition[6];
		loadWeapon(0, "Chainsaw", 1, 1, strip, 0, null, new Emitter[] {});
		loadWeapon(1, "Magnums", 2, 1, strip, 2, new Emitter[] {magEmit1, magEmit2}, null);
		loadWeapon(2, "Shotgun", 7, 1, strip, 3, new Emitter[] {shotEmit1, shotEmit2, shotEmit3, shotEmit4, shotEmit5}, null);
		loadWeapon(3, "Minigun", 2, 1, strip, 4, null, new Emitter[] {});
		loadWeapon(4, "Flamethrower", 5, 1, strip, 6, null, new Emitter[] {flameEmit});
		loadWeapon(5, "RocketLauncher", 6, 1, strip, 7, new Emitter[] {rocketEmit}, null);
	}
	
	private final static void loadWeapon(final int wpnIdx, final String name, final int x, final int y, final BufferedImage[] strip, final int imgIdx, final Emitter[] attackEmitters, final Emitter[] attackingEmitters) {
		final Panmage img = Pangine.getEngine().createImage("img.wpn." + name, new FinPanple(x, y, 0), null, null, strip[imgIdx]);
		weaponDefs[wpnIdx] = new WeaponDefinition(img, attackEmitters, attackingEmitters);
	}
	
	protected final static BufferedImage[] loadStrip(final String loc) {
		return loadStrip(loc, ImtilX.DIM);
	}
	
	protected final static BufferedImage[] loadStrip(final String loc, final int dim) {
		return ImtilX.loadStrip("org/pandcorps/shoot/res/" + loc + ".png", dim);
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
