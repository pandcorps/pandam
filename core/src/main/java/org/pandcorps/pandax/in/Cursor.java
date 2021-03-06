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
package org.pandcorps.pandax.in;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.*;

import java.util.*;

public class Cursor extends Panctor implements StepListener {
    private final static int INACTIVE_THRESHOLD = 90;
	private static Cursor active = null;
	private static int lastMouseX = -1, lastMouseY = -1;
	private boolean hiddenWhenUnused = false;
	private List<Panctor> actorsToHide = null;
	private long inactiveTimer = 0;
	
	public final static Cursor addCursor(final Panlayer layer, final Panmage img) {
		Panctor.destroy(active);
		final Pangine engine = Pangine.getEngine();
		if (!engine.isMouseSupported()) {
			return null;
		}
		active = new Cursor();
		active.setView(img);
		layer.addActor(active);
		engine.setMouseTouchEnabled(true);
		return active;
	}
	
	public final static Cursor addCursorIfNeeded(final Panlayer layer, final Panmage img) {
	    return isEnabled() ? getActive() : addCursor(layer, img);
	}
	
	public final static Cursor getActive() {
		if (active != null && !active.isDestroyed() && active.getLayer() != null) {
			return active;
		}
		return null;
	}
	
	public final static boolean isEnabled() {
		return getActive() != null;
	}
	
	public final Cursor setHiddenWhenUnused(final boolean hiddenWhenUnused) {
	    this.hiddenWhenUnused = hiddenWhenUnused;
	    return setVisibleAll(!hiddenWhenUnused);
	}
	
	public final Cursor setHiddenWhenUnused(final Panctor... actorsToHide) {
	    this.actorsToHide = Arrays.asList(actorsToHide);
	    return setHiddenWhenUnused(true);
	}
	
	public final Cursor hide() {
	    return setVisibleAll(false);
	}
	
	public final static void hide(final Cursor cursor) {
	    if (cursor != null) {
	        cursor.hide();
	    }
	}
	
	public final Cursor show() {
        return setVisibleAll(true);
    }
	
	public final static void show(final Cursor cursor) {
        if (cursor != null) {
            cursor.show();
        }
    }
	
	public final Cursor setVisibleAll(final boolean visible) {
	    setVisible(visible);
	    for (final Panctor actor : Coltil.unnull(actorsToHide)) {
	        actor.setVisible(visible);
	    }
	    if (!visible) {
	        lastMouseX = -1;
	        inactiveTimer = INACTIVE_THRESHOLD;
	    }
	    return this;
	}
	
	@Override
	public final void onStep(final StepEvent event) {
		final Pangine engine = Pangine.getEngine();
		final Panlayer layer = getLayer();
		final Panple o = (layer == null) ? FinPanple.ORIGIN : layer.getOrigin();
		final int mouseX = engine.getMouseX(), mouseY = engine.getMouseY();
		if ((mouseX != lastMouseX) || (mouseY != lastMouseY)) {
		    if (hiddenWhenUnused && (lastMouseX != -1)) {
		        show();
		    }
		    lastMouseX = mouseX;
		    lastMouseY = mouseY;
		    inactiveTimer = 0;
		} else {
		    inactiveTimer++;
		    if (hiddenWhenUnused && (inactiveTimer >= INACTIVE_THRESHOLD)) {
		        hide();
		    }
		}
		getPosition().set(o.getX() + mouseX, o.getY() + mouseY);
	}
	
	public final static long getInactiveTimer() {
	    return Panctor.isDestroyed(active) ? Long.MAX_VALUE : active.inactiveTimer;
	}
	
	@Override
	public final void onDestroy() {
		Pangine.getEngine().setMouseTouchEnabled(false);
	}
}
