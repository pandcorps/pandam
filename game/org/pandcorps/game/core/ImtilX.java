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
package org.pandcorps.game.core;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import org.pandcorps.core.Imtil;
import org.pandcorps.core.img.Pancolor;
import org.pandcorps.core.img.ReplacePixelFilter;

public final class ImtilX {
	public final static int DIM = 16;
	public static Pancolor outlineSrc = null;
    public static Pancolor outlineDst = null;
    
	private ImtilX() {
		throw new Error();
	}
	
    public final static BufferedImage loadImage(final String path) {
    	return loadImage(path, null);
    }
    
    public final static BufferedImage loadImage(final String path, final ReplacePixelFilter filter) {
    	return loadImage(path, DIM, filter);
    }
    
    public final static BufferedImage loadImage(final String path, final int dim, ReplacePixelFilter filter) {
        return loadImage(path, dim, filter, true);
    }
    
    public final static BufferedImage loadImage(final String path, int dim, ReplacePixelFilter filter, final boolean validate) {
        BufferedImage img = Imtil.load(path);
        final int h = img.getHeight();
        if (validate) {
            if (h == dim + 1) {
                // During drawing/debugging, there's an extra row at the bottom
                img = img.getSubimage(0, 0, img.getWidth(), dim);
            } else if (h != dim) {
                throw new UnsupportedOperationException("Expected image to have height=" + dim);
            }
        } else {
            dim = h;
        }
        final ColorModel cm = img.getColorModel();
        boolean transparency = false;
        for (int x = 0; x < dim; x++) {
            for (int y = 0; y < dim; y++) {
                final int rgb = img.getRGB(x, y);
                if (cm.getAlpha(rgb) == 0) {
                    transparency = true;
                    break;
                }
            }
        }
        if (!transparency) {
        	filter = ReplacePixelFilter.putToTransparent(filter, img.getRGB(0, 0));
        }
        filter = ReplacePixelFilter.putIfValued(filter, outlineSrc, outlineDst);
        return Imtil.filter(img, filter);
    }
    
    public final static BufferedImage[] loadStrip(final String path) {
    	return loadStrip(path, DIM);
    }
    
    public final static BufferedImage[] loadStrip(final String path, final int dim) {
    	return Imtil.toStrip(loadImage(path, dim, null), dim);
    }
    
    public final static BufferedImage[] loadStrip(final String path, final int w, final boolean validate) {
        return Imtil.toStrip(loadImage(path, w, null, validate), w);
    }
}
