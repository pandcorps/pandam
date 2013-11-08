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
import org.pandcorps.game.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.platform.Player.*;

public class Castle {
    private static Panroom room = null;
    private static TileMap tm = null;
    private static Panmage timg = null;
    private static TileMapImage[][] imgMap = null;
    
    protected abstract static class CastleScreen extends Panscreen {
        private final String imgName;
        
        protected CastleScreen(final String imgName) {
            this.imgName = imgName;
        }
        
        @Override
        protected final void load() throws Exception {
            final Pangine engine = Pangine.getEngine();
            engine.setBgColor(Pancolor.BLACK);
            room = PlatformGame.createRoom(256, 192);
            room.center();
            
            tm = newTileMap();
            Level.tm = tm;
            final BufferedImage buf = ImtilX.loadImage("org/pandcorps/platform/res/bg/" + imgName + ".png", 128, null);
            timg = engine.createImage("img.castle", buf);
            imgMap = tm.splitImageMap(timg);
            tm.setForegroundDepth(1);
            room.addActor(tm);
            
            draw();
            PlatformGame.fadeIn(room);
        }
        
        protected TileMap newTileMap() {
        	return new TileMap(Pantil.vmid(), room, ImtilX.DIM, ImtilX.DIM);
        }
        
        protected abstract void draw() throws Exception;
        
        @Override
        protected final void destroy() {
            Level.tm = null;
            timg.destroy();
        }
    }
    
    protected final static class ThroneScreen extends CastleScreen {
        protected ThroneScreen() {
            super("ThroneRoom");
        }
        
        @Override
        protected final void draw() throws Exception {
            tm.fillBackground(imgMap[0][4], 0, 0, 16, 1);
            for (int i = 0; i < 16; i += 2) {
                tm.rectangleBackground(3, 2, i, 1, 2, 2);
            }
            tm.fillBackground(0, 1, 16, 1, true);
            tm.fillBackground(imgMap[1][0], 0, 3, 16, 7);
            tm.fillBackground(imgMap[3][1], 0, 10, 16, 1);
            tm.fillBackground(imgMap[0][3], 0, 11, 16, 1);
            
            pillar(1);
            window(3);
            pillar(5);
            window(8);
            window(11);
            pillar(14);
            
            tm.rectangleBackground(5, 2, 8, 2, 2, 2);
            tm.initTile(10, 2).setBackground(imgMap[2][7]);
            for (int i = 11; i < 15; i += 2) {
                tm.rectangleBackground(6, 2, i, 2, 2, 1);
            }
            tm.initTile(15, 2).setBackground(imgMap[2][6]);
            tm.fillBackground(imgMap[1][6], 10, 3, 6, 1);
            tm.rectangleBackground(0, 7, 12, 3, 2, 4);
            
            addPlayers(48, 32, null);
            
            tm.register(new ActionStartListener() {
				@Override public final void onActionStart(final ActionStartEvent event) {
					PlatformGame.fadeOut(room, new PortalScreen());
				}});
        }
    }
    
    private final static void pillar(final int i) {
        tm.initTile(i, 2).setBackground(imgMap[3][2]);
        tm.initTile(i, 3).setBackground(imgMap[2][2]);
        tm.fillBackground(imgMap[1][2], i, 4, 1, 6);
        tm.initTile(i, 10).setBackground(imgMap[0][2]);
        
        tm.fillBackground(imgMap[3][0], i - 1, 3, 1, 7);
        tm.initTile(i - 1, 10).setBackground(imgMap[2][0]);
    }
    
    private final static void window(final int i) {
        tm.initTile(i, 6).setBackground(imgMap[2][1]);
        tm.fillBackground(imgMap[1][1], i, 7, 1, 2);
        tm.initTile(i, 9).setBackground(imgMap[0][1]);
    }
    
    private static int playerCount = 0;
    
    private final static class PortalAi implements Ai {
		@Override
		public final void onStep(final Player player) {
			final Panple pos = player.getPosition();
			if (pos.getX() >= 232) {
				new Spark(224, pos.getY() + 8, false);
				playerCount--;
				if (playerCount <= 0) {
					PlatformGame.fadeOut(room, new PlatformGame.PlatformScreen());
				}
				player.destroy();
			}
			player.hv = 2;
		}
    }
    
    protected final static class PortalScreen extends CastleScreen {
        protected PortalScreen() {
            super("CastleExterior");
        }
        
        @Override
        protected TileMap newTileMap() {
        	return new DynamicTileMap(Pantil.vmid(), room, ImtilX.DIM, ImtilX.DIM);
        }
        
        @Override
        protected final void draw() throws Exception {
        	final MapTileListener mtl = new MapTileListener(5);
        	for (int i = 0; i < 3; i++) {
        		mtl.put(imgMap[4][i], imgMap[4][(i + 1) % 3]);
        	}
        	((DynamicTileMap) tm).setTileListener(mtl);
        	
            tm.fillBackground(imgMap[3][2], 0, 0, 16, 1, true);
            
            tm.rectangleBackground(2, 2, 1, 1, 2, 3);
            
            block(0, 1, 3, 5);
            brick(3, 1, 5);
            block(5, 1, 2, 5);
            blockEdge(0, 2);
            block(3, 2);
            brickEdge(4, 2);
            block(0, 3, 3, 4);
            brick(3, 3);
            blockEdge(5, 3);
            carvedLine(4);
            
            windowHalf(0, 1);
            for (int j = 6; j < 10; j += 2) {
                block(1, j);
                block(2, j);
                brick(1, j + 1);
            }
            windowHalf(3, 0);
            windowHalf(4, 1);
            tm.fillBackground(imgMap[3][3], 5, 6, 1, 4);
            
            carvedLine(10);
            
            int y = 1;
            for (int i = 5; i < 8; i++) {
                for (int j = (i == 5) ? 6 : 7; j > 3; j--) {
                    tm.fillBackground(imgMap[j][i], 6, y++, 10, 1);
                }
            }
            
            tm.rectangleForeground(4, 3, 12, 1, 2, 1);
            tm.fillBackground(imgMap[2][4], 12, 2, 1, 5);
            tm.fillBackground(imgMap[4][0], 13, 1, 1, 7);
            tm.rectangleForeground(4, 1, 12, 7, 2, 2);
            
            final TileMap tm2 = new DynamicTileMap(Pantil.vmid(), 2, 9, ImtilX.DIM, ImtilX.DIM);
            tm2.setImageMap(tm);
            room.addActor(tm2);
            tm2.getPosition().set(224, 0, 3);
            tm2.setForegroundDepth(4);
            
            tm2.fillBackground(imgMap[3][2], 0, 0, 2, 1, true);
            tm2.setForeground(6, 3, 1, 1);
            tm2.fillBackground(imgMap[4][0], 0, 1, 2, 7);
            tm2.rectangleForeground(6, 1, 1, 7, 1, 2);
            tm2.setForeground(7, 2, 0, 8);
            
            addPlayers(64, 16, new PortalAi());
        }
    }
    
    private final static void addPlayers(final int x, final int y, final Ai ai) {
        playerCount = PlatformGame.pcs.size();
        for (int i = 0; i < playerCount; i++) {
            final Player player = new Player(PlatformGame.pcs.get(i));
			player.mode = Player.MODE_DISABLED;
			room.addActor(player);
			player.getPosition().set(x + (24 * i), y, 2);
			player.ai = ai;
        }
    }
    
    private final static void brick(final int i, final int j) {
        brick(i, j, 1);
    }
    
    private final static void brick(final int i, final int j, final int imY) {
        tm.rectangleBackground(0, imY, i, j, 2, 1);
    }
    
    private final static void brickEdge(final int i, final int j) {
        block(i, j, 0, 1);
        block(i + 1, j, 3, 4);
    }
    
    private final static void block(final int i, final int j) {
        block(i, j, 1, 0);
    }
    
    private final static void blockEdge(final int i, final int j) {
        block(i, j, 3, 3);
    }
    
    private final static void block(final int i, final int j, final int imX, final int imY) {
        tm.initTile(i, j).setBackground(imgMap[imY][imX]);
    }
    
    private final static void windowHalf(final int i, final int imX) {
        block(i, 5, imX, 6);
        tm.fillBackground(imgMap[3][imX], i, 6, 1, 3);
        block(i, 9, imX, 2);
    }
    
    private final static void carvedLine(final int j) {
        brick(0, j, 7);
        brick(2, j, 7);
        block(4, j, 0, 7);
        block(5, j, 2, 7);
        
        final int j1 = j + 1;
        block(0, j1, 1, 1);
        brick(1, j1);
        brick(3, j1);
        blockEdge(5, j1);
    }
}
