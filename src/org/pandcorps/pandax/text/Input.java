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

import java.util.Collections;

import org.pandcorps.core.Pantil;
import org.pandcorps.pandam.Pangine;
import org.pandcorps.pandam.Panput;
import org.pandcorps.pandam.Panteraction;
import org.pandcorps.pandam.event.action.ActionStartEvent;
import org.pandcorps.pandam.event.action.ActionStartListener;

public class Input extends TextItem {
    private final StringBuffer buf;
    private final InputSubmitListener listener;
    
    public Input(final Font font, final InputSubmitListener listener) {
        super(new Pantext(Pantil.vmid(), font, Collections.singletonList(new StringBuffer())));
        buf = (StringBuffer) label.text.get(0);
        this.listener = listener;
    }
    
    @Override
    protected final void enable() {
        //TODO Some todo notes in message apply here
        //System.out.println("Input");
        final Pangine engine = Pangine.getEngine();
        final Panteraction interaction = engine.getInteraction();
        interaction.inactivateAll();
        home();
        final ActionStartListener startListener = new ActionStartListener() {
            @Override
            public void onActionStart(final ActionStartEvent event) {
                final Panput input = event.getInput();
                if (interaction.KEY_ENTER == input) {
                    submit();
                } else if (interaction.KEY_BACKSPACE == input) {
                    //TODO hold shift and move cursor to create/modify selection
                    //TODO render selection
                    //TODO Ctrl-C/X/V - copy/cut/paste
                    //TODO Ctrl-Z - undo
                    //TODO Shift/Ctrl-Ins
                    //buf.deleteCharAt(buf.length() - 1);
                    if (label.cursorChar > 0) {
                        label.decCursor();
                        buf.deleteCharAt(label.cursorChar);
                    }
                } else if (interaction.KEY_DEL == input) {
                    if (label.cursorChar < buf.length()) {
                        buf.deleteCharAt(label.cursorChar);
                    }
                } else if (interaction.KEY_LEFT == input) {
                    label.decCursor();
                } else if (interaction.KEY_RIGHT == input) {
                    label.incCursor();
                } else if (interaction.KEY_HOME == input) {
                    home();
                } else if (interaction.KEY_END == input) {
                    label.setCursor(0, buf.length());
                } else if (!(interaction.isCtrlActive() || interaction.isAltActive())) {
                    final Character c = event.getCharacter();
                    if (c != null) {
                        final char ch = c.charValue();
                        //Character.getType(ch)
                        if (!Character.isISOControl(ch)) {
                            //buf.append(ch);
                            final int ind = label.cursorChar;
                            if (interaction.isInsEnabled() && ind < buf.length()) {
                                buf.setCharAt(ind, ch);
                            } else {
                                buf.insert(ind, ch);
                            }
                            label.incCursor();
                        }
                    }
                }
            }
            
            private final void submit() {
                layer.destroy();
                interaction.unregister(this);
                interaction.inactivateAll();
                listener.onSubmit(new InputSubmitEvent(buf));
            }
        };
        label.register(startListener);
    }
    
    private final void home() {
        label.setCursor(0, 0);
    }
}
