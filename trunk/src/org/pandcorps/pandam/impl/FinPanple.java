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

import org.pandcorps.core.*;
import org.pandcorps.core.seg.*;

public final class FinPanple extends UnmodPanple {
	public final static FinPanple ORIGIN = new FinPanple(0, 0, 0);

	private final float x;
	private final float y;
	private final float z;

	public FinPanple(final float x, final float y, final float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public final static FinPanple newMagnitudeDirection(final double mag, final double dir, final float z) {
		return new FinPanple(Mathtil.getXf(mag, dir), Mathtil.getYf(mag, dir), z);
	}

	@Override
	public final float getX() {
		return x;
	}

	@Override
	public final float getY() {
		return y;
	}

	@Override
	public final float getZ() {
		return z;
	}
	
	public final static FinPanple getFinPanple(final Segment seg, final int i, final float dx, final float dy, final float dz) {
	    final FinPanple p = getFinPanple(seg, i);
	    return p == null ? new FinPanple(dx, dy, dz) : p;
	}
	
	public final static FinPanple getFinPanple(final Segment seg, final int i) {
        return getFinPanple(seg.getField(i));
    }
    
    public final static FinPanple getFinPanple(final Field fld) {
        if (fld == null) {
            return null;
        }
        final Float x = fld.getFloat(0);
        final Float y = fld.getFloat(1);
        final Float z = fld.getFloat(2);
        if (x == null && y == null && z == null) {
            return null;
        }
        return new FinPanple(Mathtil.floatValue(x), Mathtil.floatValue(y), Mathtil.floatValue(z));
    }
}
