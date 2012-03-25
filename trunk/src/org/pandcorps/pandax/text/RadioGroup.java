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

import java.util.List;

import org.pandcorps.core.Pantil;
import org.pandcorps.pandam.Pangine;
import org.pandcorps.pandam.Panput;
import org.pandcorps.pandam.Panteraction;
import org.pandcorps.pandam.event.action.*;

public class RadioGroup extends TextItem {
    private final List<? extends CharSequence> options;
    private final RadioSubmitListener listener;
    private Panput submit = Pangine.getEngine().getInteraction().KEY_SPACE;
    private final ActionGroup actions = new ActionGroup();
    
    public RadioGroup(final Font font, final List<? extends CharSequence> options, final RadioSubmitListener listener) {
        super(new Pantext(Pantil.vmid(), font, options));
        if (!(font instanceof ByteFont)) {
        	setCharacter('-');
        }
        label.setRadioLine(0);
        this.options = options;
        this.listener = listener;
    }
    
    @Override
    protected final void enable() {
        //TODO Some todo notes in message apply here
        final Pangine engine = Pangine.getEngine();
        final Panteraction interaction = engine.getInteraction();
        final Panput up = interaction.KEY_UP, down = interaction.KEY_DOWN;
        Panput.inactivate(submit, up, down);
        final ActionStartListener upListener = new ActionStartListener() {
            @Override
            public void onActionStart(final ActionStartEvent event) {
                label.decRadioLine();
            }};
        final ActionStartListener downListener = new ActionStartListener() {
            @Override
            public void onActionStart(final ActionStartEvent event) {
                label.incRadioLine();
            }};
        final ActionStartListener submitListener = new ActionStartListener() {
            @Override
            public void onActionStart(final ActionStartEvent event) {
                layer.destroy();
                actions.unregister();
                // inactivate should only apply for the current press (and not at all if the key isn't currently pressed).
                // This disableed the next up/down press if they weren't currently pressed before adding the active check to inactivate
                Panput.inactivate(submit, up, down);
                listener.onSubmit(new RadioSubmitEvent(label.radioLine, options.get(label.radioLine)));
            }};
        actions.register(submit, submitListener);
        actions.register(up, upListener);
        actions.register(down, downListener);
    }
    
    public void setCharacter(final char c) {
    	label.charRadio = c;
    }
    
    public void setSubmit(final Panput input) {
    	submit = input;
    }
    
    @Override
    protected final void onDestroy() {
    	actions.unregister();
    }
}
