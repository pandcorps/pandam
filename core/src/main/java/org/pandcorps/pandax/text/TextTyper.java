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

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.chr.*;
import org.pandcorps.pandam.event.*;

public class TextTyper extends Pantext implements StepListener {
    private List<SubSequence> lines;
    private int min = 0;
    private int time = 1;
    private int pageTime = 60;
    private boolean loop = false;
    private Runnable finishHandler = null;
    private int timer = time;
    private boolean newPageNeeded = false;
    private int lineIndex = 0;
    
    public TextTyper(final Font font, final CharSequence msg) {
        super(Pantil.vmid(), font, msg);
        setEnd(0);
    }
    
    public final TextTyper setMin(final int min) {
        this.min = min;
        if (length() < min) {
            setEnd(min);
        }
        return this;
    }
    
    public final TextTyper setTime(final int time) {
        this.time = time;
        timer = time;
        return this;
    }
    
    public final TextTyper setPageTime(final int pageTime) {
        this.pageTime = pageTime;
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
    
    public final TextTyper setTimer(final int timer) {
        this.timer = timer;
        return this;
    }
    
    public final TextTyper setEnd(int end) {
        for (final SubSequence seq : lines) {
            final int max = seq.getMax();
            if (end < max) {
                seq.setEnd(end);
                end = 0;
            } else {
                seq.setEnd(max);
                end -= max;
            }
        }
        return this;
    }
    
    public final int length() {
        int size = 0;
        for (final SubSequence seq : lines) {
            size += Chartil.size(seq);
        }
        return size;
    }
    
    @Override
    public final void centerX() {
        if (Coltil.isEmpty(lines)) {
            return;
        }
        final SubSequence seq = lines.get(0);
        final int old = seq.length();
        try {
            seq.setEnd(seq.getMax());
            super.centerX();
        } finally {
            seq.setEnd(old);
        }
    }
    
    @Override
    protected final List<? extends CharSequence> wrapTokens(final List<? extends CharSequence> tokens) {
        final int size = Coltil.size(tokens);
        lines = new ArrayList<SubSequence>(size);
        for (final CharSequence token : tokens) {
            lines.add(new SubSequence(token));
        }
        return lines;
    }

    @Override
    public final void onStep(final StepEvent event) {
        if (Coltil.isEmpty(lines)) {
            return;
        }
        timer--;
        if (timer > 0) {
            return;
        }
        timer = time;
        if (newPageNeeded) {
            firstLine = lineIndex;
            newPageNeeded = false;
            return;
        }
        SubSequence seq = lines.get(lineIndex);
        if (!seq.increment()) {
            if (lineIndex < (lines.size() - 1)) {
                lineIndex++;
                if ((lineIndex - firstLine) >= linesPerPage) {
                    timer = pageTime;
                    newPageNeeded = true;
                }
                return;
            }
            if (loop) {
                setEnd(min);
            }
            if (finishHandler != null) {
                finishHandler.run();
            }
        }
    }
}
