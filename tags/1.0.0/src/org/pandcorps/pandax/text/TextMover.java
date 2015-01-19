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
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.visual.*;

public final class TextMover extends MoveController {
	private final StringBuilder buf = new StringBuilder();
	private final Pantext text;
	private List<String> messages = null;
	private int index = 0;
	
	public TextMover(final Panlayer layer, final MultiFont fonts) {
		text = new Pantext(Pantil.vmid(), fonts, buf);
		layer.addActor(text);
		setVelocity(new FinPanple2(-1, 0));
		layer.addActor(this);
	}
	
	public TextMover(final Panlayer layer, final MultiFont fonts, final List<String> messages, final int firstIndex,
			final float y, final float z) {
		this(layer, fonts);
		setMessages(messages, firstIndex);
		setYz(y, z);
	}
	
	@Override
	protected final void onOob() {
		changeMessage();
	}
	
	private final void changeMessage() {
		correctIndex();
		Chartil.set(buf, messages.get(index));
		index++;
		correctIndex();
		text.getPosition().setX(Pangine.getEngine().getEffectiveWidth());
	}
	
	private final void correctIndex() {
		if (index >= messages.size()) {
			index = 0;
		}
	}
	
	public final void pickRandomMessage() {
		index = Mathtil.randi(0, messages.size() - 1);
	}
	
	public final void shuffleMessages() {
		Collections.shuffle(messages);
	}
	
	public final void setMessages(final List<String> messages) {
		setMessages(messages, 0);
	}
	
	public final void setMessages(final List<String> messages, final int firstIndex) {
		final boolean first = this.messages == null;
		this.messages = messages;
		index = firstIndex;
		if (first) {
			setToMove(text);
			changeMessage();
		}
	}
	
	public final int getIndex() {
		return index;
	}
	
	public final void setIndex(final int index) {
		this.index = index;
		correctIndex();
	}
	
	public final void setYz(final float y, final float z) {
		final Panple pos = text.getPosition();
		pos.setY(y);
		pos.setZ(z);
	}
}
