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

import org.pandcorps.game.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.TileMapImage;

public class RpgGame extends BaseGame {
    /*
    TileMap support for animated tiles.
    Character sprite generator.
    Doors that open and transport player (but not NPCs).
    Load/unload neighboring TileMaps for large areas as needed
    (and maybe offset actor positions so current TileMap always starts at origin).
    */
    
	private static Panroom room = null;
	
	@Override
	protected void init(final Panroom room) throws Exception {
		Pangine.getEngine().setTitle("RPG");
		RpgGame.room = room;
		loadBackground();
	}
	
	private final static void loadBackground() {
		final Pangine engine = Pangine.getEngine();
		final TileMap tm = new TileMap("act.tilemap", room, ImtilX.DIM, ImtilX.DIM);
		//tm.getPosition().setZ(-100);
		tm.setOccupantDepth(DepthMode.Y);
		tm.setImageMap(engine.createImage("img.tile.quaint", ImtilX.loadImage("org/pandcorps/rpg/res/bg/TileQuaint.png", 128, null)));
		final TileMapImage[][] imgMap = tm.splitImageMap();
		tm.fillBackground(imgMap[5][0]);
		tm.getTile(6, 6).setBackground(imgMap[3][1]);
		tm.getTile(6, 6).setSolid(true);
		tm.getTile(6, 7).setBackground(imgMap[3][1]);
		tm.getTile(6, 7).setSolid(true);
		tm.getTile(6, 8).setBackground(imgMap[2][1]);
		tm.getTile(6, 8).setSolid(true);
		tm.getTile(6, 9).setBackground(imgMap[1][1]);
		tm.getTile(6, 9).setSolid(true);
		tm.getTile(6, 10).setForeground(imgMap[0][1]);
		tm.getTile(7, 6).setBackground(imgMap[3][4]); // Sign
		tm.getTile(7, 6).setSolid(true);
		tm.getTile(7, 7).setBackground(imgMap[3][2]);
		tm.getTile(7, 7).setSolid(true);
		tm.getTile(7, 8).setBackground(imgMap[2][2]);
		tm.getTile(7, 8).setSolid(true);
		tm.getTile(7, 9).setBackground(imgMap[1][2]);
		tm.getTile(7, 9).setSolid(true);
		tm.getTile(7, 10).setForeground(imgMap[0][2]);
		tm.getTile(8, 6).setBackground(imgMap[3][3]);
		tm.getTile(8, 6).setSolid(true);
		tm.getTile(8, 7).setBackground(imgMap[3][3]);
		tm.getTile(8, 7).setSolid(true);
		tm.getTile(8, 8).setBackground(imgMap[2][3]);
		tm.getTile(8, 8).setSolid(true);
		tm.getTile(8, 9).setBackground(imgMap[1][3]);
		tm.getTile(8, 9).setSolid(true);
		tm.getTile(8, 10).setForeground(imgMap[0][1]);
		for (int i = 9; i <= 11; i++) {
    		tm.getTile(i, 7).setBackground(imgMap[3][2]);
            tm.getTile(i, 7).setSolid(true);
            tm.getTile(i, 8).setBackground(imgMap[3][2]);
            tm.getTile(i, 8).setSolid(true);
            tm.getTile(i, 9).setBackground(imgMap[2][4]);
            tm.getTile(i, 9).setSolid(true);
            tm.getTile(i, 10).setForeground(imgMap[0][1]);
		}
		tm.getTile(10, 7).setBackground(imgMap[0][5]); // Window
		tm.getTile(4, 8).setBackground(imgMap[1][0]); // Tree
		tm.getTile(4, 8).setSolid(true);
		tm.getTile(4, 9).setForeground(imgMap[0][0]);
		tm.getTile(7, 4).setBackground(imgMap[2][0]); // Many flowers
		tm.getTile(9, 2).setBackground(imgMap[3][0]); // Some flowers
		tm.getTile(11, 11).setBackground(imgMap[3][0]);
		tm.getTile(12, 3).setBackground(imgMap[4][0]); // Dirt patch
		room.addActor(tm);
		final Player player = new Player("act.player");
		player.setPosition(tm.getTile(5, 5));
		room.addActor(player);
		engine.track(player);
	}
	
	public final static void main(final String[] args) {
        try {
            new RpgGame().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
