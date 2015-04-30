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
package org.pandcorps.furguardians;

import java.io.*;
import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.core.img.Pancolor.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panteraction.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.pandax.visual.*;
import org.pandcorps.furguardians.Level.*;
import org.pandcorps.furguardians.Menu.*;
import org.pandcorps.furguardians.Player.*;
import org.pandcorps.furguardians.Profile.*;

public class Map {
	protected final static byte VICTORY_NONE = 0;
	protected final static byte VICTORY_LEVEL = 1;
	protected final static byte VICTORY_WORLD = 2;
	
	private final static byte TILE_HORIZ = 2;
	private final static byte TILE_VERT = 3;
	private final static byte TILE_LEFTUP = 4;
	private final static byte TILE_RIGHTUP = 5;
	private final static byte TILE_LEFTDOWN = 6;
	private final static byte TILE_RIGHTDOWN = 7;
	private final static byte TILE_MARKER = 8;
	private final static byte TILE_SPECIAL = 9;
	
	private final static int DEPTH_FOREGROUND = 1;
	private final static int DEPTH_MARKER = 2;
	
	private final static String SEG_MAP = "MAP";
	private final static String SEG_MRK = "MRK";
	private final static String SEG_BLD = "BLD";
	
	private final static String[] EXT_LANDMARKS = { "Forest", "Crater" };
	private final static int MAX_CASTLE = 3;
	
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
    
	/*private final static String[] ADJECTIVES =
    	{ "brav", "bright", "clear", "deep", "fair", "good", "grand", "green", "kind", "north", "old", "strong", "sweet", "verd", "warm", "wood" };
    private final static String[] NOUNS =
    	{ "beat", "bell", "bliss", "branch", "breez", "brush", "claw", "clov", "cross", "day", "flow", "furr",
        "grain", "grass", "grov", "heart", "heath", "hill", "holl", "hom", "king", "leaf", "mead", "mint", "morn",
    	"mound", "paw", "plain", "plant", "ring", "root", "shield", "soul", "spring", "stepp", "sunn", "vin", "well", "wheat" };
    private final static String[] VERBS =
    	{ "bloom", "bound", "dash", "grow", "leap", "paint", "ris", "runn", "rush", "shin", "thriv", "wind", "wish" };
    //private final static String[] LINK_ADJ = { "al", "em", "est", "ing" };
    //private final static String[] LINK_NON = { "en", "ing", "ic", "y" }; // "ish"
    //private final static String[] LINK_VRB = { "al", "em", "er", "ing" };
    private final static String[] PLACES =
    	{ "berg", "burgh", "by", "croft", "dom", "field", "fold", "fort", "gard", "ham", "heim", "holt", "island", "isle", "march", "mark",
        "land", "nesse", "port", "shire", "stead", "strand", "thorp", "ton", "town" };
    // andria, any, bury, hold, hurst, meade, wich; ndon
    private final static String[] COLDS = { "arct", "blizz", "chill", "cold", "freez", "froz", "glac", "ic", "north", "snow", "tundr", "whit", "wintr" };
    private final static String[] WARMS = { "ash", "burn", "blaz", "cindr", "embr", "fir", "flam", "heat", "hott", "sand", "scorch", "summer", "torch", "warm" };
    // concrete/adjective + abstract/noun: Greensong, Fairnest, Brighthope, Deardream, ballad, heart, love, spell, story, tale, wish
    // Add North/South/East/West before names?
    private final static Namer nmr = Namer.get(
        Namer.get(mpt, cct, ADJECTIVES, PLACES),
        Namer.get(mpt, cct, NOUNS, PLACES),
        Namer.get(mpt, cct, VERBS, PLACES));*/
    
    /*private final static String[] GRASSES = {
    	"bell", "bloom", "branch", "breeze", "bright", "brush", "claw", "clear", "clover", "day", "fair", "flower", "fur",
        "grain", "grand", "grass", "green", "heather", "hill", "holly", "honey", "kind", "leaf", "love", "meadow", "mint", "new",
        "old", "paw", "petal", "plain", "plant", "root", "shine", "soul", "spring", "sun", "sweet", "verdant", "vine", "thrive", "wheat", "wind", "wooden" };
    private final static String[] COLDS = { "arctic", "blizzard", "chill", "cold", "freeze", "frozen", "glacier", "ice", "north", "snow", "tundra", "white", "winter" };
    private final static String[] WARMS = { "ash", "burn", "blaze", "cinder", "ember", "fire", "flame", "heat", "hot", "sand", "scorch", "south", "summer", "torch", "warm" };
    private final static String[] ENDINGS = {
    	"ballad", "bliss", "dream", "grove", "heart", "home", "hope", "nest", "rise", "song", "spell", "story", "tale", "well", "wish" };
    private final static String[] PLACES =
    	{ "burgh", "fort", "gard", "heim", "holt", "home", "mark",
        "land", "shield", "spell", "stead", "sword", "tale", };
    private final static Manipulator mpt = new MapManipulator();
    private final static Concatenator cct = new MapConcatenator();
    private final static Namer nmr = Namer.get(mpt, cct, GRASSES, ENDINGS);*/
	
    protected static MapTheme theme = MapTheme.Normal;
	protected static int bgTexture = 0;
	protected static int bgColor = 1;
	private static int lm1 = -1;
	private static int lm2 = -1;
	private static int cstl = -1;
	private static long seed = -1;
	
	protected static byte victory = VICTORY_NONE;
	private static int roomW = -1;
	private static int roomH = -1;
	private static int endColumn = -1;
	private static int endRow = -1;
	private final static int[] EYES_ROY = {2, 4, 5, 6, 7, 8, 9, 10};
	protected static Avatar royAvt = null;
	protected static int royCrown = -1;
	private static String name = null;
	private static TextMover tipMover = null;
	
	protected static boolean started = false;
	private static boolean oldMap = true;
	
	private static Panroom room = null;
	private static Panmage timg = null;
	private static TileMap tm = null;
	private final static ArrayList<Marker> markers = new ArrayList<Marker>();
	private final static ArrayList<Building> buildings = new ArrayList<Building>();
	private static Panctor portal = null;
	private static TileMapImage[][] imgMap = null;
	private static TileMapImage water = null;
	private static TileMapImage[] waters = null;
	private static TileMapImage base = null;
	private static TileMapImage ladder = null;
	private static TileMapImage bridge = null;
	
	private static MapPlayer player = null;
	
	protected final static short MOVE_NORMAL = 0;
	protected final static short MOVE_ANY_PATH = 1;
	protected final static short MOVE_ANY_TILE = 2;
	protected static short modeMove = MOVE_NORMAL;
	
	private static boolean waiting = true;
	
	protected abstract static class MapTheme {
		protected final void stepWater() {
			if ((Pangine.getEngine().getClock() % 6) == 0) {
                Tile.animate(waters);
            }
		}
		
		public final static MapTheme Normal = new MapTheme("Normal", null, Theme.Normal, 3, 3, 2, null /*, Map.nmr*/ ) {
			@Override protected final void step() {
				stepWater();
			}
			@Override protected final PixelFilter getHillFilter0() {
				return null; }
			@Override protected final PixelFilter getHillFilter1() {
				return new SwapPixelFilter(Channel.Red, Channel.Blue, Channel.Green); }
			@Override protected final PixelFilter getHillFilter2() {
				return new SwapPixelFilter(Channel.Blue, Channel.Red, Channel.Green); }};
		public final static MapTheme Snow = new MapTheme("Snow", Theme.Snow, 1, 6, 2,
		    new SwapPixelFilter(Channel.Blue, Channel.Green, Channel.Red)
		    /*, Namer.get(mpt, cct, COLDS, PLACES)*/ ) {
			@Override protected final void step() {
				flash();
			}
			@Override protected final PixelFilter getHillFilter0() {
				return new SwapPixelFilter(Channel.Red, Channel.Red, Channel.Green); }
			@Override protected final PixelFilter getHillFilter1() {
				return new SwapPixelFilter(Channel.Red, Channel.Blue, Channel.Green); }
			@Override protected final PixelFilter getHillFilter2() {
				return new SwapPixelFilter(Channel.Red, Channel.Green, Channel.Green); }
			@Override protected final PixelMask getDirtMask() {
				return getBasicDirtMask(); }};
		
		protected final PixelMask getBasicDirtMask() {
			final int minR, minG;
			if (Level.theme == Theme.Cave) {
				minR = 96;
				minG = 48;
			} else {
				minR = minG = 80;
			}
			return new AntiPixelMask(new RangePixelMask(minR, minG, 0, 255, 144, 32));
		}
		
		public final static MapTheme Sand = new MapTheme("Sand", Theme.Sand, 1, 6, 3, null
		    /*, Namer.get(mpt, cct, WARMS, PLACES)*/ ) {
			@Override protected final void step() {
				if (Pangine.getEngine().getClock() % 4 == 0) {
					Tile.animate(waters);
				}
			}
			@Override protected final PixelFilter getSkyFilter() {
				return new SwapPixelFilter(Channel.Blue, Channel.Green, Channel.Red); }
			@Override protected final PixelFilter getHillFilter0() {
				return new SwapPixelFilter(Channel.Green, Channel.Red, Channel.Red); }
			@Override protected final PixelFilter getHillFilter1() {
				return new SwapPixelFilter(Channel.Green, Channel.Blue, Channel.Red); }
			@Override protected final PixelFilter getHillFilter2() {
				return new SwapPixelFilter(Channel.Green, Channel.Green, Channel.Red); }};
		public final static MapTheme Rock = new MapTheme("Rock", Theme.Rock, 1, 6, 4,
			new SwapPixelFilter(Channel.Green, Channel.Green, Channel.Green)) {
			@Override protected final void step() {
				stepWater();
			}
			@Override protected final PixelFilter getHillFilter0() {
				return new SwapPixelFilter(Channel.Blue, Channel.Green, Channel.Blue); }
			@Override protected final PixelFilter getHillFilter1() {
				return new SwapPixelFilter(Channel.Blue, Channel.Blue, Channel.Green); }
			@Override protected final PixelFilter getHillFilter2() {
				return new SwapPixelFilter(Channel.Green, Channel.Blue, Channel.Green); }
			@Override protected final PixelMask getDirtMask() {
				return getBasicDirtMask(); }};
		
		protected final String name;
		protected final String img;
		protected final Theme levelTheme;
		protected final PixelFilter dirtFilter;
		protected final int maxLandmark;
		protected final int portalGroundRow;
		protected final int portalGroundColumn;
		
		private MapTheme(final String img, final Theme levelTheme, final int maxLandmark,
				final int portalGroundRow, final int portalGroundColumn,
				final PixelFilter dirtFilter) {
			this(img, img, levelTheme, maxLandmark, portalGroundRow, portalGroundColumn, dirtFilter);
		}
		
		private MapTheme(final String name, final String img, final Theme levelTheme, final int maxLandmark,
				final int portalGroundRow, final int portalGroundColumn,
				final PixelFilter dirtFilter) {
			this.name = name;
			this.img = img;
			this.levelTheme = levelTheme;
			this.maxLandmark = maxLandmark;
			this.portalGroundRow = portalGroundRow;
			this.portalGroundColumn = portalGroundColumn;
			this.dirtFilter = dirtFilter;
		}
		
		protected final void flash() {
			final long i = Pangine.getEngine().getClock() % 105;
            if (i < 3) {
            	if (waiting) {
            		if (i == 0) {
            			waiting = false;
            		} else {
            			return;
            		}
            	}
                Tile.animate(waters);
            }
		}
		
		protected abstract void step();
		
		protected PixelFilter getSkyFilter() {
			return null; }
		
		protected abstract PixelFilter getHillFilter0();
		
		protected abstract PixelFilter getHillFilter1();
		
		protected abstract PixelFilter getHillFilter2();
		
		protected PixelMask getDirtMask() {
			return null;
		}
	}
	
	protected final static MapTheme[] themes = {MapTheme.Normal, MapTheme.Snow, MapTheme.Sand, MapTheme.Rock};
	
	protected final static MapTheme getTheme(final String name) {
		for (final MapTheme theme : themes) {
			if (theme.name.equals(name)) {
				return theme;
			}
		}
		return MapTheme.Normal;
	}
	
	/*protected final static class MapManipulator extends Manipulator {
        @Override
        public final String manipulate(final String s) {
            return s.endsWith("isle") ? "Isle " + s.substring(0, s.length() - 4) : s;
        }
	}*/
	
	/*protected final static class MapConcatenator extends BaseConcatenator {
        @Override
        public final String getDelimValued(final String s1, final String s2) {
            final String d;
            if ("island".equals(s2)) {
                d = "ia ";
            } else if ("town".equals(s2)) {
                d = "ing ";
            } else if ("froz".equals(s1)) {
                d = "en";
            } else if ("blizz".equals(s1)) {
                d = "ard";
            } else if ("glac".equals(s1)) {
                d = "ial";
            } else if ("arct".equals(s1) || "tundr".equals(s1)) {
                d = "ic";
            } else if ("summer".equals(s1)) {
                d = "s";
            } else {
                d = s2.charAt(0) == 'g' ? "en" : "ing";
            }
            return (d.charAt(d.length() - 1) == s2.charAt(0)) ? (d + "-") : d;
        	//return (s1.charAt(s1.length() - 1) == s2.charAt(0)) ? "-" : "";
        }
	}*/
	
	protected final static class MapScreen extends Panscreen {
		@Override
        protected final void load() throws Exception {
			waiting = true;
			for (final PlayerContext pc : FurGuardiansGame.pcs) {
				if (pc.guyRun == null) {
		    		FurGuardiansGame.reloadAnimalStrip(pc);
		    	}
				Goal.initGoals(pc);
			}
		    started = true;
		    if (timg == null) {
		        loadImages();
		    }
			clear();
			if (victory == VICTORY_LEVEL) {
				for (final PlayerContext pc : FurGuardiansGame.pcs) {
					final Profile profile = pc.profile;
					final byte jmi = profile.currentAvatar.jumpMode;
					if (!profile.isJumpModeAvailable(jmi)) {
						final JumpMode jm = JumpMode.get(jmi);
						FurGuardiansGame.notify(pc, jm.getName() + " trial ended");
						profile.triedJumpModes.add(Integer.valueOf(jmi));
						profile.currentAvatar.jumpMode = Player.MODE_NORMAL;
						profile.save();
						FurGuardiansGame.reloadAnimalStrip(pc);
					}
				}
				if (tm != null && isOnLastLevel()) {
					//FurGuardiansGame.worldClose(); // This point is on MapScreen for new World
					//Iotil.delete(getMapFile()); // Want to evaluate WorldGoal sooner, now done below
				    victory = VICTORY_NONE;
					triggerLoad();
					loadImages();
				}
			}
			final int t;
			if (tm == null) {
			    t = loadMap();
			    FurGuardiansGame.saveGame();
			} else {
			    t = getStartTile();
				if (victory == VICTORY_LEVEL) {
				    victory = VICTORY_NONE;
				    getOpen().put(getKey(t), Boolean.TRUE);
				    final Building b = getBuilding(t);
				    if (isCabin(b)) {
				    	b.ij = 5;
				    	b.setView();
				    	saveMap();
				    }
				    FurGuardiansGame.saveGame();
				}
			    initRoom();
			}
			if (victory == VICTORY_NONE && isOnLastLevel() && isOpen(t)) {
				// If we exit the game after beating last level but before starting next world, we might reach this state
				victory = VICTORY_WORLD;
			}
			final boolean victoryWorld = victory == VICTORY_WORLD;
			if (victoryWorld) {
				new FloatPlayer(t);
				victory = VICTORY_LEVEL;
				portal.setView(FurGuardiansGame.portalClosed);
			} else if (oldMap) {
			    addPlayer(t);
                if (isOpen(t) && getNumberOfPaths(t) <= 2) {
                    player.go(Direction.East);
                }
			} else {
			    new FloatPlayer(t);
			    oldMap = true;
			}
			addBorder();
            addHud(victoryWorld);
            if (victoryWorld) {
            	// Could beat World, achieve old goal, receive World goal, increment here, give credit for beating World before it was assigned
	            FurGuardiansGame.worldClose();
				Iotil.delete(getMapFile());
            }
            if (victory != VICTORY_LEVEL) {
				Achievement.evaluate(); // Evaluate after addHud
			}
			FurGuardiansGame.fadeIn(room);
			FurGuardiansGame.playMenuMusic();
		}
		
		@Override
        protected final void step() {
			theme.step();
        }
		
		@Override
	    protected final void destroy() {
	        //Panmage.destroy(timg);
			Panctor.detach(tm);
			Panctor.detach(markers);
			Panctor.detach(buildings);
			Panctor.detach(portal);
	    }
	}
	
	protected final static boolean isOnLastLevel() {
	    final Profile prf = getProfile();
	    return prf.row == endRow && prf.column == endColumn;
	}
	
	protected final static void triggerLoad() {
	    Panctor.destroy(tm); // Trigger generation of new Map
        tm = null;
        Panmage.destroy(timg);
        timg = null;
	}
	
	protected final static class Marker extends Panctor {
		private Marker(final boolean open) {
			setView(open);
		}
		
		private void setView(final boolean open) {
		    if (open) {
		        setView(FurGuardiansGame.markerDefeated);
		    } else {
		        setView(FurGuardiansGame.marker);
		    }
		}
	}
	
	protected final static class Building extends TileActor {
		private int ij;
		private final int ii;
		
		private Building(final int ij, final int ii) {
			this.ij = ij;
			this.ii = ii;
			setView();
		}
		
		private void setView() {
			setView(tm, imgMap[ij][ii]);
		}
	}
	
	protected final static class FloatPlayer extends TileWalker {
		private final Bubble bubble = new Bubble();
		int steps;
		
	    private FloatPlayer(int index) {
	        setView(getPlayerContext().mapSouth.getFrames()[0].getImage());
	        if (victory == VICTORY_NONE) {
	        	steps = 2;
		        for (int i = 0; i < steps; i++) {
		            index = tm.getNeighbor(index, Direction.West);
		        }
	        } else {
	        	steps = 0;
	        }
	        setPosition(tm, index);
	        room.addActor(this);
	        Pangine.getEngine().track(this);
	        setSpeed(0.5f);
	        setSolid(false);
	        room.addActor(bubble);
	        final Panple pos = getPosition();
	        bubble.getPosition().set(0, pos.getY() + 2, pos.getZ() + 1);
	        onWalking();
	    }
	    
	    @Override
        protected void onStill() {
	    	steps--;
	    	if (steps >= 0) {
	    		if (steps == 0 && victory != VICTORY_NONE) {
	    			FurGuardiansGame.goMap();
	    		}
	    		walk(Direction.East);
	    	} else if (steps >= -30) {
	    		bubble.onStepEnd(true);
	    	} else if (victory == VICTORY_NONE) {
		        addPlayer(getIndex());
		        destroy();
	    	} else {
	    		steps = 2;
	    	}
	    }
	    
	    @Override
	    protected void onWalking() {
	    	bubble.onStepEnd(true);
	    	bubble.getPosition().setX(getPosition().getX() + 9);
	    }
	    
	    @Override
		protected final void onDestroy() {
			bubble.destroy();
		}
	}
	
	protected final static class MapPlayer extends TileWalker {
	    private final PlayerContext pc;
		private boolean disabled = false;
		private boolean onLadder = false;
		private int stillTimer = -1;
		private int waitTimer = 0;
		private List<Pantext> helps = null;
		private final long startTime;
		
		private MapPlayer(final PlayerContext pc) {
		    this.pc = pc;
		    startTime = Pangine.getEngine().getClock() + 1;
			setView(pc.mapSouth);
			setSpeed(2);
			register(new ActionStartListener() { @Override public final void onActionStart(final ActionStartEvent event) {
				if (disabled) {
					return;
				}
			    final Panput input = event.getInput();
			    final Device device = event.getDevice();
			    for (final PlayerContext oc : FurGuardiansGame.pcs) {
			        if (oc.getDevice().equals(device)) {
			        	if (oc.index > 0 && getMenuInput(oc.ctrl) == input) {
			        		goMenu(input, oc);
			        	}
			            return;
			        }
			    }
			    input.inactivate();
			    final SelectScreen screen = new SelectScreen(null, true, null);
                screen.ctrl = ControlScheme.getDefault(device);
                fadeOut(screen);
			}});
		}
		
		private final Panput getMenuInput(final ControlScheme ctrl) {
			return ctrl.get2();
		}
		
		private final Pantext addHelp(final String s, final int x, final int y) {
			final Pantext text = addText(s, x, y);
		    helps = Coltil.add(helps, text);
		    return text;
		}
		
		private final void clearHelp() {
		    waitTimer = 0;
		    Panctor.destroy(helps);
		}
		
		@Override
		protected void onStill() {
			if (disabled) {
				return;
			} else if (stillTimer >= 0) {
			    if (stillTimer == 0) {
			        changeView(pc.mapSouth);
			    }
			    stillTimer--;
			}
			waitTimer++;
			final Pangine engine = Pangine.getEngine();
			if (engine.isTouchSupported()) {
				final int lim = pc.profile.stats.defeatedLevels == 0 ? 60 : 240;
				if (waitTimer == lim) {
					final int r = PlayerScreen.getTouchButtonRadius();
				    if (isOpen(getIndex())) {
				        // Standing on a defeated Level; show help to move
				    	addHelp("Left", r, r * 2 - 4);
				    	addHelp("Down", r * 2, r - 4);
				    	addHelp("Up", r * 2, r * 3 - 4);
				    	addHelp("Right", r * 3, r * 2 - 4);
				    } else {
				        // Standing on an unplayed Level; show help to play
				        addHelp("Play", engine.getEffectiveWidth() - r, r - 4);
				    }
				} else if (waitTimer == (lim * 2)) {
				    // Maybe Player doesn't want to play Level; show Menu help
					final String s = "Change appearance, options", s2 = "Goals";
					final Panple menuSize = FurGuardiansGame.menu.getSize();
					final int w = engine.getEffectiveWidth(), h = engine.getEffectiveHeight(), mh = (int) menuSize.getY();
					addHelp(s, w - (s.length() * 4), h - mh - 9);
					addHelp(s2, w - (int) menuSize.getX() - (s2.length() * 4), h - 28);
				}
			}
			final Panteraction interaction = engine.getInteraction();
			final ControlScheme ctrl = pc.ctrl;
			if (ctrl == null) {
				return;
			}
			final boolean endListener = engine.isTouchSupported();
			// Similar to Guy4Controller
	        if (Panput.isActive(ctrl.getDown(), endListener)) {
	            go(Direction.South);
	        } else if (Panput.isActive(ctrl.getUp(), endListener)) {
	            go(Direction.North);
	        } else if (Panput.isActive(ctrl.getLeft(), endListener)) {
	            go(Direction.West);
	        } else if (Panput.isActive(ctrl.getRight(), endListener)) {
	            go(Direction.East);
	        } else if (Panput.isActive(ctrl.get1(), endListener)) {
	        	/*if (room.getBlendColor().getA() > Pancolor.MIN_VALUE) {
	        		return;
	        	}*/
	        	if (engine.getClock() <= startTime || FadeController.isFadingIn()) {
	        		ctrl.get1().inactivate();
	        		return;
	        	}
	            clearHelp();
	        	final int t = getIndex();
	            if (isOpen(t)) {
	                return;
	            }
	            changeView(pc.mapPose);
	            setPlayerPosition(t);
	            final Panscreen screen;
	            final Building b = getBuilding(t);
	            Level.seed = seed + t;
	            Level.initThemeForNonSpecialMarker(); // Depends on seed, so run this after assigning it
	            if (isCabin(b)) {
	            	screen = new Cabin.CabinScreen();
	            } else if (isCastle(b)) {
	                screen = new Castle.ThroneIntroScreen();
	                Level.setTheme(Theme.Chaos);
	            } else {
	                if (isBridge(t)) {
	                    Level.setTheme(Theme.Bridge);
	                } else if (tm.getRow(t) == ROW_CLIFF_LEVEL) {
	                	Level.setTheme(Theme.Minecart);
	                } else if (tm.getColumn(t) == (tm.getWidth() - 5)) {
	                    Level.setTheme(Theme.Night);
	                }
	            	screen = new FurGuardiansGame.PlatformScreen();
	            }
	            Level.clear(); // Called automatically for Level, but not for Cabin
	        	fadeOut(screen);
	        	FurGuardiansGame.playTransition(FurGuardiansGame.musicLevelStart);
			} else if (interaction.KEY_TAB.isActive()) {
				interaction.KEY_TAB.inactivate();
				modeMove = (short) ((modeMove + 1) % 3);
			} else if (interaction.KEY_F1.isActive()) {
				engine.captureScreen();
			} else if (Panput.isActive(getMenuInput(ctrl), endListener)) {
			    clearHelp();
				goMenu(getMenuInput(ctrl), pc);
			}
		}
		
		@Override
		protected final void onWalking() {
		    clearHelp();
		}
		
		private final void goMenu(final Panput input, final PlayerContext pc) {
			input.inactivate();
			ProfileScreen.currentTab = ProfileScreen.TAB_SELECT_AVATAR;
		    fadeOut(new ProfileScreen(pc, true));
		}
		
		private final void fadeOut(final Panscreen screen) {
		    disabled = true;
		    FurGuardiansGame.fadeOut(room, screen);
		}
		
		private void setPos(final int t) {
			setPosition(tm, t);
		}
		
		protected boolean go(final Direction d0) {
		    final int t0 = getIndex();
			if (modeMove == MOVE_ANY_TILE) {
				final int tmp = tm.getNeighbor(t0, d0);
				if (tm.getTile(tmp) != null) {
					setPos(tmp);
				}
				return true;
			}
			int t = t0;
            Direction d = d0, d1 = null;
            boolean first = true;
		    while (true) {
    		    t = tm.getNeighbor(t, d);
    		    final Tile tile = tm.getTile(t);
    		    if (tile == null || tile.isSolid()) {
    		        return goAdjusted(d0);
    		    }
    		    final byte b = tile.getBehavior();
    		    switch (b) {
    		        case TILE_MARKER :
        		        if (modeMove == MOVE_ANY_PATH || isOpen(t0) || isOpen(t)) {
        		            if (d1 == null) {
        		                walk(d0);
        		                setLadder(getIndex());
        		            } else {
        		                walk(d1, d0);
        		            }
        		            return true;
        		        }
        		        return goAdjusted(d0);
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
    		    if (first) {
    		        if (d != d0) {
    		            d1 = d;
    		        }
    		        first = false;
    		    }
		    }
		}
		
		protected boolean goAdjusted(final Direction d0) {
		    if (d0 == Direction.North || d0 == Direction.South) {
		        return false;
		    }
		    final int t0 = getIndex();
		    for (int i = 0; i < 2; i++) {
    		    final Direction d = (i == 0) ? Direction.North : Direction.South;
    		    int t = t0;
    		    final byte exBehavior;
    		    if (d0 == Direction.East) {
    		        exBehavior = (i == 0) ? TILE_RIGHTDOWN : TILE_RIGHTUP;
    		    } else {
    		        exBehavior = (i == 0) ? TILE_LEFTDOWN : TILE_LEFTUP;
    		    }
    		    while (true) {
    		        t = tm.getNeighbor(t, d);
    		        final Tile tile = tm.getTile(t);
    		        if (tile == null) {
    		        	break;
    		        }
    		        final int b = tile.getBehavior();
    		        if (b == exBehavior) {
    		            return go(d);
    		        } else if (b != TILE_VERT) {
    		            break;
    		        }
    		    }
		    }
		    return false;
		}
		
		private final Direction getOther(final Direction c, final Direction o1, final Direction o2) {
		    return c.getOpposite() == o1 ? o2 : o1;
		}
		
		@Override
	    protected void onFace(final Direction oldDir, final Direction newDir) {
			if (newDir == Direction.North) {
			    if (!onLadder) {
			        changeView(pc.mapNorth);
			    }
			} else if (newDir == Direction.East) {
			    changeView(pc.mapEast);
			} else if (newDir == Direction.West) {
			    changeView(pc.mapWest);
			} else if (newDir == Direction.South) {
			    if (!onLadder) {
			        changeView(pc.mapSouth);
			    }
			}
	    }
		
		private final void setLadder(final int index) {
		    onLadder = isLadder(index);
            if (onLadder) {
                changeView(pc.mapLadder);
            }
		}
		
		@Override
		protected void onWalked() {
		    final int t = getIndex();
		    final boolean oldLadder = onLadder;
		    setLadder(t);
			final byte b = tm.getTile(t).getBehavior();
			switch (b) {
				case TILE_MARKER :
					setPlayerPosition(t);
					stillTimer = oldLadder ? 0 : 15;
					return;
				case TILE_VERT : {
					final Direction d1 = getDirection();
					final int t2 = getDestination(d1);
					final byte b2 = tm.getTile(t2).getBehavior();
					if (b2 == TILE_LEFTUP || b2 == TILE_LEFTDOWN) {
						walk(Direction.West, d1);
					} else if (b2 == TILE_RIGHTUP || b2 == TILE_RIGHTDOWN) {
						walk(Direction.East, d1);
					} else {
					    if (isLadder(t2)) {
					        changeView(pc.mapLadder);
					        onLadder = true; // But don't set to false if old Tile was ladder
					    }
						walk(d1);
					}
					return;
				}
				case TILE_HORIZ : {
					final Direction d1 = getDirection();
					final byte b2 = tm.getTile(getDestination(d1)).getBehavior();
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
		return Mathtil.rand(75) ? base : imgMap[4][Mathtil.randi(0, 6)];
	}
	
	private final static Pair<Integer, Integer> getKey(final int index) {
	    return Pair.get(Integer.valueOf(tm.getRow(index)), Integer.valueOf(tm.getColumn(index)));
	}
	
	private final static boolean isOpen(final int index) {
	    return getOpen().get(getKey(index)) != null;
    }
	
	private final static int getNumberOfPaths(final int index) {
		try {
		    int n = 0;
		    for (final Direction dir : Direction.values()) {
		    	final Tile tile = tm.getTile(tm.getNeighbor(index, dir));
		    	if (tile == null) {
		    		continue;
		    	}
		        final int b = tile.getBehavior();
		        if (b == Tile.BEHAVIOR_SOLID || b == TILE_SPECIAL || b == Tile.BEHAVIOR_OPEN) {
		            continue;
		        }
		        n++;
		    }
		    return n;
		} catch (final Exception e) {
			final String dims;
			if (tm == null) {
				dims = "?";
			} else {
				dims = tm.getWidth() + "*" + tm.getHeight();
			}
			throw new Panception("Error getNumberOfPaths ind=" + index + "; dims=" + dims, e);
		}
	}
	
	private final static void loadImages() {
	    final String mapFile = getMapFile();
	    oldMap = Iotil.exists(mapFile);
	    if (oldMap) {
	        final SegmentStream in = SegmentStream.openLocation(mapFile);
            try {
                final Segment seg = in.readRequire(SEG_MAP);
                bgTexture = seg.intValue(1);
                bgColor = seg.intValue(2);
                lm1 = seg.intValue(5);
                lm2 = seg.intValue(6);
                cstl = seg.intValue(7);
                seed = seg.getLong(9, 0);
                theme = getTheme(seg.getValue(10));
            } catch (final IOException e) {
                throw new RuntimeException(e);
            } finally {
                in.close();
            }
	    } else {
    	    bgTexture = Mathtil.randi(0, FurGuardiansGame.dirts.length - 1);
    	    bgColor = Mathtil.randi(0, 2);
    	    final Statistics stats = getProfile().stats;
    	    final int worlds = stats.defeatedWorlds;
    	    if (theme == MapTheme.Rock) {
    	        stats.playedRockWorlds++;
    	    }
			if (worlds == 1) {
				theme = MapTheme.Snow;
			} else if (worlds == 3) {
				theme = MapTheme.Sand;
			} else if (worlds <= 5) {
				theme = MapTheme.Normal;
			} else {
				final MapTheme old = theme;
				do {
					theme = Mathtil.rand(themes);
				} while (old == theme);
			}
			lm1 = Mathtil.randi(0, theme.maxLandmark);
			do {
			    lm2 = Mathtil.randi(0, theme.maxLandmark);
			} while (lm2 == lm1);
			if (lm1 == 1 || (lm2 != 1 && lm2 < lm1)) {
			    final int t = lm1;
			    lm1 = lm2;
			    lm2 = t;
			}
			cstl = Mathtil.randi(0, MAX_CASTLE);
			seed = Mathtil.newSeed();
	    }
	    Level.initTheme();
		Img tileImg = ImtilX.loadImage("org/pandcorps/furguardians/res/bg/Map" + Chartil.unnull(theme.img) + ".png", 128, null);
		applyLandmark(tileImg, 0, lm1, 0);
		applyLandmark(tileImg, 48, lm2, 1);
		if (cstl > 0) {
			// Load castles and landmarks in loadConstants?
			final Img[] castles = ImtilX.loadStrip("org/pandcorps/furguardians/res/bg/Castles.png");
		    final Img lmImg = castles[cstl - 1];
	        Imtil.copy(lmImg, tileImg, 0, 0, ImtilX.DIM, ImtilX.DIM, 112, 96);
	        Img.close(castles);
		}
		Level.applyDirtTexture(tileImg, 48, 0, 96, 16);
		final Img terrain = Level.getDarkenedTerrain(Level.getTerrainTexture());
		Level.backgroundBuilder = new Level.HillBackgroundBuilder();
		Level.applyTerrainTexture(tileImg, 48, 32, 96, 48, terrain, Level.getTerrainMask(1));
		terrain.close();
		Level.applyColoredTerrain(tileImg, 48, 32, 48, 16);
		timg = Pangine.getEngine().createImage("img.map", tileImg);
	}
	
	private final static void applyLandmark(final Img tileImg, final int x, final int lm, final int max) {
	    if (lm == max) {
	        return;
	    }
	    final int lmd = 48;
	    final Img lmImg = ImtilX.loadImage("org/pandcorps/furguardians/res/bg/Landmark" + EXT_LANDMARKS[lm - 2] + ".png", lmd, null);
	    Imtil.copy(lmImg, tileImg, 0, 0, lmd, lmd, x, 80);
	    lmImg.close();
	}
	
	private final static int loadMap() {
		Panctor.destroy(markers);
		Panctor.destroy(buildings);
		Panctor.destroy(portal);
		portal = null;
	    int t;
		//for (int i = 0; i < 100; i++) { // For testing rarely randomly generating errors
	        //tm.destroy(); tm = null; destroy/clear markers
			t = loadMap2();
		//}
		return t;
	}
	
	private final static void clear() {
		FurGuardiansGame.room.destroy();
		for (final PlayerContext pc : FurGuardiansGame.pcs) {
		    pc.player = null;
		}
		Level.numEnemies = 0;
	}
	
	private final static int loadMap2() {
		final String mapFile = getMapFile();
		final Mapper b;
		final Segment mrk, bld;
		royAvt = new Avatar();
		if (Iotil.exists(mapFile)) {
			final SegmentStream in = SegmentStream.openLocation(mapFile);
			try {
			    final Segment seg = in.readRequire(SEG_MAP);
	            name = seg.getValue(0);
	            //bgTexture, bgColor already handled in loadImages; open in Profile
	            endColumn = seg.intValue(3);
	            endRow = seg.intValue(4);
	            royCrown = seg.getInt(8, 0);
	            mrk = in.readRequire(SEG_MRK);
	            bld = in.readRequire(SEG_BLD);
	            final Segment avt = in.readIf(FurGuardiansGame.SEG_AVT);
	            if (avt != null) {
	            	royAvt.load(avt);
	            } else {
	            	royAvt.randomize(); // Change readIf to readRequire; remove condition/randomize
	            }
				tm = TileMap.load(TileMap.class, in, timg);
				roomW = tm.getWidth() * tm.getTileWidth();
				roomH = tm.getHeight() * tm.getTileHeight();
			} catch (final IOException e) {
				throw new RuntimeException(e);
			} finally {
				in.close();
			}
			b = null;
		} else {
			getOpen().clear();
		    name = generateName();
			b = new RandomMapper();
			roomW = b.getW();
			roomH = b.getH();
			mrk = null;
			bld = null;
           	royAvt.randomize();
           	royAvt.eye = Mathtil.randElemI(EYES_ROY);
           	royAvt.col.randomizeColorful();
           	royAvt.clothing.clth = Mathtil.rand(Avatar.hiddenClothings);
           	//royAvt.clothingCol.load(royAvt.col); //negate();
           	royAvt.clothing.col.randomizeColorfulDifferent(royAvt.col);
           	royCrown = Mathtil.randi(0, FurGuardiansGame.crowns.length - 1);
		}
		initRoom();
		initTileMap(tm);
        imgMap = tm.splitImageMap();
        water = imgMap[5][6];
        waters = new TileMapImage[] {water, imgMap[6][6], imgMap[7][6]};
        base = imgMap[1][1];
        ladder = imgMap[0][6];
        bridge = imgMap[0][7];
        final int t;
        if (b == null) {
        	//column, row handled in Profile
        	for (final Field f : mrk.getRepetitions(0)) {
				addMarker(tm.getIndex(f.intValue(0), f.intValue(1)));
			}
        	for (final Field f : bld.getRepetitions(0)) {
        		addBuilding(tm.getIndex(f.intValue(0), f.intValue(1)), f.intValue(2), f.intValue(3));
        	}
        	t = getStartTile();
        } else {
			tm.fillBackground(water, true);
			b.init();
			t = getStartTile();
			b.build();
			saveMap();
        }
        Panctor.destroy(portal);
        portal = new Panctor();
        portal.setView(FurGuardiansGame.portal);
        addBuilding(tm.getIndex(endColumn, getPortalRow()), portal);
        return t;
	}
	
	private final static int getPortalRow() {
	    return endRow + ((endRow <= 8) ? 2 : -2);
	}
	
	private final static void initTileMap(final TileMap tm) {
	    tm.setImageMap(timg);
        //tm.setTileListener(mtl);
	}
	
	private final static void initRoom() {
	    room = FurGuardiansGame.createRoom(roomW, roomH);
        if (tm == null) {
            tm = new TileMap("act.tilemap", room, ImtilX.DIM, ImtilX.DIM);
        } else {
            for (final Marker m : markers) {
                room.addActor(m);
                m.setView(isOpen(tm.getContainer(m)));
            }
            for (final Building b : buildings) {
            	room.addActor(b);
            	b.setView(tm, imgMap[b.ij][b.ii]);
            }
            if (portal != null) {
                room.addActor(portal);
            }
        }
        if (tm.getForegroundDepth() != DEPTH_FOREGROUND) {
        	tm.setOccupantDepth(DepthMode.Y);
            tm.setOccupantBaseDepth(tm.getOccupantBaseDepth());
            tm.setForegroundDepth(DEPTH_FOREGROUND);
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
				tm.setBackground(i, j, imgMap[0][4]);
			}
			tm.setForeground(i, 1, imgMap[1][4]);
			tm.setForeground(i, 3, imgMap[2][1]);
			for (int j = 4; j < 10; j++) {
				tm.setBackground(i, j, getBaseImage());
			}
			tm.setForeground(i, 10, imgMap[0][1]);
		}
		mountain(5, 11, 3);
		mountain(10, 9, 3);
		for (int j = 1; j < 4; j++) {
			tm.setBackground(1, j, imgMap[0][3]);
			tm.setBackground(14, j, imgMap[0][5]);
		}
		tm.setForeground(1, 1, imgMap[1][3]);
		tm.setForeground(14, 1, imgMap[1][5]);
		for (int j = 4; j < 10; j++) {
			tm.setForeground(1, j, imgMap[1][0]);
			tm.setForeground(14, j, imgMap[1][2]);
		}
		tm.setForeground(1, 3, imgMap[2][0]);
		tm.setForeground(14, 3, imgMap[2][2]);
		tm.setForeground(1, 10, imgMap[0][0]);
		tm.setForeground(14, 10, imgMap[0][2]);
		
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
			final Profile prf = getProfile();
			final int column = prf.column, row = prf.row;
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
				        //TODO Set open to true when walk to Building to allow it to be skipped
				        building(c2, nr + dir * 4, 7, 7); // Bonus unlocked by playing optional Level
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
			building(endColumn, endRow, 6, 7);
			final int portalRow = getPortalRow(), midRow = (endRow + portalRow) / 2;
			tm.setBackground(endColumn, midRow, base, TILE_SPECIAL);
			tm.setBackground(endColumn, portalRow, getBuildingBackground(), TILE_SPECIAL);
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
	
	private final static int ROW_CLIFF_LEVEL = 15;
	
	private final static void cliff(final int m) {
		final int n = m - 1, x = m + 1;
		tm.setForeground(n, 16, imgMap[0][0]);
		tm.setForeground(m, 16, imgMap[0][1]);
		tm.setForeground(x, 16, imgMap[0][2]);
		tm.setForeground(n, 15, imgMap[1][0]);
		tm.setForeground(m, 15, (Panmage) null);
		marker(m, ROW_CLIFF_LEVEL);
		tm.setForeground(x, 15, imgMap[1][2]);
		tm.setImages(n, 14, imgMap[0][3], imgMap[2][0]);
		tm.setTile(m, 14, imgMap[0][4], imgMap[2][6], TILE_VERT);
		tm.setImages(x, 14, imgMap[0][5], imgMap[2][2]);
		tm.setImages(n, 13, imgMap[0][3], imgMap[0][2]);
		tm.setTile(m, 13, imgMap[0][4], imgMap[0][6], TILE_VERT);
		tm.setImages(x, 13, imgMap[0][5], imgMap[0][0]);
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
				tm.setForeground(i, t + 1, imgMap[0][1]);
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
		tm.setImages(x, y - 1, terrain, imgMap[2][wx - 3]);
		tm.setBackground(x, y - 2, terrain);
		tm.setImages(x, y - 3, terrain, imgMap[1][wx]);
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
			tm.setBackground(x, j, getBaseImage());
		}
	}
	
	private final static void topUp(final int x, final int y) {
		tm.setForeground(x, y + 1, imgMap[0][0]);
	}
	
	private final static void topDown(final int x, final int y) {
		tm.setForeground(x, y + 1, imgMap[0][2]);
	}
	
	private final static int getStartTile() {
		final Profile prf = getProfile();
		return tm.getIndex(prf.column, prf.row);
	}
	
	private final static PlayerContext getPlayerContext() {
		return FurGuardiansGame.pcs.get(0);
	}
	
	private final static Profile getProfile() {
		return getPlayerContext().profile;
	}
	
	private final static HashMap<Pair<Integer, Integer>, Boolean> getOpen() {
		return getProfile().open;
	}
	
	private final static void addPlayer(final int index) {
		final PlayerContext pc = getPlayerContext();
		if (pc != null && pc.ctrl != null) {
			final Panput act1 = pc.ctrl.get1();
			if (act1 != null) {
				act1.inactivate();
			}
		}
		player = new MapPlayer(pc);
		player.setPos(index);
		room.addActor(player);
		Pangine.getEngine().track(player);
	}
	
	private final static void addBorder() {
		final Pangine engine = Pangine.getEngine();
		if (room.center()) {
		    final int maxW = engine.getEffectiveWidth(), maxH = engine.getEffectiveHeight();
		    final Panple size = room.getSize(), origin = room.getOrigin();
		    final float layerW = size.getX(), layerH = size.getY();
		    final float ox = origin.getX(), oy = origin.getY();
		    if (layerW < maxW) {
		        final float bordW = -ox, bordH = Math.max(maxH, layerH);
		        final int tilesX = Mathtil.ceil(bordW / ImtilX.DIM), tilesY = Mathtil.ceil(bordH / ImtilX.DIM);
		        addBorder("left", tilesX, tilesY, -tilesX * ImtilX.DIM, oy);
		        addBorder("right", tilesX, tilesY, layerW, oy);
		    }
		    if (layerH < maxH) {
		        final float bordW = layerW, bordH = -oy;
		        final int tilesX = Mathtil.ceil(bordW / ImtilX.DIM), tilesY = Mathtil.ceil(bordH / ImtilX.DIM);
                addBorder("top", tilesX, tilesY, 0, layerH);
                addBorder("bottom", tilesX, tilesY, 0, oy);
		    }
		}
	}
	
	private final static TileMap addBorder(final String name, final int tx, final int ty, final float px, final float py) {
	    final TileMap bord = new TileMap("act.border." + name, tx, ty, ImtilX.DIM, ImtilX.DIM);
        initTileMap(bord);
        bord.fillBackground(water, true);
        room.addActor(bord);
        bord.getPosition().set(px, py);
        return bord;
	}
	
	private final static void addHud(final boolean victoryWorld) {
	    final Panlayer hud = FurGuardiansGame.addHud(room, false, false);
	    final Pantext name = addText(Map.name, FurGuardiansGame.SCREEN_W / 2, 1);
	    final Pangine engine = Pangine.getEngine();
	    if (!victoryWorld) {
		    tipMover = new TextMover(hud, FurGuardiansGame.font, FurGuardiansGame.tips, (tipMover == null) ? 0 : tipMover.getIndex(),
		    		engine.getEffectiveHeight() - 36, -10);
	    }
		PlayerScreen.initTouchButtons(hud, getPlayerContext().ctrl);
		if (engine.isTouchSupported()) {
			final Panteraction interaction = engine.getInteraction();
			final ActionEndListener lsn = new ActionEndListener() {
				@Override public final void onActionEnd(final ActionEndEvent event) {
					if (player != null) {
						player.goMenu(event.getInput(), getPlayerContext());
					}
				}};
			name.register(interaction.MENU, lsn);
			name.register(interaction.BACK, lsn);
		}
	}
	
	private final static Pantext addText(final String s, final int x, final int y) {
	    final Pantext name = new Pantext(Pantil.vmid(), FurGuardiansGame.font, s);
        name.getPosition().set(x, y, PlayerScreen.TOUCH_BUTTON_DEPTH + 10);
        name.centerX();
        FurGuardiansGame.hud.addActor(name);
        return name;
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
			final TileMapImage b = getBaseImage();
			for (int i = x; i <= stop; i++) {
				tm.setImages(i, yshadow, b, b);
			}
		}
	}
	
	private static void landmark(final int x, final int y, final int ix, final int[] used) {
		used[getIndex(x + 1)] = y + 1;
		for (int j = 0; j < 3; j++) {
			final int yj = y + j, j7 = 7 - j;
			for (int i = 0; i < 3; i++) {
				final int xi = x + i;
				setForeground(xi, yj, j7, ix + i);
				tm.setBehavior(xi, yj, TILE_SPECIAL);
			}
		}
	}
	
	private static Tile setForeground(final int x, final int y, final int ij, final int ii) {
		final int index = tm.getIndex(x, y);
		final Tile t = tm.getTile(index);
		if (isWater(t)) {
			tm.setForeground(index, imgMap[ij][ii]);
		} else {
			tm.setImages(index, base, imgMap[ij][ii]);
		}
		return t;
	}
	
	private static boolean isWater(final int x, final int y) {
		return isWater(tm.getTile(x, y));
	}
	
	private static boolean isWater(final Tile t) {
		return DynamicTileMap.getRawBackground(t) == water;
	}
	
	private static boolean isLadder(final int index) {
	    return DynamicTileMap.getRawForeground(tm.getTile(index)) == ladder;
	}
	
	private static boolean isBridge(final int index) {
        return DynamicTileMap.getRawForeground(tm.getTile(index)) == bridge;
    }
	
	private static void marker(final int i, final int j) {
		marker(i, j, true);
	}
	
	private static void marker(final int i, final int j, final boolean bg) {
		final int index = tm.getIndex(i, j);
		if (bg) {
			tm.setBackground(index, imgMap[3][0], TILE_MARKER);
		} else {
			tm.setBehavior(index, TILE_MARKER);
		}
		addMarker(index);
	}
	
	private final static void addMarker(final int index) {
		final Marker m = new Marker(isOpen(index));
		markers.add(m);
		//m.setPosition(tile);
		final Panple pos = m.getPosition();
		tm.savePosition(pos, index);
		setZ(pos, DEPTH_MARKER);
		room.addActor(m);
	}
	
	private final static void building(final int i, final int j, final int ij, final int ii) {
		tm.setBackground(i, j, getBuildingBackground(), TILE_MARKER);
		addBuilding(tm.getIndex(i, j), ij, ii);
	}
	
	private final static TileMapImage getBuildingBackground() {
	    return imgMap[3][7];
	}
	
	private final static void addBuilding(final int index, final int ij, final int ii) {
        final Building b = new Building(ij, ii);
        buildings.add(b);
        addBuilding(index, b);
	}
	
	private final static void addBuilding(final int index, final Panctor b) {
        final Panple tilePos = tm.getPosition(index);
        b.getPosition().set(tilePos.getX(), tilePos.getY() + 7);
        TileOccupant.setZ(b, tm);
        room.addActor(b);
	}
	
	private final static Building getBuilding(final int index) {
		for (final Building b : buildings) {
        	if (index == tm.getContainer(b)) {
        		return b;
        	}
        }
		return null;
	}
	
	private final static boolean isCabin(final Building b) {
		return b != null && b.ij == 7;
	}
	
	private final static boolean isCastle(final Building b) {
        return b != null && b.ij == 6;
    }
	
	private final static void setZ(final Panple pos, final int depth) {
	    //pos.setZ(tm.getForegroundDepth() + depth);
		pos.setZ(depth);
	}
	
	private static void horiz(final int i, final int j) {
		final int index = tm.getIndex(i, j);
		final Tile t = tm.getTile(index);
		if (isWater(t)) {
			tm.setForeground(index, imgMap[0][7], TILE_HORIZ);
		} else if (isWater(i - 1, j)) {
			tm.setBackground(index, imgMap[1][6], TILE_HORIZ);
		} else if (isWater(i + 1, j)) {
			tm.setBackground(index, imgMap[1][7], TILE_HORIZ);
		} else {
			tm.setBackground(index, imgMap[3][1], TILE_HORIZ);
		}
	}
	
	private static void vert(final int i, final int j) {
		tm.setBackground(i, j, imgMap[3][2], TILE_VERT);
	}
	
	private static void leftUp(final int i, final int j) {
		tm.setBackground(i, j, imgMap[3][3], TILE_LEFTUP);
	}
	
	private static void rightUp(final int i, final int j) {
		tm.setBackground(i, j, imgMap[3][4], TILE_RIGHTUP);
	}
	
	private static void leftDown(final int i, final int j) {
		tm.setBackground(i, j, imgMap[3][5], TILE_LEFTDOWN);
	}
	
	private static void rightDown(final int i, final int j) {
		tm.setBackground(i, j, imgMap[3][6], TILE_RIGHTDOWN);
	}
	
	private final static String generateName() {
		//return Mathtil.rand(ADJECTIVES) + ' ' + Mathtil.rand(NATURES) + ' ' + Mathtil.rand(PLACES);
	    //return theme.nmr.get();
		return "World " + (getProfile().stats.defeatedWorlds + 1);
	}
	
	private final static void setPlayerPosition(final int index) {
		setPlayerPosition(tm.getColumn(index), tm.getRow(index));
	}
	
	private final static void setPlayerPosition(final int column, final int row) {
		final Profile prf = getProfile();
		prf.column = column;
		prf.row = row;
	}
	
	private final static String getMapFile() {
		return getProfile().getMapFileName();
	}
	
	protected final static void saveMap() {
	    final Writer w = Iotil.getBufferedWriter(getMapFile());
	    try {
	        final Segment seg = new Segment(SEG_MAP);
	        seg.setValue(0, name);
	        seg.setInt(1, bgTexture);
	        seg.setInt(2, bgColor);
	        seg.setInt(3, endColumn);
	        seg.setInt(4, endRow);
	        seg.setInt(5, lm1);
            seg.setInt(6, lm2);
            seg.setInt(7, cstl);
            seg.setInt(8, royCrown);
            seg.setLong(9, seed);
            seg.setValue(10, theme.name);
	        seg.saveln(w);
	        final Segment mrk = new Segment(SEG_MRK);
	        final ArrayList<Field> mlist = new ArrayList<Field>(markers.size());
	        for (final Marker m : markers) {
	        	final Field f = new Field();
	        	final int tile = tm.getContainer(m);
	        	f.setInt(0, tm.getColumn(tile));
	        	f.setInt(1, tm.getRow(tile));
				mlist.add(f);
			}
	        mrk.setRepetitions(0, mlist);
	        mrk.saveln(w);
	        final Segment bld = new Segment(SEG_BLD);
	        final ArrayList<Field> alist = new ArrayList<Field>(buildings.size());
	        for (final Building b : buildings) {
	        	final Field f = new Field();
	        	final int tile = tm.getContainer(b);
	        	f.setInt(0, tm.getColumn(tile));
	        	f.setInt(1, tm.getRow(tile));
	        	f.setInt(2, b.ij);
	        	f.setInt(3, b.ii);
				alist.add(f);
			}
	        bld.setRepetitions(0, alist);
	        bld.saveln(w);
	        Segtil.saveln(royAvt, w);
	        tm.save(w);
	    } catch (final IOException e) {
	        throw new RuntimeException(e);
	    } finally {
	        Iotil.close(w);
	    }
	}
	
	/*public final static void main(final String[] args) {
	    for (final MapTheme theme : themes) {
	        System.out.println("Theme " + theme.name);
	        System.out.println();
	        theme.nmr.printDemo();
	        System.out.println();
	    }
	}*/
}
