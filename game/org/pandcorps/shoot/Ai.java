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

import org.pandcorps.game.actor.Burst;
import org.pandcorps.pandam.Panple;

public class Ai extends ShooterController {
	/*package*/ static int bamDelay;
	private int bamTimer = 0;
	
	@Override
    public final void step() {
		if (bamTimer > 0) {
			bamTimer--;
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
			shooter.getLayer().addActor(bam);
			shooter.onHurt(10);
			bamTimer = bamDelay;
		}
	}
	
	private final Shooter getTarget() {
		return ShootGame.shooter;
	}
}
