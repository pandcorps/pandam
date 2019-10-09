/*
Copyright (c) 2009-2018, Andrew M. Martin
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
package org.pandcorps.pandax.text;

import org.pandcorps.core.*;
import org.pandcorps.core.chr.*;
import org.pandcorps.pandam.event.*;

public class TextTyper extends Pantext implements StepListener {
    private final SubSequence seq;
    private int min = 0;
    private int time = 1;
    private boolean loop = false;
    private Runnable finishHandler = null;
    private int timer = time;
    
    public TextTyper(final Font font, final CharSequence msg) {
        super(Pantil.vmid(), font, new SubSequence(msg, 0));
        seq = (SubSequence) text.get(0);
    }
    
    public final TextTyper setMin(final int min) {
        this.min = min;
        if (seq.length() < min) {
            seq.setEnd(min);
        }
        return this;
    }
    
    public final TextTyper setTime(final int time) {
        this.time = time;
        timer = time;
        return this;
    }
    
    public final TextTyper setLoop(final boolean loop) {
        this.loop = loop;
        return this;
    }
    
    public final TextTyper setFinishHandler(final Runnable finishHandler) {
        this.finishHandler = finishHandler;
        return this;
    }
    
    public final TextTyper setEnd(final int end) {
        seq.setEnd(end);
        return this;
    }
    
    @Override
    public final void centerX() {
        final int old = seq.length();
        try {
            seq.setEnd(seq.getMax());
            super.centerX();
        } finally {
            seq.setEnd(old);
        }
    }

    @Override
    public final void onStep(final StepEvent event) {
        timer--;
        if (timer > 0) {
            return;
        }
        timer = time;
        if (!seq.increment()) {
            if (loop) {
                seq.setEnd(min);
            }
            if (finishHandler != null) {
                finishHandler.run();
            }
        }
    }
}
