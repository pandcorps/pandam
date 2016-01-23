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
package org.pandcorps.core.img;

import java.awt.image.*;
import java.io.*;

import javax.imageio.*;

import org.pandcorps.core.*;

public final class AwtImgFactory extends ImgFactory {
	private final static ColorModel cm = ColorModel.getRGBdefault();
	
	@Override
	public final Img load(final InputStream in) throws Exception {
		return new AwtImg(ImageIO.read(in));
	}
	
	@Override
	public final Img create(final int w, final int h) {
		return new AwtImg(new BufferedImage(w, h, Imtil.TYPE));
	}
	
	@Override
	public final int getDataElement(final int[] rgb, final int i) {
		return cm.getDataElement(rgb, i);
	}
	
	@Override
	public final int getRed(final int rgb) {
		return cm.getRed(rgb);
	}
	
	@Override
	public final int getGreen(final int rgb) {
		return cm.getGreen(rgb);
	}
	
	@Override
	public final int getBlue(final int rgb) {
		return cm.getBlue(rgb);
	}
	
	@Override
	public final int getAlpha(final int rgb) {
		return cm.getAlpha(rgb);
	}
}
