/*
Copyright (c) 2009-2016, Andrew M. Martin
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
package org.pandcorps.pandam.impl;

import org.pandcorps.pandam.*;

public final class ImplPanframe extends BasePantity implements Panframe {
	private final Panmage image;
	private final int dur;
	private final int rot;
	private final boolean mirror;
	private final boolean flip;
	private final Panple origin;
	private final Panple boundMin;
	private final Panple boundMax;

	public ImplPanframe(final String id, final Panmage image, final int dur, final int rot, final boolean mirror, final boolean flip,
	                    final Panple origin, final Panple boundMin, final Panple boundMax) {
		super(id);

		this.image = image;
		this.dur = dur;
		this.rot = rot;
		this.mirror = mirror;
		this.flip = flip;
		this.origin = origin;
		this.boundMin = boundMin;
		this.boundMax = boundMax;
	}

	@Override
	public final Panmage getImage() {
		return image;
	}

	@Override
	public final int getDuration() {
		return dur;
	}
	
	@Override
	public final int getRot() {
	    return rot;
	}
	
	@Override
	public final boolean isMirror() {
	    return mirror;
	}
    
	@Override
    public final boolean isFlip() {
	    return flip;
    }
	
	@Override
	public final Panple getOrigin() {
	    return origin;
	}

	@Override
	public final Panple getBoundingMinimum() {
	    return boundMin;
	}
    
	@Override
    public final Panple getBoundingMaximum() {
	    return boundMax;
    }
	
	@Override
	public final Panple getEffectiveOrigin() {
	    return origin == null ? image.getOrigin() : origin;
	}
	
	@Override
	public final Panple getEffectiveBoundingMinimum() {
	    return boundMin == null ? image.getBoundingMinimum() : boundMin;
	}
    
	@Override
    public final Panple getEffectiveBoundingMaximum() {
        return boundMax == null ? image.getBoundingMaximum() : boundMax;
    }
	
	@Override
	public void destroyAll() {
		destroy();
		destroy(image);
	}
}
