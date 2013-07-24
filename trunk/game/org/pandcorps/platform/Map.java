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
import org.pandcorps.game.*;
import org.pandcorps.game.core.ImtilX;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.text.Pantext;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.TileMapImage;
import org.pandcorps.platform.Player.PlayerContext;

public class Map {
	private final static byte TILE_HORIZ = 2;
	private final static byte TILE_VERT = 3;
	private final static byte TILE_LEFTUP = 4;
	private final static byte TILE_RIGHTUP = 5;
	private final static byte TILE_LEFTDOWN = 6;
	private final static byte TILE_RIGHTDOWN = 7;
	private final static byte TILE_MARKER = 8;
	private final static byte TILE_SPECIAL = 9;
	
	private final static int DEPTH_MARKER = 1;
	private final static int DEPTH_PLAYER = 2;
	
	/*private final static String[] ADJECTIVES =
		{ "Bright", "Bubbly", "Cheery", "Emerald", "Enchanted", "Fragrant", "Green", "Fun", "Happy", "Incredible", "Merry",
		"Mystic", "Sugar", "Sunny", "Sweet", "Tender", "Tranquil", "Verdant", "Vibrant", "Wonder" };
	private final static String[] NATURES = { "Beat", "Bliss", "Bounce", "Candy", "Dash", "Flower", "Grass", "Harmony", "Hill",
		"Jump", "Meadow", "Melody", "Mound", "Music", "Petal", "Plains", "Rhythm", "Rise", "Run", "Rush", "Shine" };
	private final static String[] PLACES = { "Area", "Country", "Domain", "Island", "Kingdom", "Land", "Realm", "World", "Zone" };*/
	
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
	
	/*private final static String[] NAME0 = { "fur" };
	private final static String[] NAME1 = { "b", "br", "g", "m", "n", "v", "w" };
	private final static String[] NAME2 =
		{ "aladr", "alend", "andr", "ard", "eld", "eleb", "empr", "endl", "ere", "ill", "istr", "ora", "othel" };
	private final static String[] NAME3 = { "eth", "ia", "ing", "ion", "ost" };
	private final static Namer nmr = Namer.get(NAME0, NAME1, NAME2, NAME3);*/
    
    private final static String[] ADJECTIVES =
    	{ "brav", "bright", "clear", "deep", "fair", "good", "grand", "green", "kind", "north", "old", "strong", "sweet", "verd", "warm", "wood" };
    private final static String[] NOUNS =
    	{ "beat", "bell", "bliss", "branch", "breez", "brush", "claw", "clov", "cross", "day", "flow", "furr",
        "grain", "grass", "grov", "heart", "heath", "hill", "holl", "hom", "king", "leaf", "mead", "mint", "morn",
    	"mound", "paw", "plain", "plant", "ring", "root", "shield", "soul", "spring", "stepp", "sunn", "vin", "well", "wheat" };
    private final static String[] VERBS =
    	{ "bloom", "bound", "dash", "grow", "leap", "ris", "runn", "rush", "shin", "thriv", "wind", "wish" };
    private final static String[] LINK_ADJ = { "al", "em", "est", "ing" };
    private final static String[] LINK_NON = { "en", "ing", "ic", "y" }; // "ish"
    private final static String[] LINK_VRB = { "al", "em", "er", "ing" };
    private final static String[] PLACES =
    	{ "berg", "by", "croft", "dom", "fold", "gard", "ham", "holt", "march", "land", "nesse", "port", "shire", "stead", "strand",
    	"thorp", "ton" };
    private final static Namer nmr = Namer.get(
        Namer.get(ADJECTIVES, LINK_ADJ, PLACES),
        Namer.get(NOUNS, LINK_NON, PLACES),
        Namer.get(VERBS, LINK_VRB, PLACES));
    // burgh, field, heim, town
    // bloomingberg, blooming-gard
	
	protected final static int bgTexture = 0;
	protected final static int bgColor = 1;
	
	private final static HashMap<Pair<Integer, Integer>, Boolean> open = new HashMap<Pair<Integer, Integer>, Boolean>();
	private static int roomW = -1;
	private static int roomH = -1;
	private static int column = -1;
	private static int row = -1;
	private static int endColumn = -1;
	private static int endRow = -1;
	private static String name = null;
	
	private static Panroom room = null;
	private static Panmage timg = null;
	private static DynamicTileMap tm = null;
	private final static ArrayList<Marker> markers = new ArrayList<Marker>();
	private static TileMapImage[][] imgMap = null;
	private static TileMapImage water = null;
	private static TileMapImage base = null;
	
	private static boolean debug = false;
	
	protected final static class MapScreen extends Panscreen {
		@Override
        protected final void load() throws Exception {
		    if (timg == null) {
		        loadImages();
		    }
			clear();
			if (tm != null && row == endRow && column == endColumn) {
				tm.destroy(); // Trigger generation of new Map
				tm = null;
			}
			final Tile t;
			if (tm == null) {
			    t = loadMap();
			} else {
				t = getStartTile();
				open.put(getKey(t), Boolean.TRUE);
			    initRoom();
			}
		    addPlayer(t);
            addHud();
			PlatformGame.fadeIn(room);
		}
		
		@Override
	    protected final void destroy() {
	        //Panmage.destroy(timg);
		    for (final Marker m : markers) {
		        m.detach();
		    }
	    }
	}
	
	protected final static class Marker extends Panctor {
		private Marker(final boolean open) {
			setView(open);
		}
		
		private void setView(final boolean open) {
		    if (open) {
		        setView(PlatformGame.markerDefeated);
		    } else {
		        setView(PlatformGame.marker);
		    }
		}
	}
	
	protected final static class Player extends TileWalker {
	    private final PlayerContext pc;
		private boolean disabled = false;
		
		private Player(final PlayerContext pc) {
		    this.pc = pc;
			setView(pc.guySouth);
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
	        	final Tile t = getTile();
	            if (isOpen(t)) {
	                return;
	            }
	            setPlayerPosition(t);
	        	fadeOut(new PlatformGame.PlatformScreen());
			} else if (interaction.KEY_TAB.isActive()) {
				interaction.KEY_TAB.inactivate();
				debug = !debug;
			} else if (interaction.KEY_ESCAPE.isActive()) {
				interaction.KEY_ESCAPE.inactivate();
				fadeOut(new Menu.AvatarScreen(pc));
			}
		}
		
		private final void fadeOut(final Panscreen screen) {
		    disabled = true;
		    PlatformGame.fadeOut(room, screen);
		}
		
		private void setPos(final Tile t) {
			setPosition(t);
			setZ(getPosition(), DEPTH_PLAYER);
		}
		
		protected boolean go(final Direction d0) {
		    final Tile t0 = getTile();
		    Tile t = t0;
		    Direction d = d0;
			if (debug) {
				final Tile tmp = t0.getNeighbor(d0);
				if (tmp != null) {
					setPos(tmp);
				}
				return true;
			}
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
				changeView(pc.guyNorth);
			} else if (newDir == Direction.East) {
			    changeView(pc.guyEast);
			} else if (newDir == Direction.West) {
			    changeView(pc.guyWest);
			} else if (newDir == Direction.South) {
			    changeView(pc.guySouth);
			}
	    }
		
		@Override
		protected void onWalked() {
		    final Tile t = getTile();
			final byte b = t.getBehavior();
			switch (b) {
				case TILE_MARKER :
					setPlayerPosition(t);
				    changeView(pc.guySouth);
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
	
	private final static void loadImages() {
		BufferedImage tileImg = ImtilX.loadImage("org/pandcorps/platform/res/bg/Map.png", 128, null);
		Level.applyDirtTexture(tileImg, 48, 0, 96, 16);
		final BufferedImage terrain = Level.getDarkenedTerrain(Level.getTerrainTexture());
		Level.applyTerrainTexture(tileImg, 48, 32, 96, 48, terrain, Level.getTerrainMask(1));
		tileImg = Level.getColoredTerrain(tileImg, 48, 32, 48, 16);
		timg = Pangine.getEngine().createImage("img.map", tileImg);
	}
	
	private final static Tile loadMap() {
		open.clear();
		Panctor.destroy(markers);
		name = generateName();
	    Tile t;
		//for (int i = 0; i < 100; i++) { // For testing rarely randomly generating errors
	        //tm.destroy(); tm = null; destroy/clear markers
			t = loadMap2();
		//}
		return t;
	}
	
	private final static void clear() {
		PlatformGame.room.destroy();
		for (final PlayerContext pc : PlatformGame.pcs) {
		    pc.player = null;
		}
	}
	
	private final static Tile loadMap2() {
		final Mapper b = new RandomMapper();
		roomW = b.getW();
		roomH = b.getH();
		initRoom();
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
		b.init();
		final Tile t = getStartTile();
		b.build();
		return t;
	}
	
	private final static void initRoom() {
	    room = PlatformGame.createRoom(roomW, roomH);
        if (tm == null) {
            tm = new DynamicTileMap("act.tilemap", room, ImtilX.DIM, ImtilX.DIM);
        } else {
            for (final Marker m : markers) {
                room.addActor(m);
                m.setView(isOpen(tm.getContainer(m)));
            }
        }
	    room.addActor(tm);
	}
	
	private static interface Mapper {
		public int getW();
		
		public int getH();
		
		public void init();
		
    	public void build();
    }
    
    protected final static class DemoMapper implements Mapper {
    	@Override
    	public final int getW() {
    		return 256;
    	}
    	
    	@Override
    	public final int getH() {
    		return 192;
    	}
    	
    	@Override
    	public final void init() {
    		setPlayerPosition(2, 6);
    	}
    	
    	@Override
    	public final void build() {
    		buildDemo();
    	}
    }
	
	private final static void buildDemo() {
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
		mountain(5, 11, 3);
		mountain(10, 9, 3);
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
		
		marker(2, 6);
		vert(2, 7);
		rightDown(2, 8);
		horiz(3, 8);
		marker(4, 8);
		for (int i = 3; i < 6; i++) {
			horiz(i, 6);
		}
		marker(6, 6);
	}
	
	protected final static class RandomMapper implements Mapper {
		@Override
    	public final int getW() {
    		return 400;
    	}
		
		@Override
    	public final int getH() {
    		return 272;
    	}
		
		@Override
    	public final void init() {
    		setPlayerPosition(2, newMarkerRow());
    		//row = Mathtil.rand() ? 8 : 10;
    		//row = Mathtil.rand() ? 6 : 12;
    	}
		
		@Override
    	public final void build() {
			final int stop = tm.getWidth() - 1;
			final int mid = 12 + (Mathtil.randi(-1, 3) * 2);
			final boolean single = Mathtil.rand(25);
			if (single) {
				island(2, stop);
			} else {
				island(2, mid - 2);
				island(mid, stop);
			}
			final int il = Mathtil.rand() ? 0 : 3;
			final int[] used = new int[11];
			Arrays.fill(used, -1);
			landmark(3 + Mathtil.randi(0, (mid / 2) - 5) * 2, newLandmarkY(), il, used);
			//landmark(3, newLandmarkY(), il, used);
			landmark(mid + 1 + (Mathtil.randi(0, ((stop - mid) / 2) - 3) * 2), newLandmarkY(), (il + 3) % 6, used);
			marker(column, row);
			final int cs = column + 2;
			int c, r = row, fc = -1;
			boolean needMin = row > 6, needMax = row < 12, needFork = true;
			for (c = cs; c <= stop - 2; c += 2) {
				final int nr, c2 = c - 2;
				final int used1 = used[getIndex(c)], used2 = used[getIndex(c2)];
				final boolean currFork = c > cs && needFork && used1 == -1 && used2 == -1 && (c - 2) != mid && c != mid && (c + 2) != mid; // && !isWater(c, nr) (handled by mid)
				if (currFork || c == mid) {
					nr = r;
				} else if (c > 6 && (needMin || needMax) && used1 == -1 && used2 == -1) {
					if (needMin) {
						nr = 6;
						needMin = false;
					} else {
						nr = 12;
						needMax = false;
					}
				} else {
					int tr = newMarkerRow();
					if (used2 == r) {
					    throw new RuntimeException("\nused2 = r = " + r + "\nc = " + c + "\nmid = " + mid
					    		+ "\nneedFork = " + needFork);
					}
					while (true) {
						if ((used1 == -1 || used1 != tr) && (used2 == -1 || used2 < Math.min(r, tr) || used2 > Math.max(r, tr))) {
							break;
						}
						tr += 2;
						if (tr > 12) {
							tr = 6;
						}
					}
					needMin = needMin && tr > 6;
					needMax = needMax && tr < 12;
					nr = tr;
				}
				final int rn = Math.min(r, nr), rx = Math.max(r, nr);
				if (r != nr) {
					for (int j = rn + 1; j < rx; j++) {
						vert(c2, j);
					}
					if (nr < r) {
						if (c > cs) {
							leftDown(c2, r);
						}
						rightUp(c2, nr);
					} else {
						if (c > cs) {
							leftUp(c2, r);
						}
						rightDown(c2, nr);
					}
				} else if (c > cs) {
				    if (currFork) {
				    	fc = c2;
				        marker(c2, nr);
				        needFork = false;
				        final int dir = nr <= 8 ? 1 : -1;
				        vert(c2, nr + dir);
				        marker(c2, nr + dir * 2); // Level that could be skipped
				        vert(c2, nr + dir * 3);
				        marker(c2, nr + dir * 4); // Bonus unlocked by playing optional Level
				    } else {
				        horiz(c2, nr);
				    }
				}
				if (!currFork && c2 >= cs) {
					final int mr = Mathtil.randi(rn, rx);
					marker(c2, mr, single || !isWater(c2, mr));
				}
				horiz(c - 1, nr);
				r = nr;
			}
			endColumn = c - 2;
			endRow = r;
			marker(endColumn, endRow);
			cliff(fc);
			int bc = 0, br = 0, bl = 0;
			for (r = 6; r <= 12; r++) {
				int cc = column;
				for (c = cc; c <= stop - 1; c++) {
					if (!(c != (stop - 1) && tm.getTile(c, r).isSolid() && (c + 1) != mid && (c + 2) != mid && (c + 3) != mid)) {
						final int cl = c - cc;
						if (cl > bl || (cl == bl && Mathtil.rand())) {
							bc = cc;
							br = r;
							bl = cl;
						}
						cc = c + 1;
					}
				}
			}
			if (bl > 1) {
				mountain(bc, br, bl);
			}
			tm.replaceBehavior(TILE_SPECIAL, Tile.BEHAVIOR_SOLID);
		}
	}
	
	private final static int getIndex(final int tile) {
		return (tile - 2) / 2;
	}
	
	private final static int newMarkerRow() {
		return 6 + Mathtil.randi(0, 3) * 2;
	}
	
	private final static int newLandmarkY() {
		return 7 + Mathtil.randi(0, 1) * 2;
	}
	
	private final static void cliff(final int m) {
		final int n = m - 1, x = m + 1;
		tm.getTile(n, 16).setForeground(imgMap[0][0]);
		tm.getTile(m, 16).setForeground(imgMap[0][1]);
		tm.getTile(x, 16).setForeground(imgMap[0][2]);
		tm.getTile(n, 15).setForeground(imgMap[1][0]);
		tm.getTile(m, 15).setForeground((Panmage) null);
		marker(m, 15);
		tm.getTile(x, 15).setForeground(imgMap[1][2]);
		tm.getTile(n, 14).setImages(imgMap[0][3], imgMap[2][0]);
		tm.getTile(m, 14).setImages(imgMap[0][4], imgMap[2][1], TILE_VERT);
		tm.getTile(x, 14).setImages(imgMap[0][5], imgMap[2][2]);
		tm.getTile(n, 13).setImages(imgMap[0][3], imgMap[0][2]);
		tm.getTile(m, 13).setImages(imgMap[0][4], imgMap[0][6], TILE_VERT);
		tm.getTile(x, 13).setImages(imgMap[0][5], imgMap[0][0]);
		for (int y = 12; ; y--) {
			final Tile tile = tm.getTile(m, y);
			if (!tile.isSolid()) {
				break;
			}
			vert(m, y);
		}
	}
	
	private final static void island(final int start, final int stop) {
		final int bn = 4, bx = 6, tn = 12, tx = 14;
		int b = Mathtil.randi(bn, bx), t = Mathtil.randi(tn, tx), dir = Mathtil.randi(-1, 0), tdir = Mathtil.randi(0, 1);
		tm.fillBackground(imgMap[1][0], start - 1, b, 1, t - b + 1);
		baseLeft(start - 1, b);
		topUp(start - 1, t);
		for (int i = start; i < stop - 1; i++) {
			dir = updateDir(dir, b, bn, bx);
			tdir = updateDir(tdir, t, tn, tx);
			final int cb, ct;
			if (dir < 0) {
				b += dir;
				cb = b;
				baseDown(i, b);
			} else if (dir > 0) {
				cb = b;
				baseUp(i, b);
				b += dir;
			} else {
				cb = b;
				baseMid(i, b);
			}
			if (tdir < 0) {
				ct = t;
				topDown(i, t);
				t += tdir;
			} else if (tdir > 0) {
				t += tdir;
				ct = t;
				topUp(i, t);
			} else {
				ct = t;
				tm.initTile(i, t + 1).setForeground(imgMap[0][1]);
			}
			mid(i, cb, ct);
		}
		tm.fillBackground(imgMap[1][2], stop - 1, b, 1, t - b + 1);
		baseRight(stop - 1, b);
		topDown(stop - 1, t);
	}
	
	private final static int updateDir(int dir, final int b, final int n, final int x) {
		if (Mathtil.rand(45)) {
			if (dir < 0) {
				dir = 0;
			} else if (dir > 0) {
				dir = 0;
			} else {
				dir = Mathtil.rand() ? -1 : 1;
			}
		}
		if (dir < 0 && b <= n) {
			dir = 0;
		} else if (dir > 0 && b >= x) {
			dir = 0;
		}
		return dir;
	}
	
	private final static void base(final int x, final int y, final int tx, final int wx) {
		final TileMapImage terrain = imgMap[0][tx];
		tm.initTile(x, y - 1).setImages(terrain, imgMap[2][wx - 3]);
		tm.initTile(x, y - 2).setBackground(terrain);
		tm.initTile(x, y - 3).setImages(terrain, imgMap[1][wx]);
	}
	
	private final static void baseLeft(final int x, final int y) {
		base(x, y, 3, 3);
	}
	
	private final static void baseDown(final int x, final int y) {
		base(x, y, 4, 3);
	}
	
	private final static void baseMid(final int x, final int y) {
		base(x, y, 4, 4);
	}
	
	private final static void baseUp(final int x, final int y) {
		base(x, y, 4, 5);
	}
	
	private final static void baseRight(final int x, final int y) {
		base(x, y, 5, 5);
	}
	
	private final static void mid(final int x, final int b, final int t) {
		for (int j = b; j <= t; j++) {
			tm.initTile(x, j).setBackground(getBaseImage());
		}
	}
	
	private final static void topUp(final int x, final int y) {
		tm.initTile(x, y + 1).setForeground(imgMap[0][0]);
	}
	
	private final static void topDown(final int x, final int y) {
		tm.initTile(x, y + 1).setForeground(imgMap[0][2]);
	}
	
	private final static Tile getStartTile() {
		return tm.getTile(column, row);
	}
	
	private final static void addPlayer(final Tile t) {
		final Player player = new Player(PlatformGame.pcs.get(0));
		player.setPos(t);
		room.addActor(player);
		Pangine.getEngine().track(player);
	}
	
	private final static void addHud() {
		final Pantext name = new Pantext("map.name", PlatformGame.font, Map.name);
		name.getPosition().set(PlatformGame.SCREEN_W / 2, 1);
		name.centerX();
		final Panlayer hud = PlatformGame.addHud(room, false);
		hud.addActor(name);
	}
	
	private static void mountain(final int x, final int y, final int w) {
		final boolean wtr = isWater(x, y);
		final int stop = x + w - 1;
		setForeground(x, y, 2, 3);
		for (int i = x + 1; i < stop; i++) {
			setForeground(i, y, 2, 4);
		}
		setForeground(stop, y, 2, 5);
		if (wtr) {
			final int yshadow = y - 1;
			for (int i = x; i <= stop; i++) {
				final Tile t = tm.initTile(i, yshadow);
				final TileMapImage b = getBaseImage();
				t.setBackground(b);
				t.setForeground(b);
			}
		}
	}
	
	private static void landmark(final int x, final int y, final int ix, final int[] used) {
		used[getIndex(x + 1)] = y + 1;
		for (int j = 0; j < 3; j++) {
			final int yj = y + j, j7 = 7 - j;
			for (int i = 0; i < 3; i++) {
				setForeground(x + i, yj, j7, ix + i).setBehavior(TILE_SPECIAL);
			}
		}
	}
	
	private static Tile setForeground(final int x, final int y, final int ij, final int ii) {
		final Tile t = tm.initTile(x, y);
		t.setForeground(imgMap[ij][ii]);
		if (!isWater(t)) {
			t.setBackground(base);
		}
		return t;
	}
	
	private static boolean isWater(final int x, final int y) {
		return isWater(tm.initTile(x, y));
	}
	
	private static boolean isWater(final Tile t) {
		return DynamicTileMap.getRawBackground(t) == water;
	}
	
	private static void marker(final int i, final int j) {
		marker(i, j, true);
	}
	
	private static void marker(final int i, final int j, final boolean bg) {
		final Tile tile = tm.initTile(i, j);
		if (bg) {
			tile.setBackground(imgMap[3][0], TILE_MARKER);
		}
		final Marker m = new Marker(isOpen(tile));
		markers.add(m);
		//m.setPosition(tile);
		final Panple pos = m.getPosition();
		pos.set(tile.getPosition());
		setZ(pos, DEPTH_MARKER);
		room.addActor(m);
	}
	
	private final static void setZ(final Panple pos, final int depth) {
	    pos.setZ(tm.getForegroundDepth() + depth);
	}
	
	private static void horiz(final int i, final int j) {
		final Tile t = tm.initTile(i, j);
		if (isWater(t)) {
			t.setForeground(imgMap[0][7], TILE_HORIZ);
		} else if (isWater(i - 1, j) || isWater(i + 1, j)) {
			t.setBehavior(TILE_HORIZ);
		} else {
			t.setBackground(imgMap[3][1], TILE_HORIZ);
		}
	}
	
	private static void vert(final int i, final int j) {
		tm.initTile(i, j).setBackground(imgMap[3][2], TILE_VERT);
	}
	
	private static void leftUp(final int i, final int j) {
		tm.initTile(i, j).setBackground(imgMap[3][3], TILE_LEFTUP);
	}
	
	private static void rightUp(final int i, final int j) {
		tm.initTile(i, j).setBackground(imgMap[3][4], TILE_RIGHTUP);
	}
	
	private static void leftDown(final int i, final int j) {
		tm.initTile(i, j).setBackground(imgMap[3][5], TILE_LEFTDOWN);
	}
	
	private static void rightDown(final int i, final int j) {
		tm.initTile(i, j).setBackground(imgMap[3][6], TILE_RIGHTDOWN);
	}
	
	private final static String generateName() {
		//return Mathtil.rand(ADJECTIVES) + ' ' + Mathtil.rand(NATURES) + ' ' + Mathtil.rand(PLACES);
	    return nmr.get();
	}
	
	private final static void setPlayerPosition(final Tile t) {
		setPlayerPosition(t.getColumn(), t.getRow());
	}
	
	private final static void setPlayerPosition(final int column, final int row) {
		Map.column = column;
		Map.row = row;
	}
	
	public final static void main(final String[] args) {
	    nmr.printDemo();
	}
}
