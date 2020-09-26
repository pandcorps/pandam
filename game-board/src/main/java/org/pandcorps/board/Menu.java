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
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandax.text.*;

public class Menu {
    private static int profileY = 0;
    
    protected final static class MenuScreen extends Panscreen {
        @Override
        protected void load() throws Exception {
            BoardGame.initScreen(96);
            profileY = 0;
            for (int i = BoardGame.module.numPlayers - 1; i >= 0; i--) {
                addProfile(BoardGame.module.players[i].profile);
            }
            addButton("Done", Pangine.getEngine().getEffectiveWidth() - 20, 4, BoardGame.imgDone, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    BoardGame.goGame();
                }});
        }
    }
    
    protected final static void addProfile(final BoardGameProfile profile) {
        addText(24, profileY + 8, profile.name);
        addButton("Edit." + profile.name, 4, profileY + 4, BoardGame.imgEdit, new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) {
                System.out.println("Edit");
            }});
        profileY += 24;
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
    
    protected final static class ProfileScreen extends Panscreen {
        @Override
        protected void load() throws Exception {
            BoardGame.initScreen(96);
            //addText(64, profileY, profile.name);
            //profile.color1;
            //profile.color2;
        }
    }
}
