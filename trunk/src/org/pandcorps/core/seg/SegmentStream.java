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
package org.pandcorps.core.seg;

import java.io.*;
import java.util.*;

import org.pandcorps.core.Iotil;

public class SegmentStream implements Closeable {
    
    private final BufferedReader in;
    private final Stack<Segment> stack = new Stack<Segment>();
    private final HashSet<String> skip = new HashSet<String>();
    
    public SegmentStream(final Reader in) {
        this.in = Iotil.getBufferedReader(in);
    }
    
    public SegmentStream(final InputStream in) {
        this(new InputStreamReader(in));
    }
    
    public final static SegmentStream openLocation(final String location) {
        return new SegmentStream(Iotil.getInputStream(location));
    }
    
    public final static List<Segment> readLocation(final String location) {
    	final SegmentStream in = openLocation(location);
    	try {
    		final ArrayList<Segment> list = new ArrayList<Segment>();
    		Segment seg = null;
    		while ((seg = in.read()) != null) {
    			list.add(seg);
    		}
    		return list;
    	} catch (final IOException e) {
    		throw new RuntimeException(e);
    	} finally {
    		in.close();
    	}
    }
    
    public final static List<Segment> readLocation(final String location, final String defaultContent) {
    	try {
    		return readLocation(location);
    	} catch (final RuntimeException e) {
    		if (e.getCause() instanceof FileNotFoundException) {
    			Iotil.writeFile(location, defaultContent);
    		} else {
    			throw e;
    		}
    	}
    	return readLocation(location);
    }
    
    public void skip(final String name) {
        skip.add(name);
    }
    
    private final boolean isSkipped(final Segment seg) {
        return seg == null ? true : skip.contains(seg.getName());
    }
    
    public void push(final Segment seg) {
        stack.push(seg);
    }
    
    public Segment read() throws IOException {
        while (stack.size() > 0) {
            // Seems reasonable to handle skips in push, but a skip might be added between push and read
            final Segment seg = stack.pop();
            if (!isSkipped(seg)) {
                return seg;
            }
        }
        String line;
        while ((line = in.readLine()) != null) {
        	if (line.length() == 0 || line.charAt(0) == '#') {
        		continue;
        	}
            final Segment seg = Segment.parse(line);
            if (!isSkipped(seg)) {
                return seg;
            }
        }
        return null;
    }
    
    public Segment readIf(final String... names) throws IOException {
        // Designed to be used in a loop; readWhile might be a good name; still only reads one Segment; readIf seems better
        return readConditional(true, names);
    }
    
    public Segment readUnless(final String... names) throws IOException {
        // readUntil might be good; see readIf
        return readConditional(false, names);
    }
    
    private final Segment readConditional(final boolean cond, final String... names) throws IOException {
        final Segment seg = read();
        if (seg == null) {
            return null;
        }
        final String ac = seg.getName();
        for (final String name : names) {
            /*
            For readIf, return the seg if true for any name.
            For readUnless, return the seg if false for all names.
            */
            if (ac.equals(name)) {
                return eval(cond, seg);
            }
        }
        return eval(!cond, seg);
    }
    
    private final Segment eval(final boolean cond, final Segment seg) {
        if (cond) {
            return seg;
        }
        push(seg);
        return null;
    }
    
    public Segment readRequire(final String name) throws IOException {
    	final Segment seg = read();
    	if (seg == null) {
    		throw new NullPointerException("Required segment " + name + " was missing");
    	}
    	final String ac = seg.getName();
    	if (!ac.equals(name)) {
    		throw new IllegalArgumentException("Required segment " + name + " but found " + ac);
    	}
    	return seg;
    }
    
    @Override
    public void close() /*throws IOException*/ {
        try {
            in.close();
        } catch (final IOException e) {
            //TODO should all methods do this instead of declaring IOException; should we use a different class; status like PrintStream?
            throw new RuntimeException(e);
        }
    }
}
