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
package org.pandcorps.pandam;

import org.pandcorps.core.Mathtil;
import org.pandcorps.pandam.impl.ImplPanple;

// Pandam Tuple
public abstract class Panple {
	public abstract float getX();

	public abstract void setX(float x);

	public abstract float getY();

	public abstract void setY(float y);

	public abstract float getZ();

	public abstract void setZ(float z);
	
	public final float getC(final int i) {
		switch(i) {
			case 0 : return getX();
			case 1 : return getY();
			case 2 : return getZ();
		}
		throw new ArrayIndexOutOfBoundsException(i);
	}
	
	public final void setC(final int i, final float v) {
		switch(i) {
			case 0 : setX(v); return;
			case 1 : setY(v); return;
			case 2 : setZ(v); return;
		}
		throw new ArrayIndexOutOfBoundsException(i);
	}
	
	public final static Panple subtract(final Panple p1, final Panple p2) {
		final Panple dst = new ImplPanple();
		subtract(dst, p1, p2);
		return dst;
	}
	
	public final static void subtract(final Panple dst, final Panple p1, final Panple p2) {
		dst.set(p1.getX() - p2.getX(), p1.getY() - p2.getY(), p1.getZ() - p2.getZ());
	}

	public void set(final float x, final float y) {
		setX(x);
		setY(y);
	}

	public void set(final float x, final float y, final float z) {
		set(x, y);
		setZ(z);
	}
	
	public void setMagnitude2(final double mag) {
		final float m = (float) (mag / getMagnitude2());
		setX(getX() * m);
		setY(getY() * m);
	}
	
	public void setMagnitudeDirection(final double mag, final double dir) {
		setX(Mathtil.getXf(mag, dir));
	    setY(Mathtil.getYf(mag, dir));
	}

	public void set(final Panple src) {
		set(src.getX(), src.getY(), src.getZ());
	}
	
	public void addX(final float x) {
		setX(getX() + x);
	}
	
	public void addY(final float y) {
		setY(getY() + y);
	}
	
	public void addZ(final float z) {
		setZ(getZ() + z);
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
	
	public void multiply(final float s) {
		set(getX() * s, getY() * s, getZ() * s);
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
	
	public Panple toUnit() {
		final Panple unit = new ImplPanple(this);
		unit.multiply((float) (1.0 / unit.getMagnitude()));
		return unit;
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
