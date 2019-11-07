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

import java.util.*;

import org.pandcorps.botsnbolts.Enemy.*;
import org.pandcorps.botsnbolts.RoomLoader.*;
import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.pandax.visual.*;

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
    
    public final static class FirePressureTile extends RoomFunction {
        @Override
        public final void build(final TileMap tm, final int x, final int y) {
            BlockPuzzle.FirePressureBlock.init(tm.getIndex(x, y));
        }
    }
    
    public final static class BurstTile extends RoomFunction {
        @Override
        public final void build(final TileMap tm, final int x, final int y) {
            new BlockPuzzle.BurstBlock(copyNeighbor(tm, x, y));
        }
    }
    
    public final static class BurstableTile extends RoomFunction {
        @Override
        public final void build(final TileMap tm, final int x, final int y) {
            BlockPuzzle.setBurstable(copyNeighbor(tm, x, y));
        }
    }
    
    public final static class SpikeFloor extends RoomFunction {
        @Override
        public final void build(final TileMap tm, final int x, final int y) {
            BlockPuzzle.setSpikeFloor(tm.getIndex(x, y));
        }
    }
    
    public final static class SpikeCeiling extends RoomFunction {
        @Override
        public final void build(final TileMap tm, final int x, final int y) {
            BlockPuzzle.setSpikeCeiling(tm.getIndex(x, y));
        }
    }
    
    public final static class SpikeCeilingCopyNeighbor extends RoomFunction {
        @Override
        public final void build(final TileMap tm, final int x, final int y) {
            BlockPuzzle.setSpikeCeiling(copyNeighbor(tm, x, y));
        }
    }
    
    public final static class NavalMineTile extends RoomFunction {
        @Override
        public final void build(final TileMap tm, final int x, final int y) {
            RoomLoader.addActor(new NavalMine(x, y));
        }
    }
    
    public final static class HiddenBlockTile extends RoomFunction {
        @Override
        public final void build(final TileMap tm, final int x, final int y) {
            RoomLoader.hiddenBlockIndices = addIndex(RoomLoader.hiddenBlockIndices, tm, x, y);
        }
    }
    
    public final static class HiddenBarrierTile extends RoomFunction {
        @Override
        public final void build(final TileMap tm, final int x, final int y) {
            RoomLoader.hiddenBarrierIndices = addIndex(RoomLoader.hiddenBarrierIndices, tm, x, y);
        }
    }
    
    protected final static List<Integer> addIndex(final List<Integer> list, final TileMap tm, final int x, final int y) {
        return Coltil.add(list, Integer.valueOf(tm.getIndex(x, y)));
    }
    
    protected final static int copyNeighbor(final TileMap tm, final int x, final int y) {
        final int tileIndex = tm.getIndex(x, y);
        if (isWallTile(x, y + 1)) {
            // x+1 neighbor won't be poulated yet, RoomFunctions are called while processing tiles from left to right
            tm.setTile(tileIndex, tm.getTile(x - 1, y));
        } else if (isWallTile(x - 1, y)) {
            tm.setTile(tileIndex, tm.getTile(x, y + 1)); // y-1 neighbor not populated yet
        }
        return tileIndex;
    }
    
    protected final static boolean isWallTile(final int x, final int y) {
        final byte b = Tile.getBehavior(BotsnBoltsGame.tm.getTile(x, y));
        return Chr.isAnySolidBehavior(b) && (b != BotsnBoltsGame.TILE_BURSTABLE);
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
        private TileMap tm = null;
        private int currentIndex = -1;
        private int currentTimer = 0;
        
        @Override
        protected final void init() {
            finish();
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
            if (RoomChanger.isChanging()) {
                finish();
                return;
            }
            final int maxY = RoomLoader.getWaterTile() - 2;
            if (maxY < 0) {
                return;
            }
            tm = BotsnBoltsGame.tm;
            if (tm == null) {
                return;
            }
            final Panlayer layer = tm.getLayer();
            if (layer == null) {
                return;
            }
            final int minX = Math.max(0, Math.round(layer.getViewMinimum().getX() / BotsnBoltsGame.DIM));
            final int maxX = Math.min(tm.getWidth() - 1, Math.round(layer.getViewMaximum().getX() / BotsnBoltsGame.DIM));
            final int potentialIndex = tm.getIndex(Mathtil.randi(minX, maxX), Mathtil.randi(0, maxY));
            if (Tile.getBehavior(tm.getTile(potentialIndex)) == BotsnBoltsGame.TILE_WATER) {
                currentIndex = potentialIndex;
                currentTimer = 0;
            }
        }
        
        private final void ripple() {
            if (tm == null) {
                finish();
                return;
            }
            final int frameIndex;
            if (currentTimer < 4) {
                frameIndex = 0;
            } else if (currentTimer < 7) {
                frameIndex = 1;
            } else if (currentTimer < 10) {
                frameIndex = 2;
            } else {
                finish();
                return;
            }
            tm.setBackground(currentIndex, BotsnBoltsGame.ripple[frameIndex]);
            currentTimer++;
        }
        
        @Override
        protected final void finish() {
            if ((tm != null) && (currentIndex >= 0)) {
                tm.setBackground(currentIndex, null);
            }
            currentIndex = -1;
        }
    }
    
    public final static class HailBackground extends StepHandler {
        final Pancolor[] colors = new Pancolor[11];
        
        {
            for (int i = 0; i < 3; i++) {
                colors[i] = new FinPancolor(0, 96, 48 + (24 * i));
            }
            for (int i = 0; i < 4; i++) {
                colors[3 + i] = new FinPancolor(0, 24 * (3 - i), 96);
            }
            for (int i = 0; i < 4; i++) {
                colors[7 + i] = new FinPancolor(24 * (i + 1), 0, 96);
            }
        }
        
        @Override
        protected final void step() {
            final Pangine engine = Pangine.getEngine();
            final int size = colors.length, m = 4, wait = 8;
            final int _i = ((int) (engine.getClock() % (((size * 2) + wait) * m))) / m;
            final int c;
            if (_i < wait) {
                c = 0;
            } else {
                final int i = _i - wait;
                c = (i < size) ? i : ((size * 2) - 1 - i);
            }
            engine.setBgColor(colors[c]);
        }
    }
    
    public final static class LightningBackground extends StepHandler {
        private final static int fadeFrameDuration = 2;
        private final static int lightningDuration = 15;
        private static long currentStart = -1;
        private static int totalPeriod = 180;
        
        final Pancolor[] colors = {
                new FinPancolor(255, 255, 192),
                new FinPancolor(232, 232, 184),
                new FinPancolor(216, 216, 168),
                new FinPancolor(200, 200, 152),
                new FinPancolor(176, 176, 144)};
        
        @Override
        protected final void step() {
            final Pangine engine = Pangine.getEngine();
            final long clock = engine.getClock();
            final long diff = clock - currentStart;
            final int i;
            if ((currentStart < 0) || (diff >= totalPeriod)) {
                currentStart = clock;
                totalPeriod = Mathtil.randi(40, 240);
                i = 0;
            } else {
                i = (int) diff;
            }
            final int c;
            final int fadeStart = totalPeriod - (fadeFrameDuration * 3);
            if (i >= fadeStart) {
                c = (i - (fadeStart - fadeFrameDuration)) / fadeFrameDuration;
            } else if (i >= (fadeStart - lightningDuration)) {
                c = 0;
            } else {
                c = 4;
            }
            engine.setBgColor(colors[c]);
        }
    }
    
    public final static class DroughtBackground extends StepHandler {
        final int numColors = 9;
        final Pancolor[] colors = new Pancolor[numColors];
        private int index = 2;
        
        {
            for (int i = 0; i < numColors; i++) {
                final int o = (i + 6) * 8;
                colors[i] = new FinPancolor(Pancolor.MAX_VALUE, 128 + o, 64 + o);
            }
        }
        
        @Override
        protected final void step() {
            final Pangine engine = Pangine.getEngine();
            if ((engine.getClock() % 5) == 0) {
                if (index < 1) {
                    index = 1;
                } else if (index > (numColors - 2)) {
                    index = numColors - 2;
                } else {
                    index += (Mathtil.rand() ? 1 : -1);
                }
                engine.setBgColor(colors[index]);
            }
        }
    }
    
    public final static class ArrayBackground extends StepHandler {
        final Pancolor[] colors = new Pancolor[9];
        
        {
            final short s0 = 0;
            for (short i = 0; i < 9; i++) {
                final short c = (short) ((i * 16) + 128);
                colors[i] = new FinPancolor(s0, c, c);
            }
        }
        
        @Override
        protected final void step() {
            final Pangine engine = Pangine.getEngine();
            final int i = (int) (engine.getClock() % 120);
            final int c;
            if (i < 9) {
                c = i;
            } else if (i < 60) {
                c = 8;
            } else if (i < 69) {
                c = 8 - (i - 60);
            } else {
                c = 0;
            }
            engine.setBgColor(colors[c]);
        }
    }
    
    public final static class LabBackground extends StepHandler {
        private static boolean active = true;
        private static TileMapImage[] imgs = null;
        
        protected final static void setActive(final boolean active) {
            if (LabBackground.active == active) {
                return;
            }
            LabBackground.active = active;
            update();
        }
        
        @Override
        protected final void init() {
            active = true;
            update();
        }
        
        @Override
        protected final void step() {
            if (!active) {
                return;
            } else if ((Pangine.getEngine().getClock() % 30) != 0) {
                return;
            }
            update();
        }
        
        private final static void update() {
            initImages();
            final TileMap tm = BotsnBoltsGame.tm;
            for (int x = 11; x <= 12; x++) {
                for (int y = 8; y <= 9; y++) {
                    tm.setBackground(x, y, active ? Mathtil.rand(imgs) : imgs[20]);
                }
            }
        }
        
        private final static void initImages() {
            if (imgs != null) {
                return;
            }
            final TileMapImage[] imgRow = BotsnBoltsGame.imgMap[6];
            imgs = new TileMapImage[21];
            for (int i = 0; i < 5; i++) {
                final int i4 = i * 4;
                final TileMapImage img = imgRow[3 + i];
                imgs[i4] = img;
                for (int rot = 1; rot < 4; rot++) {
                    imgs[i4 + rot] = new AdjustedTileMapImage(img, rot, false, false);
                }
            }
            imgs[20] = imgRow[2];
        }
    }
    
    public final static class Shader {
        private final Object top;
        private final Object topLeft;
        private final Object topRight;
        private final Object mid;
        private final Object left;
        private final Object right;
        
        public Shader(final Object top, final Object topLeft, final Object topRight, final Object mid, final Object left, final Object right) {
            this.top = top;
            this.topLeft = topLeft;
            this.topRight = topRight;
            this.mid = mid;
            this.left = left;
            this.right = right;
        }
        
        public final void addShadow(final TileMap tm, final int x, final int y) {
            final int index = tm.getIndex(x, y);
            final Tile tile = tm.getTile(index);
            if (Chr.isSolidTile(tile)) {
                return;
            }
            final Object bg = DynamicTileMap.getRawBackground(tile);
            if (bg == mid) {
                if (!tm.isBad(index)) {
                    tm.setBackground(index, top);
                }
            } else if (bg == null) {
                return;
            } else if (bg == left) {
                tm.setBackground(index, topLeft);
            } else if (bg == right) {
                tm.setBackground(index, topRight);
            }
        }
        
        public final void removeShadow(final TileMap tm, final int x, final int y) {
            final int index = tm.getIndex(x, y);
            final Tile tile = tm.getTile(index);
            if (tile == null) {
                return;
            } else if (Chr.isSolidTile(tile)) {
                return;
            }
            final Object bg = DynamicTileMap.getRawBackground(tile);
            if (bg == null) {
                return;
            } else if (bg == top) {
                tm.setBackground(index, mid);
            } else if (bg == topLeft) {
                tm.setBackground(index, left);
            } else if (bg == topRight) {
                tm.setBackground(index, right);
            }
        }
    }
}
