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
package org.pandcorps.test.pandam;

import java.awt.image.BufferedImage;

import org.pandcorps.core.img.*;
import org.pandcorps.pandam.*;

public final class UnitPangine extends Pangine {
    private int w, h;
    private boolean full;
    
	public final static void setEngine(final UnitPangine engine) {
		Pangine.engine = engine;
	}

	@Override
	protected final Panmage newImage(final String id, final Panple origin, final Panple boundMin, final Panple boundMax, String location) throws Panception {
		location = location.substring(location.lastIndexOf('_') + 1, location.lastIndexOf('.'));
		final int x = location.indexOf('x');
		return new UnitPanmage(
			id, Integer.parseInt(location.substring(0, x)), Integer.parseInt(location.substring(x + 1)));
	}
	
	@Override
    protected final Panmage newImage(final String id, final Panple origin, final Panple boundMin, final Panple boundMax, BufferedImage location) throws Panception {
	    return null;
	}
	
	@Override
    protected final Panmage[][] newSheet(final String prefix, final Panple origin, final Panple boundMin, final Panple boundMax, final String location,
                                         final int iw, final int ih) throws Panception {
        return null;
    }

	@Override
	protected final Panplementation newImplementation(final Panctor actor) throws Panception {
		return new UnitPanplementation(actor);
	}
	
	@Override
    public final int getDesktopWidth() {
        return 1024;
    }
    
    @Override
    public final int getDesktopHeight() {
        return 768;
    }
	
	@Override
	public final void setDisplaySize(final int w, final int h) {
	    this.w = w;
	    this.h = h;
	}
	
	@Override
	public final int getDisplayWidth() {
	    return w;
	}
	
	@Override
    public final int getDisplayHeight() {
        return h;
    }
	
	@Override
	public final void setFullScreen(final boolean full) {
	    this.full = full;
	}
	
	@Override
    public final boolean isFullScreen() {
        return full;
    }

	@Override
	public final Panteraction getInteraction() {
		return null;
	}

	@Override
	protected final void init() throws Exception {
	}

	@Override
	protected final void start() throws Exception {
	}

	//@Override
	//public final void track(final Panctor actor) {		
	//}
	
	@Override
	public final void setTitle(final String title) {
	}
	
	@Override
	public final void setIcon(final String... locations) {
	}
	
	@Override
	public final void setBgColor(final Pancolor color) {
	}

	@Override
	public final void exit() {
	}
}
