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
package org.pandcorps.pandax.touch;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;

public final class SwipeScroller implements SwipeListener {
    private Panlayer layer = null;
    private float minX = 0;
    private float maxX = 0;
    private float minY = 0;
    private float maxY = 0;
    private float oldX = 0;
    private float oldY = 0;
    private float velX = 0;
    private float velY = 0;
    private float acc = 1;
    private long last = -100;
    private ScrollContinuer continuer = null;
    
    @Override
    public final boolean onSwipe(final SwipeEvent event) {
        oldX = event.getOldX();
        oldY = event.getOldY();
        final boolean xChange = add(0, -event.getDiffX(), minX, maxX);
        final boolean yChange = add(1, -event.getDiffY(), minY, maxY);
        final boolean ret = xChange || yChange;
        if (ret) {
            last = Pangine.getEngine().getClock();
        }
        return ret;
    }
    
    @Override
    public final void onSwipeEnd(final SwipeEvent event) {
        if ((acc > 0) || ((Pangine.getEngine().getClock() - last) > 5)) {
            return;
        } else if (continuer == null || continuer.isDestroyed() || (continuer.getLayer() == null)) {
            continuer = new ScrollContinuer();
            layer.addActor(continuer);
        }
        velX = oldX - event.getNewX();
        velY = oldY - event.getNewY();
    }
    
    private final boolean add(final int i, final float off, final float min, final float max) {
        final Panple o = layer.getOrigin();
        final float val = o.getC(i);
        if (off < 0 && val > min) {
            o.setC(i, Math.max(val + off, min));
            return true;
        } else if (off > 0 && val < max) {
            o.setC(i, Math.min(val + off, max));
            return true;
        }
        return false;
    }
    
    public final void setLayer(final Panlayer layer) {
        this.layer = layer;
        final Panple o = layer.getOrigin();
        final float x = o.getX(), y = o.getY();
        setRange(x, x, y, y);
    }
    
    public final void setRange(final float minX, final float maxX, final float minY, final float maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }
    
    public final void setAcceleration(final float acc) {
        this.acc = acc;
    }
    
    private final class ScrollContinuer extends Panctor implements StepListener {
        @Override
        public final void onStep(final StepEvent event) {
            if (add(0, velX, minX, maxX)) {
                if (velX > 0) {
                    velX = Math.max(0, velX + acc);
                } else {
                    velX = Math.min(0, velX - acc);
                }
            } else {
                velX = 0;
            }
            if (add(1, velY, minY, maxY)) {
                if (velY > 0) {
                    velY = Math.max(0, velY + acc);
                } else {
                    velY = Math.min(0, velY - acc);
                }
            } else {
                velY = 0;
            }
        }
    }
}
