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
package org.pandcorps.demo.pandam.visual.overlay;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;

public final class SquareGuyActor extends Panctor implements StepListener {
    private int timer = 0;
    private int xdir = 0;
    private int ydir = 0;

    public SquareGuyActor(final String id) {
        super(id);
    }

    @Override
    public final void onStep(final StepEvent event) {
        final Panple pos = getPosition();
        if (timer == 0) {
            timer = 64;
            final float x = pos.getX(), y = pos.getY();
            if (x < 320) {
                if (y < 240) {
                    xdir = 1;
                    ydir = 0;
                } else {
                    xdir = 0;
                    ydir = -1;
                }
            } else {
                if (y < 240) {
                    xdir = 0;
                    ydir = 1;
                } else {
                    xdir = -1;
                    ydir = 0;
                }
            }
        }
        timer--;
        pos.add(xdir, ydir);
    }
}
