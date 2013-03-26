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

import org.pandcorps.pandam.Panmage;
import org.pandcorps.pandam.Panple;
import org.pandcorps.pandam.impl.FinPanple;

public final class Tile {
    
    /*package*/ final TileMap map;
    
    /*package*/ final int i;
    
    /*package*/ final int j;
    
    /*package*/ final FinPanple position;
    
    // Panimation?
    /*package*/ Object background = null;
    
    /*package*/ Object foreground = null;
    
    ///*package*/ Panctor occupant = null;
    /*package*/ TileOccupant occupant = null;
    
    /*package*/ boolean solid = false;
    
    //int brightness
    
    /*package*/ Tile(final TileMap map, final int i, final int j) {
        this.map = map;
        this.i = i;
        this.j = j;
        final Panple mapPos = map.getPosition();
        position = new FinPanple(mapPos.getX() + i * map.tw, mapPos.getY() + j * map.th, mapPos.getZ());
    }
    
    /*package*/ final Tile getNeighbor(final Direction dir) {
        final int ni = i + (dir == Direction.East ? 1 : dir == Direction.West ? -1 : 0);
        final int nj = j + (dir == Direction.North ? 1 : dir == Direction.South ? -1 : 0);
        return map.getTile(ni, nj);
    }
    
    public final void setBackground(final Panmage background) {
        this.background = background;
    }
    
    public final void setBackground(final TileMapImage background) {
        this.background = background;
    }
    
    /*package*/ final void setBackgroundO(final Object background) {
        this.background = background;
    }
    
    public final void setForeground(final Panmage foreground) {
        this.foreground = foreground;
    }
    
    public final void setForeground(final TileMapImage foreground) {
        this.foreground = foreground;
    }
    
    //TileOccupant has setTile
    //public final void setOccupant(final TileOccupant occupant) {
    //    if (this.occupant != null) {
    //    }
    //}
    
    public final void setSolid(final boolean solid) {
        this.solid = solid;
    }
    
    public static class TileMapImage {
    	/*package*/ final float ix;
    	/*package*/ final float iy;
    	
    	/*package*/ TileMapImage(final float ix, final float iy) {
    		this.ix = ix;
    		this.iy = iy;
    	}
    }
    
    public final static class TileImage extends TileMapImage {
    	/*package*/ final Panmage img;
    	
    	/*package*/ TileImage(final Panmage img, final float ix, final float iy) {
    		super(ix, iy);
    		this.img = img;
    	}
    }
}
