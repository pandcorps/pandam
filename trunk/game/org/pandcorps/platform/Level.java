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
import org.pandcorps.pandax.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.platform.Enemy.*;

public class Level {
    protected final static int ROOM_H = 256;
    
    protected final static PixelFilter terrainDarkener = new BrightnessPixelFilter((short) -40, (short) -24, (short) -32);
    
    protected static TileMapImage[] flashBlock;
    
    protected static Panroom room = null;
    protected static String theme = null;
    protected static Panmage timg = null;
    protected static Panmage bgimg = null;
    protected static TileMap tm = null;
    protected static TileMap bgtm1 = null;
    protected static TileMap bgtm2 = null;
    protected static TileMap bgtm3 = null;
    protected static TileMapImage[][] imgMap = null;
    protected static TileMapImage[][] bgMap = null;
    private static int w = 0;
    private static int ng = 0;
    private static int nt = 0;
    private static int floor = 0;
    protected static int numEnemies = 0;
    
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
    
    private final static BufferedImage loadTileImage() {
    	final BufferedImage tileImg = ImtilX.loadImage("org/pandcorps/platform/res/bg/Tiles.png", 128, null);
    	if (theme != null) {
    		final BufferedImage ext = ImtilX.loadImage("org/pandcorps/platform/res/bg/Tiles" + theme + ".png", false);
    		Imtil.copy(ext, tileImg, 0, 0, 128, 112, 0, 16);
    	}
    	return tileImg;
    }
    
    protected final static Panmage getTileImage() {
    	final BufferedImage tileImg = loadTileImage();
    	if (theme == null) {
    		applyDirtTexture(tileImg, 0, 16, 80, 128);
    	}
        return Pangine.getEngine().createImage("img.tiles", tileImg);
    }
    
    protected final static void loadLayers() {
        room = PlatformGame.createRoom(w, ROOM_H);
        final DynamicTileMap dtm = new DynamicTileMap("act.tilemap", room, ImtilX.DIM, ImtilX.DIM);
        tm = dtm;
        room.addActor(tm);
        
        timg = getTileImage();
        imgMap = tm.splitImageMap(timg);
        dtm.setTileListener(new BlockTileListener(imgMap));
        
        final Panlayer bg1 = PlatformGame.createParallax(room, 2);
        bgtm1 = new TileMap("act.bgmap1", bg1, ImtilX.DIM, ImtilX.DIM);
        bg1.addActor(bgtm1);
        BufferedImage backImg = ImtilX.loadImage("org/pandcorps/platform/res/bg/Hills" + Chartil.unnull(theme) + ".png", 128, null);
        BufferedImage terrain = getTerrainTexture();
        for (int z = 0; z < 3; z++) {
            if (z > 0) {
                terrain = getDarkenedTerrain(terrain);
            }
            final int yoff = z * 32;
            applyTerrainTexture(backImg, 0, yoff, 64, yoff + 32, terrain, getTerrainMask(z));
        }
        backImg = getColoredTerrain(backImg, 0, 0, 96, 96);
        bgimg = Pangine.getEngine().createImage("img.bg", backImg);
        bgMap = bgtm1.splitImageMap(bgimg);
        
        /*
        It would look strange if layers 1 and 3 moved without 2.
        So it's probably best if each layer's master is the one directly above it
        instead of basing all on the foreground with different divisors.
        */
        final Panlayer bg2 = PlatformGame.createParallax(bg1, 2);
        bgtm2 = new TileMap("act.bgmap2", bg2, ImtilX.DIM, ImtilX.DIM);
        bg2.addActor(bgtm2);
        bgtm2.setImageMap(bgtm1);
        
        final Panlayer bg3 = PlatformGame.createParallax(bg2, 2);
        bgtm3 = new TileMap("act.bgmap3", bg3, ImtilX.DIM, ImtilX.DIM);
        bg3.addActor(bgtm3);
        bgtm3.setImageMap(bgtm1);
        bgtm3.fillBackground(bgMap[0][6], 9, bgtm3.getHeight() - 9);
        bgtm3.fillBackground(bgMap[1][6], 8, 1);
        bgtm3.fillBackground(bgMap[2][6], 0, 8);
    }
    
    protected final static void loadLevel() {
    	final Builder b = new RandomBuilder();
    	w = b.getW();
    	nt = w / ImtilX.DIM;
    	ng = nt;
    	floor = 0;
    	loadLayers();
    	addPlayers(); // Add Players while floor has initial value before build() changes it
    	b.build();
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
        
        for (int i = 0; i < nt; i++) {
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
        
        final EnemyDefinition def = PlatformGame.enemies.get(0);
        new Enemy(def, 80, 64);
        new Enemy(def, 232, 48);
        new Enemy(def, 360, 16);
    }
    
    private static int bx;
    private static int px;
    
    protected final static class RandomBuilder implements Builder {
    	@Override
    	public int getW() {
    		return 3200;
    	}
    	
    	@Override
    	public void build() {
    	    loadTemplates();
    	    
    		buildBg(bgtm1, 4, 6, 0, false); // Nearest
    		buildBg(bgtm2, 7, 9, 2, false);
    		buildBg(bgtm3, 10, 12, 4, true); // Farthest
    		
    		final Goal goal = Mathtil.rand(goals);
    		ng = nt - goal.getWidth();
    		
    		px = 0;
    		for (bx = 8; bx < ng; ) {
    			/*
    			Raise/lower floor with 1-way ramps
    			Some templates should allow any other template on top of it
    			Some templates should allow decorations on top
    			Bonus blocks can go in front of background rises/slants
    			Block gap patterns, 2x2 block patterns
    			Natural step stairs
    			Valleys
    			Rises with ramps in them
    			Rises woven w/ pit edges
    			Slant groups
    			Block letter patterns
    			Checkered, diagonal stripe gem patterns
    			*/
    		    final Template template = Mathtil.rand(templates);
    		    template.plan();
    		    ground();
    		    if (bx < ng) {
    		    	template.build();
    		    }
    		    if (Mathtil.rand(33)) {
    		    	if (bx + 3 < ng) {
    		    		boolean up = Mathtil.rand();
    		    		final int h = Mathtil.randi(0, 2);
    		    		if (up) {
    		    			up = (floor + h) < 6;
    		    		} else {
    		    			up = (floor - h) < 1;
    		    		}
	    		    	if (up) {
	    		    		upStep(bx + 1, floor, h);
	    		    		floor += (h + 1);
	    		    	} else {
	    		    		floor -= (h + 1);
	    		    		downStep(bx + 1, floor, h);
	    		    	}
    		    	}
    		    	bx += 3;
    		    }
   		    	bx += Mathtil.randi(1, 4);
    		}
    		bx = nt;
    		ground();
    		goal.build();
    	}
    }
    
    private static void ground() {
    	final int stop = Math.min(bx + 1, nt - 1);
    	for (int i = px; i <= stop; i++) {
            tm.initTile(i, floor).setForeground(imgMap[1][1], true);
            for (int j = 0; j < floor; j++) {
            	tm.initTile(i, j).setForeground(getDirtImage(), true);
            }
        }
    	px = bx + 2;
    }
    
    private static void enemy(final int x, final int y, final int w) {
    	if (w < 3 || Mathtil.rand(40)) {
    		return;
    	}
    	new Spawner(tm.getTileWidth() * (x + Mathtil.randi(1, w - 2)), tm.getTileHeight() * y);
    	numEnemies++;
    }
    
    private final static ArrayList<Template> templates = new ArrayList<Template>();
    private final static ArrayList<Goal> goals = new ArrayList<Goal>();
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
        addTemplate(new NaturalRiseTemplate());
        addTemplate(new ColorRiseTemplate());
        addTemplate(new WallTemplate());
        addTemplate(new StepTemplate());
        addTemplate(new RampTemplate());
        addTemplate(new BushTemplate(), new TreeTemplate());
        addTemplate(new PitTemplate(), new BridgePitTemplate(), new BlockPitTemplate());
        addTemplate(new UpBlockStepTemplate(), new DownBlockStepTemplate(), new BlockWallTemplate(), new BlockGroupTemplate());
        addTemplate(new BlockBonusTemplate());
        addTemplate(new GemTemplate(), new GemMsgTemplate());
        addTemplate(new SlantTemplate(true), new SlantTemplate(false));
        goals.add(new SlantGoal());
    }
    
    private final static void addTemplate(final Template... a) {
    	templates.add(a.length == 1 ? a[0] : new ChoiceTemplate(a));
    }
    
    private abstract static class Goal {
    	protected abstract int getWidth();
    	
    	protected abstract void build();
    }
    
    private static class SlantGoal extends Goal {
    	private int stop;
    	private int h;
    	private int w;
    	
    	@Override
    	protected final int getWidth() {
    		stop = 1;
    		h = 3;
    		w = getSlantWidth(getSlantBase(stop), h);
    		return w;
    	}
    	
    	@Override
    	protected final void build() {
    		final int x = getSlantStart(ng, w, h, true);
    		slantUp(x, floor + 1, stop, h);
            goalBlock(x, floor + 8);
    	}
    }
    
    private abstract static class Template {
    	protected abstract void plan();
    	
        protected abstract void build();
    }
    
    private final static class ChoiceTemplate extends Template {
    	private final Template[] choices;
    	private Template curr = null;
    	
    	private ChoiceTemplate(final Template... choices) {
    		this.choices = choices;
    	}
    	
    	@Override
    	protected final void plan() {
    		curr = Mathtil.rand(choices);
    		curr.plan();
    	}
    	
    	@Override
    	protected final void build() {
    		curr.build();
    	}
    }
    
    private abstract static class RiseTemplate extends Template {
    	private int amt;
    	private int x;
    	
        @Override
        protected final void plan() {
        	x = bx;
            amt = Mathtil.randi(1, 3);
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
        }
        
        @Override
        protected final void build() {
            for (int i = 0; i < amt; i++) {
                final int r = Mathtil.randi(0, amt - 1);
                final int io = amt + i * 2, ro = amt + r * 2;
                swapScratch(io, ro);
                swapScratch(io + 1, ro + 1);
            }
            init();
            for (int i = 0; i < amt; i++) {
                final int xo = amt + i * 2, x = scratch[xo], y = floor + 1, w = scratch[xo + 1], h = scratch[amt - i - 1];
                rise(x, y, w, h);
                enemy(x, y + h + 1, w);
            }
            enemy(x, floor + 1, bx - x - 2);
        }
        
        protected void init() {
        }
        
        protected abstract void rise(final int x, final int y, final int w, final int h);
    }
    
    private final static class NaturalRiseTemplate extends RiseTemplate {
    	@Override
    	protected final void rise(final int x, final int y, final int w, final int h) {
    		naturalRise(x, y, w, h);
    	}
    }
    
    private static int[] colors = new int[3];
    private static int colorIndex = 0;
    
    private final static class ColorRiseTemplate extends RiseTemplate {
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
    
    private abstract static class SimpleTemplate extends Template {
    	private final int minW;
    	private final int maxW;
    	private final int ext;
    	protected int x;
    	protected int w;
    	
    	protected SimpleTemplate() {
    		this(0, 8);
    	}
    	
    	protected SimpleTemplate(final int minW, final int maxW) {
    		this(minW, maxW, 2);
    	}
    	
    	protected SimpleTemplate(final int minW, final int maxW, final int ext) {
    		this.minW = minW;
    		this.maxW = maxW;
    		this.ext = ext;
    	}
    	
        @Override
        protected final void plan() {
        	w = Mathtil.randi(minW, maxW);
        	x = bx;
        	bx += (w + ext);
        }
    }
    
    private final static class WallTemplate extends SimpleTemplate {
    	@Override
        protected final void build() {
    		final int y = floor + 1, h = Mathtil.randi(1, 3);
        	wall(x, y, w, h);
        	enemy(x, y + h, w);
        }
    }
    
    private final static class StepTemplate extends SimpleTemplate {
        @Override
        protected final void build() {
        	final int h = Mathtil.randi(0, 2);
        	step(x, floor, w, h);
        	enemy(x, floor + h + 2, w);
        }
    }
    
    private final static class RampTemplate extends Template {
    	private int w;
    	private int h;
    	private int x;
    	
        @Override
        protected final void plan() {
        	w = Mathtil.randi(0, 6);
        	h = Mathtil.randi(1, 4);
        	x = bx;
        	bx += (w + h * 2 + 2);
        }
        
        @Override
        protected final void build() {
        	ramp(x, floor, w, h);
        }
    }
    
    private final static class SlantTemplate extends Template {
        private final boolean up;
        private int stop;
        private int h;
        private int x;
        
        private SlantTemplate(final boolean up) {
            this.up = up;
        }
        
        @Override
        protected final void plan() {
            stop = Mathtil.randi(0, 2);
            h = Mathtil.randi(2, 4);
            final int w = getSlantBase(stop);
            x = getSlantStart(bx, w, h, up);
            bx += getSlantWidth(w, h);
        }
        
        @Override
        protected final void build() {
            slant(x, floor + 1, stop, h, up);
            enemy(x, floor + 1, bx - x - 2);
        }
    }
    
    private final static int getSlantBase(final int stop) {
    	return (stop + 1) * 2 - 1;
    }
    
    private final static int getSlantStart(final int x, final int w, final int h, final boolean up) {
        return x + (up ? (h - 1) : (w + 1));
    }
    
    private final static int getSlantWidth(final int w, final int h) {
        return Math.max(h + w + 1, 2);
    }
    
    private final static class PitTemplate extends SimpleTemplate {
    	protected PitTemplate() {
    		super(2, 4);
    	}
    	
        @Override
        protected final void build() {
        	pit(x, floor, w);
        }
    }
    
    private final static class BridgePitTemplate extends SimpleTemplate {
    	protected BridgePitTemplate() {
    		super(5, 10);
    	}
    	
        @Override
        protected final void build() {
        	pit(x, floor, w);
        	final int stop = x + w;
        	for (int i = x + 2; i < stop; i++) {
        		solidBlock(i, floor + 3);
        	}
        	enemy(x + 2, floor + 4, w);
        }
    }
    
    private final static class UpBlockStepTemplate extends SimpleTemplate {
    	protected UpBlockStepTemplate() {
    		super(1, 3, 0);
    	}
    	
        @Override
        protected final void build() {
        	upBlockStep(x, floor + 1, w, Mathtil.rand());
        }
    }
    
    private final static class DownBlockStepTemplate extends SimpleTemplate {
    	protected DownBlockStepTemplate() {
    		super(1, 3, 0);
    	}
    	
        @Override
        protected final void build() {
        	downBlockStep(x, floor + 1, w, Mathtil.rand());
        }
    }
    
    private final static class BlockWallTemplate extends SimpleTemplate {
    	protected BlockWallTemplate() {
    		super(1, 8, 0);
    	}
    	
        @Override
        protected final void build() {
        	final int y = floor + 1, h = Mathtil.randi(1, 3);
        	blockWall(x, y, w, h);
        	enemy(x, y + h, w);
        }
    }
    
    private abstract static class BlockBaseTemplate extends Template {
    	private int x;
    	protected int wSlope;
    	private int wFlat;
    	
    	@Override
        protected final void plan() {
        	wSlope = Mathtil.randi(1, 3);
        	wFlat = Mathtil.randi(getMinFlat(), getMaxFlat());
        	x = bx;
        	bx += (wSlope * 2 + wFlat);
        }
        
        @Override
        protected final void build() {
        	final boolean ramp = Mathtil.rand();
        	final int f = floor + 1;
        	upBlockStep(x, f, wSlope, ramp);
        	x += wSlope;
        	center(x, wFlat, ramp);
        	downBlockStep(x + wFlat, f, wSlope, ramp);
        }
        
        protected abstract int getMinFlat();
        
        protected abstract int getMaxFlat();
        
        protected abstract void center(final int x, final int w, final boolean ramp);
    }
    
    private final static class BlockGroupTemplate extends BlockBaseTemplate {
    	@Override
    	protected final int getMinFlat() {
    		return 0;
    	}
        
    	@Override
        protected final int getMaxFlat() {
    		return 6;
    	}
        
    	@Override
    	protected final void center(final int x, final int w, final boolean ramp) {
    		blockWall(x, floor + 1, w, wSlope + (ramp ? 0 : 1));
    	}
    }
    
    private final static class BlockPitTemplate extends BlockBaseTemplate {
    	@Override
    	protected final int getMinFlat() {
    		return 2;
    	}
        
    	@Override
        protected final int getMaxFlat() {
    		return 4;
    	}
    	
    	@Override
    	protected final void center(final int x, final int w, final boolean ramp) {
    		pit(x - 1, floor, w);
    	}
    }
    
    private final static class BlockBonusTemplate extends SimpleTemplate {
    	protected BlockBonusTemplate() {
    		super(1, 8, 0);
    	}
    	
        @Override
        protected final void build() {
        	final int stop = x + w;
        	final boolean flag = Mathtil.rand();
        	for (int i = x; i < stop; i++) {
        		if (flag) {
        			bumpableBlock(i, floor + 3);
        		} else {
        			breakableBlock(i, floor + 3);
        		}
        	}
        	enemy(x, floor + 4, w);
        }
    }
    
    private final static class GemTemplate extends SimpleTemplate {
    	protected GemTemplate() {
    		super(1, 10, 0);
    	}
    	
    	@Override
		protected final void build() {
    		final int stop = x + w;
    		final boolean block = Mathtil.rand();
    		for (int i = x; i < stop; i++) {
    			if (block) {
    				solidBlock(i, floor + 3);
    				gem(i, floor + 4);
    			} else {
    				gem(i, floor + 3);
    			}
    		}
    	}
    }
    
    private final static class GemMsgTemplate extends Template {
    	private int x;
    	private String msg;
    	
		@Override
		protected final void plan() {
			x = bx;
			msg = Mathtil.rand("PLAYER", "GEMS", "HURRAY", "GO", "YAY", "GREAT", "PERFECT");
			if ("PLAYER".equals(msg)) {
				msg = Mathtil.rand(PlatformGame.pcs).getName();
			}
			bx += gemMsg(x, floor + 1, msg, false) + 2;
		}

		@Override
		protected final void build() {
			gemMsg(x + 1, floor + 1, msg, true);
		}
    }
    
    private final static class BushTemplate extends SimpleTemplate {
    	protected BushTemplate() {
    		super(0, 6);
    	}
    	
    	@Override
        protected final void build() {
        	bush(x, floor + 1, w);
        	enemy(x, floor + 1, w);
        }
    }
    
    private final static class TreeTemplate extends SimpleTemplate {
    	protected TreeTemplate() {
    		super(4, 4, 0);
    	}
    	
    	@Override
        protected final void build() {
        	tree(x, floor + 1);
        	enemy(x, floor + 1, w);
        }
    }
    
    private static void buildBg(final TileMap tm, final int miny, final int maxy, final int iy, final boolean cloud) {
    	final int maxx = tm.getWidth() + 1;
    	int x = Mathtil.randi(-1, 4);
    	boolean c = Mathtil.rand();
    	while (x < maxx) {
    		final int y = Mathtil.randi(miny, maxy), w = Mathtil.randi(0, 8);
    		int cx = -100, cy = cx, cw = cx, g = Mathtil.randi(3, 5);
    		boolean cb = false;
    		if (cloud) {
    			if (c) {
    				final int o = Mathtil.randi(1, 3);
    				if (Mathtil.rand()) {
    					cx = x;
    					x += o;
    				} else {
    					cx = x + o;
    					g += o;
    				}
    				if (w == 0 || w + 1 == o) {
    					cy = y - Mathtil.randi(2, 4);
    				} else {
    					cy = y - (Mathtil.rand() ? 0 : 2);
    				}
    				cw = Math.max(1, w);
    				cb = Mathtil.rand();
    			}
    			c = !c;
    		}
    		if (cb && cx != -100) {
    			cloud(tm, cx, cy, cw);
    		}
    		hill(tm, x, y, w, Mathtil.rand() ? 0 : 3, iy);
    		if (!cb && cx != -100) {
    			cloud(tm, cx, cy, cw);
    		}
    		x += (w + g);
    	}
    }
    
    private static void addPlayers() {
    	final int size = PlatformGame.pcs.size();
        final ArrayList<Player> players = new ArrayList<Player>(size);
        for (int i = 0; i < size; i++) {
            final Player player = new Player(PlatformGame.pcs.get(i));
            room.addActor(player);
            PlatformGame.setPosition(player, 40 + (20 * i), (floor + 1) * 16, PlatformGame.DEPTH_PLAYER);
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
            setBg(tm, i, y, bgMap, 7, 1);
            setBg(tm, i, y + 1, bgMap, 6, 1);
        }
        setFg(tm, x, y, bgMap, 7, 0);
        setFg(tm, x, y + 1, bgMap, 6, 0);
        setFg(tm, stop + 1, y, bgMap, 7, 2);
        setFg(tm, stop + 1, y + 1, bgMap, 6, 2);
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
    	step(x, y, w, h, 1);
    }
    
    private static void upStep(final int x, final int y, final int h) {
    	step(x, y, 0, h, 0);
    }
    
    private static void downStep(final int x, final int y, final int h) {
    	step(x, y, -1, h, 2);
    }
    
    private static void step(final int x, final int y, final int w, final int h, final int mode) {
        // Will also want 1-way steps going up and 1-way down; same with ramps
    	if (mode != 2) {
    		tm.initTile(x, y).setForeground(imgMap[3][0], true);
    	}
        final int stop = x + w + 1, ystop = y + h + 1;
        for (int j = y + 1; j < ystop; j++) {
        	if (mode != 2) {
        		tm.initTile(x, j).setForeground(imgMap[2][0], true);
        	}
        	if (mode != 0) {
        		tm.initTile(stop, j).setForeground(imgMap[2][2], true);
        	}
        	if (mode == 1) {
	            for (int i = x + 1; i < stop; i++) {
	                tm.initTile(i, j).setForeground(getDirtImage(), true);
	            }
        	}
        }
        if (mode != 2) {
        	tm.initTile(x, ystop).setForeground(imgMap[1][0], true);
        }
        if (mode == 1) {
	        for (int i = x + 1; i < stop; i++) {
	            tm.initTile(i, ystop).setForeground(imgMap[1][1], true);
	            tm.initTile(i, y).setForeground(getDirtImage(), true);
	        }
        }
        if (mode != 0) {
	        tm.initTile(stop, ystop).setForeground(imgMap[1][2], true);
	        tm.initTile(stop, y).setForeground(imgMap[3][2], true);
        }
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
    
    private static void upBlockStep(final int x, final int y, final int w, final boolean ramp) {
    	for (int i = 0; i < w; i++) {
    		final int xi = x + i;
    		for (int j = 0; j <= i; j++) {
    			if (ramp && j == i) {
    				upBlock(xi, y + j);
    			} else {
    				solidBlock(xi, y + j);
    			}
    		}
    	}
    }
    
    private static void downBlockStep(final int x, final int y, final int w, final boolean ramp) {
    	for (int i = 0; i < w; i++) {
    		final int xi = x + w - i - 1;
    		for (int j = 0; j <= i; j++) {
    			if (ramp && j == i) {
    				downBlock(xi, y + j);
    			} else {
    				solidBlock(xi, y + j);
    			}
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
    
    // x - h + 2 to x + ((stop + 1) * 2 - 1) + 1
    private static void slantUp(final int x, final int y, final int stop, final int h) {
        slant(x, y, stop, h, true);
    }
    
    private static void slant(final int x, final int y, final int stop, final int h, final boolean up) {
        final int ystop = y + h, w = getSlantBase(stop), m, c1, c2, c3;
        final byte b;
        if (up) {
            m = 1;
            c1 = 3;
            c2 = 4;
            c3 = 0;
            b = PlatformGame.TILE_UPSLOPE_FLOOR;
        } else {
            m = -1;
            c1 = 4;
            c2 = 3;
            c3 = 2;
            b = PlatformGame.TILE_DOWNSLOPE_FLOOR;
        }
        for (int jo = y; jo < ystop; jo++) {
            final int jb = jo - y;
            tm.initTile(x - m * jb, jo).setForeground(imgMap[jb == (h - 1) ? 7 : 5][c1]);
            for (int i = 1; i <= w; i++) {
                tm.initTile(x + m * (i - jb), jo).setForeground(getDirtImage());
            }
            tm.initTile(x + m * (w + 1 - jb), jo).setForeground(imgMap[4][c2]);
        }
        for (int jb = 0; jb <= stop; jb++) {
            final int jo = jb + ystop, off = jb + 3 - h;
            tm.initTile(x + m * (off - 2), jo).setForeground(imgMap[3][c1], b);
            tm.initTile(x + m * (off - 1), jo).setForeground(jb == stop ? imgMap[6][c2] : imgMap[3][c3]);
            if (jb < stop) {
                for (int i = jb; i <= w - 3 - jb; i++) {
                    tm.initTile(x + m * (i + 3 - h), jo).setForeground(getDirtImage());
                }
                tm.initTile(x + m * (w + 1 - h - jb), jo).setForeground(imgMap[4][c2]);
            }
        }
    }
    
    private static TileMapImage getDirtImage() {
        return imgMap[Mathtil.rand(90) ? 2 : 3][1];
    }
    
    private static void pit(final int x, final int y, final int w) {
    	final int stop = x + w + 1;
    	for (int j = 0; j <= y; j++) {
    		final int iy = (j == y) ? 1 : 2;
	    	tm.initTile(x, j).setForeground(imgMap[iy][2], true);
	    	for (int i = x + 1; i < stop; i++) {
	    		tm.removeTile(i, j);
	    	}
	        tm.initTile(stop, j).setForeground(imgMap[iy][0], true);
    	}
    }
    
    private static void bush(final int x, final int y, final int w) {
        tm.initTile(x, y).setForeground(imgMap[1][5]);
        final int stop = x + w;
        for (int i = x + 1; i <= stop; i++) {
            tm.initTile(i, y).setForeground(imgMap[1][6]);
        }
        tm.initTile(stop + 1, y).setForeground(imgMap[1][7]);
    }
    
    private static void tree(final int x, final int y) {
    	for (int j = 0; j < 2; j++) {
    		tm.initTile(x + 1, y + j).setForeground(imgMap[7][1]);
    		tm.initTile(x + 2, y + j).setForeground(imgMap[7][2]);
    		tm.initTile(x + 1 + j, y + 2).setForeground(imgMap[6][2]);
    		tm.initTile(x + 1 + j, y + 3).setForeground(imgMap[5][2]);
    		tm.initTile(x + j, y + 3 + j).setForeground(imgMap[5][0]);
    		tm.initTile(x + 3 - j, y + 3 + j).setForeground(imgMap[5][1]);
    	}
    	tm.initTile(x, y + 2).setForeground(imgMap[6][0]);
		tm.initTile(x + 3, y + 2).setForeground(imgMap[6][1]);
    }
    
    private static void gem(final int x, final int y) {
        final Gem gem = new Gem();
        gem.setPosition(tm.initTile(x, y));
        room.addActor(gem);
    }
    
    private final static String[] gemFont = {
    	" *\n" +
        "* *\n" +
        "***\n" +
        "* *\n" +
        "* *",
        
        "**\n" +
        "* *\n" +
        "**\n" +
        "* *\n" +
        "**",
        
        "***\n" +
        "*\n" +
        "*\n" +
        "*\n" +
        "***",
        
        "**\n" +
        "* *\n" +
        "* *\n" +
        "* *\n" +
        "**",
        
        "***\n" +
        "*\n" +
        "***\n" +
        "*\n" +
        "***",
        
        "***\n" +
        "*\n" +
        "***\n" +
        "*\n" +
        "*",
        
        " ***\n" +
        "*\n" +
        "* **\n" +
        "*  *\n" +
        " ***",
        
        "* *\n" +
        "* *\n" +
        "***\n" +
        "* *\n" +
        "* *",
        
        "***\n" +
        " *\n" +
        " *\n" +
        " *\n" +
        "***",
        
        "  *\n" +
        "  *\n" +
        "  *\n" +
        "* *\n" +
        "***",
        
        "*  *\n" +
        "* *\n" +
        "** \n" +
        "* *\n" +
        "*  *",
        
        "*\n" +
        "*\n" +
        "*\n" +
        "*\n" +
        "***",
        
        "*   *\n" +
        "*   *\n" +
        "** **\n" +
        "* * *\n" +
        "* * *",
        
        "*  *\n" +
        "** *\n" +
        "* **\n" +
        "*  *\n" +
        "*  *",
        
        "***\n" +
        "* *\n" +
        "* *\n" +
        "* *\n" +
        "***",
        
        "**\n" +
        "* *\n" +
        "**\n" +
        "*\n" +
        "*",
        
        "****\n" +
        "*  *\n" +
        "*  *\n" +
        "* *\n" +
        "** *",
        
        "**\n" +
        "* *\n" +
        "**\n" +
        "* *\n" +
        "* *",
        
        "***\n" +
        "*\n" +
        "***\n" +
        "  *\n" +
        "***",
        
        "***\n" +
        " *\n" +
        " *\n" +
        " *\n" +
        " *",
        
        "* *\n" +
        "* *\n" +
        "* *\n" +
        "* *\n" +
        "***",
        
        "* *\n" +
        "* *\n" +
        "* *\n" +
        "* *\n" +
        " *",
        
        "* * *\n" +
        "* * *\n" +
        "** **\n" +
        "** **\n" +
        "*   *",
        
        "* *\n" +
        "* *\n" +
        " *\n" +
        "* *\n" +
        "* *",
        
        "* *\n" +
        "* *\n" +
        "***\n" +
        " *\n" +
        " *",
        
        "***\n" +
        "  *\n" +
        " *\n" +
        "*\n" +
        "***"
    };
    
    private static int gemMsg(final int x, final int y, final String msg, final boolean render) {
    	final int size = msg.length();
    	int xc = x;
    	for (int i = 0; i < size; i++) {
    		xc += (1 + gemChr(xc, y, msg.charAt(i), render));
    	}
    	return xc - x - 1;
    }
    
    private static int gemChr(final int x, final int y, final char chr, final boolean render) {
    	final int c = java.lang.Character.toUpperCase(chr) - 'A';
    	if (c < 0 || c >= gemFont.length) {
    		return -1;
    	}
    	final String s = gemFont[c];
    	final int size = s.length();
    	int xc = x, yc = y + 4, max = 0;
    	for (int i = 0; i < size; i++) {
    		final char t = s.charAt(i);
    		if (t == '\n') {
    			max = Math.max(max, xc - x);
    			xc = x;
    			yc--;
    			continue;
    		} else if (render && t == '*') {
    			gem(xc, yc);
    		}
    		xc++;
    	}
    	return Math.max(max, xc - x);
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
