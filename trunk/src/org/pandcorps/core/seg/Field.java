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
import java.util.*;

import org.pandcorps.core.*;

public class Field extends Record {
    private final static int DELIM_COMPONENT = '^';
    
    private final List<String> components = new ArrayList<String>();
    
    public final static Field create(final String value) {
    	if (value == null) {
    		return null;
    	} else {
    		final Field field = new Field();
    		field.setValue(0, value);
    		return field;
    	}
    }
    
    public final static Field parse(final String line, final int start, final int end) {
        if (end <= start) {
            return null;
        }
        Field fld = null;
        int ci = 0, cs = start;
        for (int i = start; i <= end; i++) {
            final char c = i == end ? DELIM_COMPONENT : line.charAt(i);
            if (c == DELIM_COMPONENT) {
                if (i > cs) {
                    if (fld == null) {
                        fld = new Field();
                    }
                    Coltil.set(fld.components, ci, line.substring(cs, i));
                }
                ci++;
                cs = i + 1;
            }
        }
        return fld;
    }
    
    @Override
    public final void save(final Writer w) throws IOException {
        boolean first = true;
        for (final String cmp : components) {
            if (first) {
                first = false;
            } else {
                w.write(DELIM_COMPONENT);
            }
            if (cmp != null) {
            	w.write(cmp);
            }
        }
    }
    
    public final String getValue() {
        return Coltil.getOnly(components);
    }
    
    @Override
    public final String getValue(final int i) {
        return Coltil.get(components, i);
    }
    
    @Override
    public final void setValue(final int i, final String value) {
        Coltil.setIfNeeded(components, i, value);
    }
    
    // Could add setters with no index that default to 0
    
    @Override
    public final void clear() {
    	Coltil.clear(components);
    }
    
    public final byte byteValue() {
        return parseByte(getValue());
    }
    
    public final short shortValue() {
        return parseShort(getValue());
    }
    
    public final int intValue() {
        return parseInt(getValue());
    }
    
    public final int getInt(final int def) {
        return parseInt(getValue(), def);
    }
    
    public final int initInt() {
        return getInt(0);
    }
    
    public final long longValue() {
        return parseLong(getValue());
    }
    
    public final long getLong(final long def) {
        return parseLong(getValue(), def);
    }
    
    public final long initLong() {
        return getLong(0);
    }
    
    public final float floatValue() {
        return parseFloat(getValue());
    }
    
    public final boolean booleanValue() {
        return parseBoolean(getValue());
    }
    
    public final Byte getByte() {
        return Mathtil.toByte(getValue());
    }
    
    public final Short getShort() {
        return Mathtil.toShort(getValue());
    }
    
    public final Integer getInteger() {
        return Mathtil.toInteger(getValue());
    }
    
    public final Float getFloat() {
        return Mathtil.toFloat(getValue());
    }
    
    public final Boolean getBoolean() {
        return Pantil.toBoolean(getValue());
    }
    
    public final static String getValue(final Field f) {
        return f == null ? null : f.getValue();
    }
}
