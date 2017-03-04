/*
Copyright (c) 2009-2017, Andrew M. Martin
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
    public final int OFF_X;
    public float v = 0;
    public int hv = 0;
    public float chv = 0;
    
    protected GuyPlatform(final int offX, final int h) {
        OFF_X = offX;
        setH(h);
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
    
    protected final void setMirror(final int v) {
        if (v != 0) {
            setMirror(v < 0);
        }
    }
    
    protected final byte addY() {
        final byte yStatus = addY(v);
        if (yStatus != Y_NORMAL) {
            v = 0;
        }
        return yStatus;
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
        if (v == 0) {
            setMirror(hv);
            return X_NORMAL; // No movement, but request was successful
        }
        setMirror((hv == 0) ? v : hv);
        final int mult;
        final Panple pos = getPosition();
        if (v > 0) {
            mult = 1;
            if (pos.getX() > getLayer().getSize().getX()) {
                onEnd();
                return X_END;
            }
        } else {
            mult = -1;
            if (pos.getX() <= 0) {
                onStart();
                return X_START;
            }
        }
        final int n = v * mult;
        final int offWall = (OFF_X + 1) * mult;
        for (int i = 0; i < n; i++) {
            if (onHorizontal(mult)) {
                return X_NORMAL; // onHorizontal ran successfully
            }
            boolean down = true;
            if (isWall(offWall, 0)) {
                if (isWall(offWall, 1)) {
                    return X_WALL;
                }
                pos.addY(1);
                down = false;
            }
            if (down && !isWall(offWall, -1) && isWall(offWall, -2)) {
                pos.addY(-1);
            }
            pos.addX(mult);
        }
        return X_NORMAL;
    }
    
    protected final byte moveTo(final int x, final int y) {
        final Panple pos = getPosition();
        final int cx = Math.round(pos.getX()), cy = Math.round(pos.getY());
        final int diffX = x - cx, diffY = y - cy;
        final int magX = Math.abs(diffX), magY = Math.abs(diffY);
        final byte xStatus, yStatus;
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
        } else if (v < -MAX_V) {
            v = -MAX_V;
        }
    }
    
    protected int initCurrentHorizontalVelocity() {
        chv = hv;
        return hv;
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
        
        final byte xResult = addX(initCurrentHorizontalVelocity());
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
                }
                sandSolid = true;
            }
            if (done) {
                break;
            }
        }
        if (sol) {
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
        final int b = tile.getBehavior();
        return (b == TILE_UPSLOPE || b == TILE_DOWNSLOPE || b == TILE_UPSLOPE_FLOOR || b == TILE_DOWNSLOPE_FLOOR) && isSolid(index, left, right, y);
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
    protected void onWall(final byte xResult) {
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
}
