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
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.tile.*;

public abstract class Character extends GuyPlatform {
	protected Character(final int offX, final int h) {
		super(offX, h);
	}
	
	protected final void checkScrolled() {
		if (!isInView()) {
			onScrolled();
		}
	}
	
	protected float getG() {
		return g;
	}
	
	protected final float getCeiling() {
		return FurGuardiansGame.room.getSize().getY() + 4 - H;
	}
	
	protected boolean isGrounded() {
		// v == 0 so that jumping through floor doesn't cause big jump
		return v == 0 && isSolid(OFF_GROUNDED);
	}
	
	private boolean isSolid(final int off) {
	    return getSolid(off) != -1;
	}
	
	protected final int getOffLeft() {
		return isMirror() ? -OFF_X : (-OFF_X - 1);
	}
	
	protected final int getOffRight() {
		return isMirror() ? (OFF_X + 1) : OFF_X;
	}
	
	protected int getSolid(final int off) {
		final TileMap tm = Level.tm;
		final Panple pos = getPosition();
		final float x = pos.getX(), y = pos.getY() + off, x1 = x + getOffLeft(), x2 = x + getOffRight();
		// Interesting glitch if breakpoint here
		int t1 = tm.getContainer(x1, y), t2 = tm.getContainer(x2, y);
		if (t2 == tm.getContainer(x, y)) {
		    final int t = t1;
		    t1 = t2;
		    t2 = t;
		}
		onCollide(t1);
		onCollide(t2);
		final boolean floor = off < 0 && (Math.round(y) % ImtilX.DIM == 15);
		if (isSolid(t1, floor, x1, x2, y)) {
		    return t1;
		} else if (isSolid(t2, floor, x1, x2, y)) {
		    return t2;
		}
		return -1;
	}
	
	protected boolean isWall(final int off, final int yoff) {
        final Panple pos = getPosition();
        final float px = pos.getX(), f = px + off, y = pos.getY() + yoff;
        final float left, right, b, top = y + H - 1;
        if (off > 0) {
        	right = f;
        	left = px - OFF_X;
        	b = left;
        } else {
        	left = f;
        	right = px + OFF_X;
        	b = right;
        }
        boolean sol = false;
        int t = -1;
        for (int i = 0; true; i += 16) {
        	float yi = y + i;
        	final boolean done = yi >= top;
        	if (done) {
        		yi = top;
        	}
	        final int temp = Level.tm.getContainer(f, yi);
	        if (temp != t) {
	        	t = temp;
		        onCollide(t);
		        sandSolid = false;
		        if (!sol && isSolid(t, left, right, y)) {
		        	sol = true;
		        }
		        sandSolid = true;
		        
		        /*if (!sol && yoff < 0) {
		        	final Tile tb = FurGuardiansGame.tm.getContainer(b, yi);
		        	sol = isSlope(tb, left, right, y);
		        }*/
	        }
	        if (done) {
	        	break;
	        }
        }
        if (sol) {
        	return true;
        } else if (yoff < 0) {
        	final int t3 = Level.tm.getContainer(b, y), t4 = Level.tm.getContainer(b, top);
        	return isSlope(t3, left, right, y) || isSlope(t4, left, right, y);
        }
        return false;
    }
	
	private boolean isSlope(final int index, final float left, final float right, final float y) {
		// isSolid will check for non-slopes, could cut straight to slope logic
	    final Tile tile = Level.tm.getTile(index);
		if (tile == null) {
		    return false;
		}
		final int b = tile.getBehavior();
		return (b == FurGuardiansGame.TILE_UPSLOPE || b == FurGuardiansGame.TILE_DOWNSLOPE || b == FurGuardiansGame.TILE_UPSLOPE_FLOOR || b == FurGuardiansGame.TILE_DOWNSLOPE_FLOOR) && isSolid(index, left, right, y);
	}
	
	private boolean isSolid(final int index, final float left, final float right, final float y) {
		return isSolid(index, false, left, right, y);
	}
	
	private static boolean sandSolid = true;
	
	private boolean isSolid(final int index, final boolean floor, final float left, final float right, final float y) {
	    final TileMap map = Level.tm;
	    final Tile tile = map.getTile(index);
		if (tile == null) {
			return false;
		} else if (tile.isSolid()) {
			return true;
		}
		final byte b = tile.getBehavior();
		if (b == FurGuardiansGame.TILE_BREAK || b == FurGuardiansGame.TILE_BUMP ||
				b == FurGuardiansGame.TILE_ICE || (sandSolid && b == FurGuardiansGame.TILE_SAND) ||
				(floor && b == FurGuardiansGame.TILE_FLOOR)) {
			return true;
		}
		final float top = y + H - 1, yoff = y - getPosition().getY();
		final int iy = (int) y, curHeight = iy % ImtilX.DIM;
		if (b == FurGuardiansGame.TILE_UPSLOPE || (yoff <= 0 && b == FurGuardiansGame.TILE_UPSLOPE_FLOOR)) {
			//if (v <= 0) {
			//final Panple pos = getPosition();
            // trunc/round should match getContainer
			//if (right > tile.getPosition().getX() + tile.getMap().getTileWidth()) {
			//	return false;
			//}
			if (map.getContainer(right, y) != index) {
				if (b == FurGuardiansGame.TILE_UPSLOPE_FLOOR && curHeight != 15) {
					return false;
				} else if (map.getContainer(left, y) == index) {
				    final int i = map.getColumn(index), j = map.getRow(index);
					return b != FurGuardiansGame.TILE_UPSLOPE_FLOOR || Tile.getBehavior(map.getTile(map.getRelative(i, j, 1, 1))) != FurGuardiansGame.TILE_UPSLOPE_FLOOR;
				} else if (b == FurGuardiansGame.TILE_UPSLOPE_FLOOR) {
					return false;
				}
				for (int i = 0; true; i += 16) {
					final float t = top - i;
					if (t <= y) {
						return false;
					} else if (map.getContainer(left, t) == index || map.getContainer(right, t) == index) {
						return true;
					}
				}
			}
            final int minHeight = (int) right % ImtilX.DIM;
            return (b == FurGuardiansGame.TILE_UPSLOPE_FLOOR) ? (curHeight == minHeight) : (curHeight <= minHeight);
			//}
		} else if (b == FurGuardiansGame.TILE_DOWNSLOPE || (yoff <= 0 && b == FurGuardiansGame.TILE_DOWNSLOPE_FLOOR)) {
            if (map.getContainer(left, y) != index) {
            	if (b == FurGuardiansGame.TILE_DOWNSLOPE_FLOOR && curHeight != 15) {
					return false;
				} else if (map.getContainer(right, y) == index) {
				    final int i = map.getColumn(index), j = map.getRow(index);
					return b != FurGuardiansGame.TILE_DOWNSLOPE_FLOOR || Tile.getBehavior(map.getTile(map.getRelative(i, j, -1, 1))) != FurGuardiansGame.TILE_DOWNSLOPE_FLOOR;
				} else if (b == FurGuardiansGame.TILE_DOWNSLOPE_FLOOR) {
					return false;
				}
            	for (int i = 0; true; i += 16) {
					final float t = top - i;
					if (t <= y) {
						return false;
					} else if (map.getContainer(right, t) == index || map.getContainer(left, t) == index) {
						return true;
					}
				}
            }
            final int minHeight = 15 - ((int) left % ImtilX.DIM);
            return (b == FurGuardiansGame.TILE_DOWNSLOPE_FLOOR) ? (curHeight == minHeight) : (curHeight <= minHeight);
        }
		return false;
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
	
	//@OverrideMe
	protected boolean onStepCustom() {
		return false;
	}
	
	//@OverrideMe
	protected void onCollide(final int tile) {
	}
	
	//@OverrideMe
	protected boolean isNearCheckNeeded() {
		return false;
	}
	
	//@OverrideMe
	protected void onNear(final int tile) {
	}
	
	//@OverrideMe
	protected void onStepping() {
	}
	
	//@OverrideMe
	protected void onScrolled() {
	}
	
	//@OverrideMe
	protected void onStepEnd() {
	}
	
	//@OverrideMe
	protected void onGrounded() {
	}
	
	protected void onBump(final int t) {
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
}
