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
package org.pandcorps.core;

import java.io.*;
import java.nio.*;

import org.pandcorps.core.img.*;
import org.pandcorps.pandam.*;

// Image Utility
public final class Imtil {
	public static boolean onlyResources = false;
	
	private final static ImgFactory cm = ImgFactory.getFactory();
	
    private Imtil() {
        throw new Error();
    }
    
    public final static Img load(final File file) {
        return load(file.getAbsolutePath());
    }
    
    public final static Img load(final String location) {
        InputStream in = null;
        try {
            in = onlyResources ? Iotil.getResourceInputStream(location) : Iotil.getInputStream(location);
            return cm.load(in);
        } catch (final Exception e) {
            throw new Panception("Could not load " + location, e);
        } finally {
            Iotil.close(in);
        }
    }
    
    public final static Img load(final String location, final boolean temp) {
        final Img img = load(location);
        img.setTemporary(temp);
        return img;
    }
    
    public final static Img create(final IntBuffer buf, final int w, final int h) {
        final int[] a = new int[w * h];
        for (int y = 0; y < h; y++) {
            buf.get(a, (h - (y + 1)) * w, w);
        }
        return new Img(w, h, a);
    }
    
    public final static void save(final Img img, final String location) {
        try {
            img.save(location);
        } catch (final Exception e) {
            throw Panception.get(e);
        }
    }
    
    public final static Img[] loadStrip(final String location, final int w) {
        return loadStrip(location, w, true);
    }
    
    public final static Img[] loadStrip(final String location, final int w, final boolean temp) {
        return toStrip(load(location), w, temp);
    }
    
    public final static Img[] toStrip(final Img img, final int w) {
        return toStrip(img, w, true);
    }
    
    public final static Img[] toStrip(final Img img, final int w, final boolean temp) {
        final int tw = img.getWidth();
        final Img[] strip = new Img[tw / w];
        final int h = img.getHeight();
        for (int x = 0, i = 0; x < tw; x += w, i++) {
            final Img s = img.getSubimage(x, 0, w, h);
            s.setTemporary(temp);
            strip[i] = s;
        }
        img.closeIfTemporary();
        return strip;
    }
    
    public final static Img toTransparent(final Img img, final int rgb) {
    	return filter(img, new ReplacePixelFilter(rgb));
    }
    
    public final static Img copy(final Img img) {
    	final int iw = img.getWidth(), ih = img.getHeight();
        final Img out = newImage(iw, ih);
        copy(img, out, 0, 0, iw, ih, 0, 0);
        return out;
    }
    
    public final static Img[] copy(final Img[] imgs) {
        final int size = imgs.length;
        final Img[] outs = new Img[size];
        for (int i = 0; i < size; i++) {
            outs[i] = copy(imgs[i]);
        }
        return outs;
    }
    
    public final static void copy(final Img src, final Img dst, final int srcX, final int srcY, final int w, final int h, final int dstX, final int dstY) {
    	copy(src, dst, srcX, srcY, w, h, dstX, dstY, COPY_REPLACE);
    }
    
    public final static byte COPY_REPLACE = 0;
    public final static byte COPY_FOREGROUND = 1;
    public final static byte COPY_BACKGROUND = 2;
    
    public final static void copy(final Img src, final Img dst, final int srcX, final int srcY, final int w, final int h, final int dstX, final int dstY, final byte mode) {
        copy(src, dst, srcX, srcY, w, h, dstX, dstY, mode == COPY_FOREGROUND ? TransparentPixelMask.getInstance() : null, mode == COPY_BACKGROUND ? VisiblePixelMask.getInstance() : null);
    }
    
    public final static void copy(final Img src, final Img dst, final int srcX, final int srcY, final int w, final int h, final int dstX, final int dstY,
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
                if (PixelMask.isMasked(srcMask, srcCol, srcRow, srcP)) {
                	continue;
                }
                final int dstRow = dstY + y;
                if (PixelMask.isMasked(dstMask, dstCol, dstRow, dst.getRGB(dstCol, dstRow))) {
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
    
    private final static Panception err(final Img src, final Img dst, final int srcX, final int srcY, final int w, final int h, final int dstX, final int dstY, final String op, final int errX, final int errY, final Exception e) {
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
        b.append("\nCould not ").append(op);
        b.append("\nAt");
        appendCoord(b, errX, errY);
        throw new Panception(b, e);
    }
    
    private final static StringBuilder appendDim(final StringBuilder b, final Img img) {
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
    
    public final static void mirror(final Img img) {
        final int w = img.getWidth(), w2 = w / 2, h = img.getHeight();
        for (int x = 0; x < w2; x++) {
            final int wx = w - x - 1;
            for (int y = 0; y < h; y++) {
                final int l = img.getRGB(x, y), r = img.getRGB(wx, y);
                img.setRGB(x, y, r);
                img.setRGB(wx, y, l);
            }
        }
    }
    
    public final static void flip(final Img img) {
        final int w = img.getWidth(), h = img.getHeight(), h2 = h / 2;
        for (int y = 0; y < h2; y++) {
            final int hy = h - y - 1;
            for (int x = 0; x < w; x++) {
                final int t = img.getRGB(x, y), b = img.getRGB(x, hy);
                img.setRGB(x, y, b);
                img.setRGB(x, hy, t);
            }
        }
    }
    
    // 90 degrees clockwise
    public final static void rotate(final Img img) {
        final int d = img.getWidth(), d2 = d / 2, d1 = d - 1;
        if (d != img.getHeight()) {
            throw new IllegalArgumentException("Can only rotate squares");
        }
        for (int j = 0; j < d2; j++) {
            final int end1 = d - j - 1;
            for (int i = j; i < end1; i++) {
                final int t = img.getRGB(i, j);
                img.setRGB(i, j, img.getRGB(j, d1 - i));
                img.setRGB(j, d1 - i, img.getRGB(d1 - i, end1));
                img.setRGB(d1 - i, end1, img.getRGB(end1, i));
                img.setRGB(end1, i, t);
            }
        }
    }
    
    public final static void move(final Img img, final int x, final int y) {
    	final int clear = img.getRGB(0, 0);
    	final int w = img.getWidth(), h = img.getHeight();
    	final int mw = w - Math.abs(x), mh = h - Math.abs(y);
    	for (int i = 0; i < mw; i++) {
    		for (int j = 0; j < mh; j++) {
    			final int srcX, dstX, srcY, dstY;
    			if (x <= 0) {
    				dstX = i;
    				srcX = i - x;
    			} else {
    				dstX = w - i - 1;
    				srcX = w - i - 1 - x;
    			}
    			if (y <= 0) {
    				dstY = j;
    				srcY = j - y;
    			} else {
    				dstY = h - j - 1;
    				srcY = h - j - 1 - y;
    			}
    			img.setRGB(dstX, dstY, img.getRGB(srcX, srcY));
    		}
    	}
    	if (x != 0) {
	    	drawRectangle(img, (x > 0) ? 0 : mw, 0, Math.abs(x), h, clear);
    	}
    	if (y != 0) {
	    	drawRectangle(img, (x > 0) ? x : 0, (y > 0) ? 0 : mh, mw, Math.abs(y), clear);
    	}
    }
    
    public final static void xor(final Img in1, final Img in2, final Img out) {
        final int w = in1.getWidth(), h = in1.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int p1 = in1.getRGB(x, y), p2 = in2.getRGB(x, y);
                final int r = cm.getRed(p1) ^ cm.getRed(p2);
                final int g = cm.getGreen(p1) ^ cm.getGreen(p2);
                final int b = cm.getBlue(p1) ^ cm.getBlue(p2);
                final int a1 = cm.getAlpha(p1), a2 = cm.getAlpha(p2);
                final int a;
                if (a1 == a2) {
                    a = a1;
                } else {
                    throw new IllegalStateException("Expected alpha to match but did not at (" + x + ", " + y + ")");
                }
                out.setRGB(x, y, cm.getDataElement(r, g, b, a));
            }
        }
    }
    
    public final static Img addBorders(final Img in, final int left, final int right, final int top, final int btm) {
        return addBorders(in, left, right, top, btm, null);
    }
    
    public final static Img addBorders(final Img in, final int left, final int right, final int top, final int btm, final Pancolor c) {
    	final int inw = in.getWidth(), inh = in.getHeight();
    	final int outw = inw + left + right, outh = inh + top + btm;
    	final Img out = newImage(outw, outh);
    	if (c != null) {
    	    final int d = getDataElement(c);
    	    drawRectangle(out, 0, 0, left, outh, d);
    	    drawRectangle(out, outw - right, 0, right, outh, d);
    	    drawRectangle(out, left, 0, inw, top, d);
    	    drawRectangle(out, left, outh - btm, inw, btm, d);
    	}
    	copy(in, out, 0, 0, inw, inh, left, top);
    	return out;
    }
    
    public final static void addBordersImg(final Img in, final int left, final int right, final int top, final int btm) {
    	final Img t = addBorders(in, left, right, top, btm);
    	in.swapRaw(t);
    	t.close();
    }
    
    public final static Img filter(final Img img, final PixelFilter... fs) {
    	return filter(img, Coltil.asList(fs));
    }
    
    public final static Img filter(final Img img, final PixelMask mask, final PixelFilter... fs) {
    	return filter(img, 0, 0, img.getWidth(), img.getHeight(), mask, Coltil.asList(fs));
    }
    
    public final static Img filter(final Img img, final int ox, final int oy, final int w, final int h, final PixelFilter... fs) {
    	return filter(img, ox, oy, w, h, Coltil.asList(fs));
    }
    
    public final static Img filter(final Img img, final Iterable<PixelFilter> fs) {
    	return filter(img, 0, 0, img.getWidth(), img.getHeight(), fs);
    }
    
    public final static Img filter(final Img img, final int ox, final int oy, final int w, final int h, final Iterable<PixelFilter> fs) {
    	return filter(img, ox, oy, w, h, null, fs);
    }
    
    public final static Img filter(final Img img, final int ox, final int oy, final int w, final int h, final PixelMask mask, final Iterable<PixelFilter> fs) {
    	if (Coltil.isEmpty(fs)) {
    		return img;
    	}
        //final ColorModel cm = img.getColorModel();
        //cm.getRGB(inData)
    	final int iw = img.getWidth(), ih = img.getHeight();
        final Img out = newImage(iw, ih);
        filter(img, out, ox, oy, w, h, mask, fs);
        return out;
    }
    
    private final static void filter(final Img img, final Img out, final int ox, final int oy, final int w, final int h, final PixelMask mask, final Iterable<PixelFilter> fs) {
    	if (Coltil.isEmpty(fs)) {
    		return;
    	}
    	final int iw = img.getWidth(), ih = img.getHeight();
        final int sx = ox + w, sy = oy + h;
        PixelFilter maskedFilter = (mask == null) ? null : mask.getMaskedFilter();
        for (final PixelFilter f : fs) {
            if (maskedFilter != null) {
                break;
            }
            maskedFilter = f.getMaskedFilter();
        }
        for (int x = 0; x < iw; x++) {
            for (int y = 0; y < ih; y++) {
                int p = img.getRGB(x, y);
                if (x >= ox && x < sx && y >= oy && y < sy) {
                	if (PixelMask.isMasked(mask, x, y, p)) {
                	    if (maskedFilter != null) {
                	        p = maskedFilter.filter(p);
                	    }
                	} else {
		                for (final PixelFilter f : fs) {
		                	p = f.filter(p);
		                }
                	}
                }
                out.setRGB(x, y, p);
            }
        }
        if (img != out) {
        	img.closeIfTemporary();
        }
    }
    
    public final static void filterImg(final Img img, final PixelFilter... fs) {
        filterImg(img, 0, 0, img.getWidth(), img.getHeight(), fs);
    }
    
    public final static void filterImg(final Img img, final int ox, final int oy, final int w, final int h, PixelFilter... fs) {
    	filterImg(img, ox, oy, w, h, null, fs);
    }
    
    public final static void filterImg(final Img img, final int ox, final int oy, final int w, final int h, final PixelMask mask, PixelFilter... fs) {
    	filterImg(img, ox, oy, w, h, mask, Coltil.asList(fs));
    }
    
    public final static void filterImg(final Img img, final int ox, final int oy, final int w, final int h, final PixelMask mask, final Iterable<PixelFilter> fs) {
    	filter(img, img, ox, oy, w, h, mask, fs);
    }
    
    public final static short[] getArithmeticMeanColor(final Img img) {
        final int w = img.getWidth(), h = img.getHeight();
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
    
    public final static Img recolor(final Img in, final short[] base) {
        return recolor(in, colorToDouble(base[0]), colorToDouble(base[1]), colorToDouble(base[2]));
    }
    
    public final static Img recolor(final Img in, final double baseRed, final double baseGreen, final double baseBlue) {
        final double exponentRed = getExponent(baseRed);
        final double exponentGreen = getExponent(baseGreen);
        final double exponentBlue = getExponent(baseBlue);
        
        // img.getColorModel might be indexed, and our recolored version might not work very well with the limited palette.
        // So, we always use the more flexible model.
        // That's also why the output is always a .png regardless of the input.
        //ColorModel model = img.getColorModel();

        final int w = in.getWidth(), h = in.getHeight();
        final Img out = newImage(w, h);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int pixel = in.getRGB(x, y);
                final double avg = (cm.getRed(pixel) + cm.getGreen(pixel) + cm.getBlue(pixel)) / 3.0 / 255.0;
                out.setRGB(x, y, cm.getDataElement(getComponent(avg, exponentRed), getComponent(avg, exponentGreen), getComponent(avg, exponentBlue), cm.getAlpha(pixel)));
            }
        }
        in.closeIfTemporary();

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
    
    public final static void setPseudoTranslucent(final Img in) {
    	final int w = in.getWidth(), h = in.getHeight(), t = cm.getDataElement(Pancolor.MIN_VALUE, Pancolor.MIN_VALUE, Pancolor.MIN_VALUE, Pancolor.MIN_VALUE);
    	for (int j = 0; j < h; j++) {
    		for (int i = j % 2; i < w; i += 2) {
    			in.setRGB(i, j, t);
    		}
    	}
    }
    
    public final static Img clear(final Img in, final short r, final short g, final short b, final short a) {
        return drawRectangle(in, 0, 0, in.getWidth(), in.getHeight(), r, g, b, a);
    }
    
    public final static Img drawRectangle(final Img in,
    		final int x, final int y, final int w, final int h,
    		final short r, final short g, final short b, final short a) {
    	//TODO copy if not already right ColorModel
    	return drawRectangle(in, x, y, w, h, cm.getDataElement(r, g, b, a));
    }
    
    public final static Img drawRectangle(final Img in,
    		final int x, final int y, final int w, final int h, final int c) {
    	for (int j = y + h - 1; j >= y; j--) {
    		for (int i = x + w - 1; i >= x; i--) {
    			in.setRGB(i, j, c);
    		}
    	}
    	return in;
    }
    
    public final static Img drawDiamond(final Img in,
                                        final Pancolor top, final Pancolor bottom, final Pancolor fill) {
        final int d = in.getHeight();
        if (d != in.getWidth()) {
            throw new UnsupportedOperationException();
        }
        final int cmax = d / 2, cmin = (d % 2) == 0 ? (cmax - 1) : cmax;
        final int t = getDataElement(top), b = getDataElement(bottom), f = fill == null ? 0 : getDataElement(fill);
        for (int i = 0; i <= cmin; i++) {
            final int d2i = cmax + i;
            for (int temp = 0; temp < 2; temp++) {
                final int x = (temp == 0) ? i : (d - i - 1);
                in.setRGB(x, d2i, b);
                in.setRGB(x, cmin - i, t);
                if (fill != null) {
                    for (int j = cmin - i + 1; j < d2i; j++) {
                        in.setRGB(x, j, f);
                    }
                }
            }
        }
        return in;
    }
    
    public final static Img drawCircle(final Img in,
            final short r, final short g, final short b, final short a) {
        final Pancolor c = new FinPancolor(r, g, b, a);
        return drawCircle(in, c, c, null);
    }
    
    public final static Img drawCircle(final Img in,
            final Pancolor top, final Pancolor bottom, final Pancolor fill) {
        final int t = getDataElement(top), b = getDataElement(bottom), f = fill == null ? 0 : getDataElement(fill);
        return drawCircle(in, t, b, f, fill != null);
    }
    
    public final static Img drawCircle(final Img in,
            final int t, final int b, final int f, final boolean fill) {
        // x ^ 2 + y ^ 2 = r ^ 2
        // y = sqrt(r ^ 2 - x ^ 2)
        final int d = in.getHeight();
        if (d != in.getWidth()) {
            throw new UnsupportedOperationException();
        }
        final int cmax = d / 2, cmin = (d % 2) == 0 ? (cmax - 1) : cmax, r2 = cmin * cmin;
        // If diameter is 99, center is 49, radius of 49 puts top at 49 + 49 = 98 (top pixel, since they run 0-98)
        // If d is 100, r is 49, cmax runs 50-99, cmin runs 0-49
        
        for (int i = 0; ; i++) {
            final int j = (int) Math.round(Math.sqrt(r2 - (i * i)));
            final int cmaxi = cmax + i, cmini = cmin - i;
            drawCircle8(in, cmaxi, cmini, cmax + j, cmin - j, b, t);
            if (i > j) {
                break;
            }
            if (fill) {
                for (int j2 = i; j2 < j; j2++) {
                    drawCircle8(in, cmaxi, cmini, cmax + j2, cmin - j2, f, f);
                }
            }
        }
        return in;
    }
    
    private final static void drawCircle8(final Img in, final int cmaxi, final int cmini, final int cmaxj, final int cminj, final int b, final int t) {
        in.setRGB(cmaxi, cmaxj, b); // Bottom-right
        in.setRGB(cmaxj, cmaxi, b);
        in.setRGB(cmini, cmaxj, b); // Bottom-left
        in.setRGB(cminj, cmaxi, b);
        in.setRGB(cmini, cminj, t); // Top-left
        in.setRGB(cminj, cmini, t);
        in.setRGB(cmaxi, cminj, t); // Top-right
        in.setRGB(cmaxj, cmini, t);
    }
    
    public final static void drawShadow(final Img in, final Pancolor base, final Pancolor shadow, final int x, final int y, final int w, final int h) {
        final int b = getDataElement(base), s = getDataElement(shadow);
        final int w1 = w - 1, h1 = h - 1;
        for (int j = 0; j < h1; j++) {
            final int yj = y + j, yj1 = yj + 1;
            for (int i = 0; i < w1; i++) {
                final int xi = x + i, xi1 = xi + 1;
                if (in.getRGB(xi, yj) == b) {
                    if (in.getRGB(xi, yj1) != b) {
                        in.setRGB(xi, yj1, s);
                    }
                    if (in.getRGB(xi1, yj) != b) {
                        in.setRGB(xi1, yj, s);
                    }
                    if (in.getRGB(xi1, yj1) != b) {
                        in.setRGB(xi1, yj1, s);
                    }
                }
            }
        }
    }
    
    public final static Img shrink(final Img in, final int f) {
        if (f == 1) {
            return in;
        }
    	final int w = in.getWidth() / f, h = in.getHeight() / f;
        final Img out = newImage(w, h);
        for (int j = 0; j < h; j++) {
        	final int jf = j * f;
        	for (int i = 0; i < w; i++) {
        		out.setRGB(i, j, in.getRGB(i * f, jf));
        	}
        }
        in.closeIfTemporary();
        return out;
    }
    
    public final static Img newImage(final int w, final int h) {
        return cm.create(w, h);
    }
    
    public final static int getDataElement(final Pancolor c) {
        return getDataElement(c.getR(), c.getG(), c.getB(), c.getA());
    }
    
    public final static int getDataElement(final short r, final short g, final short b, final short a) {
        return cm.getDataElement(r, g, b, a);
    }
    
    public final static FinPancolor toColor(final int pixel) {
        return new FinPancolor((short) cm.getRed(pixel), (short) cm.getGreen(pixel), (short) cm.getBlue(pixel), (short) cm.getAlpha(pixel));
    }
    
    public final static FinPancolor getColor(final Img img, final int x, final int y) {
        return toColor(img.getRGB(x, y));
    }
}
