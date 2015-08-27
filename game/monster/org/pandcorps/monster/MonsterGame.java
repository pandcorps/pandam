/*
Copyright (c) 2009-2014, Andrew M. Martin
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
package org.pandcorps.monster;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.game.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.game.core.*;
import org.pandcorps.monster.Driver.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Fonts.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.pandax.touch.*;
import org.pandcorps.pandax.visual.*;

public final class MonsterGame extends BaseGame {
    /*
    Breed/trade don't show options already on team
    Show disabled Catch if on team
    Show player's money on buy/sell
    Show container quantity on Battle screen
    Show total money/experience when earning it
    Database screen move to front after selecting favorite (or back after selecting least)
    Test that impossible options still appear as buildings handled gracefully
    Auto-save
    Full screen, menu centering
    Swipe velocity/acceleration
    Library
    Externalize Breeder name
    Validate that all items/locations/etc. have images
    Validate experience upgrades (or auto-derive)
    */
    private static volatile Driver driver = null;
    private static volatile Panroom room = null;
    private static Panlayer layerTiles = null;
    private static Panlayer layerSprites = null;
    private static Panlayer layerHud = null;
    
    private static volatile MultiFont font = null;
    private static volatile MultiFont fontTiny = null;
    
    private final static int IMG_W = 80;
    private final static int IMG_H = 80;
    private final static int MENU_W = 85;
    private final static int MENU_H = 93;
    private final static int OVERLAY_X = 3;
    private final static int OVERLAY_Y = 10;
    private final static int TEXT_X = 3;
    private final static int TEXT_Y = 2;
    private final static int TW = 16;
    private final static int TH = 16;
    private final static int DEPTH_BUTTON = 20;
    private static int DIM_BUTTON = 0;
    private final static int WILD_ENCOUNTER_RATE = 25;
    private static int imgOffX = 0;
    private static int imgOffY = 0;
    private static volatile Panmage menu = null;
    private static volatile Panmage menuIn = null;
    private static volatile Panmage menuOff = null;
    private static volatile Panmage menuCursor = null;
    //private static volatile Panmage menuLeft = null;
    //private static volatile Panmage menuRight = null;
    private static Panmage menuFull = null;
    private static Panmage menuFullTranslucent = null;
    private static Panmage speciesAll = null;
    private static Panmage speciesAllTranslucent = null;
    private final static Map<String, Panmage> imageCache = new HashMap<String, Panmage>();
    private static Panimation playerSouth = null;
    private static Panimation playerEast = null;
    private static Panimation playerNorth = null;
    private static Panimation playerWest = null;
    private static Panimation[] playerWalks = null;
    private static Panmage diamond = null;
    private static Panmage diamondIn = null;
    private static Panmage tiles = null;
    private static Img worldSrc = null;
    private static ControlScheme ctrl = null;
    
    @Override
    protected final void init(final Panroom room) throws Exception {
        MonsterGame.room = room;
        //loadConstants();
        //new Thread(driver).start();
        Panscreen.set(new LoadScreen());
    }
    
    private final static class LoadScreen extends FadeScreen {
        private final StringBuilder name = new StringBuilder("Monster Catching engine test");
        
        private LoadScreen() {
            super(Pancolor.WHITE, 30);
            final Queue<Runnable> loaders = new LinkedList<Runnable>();
            loaders.add(new Runnable() {
                @Override public final void run() {
                    loadConstants();
                }});
            setBackgroundTasks(loaders);
        }
        
        @Override
        protected final void start() {
            final Pangine engine = Pangine.getEngine();
            engine.setBgColor(Pancolor.WHITE);
            font = Fonts.getClassics(new FontRequest(8), Pancolor.WHITE, Pancolor.BLACK);
            final Pantext text = new Pantext("PandcorpsLogo", font, name);
            text.getPosition().set(8, engine.getEffectiveHeight() - 16);
            Pangame.getGame().getCurrentRoom().addActor(text);
        }
        
        @Override
        protected final void onLoading() {
            Chartil.set(name, "LOADING");
            c.getLayer().getBlendColor().setA(Mathtil.SHORT_0);
        }
        
        @Override
        protected final void finish() {
            new Thread(driver).start();
        }
        
        @Override
        protected final void destroy() {
            //Panmage.destroy(font);
        }
    }
    
    private final static void loadConstants() {
        final Pangine engine = Pangine.getEngine();
        //font = Fonts.getClassics(new FontRequest(8), Pancolor.WHITE, Pancolor.BLACK);
        fontTiny = Fonts.getTinies(FontType.Byte, Pancolor.WHITE, Pancolor.BLACK);
        menu = engine.createImage(Pantil.vmid(), ImtilX.newButton(MENU_W, MENU_H, Pancolor.GREY));
        menuIn = engine.createImage(Pantil.vmid(), ImtilX.newButton(MENU_W, MENU_H, Pancolor.CYAN));
        menuOff = engine.createImage(Pantil.vmid(), ImtilX.newButton(MENU_W, MENU_H, Pancolor.DARK_GREY));
        if (engine.isMouseSupported()) {
            menuCursor = engine.createImage(Pantil.vmid(), ImtilX.newUp2(16, Pancolor.WHITE));
        }
        //menuLeft = engine.createImage(Pantil.vmid(), ImtilX.newLeft2(80, Pancolor.BLUE));
        //menuRight = engine.createImage(Pantil.vmid(), ImtilX.newRight2(80, Pancolor.BLUE));
        final Panmage[][] players = engine.createSheet("player", new FinPanple2(8, 0), null, null, Parser.LOC + "misc/Player.png", 32, 32);
        playerSouth = createAnm(players[0]);
        playerNorth = createAnm(players[1]);
        playerEast = createAnm(players[2]);
        playerWest = createAnm(players[3]);
        playerWalks = new Panimation[] {playerSouth, playerEast, playerNorth, playerWest};
        tiles = engine.createImage("tiles", Parser.LOC + "misc/Tiles.png");
        DIM_BUTTON = getButtonSize(0);
        final Panmage[] diamonds = getDiamonds(DIM_BUTTON, Pancolor.GREY);
        diamond = diamonds[0];
        diamondIn = diamonds[1];
        final Panmage[] menuFullPair = createImgPair(Parser.LOC + "misc/MenuFull.png", "menu.full");
        menuFull = menuFullPair[0];
        menuFullTranslucent = menuFullPair[1];
        splitMenuImage();
        final Panmage[] speciesAllPair = createImgPair(Parser.LOC + "misc/Species.png", "species.all");
        speciesAll = speciesAllPair[0];
        speciesAllTranslucent = speciesAllPair[1];
        splitSpeciesImage();
        worldSrc = Imtil.load(Parser.LOC + "misc/WorldMap.png");
        ctrl = new ControlScheme();
    }
    
    private final static Panmage[] createImgPair(final String loc, final String name) {
        final Pangine engine = Pangine.getEngine();
        final Img img = Imtil.load(loc);
        img.setTemporary(false);
        final Panmage[] pair = new Panmage[2];
        pair[0] = engine.createImage(PRE_IMG + name, img);
        Imtil.setPseudoTranslucent(img);
        pair[1] = engine.createImage(PRE_IMG + name + ".translucent", img);
        img.close();
        return pair;
    }
    
    private final static Panimation createAnm(final Panmage[] row) {
        final Panmage i0 = row[0];
        final String baseId = i0.getId();
        final Panmage[] ia = {i0, row[1], i0, row[2]};
        return Pangine.getEngine().createAnimation(PRE_ANM + baseId, createFrames(PRE_FRM + baseId, 2, ia));
    }
    
    private final static void splitSpeciesImage() {
        final int fullWidth = (int) speciesAll.getSize().getX();
        final int cellsPerRow = fullWidth / IMG_W;
        final Panple size = new FinPanple2(IMG_W, IMG_H);
        for (final Species s : Species.getSpecies()) {
            final int id = s.getId() - 1;
            split(s.getName(), speciesAll, speciesAllTranslucent, id, IMG_W, IMG_H, size, cellsPerRow);
        }
    }
    
    private final static void splitMenuImage() {
        final int fullWidth = (int) menuFull.getSize().getX(), d = 24;
        final int cellsPerRow = fullWidth / d;
        final Panple size = new FinPanple2(d, d);
        final BufferedReader in = Iotil.getBufferedReader(Parser.LOC + "menu.txt");
        try {
            String line;
            int id = 0;
            while ((line = in.readLine()) != null) {
                split(line, menuFull, menuFullTranslucent, id, d, d, size, cellsPerRow);
                id++;
            }
        } catch (final Exception e) {
            throw Pantil.toRuntimeException(e);
        } finally {
            Iotil.close(in);
        }
    }
    
    private final static void split(final String name, final Panmage all, final Panmage trans, final int id, final int iw, final int ih, final Panple size, final int cellsPerRow) {
        final int j = id / cellsPerRow, i = id - (j * cellsPerRow);
        final int x = i * iw, y = j * ih;
        final Panmage image = new SubPanmage(Pantil.vmid(), null, null, null, all, x, y, size);
        imageCache.put(getKey(name, true), image);
        final Panmage imageTrans = new SubPanmage(Pantil.vmid(), null, null, null, trans, x, y, size);
        imageCache.put(getKey(name, false), imageTrans);
    }
    
    private final static String getKey(final String name, final boolean possible) {
        return name + (possible ? "" : ".trans");
    }
    
    private final static Panmage getImage(final String name, final boolean possible) {
        final String key = getKey(name, possible);
        if (imageCache.containsKey(key)) {
            return imageCache.get(key); // Can be null
        }
        final String fileName = formatFile(name);
        //for (int i = 0; i < 2; i++) {
            //final String loc = Parser.LOC + ((i == 0) ? "img/" : "misc/") + fileName + ".png";
            final String loc = Parser.LOC + "misc/" + fileName + ".png";
            if (Iotil.exists(loc)) {
                final Img im = Imtil.load(loc);
                if (!possible) {
                    Imtil.setPseudoTranslucent(im);
                }
                final Panmage img = Pangine.getEngine().createImage(Pantil.vmid(), im);
                imageCache.put(key, img);
                return img;
            }
        //}
        imageCache.put(key, null);
        return null;
    }
    
    private final static class Wrapper {
        private volatile Option value = null;
    }
    
    private final static class PndHandler extends Handler {
        @Override
        public final void exit() {
            Pangine.getEngine().exit();
        }
        
        @Override
        public final Option handle(final Option caller, final Label label, final List<? extends Option> options) {
            //while (Pangine.getEngine().getClock() < 1) {
            /*while (!isRoomInitialized()) {
                //System.out.println("Yielding");
                Thread.yield();
            }
            System.out.println("Handling");*/
            final Wrapper choice = new Wrapper();
            Pangine.getEngine().executeInGameThread(new Runnable() {
                @Override public final void run() {
                    lastCaller = MonsterGame.caller;
                    MonsterGame.caller = caller;
                    MonsterGame.options = options;
                    MonsterGame.choice = choice;
                    if (caller instanceof LocationOption) {
                        Panscreen.set(new CityScreen());
                    } else if (caller instanceof WorldOption) {
                        Panscreen.set(new WorldScreen());
                    } else {
                        Panscreen.set(new MonsterScreen(label));
                    }
                }});
            while (choice.value == null) {
                Thread.yield();
            }
            return choice.value;
        }

        @Override
        public final Driver getDriver() {
            return driver;
        }
    }
    
    private static Option caller = null;
    private static Option lastCaller = null;
    private static List<? extends Option> options = null;
    private static Wrapper choice = null;
    
    private final static class MonsterScreen extends Panscreen {
        private final Label label;
        
        private MonsterScreen(final Label label) {
            this.label = label;
        }
        
        @Override
        protected final void load() throws Exception {
            /*
            final Pangine engine = Pangine.getEngine();
            final Pantext lbl = new Pantext(Pantil.vmid(), font, label.getName());
            final int h = engine.getEffectiveHeight();
            lbl.getPosition().set(1, h - 9);
            room.addActor(lbl);
            final List<String> list = new ArrayList<String>();
            for (final Option option : options) {
                if (!option.isPossible()) {
                    continue;
                }
                list.add(option.getGoal().getName());
            }
            final RadioSubmitListener sub = new RadioSubmitListener() {
                @Override
                public final void onSubmit(final RadioSubmitEvent event) {
                    final String c = event.toString();
                    for (final Option option : options) {
                        if (option.getGoal().getName().equals(c)) {
                            // Check possible
                            choice.value = option;
                            return;
                        }
                    }
                }};
            final RadioGroup grp = new RadioGroup(font, list, sub);
            grp.setLayer(room); // Call before init
            //grp.init(lbl); // Disable's lbl's layer, which is same layer
            grp.init();
            grp.getLabel().getPosition().set(17, h - 17); // Call after init
            //grp.getLabel().setBackground(Pantext.CHAR_SPACE);
            grp.getLabel().setBackground(Pantext.CHAR_NULL);
            //grp.getLabel().setBorderEnabled(borderEnabled);
            //grp.getLabel().setBorderStyle(borderStyle);
            //room.addActor(grp.getLabel()); // Done by init
            //grp.init(lbl);
            */
            
            // Img size = 80
            // Max name = 10
            // Name size = name length * letter size = 10 * 8 = 80
            // Button width = left border + img + right border = 3 + 80 + 2 = 85
            // Button height = top border + img + space + text + bottom border = 3 + 80 + 1 + 7 + 2 = 93
            // 85 * 3 = 255, 93 * 2 = 186
            room.setClearDepthEnabled(true);
            addCursor();
            //TouchTabs.setFullScreen(true);
            //final List<TouchButton> buttons = new ArrayList<TouchButton>();
            final Pangine engine = Pangine.getEngine();
            engine.setBgColor(new Pancolor((short) 160));
            final Panteraction interaction = engine.getInteraction();
            int numRows = Mathtil.ceil(options.size() / 3f), titleOffset = 10;
            final boolean chosenDisplayed = caller instanceof BattleOption;
            final boolean detailDisplayed = caller instanceof DetailOption;
            if (chosenDisplayed || detailDisplayed) {
                numRows++;
                titleOffset = 0;
            }
            int menuH = MENU_H, btnOffY = 0;
            for (final Option option : options) {
                if (Chartil.isValued(option.getInfo())) {
                    btnOffY = 8;
                    menuH += btnOffY;
                    break;
                }
            }
            final int menuHeight = numRows * menuH;
            int x = 0, y = menuHeight - menuH;
            final int max = menuHeight + titleOffset - engine.getEffectiveHeight(); // Set range before changing y in loop below
            final SwipeScroller scroller = new SwipeScroller();
            scroller.setLayer(room);
            scroller.setRange(0, 0, 0, max);
            engine.setSwipeListener(scroller);
            if (caller != lastCaller) {
                room.getOrigin().set(0, max);
            }
            if (chosenDisplayed) {
                final BattleOption c = (BattleOption) caller;
                addImage(c.chosen, 0, y, true);
                addImage(c.opponent, MENU_W * 2, y, false);
                y -= menuH;
            } else if (detailDisplayed) {
                final Task task = (Task) ((DetailOption) caller).option;
                final Panmage delim = getImage("Plus", true), init = getImage(options.get(0) instanceof TradeTask ? "Trade" : "Equals", true);
                x = addImages(task.getRequired(), 0, y, MENU_W, delim, null, true);
                final List<? extends Label> awarded;
                if (task instanceof BreedTask) {
                    awarded = Collections.singletonList(new Label("Egg"));
                } else {
                    awarded = task.getAwarded();
                }
                addImages(awarded, x, y, MENU_W, delim, init, false);
                y -= menuH;
                x = 0;
            } else {
                final Pantext lbl = new Pantext(Pantil.vmid(), font, formatLabel(label.getName()));
                lbl.getPosition().set(1, y + menuH + 1);
                room.addActor(lbl);
            }
            Option backOption = null;
            Panctor lastActor = null;
            for (final Option option : options) {
                /*if (!option.isPossible()) {
                    continue;
                }*/
                if (option instanceof ExitOption) {
                    backOption = option;
                } else if (backOption == null && option instanceof BackOption) {
                    backOption = option;
                }
                final String labelName, name;
                if (detailDisplayed && (option instanceof RemoveTask || option instanceof BreedTask || option instanceof TradeTask)) {
                    labelName = name = "Finish";
                } else {
                    labelName = option.getGoal().getName();
                    name = formatLabel(labelName);
                }
                /*buttons.add(TouchTabs.newButton(room, name, menu, menuIn, null, 3, 10, font, name, 3, 2, new Runnable() {
                    @Override public final void run() {
                        // Check possible
                        choice.value = option;
                    }}));*/
                final boolean possible = option.isPossible();
                final Panmage img = getImage(labelName, possible || option instanceof NonOption);
                initImageOffsets(img);
                //final Panmage menuCurr = possible ? menu : menuOff; // Will grey out impossible-yet-previewable options
                final Panmage menuCurr = menu; // Impossible-yet-previewable buttons are normal color; reasonable if icon is translucent
                final TouchButton btn = new TouchButton(interaction, room, name, x, y + btnOffY, 0, menuCurr, menuIn,
                    img, OVERLAY_X + imgOffX, OVERLAY_Y + imgOffY,
                    getFont(name), name, TEXT_X, TEXT_Y, true);
                final String info = option.getInfo();
                if (Chartil.isValued(info)) {
                    final Pantext infoLabel = new Pantext(Pantil.vmid(), fontTiny, info);
                    infoLabel.getPosition().set(x, y + 1);
                    room.addActor(infoLabel);
                }
                if (x == (MENU_W * 2)) {
                    x = 0;
                    y -= menuH;
                } else {
                    x += MENU_W;
                }
                if (!possible && !(option instanceof DetailOption)) { // caller instanceof MorphOption
                    btn.setImageDisabled(menuOff);
                    btn.setEnabled(false);
                }
                lastActor = btn.getActor();
                lastActor.register(btn, new ChooseListener(option));
                engine.registerTouchButton(btn);
            }
            if (backOption != null) {
                lastActor.register(interaction.BACK, new ChooseListener(backOption));
            }
            //TouchTabs.createWithOverlays(0, menu, menuIn, menuLeft, menuRight, buttons);
        }
    }
    
    private final static class ChooseListener implements ActionEndListener {
        private final Option option;
        
        private ChooseListener(final Option option) {
            this.option = option;
        }
        
        @Override public final void onActionEnd(final ActionEndEvent event) {
            // Check possible
            choice.value = option;
        }
    }
    
    private static TileMap tm = null;
    private static TileMapImage[][] imgMap = null;
    private final static Map<Integer, Option> optMap = new HashMap<Integer, Option>();
    private static Option optWorld = null;
    private static Player player = null;
    private final static Map<Integer, Option> locMap = new HashMap<Integer, Option>();
    private final static Map<TileMapImage, Option> wildMap = new HashMap<TileMapImage, Option>();
    private final static Map<TileMapImage, Option> surfMap = new HashMap<TileMapImage, Option>();
    private final static Map<TileMapImage, Option> fishMap = new HashMap<TileMapImage, Option>();
    private final static Map<Location, Option> trackMap = new HashMap<Location, Option>();
    private static Class<? extends TileScreen> screenClass = null;
    private static Class<? extends TileScreen> lastScreenClass = null;
    
    private abstract static class TileScreen extends Panscreen {
        protected final int cols;
        protected final int rows;
        protected final int defaultX;
        protected final int defaultY;
        protected final Direction defaultDir;
        
        private TileScreen(final int cols, final int rows,
                           final int defaultX, final int defaultY, final Direction defaultDir) {
            lastScreenClass = screenClass;
            screenClass = getClass();
            this.cols = cols;
            this.rows = rows;
            this.defaultX = defaultX;
            this.defaultY = defaultY;
            this.defaultDir = defaultDir;
        }
        
        @Override
        protected final void load() throws Exception {
validateCatalystExperience();
            if (lastScreenClass != null && lastScreenClass != screenClass) {
                Player.clearLastCity();
            }
            
            final Pangine engine = Pangine.getEngine();
            //layerHud = createHud(room); // HUD and room both have constant size; use room layer for the HUD
            layerHud = room;
            layerHud.setClearDepthEnabled(false); // Cursor is in room and uses room's coordinates; don't put HUD above cursor
            //layerTiles = room;
            final int w = cols * TW, h = rows * TH;
            layerSprites = engine.createLayer("layer.sprites", w, h, 100, room);
            layerSprites.setClearDepthEnabled(false);
            room.addBeneath(layerSprites);
            layerTiles = engine.createLayer("layer.tiles", w, h, 100, room);
            //layerTiles.setClearDepthEnabled(false);
            layerSprites.addBeneath(layerTiles);
            layerTiles.setMaster(layerSprites);
            engine.setSwipeListener(null);
            layerHud.getOrigin().set(0, 0);
            layerTiles.getOrigin().set(0, 0);
            initTileMap();
            
            optMap.clear();
            optWorld = null;
            locMap.clear();
            wildMap.clear();
            surfMap.clear();
            fishMap.clear();
            buildTileMap();
            layerTiles.addActor(tm);
            layerTiles.setConstant(true);
            
            createControlDiamond(layerHud, diamond, diamondIn, ctrl, DEPTH_BUTTON);
            addCursor();
            
            addPlayer();
            layerSprites.addActor(player);
            engine.track(player);
            Player.clearLastCity();
        }
        
        protected void initTileMap() {
            final TileMap tm = new TileMap("tile.map", cols, rows, TW, TH);
            if (MonsterGame.tm == null) {
                imgMap = tm.splitImageMap(tiles);
            } else {
                tm.setImageMap(MonsterGame.tm);
                imgMap = tm.splitImageMap();
            }
            tm.setForegroundDepth(5);
            tm.setOccupantDepth(10);
            MonsterGame.tm = tm;
        }
        
        protected abstract void buildTileMap() throws Exception;
        
        protected final void addPlayer() {
            final int startX, startY;
            final Direction startDir;
            if (lastCityX >= 0) {
                startDir = Direction.South;
                startX = lastCityX;
                startY = lastCityY;
            } else {
                startDir = defaultDir;
                startX = defaultX;
                startY = defaultY;
            }
            player = new Player(startDir);
            //player.init(tm, startX, startY); // Sets player's layer to tm's, but we want it to be different
            player.setPosition(tm, startX, startY);
        }
    }
    
    private static TileMap worldTm = null;
    private static int worldCols = -1;
    private static int worldRows = -1;
    
    private final static int getWorldCols() {
        if (worldCols < 0) {
            worldCols = worldSrc.getWidth();
        }
        return worldCols;
    }
    
    private final static int getWorldRows() {
        if (worldRows < 0) {
            worldRows = worldSrc.getHeight();
        }
        return worldRows;
    }
    
    private final static class WorldScreen extends TileScreen {
        private WorldScreen() {
            super(getWorldCols(), getWorldRows(), 5, 5, Direction.South);
        }
        
        @Override
        protected final void initTileMap() {
            if (worldTm == null) {
                super.initTileMap();
            }
        }
        
        @Override
        protected final void buildTileMap() throws Exception {
            if (worldTm == null) {
                buildWorldMap();
                worldTm = tm;
            } else {
                tm = worldTm;
            }
            
            final Location currLoc = State.get().getLocation();
            for (final Option option : options) {
                final Label goal = option.getGoal();
                final String name = goal.getName();
                if (name.equals("Menu")) {
                    addMenuButton(option);
                } else if (goal instanceof Location) {
                    final Location loc = (Location) goal;
                    final int x = loc.getX();
                    if (x >= 0) {
                        final int y = loc.getY();
                        tm.setForeground(x, y, imgMap[15][4]);
                        locMap.put(Integer.valueOf(tm.getIndex(x, y)), option);
                        if (loc == currLoc && lastCityX < 0) {
                            lastCityX = x;
                            lastCityY = y - 1;
                        }
                    }
                } else if (option instanceof MenuOption) {
                    final Option wrappedOption = ((MenuOption) option).option;
                    if (wrappedOption instanceof TrackOption) {
                        trackMap.put(((TrackOption) wrappedOption).loc, option);
                    } else if (wrappedOption instanceof BattleOption) {
                        final Location loc = ((BattleOption) wrappedOption).location;
                        final Map<TileMapImage, Option> map;
                        final int imgX, imgY;
                        if (wrappedOption instanceof WildOption) {
                            map = wildMap;
                            imgX = loc.getWildImgX();
                            imgY = loc.getWildImgY();
                        } else if (wrappedOption instanceof SpecialOption && "Surf".equalsIgnoreCase(Label.getName(((SpecialOption) wrappedOption).requirement))) {
                            map = surfMap;
                            imgX = loc.getWaterImgX();
                            imgY = loc.getWaterImgY();
                        } else if (wrappedOption instanceof FishOption) {
                            map = fishMap;
                            imgX = loc.getWaterImgX();
                            imgY = loc.getWaterImgY();
                        } else {
                            map = null;
                            imgX = imgY = -1;
                        }
                        if (imgX >= 0) {
                            map.put(imgMap[imgY][imgX], option);
                        }
                    }
                }
            }
        }
        
        @Override
        protected final void destroy() {
            worldTm.detach();
        }
    }
    
    private static Location lastLocation = null;
    
    private final static class CityScreen extends TileScreen {
        private CityScreen() {
            super(32, 24, 13, 1, Direction.North);
        }
        
        @Override
        protected final void buildTileMap() throws Exception {
            final Location location;
            if (caller instanceof LocationOption) {
                location = ((LocationOption) caller).location;
                if (location != lastLocation) {
                    if (player != null) {
                        Player.clearLastCity();
                    }
                    lastLocation = location;
                }
            } else {
                location = null;
            }
            
            int grassY = 14, grassX = 0;
            if (location != null) {
                final int wx = location.getWildImgX();
                if (wx >= 0) {
                    grassX = wx;
                    grassY = location.getWildImgY();
                }
            }
            final TileMapImage grass = imgMap[grassY][grassX], wallImg = imgMap[14][2], wallLeftImg = imgMap[14][3], wallRightImg = imgMap[14][1];
            final Tile wall = tm.getTile(grass, wallImg, Tile.BEHAVIOR_SOLID);
            final Tile wallBottom = tm.getTile(grass, imgMap[13][2], Tile.BEHAVIOR_SOLID);
            final Tile wallTop = tm.getTile(grass, imgMap[15][2], Tile.BEHAVIOR_SOLID);
            final Tile wallLeft = tm.getTile(grass, wallLeftImg, Tile.BEHAVIOR_SOLID);
            final Tile wallRight = tm.getTile(grass, wallRightImg, Tile.BEHAVIOR_SOLID);
            tm.fillBackground(grass);
            final int wallWidth = 3, wallMax = wallWidth - 1;
            for (int wallLayer = 0; wallLayer < wallWidth; wallLayer++) {
                final int off = wallLayer + 1;
                final int cols1 = cols - off, rows1 = rows - off;
                for (int i = off; i < cols1; i++) {
                    tm.setTile(i, rows1, wallLayer < wallMax ? wall : wallTop);
                    tm.setTile(i, wallLayer, wallLayer < wallMax ? wall : wallBottom);
                }
                for (int j = off; j < rows1; j++) {
                    tm.setTile(wallLayer, j, wallLayer < wallMax ? wall : wallLeft);
                    tm.setTile(cols1, j, wallLayer < wallMax ? wall : wallRight);
                }
                tm.setForeground(wallLayer, rows1, wallLayer < wallMax ? wallImg : imgMap[13][4], Tile.BEHAVIOR_SOLID);
                tm.setForeground(cols1, rows1, wallLayer < wallMax ? wallImg : imgMap[13][5], Tile.BEHAVIOR_SOLID);
                tm.setForeground(wallLayer, wallLayer, wallLayer < wallMax ? wallImg : imgMap[14][4], Tile.BEHAVIOR_SOLID);
                tm.setForeground(cols1, wallLayer, wallLayer < wallMax ? wallImg : imgMap[14][5], Tile.BEHAVIOR_SOLID);
                tm.setForeground(11, wallLayer, wallLayer < wallMax ? wallLeftImg : imgMap[13][3], Tile.BEHAVIOR_SOLID);
                for (int i = 12; i < 15; i++) {
                    tm.setForeground(i, wallLayer, null, Tile.BEHAVIOR_OPEN);
                }
                tm.setForeground(15, wallLayer, wallLayer < wallMax ? wallRightImg : imgMap[13][1], Tile.BEHAVIOR_SOLID);
            }
            
            boolean needStore = true, needMorph = true, needTrainers = true, needSpecial = true;
            final int buildingOffset = 3, buildingStart = wallWidth + buildingOffset, buildingMid = buildingStart + 7;
            for (final Option option : options) {
            	final String name = option.getGoal().getName();
                if (name.equals(Data.getStore())) {
                    building(buildingStart, buildingStart, 0, 8, 4, 4, 2, option);
                    needStore = false;
                } else if (name.equals(Data.getMorph())) {
                    building(buildingMid, buildingStart, 0, 4, 5, 5, 2, option);
                    needMorph = false;
                } else if (name.equals(location.getTrainersName())) {
                    specialBuilding(buildingStart, buildingMid, option);
                    needTrainers = false;
                } else if (name.equals(Data.getTrainers())) {
                    building(buildingStart, buildingMid, 5, 4, 7, 5, 3, option);
                    needTrainers = false;
                } else if (name.equals(Special.Specialty.Lab.toString())) {
                    building(buildingMid + 6, buildingMid, 0, 12, 7, 4, 3, option);
                    needSpecial = false;
                } else if (name.equals(Special.Specialty.Trader.toString())) {
                    building(buildingMid + 8, buildingMid, 9, 8, 4, 4, 2, option);
                    needSpecial = false;
                } else if (name.equals(Special.Specialty.Breeder.toString())) {
                    building(buildingMid + 6, buildingMid, 7, 12, 7, 4, 1, option);
                    needSpecial = false;
                } else if (name.equals(Special.Specialty.Library.toString())) {
                    specialBuilding(buildingMid + 7, buildingMid, option);
                    needSpecial = false;
                } else if (name.equals("World")) {
                    optWorld = option;
                } else if (name.equals("Menu")) {
                    addMenuButton(option);
                }
            }
            final int unusedRight = buildingMid + 8, unusedBottom = buildingStart + 1;
            if (needStore) {
                unusedBuilding(buildingStart, unusedBottom);
            }
            if (needMorph) {
                unusedBuilding(buildingMid, unusedBottom);
            }
            if (needTrainers) {
                unusedBuilding(buildingStart + 1, buildingMid + 1);
            }
            if (needSpecial) {
                unusedBuilding(unusedRight, buildingMid + 1);
            }
            unusedBuilding(unusedRight, unusedBottom);
        }
    }
    
    private final static void addMenuButton(final Option option) {
        final Pangine engine = Pangine.getEngine();
        final Panmage img = getImage("Menu", true);
        final Panple size = img.getSize();
        final int x = engine.getEffectiveWidth() - (int) size.getX() - 1;
        final int y = engine.getEffectiveHeight() - (int) size.getY() - 1;
        final Panteraction inter = engine.getInteraction();
        final TouchButton btn = new TouchButton(inter, layerHud, "Menu", x, y, DEPTH_BUTTON, img, img, true);
        engine.registerTouchButton(btn);
        registerMenu(btn, option);
        registerMenu(inter.BACK, option);
    }
    
    private final static void registerMenu(final Panput btn, final Option option) {
        tm.register(btn, new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) {
                player.updateLastCity();
                choice.value = option;
            }});
    }
    
    private final static void unusedBuilding(final int tlX, final int tlY) {
        building(tlX, tlY, 12, 2, 4, 3, 0, null);
    }
    
    private final static void specialBuilding(final int tlX, final int tlY, final Option option) {
        building(tlX, tlY, 4, 8, 5, 4, 1, option);
    }
    
    private final static void building(final int tlX, final int tlY, final int imX, final int imY, final int w, final int h, final int drX, final Option option) {
        for (int j = 0; j < h; j++) {
            final int tlYj = tlY + j, imYj = imY - j;
            for (int i = 0; i < w; i++) {
                tm.setForeground(tlX + i, tlYj, imgMap[imYj][imX + i], Tile.BEHAVIOR_SOLID);
            }
        }
        if (option != null) {
            optMap.put(Integer.valueOf(tm.getIndex(tlX + drX, tlY - 1)), option);
        }
    }
    
    private static int lastCityX = -1;
    private static int lastCityY = -1;
    
    private final static class Player extends Guy4 {
        private Player(final Direction dir) {
            setView(playerWalks);
            setSpeed(2);
            face(dir);
        }
        
        @Override
        protected final void onStill() {
            Guy4Controller.onStillPlayer(ctrl, this);
        }
        
        @Override
        protected final void onStop() {
            final int index = getIndex();
            final Option locOpt = locMap.get(Integer.valueOf(index));
            if (locOpt == null) {
                final Tile tile = tm.getTile(index);
                if (tile == tileTrack) {
                    final Object bg = DynamicTileMap.getRawBackground(getTileFacing());
                    final WildOption opt = (WildOption) ((MenuOption) wildMap.get(bg)).option;
                    choose(trackMap.get(opt.location), true);
                    return;
                }
                final Object bg = DynamicTileMap.getRawBackground(tile);
                if (Mathtil.rand(WILD_ENCOUNTER_RATE)) {
                    choose(wildMap.get(bg), true);
                }
            } else {
                clearLastCity();
                choice.value = locOpt;
            }
        }
        
        @Override
        protected final void onBump() {
            final Direction dir = getDirection();
            final Option chosen;
            if (Direction.North == dir) {
                chosen = optMap.get(Integer.valueOf(getIndex()));
            } else if ((getRow() == 0) && Direction.South == dir) {
                chosen = optWorld;
            } else {
                chosen = null;
            }
            if (chosen != null) {
                if (chosen == optWorld) {
                    clearLastCity();
                } else {
                    updateLastCity();
                }
                choice.value = chosen;
                return;
            }
            final Tile tile = getTileFacing();
            final Object bg = DynamicTileMap.getRawBackground(tile);
            final Map<TileMapImage, Option> waterMap;
            if (getTile() == tileDock) {
                waterMap = fishMap;
            } else {
                waterMap = surfMap;
            }
            final Option waterOption = waterMap.get(bg);
            if (waterOption != null) {
                updateLastCity();
                choice.value = waterOption;
                return;
            }
        }
        
        private final boolean choose(final Option option, final boolean updateLocation) {
            if (!Option.isPossible(option)) {
                return false;
            } else if (updateLocation) {
                updateLastCity();
            } else {
                clearLastCity();
            }
            choice.value = option;
            return true;
        }
        
        protected final static void clearLastCity() {
            lastCityX = -1;
            lastCityY = -1;
        }
        
        protected final void updateLastCity() {
            lastCityX = getColumn();
            lastCityY = getRow();
        }
    }
    
    private static Map<Integer, Object> buildMap = null;
    private final static Integer tileTree = Integer.valueOf(0);
    private final static Integer tileRock = Integer.valueOf(1);
    private final static Integer tileStone = Integer.valueOf(2);
    private static Tile tileDock = null;
    private static Tile tileTrack = null;
    private static Tile tileDefault = null;
    
    private final static void loadTileDefinitions() throws Exception {
        final BufferedReader in = Iotil.getBufferedReader(Parser.LOC + "tiles.txt");
        try {
            final Pattern pat = Pattern.compile("\\|");
            String line;
            buildMap = new HashMap<Integer, Object>();
            while ((line = in.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                final String[] tokens = pat.split(line);
                final short r = Short.parseShort(tokens[0]);
                final short g = Short.parseShort(tokens[1]);
                final short b = Short.parseShort(tokens[2]);
                final Integer color = Integer.valueOf(Imtil.getDataElement(r, g, b, Pancolor.MAX_VALUE));
                final String token3 = tokens[3];
                if ("tree".equalsIgnoreCase(token3)) {
                    buildMap.put(color, tileTree);
                    continue;
                } else if ("rock".equalsIgnoreCase(token3)) {
                    buildMap.put(color, tileRock);
                    continue;
                } else if ("stone".equalsIgnoreCase(token3)) {
                    buildMap.put(color, tileStone);
                    continue;
                }
                final int bgX = Integer.parseInt(token3);
                final int bgY = Integer.parseInt(tokens[4]);
                final String token5 = tokens[5];
                final byte behavior;
                int special = 0;
                if ("Dock".equalsIgnoreCase(token5)) {
                    behavior = Tile.BEHAVIOR_OPEN;
                    special = 1;
                } else if ("Default".equalsIgnoreCase(token5)) {
                    behavior = Tile.BEHAVIOR_OPEN;
                    special = 2;
                } else if ("Track".equalsIgnoreCase(token5)) {
                    behavior = Tile.BEHAVIOR_OPEN;
                    special = 3;
                } else if ("Solid".equalsIgnoreCase(token5)) {
                    behavior = Tile.BEHAVIOR_SOLID;
                } else if ("Open".equalsIgnoreCase(token5)) {
                    behavior = Tile.BEHAVIOR_OPEN;
                } else {
                    throw new IllegalArgumentException("Unexpected tile behavior " + token5);
                }
                final Tile tile = tm.getTile(imgMap[bgY][bgX], null, behavior);
                buildMap.put(color, tile);
                if (special == 1) {
                    tileDock = tile;
                } else if (special == 2) {
                    tileDefault = tile;
                } else if (special == 3) {
                    tileTrack = tile;
                }
            }
        } finally {
            Iotil.close(in);
        }
    }
    
    private final static void buildWorldMap() throws Exception {
        loadTileDefinitions();
        final int w = worldSrc.getWidth(), h = worldSrc.getHeight();
        for (int j = 0; j < h; j++) {
            final int tj = h - j - 1;
            for (int i = 0; i < w; i++) {
                final Object t = getWorldTile(i, j);
                if (t == tileTree) {
                    tree(i, tj);
                } else if (t == tileRock) {
                    rock(i, tj, j);
                } else if (t == tileStone) {
                    stone(i, tj, j);
                } else {
                //} else if (t.getClass() == Tile.class) {
                    tm.setTile(i, tj, (Tile) t);
                }
            }
        }
        worldSrc.close();
        worldSrc = null;
        buildMap = null;
    }
    
    private final static Object getWorldTile(final int i, final int j) {
        return buildMap.get(Integer.valueOf(worldSrc.getRGB(i, j)));
    }
    
    private final static Tile validateWorldTile(final int i, final int j) {
        if (i < 0 || j < 0 || i >= worldSrc.getWidth() || j >= worldSrc.getHeight()) {
            return null;
        }
        final Object tile = getWorldTile(i, j);
        if (tile == tileDock) {
            return null;
        } else if (tile instanceof Tile) {
            return (Tile) tile;
        } else if (tile == tileTree) {
            return tileDefault;
        }
        return null;
    }
    
    private final static void tree(final int x, final int y) {
        tm.setBackground(x, y, imgMap[14][6], Tile.BEHAVIOR_SOLID);
        tm.setForeground(x, y + 1, imgMap[13][6], Tile.BEHAVIOR_SOLID);
    }
    
    private final static void stone(final int x, final int y, final int iy) {
        Tile neighborTile = validateWorldTile(x + 1, iy);
        if (neighborTile == null) {
            neighborTile = validateWorldTile(x - 1, iy);
            if (neighborTile == null) {
                neighborTile = validateWorldTile(x, iy + 1);
                if (neighborTile == null) {
                    neighborTile = validateWorldTile(x, iy - 1);
                    if (neighborTile == null) {
                        throw new IllegalStateException("Could not find stone neighbor");
                    }
                }
            }
        }
        tm.setTile(x, y, DynamicTileMap.getRawBackground(neighborTile), imgMap[13][7], Tile.BEHAVIOR_SOLID);
    }
    
    private final static void rock(final int x, final int y, final int iy) {
        final Tile rightTile = validateWorldTile(x + 1, iy);
        final Tile upTile = validateWorldTile(x, iy - 1);
        final Tile neighborTile;
        final int j, i;
        if (rightTile != null) {
            neighborTile = rightTile;
            i = 3;
            if (upTile != null) {
                j = 13;
            } else {
                final Tile downTile = validateWorldTile(x, iy + 1);
                if (downTile != null) {
                    j = 15;
                } else {
                    j = 14;
                }
            }
        } else {
            final Tile leftTile = validateWorldTile(x - 1, iy);
            if (leftTile != null) {
                neighborTile = leftTile;
                i = 1;
                if (upTile != null) {
                    j = 13;
                } else {
                    final Tile downTile = validateWorldTile(x, iy + 1);
                    if (downTile != null) {
                        j = 15;
                    } else {
                        j = 14;
                    }
                }
            } else if (upTile != null) {
                neighborTile = upTile;
                j = 13;
                i = 2;
            } else {
                final Tile downTile = validateWorldTile(x, iy + 1);
                if (downTile != null) {
                    neighborTile = downTile;
                    j = 15;
                    i = 2;
                } else {
                    final Tile rightUpTile = validateWorldTile(x + 1, iy - 1);
                    if (rightUpTile != null) {
                        neighborTile = rightUpTile;
                        j = 14;
                        i = 4;
                    } else {
                        final Tile rightDownTile = validateWorldTile(x + 1, iy + 1);
                        if (rightDownTile != null) {
                            neighborTile = rightDownTile;
                            j = 13;
                            i = 4;
                        } else {
                            final Tile leftUpTile = validateWorldTile(x - 1, iy - 1);
                            if (leftUpTile != null) {
                                neighborTile = leftUpTile;
                                j = 14;
                                i = 5;
                            } else {
                                final Tile leftDownTile = validateWorldTile(x - 1, iy + 1);
                                if (leftDownTile != null) {
                                    neighborTile = leftDownTile;
                                    j = 13;
                                    i = 5;
                                } else {
                                    neighborTile = null;
                                    j = 14;
                                    i = 2;
                                }
                            }
                        }
                    }
                }
            }
        }
        tm.setTile(x, y, neighborTile);
        tm.setForeground(x, y, imgMap[j][i], Tile.BEHAVIOR_SOLID);
    }
    
    private static void initImageOffsets(final Panmage img) {
        if (img == null) {
            imgOffX = 0;
            imgOffY = 0;
        } else {
            final Panple size = img.getSize();
            imgOffX = (IMG_W - (int) size.getX()) / 2;
            imgOffY = (IMG_H - (int) size.getY()) / 2;
        }
    }
    
    private static int addImages(final List<? extends Label> list, int x, final int y, final int off, final Panmage delim, final Panmage init, final boolean checkPossible) {
        initImageOffsets(delim);
        final int delimOffX = (int) delim.getSize().getX() / 2, delimOffY = OVERLAY_Y + imgOffY;
        boolean first = true;
        for (final Label s : list) {
            if (first) {
                first = false;
                addImage(init, x - delimOffX, y + delimOffY, 10, false);
            } else {
                addImage(delim, x - delimOffX, y + delimOffY, 10, false);
            }
            addImage(s, x, y, false, !checkPossible || ((Entity) s).isAvailable());
            x += off;
        }
        return x;
    }
    
    private static void addImage(final Entity s, final int x, final int y, final boolean mirror) {
        addImage(s, x, y, mirror, true);
    }
    
    private static void addImage(final Label s, final int x, final int y, final boolean mirror, final boolean possible) {
        final String name = s.getName();
        final Panmage image = getImage((s instanceof Amount) ? ((Amount) s).getUnits(): name, possible);
        initImageOffsets(image);
        addImage(image, x + OVERLAY_X + imgOffX + (mirror ? image.getSize().getX() + 1 : 0), y + OVERLAY_Y + imgOffY, 0, mirror);
        final Pantext text = new Pantext(Pantil.vmid(), getFont(name), name);
        text.getPosition().set(x + TEXT_X, y + TEXT_Y);
        room.addActor(text);
    }
    
    private static void addImage(final Panmage image, final float x, final float y, final float z, final boolean mirror) {
        final Panctor actor = new Panctor();
        actor.setView(image);
        actor.getPosition().set(x, y, z);
        actor.setMirror(mirror);
        room.addActor(actor);
    }
    
    private final static void addCursor() {
        final Cursor cursor = Cursor.addCursor(room, menuCursor);
        if (cursor != null) {
            cursor.getPosition().setZ(30);
        }
        Pangine.getEngine().clearTouchEvents();
    }
    
    private static MultiFont getFont(final String name) {
        return (name.length() > 10) ? fontTiny : font;
    }
    
    private static String formatLabel(final String name) {
        return ImgFont.format(name);
    }
    
    private static String formatFile(final String name) {
        return Chartil.remove(Chartil.remove(Chartil.removeAccents(name), ' '), '\'');
    }
    
    /*
    private final static void buildSpeciesImage() throws Exception {
        final int d = 2048, p = 80;
        final Img img = Imtil.newImage(d, d);
        int i = 1;
        int x = 0, y = 0;
        for (final Species s : Species.getSpecies()) {
            if (i != s.getId()) {
                throw new Exception("Array index " + i + " was species id " + s.getId());
            }
            System.out.println("Adding species " + i);
            final Img sub = Imtil.load(Parser.LOC + "img/" + s.getName() + ".png");
            Imtil.copy(sub, img, 0, 0, p, p, x, y);
            sub.close();
            x += p;
            if ((x + p) > d) {
                x = 0;
                y += p;
            }
            i++;
        }
        Imtil.save(img, Parser.LOC + "misc/Species.png");
    }
    
    private final static void dumpLocations() {
        for (final Location loc : Location.getLocations()) {
            System.out.print(loc.getName());
            if (Coltil.isValued(loc.getStore())) {
                System.out.print(" Store");
            }
            if (Coltil.isValued(loc.getTrained())) {
                System.out.print(" Trained");
            }
            final String special = loc.getSpecial();
            if (special != null) {
                System.out.print(" " + special);
            }
            if (Coltil.isValued(loc.getFish())) {
                System.out.print(" Fish");
            }
            for (final Item item : loc.getSpecials().keySet()) {
                System.out.print(" " + item.getName());
            }
            if (Coltil.isValued(loc.getNormal())) {
                System.out.print(" Wild");
            }
            if (Coltil.isValued(loc.getTrackable())) {
                System.out.print(" Track");
            }
            System.out.println();
        }
    }
    
    private final static void validateSpecies() {
        int start = 0, mid = 0, adv = 0;
        for (final Species s : Species.getSpecies()) {
            final Species p = s.getPrecursor();
            final int sr = s.getRank();
            if (s.isStart()) {
                start++;
                if (sr != 2) {
                    err("Starter " + s + " had rank " + sr + " instead of 2");
                }
            }
            if (p != null) {
                final int pr = p.getRank();
                if (pr >= sr) {
                    err(s + " had rank " + sr + " but precursor " + p + " had rank " + pr);
                } else if (p.isStart()) {
                    mid++;
                    if (sr != 3) {
                        err("Middle starter " + s + " had rank " + sr + " instead of 3");
                    }
                } else {
                    final Species b = p.getPrecursor();
                    if (b != null && b.isStart()) {
                        adv++;
                        if (sr != 4) {
                            err("Advanced starter " + s + " had rank " + sr + " instead of 4");
                        }
                    }
                }
            }
        }
        if (start != 12) {
            err("Found " + start + " starters instead of 12");
        } else if (mid != 12) {
            err("Found " + mid + " starter middle forms instead of 12");
        } else if (adv != 12) {
            err("Found " + adv + " starter advanced forms instead of 12");
        }
    }
    
    private final static void err(final String s) {
        throw new IllegalStateException(s);
        //System.err.println(s);
    }
    
    private static Set<String> imgNames = new LinkedHashSet<String>();
    
    private final static void assertImage(final Label lbl) {
        assertImage(lbl.getName());
    }
    
    private final static void assertImage(final String name) {
        if (getImage(name, true) == null) {
            err("No image for " + name);
        } else {
            System.out.println(name);
            if (!imgNames.add(name)) {
                throw new IllegalStateException("Adding image " + name + " twice");
            }
        }
    }
    
    private final static void assertNoImage(final Label lbl) {
        if (getImage(lbl.getName(), true) != null) {
            err("Unnecessary image for " + lbl);
        }
    }
    
    private final static void validateImages() {
        for (final Item item : Item.getItems()) {
            assertImage(item);
        }
        for (final Location loc : Location.getLocations()) {
            if (loc.isCity()) {
                assertImage(loc);
            } else {
                assertNoImage(loc);
            }
        }
        final String[] menu = { "Egg", "Travel", "Experience", "Money", "Fight", "Buy", "Sell", "Menu", "Plus", "Equals", "Up", "Back", "Exit" };
        // Items/database bigger
        for (final String m : menu) {
            assertImage(m);
        }
        if (Pangine.getEngine() != null) {
            return;
        }
        final int d1 = 24, numImgs = imgNames.size(), totalArea = numImgs * d1 * d1;
        final double approx = Math.sqrt(totalArea);
        int da = 2;
        while (da < approx) {
            da *= 2;
        }
        System.out.println("Number of images: " + numImgs + "; area: " + totalArea + "; approx: " + approx + "; dim: " + da);
        final Img cache24 = Imtil.newImage(da, da);
        int x = 0, y = 0;
        for (final String name : imgNames) {
            final Img src = Imtil.load(Parser.LOC + "misc/" + formatFile(name) + ".png");
            if (src.getWidth() != d1) {
                throw new RuntimeException(name + " had width " + src.getWidth() + " instead of " + d1);
            } else if (src.getHeight() != d1) {
                throw new RuntimeException(name + " had height " + src.getHeight() + " instead of " + d1);
            }
            try {
                Imtil.copy(src, cache24, 0, 0, d1, d1, x, y);
            } catch (final Exception e) {
                throw new RuntimeException("Error copying " + name, e);
            }
            x += d1;
            if ((x + d1) > da) {
                x = 0;
                y += d1;
            }
            src.close();
        }
        Imtil.save(cache24, Parser.LOC + "misc/MenuFull.png");
        cache24.close();
    }
    
    private final static void validateTrack() {
        for (final Species s : Species.getSpecies()) {
            if (s.canTrack()) {
                System.out.println(s + " - " + s.getWild());
            }
        }
    }
    */
    private final static void validateCatalystExperience() {
        for (final Species s : Species.getSpecies()) {
            final Entity c = s.getCatalyst();
            if (c instanceof Experience) {
                final int ex = s.getCatalystExperience(), ac = ((Experience) c).value;
                if (ex != ac) {
                    System.err.println(s.getName() + " expected " + ex + "; actual " + ac);
                }
            }
        }
    }
    
    public final static void main(final String[] args) {
        try {
            Handler.implClass = PndHandler.class;
            Handler.get(); // Must instantiate Handler to call Parser.run
            driver = new Driver(new State("Trainer"));
            //driver.run();
            new MonsterGame().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
