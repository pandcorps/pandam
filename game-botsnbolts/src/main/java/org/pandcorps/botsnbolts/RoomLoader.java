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
                    } else if ("LDR".equals(name)) { // Ladder
                    } else if ("BRR".equals(name)) { // Barrier
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
