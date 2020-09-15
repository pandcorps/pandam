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
package org.pandcorps.board;

import java.io.*;
import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.game.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandax.in.*;

public class BoardGame extends BaseGame {
    protected final static String TITLE = "Board Games";
    protected final static String VERSION = "0.0.1";
    protected final static String YEAR = "2020";
    protected final static String AUTHOR = "Andrew M. Martin";
    
    protected final static String RES = "org/pandcorps/board/";
    
    protected final static int DIM = 16;
    protected final static int TITLE_COLUMNS = 24;
    protected final static int TITLE_ROWS = 14;
    protected final static int TITLE_W = TITLE_COLUMNS * DIM; // 384
    protected final static int TITLE_H = TITLE_ROWS * DIM; // 224;
    
    protected final static int DEPTH_CELL = 0;
    protected final static int DEPTH_PIECE = 2;
    protected final static int DEPTH_CURSOR = 4;
    
    protected final static Pancolor BLACK = new FinPancolor(64);
    
    protected final static String SEG_STATE = "BGS";
    
    protected final static String LOC_AUTOSAVE = "autosave.txt";
    
    protected final static int MAX_HISTORY_SIZE = 5;
    
    protected static Queue<Runnable> loaders = new LinkedList<Runnable>();
    protected static Panmage imgCursor = null;
    protected static Panmage square = null;
    protected static Panmage circle = null;
    protected static Panmage circles = null;
    
    protected final static BoardGamePlayer[] players = { new BoardGamePlayer(0), new BoardGamePlayer(1) };
    protected static BoardGameModule<? extends BoardGamePiece> module = null;
    protected static Panroom room = null;
    protected static Cursor cursor = null;
    protected static Pancolor highlightColor = null;
    protected final static Set<Integer> highlightSquares = new HashSet<Integer>();
    protected static int currentPlayerIndex = 0;
    
    @Override
    protected final boolean isFullScreen() {
        return false;
    }
    
    @Override
    protected final int getGameWidth() {
        return TITLE_W; // Used on title screen; individual games can set their own resolution
    }
    
    @Override
    protected final int getGameHeight() {
        return TITLE_H;
    }
    
    @Override
    protected final void init(final Panroom room) throws Exception {
        final Pangine engine = Pangine.getEngine();
        engine.setTitle(TITLE);
        engine.setEntityMapEnabled(false);
        Imtil.onlyResources = true;
        if (loaders != null) {
            loaders.add(new Runnable() {
                @Override public final void run() {
                    loadResources();
                }});
        }
        BoardGame.room = room;
        module = new CheckersModule();
        Panscreen.set(new LogoScreen(BoardGameScreen.class, loaders));
    }
    
    private final static void loadResources() {
        final Pangine engine = Pangine.getEngine();
        if (isCursorNeeded()) {
            imgCursor = engine.createImage(PRE_IMG + "cursor", RES + "Cursor.png");
        }
        square = engine.createImage(PRE_IMG + "square", RES + "Square.png");
        circle = engine.createImage(PRE_IMG + "circle", RES + "Circle.png");
        circles = engine.createImage(PRE_IMG + "circles", RES + "Circles.png");
    }
    
    protected final static boolean isCursorNeeded() {
        final Pangine engine = Pangine.getEngine();
        return engine.isMouseSupported() && engine.isFullScreen();
    }
    
    private final static void addCursor() {
        if (!isCursorNeeded()) {
            final Pangine engine = Pangine.getEngine();
            if (engine.isMouseSupported()) {
                engine.setMouseTouchEnabled(true);
            }
            return;
        }
        cursor = Cursor.addCursorIfNeeded(room, imgCursor);
        cursor.getPosition().setZ(DEPTH_CURSOR);
    }
    
    private final static Pancolor pickHighlightColor() {
        final Pancolor color0 = players[0].getColor();
        final Pancolor color1 = players[1].getColor();
        final boolean dark0 = isDark(color0);
        final boolean dark1 = isDark(color1);
        if (dark0 && dark1) {
            return Pancolor.WHITE;
        } else if (!(dark0 || dark1)) {
            return BLACK;
        }
        return new FinPancolor((color0.getR() + color1.getR()) / 2, (color0.getG() + color1.getG()) / 2, (color0.getB() + color1.getB()) / 2);
    }
    
    private final static boolean isDark(final Pancolor color) {
        return isDark(color.getR()) && isDark(color.getG()) && isDark(color.getB());
    }
    
    private final static boolean isDark(final short channel) {
        return channel < 128;
    }
    
    private static int touchStartIndex = -1;
    
    protected final static class BoardGameScreen extends Panscreen {
        @Override
        protected final void load() {
            final Pangine engine = Pangine.getEngine();
            engine.enableColorArray();
            engine.zoomToMinimum(module.numVerticalCells * DIM);
            module.prepare();
            addCursor();
            highlightColor = pickHighlightColor();
            module.clear();
            module.initGame();
            module.onLoad();
            addState();
        }
    }
    
    protected final static boolean isHighlight(final int cellIndex) {
        return highlightSquares.contains(Integer.valueOf(cellIndex));
    }
    
    protected final static void setHighlightSquares(final Set<Integer> highlightSquares) {
        BoardGame.highlightSquares.clear();
        BoardGame.highlightSquares.addAll(highlightSquares);
    }
    
    protected final static void toggleCurrentPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.length;
        addState();
    }
    
    protected final static void addState() {
        module.getGrid().addState();
    }
    
    protected final static void save() {
        save(LOC_AUTOSAVE);
    }
    
    protected final static void save(final String loc) {
        final Writer w = Iotil.getWriter(loc);
        try {
            module.save(w);
        } catch (final IOException e) {
            throw new Panception(e);
        } finally {
            Iotil.close(w);
        }
    }
    
    protected abstract static class BoardGameModule<P extends BoardGamePiece> {
        protected final int numVerticalCells;
        
        protected BoardGameModule(final int numVerticalCells) {
            this.numVerticalCells = numVerticalCells;
        }
        
        protected final void prepare() {
            final BoardGameGrid<P> grid = getGrid();
            room.addActor(grid);
            final Touch touch = Pangine.getEngine().getInteraction().TOUCH;
            grid.register(touch, new ActionStartListener() {
                @Override public final void onActionStart(final ActionStartEvent event) {
                    touchStartIndex = grid.getIndex(touch);
                }});
            grid.register(touch, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    final int touchEndIndex = grid.getIndex(touch);
                    if ((touchEndIndex == touchStartIndex) && grid.isValid(touchEndIndex)) {
                        module.processTouch(touchEndIndex);
                    } else {
                        touchStartIndex = -1;
                    }
                }});
        }
        
        protected abstract void initGame();
        
        protected abstract BoardGameGrid<P> getGrid();
        
        protected abstract BoardGameCell getCell(final int x, final int y);
        
        protected abstract Pancolor getDefaultColor(final int playerIndex);
        
        protected abstract void processTouch(final int cellIndex);
        
        protected abstract void onLoad();
        
        protected abstract char serialize(final P piece);
        
        protected abstract P parse(final char value, final int player);
        
        public final void save(final Writer w) throws IOException {
            final BoardGameGrid<P> grid = getGrid();
            final Segment seg = new Segment();
            //TODO currentPlayerIndex, currentStateIndex? players? module? others? EOF to detect partial/corrupted files? call this from somewhere
            for (final BoardGameState<P> state: grid.states) {
                seg.clear();
                seg.setName(SEG_STATE);
                seg.setValue(0, Integer.toString(currentPlayerIndex));
                for (final P piece : state.grid) {
                    final Field field = new Field();
                    field.setInt(0, piece.player);
                    field.setInt(1, piece.x);
                    field.setInt(2, piece.y);
                    field.setChar(3, serialize(piece));
                    seg.addField(1, field);
                }
                seg.saveln(w);
            }
        }
        
        public final void load(final String loc) throws IOException {
            final SegmentStream in = SegmentStream.openLocation(loc);
            try {
                load(in);
            } finally {
                in.close();
            }
            Iotil.delete(loc);
        }
        
        public final void load(final SegmentStream in) throws IOException {
            clear();
            Segment seg;
            while ((seg = in.readIf(SEG_STATE)) != null) {
                final BoardGameGrid<P> grid = getGrid();
                final List<BoardGameState<P>> states = grid.states;
                states.clear();
                final int currentPlayerIndex = seg.intValue(0);
                final List<Field> fields = seg.getRepetitions(1);
                final List<P> pieces = new ArrayList<P>(fields.size());
                for (final Field field : fields) {
                    final int player = field.intValue(0);
                    final int x = field.intValue(1);
                    final int y = field.intValue(2);
                    final char pieceType = field.charValue(3);
                    final P piece = parse(pieceType, player);
                    piece.x = x; piece.y = y;
                    pieces.add(piece);
                }
                states.add(new BoardGameState<P>(pieces, currentPlayerIndex));
            }
            onLoad();
        }
        
        public final void clear() {
            getGrid().clear();
        }
    }
    
    protected final static int convertScreenToGrid(final int screenVal) {
        return screenVal / DIM;
    }
    
    protected static class BoardGameGrid<P extends BoardGamePiece> extends Panctor {
        private final int w;
        private final int h;
        private final int numCells;
        private final List<P> grid;
        private final Map<Integer, BoardGamePieceList<P>> lists = new HashMap<Integer, BoardGamePieceList<P>>();
        private final List<BoardGameState<P>> states = new ArrayList<BoardGameState<P>>();
        private int currentStateIndex = 0;
        
        protected BoardGameGrid(final int dim) {
            this(dim, dim);
        }
        
        protected BoardGameGrid(final int w, final int h) {
            this.w = w;
            this.h = h;
            numCells = w * h;
            grid = new ArrayList<P>(numCells);
            for (int i = 0; i < numCells; i++) {
                grid.add(null);
            }
        }
        
        protected final void clear() {
            for (int i = 0; i < numCells; i++) {
                grid.set(i, null);
            }
            states.clear();
            currentStateIndex = 0;
        }
        
        protected final int getWidth() {
            return w;
        }
        
        protected final int getHeight() {
            return h;
        }
        
        protected final List<P> getPieces(final int player) {
            final Integer key = Integer.valueOf(player);
            BoardGamePieceList<P> list = lists.get(key);
            if (list == null) {
                list = new BoardGamePieceList<P>();
                lists.put(key, list);
            }
            // Maybe track grid age in set method and BoardGamePieceList; if not stale, return externalList without rebuilding; might call this multiple times per turn
            final List<P> internal = list.internalList;
            internal.clear();
            for (final P piece : grid) {
                if ((piece != null) && (piece.player == player)) {
                    internal.add(piece);
                }
            }
            return list.externalList;
        }
        
        protected final P get(final int x, final int y) {
            final int index = getIndexOptional(x, y);
            return get(index);
        }
        
        protected final P get(final int index) {
            return isValid(index) ? grid.get(index) : null;
        }
        
        protected final void set(final int x, final int y, final P piece) {
            // Null out previous location here? Separate move method to do that?
            grid.set(getIndexRequired(x, y), piece);
            if (piece != null) {
                unset(piece);
                piece.x = x;
                piece.y = y;
            }
        }
        
        protected final void set(final int index, final P piece) {
            grid.set(index, piece);
            if (piece != null) {
                unset(piece);
                piece.x = getX(index);
                piece.y = getY(index);
            }
        }
        
        private final void unset(final P piece) {
            final int index = getIndexOptional(piece.x, piece.y);
            if (isValid(index)) {
                grid.set(index, null);
            }
        }
        
        protected final void remove(final int x, final int y) {
            grid.set(getIndexRequired(x, y), null);
        }
        
        /*protected final void set(final BoardGameCell cell, final P piece) {
            set(cell.x, cell.y, piece);
        }*/
        
        protected final boolean isOpen(final int x, final int y) {
            return isValid(x, y) && (get(x, y) == null);
        }
        
        protected final boolean isOpen(final int index) {
            return grid.get(index) == null;
        }
        
        /*protected final boolean isOpen(final BoardGameCell cell) {
            return isOpen(cell.x, cell.y);
        }*/
        
        protected final boolean isValid(final int x, final int y) {
            return (x >= 0) && (x < w) && (y >= 0) && (y < h);
        }
        
        protected final boolean isValid(final int index) {
            return (index >= 0) && (index < numCells);
        }
        
        protected final int getIndexRequired(final int x, final int y) {
            final int index = getIndexOptional(x, y);
            if (!isValid(index)) {
                throw new IllegalArgumentException("Invalid position: " + x + ", " + y);
            }
            return index;
        }
        
        protected final int getIndexOptional(final int x, final int y) {
            if (!isValid(x, y)) {
                return -1;
            }
            return (y * w) + x;
        }
        
        protected final int getIndex(final Touch touch) {
            return getIndexOptional(convertScreenToGrid(touch.getX()), convertScreenToGrid(touch.getY()));
        }
        
        protected final Integer getIndexWrapped(final int x, final int y) {
            return Integer.valueOf(getIndexRequired(x, y));
        }
        
        protected final int getX(final int index) {
            return index % w;
        }
        
        protected final int getY(final int index) {
            return index / w;
        }
        
        protected final void addState() {
            // if undoing and playing a new move, clear what was lost from the undo
            for (int i = states.size() - 1; i > currentStateIndex; i--) {
                states.remove(i);
            }
            while (states.size() >= MAX_HISTORY_SIZE) {
                states.remove(0);
            }
            currentStateIndex = states.size(); // Before adding new state
            states.add(new BoardGameState<P>(this));
            save();
        }
        
        protected final void setState(final int newStateIndex) {
            final BoardGameState<P> state = states.get(newStateIndex);
            BoardGame.currentPlayerIndex = state.currentPlayerIndex;
            grid.clear();
            grid.addAll(state.grid);
            currentStateIndex = newStateIndex;
            module.onLoad();
        }
        
        protected final void load(final SegmentStream in) {
            
        }
        
        protected final boolean isUndoAllowed() {
            return currentStateIndex > 0;
        }
        
        protected final void undo() {
            if (isUndoAllowed()) {
                setState(currentStateIndex - 1);
            }
        }
        
        protected final boolean isRedoAllowed() {
            return currentStateIndex < (states.size() - 1);
        }
        
        protected final void redo() {
            if (isRedoAllowed()) {
                setState(currentStateIndex + 1);
            }
        }
        
        protected final void renderView(final Panderer renderer) {
            for (int y = 0; y < h; y++) {
                final int yd = y * DIM;
                for (int x = 0; x < w; x++) {
                    final int xd = x * DIM;
                    final BoardGameCell cell = module.getCell(x, y);
                    final Pancolor color = cell.getColor();
                    render(renderer, cell.getImage(), xd, yd, DEPTH_CELL, color);
                    final P piece = get(x, y);
                    if (piece != null) {
                        render(renderer, piece.getImage(), xd, yd, DEPTH_PIECE, players[piece.player].getColor());
                    }
                }
            }
        }
        
        protected final void render(final Panderer renderer, final Panmage image, final int x, final int y, final int z, final Pancolor color) {
            renderer.render(getLayer(), image, x, y, z, 0, 0, DIM, DIM, 0, false, false, color.getRf(), color.getGf(), color.getBf());
        }
    }
    
    // Just use Grid.getIndex instead
    /*protected final static class BoardGameCell {
        protected final int x;
        protected final int y;
        
        protected BoardGameCell(final int x, final int y) {
            this.x = x;
            this.y = y;
        }
        
        public final boolean equals(final Object o) {
            if (o == this) {
                return true;
            } else if (o == null) {
                return false;
            } else if (o.getClass() != BoardGameCell.class) {
                return false;
            }
            final BoardGameCell cell = (BoardGameCell) o;
            return (x == cell.x) && (y == cell.y); 
        }
    }*/
    
    protected static interface BoardGameCell {
        public Panmage getImage();
        
        public Pancolor getColor();
    }
    
    protected final static BoardGameCell square0 = new BoardGameCell() {
        @Override public final Panmage getImage() { return square; }
        @Override public final Pancolor getColor() { return players[0].getColor(); }};
    protected final static BoardGameCell square1 = new BoardGameCell() {
        @Override public final Panmage getImage() { return square; }
        @Override public final Pancolor getColor() { return players[1].getColor(); }};
    protected final static BoardGameCell squareH = new BoardGameCell() {
        @Override public final Panmage getImage() { return square; }
        @Override public final Pancolor getColor() { return highlightColor; }};
    protected final static BoardGameCell getPlayerSquare(final int x, final int y) {
        final int index = module.getGrid().getIndexOptional(x, y);
        if ((index >= 0) && highlightSquares.contains(Integer.valueOf(index))) {
            return squareH;
        }
        return (x % 2) == (y % 2) ? square0 : square1;
    }
    
    protected abstract static class BoardGamePiece {
        protected int player;
        protected int x = -1;
        protected int y = -1;
        
        protected BoardGamePiece(final int player) {
            this.player = player;
        }
        
        protected final Integer getIndexWrapped() {
            return module.getGrid().getIndexWrapped(x, y);
        }
        
        protected abstract Panmage getImage();
    }
    
    private final static class BoardGamePieceList<P extends BoardGamePiece> {
        private final List<P> internalList = new ArrayList<P>();
        private final List<P> externalList = Collections.unmodifiableList(internalList);
    }
    
    protected final static class BoardGamePlayer {
        protected final int index;
        private Pancolor color1 = null; // Null means to use the default for each game
        private Pancolor color2 = null; // If player 2's preferred color matches player 1, then use color 2 instead
        
        private BoardGamePlayer(final int index) {
            this.index = index;
        }
        
        protected final Pancolor getColor() {
            final Pancolor firstChoice = getColor(color1);
            if (index == 0) {
                return firstChoice;
            }
            final Pancolor color0 = getColor0();
            return firstChoice.equals(color0) ? getColor(color2) : firstChoice;
        }
        
        private final Pancolor getColor(final Pancolor color) {
            if (color != null) {
                return color;
            }
            final Pancolor defaultColor = module.getDefaultColor(index);
            if (index == 0) {
                return defaultColor;
            }
            final Pancolor color0 = getColor0();
            return defaultColor.equals(color0) ? module.getDefaultColor(0) : defaultColor;
        }
        
        private final static Pancolor getColor0() {
            return players[0].getColor();
        }
    }
    
    protected final static class BoardGameState<P extends BoardGamePiece> {
        private final int currentPlayerIndex;
        private final List<P> grid;
        
        protected BoardGameState(final BoardGameGrid<P> grid) {
            this(new ArrayList<P>(grid.grid), BoardGame.currentPlayerIndex);
        }
        
        protected BoardGameState(final List<P> grid, final int currentPlayerIndex) {
            this.currentPlayerIndex = currentPlayerIndex;
            this.grid = grid;
        }
    }
    
    public final static void main(final String[] args) {
        try {
            new BoardGame().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
