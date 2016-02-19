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
package org.pandcorps.demo.pandax.physics.velocity;

import org.pandcorps.pandam.*;

public final class VelocityDemo {
	/*
	extends Pangame {
	@Override
	protected final Panroom getFirstRoom() {
		final Pangine engine = Pangine.getEngine();
		final Panroom room = engine.createRoom("VelocityRoom", 640, 480);

		final Panmage imageLilStar = engine.createImage("LilStarImage", "org/pandcorps/demo/pandax/LilStar.gif");
		final Panframe frameLilStar = engine.createFrame("LilStarFrame", imageLilStar, 1);
		final Panimation animLilStar = engine.createAnimation("LilStarAnimation", frameLilStar, frameLilStar);

		final Panmage imageBigStar1 = engine.createImage("BigStarImage1", "org/pandcorps/demo/pandax/BigStar1.gif");
		final Panframe frameBigStar1 = engine.createFrame("BigStarFrame1", imageBigStar1, 28);
		final Panmage imageBigStar2 = engine.createImage("BigStarImage2", "org/pandcorps/demo/pandax/BigStar2.gif");
		final Panframe frameBigStar2 = engine.createFrame("BigStarFrame2", imageBigStar2, 2);
		//final Panimation animBigStar =
		engine.createAnimation("BigStarAnimation", frameBigStar1, frameBigStar2);

		engine.createType("VelocityType", VelocityActor.class, animLilStar);
		engine.createType("VelocityControllerType", VelocityController.class, animLilStar);

		return room;
	}
	*/

	public final static void main(final String[] args) {
		try {
			new
				//VelocityDemo()
				PanmlGame("org/pandcorps/demo/pandax/physics/velocity/VelocityDemo.panml")
				.start();
		} catch (final Throwable e) {
			e.printStackTrace();
		}
	}
}
