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
package org.pandcorps.core.aud;

import java.io.*;
import java.util.*;

import org.pandcorps.core.*;

public class Mid2Jet {
    private final static int MSG_OFF_0 = 0x80;
    private final static int MSG_OFF_15 = 0x8F;
    private final static int MSG_ON_0 = 0x90;
    private final static int MSG_ON_15 = 0x9F;
    private final static int MSG_PROGRAM_CHANGE_0 = 0xC0;
    private final static int MSG_PROGRAM_CHANGE_15 = 0xCF;
    private final static int MSG_META = 0xFF;
    
    private static Set<String> exclusionSet = null;
    private static byte[] jetHeader = null;
    
    public final static void main(final String[] args) throws Exception {
        final String exclusions = Pantil.getProperty("org.pandcorps.core.Mid2Jet.exclusions");
        if (exclusions != null) {
            exclusionSet = new HashSet<String>(Arrays.asList(exclusions.split(";")));
        }
        jetHeader = Iotil.readBytes("org/pandcorps/core/JetHeader.bin");
        for (final String midLoc : args) {
            convert(midLoc);
        }
    }
    
    private final static void convert(final String midLoc) throws Exception {
        convert(new File(midLoc));
    }
    
    private final static void convert(final File file) throws Exception {
        if (exclusionSet.contains(file.getName())) {
            System.out.println("Skipping " + file.getAbsolutePath());
            return;
        } else if (file.isDirectory()) {
            convertDirectory(file);
        } else {
            convertFile(file.getAbsolutePath());
        }
    }
    
    private final static void convertDirectory(final File dir) throws Exception {
        for (final File file : dir.listFiles()) {
            convert(file);
        }
    }
    
    private final static void convertFile(final String midLoc) throws Exception {
        if (!midLoc.endsWith(".mid")) {
            return;
        }
        System.out.println("Converting " + midLoc);
        final String jetLoc = midLoc.substring(0, midLoc.length() - 3) + "jet";
        OutputStream out = null;
        try {
            debug("Converting");
            
            final MidFile mid = new MidFile(new BufferedInputStream(Iotil.getInputStream(midLoc)));
            final ByteArrayOutputStream bout = new ByteArrayOutputStream();
            convertTrack(mid, bout);
            final byte[] a = bout.toByteArray();
            final int midLength = a.length;
            final int jetContentLength = midLength + 22;
            final int jetLength = jetContentLength + 4 + jetHeader.length;
            
            out = new BufferedOutputStream(new FileOutputStream(jetLoc));
            out.write("JET ".getBytes());
            writeInt(out, jetLength, 4);
            
            out.write(jetHeader);
            writeInt(out, jetContentLength, 4);
            
            out.write("MThd".getBytes());
            writeIntBigFirst(out, mid.header.size, 4);
            writeIntBigFirst(out, mid.header.type, 2);
            writeIntBigFirst(out, mid.header.numTracks, 2);
            writeIntBigFirst(out, mid.header.division, 2);
            out.write("MTrk".getBytes());
            writeIntBigFirst(out, midLength, 4);
            out.write(a);
            out.flush();
        } finally {
            Iotil.close(out);
        }
    }
    
    private final static void convertTrack(final MidFile mid, final OutputStream out) throws Exception {
        int prevMessageType = -1;
        for (final MidEvent event : mid.track.events) {
            int messageType = event.messageType;
            if ((messageType >= MSG_OFF_0) && (messageType <= MSG_OFF_15)) {
                messageType += (MSG_ON_0 - MSG_OFF_0);
            }
            final MidMessage message = event.message;
            if (message instanceof MetaMessage) {
                final MetaMessage meta = (MetaMessage) message;
                if (meta.metaType == 3) {
                    continue;
                }
                writeInt(out, event.deltaTime, 1);
                writeInt(out, messageType, 1);
                writeInt(out, meta.metaType, 1);
                writeInt(out, meta.size, 1);
                out.write(meta.data);
            } else if (message instanceof ShortMessage) {
                writeInt(out, event.deltaTime, 1);
                if (messageType != prevMessageType) {
                    writeInt(out, messageType, 1);
                }
                out.write(((ShortMessage) message).data);
            }
            prevMessageType = messageType;
        }
        out.flush();
    }
    
    private final static void writeInt(final OutputStream out, int value, final int size) throws Exception {
        for (int i = 0; i < size; i++) {
            final int b = value % 256;
            out.write(b);
            value /= 256;
        }
    }
    
    private final static void writeIntBigFirst(final OutputStream out, int value, final int size) throws Exception {
        for (int i = size - 1; i >= 0; i--) {
            final int b = (value >>> (i * 8)) % 256;
            out.write(b);
        }
    }
    
    private final static void debug(final String s) {
        System.out.println(s);
    }
    
    private final static void assertBytes(final String ex, final InputStream in) throws Exception {
        final int size = ex.length();
        for (int i = 0; i < size; i++) {
            final char exChar = ex.charAt(i);
            final int acByte = nextByte(in);
            if (acByte != exChar) {
                throw new IllegalStateException("Expected " + exChar + " but found " + acByte);
            }
        }
    }
    
    private final static void assertInt(final int ex, final int ac) {
        if (ac != ex) {
            throw new IllegalStateException("Expectd " + ex + " but found " + ac);
        }
    }
    
    private static int peaked = -1;
    
    private static int nextByte(final InputStream in) throws Exception {
        if (peaked == -1) {
            return in.read();
        }
        try {
            return peaked;
        } finally{
            peaked = -1;
        }
    }
    
    private final static int getInt(final InputStream in, final int size) throws Exception {
        int total = 0;
        for (int index = 0; index < size; index++) {
            int b = nextByte(in);
            if (b < 0) {
                b += 256;
            }
            total *= 256;
            total += b;
        }
        return total;
    }
    
    private final static byte[] getBytes(final InputStream in, final int size) throws Exception {
        final byte[] sub = new byte[size];
        if (peaked == -1) {
            in.read(sub);
        } else {
            sub[0] = (byte) peaked;
            in.read(sub, 1, size - 1);
            peaked = -1;
        }
        return sub;
    }
    
    public final static class MidFile {
        private final MidHeader header;
        private final MidTrack track;
        
        private MidFile(final InputStream in) throws Exception {
            header = new MidHeader(in);
            track = new MidTrack(in); // 4 for MThd, 4 for size, content
        }
    }
    
    public static class MidChunk {
    }
    
    public final static class MidHeader extends MidChunk {
        private final int size;
        private final int type;
        private final int numTracks;
        private final int division;
        
        private MidHeader(final InputStream in) throws Exception {
            assertBytes("MThd", in);
            size = getInt(in, 4);
            assertInt(6, size);
            type = getInt(in, 2);
            assertInt(0, type); // Type 0 is a single track file
            numTracks = getInt(in, 2);
            assertInt(1, numTracks); // If type is 0, then the number of tracks must be 1
            division = getInt(in, 2);
        }
    }
    
    public final static class MidTrack extends MidChunk {
        private final int size;
        private final List<MidEvent> events;
        
        private MidTrack(final InputStream in) throws Exception {
            assertBytes("MTrk", in);
            size = getInt(in, 4);
            MidMessage.next = 0;
            events = new ArrayList<MidEvent>();
            while (MidMessage.next < size) {
                events.add(new MidEvent(this, in));
            }
        }
    }
    
    public final static class MidEvent {
        private final int deltaTime;
        private final int messageType;
        private final MidMessage message;
        
        private MidEvent(final MidTrack track, final InputStream in) throws Exception {
            deltaTime = getInt(in, 1);
            int mt = getInt(in, 1);
            if (mt < MSG_OFF_0) {
                peaked = mt;
                mt = track.events.get(track.events.size() - 1).messageType;
                MidMessage.next++;
            } else {
                MidMessage.next += 2;
            }
            messageType = mt;
            if (messageType == MSG_META) {
                message = new MetaMessage(in);
            } else if ((messageType >= MSG_PROGRAM_CHANGE_0) && (messageType <= MSG_PROGRAM_CHANGE_15)) {
                message = new ShortMessage(in, 1);
            } else if ((messageType >= MSG_ON_0) && (messageType <= MSG_ON_15)) {
                message = new ShortMessage(in, 2);
            } else if ((messageType >= MSG_OFF_0) && (messageType <= MSG_OFF_15)) {
                message = new ShortMessage(in, 2);
            } else {
                throw new IllegalArgumentException("Unexpected message type " + messageType);
            }
        }
    }
    
    public static class MidMessage {
        protected static int next = 0;
    }
    
    public static class MetaMessage extends MidMessage {
        private final int metaType;
        private final int size;
        private final byte[] data;
        
        private MetaMessage(final InputStream in) throws Exception {
            metaType = getInt(in, 1);
            size = getInt(in, 1);
            data = getBytes(in, size);
            next += (2 + size);
        }
    }
    
    public static class ShortMessage extends MidMessage {
        private final byte data[];
        
        private ShortMessage(final InputStream in, final int size) throws Exception {
            data = getBytes(in, size);
            next += size;
        }
    }
}
