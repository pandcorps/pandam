/*
Copyright (c) 2009-2014, Andrew M. Martin
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

import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandax.tile.*;

public abstract class Character extends Panctor implements StepListener, Collidable {
	protected final static int MAX_V = 10;
	protected final static int MIN_Y = -12;
	protected static float g = -0.65f;
	protected final int H;
	private final int OFF_GROUNDED = -1;
	private final int OFF_BUTTING;
	private final int OFF_X;
	protected float v = 0;
	protected int hv = 0;
	
	protected Character(final int offX, final int h) {
		OFF_X = offX;
		H = h;
		OFF_BUTTING = H + 1;
	}
	
	protected final boolean addX(final int v) {
		if (v == 0) {
			return true; // No movement, but request was successful
		}
	    setMirror(v < 0);
	    final int mult;
	    final Panple pos = getPosition();
	    if (v > 0) {
	        mult = 1;
	        if (pos.getX() > PlatformGame.room.getSize().getX()) {
	            return false;
	        }
	    } else {
	        mult = -1;
	        if (pos.getX() <= 0) {
	            return false;
	        }
	    }
	    final int n = v * mult;
	    final int offWall = (OFF_X + 1) * mult;
	    for (int i = 0; i < n; i++) {
	    	if (onHorizontal(mult)) {
	    		return true; // onHorizontal ran successfully
	    	}
	    	boolean down = true;
	        if (isWall(offWall, 0)) {
	        	if (isWall(offWall, 1)) {
	        		return false;
	        	}
	            pos.addY(1);
	            down = false;
	        }
	        if (down && !isWall(offWall, -1) && isWall(offWall, -2)) {
	        	pos.addY(-1);
	        }
	        pos.addX(mult);
	    }
	    return true;
	}
	
	protected final void addV(final float a) {
	    v += a;
	    if (a > 0 && v > MAX_V) {
	        v = MAX_V;
	    } else if (v < -MAX_V) {
	        v = -MAX_V;
	    }
	}
	
	@Override
	public final void onStep(final StepEvent event) {
		if (onStepCustom()) {
			onStepEnd();
			return;
		}
		
		final Panple pos = getPosition();
		final int offSol, mult, n;
		if (v > 0) {
			offSol = OFF_BUTTING;
			mult = 1;
		} else {
			offSol = OFF_GROUNDED;
			mult = -1;
		}
		n = Math.round(v * mult);
		for (int i = 0; i < n; i++) {
		    final int t = getSolid(offSol);
			if (t != -1) {
			    if (v > 0) {
			        Tiles.bump(this, t);
			        v = 0;
			    } else {
			        onLanded();
			    }
				break;
			}
			pos.addY(mult);
			final float y = pos.getY();
			if (y < MIN_Y) {
			    pos.setY(MIN_Y);
				v = 0;
				if (onFell()) {
					return;
				}
				break;
			} else {
			    final float max = getCeiling();
			    if (y >= max) {
    			    pos.setY(max - 1);
    			    v = 0;
    			    break;
			    }
			}
		}
		
		if (!addX(hv)) {
			onWall();
		}
		
		onStepping();
		if (isGrounded()) {
			onGrounded();
		} else {
			if (!onAir()) {
				addV(g);
			}
		}
		
		checkScrolled();
		
		onStepEnd();
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
	
	protected final void checkScrolled() {
		if (!isInView()) {
			onScrolled();
		}
	}
	
	protected final float getCeiling() {
		return PlatformGame.room.getSize().getY() + 4 - H;
	}
	
	protected boolean isGrounded() {
		// v == 0 so that jumping through floor doesn't cause big jump
		return v == 0 && isSolid(OFF_GROUNDED);
	}
	
	/*protected boolean isButting() {
		return isSolid(OFF_BUTTING);
	}*/
	
	private boolean isSolid(final int off) {
	    return getSolid(off) != -1;
	}
	
	private final int getOffLeft() {
		return isMirror() ? -OFF_X : (-OFF_X - 1);
	}
	
	private final int getOffRight() {
		return isMirror() ? (OFF_X + 1) : OFF_X;
	}
	
	private int getSolid(final int off) {
		final Panple pos = getPosition();
		final float x = pos.getX(), y = pos.getY() + off, x1 = x + getOffLeft(), x2 = x + getOffRight();
		// Interesting glitch if breakpoint here
		int t1 = Level.tm.getContainer(x1, y), t2 = Level.tm.getContainer(x2, y);
		if (t2 == Level.tm.getContainer(x, y)) {
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
	
	private boolean isWall(final int off, final int yoff) {
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
		        if (!sol && isSolid(t, left, right, y)) {
		        	sol = true;
		        }
		        
		        /*if (!sol && yoff < 0) {
		        	final Tile tb = PlatformGame.tm.getContainer(b, yi);
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
		return (b == PlatformGame.TILE_UPSLOPE || b == PlatformGame.TILE_DOWNSLOPE || b == PlatformGame.TILE_UPSLOPE_FLOOR || b == PlatformGame.TILE_DOWNSLOPE_FLOOR) && isSolid(index, left, right, y);
	}
	
	private boolean isSolid(final int index, final float left, final float right, final float y) {
		return isSolid(index, false, left, right, y);
	}
	
	private boolean isSolid(final int index, final boolean floor, final float left, final float right, final float y) {
	    final Tile tile = Level.tm.getTile(index);
		if (tile == null) {
			return false;
		} else if (tile.isSolid()) {
			return true;
		}
		final byte b = tile.getBehavior();
		if (b == PlatformGame.TILE_BREAK || b == PlatformGame.TILE_BUMP || (floor && b == PlatformGame.TILE_FLOOR)) {
			return true;
		}
		final float top = y + H - 1, yoff = y - getPosition().getY();
		final TileMap map = tile.getMap();
		final int iy = (int) y, curHeight = iy % ImtilX.DIM;
		if (b == PlatformGame.TILE_UPSLOPE || (yoff <= 0 && b == PlatformGame.TILE_UPSLOPE_FLOOR)) {
			//if (v <= 0) {
			//final Panple pos = getPosition();
            // trunc/round should match getContainer
			//if (right > tile.getPosition().getX() + tile.getMap().getTileWidth()) {
			//	return false;
			//}
			if (map.getContainer(right, y) != index) {
				if (b == PlatformGame.TILE_UPSLOPE_FLOOR && curHeight != 15) {
					return false;
				} else if (map.getContainer(left, y) == index) {
				    final int i = Level.tm.getColumn(index), j = Level.tm.getRow(index);
					return b != PlatformGame.TILE_UPSLOPE_FLOOR || Tile.getBehavior(Level.tm.getTile(Level.tm.getRelative(i, j, 1, 1))) != PlatformGame.TILE_UPSLOPE_FLOOR;
				} else if (b == PlatformGame.TILE_UPSLOPE_FLOOR) {
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
            return (b == PlatformGame.TILE_UPSLOPE_FLOOR) ? (curHeight == minHeight) : (curHeight <= minHeight);
			//}
		} else if (b == PlatformGame.TILE_DOWNSLOPE || (yoff <= 0 && b == PlatformGame.TILE_DOWNSLOPE_FLOOR)) {
            if (map.getContainer(left, y) != index) {
            	if (b == PlatformGame.TILE_DOWNSLOPE_FLOOR && curHeight != 15) {
					return false;
				} else if (map.getContainer(right, y) == index) {
				    final int i = Level.tm.getColumn(index), j = Level.tm.getRow(index);
					return b != PlatformGame.TILE_DOWNSLOPE_FLOOR || Tile.getBehavior(Level.tm.getTile(Level.tm.getRelative(i, j, -1, 1))) != PlatformGame.TILE_DOWNSLOPE_FLOOR;
				} else if (b == PlatformGame.TILE_DOWNSLOPE_FLOOR) {
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
            return (b == PlatformGame.TILE_DOWNSLOPE_FLOOR) ? (curHeight == minHeight) : (curHeight <= minHeight);
        }
		return false;
	}
	
	//@OverrideMe
	protected boolean onStepCustom() {
		return false;
	}
	
	//@OverrideMe
	protected void onCollide(final int tile) {
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
	protected void onBump(final Character c) {
	}
	
	protected abstract boolean onFell();
}
