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
package org.pandcorps.demo.pandax.physics.velocity;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;

public final class VelocityController extends Panctor implements RoomAddListener {
	public VelocityController(final String id) {
		super(id);
	}

	@Override
	public final void onRoomAdd(final RoomAddEvent event) {
		final Pangine engine = Pangine.getEngine();
		final Panmage img = (Panmage) engine.getEntity("LilStarImage");
		final Panimation anim = (Panimation) engine.getEntity("BigStarAnimation");
		float vel = 8;
		int max = 20;
		float z = -1;
		final Panroom room = event.getRoom();
		final Panple size = room.getSize();
		final float w = size.getX() - 1, h = size.getY() - 1;
		for (int a = 0; a < 2; a++) {
			for (int i = 0; i < max; i++) {
				final VelocityActor actor = new VelocityActor(Pantil.vmid());
				if (a == 0) {
					actor.setView(img);
				}
				else {
					actor.setView(anim);
				}
				actor.getPosition().set(Mathtil.randf(0, w), Mathtil.randf(0, h), z);
				actor.getVelocity().set(vel, 0);
				room.addActor(actor);
			}
			if (a == 1) {
				break;
			}
			vel = 4;
			max = 5;
			z = 1;
		}
	}
}