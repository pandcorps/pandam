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
package org.pandcorps.monster;

import java.util.*;

import org.pandcorps.core.*;

public abstract class Entity extends Label {
    private final static Trade trade = new Trade();
    private final static String[] ids = new String[1024];
    
    static {
        for (int i = ids.length - 1; i >= 0; i--) {
            ids[i] = Integer.toString(i);
        }
    }
    
	protected Entity(final String name) {
	    super(name);
	}

	public abstract boolean isAvailable();

	//@OverrideMe
	public void use() {
	    // Some subclasses will override, others will not be exhaustible
	}

	public abstract void add();

	/*public final static <T extends Entity> T getEntity(Collection<T> collection, final String name) {
		for (final T element : collection) {
			if (element.getName().equals(name)) {
			    return element;
			}
		}
		throw new IllegalArgumentException(name);
	}*/
	
	protected final static String format(final String name) {
	    return name == null ? null : name.toLowerCase();
	}
	
	protected final static String format(final int id) {
        return id < 0 ? null : id >= ids.length ? Integer.toString(id) : ids[id];
    }
	
	protected static <T extends Entity> Map<String, T> map(Collection<T> collection) {
	    final HashMap<String, T> m = new HashMap<String, T>();
        for (final T element : collection) {
            m.put(format(element.name), element);
        }
        return m;
    }
	
	protected final static <T extends Entity> T get(final Map<String, T> map, final String name) {
	    if (!Chartil.isValued(name)) {
	        return null;
	    }
	    final T t = map.get(format(name));
	    if (t == null) {
	        throw new IllegalArgumentException(name);
	    }
	    return t;
    }

	public final static Entity getEntity(final String value) {
	    if (value == null || value.length() == 0) {
	        return null;
	    } else if (value.startsWith("Experience.")) {
	        return new Experience(Integer.parseInt(value.substring(11)));
	    } else if (value.startsWith("Money.")) {
            return new Money(Integer.parseInt(value.substring(6)));
        } else if (value.equals("Trade") || value.equals("trad")) {
	        return trade;
	    }
	    return Item.getItem(value);
	}

	public final static class Trade extends Entity {
	    private Trade() {
	        super("Trade");
	    }

        @Override
        public void add() {
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
	}

	public final static <E> List<E> unmod(final List<E> list) {
	    return Collections.unmodifiableList(new ArrayList<E>(list));
    }
	
	protected boolean canChoose(final Species s) {
        throw new IllegalArgumentException(toString()); 
    }
}
