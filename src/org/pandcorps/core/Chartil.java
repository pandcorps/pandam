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

public final class Chartil {
    private Chartil() {
        throw new Error();
    }
    
    public final static int size(final CharSequence s) {
        return s == null ? 0 : s.length();
    }
    
    public final static boolean isValued(final CharSequence s) {
        return size(s) > 0;
    }
    
    public final static boolean isEmpty(final CharSequence s) {
        return size(s) == 0;
    }
    
    public final static String toString(final Object o) {
        return o == null ? null : o.toString();
    }
    
    public final static String toUpperCase(final String s) {
        return s == null ? null : s.toUpperCase();
    }
    
    public final static char charAt(final CharSequence s, final int i) {
        return size(s) <= i ? 0 : s.charAt(i);
    }
    
    public final static boolean startsWithIgnoreCase(final CharSequence s, final CharSequence sub) {
        // String.startsWith(String) is true if sub is empty or equals s
        final int subSize = size(sub);
        if (subSize == 0) {
            return true;
        } else if (size(s) < subSize) {
            return false;
        }
        for (int i = 0; i < subSize; i++) {
            if (!equalsIgnoreCase(s.charAt(i), sub.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    public final static boolean equalsIgnoreCase(final char c1, final char c2) {
        return Character.toLowerCase(c1) == Character.toLowerCase(c2);
    }
    
    public final static boolean inIgnoreCase(final String s, final String... a) {
        for (final String as : a) {
            if (s.equalsIgnoreCase(as)) {
                return true;
            }
        }
        return false;
    }
    
    public final static String padZero(final int i, final int size) {
        final String s = Integer.toString(i);
        final int dif = size - s.length();
        if (dif <= 0) {
            return s;
        }
        final StringBuilder sb = new StringBuilder(size);
        for (int j = 0; j < dif; j++) {
            sb.append(' ');
        }
        sb.append(s);
        return sb.toString();
    }
    
    public final static String remove(final String s, final char toRemove) {
    	StringBuilder b = null;
    	final int size = size(s);
    	int start = 0;
    	for (int i = 0; i < size; i++) {
    		if (s.charAt(i) == toRemove) {
    			if (b == null) {
    				b = new StringBuilder();
    			}
    			b.append(s, start, i);
    			start = i + 1;
    		}
    	}
    	if (b == null) {
    		return s;
    	}
    	b.append(s, start, size);
    	return b.toString();
    }
    
    public final static char[] concat(final char[]... a) {
    	int size = 0;
    	for (final char[] e : a) {
    		size += e.length;
    	}
    	final char[] c = new char[size];
    	int ci = 0;
    	for (final char[] e : a) {
    		size = e.length;
    		System.arraycopy(e, 0, c, ci, size);
    		ci += size;
    	}
    	return c;
    }
}
