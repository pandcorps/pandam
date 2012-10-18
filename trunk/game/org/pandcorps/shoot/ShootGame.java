package org.pandcorps.shoot;

import java.awt.image.BufferedImage;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.core.img.scale.PandScaler;
import org.pandcorps.game.*;
import org.pandcorps.game.actor.Guy2.*;
import org.pandcorps.game.core.ImtilX;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.FinPanple;
import org.pandcorps.pandax.text.Font;
import org.pandcorps.pandax.text.Fonts;
import org.pandcorps.pandax.text.Fonts.FontRequest;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.pandax.visual.FadeScreen;
import org.pandcorps.shoot.Projectile.*;
import org.pandcorps.shoot.Shooter.ShooterDefinition;
import org.pandcorps.shoot.Weapon.WeaponDefinition;

public class ShootGame extends Guy2Game {
	/*package*/ static Guy2Type type = null;
	private static ShooterDefinition playerDef = null;
	private static ShooterDefinition merchantDef = null;
	/*package*/ static ShooterDefinition[] trooperDefs = null;
	/*package*/ static WeaponDefinition[] weaponDefs = null;
	private static Panmage title = null;
	/*package*/ static Panimation blood = null;
	/*package*/ static Panimation explosion = null;
	/*package*/ static Panimation puff = null;
	/*package*/ static Panmage interact = null;
	/*package*/ static Font font = null;
	private static Panroom room = null;
	/*package*/ static FinPanple max = null;
	/*package*/ static Panimation smokeBigAnm = null;
	/*package*/ static Panimation flameLoopAnm = null;
	/*package*/ static Panimation rocketFireAnm = null;
	private static TileMap tm = null;

	@Override
	protected void init(final Panroom room) throws Exception {
		ShootGame.room = room;
		loadConstants();
		Panscreen.set(new LogoScreen(TitleScreen.class));
	}
	
	private final static void loadConstants() {
		final Pangine engine = Pangine.getEngine();
		engine.setTitle("Will Killem");
		title = createImage("misc/Title", 128);
        final BufferedImage[] constantImgs = loadConstantImgs();
		final Panmage shadowImg = engine.createImage("img.shadow", new FinPanple(8, 4, 0), null, null, constantImgs[0]);
        type = new Guy2Type(shadowImg, -480);
        blood = createBloodAnm(constantImgs, 2);
        explosion = createExplosionAnm(constantImgs, 3);
        puff = createPuffAnm(constantImgs, 1);
        interact = engine.createEmptyImage("img.interact", new FinPanple(1, 1, 1), new FinPanple(0, 0, 0), new FinPanple(2, 2, 2));
        font = Fonts.getSimple(new FontRequest(8), Pancolor.BLUE, Pancolor.CYAN, Pancolor.CYAN, Pancolor.BLACK);
		loadCharacters();
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
		tm.getTile(7, 6).setBackground(imgMap[3][2]);
		tm.getTile(8, 6).setBackground(imgMap[1][4]);
		tm.getTile(9, 6).setBackground(imgMap[3][0]);
		tm.getTile(21, 6).setBackground(imgMap[3][2]);
		tm.getTile(22, 6).setBackground(imgMap[2][4]);
		tm.getTile(23, 6).setBackground(imgMap[3][0]);
		tm.getPosition().setZ(type.getDepthShadow() - 1);
	}
	
	private final static void loadCharacters() {
		playerDef = ShooterDefinition.create("Will", loadChrStrip("Will"));
		final int numTrps = 5;
		trooperDefs = new ShooterDefinition[numTrps];
		for (int i = 0; i < trooperDefs.length; i++) {
			trooperDefs[i] = getTrp(i + 4);
		}
		merchantDef = ShooterDefinition.create("Merchant", loadChrStrip("Merchant"));
	}
	
	private final static void loadWeapons() {
		final Pangine engine = Pangine.getEngine();
		final BufferedImage[] strip = loadStrip("misc/Weapons");
		final BufferedImage[] strip4 = loadStrip("misc/Weapons4", 4);
		final BufferedImage[] strip8 = loadStrip("misc/Weapons8", 8);
		final Panmage smoke1Img = engine.createImage("img.smoke.1", CENTER_4, null, null, strip4[4]);
		final Panmage smoke2Img = engine.createImage("img.smoke.2", CENTER_4, null, null, strip4[5]);
		final Panmage smoke3Img = engine.createImage("img.smoke.3", CENTER_4, null, null, strip4[6]);
		final Panmage smoke4Img = engine.createImage("img.smoke.4", CENTER_8, null, null, strip8[4]);
		final Panmage smoke5Img = engine.createImage("img.smoke.5", CENTER_8, null, null, strip8[5]);
		final Panframe smoke1Frm = engine.createFrame("frm.smoke.1", smoke1Img, 4);
		final Panframe smoke2Frm = engine.createFrame("frm.smoke.2", smoke2Img, 4);
		final Panframe smoke3Frm = engine.createFrame("frm.smoke.3", smoke3Img, 4);
		final Panframe smoke4Frm = engine.createFrame("frm.smoke.4", smoke4Img, 4);
		final Panframe smoke5Frm = engine.createFrame("frm.smoke.5", smoke5Img, 4);
		final Panimation smokeSmallAnm = engine.createAnimation("anm.smoke.small", smoke1Frm, smoke2Frm, smoke3Frm);
		smokeBigAnm = engine.createAnimation("anm.smoke.big", smoke1Frm, smoke2Frm, smoke4Frm, smoke5Frm);
		final Panmage flashSmall1Img = engine.createImage("img.flash.small.1", CENTER_4, null, null, strip4[9]);
		final Panmage flashSmall2Img = engine.createImage("img.flash.small.2", CENTER_4, null, null, strip4[10]);
		final Panframe flashSmall1Frm = engine.createFrame("frm.flash.small.1", flashSmall1Img, 3);
		final Panframe flashSmall2Frm = engine.createFrame("frm.flash.small.2", flashSmall2Img, 3);
		final Panimation flashSmallAnm = engine.createAnimation("anm.flash.small", flashSmall1Frm, flashSmall2Frm);
		final Panframe flashFast1Frm = engine.createFrame("frm.flash.fast.1", flashSmall1Img, 1);
		final Panframe flashFast2Frm = engine.createFrame("frm.flash.fast.2", flashSmall2Img, 1);
		final Panimation flashFastAnm = engine.createAnimation("anm.flash.fast", flashFast1Frm, flashFast2Frm);
		final Panmage flashBig1Img = engine.createImage("img.flash.big.1", CENTER_8, null, null, strip8[6]);
		final Panmage flashBig2Img = engine.createImage("img.flash.big.2", CENTER_8, null, null, strip8[7]);
		final Panframe flashBig1Frm = engine.createFrame("frm.flash.big.1", flashBig1Img, 5);
		final Panframe flashBig2Frm = engine.createFrame("frm.flash.big.2", flashBig2Img, 5);
		final Panimation flashBigAnm = engine.createAnimation("anm.flash.big", flashBig1Frm, flashBig2Frm);
		final Panmage casing1Img = engine.createImage("img.casing.1", CENTER_4, null, null, strip4[7]);
		final Panmage casing2Img = engine.createImage("img.casing.2", CENTER_4, null, null, strip4[8]);
		final int cd = 2;
		final Panframe casing1Frm = engine.createFrame("frm.casing.1", casing1Img, cd);
		final Panframe casing2Frm = engine.createFrame("frm.casing.2", casing2Img, cd);
		final Panframe casing3Frm = engine.createFrame("frm.casing.3", casing1Img, cd, 1, false, false);
        final Panframe casing4Frm = engine.createFrame("frm.casing.4", casing2Img, cd, 1, false, false);
        final Panframe casing5Frm = engine.createFrame("frm.casing.5", casing1Img, cd, 2, false, false);
        final Panframe casing6Frm = engine.createFrame("frm.casing.6", casing2Img, cd, 2, false, false);
		final Panimation casingAnm = engine.createAnimation("anm.casing", casing1Frm, casing2Frm, casing3Frm, casing4Frm, casing5Frm, casing6Frm);
		final Panmage projSawImg = engine.createEmptyImage("img.proj.saw", CENTER_4, null, null);
		final Panmage projMagImg = engine.createImage("img.proj.mag", new FinPanple(2, 1, 0), null, null, strip4[0]);
		final Panmage projShotImg = engine.createImage("img.proj.shot", new FinPanple(1, 2, 0), null, null, strip4[1]);
		final Panmage projMiniImg = engine.createImage("img.proj.mini", CENTER_4, null, null, strip4[2]);
		final Panmage projFlame1Img = engine.createImage("img.proj.flame.1", CENTER_4, null, null, strip4[3]);
		final Panmage projFlame2Img = engine.createImage("img.proj.flame.2", CENTER_8, null, null, strip8[0]);
		final Panmage projFlame3Img = engine.createImage("img.proj.flame.3", CENTER_8, null, null, strip8[1]);
		final Panmage projFlame4Img = engine.createImage("img.proj.flame.4", CENTER_8, null, null, strip8[2]);
		final Panframe projFlame1Frm = engine.createFrame("frm.proj.flame.1", projFlame1Img, 4);
		final Panframe projFlame2Frm = engine.createFrame("frm.proj.flame.2", projFlame2Img, 4);
		final Panframe projFlame3Frm = engine.createFrame("frm.proj.flame.3", projFlame3Img, 4);
		final Panframe projFlame4Frm = engine.createFrame("frm.proj.flame.4", projFlame4Img, 4);
		final Panimation projFlame1Anm = engine.createAnimation("anm.proj.flame.1", projFlame1Frm, projFlame2Frm);
		flameLoopAnm = engine.createAnimation("anm.proj.flame.2", projFlame3Frm, projFlame4Frm);
		final Panmage projRocketImg = engine.createImage("img.proj.rocket", CENTER_8, null, null, strip8[3]);
		final Panmage projRocketFire1Img = engine.createImage("img.proj.rocket.fire.1", CENTER_4, null, null, strip4[11]);
		final Panmage projRocketFire2Img = engine.createImage("img.proj.rocket.fire.2", CENTER_4, null, null, strip4[12]);
		final Panframe projRocketFire1Frm = engine.createFrame("frm.proj.rocket.fire.1", projRocketFire1Img, 3);
		final Panframe projRocketFire2Frm = engine.createFrame("frm.proj.rocket.fire.2", projRocketFire2Img, 3);
		rocketFireAnm = engine.createAnimation("anm.proj.rocket.fire.1", projRocketFire1Frm, projRocketFire2Frm);
		final Panple velBullet = new FinPanple(3, 0, 0);
		final Emitter sawEmit = new Emitter(15, 1, FinPanple.ORIGIN, (byte) 1, projSawImg);
		final Emitter magEmit1 = new Emitter(7, 5, new FinPanple(3, -0.125f, 0), (byte) -1, projMagImg);
		final Emitter magEmit2 = new Emitter(16, 5, new FinPanple(3, 0.125f, 0), (byte) -1, projMagImg);
		final float sx = 11, sy = 5;
		final Emitter shotEmit = new Emitter(ShotProjectile.class, sx, sy, new FinPanple(3, 0, 0), (byte) -1, projShotImg);
		final Emitter miniEmit = new Emitter(MiniProjectile.class, 15, 3, new FinPanple(4, 0, 0), (byte) -1, projMiniImg);
		final Emitter flameEmit = new Emitter(FlameProjectile.class, 11, 5, velBullet, (byte) -1, projFlame1Anm);
		final Emitter rocketEmit = new Emitter(RocketProjectile.class, 13, 8, velBullet, (byte) -1, projRocketImg);
		weaponDefs = new WeaponDefinition[6];
		loadWeapon(0, "Chainsaw", 1, 1, strip, 0, null, null, smokeSmallAnm, 1, null, new Emitter[] {sawEmit}, 0, 20, 20, Weapon.INF, Weapon.INF, 5, 5, 1, 1, -1, -1, 1, 1);
		loadWeapon(1, "Magnums", 2, 1, strip, 2, flashSmallAnm, casingAnm, null, -1, new Emitter[] {magEmit1, magEmit2}, null, 8, 10, 100, Weapon.INF, Weapon.INF, 2, 5, 1, 1, -1, -1, 1, 1);
		loadWeapon(2, "Shotgun", 7, 1, strip, 3, flashSmallAnm, casingAnm, null, -1, new Emitter[] {shotEmit}, null, 12, 2, 20, 50, 200, 1, 1, 4, 8, -1, -1, 1, 1);
		loadWeapon(3, "Minigun", 2, 1, strip, 4, flashFastAnm, casingAnm, null, 5, null, new Emitter[] {miniEmit}, 1, 1, 10, 100, 400, 1, 1, 1, 1, -1, -1, 1, 1);
		loadWeapon(4, "Flamethrower", 5, 1, strip, 6, null, null, null, -1, null, new Emitter[] {flameEmit}, 0, 5, 15, 200, 1000, 1, 1, 1, 1, 16, 32, 1, 1);
		loadWeapon(5, "RocketLauncher", 6, 1, strip, 7, flashBigAnm, null, null, -1, new Emitter[] {rocketEmit}, null, 20, 50, 500, 1, 10, 1, 1, 1, 1, -1, -1, 3, 8);
	}
	
	private final static void loadWeapon(final int wpnIdx, final String name,
			final int x, final int y, final BufferedImage[] strip, final int imgIdx,
			final Panimation flash, final Panimation casing, final Panimation smoke, final int imgIdx2,
			final Emitter[] attackEmitters, final Emitter[] attackingEmitters,
			final int delay, final int minPower, final int maxPower,
			final int minCapacity, final int maxCapacity,
			final int minPierce, final int maxPierce,
			final int minSpray, final int maxSpray,
			final int minRange, final int maxRange,
			final int minBlast, final int maxBlast) {
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
		weaponDefs[wpnIdx] = new WeaponDefinition(name, img, flash, casing, smoke, attack, attackEmitters, attackingEmitters, delay,
		    minPower, maxPower, minCapacity, maxCapacity, minPierce, maxPierce, minSpray, maxSpray, minRange, maxRange, minBlast, maxBlast);
	}
	
	protected final static BufferedImage[] loadStrip(final String loc) {
		return loadStrip(loc, ImtilX.DIM);
	}
	
	protected final static BufferedImage[] loadStrip(final String loc, final int dim) {
		return ImtilX.loadStrip("org/pandcorps/shoot/res/" + loc + ".png", dim);
	}
	
	protected final static BufferedImage loadImage(final String loc, final int dim) {
		return ImtilX.loadImage("org/pandcorps/shoot/res/" + loc + ".png", dim, null);
	}
	
	protected final static Panmage createImage(final String loc, final int dim) {
		return Pangine.getEngine().createImage("img." + loc, loadImage(loc, dim));
	}
	
	protected final static BufferedImage[] loadChrStrip(final String name) {
		return loadStrip("chr/" + name);
	}
	
	private final static BufferedImage getTrpImg(final BufferedImage[] strip, final int i, final BufferedImage head, final int h) {
		final BufferedImage body = strip[i];
		Imtil.copy(head, body, 0, 0, ImtilX.DIM, ImtilX.DIM - h, 0, -h, true);
		return body;
	}
	
	protected final static ShooterDefinition getTrp(final int headIndex) {
		final BufferedImage[] strip = loadChrStrip("Blitztrooper");
		final BufferedImage head = strip[headIndex];
		final BufferedImage still = getTrpImg(strip, 0, head, 0);
		final BufferedImage left = getTrpImg(strip, 1, head, 1);
		final BufferedImage right = getTrpImg(strip, 2, head, 1);
		return ShooterDefinition.create("Blitztrooper." + headIndex, still, left, right);
	}
	
	private final static class TitleScreen extends FadeScreen {
		private Panmage will = null;
		
        private TitleScreen() {
            super(Pancolor.WHITE, 240);
        }
        
        @Override
        protected final void start() {
            final Pangine engine = Pangine.getEngine();
            engine.setBgColor(Pancolor.WHITE);
            final Panctor act = new Panctor("TitleAct");
            act.setView(title);
            act.getPosition().set(64, 60);
            room.addActor(act);
            final BufferedImage willImg = new PandScaler().scale(loadImage("misc/WillBig", 64));
            will = engine.createImage("img.title.will", willImg);
            final Panctor guy = new Panctor("GuyAct");
            guy.setView(will);
            guy.getPosition().set(112, 4, -1);
            room.addActor(guy);
        }
        
        @Override
        protected final void finish() {
        	Story.playIntro();
        }
        
        @Override
        protected final void destroy() {
            Panmage.destroy(will);
        }
    }
	
	protected final static class ShootScreen extends Panscreen {
		@Override
        protected final void load() throws Exception {
			final Pangine engine = Pangine.getEngine();
			room.destroy();
			room = engine.createRoom(Pantil.vmid(), new FinPanple(512, 192, 0));
			max = new FinPanple(512, 95, 0);
			Pangame.getGame().setCurrentRoom(room);
			loadBackground();
			engine.setBgColor(Pancolor.GREEN);
			final Shooter shooter = new Shooter("STR.1", room, playerDef);
			for (int i = 0; i < weaponDefs.length; i++) {
			    shooter.addWeapon(i);
			}
			shooter.weapon2();
			shooter.getPosition().set(64, 64);
			new Player(shooter).setShooter(shooter);
			engine.track(shooter);
			final Shooter enemy = new Shooter("ENM.1", room, trooperDefs[3]);
			enemy.getPosition().set(192, 64);
			enemy.setMirror(true);
			new Ai().setShooter(enemy);
			room.addActor(tm);
			final Shooter merchant = new Shooter("MER", room, merchantDef);
			merchant.getPosition().set(224, 88);
			merchant.setMirror(true);
			new Merchant().setShooter(merchant);
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
