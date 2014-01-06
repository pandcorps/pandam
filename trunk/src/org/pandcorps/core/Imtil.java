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

import java.awt.image.*;
import java.io.*;
import java.nio.*;

import javax.imageio.*;

import org.pandcorps.core.img.*;
import org.pandcorps.pandam.*;

// Image Utility
public final class Imtil {
	public final static int TYPE = BufferedImage.TYPE_INT_ARGB;
	
    private Imtil() {
        throw new Error();
    }
    
    public final static BufferedImage load(final String location) {
        InputStream in = null;
        try {
            //return ImageIO.read(getClass().getResource(location));
            //return ImageIO.read(getClass().getClassLoader().getResource(location));
            //return ImageIO.read(Imtil.class.getClassLoader().getResource(location));
            in = Iotil.getInputStream(location);
            return ImageIO.read(in);
        } catch (final IOException e) {
            throw new Panception(e);
        } finally {
            Iotil.close(in);
        }
    }
    
    public final static BufferedImage create(final ByteBuffer buf, final int w, final int h, final int type) {
        if (type != BufferedImage.TYPE_INT_RGB) {
            throw new UnsupportedOperationException("Currently only support INT_RGB");
        }
        //final BufferedImage img = new BufferedImage(w, h, type);
        final BufferedImage img = newImage(w, h); // type only needed to interpret ByteBuffer, we can choose any type for output
        final ColorModel cm = getColorModel();
        final int[] rgba = new int[4];
        rgba[3] = 255;
        for (int y = 0; y < h; y++) {
            final int wy = w * y;
            for (int x = 0; x < w; x++) {
                final int i = (wy + x) * 3;
                for (int j = 0; j < 3; j++) {
                    rgba[j] = buf.get(i + j);
                }
                img.setRGB(x, h - (y + 1), cm.getDataElement(rgba, 0));
            }
        }
        return img;
    }
    
    public final static void save(final BufferedImage img, final String location) {
        try {
            ImageIO.write(img, "png", new File(location));
        } catch (final IOException e) {
            throw new Panception(e);
        }
    }
    
    public final static ColorModel getColorModel() {
    	return ColorModel.getRGBdefault();
    }
    
    //public final static BufferedImage sub(final BufferedImage img, final int x, final int y, final int w, final int h) {
        //final BufferedImage out = newImage(w, h);
    //    return img.getSubimage(x, y, w, h);
    //}
    
    public final static BufferedImage[] loadStrip(final String location, final int w) {
        return toStrip(load(location), w);
    }
    
    public final static BufferedImage[] toStrip(final BufferedImage img, final int w) {
        final int tw = img.getWidth();
        final BufferedImage[] strip = new BufferedImage[tw / w];
        final int h = img.getHeight();
        for (int x = 0, i = 0; x < tw; x += w, i++) {
            strip[i] = img.getSubimage(x, 0, w, h);
        }
        return strip;
    }
    
    public final static BufferedImage toTransparent(final BufferedImage img, final int rgb) {
    	return filter(img, new ReplacePixelFilter(rgb));
    }
    
    public final static BufferedImage copy(final BufferedImage img) {
    	final int iw = img.getWidth(), ih = img.getHeight();
        final BufferedImage out = newImage(iw, ih);
        copy(img, out, 0, 0, iw, ih, 0, 0);
        return out;
    }
    
    public final static void copy(final BufferedImage src, final BufferedImage dst, final int srcX, final int srcY, final int w, final int h, final int dstX, final int dstY) {
    	copy(src, dst, srcX, srcY, w, h, dstX, dstY, COPY_REPLACE);
    }
    
    public final static byte COPY_REPLACE = 0;
    public final static byte COPY_FOREGROUND = 1;
    public final static byte COPY_BACKGROUND = 2;
    
    public final static void copy(final BufferedImage src, final BufferedImage dst, final int srcX, final int srcY, final int w, final int h, final int dstX, final int dstY, final byte mode) {
        copy(src, dst, srcX, srcY, w, h, dstX, dstY, mode == COPY_FOREGROUND ? TransparentPixelMask.getInstance() : null, mode == COPY_BACKGROUND ? VisiblePixelMask.getInstance() : null);
    }
    
    public final static void copy(final BufferedImage src, final BufferedImage dst, final int srcX, final int srcY, final int w, final int h, final int dstX, final int dstY,
                                  final PixelMask srcMask, final PixelMask dstMask) {
    	for (int x = 0; x < w; x++) {
            final int srcCol = srcX + x, dstCol = dstX + x;
            for (int y = 0; y < h; y++) {
                // Different color models?
                final int srcP;
                final int srcRow = srcY + y;
                try {
                    srcP = src.getRGB(srcCol, srcRow);
                } catch (final Exception e) {
                    throw err(src, dst, srcX, srcY, w, h, dstX, dstY, "get", srcCol, srcRow, e);
                }
                if (PixelMask.isMasked(srcMask, srcP)) {
                	continue;
                }
                final int dstRow = dstY + y;
                if (PixelMask.isMasked(dstMask, dst.getRGB(dstCol, dstRow))) {
                	continue;
                }
                try {
                    dst.setRGB(dstCol, dstRow, srcP);
                } catch (final Exception e) {
                    throw err(src, dst, srcX, srcY, w, h, dstX, dstY, "set", dstCol, dstRow, e);
                }
            }
        }
    }
    
    private final static Panception err(final BufferedImage src, final BufferedImage dst, final int srcX, final int srcY, final int w, final int h, final int dstX, final int dstY, final String op, final int errX, final int errY, final Exception e) {
        final StringBuilder b = new StringBuilder();
        b.append("Copying from src whose dimensions are");
        appendDim(b, src);
        b.append("\nStarting at");
        appendCoord(b, srcX, srcY);
        b.append("\nCopying dimensions");
        appendDim(b, w, h);
        b.append("\nCopying to dst whose dimensions are");
        appendDim(b, dst);
        b.append("\nStarting at");
        appendCoord(b, dstX, dstY);
        b.append("\nCould not").append(op);
        b.append("\nAt");
        appendCoord(b, errX, errY);
        throw new Panception(b, e);
    }
    
    private final static StringBuilder appendDim(final StringBuilder b, final BufferedImage img) {
        return appendDim(b, img.getWidth(), img.getHeight());
    }
    
    private final static StringBuilder appendDim(final StringBuilder b, final int w, final int h) {
        return appendPair(b, w, h, '*');
    }
    
    private final static StringBuilder appendCoord(final StringBuilder b, final int x, final int y) {
        return appendPair(b, x, y, ',');
    }
    
    private final static StringBuilder appendPair(final StringBuilder b, final int w, final int h, final char delim) {
        b.append(" (").append(w).append(delim).append(h).append(')');
        return b;
    }
    
    public final static void mirror(final BufferedImage img) {
        final int w = img.getWidth(), w2 = w / 2, h = img.getHeight();
        for (int x = 0; x < w2; x++) {
            for (int y = 0; y < h; y++) {
                final int wx = w - x - 1;
                final int l = img.getRGB(x, y), r = img.getRGB(wx, y);
                img.setRGB(x, y, r);
                img.setRGB(wx, y, l);
            }
        }
    }
    
    public final static BufferedImage filter(final BufferedImage img, final PixelFilter... fs) {
    	return filter(img, Coltil.asList(fs));
    }
    
    public final static BufferedImage filter(final BufferedImage img, final int ox, final int oy, final int w, final int h, final PixelFilter... fs) {
    	return filter(img, ox, oy, w, h, Coltil.asList(fs));
    }
    
    public final static BufferedImage filter(final BufferedImage img, final Iterable<PixelFilter> fs) {
    	return filter(img, 0, 0, img.getWidth(), img.getHeight(), fs);
    }
    
    public final static BufferedImage filter(final BufferedImage img, final int ox, final int oy, final int w, final int h, final Iterable<PixelFilter> fs) {
    	if (Coltil.isEmpty(fs)) {
    		return img;
    	}
        //final ColorModel cm = img.getColorModel();
        //cm.getRGB(inData)
    	final int iw = img.getWidth(), ih = img.getHeight();
        final BufferedImage out = newImage(iw, ih);
        final int sx = ox + w, sy = oy + h;
        for (int x = 0; x < iw; x++) {
            for (int y = 0; y < ih; y++) {
                int p = img.getRGB(x, y);
                if (x >= ox && x < sx && y >= oy && y < sy) {
	                for (final PixelFilter f : fs) {
	                	p = f.filter(p);
	                }
                }
                out.setRGB(x, y, p);
            }
        }
        return out;
    }
    
    public final static short[] getArithmeticMeanColor(final BufferedImage img) {
        final int w = img.getWidth(), h = img.getHeight();
        final ColorModel cm = getColorModel();
        long imgR = 0, imgG = 0, imgB = 0;
        int total = 0;
        for (int y = 0; y < h; y++) {
            // Worried about overflow for huge images, but won't work if we skip transparent
            //long rowR = 0, rowG = 0, rowB = 0;
            for (int x = 0; x < w; x++) {
                final int pixel = img.getRGB(x, y);
                if (cm.getAlpha(pixel) == 0) {
                    continue;
                }
                //rowR += cm.getRed(pixel);
                imgR += cm.getRed(pixel);
                imgG += cm.getGreen(pixel);
                imgB += cm.getBlue(pixel);
                total++;
            }
            //imgR += rowR / w;
        }
        //imgR /= h;
        imgR /= total;
        imgG /= total;
        imgB /= total;
        return new short[] {(short) imgR, (short) imgG, (short) imgB};
    }
    
    public final static float colorToFloat(final short b) {
        return b / 255f;
    }
    
    public final static double colorToDouble(final short b) {
        return b / 255.0;
    }
    
    public final static BufferedImage recolor(final BufferedImage in, final short[] base) {
        return recolor(in, colorToDouble(base[0]), colorToDouble(base[1]), colorToDouble(base[2]));
    }
    
    public final static BufferedImage recolor(final BufferedImage in, final double baseRed, final double baseGreen, final double baseBlue) {
        final double exponentRed = getExponent(baseRed);
        final double exponentGreen = getExponent(baseGreen);
        final double exponentBlue = getExponent(baseBlue);
        
        final int rgba[] = new int[4];
        final ColorModel cm = getColorModel();
        // img.getColorModel might be indexed, and our recolored version might not work very well with the limited palette.
        // So, we always use the more flexible model.
        // That's also why the output is always a .png regardless of the input.
        //ColorModel model = img.getColorModel();

        final int w = in.getWidth(), h = in.getHeight();
        final BufferedImage out = newImage(w, h);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int pixel = in.getRGB(x, y);
                final double avg = (cm.getRed(pixel) + cm.getGreen(pixel) + cm.getBlue(pixel)) / 3.0 / 255.0;
                rgba[0] = getComponent(avg, exponentRed);
                rgba[1] = getComponent(avg, exponentGreen);
                rgba[2] = getComponent(avg, exponentBlue);
                rgba[3] = cm.getAlpha(pixel);
                out.setRGB(x, y, cm.getDataElement(rgba, 0));
            }
        }

        return out;
    }
    
    private final static double getExponent(double constant) {
        /*
        f(x) = x^m
        f(0) = 0
        f(1) = 1
        f(.5) = k = .5^m
        m = log.5(k)
        m = ln(k) / ln(.5)
        */

        if (constant < 0.01) {
            constant = 0.01;
        } else if (constant > 0.99) {
            constant = 0.99;
        }

        return Math.log(constant) / Math.log(.5);
    }
    
    private final static int getComponent(double avg, double exponent) {
        return Math.min(255, Math.max(0, (int) (Math.pow(avg, exponent) * 255.0)));
    }
    
    public final static BufferedImage drawRectangle(final BufferedImage in,
    		final int x, final int y, final int w, final int h,
    		final short r, final short g, final short b, final short a) {
    	//TODO copy if not already right ColorModel
    	final ColorModel cm = getColorModel();
    	final int[] rgba = {r, g, b, a};
    	final int c = cm.getDataElement(rgba, 0);
    	for (int j = y + h - 1; j >= y; j--) {
    		for (int i = x + w - 1; i >= x; i--) {
    			in.setRGB(i, j, c);
    		}
    	}
    	return in;
    }
    
    public final static BufferedImage shrink(final BufferedImage in, final int f) {
    	final int w = in.getWidth() / f, h = in.getHeight() / f;
        final BufferedImage out = newImage(w, h);
        for (int j = 0; j < h; j++) {
        	final int jf = j * f;
        	for (int i = 0; i < w; i++) {
        		out.setRGB(i, j, in.getRGB(i * f, jf));
        	}
        }
        return out;
    }
    
    private final static BufferedImage newImage(final int w, final int h) {
        return new BufferedImage(w, h, TYPE);
    }
}
