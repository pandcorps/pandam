/*
Copyright (c) 2009-2018, Andrew M. Martin
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
package org.pandcorps.botsnbolts;

import org.pandcorps.botsnbolts.RoomLoader.*;
import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;

public abstract class RoomFunction {
    public abstract void build(final TileMap tm, final int x, final int y);
    
    public final static class PineTree extends RoomFunction {
        private final TileMapImage topLeft = BotsnBoltsGame.imgMap[2][0];
        private final TileMapImage left = BotsnBoltsGame.imgMap[3][0];
        private final TileMapImage topRight = new AdjustedTileMapImage(topLeft, 0, true, false);
        private final TileMapImage right = new AdjustedTileMapImage(left, 0, true, false);
        
        @Override
        public final void build(final TileMap tm, final int x, final int y) {
            final int x1 = x + 1;
            tm.setOverlayOptional(x, y, topLeft, Tile.BEHAVIOR_OPEN);
            tm.setOverlayOptional(x1, y, topRight, Tile.BEHAVIOR_OPEN);
            for (int j = y - 1; j >= 0; j--) {
                setOverlayIfOpen(tm, x, j, left, Tile.BEHAVIOR_OPEN);
                setOverlayIfOpen(tm, x1, j, right, Tile.BEHAVIOR_OPEN);
            }
        }
    }
    
    public final static class BigFan extends RoomFunction {
        private final TileMapImage topLeft = BotsnBoltsGame.imgMap[5][4];
        private final TileMapImage bottomLeft = BotsnBoltsGame.imgMap[6][4];
        private final TileMapImage topRight = new AdjustedTileMapImage(topLeft, 0, true, false);
        private final TileMapImage bottomRight = new AdjustedTileMapImage(bottomLeft, 0, true, false);
        
        private final TileMapImage topLeft1 = new AdjustedTileMapImage(topLeft, 1, false, false);
        private final TileMapImage bottomLeft1 = new AdjustedTileMapImage(bottomLeft, 1, false, false);
        private final TileMapImage topRight1 = new AdjustedTileMapImage(topLeft, 1, false, true);
        private final TileMapImage bottomRight1 = new AdjustedTileMapImage(bottomLeft, 1, false, true);
        
        private final TileMapImage topLeft2 = new AdjustedTileMapImage(topLeft, 2, false, false);
        private final TileMapImage bottomLeft2 = new AdjustedTileMapImage(bottomLeft, 2, false, false);
        private final TileMapImage topRight2 = new AdjustedTileMapImage(topLeft, 2, true, false);
        private final TileMapImage bottomRight2 = new AdjustedTileMapImage(bottomLeft, 2, true, false);
        
        private final TileMapImage topLeft3 = new AdjustedTileMapImage(topLeft, 3, false, false);
        private final TileMapImage bottomLeft3 = new AdjustedTileMapImage(bottomLeft, 3, false, false);
        private final TileMapImage topRight3 = new AdjustedTileMapImage(topLeft, 3, false, true);
        private final TileMapImage bottomRight3 = new AdjustedTileMapImage(bottomLeft, 3, false, true);
        
        private final TileMapImage poleLeft = BotsnBoltsGame.imgMap[7][4];
        private final TileMapImage poleRight = new AdjustedTileMapImage(poleLeft, 0, true, false);
        
        private static Panroom lastRoom = null;
        
        @Override
        public final void build(final TileMap tm, final int x, final int y) {
            final Tile tileTopLeft = tm.getTile(null, topLeft, Tile.BEHAVIOR_OPEN);
            final Tile tileBottomLeft = tm.getTile(poleLeft, bottomLeft, Tile.BEHAVIOR_OPEN);
            final Tile tileTopRight = tm.getTile(null, topRight, Tile.BEHAVIOR_OPEN);
            final Tile tileBottomRight = tm.getTile(poleRight, bottomRight, Tile.BEHAVIOR_OPEN);
            final Tile tilePoleLeft = tm.getTile(poleLeft, null, Tile.BEHAVIOR_OPEN);
            final Tile tilePoleRight = tm.getTile(poleRight, null, Tile.BEHAVIOR_OPEN);
            final int x1 = x + 1, y1 = y - 1;
            tm.setTile(x, y, tileTopLeft);
            tm.setTile(x1, y, tileTopRight);
            tm.setTile(x, y1, tileBottomLeft);
            tm.setTile(x1, y1, tileBottomRight);
            for (int j = 0; j < y1; j++) {
                tm.setTile(x, j, tilePoleLeft);
                tm.setTile(x1, j, tilePoleRight);
            }
            if (lastRoom != RoomLoader.nextRoom) {
                final int d = 4;
                RoomLoader.animators.add(new RoomLoader.TileAnimator(tileTopLeft,
                    new TileFrame(null, topLeft, d), new TileFrame(null, topRight1, d), new TileFrame(null, bottomRight2, d), new TileFrame(null, bottomLeft3, d)));
                RoomLoader.animators.add(new RoomLoader.TileAnimator(tileBottomLeft,
                    new TileFrame(poleLeft, bottomLeft, d), new TileFrame(poleLeft, topLeft1, d), new TileFrame(poleLeft, topRight2, d), new TileFrame(poleLeft, bottomRight3, d)));
                RoomLoader.animators.add(new RoomLoader.TileAnimator(tileTopRight,
                    new TileFrame(null, topRight, d), new TileFrame(null, bottomRight1, d), new TileFrame(null, bottomLeft2, d), new TileFrame(null, topLeft3, d)));
                RoomLoader.animators.add(new RoomLoader.TileAnimator(tileBottomRight,
                    new TileFrame(poleRight, bottomRight, d), new TileFrame(poleRight, bottomLeft1, d), new TileFrame(poleRight, topLeft2, d), new TileFrame(poleRight, topRight3, d)));
                lastRoom = RoomLoader.nextRoom;
            }
        }
    }
    
    protected final static void setOverlayIfOpen(final TileMap tm, final int i, final int j, final Object overlay, final byte behavior) {
        final int index = tm.getIndex(i, j);
        if (tm.isBad(index)) {
            return;
        } else if (Tile.getBehavior(tm.getTile(index)) != Tile.BEHAVIOR_OPEN) {
            return;
        }
        tm.setOverlay(index, overlay, behavior);
    }
    
    public abstract static class StepHandler {
        protected void init() {
        }
        
        protected abstract void step();
        
        protected void finish() {
        }
    }
    
    public final static class WaterRipple extends StepHandler {
        private TileMapImage imgWater = null;
        private Tile tileWater = null;
        private int currentIndex = -1;
        private int currentTimer = 0;
        
        @Override
        protected final void init() {
            imgWater = BotsnBoltsGame.imgMap[7][0];
            tileWater = BotsnBoltsGame.tm.getTile(imgWater, null, Tile.BEHAVIOR_OPEN);
            currentIndex = -1;
        }
        
        @Override
        protected final void step() {
            if (currentIndex == -1) {
                pickTile();
            } else {
                ripple();
            }
        }
        
        private final void pickTile() {
            final TileMap tm = BotsnBoltsGame.tm;
            final int potentialIndex = tm.getIndex(Mathtil.randi(0, tm.getWidth() - 1), Mathtil.randi(0, tm.getHeight() - 1));
            if (tm.getTile(potentialIndex) == tileWater) {
                currentIndex = potentialIndex;
                currentTimer = 0;
            }
        }
        
        private final void ripple() {
            final int frameIndex;
            if (currentTimer < 4) {
                frameIndex = 0;
            } else if (currentTimer < 7) {
                frameIndex = 1;
            } else if (currentTimer < 10) {
                frameIndex = 2;
            } else {
                BotsnBoltsGame.tm.setBackground(currentIndex, imgWater);
                currentIndex = -1;
                return;
            }
            BotsnBoltsGame.tm.setBackground(currentIndex, BotsnBoltsGame.ripple[frameIndex]);
            currentTimer++;
        }
    }
}
