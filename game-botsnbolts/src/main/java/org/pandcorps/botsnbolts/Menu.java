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
package org.pandcorps.botsnbolts;

import java.util.*;

import org.pandcorps.botsnbolts.HudMeter.*;
import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.botsnbolts.RoomLoader.*;
import org.pandcorps.botsnbolts.Story.*;
import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.core.img.process.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;

public class Menu {
    private final static int DIM_BUTTON = 59;
    private final static int LEVEL_SELECT_X = 8;
    private final static int LEVEL_SELECT_Y = 24;
    private final static int LEVEL_W = 80;
    private final static int LEVEL_H = 64;
    private final static int LEVEL_COLUMNS = 5;
    private final static int LEVEL_ROWS = 3;
    private final static int LEVEL_DEFAULT_ROW = 1;
    private final static int LEVEL_DEFAULT_COLUMN = 2;
    
    protected static Panmage imgCursor = null;
    protected static ButtonImages circleImages = null;
    protected static ButtonImages rightImages = null;
    protected static ButtonImages upImages = null;
    protected static ButtonImages downImages = null;
    protected static Panmage imgPause = null;
    protected static Panmage imgPlay = null;
    protected static Panmage imgLevelSelect = null;
    protected static Panmage imgQuit = null;
    protected static Panmage imgOptions = null;
    protected static Panmage imgOn = null;
    protected static Panmage imgOff = null;
    
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
        imgOptions = Pangine.getEngine().createImage("Options", BotsnBoltsGame.RES + "menu/Options.png");
        imgOn = Pangine.getEngine().createImage("On", BotsnBoltsGame.RES + "menu/On.png");
        imgOff = Pangine.getEngine().createImage("Off", BotsnBoltsGame.RES + "menu/Off.png");
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
        cursor = Cursor.addCursorIfNeeded(room, imgCursor);
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
        final int w = engine.getEffectiveWidth();
        final int o = 1;
        final int d = DIM_BUTTON;
        jump = addButton(hud, "Jump", w - d - o, o, input, act, jump, circleImages, false, activeListener, false, d);
        attack = addButton(hud, "Attack", w - (2 * d) - 1 - o, o, input, act, attack, circleImages, false, activeListener, false, d);
        right = addButton(hud, "Right", d + 1 + o, o, input, act, right, rightImages, false, activeListener, false, d);
        left = addButton(hud, "Left", o, o, input, act, left, rightImages, false, activeListener, true, d);
        final int ds = 31, upDownX = o + d - 15;
        up = addButton(hud, "Up", upDownX, o + d + 1 + Math.round(downImages.base.getSize().getY()), input, act, up, upImages, false, activeListener, false, ds);
        down = addButton(hud, "Down", upDownX, o + d + 1, input, act, down, downImages, false, activeListener, false, ds);
        if (act) {
            addCursor(hud);
        }
    }
    
    protected final static void addToggleButtons(final HudShootMode hudShootMode, final HudJumpMode hudJumpMode) {
        toggleJump = addToggleButton("ToggleJump", hudJumpMode);
        toggleAttack = addToggleButton("ToggleAttack", hudShootMode);
        addPauseButton();
    }
    
    private final static void addPauseButton() {
        if (!isScreenGameplayLayoutNeeded()) {
            return;
        }
        final int pd = 16;
        // Some devices have rounded corners and won't display an image exactly in the corner, so mirror the toggleAttack button which is lower
        final int px = BotsnBoltsGame.GAME_W - pd - HudMeter.HUD_ICON_X, py = BotsnBoltsGame.GAME_H - HudMeter.HUD_ICON_TOP_YOFF;
        TouchButton.destroy(pause);
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
    
    private final static TouchButton addTopRightButton(final Panlayer layer, final String name, final Panmage img, final Panctor src, final ActionEndListener listener) {
        // Some devices have rounded corners and won't display an image exactly in the corner, so move inward
        final TouchButton btn = addButton(layer, name, BotsnBoltsGame.GAME_W - 31, BotsnBoltsGame.GAME_H - 23, true, true, null, img, img, false, null, false, 16);
        src.register(btn, listener);
        return btn;
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
    
    private static int numPauseMenuButtons = 3;
    private final static TouchButton[] pauseMenuButtons = new TouchButton[numPauseMenuButtons];
    private static int pauseMenuIndex = 0;
    
    protected final static void addPauseMenu(final Player player) {
        addPauseMenu(BotsnBoltsGame.hud, player.pc, player.healthMeter, player, true);
    }
    
    private final static void addPauseMenu(final Panlayer layer, final PlayerContext pc, final Panctor registrar, final Player player, final boolean levelSelectNeeded) {
        for (final TouchButton btn : levelButtons) {
            btn.setEnabled(false);
        }
        Panlayer.setVisible(levelSelectLayer, false);
        final boolean levelSelectReallyNeeded = levelSelectNeeded && RoomLoader.isFirstLevelFinished();
        final boolean optionsNeeded = !levelSelectNeeded;
        final int numBtns = (levelSelectReallyNeeded || optionsNeeded) ? 3 : 2, btnSize = 32, spaceBetween = 48, nextOffset = btnSize + spaceBetween;
        int px = (BotsnBoltsGame.GAME_W - (numBtns * btnSize) - ((numBtns - 1) * spaceBetween)) / 2;
        final int py = (BotsnBoltsGame.GAME_H - btnSize) / 2;
        final Panmage active = pc.pi.highlightBox;
        pauseMenuIndex = 0;
        int currIndex = 0;
        pauseMenuButtons[currIndex++] = play = addPauseMenuButton(layer, "Play", px, py, active, imgPlay);
        px += nextOffset;
        numPauseMenuButtons = numBtns;
        if (player == null) {
            registrar.register(play, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    destroyPauseMenu();
                }});
        } else {
            player.registerPause(play);
        }
        play.activate(true);
        if (levelSelectReallyNeeded) {
            pauseMenuButtons[currIndex++] = levelSelect = addPauseMenuButton(layer, "LevelSelect", px, py, active, imgLevelSelect);
            px += nextOffset;
            registrar.register(levelSelect, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    destroyPauseMenu();
                    RoomLoader.clear(); // Exit from Bolt room to Menu; start Earthquake level; TileMapImage Map wasn't cleared without this, causing graphical bug
                    goLevelSelect();
                }});
        } else if (optionsNeeded) {
            pauseMenuButtons[currIndex++] = levelSelect = addPauseMenuButton(layer, "Options", px, py, active, imgOptions);
            px += nextOffset;
            registrar.register(levelSelect, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    destroyPauseMenu();
                    goOptions();
                }});
        }
        pauseMenuButtons[currIndex++] = quit = addPauseMenuButton(layer, "Quit", px, py, active, imgQuit);
        registrar.register(quit, new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) {
                Pangine.getEngine().exit();
            }});
        addCursor(layer);
        final Panctor actor = play.getActor();
        for (int i = 0; i < 3; i++) {
            final TouchButton pauseMenuButton = pauseMenuButtons[i];
            if (pauseMenuButton == null) {
                continue;
            }
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
        final ControlScheme ctrl = pc.ctrl;
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
        if (((dir == -1) && (pauseMenuIndex == 0)) || ((dir == 1) && (pauseMenuIndex == (numPauseMenuButtons - 1)))) {
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
        if (!(Panscreen.get() instanceof LevelSelectScreen) && !isTouchEnabled()) {
            Panctor.destroy(cursor);
        }
        Panlayer.setVisible(levelSelectLayer, true);
        TouchButton.destroy(play);
        play = null;
        TouchButton.destroy(levelSelect);
        TouchButton.destroy(quit);
        for (final TouchButton btn : levelButtons) {
            btn.setEnabled(true);
        }
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
        final Pangine engine = Pangine.getEngine();
        if (engine.isPaused()) {
            engine.setPaused(false);
        }
        Panscreen.set(RoomLoader.isFirstLevelFinished() ? new LevelSelectScreen() : new LabScreen1());
    }
    
    private final static List<TouchButton> levelButtons = new ArrayList<TouchButton>(12);
    private static Panlayer levelSelectLayer = null;
    
    private final static void clearLevelButtons() {
        if (BotsnBoltsGame.room != null) {
            BotsnBoltsGame.room.setClearDepthEnabled(true);
        }
        levelButtons.clear();
        levelSelectLayer = null;
    }
    
    protected final static class LevelSelectScreen extends Panscreen {
        private static Panmage imgEmpty = null;
        private static LevelSelectGrid grid = null;
        
        @Override
        protected final void load() throws Exception {
            clearLevelButtons();
            final Pangine engine = Pangine.getEngine();
            BotsnBoltsGame.musicLevelSelect.changeMusic();
            final PlayerContext pc = BotsnBoltsGame.pc;
            final Profile prf = pc.prf;
            prf.saveProfile();
            BotsnBoltsGame.initPlayerStart();
            if (imgEmpty == null) {
                imgEmpty = engine.createEmptyImage("select.level", FinPanple.ORIGIN, FinPanple.ORIGIN, new FinPanple2(48, 48));
            }
            engine.setBgColor(new FinPancolor(96));
            final Pangame game = Pangame.getGame();
            Panroom room = game.getCurrentRoom();
            final int roomW = BotsnBoltsGame.GAME_W, roomH = BotsnBoltsGame.GAME_H;
            final float roomZ = room.getSize().getZ();
            room.destroy();
            room = engine.createRoom(Pantil.vmid(), roomW, roomH, roomZ);
            final Panroom newRoom = room;
            game.setCurrentRoom(room);
            BotsnBoltsGame.room = room;
            room.setClearDepthEnabled(false);
            final Panlayer layer = levelSelectLayer = engine.createLayer("layer.grid", roomW, roomH, roomZ, room);
            grid = new LevelSelectGrid();
            room.addActor(grid);
            Player.registerCapture(grid);
            BotLevel centerLevel = null;
            boolean allBasicFinished = true, anyDenied = false;
            LevelSelectCell suggestedCell = null;
            for (final BotLevel level : RoomLoader.levels) {
                if (level.isSpecialLevel()) {
                    if (!level.isAllowed()) {
                        anyDenied = true;
                        continue;
                    } else if (!level.isReplayable()) {
                        if (!level.isFinished()) {
                            centerLevel = level;
                        }
                        continue;
                    }
                } else if (!level.isFinished()) {
                    allBasicFinished = false;
                }
                final LevelSelectCell cell = addLevelButton(layer, level.selectX, level.selectY, level);
                if ((suggestedCell == null) && (level.boltName) != null && !prf.isUpgradeAvailable(level.boltName)) {
                    suggestedCell = cell;
                }
            }
            if (allBasicFinished && anyDenied && (centerLevel == null)) {
                if (prf.upgrades.isEmpty()) {
                    BotsnBoltsGame.notify("Collect upgrades to reach the next level!");
                } else {
                    BotsnBoltsGame.notify("Keep looking for more upgrades!");
                }
            }
            if (centerLevel == null) {
                final int x = 176, y = 96;
                addPortrait(layer, x, y, pc.pi.portrait, true);
                grid.pupils = new Pupils(layer, x, y);
            } else {
                addLevelButton(room, 2, 1, centerLevel);
            }
            if ((suggestedCell != null) && prf.levelSuggestions) {
                grid.setCurrentCell(suggestedCell, false);
            }
            addTopRightButton(layer, "Quit", imgOptions, grid, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    if (isGridEnabled()) {
                        addQuitMenu(newRoom, pc);
                    }
                }});
            grid.register(pc.ctrl.getMenu(), new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    if (isPauseMenuEnabled()) {
                        destroyPauseMenu();
                    } else {
                        addQuitMenu(newRoom, pc);
                    }
                }});
            grid.register(engine.getInteraction().BACK, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    if (isPauseMenuEnabled()) {
                        engine.exit();
                    } else {
                        addQuitMenu(newRoom, pc);
                    }
                }});
            layer.setConstant(true);
            room.addBeneath(layer);
            addCursor(room);
        }
        
        @Override
        protected final void destroy() {
            clearLevelButtons();
        }
        
        private final static void addQuitMenu(final Panlayer layer, final PlayerContext pc) {
            addPauseMenu(layer, pc, grid, null, false);
        }
        
        private final static void addPortrait(final Panlayer layer, final int x, final int y, final BotLevel level) {
            final Boolean portraitMirror = level.portraitMirror;
            final boolean allFinished = RoomLoader.levels.get(RoomLoader.levels.size() - 1).isReplayable();
            final Panmage portrait = (!allFinished && level.isFinished() && (level.portraitGrey != null)) ? level.portraitGrey : level.portrait;
            addPortrait(layer, x, y, portrait, (portraitMirror == null) ? ((x < 176) || ((x == 176) && (y < 112))) : !portraitMirror.booleanValue());
        }
        
        private final static void addPortrait(final Panlayer layer, final int x, final int y, final Panmage image, final boolean right) {
            final Panctor portrait = new Panctor();
            final int sizeOff = (32 - Math.round(image.getSize().getX())) / 2, off;
            if (right) {
                off = sizeOff;
            } else {
                off = 31 - sizeOff;
                portrait.setMirror(true);
            }
            portrait.getPosition().set(x + off, y + sizeOff, BotsnBoltsGame.DEPTH_ENEMY);
            portrait.setView(image);
            layer.addActor(portrait);
        }
        
        private final static LevelSelectCell addLevelButton(final Panlayer layer, final int selectX, final int selectY, final BotLevel level) {
            final int x = LEVEL_SELECT_X + (selectX * LEVEL_W), y = LEVEL_SELECT_Y + (selectY * LEVEL_H), x24 = x + 24;
            BotsnBoltsGame.addText(layer, level.name1, x24, y - 8);
            BotsnBoltsGame.addText(layer, level.name2, x24, y - 16);
            final int portraitX = x + 8, portraitY = y + 8;
            addPortrait(layer, portraitX, portraitY, level);
            if (Story.isPupilNeeded(level.portrait)) {
                new Pupils(layer, portraitX, portraitY);
            }
            final Pangine engine = Pangine.getEngine();
            final TouchButton btn = new TouchButton(engine.getInteraction(), layer, "level." + x + "." + y, x, y, BotsnBoltsGame.DEPTH_FG, imgEmpty, null, true);
            levelButtons.add(btn);
            engine.registerTouchButton(btn);
            grid.register(btn, new GridEndListener() {
                @Override public final void onGridEnd() {
                    startLevel(level);
                }});
            final LevelSelectCell cell = grid.cells[selectY][selectX];
            cell.level = level;
            grid.register(btn, new GridStartListener() {
                @Override public final void onGridStart() {
                    grid.setCurrentCell(cell);
                }});
            btn.setActiveListener(new TouchButtonActiveListener() {
                @Override public final void onActive(final TouchButton btn) {
                    if (isGridEnabled()) {
                        grid.setCurrentCell(cell);
                    }
                }});
            int iconX = x + 23;
            iconX = addLevelIcon(iconX, y, level.boltName, BotsnBoltsGame.iconBolt);
            iconX = addLevelIcons(iconX, y, level.diskNames, BotsnBoltsGame.iconDisk);
            iconX = addLevelIcons(iconX, y, level.otherBossNames, BotsnBoltsGame.iconBoss);
            iconX = addLevelIcon(iconX, y, level.bossClassName, BotsnBoltsGame.iconBoss);
            return cell;
        }
        
        private final static int addLevelIcon(final int x, final int y, final String req, final Panmage iconIfMet) {
            if (req == null) {
                return x;
            }
            final Panmage icon = Profile.isRequirementMet(req) ? iconIfMet : BotsnBoltsGame.iconBlank;
            final Panctor actor = new Panctor();
            actor.setView(icon);
            actor.getPosition().set(x, y, BotsnBoltsGame.DEPTH_ENEMY_BACK);
            levelSelectLayer.addActor(actor);
            return x + 8;
        }
        
        private final static int addLevelIcons(int x, final int y, final List<String> reqs, final Panmage iconIfMet) {
            for (final String req : Coltil.unnull(reqs)) {
                x = addLevelIcon(x, y, req, iconIfMet);
            }
            return x;
        }
    }
    
    private final static boolean isGridEnabled() {
        return !isPauseMenuEnabled();
    }
    
    private final static void startLevel(final BotLevel level) {
        clearLevelButtons();
        Pangine.getEngine().getAudio().stop();
        BotsnBoltsGame.fxMenuClick.startSound();
        prepareLevel(level);
        startLevelIntroScreen(level);
    }
    
    protected final static void prepareLevel(final BotLevel level) {
        Boss.dropping = false;
        RoomLoader.levelVariables.clear();
        RoomLoader.startX = level.levelX;
        RoomLoader.startY = level.levelY;
        RoomLoader.levelVersion = level.version;
        RoomLoader.level = level;
        RoomLoader.visitedRooms.clear();
    }
    
    protected final static void startLevelIntroScreen(final BotLevel level) {
        Panscreen.set((level.startScreen == null) ? new LevelStartScreen() : level.startScreen.screen);
    }
    
    protected final static void startLevelGameplay() {
        Panscreen.set(new BotsnBoltsGame.BotsnBoltsScreen());
    }
    
    protected final static class Pupils extends Panctor {
        private final float x;
        private final float y;
        private float leftOff = 0;
        private float rightOff = 0;
        private float yOff = 0;
        
        protected Pupils(final Panlayer layer, final float x, final float y) {
            layer.addActor(this);
            this.x = x;
            this.y = y;
        }
        
        private final void lookAt(final LevelSelectCell cell) {
            final int i = cell.i - 2, j = cell.j - 1;
            yOff = (Math.abs(i) > 1) ? j : (j * 2);
            if (i < 0) {
                leftOff = (j == 0) ? -4 : -3;
                rightOff = -1;
            } else if (i > 0) {
                leftOff = 1;
                rightOff = (j == 0) ? 4 : 3;
            } else {
                leftOff = 0;
                rightOff = 0;
            }
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panlayer layer = getLayer();
            renderView(renderer, layer, x, y, leftOff, rightOff, yOff);
        }
        
        protected final static void renderView(final Panderer renderer, final Panlayer layer, final float x, final float y,
                                               final float leftOff, final float rightOff, final float yOff) {
            final Panmage img = BotsnBoltsGame.pupil;
            final float yp = y + 10 + yOff;
            renderer.render(layer, img, x + 12 + leftOff, yp, BotsnBoltsGame.DEPTH_HUD_OVERLAY);
            renderer.render(layer, img, x + 19 + rightOff, yp, BotsnBoltsGame.DEPTH_HUD_OVERLAY);
        }
    }
    
    protected final static class LevelSelectGrid extends Panctor {
        private static Panmage bg = null;
        private final LevelSelectCell[][] cells = new LevelSelectCell[LEVEL_ROWS][LEVEL_COLUMNS];
        private LevelSelectCell currentCell;
        private Pupils pupils = null;
        
        {
            for (int j = 0; j < LEVEL_ROWS; j++) {
                final LevelSelectCell[] row = cells[j];
                for (int i = 0; i < LEVEL_COLUMNS; i++) {
                    row[i] = new LevelSelectCell(i, j);
                }
            }
            currentCell = cells[LEVEL_DEFAULT_ROW][LEVEL_DEFAULT_COLUMN];
            final ControlScheme ctrl = BotsnBoltsGame.pc.ctrl;
            register(ctrl.getUp(), new GridStartListener() {
                @Override public final void onGridStart() {
                    moveCurrentCell(0, 1);
                }});
            register(ctrl.getDown(), new GridStartListener() {
                @Override public final void onGridStart() {
                    moveCurrentCell(0, -1);
                }});
            register(ctrl.getRight(), new GridStartListener() {
                @Override public final void onGridStart() {
                    moveCurrentCell(1, 0);
                }});
            register(ctrl.getLeft(), new GridStartListener() {
                @Override public final void onGridStart() {
                    moveCurrentCell(-1, 0);
                }});
            final ActionEndListener selectListener = new GridEndListener() {
                @Override public final void onGridEnd() {
                    final BotLevel level = currentCell.level;
                    if (level != null) {
                        startLevel(level);
                    }
                }};
            register(ctrl.get1(), selectListener);
            register(ctrl.get2(), selectListener);
            register(ctrl.getSubmit(), selectListener);
        }
        
        private final void moveCurrentCell(final int xAmt, final int yAmt) {
            final LevelSelectCell newCell = Coltil.get(Coltil.get(cells, currentCell.j + yAmt), currentCell.i + xAmt);
            if ((newCell != null) && newCell.isSelectable()) {
                setCurrentCell(newCell);
            }
        }
        
        protected final void setCurrentCell(final LevelSelectCell currentCell) {
            setCurrentCell(currentCell, true);
        }
        
        protected final void setCurrentCell(final LevelSelectCell currentCell, final boolean soundNeeded) {
            if (this.currentCell != currentCell) {
                this.currentCell = currentCell;
                if (pupils != null) {
                    pupils.lookAt(currentCell);
                }
                if (soundNeeded) {
                    BotsnBoltsGame.fxMenuHover.startSound();
                }
            }
        }
        
        protected final static Panmage getSelectBg() {
            if (bg == null) {
                bg = Pangine.getEngine().createImage("select.bg", BotsnBoltsGame.RES + "menu/SelectBg.png");
            }
            return bg;
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panlayer layer = getLayer();
            final Panmage box = BotsnBoltsGame.getBox();
            final boolean gridEnabled = isGridEnabled();
            for (final LevelSelectCell[] row : cells) {
                int y = 0;
                for (final LevelSelectCell cell : row) {
                    if (!cell.isSelectable()) {
                        continue;
                    }
                    renderBox(renderer, layer, cell.x, y = cell.y, BotsnBoltsGame.DEPTH_BG, (gridEnabled && (cell == currentCell)) ? BotsnBoltsGame.pc.pi.highlightBox : box);
                }
                renderBg(renderer, layer, y + 8);
            }
        }
        
        protected final static void renderBg(final Panderer renderer, final Panlayer layer, final int y) {
            renderer.render(layer, getSelectBg(), 0, y, BotsnBoltsGame.DEPTH_BEHIND, 0, 0, BotsnBoltsGame.GAME_W, 32, 0, false, false);
        }
        
        private final static void renderBox(final Panderer renderer, final Panlayer layer, final int x, final int y, final int z, final Panmage img) {
            renderBox(renderer, layer, x, y, z, img, 2, 2);
        }
        
        protected final static void renderBox(final Panderer renderer, final Panlayer layer, final int x, final int y, final int z, final Panmage img, final int w, final int h) {
            final int x8 = x + 8, y8 = y + 8, w16 = 16 * w, h16 = 16 * h, x40 = x + w16 + 8, y40 = y + h16 + 8;
            renderer.render(layer, img, x, y, z, 0, 24, 8, 8, 0, false, false);
            renderer.render(layer, img, x40, y, z, 24, 24, 8, 8, 0, false, false);
            renderer.render(layer, img, x, y40, z, 0, 0, 8, 8, 0, false, false);
            renderer.render(layer, img, x40, y40, z, 24, 0, 8, 8, 0, false, false);
            for (int i = 0; i < w; i++) {
                final int i16 = i * 16;
                renderer.render(layer, img, x8 + i16, y, z, 8, 24, 16, 8, 0, false, false);
                renderer.render(layer, img, x8 + i16, y40, z, 8, 0, 16, 8, 0, false, false);
            }
            for (int i = 0; i < h; i++) {
                final int i16 = i * 16;
                renderer.render(layer, img, x, y8 + i16, z, 0, 8, 8, 16, 0, false, false);
                renderer.render(layer, img, x40, y8 + i16, z, 24, 8, 8, 16, 0, false, false);
            }
            renderer.render(layer, BotsnBoltsGame.black, x + 8, y + 8, z, 0, 0, w16, h16);
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
            x = LEVEL_SELECT_X + (i * LEVEL_W);
            y = LEVEL_SELECT_Y + (j * LEVEL_H);
        }
        
        private final boolean isSelectable() {
            return (((level != null) && level.isAllowed()) || ((j == LEVEL_DEFAULT_ROW) && (i == LEVEL_DEFAULT_COLUMN)));
        }
    }
    
    private abstract static class GridStartListener implements ActionStartListener {
        @Override
        public final void onActionStart(final ActionStartEvent event) {
            if (isGridEnabled()) {
                onGridStart();
            }
        }
        
        public abstract void onGridStart();
    }
    
    private abstract static class GridEndListener implements ActionEndListener {
        @Override
        public final void onActionEnd(final ActionEndEvent event) {
            if (isGridEnabled()) {
                onGridEnd();
            }
        }
        
        public abstract void onGridEnd();
    }
    
    protected final static void goOptions() {
        Panscreen.set(new OptionsScreen());
    }
    
    private final static int optionsX = 48;
    private static int optionsY = 0;
    
    protected final static class OptionsScreen extends Panscreen {
        @Override
        protected final void load() throws Exception {
            final Panroom room = Pangame.getGame().getCurrentRoom();
            BotsnBoltsGame.room = room;
            final Pangine engine = Pangine.getEngine();
            engine.setBgColor(new FinPancolor(96));
            final Panctor bg = new LevelStartBg(24);
            room.addActor(bg);
            optionsY = 156;
            final Profile prf = BotsnBoltsGame.pc.prf;
            addOption(bg, "Suggest next level", new OptionSetter() {
                @Override public final boolean set() {
                    return (prf.levelSuggestions = !prf.levelSuggestions); }});
            addOption(bg, "Upgrade bolt usage hints", new OptionSetter() {
                @Override public final boolean set() {
                    return (prf.boltUsageHints = !prf.boltUsageHints); }});
            addOption(bg, "Frequent checkpoints", new OptionSetter() {
                @Override public final boolean set() {
                    return (prf.frequentCheckpoints = !prf.frequentCheckpoints); }});
            addOption(bg, "Adaptive health batteries", new OptionSetter() {
                @Override public final boolean set() {
                    return (prf.adaptiveBatteries = !prf.adaptiveBatteries); }});
            addOption(bg, "Endure spikes", new OptionSetter() {
                @Override public final boolean set() {
                    return (prf.endureSpikes = !prf.endureSpikes); }});
            addOption(bg, "Infinite upgrade usage", new OptionSetter() {
                @Override public final boolean set() {
                    return (prf.infiniteStamina = !prf.infiniteStamina); }});
            addTopRightButton(room, "LevelSelect", RoomLoader.isFirstLevelFinished() ? imgLevelSelect : imgPlay, bg, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    exitOptions();
                }});
            bg.register(engine.getInteraction().BACK, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    exitOptions();
                }});
            Player.registerCapture(bg);
            addCursor(room);
        }
    }
    
    private final static void exitOptions() {
        BotsnBoltsGame.pc.prf.saveProfile(); // Save in case goLevelSelect will actually start first level instead of level select (checks isSame anyway, so won't save twice)
        goLevelSelect();
    }
    
    private final static void addOption(final Panctor src, final String label, final OptionSetter setter) {
        final Pantext text = new Pantext(Pantil.vmid(), BotsnBoltsGame.font, label);
        text.getPosition().set(optionsX, optionsY);
        BotsnBoltsGame.addActor(text);
        setter.set(); // Flip current value just to flip it back to see what it was below
        final boolean on = setter.set();
        final Panmage img = on ? imgOn : imgOff;
        final TouchButton btn;
        btn = addButton(BotsnBoltsGame.room, label, BotsnBoltsGame.GAME_W - 16 - optionsX, optionsY - 4, true, true, null, img, img, true, null, false, 16);
        src.register(btn, new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) {
                final Panmage imgNew = setter.set() ? imgOn : imgOff;
                btn.setImageActive(imgNew);
                btn.setImageInactive(imgNew);
                BotsnBoltsGame.fxMenuClick.startSound();
            }});
        optionsY -= 24;
    }
    
    private static interface OptionSetter {
        public boolean set();
    }
    
    protected abstract static class StartScreen extends Panscreen {
        @Override
        protected final void load() throws Exception {
            final Panctor actor = draw();
            Pangine.getEngine().addTimer(actor, 16, new TimerListener() {
                @Override public final void onTimer(final TimerEvent event) {
                    startMusic();
                }});
        }
        
        protected abstract Panctor draw() throws Exception;
        
        protected abstract void startMusic();
    }
    
    protected final static class LevelStartScreen extends StartScreen {
        private final static int floorY = 4;
        private LevelStartBg bg = null;
        
        @Override
        protected final Panctor draw() throws Exception {
            final Panroom room = Pangame.getGame().getCurrentRoom();
            BotsnBoltsGame.room = room;
            final BotLevel level = RoomLoader.level;
            bg = new LevelStartBg();
            room.addActor(bg);
            room.addActor(newTileMap());
            final int bossY = floorY + 1;
            final Player p = PlayerContext.getPlayer(BotsnBoltsGame.pc);
            if (p != null) {
                p.getPosition().setX(0);
            }
            final Boss boss = RoomLoader.newBoss(16, bossY, level.bossClassName);
            room.addActor(boss);
            final int textX = 8 * BotsnBoltsGame.DIM;
            boss.tauntFinishHandler = new Runnable() {
                @Override public final void run() {
                    final TextTyper nameTyper = new TextTyper(BotsnBoltsGame.font, level.bossDisplayName).setTime(8).setTimer(0);
                    nameTyper.getPosition().set(textX, bossY * BotsnBoltsGame.DIM, BotsnBoltsGame.DEPTH_HUD);
                    nameTyper.centerX();
                    room.addActor(nameTyper);
                }};
            final Boolean portraitMirror = level.portraitMirror;
            Story.portrait(boss.getPortrait(), textX - 24, 96, (portraitMirror != null) && portraitMirror.booleanValue());
            return bg;
        }
        
        private final static TileMap newTileMap() {
            final TileMap tm = BotsnBoltsGame.BotsnBoltsScreen.newTileMap();
            tm.fillBackground(null, 0, floorY, BotsnBoltsGame.GAME_COLUMNS, 1, true);
            BotsnBoltsGame.tm = tm;
            tm.getPosition().addY(-8);
            return tm;
        }
        
        @Override
        protected final void startMusic() {
            BotsnBoltsGame.musicLevelStart.startSound();
            startLevelTimer(bg);
        }
        
        private final static void startLevelTimer(final Panctor actor) {
            Pangine.getEngine().addTimer(actor, 300, new TimerListener() {
                @Override public final void onTimer(final TimerEvent event) {
                    startLevelGameplay();
                }});
        }
    }
    
    protected final static class LevelStartBg extends Panctor {
        private final int marginY;
        
        protected LevelStartBg() {
            this(40);
        }
        
        protected LevelStartBg(final int marginY) {
            this.marginY = marginY;
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panlayer layer = getLayer();
            LevelSelectGrid.renderBg(renderer, layer, marginY);
            LevelSelectGrid.renderBg(renderer, layer, BotsnBoltsGame.GAME_H - 32 - marginY);
        }
    }
    
    protected final static class FortressStartScreen extends StartScreen {
        private static Panmage imgFortress = null;
        private static Panmage imgTiles = null;
        
        @Override
        protected final Panctor draw() throws Exception {
            final Panroom room = Pangame.getGame().getCurrentRoom();
            final Panctor fortress = new Panctor();
            final Pangine engine = Pangine.getEngine();
            engine.setBgColor(Pancolor.BLACK);
            fortress.setView(getImageFortress());
            fortress.getPosition().set(64, 0, 2);
            room.addActor(fortress);
            room.addActor(newTileMap());
            engine.addTimer(fortress, 90, new TimerListener() {
                @Override public final void onTimer(final TimerEvent event) {
                    room.addActor(new FortressMap(RoomLoader.StartScreenDefinition.Fortress));
                }});
            LevelStartScreen.startLevelTimer(fortress);
            return fortress;
        }
        
        @Override
        protected final void startMusic() {
            BotsnBoltsGame.musicFortressStart.startSound();
        }
        
        private final static TileMap newTileMap() {
            final TileMap tm = BotsnBoltsGame.BotsnBoltsScreen.newTileMap();
            final TileMapImage[][] imgMap = tm.splitImageMap(getImageTiles());
            tm.setBackground(3, 0, imgMap[3][1]);
            tm.setBackground(3, 1, imgMap[2][1]);
            tm.setBackground(0, 0, imgMap[3][0]);
            tm.setBackground(1, 0, imgMap[2][0]);
            tm.setBackground(2, 0, imgMap[3][0]);
            tm.setBackground(20, 0, imgMap[2][0]);
            tm.setBackground(21, 0, imgMap[3][0]);
            tm.setBackground(22, 0, imgMap[3][0]);
            tm.setBackground(23, 0, imgMap[2][0]);
            addRow(tm, 0, 2, 19, 23, 1, imgMap[3][2]);
            addRow(tm, 0, 4, 19, 23, 2, imgMap[2][2]);
            addRow(tm, 0, 4, 18, 23, 3, imgMap[3][3]);
            addRow(tm, 0, 4, 17, 23, 4, imgMap[2][3]);
            tm.setBackground(4, 1, imgMap[3][2]);
            tm.setBackground(0, 8, imgMap[1][3]);
            addTiles(tm, 0, 9, imgMap[1][0], imgMap[1][2], imgMap[1][3]);
            addTiles(tm, 0, 10, imgMap[1][1], imgMap[1][0], imgMap[1][0], imgMap[1][2]);
            addTiles(tm, 0, 11, imgMap[0][3], imgMap[1][0], imgMap[1][0], imgMap[1][1]);
            addTiles(tm, 0, 12, imgMap[0][1], imgMap[0][3], imgMap[1][0], imgMap[1][0]);
            addTiles(tm, 0, 13, imgMap[0][0], imgMap[0][1], imgMap[0][2], imgMap[0][2]);
            final AdjustedTileMapImage m01 = new AdjustedTileMapImage(imgMap[0][1], 0, true, false);
            final AdjustedTileMapImage m03 = new AdjustedTileMapImage(imgMap[0][3], 0, true, false);
            final AdjustedTileMapImage m11 = new AdjustedTileMapImage(imgMap[1][1], 0, true, false);
            final AdjustedTileMapImage m13 = new AdjustedTileMapImage(imgMap[1][3], 0, true, false);
            tm.setBackground(23, 9, m13);
            addTiles(tm, 20, 10, imgMap[1][3], null, m13, m11);
            addTiles(tm, 20, 11, imgMap[1][0], imgMap[1][2], imgMap[1][0], imgMap[1][0]);
            addTiles(tm, 20, 12, imgMap[1][0], m03, imgMap[0][2], imgMap[0][2]);
            addTiles(tm, 20, 13, imgMap[0][2], imgMap[0][0], m01, imgMap[0][0]);
            return tm;
        }
        
        private final static void addRow(final TileMap tm, final int xMin1, final int xMax1, final int xMin2, final int xMax2, final int y, final TileMapImage img) {
            addRow(tm, xMin1, xMax1, y, img);
            addRow(tm, xMin2, xMax2, y, img);
        }
        
        private final static void addRow(final TileMap tm, final int xMin, final int xMax, final int y, final TileMapImage img) {
            for (int x = xMin; x <= xMax; x++) {
                if (img != null) {
                    tm.setBackground(x, y, img);
                }
            }
        }
        
        private final static void addTiles(final TileMap tm, int x, final int y, final TileMapImage... imgs) {
            for (final TileMapImage img : imgs) {
                tm.setBackground(x, y, img);
                x++;
            }
        }
        
        private final static Panmage getImageFortress() {
            return (imgFortress = getStoryImage(imgFortress, "Fortress"));
        }
        
        private final static Panmage getImageTiles() {
            return (imgTiles = getStoryImage(imgTiles, "FortressTiles"));
        }
    }
    
    private final static class FortressMap extends Panctor {
        private final static FortressLineDefinition[] lineDefs = {
                new FortressLineDefinition(8, 0, 3, true), new FortressLineDefinition(8, 0, 0, false),
                new FortressLineDefinition(0, 0, 0, false), new FortressLineDefinition(0, 8, 0, false),
                new FortressLineDefinition(8, 8, 0, false), new FortressLineDefinition(0, 8, 3, true) };
        private static Panmage finalIcon = null;
        private static Panmage mapMarker = null;
        private static Panmage mapLine = null;
        private final StartScreenDefinition def;
        private int currLineSize;
        
        private FortressMap(final StartScreenDefinition def) {
            this.def = def;
            currLineSize = RoomLoader.level.startLineSize;
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            renderIcons(renderer);
            renderMarkers(renderer);
            renderLines(renderer);
        }
        
        private final void renderIcons(final Panderer renderer) {
            final Panlayer layer = getLayer();
            getFinalIcon();
            final int[] a = def.icons;
            final int size = a.length;
            for (int i = 0; i < size; i += 2) {
                renderer.render(layer, finalIcon, (a[i] * 8) - 4, (a[i + 1] * 8) - 4, 6);
            }
        }
        
        private final void renderMarkers(final Panderer renderer) {
            final Panlayer layer = getLayer();
            getMapMarker();
            final int[] a = def.markers;
            final int size = a.length;
            for (int i = 0; i < size; i += 2) {
                renderer.render(layer, mapMarker, a[i] * 8, a[i + 1] * 8, 4);
            }
        }
        
        private final void renderLines(final Panderer renderer) {
            final Panlayer layer = getLayer();
            getMapLine();
            final int[] a = def.lines;
            final int fields = 3;
            final int size = currLineSize * fields;
            for (int i = 0; i < size; i += fields) {
                final FortressLineDefinition def = lineDefs[a[i + 2]];
                renderer.render(layer, mapLine, a[i] * 8, a[i + 1] * 8, 4, def.x, def.y, 8, 8, def.rot, def.mirror, false);
            }
            int endLineSize = RoomLoader.level.endLineSize;
            if (endLineSize <= 0) {
                endLineSize = (a.length / fields);
            }
            if (currLineSize < endLineSize) {
                currLineSize++;
            }
        }
        
        private final static Panmage getFinalIcon() {
            return (finalIcon = getStoryImage(finalIcon, "FinalIcon"));
        }
        
        private final static Panmage getMapMarker() {
            return (mapMarker = getStoryImage(mapMarker, "MapMarker"));
        }
        
        private final static Panmage getMapLine() {
            return (mapLine = getStoryImage(mapLine, "MapLine"));
        }
    }
    
    private final static class FortressLineDefinition {
        private final int x;
        private final int y;
        private final int rot;
        private final boolean mirror;
        
        private FortressLineDefinition(final int x, final int y, final int rot, final boolean mirror) {
            this.x = x;
            this.y = y;
            this.rot = rot;
            this.mirror = mirror;
        }
    }
    
    protected final static Panmage getStoryImage(final Panmage img, final String name) {
        return (img == null) ? Pangine.getEngine().createImage(name, BotsnBoltsGame.RES + "story/" + name + ".png") : img;
    }
}
