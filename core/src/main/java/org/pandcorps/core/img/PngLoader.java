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
package org.pandcorps.core.img;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.pandcorps.core.*;
import org.pandcorps.core.io.*;

public class PngLoader {
    private final static int[] HEADER = { 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
    private final static int[] CHUNK_IHDR = { 'I', 'H', 'D', 'R' };
    private final static int[] CHUNK_PLTE = { 'P', 'L', 'T', 'E' };
    private final static int[] CHUNK_TRNS = { 't', 'R', 'N', 'S' };
    private final static int[] CHUNK_IDAT = { 'I', 'D', 'A', 'T' };
    private final static int[] CHUNK_IEND = { 'I', 'E', 'N', 'D' };
    private final static int COLOR_RGB = 2;
    private final static int COLOR_PALETTE = 3;
    private final static int COLOR_RGBA = 6;
    private final static int FILTER_NONE = 0;
    private final static int FILTER_SUB = 1;
    private final static int FILTER_UP = 2;
    private final static int FILTER_AVERAGE = 3;
    private final static int FILTER_PAETH = 4;
    private final static int MOD_256 = 255;
    
    private int chunkLength = 0;
    private final int[] chunkType = new int[4];
    private int width = 0, height = 0, bitDepth = 0, colorType = 0, compressionMethod = 0, filterMethod = 0, interlaceMethod = 0;
    private int cpp = 0; // channels per pixel
    private int bpc = 0; // bytes per channel
    private int bpp = 0; // bytes per pixel
    private int[] currLine, prevLine = null;
    private int filterType = 0;
    private int x = 0, y = 0;
    
    public final static void main(final String[] args) throws IOException {
        load(new FileInputStream(args[0]));
    }
    
    public final static Img load(final InputStream in) throws IOException {
        return new PngLoader().run(in);
    }
    
    private final Img run(final InputStream in) throws IOException {
        for (final int b : HEADER) {
            if (nextByte(in) != b) {
                throw new IllegalStateException("Expected " + b);
            }
        }
        int[] buffer = null;
        int bufferIndex = 0;
        int[] palette = null, transparencies = null;
        boolean loading = true;
        InflaterInputStream inflater = null;
        final ImgFactory f = ImgFactory.getFactory();
        try {
            // Read chunks
            while (loading) {
                startChunk(in);
                if (Arrays.equals(chunkType, CHUNK_IHDR)) {
                    width = readInt(in); height = readInt(in);
                    buffer = new int[width * height];
                    bitDepth = readByte(in); colorType = readByte(in);
                    compressionMethod = readByte(in); filterMethod = readByte(in); interlaceMethod = readByte(in);
                    if (bitDepth > 8) {
                        throw new UnsupportedOperationException("Bit depth > 8 currently unsupported but found " + bitDepth);
                    } else if (compressionMethod != 0) {
                        throw new IllegalStateException("Expected compression method 0 but found " + compressionMethod);
                    } else if (filterMethod != 0) {
                        throw new IllegalStateException("Expected filter method 0 but found " + filterMethod);
                    } else if (interlaceMethod != 0) {
                        throw new IllegalStateException("Expected interlace method 0 but found " + interlaceMethod);
                    }
                    if (colorType == COLOR_PALETTE) {
                        cpp = 1;
                    } else if (colorType == COLOR_RGB) {
                        cpp = 3;
                    } else if (colorType == COLOR_RGBA) {
                        cpp = 4;
                    }
                    bpc = (bitDepth > 8) ? 2 : 1;
                    bpp = bpc * cpp;
                    currLine = new int[width * bpp];
                } else if (Arrays.equals(chunkType, CHUNK_PLTE)) {
                    palette = readArray(in, chunkLength);
                } else if (Arrays.equals(chunkType, CHUNK_TRNS)) {
                    transparencies = readArray(in, chunkLength);
                } else if (Arrays.equals(chunkType, CHUNK_IDAT)) {
                    final IdatInputStream idatInputStream = new IdatInputStream(in, chunkLength);
                    inflater = new InflaterInputStream(idatInputStream);
                    for (y = 0; y < height; y++) {
                        filterType = nextByte(inflater);
                        bitIndex = 0;
                        for (x = 0; x < width; x++) {
                            final int r, g, b, a;
                            if (colorType == COLOR_PALETTE) {
                                final int colorIndex = read(inflater, 0), ci3 = colorIndex * 3;
                                r = palette[ci3]; g = palette[ci3 + 1]; b = palette[ci3 + 2];
                                a = ((transparencies == null) || (colorIndex >= transparencies.length)) ? 255 : transparencies[colorIndex];
                            } else if (colorType == COLOR_RGB) {
                                r = read(inflater, 0); g = read(inflater, 1); b = read(inflater, 2); a = 255;
                            } else if (colorType == COLOR_RGBA) {
                                r = read(inflater, 0); g = read(inflater, 1); b = read(inflater, 2); a = read(inflater, 3);
                            } else {
                                throw new UnsupportedOperationException("Found currently unsupported color type " + colorType);
                            }
                            buffer[bufferIndex++] = f.getDataElement(r, g, b, a);
                        }
                        swapLines();
                    }
                    idatInputStream.moreChunksExpected = false;
                    if (inflater.read() != -1) {
                        throw new IllegalStateException("Found unexpected extra content for line " + y);
                    }
                } else if (Arrays.equals(chunkType, CHUNK_IEND)) {
                    if (chunkLength != 0) {
                        throw new IllegalStateException("Expected IEND length to be 0 but found " + chunkLength);
                    }
                    loading = false;
                } else {
                    skip(in, chunkLength);
                }
                endChunk(in);
            }
            if (in.read() != -1) {
                throw new IllegalStateException("Found content after IEND");
            }
        } finally {
            Iotil.close(inflater);
        }
        return new Img(width, height, buffer);
    }
    
    private final int read(final InputStream in, final int channel) throws IOException {
        final int currIndex = (x * bpp) + channel, leftIndex = currIndex - bpp;
        final int raw = nextSample(in), sub;
        final int a = (leftIndex < 0) ? 0 : currLine[leftIndex];
        final int b = (prevLine == null) ? 0 : prevLine[currIndex];
        final int c = ((leftIndex < 0) || (prevLine == null)) ? 0 : prevLine[leftIndex];
        if (filterType == FILTER_NONE) {
            sub = raw;
        } else if (filterType == FILTER_SUB) {
            sub = raw + a;
        } else if (filterType == FILTER_UP) {
            sub = raw + b;
        } else if (filterType == FILTER_AVERAGE) {
            sub = raw + (a + b) / 2;
        } else if (filterType == FILTER_PAETH) {
            sub = raw + paethPredictor(a, b, c);
        } else {
            throw new IllegalStateException("Unexpected filter type type " + filterType + " for line " + y);
        }
        final int ret = sub & MOD_256;
        currLine[currIndex] = ret;
        return ret;
    }
    
    private final static int nextByte(final InputStream in) throws IOException {
        final int b = in.read();
        if (b == -1) {
            throw new IllegalStateException("Unexpected EOF");
        }
        return b;
    }

    private int lastByte = -1;
    private int bitIndex = 0;
    private int bitMask = -1;
    
    private final int nextSample(final InputStream in) throws IOException {
        if (bitDepth == 8) {
            return nextByte(in);
        } else if (bitIndex == 0) {
            if (bitMask == -1) {
                bitMask = getBitMask();
            }
            lastByte = nextByte(in);
        }
        final int sample = (lastByte >>> (8 - bitDepth - bitIndex)) & bitMask;
        bitIndex += bitDepth;
        if (bitIndex >= 8) {
            bitIndex = 0;
        }
        return sample;
    }
    
    private final int getBitMask() {
        if (bitDepth == 1) {
            return 1;
        } else if (bitDepth == 2) {
            return 3;
        } else if (bitDepth == 4) {
            return 15;
        }
        throw new IllegalStateException("Expected bitDepth to be 1, 2, or 4 at this point");
    }
    
    private final int paethPredictor(final int a, final int b, final int c) {
        final int p = a + b - c;
        final int pa = Math.abs(p - a), pb = Math.abs(p - b), pc = Math.abs(p - c);
        if ((pa <= pb) && (pa <= pc)) {
            return a;
        } else if (pb <= pc) {
            return b;
        } else {
            return c;
        }
    }
    
    private final static int readInt(final InputStream in) throws IOException {
        return readInt(in, 4);
    }
    
    private final static int readByte(final InputStream in) throws IOException {
        return readInt(in, 1);
    }
    
    private final static int readInt(final InputStream in, final int size) throws IOException {
        int value = 0;
        for (int i = 0; i < size; i++) {
            value = (value * 256) + nextByte(in);
        }
        return value;
    }
    
    private final static int[] readArray(final InputStream in, final int size) throws IOException {
        final int[] a = new int[size];
        for (int i = 0; i < size; i ++) {
            a[i] = nextByte(in);
        }
        return a;
    }
    
    private final static void skip(final InputStream in, final int size) throws IOException {
        for (int i = 0; i < size; i++) {
            nextByte(in);
        }
    }
    
    private final void swapLines() {
        if (prevLine == null) {
            prevLine = new int[currLine.length];
        }
        final int[] temp = prevLine;
        prevLine = currLine;
        currLine = temp;
    }
    
    private final static String getChunkType(final int[] chunkType) {
        final StringBuilder b = new StringBuilder(chunkType.length);
        for (final int c : chunkType) {
            b.append((char) c);
        }
        return b.toString();
    }
    
    private final void startChunk(final InputStream in) throws IOException {
        chunkLength = readInt(in);
        for (int i = 0; i < 4; i++) {
            chunkType[i] = nextByte(in);
        }
    }
    
    private final static void endChunk(final InputStream in) throws IOException {
        skip(in, 4); // CRC
    }
    
    private final class IdatInputStream extends InputStream {
        private final InputStream raw;
        private InputStream chunk;
        private boolean moreChunksExpected = true;
        
        private IdatInputStream(final InputStream raw, final int initialChunkLength) {
            this.raw = raw;
            chunk = new SubInputStream(raw, initialChunkLength);
        }
        
        @Override
        public final int read() throws IOException {
            final int b = chunk.read();
            if ((b != -1) || !moreChunksExpected) {
                return b;
            }
            prepareNextChunk();
            return chunk.read();
        }
        
        @Override
        public final int read(final byte[] buf) throws IOException {
            return read(buf, 0, buf.length);
        }
        
        @Override
        public final int read(final byte[] buf, final int off, final int len) throws IOException {
            final int bytesRead = chunk.read(buf, off, len);
            if ((bytesRead != -1) || !moreChunksExpected) {
                return bytesRead;
            }
            prepareNextChunk();
            return chunk.read(buf, off, len);
        }
        
        private final void prepareNextChunk() throws IOException {
            endChunk(raw);
            startChunk(raw);
            if (!Arrays.equals(chunkType, CHUNK_IDAT)) {
                throw new IllegalStateException("Expected another IDAT chunk but found " + getChunkType(chunkType));
            }
            chunk = new SubInputStream(raw, chunkLength);
        }
    }
}
