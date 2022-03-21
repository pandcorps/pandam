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
import java.util.Map.*;

import org.pandcorps.core.*;
import org.pandcorps.core.col.*;
import org.pandcorps.core.seg.*;

public final class ImgTool {
    private final static int ALPHA_OPAQUE = Pancolor.MAX_VALUE;
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
    private final static int expectedWidth = Pantil.getProperty("org.pandcorps.core.img.expectedWidth", -1);
    private final static int expectedHeight = Pantil.getProperty("org.pandcorps.core.img.expectedHeight", -1);
    private final static boolean debugMode = Pantil.isProperty("org.pandcorps.core.img.debug", false);
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
        } else if (inLoc.startsWith("GRD")) {
            processGradient(inLoc, outLoc);
        } else if (inLoc.startsWith("WIR")) {
            processWire(inLoc, outLoc);
        } else if ("reduce".equalsIgnoreCase(mode)) {
            reduceColors(inLoc, outLoc);
        } else if ("pad".equalsIgnoreCase(mode)) {
            pad(inLoc, outLoc);
        } else if ("param".equalsIgnoreCase(mode)) {
            // a2,a2,a2 stretch brighten 0.025*x,0.25*x,x
            processParameterized(inLoc, outLoc, args);
        } else if ("map".equalsIgnoreCase(mode)) {
            applyPaletteMap(inLoc, outLoc, args[3], args[4]);
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
        final File outDir = new File(outLoc);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        for (final File inFile : inDir.listFiles()) {
            processFile(inFile, outLoc + sep + inFile.getName());
        }
    }
    
    private final static void processFile(final File inFile, final String outLoc) {
        info("Processing from " + inFile + " into " + outLoc);
        final Img img = Imtil.load(inFile);
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
        return clamp(cRaw + cLight - 128);
    }
    
    private final static int clamp(final int c) {
        return Math.max(0, Math.min(Pancolor.MAX_VALUE, c));
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
        /*if (isGray(r, g, b)) {
            return handleGray(b, a);
        }else if (isGreen(r, g, b)) {
            return handleGreen(r, g, b, a);
        } else if (isBrown(r, g, b)) {
            return handleBrown(r, g, b, a);
        } else if (isTransparent(r, g, b)) {
            return f.getDataElement(0, 0, 0, 0);
        }*/
        /*if ((g == 0) && (b == 0)) {
            return f.getDataElement(0, r, r, a);
        }
        return f.getDataElement(0, 0, 0, a);*/
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
        //return f.getDataElement(r, g, b, a);
        return f.getDataElement(0, 0, b, a);
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
        return clamp(Math.round(v * multiplier) + offset);
    }
    
    protected final static int multiplyChannel(final int c, final float m) {
        return clamp(Math.round(c * m));
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
            colors[i] = getOpaque(color.intValue(0), color.intValue(1), color.intValue(2));
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
    
    private final static void processGradient(final String gradientDef, final String outLoc) {
        final Segment seg = Segment.parse(gradientDef);
        final int baseR = seg.intValue(0), baseG = seg.intValue(1), baseB = seg.intValue(2);
        final int offR = seg.intValue(3), offG = seg.intValue(4), offB = seg.intValue(5);
        final int n = seg.intValue(6);
        int r = baseR, g = baseG, b = baseB;
        final Img img = Imtil.newImage(n, 1);
        int x = 0;
        while (true) {
            img.setRGB(x, 0, getOpaque(r, g, b));
            x++;
            if (x >= n) {
                break;
            }
            r = clamp(r + offR); g = clamp(g + offG); b = clamp(b + offB);
        }
        Imtil.save(img, outLoc);
    }
    
    private final static int WIRE_BG = 24;
    private final static int WIRE_MARGIN = 6;
    
    private final static void processWire(final String noiseDef, final String outLoc) {
        final Segment seg = Segment.parse(noiseDef);
        final int w = seg.intValue(0), h = seg.intValue(1), w1 = w - 1, h1 = h - 1;
        final Img img = Imtil.newImage(w, h);
        final int def = getOpaque(0, 0, WIRE_BG);
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
        return getOpaque(0, 0, getWireChannel(base, i, size));
    }
    
    private final static void setWireRgb(final Img img, final int x, final int y, final int base, final int i, final int size, final int def, final boolean dark) {
        final int oldRgb = img.getRGB(x, y), newRgb;
        final int oldBlue = f.getBlue(oldRgb);
        if (dark) {
            newRgb = getOpaque(0, 0, Math.max(oldBlue - 24, 0));
        } else if (oldRgb == def) {
            newRgb = getWireRgb(base, i, size);
        } else {
            final int newDiff = getWireChannel(base, i, size) - WIRE_BG;
            final int oldDiff = oldBlue - WIRE_BG;
            final int newBlue = clamp(WIRE_BG + ((newDiff + oldDiff) * 3 / 4));
            //TODO Make sure newBlue is within allowed palette
            newRgb = getOpaque(0, 0, newBlue);
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
        //final int div = 4;
        final int div = 8;
        final float divFloat = div;
        final int reduced = Math.round(channelFloat / divFloat);
        final int rounded = clamp(reduced * div);
        return rounded;
    }
    
    private final static void pad(final String inLoc, final String outLoc) {
        info("Padding image from " + inLoc + " into " + outLoc);
        final Img in = Imtil.load(inLoc);
        final Img out = Imtil.copy(in); // Don't write to original Img, or will end up padding the padding
        final int w = in.getWidth(), h = in.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int p = in.getRGB(x, y);
                if (f.getAlpha(p) != 0) {
                    continue; // Don't need to pad if already is visible
                } else if (pad(in, out, x, y, p, -1, 0)) { //TODO Could try all directions and average all visible returned values
                    continue;
                } else if (pad(in, out, x, y, p, 1, 0)) {
                    continue;
                } else if (pad(in, out, x, y, p, 0, 1)) {
                    continue;
                } else if (pad(in, out, x, y, p, 0, -1)) {
                    continue;
                }
            }
        }
        Imtil.save(out, outLoc);
        in.close();
        out.close();
    }
    
    private final static boolean pad(final Img in, final Img out, final int x, final int y, final int p, final int xOff, final int yOff) {
        final int xn = x + xOff, yn = y + yOff;
        if (!isValidCoordinates(xn, yn, in)) {
            return false;
        }
        final int n = in.getRGB(xn, yn);
        if (f.getAlpha(n) == 0) {
            return false; // Neighbor not visible, so no need to pad this pixel
        }
        out.setRGB(x, y, n); //TODO Pick padding color based on local trend (if getting darker near edges, padding color should be darker than neighbor
        return true;
    }
    
    private final static void processParameterized(final String inLoc, final String outLoc, final String[] xforms) {
        final File inFile = new File(inLoc);
        if (inFile.isDirectory()) {
            final File outFile = new File(outLoc);
            for (final File childFile : inFile.listFiles()) {
                if (childFile.isDirectory()) {
                    continue;
                }
                processParameterized(childFile, new File(outFile, childFile.getName()).getAbsolutePath(), xforms);
            }
        } else {
            processParameterized(inFile, outLoc, xforms);
        }
    }
    
    private final static void processParameterized(final File inFile, final String outLoc, final String[] xforms) {
        if (Iotil.exists(outLoc)) {
            info(outLoc + " already exists, skipping");
            return;
        }
        final String inLoc = inFile.getAbsolutePath();
        if (!inLoc.endsWith(".png")) {
            info(inLoc + " is not a png, skipping");
            return;
        }
        info("Processing " + inLoc);
        final Img img = Imtil.load(inLoc);
        if ((expectedWidth > 0) && (expectedWidth != img.getWidth())) {
            throw new IllegalStateException("Expected width " + expectedWidth + " but found " + img.getWidth() + " for " + inLoc);
        } else if ((expectedHeight > 0) && (expectedHeight != img.getHeight())) {
            throw new IllegalStateException("Expected height " + expectedHeight + " but found " + img.getHeight() + " for " + inLoc);
        }
        final int size = xforms.length;
        for (int i = 3; i < size; i++) {
            processParameterized(img, xforms[i]);
            if (debugMode && (i < (size - 1))) {
                Imtil.save(img, outLoc + "." + i + ".png");
            }
        }
        Imtil.save(img, outLoc);
        info("Saved " + outLoc);
    }
    
    private final static void processParameterized(final Img img, final String xform) {
        if ("brighten".equalsIgnoreCase(xform)) {
            brighten(img);
            return;
        } else if ("stretch".equalsIgnoreCase(xform)) {
            stretch(img);
            return;
        }
        final String[] xformTokens = xform.split(",");
        if (xformTokens.length != 3) {
            throw new IllegalStateException("Expectd 3 channel transforms but found " + xform);
        }
        processParameterized(img, xformTokens[0], xformTokens[1], xformTokens[2]);
    }
    
    private final static void brighten(final Img img) {
        final int w = img.getWidth(), h = img.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int p = img.getRGB(x, y);
                final int r = f.getRed(p), g = f.getGreen(p), b = f.getBlue(p);
                final int ro = brighten(r), go = brighten(g), bo = brighten(b);
                img.setRGB(x, y, f.getDataElement(ro, go, bo, Pancolor.MAX_VALUE));
            }
        }
    }
    
    private final static int brighten(final int value) {
        final float v = value;
        final float f1 = v / Pancolor.MAX_VALUE; // 0 <= f1 <= 1
        final float f2 = (float) Math.sqrt(f1); // 0 <= f1 <= f2 <= 1 (square root of number between 0 and 1 is greater than the original number)
        return multiplyChannel(Pancolor.MAX_VALUE, f2);
    }
    
    private static int minAnalyzedValue = -1;
    private static int maxAnalyzedValue = -1;
    private static int stretchMin = -1;
    private static float stretchMultiplier = -1;
    
    private final static void stretch(final Img img) {
        final int w = img.getWidth(), h = img.getHeight();
        clearStretchValues();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int n = 0;
                int r = 0, g = 0, b = 0;
                // Find average of neighboring pixels so a single white pixel doesn't throw off the whole image
                for (int yi = (Math.max(0, y - 2)); yi <= Math.min(h - 1, y + 2); yi++) {
                    for (int xi = (Math.max(0, x - 2)); xi <= Math.min(w - 1, x + 2); xi++) {
                        final int p = img.getRGB(xi, yi);
                        r += f.getRed(p);
                        g += f.getGreen(p);
                        b += f.getBlue(p);
                        n++;
                    }
                }
                analyzeStretchValue(r, n);
                analyzeStretchValue(g, n);
                analyzeStretchValue(b, n);
            }
        }
        stretchMin = minAnalyzedValue;
        final float stretchDenominator = maxAnalyzedValue - minAnalyzedValue;
        stretchMultiplier = Pancolor.MAX_VALUE / stretchDenominator;
        clearStretchValues();
        boolean maxFound = false;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int p = img.getRGB(x, y);
                final int r = f.getRed(p), g = f.getGreen(p), b = f.getBlue(p);
                final int ro = stretch(r), go = stretch(g), bo = stretch(b);
                img.setRGB(x, y, f.getDataElement(ro, go, bo, Pancolor.MAX_VALUE));
                if (!maxFound && ((ro == Pancolor.MAX_VALUE) || (go == Pancolor.MAX_VALUE) || (bo == Pancolor.MAX_VALUE))) {
                    //info("Stretched to max value at " + x + ", " + y);
                    maxFound = true;
                }
            }
        }
        if ((minAnalyzedValue != 0) || (maxAnalyzedValue != Pancolor.MAX_VALUE)) {
            throw new IllegalStateException("Min stretched value: " + minAnalyzedValue + "; max stretched value: " + maxAnalyzedValue);
        }
    }
    
    private final static int stretch(final int value) {
        final int stretched = multiplyChannel(value - stretchMin, stretchMultiplier);
        analyzeStretchValue(stretched);
        return stretched;
    }
    
    private final static void clearStretchValues() {
        minAnalyzedValue = Pancolor.MAX_VALUE;
        maxAnalyzedValue = 0;
    }
    
    private final static void analyzeStretchValue(final float sum, final float n) {
        analyzeStretchValue(clamp(Math.round(sum / n)));
    }

    private final static void analyzeStretchValue(final int value) {
        minAnalyzedValue = Math.min(minAnalyzedValue, value);
        maxAnalyzedValue = Math.max(maxAnalyzedValue, value);
    }
    
    private final static void applyPaletteMap(final String xformSrc, final String xformDst, final String mapSrc, final String mapDst) {
        final Map<Integer, Integer> map = getPaletteMap(mapSrc, mapDst);
        info("Applying palette map for " + xformSrc + " to " + xformDst);
        final File srcFile = new File(xformSrc);
        final File dstFile = new File(xformDst);
        if (srcFile.isDirectory()) {
            dstFile.mkdirs();
            Iotil.mkdirs(xformDst);
            for (final File srcChild : srcFile.listFiles()) {
                final File dstChild = new File(dstFile, srcChild.getName());
                info("Applying palette map for " + srcChild.getAbsolutePath() + " to " + dstChild.getAbsolutePath());
                applyPaletteMap(map, srcChild, dstChild);
                info("Finished applying palette map for " + srcChild.getAbsolutePath() + " to " + dstChild.getAbsolutePath());
            }
        } else {
            applyPaletteMap(map, srcFile, dstFile);
        }
        info("Finished applying palette map for " + xformSrc + " to " + xformDst);
    }
    
    private final static void applyPaletteMap(final Map<Integer, Integer> map, final File srcFile, final File dstFile) {
        final Img src = Imtil.load(srcFile);
        final int w = src.getWidth(), h = src.getHeight();
        final Img dst = Imtil.newImage(w, h);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int sp = src.getRGB(x, y);
                int dp = sp;
                if (f.getAlpha(sp) > 0) {
                    final Integer mapped = map.get(Integer.valueOf(sp));
                    if (mapped != null) {
                        dp = mapped.intValue();
                    }
                }
                dst.setRGB(x, y, dp);
            }
        }
        Imtil.save(dst, dstFile.getAbsolutePath());
    }
    
    private final static Map<Integer, Integer> getPaletteMap(final String src, final String dst) {
        info("Building palette map for " + src + " to " + dst);
        final File srcFile = new File(src);
        final File dstFile = new File(dst);
        final Map<Integer, CountMap<Integer>> counts = new HashMap<Integer, CountMap<Integer>>();
        if (srcFile.isDirectory()) {
            final File[] dstChildren = dstFile.listFiles();
            final Map<String, File> dstMap = new HashMap<String, File>(dstChildren.length);
            for (final File dstChild : dstChildren) {
                if (dstChild.isDirectory()) {
                    continue;
                }
                dstMap.put(dstChild.getName(), dstChild);
            }
            for (final File srcChild : srcFile.listFiles()) {
                if (srcChild.isDirectory()) {
                    continue;
                }
                final File dstChild = dstMap.get(srcChild.getName());
                info("Building palette map for " + srcChild.getAbsolutePath() + " to " + dstChild.getAbsolutePath());
                updatePaletteMap(counts, srcChild, dstChild);
                info("Finished building palette map for " + srcChild.getAbsolutePath() + " to " + dstChild.getAbsolutePath());
            }
        } else {
            updatePaletteMap(counts, srcFile, dstFile);
        }
        final Map<Integer, Integer> map = new HashMap<Integer, Integer>(counts.size());
        for (final Entry<Integer, CountMap<Integer>> entry : counts.entrySet()) {
            final Integer srcKey = entry.getKey();
            final Integer dstKey = entry.getValue().getMostFrequentKey();
            map.put(srcKey, dstKey);
        }
        info("Finished building palette map for " + src + " to " + dst);
        return map;
    }
    
    private final static void updatePaletteMap(final Map<Integer, CountMap<Integer>> counts, final File srcFile, final File dstFile) {
        final Img src = Imtil.load(srcFile);
        final Img dst = Imtil.load(dstFile);
        final int w = src.getWidth(), h = src.getHeight();
        if (w != dst.getWidth()) {
            throw new IllegalStateException("Source width: " + w + ", destination width: " + dst.getWidth());
        } else if (h != dst.getHeight()) {
            throw new IllegalStateException("Source height: " + h + ", destination height: " + dst.getHeight());
        }
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int sp = src.getRGB(x, y);
                if (f.getAlpha(sp) == 0) {
                    continue;
                }
                final int dp = dst.getRGB(x, y);
                final Integer srcKey = Integer.valueOf(sp);
                CountMap<Integer> dsts = counts.get(srcKey);
                if (dsts == null) {
                    dsts = new CountMap<Integer>();
                    counts.put(srcKey, dsts);
                }
                dsts.inc(Integer.valueOf(dp));
            }
        }
    }
    
    private final static void processParameterized(final Img img, final String xformR, final String xformG, final String xformB) {
        final int w = img.getWidth(), h = img.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int p = img.getRGB(x, y);
                final int r = f.getRed(p), g = f.getGreen(p), b = f.getBlue(p);
                final int ro = process(xformR, r, g, b);
                final int go = process(xformG, r, g, b);
                final int bo = process(xformB, r, g, b);
                img.setRGB(x, y, f.getDataElement(ro, go, bo, Pancolor.MAX_VALUE));
            }
        }
    }
    
    private final static int process(final String xform, final int r, final int g, final int b) {
        final int indexTimes = xform.indexOf('*');
        final float mult;
        if (indexTimes >= 0) {
            mult = Float.parseFloat(xform.substring(0, indexTimes));
        } else {
            mult = 1.0f;
        }
        final String srcType = xform.substring(indexTimes + 1);
        final int src;
        sortChannels(r, g, b);
        if ("r".equals(srcType)) { // Red
            src = r;
        } else if ("g".equalsIgnoreCase(srcType)) { // Green
            src = g;
        } else if ("b".equalsIgnoreCase(srcType)) { // Blue
            src = b;
        } else if ("n".equalsIgnoreCase(srcType)) { // miN
            src = channels[0];
        } else if ("d".equalsIgnoreCase(srcType)) { // miD
            src = channels[1];
        } else if ("x".equalsIgnoreCase(srcType)) { // maX
            src = channels[2];
        } else if ("a3".equalsIgnoreCase(srcType)) { // Average (3 components)
            src = (r + g + b) / 3;
        } else if ("a2".equalsIgnoreCase(srcType)) { // Average (2 components, min an dmax)
            src = (channels[0] + channels[2]) / 3;
        } else if ("0".equalsIgnoreCase(srcType)) {
            src = 0;
        } else {
            throw new IllegalStateException("Unexpected channel source type: " + srcType);
        }
        final int value = multiplyChannel(src, mult);
        return value;
    }
    
    private final static boolean isValidCoordinates(final int x, final int y, final Img img) {
        return (x >= 0) && (y >= 0) && (x < img.getWidth()) && (y < img.getHeight());
    }
    
    private final static int getOpaque(final int r, final int g, final int b) {
        return f.getDataElement(r, g, b, ALPHA_OPAQUE);
    }
    
    private final static void info(final Object s) {
        System.out.println(s);
    }
}
