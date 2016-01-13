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
package org.pandcorps.game.actor;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.tile.*;

public abstract class Guy4Controller {
    public final static RandomController RANDOM = new RandomController();
    //public final static NpcController STILL = null;
    
	public final static boolean onStillPlayer(final Guy4 guy) {
	    return onStillPlayer(ControlScheme.getDefaultKeyboard(), guy);
	}
	
	public final static boolean onStillPlayer(final ControlScheme ctrl, final Guy4 guy) {
        if (ctrl.getDown().isActive()) {
            guy.go(Direction.South);
        } else if (ctrl.getUp().isActive()) {
        	guy.go(Direction.North);
        } else if (ctrl.getLeft().isActive()) {
        	guy.go(Direction.West);
        } else if (ctrl.getRight().isActive()) {
        	guy.go(Direction.East);
        } else if (Panput.isActive(ctrl.get1())) {
            final TileOccupant neighbor = guy.getFacing();
            if (neighbor != null) {
                neighbor.onInteract(guy);
            }
        } else {
        	return false;
        }
        return true;
	}
	
	public final static void onStillNpc(final Guy4 guy) {
        final Direction dir = Mathtil.rand(Guy4.weights, Guy4.directions);
        if (dir != null) {
            guy.go(dir);
        }
    }
	
	public final static void onStill(final Guy4 guy, final NpcController controller) {
	    if (controller != null) {
	        controller.onStill(guy);
	    }
	}
	
	public static interface NpcController {
	    public void onStill(final Guy4 guy);
	}
	
	public final static class RandomController implements NpcController {
	    private RandomController() {
	    }
	    
        @Override
        public final void onStill(final Guy4 guy) {
            onStillNpc(guy);
        }
	}
}
