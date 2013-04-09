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
    Load/unload neighboring TileMaps for large areas as needed
    (and maybe offset actor positions so current TileMap always starts at origin).
    Shaded counter seems to have a pixel that's too dark.
    License comment years.
    */
    
	private static Panroom room = null;
	private static Font hudFont = null;
	/*package*/ static Pantext hudInteract = null;
	/*package*/ final static StringBuilder hudInteractText = new StringBuilder();
	private static DynamicTileMap tm = null;
	private static Panmage[] containers = null;
	/*package*/ static Player player = null;
	
	@Override
	protected void init(final Panroom room) throws Exception {
		Pangine.getEngine().setTitle("RPG");
		RpgGame.room = room;
		loadConstants();
		loadArea(new Town());
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
	
	private final static void loadConstants() {
		hudFont = Fonts.getClassic(new FontRequest(8), Pancolor.WHITE);
		containers = createSheet("container", "org/pandcorps/rpg/res/misc/Container01.png", ImtilX.DIM, Container.o);
	}
	
	/*package*/ final static void loadArea(final Area area) {
		if (player != null) {
			player.detach();
		}
		Panscreen.set(new RpgScreen(area));
	}
	
	protected final static class RpgScreen extends Panscreen {
		private final Area area;
	    
	    protected RpgScreen(Area area) {
	        this.area = area;
	    }
	    
		@Override
        protected final void load() throws Exception {
			tm = new DynamicTileMap("act.tilemap", room, ImtilX.DIM, ImtilX.DIM);
			tm.setOccupantDepth(DepthMode.Y);
			//tm.getPosition().setZ(-10);
			room.addActor(tm);
			area.start();
			createPlayer();
			createHud();
		}
	}
	
	private final static void loadTown() {
		final Panmage[] doors = createSheet("door", "org/pandcorps/rpg/res/misc/DoorQuaint.png");
		tm.setImageMap(Pangine.getEngine().createImage("img.tile.quaint", ImtilX.loadImage("org/pandcorps/rpg/res/bg/TileQuaint.png", 128, null)));
		final TileMapImage[][] imgMap = tm.splitImageMap();
		tm.setTileListener(new QuaintTileListener(imgMap));
		tm.fillBackground(imgMap[5][0]);
		tm.getTile(6, 5).setBackground(imgMap[4][1], true);
		tm.getTile(6, 6).setBackground(imgMap[4][1], true);
		tm.getTile(6, 7).setBackground(imgMap[3][1], true);
		tm.getTile(6, 8).setBackground(imgMap[2][1], true);
		tm.getTile(6, 9).setBackground(imgMap[1][1], true);
		tm.getTile(6, 10).setForeground(imgMap[0][1]);
		tm.getTile(7, 5).setBackground(imgMap[3][4], true); // Window
		tm.getTile(7, 6).setBackground(imgMap[4][2], true);
		tm.getTile(7, 7).setBackground(imgMap[3][2], true);
		tm.getTile(7, 8).setBackground(imgMap[2][2], true);
		tm.getTile(7, 9).setBackground(imgMap[1][2], true);
		tm.getTile(7, 10).setForeground(imgMap[0][2]);
		tm.getTile(8, 5).setBackground(imgMap[4][3], true);
		tm.getTile(8, 6).setBackground(imgMap[4][3], true);
		tm.getTile(8, 7).setBackground(imgMap[3][3], true);
		tm.getTile(8, 8).setBackground(imgMap[2][3], true);
		tm.getTile(8, 9).setBackground(imgMap[1][3], true);
		tm.getTile(8, 10).setForeground(imgMap[0][1]);
		for (int i = 9; i <= 13; i++) {
    		tm.getTile(i, 7).setBackground(imgMap[4][2], true);
            tm.getTile(i, 8).setBackground(imgMap[4][2], true);
            tm.getTile(i, 9).setBackground(imgMap[2][4], true);
            tm.getTile(i, 10).setForeground(imgMap[0][1]);
		}
		tm.getTile(12, 7).setBackground(imgMap[0][5]); // Sign
		tm.getTile(14, 7).setBackground(imgMap[1][6]); // Shadow
		tm.getTile(14, 8).setBackground(imgMap[0][6]);
		tm.getTile(4, 8).setBackground(imgMap[1][0], true); // Tree
		tm.getTile(4, 9).setForeground(imgMap[0][0]);
		tm.getTile(7, 3).setBackground(imgMap[2][0]); // Many flowers
		tm.getTile(9, 1).setBackground(imgMap[3][0]); // Some flowers
		tm.getTile(11, 11).setBackground(imgMap[3][0]);
		tm.getTile(12, 2).setBackground(imgMap[4][0]); // Dirt patch
		tm.getTile(2, 5).setBackground(imgMap[2][6]);
		tm.getTile(3, 5).setBackground(imgMap[2][6]);
		tm.getTile(1, 4).setBackground(imgMap[5][1], true); // Water
		tm.getTile(2, 4).setBackground(imgMap[5][5], true);
		tm.getTile(3, 4).setBackground(imgMap[5][5], true);
		tm.getTile(4, 4).setBackground(imgMap[5][2], true);
		tm.getTile(0, 3).setBackground(imgMap[4][6]);
		tm.getTile(1, 3).setBackground(imgMap[5][5], true);
		tm.getTile(2, 3).setBackground(imgMap[5][5], true);
		tm.getTile(3, 3).setBackground(imgMap[5][5], true);
		tm.getTile(4, 3).setBackground(imgMap[5][5], true);
		tm.getTile(5, 3).setBackground(imgMap[3][6]);
		tm.getTile(0, 2).setBackground(imgMap[4][6]);
		tm.getTile(1, 2).setBackground(imgMap[5][5], true);
		tm.getTile(2, 2).setBackground(imgMap[5][5], true);
		tm.getTile(3, 2).setBackground(imgMap[5][5], true);
		tm.getTile(4, 2).setBackground(imgMap[5][4], true);
		tm.getTile(1, 1).setBackground(imgMap[5][3], true);
		tm.getTile(2, 1).setBackground(imgMap[5][5], true);
		tm.getTile(3, 1).setBackground(imgMap[5][4], true);
		tm.getTile(2, 0).setBackground(imgMap[5][6]);
		tm.getTile(10, 6).setBackground(imgMap[3][7]); // Path
		tm.getTile(10, 5).setBackground(imgMap[2][7]);
		tm.getTile(10, 4).setBackground(imgMap[5][6]);
		for (int i = 11; i < 16; i++) {
			tm.getTile(i, 6).setBackground(imgMap[2][6]);
			tm.getTile(i, 5).setBackground(imgMap[0][7]);
			tm.getTile(i, 4).setBackground(imgMap[5][6]);
		}
		final Door door = new Door("STORE", doors[0], doors[1], new Store());
		door.setPosition(tm.getTile(10, 7));
		room.addActor(door);
		final Container barrel = new Container("BARREL", containers[2], null);
		barrel.setPosition(tm.getTile(6, 4));
		room.addActor(barrel);
		final Container chest = new Container("CHEST", containers[0], containers[1]);
		chest.setPosition(tm.getTile(8, 4));
		room.addActor(chest);
		final Npc npc = new Npc("act.npc");
		npc.setPosition(tm.getTile(10, 5));
		room.addActor(npc);
	}
	
	/*package*/ abstract static class Area {
		protected void start() {
	        init();
	    }
		
		protected abstract void init();
	}
	
	private final static class Town extends Area {
		@Override
		protected final void init() {
			loadTown();
		}
	}
	
	/*package*/ final static class Store extends Area {
		@Override
		protected final void init() {
			final Panmage[] counters = createSheet("counter", "org/pandcorps/rpg/res/misc/Counter01.png");
			tm.setImageMap(Pangine.getEngine().createImage("img.tile.inside", ImtilX.loadImage("org/pandcorps/rpg/res/bg/TileInside.png", 128, null)));
			final TileMapImage[][] imgMap = tm.splitImageMap();
			tm.fillBackground(imgMap[2][3]);
			tm.getTile(0, 0).setBackground(null, true);
			tm.getTile(0, 1).setBackground(imgMap[2][0], true);
			tm.getTile(0, 11).setBackground(imgMap[0][0], true);
			for (int i = 1; i < 15; i++) {
				tm.getTile(i, 0).setBackground(null, true);
				tm.getTile(i, 1).setForeground(imgMap[2][1]);
				tm.getTile(i, 11).setBackground(imgMap[0][1], true);
			}
			tm.getTile(15, 0).setBackground(null, true);
			tm.getTile(15, 1).setBackground(imgMap[2][2], true);
			tm.getTile(15, 11).setBackground(imgMap[0][2], true);
			for (int j = 2; j < 11; j++) {
				tm.getTile(0, j).setBackground(imgMap[1][0], true);
				tm.getTile(1, j).setBackground(imgMap[0][3]);
				tm.getTile(15, j).setBackground(imgMap[1][2], true);
			}
			tm.getTile(1, 2).setBackground(imgMap[1][3]);
			tm.getTile(10, 1).setForeground(imgMap[1][1]);
			tm.getTile(10, 1).setBackground(imgMap[1][3]);
			for (int i = 1; i < 9; i++) {
				final Counter c = new Counter(counters[i == 1 ? 0 : i == 8 ? 6 : 1]);
				c.setPosition(tm.getTile(i, 8));
				room.addActor(c);
			}
			final Counter c = new Counter(counters[9]);
			c.setPosition(tm.getTile(8, 9));
			room.addActor(c);
			final Container chest = new Container("CHEST", containers[0], containers[1]);
			chest.setPosition(tm.getTile(1, 9));
			room.addActor(chest);
		}
	}
	
	private final static void createPlayer() {
		if (player == null) {
			player = new Player("act.player");
		}
		player.active = true;
		room.addActor(player);
		player.setPosition(tm.getTile(5, 5));
		Pangine.getEngine().track(player);
	}
	
	private final static void createHud() {
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
