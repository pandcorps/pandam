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

import org.pandcorps.game.core.ImtilX;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.FinPanple;
import org.pandcorps.pandax.tile.*;

public class Player extends Panctor implements StepListener {
    private final static int H = 16;
	private final static int OFF_GROUNDED = -1;
	private final static int OFF_BUTTING = H + 1;
	private final static int OFF_X = 7;
	private final static int VEL_WALK = 3;
	
	protected static int g = -1;
	private int v = 0;
	
	public Player() {
		final Pangine engine = Pangine.getEngine();
		setView(engine.createImage("guy", new FinPanple(8, 0, 0), null, null, ImtilX.loadImage("org/pandcorps/platform/res/chr/Player.png")));
		final Panteraction interaction = engine.getInteraction();
		interaction.register(this, interaction.KEY_SPACE, new ActionStartListener() {
			@Override public final void onActionStart(final ActionStartEvent event) { jump(); }});
		interaction.register(this, interaction.KEY_SPACE, new ActionEndListener() {
			@Override public final void onActionEnd(final ActionEndEvent event) { releaseJump(); }});
		interaction.register(this, interaction.KEY_RIGHT, new ActionListener() {
			@Override public final void onAction(final ActionEvent event) { right(); }});
		interaction.register(this, interaction.KEY_LEFT, new ActionListener() {
			@Override public final void onAction(final ActionEvent event) { left(); }});
	}
	
	private final void jump() {
		if (isGrounded()) {
			v = 10;
		}
	}
	
	private final void releaseJump() {
		if (v > 0) {
			v = 0;
		}
	}
	
	private final void right() {
	    setMirror(false);
		addX(VEL_WALK);
	}
	
	private final void left() {
	    setMirror(true);
		addX(-VEL_WALK);
	}
	
	private final void addX(final int v) {
	    final int mult = v > 0 ? 1 : -1;
	    final int n = v * mult;
	    final int offWall = (OFF_X + 1) * mult;
	    for (int i = 0; i < n; i++) {
	        if (isWall(offWall)) {
	            break;
	        }
	        getPosition().addX(mult);
	    }
	}

	@Override
	public final void onStep(final StepEvent event) {
		final Panple pos = getPosition();
		final int offSol, mult, n;
		if (v > 0) {
			offSol = OFF_BUTTING;
			mult = 1;
		} else {
			offSol = OFF_GROUNDED;
			mult = -1;
		}
		n = v * mult;
		for (int i = 0; i < n; i++) {
		    final Tile t = getSolid(offSol);
			if (t != null) {
			    if (v > 0) {
			        Tiles.bump(this, t);
			    }
				v = 0;
				break;
			}
			pos.addY(mult);
		}
		if (!isGrounded()) {
			v += g;
		}
		/*
		Issues with slopes:
		
		If Player walks into a slope, it should raise him.
		If he jumps onto a slope, it should stop him at the right spot.
		If he jumps from a slope, it should work (realize he's grounded).
		If he walks down a slope, he should be able to jump while walking
		(not move horizontally faster than falling vertically, breaking the grounding).
		If he walks to the end of the slope, he should walk onto the flat ground beyond it.
		
		Maybe smarter horizontal movement is key.
		We do it one pixel at a time anyway.
		Maybe we should allow collisions at the bottom pixel and raise by one pixel at time of h-move.
		Previous attempts ignored the slope during h-move and corrected afterward.
		Each pixel of h-move could also check for a slope one pixel below and lower by one pixel at that time.
		*/
		/*if (v <= 0) {
		    final Tile t = PlatformGame.tm.getContainer(pos);
		    if (t != null) {
		        final byte b = t.getBehavior();
		        if (b == PlatformGame.TILE_UP) {
		            // trunc/round should match getContainer
		            final int minHeight = (int) pos.getX() % ImtilX.DIM;
		            final int iy = (int) pos.getY();
		            final int curHeight = iy % ImtilX.DIM;
		            if (curHeight < minHeight) {
		                pos.setY((iy / ImtilX.DIM) * ImtilX.DIM + minHeight);
		            }
		        }
		    }
		}*/
	}
	
	private boolean isGrounded() {
		// v == 0 so that jumping through floor doesn't cause big jump
		return v == 0 && isSolid(OFF_GROUNDED);
	}
	
	/*protected boolean isButting() {
		return isSolid(OFF_BUTTING);
	}*/
	
	private boolean isSolid(final int off) {
	    return getSolid(off) != null;
	}
	
	private Tile getSolid(final int off) {
		final Panple pos = getPosition();
		final float x = pos.getX(), y = pos.getY() + off, x1, x2;
		if (isMirror()) {
		    x1 = x - OFF_X;
            x2 = x + OFF_X + 1;
		} else {
		    x1 = x - OFF_X - 1;
		    x2 = x + OFF_X;
		}
		// Interesting glitch if breakpoint here
		Tile t1 = PlatformGame.tm.getContainer(x1, y), t2 = PlatformGame.tm.getContainer(x2, y);
		if (t2 == PlatformGame.tm.getContainer(x, y)) {
		    final Tile t = t1;
		    t1 = t2;
		    t2 = t;
		}
		collide(t1);
		collide(t2);
		final boolean floor = off < 0 && (Math.round(y) % ImtilX.DIM == 15);
		if (isSolid(t1, floor)) {
		    return t1;
		} else if (isSolid(t2, floor)) {
		    return t2;
		}
		return null;
	}
	
	private boolean isWall(final int off) {
        final Panple pos = getPosition();
        final float x = pos.getX() + off, y = pos.getY();
        //TODO for (i = 0; i += 16; ...) if h > 16
        final Tile t1 = PlatformGame.tm.getContainer(x, y), t2 = PlatformGame.tm.getContainer(x, y + H - 1);
        collide(t1);
        collide(t2);
        return isSolid(t1) || isSolid(t2);
    }
	
	private boolean isSolid(final Tile tile) {
		return isSolid(tile, false);
	}
	
	private boolean isSolid(final Tile tile, final boolean floor) {
		if (tile == null) {
			return false;
		} else if (tile.isSolid()) {
			return true;
		}
		final byte b = tile.getBehavior();
		return b == PlatformGame.TILE_BREAK || b == PlatformGame.TILE_BUMP || (floor && b == PlatformGame.TILE_FLOOR);
	}
	
	private void collide(final Tile tile) {
		final TileOccupant o = Tile.getOccupant(tile);
		if (o == null) {
			return;
		}
		((Gem) o).onCollide(this);
	}
}
