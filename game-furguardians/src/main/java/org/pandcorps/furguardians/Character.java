/*
Copyright (c) 2009-2021, Andrew M. Martin
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

import java.util.*;
import java.util.Map;

import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandax.tile.*;

public abstract class Character extends GuyPlatform {
    protected final static int VEL_DESTROY_HELD = Player.VEL_BUMP;
    private final static Map<Integer, TubeHandler> tubeDownLefts = new HashMap<Integer, TubeHandler>();
    private final static Map<Integer, TubeHandler> tubeDownRights = new HashMap<Integer, TubeHandler>();
    protected Player holder = null;
    
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
	
	//@OverrideMe
    protected void onBump(final Character c) {
    }
	
	@Override
	protected final void onBump(final int t) {
	    Tiles.bump(this, t);
	}
	
	//@OverrideMe
	protected boolean isHoldable() {
	    return false;
	}
	
	//@OverrideMe
    protected void onStepEndHeld() {
    }
	
	//@OverrideMe
    protected boolean isShieldWhenHeld() {
        return false;
    }
    
    //@OverrideMe
    protected void destroyWhenHeld() {
        flipAndFall(VEL_DESTROY_HELD);
    }
	
	//@OverrideMe
    protected void onRelease() {
    }
    
    protected void onKickUpward() {
        v = Player.VEL_KICKED_UPWARD;
        initHorizontalVelocityOnKickUpward(holder);
    }
    
    protected final void initHorizontalVelocityOnKickUpward(final Player holder) {
        hv = holder.hv;
        chv = holder.chv;
    }
    
    // Can be called by sub-classes during onWallTile to break the tile if appropriate
    protected final byte onWallTileBump(final Player player, final int tileIndex) {
        return Tiles.bump(player, tileIndex);
    }
	
	@Override
	protected final TileMap getTileMap() {
        return Level.tm;
    }
	
	@Override
	protected final boolean isSolidBehavior(final byte b) {
        return b == FurGuardiansGame.TILE_BREAK || b == FurGuardiansGame.TILE_BUMP;
    }
	
	@Override
    protected final boolean isFloorBehavior(final byte b) {
        return b == FurGuardiansGame.TILE_FLOOR;
    }
	
	protected final static void clearTubes() {
	    tubeDownLefts.clear();
	    tubeDownRights.clear();
	}
	
	protected final static void addTubeDown(final int x, final int y, final TubeHandler dst) {
	    final TileMap tm = Level.tm;
	    tubeDownLefts.put(Integer.valueOf(tm.getIndexRequired(x, y)), dst);
	    tubeDownRights.put(Integer.valueOf(tm.getIndexRequired(x + 1, y)), dst);
	}
	
	protected final void checkTubeDown(final Panmage image) {
	    final int neighbor = getNeighborTileIndex(Direction.South);
        boolean tube = false;
        TubeHandler dst;
        if ((dst = getTubeDownLeftSide(neighbor)) != null) {
            moveFromLeftSideToCenter();
            tube = true;
        } else if ((dst = getTubeDownRightSide(neighbor)) != null) {
            moveFromRightSideToCenter();
            tube = true;
        }
        if (tube) {
            new Tuber(this, dst, image, 0, -1);
        }
	}
	
	protected final TubeHandler getTubeDownLeftSide(final int index) {
	    return tubeDownLefts.get(Integer.valueOf(index));
	}
	
	protected final TubeHandler getTubeDownRightSide(final int index) {
	    return tubeDownRights.get(Integer.valueOf(index));
    }
	
	protected final void moveFromLeftSideToCenter() {
	    final Panple pos = getPosition();
	    pos.addX(16 - (Math.round(pos.getX()) % 16));
	}
	
	protected final void moveFromRightSideToCenter() {
	    final Panple pos = getPosition();
        pos.addX(-(Math.round(pos.getX()) % 16));
    }
	
	protected final static class Tuber extends Panctor implements StepListener {
	    private final Panctor src;
	    private final TubeHandler dst;
	    protected final int xDir, yDir;
	    private int timer = 32;
	    
	    private Tuber(final Panctor src, final TubeHandler dst, final int xDir, final int yDir) {
	        this.src = src;
	        this.dst = dst;
	        this.xDir = xDir;
	        this.yDir = yDir;
	        src.getLayer().addActor(this);
	        src.detach();
	        final Panple srcPos = src.getPosition();
	        FurGuardiansGame.setPosition(this, srcPos.getX(), srcPos.getY(), FurGuardiansGame.DEPTH_BETWEEN);
            setMirror(xDir < 0);
            //TODO Sound
	    }
	    
	    protected Tuber(final Panctor src, final TubeHandler dst, final Panmage view, final int xDir, final int yDir) {
	        this(src, dst, xDir, yDir);
	        setView(view);
	    }
	    
	    protected Tuber(final Panctor src, final TubeHandler dst, final Panimation view, final int xDir, final int yDir) {
            this(src, dst, xDir, yDir);
            setView(view);
        }

        @Override
        public final void onStep(final StepEvent event) {
            if (timer <= 0) {
                finish();
                return;
            }
            getPosition().add(xDir, yDir);
            timer--;
        }
        
        private final void finish() {
            dst.onTubeEntered(this);
        }
	}
	
	protected static interface TubeHandler {
	    public void onTubeEntered(final Tuber tuber);
	}
	
	protected final static class TempTubeHandler implements TubeHandler {
	    @Override
	    public final void onTubeEntered(final Tuber tuber) {
	        tuber.getLayer().addActor(tuber.src);
	        tuber.destroy();
	    }
	}
}
