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
package org.pandcorps.monster;

import java.util.concurrent.*;

import org.pandcorps.core.*;

public class Special {
    public static enum Specialty {
        Library,
        Lab,
        Fish,
        Condition,
        Breeder(new Callable<String>() {
            @Override public final String call() {
                return Data.getBreeder();
            }}),
        Trader,
        Split;
        
        private final Callable<String> namer;
        
        private Specialty(final Callable<String> namer) {
            this.namer = namer;
        }
        
        private Specialty() {
            this(null);
        }
        
        @Override
        public final String toString() {
            try {
                return (namer == null) ? super.toString() : namer.call();
            } catch (final Exception e) {
                throw Pantil.toRuntimeException(e);
            }
        }
    }
    
    public final static Special LIBRARY = new Special(Specialty.Library, null);
    public final static Special BREEDER = new Special(Specialty.Breeder, null);
    public final static Special TRADER = new Special(Specialty.Trader, null);
    public final static Special SPLIT = new Special(Specialty.Split, null);
    
    private final Specialty specialty;
    private final Item requirement;
    
    protected Special(final Specialty specialty, final Item requirement) {
        this.specialty = specialty;
        this.requirement = requirement;
    }
    
    public Specialty getSpecialty() {
        return specialty;
    }
    
    public Item getRequirement() {
        return requirement;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Special)) {
            return false;
        }
        final Special s = (Special) o;
        return specialty == s.specialty && Pantil.equals(requirement, s.requirement);
    }
    
    @Override
    public String toString() {
        return specialty + (requirement == null ? "" : ("." + requirement.getCode()));
    }
    
    public final static Special getSpecial(final String value) {
        if (value == null || value.length() == 0) {
            return null;
        }
        final Specialty specialty;
        if (value.startsWith("Lab.")) {
            specialty = Specialty.Lab;
        } else if (value.startsWith("Fish.")) {
            specialty = Specialty.Fish;
        } else if (value.startsWith("Condition.")) {
            specialty = Specialty.Condition;
        } else if (value.equals("Library") || value.equals("lbry")) {
            return LIBRARY;
        } else if (value.equals("Breeder") || value.equals("brdr")) {
            return BREEDER;
        } else if (value.equals("Trader") || value.equals("trdr")) {
            return TRADER;
        } else if (value.equals("Split") || value.equals("splt")) {
            return SPLIT;
        } else {
            throw new IllegalArgumentException(value);
        }
        return new Special(specialty, Item.getItem(value.substring(value.indexOf('.') + 1)));
    }
    
    public final static Specialty getSpecialty(final Special special) {
        return special == null ? null : special.specialty;
    }
    
    /*
    protected Special(final Item requirement) {
        this.requirement = requirement;
    } 
 
    protected Special() {
        this(null);
    }
    
    public final static class Library extends Special {
        public Library() {
        }
    }
    
    public final static class Lab extends Special {
        public Lab(final Item requirement) {
            super(requirement);
        }
    }
    
    public final static class Fish extends Special {
        public Fish(final Item requirement) {
            super(requirement);
        }
    }
    
    public final static class Move extends Special {
        public Move(final Item requirement) {
            super(requirement);
        }
    }
    */
}
