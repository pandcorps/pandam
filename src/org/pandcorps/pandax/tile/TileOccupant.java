/*
Copyright (c) 2009-2014, Andrew M. Martin
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

import org.pandcorps.pandam.*;

public class TileOccupant extends Panctor {
    
    /*package*/ TileMap tm = null;
    /*package*/ int index = -1;
    
    public TileOccupant() {
    }
    
    public TileOccupant(final String id) {
        super(id);
    }
    
    /*package*/ final void setTile(final TileMap tm, final int index) {
        if (this.tm != null) {
            this.tm.setOccupant(this.index, null); //TODO TileMap.removeOccupant?
        }
        if (tm != null) {
            tm.setOccupant(index, this);
        }
        this.tm = tm;
        this.index = index;
    }
    
    public void init(final TileMap tm, final int i, final int j) {
        init(tm, tm.getIndex(i, j));
    }
    
    public void init(final TileMap tm, final int index) {
    	setPosition(tm, index);
    	tm.getLayer().addActor(this);
    }
    
    public final void setPosition(final TileMap tm, final int i, final int j) {
        setPosition(tm, tm.getIndex(i, j));
    }
    
    public final void setPosition(final TileMap tm, final int index) {
        setTile(tm, index);
        tm.savePosition(getPosition(), index);
        setZ();
    }
    
    /*package*/ final void setZ() {
        setZ(this, tm);
    }
    
    public final static void setZ(final Panctor actor, final TileMap map) {
        final Object depth = map.occupantDepth;
        if (depth == null) {
            return;
        } else {
            final Panple pos = actor.getPosition();
            if (depth.getClass() == Float.class) {
                pos.setZ(((Float) depth).floatValue());
            } else {
                pos.setZ(map.getOccupantBaseDepth() - pos.getY());
            }
        }
    }
    
    //public final void addToRoom(final Tile dst) {
    //    setPosition(dst);
    //    Pangine.getEngine.getRoom.add(this);
    //}

    public TileMap getTileMap() {
    	return tm;
    }
    
    public int getIndex() {
        return index;
    }
    
    public int getRow() {
        return tm.getRow(index);
    }
    
    public int getColumn() {
        return tm.getColumn(index);
    }
    
    public TileOccupant getNeighbor(final Direction dir) {
        return tm.getOccupant(tm.getNeighbor(index, dir));
    }
    
    public TileOccupant getOpposite(final TileWalker src) {
        final int i = tm.getColumn(index), j = tm.getRow(index);
        final int si = src.tm.getColumn(src.index), sj = src.tm.getRow(src.index);
    	return tm.getOccupant(tm.getIndex(i * 2 - si, j * 2 - sj));
    }
    
    //@OverrideMe
    public void onInteract(final TileWalker initiator) {
    }
    
    //@OverrideMe
    public String getInteractLabel() {
    	return null;
    }
    
    public final static String getInteractLabel(final TileOccupant o) {
    	return o == null ? null : o.getInteractLabel();
    }
    
    @Override
    protected final void onDestroy() {
        if (tm != null) {
            tm.setOccupant(index, null);
        }
        onDestroyOccupant();
        super.onDestroy();
    }
    
    //@OverrideMe
    protected void onDestroyOccupant() {
    }
}
