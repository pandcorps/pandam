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
    private final static Pancolor COLOR_BASE;
    private final static Pancolor COLOR_BACKGROUND;
    private final static Pancolor COLOR_CURSOR;
    
    static {
        final short base = 66;
        COLOR_BASE = new FinPancolor(base, base, Pancolor.MAX_VALUE, Pancolor.MAX_VALUE);
        final short bg = 160;
        COLOR_BACKGROUND = new FinPancolor(bg, bg, Pancolor.MAX_VALUE, Pancolor.MAX_VALUE);
        final short cursor = 32, cursorBlue = 128;
        COLOR_CURSOR = new FinPancolor(cursor, cursor, cursorBlue, Pancolor.MAX_VALUE);
    }
    
    public final static Panmage getOutline(final int size, final Pancolor color) {
        return getOutline(size, color, color, color);
    }
    
    public final static Panmage getOutline(final int size, final Pancolor base, final Pancolor background, final Pancolor cursor) {
        return getBasic("Outline", size, base, background, cursor);
    }
    
    public final static Panmage getSimple(final int size, final Pancolor color) {
        return getOutline(size, color, color, color);
    }
    
    public final static Panmage getSimple(final int size, final Pancolor base, final Pancolor background, final Pancolor cursor) {
        return getBasic("Simple", size, base, background, cursor);
    }
    
    private final static Panmage getBasic(final String style, final int size, final Pancolor base, final Pancolor background, final Pancolor cursor) {
        final HashMap<Pancolor, Pancolor> map = new HashMap<Pancolor, Pancolor>();
        map.put(COLOR_BASE, base);
        map.put(COLOR_BACKGROUND, background);
        map.put(COLOR_CURSOR, cursor);
        final ReplacePixelFilter filter = new ReplacePixelFilter(map);
        return get(style, size, base.toString() + '.' + background + '.' + cursor, filter);
    }
    
    private final static Panmage get(final String style, final int size, final String filterDesc, final PixelFilter filter) {
        final String name = style + size;
        final String id = "org.pandcorps.pandax.text.Fonts." + name + '.' + filterDesc;
        final Pangine engine = Pangine.getEngine();
        final Panmage image = engine.getImage(id);
        if (image != null) {
            return image;
        }
        final BufferedImage img = Imtil.load("org/pandcorps/res/img/Font" + name + ".png");
        return engine.createImage(id, Imtil.filter(img, filter));
    }
}
