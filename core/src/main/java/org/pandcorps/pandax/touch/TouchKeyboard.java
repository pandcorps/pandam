/*
Copyright (c) 2009-2016, Andrew M. Martin
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
package org.pandcorps.pandax.touch;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandax.text.*;

public class TouchKeyboard {
	private final static char CHAR_BACKSPACE = '<';
	private final Panmage img;
	private final Panmage imgAct;
	private final MultiFont fonts;
	private final Panlayer layer;
	private final int offX;
	private final int offY;
	private final static int z = 0;
	
    public TouchKeyboard(final Panmage img, final Panmage imgAct, final MultiFont fonts) {
    	this(img, imgAct, fonts, getCenteredY(img));
    }
    
    public TouchKeyboard(final Panmage img, final Panmage imgAct, final MultiFont fonts, int y) {
    	this.img = img;
    	this.imgAct = imgAct;
    	this.fonts = fonts;
    	layer = Pangame.getGame().getCurrentRoom();
        final char[][] layout = {
                {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p'},
                {'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l'},
                {'z', 'x', 'c', 'v', 'b', 'n', 'm', CHAR_BACKSPACE}
        };
        final Panple size = img.getSize();
        final int w = (int) size.getX(), h = (int) size.getY();
        final Pangine engine = Pangine.getEngine();
        int minX = (engine.getEffectiveWidth() - (w * 10)) / 2;
        offX = (w - fonts.getWidth()) / 2;
        offY = (h - fonts.getHeight()) / 2;
        for (final char[] row : layout) {
            int x = minX;
            for (final char c : row) {
                newTouchKey(c, x, y);
                x += w;
            }
            y -= h;
            minX += (w / 2);
        }
    }
    
    private final TouchButton newTouchKey(final char c, final int x, final int y) {
    	final Panteraction inter = Pangine.getEngine().getInteraction();
    	final String txt = String.valueOf(Character.toUpperCase(c));
    	final TouchButton btn = new TouchButton(inter, layer, txt, x, y, z, img, imgAct, null, 0, 0, fonts, txt, offX, offY, true);
    	btn.setMappedInput(c == CHAR_BACKSPACE ? inter.KEY_BACKSPACE : inter.getKey(c));
    	Pangine.getEngine().registerTouchButton(btn);
    	return btn;
    }
    
    public final static int getMaxKeyWidth() {
    	return Pangine.getEngine().getEffectiveWidth() / 10;
    }
    
    private final static int getCenteredY(final Panmage img) {
    	final int h = (int) img.getSize().getY();
    	return ((Pangine.getEngine().getEffectiveHeight() - (h * 3)) / 2) + (h * 2);
    }
}
