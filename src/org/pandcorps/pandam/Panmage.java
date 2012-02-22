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
package org.pandcorps.pandam;

import org.pandcorps.pandam.impl.*;

// Pandam Image
public abstract class Panmage extends BasePantity implements Panview, Pansplay {
	private final Panple origin;
	private final Panple boundMin;
	private Panple boundMax;

	protected Panmage(final String id, final Panple origin, final Panple boundMin, final Panple boundMax) {
		super(id);
		this.origin = origin == null ? FinPanple.ORIGIN : origin;
		/*
		Boundaries should be relative to the image origin.
		The default boundaries are the image corners.
		So if a non-default origin is given,
		then the default boundaries must be offset from that.
		
		The origin should only be needed when rendering the image.
		The boundaries should only be needed for collision detection.
		
		See Panctor.MinPanple comments.
		*/
		this.boundMin = boundMin == null ?
			new FinPanple(-this.origin.getX(), -this.origin.getY(), -this.origin.getZ()) :
			boundMin;
		this.boundMax = boundMax; // If null, will be assigned in first getBoundingMaximum() invocation
	}

	public abstract Panple getSize();

	@Override
	public final Panple getOrigin() {
		return origin;
	}

	@Override
	public final Panple getBoundingMinimum() {
		return boundMin;
	}

	@Override
	public final Panple getBoundingMaximum() {
		if (boundMax == null) {
			final Panple size = getSize();
			boundMax = new FinPanple(
				boundMin.getX() + size.getX(),
				boundMin.getY() + size.getY(),
				boundMin.getZ() + size.getZ());
		}
		return boundMax;
	}

    protected final void render(final Panlayer layer, final float x, final float y, final float z) {
        render(layer, x, y, z, 0, false, false);
    }
	
    protected final void render(final Panlayer layer, final float x, final float y, final float z, final int rot, final boolean mirror, final boolean flip) {
        render(layer, x, y, z, rot, mirror, flip, null);
    }
    
    protected final void render(final Panlayer layer, final float x, final float y, final float z, final int rot, final boolean mirror, final boolean flip, final Panple o) {
        //render(x, y, z, 0, 0, w, h);
        final Panple origin = o == null ? getOrigin() : o;
        final float ox = origin.getX(), oy = origin.getY();
        //final boolean mirror = true;
        final Panple size = getSize();
        final float w = size.getX(), h = size.getY();
        final float orx, ory;
        final int r = rot % 4;
        switch (r) {
            case 0 :
                orx = ox;
                ory = oy;
                break;
            case 1 :
                orx = oy;
                ory = w - ox - 1;
                break;
            case 2 :
                orx = w - ox - 1;
                ory = h - oy - 1;
                break;
            default :
                orx = h - oy - 1;
                ory = ox;
                break;
        }
        render(layer, x - (mirror ? w - orx - 1 : orx), y - (flip ? h - ory - 1 : ory), z - origin.getZ(), 0, 0, w, h, r, mirror, flip);
    }

	protected final void render(final Panlayer layer, final float x, final float y, final float z,
		final float ix, final float iy, final float iw, final float ih) {
	    render(layer, x, y, z, ix, iy, iw, ih, 0, false, false);
	}
	
	protected abstract void render(final Panlayer layer, final float x, final float y, final float z,
        final float ix, final float iy, final float iw, final float ih, final int rot, final boolean mirror, final boolean flip);
	
	public final void destroy() {
	    Pangine.getEngine().unregister(this);
	    close();
	}
	
	protected abstract void close();
}
