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
package org.pandcorps.pandax.text;

import java.awt.image.BufferedImage;
import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.pandam.*;

public final class Fonts {
    public static enum FontType {
        Byte,
        Upper,
        Number
    }
    
    public final static class FontRequest {
        private FontType type = FontType.Byte;
        private int size = 8;
        
        public FontRequest(final FontType type, final int size) {
            this.type = type;
            this.size = size;
        }
        
        public FontRequest(final FontType type) {
            this.type = type;
        }
        
        public FontRequest(final int size) {
            this.size = size;
        }
        
        private FontRequest() {
        }
    }
    
    private final static FontRequest DEFAULT_REQUEST = new FontRequest();
    private final static Pancolor COLOR_BASE;
    private final static Pancolor COLOR_BACKGROUND;
    private final static Pancolor COLOR_CURSOR;
    private final static Pancolor COLOR_OUTLINE;
    
    static {
        final short base = 66;
        COLOR_BASE = new FinPancolor(base, base, Pancolor.MAX_VALUE, Pancolor.MAX_VALUE);
        final short bg = 160;
        COLOR_BACKGROUND = new FinPancolor(bg, bg, Pancolor.MAX_VALUE, Pancolor.MAX_VALUE);
        final short cursor = 32, cursorBlue = 128;
        COLOR_CURSOR = new FinPancolor(cursor, cursor, cursorBlue, Pancolor.MAX_VALUE);
        COLOR_OUTLINE = Pancolor.BLACK;
    }
    
    public final static Font getOutline(final FontRequest req, final Pancolor color) {
        return getOutline(req, color, color, color);
    }
    
    public final static Font getOutline(final FontRequest req, final Pancolor base, final Pancolor background, final Pancolor cursor) {
    	return getOutline(req, base, background, cursor, COLOR_OUTLINE);
    }
    
    public final static Font getOutline(final FontRequest req, final Pancolor base, final Pancolor background, final Pancolor cursor, final Pancolor outline) {
        return getBasic("Outline", req, base, background, cursor, outline);
    }
    
    public final static Font getSimple(final FontRequest req, final Pancolor color) {
        return getSimple(req, color, color, color);
    }
    
    public final static Font getSimple(final FontRequest req, final Pancolor base, final Pancolor background, final Pancolor cursor) {
        return getBasic("Simple", req, base, background, cursor, COLOR_OUTLINE);
    }
    
    private final static Font getBasic(final String style, final FontRequest req, final Pancolor base, final Pancolor background, final Pancolor cursor, final Pancolor outline) {
        final HashMap<Pancolor, Pancolor> map = new HashMap<Pancolor, Pancolor>();
        map.put(COLOR_BASE, base);
        map.put(COLOR_BACKGROUND, background);
        map.put(COLOR_CURSOR, cursor);
        map.put(COLOR_OUTLINE, outline);
        final ReplacePixelFilter filter = new ReplacePixelFilter(map);
        return get(style, req, base.toString() + '.' + background + '.' + cursor, filter);
    }
    
    private final static Font get(final String style, FontRequest req, final String filterDesc, final PixelFilter filter) {
        if (req == null) {
            req = DEFAULT_REQUEST;
        }
        final int size = req.size;
        final String name = style + size;
        final FontType type = req.type;
        final String id = "org.pandcorps.pandax.text.Fonts." + name + '.' + type + '.' + filterDesc;
        final Pangine engine = Pangine.getEngine();
        Panmage image = engine.getImage(id);
        if (image == null) {
            BufferedImage img = Imtil.load("org/pandcorps/res/img/Font" + name + ".png");
            if (type == FontType.Number) {
                //Imtil.save(img, "c:\\raw.png");
                final int newSize = size * NumberFont.NUM;
                final BufferedImage out = new BufferedImage(newSize, newSize, BufferedImage.TYPE_INT_ARGB);
                Imtil.copy(img, out, 10 * size, 2 * size, 4 * size, size, 0, 0);
                Imtil.copy(img, out, 14 * size, 2 * size, 2 * size, size, 0, size);
                Imtil.copy(img, out, 0, 3 * size, 2 * size, size, size * 2, size);
                Imtil.copy(img, out, 2 * size, 3 * size, 4 * size, size, 0, size * 2);
                Imtil.copy(img, out, 6 * size, 3 * size, 4 * size, size, 0, size * 3);
                //Imtil.save(out, "c:\\num.png");
                img = out;
            } else if (type == FontType.Upper) {
                final int newSize = size * UpperFont.NUM;
                final BufferedImage out = new BufferedImage(newSize, newSize, BufferedImage.TYPE_INT_ARGB);
                Imtil.copy(img, out, 0, 2 * size, 8 * size, size, 0, 0);
                Imtil.copy(img, out, 8 * size, 2 * size, 8 * size, size, 0, size);
                Imtil.copy(img, out, 0, 3 * size, 8 * size, size, 0, size * 2);
                Imtil.copy(img, out, 8 * size, 3 * size, 8 * size, size, 0, size * 3);
                Imtil.copy(img, out, 0, 4 * size, 8 * size, size, 0, size * 4);
                Imtil.copy(img, out, 8 * size, 4 * size, 8 * size, size, 0, size * 5);
                Imtil.copy(img, out, 0, 5 * size, 8 * size, size, 0, size * 6);
                Imtil.copy(img, out, 8 * size, 5 * size, 8 * size, size, 0, size * 7);
                //Imtil.save(out, "c:\\up.png");
                img = out;
            }
            image = engine.createImage(id, Imtil.filter(img, filter));
        }
        if (type == FontType.Number) {
            return new NumberFont(image);
        } else if (type == FontType.Upper) {
            return new UpperFont(image);
        }
        return new ByteFont(image);
    }
}
