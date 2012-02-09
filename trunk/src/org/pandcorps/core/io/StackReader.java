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
package org.pandcorps.core.io;

import java.io.*;
import java.nio.CharBuffer;
import java.util.Stack;

public class StackReader extends BufferedReader {
    
    private Stack<String> stack = new Stack<String>();
    
    public StackReader(final Reader in) {
        super(in);
    }
    
    @Override
    public void close() throws IOException {
        super.close();
        stack = null;
    }
    
    @Override
    public void mark(final int readAheadLimit) throws IOException {
        // Why extend BufferedReader if most operations will be unsupported?
        // This way we can pass a StackReader to code that already uses BufferedReader.readLine()
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean markSupported() {
        return false;
    }
    
    @Override
    public int read() throws IOException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int read(final char[] cbuf) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int read(final CharBuffer target) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int read(final char[] cbuf, final int off, final int len) throws IOException {
        throw new UnsupportedOperationException();
    }
    
    public void push(final String line) {
        stack.push(line);
    }
    
    @Override
    public String readLine() throws IOException {
        if (stack.size() > 0) {
            return stack.pop();
        }
        return super.readLine();
    }
    
    @Override
    public boolean ready() throws IOException {
        return stack.size() > 0 || super.ready();
    }
    
    @Override
    public void reset() throws IOException {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public long skip(final long n) throws IOException {
        throw new UnsupportedOperationException();
    }
}
