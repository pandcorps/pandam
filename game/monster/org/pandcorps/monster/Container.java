/*
Copyright (c) 2009-2016, Andrew M. Martin
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

public class Container extends Item implements Comparable<Container> {
    private static List<Container> containers = null;
    private final int rank;
    
    /*package*/ Container(final String code, final String name, final int price, final boolean exhaustible, final boolean unique, final boolean secret, final int rank) {
        super(code, name, price, exhaustible, unique, secret);
        this.rank = rank;
    }
    
    public final int getRank() {
        return rank;
    }
    
    public final static Container getContainer(final Species species) {
        final int rank = species.getRank();
        for (final Container container : containers) {
            if (container.rank >= rank) {
                return container;
            }
        }
        throw new IllegalArgumentException(species.toString());
    }

    @Override
    public int compareTo(Container o) {
        return rank < o.rank ? -1 : rank > o.rank ? 1 : 0;
    }
    
    /*package*/ final static void setContainers(final List<Item> items) {
        containers = new ArrayList<Container>();
        for (final Item item : items) {
            if (item instanceof Container) {
                final Container container = (Container) item;
                final int index = Collections.binarySearch(containers, container);
                containers.add(index < 0 ? Math.abs(index + 1) : index, container);
            }
        }
        containers = Collections.unmodifiableList(containers);
    }
}
