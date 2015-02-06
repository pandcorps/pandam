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
package org.pandcorps.fight;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.FinPanple;

public final class MoveFrame {
    /*package*/ final Panframe pframe;
    /*
    Probably only want to move along x-axis; y & z are synchronized.
    Might want to make move that looks like a jump,
    but that would really be movement in the plane.
    Wouldn't usually want movement in the plane.
    If we want vertical movement in something like a rising kick,
    we should probably modify Panframe to allow frame-specific modifications
    of the Panmage origin.
    That would also be good for lobbing projectiles.
    Could treat y-value in velocity as origin change instead of position change.
    */
    /*package*/ final FinPanple velocity;
    /*package*/ final Panmage trail;
    /*package*/ final Emitter[] emitters;
    
    /*package*/ MoveFrame(final Panframe pframe, final FinPanple velocity, final Emitter... emitters) {
        this(pframe, velocity, null, emitters);
    }
    
    public MoveFrame(final Panframe pframe, final FinPanple velocity, final Panmage trail, final Emitter... emitters) {
        this.pframe = pframe;
        this.velocity = velocity == null ? FinPanple.ORIGIN : velocity;
        this.trail = trail;
        this.emitters = emitters;
    }
}
