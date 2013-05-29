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
package org.pandcorps.game;

import org.pandcorps.core.*;

public abstract class Namer {
    public abstract String get();
    
    public abstract int size();
    
    public final static class SimpleNamer extends Namer {
        private final String[][] components;
        
        public SimpleNamer(final String[][] components) {
            this.components = components;
        }
        
        @Override
        public final String get() {
            final StringBuilder b = new StringBuilder();
            for (final String[] component : components) {
                final String c = Mathtil.rand(component);
                if (Chartil.isEmpty(c)) {
                    continue; // Likely if first component is usually a consonant but sometimes empty
                } else if (b.length() == 0) {
                    b.append(Character.toUpperCase(c.charAt(0)));
                    b.append(c, 1, c.length());
                } else {
                    b.append(c);
                }
            }
            return b.toString();
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
    
    public final static class MultiNamer extends Namer {
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
