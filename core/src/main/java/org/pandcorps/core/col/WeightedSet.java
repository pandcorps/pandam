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

public class WeightedSet<E> {
    private final Map<E, Integer> weights = new HashMap<E, Integer>();
    private int sum = 0;
    
    public final void add(final E element, final int weight) {
        final Integer old = weights.put(element, Integer.valueOf(weight));
        if (old != null) {
            throw new IllegalArgumentException("Duplicate entries for " + element + ", " + weight + " and " + old);
        }
        sum += weight;
    }
    
    public final E rnd() {
        int r = Mathtil.randi(0, sum - 1);
        for (final Entry<E, Integer> entry : weights.entrySet()) {
            final int weight = entry.getValue().intValue();
            if (r < weight) {
                return entry.getKey();
            }
            r -= weight;
        }
        throw new IllegalStateException("Exhausted set, r = " + r);
    }
    
    public final void clear() {
        weights.clear();
        sum = 0;
    }
    
    public final void setAll(final WeightedSet<E> set) {
        clear();
        weights.putAll(set.weights);
        sum = set.sum;
    }
    
    public final Map<E, Integer> getWeights() {
        return weights;
    }
}
