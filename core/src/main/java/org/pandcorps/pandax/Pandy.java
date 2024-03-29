/*
Copyright (c) 2009-2021, Andrew M. Martin
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
package org.pandcorps.pandax;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.*;

// Pandam Body
public class Pandy extends Panctor implements StepListener, SpecVelocity {
	private final Panple vel = new ImplPanple();
	private final Panple acc;

	public Pandy() {
	    this(new ImplPanple());
	}
	
	// Gravity will likely be a constant, so allow same Panple to be shared
	public Pandy(final Panple acc) {
	    this.acc = acc;
    }
	
	public Pandy(final String id) {
		super(id);
		acc = new ImplPanple();
	}

	@Override
	public void onStep(final StepEvent event) {
		getPosition().add(vel);
		vel.add(acc);
	}

	@Override
	public final Panple getVelocity() {
		return vel;
	}

	public final Panple getAcceleration() {
		return acc;
	}
	
	public final static class Mover extends Panctor implements StepListener, RoomAddListener {
	    private final Panple vel = new ImplPanple();
	    private final Panctor subject;
	    
	    public Mover(final Panctor subject) {
	        this.subject = subject;
	    }
	    
	    @Override
        public final void onRoomAdd(final RoomAddEvent event) {
            if (subject.getLayer() == null) {
                getLayer().addActor(subject);
            }
        }
	    
	    @Override
	    public void onStep(final StepEvent event) {
	        final Panlayer layer = getLayer();
	        if (layer == null) {
	            return;
	        }
	        subject.getPosition().add(vel);
	        final float vx = vel.getX(), vy = vel.getY();
	        final Panple smin = subject.getBoundingMinimum(), smax = subject.getBoundingMaximum();
	        final Panple vmin = layer.getViewMinimum(), vmax = layer.getViewMaximum();
	        if ((vx < 0) && (smin.getX() < vmin.getX())) {
	            vel.setX(-vx);
	        } else if ((vx > 0) && (smax.getX() > vmax.getX())) {
	            vel.setX(-vx);
	        }
	        if ((vy < 0) && (smin.getY() < vmin.getY())) {
                vel.setY(-vy);
            } else if ((vy > 0) && (smax.getY() > vmax.getY())) {
                vel.setY(-vy);
            }
	    }
	    
	    @Override
	    public final void onDetach() {
	        subject.detach();
	    }
	    
	    public final Panple getVelocity() {
	        return vel;
	    }
	}
}
