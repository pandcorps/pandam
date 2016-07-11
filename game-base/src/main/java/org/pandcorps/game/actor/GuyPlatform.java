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
package org.pandcorps.game.actor;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.*;

public abstract class GuyPlatform extends Panctor implements StepListener, Collidable {
    public final static int MAX_V = 10;
    public final static int MIN_Y = -12;
    public final static float g = -0.65f;
    public final static float gFlying = -0.38f;
    public final int H;
    public final int OFF_GROUNDED = -1;
    public final int OFF_BUTTING;
    public final int OFF_X;
    public float v = 0;
    public int hv = 0;
    public float chv = 0;
    
    protected GuyPlatform(final int offX, final int h) {
        OFF_X = offX;
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
}
