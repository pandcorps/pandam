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

import org.pandcorps.game.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.TileMapImage;

public class PlatformGame extends BaseGame {
	protected final static byte TILE_BREAK = 2;
	protected final static byte TILE_BUMP = 3;
    //protected final static byte TILE_UP = 2;
	
	//protected final static int DEPTH_POWERUP = 0;
	//protected final static int DEPTH_ENEMY = 1;
	protected final static int DEPTH_PLAYER = 2;
	protected final static int DEPTH_SHATTER = 3;
	
	protected static Panroom room = null;
	protected static TileMap tm = null;
	protected static TileMapImage[][] imgMap = null;
	protected static Panmage block8 = null;
	
	@Override
	protected final void init(final Panroom room) throws Exception {
		Pangine.getEngine().setTitle("Platformer");
		PlatformGame.room = room;
		loadConstants();
		loadLevel();
	}
	
	private final static void loadConstants() {
	    block8 = createImage("block8", "org/pandcorps/platform/res/misc/Block8.png", 8);
	}
	
	private final static void loadLevel() {
		tm = new TileMap("act.tilemap", room, ImtilX.DIM, ImtilX.DIM);
		room.addActor(tm);
		tm.setImageMap(createImage("tiles", "org/pandcorps/platform/res/bg/Tiles.png", 128));
		imgMap = tm.splitImageMap();
		tm.fillBackground(imgMap[0][3]);
		for (int i = 0; i < 16; i++) {
			tm.getTile(i, 0).setForeground(imgMap[1][1], true);
		}
		tm.getTile(2, 3).setForeground(imgMap[0][2], TILE_BREAK);
		tm.getTile(3, 3).setForeground(imgMap[0][2], TILE_BREAK);
		tm.getTile(4, 3).setForeground(imgMap[0][0], TILE_BUMP);
		tm.getTile(5, 3).setForeground(imgMap[0][1], true);
		//tm.getTile(8, 1).setForeground(imgMap[7][4], TILE_UP);
		tm.getTile(9, 1).setForeground(imgMap[0][1], true);
		//tm.getTile(10, 1).setForeground(imgMap[7][3], TILE_DOWN);
		final Player player = new Player();
		room.addActor(player);
		setPosition(player, 16, 16, DEPTH_PLAYER);
	}
	
	protected static void setPosition(final Panctor act, final float x, final float y, final float depth) {
	    act.getPosition().set(x, y, tm.getForegroundDepth() + depth);
	}
	
	public final static void main(final String[] args) {
        try {
            new PlatformGame().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
