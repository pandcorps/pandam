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
package org.pandcorps.pandam.lwjgl;

import org.pandcorps.pandam.*;

public final class LwjglPanplementation extends Panplementation {
	/*package*/ float x = 0;
	/*package*/ float y = 0;
	/*package*/ float z = 0;
	private final LwjglPanple pos = new LwjglPanple(this);
	private boolean vis;
	private int rot;
	private boolean mirror;
	private boolean flip;
	private Panmage image = null;

	public LwjglPanplementation(final Panctor actor) {
		super(actor);

		// Default values should probably be maintained in super class;
		// maybe these fields and methods could exist entirely in super class too
		setVisible(true);
		setRot(0);
		setMirror(false);
		setFlip(false);
	}

	@Override
	public final Panple getPosition() {
		return pos;
	}

	@Override
	public final boolean isVisible() {
		return vis;
	}

	@Override
	public final void setVisible(final boolean vis) {
		this.vis = vis;
	}
	
	@Override
	public final int getRot() {
	    return rot;
	}
	
	@Override
	public final void setRot(final int rot) {
	    this.rot = rot;
	}
	
	@Override
    public final boolean isMirror() {
        return mirror;
    }

    @Override
    public final void setMirror(final boolean mirror) {
        this.mirror = mirror;
    }
    
    @Override
    public final boolean isFlip() {
        return flip;
    }

    @Override
    public final void setFlip(final boolean flip) {
        this.flip = flip;
    }

	@Override
	protected final void updateView(final Panmage image) {
		//this.image = (LwjglPanmage) image;
	    this.image = image; // Might be EmptyPanmage
	}

	@Override
	protected final void renderView() {
		if (!vis) {
			return;
		}
		//image.render(actor.getLayer(), x, y, z, mirror ^ currMirror, flip ^ currFlip);
		render(image, x, y, z, rot + currRot, mirror ^ currMirror, flip ^ currFlip, currOrigin);
	}
}
