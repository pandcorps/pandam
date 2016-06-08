/*
Copyright (c) 2009-2016, Andrew M. Martin
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
package org.pandcorps.furguardians;

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.chr.*;
import org.pandcorps.core.img.*;
import org.pandcorps.core.img.Pancolor.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.game.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.game.actor.CustomBurst.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Fonts.*;
import org.pandcorps.pandax.text.Notifications.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.touch.*;
import org.pandcorps.pandax.visual.*;
import org.pandcorps.furguardians.Avatar.*;
import org.pandcorps.furguardians.Enemy.*;
import org.pandcorps.furguardians.Menu.*;
import org.pandcorps.furguardians.Player.*;

public class FurGuardiansGame extends BaseGame {
	protected final static String TITLE = "Fur-Guardians"; // res/values/strings.xml/app_name
    protected final static String VERSION = "1.24.0"; // AndroidManifest.xml/versionName
    protected final static String YEAR = "2014-2016";
    protected final static String AUTHOR = "Andrew M. Martin";
	/*
	Hippo/squirrel/gator/pig/walrus/beaver/stag/bull/ram player face.
	Ghost trail, particle trail.
	Spin-float when hold jump while falling, 2xjump, inf flip-jump, kite.
	Player sliding image.
	Warp Map marker for entry/exit point.
	Replace bush with Rise.png for some levels; rise will be higher than 1 tile; separate build method.
	Taller bushes.
	Map landmarks: Mountain, garden.
	Train-riding levels.
	Ridable dragons - classic menu eyes/name.
	Enemy Elementals, winged Imp, Banshee, Wraith, Shade, Orc.
	Nether Wisp in Chaos levels with curse/poison effect.
	Drolock should walk sometimes.
	Enemy-specific Level templates (Imp walking into ArmorBall).
	Gargoyles catch/carry Player, like moving platforms, one can jump to/from them, but not run on them.
	Cannons on ground that Player enters to be launched.
	Cannons in air that auto-fire, others that wait for jump input.
	Item/costume/vehicle/power-up found in Level that Player or certain monster(s) can use
	Maximum effective (per tick) velocity & higher absolute max; set current to max effective before evaluating.
	Options (in/out game) - track active/p1
	Font w/ custom chars for custom string/min square/case space if needed.
	Colored player names.
	Bump w/o break ceiling block.
	Bounce/blow floor.
	Spike/fire floor tile.
	Jumping/walking enemy.
	Collect fruit from trees.
	Level to-do notes.
	Let LogoScreen finish after a specified Runnable finishes.
	Let Thread keep loading through title screen.
	Give images a real transparent background, disable ImtilX preprocessing.
	Assist: Berserker? (Defeat Enemy by touching it)
	Assist: Teleport (After fall, teleport to target immediately instead of slow bubble, no gem loss)
	Clear TileMap.map/imgs?
	Static TileMaps should combine adjacent Tiles with adjacent TileMapImages into one larger Tile/TileMapImage.
	Hoard/Vault Menu to show amount of each type of Gem.
	Level builder should use setTile instead of forcing getTile Map lookups.
	Console: sub/subtract/remove, set gems.
	Console: Filters, 8-bit, b/w (requires restart).
	Mouse TouchEvents.
	A BounceBall should be able to bump blocks (from below and side) and give Gem to Player that kicked.
	User saw Enemy defeated by bumped block fail to give Player a Gem.
	Once saw Player appear on wrong Marker after goal-met screen.
	Move music generation, preprocessing classes into a new folder to be excluded from jar.
	GoalTemplate: 2*2 block steps.
	Sand level cactus, Snow level crystal/pine tree
	Chant Music - rise * 3 at end?
	OutputStream that writes to tmp file then swaps on close, keeping original as bak.
	InputStream that reads bak file if can't find main.
	Breaking block with Imp at edge above it can miss the Imp.
	Menu.menuTouch - call allow method before processing a tab change or exit?
	MaskMap cache
	Can get stuck under bridges in auto-run mode
	DoubleOrb looks like an invincibility effect; maybe slow down spark frequency would look less like that
	Creating a new profile should maybe check for a delete map file matching the new name
	Level end doesn't clear blobs
	
	Remove System.out/err/printStackTrace/etc.
	Screen shots.
	Version #
	*/
	
	protected final static byte TILE_BREAK = 2;
	protected final static byte TILE_BUMP = 3;
	protected final static byte TILE_FLOOR = 4;
    protected final static byte TILE_UPSLOPE = 5;
    protected final static byte TILE_DOWNSLOPE = 6;
    protected final static byte TILE_UPSLOPE_FLOOR = 7;
    protected final static byte TILE_DOWNSLOPE_FLOOR = 8;
    protected final static byte TILE_GEM = 9;
    protected final static byte TILE_ICE = 10;
    protected final static byte TILE_SAND = 11;
    protected final static byte TILE_HURT = 12;
	
	protected final static int DEPTH_BACK = 0;
	protected final static int DEPTH_ENEMY_BACK = 6;
	protected final static int DEPTH_ENEMY = 7;
	protected final static int DEPTH_ENEMY_FRONT = 8;
	protected final static int _DEPTH_PLAYER_BACK = 1;
	protected final static int _DEPTH_PLAYER = 2;
	protected final static int _DEPTH_PLAYER_FRONT = 3;
	protected final static int _DEPTH_CONTAINER = 4;
	protected final static int _DEPTH_BUBBLE = 5;
	protected final static int DEPTH_SHATTER = 14;
	protected final static int DEPTH_SPARK = 15;
	protected final static int DEPTH_TEXT = 16;
	protected final static int DEPTH_OFF_DRAGON = 8;
	// DEPTH_PLAYER_DRAGON_BACK - BUBBLE = 9 - 13
	
	protected final static int OFF_GEM = 16;
	
	protected final static int TIME_FLASH = 60;
	
	protected final static int ORIG_X_SPIKE = 7;
	
	private final static FinPanple2 ORIG_MAP = new FinPanple2(8, -6);
	private final static FinPanple2 ORIG_MAP_DRAGON_EAST_WEST = new FinPanple2(15, -6);
	private final static int DUR_MAP = 6;
	protected final static int DUR_BLINK = 120;
	protected final static int DUR_CLOSED = DUR_BLINK / 30;
	
	protected final static short SPEED_FADE = 6;
	
	protected final static int MAX_NAME_PROFILE = 8;
	protected final static int MAX_NAME_AVATAR = 8;
	
	protected final static String RES = "org/pandcorps/furguardians/";
	
	protected final static String FILE_CFG = "Config.txt";
	protected final static String EXT_PRF = ".prf.txt";
	protected final static String EXT_MAP = ".map.txt";
	
	protected final static String SEG_CFG = "CFG";
	protected final static String SEG_PRF = "PRF";
	protected final static String SEG_STX = "STX";
	protected final static String SEG_ACH = "ACH";
	protected final static String SEG_LOC = "LOC";
	protected final static String SEG_AVT = "AVT";
	
	protected final static String KIND_BEE = "Bee";
	
	private final static List<String> animals =
			Arrays.asList("Bear", "Cat", "Dog", "Elephant", "Horse", "Koala", "Mouse", "Panda", "Rabbit", "Rhino", "Wolf", "Zebra");
	private static List<String> basicAnimals = null;
	
	protected static List<String> tips = null;
	
	private final static List<String> initTips() {
		final List<String> list = new ArrayList<String>();
		if (Pangine.getEngine().isMouseSupported()) {
			list.add("You can enter the menu by pressing Esc on the map");
			list.add("You can view your current Goals by pressing G on the map");
		}
		list.add("You can turn music and sounds on/off in Menu/Setup/Music");
		list.add("You can change your Avatar's appearance in Menu/Edit");
		list.add("You can spend Gems on clothing and powerups in Menu/Edit/Gear");
		list.add("You can try powerups for 1 Level without spending any Gems");
		list.add("You can change the color of clothing that you have purchased");
		list.add("You can switch between full control and auto-run in Menu/Setup"); // on-screen buttons
		list.add("You can add a Profile for a new user of this device in Menu/Add");
		list.add("Spending Gems in one Profile will not take Gems from other Profiles");
		list.add("Items purchased in one Profile are unavailable to other Profiles");
		list.add("You can create a new Avatar for yourself in Menu/Add");
		list.add("A single Profile can have multiple Avatars");
		list.add("Items purchased for one Avatar can be used by other Avatars of that Profile");
		list.add("You can spend Gems on new abilities in Menu/Setup/Perks");
		list.add("You can adjust the game's difficulty in Menu/Setup/Easy-Hard");
		return list;
	}
	
	protected final static String[][] ads = {
		{ "Thank you", "for playing" },
		{ "Thank you", "for downloading" },
		{ "#gamedev", "#indiedev" },
		{ "Open", "source" },
		{ "You", "rock!" },
		{ "You're", "a superstar!" },
		{ "You're", "awesome!" },
	    { "Like the game?", "Tell a friend" },
	    { "Like the game?", "Rate it" },
	    { "Like the game?", "Write a review" },
	    { "Like the game?", "+1 it" },
	    { "Pandcorps has no", "marketing team" },
	    { "The Pandcorps", "ad budget is 0" },
	    { "You can follow", "@pandcorps" },
	    { "You can contact", getEmail() },
	    { "You can visit", "http://pandcorps.org" },
	    { "Find a bug?", "Tell Pandcorps" },
	    { "Have a suggesstion?", "Tell Pandcorps" },
	    { "Trying to be", "fun for all" }};
	
	private final static PixelMask greyMask = new GreyScalePixelMask();
	
	protected static boolean debugMode = false;
	protected static boolean level = false;
	protected static Panroom room = null;
	protected static Panlayer hud = null;
	protected static Gem hudGem = null;
	protected final static ArrayList<PlayerContext> pcs = new ArrayList<PlayerContext>();
	protected static MultiFont font = null;
	protected static Font fontTiny = null;
	protected static Notifications notifications = null;
	private final static FinPanple2 ng = Character.getMin(Player.PLAYER_X);
	private final static FinPanple2 xg = Character.getMax(Player.PLAYER_X, Player.PLAYER_H);
	protected final static FinPanple2 og = new FinPanple2(17, 1);
	protected final static FinPanple2 ow = new FinPanple2(18, 1);
	protected final static FinPanple2 owf = new FinPanple2(18, 2);
	protected final static FinPanple2 os = new FinPanple2(17, 11);
	protected final static FinPanple2 od = new FinPanple2(13, 1);
	protected final static FinPanple odf = new FinPanple(13, 1, _DEPTH_PLAYER_BACK - _DEPTH_PLAYER_FRONT);
	protected final static FinPanple2 or = new FinPanple2(21, -6);
	protected final static int BIRD_X = 7;
	protected final static int BIRD_Y = 1;
	protected final static FinPanple2 oBird = new FinPanple2(BIRD_X, BIRD_Y);
	protected static Img[] guysBlank = null;
	protected static Img[] guysRide = null;
	protected final static HashMap<String, Img> facesAll = new HashMap<String, Img>();
	protected final static HashMap<String, Img[]> faceMapsAll = new HashMap<String, Img[]>();
	protected final static HashMap<String, Img[]> tailsAll = new HashMap<String, Img[]>();
	protected final static HashMap<String, Img[]> bodiesAll = new HashMap<String, Img[]>();
	protected final static HashMap<String, Img[]> birdsAll = new HashMap<String, Img[]>();
	protected static Img[] bees = null;
	protected final static Img[] eyesAll = new Img[getNumEyes()];
	protected final static HashMap<String, Img> masksAll = new HashMap<String, Img>();
	protected static Img eyesBlink = null;
	protected final static Img[] dragonEyesAll = new Img[getNumDragonEyes()];
	protected static Panmage egg = null;
	protected static Panmage egg8 = null;
	protected static Panmage[] crownPanmages = null;
	protected static Panimation bee = null;
	protected static Panmage frozen = null;
	protected static Panimation burn = null;
	protected static Panimation electric = null;
	protected static Panimation doubleFx = null;
	protected static Panmage lightningOrb = null;
	protected static Panmage doubleOrb = null;
	protected static Panmage blockPower = null;
	protected static Panmage bubble = null;
	protected static Panimation minecart = null;
	protected static Panimation owl = null;
	protected final static List<EnemyDefinition> allEnemies = new ArrayList<EnemyDefinition>();
	protected static List<EnemyDefinition> enemies = null;
	protected static EnemyDefinition imp = null;
	protected static EnemyDefinition armoredImp = null;
	protected static EnemyDefinition spikedImp = null;
	protected static EnemyDefinition bounceBall = null;
	protected static EnemyDefinition trollColossus = null;
	protected static EnemyDefinition ogreBehemoth = null;
	protected static EnemyDefinition iceWisp = null;
	protected static EnemyDefinition fireWisp = null;
	protected static EnemyDefinition rockSprite = null;
	protected static EnemyDefinition rockTrio = null;
	protected static EnemyDefinition rockLeg = null;
	protected static Panmage rockBack = null;
	protected static Panimation anger = null;
	protected static Panmage block8 = null;
	protected static Panmage blockLetter8 = null;
	protected static Panmage blockIce8 = null;
	protected static Panmage vineShatter = null;
	protected static Panmage shakingSpike = null;
	protected static Panmage fallingSpike = null;
	protected static Panmage[] gem = null;
	protected static Panmage[] gemBlue = null;
	protected static Panimation gemAnm = null;
	protected static Panimation gemBlueAnm = null;
	protected static Panimation gemCyanAnm = null;
	protected static Panimation gemGreenAnm = null;
	protected static Panmage gemWhite = null;
	protected static Panimation gemLevelAnm = null;
	protected static Panimation gemWorldAnm = null;
	protected static Panimation gemBlueWorldAnm = null;
	protected static Panimation gemCyanWorldAnm = null;
	protected static Panimation gemGreenWorldAnm = null;
	protected static Panimation gemWhiteWorldAnm = null;
	protected static Panimation gemWordAnm = null;
	protected final static String defaultBlockWord = "FUR";
	protected static String blockWord = defaultBlockWord;
	protected static Panmage[] gemLetters = null;
	protected static Panmage[] blockLetters = null;
	protected static Panmage[] translucentBlockLetters = null;
	protected static Panmage[] greenBlockLetters = null;
	protected static Panmage[] whiteBlockLetters = null;
	protected static Panmage[] gemGoal = null;
	protected static Panmage emptyGoal = null;
	protected static Panmage[] gemRank = null;
	protected static Panmage[] gemBlueRank = null;
	protected static Panmage[] gemCyanRank = null;
	protected static Panmage[] gemGreenRank = null;
	protected static Panmage[] gemWhiteRank = null;
	protected static Panmage[] gemAchieve = null;
	protected static Panmage gemShatter = null;
	protected static Panimation spark = null;
	protected static Panimation puff = null;
	protected static Panimation teleport = null;
	protected static Panimation projectile1 = null;
	protected static Panimation marker = null;
	protected static Panmage markerDefeated = null;
	protected static Panimation portal = null;
	protected static Panimation portalClosed = null;
	protected static Img[] dirts = null;
	protected static Img[] terrains = null;
	protected static Img[] crowns = null;
	protected static int DIM_BUTTON = 0;
	protected final static int MENU_W = 48, MENU_H = 40;
	protected static Panmage button = null;
	protected static Panmage buttonIn = null;
	protected static Panmage right2 = null;
	protected static Panmage right2In = null;
	protected static Panmage left2 = null;
	protected static Panmage left2In = null;
	protected static Panmage diamond = null;
    protected static Panmage diamondIn = null;
	protected static Panmage menu = null;
	protected static Panmage menuIn = null;
	protected static Panmage menuDisabled = null;
	protected static Panmage menuLeft = null;
	protected static Panmage menuRight = null;
	protected static Panmage menuUp = null;
	protected static Panmage menuDown = null;
	protected static Panmage menuCheck = null;
	protected static Panmage menuX = null;
	protected static Panmage menuPlus = null;
	protected static Panmage menuMinus = null;
	protected static Panmage menuExclaim = null;
	protected static Panmage menuQuestion = null;
	protected static Panmage menuOff = null;
	protected static Panmage menuTrophy = null;
	protected static Panmage menuStar = null;
	protected static Panmage menuGraph = null;
	protected static Panmage menuInfo = null;
	protected static Panmage menuFoes = null;
	protected static Panmage menuPause = null;
	protected static Panmage menuOptions = null;
	protected static Panmage menuMenu = null;
	protected static Panmage menuMusic = null;
	protected static Panmage menuSound = null;
	protected static Panmage menuDifficulty = null;
	protected static Panmage menuDefeat = null;
	protected static Panmage menuWorld = null;
	protected static Panmage menuAvatar = null;
	protected static Panmage menuColor = null;
	protected static Panmage menuAnimal = null;
	protected static Panmage menuEyes = null;
	protected static Panmage menuGear = null;
	protected static Panmage menuClothing = null;
	protected static Panmage menuHat = null;
	protected static Panmage menuJump = null;
	protected static Panmage menuBird = null;
	protected static Panmage menuKeyboard = null;
	protected static Panmage menuRgb = null;
	protected static Panmage menuEyesDragon = null;
	protected static Panmage menuButtons = null;
	protected static Panmage menuGames = null;
	protected static Panmage menuOptions64 = null;
	protected static Panmage menuCursor = null;
	protected static Panmage redUp = null;
	protected static Panmage redDown = null;
	protected static Panmage greenUp = null;
	protected static Panmage greenDown = null;
	protected static Panmage key = null;
	protected static Panmage keyIn = null;
	protected static Pansound musicMenu = null;
	protected static Pansound musicHappy = null;
	protected static Pansound musicSnow = null;
	protected static Pansound musicSand = null;
	protected static Pansound musicRock = null;
	protected static Pansound musicHive = null;
	protected static Pansound musicJungle = null;
	protected static Pansound musicBridge = null;
	protected static Pansound musicCave = null;
	protected static Pansound musicNight = null;
	protected static Pansound musicHeartbeat = null;
	protected static Pansound musicOcarina = null;
	protected static Pansound musicMinecart = null;
	protected static Pansound musicChant = null;
	protected static Pansound musicLevelStart = null;
	protected static Pansound musicLevelEnd = null;
	protected static Pansound soundGem = null;
	protected static Pansound soundJump = null;
	protected static Pansound soundBounce = null;
	protected static Pansound soundThud = null;
	protected static Pansound soundCrumble = null;
	protected static Pansound soundArmor = null;
	protected static Pansound soundWhoosh = null;
	protected static Queue<Runnable> loaders = new LinkedList<Runnable>();
	protected static Runnable btnLoader = null;
	
	@Override
	protected final boolean isFullScreen() {
        return true;
    }

	/*
	// Simulate a tablet
	@Override
	protected final int getDebugWindowWidth() {
		return 1024;
	}
	
	@Override
	protected final int getDebugWindowHeight() {
		//return 600;
		return 575; // This tablet has a menu bar that takes up part of the height
	}
	*/
	
	@Override
    protected final void initEarliest() {
	    loadConfig();
	}
	
	@Override
	protected final void init(final Panroom room) throws Exception {
	    final Pangine engine = Pangine.getEngine();
	    if (engine.isTouchSupported()) {
	        engine.setFatalLogged(true);
	    }
	    debugMode = Boolean.getBoolean("org.pandcorps.furguardians.debugMode");
	    tips = initTips();
	    engine.setTitle(TITLE);
	    engine.setEntityMapEnabled(false);
	    Imtil.onlyResources = true;
		FurGuardiansGame.room = room;
		loadConstants();
		Panscreen.set(new LogoScreen(TitleScreen.class, loaders));
	}
	
	protected final static void fadeIn(final Panlayer layer) {
	    fadeIn(layer, SPEED_FADE);
	}
	
	protected final static void fadeIn(final Panlayer layer, final short speed) {
		FadeController.fadeIn(layer, Pancolor.MIN_VALUE, Pancolor.MIN_VALUE, Pancolor.MIN_VALUE, speed);
	}
	
	protected final static void fadeOut(final Panlayer layer, final Panscreen screen) {
	    fadeOut(layer, SPEED_FADE, screen);
	}
	
	protected final static void fadeOut(final Panlayer layer, final short speed, final Panscreen screen) {
	    Player.clearPower();
		if (screen instanceof Map.MapScreen && goGoalsIfNeeded()) {
			return;
		}
		Notifications.fadeOut(notifications, layer, Pancolor.MIN_VALUE, Pancolor.MIN_VALUE, Pancolor.MIN_VALUE, speed, screen, true);
	}
	
	protected final static void setScreen(final Panscreen screen) {
	    Player.clearPower();
		Panctor.detach(notifications);
		Panscreen.set(screen);
	}
	
	protected final static void notify(final Named n, final String msg) {
	    notify(n, msg, null);
	}
	
	protected final static void notify(final Named n, final String msg, final Panctor a) {
        if (a != null) {
            int x = 16;
            final PlayerContext pc = Coltil.get(pcs, 0);
            if (pc != null) {
            	final Player p = pc.player;
            	final int gems = p == null ? pc.getGems() : p.getCurrentLevelGems();
            	x += ((Math.max(String.valueOf(gems).length(), pc.getName().length()) * 8) + 1);
            }
            a.getPosition().set(x, Pangine.getEngine().getEffectiveTop() - 17);
        }
		final String name = n.getName(), s;
		final int size = Coltil.size(pcs);
		if (size > 1 || (size == 1 && !name.equals(pcs.get(0).getName()))) {
			s = name + ": " + msg;
		} else {
			s = msg;
		}
		notifications.enqueue(new Notification(s, a));
	}
	
	protected final static class PlatformScreen extends Panscreen {
		private boolean waiting = true;
		
		@Override
        protected final void load() throws Exception {
			level = true;
			Player.clearPower();
			loadLevel();
			fadeIn(room);
			for (final PlayerContext pc : pcs) {
				for (final Goal g : pc.profile.currentGoals) {
					String msg = g.getName();
					final String progress = g.getProgress(pc);
					if (Chartil.isValued(progress)) {
						msg = msg + ", " + progress;
					}
					FurGuardiansGame.notify(pc, msg);
				}
			}
			Level.theme.getMusic().changeMusic();
		}
		
		@Override
        protected final void step() {
			final long clock = Pangine.getEngine().getClock();
			final long i = clock % TIME_FLASH;
			Level.theme.step(clock);
            if (i < 4) {
            	if (waiting) {
            		if (i == 0) {
            			waiting = false;
            		} else {
            			return;
            		}
            	}
                Tile.animate(Level.flashBlock);
                Level.theme.flash(i);
                if (i < 3) {
                    final int gemIndex = (((int) i) + 1) % 3;
                    final Tile tileGem = Level.tileGem;
                    if (tileGem != null) {
                        tileGem.setForeground(gem[gemIndex]);
                    }
                    final Tile tileBlueGem = Level.tileBlueGem;
                    if (tileBlueGem != null) {
                        tileBlueGem.setForeground(gemBlue[gemIndex]);
                    }
        		}
            }
            Player.step();
		}
		
		@Override
	    protected final void destroy() {
			final Pangine engine = Pangine.getEngine();
			engine.getAudio().stopMusic();
	        Panmage.destroy(Level.timg);
	        Panmage.destroy(Level.bgimg);
	    }
	}
	
	private final static Img[] loadChrStrip(final String name, final int dim, final PixelFilter f) {
		return loadChrStrip(name, dim, f, true);
	}
	
	protected final static Img[] loadChrStrip(final String name, final int dim, final boolean req) {
		final String fileName = RES + "chr/" + name;
		if (!(req || Iotil.exists(fileName))) {
			return null;
		}
		final Img[] strip = ImtilX.loadStrip(fileName, dim);
		return strip;
	}
	
	private final static Img[] loadChrStrip(final String name, final int dim, final PixelFilter f, final boolean req) {
	    final Img[] strip = loadChrStrip(name, dim, req);
	    if (strip == null) {
	    	return null;
	    }
	    filterStrip(strip, strip, f);
		return strip;
	}
	
	private final static void filterStrip(final Img[] in, final Img[] out, final PixelFilter f) {
		filterStrip(in, out, null, f);
	}
	
	private final static void filterStrip(final Img[] in, final Img[] out, final PixelMask m, final PixelFilter f) {
		if (f != null) {
			final int size = in.length;
			for (int i = 0; i < size; i++) {
				out[i] = Imtil.filter(in[i], m, f);
			}
		}
	}
	
	protected final static PlayerContext newPlayerContext(final Profile profile, final ControlScheme ctrl, final int index) {
		final PlayerContext pc = new PlayerContext(profile, ctrl, index);
		Coltil.set(pcs, index, pc);
		return pc;
	}
	
	private final static void createAnimalStrip(final Profile profile, final ControlScheme ctrl, final int index) {
		final PlayerContext pc = newPlayerContext(profile, ctrl, index);
		reloadAnimalStrip(pc);
	}
	
	private final static void buildGuy(final Img guy, final Img face, final Img[] tails, final Img eyes, final Img clothing, final Img clothingOverlay,
			final int y, final int t) {
        if (tails != null) {
            Imtil.copy(tails[0], guy, 0, 0, 12, 12, t, 20 + y - t, Imtil.COPY_BACKGROUND);
        }
        if (clothing != null) {
            Imtil.copy(clothing, guy, 0, 0, 32, 32, 0, 0, Imtil.COPY_FOREGROUND);
        }
        Imtil.copy(face, guy, 0, 0, face.getWidth(), face.getHeight(), 8, 1 + y, Imtil.COPY_FOREGROUND);
        if (clothingOverlay != null) {
            Imtil.copy(clothingOverlay, guy, 0, 0, 32, 32, 0, 0, Imtil.COPY_FOREGROUND);
        }
        /*if (hat != null) {
            Imtil.copy(hat, guy, 0, 0, 18, 18, 8, 1 + y, Imtil.COPY_FOREGROUND);
        }*/
        Imtil.copy(eyes, guy, 0, 0, 8, 4, 15, 10 + y, Imtil.COPY_FOREGROUND);
	}
	
	private final static void buildGuyRide(final Img guy, final Img face, final Img eyes, final Img clothing) {
		Imtil.copy(face, guy, 0, 0, face.getWidth(), face.getHeight(), 8, 0, Imtil.COPY_FOREGROUND);
		Imtil.copy(eyes, guy, 0, 0, 8, 4, 15, 9, Imtil.COPY_FOREGROUND);
		if (clothing != null) {
            Imtil.copy(clothing, guy, 0, 0, 32, 32, 0, 0, Imtil.COPY_FOREGROUND);
        }
	}
	
	private final static PixelFilter getFilter(final SimpleColor col) {
		float r = col.r, g = col.g, b = col.b;
	    if (r == 0 && g == 0 && b == 0) {
	    	r = g = b = 0.09375f;
	    }
	    return new MultiplyPixelFilter(Channel.Blue, r, Channel.Blue, g, Channel.Blue, b);
	}
	
	private static void setMaskedFilter(final PixelFilter f, final boolean needed, final SimpleColor col2) {
        if (needed) {
            f.setMaskedFilter(getFilter(col2));
        }
    }
	
	private final static Img getImg(final HashMap<String, Img> all, final String type, final String anm) {
		Img raw = all.get(anm);
		if (raw == null) {
			raw = ImtilX.loadImage(RES + "chr/" + type + anm + ".png", false);
			raw.setTemporary(false);
			all.put(anm, raw);
		}
		return raw;
	}
	
	protected final static int fixEye(final int i) {
		return (i < 1) ? 1 : i;
	}
	
	private final static Img getEyes(final Img[] eyesAll, int i, final String loc) {
		i = fixEye(i);
		Img e = eyesAll[i - 1];
		if (e == null) {
		    e = ImtilX.loadImage(RES + "chr/" + loc + "Eyes" + Chartil.padZero(i, 2) + ".png", false);
		    eyesAll[i - 1] = e;
		}
		return e;
	}
	
	private final static Img getEyes(final int i) {
	    return getEyes(eyesAll, i, "");
	}
	
	private final static Img[] getImages(final HashMap<String, Img[]> all, final String key, final String name, final int dim) {
		Img[] guysRaw = all.get(key);
        if (guysRaw == null) {
            guysRaw = loadChrStrip(name, dim, true);
            Img.setTemporary(false, guysRaw);
            all.put(key, guysRaw);
        }
        return guysRaw;
	}
	
	private final static Img[] getBodies(final String body) {
	    return getImages(bodiesAll, body, "Bear" + body + ".png", 32);
	}
	
	private final static Img[] getBirds(final String kind) {
	    if (KIND_BEE.equalsIgnoreCase(kind)) {
	        return bees;
	    }
	    return getImages(birdsAll, kind, "bird/" + Chartil.toCode(kind, false) + ".png", 16);
    }
	
	protected final static Panimation getBirdAnm(final String pre, final String kind, final int eyeIndex) {
	    if (kind == null) {
	        return null;
	    }
        final Img[] birdRaw = getBirds(kind);
        final int size = birdRaw.length;
        final boolean bee = KIND_BEE.equalsIgnoreCase(kind);
        final Panframe[] bird = new Panframe[bee ? 4 : size];
        final Img eye = getEyes(eyeIndex);
        final int eyeWidth = eye.getWidth(), eyeHeight = eye.getHeight();
        final String bpre = pre + ".bird";
        final String ibpre = PRE_IMG + bpre + ".";
        final String fbpre = PRE_FRM + bpre + ".";
        final Pangine engine = Pangine.getEngine();
        for (int i = 0; i < size; i++) {
            final Img img = Imtil.copy(birdRaw[i]);
            Imtil.copy(eye, img, 0, 0, eyeWidth, eyeHeight, 8, 5, Imtil.COPY_FOREGROUND);
            final int dur;
            if (bee) {
                dur = 2;
            } else {
                dur = (i == 1) ? 3 : 6;
            }
            final Panframe f = engine.createFrame(fbpre + i, engine.createImage(ibpre + i, oBird, null, null, img), dur);
            bird[i] = f;
            if (bee && (i == 1)) {
                bird[3] = f;
            }
        }
        return engine.createAnimation(PRE_ANM + bpre, bird);
	}
	
	protected final static Panmage getEgg() {
	    if (egg == null) {
	        egg = createImage(PRE_IMG + "egg", RES + "chr/bird/Egg.png", 16, oBird);
	    }
	    return egg;
	}
	
	protected final static Panmage getEgg8() {
        if (egg8 == null) {
            egg8 = createImage(PRE_IMG + "egg8", RES + "chr/bird/Egg8.png", 8, oBird);
        }
        return egg8;
    }
	
	protected final static Panmage getCrown(final int i) {
	    if (crownPanmages == null) {
	        crownPanmages = new Panmage[crowns.length];
	    }
	    Panmage crown = crownPanmages[i];
	    if (crown == null) {
	        crown = Pangine.getEngine().createImage(PRE_IMG + "crown." + i, crowns[i]);
	        crownPanmages[i] = crown;
	    }
	    return crown;
	}
	
	protected final static class PlayerImages {
		protected final PixelFilter f;
		protected final Img[] guys;
		protected final Img guyBlink;
		protected final Img face;
		protected final Img[] tails;
		protected final Img eyes;
		protected final PixelFilter clothingFilter;
		protected final PixelFilter hatFilter;
	
		protected PlayerImages(final Avatar avatar) {
		    f = getFilter(avatar.col);
		    final String anm = avatar.anm;
		    final Animal animal = avatar.getAnimal();
		    setMaskedFilter(f, animal != null, avatar.col2);
		    final Clothing c = avatar.clothing.clth;
		    Img[] guysRaw;
		    final String body = c == null ? null : c.getBody();
		    final boolean needDragon = avatar.jumpMode == Player.JUMP_DRAGON;
		    if (needDragon) {
		    	if (animal == null) {
			    	if (guysRide == null) {
			    		guysRide = loadChrStrip("BearRide.png", 32, true);
					    Img.setTemporary(false, guysRide);
			    	}
			    	guysRaw = guysRide;
		    	} else {
		    		guysRaw = getBodies(anm + "Ride");
		    	}
		    } else if (body == null) {
		    	if (animal == null) {
		    		guysRaw = guysBlank;
		    	} else {
		    		guysRaw = getBodies(anm);
		    	}
		    } else {
		    	guysRaw = getBodies(body);
		    }
			guys = new Img[guysRaw.length];
			filterStrip(guysRaw, guys, greyMask, f);
			final boolean hasStill = hasStill(guys);
			guyBlink = Imtil.copy(getStill(guys, hasStill));
			final Img faceRaw = getImg(facesAll, "Face", anm);
			face = Imtil.filter(faceRaw, greyMask, f);
			final String tailAnm = avatar.getTailAnm();
			Img[] tailsRaw = tailsAll.get(tailAnm);
			if (tailsRaw == null && !tailsAll.containsKey(tailAnm)) {
				tailsRaw = loadChrStrip("Tail" + tailAnm + ".png", 12, false);
				Img.setTemporary(false, tailsRaw);
				tailsAll.put(tailAnm, tailsRaw);
			}
			if (tailsRaw == null) {
				tails = null;
			} else {
				tails = new Img[tailsRaw.length];
				filterStrip(tailsRaw, tails, greyMask, f);
			}
			eyes = getEyes(avatar.eye);
			final Img[] clothings, clothingOverlays;
			if (c == null) {
			    clothingFilter = null;
			    clothings = null;
			    clothingOverlays = null;
			} else {
			    c.init();
			    final Img[] imgs, imgOverlays;
			    if (needDragon) {
			        imgs = c.rideImgs;
			        imgOverlays = null;
			    } else {
			        imgs = c.imgs;
			        imgOverlays = c.imgOverlays;
			    }
			    clothings = new Img[imgs.length];
			    clothingOverlays = imgOverlays == null ? null : new Img[imgOverlays.length];
			    clothingFilter = getFilter(avatar.clothing.col);
			    filterStrip(imgs, clothings, greyMask, clothingFilter);
			    if (clothingOverlays != null) {
			        filterStrip(imgOverlays, clothingOverlays, greyMask, clothingFilter);
			    }
			}
			final Hat h = (Hat) avatar.hat.clth;
			final Img hat, mask;
			if (h == null) {
				hatFilter = null;
				hat = null;
				mask = null;
			} else {
				h.init();
				hatFilter = getFilter(avatar.hat.col);
				setMaskedFilter(hatFilter, h.isSecondaryColorSupported(), avatar.hat.col2);
				hat = Imtil.filter(h.imgs[0], greyMask, hatFilter);
				mask = h.maskNeeded ? getImg(masksAll, "mask/Mask", avatar.getBaseAnm()) : null;
			}
			if (hat != null) {
			    final PixelMask dstMask;
			    if (h.frontNeeded) {
			        dstMask = ImgPixelMask.getMask(mask, Pancolor.BLACK);
			    } else {
			        dstMask = VisiblePixelMask.getInstance();
			    }
		    	Imtil.copy(hat, face, 0, 0, 18, 18, 0, 0, TransparentPixelMask.getInstance(), dstMask);
		    	hat.close();
		    }
			if (needDragon) {
				final Img clth = clothings == null ? null : clothings[1];
				buildGuyRide(guys[1], face, eyes, clth);
				buildGuyRide(guyBlink, face, eyesBlink, clth);
				if (clothings != null) {
		            Imtil.copy(clothings[0], guys[0], 0, 0, 32, 32, 0, 0, Imtil.COPY_FOREGROUND);
		        }
			} else {
				final int size = guys.length;
				for (int i = 0; i < size; i++) {
					final int tailH;
					if (i < 3) {
						tailH = i;
					} else if (i == 5) {
						tailH = 0;
					} else {
						tailH = 1;
					}
					buildGuy(guys[i], face, tails, eyes, clothings == null ? null : clothings[i], clothingOverlays == null ? null : clothingOverlays[i], (i == 3) ? -1 : 0, tailH);
				}
				buildGuy(guyBlink, face, tails, eyesBlink, clothings == null ? null : getStill(clothings, hasStill), clothingOverlays == null ? null : getStill(clothingOverlays, hasStill), 0, 0);
			}
			Img.close(clothings);
		}
		
		protected final void close() {
			Img.close(guys);
			Img.close(tails);
			Img.close(guyBlink, face);
		}
	}
	
	private final static boolean hasStill(final Img[] guys) {
	    return guys.length > 5;
	}
	
	private final static Img getStill(final Img[] guys, final boolean hasStill) {
	    return guys[hasStill ? 5 : guys.length == 2 ? 1 : 0];
	}
	
	protected final static void reloadAnimalStrip(final PlayerContext pc) {
		reloadAnimalStrip(pc, true);
	}
	
	protected final static void reloadAnimalStrip(final PlayerContext pc, final boolean full) {
		pc.destroy();
		final Profile profile = pc.profile;
	    final Avatar avatar = profile.currentAvatar;
	    final String anm = avatar.anm, baseAnm = avatar.getBaseAnm();
	    final PlayerImages pi = new PlayerImages(avatar);
	    final Img guys[] = pi.guys, tails[] = pi.tails, eyes = pi.eyes;
		final String pre = "guy." + pc.index;
		
		final Pangine engine = Pangine.getEngine();
		final String ipre = PRE_IMG + pre + ".";
		final boolean hasStill = hasStill(guys);
		final boolean needDragon = avatar.jumpMode == Player.JUMP_DRAGON;
		final Panple oStill = needDragon ? or : og;
		final Panmage guy = engine.createImage(ipre + "still", oStill, ng, xg, getStill(guys, hasStill));
		final Panmage guyB = engine.createImage(ipre + "blink", oStill, ng, xg, pi.guyBlink);
		final String fpre = PRE_FRM + pre + ".";
		final String spre = fpre + "still.";
		final Panframe gfs1 = engine.createFrame(spre + "1", guy, DUR_BLINK - DUR_CLOSED), gfs2 = engine.createFrame(spre + "2", guyB, DUR_CLOSED);
		pc.guy = engine.createAnimation(PRE_ANM + pre + ".still", gfs1, gfs2);
		
		final boolean needWing = avatar.jumpMode == Player.JUMP_FLY;
		final PixelFilter pf;
		Img drgnEye = null;
		if (needWing) {
			pf = getFilter(avatar.jumpCol);
		} else if (needDragon) {
			pf = getFilter(avatar.dragon.col);
			drgnEye = getEyes(dragonEyesAll, avatar.dragon.eye, "dragon/Dragon");
		} else {
			pf = null;
		}
		
		pc.bird = getBirdAnm(pre, avatar.bird.kind, avatar.bird.eye);
		
		if (full) {
		    ImtilX.validateDefault = false;
			Img[] faceMapRaw = faceMapsAll.get(anm);
			if (faceMapRaw == null) {
				faceMapRaw = ImtilX.loadStrip(RES + "chr/FaceMap" + anm + ".png", 18);
				Img.setTemporary(false, faceMapRaw);
				faceMapsAll.put(anm, faceMapRaw);
			}
			final Img[] faceMap = new Img[faceMapRaw.length];
			filterStrip(faceMapRaw, faceMap, greyMask, pi.f);
			ImtilX.validateDefault = true;
			final Img[] hatMapRaw = (avatar.hat.clth == null) ? null : avatar.hat.clth.mapImgs;
			if (hatMapRaw != null) {
				final Img[] hatMap, maskMap;
				hatMap = new Img[hatMapRaw.length];
				filterStrip(hatMapRaw, hatMap, greyMask, pi.hatFilter);
				final Hat hat = (Hat) avatar.hat.clth;
				final boolean maskNeeded = hat.maskNeeded;
				ImtilX.validateDefault = false;
				maskMap = maskNeeded ? loadChrStrip("mask/MaskMap" + baseAnm + ".png", 18, null) : null;
				ImtilX.validateDefault = true;
				final PixelMask srcMask = TransparentPixelMask.getInstance();
				for (int i = 0; i < 3; i++) {
					final PixelMask dstMask;
					if ((i == 2) && !hat.backNeeded) {
						dstMask = VisiblePixelMask.getInstance();
					} else if ((i != 2) && !hat.frontNeeded) {
					    dstMask = VisiblePixelMask.getInstance();
					} else if (maskNeeded) {
						dstMask = new ImgPixelMask(maskMap[i], Pancolor.BLACK);
					} else {
						dstMask = null;
					}
					Imtil.copy(hatMap[i], faceMap[i], 0, 0, 18, 18, 0, 0, srcMask, dstMask);
				}
				Img.close(hatMap);
				Img.close(maskMap);
			}
			
			final String rpre = fpre + "run.";
			if (needDragon) {
				final Img guy0 = guys[0], face1 = faceMap[1];
				Imtil.copy(face1, guy0, 0, 0, face1.getWidth(), face1.getHeight(), 7, 0, Imtil.COPY_FOREGROUND);
				Imtil.copy(eyes, guy0, 0, 0, 4, 4, 18, 9, Imtil.COPY_FOREGROUND);
				if (tails != null) {
					Imtil.copy(tails[1], guy0, 0, 0, 12, 12, 0, 19, Imtil.COPY_BACKGROUND);
				}
				final Panmage guy1 = engine.createImage(ipre + "1", or, ng, xg, guy0);
				final Panframe gfr1 = engine.createFrame(rpre + "1", guy1, 2);
				pc.guyRun = engine.createAnimation(PRE_ANM + pre + ".run", gfr1);
				pc.guyJump = guy1;
				pc.guyFall = guy1;
			} else {
			    final Panmage guy1 = hasStill ? engine.createImage(ipre + "1", og, ng, xg, guys[0]) : guy;
				final Panmage guy2 = engine.createImage(ipre + "2", og, ng, xg, guys[1]);
				final Panmage guy3 = engine.createImage(ipre + "3", og, ng, xg, guys[2]);
				final Panframe gfr1 = engine.createFrame(rpre + "1", guy1, 2), gfr2 = engine.createFrame(rpre + "2", guy2, 2), gfr3 = engine.createFrame(rpre + "3", guy3, 2);
				pc.guyRun = engine.createAnimation(PRE_ANM + pre + ".run", gfr2, gfr3, gfr1);
				pc.guyJump = engine.createImage(ipre + "jump", og, ng, xg, guys[3]);
				pc.guyFall = engine.createImage(ipre + "fall", og, ng, xg, guys[4]);
			    //guy = engine.createImage(pre, new FinPanple2(8, 0), null, null, ImtilX.loadImage(RES + "chr/Player.png"));
			}
		    
			//final Img[] maps = loadChrStrip("BearMap.png", 32, pi.f);
			final Animal animal = avatar.getAnimal();
			final Img[] mapsRaw = getBodies((animal == null) ? "Map" : (anm + "Map"));
			final Img[] maps = new Img[mapsRaw.length];
			filterStrip(mapsRaw, maps, greyMask, pi.f);
			final Img[] clothingMapRaw = (avatar.clothing.clth == null) ? null : avatar.clothing.clth.mapImgs;
			final Img[] clothingMap;
			if (clothingMapRaw == null) {
			    clothingMap = null;
            } else {
                clothingMap = new Img[clothingMapRaw.length];
                filterStrip(clothingMapRaw, clothingMap, greyMask, pi.clothingFilter);
            }
			final Img[] wingMap = needWing ? loadChrStrip("WingsMap.png", 32, pf) : null;
			final Img[] dragonMap = needDragon ? loadChrStrip("DragonMap.png", 32, pf) : null;
			final Img south1 = maps[0], southPose = maps[5], faceSouth = faceMap[0];
			if (needWing) {
				for (final Img south : new Img[] {south1, southPose}) {
					Imtil.copy(wingMap[0], south, 0, 0, 32, 32, 0, 0, Imtil.COPY_BACKGROUND);
				}
			}
			if (clothingMap != null) {
                Imtil.copy(clothingMap[0], south1, 0, 0, 32, 32, 0, 0, Imtil.COPY_FOREGROUND);
                Imtil.copy(clothingMap[5], southPose, 0, 0, 32, 32, 0, 0, Imtil.COPY_FOREGROUND);
            }
			Img south2 = null;
			if (!needDragon) {
				south2 = Imtil.copy(south1);
				Imtil.mirror(south2);
			}
			for (final Img south : new Img[] {south1, south2, southPose}) {
				if (south == null) {
					continue;
				}
				Imtil.copy(faceSouth, south, 0, 0, faceSouth.getWidth(), faceSouth.getHeight(), 7, 5, Imtil.COPY_FOREGROUND);
				Imtil.copy(eyes, south, 0, 0, 8, 4, 12, 14, Imtil.COPY_FOREGROUND);
			}
			final int drgnY = 12;
			if (needDragon) {
				final Img drgnSth = dragonMap[0];
				for (Img south : new Img[] {south1, southPose, null}) {
					if (south == null) {
						south = south2;
						Imtil.mirror(drgnSth);
					} else {
						//Imtil.move(south, 5, -5);
						Imtil.addBordersImg(south, 0, 0, 0, drgnY);
						if (south == south1) {
							south2 = Imtil.copy(south1);
						}
					}
					Imtil.copy(drgnSth, south, 0, 0, 32, 32, 0, drgnY, Imtil.COPY_FOREGROUND);
					Imtil.copy(drgnEye, south, 9, 0, 14, 7, 9, drgnY + 10, Imtil.COPY_FOREGROUND);
				}
			}
			pc.mapSouth = createAnmMap(pre, "south", south1, south2);
	        pc.mapPose = engine.createImage(ipre + "map.pose", ORIG_MAP, null, null, southPose);
			final Img east1 = maps[1], faceEast = faceMap[1];
			Img east2 = needDragon ? null : maps[2];
			final Img[] easts = {east1, east2};
			final int drgnYh = 8, drgnX = 7;
			for (final Img east : easts) {
				if (needWing) {
					Imtil.copy(wingMap[1], east, 0, 0, 32, 32, 0, 0, Imtil.COPY_BACKGROUND);
				}
				if (clothingMap != null) {
                    Imtil.copy(clothingMap[east == east1 ? 1 : 2], east, 0, 0, 32, 32, 0, 0, Imtil.COPY_FOREGROUND);
                    //Imtil.copy(clothingMap[2], east2, 0, 0, 32, 32, 0, 0, Imtil.COPY_FOREGROUND);
                }
				Imtil.copy(faceEast, east, 0, 0, faceEast.getWidth(), faceEast.getHeight(), 7, 5, Imtil.COPY_FOREGROUND);
				if (tails != null) {
					Imtil.copy(tails[1], east, 0, 0, 12, 12, 1, 20, Imtil.COPY_BACKGROUND);
				}
				if (needDragon) {
					//Imtil.move(east, -3, -5);
				    Imtil.addBordersImg(east, drgnX, drgnX, 0, drgnYh);
				    Imtil.move(east, -6, 0);
					east2 = Imtil.copy(east);
					easts[1] = east2;
					final Img drgnEast = dragonMap[1];
					Imtil.copy(drgnEye, drgnEast, 0, 0, 9, 7, 16, 10, Imtil.COPY_FOREGROUND);
					Imtil.copy(drgnEast, east, 0, 0, 32, 32, drgnX, drgnYh, Imtil.COPY_BACKGROUND);
					break;
				}
			}
			if (needDragon) {
				final Img drgnEast = dragonMap[2];
				Imtil.copy(drgnEye, drgnEast, 0, 0, 9, 7, 16, 10, Imtil.COPY_FOREGROUND);
			    Imtil.copy(drgnEast, east2, 0, 0, 32, 32, drgnX, drgnYh, Imtil.COPY_BACKGROUND);
			}
			final Img west1 = Imtil.copy(east1), west2 = Imtil.copy(east2);
			final Img eyesEast = eyes.getSubimage(0, 0, 4, 4);
			final int eyeDstY = 14;
			for (final Img east : easts) {
				Imtil.copy(eyesEast, east, 0, 0, 4, 4, needDragon ? 19 : 18, eyeDstY, Imtil.COPY_FOREGROUND);
			}
			eyesEast.close();
			final Panple origEastWest = needDragon ? ORIG_MAP_DRAGON_EAST_WEST : ORIG_MAP;
			pc.mapEast = createAnmMap(pre, "east", origEastWest, east1, east2);
			Imtil.mirror(west1);
			Imtil.mirror(west2);
			final Img eyesWest = eyes.getSubimage(4, 0, 4, 4);
			for (final Img west : new Img[] {west1, west2}) {
				Imtil.copy(eyesWest, west, 0, 0, 4, 4, needDragon ? 23 : 10, eyeDstY, Imtil.COPY_FOREGROUND);
			}
			eyesWest.close();
			pc.mapWest = createAnmMap(pre, "west", origEastWest, west1, west2);
			final Img tailNorth = Coltil.get(tails, 2), faceNorth = faceMap[2];
			final Img wing = needWing ? wingMap[0] : null;
			final Img drgn = needDragon ? dragonMap[3] : null;
			pc.mapNorth = createNorth(maps, 3, wing, drgn, clothingMap, tailNorth, faceNorth, pre, "North");
			pc.mapLadder = needDragon ? pc.mapNorth : createNorth(maps, 4, wing, drgn, clothingMap, tailNorth, faceNorth, pre, "Ladder");
			
			Img.close(maps);
			Img.close(clothingMap);
			Img.close(wingMap);
			Img.close(dragonMap);
			Img.close(faceMap);
		}
		
		if (needWing) {
		    final String wpre = pre + ".wing.";
		    final String iwpre = PRE_IMG + wpre;
		    final Img[] wings = loadChrStrip("Wings.png", 32, pf);
		    pc.back = engine.createImage(iwpre + "still", ow, ng, xg, wings[0]);
		    if (full) {
			    final String fwpre = PRE_FRM + wpre;
			    final Panframe[] frames = new Panframe[6];
			    for (int i = 0; i < 6; i++) {
				    final Panmage img = engine.createImage(iwpre + "fly." + i, i > 3 ? owf : ow, ng, xg, wings[i + 1]);
					frames[i] = engine.createFrame(fwpre + "fly." + i, img, (i == 0 || i == 3) ? 6 : 3);
			    }
			    pc.backJump = engine.createAnimation(PRE_ANM + wpre + ".fly", frames[1], frames[2], frames[3], frames[0]);
			    pc.backFall = engine.createAnimation(PRE_ANM + wpre + ".fall", frames[4], frames[5]);
		    }
		    Img.close(wings);
		} else if (full && avatar.jumpMode == Player.JUMP_HIGH) {
		    pc.backJump = createAnm(pre + ".spring", RES + "chr/Springs.png", 32, 5, os, ng, xg);
		} else if (needDragon) {
			final String wpre = pre + ".dragon.";
		    final String iwpre = PRE_IMG + wpre;
			final Img[] drgns = loadChrStrip("Dragon.png", 32, pf);
			final Img drgnStill = drgns[3];
			Imtil.copy(drgnEye, drgnStill, 23, 0, 13, 7, 14, 6, Imtil.COPY_FOREGROUND);
			pc.back = engine.createImage(iwpre + "still", odf, ng, xg, drgnStill);
		    if (full) {
			    final String fwpre = PRE_FRM + wpre;
			    final Panframe[] frames = new Panframe[5];
			    for (int i = 0, f = 0; i < 6; i++) {
			    	if (i == 3) {
			    		continue;
			    	}
			    	final Img drgni = drgns[i];
			    	Imtil.copy(drgnEye, drgni, 0, 0, 9, 7, 16, (i < 3) ? (6 - i) : 6, Imtil.COPY_FOREGROUND);
				    final Panmage img = engine.createImage(iwpre + "move." + i, od, ng, xg, drgni);
					frames[f] = engine.createFrame(fwpre + "move." + i, img, 2);
					f++;
			    }
			    pc.backRun = engine.createAnimation(PRE_ANM + wpre + ".run", frames[0], frames[1], frames[2]);
			    pc.backJump = engine.createAnimation(PRE_ANM + wpre + ".jump", frames[3]);
			    pc.backFall = engine.createAnimation(PRE_ANM + wpre + ".fall", frames[4]);
		    }
		    Img.close(drgns);
		}
		
		pi.close();
	}
	
	private final static Panimation createNorth(final Img[] maps, final int mi, final Img wing, final Img drgn, final Img[] clothingMap,
	                                            final Img tailNorth, final Img faceNorth,
	                                            final String pre, final String suf) {
		final Img north1 = maps[mi];
		if (clothingMap != null) {
		    Imtil.copy(clothingMap[mi], north1, 0, 0, 32, 32, 0, 0, Imtil.COPY_FOREGROUND);
		}
		if (tailNorth != null) {
			Imtil.copy(tailNorth, north1, 0, 0, 12, 12, 10, 20, Imtil.COPY_FOREGROUND);
		}
		if (wing != null) {
			Imtil.copy(wing, north1, 0, 0, 32, 32, 0, 0, Imtil.COPY_FOREGROUND);
		}
		if (drgn != null) {
			Imtil.move(north1, 0, -5);
		}
		Img north2 = null;
		if (drgn == null) {
			north2 = Imtil.copy(north1);
			Imtil.mirror(north2);
		}
		for (final Img north : new Img[] {north1, north2}) {
			if (north == null) {
				continue;
			}
			Imtil.copy(faceNorth, north, 0, 0, faceNorth.getWidth(), faceNorth.getHeight(), 7, drgn == null ? 5 : 0, Imtil.COPY_FOREGROUND);
		}
		if (drgn != null) {
			north2 = Imtil.copy(north1);
			for (final Img north : new Img[] {north1, north2}) {
				if (north == north2) {
					Imtil.mirror(drgn);
				}
				Imtil.copy(drgn, north, 0, 0, 32, 32, 0, 0, Imtil.COPY_BACKGROUND);
			}
		}
		return createAnmMap(pre, suf, north1, north2);
	}
	
	private final static Panimation createAnmMap(final String pre, final String suf, final Img... a) {
		return createAnmMap(pre, suf, ORIG_MAP, a);
	}
	
	private final static Panimation createAnmMap(final String pre, final String suf, final Panple o, final Img... a) {
		return createAnm(pre + ".map." + suf.toLowerCase(), DUR_MAP, o, a);
	}
	
	private final static void replace(final ReplacePixelFilter f, final short r, final short g, final short b) {
		f.put(r, g, b, Pancolor.MAX_VALUE, r, b, g, Pancolor.MAX_VALUE);
	}
	
	protected final static Profile loadProfile(final String pname, final ControlScheme ctrl, final int index) throws Exception {
		final SegmentStream plist = SegmentStream.openLocation(Profile.getFileName(pname));
		/*
		PRF|Andrew|Balue|1|0
		AVT|Grabbit|Rabbit|2|0|1|0.25
		AVT|Balue|Bear|1|0|1|1
		*/
		final Profile profile = new Profile();
        //profile.setName(pname);
        Segment seg = null;
        seg = plist.readRequire(SEG_PRF);
        profile.load(seg);
        Pangine.getEngine().setFrameRate(profile.frameRate);
        final String curName = seg.getValue(1);
        seg = plist.readIf(SEG_STX);
        if (seg != null) {
        	profile.stats.load(seg, profile.getGems());
        }
        seg = plist.readIf(SEG_ACH);
        if (seg != null) {
        	profile.loadAchievements(seg);
        }
        seg = plist.readIf(SEG_LOC);
        if (seg != null) {
            profile.loadLocation(seg);
        }
        while ((seg = plist.readIf(SEG_AVT)) != null) {
        	final Avatar avatar = new Avatar();
        	avatar.load(seg);
        	profile.avatars.add(avatar);
        }
        plist.close();
        /*avatar.setName("Balue");
        avatar.anm = "Bear";
        avatar.eye = 1;
        avatar.r = 0;
        avatar.g = 1;
        avatar.b = 1;*/
        profile.setCurrentAvatar(curName);
        //profile.ctrl = 0;
		createAnimalStrip(profile, ctrl, index);
		//profile.serialize("temptemp.txt");
		//createAnimalStrip("Grabbit", "Rabbit", 2, new MultiplyPixelFilter(Channel.Blue, 0f, Channel.Blue, 1f, Channel.Blue, 0.25f), 1);
		//createAnimalStrip("Roddy", "Mouse", 3, new SwapPixelFilter(Channel.Blue, Channel.Red, Channel.Blue), 0);
		//createAnimalStrip("Felip", "Cat", 4, new SwapPixelFilter(Channel.Red, Channel.Red, Channel.Blue), 0);
		return profile;
	}
	
	private final static void loadConfig() {
	    // SegmentStream.readLocation creates a file containing "CFG|" if it doesn't exist
        final Segment cfg = SegmentStream.readLocation(FILE_CFG, "CFG|").get(0);
        // CFG|Andrew
        Config.defaultProfileName = cfg.getValue(0);
        Config.btnSize = cfg.getInt(1, 0);
        Config.zoomMag = cfg.getInt(2, -1);
        zoomMag = Config.zoomMag;
        Config.setMusicEnabled(cfg.getBoolean(3, Config.DEF_MUSIC_ENABLED));
        Config.setSoundEnabled(cfg.getBoolean(4, Config.DEF_SOUND_ENABLED));
	}
	
	private final static void loadConstants() throws Exception {
	    if (loaders == null) {
	        return;
	    }
	    
		final Pangine engine = Pangine.getEngine();
		
		loaders.add(new Runnable() { @Override public final void run() {
		    guysBlank = loadChrStrip("Bear.png", 32, true);
		    Img.setTemporary(false, guysBlank);
		    eyesBlink = ImtilX.loadImage(RES + "chr/EyesBlink.png", false); }});
		
		loaders.add(new Runnable() { @Override public final void run() {
//info("loadConstants start " + System.currentTimeMillis());
			Coltil.set(allEnemies, Level.DROWID, new EnemyDefinition("Drowid", 1, null, true, 1)); }}); // Teleport when stomped
		loaders.add(new Runnable() { @Override public final void run() {
			final Panmage pimg1 = createImage("projectile1", RES + "enemy/Projectile1.png", 8, CENTER_8, new FinPanple2(-3, -3), new FinPanple2(2, 2));
		    final Panframe[] pfrms = new Panframe[4];
		    for (int i = 0; i < 4; i++) {
		        pfrms[i] = engine.createFrame(PRE_FRM + "projectile1." + i, pimg1, 4, (4 - i) % 4, false, false);
		    }
		    projectile1 = engine.createAnimation(PRE_ANM + "projectile1", pfrms);
			final EnemyDefinition drolock = new EnemyDefinition("Drolock", 4, null, false, 0, 0);
			drolock.projectile = projectile1;
			Coltil.set(allEnemies, Level.DROLOCK, drolock); }}); // Teleport/shoot periodically
		loaders.add(new Runnable() { @Override public final void run() {
		    Coltil.set(allEnemies, Level.HOB_TROLL, new EnemyDefinition("Hob-troll", 2, null, true)); }}); // Was Troblin
		loaders.add(new Runnable() { @Override public final void run() {
			final ReplacePixelFilter f = new ReplacePixelFilter();
			replace(f, (short) 104, (short) 120, (short) 172);
			replace(f, (short) 80, (short) 96, (short) 144);
			replace(f, (short) 64, (short) 80, (short) 112);
			replace(f, (short) 48, (short) 56, (short) 80);
			Coltil.set(allEnemies, Level.HOB_OGRE, new EnemyDefinition("Hob-ogre", 2, f, false)); // Was Obglin
			final int impX = 4, impH = 14;
			imp = new EnemyDefinition("Imp", 3, null, true, true, impX, impH);
			Coltil.set(allEnemies, Level.IMP, imp);
			final EnemyDefinition troll, ogre;
			troll = new EnemyDefinition("Troll", 5, null, true, false, 0, 8, 30, 1, 32);
			troll.award = GemBumped.AWARD_2;
			final class MultiStompHandler implements InteractionHandler {
			    private final int n;
			    private MultiStompHandler(final int n) {
			        this.n = n;
			    }
			    @Override public final boolean onInteract(final Enemy enemy, final Player player) {
			        final int a = Math.abs(enemy.hv);
			        if (a > 1 && enemy.timer > 0) {
                        return true;
                    }
			        final int amt = (player != null && player.isDragonStomping()) ? 2 : 1;
			        if ((a + amt) <= n) {
                        if (enemy.hv > 0) {
                            enemy.hv += amt;
                        } else {
                            enemy.hv -= amt;
                        }
                        enemy.timer = 5;
                        final int stop = Math.abs(enemy.hv) - 1;
                        for (int i = 0; i < stop; i++) {
                            enemy.burst(anger, enemy, null, (32 * (n - 1)) + 4 + (i * 8));
                        }
                        soundBounce.startSound();
                        return true;
                    }
                    return false;
                }
			}
			troll.stompHandler = new MultiStompHandler(2);
            troll.stepHandler = new InteractionHandler() {
                @Override public final boolean onInteract(final Enemy enemy, final Player player) {
                	if (enemy.timer > 0) {
                		enemy.timer--;
                	}
                    return false;
                }};
            Coltil.set(allEnemies, Level.TROLL, troll);
			ogre = new EnemyDefinition("Ogre", 5, f, false, false, 0, 8, 30, 1, 32);
			ogre.init(troll);
			Coltil.set(allEnemies, Level.OGRE, ogre);
			trollColossus = new EnemyDefinition("Troll Colossus", 11, null, true, false, 0, 26, 62, 1, 64);
			trollColossus.award = GemBumped.AWARD_3;
			trollColossus.stompHandler = new MultiStompHandler(3);
			trollColossus.stepHandler = troll.stepHandler;
			Coltil.set(allEnemies, Level.TROLL_COLOSSUS, trollColossus);
			ogreBehemoth = new EnemyDefinition("Ogre Behemoth", 11, f, false, false, 0, 26, 62, 1, 64);
			ogreBehemoth.init(trollColossus);
			Coltil.set(allEnemies, Level.OGRE_BEHEMOTH, ogreBehemoth);
			final EnemyDefinition armorBall, thrownImp;
			armorBall = new EnemyDefinition("Armor Ball", 7, null, false, 0, 0);
			Enemy.currentWalk = 3;
			bounceBall = new EnemyDefinition("Bounce Ball", 7, null, false, 0, 4);
			Enemy.currentSplat = 8;
			armoredImp = new EnemyDefinition("Armored Imp", 6, null, true, true, Enemy.DEFAULT_X, Enemy.DEFAULT_H);
			armorBall.code = armoredImp.code;
			bounceBall.code = armoredImp.code;
			thrownImp = new EnemyDefinition("Thrown Imp", 8, null, false, false, 0, impX, impH, 10);
			thrownImp.code = imp.code;
			armorBall.stepHandler = new InteractionHandler() {
				@Override public final boolean onInteract(final Enemy enemy, final Player player) {
					if (enemy.timer == 0) {
						if (!enemy.full){
							return false;
						} else if (enemy.timerMode == 5) {
							openArmoredImp(enemy, enemy);
							return false;
						}
						enemy.v = 2;
						enemy.timerMode++;
						enemy.timer = (6 - enemy.timerMode) * 10;
					} else {
						enemy.timer--;
					}
					return false;
				}};
			armorBall.stompHandler = new InteractionHandler() {
				@Override public final boolean onInteract(final Enemy enemy, final Player player) {
					if (enemy.timerMode == 0 && enemy.timer > 57) {
						return true;
					} else if (enemy.full) {
						enemy.full = false;
						final Enemy imp = new Enemy(thrownImp, enemy);
						imp.setEnemyMirror(enemy.isMirror());
						imp.v = 2;
					}
					enemy.v = 2;
					soundArmor.startSound();
					return true;
				}};
			armorBall.rewardHandler = new InteractionHandler() {
				@Override public final boolean onInteract(final Enemy enemy, final Player player) {
					return enemy.full;
				}};
			armorBall.hurtHandler = new InteractionHandler() {
                @Override public final boolean onInteract(final Enemy enemy, final Player player) {
                    final Enemy e = new BounceBall(bounceBall, enemy, player);
                    e.full = enemy.full;
                    e.setEnemyMirror(player.isMirror());
                    e.v = 9;
                    player.pc.profile.stats.kicks++;
                    enemy.destroy();
                    soundArmor.startSound();
                    return false;
                }};
            armoredImp.splatDecider = new InteractionHandler() {
                @Override public final boolean onInteract(final Enemy enemy, final Player player) {
                    return player == null || !player.isDragonStomping();
                }};
			armoredImp.splatHandler = new BurstHandler() {@Override public final void onBurst(final CustomBurst burst) {
				final Enemy ball = new ArmorBall(armorBall, burst);
				ball.full = true;
				ball.setMirror(burst.isMirror()); }};
			bounceBall.stepHandler = new InteractionHandler() {
                @Override public final boolean onInteract(final Enemy enemy, final Player player) {
                    if (enemy.timer < 3) {
                    	enemy.timer++;
                    }
                    return false;
                }};
			bounceBall.landedHandler = new InteractionHandler() {
                @Override public final boolean onInteract(final Enemy enemy, final Player player) {
                	if (enemy.v > 0) {
                		return true;
                	}
                    enemy.v = -enemy.v * 0.9f;
                    if (enemy.v >= 1) {
                    	soundArmor.startSound();
                    }
                    return true;
                }};
            bounceBall.stompHandler = new InteractionHandler() {
                @Override public final boolean onInteract(final Enemy enemy, final Player player) {
                    final Enemy ball = new ArmorBall(armorBall, enemy);
                    ball.full = enemy.full;
                    ball.setMirror(enemy.isMirror());
                    enemy.destroy();
                    soundArmor.startSound();
                    return true;
                }};
            bounceBall.rewardHandler = armorBall.rewardHandler;
            bounceBall.hurtHandler = new InteractionHandler() {
                @Override public final boolean onInteract(final Enemy enemy, final Player player) {
                    return enemy.timer >= 3;
                }};
			thrownImp.splat = imp.splat;
			thrownImp.stepHandler = new InteractionHandler() {
                @Override public final boolean onInteract(final Enemy enemy, final Player player) {
                    final int hv = enemy.hv;
                    if (hv == 0) {
                    	if (enemy.timer < 38) {
                    		enemy.timer++;
                    	} else if (enemy.isGrounded()) {
	                        new Enemy(imp, enemy).setEnemyMirror(!enemy.isMirror());
	                        enemy.destroy();
                    	}
                    } else if (enemy.timer < 8 && Math.abs(hv) == 1) {
                    	enemy.timer++;
                    } else if (hv > 0) {
                        enemy.hv--;
                    } else {
                        enemy.hv++;
                    }
                    return false;
                }};
            thrownImp.defeatHandler = new InteractionHandler() {
                @Override public final boolean onInteract(final Enemy enemy, final Player player) {
                	return Math.abs(enemy.hv) < 4;
                }};
            Coltil.set(allEnemies, Level.ARMORED_IMP, armoredImp);
			spikedImp = new EnemyDefinition("Spiked Imp", 10, null, true);
			spikedImp.stompHandler = new InteractionHandler() {
                @Override public final boolean onInteract(final Enemy enemy, final Player player) {
                	if (player.isDragonStomping()) {
                		return false;
                	}
                    player.startHurt();
                    return true;
                }};
            spikedImp.award = GemBumped.AWARD_2;
            Coltil.set(allEnemies, Level.SPIKED_IMP, spikedImp);
			anger = createAnm("anger", 10, CENTER_16, Enemy.loadStrip(9, ImtilX.DIM));
			iceWisp = new EnemyDefinition("Ice Wisp", 12, Enemy.wispFactory);
			iceWisp.hurtHandler = new InteractionHandler() {
                @Override public final boolean onInteract(final Enemy enemy, final Player player) {
                	player.startFreeze();
                	return false;
                }};
            iceWisp.award = GemBumped.AWARD_2;
			Coltil.set(allEnemies, Level.ICE_WISP, iceWisp);
			fireWisp = new EnemyDefinition("Fire Wisp", 13, Enemy.wispFactory);
			fireWisp.hurtHandler = new InteractionHandler() {
                @Override public final boolean onInteract(final Enemy enemy, final Player player) {
                	player.startBurn();
                	return false;
                }};
            fireWisp.award = GemBumped.AWARD_2;
			Coltil.set(allEnemies, Level.FIRE_WISP, fireWisp);
			final int SPLAT_BLOB = 45;
			Enemy.currentSplat = SPLAT_BLOB;
			final ReplacePixelFilter bbf = new ReplacePixelFilter();
			bbf.put(Pancolor.MIN_VALUE, (short) 96, (short) 96, (short) 24, (short) 24, (short) 24);
			bbf.put(Pancolor.MIN_VALUE, (short) 136, (short) 136, (short) 48, (short) 48, (short) 48);
			bbf.put((short) 32, (short) 168, (short) 200, (short) 72, (short) 72, (short) 72);
			bbf.put((short) 64, (short) 192, Pancolor.MAX_VALUE, (short) 96, (short) 96, (short) 96);
			bbf.put((short) 96, (short) 216, Pancolor.MAX_VALUE, (short) 120, (short) 120, (short) 120);
			final EnemyDefinition blackBlob = new EnemyDefinition("Black Blob", 14, bbf, true, true, Enemy.DEFAULT_X, Enemy.DEFAULT_H); // Grim Blob
			blackBlob.splatHandler = new BurstHandler() {@Override public final void onBurst(final CustomBurst burst) {
				final Enemy prev = (Enemy) burst.getContext();
				final BurstHandler h = new BurstHandler() {@Override public final void onBurst(final CustomBurst b) {
					final Enemy blob = new Enemy(prev.def, b);
					blob.timer = prev.timer;
					blob.lastStomper = prev.lastStomper;
					blob.setEnemyMirror(b.isMirror()); }};
				Enemy.burst(burst, prev.def.extra, burst, h, 0); }};
			blackBlob.award = GemBumped.AWARD_2;
			Coltil.set(allEnemies, Level.BLACK_BLOB, blackBlob);
			Enemy.currentSplat = SPLAT_BLOB;
			final EnemyDefinition blob = new EnemyDefinition("Blob", 14, null, true, true, Enemy.DEFAULT_X, Enemy.DEFAULT_H);
			blob.splatHandler = blackBlob.splatHandler;
			blob.splatDecider = new InteractionHandler() {
                @Override public final boolean onInteract(final Enemy enemy, final Player player) {
                	enemy.timer++;
                	final boolean ret;
                	if (enemy.timer > 1 && enemy.def == blob) {
                		ret = false;
                	} else {
                	    ret = player == null || !player.isDragonStomping();
                	}
                	if (ret) {
                	    soundBounce.startSound();
                	}
                    return ret;
                }};
            blackBlob.splatDecider = blob.splatDecider;
			Coltil.set(allEnemies, Level.BLOB, blob);
			final Panple rockO = new FinPanple2(8, 2);
			Enemy.currentO = rockO;
			rockSprite = new EnemyDefinition("Rock Sprite", 15, null, false, false, 0, Enemy.DEFAULT_X, Enemy.DEFAULT_H, 2);
			rockSprite.stepHandler = new InteractionHandler() {
                @Override public final boolean onInteract(final Enemy enemy, final Player player) {
                	if (enemy.isGrounded()) {
                	    if (enemy.timer < 0) {
                	        enemy.initHv();
                	        enemy.initTimer(0);
                	        return true;
                	    } else if (enemy.timer == 0) {
                			enemy.facePlayers();
                			enemy.initTimer(0);
                			enemy.v = Mathtil.randi(6, 8);
                			soundJump.startSound();
                			return false;
                		}
                		enemy.timer--;
                		return true;
                	}
                	return false;
                }};
            rockSprite.stompHandler = new InteractionHandler() {
                @Override public final boolean onInteract(final Enemy enemy, final Player player) {
                    return enemy.timer < 0;
                }};
			Coltil.set(allEnemies, Level.ROCK_SPRITE, rockSprite);
			Enemy.currentWalkAnm = rockSprite.walk;
			rockTrio = new EnemyDefinition("Rock Walker", 15, 3, Enemy.trioFactory);
			rockTrio.rewardHandler = new InteractionHandler() {
                @Override public final boolean onInteract(final Enemy enemy, final Player player) {
                    return false;
                }};
            rockTrio.menu = new TrioEnemyMenu();
			Coltil.set(allEnemies, Level.ROCK_TRIO, rockTrio);
			rockLeg = new EnemyDefinition("Rock Leg", 16, null, false, false, 0, Enemy.DEFAULT_X, Enemy.DEFAULT_H, 3);
			rockLeg.rewardHandler = rockTrio.rewardHandler;
			rockBack = createImage("rock.back", RES + "enemy/Enemy17.png", 16, rockO, Enemy.DEFAULT_MIN, Enemy.DEFAULT_MAX);
			Level.initTheme(); }});
		
		loaders.add(new Runnable() { @Override public final void run() {
			final Panmage[] owls = createSheet("owl", RES + "chr/Owl.png", 32);
			final int owlBlink = DUR_BLINK + 30;
			final Panframe owl1 = engine.createFrame(PRE_FRM + "owl.1", owls[0], owlBlink - DUR_CLOSED);
			final Panframe owl2 = engine.createFrame(PRE_FRM + "owl.2", owls[1], DUR_CLOSED);
			owl = engine.createAnimation(PRE_ANM + "owl", owl1, owl2); }});
		
		loaders.add(new Runnable() { @Override public final void run() {
		    bees = ImtilX.loadStrip(RES + "chr/misc/Bee.png");
		    bee = getBirdAnm("bee", KIND_BEE, 2);
			frozen = createImage("frozen", RES + "chr/Frozen.png", 32, og);
			burn = createAnm("burn", RES + "chr/Burn.png", 32, 6, og, null, null);
			electric = createAnm("electric", RES + "chr/Electric.png", 32, 3, og, null, null);
			doubleFx = createSparkAnm("fx.double", "misc/DoubleFx.png");
			lightningOrb = createImage("orb.lightning", RES + "misc/LightningOrb.png", 16);
			doubleOrb = createImage("orb.double", RES + "misc/DoubleOrb.png", 16);
			blockPower = createImage("block.power", RES + "misc/BlockPower.png", 16);
			bubble = createImage("bubble", RES + "chr/Bubble.png", 32, og);
			minecart = createAnm("minecart", RES + "misc/Minecart.png", 32, 2, new FinPanple2(16, 7), null, null); }});
	    
		loaders.add(new Runnable() { @Override public final void run() {
			font = Fonts.getClassics(new FontRequest(8), Pancolor.WHITE, Pancolor.BLACK); }});
		loaders.add(new Runnable() { @Override public final void run() {
			fontTiny = Fonts.getTiny(FontType.Upper, Pancolor.WHITE); }});
	    
		loaders.add(new Runnable() { @Override public final void run() {
			block8 = createImage("block8", RES + "misc/Block8.png", 8);
			blockLetter8 = createImage("block.letter8", RES + "misc/BlockLetter8.png", 8);
			blockIce8 = createImage("block.ice8", RES + "misc/BlockIce8.png", 8);
			vineShatter = createImage("vine.shatter", RES + "misc/VineShatter.png", 8);
			final FinPanple2 origSpike = new FinPanple2(ORIG_X_SPIKE, 0);
			shakingSpike = engine.createImage(PRE_IMG + "shaking.spike", origSpike, null, null, RES + "misc/ShakingSpike.png");
			fallingSpike = engine.createImage(PRE_IMG + "falling.spike", origSpike, null, null, RES + "misc/FallingSpike.png"); }});
	    
		loaders.add(new Runnable() { @Override public final void run() {
		    final PixelFilter gemCyanFilter = new SwapPixelFilter(Channel.Green, Channel.Red, Channel.Blue);
		    final PixelFilter gemBlueFilter = new SwapPixelFilter(Channel.Red, Channel.Red, Channel.Blue);
		    final PixelFilter gemGreenFilter = new SwapPixelFilter(Channel.Red, Channel.Blue, Channel.Red);
		    final PixelFilter gemWhiteFilter = new SwapPixelFilter(Channel.Red, Channel.Red, Channel.Blue);
		    final Img[] gemStrip = ImtilX.loadStrip(RES + "misc/Gem.png");
		    Img.setTemporary(false, gemStrip);
		    final Img gem1 = Imtil.copy(gemStrip[0]);
		    gem = createSheet("gem", null, gemStrip);
		    gemAnm = createGemAnm("gem", gem);
		    gemShatter = createImage("gem.shatter", RES + "misc/GemShatter.png", 8);
		    gemCyanAnm = createGemAnm("cyan", gemStrip, gemCyanFilter);
		    gemBlue = createGemSheet("blue", gemStrip, gemBlueFilter);
		    gemBlueAnm = createGemAnm("gem.blue", gemBlue);
		    gemGreenAnm = createGemAnm("green", gemStrip, gemGreenFilter);
		    gemWhite = engine.createImage(PRE_IMG + "gem.white", Imtil.filter(gem1, gemWhiteFilter));
		    Img.close(gemStrip);
		    gemLevelAnm = createGemAnm("gem.level", createSheet("gem.level", null, ImtilX.loadStrip(RES + "misc/Gem5.png")));
		    final Img[] worldStrip = ImtilX.loadStrip(RES + "misc/Gem4.png");
		    Img.setTemporary(false, worldStrip);
		    final Img[] worldCopy = Imtil.copy(worldStrip);
		    gemWorldAnm = createGemAnm("gem.world", createSheet("gem.world", null, worldStrip));
		    gemCyanWorldAnm = createGemAnm("cyan.world", worldStrip, gemCyanFilter);
            gemBlueWorldAnm = createGemAnm("blue.world", worldStrip, gemBlueFilter);
            gemGreenWorldAnm = createGemAnm("green.world", worldStrip, gemGreenFilter);
            gemWhiteWorldAnm = createGemAnm("white.world", worldCopy, gemWhiteFilter);
		    Img.close(worldStrip);
		    gemWordAnm = createGemAnm("gem.word", createSheet("gem.word", null, ImtilX.loadStrip(RES + "misc/Gem6.png")));
		    gemLetters = createSheet("gem.letter", null, ImtilX.loadStrip(RES + "misc/GemLetters.png"));
		    final Img[] blStrip = loadBlockLetterStrip();
		    blockLetters = createSheet("block.letter", null, blStrip);
		    for (final Img bl : blStrip) {
		    	Imtil.setPseudoTranslucent(bl);
		    }
		    translucentBlockLetters = createSheet("translucent.block.letter", null, blStrip);
		    Img.close(blStrip);
		    gemGoal = createSheet("gem.goal", null, ImtilX.loadStrip(RES + "misc/GemStar.png"));
		    emptyGoal = createImage("empty.goal", RES + "misc/EmptyStar.png", ImtilX.DIM);
		    final Img[] orbStrip = ImtilX.loadStrip(RES + "misc/GemOrb.png");
		    Img.setTemporary(false, orbStrip);
		    final Img[] orbCopy = Imtil.copy(orbStrip);
		    gemRank = createSheet("gem.rank", null, orbStrip);
		    gemCyanRank = createGemSheet("cyan.rank", orbStrip, gemCyanFilter);
            gemBlueRank = createGemSheet("blue.rank", orbStrip, gemBlueFilter);
            gemGreenRank = createGemSheet("green.rank", orbStrip, gemGreenFilter);
            gemWhiteRank = createGemSheet("white.rank", orbCopy, gemWhiteFilter);
		    gemAchieve = createSheet("gem.achieve", null, ImtilX.loadStrip(RES + "misc/GemTrophy.png"));
		    Img.close(orbStrip); }});
	    
		loaders.add(new Runnable() { @Override public final void run() {
		    spark = createSparkAnm("spark", "misc/Spark.png");
		    Spark.class.getClass(); // Force class load? Save time later?
		    puff = createAnm("puff", RES + "misc/Puff.png", 8, 3, CENTER_8); }});
	    
		loaders.add(new Runnable() { @Override public final void run() {
			teleport = createAnm("teleport", RES + "enemy/Teleport.png", ImtilX.DIM, 5, Enemy.DEFAULT_O); }});
	    
		loaders.add(new Runnable() { @Override public final void run() {
			final FinPanple2 mo = new FinPanple2(-4, -4);
		    final Panmage[] ma = createSheet("Marker", RES + "bg/Marker.png", 8, mo);
			final Panframe[] fa = new Panframe[ma.length];
			for (int i = 0; i < ma.length; i++) {
				fa[i] = engine.createFrame(PRE_FRM + "marker." + i, ma[i], 2 * (2 - i % 2));
			}
			marker = engine.createAnimation(PRE_ANM + "marker", fa);
			final Img def = ImtilX.loadImage(RES + "bg/MarkerDefeated.png", 8, null);
			markerDefeated = engine.createImage(PRE_IMG + "Marker.def", mo, null, null, def); }});
		loaders.add(new Runnable() { @Override public final void run() {
			portal = createAnm("portal", RES + "bg/Portal.png", 6); }});
		loaders.add(new Runnable() { @Override public final void run() {
			portalClosed = createAnm("portal.closed", RES + "bg/PortalClosed.png", 15); }});
		
		loaders.add(new Runnable() { @Override public final void run() {
			dirts = Imtil.loadStrip(RES + "bg/Dirt.png", ImtilX.DIM);
			Img.setTemporary(false, dirts); }});
		loaders.add(new Runnable() { @Override public final void run() {
			terrains = Imtil.loadStrip(RES + "bg/Terrain.png", ImtilX.DIM);
			Img.setTemporary(false, terrains); }});
		loaders.add(new Runnable() { @Override public final void run() {
			crowns = ImtilX.loadStrip(RES + "chr/Crowns.png", 14, false);
			Img.setTemporary(false, crowns); }});
		
		loaders.add(new Runnable() { @Override public final void run() {
			TitleScreen.generateTitleCharacters(); }});
		
		if (engine.isMouseSupported()) {
			loaders.add(new Runnable() { @Override public final void run() {
				final Img icn = ImtilX.loadImage(RES + "menu/Cursor.png", false);
				menuCursor = Pangine.getEngine().createImage(Pantil.vmid(), new FinPanple2(0, 15), null, null, icn);
				icn.close(); }});
		}
		
		if (engine.isTouchSupported() || engine.isMouseSupported()) {
			btnLoader = new Runnable() { @Override public final void run() {
				// 400 x 240
				DIM_BUTTON = getButtonSize(Config.btnSize);
				final int d = DIM_BUTTON;
				final Pancolor f = new FinPancolor((short) 160, Mathtil.SHORT_0, Pancolor.MAX_VALUE);
				final Img circle = Imtil.newImage(d, d);
				Imtil.drawCircle(circle, Pancolor.BLACK, Pancolor.BLACK, f);
				ImtilX.highlight(circle, 2);
				final Img circleIn = ImtilX.indent(circle);
				Imtil.setPseudoTranslucent(circle);
				Imtil.setPseudoTranslucent(circleIn);
				button = engine.createImage(Pantil.vmid(), circle);
				buttonIn = engine.createImage(Pantil.vmid(), circleIn);
				final Img r2 = ImtilX.newArrow2(d, Pancolor.BLACK, Pancolor.BLACK, f, true, true);
				ImtilX.highlight(r2, 1);
				final Img r2In = ImtilX.indent(r2);
				Imtil.setPseudoTranslucent(r2);
				Imtil.setPseudoTranslucent(r2In);
				r2.setTemporary(false);
				r2In.setTemporary(false);
				right2 = engine.createImage(Pantil.vmid(), r2);
				right2In = engine.createImage(Pantil.vmid(), r2In);
				Imtil.mirror(r2);
				Imtil.mirror(r2In);
				left2 = engine.createImage(Pantil.vmid(), r2);
				left2In = engine.createImage(Pantil.vmid(), r2In);
				r2.close();
				r2In.close();
				final Panmage[] diamonds = getDiamonds(d, f);
				diamond = diamonds[0];
		        diamondIn = diamonds[1];
				}};
			loaders.add(btnLoader);
			loaders.add(new Runnable() { @Override public final void run() {
			    final Pancolor clrBtn = new FinPancolor((short) 160, (short) 192, (short) 224);
			    final Pancolor clrIn = new FinPancolor((short) 128, (short) 224, (short) 255);
			    menu = engine.createImage(Pantil.vmid(), ImtilX.newButton(MENU_W, MENU_H, clrBtn));
			    menuCheck = createMenuImg("Check");
			    menuX = createMenuImg("X");
			    menuPlus = createMenuImg("Plus");
			    menuMinus = createMenuImg("Minus");
			    menuExclaim = createMenuImg("Exclaim");
			    menuQuestion = createMenuImg("Question");
			    menuOff = createMenuImg("Off");
			    menuTrophy = gemAchieve[0];
			    menuStar = gemGoal[0];
			    menuGraph = createMenuImg("Graph");
			    menuInfo = createMenuImg("Info");
			    menuFoes = createMenuImg("Foes");
			    menuPause = createMenuImg("Pause");
			    menuOptions = createMenuImg("Options");
			    menuMenu = createMenuImg("Menu");
			    menuMusic = createMenuImg("Music");
			    menuSound = createMenuImg("Sound");
			    menuDifficulty = createMenuImg("Difficulty");
			    menuDefeat = createMenuImg("Defeat");
			    menuWorld = createMenuImg("World");
			    menuAvatar = createMenuImg("Avatar");
			    menuColor = createMenuImg("Color");
			    menuAnimal = createMenuImg("Animal");
			    menuEyes = createMenuImg("Eyes");
			    menuGear = createMenuImg("Gear");
			    menuClothing = createMenuImg("Clothing");
			    menuHat = createMenuImg("Hats");
			    menuJump = createMenuImg("Jump");
			    menuBird = createMenuImg("Bird");
			    menuKeyboard = createMenuImg("Keyboard");
			    menuRgb = createMenuImg("Rgb");
			    menuEyesDragon = createMenuImg("EyesDragon");
			    menuButtons = createMenuImg("Buttons");
			    menuGames = createMenuImg("Games");
			    //menuIn = engine.createImage(Pantil.vmid(), ImtilX.indent(left));
			    menuIn = engine.createImage(Pantil.vmid(), ImtilX.newButton(MENU_W, MENU_H, clrIn));
			    menuDisabled = engine.createImage(Pantil.vmid(), ImtilX.newButton(MENU_W, MENU_H, new FinPancolor((short) 128, (short) 96, (short) 160)));
			    menuLeft = createMenuImg("Left");
			    menuRight = createMenuImg("Right");
			    menuUp = createMenuImg("Up");
			    menuDown = createMenuImg("Down");
			    redUp = createMenuImg("UpRed");
			    redDown = createMenuImg("DownRed");
			    greenUp = createMenuImg("UpGreen");
			    greenDown = createMenuImg("DownGreen");
			    final int keyW = TouchKeyboard.getMaxKeyWidth();
			    key = engine.createImage(Pantil.vmid(), ImtilX.newButton(keyW, keyW, clrBtn));
			    keyIn = engine.createImage(Pantil.vmid(), ImtilX.newButton(keyW, keyW, clrIn));
//info("loadConstants end " + System.currentTimeMillis());
			    }});
		}
		
	    loaders.add(new Runnable() { @Override public final void run() {
	    	final Panaudio audio = engine.getAudio();
	    	audio.ensureCapacity(6);
	    	musicMenu = audio.createMusic(RES + "music/menu.mid");
	    	musicHappy = audio.createMusic(RES + "music/happy.mid");
	    	musicSnow = audio.createMusic(RES + "music/snow.mid");
	    	musicSand = audio.createMusic(RES + "music/sand.mid");
	    	musicRock = audio.createMusic(RES + "music/rock.mid");
	    	musicHive = audio.createMusic(RES + "music/hive.mid");
	    	musicJungle = audio.createMusic(RES + "music/jungle.mid");
	    	musicBridge = audio.createMusic(RES + "music/bridge.mid");
	    	musicCave = audio.createMusic(RES + "music/cave.mid");
	    	musicNight = audio.createMusic(RES + "music/night.mid");
	    	musicHeartbeat = audio.createMusic(RES + "music/heartbeat.mid");
	    	musicOcarina = audio.createMusic(RES + "music/ocarina.mid");
	    	musicMinecart = audio.createMusic(RES + "music/minecart.mid");
	    	musicChant = audio.createMusic(RES + "music/chant.mid");
	    	musicLevelStart = audio.createTransition(RES + "music/levelstart.mid");
	    	musicLevelEnd = audio.createTransition(RES + "music/levelend.mid");
	    	soundGem = audio.createSound(RES + "sound/gem.mid");
	    	soundJump = audio.createSound(RES + "sound/jump.mid");
	    	soundBounce = audio.createSound(RES + "sound/bounce.mid");
	    	soundThud = audio.createSound(RES + "sound/thud.mid");
	    	soundCrumble = audio.createSound(RES + "sound/crumble.mid");
	    	soundArmor = audio.createSound(RES + "sound/armor.mid");
	    	soundWhoosh = audio.createSound(RES + "sound/whoosh.mid");
	    	bounceBall.wallSound = soundArmor;
	    	armoredImp.stompSound = soundArmor;
	    	rockSprite.stompSound = soundArmor;
	    	rockTrio.stompSound = soundArmor;
	    	rockLeg.stompSound = soundArmor;
	    	}});
	    
	    Achievement.validateAchievements();
	}
	
	protected final static Img[] loadBlockLetterStrip() {
	    final Img[] blStrip = ImtilX.loadStrip(RES + "misc/BlockLetters.png");
	    Img.setTemporary(false, blStrip);
	    return blStrip;
	}
	
	protected final static void reloadButtons() {
	    Panmage.destroyAll(button, buttonIn, right2, right2In, left2, left2In, diamond, diamondIn);
	    btnLoader.run();
	}
	
	protected final static void openArmoredImp(final Enemy enemyPos, final Enemy enemyDir) {
	    enemyPos.burst(armoredImp.splat, enemyDir, new BurstHandler() {
            @Override public final void onBurst(final CustomBurst burst) {
                new Enemy(armoredImp, burst).setEnemyMirror(enemyDir.isMirror()); }});
	    enemyPos.destroy();
	}
	
	private final static Panmage createMenuImg(final String name) {
		final Img icn = ImtilX.loadImage(RES + "menu/" + name + ".png", false);
		final Panmage img = Pangine.getEngine().createImage(Pantil.vmid(), icn);
		icn.close();
		return img;
	}
	
	private final static Panimation createSparkAnm(final String name, final String loc) {
	    final Panframe[] sa = createFrames(name, RES + loc, 8, 1, CENTER_8);
        return Pangine.getEngine().createAnimation(PRE_ANM + name, sa[0], sa[1], sa[2], sa[3], sa[2], sa[1], sa[0]);
	}
	
	private final static Panimation createGemAnm(final String name, final Panmage[] gem) {
		final Pangine engine = Pangine.getEngine();
		return engine.createAnimation(PRE_ANM + name, engine.createFrame(PRE_FRM + name + ".0", gem[0], 3), engine.createFrame(PRE_FRM + name + ".1", gem[1], 1), engine.createFrame(PRE_FRM + name + ".2", gem[2], 1));
	}
	
	private final static Panmage[] createGemSheet(final String col, final Img[] strip, final PixelFilter gemFilter) {
        for (int i = 0; i < 3; i++) {
        	final Img oldImg = strip[i], newImg = Imtil.filter(oldImg, gemFilter);
            oldImg.close();
            newImg.setTemporary(false);
            strip[i] = newImg;
        }
        return createSheet("gem." + col, null, strip);
	}
	
	private final static Panimation createGemAnm(final String col, final Img[] strip, final PixelFilter gemFilter) {
        return createGemAnm("gem." + col, createGemSheet(col, strip, gemFilter));
	}
	
	protected final static Panimation getGemAnm(final Panmage img) {
	    if (Panmage.contains(gemAnm, img)) {
	        return gemAnm;
	    } else if (Panmage.contains(gemBlueAnm, img)) {
            return gemBlueAnm;
        }
	    return null;
	}
	
	private final static void loadLevel() {
		initTouchButtons(null, true, true, null); // Must define inputs before creating Player
	    Level.loadLevel();
	    addHud(room, true, true);
	    Tiles.initLetters();
	}
	
	protected final static Cursor addCursor(final Panlayer room) {
        return Cursor.addCursor(room, menuCursor);
    }
	
	protected final static Panlayer addHud(final Panroom room, final boolean allowAuto, final boolean level) {
		hud = createHud(room);
		final int h = Pangine.getEngine().getEffectiveTop() - 17;
		hudGem = addHudGem(hud, 0, h);
        final int size = pcs.size();
        for (int i = 0; i < size; i++) {
        	addHud(hud, pcs.get(i), OFF_GEM + (i * 56), h, level, true);
        }
        addNotifications(hud);
        if (level) {
        	initTouchButtons(hud, allowAuto, false, hudGem); // Must define actors after creating layer
        }
        return hud;
	}
	
	protected final static void initTouchButtons(final Panlayer layer, final boolean allowAuto, final boolean input, final Panctor bound) {
	    final PlayerContext pc = pcs.get(0);
		PlayerScreen.initTouchButtons(layer, pc.ctrl, (allowAuto && pc.isAutoRunEnabled()) ? Menu.TOUCH_JUMP : Menu.TOUCH_HORIZONTAL, input, !input, bound);
	}
	
	protected final static Gem addHudGem(final Panlayer hud, final int x, final int y) {
		final Gem hudGem = new Gem();
        hudGem.getPosition().set(x, y);
        hud.addActor(hudGem);
        return hudGem;
	}
	
	protected final static void addHud(final Panlayer hud, final PlayerContext pc, final int x, final int y, final boolean level, final boolean mult) {
        final CallSequence gemSeq;
        if (level) {
            gemSeq = new CallSequence() {@Override protected String call() {
            	// pc.player can be null when one player is in a bonus level while other players wait
                return pc.player == null ? "0" : String.valueOf(pc.player.getCurrentLevelGems());}};
        } else {
            gemSeq = new CallSequence() {@Override protected String call() {
            	int gems = pc.getGems(), temp = pc.tempGems;
            	if (temp >= 0) {
            		int off = (gems - temp) / 200;
            		if (off == 0) {
            			if (gems > temp) {
            				off = 1;
            			} else if (gems < temp) {
            				off = -1;
            			}
            		}
            		gems = temp + off;
            		pc.tempGems = gems == temp ? -1 : gems;
            	}
                return String.valueOf(gems);}};
        }
        final int i = pc.index;
        final String name = pc.getName();
        if (!Menu.NAME_NEW.equals(name)) {
	        final Pantext hudName = new Pantext("hud.name." + i, font, name);
	        hudName.getPosition().set(x, y + 8);
	        hud.addActor(hudName);
        }
        final Pantext hudGems = new Pantext("hud.gems." + i, font, gemSeq);
        hudGems.getPosition().set(x, y);
        hud.addActor(hudGems);
        if (mult) {
            final CallSequence multSeq = new CallSequence() {@Override protected String call() {
                final int m = pc.getGemMultiplier();
                return (m > 1) ? ("x" + m) : "";}};
            final Pantext hudMult = new Pantext("hud.mult." + i, fontTiny, multSeq);
            hudMult.getPosition().set(x, y - 7);
            hud.addActor(hudMult);
        }
	}
	
	protected final static void addNotifications(final Panlayer layer) {
		if (notifications == null || notifications.isDestroyed()) {
			notifications = new Notifications(layer, font);
			notifications.setDestroyAllowed(false);
			notifications.getLabel().getPosition().set(8, Pangine.getEngine().getEffectiveTop() - 25);
		} else {
			layer.addActor(notifications);
		}
	}
	
	protected final static void setPosition(final Panctor act, final float x, final float y, final float depth) {
		//TODO If "- y" works, only use it in level
		//still need to put in character/wing/spring/bubble/onStep as y changes
		//onStep will need to know base depth
	    //act.getPosition().set(x, y, Level.tm.getForegroundDepth() - y + depth);
		act.getPosition().set(x, y, (Level.tm == null) ? 0 : Level.tm.getForegroundDepth() + depth);
	}
	
	protected final static void setDepth(final Panctor act, final float depth) {
		act.getPosition().setZ(Level.tm.getForegroundDepth() + depth);
	}
	
	protected final static void setPosition(final Player player, final float x, final float y) {
		setPosition(player, x, y, getDepthPlayer(player.jumpMode));
	}
	
	protected final static int getDepthPlayer(final byte jumpMode) {
		return _DEPTH_PLAYER + getDepthPlayerOffset(jumpMode);
	}
	
	protected final static int getDepthPlayerBack(final byte jumpMode) {
		return _DEPTH_PLAYER_BACK + getDepthPlayerOffset(jumpMode);
	}
	
	protected final static int getDepthBubble(final byte jumpMode) {
		return _DEPTH_BUBBLE + getDepthPlayerOffset(jumpMode);
	}
	
	protected final static int getDepthContainer(final byte jumpMode) {
		return _DEPTH_CONTAINER + getDepthPlayerOffset(jumpMode);
	}
	
	private final static int getDepthPlayerOffset(final byte jumpMode) {
		return (jumpMode == Player.JUMP_DRAGON) ? DEPTH_OFF_DRAGON : 0;
	}
	
	protected final static void levelVictory() {
	    for (final Panctor actor : room.getActors()) {
	        // Trio is sub-class of Enemy, so check for it first
	        // Wisp can't be bumped; Trio turns into Sprite when bumped
	        if (actor instanceof Wisp || actor instanceof Trio || actor instanceof Leg || actor instanceof Projectile) {
                Gem.spark(actor.getPosition(), false);
                actor.destroy();
            } else if (actor instanceof Enemy) {
                ((Enemy) actor).flipAndFall(Player.VEL_BUMP); // onBump(null);
            //} else if (actor instanceof Gem) {
            //    ((Gem) actor).spark(); // No longer objects
            }
        }
	    if (Coltil.size(Level.collectedLetters) == blockWord.length()) {
	        clearLetters(gemBlueAnm, null);
	    }
	    Level.victory = true;
	}
	
	private final static void showLetterBonusGems(final Panimation anm) {
		final Pangine engine = Pangine.getEngine();
		final int x = (engine.getEffectiveWidth() - ImtilX.DIM * 5) / 2, y = engine.getEffectiveTop() - 96;
		for (int i = 0; i < 5; i++) {
			final int xc = x + i * ImtilX.DIM, yc = y + 12 * Math.abs(2 - i);
			new GemBumped(hud, null, xc, yc, 0, GemBumped.TYPE_DECORATION, anm, Tiles.g);
			if (i == 2 && anm == gemBlueAnm) {
				new GemBumped(hud, null, xc, yc + 32, 0, GemBumped.TYPE_DECORATION, gemWordAnm, Tiles.g);
			}
		}
	}
	
	protected final static void shatterLetter(final Panple pos) {
		Tiles.shatterTile(Pantil.nvl(hud, room), blockLetter8, pos, false);
	}
	
	protected final static Panmage getGemLetter(final Object blockLetterImg) {
		return gemLetters[getLetterIndex(blockLetters, (Panmage) blockLetterImg)];
	}
	
	protected final static void clearLetters(final Panimation anm, final Runnable finishHandler) {
	    Pangine.getEngine().addTimer(Level.tm, 8, new TimerListener() {
            @Override public final void onTimer(final TimerEvent event) {
                final int i = Level.collectedLetters.size() - 1;
                final Panctor letter = Level.collectedLetters.remove(i);
                final Panple pos = letter.getPosition();
                final GemBumped gem;
                gem = new GemBumped(hud, null, pos.getX(), pos.getY() - ImtilX.DIM, 0, GemBumped.TYPE_DECORATION, null, FinPanple.ORIGIN);
                gem.getVelocity().setY(0);
                gem.setView(getGemLetter(letter.getView()));
                shatterLetter(pos);
                letter.destroy();
                Level.currLetter--;
                if (i > 0) {
                    clearLetters(anm, finishHandler);
                } else {
                	Pangine.getEngine().addTimer(Level.tm, 8, new TimerListener() {
                		@Override public final void onTimer(final TimerEvent event) {
                			showLetterBonusGems(anm);
                		}});
                	if (finishHandler == null) {
	                    for (final PlayerContext pc : pcs) {
	                        pc.player.addGems(50);
	                    }
	                } else {
	                	finishHandler.run();
	                }
                }
            }});
	}
	
	protected final static void levelClose() {
	    for (final PlayerContext pc : pcs) {
	        pc.onFinishLevel();
	    }
	    markerClose();
	}
	
	protected final static void markerClose() {
		Level.initTheme();
	    Achievement.evaluate();
	    if (Map.isOnLastLevel()) {
	    	Map.victory = Map.VICTORY_WORLD;
	        fadeOut(room, new Castle.ThroneWinScreen());
	    } else {
	    	Map.victory = Map.VICTORY_LEVEL;
	        goMap();
	    }
	}
	
	protected final static void worldClose() {
	    // Should possibly only call this for first Player; other Players could skip worlds when they play solo
		for (final PlayerContext pc : pcs) {
			pc.onFinishWorld();
		}
		Achievement.evaluate();
	}
	
	protected final static void saveGame() {
	    // Map must call after updating markers (at least for player 1 who is bound to that Map)
	    for (final PlayerContext pc : pcs) {
            pc.profile.save();
        }
	}
	
	protected final static void goGoals(final PlayerContext pc) {
		InfoScreen.currentTab = InfoScreen.TAB_GOALS;
		fadeOut(room, new InfoScreen(pc, false));
	}
	
	protected final static void goMiniGames(final PlayerContext pc) {
	    fadeOut(room, new MiniGamesScreen(pc));
	}
	
	protected final static boolean goGoalsIfNeeded() {
		for (final PlayerContext pc : pcs) {
			if (Goal.isAnyMet(pc)) {
				goGoals(pc);
				return true;
			}
		}
		return false;
	}
	
	protected final static void goMap() {
		goMap(SPEED_FADE);
	}
	
	protected final static void goMap(final short speed) {
        fadeOut(room, speed, new Map.MapScreen());
	}
	
	protected final static Panroom createRoom(final int w, final int h) {
	    if (room != null) {
	    	Panctor.detach(notifications);
	        room.destroy();
	    }
	    room = Pangine.getEngine().createRoom(Pantil.vmid(), w, h, 0);
	    Pangame.getGame().setCurrentRoom(room);
	    return room;
	}
	
	protected final static PlayerContext getPlayerContext(final String name) {
		for (final PlayerContext pc : pcs) {
			if (name.equals(pc.profile.getName())) {
				return pc;
			}
		}
		return null;
	}
	
	protected final static Panmage getImageLetter(final Panmage[] letters, final char c) {
		return letters[(c < 'A' || c > 'Z') ? 26 : (c - 'A')];
	}
	
	protected final static Panmage getImageWordLetter(final Panmage[] letters, final int i) {
        return getImageLetter(letters, blockWord.charAt(i));
    }
	
	protected final static int getLetterIndex(final Panmage[] letters, final Panmage img) {
		final int size = letters.length;
		for (int i = 0; i < size; i++) {
			if (img == letters[i]) {
				return i;
			}
		}
		throw new IllegalStateException("Could not find index for letter image " + img);
	}
	
	protected final static Panmage getBlockLetter(final char c) {
		return getImageLetter(blockLetters, c);
	}
	
	protected final static Panmage getBlockWordLetter(final int i) {
		return getBlockLetter(blockWord.charAt(i));
	}
	
	protected final static Panmage getGemLetter(final char c) {
		return getImageLetter(gemLetters, c);
	}
	
	protected final static Panmage getGemWordLetter(final int i) {
		return getGemLetter(blockWord.charAt(i));
	}
	
	protected final static Panmage getTranslucentBlockLetter(final char c) {
        return getImageLetter(translucentBlockLetters, c);
    }
	
	protected final static Panmage getTranslucentBlockWordLetter(final int i) {
		return getTranslucentBlockLetter(blockWord.charAt(i));
	}
	
	protected final static List<String> getAvailableProfiles() {
		final List<String> list = new ArrayList<String>();
		final int extLen = EXT_PRF.length();
		for (final String f : Iotil.list()) {
			if (f.endsWith(EXT_PRF)) {
				/*
				// PRUNE PROFILES MADE DURING DEBUGGING; TEMPORARY
				if (EXT_PRF.equals(f) || ("null" + EXT_PRF).equals(f)) {
					Iotil.delete(f);
					continue;
				}
				*/
				final String prf = f.substring(0, f.length() - extLen);
				if (getPlayerContext(prf) == null) {
					list.add(prf);
				}
			}
		}
		return list;
	}
	
	protected final static List<String> getAnimals() {
		return animals;
	}
	
	protected final static List<String> getBasicAnimals() {
		if (basicAnimals == null) {
			final List<String> allAnimals = getAnimals();
			basicAnimals = new ArrayList<String>(allAnimals.size() - Avatar.SPECIAL_ANIMALS.size());
			for (final String anm : allAnimals) {
				if (!Avatar.SPECIAL_ANIMALS.containsKey(anm)) {
					basicAnimals.add(anm);
				}
			}
		}
		return basicAnimals;
	}
	
	protected final static int getNumEyes() {
	    return 28;
	}
	
	protected final static int getNumDragonEyes() {
	    return 3;
	}
	
	protected final static EnemyDefinition getEnemy(final String name) {
	    for (final EnemyDefinition def : allEnemies) {
	        if (def.getName().equals(name)) {
	            return def;
	        }
	    }
	    return null;
	}
	
	protected final static void playMenuMusic() {
		if (Pangine.getEngine().getAudio().getMusic() == musicChant) {
			return;
		}
		(Map.theme == null ? musicMenu : Map.theme.getMenuMusic()).changeMusic();
	}
	
	protected final static void playTransition(final Pansound music) {
		Pangine.getEngine().getAudio().stopMusic();
    	music.startSound();
	}
	
	@Override
	public boolean onPause() {
		if (Pangine.getEngine().isPaused()) {
			return true;
		} else if (Panscreen.get() instanceof PlatformScreen && hud != null) {
			PlayerScreen.promptQuit(hud);
			return true;
		}
		return false;
	}
	
	protected final static boolean isMultiTouchSupported() {
	    final Pangine engine = Pangine.getEngine();
	    return engine.isMultiTouchSupported() || engine.isMouseSupported();
	}
	
	public final static void main(final String[] args) {
        try {
            new FurGuardiansGame().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
