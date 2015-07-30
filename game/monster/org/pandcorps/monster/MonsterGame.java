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
import org.pandcorps.game.core.*;
import org.pandcorps.monster.Driver.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Fonts.*;
import org.pandcorps.pandax.touch.*;

public final class MonsterGame extends BaseGame {
    /*
    Breed/lab don't show options already on team
    Show disabled Catch if on team
    Upgrade screen show requirements
    Show player's money on buy/sell
    Database screen
    */
    private static volatile Driver driver = null;
    private static volatile Panroom room = null;
    
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
    private static volatile Panmage menu = null;
    private static volatile Panmage menuIn = null;
    private static volatile Panmage menuOff = null;
    private static volatile Panmage menuCursor = null;
    //private static volatile Panmage menuLeft = null;
    //private static volatile Panmage menuRight = null;
    private final static Map<String, Panmage> imageCache = new HashMap<String, Panmage>();
    
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
                    Panscreen.set(new MonsterScreen(caller, label, options, choice));
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
    
    private final static class MonsterScreen extends Panscreen {
        private final Option caller;
        private final Label label;
        private final List<? extends Option> options;
        private final Wrapper choice;
        
        private MonsterScreen(final Option caller, final Label label, final List<? extends Option> options, final Wrapper choice) {
            this.caller = caller;
            this.label = label;
            this.options = options;
            this.choice = choice;
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
            final Cursor cursor = Cursor.addCursor(room, menuCursor);
            if (cursor != null) {
                cursor.getPosition().setZ(20);
            }
            //TouchTabs.setFullScreen(true);
            //final List<TouchButton> buttons = new ArrayList<TouchButton>();
            final Pangine engine = Pangine.getEngine();
            engine.setBgColor(new Pancolor((short) 160));
            final Panteraction interaction = engine.getInteraction();
            int numRows = Mathtil.ceil(options.size() / 3f), titleOffset = 10;
            final boolean chosenDisplayed = caller instanceof BattleOption;
            if (caller instanceof BattleOption) {
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
            } else {
                final Pantext lbl = new Pantext(Pantil.vmid(), font, formatLabel(label.getName()));
                lbl.getPosition().set(1, y + menuH + 1);
                room.addActor(lbl);
            }
            for (final Option option : options) {
                /*if (!option.isPossible()) {
                    continue;
                }*/
                final String labelName = option.getGoal().getName();
                final String name = formatLabel(labelName);
                /*buttons.add(TouchTabs.newButton(room, name, menu, menuIn, null, 3, 10, font, name, 3, 2, new Runnable() {
                    @Override public final void run() {
                        // Check possible
                        choice.value = option;
                    }}));*/
                final boolean possible = option.isPossible();
                final Panmage img = getImage(labelName, possible);
                final int imgOffX, imgOffY;
                if (img == null) {
                    imgOffX = 0;
                    imgOffY = 0;
                } else {
                    final Panple size = img.getSize();
                    imgOffX = (IMG_W - (int) size.getX()) / 2;
                    imgOffY = (IMG_H - (int) size.getY()) / 2;
                }
                final TouchButton btn = new TouchButton(interaction, room, name, x, y + btnOffY, 0, menu, menuIn,
                    img, OVERLAY_X + imgOffX, OVERLAY_Y + imgOffY,
                    (name.length() > 10) ? fontTiny : font, name, TEXT_X, TEXT_Y, true);
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
                if (!possible) {
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
    
    private static void addImage(final Species s, final int x, final int y, final boolean mirror) {
        final String name = s.getName();
        final Panctor actor = new Panctor();
        final Panmage image = getImage(name, true);
        actor.setView(image);
        //actor.getPosition().set(x, y);
        actor.getPosition().set(x + OVERLAY_X + (mirror ? image.getSize().getX() + 1 : 0), y + OVERLAY_Y);
        actor.setMirror(mirror);
        room.addActor(actor);
        final Pantext text = new Pantext(Pantil.vmid(), font, name);
        text.getPosition().set(x + TEXT_X, y + TEXT_Y);
        room.addActor(text);
    }
    
    private static String formatLabel(final String name) {
        return ImgFont.format(name);
    }
    
    private static String formatFile(final String name) {
        return Chartil.remove(Chartil.remove(Chartil.removeAccents(name), ' '), '\'');
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
