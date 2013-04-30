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

import org.pandcorps.core.Pantil;
import org.pandcorps.game.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.FinPanple;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.TileMapImage;

public class PlatformGame extends BaseGame {
	protected final static byte TILE_BREAK = 2;
	protected final static byte TILE_BUMP = 3;
	protected final static byte TILE_FLOOR = 4;
    //protected final static byte TILE_UPSLOPE = 2;
	
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
		Panscreen.set(new LogoScreen(PlatformScreen.class));
	}
	
	protected final static class PlatformScreen extends Panscreen {
		@Override
        protected final void load() throws Exception {
			loadLevel();
		}
	}
	
	private final static void loadConstants() {
	    block8 = createImage("block8", "org/pandcorps/platform/res/misc/Block8.png", 8);
	}
	
	private final static void loadLevel() {
		final Pangine engine = Pangine.getEngine();
		room.destroy();
		room = engine.createRoom(Pantil.vmid(), new FinPanple(512, 192, 0));
		Pangame.getGame().setCurrentRoom(room);
		tm = new TileMap("act.tilemap", room, ImtilX.DIM, ImtilX.DIM);
		room.addActor(tm);
		final Panmage timg = createImage("tiles", "org/pandcorps/platform/res/bg/Tiles.png", 128);
		tm.setImageMap(timg);
		imgMap = tm.splitImageMap();
		
		final Panlayer bg = engine.createLayer(Pantil.vmid(), 384, 192, 1, room);
		room.addBeneath(bg);
		bg.setMaster(room);
		final TileMap bgtm = new TileMap("act.bgmap", bg, ImtilX.DIM, ImtilX.DIM);
		bg.addActor(bgtm);
		//bgtm.setImageMap(timg);
		bgtm.setImageMap(createImage("bg", "org/pandcorps/platform/res/bg/Hills.png", 128));
		final TileMapImage[][] bgMap = bgtm.splitImageMap();
		bgtm.fillBackground(bgMap[0][3]);
		bgtm.fillBackground(bgMap[1][3], 7, 1);
		bgtm.fillBackground(bgMap[2][3], 0, 7);
		cloud(bgtm, bgMap, 10, 10, 7);
		hill(bgtm, bgMap, 13, 10, 5, 4);
		hill(bgtm, bgMap, 3, 8, 12, 2);
		hill(bgtm, bgMap, 1, 4, 8, 0);
		hill(bgtm, bgMap, 15, 5, 6, 0);
		
		tm.fillBackground(imgMap[7][7]); //TODO Don't require transparent image
		for (int i = 0; i < 32; i++) {
			tm.getTile(i, 0).setForeground(imgMap[1][1], true);
		}
		tm.getTile(12, 0).setForeground(imgMap[3][0], true);
		tm.getTile(12, 1).setForeground(imgMap[2][0], true);
		tm.getTile(12, 2).setForeground(imgMap[1][0], true);
		tm.getTile(13, 2).setForeground(imgMap[1][1], true);
		tm.getTile(13, 1).setForeground(imgMap[2][1], true);
		tm.getTile(13, 0).setForeground(imgMap[2][1], true);
		tm.getTile(14, 2).setForeground(imgMap[1][2], true);
		tm.getTile(14, 1).setForeground(imgMap[2][2], true);
		tm.getTile(14, 0).setForeground(imgMap[3][2], true);
		rise(19, 1, 5, 3);
		rise(18, 1, 1, 1);
		tm.getTile(2, 3).setForeground(imgMap[0][2], TILE_BREAK);
		tm.getTile(3, 3).setForeground(imgMap[0][2], TILE_BREAK);
		tm.getTile(4, 3).setForeground(imgMap[0][0], TILE_BUMP);
		tm.getTile(5, 3).setForeground(imgMap[0][0], TILE_BUMP);
		tm.getTile(6, 3).setForeground(imgMap[0][1], true);
		//tm.getTile(8, 1).setForeground(imgMap[7][4], TILE_UP);
		tm.getTile(9, 1).setForeground(imgMap[0][1], true);
		//tm.getTile(10, 1).setForeground(imgMap[7][3], TILE_DOWN);
		final Player player = new Player();
		room.addActor(player);
		Pangine.getEngine().track(player);
		setPosition(player, 16, 16, DEPTH_PLAYER);
	}
	
	protected static void setPosition(final Panctor act, final float x, final float y, final float depth) {
	    act.getPosition().set(x, y, tm.getForegroundDepth() + depth);
	}
	
	private static void setBg(final TileMap tm, final int i, final int j, final TileMapImage[][] imgMap, final int iy, final int ix) {
	    final Tile t = tm.getTile(i, j);
	    t.setBackground(imgMap[iy][ix]);
	    t.setForeground(null, false);
	}
	
	private static void hill(final TileMap tm, final TileMapImage[][] imgMap, final int x, final int y, final int w, final int iy) {
		for (int j = 0; j < y; j++) {
			setBg(tm, x, j, imgMap, iy + 1, 0);
			setBg(tm, x + w + 1, j, imgMap, iy + 1, 2);
		}
		final int stop = x + w;
		for (int i = x + 1; i <= stop; i++) {
		    setBg(tm, i, y, imgMap, iy, 1);
			for (int j = 0; j < y; j++) {
			    setBg(tm, i, j, imgMap, iy + 1, 1);
			}
		}
		tm.getTile(x, y).setForeground(imgMap[iy][0]);
		tm.getTile(stop + 1, y).setForeground(imgMap[iy][2]);
	}
	
	private static void cloud(final TileMap tm, final TileMapImage[][] imgMap, final int x, final int y, final int w) {
        final int stop = x + w;
        for (int i = x + 1; i <= stop; i++) {
            tm.getTile(i, y).setBackground(imgMap[7][1]);
            tm.getTile(i, y + 1).setBackground(imgMap[6][1]);
        }
        tm.getTile(x, y).setForeground(imgMap[7][0]);
        tm.getTile(x, y + 1).setForeground(imgMap[6][0]);
        tm.getTile(stop + 1, y).setForeground(imgMap[7][2]);
        tm.getTile(stop + 1, y + 1).setForeground(imgMap[6][2]);
    }
	
	private static void rise(final int x, final int y, final int w, final int h) {
		final int ystop = y + h;
		for (int j = y; j < ystop; j++) {
			tm.getTile(x, j).setBackground(imgMap[2][3]);
			tm.getTile(x + w + 1, j).setBackground(imgMap[2][4]);
		}
		final int stop = x + w;
		for (int i = x + 1; i <= stop; i++) {
			tm.getTile(i, ystop).setBackground(imgMap[1][1], TILE_FLOOR);
			for (int j = y; j < ystop; j++) {
				tm.getTile(i, j).setBackground(imgMap[2][1]);
			}
		}
		tm.getTile(x, ystop).setForeground(imgMap[1][3], TILE_FLOOR);
		tm.getTile(stop + 1, ystop).setForeground(imgMap[1][4], TILE_FLOOR);
	}
	
	public final static void main(final String[] args) {
        try {
            new PlatformGame().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
