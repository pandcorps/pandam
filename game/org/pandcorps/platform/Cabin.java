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

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.platform.Tiles.*;

public class Cabin {
	private static Panroom room = null;
	private static TileMap tm = null;
	private static Panmage timg = null;
	private static TileMapImage[][] imgMap = null;
	
	protected final static class CabinScreen extends Panscreen {
		@Override
		protected final void load() throws Exception {
			final Pangine engine = Pangine.getEngine();
			engine.setBgColor(Pancolor.BLACK);
			room = PlatformGame.createRoom(256, 192);
			room.center();
			
			tm = new TileMap(Pantil.vmid(), room, ImtilX.DIM, ImtilX.DIM);
			Level.tm = tm;
			final BufferedImage tbuf = ImtilX.loadImage("org/pandcorps/platform/res/bg/Tiles.png", 128, null);
			final BufferedImage buf = ImtilX.loadImage("org/pandcorps/platform/res/bg/Cabin.png", 128, null);
			Imtil.copy(tbuf, buf, 64, 0, 16, 16, 32, 64);
			timg = engine.createImage("img.cabin", buf);
			imgMap = tm.splitImageMap(timg);
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
				tm.initTile(2, j).setBackground(tml);
				tm.initTile(3, j).setBackground(tmi);
				tm.initTile(4, j).setBackground(tmr);
				tm.initTile(5, j).setBackground(tmi);
				tm.initTile(6, j).setBackground(tml);
				tm.fillBackground(tmi, 7, j, 2, 1);
				tm.initTile(9, j).setBackground(tmr);
				tm.initTile(10, j).setBackground(tmi);
				tm.initTile(11, j).setBackground(tml);
				tm.initTile(12, j).setBackground(tmi);
				tm.initTile(13, j).setBackground(tmr);
			}
			for (int i = 5; i <= 10; i += 5) {
				tm.fillBackground(imgMap[1][3], i - 1, 8, 1, 2);
				tm.fillBackground(imgMap[1][2], i, 8, 1, 2);
				tm.initTile(i, 5).setBackground(imgMap[1][4]);
				tm.fillBackground(imgMap[1][1], i + 1, 8, 1, 2);
			}
			tm.fillBackground(imgMap[1][2], 7, 8, 2, 2);
			tm.fillBackground(imgMap[1][0], 4, 10, 8, 1);
			tm.fillBackground(imgMap[3][0], 1, 4, 1, 4);
			tm.fillBackground(imgMap[3][4], 14, 4, 1, 4);
			for (int t = 0; t <= 1; t++) {
				final int j = 8 + t;
				tm.initTile(1 + t, j).setBackground(imgMap[2][0]);
				tm.initTile(2 + t, j).setBackground(imgMap[0][1]);
				tm.initTile(13 - t, j).setBackground(imgMap[0][3]);
				tm.initTile(14 - t, j).setBackground(imgMap[2][4]);
			}
			tm.initTile(3, 8).setBackground(imgMap[0][2]);
			tm.initTile(4, 9).setBackground(imgMap[0][5]);
			tm.initTile(11, 9).setBackground(imgMap[0][6]);
			tm.initTile(12, 8).setBackground(imgMap[0][4]);
			
			tm.initTile(3, 10).setBackground(imgMap[2][0]);
			tm.initTile(12, 10).setBackground(imgMap[2][4]);
			
			for (int i = 0; i < 3; i++) {
				final int j = 3 - i;
				tm.initTile(0, j).setBackground(imgMap[2 + i][0]);
				if (i < 2) {
					tm.initTile(1, j).setBackground(imgMap[1 + i][5]);
					tm.initTile(14, j).setBackground(imgMap[1 + i][6]);
				}
				tm.initTile(15, j).setBackground(imgMap[2 + i][4]);
			}
			
			for (int i = 1; i <= 14; i++) {
				tm.initTile(i, 1).setSolid(true);
				tm.initTile(i, 8).setSolid(true);
			}
			for (int j = 2; j <= 7; j++) {
				tm.initTile(1, j).setSolid(true);
				tm.initTile(14, j).setSolid(true);
			}
			
			final Panctor owl = new Panctor("act.owl");
			owl.setView(PlatformGame.owl);
			room.addActor(owl);
			owl.getPosition().set(112, 128, 1);
			
			//TODO All players?
			PlatformGame.addHud(room, true);
			final Player player = new Player(PlatformGame.pcs.get(0));
			player.mode = Player.MODE_DISABLED;
			room.addActor(player);
			PlatformGame.setPosition(player, 74, 32, PlatformGame.DEPTH_PLAYER);
			
			final Pantext instr = new Pantext("act.instr", PlatformGame.font, "Hoo! Hoo! Pick one!");
			room.addActor(instr);
			instr.getPosition().set(128, 112, 1);
			instr.centerX();
			engine.addTimer(instr, 80, new TimerListener() {
				@Override public void onTimer(final TimerEvent event) {
					instr.destroy();
					for (int i = 0; i < 4; i++) {
						tm.initTile(3 + (i * 3), 5).setForeground(imgMap[0][0], PlatformGame.TILE_BUMP);
					}
					//TODO Show 4 possible colors shuffling
					player.mode = Player.MODE_NORMAL;
				}});
		}
	}
	
	protected final static CabinTileHandler cabinTileHandler = new CabinTileHandler();
	
	protected final static class CabinTileHandler extends TileHandler {
		@Override
		protected boolean isNormalAward(final Tile t) {
			return true;
		}
		
		@Override
		protected boolean isSpecialBump(final Tile t) {
			return false;
		}
		
		@Override
		protected final int rndAward() {
			final int r = Mathtil.randi(0, 9999);
			// Looks like bonus Gems are pre-sorted, so 25% chance of getting 1000,
			// but decide after Player picks, so 50% chance of 1000, then 35/14.5/0.5
			if (r < 5000) {
				return GemBumped.AWARD_4;
			} else if (r < 8500) {
				return GemBumped.AWARD_3;
			} else if (r < 9950) {
				return GemBumped.AWARD_2;
			}
			//TODO Show contents of other 3, add levelGems to total, return to Map
			return GemBumped.AWARD_DEF;
		}
		
		@Override
		protected final TileMapImage getBumpedImage() {
			return imgMap[4][2];
		}
	}
}
