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
package org.pandcorps.block;

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.game.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.Panteraction.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Fonts.*;
import org.pandcorps.pandax.tile.*;

public class BlockGame extends BaseGame {
    protected final static String TITLE = "Mana Stones";
    protected final static String VERSION = "0.0.1";
    protected final static String YEAR = "2021";
    protected final static String AUTHOR = "Andrew M. Martin";
    protected final static String COPYRIGHT_SHORT = Pantext.CHAR_COPYRIGHT + " " + YEAR;
    protected final static String COPYRIGHT = "Copyright " + COPYRIGHT_SHORT;
    
    protected final static String RES = "org/pandcorps/block/";
    protected final static String RES_IMG = RES + "image/";
    protected final static String RES_SND = RES + "sound/";
    protected final static String RES_MUS = RES + "music/";
    
    protected final static int DIM = 8;
    protected final static int GAME_W = 288;
    protected final static int GAME_H = 160;
    protected final static int GAME_HALF = GAME_W / 2;
    protected final static int GRID_W = 16;
    protected final static int GRID_H = 16;
    protected final static int GRID_AND_NEXT_H = GRID_H + 2;
    protected final static int GRID_SIZE = GRID_W * GRID_H;
    protected final static int GRID_HALF = GRID_W / 2;
    
    protected final static int X = (GAME_W - (GRID_W * DIM)) / 2;
    protected final static int Y = (GAME_H - (GRID_AND_NEXT_H * DIM)) / 2;
    
    protected final static int NEXT_Y = GRID_H + 1;
    
    protected final static int Z_BG = 0;
    protected final static int Z_GRID = 2;
    protected final static int Z_FALLING_1 = 4;
    protected final static int Z_FALLING_2 = 6;
    protected final static int Z_BURST = 8;
    protected final static int Z_PUFF = 10;
    protected final static int Z_CURSOR = 12;
    private final static int[] Z_FALLINGS = { Z_FALLING_1, Z_FALLING_2 };
    
    protected final static int FALL_TIME = 30;
    protected final static int FAST_TIME = 2;
    protected final static int GRID_DROP_TIME = 8;
    protected final static int MOVE_HOLD_TIME = 12;
    protected final static int MOVE_FAST_TIME = 2;
    
    protected final static int MATCH_THRESHOLD_MIN = 3;
    protected final static int MATCH_THRESHOLD_MAX = 4;
    
    protected final static byte TILE_STONE = Tile.BEHAVIOR_SOLID; // Falls when tiles beneath are cleared
    protected final static byte TILE_ENEMY = 2; // Stays in its place until cleared
    
    protected final static int ROT_EAST = 0;
    protected final static int ROT_SOUTH = 1;
    protected final static int ROT_WEST = 2;
    protected final static int ROT_NORTH = 3;
    protected final static int ROT_MIN = 0;
    protected final static int ROT_MAX = 3;
    
    protected final static int NUM_COLORS = 3;
    
    protected static int MAX_PLAYERS = 2;
    
    protected final static int LEVEL_MIN = 1; // Level n will have 3n enemies (n of each color)
    protected final static int LEVEL_MAX = 64;
    
    protected final static int DIM_TEXT = 8;
    
    protected static Queue<Runnable> loaders = new LinkedList<Runnable>();
    protected static Panmage block = null;
    protected static Panmage black = null;
    protected static Panimation anmPuff = null;
    protected static Font font = null;
    protected static Panmage cursorImg = null;
    protected static Pansound fxMove = null; // Left/right, not down
    protected static Pansound fxRotate = null;
    protected static Pansound fxThud = null;
    protected static Pansound fxMatch = null;
    protected static Pansound fxVictory = null;
    protected static Pansound fxDefeat = null;
    protected static Pansound music = null;
    protected final static Panple size = new FinPanple2(DIM, DIM);
    protected final static CellBehavior STONE_BEHAVIOR = new CellBehavior(0.0f, 0.0f, TILE_STONE);
    protected final static CellBehavior ENEMY_BEHAVIOR = new CellBehavior(8.0f, 0.0f, TILE_ENEMY);
    protected static CellType[] STONE_TYPES = new CellType[NUM_COLORS];
    protected static CellType[] ENEMY_TYPES = new CellType[NUM_COLORS];
    protected static Panroom room = null;
    protected static Background background = null;
    protected static Grid grid = null;
    private static Cursor cursor = null;
    protected final static List<ControlScheme> controlSchemes = new ArrayList<ControlScheme>(MAX_PLAYERS);
    protected static ControlScheme inactiveControlScheme = null;
    protected static ControlScheme defaultKeyboard = null;
    private final static Set<Panput> customMappedInputs = new HashSet<Panput>();
    protected final static List<Player> players = new ArrayList<Player>(MAX_PLAYERS);
    protected static int level = LEVEL_MIN;
    protected static int matchThreshold = MATCH_THRESHOLD_MAX;
    
    @Override
    protected final boolean isFullScreen() {
        return true;
    }
    
    @Override
    protected final int getGameWidth() {
        return GAME_W;
    }
    
    @Override
    protected final int getGameHeight() {
        return GAME_H;
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
        Panscreen.set(new LogoScreen(DeviceSelectScreen.class, loaders));
    }
    
    private final static void loadResources() {
        MAX_PLAYERS = getMaxPlayers();
        defaultKeyboard = ControlScheme.getDefaultKeyboard();
        loadImages();
        loadAudio();
    }
    
    private final static void loadImages() {
        block = Pangine.getEngine().createImage("block", RES_IMG + "Block.png");
        final ImgFactory f = ImgFactory.getFactory();
        final Img img = f.create(1, 1);
        img.setRGB(0, 0, f.getDataElement(0, 0, 0, 255));
        black = Pangine.getEngine().createImage("black", img);
        initColor(0, block, 0.5f, 1.0f, 1.0f);
        initColor(1, block, 0.0f, 0.5f, 1.0f);
        initColor(2, block, 0.375f, 0.375f, 0.375f);
        anmPuff = newAnimation(3, 0.75f, 1.0f, 1.0f, 16, 16, 16, 24, 24, 24);
        font = Fonts.getClassic(new FontRequest(FontType.Upper, DIM_TEXT), new FinPancolor(192, Pancolor.MAX_VALUE, Pancolor.MAX_VALUE));
        if (isCursorNeeded()) {
            final Panmage cursorRaw = Pangine.getEngine().createImage("cursor.raw", new FinPanple2(0, 7), null, null, RES_IMG + "Cursor.png");
            cursorImg = new AdjustedPanmage("cursor", cursorRaw, 0.75f, 1.0f, 1.0f);
        }
    }
    
    private final static void loadAudio() {
        Pansound.setDefaultReplayThreshold(4);
        final Panaudio audio = Pangine.getEngine().getAudio();
        fxMove = audio.createSound(RES_SND + "Move.mid");
        fxRotate = audio.createSound(RES_SND + "Rotate.mid");
        fxThud = audio.createSound(RES_SND + "Thud.mid");
        fxMatch = audio.createSound(RES_SND + "Match.mid");
        fxVictory = audio.createTransition(RES_SND + "Victory.mid");
        fxDefeat = audio.createTransition(RES_SND + "Defeat.mid");
        music = audio.createMusic(RES_MUS + "Music.mid");
    }
    
    private final static void initColor(final int i, final Panmage block, final float r, final float g, final float b) {
        final CellColor color = new CellColor(r, g, b);
        STONE_TYPES[i] = newStone(color);
        ENEMY_TYPES[i] = newEnemy(color);
        color.anmBurst = newAnimation(4, r, g, b, 24, 0, 24, 8, 24, 16);
    }
    
    private final static CellType newStone(final CellColor color) {
        return new CellType(color, STONE_BEHAVIOR);
    }
    
    protected final static CellType newEnemy(final CellColor color) {
        return new CellType(color, ENEMY_BEHAVIOR);
    }
    
    private final static Panimation newAnimation(final int dur, final float r, final float g, final float b,
            final int x0, final int y0, final int x1, final int y1, final int x2, final int y2) {
        return Pangine.getEngine().createAnimation(Pantil.vmid(),
                newFrame(dur, r, g, b, x0, y0),
                newFrame(dur, r, g, b, x1, y1),
                newFrame(dur, r, g, b, x2, y2));
    }
    
    private final static Panframe newFrame(final int dur, final float r, final float g, final float b, final int x, final int y) {
        return Pangine.getEngine().createFrame(Pantil.vmid(), newSub(r, g, b, x, y), dur);
    }
    
    private final static Panmage newSub(final float r, final float g, final float b, final int x, final int y) {
        return new AdjustedPanmage(Pantil.vmid(), block, 0, false, false, r, g, b, x, y, size);
    }
    
    private final static String getCursorCharacter() {
        return isCursorNeeded() ? "*" : "";
    }
    
    private final static boolean isCursorNeeded() {
        return Pangine.getEngine().isMouseSupported();
    }
    
    private final static Cursor addCursor() {
        if (!isCursorNeeded()) {
            return null;
        }
        cursor = Cursor.addCursorIfNeeded(room, cursorImg).setHiddenWhenUnused(true);
        cursor.getPosition().setZ(Z_CURSOR);
        return cursor;
    }
    
    private final static void hideCursor() {
        Cursor.hide(cursor);
    }
    
    protected final static boolean isKeyboardRegistered() {
        for (final ControlScheme ctrl : Coltil.unnull(controlSchemes)) {
            if (ctrl.getDevice() instanceof Keyboard) {
                return true;
            }
        }
        return false;
    }
    
    protected final static void registerMenuUpDown(final Panctor actor, final ControlScheme scheme, final ActionEndListener listenerUp, final ActionEndListener listenerDown) {
        actor.register(scheme.getUp(), listenerUp);
        actor.register(scheme.getDown(), listenerDown);
        if (!isKeyboardRegistered()) {
            actor.register(defaultKeyboard.getUp(), listenerUp);
            actor.register(defaultKeyboard.getDown(), listenerDown);
        }
    }
    
    protected final static void registerMenuLeftRight(final Panctor actor, final ControlScheme scheme, final ActionEndListener listenerLeft, final ActionEndListener listenerRight) {
        actor.register(scheme.getLeft(), listenerLeft);
        actor.register(scheme.getRight(), listenerRight);
        if (!isKeyboardRegistered()) {
            actor.register(defaultKeyboard.getLeft(), listenerLeft);
            actor.register(defaultKeyboard.getRight(), listenerRight);
        }
    }
    
    protected final static void registerMenu12(final Panctor actor, final ControlScheme scheme, final ActionEndListener listener) {
        actor.register(scheme.get1(), listener);
        actor.register(scheme.get2(), listener);
        if (!isKeyboardRegistered()) {
            actor.register(defaultKeyboard.get1(), listener);
            actor.register(defaultKeyboard.get2(), listener);
        }
    }
    
    protected final static void registerMenuSubmit(final Panctor actor, final ControlScheme scheme, final ActionEndListener listener) {
        actor.register(scheme.getSubmit(), listener);
        if (!isKeyboardRegistered()) {
            actor.register(defaultKeyboard.getSubmit(), listener);
        }
    }
    
    protected final static TouchButton newTouchButton(final Panctor actor, final int x, final int y, final int w, final int h, final ActionEndListener listener) {
        final TouchButton button;
        final Pangine engine = Pangine.getEngine();
        final Panteraction in = engine.getInteraction();
        button = new TouchButton(in, Pantil.vmid(), x - 2, y - 2, w + 4, h + 4, true);
        engine.registerTouchButton(button);
        actor.register(button, listener);
        return button;
    }
    
    private final static int getNumPlayers() {
        return Math.max(controlSchemes.size(), 1);
    }
    
    private final static int getMaxPlayers() {
        return isCursorNeeded() ? 2 : 1;
    }
    
    protected final static CellType randomStone() {
        return Mathtil.rand(STONE_TYPES);
    }
    
    protected final static boolean isOccupied(final int x, final int y) {
        if (y >= GRID_H) {
            return false;
        } else if (y < 0) {
            return true;
        } else if (x < 0) {
            return true;
        } else if (x >= GRID_W) {
            return true;
        }
        return grid.getCell(x, y) != null;
    }
    
    protected final static void burst(final int index, final CellType cellType) {
        burst(grid.getX(index), grid.getY(index), cellType);
    }
    
    protected final static void burst(final int x, final int y, final CellType cellType) {
        burst(cellType.color.anmBurst, x, y);
    }
    
    protected final static void puff(final int x, final int y) {
        burst(anmPuff, x, y);
    }
    
    protected final static void burst(final Panimation anm, final int x, final int y) {
        final Burst burst = new Burst(anm);
        burst.getPosition().set(X + (x * DIM), Y + (y * DIM), Z_PUFF);
        room.addActor(burst);
    }
    
    private static int oldNumPlayers = 1;
    
    protected abstract static class BackgroundScreen extends Panscreen {
        @Override
        protected final void load() throws Exception {
            final Pangine engine = Pangine.getEngine();
            engine.enableColorArray();
            engine.setBgColor(Pancolor.BLACK);
            room = Pangame.getGame().getCurrentRoom();
            final Panlayer bg = engine.createLayer("bg", GAME_W, GAME_H, room.getSize().getZ(), room);
            final int numPlayers = getNumPlayers();
            if (numPlayers != oldNumPlayers) {
                Panctor.destroy(background);
                oldNumPlayers = numPlayers;
                background = null;
            }
            if (background == null) {
                background = new Background();
            }
            bg.addActor(background);
            bg.setConstant(true);
            room.addBeneath(bg);
            room.setClearDepthEnabled(false);
            registerGlobal();
            loadBlock();
        }
        
        protected final void registerGlobal() {
            GuyPlatform.registerCapture(background);
        }
        
        protected abstract void loadBlock();
        
        @Override
        protected final void destroy() {
            destroyBlock();
            background.detach();
        }
        
        protected void destroyBlock() {
        }
        
        protected final static Pantext addText(final CharSequence msg, final float x, final float y) {
            return addText(msg, x, y, false);
        }
        
        protected final static Pantext addText(final CharSequence msg, final float x, final float y, final boolean center) {
            final Pantext text = new Pantext(Pantil.vmid(), font, msg);
            text.getPosition().set(x, y, Z_PUFF);
            if (center) {
                text.centerX();
            }
            room.addActor(text);
            return text;
        }
    }
    
    protected final static class BlockScreen extends BackgroundScreen {
        @Override
        protected final void loadBlock() {
            grid = new Grid();
            players.clear();
            final int numPlayers = getNumPlayers();
            for (final ControlScheme ctrl : controlSchemes) {
                final int startX;
                if (numPlayers < 2) {
                    startX = (GRID_W / 2) - 1;
                } else {
                    startX = (GRID_HALF * players.size()) + (GRID_HALF / 2) - 1;
                }
                new Player(startX).register(ctrl);
            }
            addCursor(); // Can be used to click pause button and click options on pause menu
            music.startMusic();
        }
        
        @Override
        protected final void destroyBlock() {
            Pangine.getEngine().getAudio().stopMusic();
        }
    }
    
    protected abstract static class TitledScreen extends BackgroundScreen {
        @Override
        protected final void loadBlock() {
            int y = GAME_H - 32;
            addText(TITLE, GAME_HALF, y, true);
            y -= 8;
            addText(COPYRIGHT_SHORT, GAME_HALF, y, true);
            y -= 8;
            addText(AUTHOR, GAME_HALF, y, true);
            loadExtra();
        }
        
        protected abstract void loadExtra();
    }
    
    private final static int getTextY() {
        return GAME_H / 2;
    }
    
    protected abstract static class TextScreen extends TitledScreen {
        protected Pantext text = null;
        
        @Override
        protected final void loadExtra() {
            int y = getTextY();
            for (final String msg : getTexts()) {
                text = addText(msg, GAME_HALF, y, true);
                y -= 8;
            }
            final Panscreen nextScreen = getNextScreen();
            loadText();
            if (nextScreen == null) {
                return;
            }
            Pangine.getEngine().addTimer(text, 15, new TimerListener() {
                @Override public final void onTimer(final TimerEvent event) {
                    text.register(new ActionEndListener() {
                        @Override public final void onActionEnd(final ActionEndEvent event) {
                            Panscreen.set(nextScreen);
                        }});
                }});
        }
        
        protected abstract String[] getTexts();
        
        protected void loadText() {
        }
        
        protected Panscreen getNextScreen() {
            return null;
        }
    }
    
    protected final static class DeviceSelectScreen extends TextScreen {
        @Override
        protected final String[] getTexts() {
            if (!isCursorNeeded()) {
                return new String[] { "Tap to start" };
            }
            return new String[] { "Player " + (controlSchemes.size() + 1), "Press any button" };
        }
        
        @Override
        protected final void loadText() {
            text.register(new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    final Device device = event.getDevice();
                    if ((device instanceof Mouse) || (device instanceof Touchscreen)) {
                        return;
                    }
                    for (final ControlScheme ctrl : controlSchemes) {
                        if (device == ctrl.getDevice()) {
                            if (device instanceof Keyboard) {
                                controlSchemes.add(new ControlScheme());
                                fxMatch.startSound();
                                goMapInput();
                            }
                            return;
                        }
                    }
                    fxMatch.startSound();
                    controlSchemes.add(ControlScheme.getDefault(device));
                    Panscreen.set(new MenuScreen());
                }});
        }
    }
    
    protected final static void goMapInput() {
        customMappedInputs.clear();
        Panscreen.set(new MapInputScreen(0));
    }
    
    protected final static class MapInputScreen extends TitledScreen {
        private final static int X_LABEL = X;
        private final static String[] INPUT_NAMES = { "Up", "Down", "Left", "Right", "Clockwise", "Counterclockwise", "Start" };
        private final int playerIndex;
        private final List<Panput> inputs = new ArrayList<Panput>();
        private int y = 100;
        
        protected MapInputScreen(final int playerIndex) {
            this.playerIndex = playerIndex;
        }
        
        @Override
        protected final void loadExtra() {
            final Pantext text = addText("Player " + (playerIndex + 1));
            addNextLabel();
            text.register(new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    final Panput input = event.getInput();
                    if (customMappedInputs.contains(input)) {
                        goMapInput(); // Collision with other player, so just start over for both
                        return;
                    } else if (inputs.contains(input)) {
                        Panscreen.set(new MapInputScreen(playerIndex)); // Collision with self, so start over for self
                        return;
                    }
                    inputs.add(input);
                    if (inputs.size() < INPUT_NAMES.length) {
                        fxRotate.startSound();
                        addNextLabel();
                    } else {
                        fxMatch.startSound();
                        onFinish();
                    }
                }});
        }
        
        protected final void addNextLabel() {
            addText(INPUT_NAMES[inputs.size()]);
        }
        
        protected final Pantext addText(final String msg) {
            final Pantext text = addText(msg, X_LABEL, y);
            y -= 12;
            return text;
        }
        
        protected final void onFinish() {
            customMappedInputs.addAll(inputs);
            controlSchemes.set(playerIndex, new ControlScheme(
                    inputs.get(1), inputs.get(0), inputs.get(2), inputs.get(3),
                    inputs.get(4), inputs.get(5), null, inputs.get(6)));
            Panscreen.set((playerIndex >= (controlSchemes.size() - 1)) ? new MenuScreen() : new MapInputScreen(playerIndex + 1));
        }
    }
    
    protected static class MenuScreen extends TitledScreen {
        private final static int X_CURSOR = X;
        protected final static int X_OPTION = X + 8;
        private final static int X_VALUE = X + 80;
        protected final static int Y_OFFSET = 16;
        private final List<Option> options = new ArrayList<Option>();
        protected int y;
        private int optionIndex = 0;
        private Pantext cursor = null;
        
        @Override
        protected final void loadExtra() {
            y = getTopY();
            cursor = addText(getCursorCharacter(), X_CURSOR, y);
            loadMenu();
            register(controlSchemes.get(0));
            addCursor();
        }
        
        protected int getTopY() {
            return 96;
        }
        
        protected void loadMenu() {
            level = Math.min(level, LEVEL_MAX);
            addOption("Level", new IntOption() {
                @Override protected final int get() {
                    return level;
                }
                @Override protected final void set(final int value) {
                    level = value;
                }
                @Override
                protected final int max() {
                    return LEVEL_MAX;
                }});
            addOption("Match", new IntOption() {
                @Override protected final int get() {
                    return matchThreshold;
                }
                @Override protected final void set(final int value) {
                    matchThreshold = value;
                }
                @Override
                protected final int min() {
                    return MATCH_THRESHOLD_MIN;
                }
                @Override
                protected final int max() {
                    return MATCH_THRESHOLD_MAX;
                }});
            addOption("Players", MAX_PLAYERS > 1, new IntOption() {
                @Override protected final int get() {
                    return controlSchemes.size();
                }
                @Override protected final void set(final int value) {
                    if (value <= 1) {
                        while (controlSchemes.size() > 1) {
                            inactiveControlScheme = controlSchemes.remove(controlSchemes.size() - 1);
                        }
                    } else {
                        if (inactiveControlScheme == null) {
                            Panscreen.set(new DeviceSelectScreen());
                        } else {
                            controlSchemes.add(inactiveControlScheme);
                        }
                    }
                }
                @Override
                protected final int max() {
                    return MAX_PLAYERS;
                }});
            addOption("Map Input", new Option() {
                @Override protected final void onAction() {
                    goMapInput();
                }});
            addOption("Start", new Option() {
                @Override protected final void onAction() {
                    startGame();
                }});
            addOption("Quit", new Option() {
                @Override protected final void onAction() {
                    Pangine.getEngine().exit();
                }});
        }
        
        protected final void addOption(final String msg, final boolean needed, final Option option) {
            if (!needed) {
                return;
            }
            addOption(msg, option);
        }
        
        protected final void addOption(final String msg, final Option option) {
            addText(msg, X_OPTION, y);
            option.y = y;
            options.add(option);
            option.init();
            y -= Y_OFFSET;
        }
        
        protected final void register(final ControlScheme scheme) {
            final Panctor actor = cursor;
            registerMenuUpDown(actor, scheme,
                new ActionEndListener() { @Override public final void onActionEnd(final ActionEndEvent event) { onUp(); }},
                new ActionEndListener() { @Override public final void onActionEnd(final ActionEndEvent event) { onDown(); }});
            registerMenuLeftRight(actor, scheme,
                new ActionEndListener() { @Override public final void onActionEnd(final ActionEndEvent event) { onX(-1); }},
                new ActionEndListener() { @Override public final void onActionEnd(final ActionEndEvent event) { onX(1); }});
            registerMenu12(actor, scheme, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) { onAction(); }});
            registerMenuSubmit(actor, scheme, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) { startGame(); }});
        }
        
        protected final void startGame() {
            fxMatch.startSound();
            Panscreen.set(new LevelStartScreen());
        }
        
        protected final void onUp() {
            if (optionIndex <= 0) {
                optionIndex = options.size() - 1;
            } else {
                optionIndex--;
            }
            updateCursor();
        }
        
        protected final void onDown() {
            if (optionIndex >= (options.size() - 1)) {
                optionIndex = 0;
            } else {
                optionIndex++;
            }
            updateCursor();
        }
        
        protected final void onX(final int dir) {
            getOption().onX(dir);
        }
        
        protected final void onAction() {
            getOption().onAction();
        }
        
        protected final void updateCursor() {
            cursor.getPosition().setY(getOption().y);
            fxMove.startSound();
            hideCursor();
        }
        
        protected final Option getOption() {
            return options.get(optionIndex);
        }
        
        protected abstract class Option {
            protected int y;
            
            protected void init() {
                newTouchButton(cursor, X, y, GRID_W * DIM_TEXT, DIM_TEXT, new ActionEndListener() {
                    @Override public final void onActionEnd(final ActionEndEvent event) { onAction(); }});
            }
            
            protected void onX(final int dir) {
            }
            
            protected void onAction() {
            }
        }
        
        protected abstract class IntOption extends Option {
            private StringBuilder valueSequence = null;
            
            @Override
            protected final void onX(final int dir) {
                int value = get() + dir;
                final int mx = max(), mn = min();
                if (value > mx) {
                    value = mn;
                } else if (value < mn) {
                    value = mx;
                }
                set(value);
                setValueSequence();
                fxRotate.startSound();
            }
            
            protected final void init() {
                valueSequence = new StringBuilder();
                setValueSequence();
                final Pantext text = addText(valueSequence, X_VALUE, y);
                newTouchButton(text, X_VALUE, y, DIM_TEXT, DIM_TEXT, new ActionEndListener() {
                    @Override public final void onActionEnd(final ActionEndEvent event) { onX(-1); }});
                newTouchButton(text, X_VALUE + ((max() < 10) ? 4 : 5) * 8, y, DIM_TEXT, DIM_TEXT, new ActionEndListener() {
                    @Override public final void onActionEnd(final ActionEndEvent event) { onX(1); }});
            }
            
            protected final void setValueSequence() {
                Chartil.clear(valueSequence);
                valueSequence.append("< ");
                if (max() < 10) {
                    valueSequence.append(get());
                } else {
                    valueSequence.append(Chartil.padZero(get(), 2));
                }
                valueSequence.append(" >");
            }
            
            protected abstract int get();
            
            protected abstract void set(final int value);
            
            protected int min() {
                return 1;
            }
            
            protected abstract int max();
        }
    }
    
    protected final static class LevelStartScreen extends TextScreen {
        @Override
        protected final String[] getTexts() {
            return new String[] { "Level " + level, "Start" };
        }
        
        @Override
        protected final Panscreen getNextScreen() {
            return new BlockScreen();
        }
    }
    
    protected final static class LevelCompleteScreen extends TextScreen {
        @Override
        protected final String[] getTexts() {
            return new String[] { "Level " + (level - 1), "Complete" }; // Level incremented before this screen
        }
        
        @Override
        protected final Panscreen getNextScreen() {
            if (level > LEVEL_MAX) { // Level incremented before this screen
                return new VictoryScreen();
            }
            return new LevelStartScreen();
        }
    }
    
    protected final static class VictoryScreen extends TextScreen {
        @Override
        protected final String[] getTexts() {
            return new String[] { "You win!" };
        }
        
        @Override
        protected final Panscreen getNextScreen() {
            return new MenuScreen();
        }
    }
    
    protected final static class GameOverScreen extends MenuScreen {
        @Override
        protected final void loadMenu() {
            addText("Game over", GAME_HALF, y + Y_OFFSET, true);
            addOption("Continue", new Option() {
                @Override protected final void onAction() {
                    Panscreen.set(new LevelStartScreen());
                }});
            addOption("Quit", new Option() {
                @Override protected final void onAction() {
                    Panscreen.set(new MenuScreen());
                }});
        }
        
        @Override
        protected final int getTopY() {
            return getTextY() - Y_OFFSET;
        }
    }
    
    protected final static class Background extends Panctor {
        private final static int topNext = GAME_H - DIM;
        private final static int topGrid = topNext - (DIM * 2);
        private final static float r = 0.0f, g = 0.25f, b = 0.5f;
        private final static Random rand = new Random();
        private final static long seed = Mathtil.newSeed();
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panlayer layer = getLayer();
            rand.setSeed(seed);
            final int left = X - DIM, right = X + (GRID_W * DIM);
            for (int i = 0; i < GRID_H; i++) {
                renderer.render(layer, block, left, Y + (DIM * i), Z_BG, 8, 8, DIM, DIM, 1, false, false, r, g, b);
                renderer.render(layer, block, right, Y + (DIM * i), Z_BG, 8, 8, DIM, DIM, 3, false, false, r, g, b);
            }
            renderer.render(layer, block, left, topGrid, Z_BG, 0, 8, DIM, DIM, 0, false, false, r, g, b);
            renderer.render(layer, block, right, topGrid, Z_BG, 0, 8, DIM, DIM, 3, false, false, r, g, b);
            renderer.render(layer, block, left, 0, Z_BG, 0, 8, DIM, DIM, 1, false, false, r, g, b);
            renderer.render(layer, block, right, 0, Z_BG, 0, 8, DIM, DIM, 2, false, false, r, g, b);
            final int rightBg = right + DIM, rightFar = GAME_W - DIM;
            final int topBg = topNext - DIM;
            render8s(renderer, layer, 0, 0, 9, 1);
            render8s(renderer, layer, rightBg, 0, 9, 1);
            render8s(renderer, layer, 0, DIM, 1, 18);
            render8s(renderer, layer, rightFar, DIM, 1, 18);
            render8s(renderer, layer, 0, topNext, 9, 1);
            render8s(renderer, layer, rightBg, topNext, 9, 1);
            render8s(renderer, layer, left, topBg, 1, 2);
            render8s(renderer, layer, right, topBg, 1, 2);
            render16s(renderer, layer, DIM, DIM, 4, 9);
            render16s(renderer, layer, rightBg, DIM, 4, 9);
            if (getNumPlayers() < 2) {
                renderTop(renderer, layer, X, GRID_W);
            } else {
                for (int j = 0; j < 2; j++) {
                    renderTop(renderer, layer, (GRID_HALF * DIM * j) + X, GRID_HALF);
                }
            }
        }
        
        private final static void renderTop(final Panderer renderer, final Panlayer layer, final int xBase, final int w) {
            final int openLeft = (w / 2) - 2, openRight = openLeft + 3;
            for (int i = 0; i < w; i++) {
                // Top
                final int x = xBase + (DIM * i), y = ((i > openLeft) && (i < openRight)) ? topNext : topGrid;
                if (i == openLeft) {
                    renderer.render(layer, block, x, y, Z_BG, 16, 8, DIM, DIM, 0, false, false, r, g, b);
                    renderer.render(layer, block, x, y + DIM, Z_BG, 8, 8, DIM, DIM, 1, false, false, r, g, b);
                    renderer.render(layer, block, x, topNext, Z_BG, 0, 8, DIM, DIM, 0, false, false, r, g, b);
                } else if (i == openRight) {
                    renderer.render(layer, block, x, y, Z_BG, 16, 8, DIM, DIM, 3, false, false, r, g, b);
                    renderer.render(layer, block, x, y + DIM, Z_BG, 8, 8, DIM, DIM, 3, false, false, r, g, b);
                    renderer.render(layer, block, x, topNext, Z_BG, 0, 8, DIM, DIM, 3, false, false, r, g, b);
                } else {
                    renderer.render(layer, block, x, y, Z_BG, 8, 8, DIM, DIM, 0, false, false, r, g, b);
                    if (y == topGrid) {
                        render8s(renderer, layer, x, y + DIM, 1, 2);
                    }
                }
                // Bottom
                renderer.render(layer, block, x, 0, Z_BG, 8, 8, DIM, DIM, 2, false, false, r, g, b);
            }
        }
        
        private final static void render8s(final Panderer renderer, final Panlayer layer, final int x, final int y, final int w, final int h) {
            renderBgs(renderer, layer, x, y, w, h, 0, 0, DIM);
        }
        
        private final static void render16s(final Panderer renderer, final Panlayer layer, final int x, final int y, final int w, final int h) {
            renderBgs(renderer, layer, x, y, w, h, 0, 16, 16);
        }
        
        private final static void renderBgs(final Panderer renderer, final Panlayer layer, final int x, final int y, final int w, final int h,
                final float ix, final float iy, final int d) {
            final float df = d;
            for (int j = 0; j < h; j++) {
                final float yj = y + (j * df);
                for (int i = 0; i < w; i++) {
                    final float b = Mathtil.randf(rand, 0.35f, 1.0f);
                    final float g = Mathtil.randf(rand, 0.0f, b);
                    final float r = Mathtil.randf(rand, 0.0f, Math.min(g, 0.65f * b));
                    renderer.render(layer, block, x + (i * df), yj, Z_BG, ix, iy, df, df, 0, false, false, r, g, b);
                }
            }
        }
    }
    
    protected final static class Grid extends Panctor implements StepListener {
        protected final CellType[] cells = new CellType[GRID_SIZE];
        private Set<Integer> indicesToDrop = new TreeSet<Integer>();
        private Set<Integer> indicesToDropBack = new TreeSet<Integer>();
        private Set<Integer> indicesDropped = new HashSet<Integer>();
        private final Set<Integer> indicesToCheckForMatches = new TreeSet<Integer>();
        private final Set<Integer> indicesToClear = new HashSet<Integer>();
        private int dropTimer = 0;
        private int enemyCount = 0;
        private final int[] enemiesNeeded = new int[NUM_COLORS];
        private int enemiesAdded = 0;
        private boolean paused = false;
        private int pauser = -1;
        private BasePauseMenu pauseMenu = null;
        private int startTimer = 60;
        
        protected Grid() {
            this(true);
        }
        
        protected Grid(final boolean initializationNeeded) {
            if (!initializationNeeded) {
                return;
            }
            room.addActor(this);
            initEnemies();
            registerGlobal();
        }
        
        protected final void registerGlobal() {
            register(Pangine.getEngine().getInteraction().KEY_ESCAPE, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    togglePause(0);
                }});
        }
        
        protected final void initEnemies() {
            while (!attemptInitEnemies()) {
                for (int i = 0; i < GRID_SIZE; i++) {
                    cells[i] = null;
                }
            }
        }
        
        private final boolean attemptInitEnemies() {
            initBaseEnemies();
            return initExtraEnemies();
        }
        
        protected final void initBaseEnemies() {
            enemyCount = level * NUM_COLORS;
            final int baseSize = getEnemyBaseSize();
            for (int i = 0; i < NUM_COLORS; i++) {
                enemiesNeeded[i] = level;
            }
            int n = baseSize;
            enemiesAdded = 0;
            for (int i = 0; i < baseSize; i++) {
                initEnemy(i, n);
                n--;
            }
        }
        
        protected final boolean initExtraEnemies() {
            if (enemiesAdded < enemyCount) {
                final int baseSize = getEnemyBaseSize();
                int y = baseSize / GRID_W, xRaw = 0;
                while (enemiesAdded < enemyCount) {
                    final int x2 = xRaw / 2;
                    final int x = ((xRaw % 2) == 0) ? x2 : (GRID_W - x2 - 1);
                    final int i = getIndex(x, y);
                    initEnemy(i, enemyCount - enemiesAdded);
                    if (enemiesAdded >= enemyCount) {
                        break;
                    }
                    xRaw++;
                    if (xRaw >= GRID_W) {
                        xRaw = 0;
                        y++;
                        if (y >= (GRID_H - 1)) {
                            return false; // Ran out of room, start over
                        }
                    }
                }
            }
            return true;
        }
        
        private final void initEnemy(final int i, final int n) {
            final int rnd = Mathtil.randi(0, n - 1);
            int enemiesNeededSum = 0;
            for (int colorIndex = 0; colorIndex < NUM_COLORS; colorIndex++) {
                enemiesNeededSum += enemiesNeeded[colorIndex];
                if (rnd < enemiesNeededSum) {
                    for (int colorAttempt = 0; colorAttempt < NUM_COLORS; colorAttempt++) {
                        final int attemptIndex = (colorIndex + colorAttempt) % NUM_COLORS;
                        cells[i] = ENEMY_TYPES[attemptIndex];
                        if (isMatched(i)) {
                            cells[i] = null;
                        } else {
                            if (enemiesNeeded[attemptIndex] > 0) {
                                enemiesNeeded[attemptIndex]--;
                            }
                            enemiesAdded++;
                            break;
                        }
                    }
                    break;
                }
            }
        }
        
        private final int getEnemyBaseSize() {
            return LEVEL_MAX * NUM_COLORS;
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            if (paused) {
                return;
            } else if (startTimer > 0) {
                startTimer--;
                return;
            }
            ENEMY_BEHAVIOR.ix = Pangine.getEngine().isOn(15) ? 8.0f : 16.0f;
            if (enemyCount <= 0) {
                onVictory();
                return;
            }
            for (final Player player : players) {
                player.onStep();
            }
            if (indicesToDrop.isEmpty()) {
                dropTimer = 0;
                stepMatch();
            } else {
                stepDrop();
            }
        }
        
        private final void stepDrop() {
            dropTimer++;
            if (dropTimer < GRID_DROP_TIME) {
                return;
            }
            dropTimer = 0;
            indicesToDropBack.clear();
            indicesDropped.clear();
            for (final Integer key : indicesToDrop) {
                final int baseIndex = key.intValue();
                if (baseIndex >= GRID_SIZE) {
                    continue;
                }
                int index = baseIndex;
                int beneath = index - GRID_W;
                if ((beneath < 0) || (cells[beneath] != null)) {
                    queueColumnForMatching(baseIndex);
                    fxThud.startSound();
                    continue;
                } else if (indicesDropped.contains(key)) {
                    continue;
                }
                while (index < GRID_SIZE) {
                    final CellType cellType = cells[index];
                    if ((cellType == null) || (cellType.behavior.b != TILE_STONE)) {
                        break;
                    } else if (index == baseIndex) {
                        indicesToDropBack.add(Integer.valueOf(beneath));
                    } else {
                        indicesDropped.add(Integer.valueOf(index));
                    }
                    cells[index] = null;
                    cells[beneath] = cellType;
                    beneath = index;
                    index += GRID_W;
                }
            }
            final Set<Integer> tmp = indicesToDrop;
            indicesToDrop = indicesToDropBack;
            indicesToDropBack = tmp;
        }
        
        private final void queueColumnForMatching(int index) {
            while (true) {
                if (index >= GRID_SIZE) {
                    break;
                }
                final CellType cellType = cells[index];
                if (cellType == null || cellType.behavior.b != TILE_STONE) {
                    break;
                }
                indicesToCheckForMatches.add(Integer.valueOf(index));
                index += GRID_W;
            }
        }
        
        private final void stepMatch() {
            for (final Integer key : indicesToCheckForMatches) {
                final int index = key.intValue();
                if (cells[index] == null) {
                    continue;
                }
                final int verticalSize = matchVertical(index, false);
                final int horizontalSize = matchHorizontal(index, false);
                if (verticalSize >= matchThreshold) {
                    indicesToClear.add(key);
                    matchVertical(index, true);
                }
                if (horizontalSize >= matchThreshold) {
                    indicesToClear.add(key);
                    matchHorizontal(index, true);
                }
            }
            indicesToCheckForMatches.clear();
            for (Integer key : indicesToClear) {
                final int index = key.intValue();
                final CellType cellType = cells[index];
                if (cellType.behavior.b == TILE_ENEMY) {
                    enemyCount--;
                }
                cells[index] = null;
                final int indexAbove = index + GRID_W;
                if (indexAbove < GRID_SIZE) {
                    final CellType cellAbove = cells[indexAbove];
                    if (cellAbove != null && cellAbove.behavior.b == TILE_STONE) {
                        indicesToDrop.add(Integer.valueOf(indexAbove));
                    }
                }
                burst(index, cellType);
                fxMatch.startSound();
            }
            indicesToClear.clear();
        }
        
        private final int matchVertical(final int index, final boolean clear) {
            final CellType baseType = cells[index];
            int current = index, size = 1;
            while (true) {
                current -= GRID_W;
                if (current < 0) {
                    break;
                } else if (matchCell(baseType, current, clear)) {
                    size++;
                } else {
                    break;
                }
            }
            current = index;
            while (true) {
                current += GRID_W;
                if (current >= GRID_SIZE) {
                    break;
                } else if (matchCell(baseType, current, clear)) {
                    size++;
                } else {
                    break;
                }
            }
            return size;
        }
        
        private final int matchHorizontal(final int index, final boolean clear) {
            final CellType baseType = cells[index];
            final int min = (index / GRID_W) * GRID_W, max = min + GRID_W;
            int current = index, size = 1;
            while (true) {
                current--;
                if (current < min) {
                    break;
                } else if (matchCell(baseType, current, clear)) {
                    size++;
                } else {
                    break;
                }
            }
            current = index;
            while (true) {
                current++;
                if (current >= max) {
                    break;
                } else if (matchCell(baseType, current, clear)) {
                    size++;
                } else {
                    break;
                }
            }
            return size;
        }
        
        private final boolean matchCell(final CellType baseType, final int current, final boolean clear) {
            final CellType neighborType = cells[current];
            if (baseType.isSameColor(neighborType)) {
                if (clear) {
                    indicesToClear.add(Integer.valueOf(current));
                }
                return true;
            }
            return false;
        }
        
        protected final boolean isMatched(final int index) {
            return (matchVertical(index, false) >= matchThreshold) || (matchHorizontal(index, false) >= matchThreshold);
        }
        
        protected final void togglePause(final int playerIndex) {
            if (paused) {
                if (pauser == playerIndex) {
                    Panctor.destroy(pauseMenu);
                } else {
                    return;
                }
            }
            fxMatch.startSound();
            paused = !paused;
            if (paused) {
                Pangine.getEngine().getAudio().pauseMusic();
                pauser = playerIndex;
                new PauseMenu(playerIndex);
            } else {
                hideCursor();
                try {
                    Pangine.getEngine().getAudio().resumeMusic();
                } catch (final Exception e) {
                    throw Panception.get(e);
                }
            }
        }
        
        protected final boolean isFree() {
            return !paused && (startTimer <= 0) && indicesToDrop.isEmpty() && indicesToCheckForMatches.isEmpty();
        }
        
        protected final int getX(final int index) {
            return index % GRID_W;
        }
        
        protected final int getY(final int index) {
            return index / GRID_W;
        }
        
        protected final int getIndex(final int x, final int y) {
            return (y * GRID_W) + x;
        }
        
        protected final CellType getCell(final int x, final int y) {
            return cells[getIndex(x, y)];
        }
        
        private final CellType setCell(final int x, final int y, final CellType type) {
            final int index = getIndex(x, y);
            final CellType oldCell = cells[index];
            if (oldCell != null) {
                return oldCell;
            }
            cells[index] = type;
            final Integer key = Integer.valueOf(index);
            if (isOccupied(x, y - 1)) {
                indicesToCheckForMatches.add(key);
            } else {
                indicesToDrop.add(key);
            }
            return null;
        }
        
        protected final boolean placeStone(final int x, int y, final CellType type) {
            while (setCell(x, y, type) != null) {
                y++;
                if (y >= GRID_H) {
                    return false;
                }
            }
            return true;
        }
        
        protected final void onVictory() {
            fxVictory.startSound();
            level++;
            Panscreen.set(new LevelCompleteScreen());
        }
        
        protected final void onDefeated() {
            fxDefeat.startSound();
            final int waitTimer = 90;
            startTimer = waitTimer * 2;
            Pangine.getEngine().getAudio().stopMusic();
            Pangine.getEngine().addTimer(this, waitTimer, new TimerListener() {
                @Override public final void onTimer(final TimerEvent event) {
                    Panscreen.set(new GameOverScreen());
                }});
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            int x = 0, y = 0;
            for (int i = 0; i < GRID_SIZE; i++) {
                final CellType type = cells[i];
                if (type != null) {
                    render(renderer, x, y, Z_GRID, type);
                }
                x++;
                if (x >= GRID_W) {
                    x = 0;
                    y++;
                }
            }
        }
    }
    
    protected final static class CellBehavior {
        private float ix;
        private final float iy;
        private final byte b;
        
        protected CellBehavior(final float ix, final float iy, final byte b) {
            this.ix = ix;
            this.iy = iy;
            this.b = b;
        }
    }
    
    protected final static class CellColor {
        private final float r, g, b;
        protected Panimation anmBurst = null;
        
        protected CellColor(final float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }
    
    protected final static class CellType {
        private final CellColor color;
        private final CellBehavior behavior;
        
        protected CellType(final CellColor color, final CellBehavior behavior) {
            this.color = color;
            this.behavior = behavior;
        }
        
        protected final boolean isSameColor(final CellType other) {
            if (other == null) {
                return false;
            }
            return color == other.color;
        }
    }
    
    protected final static class Player extends Panctor {
        private final int playerIndex;
        private final int otherPlayerIndex;
        private final int startX;
        private int timer = FALL_TIME;
        private int stoneX;
        private int stoneY;
        private int stoneRot = ROT_EAST;
        private CellType stoneType = null;
        private CellType otherStoneType = null;
        private CellType nextStoneType = null;
        private CellType nextOtherStoneType = null;
        private int nextDir = 0;
        private int nextRot = 0;
        private int moveTimer = 0;
        
        protected Player(final int startX) {
            playerIndex = players.size();
            otherPlayerIndex = (playerIndex + 1) % MAX_PLAYERS;
            this.startX = startX;
            players.add(this);
            room.addActor(this);
            pickRandomStones();
        }
        
        protected final void register(final ControlScheme scheme) {
            register(scheme.get1(), new ActionStartListener() {
                @Override public final void onActionStart(final ActionStartEvent event) { onClockwise(); }});
            register(scheme.get2(), new ActionStartListener() {
                @Override public final void onActionStart(final ActionStartEvent event) { onCounterclockwise(); }});
            register(scheme.getLeft(), new ActionStartListener() {
                @Override public final void onActionStart(final ActionStartEvent event) { onLeftStart(); }});
            register(scheme.getLeft(), new ActionListener() {
                @Override public final void onAction(final ActionEvent event) { onLeftContinue(); }});
            register(scheme.getRight(), new ActionStartListener() {
                @Override public final void onActionStart(final ActionStartEvent event) { onRightStart(); }});
            register(scheme.getRight(), new ActionListener() {
                @Override public final void onAction(final ActionEvent event) { onRightContinue(); }});
            register(scheme.getDown(), new ActionListener() {
                @Override public final void onAction(final ActionEvent event) { onDown(); }});
            register(scheme.getSubmit(), new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) { onPause(); }});
        }
        
        protected final void pickRandomStones() {
            nextStoneType = randomStone();
            nextOtherStoneType = randomStone();
        }
        
        protected final void startNextStones() {
            stoneX = startX;
            stoneY = NEXT_Y - 1;
            stoneRot = ROT_EAST;
            stoneType = nextStoneType;
            otherStoneType = nextOtherStoneType;
            timer = FALL_TIME;
            clearNextMove();
            pickRandomStones();
            puff(startX, NEXT_Y);
            puff(startX + 1, NEXT_Y);
            controlSchemes.get(playerIndex).getDown().inactivate();
        }
        
        private final void clearNextMove() {
            nextDir = 0;
            nextRot = 0;
        }
        
        private final boolean isMoveForbidden() {
            return (stoneY >= GRID_H) || grid.paused || (stoneType == null);
        }
        
        private final void onMoveStart(final int dir) {
            if (isMoveForbidden()) {
                return;
            }
            nextDir = dir;
            moveTimer = MOVE_HOLD_TIME;
            fxMove.startSound();
        }
        
        private final void onMoveContinue(final int dir) {
            if (isMoveForbidden()) {
                return;
            }
            moveTimer--;
            if (moveTimer <= 0) {
                nextDir = dir;
                moveTimer = MOVE_FAST_TIME;
            }
        }
        
        protected final void onLeftStart() {
            onMoveStart(-1);
        }
        
        protected final void onLeftContinue() {
            onMoveContinue(-1);
        }
        
        protected final void left() {
            stoneX--;
            if (isEitherFallingStoneOccupied()) {
                stoneX++;
            } else {
                slowDrop();
            }
        }
        
        protected final void onRightStart() {
            onMoveStart(1);
        }
        
        protected final void onRightContinue() {
            onMoveContinue(1);
        }
        
        protected final void right() {
            stoneX++;
            if (isEitherFallingStoneOccupied()) {
                stoneX--;
            } else {
                slowDrop();
            }
        }
        
        private final void onRotate(final int dir) {
            if (isMoveForbidden()) {
                return;
            }
            nextRot = dir;
            fxRotate.startSound();
        }
        
        protected final void onClockwise() {
            onRotate(1);
        }
        
        protected final void clockwise() {
            final int oldX = stoneX, oldRot = stoneRot;
            if (stoneRot >= ROT_MAX) {
                stoneRot = ROT_MIN;
            } else {
                stoneRot++;
            }
            adjustRotationIfNeeded(oldX, oldRot);
        }
        
        protected final void onCounterclockwise() {
            onRotate(-1);
        }
        
        protected final void counterclockwise() {
            final int oldX = stoneX, oldRot = stoneRot;
            if (stoneRot <= ROT_MIN) {
                stoneRot = ROT_MAX;
            } else {
                stoneRot--;
            }
            adjustRotationIfNeeded(oldX, oldRot);
        }
        
        protected final void onDown() {
            if (grid.paused) {
                return;
            } else if (timer > FAST_TIME) {
                timer = FAST_TIME;
            }
        }
        
        private final void slowDrop() {
            timer = Math.min(timer + 2, FALL_TIME);
        }
        
        protected final void onPause() {
            grid.togglePause(playerIndex);
        }

        public final void onStep() {
            if (stoneType != null) {
                stepControl();
                stepDrop();
            } else if (grid.isFree()) {
                startNextStones();
            }
        }
        
        private final void stepControl() {
            if (nextDir < 0) {
                left();
            } else if (nextDir > 0) {
                right();
            }
            if (nextRot > 0) {
                clockwise();
            } else if (nextRot < 0) {
                counterclockwise();
            }
            clearNextMove();
        }
        
        private final void stepDrop() {
            timer--;
            if (timer <= 0) {
                decrementFallingStones();
                timer = FALL_TIME;
            }
        }
        
        protected final void decrementFallingStones() {
            if (isFallFinished(stoneX, stoneY) || isFallFinished(getOtherStoneX(), getOtherStoneY())) {
                finishFall();
                return;
            }
            stoneY--;
        }
        
        protected final boolean isFallFinished(final int x, final int y) {
            return isOccupied(x, y) || isOccupied(x, y - 1);
        }
        
        protected final void finishFall() {
            if (stoneY >= GRID_H) {
                grid.onDefeated();
            }
            final int otherY = getOtherStoneY();
            if (otherY >= GRID_H) {
                grid.onDefeated();
            } else if (!(grid.placeStone(stoneX, stoneY, stoneType) && grid.placeStone(getOtherStoneX(), otherY, otherStoneType))) {
                grid.onDefeated();
            }
            stoneType = null;
            fxThud.startSound();
        }
        
        protected final void adjustRotationIfNeeded(final int oldX, final int oldRot) {
            slowDrop();
            fixStoneX();
            if (isEitherFallingStoneOccupied() || isOtherStoneTooHigh()) {
                stoneX = oldX;
                stoneRot = oldRot;
                // Try another rot; north-to-south or east-to-west always work if we adjust position too
                if (stoneRot == ROT_EAST) {
                    stoneRot = ROT_WEST;
                    stoneX++;
                } else if (stoneRot == ROT_WEST) {
                    stoneRot = ROT_EAST;
                    stoneX--;
                } else if (stoneRot == ROT_NORTH) {
                    stoneRot = ROT_SOUTH;
                    stoneY++;
                } else if (stoneRot == ROT_SOUTH) {
                    stoneRot = ROT_NORTH;
                    stoneY--;
                }
            }
        }
        
        protected final void fixStoneX() {
            stoneX = Math.min(Math.max(stoneX, getMinStoneX()), getMaxStoneX());
        }
        
        private final boolean isOtherStoneTooHigh() {
            return (stoneRot == ROT_NORTH) && (stoneY >= GRID_H - 1) && (grid.enemyCount <= 72);
        }
        
        protected final int getOtherStoneX() {
            if (stoneRot == ROT_EAST) {
                return stoneX + 1;
            } else if (stoneRot == ROT_WEST) {
                return stoneX - 1;
            }
            return stoneX;
        }
        
        protected final int getOtherStoneY() {
            if (stoneRot == ROT_NORTH) {
                return stoneY + 1;
            } else if (stoneRot == ROT_SOUTH) {
                return stoneY - 1;
            }
            return stoneY;
        }
        
        protected final int getMinStoneX() {
            return (stoneRot == ROT_WEST) ? 1 : 0;
        }
        
        protected final int getMaxStoneX() {
            return GRID_W - ((stoneRot == ROT_EAST) ? 2 : 1);
        }
        
        protected final boolean isEitherFallingStoneOccupied() {
            return isOccupied(stoneX, stoneY) || isOccupied(getOtherStoneX(), getOtherStoneY());
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final int z = Z_FALLINGS[Pangine.getEngine().isOn(8) ? playerIndex : otherPlayerIndex];
            if (stoneType != null) {
                render(renderer, stoneX, stoneY, z, stoneType);
                render(renderer, getOtherStoneX(), getOtherStoneY(), z, otherStoneType);
            }
            render(renderer, startX, NEXT_Y, z, nextStoneType);
            render(renderer, startX + 1, NEXT_Y, z, nextOtherStoneType);
        }
    }
    
    protected final static void render(final Panderer renderer, final int x, final int y, final int z, final CellType type) {
        final CellBehavior b = type.behavior;
        final CellColor c = type.color;
        renderer.render(room, block, X + (x * DIM), Y + (y * DIM), z, b.ix, b.iy, DIM, DIM, 0, false, false, c.r, c.g, c.b);
    }
    
    protected abstract static class BasePauseMenu extends Panctor {
        private final static int PAUSE_MENU_W = 64;
        private final static int OFF_TEXT = 12;
        protected final int playerIndex;
        private final List<Pantext> texts = new ArrayList<Pantext>();
        private final List<TouchButton> buttons = new ArrayList<TouchButton>();
        private final Pantext cursor;
        private int cursorIndex = 0;
        
        protected BasePauseMenu(final int playerIndex) {
            this.playerIndex = playerIndex;
            final float x = (playerIndex == 0) ? DIM : (GAME_W - 72);
            getPosition().set(x, DIM, Z_GRID);
            final float xText = x + OFF_TEXT;
            texts.add(BackgroundScreen.addText(getTitle(), xText, 96));
            addOption(get0(), 80, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) { on0(); }});
            addOption(get1(), 64, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) { on1(); }});
            cursor = BackgroundScreen.addText(getCursorCharacter(), x + 4, -DIM); // Next line will set y
            setCursorPosition();
            texts.add(cursor);
            register(controlSchemes.get(playerIndex));
            room.addActor(this);
            grid.pauseMenu = this;
        }
        
        private final void addOption(final CharSequence msg, final int y, final ActionEndListener listener) {
            final float x = getPosition().getX();
            texts.add(BackgroundScreen.addText(msg, x + OFF_TEXT, y));
            buttons.add(newTouchButton(this, Math.round(x), y, PAUSE_MENU_W, DIM_TEXT, listener));
        }
        
        protected final void register(final ControlScheme scheme) {
            registerMenuUpDown(this, scheme,
                new ActionEndListener() { @Override public final void onActionEnd(final ActionEndEvent event) { onY(); }},
                new ActionEndListener() { @Override public final void onActionEnd(final ActionEndEvent event) { onY(); }});
            registerMenu12(this, scheme, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) { onAction(); }});
        }
        
        protected final void onY() {
            cursorIndex = (cursorIndex + 1) % 2;
            setCursorPosition();
            fxMove.startSound();
            hideCursor();
        }
        
        private final void setCursorPosition() {
            cursor.getPosition().setY(texts.get(cursorIndex + 1).getPosition().getY()); // 0 is the title, so add 1
        }
        
        protected final void onAction() {
            if (cursorIndex == 0) {
                on0();
            } else {
                on1();
            }
        }
        
        protected abstract String getTitle();
        
        protected abstract String get0();
        
        protected abstract String get1();
        
        protected abstract void on0();
        
        protected abstract void on1();
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panple pos = getPosition();
            renderer.render(getLayer(), black, pos.getX(), pos.getY(), pos.getZ(), 0, 0, PAUSE_MENU_W, GAME_H - (DIM * 2), 0, false, false);
        }
        
        @Override
        protected final void onDestroy() {
            for (final Pantext text : texts) {
                text.destroy();
            }
            final Pangine engine = Pangine.getEngine();
            for (final TouchButton button : buttons) {
                engine.unregisterTouchButton(button);
            }
        }
    }
    
    protected final static class PauseMenu extends BasePauseMenu {
        protected PauseMenu(final int playerIndex) {
            super(playerIndex);
        }
        
        @Override protected final String getTitle() {
            return "Paused";
        }
        
        @Override protected final String get0() {
            return "Play";
        }
        
        @Override protected final String get1() {
            return "Quit";
        }
        
        @Override protected final void on0() {
            grid.togglePause(playerIndex);
            destroy();
        }
        
        @Override protected final void on1() {
            fxRotate.startSound();
            new QuitPauseMenu(playerIndex);
            destroy();
        }
    }
    
    protected final static class QuitPauseMenu extends BasePauseMenu {
        protected QuitPauseMenu(final int playerIndex) {
            super(playerIndex);
        }
        
        @Override protected final String getTitle() {
            return "Quit?";
        }
        
        @Override protected final String get0() {
            return "No";
        }
        
        @Override protected final String get1() {
            return "Yes";
        }
        
        @Override protected final void on0() {
            fxRotate.startSound();
            new PauseMenu(playerIndex);
            destroy();
        }
        
        @Override protected final void on1() {
            fxRotate.startSound();
            Panscreen.set(new MenuScreen());
        }
    }
    
    public final static void main(final String[] args) {
        try {
            new BlockGame().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
