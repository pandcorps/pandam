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

import org.pandcorps.core.img.*;
import org.pandcorps.game.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Fonts.FontRequest;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.TileMapImage;
import org.pandcorps.rpg.Character.*;

public class RpgGame extends BaseGame {
    /*
    Load/unload neighboring TileMaps for large areas as needed
    (and maybe offset actor positions so current TileMap always starts at origin).
    Shaded counter seems to have a pixel that's too dark (try removing floor bg behind it).
    Load resources in separate Area method that tags resources with age.
    After loading new resources, prune old ones (maybe in separate Thread).
    Load characters separately.
    License comment years.
    */
    
	private static Panroom room = null;
	private static Panmage empty = null;
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
		loadArea(new Town(), 5, 5);
	}
	
	private final static void loadConstants() {
		empty = Pangine.getEngine().createEmptyImage("img.empty", null, null, null);
		hudFont = Fonts.getClassic(new FontRequest(8), Pancolor.WHITE);
		containers = createSheet("container", "org/pandcorps/rpg/res/misc/Container01.png", ImtilX.DIM, Container.o);
	}
	
	/*package*/ final static void loadArea(final Area area, final int i, final int j) {
		if (player != null) {
			player.detach();
		}
		Panscreen.set(new RpgScreen(area, i, j));
	}
	
	protected final static class RpgScreen extends Panscreen {
		private final Area area;
		private final int i;
		private final int j;
	    
	    protected RpgScreen(final Area area, final int i, final int j) {
	        this.area = area;
	        this.i = i;
	        this.j = j;
	    }
	    
		@Override
        protected final void load() throws Exception {
			tm = new DynamicTileMap("act.tilemap", room, ImtilX.DIM, ImtilX.DIM);
			tm.setOccupantDepth(DepthMode.Y);
			//tm.getPosition().setZ(-10);
			room.addActor(tm);
			area.start();
			createPlayer(i, j);
			createHud();
		}
	}
	
	private final static void loadTown() {
		final Panmage[] doors = createSheet("door", "org/pandcorps/rpg/res/misc/DoorQuaint.png");
		tm.setImageMap(createImage("tile.quaint", "org/pandcorps/rpg/res/bg/TileQuaint.png", 128));
		final TileMapImage[][] imgMap = tm.splitImageMap();
		final MapTileListener mtl = new MapTileListener(8);
		for (int x = 1; x <= 5; x++) {
			for (int y = 0; y <= 2; y++) {
				mtl.put(imgMap[5 + y][x], imgMap[5 + ((y + 1) % 3)][x]);
			}
		}
		tm.setTileListener(mtl);
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
		new Door("STORE", doors[0], doors[1], new Store(), 10, 1).init(tm, 10, 7);
		new Container("BARREL", containers[2], null).init(tm, 6, 4);
		new Container("CHEST", containers[0], containers[1]).init(tm, 8, 4);
		if (player == null || player.getPosition().getX() < 100) {
			final CharacterLayer face = new CharacterLayer(0, 180, 130, 90, 200, 150, 110, 220, 170, 130, 240, 190, 150);
			final CharacterLayer hair = new CharacterLayer(0, 128, 64, 0, 160, 80, 0, 192, 96, 0, 224, 112, 0);
			final CharacterLayer legs = new CharacterLayer(0, 128, 0, 0, 160, 0, 0, 192, 0, 0, 224, 0, 0);
			final CharacterLayer feet = new CharacterLayer(0, 96, 24, 0, 128, 32, 0, 160, 40, 0, 192, 48, 0);
			final CharacterLayer torso = new CharacterLayer(1, 32, 32, 24, 48, 48, 40, 64, 64, 56, 80, 80, 72);
			final CharacterDefinition def = new CharacterDefinition(face, 0, hair, legs, feet, torso);
			new Npc("act.npc", def, Guy4Controller.RANDOM).init(tm, 10, 5);
		}
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
			tm.setImageMap(createImage("tile.inside", "org/pandcorps/rpg/res/bg/TileInside.png", 128));
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
				new Counter(counters[i == 1 ? 0 : i == 8 ? 6 : 1]).init(tm, i, 8);
			}
			new Counter(counters[9]).init(tm, 8, 9);
			new Container("CHEST", containers[0], containers[1]).init(tm, 1, 9);
			new Door("OUTSIDE", empty, null, new Town(), 10, 6).init(tm, 10, 0);
		}
	}
	
	private final static void createPlayer(final int i, final int j) {
		if (player == null) {
			final CharacterLayer face = new CharacterLayer(1, 200, 152, 112, 216, 168, 128, 232, 184, 144, 248, 200, 160);
			final CharacterLayer hair = new CharacterLayer(1, 0, 0, 128, 0, 0, 160, 00, 0, 192, 0, 0, 224);
			final CharacterLayer legs = new CharacterLayer(0, 0, 128, 128, 0, 160, 160, 0, 192, 192, 0, 224, 224);
			final CharacterLayer feet = new CharacterLayer(0, 72, 72, 72, 104, 104, 104, 136, 136, 136, 168, 168, 168);
			final CharacterLayer torso = new CharacterLayer(0, 128, 128, 128, 160, 160, 160, 192, 192, 192, 224, 224, 224);
			final CharacterDefinition def = new CharacterDefinition(face, 4, hair, legs, feet, torso);
			player = new Player("act.player", def);
		}
		player.active = true;
		player.init(tm, i, j);
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
