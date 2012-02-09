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
package org.pandcorps.pandax.tile;

import org.pandcorps.pandam.Panctor;
import org.pandcorps.pandam.Panple;

public class TileOccupant extends Panctor {
    
    /*package*/ Tile tile = null;
    
    public TileOccupant(final String id) {
        super(id);
    }
    
    /*package*/ final void setTile(final Tile dst) {
        if (tile != null) {
            tile.occupant = null;
        }
        if (dst != null) {
            dst.occupant = this;
        }
        tile = dst;
    }
    
    public final void setPosition(final Tile dst) {
        setTile(dst);
        getPosition().set(dst.position);
        setZ();
    }
    
    /*package*/ final void setZ() {
        final TileMap map = tile.map;
        final Object depth = map.occupantDepth;
        if (depth == null) {
            return;
        } else {
            final Panple pos = getPosition();
            if (depth.getClass() == Float.class) {
                pos.setZ(((Float) depth).floatValue());
            } else {
                pos.setZ(map.getForegroundDepth() - pos.getY());
            }
        }
    }
    
    //public final void addToRoom(final Tile dst) {
    //    setPosition(dst);
    //    Pangine.getEngine.getRoom.add(this);
    //}
    
    public TileOccupant getNeighbor(final Direction dir) {
        final Tile nt = tile.getNeighbor(dir);
        return nt == null ? null : nt.occupant;
    }
}
