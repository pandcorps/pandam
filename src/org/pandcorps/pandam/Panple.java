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

import org.pandcorps.core.Mathtil;

// Pandam Tuple
public abstract class Panple {
	public abstract float getX();

	public abstract void setX(float x);

	public abstract float getY();

	public abstract void setY(float y);

	public abstract float getZ();

	public abstract void setZ(float z);

	public void set(final float x, final float y) {
		setX(x);
		setY(y);
	}

	public void set(final float x, final float y, final float z) {
		set(x, y);
		setZ(z);
	}
	
	public void setMagnitudeDirection(final double mag, final double dir) {
		setX(Mathtil.getXf(mag, dir));
	    setY(Mathtil.getYf(mag, dir));
	}

	public void set(final Panple src) {
		set(src.getX(), src.getY(), src.getZ());
	}

	public void add(final float x, final float y) {
		set(getX() + x, getY() + y);
	}
	
	public final void add(final float x, final float y, final float minX, final float minY, final float maxX, final float maxY) {
	    set(add(getX(), x, minX, maxX), add(getY(), y, minY, maxY));
	}
	
	private final static float add(float c, final float o, final float min, final float max) {
	    c += o;
        if (c < min) {
            return min;
        } else if (c > max) {
            return max;
        }
        return c;
	}

	public void add(final float x, final float y, final float z) {
		set(getX() + x, getY() + y, getZ() + z);
	}

	public void add(final Panple src) {
		add(src.getX(), src.getY(), src.getZ());
	}
	
	public double getMagnitude() {
	    final float x = getX(), y = getY(), z = getZ();
	    return Math.sqrt(x * x + y * y + z * z);
	}
	
	public double getMagnitude2() {
        final float x = getX(), y = getY();
        return Math.sqrt(x * x + y * y);
    }
	
	public double getDirection2() {
	    // x / mag should be <= 1, but account for possible float arithmetic errors
	    final double a = Math.acos(Math.max(-1, Math.min(1, getX() / getMagnitude2()))); // 0 - pi
	    return getY() < 0 ? (Mathtil.PI2 - a) : a;
	}
	
	@Override
	public final String toString() {
	    return "(" + getX() + ", " + getY() + ", " + getZ() + ")";
	}
	
	@Override
	public final boolean equals(final Object o) {
		if (!(o instanceof Panple)) {
			return false;
		}
		final Panple p = (Panple) o;
		return getX() == p.getX() && getY() == p.getY() && getZ() == p.getZ();
	}
	
	@Override
	public final int hashCode() {
		return Float.floatToIntBits(getX()) ^ Float.floatToIntBits(getY()) ^ Float.floatToIntBits(getZ());
	}
}
