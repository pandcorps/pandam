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
package org.pandcorps.botsnbolts;

import org.pandcorps.pandam.*;

public abstract class HudMeter extends Panctor {
    protected final static int MAX_VALUE = 28;
    
    private final HudMeterImages images;
    
    protected HudMeter(final HudMeterImages images) {
        this.images = images;
    }
    
    @Override
    protected final void renderView(final Panderer renderer) {
        final Panlayer layer = getLayer();
        if (layer == null) {
            return;
        }
        final Panple pos = getPosition();
        final float x = pos.getX(), y = pos.getY(), z = pos.getZ();
        final int value = getValue();
        final int end = MAX_VALUE - 1;
        for (int i = 0; i < MAX_VALUE; i++) {
            final HudMeterImages set = (value < i) ? null : images; //TODO blankImages
            final Panmage image;
            if (i == 0) {
                image = set.bottom;
            } else if (i < end) {
                image = set.middle;
            } else {
                image = set.top;
            }
            renderer.render(layer, image, x, y + (i * 2), z);
        }
    }
    
    protected abstract int getValue();
    
    protected final static class HudMeterImages {
        private final Panmage bottom;
        private final Panmage middle;
        private final Panmage top;
        
        protected HudMeterImages(final Panmage bottom, final Panmage middle, final Panmage top) {
            this.bottom = bottom;
            this.middle = middle;
            this.top = top;
        }
    }
}
