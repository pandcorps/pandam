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
package org.pandcorps.core.seg;

import java.io.*;

import org.pandcorps.core.*;

public abstract class Record {
    public abstract void serialize(final Writer w) throws IOException;
    
    public abstract String getValue(final int i);
    
    public abstract void setValue(final int i, final String v);
    
    @Override
    public final String toString() {
        final StringWriter w = new StringWriter();
        try {
            serialize(w);
            return w.toString();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } finally {
            Iotil.close(w);
        }
    }
    
    public final byte byteValue(final int i) {
        return parseByte(getValue(i));
    }
    
    public final short shortValue(final int i) {
        return parseShort(getValue(i));
    }
    
    public final int intValue(final int i) {
        return parseInt(getValue(i));
    }
    
    public final float floatValue(final int i) {
        return parseFloat(getValue(i));
    }
    
    public final boolean booleanValue(final int i) {
        return parseBoolean(getValue(i));
    }
    
    public final Byte getByte(final int i) {
        return Mathtil.toByte(getValue(i));
    }
    
    public final Short getShort(final int i) {
        return Mathtil.toShort(getValue(i));
    }
    
    public final Integer getInteger(final int i) {
        return Mathtil.toInteger(getValue(i));
    }
    
    public final Float getFloat(final int i) {
        return Mathtil.toFloat(getValue(i));
    }
    
    public final Boolean getBoolean(final int i) {
        return Pantil.toBoolean(getValue(i));
    }
    
    public final void setByte(final int i, final byte value) {
    	setValue(i, String.valueOf(value));
    }
    
    public final void setShort(final int i, final short value) {
    	setValue(i, String.valueOf(value));
    }
    
    public final void setInt(final int i, final int value) {
    	setValue(i, String.valueOf(value));
    }
    
    public final void setFloat(final int i, final float value) {
    	setValue(i, String.valueOf(value));
    }
    
    public final void setBoolean(final int i, final boolean value) {
    	setValue(i, String.valueOf(value));
    }
    
    public final void setByte(final int i, final Byte value) {
    	setValue(i, Chartil.toString(value));
    }
    
    public final void setShort(final int i, final Short value) {
    	setValue(i, Chartil.toString(value));
    }
    
    public final void setInteger(final int i, final Integer value) {
    	setValue(i, Chartil.toString(value));
    }
    
    public final void setFloat(final int i, final Float value) {
    	setValue(i, Chartil.toString(value));
    }
    
    public final void setBoolean(final int i, final Boolean value) {
    	setValue(i, Chartil.toString(value));
    }
    
    protected final static byte parseByte(final String value) {
        return Byte.parseByte(value);
    }
    
    protected final static short parseShort(final String value) {
        return Short.parseShort(value);
    }
    
    protected final static int parseInt(final String value) {
        return Integer.parseInt(value); // Define here in case we decide to default to 0 or something
    }
    
    protected final static float parseFloat(final String value) {
        return Float.parseFloat(value);
    }
    
    protected final static boolean parseBoolean(final String value) {
        return Boolean.parseBoolean(value);
    }
}
