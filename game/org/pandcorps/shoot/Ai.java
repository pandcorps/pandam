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

import org.pandcorps.core.Mathtil;
import org.pandcorps.game.actor.Burst;
import org.pandcorps.pandam.Panple;

public class Ai extends ShooterController {
	private final static byte ACTION_STILL = 0;
	private final static byte ACTION_ADVANCE = 1;
	private final static byte ACTION_RETREAT = 2;
	
    private byte timer = 0;
    private byte action = ACTION_STILL;
    
	/*package*/ static int bamDelay;
	private int bamTimer = 0;
	
	@Override
    public final void step() {
		if (bamTimer > 0) {
			bamTimer--;
		}
		if (timer > 0) {
			timer--;
		} else {
			final int r = Mathtil.randi(0, 99);
			if (r < 50) {
				action = ACTION_STILL;
				timer = Mathtil.randb((byte) 15, (byte) 50);
			} else if (r < 80) {
				action = ACTION_ADVANCE;
				timer = Mathtil.randb((byte) 15, (byte) 40);
			} else {
				action = ACTION_RETREAT;
				timer = Mathtil.randb((byte) 10, (byte) 30);
			}
		}
		if (action == ACTION_ADVANCE) {
			stepAdvance(getTarget());
		} else if (action == ACTION_RETREAT) {
			if (!stepRetreat(getTarget())) {
				action = ACTION_STILL;
				timer = 0;
			}
		}
	}
	
	@Override
	/*package*/ final void onCollision(final Shooter shooter) {
		if (bamTimer == 0 && shooter == getTarget()) {
			final Burst bam = new Burst(ShootGame.bam);
			final Panple pos = bam.getPosition();
			pos.set(shooter.getPosition());
			pos.add(this.guy.getPosition());
			pos.multiply(0.5f);
			pos.addY(Shooter.OFF_ADD_Y);
			pos.addZ(2);
			shooter.getLayer().addActor(bam);
			shooter.onHurt(10);
			bamTimer = bamDelay;
		}
	}
	
	private final Shooter getTarget() {
		return ShootGame.shooter;
	}
}
