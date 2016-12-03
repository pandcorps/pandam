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

import org.pandcorps.core.*;
import org.pandcorps.furguardians.Player.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;

public final class BombScreen extends MiniGameScreen {
    private final static int DIM = 16;
    private final static int PLAYABLE_COLS = 13;
    private final static int PLAYABLE_ROWS = 11;
    private final static int TOTAL_COLS = PLAYABLE_COLS + 2; // Playing field + left border + right border
    private final static int TOTAL_ROWS = PLAYABLE_ROWS + 3; // Playing field + top border + low border + HUD
    private final static int SCREEN_H = 224;
    private static Panroom room = null;
    private static Panmage img = null;
    private static TileMap tm = null;
    private static TileMapImage[][] imgMap = null;
    
    @Override
    protected final void load() throws Exception {
        room = initMiniZoom(SCREEN_H);
        img = Pangine.getEngine().createImage(Pantil.vmid(), FurGuardiansGame.RES + "bg/Tiles.png");
        tm = new TileMap(Pantil.vmid(), TOTAL_COLS, TOTAL_ROWS, DIM, DIM);
        imgMap = tm.splitImageMap(img);
        room.addActor(tm);
        tm.getPosition().setX((Pangine.getEngine().getEffectiveWidth() - (TOTAL_COLS * DIM)) / 2);
        buildBorder();
    }
    
    @Override
    protected final void destroy() {
        Panmage.destroy(img);
    }
    
    private final void buildBorder() {
        final int yMin = 0, yMax = PLAYABLE_ROWS + 1, xMax = PLAYABLE_COLS + 1;
        final Tile tile = tm.getTile(imgMap[0][4], null, Tile.BEHAVIOR_SOLID);
        for (int i = 1; i < xMax; i++) {
            tm.setTile(i, yMin, tile);
            tm.setTile(i, yMax, tile);
        }
        for (int j = yMin; j <= yMax; j++) {
            tm.setTile(0, j, tile);
            tm.setTile(xMax, j, tile);
        }
    }
    
    protected final static class BombGuy extends Panctor {
        private final PlayerContext pc;
        
        protected BombGuy(final PlayerContext pc) {
            this.pc = pc;
            setView(pc.mapSouth);
        }
    }
}
