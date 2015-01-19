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
package org.pandcorps.pandam.event.action;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.event.*;

public class InputEvent extends Panvent {
    private final Panput input;

    ///*package*/ InputEvent() {
    /*package*/ InputEvent(final Panput input) {
        this.input = input;
    }
    
    /*private final Character character; // Won't be valued for non-key events or some key events
    
    // Current underlying implementation (LWJGL) works well for giving character at press time,
    // but not while held or when released, so we always manage ourselves
    public final static ActionStartEvent getEvent(final Panput input, final Character character) {
        return new ActionStartEvent(input, character); // Could cache these
    }

    //private ActionStartEvent() {
    private ActionStartEvent(final Panput input, final Character character) {
        this.input = input;
        this.character = character;
    }*/
    
    public final Panput getInput() {
        return input;
    }
    
    public final Character getCharacter() {
        //return character;
        return input.getClass() == Key.class ? ((Key) input).getCurrentCharacter() : null;
    }
}
