/*
Copyright (c) 2009-2011, Andrew M. Martin
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

import org.pandcorps.core.Pantil;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.action.*;

// Conversation? Dialogue?
public class Message extends TextItem {
    private final MessageCloseListener listener;
    
    public Message(final Font font, final String text) {
        this(font, text, null);
    }
    
    public Message(final Font font, final String text, final MessageCloseListener listener) {
        this(new Pantext(Pantil.vmid(), font, text), listener);
    }
    
    public Message(final MultiFont fonts, final String text, final MessageCloseListener listener) {
        this(new Pantext(Pantil.vmid(), fonts, text), listener);
    }
    
    public Message(final Pantext text, final MessageCloseListener listener) {
        super(text);
        this.listener = listener;
        label.setLinesPerPage(Math.min(2, label.getTotalLines()));
    }
    
    @Override
    protected final void focus() {
        /*
        TODO
        Add appropriate setters instead of a fixed argument list.
        Class probably isn't a Panctor.
        It might create multiple Panctors internally.
        It would need a method like addToLayer(Panlayer).
        Or maybe just enable() which would create its own layer.
        */
        final Pangine engine = Pangine.getEngine();
        final Panteraction interaction = engine.getInteraction();
        final Panput submit = interaction.KEY_SPACE;
        submit.inactivate();
        //label.setRadioLine(1);
        label.register(submit, new ActionStartListener() {
            @Override
            public void onActionStart(final ActionStartEvent event) {
                if (label.scrollPage()) {
                    return;
                }
                //label.destroy();
                //layer.detach();
                close();
                if (form == null) {
                	interaction.unregister(this);
                }
                submit.inactivate();
                if (listener != null) {
                    listener.onClose(MessageCloseEvent.getInstance());
                }
            }});
    }
}
