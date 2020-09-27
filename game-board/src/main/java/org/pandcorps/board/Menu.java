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

import org.pandcorps.board.BoardGame.*;
import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.text.*;

public class Menu {
    private static final int buttonLeft = 4;
    private static final int textLeft = 24;
    private static int profileY = 0;
    private static BoardGameProfile profile;
    private static Variable<Pancolor> color;
    
    protected final static void goMenu() {
        Panscreen.set(new MenuScreen());
    }
    
    protected final static class MenuScreen extends Panscreen {
        @Override
        protected void load() throws Exception {
            BoardGame.initScreen(96);
            profileY = 0;
            for (int i = BoardGame.module.numPlayers - 1; i >= 0; i--) {
                addProfile(BoardGame.module.players[i].profile);
            }
            addDone(new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    BoardGame.goGame();
                }});
            addButton("Exit", getButtonRight(), getButtonTop(), BoardGame.imgExit, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    Pangine.getEngine().exit();
                }});
        }
    }
    
    protected final static void addDone(final ActionEndListener listener) {
        addButton("Done", getButtonRight(), 4, BoardGame.imgDone, listener);
    }
    
    protected final static int getButtonRight() {
        return Pangine.getEngine().getEffectiveWidth() - 20;
    }
    
    protected final static int getButtonTop() {
        return Pangine.getEngine().getEffectiveHeight() - 20;
    }
    
    protected final static void addProfile(final BoardGameProfile profile) {
        addPair(profileY, profile.name, BoardGame.imgEdit, new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) {
                Menu.profile = profile;
                goProfile();
            }});
        profileY += 24;
    }
    
    protected final static void addPair(final int y, final String name, final Panmage img, final ActionEndListener listener) {
        addText(textLeft, y + 8, name);
        addButton("Edit." + name, buttonLeft, y + 4, img, listener);
    }
    
    protected final static TouchButton addButton(final String name, final int x, final int y, final Panmage img, final ActionEndListener listener) {
        final Panroom room = Pangame.getGame().getCurrentRoom();
        final TouchButton button = new TouchButton(null, room, name, x, y, BoardGame.DEPTH_CELL, BoardGame.square, BoardGame.square, img, 0, 0, null, null, 0, 0, true);
        Pangine.getEngine().registerTouchButton(button);
        button.getActor().register(button, listener);
        return button;
    }
    
    protected final static Pantext addText(final int x, final int y, final String value) {
        final Pantext text = new Pantext(Pantil.vmid(), BoardGame.font, value);
        text.getPosition().set(x, y, BoardGame.DEPTH_CELL);
        Pangame.getGame().getCurrentRoom().addActor(text);
        return text;
    }
    
    protected final static void goProfile() {
        Panscreen.set(new ProfileScreen());
    }
    
    protected final static class ProfileScreen extends Panscreen {
        @Override
        protected void load() throws Exception {
            BoardGame.initScreen(96);
            final int h = Pangine.getEngine().getEffectiveHeight();
            addText(textLeft, h - 16, profile.name);
            addColor(h - 48, "Primary", new Variable<Pancolor>() {
                @Override public final Pancolor get() { return profile.color1; }
                @Override public final void set(final Pancolor t) { profile.color1 = t; }});
            addColor(h - 80, "Alternate", new Variable<Pancolor>() {
                @Override public final Pancolor get() { return profile.color2; }
                @Override public final void set(final Pancolor t) { profile.color2 = t; }});
            addDone(new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    goMenu();
                }});
        }
    }
    
    protected final static void addColor(final int y, final String name, final Variable<Pancolor> color) {
        addPair(y, name, getImage(color.get(), BoardGame.imgEdit), new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) {
                Menu.color = color;
                Panscreen.set(new ColorScreen());
            }});
    }
    
    protected final static Panmage getImage(final Pancolor color, final Panmage def) {
        return (color == null) ? def : new RecolorPanmage(Pantil.vmid(), BoardGame.circle, color);
    }
    
    protected final static class ColorScreen extends Panscreen {
        @Override
        protected void load() throws Exception {
            BoardGame.initScreen(96);
            final Pangine engine = Pangine.getEngine();
            final int w = engine.getEffectiveWidth(), h = engine.getEffectiveHeight();
            final int x1 = w / 6, x2 = w * 2 / 6, x3 = w * 3 / 6, x4 = w * 4 / 6, x5 = w * 5 / 6;
            final int y2 = h / 3 - 8, y1 = h * 2 / 3;
            addColorOption(x1, y1, Pancolor.WHITE);
            addColorOption(x1, y2, BoardGame.BLACK);
            addColorOption(x2, y1, Pancolor.RED);
            addColorOption(x2, y2, BoardGame.ORANGE);
            addColorOption(x3, y1, Pancolor.YELLOW);
            addColorOption(x3, y2, Pancolor.GREEN);
            addColorOption(x4, y1, Pancolor.CYAN);
            addColorOption(x4, y2, Pancolor.BLUE);
            addColorOption(x5, y1, Pancolor.MAGENTA);
            addColorOption(x5, y2, null);
        }
    }
    
    protected final static void addColorOption(final int x, final int y, final Pancolor option) {
        addButton(Chartil.toString(option), x - 8, y - 8, getImage(option, null), new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) {
                color.set(option);
                profile.save();
                goProfile();
            }});
    }
}
