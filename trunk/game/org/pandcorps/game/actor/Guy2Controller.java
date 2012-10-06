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
package org.pandcorps.game.actor;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.action.ActionEvent;
import org.pandcorps.pandam.event.action.ActionListener;

public class Guy2Controller {
	protected Guy2 guy = null;
    
    protected Guy2Controller() {
    }
    
    public void step() {
    }
    
    protected void setGuy(final Guy2 guy) {
    	if (this.guy == guy) {
    		return;
    	}
    	if (this.guy != null) {
    		this.guy.controller = null;
    	}
        this.guy = guy;
        if (guy != null) {
        	if (guy.controller != null) {
        		guy.controller.guy = null;
        	}
        	guy.controller = this;
        }
    }
    
	protected final void walkDown() {
		guy.walkDown();
	}

	protected final void walkUp() {
		guy.walkUp();
	}

	protected final void walkLeft() {
		guy.walkLeft();
	}

	protected final void walkRight() {
		guy.walkRight();
	}
	
	protected final void registerPlayer(final Panctor bound) {
		final Panteraction inter = Pangine.getEngine().getInteraction();
		bound.register(inter.KEY_DOWN, new ActionListener() {@Override public void onAction(final ActionEvent event) {
            walkDown();
        }});
        bound.register(inter.KEY_UP, new ActionListener() {@Override public void onAction(final ActionEvent event) {
            walkUp();
        }});
        bound.register(inter.KEY_LEFT, new ActionListener() {@Override public void onAction(final ActionEvent event) {
            walkLeft();
        }});
        bound.register(inter.KEY_RIGHT, new ActionListener() {@Override public void onAction(final ActionEvent event) {
            walkRight();
        }});
	}
	
	protected final void stepAdvance(final Panctor target) {
		final Panple pos = guy.getPosition();
		final Panple tpos = target.getPosition();
		final float x = pos.getX(), y = pos.getY();
		final float tx = tpos.getX(), ty = tpos.getY();
		if (tx < x - 1) {
			walkLeft();
		} else if (tx > x + 1) {
			walkRight();
		}
		if (ty < y - 1) { // Without +/- 1, can get rapid up/down alternating
			walkDown();
		} else if (ty > y + 1) {
			walkUp();
		}
	}
	
	protected final boolean stepRetreat(final Panctor target) {
		final Panple pos = guy.getPosition();
		final Panple tpos = target.getPosition();
		final float x = pos.getX(), y = pos.getY();
		final float tx = tpos.getX(), ty = tpos.getY();
		//Change behavior if hit boundary, but hitting x boundary wouldn't need to mean to stop y movement.
		final Panple min = guy.getMin(), max = guy.getMax();
		if (x <= min.getX() || x >= max.getX() || y <= min.getY() || y >= max.getY()) {
			return false;
		} else {
    		if (tx < x) {
				walkRight();
			} else if (tx > x) {
				walkLeft();
			} //TODO else walk toward center of bg
			if (ty < y) {
				walkUp();
			} else if (ty > y) {
				walkDown();
			} //TODO else walk toward center of bg
		}
		return true;
	}
}
