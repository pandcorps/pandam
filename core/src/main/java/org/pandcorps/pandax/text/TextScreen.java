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
package org.pandcorps.pandax.text;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandax.visual.*;

public abstract class TextScreen extends TempScreen {
    protected final TextScreenSequence sequence;
    protected final Pantext text;
    
    public TextScreen(final Pantext text) {
        this(null, text);
    }
    
    public TextScreen(final TextScreenSequence sequence, final Pantext text) {
        this.sequence = sequence;
        this.text = text;
        if (text.getLinesPerPage() <= 0) {
            // scrollPage is a no-op with 0 signs per page, breaks any-key advancing through text
            text.setLinesPerPage(1);
        }
        // Can't add to room in constructor, Panscreen.set will clear it; must do in init
        //Pangame.getGame().getCurrentRoom().addActor(text);
    }
    
    @Override
    protected final void init() throws Exception {
        Pangame.getGame().getCurrentRoom().addActor(text);
        final ActionStartListener anyKey;
        final Panput esc = Pangine.getEngine().getInteraction().KEY_ESCAPE;
        anyKey = new ActionStartListener() { @Override public final void onActionStart(final ActionStartEvent event) {
            if (esc.equals(event.getInput())) {
                cancel();
                return;
            }
            if (!text.scrollPage()) {
                finish(text);
            }
        }};
        text.register(anyKey);
    }
    
    protected final void cancel() {
        if (sequence == null) {
            finish(text);
            return;
        }
        // If we open a new Panscreen, this will be automatic, but do it in case finish does something else
        // Like TempScreen.finish
        text.unregisterListeners();
        sequence.cancel();
        text.destroy();
    }
}
