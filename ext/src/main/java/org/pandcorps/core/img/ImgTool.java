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
package org.pandcorps.core.img;

import java.io.*;
import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.seg.*;

public final class ImgTool {
    private final static ImgFactory f = ImgFactory.getFactory();
    private final static String sep = System.getProperty("file.separator");
    private final static boolean enhanceBlue = Pantil.isProperty("org.pandcorps.core.img.enhanceBlue", false);
    private final static boolean enhanceGreen = Pantil.isProperty("org.pandcorps.core.img.enhanceGreen", false);
    private final static boolean enhanceRed = Pantil.isProperty("org.pandcorps.core.img.enhanceRed", false);
    private final static String mapSrc = Pantil.getProperty("org.pandcorps.core.img.mapSrc");
    private final static String mapDst = Pantil.getProperty("org.pandcorps.core.img.mapDst");
    private final static int grayOffsetRed = Pantil.getProperty("org.pandcorps.core.img.grayOffsetRed", 0);
    private final static int grayOffsetGreen = Pantil.getProperty("org.pandcorps.core.img.grayOffsetGreen", 0);
    private final static int grayOffsetBlue = Pantil.getProperty("org.pandcorps.core.img.grayOffsetBlue", 0);
    private final static float grayMultiplierRed = Pantil.getProperty("org.pandcorps.core.img.grayMultiplierRed", 1.0f);
    private final static float grayMultiplierGreen = Pantil.getProperty("org.pandcorps.core.img.grayMultiplierGreen", 1.0f);
    private final static float grayMultiplierBlue = Pantil.getProperty("org.pandcorps.core.img.grayMultiplierBlue", 1.0f);
    private final static int transparentRed = Pantil.getProperty("org.pandcorps.core.img.transparentRed", -1);
    private final static int transparentGreen = Pantil.getProperty("org.pandcorps.core.img.transparentGreen", -1);
    private final static int transparentBlue = Pantil.getProperty("org.pandcorps.core.img.transparentBlue", -1);
    private final static String lightMapLoc = Pantil.getProperty("org.pandcorps.core.img.lightMapLoc");
    private final static int[] channels = new int[3];
    private static PixelFilter filter = null;
    private static Img lightMap = null;
    
    public final static void main(final String[] args) {
        info("Starting");
        initFilter();
        final String inLoc = args[0];
        final String outLoc = args[1];
        final String mode = Coltil.get(args, 2);
        final File inFile = new File(inLoc);
        if (inLoc.startsWith("NOI")) {
            processNoise(inLoc, outLoc);
        } else if (inLoc.startsWith("WIR")) {
            processWire(inLoc, outLoc);
        } else if ("reduce".equalsIgnoreCase(mode)) {
            reduceColors(inLoc, outLoc);
        } else if (inFile.isDirectory()) {
            processDirectory(inFile, outLoc);
        } else {
            processFile(inFile, outLoc);
        }
        info("Finished");
    }
    
    private final static void initFilter() {
        if (mapSrc != null) {
            final Img src = Imtil.load(mapSrc), dst = Imtil.load(mapDst);
            filter = getFilter(src, dst);
        }
        if (lightMapLoc != null) {
            info("Loading light map " + lightMapLoc);
            lightMap = Imtil.load(lightMapLoc);
        }
    }
    
    private final static void processDirectory(final File inDir, final String outLoc) {
        for (final File inFile : inDir.listFiles()) {
            processFile(inFile, outLoc + sep + inFile.getName());
        }
    }
    
    private final static void processFile(final File inFile, final String outLoc) {
        info("Processing from " + inFile + " into " + outLoc);
        final Img img = Imtil.load(inFile.getAbsolutePath());
        if (filter != null) {
            info("Filtering image");
            Imtil.filterImg(img, filter);
            info("Finished filtering image");
        } else if (lightMap != null) {
            applyLightMap(img, outLoc);
        } else {
            processFile(img, outLoc);
        }
        Imtil.save(img, outLoc);
        img.close();
    }
    
    private final static void processFile(final Img img, final String outLoc) {
        final int w = img.getWidth(), h = img.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int p = img.getRGB(x, y);
                final int r = f.getRed(p), g = f.getGreen(p), b = f.getBlue(p), a = f.getAlpha(p);
                img.setRGB(x, y, maximizeBlue(r, g, b, a));
                //img.setRGB(x, y, greenify(r, g, b, a));
                //img.setRGB(x, y, toCyan(r, g, b, a));
                //img.setRGB(x, y, toCyanGray(r, g, b, a));
                //img.setRGB(x, y, mean(r, g, b, a));
                //img.setRGB(x, y, min(r, g, b, a));
            }
        }
    }
    
    private final static void applyLightMap(final Img img, final String outLoc) {
        info("Applying light map");
        final int w = img.getWidth(), h = img.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int p = img.getRGB(x, y);
                final int r = f.getRed(p), g = f.getGreen(p), b = f.getBlue(p), a = f.getAlpha(p);
                final int l = lightMap.getRGB(x, y);
                final int rl = f.getRed(l), gl = f.getGreen(l), bl = f.getBlue(l);
                final int r2 = applyLightMap(r, rl), g2 = applyLightMap(g, gl), b2 = applyLightMap(b, bl);
                img.setRGB(x, y, f.getDataElement(r2, g2, b2, a));
            }
        }
        info("Finished applying light map");
    }
    
    private final static int applyLightMap(final int cRaw, int cLight) {
        if (cRaw == 0) {
            return cRaw;
        }
        if (cLight == 255) {
            cLight = 256;
        }
        return Math.max(0, Math.min(Pancolor.MAX_VALUE, cRaw + cLight - 128));
    }
    
    protected final static boolean isRed(final int p) {
        return f.getRed(p) > f.getBlue(p);
    }
    
    protected final static boolean isGreen(final int r, final int g, final int b) {
        return (g > r) && (g > b);
    }
    
    protected final static boolean isBrown(final int r, final int g, final int b) {
        return (r > g) && (g > b);
    }
    
    protected final static boolean isGray(final int r, final int g, final int b) {
        return (r == b) && (g == b);
    }
    
    protected final static boolean isTransparent(final int r, final int g, final int b) {
        return (r == transparentRed) && (g == transparentGreen) && (b == transparentBlue);
    }
    
    protected final static int maximizeBlue(int r, int g, int b, int a) {
        if (isGray(r, g, b)) {
            return handleGray(b, a);
        } else if (isGreen(r, g, b)) {
            return handleGreen(r, g, b, a);
        } else if (isBrown(r, g, b)) {
            return handleBrown(r, g, b, a);
        } else if (isTransparent(r, g, b)) {
            return f.getDataElement(0, 0, 0, 0);
        }
        sortChannels(r, g, b);
        r = channels[0]; g = channels[1]; b = channels[2];
        if (enhanceBlue) {
            b = multiplyChannel(b, 1.5f);
        }
        if (enhanceGreen) {
            g = (g + b) / 2;
        }
        if (enhanceRed) {
            r = (r + g) / 2;
        }
        return f.getDataElement(r, g, b, a);
    }
    
    protected final static void sortChannels(final int r, final int g, final int b) {
        channels[0] = r; channels[1] = g; channels[2] = b;
        Arrays.sort(channels);
    }
    
    protected final static int toCyan(final int r, final int g, final int b, final int a) {
        sortChannels(r, g, b);
        final int x = channels[2];
        return f.getDataElement(channels[0], x, x, a);
    }
    
    protected final static int toCyanGray(final int r, final int g, final int b, final int a) {
        sortChannels(r, g, b);
        //final int n = channels[0], x = channels[2];
        //return f.getDataElement(n + (x - n) / 3, x, x, a);
        //int x = channels[2];
        //final int n = multiplyChannel(x, (1.0f / 6.0f));
        //x = multiplyChannel(x, (5.0f / 6.0f));
        //return f.getDataElement(n + (x - n) * 2 / 3, x, x, a);
        final int n = channels[0], x = channels[2], d = x - n, d2 = Math.round(d / 4.0f);
        final int n2 = n + d2, x2 = x - d2;
        return f.getDataElement(n2, x2, x2, a);
    }
    
    protected final static int toCyanGray2(final int r, final int g, final int b, final int a) {
        sortChannels(r, g, b);
        //final int n = channels[0], x = channels[2];
        //return f.getDataElement(n + (x - n) * 2 / 3, x, x, a);
        //int x = channels[2];
        //final int n = multiplyChannel(x, (2.0f / 6.0f));
        //x = multiplyChannel(x, (4.0f / 6.0f));
        //return f.getDataElement(n + (x - n) * 2 / 3, x, x, a);
        final int n = channels[0], x = channels[2], d = x - n, d2 = Math.round(d * 2.0f / 5.0f);
        final int n2 = n + d2, x2 = x - d2;
        return f.getDataElement(n2, x2, x2, a);
    }
    
    protected final static int greenify(final int r, final int g, final int b, final int a) {
        return f.getDataElement((r + g) / 2, g, (b + g) / 2, a);
    }
    
    protected final static int handleGray(final int v, final int a) {
        return f.getDataElement(
                handle(v, grayMultiplierRed, grayOffsetRed),
                handle(v, grayMultiplierGreen, grayOffsetGreen),
                handle(v, grayMultiplierBlue, grayOffsetBlue), a);
    }
    
    protected final static int handleGreen(int r, int g, final int b, final int a) {
        //final float g2 = g * 1.1f;
        g = Math.round(g * 1.1f);
        r = Math.min(r, b);
        if (g > Pancolor.MAX_VALUE) {
            final float m = ((float) g) / Pancolor.MAX_VALUE;
            g = Pancolor.MAX_VALUE;
            r = multiplyChannel(r, m);
        }
        //g = multiplyChannel(g, 1.1f); // Discards overflow, but the above applies it to another channel
        return f.getDataElement(r, g, g, a);
    }
    
    protected final static int handleBrown(final int r, final int g, final int b, final int a) {
        final float darkR = 0.8f * r, darkG = 0.8f * g, darkB = 0.8f * b;
        //final float grayR = (darkR + darkG) / 2.0f, grayB = (darkB + darkG) / 2.0f;
        final float grayR = (0.25f * darkR) + (0.75f * darkG), grayB = (0.25f * darkB) + (0.75f * darkG);
        return f.getDataElement(Math.round(grayR), Math.round(darkG), Math.round(grayB), a);
    }
    
    protected final static int handle(final int v, final float multiplier, final int offset) {
        return Math.max(0, Math.min(Pancolor.MAX_VALUE, Math.round(v * multiplier) + offset));
    }
    
    protected final static int multiplyChannel(final int c, final float m) {
        return Math.min(Pancolor.MAX_VALUE, Math.round(c * m));
    }
    
    protected final static int mean(int r, int g, int b, int a) {
        final int m = (r + g + b) / 3;
        return f.getDataElement(m, m, m, a);
    }
    
    protected final static int min(int r, int g, int b, int a) {
        final int m = Math.min(Math.min(r,  g), b);
        return f.getDataElement(m, m, m, a);
    }
    
    protected final static ReplacePixelFilter getFilter(final Img src, final Img dst) {
        final int w = src.getWidth(), h = src.getHeight();
        final ReplacePixelFilter filter = new ReplacePixelFilter();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int sp = src.getRGB(x, y), dp = dst.getRGB(x, y);
                if (sp != dp) {
                    filter.put(sp, dp);
                }
            }
        }
        return filter;
    }
    
    private final static void processNoise(final String noiseDef, final String outLoc) {
        final Segment seg = Segment.parse(noiseDef);
        final int w = seg.intValue(0), h = seg.intValue(1);
        final List<Field> colorReps = seg.getRepetitions(2);
        final boolean preventNeighborCollisions = seg.getBoolean(3, false);
        final int numColors = colorReps.size();
        final int[] colors = new int[numColors];
        for (int i = 0; i < numColors; i++) {
            final Field color = colorReps.get(i);
            colors[i] = f.getDataElement(color.intValue(0), color.intValue(1), color.intValue(2), Pancolor.MAX_VALUE);
        }
        final int nullMarker = -1; //TODO Pick something outside of colors array
        final Img img = Imtil.newImage(w, h);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int y1 = (y == 0) ? nullMarker : img.getRGB(x, y - 1);
                final int x1 = (x == 0) ? nullMarker : img.getRGB(x - 1, y);
                int rgb;
                while (true) {
                    rgb = Mathtil.randElemI(colors);
                    if (!preventNeighborCollisions || ((rgb != y1) && (rgb != x1))) {
                        break;
                    }
                }
                img.setRGB(x, y, rgb);
            }
        }
        Imtil.save(img, outLoc);
    }
    
    private final static int WIRE_BG = 24;
    private final static int WIRE_MARGIN = 6;
    
    private final static void processWire(final String noiseDef, final String outLoc) {
        final Segment seg = Segment.parse(noiseDef);
        final int w = seg.intValue(0), h = seg.intValue(1), w1 = w - 1, h1 = h - 1;
        final Img img = Imtil.newImage(w, h);
        final int def = f.getDataElement(0, 0, WIRE_BG, Pancolor.MAX_VALUE);
        final int minSize = WIRE_MARGIN * 2 + 1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                img.setRGB(x, y, def);
            }
        }
        boolean dark = true;
        for (int j = 0; j < 4; j++) {
            //final boolean dark = (j % 2) == 1;
            for (int x = 0; x < w; x++) {
                final int yStart = Mathtil.randi(0, h1);
                final int ySize = Mathtil.randi(minSize, h1);
                final int b = getWireBaseChannel(x);
                dark = Mathtil.rand();
                for (int yIndex = 0; yIndex <= ySize; yIndex++) {
                    final int y = (yStart + yIndex) % h;
                    setWireRgb(img, x, y, b, yIndex, ySize, def, dark);
                }
            }
            for (int y = 0; y < h; y++) {
                final int xStart = Mathtil.randi(0, w1);
                final int xSize = Mathtil.randi(minSize, w1);
                final int b = getWireBaseChannel(y);
                dark = Mathtil.rand();
                for (int xIndex = 0; xIndex <= xSize; xIndex++) {
                    final int x = (xStart + xIndex) % w;
                    setWireRgb(img, x, y, b, xIndex, xSize, def, dark);
                }
            }
        }
        Imtil.save(img, outLoc);
    }
    
    private final static int getWireBaseChannel(final int i) {
        //return (((i % 2) == 0) ? Mathtil.randi(8, 12) : Mathtil.randi(13, 17)) * 8; // Designed for one pass
        return (((i % 2) == 0) ? Mathtil.randi(6, 10) : Mathtil.randi(11, 15)) * 8;
    }
    
    private final static int getWireChannel(final int base, final int i, final int size) {
        //TODO noise?
        final int m = Math.min(i, size - i);
        if (m < WIRE_MARGIN) {
            final float wm = WIRE_MARGIN + 1;
            return WIRE_BG + Math.round((base - WIRE_BG) * (m + 1) / wm);
            //TODO palette
        }
        return base;
    }
    
    private final static int getWireRgb(final int base, final int i, final int size) {
        return f.getDataElement(0, 0, getWireChannel(base, i, size), Pancolor.MAX_VALUE);
    }
    
    private final static void setWireRgb(final Img img, final int x, final int y, final int base, final int i, final int size, final int def, final boolean dark) {
        final int oldRgb = img.getRGB(x, y), newRgb;
        final int oldBlue = f.getBlue(oldRgb);
        if (dark) {
            newRgb = f.getDataElement(0, 0, Math.max(oldBlue - 24, 0), Pancolor.MAX_VALUE);
        } else if (oldRgb == def) {
            newRgb = getWireRgb(base, i, size);
        } else {
            final int newDiff = getWireChannel(base, i, size) - WIRE_BG;
            final int oldDiff = oldBlue - WIRE_BG;
            final int newBlue = Math.min(WIRE_BG + ((newDiff + oldDiff) * 3 / 4), Pancolor.MAX_VALUE);
            //TODO Make sure newBlue is within allowed palette
            newRgb = f.getDataElement(0, 0, newBlue, Pancolor.MAX_VALUE);
        }
        img.setRGB(x, y, newRgb);
    }
    
    private final static void reduceColors(final String inLoc, final String outLoc) {
        info("Reducing colors from " + inLoc + " into " + outLoc);
        final Img img = Imtil.load(inLoc);
        final int w = img.getWidth(), h = img.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int rgb = img.getRGB(x, y);
                img.setRGB(x, y, f.getDataElement(reduceColor(f.getRed(rgb)), reduceColor(f.getGreen(rgb)), reduceColor(f.getBlue(rgb)), f.getAlpha(rgb)));
            }
        }
        Imtil.save(img, outLoc);
        img.close();
    }
    
    private final static int reduceColor(final int channel) {
        final float channelFloat = channel;
        final int reduced = Math.round(channelFloat / 8.0f);
        final int rounded = Math.min(Pancolor.MAX_VALUE, reduced * 8);
        return rounded;
    }
    
    private final static void info(final Object s) {
        System.out.println(s);
    }
}
