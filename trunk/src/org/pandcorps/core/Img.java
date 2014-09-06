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
package org.pandcorps.core;

import java.io.*;
import java.nio.*;

public abstract class Img implements Closeable {
	private boolean temp = true;
	//private final Exception openState = new Exception();
	
	public abstract Object getRaw();
	
	public abstract int getWidth();
	
	public abstract int getHeight();
	
	public abstract int getRGB(final int x, final int y);
	
	public abstract void setRGB(final int x, final int y, final int rgb);
	
	public abstract Img getSubimage(final int x, final int y, final int w, final int h);
	
	public abstract void save(final String location) throws Exception;
	
	public ByteBuffer toByteBuffer() {
		final int w = getWidth(), h = getHeight();
		//final ByteBuffer scratch = ByteBuffer.wrap(data.getData());
		final int capacity = w * h * 4;
		final byte[] raster = new byte[capacity];
		final ImgFactory model = ImgFactory.getFactory();
		for (int y = 0; y < h; y++) {
			final int row = y * h * 4;
			for (int x = 0; x < w; x++) {
				final int pixel = getRGB(x, y);
				int i = row + (x * 4);
				raster[i++] = (byte) model.getRed(pixel);
				raster[i++] = (byte) model.getGreen(pixel);
				raster[i++] = (byte) model.getBlue(pixel);
				raster[i] = (byte) model.getAlpha(pixel);
			}
		}
		
		//final ByteBuffer scratch = ByteBuffer.wrap(raster);
		final ByteBuffer scratch = ByteBuffer.allocateDirect(capacity);
		scratch.put(raster);
		scratch.rewind();
		return scratch;
	}
	
	@Override
	public abstract void close();
	
	public abstract boolean isClosed();
	
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
			System.err.println("Finalized unclosed Img " + getRaw());
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
}
