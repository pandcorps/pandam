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
package org.pandcorps.core.img;

import org.pandcorps.core.*;
import org.pandcorps.core.seg.*;

public class Pancolor {
    public final static short MIN_VALUE = 0;
	public final static short MAX_VALUE = 255;
	public final static FinPancolor BLACK = new FinPancolor(MIN_VALUE, MIN_VALUE, MIN_VALUE);
	public final static FinPancolor WHITE = new FinPancolor(MAX_VALUE, MAX_VALUE, MAX_VALUE);
	public final static FinPancolor RED = new FinPancolor(MAX_VALUE, MIN_VALUE, MIN_VALUE);
	public final static FinPancolor GREEN = new FinPancolor(MIN_VALUE, MAX_VALUE, MIN_VALUE);
	public final static FinPancolor BLUE = new FinPancolor(MIN_VALUE, MIN_VALUE, MAX_VALUE);
	public final static FinPancolor YELLOW = new FinPancolor(MAX_VALUE, MAX_VALUE, MIN_VALUE);
	public final static FinPancolor CYAN = new FinPancolor(MIN_VALUE, MAX_VALUE, MAX_VALUE);
	public final static FinPancolor MAGENTA = new FinPancolor(MAX_VALUE, MIN_VALUE, MAX_VALUE);
	public final static FinPancolor GRAY = new FinPancolor((short) 192, (short) 192, (short) 192);
    private short r;
    private short g;
    private short b;
    private short a;
    
    public static enum Channel {
        Red,
        Green,
        Blue,
        Alpha
    }
    
    public Pancolor(final short[] rgb) {
        this(rgb[0], rgb[1], rgb[2], rgb.length > 3 ? rgb[3] : MAX_VALUE);
    }
    
    public Pancolor(final short v) {
        this(v, v, v);
    }
    
    public Pancolor(final short r, final short g, final short b) {
        this(r, g, b, MAX_VALUE);
    }
    
    public Pancolor(final int r, final int g, final int b) {
        this((short) r, (short) g, (short) b);
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
    
    public final short get(final Channel c) {
        if (c == Channel.Red) {
            return r;
        } else if (c == Channel.Green) {
            return g;
        } else if (c == Channel.Blue) {
            return b;
        } else if (c == Channel.Alpha) {
            return a;
        }
        throw new IllegalArgumentException(c.toString());
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
    
    public void setR(final short r) {
        this.r = r;
    }
    
    public void setG(final short g) {
        this.g = g;
    }
    
    public void setB(final short b) {
        this.b = b;
    }
    
    public void setA(final short a) {
        this.a = a;
    }
    
    public final void set(final Pancolor color) {
        set(color.getR(), color.getG(), color.getB(), color.getA());
    }
    
    public final void set(final short r, final short g, final short b, final short a) {
        setR(r);
        setG(g);
        setB(b);
        setA(a);
    }
    
    public final boolean addR(final short r) {
        final short n = add(this.r, r);
        if (n == this.r) {
            return false;
        }
        setR(n);
        return true;
    }
    
    public final boolean addG(final short g) {
        final short n = add(this.g, g);
        if (n == this.g) {
            return false;
        }
        setG(n);
        return true;
    }
    
    public final boolean addB(final short b) {
        final short n = add(this.b, b);
        if (n == this.b) {
            return false;
        }
        setB(n);
        return true;
    }
    
    public final boolean addA(final short a) {
        final short n = add(this.a, a);
        if (n == this.a) {
            return false;
        }
        setA(n);
        return true;
    }
    
    private final static short add(short c, final short i) {
        c += i;
        if (c > MAX_VALUE) {
            c = MAX_VALUE;
        } else if (c < 0) {
            c = 0;
        }
        return c;
    }
    
    @Override
    public final int hashCode() {
        return r + (256 * g) + (65536 * b) + (16777216 * a);
    }
    
    @Override
    public final boolean equals(final Object o) {
        if (!(o instanceof Pancolor)) {
            return false;
        }
        final Pancolor c = (Pancolor) o;
        return r == c.r && g == c.g && b == c.b && a == c.a;
    }
    
    @Override
    public final String toString() {
        return "(" + r + ", " + g + ", " + b + ", " + a + ")";
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
