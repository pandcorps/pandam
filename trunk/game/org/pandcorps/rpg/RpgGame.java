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
package org.pandcorps.rpg;

import java.util.IdentityHashMap;

import org.pandcorps.core.img.Pancolor;
import org.pandcorps.game.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Fonts.FontRequest;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.TileMapImage;

public class RpgGame extends BaseGame {
    /*
    Character sprite generator.
    Doors transport player.
    Load/unload neighboring TileMaps for large areas as needed
    (and maybe offset actor positions so current TileMap always starts at origin).
    License comment years.
    */
    
	private static Panroom room = null;
	/*package*/ static Pantext hudInteract = null;
	/*package*/ final static StringBuilder hudInteractText = new StringBuilder();
	
	@Override
	protected void init(final Panroom room) throws Exception {
		Pangine.getEngine().setTitle("RPG");
		RpgGame.room = room;
		loadBackground();
	}
	
	private final static class QuaintTileListener implements TileListener {
		private final IdentityHashMap<TileMapImage, TileMapImage> map = new IdentityHashMap<TileMapImage, TileMapImage>();
		
		private QuaintTileListener(final TileMapImage[][] imgMap) {
			for (int x = 1; x <= 5; x++) {
				for (int y = 0; y <= 2; y++) {
					map.put(imgMap[5 + y][x], imgMap[5 + ((y + 1) % 3)][x]);
				}
			}
		}
		
		@Override
		public boolean isActive() {
			return Pangine.getEngine().getClock() % 8 == 0;
		}
		
		@Override
		public final void onStep(final Tile tile) {
			final TileMapImage next = map.get(DynamicTileMap.getRawBackground(tile));
			if (next != null) {
				tile.setBackground(next);
			}
		}
	}
	
	private final static void loadBackground() {
		final Pangine engine = Pangine.getEngine();
		final Panmage[] containers = createSheet("container", "org/pandcorps/rpg/res/misc/Container01.png", ImtilX.DIM, Container.o);
		final Panmage[] doors = createSheet("door", "org/pandcorps/rpg/res/misc/DoorQuaint.png");
		final Font hudFont = Fonts.getClassic(new FontRequest(8), Pancolor.WHITE);
		final DynamicTileMap tm = new DynamicTileMap("act.tilemap", room, ImtilX.DIM, ImtilX.DIM);
		tm.setOccupantDepth(DepthMode.Y);
		tm.setImageMap(engine.createImage("img.tile.quaint", ImtilX.loadImage("org/pandcorps/rpg/res/bg/TileQuaint.png", 128, null)));
		final TileMapImage[][] imgMap = tm.splitImageMap();
		tm.setTileListener(new QuaintTileListener(imgMap));
		tm.fillBackground(imgMap[5][0]);
		tm.getTile(6, 5).setBackground(imgMap[4][1]);
		tm.getTile(6, 5).setSolid(true);
		tm.getTile(6, 6).setBackground(imgMap[4][1]);
		tm.getTile(6, 6).setSolid(true);
		tm.getTile(6, 7).setBackground(imgMap[3][1]);
		tm.getTile(6, 7).setSolid(true);
		tm.getTile(6, 8).setBackground(imgMap[2][1]);
		tm.getTile(6, 8).setSolid(true);
		tm.getTile(6, 9).setBackground(imgMap[1][1]);
		tm.getTile(6, 9).setSolid(true);
		tm.getTile(6, 10).setForeground(imgMap[0][1]);
		tm.getTile(7, 5).setBackground(imgMap[3][4]); // Window
		tm.getTile(7, 5).setSolid(true);
		tm.getTile(7, 6).setBackground(imgMap[4][2]);
		tm.getTile(7, 6).setSolid(true);
		tm.getTile(7, 7).setBackground(imgMap[3][2]);
		tm.getTile(7, 7).setSolid(true);
		tm.getTile(7, 8).setBackground(imgMap[2][2]);
		tm.getTile(7, 8).setSolid(true);
		tm.getTile(7, 9).setBackground(imgMap[1][2]);
		tm.getTile(7, 9).setSolid(true);
		tm.getTile(7, 10).setForeground(imgMap[0][2]);
		tm.getTile(8, 5).setBackground(imgMap[4][3]);
		tm.getTile(8, 5).setSolid(true);
		tm.getTile(8, 6).setBackground(imgMap[4][3]);
		tm.getTile(8, 6).setSolid(true);
		tm.getTile(8, 7).setBackground(imgMap[3][3]);
		tm.getTile(8, 7).setSolid(true);
		tm.getTile(8, 8).setBackground(imgMap[2][3]);
		tm.getTile(8, 8).setSolid(true);
		tm.getTile(8, 9).setBackground(imgMap[1][3]);
		tm.getTile(8, 9).setSolid(true);
		tm.getTile(8, 10).setForeground(imgMap[0][1]);
		for (int i = 9; i <= 13; i++) {
    		tm.getTile(i, 7).setBackground(imgMap[4][2]);
            tm.getTile(i, 7).setSolid(true);
            tm.getTile(i, 8).setBackground(imgMap[4][2]);
            tm.getTile(i, 8).setSolid(true);
            tm.getTile(i, 9).setBackground(imgMap[2][4]);
            tm.getTile(i, 9).setSolid(true);
            tm.getTile(i, 10).setForeground(imgMap[0][1]);
		}
		tm.getTile(12, 7).setBackground(imgMap[0][5]); // Sign
		tm.getTile(14, 7).setBackground(imgMap[1][6]); // Shadow
		tm.getTile(14, 8).setBackground(imgMap[0][6]);
		tm.getTile(4, 8).setBackground(imgMap[1][0]); // Tree
		tm.getTile(4, 8).setSolid(true);
		tm.getTile(4, 9).setForeground(imgMap[0][0]);
		tm.getTile(7, 3).setBackground(imgMap[2][0]); // Many flowers
		tm.getTile(9, 1).setBackground(imgMap[3][0]); // Some flowers
		tm.getTile(11, 11).setBackground(imgMap[3][0]);
		tm.getTile(12, 2).setBackground(imgMap[4][0]); // Dirt patch
		tm.getTile(2, 5).setBackground(imgMap[2][6]);
		tm.getTile(3, 5).setBackground(imgMap[2][6]);
		tm.getTile(1, 4).setBackground(imgMap[5][1]); // Water
		tm.getTile(1, 4).setSolid(true);
		tm.getTile(2, 4).setBackground(imgMap[5][5]);
		tm.getTile(2, 4).setSolid(true);
		tm.getTile(3, 4).setBackground(imgMap[5][5]);
		tm.getTile(3, 4).setSolid(true);
		tm.getTile(4, 4).setBackground(imgMap[5][2]);
		tm.getTile(4, 4).setSolid(true);
		tm.getTile(0, 3).setBackground(imgMap[4][6]);
		tm.getTile(1, 3).setBackground(imgMap[5][5]);
		tm.getTile(1, 3).setSolid(true);
		tm.getTile(2, 3).setBackground(imgMap[5][5]);
		tm.getTile(2, 3).setSolid(true);
		tm.getTile(3, 3).setBackground(imgMap[5][5]);
		tm.getTile(3, 3).setSolid(true);
		tm.getTile(4, 3).setBackground(imgMap[5][5]);
		tm.getTile(4, 3).setSolid(true);
		tm.getTile(5, 3).setBackground(imgMap[3][6]);
		tm.getTile(0, 2).setBackground(imgMap[4][6]);
		tm.getTile(1, 2).setBackground(imgMap[5][5]);
		tm.getTile(1, 2).setSolid(true);
		tm.getTile(2, 2).setBackground(imgMap[5][5]);
		tm.getTile(2, 2).setSolid(true);
		tm.getTile(3, 2).setBackground(imgMap[5][5]);
		tm.getTile(3, 2).setSolid(true);
		tm.getTile(4, 2).setBackground(imgMap[5][4]);
		tm.getTile(4, 2).setSolid(true);
		tm.getTile(1, 1).setBackground(imgMap[5][3]);
		tm.getTile(1, 1).setSolid(true);
		tm.getTile(2, 1).setBackground(imgMap[5][5]);
		tm.getTile(2, 1).setSolid(true);
		tm.getTile(3, 1).setBackground(imgMap[5][4]);
		tm.getTile(3, 1).setSolid(true);
		tm.getTile(2, 0).setBackground(imgMap[5][6]);
		tm.getTile(10, 6).setBackground(imgMap[3][7]); // Path
		tm.getTile(10, 5).setBackground(imgMap[2][7]);
		tm.getTile(10, 4).setBackground(imgMap[5][6]);
		for (int i = 11; i < 16; i++) {
			tm.getTile(i, 6).setBackground(imgMap[2][6]);
			tm.getTile(i, 5).setBackground(imgMap[0][7]);
			tm.getTile(i, 4).setBackground(imgMap[5][6]);
		}
		room.addActor(tm);
		final Door door = new Door("STORE", doors[0], doors[1]);
		door.setPosition(tm.getTile(10, 7));
		room.addActor(door);
		final Container barrel = new Container("BARREL", containers[2], null);
		barrel.setPosition(tm.getTile(6, 4));
		room.addActor(barrel);
		final Container chest = new Container("CHEST", containers[0], containers[1]);
		chest.setPosition(tm.getTile(8, 4));
		room.addActor(chest);
		final Player player = new Player("act.player");
		player.setPosition(tm.getTile(5, 5));
		room.addActor(player);
		engine.track(player);
		final Panlayer hud = createHud(room);
		hudInteract = new Pantext("hud.interact", hudFont, hudInteractText);
		hud.addActor(hudInteract);
	}
	
	public final static void main(final String[] args) {
        try {
            new RpgGame().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
