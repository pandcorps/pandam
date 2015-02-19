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
package org.pandcorps.test.pandam;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.*;

public class UnitPanplementation extends Panplementation {
	private final Panple pos = new ImplPanple();
	private boolean vis = true;
	private int rot = 0;
	private boolean mirror = false;
	private boolean flip = false;

	public UnitPanplementation(final Panctor actor) {
		super(actor);
	}

	@Override
	protected void updateView(final Panmage image) {
	}

	@Override
	protected void renderView() {
	}

	@Override
	public Panple getPosition() {
		return pos;
	}

	@Override
	public boolean isVisible() {
		return vis;
	}

	@Override
	public void setVisible(final boolean vis) {
		this.vis = vis;
	}
	
	@Override
	public int getRot() {
	    return rot;
	}
	
	@Override
	public void setRot(final int rot) {
	    this.rot = rot;
	}
	
	@Override
    public boolean isMirror() {
        return mirror;
    }

    @Override
    public void setMirror(final boolean mirror) {
        this.mirror = mirror;
    }
    
    @Override
    public boolean isFlip() {
        return flip;
    }

    @Override
    public void setFlip(final boolean flip) {
        this.flip = flip;
    }
}
