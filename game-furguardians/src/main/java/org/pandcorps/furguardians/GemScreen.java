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
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;

public final class GemScreen extends MiniGameScreen {
    private final static int DIM = 16;
    private final static int SIZE = 6;
    private final static int NUM_COLORS = 4;
    private static Panmage gemTiles = null;
    private static Panmage gemTiles2 = null;
    private static Panmage gemTiles3 = null;
    private static Panroom room = null;
    private static TileMap tm = null;
    private static TileMapImage[][] imgMap = null;
    
    /*
    TODO
    Profile setting for last mini-game played; Menu should default to that.
    Statistics for biggest gem block, most tiles cleared in one move, Gem-games played.
    */
    
    @Override
    protected final void load() {
        room = initMiniZoom(96);
        addCursor(room, 20);
        initImages();
        initGrid();
    }
    
    @Override
    protected final void step() {
        if (tm == null) {
            return;
        }
        final long i = Pangine.getEngine().getClock() % FurGuardiansGame.TIME_FLASH;
        if (i > 1) {
            tm.setImageMap(gemTiles);
        } else if (i > 0) {
            tm.setImageMap(gemTiles3);
        } else {
            tm.setImageMap(gemTiles2);
        }
    }
    
    private final static void initImages() {
        if (gemTiles != null) {
            return;
        }
        final Pangine engine = Pangine.getEngine();
        gemTiles = engine.createImage("gem.tiles", FurGuardiansGame.RES + "misc/GemTiles.png");
        gemTiles2 = engine.createImage("gem.tiles.2", FurGuardiansGame.RES + "misc/GemTiles2.png");
        gemTiles3 = engine.createImage("gem.tiles.3", FurGuardiansGame.RES + "misc/GemTiles3.png");
    }
    
    private final static void initGrid() {
        tm = new TileMap(Pantil.vmid(), SIZE, SIZE, DIM, DIM);
        imgMap = tm.splitImageMap(gemTiles);
        tm.setForegroundDepth(10);
        room.addActor(tm);
        buildGrid();
    }
    
    private final static void buildGrid() {
        final int area = SIZE * SIZE, perColor = area / NUM_COLORS;
        final List<TileMapImage> list = new ArrayList<TileMapImage>(area);
        for (int color = 0; color < NUM_COLORS; color++) {
            final int row = color/ 2, col = color % 2;
            final TileMapImage img = imgMap[row * 3][col * 4];
            for (int i = 0; i < perColor; i++) {
                list.add(img);
            }
        }
        Collections.shuffle(list);
        int i = 0, j = 0;
        for (final TileMapImage img : list) {
            new Cell(i, j, img);
            i++;
            if (i >= SIZE) {
                i = 0;
                j++;
            }
        }
    }
    
    private final static class Cell {
        private final int i;
        private final int j;
        
        private Cell(final int i, final int j, final TileMapImage img) {
            this.i = i;
            this.j = j;
            tm.setBackground(i, j, img);
            newButton();
        }
        
        private final TouchButton newButton() {
            final Pangine engine = Pangine.getEngine();
            final int x = i * DIM;
            final int y = j * DIM;
            final TouchButton button = new TouchButton(engine.getInteraction(), "Gem." + i + "." + j, x, y, DIM, DIM);
            engine.registerTouchButton(button);
            tm.register(button, new ActionStartListener() {
                @Override public final void onActionStart(final ActionStartEvent event) {
                    tm.setForeground(i, j, imgMap[6][4]);
                }});
            return button;
        }
    }
}
