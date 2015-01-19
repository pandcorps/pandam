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
package org.pandcorps.pandax.visual;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;

public final class ScrollTexture extends Pantexture implements StepListener {
	private int velX = 0;
	private int velY = 0;
	
	public ScrollTexture(final Panmage image) {
        super(image);
    }
	
	public ScrollTexture(final String id, final Panmage image) {
		super(id, image);
	}
	
	public final void setVelocity(final int velX, final int velY) {
		this.velX = velX;
		this.velY = velY;
	}
	
	@Override
	public final void onStep(final StepEvent event) {
		final Panple size = image.getSize();
		if (velX != 0) {
			offX = add(offX, velX, size.getX());
		}
		if (velY != 0) {
			offY = add(offY, velY, size.getY());
		}
	}
	
	private final static int add(int off, final int vel, final float size) {
		final int s = (int) size;
		off += vel;
		if (off < 0) {
			off += s;
		} else if (off >= s) {
			off -= s;
		}
		return off;
	}
}
