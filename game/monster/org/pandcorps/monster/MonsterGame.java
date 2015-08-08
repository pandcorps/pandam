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

import java.util.*;

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

public final class MonsterGame extends BaseGame {
    /*
    Breed/trade don't show options already on team
    Show disabled Catch if on team
    Show player's money on buy/sell
    Show container quantity on Battle screen
    Show total money/experience when earning it
    Database screen
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
    private final static Panple SIZE_24 = new FinPanple2(24, 24);
    private final static int DEPTH_BUTTON = 20;
    private static int DIM_BUTTON = 0;
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
    private final static Map<String, Panmage> imageCache = new HashMap<String, Panmage>();
    private static Panimation playerSouth = null;
    private static Panimation playerEast = null;
    private static Panimation playerNorth = null;
    private static Panimation playerWest = null;
    private static Panimation[] playerWalks = null;
    private static Panmage diamond = null;
    private static Panmage diamondIn = null;
    private static Panmage tiles = null;
    private static ControlScheme ctrl = null;
    
    @Override
    protected final void init(final Panroom room) throws Exception {
        MonsterGame.room = room;
        loadConstants();
        new Thread(driver).start();
    }
    
    private final static void loadConstants() throws Exception {
        final Pangine engine = Pangine.getEngine();
        font = Fonts.getClassics(new FontRequest(8), Pancolor.WHITE, Pancolor.BLACK);
        fontTiny = Fonts.getTinies(FontType.Byte, Pancolor.WHITE, Pancolor.BLACK);
        menu = engine.createImage(Pantil.vmid(), ImtilX.newButton(MENU_W, MENU_H, Pancolor.GREY));
        menuIn = engine.createImage(Pantil.vmid(), ImtilX.newButton(MENU_W, MENU_H, Pancolor.CYAN));
        menuOff = engine.createImage(Pantil.vmid(), ImtilX.newButton(MENU_W, MENU_H, Pancolor.DARK_GREY));
        menuCursor = engine.createImage(Pantil.vmid(), ImtilX.newUp2(16, Pancolor.WHITE));
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
        final Img menuFullImg = Imtil.load(Parser.LOC + "misc/MenuFull.png");
        menuFullImg.setTemporary(false);
        menuFull = engine.createImage(PRE_IMG + "menu.full", menuFullImg);
        Imtil.setPseudoTranslucent(menuFullImg);
        menuFullTranslucent = engine.createImage(PRE_IMG + "menu.full.translucent", menuFullImg);
        menuFullImg.close();
        ctrl = new ControlScheme();
    }
    
    private final static Panimation createAnm(final Panmage[] row) {
        final Panmage i0 = row[0];
        final String baseId = i0.getId();
        final Panmage[] ia = {i0, row[1], i0, row[2]};
        return Pangine.getEngine().createAnimation(PRE_ANM + baseId, createFrames(PRE_FRM + baseId, 2, ia));
    }
    
    private final static Panmage getImage(final String name, final boolean possible) {
        final String key = name + (possible ? "" : ".trans");
        if (imageCache.containsKey(key)) {
            return imageCache.get(key); // Can be null
        }
        final String fileName = formatFile(name);
        for (int i = 0; i < 2; i++) {
            final String loc = Parser.LOC + ((i == 0) ? "img/" : "misc/") + fileName + ".png";
            if (Iotil.exists(loc)) {
                final Img im = Imtil.load(loc);
                if (!possible) {
                    Imtil.setPseudoTranslucent(im);
                }
                final Panmage img = Pangine.getEngine().createImage(Pantil.vmid(), im);
                imageCache.put(key, img);
                return img;
            }
        }
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
                    if (caller instanceof LocationOption) {
                        Panscreen.set(new CityScreen(options, choice));
                    } else if (caller instanceof WorldOption) {
                        Panscreen.set(new WorldScreen(options, choice));
                    } else {
                        Panscreen.set(new MonsterScreen(caller, label, options, choice));
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
    
    private static Option lastCaller = null;
    private static List<? extends Option> options = null;
    private static Wrapper choice = null;
    
    private final static class MonsterScreen extends Panscreen {
        private final Option caller;
        private final Label label;
        
        private MonsterScreen(final Option caller, final Label label, final List<? extends Option> options, final Wrapper choice) {
            this.caller = caller;
            this.label = label;
            MonsterGame.options = options;
            MonsterGame.choice = choice;
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
            final boolean detailDisplayed = caller instanceof MorphDetailOption;
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
                lastCaller = caller;
            }
            if (chosenDisplayed) {
                final BattleOption c = (BattleOption) caller;
                addImage(c.chosen, 0, y, true);
                addImage(c.opponent, MENU_W * 2, y, false);
                y -= menuH;
            } else if (detailDisplayed) {
                final Task task = (Task) ((MorphDetailOption) caller).option;
                final Panmage delim = getImage("Plus", true), init = getImage("Equals", true);
                x = addImages(task.getRequired(), 0, y, MENU_W, delim, null, true);
                addImages(task.getAwarded(), x, y, MENU_W, delim, init, false);
                y -= menuH;
                x = 0;
            } else {
                final Pantext lbl = new Pantext(Pantil.vmid(), font, formatLabel(label.getName()));
                lbl.getPosition().set(1, y + menuH + 1);
                room.addActor(lbl);
            }
            for (final Option option : options) {
                /*if (!option.isPossible()) {
                    continue;
                }*/
                final String labelName, name;
                if (detailDisplayed && option instanceof RemoveTask) {
                    labelName = "Up";
                    name = Data.getMorph();
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
                if (!possible && !(caller instanceof MorphOption)) {
                    btn.setImageDisabled(menuOff);
                    btn.setEnabled(false);
                }
                btn.getActor().register(btn, new ActionEndListener() {
                    @Override public final void onActionEnd(final ActionEndEvent event) {
                        // Check possible
                        choice.value = option;
                    }});
                engine.registerTouchButton(btn);
            }
            //TouchTabs.createWithOverlays(0, menu, menuIn, menuLeft, menuRight, buttons);
        }
    }
    
    private static TileMap tm = null;
    private static TileMapImage[][] imgMap = null;
    private final static Map<Integer, Option> optMap = new HashMap<Integer, Option>();
    private static Option optWorld = null;
    private static Player player = null;
    
    private abstract static class TileScreen extends Panscreen {
        protected final int cols;
        protected final int rows;
        protected final int defaultX;
        protected final int defaultY;
        protected final Direction defaultDir;
        
        private TileScreen(final List<? extends Option> options, final Wrapper choice, final int cols, final int rows,
                           final int defaultX, final int defaultY, final Direction defaultDir) {
            MonsterGame.options = options;
            MonsterGame.choice = choice;
            this.cols = cols;
            this.rows = rows;
            this.defaultX = defaultX;
            this.defaultY = defaultY;
            this.defaultDir = defaultDir;
        }
        
        @Override
        protected final void load() throws Exception {
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
            
            optMap.clear();
            optWorld = null;
            buildTileMap();
            layerTiles.addActor(tm);
            layerTiles.setConstant(true);
            
            createControlDiamond(layerHud, diamond, diamondIn, ctrl, DEPTH_BUTTON);
            addCursor();
            
            addPlayer();
            layerSprites.addActor(player);
            engine.track(player);
            player.clearLastCity();
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
    
    private final static class WorldScreen extends TileScreen {
        private WorldScreen(final List<? extends Option> options, final Wrapper choice) {
            super(options, choice, 96, 72, 5, 5, Direction.South);
        }
        
        @Override
        protected final void buildTileMap() throws Exception {
            tm.fillBackground(imgMap[13][0]);
            
            for (final Option option : options) {
                final String name = option.getGoal().getName();
                if (name.equals("Menu")) {
                    addMenuButton(option);
                }
            }
        }
    }
    
    private final static class CityScreen extends TileScreen {
        private CityScreen(final List<? extends Option> options, final Wrapper choice) {
            super(options, choice, 32, 24, 13, 1, Direction.North);
        }
        
        @Override
        protected final void buildTileMap() throws Exception {
            final TileMapImage grass = imgMap[13][0];
            final Tile wallBottom = tm.getTile(grass, imgMap[13][2], Tile.BEHAVIOR_SOLID);
            final Tile wallTop = tm.getTile(grass, imgMap[15][2], Tile.BEHAVIOR_SOLID);
            final Tile wallLeft = tm.getTile(grass, imgMap[14][3], Tile.BEHAVIOR_SOLID);
            final Tile wallRight = tm.getTile(grass, imgMap[14][1], Tile.BEHAVIOR_SOLID);
            tm.fillBackground(grass);
            final int cols1 = cols - 1, rows1 = rows - 1;
            for (int i = 1; i < cols1; i++) {
                tm.setTile(i, rows1, wallTop);
                tm.setTile(i, 0, wallBottom);
            }
            for (int j = 1; j < rows1; j++) {
                tm.setTile(0, j, wallLeft);
                tm.setTile(cols1, j, wallRight);
            }
            tm.setForeground(0, rows1, imgMap[13][4], Tile.BEHAVIOR_SOLID);
            tm.setForeground(cols1, rows1, imgMap[13][5], Tile.BEHAVIOR_SOLID);
            tm.setForeground(0, 0, imgMap[14][4], Tile.BEHAVIOR_SOLID);
            tm.setForeground(cols1, 0, imgMap[14][5], Tile.BEHAVIOR_SOLID);
            tm.setForeground(11, 0, imgMap[13][3], Tile.BEHAVIOR_SOLID);
            for (int i = 12; i < 15; i++) {
                tm.setForeground(i, 0, null, Tile.BEHAVIOR_OPEN);
            }
            tm.setForeground(15, 0, imgMap[13][1], Tile.BEHAVIOR_SOLID);
            
            boolean needStore = true, needMorph = true, needTrainers = true, needSpecial = true;
            for (final Option option : options) {
            	final String name = option.getGoal().getName();
                if (name.equals(Data.getStore())) {
                    building(4, 4, 0, 8, 4, 4, 2, option);
                    needStore = false;
                } else if (name.equals(Data.getMorph())) {
                    building(11, 4, 0, 4, 5, 5, 2, option);
                    needMorph = false;
                } else if (name.equals(Data.getTrainers())) {
                    building(4, 11, 5, 4, 7, 5, 3, option);
                    needTrainers = false;
                } else if (name.equals(Special.Specialty.Lab.toString())) {
                    building(14, 11, 0, 12, 7, 4, 3, option);
                    needSpecial = false;
                } else if (name.equals("World")) {
                    optWorld = option;
                } else if (name.equals("Menu")) {
                    addMenuButton(option);
                }
            }
            if (needStore) {
                unusedBuilding(4, 5);
            }
            if (needMorph) {
                unusedBuilding(11, 5);
            }
            if (needTrainers) {
                unusedBuilding(5, 12);
            }
            if (needSpecial) {
                unusedBuilding(15, 12);
            }
        }
    }
    
    private final static void addMenuButton(final Option option) {
        final Pangine engine = Pangine.getEngine();
        final Panmage img = getImage("Menu", true);
        final Panple size = img.getSize();
        final int x = engine.getEffectiveWidth() - (int) size.getX() - 1;
        final int y = engine.getEffectiveHeight() - (int) size.getY() - 1;
        final TouchButton btn = new TouchButton(engine.getInteraction(), layerHud, "Menu", x, y, DEPTH_BUTTON, img, img, true);
        engine.registerTouchButton(btn);
        tm.register(btn, new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) {
                player.updateLastCity();
                choice.value = option;
            }});
    }
    
    private final static void unusedBuilding(final int tlX, final int tlY) {
        building(tlX, tlY, 12, 2, 4, 3, 0, null);
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
            }
        }
        
        protected final void clearLastCity() {
            lastCityX = -1;
            lastCityY = -1;
        }
        
        protected final void updateLastCity() {
            lastCityX = getColumn();
            lastCityY = getRow();
        }
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
    
    private static int addImages(final List<Entity> list, int x, final int y, final int off, final Panmage delim, final Panmage init, final boolean checkPossible) {
        initImageOffsets(delim);
        final int delimOffX = (int) delim.getSize().getX() / 2, delimOffY = OVERLAY_Y + imgOffY;
        boolean first = true;
        for (final Entity s : list) {
            if (first) {
                first = false;
                addImage(init, x - delimOffX, y + delimOffY, 10, false);
            } else {
                addImage(delim, x - delimOffX, y + delimOffY, 10, false);
            }
            addImage(s, x, y, false, !checkPossible || s.isAvailable());
            x += off;
        }
        return x;
    }
    
    private static void addImage(final Entity s, final int x, final int y, final boolean mirror) {
        addImage(s, x, y, mirror, true);
    }
    
    private static void addImage(final Entity s, final int x, final int y, final boolean mirror, final boolean possible) {
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
            System.out.println();
        }
    }
    */
    
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
