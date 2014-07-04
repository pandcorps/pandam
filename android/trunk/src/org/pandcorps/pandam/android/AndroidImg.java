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
package org.pandcorps.pandam.android;

import java.nio.*;

import android.graphics.*;
import android.graphics.Bitmap.*;

import org.pandcorps.core.*;

public final class AndroidImg extends Img {
	private Bitmap raw;
	
	public AndroidImg(final Bitmap raw) {
		this.raw = Config.ARGB_8888 == raw.getConfig() ? raw : getSubbitmap(raw, 0, 0, raw.getWidth(), raw.getHeight());
		//if (Config.ARGB_8888 != raw.getConfig()) {
			//raw.setConfig(Config.ARGB_8888); // NoSuchMethodError
			//raw.reconfigure(raw.getWidth(), raw.getHeight(), Config.ARGB_8888); // NoSuchMethodError
		//}
	}
	
	@Override
	public final Bitmap getRaw() {
		return raw;
	}
	
	@Override
	public final int getWidth() {
		return raw.getWidth();
	}
	
	@Override
	public final int getHeight() {
		return raw.getHeight();
	}
	
	@Override
	public final int getRGB(final int x, final int y) {
		return raw.getPixel(x, y);
	}
	
	@Override
	public final void setRGB(final int x, final int y, final int rgb) {
		try {
			raw.setPixel(x, y, rgb);
		} catch (final IllegalStateException e) { // raw loaded from file, immutable, see factory
			raw = getSubbitmap(raw, 0, 0, getWidth(), getHeight());
			raw.setPixel(x, y, rgb);
		}
	}
	
	@Override
	public final Img getSubimage(final int x, final int y, final int w, final int h) {
		return new AndroidImg(getSubbitmap(raw, x, y, w, h));
	}
	
	private final static Bitmap getSubbitmap(final Bitmap raw, final int x, final int y, final int w, final int h) {
		//return Bitmap.createBitmap(raw, x, y, w, h); // Immutable
		final Bitmap sub = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		for (int j = 0; j < h; j++) {
			final int yj = y + j;
			for (int i = 0; i < w; i++) {
				sub.setPixel(i, j, raw.getPixel(x + i, yj));
			}
		}
		return sub;
	}
	
	@Override
	public final void save(final String location) throws Exception {
		//TODO
	}
	
	@Override
	public ByteBuffer toByteBuffer() {
		final ByteBuffer scratch = ByteBuffer.allocateDirect(getWidth() * getHeight() * 4);
		raw.copyPixelsToBuffer(scratch);
		scratch.rewind();
		return scratch;
	}
	
	@Override
	public final void close() {
		raw.recycle();
	}
	
	@Override
	public final boolean isClosed() {
		return raw.isRecycled();
	}
}
