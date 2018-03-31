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
import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.core.img.process.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.in.*;

public class Menu {
    private final static int DIM_BUTTON = 59;
    
    protected static Panmage imgCursor = null;
    protected static ButtonImages circleImages = null;
    protected static ButtonImages rightImages = null;
    protected static ButtonImages upImages = null;
    protected static ButtonImages downImages = null;
    
    protected static Cursor cursor = null;
    protected static TouchButton jump = null;
    protected static TouchButton attack = null;
    protected static TouchButton right = null;
    protected static TouchButton left = null;
    protected static TouchButton up = null;
    protected static TouchButton down = null;
    protected static TouchButton toggleAttack = null;
    protected static TouchButton toggleJump = null;
    
    protected final static void loadMenu() {
        loadCursor();
        loadGameplayButtons();
    }
    
    protected final static boolean isCursorNeeded() {
        return true;
    }
    
    protected final static boolean isScreenGameplayLayoutNeeded() {
        return true;
    }
    
    private final static void loadCursor() {
        if (!isCursorNeeded()) {
            return;
        }
        imgCursor = Pangine.getEngine().createImage(Pantil.vmid(), new FinPanple2(0, 15), null, null, BotsnBoltsGame.RES + "menu/Cursor.png");
    }
    
    private final static void loadGameplayButtons() {
        if (!isScreenGameplayLayoutNeeded()) {
            return;
        }
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
    }
    
    private final static TouchButton addToggleButton(final String name, final HudIcon hudIcon) {
        final Panlayer hud = BotsnBoltsGame.hud;
        hud.addActor(hudIcon);
        final Panple pos = hudIcon.getPosition();
        final TouchButton button = addButton(hud, name, Math.round(pos.getX()), Math.round(pos.getY()), true, false, null, null, false, null, false, 18);
        button.setLayer(hud);
        return button;
    }
    
    private final static TouchButton addButton(final Panlayer room, final String name, final int x, final int y,
            final boolean input, final boolean act, final Panput old, final ButtonImages images,
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
            actor.setView(images.full);
            button.setActor(actor, images.pressed);
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
}
