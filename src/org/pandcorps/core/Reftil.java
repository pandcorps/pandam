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

import java.lang.reflect.*;

// Reflect Util
public final class Reftil {
	private final static Class<?>[] EMPTY_ARRAY_CLASS = {};
	private final static Object[] EMPTY_ARRAY_OBJECT = {};

	private Reftil() {
		throw new Error();
	}

	public final static Object newInstance(final String className) {
		final Class<?> c;
		try {
			c = Class.forName(className);
		} catch (final ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return newInstance(c);
	}

	public final static <T> T newInstance(final Class<T> c) {
	    return newInstance(getConstructor(c));
	}
	
	public final static <T> T newInstance(final Class<T> c, final Object... a) {
		final int size = Coltil.size(a);
		final Class<?>[] ca = new Class<?>[size];
		for (int i = 0; i < size; i++) {
			ca[i] = a[i].getClass();
		}
		return newInstance(getConstructor(c, ca), a);
	}
	
	public final static <T> T newInstance(final Constructor<T> c) {
		return newInstance(c, EMPTY_ARRAY_OBJECT);
	}
	
	public final static <T> T newInstance(final Constructor<T> c, final Object... a) {
        try {
            return c.newInstance(a);
        } catch (final Exception e) {
            throw Pantil.toRuntimeException(e);
        }
    }
	
	public final static <T> Constructor<T> getConstructor(final Class<T> c) {
		return getConstructor(c, EMPTY_ARRAY_CLASS);
	}
	
	public final static <T> Constructor<T> getConstructor(final Class<T> c, final Class<?>... a) {
		try {
			final Constructor<T> constructor = c.getDeclaredConstructor(a);
			constructor.setAccessible(true);
			return constructor;
		} catch (final Exception e) {
			throw Pantil.toRuntimeException(e);
		}
	}

	public final static String getClassName(final Object o) {
		return o == null ? null : o.getClass().getName();
	}
}
