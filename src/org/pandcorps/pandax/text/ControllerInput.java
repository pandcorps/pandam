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

import org.pandcorps.pandam.Pangine;
import org.pandcorps.pandam.Panput;
import org.pandcorps.pandam.Panteraction;
import org.pandcorps.pandam.event.action.ActionStartEvent;
import org.pandcorps.pandam.event.action.ActionStartListener;

public final class ControllerInput extends Input {
	private final static char[] UPPER
		= {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
	
	private final char[] chars = UPPER;
	private int index;
	private int value;
	
	public ControllerInput(final Font font, final InputSubmitListener listener) {
		super(font, listener);
	}
	
	public ControllerInput(final MultiFont fonts, final InputSubmitListener listener) {
		super(fonts, listener);
	}
	
	private int getValue(final char c) {
		final int size = chars.length;
		for (int i = 0; i < size; i++) {
			if (c == chars[i]) {
				return i;
			}
		}
		return size;
	}
	
	private final void initChr(final int ind) {
		initChr(ind, chars.length);
	}
	
	private final void initChr(final int ind, final int val) {
		index = ind;
    	value = val;
    	label.setCursor(0, index);
	}
	
	@Override
    protected final void focus() {
		initChr(buf.length());
		final Pangine engine = Pangine.getEngine();
        final Panteraction in = engine.getInteraction();
        final Panput sub = in.KEY_ENTER, adv = in.KEY_SPACE, bak = in.KEY_ESCAPE, up = in.KEY_UP, down = in.KEY_DOWN;
        Panput.inactivate(sub, adv, bak, up, down);
        final ActionStartListener upListener = new ActionStartListener() {
            @Override
            public void onActionStart(final ActionStartEvent event) {
            	if (value == chars.length) {
            		value = 0;
            		buf.append(' ');
            	} else {
            		value++;
            	}
            	if (value != chars.length) {
            		buf.setCharAt(index, chars[value]);
            	} else {
            		buf.setLength(index);
            	}
                //submit(changeListener);
            }};
        final ActionStartListener downListener = new ActionStartListener() {
            @Override
            public void onActionStart(final ActionStartEvent event) {
            	if (value == 0) {
            		value = chars.length;
            	} else {
            		if (value == chars.length) {
                		buf.append(' ');
                	}
            		value--;
            	}
            	if (value != chars.length) {
            		buf.setCharAt(index, chars[value]);
            	} else {
            		buf.setLength(index);
            	}
                //submit(changeListener);
            }};
        final ActionStartListener advListener = new ActionStartListener() {
            @Override
            public void onActionStart(final ActionStartEvent event) {
            	if (value != chars.length && (max <= 0 || buf.length() < max)) {
            		initChr(index + 1);
            	}
            }};
        final ActionStartListener bakListener = new ActionStartListener() {
            @Override
            public void onActionStart(final ActionStartEvent event) {
            	if (index == 0) {
            		return;
            	} else if (value != chars.length) {
            		buf.setLength(buf.length() - 1);
            	}
            	initChr(index - 1, getValue(buf.charAt(buf.length() - 1)));
            }};
        final ActionStartListener subListener = new ActionStartListener() {
            @Override
            public void onActionStart(final ActionStartEvent event) {
            	close();
            	sub.inactivate();
            	submit();
            }};
        label.register(up, upListener);
        label.register(down, downListener);
        label.register(adv, advListener);
        label.register(bak, bakListener);
        label.register(sub, subListener);
	}
}
