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
package org.pandcorps.pandax.text;

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.*;

public final class Fonts {
    public static enum FontType {
        Byte,
        Upper,
        Number
    }
    
    public final static class FontRequest {
        private final static FontType DEF_TYPE = FontType.Byte;
        private final static int DEF_SIZE = 8;
        
        private final FontType type;
        private final int size;
        private final int width;
        private final int height;
        
        private FontRequest(final FontType type, final int size, final int width, final int height) {
            this.type = type;
            this.size = size;
            this.width = width;
            this.height = height;
        }
        
        public FontRequest(final FontType type, final int size) {
            this(type, size, size, size);
        }
        
        public FontRequest(final FontType type) {
            this(type, DEF_SIZE);
        }
        
        public FontRequest(final int size) {
            this(DEF_TYPE, size);
        }
        
        private FontRequest() {
            this(DEF_TYPE);
        }
    }
    
    private final static FontRequest newTinyRequest(final FontType type) {
        return new FontRequest(type, -1, 4, 6);
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
        return getOutline(req, base, background, cursor, outline, null);
    }
    
    public final static Font getOutline(final FontRequest req, final Pancolor base, final Pancolor background, final Pancolor cursor, final Pancolor outline, final Pancolor transparent) {
        return getBasic("Outline", req, base, background, cursor, outline, transparent);
    }
    
    public final static Font getSimple(final FontRequest req, final Pancolor color) {
        return getSimple(req, color, color, color);
    }
    
    public final static Font getSimple(final FontRequest req, final Pancolor base, final Pancolor background, final Pancolor cursor) {
        return getSimple(req, base, background, cursor, null);
    }
    
    public final static Font getSimple(final FontRequest req, final Pancolor base, final Pancolor background, final Pancolor cursor, final Pancolor transparent) {
        return getBasic("Simple", req, base, background, cursor, COLOR_OUTLINE, transparent);
    }
    
    public final static Font getClassic(final FontRequest req, final Pancolor color) {
        return getClassic(req, color, color, color);
    }
    
    public final static Font getClassic(final FontRequest req, final Pancolor base, final Pancolor background, final Pancolor cursor) {
        return getClassic(req, base, background, cursor, null);
    }
    
    public final static Font getClassic(final FontRequest req, final Pancolor base, final Pancolor background, final Pancolor cursor, final Pancolor transparent) {
        return getBasic("Classic", req, base, background, cursor, COLOR_OUTLINE, transparent);
    }
    
    public final static Font getTiny(final FontType type, final Pancolor color) {
        return getTiny(type, color, color, color);
    }
    
    public final static Font getTiny(final FontType type, final Pancolor base, final Pancolor background, final Pancolor cursor) {
        return getTiny(type, base, background, cursor, null);
    }
    
    public final static Font getTiny(final FontType type, final Pancolor base, final Pancolor background, final Pancolor cursor, final Pancolor transparent) {
        return getBasic("Tiny", newTinyRequest(type), base, background, cursor, COLOR_OUTLINE, transparent);
    }
    
    private final static Font getBasic(final String style, final FontRequest req, final Pancolor base, final Pancolor background, final Pancolor cursor, final Pancolor outline, final Pancolor transparent) {
        final HashMap<Pancolor, Pancolor> map = new HashMap<Pancolor, Pancolor>();
        map.put(COLOR_BASE, base);
        map.put(COLOR_BACKGROUND, background);
        map.put(COLOR_CURSOR, cursor);
        map.put(COLOR_OUTLINE, outline);
        final ReplacePixelFilter filter = new ReplacePixelFilter(map);
        return get(style, req, base.toString() + '.' + background + '.' + cursor, filter, transparent);
    }
    
    private final static Font get(final String style, FontRequest req, final String filterDesc, ReplacePixelFilter filter, final Pancolor transparent) {
        if (req == null) {
            req = DEFAULT_REQUEST;
        }
        final int size = req.size;
        final String name = (size < 0) ? style : (style + size);
        final FontType type = req.type;
        final String id = "org.pandcorps.pandax.text.Fonts." + name + '.' + type + '.' + filterDesc + '.' + transparent;
        final Pangine engine = Pangine.getEngine();
        Panmage image = engine.getImage(id);
        if (image == null) {
            Img img = Imtil.load("org/pandcorps/res/img/Font" + name + ".png");
            if (transparent != null) {
            	final int src = img.getRGB(0, 0), dst = PixelFilter.getRgba(transparent);
            	if (filter == null) {
            		filter = new ReplacePixelFilter(src, dst);
            	} else {
            		filter.put(src, dst);
            	}
            }
            final int w = req.width, h = req.height;
            if (type == FontType.Number) {
                //Imtil.save(img, "c:\\raw.png");
                final Img out = Imtil.newImage(w * NumberFont.NUM, h * NumberFont.NUM);
                Imtil.copy(img, out, 10 * w, 2 * h, 4 * w, h, 0, 0);
                Imtil.copy(img, out, 14 * w, 2 * h, 2 * w, h, 0, h);
                Imtil.copy(img, out, 0, 3 * h, 2 * w, h, w * 2, h);
                Imtil.copy(img, out, 2 * w, 3 * h, 4 * w, h, 0, h * 2);
                Imtil.copy(img, out, 6 * w, 3 * h, 4 * w, h, 0, h * 3);
                //Imtil.save(out, "c:\\num.png");
                img.close();
                img = out;
            } else if (type == FontType.Upper) {
                final Img out = Imtil.newImage(w * UpperFont.NUM, h * UpperFont.NUM);
                Imtil.copy(img, out, 0, 2 * h, 8 * w, h, 0, 0);
                Imtil.copy(img, out, 8 * w, 2 * h, 8 * w, h, 0, h);
                Imtil.copy(img, out, 0, 3 * h, 8 * w, h, 0, h * 2);
                Imtil.copy(img, out, 8 * w, 3 * h, 8 * w, h, 0, h * 3);
                Imtil.copy(img, out, 0, 4 * h, 8 * w, h, 0, h * 4);
                Imtil.copy(img, out, 8 * w, 4 * h, 8 * w, h, 0, h * 5);
                Imtil.copy(img, out, 0, 5 * h, 8 * w, h, 0, h * 6);
                Imtil.copy(img, out, 8 * w, 5 * h, 8 * w, h, 0, h * 7);
                //Imtil.save(out, "c:\\up.png");
                img.close();
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
    
    public final static MultiFont getClassics(final FontRequest req, final Pancolor foreground, final Pancolor shadow) {
    	return new MultiFont(new FontLayer(getClassic(req, foreground), FinPanple.ORIGIN), new FontLayer(getClassic(req, shadow), new FinPanple(1, -1, -1)));
    }
    
    public final static MultiFont getTinies(final FontType type, final Pancolor foreground, final Pancolor shadow) {
        return new MultiFont(new FontLayer(getTiny(type, foreground), FinPanple.ORIGIN), new FontLayer(getTiny(type, shadow), new FinPanple(1, -1, -1)));
    }
}
