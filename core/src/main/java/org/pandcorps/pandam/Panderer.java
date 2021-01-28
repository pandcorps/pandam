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
package org.pandcorps.pandam;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;

// Pandam Renderer
public final class Panderer {
    private static Panmage imgRectangle = null;
    
	//TODO Throw an Exception if invoked by anything other than the Pangine
	// to thwart instantiation by reflection
	/*package*/ Panderer() {
	}

	public final void render(final Panlayer layer, final Panmage image, final float x, final float y, final float z) {
		image.render(layer, x, y, z);
	}
	
	public final void render(final Panlayer layer, final Panmage image, final float x, final float y, final float z,
	    final int rot, final boolean mirror, final boolean flip) {
	    image.render(layer, x, y, z, rot, mirror, flip);
	}

	public final void render(final Panlayer layer, final Panmage image, final float x, final float y, final float z,
		final float ix, final float iy, final float iw, final float ih) {
		image.render(layer, x, y, z, ix, iy, iw, ih);
	}
	
	public final void render(final Panlayer layer, final Panmage image, final float x, final float y, final float z,
	    final float ix, final float iy, final float iw, final float ih, final int rot, final boolean mirror, final boolean flip) {
	    image.render(layer, x, y, z, ix, iy, iw, ih, rot, mirror, flip);
	}
	
	public final void render(final Panlayer layer, final Panmage image, final float x, final float y, final float z,
        final float ix, final float iy, final float iw, final float ih, final int rot, final boolean mirror, final boolean flip,
        final float r, final float g, final float b) {
        image.render(layer, x, y, z, ix, iy, iw, ih, rot, mirror, flip, r, g, b);
    }
	
	public final void rectangle(final Panlayer layer, final float x, final float y, final float z, final float w, final float h,
	    final float r, final float g, final float b) {
	    if (imgRectangle == null) {
	        final Img img = Imtil.newImage(1, 1);
	        img.setRGB(0, 0, Imtil.getDataElement(Pancolor.MAX_VALUE, Pancolor.MAX_VALUE, Pancolor.MAX_VALUE, Pancolor.MAX_VALUE));
	        imgRectangle = Pangine.getEngine().createImage(Pantil.vmid(), img);
	    }
	    render(layer, imgRectangle, x, y, z, 0, 0, w, h, 0, false, false, r, g, b);
	}
}
