package org.pandcorps.shoot;

import java.awt.image.BufferedImage;

import org.pandcorps.core.img.Pancolor;
import org.pandcorps.game.*;
import org.pandcorps.game.actor.Guy2.*;
import org.pandcorps.game.core.ImtilX;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.FinPanple;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;
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
	private static TileMap tm = null;

	@Override
	protected void init(final Panroom room) throws Exception {
		ShootGame.room = room;
		loadConstants();
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
		loadBackground();
		loadWeapons();
	}
	
	private final static void loadBackground() {
		tm = new TileMap("act.bg", room, 16, 16);
		tm.setImageMap(Pangine.getEngine().createImage("img.bg.city", "org/pandcorps/shoot/res/bg/TileCity.png"));
		final TileMapImage[][] imgMap = tm.splitImageMap();
		tm.fillBackground(imgMap[7][0], 0, 2);
		tm.fillBackground(imgMap[6][0], 2, 1);
		tm.fillBackground(imgMap[5][0], 3, 2);
		tm.fillBackground(imgMap[4][1], 5, 1);
		tm.fillBackground(imgMap[3][1], 6, 6);
		tm.getPosition().setZ(type.getDepthShadow() - 1);
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
		final Panmage smoke3Img = engine.createImage("img.smoke.3", o4, null, null, strip4[6]);
		final Panmage smoke4Img = engine.createImage("img.smoke.4", o8, null, null, strip8[4]);
		final Panmage smoke5Img = engine.createImage("img.smoke.5", o8, null, null, strip8[5]);
		final Panframe smoke1Frm = engine.createFrame("frm.smoke.1", smoke1Img, 4);
		final Panframe smoke2Frm = engine.createFrame("frm.smoke.2", smoke2Img, 4);
		final Panframe smoke3Frm = engine.createFrame("frm.smoke.3", smoke3Img, 4);
		final Panframe smoke4Frm = engine.createFrame("frm.smoke.4", smoke4Img, 4);
		final Panframe smoke5Frm = engine.createFrame("frm.smoke.5", smoke5Img, 4);
		final Panimation smokeSmallAnm = engine.createAnimation("anm.smoke.small", smoke1Frm, smoke2Frm, smoke3Frm);
		smokeBigAnm = engine.createAnimation("anm.smoke.big", smoke1Frm, smoke2Frm, smoke4Frm, smoke5Frm);
		final Panmage flashSmall1Img = engine.createImage("img.flash.small.1", o4, null, null, strip4[9]);
		final Panmage flashSmall2Img = engine.createImage("img.flash.small.2", o4, null, null, strip4[10]);
		final Panframe flashSmall1Frm = engine.createFrame("frm.flash.small.1", flashSmall1Img, 3);
		final Panframe flashSmall2Frm = engine.createFrame("frm.flash.small.2", flashSmall2Img, 3);
		final Panimation flashSmallAnm = engine.createAnimation("anm.flash.small", flashSmall1Frm, flashSmall2Frm);
		final Panframe flashFast1Frm = engine.createFrame("frm.flash.fast.1", flashSmall1Img, 1);
		final Panframe flashFast2Frm = engine.createFrame("frm.flash.fast.2", flashSmall2Img, 1);
		final Panimation flashFastAnm = engine.createAnimation("anm.flash.fast", flashFast1Frm, flashFast2Frm);
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
		final Panmage projSawImg = engine.createEmptyImage("img.proj.saw", o4, null, null);
		final Panmage projMagImg = engine.createImage("img.proj.mag", new FinPanple(2, 1, 0), null, null, strip4[0]);
		final Panmage projShotImg = engine.createImage("img.proj.shot", new FinPanple(1, 2, 0), null, null, strip4[1]);
		final Panmage projMiniImg = engine.createImage("img.proj.mini", o4, null, null, strip4[2]);
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
		final Emitter sawEmit = new Emitter(15, 1, FinPanple.ORIGIN, (byte) 1, projSawImg);
		final Emitter magEmit1 = new Emitter(7, 5, velBullet, (byte) -1, projMagImg);
		final Emitter magEmit2 = new Emitter(16, 5, velBullet, (byte) -1, projMagImg);
		final float sx = 11, sy = 5;
		final Emitter shotEmit1 = new Emitter(sx, sy, new FinPanple(3, 0, 0), (byte) -1, projShotImg);
		final Emitter shotEmit2 = new Emitter(sx, sy, new FinPanple(2.8f, .2f, 0), (byte) -1, projShotImg);
		final Emitter shotEmit3 = new Emitter(sx, sy, new FinPanple(2.8f, -.2f, 0), (byte) -1, projShotImg);
		final Emitter shotEmit4 = new Emitter(sx, sy, new FinPanple(2.6f, .4f, 0), (byte) -1, projShotImg);
		final Emitter shotEmit5 = new Emitter(sx, sy, new FinPanple(2.6f, -.4f, 0), (byte) -1, projShotImg);
		final Emitter miniEmit = new Emitter(15, 3, new FinPanple(4, 0, 0), (byte) -1, projMiniImg);
		final Emitter flameEmit = new Emitter(FlameProjectile.class, 10, 4, velBullet, (byte) 28, projFlame1Anm);
		final Emitter rocketEmit = new Emitter(RocketProjectile.class, 13, 8, velBullet, (byte) -1, projRocketImg);
		weaponDefs = new WeaponDefinition[6];
		loadWeapon(0, "Chainsaw", 1, 1, strip, 0, null, null, smokeSmallAnm, 1, null, new Emitter[] {sawEmit}, 0);
		loadWeapon(1, "Magnums", 2, 1, strip, 2, flashSmallAnm, casingAnm, null, -1, new Emitter[] {magEmit1, magEmit2}, null, 8);
		loadWeapon(2, "Shotgun", 7, 1, strip, 3, flashSmallAnm, casingAnm, null, -1, new Emitter[] {shotEmit1, shotEmit2, shotEmit3, shotEmit4, shotEmit5}, null, 12);
		loadWeapon(3, "Minigun", 2, 1, strip, 4, flashFastAnm, casingAnm, null, 5, null, new Emitter[] {miniEmit}, 1);
		loadWeapon(4, "Flamethrower", 5, 1, strip, 6, null, null, null, -1, null, new Emitter[] {flameEmit}, 0);
		loadWeapon(5, "RocketLauncher", 6, 1, strip, 7, flashBigAnm, null, null, -1, new Emitter[] {rocketEmit}, null, 20);
	}
	
	private final static void loadWeapon(final int wpnIdx, final String name,
			final int x, final int y, final BufferedImage[] strip, final int imgIdx,
			final Panimation flash, final Panimation casing, final Panimation smoke, final int imgIdx2,
			final Emitter[] attackEmitters, final Emitter[] attackingEmitters,
			final int delay) {
		final Pangine engine = Pangine.getEngine();
		final String imgName = "img.wpn." + name;
		final FinPanple o = new FinPanple(x, y, 0);
		final Panmage img = engine.createImage(imgName, o, null, null, strip[imgIdx]);
		final Panimation attack;
		if (imgIdx2 < 0) {
			attack = null;
		} else  {
			final Panmage img2 = engine.createImage(imgName + ".2", o, null, null, strip[imgIdx2]);
			final String frmName = "frm.wpn." + name;
			final Panframe frm1 = engine.createFrame(frmName, img, 3);
			final Panframe frm2 = engine.createFrame(frmName + ".2", img2, 3);
			attack = engine.createAnimation("anm.wpn." + name, frm1, frm2);
		}
		weaponDefs[wpnIdx] = new WeaponDefinition(img, flash, casing, smoke, attack, attackEmitters, attackingEmitters, delay);
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
			room.addActor(tm);
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
