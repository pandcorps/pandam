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
package org.pandcorps.game;

import org.pandcorps.core.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;

public abstract class Guy2Game extends BaseGame {
	@Override
    public final void step() {
        Guy2.step();
    }
	
	protected final static Img[] loadConstantImgs() {
	    return ImtilX.loadStrip("org/pandcorps/game/res/misc/Constants.png");
	}
	
	protected final static Panimation createBloodAnm(final Img[] constantImgs, final int dur) {
	    return createAnm("blood", constantImgs, dur, 6, 7);
	}
	
	protected final static Panimation createExplosionAnm(final Img[] constantImgs, final int dur) {
        return createAnm("explosion", constantImgs, dur, 3, 4, 5);
    }
	
	protected final static Panimation createPuffAnm(final Img[] constantImgs, final int dur) {
	    return createAnm("puff", constantImgs, dur, 8, 9, 10);
	}
	
	protected final static Panimation createBamAnm(final Img[] constantImgs, final int dur) {
	    return createAnm("bam", constantImgs, dur, 1, 2);
	}
	
	protected final static Panimation createAnm(final String name, final Img[] constantImgs, final int dur, final int... is) {
	    final Pangine engine = Pangine.getEngine();
	    final int size = is.length;
	    final Panframe[] frms = new Panframe[size];
	    for (int i = 0; i < size; i++) {
    	    final Panmage img = engine.createImage("img." + name + "." + i, CENTER_16, null, null, constantImgs[is[i]]);
            frms[i] = engine.createFrame("frm." + name + "." + i, img, dur);
	    }
        return engine.createAnimation("anm." + name, frms);
	}
}
