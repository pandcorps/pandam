/*
Copyright (c) 2009-2021, Andrew M. Martin
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
package org.pandcorps.core.col;

import java.util.*;
import java.util.Map.*;

import org.pandcorps.core.*;

public final class CountMap<K> extends LinkedHashMap<K, Long> {
	private final static long serialVersionUID = 1L;
	
	public CountMap() {
	}
	
	public CountMap(final int initialCapacity) {
		super(initialCapacity);
	}

	public final void add(final K key, final int amount) {
		final Long val = get(key);
		put(key, Long.valueOf((val == null) ? amount : (val.longValue() + amount)));
	}
	
	public final void inc(final K key) {
	    add(key, 1);
	}
	
	public final void dec(final K key) {
        add(key, -1);
    }
	
	public final boolean decIfPositive(final K key) {
        final Long val = get(key);
        if (val == null) {
            return false;
        }
        final long lv = val.longValue();
        if (lv <= 0) {
            return false;
        }
        put(key, Long.valueOf(lv - 1));
        return true;
    }
	
	public final long longValue(final K key) {
		return Mathtil.longValue(get(key));
	}
	
	public final K getMostFrequentKey() {
	    K mostFrequentKey = null;
	    long mostFrequentCount = 0;
	    for (final Entry<K, Long> entry : entrySet()) {
	        final long count = entry.getValue().longValue();
	        if (count > mostFrequentCount) {
	            mostFrequentKey = entry.getKey();
	            mostFrequentCount = count;
	        }
	    }
	    return mostFrequentKey;
	}
	
	public List<Entry<K, Long>> sortedEntryList(final boolean asc) {
        final List<Entry<K, Long>> list = new ArrayList<Entry<K, Long>>(entrySet());
        final int sign = asc ? 1 : -1;
        Collections.sort(list, new Comparator<Entry<K, Long>>() {
            @Override public final int compare(final Entry<K, Long> o1, final Entry<K, Long> o2) {
                return sign * o1.getValue().compareTo(o2.getValue());
            }});
        return list;
    }
}
