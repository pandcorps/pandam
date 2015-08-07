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
package org.pandcorps.pandam.impl;

import org.pandcorps.pandam.*;

public class SubPanmage extends Panmage {
    private Panmage src = null;
    private final float subX;
    private final float subY;
    private final Panple subSize;
    
    public SubPanmage(final String id, final Panple origin, final Panple boundMin, final Panple boundMax, final Panmage src,
                      final float subX, final float subY, final Panple subSize) {
        super(id, origin, boundMin, boundMax);
        this.src = src;
        this.subX = subX;
        this.subY = subY;
        this.subSize = subSize; // Many sub-images could have same size; let caller pass same instance to each
    }
    
    @Override
    public final Panple getSize() {
        return subSize;
    }

    @Override
    protected final void render(final Panlayer layer, final float x, final float y, final float z,
                                final float ix, final float iy, final float iw, final float ih,
                                final int rot, final boolean mirror, final boolean flip) {
        render(src, layer, x, y, z, subX + ix, subY + iy, iw, ih, rot, mirror, flip);
    }
    
    @Override
    protected final void close() {
        src = null; // Doesn't free underlying resource; it must be closed separately if needed
    }
    
    @Override
    protected final boolean isClosed() {
        return src == null;
    }
}
