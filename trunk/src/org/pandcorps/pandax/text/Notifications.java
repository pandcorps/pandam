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
package org.pandcorps.pandax.text;

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandax.visual.*;

public class Notifications extends Panctor implements StepListener {
    protected final Pantext label;
    protected final StringBuilder seq;
    private Queue<String> queue = new LinkedList<String>();
    private ArrayList<Runnable> freeListeners = null;
    private int timer = 0;
    
    private Notifications(final Panlayer layer, final Pantext label) {
        layer.addActor(label);
        this.label = label;
        seq = (StringBuilder) label.text.get(0);
        label.setVisible(false);
        setVisible(false);
        layer.addActor(this);
    }
    
    public Notifications(final Panlayer layer, final Font font) {
        this(layer, new Pantext(Pantil.vmid(), font, new StringBuilder()));
    }
    
    public Notifications(final Panlayer layer, final MultiFont fonts) {
        this(layer, new Pantext(Pantil.vmid(), fonts, new StringBuilder()));
    }
    
    public void enqueue(final String msg) {
        if (!label.isVisible()) {
            init(msg);
            label.setVisible(true);
        } else {
            queue.offer(msg);
        }
    }
    
    public boolean isBusy() {
        return label.isVisible();
    }
    
    public void addFreeListener(final Runnable r) {
        freeListeners = Coltil.add(freeListeners, r);
        if (!isBusy()) {
            free();
        }
    }
    
    public final void fadeOut(final Panlayer layer, final short r, final short g, final short b, final short speed, final Panscreen nextScreen) {
        addFreeListener(new Runnable() {
            @Override public final void run() {
                FadeController.fadeOut(layer, r, g, b, speed, nextScreen);
            }});
    }

    @Override
    public void onStep(final StepEvent event) {
        if (timer <= 0) {
            free();
            return;
        }
        timer--;
        if (timer <= 0) {
            if (queue.isEmpty()) {
                label.setVisible(false);
                Chartil.clear(seq);
                free();
            } else {
                init(queue.poll());
            }
        }
    }
    
    private final void init(final String msg) {
        Chartil.set(seq, msg);
        timer = 90;
    }
    
    private final void free() {
        for (final Runnable r : Coltil.unnull(freeListeners)) {
            r.run();
        }
        Coltil.clear(freeListeners);
    }
    
    public final Pantext getLabel() {
        return label;
    }
    
    public final static void fadeOut(final Notifications q, final Panlayer layer, final short r, final short g, final short b, final short speed, final Panscreen nextScreen) {
        if (q == null) {
            FadeController.fadeOut(layer, r, g, b, speed, nextScreen);
        } else {
            q.fadeOut(layer, r, g, b, speed, nextScreen);
        }
    }
}
