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
package org.pandcorps.core.col;

import java.util.*;

import org.pandcorps.core.*;

public final class SequenceIterable<E> implements Iterable<E> {
    
    final Iterable<Iterable<E>> sequence;
    
    public final static <E> Iterable<E> create(final Iterable<E> i1, final Iterable<E> i2) {
        if (i1 == null) {
            return i2;
        } else if (i2 == null) {
            return i1;
        }
        return new SequenceIterable<E>(i1, i2);
    }
    
    private SequenceIterable(final Iterable<E> i1, final Iterable<E> i2) {
        final ArrayList<Iterable<E>> list = new ArrayList<Iterable<E>>(2);
        list.add(i1);
        list.add(i2);
        sequence = list;
    }
    
    @Override
    public final Iterator<E> iterator() {
        return new SequenceIterator();
    }
    
    private final class SequenceIterator implements Iterator<E> {
        
        private final Iterator<Iterable<E>> siter = sequence.iterator();
        
        private Iterator<E> citer = null;
        
        private final void init() {
            while (!Coltil.hasNext(citer) && siter.hasNext()) {
                citer = siter.next().iterator();
            }
        }
        
        @Override
        public boolean hasNext() {
            init();
            return Coltil.hasNext(citer);
        }

        @Override
        public E next() {
            init();
            if (citer == null) {
                throw new NoSuchElementException();
            }
            return citer.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
