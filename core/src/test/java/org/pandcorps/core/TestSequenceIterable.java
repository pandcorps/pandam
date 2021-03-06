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
package org.pandcorps.core;

import java.util.*;

import org.pandcorps.core.col.*;
import org.pandcorps.test.*;

public class TestSequenceIterable extends Pantest {
    public void testIterate() {
        final List<String> empty = Collections.emptyList(), one = Collections.singletonList("one");
        final List<String> two = Arrays.asList("two", "three");
        runIterate(empty, SequenceIterable.create(empty, null));
        runIterate(empty, SequenceIterable.create(null, empty));
        runIterate(empty, SequenceIterable.create(empty, empty));
        runIterate(one, SequenceIterable.create(empty, one));
        runIterate(two, SequenceIterable.create(two, empty));
        runIterate(Arrays.asList("one", "two", "three"), SequenceIterable.create(one, two));
    }
    
    private void runIterate(final List<String> ex, final Iterable<String> raw) {
        final Iterator<String> iter = ex.iterator();
        for (final String elem : raw) {
            assertEquals(iter.next(), elem);
        }
        assertFalse(iter.hasNext());
    }
}
