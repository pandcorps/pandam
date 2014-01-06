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

import java.awt.image.BufferedImage;

import org.pandcorps.core.*;
import org.pandcorps.core.chr.CallSequence;
import org.pandcorps.core.img.*;
import org.pandcorps.core.img.scale.PandScaler;
import org.pandcorps.game.*;
import org.pandcorps.game.actor.Guy2.*;
import org.pandcorps.game.core.ImtilX;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.TimerEvent;
import org.pandcorps.pandam.event.TimerListener;
import org.pandcorps.pandam.impl.FinPanple;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Fonts.FontRequest;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.visual.FadeScreen;
import org.pandcorps.shoot.Projectile.*;
import org.pandcorps.shoot.Shooter.ShooterDefinition;
import org.pandcorps.shoot.Weapon.WeaponDefinition;

public class ShootGame extends Guy2Game {
    /*
    Grey out menu options.
    Save/load (weapon args, ammo, money, constitution, health, experience).
    Use all available tiles.
    */
    private final static String PROP_DEBUG = "org.pandcorps.shoot.ShootGame.debug";
    private final static boolean debug = Boolean.getBoolean(PROP_DEBUG);
	/*package*/ final static char CHAR_HEALTH = 2;
	/*package*/ final static char CHAR_ARROW = 16;
	/*package*/ final static char CHAR_AMMO = 132;
	/*package*/ final static char CHAR_MONEY = 225;
	/*package*/ static Guy2Type type = null;
	private static ShooterDefinition playerDef = null;
	/*package*/ static ShooterDefinition merchantDef = null;
	/*package*/ static ShooterDefinition[] trooperDefs = null;
	/*package*/ static ShooterDefinition[] bossDefs = null;
	/*package*/ static WeaponDefinition[] weaponDefs = null;
	/*package*/ static WeaponDefinition chainsaw = null;
	private static Panmage title = null;
	/*package*/ static Panimation blood = null;
	/*package*/ static Panimation explosion = null;
	/*package*/ static Panimation puff = null;
	/*package*/ static Panimation bam = null;
	/*package*/ static Panmage[] rain = null;
	/*package*/ static Panimation splash = null;
	/*package*/ static Panmage interact = null;
	/*package*/ static Font font = null;
	/*package*/ static Font hudFont = null;
	/*package*/ static Panroom room = null;
	/*package*/ static Panlayer hud = null;
	/*package*/ static Shooter shooter = null;
	/*package*/ final static FinPanple min = FinPanple.ORIGIN;
	/*package*/ static FinPanple max = null;
	/*package*/ static Panimation smokeBigAnm = null;
	/*package*/ static Panimation flameLoopAnm = null;
	/*package*/ static Panimation rocketFireAnm = null;
	/*package*/ static Panmage money = null;
	/*package*/ static Panmage ammoShotgun = null;
	/*package*/ static Panmage ammoMinigun = null;
	/*package*/ static Panmage ammoFlamethrower = null;
	/*package*/ static Panmage ammoRocketLauncher = null;
	/*package*/ static Panmage health = null;
	/*package*/ static TileMap tm = null;
	/*package*/ static Pantext hudArrow = null; 

	@Override
	protected void init(final Panroom room) throws Exception {
	    Pangine.getEngine().setIcon("org/pandcorps/shoot/res/misc/WillKillemIcon32.png", "org/pandcorps/shoot/res/misc/WillKillemIcon16.png");
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
        bam = createBamAnm(constantImgs, 2);
        Ai.bamDelay = bam.getDuration() + 2;
        rain = createSheet("rain", "org/pandcorps/game/res/misc/Rain.png");
        splash = createAnm("rain4", "org/pandcorps/game/res/misc/Rain4.png", 4, 3);
        interact = engine.createEmptyImage("img.interact", new FinPanple(1, 1, 1), new FinPanple(0, 0, 0), new FinPanple(2, 2, 2));
        font = Fonts.getSimple(new FontRequest(8), Pancolor.BLUE, Pancolor.CYAN, Pancolor.CYAN, Pancolor.BLACK);
        hudFont = Fonts.getOutline(new FontRequest(8), Pancolor.BLUE, Pancolor.BLUE, Pancolor.BLUE, new FinPancolor(Pancolor.MIN_VALUE, Pancolor.MIN_VALUE, (short) 128, Pancolor.MAX_VALUE));
        final BufferedImage[] powerUps = loadStrip("misc/PowerUps", 16);
        final FinPanple opu = new FinPanple(8, 5, 0);
        final FinPanple npu = new FinPanple(-4, 1, 0);
        final FinPanple xpu = new FinPanple(4, 9, 0);
        money = engine.createImage("img.money", opu, npu, xpu, powerUps[0]);
        ammoShotgun = engine.createImage("img.ammo.shotgun", opu, npu, xpu, powerUps[1]);
        ammoMinigun = engine.createImage("img.ammo.minigun", opu, npu, xpu, powerUps[2]);
        ammoFlamethrower = engine.createImage("img.ammo.flamethrower", opu, npu, xpu, powerUps[3]);
        ammoRocketLauncher = engine.createImage("img.ammo.rocketlauncher", opu, npu, xpu, powerUps[4]);
        health = engine.createImage("img.health", opu, npu, xpu, powerUps[5]);
        loadWeapons();
        loadCharacters();
	}
	
	private final static void loadCharacters() {
		playerDef = ShooterDefinition.create("Will", debug ? 10000 : 200, loadChrStrip("Will"));
		trooperDefs = new ShooterDefinition[8];
		trooperDefs[0] = getTrp(7, 50, 1, null, false);
		trooperDefs[1] = getTrp(5, 60, 2, null, false);
		trooperDefs[2] = getTrp(6, 70, 4, null, false);
		trooperDefs[3] = getTrp(8, 80, 7, null, false);
		trooperDefs[4] = getTrp(4, 90, 11, null, false);
		trooperDefs[5] = getTrp(6, 100, 3, weaponDefs[2], false);
		trooperDefs[6] = getTrp(8, 110, 3, weaponDefs[4], false);
		trooperDefs[7] = getTrp(4, 200, 3, weaponDefs[1], true);
		bossDefs = new ShooterDefinition[3];
		bossDefs[0] = getBoss("Bladander", 400, 10, weaponDefs[3]);
		bossDefs[1] = getBoss("Bladigar", 800, 15, weaponDefs[4]);
		bossDefs[2] = getBoss("Bladimir", 1600, 20, weaponDefs[1]);
		merchantDef = ShooterDefinition.create("Merchant", Weapon.INF, loadChrStrip("Merchant"));
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
		chainsaw = loadWeapon(0, "Chainsaw", 1, 1, strip, 0, null, null, smokeSmallAnm, 1, null, null, new Emitter[] {sawEmit}, 20, 20, Weapon.INF, Weapon.INF, 0, 0, 5, 5, 1, 1, -1, -1, 1, 1);
		loadWeapon(1, "Magnums", 2, 1, strip, 2, flashSmallAnm, casingAnm, null, -1, null, new Emitter[] {magEmit1, magEmit2}, null, 10, 100, Weapon.INF, Weapon.INF, 5, 5, 2, 5, 1, 1, -1, -1, 1, 1);
		/*
		WeaponDefinition converts delay (which upgrades should decrease) to rate (which upgrades should increase).
		So menu's rate shouldn't match these numbers and should increase with each purchase.
		*/
		loadWeapon(2, "Shotgun", 7, 1, strip, 3, flashSmallAnm, casingAnm, null, -1, ammoShotgun, new Emitter[] {shotEmit}, null, 20, 20, 100, 100, 14, 6, 1, 1, 4, 8, -1, -1, 1, 1);
		loadWeapon(3, "Minigun", 2, 1, strip, 4, flashFastAnm, casingAnm, null, 5, ammoMinigun, null, new Emitter[] {miniEmit}, 8, 20, 100, 400, 1, 1, 1, 1, 1, 1, -1, -1, 1, 1);
		loadWeapon(4, "Flamethrower", 5, 1, strip, 6, null, null, null, -1, ammoFlamethrower, null, new Emitter[] {flameEmit}, 5, 15, 1000, 1000, 0, 0, 1, 1, 1, 1, 16, 32, 1, 1);
		loadWeapon(5, "Rocket Launcher", 6, 1, strip, 7, flashBigAnm, null, null, -1, ammoRocketLauncher, new Emitter[] {rocketEmit}, null, 250, 250, 1, 10, 20, 20, 1, 1, 1, 1, -1, -1, 3, 8);
	}
	
	private final static WeaponDefinition loadWeapon(final int wpnIdx, final String name,
			final int x, final int y, final BufferedImage[] strip, final int imgIdx,
			final Panimation flash, final Panimation casing, final Panimation smoke, final int imgIdx2,
			final Panmage ammo, final Emitter[] attackEmitters, final Emitter[] attackingEmitters,
			final int minPower, final int maxPower,
			final int minCapacity, final int maxCapacity,
			final int maxDelay, final int minDelay,
			final int minPierce, final int maxPierce,
			final int minSpray, final int maxSpray,
			final int minRange, final int maxRange,
			final int minBlast, final int maxBlast) {
		final Pangine engine = Pangine.getEngine();
		final String code = Chartil.remove(name, ' ');
		final String imgName = "img.wpn." + code;
		final FinPanple o = new FinPanple(x, y, 0);
		final Panmage img = engine.createImage(imgName, o, null, null, strip[imgIdx]);
		final Panimation attack;
		if (imgIdx2 < 0) {
			attack = null;
		} else  {
			final Panmage img2 = engine.createImage(imgName + ".2", o, null, null, strip[imgIdx2]);
			final String frmName = "frm.wpn." + code;
			final Panframe frm1 = engine.createFrame(frmName, img, 3);
			final Panframe frm2 = engine.createFrame(frmName + ".2", img2, 3);
			attack = engine.createAnimation("anm.wpn." + code, frm1, frm2);
		}
		final WeaponDefinition d = new WeaponDefinition(name, img, flash, casing, smoke, attack, ammo, attackEmitters, attackingEmitters,
		    minPower, maxPower, minCapacity, maxCapacity, maxDelay, minDelay, minPierce, maxPierce, minSpray, maxSpray, minRange, maxRange, minBlast, maxBlast);
		weaponDefs[wpnIdx] = d;
		return d;
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
	
	private final static BufferedImage getTrpImg(final BufferedImage[] strip, final int i, final BufferedImage head, final int h, final boolean cape) {
		final BufferedImage body = strip[i];
		if (cape) {
			Imtil.copy(strip[3], body, 0, h, ImtilX.DIM, ImtilX.DIM - h, 0, 0, Imtil.COPY_BACKGROUND);
		}
		
		Imtil.copy(head, body, 0, h, ImtilX.DIM, ImtilX.DIM - h, 0, 0, Imtil.COPY_FOREGROUND);
		
		return body;
	}
	
	protected final static ShooterDefinition getTrp(final int headIndex, final int constitution, final int melee, final WeaponDefinition weapon, final boolean cape) {
		final BufferedImage[] strip = loadChrStrip("Blitztrooper");
		final BufferedImage head = strip[headIndex];
		final BufferedImage still = getTrpImg(strip, 0, head, 0, cape);
		final BufferedImage left = getTrpImg(strip, 1, head, 1, cape);
		final BufferedImage right = getTrpImg(strip, 2, head, 1, cape);
		return ShooterDefinition.create("Blitztrooper." + headIndex + "." + (weapon == null ? "Unarmed" : weapon.name), constitution, melee, weapon, still, left, right);
	}
	
	protected final static ShooterDefinition getBoss(final String name, final int constitution, final int melee, final WeaponDefinition weapon) {
		return ShooterDefinition.create(name, constitution, melee, weapon, loadChrStrip(name));
	}
	
	/*package*/ final static class TitleScreen extends FadeScreen {
		private Panmage will = null;
		
		/*package*/ TitleScreen() {
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
	    private final Level level;
	    
	    protected ShootScreen(final Level level) {
	        this.level = level;
	    }
	    
		@Override
        protected final void load() throws Exception {
			final Pangine engine = Pangine.getEngine();
			room.destroy();
			final int w = 640;
			room = engine.createRoom(Pantil.vmid(), new FinPanple(w, 192, 0));
			max = new FinPanple(w, 93, 0);
			Pangame.getGame().setCurrentRoom(room);
			createHud();
			level.start();
			tm.getPosition().setZ(type.getDepthShadow() - 1);
			room.addActor(tm);
			engine.setBgColor(Pancolor.GREEN);
			createPlayer();
			clearDelay();
		}
		
		private static void createPlayer() {
		    if (shooter == null) {
    			shooter = new Shooter("STR.1", room, playerDef);
    			for (int i = 0; i < weaponDefs.length; i++) {
    			    shooter.addWeapon(i);
    			}
    			shooter.weapon2();
    			if (debug) {
    			    shooter.addMoney(Shooter.MAX_MONEY);
    			}
    			new Player(shooter).setShooter(shooter);
		    } else {
		        shooter.attach(room);
		    }
		    shooter.getPosition().set(64, 64);
			Pangine.getEngine().track(shooter);
		}
		
		private static void createHud() {
			final float h = Pangine.getEngine().getGameHeight();
			hud = BaseGame.createHud(room);
			final Pantext hudHealth, hudAmmo, hudMoney;
			hudHealth = new Pantext("hud.health", hudFont, new CallSequence() {@Override protected String call() {
				return getHud(CHAR_HEALTH, shooter.getHealth());}});
			hudHealth.getPosition().set(4, h - 12);
			hud.addActor(hudHealth);
			hudAmmo = new Pantext("hud.ammo", hudFont, new CallSequence() {@Override protected String call() {
				return getHud(CHAR_AMMO, shooter.weapon.getAmmo());}});
			hudAmmo.getPosition().set(100, h - 12);
			hud.addActor(hudAmmo);
			hudMoney = new Pantext("hud.money", hudFont, new CallSequence() {@Override protected String call() {
				return getHud(CHAR_MONEY, shooter.getMoney());}});
			hudMoney.getPosition().set(188, h - 12);
			hud.addActor(hudMoney);
			hudArrow = new Pantext("hud.arrow", hudFont, Character.toString(CHAR_ARROW));
			hudArrow.getPosition().set(244, h - 24);
			hudArrow.setVisible(false);
			hud.addActor(hudArrow);
		}
		
		private static String getHud(final char label, final int val) {
			return Character.toString(label) + ' ' + (val == Weapon.INF ? Character.toString((char) 236) : Integer.toString(val));
		}
		
		@Override
	    public final void step() {
		    final boolean cleared = isCleared();
	        hudArrow.setVisible(cleared && level.next != null && Pangine.getEngine().isOn(15));
	        if (cleared) {
	        	if (level.next == null) {
	        		end("YOU WIN");
	        	} else if (!shooter.isMirror() && shooter.getPosition().getX() >= (max.getX() - shooter.getSpeed())) {
	        		shooter.detach();
	        		Panscreen.set(new ShootScreen(level.next));
	        	}
	        }
	    }
	}
	
	private static long clearTime = 0;
	
	private final static void clearDelay() {
		clearTime = Pangine.getEngine().getClock() + 5;
	}
	
	private static boolean isCleared() {
	    for (final Panctor actor : room.getActors()) {
	        final Class<?> c = actor.getClass();
	        if (c == Spawner.class) {
	        	clearDelay();
	            return false;
	        } else if (c == Shooter.class && ((Shooter) actor).getController() instanceof Ai) {
	        	clearDelay();
	            return false;
	        }
	    }
	    return Pangine.getEngine().getClock() > clearTime;
	}
	
	/*package*/ static void end(final String msg) {
		final Pantext text = new Pantext("end", hudFont, msg);
        text.getPosition().set(SCREEN_W / 2, hudArrow.getPosition().getY() - 16);
        text.centerX();
        hud.addActor(text);
        Pangine.getEngine().addTimer(tm, 60, new TimerListener() { @Override public void onTimer(final TimerEvent event) { Panscreen.set(new ShootGame.TitleScreen()); }});
	}
	
	public final static void main(final String[] args) {
        try {
            new ShootGame().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
