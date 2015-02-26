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
package org.pandcorps.furguardians;

import org.pandcorps.core.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;

public final class Spark extends Burst implements StepListener {
	public final static int DEF_COUNT = 3;
	private final int count;
	private final boolean end; // Could replace with handler
	private int age = 0;
	
	public Spark(final float x, final float y, final boolean end) {
		this(FurGuardiansGame.room, DEF_COUNT, x, y, end);
	}
	
	public Spark(final Panlayer layer, final int count, final float x, final float y, final boolean end) {
		super(FurGuardiansGame.spark);
		this.count = count;
		this.end = end;
		FurGuardiansGame.setPosition(this, x + Mathtil.randf(-7, 7), y + Mathtil.randf(-7, 7), FurGuardiansGame.DEPTH_SPARK);
		layer.addActor(this);
	}

	@Override
	public final void onStep(final StepEvent event) {
		if (count == 0) {
			return;
		} else if (age == 2) {
			final Panple pos = getPosition();
			new Spark(getLayer(), count - 1, pos.getX(), pos.getY(), end);
		}
		age++;
	}
	
	@Override
	public void onDestroy() {
		if (end && count == 0 && age != -1) {
			age = -1;
			FurGuardiansGame.levelClose();
		}
	}
}
