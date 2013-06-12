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
    protected static TileMapImage[][] imgMap = null;
    
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
    
    protected final static void loadLevel() {
        final Pangine engine = Pangine.getEngine();
        PlatformGame.room.destroy();
        final int w = 768;
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
        final Panlayer bg2 = PlatformGame.createParallax(bg1, 2);
        final TileMap bgtm2 = new TileMap("act.bgmap2", bg2, ImtilX.DIM, ImtilX.DIM);
        bg2.addActor(bgtm2);
        bgtm2.setImageMap(bgimg);
        hill(bgtm2, bgMap, 0, 6, 4, 3, 2);
        hill(bgtm2, bgMap, 7, 8, 7, 0, 2);
        
        final Panlayer bg3 = PlatformGame.createParallax(bg2, 2);
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
        
        step(13, 0, 1, 1);
        bush(4, 1, 0);
        ramp(27, 0, 6, 3);
        wall(32, 4, 2, 2);
        naturalRise(19, 1, 5, 3);
        naturalRise(18, 1, 1, 1);
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
        final int size = PlatformGame.pcs.size();
        final ArrayList<Player> players = new ArrayList<Player>(size);
        for (int i = 0; i < size; i++) {
            final Player player = new Player(PlatformGame.pcs.get(i));
            room.addActor(player);
            PlatformGame.setPosition(player, 40 + (20 * i), 16, PlatformGame.DEPTH_PLAYER);
            players.add(player);
        }
        Pangine.getEngine().track(Panverage.getArithmeticMean(players));
        
        new Enemy(80, 64);
        new Enemy(232, 48);
        new Enemy(360, 16);
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
        // f 39
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
    
    private static void naturalRise(final int x, final int y, final int w, final int h) {
        colorRise(x, y, w, h, 6);
        if (Level.class != null) {
            return;
        }
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
    
    private static void colorRise(final int x, final int y, final int w, final int h, final int o) {
        final int o1 = o + 1, ystop = y + h;
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
