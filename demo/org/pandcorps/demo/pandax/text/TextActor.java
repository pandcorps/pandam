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
package org.pandcorps.demo.pandax.text;

import org.pandcorps.core.img.Pancolor;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Fonts.*;

public final class TextActor extends Panctor implements RoomAddListener {
	public TextActor(final String id) {
		super(id);
	}

	@Override
	public void onRoomAdd(RoomAddEvent event) {
		final Pantext text = new Pantext("TextText", new ByteFont((Panmage) getCurrentDisplay()), "Aa1");
		event.getRoom().addActor(text);
		text.getPosition().set(getPosition());
		
		final Font upper = Fonts.getSimple(new FontRequest(FontType.Upper), Pancolor.GREEN);
		// Upper Fonts don't contain lowercase letters but will automatically convert them to uppercase.
		// They contain everything from ' ' to '_'.
		final Pantext text2 = new Pantext("Text2", upper, "Upper & stuff like '_'");
		event.getRoom().addActor(text2);
        text2.getPosition().set(getPosition());
        text2.getPosition().add(0, -16);
		
		final Font number = Fonts.getSimple(new FontRequest(FontType.Number), Pancolor.RED);
		// Number Fonts don't contain space characters but will automatically leave an empty slot for them when rendering.
		// They contain everything from '*' to '9'.
		final Pantext text3 = new Pantext("Text3", number, "1 2... 3*3 - 9");
        event.getRoom().addActor(text3);
        text3.getPosition().set(getPosition());
        text3.getPosition().add(0, -32);
	}
}
