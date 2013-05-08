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

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.game.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.FinPanple;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.TileMapImage;

public class PlatformGame extends BaseGame {
	protected final static byte TILE_BREAK = 2;
	protected final static byte TILE_BUMP = 3;
	protected final static byte TILE_FLOOR = 4;
    protected final static byte TILE_UPSLOPE = 5;
    protected final static byte TILE_DOWNSLOPE = 6;
	
	//protected final static int DEPTH_POWERUP = 0;
	//protected final static int DEPTH_ENEMY = 1;
	protected final static int DEPTH_PLAYER = 2;
	protected final static int DEPTH_SHATTER = 3;
	protected final static int DEPTH_SPARK = 4;
	
	protected final static int TIME_FLASH = 60;
	
	protected static Panroom room = null;
	protected static DynamicTileMap tm = null;
	protected static TileMapImage[][] imgMap = null;
	protected static Panmage block8 = null;
	protected static Panmage[] gem = null;
	protected static Panimation gemAnm = null;
	protected static Panimation spark = null;
	protected static final TileActor bump = new TileActor();
	
	@Override
	protected final void init(final Panroom room) throws Exception {
		Pangine.getEngine().setTitle("Platformer");
		PlatformGame.room = room;
		loadConstants();
		Panscreen.set(new LogoScreen(PlatformScreen.class));
	}
	
	protected final static class PlatformScreen extends Panscreen {
		@Override
        protected final void load() throws Exception {
			loadLevel();
		}
	}
	
	private final static void loadConstants() {
		final Pangine engine = Pangine.getEngine();
	    block8 = createImage("block8", "org/pandcorps/platform/res/misc/Block8.png", 8);
	    gem = createSheet("gem", "org/pandcorps/platform/res/misc/Gem.png");
	    gemAnm = engine.createAnimation("anm.gem", engine.createFrame("frm.gem.0", gem[0], 3), engine.createFrame("frm.gem.1", gem[1], 1), engine.createFrame("frm.gem.2", gem[2], 1));
	    final Panframe[] sa = createFrames("spark", "org/pandcorps/platform/res/misc/Spark.png", 8, 1);
	    spark = engine.createAnimation("anm.spark", sa[0], sa[1], sa[2], sa[3], sa[2], sa[1], sa[0]);
	}
	
	private final static class BlockTileListener implements TileListener {
		final TileMapImage[] blocks;
		private int tick = 0;
		
		private BlockTileListener(final TileMapImage[][] imgMap) {
			blocks = imgMap[0];
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
			}
			tile.setForeground(blocks[tick]);
		}
	}
	
	private final static void loadLevel() {
		final Pangine engine = Pangine.getEngine();
		room.destroy();
		final int w = 768;
		room = engine.createRoom(Pantil.vmid(), new FinPanple(w, 192, 0));
		Pangame.getGame().setCurrentRoom(room);
		tm = new DynamicTileMap("act.tilemap", room, ImtilX.DIM, ImtilX.DIM);
		room.addActor(tm);
		BufferedImage tileImg = ImtilX.loadImage("org/pandcorps/platform/res/bg/Tiles.png", 128, null);
		final int bgMode = 0;
        final BufferedImage dirt = Imtil.loadStrip("org/pandcorps/platform/res/bg/Dirt.png", ImtilX.DIM)[bgMode];
        final PixelMask tileMask = new AntiPixelMask(new ColorPixelMask(224, 112, 0, Pancolor.MAX_VALUE));
        for (int x = 0; x < 80; x += 16) {
            for (int y = 16; y < 48; y += 16) {
                Imtil.copy(dirt, tileImg, 0, 0, 16, 16, x, y, null, tileMask);
            }
        }
        for (int x = 0; x < 48; x += 16) {
        	Imtil.copy(dirt, tileImg, 0, 0, 16, 16, x, 48, null, tileMask);
        }
		final Panmage timg = engine.createImage("img.tiles", tileImg);
		tm.setImageMap(timg);
		imgMap = tm.splitImageMap();
		tm.setTileListener(new BlockTileListener(imgMap));
		
		final Panlayer bg1 = engine.createLayer(Pantil.vmid(), 512, 192, 1, room);
		room.addBeneath(bg1);
		bg1.setMaster(room);
		final TileMap bgtm1 = new TileMap("act.bgmap1", bg1, ImtilX.DIM, ImtilX.DIM);
		bg1.addActor(bgtm1);
		final BufferedImage backImg = ImtilX.loadImage("org/pandcorps/platform/res/bg/Hills.png", 128, null);
		BufferedImage terrain = Imtil.loadStrip("org/pandcorps/platform/res/bg/Terrain.png", ImtilX.DIM)[bgMode];
		final PixelFilter backFilter = new BrightnessPixelFilter((short) -40, (short) -24, (short) -32);
		for (int z = 0; z < 3; z++) {
			final PixelMask backMask = new AntiPixelMask(new ColorPixelMask(196 - 40 * z, 220 - 24 * z, 208 - 32 * z, Pancolor.MAX_VALUE));
			if (z > 0) {
				terrain = Imtil.filter(terrain, backFilter);
			}
			for (int x = 0; x < 64; x += 16) {
				final int yoff = z * 32, ystop = yoff + 32;
				for (int y = yoff; y < ystop; y += 16) {
					Imtil.copy(terrain, backImg, 0, 0, 16, 16, x, y, null, backMask);
				}
			}
		}
		final Panmage bgimg = engine.createImage("img.bg", backImg);
		bgtm1.setImageMap(bgimg);
		final TileMapImage[][] bgMap = bgtm1.splitImageMap();
		hill(bgtm1, bgMap, 1, 4, 8, 0);
		hill(bgtm1, bgMap, 15, 5, 6, 0);
		
		final Panlayer bg2 = engine.createLayer(Pantil.vmid(), 384, 192, 1, room);
        bg1.addBeneath(bg2);
        bg2.setMaster(room);
        final TileMap bgtm2 = new TileMap("act.bgmap2", bg2, ImtilX.DIM, ImtilX.DIM);
        bg2.addActor(bgtm2);
        bgtm2.setImageMap(bgimg);
        hill(bgtm2, bgMap, 3, 8, 12, 2);
        
        final Panlayer bg3 = engine.createLayer(Pantil.vmid(), 320, 192, 1, room);
        bg2.addBeneath(bg3);
        bg3.setMaster(room);
        final TileMap bgtm3 = new TileMap("act.bgmap3", bg3, ImtilX.DIM, ImtilX.DIM);
        bg3.addActor(bgtm3);
        bgtm3.setImageMap(bgimg);
        bgtm3.fillBackground(bgMap[0][3]);
        bgtm3.fillBackground(bgMap[1][3], 7, 1);
        bgtm3.fillBackground(bgMap[2][3], 0, 7);
        cloud(bgtm3, bgMap, 10, 10, 7);
        hill(bgtm3, bgMap, 13, 10, 5, 4);
		
		//tm.fillBackground(imgMap[7][7]); // Don't require transparent image
        final int n = w / 16;
		for (int i = 0; i < n; i++) {
			tm.initTile(i, 0).setForeground(imgMap[1][1], true);
		}
		tm.initTile(13, 0).setForeground(imgMap[3][0], true);
		tm.initTile(13, 1).setForeground(imgMap[2][0], true);
		tm.initTile(13, 2).setForeground(imgMap[1][0], true);
		tm.initTile(14, 2).setForeground(imgMap[1][1], true);
		tm.initTile(14, 1).setForeground(imgMap[2][1], true);
		tm.initTile(14, 0).setForeground(imgMap[2][1], true);
		tm.initTile(15, 2).setForeground(imgMap[1][2], true);
		tm.initTile(15, 1).setForeground(imgMap[2][2], true);
		tm.initTile(15, 0).setForeground(imgMap[3][2], true);
		bush(1, 1, 0);
		tm.initTile(28, 1).setForeground(imgMap[3][3], TILE_UPSLOPE);
		tm.initTile(29, 2).setForeground(imgMap[3][3], TILE_UPSLOPE);
		tm.initTile(30, 3).setForeground(imgMap[3][3], TILE_UPSLOPE);
		for (int i = 31; i < 37; i++) {
		    tm.initTile(i, 3).setForeground(imgMap[2][1], true);
		}
		tm.initTile(37, 3).setForeground(imgMap[3][4], TILE_DOWNSLOPE);
        tm.initTile(38, 2).setForeground(imgMap[3][4], TILE_DOWNSLOPE);
        tm.initTile(39, 1).setForeground(imgMap[3][4], TILE_DOWNSLOPE);
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
		gem(4, 1);
		gem(14, 5);
		tm.initTile(8, 1).setForeground(imgMap[0][6], TILE_UPSLOPE);
		tm.initTile(9, 1).setForeground(imgMap[0][4], true);
		tm.initTile(10, 1).setForeground(imgMap[0][7], TILE_DOWNSLOPE);
		final Player player = new Player();
		room.addActor(player);
		Pangine.getEngine().track(player);
		setPosition(player, 16, 16, DEPTH_PLAYER);
	}
	
	protected static void setPosition(final Panctor act, final float x, final float y, final float depth) {
	    act.getPosition().set(x, y, tm.getForegroundDepth() + depth);
	}
	
	private static void setBg(final TileMap tm, final int i, final int j, final TileMapImage[][] imgMap, final int iy, final int ix) {
	    final Tile t = tm.initTile(i, j);
	    t.setBackground(imgMap[iy][ix]);
	    t.setForeground(null, false);
	}
	
	private static void hill(final TileMap tm, final TileMapImage[][] imgMap, final int x, final int y, final int w, final int iy) {
		for (int j = 0; j < y; j++) {
			setBg(tm, x, j, imgMap, iy + 1, 0);
			setBg(tm, x + w + 1, j, imgMap, iy + 1, 2);
		}
		final int stop = x + w;
		for (int i = x + 1; i <= stop; i++) {
		    setBg(tm, i, y, imgMap, iy, 1);
			for (int j = 0; j < y; j++) {
			    setBg(tm, i, j, imgMap, iy + 1, 1);
			}
		}
		tm.initTile(x, y).setForeground(imgMap[iy][0]);
		tm.initTile(stop + 1, y).setForeground(imgMap[iy][2]);
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
	
	public final static void main(final String[] args) {
        try {
            new PlatformGame().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
