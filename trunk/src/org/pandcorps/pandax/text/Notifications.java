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
package org.pandcorps.pandax.text;

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandax.visual.*;

public class Notifications extends Panctor implements StepListener {
    protected final Pantext label;
    protected final StringBuilder seq;
    private LinkedList<Notification> queue = new LinkedList<Notification>();
    private List<Runnable> freeListeners = null;
    private int timer = 0;
    private Panctor icon = null;
    
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
        enqueue(new Notification(msg));
    }
    
    public void enqueue(final Notification n) {
    	if (isDestroyed()) {
    		throw new IllegalStateException("Added \"" + n.msg + "\" to a destroyed Notifications queue");
    	} else if (!label.isVisible()) {
            init(n);
            label.setVisible(true);
        } else {
        	if (n.priority == 0) {
        		queue.offer(n);
        	} else {
        		int i = 0;
        		for (final Notification o : queue) {
        			if (n.priority > o.priority) {
        				queue.add(i, n);
        				break;
        			}
        			i++;
        		}
        	}
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
    
    public final void fadeOut(final Panlayer layer, final short r, final short g, final short b, final short speed, final Panscreen nextScreen, final boolean detach) {
        addFreeListener(new Runnable() {
            @Override public final void run() {
            	if (detach) {
            		detach();
            	}
                FadeController.fadeOut(layer, r, g, b, speed, nextScreen);
            }});
    }
    
    @Override
    public void onStep(final StepEvent event) {
        if (timer <= 0) {
            free();
            return;
        } else if (FadeController.isFadingIn()) {
        	return;
        } else if (label.getLayer() == null) {
        	initLayer().addActor(label);
        }
        timer--;
        if (timer <= 0) {
            Panctor.destroy(icon);
            if (queue.isEmpty()) {
                label.setVisible(false);
                Chartil.clear(seq);
                free();
            } else {
                init(queue.poll());
            }
        }
    }
    
    @Override
    protected void onDetach() {
    	label.detach();
    }
    
    private final void init(final Notification n) {
        Chartil.set(seq, n.msg);
        timer = 90;
        if (n.icon != null) {
            icon = n.icon;
            initLayer().addActor(icon);
        }
    }
    
    private final Panlayer initLayer() {
    	Panlayer layer = getLayer();
    	if (layer == null) {
    		layer = Pangame.getGame().getCurrentRoom().getTop();
    		layer.addActor(this);
    	}
    	return layer;
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
    
    public final static void fadeOut(final Notifications q, final Panlayer layer, final short r, final short g, final short b, final short speed, final Panscreen nextScreen, final boolean detach) {
        if (q == null) {
            FadeController.fadeOut(layer, r, g, b, speed, nextScreen);
        } else {
            q.fadeOut(layer, r, g, b, speed, nextScreen, detach);
        }
    }
    
    public final static class Notification {
        private final String msg;
        private final Panctor icon;
        private final int priority;
        
        public Notification(final String msg) {
            this(msg, null);
        }
        
        public Notification(final String msg, final Panctor icon) {
        	this(msg, icon, 0);
        }
        
        public Notification(final String msg, final Panctor icon, final int priority) {
            this.msg = msg;
            this.icon = icon;
            this.priority = priority;
        }
    }
}
