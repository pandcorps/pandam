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

public abstract class BaseFont implements Font {
    private final Panmage image;
    
    protected BaseFont(final Panmage image) {
        this.image = image;
    }
    
    @Override
    public final Panmage getImage() {
        return image;
    }
    
    @Override
    public final int getAmount() {
        final int r = getRowAmount();
        return r * r;
    }
    
    @Override
    public final int getRow(final char c) {
        return getRow(getIndex(c), getRowAmount());
    }
    
    @Override
    public final int getColumn(final char c) {
        return getColumn(getIndex(c), getRowAmount());
    }
    
    @Override
    public int getWidth() {
    	return (int) image.getSize().getX() / getRowAmount();
    }
    
    @Override
    public int getHeight() {
    	return (int) image.getSize().getY() / getRowAmount();
    }
    
    protected final static int getRow(final int index, final int rowAmount) {
        return index / rowAmount;
    }
    
    protected final static int getColumn(final int index, final int rowAmount) {
        return index % rowAmount;
    }
}
