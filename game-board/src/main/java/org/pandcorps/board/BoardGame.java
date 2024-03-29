/*
Copyright (c) 2009-2023, Andrew M. Martin
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

import org.pandcorps.board.Menu.*;
import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.game.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Fonts.*;

public class BoardGame extends BaseGame {
    protected final static String TITLE = "2-Player Games For 1 Device";
    protected final static String VERSION = "1.0.3";
    protected final static String YEAR = "2023";
    protected final static String AUTHOR = "Andrew M. Martin";
    protected final static String COPYRIGHT = "Copyright " + Pantext.CHAR_COPYRIGHT + " " + YEAR;
    
    protected final static String RES = "org/pandcorps/board/";
    
    protected final static int DIM = 16;
    protected final static int TITLE_COLUMNS = 14;
    protected final static int TITLE_ROWS = 8;
    protected final static int TITLE_W = TITLE_COLUMNS * DIM; // 224
    protected final static int TITLE_H = TITLE_ROWS * DIM; // 128;
    
    protected final static int DEPTH_CELL = 0;
    protected final static int DEPTH_PREVIOUS_TURN = 2;
    protected final static int DEPTH_PIECE = 4;
    protected final static int DEPTH_CURSOR = 6;
    
    protected final static int TEXT_OFF_TOP = 13;
    protected final static int TEXT_LINE_HEIGHT = 9;
    
    protected final static int TURN_DISPLAY_DISTANCE = DIM / 2;
    
    protected final static Pancolor BLACK = Pancolor.DARK_GREY;
    protected final static Pancolor ORANGE = new FinPancolor(Pancolor.MAX_VALUE, 128, 0);
    protected final static Pancolor HIGHLIGHT_DEFAULT = Pancolor.CYAN;
    
    protected final static String SEG_CONTEXT = "CTX";
    protected final static String SEG_STATE = "BGS";
    protected final static String SEG_PROFILE = "PRF";
    protected final static String SEG_END_OF_FILE = "EOF";
    
    protected final static String EXT_SAVE = ".txt";
    protected final static String LOC_SUFFIX_AUTOSAVE = "_autosave" + EXT_SAVE;
    
    protected final static int MAX_HISTORY_SIZE = 5;
    
    protected final static int NULL_INDEX = -1;
    
    protected final static int NUM_PLAYERS = 2;
    
    private static int nextMenuIndex = Integer.MAX_VALUE;
    
    protected final static int RESULT_NULL = -1;
    protected final static int RESULT_WIN = 0;
    protected final static int RESULT_TIE = 1;
    
    protected final static CheckersModule CHECKERS = new CheckersModule();
    protected final static OthelloModule OTHELLO = new OthelloModule();
    protected final static FourInARowModule FOUR_IN_A_ROW = new FourInARowModule();
    protected final static ChessModule CHESS = new ChessModule();
    
    protected static Queue<Runnable> loaders = new LinkedList<Runnable>();
    protected static Panmage imgCursor = null;
    protected static Panmage imgUndo = null;
    protected static Panmage imgRedo = null;
    protected static Panmage imgPlus = null;
    protected static Panmage imgMenu = null;
    protected static Panmage imgEdit = null;
    protected static Panmage imgOpen = null;
    protected static Panmage imgSave = null;
    protected static Panmage imgX = null;
    protected static Panmage imgDelete = null;
    protected static Panmage imgDone = null;
    protected static Panmage imgExit = null;
    protected static Panmage square = null;
    protected static Panmage squareBlack = null;
    protected static Panmage imgPreviousTurn = null;
    protected static Panmage circle = null;
    protected static Panmage circles = null;
    protected static Panmage verticalSquare = null;
    protected static Panmage verticalCircle = null;
    protected static Panmage pawn = null;
    protected static Panmage rook = null;
    protected static Panmage knight = null;
    protected static Panmage bishop = null;
    protected static Panmage queen = null;
    protected static Panmage king = null;
    protected static Font font = null;
    protected static List<MenuButton> menuButtons = null;
    protected static int numStandardMenuButtons = 0;
    
    protected final static List<BoardGameProfile> profiles = new ArrayList<BoardGameProfile>();
    protected static BoardGameModule<? extends BoardGamePiece> module = null;
    protected static Panroom room = null;
    protected final static StringBuilder label = new StringBuilder();
    protected final static StringBuilder label2 = new StringBuilder();
    protected static Cursor cursor = null;
    protected static Pancolor highlightColor = null;
    protected final static Set<Integer> highlightSquares = new HashSet<Integer>();
    
    @Override
    protected final boolean isFullScreen() {
        return true;
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
        Panscreen.set(new LogoScreen(ModuleScreen.class, loaders));
    }
    
    private final static void loadResources() {
        final Pangine engine = Pangine.getEngine();
        if (isCursorNeeded()) {
            imgCursor = engine.createImage(PRE_IMG + "cursor", new FinPanple2(0, 15), null, null, RES + "Cursor.png");
        }
        imgUndo = engine.createImage(PRE_IMG + "undo", RES + "Undo.png");
        imgRedo = new AdjustedPanmage(PRE_IMG + "redo", imgUndo, 0, true, false);
        imgPlus = engine.createImage(PRE_IMG + "plus", RES + "Plus.png");
        imgMenu = engine.createImage(PRE_IMG + "menu", RES + "Menu.png");
        imgEdit = engine.createImage(PRE_IMG + "edit", RES + "Pencil.png");
        imgOpen = engine.createImage(PRE_IMG + "open", RES + "Open.png");
        imgSave = engine.createImage(PRE_IMG + "save", RES + "Save.png");
        imgX = engine.createImage(PRE_IMG + "delete", RES + "Delete.png");
        imgDelete = new AdjustedPanmage(Pantil.vmid(), imgX, Pancolor.RED);
        imgDone = engine.createImage(PRE_IMG + "done", RES + "Check.png");
        imgExit = engine.createImage(PRE_IMG + "exit", RES + "Exit.png");
        square = engine.createImage(PRE_IMG + "square", RES + "Square.png");
        squareBlack = Menu.getSquare(BLACK);
        imgPreviousTurn = engine.createImage(PRE_IMG + "previous.turn", RES + "PreviousTurn.png");
        circle = engine.createImage(PRE_IMG + "circle", RES + "Circle.png");
        circles = engine.createImage(PRE_IMG + "circles", RES + "Circles.png");
        verticalCircle = engine.createImage(PRE_IMG + "vertical.circle", RES + "VerticalCircle.png");
        verticalSquare = engine.createImage(PRE_IMG + "vertical.square", RES + "VerticalSquare.png");
        pawn = engine.createImage(PRE_IMG + "pawn", RES + "Pawn.png");
        rook = engine.createImage(PRE_IMG + "rook", RES + "Rook.png");
        knight = engine.createImage(PRE_IMG + "knight", RES + "Knight.png");
        bishop = engine.createImage(PRE_IMG + "bishop", RES + "Bishop.png");
        queen = engine.createImage(PRE_IMG + "queen", RES + "Queen.png");
        king = engine.createImage(PRE_IMG + "king", RES + "King.png");
        font = Fonts.getClassic(new FontRequest(FontType.Upper, 8), Pancolor.WHITE, Pancolor.WHITE, Pancolor.WHITE, null, Pancolor.BLACK);
        initMenuButtons();
        loadProfiles();
    }
    
    private final static void initMenuButtons() {
        menuButtons = new ArrayList<MenuButton>(8);
        menuButtons.addAll(Arrays.asList(
                new MenuButton(9, 1, imgUndo) {
                    @Override
                    protected final boolean isActive() {
                        return module.getGrid().isUndoAllowed();
                    }
                    @Override
                    protected final void onTouch() {
                        module.getGrid().undo();
                    }
                },
                new MenuButton(11, 1, imgRedo) {
                    @Override
                    protected final boolean isActive() {
                        return module.getGrid().isRedoAllowed();
                    }
                    @Override
                    protected final void onTouch() {
                        module.getGrid().redo();
                    }
                },
                new MenuButton(9, 3, imgPlus) {
                    @Override
                    protected final void onTouch() {
                        module.getGrid().detach();
                        Menu.goPrompt(
                                "New game?",
                                new ActionEndListener() {
                                    @Override public final void onActionEnd(final ActionEndEvent event) {
                                        module.startNewGame();
                                        goGame();
                                    }},
                                new ActionEndListener() {
                                    @Override public final void onActionEnd(final ActionEndEvent event) {
                                        goGame();
                                    }});
                    }
                },
                new MenuButton(11, 3, imgMenu) {
                    @Override
                    protected final void onTouch() {
                        goMenu();
                    }
                }
        ));
        numStandardMenuButtons = menuButtons.size();
    }
    
    protected final static void addExtraMenuButton(final MenuButton menuButton) {
        menuButtons.add(menuButton);
    }
    
    protected final static void clearExtraMenuButtons() {
        int size = menuButtons.size();
        while (size > numStandardMenuButtons) {
            size--;
            menuButtons.remove(size);
        }
    }
    
    protected final static boolean isExtraMenuButtonAvailable() {
        return menuButtons.size() > numStandardMenuButtons;
    }
    
    private final static void loadProfiles() {
        for (int profileIndex = 0; true; profileIndex++) {
            if (Iotil.exists(BoardGameProfile.getProfileFileName(profileIndex))) {
                final BoardGameProfile profile = new BoardGameProfile();
                try {
                    profile.load(profileIndex);
                } catch (final Exception e) {
                    profile.delete();
                }
                profiles.add(profile);
            } else {
                break;
            }
        }
        reactivateMinimumProfiles();
        createInitialProfiles();
    }
    
    private final static void reactivateMinimumProfiles() {
        int activeSize = getActiveProfilesSize();
        if (activeSize >= NUM_PLAYERS) {
            return;
        }
        final int numProfiles = profiles.size();
        for (int i = 0; i < numProfiles; i++) {
            final BoardGameProfile profile = profiles.get(i);
            if (profile.deleted) {
                profile.init(i);
                activeSize++;
                if (activeSize >= NUM_PLAYERS) {
                    return;
                }
            }
        }
    }
    
    private final static void createInitialProfiles() {
        for (int profileIndex = profiles.size(); profileIndex < NUM_PLAYERS; profileIndex++) {
            final BoardGameProfile profile = new BoardGameProfile();
            profile.init(profileIndex);
            profiles.add(profile);
        }
    }
    
    protected final static int getActiveProfilesSize() {
        int size = 0;
        for (final BoardGameProfile profile : profiles) {
            if (!profile.deleted) {
                size++;
            }
        }
        return size;
    }
    
    protected final static BoardGameProfile getActiveProfile(final int index) {
        int i = 0;
        for (final BoardGameProfile profile : profiles) {
            if (!profile.deleted) {
                if (i == index) {
                    return profile;
                }
                i++;
            }
        }
        throw new IllegalStateException("Could not find active profile " + index);
    }
    
    protected final static BoardGameProfile newProfile() {
        for (final BoardGameProfile profile : profiles) {
            if (profile.deleted) {
                profile.init(profile.profileIndex);
                return profile;
            }
        }
        final int newIndex = profiles.size();
        final BoardGameProfile profile = new BoardGameProfile();
        profile.init(newIndex);
        profiles.add(profile);
        return profile;
    }
    
    private final static void initPlayerProfiles() {
        final int numPlayers = module.numPlayers;
        final BoardGamePlayer[] players = module.players;
        for (int i = 0; i < numPlayers; i++) {
            final BoardGamePlayer player = players[i];
            if (player.profile == null) {
                player.profile = getActiveProfile(i);
            }
        }
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
    
    protected final static Pancolor pickNonPlayerColor() {
        final BoardGamePlayer[] players = module.players;
        final Pancolor color0 = players[0].getColor();
        final Pancolor color1 = players[1].getColor();
        if (isAnyDark(color0) && isAnyDark(color1)) {
            return Pancolor.WHITE;
        } else if (!(isAllDark(color0) || isAllDark(color1))) {
            return BLACK;
        }
        return HIGHLIGHT_DEFAULT;
    }
    
    private final static boolean isAnyDark(final Pancolor color) {
        return isDark(color.getR()) || isDark(color.getG()) || isDark(color.getB());
    }
    
    private final static boolean isAllDark(final Pancolor color) {
        return isDark(color.getR()) && isDark(color.getG()) && isDark(color.getB());
    }
    
    private final static boolean isDark(final short channel) {
        return channel <= 128;
    }
    
    private static int touchStartIndex = NULL_INDEX;
    
    protected final static void goGame() {
        Panscreen.set(new BoardGameScreen());
    }
    
    protected final static void reload() {
        module.getGrid().reload();
    }
    
    protected final static void registerCapture(final Panctor actor) {
        final Pangine engine = Pangine.getEngine();
        final Panteraction interaction = engine.getInteraction();
        actor.register(interaction.KEY_F1, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { engine.captureScreen(); }});
    }
    
    protected static class BaseScreen extends Panscreen {
        @Override
        protected final void load() {
            final Pangine engine = Pangine.getEngine();
            engine.setBgColor(Pancolor.GREY);
            engine.enableColorArray();
            addCursor();
            afterBaseLoad();
        }
        
        protected void afterBaseLoad() {
        }
    }
    
    protected final static class BoardGameScreen extends BaseScreen {
        @Override
        protected final void afterBaseLoad() {
            module.prepare();
            final int top = Pangine.getEngine().getEffectiveHeight() - TEXT_OFF_TOP;
            addText(label, top);
            addText(label2, top - TEXT_LINE_HEIGHT);
            final BoardGameGrid<?> grid = module.getGrid();
            if (Coltil.isValued(grid.grid)) {
                module.resumeGame();
            } else {
                loadGame();
            }
            module.pickColors(); // Must be done after loading game (which picks player profiles)
            registerCapture(grid);
        }
        
        protected final void loadGame() {
            final String locAutosave = getLocationAutosave();
            boolean newGame = true;
            if (Iotil.exists(locAutosave)) {
                try {
                    module.load(locAutosave);
                    newGame = false;
                } catch (final Exception e) {
                    // Just start a new game if the auto-save file is corrupted
                }
            }
            if (newGame) {
                initPlayerProfiles();
                module.startNewGame();
            }
        }
        
        protected final static Pantext addText(final CharSequence label, final int y) {
            return addText(label, (module.getGrid().w * DIM) + 8, y);
        }
        
        protected final static Pantext addText(final CharSequence label, final int x, final int y) {
            final Pantext text = new Pantext(Pantil.vmid(), font, label);
            text.getPosition().set(x, y);
            room.addActor(text);
            return text;
        }
        
        protected final static Pantext addTextCentered(final CharSequence label, final int x, final int y) {
            final Pantext text = addText(label, x, y);
            text.centerX();
            return text;
        }
        
        @Override
        protected final void step() {
            Chartil.clear(label);
            Chartil.clear(label2);
            final BoardGamePlayer[] players = module.players;
            final BoardGameResult result = module.result;
            if (result == null) {
                label.append(players[module.currentPlayerIndex].profile.name);
                label2.append("Turn");
            } else {
                if (result.resultStatus == RESULT_TIE) {
                    label.append(module.getTieLabel());
                } else {
                    label.append(players[result.playerIndex].profile.name);
                    label2.append("Wins");
                }
            }
            module.step();
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
        module.currentPlayerIndex = getNextPlayerIndex();
        module.turnTaken = true;
        if (module.isReverseRequired()) {
            module.getGrid().reverse();
        }
    }
    
    protected final static int getNextPlayerIndex() {
        return getNextPlayerIndex(module.currentPlayerIndex);
    }
    
    protected final static int getNextPlayerIndex(final int currentPlayerIndex) {
        return (currentPlayerIndex + 1) % module.players.length;
    }
    
    protected final static void addState() {
        module.getGrid().addState();
    }
    
    protected final static String getLocationAutosave() {
        return module.getName() + LOC_SUFFIX_AUTOSAVE;
    }
    
    protected final static void autosave() {
        save(getLocationAutosave());
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
    
    protected final static SegmentStream openSegmentStream(final String loc) throws IOException {
        validateSegmentStream(loc);
        return SegmentStream.openLocation(loc);
    }
    
    protected final static void validateSegmentStream(final String loc) throws IOException {
        final SegmentStream in = SegmentStream.openLocation(loc);
        try {
            while (in.readUnless(SEG_END_OF_FILE) != null);
            validateEndOfFile(in);
        } finally {
            Iotil.close(in);
        }
    }
    
    protected final static Field toField(final Pancolor color) {
        if (color == null) {
            return null;
        }
        final Field f = new Field();
        f.setShort(0, color.getR());
        f.setShort(1, color.getG());
        f.setShort(2, color.getB());
        return f;
    }
    
    protected final static Pancolor toColor(final Field f) {
        return (f == null) ? null : new Pancolor(f.shortValue(0), f.shortValue(1), f.shortValue(2));
    }
    
    protected final static void writeEndOfFile(final Writer w, final Segment seg) throws IOException {
        seg.clear();
        seg.setName(SEG_END_OF_FILE);
        seg.saveln(w);
    }
    
    protected final static void validateEndOfFile(final SegmentStream in) throws IOException {
        in.readRequire(SEG_END_OF_FILE);
    }
    
    protected final static boolean processTouchMenu(final int index) {
        for (final MenuButton menuButton : menuButtons) {
            if (menuButton.index == index) {
                menuButton.onTouch();
                return true;
            }
        }
        return false;
    }
    
    protected final static void goMenu() {
        module.getGrid().detach();
        Menu.goMenu();
    }
    
    protected abstract static class BoardGameModule<P extends BoardGamePiece> {
        protected final BoardGamePlayer[] players = new BoardGamePlayer[NUM_PLAYERS];
        protected final int numPlayers = players.length;
        protected int currentPlayerIndex = 0;
        protected P pieceToMove = null;
        protected List<Integer> pieceToMoveTurnIndices = null;
        protected P lastCapturedPiece = null;
        protected boolean turnTaken = false;
        protected BoardGameResult result = null;
        
        protected BoardGameModule() {
            for (int i = 0; i < numPlayers; i++) {
                players[i] = new BoardGamePlayer(i);
            }
        }
        
        protected final void prepare() {
            final BoardGameGrid<P> grid = getGrid();
            grid.module = this;
            room.addActor(grid);
            final Pangine engine = Pangine.getEngine();
            final Panteraction interaction = engine.getInteraction();
            final Touch touch = interaction.TOUCH;
            grid.register(touch, new ActionStartListener() {
                @Override public final void onActionStart(final ActionStartEvent event) {
                    touchStartIndex = grid.getIndex(touch);
                }});
            grid.register(touch, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    final int touchEndIndex = grid.getIndex(touch);
                    if (touchEndIndex == touchStartIndex) {
                        final boolean handled = processTouchMenu(touchEndIndex);
                        if (!handled && grid.isValid(touchEndIndex) && (module.result == null)) {
                            result = processTouch(touchEndIndex);
                        }
                        if (module.turnTaken) {
                            addState();
                            module.turnTaken = false;
                        }
                    }
                    touchStartIndex = NULL_INDEX;
                }});
            final ActionEndListener undoListener = new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    grid.undo();
                }};
            grid.register(interaction.KEY_U, undoListener);
            grid.register(interaction.KEY_Z, undoListener);
            final ActionEndListener redoListener = new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    grid.redo();
                }};
            grid.register(interaction.KEY_R, redoListener);
            grid.register(interaction.KEY_Y, redoListener);
            Menu.addBackListener(grid, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    goMenu();
                }});
            prepareGame();
        }
        
        protected void pickColors() {
            highlightColor = pickNonPlayerColor();
        }
        
        protected final String getName() {
            final String className = getClass().getSimpleName();
            return className.substring(0, className.length() - 6); // Remove "Module" from class name
        }
        
        //@OverrideMe
        protected void prepareGame() {
        }
        
        //@OverrideMe
        protected void step() {
        }
        
        protected abstract void initGame();
        
        protected abstract BoardGameGrid<P> getGrid();
        
        protected abstract BoardGameCell getCell(final int x, final int y);
        
        protected abstract Pancolor getDefaultColor(final int playerIndex);
        
        protected abstract BoardGameResult processTouch(final int cellIndex);
        
        protected abstract void onLoad();
        
        protected abstract char serialize(final P piece);
        
        protected abstract P parse(final char pieceType, final int player);
        
        protected final BoardGameResult processTouchToMove(final int cellIndex) {
            if (pieceToMove == null) {
                pickPieceToMove(cellIndex);
                return null;
            } else if (BoardGame.isHighlight(cellIndex)) {
                pickDestination(cellIndex);
                return BoardGame.highlightSquares.isEmpty() ? getFinalResult() : null;
            } else { // Illegal destination picked for pieceToMove
                /*
                Sometimes multiple moves are allowed in a single turn, like double-jumping in Checkers.
                So don't just cancel move.  Reload state to revert any previous moves for the turn.
                */
                reload();
                return null;
            }
        }
        
        protected final void pickPieceToMove(final int cellIndex) {
            if (!BoardGame.isHighlight(cellIndex)) {
                return;
            }
            pieceToMove = getGrid().get(cellIndex);
            if (pieceToMoveTurnIndices == null) {
                pieceToMoveTurnIndices = new ArrayList<>();
            } else {
                pieceToMoveTurnIndices.clear();
            }
            lastCapturedPiece = null;
            addTurnIndex(cellIndex);
            highlightAllowedDestinations();
        }
        
        protected final void addTurnIndex(final int cellIndex) {
            pieceToMoveTurnIndices.add(Integer.valueOf(cellIndex));
        }
        
        //@OverrideMe
        protected void highlightAllowedDestinations() {
        }
        
        //@OverrideMe
        protected void pickDestination(final int cellIndex) {
        }
        
        //@OverrideMe
        protected BoardGameResult getFinalResult() {
            return null;
        }
        
        protected boolean isReverseRequired() {
            return false;
        }
        
        protected String getTieLabel() {
            return "Tie Game";
        }
        
        private P parse(final char pieceType, final int player, final int x, final int y) {
            final P piece = parse(pieceType, player);
            piece.x = x; piece.y = y;
            return piece;
        }
        
        protected final P copy(final P piece) {
            return (piece == null) ? null : parse(serialize(piece), piece.player, piece.x, piece.y);
        }
        
        protected final List<P> copy(final List<P> pieces) {
            final List<P> copied = new ArrayList<P>(pieces.size());
            for (final P piece : pieces) {
                copied.add(copy(piece));
            }
            return copied;
        }
        
        public final void save(final Writer w) throws IOException {
            final BoardGameGrid<P> grid = getGrid();
            final Segment seg = new Segment();
            seg.setName(SEG_CONTEXT);
            seg.setInt(0, grid.currentStateIndex); // Don't need currentPlayerIndex/result; stored for each state below
            for (final BoardGamePlayer player : players) {
                seg.addInt(1, player.profile.profileIndex);
            }
            seg.saveln(w);
            for (final BoardGameState<P> state: grid.states) {
                seg.clear();
                seg.setName(SEG_STATE);
                seg.setValue(0, Integer.toString(state.playerIndex));
                for (final P piece : state.grid) {
                    if (piece == null) {
                        continue;
                    }
                    seg.addField(1, toField(piece));
                }
                final BoardGameResult result = state.result;
                if (result != null) {
                    seg.setInt(2, result.resultStatus);
                    seg.setInt(3, result.playerIndex);
                }
                final int numTurnIndices = Coltil.size(state.previousTurnIndices);
                for (int i = 0; i < numTurnIndices; i++) {
                    final Field field = new Field();
                    field.setInt(0, state.previousTurnIndices.get(i));
                    seg.addField(4, field);
                }
                if (state.lastCapturedPiece != null) {
                    seg.setField(5, toField(state.lastCapturedPiece));
                }
                seg.saveln(w);
            }
            writeEndOfFile(w, seg);
        }
        
        private final Field toField(final P piece) {
            final Field field = new Field();
            field.setInt(0, piece.player);
            field.setInt(1, piece.x);
            field.setInt(2, piece.y);
            field.setChar(3, serialize(piece));
            return field;
        }
        
        public final void load(final String loc) throws IOException {
            final SegmentStream in = openSegmentStream(loc);
            try {
                load(in);
            } finally {
                in.close();
            }
        }
        
        public final BoardGameContext parseGameContext(final SegmentStream in) throws IOException {
            final Segment seg = in.readRequire(SEG_CONTEXT);
            final int currentStateIndex = seg.intValue(0);
            final List<Field> playerFields = seg.getRepetitions(1);
            final int numPlayerFields = playerFields.size();
            final int[] profileIndices = new int[numPlayerFields];
            for (int playerIndex = 0; playerIndex < numPlayerFields; playerIndex++) {
                profileIndices[playerIndex] = playerFields.get(playerIndex).intValue();
            }
            return new BoardGameContext(currentStateIndex, profileIndices);
        }
        
        public final void load(final SegmentStream in) throws IOException {
            clear();
            Segment seg;
            final BoardGameContext context = parseGameContext(in);
            final int[] profileIndices = context.profileIndices;
            final int numPlayers = profileIndices.length;
            for (int playerIndex = 0; playerIndex < numPlayers; playerIndex++) {
                loadProfile(playerIndex, profileIndices[playerIndex]);
            }
            final BoardGameGrid<P> grid = getGrid();
            final List<BoardGameState<P>> states = grid.states;
            while ((seg = in.readIf(SEG_STATE)) != null) {
                final int playerIndex = seg.intValue(0);
                final List<Field> fields = seg.getRepetitions(1);
                final List<P> pieces = new ArrayList<P>(fields.size());
                for (final Field field : fields) {
                    final P piece = toPiece(field);
                    Coltil.set(pieces, grid.getIndexRequired(piece.x, piece.y), piece);
                }
                final int resultStatus = seg.getInt(2, RESULT_NULL);
                final BoardGameResult result;
                if (resultStatus == RESULT_NULL) {
                    result = null;
                } else {
                    result = new BoardGameResult(resultStatus, seg.intValue(3));
                }
                final List<Field> turnIndexFields = seg.getRepetitions(4);
                final int numTurnIndices = Coltil.size(turnIndexFields);
                List<Integer> turnIndices = null;
                if (numTurnIndices > 0) {
                    turnIndices = new ArrayList<Integer>(numTurnIndices);
                    for (final Field turnIndexField : turnIndexFields) {
                        turnIndices.add(turnIndexField.getInteger());
                    }
                }
                final Field lastCapturedPieceField = seg.getField(5);
                final P lastCapturedPiece = (lastCapturedPieceField == null) ? null : toPiece(lastCapturedPieceField);
                states.add(new BoardGameState<P>(pieces, playerIndex, turnIndices, lastCapturedPiece, result));
            }
            validateEndOfFile(in);
            getGrid().setState(context.currentStateIndex);
        }
        
        private final P toPiece(final Field field) {
            final int player = field.intValue(0);
            final int x = field.intValue(1);
            final int y = field.intValue(2);
            final char pieceType = field.charValue(3);
            return parse(pieceType, player, x, y);
        }
        
        protected final void loadProfile(final int playerIndex, final int profileIndex) throws IOException {
            final BoardGamePlayer player = players[playerIndex];
            if (player.getProfileIndex() != profileIndex) {
                boolean loadNeeded = true;
                for (int otherPlayerIndex = 0; otherPlayerIndex < numPlayers; otherPlayerIndex++) {
                    if (otherPlayerIndex == playerIndex) {
                        continue;
                    }
                    final BoardGamePlayer otherPlayer = players[otherPlayerIndex];
                    if (otherPlayer.getProfileIndex() == profileIndex) {
                        final BoardGameProfile otherProfile = otherPlayer.profile;
                        otherPlayer.profile = player.profile;
                        player.profile = otherProfile;
                        loadNeeded = false;
                        break;
                    }
                }
                if (loadNeeded) {
                    final BoardGameProfile profile = profiles.get(profileIndex);
                    if (profile.deleted) {
                        throw new IOException("Deleted Profile");
                    }
                    player.profile = profile;
                }
            }
        }
        
        public final void startNewGame() {
            clear();
            initGame();
            onLoad();
            addState();
        }
        
        public final void resumeGame() {
            room.addActor(getGrid());
            onLoad();
        }
        
        public final void clear() {
            currentPlayerIndex = 0;
            result = null;
            getGrid().clear();
            Coltil.clear(pieceToMoveTurnIndices);
            lastCapturedPiece = null;
        }
        
        protected final boolean isEachPlayerDefaultColor() {
            for (final BoardGamePlayer player : players) {
                if (player.profile.color1 != null) {
                    return false;
                }
            }
            return true;
        }
        
        //@OverrideMe
        protected void renderView(final Panderer renderer) {
        }
    }
    
    protected final static int convertScreenToGrid(final int screenVal) {
        return screenVal / DIM;
    }
    
    protected static class BoardGameGrid<P extends BoardGamePiece> extends Panctor {
        private final int w;
        private final int h;
        private final int numCells;
        protected final List<P> grid;
        private final Map<Integer, BoardGamePieceList<P>> lists = new HashMap<Integer, BoardGamePieceList<P>>();
        private final List<BoardGameState<P>> states = new ArrayList<BoardGameState<P>>();
        private int currentStateIndex = 0;
        private BoardGameModule<P> module = null;
        
        protected BoardGameGrid(final int dim) {
            this(dim, dim);
        }
        
        protected BoardGameGrid(final int w, final int h) {
            this.w = w;
            this.h = h;
            numCells = w * h;
            grid = new ArrayList<P>(numCells);
        }
        
        protected final void clear() {
            grid.clear();
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
            return isValid(index) ? Coltil.get(grid, index) : null;
        }
        
        private final P set(final int index, final int x, final int y, final P piece) {
            final P old = Coltil.get(grid, index);
            Coltil.set(grid, index, piece);
            if ((piece != null) && ((piece.x != x) || (piece.y != y))) { // Can detach and reattach in same place; don't want to unset below then
                unset(piece);
                piece.x = x;
                piece.y = y;
            }
            return old;
        }
        
        protected final P set(final int x, final int y, final P piece) {
            return set(getIndexRequired(x, y), x, y, piece);
        }
        
        protected final P set(final int index, final P piece) {
            return set(index, getX(index), getY(index), piece);
        }
        
        protected final void set(final List<P> pieces) {
            grid.clear();
            for (final P piece : pieces) {
                if (piece != null) {
                    Coltil.set(grid, getIndexRequired(piece.x, piece.y), module.copy(piece));
                }
            }
        }
        
        private final void unset(final P piece) {
            final int index = getIndexOptional(piece.x, piece.y);
            if (get(index) == piece) { // Make sure piece wasn't detached; otherwise another piece might be in its old cell
                Coltil.set(grid, index, null);
            }
        }
        
        protected final P remove(final int x, final int y) {
            final int index = getIndexRequired(x, y);
            final P piece = Coltil.get(grid, index);
            Coltil.set(grid, index, null);
            return piece;
        }
        
        /*protected final void set(final BoardGameCell cell, final P piece) {
            set(cell.x, cell.y, piece);
        }*/
        
        protected final boolean isOpen(final int x, final int y) {
            return isValid(x, y) && (get(x, y) == null);
        }
        
        protected final boolean isOpen(final int index) {
            return Coltil.get(grid, index) == null;
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
                return NULL_INDEX;
            }
            return (y * w) + x;
        }
        
        protected final int getIndex(final Touch touch) {
            final int tx = BoardGame.getX(cursor, touch), ty = BoardGame.getY(cursor, touch);
            final int x = convertScreenToGrid(tx), y = convertScreenToGrid(ty);
            final int index = getIndexOptional(x, y);
            if (isValid(index)) {
                return index;
            }
            for (final MenuButton menuButton : menuButtons) {
                if ((x == menuButton.x) && (y == menuButton.y)) {
                    return menuButton.index;
                }
            }
            return NULL_INDEX;
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
        
        private final int getLastIndex() {
            return numCells - 1;
        }
        
        protected final void reverse() {
            final int half = numCells / 2, last = getLastIndex();
            for (int index = 0; index < half; index++) {
                final int reversedIndex = last - index;
                final P first = get(index);
                final P opposing = get(reversedIndex);
                if ((first != null) || (opposing != null)) {
                    set(index, opposing);
                    set(reversedIndex, first);
                }
            }
            reverseTurnIndices();
        }
        
        protected final void reverseTurnIndices() {
            final int size = Coltil.size(module.pieceToMoveTurnIndices);
            final int last = getLastIndex();
            for (int i = 0; i < size; i++) {
                final int index = module.pieceToMoveTurnIndices.get(i).intValue();
                final int reversedIndex = last - index;
                module.pieceToMoveTurnIndices.set(i, Integer.valueOf(reversedIndex));
            }
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
            states.add(newState());
            autosave();
        }
        
        protected BoardGameState<P> newState() {
            final List<P> copied = module.copy(grid);
            set(copied);
            return new BoardGameState<P>(copied, module.currentPlayerIndex, Coltil.copyList(module.pieceToMoveTurnIndices), module.copy(module.lastCapturedPiece), module.result);
        }
        
        protected final BoardGameState<P> getCurrentState() {
            return states.get(currentStateIndex);
        }
        
        protected final void setState(final int newStateIndex) {
            final BoardGameState<P> state = states.get(newStateIndex);
            module.currentPlayerIndex = state.playerIndex;
            set(state.grid);
            currentStateIndex = newStateIndex;
            module.pieceToMoveTurnIndices = Coltil.copyList(state.previousTurnIndices);
            module.lastCapturedPiece = module.copy(state.lastCapturedPiece);
            module.result = state.result;
            module.onLoad();
            autosave();
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
        
        protected final void reload() {
            setState(currentStateIndex);
        }
        
        protected final void renderView(final Panderer renderer) {
            final BoardGamePlayer[] players = module.players;
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
            renderTurnIndices(renderer);
            renderMenuButtons(renderer);
            renderLastCapturedPiece(renderer);
            renderModule(renderer);
        }
        
        private final void renderTurnIndices(final Panderer renderer) {
            final List<Integer> turnIndices = module.pieceToMoveTurnIndices;
            final int size = Coltil.size(turnIndices);
            if (size <= 1) {
                return;
            }
            for (int dst = 1; dst < size; dst++) {
                final int srcTileIndex = turnIndices.get(dst - 1).intValue();
                final int srcX = getX(srcTileIndex) * DIM, srcY = getY(srcTileIndex) * DIM;
                final int dstTileIndex = turnIndices.get(dst).intValue();
                final int dstX = getX(dstTileIndex) * DIM, dstY = getY(dstTileIndex) * DIM;
                final int diffX = dstX - srcX, diffY = dstY - srcY;
                final int diff = Math.max(Math.abs(diffX), Math.abs(diffY));
                if (diff == 0) {
                    continue;
                }
                final int offX = TURN_DISPLAY_DISTANCE * diffX / diff, offY = TURN_DISPLAY_DISTANCE * diffY / diff;
                final int dots = diff / TURN_DISPLAY_DISTANCE;
                for (int dot = (dst == 1) ? 0 : 1; dot <= dots; dot++) {
                    render(renderer, imgPreviousTurn, srcX + (dot * offX), srcY + (dot * offY), DEPTH_PREVIOUS_TURN, highlightColor);
                }
            }
        }
        
        private final void renderMenuButtons(final Panderer renderer) {
            for (final MenuButton menuButton : menuButtons) {
                renderMenu(renderer, menuButton);
            }
        }
        
        private final void renderLastCapturedPiece(final Panderer renderer) {
            final P lastCapturedPiece = module.lastCapturedPiece;
            if ((lastCapturedPiece == null) || isExtraMenuButtonAvailable()) {
                return;
            }
            final int x = 9 * DIM, y = 5 * DIM;
            final int capturedPlayer = lastCapturedPiece.player;
            render(renderer, lastCapturedPiece.getImage(), x, y, DEPTH_CELL, module.players[capturedPlayer].getColor());
            final int otherPlayer = getNextPlayerIndex(capturedPlayer);
            render(renderer, imgX, x, y, DEPTH_PIECE, module.players[otherPlayer].getColor());
        }
        
        private final void renderModule(final Panderer renderer) {
            module.renderView(renderer);
        }
        
        protected final void render(final Panderer renderer, final Panmage image, final int x, final int y, final int z, final Pancolor color) {
            renderer.render(getLayer(), image, x, y, z, 0, 0, DIM, DIM, 0, false, false, color.getRf(), color.getGf(), color.getBf());
        }
        
        protected final void renderMenu(final Panderer renderer, final MenuButton menuButton) {
            final int xd = menuButton.x * DIM, yd = menuButton.y * DIM;
            final Pancolor color = menuButton.isActive() ? Pancolor.WHITE : Pancolor.DARK_GREY;
            render(renderer, square, xd, yd, DEPTH_CELL, color);
            render(renderer, menuButton.img, xd, yd, DEPTH_PIECE, color);
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
        @Override public final Pancolor getColor() { return module.players[0].getColor(); }};
    protected final static BoardGameCell square1 = new BoardGameCell() {
        @Override public final Panmage getImage() { return square; }
        @Override public final Pancolor getColor() { return module.players[1].getColor(); }};
    protected final static Pancolor getCurrentPlayerColor() { return module.players[module.currentPlayerIndex].getColor(); }
    protected final static BoardGameCell squareC = new BoardGameCell() {
        @Override public final Panmage getImage() { return square; }
        @Override public final Pancolor getColor() { return getCurrentPlayerColor(); }};
    protected final static Pancolor getWinnerColor() { return module.players[module.result.playerIndex].getColor(); }
    protected final static BoardGameCell verticalSquareW = new BoardGameCell() {
        @Override public final Panmage getImage() { return verticalSquare; }
        @Override public final Pancolor getColor() { return getWinnerColor(); }};
    protected final static BoardGameCell squareH = new BoardGameCell() {
        @Override public final Panmage getImage() { return square; }
        @Override public final Pancolor getColor() { return highlightColor; }};
    protected final static boolean isHighlightSquare(final int x, final int y) {
        final int index = module.getGrid().getIndexOptional(x, y);
        return (index >= 0) && highlightSquares.contains(Integer.valueOf(index));
    }
    protected final static BoardGameCell getPlayerSquare(final int x, final int y) {
        if (isHighlightSquare(x, y)) {
            return squareH;
        }
        return (x % 2) == (y % 2) ? square0 : square1;
    }
    protected final static BoardGameCell getPlayerSquareInverted(final int x, final int y) {
        if (isHighlightSquare(x, y)) {
            return squareH;
        }
        return (x % 2) == (y % 2) ? square1 : square0;
    }
    
    protected abstract static class BoardGamePiece {
        protected int player;
        protected int x = -1;
        protected int y = -1;
        
        protected BoardGamePiece(final int player) {
            this.player = player;
        }
        
        protected final int getIndexRequired() {
            return module.getGrid().getIndexRequired(x, y);
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
        protected BoardGameProfile profile = null;
        
        private BoardGamePlayer(final int index) {
            this.index = index;
        }
        
        protected final Pancolor getColor() {
            final Pancolor firstChoice = getColor(profile.color1);
            if (index == 0) {
                return firstChoice;
            }
            final Pancolor color0 = getColor0();
            return firstChoice.equals(color0) ? getColor(profile.color2) : firstChoice;
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
            return module.players[0].getColor();
        }
        
        private final int getProfileIndex() {
            return (profile == null) ? -1 : profile.profileIndex;
        }
        
        @Override
        public final boolean equals(final Object o) {
            return (o instanceof BoardGamePlayer) && index == ((BoardGamePlayer) o).index;
        }
        
        @Override
        public final int hashCode() {
            return index;
        }
    }
    
    protected final static class BoardGameProfile {
        protected int profileIndex = -1;
        protected String name = null;
        protected Pancolor color1 = null; // Null means to use the default for each game
        protected Pancolor color2 = null; // If player 2's preferred color matches player 1, then use color 2 instead
        protected boolean deleted = false;
        
        protected final void init(final int index) {
            profileIndex = index;
            name = "Player " + (index + 1);
            color1 = null;
            color2 = null;
            deleted = false;
            save();
        }
        
        protected final void changeName(final String name) {
            if (Chartil.isValued(name)) {
                this.name = name;
            }
        }
        
        protected final void save() {
            final Segment seg = new Segment(SEG_PROFILE);
            seg.setValue(0, name);
            seg.setField(1, toField(color1));
            seg.setField(2, toField(color2));
            seg.setBoolean(3, deleted);
            final Writer w = Iotil.getWriter(getFileName());
            try {
                seg.saveln(w);
                writeEndOfFile(w, seg);
            } catch (final IOException e) {
                throw new Panception(e);
            } finally {
                Iotil.close(w);
            }
        }
        
        protected final void load(final int profileIndex) throws IOException {
            this.profileIndex = profileIndex;
            final SegmentStream in = openSegmentStream(getFileName());
            try {
                final Segment seg = in.readRequire(SEG_PROFILE);
                name = seg.getValue(0);
                color1 = toColor(seg.getField(1));
                color2 = toColor(seg.getField(2));
                deleted = seg.toBoolean(3);
                validateEndOfFile(in);
            } finally {
                in.close();
            }
        }
        
        protected final void delete() {
            deleted = true;
            save();
        }
        
        protected final String getFileName() {
            return getProfileFileName(profileIndex);
        }
        
        protected final static String getProfileFileName(final int profileIndex) {
            return "profile_" + profileIndex + ".txt";
        }
    }
    
    protected final static class BoardGameState<P extends BoardGamePiece> {
        private final List<P> grid;
        private final int playerIndex;
        protected final List<Integer> previousTurnIndices;
        private final P lastCapturedPiece;
        private final BoardGameResult result;
        
        protected BoardGameState(final List<P> grid, final int playerIndex, final List<Integer> previousTurnIndices, final P lastCapturedPiece, final BoardGameResult result) {
            this.grid = grid;
            this.playerIndex = playerIndex;
            this.previousTurnIndices = previousTurnIndices;
            this.lastCapturedPiece = lastCapturedPiece;
            this.result = result;
        }
    }
    
    protected final static class BoardGameResult {
        private final int resultStatus;
        private final int playerIndex;
        
        protected BoardGameResult(final int resultStatus, final int playerIndex) {
            this.resultStatus = resultStatus;
            this.playerIndex = playerIndex;
        }
        
        protected final static BoardGameResult newTie() {
            return new BoardGameResult(RESULT_TIE, -1);
        }
    }
    
    protected final static class BoardGameContext {
        final int currentStateIndex;
        final int[] profileIndices;
        
        protected BoardGameContext(final int currentStateIndex, final int[] profileIndices) {
            this.currentStateIndex = currentStateIndex;
            this.profileIndices = profileIndices;
        }
    }
    
    protected abstract static class MenuButton {
        private final int x, y;
        private final int index;
        private final Panmage img;
        
        protected MenuButton(final int x, final int y, final Panmage img) {
            this.x = x;
            this.y = y;
            this.index = nextMenuIndex;
            nextMenuIndex--;
            this.img = img;
        }
        
        protected boolean isActive() {
            return true;
        }
        
        protected abstract void onTouch();
    }
    
    public final static void main(final String[] args) {
        try {
            new BoardGame().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
