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

public class FadeController extends Panctor implements StepListener {
    private short velocity = 0;
    private Queue<Runnable> tasks = null;
    
    public FadeController() {
        setVisible(false);
    }
    
    public FadeController(final String id) {
        super(id);
        setVisible(false);
    }
    
    @Override
    public final void onStep(final StepEvent event) {
        if (velocity == 0 || isDestroyed()) {
            return;
        } else if (Coltil.isValued(tasks)) {
            // Could run multiple tasks until some time threshold is met
            tasks.remove().run();
        }
        final Pancolor color = getLayer().getBlendColor();
        if (!color.addA(velocity)) {
            //TODO Optionally check for remaining tasks before ending
            onFadeEnd();
            velocity = 0;
        }
    }
    
    protected void onFadeEnd() {
    }
    
    public void setVelocity(final short velocity) {
        this.velocity = velocity;
    }
    
    // If using FadeScreen, then use its setTasks, so that it can run tasks during pause between fade-in and fade-out
    public void setTasks(final Queue<Runnable> tasks) {
        this.tasks = tasks;
    }
    
    public final static void fadeIn(final Panlayer layer, final short r, final short g, final short b, final short speed) {
    	clearFadeControllers(layer);
        layer.getBlendColor().set(r, g, b, Pancolor.MAX_VALUE);
        final FadeController c = new FadeController();
        c.setVelocity((short) -speed);
        layer.addActor(c);
    }
    
    public final static void fadeOut(final Panlayer layer, final short r, final short g, final short b, final short speed, final Panscreen nextScreen) {
    	clearFadeControllers(layer);
    	// Will normally already be min; but if it's already partially faded, just use that as starting point
        //layer.getBlendColor().set(r, g, b, Pancolor.MIN_VALUE);
        final FadeController c = new FadeController() {
            @Override protected final void onFadeEnd() {
                Panscreen.set(nextScreen);
            }};
        c.setVelocity(speed);
        layer.addActor(c);
    }
    
    public final static boolean isFadingIn() {
    	final Panroom room = Pangame.getGame().getCurrentRoom();
    	if (room == null) {
    		return false;
    	} else if (isFadingIn(room)) {
    		return true;
    	}
    	Panlayer layer = room;
    	while ((layer = layer.getAbove()) != null) {
    		if (isFadingIn(layer)) {
    			return true;
    		}
    	}
    	layer = room;
    	while ((layer = layer.getBeneath()) != null) {
    		if (isFadingIn(layer)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public final static boolean isFadingIn(final Panlayer layer) {
    	for (final Panctor actor : layer.getActors()) {
    		if (actor instanceof FadeController && ((FadeController) actor).velocity < 0) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private final static void clearFadeControllers(final Panlayer layer) {
    	for (final Panctor actor : layer.getActors()) {
    		if (actor instanceof FadeController) {
    			actor.destroy();
    		}
    	}
    }
}
