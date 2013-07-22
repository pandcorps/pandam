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

import java.util.*;

import org.pandcorps.core.Pantil;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.action.*;

public abstract class Input extends TextItem {
    protected final StringBuffer buf;
    protected final InputSubmitListener listener;
    protected InputSubmitListener changeListener = null;
    protected int max = 0;
    
    public Input(final Font font, final InputSubmitListener listener) {
    	this(new Pantext(Pantil.vmid(), font, newText()), listener);
    }
    
    public Input(final MultiFont fonts, final InputSubmitListener listener) {
    	this(new Pantext(Pantil.vmid(), fonts, newText()), listener);
    }
    
    private final static List<? extends CharSequence> newText() {
    	return Collections.singletonList(new StringBuffer());
    }
    
    private Input(final Pantext text, final InputSubmitListener listener) {
        super(text);
        buf = (StringBuffer) label.text.get(0);
        home();
        this.listener = listener;
    }
    
    public final static class KeyInput extends Input {
    	public KeyInput(final Font font, final InputSubmitListener listener) {
    		super(font, listener);
    	}
    	
    @Override
    protected final void focus() {
        //TODO Some todo notes in message apply here
        //System.out.println("Input");
        final Pangine engine = Pangine.getEngine();
        final Panteraction interaction = engine.getInteraction();
        interaction.inactivateAll();
        final ActionStartListener startListener = new ActionStartListener() {
            @Override
            public void onActionStart(final ActionStartEvent event) {
                final Panput input = event.getInput();
                if (interaction.KEY_ENTER == input) {
                	close();
                    interaction.unregister(this);
                    interaction.inactivateAll();
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
                        change();
                    }
                } else if (interaction.KEY_DEL == input) {
                    if (label.cursorChar < buf.length()) {
                        buf.deleteCharAt(label.cursorChar);
                        change();
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
                            final int ind = label.cursorChar, size = buf.length();
                            if (interaction.isInsEnabled() && ind < size) {
                                buf.setCharAt(ind, ch);
                            } else if (max <= 0 || max > size) {
                                buf.insert(ind, ch);
                            } else {
                            	return;
                            }
                            change();
                            label.incCursor();
                        }
                    }
                }
            }
        };
        label.register(startListener);
    }
    }
    
    protected final void submit() {
    	submit(listener);
    }
    
    protected final void change() {
    	submit(changeListener);
    }
    
    protected final void submit(final InputSubmitListener listener) {
    	if (listener != null) {
        	listener.onSubmit(new InputSubmitEvent(buf));
        }
    }
    
    protected final void home() {
        label.setCursor(0, 0);
    }
    
    public void setChangeListener(final InputSubmitListener changeListener) {
    	this.changeListener = changeListener;
    }
    
    public void setMax(final int max) {
    	this.max = max;
    }
}
