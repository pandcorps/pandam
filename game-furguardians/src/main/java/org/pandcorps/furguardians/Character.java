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
	
	protected boolean isSlope(final int index, final float left, final float right, final float y) {
		// isSolid will check for non-slopes, could cut straight to slope logic
	    final Tile tile = Level.tm.getTile(index);
		if (tile == null) {
		    return false;
		}
		final int b = tile.getBehavior();
		return (b == FurGuardiansGame.TILE_UPSLOPE || b == FurGuardiansGame.TILE_DOWNSLOPE || b == FurGuardiansGame.TILE_UPSLOPE_FLOOR || b == FurGuardiansGame.TILE_DOWNSLOPE_FLOOR) && isSolid(index, left, right, y);
	}
	
	protected boolean isSolid(final int index, final float left, final float right, final float y) {
		return isSolid(index, false, left, right, y);
	}
	
	protected boolean isSolid(final int index, final boolean floor, final float left, final float right, final float y) {
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
}
