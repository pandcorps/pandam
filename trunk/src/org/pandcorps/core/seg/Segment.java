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

public class Segment extends Record {
    private final static char DELIM_FIELD = '|';
    private final static char DELIM_REP = '~';
    
    private String name = null;
    private final ArrayList<ArrayList<Field>> fields = new ArrayList<ArrayList<Field>>();
    
    public Segment() {
    }
    
    public Segment(final String name) {
        this.name = name;
    }
    
    public final static Segment parse(final String line) {
        final int size = Chartil.size(line);
        if (size == 0) {
            return null;
        }
        Segment seg = null;
        int f = 0, start = 0;
        for (int i = 0; i <= size; i++) {
            final char c = i == size ? DELIM_FIELD : line.charAt(i);
            if (c == DELIM_FIELD || c == DELIM_REP) {
                if (seg == null) {
                    if (c == DELIM_REP) {
                        throw new RuntimeException("Unexpected " + DELIM_REP + " at " + i);
                    }
                    seg = new Segment(line.substring(0, i));
                } else {
                    final Field field = Field.parse(line, start, i);
                    // SEG|| should have no SEG.0 at all; SEG|~value| should have a null repetition before value
                    if (field != null || c == DELIM_REP) {
                        ArrayList<Field> reps = Coltil.get(seg.fields, f);
                        if (reps == null) {
                            reps = new ArrayList<Field>();
                            Coltil.set(seg.fields, f, reps);
                        }
                        reps.add(field);
                    }
                    if (c == DELIM_FIELD) {
                        f++;
                    }
                }
                start = i + 1;
            }
        }
        return seg;
    }
    
    @Override
    public final void save(final Writer w) throws IOException {
        w.write(name);
        for (final List<Field> field : fields) {
            w.write(DELIM_FIELD);
            boolean first = true;
            for (final Field rep : Coltil.unnull(field)) {
                if (first) {
                    first = false;
                } else {
                    w.write(DELIM_REP);
                }
                rep.save(w);
            }
        }
    }
    
    public final String getName() {
        return name;
    }
    
    public final List<Field> getRepetitions(final int i) {
        return Coltil.unmodifiableList(Coltil.get(fields, i));
    }
    
    public final Field getField(final int i) {
        return Coltil.getOnly(getRepetitions(i));
    }
    
    @Override
    public final String getValue(final int i) {
        return Field.getValue(getField(i));
    }
    
    public final void setName(final String name) {
        this.name = name;
    }
    
    public final void setRepetitions(final int i, final ArrayList<Field> repetitions) {
    	Coltil.setIfNeeded(fields, i, repetitions);
    }
    
    public final void setField(final int i, final Field field) {
    	final ArrayList<Field> repetitions;
    	if (field == null) {
    		repetitions = null;
    	} else {
    		repetitions = new ArrayList<Field>(1);
    		repetitions.add(field);
    	}
    	setRepetitions(i, repetitions);
    }
    
    public final void addField(final int i, final Field field) {
    	setRepetitions(i, Coltil.add(Coltil.get(fields, i), field));
    }
    
    @Override
    public final void setValue(final int i, final String value) {
    	setField(i, Field.create(value));
    }
    
    @Override
    public final void clear() {
    	Coltil.clear(fields);
    }
    
    public final void addValue(final int i, final String value) {
    	addField(i, Field.create(value));
    }
    
    public final void addByte(final int i, final byte value) {
    	addValue(i, String.valueOf(value));
    }
    
    public final void addShort(final int i, final short value) {
    	addValue(i, String.valueOf(value));
    }
    
    public final void addInt(final int i, final int value) {
    	addValue(i, String.valueOf(value));
    }
    
    public final void addFloat(final int i, final float value) {
    	addValue(i, String.valueOf(value));
    }
    
    public final void addBoolean(final int i, final boolean value) {
    	addValue(i, String.valueOf(value));
    }
    
    public final void addByte(final int i, final Byte value) {
    	addValue(i, Chartil.toString(value));
    }
    
    public final void addShort(final int i, final Short value) {
    	addValue(i, Chartil.toString(value));
    }
    
    public final void addInteger(final int i, final Integer value) {
    	addValue(i, Chartil.toString(value));
    }
    
    public final void addFloat(final int i, final Float value) {
    	addValue(i, Chartil.toString(value));
    }
    
    public final void addBoolean(final int i, final Boolean value) {
    	addValue(i, Chartil.toString(value));
    }
}
