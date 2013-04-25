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
package org.pandcorps.platform;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.boundary.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.*;
import org.pandcorps.pandax.tile.*;

public class Tiles {
    private final static FinPanple g = new FinPanple(0, Player.g, 0);
    
    protected final static void bump(final Tile t) {
    	final byte b = t.getBehavior();
    	if (b == PlatformGame.TILE_BREAK) {
    		t.setForeground(null, false);
    		final Panple pos = t.getPosition();
    		final float x = pos.getX(), y = pos.getY();
    		new Shatter(x, y, -2, 2);
    		new Shatter(x + 8, y, 2, 2);
    		new Shatter(x, y + 8, -1, 3);
    		new Shatter(x + 8, y + 8, 1, 3);
    	} else if (b == PlatformGame.TILE_BUMP) {
    	    new Bump(t); // Copy image before changing
    		t.setForeground(PlatformGame.imgMap[0][1]);
    	}
    }
    
    private final static class Shatter extends Pandy implements AllOobListener {
        private Shatter(final float x, final float y, final int xm, final int ym) {
            super(g);
            setView(PlatformGame.block8);
            PlatformGame.setPosition(this, x, y, PlatformGame.DEPTH_SHATTER);
            getVelocity().set(xm * Mathtil.randf(0.7f, 1.3f), ym * Mathtil.randf(0.7f, 1.3f));
            PlatformGame.room.addActor(this);
        }

        @Override
        public final void onAllOob(final AllOobEvent event) {
            destroy();
        }
    }
    
    private final static class Bump extends TileActor implements StepListener {
        private int age = 0;
        private Bump(final Tile t) {
            setViewFromForeground(t);
            final Panple pos = t.getPosition();
            PlatformGame.setPosition(this, pos.getX(), pos.getY() + 2, PlatformGame.DEPTH_SHATTER);
            PlatformGame.room.addActor(this);
        }

        @Override
        public final void onStep(final StepEvent event) {
            if (age < 2) {
                getPosition().addY(2);
            } else if (age < 5) {
                getPosition().addY(-1);
            } else {
                destroy();
                return;
            }
            age++;
        }
    }
}
