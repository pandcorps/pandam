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
package org.pandcorps.pandam.impl;

import org.pandcorps.core.seg.*;
import org.pandcorps.pandam.*;

public final class ImplPanple extends Panple {
	private float x;
	private float y;
	private float z;

	public ImplPanple() {
		this(0, 0, 0);
	}
	
	public ImplPanple(final Panple p) {
		this(p.getX(), p.getY(), p.getZ());
	}
	
	public ImplPanple(final float x, final float y, final float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public final float getX() {
		return x;
	}

	@Override
	public final void setX(final float x) {
		this.x = x;
	}

	@Override
	public final float getY() {
		return y;
	}

	@Override
	public final void setY(final float y) {
		this.y = y;
	}

	@Override
	public final float getZ() {
		return z;
	}

	@Override
	public final void setZ(final float z) {
		this.z = z;
	}

	@Override
	public final void set(final float x, final float y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public final void set(final float x, final float y, final float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public final void add(final float x, final float y) {
		this.x += x;
		this.y += y;
	}

	@Override
	public final void add(final float x, final float y, final float z) {
		this.x += x;
		this.y += y;
		this.z += z;
	}
	
	public final static ImplPanple getImplPanple(final Segment seg, final int i) {
        return getImplPanple(seg.getField(i));
    }
    
    public final static ImplPanple getImplPanple(final Field fld) {
        final FinPanple f = FinPanple.getFinPanple(fld);
        return f == null ? null : new ImplPanple(f.getX(), f.getY(), f.getZ());
    }
}
