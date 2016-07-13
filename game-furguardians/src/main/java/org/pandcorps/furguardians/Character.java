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
package org.pandcorps.furguardians;

import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.tile.*;

public abstract class Character extends GuyPlatform {
	protected Character(final int offX, final int h) {
		super(offX, h);
	}
	
	protected final void flipAndFall(final float v) {
	    flipAndFall(this, H, v);
	}
	
	protected final static void flipAndFall(final Panctor actor, final int h, final float v) {
        final Panple pos = actor.getPosition();
        final Tiles.Faller f = new Tiles.Faller((Panmage) actor.getCurrentDisplay(), pos.getX(), pos.getY() + h, 0, v);
        f.setMirror(actor.isMirror());
        f.setFlip(true);
        actor.destroy();
    }
	
	@Override
	protected final void onBump(final int t) {
	    Tiles.bump(this, t);
	}
	
	protected void onLanded() {
	    v = 0;
	}
	
	//@OverrideMe
	protected boolean onHorizontal(final int off) {
		return false;
	}
	
	//@OverrideMe
	protected boolean onAir() {
		return false;
	}
	
	//@OverrideMe
	protected void onWall() {
	}
	
	//@OverrideMe
    protected void onEnd() {
    }
	
	//@OverrideMe
	protected void onBump(final Character c) {
	}
	
	protected abstract boolean onFell();
	
	@Override
	protected final TileMap getTileMap() {
        return Level.tm;
    }
	
	@Override
	protected final boolean isSolidBehavior(final byte b) {
        return b == FurGuardiansGame.TILE_BREAK || b == FurGuardiansGame.TILE_BUMP;
    }
}
