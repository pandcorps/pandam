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
import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.core.img.Pancolor.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;

public class Level {
    protected final static PixelFilter terrainDarkener = new BrightnessPixelFilter((short) -40, (short) -24, (short) -32);
    
    protected static TileMapImage[] flashBlock;
    
    protected static Panroom room = null;
    protected static Panmage timg = null;
    protected static Panmage bgimg = null;
    protected static DynamicTileMap tm = null;
    protected static TileMap bgtm1 = null;
    protected static TileMap bgtm2 = null;
    protected static TileMap bgtm3 = null;
    protected static TileMapImage[][] imgMap = null;
    protected static TileMapImage[][] bgMap = null;
    private static int w = 0;
    private static int n = 0;
    
    private final static class BlockTileListener implements TileListener {
        private int tick = 0;
        
        private BlockTileListener(final TileMapImage[][] imgMap) {
            flashBlock = imgMap[0];
        }
        
        @Override
        public boolean isActive() {
            tick = (int) Pangine.getEngine().getClock() % PlatformGame.TIME_FLASH;
            if (tick < 4) {
                tick = (tick + 1) % 4;
                return true;
            }
            return false;
        }
        
        @Override
        public final void onStep(final Tile tile) {
            if (tile == null || tile.getBehavior() != PlatformGame.TILE_BUMP) {
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
        final BufferedImage dirt = PlatformGame.dirts[Map.bgTexture];
        final PixelMask tileMask = new AntiPixelMask(new ColorPixelMask(224, 112, 0, Pancolor.MAX_VALUE));
        for (int x = ix; x < fx; x += 16) {
            for (int y = iy; y < fy; y += 16) {
                Imtil.copy(dirt, tileImg, 0, 0, 16, 16, x, y, null, tileMask);
            }
        }
    }
    
    protected final static BufferedImage getTerrainTexture() {
        return PlatformGame.terrains[Map.bgTexture];
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
    
    protected final static void loadLayers() {
        final Pangine engine = Pangine.getEngine();
        PlatformGame.room.destroy();
        room = engine.createRoom(Pantil.vmid(), new FinPanple(w, 192, 0));
        PlatformGame.room = room;
        Pangame.getGame().setCurrentRoom(room);
        tm = new DynamicTileMap("act.tilemap", room, ImtilX.DIM, ImtilX.DIM);
        room.addActor(tm);
        final BufferedImage tileImg = ImtilX.loadImage("org/pandcorps/platform/res/bg/Tiles.png", 128, null);
        
        applyDirtTexture(tileImg, 0, 16, 80, 128);
        timg = engine.createImage("img.tiles", tileImg);
        tm.setImageMap(timg);
        imgMap = tm.splitImageMap();
        tm.setTileListener(new BlockTileListener(imgMap));
        
        final Panlayer bg1 = PlatformGame.createParallax(room, 2);
        bgtm1 = new TileMap("act.bgmap1", bg1, ImtilX.DIM, ImtilX.DIM);
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
        bgMap = bgtm1.splitImageMap();
        
        /*
        It would look strange if layers 1 and 3 moved without 2.
        So it's probably best if each layer's master is the one directly above it
        instead of basing all on the foreground with different divisors.
        */
        final Panlayer bg2 = PlatformGame.createParallax(bg1, 2);
        bgtm2 = new TileMap("act.bgmap2", bg2, ImtilX.DIM, ImtilX.DIM);
        bg2.addActor(bgtm2);
        bgtm2.setImageMap(bgimg);
        
        final Panlayer bg3 = PlatformGame.createParallax(bg2, 2);
        bgtm3 = new TileMap("act.bgmap3", bg3, ImtilX.DIM, ImtilX.DIM);
        bg3.addActor(bgtm3);
        bgtm3.setImageMap(bgimg);
        bgtm3.fillBackground(bgMap[0][6]);
        bgtm3.fillBackground(bgMap[1][6], 8, 1);
        bgtm3.fillBackground(bgMap[2][6], 0, 8);
    }
    
    protected final static void loadLevel() {
    	final Builder b = new RandomBuilder();
    	w = b.getW();
    	n = w / 16;
    	loadLayers();
    	b.build();
    	addPlayers();
    }
    
    private static interface Builder {
    	public int getW();
    	
    	public void build();
    }
    
    protected final static class DemoBuilder implements Builder {
    	@Override
    	public int getW() {
    		return 768;
    	}
    	
    	@Override
    	public void build() {
    		buildDemo();
    	}
    }
    
    protected final static void buildDemo() {
        hill(bgtm1, 1, 4, 8, 0, 0);
        hill(bgtm1, 15, 5, 6, 3, 0);
        hill(bgtm1, 24, 4, 4, 0, 0);
        
        hill(bgtm2, 0, 6, 4, 3, 2);
        hill(bgtm2, 7, 8, 7, 0, 2);
        
        cloud(bgtm3, 10, 10, 7);
        hill(bgtm3, 2, 9, 4, 0, 4);
        cloud(bgtm3, 4, 6, 3);
        hill(bgtm3, 13, 10, 5, 3, 4);
        
        for (int i = 0; i < n; i++) {
            tm.initTile(i, 0).setForeground(imgMap[1][1], true);
        }
        tm.removeTile(0, 0);
        tm.removeTile(1, 0);
        tm.initTile(2, 0).setForeground(imgMap[1][0], true);
        
        step(13, 0, 1, 1);
        bush(4, 1, 0);
        ramp(27, 0, 6, 3);
        wall(32, 4, 2, 1);
        naturalRise(18, 1, 4, 3);
        naturalRise(17, 1, 1, 1);
        colorRise(25, 1, 0, 2, 0);
        breakableBlock(2, 3);
        breakableBlock(3, 3);
        bumpableBlock(4, 3);
        bumpableBlock(5, 3);
        solidBlock(6, 3);
        upBlock(2, 6);
        downBlock(6, 6);
        gem(9, 4);
        gem(14, 5);
        gem(34, 7);
        upBlock(8, 1);
        solidBlock(9, 1);
        downBlock(10, 1);
        slantUp(42, 1, 1, 3);
        goalBlock(42, 8);
        
        new Enemy(80, 64);
        new Enemy(232, 48);
        new Enemy(360, 16);
    }
    
    private static int bx;
    
    protected final static class RandomBuilder implements Builder {
    	@Override
    	public int getW() {
    		return 3200;
    	}
    	
    	@Override
    	public void build() {
    	    loadTemplates();
    	    
    		buildBg(bgtm1, 4, 6, 0); // Nearest
    		buildBg(bgtm2, 7, 9, 2);
    		buildBg(bgtm3, 10, 12, 4); // Farthest
            //cloud
    		
    		for (int i = 0; i < n; i++) {
                tm.initTile(i, 0).setForeground(imgMap[1][1], true);
            }
    		
    		for (bx = 8; bx < n; ) {
    			/*
    			Raise/lower floor (with 1-way steps or ramps)
    			Some templates should allow any other template on top of it
    			Some templates should allow decorations on top
    			Block ramps, stairs, gap patterns, 2x2 block patterns
    			Slant
    			Gems
    			Enemies
    			Goal
    			*/
    		    Mathtil.rand(templates).build();
    		    bx += Mathtil.randi(1, 4);
    		}
    	}
    }
    
    private final static ArrayList<Template> templates = new ArrayList<Template>();
    private final static int[] scratch = new int[128];
    
    private final static void swapScratch(final int i, final int j) {
    	swap(scratch, i, j);
    }
    
    private final static void swap(final int[] a, final int i, final int j) {
        final int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }
    
    private final static void loadTemplates() {
        if (templates.size() > 0) {
            return;
        }
        new NaturalRiseTemplate();
        new ColorRiseTemplate();
        new WallTemplate();
        new StepTemplate();
        new RampTemplate();
        new BushTemplate();
        new PitTemplate();
        new BridgePitTemplate();
        new UpBlockStepTemplate();
        new DownBlockStepTemplate();
        new BlockWallTemplate();
    }
    
    private static abstract class Template {
        protected Template() {
            templates.add(this);
        }
        
        protected abstract void build();
    }
    
    private static abstract class RiseTemplate extends Template {
        @Override
        protected final void build() {
            final int amt = Mathtil.randi(1, 3);
            for (int i = 0; i < amt; i++) {
                scratch[i] = ((i == 0) ? -1 : scratch[i - 1]) + Mathtil.randi(1, 3);
            }
            final int stop = amt * 3;
            int start = bx;
            for (int i = amt; i < stop; i += 2) {
                scratch[i] = start;
                int w = Mathtil.randi(0, 8);
                if (i > amt) {
                    final int min = scratch[i - 2] + scratch[i - 1];
                    if (start + w <= min) {
                        w = min + 1 - start;
                    }
                }
                start += (Mathtil.randi(1, w + 1));
                if (i > amt && start == scratch[i - 2] + scratch[i - 1] + 2) {
                    start++;
                    w++;
                }
                scratch[i + 1] = w;
                bx = start + w + 2;
            }
            if (bx >= n) {
                return;
            }
            for (int i = 0; i < amt; i++) {
                final int r = Mathtil.randi(0, amt - 1);
                final int io = amt + i * 2, ro = amt + r * 2;
                swapScratch(io, ro);
                swapScratch(io + 1, ro + 1);
            }
            init();
            for (int i = 0; i < amt; i++) {
                final int xo = amt + i * 2;
                rise(scratch[xo], 1, scratch[xo + 1], scratch[amt - i - 1]);
            }
        }
        
        protected void init() {
        }
        
        protected abstract void rise(final int x, final int y, final int w, final int h);
    }
    
    private static final class NaturalRiseTemplate extends RiseTemplate {
    	@Override
    	protected final void rise(final int x, final int y, final int w, final int h) {
    		naturalRise(x, y, w, h);
    	}
    }
    
    private static int[] colors = new int[3];
    private static int colorIndex = 0;
    
    private static final class ColorRiseTemplate extends RiseTemplate {
    	@Override
    	protected final void init() {
    		for (int i = 0; i < 3; i++) {
    			colors[i] = i;
    		}
    		colorIndex = 0;
    		for (int i = 0; i < 3; i++) {
    			swap(colors, i, Mathtil.randi(0, 2));
    		}
    	}
    	
        @Override
        protected final void rise(final int x, final int y, final int w, final int h) {
            colorRise(x, y, w, h, colors[colorIndex]);
            colorIndex = (colorIndex + 1) % 3;
        }
    }
    
    private static final class WallTemplate extends Template {
        @Override
        protected final void build() {
        	final int w = Mathtil.randi(0, 8), x = bx;
        	bx += (w + 2);
        	if (bx >= n) {
                return;
            }
        	wall(x, 1, w, Mathtil.randi(0, 3));
        }
    }
    
    private static final class StepTemplate extends Template {
        @Override
        protected final void build() {
        	final int w = Mathtil.randi(0, 8), x = bx;
        	bx += (w + 2);
        	if (bx >= n) {
                return;
            }
        	step(x, 0, w, Mathtil.randi(0, 2));
        }
    }
    
    private static final class RampTemplate extends Template {
        @Override
        protected final void build() {
        	final int w = Mathtil.randi(0, 6), h = Mathtil.randi(1, 4), x = bx;
        	bx += (w + h * 2 + 2);
        	if (bx >= n) {
                return;
            }
        	ramp(x, 0, w, h);
        }
    }
    
    private static final class PitTemplate extends Template {
        @Override
        protected final void build() {
        	final int w = Mathtil.randi(2, 4), x = bx;
        	bx += (w + 2);
        	if (bx >= n) {
                return;
            }
        	pit(x, 0, w);
        }
    }
    
    private static final class BridgePitTemplate extends Template {
        @Override
        protected final void build() {
        	final int w = Mathtil.randi(5, 10), x = bx;
        	bx += (w + 2);
        	if (bx >= n) {
                return;
            }
        	pit(x, 0, w);
        	final int stop = x + w;
        	for (int i = x + 2; i < stop; i++) {
        		solidBlock(i, 3);
        	}
        }
    }
    
    private static final class UpBlockStepTemplate extends Template {
        @Override
        protected final void build() {
        	final int w = Mathtil.randi(1, 3), x = bx;
        	bx += w;
        	if (bx >= n) {
                return;
            }
        	upBlockStep(x, 1, w);
        }
    }
    
    private static final class DownBlockStepTemplate extends Template {
        @Override
        protected final void build() {
        	final int w = Mathtil.randi(1, 3), x = bx;
        	bx += w;
        	if (bx >= n) {
                return;
            }
        	downBlockStep(x, 1, w);
        }
    }
    
    private static final class BlockWallTemplate extends Template {
        @Override
        protected final void build() {
        	final int w = Mathtil.randi(1, 8), x = bx;
        	bx += w;
        	if (bx >= n) {
                return;
            }
        	blockWall(x, 1, w, Mathtil.randi(1, 3));
        }
    }
    
    private static final class BushTemplate extends Template {
    	@Override
        protected final void build() {
        	final int w = Mathtil.randi(0, 6), x = bx;
        	bx += (w + 2);
        	if (bx >= n) {
                return;
            }
        	bush(x, 1, w);
        }
    }
    
    private static void buildBg(final TileMap tm, final int miny, final int maxy, final int iy) {
    	final int maxx = tm.getWidth() + 1;
    	int x = Mathtil.randi(-1, 4);
    	while (x < maxx) {
    		final int w = Mathtil.randi(4, 8);
    		hill(tm, x, Mathtil.randi(miny, maxy), w, Mathtil.rand() ? 0 : 3, iy);
    		x += (w + Mathtil.randi(3, 7));
    	}
    }
    
    private static void addPlayers() {
    	final int size = PlatformGame.pcs.size();
        final ArrayList<Player> players = new ArrayList<Player>(size);
        for (int i = 0; i < size; i++) {
            final Player player = new Player(PlatformGame.pcs.get(i));
            room.addActor(player);
            PlatformGame.setPosition(player, 40 + (20 * i), 16, PlatformGame.DEPTH_PLAYER);
            players.add(player);
        }
        Pangine.getEngine().track(Panverage.getArithmeticMean(players));
    }
    
    private static void setBg(final TileMap tm, final int i, final int j, final TileMapImage[][] imgMap, final int iy, final int ix) {
    	if (tm.isBad(i, j)) {
    		return;
    	}
        final Tile t = tm.initTile(i, j);
        t.setBackground(imgMap[iy][ix]);
        t.setForeground(null, false);
    }
    
    private static void setFg(final TileMap tm, final int i, final int j, final TileMapImage[][] imgMap, final int iy, final int ix) {
    	if (tm.isBad(i, j)) {
    		return;
    	}
    	tm.initTile(i, j).setForeground(imgMap[iy][ix]);
    }
    
    private static void hill(final TileMap tm, final int x, final int y, final int w, final int ix, final int iy) {
        for (int j = 0; j < y; j++) {
            setBg(tm, x, j, bgMap, iy + 1, ix);
            setBg(tm, x + w + 1, j, bgMap, iy + 1, ix + 2);
        }
        final int stop = x + w;
        for (int i = x + 1; i <= stop; i++) {
            setBg(tm, i, y, bgMap, iy, ix + 1);
            for (int j = 0; j < y; j++) {
                setBg(tm, i, j, bgMap, iy + 1, ix + 1);
            }
        }
        setFg(tm, x, y, bgMap, iy, ix);
        setFg(tm, stop + 1, y, bgMap, iy, ix + 2);
    }
    
    private static void cloud(final TileMap tm, final int x, final int y, final int w) {
        final int stop = x + w;
        for (int i = x + 1; i <= stop; i++) {
            tm.initTile(i, y).setBackground(bgMap[7][1]);
            tm.initTile(i, y + 1).setBackground(bgMap[6][1]);
        }
        tm.initTile(x, y).setForeground(bgMap[7][0]);
        tm.initTile(x, y + 1).setForeground(bgMap[6][0]);
        tm.initTile(stop + 1, y).setForeground(bgMap[7][2]);
        tm.initTile(stop + 1, y + 1).setForeground(bgMap[6][2]);
    }
    
    private static void solidBlock(final int x, final int y) {
        tm.initTile(x, y).setForeground(imgMap[0][4], true);
    }
    
    private static void bumpableBlock(final int x, final int y) {
        final Tile block = tm.initTile(x, y);
        block.setForeground(imgMap[0][0], PlatformGame.TILE_BUMP);
        PlatformGame.bump.setViewFromForeground(block);
    }
    
    private static void breakableBlock(final int x, final int y) {
        tm.initTile(x, y).setForeground(imgMap[0][5], PlatformGame.TILE_BREAK);
    }
    
    private static void upBlock(final int x, final int y) {
        tm.initTile(x, y).setForeground(imgMap[0][6], PlatformGame.TILE_UPSLOPE);
    }
    
    private static void downBlock(final int x, final int y) {
        tm.initTile(x, y).setForeground(imgMap[0][7], PlatformGame.TILE_DOWNSLOPE);
    }
    
    private static void goalBlock(final int x, final int y) {
        tm.initTile(x, y).setForeground(imgMap[7][0], PlatformGame.TILE_BUMP);
    }
    
    private static void step(final int x, final int y, final int w, final int h) {
        // Will also want 1-way steps going up and 1-way down; same with ramps
        tm.initTile(x, y).setForeground(imgMap[3][0], true);
        final int stop = x + w + 1, ystop = y + h + 1;
        for (int j = y + 1; j < ystop; j++) {
            tm.initTile(x, j).setForeground(imgMap[2][0], true);
            tm.initTile(stop, j).setForeground(imgMap[2][2], true);
            for (int i = x + 1; i < stop; i++) {
                tm.initTile(i, j).setForeground(getDirtImage(), true);
            }
        }
        tm.initTile(x, ystop).setForeground(imgMap[1][0], true);
        for (int i = x + 1; i < stop; i++) {
            tm.initTile(i, ystop).setForeground(imgMap[1][1], true);
            tm.initTile(i, y).setForeground(getDirtImage(), true);
        }
        tm.initTile(stop, ystop).setForeground(imgMap[1][2], true);
        tm.initTile(stop, y).setForeground(imgMap[3][2], true);
    }
    
    private static void ramp(final int x, final int y, final int w, final int h) {
        final int fstop = x + w + h * 2, ystop = y + h;
        for (int jo = y; jo <= ystop; jo++) {
            final int jb = jo - y, stop = fstop - jb;
            if (jb != 0) {
                tm.initTile(x + jb, jo).setForeground(imgMap[3][3], PlatformGame.TILE_UPSLOPE);
                tm.initTile(stop + 1, jo).setForeground(imgMap[3][4], PlatformGame.TILE_DOWNSLOPE);
            }
            if (jo == ystop) {
                for (int i = x + jb + 1; i <= stop; i++) {
                    tm.initTile(i, jo).setForeground(imgMap[1][1], true);
                }
            } else {
                tm.initTile(x + jb + 1, jo).setForeground(imgMap[3][0], true);
                for (int i = x + jb + 2; i < stop; i++) {
                    tm.initTile(i, jo).setForeground(getDirtImage(), true);
                }
                tm.initTile(stop, jo).setForeground(imgMap[3][2], true);
            }
        }
    }
    
    private static void blockWall(final int x, final int y, final int w, final int h) {
    	for (int i = 0; i < w; i++) {
    		final int xi = x + i;
    		for (int j = 0; j < h; j++) {
    			solidBlock(xi, y + j);
    		}
    	}
    }
    
    private static void upBlockStep(final int x, final int y, final int w) {
    	for (int i = 0; i < w; i++) {
    		final int xi = x + i;
    		for (int j = 0; j <= i; j++) {
    			solidBlock(xi, y + j);
    		}
    	}
    }
    
    private static void downBlockStep(final int x, final int y, final int w) {
    	for (int i = 0; i < w; i++) {
    		final int xi = x + w - i - 1;
    		for (int j = 0; j <= i; j++) {
    			solidBlock(xi, y + j);
    		}
    	}
    }
    
    private static void naturalRise(final int x, final int y, final int w, final int h) {
        final int ystop = y + h;
        for (int j = y; j < ystop; j++) {
            tm.initTile(x, j).setBackground(imgMap[2][3]);
            tm.initTile(x + w + 1, j).setBackground(imgMap[2][4]);
        }
        final int stop = x + w;
        for (int i = x + 1; i <= stop; i++) {
            tm.initTile(i, ystop).setBackground(imgMap[1][1], PlatformGame.TILE_FLOOR);
            for (int j = y; j < ystop; j++) {
                tm.initTile(i, j).setBackground(getDirtImage());
            }
        }
        tm.initTile(x, ystop).setForeground(imgMap[1][3], PlatformGame.TILE_FLOOR);
        tm.initTile(stop + 1, ystop).setForeground(imgMap[1][4], PlatformGame.TILE_FLOOR);
    }
    
    private static void colorRise(final int x, final int y, final int w, final int h, final int _o) {
        final int o = _o * 2 + 2, o1 = o + 1, ystop = y + h;
        for (int j = y; j < ystop; j++) {
            tm.initTile(x, j).setBackground(imgMap[o1][5]);
            tm.initTile(x + w + 1, j).setBackground(imgMap[o1][7]);
        }
        final int stop = x + w;
        for (int i = x + 1; i <= stop; i++) {
            tm.initTile(i, ystop).setBackground(imgMap[o][6], PlatformGame.TILE_FLOOR);
            for (int j = y; j < ystop; j++) {
                tm.initTile(i, j).setBackground(imgMap[o1][6]);
            }
        }
        tm.initTile(x, ystop).setForeground(imgMap[o][5], PlatformGame.TILE_FLOOR);
        tm.initTile(stop + 1, ystop).setForeground(imgMap[o][7], PlatformGame.TILE_FLOOR);
    }
    
    private static void wall(final int x, final int y, final int w, final int h) {
        final int ystop = y + h, xstop = x + w + 1;
        for (int j = y; j < ystop; j++) {
            tm.initTile(x, j).setForeground(imgMap[4][0], true);
            for (int i = x + 1; i < xstop; i++) {
                tm.initTile(i, j).setForeground(imgMap[4][1], true);
            }
            tm.initTile(xstop, j).setForeground(imgMap[4][2], true);
        }
    }
    
    private static void slantUp(final int x, final int y, final int stop, final int h) {
        final int ystop = y + h, w = (stop + 1) * 2 - 1;
        for (int jo = y; jo < ystop; jo++) {
            final int jb = jo - y;
            tm.initTile(x - jb, jo).setForeground(imgMap[jb == (h - 1) ? 7 : 5][3]);
            for (int i = 1; i <= w; i++) {
                tm.initTile(x + i - jb, jo).setForeground(getDirtImage());
            }
            tm.initTile(x + w + 1 - jb, jo).setForeground(imgMap[4][4]);
        }
        for (int jb = 0; jb <= stop; jb++) {
            final int jo = jb + ystop;
            tm.initTile(x - 2 + jb, jo).setForeground(imgMap[3][3], PlatformGame.TILE_UPSLOPE_FLOOR);
            tm.initTile(x - 1 + jb, jo).setForeground(jb == stop ? imgMap[6][4] : imgMap[3][0]);
            if (jb < stop) {
                for (int i = jb; i <= w - 3 - jb; i++) {
                    tm.initTile(x + i, jo).setForeground(getDirtImage());
                }
                tm.initTile(x + w - 2 - jb, jo).setForeground(imgMap[4][4]);
            }
        }
    }
    
    private static TileMapImage getDirtImage() {
        return imgMap[Mathtil.rand(90) ? 2 : 3][1];
    }
    
    private static void pit(final int x, final int y, final int w) {
    	tm.initTile(x, 0).setForeground(imgMap[1][2], true);
    	final int stop = x + w + 1;
    	for (int i = x + 1; i < stop; i++) {
    		tm.removeTile(i, 0);
    	}
        tm.initTile(stop, 0).setForeground(imgMap[1][0], true);
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
}
