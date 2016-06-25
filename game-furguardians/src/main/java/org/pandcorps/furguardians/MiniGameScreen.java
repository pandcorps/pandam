/*
Copyright (c) 2009-2016, Andrew M. Martin
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
package org.pandcorps.furguardians;

import org.pandcorps.core.*;
import org.pandcorps.furguardians.Player.*;
import org.pandcorps.furguardians.Profile.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandax.in.*;

public abstract class MiniGameScreen extends Panscreen {
    
    protected final static PlayerContext getPlayerContext() {
        return Coltil.isEmpty(FurGuardiansGame.pcs) ? null : FurGuardiansGame.pcs.get(0);
    }
    
    protected final static Profile getProfile() {
        return PlayerContext.getProfile(getPlayerContext());
    }
    
    protected final static Statistics getStatistics() {
        return PlayerContext.getStatistics(getPlayerContext());
    }
    
    protected final static void addGems(final int n) {
        final PlayerContext pc = getPlayerContext();
        if (pc != null) {
            pc.addGems(n);
        }
    }
    
    protected final static void save() {
        final Profile prf = getProfile();
        if (prf != null) {
            prf.save();
        }
    }
    
    protected final static Panroom initMiniZoom(final int min) {
        final Pangine engine = Pangine.getEngine();
        engine.zoomToMinimum(min);
        engine.setBgColor(Menu.COLOR_BG);
        engine.getAudio().stopMusic();
        return FurGuardiansGame.createRoom(engine.getEffectiveWidth(), engine.getEffectiveHeight());
    }
    
    protected final static Cursor addCursor(final Panlayer room, final int z) {
        final Cursor cursor = FurGuardiansGame.addCursor(room);
        if (cursor != null) {
            cursor.getPosition().setZ(z);
        }
        return cursor;
    }
    
    protected final static boolean isTouchActive(final ButtonWrapper[][] grid) {
        for (final ButtonWrapper[] row : grid) {
            for (final ButtonWrapper cell : row) {
                if (cell.getButton().isActive()) {
                    return true;
                }
            }
        }
        return Pangine.getEngine().getInteraction().TOUCH.isActive();
    }
    
    protected final static boolean isAdjacentTo(final int row, final int col, final int otherRow, final int otherCol) {
        if (row == otherRow) {
            return Math.abs(col - otherCol) == 1;
        } else if (col == otherCol) {
            return Math.abs(row - otherRow) == 1;
        }
        return false;
    }
    
    private final ActionEndListener newMenuListener(final Panscreen nextScreen) {
        return new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) {
                goMiniMenu(nextScreen, Text.PLAY, true);
            }};
    }
    
    protected final void registerMiniInputs(final Panctor actor, final Panscreen nextScreen, final Panmage menuImg, final int menuX, final int menuY) {
        final Pangine engine = Pangine.getEngine();
        final Panteraction interaction = engine.getInteraction();
        Player.registerCaptureScreen(actor);
        actor.register(interaction.BACK, newMenuListener(nextScreen));
        if (engine.isMouseSupported()) {
            actor.register(interaction.KEY_ESCAPE, newMenuListener(nextScreen));
        }
        final TouchButton button = new TouchButton(interaction, actor.getLayer(), "mini.menu", menuX, menuY, 0, menuImg, null, true);
        engine.registerTouchButton(button);
        actor.register(button, newMenuListener(nextScreen));
    }
    
    protected final void goMiniMenu(final Panscreen nextScreen, final String nextLabel, final boolean quitNeeded) {
        FurGuardiansGame.setScreen(new MiniMenuScreen(nextScreen, nextLabel, quitNeeded, getExtraButton()));
    }
    
    //@OverrideMe
    protected MiniButton getExtraButton() {
        return null;
    }
    
    protected static interface ButtonWrapper {
        public TouchButton getButton();
    }
    
    protected final class MiniAwardScreen extends Panscreen {
        private final int award;
        private final Panscreen nextScreen;
        
        protected MiniAwardScreen(final int award, final Panscreen nextScreen) {
            this.award = award;
            this.nextScreen = nextScreen;
        }
        
        @Override
        protected final void load() {
            final PlayerContext pc = getPlayerContext();
            if (pc == null) {
                goNext();
                return;
            }
            final Panroom room = initMiniZoom(128);
            final Pangine engine = Pangine.getEngine();
            final int w = engine.getEffectiveWidth(), h = engine.getEffectiveHeight();
            final Gem gem = Menu.PlayerScreen.addHudGems(room, pc, (w - 72) / 2, (h - 16) / 2);
            addGems(award);
            save();
            gem.register(new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    goNext();
                }});
            gem.register(90, new TimerListener() {
                @Override public final void onTimer(final TimerEvent event) {
                    goNext();
                }});
        }
        
        private final void goNext() {
            goMiniMenu(nextScreen, Text.NEXT, false);
        }
    }
    
    protected final static class MiniMenuScreen extends Panscreen {
        private final Panscreen nextScreen;
        private final String nextLabel;
        private final boolean quitNeeded;
        private final MiniButton ext;
        private Panroom room = null;
        private int y = 0;
        
        protected MiniMenuScreen(final Panscreen nextScreen, final String nextLabel, final boolean quitNeeded, final MiniButton ext) {
            this.nextScreen = nextScreen;
            this.nextLabel = nextLabel;
            this.quitNeeded = quitNeeded;
            this.ext = ext;
        }
        
        @Override
        protected final void load() {
            room = initMiniZoom(128);
            addCursor(room, 20);
            final Pangine engine = Pangine.getEngine();
            int numButtons = 2;
            if (quitNeeded) {
                numButtons++;
            }
            if (ext != null) {
                numButtons++;
            }
            final int w = FurGuardiansGame.MENU_W;
            int x = (engine.getEffectiveWidth() - (w * numButtons)) / 2;
            y = (engine.getEffectiveHeight() - FurGuardiansGame.MENU_H) / 2;
            final TouchButton nextButton;
            nextButton = newButton("Next", x, FurGuardiansGame.menuRight, nextLabel, new Runnable() {
                @Override public final void run() {
                    goNext();
                }});
            x += w;
            newButton("Menu", x, FurGuardiansGame.menuOptions, Text.MENU, new Runnable() {
                @Override public final void run() {
                    goMenu();
                }});
            x += w;
            if (ext != null) {
                newButton("Extra", x, ext.img, ext.txt, new Runnable() {
                    @Override public final void run() {
                        ext.run.run();
                        goNext();
                    }});
                x += w;
            }
            if (quitNeeded) {
                newButton("Quit", x, FurGuardiansGame.menuOff, Text.QUIT, new Runnable() {
                    @Override public final void run() {
                        engine.exit();
                    }});
                x += w;
            }
            final Panctor actor = nextButton.getActor();
            final Panteraction interaction = engine.getInteraction();
            actor.register(interaction.BACK, newMenuListener());
            if (engine.isMouseSupported()) {
                actor.register(interaction.KEY_SPACE, newNextListener());
                actor.register(interaction.KEY_ENTER, newNextListener());
                actor.register(interaction.KEY_ESCAPE, newMenuListener());
            }
        }
        
        private final TouchButton newButton(final String name, final int x, final Panmage img, final String txt, final Runnable r) {
            return Menu.PlayerScreen.newFormButton(room, name, x, y, img, txt, r);
        }
        
        private final void goNext() {
            FurGuardiansGame.setScreen(nextScreen);
        }
        
        private final void goMenu() {
            FurGuardiansGame.goMiniGames(getPlayerContext(), !quitNeeded);
        }
        
        private final ActionEndListener newNextListener() {
            return new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    goNext();
                }};
        }
        
        private final ActionEndListener newMenuListener() {
            return new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    goMenu();
                }};
        }
    }
    
    protected final static class MiniButton {
        private final Panmage img;
        private final String txt;
        private final Runnable run;
        
        protected MiniButton(final Panmage img, final String txt, final Runnable run) {
            this.img = img;
            this.txt = txt;
            this.run = run;
        }
    }
}
