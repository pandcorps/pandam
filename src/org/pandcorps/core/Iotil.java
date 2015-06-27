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
package org.pandcorps.core;

import java.io.*;
import java.net.*;

// Input/Output Utility
public final class Iotil {
	public final static String BR = System.getProperty("line.separator");
	
	private final static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	private static WriterFactory writerFactory = new FileWriterFactory();
	
	private static InputStreamFactory inputStreamFactory = new FileInputStreamFactory();
	
	private static ResourceChecker resourceChecker = new FileResourceChecker();
	
	private static ResourceDeleter resourceDeleter = new FileResourceDeleter();
	
	private static ResourceLister resourceLister = new FileResourceLister();
	
	private Iotil() {
		throw new Error();
	}

	public final static InputStream getInputStream(final String location) {
		try {
			final InputStream fin = inputStreamFactory.newInputStream(location);
			if (fin != null) {
				return fin;
			}
		} catch (final Exception e) {
			// Just try URL
		}

		//return Iotil.class.getResourceAsStream(location);
		URL url = Iotil.class.getClassLoader().getResource(location);
		try {
			if (url == null) {
				url = new URL(location);
			}
			return url.openStream();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public final static InputStream getResourceInputStream(final String location) {
	    return Iotil.class.getClassLoader().getResourceAsStream(location);
	}
	
	public final static boolean exists(final String location) {
		return resourceChecker.exists(location) || Iotil.class.getClassLoader().getResource(location) != null;
	}
	
	public final static boolean delete(final String location) {
	    return resourceDeleter.delete(location);
	}
	
	public final static String[] list() {
		return resourceLister.list();
	}
	
	public final static Reader getReader(final String location) {
		return new InputStreamReader(getInputStream(location));
	}
	
	public final static Writer getWriter(final String location) {
		try {
			return writerFactory.newWriter(location);
		} catch (final Exception e) {
			throw Pantil.toRuntimeException(e);
		}
	}
	
	public final static void writeFile(final String location, final String content) {
		final Writer out = getWriter(location);
		try {
			out.write(content);
			out.flush();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		} finally {
			close(out);
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
	
	public final static BufferedReader getBufferedReader(final String location) {
		return getBufferedReader(getReader(location));
	}
	
	public final static BufferedWriter getBufferedWriter(final String location) {
		return getBufferedWriter(getWriter(location));
	}
	
	public final static PrintStream getPrintStream(final OutputStream out) {
        return out instanceof PrintStream ? (PrintStream) out : new PrintStream(out);
    }
	
	public final static PrintWriter getPrintWriter(final Writer out) {
        return out instanceof PrintWriter ? (PrintWriter) out : new PrintWriter(out);
    }
	
	public final static PrintWriter getPrintWriter(final String location) {
        return getPrintWriter(getWriter(location));
    }
	
	public final static String read(final String location) {
	    final ByteArrayOutputStream out = new ByteArrayOutputStream();
	    InputStream in = null;
	    try {
	        in = getInputStream(location);
	        try {
	            copy(in, out);
	        } catch (final IOException e) {
	            throw new RuntimeException(e);
	        }
	        return out.toString();
	    } finally {
	        close(in);
	    }
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
	
	public final static void setWriterFactory(final WriterFactory writerFactory) {
		Iotil.writerFactory = writerFactory;
	}
	
	public final static void setInputStreamFactory(final InputStreamFactory inputStreamFactory) {
		Iotil.inputStreamFactory = inputStreamFactory;
	}
	
	public final static void setResourceChecker(final ResourceChecker resourceChecker) {
		Iotil.resourceChecker = resourceChecker;
	}
	
	public final static void setResourceDeleter(final ResourceDeleter resourceDeleter) {
		Iotil.resourceDeleter = resourceDeleter;
	}
	
	public final static void setResourceLister(final ResourceLister resourceLister) {
		Iotil.resourceLister = resourceLister;
	}
	
	public static interface WriterFactory {
		public Writer newWriter(final String location) throws Exception;
	}
	
	public static interface InputStreamFactory {
		public InputStream newInputStream(final String location) throws Exception;
	}
	
	public static interface ResourceChecker {
		public boolean exists(final String location);
	}
	
	public static interface ResourceDeleter {
		public boolean delete(final String location);
	}
	
	public static interface ResourceLister {
		public String[] list();
	}
	
	private final static class FileWriterFactory implements WriterFactory {
		@Override
		public final Writer newWriter(final String location) throws Exception {
			final File f = new File(location);
			if (!f.exists()) {
				f.createNewFile();
			}
			if (!f.canWrite()) {
				f.setWritable(true);
			}
			return new FileWriter(f);
		}
	}
	
	private final static class FileInputStreamFactory implements InputStreamFactory {
		@Override
		public final InputStream newInputStream(final String location) throws Exception {
			final File f = new File(location);
			if (f.exists()) {
				try {
					return new FileInputStream(f);
				} catch (final FileNotFoundException e) {
					throw new Error(e);
				}
			}
			return null;
		}
	}
	
	private final static class FileResourceChecker implements ResourceChecker {
		@Override
		public final boolean exists(final String location) {
			return new File(location).exists();
		}
	}
	
	private final static class FileResourceDeleter implements ResourceDeleter {
		@Override
		public final boolean delete(final String location) {
			return new File(location).delete();
		}
	}
	
	private final static class FileResourceLister implements ResourceLister {
		@Override
		public final String[] list() {
			return new File(".").list();
		}
	}
	
	/*public final static void main(final String[] args) {
	    try {
	        final long start = System.currentTimeMillis();
	        for (int i = 0; i < 1000; i++) {
	            //getInputStream("org/pandcorps/res/img/FontSimple8.png").close(); // 251, 231, 231
	            getResourceInputStream("org/pandcorps/res/img/FontSimple8.png").close(); // 200, 203, 208
	        }
	        System.out.println("Time: " + (System.currentTimeMillis() - start));
	    } catch (final Throwable e) {
	        e.printStackTrace();
	    }
	}*/
}
