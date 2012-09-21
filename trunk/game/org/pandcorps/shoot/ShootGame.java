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
	/*package*/ static Panimation smokeBigAnm = null;
	/*package*/ static Panimation flameLoopAnm = null;
	/*package*/ static Panimation rocketFireAnm = null;

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
		final FinPanple o8 = new FinPanple(4, 4, 0);
		final FinPanple o4 = new FinPanple(2, 2, 0);
		final Panmage smoke1Img = engine.createImage("img.smoke.1", o4, null, null, strip4[4]);
		final Panmage smoke2Img = engine.createImage("img.smoke.2", o4, null, null, strip4[5]);
		final Panmage smoke4Img = engine.createImage("img.smoke.4", o8, null, null, strip8[4]);
		final Panmage smoke5Img = engine.createImage("img.smoke.5", o8, null, null, strip8[5]);
		final Panframe smoke1Frm = engine.createFrame("frm.smoke.1", smoke1Img, 4);
		final Panframe smoke2Frm = engine.createFrame("frm.smoke.2", smoke2Img, 4);
		final Panframe smoke4Frm = engine.createFrame("frm.smoke.4", smoke4Img, 4);
		final Panframe smoke5Frm = engine.createFrame("frm.smoke.5", smoke5Img, 4);
		smokeBigAnm = engine.createAnimation("anm.smoke.big", smoke1Frm, smoke2Frm, smoke4Frm, smoke5Frm);
		final Panmage flashSmall1Img = engine.createImage("img.flash.small.1", o4, null, null, strip4[9]);
		final Panmage flashSmall2Img = engine.createImage("img.flash.small.2", o4, null, null, strip4[10]);
		final Panframe flashSmall1Frm = engine.createFrame("frm.flash.small.1", flashSmall1Img, 2);
		final Panframe flashSmall2Frm = engine.createFrame("frm.flash.small.2", flashSmall2Img, 2);
		final Panimation flashSmallAnm = engine.createAnimation("anm.flash.small", flashSmall1Frm, flashSmall2Frm);
		final Panmage flashBig1Img = engine.createImage("img.flash.big.1", o8, null, null, strip8[6]);
		final Panmage flashBig2Img = engine.createImage("img.flash.big.2", o8, null, null, strip8[7]);
		final Panframe flashBig1Frm = engine.createFrame("frm.flash.big.1", flashBig1Img, 5);
		final Panframe flashBig2Frm = engine.createFrame("frm.flash.big.2", flashBig2Img, 5);
		final Panimation flashBigAnm = engine.createAnimation("anm.flash.big", flashBig1Frm, flashBig2Frm);
		final Panmage casing1Img = engine.createImage("img.casing.1", o4, null, null, strip4[7]);
		final Panmage casing2Img = engine.createImage("img.casing.2", o4, null, null, strip4[8]);
		final int cd = 2;
		final Panframe casing1Frm = engine.createFrame("frm.casing.1", casing1Img, cd);
		final Panframe casing2Frm = engine.createFrame("frm.casing.2", casing2Img, cd);
		final Panframe casing3Frm = engine.createFrame("frm.casing.3", casing1Img, cd, 1, false, false);
        final Panframe casing4Frm = engine.createFrame("frm.casing.4", casing2Img, cd, 1, false, false);
        final Panframe casing5Frm = engine.createFrame("frm.casing.5", casing1Img, cd, 2, false, false);
        final Panframe casing6Frm = engine.createFrame("frm.casing.6", casing2Img, cd, 2, false, false);
		final Panimation casingAnm = engine.createAnimation("anm.casing", casing1Frm, casing2Frm, casing3Frm, casing4Frm, casing5Frm, casing6Frm);
		final Panmage projMagImg = engine.createImage("img.proj.mag", new FinPanple(2, 1, 0), null, null, strip4[0]);
		final Panmage projShotImg = engine.createImage("img.proj.shot", new FinPanple(1, 2, 0), null, null, strip4[1]);
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
		final Panmage projRocketImg = engine.createImage("img.proj.rocket", o8, null, null, strip8[3]);
		final Panmage projRocketFire1Img = engine.createImage("img.proj.rocket.fire.1", o4, null, null, strip4[11]);
		final Panmage projRocketFire2Img = engine.createImage("img.proj.rocket.fire.2", o4, null, null, strip4[12]);
		final Panframe projRocketFire1Frm = engine.createFrame("frm.proj.rocket.fire.1", projRocketFire1Img, 3);
		final Panframe projRocketFire2Frm = engine.createFrame("frm.proj.rocket.fire.2", projRocketFire2Img, 3);
		rocketFireAnm = engine.createAnimation("anm.proj.rocket.fire.1", projRocketFire1Frm, projRocketFire2Frm);
		final Panple velBullet = new FinPanple(3, 0, 0);
		final Emitter magEmit1 = new Emitter(7, 5, velBullet, (byte) -1, projMagImg);
		final Emitter magEmit2 = new Emitter(16, 5, velBullet, (byte) -1, projMagImg);
		final float sx = 11, sy = 5;
		final Emitter shotEmit1 = new Emitter(sx, sy, new FinPanple(3, 0, 0), (byte) -1, projShotImg);
		final Emitter shotEmit2 = new Emitter(sx, sy, new FinPanple(2.8f, .2f, 0), (byte) -1, projShotImg);
		final Emitter shotEmit3 = new Emitter(sx, sy, new FinPanple(2.8f, -.2f, 0), (byte) -1, projShotImg);
		final Emitter shotEmit4 = new Emitter(sx, sy, new FinPanple(2.6f, .4f, 0), (byte) -1, projShotImg);
		final Emitter shotEmit5 = new Emitter(sx, sy, new FinPanple(2.6f, -.4f, 0), (byte) -1, projShotImg);
		final Emitter flameEmit = new Emitter(FlameProjectile.class, 10, 4, velBullet, (byte) 28, projFlame1Anm);
		final Emitter rocketEmit = new Emitter(RocketProjectile.class, 13, 8, velBullet, (byte) -1, projRocketImg);
		weaponDefs = new WeaponDefinition[6];
		loadWeapon(0, "Chainsaw", 1, 1, strip, 0, null, null, null, new Emitter[] {});
		loadWeapon(1, "Magnums", 2, 1, strip, 2, flashSmallAnm, casingAnm, new Emitter[] {magEmit1, magEmit2}, null);
		loadWeapon(2, "Shotgun", 7, 1, strip, 3, flashSmallAnm, casingAnm, new Emitter[] {shotEmit1, shotEmit2, shotEmit3, shotEmit4, shotEmit5}, null);
		loadWeapon(3, "Minigun", 2, 1, strip, 4, flashSmallAnm, casingAnm, null, new Emitter[] {});
		loadWeapon(4, "Flamethrower", 5, 1, strip, 6, null, null, null, new Emitter[] {flameEmit});
		loadWeapon(5, "RocketLauncher", 6, 1, strip, 7, flashBigAnm, null, new Emitter[] {rocketEmit}, null);
	}
	
	private final static void loadWeapon(final int wpnIdx, final String name, final int x, final int y, final BufferedImage[] strip, final int imgIdx, final Panimation flash, final Panimation casing, final Emitter[] attackEmitters, final Emitter[] attackingEmitters) {
		final Panmage img = Pangine.getEngine().createImage("img.wpn." + name, new FinPanple(x, y, 0), null, null, strip[imgIdx]);
		weaponDefs[wpnIdx] = new WeaponDefinition(img, flash, casing, attackEmitters, attackingEmitters);
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
