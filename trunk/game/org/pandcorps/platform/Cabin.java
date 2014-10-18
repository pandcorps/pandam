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
package org.pandcorps.platform;

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.platform.Player.*;
import org.pandcorps.platform.Tiles.*;

public class Cabin {
	private final static int NUM_BLOCKS = 4;
	
	private static Panroom room = null;
	private static TileMap tm = null;
	private static Panmage timg = null;
	private static TileMapImage[][] imgMap = null;
	private static TileMapImage bumpedImage = null;
	private static PlayerContext pc = null;
	private static Pantext instr = null;
	private static Panctor[] gems = new Panctor[NUM_BLOCKS];
	
	protected final static class CabinScreen extends Panscreen {
		@Override
		protected final void load() throws Exception {
		    clear();
			final Pangine engine = Pangine.getEngine();
			engine.setBgColor(Pancolor.BLACK);
			room = PlatformGame.createRoom(256, 192);
			room.center();
			
			tm = new TileMap(Pantil.vmid(), room, ImtilX.DIM, ImtilX.DIM);
			Level.tm = tm;
			final Img tbuf = ImtilX.loadImage("org/pandcorps/platform/res/bg/Tiles.png", 128, null);
			final Img buf = ImtilX.loadImage("org/pandcorps/platform/res/bg/Cabin.png", 128, null);
			Imtil.copy(tbuf, buf, 64, 0, 16, 16, 32, 64);
			tbuf.close();
			timg = engine.createImage("img.cabin", buf);
			imgMap = tm.splitImageMap(timg);
			bumpedImage = imgMap[4][2];
			room.addActor(tm);
			
			tm.fillBackground(imgMap[4][1], 1, 1, 14, 1);
			tm.fillBackground(imgMap[4][3], 2, 2, 12, 1);
			for (int j = 3; j <= 7; j += 1) {
				final int ij;
				switch (j) {
					case 3 :
					case 6 :
						ij = 3;
						break;
					case 4 :
					case 7 :
						ij = 2;
						break;
					default :
						ij = 1;
						break;
				}
				final TileMapImage tml = imgMap[ij][1], tmi = imgMap[ij][2], tmr = imgMap[ij][3];
				tm.setBackground(2, j, tml);
				tm.setBackground(3, j, tmi);
				tm.setBackground(4, j, tmr);
				tm.setBackground(5, j, tmi);
				tm.setBackground(6, j, tml);
				tm.fillBackground(tmi, 7, j, 2, 1);
				tm.setBackground(9, j, tmr);
				tm.setBackground(10, j, tmi);
				tm.setBackground(11, j, tml);
				tm.setBackground(12, j, tmi);
				tm.setBackground(13, j, tmr);
			}
			for (int i = 5; i <= 10; i += 5) {
				tm.fillBackground(imgMap[1][3], i - 1, 8, 1, 2);
				tm.fillBackground(imgMap[1][2], i, 8, 1, 2);
				tm.setBackground(i, 5, imgMap[1][4]);
				tm.fillBackground(imgMap[1][1], i + 1, 8, 1, 2);
			}
			tm.fillBackground(imgMap[1][2], 7, 8, 2, 2);
			tm.fillBackground(imgMap[1][0], 4, 10, 8, 1);
			tm.fillBackground(imgMap[3][0], 1, 4, 1, 4);
			tm.fillBackground(imgMap[3][4], 14, 4, 1, 4);
			for (int t = 0; t <= 1; t++) {
				final int j = 8 + t;
				tm.setBackground(1 + t, j, imgMap[2][0]);
				tm.setBackground(2 + t, j, imgMap[0][1]);
				tm.setBackground(13 - t, j, imgMap[0][3]);
				tm.setBackground(14 - t, j, imgMap[2][4]);
			}
			tm.setBackground(3, 8, imgMap[0][2]);
			tm.setBackground(4, 9, imgMap[0][5]);
			tm.setBackground(11, 9, imgMap[0][6]);
			tm.setBackground(12, 8, imgMap[0][4]);
			
			tm.setBackground(3, 10, imgMap[2][0]);
			tm.setBackground(12, 10, imgMap[2][4]);
			
			for (int i = 0; i < 3; i++) {
				final int j = 3 - i;
				tm.setBackground(0, j, imgMap[2 + i][0]);
				if (i < 2) {
					tm.setBackground(1, j, imgMap[1 + i][5]);
					tm.setBackground(14, j, imgMap[1 + i][6]);
				}
				tm.setBackground(15, j, imgMap[2 + i][4]);
			}
			
			for (int i = 1; i <= 14; i++) {
				tm.setBehavior(i, 1, Tile.BEHAVIOR_SOLID);
				tm.setBehavior(i, 8, Tile.BEHAVIOR_SOLID);
			}
			for (int j = 2; j <= 7; j++) {
				tm.setBehavior(1, j, Tile.BEHAVIOR_SOLID);
				tm.setBehavior(14, j, Tile.BEHAVIOR_SOLID);
			}
			
			final Panctor owl = new Panctor("act.owl");
			owl.setView(PlatformGame.owl);
			room.addActor(owl);
			owl.getPosition().set(112, 128, 1);
			
			//TODO All players?
			PlatformGame.initTouchButtons(null, false, true, null);
			pc = PlatformGame.pcs.get(0);
			final Player player = new Player(pc);
			room.addActor(player);
			PlatformGame.setPosition(player, 74, 32, PlatformGame.DEPTH_PLAYER);
			PlatformGame.addHud(room, false, true);
			
			final String txt = Mathtil.rand() ? loadName() : loadShuffle();
			instr = new Pantext("act.instr", PlatformGame.font, txt);
			room.addActor(instr);
			instr.getPosition().set(128, 114, 1);
			instr.centerX();
			PlatformGame.fadeIn(room);
			PlatformGame.musicOcarina.changeMusic();
		}
		
		private final String loadShuffle() {
			cabinTileHandler = new ShuffleTileHandler();
			pc.player.mode = Player.MODE_DISABLED;
			for (int i = 0; i < NUM_BLOCKS; i++) {
				tm.setForeground(3 + (i * 3), 5, imgMap[0][0], PlatformGame.TILE_BUMP);
			}
			shuffle(30, 0);
			return "Hoo! Hoo! Pick one!";
		}
		
		private final String loadName() {
			cabinTileHandler = new NameTileHandler();
			final String name = pc.getBonusName();
			PlatformGame.blockWord = name;
			final int size = name.length();
			int x = 12 - size;
			if (x % 2 == 1) {
				x++;
			}
			x = (x / 2) + 2;
			for (int i = 0; i < size; i++) {
				tm.setForeground(x + i, 5, PlatformGame.getBlockWordLetter(i), PlatformGame.TILE_BUMP);
			}
			Pangine.getEngine().addTimer(tm, 60, new TimerListener() {
				@Override public final void onTimer(final TimerEvent event) {
                    instr.destroy();
				}});
			return "Hoo! Hoo! Hit " + ((size == 1) ? "the block" : "them") + "!";
		}
		
		@Override
        protected void destroy() {
            Level.tm = null;
            timg.destroy();
            PlatformGame.blockWord = PlatformGame.defaultBlockWord;
        }
	}
	
	protected static TileHandler cabinTileHandler = null;
	
	protected final static class ShuffleTileHandler extends CabinTileHandler {
		@Override
		protected boolean isNormalAward(final Tile t) {
			return true;
		}
		
		@Override
		protected final int rndAward() {
		    final int r = Mathtil.randi(0, 9999), awd;
            // Looks like bonus Gems are pre-sorted, so 25% chance of getting 1000,
            // but decide after Player picks, so 80% chance of 1000, then 18/1.9/0.1
            if (r < 8000) {
                awd = GemBumped.AWARD_4;
            } else if (r < 9800) {
                awd = GemBumped.AWARD_3;
            } else if (r < 9990) {
                awd = GemBumped.AWARD_2;
            } else {
                awd = GemBumped.AWARD_DEF;
            }
		    pc.player.mode = Player.MODE_DISABLED;
            shuffle(45, awd);
            return awd;
		}
	}
	
	protected final static class NameTileHandler extends CabinTileHandler {
		@Override
		protected boolean isNormalAward(final Tile t) {
			return false;
		}
		
		@Override
		protected TileMapImage getBumpedImage() {
			if (Coltil.size(Level.collectedLetters) == PlatformGame.blockWord.length() && pc.player.mode == Player.MODE_NORMAL) {
				pc.player.mode = Player.MODE_DISABLED;
				pc.player.addGems(500);
				Pangine.getEngine().addTimer(tm, 20, new TimerListener() {
					@Override public final void onTimer(final TimerEvent event) {
						PlatformGame.clearLetters(PlatformGame.gemCyanAnm, new Runnable() { @Override public final void run() {
							finish();
						}});
					}});
			}
			return super.getBumpedImage();
		}
	}
	
	protected abstract static class CabinTileHandler extends TileHandler {
		@Override
		protected TileMapImage getBumpedImage() {
			return bumpedImage;
		}
	}
	
	private final static void shuffle(final int time, final int awd) {
		shuffle(time, awd, 10);
	}
	
	private final static void shuffle(final int time, final int awd, final int whiteTime) {
        Pangine.getEngine().addTimer(pc.player, time, new TimerListener() {
            @Override public void onTimer(final TimerEvent event) {
                final boolean end = awd > 0;
                final List<Integer> awds = new ArrayList<Integer>(NUM_BLOCKS - (end ? 1 : 0));
                add(awds, awd, GemBumped.AWARD_4);
                add(awds, awd, GemBumped.AWARD_3);
                add(awds, awd, GemBumped.AWARD_2);
                add(awds, awd, GemBumped.AWARD_DEF);
                boolean shuffling = true;
                while (shuffling) {
	                Collections.shuffle(awds);
	                shuffling = false;
	                if (!end) {
	                	for (int i = 0; i < NUM_BLOCKS; i++) {
	                		final Panctor gem = gems[NUM_BLOCKS - i - 1];
	                		if (gem != null && gem.getView() == getGemImg(awds.get(i))) {
	                			shuffling = true;
	                			break;
	                		}
	                	}
	                }
                }
                final boolean white = whiteTime <= 0;
                for (int i = 0; i < NUM_BLOCKS; i++) {
                    final int x = 3 + (i * 3);
                    if (end) {
                    	final int index = tm.getIndex(x, 5);
                        if (DynamicTileMap.getRawForeground(tm.getTile(index)) == bumpedImage) {
                            continue;
                        }
                        tm.setForeground(index, bumpedImage);
                    }
                    Panctor gem = gems[i];
                    if (gem == null) {
                        gem = end ? new Blink(15) : new Panctor();
                        PlatformGame.setPosition(gem, x * 16, 97, PlatformGame.DEPTH_SPARK);
                        room.addActor(gem);
                        gems[i] = gem;
                    }
                    gem.setView(white ? PlatformGame.gemWhite : getGemImg(awds.remove(awds.size() - 1)));
                }
                if (end) {
                	Pangine.getEngine().addTimer(gems[0], 105, new TimerListener() {
						@Override public final void onTimer(final TimerEvent event) {
	                        finish();
						}});
                } else {
                    final int newTime = time - ((time > 10) ? 2 : 1);
                    if (white) {
                    	Pangine.getEngine().addTimer(gems[0], 45, new TimerListener() {
							@Override public final void onTimer(final TimerEvent event) {
		                    	pc.player.mode = Player.MODE_NORMAL;
		                        clear();
							}});
                    } else if (newTime > 1) {
                        shuffle(newTime, awd);
                    } else {
                        shuffle(1, awd, whiteTime - 1);
                    }
                }
            }});
    }
	
	private final static Panmage getGemImg(final Integer key) {
		return GemBumped.getAnm(key.intValue()).getFrames()[0].getImage();
	}
	
	private final static void add(final List<Integer> awds, final int usedAwd, final int currAwd) {
        if (usedAwd != currAwd) {
            awds.add(Integer.valueOf(currAwd));
        }
    }
	
	private final static void clear() {
	    Panctor.destroy(gems);
	    Panctor.destroy(instr);
	}
	
	private final static void finish() {
		clear();
        pc.onFinishBonus();
        PlatformGame.markerClose();
        PlatformGame.playTransition(PlatformGame.musicLevelEnd);
	}
}
