/*
Copyright (c) 2009-2018, Andrew M. Martin
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
package org.pandcorps.core.chr;

import org.pandcorps.core.*;

public class SubSequence implements CharSequence {
    private final CharSequence seq;
    private int end;
    
    public SubSequence(final CharSequence seq) {
        this(seq, Chartil.size(seq));
    }
    
    public SubSequence(final CharSequence seq, final int end) {
        this.seq = seq;
        this.end = end;
    }
    
    public final void setEnd(final int end) {
        this.end = end;
    }
    
    public final boolean increment() {
        if (end < getMax()) {
            end++;
            return true;
        }
        return false;
    }
    
    public final int getMax() {
        return Chartil.size(seq);
    }
    
    @Override
    public String toString() {
        // SubSequence doesn't provide extra efficiency if toString is called, but it does if only charAt and length are called for varying lengths
        return seq.subSequence(0, end).toString();
    }
    
    @Override
    public char charAt(final int i) {
        return seq.charAt(i);
    }

    @Override
    public int length() {
        return end;
    }

    @Override
    public CharSequence subSequence(final int off, final int len) {
        return seq.subSequence(off, len);
    }
}
