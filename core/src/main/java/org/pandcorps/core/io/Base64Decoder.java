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
package org.pandcorps.core.io;

import java.io.*;

import org.pandcorps.core.*;

// java.util.Base64 added in API level 26, but our minimum supported level is 8
public class Base64Decoder extends FilterInputStream {
    private int off = 0;
    private int last = 0;
    
    protected Base64Decoder(final InputStream in) {
        super(Iotil.getBufferedInputStream(in));
    }
    
    @Override
    public final int read() throws IOException {
        int b = readIndex();
        
        if (b == -1) {
            return -1;
        }
        
        if (this.off == 0) {
            this.last = b;
            this.off++;
            b = readIndex();
            if (b == -1) {
                throw new IOException("Unexpected EOF");
            }
        }
        
        return decode(b);
    }
    
    protected final int readIndex() throws IOException {
        while (true) {
            final int i = this.in.read();
            if ((i == -1) || (i == '=')) {
                return -1;
            }
            
            final int b = getIndex(i);
            if (b != -1) {
                return b;
            }
        }
    }
    
    private final static int getIndex(final int b) {
        if ((b >= 'A') && (b <= 'Z')) {
            return b - 'A';
        } else if ((b >= 'a') && (b <= 'z')) {
            return b + 26 - 'a';
        } else if ((b >= '0') && (b <= '9')) {
            return b + 52 - '0';
        } else if (b == '+') {
            return 62;
        } else if (b == '/') {
            return 63;
        } else {
            return -1;
        }
    }
    
    private final int decode(final int b) throws IOException {
        final int ret;
        
        switch (this.off) {
            case 1:
                ret = (this.last << 2) + (b >> 4);
                break;
            case 2:
                ret = ((this.last & 15) << 4) + (b >> 2);
                break;
            default:
                ret = ((this.last & 3) << 6) + b;
                break;
        }
        this.last = b;
        this.off++;
        if (this.off == 4) {
            this.off = 0;
        }
        return ret;
    }
    
    @Override
    public final int read(final byte[] b, int off, int len) throws IOException {
        int c = 0;
        for (len += off; off < len; off++) {
            final int i = read();
            if (i == -1) {
                break;
            }
            c++;
            b[off] = (byte) i;
        }
        
        return c == 0 ? -1 : c;
    }
    
    @Override
    public final int available() throws IOException {
        final int i = this.in.available() - 1;
        return (i <= 0) ? 0 : (i >> 1);
    }
    
    @Override
    public final void mark(final int readlimit) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public final boolean markSupported() {
        return false;
    }
    
    @Override
    public final void reset() throws IOException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public final long skip(final long n) throws IOException {
        for (long i = 0; i < n; i++) {
            if (read() == -1) {
                return i;
            }
        }
        
        return n;
    }
    
    public final static byte[] decode(final String encoded) {
        final Base64Decoder in = new Base64Decoder(new ByteArrayInputStream(encoded.getBytes()));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Iotil.copy(in, out);
            out.flush();
            out.close();
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        return out.toByteArray();
    }
}
