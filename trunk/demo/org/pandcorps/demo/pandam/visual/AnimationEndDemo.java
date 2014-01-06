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
package org.pandcorps.demo.pandam.visual;

import org.pandcorps.core.*;
import org.pandcorps.demo.DemoGame;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;

public final class AnimationEndDemo extends DemoGame {
	@Override
	protected final void init(final Panroom room) {
		final class AnimationEndActor extends Panctor implements AnimationEndListener {
			public AnimationEndActor(final String id) {
				super(id);
			}

			@Override
			public final void onAnimationEnd(final AnimationEndEvent event) {
				getPosition().set(Mathtil.randf(8, 632), Mathtil.randf(8, 472));
			}
		}

		final Pangine engine = Pangine.getEngine();
		final Panmage image1 = engine.createImage("AnimationEndImage1", "org/pandcorps/demo/res/img/SquareGuy.gif");
		final Panframe frame1 = engine.createFrame("AnimationEndFrame1", image1, 25);
		final Panmage image2 = engine.createImage("AnimationEndImage2", "org/pandcorps/demo/res/img/SquareGuyBlink.gif");
		final Panframe frame2 = engine.createFrame("AnimationEndFrame2", image2, 5);
		final Panimation anim = engine.createAnimation("AnimationEndAnimation", frame1, frame2);
		engine.createType("AnimationEndType", AnimationEndActor.class, anim);
		final Panctor actor = new AnimationEndActor("AnimationEndActor");
		actor.getPosition().set(320, 240);
		room.addActor(actor);
	}

	public final static void main(final String[] args) {
		try {
			new AnimationEndDemo().start();
		} catch (final Throwable e) {
			e.printStackTrace();
		}
	}
}
