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
package org.pandcorps.core;

import java.io.*;
import java.net.*;

// Input/Output Utility
public final class Iotil {
	public final static String BR = System.getProperty("line.separator");
	
	private final static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	private Iotil() {
		throw new Error();
	}

	public final static InputStream getInputStream(final String location) {
		final File f = new File(location);
		if (f.exists()) {
			try {
				return new FileInputStream(f);
			} catch (final FileNotFoundException e) {
				throw new Error(e);
			}
		}

		//return Iotil.class.getResourceAsStream(location);
		URL url = Iotil.class.getClassLoader().getResource(location);
		try
			{
			if (url == null) {
				url = new URL(location);
			}
			return url.openStream();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public final static Writer getWriter(final String location) {
		try {
			return new FileWriter(location);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public final static void close(final Closeable s) {
		if (s != null) {
			try {
				s.close();
			} catch (final Exception e) {
			    throw Pantil.toRuntimeException(e); // Sometimes might want to silently ignore
			}
		}
	}
	
	public final static BufferedInputStream getBufferedInputStream(final InputStream in) {
        return in instanceof BufferedInputStream ? (BufferedInputStream) in : new BufferedInputStream(in);
    }
	
	public final static BufferedReader getBufferedReader(final Reader in) {
	    return in instanceof BufferedReader ? (BufferedReader) in : new BufferedReader(in);
	}
	
	public final static BufferedOutputStream getBufferedOutputStream(final OutputStream out) {
	    return out instanceof BufferedOutputStream ? (BufferedOutputStream) out : new BufferedOutputStream(out);
	}
	
	public final static BufferedWriter getBufferedWriter(final Writer out) {
	    return out instanceof BufferedWriter ? (BufferedWriter) out : new BufferedWriter(out);
	}
	
	public final static BufferedInputStream getBufferedInputStream(final String location) {
		return getBufferedInputStream(getInputStream(location));
	}
	
	public final static BufferedWriter getBufferedWriter(final String location) {
		return getBufferedWriter(getWriter(location));
	}
	
	public final static void copy(final InputStream in, final OutputStream out) throws IOException {
		final int size = 1024;
		final byte[] buf = new byte[size];
		while (true) {
			final int len = in.read(buf);
			if (len < 0) {
				break;
			}
			out.write(buf, 0, len);
		}
	}
	
	public final static void println(final Writer w) {
		try {
			w.write(BR);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public final static String readln() {
		try {
			return in.readLine();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
