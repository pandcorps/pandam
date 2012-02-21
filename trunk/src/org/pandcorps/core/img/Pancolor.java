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
package org.pandcorps.core.img;

import org.pandcorps.core.*;
import org.pandcorps.core.seg.*;

public final class Pancolor {
    public final static short MIN_VALUE = 0;
	public final static short MAX_VALUE = 255;
	public final static Pancolor BLACK = new Pancolor(MIN_VALUE, MIN_VALUE, MIN_VALUE, MAX_VALUE);
	public final static Pancolor WHITE = new Pancolor(MAX_VALUE, MAX_VALUE, MAX_VALUE, MAX_VALUE);
    private short r;
    private short g;
    private short b;
    private short a;
    
    public Pancolor(final short[] rgb) {
        this(rgb[0], rgb[1], rgb[2], rgb.length > 3 ? rgb[3] : MAX_VALUE);
    }
    
    public Pancolor(final short r, final short g, final short b, final short a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
    
    public final short getR() {
        return r;
    }
    
    public final short getG() {
        return g;
    }
    
    public final short getB() {
        return b;
    }
    
    public final short getA() {
        return a;
    }
    
    // Lightness
    public final short getL() {
    	return (short) Math.round(Mathtil.max(r, g, b) + Mathtil.min(r, g, b) * 0.5f);
    }
    
    // Intensity
    public final short getI() {
    	return (short) Math.round((r + g + b) / 3f);
    }
    
    // HSV Value (HSB Brightness)
    public final short getV() {
    	return (short) Mathtil.max(r, g, b);
    }
    
    public final float getRf() {
        return Imtil.colorToFloat(r);
    }
    
    public final float getGf() {
        return Imtil.colorToFloat(g);
    }
    
    public final float getBf() {
        return Imtil.colorToFloat(b);
    }
    
    public final float getAf() {
        return Imtil.colorToFloat(a);
    }
    
    public final float getLf() {
    	return Imtil.colorToFloat(getL());
    }
    
    public final float getIf() {
    	return Imtil.colorToFloat(getI());
    }
    
    public final float getVf() {
    	return Imtil.colorToFloat(getV());
    }
    
    public final void setR(final short r) {
        this.r = r;
    }
    
    public final void setG(final short g) {
        this.g = g;
    }
    
    public final void setB(final short b) {
        this.b = b;
    }
    
    public final void setA(final short a) {
        this.a = a;
    }
    
    public final static Pancolor getPancolor(final Segment seg, final int i) {
        return getPancolor(seg.getField(i));
    }
    
    public final static Pancolor getPancolor(final Field fld) {
        if (fld == null) {
            return null;
        }
        final Short r = fld.getShort(0);
        final Short g = fld.getShort(1);
        final Short b = fld.getShort(2);
        final Short a = fld.getShort(3);
        if (r == null && g == null && b == null && a == null) {
            return null;
        }
        return new Pancolor(Mathtil.shortValue(r), Mathtil.shortValue(g), Mathtil.shortValue(b), Mathtil.shortValue(a, MAX_VALUE));
    }
}
