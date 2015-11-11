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

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.furguardians.Enemy.*;
import org.pandcorps.furguardians.Player.*;
import org.pandcorps.furguardians.Profile.*;
import org.pandcorps.furguardians.Spawner.*;

public class Level {
	private final static int DEF_ROOM_H = 256;
    protected static int ROOM_H = DEF_ROOM_H;
    
    protected final static int HOB_TROLL = 0;
    protected final static int HOB_OGRE = 1;
    protected final static int TROLL = 2;
    protected final static int OGRE = 3;
    protected final static int TROLL_COLOSSUS = 4;
    protected final static int OGRE_BEHEMOTH = 5;
    protected final static int IMP = 6;
    protected final static int ARMORED_IMP = 7;
    protected final static int SPIKED_IMP = 8;
    protected final static int DROWID = 9;
    protected final static int DROLOCK = 10;
    protected final static int ICE_WISP = 11;
    protected final static int FIRE_WISP = 12;
    protected final static int ROCK_SPRITE = 13;
    protected final static int ROCK_TRIO = 14;
    protected final static int BLOB = 15;
    protected final static int BLACK_BLOB = 16;
    
    private final static byte FLOOR_GRASSY = 0;
    private final static byte FLOOR_BLOCK = 1;
    private final static byte FLOOR_BRIDGE = 2;
    private final static byte FLOOR_TRACK = 3;
    
    private final static byte HEX_RISE = 0;
    private final static byte HEX_UP = 1;
    private final static byte HEX_FALL = 2;
    private final static byte HEX_DOWN = 3;
    
    private final static int LAST_DEFEATED_LEVEL_COUNT_TO_FORCE_BACKGROUND = 1;
    private final static int LAST_DEFEATED_LEVEL_COUNT_TO_FORCE_ENEMY = 2;
    
    protected final static PixelFilter terrainDarkener = new BrightnessPixelFilter((short) -40, (short) -24, (short) -32);
    
    private final static Set<Class<? extends Template>> oneUseTemplates = new HashSet<Class<? extends Template>>();
    
    protected static TileMapImage[] flashBlock = null;
    private static TileMapImage[] extraAnimBlock = null;
    
    protected static long seed = -1;
    protected static Panroom room = null;
    protected static Theme theme = null;
    protected static BackgroundBuilder backgroundBuilder = null;
    protected static Builder builder = null;
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
    private static int floatOffset = 0;
    protected static int goalIndex = 0;
    private static byte floorMode = FLOOR_GRASSY;
    private static Pancolor topSkyColor = null;
    private static Pancolor bottomSkyColor = null;
    protected static Tile tileGem = null;
    protected static Tile tileTrackTop = null;
    protected static Tile tileTrackBase = null;
    protected static int numEnemies = 0;
    protected static int numGems = 0;
    protected static int numBreakable = 0;
    protected static int numPower = 0;
    protected static int currLetter = 0;
    protected static List<Panctor> collectedLetters = null;
    protected static List<Panctor> uncollectedLetters = null;
    protected static boolean victory = false;
    
    static {
    	oneUseTemplates.add(GemMsgTemplate.class);
    	oneUseTemplates.add(GiantTemplate.class);
    }
    
    protected abstract static class Theme {
    	private final static String[] MSG = {"PLAYER", "GEMS!!!", "HURRAY", "GO GO!", "YAY", "GREAT", "PERFECT"};
    	private final static int[] NORMAL_ENEMIES = {HOB_TROLL, HOB_OGRE, TROLL, OGRE, IMP, ARMORED_IMP};
    	private final static int[] getNormalEnemies(final int... extra) {
    		return getCombinedEnemies(NORMAL_ENEMIES, extra);
    	}
    	private final static int[] getCombinedEnemies(final int[] normal, final int... extra) {
    		final int nlen = normal.length, elen = extra.length;
    		final int[] a = new int[nlen + elen];
    		System.arraycopy(normal, 0, a, 0, nlen);
    		System.arraycopy(extra, 0, a, nlen, elen);
    		return a;
    	}
    	protected Pancolor getTopSkyColor(final Img img) {
    		return Imtil.getColor(img, 96, 0);
    	}
    	protected Pancolor getBottomSkyColor(final Img img) {
    		return Imtil.getColor(img, 96, 32);
    	}
    	public final static Theme Normal = new Theme(null, MSG) {
    	    @Override protected final int[] getEnemyIndices(final int worlds, final int levels) {
    	        switch (worlds) {
    	            case 0 :
    	            	if (levels == 0) {
    	            		return new int[] {HOB_TROLL};
    	            	} else if (levels == 1) {
    	            		return new int[] {IMP};
    	            	} else if (levels == LAST_DEFEATED_LEVEL_COUNT_TO_FORCE_ENEMY) {
    	            		return new int[] {HOB_TROLL, HOB_OGRE};
    	            	}
    	            case 1 : return new int[] {HOB_TROLL, HOB_OGRE, IMP}; // 2nd world is Snow
    	            case 2 : // After 2nd world
    	            case 3 : return new int[] {HOB_TROLL, HOB_OGRE, IMP, ARMORED_IMP}; // 4th world is Sand
    	            default: return NORMAL_ENEMIES; // After 4rd
    	            // troll colossus and ogre behemoth after 5th (see addGiantTemplate)
    	        }
    	    }
    	    
    		@Override protected final BackgroundBuilder getRandomBackground() {
    			switch (getDefeatedLevels()) {
	    			case 0 : return new HillBackgroundBuilder();
	    			case LAST_DEFEATED_LEVEL_COUNT_TO_FORCE_BACKGROUND : return new ForestBackgroundBuilder();
    			}
    			return Mathtil.rand(new HillBackgroundBuilder(), new ForestBackgroundBuilder(), new TownBackgroundBuilder());
    		}
    		
    		@Override protected final Builder getRandomBuilder() {
    			if (backgroundBuilder instanceof HillBackgroundBuilder) {
    				if (getDefeatedLevels() <= getLastDefeatedLevelCountToForceNormal()) {
    					return getBasicBuilder();
    				}
    				return Mathtil.rand() ? new GrassyBuilder() : new PlatformBuilder();
    			} else if (backgroundBuilder instanceof TownBackgroundBuilder) {
    				return new FlatBuilder();
    			} else {
    				return getBasicBuilder();
    			}
    		}
    	};
    	public final static Theme Snow = new Theme("Snow", null, MSG) {
    	    @Override protected final int[] getEnemyIndices(final int worlds, final int levels) {
    	        return worlds < 2 ? new int[] {HOB_TROLL, HOB_OGRE, IMP, ICE_WISP} : getNormalEnemies(ICE_WISP);
    	    }
    	    
    		@Override protected final BackgroundBuilder getRandomBackground() {
    			return Mathtil.rand() ? new HillBackgroundBuilder() : new MountainBackgroundBuilder();
    		}
    		
    		@Override protected final TileMapImage[] getExtraAnimBlock() {
    			final TileMapImage[] row = imgMap[1];
    	        return new TileMapImage[] {row[5], row[6], row[7]};
        	}
    		
    		@Override protected Pansound getMusic() {
                return FurGuardiansGame.musicSnow;
            }
    		
    		@Override protected final void flash(final long i) {
    			if (i < 3 && extraAnimBlock != null) {
    				Tile.animate(extraAnimBlock);
    			}
        	}
    		
    		@Override protected final byte getSpecialGroundBehavior() {
                return FurGuardiansGame.TILE_ICE;
            }
    	};
    	public final static Theme Sand = new Theme("Sand", null, MSG) {
    	    @Override protected final int[] getEnemyIndices(final int worlds, final int levels) {
    	    	return worlds < 4 ? new int[] {HOB_TROLL, HOB_OGRE, IMP, ARMORED_IMP, FIRE_WISP} : getNormalEnemies(FIRE_WISP);
    	    }
    	    
    		@Override protected final BackgroundBuilder getRandomBackground() {
    			return new HillBackgroundBuilder();
    		}
    		
    		@Override protected final TileMapImage[] getExtraAnimBlock() {
    			final TileMapImage[] row = imgMap[1];
    	        return new TileMapImage[] {row[5], row[6], row[7]};
        	}
    		
    		@Override protected Pansound getMusic() {
                return FurGuardiansGame.musicSand;
            }
    		
    		@Override protected final void step(final long clock) {
    			if (clock % 6 == 0 && extraAnimBlock != null) {
    				Tile.animate(extraAnimBlock);
    			}
        	}
    		
    		@Override protected final byte getSpecialGroundBehavior() {
                return FurGuardiansGame.TILE_SAND;
            }
    	};
    	public final static Theme Rock = new Theme("Rock", null, MSG) {
    	    @Override protected final int[] getEnemyIndices(final int worlds, final int levels) {
    	    	return getNormalEnemies(ROCK_SPRITE, ROCK_TRIO);
    	    }
    	    
    		@Override protected final BackgroundBuilder getRandomBackground() {
    			return new HillBackgroundBuilder();
    		}
    		
    		@Override protected Pansound getMusic() {
                return FurGuardiansGame.musicRock;
            }
    	};
    	public final static Theme Hive = new Theme("Hive", null, MSG) {
            @Override protected final int[] getEnemyIndices(final int worlds, final int levels) {
                return NORMAL_ENEMIES;
            }
            
            @Override protected final BackgroundBuilder getRandomBackground() {
                return new HillBackgroundBuilder();
            }
            
            @Override protected final Builder getBasicBuilder() {
                return new HexBuilder();
            }
            
            @Override protected final int getGroundWidthOffset() {
                return 1;
            }
            
            @Override protected Pansound getMusic() {
                return FurGuardiansGame.musicHive;
            }
        };
    	public final static Theme Bridge = new Theme("Bridge", MSG) {
            @Override protected final int[] getEnemyIndices(final int worlds, final int levels) {
                return Map.theme.levelTheme.getEnemyIndices(worlds, levels);
            }
            
            @Override protected final BackgroundBuilder getRandomBackground() {
                return new HillBackgroundBuilder();
            }
            
            @Override protected final Builder getBasicBuilder() {
                return new BridgeBuilder();
            }
            
            @Override protected final String getBgImg() {
                return Map.theme.levelTheme.bgImg;
            }
            
            @Override protected Pansound getMusic() {
                return FurGuardiansGame.musicBridge;
            }
        };
        public final static Theme Cave = new Theme("Cave", MSG) {
            @Override protected final int[] getEnemyIndices(final int worlds, final int levels) {
            	return getLimitedEnemies(worlds, levels, BLOB);
            }
            
            @Override protected final BackgroundBuilder getRandomBackground() {
                return new CaveBackgroundBuilder();
            }
            
            @Override protected final Builder getBasicBuilder() {
                return new CaveBuilder();
            }
            
            @Override protected final String getBgImg() {
                return Map.theme.levelTheme.bgImg;
            }
            
            @Override protected final Pancolor getTopSkyColor(final Img img) {
        		return getBottomSkyColor(img);
        	}
            
            @Override protected final Pancolor getBottomSkyColor(final Img img) {
        		return Imtil.getColor(img, 50, 80);
        	}
            
            @Override protected Pansound getMusic() {
                return FurGuardiansGame.musicCave;
            }
        };
        public final static Theme Minecart = new Theme("Minecart", MSG) {
            @Override protected final int[] getEnemyIndices(final int worlds, final int levels) {
                return Map.theme.levelTheme.getEnemyIndices(worlds, levels);
            }
            
            @Override protected final BackgroundBuilder getRandomBackground() {
                return new HillBackgroundBuilder();
            }
            
            @Override protected final Builder getBasicBuilder() {
                return new MinecartBuilder();
            }
            
            @Override protected final String getBgImg() {
                return Map.theme.levelTheme.bgImg;
            }
            
            @Override protected Pansound getMusic() {
                return FurGuardiansGame.musicMinecart;
            }
        };
        private final static int[] getLimitedEnemies(final int worlds, final int levels, final int special) {
        	if (worlds < 3) {
                if (Map.theme == Map.MapTheme.Snow) {
                	return new int[] {special, ICE_WISP};
                } else if (Map.theme == Map.MapTheme.Sand) {
                	return new int[] {special, FIRE_WISP};
                } else if (Map.theme == Map.MapTheme.Rock) {
                    return new int[] {special, ROCK_SPRITE};
                }
            	return new int[] {special};
        	} else {
        		if (Map.theme == Map.MapTheme.Snow) {
                	return new int[] {special, ICE_WISP, ARMORED_IMP};
                } else if (Map.theme == Map.MapTheme.Sand) {
                	return new int[] {special, FIRE_WISP, ARMORED_IMP};
                } else if (Map.theme == Map.MapTheme.Rock) {
                    return new int[] {special, ROCK_SPRITE, ARMORED_IMP};
                }
            	return new int[] {special, ARMORED_IMP};
        	}
        }
        public final static Theme Night = new Theme("Night", MSG) {
            @Override protected final int[] getEnemyIndices(final int worlds, final int levels) {
            	return getLimitedEnemies(worlds, levels, BLACK_BLOB);
            }
            
            @Override protected final BackgroundBuilder getRandomBackground() {
                return new HillBackgroundBuilder();
            }
            
            @Override protected final Builder getBasicBuilder() {
                return Map.theme.levelTheme.getBasicBuilder();
            }
            
            @Override protected final int getGroundWidthOffset() {
                return Map.theme.levelTheme.getGroundWidthOffset();
            }
            
            @Override protected final String getBgImg() {
                return Map.theme.levelTheme.bgImg;
            }
            
            @Override protected final PixelFilter getSkyFilter() {
                final ReplacePixelFilter f = new ReplacePixelFilter();
                for (int i = 0; i < 5; i++) {
                    final short grey = (short) (48 - (i * 8));
                    f.put((short) (64 + (i * 16)), (short) (64 + (i * 32)), Pancolor.MAX_VALUE, grey, grey, grey);
                }
                return f;
            }
            
            @Override protected final TileMapImage[] getExtraAnimBlock() {
                return Map.theme.levelTheme.getExtraAnimBlock();
            }
            
            @Override protected Pansound getMusic() {
                return FurGuardiansGame.musicNight;
            }
            
            @Override protected final void step(final long clock) {
                Map.theme.levelTheme.step(clock);
            }
            
            @Override protected final void flash(final long i) {
                Map.theme.levelTheme.flash(i);
            }
            
            @Override protected final byte getSpecialGroundBehavior() {
                return Map.theme.levelTheme.getSpecialGroundBehavior();
            }
        };
    	private final static String[] MSG_CHAOS = {"CHAOS", "HAVOC", "BEWARE", "FEAR", "DANGER"};
    	public final static Theme Chaos = new Theme("Chaos", MSG_CHAOS) {
    	    @Override protected final int[] getEnemyIndices(final int worlds, final int levels) {
                switch (worlds) {
                    case 0 :
                    case 1 : return new int[] {DROWID, DROLOCK, IMP};
                    case 2 : return new int[] {DROWID, DROLOCK, IMP, ARMORED_IMP};
                    default: return new int[] {DROWID, DROLOCK, IMP, ARMORED_IMP, SPIKED_IMP};
                }
            }
    	    
    		@Override protected final BackgroundBuilder getRandomBackground() {
    			return new HillBackgroundBuilder();
    		}
    		
    		@Override protected Pansound getMusic() {
        		return FurGuardiansGame.musicHeartbeat;
        	}
    	};
    	
    	protected final String img;
    	protected final String bgImg;
    	protected final String[] gemMessages;
    	
    	private Theme(final String img, final String[] gemMessages) {
    		this(img, img, gemMessages);
    	}
    	
    	private Theme(final String img, final String bgImg, final String[] gemMessages) {
    		this.img = img;
    		this.bgImg = bgImg;
    		this.gemMessages = gemMessages;
    	}
    	
    	private final List<EnemyDefinition> getEnemies() {
    	    final int[] enemies = getEnemyIndices(getDefeatedWorlds(), getDefeatedLevels());
    		final List<EnemyDefinition> list = new ArrayList<EnemyDefinition>(enemies.length);
    		for (final int enemy : enemies) {
    		    list.add(FurGuardiansGame.allEnemies.get(enemy));
    		}
    		return list;
    	}
    	
    	protected abstract int[] getEnemyIndices(final int worlds, final int levels);
    	
    	protected abstract BackgroundBuilder getRandomBackground();
    	
    	protected Builder getBasicBuilder() {
            return new GrassyBuilder();
        }
    	
    	protected Builder getRandomBuilder() {
    	    return getBasicBuilder();
    	}
    	
    	protected int getGroundWidthOffset() {
    	    return 0;
    	}
    	
    	protected String getBgImg() {
    	    return bgImg;
    	}
    	
    	protected PixelFilter getSkyFilter() {
    	    return Map.theme.getSkyFilter();
    	}
    	
    	protected TileMapImage[] getExtraAnimBlock() {
    		return null;
    	}
    	
    	protected Pansound getMusic() {
    		return FurGuardiansGame.musicHappy;
    	}
    	
    	protected void step(final long clock) {
    	}
    	
    	protected void flash(final long i) {
    	}
    	
    	protected byte getSpecialGroundBehavior() {
    	    return Tile.BEHAVIOR_SOLID;
    	}
    }
    
    protected static void setTheme(final Theme theme) {
    	Level.theme = theme;
    	FurGuardiansGame.enemies = theme.getEnemies();
    }
    
    private final static int getLastDefeatedLevelCountToForceNormal() {
    	return Math.max(LAST_DEFEATED_LEVEL_COUNT_TO_FORCE_BACKGROUND, LAST_DEFEATED_LEVEL_COUNT_TO_FORCE_ENEMY);
    }
    
    protected static void initTheme() {
    	setTheme(Map.theme.levelTheme);
    }
    
    protected static void initThemeForNonSpecialMarker() {
    	final int levels = getDefeatedLevels();
    	final Statistics stats = getStatistics();
    	final Theme theme;
    	if (levels <= getLastDefeatedLevelCountToForceNormal()) {
    		theme = Map.theme.levelTheme;
    	} else if (stats != null && stats.playedCaveLevels == 0) {
    		theme = Theme.Cave;
    	} else {
    		Mathtil.setSeed(seed); // Replaying a level should always use same theme
    		final int r = Mathtil.randi(0, 99); // so always set seed and always use first value from it
    		//final int r = (int) (seed % 100); // using seed itself doesn't give variety; all seeds from a map are similar
    		if (r < 20) {
    			theme = Theme.Cave;
    		} else {
    			theme = Map.theme.levelTheme;
    		}
    	}
    	setTheme(theme);
    }
    
    protected static boolean isNormalTheme() {
    	return theme != Theme.Chaos;
    }
    
    private final static Profile getProfile() {
        final PlayerContext pc = Coltil.get(FurGuardiansGame.pcs, 0);
        return (pc == null) ? null : pc.profile;
    }
    
    private final static Statistics getStatistics() {
    	return Profile.getStatistics(getProfile());
    }
    
    private final static int getDefeatedWorlds() {
        final Statistics stats = getStatistics();
        return (stats == null) ? 0 : stats.defeatedWorlds;
    }
    
    private final static int getDefeatedLevels() {
    	final Statistics stats = getStatistics();
        return (stats == null) ? 0 : stats.defeatedLevels;
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
    
    protected final static void applyDirtTexture(final Img tileImg, final int ix, final int iy, final int fx, final int fy) {
        final Img dirt = FurGuardiansGame.dirts[Map.bgTexture];
        final PixelMask tileMask = new AntiPixelMask(new ColorPixelMask(224, 112, 0, Pancolor.MAX_VALUE));
        for (int x = ix; x < fx; x += 16) {
            for (int y = iy; y < fy; y += 16) {
                Imtil.copy(dirt, tileImg, 0, 0, 16, 16, x, y, null, tileMask);
            }
        }
        if (Map.theme.dirtFilter != null) {
        	Imtil.filterImg(tileImg, ix, iy, ((theme == Theme.Cave) ? 128 : fx) - ix, fy - iy, Map.theme.getDirtMask(), Map.theme.dirtFilter);
        }
    }
    
    protected final static Img getTerrainTexture() {
        return FurGuardiansGame.terrains[Map.bgTexture];
    }
    
    protected final static PixelMask getTerrainMask(final int _z) {
    	final int z = _z + backgroundBuilder.getPreDarken();
        return new AntiPixelMask(new ColorPixelMask(196 - 40 * z, 220 - 24 * z, 208 - 32 * z, Pancolor.MAX_VALUE));
    }
    
    protected final static Img getDarkenedTerrain(final Img terrain) {
        return Imtil.filter(terrain, terrainDarkener);
    }
    
    protected final static void applyTerrainTexture(final Img backImg, final int ix, final int iy, final int fx, final int fy, final Img terrain, final PixelMask backMask) {
        for (int x = ix; x < fx; x += 16) {
            for (int y = iy; y < fy; y += 16) {
                Imtil.copy(terrain, backImg, 0, 0, 16, 16, x, y, null, backMask);
            }
        }
    }
    
    protected final static void applyColoredTerrain(final Img backImg, final int x, final int y, final int w, final int h) {
        Imtil.filterImg(backImg, x, y, w, h, getHillFilter(Map.bgColor));
    }
    
    private final static Theme getDayTheme() {
    	return (theme == Theme.Night) ? Map.theme.levelTheme : theme;
    }
    
    private final static Img loadTileImage() {
    	final Img tileImg = ImtilX.loadImage("org/pandcorps/furguardians/res/bg/Tiles.png", 128, null);
    	final Theme theme = getDayTheme();
    	if (theme != Theme.Normal) {
    		final Img ext = ImtilX.loadImage("org/pandcorps/furguardians/res/bg/Tiles" + theme.img + ".png", false);
    		Imtil.copy(ext, tileImg, 0, 0, 128, 112, 0, 16);
    		ext.close();
    	}
    	return tileImg;
    }
    
    protected final static Panmage getTileImage() {
    	final Img tileImg = loadTileImage();
    	if (isNormalTheme() && theme != Theme.Bridge && theme != Theme.Minecart) {
    		applyDirtTexture(tileImg, 0, 16, 80, 128);
    	}
        return Pangine.getEngine().createImage("img.tiles", tileImg);
    }
    
    protected final static void loadLayers() {
    	final Pangine engine = Pangine.getEngine();
    	ROOM_H = Math.max(DEF_ROOM_H, engine.getEffectiveHeight());
        room = FurGuardiansGame.createRoom(w, ROOM_H);
        room.setClearDepthEnabled(false);
        tm = new TileMap("act.tilemap", room, ImtilX.DIM, ImtilX.DIM);
        room.addActor(tm);
        
        timg = getTileImage();
        imgMap = tm.splitImageMap(timg);
        final TileMapImage[] row = imgMap[0];
        flashBlock = new TileMapImage[] {row[0], row[1], row[2], row[3]};
        extraAnimBlock = theme.getExtraAnimBlock();
        
        final Panlayer bg1 = FurGuardiansGame.createParallax(room, 2);
        bg1.setClearDepthEnabled(false);
        bgtm1 = newBackgroundTileMap(1, bg1);
        Img backImg = backgroundBuilder.getImage();
        bgimg = engine.createImage("img.bg", backImg);
        bgMap = bgtm1.splitImageMap(bgimg);
        
        /*
        It would look strange if layers 1 and 3 moved without 2.
        So it's probably best if each layer's master is the one directly above it
        instead of basing all on the foreground with different divisors.
        */
        final Panlayer bg2 = FurGuardiansGame.createParallax(bg1, 2);
        bg2.setClearDepthEnabled(false);
        bgtm2 = newBackgroundTileMap(2, bg2);
        bgtm2.setImageMap(bgtm1);
        
        final Panlayer bg3 = FurGuardiansGame.createParallax(bg2, 2);
        bgtm3 = newBackgroundTileMap(3, bg3);
        bgtm3.setImageMap(bgtm1);
    }
    
    private final static TileMap newBackgroundTileMap(final int i, final Panlayer bg) {
    	final TileMap bgtm = new TileMap("act.bgmap" + i, bg, ImtilX.DIM, ImtilX.DIM);
    	final float d = -10 * i;
    	bgtm.getPosition().setZ(d);
    	bgtm.setForegroundDepth(d + 5);
    	bg.addActor(bgtm);
    	return bgtm;
    }
    
    protected final static void clear() {
        numEnemies = 0;
        enemyProbability = DEFAULT_ENEMY_PROBABILITY;
        numGems = 0;
        numBreakable = 0;
        numPower = 0;
        powerProbability = DEFAULT_POWER_PROBABILITY;
        currLetter = 0;
        Coltil.clear(collectedLetters);
    }
    
    protected final static void loadLevel() {
        Mathtil.setSeed(seed);
        Spawner.setSeed(seed + 1);
        GemBumped.setSeed(seed + 2);
        clear();
        victory = false;
    	floorMode = FLOOR_GRASSY;
    	topSkyColor = null;
	    bottomSkyColor = null;
	    tileGem = null;
	    tileTrackTop = null;
	    tileTrackBase = null;
    	backgroundBuilder = theme.getRandomBackground();
    	builder = theme.getRandomBuilder();
    	w = builder.getW();
    	nt = w / ImtilX.DIM;
    	ng = nt;
    	floor = builder.getFloor();
    	floatOffset = builder.getFloatOffset();
    	loadLayers();
    	addPlayers(); // Add Players while floor has initial value before build() changes it
    	if (theme == Theme.Minecart) {
    		tileTrackTop = tm.getTile(null, imgMap[1][2], Tile.BEHAVIOR_OPEN);
            tileTrackBase = tm.getTile(null, imgMap[2][2], Tile.BEHAVIOR_SOLID);
        }
    	builder.build();
    	/*tm.info();
    	bgtm1.info();
    	bgtm2.info();
    	bgtm3.info();*/
    }
    
    private static abstract class Builder {
    	public abstract int getW();
    	
    	public abstract int getFloor();
    	
    	public int getFloatOffset() {
            return 0;
        }
    	
    	public abstract void build();
    	
    	protected void flatten(final int x, final int w) {
        }
    }
    
    protected final static class DemoBuilder extends Builder {
    	@Override
    	public int getW() {
    		return 768;
    	}
    	
    	@Override
    	public int getFloor() {
    		return 0;
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
        
        buildSky(bgtm3);
        cloud(bgtm3, 10, 10, 7);
        hill(bgtm3, 2, 9, 4, 0, 4);
        cloud(bgtm3, 4, 6, 3);
        hill(bgtm3, 13, 10, 5, 3, 4);
        
        for (int i = 0; i < nt; i++) {
            tm.setForeground(i, 0, imgMap[1][1], Tile.BEHAVIOR_SOLID);
        }
        tm.removeTile(0, 0);
        tm.removeTile(1, 0);
        tm.setForeground(2, 0, imgMap[1][0], Tile.BEHAVIOR_SOLID);
        
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
        
        final EnemyDefinition def = FurGuardiansGame.enemies.get(0);
        new Enemy(def, 80, 64);
        new Enemy(def, 232, 48);
        new Enemy(def, 360, 16);
    }
    
    private static int bx;
    private static int px;
    
    protected abstract static class RandomBuilder extends Builder {
    	protected final ArrayList<Template> templates = new ArrayList<Template>();
        protected final ArrayList<GoalTemplate> goals = new ArrayList<GoalTemplate>();
        
        protected abstract void loadTemplates();
        
        protected final void addTemplate(final Template... a) {
        	templates.add(a.length == 1 ? a[0] : new ChoiceTemplate(a));
        }
        
        protected final void addGiantTemplate() {
            if (isNormalTheme() && getDefeatedWorlds() >= 5) {
                addTemplate(new GiantTemplate());
            }
        }
        
        protected Template getPitTemplate() {
        	return new AnyPitTemplate();
        }
        
        protected final void addFloatTemplates() {
            addTemplate(new BlockBonusTemplate());
            addTemplate(new GemTemplate());
            addTemplate(new GemMsgTemplate());
        }
        
        protected final void addNormalGoals() {
            goals.add(new SlantGoal());
            goals.add(new UpBlockGoal());
            goals.add(new RiseGoal());
            goals.add(new ColorRiseGoal());
        }
        
    	@Override
    	public int getW() {
    		return 3200;
    	}
    	
    	@Override
    	public int getFloor() {
    		return Mathtil.randi(3, 5);
    	}
    	
    	protected int getMaxFloorChange() {
    		return 3;
    	}
    	
    	protected int getMaxFloor() {
    		return 6;
    	}
    	
    	protected boolean changeFloor() {
    		return Mathtil.rand(33);
    	}
    	
    	protected int getFloorChangeWidth() {
    		return 3;
    	}
    	
    	@Override
    	public void build() {
    	    loadTemplates();
    	    
    		backgroundBuilder.build();
    		
    		final GoalTemplate goal = Mathtil.rand(goals);
    		ng = nt - goal.getWidth();
    		
    		px = 0;
    		final int floorLim = getMaxFloor(), bxStart = 8, floorChangeWidth = getFloorChangeWidth();
    		for (bx = bxStart; bx < ng; ) {
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
    		    final int numLetters = FurGuardiansGame.blockWord.length(), ibx = bx;
    		    Template template = null;
    		    for (int i = 0; i < 4; i++) {
    		    	bx = ibx;
    		    	if (bx == bxStart) {
    		    		if (getDefeatedLevels() == 0) {
    		    			// Start first level with Player's name in Gems
        		    	    template = new GemMsgTemplate();
        		    	} else {
	    		    		// Always start with pit to make fall goal easier
	    		    		template = getPitTemplate();
        		    	}
    		    	} else if (currLetter < numLetters && bx >= ng * (currLetter + 1) / (numLetters + 1)) {
	    		    	template = new BlockLetterTemplate();
	    		    } else if (i == 3) {
	    		    	if (theme == Theme.Minecart) {
	    		    		template = new GemTemplate(1);
	    		    	} else {
	    		    		template = Mathtil.rand() ? new BlockBonusTemplate(1) : new GemTemplate(1);
	    		    	}
	    		    } else {
	    		    	template = Mathtil.rand(templates);
	    		    }
	    		    if (oneUseTemplates.contains(template.getClass())) {
	    		    	templates.remove(template);
	    		    }
    		    	template.plan();
    		    	if (bx < ng) {
    		    		break;
    		    	}
    		    }
    		    ground();
    		    if (bx < ng) {
    		    	template.build();
    		    }
    		    if (changeFloor()) {
    		    	if ((bx + floorChangeWidth) < ng) {
    		    		boolean up = Mathtil.rand();
    		    		final int h = Mathtil.randi(0, getMaxFloorChange() - 1);
    		    		if (up) {
    		    			up = (floor + h) < floorLim;
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
    		    	bx += floorChangeWidth;
    		    }
   		    	bx += Mathtil.randi(1, 4);
    		}
    		bx = nt;
    		ground();
    		goal.build();
    		buildCeiling();
    	}
    	
    	protected void buildCeiling() {
    	}
    	
    	protected final void ground() {
    		final int stop = Math.min(bx + 1, nt - 1 + theme.getGroundWidthOffset());
        	ground(px, stop);
        	px = bx + 2;
    	}
    	
    	protected abstract void ground(final int start, final int stop);
    	
    	protected abstract void upStep(final int x, final int y, final int h);
    	
    	protected abstract void downStep(final int x, final int y, final int h);
    }
    
    private static interface BackgroundBuilder {
    	public Img getImage();
    	
    	public int getPreDarken();
    	
    	public void build();
    }
    
    protected static class HillBackgroundBuilder implements BackgroundBuilder {
    	@Override
    	public Img getImage() {
    		final Img backImg = ImtilX.loadImage("org/pandcorps/furguardians/res/bg/Hills" + Chartil.unnull(theme.getBgImg()) + ".png", 128, null);
            if (isNormalTheme()) {
            	final PixelFilter skyFilter = theme.getSkyFilter();
            	if (skyFilter != null) {
            		Imtil.filterImg(backImg, 96, 0, 16, 48, skyFilter);
            	}
            	applyTerrainTexture(backImg, 0, 0, 48, 32);
            	applyColoredTerrain(backImg, 0, 0, 96, 96);
            } else {
            	extractSkyColors(backImg);
            }
            return backImg;
    	}
    	
    	@Override
    	public final int getPreDarken() {
    		return 0;
    	}
    	
    	@Override
    	public final void build() {
    		buildHills(bgtm1, 4, 6, 0, false); // Nearest
    		buildBackHills();
    	}
    }
    
    protected final static class CaveBackgroundBuilder extends HillBackgroundBuilder {
    	@Override
    	public final Img getImage() {
    		final Img backImg = ImtilX.loadImage("org/pandcorps/furguardians/res/bg/Cave.png", 128, null);
        	applyTerrainTexture(backImg, 0, 0, 48, 32);
        	applyTerrainTexture(backImg, 48, 0, 96, 16);
        	applyColoredTerrain(backImg, 0, 0, 96, 96);
        	reextractSkyColors(backImg);
            return backImg;
    	}
    }
    
    private final static void buildBackHills() {
    	buildHills(bgtm2, 7, 9, 2, false);
    	buildSky(bgtm3);
		buildHills(bgtm3, 10, 12, 4, theme != Theme.Cave); // Farthest
    }
    
    protected final static class TownBackgroundBuilder implements BackgroundBuilder {
    	@Override
    	public final Img getImage() {
    		final Img backImg = ImtilX.loadImage("org/pandcorps/furguardians/res/bg/Town.png", 128, null);
    		applyTerrainTexture(backImg, 112, 96, 128, 128, 0, 1);
        	applyTerrainTexture(backImg, 0, 0, 48, 32, 1, 3);
        	applyColoredTerrain(backImg, 0, 32, 96, 64);
        	applyColoredTerrain(backImg, 112, 96, 16, 32);
            return backImg;
    	}
    	
    	@Override
    	public final int getPreDarken() {
    		return 0;
    	}
    	
    	@Override
    	public final void build() {
    		final int y = 2;
    		bgtm1.fillBackground(bgMap[7][7], 0, y);
    		bgtm1.fillBackground(bgMap[6][7], y, 1);
    		int i = Mathtil.randi(0, 2);
    		while (i < bgtm1.getWidth()) {
    			//i = house(bgtm1, i, y, 1, 1, 1);
    			final int win = Mathtil.randi(1, 3), winLeft = Mathtil.randi(0, win);
    			i = house(bgtm1, i, y, Mathtil.randi(0, 1), winLeft, win - winLeft);
    			i += Mathtil.randi(2, 5);
    		}
    		buildBackHills();
    	}
    }
    
    private final static class ForestBackgroundBuilder implements BackgroundBuilder {
    	@Override
    	public final Img getImage() {
    		final Img backImg = Imtil.load("org/pandcorps/furguardians/res/bg/Forest.png");
    		applyTerrainTexture(backImg, 32, 16, 48, 32, 0, 3, 32);
    		return backImg;
    	}
    	
    	@Override
    	public final int getPreDarken() {
    		return 2;
    	}
    	
    	@Override
    	public final void build() {
    		buildForest(bgtm1, 0, false);
    		buildForest(bgtm2, 1, false);
    		buildForest(bgtm3, 2, true);
    	}
    }
    
    private final static class MountainBackgroundBuilder implements BackgroundBuilder {
    	@Override
    	public final Img getImage() {
    		final Img backImg = ImtilX.loadImage("org/pandcorps/furguardians/res/bg/Mountains.png", 128, null);
    		applyTerrainTexture(backImg, 0, 0, 32, 32);
    		applyTerrainTexture(backImg, 112, 0, 128, 16);
    		applyColoredTerrain(backImg, 0, 0, 64, 96);
    		applyColoredTerrain(backImg, 112, 0, 16, 96);
    		return backImg;
    	}
    	
    	@Override
    	public final int getPreDarken() {
    		return 0;
    	}
    	
    	@Override
    	public final void build() {
    		buildMountains(bgtm1, 6, 0, false);
    		buildMountains(bgtm2, 9, 1, false);
    		buildMountains(bgtm3, 12, 2, true);
    	}
    }
    
    private static void applyTerrainTexture(final Img backImg, final int ix, final int iy, final int fx, final int fy) {
    	applyTerrainTexture(backImg, ix, iy, fx, fy, 0, 3);
    }
    
    private static void applyTerrainTexture(final Img backImg, final int ix, final int iy, final int fx, final int fy, int skip, final int size) {
    	applyTerrainTexture(backImg, ix, iy, fx, fy, skip, size, fy - iy);
    }
    
    private static void applyTerrainTexture(final Img backImg, final int ix, final int iy, final int fx, final int fy, int skip, final int size, final int dist) {
        extractSkyColors(backImg);
    	Img terrain = getTerrainTexture();
    	for (int i = backgroundBuilder.getPreDarken(); i > 0; i--) {
    		terrain = getDarkenedTerrain(terrain);
    	}
        for (int z = 0; z < size; z++) {
            if (z > 0) {
                terrain = getDarkenedTerrain(terrain);
            }
            final int yoff = z * dist;
            if (skip <= 0) {
            	applyTerrainTexture(backImg, ix, iy + yoff, fx, fy + yoff, terrain, getTerrainMask(z));
            } else {
            	skip--;
            }
        }
        terrain.closeIfTemporary();
    }
    
    private static void ground(final int px, final int stop) {
    	for (int i = px; i <= stop; i++) {
            tm.setForeground(i, floor, imgMap[1][1], Tile.BEHAVIOR_SOLID);
            for (int j = 0; j < floor; j++) {
            	tm.setForeground(i, j, getDirtImage(), Tile.BEHAVIOR_SOLID);
            }
        }
    }
    
    private final static int DEFAULT_ENEMY_PROBABILITY = 40;
    
    private static int enemyProbability = DEFAULT_ENEMY_PROBABILITY;
    
    private final static void enemy(final int x, final int y, final int w) {
    	if (theme == Theme.Minecart) {
    		return;
    	} else if (w < 3 || (numEnemies > 0 && Mathtil.rand(enemyProbability))) {
    		if (enemyProbability > 0) {
    			enemyProbability -= 5;
    		}
    		return;
    	} else if (y >= (tm.getHeight() - 1)) { // Player needs two tiles
    	    return; // Don't drop Enemy with only one tile so Player can't reach
    	}
    	final int i = x + Mathtil.randi(1, w - 2);
    	new Spawner(tm.getTileWidth() * i, tm.getTileHeight() * fixEnemyIndexY(i, y));
    	numEnemies++;
    	enemyProbability = DEFAULT_ENEMY_PROBABILITY;
    }
    
    private final static void enemy(final EnemyDefinition def, final int x, final int y) {
    	new SpecificSpawner(def, tm.getTileWidth() * x, tm.getTileHeight() * fixEnemyIndexY(x, y));
    	numEnemies++;
    }
    
    private final static int fixEnemyIndexY(final int indexX, final int indexY) {
        if (indexY == floor + 1) {
            return getFloorIndexForIndex(indexX);
        }
        return indexY;
    }
    
    private final static void bee(final int x, final int y) {
        FurGuardiansGame.setPosition(new Bouncer(),
            (tm.getTileWidth() * x) + FurGuardiansGame.BIRD_X + 1, (tm.getTileHeight() * y) + FurGuardiansGame.BIRD_Y, FurGuardiansGame.DEPTH_BACK);
    }
    
    private final static int[] scratch = new int[128];
    
    private final static void swapScratch(final int i, final int j) {
    	Coltil.swap(scratch, i, j);
    }
    
    private static class GrassyBuilder extends RandomBuilder {
    	@Override
	    protected void loadTemplates() {
	        addTemplate(new NaturalRiseTemplate());
	        addTemplate(new ColorRiseTemplate());
	        addTemplate(new WallTemplate());
	        addTemplate(new StepTemplate());
	        addTemplate(new RampTemplate());
	        final Theme theme = getDayTheme();
	        if (theme == Theme.Snow || theme == Theme.Sand) {
	        	addTemplate(new SpecialGroundTemplate());
	        } else if (theme == Theme.Rock) {
	            addTemplate(new BushTemplate());
	        } else {
	        	addTemplate(new BushTemplate(), new TreeTemplate());
	        }
	        addTemplate(new AnyPitTemplate());
	        addTemplate(new UpBlockStepTemplate(), new DownBlockStepTemplate(), new BlockWallTemplate(), new BlockGroupTemplate());
	        addFloatTemplates();
	        addTemplate(new SlantTemplate(true), new SlantTemplate(false));
	        addGiantTemplate();
	        addNormalGoals();
	    }
    	
    	@Override
    	protected final void ground(final int start, final int stop) {
    		Level.ground(start, stop);
    	}
    	
    	@Override
    	protected final void upStep(final int x, final int y, final int h) {
    		Level.upStep(x, y, h);
    	}
    	
    	@Override
    	protected final void downStep(final int x, final int y, final int h) {
    		Level.downStep(x, y, h);
    	}
    }
    
    private static class FlatBuilder extends GrassyBuilder {
    	@Override
	    protected final void loadTemplates() {
    		//TODO Multi-level block patterns
	        addTemplate(new WallTemplate());
	        addNatureTemplate();
	        addTemplate(new AnyPitTemplate());
	        addTemplate(new UpBlockStepTemplate(), new DownBlockStepTemplate(), new BlockWallTemplate(), new BlockGroupTemplate());
	        addTemplate(new BlockBonusTemplate());
	        addTemplate(new GemTemplate());
	        addTemplate(new GemMsgTemplate());
	        addTemplate(new SlantTemplate(true), new SlantTemplate(false));
	        addGiantTemplate();
	        addNormalGoals();
	    }
    	
    	protected void addNatureTemplate() {
    		addTemplate(new BushTemplate(), new TreeTemplate());
    	}
    	
    	@Override
    	public final int getFloor() {
    		return 3;
    	}
    	
    	@Override
    	protected final int getMaxFloorChange() {
    		return (floor) > 1 ? 2 : 1;
    	}
    	
    	@Override
    	protected final int getMaxFloor() {
    		return 1;
    	}
    	
    	@Override
    	protected final boolean changeFloor() {
    		return (floor > 1) || super.changeFloor();
    	}
    }
    
    protected final static int MAX_CAVE_CEILING_SIZE = 5;
    
    private final static class CaveBuilder extends FlatBuilder {
    	@Override
    	protected final void addNatureTemplate() {
    		addTemplate(new BushTemplate());
    		addTemplate(new SpikeTemplate());
    	}
    	
    	@Override
    	protected final void buildCeiling() {
    		final int w = tm.getWidth(), h = tm.getHeight(), max = h - 1;
    		int min = h - 3, j = max;
    		for (int i = 0; i < w; i++) {
    			if (Mathtil.rand(20)) {
    				if (i > 10) {
    					min = h - MAX_CAVE_CEILING_SIZE;
    				}
    				final boolean up;
    				if (j >= max) {
    					up = false;
    				} else if (j <= min) {
    					up = true;
    				} else {
    					up = Mathtil.rand();
    				}
    				if (up) {
    					tm.setForeground(i, j, imgMap[6][2], Tile.BEHAVIOR_SOLID);
    					j++;
    					tm.setForeground(i, j, imgMap[5][2], Tile.BEHAVIOR_SOLID);
    					fillCeiling(i, j, max);
    				} else {
    					tm.setForeground(i, j, imgMap[5][0], Tile.BEHAVIOR_SOLID);
    					fillCeiling(i, j, max);
    					j--;
    					tm.setForeground(i, j, imgMap[6][0], Tile.BEHAVIOR_SOLID);
    				}
    				i++;
    				if (i >= w) {
    					break;
    				}
    			}
    			tm.setForeground(i, j, imgMap[6][1], Tile.BEHAVIOR_SOLID);
    			fillCeiling(i, j, max);
    			if (Mathtil.rand(15) && tm.getTile(i, j - 1) == null) {
    				tm.setBackground(i, j - 1, imgMap[7][2], FurGuardiansGame.TILE_HURT);
    			}
    		}
    	}
    	
    	private final void fillCeiling(final int i, final int j, final int max) {
			for (int j2 = j + 1; j2 <= max; j2++) {
				tm.setForeground(i, j2, getDirtImage(), Tile.BEHAVIOR_SOLID);
			}
		}
    }
    
    private static class PlatformBuilder extends RandomBuilder {
    	@Override
	    protected void loadTemplates() {
    		floorMode = getFloorMode();
	        addTemplate(new WallTemplate());
	        addTemplate(new PitTemplate());
	        addTemplate(new BridgePitTemplate());
	        addTemplate(new BlockPitTemplate());
	        addTemplate(new UpBlockStepTemplate(), new DownBlockStepTemplate(), new BlockWallTemplate(), new BlockGroupTemplate());
	        addTemplate(new BlockBonusTemplate());
	        addTemplate(new GemTemplate());
	        addTemplate(new GemMsgTemplate());
	        addGiantTemplate();
	        goals.add(new UpBlockGoal());
	    }
    	
    	protected byte getFloorMode() {
    	    return FLOOR_BLOCK;
    	}
    	
    	@Override
    	protected void ground(final int start, final int stop) {
    		Level.blockWall(start, floor, stop - start + 1, 1);
    	}
    	
    	@Override
    	protected final void upStep(final int x, final int y, final int h) {
    	}
    	
    	@Override
    	protected final void downStep(final int x, final int y, final int h) {
    	}
    }
    
    private static class BridgeBuilder extends PlatformBuilder {
        @Override
        protected byte getFloorMode() {
            return FLOOR_BRIDGE;
        }
        
        @Override
        protected final void ground(int start, final int stop) {
        	boolean first = true;
            for (int i = start; i <= stop; i++) {
                final int imgCol;
                if (i == start) {
                    if (i == 0) {
                        imgCol = 1;
                    } else if (DynamicTileMap.getRawBackground(tm.getTile(i - 1, floor)) == imgMap[2][0]) {
                        imgCol = 1;
                        column(i - 1, imgCol);
                    } else if (first && isGapNeeded()) {
                    	start++;
                    	first = false;
                    	continue;
                    } else {
                        imgCol = 0;
                    }
                } else if (i == stop) {
                    imgCol = (i == nt - 1) ? 1 : 0;
                } else {
                    imgCol = 1;
                }
                column(i, imgCol);
                first = false;
            }
        }
        
        protected final void column(final int i, final int imgCol) {
        	tm.setBackground(i, floor + 1, imgMap[1][imgCol], Tile.BEHAVIOR_OPEN);
            tm.setBackground(i, floor, imgMap[2][imgCol], Tile.BEHAVIOR_SOLID);
            if (floor > 0) {
            	fill(i, imgCol);
            }
        }
        
        //@OverrideMe
        protected void fill(final int i, final int imgCol) {
        }
        
        //@OverrideMe
        protected boolean isGapNeeded() {
        	return false;
        }
    }
    
    private final static class HexBuilder extends RandomBuilder {
        @Override
        protected final void loadTemplates() {
            addTemplate(getPitTemplate());
            addFloatTemplates();
            addGiantTemplate();
            addTemplate(new BeeTemplate());
            goals.add(new BeeGoal());
            goals.add(new PlatformGoal());
        }
        
        @Override
        protected final Template getPitTemplate() {
            return new ChoiceTemplate(new PitTemplate(), new BridgePitTemplate(), new BeePitTemplate());
        }
        
        @Override
        protected final boolean changeFloor() {
            if (!super.changeFloor()) {
                return false;
            }
            final int m = px % 4;
            if (m == 0) {
                return true;
            }
            bx = px + (4 - m) - 1;
            ground();
            return true;
        }
        
        @Override
        protected final int getFloorChangeWidth() {
            return 5;
        }
        
        @Override
        public final int getFloatOffset() {
            return 1;
        }
        
        @Override
        protected final void ground(int start, final int stop) {
            final int m = start % 4;
            if (m != 0) {
                start = start + (4 - m);
                if (start > stop) {
                    return;
                }
            }
            final int f = floor + 1;
            for (int i = start - 1; i <= stop; i += 4) {
                for (int j = 0; j <= f; j += 2) {
                    hexagon(i, j); // (i, j - 1) to (i + 2, j)
                }
                final int i2 = i + 2;
                for (int j = 1; j <= f; j += 2) {
                    hexagon(i2, j);
                }
            }
        }

        @Override
        protected final void upStep(final int x, final int y, final int h) {
        }

        @Override
        protected final void downStep(final int x, final int y, final int h) {
        }
        
        @Override
        protected final int getMaxFloorChange() {
            return 1;
        }
        
        @Override
        protected final void flatten(final int x, final int w) {
            fillHexagonGaps(x, w);
        }
    }
    
    private final static class MinecartBuilder extends BridgeBuilder {
    	@Override
	    protected final void loadTemplates() {
    		floorMode = getFloorMode();
	        addTemplate(getPitTemplate());
	        addTemplate(new GemTemplate());
	        addTemplate(new GemMsgTemplate());
	        goals.add(new FlatGoal());
	    }
    	
    	@Override
    	protected final Template getPitTemplate() {
        	return new PitTemplate();
        }
    	
        @Override
        protected final byte getFloorMode() {
            return FLOOR_TRACK;
        }
        
        @Override
        protected final int getFloorChangeWidth() {
    		return 4;
    	}
        
        @Override
        public final int getFloatOffset() {
            return 1;
        }
        
        @Override
        protected final void fill(final int i, final int imgCol) {
        	for (int j = floor - 1; j >= 0; j--) {
        		tm.setBackground(i, j, imgMap[3][imgCol], Tile.BEHAVIOR_OPEN);
        	}
        }
        
        @Override
        protected final boolean isGapNeeded() {
        	return true;
        }
    }
    
    private abstract static class GoalTemplate {
    	protected abstract int getWidth();
    	
    	protected abstract void build();
    }
    
    private final static class SlantGoal extends GoalTemplate {
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
            solidBlock(x - 2, floor + 1);
    	}
    }
    
    private final static class UpBlockGoal extends GoalTemplate {
    	@Override
    	protected final int getWidth() {
    		return 5;
    	}
    	
    	@Override
    	protected final void build() {
    		upBlockStep(ng, floor + 1, 3, Mathtil.rand());
            goalBlock(ng + 3, floor + 7);
    	}
    }
    
    private final static class FlatGoal extends GoalTemplate {
    	@Override
    	protected final int getWidth() {
    		return 3;
    	}
    	
    	@Override
    	protected final void build() {
            goalBlock(ng + 1, floor + 4);
    	}
    }
    
    private static class RiseGoal extends GoalTemplate {
        @Override
        protected final int getWidth() {
            return 6; // 2 for 1st step + 1 for 2nd + 1 for 3rd + 1 for goal + 1 for gap
        }
        
        @Override
        protected final void build() {
            init();
            for (int i = 2; i >= 0; i--) {
                rise(ng + i, floor + 1, 0, i);
            }
            goalBlock(ng + 4, floor + 7);
        }
        
        protected void init() {
        }
        
        protected void rise(final int x, final int y, final int w, final int h) {
            naturalRise(x, y, w, h);
        }
    }
    
    private final static class ColorRiseGoal extends RiseGoal {
        @Override
        protected final void init() {
            ColorRiseTemplate.initStatic();
        }
        
        @Override
        protected final void rise(final int x, final int y, final int w, final int h) {
            ColorRiseTemplate.riseStatic(x, y, w, h);
        }
    }
    
    private final static class BeeGoal extends GoalTemplate {
        private int w;
        
        @Override
        protected int getWidth() {
            w = Mathtil.randi(3, 4); // 1 for jump room + ((1 for bee + 1 for goal) or 1 for both) + 1 for gap
            return w;
        }

        @Override
        protected void build() {
            final int x = ng + 1, y = floor + Mathtil.randi(3, 4);
            bee(x, y);
            goalBlock(x + w - 3, y + 4);
        }
    }
    
    private final static class PlatformGoal extends GoalTemplate {
        @Override
        protected int getWidth() {
            return 6; // 2 for jump room + 3 for platform + 1 for gap
        }
        
        @Override
        protected void build() {
            final int x = ng + 2, y = floor + 3 + floatOffset;
            for (int i = 0; i < 3; i++) {
                solidBlock(x + i, y);
            }
            goalBlock(x + 2, y + 3);
        }
    }
    
    private abstract static class Template {
    	protected abstract void plan();
    	
        protected abstract void build();
        
        @Override
        public final boolean equals(final Object o) {
            return (this == o) || ((o != null) && (getClass() == o.getClass()));
        }
        
        @Override
        public final int hashCode() {
            return getClass().hashCode();
        }
    }
    
    private static class ChoiceTemplate extends Template {
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
                scratch[i] = ((i == 0) ? -1 : scratch[i - 1]) + Mathtil.randi(2, 3);
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
    	    initStatic();
    	}
    	
    	private final static void initStatic() {
    		for (int i = 0; i < 3; i++) {
    			colors[i] = i;
    		}
    		colorIndex = 0;
    		Mathtil.shuffle(colors);
    	}
    	
        @Override
        protected final void rise(final int x, final int y, final int w, final int h) {
            riseStatic(x, y, w, h);
        }
        
        private final static void riseStatic(final int x, final int y, final int w, final int h) {
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
        	w = newWidth(minW, maxW);
        	x = bx;
        	bx += (w + ext);
        }
        
        protected int newWidth(final int minW, final int maxW) {
            return Mathtil.randi(minW, maxW);
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
    
    private final static class AnyPitTemplate extends ChoiceTemplate {
    	private AnyPitTemplate() {
    		super(new PitTemplate(), new BridgePitTemplate(), new BlockPitTemplate());
    	}
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
    
    private final static class BeePitTemplate extends SimpleTemplate {
        protected BeePitTemplate() {
            super(3, 9);
        }
        
        @Override
        protected final void build() {
            pit(x, floor, w);
            final int xb = x + 1, yb = floor + 3;
            for (int i = 1; i < w; i += 3) {
                bee(xb + i, yb);
            }
        }
        
        @Override
        protected final int newWidth(final int minW, final int maxW) {
            int b = super.newWidth(minW, maxW);
            final int m = b % 3;
            if (m != 0) {
                b += (3 - m);
            }
            return b;
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
    		this(8);
    	}
    	
    	protected BlockBonusTemplate(final int maxW) {
    		super(1, maxW, 0);
    	}
    	
        @Override
        protected final void build() {
        	final int stop = x + w;
        	final boolean flag = Mathtil.rand();
        	final int y = floor + 3 + floatOffset;
        	for (int i = x; i < stop; i++) {
        		if (flag) {
        			bumpableBlock(i, y);
        		} else {
        			breakableBlock(i, y);
        		}
        	}
        	enemy(x, y + 1, w);
        }
    }
    
    private final static class BlockLetterTemplate extends SimpleTemplate {
    	protected BlockLetterTemplate() {
    		super(1, 1, 0);
    	}
    	
        @Override
        protected final void build() {
        	letterBlock(x, floor + 3 + floatOffset);
        }
    }
    
    private final static class GemTemplate extends SimpleTemplate {
    	protected GemTemplate() {
    		this(10);
    	}
    	
    	protected GemTemplate(final int maxW) {
    		super(1, maxW, 0);
    	}
    	
    	@Override
		protected final void build() {
    		final int stop = x + w;
    		final boolean block = (theme != Theme.Minecart) && Mathtil.rand();
    		final int y = floor + 3 + floatOffset;
    		for (int i = x; i < stop; i++) {
    			if (block) {
    				solidBlock(i, y);
    				gem(i, y + 1);
    			} else {
    				gem(i, y);
    			}
    		}
    		enemy(x, floor + 1, w);
    	}
    }
    
    private final static class GemMsgTemplate extends Template {
    	private int x;
    	private String msg;
    	
		@Override
		protected final void plan() {
			x = bx;
			if (getDefeatedLevels() == 0) {
			    msg = FurGuardiansGame.pcs.get(0).getBonusName();
			} else {
    			msg = Mathtil.rand(theme.gemMessages);
    			if ("PLAYER".equals(msg)) {
    				msg = Mathtil.rand(FurGuardiansGame.pcs).getBonusName();
    			}
			}
			bx += gemMsg(x, floor + 1 + floatOffset, msg, false) + 2;
		}

		@Override
		protected final void build() {
			gemMsg(x + 1, floor + 1 + floatOffset, msg, true);
		}
    }
    
    private final static class SpecialGroundTemplate extends SimpleTemplate {
    	protected SpecialGroundTemplate() {
    		super(4, 10);
    	}
    	
    	@Override
        protected final void build() {
    		final int stop = x + w + 1, y;
    		final boolean sunken = floor >= 2;
    		if (sunken) {
    			y = floor - 1;
    			for (int i = 0; i <= 2; i++) {
	    			tm.setForeground(x, floor - i, imgMap[1 + i][2], Tile.BEHAVIOR_SOLID);
	    			tm.setForeground(stop, floor - i, imgMap[1 + i][0], Tile.BEHAVIOR_SOLID);
    			}
    		} else {
	    		for (int i = 1; i <= 2; i++) {
	    			solidBlock(x, floor + i);
	    			solidBlock(stop, floor + i);
	    		}
	    		y = floor + 1;
    		}
    		final byte behavior = theme.getSpecialGroundBehavior();
    		for (int i = x + 1; i < stop; i++) {
    			if (sunken) {
    				tm.removeTile(i, floor);
    				tm.setForeground(i, y - 1, imgMap[1][1], Tile.BEHAVIOR_SOLID);
    			}
    			tm.setForeground(i, y, imgMap[1][5], behavior);
    		}
    	}
    }
    
    private final static class BeeTemplate extends SimpleTemplate {
        protected BeeTemplate() {
            super(1, 1, 0);
        }
        
        @Override
        protected void build() {
            final int base = floor + floatOffset;
            bee(x, base + 3);
            bonus(x, base + 7);
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
    
    private final static class SpikeTemplate extends SimpleTemplate {
    	protected SpikeTemplate() {
    		super(1, 2, 0);
    	}
    	
    	@Override
        protected final void build() {
    		final int y = floor + 1, stop = x + w;
    		final Tile tile = tm.getTile(imgMap[7][1], null, FurGuardiansGame.TILE_HURT);
    		for (int i = x; i < stop; i++) {
    			tm.setTile(i, y, tile);
    		}
    	}
    }
    
    private final static class GiantTemplate extends SimpleTemplate {
    	protected GiantTemplate() {
    		super(12, 12, 0);
    	}
    	
    	@Override
        protected final void build() {
    	    builder.flatten(x, w);
    	    final int base = floor + 1 + floatOffset;
    		blockWall(x, base, 1, 2);
    		blockWall(x + w - 1, base, 1, 2);
    		enemy(Mathtil.rand() ? FurGuardiansGame.trollColossus : FurGuardiansGame.ogreBehemoth, x + 5, base);
    	}
    }
    
    private final static void extractSkyColors(final Img img) {
    	if (topSkyColor == null) {
    		reextractSkyColors(img);
    	}
    }
    
    private final static void reextractSkyColors(final Img img) {
        topSkyColor = theme.getTopSkyColor(img);
        bottomSkyColor = theme.getBottomSkyColor(img);
    }
    
    private final static void buildSky(final TileMap tm, final int base, final int mid) {
    	final Pangine engine = Pangine.getEngine();
    	if (theme == Theme.Cave) {
    		engine.setBgColor(bottomSkyColor);
    		return;
    	}
        final int topHeight = tm.getHeight() - (mid + 1), bottomHeight = mid - base;
        if (topHeight < bottomHeight) {
            tm.fillBackground(bgMap[0][6], mid + 1, topHeight);
            engine.setBgColor(bottomSkyColor);
        } else {
            tm.fillBackground(bgMap[2][6], base, bottomHeight);
            engine.setBgColor(topSkyColor);
        }
        tm.fillBackground(bgMap[1][6], mid, 1);
    }
    
    private final static void buildSky(final TileMap tm) {
        buildSky(tm, 0, 8);
    }
    
    private final static void buildHills(final TileMap tm, final int miny, final int maxy, final int iy, final boolean cloud) {
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
    
    private final static void buildForest(final TileMap tm, final int off, final boolean sky) {
    	final int iy = off * 2;
    	tm.fillBackground(bgMap[iy + 1][2], 0, off + 1);
    	tm.fillBackground(bgMap[iy][2], off + 1, 1);
    	final int tmw = tm.getWidth(), tmh = tm.getHeight();
    	if (sky) {
    	    buildSky(tm, off + 2, tmh - 5);
    	}
    	int i = Mathtil.randi(-1, 2);
    	while (i < tmw) {
    		if (i >= 0) {
    			tm.fillBackground(bgMap[iy + 1][0], i, off + 2, 1, tmh - (off * 2) - 3);
    			tm.setBackground(0, iy, i, tmh - off - 2);
    		}
    		if (i < (tmw - 1)) {
    			tm.fillBackground(bgMap[iy + 1][1], i + 1, off + 2, 1, tmh - (off * 2) - 3);
    			tm.setBackground(1, iy, i + 1, tmh - off - 2);
    		}
    		i += Mathtil.randi(3, 6);
    	}
    }
    
    private final static void buildMountains(final TileMap tm, final int maxy, final int v, final boolean sky) {
    	final int miny = maxy - 3;
    	final int tmw = tm.getWidth();
    	if (sky) {
    		buildSky(tm);
    	}
    	int i = Mathtil.randi(-2, 2);
    	do {
    		final int y = Mathtil.randi(miny, maxy);
    		mountain(tm, i, y, v, Mathtil.rand());
    		i = i + y * 2 + Mathtil.randi(2, 4);
    	} while (i < tmw);
    }
    
    private final static void addPlayers() {
    	final int size = FurGuardiansGame.pcs.size();
        final ArrayList<Player> players = new ArrayList<Player>(size);
        final int th = tm.getTileHeight();
        for (int i = 0; i < size; i++) {
        	final PlayerContext pc = FurGuardiansGame.pcs.get(i);
        	Goal.initGoals(pc);
        	final int x = 40 + (20 * i);
        	players.add(new Player(pc, x, getFloorIndexForPosition(x) * th));
        }
        Pangine.getEngine().track(Panverage.getArithmeticMean(players));
    }
    
    private final static int getFloorIndexForPosition(final int x) {
        return getFloorIndexForIndex(x / 16);
    }
    
    private final static int getFloorIndexForIndex(final int i) {
        int base = floor + 1;
        if (builder instanceof HexBuilder) {
            if (getHexagonFloorType(i) != HEX_DOWN) {
                base++;
            }
        }
        return base;
    }
    
    private final static byte getHexagonFloorType(final int i) {
        final int base = floor + 1;
        final int im = i % 4, ib = base % 2;
        if (im == 0) { // Always odd number of tiles, bottom is half hexagon
            if (ib == 0) {
                return HEX_UP;
            }
        } else if (im == 2) { // Always even, bottom is full hexagon
            if (ib == 1) {
                return HEX_UP;
            }
        } else if (im == 1) { // Diagonals always go up from base floor
            return (ib == 0) ? HEX_FALL : HEX_RISE;
        } else { // im == 3
            return (ib == 1) ? HEX_FALL : HEX_RISE;
        }
        return HEX_DOWN;
    }
    
    private final static void setBg(final TileMap tm, final int i, final int j, final TileMapImage[][] imgMap, final int iy, final int ix) {
    	if (tm.isBad(i, j)) {
    		return;
    	}
        tm.setTile(i, j, imgMap[iy][ix], null, Tile.BEHAVIOR_OPEN);
    }
    
    private final static void setFg(final TileMap tm, final int i, final int j, final TileMapImage[][] imgMap, final int iy, final int ix) {
    	if (tm.isBad(i, j)) {
    		return;
    	}
    	tm.setForeground(i, j, imgMap[iy][ix]);
    }
    
    private final static int house(final TileMap tm, final int x, final int y, final int border, final int winLeft, final int winRight) {
    	final int sectionSize = 3, winLeftMult = winLeft * sectionSize, winRightMult = winRight * sectionSize;
    	final int last = x + 5 + border + winLeftMult + sectionSize + winRightMult + border;
    	if (last >= tm.getWidth()) {
    		return last;
    	}
    	tm.rectangleBackground(4, 7, x + 1, y, 3, 2);
    	tm.rectangleBackground(1, 1, x + 1, y + 2, 3, 1);
    	tm.rectangleBackground(4, 7, x + 1, y + 3, 3, 1);
    	tm.rectangleBackground(0, 0, x, y + 4, 2, 1);
    	setBg(tm, x + 2, y + 4, bgMap, 6, 5);
    	tm.rectangleForeground(3, 0, x + 3, y + 4, 2, 1);
    	setBg(tm, x + 1, y + 5, bgMap, 5, 6);
    	tm.rectangleBackground(5, 1, x + 2, y + 5, 1, 2);
    	setFg(tm, x + 3, y + 5, bgMap, 5, 7);
    	int xb = x + 4;
    	for (int i = 0; i < border; i++) {
    		houseMid(tm, xb + i, y);
    	}
    	for (int i = 0; i < winLeft; i++) {
    		houseSection(tm, xb + border + (i * sectionSize), y, false);
    	}
    	houseSection(tm, xb + border + winLeftMult, y, true);
    	xb += sectionSize;
    	for (int i = 0; i < winRight; i++) {
    		houseSection(tm, xb + border + winLeftMult + (i * sectionSize), y, false);
    	}
    	for (int i = 0; i < border; i++) {
    		houseMid(tm, xb + border + winLeftMult + winRightMult + i, y);
    	}
    	houseRight(tm, xb + border + winLeftMult + winRightMult + border, y);
    	tm.fillBackground(bgMap[1][4], x + 4, y + 4, 1 + border + winLeftMult + sectionSize + winRightMult + border, 1);
    	setBg(tm, last, y + 4, bgMap, 0, 4);
    	tm.fillBackground(bgMap[1][0], x + 3, y + 5, 1 + border + winLeftMult + sectionSize + winRightMult + border, 1);
    	setBg(tm, x + 4 + border + winLeftMult + sectionSize + winRightMult + border, y + 5, bgMap, 5, 7);
    	tm.fillBackground(bgMap[4][6], x + 3, y + 6, border + winLeftMult + sectionSize + winRightMult + border, 1);
    	setBg(tm, x + 3 + border + winLeftMult + sectionSize + winRightMult + border, y + 6, bgMap, 4, 7);
    	tm.fillBackground(bgMap[6][7], x + 1, y - 1, 4 + border + winLeftMult + sectionSize + winRightMult + border, 1);
    	return last;
    }
    
    private final static void houseSection(final TileMap tm, final int x, final int y, final boolean door) {
    	houseRight(tm, x, y);
    	houseMid(tm, x + 1, y);
    	if (door) {
    		tm.rectangleBackground(3, 7, x + 1, y, 1, 2);
    	} else {
    		setBg(tm, x + 1, y + 2, bgMap, 0, 2); // Window
    	}
    	houseLeft(tm, x + 2, y);
    }
    
    private final static void houseLeft(final TileMap tm, final int x, final int y) {
    	houseCol(tm, x, y, -1);
    }
    
    private final static void houseMid(final TileMap tm, final int x, final int y) {
    	houseCol(tm, x, y, 0);
    }
    
    private final static void houseRight(final TileMap tm, final int x, final int y) {
    	houseCol(tm, x, y, 1);
    }
    
    private final static void houseCol(final TileMap tm, final int x, final int y, final int off) {
    	tm.rectangleBackground(5 + off, 7, x, y, 1, 2);
		setBg(tm, x, y + 2, bgMap, 1, 2 + off);
		setBg(tm, x, y + 3, bgMap, 7, 5 + off);
    }
    
    private final static void hill(final TileMap tm, final int x, final int y, final int w, final int ix, final int iy) {
    	if (ix > 0 && theme == Theme.Cave) {
    		hillFlipped(tm, x, y, w, iy);
    		return;
    	}
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
    
    private final static void hillFlipped(final TileMap tm, final int x, final int y, final int w, final int _iy) {
    	final int ixBase = 0, ixTip = 3;
    	final int iyBase = _iy + 1, iyTip = _iy / 2;
    	final int top = tm.getHeight() - 1;
    	for (int j = 0; j < y; j++) {
            setBg(tm, x, top - j, bgMap, iyBase, ixBase);
            setBg(tm, x + w + 1, top - j, bgMap, iyBase, ixBase + 2);
        }
        final int stop = x + w;
        for (int i = x + 1; i <= stop; i++) {
            setBg(tm, i, top - y, bgMap, iyTip, ixTip + 1);
            for (int j = 0; j < y; j++) {
                setBg(tm, i, top - j, bgMap, iyBase, ixBase + 1);
            }
        }
        setFg(tm, x, top - y, bgMap, iyTip, ixTip);
        setFg(tm, stop + 1, top - y, bgMap, iyTip, ixTip + 2);
    }
    
    private final static void mountain(final TileMap tm, final int x, final int y, final int v, final boolean tex) {
    	final int ix = tex ? 0 : 2, ix1 = ix + 1, iy = v * 2, iy1 = iy + 1;
    	final int m = (tex ? 0 : 3) + v, mx = 7;
    	final int is = 4, is1 = 5, ms = 3 + v, mxs = 6;
    	final int ys = y * 3 / 4;
    	for (int j = 0; j < y; j++) {
    		final int xj = x + j, xy2j = x + y * 2 - j - 1;
    		final int ixc, ixc1;
    		final int mc, mxc;
    		if (j < ys) {
	    		ixc = ix;
	    		ixc1 = ix1;
	    		mc = m;
	    		mxc = mx;
    		} else {
    			ixc = is;
	    		ixc1 = is1;
	    		mc = ms;
	    		mxc = mxs;
    		}
    		setFg(tm, xj, j, bgMap, iy, ixc);
    		if (j < y - 1) {
    			final int xy2j1 = xy2j - 1;
    			setBg(tm, xj + 1, j, bgMap, iy1, ixc);
    			for (int i = xj + 2; i < xy2j1; i++) {
    				setBg(tm, i, j, bgMap, mc, mxc);
    			}
    			setBg(tm, xy2j1, j, bgMap, iy1, ixc1);
    		}
    		setFg(tm, xy2j, j, bgMap, iy, ixc1);
    		if (j == ys - 1) {
    			for (int i = xj + 1; i < xy2j; i += 2) {
    				setFg(tm, i, j, bgMap, 7, v * 2);
    				setFg(tm, i + 1, j, bgMap, 7, v * 2 + 1);
    			}
    		} else if (j == ys) {
    			for (int i = xj + 1; i < xy2j; i += 2) {
    				setFg(tm, i, j, bgMap, 6, v * 2 + 1);
   					setFg(tm, i + 1, j, bgMap, 6, v * 2);
    			}
    		}
    	}
    }
    
    private final static void cloud(final TileMap tm, final int x, final int y, final int w) {
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
    
    private final static void addShadow(final int x, final int y) {
        if (y == 0 || floorMode != FLOOR_BRIDGE) {
            return;
        }
        final int y1 = y - 1;
        final Object bg = DynamicTileMap.getRawBackground(tm.getTile(x, y1));
        for (int col = 0; col < 2; col++) {
            if (bg == imgMap[2][col]) {
                tm.setBackground(x, y1, imgMap[3][col]);
                break;
            }
        }
    }
    
    private final static void setFgShadowed(final int x, final int y, final int iy, final int ix, final byte behavior) {
        tm.setForeground(x, y, imgMap[iy][ix], behavior);
        addShadow(x, y);
    }
    
    private final static void solidBlock(final int x, final int y) {
        setFgShadowed(x, y, 0, 4, Tile.BEHAVIOR_SOLID);
    }
    
    private final static int DEFAULT_POWER_PROBABILITY = 40;
    
    private static int powerProbability = DEFAULT_POWER_PROBABILITY;
    
    private final static boolean powerBlock(final int x, final int y) {
        if (numPower == 0) {
            if (Mathtil.rand(powerProbability)) {
                numPower++;
                tm.setForeground(x, y, FurGuardiansGame.blockPower, FurGuardiansGame.TILE_BUMP);
                return true;
            } else if (powerProbability < 100) {
                powerProbability += 15;
            }
        }
        return false;
    }
    
    private final static void bumpableBlock(final int x, final int y) {
        if (!powerBlock(x, y)) {
            tm.setForeground(x, y, imgMap[0][0], FurGuardiansGame.TILE_BUMP);
        }
    }
    
    private final static void breakableBlock(final int x, final int y) {
        if (!powerBlock(x, y)) {
            tm.setForeground(x, y, imgMap[0][5], FurGuardiansGame.TILE_BREAK);
            numBreakable++;
        }
    }
    
    private final static void letterBlock(final int x, final int y) {
        tm.setForeground(x, y, FurGuardiansGame.getBlockWordLetter(currLetter++), FurGuardiansGame.TILE_BUMP);
    }
    
    private final static void upBlock(final int x, final int y) {
        setFgShadowed(x, y, 0, 6, FurGuardiansGame.TILE_UPSLOPE);
    }
    
    private final static void downBlock(final int x, final int y) {
        setFgShadowed(x, y, 0, 7, FurGuardiansGame.TILE_DOWNSLOPE);
    }
    
    private final static void goalBlock(final int x, final int y) {
        goalIndex = tm.getIndex(x, y);
        tm.setForeground(goalIndex, imgMap[7][0], FurGuardiansGame.TILE_BUMP);
    }
    
    private final static void step(final int x, final int y, final int w, final int h) {
    	step(x, y, w, h, 1);
    }
    
    private final static void upStep(final int x, final int y, final int h) {
    	step(x, y, 0, h, 0);
    }
    
    private final static void downStep(final int x, final int y, final int h) {
    	step(x, y, -1, h, 2);
    }
    
    private final static void step(final int x, final int y, final int w, final int h, final int mode) {
        // Will also want 1-way steps going up and 1-way down; same with ramps
    	if (mode != 2) {
    		tm.setForeground(x, y, imgMap[3][0], Tile.BEHAVIOR_SOLID);
    	}
        final int stop = x + w + 1, ystop = y + h + 1;
        for (int j = y + 1; j < ystop; j++) {
        	if (mode != 2) {
        		tm.setForeground(x, j, imgMap[2][0], Tile.BEHAVIOR_SOLID);
        	}
        	if (mode != 0) {
        		tm.setForeground(stop, j, imgMap[2][2], Tile.BEHAVIOR_SOLID);
        	}
        	if (mode == 1) {
	            for (int i = x + 1; i < stop; i++) {
	                tm.setForeground(i, j, getDirtImage(), Tile.BEHAVIOR_SOLID);
	            }
        	}
        }
        if (mode != 2) {
        	tm.setForeground(x, ystop, imgMap[1][0], Tile.BEHAVIOR_SOLID);
        }
        if (mode == 1) {
	        for (int i = x + 1; i < stop; i++) {
	            tm.setForeground(i, ystop, imgMap[1][1], Tile.BEHAVIOR_SOLID);
	            tm.setForeground(i, y, getDirtImage(), Tile.BEHAVIOR_SOLID);
	        }
        }
        if (mode != 0) {
	        tm.setForeground(stop, ystop, imgMap[1][2], Tile.BEHAVIOR_SOLID);
	        tm.setForeground(stop, y, imgMap[3][2], Tile.BEHAVIOR_SOLID);
        }
    }
    
    private final static void ramp(final int x, final int y, final int w, final int h) {
        final int fstop = x + w + h * 2, ystop = y + h;
        for (int jo = y; jo <= ystop; jo++) {
            final int jb = jo - y, stop = fstop - jb;
            if (jb != 0) {
                tm.setForeground(x + jb, jo, imgMap[3][3], FurGuardiansGame.TILE_UPSLOPE);
                tm.setForeground(stop + 1, jo, imgMap[3][4], FurGuardiansGame.TILE_DOWNSLOPE);
            }
            if (jo == ystop) {
                for (int i = x + jb + 1; i <= stop; i++) {
                    tm.setForeground(i, jo, imgMap[1][1], Tile.BEHAVIOR_SOLID);
                }
            } else {
                tm.setForeground(x + jb + 1, jo, imgMap[3][0], Tile.BEHAVIOR_SOLID);
                for (int i = x + jb + 2; i < stop; i++) {
                    tm.setForeground(i, jo, getDirtImage(), Tile.BEHAVIOR_SOLID);
                }
                tm.setForeground(stop, jo, imgMap[3][2], Tile.BEHAVIOR_SOLID);
            }
        }
    }
    
    private final static void blockWall(final int x, final int y, final int w, final int h) {
    	for (int i = 0; i < w; i++) {
    		final int xi = x + i;
    		for (int j = 0; j < h; j++) {
    			solidBlock(xi, y + j);
    		}
    	}
    }
    
    private final static void upBlockStep(final int x, final int y, final int w, final boolean ramp) {
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
    
    private final static void downBlockStep(final int x, final int y, final int w, final boolean ramp) {
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
    
    private final static void naturalRise(final int x, final int y, final int w, final int h) {
        final int ystop = y + h;
        for (int j = y; j < ystop; j++) {
            tm.setBackground(x, j, imgMap[2][3]);
            tm.setBackground(x + w + 1, j, imgMap[2][4]);
        }
        final int stop = x + w;
        for (int i = x + 1; i <= stop; i++) {
            tm.setBackground(i, ystop, imgMap[1][1], FurGuardiansGame.TILE_FLOOR);
            for (int j = y; j < ystop; j++) {
                tm.setBackground(i, j, getDirtImage());
            }
        }
        tm.setForeground(x, ystop, imgMap[1][3], FurGuardiansGame.TILE_FLOOR);
        tm.setForeground(stop + 1, ystop, imgMap[1][4], FurGuardiansGame.TILE_FLOOR);
    }
    
    private final static void colorRise(final int x, final int y, final int w, final int h, final int _o) {
        final int o = _o * 2 + 2, o1 = o + 1, ystop = y + h;
        for (int j = y; j < ystop; j++) {
            tm.setBackground(x, j, imgMap[o1][5]);
            tm.setBackground(x + w + 1, j, imgMap[o1][7]);
        }
        final int stop = x + w;
        for (int i = x + 1; i <= stop; i++) {
            tm.setBackground(i, ystop, imgMap[o][6], FurGuardiansGame.TILE_FLOOR);
            for (int j = y; j < ystop; j++) {
                tm.setBackground(i, j, imgMap[o1][6]);
            }
        }
        tm.setForeground(x, ystop, imgMap[o][5], FurGuardiansGame.TILE_FLOOR);
        tm.setForeground(stop + 1, ystop, imgMap[o][7], FurGuardiansGame.TILE_FLOOR);
    }
    
    private final static void wall(final int x, final int y, final int w, final int h) {
        final int ystop = y + h - 1, xstop = x + w + 1;
        for (int j = y; j <= ystop; j++) {
        	final int iy = (floorMode == FLOOR_BRIDGE && j < ystop) ? 5 : 4;
            setFgShadowed(x, j, iy, 0, Tile.BEHAVIOR_SOLID);
            for (int i = x + 1; i < xstop; i++) {
                setFgShadowed(i, j, iy, 1, Tile.BEHAVIOR_SOLID);
            }
            setFgShadowed(xstop, j, iy, 2, Tile.BEHAVIOR_SOLID);
        }
    }
    
    // x - h + 2 to x + ((stop + 1) * 2 - 1) + 1
    private final static void slantUp(final int x, final int y, final int stop, final int h) {
        slant(x, y, stop, h, true);
    }
    
    private final static void slant(final int x, final int y, final int stop, final int h, final boolean up) {
        final int ystop = y + h, w = getSlantBase(stop), m, c1, c2, c3;
        final byte b;
        if (up) {
            m = 1;
            c1 = 3;
            c2 = 4;
            c3 = 0;
            b = FurGuardiansGame.TILE_UPSLOPE_FLOOR;
        } else {
            m = -1;
            c1 = 4;
            c2 = 3;
            c3 = 2;
            b = FurGuardiansGame.TILE_DOWNSLOPE_FLOOR;
        }
        for (int jo = y; jo < ystop; jo++) {
            final int jb = jo - y;
            tm.setForeground(x - m * jb, jo, imgMap[jb == (h - 1) ? 7 : 5][c1]);
            for (int i = 1; i <= w; i++) {
                tm.setForeground(x + m * (i - jb), jo, getDirtImage());
            }
            tm.setForeground(x + m * (w + 1 - jb), jo, imgMap[4][c2]);
        }
        for (int jb = 0; jb <= stop; jb++) {
            final int jo = jb + ystop, off = jb + 3 - h;
            tm.setForeground(x + m * (off - 2), jo, imgMap[3][c1], b);
            tm.setForeground(x + m * (off - 1), jo, jb == stop ? imgMap[6][c2] : imgMap[3][c3]);
            if (jb < stop) {
                for (int i = jb; i <= w - 3 - jb; i++) {
                    tm.setForeground(x + m * (i + 3 - h), jo, getDirtImage());
                }
                tm.setForeground(x + m * (w + 1 - h - jb), jo, imgMap[4][c2]);
            }
        }
    }
    
    private final static TileMapImage getDirtImage() {
        return imgMap[Mathtil.rand(90) ? 2 : 3][1];
    }
    
    private final static void pit(final int x, final int y, final int w) {
        final boolean hive = builder instanceof HexBuilder;
    	final int stop = x + w + 1, ystop = (floorMode == FLOOR_BRIDGE || floorMode == FLOOR_TRACK || hive) ? (y + 1) : y;
    	final byte OPEN = Tile.BEHAVIOR_OPEN, SOLID = Tile.BEHAVIOR_SOLID;
    	for (int j = 0; j <= ystop; j++) {
    	    if (hive) {
    	        final Tile left = tm.getTile(x, j), right = tm.getTile(stop, j);
    	        final Tile aboveLeft = tm.getTile(x, j + 1), aboveRight = tm.getTile(stop, j + 1);
    	        final Object leftFore = DynamicTileMap.getRawForeground(left), leftBack = DynamicTileMap.getRawBackground(left);
    	        for (int imY = 1; imY < 6; imY += 4) {
    	            final TileMapImage[] row = imgMap[imY], row2 = imgMap[imY + 1];
        	        if (leftFore == row[1]) {
        	            tm.setForeground(x, j, row[4], SOLID);
        	        } else if (leftBack == row2[1]) {
                        tm.setBackground(x, j, row2[4], SOLID);
                    } else if (leftFore == row[0]) {
                        tm.setForeground(x, j, null, (leftBack == null) ? OPEN : SOLID);
                    } else if (leftBack == row2[0]) {
                        final byte b;
                        if (leftFore == null) {
                            b = OPEN;
                        } else if ((leftFore == imgMap[1][2] || leftFore == imgMap[5][2]) && Tile.getBehavior(aboveLeft) != SOLID) {
                            b = FurGuardiansGame.TILE_DOWNSLOPE;
                        } else {
                            b = SOLID;
                        }
                        tm.setBackground(x, j, null, b);
                    } else {
                        continue;
                    }
        	        break;
    	        }
    	        for (int imY = 1; imY < 6; imY += 4) {
    	            final TileMapImage[] row = imgMap[imY], row2 = imgMap[imY + 1];
        	        final Object rightFore = DynamicTileMap.getRawForeground(right), rightBack = DynamicTileMap.getRawBackground(right);
        	        if (rightFore == row[1]) {
                        tm.setForeground(stop, j, row[3], SOLID);
                    } else if (rightBack == row2[1]) {
                        tm.setBackground(stop, j, row2[3], SOLID);
                    } else if (rightFore == row[2]) {
                        tm.setForeground(stop, j, null, (rightBack == null) ? OPEN : SOLID);
                    } else if (rightBack == row2[2]) {
                        final byte b;
                        if (rightFore == null) {
                            b = OPEN;
                        } else if ((rightFore == imgMap[1][0] || rightFore == imgMap[5][0]) && Tile.getBehavior(aboveRight) != SOLID) {
                            b = FurGuardiansGame.TILE_UPSLOPE;
                        } else {
                            b = SOLID;
                        }
                        tm.setBackground(stop, j, null, b);
                    } else {
                        continue;
                    }
                    break;
                }
    	    } else if (floorMode == FLOOR_GRASSY) {
	    		final int iy = (j == y) ? 1 : 2;
		    	tm.setForeground(x, j, imgMap[iy][2], SOLID);
		    	tm.setForeground(stop, j, imgMap[iy][0], SOLID);
    		} else if (floorMode == FLOOR_BLOCK) {
    		    if (j == y) {
        			solidBlock(x, j);
        			solidBlock(stop, j);
    		    }
    		} else if (floorMode == FLOOR_BRIDGE || floorMode == FLOOR_TRACK) {
    		    if (j == y) {
                    tm.setBackground(x, j, imgMap[Tile.getBehavior(tm.getTile(x, j + 1)) != OPEN ? 3 : 2][0], SOLID);
                    tm.setBackground(stop, j, imgMap[2][0], SOLID);
    		    } else if (j == ystop) {
    		        tm.setBackground(x, j, imgMap[1][0]); // Might have a block at pit edge; don't make it open
    		        tm.setBackground(stop, j, imgMap[1][0]);
    		    } else if (floorMode == FLOOR_TRACK) {
    		    	tm.setBackground(x, j, imgMap[3][0], OPEN);
    		    	tm.setBackground(stop, j, imgMap[3][0], OPEN);
    		    }
    		} else {
    		    throw new IllegalStateException("Unexpected floorMode " + floorMode);
    		}
	    	for (int i = x + 1; i < stop; i++) {
	    		tm.removeTile(i, j);
	    	}
    	}
    }
    
    private final static void bush(final int x, final int y, final int w) {
        tm.setForeground(x, y, imgMap[1][5]);
        final int stop = x + w;
        for (int i = x + 1; i <= stop; i++) {
            tm.setForeground(i, y, imgMap[1][6]);
        }
        tm.setForeground(stop + 1, y, imgMap[1][7]);
    }
    
    private final static void tree(final int x, final int y) {
    	for (int j = 0; j < 2; j++) {
    		tm.setForeground(x + 1, y + j, imgMap[7][1]);
    		tm.setForeground(x + 2, y + j, imgMap[7][2]);
    		tm.setForeground(x + 1 + j, y + 2, imgMap[6][2]);
    		tm.setForeground(x + 1 + j, y + 3, imgMap[5][2]);
    		tm.setForeground(x + j, y + 3 + j, imgMap[5][0]);
    		tm.setForeground(x + 3 - j, y + 3 + j, imgMap[5][1]);
    	}
    	tm.setForeground(x, y + 2, imgMap[6][0]);
		tm.setForeground(x + 3, y + 2, imgMap[6][1]);
    }
    
    private final static void hexagon(final int x, final int y) {
        final int imY = Mathtil.rand() ? 1 : 5;
        hexTopCorner(x, y, 0, imY, FurGuardiansGame.TILE_UPSLOPE);
        final int x1 = x + 1;
        if (!tm.isBadColumn(x1)) {
            tm.setForeground(x1, y, imgMap[imY][1], Tile.BEHAVIOR_SOLID);
        }
        hexTopCorner(x + 2, y, 2, imY, FurGuardiansGame.TILE_DOWNSLOPE);
        final int y1 = y - 1;
        if (y1 < 0) {
            return;
        }
        final TileMapImage[] row = imgMap[imY + 1];
        for (int i = 0; i < 3; i++) {
            final int xi = x + i;
            if (!tm.isBadColumn(xi)) {
                tm.setBackground(xi, y1, row[i], Tile.BEHAVIOR_SOLID);
            }
        }
    }
    
    private final static void hexTopCorner(final int x, final int y, final int imX, final int imY, final byte b) {
        final int index = tm.getIndex(x, y);
        if (index < 0) {
            return;
        }
        tm.setForeground(index, imgMap[imY][imX], (Tile.getBehavior(tm.getTile(index)) == Tile.BEHAVIOR_SOLID) ? Tile.BEHAVIOR_SOLID : b);
    }
    
    private final static void fillHexagonGaps(final int x, final int w) {
        final int xw = x + w, j = floor + 1;
        for (int i = x; i < xw; i++) {
            final byte ft = getHexagonFloorType(i);
            final int imX;
            if (ft == HEX_RISE) {
                imX = 2;
            } else if (ft == HEX_DOWN) {
                imX = 1;
            } else if (ft == HEX_FALL) {
                imX = 0;
            } else {
                imX = -1;
            }
            if (imX != -1) {
                tm.setBackground(i, j, imgMap[3][imX], Tile.BEHAVIOR_SOLID);
            }
        }
    }
    
    private final static void gem(final int x, final int y) {
        if (tileGem == null) {
        	tileGem = tm.getTile(null, FurGuardiansGame.gem[0], FurGuardiansGame.TILE_GEM);
        }
        tm.setTile(x, y, tileGem);
        numGems++;
    }
    
    private final static void bonus(final int x, final int y) {
        final int r = Mathtil.randi(0, 299);
        if (r < 100) {
            gem(x, y);
        } else if (r < 200) {
            bumpableBlock(x, y);
        } else {
            breakableBlock(x, y);
        }
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
        "***",
        
        "*\n" +
        "*\n" +
        "*\n" +
        "\n" +
        "*\n"
    };
    
    private final static int gemMsg(final int x, final int y, final String msg, final boolean render) {
    	final int size = msg.length();
    	int xc = x;
    	for (int i = 0; i < size; i++) {
    		xc += (1 + gemChr(xc, y, msg.charAt(i), render));
    	}
    	return xc - x - 1;
    }
    
    private final static int gemChr(final int x, int y, final char chr, final boolean render) {
        if (chr == ' ') {
            return 1;
        }
    	final int c = (chr == '!') ? 26 : (java.lang.Character.toUpperCase(chr) - 'A');
    	if (c < 0 || c >= gemFont.length) {
    		return -1;
    	}
    	final String s = gemFont[c];
    	final int size = s.length();
    	if (floorMode == FLOOR_BRIDGE) {
    	    y++;
    	}
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
    
    private final static PixelFilter getHillFilter(final int mode) {
        switch (mode) {
            case 0 : return Map.theme.getHillFilter0();
            case 1 : return Map.theme.getHillFilter1();
            case 2 : return Map.theme.getHillFilter2();
        }
        throw new IllegalArgumentException(String.valueOf(mode));
    }
}
