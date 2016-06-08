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
package org.pandcorps.core.io;

import java.io.*;

import org.pandcorps.core.*;

public final class CharacterAnalyzer {
    public final static void main(final String[] args) {
        try {
            new CharacterAnalyzer().runFile(args[0]);
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
    
    public final void runFile(final String loc) throws Exception {
        run(new File(loc));
    }
    
    public final void run(final File f) throws Exception {
        if (f.isDirectory()) {
            for (final File c : f.listFiles()) {
                run(c);
            }
        } else {
            final String name = f.getName();
            final int dot = name.lastIndexOf('.');
            if (dot < 0) {
                skip(name);
                return;
            }
            final String ext = name.substring(dot + 1);
            if (Chartil.inIgnoreCase(ext, "class", "jar", "zip", "png", "tif", "gif", "jpg", "bmp", "ico")) {
                return;
            } else if (!Chartil.inIgnoreCase(ext, "java", "project", "classpath", "txt", "htm", "html", "xml", "xsd", "def", "panml")) {
                skip(name);
                return;
            }
            FileInputStream in = null;
            try {
                in = new FileInputStream(f);
                run(f.getAbsolutePath(), in);
            } finally {
                Iotil.close(in);
            }
        }
    }
    
    public final void run(final String name, final InputStream _in) throws Exception {
        final BufferedInputStream in = Iotil.getBufferedInputStream(_in);
        int c, prev = 0, b = 'a';
        boolean r = false, n = false;
        while ((c = in.read()) != -1) {
            if (c == '\r') {
                r = true;
            } else if (c == '\n') {
                if (prev != '\r') {
                    n = true;
                }
            } else if (c < 9 || c == 11 || c == 12 || c > 13 && c < 32 || c > 126) {
                b = c;
            }
            prev = c;
        }
        final String eol;
        if (r) {
            eol = n ? "x" : "r";
        } else {
            eol = n ? "n" : "0";
        }
        final String bad = b == 'a' ? "___" : Chartil.padZero(b, 3);
        System.out.println(eol + " " + bad + " " + name);
    }
    
    private void skip(final String name) {
        System.err.println("Skipping " + name);
    }
}
