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

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.game.actor.Decoration;
import org.pandcorps.game.core.ImtilX;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Fonts.FontRequest;
import org.pandcorps.pandax.visual.Pantexture;
import org.pandcorps.shoot.ShootGame.ShootScreen;
import org.pandcorps.shoot.Shooter.ShooterDefinition;

public class Story {
	private static Font font = null;
	private static IntroSequence intro = new IntroSequence();
	private static Panmage imgBlack = null;
	private static Panmage bgEurope = null;
	private static Panmage bgEurope2 = null;
	private static Panmage bgPodium = null;
	private static Panmage bgOvalOffice = null;
	private static Panmage bgGym = null;
	private static Panmage bgTitle = null;
	private static Panmage chrBladimir = null;
	private static Panmage chrBladigar = null;
	private static Panmage chrBladander = null;
	private static Panmage chrPotus = null;
	private static Panmage chrWill = null;
	private static Panimation anmTrp = null;
	
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
			imgBlack.destroy();
			bgEurope.destroy();
			bgEurope2.destroy();
			bgPodium.destroy();
			bgOvalOffice.destroy();
			bgGym.destroy();
			bgTitle.destroy();
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
			super("Europe\n2079", bgEurope);
		}

		@Override
		protected void finish() {
			Panscreen.set(new Map2Screen());
		}
	}
	
	protected final static class Map2Screen extends IntroScreen {

		public Map2Screen() {
			super("The Black Reich of Bladavosnia spreads across the continent.  None can withstand the onslaught of the deadly Blitztroopers.", bgEurope2);
		}
		
		private void addBorder(final Panroom room, final int x, final int y, final int w, final int h) {
			final Pantexture left = new Pantexture("tex." + x + "." + y, imgBlack);
			room.addActor(left);
			left.getPosition().set(x, y, 256);
			left.setSize(w, h);
		}

		@Override
		protected void startExtra() throws Exception {
			final Panroom room = Pangame.getGame().getCurrentRoom();
			addBorder(room, 16, 96, 80, 96);
			addBorder(room, 96, 80, 64, 16);
			for (int j = 0; j < 6; j++) {
				final int yoff = j * 8;
				for (int i = 0; i < 2; i++) {
					final Trp trp = new Trp();
					trp.getPosition().set(88 - (i * 16) - yoff, 128 + yoff, 64 - yoff);
					room.addActor(trp);
				}
			}
		}

		@Override
		protected void finish() {
			Panscreen.set(new PodiumScreen());
		}
	}
	
	protected final static class PodiumScreen extends IntroScreen {

		public PodiumScreen() {
			super("The ruthless dictator Bladimir will not stop until the entire world is in his grasp.\n\"Hurravah, Bladavosnia!\"\n\"Hurravah, Bladavosnia!\"", bgPodium);
		}

		@Override
		protected void startExtra() throws Exception {
			addDec(chrBladimir, 135, 124, 1).setMirror(true);
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
			super("Joseph Darkwater, the President of the United States, remembers his days with the Marines and reaches out to an old friend.  \"Will, I don't know what to do.\"", bgOvalOffice);
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
			super("\"I do.  I will kill 'em.  All of 'em.\"", bgGym);
		}

		@Override
		protected void startExtra() throws Exception {
			addDec(chrWill, 120, 100, 1);
		}

		@Override
		protected void finish() {
			Panscreen.set(new TitleScreen());
		}
	}
	
	protected final static class TitleScreen extends IntroScreen {

		public TitleScreen() {
			super("Will Killem\nEpisode 1\nVeni, Vidi, Vici, Baby", bgTitle);
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
		return ShootGame.loadChrStrip(name)[0];
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
		return getChr(name, bi);
	}
	
	private final static Panmage getChr(final String name, final BufferedImage bi) {
		return Pangine.getEngine().createImage("img.chr." + name, bi);
	}
	
	private final static BufferedImage getTrpImg(final BufferedImage[] strip, final int i, final BufferedImage head, final int h) {
		final BufferedImage body = strip[i];
		Imtil.copy(head, body, 0, 0, ImtilX.DIM, ImtilX.DIM - h, 0, -h, true);
		//return getChr("Blitztrooper." + i, body);
		return body;
	}
	
	private final static Panimation getTrpAnm() {
		final BufferedImage[] strip = ShootGame.loadChrStrip("Blitztrooper");
		final BufferedImage head = strip[4];
		final BufferedImage still = getTrpImg(strip, 0, head, 0);
		final BufferedImage left = getTrpImg(strip, 1, head, 1);
		final BufferedImage right = getTrpImg(strip, 2, head, 1);
		return ShooterDefinition.create("Blitztrooper", still, left, right).walk;
	}
	
	private final static class Trp extends Panctor implements StepListener {
		public Trp() {
			super(Pantil.vmid());
			setView(anmTrp);
		}
		
		public void onStep(final StepEvent event) {
			if ((Pangine.getEngine().getClock() % 2) != 0) {
				return;
			}
			final Panple pos = getPosition();
			pos.add(1, -1, 1);
			if (pos.getY() < 80) {
				pos.add(-48, 48, -48);
			}
		}
	}
	
	private final static Panmage getBlack() {
		final int d = 16;
		BufferedImage bi = new BufferedImage(d, d, Imtil.TYPE);
		final short m = Pancolor.MIN_VALUE;
		bi = Imtil.drawRectangle(bi, 0, 0, d, d, m, m, m, Pancolor.MAX_VALUE);
		return Pangine.getEngine().createImage("img.black", bi);
	}
	
	protected final static void playIntro() {
		imgBlack = getBlack();
		bgEurope = getBg("Europe");
		bgEurope2 = getBg("Europe2");
		bgPodium = getBg("Podium");
		bgOvalOffice = getBg("OvalOffice");
		bgGym = getBg("Gym");
		bgTitle = getBg("Title");
		chrBladimir = getChr("Bladimir", 12);
		chrBladigar = getChr("Bladigar");
		chrBladander = getChr("Bladander");
		chrPotus = getChr("Potus", 12);
		chrWill = getChr("Will");
		anmTrp = getTrpAnm();
		font = Fonts.getOutline(new FontRequest(8), Pancolor.BLUE, Pancolor.BLUE, Pancolor.BLUE, new FinPancolor(Pancolor.MIN_VALUE, Pancolor.MIN_VALUE, (short) 128, Pancolor.MAX_VALUE));
		Panscreen.set(new MapScreen());
	}
}
