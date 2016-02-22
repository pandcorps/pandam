/*
Copyright (c) 2009-2016, Andrew M. Martin
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
import org.pandcorps.pandax.text.Fonts.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;
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
	private static TileMap tm = null;
	private static TileMapImage[][] animTiles = null;
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
        protected final void step() {
	    	area.step();
	    }
	    
		@Override
        protected final void load() throws Exception {
			animTiles = null;
			tm = new TileMap("act.tilemap", room, ImtilX.DIM, ImtilX.DIM);
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
		animTiles = new TileMapImage[5][];
		for (int x = 1; x <= 5; x++) {
			animTiles[x - 1] = new TileMapImage[] {imgMap[5][x], imgMap[6][x], imgMap[7][x]};
		}
		tm.fillBackground(imgMap[5][0]);
		tm.setBackground(6, 5, imgMap[4][1], Tile.BEHAVIOR_SOLID);
		tm.setBackground(6, 6, imgMap[4][1], Tile.BEHAVIOR_SOLID);
		tm.setBackground(6, 7, imgMap[3][1], Tile.BEHAVIOR_SOLID);
		tm.setBackground(6, 8, imgMap[2][1], Tile.BEHAVIOR_SOLID);
		tm.setBackground(6, 9, imgMap[1][1], Tile.BEHAVIOR_SOLID);
		tm.setForeground(6, 10, imgMap[0][1]);
		tm.setBackground(7, 5, imgMap[3][4], Tile.BEHAVIOR_SOLID); // Window
		tm.setBackground(7, 6, imgMap[4][2], Tile.BEHAVIOR_SOLID);
		tm.setBackground(7, 7, imgMap[3][2], Tile.BEHAVIOR_SOLID);
		tm.setBackground(7, 8, imgMap[2][2], Tile.BEHAVIOR_SOLID);
		tm.setBackground(7, 9, imgMap[1][2], Tile.BEHAVIOR_SOLID);
		tm.setForeground(7, 10, imgMap[0][2]);
		tm.setBackground(8, 5, imgMap[4][3], Tile.BEHAVIOR_SOLID);
		tm.setBackground(8, 6, imgMap[4][3], Tile.BEHAVIOR_SOLID);
		tm.setBackground(8, 7, imgMap[3][3], Tile.BEHAVIOR_SOLID);
		tm.setBackground(8, 8, imgMap[2][3], Tile.BEHAVIOR_SOLID);
		tm.setBackground(8, 9, imgMap[1][3], Tile.BEHAVIOR_SOLID);
		tm.setForeground(8, 10, imgMap[0][1]);
		for (int i = 9; i <= 13; i++) {
    		tm.setBackground(i, 7, imgMap[4][2], Tile.BEHAVIOR_SOLID);
            tm.setBackground(i, 8, imgMap[4][2], Tile.BEHAVIOR_SOLID);
            tm.setBackground(i, 9, imgMap[2][4], Tile.BEHAVIOR_SOLID);
            tm.setForeground(i, 10, imgMap[0][1]);
		}
		tm.setBackground(12, 7, imgMap[0][5]); // Sign
		tm.setBackground(14, 7, imgMap[1][6]); // Shadow
		tm.setBackground(14, 8, imgMap[0][6]);
		tm.setBackground(4, 8, imgMap[1][0], Tile.BEHAVIOR_SOLID); // Tree
		tm.setForeground(4, 9, imgMap[0][0]);
		tm.setBackground(7, 3, imgMap[2][0]); // Many flowers
		tm.setBackground(9, 1, imgMap[3][0]); // Some flowers
		tm.setBackground(11, 11, imgMap[3][0]);
		tm.setBackground(12, 2, imgMap[4][0]); // Dirt patch
		tm.setBackground(2, 5, imgMap[2][6]);
		tm.setBackground(3, 5, imgMap[2][6]);
		tm.setBackground(1, 4, imgMap[5][1], Tile.BEHAVIOR_SOLID); // Water
		tm.setBackground(2, 4, imgMap[5][5], Tile.BEHAVIOR_SOLID);
		tm.setBackground(3, 4, imgMap[5][5], Tile.BEHAVIOR_SOLID);
		tm.setBackground(4, 4, imgMap[5][2], Tile.BEHAVIOR_SOLID);
		tm.setBackground(0, 3, imgMap[4][6]);
		tm.setBackground(1, 3, imgMap[5][5], Tile.BEHAVIOR_SOLID);
		tm.setBackground(2, 3, imgMap[5][5], Tile.BEHAVIOR_SOLID);
		tm.setBackground(3, 3, imgMap[5][5], Tile.BEHAVIOR_SOLID);
		tm.setBackground(4, 3, imgMap[5][5], Tile.BEHAVIOR_SOLID);
		tm.setBackground(5, 3, imgMap[3][6]);
		tm.setBackground(0, 2, imgMap[4][6]);
		tm.setBackground(1, 2, imgMap[5][5], Tile.BEHAVIOR_SOLID);
		tm.setBackground(2, 2, imgMap[5][5], Tile.BEHAVIOR_SOLID);
		tm.setBackground(3, 2, imgMap[5][5], Tile.BEHAVIOR_SOLID);
		tm.setBackground(4, 2, imgMap[5][4], Tile.BEHAVIOR_SOLID);
		tm.setBackground(1, 1, imgMap[5][3], Tile.BEHAVIOR_SOLID);
		tm.setBackground(2, 1, imgMap[5][5], Tile.BEHAVIOR_SOLID);
		tm.setBackground(3, 1, imgMap[5][4], Tile.BEHAVIOR_SOLID);
		tm.setBackground(2, 0, imgMap[5][6]);
		tm.setBackground(10, 6, imgMap[3][7]); // Path
		tm.setBackground(10, 5, imgMap[2][7]);
		tm.setBackground(10, 4, imgMap[5][6]);
		for (int i = 11; i < 16; i++) {
			tm.setBackground(i, 6, imgMap[2][6]);
			tm.setBackground(i, 5, imgMap[0][7]);
			tm.setBackground(i, 4, imgMap[5][6]);
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
		
		protected void step() {
		}
	}
	
	private final static class Town extends Area {
		@Override
		protected final void init() {
			loadTown();
		}
		
		@Override
		protected final void step() {
			if ((Pangine.getEngine().getClock() % 8) == 0) {
				for (final TileMapImage[] a : animTiles) {
					Tile.animate(a);
				}
            }
		}
	}
	
	/*package*/ final static class Store extends Area {
		@Override
		protected final void init() {
			final Panmage[] counters = createSheet("counter", "org/pandcorps/rpg/res/misc/Counter01.png");
			tm.setImageMap(createImage("tile.inside", "org/pandcorps/rpg/res/bg/TileInside.png", 128));
			final TileMapImage[][] imgMap = tm.splitImageMap();
			tm.fillBackground(imgMap[2][3]);
			tm.setBackground(0, 0, null, Tile.BEHAVIOR_SOLID);
			tm.setBackground(0, 1, imgMap[2][0], Tile.BEHAVIOR_SOLID);
			tm.setBackground(0, 11, imgMap[0][0], Tile.BEHAVIOR_SOLID);
			for (int i = 1; i < 15; i++) {
				tm.setBackground(i, 0, null, Tile.BEHAVIOR_SOLID);
				tm.setForeground(i, 1, imgMap[2][1]);
				tm.setBackground(i, 11, imgMap[0][1], Tile.BEHAVIOR_SOLID);
			}
			tm.setBackground(15, 0, null, Tile.BEHAVIOR_SOLID);
			tm.setBackground(15, 1, imgMap[2][2], Tile.BEHAVIOR_SOLID);
			tm.setBackground(15, 11, imgMap[0][2], Tile.BEHAVIOR_SOLID);
			for (int j = 2; j < 11; j++) {
				tm.setBackground(0, j, imgMap[1][0], Tile.BEHAVIOR_SOLID);
				tm.setBackground(1, j, imgMap[0][3]);
				tm.setBackground(15, j, imgMap[1][2], Tile.BEHAVIOR_SOLID);
			}
			tm.setBackground(1, 2, imgMap[1][3]);
			tm.setForeground(10, 1, imgMap[1][1]);
			tm.setBackground(10, 1, imgMap[1][3]);
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
