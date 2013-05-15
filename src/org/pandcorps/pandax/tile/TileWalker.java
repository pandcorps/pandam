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
package org.pandcorps.pandax.tile;

import org.pandcorps.pandam.Panple;
import org.pandcorps.pandam.event.StepEvent;
import org.pandcorps.pandam.event.StepListener;

public class TileWalker extends TileOccupant implements StepListener {

    private float dx = 0;
    
    private float dy = 0;
    
    private Direction dir = null;
    
    /*package*/ float speed = 1;
    
    public TileWalker() {
    	super();
    }
    
    public TileWalker(final String id) {
        super(id);
    }
    
    protected final Tile getDestination(final Direction dir) {
        final Tile dst = tile.getNeighbor(dir);
        return (dst == null || dst.isSolid() || dst.occupant != null) ? null : dst;
    }
    
    // Would it ever be useful to call this
    ///*package*/ final boolean canWalk(final Direction dir) {
    //    return getDestination(dir) != null;
    //}
    
    protected final void face(final Direction dir) {
        onFace(this.dir, dir); // Face new direction whether or not we can walk that way
        this.dir = dir;
    }
    
    protected final boolean walk(final Direction dir) {
    	return walk(dir, null);
    }
    
    protected final boolean walk(final Direction dir, final Direction dir2) {
        Tile dst = dir2 == null ? getDestination(dir) : tile.getNeighbor(dir);
        face(dir);
        if (dst == null) {
            return false;
        } else {
        	final Tile prev = tile;
            setTile(dst);
            if (dir2 != null) {
            	dst = getDestination(dir2);
            	if (dst == null) {
            		setTile(prev);
            		return false;
            	} else {
            		setTile(dst);
            	}
            }
            //TODO Update Z?
            final Panple pos = getPosition();
            final Panple dstPos = dst.position;
            //pos.set(dstPos.getX(), dstPos.getY(), pos.getZ());
            dx = dstPos.getX() - pos.getX();
            dy = dstPos.getY() - pos.getY();
            onWalk();
            return true;
        }
    }
    
    @Override
    public final void onStep(final StepEvent event) {
        if (dx != 0 || dy != 0) {
            final Panple pos = getPosition();
            final float cx = getStep(dx), cy = getStep(dy);
            dx -= cx;
            dy -= cy;
            pos.add(cx, cy);
            setZ();
            onWalking();
            if (dx == 0 && dy == 0) {
                onWalked();
            }
        } else {
            onStill();
        }
    }
    
    //@OverrideMe
    protected void onFace(final Direction oldDir, final Direction newDir) {
    }
    
    //@OverrideMe
    protected void onWalk() {
    }
    
    //@OverrideMe
    protected void onWalking() {
    }
    
    //@OverrideMe
    protected void onWalked() {
    }
    
    //@OverrideMe
    protected void onStill() {
    }
    
    private final float getStep(final float d) {
        return d > 0 ? speed : d < 0 ? -speed : 0;
    }
    
    public final Direction getDirection() {
        return dir;
    }
    
    public TileOccupant getFacing() {
        return getNeighbor(getDirection());
    }
    
    public final void setSpeed(final float speed) {
        this.speed = speed;
    }
}
