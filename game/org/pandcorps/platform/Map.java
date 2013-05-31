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
import org.pandcorps.game.*;
import org.pandcorps.game.core.ImtilX;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.FinPanple;
import org.pandcorps.pandax.text.Pantext;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.TileMapImage;

public class Map {
	private final static byte TILE_HORIZ = 2;
	private final static byte TILE_VERT = 3;
	private final static byte TILE_LEFTUP = 4;
	private final static byte TILE_RIGHTUP = 5;
	private final static byte TILE_LEFTDOWN = 6;
	private final static byte TILE_RIGHTDOWN = 7;
	private final static byte TILE_MARKER = 8;
	
	/*private final static String[] ADJECTIVES =
		{ "Bright", "Bubbly", "Cheery", "Emerald", "Enchanted", "Fragrant", "Green", "Fun", "Happy", "Incredible", "Merry",
		"Mystic", "Sugar", "Sunny", "Sweet", "Tender", "Tranquil", "Verdant", "Vibrant", "Wonder" };
	private final static String[] NATURES = { "Beat", "Bliss", "Bounce", "Candy", "Dash", "Flower", "Grass", "Harmony", "Hill",
		"Jump", "Meadow", "Melody", "Mound", "Music", "Petal", "Plains", "Rhythm", "Rise", "Run", "Rush", "Shine" };
	private final static String[] PLACES = { "Area", "Country", "Island", "Kingdom", "Land", "Realm", "World", "Zone" };*/
	
	/*private final static String[] NAME0 = { "fur" };
	private final static String[] NAMEH = { "b", "d", "g", "m", "n", "p" }; // 1=H, 4=S; or vice versa
	private final static String[] NAME2 = { "a", "e", "o", "u" };
	private final static String[] NAME3 = { "l", "n", "r", "" };
	private final static String[] NAMES = { "s", "th", "v" };
	private final static String[] NAMEC = { "br", "dr", "mpr", "ndr", "pr", "str" };
	private final static String[] NAME5 = { "and", "ay", "esse", "eth", "ia", "ing", "ion", "ior", "ire", "oft", "old", "om", "osh" };
	private final static Namer nmr = Namer.get( // 8640
	    Namer.get(NAME0, NAMEH, NAME2, NAME3, NAMES, NAME5),
	    Namer.get(NAME0, NAMES, NAME2, NAME3, NAMEH, NAME5),
	    Namer.get(NAME0, NAMEH, NAME2, NAMEC, NAME5));*/
	
	private final static String[] NAME0 = { "fur" };
	private final static String[] NAME1 = { "b", "g", "m", "n", "v", "w" };
	private final static String[] NAME2 = { "aladr", "alend", "andr", "ard", "eld", "eleb", "empr", "endl", "ill", "istr", "othel" };
	private final static String[] NAME3 = { "eth", "ia", "ing", "ion", "ost" };
	private final static Namer nmr = Namer.get(NAME0, NAME1, NAME2, NAME3);
    
    /*private final static String[] ADJECTIVES = { "brav", "bright", "fair", "good", "grand", "green", "kind", "north", "sweet", "verd" };
    private final static String[] NOUNS = { "beat", "bliss", "grass", "hill", "mead", "mound", "plain" };
    private final static String[] VERBS = { "bloom", "bound", "dash", "grow", "leap", "ris", "runn", "rush", "shin" };
    private final static String[] LINK_ADJ = { "al", "em", "est", "ing" };
    private final static String[] LINK_NON = { "al", "em", "en", "ing" };
    private final static String[] LINK_VRB = { "al", "em", "er", "ing" };
    private final static String[] PLACES = { "berg", "by", "croft", "fold", "gard", "ham", "holt", "march", "land", "nesse", "port", "shire", "stead", "strand", "thorp", "ton" };
    private final static Namer nmr = Namer.get(
        Namer.get(ADJECTIVES, LINK_ADJ, PLACES),
        Namer.get(NOUNS, LINK_NON, PLACES),
        Namer.get(VERBS, LINK_VRB, PLACES));
    // burgh, field, heim, town
    // bloomingberg, blooming-gard*/
	
	protected final static int bgTexture = 0;
	protected final static int bgColor = 1;
	
	private final static HashMap<Pair<Integer, Integer>, Boolean> open = new HashMap<Pair<Integer, Integer>, Boolean>();
	private static int column = 2;
	private static int row = 6;
	private static boolean first = true;
	
	private static Panroom room = null;
	private static Panmage timg = null;
	private static DynamicTileMap tm = null;
	private static TileMapImage[][] imgMap = null;
	private static TileMapImage water = null;
	private static TileMapImage base = null;
	
	protected final static class MapScreen extends Panscreen {
		@Override
        protected final void load() throws Exception {
			loadMap();
			PlatformGame.fadeIn(room);
		}
		
		@Override
	    protected final void destroy() {
	        Panmage.destroy(timg);
	    }
	}
	
	protected final static class Marker extends Panctor {
		private Marker(final boolean open) {
		    if (open) {
		        setView(PlatformGame.markerDefeated);
		    } else {
		        setView(PlatformGame.marker);
		    }
		}
	}
	
	protected final static class Player extends TileWalker {
		private boolean disabled = false;
		
		{
			setView(PlatformGame.guySouth);
			setSpeed(2);
		}
		
		@Override
		protected void onStill() {
			if (disabled) {
				return;
			}
			final Panteraction interaction = Pangine.getEngine().getInteraction();
			// Similar to Guy4Controller
	        if (interaction.KEY_DOWN.isActive()) {
	            go(Direction.South);
	        } else if (interaction.KEY_UP.isActive()) {
	            go(Direction.North);
	        } else if (interaction.KEY_LEFT.isActive()) {
	            go(Direction.West);
	        } else if (interaction.KEY_RIGHT.isActive()) {
	            go(Direction.East);
	        } else if (interaction.KEY_SPACE.isActive()) {
	        	/*if (room.getBlendColor().getA() > Pancolor.MIN_VALUE) {
	        		return;
	        	}*/
	            if (isOpen(getTile())) {
	                return;
	            }
	        	disabled = true;
	        	PlatformGame.fadeOut(room, new PlatformGame.PlatformScreen());
			}
		}
		
		protected boolean go(final Direction d0) {
		    final Tile t0 = getTile();
		    Tile t = t0;
		    Direction d = d0;
		    while (true) {
    		    t = t.getNeighbor(d);
    		    if (t == null || t.isSolid()) {
    		        return false;
    		    }
    		    final byte b = t.getBehavior();
    		    switch (b) {
    		        case TILE_MARKER :
        		        if (isOpen(t0) || isOpen(t)) {
        		            walk(d0);
        		            return true;
        		        }
        		        return false;
    		        case TILE_VERT :
    		        case TILE_HORIZ :
    		            // d stays the same
    		            break;
    		        case TILE_LEFTUP :
    		            d = getOther(d, Direction.West, Direction.North);
    		            break;
    		        case TILE_LEFTDOWN :
                        d = getOther(d, Direction.West, Direction.South);
                        break;
    		        case TILE_RIGHTUP :
                        d = getOther(d, Direction.East, Direction.North);
                        break;
    		        case TILE_RIGHTDOWN :
                        d = getOther(d, Direction.East, Direction.South);
                        break;
    		    }
		    }
		}
		
		private final Direction getOther(final Direction c, final Direction o1, final Direction o2) {
		    return c.getOpposite() == o1 ? o2 : o1;
		}
		
		@Override
	    protected void onFace(final Direction oldDir, final Direction newDir) {
			if (newDir == Direction.North) {
				changeView(PlatformGame.guyNorth);
			} else if (newDir == Direction.East) {
			    changeView(PlatformGame.guyEast);
			} else if (newDir == Direction.West) {
			    changeView(PlatformGame.guyWest);
			} else if (newDir == Direction.South) {
			    changeView(PlatformGame.guySouth);
			}
	    }
		
		@Override
		protected void onWalked() {
		    final Tile t = getTile();
			final byte b = t.getBehavior();
			switch (b) {
				case TILE_MARKER :
				    row = t.getRow();
				    column = t.getColumn();
				    changeView(PlatformGame.guySouth);
					return;
				case TILE_VERT : {
					final Direction d1 = getDirection();
					final byte b2 = getDestination(d1).getBehavior();
					if (b2 == TILE_LEFTUP || b2 == TILE_LEFTDOWN) {
						walk(Direction.West, d1);
					} else if (b2 == TILE_RIGHTUP || b2 == TILE_RIGHTDOWN) {
						walk(Direction.East, d1);
					} else {
						walk(d1);
					}
					return;
				}
				case TILE_HORIZ : {
					final Direction d1 = getDirection();
					final byte b2 = getDestination(d1).getBehavior();
					if (b2 == TILE_LEFTUP || b2 == TILE_RIGHTUP) {
						walk(Direction.North, d1);
					} else if (b2 == TILE_LEFTDOWN || b2 == TILE_RIGHTDOWN) {
						walk(Direction.South, d1);
					} else {
						walk(d1);
					}
					return;
				}
			}
	    }
	}
	
	private final static TileMapImage getBaseImage() {
		return Mathtil.rand(75) ? base : imgMap[4][Mathtil.randi(0, 5)];
	}
	
	private final static Pair<Integer, Integer> getKey(final Tile t) {
	    return Pair.get(Integer.valueOf(t.getRow()), Integer.valueOf(t.getColumn()));
	}
	
	private final static boolean isOpen(final Tile t) {
	    return open.get(getKey(t)) != null;
    }
	
	private final static void loadMap() {
		final Pangine engine = Pangine.getEngine();
		PlatformGame.room.destroy();
		PlatformGame.player = null;
		room = engine.createRoom(Pantil.vmid(), new FinPanple(256, 192, 0));
		PlatformGame.room = room;
		Pangame.getGame().setCurrentRoom(room);
		tm = new DynamicTileMap("act.tilemap", room, ImtilX.DIM, ImtilX.DIM);
		room.addActor(tm);
		BufferedImage tileImg = ImtilX.loadImage("org/pandcorps/platform/res/bg/Map.png", 128, null);
		PlatformGame.applyDirtTexture(tileImg, 48, 0, 96, 16);
		final BufferedImage terrain = PlatformGame.getDarkenedTerrain(PlatformGame.getTerrainTexture());
		PlatformGame.applyTerrainTexture(tileImg, 48, 32, 96, 48, terrain, PlatformGame.getTerrainMask(1));
		tileImg = PlatformGame.getColoredTerrain(tileImg, 48, 32, 48, 16);
		timg = engine.createImage("img.map", tileImg);
		tm.setImageMap(timg);
		imgMap = tm.splitImageMap();
		final MapTileListener mtl = new MapTileListener(6);
		for (int y = 0; y <= 2; y++) {
			mtl.put(imgMap[5 + y][6], imgMap[5 + ((y + 1) % 3)][6]);
		}
		tm.setTileListener(mtl);
		water = imgMap[5][6];
		base = imgMap[1][1];
		tm.fillBackground(water, true);
		for (int i = 2; i < 14; i++) {
			for (int j = 1; j < 4; j++) {
				tm.initTile(i, j).setBackground(imgMap[0][4]);
			}
			tm.initTile(i, 1).setForeground(imgMap[1][4]);
			tm.initTile(i, 3).setForeground(imgMap[2][1]);
			for (int j = 4; j < 10; j++) {
				tm.initTile(i, j).setBackground(getBaseImage());
			}
			tm.initTile(i, 10).setForeground(imgMap[0][1]);
		}
		mountain(5, 11);
		mountain(10, 9);
		for (int j = 1; j < 4; j++) {
			tm.initTile(1, j).setBackground(imgMap[0][3]);
			tm.initTile(14, j).setBackground(imgMap[0][5]);
		}
		tm.initTile(1, 1).setForeground(imgMap[1][3]);
		tm.initTile(14, 1).setForeground(imgMap[1][5]);
		for (int j = 4; j < 10; j++) {
			tm.initTile(1, j).setForeground(imgMap[1][0]);
			tm.initTile(14, j).setForeground(imgMap[1][2]);
		}
		tm.initTile(1, 3).setForeground(imgMap[2][0]);
		tm.initTile(14, 3).setForeground(imgMap[2][2]);
		tm.initTile(1, 10).setForeground(imgMap[0][0]);
		tm.initTile(14, 10).setForeground(imgMap[0][2]);
		
		final Tile t = tm.getTile(column, row);
        if (first) {
            first = false;
        } else {
            open.put(getKey(t), Boolean.TRUE);
        }
		
		marker(2, 6);
		tm.initTile(2, 7).setBackground(imgMap[3][2], TILE_VERT);
		tm.initTile(2, 8).setBackground(imgMap[3][6], TILE_RIGHTDOWN);
		tm.initTile(3, 8).setBackground(imgMap[3][1], TILE_HORIZ);
		marker(4, 8);
		for (int i = 3; i < 6; i++) {
			tm.initTile(i, 6).setBackground(imgMap[3][1], TILE_HORIZ);
		}
		marker(6, 6);
		
		final Player player = new Player();
		player.setPosition(t);
		player.getPosition().setZ(tm.getForegroundDepth() + 1);
		room.addActor(player);
		
		final Pantext name = new Pantext("map.name", PlatformGame.font, generateName());
		name.getPosition().set(PlatformGame.SCREEN_W / 2, 0);
		name.centerX();
		final Panlayer hud = PlatformGame.addHud(room, new CallSequence() {@Override protected String call() {
            return String.valueOf(PlatformGame.pc.getGems());}});
		hud.addActor(name);
	}
	
	private static void mountain(final int x, final int y) {
		setForeground(x, y, 2, 3);
		setForeground(x + 1, y, 2, 4);
		setForeground(x + 2, y, 2, 5);
		final int stop = x + 2, yshadow = y - 1;
		for (int i = x; i <= stop; i++) {
			final Tile t = tm.initTile(i, yshadow);
			final TileMapImage b = getBaseImage();
			t.setBackground(b);
			t.setForeground(b);
		}
	}
	
	private static void setForeground(final int x, final int y, final int ij, final int ii) {
		final Tile t = tm.initTile(x, y);
		t.setForeground(imgMap[ij][ii]);
		if (DynamicTileMap.getRawBackground(t) != water) {
			t.setBackground(base);
		}
	}
	
	private static void marker(final int i, final int j) {
		final Tile tile = tm.initTile(i, j);
		tile.setBackground(imgMap[3][0], TILE_MARKER);
		final Marker m = new Marker(isOpen(tile));
		//m.setPosition(tile);
		m.getPosition().set(tile.getPosition());
		room.addActor(m);
	}
	
	private final static String generateName() {
		//return Mathtil.rand(ADJECTIVES) + ' ' + Mathtil.rand(NATURES) + ' ' + Mathtil.rand(PLACES);
	    return nmr.get();
	}
	
	public final static void main(final String[] args) {
	    nmr.printDemo();
	}
}
