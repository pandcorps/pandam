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

import org.pandcorps.pandam.*;
import org.pandcorps.pandax.text.*;

public class Story {
    protected final static void dialogue(final Panmage portrait, final boolean portraitLeft, final CharSequence msg) {
        final DialogueBox box = new DialogueBox(portrait, portraitLeft);
        BotsnBoltsGame.addActor(box);
        final TextTyper typer = new TextTyper(BotsnBoltsGame.font, msg);
        typer.setLinesPerPage(6);
        typer.setGapY(2);
        typer.getPosition().set(box.xText + 12, 197, BotsnBoltsGame.DEPTH_HUD_TEXT);
        BotsnBoltsGame.addActor(typer);
    }
    
    protected final static class DialogueBox extends Panctor {
        private final Panmage portrait;
        private final boolean portraitLeft;
        private final int xText;
        private final int xPortrait;
        
        protected DialogueBox(final Panmage portrait, final boolean portraitLeft) {
            this.portrait = portrait;
            this.portraitLeft = portraitLeft;
            if (portraitLeft) {
                xText = 88;
                xPortrait = 40;
            } else {
                xText = 40;
                xPortrait = 296;
            }
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panlayer layer = getLayer();
            final Panmage box = BotsnBoltsGame.getBox();
            Menu.LevelSelectGrid.renderBox(renderer, layer, xText, 136, BotsnBoltsGame.DEPTH_HUD, box, 15, 4);
            Menu.LevelSelectGrid.renderBox(renderer, layer, xPortrait, 168, BotsnBoltsGame.DEPTH_HUD, box, 2, 2);
            renderer.render(layer, portrait, xPortrait + 8, 176, BotsnBoltsGame.DEPTH_HUD_TEXT, 0, 0, 32, 32, 0, !portraitLeft, false);
            renderer.render(layer, BotsnBoltsGame.black, xText + 8, 144, BotsnBoltsGame.DEPTH_HUD, 0, 0, 240, 64);
        }
    }
}
