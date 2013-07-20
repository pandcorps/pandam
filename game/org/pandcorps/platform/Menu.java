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
import org.pandcorps.core.img.*;
import org.pandcorps.game.core.ImtilX;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.platform.Player.*;

public class Menu {
	protected final static class AvatarScreen extends Panscreen {
		private final PlayerContext pc;
		private Panmage timg = null;
		private Panctor actor = null;
		
		protected AvatarScreen(final PlayerContext pc) {
			this.pc = pc;
		}
		
		@Override
		protected final void load() throws Exception {
			final float w = PlatformGame.SCREEN_W, h = PlatformGame.SCREEN_H;
			final Pangine engine = Pangine.getEngine();
			final Panroom room = engine.createRoom(Pantil.vmid(), w, h, 0);
			engine.setBgColor(new FinPancolor((short) 128, (short) 192, Pancolor.MAX_VALUE));
			Pangame.getGame().setCurrentRoom(room);
			
			final TileMap tm = new TileMap(Pantil.vmid(), room, ImtilX.DIM, ImtilX.DIM);
			timg = Level.getTileImage();
			tm.setImageMap(timg);
			final TileMapImage[][] imgMap = tm.splitImageMap();
			tm.fillBackground(imgMap[1][1], 0, 1);
			room.addActor(tm);
			
			actor = new Panctor();
			actor.setView(pc.guy);
			actor.getPosition().set(w / 2, 16);
			room.addActor(actor);
			final Avatar avt = pc.profile.currentAvatar;
			final Panform form = new Panform();
			final List<String> animals = Arrays.asList("Bear", "Cat", "Mouse", "Rabbit");
			final AvtListener anmLsn = new AvtListener() {
				@Override public final void update(final String value) {
					avt.anm = value; }};
			addRadio(form, animals, anmLsn, 8);
			final List<String> eyes = Arrays.asList("1", "2", "3", "4");
			final AvtListener eyeLsn = new AvtListener() {
				@Override public final void update(final String value) {
					avt.eye = Integer.parseInt(value); }};
			addRadio(form, eyes, eyeLsn, 72);
			final List<String> colors = Arrays.asList("0", "1", "2", "3", "4");
			final AvtListener redLsn = new ColorListener() {
				@Override public final void update(final float value) {
					avt.r = value; }};
			addRadio(form, colors, redLsn, 96);
			final AvtListener greenLsn = new ColorListener() {
				@Override public final void update(final float value) {
					avt.g = value; }};
			addRadio(form, colors, greenLsn, 120);
			final AvtListener blueLsn = new ColorListener() {
				@Override public final void update(final float value) {
					avt.b = value; }};
			addRadio(form, colors, blueLsn, 144);
			form.init();
		}
		
		private abstract class AvtListener implements RadioSubmitListener {
			@Override public final void onSubmit(final RadioSubmitEvent event) {
				update(event.toString());
				PlatformGame.reloadAnimalStrip(pc);
				actor.setView(pc.guy);
			}
			
			protected abstract void update(final String value);
		}
		
		private abstract class ColorListener extends AvtListener {
			@Override
			protected final void update(final String value) {
				update(Integer.parseInt(value) / 4f);
			}
			
			protected abstract void update(final float value);
		}
		
		@Override
	    protected final void destroy() {
			timg.destroy();
		}
	}
	
	private final static void addRadio(final Panform form, final List<String> list, final RadioSubmitListener lsn, final int x) {
		final RadioGroup anmGrp = new RadioGroup(PlatformGame.font, list, null);
		anmGrp.setChangeListener(lsn);
		final Pantext anmLbl = anmGrp.getLabel();
		anmLbl.getPosition().set(x, 176);
		anmLbl.setBackground(Pantext.CHAR_SPACE);
		anmLbl.setBorderStyle(BorderStyle.Simple);
		form.addItem(anmGrp);
	}
}
