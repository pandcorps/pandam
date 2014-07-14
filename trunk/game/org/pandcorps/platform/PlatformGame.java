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
package org.pandcorps.platform;

import java.io.*;
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
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Fonts.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.touch.*;
import org.pandcorps.pandax.visual.*;
import org.pandcorps.platform.Avatar.*;
import org.pandcorps.platform.Enemy.*;
import org.pandcorps.platform.Level.*;
import org.pandcorps.platform.Player.*;

public class PlatformGame extends BaseGame {
	/*
	Horse/hippo/elephant/squirrel/gator/pig/walrus/beaver/stag/bull/ram player face.
	Player shirts.
	Ghost trail, particle trail.
	Spin-float when hold jump while falling, 2xjump, inf flip-jump.
	Player sliding image.
	Warp Map marker for entry/exit point.
	Replace bush with Rise.png for some levels; rise will be higher than 1 tile; separate build method.
	Taller bushes.
	Map landmarks: Mountain, garden.
	Train-riding levels.
	Ridable dragons.
	Enemy Wisp, Elementals, Impix (winged Imp).
	Gargoyles catch/carry Player, like moving platforms, one can jump to/from them, but not run on them.
	Cannons on ground that Player enters to be launched.
	Cannons in air that auto-fire, others that wait for jump input.
	Maximum effective (per tick) velocity & higher absolute max; set current to max effective before evaluating.
	Horizontal acceleration.
	Options (in/out game) - track active/p1
	Exit game from within Level.
	Font w/ custom chars for custom string/min square/case space if needed.
	Colored player names.
	Bump w/o break ceiling block.
	Bounce/blow floor.
	Spike/fire floor tile.
	Spike/fire enemy.
	Collect fruit from trees.
	Level to-do notes.
	Goals: Collect n gems, defeat n enemies.
	Random music per map.
	Sound effects for jump, bump, stomp, hurt, etc.
	Automatically advance on Map if standing on defeated Level and there is only one adjacent undefeated Level.
	Option to auto-run, only one button for jumping.
	Flags to simplify menu in some environments (one Profile, maybe one Avatar).
	Let LogoScreen finish after a specified Runnable finishes.
	Let Thread keep loading through title screen.
	Give images a real transparent background, disable ImtilX preprocessing.
	Assist: Berserker? (Defeat Enemy by touching it)
	Assist: Teleport (After fall, teleport to target immediately instead of slow bubble, no gem loss)
	Remove Gem class? Just use Tile.foreground and Tile.animate?
	Clear TileMap.map/imgs?
	Static TileMaps should combine adjacent Tiles with adjacent TileMapImages into one larger Tile/TileMapImage.
	Flash block main image is wrong.
	New Level generator for town background; nearly constant ground height so houses always look right.
	Enemy no longer throws Projectile.
	Stats for touch menu.
	Wing color touch menu.
	Frame rate menu.
	Wing gravity tweak.
	BounceBall vertical velocity and bounce multipler tweak.
	Level builder should use setTile instead of forcing getTile Map lookups.
	Add TM/C.
	Improve World name generator.
	Disable million gem bonus.
	A kicked BounceBall should give Gems to Player that kicked it when it defeats an Enemy.
	A BounceBall should be able to bump blocks (from below and side) and give Gem to Player that kicked.
	Names? Hob-troll, Hob-ogre, Pixy-imp?
	*/
	
	protected final static byte TILE_BREAK = 2;
	protected final static byte TILE_BUMP = 3;
	protected final static byte TILE_FLOOR = 4;
    protected final static byte TILE_UPSLOPE = 5;
    protected final static byte TILE_DOWNSLOPE = 6;
    protected final static byte TILE_UPSLOPE_FLOOR = 7;
    protected final static byte TILE_DOWNSLOPE_FLOOR = 8;
	
	//protected final static int DEPTH_POWERUP = 0;
	protected final static int DEPTH_ENEMY = 4;
	protected final static int DEPTH_PLAYER_BACK = 1;
	protected final static int DEPTH_PLAYER = 2;
	protected final static int DEPTH_BUBBLE = 3;
	protected final static int DEPTH_SHATTER = 5;
	protected final static int DEPTH_SPARK = 6;
	
	protected final static int OFF_GEM = 16;
	
	protected final static int TIME_FLASH = 60;
	
	private final static FinPanple ORIG_MAP = new FinPanple(8, -6, 0);
	private final static int DUR_MAP = 6;
	protected final static int DUR_BLINK = 120;
	protected final static int DUR_CLOSED = DUR_BLINK / 30;
	
	protected final static short SPEED_FADE = 6;
	
	protected final static int MAX_NAME_PROFILE = 8;
	protected final static int MAX_NAME_AVATAR = 8;
	
	protected final static String FILE_CFG = "Config.txt";
	protected final static String EXT_PRF = ".prf.txt";
	protected final static String EXT_MAP = ".map.txt";
	
	protected final static String SEG_CFG = "CFG";
	protected final static String SEG_PRF = "PRF";
	protected final static String SEG_STX = "STX";
	protected final static String SEG_ACH = "ACH";
	protected final static String SEG_LOC = "LOC";
	protected final static String SEG_AVT = "AVT";
	
	protected static Panroom room = null;
	protected final static ArrayList<PlayerContext> pcs = new ArrayList<PlayerContext>();
	protected static MultiFont font = null;
	protected static Font fontTiny = null;
	protected static Notifications notifications = null;
	protected final static FinPanple og = new FinPanple(16, 1, 0);
	protected final static FinPanple ow = new FinPanple(17, 1, 0);
	protected final static FinPanple owf = new FinPanple(17, 2, 0);
	protected final static FinPanple os = new FinPanple(16, 11, 0);
	protected static Img[] guysBlank = null;
	protected final static HashMap<String, Img> facesAll = new HashMap<String, Img>();
	protected final static HashMap<String, Img[]> tailsAll = new HashMap<String, Img[]>();
	protected final static Img[] eyesAll = new Img[getNumEyes()];
	protected static Img eyesBlink = null;
	protected static Panmage bubble = null;
	protected static Panimation owl = null;
	protected final static List<EnemyDefinition> allEnemies = new ArrayList<EnemyDefinition>();
	protected static List<EnemyDefinition> enemies = null;
	protected static EnemyDefinition imp = null;
	protected static EnemyDefinition armoredImp = null;
	protected static Panimation anger = null;
	protected static Panmage block8 = null;
	protected static Panmage[] gem = null;
	protected static Panimation gemAnm = null;
	protected static Panimation gemBlueAnm = null;
	protected static Panimation gemCyanAnm = null;
	protected static Panimation gemGreenAnm = null;
	protected static Panmage gemWhite = null;
	protected static Panimation gemLevelAnm = null;
	protected static Panimation gemWorldAnm = null;
	protected static Panmage gemShatter = null;
	protected static Panimation spark = null;
	protected static Panimation teleport = null;
	protected static Panimation projectile1 = null;
	protected static Panimation marker = null;
	protected static Panmage markerDefeated = null;
	protected static Panimation portal = null;
	protected static Panimation portalClosed = null;
	protected static Img[] dirts = null;
	protected static Img[] terrains = null;
	protected static Img[] crowns = null;
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
	protected static Panmage menuOff = null;
	protected static Panmage menuMenu = null;
	protected static Panmage menuAvatar = null;
	protected static Panmage menuColor = null;
	protected static Panmage menuAnimal = null;
	protected static Panmage menuEyes = null;
	protected static Panmage menuGear = null;
	protected static Panmage menuKeyboard = null;
	protected static Panmage redUp = null;
	protected static Panmage redDown = null;
	protected static Panmage greenUp = null;
	protected static Panmage greenDown = null;
	protected static Panmage key = null;
	protected static Panmage keyIn = null;
	protected static Queue<Runnable> loaders = new LinkedList<Runnable>();
	
	@Override
	protected final boolean isFullScreen() {
        return true;
    }
	
	@Override
	protected final void init(final Panroom room) throws Exception {
	    Pangine.getEngine().setTitle("Platformer");
		//engine.setFrameRate(60);
		PlatformGame.room = room;
		loadConstants();
		Panscreen.set(new LogoScreen(Menu.TitleScreen.class, loaders));
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
		Notifications.fadeOut(notifications, layer, Pancolor.MIN_VALUE, Pancolor.MIN_VALUE, Pancolor.MIN_VALUE, speed, screen, true);
	}
	
	protected final static void notify(final Named n, final String msg) {
		notifications.enqueue(n.getName() + ": " + msg);
	}
	
	protected final static class PlatformScreen extends Panscreen {
		@Override
        protected final void load() throws Exception {
			loadLevel();
			fadeIn(room);
			final Pangine engine = Pangine.getEngine();
			if (engine.isMusicSupported()) {
				engine.getMusic().playMusic(Music.newSongCreepy());
			}
		}
		
		@Override
        protected final void step() {
            if ((Pangine.getEngine().getClock() % TIME_FLASH) < 4) {
                Tile.animate(Level.flashBlock);
            }
		}
		
		@Override
	    protected final void destroy() {
			final Pangine engine = Pangine.getEngine();
			if (engine.isMusicSupported()) {
				engine.getMusic().stop();
			}
	        Panmage.destroy(Level.timg);
	        Panmage.destroy(Level.bgimg);
	    }
	}
	
	private final static Img[] loadChrStrip(final String name, final int dim, final PixelFilter f) {
		return loadChrStrip(name, dim, f, true);
	}
	
	private final static Img[] loadChrStrip(final String name, final int dim, final boolean req) {
		final String fileName = "org/pandcorps/platform/res/chr/" + name;
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
		if (f != null) {
			final int size = in.length;
			for (int i = 0; i < size; i++) {
				out[i] = Imtil.filter(in[i], f);
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
	
	private final static void buildGuy(final Img guy, final Img face, final Img[] tails, final Img eyes, final int y, final int t) {
	    Imtil.copy(face, guy, 0, 0, 18, 18, 8, 1 + y, Imtil.COPY_FOREGROUND);
        Imtil.copy(eyes, guy, 0, 0, 8, 4, 15, 10 + y, Imtil.COPY_FOREGROUND);
        if (tails != null) {
            Imtil.copy(tails[0], guy, 0, 0, 12, 12, t, 20 + y - t, Imtil.COPY_BACKGROUND);
        }
	}
	
	private final static PixelFilter getFilter(final SimpleColor col) {
		float r = col.r, g = col.g, b = col.b;
	    if (r == 0 && g == 0 && b == 0) {
	    	r = g = b = 0.09375f;
	    }
	    return new MultiplyPixelFilter(Channel.Blue, r, Channel.Blue, g, Channel.Blue, b);
	}
	
	protected final static class PlayerImages {
		protected final PixelFilter f;
		protected final Img[] guys;
		protected final Img guyBlink;
		protected final Img face;
		protected final Img[] tails;
		protected final Img eyes;
	
		protected PlayerImages(final Avatar avatar) {
		    f = getFilter(avatar.col);
			guys = new Img[guysBlank.length];
			filterStrip(guysBlank, guys, f);
			guyBlink = Imtil.copy(guys[0]);
			final String anm = avatar.anm;
			Img faceRaw = facesAll.get(anm);
			if (faceRaw == null) {
				faceRaw = ImtilX.loadImage("org/pandcorps/platform/res/chr/Face" + anm + ".png", false);
				faceRaw.setTemporary(false);
				facesAll.put(anm, faceRaw);
			}
			face = Imtil.filter(faceRaw, f);
			Img[] tailsRaw = tailsAll.get(anm);
			if (tailsRaw == null && !tailsAll.containsKey(anm)) {
				tailsRaw = loadChrStrip("Tail" + anm + ".png", 12, false);
				Img.setTemporary(false, tailsRaw);
				tailsAll.put(anm, tailsRaw);
			}
			if (tailsRaw == null) {
				tails = null;
			} else {
				tails = new Img[tailsRaw.length];
				filterStrip(tailsRaw, tails, f);
			}
			Img e = eyesAll[avatar.eye - 1];
			if (e == null) {
			    e = ImtilX.loadImage("org/pandcorps/platform/res/chr/Eyes0" + avatar.eye + ".png", false);
			    eyesAll[avatar.eye - 1] = e;
			}
			eyes = e;
			final int size = guys.length;
			for (int i = 0; i < size; i++) {
				buildGuy(guys[i], face, tails, eyes, (i == 3) ? -1 : 0, (i < 3) ? i : 1);
			}
			buildGuy(guyBlink, face, tails, eyesBlink, 0, 0);
		}
		
		protected final void close() {
			Img.close(guys);
			Img.close(tails);
			Img.close(guyBlink, face);
		}
	}
	
	protected final static void reloadAnimalStrip(final PlayerContext pc) {
		reloadAnimalStrip(pc, true);
	}
	
	protected final static void reloadAnimalStrip(final PlayerContext pc, final boolean full) {
		pc.destroy();
		final Profile profile = pc.profile;
	    final Avatar avatar = profile.currentAvatar;
	    final String anm = avatar.anm;
	    final PlayerImages pi = new PlayerImages(avatar);
	    final Img guys[] = pi.guys, tails[] = pi.tails, eyes = pi.eyes;
		final String pre = "guy." + pc.index;
		
		final Pangine engine = Pangine.getEngine();
		final FinPanple ng = new FinPanple(-Player.PLAYER_X, 0, 0), xg = new FinPanple(Player.PLAYER_X, Player.PLAYER_H, 0);
		final String ipre = PRE_IMG + pre + ".";
		final Panmage guy = engine.createImage(ipre + "1", og, ng, xg, guys[0]);
		final Panmage guyB = engine.createImage(ipre + "blink", og, ng, xg, pi.guyBlink);
		final String fpre = PRE_FRM + pre + ".";
		final String spre = fpre + "still.";
		final Panframe gfs1 = engine.createFrame(spre + "1", guy, DUR_BLINK - DUR_CLOSED), gfs2 = engine.createFrame(spre + "2", guyB, DUR_CLOSED);
		pc.guy = engine.createAnimation(PRE_ANM + pre + ".still", gfs1, gfs2);
		
		final boolean needWing = avatar.jumpMode == Player.JUMP_FLY;
		final PixelFilter pf = needWing ? getFilter(avatar.jumpCol) : null;
		
		if (full) {
			final Panmage guy2 = engine.createImage(ipre + "2", og, ng, xg, guys[1]);
			final Panmage guy3 = engine.createImage(ipre + "3", og, ng, xg, guys[2]);
			final String rpre = fpre + "run.";
			final Panframe gfr1 = engine.createFrame(rpre + "1", guy, 2), gfr2 = engine.createFrame(rpre + "2", guy2, 2), gfr3 = engine.createFrame(rpre + "3", guy3, 2);
			pc.guyRun = engine.createAnimation(PRE_ANM + pre + ".run", gfr2, gfr3, gfr1);
			pc.guyJump = engine.createImage(ipre + "jump", og, ng, xg, guys[3]);
			pc.guyFall = engine.createImage(ipre + "fall", og, ng, xg, guys[4]);
		    //guy = engine.createImage(pre, new FinPanple(8, 0, 0), null, null, ImtilX.loadImage("org/pandcorps/platform/res/chr/Player.png"));
		    
			final Img[] maps = loadChrStrip("BearMap.png", 32, pi.f);
			final Img[] wingMap = needWing ? loadChrStrip("WingsMap.png", 32, pf) : null;
			final Img[] faceMap = loadChrStrip("FaceMap" + anm + ".png", 18, pi.f);
			final Img south1 = maps[0], southPose = maps[5], faceSouth = faceMap[0];
			if (needWing) {
				for (final Img south : new Img[] {south1, southPose}) {
					Imtil.copy(wingMap[0], south, 0, 0, 32, 32, 0, 0, Imtil.COPY_BACKGROUND);
				}
			}
			final Img south2 = Imtil.copy(south1);
			Imtil.mirror(south2);
			for (final Img south : new Img[] {south1, south2, southPose}) {
				Imtil.copy(faceSouth, south, 0, 0, 18, 18, 7, 5, Imtil.COPY_FOREGROUND);
				Imtil.copy(eyes, south, 0, 0, 8, 4, 12, 14, Imtil.COPY_FOREGROUND);
			}
			pc.mapSouth = createAnmMap(pre, "south", south1, south2);
	        pc.mapPose = engine.createImage(ipre + "map.pose", ORIG_MAP, null, null, southPose);
			final Img east1 = maps[1], east2 = maps[2], faceEast = faceMap[1];
			final Img[] easts = {east1, east2};
			for (final Img east : easts) {
				if (needWing) {
					Imtil.copy(wingMap[1], east, 0, 0, 32, 32, 0, 0, Imtil.COPY_BACKGROUND);
				}
				Imtil.copy(faceEast, east, 0, 0, 18, 18, 7, 5, Imtil.COPY_FOREGROUND);
				if (tails != null) {
					Imtil.copy(tails[1], east, 0, 0, 12, 12, 1, 20, Imtil.COPY_BACKGROUND);
				}
			}
			final Img west1 = Imtil.copy(east1), west2 = Imtil.copy(east2);
			final Img eyesEast = eyes.getSubimage(0, 0, 4, 4);
			for (final Img east : easts) {
				Imtil.copy(eyesEast, east, 0, 0, 4, 4, 18, 14, Imtil.COPY_FOREGROUND);
			}
			eyesEast.close();
			pc.mapEast = createAnmMap(pre, "east", east1, east2);
			Imtil.mirror(west1);
			Imtil.mirror(west2);
			final Img eyesWest = eyes.getSubimage(4, 0, 4, 4);
			for (final Img west : new Img[] {west1, west2}) {
				Imtil.copy(eyesWest, west, 0, 0, 4, 4, 10, 14, Imtil.COPY_FOREGROUND);
			}
			eyesWest.close();
			pc.mapWest = createAnmMap(pre, "west", west1, west2);
			final Img tailNorth = Coltil.get(tails, 2), faceNorth = faceMap[2];
			final Img wing = needWing ? wingMap[0] : null;
			pc.mapNorth = createNorth(maps, 3, wing, tailNorth, faceNorth, pre, "North");
			pc.mapLadder = createNorth(maps, 4, wing, tailNorth, faceNorth, pre, "Ladder");
			
			Img.close(maps);
			Img.close(wingMap);
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
		} else if (full && avatar.jumpMode == Player.JUMP_HIGH) {
		    pc.backJump = createAnm(pre + ".spring", "org/pandcorps/platform/res/chr/Springs.png", 32, 5, os, ng, xg);
		}
		
		pi.close();
	}
	
	private final static Panimation createNorth(final Img[] maps, final int mi, final Img wing, final Img tailNorth, final Img faceNorth,
	                                            final String pre, final String suf) {
		final Img north1 = maps[mi];
		if (tailNorth != null) {
			Imtil.copy(tailNorth, north1, 0, 0, 12, 12, 10, 20, Imtil.COPY_FOREGROUND);
		}
		if (wing != null) {
			Imtil.copy(wing, north1, 0, 0, 32, 32, 0, 0, Imtil.COPY_FOREGROUND);
		}
		final Img north2 = Imtil.copy(north1);
		Imtil.mirror(north2);
		for (final Img north : new Img[] {north1, north2}) {
			Imtil.copy(faceNorth, north, 0, 0, 18, 18, 7, 5, Imtil.COPY_FOREGROUND);
		}
		return createAnmMap(pre, suf, north1, north2);
	}
	
	private final static Panimation createAnmMap(final String pre, final String suf, final Img... a) {
		return createAnm(pre + ".map." + suf.toLowerCase(), DUR_MAP, ORIG_MAP, a);
	}
	
	private final static void replace(final ReplacePixelFilter f, final short r, final short g, final short b) {
		f.put(r, g, b, Pancolor.MAX_VALUE, r, b, g, Pancolor.MAX_VALUE);
	}
	
	protected final static Profile loadProfile(final String pname, final ControlScheme ctrl, final int index) throws Exception {
		final SegmentStream plist = SegmentStream.openLocation(pname + EXT_PRF);
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
        final String curName = seg.getValue(1);
        seg = plist.readIf(SEG_STX);
        if (seg != null) {
        	profile.stats.load(seg);
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
        profile.currentAvatar = profile.getAvatar(curName);
        //profile.ctrl = 0;
		createAnimalStrip(profile, ctrl, index);
		//profile.serialize("temptemp.txt");
		//createAnimalStrip("Grabbit", "Rabbit", 2, new MultiplyPixelFilter(Channel.Blue, 0f, Channel.Blue, 1f, Channel.Blue, 0.25f), 1);
		//createAnimalStrip("Roddy", "Mouse", 3, new SwapPixelFilter(Channel.Blue, Channel.Red, Channel.Blue), 0);
		//createAnimalStrip("Felip", "Cat", 4, new SwapPixelFilter(Channel.Red, Channel.Red, Channel.Blue), 0);
		return profile;
	}
	
	private final static void loadConstants() throws Exception {
		final Pangine engine = Pangine.getEngine();
		// SegmentStream.readLocation creates a file containing "CFG|" if it doesn't exist
		final Segment cfg = SegmentStream.readLocation(FILE_CFG, "CFG|").get(0);
		// CFG|Andrew
		Config.defaultProfileName = cfg.getValue(0);
		
		loaders.add(new Runnable() { @Override public final void run() {
		    guysBlank = loadChrStrip("Bear.png", 32, true);
		    Img.setTemporary(false, guysBlank);
		    eyesBlink = ImtilX.loadImage("org/pandcorps/platform/res/chr/EyesBlink.png", false); }});
		
		loaders.add(new Runnable() { @Override public final void run() {
System.out.println("loadConstants start " + System.currentTimeMillis());
			allEnemies.add(new EnemyDefinition("Drowid", 1, null, true, 1)); }}); // Teleport when stomped
		loaders.add(new Runnable() { @Override public final void run() {
			final EnemyDefinition drolock = new EnemyDefinition("Drolock", 4, null, false, 0, 0);
			drolock.projectile = projectile1;
			allEnemies.add(drolock); }}); // Teleport/shoot periodically
		loaders.add(new Runnable() { @Override public final void run() {
			allEnemies.add(new EnemyDefinition("Troblin", 2, null, true)); }});
		loaders.add(new Runnable() { @Override public final void run() {
			final ReplacePixelFilter f = new ReplacePixelFilter();
			replace(f, (short) 104, (short) 120, (short) 172);
			replace(f, (short) 80, (short) 96, (short) 144);
			replace(f, (short) 64, (short) 80, (short) 112);
			replace(f, (short) 48, (short) 56, (short) 80);
			allEnemies.add(new EnemyDefinition("Obglin", 2, f, false));
			final int impX = 4, impH = 14;
			imp = new EnemyDefinition("Imp", 3, null, true, true, impX, impH);
			allEnemies.add(imp);
			final EnemyDefinition troll, ogre;
			troll = new EnemyDefinition("Troll", 5, null, true, false, 0, 8, 30, 1, 32);
			troll.stompHandler = new InteractionHandler() {
                @Override public final boolean onInteract(final Enemy enemy, final Player player) {
                	if (Math.abs(enemy.hv) <= 1) {
                		enemy.hv *= 2;
                		enemy.timer = 3;
                		enemy.burst(anger, enemy, null, 36);
                		return true;
                	} else if (enemy.timer > 0) {
                		return true;
                	}
                    return false;
                }};
            troll.stepHandler = new InteractionHandler() {
                @Override public final boolean onInteract(final Enemy enemy, final Player player) {
                	if (enemy.timer > 0) {
                		enemy.timer--;
                	}
                    return false;
                }};
			allEnemies.add(troll);
			ogre = new EnemyDefinition("Ogre", 5, f, false, false, 0, 8, 30, 1, 32);
			ogre.stompHandler = troll.stompHandler;
			ogre.stepHandler = troll.stepHandler;
			allEnemies.add(ogre);
			final EnemyDefinition armorBall, bounceBall, thrownImp;
			armorBall = new EnemyDefinition("Armor Ball", 7, null, false, 0, 0);
			bounceBall = new EnemyDefinition("Bounce Ball", 7, null, false, 0, 4);
			Enemy.currentSplat = 8;
			armoredImp = new EnemyDefinition("Armored Imp", 6, null, true, true, Enemy.DEFAULT_X, Enemy.DEFAULT_H);
			thrownImp = new EnemyDefinition("Thrown Imp", 8, null, false, false, 0, impX, impH, 10);
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
					return true;
				}};
			armorBall.rewardHandler = new InteractionHandler() {
				@Override public final boolean onInteract(final Enemy enemy, final Player player) {
					return enemy.full;
				}};
			armorBall.hurtHandler = new InteractionHandler() {
                @Override public final boolean onInteract(final Enemy enemy, final Player player) {
                    final Enemy e = new BounceBall(bounceBall, enemy);
                    e.full = enemy.full;
                    e.setEnemyMirror(player.isMirror());
                    e.v = 6;
                    enemy.destroy();
                    return false;
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
                    enemy.v = Math.abs(enemy.v) * 0.8f;
                    return true;
                }};
            bounceBall.stompHandler = new InteractionHandler() {
                @Override public final boolean onInteract(final Enemy enemy, final Player player) {
                    final Enemy ball = new ArmorBall(armorBall, enemy);
                    ball.full = enemy.full;
                    ball.setMirror(enemy.isMirror());
                    enemy.destroy();
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
			allEnemies.add(armoredImp);
			anger = createAnm("anger", 10, CENTER_16, Enemy.loadStrip(9, ImtilX.DIM));
			Level.setTheme(Theme.Normal); }});
		
		loaders.add(new Runnable() { @Override public final void run() {
			final Panmage[] owls = createSheet("owl", "org/pandcorps/platform/res/chr/Owl.png", 32);
			final int owlBlink = DUR_BLINK + 30;
			final Panframe owl1 = engine.createFrame(PRE_FRM + "owl.1", owls[0], owlBlink - DUR_CLOSED);
			final Panframe owl2 = engine.createFrame(PRE_FRM + "owl.2", owls[1], DUR_CLOSED);
			owl = engine.createAnimation(PRE_ANM + "owl", owl1, owl2); }});
		
		loaders.add(new Runnable() { @Override public final void run() {
			bubble = createImage("bubble", "org/pandcorps/platform/res/chr/Bubble.png", 32, og); }});
	    
		loaders.add(new Runnable() { @Override public final void run() {
			font = Fonts.getClassics(new FontRequest(8), Pancolor.WHITE, Pancolor.BLACK); }});
		loaders.add(new Runnable() { @Override public final void run() {
			fontTiny = Fonts.getTiny(FontType.Upper, Pancolor.WHITE); }});
	    
		loaders.add(new Runnable() { @Override public final void run() {
			block8 = createImage("block8", "org/pandcorps/platform/res/misc/Block8.png", 8); }});
	    
		loaders.add(new Runnable() { @Override public final void run() {
		    final Img[] gemStrip = ImtilX.loadStrip("org/pandcorps/platform/res/misc/Gem.png");
		    Img.setTemporary(false, gemStrip);
		    final Img gem1 = Imtil.copy(gemStrip[0]);
		    gem = createSheet("gem", null, gemStrip);
		    gemAnm = createGemAnm("gem", gem);
		    gemShatter = createImage("gem.shatter", "org/pandcorps/platform/res/misc/GemShatter.png", 8);
		    gemCyanAnm = createGemAnm("cyan", gemStrip, Channel.Green, Channel.Red, Channel.Blue);
		    gemBlueAnm = createGemAnm("blue", gemStrip, Channel.Red, Channel.Red, Channel.Blue);
		    gemGreenAnm = createGemAnm("green", gemStrip, Channel.Red, Channel.Blue, Channel.Red);
		    gemWhite = engine.createImage(PRE_IMG + "gem.white", Imtil.filter(gem1, new SwapPixelFilter(Channel.Red, Channel.Red, Channel.Blue)));
		    Img.close(gemStrip); }});
		loaders.add(new Runnable() { @Override public final void run() {
		    gemLevelAnm = createGemAnm("gem.level", createSheet("gem.level", null, ImtilX.loadStrip("org/pandcorps/platform/res/misc/Gem5.png"))); }});
		loaders.add(new Runnable() { @Override public final void run() {
		    gemWorldAnm = createGemAnm("gem.world", createSheet("gem.world", null, ImtilX.loadStrip("org/pandcorps/platform/res/misc/Gem6.png"))); }});
	    
		loaders.add(new Runnable() { @Override public final void run() {
		    final Panframe[] sa = createFrames("spark", "org/pandcorps/platform/res/misc/Spark.png", 8, 1);
		    spark = engine.createAnimation(PRE_ANM + "spark", sa[0], sa[1], sa[2], sa[3], sa[2], sa[1], sa[0]);
		    Spark.class.getClass(); }}); // Force class load? Save time later?
	    
		loaders.add(new Runnable() { @Override public final void run() {
			teleport = createAnm("teleport", "org/pandcorps/platform/res/enemy/Teleport.png", ImtilX.DIM, 5, Enemy.DEFAULT_O); }});
	    
		loaders.add(new Runnable() { @Override public final void run() {
			final Panmage pimg1 = createImage("projectile1", "org/pandcorps/platform/res/enemy/Projectile1.png", 8, CENTER_8, new FinPanple(-3, -3, 0), new FinPanple(2, 2, 0));
		    final Panframe[] pfrms = new Panframe[4];
		    for (int i = 0; i < 4; i++) {
		        pfrms[i] = engine.createFrame(PRE_FRM + "projectile1." + i, pimg1, 4, i, false, false);
		    }
		    projectile1 = engine.createAnimation(PRE_ANM + "projectile1", pfrms); }});
	    
		loaders.add(new Runnable() { @Override public final void run() {
			final FinPanple mo = new FinPanple(-4, -4, 0);
		    final Panmage[] ma = createSheet("Marker", "org/pandcorps/platform/res/bg/Marker.png", 8, mo);
			final Panframe[] fa = new Panframe[ma.length];
			for (int i = 0; i < ma.length; i++) {
				fa[i] = engine.createFrame(PRE_FRM + "marker." + i, ma[i], 2 * (2 - i % 2));
			}
			marker = engine.createAnimation(PRE_ANM + "marker", fa);
			final Img[] defStrip = ImtilX.loadStrip("org/pandcorps/platform/res/bg/MarkerDefeated.png", 8);
			markerDefeated = engine.createImage(PRE_IMG + "Marker.def", mo, null, null, defStrip[3]);
			Img.close(defStrip); }});
		loaders.add(new Runnable() { @Override public final void run() {
			portal = createAnm("portal", "org/pandcorps/platform/res/bg/Portal.png", 6); }});
		loaders.add(new Runnable() { @Override public final void run() {
			portalClosed = createAnm("portal.closed", "org/pandcorps/platform/res/bg/PortalClosed.png", 15); }});
		
		loaders.add(new Runnable() { @Override public final void run() {
			dirts = Imtil.loadStrip("org/pandcorps/platform/res/bg/Dirt.png", ImtilX.DIM);
			Img.setTemporary(false, dirts); }});
		loaders.add(new Runnable() { @Override public final void run() {
			terrains = Imtil.loadStrip("org/pandcorps/platform/res/bg/Terrain.png", ImtilX.DIM);
			Img.setTemporary(false, terrains); }});
		loaders.add(new Runnable() { @Override public final void run() {
			crowns = ImtilX.loadStrip("org/pandcorps/platform/res/chr/Crowns.png", 14, false);
			Img.setTemporary(false, crowns); }});
		
		loaders.add(new Runnable() { @Override public final void run() {
			Menu.TitleScreen.generateTitleCharacters(); }});
		
		if (engine.isTouchSupported()) {
			loaders.add(new Runnable() { @Override public final void run() {
				// 400 x 240
				final int d = (Math.min(60 * engine.getEffectiveWidth() / 400, 60 * engine.getEffectiveHeight() / 240) / 2) * 2;
				final Pancolor f = new FinPancolor((short) 160, Mathtil.SHORT_0, Pancolor.MAX_VALUE);
				final Img circle = Imtil.newImage(d, d);
				Imtil.drawCircle(circle, Pancolor.WHITE, Pancolor.BLACK, f);
				final Img circleIn = ImtilX.indent(circle);
				Imtil.setPseudoTranslucent(circle);
				Imtil.setPseudoTranslucent(circleIn);
				button = engine.createImage(Pantil.vmid(), circle);
				buttonIn = engine.createImage(Pantil.vmid(), circleIn);
				final Img r2 = ImtilX.newRight2(d, f);
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
				final Img dia = Imtil.newImage(d, d);
                Imtil.drawDiamond(dia, Pancolor.WHITE, Pancolor.BLACK, f);
                final Img diaIn = ImtilX.indent(dia);
                Imtil.setPseudoTranslucent(dia);
                Imtil.setPseudoTranslucent(diaIn);
                diamond = engine.createImage(Pantil.vmid(), dia);
                diamondIn = engine.createImage(Pantil.vmid(), diaIn);
				}});
			loaders.add(new Runnable() { @Override public final void run() {
			    final int w = 48, h = 40, d = 28;
			    final Pancolor clrBtn = new FinPancolor((short) 160, (short) 192, (short) 224);
			    final Pancolor clrIn = new FinPancolor((short) 128, (short) 224, (short) 255);
			    menu = engine.createImage(Pantil.vmid(), ImtilX.newButton(w, h, clrBtn));
			    menuCheck = createMenuImg("Check");
			    menuX = createMenuImg("X");
			    menuPlus = createMenuImg("Plus");
			    menuMinus = createMenuImg("Minus");
			    menuOff = createMenuImg("Off");
			    menuMenu = createMenuImg("Menu");
			    menuAvatar = createMenuImg("Avatar");
			    menuColor = createMenuImg("Color");
			    menuAnimal = createMenuImg("Animal");
			    menuEyes = createMenuImg("Eyes");
			    menuGear = createMenuImg("Gear");
			    menuKeyboard = createMenuImg("Keyboard");
			    //menuIn = engine.createImage(Pantil.vmid(), ImtilX.indent(left));
			    menuIn = engine.createImage(Pantil.vmid(), ImtilX.newButton(w, h, clrIn));
			    menuDisabled = engine.createImage(Pantil.vmid(), ImtilX.newButton(w, h, new FinPancolor((short) 128, (short) 96, (short) 160)));
			    menuLeft = engine.createImage(Pantil.vmid(), ImtilX.newLeft2(d, Pancolor.BLUE));
			    menuRight = engine.createImage(Pantil.vmid(), ImtilX.newRight2(d, Pancolor.BLUE));
			    menuUp = engine.createImage(Pantil.vmid(), ImtilX.newUp2(d, Pancolor.BLUE));
			    menuDown = engine.createImage(Pantil.vmid(), ImtilX.newDown2(d, Pancolor.BLUE));
			    redUp = engine.createImage(Pantil.vmid(), ImtilX.newUp2(d, Pancolor.RED));
			    redDown = engine.createImage(Pantil.vmid(), ImtilX.newDown2(d, Pancolor.RED));
			    greenUp = engine.createImage(Pantil.vmid(), ImtilX.newUp2(d, Pancolor.GREEN));
			    greenDown = engine.createImage(Pantil.vmid(), ImtilX.newDown2(d, Pancolor.GREEN));
			    final int keyW = TouchKeyboard.getMaxKeyWidth();
			    key = engine.createImage(Pantil.vmid(), ImtilX.newButton(keyW, keyW, clrBtn));
			    keyIn = engine.createImage(Pantil.vmid(), ImtilX.newButton(keyW, keyW, clrIn));
System.out.println("loadConstants end " + System.currentTimeMillis());
			    }});
		}
		
		if (engine.isMusicSupported()) {
		    loaders.add(new Runnable() { @Override public final void run() {
		        engine.getMusic().ensureCapacity(5); }});
		}
	}
	
	protected final static void openArmoredImp(final Enemy enemyPos, final Enemy enemyDir) {
	    enemyPos.burst(armoredImp.splat, enemyDir, new BurstHandler() {
            @Override public final void onBurst(final CustomBurst burst) {
                new Enemy(armoredImp, burst).setEnemyMirror(enemyDir.isMirror()); }});
	    enemyPos.destroy();
	}
	
	private final static Panmage createMenuImg(final String name) {
		final Img icn = ImtilX.loadImage("org/pandcorps/platform/res/menu/" + name + ".png", false);
		final Panmage img = Pangine.getEngine().createImage(Pantil.vmid(), icn);
		icn.close();
		return img;
	}
	
	private final static Panimation createGemAnm(final String name, final Panmage[] gem) {
		final Pangine engine = Pangine.getEngine();
		return engine.createAnimation(PRE_ANM + name, engine.createFrame(PRE_FRM + name + ".0", gem[0], 3), engine.createFrame(PRE_FRM + name + ".1", gem[1], 1), engine.createFrame(PRE_FRM + name + ".2", gem[2], 1));
	}
	
	private final static Panimation createGemAnm(final String col, final Img[] strip, final Channel r, final Channel g, final Channel b) {
	    final SwapPixelFilter gemFilter = new SwapPixelFilter(r, g, b);
        for (int i = 0; i < 3; i++) {
        	final Img oldImg = strip[i], newImg = Imtil.filter(oldImg, gemFilter);
            oldImg.close();
            newImg.setTemporary(false);
            strip[i] = newImg;
        }
        final String name = "gem." + col;
        final Panmage[] gemCyan = createSheet(name, null, strip);
        return createGemAnm(name, gemCyan);
	}
	
	private final static void loadLevel() {
		initTouchButtons(null, true); // Must define inputs before creating Player
	    Level.loadLevel();
	    addHud(room, true);
	}
	
	protected final static Panlayer addHud(final Panroom room, final boolean level) {
		final Panlayer hud = createHud(room);
		final int h = Pangine.getEngine().getEffectiveHeight() - 17;
		addHudGem(hud, 0, h);
        final int size = pcs.size();
        for (int i = 0; i < size; i++) {
        	addHud(hud, pcs.get(i), OFF_GEM + (i * 56), h, level, true);
        }
        addNotifications(hud);
        if (level) {
        	initTouchButtons(hud, false); // Must define actors after creating layer
        }
        return hud;
	}
	
	protected final static void initTouchButtons(final Panlayer layer, final boolean input) {
		Menu.PlayerScreen.initTouchButtons(layer, pcs.get(0).ctrl, false, input, !input);
	}
	
	protected final static void addHudGem(final Panlayer hud, final int x, final int y) {
		final Gem hudGem = new Gem();
        hudGem.getPosition().set(x, y);
        hud.addActor(hudGem);
	}
	
	protected final static void addHud(final Panlayer hud, final PlayerContext pc, final int x, final int y, final boolean level, final boolean mult) {
        final CallSequence gemSeq;
        if (level) {
            gemSeq = new CallSequence() {@Override protected String call() {
            	// pc.player can be null when one player is in a bonus level while other players wait
                return pc.player == null ? "0" : String.valueOf(pc.player.getCurrentLevelGems());}};
        } else {
            gemSeq = new CallSequence() {@Override protected String call() {
                return String.valueOf(pc.getGems());}};
        }
        final int i = pc.index;
        final Pantext hudName = new Pantext("hud.name." + i, font, pc.getName());
        hudName.getPosition().set(x, y + 8);
        hud.addActor(hudName);
        final Pantext hudGems = new Pantext("hud.gems." + i, font, gemSeq);
        hudGems.getPosition().set(x, y);
        hud.addActor(hudGems);
        if (mult) {
            final int m = pc.profile.getGemMultiplier();
            if (m > 1) { // If multiplier only changes in Menu, can pre-store value
                final Pantext hudMult = new Pantext("hud.mult." + i, fontTiny, "x" + m);
                hudMult.getPosition().set(x, y - 7);
                hud.addActor(hudMult);
            }
        }
	}
	
	protected final static void addNotifications(final Panlayer layer) {
		if (notifications == null) {
			notifications = new Notifications(layer, font);
			notifications.getLabel().getPosition().set(8, Pangine.getEngine().getEffectiveHeight() - 25);
		} else {
			layer.addActor(notifications);
		}
	}
	
	protected static void setPosition(final Panctor act, final float x, final float y, final float depth) {
	    act.getPosition().set(x, y, Level.tm.getForegroundDepth() + depth);
	}
	
	protected final static void levelVictory() {
	    for (final Panctor actor : room.getActors()) {
            if (actor instanceof Enemy) {
                ((Enemy) actor).onBump(null);
            } else if (actor instanceof Gem) {
                ((Gem) actor).spark();
            }
        }
	}
	
	protected final static void levelClose() {
	    for (final PlayerContext pc : pcs) {
	        pc.onFinishLevel();
	    }
	    markerClose();
	}
	
	protected final static void markerClose() {
		Level.setTheme(Theme.Normal);
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
	
	protected final static void goMap() {
		goMap(SPEED_FADE);
	}
	
	protected final static void goMap(final short speed) {
        fadeOut(room, speed, new Map.MapScreen());
	}
	
	protected final static Panroom createRoom(final int w, final int h) {
	    if (room != null) {
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
	
	protected final static List<String> getAvailableProfiles() {
		final ArrayList<String> list = new ArrayList<String>();
		final int extLen = EXT_PRF.length();
		for (final String f : new File(".").list()) {
			if (f.endsWith(EXT_PRF)) {
				/*
				// PRUNE PROFILES MADE DURING DEBUGGING; TEMPORARY
				if (EXT_PRF.equals(f) || ("null" + EXT_PRF).equals(f)) {
					new File(f).delete();
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
	    return Arrays.asList("Bear", "Cat", "Dog", "Koala", "Mouse", "Rabbit", "Rhino");
	}
	
	protected final static int getNumEyes() {
	    return 8;
	}
	
	public final static void main(final String[] args) {
        try {
            new PlatformGame().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
