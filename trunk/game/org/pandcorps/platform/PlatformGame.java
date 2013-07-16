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

import java.awt.image.BufferedImage;
import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.chr.CallSequence;
import org.pandcorps.core.img.*;
import org.pandcorps.core.img.Pancolor.Channel;
import org.pandcorps.game.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.FinPanple;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Fonts.FontRequest;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.visual.FadeController;
import org.pandcorps.platform.Enemy.EnemyDefinition;
import org.pandcorps.platform.Player.PlayerContext;

public class PlatformGame extends BaseGame {
	/*
	Dog/horse/hippo/rhino/elephant/squirrel/koala/gator/pig/walrus/beaver/stag/bull/ram player face.
	Player tail image for each species.
	Player shirts.
	Player falling/sliding images.
	Allow jumping a little above top of Level.
	Warp Map marker for entry/exit point.
	Ladder climb Map animation.
	Pick random mountain color/texture shape used by Map and Level whenever a new Map is generated.
	Replace bush with Rise.png for some levels; rise will be higher than 1 tile; separate build method.
	Taller bushes.
	Map landmarks: Mountain, forest, garden, crater.
	Bubble when map-traveling.
	Train-riding levels.
	Cannons that Player enters to be launched.
	Maximum effective (per tick) velocity & higher absolute max; set current to max effective before evaluating.
	Horizontal acceleration.
	Shatter Gem effect when hurt.
	Options (in/out game) - track active/p1
	Game supports multiple profiles each w/ multiple characters.
	Save/load.
	Font w/ custom chars for custom string/min square/case space if needed.
	Colored player names.
	Bump w/o break ceiling block.
	Bounce/blow floor.
	Spike/fire floor tile.
	Spike/fire enemy.
	Collect fruit from trees.
	Level to-do notes.
	Achievements: 1st level, 1st map, level w/ no damage.
	Goals: Collect n gems, defeat n enemies.
	Random music per map.
	*/
	
	protected final static byte TILE_BREAK = 2;
	protected final static byte TILE_BUMP = 3;
	protected final static byte TILE_FLOOR = 4;
    protected final static byte TILE_UPSLOPE = 5;
    protected final static byte TILE_DOWNSLOPE = 6;
    protected final static byte TILE_UPSLOPE_FLOOR = 7;
    protected final static byte TILE_DOWNSLOPE_FLOOR = 8;
	
	//protected final static int DEPTH_POWERUP = 0;
	protected final static int DEPTH_ENEMY = 3;
	protected final static int DEPTH_PLAYER = 1;
	protected final static int DEPTH_BUBBLE = 2;
	protected final static int DEPTH_SHATTER = 4;
	protected final static int DEPTH_SPARK = 5;
	
	protected final static int TIME_FLASH = 60;
	
	protected final static short SPEED_FADE = 3;
	
	protected static Panroom room = null;
	protected final static ArrayList<PlayerContext> pcs = new ArrayList<PlayerContext>();
	protected static MultiFont font = null;
	protected final static FinPanple og = new FinPanple(16, 1, 0);
	protected static Panmage bubble = null;
	protected final static ArrayList<EnemyDefinition> enemies = new ArrayList<EnemyDefinition>();
	protected static Panmage block8 = null;
	protected static Panmage[] gem = null;
	protected static Panimation gemAnm = null;
	protected static Panimation gemCyanAnm = null;
	protected static Panimation spark = null;
	protected static final TileActor bump = new TileActor();
	protected static Panimation marker = null;
	protected static Panmage markerDefeated = null;
	protected static BufferedImage[] dirts = null;
	protected static BufferedImage[] terrains = null;
	
	@Override
	protected final void init(final Panroom room) throws Exception {
		Pangine.getEngine().setTitle("Platformer");
		PlatformGame.room = room;
		loadConstants();
		Panscreen.set(new LogoScreen(Map.MapScreen.class));
	}
	
	protected final static void fadeIn(final Panlayer layer) {
		FadeController.fadeIn(layer, Pancolor.MIN_VALUE, Pancolor.MIN_VALUE, Pancolor.MIN_VALUE, SPEED_FADE);
	}
	
	protected final static void fadeOut(final Panlayer layer, final Panscreen screen) {
		FadeController.fadeOut(layer, Pancolor.MIN_VALUE, Pancolor.MIN_VALUE, Pancolor.MIN_VALUE, SPEED_FADE, screen);
	}
	
	protected final static class PlatformScreen extends Panscreen {
		@Override
        protected final void load() throws Exception {
			loadLevel();
			fadeIn(room);
			Pangine.getEngine().getMusic().start(Music.createSequence());
		}
		
		@Override
	    protected final void destroy() {
			Pangine.getEngine().getMusic().end();
	        Panmage.destroy(Level.timg);
	        Panmage.destroy(Level.bgimg);
	    }
	}
	
	private final static BufferedImage[] loadChrStrip(final String name, final int dim, final PixelFilter f) {
		final BufferedImage[] strip = ImtilX.loadStrip("org/pandcorps/platform/res/chr/" + name, dim);
		if (f != null) {
			final int size = strip.length;
			for (int i = 0; i < size; i++) {
				strip[i] = Imtil.filter(strip[i], f);
			}
		}
		return strip;
	}
	
	private final static void createAnimalStrip(final Profile profile) {
	    final Avatar avatar = profile.currentAvatar;
	    final PixelFilter f = new MultiplyPixelFilter(Channel.Blue, avatar.r, Channel.Blue, avatar.g, Channel.Blue, avatar.b);
		final BufferedImage[] guys = loadChrStrip("Bear.png", 32, f);
		final String anm = avatar.anm;
		final BufferedImage face = Imtil.filter(ImtilX.loadImage("org/pandcorps/platform/res/chr/Face" + anm + ".png", false), f);
		final BufferedImage eyes = ImtilX.loadImage("org/pandcorps/platform/res/chr/Eyes0" + avatar.eye + ".png", false);
		final int size = guys.length;
		for (int i = 0; i < size; i++) {
			final int y = (i == 3) ? -1 : 0;
			Imtil.copy(face, guys[i], 0, 0, 18, 18, 8, 1 + y, Imtil.COPY_FOREGROUND);
			Imtil.copy(eyes, guys[i], 0, 0, 8, 4, 15, 10 + y, Imtil.COPY_FOREGROUND);
		}
		final String pre = "guy." + pcs.size();
		final PlayerContext pc = new PlayerContext(profile);
		
		final Pangine engine = Pangine.getEngine();
		final FinPanple ng = new FinPanple(-Player.PLAYER_X, 0, 0), xg = new FinPanple(Player.PLAYER_X, Player.PLAYER_H, 0);
		pc.guy = engine.createImage(pre, og, ng, xg, guys[0]);
		final Panmage guy2 = engine.createImage(pre + ".2", og, ng, xg, guys[1]);
		final Panmage guy3 = engine.createImage(pre + ".3", og, ng, xg, guys[2]);
		final String fpre = "frm." + pre + ".";
		final Panframe gf1 = engine.createFrame(fpre + "1", pc.guy, 2), gf2 = engine.createFrame(fpre + "2", guy2, 2), gf3 = engine.createFrame(fpre + "3", guy3, 2);
		pc.guyRun = engine.createAnimation("anm." + pre + ".run", gf1, gf2, gf3);
		pc.guyJump = engine.createImage(pre + ".jump", og, ng, xg, guys[3]);
	    //guy = engine.createImage(pre, new FinPanple(8, 0, 0), null, null, ImtilX.loadImage("org/pandcorps/platform/res/chr/Player.png"));
	    
		final BufferedImage[] maps = loadChrStrip("BearMap.png", 32, f);
		final BufferedImage[] faceMap = loadChrStrip("FaceMap" + anm + ".png", 18, f);
		final BufferedImage south1 = maps[0], south2 = Imtil.copy(south1), faceSouth = faceMap[0];
		Imtil.mirror(south2);
		for (final BufferedImage south : new BufferedImage[] {south1, south2}) {
			Imtil.copy(faceSouth, south, 0, 0, 18, 18, 7, 5, Imtil.COPY_FOREGROUND);
			Imtil.copy(eyes, south, 0, 0, 8, 4, 12, 14, Imtil.COPY_FOREGROUND);
		}
		final FinPanple om = new FinPanple(8, -6, 0);
		final int dm = 6;
		pc.guySouth = createAnm(pre + ".south", dm, om, south1, south2);
		final BufferedImage east1 = maps[1], east2 = maps[2], faceEast = faceMap[1];
		final BufferedImage[] easts = {east1, east2};
		for (final BufferedImage east : easts) {
			Imtil.copy(faceEast, east, 0, 0, 18, 18, 7, 5, Imtil.COPY_FOREGROUND);
		}
		final BufferedImage west1 = Imtil.copy(east1), west2 = Imtil.copy(east2);
		final BufferedImage eyesEast = eyes.getSubimage(0, 0, 4, 4);
		for (final BufferedImage east : easts) {
			Imtil.copy(eyesEast, east, 0, 0, 4, 4, 18, 14, Imtil.COPY_FOREGROUND);
		}
		pc.guyEast = createAnm(pre + ".east", dm, om, east1, east2);
		Imtil.mirror(west1);
		Imtil.mirror(west2);
		final BufferedImage eyesWest = eyes.getSubimage(4, 0, 4, 4);
		for (final BufferedImage west : new BufferedImage[] {west1, west2}) {
			Imtil.copy(eyesWest, west, 0, 0, 4, 4, 10, 14, Imtil.COPY_FOREGROUND);
		}
		pc.guyWest = createAnm(pre + ".west", dm, om, west1, west2);
		final BufferedImage north1 = maps[3], north2 = Imtil.copy(north1), faceNorth = faceMap[2];
		Imtil.mirror(north2);
		for (final BufferedImage north : new BufferedImage[] {north1, north2}) {
			Imtil.copy(faceNorth, north, 0, 0, 18, 18, 7, 5, Imtil.COPY_FOREGROUND);
		}
		pc.guyNorth = createAnm(pre + ".north", dm, om, north1, north2);
		//guyMap = engine.createImage(pre + ".map", ImtilX.loadImage("org/pandcorps/platform/res/chr/PlayerMap.png"));
		
		final Panteraction interaction = engine.getInteraction();
		if (profile.ctrl == 0) {
    		pc.inJump = interaction.KEY_SPACE;
    		pc.inLeft = interaction.KEY_LEFT;
    		pc.inRight = interaction.KEY_RIGHT;
		} else {
			final Panteraction.Controller c = Coltil.get(interaction.CONTROLLERS, 0);
			if (c == null) {
			    pc.inJump = interaction.KEY_W;
	            pc.inLeft = interaction.KEY_A;
	            pc.inRight = interaction.KEY_D;
			} else {
				pc.inJump = c.BUTTON_1;
				pc.inLeft = c.LEFT;
				pc.inRight = c.RIGHT;
			}
		}
		
		pcs.add(pc);
	}
	
	private final static void replace(final ReplacePixelFilter f, final short r, final short g, final short b) {
		f.put(r, g, b, Pancolor.MAX_VALUE, r, b, g, Pancolor.MAX_VALUE);
	}
	
	private final static void loadConstants() {
		final Pangine engine = Pangine.getEngine();
		final Profile profile = new Profile();
        profile.setName("A");
        final Avatar avatar = new Avatar();
        avatar.setName("Balue");
        avatar.anm = "Bear";
        avatar.eye = 1;
        avatar.r = 0;
        avatar.g = 1;
        avatar.b = 1;
        profile.currentAvatar = avatar;
        profile.avatars.add(avatar);
        profile.ctrl = 0;
		createAnimalStrip(profile);
		//createAnimalStrip("Grabbit", "Rabbit", 2, new MultiplyPixelFilter(Channel.Blue, 0f, Channel.Blue, 1f, Channel.Blue, 0.25f), 1);
		//createAnimalStrip("Roddy", "Mouse", 3, new SwapPixelFilter(Channel.Blue, Channel.Red, Channel.Blue), 0);
		//createAnimalStrip("Felip", "Cat", 4, new SwapPixelFilter(Channel.Red, Channel.Red, Channel.Blue), 0);
		
		enemies.add(new EnemyDefinition("", 1, null, true));
		enemies.add(new EnemyDefinition("Troblin", 2, null, false));
		final ReplacePixelFilter f = new ReplacePixelFilter();
		replace(f, (short) 104, (short) 120, (short) 172);
		replace(f, (short) 80, (short) 96, (short) 144);
		replace(f, (short) 64, (short) 80, (short) 112);
		replace(f, (short) 48, (short) 56, (short) 80);
		enemies.add(new EnemyDefinition("Obglin", 2, f, false));
		
		bubble = createImage("bubble", "org/pandcorps/platform/res/chr/Bubble.png", 32, og);
	    
	    font = Fonts.getClassics(new FontRequest(8), Pancolor.WHITE, Pancolor.BLACK);
	    
	    block8 = createImage("block8", "org/pandcorps/platform/res/misc/Block8.png", 8);
	    
	    final BufferedImage[] gemStrip = ImtilX.loadStrip("org/pandcorps/platform/res/misc/Gem.png");
	    gem = createSheet("gem", null, gemStrip);
	    gemAnm = createGemAnimation("gem", gem);
	    
	    final SwapPixelFilter gemFilter = new SwapPixelFilter(Channel.Green, Channel.Red, Channel.Blue);
	    for (int i = 0; i < 3; i++) {
	    	gemStrip[i] = Imtil.filter(gemStrip[i], gemFilter);
	    }
	    final Panmage[] gemCyan = createSheet("gem.cyan", null, gemStrip);
	    gemCyanAnm = createGemAnimation("gem.cyan", gemCyan);
	    
	    final Panframe[] sa = createFrames("spark", "org/pandcorps/platform/res/misc/Spark.png", 8, 1);
	    spark = engine.createAnimation("anm.spark", sa[0], sa[1], sa[2], sa[3], sa[2], sa[1], sa[0]);
	    Spark.class.getClass(); // Force class load? Save time later?
	    
	    final FinPanple mo = new FinPanple(-4, -4, 0);
	    final Panmage[] ma = PlatformGame.createSheet("Marker", "org/pandcorps/platform/res/bg/Marker.png", 8, mo);
		final Panframe[] fa = new Panframe[ma.length];
		for (int i = 0; i < ma.length; i++) {
			fa[i] = engine.createFrame("frm.marker." + i, ma[i], 2 * (2 - i % 2));
		}
		marker = engine.createAnimation("anm.marker", fa);
		markerDefeated = engine.createImage("img.Marker.def", mo, null, null, ImtilX.loadStrip("org/pandcorps/platform/res/bg/MarkerDefeated.png", 8)[3]);
		
		dirts = Imtil.loadStrip("org/pandcorps/platform/res/bg/Dirt.png", ImtilX.DIM);
		terrains = Imtil.loadStrip("org/pandcorps/platform/res/bg/Terrain.png", ImtilX.DIM);
	}
	
	private final static Panimation createGemAnimation(final String name, final Panmage[] gem) {
		final Pangine engine = Pangine.getEngine();
		return engine.createAnimation("anm." + name, engine.createFrame("frm." + name + ".0", gem[0], 3), engine.createFrame("frm." + name + ".1", gem[1], 1), engine.createFrame("frm." + name + ".2", gem[2], 1));
	}
	
	private final static void loadLevel() {
	    Level.loadLevel();
	    addHud(room, true);
	}
	
	protected final static Panlayer addHud(final Panroom room, final boolean level) {
		final Panlayer hud = createHud(room);
        final Gem hudGem = new Gem();
        hudGem.getPosition().setY(175);
        hud.addActor(hudGem);
        final int size = pcs.size();
        for (int i = 0; i < size; i++) {
            final PlayerContext pc = pcs.get(i);
            final CallSequence gemSeq;
            if (level) {
                gemSeq = new CallSequence() {@Override protected String call() {
                    return String.valueOf(pc.player.getCurrentLevelGems());}};
            } else {
                gemSeq = new CallSequence() {@Override protected String call() {
                    return String.valueOf(pc.getGems());}};
            }
            final Pantext hudName = new Pantext("hud.name." + i, font, pc.getName());
            final int x = 16 + (i * 56);
            hudName.getPosition().set(x, 183);
            hud.addActor(hudName);
            final Pantext hudGems = new Pantext("hud.gems." + i, font, gemSeq);
            hudGems.getPosition().set(x, 175);
            hud.addActor(hudGems);
        }
        return hud;
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
	        pc.player.onFinishLevel();
	    }
        fadeOut(PlatformGame.room, new Map.MapScreen());
	}
	
	public final static void main(final String[] args) {
        try {
            new PlatformGame().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
