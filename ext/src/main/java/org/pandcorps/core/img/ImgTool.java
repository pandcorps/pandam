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

import org.pandcorps.core.*;

public final class ImgTool {
    private final static ImgFactory f = ImgFactory.getFactory();
    private final static String sep = System.getProperty("file.separator");
    private final static int[] channels = new int[3];
    
    public final static void main(final String[] args) {
        info("Starting");
        final String inLoc = args[0];
        final String outLoc = args[1];
        final File inFile = new File(inLoc);
        if (inFile.isDirectory()) {
            processDirectory(inFile, outLoc);
        } else {
            processFile(inFile, outLoc);
        }
        info("Finished");
    }
    
    private final static void processDirectory(final File inDir, final String outLoc) {
        for (final File inFile : inDir.listFiles()) {
            processFile(inFile, outLoc + sep + inFile.getName());
        }
    }
    
    private final static void processFile(final File inFile, final String outLoc) {
        info("Processing from " + inFile + " into " + outLoc);
        final Img img = Imtil.load(inFile.getAbsolutePath());
        final int w = img.getWidth(), h = img.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final int p = img.getRGB(x, y);
                channels[0] = f.getRed(p);
                channels[1] = f.getGreen(p);
                channels[2] = f.getBlue(p);
                Arrays.sort(channels);
                img.setRGB(x, y, f.getDataElement(channels[0], channels[1], channels[2], f.getAlpha(p)));
            }
        }
        Imtil.save(img, outLoc);
    }
    
    protected final static boolean isRed(final int p) {
        return f.getRed(p) > f.getBlue(p);
    }
    
    private final static void info(final Object s) {
        System.out.println(s);
    }
}
