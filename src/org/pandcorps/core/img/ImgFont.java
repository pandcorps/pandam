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
package org.pandcorps.core.img;

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.img.scale.*;

public final class ImgFont {
    private final Img img;
    private final int maxLetterWidth;
    private final int letterHeight;
    private final Map<Character, ImgLetter> map = new HashMap<Character, ImgLetter>();
    
    public ImgFont(final Img img) {
        this.img = img;
        maxLetterWidth = img.getWidth() / 16;
        letterHeight = img.getHeight() / 16;
    }
    
    public final Img newImage(final short r, final short g, final short b, final short a, final String s) {
        final int[] dims = run(null, 0, 0, s);
        final Img img = Imtil.newImage(dims[0], dims[1]);
        Imtil.clear(img, r, g, b, a);
        draw(img, 0, 0, s);
        return img;
    }
    
    public final void draw(final Img canvas, final int x, int y, final String s) {
        if (canvas == null) {
            throw new NullPointerException("Null canvas Img");
        }
        run(canvas, x, y, s);
    }
    
    private final int[] run(final Img canvas, final int x, int y, final String s) {
        final int size = s.length();
        int cx = x, maxWidth = 0;
        for (int i = 0; i < size; i++) {
            final char c = s.charAt(i);
            if (c == '\n') {
                maxWidth = Math.max(maxWidth, cx);
                cx = x;
                y += letterHeight;
                continue;
            }
            if (cx > x) {
                cx++;
            }
            final ImgLetter letter = getLetter(c);
            if (canvas != null) {
                letter.draw(canvas, cx, y);
            }
            cx += letter.letterWidth;
        }
        maxWidth = Math.max(maxWidth, cx);
        return new int[] {maxWidth, y + letterHeight - 1};
    }
    
    private final ImgLetter getLetter(final char c) {
        final Character key = Character.valueOf(c);
        ImgLetter letter = map.get(key);
        if (letter == null) {
            letter = new ImgLetter(c);
            map.put(key, letter);
        }
        return letter;
    }
    
    private final class ImgLetter {
        private final int offX;
        private final int offY;
        private final int letterWidth;
        
        private ImgLetter(final char c) {
            final int i = c;
            final int row = i / 16;
            offY = row * letterHeight;
            final int col = i - (row * 16);
            int _offX = col * maxLetterWidth;
            if (c == ' ') {
                offX = _offX;
                letterWidth = 5;
                return;
            }
            int currOff = maxLetterWidth - 1, currWidth = 0;
            for (int y = 0; y < letterHeight; y++) {
                final int r = offY + y;
                for (int x = 0; x < maxLetterWidth; x++) {
                    if (Imtil.getColor(img, _offX + x, r).getA() > 0) {
                        currOff = Math.min(currOff, x);
                        currWidth = Math.max(currWidth, x);
                    }
                }
            }
            offX = _offX + currOff;
            letterWidth = currWidth + 1 - currOff;
        }
        
        private final void draw(final Img canvas, final int x, final int y) {
            Imtil.copy(img, canvas, offX, offY, letterWidth, letterHeight, x, y, Imtil.COPY_FOREGROUND);
        }
    }
    
    public final static String format(String s) {
        //TODO More characters could be replaced; could automatically format Strings passed to ImgFont; could be more efficient
        s = s.replace((char) 232, (char) 138); // e grave
        s = s.replace((char) 233, (char) 130); // e acute
        s = s.replace((char) 234, (char) 136); // e circumflex
        s = s.replace((char) 235, (char) 137); // e umlaut
        s = s.replace((char) 236, (char) 141); // i grave
        s = s.replace((char) 237, (char) 161); // i acute
        s = s.replace((char) 238, (char) 140); // i circumflex
        s = s.replace((char) 239, (char) 139); // i umlaut
        return s;
    }
    
    public final static void main(final String[] args) {
        final Img fontImg = Imtil.load("org/pandcorps/res/img/FontClassic8.png");
        final short colMax = Pancolor.MAX_VALUE, colMin = Pancolor.MIN_VALUE, colMed = 66;
        final PixelFilter f = new ReplacePixelFilter(colMed, colMed, colMax, colMax, colMin, colMin, colMin, colMax);
        Imtil.filterImg(fontImg, 0, 0, fontImg.getWidth(), fontImg.getHeight(), f);
        final ImgFont font = new ImgFont(fontImg);
        String s = args[0].replaceAll("\\\\n", "\n");
        if (args.length > 2 && "upper".equalsIgnoreCase(args[2])) {
            s = s.toUpperCase();
        }
        final String loc = args[1];
        final Img textImg = font.newImage(colMax, colMax, colMax, colMax, s);
        final Img magImg = new NearestNeighborScaler(3).scale(textImg);
        final Img framedImg = Imtil.addBorders(magImg, 5, 5, 5, 5, Pancolor.WHITE);
        final Img cornerImg = Imtil.load("org/pandcorps/res/img/BalloonCorner.png");
        final int cornerSize = cornerImg.getWidth();
        Imtil.copy(cornerImg, framedImg, 0, 0, cornerSize, cornerSize, 0, 0, Imtil.COPY_FOREGROUND);
        Imtil.mirror(cornerImg);
        final int w = framedImg.getWidth(), r = w - cornerSize, h = framedImg.getHeight(), b = h - cornerSize;
        Imtil.copy(cornerImg, framedImg, 0, 0, cornerSize, cornerSize, r, 0, Imtil.COPY_FOREGROUND);
        Imtil.flip(cornerImg);
        Imtil.copy(cornerImg, framedImg, 0, 0, cornerSize, cornerSize, r, b, Imtil.COPY_FOREGROUND);
        Imtil.mirror(cornerImg);
        Imtil.copy(cornerImg, framedImg, 0, 0, cornerSize, cornerSize, 0, b, Imtil.COPY_FOREGROUND);
        final int c2 = cornerSize * 2, fw = w - c2, fh = h - c2, c = Imtil.getDataElement(Pancolor.BLACK);
        final int borderSize = 2, br = w - borderSize, bb = h - borderSize;
        Imtil.drawRectangle(framedImg, cornerSize, 0, fw, borderSize, c);
        Imtil.drawRectangle(framedImg, cornerSize, bb, fw, borderSize, c);
        Imtil.drawRectangle(framedImg, 0, cornerSize, borderSize, fh, c);
        Imtil.drawRectangle(framedImg, br, cornerSize, borderSize, fh, c);
        Imtil.save(framedImg, loc);
    }
}
