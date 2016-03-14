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
package org.pandcorps.furguardians;

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.furguardians.FurGuardiansGame.*;
import org.pandcorps.furguardians.Player.*;

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
            room = FurGuardiansGame.createRoom(256, 192);
            room.center();
            
            tm = newTileMap();
            Level.tm = tm;
            final Img buf = ImtilX.loadImage("org/pandcorps/furguardians/res/bg/" + imgName + ".png", 128, null);
            timg = engine.createImage("img.castle", buf);
            imgMap = tm.splitImageMap(timg);
            tm.setForegroundDepth(1);
            room.addActor(tm);
            
            Menu.PlayerScreen.registerBackPromptQuit(tm);
            draw();
            FurGuardiansGame.fadeIn(room);
            getMusic().changeMusic();
        }
        
        protected Pansound getMusic() {
        	return FurGuardiansGame.musicHeartbeat;
        }
        
        protected TileMap newTileMap() {
        	return new TileMap(Pantil.vmid(), room, ImtilX.DIM, ImtilX.DIM);
        }
        
        protected abstract void draw() throws Exception;
        
        @Override
        protected final void destroy() {
            Level.tm = null;
            timg.destroy();
            onDestroy();
        }
        
        protected void onDestroy() {
        }
    }
    
    protected final static class ThroneIntroScreen extends ThroneScreen {
    	protected ThroneIntroScreen() {
    		super(Arrays.asList(
	            "A portal has appeared outside",
	            "the castle. Monsters from the",
	            "Realm of Chaos have invaded",
	            "the kingdom! Please close the",
	            "gateway from the other side."));
    	}
    	
    	@Override
    	protected final Panscreen getNextScreen() {
    		return new PortalScreen();
    	}
    }
    
    protected final static class ThroneWinScreen extends ThroneScreen {
        protected ThroneWinScreen() {
            super(Arrays.asList(
                "You destroyed the Havoc Stone,",
                "sending everything that passed",
                "through the Chaos Gate back to",
                "the original side! I am ",
                "forever in your debt."));
        }
        
        @Override
        protected final Pansound getMusic() {
        	return FurGuardiansGame.musicChant;
        }
        
        @Override
        protected final Panscreen getNextScreen() {
            return new Map.MapScreen();
        }
    }
    
    private abstract static class ThroneScreen extends CastleScreen {
    	private final List<String> msg;
    	private Panimation royAnm = null;
    	private Panmage royImg = null;
    	
        protected ThroneScreen(final List<String> msg) {
            super("ThroneRoom");
            this.msg = msg;
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
            tm.setBackground(10, 2, imgMap[2][7]);
            for (int i = 11; i < 15; i += 2) {
                tm.rectangleBackground(6, 2, i, 2, 2, 1);
            }
            tm.setBackground(15, 2, imgMap[2][6]);
            tm.fillBackground(imgMap[1][6], 10, 3, 6, 1);
            tm.rectangleBackground(0, 7, 12, 3, 2, 4);
            
            addPlayers(48, 32, null);
            
            int offY = 0;
            if (Map.theme == Map.MapTheme.Hive) {
                royAnm = FurGuardiansGame.getBirdAnm("roy", FurGuardiansGame.KIND_BEE, Map.royAvt.eye);
                offY = Player.OFF_BIRD;
                final Panctor crown = new Panctor();
                crown.setView(FurGuardiansGame.getCrown(Map.royCrown));
                room.addActor(crown);
                crown.setMirror(true);
                crown.getPosition().set(187, 102, 3);
            } else if (Map.theme == Map.MapTheme.Jungle) {
                royImg = Pangine.getEngine().createImage(FurGuardiansGame.PRE_IMG + "roy.tiles", Level.loadTileImage(Level.Theme.Jungle));
                final int royW = 7;
                //TODO JUNGLE crown
                final TileMap rm = new TileMap(Pantil.vmid(), royW, 3, ImtilX.DIM, ImtilX.DIM);
                final TileMapImage[][] royMap = rm.splitImageMap(royImg);
                rm.getPosition().set(256 - (royW * ImtilX.DIM), 45, 20);
                rm.setForegroundDepth(21);
                rm.setForeground(1, 2, royMap[5][2]);
                rm.setForeground(2, 2, royMap[6][0]);
                rm.setForeground(2, 1, royMap[6][2]);
                rm.setForeground(2, 0, royMap[5][0]);
                rm.setForeground(1, 0, royMap[7][2]);
                rm.setForeground(0, 0, royMap[5][1]);
                rm.setForeground(0, 1, royMap[6][1]);
                rm.setForeground(1, 1, royMap[7][1]);
                rm.setForeground(3, 1, royMap[6][0]);
                rm.setForeground(3, 0, royMap[5][1]);
                rm.setForeground(4, 0, royMap[7][1]);
                rm.setForeground(5, 0, royMap[7][1]);
                rm.setForeground(6, 0, royMap[7][1]);
                room.addActor(rm);
            } else {
                final PlayerImages pi = new PlayerImages(Map.royAvt);
                final Img k1 = pi.guys[0], k2 = pi.guyBlink;
                final Img crownImg = FurGuardiansGame.crowns[Map.royCrown];
                for (final Img k : new Img[] {k1, k2}) {
                    Imtil.copy(crownImg, k, 0, 0, 14, 9, 10, 1, Imtil.COPY_FOREGROUND);
                }
                final Pangine en = Pangine.getEngine();
                royAnm = en.createAnimation(FurGuardiansGame.PRE_ANM + "roy",
                	en.createFrame(FurGuardiansGame.PRE_FRM + "roy.1", en.createImage(FurGuardiansGame.PRE_IMG + "roy.1", k1), FurGuardiansGame.DUR_BLINK + 20),
                	en.createFrame(FurGuardiansGame.PRE_FRM + "roy.2", en.createImage(FurGuardiansGame.PRE_IMG + "roy.2", k2), FurGuardiansGame.DUR_CLOSED));
                pi.close();
            }
            if (royAnm != null) {
                final Panctor roy = new Panctor();
                roy.setView(royAnm);
                room.addActor(roy);
                roy.setMirror(true);
                roy.getPosition().set(184, 60 + offY, 2);
            }
            
            final Pantext text = new Pantext(Pantil.vmid(), FurGuardiansGame.font, msg);
            text.getPosition().set(8, 160, 10);
            room.addActor(text);
            
            tm.register(new ActionStartListener() {
				@Override public final void onActionStart(final ActionStartEvent event) {
					FurGuardiansGame.fadeOut(room, getNextScreen());
				}});
        }
        
        protected abstract Panscreen getNextScreen();
        
        @Override
        protected final void onDestroy() {
        	Panmage.destroyAll(royAnm);
        	Panmage.destroy(royImg);
        }
    }
    
    private final static void pillar(final int i) {
        tm.setBackground(i, 2, imgMap[3][2]);
        tm.setBackground(i, 3, imgMap[2][2]);
        tm.fillBackground(imgMap[1][2], i, 4, 1, 6);
        tm.setBackground(i, 10, imgMap[0][2]);
        
        tm.fillBackground(imgMap[3][0], i - 1, 3, 1, 7);
        tm.setBackground(i - 1, 10, imgMap[2][0]);
    }
    
    private final static void window(final int i) {
        tm.setBackground(i, 6, imgMap[2][1]);
        tm.fillBackground(imgMap[1][1], i, 7, 1, 2);
        tm.setBackground(i, 9, imgMap[0][1]);
    }
    
    private static int playerCount = 0;
    
    private final static class PortalAi implements Ai {
		@Override
		public final void onStep(final Player player) {
			final Panple pos = player.getPosition();
			if (pos.getX() >= 232) {
				new Spark(224, pos.getY() + 8, false);
				FurGuardiansGame.soundWhoosh.startSound();
				playerCount--;
				if (playerCount <= 0) {
					FurGuardiansGame.fadeOut(room, new FurGuardiansGame.PlatformScreen());
				}
				player.destroy();
			}
			player.hv = 2;
		}

        @Override
        public final FlyerAi getFlyerAi() {
            return new FlyerAi() {
                @Override public final void onStep(final Flyer flyer) {
                    final Panple pos = flyer.getPosition(), ppos = flyer.player.getPosition();
                    pos.set(ppos.getX(), ppos.getY() + 34, ppos.getZ());
                }};
        }
    }
    
    protected final static class PortalScreen extends CastleScreen {
    	private TileMapImage[] portals = null;
    	
        protected PortalScreen() {
            super("CastleExterior");
        }
        
        @Override
        protected TileMap newTileMap() {
        	return new TileMap(Pantil.vmid(), room, ImtilX.DIM, ImtilX.DIM);
        }
        
        @Override
        protected final void step() {
        	if ((Pangine.getEngine().getClock() % 5) == 0) {
        		Tile.animate(portals);
        	}
        }
        
        @Override
        protected final void draw() throws Exception {
        	portals = new TileMapImage[] {imgMap[4][0], imgMap[4][1], imgMap[4][2]};
        	
        	final TileMapImage ground = imgMap[Map.theme.portalGroundRow][Map.theme.portalGroundColumn];
            tm.fillBackground(ground, 0, 0, 16, 1, true);
            
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
            
            final TileMap tm2 = new TileMap(Pantil.vmid(), 2, 9, ImtilX.DIM, ImtilX.DIM);
            tm2.setImageMap(tm);
            room.addActor(tm2);
            tm2.getPosition().set(224, 0, 20);
            tm2.setForegroundDepth(21);
            
            tm2.fillBackground(ground, 0, 0, 2, 1, true);
            tm2.setForeground(6, 3, 1, 1);
            tm2.fillBackground(imgMap[4][0], 0, 1, 2, 7);
            tm2.rectangleForeground(6, 1, 1, 7, 1, 2);
            tm2.setForeground(7, 2, 0, 8);
            
            addPlayers(64, 16, new PortalAi());
        }
    }
    
    private final static void addPlayers(final int x, final int y, final Ai ai) {
        playerCount = FurGuardiansGame.pcs.size();
        for (int i = 0; i < playerCount; i++) {
            final PlayerContext pc = FurGuardiansGame.pcs.get(i);
            final Player oldPlayer = pc.player, player = new Player(pc, x + (24 * i), y);
            player.loadState(oldPlayer);
			player.mode = Player.MODE_DISABLED;
			// Use FurGuardiansGame.setPosition; otherwise wings can appear in front of Player
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
        tm.setBackground(i, j, imgMap[imY][imX]);
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
