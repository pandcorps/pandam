/*
Copyright (c) 2009-2011, Andrew M. Martin
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

import java.awt.image.*;
import java.io.*;
import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.chr.*;
import org.pandcorps.core.img.*;
import org.pandcorps.core.img.Pancolor.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.game.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Fonts.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.visual.*;
import org.pandcorps.platform.Avatar.*;
import org.pandcorps.platform.Enemy.*;
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
	Bubble when leaving map.
	Portal near castle, source of monsters, must enter to close, another plane/dimension.
	Train-riding levels.
	Ridable dragons.
	Mage Enemy w/ pointy hat, Wisp, Elementals, Trolls, Ogres.
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
	Pangine pause, disable step/timers.
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
	protected static Panmage bubble = null;
	protected static Panimation owl = null;
	protected final static ArrayList<EnemyDefinition> enemies = new ArrayList<EnemyDefinition>();
	protected static Panmage block8 = null;
	protected static Panmage[] gem = null;
	protected static Panimation gemAnm = null;
	protected static Panimation gemBlueAnm = null;
	protected static Panimation gemCyanAnm = null;
	protected static Panimation gemGreenAnm = null;
	protected static Panmage gemWhite = null;
	protected static Panimation gemLevelAnm = null;
	protected static Panmage gemShatter = null;
	protected static Panimation spark = null;
	protected static final TileActor bump = new TileActor();
	protected static Panimation marker = null;
	protected static Panmage markerDefeated = null;
	protected static Panimation portal = null;
	protected static BufferedImage[] dirts = null;
	protected static BufferedImage[] terrains = null;
	
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
		Panscreen.set(new LogoScreen(Menu.TitleScreen.class));
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
			Pangine.getEngine().getMusic().playMusic(Music.newSongCreepy());
		}
		
		@Override
	    protected final void destroy() {
			Pangine.getEngine().getMusic().stop();
	        Panmage.destroy(Level.timg);
	        Panmage.destroy(Level.bgimg);
	    }
	}
	
	private final static BufferedImage[] loadChrStrip(final String name, final int dim, final PixelFilter f) {
		return loadChrStrip(name, dim, f, true);
	}
	
	private final static BufferedImage[] loadChrStrip(final String name, final int dim, final PixelFilter f, final boolean req) {
		final String fileName = "org/pandcorps/platform/res/chr/" + name;
		if (!(req || Iotil.exists(fileName))) {
			return null;
		}
		final BufferedImage[] strip = ImtilX.loadStrip(fileName, dim);
		if (f != null) {
			final int size = strip.length;
			for (int i = 0; i < size; i++) {
				strip[i] = Imtil.filter(strip[i], f);
			}
		}
		return strip;
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
	
	private final static void buildGuy(final BufferedImage guy, final BufferedImage face, final BufferedImage[] tails, final BufferedImage eyes, final int y, final int t) {
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
	
	protected final static void reloadAnimalStrip(final PlayerContext pc) {
		pc.destroy();
		final Profile profile = pc.profile;
	    final Avatar avatar = profile.currentAvatar;
	    final PixelFilter f = getFilter(avatar.col);
		final BufferedImage[] guys = loadChrStrip("Bear.png", 32, f);
		final BufferedImage guyBlink = Imtil.copy(guys[0]);
		final String anm = avatar.anm;
		final BufferedImage face = Imtil.filter(ImtilX.loadImage("org/pandcorps/platform/res/chr/Face" + anm + ".png", false), f);
		final BufferedImage[] tails = loadChrStrip("Tail" + anm + ".png", 12, f, false);
		final BufferedImage eyes = ImtilX.loadImage("org/pandcorps/platform/res/chr/Eyes0" + avatar.eye + ".png", false);
		final BufferedImage eyesBlink = ImtilX.loadImage("org/pandcorps/platform/res/chr/EyesBlink.png", false);
		final int size = guys.length;
		for (int i = 0; i < size; i++) {
			buildGuy(guys[i], face, tails, eyes, (i == 3) ? -1 : 0, (i < 3) ? i : 1);
		}
		buildGuy(guyBlink, face, tails, eyesBlink, 0, 0);
		final String pre = "guy." + pc.index;
		
		final Pangine engine = Pangine.getEngine();
		final FinPanple ng = new FinPanple(-Player.PLAYER_X, 0, 0), xg = new FinPanple(Player.PLAYER_X, Player.PLAYER_H, 0);
		final String ipre = PRE_IMG + pre + ".";
		final Panmage guy = engine.createImage(ipre + "1", og, ng, xg, guys[0]);
		final Panmage guyB = engine.createImage(ipre + "blink", og, ng, xg, guyBlink);
		final String fpre = PRE_FRM + pre + ".";
		final String spre = fpre + "still.";
		final Panframe gfs1 = engine.createFrame(spre + "1", guy, DUR_BLINK - DUR_CLOSED), gfs2 = engine.createFrame(spre + "2", guyB, DUR_CLOSED);
		pc.guy = engine.createAnimation(PRE_ANM + pre + ".still", gfs1, gfs2);
		final Panmage guy2 = engine.createImage(ipre + "2", og, ng, xg, guys[1]);
		final Panmage guy3 = engine.createImage(ipre + "3", og, ng, xg, guys[2]);
		final String rpre = fpre + "run.";
		final Panframe gfr1 = engine.createFrame(rpre + "1", guy, 2), gfr2 = engine.createFrame(rpre + "2", guy2, 2), gfr3 = engine.createFrame(rpre + "3", guy3, 2);
		pc.guyRun = engine.createAnimation(PRE_ANM + pre + ".run", gfr2, gfr3, gfr1);
		pc.guyJump = engine.createImage(ipre + "jump", og, ng, xg, guys[3]);
		pc.guyFall = engine.createImage(ipre + "fall", og, ng, xg, guys[4]);
	    //guy = engine.createImage(pre, new FinPanple(8, 0, 0), null, null, ImtilX.loadImage("org/pandcorps/platform/res/chr/Player.png"));
	    
		final BufferedImage[] maps = loadChrStrip("BearMap.png", 32, f);
		final boolean needWing = avatar.jumpMode == Player.JUMP_FLY;
		final PixelFilter pf;
		final BufferedImage[] wingMap;
		if (needWing) {
			pf = getFilter(avatar.jumpCol);
			wingMap = loadChrStrip("WingsMap.png", 32, pf);
		} else {
			pf = null;
			wingMap = null;
		}
		final BufferedImage[] faceMap = loadChrStrip("FaceMap" + anm + ".png", 18, f);
		final BufferedImage south1 = maps[0], southPose = maps[5], faceSouth = faceMap[0];
		if (needWing) {
			for (final BufferedImage south : new BufferedImage[] {south1, southPose}) {
				Imtil.copy(wingMap[0], south, 0, 0, 32, 32, 0, 0, Imtil.COPY_BACKGROUND);
			}
		}
		final BufferedImage south2 = Imtil.copy(south1);
		Imtil.mirror(south2);
		for (final BufferedImage south : new BufferedImage[] {south1, south2, southPose}) {
			Imtil.copy(faceSouth, south, 0, 0, 18, 18, 7, 5, Imtil.COPY_FOREGROUND);
			Imtil.copy(eyes, south, 0, 0, 8, 4, 12, 14, Imtil.COPY_FOREGROUND);
		}
		pc.mapSouth = createAnmMap(pre, "south", south1, south2);
        pc.mapPose = engine.createImage(ipre + "map.pose", ORIG_MAP, null, null, southPose);
		final BufferedImage east1 = maps[1], east2 = maps[2], faceEast = faceMap[1];
		final BufferedImage[] easts = {east1, east2};
		for (final BufferedImage east : easts) {
			if (needWing) {
				Imtil.copy(wingMap[1], east, 0, 0, 32, 32, 0, 0, Imtil.COPY_BACKGROUND);
			}
			Imtil.copy(faceEast, east, 0, 0, 18, 18, 7, 5, Imtil.COPY_FOREGROUND);
			if (tails != null) {
				Imtil.copy(tails[1], east, 0, 0, 12, 12, 1, 20, Imtil.COPY_BACKGROUND);
			}
		}
		final BufferedImage west1 = Imtil.copy(east1), west2 = Imtil.copy(east2);
		final BufferedImage eyesEast = eyes.getSubimage(0, 0, 4, 4);
		for (final BufferedImage east : easts) {
			Imtil.copy(eyesEast, east, 0, 0, 4, 4, 18, 14, Imtil.COPY_FOREGROUND);
		}
		pc.mapEast = createAnmMap(pre, "east", east1, east2);
		Imtil.mirror(west1);
		Imtil.mirror(west2);
		final BufferedImage eyesWest = eyes.getSubimage(4, 0, 4, 4);
		for (final BufferedImage west : new BufferedImage[] {west1, west2}) {
			Imtil.copy(eyesWest, west, 0, 0, 4, 4, 10, 14, Imtil.COPY_FOREGROUND);
		}
		pc.mapWest = createAnmMap(pre, "west", west1, west2);
		final BufferedImage tailNorth = Coltil.get(tails, 2), faceNorth = faceMap[2];
		final BufferedImage wing = needWing ? wingMap[0] : null;
		pc.mapNorth = createNorth(maps, 3, wing, tailNorth, faceNorth, pre, "North");
		pc.mapLadder = createNorth(maps, 4, wing, tailNorth, faceNorth, pre, "Ladder");
		
		if (needWing) {
		    final String wpre = pre + ".wing.";
		    final String iwpre = PRE_IMG + wpre;
		    final BufferedImage[] wings = loadChrStrip("Wings.png", 32, pf);
		    pc.back = engine.createImage(iwpre + "still", ow, ng, xg, wings[0]);
		    final String fwpre = PRE_FRM + wpre;
		    final Panframe[] frames = new Panframe[6];
		    for (int i = 0; i < 6; i++) {
			    final Panmage img = engine.createImage(iwpre + "fly." + i, i > 3 ? owf : ow, ng, xg, wings[i + 1]);
				frames[i] = engine.createFrame(fwpre + "fly." + i, img, (i == 0 || i == 3) ? 6 : 3);
		    }
		    pc.backJump = engine.createAnimation(PRE_ANM + wpre + ".fly", frames[1], frames[2], frames[3], frames[0]);
		    pc.backFall = engine.createAnimation(PRE_ANM + wpre + ".fall", frames[4], frames[5]);
		} else if (avatar.jumpMode == Player.JUMP_HIGH) {
		    pc.backJump = createAnm(pre + ".spring", "org/pandcorps/platform/res/chr/Springs.png", 32, 5, os, ng, xg);
		}
	}
	
	private final static Panimation createNorth(final BufferedImage[] maps, final int mi, final BufferedImage wing, final BufferedImage tailNorth, final BufferedImage faceNorth,
	                                            final String pre, final String suf) {
		final BufferedImage north1 = maps[mi];
		if (tailNorth != null) {
			Imtil.copy(tailNorth, north1, 0, 0, 12, 12, 10, 20, Imtil.COPY_FOREGROUND);
		}
		if (wing != null) {
			Imtil.copy(wing, north1, 0, 0, 32, 32, 0, 0, Imtil.COPY_FOREGROUND);
		}
		final BufferedImage north2 = Imtil.copy(north1);
		Imtil.mirror(north2);
		for (final BufferedImage north : new BufferedImage[] {north1, north2}) {
			Imtil.copy(faceNorth, north, 0, 0, 18, 18, 7, 5, Imtil.COPY_FOREGROUND);
		}
		return createAnmMap(pre, suf, north1, north2);
	}
	
	private final static Panimation createAnmMap(final String pre, final String suf, final BufferedImage... a) {
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
		final Segment cfg = SegmentStream.readLocation(FILE_CFG, "CFG|").get(0);
		// CFG|Andrew
		Config.defaultProfileName = cfg.getValue(0);
		
		enemies.add(new EnemyDefinition("Drowid", 1, null, true));
		enemies.add(new EnemyDefinition("Troblin", 2, null, false));
		final ReplacePixelFilter f = new ReplacePixelFilter();
		replace(f, (short) 104, (short) 120, (short) 172);
		replace(f, (short) 80, (short) 96, (short) 144);
		replace(f, (short) 64, (short) 80, (short) 112);
		replace(f, (short) 48, (short) 56, (short) 80);
		enemies.add(new EnemyDefinition("Obglin", 2, f, false));
		
		final Panmage[] owls = createSheet("owl", "org/pandcorps/platform/res/chr/Owl.png", 32);
		final int owlBlink = DUR_BLINK + 30;
		final Panframe owl1 = engine.createFrame(PRE_FRM + "owl.1", owls[0], owlBlink - DUR_CLOSED);
		final Panframe owl2 = engine.createFrame(PRE_FRM + "owl.2", owls[1], DUR_CLOSED);
		owl = engine.createAnimation(PRE_ANM + "owl", owl1, owl2);
		
		bubble = createImage("bubble", "org/pandcorps/platform/res/chr/Bubble.png", 32, og);
	    
	    font = Fonts.getClassics(new FontRequest(8), Pancolor.WHITE, Pancolor.BLACK);
	    fontTiny = Fonts.getTiny(FontType.Upper, Pancolor.WHITE);
	    
	    block8 = createImage("block8", "org/pandcorps/platform/res/misc/Block8.png", 8);
	    
	    final BufferedImage[] gemStrip = ImtilX.loadStrip("org/pandcorps/platform/res/misc/Gem.png");
	    final BufferedImage gem1 = Imtil.copy(gemStrip[0]);
	    gem = createSheet("gem", null, gemStrip);
	    gemAnm = createGemAnm("gem", gem);
	    gemShatter = createImage("gem.shatter", "org/pandcorps/platform/res/misc/GemShatter.png", 8);
	    gemCyanAnm = createGemAnm("cyan", gemStrip, Channel.Green, Channel.Red, Channel.Blue);
	    gemBlueAnm = createGemAnm("blue", gemStrip, Channel.Red, Channel.Red, Channel.Blue);
	    gemGreenAnm = createGemAnm("green", gemStrip, Channel.Red, Channel.Blue, Channel.Red);
	    gemWhite = engine.createImage(PRE_IMG + "gem.white", Imtil.filter(gem1, new SwapPixelFilter(Channel.Red, Channel.Red, Channel.Blue)));
	    gemLevelAnm = createGemAnm("gem.level", createSheet("gem.level", null, ImtilX.loadStrip("org/pandcorps/platform/res/misc/Gem4.png")));
	    
	    final Panframe[] sa = createFrames("spark", "org/pandcorps/platform/res/misc/Spark.png", 8, 1);
	    spark = engine.createAnimation(PRE_ANM + "spark", sa[0], sa[1], sa[2], sa[3], sa[2], sa[1], sa[0]);
	    Spark.class.getClass(); // Force class load? Save time later?
	    
	    final FinPanple mo = new FinPanple(-4, -4, 0);
	    final Panmage[] ma = createSheet("Marker", "org/pandcorps/platform/res/bg/Marker.png", 8, mo);
		final Panframe[] fa = new Panframe[ma.length];
		for (int i = 0; i < ma.length; i++) {
			fa[i] = engine.createFrame(PRE_FRM + "marker." + i, ma[i], 2 * (2 - i % 2));
		}
		marker = engine.createAnimation(PRE_ANM + "marker", fa);
		markerDefeated = engine.createImage(PRE_IMG + "Marker.def", mo, null, null, ImtilX.loadStrip("org/pandcorps/platform/res/bg/MarkerDefeated.png", 8)[3]);
		portal = createAnm("portal", "org/pandcorps/platform/res/bg/Portal.png", 6);
		
		dirts = Imtil.loadStrip("org/pandcorps/platform/res/bg/Dirt.png", ImtilX.DIM);
		terrains = Imtil.loadStrip("org/pandcorps/platform/res/bg/Terrain.png", ImtilX.DIM);
		
		engine.getMusic().ensureCapacity(5);
	}
	
	private final static Panimation createGemAnm(final String name, final Panmage[] gem) {
		final Pangine engine = Pangine.getEngine();
		return engine.createAnimation(PRE_ANM + name, engine.createFrame(PRE_FRM + name + ".0", gem[0], 3), engine.createFrame(PRE_FRM + name + ".1", gem[1], 1), engine.createFrame(PRE_FRM + name + ".2", gem[2], 1));
	}
	
	private final static Panimation createGemAnm(final String col, final BufferedImage[] strip, final Channel r, final Channel g, final Channel b) {
	    final SwapPixelFilter gemFilter = new SwapPixelFilter(r, g, b);
        for (int i = 0; i < 3; i++) {
            strip[i] = Imtil.filter(strip[i], gemFilter);
        }
        final String name = "gem." + col;
        final Panmage[] gemCyan = createSheet(name, null, strip);
        return createGemAnm(name, gemCyan);
	}
	
	private final static void loadLevel() {
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
        return hud;
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
	    Achievement.evaluate();
	    Map.victory = true;
	    goMap();
	}
	
	protected final static void worldClose() {
		for (final PlayerContext pc : PlatformGame.pcs) {
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
