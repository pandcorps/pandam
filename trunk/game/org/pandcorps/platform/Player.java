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
import org.pandcorps.pandam.impl.ImplPanple;
import org.pandcorps.pandax.tile.*;

public class Player extends Panctor implements StepListener {
    private final static int H = 15;
	private final static int OFF_GROUNDED = -1;
	private final static int OFF_BUTTING = H + 1;
	private final static int OFF_X = 7;
	private final static int VEL_WALK = 3;
	private final static int VEL_RETURN = 2;
	private final static int MAX_V = 10;
	private final static byte MODE_NORMAL = 0;
	private final static byte MODE_RETURN = 1;
	private final static byte JUMP_HIGH = 1;
	//private final static byte JUMP_DOUBLE = 2;
	//private final static byte JUMP_INFINITE = 3;
	private final static byte JUMP_FLY = 4;
	
	// Player attributes preserved between levels
	public final static class PlayerContext {
	    private int gems = 0;
	    
	    public final int getGems() {
	        return gems;
	    }
	}
	
	protected static float g = -0.65f;
	protected final PlayerContext pc;
	private byte mode = MODE_NORMAL;
	private byte jumpMode = MODE_NORMAL;
	private boolean flying = false;
	private float v = 0;
	private final Panple safe = new ImplPanple(0, 0, 0);
	private int levelGems = 0;
	
	public Player(final PlayerContext pc) {
	    this.pc = pc;
		final Pangine engine = Pangine.getEngine();
		setView(PlatformGame.guy);
		final Panteraction interaction = engine.getInteraction();
		interaction.register(this, interaction.KEY_SPACE, new ActionStartListener() {
			@Override public final void onActionStart(final ActionStartEvent event) { jump(); }});
		interaction.register(this, interaction.KEY_SPACE, new ActionEndListener() {
			@Override public final void onActionEnd(final ActionEndEvent event) { releaseJump(); }});
		interaction.register(this, interaction.KEY_RIGHT, new ActionListener() {
			@Override public final void onAction(final ActionEvent event) { right(); }});
		interaction.register(this, interaction.KEY_LEFT, new ActionListener() {
			@Override public final void onAction(final ActionEvent event) { left(); }});
		
		// Debug
		interaction.register(this, interaction.KEY_1, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { left(); }});
		interaction.register(this, interaction.KEY_2, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { right(); }});
		interaction.register(this, interaction.KEY_9, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { addX(-1); }});
        interaction.register(this, interaction.KEY_0, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { addX(1); }});
        interaction.register(this, interaction.KEY_Q, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { jumpMode = MODE_NORMAL; }});
        interaction.register(this, interaction.KEY_W, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { jumpMode = JUMP_HIGH; }});
        interaction.register(this, interaction.KEY_E, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { jumpMode = JUMP_FLY; }});
	}
	
	private final void jump() {
	    if (jumpMode == JUMP_FLY) {
	        flying = true;
	        addV(-g);
	        return;
	    }
		if (isGrounded()) {
			v = jumpMode == JUMP_HIGH ? MAX_V : 8;
		}
	}
	
	private final void releaseJump() {
	    if (jumpMode == JUMP_FLY) {
            flying = false;
            return;
        }
		if (v > 0) {
			v = 0;
		}
	}
	
	private final void right() {
		addX(VEL_WALK);
	}
	
	private final void left() {
		addX(-VEL_WALK);
	}
	
	private final void addX(final int v) {
		if (mode == MODE_RETURN) {
			return;
		}
	    setMirror(v < 0);
	    final int mult = v > 0 ? 1 : -1;
	    final int n = v * mult;
	    final int offWall = (OFF_X + 1) * mult;
	    final Panple pos = getPosition();
	    for (int i = 0; i < n; i++) {
	    	boolean down = true;
	        if (isWall(offWall, 0)) {
	        	if (isWall(offWall, 1)) {
	        		break;
	        	}
	            pos.addY(1);
	            down = false;
	        }
	        if (down && !isWall(offWall, -1) && isWall(offWall, -2)) {
	        	pos.addY(-1);
	        }
	        pos.addX(mult);
	    }
	}
	
	private final void addV(final float a) {
	    v += a;
	    if (a > 0 && v > MAX_V) {
	        v = MAX_V;
	    } else if (v < -MAX_V) {
	        v = -MAX_V;
	    }
	}

	@Override
	public final void onStep(final StepEvent event) {
		final Panple pos = getPosition();
		if (mode == MODE_RETURN) {
			final Panple diff = Panple.subtract(safe, pos);
			final double dist = diff.getMagnitude();
			if (dist <= VEL_RETURN) {
				pos.set(safe);
				mode = MODE_NORMAL;
				return;
			}
			diff.multiply((float) (VEL_RETURN / dist));
			pos.add(diff);
			return;
		}
		
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
		    final Tile t = getSolid(offSol);
			if (t != null) {
			    if (v > 0) {
			        Tiles.bump(this, t);
			    }
				v = 0;
				break;
			}
			pos.addY(mult);
			final float y = pos.getY();
			if (y < 0) {
			    pos.setY(0);
				v = 0;
				if (jumpMode != JUMP_FLY) {
    				onHurt();
    				mode = MODE_RETURN;
				}
				return;
			} else {
			    final float max = PlatformGame.room.getSize().getY() - H;
			    if (y >= max) {
    			    pos.setY(max - 1);
    			    v = 0;
    			    return;
			    }
			}
		}
		if (flying) {
		    if (jumpMode != JUMP_FLY) {
		        flying = false;
		    } else {
		        addV(-g);
		    }
		}
		if (isGrounded()) {
			safe.set(pos);
		} else if (!flying) {
			addV(g);
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
	
	private final int getOffLeft() {
		return isMirror() ? -OFF_X : (-OFF_X - 1);
	}
	
	private final int getOffRight() {
		return isMirror() ? (OFF_X + 1) : OFF_X;
	}
	
	private Tile getSolid(final int off) {
		final Panple pos = getPosition();
		final float x = pos.getX(), y = pos.getY() + off, x1 = x + getOffLeft(), x2 = x + getOffRight();
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
		if (isSolid(t1, floor, x1, x2, y)) {
		    return t1;
		} else if (isSolid(t2, floor, x1, x2, y)) {
		    return t2;
		}
		return null;
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
        //TODO for (i = 0; i += 16; ...) if h > 16
        //final float x = yoff == 0 ? f : b;
        final Tile t1 = PlatformGame.tm.getContainer(f, y), t2 = PlatformGame.tm.getContainer(f, top);
        collide(t1);
        collide(t2);
        if (isSolid(t1, left, right, y) || isSolid(t2, left, right, y) /*|| isSolid(t2, left, right, top)*/) {
        	return true;
        } else if (yoff < 0) {
        	final Tile t3 = PlatformGame.tm.getContainer(b, y), t4 = PlatformGame.tm.getContainer(b, top);
        	return isSlope(t3, left, right, y) || isSlope(t4, left, right, y);
        }
        return false;
    }
	
	private boolean isSlope(final Tile tile, final float left, final float right, final float y) {
		// isSolid will check for non-slopes, could cut straight to slope logic
		if (tile == null) {
		    return false;
		}
		final int b = tile.getBehavior();
		return (b == PlatformGame.TILE_UPSLOPE || b == PlatformGame.TILE_DOWNSLOPE || b == PlatformGame.TILE_UPSLOPE_FLOOR || b == PlatformGame.TILE_DOWNSLOPE_FLOOR) && isSolid(tile, left, right, y);
	}
	
	private boolean isSolid(final Tile tile, final float left, final float right, final float y) {
		return isSolid(tile, false, left, right, y);
	}
	
	private boolean isSolid(final Tile tile, final boolean floor, final float left, final float right, final float y) {
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
			if (map.getContainer(right, y) != tile) {
				if (b == PlatformGame.TILE_UPSLOPE_FLOOR && curHeight != 15) {
					return false;
				}
				return map.getContainer(left, y) == tile || (b != PlatformGame.TILE_UPSLOPE_FLOOR && (map.getContainer(left, top) == tile || map.getContainer(right, top) == tile));
			}
            final int minHeight = (int) right % ImtilX.DIM;
            return (b == PlatformGame.TILE_UPSLOPE_FLOOR) ? (curHeight == minHeight) : (curHeight <= minHeight);
			//}
		} else if (b == PlatformGame.TILE_DOWNSLOPE || (yoff <= 0 && b == PlatformGame.TILE_DOWNSLOPE_FLOOR)) {
            if (map.getContainer(left, y) != tile) {
            	if (b == PlatformGame.TILE_DOWNSLOPE_FLOOR && curHeight != 15) {
					return false;
				}
                return map.getContainer(right, y) == tile || (b != PlatformGame.TILE_DOWNSLOPE_FLOOR && (map.getContainer(right, top) == tile || map.getContainer(left, top) == tile));
            }
            final int minHeight = 15 - ((int) left % ImtilX.DIM);
            return (b == PlatformGame.TILE_DOWNSLOPE_FLOOR) ? (curHeight == minHeight) : (curHeight <= minHeight);
        }
		return false;
	}
	
	private void collide(final Tile tile) {
		final TileOccupant o = Tile.getOccupant(tile);
		if (o == null) {
			return;
		}
		((Gem) o).onCollide(this);
	}
	
	public final int getCurrentLevelGems() {
        return levelGems;
    }
	
	public final void addGem() {
        levelGems++;
    }
	
	public final void onHurt() {
        if (levelGems == 0) {
            return;
        }
        levelGems -= (Math.max(1, levelGems / 10));
    }
	
	public final void onFinishLevel() {
		pc.gems += levelGems;
	}
}
