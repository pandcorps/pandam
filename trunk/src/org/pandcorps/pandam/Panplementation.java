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
package org.pandcorps.pandam;

import org.pandcorps.pandam.event.*;

// Pandam Implementation
public abstract class Panplementation {
	protected final Panctor actor;
	private Panview view = null;
	/*package*/ int currFrame;
	/*package*/ int currFrameDur;
	protected int currRot = 0;
	protected boolean currMirror = false; // Might be able to implement Panframe flipping by using Panplementation.setMirror,
	protected boolean currFlip = false; // but Panctor.isFlipped() should reflect Panctor's state, independent of current Panframe
	protected Panple currOrigin = null;
	private long lastUpdateView;

	protected Panplementation(final Panctor actor) {
		this.actor = actor;
		initAnim();
	}
	
	private final void initAnim() {
	    currFrame = 0;
	    currFrameDur = 0;
	    lastUpdateView = -1;
	}

	/*package*/ final Panview getView() {
		return view;
	}

	/*package*/ final void setView(final Panview view) {
		this.view = view;

		if (view instanceof Panmage) {
		    currRot = 0;
		    currMirror = false;
		    currFlip = false;
		    currOrigin = null;
			updateView((Panmage) view);
		} else if (view != null) {
		    /*
		     * Might want to be able to disable this.
		     * If switching from a walking animation to a walking/shooting animation,
		     * probably want to keep the same frame/dur.
		     * Otherwise the legs would twitch when shooting starts.
		     * By default we should init the frame/dur.
		     * We don't want to start in the middle of an animation.
		     */
		    initAnim();
			setFrame(((Panimation) view).getFrames()[0]);
		}
	}
	
	private final void setFrame(final Panframe frame) {
	    updateView(frame.getImage());
	    currRot = frame.getRot();
        currMirror = frame.isMirror();
        currFlip = frame.isFlip();
        currOrigin = frame.getOrigin();
	}
	
	/*package*/ final boolean changeView(final Panview view) {
        if (this.view == view) {
            return false;
        }
        setView(view);
        return true;
    }

	protected abstract Panple getPosition();

	protected abstract boolean isVisible();

	protected abstract void setVisible(boolean vis);
	
	protected abstract int getRot();
	
	protected abstract void setRot(int rot);
	
	protected abstract boolean isMirror();

    protected abstract void setMirror(boolean mirror);
    
    protected abstract boolean isFlip();

    protected abstract void setFlip(boolean flip);

	/*package*/ final void updateView() {
		if (view == null || view instanceof Panmage) {
			return;
		}
		final Pangine engine = Pangine.getEngine();
		final long clock = engine.getClock();
		if (clock == lastUpdateView) {
			/*
			Actors might want to synchronize with animation.
			If they know they're done changing the view,
			they might want to force an update before the engine
			automatically does so.
			If there's a manual update, we want to skip the next automatic update.
			*/
			return;
		}
		lastUpdateView = clock;
		final Panframe[] frames = ((Panimation) view).getFrames();

		currFrameDur++;
		if (currFrameDur == frames[currFrame].getDuration()) {
			currFrameDur = 0;
			currFrame++;
			if (currFrame == frames.length) {
				currFrame = 0;
				if (actor instanceof AnimationEndListener) {
					//((AnimationEndListener) actor).onAnimationEnd(AnimationEndEvent.INSTANCE);
					engine.animationEndListeners.add((AnimationEndListener) actor);
				}
			}
			setFrame(frames[currFrame]);
		}
	}

	protected abstract void updateView(Panmage image);

	/*package*/ final Pansplay getCurrentDisplay() {
		// Might want to store the current Pansplay in a separate field
		return view instanceof Panimation ? (((Panimation) view).getFrames())[currFrame].getImage() : (Panmage) view;
	}

	protected abstract void renderView();
	
	protected final void render(final Panmage image, final float x, final float y, final float z, final int rot, final boolean mirror, final boolean flip, final Panple o) {
	    image.render(actor.getLayer(), x, y, z, rot, mirror, flip, o);
	}
}
