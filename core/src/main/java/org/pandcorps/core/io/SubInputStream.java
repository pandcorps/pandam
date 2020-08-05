/*
Copyright (c) 2009-2020, Andrew M. Martin
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
package org.pandcorps.core.io;

import java.io.*;

public final class SubInputStream extends FilterInputStream {
    private int remaining;
    
    public SubInputStream(final InputStream in, final int size) {
        super(in);
        remaining = size;
    }
    
    @Override
    public final int read() throws IOException {
        if (remaining > 0) {
            remaining--;
            return super.read();
        }
        return -1;
    }
    
    @Override
    public final int read(final byte b[], final int off, int len) throws IOException {
        if (remaining <= 0) {
            return -1;
        } else if (len > remaining) {
            len = remaining;
        }
        remaining -= len;
        return in.read(b, off, len);
    }
    
    @Override
    public final long skip(long n) throws IOException {
        if (n > remaining) {
            n = remaining;
        }
        remaining -= n;
        return in.skip(n);
    }
    
    @Override
    public final boolean markSupported() {
        return false;
    }
    
    @Override
    public final int available() throws IOException {
        return Math.min(remaining, in.available());
    }
}
