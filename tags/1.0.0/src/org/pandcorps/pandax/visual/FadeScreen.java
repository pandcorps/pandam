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
package org.pandcorps.pandax.visual;

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;

public abstract class FadeScreen extends TempScreen {
    private final static int SPEED = 4;
    protected final FadeController c = new FadeScreenController();
    private final Panroom room;
    private final Pancolor color;
    private final short oldAlpha;
    private final int time;
    private TimerListener timer = null;
    private Queue<Runnable> tasks = null;
    private Thread bg = null;
    private boolean bgWaiting = true;
    private volatile RuntimeException bgException = null;
    private boolean finished = false;
    private boolean loading = false;
    
    protected FadeScreen(final Pancolor color, final int time) {
        room = Pangame.getGame().getCurrentRoom();
        this.color = room.getBlendColor();
        oldAlpha = this.color.getA();
        this.color.set(color);
        this.time = time;
    }
    
    @Override
    protected final void init() throws Exception {
        room.addActor(c);
        color.setA(Pancolor.MAX_VALUE);
        c.setVelocity((short) -SPEED);
        final Pangine engine = Pangine.getEngine();
        final ActionStartListener anyKey;
        anyKey = new ActionStartListener() { @Override public final void onActionStart(final ActionStartEvent event) {
            if (isBusy() || (c.getVelocity() < 0 && c.getLayer().getBlendColor().getA() > (Pancolor.MAX_VALUE - (SPEED * 2)))) {
                return;
            }
            c.setVelocity((short) SPEED);
            engine.removeTimer(timer);
        }};
        c.register(anyKey);
    }
    
    @Override
    protected final void step() {
    	if (bgWaiting && bg != null) {
        	bg.start();
        	bgWaiting = false;
        }
    	FadeController.run(tasks);
    	if (finished) {
    		fadeFinished();
    	}
    }
    
    public void setTasks(final Queue<Runnable> tasks) {
        this.tasks = tasks;
    }
    
    public void setBackgroundTasks(final Queue<Runnable> tasks) {
    	if (bg != null) {
    		throw new IllegalStateException("Cannot call setBackgroundTasks twice");
    	}
        bg = new Thread(new Runnable() {
            @Override public final void run() {
            	try {
	                for (final Runnable task : tasks) {
	                    task.run();
	                }
            	} catch (final RuntimeException e) {
            		bgException = e;
            		throw e;
            	}
            }});
        bg.setPriority(Math.max(Thread.MIN_PRIORITY, Thread.currentThread().getPriority() - 2));
        bg.setDaemon(true);
    }
    
    private final class FadeScreenController extends FadeController {
        private FadeScreenController() {
            super(Pantil.vmid());
        }
        
        @Override
        protected final void onFadeEnd() {
            if (color.getA() == 0) {
                timer = new TimerListener() { @Override public final void onTimer(final TimerEvent event) {
                    setVelocity((short) SPEED);
                }};
                c.register(time, timer);
            } else {
            	finished = true;
            	fadeFinished();
            }
        }
    }
    
    private final boolean isBusy() {
    	return Coltil.isValued(tasks) || (bg != null && bg.isAlive());
    }
    
    private final void fadeFinished() {
        if (isBusy()) {
            if (!loading) {
                loading = true;
                onLoading();
            }
    		return;
    	} else if (bgException != null) {
    		throw bgException;
    	}
    	color.setA(oldAlpha);
        // Might open a new FadeScreen, so revert alpha first
        finish(c);
        finished = false; // Make sure we don't run this method again
    }
    
    //@OverrideMe
    protected void onLoading() {
    }
}
