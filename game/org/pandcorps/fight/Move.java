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
package org.pandcorps.fight;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;

public final class Move {
    /*package*/ final static int LOOP_INF = 0; // Loop until OOB
    ///*package*/ final static int LOOP_HIT = -1; // Loop until hit someone (or OOB); No, this flag applies for infite or finite
    
    /*package*/ final static byte ANIM_NORMAL = 0;
    /*package*/ final static byte ANIM_FLIP = 1;
    /*package*/ final static byte ANIM_MIRROR = 2;
    /*package*/ final static byte ANIM_DIAGONAL = 3;
    /*package*/ final static byte ANIM_RISE = 4;
    
    /*package*/ final int loop; // number of loops (or OOB)
    /*package*/ final boolean stopAfterHit;
    /*package*/ final Panimation anim;
    /*package*/ final MoveFrame[] mframes;
    
    /*public Move(final Panimation anim) {
        this.anim = anim;
    }*/
    
    public Move(final String id, final int loop, final boolean stopAfterHit, final MoveFrame... mframes) {
        this.loop = loop;
        this.stopAfterHit = stopAfterHit;
        this.mframes = mframes;
        final int size = mframes.length;
        final Panframe[] pframes = new Panframe[size];
        for (int i = 0; i < size; i++) {
            pframes[i] = mframes[i].pframe;
        }
        this.anim = Pangine.getEngine().createAnimation(id, pframes);
    }
    
    /*package*/ final static byte parseAnim(final String val) {
        if ("flip".equalsIgnoreCase(val)) {
            return ANIM_FLIP;
        } else if ("mirror".equalsIgnoreCase(val)) {
            return ANIM_MIRROR;
        } else if (Chartil.startsWithIgnoreCase(val, "diag")) {
            return ANIM_DIAGONAL;
        } else if ("rise".equalsIgnoreCase(val)) {
            return ANIM_RISE;
        } else if ("normal".equalsIgnoreCase(val) || Chartil.isEmpty(val)) {
        	return ANIM_NORMAL;
        }
        throw new IllegalArgumentException(val);
    }
}
