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

import org.pandcorps.game.actor.*;

public abstract class ShooterController extends Guy2Controller {
    protected Shooter shooter = null;
    
    protected ShooterController() {
    }
    
    public void setShooter(final Shooter shooter) {
    	setGuy(shooter);
        this.shooter = shooter;
    }
    
    /*package*/ final void attack() {
		shooter.attack();
	}
    
    /*package*/ final void attacking() {
		shooter.attacking();
	}
    
    /*package*/ final void interact() {
    	shooter.interact();
    }
    
	/*package*/ final void weapon1() {
		shooter.weapon1();
	}
	
	/*package*/ final void weapon2() {
		shooter.weapon2();
	}
	
	/*package*/ final void weapon3() {
		shooter.weapon3();
	}
	
	/*package*/ final void weapon4() {
		shooter.weapon4();
	}
	
	/*package*/ final void weapon5() {
		shooter.weapon5();
	}
	
	/*package*/ final void weapon6() {
		shooter.weapon6();
	}
	
	/*package*/ boolean onInteract(final Shooter initiator) {
		return false;
	}
	
	/*package*/ void onCollision(final Shooter shooter) {
	}
	
	/*package*/ void onHurt(final Projectile p) {
	}
	
	/*package*/ void onDestroy() {
	}
}
