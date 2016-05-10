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
package org.pandcorps.furguardians;

import java.util.*;

import org.pandcorps.pandam.*;

public final class WordScreen extends Panscreen {
    private List<String> words = null;
    private Letter[][] grid = null;
    
    @Override
    protected final void load() throws Exception {
        Pangine.getEngine().zoomToMinimum(64);
        initWords();
    }
    
    private final void initWords() {
        words = new ArrayList<String>();
        words.add("ABCD");
        words.add("EFGH");
        words.add("IJKL");
        words.add("MNOP");
        grid = new Letter[][] {
            { new Letter('A'), new Letter('B'), new Letter('C'), new Letter('D') },
            { new Letter('E'), new Letter('L'), new Letter('M'), new Letter('N') },
            { new Letter('F'), new Letter('K'), new Letter('J'), new Letter('O') },
            { new Letter('G'), new Letter('H'), new Letter('I'), new Letter('P') }
        };
        //new TouchButton();
    }
    
    private final static byte MODE_UNUSED = 0;
    private final static byte MODE_ACTIVE = 1;
    private final static byte MODE_USED = 2;
    
    private final static class Letter {
        private final char c;
        private Panmage img;
        private byte mode = MODE_UNUSED;
        
        private Letter(final char c) {
            this.c = c;
            img = FurGuardiansGame.getBlockLetter(c);
        }
    }
}
