/*
Copyright (c) 2009-2021, Andrew M. Martin
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

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;

public final class Tile {
	public final static byte BEHAVIOR_OPEN = 0;
	public final static byte BEHAVIOR_SOLID = 1;
	/*package*/ final static byte BEHAVIOR_DEFAULT = BEHAVIOR_OPEN;
	
    // bg/fg/solid behavior could likely be moved into a TileDefinition; many Tiles would likely share the same definition
	// Shouldn't need TileMap.initTile or setters in Tile; Tile should usually be immutable
    
    // Panimation?
    /*package*/ Object background = null;
    /*package*/ Object foreground = null;
    /*package*/ byte behavior = BEHAVIOR_DEFAULT;
    
    //int brightness
    
    /*package*/ Tile() {
    }
    
    /*public final void setBackground(final Panmage background) {
        this.background = background;
    }
    
    public final void setBackground(final TileMapImage background) {
        this.background = background;
    }
    
    public final void setBackground(final TileMapImage background, final boolean solid) {
        setBackground(background);
        setSolid(solid);
    }
    
    public final void setBackground(final TileMapImage background, final byte behavior) {
        setBackground(background);
        setBehavior(behavior);
    }*/
    
    public final void setBackground(final Object background) {
        this.background = background;
    }
    
    // Changes foreground for all occurrences of this Tile
    public final void setForeground(final Object foreground) {
        this.foreground = foreground;
    }
    
    /*public final void setForeground(final TileMapImage foreground, final boolean solid) {
        setForeground(foreground);
        setSolid(solid);
    }
    
    public final void setForeground(final TileMapImage foreground, final byte behavior) {
        setForeground(foreground);
        setBehavior(behavior);
    }
    
    public final void setImages(final TileMapImage background, final TileMapImage foreground) {
        setBackground(background);
        setForeground(foreground);
    }
    
    public final void setImages(final TileMapImage background, final TileMapImage foreground, final byte behavior) {
    	setImages(background, foreground);
    	setBehavior(behavior);
    }
    
    //TileOccupant has setTile
    //public final void setOccupant(final TileOccupant occupant) {
    //    if (this.occupant != null) {
    //    }
    //}
    
    public final void setSolid(final boolean solid) {
        behavior = getSolidBehavior(solid);
    }*/
    
    /*package*/ final static byte getSolidBehavior(final boolean solid) {
        return solid ? BEHAVIOR_SOLID : BEHAVIOR_OPEN;
    }
    
    public final boolean isSolid() {
    	return behavior == BEHAVIOR_SOLID;
    }
    
    /*public final void setBehavior(final byte behavior) {
        this.behavior = behavior;
    }*/
    
    public final byte getBehavior() {
        return behavior;
    }
    
    @Override
    public final String toString() {
        return "Bg: " + background + "; Fg: " + foreground + "; B: " + behavior;
    }
    
    public static class TileMapImage {
    	/*package*/ float ix; // These could probably be int
    	/*package*/ float iy;
    	
    	/*package*/ TileMapImage(final float ix, final float iy) {
    		this.ix = ix;
    		this.iy = iy;
    	}
    	
    	@Override
    	public int hashCode() {
    	    return Float.floatToIntBits(ix) ^ Float.floatToIntBits(iy);
    	}
    	
    	@Override
    	public boolean equals(final Object o) {
    	    if (this == o) {
                return true;
            } else if (o == null || o.getClass() != TileMapImage.class) {
    	        return false;
    	    }
    	    return equalsTileMapImage((TileMapImage) o);
    	}
    	
    	protected final boolean equalsTileMapImage(final TileMapImage t) {
    	    return ix == t.ix && iy == t.iy;
    	}
    	
    	@Override
        public final String toString() {
    	    return "(" + ix + ", " + iy + ")";
    	}
    }
    
    public final static class TileImage extends TileMapImage {
    	/*package*/ final Panmage img;
    	
    	/*package*/ TileImage(final Panmage img, final float ix, final float iy) {
    		super(ix, iy);
    		this.img = img;
    	}
    	
    	@Override
        public final int hashCode() {
            return super.hashCode() ^ img.hashCode();
        }
    	
    	@Override
        public final boolean equals(final Object o) {
    	    if (this == o) {
    	        return true;
    	    } else if (o == null || o.getClass() != TileImage.class) {
                return false;
            }
            return equalsTileImage((TileImage) o);
    	}
    	
    	protected final boolean equalsTileImage(final TileImage t) {
            return equalsTileMapImage(t) && img == t.img;
        }
    }
    
    public static class AdjustedTileMapImage extends TileMapImage {
        private TileMapImage raw = null;
        /*package*/ final float offZ;
        /*package*/ final int rot;
        /*package*/ final boolean mirror;
        /*package*/ final boolean flip;
        
        public AdjustedTileMapImage(final float ix, final float iy, final int rot, final boolean mirror, final boolean flip) {
            this(ix, iy, 0, rot, mirror, flip);
        }
        
        public AdjustedTileMapImage(final float ix, final float iy, final float offZ, final int rot, final boolean mirror, final boolean flip) {
            super(ix, iy);
            this.offZ = offZ;
            this.rot = rot;
            this.mirror = mirror;
            this.flip = flip;
        }
        
        public AdjustedTileMapImage(final TileMapImage img, final int rot, final boolean mirror, final boolean flip) {
            this(img, 0, rot, mirror, flip);
        }
        
        public AdjustedTileMapImage(final TileMapImage img, final float offZ, final int rot, final boolean mirror, final boolean flip) {
            this(img.ix, img.iy, offZ, rot, mirror, flip);
            raw = img;
        }
        
        public final TileMapImage getRaw() {
            return raw;
        }
        
        @Override
        public int hashCode() {
            return super.hashCode() ^ rot;
        }
        
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            } else if (o == null || o.getClass() != AdjustedTileMapImage.class) {
                return false;
            }
            return equalsAdjustedTileMapImage((AdjustedTileMapImage) o);
        }
        
        protected final boolean equalsAdjustedTileMapImage(final AdjustedTileMapImage t) {
            return equalsTileMapImage(t) && offZ == t.offZ && rot == t.rot && mirror == t.mirror && flip == t.flip;
        }
    }
    
    public final static class MultiTileMapImage extends AdjustedTileMapImage {
        /*package*/ final int w;
        /*package*/ final int h;
        
        public MultiTileMapImage(final float ix, final float iy, final float offZ, final int rot, final boolean mirror, final boolean flip, final int w, final int h) {
            super(ix, iy, offZ, rot, mirror, flip);
            this.w = w;
            this.h = h;
        }
        
        public MultiTileMapImage(final TileMapImage img, final float offZ, final int rot, final boolean mirror, final boolean flip, final int w, final int h) {
            super(img, offZ, rot, mirror, flip);
            this.w = w;
            this.h = h;
        }
        
        @Override
        public final int hashCode() {
            return super.hashCode() ^ w ^ h;
        }
        
        @Override
        public final boolean equals(final Object o) {
            if (this == o) {
                return true;
            } else if (o == null || o.getClass() != MultiTileMapImage.class) {
                return false;
            }
            return equalsMultiTileMapImage((MultiTileMapImage) o);
        }
        
        protected final boolean equalsMultiTileMapImage(final MultiTileMapImage t) {
            return equalsAdjustedTileMapImage(t) && w == t.w && h == t.h;
        }
    }
    
    /*package*/ final static class ExtensionTileMapImage {
        /*package*/ final int itw;
        /*package*/ final int jth;
        /*package*/ final Object srcImg;
        /*package*/ final int tw;
        /*package*/ final int th;
        
        /*package*/ ExtensionTileMapImage(final int itw, final int jth, final Object srcImg, final int tw, final int th) {
            this.itw = itw;
            this.jth = jth;
            this.srcImg = srcImg;
            this.tw = tw;
            this.th = th;
        }
    }
    
    @Override
    public final int hashCode() {
        return Pantil.hashCode(background) ^ Pantil.hashCode(foreground) ^ behavior;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o == null || o.getClass() != Tile.class) {
            return false;
        }
        final Tile t = (Tile) o;
        return Pantil.equals(background, t.background) && Pantil.equals(foreground, t.foreground) && behavior == t.behavior;
    }
    
    public final static byte getBehavior(final Tile t) {
    	return t == null ? BEHAVIOR_DEFAULT : t.behavior;
    }
    
    public final static void animate(final TileMapImage... imgs) {
        TileMapImage cur = imgs[0];
        final TileMapImage tmp = new TileMapImage(cur.ix, cur.iy);
        final int stop = imgs.length;
        for (int i = 1; i <= stop; i++) {
            final TileMapImage nxt = (i < stop) ? imgs[i] : tmp;
            cur.ix = nxt.ix;
            cur.iy = nxt.iy;
            cur = nxt;
        }
    }
}
