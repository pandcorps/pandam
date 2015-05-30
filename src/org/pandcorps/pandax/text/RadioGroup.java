/*
Copyright (c) 2009-2014, Andrew M. Martin
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

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panteraction.*;
import org.pandcorps.pandam.event.action.*;

public class RadioGroup extends TextItem {
    private final List<? extends CharSequence> options;
    private final RadioSubmitListener listener;
    private RadioSubmitListener changeListener = null;
    private Panput submit = Pangine.getEngine().getInteraction().KEY_SPACE;
    private Panput up = null;
    private Panput down = null;
    private Boolean reactOnEnd = null;
    
    public RadioGroup(final Font font, final List<? extends CharSequence> options, final RadioSubmitListener listener) {
    	this(new Pantext(Pantil.vmid(), font, options), options, listener);
    }
    
    public RadioGroup(final MultiFont fonts, final List<? extends CharSequence> options, final RadioSubmitListener listener) {
    	this(new Pantext(Pantil.vmid(), fonts, options), options, listener);
    }
    
    private RadioGroup(final Pantext text, final List<? extends CharSequence> options, final RadioSubmitListener listener) {
        super(text);
        if (!(text.getFont() instanceof ByteFont)) {
        	setCharacter('-');
        }
        label.setRadioLine(0);
        this.options = options;
        this.listener = listener;
    }
    
    @Override
    protected final void focus() {
        //TODO Some todo notes in message apply here
        final Pangine engine = Pangine.getEngine();
        if (ctrl == null) {
            final Panteraction interaction = engine.getInteraction();
            up = interaction.KEY_UP;
            down = interaction.KEY_DOWN;
        } else {
            submit = ctrl.get1();
            up = ctrl.getUp();
            down = ctrl.getDown();
        }
        Panput.inactivate(submit, up, down);
        final boolean end;
        if (reactOnEnd == null) {
        	end = up.getDevice() instanceof Touchscreen;
        } else {
        	end = reactOnEnd.booleanValue();
        }
        if (end) {
        	registerEnd();
        } else {
        	registerStart();
        }
    }
    
    private final void registerStart() {
        final ActionStartListener upListener = new ActionStartListener() {
            @Override
            public void onActionStart(final ActionStartEvent event) {
                label.decRadioLine();
                submit(changeListener);
            }};
        final ActionStartListener downListener = new ActionStartListener() {
            @Override
            public void onActionStart(final ActionStartEvent event) {
                label.incRadioLine();
                submit(changeListener);
            }};
        final ActionStartListener submitListener = new ActionStartListener() {
            @Override
            public void onActionStart(final ActionStartEvent event) {
            	submit();
            }};
        label.register(submit, submitListener);
        label.register(up, upListener);
        label.register(down, downListener);
    }
    
    private final void registerEnd() {
        final ActionEndListener upListener = new ActionEndListener() {
            @Override
            public void onActionEnd(final ActionEndEvent event) {
                label.decRadioLine();
                submit(changeListener);
            }};
        final ActionEndListener downListener = new ActionEndListener() {
            @Override
            public void onActionEnd(final ActionEndEvent event) {
                label.incRadioLine();
                submit(changeListener);
            }};
        final ActionEndListener submitListener = new ActionEndListener() {
            @Override
            public void onActionEnd(final ActionEndEvent event) {
            	submit();
            }};
        label.register(submit, submitListener);
        label.register(up, upListener);
        label.register(down, downListener);
    }
    
    public final void submit() {
    	close();
        // inactivate should only apply for the current press (and not at all if the key isn't currently pressed).
        // This disableed the next up/down press if they weren't currently pressed before adding the active check to inactivate
        Panput.inactivate(submit, up, down);
        // onSubmit might create a new TextItem with same parent (deactivating it); activate first so we don't undo onSubmit
        submit(listener);
    }
    
    private void submit(final RadioSubmitListener listener) {
    	if (listener != null) {
        	listener.onSubmit(new RadioSubmitEvent(this, label.radioLine, getSelected()));
        }
    }
    
    public void setCharacter(final char c) {
    	label.charRadio = c;
    }
    
    public void setSubmit(final Panput input) {
    	submit = input;
    }
    
    public void setChangeListener(final RadioSubmitListener changeListener) {
    	this.changeListener = changeListener;
    }
    
    public void setSelected(final int line) {
    	label.setRadioLine(line);
    }
    
    public void setSelected(final CharSequence s) {
        final int line = label.lineOf(s);
        if (line < 0) {
            throw new IllegalArgumentException("Invalid radio option " + s);
        }
        label.setRadioLine(line);
    }
    
    public CharSequence getSelected() {
        return options.get(label.radioLine);
    }
    
    public void setReactOnEnd(final boolean reactOnEnd) {
    	this.reactOnEnd = Boolean.valueOf(reactOnEnd);
    }
}
