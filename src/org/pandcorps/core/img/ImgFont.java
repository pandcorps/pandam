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
        return new int[] {maxWidth, y + letterHeight};
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
    
    public final static void main(final String[] args) {
        final Img fontImg = Imtil.load("org/pandcorps/res/img/FontClassic8.png");
        final ImgFont font = new ImgFont(fontImg);
        final String s = args[0].replaceAll("\\\\n", "\n"), loc = args[1];
        final Img textImg = font.newImage(Pancolor.MAX_VALUE, Pancolor.MAX_VALUE, Pancolor.MAX_VALUE, Pancolor.MAX_VALUE, s);
        Imtil.save(textImg, loc);
    }
}
