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

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.zip.*;

import org.pandcorps.pandam.*;

// Input/Output Utility
public final class Iotil {
	public final static String BR = System.getProperty("line.separator");
	
	private final static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	private final static int BUFFER_SIZE = 1024;
	private final static String CURRENT_DIR = ".";
	
	private static OutputStreamFactory outputStreamFactory = new FileOutputStreamFactory();
	
	private static WriterFactory writerFactory = new FileWriterFactory();
	
	private static InputStreamFactory inputStreamFactory = new FileInputStreamFactory();
	
	private static ResourceChecker resourceChecker = new FileResourceChecker();
	
	private static ResourceDeleter resourceDeleter = new FileResourceDeleter();
	
	private static ResourceLister resourceLister = new FileResourceLister();
	
	private static FileGetter fileGetter = new FileFileGetter();
	
	private static DirectoryGetter directoryGetter = new FileDirectoryGetter();
	
	private static DirectoryMaker directoryMaker = new FileDirectoryMaker();
	
	private Iotil() {
		throw new Error();
	}
	
	private final static class RobustFile {
	    private RobustFileVersion readVersion;
	    private RobustFileVersion writeVersion;
	    
	    private RobustFile(final String location) {
            final RobustFileVersion v1 = new RobustFileVersion(location, 1);
            final RobustFileVersion v2 = new RobustFileVersion(location, 2);
            if (!(v1.valid || v2.valid)) {
                readVersion = new RobustFileVersion(location, -1);
                writeVersion = v1;
                writeVersion.nextVersionNumber = System.currentTimeMillis();
            } else if (v1.isMoreImportantThan(v2)) {
                readVersion = v1;
                writeVersion = v2;
                writeVersion.nextVersionNumber = readVersion.nextVersionNumber + 1;
            } else {
                readVersion = v2;
                writeVersion = v1;
                writeVersion.nextVersionNumber = readVersion.nextVersionNumber + 1;
            }
        }
	}
	
	private final static class RobustFileVersion {
	    private final String location;
	    private final long versionNumber;
	    private final boolean valid;
	    private long nextVersionNumber = -1;
	    
	    private RobustFileVersion(final String location, final int label) {
	        if (label == -1) {
	            this.location = location;
	            versionNumber = -1;
	            valid = false;
	            return;
	        }
	        this.location = location + "." + label + ".panver";
	        long versionNumber = -1;
	        boolean valid = false;
	        try {
	            final String commit = read(getCommitLocation());
	            if (commit.endsWith(".c")) {
	                versionNumber = Long.parseLong(commit.substring(0, commit.length() - 2));
	                valid = true;
	            }
	        } catch (final Exception e) {
	            // Just keep valid as false
	        }
	        this.versionNumber = versionNumber;
	        this.valid = valid;
	    }
	    
	    private boolean isMoreImportantThan(final RobustFileVersion v) {
	        if (valid == v.valid) {
	            return versionNumber > v.versionNumber;
	        }
	        return valid;
	    }
	    
	    private String getCommitLocation() {
	        return location + ".commit";
	    }
	    
	    private void commit() {
	        writeFile(getCommitLocation(), nextVersionNumber + ".c");
	    }
	    
	    private void prepareWrite() {
	        delete(getCommitLocation());
	    }
    }
	
	private final static String getNewestValidInputLocation(final String location) {
	    // Check both versions; if nothing found, try unversioned raw name
	    return location;
	}
	
	private final static String getLeastImportantOutputLocation(final String location) {
	    // If two valid files are found, return the older one; if newer file is invalid, return it
        return location;
    }
	
	private final static boolean isLocationValid(final String location) {
	    return true;
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
			throw new RuntimeException("Could not open " + location, e);
		}
	}
	
	public final static InputStream getResourceInputStream(final String location) {
	    return Iotil.class.getClassLoader().getResourceAsStream(location);
	}
	
	public final static int size(final InputStream in) {
	    final byte[] buf = new byte[BUFFER_SIZE];
	    int totalSize = 0, currentSize;
	    try {
    	    while ((currentSize = in.read(buf)) >= 0) {
    	        totalSize += currentSize;
    	    }
	    } catch (final IOException e) {
	        throw new RuntimeException(e);
	    }
	    return totalSize;
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
	
	public final static String listTree() {
	    return listTree(CURRENT_DIR);
	}
	
	public final static String listTree(final String loc) {
	    return listTree(loc, false);
	}
	
	public final static String listTree(final String loc, final boolean indent) {
	    final File dir = getDir(loc);
	    final StringBuilder b = new StringBuilder();
	    listTree(dir, b, indent ? 0 : -100000);
	    return b.toString();
	}
	
	private final static void listTree(final File dir, final StringBuilder b, final int indent) {
	    for (final File child : dir.listFiles()) {
	        if (indent < 0) {
	            b.append(child.getAbsolutePath());
	        } else {
	            for (int i = 0; i < indent; i++) {
                    b.append(' ');
                }
                b.append(child.getName());
	        }
	        b.append('\n');
	        if (child.isDirectory()) {
	            listTree(child, b, indent + 1);
	        }
	    }
	}
	
	public final static File getFile(final String location) {
        return fileGetter.getFile(location);
    }
	
	public final static File getDir() {
	    return getDir(CURRENT_DIR);
	}
	
	public final static File getDir(final String location) {
        return directoryGetter.getDir(location);
    }
	
	public final static boolean mkdirs(final String location) {
	    if (CURRENT_DIR.equals(location)) {
	        return false;
	    }
        return directoryMaker.mkdirs(location);
    }
	
	private final static void mkdirsForFile(final String location) {
	    final int index = location.lastIndexOf(File.separatorChar);
	    if (index > 0) {
	        mkdirs(location.substring(0, index));
	    }
	}
	
	public final static boolean isFileContentIdentical(final String location1, final String location2) {
	    InputStream in1 = null, in2 = null;
	    try {
	        in1 = getInputStream(location1);
	        in2 = getInputStream(location2);
	        final byte[] buf1 = new byte[BUFFER_SIZE], buf2 = new byte[BUFFER_SIZE];
	        while (true) {
	            final int size1 = in1.read(buf1), size2 = in2.read(buf2);
	            if (size1 != size2) {
	                return false;
	            } else if (size1 == -1) {
	                return true;
	            }
	            for (int i = 0; i < size1; i++) {
	                if (buf1[i] != buf2[i]) {
	                    return false;
	                }
	            }
	        }
	    } catch (final IOException e) {
	        throw new RuntimeException(e);
	    } finally {
	        close(in1);
	        close(in2);
	    }
	}
	
	public final static Reader getReader(final String location) {
		return new InputStreamReader(getInputStream(location), Charset.forName("ISO-8859-1"));
	}
	
	public final static OutputStream getOutputStream(final String location) {
	    mkdirsForFile(location);
        try {
            return outputStreamFactory.newOutputStream(location);
        } catch (final Exception e) {
            throw Pantil.toRuntimeException(e);
        }
    }
	
	public final static Writer getWriter(final String location) {
	    mkdirsForFile(location);
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
	    return readByteArrayOutputStream(location).toString();
	}
	
	public final static byte[] readBytes(final String location) {
        return readByteArrayOutputStream(location).toByteArray();
    }
	
	public final static byte[] readBytes(final InputStream in) {
        return readByteArrayOutputStream(in).toByteArray();
    }
	
	private final static ByteArrayOutputStream readByteArrayOutputStream(final String location) {
	    InputStream in = null;
        try {
            in = getInputStream(location);
            return readByteArrayOutputStream(in);
        } finally {
            close(in);
        }
	}
	
	private final static ByteArrayOutputStream readByteArrayOutputStream(final InputStream in) {
	    final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            copy(in, out);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return out;
	}
	
	public final static long copy(final InputStream in, final OutputStream out) throws IOException {
		return copy(in, out, new byte[BUFFER_SIZE]);
	}
	
	public final static long copy(final InputStream in, final OutputStream out, final byte[] buf) throws IOException {
	    long size = 0;
		while (true) {
			final int len = in.read(buf);
			if (len < 0) {
				break;
			}
			size += len;
			out.write(buf, 0, len);
		}
		return size;
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
	
	public final static String formatDirectory(final String loc) {
	    if (loc.charAt(loc.length() - 1) == File.separatorChar) {
	        return loc;
	    }
	    return loc + File.separatorChar;
	}
	
	public final static void unzip(final InputStream in, final String loc, final StringBuilder log) {
	    final byte[] buf = new byte[BUFFER_SIZE];
	    final ZipInputStream zip = new ZipInputStream(in);
	    final String dir = formatDirectory(loc);
        try {
            ZipEntry entry = null;
            while ((entry = zip.getNextEntry()) != null) {
                unzipEntry(zip, dir + entry.getName(), buf, log);
                zip.closeEntry();
            }
        } catch (final IOException e) {
            throw new Panception(e);
        }
	}
	
	private final static void unzipEntry(final ZipInputStream zip, String loc, final byte[] buf, final StringBuilder log) throws IOException {
	    OutputStream out = null;
	    try {
	        if (File.separatorChar != '\\') { // Zip can entries use \ as separator (maybe not standard, but seems to be possible)
	            loc = loc.replace('\\', File.separatorChar);
	        }
	        //TODO If unzipping a standard zip file with / onto Windows, should prob change / to \
	        final File file = getFile(loc);
	        final boolean existed = file.exists();
	        out = new FileOutputStream(file);
	        final long size = copy(zip, out, buf);
	        if (log != null) {
	            if (log.length() > 0) {
	                log.append('\n');
	            }
	            log.append(file.getAbsolutePath()).append(' ').append(size).append(existed ? " updated" : " created");
	        }
	    } finally {
	        close(out);
	    }
	}
	
	public final static void zipDir(final String loc, final OutputStream out, final String zipEntryBase) {
	    try {
    	    final File dir = getDir(loc);
    	    final ZipOutputStream zip = new ZipOutputStream(out);
    	    final byte[] buf = new byte[BUFFER_SIZE];
    	    //TODO Replace \ in zipEntryBase with / ?
    	    zipDir(dir, zip, zipEntryBase, buf);
    	    zip.close();
	    } catch (final IOException e) {
            throw new Panception(e);
        }
	}
	
	private final static void zipDir(final File dir, final ZipOutputStream zip, final String zipEntryBase, final byte[] buf) throws IOException {
	    for (final File child : dir.listFiles()) {
	        final String childName = zipEntryBase + '/' + child.getName(); // '/' is the zip separator; don't use the system's separator
	        if (child.isDirectory()) {
	            zipDir(child, zip, childName, buf);
	        } else {
	            zipFile(child, zip, childName, buf);
	        }
	    }
	}
	
	private final static void zipFile(final File file, final ZipOutputStream zip, final String zipEntryName, final byte[] buf) throws IOException {
	    final ZipEntry entry = new ZipEntry(zipEntryName);
	    zip.putNextEntry(entry);
	    InputStream in = null;
	    try {
	        in = new FileInputStream(file);
	        copy(in, zip, buf);
	    } finally {
	        close(in);
	    }
	    zip.closeEntry();
	}
	
	public final static void setOutputStreamFactory(final OutputStreamFactory outputStreamFactory) {
        Iotil.outputStreamFactory = outputStreamFactory;
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
	
	public final static void setFileGetter(final FileGetter fileGetter) {
        Iotil.fileGetter = fileGetter;
    }
	
	public final static void setDirectoryGetter(final DirectoryGetter directoryGetter) {
        Iotil.directoryGetter = directoryGetter;
    }
	
	public final static void setDirectoryMaker(final DirectoryMaker directoryMaker) {
        Iotil.directoryMaker = directoryMaker;
    }
	
	public static interface OutputStreamFactory {
        public OutputStream newOutputStream(final String location) throws Exception;
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
	
	public static interface FileGetter {
        public File getFile(final String location);
    }
	
	public static interface DirectoryGetter {
        public File getDir(final String location);
    }
	
	public static interface DirectoryMaker {
        public boolean mkdirs(final String location);
    }
	
	private final static class FileOutputStreamFactory implements OutputStreamFactory {
        @Override
        public final OutputStream newOutputStream(final String location) throws Exception {
            return new FileOutputStream(FileWriterFactory.getFileForWrite(location));
        }
    }
	
	private final static class FileWriterFactory implements WriterFactory {
	    private final static File getFileForWrite(final String location) throws Exception {
			final File f = new File(location);
			if (!f.exists()) {
				f.createNewFile();
			}
			if (!f.canWrite()) {
				f.setWritable(true);
			}
			return f;
	    }
	    
	    @Override
        public final Writer newWriter(final String location) throws Exception {
			return new FileWriter(getFileForWrite(location));
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
			return new File(CURRENT_DIR).list();
		}
	}
	
	private final static class FileFileGetter implements FileGetter {
        @Override
        public final File getFile(final String location) {
            return new File(location);
        }
    }
	
	private final static class FileDirectoryGetter implements DirectoryGetter {
        @Override
        public final File getDir(final String location) {
            mkdirs(location);
            return new File(location);
        }
    }
	
	private final static class FileDirectoryMaker implements DirectoryMaker {
        @Override
        public final boolean mkdirs(final String location) {
            return new File(location).mkdirs();
        }
    }
	
	/*public final static void main(final String[] args) {
	    try {
	        final long start = System.currentTimeMillis();
	        for (int i = 0; i < 1000; i++) {
	            //getInputStream(Pantil.RES + "img/FontSimple8.png").close(); // 251, 231, 231
	            getResourceInputStream(Pantil.RES + "img/FontSimple8.png").close(); // 200, 203, 208
	        }
	        System.out.println("Time: " + (System.currentTimeMillis() - start));
	    } catch (final Throwable e) {
	        e.printStackTrace();
	    }
	}*/
}
