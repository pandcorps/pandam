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
package org.pandcorps.pandam.impl;

import java.nio.*;
import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.img.scale.*;
import org.pandcorps.pandam.*;

public final class GlPanmage extends Panmage {
	private final static int NULL_TID = 0;
	public static boolean saveTextures = false;
	private final int w;
	private final int h;
	/*package*/ final SizePanple size;
	private int tid;
	protected Texture tex = null;
	private final float offx;
	private final float offy;
	private final int tw;
	private final int th;
	//private final int up = 1; // Could be flipped in Panplementation
	//private final int down = 0;
	//private final int left = 0;
	//private final int right = 1;
	//private final FloatChain t = new FloatChain();
	//private final FloatChain v = new FloatChain();
	private final static class ImageLayer {
	    private final FloatChain t = new FloatChain();
	    private final FloatChain v = new FloatChain();
	}
	private final IdentityHashMap<Panlayer, ImageLayer> layers = new IdentityHashMap<Panlayer, ImageLayer>();

	private final class SizePanple extends UnmodPanple {
		@Override
		public final float getX() {
			return w;
		}

		@Override
		public final float getY() {
			return h;
		}

		@Override
		public final float getZ() {
			return 0;
		}
	}
	
	protected final static class Texture {
	    private final int usableWidth; // raw
	    
	    private final int usableHeight;
	    
	    private final int paddedSize;
	    
	    private final int scaledWidth;
	    
	    private final int scaledHeight;
	    
	    protected ByteBuffer scratch;
	    
	    private int tid = NULL_TID; // Multiple GLPanmages can share same Texture
	    
	    private Texture(final int usableWidth, final int usableHeight, final int paddedSize, final int scaledWidth, final int scaledHeight, final ByteBuffer scratch) {
	        this.usableWidth = usableWidth;
	        this.usableHeight = usableHeight;
	        this.paddedSize = paddedSize;
	        this.scaledWidth = scaledWidth;
	        this.scaledHeight = scaledHeight;
	        this.scratch = scratch;
	    }
	    
	    private final void close() {
	    	scratch.clear();
			scratch = null;
	    }
	}

	private final static Texture prepareTexture(final String location) {
////
	    return prepareTexture(Imtil.load(location));
	}
	
	private final static int toPow2(final int d) {
	    for (int i = 1; ; i *= 2) {
	        if (i >= d) {
	            return i;
	        }
	    }
	}
	
	private final static Img pad(final Img _img) {
	    final int usableWidth = _img.getWidth(), usableHeight = _img.getHeight();
	    final int d = Math.max(toPow2(usableWidth), toPow2(usableHeight));
	    final Img padded = Imtil.newImage(d, d);
	    Imtil.copy(_img, padded, 0, 0, usableWidth, usableHeight, 0, 0);
	    return padded;
	}
	
	private final static Texture prepareTexture(final Img _img) {
		final Pangine engine = Pangine.getEngine();
		final Scaler scaler = engine.getImageScaler();
		final float zoom = engine.getZoom();
		final Img padded = pad(_img);
		Img img = padded;
		if (scaler != null) {
			for (int i = 1; i < zoom; i *= 2) {
				final Img tmp = scaler.scale(img);
				img.closeIfTemporary();
				img = tmp;
			}
		}
		final int w = img.getWidth();
		final int h = img.getHeight();
		
		if (w != h) {
		    throw new UnsupportedOperationException("I think images need to be squares; I think their dimensions must be powers of two also");
		}

		try {
			return new Texture(_img.getWidth(), _img.getHeight(), padded.getWidth(), w, h, img.toByteBuffer());
		} finally {
			_img.closeIfTemporary();
			padded.closeIfTemporary();
			img.closeIfTemporary();
		}
	}
	
	/*
	TODO
	Pangine's main loop should start binding any unbound images during frames with extra time.
	*/
	private final void bindTexture() {
		if (tex.tid != NULL_TID) {
			tid = tex.tid;
			/*if (tid == NULL_TID) {
	            throw new IllegalStateException("Previously handled texture id was unexpectedly " + tid);
	        }*/
			return; // Must have been handled by a different GlPanmage with same Texture
		}
		// Create A IntBuffer For Image Address In Memory
		final IntBuffer buf = Pantil.allocateDirectIntBuffer(1);
		GlPangine.gl.glGenTextures(buf); // Create Texture In OpenGL
		// Create Nearest Filtered Texture
		tid = buf.get(0);
		if (tid == NULL_TID) {
            throw new IllegalStateException("New texture id was unexpectedly " + tid);
        }
		tex.tid = tid;
		rebindTexture();
		if (!saveTextures) {
			tex.close();
		}
	}
	
	protected final void rebindTexture() {
		final Pangl gl = GlPangine.gl;
		gl.glBindTexture(gl.GL_TEXTURE_2D, tid);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_NEAREST);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_NEAREST);
		gl.glTexImage2D(
			gl.GL_TEXTURE_2D, 0, gl.GL_RGBA, tex.scaledWidth, tex.scaledHeight, 0, gl.GL_RGBA, gl.GL_UNSIGNED_BYTE,
			tex.scratch);
	}
	
	private GlPanmage(final String id, final Panple origin,
	                    final Panple boundMin, final Panple boundMax, final Texture tex) {
	    super(id, origin, boundMin, boundMax);
	    w = tex.usableWidth;
	    h = tex.usableHeight;
		//size = new FinPanple(w, h, 0);
		size = new SizePanple();
		offx = 0;
		offy = 0;
		tw = tex.paddedSize;
		th = tex.paddedSize;
		this.tex = tex;
	}
	
	public GlPanmage(final String id, final Panple origin,
                        final Panple boundMin, final Panple boundMax, final String location) {
	    this(id, origin, boundMin, boundMax, prepareTexture(location));
	}
	
	public GlPanmage(final String id, final Panple origin,
                        final Panple boundMin, final Panple boundMax, final Img img) {
        this(id, origin, boundMin, boundMax, prepareTexture(img));
    }
	
	private GlPanmage(final String id, final Panple origin,
	                     final Panple boundMin, final Panple boundMax,
	                     final Texture tex, final int w, final int h,
	                     final float offx, final float offy,
	                     final int tw, final int th) {
	    super(id, origin, boundMin, boundMax);
        this.w = w;
        this.h = h;
        this.tex = tex;
        size = new SizePanple();
        this.offx = offx;
        this.offy = offy;
        this.tw = tw;
        this.th = th;
	}
	
	public final static GlPanmage[][] createSheet(final String prefix, final Panple origin,
	                                                 final Panple boundMin, final Panple boundMax,
	                                                 final String location,
	                                                 final int iw, final int ih) {
	    final Texture tex = prepareTexture(location);
	    final int tw = tex.usableWidth;
	    if (tw % iw != 0) {
	        throw new IllegalArgumentException("Texture width " + tw + " not divisible by image width " + iw);
	    }
	    final int th = tex.usableHeight;
        if (th % ih != 0) {
            throw new IllegalArgumentException("Texture height " + th + " not divisible by image height " + ih);
        }
        final int rows = th / ih, cols = tw / iw;
        final GlPanmage[][] sheet = new GlPanmage[rows][cols];
        for (int oy = 0; oy < rows; oy++) {
            final GlPanmage[] row = sheet[oy];
            for (int ox = 0; ox < cols; ox++) {
                row[ox] = new GlPanmage(prefix + "-" + oy + "-" + ox, origin, boundMin, boundMax, tex, iw, ih, ((float) ox * iw) / tw, ((float) oy * ih) / th, tw, th);
            }
        }
        //TODO group layers by texture if multiple images can use same texture
        return sheet;
	}

	@Override
	public final Panple getSize() {
		return size;
	}

	public final void clearAll() {
	    for (final ImageLayer l : layers.values()) {
	        l.t.clear();
	        l.v.clear();
	    }
	}
	
	public final void renderAll(final Panlayer layer) {
	    final ImageLayer l = layers.get(layer);
	    if (l == null) {
	        return;
	    }
	    final FloatChain t = l.t;
	    final int numTexCoords = t.getSize();
	    if (numTexCoords > 0) {
	    	final Pangl gl = GlPangine.gl;
	    	if (tid == NULL_TID) {
	    	    bindTexture();
	    	    if (!saveTextures) {
	    	    	tex = null;
	    	    }
	    	}
    	    gl.glLoadIdentity();
    	    gl.glBindTexture(gl.GL_TEXTURE_2D, tid);
    	    //gl.glColor3b((byte) 0, (byte) 0, Byte.MAX_VALUE); // Kind of works to make everything blue
    	    final FloatBuffer tb = t.getBuffer();
    	    tb.rewind();
    	    gl.glTexCoordPointer(2, 0, tb);
    	    final FloatBuffer vb = l.v.getBuffer();
    	    vb.rewind();
            gl.glVertexPointer(3, 0, vb);
            //gl.glDrawElements(gl.GL_QUADS, wrap(i)); // Allows you to specify the index of a single vertex multiple times, less total vertices required
            gl.glDrawArrays(gl.isQuadSupported() ? gl.GL_QUADS : gl.GL_TRIANGLES, 0, numTexCoords / 2); // Number of vertices
	    }
	}

	@Override
	protected final void render(final Panlayer layer, final float x, final float y, final float z,
		//final float ix, final float iy, final float iw, final float ih) {
        final float ix, final float iy, final float iw, final float ih, final int rot, final boolean mirror, final boolean flip) {
		//final boolean mirror = true;
		//gl.glLoadIdentity();
		//gl.glTranslatef(x, y, z);
		// Might be better to store rounded values whenever they are changed
		//gl.glTranslatef(Math.round(x), Math.round(y), z);
		//gl.glRotatef(rotation, 0f, 0f, 1f);

		//gl.glBindTexture(gl.GL_TEXTURE_2D, tid);
		
		/*
		gl.glBegin(gl.GL_QUADS); {
			final float left = ix / w;
			final float right = (ix + iw) / w;
			final float down = iy / h;
			final float up = (iy + ih) / h;
			gl.glTexCoord2f(right, up);
			gl.glVertex2f(iw, 0);

			gl.glTexCoord2f(left, up);
			gl.glVertex2f(0, 0);

			gl.glTexCoord2f(left, down);
			gl.glVertex2f(0, ih);

			gl.glTexCoord2f(right, down);
			gl.glVertex2f(iw, ih);
		}
	   	gl.glEnd();
	   	*/
	   	
	   	/*
	   	final float[] v = new float[8];
	   	final float[] t = new float[8];
	   	final float tleft = ix / w;
        final float tright = (ix + iw) / w;
        final float tdown = iy / h;
        final float tup = (iy + ih) / h;
        final int ox = Math.round(x), oy = Math.round(y);
        final float vleft = ox, vright = iw + ox, vdown = ih + oy, vup = oy;
        t[0] = tright; t[1] = tup;
        //v[0] = iw; v[1] = 0;
        v[0] = vright; v[1] = vup;
        t[2] = tleft; t[3] = tup;
        //v[2] = 0; v[3] = 0;
        v[2] = vleft; v[3] = vup;
        t[4] = tleft; t[5] = tdown;
        //v[4] = 0; v[5] = ih;
        v[4] = vleft; v[5] = vdown;
        t[6] = tright; t[7] = tdown;
        //v[6] = iw; v[7] = ih;
        v[6] = vright; v[7] = vdown;
        gl.glTexCoordPointer(2, 0, wrap(t));
        gl.glVertexPointer(2, 0, wrap(v));
        //gl.glDrawElements(gl.GL_QUADS, wrap(i)); // Allows you to specify the index of a single vertex multiple times, less total vertices required
        gl.glDrawArrays(gl.GL_QUADS, 0, 4); // Number of vertices
        */
	    
	    final float tbleft = offx + ix / tw, tbright = offx + (ix + iw) / tw;
	    final float tbdown = offy + iy / th, tbup = offy + (iy + ih) / th;
	    //final float trleft, trright, trdown, trup;
	    final float trdlx, trdly, trulx, truly, trurx, trury, trdrx, trdry;
	    final float irw, irh;
	    final int r = rot % 4;
	    switch (r) {
	        case 0 :
	            /*trleft = tbleft;
	            trright = tbright;
	            trdown = tbdown;
	            trup = tbup;*/
	            
	            trdlx = tbleft;
	            trdly = tbdown;
	            trulx = tbleft;
	            truly = tbup;
	            trurx = tbright;
	            trury = tbup;
	            trdrx = tbright;
	            trdry = tbdown;
	            
	            irw = iw;
	            irh = ih;
	            break;
	        case 1 :
	            /*trleft = tbdown;
                trright = tbup;
                trdown = tbright;
                trup = tbleft;*/
	            
	            trdlx = tbright;
                trdly = tbdown;
                trulx = tbleft;
                truly = tbdown;
                trurx = tbleft;
                trury = tbup;
                trdrx = tbright;
                trdry = tbup;
                
                irw = ih;
                irh = iw;
                break;
	        case 2 :
                /*trleft = tbright;
                trright = tbleft;
                trdown = tbup;
                trup = tbdown;*/
	            
	            trdlx = tbright;
                trdly = tbup;
                trulx = tbright;
                truly = tbdown;
                trurx = tbleft;
                trury = tbdown;
                trdrx = tbleft;
                trdry = tbup;
                
                irw = iw;
                irh = ih;
                break;
            default :
                /*trleft = tbup;
                trright = tbdown;
                trdown = tbleft;
                trup = tbright;*/
                
                trdlx = tbleft;
                trdly = tbup;
                trulx = tbright;
                truly = tbup;
                trurx = tbright;
                trury = tbdown;
                trdrx = tbleft;
                trdry = tbdown;
                
                irw = ih;
                irh = iw;
                break;
	    }
	    /*final float tleft, tright;
	    if (mirror) {
	        tleft = trright;
	        tright = trleft;
	    } else {
	        tleft = trleft;
	        tright = trright;
	    }*/
	    final float tmdlx, tmdly, tmulx, tmuly, tmurx, tmury, tmdrx, tmdry;
        if (mirror) {
            tmdlx = trdrx;
            tmdly = trdry;
            tmulx = trurx;
            tmuly = trury;
            tmurx = trulx;
            tmury = truly;
            tmdrx = trdlx;
            tmdry = trdly;
        } else {
            tmdlx = trdlx;
            tmdly = trdly;
            tmulx = trulx;
            tmuly = truly;
            tmurx = trurx;
            tmury = trury;
            tmdrx = trdrx;
            tmdry = trdry;
        }
	    /*final float tdown, tup;
	    if (flip) {
	        tdown = trup;
	        tup = trdown;
	    } else {
	        tdown = trdown;
	        tup = trup;
	    }*/
        final float tdlx, tdly, tulx, tuly, turx, tury, tdrx, tdry;
        if (flip) {
            tdlx = tmulx;
            tdly = tmuly;
            tulx = tmdlx;
            tuly = tmdly;
            turx = tmdrx;
            tury = tmdry;
            tdrx = tmurx;
            tdry = tmury;
        } else {
            tdlx = tmdlx;
            tdly = tmdly;
            tulx = tmulx;
            tuly = tmuly;
            turx = tmurx;
            tury = tmury;
            tdrx = tmdrx;
            tdry = tmdry;
        }
        final int ox = Math.round(x), oy = Math.round(y);
        final float vleft = ox, vright = irw + ox, vdown = irh + oy, vup = oy;
        ImageLayer l = layers.get(layer);
        if (l == null) {
            l = new ImageLayer();
            layers.put(layer, l);
        }
        final FloatChain t = l.t, v = l.v;
/*if (r == 1) {
    System.out.println("Up: " + tup + "; Left: " + tleft + "; Down: " + tdown + "; Right: " + tright);
}*/
        t.append(turx); t.append(tury);
        v.append(vright); v.append(vup); v.append(z);
        t.append(tulx); t.append(tuly);
        v.append(vleft); v.append(vup); v.append(z);
        t.append(tdlx); t.append(tdly);
        v.append(vleft); v.append(vdown); v.append(z);
        t.append(tdrx); t.append(tdry);
        v.append(vright); v.append(vdown); v.append(z);
        if (!GlPangine.gl.isQuadSupported()) {
        	t.append(turx); t.append(tury);
            v.append(vright); v.append(vup); v.append(z);
            t.append(tdlx); t.append(tdly);
            v.append(vleft); v.append(vdown); v.append(z);
        }
        
        /*
        TODO
        display lists?
        vertex buffer objects?
        texture atlas?
        */
	}
	
	//private final static FloatBuffer wrap(final float[] f) {
	//    //return FloatBuffer.wrap(f); // FloatBuffer is not direct
	//    //final ByteBuffer bb = ByteBuffer.allocateDirect(f.length * FLOAT_SIZE);
	//    final FloatBuffer fb = Pantil.allocateDirectFloatBuffer(f.length);
	//    fb.put(f);
	//    fb.rewind();
	//    return fb;
	//}
	
    //private final static IntBuffer wrap(final int[] i) {
    //    //return IntBuffer.wrap(i); // IntBuffer is not direct
    //    final ByteBuffer bb = Pantil.allocateDirectIntBuffer(i.length);
    //    ib.put(i);
    //    ib.rewind();
    //    return ib;
    //}
	
	@Override
    protected final void close() {
		if (tid == NULL_TID) {
			return;
		} else if (saveTextures && tex != null && tex.scratch != null) {
			tex.close();
		}
		GlPangine.images.remove(this);
	    //System.out.println("Closing " + tid + "; isTexture: " + gl.glIsTexture(tid)); // true
		GlPangine.gl.glDeleteTextures(tid);
	    //System.out.println("Closed " + tid + "; isTexture: " + gl.glIsTexture(tid)); // false, and no longer displayed
	    tid = NULL_TID;
    }
}
