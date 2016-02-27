/*
Copyright (c) 2009-2016, Andrew M. Martin
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
package org.pandcorps.test.pandam;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.*;

public final class TestPanmlGame extends PandamTest {
	public final void testInit() {
		final PanmlGame actual = new PanmlGame("org/pandcorps/test/pandam/test.panml");
		actual.init();
		UnitPangine.setEngine(new UnitPangine());
		final Pangame expected = new ExpectedGame();
		expected.init();
		assertEquals(expected, actual);
	}

	private final static class ExpectedGame extends Pangame {
		private Panroom room = null;

		@Override
		public final void init() {
			final Pangine engine = Pangine.getEngine();
			final Panmage lilIm1 = engine.createImage("LilImage1", "org/pandcorps/test/resource/img/lil1_8x8.gif");
			final Panmage lilIm2 = engine.createImage("LilImage2", "org/pandcorps/test/resource/img/lil2_8x8.gif");
			final Panmage bigIm1 = engine.createImage("BigImage1", "org/pandcorps/test/resource/img/big1_16x16.gif");
			final Panmage bigIm2 = engine.createImage("BigImage2", "org/pandcorps/test/resource/img/big1_16x16.gif");
			final Panframe lilFr1 = engine.createFrame("LilFrame1", lilIm1, 14);
			final Panframe lilFr2 = engine.createFrame("LilFrame2", lilIm2, 1);
			final Panframe bigFr1 = engine.createFrame("BigFrame1", bigIm1, 28);
			final Panframe bigFr2 = engine.createFrame("BigFrame2", bigIm2, 2);
			final Panimation lilAn = engine.createAnimation("LilAnimation", lilFr1, lilFr2);
			final Panimation bigAn = engine.createAnimation("BigAnimation", bigFr1, bigFr2);
			engine.createType("LilType", LilActor.class, lilAn);
			engine.createType("BigType", BigActor.class, bigAn);
			room = engine.createRoom("TestRoom", 640, 480, 0);
			final LilActor lilAc = new LilActor("LilActor");
			lilAc.getPosition().set(300, 220, 0);
			lilAc.setVisible(true);
			room.addActor(lilAc);
			final BigActor bigAc = new BigActor("BigActor");
			bigAc.getPosition().set(340, 260, 0);
			bigAc.setVisible(false);
			room.addActor(bigAc);
		}
		
		@Override
		protected final FinPanple getFirstRoomSize() {
	        throw new UnsupportedOperationException();
	    }

		@Override
	    protected final void init(final Panroom room) throws Exception {
	        throw new UnsupportedOperationException();
	    }

		@Override
		protected final Panroom getFirstRoom() {
			return room;
		}
	}
}
