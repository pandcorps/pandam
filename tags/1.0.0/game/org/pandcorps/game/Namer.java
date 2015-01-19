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
package org.pandcorps.game;

import java.util.*;

import org.pandcorps.core.*;

public abstract class Namer {
    public final static Namer get(final String[]... components) {
        return get(null, null, components);
    }
    
    public final static Namer get(final Manipulator manipulator, final Concatenator concatenator, final String[]... components) {
        return new SimpleNamer(manipulator, concatenator, components);
    }
    
    public final static Namer get(final Namer... namers) {
        return new MultiNamer(namers);
    }
    
    public abstract String get();
    
    public abstract int size();
    
    public final String get(final Collection<String> used) {
        // Would be redundant for MultiNamer to have a used field in Namer
        while (true) {
            final String name = get();
            if (used.add(name)) {
                return name;
            }
        }
    }
    
    public final void printDemo() {
        final int n = 50;
        final HashSet<String> used = new HashSet<String>(n);
        System.out.println(getClass().getName() + " (size=" + size() + ")");
        for (int i = 0; i < n; i++) {
            System.out.println(get(used));
        }
    }
    
    private final static class SimpleNamer extends Namer {
        private final Manipulator manipulator;
        private final Concatenator concatenator;
        private final String[][] components;
        
        public SimpleNamer(final Manipulator manipulator, final Concatenator concatenator, final String[]... components) {
            this.manipulator = manipulator;
            this.concatenator = concatenator;
            this.components = components;
        }
        
        @Override
        public final String get() {
            String prev = null;
            final StringBuilder b = new StringBuilder();
            for (final String[] component : components) {
                final String c = Mathtil.rand(component);
                if (Chartil.isEmpty(c)) {
                    continue; // Likely if first component is usually a consonant but sometimes empty
                }
                if (concatenator != null) {
                    b.append(concatenator.getDelim(prev, c));
                }
                final int size = b.length();
                if (size == 0 || b.charAt(size - 1) == ' ') {
                    b.append(Character.toUpperCase(c.charAt(0)));
                    b.append(c, 1, c.length());
                } else {
                    b.append(c);
                }
                prev = c;
            }
            final String s = b.toString();
            return manipulator == null ? s : manipulator.manipulate(s);
        }
        
        @Override
        public final int size() {
            int s = 1;
            for (final String[] component : components) {
                s *= component.length;
            }
            return s;
        }
    }
    
    private final static class MultiNamer extends Namer {
        private final Namer[] namers;
        private final int[] weights;
        
        public MultiNamer(final Namer... namers) {
            this.namers = namers;
            final int size = namers.length;
            weights = new int[size];
            for (int i = 0; i < size; i++) {
                weights[i] = namers[i].size();
            }
        }
        
        @Override
        public final String get() {
            return Mathtil.rand(weights, namers).get();
        }
        
        @Override
        public final int size() {
            int s = 0;
            for (final int w : weights) {
                s += w;
            }
            return s;
        }
    }
}
