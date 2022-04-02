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
package org.pandcorps.wad;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.pandcorps.core.*;

public class Wads {
    private static byte[] inputBuffer;
    private static int inputOffset;
    private final static StringBuilder stringBuilder = new StringBuilder(8);
    
    private final static void writeSignedShort(final OutputStream out, final short s) throws IOException {
        writeUnsignedShort(out, s);
    }
    
    private final static void writeUnsignedShort(final OutputStream out, final short s) throws IOException {
        out.write(s & 255);
        out.write((s >> 8) & 255);
    }
    
    private final static void writeInt(final OutputStream out, final int i) throws IOException {
        out.write(i & 255);
        out.write((i >> 8) & 255);
        out.write((i >> 16) & 255);
        out.write((i >> 24) & 255);
    }
    
    private final static void writeString(final OutputStream out, final String s) throws IOException {
        writeString(out, s, 8);
    }
    
    private final static void writeString(final OutputStream out, final String s, final int requiredSize) throws IOException {
        final int size = s.length();
        for (int i = 0; i < requiredSize; i++) {
            out.write((i < size) ? s.charAt(i) : 0);
        }
    }
    
    private final static int read() {
        return Byte.toUnsignedInt(inputBuffer[inputOffset++]);
    }
    
    private final static short readSignedShort() throws IOException {
        return readUnsignedShort();
    }
    
    private final static short readUnsignedShort() throws IOException {
        final int b1 = read();
        final int b2 = read();
        return (short) (b1 | (b2 << 8));
    }
    
    private final static int readInt() throws IOException {
        final int b1 = read();
        final int b2 = read();
        final int b3 = read();
        final int b4 = read();
        return b1 | (b2 << 8) | (b3 << 16) | (b4 << 24);
    }
    
    private final static String readString() throws IOException {
        return readString(8);
    }
    
    private final static String readString(final int requiredSize) throws IOException {
        Chartil.clear(stringBuilder);
        for (int i = 0; i < requiredSize; i++) {
            int c = read();
            if (c != 0) {
                stringBuilder.append((char) c);
            }
        }
        return stringBuilder.toString();
    }
    
    public abstract static class LumpType {
        protected abstract void save(final OutputStream out) throws IOException;
        
        protected abstract void load() throws IOException;
        
        protected abstract int getSize(); // Number of bytes for an element of this type
    }
    
    public final static short TYPE_PLAYER_START_1 = 1;
    
    public final static short SPAWN_FLAG_EASY = 0x0001;
    public final static short SPAWN_FLAG_MEDIUM = 0x0002;
    public final static short SPAWN_FLAG_HARD = 0x0004;
    public final static short SPAWN_FLAG_AMBUSH = 0x0008;
    public final static short SPAWN_FLAG_NOTSINGLE = 0x0010;
    public final static short SPAWN_FLAG_NOTDEATHMATCH = 0x0020;
    public final static short SPAWN_FLAG_NOTCOOPERATIVE = 0x0040;
    public final static short SPAWN_FLAG_BADEDITORCHECK = 0x0100;
    
    public final static class Thing extends LumpType {
        public short x;
        public short y;
        public short angle;
        public short type;
        public short spawnFlags;
        
        public Thing() {
        }
        
        @Override
        protected final void save(final OutputStream out) throws IOException {
            writeSignedShort(out, x);
            writeSignedShort(out, y);
            writeUnsignedShort(out, angle);
            writeUnsignedShort(out, type);
            writeUnsignedShort(out, spawnFlags);
        }
        
        @Override
        protected final void load() throws IOException {
            x = readSignedShort();
            y = readSignedShort();
            angle = readUnsignedShort();
            type = readUnsignedShort();
            spawnFlags = readUnsignedShort();
        }
        
        @Override
        protected final int getSize() {
            return 10;
        }
    }
    
    public final static short LINEDEF_FLAG_BLOCKING = 0x0001;
    public final static short LINEDEF_FLAG_BLOCKMONSTERS = 0x0002;
    public final static short LINEDEF_FLAG_TWOSIDED = 0x0004;
    public final static short LINEDEF_FLAG_DONTPEGTOP = 0x0008;
    public final static short LINEDEF_FLAG_DONTPEGBOTTOM = 0x0010;
    public final static short LINEDEF_FLAG_SECRET = 0x0020;
    public final static short LINEDEF_FLAG_SOUNDBLOCK = 0x0040;
    public final static short LINEDEF_FLAG_DONTDRAW = 0x0080;
    public final static short LINEDEF_FLAG_MAPPED = 0x0100;
    
    public final static short SIDEDEF_NONE = (short) 0xffff;
    
    public final static class Linedef extends LumpType {
        public short beginningVertex;
        public short endingVertex;
        public short flags;
        public short lineType;
        public short sectorTag;
        public short rightSidedef = SIDEDEF_NONE; // The front is always 90 degrees to the right / clockwise from the ray you would draw starting from the first point to the second point
        public short leftSidedef = SIDEDEF_NONE;
        
        public Linedef() {
        }
        
        public final boolean isFlag(final short flag) {
            return (flags & flag) != 0;
        }
        
        public final void setFlag(final short flag, final boolean value) {
            if (isFlag(flag) == value) {
                return;
            } else if (value) {
                flags = (short) (flags | flag);
            }
        }
        
        @Override
        protected final void save(final OutputStream out) throws IOException {
            writeUnsignedShort(out, beginningVertex);
            writeUnsignedShort(out, endingVertex);
            writeUnsignedShort(out, flags);
            writeUnsignedShort(out, lineType);
            writeUnsignedShort(out, sectorTag);
            writeUnsignedShort(out, rightSidedef);
            writeUnsignedShort(out, leftSidedef);
        }
        
        @Override
        protected final void load() throws IOException {
            beginningVertex = readUnsignedShort();
            endingVertex = readUnsignedShort();
            flags = readUnsignedShort();
            lineType = readUnsignedShort();
            sectorTag = readUnsignedShort();
            rightSidedef = readUnsignedShort();
            leftSidedef = readUnsignedShort();
        }
        
        @Override
        protected final int getSize() {
            return 14;
        }
    }
    
    public final static class Sidedef extends LumpType {
        public short xOffset;
        public short yOffset;
        public String upperTexture;
        public String lowerTexture;
        public String middleTexture;
        public short sectorReference;
        
        public Sidedef() {
        }
        
        @Override
        protected final void save(final OutputStream out) throws IOException {
            writeSignedShort(out, xOffset);
            writeSignedShort(out, yOffset);
            writeString(out, upperTexture);
            writeString(out, lowerTexture);
            writeString(out, middleTexture);
            writeUnsignedShort(out, sectorReference);
        }
        
        @Override
        protected final void load() throws IOException {
            xOffset = readSignedShort();
            yOffset = readSignedShort();
            upperTexture = readString();
            lowerTexture = readString();
            middleTexture = readString();
            sectorReference = readUnsignedShort();
        }
        
        @Override
        protected final int getSize() {
            return 30;
        }
    }
    
    public final static class Vertex extends LumpType implements Comparable<Vertex> {
        public short x;
        public short y;
        
        public Vertex() {
        }
        
        public Vertex(final short x, final short y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        protected final void save(final OutputStream out) throws IOException {
            writeSignedShort(out, x);
            writeSignedShort(out, y);
        }
        
        @Override
        protected final void load() throws IOException {
            x = readSignedShort();
            y = readSignedShort();
        }
        
        @Override
        protected final int getSize() {
            return 4;
        }
        
        @Override
        public final int hashCode() {
            final int x = this.x, y = this.y;
            return (x << 16) | y;
        }
        
        @Override
        public final boolean equals(final Object o) {
            final Vertex v = (Vertex) o;
            return (x == v.x) && (y == v.y);
        }

        @Override
        public final int compareTo(final Vertex v) {
            final int c = Short.compare(x, v.x);
            return (c == 0) ? Short.compare(y, v.y) : c;
        }
    }
    
    public final static class Sector extends LumpType {
        public short floorHeight;
        public short ceilingHeight;
        public String floorTexture;
        public String ceilingTexture;
        public short lightLevel;
        public short sectorSpecial;
        public short sectorTag;
        
        public Sector() {
        }
        
        @Override
        protected final void save(final OutputStream out) throws IOException {
            writeSignedShort(out, floorHeight);
            writeSignedShort(out, ceilingHeight);
            writeString(out, floorTexture);
            writeString(out, ceilingTexture);
            writeSignedShort(out, lightLevel);
            writeUnsignedShort(out, sectorSpecial);
            writeUnsignedShort(out, sectorTag);
        }
        
        @Override
        protected final void load() throws IOException {
            floorHeight = readSignedShort();
            ceilingHeight = readSignedShort();
            floorTexture = readString();
            ceilingTexture = readString();
            lightLevel = readSignedShort();
            sectorSpecial = readUnsignedShort();
            sectorTag = readUnsignedShort();
        }
        
        @Override
        protected final int getSize() {
            return 26;
        }
    }
    
    public final static class Lump<T extends LumpType> extends ArrayList<T> {
        private static final long serialVersionUID = 1L;
        
        public String name;
        private final Constructor<T> constructor;
        
        public Lump(final String name, final Class<T> elementClass) {
            this.name = name;
            constructor = Reftil.getConstructor(elementClass);
        }
        
        public final Lump<T> append(final T element) {
            add(element);
            return this;
        }
        
        private final void save(final OutputStream out) throws IOException {
            for (final T element : this) {
                element.save(out);
            }
        }
        
        private final void load(final int numBytes) throws IOException {
            int bytesRead = 0;
            while (bytesRead < numBytes) {
                final T element = Reftil.newInstance(constructor);
                element.load();
                add(element);
                bytesRead += element.getSize();
            }
            if (bytesRead != numBytes) {
                throw new IllegalStateException("Expected to read " + numBytes + " but read " + bytesRead + " for " + name);
            }
        }
        
        private final int getSizeBytes() {
            if (isEmpty()) {
                return 0;
            }
            return size() * get(0).getSize();
        }
    }
    
    public final static class Level {
        public final Lump<Vertex> name;
        public final Lump<Thing> things = new Lump<Thing>("THINGS", Thing.class);
        public final Lump<Linedef> linedefs = new Lump<Linedef>("LINEDEFS", Linedef.class);
        public final Lump<Sidedef> sidedefs = new Lump<Sidedef>("SIDEDEFS", Sidedef.class);
        public final Lump<Vertex> vertexes = new Lump<Vertex>("VERTEXES", Vertex.class);
        public final Lump<Sector> sectors = new Lump<Sector>("SECTORS", Sector.class);
        
        public Level(final String name) {
            this.name = new Lump<Vertex>(name, Vertex.class);
        }
        
        private final void save(final OutputStream out) throws IOException {
            things.save(out);
            linedefs.save(out);
            sidedefs.save(out);
            vertexes.save(out);
            sectors.save(out);
        }
        
        public final List<Lump<? extends LumpType>> getLumps() {
            return Arrays.asList(name, things, linedefs, sidedefs, vertexes, sectors);
        }
        
        public final Lump<? extends LumpType> getLump(final String name) {
            for (final Lump<? extends LumpType> lump : getLumps()) {
                if (name.equals(lump.name)) {
                    return lump;
                }
            }
            return null;
        }
        
        /*public final Level prune() {
            final Set<Vertex> usedVertexes = new HashSet<Vertex>();
            for (final Linedef linedef : linedefs) {
                usedVertexes.add(linedef.beginningVertex); // Indexes, not vertexes; also, removing an unused vertex means we need to update all follow vertex references in linedefs
            }
            return this;
        }*/
    }
    
    public final static class DirectoryEntry {
        public final int lumpPointer;
        public final int lumpSize;
        public final String lumpName;
        
        public DirectoryEntry(final int lumpPointer, final int lumpSize, final String lumpName) {
            this.lumpPointer = lumpPointer;
            this.lumpSize = lumpSize;
            this.lumpName = lumpName;
        }
    }
    
    public final static class Wad {
        public String wadType = "PWAD"; // Or IWAD
        public final List<Level> levels = new ArrayList<Level>();
        
        private final void save(final OutputStream out) throws IOException {
            // Header
            writeString(out, wadType, 4);
            final int directoryNumberOfEntries = levels.size() * levels.get(0).getLumps().size();
            writeInt(out, directoryNumberOfEntries);
            final int directoryPointer = 12;
            writeInt(out, directoryPointer);
            
            // Directory
            final int directorySizeBytes = 16 * directoryNumberOfEntries;
            int lumpPointer = directoryPointer + directorySizeBytes;
            for (final Level level : levels) {
                for (final Lump<? extends LumpType> lump : level.getLumps()) {
                    writeInt(out, lumpPointer);
                    final int lumpSize = lump.getSizeBytes();
                    writeInt(out, lumpSize);
                    lumpPointer += lumpSize;
                    writeString(out, lump.name);
                }
            }
            
            // Lumps
            for (final Level level : levels) {
                level.save(out);
            }
        }
        
        private final void load(final InputStream in) throws IOException {
            // Header
            inputBuffer = Iotil.readBytes(in);
            wadType = readString(4);
            final int directoryNumberOfEntries = readInt();
            final int directoryPointer = readInt();
            
            // Directory
            inputOffset = directoryPointer;
            final List<DirectoryEntry> directory = new ArrayList<DirectoryEntry>(directoryNumberOfEntries);
            for (int directoryIndex = 0; directoryIndex < directoryNumberOfEntries; directoryIndex++) {
                final int lumpPointer = readInt();
                final int lumpSize = readInt();
                final String lumpName = readString();
                directory.add(new DirectoryEntry(lumpPointer, lumpSize, lumpName));
            }
            
            // Lumps
            Level level = null;
            for (int directoryIndex = 0; directoryIndex < directoryNumberOfEntries; directoryIndex++) {
                final DirectoryEntry entry = directory.get(directoryIndex);
                if (directoryIndex < (directoryNumberOfEntries - 1)) {
                    final DirectoryEntry next = directory.get(directoryIndex + 1);
                    if ("THINGS".equals(next.lumpName)) {
                        level = new Level(entry.lumpName);
                        levels.add(level);
                        continue;
                    }
                }
                if (level == null) {
                    info("Skipping " + entry.lumpName);
                    continue;
                }
                final Lump<? extends LumpType> lump = level.getLump(entry.lumpName);
                if (lump == null) {
                    continue;
                }
                inputOffset = entry.lumpPointer;
                lump.load(entry.lumpSize);
            }
        }
    }
    
    public final static void main(final String[] args) throws Exception {
        final Wad wad = new Wad();
        /*
        final Level level = new Level("MAP01");
        //level.things;
        level.vertexes.add(new Vertex((short) 0, (short) 0)).add(new Vertex((short) 0, (short) 64)).add(new Vertex((short) 64, (short) 64)).add(new Vertex((short) 64, (short) 0));
        //level.linedefs;
        //level.sidedefs;
        //level.sectors;
        wad.levels.add(level);
        */
        final String loc = args[0];
        info("Loading " + loc);
        InputStream in = null;
        try {
            in = Iotil.getInputStream(loc);
            wad.load(in);
        } finally {
            Iotil.close(in);
        }
        info("Found " + wad.levels.size() + " levels");
        long minWidth = Long.MAX_VALUE, minHeight = minWidth, minArea = minWidth;
        long maxWidth = 0, maxHeight = 0, maxArea = 0;
        long totalWidth = 0, totalHeight = 0, totalArea = 0;
        for (final Level level : wad.levels) {
            info("Level name " + level.name.name);
            info("THINGS size " + level.things.size());
            int minV = Short.MAX_VALUE, maxV = Short.MIN_VALUE;
            for (final Linedef line : level.linedefs) {
                final short b = line.beginningVertex, e = line.endingVertex;
                if (b < minV) {
                    minV = b;
                } else if (b > maxV) {
                    maxV = b;
                }
                if (e < minV) {
                    minV = e;
                } else if (e > maxV) {
                    maxV = e;
                }
            }
            info("LINEDEFS size " + level.linedefs.size() + ", vertices " + minV + " - " + maxV);
            info("SIDEDEFS size " + level.sidedefs.size());
            short minX = Short.MAX_VALUE, minY = minX, maxX = Short.MIN_VALUE, maxY = maxX;
            for (final Vertex v : level.vertexes) {
                final short x = v.x, y = v.y;
                if (x < minX) {
                    minX = x;
                }
                if (x > maxX) {
                    maxX = x;
                }
                if (y < minY) {
                    minY = y;
                }
                if (y > maxY) {
                    maxY = y;
                }
            }
            info("VERTEXES size " + level.vertexes.size() + " (" + minX + ", " + minY + ") - (" + maxX + ", " + maxY + ")");
            final int width = maxX - minX, height = maxY - minY, area = width * height;
            if (width < minWidth) {
                minWidth = width;
            }
            if (width > maxWidth) {
                maxWidth = width;
            }
            if (height < minHeight) {
                minHeight = height;
            }
            if (height > maxHeight) {
                maxHeight = height;
            }
            if (area < minArea) {
                minArea = area;
            }
            if (area > maxArea) {
                maxArea = area;
            }
            totalWidth += width;
            totalHeight += height;
            totalArea += area;
            info("SECTORS size " + level.sectors.size());
            //TODO Validate references
        }
        final int numLevels = wad.levels.size();
        long meanWidth = totalWidth / numLevels;
        long meanHeight = totalHeight / numLevels;
        long meanArea = totalArea / numLevels;
        info("Min width/height/area " + minWidth + "/" + minHeight + "/" + minArea);
        info("Mean width/height/area " + meanWidth + "/" + meanHeight + "/" + meanArea);
        info("Max width/height/area " + maxWidth + "/" + maxHeight + "/" + maxArea);
    }
    
    private final static void info(final Object s) {
        System.out.println(s);
    }
}
