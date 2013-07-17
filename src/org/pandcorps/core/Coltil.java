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
import java.util.*;

// Collection Utility
public final class Coltil {
	private Coltil() {
		throw new Error();
	}

	public final static int size(final Collection<?> c) {
		return c == null ? 0 : c.size();
	}

	public final static int size(final Map<?, ?> m) {
		return m == null ? 0 : m.size();
	}

	public final static int size(final Object... a) {
		return a == null ? 0 : a.length;
	}
	
	public final static int size(final Iterable<?> c) {
		return c instanceof Collection ? size((Collection<?>) c) : size(iterator(c));
	}
	
	public final static int size(final Iterator<?> ator) {
		if (ator == null) {
			return 0;
		}
		int i = 0;
		while (ator.hasNext()) {
			ator.next();
			i++;
		}
		return i;
	}
	
	public final static <E> Iterator<E> iterator(final Iterable<E> able) {
		return able == null ? null : able.iterator();
	}
	
	public final static boolean isValued(final Iterable<?> able) {
		return able == null ? false : isValued(able.iterator());
	}
	
	public final static boolean isValued(final Iterator<?> ator) {
		return ator == null ? false : ator.hasNext();
	}
	
	public final static boolean isEmpty(final Iterable<?> able) {
		return !isValued(able);
	}
	
	public final static <E, T extends E> T has(final Collection<E> c, final T e) {
		return c.contains(e) ? e : null;
	}
	
	public final static <E> List<E> asList(final E... a) {
		final int size = size((Object[]) a);
		return (size == 0 || (size == 1 && a[0] == null)) ? null : Arrays.asList(a);
	}
	
	public final static <E> Iterable<E> unnull(final Iterable<E> i) {
	    if (i == null) {
	        return Collections.emptyList();
	    }
	    return i;
	}
	
	public final static <E> Iterable<E> copy(final Iterable<E> i) {
		final ArrayList<E> list = new ArrayList<E>();
		addAll(list, i);
		return list;
	}
	
	//private final static Object[] EMPTY = {};
	
	//public final static <E> E[] unnull(final E... a) {
	//    final E[] v = new E[0];
	//    return a == null ? new E[] {} : a;
	//}
	
	public final static <E> List<E> unmodifiableList(final List<? extends E> list) {
	    return list == null ? null : Collections.unmodifiableList(list);
	}
	
	@SuppressWarnings("unchecked")
	public final static <E> E[] toArray(final Collection<E> col) {
	    //return col == null ? null : (E[]) col.toArray(); // Can't cast
	    if (col == null) {
	        return null;
	    }
	    E[] a = null;
	    int i = -1;
	    for (final E elem : col) {
	        i++;
	        if (elem == null) {
	            continue;
	        }
	        if (a == null) {
	            // The first element might be a subclass of E; other elements might not be compatible
	            a = (E[]) Array.newInstance(elem.getClass(), col.size());
	        }
	        a[i] = elem;
	    }
	    return a;
	}
	
	public final static boolean contains(final Collection<?> col, final Object o) {
		return col == null ? false : col.contains(o);
	}
	
	public final static <E> E get(final List<E> list, final int i) {
	    return size(list) <= i ? null : list.get(i);
	}
	
	public final static <E> E get(final E[] a, final int i) {
    	return size(a) <= i ? null : a[i];
    }
	
	public final static <E> E getOnly(final List<E> list) {
	    final int size = size(list);
	    if (size < 1) {
	        return null;
	    } else if (size == 1) {
	        return list.get(0);
	    }
	    throw new RuntimeException("Found " + size + " elements where no more than 1 was expected");
	}
	
	public final static <E, T extends E> void set(final List<E> list, final int i, final T elem) {
	    final int size = list.size();
	    for (int j = size; j <= i; j++) {
	        list.add(null);
	    }
	    list.set(i, elem);
	}
	
	public final static <E, T extends E> void setIfNeeded(final List<E> list, final int i, final T elem) {
		if (elem != null || (list.size() - 1) >= i) {
    		Coltil.set(list, i, elem);
    	}
	}
	
	public final static <E, T extends E> ArrayList<E> add(ArrayList<E> list, final T elem) {
	    if (list == null) {
	        list = new ArrayList<E>();
	    }
	    list.add(elem);
	    return list;
	}
	
	public final static <E, T extends E> ArrayList<E> addIfValued(ArrayList<E> list, final T elem) {
		return elem == null ? list : add(list, elem);
	}
	
	public final static <E> void addAll(final Collection<E> dst, final Iterable<E> src) {
		for (final E elem : unnull(src)) {
			dst.add(elem);
		}
	}
	
	public final static boolean hasNext(final Iterator<?> iter) {
	    return iter == null ? false : iter.hasNext();
	}
}
