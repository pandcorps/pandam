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
    private final static List<Thing> things = new ArrayList<Thing>();
    
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
    
    private final static Edge getEdge(final Vertex v1, final Vertex v2, final float[] ms) {
        return getEdge(getEdgeKey(v1, v2), v1, v2, ms);
    }
    
    private final static Edge getEdge(final Line key, final Vertex v1, final Vertex v2, final int pieces) {
        final float[] ms = new float[pieces];
        final float m = 1.0f / pieces;
        for (int i = 0; i < pieces; i++) {
            ms[i] = m;
        }
        return getEdge(key, v1, v2, ms);
    }
    
    private final static Edge getEdge(final Line key, final Vertex v1, final Vertex v2, final float[] ms) {
        final Edge edge = getEdge(key, v1, v2);
        if (edge == null) {
            return null;
        }
        final List<Line> lines = edge.lines;
        final int existingSize = lines.size();
        final int pieces = ms.length;
        if (existingSize >= pieces) {
            return edge;
        } else if (existingSize > 1) {
            throw new UnsupportedOperationException();
        }
        final Line existingLine = lines.get(0);
        lines.clear();
        final Vertex first = existingLine.beginningVertex, last = existingLine.endingVertex;
        final short fx = first.x, lx = last.x, dx = (short) (lx - fx);
        final short fy = first.y, ly = last.y, dy = (short) (ly - fy);
        Vertex prev = v1;
        for (int i = 0; i < pieces; i++) {
            final float m = ms[i];
            final Vertex next = getVertex((short) (prev.x + Math.round(m * dx)), (short) (prev.y + Math.round(m * dy)));
            lines.add(new Line(prev, next));
            prev = next;
        }
        return edge;
    }
    
    private final static Line getEdgeKey(final Vertex v1, final Vertex v2) {
        return (v1.compareTo(v2) < 0) ? new Line(v1, v2) : new Line(v2, v1);
    }
    
    private static boolean edgeCreationAllowed = true;
    
    private final static Edge getEdge(final Line key, final Vertex v1, final Vertex v2) {
        ArrayList<Line> edge = edges.get(key);
        boolean existed = true;
        if (edge == null) {
            if (!edgeCreationAllowed) {
                return null;
            }
            edge = edges.add(key, (key.beginningVertex == v1) ? key : new Line(v1, v2));
            existed = false;
        }
        return new Edge(key, edge, edge.get(0).beginningVertex == v1, existed);
    }
    
    private final static Edge getEdgeIfExists(final Vertex v1, final Vertex v2) {
        try {
            edgeCreationAllowed = false;
            return getEdge(v1, v2);
        } finally {
            edgeCreationAllowed = true;
        }
    }
    
    private final static void removeEdge(final Line key) {
        edges.remove(key);
    }
    
    private final static void removeLine(final Edge edge, final Line line) {
        final Line edgeKey = edge.key;
        final List<Line> lines = edges.get(edgeKey);
        if (edges.size() == 1) { 
            removeEdge(edgeKey);
        } else {
            final Iterator<Line> iter = lines.iterator();
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
        private final Line key;
        private final List<Line> lines;
        private final boolean front;
        private final boolean existed;
        
        private Edge(final Line key, final List<Line> lines, final boolean front, final boolean existed) {
            this.key = key;
            this.lines = lines;
            this.front = front;
            this.existed = existed;
        }
        
        private final Edge sort() {
            final List<Line> copy = new ArrayList<Line>(lines);
            Collections.sort(copy);
            return new Edge(key, copy, front, existed);
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
        
        private final Sidedef toSidedef(final short sectorReference) {
            final Sidedef sidedef = new Sidedef();
            sidedef.xOffset = xOffset;
            sidedef.yOffset = yOffset;
            sidedef.upperTexture = upperTexture;
            sidedef.lowerTexture = lowerTexture;
            sidedef.middleTexture = middleTexture;
            sidedef.sectorReference = sectorReference;
            return sidedef;
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
        private final Sector sector;
        private final Sidedef possibleTextures; // Maybe allow four of these for N/E/S/W, but use first for all if no others
        private final Sidedef neighborOverrideTextures;
        private final Short lineType;
        private CellType diagonal = null;
        
        private CellType(final Sector sector, final Sidedef possibleTextures, final Sidedef neighborOverrideTextures, final Short lineType) {
            this.sector = sector;
            this.possibleTextures = possibleTextures;
            this.neighborOverrideTextures = neighborOverrideTextures;
            this.lineType = lineType;
        }
        
        private CellType(final Segment seg) {
            sector = new Sector();
            possibleTextures = new Sidedef();
            neighborOverrideTextures = new Sidedef();
            setSector(sector, seg.getField(0));
            setTextures(possibleTextures, seg.getField(1));
            setTextures(neighborOverrideTextures, seg.getField(2));
            lineType = seg.toShort(3);
            // 4 - description
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
        
        private final CellType newSector() {
            return new CellType(sector.copy(), possibleTextures, neighborOverrideTextures, lineType);
        }
        
        private final CellType getDiagonal() {
            return (diagonal == null) ? this : diagonal;
        }
    }
    
    private static int minPriority = Integer.MAX_VALUE;
    private static int maxPriority = Integer.MIN_VALUE;
    private final static CountMap<CellType> neighborMap = new CountMap<CellType>();
    
    private abstract static class CellBuilder {
        protected final int priority;
        protected int i, j;
        protected int x, y;
        protected Vertex v00, v01, v10, v11;
        
        protected CellBuilder(final int priority) {
            this.priority = priority;
            if (priority < minPriority) {
                minPriority = priority;
            }
            if (priority > maxPriority) {
                maxPriority = priority;
            }
        }
        
        protected void build(final int i, final int j) {
            this.i = i; this.j = j;
            x = i * cw; y = j * ch;
            v00 = getVertex(x, y); v01 = getVertex(x, y + ch); v10 = getVertex(x + cw, y); v11 = getVertex(x + cw, y + ch);
        }
        
        protected abstract void build();
        
        protected final void addThing(final short type) {
            final Thing thing = new Thing();
            thing.x = (short) (x + (cw / 2));
            thing.y = (short) (y + (ch / 2));
            thing.angle = 0;
            thing.type = type;
            thing.spawnFlags = Wads.SPAWN_FLAG_EASY | Wads.SPAWN_FLAG_MEDIUM | Wads.SPAWN_FLAG_HARD;
        }
        
        protected final void buildSimple(final CellType cellType) {
            processEdge(v00, v01, cellType);
            processEdge(v01, v11, cellType);
            processEdge(v11, v10, cellType);
            processEdge(v10, v00, cellType);
        }
        
        protected final CellType getCellType(final int i, final int j) {
            if ((i < 0) || (i >= grid.length)) {
                return null;
            }
            final CellBuilder[] col = grid[i];
            if ((j < 0) || (j >= col.length)) {
                return null;
            }
            final CellBuilder cellBuilder = col[j];
            if ((cellBuilder == null) || cellBuilder.isVoid()) {
                return null;
            }
            return ((SimpleCellBuilder) cellBuilder).cellType;
        }
        
        protected final CellType getNeighborCellType() {
            neighborMap.clear();
            addNeighbor(-1, 0);
            addNeighbor(1, 0);
            addNeighbor(0, -1);
            addNeighbor(0, 1);
            //TODO Break tie by picking the one with the largest floor-to-ceiling difference
            return neighborMap.sortedEntryList(false).get(0).getKey();
        }
        
        private final void addNeighbor(final int di, final int dj) {
            final CellType cellType = getCellType(i + di, j + dj);
            if (cellType != null) {
                neighborMap.inc(cellType);
            }
        }
        
        protected final void buildSimpleThing(final short thingType) {
            buildSimple(getNeighborCellType());
            addThing(thingType);
        }
        
        protected boolean isVoid() {
            return false;
        }
    }
    
    private final static class SimpleCellBuilder extends CellBuilder {
        private final CellType cellType;
        
        private SimpleCellBuilder(final CellType cellType) {
            super(1);
            this.cellType = cellType;
        }
        
        @Override
        protected final void build() {
            buildSimple(cellType);
        }
    }
    
    private final static void processEdge(final Vertex v1, final Vertex v2, final CellType cellType) {
        final Edge edge = getEdge(v1, v2);
        processEdge(edge, cellType);
    }
    
    private final static void processEdge(final Edge edge, final CellType cellType) {
        for (final Line line : edge.lines) {
            processLine(edge, line, cellType);
        }
    }
    
    private final static void processLine(final Edge edge, final Line line, final CellType cellType) {
        final boolean thisFlag = edge.front, otherFlag = !thisFlag;
        final Sector thisSector = cellType.sector;
        final Sidedef possibleTextures = cellType.possibleTextures, neighborOverrideTextures = cellType.neighborOverrideTextures;
        final Sidedef otherPossibleTextures = line.getPossibleTextures(otherFlag), otherNeighborOverrideTextures = line.getNeighborOverrideTextures(otherFlag);
        final Sector otherSector = line.getSector(otherFlag);
        if (otherSector == null) {
            line.setSide(thisFlag, getSide(possibleTextures, neighborOverrideTextures,
                    possibleTextures.xOffset, possibleTextures.yOffset, null, null, possibleTextures.middleTexture, thisSector));
            line.linedef.setFlag(Wads.LINEDEF_FLAG_SOUNDBLOCK, true); // Could get this from the cell type's linedef
        } else if (otherSector == thisSector) {
            removeLine(edge, line);
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
    
    private final static class ThingCellBuilder extends CellBuilder {
        private final short thingType;
        
        private ThingCellBuilder(final short thingType) {
            super(2);
            this.thingType = thingType;
        }
        
        @Override
        protected final void build() {
            buildSimpleThing(thingType);
        }
    }
    
    private final static class DiagonalCellBuilder extends CellBuilder {
        private DiagonalCellBuilder() {
            super(1);
        }
        
        @Override
        protected final void build() {
            final CellType west = getType(i - 1, j), north = getType(i, j + 1);
            final CellType east = getType(i + 1, j), south = getType(i, j - 1);
            processEdge(v00, v01, west);
            processEdge(v01, v11, north);
            processEdge(v11, v10, east);
            processEdge(v10, v00, south);
            if ((west == north) && (east == south)) {
                processEdge(v00, v11, west);
                processEdge(v11, v00, east);
            } else if ((east == north) && (west == south)) {
                processEdge(v01, v10, west);
                processEdge(v10, v01, east);
            } else {
                throw new IllegalStateException();
            }
        }
        
        private final CellType getType(final int i, final int j) {
            final CellType baseType = getCellType(i, j);
            return (baseType == null) ? null : baseType.getDiagonal();
        }
    }
    
    private final static DiagonalCellBuilder diagonalCellBuilder = new DiagonalCellBuilder();
    
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
            super(1);
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
            final Edge west = getEdge(v00, v01, verticalPieces).sort(), north = getEdge(v01, v11, horizontalPieces).sort();
            final Edge east = getEdge(v11, v10, verticalPieces).sort(), south = getEdge(v10, v00, horizontalPieces).sort();
            final Edge side1, side2, min, max;
            if (vertical) {
                side1 = east; side2 = west; // min side1 to min side2 is clockwise
                if (direction == DIR_NORTH) {
                    min = south; max = north;
                } else {
                    min = north; max = south;
                }
            } else {
                side1 = south; side2 = north; // min side1 to min side2 is clockwise
                if (direction == DIR_EAST) {
                    min = west; max = east;
                } else {
                    min = east; max = west;
                }
            }
            processEdge(min, stepTypes.get(0));
            processEdge(max, stepTypes.get(last));
            final boolean positive = (direction == DIR_NORTH) || (direction == DIR_EAST);
            for (int i = 0; i < steps; i++) {
                final int c = positive ? i : (last - i);
                final CellType cellType = stepTypes.get(c);
                final Line line1 = side1.lines.get(c), line2 = side2.lines.get(c);
                processLine(side1, line1, cellType);
                processLine(side2, line2, cellType);
                if (i > 0) {
                    processEdge(line1.getMinVertex(), line2.getMinVertex(), cellType);
                }
                if (i < last) {
                    processEdge(line2.getMaxVertex(), line1.getMaxVertex(), cellType);
                }
            }
        }
    }
    
    private final static class BlockDoorCellBuilder extends CellBuilder {
        private final CellType exampleCellType; // Will create a new cell type with a new sector for each door built
        private final Short thingType;
        
        private BlockDoorCellBuilder(final CellType exampleCellType, final Short thingType) {
            super(2);
            this.exampleCellType = exampleCellType;
            this.thingType = thingType;
        }
        
        @Override
        protected final void build() {
            final CellType cellType = exampleCellType.newSector();
            final Edge west = initEdge(v00, v01, cellType);
            final Edge north = initEdge(v01, v11, cellType);
            final Edge east = initEdge(v11, v10, cellType);
            final Edge south = initEdge(v10, v00, cellType);
            if (west.existed && east.existed && !north.existed && !south.existed) {
                initDoor(west, east, north, south, cellType);
            } else if (north.existed && south.existed && !west.existed && !east.existed) {
                initDoor(north, south, west, east, cellType);
            } else if (thingType != null) {
                buildSimpleThing(thingType.shortValue());
            }
        }
        
        private final Edge initEdge(final Vertex v1, final Vertex v2, final CellType cellType) {
            final Edge edge = getEdge(v1, v2);
            processEdge(edge, cellType);
            return edge;
        }
    }
    
    private static short sectorTagSequence = 1;
    
    private final static void initDoor(final Edge edge1, final Edge edge2, final Edge track1, final Edge track2, final CellType cellType) {
        final short sectorTag = sectorTagSequence++;
        initDoor(edge1, sectorTag, cellType);
        initDoor(edge2, sectorTag, cellType);
        initDoorTrack(track1);
        initDoorTrack(track2);
    }
    
    private final static void initDoor(final Line line1, final Line line2, final Line track1, final Line track2, final CellType cellType) {
        final short sectorTag = sectorTagSequence++;
        initDoor(line1, sectorTag, cellType);
        initDoor(line2, sectorTag, cellType);
        initDoorTrack(track1);
        initDoorTrack(track2);
    }
    
    private final static void initDoor(final Edge edge, final short sectorTag, final CellType cellType) {
        for (final Line line : edge.lines) {
            initDoor(line, sectorTag, cellType);
        }
    }
    
    private final static void initDoor(final Line line, final short sectorTag, final CellType cellType) {
        final Short lineType = cellType.lineType;
        line.linedef.lineType = (lineType == null) ? Wads.LINEDEF_TYPE_DOOR_PR_SLOW_MONST_OPEN_CLOSE : lineType.shortValue();
        line.linedef.sectorTag = sectorTag;
        cellType.sector.sectorTag = sectorTag;
    }
    
    private final static void initDoorTrack(final Edge edge) {
        for (final Line line : edge.lines) {
            initDoorTrack(line);
        }
    }
    
    private final static void initDoorTrack(final Line line) {
        line.linedef.setFlag(Wads.LINEDEF_FLAG_DONTPEGBOTTOM, true);
    }
    
    private final static class DoorCellBuilder extends CellBuilder {
        private final CellType indentationCellType;
        private final CellType exampleDoorCellType;
        private final int doorWidth;
        private final float[] ms = new float[3];
        
        private DoorCellBuilder(final CellType indentationCellType, final CellType exampleDoorCellType, final int doorWidth) {
            super(1);
            this.indentationCellType = indentationCellType;
            this.exampleDoorCellType = exampleDoorCellType;
            this.doorWidth = doorWidth;
            if (cw != ch) {
                throw new UnsupportedOperationException();
            }
            final float indentationWidth = (cw - doorWidth) / 2;
            final float indentationMultiplier = indentationWidth / cw;
            ms[0] = indentationMultiplier;
            ms[1] = ((float) doorWidth) / cw;
            ms[2] = indentationMultiplier;
        }
        
        @Override
        protected final void build() {
            final CellType doorCellType = exampleDoorCellType.newSector();
            final Edge trackEdge1, entryEdge1, trackEdge2, entryEdge2;
            if (grid[i - 1][j].isVoid() && grid[i + 1][j].isVoid()) {
                // Vertical
                trackEdge1 = getEdge(v00, v01, ms).sort();
                entryEdge1 = getEdge(v01, v11);
                trackEdge2 = getEdge(v11, v10, ms).sort();
                entryEdge2 = getEdge(v10, v00);
            } else if (grid[i][j - 1].isVoid() && grid[i][j + 1].isVoid()) {
                // Horizontal
                entryEdge1 = getEdge(v00, v01);
                trackEdge1 = getEdge(v01, v11, ms).sort();
                entryEdge2 = getEdge(v11, v10);
                trackEdge2 = getEdge(v10, v00, ms).sort();
            } else {
                throw new IllegalStateException();
            }
            processEdge(entryEdge1, indentationCellType);
            processEdge(entryEdge2, indentationCellType);
            processTrack(trackEdge1, doorCellType);
            processTrack(trackEdge2, doorCellType);
            final Line trackLine1 = trackEdge1.lines.get(1);
            final Line trackLine2 = trackEdge2.lines.get(1);
            initDoor(getDoorLine(trackLine1.getMinVertex(), trackLine2.getMinVertex(), doorCellType),
                    getDoorLine(trackLine1.getMaxVertex(), trackLine2.getMaxVertex(), doorCellType),
                    entryEdge1.lines.get(1), entryEdge2.lines.get(1), doorCellType);
        }
        
        private final void processTrack(final Edge edge, final CellType doorCellType) {
            processLine(edge, edge.lines.get(0), indentationCellType);
            final Line track = edge.lines.get(1);
            processLine(edge, track, doorCellType);
            initDoorTrack(track);
            processLine(edge, edge.lines.get(2), indentationCellType);
        }
        
        private final static Line getDoorLine(final Vertex v1, final Vertex v2, final CellType doorCellType) {
            final Edge edge = getEdge(v1, v2);
            processEdge(edge, doorCellType);
            final List<Line> lines = edge.lines;
            if (lines.size() == 1) {
                return lines.get(0);
            }
            throw new IllegalStateException();
        }
    }
    
    private final static class VoidCellBuilder extends CellBuilder {
        private VoidCellBuilder() {
            super(1);
        }
        
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
        private final String name;
        private final short cellWidth;
        private final short cellHeight;
        private final short defaultLightLevel;
        private final List<CellType> cellTypes = new ArrayList<CellType>();
        private final Map<Integer, CellBuilder> rgbs = new HashMap<Integer, CellBuilder>();
        private final Img img;
        
        private LevelBuilder(final String loc) throws IOException {
            this(loc, new SegmentStream(Iotil.getReader(loc)), Imtil.load(loc.substring(0, loc.lastIndexOf('.')) + ".png"));
        }
        
        private LevelBuilder(final String loc, final SegmentStream in, final Img img) throws IOException {
            lb = this;
            final String fileName = new File(loc).getName();
            name = fileName.substring(0, fileName.indexOf('.'));
            this.img = img;
            Segment seg;
            
            seg = in.readRequire("CEL");
            cellWidth = seg.shortValue(0);
            cellHeight = seg.shortValue(1);
            while ((seg = in.readIf("SCT")) != null) {
                final CellType cellType = new CellType(seg);
                seg = in.readIf("DGN");
                if (seg != null) {
                    // Used by DiagonalCellBuilder, could be similar wall textures adjusted size for diagonal walls, like 91 pixels instead of 64.
                    // If original has two 32-pixel sections, could have 3 30-31-pixel sections on diagonal.
                    cellType.diagonal = new CellType(seg);
                }
                cellTypes.add(cellType);
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
                } else if ("Thing".equalsIgnoreCase(cellType)) {
                    cellBuilder = new ThingCellBuilder(seg.shortValue(2));
                } else if ("Diagonal".equalsIgnoreCase(cellType)) {
                    cellBuilder = diagonalCellBuilder;
                } else if ("Stair".equalsIgnoreCase(cellType)) {
                    final List<Field> reps = seg.getRepetitions(2);
                    final List<CellType> stepTypes = new ArrayList<CellType>(reps.size());
                    for (final Field rep : reps) {
                        stepTypes.add(cellTypes.get(rep.intValue()));
                    }
                    cellBuilder = new StairCellBuilder(stepTypes, seg.intValue(3));
                } else if ("BlockDoor".equalsIgnoreCase(cellType)) {
                    cellBuilder = new BlockDoorCellBuilder(cellTypes.get(seg.intValue(2)), seg.toShort(3));
                } else if ("Door".equalsIgnoreCase(cellType)) {
                    cellBuilder = new DoorCellBuilder(cellTypes.get(seg.intValue(2)), cellTypes.get(seg.intValue(3)), seg.intValue(4));
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
            for (int priority = minPriority; priority <= maxPriority; priority++) {
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        if (grid[x][y].priority != priority) {
                            continue;
                        }
                        grid[x][y].build(x, y);
                    }
                }
            }
            combine();
            return convert();
        }
        
        private final void combine() {
            //TODO Combine adjacent lines when possible, maybe sectors too if any chances missed by building code
        }
        
        private Map<Vertex, Short> vertexMap = null;
        private Lump<Vertex> vertexLump = null;
        private Map<Side, Short> sideMap = null;
        private Map<Sidedef, Short> sidedefMap = null;
        private Lump<Sidedef> sidedefLump = null;
        private Map<Sector, Short> sectorMap = null;
        private Lump<Sector> sectorLump = null;
        
        private final Level convert() {
            final Level level = new Level(name);
            vertexMap = new HashMap<Vertex, Short>();
            vertexLump = level.vertexes;
            sideMap = new HashMap<Side, Short>();
            sidedefMap = new HashMap<Sidedef, Short>();
            sidedefLump = level.sidedefs;
            sectorMap = new HashMap<Sector, Short>();
            sectorLump = level.sectors;
            final Lump<Linedef> linedefs = level.linedefs;
            for (final List<Line> edge : edges.values()) {
                for (final Line line : edge) {
                    final Linedef linedef = line.linedef;
                    linedef.beginningVertex = getVertexIndex(line.beginningVertex);
                    linedef.endingVertex = getVertexIndex(line.endingVertex);
                    linedef.rightSidedef = getSidedefIndex(line.rightSide);
                    linedef.leftSidedef = getSidedefIndex(line.leftSide);
                    linedefs.add(linedef);
                }
            }
            // Don't need to prune Img2Wad.vertexes/sides; those are only used while processing image; conversion to raw format only uses what's found in edges
            level.things.addAll(things);
            return level;
        }
        
        private final short getVertexIndex(final Vertex vertex) {
            return getIndex(vertexMap, vertexLump, vertex);
        }
        
        private final short getSidedefIndex(final Side side) {
            if (side == null) {
                return Wads.SIDEDEF_NONE;
            }
            Short value = sideMap.get(side);
            if (value != null) {
                return value.shortValue();
            }
            final Sidedef sidedef = side.toSidedef(getSectorIndex(side.sector));
            value = sidedefMap.get(sidedef);
            if (value == null) {
                sidedefLump.add(sidedef);
                final short index = (short) (sidedefLump.size() - 1);
                value = Short.valueOf(index);
                sidedefMap.put(sidedef, value);
            }
            sideMap.put(side, value);
            return value.shortValue();
        }
        
        private final short getSectorIndex(final Sector sector) {
            return getIndex(sectorMap, sectorLump, sector);
        }
        
        private final <T extends LumpType> short getIndex(final Map<T, Short> map, final Lump<T> lump, final T key) {
            final Short value = map.get(key);
            if (value != null) {
                return value.shortValue();
            }
            lump.add(key);
            final short index = (short) (lump.size() - 1);
            map.put(key, Short.valueOf(index));
            return index;
        }
    }
}
