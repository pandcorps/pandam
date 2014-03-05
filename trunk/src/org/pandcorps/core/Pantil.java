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
package org.pandcorps.core;

import java.nio.*;
//import java.rmi.dgc.*;

// Pancorps Utility
public final class Pantil {
    private final static int INT_SIZE = Integer.SIZE / 8;
    private final static int FLOAT_SIZE = Float.SIZE / 8;
    
	private Pantil() {
		throw new Error();
	}
	
	public final static String getProperty(final String key) {
	    return System.getProperty(key);
	}
	
	public final static boolean isProperty(final String key, final boolean def) {
	    final String val = getProperty(key);
	    return Chartil.isValued(val) ? Boolean.parseBoolean(val) : def;
	}

	private static long nextId = 0;
	
	public final static String vmid() {
		//return new VMID().toString(); // Not on Android
		return Long.toString(nextId++);
	}
	
	public final static Exception toException(final Throwable e) {
        return e instanceof Exception ? (Exception) e : new Exception(e);
    }

	public final static RuntimeException toRuntimeException(final Throwable e) {
		return e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
	}

	public final static boolean equals(final Object o1, final Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}
	
	public final static <T> T nvl(final T t1, final T t2) {
		return t1 == null ? t2 : t1;
	}
	
	public final static <T> T nvl(final T t1, final T t2, final T t3) {
		return nvl(nvl(t1, t2), t3);
	}
	
	public final static <T> T nvl(final T t1, final T t2, final T t3, final T t4) {
		return nvl(nvl(t1, t2, t3), t4);
	}
	
	public final static <T> T coalesce(final T... ts) {
		if (ts == null) {
			return null;
		}
		for (final T t : ts) {
			if (t != null) {
				return t;
			}
		}
		return null;
	}
	
	public final static boolean booleanValue(final Boolean b) {
        return booleanValue(b, false);
    }
    
    public final static boolean booleanValue(final Boolean b, final boolean def) {
        return b == null ? def : b.booleanValue();
    }
	
	public final static Boolean toBoolean(final String value) {
        return Chartil.isValued(value) ? Boolean.valueOf(value) : null;
    }
	
	public final static int hashCode(final Object o) {
	    return o == null ? 0 : o.hashCode();
	}
	
	public final static FloatBuffer allocateDirectFloatBuffer(final int capacity) {
	    return allocateDirectByteBuffer(capacity * FLOAT_SIZE).asFloatBuffer();
	}
	
	public final static IntBuffer allocateDirectIntBuffer(final int capacity) {
        return allocateDirectByteBuffer(capacity * INT_SIZE).asIntBuffer();
    }
	
	public final static ByteBuffer allocateDirectByteBuffer(final int capacity) {
        return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
    }
}
