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
import java.util.regex.*;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.*;

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
    
    private final MultiFont fonts;
    /*package*/ Font f;
	private Panmage font;
	//private final String text;
	/*package*/ final List<? extends CharSequence> text;
	/*package*/ int fontNum;
	/*package*/ float fontWidth;
	/*package*/ float fontHeight;
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
	private boolean cursorEnabled = true;
	private boolean underlineEnabled = false;
	private boolean borderEnabled = false;
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
		this(id, new MultiFont(new FontLayer(font, FinPanple.ORIGIN)), text, charactersPerLine);
	}
	
	public Pantext(final String id, final MultiFont fonts, final CharSequence text) {
	    this(id, fonts, Collections.singletonList(text));
	}
	
	public Pantext(final String id, final MultiFont fonts, final List<? extends CharSequence> text) {
		this(id, fonts, text, 0);
	}
	
	public Pantext(final String id, final MultiFont fonts, final List<? extends CharSequence> text, final int charactersPerLine) {
		super(id);
		this.text = text;
		this.fonts = fonts;
		init();
	}
	
	private final void init() {
		set(fonts.layers[0].font);
	}
	
	private final void set(final Font font) {
		f = font;
		this.font = font.getImage();
		fontNum = font.getRowAmount();
		final Panple fontSize = this.font.getSize();
		fontWidth = fontSize.getX() / fontNum;
		fontHeight = fontSize.getY() / fontNum;
		//size = new FinPanple(fontSize * text.length(), fontSize, 0);
	}
	
	/*package*/ final class SizePanple extends UnmodPanple {

        @Override
        public float getX() {
            return getNumColumns() * fontWidth;
        }

        @Override
        public float getY() {
            return getNumRows() * fontHeight;
        }

        @Override
        public float getZ() {
            return 0;
        }
	}
	
	// Number of columns on current page
	protected final int getNumColumns() {
	    return getNumColumns(firstLine, getStopLine());
	}
	
	private final int getMaxColumns() {
	    return getNumColumns(0, text.size());
	}
	
	private final int getNumColumns(final int start, final int stop) {
	    int max = 0;
        //for (final String line : text) {
        for (int i = start; i < stop; i++) {
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
        return isPagingEnabled() ? linesPerPage : text.size();
	}
	
	protected final boolean isPagingEnabled() {
	    return linesPerPage > 0;
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
	    if (!isVisible()) {
	        return;
	    }
		final Panple pos = getPosition();
		final float x = pos.getX();
		final float y = pos.getY();
		final float z = pos.getZ();
		for (final FontLayer layer : fonts.layers) {
			set(layer.font);
			final Panple off = layer.off;
			renderFont(renderer, x + off.getX(), y + off.getY(), z + off.getZ());
		}
		init();
	}
	
	private final void renderFont(final Panderer renderer, final float x, final float y, final float z) {
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
        
        // Character rendering accounts for first line of page; border is independent; this will undo that
        final float by = y - firstLine * fontHeight;
        
        if (title != null) {
            final int lineSize = title.length();
            //maxLineSize = Math.max(lineSize + 1, maxLineSize);
            render(renderer, layer, x, y, z, title, lineSize, 1, -2);
            renderTop(renderer, layer, x, by, z, 0, lineSize + 1, -3, 1);
            //render(renderer, layer, x, y, z, title, maxLineSize - 1, 1, -2);
            //renderTop(renderer, layer, x, y, z, 0, maxLineSize, -3, 1);
        }
        
        if (radioLine >= 0) {
            render(renderer, layer, x, y, z, charRadio, -1, radioLine);
        }
        
        if (cursorLine >= 0 && cursorChar >= 0) { // && cursorLine on page
            if (cursorEnabled && cursorTime < 6) {
                final Panteraction interaction = Pangine.getEngine().getInteraction();
                final char ch = interaction.isInsEnabled() ? CHAR_CURSOR_INS : CHAR_CURSOR;
                render(renderer, layer, x, y, z + 1, ch, cursorChar, cursorLine);
            }
            cursorTime++;
            if (cursorTime >= 12) {
                cursorTime = 0;
            }
        }
        
        if (isBorderNeeded()) {
            //final int lineLim = (charactersPerLine > 0 ? charactersPerLine : maxLineSize) + 1;
        	//final int lineLim = maxLineSize + 1;
            //final int height = stop - firstLine;
            renderTop(renderer, layer, x, by, z, -1, maxLineSize, -1, height);
            render(renderer, layer, x, by, z, CHAR_BOTTOM_LEFT, -1, height);
            final char bottom = borderStyle == BorderStyle.Simple ? CHAR_HORIZ : CHAR_BOTTOM;
            for (int i = 0; i < maxLineSize; i++) {
                render(renderer, layer, x, by, z, bottom, i, height);
            }
            render(renderer, layer, x, by, z, CHAR_BOTTOM_RIGHT, maxLineSize, height);
        }
        
		//renderer.render(font, x, y, z, 8, 32, fontSize, fontSize);
	}
	
	private final boolean isBorderNeeded() {
		return borderEnabled && borderStyle != null;
	}
	
	private final void renderTop(final Panderer renderer, final Panlayer layer,
	                             final float x, final float y, final float z,
	                             final int lineStart, final int lineLim,
	                             final int j, final int height) {
	    if (!isBorderNeeded()) {
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
            if (underlineEnabled && (!cursorEnabled || cursorChar != offi || cursorLine != j)) {
            	render(renderer, layer, x, y, z - 2, CHAR_CURSOR, offi, j);
            }
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
	    final float xoff = BaseFont.getColumn(index, fontNum) * fontWidth;
        final float yoff = BaseFont.getRow(index, fontNum) * fontHeight;
        renderer.render(layer, font, x + (i * fontWidth), y - ((j - firstLine) * fontHeight), z, xoff, yoff, fontWidth, fontHeight);
	}
	
	public final int getTotalLines() {
		return text.size();
	}
	
	public final void setCharactersPerLine(final int charactersPerLine) {
	    this.charactersPerLine = charactersPerLine;
	}
	
	public final void stretchCharactersPerLineToFit() {
	    this.charactersPerLine = getMaxColumns();
	}
	
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
	    if (!isPagingEnabled()) {
	        return false;
	    // TextScreen will scroll by numLines, but it allows blank lines on the last page.
	    // So we should allow the scroll as long as there's at least one more line, even if there's not a whole page.
	    //} else if (firstLine + numLines + linesPerPage - 1 >= text.size()) {
	    } else if ((numLines > 0) && ((firstLine + 1 + linesPerPage - 1) >= text.size())) {
            return false;
        } else if ((firstLine + numLines) < 0) {
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
        return isPagingEnabled() ? Math.min(totalLines, firstLine + linesPerPage) : totalLines;
    }
	
	public final void setRadioLine(final int radioLine) {
	    //this.radioLine = radioLine; // Doesn't account for firstLine, paging
	    final int orig = this.radioLine;
	    while (this.radioLine != radioLine) {
	        incRadioLine();
	        if (this.radioLine == orig) {
	            throw new IllegalArgumentException("Invalid radioLine " + radioLine);
	        }
	    }
	}
	
	public final void incRadioLine() {
	    radioLine++;
	    if (radioLine >= getStopLine() && !scrollLine()) {
	        radioLine = 0;
	        firstLine = 0;
	    }
	}
	
	public final void decRadioLine() {
	    radioLine--;
	    if (radioLine < firstLine && !scroll(-1)) {
	        //radioLine = getStopLine() - 1;
	        final int size = text.size();
	        radioLine = size - 1;
	        if (isPagingEnabled() && radioLine >= getStopLine()) {
	            firstLine = size - linesPerPage;
	        }
	    }
	}
	
	public final int lineOf(final CharSequence s) {
	    int i = 0;
	    for (final CharSequence line : text) {
	        if (Chartil.equals(s, line)) {
	            return i;
	        }
	        i++;
	    }
	    return -1;
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
	
	public final void setCursorEnabled(final boolean cursorEnabled) {
		this.cursorEnabled = cursorEnabled;
	}
	
	public final void setUnderlineEnabled(final boolean underlineEnabled) {
		this.underlineEnabled = underlineEnabled;
	}
	
	public final void setBorderEnabled(final boolean borderEnabled) {
	    this.borderEnabled = borderEnabled;
	}
	
	public final void setTitle(final String title) {
	    this.title = title;
	}
	
	public final void setBackground(final char bg) {
	    this.bg = bg;
	}
	
	public final void setBorderStyle(final BorderStyle borderStyle) {
        this.borderStyle = borderStyle;
    }
	
	//TODO class Orientation? Justification? Center/Left/Right? Center/Min/Max? set(Justification, x)
	// Centers relative to the current position, not the screen/layer/etc.
	public final void centerX() {
		final Panple pos = getPosition();
		pos.setX(pos.getX() - (size.getX() / 2));
	}
	
	public final void uncenterX() {
		final Panple pos = getPosition();
		pos.setX(pos.getX() + (size.getX() / 2));
	}
	
	// Justifies relative to the current position, not the screen/layer/etc.
	// Justifies the chunk, not each individual line
    public final void rightJustify() {
        final Panple pos = getPosition();
        pos.setX(pos.getX() - size.getX());
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
