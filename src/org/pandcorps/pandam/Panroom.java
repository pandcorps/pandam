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
package org.pandcorps.pandam;

import org.pandcorps.pandam.event.RoomAddEvent;
import org.pandcorps.pandam.impl.FinPanple;

public final class Panroom extends Panlayer {
    /*package*/ final RoomAddEvent addEvent = new RoomAddEvent(this);
    
    /*package*/ Panlayer base;
    
	/*package*/ Panroom(
		final String id,
		final float width, final float height, final float depth) {
	    super(id, width, height, depth, null);
	    base = this;
	}
	
	/*package*/ Panroom(final String id, final FinPanple size) {
        super(id, size, null);
        base = this;
    }

	@Override
	public final Panlayer getBase() {
	    return base;
	}
	
	public void clear() {
		base.destroyAllActors();
		Panlayer layer;
		while ((layer = base.getAbove()) != null) {
			layer.destroy();
		}
		while ((layer = base.getBeneath()) != null) {
			layer.destroy();
		}
	}
}
