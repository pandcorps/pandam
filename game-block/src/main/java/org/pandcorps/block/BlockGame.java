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
    protected final static String COPYRIGHT = "Copyright " + Pantext.CHAR_COPYRIGHT + " " + YEAR;
    
    protected final static String RES = "org/pandcorps/block/";
    
    protected final static int DIM = 8;
    protected final static int GAME_W = 288;
    protected final static int GAME_H = 160;
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
    protected final static int Z_FALLING = 4;
    protected final static int Z_BURST = 6;
    protected final static int Z_PUFF = 8;
    
    protected final static int FALL_TIME = 30;
    protected final static int FAST_TIME = 2;
    protected final static int GRID_DROP_TIME = 8;
    protected final static int MOVE_HOLD_TIME = 12;
    protected final static int MOVE_FAST_TIME = 2;
    
    protected final static int MATCH_THRESHOLD = 3;
    
    protected final static byte TILE_STONE = Tile.BEHAVIOR_SOLID; // Falls when tiles beneath are cleared
    protected final static byte TILE_ENEMY = 2; // Stays in its place until cleared
    
    protected final static int ROT_EAST = 0;
    protected final static int ROT_SOUTH = 1;
    protected final static int ROT_WEST = 2;
    protected final static int ROT_NORTH = 3;
    protected final static int ROT_MIN = 0;
    protected final static int ROT_MAX = 3;
    
    protected final static int NUM_COLORS = 3;
    protected final static int NUM_PLAYERS = 2;
    
    protected final static int LEVEL_MIN = 1; // Level n will have 3n enemies (n of each color)
    protected final static int LEVEL_MAX = 64;
    
    protected static Queue<Runnable> loaders = new LinkedList<Runnable>();
    protected static Panmage block = null;
    protected static Panmage black = null;
    protected static Panimation anmPuff = null;
    protected static Font font = null;
    protected final static Panple size = new FinPanple2(DIM, DIM);
    protected final static CellBehavior STONE_BEHAVIOR = new CellBehavior(0.0f, 0.0f, TILE_STONE);
    protected final static CellBehavior ENEMY_BEHAVIOR = new CellBehavior(8.0f, 0.0f, TILE_ENEMY);
    protected static CellType[] STONE_TYPES = new CellType[NUM_COLORS];
    protected static CellType[] ENEMY_TYPES = new CellType[NUM_COLORS];
    protected static Panroom room = null;
    protected static Background background = null;
    protected static Grid grid = null;
    protected final static List<ControlScheme> controlSchemes = new ArrayList<ControlScheme>(NUM_PLAYERS);
    protected final static List<Player> players = new ArrayList<Player>(NUM_PLAYERS);
    private static int level = LEVEL_MIN;
    
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
        Panscreen.set(new LogoScreen(LevelStartScreen.class, loaders));
    }
    
    private final static void loadResources() {
        block = Pangine.getEngine().createImage("block", RES + "Block.png");
        final ImgFactory f = ImgFactory.getFactory();
        final Img img = f.create(1, 1);
        img.setRGB(0, 0, f.getDataElement(0, 0, 0, 255));
        black = Pangine.getEngine().createImage("black", img);
        initColor(0, block, 0.5f, 1.0f, 1.0f);
        initColor(1, block, 0.0f, 0.5f, 1.0f);
        initColor(2, block, 0.375f, 0.375f, 0.375f);
        anmPuff = newAnimation(3, 0.75f, 1.0f, 1.0f, 16, 16, 16, 24, 24, 24);
        font = Fonts.getClassic(new FontRequest(FontType.Upper, 8), new FinPancolor(192, Pancolor.MAX_VALUE, Pancolor.MAX_VALUE));
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
    
    private final static CellType newEnemy(final CellColor color) {
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
    
    protected abstract static class BackgroundScreen extends Panscreen {
        @Override
        protected final void load() throws Exception {
            final Pangine engine = Pangine.getEngine();
            engine.enableColorArray();
            room = Pangame.getGame().getCurrentRoom();
            final Panlayer bg = engine.createLayer("bg", GAME_W, GAME_H, room.getSize().getZ(), room);
            if (background == null) {
                background = new Background();
            }
            bg.addActor(background);
            bg.setConstant(true);
            room.addBeneath(bg);
            room.setClearDepthEnabled(false);
        }
        
        protected abstract void loadBlock();
        
        @Override
        protected final void destroy() {
            background.detach();
        }
    }
    
    protected final static class BlockScreen extends BackgroundScreen {
        @Override
        protected final void loadBlock() {
            grid = new Grid();
            players.clear();
            for (final ControlScheme ctrl : controlSchemes) {
                new Player().register(ctrl);
            }
        }
    }
    
    protected abstract static class TextScreen extends BackgroundScreen {
        protected Pantext text = null;
        
        @Override
        protected final void loadBlock() {
            text = new Pantext(Pantil.vmid(), font, getText());
            text.getPosition().set(GAME_W / 2, GAME_H / 2, Z_PUFF);
            text.centerX();
            room.addActor(text);
            final Panscreen nextScreen = getNextScreen();
            loadText();
            if (nextScreen == null) {
                return;
            }
            Pangine.getEngine().addTimer(text, 30, new TimerListener() {
                @Override public final void onTimer(final TimerEvent event) {
                    Panscreen.set(nextScreen);
                }});
        }
        
        protected abstract String getText();
        
        protected void loadText() {
        }
        
        protected Panscreen getNextScreen() {
            return null;
        }
    }
    
    protected final static class DeviceSelectScreen extends TextScreen {
        @Override
        protected final String getText() {
            return "Press any button player " + (controlSchemes.size() + 1);
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
                            return;
                        }
                    }
                    controlSchemes.add(ControlScheme.getDefault(device));
                    if (controlSchemes.size() < NUM_PLAYERS) {
                        Panscreen.set(new DeviceSelectScreen());
                    }
                    //elseStartGame;
                }});
        }
    }
    
    protected final static class LevelStartScreen extends TextScreen {
        @Override
        protected final String getText() {
            return "Level " + level + " start";
        }
        
        @Override
        protected final Panscreen getNextScreen() {
            return new BlockScreen();
        }
    }
    
    protected final static class LevelCompleteScreen extends TextScreen {
        @Override
        protected final String getText() {
            return "Level " + (level - 1) + " complete"; // Level incremented before this screen
        }
        
        @Override
        protected final Panscreen getNextScreen() {
            if (level >= LEVEL_MAX) {
                return new VictoryScreen();
            }
            return new LevelStartScreen();
        }
    }
    
    protected final static class VictoryScreen extends TextScreen {
        @Override
        protected final String getText() {
            return "You win!";
        }
        
        @Override
        protected final Panscreen getNextScreen() {
            level = 1;
            return new LevelStartScreen(); //TODO Menu screen
        }
    }
    
    protected final static class GameOverScreen extends TextScreen {
        @Override
        protected final String getText() {
            return "Game over";
        }
        
        @Override
        protected final Panscreen getNextScreen() {
            level = 1;
            return new LevelStartScreen(); //TODO Menu screen
        }
    }
    
    protected final static class Background extends Panctor {
        private final static Random rand = new Random();
        private final static long seed = Mathtil.newSeed();
        
        protected final void renderView(final Panderer renderer) {
            final Panlayer layer = getLayer();
            rand.setSeed(seed);
            final int topNext = GAME_H - DIM;
            final int topGrid = topNext - (DIM * 2);
            final float r = 0.0f, g = 0.25f, b = 0.5f;
            for (int j = 0; j < 2; j++) {
                for (int i = 0; i < 8; i++) {
                    final int x = (GRID_HALF * DIM * j) + X + (DIM * i), y = ((i >= 3) && (i <= 4)) ? topNext : topGrid;
                    if (i == 2) {
                        renderer.render(layer, block, x, y, Z_BG, 16, 8, DIM, DIM, 0, false, false, r, g, b);
                        renderer.render(layer, block, x, y + DIM, Z_BG, 8, 8, DIM, DIM, 1, false, false, r, g, b);
                        renderer.render(layer, block, x, topNext, Z_BG, 0, 8, DIM, DIM, 0, false, false, r, g, b);
                    } else if (i == 5) {
                        renderer.render(layer, block, x, y, Z_BG, 16, 8, DIM, DIM, 3, false, false, r, g, b);
                        renderer.render(layer, block, x, y + DIM, Z_BG, 8, 8, DIM, DIM, 3, false, false, r, g, b);
                        renderer.render(layer, block, x, topNext, Z_BG, 0, 8, DIM, DIM, 3, false, false, r, g, b);
                    } else {
                        renderer.render(layer, block, x, y, Z_BG, 8, 8, DIM, DIM, 0, false, false, r, g, b);
                    }
                    renderer.render(layer, block, x, 0, Z_BG, 8, 8, DIM, DIM, 2, false, false, r, g, b);
                }
            }
            final int left = X - DIM, right = X + (GRID_W * DIM);
            for (int i = 0; i < GRID_H; i++) {
                renderer.render(layer, block, left, Y + (DIM * i), Z_BG, 8, 8, DIM, DIM, 1, false, false, r, g, b);
                renderer.render(layer, block, right, Y + (DIM * i), Z_BG, 8, 8, DIM, DIM, 3, false, false, r, g, b);
            }
            renderer.render(layer, block, left, topGrid, Z_BG, 0, 8, DIM, DIM, 0, false, false, r, g, b);
            renderer.render(layer, block, right, topGrid, Z_BG, 0, 8, DIM, DIM, 3, false, false, r, g, b);
            renderer.render(layer, block, left, 0, Z_BG, 0, 8, DIM, DIM, 1, false, false, r, g, b);
            renderer.render(layer, block, right, 0, Z_BG, 0, 8, DIM, DIM, 2, false, false, r, g, b);
            final int rightBg = right + DIM, rightNext = rightBg - (DIM * 3), rightFar = GAME_W - DIM;
            final int xMid = (GAME_W / 2) - (DIM * 2);
            final int topBg = topNext - DIM;
            render8s(renderer, layer, 0, 0, 9, 1);
            render8s(renderer, layer, rightBg, 0, 9, 1);
            render8s(renderer, layer, 0, 1, 1, 19);
            render8s(renderer, layer, rightFar, 1, 1, 19);
            render8s(renderer, layer, 0, topNext, 12, 1);
            render8s(renderer, layer, rightNext, topNext, 12, 1);
            render8s(renderer, layer, xMid, topBg, 4, 2);
            render8s(renderer, layer, left, topBg, 3, 1);
            render8s(renderer, layer, rightNext, topBg, 3, 1);
            render16s(renderer, layer, DIM, DIM, 4, 9);
            render16s(renderer, layer, rightBg, DIM, 4, 9);
            renderer.render(layer, black, X, Y, Z_BG, 0, 0, 128, 128, 0, false, false);
            renderer.render(layer, black, 104, topGrid, Z_BG, 0, 0, 16, 16, 0, false, false);
            renderer.render(layer, black, 168, topGrid, Z_BG, 0, 0, 16, 16, 0, false, false);
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
                    final float b = Mathtil.randf(rand, 0.25f, 1.0f);
                    final float g = Mathtil.randf(rand, 0.0f, b);
                    final float r = Mathtil.randf(rand, 0.0f, Math.min(g, 0.75f * b));
                    renderer.render(layer, block, x + (i * df), yj, Z_BG, ix, iy, df, df, 0, false, false, r, g, b);
                }
            }
        }
    }
    
    protected final static class Grid extends Panctor implements StepListener {
        private final CellType[] cells = new CellType[GRID_SIZE];
        private Set<Integer> indicesToDrop = new TreeSet<Integer>();
        private Set<Integer> indicesToDropBack = new TreeSet<Integer>();
        private Set<Integer> indicesDropped = new HashSet<Integer>();
        private final Set<Integer> indicesToCheckForMatches = new TreeSet<Integer>();
        private final Set<Integer> indicesToClear = new HashSet<Integer>();
        private int dropTimer = 0;
        private int enemyCount = 0;
        private final int[] enemiesNeeded = new int[NUM_COLORS];
        private int enemiesAdded = 0;
        
        protected Grid() {
            room.addActor(this);
            initEnemies();
        }
        
        protected final void initEnemies() {
            while (!attemptInitEnemies());
        }
        
        private final boolean attemptInitEnemies() {
            enemyCount = level * NUM_COLORS;
            final int baseSize = LEVEL_MAX * NUM_COLORS;
            for (int i = 0; i < NUM_COLORS; i++) {
                enemiesNeeded[i] = level;
            }
            int n = baseSize;
            enemiesAdded = 0;
            for (int i = 0; i < baseSize; i++) {
                initEnemy(i, n);
                n--;
            }
            if (enemiesAdded < enemyCount) {
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
            final int rnd = Mathtil.randi(0, n);
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
                            enemiesNeeded[attemptIndex]--;
                            enemiesAdded++;
                            break;
                        }
                    }
                    break;
                }
            }
        }
        
        @Override
        public final void onStep(final StepEvent event) {
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
                final int verticalSize = matchVertical(index, false);
                final int horizontalSize = matchHorizontal(index, false);
                if (verticalSize >= MATCH_THRESHOLD) {
                    indicesToClear.add(key);
                    matchVertical(index, true);
                }
                if (horizontalSize >= MATCH_THRESHOLD) {
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
        
        private final boolean isMatched(final int index) {
            return (matchVertical(index, false) >= MATCH_THRESHOLD) || (matchHorizontal(index, false) >= MATCH_THRESHOLD);
        }
        
        protected final boolean isFree() {
            return indicesToDrop.isEmpty() && indicesToCheckForMatches.isEmpty();
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
            level++;
            Panscreen.set(new LevelCompleteScreen());
        }
        
        protected final void onDefeated() {
            Panscreen.set(new GameOverScreen());
        }
        
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
        
        protected Player() {
            startX = (GRID_HALF * players.size()) + (GRID_HALF / 2) - 1;
            players.add(this);
            room.addActor(this);
            pickRandomStones();
            startNextStones();
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
            pickRandomStones();
            puff(startX, NEXT_Y);
            puff(startX + 1, NEXT_Y);
        }
        
        private final void onMoveStart(final int dir) {
            if (stoneY >= GRID_H) {
                return;
            }
            nextDir = dir;
            moveTimer = MOVE_HOLD_TIME;
        }
        
        private final void onMoveContinue(final int dir) {
            if (stoneY >= GRID_H) {
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
            }
        }
        
        protected final void onClockwise() {
            nextRot = 1;
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
            nextRot = -1;
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
            if (timer > FAST_TIME) {
                timer = FAST_TIME;
            }
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
            nextDir = 0;
            nextRot = 0;
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
        }
        
        protected final void adjustRotationIfNeeded(final int oldX, final int oldRot) {
            fixStoneX();
            if (isEitherFallingStoneOccupied()) {
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
        
        protected final void renderView(final Panderer renderer) {
            if (stoneType != null) {
                render(renderer, stoneX, stoneY, Z_FALLING, stoneType);
                render(renderer, getOtherStoneX(), getOtherStoneY(), Z_FALLING, otherStoneType);
            }
            render(renderer, startX, NEXT_Y, Z_FALLING, nextStoneType);
            render(renderer, startX + 1, NEXT_Y, Z_FALLING, nextOtherStoneType);
        }
    }
    
    protected final static void render(final Panderer renderer, final int x, final int y, final int z, final CellType type) {
        final CellBehavior b = type.behavior;
        final CellColor c = type.color;
        renderer.render(room, block, X + (x * DIM), Y + (y * DIM), z, b.ix, b.iy, DIM, DIM, 0, false, false, c.r, c.g, c.b);
    }
    
    public final static void main(final String[] args) {
        try {
            new BlockGame().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
