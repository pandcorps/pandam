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
package org.pandcorps.platform;

import java.util.*;

import org.pandcorps.core.Pantil;
import org.pandcorps.core.img.Pancolor;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.platform.Player.*;

public class Menu {
	protected final static class AvatarScreen extends Panscreen {
		private final PlayerContext pc;
		
		protected AvatarScreen(final PlayerContext pc) {
			this.pc = pc;
		}
		
		@Override
		protected final void load() throws Exception {
			final float w = PlatformGame.SCREEN_W, h = PlatformGame.SCREEN_H;
			final Pangine engine = Pangine.getEngine();
			final Panroom room = engine.createRoom(Pantil.vmid(), w, h, 0);
			engine.setBgColor(Pancolor.YELLOW);
			Pangame.getGame().setCurrentRoom(room);
			final Panctor actor = new Panctor();
			actor.setView(pc.guy);
			actor.getPosition().set(w / 2, h / 2);
			room.addActor(actor);
			final Avatar avt = pc.profile.currentAvatar;
			final Panform form = new Panform();
			final List<String> animals = Arrays.asList("Bear", "Cat", "Mouse", "Rabbit");
			final RadioSubmitListener anmLsn = new RadioSubmitListener() {
				@Override public final void onSubmit(final RadioSubmitEvent event) {
					avt.anm = event.toString(); }};
			addRadio(form, animals, anmLsn, 8);
			final List<String> eyes = Arrays.asList("1", "2", "3", "4");
			final RadioSubmitListener eyeLsn = new RadioSubmitListener() {
				@Override public final void onSubmit(final RadioSubmitEvent event) {
					avt.eye = Integer.parseInt(event.toString()); }};
			addRadio(form, eyes, eyeLsn, 80);
			form.init();
		}
	}
	
	private final static void addRadio(final Panform form, final List<String> list, final RadioSubmitListener lsn, final int x) {
		final RadioGroup anmGrp = new RadioGroup(PlatformGame.font, list, lsn);
		final Pantext anmLbl = anmGrp.getLabel();
		anmLbl.getPosition().set(x, 64);
		anmLbl.setBackground(Pantext.CHAR_SPACE);
		anmLbl.setBorderStyle(BorderStyle.Simple);
		form.addItem(anmGrp);
	}
}
