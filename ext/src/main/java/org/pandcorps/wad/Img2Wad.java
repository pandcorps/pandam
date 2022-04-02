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
import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.col.*;
import org.pandcorps.core.img.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.wad.Wads.*;

public class Img2Wad {
    private final static ImgFactory cm = ImgFactory.getFactory();
    
    private static LevelBuilder lb = null;
    private static CellBuilder[][] grid = null;
    private static short cw;
    private static short ch;
    private final static Map<Vertex, Vertex> vertexes = new HashMap<Vertex, Vertex>();
    //private final static HashMultimap<Vertex, Line> lines = new HashMultimap<Vertex, Line>();
    private final static HashMultimap<Line, Line> edges = new HashMultimap<Line, Line>();
    private final static Map<Side, Side> sides = new HashMap<Side, Side>();
    
    private final static <T> T putIfAbsent(final Map<T, T> map, final T key) {
        T value = map.get(key);
        if (value == null) {
            value = key;
            map.put(key, value);
        }
        return value;
    }
    
    private final static Vertex getVertex(final int x, final int y) {
        return putIfAbsent(vertexes, new Vertex((short) x, (short) y));
    }
    
    /*private final static List<Line> getLines(final Vertex v1, final Vertex v2) {
        final ArrayList<Line> candidates = lines.get(v1);
        for (final Line candidate : candidates) {
            if (v2.equals(candidate.beginningVertex) || v2.equals(candidate.endingVertex)) {
                return new ArrayList<Line>(Collections.singletonList(candidate));
            }
        }
    }*/
    
    private final static Edge getEdge(final Vertex v1, final Vertex v2) {
        return getEdge(v1, v2, 1);
    }
    
    private final static Edge getEdge(final Vertex v1, final Vertex v2, final int pieces) {
        return getEdge(getEdgeKey(v1, v2), v1, v2, pieces);
    }
    
    private final static Edge getEdge(final Line key, final Vertex v1, final Vertex v2, final int pieces) {
        final Edge edge = getEdge(key, v1, v2);
        final List<Line> lines = edge.lines;
        final int existingSize = lines.size();
        if (existingSize >= pieces) {
            return edge;
        } else if (existingSize > 1) {
            throw new UnsupportedOperationException();
        }
        final Line existingLine = lines.get(0);
        lines.clear();
        final Vertex first = existingLine.beginningVertex, last = existingLine.endingVertex;
        final short fx = first.x, lx = last.x, dx = (short) ((lx - fx) / pieces);
        final short fy = first.y, ly = last.y, dy = (short) ((ly - fy) / pieces);
        Vertex prev = v1;
        for (int i = 0; i < pieces; i++) {
            final Vertex next = getVertex((short) (fx + dx * (i + 1) / pieces), (short) (fy + dy * (i + 1) / pieces));
            lines.add(new Line(prev, next));
            prev = next;
        }
        return edge;
    }
    
    private final static Line getEdgeKey(final Vertex v1, final Vertex v2) {
        return (v1.compareTo(v2) < 0) ? new Line(v1, v2) : new Line(v2, v1);
    }
    
    private final static Edge getEdge(final Line key, final Vertex v1, final Vertex v2) {
        ArrayList<Line> edge = edges.get(key);
        if (edge == null) {
            edge = edges.add(key, (key.beginningVertex == v1) ? key : new Line(v1, v2));
        }
        return new Edge(edge, edge.get(0).beginningVertex == v1);
    }
    
    private final static void removeEdge(final Line key) {
        edges.remove(key);
    }
    
    private final static void removeLine(final Line edgeKey, final Line line) {
        final List<Line> edge = edges.get(edgeKey);
        if (edges.size() == 1) { 
            removeEdge(edgeKey);
        } else {
            final Iterator<Line> iter = edge.iterator();
            while (iter.hasNext()) {
                final Line edgeLine = iter.next();
                if (edgeLine.equals(line)) {
                    iter.remove();
                    break;
                }
            }
        }
    }
    
    private final static Side getSide(final Sidedef possibleTextures, final Sidedef neighborOverrideTextures,
            final short xOffset, final short yOffset, final String upperTexture, final String lowerTexture, final String middleTexture,
            final Sector sector) {
        return putIfAbsent(sides, new Side(possibleTextures, neighborOverrideTextures, xOffset, yOffset, upperTexture, lowerTexture, middleTexture, sector));
    }
    
    private final static CellBuilder getCellBuilder(final int i, final int j) {
        if ((i < 0) || (i >= grid.length)) {
            return voidCellBuilder;
        }
        final CellBuilder[] column = grid[i];
        if (j < 0 || j >= column.length) {
            return voidCellBuilder;
        }
        return column[j];
    }
    
    private final static class Line implements Comparable<Line> {
        private final Vertex beginningVertex;
        private final Vertex endingVertex;
        private final Linedef linedef = new Linedef();
        private Side rightSide = null; // Front
        private Side leftSide= null;
        
        private Line(final Vertex beginningVertex, final Vertex endingVertex) {
            this.beginningVertex = beginningVertex;
            this.endingVertex = endingVertex;
        }
        
        private final Side getSide(final boolean front) {
            return front ? rightSide : leftSide;
        }
        
        private final void setSide(final boolean front, final Side side) {
            if (front) {
                rightSide = side;
            } else {
                leftSide = side;
            }
        }
        
        private final Sidedef getPossibleTextures(final boolean front) {
            final Side side = getSide(front);
            return (side == null) ? null : side.possibleTextures;
        }
        
        private final Sidedef getNeighborOverrideTextures(final boolean front) {
            final Side side = getSide(front);
            return (side == null) ? null : side.neighborOverrideTextures;
        }
        
        private final Sector getSector(final boolean front) {
            final Side side = getSide(front);
            return (side == null) ? null : side.sector;
        }
        
        private final Vertex getMinVertex() {
            return (beginningVertex.compareTo(endingVertex) < 0) ? beginningVertex : endingVertex;
        }
        
        private final Vertex getMaxVertex() {
            return (beginningVertex.compareTo(endingVertex) > 0) ? beginningVertex : endingVertex;
        }
        
        @Override
        public final int hashCode() {
            final long h1 = beginningVertex.hashCode(), h2 = endingVertex.hashCode();
            return Long.hashCode((h1 << 32) | h2);
        }
        
        @Override
        public final boolean equals(final Object o) {
            final Line ln = (Line) o;
            return beginningVertex.equals(ln.beginningVertex) && endingVertex.equals(ln.endingVertex);
        }

        @Override
        public int compareTo(final Line o) {
            return Integer.compare(beginningVertex.x + beginningVertex.y + endingVertex.x + endingVertex.y,
                    o.beginningVertex.x + o.beginningVertex.y + o.endingVertex.x + o.endingVertex.y);
        }
    }
    
    private final static class Edge {
        private final List<Line> lines;
        private final boolean front;
        
        private Edge(final List<Line> lines, final boolean front) {
            this.lines = lines;
            this.front = front;
        }
        
        private final Edge sort() {
            final List<Line> copy = new ArrayList<Line>(lines);
            Collections.sort(copy);
            return new Edge(copy, front);
        }
    }
    
    private final static class Side {
        private final Sidedef possibleTextures;
        private final Sidedef neighborOverrideTextures;
        private final short xOffset;
        private final short yOffset;
        private final String upperTexture;
        private final String lowerTexture;
        private final String middleTexture;
        private final Sector sector;
        
        private Side(Sidedef possibleTextures, final Sidedef neighborOverrideTextures,
                final short xOffset, final short yOffset, final String upperTexture, final String lowerTexture, final String middleTexture,
                final Sector sector) {
            this.possibleTextures = possibleTextures;
            this.neighborOverrideTextures = neighborOverrideTextures;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.upperTexture = upperTexture;
            this.lowerTexture = lowerTexture;
            this.middleTexture = middleTexture;
            this.sector = sector;
        }
        
        @Override
        public final int hashCode() {
            return possibleTextures.hashCode() ^ neighborOverrideTextures.hashCode()
                    ^ xOffset ^ yOffset ^ Pantil.hashCode(upperTexture) ^ Pantil.hashCode(lowerTexture) ^ Pantil.hashCode(middleTexture)
                    ^ Pantil.hashCode(sector);
        }
        
        @Override
        public final boolean equals(final Object o) {
            final Side s = (Side) o;
            return possibleTextures.equals(s.possibleTextures) && neighborOverrideTextures.equals(s.neighborOverrideTextures)
                    && (xOffset == s.xOffset) && (yOffset == s.yOffset)
                    && Pantil.equals(upperTexture, s.upperTexture) && Pantil.equals(lowerTexture, s.lowerTexture) && Pantil.equals(middleTexture, s.middleTexture)
                    && Pantil.equals(sector, s.sector);
        }
    }
    
    private final static class CellType {
        private final Sector sector = new Sector();
        private final Sidedef possibleTextures = new Sidedef();
        private final Sidedef neighborOverrideTextures = new Sidedef();
        
        private CellType(final Segment seg) {
            setSector(sector, seg.getField(0));
            setTextures(possibleTextures, seg.getField(1));
            setTextures(neighborOverrideTextures, seg.getField(2));
            // 3 - description
        }
        
        private final static void setSector(final Sector sector, final Piped pip) {
            sector.floorHeight = pip.shortValue(0);
            sector.ceilingHeight = pip.shortValue(1);
            sector.floorTexture = pip.getValue(2);
            sector.ceilingTexture = pip.getValue(3);
            sector.lightLevel = pip.getShort(4, lb.defaultLightLevel);
            sector.sectorSpecial = pip.shortValue(5); // Could map list of strings to bits
            sector.sectorTag = pip.shortValue(6); // Unlikely to be used; builder will generate as needed
        }
        
        private final static void setTextures(final Sidedef possibleTextures, final Piped pip) {
            possibleTextures.xOffset = pip.initShort(0);
            possibleTextures.yOffset = pip.initShort(1);
            possibleTextures.upperTexture = pip.getValue(2);
            possibleTextures.lowerTexture = pip.getValue(3);
            possibleTextures.middleTexture = pip.getValue(4);
            // Can't assign sector, this just defines which textures to use, can end up being middle or upper or lower or upper and lower
        }
    }
    
    private abstract static class CellBuilder {
        protected int i, j;
        protected int x, y;
        protected Vertex v00, v01, v10, v11;
        
        protected void build(final int i, final int j) {
            this.i = i; this.j = j;
            x = i * cw; y = j * ch;
            v00 = getVertex(x, y); v01 = getVertex(x, y + ch); v10 = getVertex(x + cw, y); v11 = getVertex(x + cw, y + ch);
        }
        
        protected abstract void build();
        
        protected boolean isVoid() {
            return false;
        }
    }
    
    private final static class SimpleCellBuilder extends CellBuilder {
        private final CellType cellType;
        
        private SimpleCellBuilder(final CellType cellType) {
            this.cellType = cellType;
        }
        
        @Override
        protected final void build() {
            //final CellBuilder left = getCellBuilder(x - 1, y);
            //final List<Line> left = getEdge(v00, v01);
            //final Edge left = getEdge(v00, v01);
            processEdge(v00, v01);
            processEdge(v01, v11);
            processEdge(v11, v10);
            processEdge(v10, v00);
        }
        
        private final void processEdge(final Vertex v1, final Vertex v2) {
            Img2Wad.processEdge(v1, v2, cellType);
        }
    }
    
    private final static void processEdge(final Vertex v1, final Vertex v2, final CellType cellType) {
        final Line edgeKey = getEdgeKey(v1, v2);
        final Edge edge = getEdge(edgeKey, v1, v2);
        processEdge(edgeKey, edge, cellType);
    }
    
    private final static void processEdge(final Line edgeKey, final Edge edge, final CellType cellType) {
        final boolean thisFlag= edge.front;
        for (final Line line : edge.lines) {
            processLine(edgeKey, line, cellType, thisFlag);
        }
    }
    
    private final static void processLine(final Line edgeKey, final Line line, final CellType cellType, final boolean thisFlag) {
        final boolean otherFlag = !thisFlag;
        final Sector thisSector = cellType.sector;
        final Sidedef possibleTextures = cellType.possibleTextures, neighborOverrideTextures = cellType.neighborOverrideTextures;
        final Sidedef otherPossibleTextures = line.getPossibleTextures(otherFlag), otherNeighborOverrideTextures = line.getNeighborOverrideTextures(otherFlag);
        final Sector otherSector = line.getSector(otherFlag);
        if (otherSector == null) {
            line.setSide(thisFlag, getSide(possibleTextures, neighborOverrideTextures,
                    possibleTextures.xOffset, possibleTextures.yOffset, null, null, possibleTextures.middleTexture, thisSector));
            line.linedef.setFlag(Wads.LINEDEF_FLAG_SOUNDBLOCK, true); // Could get this from the cell type's linedef
        } else if (otherSector == thisSector) {
            removeLine(edgeKey, line);
        } else {
            final String thisLower, otherLower;
            if (thisSector.floorHeight < otherSector.floorHeight) {
                thisLower = Chartil.nvl(otherNeighborOverrideTextures.lowerTexture, possibleTextures.lowerTexture);
                otherLower = null;
            } else if (thisSector.floorHeight > otherSector.floorHeight) {
                thisLower = null;
                otherLower = Chartil.nvl(neighborOverrideTextures.lowerTexture, otherPossibleTextures.lowerTexture);
            } else {
                thisLower = null;
                otherLower = null;
            }
            final String thisUpper, otherUpper;
            if (thisSector.ceilingHeight < otherSector.ceilingHeight) {
                thisUpper = null;
                otherUpper = Chartil.nvl(neighborOverrideTextures.upperTexture, otherPossibleTextures.upperTexture);
            } else if (thisSector.ceilingHeight > otherSector.ceilingHeight) {
                thisUpper = Chartil.nvl(otherNeighborOverrideTextures.upperTexture, possibleTextures.upperTexture);
                otherUpper = null;
            } else {
                thisUpper = null;
                otherUpper = null;
            }
            line.setSide(thisFlag, getSide(possibleTextures, neighborOverrideTextures,
                    possibleTextures.xOffset, possibleTextures.yOffset, thisUpper, thisLower, null, thisSector));
            line.setSide(otherFlag, getSide(otherPossibleTextures, otherNeighborOverrideTextures,
                    otherPossibleTextures.xOffset, otherPossibleTextures.yOffset, otherUpper, otherLower, null, otherSector));
            line.linedef.setFlag(Wads.LINEDEF_FLAG_SOUNDBLOCK, false);
            line.linedef.setFlag(Wads.LINEDEF_FLAG_TWOSIDED, true);
        }
    }
    
    private final static int DIR_NORTH = 0;
    private final static int DIR_EAST = 1;
    private final static int DIR_SOUTH = 2;
    private final static int DIR_WEST = 3;
    
    private final static class StairCellBuilder extends CellBuilder {
        // Add a mechanism so that bottom sector/cellType can be inferred from neighbor?  Could get complicated, which neighbor? What if next to other stair cell (wide stairs)?
        // Maybe best not to worry about that
        private final List<CellType> stepTypes;
        private final int direction;
        
        private StairCellBuilder(final List<CellType> stepTypes, final int direction) {
            this.stepTypes = stepTypes;
            this.direction = direction;
        }
        
        @Override
        protected final void build() {
            final int steps = stepTypes.size(), last = steps - 1;
            final int verticalPieces, horizontalPieces;
            final boolean vertical = (direction == DIR_NORTH) || (direction == DIR_SOUTH);
            if (vertical) {
                verticalPieces = steps;
                horizontalPieces = 1;
            } else {
                verticalPieces = 1;
                horizontalPieces = steps;
            }
            final Line westKey = getEdgeKey(v00, v01), northKey = getEdgeKey(v01, v11);
            final Line eastKey = getEdgeKey(v11, v10), southKey = getEdgeKey(v10, v00);
            final Edge west = getEdge(westKey, v00, v01, verticalPieces).sort(), north = getEdge(northKey, v01, v11, horizontalPieces).sort();
            final Edge east = getEdge(eastKey, v11, v10, verticalPieces).sort(), south = getEdge(southKey, v10, v00, horizontalPieces).sort();
            final Line sideKey1, sideKey2, minKey, maxKey;
            final Edge side1, side2, min, max;
            if (vertical) {
                sideKey1 = eastKey; sideKey2 = westKey; // min side1 to min side2 is clockwise
                side1 = east; side2 = west;
                if (direction == DIR_NORTH) {
                    minKey = southKey; maxKey = northKey;
                    min = south; max = north;
                } else {
                    minKey = northKey; maxKey = southKey;
                    min = north; max = south;
                }
            } else {
                sideKey1 = southKey; sideKey2 = northKey; // min side1 to min side2 is clockwise
                side1 = south; side2 = north;
                if (direction == DIR_EAST) {
                    minKey = westKey; maxKey = eastKey;
                    min = west; max = east;
                } else {
                    minKey = eastKey; maxKey = westKey;
                    min = east; max = west;
                }
            }
            processEdge(minKey, min, stepTypes.get(0));
            processEdge(maxKey, max, stepTypes.get(last));
            final boolean positive = (direction == DIR_NORTH) || (direction == DIR_EAST);
            for (int i = 0; i < steps; i++) {
                final int c = positive ? i : (last - i);
                final CellType cellType = stepTypes.get(c);
                final Line line1 = side1.lines.get(c), line2 = side2.lines.get(c);
                processLine(sideKey1, line1, cellType, side1.front);
                processLine(sideKey2, line2, cellType, side2.front);
                if (i > 0) {
                    processEdge(line1.getMinVertex(), line2.getMinVertex(), cellType);
                }
                if (i < last) {
                    processEdge(line2.getMaxVertex(), line1.getMaxVertex(), cellType);
                }
            }
            throw new UnsupportedOperationException();
        }
    }
    
    private final static class DoorCellBuilder extends CellBuilder {
        @Override
        protected final void build() {
            throw new UnsupportedOperationException();
        }
    }
    
    private final static class VoidCellBuilder extends CellBuilder {
        @Override
        protected final void build() {
        }
        
        @Override
        protected final boolean isVoid() {
            return true;
        }
    }
    private final static VoidCellBuilder voidCellBuilder = new VoidCellBuilder();
    
    private final static class LevelBuilder {
        private short cellWidth;
        private short cellHeight;
        private short defaultLightLevel;
        private final List<CellType> cellTypes = new ArrayList<CellType>();
        private final Map<Integer, CellBuilder> rgbs = new HashMap<Integer, CellBuilder>();
        private final Img img;
        
        private LevelBuilder(final String loc) throws IOException {
            this(new SegmentStream(Iotil.getReader(loc)), Imtil.load(loc.substring(0, loc.lastIndexOf('.')) + ".png"));
        }
        
        private LevelBuilder(final SegmentStream in, final Img img) throws IOException {
            lb = this;
            this.img = img;
            Segment seg;
            
            seg = in.readRequire("CEL");
            cellWidth = seg.shortValue(0);
            cellHeight = seg.shortValue(1);
            while ((seg = in.readIf("SCT")) != null) {
                cellTypes.add(new CellType(seg));
            }
            
            seg = in.readRequire("DEF");
            defaultLightLevel = seg.getShort(0, Short.MAX_VALUE);
            
            while ((seg = in.readIf("RGB")) != null) {
                final Field fld = seg.getField(0);
                final int r = fld.intValue(0);
                final int g = fld.intValue(1);
                final int b = fld.intValue(2);
                final int a = fld.getInt(3, Pancolor.MAX_VALUE);
                final int rgb = cm.getDataElement(r, g, b, a);
                final String cellType = seg.getValue(1);
                final CellBuilder cellBuilder;
                if ("Simple".equalsIgnoreCase(cellType)) {
                    cellBuilder = new SimpleCellBuilder(cellTypes.get(seg.intValue(2)));
                } else if ("Stair".equalsIgnoreCase(cellType)) {
                    final List<Field> reps = seg.getRepetitions(2);
                    final List<CellType> stepTypes = new ArrayList<CellType>(reps.size());
                    for (final Field rep : reps) {
                        stepTypes.add(cellTypes.get(rep.intValue()));
                    }
                    cellBuilder = new StairCellBuilder(stepTypes, seg.intValue(3));
                } else if ("Void".equalsIgnoreCase(cellType)) {
                    cellBuilder = voidCellBuilder;
                } else {
                    throw new IllegalStateException("Unexpected cell type " + cellType);
                }
                rgbs.put(Integer.valueOf(rgb), cellBuilder);
            }
        }
        
        private final CellBuilder getCellBuilder(final int rgb) {
            final CellBuilder cellBuilder = rgbs.get(Integer.valueOf(rgb));
            if (cellBuilder == null) {
                throw new IllegalStateException("No cellBuilder specified for R" + cm.getRed(rgb) + ",G" + cm.getGreen(rgb) + ",B" + cm.getBlue(rgb));
            }
            return cellBuilder.isVoid() ? null : cellBuilder;
        }
        
        private final Level buildLevel() {
            cw = cellWidth;
            ch = cellHeight;
            vertexes.clear();
            final int w = img.getWidth(), h = img.getHeight();
            grid = new CellBuilder[w][h];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    final int rgb = img.getRGB(x, h - y - 1);
                    final CellBuilder cellBuilder = getCellBuilder(rgb);
                    grid[x][y] = cellBuilder;
                }
            }
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    grid[x][y].build(x, y);
                }
            }
            throw new UnsupportedOperationException();
        }
    }
}
