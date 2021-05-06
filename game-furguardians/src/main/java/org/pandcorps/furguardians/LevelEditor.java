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
package org.pandcorps.furguardians;

import java.lang.reflect.Constructor;
import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.furguardians.Character.*;
import org.pandcorps.furguardians.Player.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.Panteraction.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.tile.*;

public class LevelEditor {
    private final static Panple tileSize = new FinPanple2(16, 16);
    private static Tile edgeLeft = null;
    private static Tile edgeRight = null;
    private static Tile edgeTop = null;
    private static Tile edgeBottom = null;
    private static Tile blockSolid = null;
    private static Tile blockUpSlope = null;
    private static Tile blockDownSlope = null;
    private static Tile groundTopLeftConvex = null;
    private static Tile groundTop = null;
    private static Tile groundTopRightConvex = null;
    private static Tile groundLeft = null;
    private static Tile groundMiddle = null;
    private static Tile groundRight = null;
    private static Tile groundTopLeftConcave = null;
    private static Tile groundDecorated = null;
    private static Tile groundTopRightConcave = null;
    private static Tile groundUpSlope = null;
    private static Tile groundDownSlope = null;
    private final static HashMap<java.lang.Character, CellType> cellTypeMap = new HashMap<java.lang.Character, CellType>();
    private final static Random currentRandom = new Random();
    private static RoomDefinition roomDef;
    private static GridDefinition grid;
    private static int cx, cy, currentIndex;
    private static Panmage iconTubeDown = null;
    private static Panmage iconTubeUp = null;
    private static Panmage iconTubeRight = null;
    private static Panmage iconTubeLeft = null;
    private static Panmage iconRock = null;
    private static Panmage selectedTile = null;
    private static Cursor cursor = null;
    private static int touchStartTileX = -1;
    private static int touchStartTileY = -1;
    private final static List<Editor> editors = new ArrayList<Editor>();
    
    protected final static void loadCommonTiles() {
        if (edgeLeft != null) {
            return;
        }
        final TileMap tm = Level.tm;
        edgeRight = tm.getTile(null, newCommon(32, 16, 1f, 1f, 1f), Tile.BEHAVIOR_OPEN);
        edgeLeft = tm.getTile(null, newCommon(32, 16, 0, true, false, 1f, 1f, 1f), Tile.BEHAVIOR_OPEN);
        edgeTop = tm.getTile(null, newCommon(32, 16, 1, false, false, 1f, 1f, 1f), Tile.BEHAVIOR_OPEN);
        edgeBottom = tm.getTile(null, newCommon(32, 16, 3, false, false, 1f, 1f, 1f), Tile.BEHAVIOR_OPEN);
        blockSolid = tm.getTile(null, newCommon(64, 0, 1f, 1f, 1f), Tile.BEHAVIOR_SOLID);
        blockUpSlope = tm.getTile(null, newCommon(96, 0, 1f, 1f, 1f), FurGuardiansGame.TILE_UPSLOPE);
        blockDownSlope = tm.getTile(null, newCommon(112, 0, 1f, 1f, 1f), FurGuardiansGame.TILE_DOWNSLOPE);
        groundTopLeftConvex = tm.getTile(null, Level.imgMap[1][0], Tile.BEHAVIOR_SOLID);
        groundTop = tm.getTile(null, Level.imgMap[1][1], FurGuardiansGame.TILE_FLOOR);
        groundTopRightConvex = tm.getTile(null, Level.imgMap[1][2], Tile.BEHAVIOR_SOLID);
        groundLeft = tm.getTile(null, Level.imgMap[2][0], Tile.BEHAVIOR_SOLID);
        groundMiddle = tm.getTile(null, Level.imgMap[2][1], Tile.BEHAVIOR_OPEN);
        groundRight = tm.getTile(null, Level.imgMap[2][2], Tile.BEHAVIOR_SOLID);
        groundTopLeftConcave = tm.getTile(null, Level.imgMap[3][0], Tile.BEHAVIOR_SOLID);
        groundDecorated = tm.getTile(null, Level.imgMap[3][1], Tile.BEHAVIOR_OPEN);
        groundTopRightConcave = tm.getTile(null, Level.imgMap[3][2], Tile.BEHAVIOR_SOLID);
        groundUpSlope = tm.getTile(null, Level.imgMap[3][3], FurGuardiansGame.TILE_UPSLOPE_FLOOR);
        groundDownSlope = tm.getTile(null, Level.imgMap[3][4], FurGuardiansGame.TILE_DOWNSLOPE_FLOOR);
        for (final EditorMode mode : EDITOR_MODES) {
            if (mode instanceof GridMode) {
                final CellType cellType = ((GridMode) mode).getCellType();
                cellTypeMap.put(java.lang.Character.valueOf(cellType.getKey()), cellType);
            }
        }
        cellTypeMap.put(java.lang.Character.valueOf(' '), null);
    }
    
    protected final static void loadUi() {
        if (iconTubeDown != null) {
            return;
        }
        iconTubeDown = newCommon(0, 24, 1f, 1f, 1f);
        iconTubeUp = newCommon(0, 24, 0, false, true, 1f, 1f, 1f);
        iconTubeLeft = newCommon(0, 24, 3, true, false, 1f, 1f, 1f);
        iconTubeRight = newCommon(0, 24, 1, true, false, 1f, 1f, 1f);
        iconRock = newCommon(64f, 16f, 0, false, false, 1f, 1f, 1f);
        selectedTile = FurGuardiansGame.createMenuImg("EditorSelection");
    }
    
    protected final static void goEditor() {
        loadUi();
        Level.initLevel(); // Current level could use block images instead of normal; force to normal or let editor pick (and support block images)
        roomDef = new RoomDefinition();
        roomDef.features.add(new GridDefinition());
        roomDef.build();
        editors.clear();
        Editor first = null;
        for (final PlayerContext pc : FurGuardiansGame.pcs) {
            final Editor editor = new Editor(pc);
            if (first == null) {
                first = editor;
            }
            editors.add(editor);
        }
        first.registerKeyboard();
        first.registerTouch();
        cursor = Menu.PlayerScreen.addCursor(FurGuardiansGame.room);
        clearTouch();
    }
    
    private static void clearTouch() {
        touchStartTileX = touchStartTileY = -1;
    }
    
    protected final static Object getForegroundImage(final Tile tile) {
        return DynamicTileMap.getRawForeground(tile);
    }
    
    protected final static Panmage newCommon(final float subX, final float subY, final float r, final float g, final float b) {
        return newCommon(subX, subY, 0, false, false, r, g, b);
    }
    
    protected final static Panmage newCommon(final float subX, final float subY, final int rot, final boolean mirror, final boolean flip, final float r, final float g, final float b) {
        return new AdjustedPanmage(null, FurGuardiansGame.common, rot, mirror, flip, r, g, b, subX, subY, tileSize);
    }
    
    protected final static void setTile(final int x, final int y, final Object background, final Object foreground, final byte behavior) {
        if (Level.tm.isBad(x, y)) {
            return;
        }
        Level.tm.setTile(x, y, background, foreground, behavior);
    }
    
    protected final static void setTile(final int x, final int y, final Tile tile) {
        final int index = Level.tm.getIndex(x, y);
        if (Level.tm.isBad(index)) {
            return;
        }
        setTile(index, tile);
    }
    
    protected final static void setTile(final int index, final Tile tile) {
        Level.tm.setTile(index, tile);
    }
    
    protected final static void setTileIfEmpty(final int x, final int y, final Tile tile) {
        setTileIf(x, y, tile, null);
    }
    
    protected final static void setTileIf(final int x, final int y, final Tile newTile, final Tile oldTile) {
        final int index = Level.tm.getIndex(x, y);
        if (Level.tm.isBad(index) || (Level.tm.getTile(index) != oldTile)) {
            return;
        }
        setTile(index, newTile);
    }
    
    //TODO GameDefinition link MapDefinitions together at doors/transition points between areas
    //TODO MapDefinition link LevelDefinitions together on world map
    protected final static class LevelDefinition {
        //rooms
    }
    
    protected final static class RoomDefinition {
        // seed(s)
        // bg layers
        protected final List<FeatureDefinition> features = new ArrayList<FeatureDefinition>(); // LinkedList might work better if frequently adding/removing while editing
        
        {
            loadCommonTiles();
        }
        
        protected final void build() {
            for (final FeatureDefinition feature : features) {
                feature.build();
            }
        }
    }
    
    protected abstract static class FeatureDefinition implements Segmented {
        protected int x;
        protected int y;
        protected int w = 1;
        protected int h = 1;
        
        protected int getW() {
            return w;
        }
        
        protected final void setW(final int w) {
            this.w = w;
            this.w = getW();
        }
        
        protected int getH() {
            return h;
        }
        
        protected final void setH(final int h) {
            this.h = h;
            this.h = getH();
        }
        
        protected final boolean contains(final int x, final int y) {
            return (x >= this.x) && (y >= this.y) && (x < (this.x + getW())) && (y < (this.y + getH()));
        }
        
        protected abstract FeatureMode getMode();
        
        protected abstract void build();
        
        protected void destroy() {
            final int w = getW(), h = getH();
            for (int j = 0; j < h; j++) {
                final int yj = y + j;
                for (int i = 0; i < w; i++) {
                    setTile(x + i, yj, null);
                }
            }
        }
        
        @Override
        public void save(final Segment seg) {
            seg.setName("FTR");
            seg.setValue(0, getClass().getSimpleName());
            seg.setInt(1, x);
            seg.setInt(2, y);
            seg.setInt(3, getW());
            seg.setInt(4, getH());
        }
        
        private final static HashMap<String, Constructor<? extends FeatureDefinition>> featureDefinitionTypes = new HashMap<String, Constructor<? extends FeatureDefinition>>();
        
        protected final static FeatureDefinition newFeatureDefinition(final Segment seg) {
            final String className = seg.getValue(0);
            final FeatureDefinition def = Reftil.getDeclaredClassInstance(featureDefinitionTypes, LevelEditor.class, className);
            def.load(seg);
            return def;
        }
        
        protected void load(final Segment seg) {
            x = seg.intValue(1);
            y = seg.intValue(2);
            setW(seg.intValue(3));
            setH(seg.intValue(4));
        }
        
        private final void renderSelection(final Panderer renderer, final Editor editor) {
            final Panlayer layer = editor.getLayer();
            final Panmage image = editor.getSelectedTileImage();
            final int w = getW(), h = getH();
            final float z = FurGuardiansGame.getDepth(FurGuardiansGame.DEPTH_SHATTER);
            for (int j = 0; j < h; j++) {
                final float yj = (y + j) * 16;
                for (int i = 0; i < w; i++) {
                    final float xi = (x + i) * 16;
                    renderer.render(layer, image, xi, yj, z);
                }
            }
        }
    }
    
    protected final static class GridDefinition extends FeatureDefinition {
        private CellType[] cells = new CellType[Level.tm.getWidth() * Level.tm.getHeight()]; //TODO put somewhere else, adapt to room size changes
        
        @Override
        protected final int getW() {
            return Level.tm.getWidth();
        }
        
        @Override
        protected final int getH() {
            return Level.tm.getHeight();
        }
        
        protected final int getIndex(final int x, final int y) {
            return (y * getW()) + x;
        }
        
        protected final CellType get(final int x, final int y) {
            return get(x, y, null);
        }
        
        protected final CellType get(final int x, final int y, final CellType oob) {
            return ((x < 0) || (y < 0) || (x >= getW()) || (y >= getH())) ? oob : cells[getIndex(x, y)];
        }
        
        protected final void set(final int x, final int y, final CellType cellType) {
            set(x, y, 1, 1, cellType);
        }
        
        protected final void set(final int x, final int y, final int w, final int h, final CellType cellType) {
            for (int j = 0; j < h; j++) {
                final int yj = y + j;
                for (int i = 0; i < w; i++) {
                    cells[getIndex(x + i, yj)] = cellType;
                }
            }
            build(x - 1, y - 1, w + 2, h + 2);
        }
        
        protected final void clear(final int x, final int y) {
            final int index = getIndex(x, y);
            cells[index] = null;
            Level.tm.setTile(index, null);
            build(x - 1, y - 1, 3, 3);
        }
        
        protected final void toggle(final int x, final int y, final CellType cellType) {
            if (get(x, y) == cellType) {
                clear(x, y);
            } else {
                set(x, y, cellType);
            }
        }
        
        @Override protected final FeatureMode getMode() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        protected final void build() {
            grid = this;
            final int w = getW(), h = getH();
            build(0, 0, w, h);
        }
        
        private final void build(final int x, final int y, final int w, final int h) {
            final int totalW = getW();
            for (int j = 0; j < h; j++) {
                cy = y + j;
                if (Level.tm.isBadRow(cy)) {
                    continue;
                }
                final int yw = cy * totalW;
                for (int i = 0; i < w; i++) {
                    cx = x + i;
                    if (Level.tm.isBadColumn(cx)) {
                        continue;
                    }
                    currentIndex = yw + cx;
                    final CellType cell = cells[currentIndex];
                    if (cell == null) {
                        //Level.tm.setTile(currentIndex, null); // Could be neighbor occupied by another feature, so don't clear here
                        continue;
                    }
                    cell.build();
                }
            }
        }
        
        @Override
        protected final void destroy() {
            //TODO Only clear TileMap cells covered by this grid's occupied cells, or will this only be needed if clearing the whole room?
        }
        
        @Override
        public void save(final Segment seg) {
            super.save(seg);
            final StringBuilder b = new StringBuilder();
            for (final CellType cell : cells) {
                b.append((cell == null) ? ' ' : cell.getKey());
            }
            seg.setValue(5, b.toString());
        }
        
        @Override
        public void load(final Segment seg) {
            final String m = seg.getValue(5);
            //TODO Replace cells array if size is different
            final int size = m.length();
            for (int i = 0; i < size; i++) {
                cells[i] = cellTypeMap.get(java.lang.Character.valueOf(m.charAt(i)));
            }
        }
    }
    
    private final static void initRandomForCurrentTile() {
        initRandomForTile(currentIndex);
    }
    
    private final static void initRandomForTile(final int tileIndex) {
        currentRandom.setSeed(tileIndex); //TODO Combine with room seed
    }
    
    private final static int MAX_INT_1000 = 2147483; // Integer.MAX_VALUE = 2147483647
    
    private final static int randiCurrent(final int max) {
        if (max < MAX_INT_1000) {
            return Mathtil.randi(currentRandom, 0, ((max + 1) * 1000) - 1) / 1000;
        } else {
            return Mathtil.randi(currentRandom, 0, max);
        }
    }
    
    protected abstract static class CellType {
        protected abstract void build();
        
        protected abstract char getKey();
    }
    
    protected final static GroundType groundType = new GroundType();
    
    protected final static class GroundType extends CellType {
        private final CellType get(final int x, final int y) {
            final CellType type = grid.get(x, y, groundType);
            if ((type == groundUpSlopeType) && isUpSlopeObstructed(x, y)) {
                return groundType;
            } else if ((type == groundDownSlopeType) && isDownSlopeObstructed(x, y)) {
                return groundType;
            }
            return type;
        }
        
        @Override
        protected final void build() {
            final int nx = cx - 1, px = cx + 1, py = cy + 1; // ny = cy - 1
            //final CellType c1 = get(nx, ny);
            //final CellType c2 = get(cx, ny);
            //final CellType c3 = get(px, ny);
            final CellType c4 = get(nx, cy);
            //final CellType c5 = get(cx, cy);
            final CellType c6 = get(px, cy);
            final CellType c7 = get(nx, py);
            final CellType c8 = get(cx, py);
            final CellType c9 = get(px, py);
            final Tile tile;
            if ((c8 != groundType) && (c8 != groundUpSlopeType) && (c8 != groundDownSlopeType)) {
                if ((c4 != groundType) && (c4 != groundUpSlopeType)) {
                    tile = groundTopLeftConvex;
                } else if ((c6 != groundType) && (c6 != groundDownSlopeType)) {
                    tile = groundTopRightConvex;
                } else {
                    tile = groundTop;
                }
            } else if ((c4 != groundType) && (c4 != groundUpSlopeType)) {
                tile = groundLeft;
            } else if ((c6 != groundType) && (c6 != groundDownSlopeType)) {
                tile = groundRight;
            } else if (c7 != groundType) {
                tile = groundTopLeftConcave;
            } else if (c9 != groundType) {
                tile = groundTopRightConcave;
            } else {
                initRandomForCurrentTile();
                tile = Mathtil.rand(currentRandom, 90) ? groundMiddle : groundDecorated;
            }
            //TODO Add art for bottom edge
            Level.tm.setTile(currentIndex, tile);
        }
        
        @Override
        protected final char getKey() {
            return '.';
        }
    }
    
    protected final static GroundUpSlopeType groundUpSlopeType = new GroundUpSlopeType();
    
    protected final static class GroundUpSlopeType extends CellType {
        @Override
        protected final void build() {
            if (isUpSlopeObstructed()) {
                groundType.build();
            } else {
                Level.tm.setTile(currentIndex, groundUpSlope);
            }
        }
        
        @Override
        protected final char getKey() {
            return '/';
        }
    }
    
    protected final static GroundDownSlopeType groundDownSlopeType = new GroundDownSlopeType();
    
    protected final static class GroundDownSlopeType extends CellType {
        @Override
        protected final void build() {
            if (isDownSlopeObstructed()) {
                groundType.build();
            } else {
                Level.tm.setTile(currentIndex, groundDownSlope);
            }
        }
        
        @Override
        protected final char getKey() {
            return '\\';
        }
    }
    
    protected final static BlockType blockType = new BlockType();
    
    protected final static class BlockType extends CellType {
        @Override
        protected final void build() {
            setTile(currentIndex, blockSolid);
        }
        
        @Override
        protected final char getKey() {
            return 'b';
        }
    }
    
    protected final static BlockUpSlopeType blockUpSlopeType = new BlockUpSlopeType();
    
    protected final static class BlockUpSlopeType extends CellType {
        @Override
        protected final void build() {
            final Tile tile;
            if (isUpSlopeObstructed()) {
                tile = blockSolid;
            } else {
                tile = blockUpSlope;
            }
            setTile(currentIndex, tile);
        }
        
        @Override
        protected final char getKey() {
            return 'u';
        }
    }
    
    protected final static boolean isUpSlopeObstructed() {
        return isUpSlopeObstructed(cx, cy);
    }
    
    protected final static boolean isUpSlopeObstructed(final int cx, final int cy) {
        return (grid.get(cx, cy + 1) != null) || (grid.get(cx - 1, cy) != null);
    }
    
    protected final static BlockDownSlopeType blockDownSlopeType = new BlockDownSlopeType();
    
    protected final static class BlockDownSlopeType extends CellType {
        @Override
        protected final void build() {
            final Tile tile;
            if (isDownSlopeObstructed()) {
                tile = blockSolid;
            } else {
                tile = blockDownSlope;
            }
            setTile(currentIndex, tile);
        }
        
        @Override
        protected final char getKey() {
            return 'd';
        }
    }
    
    protected final static boolean isDownSlopeObstructed() {
        return isDownSlopeObstructed(cx, cy);
    }
    
    protected final static boolean isDownSlopeObstructed(final int cx, final int cy) {
        return (grid.get(cx, cy + 1) != null) || (grid.get(cx + 1, cy) != null);
    }
    
    protected final static RockType rockType = new RockType();
    
    protected final static class RockType extends CellType {
        @Override
        protected final void build() {
            initRandomForCurrentTile();
            final int rot = randiCurrent(3);
            final boolean mirror = currentRandom.nextBoolean();
            final float b = Mathtil.randf(currentRandom, 0.35f, 1f); //TODO Control ranges in RoomDefinition
            final float g = Mathtil.randf(currentRandom, 0f, b);
            final float r = Mathtil.randf(currentRandom, 0f, Math.min(0.65f, g));
            Level.tm.setTile(currentIndex, newCommon(64f, 16f, rot, mirror, false, r, g, b), null, FurGuardiansGame.TILE_BREAK);
        }
        
        @Override
        protected final char getKey() {
            return '#';
        }
    }
    
    protected abstract static class ColorfulFeatureDefinition extends FeatureDefinition {
        // inherit from room/level/map/game, define defaults for various types (tube/block/etc.)
        protected float r = -1f;
        protected float g = -1f;
        protected float b = -1f;
        
        protected final Panmage newColorful(final float subX, final float subY) {
            return newCommon(subX, subY, r, g, b);
        }
        
        protected final Panmage newColorful(final float subX, final float subY, final int rot, final boolean mirror, final boolean flip) {
            return newCommon(subX, subY, rot, mirror, flip, r, g, b);
        }
        
        protected final boolean isDefaultColor() {
            return r < 0;
        }
        
        @Override
        protected final void build() {
            boolean defaultColor = isDefaultColor();
            if (defaultColor) {
                r = 0f;
                g = 1f;
                b = 1f; //TODO get default for room/level/etc. for this type
            }
            buildColorful();
            if (defaultColor) {
                r = g = b = -1f;
            }
        }
        
        protected abstract void buildColorful();
        
        @Override
        public void save(final Segment seg) {
            super.save(seg);
            if (isDefaultColor()) {
                return;
            }
            seg.setFloat(5, r);
            seg.setFloat(6, g);
            seg.setFloat(7, b);
        }
        
        @Override
        public void load(final Segment seg) {
            super.load(seg);
            r = seg.getFloat(5, -1);
            g = seg.getFloat(6, -1);
            b = seg.getFloat(7, -1);
        }
    }
    
    protected abstract static class NaturulRiseDefinition extends FeatureDefinition {
        //TODO
    }
    
    protected abstract static class ColorfulRiseDefinition extends ColorfulFeatureDefinition {
        //TODO
    }
    
    protected abstract static class TubeDefinition extends ColorfulFeatureDefinition {
        protected TubeHandler dst = null;
        
        @Override
        protected final void destroy() {
            super.destroy();
            destroyTube();
        }
        
        protected abstract void destroyTube();
    }
    
    protected abstract static class VerticalTubeDefinition extends TubeDefinition {
        {
            h = 2;
        }
        
        @Override
        protected final int getW() {
            return 2;
        }
        
        @Override
        protected final void buildColorful() {
            final TileMap tm = Level.tm;
            final Panmage leftImg = newColorful(0, 32), rightImg = newColorful(16, 32);
            final Tile left = tm.getTile(null, leftImg, Tile.BEHAVIOR_SOLID);
            final Tile right = tm.getTile(null, rightImg, Tile.BEHAVIOR_SOLID);
            final int x1 = x + 1, h1 = h - 1, baseY = getBaseY(), openingY = getOpeningY();
            for (int j = 0; j < h1; j++) {
                final int yj = baseY + j;
                setTile(x, yj, left);
                setTile(x1, yj, right);
            }
            setTile(x, openingY, null, newColorful(0, 16), Tile.BEHAVIOR_SOLID);
            setTile(x1, openingY, null, newColorful(16, 16), Tile.BEHAVIOR_SOLID);
            setTileIfEmpty(x - 1, openingY, edgeLeft);
            setTileIfEmpty(x + 2, openingY, edgeRight);
            getTubeManager().addTube(x, openingY, dst);
        }
        
        @Override
        protected final void destroyTube() {
            final int openingY = getOpeningY();
            setTileIf(x - 1, openingY, null, edgeLeft);
            setTileIf(x + 2, openingY, null, edgeRight);
        }
        
        protected abstract int getBaseY();
        
        protected abstract int getOpeningY();
        
        protected abstract VerticalTubeManager getTubeManager();
    }
    
    protected final static class DownTubeDefinition extends VerticalTubeDefinition {
        @Override protected final int getBaseY() {
            return y;
        }
        @Override protected final int getOpeningY() {
            return y + h - 1;
        }
        @Override protected final VerticalTubeManager getTubeManager() {
            return Character.downTubeManager;
        }
        @Override protected final FeatureMode getMode() {
            return downTubeMode;
        }
    }
    
    protected final static class UpTubeDefinition extends VerticalTubeDefinition {
        @Override protected final int getBaseY() {
            return y + 1;
        }
        @Override protected final int getOpeningY() {
            return y;
        }
        @Override protected final VerticalTubeManager getTubeManager() {
            return Character.upTubeManager;
        }
        @Override protected final FeatureMode getMode() {
            return upTubeMode;
        }
    }
    
    protected abstract static class HorizontalTubeDefinition extends TubeDefinition {
        {
            w = 2;
        }
        
        @Override
        protected final int getH() {
            return 2;
        }
        
        @Override
        protected final void buildColorful() {
            final TileMap tm = Level.tm;
            final Panmage topImg = newColorful(0, 32, 3, false, false), bottomImg = newColorful(16, 32, 3, false, false);
            final Tile top = tm.getTile(null, topImg, Tile.BEHAVIOR_SOLID);
            final Tile bottom = tm.getTile(null, bottomImg, Tile.BEHAVIOR_SOLID);
            final int y1 = y + 1, w1 = w - 1, baseX = getBaseX(), openingX = getOpeningX();
            for (int i = 0; i < w1; i++) {
                final int xi = baseX + i;
                setTile(xi, y, bottom);
                setTile(xi, y1, top);
            }
            setTile(openingX, y, null, newColorful(16, 16, 3, true, false), Tile.BEHAVIOR_SOLID);
            setTile(openingX, y1, null, newColorful(0, 16, 3, true, false), Tile.BEHAVIOR_SOLID);
            setTileIfEmpty(openingX, y - 1, edgeBottom);
            setTileIfEmpty(openingX, y + 2, edgeTop);
            getTubeManager().addTube(openingX, y, dst);
        }
        
        @Override
        protected final void destroyTube() {
            final int openingX = getOpeningX();
            setTileIf(openingX, y - 1, null, edgeBottom);
            setTileIf(openingX, y + 2, null, edgeTop);
        }
        
        protected abstract int getBaseX();
        
        protected abstract int getOpeningX();
        
        protected abstract HorizontalTubeManager getTubeManager();
    }
    
    protected final static class RightTubeDefinition extends HorizontalTubeDefinition {
        @Override protected final int getBaseX() {
            return x + 1;
        }
        @Override protected final int getOpeningX() {
            return x;
        }
        @Override protected final HorizontalTubeManager getTubeManager() {
            return Character.rightTubeManager;
        }
        @Override protected final FeatureMode getMode() {
            return rightTubeMode;
        }
    }
    
    protected final static class LeftTubeDefinition extends HorizontalTubeDefinition {
        @Override protected final int getBaseX() {
            return x;
        }
        @Override protected final int getOpeningX() {
            return x + w - 1;
        }
        @Override protected final HorizontalTubeManager getTubeManager() {
            return Character.leftTubeManager;
        }
        @Override protected final FeatureMode getMode() {
            return leftTubeMode;
        }
    }
    
    protected final static FeatureDefinition pickFeatureDefinition(final int x, final int y, final boolean remove) {
        final List<FeatureDefinition> features = roomDef.features;
        for (int i = features.size() - 1; i >= 0; i--) {
            final FeatureDefinition feature = features.get(i);
            if (feature.contains(x, y) && !(feature instanceof GridDefinition)) {
                if (remove) {
                    features.remove(i);
                }
                return feature;
            }
        }
        return null;
    }
    
    protected final static void deleteFeatureDefinition(final int x, final int y) {
        final FeatureDefinition feature = pickFeatureDefinition(x, y, true);
        if (feature != null) {
            feature.destroy();
        }
    }
    
    protected final static class Editor extends Panctor {
        private EditorMode mode = null;
        private EditorOperation operation = null;
        private CellType cellType = null;
        private FeatureDefinition featureDefinition = null;
        private int x = 0;
        private int y = 0;
        private int rectangleX = -1;
        private int rectangleY = -1;
        private int iconX = -1;
        private int iconY = -1;
        
        protected Editor(final PlayerContext pc) {
            setView(FurGuardiansGame.editorCursor);
            setMode(EDITOR_MODES.get(0));
            FurGuardiansGame.setDepth(this, FurGuardiansGame.DEPTH_SPARK);
            Level.room.addActor(this);
            register(pc.ctrl);
            final Pangine engine = Pangine.getEngine();
            iconX = engine.getEffectiveWidth() / 2 - 16;
            iconY = engine.getEffectiveTop() - 24;
        }
        
        protected final void register(final ControlScheme ctrl) {
            register(ctrl.getLeft(), new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    left();
                }});
            register(ctrl.getRight(), new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    right();
                }});
            register(ctrl.getDown(), new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    down();
                }});
            register(ctrl.getUp(), new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    up();
                }});
            register(ctrl.get1(), new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    on1();
                }});
            // How should touch-screen work? Clicking any cell calls update position and on1?
            final Device device = ctrl.getDevice();
            if (device instanceof Controller) {
                registerController((Controller) device);
            }
        }
        
        private final void registerController(final Controller controller) {
            reg(controller.BUTTON_SHOULDER_LEFT1, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    decMode();
                }});
            reg(controller.BUTTON_SHOULDER_RIGHT1, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    incMode();
                }});
            reg(controller.BUTTON_SHOULDER_LEFT2, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    decOperation();
                }});
            reg(controller.BUTTON_SHOULDER_RIGHT2, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    incOperation();
                }});
        }
        
        private final void registerKeyboard() {
            final Panteraction interaction = Pangine.getEngine().getInteraction();
            register(interaction.KEY_TAB, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    // Alt+Tab can change from game window to another window on the device, so this won't always work
                    if (interaction.isAltActive()) {
                        if (interaction.isShiftActive()) {
                            decOperation();
                        } else {
                            incOperation();
                        }
                    } else {
                        if (interaction.isShiftActive()) {
                            decMode();
                        } else {
                            incMode();
                        }
                    }
                }});
            register(interaction.KEY_GRAVE, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    if (interaction.isShiftActive()) {
                        decOperation();
                    } else {
                        incOperation();
                    }
                }});
            register(interaction.KEY_BRACKET_LEFT, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    if (interaction.isShiftActive()) {
                        decOperation();
                    } else {
                        decMode();
                    }
                }});
            register(interaction.KEY_BRACKET_RIGHT, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    if (interaction.isShiftActive()) {
                        incOperation();
                    } else {
                        incMode();
                    }
                }});
            register(interaction.KEY_MINUS, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    decOperation();
                }});
            register(interaction.KEY_EQUALS, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    incOperation();
                }});
        }
        
        private final void registerTouch() {
            final Pangine engine = Pangine.getEngine();
            final Panteraction interaction = engine.getInteraction();
            final Touch touch = interaction.TOUCH;
            register(touch, new ActionStartListener() {
                @Override public final void onActionStart(final ActionStartEvent event) {
                    touchStartTileX = FurGuardiansGame.getX(cursor, touch) / 16;
                    touchStartTileY = FurGuardiansGame.getY(cursor, touch) / 16;
                }});
            register(touch, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    final int touchX = FurGuardiansGame.getX(cursor, touch);
                    final int touchY = FurGuardiansGame.getY(cursor, touch);
                    final int tileX = touchX / 16, tileY = touchY / 16;
                    final boolean sameTile = (touchStartTileX == tileX) && (touchStartTileY == tileY);
                    clearTouch();
                    if (!sameTile) {
                        return;
                    }
                    for (final Editor editor : editors) {
                        if (editor.handleTouch(touchX, touchY)) {
                            return;
                        }
                    }
                    //TODO process touch
                }});
        }
        
        private final void reg(final Panput input, final ActionEndListener listener) {
            if (input == null) {
                return;
            }
            register(input, listener);
        }
        
        private final void left() {
            if (x > 0) {
                x--;
                updatePosition();
            }
        }
        
        private final void right() {
            if (x < (grid.getW() - 1)) {
                x++;
                updatePosition();
            }
        }
        
        private final void down() {
            if (y > 0) {
                y--;
                updatePosition();
            }
        }
        
        private final void up() {
            if (y < (grid.getH() - 1)) {
                y++;
                updatePosition();
            }
        }
        
        private final void updatePosition() {
            getPosition().set(x * 16, y * 16);
            destroyFeature();
            operation.onEditorMove(this);
            buildFeature();
        }
        
        private final void on1() {
            destroyFeature();
            operation.on1(Editor.this);
            buildFeature();
        }
        
        private final void buildFeature() {
            if (featureDefinition != null) {
                featureDefinition.build();
            }
        }
        
        private final void destroyFeature() {
            if (featureDefinition != null) {
                featureDefinition.destroy();
            }
        }
        
        private final void setMode(final EditorMode mode) {
            this.mode = mode;
            if ((featureDefinition != null) && !featureDefinition.getMode().equals(mode)) {
                featureDefinition = null;
            }
            final List<EditorOperation> operations = mode.getOperations();
            final boolean newOperationRequiredWithoutFeature = (featureDefinition == null) && (operation != null) && operation.isPickedFeatureRequired();
            final boolean operationNotSupportedForNewMode = !operations.contains(operation);
            if (newOperationRequiredWithoutFeature || operationNotSupportedForNewMode) {
                setOperation(operations.get(0)); // Also clears context
            } else {
                clearContext();
            }
            mode.onSelectMode(this);
        }
        
        private final void incMode() {
            setMode(Coltil.getNext(EDITOR_MODES, mode));
        }
        
        private final void decMode() {
            setMode(Coltil.getPrevious(EDITOR_MODES, mode));
        }
        
        private final void setOperation(final EditorOperation operation) {
            this.operation = operation;
            clearContext();
        }
        
        private final void incOperation() {
            setOperation(Coltil.getNext(mode.getOperations(), operation));
        }
        
        private final void decOperation() {
            setOperation(Coltil.getPrevious(mode.getOperations(), operation));
        }
        
        private final void setCellType(final CellType cellType) {
            this.cellType = cellType;
        }
        
        /*private final void incCellType() {
            setCellType(Coltil.getNext(CELL_TYPES, cellType));
        }*/
        
        private final void clearContext() {
            rectangleX = rectangleY = -1;
        }
        
        private final void startRectangle() {
            rectangleX = x;
            rectangleY = y;
        }
        
        private final int getRectangleX() {
            return Math.min(rectangleX, x);
        }
        
        private final int getRectangleY() {
            return Math.min(rectangleY, y);
        }
        
        private final int getRectangleW() {
            return Math.abs(x - rectangleX) + 1;
        }
        
        private final int getRectangleH() {
            return Math.abs(y - rectangleY) + 1;
        }
        
        protected final Panmage getSelectedTileImage() {
            return selectedTile;
        }
        
        protected final boolean handleTouch(final int touchX, final int touchY) {
            if ((touchY < iconY) || (touchY >= (iconY + 16))) {
                return false;
            } else if (touchX < iconX) {
                return false;
            } else if (touchX < (iconX + 16)) {
                incMode();
                return true;
            } else if (touchX < (iconX + 32)) {
                incOperation();
                return true;
            }
            return false;
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            super.renderView(renderer);
            if (featureDefinition != null) {
                featureDefinition.renderSelection(renderer, this);
            }
            final Panlayer layer = getLayer();
            final float cursorZ = getPosition().getZ();
            final float iconZ = cursorZ - 1;
            Level.tm.render(renderer, layer, mode.getIcon(), iconX, iconY, iconZ);
            Level.tm.render(renderer, layer, operation.getIcon(), iconX + 16, iconY, iconZ);
        }
    }
    
    protected final static DownTubeMode downTubeMode = new DownTubeMode();
    protected final static UpTubeMode upTubeMode = new UpTubeMode();
    protected final static RightTubeMode rightTubeMode = new RightTubeMode();
    protected final static LeftTubeMode leftTubeMode = new LeftTubeMode();
    
    protected final static List<EditorMode> EDITOR_MODES = Arrays.asList(
            new GroundMode(), new GroundUpSlopeMode(), new GroundDownSlopeMode(),
            new BlockMode(), new BlockUpSlopeMode(), new BlockDownSlopeMode(),
            new RockMode(),
            downTubeMode, upTubeMode, rightTubeMode, leftTubeMode);
    
    protected abstract static class EditorMode {
        protected abstract List<EditorOperation> getOperations();
        
        protected void onSelectMode(final Editor editor) {
        }
        
        protected abstract Object getIcon();
    }
    
    protected abstract static class GridMode extends EditorMode {
        protected final static List<EditorOperation> GRID_OPERATIONS = Arrays.asList(
                new AddCellOperation(), new RectangleCellOperation());
        
        @Override
        protected List<EditorOperation> getOperations() {
            return GRID_OPERATIONS;
        }
        
        @Override
        protected final void onSelectMode(final Editor editor) {
            editor.setCellType(getCellType());
        }
        
        protected abstract CellType getCellType();
    }
    
    protected final static class GroundMode extends GridMode {
        @Override
        protected final CellType getCellType() {
            return groundType;
        }
        
        @Override
        protected final Object getIcon() {
            return getForegroundImage(groundTop);
        }
    }
    
    protected final static class GroundUpSlopeMode extends GridMode {
        @Override
        protected final CellType getCellType() {
            return groundUpSlopeType;
        }
        
        @Override
        protected final Object getIcon() {
            return getForegroundImage(groundUpSlope);
        }
    }
    
    protected final static class GroundDownSlopeMode extends GridMode {
        @Override
        protected final CellType getCellType() {
            return groundDownSlopeType;
        }
        
        @Override
        protected final Object getIcon() {
            return getForegroundImage(groundDownSlope);
        }
    }
    
    protected final static class BlockMode extends GridMode {
        @Override
        protected final CellType getCellType() {
            return blockType;
        }
        
        @Override
        protected final Object getIcon() {
            return getForegroundImage(blockSolid);
        }
    }
    
    protected final static class BlockUpSlopeMode extends GridMode {
        @Override
        protected final CellType getCellType() {
            return blockUpSlopeType;
        }
        
        @Override
        protected final Object getIcon() {
            return getForegroundImage(blockUpSlope);
        }
    }
    
    protected final static class BlockDownSlopeMode extends GridMode {
        @Override
        protected final CellType getCellType() {
            return blockDownSlopeType;
        }
        
        @Override
        protected final Object getIcon() {
            return getForegroundImage(blockDownSlope);
        }
    }
    
    protected final static class RockMode extends GridMode {
        @Override
        protected final CellType getCellType() {
            return rockType;
        }
        
        @Override
        protected final Panmage getIcon() {
            return iconRock;
        }
    }
    
    protected abstract static class FeatureMode extends EditorMode {
        protected final static List<EditorOperation> FEATURE_OPERATIONS = Arrays.asList(
                new AddFeatureOperation(), new PickFeatureOperation(), new MoveFeatureOperation(), new ResizeFeatureOperation(), new DeleteFeatureOperation());
        
        @Override
        protected List<EditorOperation> getOperations() {
            return FEATURE_OPERATIONS;
        }
        
        protected abstract FeatureDefinition newFeature();
    }
    
    protected abstract static class ColorfulFeatureMode extends FeatureMode {
        protected final static List<EditorOperation> COLORFUL_OPERATIONS = new ArrayList<EditorOperation>(FEATURE_OPERATIONS);
        
        static {
            COLORFUL_OPERATIONS.add(new RecolorFeatureOperation());
        }
        
        @Override
        protected List<EditorOperation> getOperations() {
            return COLORFUL_OPERATIONS;
        }
    }
    
    protected abstract static class TubeMode extends ColorfulFeatureMode {
        // getOperations() adds destination operation, maybe opens window in onSelectMode
    }
    
    protected final static class DownTubeMode extends TubeMode {
        @Override
        protected final FeatureDefinition newFeature() {
            return new DownTubeDefinition();
        }
        
        @Override
        protected final Panmage getIcon() {
            return iconTubeDown;
        }
    }
    
    protected final static class UpTubeMode extends TubeMode {
        @Override
        protected final FeatureDefinition newFeature() {
            return new UpTubeDefinition();
        }
        
        @Override
        protected final Panmage getIcon() {
            return iconTubeUp;
        }
    }
    
    protected final static class RightTubeMode extends TubeMode {
        @Override
        protected final FeatureDefinition newFeature() {
            return new RightTubeDefinition();
        }
        
        @Override
        protected final Panmage getIcon() {
            return iconTubeRight;
        }
    }
    
    protected final static class LeftTubeMode extends TubeMode {
        @Override
        protected final FeatureDefinition newFeature() {
            return new LeftTubeDefinition();
        }
        
        @Override
        protected final Panmage getIcon() {
            return iconTubeLeft;
        }
    }
    
    protected abstract static class EditorOperation {
        protected boolean isPickedFeatureRequired() {
            return false;
        }
        
        protected void onEditorMove(final Editor editor) {
        }
        
        protected void on1(final Editor editor) {
        }
        
        protected abstract Panmage getIcon();
    }
    
    protected final static class AddFeatureOperation extends EditorOperation {
        @Override
        protected final void on1(final Editor editor) {
            final FeatureDefinition featureDefinition = ((FeatureMode) editor.mode).newFeature();
            featureDefinition.x = editor.x;
            featureDefinition.y = editor.y;
            roomDef.features.add(featureDefinition);
            editor.featureDefinition = featureDefinition;
        }
        
        @Override
        protected final Panmage getIcon() {
            return FurGuardiansGame.menuPlus;
        }
    }
    
    protected final static class PickFeatureOperation extends EditorOperation {
        @Override
        protected final void on1(final Editor editor) {
            final FeatureDefinition picked = pickFeatureDefinition(editor.x, editor.y, false);
            if (picked != null) {
                editor.featureDefinition = picked;
                editor.setMode(picked.getMode());
            }
        }
        
        @Override
        protected final Panmage getIcon() {
            return FurGuardiansGame.menuQuestion;
        }
    }
    
    protected final static class MoveFeatureOperation extends EditorOperation {
        @Override
        protected final boolean isPickedFeatureRequired() {
            return true;
        }
        
        @Override
        protected final void onEditorMove(final Editor editor) {
            editor.featureDefinition.x = editor.x;
            editor.featureDefinition.y = editor.y;
        }
        
        @Override
        protected final Panmage getIcon() {
            return FurGuardiansGame.menuCursor;
        }
    }
    
    protected final static class ResizeFeatureOperation extends EditorOperation {
        @Override
        protected final boolean isPickedFeatureRequired() {
            return true;
        }
        
        @Override
        protected final void onEditorMove(final Editor editor) {
            final FeatureDefinition featureDefinition = editor.featureDefinition;
            featureDefinition.setW(Math.max(1, editor.x - featureDefinition.x + 1));
            featureDefinition.setH(Math.max(1, editor.y - featureDefinition.y + 1));
        }
        
        @Override
        protected final Panmage getIcon() {
            return FurGuardiansGame.menuGraph;
        }
    }
    
    protected final static class DeleteFeatureOperation extends EditorOperation {
        @Override
        protected final void on1(final Editor editor) {
            deleteFeatureDefinition(editor.x, editor.y);
        }
        
        @Override
        protected final Panmage getIcon() {
            return FurGuardiansGame.menuMinus;
        }
    }
    
    protected final static class RecolorFeatureOperation extends EditorOperation {
        //TODO
        @Override
        protected final boolean isPickedFeatureRequired() {
            return true;
        }
        
        @Override
        protected final Panmage getIcon() {
            return FurGuardiansGame.menuRgb;
        }
    }
    
    /*protected final static class PickCellTypeOperation extends EditorOperation {
        @Override
        protected final void on1(final Editor editor) {
            editor.incCellType();
        }
        
        @Override
        protected final Panmage getIcon() {
            return FurGuardiansGame.menuQuestion;
        }
    }*/
    
    protected final static class AddCellOperation extends EditorOperation {
        @Override
        protected final void on1(final Editor editor) {
            grid.toggle(editor.x, editor.y, editor.cellType);
        }
        
        @Override
        protected final Panmage getIcon() {
            return FurGuardiansGame.menuPlus;
        }
    }
    
    protected final static class RectangleCellOperation extends EditorOperation {
        @Override
        protected final void on1(final Editor editor) {
            if (editor.rectangleX < 0) {
                editor.startRectangle();
            } else {
                grid.set(editor.getRectangleX(), editor.getRectangleY(), editor.getRectangleW(), editor.getRectangleH(), editor.cellType);
            }
        }
        
        @Override
        protected final Panmage getIcon() {
            return FurGuardiansGame.menuGraph;
        }
    }
}
