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

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.furguardians.Character.*;
import org.pandcorps.furguardians.Player.*;
import org.pandcorps.pandam.*;
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
    private final static Random currentRandom = new Random();
    private static RoomDefinition roomDef;
    private static GridDefinition grid;
    private static int cx, cy, currentIndex;
    
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
    }
    
    protected final static void goEditor() {
        Level.initLevel();
        roomDef = new RoomDefinition();
        roomDef.features.add(new GridDefinition());
        roomDef.build();
        for (final PlayerContext pc : FurGuardiansGame.pcs) {
            new Editor(pc);
        }
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
    
    protected abstract static class FeatureDefinition {
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
        
        protected void save() { } //TODO
        
        protected void load() { }
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
            return cells[getIndex(x, y)];
        }
        
        protected final CellType get(final int x, final int y, final CellType oob) {
            return ((x < 0) || (y < 0) || (x >= getW()) || (y >= getH())) ? oob : get(x, y);
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
    }
    
    protected final static GroundType groundType = new GroundType();
    
    protected final static class GroundType extends CellType {
        @Override
        protected final void build() {
            final int nx = cx - 1, px = cx + 1, py = cy + 1; // ny = cy - 1
            //final CellType c1 = grid.get(nx, ny, groundType);
            //final CellType c2 = grid.get(cx, ny, groundType);
            //final CellType c3 = grid.get(px, ny, groundType);
            final CellType c4 = grid.get(nx, cy, groundType);
            //final CellType c5 = grid.get(cx, cy, groundType);
            final CellType c6 = grid.get(px, cy, groundType);
            final CellType c7 = grid.get(nx, py, groundType);
            final CellType c8 = grid.get(cx, py, groundType);
            final CellType c9 = grid.get(px, py, groundType);
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
    }
    
    protected final static GroundUpSlopeType groundUpSlopeType = new GroundUpSlopeType();
    
    protected final static class GroundUpSlopeType extends CellType {
        @Override
        protected final void build() {
            //TODO
        }
    }
    
    protected final static GroundDownSlopeType groundDownSlopeType = new GroundDownSlopeType();
    
    protected final static class GroundDownSlopeType extends CellType {
        @Override
        protected final void build() {
            //TODO
        }
    }
    
    protected final static class BlockType extends CellType {
        @Override
        protected final void build() {
            setTile(currentIndex, blockSolid);
        }
    }
    
    protected final static class BlockUpSlopeType extends CellType {
        @Override
        protected final void build() {
            final Tile tile;
            if ((grid.get(cx, cy + 1) != null) || (grid.get(cx - 1, cy) != null)) {
                tile = blockSolid;
            } else {
                tile = blockUpSlope;
            }
            setTile(currentIndex, tile);
        }
    }
    
    protected final static class BlockDownSlopeType extends CellType {
        @Override
        protected final void build() {
            final Tile tile;
            if ((grid.get(cx, cy + 1) != null) || (grid.get(cx + 1, cy) != null)) {
                tile = blockSolid;
            } else {
                tile = blockDownSlope;
            }
            setTile(currentIndex, tile);
        }
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
    }
    
    protected final static List<CellType> CELL_TYPES = Arrays.asList(
            groundType, groundUpSlopeType, groundDownSlopeType,
            new BlockType(), new BlockUpSlopeType(), new BlockDownSlopeType(),
            rockType);
    
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
        
        @Override
        protected final void build() {
            boolean defaultColor = r < 0;
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
        
        protected Editor(final PlayerContext pc) {
            setView(FurGuardiansGame.editorCursor);
            setMode(EDITOR_MODES.get(0));
            cellType = CELL_TYPES.get(0);
            FurGuardiansGame.setDepth(this, FurGuardiansGame.DEPTH_SPARK);
            Level.room.addActor(this);
            register(pc.ctrl);
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
            //TODO Keep current operation if it's not null and available for new mode?
            setOperation(mode.getOperations().get(0)); // Also clears context
        }
        
        private final void setOperation(final EditorOperation operation) {
            this.operation = operation;
            clearContext();
        }
        
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
    }
    
    protected final static List<EditorMode> EDITOR_MODES = Arrays.asList(
            new GridMode(),
            new DownTubeMode(), new UpTubeMode(), new LeftTubeMode(), new RightTubeMode());
    
    protected abstract static class EditorMode {
        protected abstract List<EditorOperation> getOperations();
    }
    
    protected final static class GridMode extends EditorMode {
        protected final static List<EditorOperation> GRID_OPERATIONS = Arrays.asList(
                new AddCellOperation());
        
        @Override
        protected List<EditorOperation> getOperations() {
            return GRID_OPERATIONS;
        }
    }
    
    protected abstract static class FeatureMode extends EditorMode {
        protected final static List<EditorOperation> FEATURE_OPERATIONS = Arrays.asList(
                new AddFeatureOperation(), new MoveFeatureOperation(), new ResizeFeatureOperation());
        
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
        // getOperations() adds destination operation, maybe opens window as soon as selected, new featuremode method for onSelectMode
    }
    
    protected final static class DownTubeMode extends TubeMode {
        @Override
        protected final FeatureDefinition newFeature() {
            return new DownTubeDefinition();
        }
    }
    
    protected final static class UpTubeMode extends TubeMode {
        @Override
        protected final FeatureDefinition newFeature() {
            return new UpTubeDefinition();
        }
    }
    
    protected final static class RightTubeMode extends TubeMode {
        @Override
        protected final FeatureDefinition newFeature() {
            return new RightTubeDefinition();
        }
    }
    
    protected final static class LeftTubeMode extends TubeMode {
        @Override
        protected final FeatureDefinition newFeature() {
            return new LeftTubeDefinition();
        }
    }
    
    protected abstract static class EditorOperation {
        protected void onEditorMove(final Editor editor) {
        }
        
        protected void on1(final Editor editor) {
        }
    }
    
    protected final static class AddFeatureOperation extends EditorOperation {
        @Override
        protected final void on1(final Editor editor) {
            final FeatureDefinition featureDefinition = ((FeatureMode) editor.mode).newFeature();
            featureDefinition.x = editor.x;
            featureDefinition.y = editor.y;
            roomDef.features.add(featureDefinition);
            editor.featureDefinition = featureDefinition;
            editor.setOperation(null); //TODO
        }
    }
    
    protected final static class MoveFeatureOperation extends EditorOperation {
        @Override
        protected final void onEditorMove(final Editor editor) {
            editor.featureDefinition.x = editor.x;
            editor.featureDefinition.y = editor.y;
        }
    }
    
    protected final static class ResizeFeatureOperation extends EditorOperation {
        @Override
        protected final void onEditorMove(final Editor editor) {
            final FeatureDefinition featureDefinition = editor.featureDefinition;
            featureDefinition.setW(Math.max(1, editor.x - featureDefinition.x + 1));
            featureDefinition.setH(Math.max(1, editor.y - featureDefinition.y + 1));
        }
    }
    
    // Delete feature operation, select feature operation
    
    protected final static class RecolorFeatureOperation extends EditorOperation {
        //TODO
    }
    
    protected final static class AddCellOperation extends EditorOperation {
        @Override
        protected final void on1(final Editor editor) {
            grid.set(editor.x, editor.y, editor.cellType);
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
    }
}
