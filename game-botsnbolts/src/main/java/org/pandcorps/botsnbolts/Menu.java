/*
Copyright (c) 2009-2018, Andrew M. Martin
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
package org.pandcorps.botsnbolts;

import org.pandcorps.botsnbolts.HudMeter.*;
import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.botsnbolts.RoomLoader.*;
import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.core.img.process.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.in.*;

public class Menu {
    private final static int DIM_BUTTON = 59;
    
    protected static Panmage imgCursor = null;
    protected static ButtonImages circleImages = null;
    protected static ButtonImages rightImages = null;
    protected static ButtonImages upImages = null;
    protected static ButtonImages downImages = null;
    protected static Panmage imgPause = null;
    protected static Panmage imgPlay = null;
    protected static Panmage imgLevelSelect = null;
    protected static Panmage imgQuit = null;
    
    protected static Cursor cursor = null;
    protected static TouchButton jump = null;
    protected static TouchButton attack = null;
    protected static TouchButton right = null;
    protected static TouchButton left = null;
    protected static TouchButton up = null;
    protected static TouchButton down = null;
    protected static TouchButton toggleAttack = null;
    protected static TouchButton toggleJump = null;
    protected static TouchButton pause = null;
    protected static TouchButton play = null;
    protected static TouchButton levelSelect = null;
    protected static TouchButton quit = null;
    
    protected final static void loadMenu() {
        loadCursor();
        loadGameplayButtons();
        loadPauseMenuButtons();
    }
    
    protected final static boolean isCursorNeeded() {
        return Pangine.getEngine().isMouseSupported();
    }
    
    protected final static boolean isScreenGameplayLayoutNeeded() {
        return isTouchEnabled();
    }
    
    protected final static boolean isTouchEnabled() {
        final Pangine engine = Pangine.getEngine();
        return engine.isTouchSupported() && !engine.isMouseSupported();
    }
    
    private final static void loadCursor() {
        if (!isCursorNeeded()) {
            return;
        }
        imgCursor = Pangine.getEngine().createImage("Cursor", new FinPanple2(0, 15), null, null, BotsnBoltsGame.RES + "menu/Cursor.png");
    }
    
    private final static void loadGameplayButtons() {
        if (!isScreenGameplayLayoutNeeded()) {
            return;
        }
        imgPause = Pangine.getEngine().createImage("Pause", BotsnBoltsGame.RES + "menu/Pause.png");
        final Img circle = newButtonImg();
        Imtil.drawCircle(circle, Pancolor.BLACK, Pancolor.BLACK, Pancolor.DARK_GREY);
        final int white = PixelTool.getRgba(Pancolor.WHITE);
        final int grey = PixelTool.getRgba(Pancolor.DARK_GREY);
        final int darkGrey = PixelTool.getRgba(96, 96, 96, Pancolor.MAX_VALUE);
        final int black = PixelTool.getRgba(Pancolor.BLACK);
        ImtilX.highlight(circle, new int[] { white }, true);
        ImtilX.highlight(circle, new int[] { darkGrey, grey, grey, grey, white, black, darkGrey }, false);
        final PixelFilter clearFilter = new ReplacePixelFilter(Pancolor.DARK_GREY, Pancolor.CLEAR);
        circleImages = newButtonImages(circle, clearFilter, DIM_BUTTON, null);
        loadTriangleButton(white, grey, darkGrey, black, clearFilter);
        loadUpButton(white, grey, darkGrey, black);
        loadDownButton(white, grey, darkGrey, black);
    }
    
    private final static void loadPauseMenuButtons() {
        imgPlay = Pangine.getEngine().createImage("Play", BotsnBoltsGame.RES + "menu/Play.png");
        imgLevelSelect = Pangine.getEngine().createImage("LevelSelect", BotsnBoltsGame.RES + "menu/LevelSelect.png");
        imgQuit = Pangine.getEngine().createImage("Quit", BotsnBoltsGame.RES + "menu/Quit.png");
    }
    
    private final static void loadTriangleButton(final int white, final int grey, final int darkGrey, final int black, final PixelFilter clearFilter) {
        final int d1 = DIM_BUTTON - 1, d3 = d1 - 2;
        final Img img = newButtonImg();
        for (int y = 3; y < d3; y++) {
            img.setRGB(0, y, black);
        }
        for (int x = 1; x < d1; x++) {
            final int y;
            if (x == 1) {
                y = 2;
            } else if (x < 4) {
                y = 1;
            } else if (x < 8) {
                y = 0;
            } else {
                y = (x - 6) / 2;
            }
            img.setRGB(x, y, black);
            img.setRGB(x, d1 - y, black);
            final int d1y1 = d1 - y - 1;
            for (int j = y + 2; j < d1y1; j++) {
                img.setRGB(x, j, grey);
            }
            img.setRGB(x, d1y1, darkGrey);
            img.setRGB(x, d1y1 - 4, white);
            img.setRGB(x, d1y1 - 5, black);
            if (x < d3) {
                img.setRGB(x, y + 1, white);
                img.setRGB(x, d1y1 - 6, darkGrey);
            } else {
                img.setRGB(x, y + 1, grey);
            }
        }
        final int y = (d1 - 6) / 2;
        for (int j = 0; j < 7; j++) {
            img.setRGB(d1, y + j, black);
        }
        rightImages = newButtonImages(img, clearFilter, DIM_BUTTON, null);
    }
    
    private final static void loadUpButton(final int white, final int grey, final int darkGrey, final int black) {
        final Img img = newButtonImg(32);
        for (int x = 3; x < 28; x++) {
            img.setRGB(x, 31, black);
            img.setRGB(x, 30, darkGrey);
            img.setRGB(x, 26, white);
            img.setRGB(x, 25, black);
            img.setRGB(x, 24, darkGrey);
        }
        for (int x = 3; x < 16; x++) {
            img.setRGB(x, 16 - x, black);
            img.setRGB(x, 17 - x, white);
        }
        for (int x = 16; x < 28; x++) {
            img.setRGB(x, x - 14, black);
            img.setRGB(x, x - 13, white);
        }
        for (int y = 18; y < 28; y++) {
            img.setRGB(0, y, black);
            img.setRGB(30, y, black);
        }
        for (int i = 0; i < 2; i++) {
            final int x = (i == 0) ? 1 : 29;
            for (int j = 0; j < 2; j++) {
                final int y = (j == 0) ? 16 : 22;
                img.setRGB(x, y, black);
                img.setRGB(x, y + 1, black);
                img.setRGB(x, y + 2, white);
                img.setRGB(x, y + 5, darkGrey);
            }
            img.setRGB(x, 28, black);
            img.setRGB(x, 29, black);
            final int x1 = (i == 0) ? 2 : 28;
            img.setRGB(x1, 14, black);
            img.setRGB(x1, 15, black);
            img.setRGB(x1, 16, white);
            img.setRGB(x1, 23, darkGrey);
            img.setRGB(x1, 24, black);
            img.setRGB(x1, 25, white);
            img.setRGB(x1, 29, darkGrey);
            img.setRGB(x1, 30, black);
        }
        upImages = newButtonImages(img, null, 31, null);
    }
    
    private final static void loadDownButton(final int white, final int grey, final int darkGrey, final int black) {
        final Img img = newButtonImg(32);
        for (int x = 3; x < 28; x++) {
            img.setRGB(x, 1, black);
            img.setRGB(x, 2, white);
            final int y = (x < 16) ? (x + 9) : (39 - x);
            img.setRGB(x, y, darkGrey);
            img.setRGB(x, y + 1, black);
            img.setRGB(x, y + 2, white);
            img.setRGB(x, y + 6, darkGrey);
            img.setRGB(x, y + 7, black);
        }
        for (int y = 26; y < 31; y++) {
            img.setRGB(15, y, black);
        }
        for (int y = 5; y < 15; y++) {
            img.setRGB(0, y, black);
            img.setRGB(30, y, black);
        }
        for (int i = 0; i < 2; i++) {
            final int x = (i == 0) ? 1 : 29;
            for (int j = 0; j < 2; j++) {
                final int y = (j == 0) ? 3 : 9;
                img.setRGB(x, y, black);
                img.setRGB(x, y + 1, black);
                img.setRGB(x, y + 2, white);
                img.setRGB(x, y + 5, darkGrey);
            }
            img.setRGB(x, 15, black);
            img.setRGB(x, 16, black);
            final int x1 = (i == 0) ? 2 : 28;
            img.setRGB(x1, 2, black);
            img.setRGB(x1, 3, white);
            img.setRGB(x1, 10, darkGrey);
            img.setRGB(x1, 11, black);
            img.setRGB(x1, 12, black);
            img.setRGB(x1, 13, white);
            img.setRGB(x1, 16, darkGrey);
            img.setRGB(x1, 17, black);
            img.setRGB(x1, 18, black);
        }
        downImages = newButtonImages(img, null, 31, new ImgProcessor() {
            @Override public final void process(final Img img) {
                final int c = PixelTool.getRgba(Pancolor.CLEAR);
                for (int y = 25; y < 28; y++) {
                    img.setRGB(15, y, c);
                }
                img.setRGB(15, 28, darkGrey);
            }});
    }
    
    private final static Img newButtonImg() {
        return newButtonImg(DIM_BUTTON);
    }
    
    private final static Img newButtonImg(final int d) {
        final Img img = Imtil.newImage(d, d);
        img.setTemporary(false);
        return img;
    }
    
    private final static ButtonImages newButtonImages(final Img img, final PixelFilter clearFilter, final int w, final ImgProcessor postIndentProcessor) {
        final Pangine engine = Pangine.getEngine();
        final Panmage full = engine.createImage(Pantil.vmid(), img);
        final Panmage base;
        if (clearFilter == null) {
            base = full;
        } else {
            Imtil.filterImg(img, clearFilter);
            base = engine.createImage(Pantil.vmid(), img);
        }
        ImtilX.indent2(img, 4, w);
        ImgProcessor.process(postIndentProcessor, img);
        final Panmage pressed = engine.createImage(Pantil.vmid(), img);
        img.close();
        return new ButtonImages(full, base, pressed);
    }
    
    private final static Cursor addCursor(final Panlayer room) {
        if (!isCursorNeeded()) {
            return null;
        }
        cursor = Cursor.addCursor(room, imgCursor);
        cursor.getPosition().setZ(BotsnBoltsGame.DEPTH_CURSOR);
        return cursor;
    }
    
    protected final static void addGameplayButtonInputs() {
        destroyGameplayButtons();
        addGameplayButtons(true, false);
    }
    
    protected final static void addGameplayButtonActors() {
        addGameplayButtons(false, true);
    }
    
    private final static void destroyGameplayButtons() {
        Panctor.destroy(cursor);
        TouchButton.destroy(jump);
        TouchButton.destroy(attack);
        TouchButton.destroy(right);
        TouchButton.destroy(left);
        TouchButton.destroy(up);
        TouchButton.destroy(down);
        TouchButton.destroy(toggleAttack);
        TouchButton.destroy(toggleJump);
    }

    private final static void addGameplayButtons(final boolean input, final boolean act) {
        if (!isScreenGameplayLayoutNeeded()) {
            return;
        }
        final Panlayer hud = BotsnBoltsGame.hud;
        final TouchButtonActiveListener activeListener = new TouchButtonActiveListener() {
            @Override public final void onActive(final TouchButton btn) {
                jump.setImageInactive(circleImages.base);
                attack.setImageInactive(circleImages.base);
                right.setImageInactive(rightImages.base);
                left.setImageInactive(rightImages.base);
            }};
        final Pangine engine = Pangine.getEngine();
        final int w = engine.getEffectiveWidth(), h = engine.getEffectiveHeight();
        final int o = 1;
        final int d = DIM_BUTTON;
        jump = addButton(hud, "Jump", w - d - o, o, input, act, jump, circleImages, false, activeListener, false, d);
        attack = addButton(hud, "Attack", w - (2 * d) - 1 - o, o, input, act, attack, circleImages, false, activeListener, false, d);
        right = addButton(hud, "Right", d + 1 + o, o, input, act, right, rightImages, false, activeListener, false, d);
        left = addButton(hud, "Left", o, o, input, act, left, rightImages, false, activeListener, true, d);
        final int ds = 31, upDownX = o + d - 15;
        up = addButton(hud, "Up", upDownX, h - d, input, act, up, upImages, false, activeListener, false, ds);
        down = addButton(hud, "Down", upDownX, o + d + 1, input, act, down, downImages, false, activeListener, false, ds);
        if (act) {
            addCursor(hud);
        }
    }
    
    protected final static void addToggleButtons(final HudShootMode hudShootMode, final HudJumpMode hudJumpMode) {
        toggleJump = addToggleButton("ToggleJump", hudJumpMode);
        toggleAttack = addToggleButton("ToggleAttack", hudShootMode);
        addPauseButton(hudShootMode);
    }
    
    private final static void addPauseButton(final HudShootMode hudShootMode) {
        if (!isScreenGameplayLayoutNeeded()) {
            return;
        }
        final Panple pos = hudShootMode.getPosition();
        final int pd = 16;
        final int px = Pangine.getEngine().getEffectiveWidth() - pd - Math.round(pos.getX()), py = Math.round(pos.getY());
        pause = addButton(BotsnBoltsGame.hud, "Pause", px, py, true, true, null, imgPause, imgPause, false, null, false, pd);
    }
    
    private final static TouchButton addToggleButton(final String name, final HudIcon hudIcon) {
        final Panlayer hud = BotsnBoltsGame.hud;
        hud.addActor(hudIcon);
        final Panple pos = hudIcon.getPosition();
        final TouchButton button = addButton(hud, name, Math.round(pos.getX()), Math.round(pos.getY()), true, false, null, null, null, false, null, false, 18);
        button.setLayer(hud);
        return button;
    }
    
    private final static TouchButton addButton(final Panlayer room, final String name, final int x, final int y,
            final boolean input, final boolean act, final Panput old, final ButtonImages images,
            final boolean moveCancel, final TouchButtonActiveListener activeListener, final boolean mirror, final int d) {
        return addButton(room, name, x, y, input, act, old, images.full, images.pressed, moveCancel, activeListener, mirror, d);
    }
    
    private final static TouchButton addButton(final Panlayer room, final String name, final int x, final int y,
            final boolean input, final boolean act, final Panput old, final Panmage full, final Panmage pressed,
            final boolean moveCancel, final TouchButtonActiveListener activeListener, final boolean mirror, final int d) {
        final TouchButton button;
        if (input) {
            final Pangine engine = Pangine.getEngine();
            final Panteraction in = engine.getInteraction();
            button = new TouchButton(in, name, x, y, d, d, moveCancel);
            engine.registerTouchButton(button);
        } else {
            button = (TouchButton) old;
        }
        if (act) {
            final Panctor actor = new Panctor();
            actor.setView(full);
            button.setActor(actor, pressed);
            button.setActiveListener(activeListener);
            actor.getPosition().set(x + (mirror ? (DIM_BUTTON - 1) : 0), y, BotsnBoltsGame.DEPTH_HUD);
            actor.setMirror(mirror);
            room.addActor(actor);
        }
        return button;
    }
    
    protected final static void showUpDown() {
        if (TouchButton.reattach(up)) {
            TouchButton.reattach(down);
        }
    }
    
    protected final static void hideUpDown() {
        if (TouchButton.detach(up)) {
            TouchButton.detach(down);
        }
    }
    
    private static TouchButton[] pauseMenuButtons = new TouchButton[3];
    private static int pauseMenuIndex = 0;
    
    protected final static void addPauseMenu(final Player player) {
        final int numBtns = 3, btnSize = 32, spaceBetween = 32, nextOffset = btnSize + spaceBetween;
        final int px = (BotsnBoltsGame.GAME_W - (numBtns * btnSize) - ((numBtns - 1) * spaceBetween)) / 2;
        final int py = (BotsnBoltsGame.GAME_H - btnSize) / 2;
        final Panlayer layer = BotsnBoltsGame.hud;
        final Panmage active = player.pi.highlightBox;
        pauseMenuIndex = 0;
        pauseMenuButtons[0] = play = addPauseMenuButton(layer, "Play", px, py, active, imgPlay);
        player.registerPause(play);
        play.activate(true);
        pauseMenuButtons[1] = levelSelect = addPauseMenuButton(layer, "LevelSelect", px + nextOffset, py, active, imgLevelSelect);
        player.healthMeter.register(levelSelect, new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) {
                destroyPauseMenu();
                goLevelSelect();
            }});
        pauseMenuButtons[2] = quit = addPauseMenuButton(layer, "Quit", px + (nextOffset * 2), py, active, imgQuit);
        player.healthMeter.register(quit, new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) {
                Pangine.getEngine().exit();
            }});
        addCursor(layer);
        final Panctor actor = play.getActor();
        for (int i = 0; i < 3; i++) {
            final TouchButton pauseMenuButton = pauseMenuButtons[i];
            final int newPauseMenuIndex = i;
            actor.register(pauseMenuButton, new ActionStartListener() {
                @Override public final void onActionStart(final ActionStartEvent event) {
                    setPauseMenuButton(newPauseMenuIndex);
                }});
            pauseMenuButton.setActiveListener(new TouchButtonActiveListener() {
                @Override public final void onActive(final TouchButton btn) {
                    setPauseMenuButton(newPauseMenuIndex);
                }});
        }
        final ControlScheme ctrl = player.pc.ctrl;
        actor.register(ctrl.getRight(), new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) {
                changePauseMenuButton(1);
            }});
        actor.register(ctrl.getLeft(), new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) {
                changePauseMenuButton(-1);
            }});
        final ActionEndListener selectListener = new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) {
                final TouchButton pauseMenuButton = pauseMenuButtons[pauseMenuIndex];
                if (!pauseMenuButton.isActivated()) {
                    pauseMenuButton.activate(true);
                    return;
                }
                for (final ActionEndListener listener : Coltil.copy(Pangine.getEngine().getInteraction().getEndListeners(pauseMenuButton))) {
                    listener.onActionEnd(event);
                }
            }};
        actor.register(ctrl.get1(), selectListener);
        actor.register(ctrl.get2(), selectListener);
    }
    
    private final static void changePauseMenuButton(final int dir) {
        if (((dir == -1) && (pauseMenuIndex == 0)) || ((dir == 1) && (pauseMenuIndex == 2))) {
            final TouchButton pauseMenuButton = pauseMenuButtons[pauseMenuIndex];
            if (!pauseMenuButton.isActivated()) {
                pauseMenuButton.activate(true);
            }
            return;
        }
        setPauseMenuButton(pauseMenuIndex + dir);
    }
    
    private final static void setPauseMenuButton(final int newPauseMenuIndex) {
        if (newPauseMenuIndex == pauseMenuIndex) {
            return;
        }
        pauseMenuButtons[pauseMenuIndex].activate(false);
        pauseMenuIndex = newPauseMenuIndex;
        pauseMenuButtons[pauseMenuIndex].activate(true);
    }
    
    private final static TouchButton addPauseMenuButton(final Panlayer layer, final String name, final int x, final int y, final Panmage active, final Panmage img) {
        final TouchButton button;
        final Pangine engine = Pangine.getEngine();
        final Panteraction in = engine.getInteraction();
        button = new TouchButton(in, name, x, y, 32, 32, true);
        button.setLayer(layer);
        engine.registerTouchButton(button);
        final Panctor actor = new Panctor();
        actor.setView(BotsnBoltsGame.getBox());
        button.setActor(actor, active);
        actor.getPosition().set(x, y, BotsnBoltsGame.DEPTH_HUD);
        button.setOverlay(img, 8, 8);
        layer.addActor(actor);
        return button;
    }
    
    protected final static void destroyPauseMenu() {
        Panctor.destroy(cursor);
        TouchButton.destroy(play);
        play = null;
        TouchButton.destroy(levelSelect);
        TouchButton.destroy(quit);
    }
    
    protected final static boolean isPauseMenuEnabled() {
        return play != null;
    }
    
    private final static class ButtonImages {
        private final Panmage full;
        private final Panmage base;
        private final Panmage pressed;
        
        private ButtonImages(final Panmage full, final Panmage base, final Panmage pressed) {
            this.full = full;
            this.base = base;
            this.pressed = pressed;
        }
    }
    
    protected final static void goLevelSelect() {
        Panscreen.set(new LevelSelectScreen());
    }
    
    protected final static class LevelSelectScreen extends Panscreen {
        private static Panmage imgEmpty = null;
        private static LevelSelectGrid grid = null;
        
        @Override
        protected final void load() throws Exception {
            final Pangine engine = Pangine.getEngine();
            final PlayerContext pc = BotsnBoltsGame.pc;
            pc.prf.saveProfile();
            BotsnBoltsGame.initPlayerStart();
            if (imgEmpty == null) {
                imgEmpty = engine.createEmptyImage("select.level", FinPanple.ORIGIN, FinPanple.ORIGIN, new FinPanple2(48, 48));
            }
            engine.setBgColor(new FinPancolor(96, 96, 96));
            final Pangame game = Pangame.getGame();
            Panroom room = game.getCurrentRoom();
            final float roomZ = room.getSize().getZ();
            room.destroy();
            room = Pangine.getEngine().createRoom(Pantil.vmid(), BotsnBoltsGame.GAME_W, BotsnBoltsGame.GAME_H, roomZ);
            game.setCurrentRoom(room);
            final Panlayer layer = engine.createLayer("layer.grid", BotsnBoltsGame.GAME_W, BotsnBoltsGame.GAME_H, roomZ, room);
            grid = new LevelSelectGrid();
            layer.addActor(grid);
            Player.registerCapture(grid);
            for (final BotLevel level : RoomLoader.levels) {
                final int x = 88 + (level.selectX * 80), y = 24 + (level.selectY * 64), x24 = x + 24;
                BotsnBoltsGame.addText(layer, level.name1, x24, y - 8);
                BotsnBoltsGame.addText(layer, level.name2, x24, y - 16);
                addPortrait(layer, x + 8, y + 8, level);
                addLevelButton(room, x, y, level);
            }
            addPortrait(layer, 176, 96, pc.pi.portrait, true);
            layer.setConstant(true);
            room.addBeneath(layer);
            addCursor(room);
        }
        
        private final static void addPortrait(final Panlayer layer, final int x, final int y, final BotLevel level) {
            addPortrait(layer, x, y, level.portrait, (x < 176) || ((x == 176) && (y < 112)));
        }
        
        private final static void addPortrait(final Panlayer layer, final int x, final int y, final Panmage image, final boolean right) {
            final Panctor portrait = new Panctor();
            final int off;
            if (right) {
                off = 0;
            } else {
                off = 31;
                portrait.setMirror(true);
            }
            portrait.getPosition().set(x + off, y, BotsnBoltsGame.DEPTH_ENEMY);
            portrait.setView(image);
            layer.addActor(portrait);
        }
        
        private final static void addLevelButton(final Panlayer layer, final int x, final int y, final BotLevel level) {
            final Pangine engine = Pangine.getEngine();
            final TouchButton btn = new TouchButton(engine.getInteraction(), layer, "level." + x + "." + y, x, y, BotsnBoltsGame.DEPTH_FG, imgEmpty, null, true);
            engine.registerTouchButton(btn);
            grid.register(btn, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    startLevel(level);
                }});
            final LevelSelectCell cell = grid.cells[level.selectY][level.selectX];
            cell.level = level;
            grid.register(btn, new ActionStartListener() {
                @Override public final void onActionStart(final ActionStartEvent event) {
                    grid.currentCell = cell;
                }});
            btn.setActiveListener(new TouchButtonActiveListener() {
                @Override public final void onActive(final TouchButton btn) {
                    grid.currentCell = cell;
                }});
        }
    }
    
    private final static void startLevel(final BotLevel level) {
        RoomLoader.startX = level.levelX;
        RoomLoader.startY = level.levelY;
        Panscreen.set(new BotsnBoltsGame.BotsnBoltsScreen());
    }
    
    private final static class LevelSelectGrid extends Panctor {
        private static Panmage bg = null;
        private final LevelSelectCell[][] cells = new LevelSelectCell[3][3];
        private LevelSelectCell currentCell;
        
        {
            for (int j = 0; j < 3; j++) {
                final LevelSelectCell[] row = cells[j];
                for (int i = 0; i < 3; i++) {
                    row[i] = new LevelSelectCell(i, j);
                }
            }
            currentCell = cells[1][1];
            final ControlScheme ctrl = BotsnBoltsGame.pc.ctrl;
            register(ctrl.getUp(), new ActionStartListener() {
                @Override public final void onActionStart(final ActionStartEvent event) {
                    if (currentCell.j < 2) {
                        currentCell = cells[currentCell.j + 1][currentCell.i];
                    }
                }});
            register(ctrl.getDown(), new ActionStartListener() {
                @Override public final void onActionStart(final ActionStartEvent event) {
                    if (currentCell.j > 0) {
                        currentCell = cells[currentCell.j - 1][currentCell.i];
                    }
                }});
            register(ctrl.getRight(), new ActionStartListener() {
                @Override public final void onActionStart(final ActionStartEvent event) {
                    if (currentCell.i < 2) {
                        currentCell = cells[currentCell.j][currentCell.i + 1];
                    }
                }});
            register(ctrl.getLeft(), new ActionStartListener() {
                @Override public final void onActionStart(final ActionStartEvent event) {
                    if (currentCell.i > 0) {
                        currentCell = cells[currentCell.j][currentCell.i - 1];
                    }
                }});
            final ActionEndListener selectListener = new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    final BotLevel level = currentCell.level;
                    if (level != null) {
                        startLevel(level);
                    }
                }};
            register(ctrl.get1(), selectListener);
            register(ctrl.get2(), selectListener);
            register(ctrl.getSubmit(), selectListener);
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panlayer layer = getLayer();
            if (bg == null) {
                bg = Pangine.getEngine().createImage("select.bg", BotsnBoltsGame.RES + "menu/SelectBg.png");
            }
            final Panmage box = BotsnBoltsGame.getBox();
            for (final LevelSelectCell[] row : cells) {
                int y = 0;
                for (final LevelSelectCell cell : row) {
                    renderBox(renderer, layer, cell.x, y = cell.y, (cell == currentCell) ? BotsnBoltsGame.pc.pi.highlightBox : box);
                }
                renderer.render(layer, bg, 0, y + 8, BotsnBoltsGame.DEPTH_BEHIND, 0, 0, BotsnBoltsGame.GAME_W, 32, 0, false, false);
            }
        }
        
        private final static void renderBox(final Panderer renderer, final Panlayer layer, final int x, final int y, final Panmage img) {
            final int x8 = x + 8, y8 = y + 8, x40 = x + 40, y40 = y + 40;
            renderer.render(layer, img, x, y, BotsnBoltsGame.DEPTH_BG, 0, 24, 8, 8, 0, false, false);
            renderer.render(layer, img, x40, y, BotsnBoltsGame.DEPTH_BG, 24, 24, 8, 8, 0, false, false);
            renderer.render(layer, img, x, y40, BotsnBoltsGame.DEPTH_BG, 0, 0, 8, 8, 0, false, false);
            renderer.render(layer, img, x40, y40, BotsnBoltsGame.DEPTH_BG, 24, 0, 8, 8, 0, false, false);
            for (int i = 0; i < 2; i++) {
                final int i16 = i * 16;
                renderer.render(layer, img, x8 + i16, y, BotsnBoltsGame.DEPTH_BG, 8, 24, 16, 8, 0, false, false);
                renderer.render(layer, img, x8 + i16, y40, BotsnBoltsGame.DEPTH_BG, 8, 0, 16, 8, 0, false, false);
                renderer.render(layer, img, x, y8 + i16, BotsnBoltsGame.DEPTH_BG, 0, 8, 8, 16, 0, false, false);
                renderer.render(layer, img, x40, y8 + i16, BotsnBoltsGame.DEPTH_BG, 24, 8, 8, 16, 0, false, false);
            }
        }
    }
    
    private final static class LevelSelectCell {
        private final int i;
        private final int j;
        private final int x;
        private final int y;
        private BotLevel level = null;
        
        private LevelSelectCell(final int i, final int j) {
            this.i = i;
            this.j = j;
            x = 88 + (i * 80);
            y = 24 + (j * 64);
        }
    }
}
