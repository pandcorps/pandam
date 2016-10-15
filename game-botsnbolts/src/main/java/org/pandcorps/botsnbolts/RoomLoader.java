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
package org.pandcorps.botsnbolts;

import java.util.*;

import org.pandcorps.botsnbolts.BlockPuzzle.*;
import org.pandcorps.botsnbolts.Enemy.*;
import org.pandcorps.botsnbolts.ShootableDoor.*;
import org.pandcorps.core.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;

public abstract class RoomLoader {
    protected String roomId = null;
    
    protected final void setRoomId(final String roomId) {
        this.roomId = roomId;
    }
    
    protected abstract Panroom newRoom();
    
    protected final static class ScriptRoomLoader extends RoomLoader {
        @Override
        protected final Panroom newRoom() {
            final Panroom room = BotsnBoltsGame.BotsnBoltsScreen.newRoom();
            SegmentStream in = null;
            try {
                Segment seg;
                in = SegmentStream.openLocation(BotsnBoltsGame.RES + "/level/" + roomId + ".txt");
                seg = in.readRequire("IMG"); //TODO Add bg image and bg color
                BotsnBoltsGame.BotsnBoltsScreen.loadTileImage(seg.getValue(0));
                while ((seg = in.read()) != null) {
                    final String name = seg.getName();
                    if ("RCT".equals(name)) { // Rectangle
                        rct(seg.intValue(0), seg.intValue(1), seg.intValue(2), seg.intValue(3), seg, 4);
                    } else if ("ROW".equals(name)) { // Row
                        row(seg);
                    } else if ("COL".equals(name)) { // Column
                        col(seg);
                    } else if ("BOX".equals(name)) { // Power-up Box
                        box(seg.intValue(0), seg.intValue(1));
                    } else if ("ENM".equals(name)) { // Enemy
                        enm(seg.intValue(0), seg.intValue(1), seg.getValue(2));
                    } else if ("SHP".equals(name)) { // Shootable Block Puzzle
                        shp(seg);
                    } else if ("TMP".equals(name)) { // Timed Block Puzzle
                        tmp(in);
                    } else if ("SPP".equals(name)) { // Spike Block Puzzle
                        spp(seg);
                    } else if ("LDR".equals(name)) { // Ladder
                        ldr(seg.intValue(0), seg.intValue(1), seg.intValue(2));
                    } else if ("BRR".equals(name)) { // Barrier
                        brr(seg.intValue(0), seg.intValue(1), seg.getValue(2));
                    } else if ("DOR".equals(name)) { // Door
                        dor(seg.intValue(0), seg.intValue(1), seg.getValue(2));
                    }
                }
                return room;
            } catch (final Exception e) {
                throw Pantil.toRuntimeException(e);
            } finally {
                Iotil.close(in);
            }
        }
    }
    
    private final static void rct(final int x, final int y, final int w, final int h, final Segment seg, final int tileOffset) throws Exception {
        final TileMap tm = BotsnBoltsGame.tm;
        final TileMapImage bg = getTileMapImage(seg, tileOffset), fg = getTileMapImage(seg, tileOffset + 2);
        final byte b = seg.getByte(tileOffset + 4, Tile.BEHAVIOR_OPEN);
        final Tile tile = (bg == null && fg == null && b == Tile.BEHAVIOR_OPEN) ? null : tm.getTile(bg, fg, b);
        for (int i = 0; i < w; i++) {
            final int currX = x + i;
            for (int j = 0; j < h; j++) {
                final int currY = y + j;
                tm.setTile(currX, currY, tile);
            }
        }
    }
    
    private final static void row(final Segment seg) throws Exception {
        rct(0, seg.intValue(0), BotsnBoltsGame.tm.getWidth(), 1, seg, 1);
    }
    
    private final static void col(final Segment seg) throws Exception {
        rct(seg.intValue(0), 0, 1, BotsnBoltsGame.tm.getHeight(), seg, 1);
    }
    
    private final static void box(final int x, final int y) {
        new PowerBox(x, y);
    }
    
    private final static void enm(final int x, final int y, final String enemyType) {
        //TODO
    }
    
    private final static void shp(final Segment seg) {
        new ShootableBlockPuzzle(getTileIndexArray(seg, 0), getTileIndexArray(seg, 1));
    }
    
    private final static int[] getTileIndexArray(final Segment seg, final int fieldIndex) {
        final TileMap tm = BotsnBoltsGame.tm;
        final List<Field> reps = seg.getRepetitions(fieldIndex);
        final int size = reps.size();
        final int[] indices = new int[size];
        for (int i = 0; i < size; i++) {
            final Field f = reps.get(i);
            indices[i] = tm.getIndex(f.intValue(0), f.intValue(1));
        }
        return indices;
    }
    
    private final static void tmp(final SegmentStream in) throws Exception {
        final List<int[]> steps = new ArrayList<int[]>();
        Segment seg;
        while ((seg = in.readIf("TMS")) != null) {
            steps.add(getTileIndexArray(seg, 0));
        }
        new TimedBlockPuzzle(steps);
    }
    
    private final static void spp(final Segment seg) {
        new SpikeBlockPuzzle(getTileIndexArray(seg, 0), getTileIndexArray(seg, 1));
    }
    
    private final static void ldr(final int x, final int y, final int h) {
        final TileMap tm = BotsnBoltsGame.tm;
        final int end = h - 1;
        for (int j = 0; j < h; j++) {
            tm.setForeground(x, y + j, null, (j == end) ? BotsnBoltsGame.TILE_LADDER_TOP : BotsnBoltsGame.TILE_LADDER); //TODO img
        }
    }
    
    private final static void brr(final int x, final int y, final String doorType) {
        new ShootableBarrier(x, y, ShootableDoor.getShootableDoorDefinition(doorType));
    }
    
    private final static void dor(final int x, final int y, final String doorType) {
        if ("Boss".equals(doorType)) {
            new BossDoor(x, y);
        } else {
            new ShootableDoor(x, y, ShootableDoor.getShootableDoorDefinition(doorType));
        }
    }
    
    private final static TileMapImage getTileMapImage(final Segment seg, final int imageOffset) {
        final int imgX = seg.getInt(imageOffset, -1);
        if (imgX < 0) {
            return null;
        }
        return BotsnBoltsGame.imgMap[seg.intValue(imageOffset + 1)][imgX];
    }
    
    protected final static class DemoRoomLoader extends RoomLoader {
        @Override
        protected final Panroom newRoom() {
            return BotsnBoltsGame.BotsnBoltsScreen.newDemoRoom();
        }
    }
}
