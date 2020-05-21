/*
Copyright (c) 2009-2020, Andrew M. Martin
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
package org.pandcorps.pandax.visual;

import org.pandcorps.pandam.*;

public class Pantexture extends Panctor {
	protected Panmage image = null;
	protected int offX = 0;
	protected int offY = 0;
	protected int width = 0;
	protected int height = 0;
	
	public Pantexture() {
    }
	
	public Pantexture(final Panmage image) {
	    this.image = image;
	}
	
	public Pantexture(final String id, final Panmage image) {
		super(id);
		this.image = image;
	}
	
	public final void setImage(final Panmage image) {
	    this.image = image;
	}
	
	public final Panmage getImage() {
	    return image;
	}
	
	public final void setOffset(final int offX, final int offY) {
		this.offX = offX;
		this.offY = offY;
	}
	
	public final int gettOffsetX() {
	    return offX;
	}
	
	public final int gettOffsetY() {
        return offY;
    }
	
	public final void setSize(final int width, final int height) {
		this.width = width;
		this.height = height;
	}
	
	public final int getWidth() {
	    return width;
	}
	
	public final int getHeight() {
        return height;
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
		renderer.render(getLayer(), image, pos.getX(), pos.getY(), pos.getZ(), offX, offY, width, height);
	}
}
