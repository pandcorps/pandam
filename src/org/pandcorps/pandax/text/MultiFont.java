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

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.*;

public final class MultiFont implements Font {
	protected final FontLayer[] layers;
	
	public MultiFont(final FontLayer... layers) {
		this.layers = layers;
	}
	
	@Override
	public final int getWidth() {
		// Doesn't handle Fonts with different widths or negative offsets
		int maxWidth = 0, maxOff = 0;
		for (final FontLayer layer : layers) {
			maxWidth = Math.max(maxWidth, layer.font.getWidth());
			maxOff = Math.max(maxOff, (int) layer.off.getX());
		}
		return maxWidth + maxOff;
	}
    
	@Override
    public final int getHeight() {
    	int maxHeight = 0, maxOff = 0;
		for (final FontLayer layer : layers) {
			maxHeight = Math.max(maxHeight, layer.font.getHeight());
			maxOff = Math.max(maxOff, (int) layer.off.getY());
		}
		return maxHeight + maxOff;
    }
	
	@Override
    public final Panmage getImage() {
        throw new UnsupportedOperationException();
    }
    
	@Override
    public final int getRowAmount() {
        throw new UnsupportedOperationException();
    }
    
	@Override
    public final int getAmount() {
        throw new UnsupportedOperationException();
    }
    
	@Override
    public final int getRow(final char c) {
        throw new UnsupportedOperationException();
    }
    
	@Override
    public final int getColumn(final char c) {
        throw new UnsupportedOperationException();
    }
    
	@Override
    public final int getIndex(final char c) {
	    throw new UnsupportedOperationException();
	}
	
	public final static MultiFont toMultiFont(final Font font) {
	    return (font instanceof MultiFont) ? ((MultiFont) font) : new MultiFont(new FontLayer(font, FinPanple.ORIGIN));
	}
}
