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
package org.pandcorps.shoot;

import java.awt.image.BufferedImage;

import org.pandcorps.core.Imtil;
import org.pandcorps.core.Pantil;
import org.pandcorps.core.img.FinPancolor;
import org.pandcorps.core.img.Pancolor;
import org.pandcorps.game.actor.Decoration;
import org.pandcorps.game.core.ImtilX;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Fonts.FontRequest;
import org.pandcorps.shoot.ShootGame.ShootScreen;

public class Story {
	private static Font font = null;
	private static IntroSequence intro = new IntroSequence();
	private static Panmage bgEurope = null;
	private static Panmage bgPodium = null;
	private static Panmage bgOvalOffice = null;
	private static Panmage bgGym = null;
	private static Panmage chrBladimir = null;
	private static Panmage chrBladigar = null;
	private static Panmage chrBladander = null;
	private static Panmage chrPotus = null;
	private static Panmage chrWill = null;
	
	protected abstract static class StoryScreen extends TextScreen {
		private final Panmage bgImg;
		public StoryScreen(final TextScreenSequence sequence, final String msg, final Panmage bg) {
			super(sequence, new Pantext(Pantil.vmid(), font, msg, 28));
			text.getPosition().set(16, 64, 0);
			text.setLinesPerPage(4);
			bgImg = bg;
		}
		
		protected Decoration addDec(final Panmage img, final float x, final float y, final float z) {
			final Decoration act = new Decoration(Pantil.vmid());
			act.setView(img);
			act.getPosition().set(x, y, z);
			Pangame.getGame().getCurrentRoom().addActor(act);
			return act;
		}
		
		@Override
		protected final void start() throws Exception {
			addDec(bgImg, 96, 96, 0);
			startExtra();
		}
		
		protected void startExtra() throws Exception {
		}
	}
	
	protected final static class IntroSequence extends TextScreenSequence {
		@Override
		protected void cancel() {
			bgEurope.destroy();
			bgPodium.destroy();
			bgOvalOffice.destroy();
			bgGym.destroy();
			chrBladimir.destroy();
			chrBladigar.destroy();
			chrBladander.destroy();
			chrPotus.destroy();
			chrWill.destroy();
			Panscreen.set(new ShootScreen());
		}		
	}
	
	protected abstract static class IntroScreen extends StoryScreen {
		public IntroScreen(final String msg, final Panmage bg) {
			super(intro, msg, bg);
		}
	}
	
	protected final static class MapScreen extends IntroScreen {

		public MapScreen() {
			super("Hi", bgEurope);
		}

		@Override
		protected void startExtra() throws Exception {
			
		}

		@Override
		protected void finish() {
			Panscreen.set(new PodiumScreen());
		}
	}
	
	protected final static class PodiumScreen extends IntroScreen {

		public PodiumScreen() {
			super("Hi", bgPodium);
		}

		@Override
		protected void startExtra() throws Exception {
			addDec(chrBladimir, 120, 124, 1);
			addDec(chrBladigar, 96, 104, 1);
			addDec(chrBladander, 159, 104, 1).setMirror(true);
		}

		@Override
		protected void finish() {
			Panscreen.set(new OvalOfficeScreen());
		}
	}
	
	protected final static class OvalOfficeScreen extends IntroScreen {

		public OvalOfficeScreen() {
			super("Hi", bgOvalOffice);
		}

		@Override
		protected void startExtra() throws Exception {
			addDec(chrPotus, 120, 124, 1);
		}

		@Override
		protected void finish() {
			Panscreen.set(new GymScreen());
		}
	}
	
	protected final static class GymScreen extends IntroScreen {

		public GymScreen() {
			super("Hi", bgGym);
		}

		@Override
		protected void startExtra() throws Exception {
			addDec(chrWill, 120, 100, 1);
		}

		@Override
		protected void finish() {
			cancel();
		}
	}
	
	private final static Panmage getImg(final String type, final String name) {
		return Pangine.getEngine().createImage("img." + type + '.' + name, "org/pandcorps/shoot/res/" + type + '/' + name + ".png");
	}
	
	private final static Panmage getBg(final String name) {
		return getImg("story", name);
	}
	
	private final static BufferedImage getChrBi(final String name) {
		return ImtilX.loadStrip("org/pandcorps/shoot/res/chr/" + name + ".png")[0];
	}
	
	private final static Panmage getChr(final String name) {
		return getChr(name, -1);
	}
	
	private final static Panmage getChr(final String name, int h) {
		BufferedImage bi = getChrBi(name);
		if (h >= 0) {
			//bi = bi.getSubimage(0, 0, 16, h);
			final short m = Pancolor.MIN_VALUE;
			Imtil.drawRectangle(bi, 0, h, 16, 16 - h, m, m, m, m);
		}
		return Pangine.getEngine().createImage("img.chr." + name, bi);
	}
	
	protected final static void playIntro() {
		bgEurope = getBg("Europe");
		bgPodium = getBg("Podium");
		bgOvalOffice = getBg("OvalOffice");
		bgGym = getBg("Gym");
		chrBladimir = getChr("Bladimir", 12);
		chrBladigar = getChr("Bladigar");
		chrBladander = getChr("Bladander");
		chrPotus = getChr("Potus", 12);
		chrWill = getChr("Will");
		font = Fonts.getOutline(new FontRequest(8), Pancolor.BLUE, Pancolor.BLUE, Pancolor.BLUE, new FinPancolor(Pancolor.MIN_VALUE, Pancolor.MIN_VALUE, (short) 128, Pancolor.MAX_VALUE));
		Panscreen.set(new MapScreen());
	}
}
