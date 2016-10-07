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

import org.pandcorps.core.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.pandam.*;

public abstract class RoomLoader {
    protected String roomId = null;
    
    protected final void setRoomId(final String roomId) {
        this.roomId = roomId;
    }
    
    protected abstract Panroom newRoom();
    
    protected final static class ScriptRoomLoader extends RoomLoader {
        @Override
        protected final Panroom newRoom() {
            SegmentStream in = null;
            try {
                Segment seg;
                in = SegmentStream.openLocation(BotsnBoltsGame.RES + "/level/" + roomId + ".txt");
                while ((seg = in.read()) != null) {
                    final String name = seg.getName();
                    if ("RCT".equals(name)) {
                        rct(seg.intValue(0), seg.intValue(1), seg.intValue(2), seg.intValue(3), in);
                    }
                }
                return null;
            } catch (final Exception e) {
                throw Pantil.toRuntimeException(e);
            } finally {
                Iotil.close(in);
            }
        }
    }
    
    private final static void rct(final int x, final int y, final int w, final int h, final SegmentStream in) throws Exception {
        final Segment contentsEG = in.read();
        final int contentW = 1;
        final int contentH = 1;
        for (int i = 0; i < w; i++) {
            final int currX = x + (i * contentW);
            for (int j = 0; j < h; j++) {
                final int currY = y + (j * contentH);
            }
        }
    }
    
    protected final static class DemoRoomLoader extends RoomLoader {
        @Override
        protected final Panroom newRoom() {
            return BotsnBoltsGame.BotsnBoltsScreen.newRoom();
        }
    }
}
