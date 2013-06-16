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
package org.pandcorps.core;

import java.util.*;

// Math Utility
public final class Mathtil {
    public final static byte BYTE_0 = 0;
    public final static short SHORT_0 = 0;
    public final static double PI2 = 2.0 * Math.PI;
    
	private final static Random rand = new Random();

	private Mathtil() {
		throw new Error();
	}
	
	public final static boolean rand() {
	    return rand.nextBoolean();
	}
	
	public final static boolean rand(final int percentage) {
	    return randi(0, 100) < percentage;
	}
	
	public final static byte randb(final byte min, final byte max) {
		return (byte) randi(min, max);
	}

	public final static int randi(final int min, final int max) {
		return rand.nextInt(max + 1 - min) + min;
	}

	public final static float randf(final float min, final float max) {
		return rand.nextFloat() * (max - min) + min;
	}
	
	/**
	 * Retrieves a random element of the given array
	 * 
	 * @param <E> the element type
	 * @param array the array
	 * @return the element
	 */
	public final static <E> E rand(final E... array) {
	    return array[randi(0, array.length - 1)];
	}
	
	public final static <E> E rand(final List<E> list) {
        return list.get(randi(0, list.size() - 1));
    }
	
	public final static <E> E rand(final int[] weights, final E... array) {
	    int sum = 0;
	    for (final int weight : weights) {
	        sum += weight;
	    }
	    final int r = randi(0, sum);
	    final int size = array.length;
	    sum = 0;
	    for (int i = 0; i < size; i++) {
	        sum += weights[i];
	        if (r <= sum) {
	            return array[i];
	        }
	    }
	    if (size != weights.length) {
	        throw new IllegalArgumentException("Weights length " + weights.length + " did not equal array length " + size);
	    }
	    throw new RuntimeException("Internal logic error");
	}
	
	public final static int ceil(final float f) {
		final int i = (int) f;
		final float fi = i;
		return (f == fi) ? i : (i + 1);
	}
	
	public final static int max(final int... a) {
		return bound(true, a);
	}
	
	public final static int min(final int... a) {
		return bound(false, a);
	}
	
	private final static int bound(final boolean max, final int... a) {
		int m = a[0];
		for (int i = a.length - 1; i > 0; i--) {
			final int e = a[i];
			if (e > m == max) {
				m = e;
			}
		}
		return m;
	}
	
	public final static double getX(final double mag, final double dir) {
		return Math.cos(dir) * mag;
	}
	
	public final static double getY(final double mag, final double dir) {
		return Math.sin(dir) * mag;
	}
	
	public final static float getXf(final double mag, final double dir) {
		return (float) getX(mag, dir);
	}
	
	public final static float getYf(final double mag, final double dir) {
		return (float) getY(mag, dir);
	}
	
	public final static int byteValue(final Number n) {
        return byteValue(n, BYTE_0);
    }
    
    public final static byte byteValue(final Number n, final byte def) {
        return n == null ? def : n.byteValue();
    }
    
    public final static short shortValue(final Number n) {
        return shortValue(n, SHORT_0);
    }
    
    public final static short shortValue(final Number n, final short def) {
        return n == null ? def : n.shortValue();
    }
	
	public final static int intValue(final Number n) {
        return intValue(n, 0);
    }
    
    public final static int intValue(final Number n, final int def) {
        return n == null ? def : n.intValue();
    }
	
	public final static float floatValue(final Number n) {
	    return floatValue(n, 0);
	}
	
	public final static float floatValue(final Number n, final float def) {
	    return n == null ? def : n.floatValue();
	}
	
	public final static Byte toByte(final String value) {
        return Chartil.isValued(value) ? Byte.valueOf(value) : null;
    }
	
	public final static Short toShort(final String value) {
        return Chartil.isValued(value) ? Short.valueOf(value) : null;
    }
	
	public final static Integer toInteger(final String value) {
        return Chartil.isValued(value) ? Integer.valueOf(value) : null;
    }
    
	public final static Float toFloat(final String value) {
        return Chartil.isValued(value) ? Float.valueOf(value) : null;
    }
}
