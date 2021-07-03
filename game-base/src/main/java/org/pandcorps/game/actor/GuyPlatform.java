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
package org.pandcorps.game.actor;

import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.tile.*;

public abstract class GuyPlatform extends Panctor implements StepListener, Collidable {
    public final static int MAX_V = 10;
    public final static int MIN_Y = -12;
    public final static float g = -0.65f;
    public final static float gFlying = -0.38f;
    protected final static byte X_NORMAL = 0;
    protected final static byte X_END = 1;
    protected final static byte X_START = 2;
    protected final static byte X_WALL = 3;
    protected final static byte Y_NORMAL = 0;
    protected final static byte Y_BUMP = 1;
    protected final static byte Y_LANDED = 2;
    protected final static byte Y_CEILING = 3;
    protected final static byte Y_FLOOR = 4;
    protected final static byte Y_FELL = 5;
    protected final static byte Y_WALL = 6;
    public final static int SLOPE_UP = -1;
    public final static int SLOPE_NONE = 0;
    public final static int SLOPE_DOWN = 1;
    public static byte TILE_UPSLOPE = -1;
    public static byte TILE_DOWNSLOPE = -1;
    public static byte TILE_UPSLOPE_FLOOR = -1;
    public static byte TILE_DOWNSLOPE_FLOOR = -1;
    public static byte TILE_ICE = -1;
    public static byte TILE_SAND = -1;
    protected static boolean sandSolid = true;
    public int H;
    public final int OFF_GROUNDED = -1;
    public int OFF_BUTTING;
    public int OFF_X;
    public float v = 0;
    public int hv = 0;
    public float chv = 0;
    
    protected GuyPlatform(final int offX, final int h) {
        setOffX(offX);
        setH(h);
    }
    
    protected final void setOffX(final int offX) {
        OFF_X = offX;
    }
    
    protected final void setH(final int h) {
        H = h;
        OFF_BUTTING = H + 1;
    }
    
    public final static FinPanple2 getMin(final int offX) {
        return new FinPanple2(-offX - 1, 0);
    }
    
    public final static FinPanple2 getMax(final int offX, final int h) {
        return new FinPanple2(offX, h);
    }
    
    protected void setMirror(final int v) {
        if ((v != 0) && isMirrorable()) {
            setMirror(v < 0);
        }
    }
    
    protected boolean isMirrorable() {
        return true;
    }
    
    protected final byte addY() {
        final byte yStatus = addY(v);
        if ((v > 0) && ((yStatus == Y_BUMP) || (yStatus == Y_CEILING))) {
            v = 0;
        } else if ((v < 0) && ((yStatus == Y_LANDED) || (yStatus == Y_FLOOR) || (yStatus == Y_FELL))) {
            // onLanded could change a negative v to positive for bouncing; only set to 0 if that didn't happen
            v = 0;
        }
        return yStatus;
    }
    
    protected final boolean isJumpPossible() {
        return getSolid(1, false) == -1;
    }
    
    protected final byte addY(final float v) {
        final int offSol, mult, n;
        if (v > 0) {
            offSol = OFF_BUTTING;
            mult = 1;
        } else {
            offSol = OFF_GROUNDED;
            mult = -1;
        }
        n = Math.round(v * mult);
        final Panple pos = getPosition();
        if (n == 0) {
            getSolid(0); // Calls onCollide
        }
        for (int i = 0; i < n; i++) {
            final int t = getSolid(offSol);
            if (t != -1) {
                if (v > 0) {
                    onBump(t);
                    return Y_BUMP;
                } else {
                    onLanded();
                    return Y_LANDED;
                }
            }
            pos.addY(mult);
            final float y = pos.getY();
            if (y < MIN_Y) {
                pos.setY(MIN_Y);
                if (v < 0) { // This check helps with room transitions; might not be needed by games without room transitions
                    if (onFell()) {
                        return Y_FELL;
                    }
                    return Y_FLOOR;
                }
            } else {
                final float max = getCeiling();
                if (y >= max) {
                    pos.setY(max - 1);
                    if (v > 0) { // This check helps with room transitions; might not be needed by games without room transitions
                        onCeiling();
                        return Y_CEILING;
                    }
                }
            }
        }
        return Y_NORMAL;
    }
    
    protected final byte addX(final int v) {
        return addX(v, false);
    }
    
    protected final byte addX(final int v, final boolean adjustY) {
        return addX(v, adjustY, true);
    }
    
    protected final byte addX(final int v, final boolean adjustY, final boolean adjustMirror) {
        if (v == 0) {
            if (adjustMirror) {
                setMirror(hv);
            }
            return X_NORMAL; // No movement, but request was successful
        } else if (adjustMirror) {
            setMirror((hv == 0) ? v : hv);
        }
        final int mult = (v > 0) ? 1 : -1;
        final Panple pos = getPosition();
        final int n = v * mult;
        final int offWall = (OFF_X + 1) * mult;
        for (int i = 0; i < n; i++) {
            if (onHorizontal(mult)) {
                return X_NORMAL; // onHorizontal ran successfully
            }
            boolean down = true;
            if (isWall(offWall, 0)) {
                if (isWall(offWall, 1, true)) {
                    return X_WALL;
                } else if (adjustY) {
                    pos.addY(1);
                }
                down = false;
            }
            if (down && !isWall(offWall, -1) && isWall(offWall, -2)) {
                if (adjustY) {
                    pos.addY(-1);
                }
            }
            pos.addX(mult);
        }
        if (v > 0) {
            final Panlayer layer = getLayer();
            if (layer != null) {
                final float end = layer.getSize().getX() - 1;
                if (pos.getX() > end) {
                    pos.setX(end);
                    onEnd();
                    return X_END;
                }
            }
        } else {
            if (pos.getX() < 0) {
                pos.setX(0);
                onStart();
                return X_START;
            }
        }
        return X_NORMAL;
    }
    
    protected final byte moveTo(final int x, final int y) {
        final Panple pos = getPosition();
        final int cx = Math.round(pos.getX()), cy = Math.round(pos.getY());
        final int diffX = x - cx, diffY = y - cy;
        final int magX = Math.abs(diffX), magY = Math.abs(diffY);
        final byte xStatus, yStatus;
        v = diffY;
        if (magX > magY) {
            xStatus = addX(diffX);
            yStatus = addY(diffY);
        } else {
            yStatus = addY(diffY);
            xStatus = addX(diffX);
        }
        if (yStatus != Y_NORMAL) {
            return yStatus;
        } else if (xStatus != X_NORMAL) {
            return Y_WALL;
        } else {
            return Y_NORMAL;
        }
    }
    
    protected final void addV(final float a) {
        v += a;
        if (a > 0 && v > MAX_V) {
            v = MAX_V;
        } else {
            final float minV = getMinV();
            if (v < minV) {
                v = minV;
            }
        }
    }
    
    protected float getMinV() {
        return -MAX_V;
    }
    
    protected int initCurrentHorizontalVelocity() {
        chv = hv;
        return hv;
    }
    
    protected final int initCurrentHorizontalVelocitySand() {
        final int thv = (hv == 0) ? 0 : (hv / Math.abs(hv));
        chv = thv;
        return thv;
    }
    
    protected final int initCurrentHorizontalVelocityIce() {
        return initCurrentHorizontalVelocitySlide(0.125f);
    }
    
    protected final int initCurrentHorizontalVelocitySlide(final float friction) {
        final float dif = hv - chv;
        if (dif > 0) {
            chv += friction;
        } else if (dif < 0) {
            chv -= friction;
        }
        return Math.round(chv);
    }
    
    protected final int initCurrentHorizontalVelocityAccelerating() {
        if (hv > 0) {
            if (chv <= 0) {
                chv = 1;
            } else {
                chv = (chv < hv) ? (chv + 1) : hv;
            }
        } else {
            if (chv >= 0) {
                chv = -1;
            } else {
                chv = (chv > hv) ? (chv - 1) : hv;
            }
        }
        return Math.round(chv);
    }
    
    @Override
    public final void onStep(final StepEvent event) {
        if (onStepCustom()) {
            onStepEnd();
            return;
        }
        
        // onStepCustom might destroy or detach
        if (getLayer() == null) {
            return;
        }
        
        if (isNearCheckNeeded()) {
            final TileMap tm = getTileMap();
            final Panple pos = getPosition();
            final float x = pos.getX() + getOffLeft(), y = pos.getY();
            for (int i = -1; i < 3; i++) {
                final float xn = x + (16 * i);
                for (int j = -1; j < 3; j++) {
                    onNear(tm.getContainer(xn, y + (16 * j)));
                }
            }
        }
        if (addY() == Y_FELL) {
            return;
        }
        
        final byte xResult = addX(initCurrentHorizontalVelocity(), true);
        if (xResult != X_NORMAL) {
            onWall(xResult);
            chv = 0;
        }
        
        onStepping();
        if (isGrounded()) {
            onGrounded();
        } else {
            if (!onAir()) {
                addV(getG());
            }
        }
        
        checkScrolled();
        
        onStepEnd();
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
        Panlayer layer = getLayer();
        if (layer == null) {
            layer = Pangame.getGame().getCurrentRoom();
        }
        final Panple size = (layer == null) ? null : layer.getSize();
        if (size == null) {
            return Float.MAX_VALUE;
        }
        return size.getY() + 4 - H;
    }
    
    public boolean isGrounded() {
        // v == 0 so that jumping through floor doesn't cause big jump
        return v == 0 && isSolid(OFF_GROUNDED);
    }
    
    // Should only be called when isGrounded
    public int getCurrentSlope() {
        final Panple pos = getPosition();
        final float x = pos.getX(), y = pos.getY();
        final int slope = getSlope(x, y);
        if (slope != SLOPE_NONE) {
            return slope;
        }
        return getSlope(x, y - 8);
    }
    
    private final int getSlope(final float x, final float y) {
        final TileMap tm = getTileMap();
        final byte b = Tile.getBehavior(tm.getTile(tm.getContainer(x, y)));
        if (isAnyUpslope(b)) {
            return SLOPE_UP;
        } else if (isAnyDownslope(b)) {
            return SLOPE_DOWN;
        }
        return SLOPE_NONE;
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
        return getSolid(off, true);
    }
    
    protected int getSolid(final int off, final boolean eventTriggeringAllowed) {
        final TileMap tm = getTileMap();
        final Panple pos = getPosition();
        final float x = pos.getX(), y = pos.getY() + off, x1 = x + getOffLeft(), x2 = x + getOffRight();
        // Interesting glitch if breakpoint here
        int t1 = tm.getContainer(x1, y), t2 = tm.getContainer(x2, y);
        if (t2 == tm.getContainer(x, y)) {
            final int t = t1;
            t1 = t2;
            t2 = t;
        }
        if (eventTriggeringAllowed) {
            onCollide(t1);
            onCollide(t2);
        }
        final boolean floor = off < 0 && (Math.round(y) % ImtilX.DIM == 15);
        if (isSolid(t1, floor, x1, x2, y)) {
            return t1;
        } else if (isSolid(t2, floor, x1, x2, y)) {
            return t2;
        }
        return -1;
    }
    
    protected boolean isWall(final int off, final int yoff) {
        return isWall(off, yoff, false);
    }
    
    protected boolean isWall(final int off, final int yoff, final boolean wallTileEvent) {
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
        int solidIndex = -1;
        int t = -1;
        final TileMap tm = getTileMap();
        for (int i = 0; true; i += 16) {
            float yi = y + i;
            final boolean done = yi >= top;
            if (done) {
                yi = top;
            }
            final int temp = tm.getContainer(f, yi);
            if (temp != t) {
                t = temp;
                onCollide(t);
                sandSolid = false;
                if (!sol && isSolid(t, left, right, y)) {
                    sol = true;
                    solidIndex = t;
                }
                sandSolid = true;
            }
            if (done) {
                break;
            }
        }
        if (sol) {
            if (wallTileEvent) {
                onWallTile(solidIndex);
            }
            return true;
        } else if (yoff < 0) {
            final int t3 = tm.getContainer(b, y), t4 = tm.getContainer(b, top);
            return isSlope(t3, left, right, y) || isSlope(t4, left, right, y);
        }
        return false;
    }
    
    protected boolean isSlope(final int index, final float left, final float right, final float y) {
        // isSolid will check for non-slopes, could cut straight to slope logic
        final Tile tile = getTileMap().getTile(index);
        if (tile == null) {
            return false;
        }
        final byte b = tile.getBehavior();
        return (isAnyUpslope(b) || isAnyDownslope(b)) && isSolid(index, left, right, y);
    }
    
    protected boolean isAnyUpslope(final byte b) {
        return (b == TILE_UPSLOPE) || (b == TILE_UPSLOPE_FLOOR);
    }
    
    protected boolean isAnyDownslope(final byte b) {
        return (b == TILE_DOWNSLOPE) || (b == TILE_DOWNSLOPE_FLOOR);
    }
    
    protected boolean isSolid(final int index, final float left, final float right, final float y) {
        return isSolid(index, false, left, right, y);
    }
    
    protected boolean isSolid(final int index, final boolean floor, final float left, final float right, final float y) {
        final TileMap map = getTileMap();
        final Tile tile = map.getTile(index);
        if (tile == null) {
            return false;
        } else if (tile.isSolid()) {
            return true;
        }
        final byte b = tile.getBehavior();
        if (isSolidBehavior(b) || b == TILE_ICE || (sandSolid && b == TILE_SAND) || (floor && isFloorBehavior(b))) {
            return true;
        }
        final float top = y + H - 1, yoff = y - getPosition().getY();
        final int iy = (int) y, curHeight = iy % ImtilX.DIM;
        if (b == TILE_UPSLOPE || (yoff <= 0 && b == TILE_UPSLOPE_FLOOR)) {
            if (map.getContainer(right, y) != index) {
                if (b == TILE_UPSLOPE_FLOOR && curHeight != 15) {
                    return false;
                } else if (map.getContainer(left, y) == index) {
                    final int i = map.getColumn(index), j = map.getRow(index);
                    return b != TILE_UPSLOPE_FLOOR || Tile.getBehavior(map.getTile(map.getRelative(i, j, 1, 1))) != TILE_UPSLOPE_FLOOR;
                } else if (b == TILE_UPSLOPE_FLOOR) {
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
            return (b == TILE_UPSLOPE_FLOOR) ? (curHeight == minHeight) : (curHeight <= minHeight);
        } else if (b == TILE_DOWNSLOPE || (yoff <= 0 && b == TILE_DOWNSLOPE_FLOOR)) {
            if (map.getContainer(left, y) != index) {
                if (b == TILE_DOWNSLOPE_FLOOR && curHeight != 15) {
                    return false;
                } else if (map.getContainer(right, y) == index) {
                    final int i = map.getColumn(index), j = map.getRow(index);
                    return b != TILE_DOWNSLOPE_FLOOR || Tile.getBehavior(map.getTile(map.getRelative(i, j, -1, 1))) != TILE_DOWNSLOPE_FLOOR;
                } else if (b == TILE_DOWNSLOPE_FLOOR) {
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
            return (b == TILE_DOWNSLOPE_FLOOR) ? (curHeight == minHeight) : (curHeight <= minHeight);
        }
        return false;
    }
    
    public final int getContainerTileIndex() {
        return getTileMap().getContainer(this);
    }
    
    public final int getNeighborTileIndex(final Direction dir) {
        final int container = getContainerTileIndex();
        final TileMap tm = getTileMap();
        if (tm.isBad(container)) {
            return -1;
        }
        return tm.getNeighbor(container, dir);
    }
    
    //@OverrideMe
    protected boolean onStepCustom() {
        return false;
    }
    
    //@OverrideMe
    protected void onCollide(final int tile) {
    }
    
    protected final boolean isCollisionStandingOnTile(final int tile) {
        final TileMap tm = getTileMap();
        return (v <= 0) && (getPosition().getY() == ((tm.getRow(tile) + 1) * tm.getTileHeight()));
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
    
    //TODO Rename, keep confusing it with Panctor.onStepEnd(StepEndEvent)
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
    
    protected final boolean onHorizontalEdgeTurn(final int off) {
        if (isOnEdge(off)) {
            hv *= -1;
            return true;
        }
        return false;
    }
    
    protected final boolean isOnEdge(final int off) {
        if (!isGrounded()) { // Don't change direction if already in air
            return false;
        }
        final Panple pos = getPosition();
        final float x = pos.getX(), y = pos.getY();
        pos.addX(off);
        try {
            if (!isGrounded()) {
                pos.addY(-1);
                if (!isGrounded()) {
                    return true;
                }
            }
        } finally {
            pos.set(x, y);
        }
        return false;
    }
    
    //@OverrideMe
    protected boolean onAir() {
        return false;
    }
    
    // This instance bumped into any horizontal barrier (solid tile, far-left, far-right)
    //@OverrideMe
    protected void onWall(final byte xResult) {
    }

    // This instance bumped into a solid tile with the given index
    //@OverrideMe
    protected void onWallTile(final int tileIndex) {
    }
    
    //@OverrideMe
    protected void onStart() {
    }
    
    //@OverrideMe
    protected void onEnd() {
    }
    
    //@OverrideMe
    protected void onCeiling() {
    }
    
    protected abstract boolean onFell();
    
    protected abstract void onBump(final int t);
    
    protected abstract TileMap getTileMap();
    
    protected abstract boolean isSolidBehavior(final byte b);
    
    protected abstract boolean isFloorBehavior(final byte b);
    
    public final static void registerCapture(final Panctor actor) {
        final Pangine engine = Pangine.getEngine();
        final Panteraction interaction = engine.getInteraction();
        actor.register(interaction.KEY_F1, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { engine.captureScreen(); }});
        actor.register(interaction.KEY_F2, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { engine.startCaptureFrames(); }});
        actor.register(interaction.KEY_F3, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { engine.stopCaptureFrames(); }});
    }
}
