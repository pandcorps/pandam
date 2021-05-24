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
package org.pandcorps.core.img;

import org.pandcorps.core.seg.*;

public class FloatColor {
    private float r = 1.0f;
    private float g = 1.0f;
    private float b = 1.0f;
    
    public FloatColor() {
    }
    
    public FloatColor(final float r, final float g, final float b) {
        set(r, g, b);
    }
    
    public FloatColor(final Piped seg) {
        set(seg);
    }
    
    public final float getR() {
        return r;
    }
    
    public final float getG() {
        return g;
    }
    
    public final float getB() {
        return b;
    }
    
    public final void setR(final float r) {
        this.r = r;
    }
    
    public final void setG(final float g) {
        this.g = g;
    }
    
    public final void setB(final float b) {
        this.b = b;
    }
    
    public final void setGrey(final float value) {
        r = g = b = value;
    }
    
    public final void set(final FloatColor src) {
        set(src.r, src.g, src.b);
    }
    
    public final void set(final float r, final float g, final float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }
    
    public final void set(final Piped seg) {
        set(seg, 0);
    }
    
    public final void set(final Piped seg, final int i) {
        r = seg.floatValue(i);
        g = seg.floatValue(i + 1);
        b = seg.floatValue(i + 2);
    }
    
    public final void addR(final float r) {
        this.r = add(this.r, r);
    }
    
    public final void addG(final float g) {
        this.g = add(this.g, g);
    }
    
    public final void addB(final float b) {
        this.b = add(this.b, b);
    }
    
    private final static float add(float c, final float amount) {
        c += amount;
        if (c > 1.0f) {
            c = 1.0f;
        } else if (c < 0.0f) {
            c = 0.0f;
        }
        return c;
    }
    
    public final void addAll(final float amount) {
        addR(amount);
        addG(amount);
        addB(amount);
    }
}
