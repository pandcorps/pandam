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
    private boolean firstStep = true;
    
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
        }
        run(tasks);
        final Pancolor color = getLayer().getBlendColor();
        if (color.addA(velocity)) {
        	if (firstStep && fadingOut()) {
        		// Clear fade-in controller added during same frame as this (see fadeOut clear comment)
        		clearFadeInControllers(getLayer());
        	}
        	firstStep = false;
        } else {
            //TODO Optionally check for remaining tasks before ending
            onFadeEnd();
            velocity = 0;
        }
    }
    
    protected final static void run(final Queue<Runnable> tasks) {
    	if (Coltil.isEmpty(tasks)) {
    		return;
    	}
        // Could run multiple tasks until some time threshold is met
    	tasks.remove().run();
    }
    
    //@OverrideMe
    protected void onFadeEnd() {
    }
    
    public short getVelocity() {
    	return velocity;
    }
    
    public void setVelocity(final short velocity) {
        this.velocity = velocity;
    }
    
    // If using FadeScreen, then use its setTasks, so that it can run tasks during pause between fade-in and fade-out
    public void setTasks(final Queue<Runnable> tasks) {
        this.tasks = tasks;
    }
    
    public final boolean fadingIn() {
    	return velocity < 0 && !isDestroyed(); // Blend rectangle opacity decreasing
    }
    
    public final boolean fadingOut() {
    	return velocity > 0 && !isDestroyed(); // Blend rectangle opacity increasing
    }
    
    public final boolean fading(final boolean in) {
    	return in ? fadingIn() : fadingOut();
    }
    
    public final static void fadeIn(final Panlayer layer, final short r, final short g, final short b, final short speed) {
    	clearFadeInControllers(layer);
        layer.getBlendColor().set(r, g, b, Pancolor.MAX_VALUE);
        final FadeController c = new FadeController() {
            @Override protected final void onFadeEnd() {
            	destroy();
            }};
        c.setVelocity((short) -speed);
        layer.addActor(c);
    }
    
    public final static void fadeOut(final Panlayer layer, final short r, final short g, final short b, final short speed, final Panscreen nextScreen) {
    	// Won't find controllers added during same frame (still in layer.addedActors instead of actors)
    	clearFadeControllers();
    	// Will normally already be min; but if it's already partially faded, just use that as starting point
        //layer.getBlendColor().set(r, g, b, Pancolor.MIN_VALUE);
    	final Trace state = new Trace("State when starting fadeOut");
        final FadeController c = new FadeController() {
            @Override protected final void onFadeEnd() {
            	try {
            		Panscreen.set(nextScreen);
            	} catch (final RuntimeException e) {
            		Pantil.getRootCause(e).initCause(state);
            		throw e;
            	}
            }};
        c.setVelocity(speed);
        layer.addActor(c);
    }
    
    private final static class FadingIteration implements Iteration<Panlayer> {
    	private final boolean in;
    	private boolean fading = false;
    	
    	private FadingIteration(final boolean in) {
    		this.in = in;
    	}
    	
		@Override
		public final boolean step(final Panlayer elem) {
			fading = isFading(elem, in);
			return !fading;
		}
	}
		
    public final static boolean isFadingIn() {
    	return isFading(true);
    }
    
    public final static boolean isFadingOut() {
    	return isFading(false);
    }
    
    public final static boolean isFading(final boolean in) {
    	final FadingIteration iteration = new FadingIteration(in);
    	Panlayer.iterateLayers(iteration);
    	return iteration.fading;
    }
    
    public final static boolean isFadingIn(final Panlayer layer) {
    	return isFading(layer, true);
    }
    
    public final static boolean isFadingOut(final Panlayer layer) {
    	return isFading(layer, false);
    }
    
    public final static boolean isFading(final Panlayer layer, final boolean in) {
    	for (final Panctor actor : layer.getActors()) {
    		if (actor instanceof FadeController && ((FadeController) actor).fading(in)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private final static void clearFadeControllers() {
        Panlayer.iterateLayers(new Iteration<Panlayer>() {
			@Override public final boolean step(final Panlayer elem) {
				clearFadeControllers(elem);
				return true;
			}});
    }
    
    private final static void clearFadeControllers(final Panlayer layer) {
    	clearFadeControllers(layer, false);
    }
    
    private final static boolean clearFadeInControllers(final Panlayer layer) {
    	return clearFadeControllers(layer, true);
    }
    
    private final static boolean clearFadeControllers(final Panlayer layer, final boolean onlyClearFadeIn) {
    	boolean cleared = false;
    	for (final Panctor actor : layer.getActors()) {
    		if (actor instanceof FadeController) {
    			if (!onlyClearFadeIn || ((FadeController) actor).fadingIn()) {
    				actor.destroy();
    				cleared = true;
    			}
    		}
    	}
    	return cleared;
    }
}
