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

import java.util.*;
import java.util.regex.Pattern;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.UnmodPanple;

public class Pantext extends Panctor {
    public final static char CHAR_NULL = 0;
    public final static char CHAR_SPACE = 32; // TODO dbl-check 32
    
    public final static char CHAR_RADIO = 7; // bel
    public final static char CHAR_CURSOR = 22; // syn
    public final static char CHAR_CURSOR_INS = 254;
    
    public final static char CHAR_VERT = 179;
    public final static char CHAR_RIGHT = 180;
    public final static char CHAR_TOP_RIGHT = 191;
    public final static char CHAR_BOTTOM_LEFT = 192;
    public final static char CHAR_BOTTOM = 193;
    public final static char CHAR_TOP = 194;
    public final static char CHAR_LEFT = 195;
    public final static char CHAR_HORIZ = 196;
    public final static char CHAR_BOTTOM_RIGHT = 217;
    public final static char CHAR_TOP_LEFT = 218;
    
    public final static char CHAR_LIGHT = 176;
    public final static char CHAR_MEDIUM = 177;
    public final static char CHAR_DARK = 178;
    public final static char CHAR_SOLID = 219;
    
    /*package*/ final Font f;
	private final Panmage font;
	//private final String text;
	/*package*/ final List<? extends CharSequence> text;
	/*package*/ final int fontNum;
	/*package*/ final float fontSize;
	//private final FinPanple size;
	/*package*/ final SizePanple size = new SizePanple();
	/*
	TODO
	probably need to handle origin too
	I believe the origin is the bottom left corner of the first line.
	So text is top/left justified even though the screen's origin is the bottom left corner.
	*/
	private int charactersPerLine = 0; // Maybe can still use this for fixed size instead of using the maximum of each line
	private int linesPerPage = 0;
	private int firstLine = 0;
	/*package*/ int radioLine = -1;
	private int cursorLine = -1;
	/*package*/ int cursorChar = -1;
	private int cursorTime = 0;
	//private boolean border = false; // Replaced by borderStyle
	private String title = null;
	private char bg = CHAR_NULL;
	private BorderStyle borderStyle = null;
	/*package*/ char charRadio = CHAR_RADIO;
	//private TextItem item = null;

	public Pantext(final String id, final Font font, final CharSequence text) {
	    this(id, font, Collections.singletonList(text));
	}
	
	public Pantext(final String id, final Font font, final String text, final int charactersPerLine) {
	    this(id, font, split(text, charactersPerLine));
	    this.charactersPerLine = charactersPerLine;
	}
	
	public Pantext(final String id, final Font font, final List<? extends CharSequence> text) {
	    this(id, font, text, 0);
	}
	
	public Pantext(final String id, final Font font, final List<? extends CharSequence> text, final int charactersPerLine) {
		super(id);
		f = font;
		this.font = font.getImage();
		this.text = text;
		fontNum = font.getRowAmount();
		fontSize = this.font.getSize().getX() / fontNum;
		//size = new FinPanple(fontSize * text.length(), fontSize, 0);
	}
	
	/*package*/ final class SizePanple extends UnmodPanple {

        @Override
        public float getX() {
            return getNumColumns() * fontSize;
        }

        @Override
        public float getY() {
            return getNumRows() * fontSize;
        }

        @Override
        public float getZ() {
            return 0;
        }
	}
	
	protected final int getNumColumns() {
	    int max = 0;
        final int stop = getStopLine();
        //for (final String line : text) {
        for (int i = firstLine; i < stop; i++) {
            final CharSequence line = text.get(i);
            int lineLength = line.length();
            if (i == cursorLine) {
                //lineLength = Math.max(lineLength, cursorChar + 1); // Moving cursor would change size?
                lineLength++; // Only adding characters changes size?
            }
            max = Math.max(max, lineLength);
        }
        if (title != null) {
            max = Math.max(max, title.length() + 1);
        }
        return max;
	}
	
	protected final int getNumRows() {
	    //return text.size(); // Old
        //return (getStopLine() - firstLine); // Wrong?
        return linesPerPage <= 0 ? text.size() : linesPerPage;
	}

	@Override
	protected void updateView() {		
	}

	@Override
	public Pansplay getCurrentDisplay() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void renderView(final Panderer renderer) {
		final Panple pos = getPosition();
		final float x = pos.getX();
		final float y = pos.getY();
		final float z = pos.getZ();
		final Panlayer layer = getLayer();
		/*int textSize = text.length(), off = 0;
		while (true) {
		    int indent = 0;
		    for (indent = 0; indent < textSize; indent++) {
		        if (text.charAt(off + indent) != ' ') {
		            break;
		        }
		    }
		    textSize -= indent;
		    off += indent;
		    if (textSize <= 0) {
		        break;
		    }
		    final int lineSize;
		    if (textSize <= charactersPerLine) {
		        lineSize = textSize;
		    } else {
		        //if (text.charAt(off + charactersPerLine) == ' ') {
		        //    lineSize = charactersPerLine;
		        //}
		        int lim = charactersPerLine;
		        for (int i = charactersPerLine; i > 0; i--) {
		            if (text.charAt(off + i) == ' ') {
		                lim = i;
		                break;
		            }
		        }
		        lineSize = lim;
		    }*/
		//int j = 0;
		final int height = getNumRows();
		//final int stop = getStopLine();
		final int stop = height + firstLine;
        //for (final String line : text) {
		//int maxLineSize = 0;
		//final int maxLineSize = getNumColumns();
		final int maxLineSize = charactersPerLine > 0 ? charactersPerLine : getNumColumns();
        for (int j = firstLine; j < stop; j++) {
            final CharSequence line = Coltil.get(text, j);
		    //final int lineSize = line.length();
		    //maxLineSize = Math.max(lineSize, maxLineSize);
    		//render(renderer, layer, x, y, z, line, lineSize, 0, j);
		    render(renderer, layer, x, y, z, line, maxLineSize, 0, j);
    		//j++;
    		/*textSize -= lineSize;
    		off += lineSize;*/
		}
        
        if (title != null) {
            final int lineSize = title.length();
            //maxLineSize = Math.max(lineSize + 1, maxLineSize);
            render(renderer, layer, x, y, z, title, lineSize, 1, -2);
            renderTop(renderer, layer, x, y, z, 0, lineSize + 1, -3, 1);
            //render(renderer, layer, x, y, z, title, maxLineSize - 1, 1, -2);
            //renderTop(renderer, layer, x, y, z, 0, maxLineSize, -3, 1);
        }
        
        if (radioLine >= 0) {
            render(renderer, layer, x, y, z, charRadio, -1, radioLine);
        }
        
        if (cursorLine >= 0 && cursorChar >= 0) { // && cursorLine on page
            if (cursorTime < 6) {
                final Panteraction interaction = Pangine.getEngine().getInteraction();
                final char ch = interaction.isInsEnabled() ? CHAR_CURSOR_INS : CHAR_CURSOR;
                render(renderer, layer, x, y, z + 1, ch, cursorChar, cursorLine);
            }
            cursorTime++;
            if (cursorTime >= 12) {
                cursorTime = 0;
            }
        }
        
        if (borderStyle != null) {
            //final int lineLim = (charactersPerLine > 0 ? charactersPerLine : maxLineSize) + 1;
        	//final int lineLim = maxLineSize + 1;
            //final int height = stop - firstLine;
            renderTop(renderer, layer, x, y, z, -1, maxLineSize, -1, height);
            render(renderer, layer, x, y, z, CHAR_BOTTOM_LEFT, -1, height);
            final char bottom = borderStyle == BorderStyle.Simple ? CHAR_HORIZ : CHAR_BOTTOM;
            for (int i = 0; i < maxLineSize; i++) {
                render(renderer, layer, x, y, z, bottom, i, height);
            }
            render(renderer, layer, x, y, z, CHAR_BOTTOM_RIGHT, maxLineSize, height);
        }
        
		//renderer.render(font, x, y, z, 8, 32, fontSize, fontSize);
	}
	
	private final void renderTop(final Panderer renderer, final Panlayer layer,
	                             final float x, final float y, final float z,
	                             final int lineStart, final int lineLim,
	                             final int j, final int height) {
	    if (borderStyle == null) {
	        return;
	    }
	    
	    render(renderer, layer, x, y, z, CHAR_TOP_LEFT, lineStart, j); // xoff = -2 if radio
	    final boolean simple = borderStyle == BorderStyle.Simple;
	    final char top = simple ? CHAR_HORIZ : CHAR_TOP;
        for (int i = lineStart + 1; i < lineLim; i++) {
            render(renderer, layer, x, y, z, top, i, j);
        }
        render(renderer, layer, x, y, z, CHAR_TOP_RIGHT, lineLim, j);
        
        final int vertStart = j + 1, vertStop = height + vertStart;
        final char left = simple ? CHAR_VERT : CHAR_LEFT;
        for (int i = vertStart; i < vertStop; i++) {
            render(renderer, layer, x, y, z, left, lineStart, i);
        }
        final char right = simple ? CHAR_VERT : CHAR_RIGHT;
        for (int i = vertStart; i < vertStop; i++) {
            render(renderer, layer, x, y, z, right, lineLim, i);
        }
	}
	
	private final void render(final Panderer renderer, final Panlayer layer,
	                          final float x, final float y, final float z,
	                          final CharSequence line, final int lineSize, final int off, final int j) {
	    for (int i = 0; i < lineSize; i++) {
	        final int offi = off + i;
	        if (bg != CHAR_NULL) { // Also above, move into render?
                render(renderer, layer, x, y, z - 1, bg, offi, j);
            }
            //final char c = text.charAt(off + i);
            final char c = Chartil.charAt(line, i);
            if (c > 255) {
                continue;
            } else if (c == CHAR_NULL || c == CHAR_SPACE) {
                continue;
            }
            //render(renderer, layer, x, y, z, c, i, j);
            render(renderer, layer, x, y, z, c, offi, j);
        }
	}
	
	private final void render(final Panderer renderer, final Panlayer layer,
	                          final float x, final float y, final float z,
	                          final char c, final int i, final int j) {
	    final int index = f.getIndex(c);
	    if (index == Font.INDEX_EMPTY) {
	        return;
	    } else if (index == Font.INDEX_ILLEGAL) {
	        throw new IllegalArgumentException("Cannot render " + c + " with " + f.getClass().getName());
	    }
	    final float xoff = BaseFont.getColumn(index, fontNum) * fontSize;
        final float yoff = BaseFont.getRow(index, fontNum) * fontSize;
        renderer.render(layer, font, x + (i * fontSize), y - ((j - firstLine) * fontSize), z, xoff, yoff, fontSize, fontSize);
	}
	
	//public final void setCharactersPerLine(final int charactersPerLine) {
	//    this.charactersPerLine = charactersPerLine;
	//}
	
	public final void setLinesPerPage(final int linesPerPage) {
	    this.linesPerPage = linesPerPage;
	}
	
	public final int getLinesPerPage() {
	    return linesPerPage;
	}
	
	public final void setFirstLine(final int firstLine) {
	    this.firstLine = firstLine;
	}
	
	private final boolean scroll(final int numLines) {
	    if (firstLine >= text.size() - numLines) {
            return false;
        }
        firstLine += numLines;
        return true;
	}
	
	public final boolean scrollLine() {
	    return scroll(1);
	}
	
	public final boolean scrollPage() {
	    return scroll(linesPerPage);
    }
	
	private final int getStopLine() {
        final int totalLines = text.size();
        return linesPerPage <= 0 ? totalLines : Math.min(totalLines, firstLine + linesPerPage);
    }
	
	public final void setRadioLine(final int radioLine) {
	    this.radioLine = radioLine;
	}
	
	public final void incRadioLine() {
	    radioLine++;
	    if (radioLine >= getStopLine()) {
	        radioLine = firstLine;
	    }
	}
	
	public final void decRadioLine() {
	    radioLine--;
	    if (radioLine < firstLine) {
	        radioLine = getStopLine() - 1;
	    }
	}
	
	public final void setCursor(final int cursorLine, final int cursorChar) {
	    this.cursorLine = cursorLine;
	    this.cursorChar = cursorChar;
	}
	
	public final void incCursor() {
	    if (cursorChar == text.get(cursorLine).length()) {
	        if (cursorLine < text.size() - 1) {
	            cursorLine++;
	            cursorChar = 0;
	        }
	    } else {
	        cursorChar++;
	    }
	}
	
	public final void decCursor() {
        if (cursorChar == 0) {
            if (cursorLine > 0) {
                cursorLine--;
                cursorChar = text.get(cursorLine).length();
            }
        } else {
            cursorChar--;
        }
    }
	
	//public final void setBorder(final boolean border) {
	//    this.border = border;
	//}
	
	public final void setTitle(final String title) {
	    this.title = title;
	}
	
	public final void setBackground(final char bg) {
	    this.bg = bg;
	}
	
	public final void setBorderStyle(final BorderStyle borderStyle) {
        this.borderStyle = borderStyle;
    }
	
	public final void centerX() {
		final Panple pos = getPosition();
		pos.setX(pos.getX() - (size.getX() / 2));
	}
	
	public final Font getFont() {
	    return f;
	}
	
	private final static Pattern PAT_BR = Pattern.compile("[\r\n]");
	
	public final static List<String> split(final String text, final int charactersPerLine) {
		//TODO option not to treat '\n' as a line break?
        final String[] tokens = PAT_BR.split(text);
		final List<String> list = new ArrayList<String>();
		final int size = tokens.length;
		for (int i = 0; i < size; i++) {
			splitIntern(list, tokens[i], charactersPerLine);
		}
		return list;
	}
	
	private final static List<String> splitIntern(final List<String> list, final String text, final int charactersPerLine) {
        int textSize = text.length(), off = 0;
        while (true) {
            //TODO '\t'?
            int indent;
            for (indent = 0; indent < textSize; indent++) {
                if (text.charAt(off + indent) != ' ') {
                    break;
                }
            }
            textSize -= indent;
            off += indent;
            if (textSize <= 0) {
                break;
            }
            final int lineSize;
            if (textSize <= charactersPerLine) {
                lineSize = textSize;
            } else {
                /*if (text.charAt(off + charactersPerLine) == ' ') {
                    lineSize = charactersPerLine;
                }*/
                int lim = charactersPerLine;
                for (int i = charactersPerLine; i > 0; i--) {
                    if (text.charAt(off + i) == ' ') {
                        lim = i;
                        break;
                    }
                }
                lineSize = lim;
            }
            list.add(text.substring(off, lineSize + off));
            textSize -= lineSize;
            off += lineSize;
        }
        return list;
    }
	
	///*package*/ final void setItem(final TextItem item) {
	//	this.item = item;
	//}
}
