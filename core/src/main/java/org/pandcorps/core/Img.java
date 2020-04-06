/*
Copyright (c) 2009-2020, Andrew M. Martin
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
package org.pandcorps.core;

import java.io.*;
import java.nio.*;

public final class Img implements Closeable {
    private int w;
    private int h;
    private int[] a;
    
	private boolean temp = true;
	//private final Exception openState = new Exception();
	
    public Img(final int w, final int h, final int[] a) {
        this.w = w;
        this.h = h;
        this.a = a;
    }
    
    public Img(final int w, final int h) {
        this(w, h, new int[w * h]);
    }
	
    public final Object getRaw() {
        return a;
    }
    
    public final void swapRaw(final Img o) {
        final int w = o.w, h = o.h, a[] = o.a;
        o.w = this.w; o.h = this.h; o.a = this.a;
        this.w = w; this.h = h; this.a = a;
    }
    
    public final int getWidth() {
        return w;
    }
    
    public final int getHeight() {
        return h;
    }
    
    private final int getIndex(final int x, final int y) {
        return (y * w) + x;
    }
    
    public final int getRGB(final int x, final int y) {
        return a[getIndex(x, y)];
    }
    
    public final void setRGB(final int x, final int y, final int rgb) {
        a[getIndex(x, y)] = rgb;
    }
    
    public final Img getSubimage(final int x, final int y, final int w, final int h) {
        final Img sub = new Img(w, h);
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                System.arraycopy(a, ((j + y) * this.w) + x, sub.a, j * w, w);
            }
        }
        return sub;
    }
    
    public final void save(final String location) throws Exception {
        throw new UnsupportedOperationException();
    }
	
	public ByteBuffer toByteBuffer() {
		final int w = getWidth(), h = getHeight();
		//final ByteBuffer scratch = ByteBuffer.wrap(data.getData());
		final int capacity = w * h * 4;
		final byte[] raster = new byte[capacity];
		final ImgFactory model = ImgFactory.getFactory();
		int i = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				final int pixel = getRGB(x, y);
				raster[i++] = (byte) model.getRed(pixel);
				raster[i++] = (byte) model.getGreen(pixel);
				raster[i++] = (byte) model.getBlue(pixel);
				raster[i++] = (byte) model.getAlpha(pixel);
			}
		}
		
		//final ByteBuffer scratch = ByteBuffer.wrap(raster);
		final ByteBuffer scratch = ByteBuffer.allocateDirect(capacity);
		scratch.put(raster);
		scratch.rewind();
		return scratch;
	}
	
	@Override
    public final void close() {
        a = null;
    }
    
    public final boolean isClosed() {
        return a == null;
    }
	
	public final void setTemporary(final boolean temp) {
		this.temp = temp;
	}
	
	public final void closeIfTemporary() {
		if (temp) {
			close();
		}
	}
	
	@Override
	protected final void finalize() throws Throwable {
		if (!isClosed()) {
			//error("Finalized unclosed Img " + getRaw());
			//openState.printStackTrace();
		}
		//close(); // Should do before gc
		super.finalize();
	}
	
	public final static void setTemporary(final boolean temp, final Img... imgs) {
		if (imgs == null) {
			return;
		}
		for (final Img img : imgs) {
			img.setTemporary(temp);
		}
	}
	
	public final static void close(final Img... imgs) {
		if (imgs == null) {
			return;
		}
		for (final Img img : imgs) {
			if (img != null) {
				img.close();
			}
		}
	}
	
	public final static void close(final Iterable<Img> imgs) {
        if (imgs == null) {
            return;
        }
        for (final Img img : imgs) {
            if (img != null) {
                img.close();
            }
        }
    }
}
