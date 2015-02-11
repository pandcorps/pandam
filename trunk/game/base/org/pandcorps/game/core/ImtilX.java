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
package org.pandcorps.game.core;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;

public final class ImtilX {
	public final static int DIM = 16;
	public static Pancolor outlineSrc = null;
    public static Pancolor outlineDst = null;
    public static boolean validateDefault = true;
    
	private ImtilX() {
		throw new Error();
	}
	
    public final static Img loadImage(final String path) {
    	return loadImage(path, null);
    }
    
    public final static Img loadImage(final String path, final boolean validate) {
    	return loadImage(path, DIM, null, validate);
    }
    
    public final static Img loadImage(final String path, final ReplacePixelFilter filter) {
    	return loadImage(path, DIM, filter);
    }
    
    public final static Img loadImage(final String path, final int dim, ReplacePixelFilter filter) {
        return loadImage(path, dim, filter, validateDefault);
    }
    
    public final static Img loadImage(final String path, int dim, ReplacePixelFilter filter, final boolean validate) {
        Img img = Imtil.load(path);
        final int h = img.getHeight();
        if (validate) {
            if (h == dim + 1) {
                // During drawing/debugging, there's an extra row at the bottom
            	final Img old = img;
                img = img.getSubimage(0, 0, img.getWidth(), dim);
                old.close();
            } else if (h != dim) {
                throw new UnsupportedOperationException("Expected image to have height=" + dim);
            }
        } else {
            dim = h;
        }
        final ImgFactory cm = ImgFactory.getFactory();
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
    
    public final static Img[] loadStrip(final String path) {
    	return loadStrip(path, DIM);
    }
    
    public final static Img[] loadStrip(final String path, final int dim) {
    	final Img img = loadImage(path, dim, null);
    	try {
    		return Imtil.toStrip(img, dim);
    	} finally {
    		img.close();
    	}
    }
    
    public final static Img[] loadStrip(final String path, final int w, final boolean validate) {
    	final Img img = loadImage(path, w, null, validate);
    	try {
    		return Imtil.toStrip(img, w);
    	} finally {
    		img.close();
    	}
    }
    
    public final static Img indent(final Img raw) {
        final int w = raw.getWidth(), h = raw.getHeight();
        final Img img = Imtil.newImage(w, h);
        final ImgFactory cm = ImgFactory.getFactory();
        final int b = cm.getDataElement(new int[] {Pancolor.MIN_VALUE, Pancolor.MIN_VALUE, Pancolor.MIN_VALUE, Pancolor.MAX_VALUE}, 0);
        final int indent = Math.max(1, Math.round(h / 10.0f));
        for (int y = h - 1; y >= 0; y--) {
            for (int x = 0; x < w; x++) {
                final int rgb = raw.getRGB(x, y);
                if (cm.getAlpha(rgb) == 0) {
                    continue;
                }
                int ref;
                if (y < indent) {
                    ref = b;
                } else {
                    ref = raw.getRGB(x, y - indent);
                    if (cm.getAlpha(ref) == 0) {
                        ref = b;
                    } else if (y == h - 1 || cm.getAlpha(raw.getRGB(x, y + 1)) == 0) {
                    	ref = b;
                    } else if (x == 0 || cm.getAlpha(raw.getRGB(x - 1, y)) == 0) {
                    	ref = b;
                    } else if (x == w - 1 || cm.getAlpha(raw.getRGB(x + 1, y)) == 0) {
                    	ref = b;
                    }
                }
                img.setRGB(x, y, ref);
            }
        }
        return img;
    }
    
    public final static void highlight(final Img raw, final int amt) {
        final int w = raw.getWidth(), h = raw.getHeight();
        final ImgFactory cm = ImgFactory.getFactory();
        final int wht = cm.getDataElement(new int[] {Pancolor.MAX_VALUE, Pancolor.MAX_VALUE, Pancolor.MAX_VALUE, Pancolor.MAX_VALUE}, 0);
        for (int x = 0; x < w; x++) {
            int mode = 0, edge = 0, currAmt = amt;
            for (int y = 0; y < h; y++) {
                final int rgb = raw.getRGB(x, y);
                if (mode == 0) {
                    if (cm.getAlpha(rgb) != 0) {
                        edge = rgb;
                        mode++;
                    }
                } else if (mode >= 1) {
                    if (rgb == edge) {
                        if (mode >= 2) {
                            break;
                        }
                    } else {
                        if (cm.getAlpha(rgb) == 0) {
                            break; // Left or right edge, nothing to highlight
                        }
                        raw.setRGB(x, y, wht);
                        currAmt--;
                        if (currAmt == 0) {
                            break;
                        }
                        mode = 2;
                    }
                }
            }
        }
    }
    
    public final static void copyCenter(final Img src, final Img dst) {
        final int sw = src.getWidth(), sh = src.getHeight();
        Imtil.copy(src, dst, 0, 0, sw, sh, (dst.getWidth() - sw) / 2, (dst.getHeight() - sh) / 2, Imtil.COPY_FOREGROUND);
        src.closeIfTemporary();
    }
    
    public final static Img newLeft2(final int d, final Pancolor fill) {
        return newArrow2(d, fill, false, true);
    }
    
    public final static Img newRight2(final int d, final Pancolor fill) {
    	return newArrow2(d, fill, true, true);
    }
    
    public final static Img newUp2(final int d, final Pancolor fill) {
        return newArrow2(d, fill, true, false);
    }
    
    public final static Img newDown2(final int d, final Pancolor fill) {
        return newArrow2(d, fill, false, false);
    }
    
    public final static Img newArrow2(final int d, final Pancolor fill, final boolean pos, final boolean horiz) {
        return newArrow2(d, Pancolor.WHITE, Pancolor.BLACK, fill, pos, horiz);
    }
    
    public final static Img newArrow2(final int d, final Pancolor top, final Pancolor bottom, final Pancolor fill, final boolean pos, final boolean horiz) {
        final Img img = Imtil.newImage(d, d);
        drawArrow2(img, 0, 0, d, top, bottom, fill, pos, horiz);
        return img;
    }
    
    public final static void drawRight2(final Img img, final int x, final int y, final int d, final Pancolor fill) {
        drawArrow2(img, x, y, d, fill, true);
    }
    
    public final static void drawArrow2(final Img img, final int x, final int y, final int d, final Pancolor fill, final boolean right) {
    	drawArrow2(img, x, y, d, fill, right, true);
    }
    
    public final static void drawArrow2(final Img img, final int x, final int y, final int d, final Pancolor fill,
    		final boolean pos, final boolean horiz) {
        drawArrow2(img, x, y, d, Pancolor.WHITE, Pancolor.BLACK, fill, pos, horiz);
    }
    
    public final static void drawArrow2(final Img img, final int _x, final int _y, final int d,
                                        final Pancolor top, final Pancolor bottom, final Pancolor fill,
                                        final boolean _pos, final boolean horiz) {
    	final int t = Imtil.getDataElement(top), b = Imtil.getDataElement(bottom), f = Imtil.getDataElement(fill);
    	final int x, y;
    	final boolean pos;
    	if (horiz) {
    		x = _x;
    		y = _y;
    		pos = _pos;
    	} else {
    		x = _y;
    		y = _x;
    		pos = !_pos; // Image coordinates treat down as positive, but usually we think of up as positive, so switch
    	}
    	final int s = pos ? x : (x + d - 1);
    	for (int j = d - 2; j >= 0; j--) {
    		if (horiz) {
    			img.setRGB(s, y + j, t);
    		} else {
    			img.setRGB(y + j, s, t);
    		}
    	}
    	if (horiz) {
    		img.setRGB(s, y + d - 1, b);
    	} else {
    		img.setRGB(y + d - 1, s, b);
    	}
    	for (int i = 1; i < d; i++) {
    		final int i2 = i / 2, di2 = d - 1 - i2, xi = (pos ? (x + i) : (x + d - i - 1));
    		if (horiz) {
	    		img.setRGB(xi, y + i2, t);
	    		img.setRGB(xi, y + di2, b);
    		} else {
    			img.setRGB(y + i2, xi, t);
	    		img.setRGB(y + di2, xi, b);
    		}
    		for (int j = i2 + 1; j < di2; j++) {
    			if (horiz) {
    				img.setRGB(xi, y + j, f);
    			} else {
    				img.setRGB(y + j, xi, f);
    			}
    		}
    	}
    	//Imtil.setPseudoTranslucent(img); // Must indent first
    }
    
    public final static Img newRectangle(final int w, final int h, final Pancolor fill) {
        final Img img = Imtil.newImage(w, h);
        final int t = Imtil.getDataElement(Pancolor.WHITE), b = Imtil.getDataElement(Pancolor.BLACK), f = Imtil.getDataElement(fill);
        final int w1 = w - 1, h1 = h - 1;
        for (int j = 1; j < h; j++) {
            img.setRGB(0, j, t);
            img.setRGB(w1, h1 - j, b);
        }
        for (int i = 0; i < w; i++) {
            img.setRGB(i, 0, t);
            img.setRGB(w1 - i, h1, b);
        }
        for (int j = 1; j < h1; j++) {
            for (int i = 1; i < w1; i++) {
                img.setRGB(i, j, f);
            }
        }
        return img;
    }
    
    public final static Img newButton(final int w, final int h, final Pancolor fill) {
        final Img img = Imtil.newImage(w, h);
        final int t = Imtil.getDataElement(Pancolor.WHITE), b = Imtil.getDataElement(Pancolor.BLACK);
        final int f = Imtil.getDataElement(fill), c = Imtil.getDataElement(Pancolor.CLEAR);
        final int w1 = w - 1, h1 = h - 1;
        img.setRGB(0, 0, c);
        img.setRGB(0, h1, c);
        img.setRGB(w1, 0, c);
        img.setRGB(w1, h1, c);
        for (int j = 1; j < h1; j++) {
            img.setRGB(0, j, b);
            img.setRGB(1, j, t);
            img.setRGB(w1, h1 - j, b);
        }
        for (int i = 1; i < w1; i++) {
            img.setRGB(i, 0, b);
            img.setRGB(i, 1, t);
            img.setRGB(w1 - i, h1, b);
        }
        for (int j = 2; j < h1; j++) {
            for (int i = 2; i < w1; i++) {
                img.setRGB(i, j, f);
            }
        }
        return img;
    }
}
