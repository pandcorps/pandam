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
package org.pandcorps.pandam.impl;

import org.pandcorps.core.img.*;
import org.pandcorps.pandam.*;

public class AdjustedPanmage extends Panmage {
    private Panmage src = null;
    private final int rot;
    private final boolean mirror;
    private final boolean flip;
    private final float r;
    private final float g;
    private final float b;
    private final float subX;
    private final float subY;
    private final Panple subSize;
    
    public AdjustedPanmage(final String id, final Panmage src, final int rot, final boolean mirror, final boolean flip) {
        this(id, src, rot, mirror, flip, 1.0f, 1.0f, 1.0f);
    }
    
    public AdjustedPanmage(final String id, final Panmage src, final Pancolor color) {
        this(id, src, color.getRf(), color.getGf(), color.getBf());
    }
    
    public AdjustedPanmage(final String id, final Panmage src, final float r, final float g, final float b) {
        this(id, src, 0, false, false, r, g, b);
    }
    
    public AdjustedPanmage(final String id, final Panmage src, final int rot, final boolean mirror, final boolean flip, final float r, final float g, final float b) {
        this(id, src, rot, mirror, flip, r, g, b, 0, 0, null);
    }
    
    public AdjustedPanmage(final String id, final Panmage src, final int rot, final boolean mirror, final boolean flip, final float r, final float g, final float b,
            final float subX, final float subY, final Panple subSize) {
        super(id, src.getOrigin(), src.getBoundingMinimum(), src.getBoundingMaximum());
        this.src = src;
        this.rot = rot;
        this.mirror = mirror;
        this.flip = flip;
        this.r = r;
        this.g = g;
        this.b = b;
        this.subX = subX;
        this.subY = subY;
        this.subSize = subSize;
    }
    
    @Override
    public final Panple getSize() {
        return (subSize == null) ? src.getSize() : subSize;
    }

    @Override
    protected final void render(final Panlayer layer, final float x, final float y, final float z,
                                final float ix, final float iy, final float iw, final float ih,
                                final int rot, final boolean mirror, final boolean flip,
                                final float r, final float g, final float b) {
        render(src, layer, x, y, z, subX + ix, subY + iy, iw, ih, (this.rot + rot) % 4, this.mirror ^ mirror, this.flip ^ flip, this.r * r, this.g * g, this.b * b);
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
