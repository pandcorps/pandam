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
import org.pandcorps.pandax.tile.Tile.TileMapImage;
import org.pandcorps.pandax.visual.FadeController;
import org.pandcorps.platform.Player.PlayerContext;

public class PlatformGame extends BaseGame {
	/*
	Dog player face.
	Player falling/sliding images.
	Warp Map marker for entry/exit point.
	Multiple islands for Map.
	Replace bush with Rise.png for some levels; rise will be higher than 1 tile; separate build method.
	Random levels.
	Random maps.
	Gamepads.
	Multiplayer camera.
	Don't spawn Enemies until Player is near.
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
	
	protected final static PixelFilter terrainDarkener = new BrightnessPixelFilter((short) -40, (short) -24, (short) -32);
	
	protected static Panroom room = null;
	protected final static ArrayList<PlayerContext> pcs = new ArrayList<PlayerContext>();
	protected static MultiFont font = null;
	protected static DynamicTileMap tm = null;
	protected static TileMapImage[][] imgMap = null;
	protected final static FinPanple og = new FinPanple(16, 1, 0);
	protected static Panmage bubble = null;
	protected static Panimation enemy01 = null;
	protected static Panmage block8 = null;
	protected static Panmage[] gem = null;
	protected static Panimation gemAnm = null;
	protected static Panimation gemCyanAnm = null;
	protected static Panimation spark = null;
	protected static TileMapImage[] flashBlock;
	protected static Panmage timg = null;
	protected static Panmage bgimg = null;
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
	        Panmage.destroy(timg);
	        Panmage.destroy(bgimg);
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
	
	private final static void createAnimalStrip(final String name, final String anm, final int eye, final PixelFilter f, final int ctrl) {
		final BufferedImage[] guys = loadChrStrip("Bear.png", 32, f);
		final BufferedImage face = Imtil.filter(ImtilX.loadImage("org/pandcorps/platform/res/chr/Face" + anm + ".png", false), f);
		final BufferedImage eyes = ImtilX.loadImage("org/pandcorps/platform/res/chr/Eyes0" + eye + ".png", false);
		final int size = guys.length;
		for (int i = 0; i < size; i++) {
			final int y = (i == 3) ? -1 : 0;
			Imtil.copy(face, guys[i], 0, 0, 18, 18, 8, 1 + y, Imtil.COPY_FOREGROUND);
			Imtil.copy(eyes, guys[i], 0, 0, 8, 4, 15, 10 + y, Imtil.COPY_FOREGROUND);
		}
		final String pre = "guy." + pcs.size();
		final PlayerContext pc = new PlayerContext(name);
		
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
		if (ctrl == 0) {
    		pc.inJump = interaction.KEY_SPACE;
    		pc.inLeft = interaction.KEY_LEFT;
    		pc.inRight = interaction.KEY_RIGHT;
		} else {
		    pc.inJump = interaction.KEY_W;
            pc.inLeft = interaction.KEY_A;
            pc.inRight = interaction.KEY_D;
		}
		
		pcs.add(pc);
	}
	
	private final static void loadConstants() {
		final Pangine engine = Pangine.getEngine();
		createAnimalStrip("Balue", "Bear", 1, null, 0);
		createAnimalStrip("Grabbit", "Rabbit", 2, new SwapPixelFilter(Channel.Red, Channel.Blue, Channel.Red), 1);
		//createAnimalStrip("Roddy", "Mouse", 3, new SwapPixelFilter(Channel.Blue, Channel.Red, Channel.Blue), 0);
		//createAnimalStrip("Felip", "Cat", 4, new SwapPixelFilter(Channel.Red, Channel.Red, Channel.Blue), 0);
		
		enemy01 = createAnm("enemy", "org/pandcorps/platform/res/enemy/Enemy01.png", 16, 6, new FinPanple(8, 1, 0), new FinPanple(-Enemy.ENEMY_X, 0, 0), new FinPanple(Enemy.ENEMY_X, Enemy.ENEMY_H, 0));
		
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
	
	private final static class BlockTileListener implements TileListener {
		private int tick = 0;
		
		private BlockTileListener(final TileMapImage[][] imgMap) {
			flashBlock = imgMap[0];
		}
		
		@Override
		public boolean isActive() {
			tick = (int) Pangine.getEngine().getClock() % TIME_FLASH;
			if (tick < 4) {
				tick = (tick + 1) % 4;
				return true;
			}
			return false;
		}
		
		@Override
		public final void onStep(final Tile tile) {
			if (tile == null || tile.getBehavior() != TILE_BUMP) {
				return;
			} else if (!isFlash(tile)) {
				return;
			}
			tile.setForeground(flashBlock[tick]);
		}
	}
	
	protected final static boolean isFlash(final Tile tile) {
		final Object bg = DynamicTileMap.getRawForeground(tile);
		for (int i = 0; i < 4; i++) {
			if (flashBlock[i] == bg) {
				return true;
			}
		}
		return false;
	}
	
	protected final static void applyDirtTexture(final BufferedImage tileImg, final int ix, final int iy, final int fx, final int fy) {
		final BufferedImage dirt = dirts[Map.bgTexture];
		final PixelMask tileMask = new AntiPixelMask(new ColorPixelMask(224, 112, 0, Pancolor.MAX_VALUE));
		for (int x = ix; x < fx; x += 16) {
            for (int y = iy; y < fy; y += 16) {
                Imtil.copy(dirt, tileImg, 0, 0, 16, 16, x, y, null, tileMask);
            }
        }
	}
	
	protected final static BufferedImage getTerrainTexture() {
		return terrains[Map.bgTexture];
	}
	
	protected final static PixelMask getTerrainMask(final int z) {
		return new AntiPixelMask(new ColorPixelMask(196 - 40 * z, 220 - 24 * z, 208 - 32 * z, Pancolor.MAX_VALUE));
	}
	
	protected final static BufferedImage getDarkenedTerrain(final BufferedImage terrain) {
		return Imtil.filter(terrain, terrainDarkener);
	}
	
	protected final static void applyTerrainTexture(final BufferedImage backImg, final int ix, final int iy, final int fx, final int fy, final BufferedImage terrain, final PixelMask backMask) {
		for (int x = ix; x < fx; x += 16) {
			for (int y = iy; y < fy; y += 16) {
				Imtil.copy(terrain, backImg, 0, 0, 16, 16, x, y, null, backMask);
			}
		}
	}
	
	protected final static BufferedImage getColoredTerrain(final BufferedImage backImg, final int x, final int y, final int w, final int h) {
		return Imtil.filter(backImg, x, y, w, h, getHillFilter(Map.bgColor));
	}
	
	private final static void loadLevel() {
		final Pangine engine = Pangine.getEngine();
		room.destroy();
		final int w = 768;
		room = engine.createRoom(Pantil.vmid(), new FinPanple(w, 192, 0));
		Pangame.getGame().setCurrentRoom(room);
		tm = new DynamicTileMap("act.tilemap", room, ImtilX.DIM, ImtilX.DIM);
		room.addActor(tm);
		final BufferedImage tileImg = ImtilX.loadImage("org/pandcorps/platform/res/bg/Tiles.png", 128, null);
		
        applyDirtTexture(tileImg, 0, 16, 80, 128);
		timg = engine.createImage("img.tiles", tileImg);
		tm.setImageMap(timg);
		imgMap = tm.splitImageMap();
		tm.setTileListener(new BlockTileListener(imgMap));
		
		final Panlayer bg1 = createParallax(room, 2);
		final TileMap bgtm1 = new TileMap("act.bgmap1", bg1, ImtilX.DIM, ImtilX.DIM);
		bg1.addActor(bgtm1);
		BufferedImage backImg = ImtilX.loadImage("org/pandcorps/platform/res/bg/Hills.png", 128, null);
		BufferedImage terrain = getTerrainTexture();
		for (int z = 0; z < 3; z++) {
			if (z > 0) {
				terrain = getDarkenedTerrain(terrain);
			}
			final int yoff = z * 32;
			applyTerrainTexture(backImg, 0, yoff, 64, yoff + 32, terrain, getTerrainMask(z));
		}
		backImg = getColoredTerrain(backImg, 0, 0, 96, 96);
		bgimg = engine.createImage("img.bg", backImg);
		bgtm1.setImageMap(bgimg);
		final TileMapImage[][] bgMap = bgtm1.splitImageMap();
		hill(bgtm1, bgMap, 1, 4, 8, 0, 0);
		hill(bgtm1, bgMap, 15, 5, 6, 3, 0);
		hill(bgtm1, bgMap, 24, 4, 4, 0, 0);
		
		/*
		It would look strange if layers 1 and 3 moved without 2.
		So it's probably best if each layer's master is the one directly above it
		instead of basing all on the foreground with different divisors.
		*/
		final Panlayer bg2 = createParallax(bg1, 2);
        final TileMap bgtm2 = new TileMap("act.bgmap2", bg2, ImtilX.DIM, ImtilX.DIM);
        bg2.addActor(bgtm2);
        bgtm2.setImageMap(bgimg);
        hill(bgtm2, bgMap, 0, 6, 4, 3, 2);
        hill(bgtm2, bgMap, 7, 8, 7, 0, 2);
        
        final Panlayer bg3 = createParallax(bg2, 2);
        final TileMap bgtm3 = new TileMap("act.bgmap3", bg3, ImtilX.DIM, ImtilX.DIM);
        bg3.addActor(bgtm3);
        bgtm3.setImageMap(bgimg);
        bgtm3.fillBackground(bgMap[0][6]);
        bgtm3.fillBackground(bgMap[1][6], 7, 1);
        bgtm3.fillBackground(bgMap[2][6], 0, 7);
        cloud(bgtm3, bgMap, 10, 10, 7);
        hill(bgtm3, bgMap, 2, 9, 4, 0, 4);
        cloud(bgtm3, bgMap, 4, 6, 3);
        hill(bgtm3, bgMap, 13, 10, 5, 3, 4);
		
		//tm.fillBackground(imgMap[7][7]); // Don't require transparent image
        final int n = w / 16;
		for (int i = 0; i < n; i++) {
			tm.initTile(i, 0).setForeground(imgMap[1][1], true);
		}
		tm.removeTile(0, 0);
		tm.removeTile(1, 0);
		tm.initTile(2, 0).setForeground(imgMap[1][0], true);
		
		tm.initTile(13, 0).setForeground(imgMap[3][0], true);
		tm.initTile(13, 1).setForeground(imgMap[2][0], true);
		tm.initTile(13, 2).setForeground(imgMap[1][0], true);
		tm.initTile(14, 2).setForeground(imgMap[1][1], true);
		tm.initTile(14, 1).setForeground(imgMap[2][1], true);
		tm.initTile(14, 0).setForeground(imgMap[2][1], true);
		tm.initTile(15, 2).setForeground(imgMap[1][2], true);
		tm.initTile(15, 1).setForeground(imgMap[2][2], true);
		tm.initTile(15, 0).setForeground(imgMap[3][2], true);
		bush(4, 1, 0);
		for (int j = 0; j <= 3; j++) {
		    if (j != 0) {
        		tm.initTile(27 + j, j).setForeground(imgMap[3][3], TILE_UPSLOPE);
        		tm.initTile(40 - j, j).setForeground(imgMap[3][4], TILE_DOWNSLOPE);
		    }
    		if (j == 3) {
        		for (int i = 27 + j + 1; i < 40 - j; i++) {
        		    tm.initTile(i, j).setForeground(imgMap[1][1], true);
        		}
    		} else {
    		    tm.initTile(28 + j, j).setForeground(imgMap[3][0], true);
    		    for (int i = 28 + j + 1; i < 39 - j; i++) {
                    tm.initTile(i, j).setForeground(imgMap[2][1], true);
                }
    		    tm.initTile(39 - j, j).setForeground(imgMap[3][2], true);
    		}
		}
		bush(32, 4, 2);
		rise(19, 1, 5, 3);
		tm.initTile(23, 3).setBackground(imgMap[3][1]);
		tm.initTile(22, 2).setBackground(imgMap[3][1]);
		rise(18, 1, 1, 1);
		tm.initTile(2, 3).setForeground(imgMap[0][5], TILE_BREAK);
		tm.initTile(3, 3).setForeground(imgMap[0][5], TILE_BREAK);
		final Tile block = tm.initTile(4, 3);
		block.setForeground(imgMap[0][0], TILE_BUMP);
		bump.setViewFromForeground(block);
		tm.initTile(5, 3).setForeground(imgMap[0][0], TILE_BUMP);
		tm.initTile(6, 3).setForeground(imgMap[0][4], true);
		tm.initTile(2, 6).setForeground(imgMap[0][6], TILE_UPSLOPE);
		tm.initTile(6, 6).setForeground(imgMap[0][7], TILE_DOWNSLOPE);
		gem(9, 4);
		gem(14, 5);
		gem(34, 7);
		tm.initTile(8, 1).setForeground(imgMap[0][6], TILE_UPSLOPE);
		tm.initTile(9, 1).setForeground(imgMap[0][4], true);
		tm.initTile(10, 1).setForeground(imgMap[0][7], TILE_DOWNSLOPE);
		for (int y = 1; y <= 3; y++) {
			tm.initTile(43 - y, y).setForeground(imgMap[y == 3 ? 7 : 5][3]);
			for (int x = 1; x <= 3; x++) {
				tm.initTile(43 + x - y, y).setForeground(imgMap[2][1]);
			}
			tm.initTile(47 - y, y).setForeground(imgMap[4][4]);
		}
		for (int y = 1; y <= 2; y++) {
			tm.initTile(39 + y, 3 + y).setForeground(imgMap[3][3], TILE_UPSLOPE_FLOOR);
			tm.initTile(40 + y, 3 + y).setForeground(y == 2 ? imgMap[6][4] : imgMap[3][0]);
			if (y < 2) {
				tm.initTile(43 - y, 3 + y).setForeground(imgMap[2][1]);
				tm.initTile(44 - y, 3 + y).setForeground(imgMap[4][4]);
			}
		}
		tm.initTile(42, 8).setForeground(imgMap[7][0], TILE_BUMP);
		final int size = pcs.size();
		for (int i = 0; i < size; i++) {
    		final Player player = new Player(pcs.get(i));
    		room.addActor(player);
    		Pangine.getEngine().track(player);
    		setPosition(player, 40 + (20 * i), 16, DEPTH_PLAYER);
		}
		
		new Enemy(80, 64);
		new Enemy(232, 48);
		new Enemy(360, 16);
		
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
	    act.getPosition().set(x, y, tm.getForegroundDepth() + depth);
	}
	
	private static void setBg(final TileMap tm, final int i, final int j, final TileMapImage[][] imgMap, final int iy, final int ix) {
	    final Tile t = tm.initTile(i, j);
	    t.setBackground(imgMap[iy][ix]);
	    t.setForeground(null, false);
	}
	
	private static void hill(final TileMap tm, final TileMapImage[][] imgMap, final int x, final int y, final int w, final int ix, final int iy) {
		for (int j = 0; j < y; j++) {
			setBg(tm, x, j, imgMap, iy + 1, ix);
			setBg(tm, x + w + 1, j, imgMap, iy + 1, ix + 2);
		}
		final int stop = x + w;
		for (int i = x + 1; i <= stop; i++) {
		    setBg(tm, i, y, imgMap, iy, ix + 1);
			for (int j = 0; j < y; j++) {
			    setBg(tm, i, j, imgMap, iy + 1, ix + 1);
			}
		}
		tm.initTile(x, y).setForeground(imgMap[iy][ix]);
		tm.initTile(stop + 1, y).setForeground(imgMap[iy][ix + 2]);
	}
	
	private static void cloud(final TileMap tm, final TileMapImage[][] imgMap, final int x, final int y, final int w) {
        final int stop = x + w;
        for (int i = x + 1; i <= stop; i++) {
            tm.initTile(i, y).setBackground(imgMap[7][1]);
            tm.initTile(i, y + 1).setBackground(imgMap[6][1]);
        }
        tm.initTile(x, y).setForeground(imgMap[7][0]);
        tm.initTile(x, y + 1).setForeground(imgMap[6][0]);
        tm.initTile(stop + 1, y).setForeground(imgMap[7][2]);
        tm.initTile(stop + 1, y + 1).setForeground(imgMap[6][2]);
    }
	
	private static void rise(final int x, final int y, final int w, final int h) {
		final int ystop = y + h;
		for (int j = y; j < ystop; j++) {
			tm.initTile(x, j).setBackground(imgMap[2][3]);
			tm.initTile(x + w + 1, j).setBackground(imgMap[2][4]);
		}
		final int stop = x + w;
		for (int i = x + 1; i <= stop; i++) {
			tm.initTile(i, ystop).setBackground(imgMap[1][1], TILE_FLOOR);
			for (int j = y; j < ystop; j++) {
				tm.initTile(i, j).setBackground(imgMap[2][1]);
			}
		}
		tm.initTile(x, ystop).setForeground(imgMap[1][3], TILE_FLOOR);
		tm.initTile(stop + 1, ystop).setForeground(imgMap[1][4], TILE_FLOOR);
	}
	
	private static void bush(final int x, final int y, final int w) {
		tm.initTile(x, y).setForeground(imgMap[1][5]);
		final int stop = x + w;
		for (int i = x + 1; i <= stop; i++) {
			tm.initTile(i, y).setForeground(imgMap[1][6]);
		}
		tm.initTile(stop + 1, y).setForeground(imgMap[1][7]);
	}
	
	private static void gem(final int x, final int y) {
		final Gem gem = new Gem();
		gem.setPosition(tm.initTile(x, y));
		room.addActor(gem);
	}
	
	private static PixelFilter getHillFilter(final int mode) {
		switch (mode) {
			case 0 : return null;
			case 1 : return new SwapPixelFilter(Channel.Red, Channel.Blue, Channel.Green);
			case 2 : return new SwapPixelFilter(Channel.Blue, Channel.Red, Channel.Green);
		}
		throw new IllegalArgumentException(String.valueOf(mode));
	}
	
	protected final static void levelVictory() {
	    for (final Panctor actor : room.getActors()) {
            if (actor instanceof Enemy) {
                ((Enemy) actor).onBump();
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
