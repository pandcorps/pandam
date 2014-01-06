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
package org.pandcorps.pandam;

import org.pandcorps.core.Chartil;
import org.pandcorps.pandam.Panteraction.*;

// Pandam Input
public abstract class Panput {
    /*package*/ final static Any any = new Any();
    /*package*/ Device device;
    private final String name;
    /*package*/ boolean active = false;
    /*package*/ boolean inactivated = false;
    
    protected Panput(final Device device, final String name) {
    	this.device = device;
    	this.name = name;
    }
    
    public final Device getDevice() {
    	return device;
    }
    
    public final String getName() {
    	return name;
    }
    
    // We allow event-based input and polling
    //TODO Do we need this and Panction?
    public final boolean isActive() {
        return active && !inactivated;
    }
    
    //TODO Support inactivate in Listeners and Panction
    public final void inactivate() {
        // I think inactivated is cleared during the key release event;
        // I don't think anything checks after every frame to clear this if it's not pressed.
        // So I think we need to check active before setting inactivated.
        if (active) {
            inactivated = true;
        }
    }
    
    public final static void inactivate(final Panput... inputs) {
        for (final Panput input : inputs) {
            input.inactivate();
        }
    }
    
	public final static class Key extends Panput {
	    private final Panteraction interaction;
		private final int index;
		private final Character base;
		private final Character shift;
		private final boolean letter;
		/*package*/ Key(final Panteraction interaction, final int index, final Character base, final Character shift, final boolean letter, final String s) {
			super(interaction.KEYBOARD, s == null ? Chartil.toString(base) : s);
			/*if (getName() == null) {
				System.err.println("No name for key " + index);
			}*/
		    this.interaction = interaction;
			this.index = index;
			this.base = base;
			this.shift = shift;
			this.letter = letter;
		}
		public final int getIndex() {
			return index;
		}
		public final Character getBaseCharacter() {
		    return base;
		}
		public final Character getCurrentCharacter() {
            return interaction.isShiftActive() ^ (letter && interaction.isCapsLockEnabled())? shift : base;
        }
		public final boolean isLetter() {
		    return letter;
		}
		@Override
		public final int hashCode() {
			return index;
		}
		// We shouldn't allow two instances of same key, so this is unnecessary work
		//@Override
		//public final boolean equals(final Object o) {
		//	return o instanceof Key ? ((Key) o).index == index : false;
		//}
	}
	
	public final static class Button extends Panput {
		/*package*/ Button(final String name) {
			super(null, name); // Buttons are created before Controller and passed to Controller constructor which assigns device
		}
	}
	
	/*package*/ final static class Any extends Panput {
	    private Any() {
	    	super(null, "Any");
	    }
	}
}
