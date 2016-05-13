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

import org.pandcorps.core.*;
import org.pandcorps.furguardians.Player.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;

public final class WordScreen extends Panscreen {
    private final static int DIM = 16;
    private final static int SIZE = 4;
    private Panroom room = null;
    private List<String> words = null;
    private Letter[][] grid = null;
    private final List<Letter> currentSelection = new ArrayList<Letter>(SIZE * SIZE);
    
    @Override
    protected final void load() throws Exception {
        final Pangine engine = Pangine.getEngine();
        engine.zoomToMinimum(64);
        room = FurGuardiansGame.createRoom(engine.getEffectiveWidth(), engine.getEffectiveHeight());
        initWords();
    }
    
    private final void initWords() {
        words = new ArrayList<String>();
        words.add("ABCD");
        words.add("EFGH");
        words.add("IJKL");
        words.add("MNOP");
        grid = new Letter[SIZE][SIZE];
        new Letter(0, 0, 'A'); new Letter(0, 1, 'B'); new Letter(0, 2, 'C'); new Letter(0, 3, 'D');
        new Letter(1, 0, 'E'); new Letter(1, 1, 'L'); new Letter(1, 2, 'M'); new Letter(1, 3, 'N');
        new Letter(2, 0, 'F'); new Letter(2, 1, 'K'); new Letter(2, 2, 'J'); new Letter(2, 3, 'O');
        new Letter(3, 0, 'G'); new Letter(3, 1, 'H'); new Letter(3, 2, 'I'); new Letter(3, 3, 'P');
        currentSelection.clear();
    }
    
    private final void onRelease() {
        final StringBuilder b = new StringBuilder(currentSelection.size());
        for (final Letter letter : currentSelection) {
            b.append(letter.c);
        }
        final String currentWord = b.toString();
        for (final String word : words) {
            if (currentWord.equals(word)) {
                award(word);
                return;
            }
        }
        clearCurrentSelection();
    }
    
    private final void award(final String word) {
        for (final Letter letter : currentSelection) {
            letter.use();
        }
        currentSelection.clear();
        if (isVictory()) {
            victory();
        }
    }
    
    private final boolean isVictory() {
        for (final Letter[] row : grid) {
            for (final Letter letter : row) {
                if (letter.mode != MODE_USED) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private final void victory() {
        //TODO
    }
    
    private final void clearCurrentSelection() {
        for (final Letter letter : currentSelection) {
            letter.inactivate();
        }
        currentSelection.clear();
    }
    
    private final static byte MODE_UNUSED = 0;
    private final static byte MODE_ACTIVE = 1;
    private final static byte MODE_USED = 2;
    
    private final class Letter extends Panctor {
        private final int row;
        private final int col;
        private final char c;
        private byte mode = MODE_UNUSED;
        
        private Letter(final int row, final int col, final char c) {
            this.row = row;
            this.col = col;
            this.c = c;
            inactivate();
            room.addActor(this);
            grid[row][col] = this;
            final Pangine engine = Pangine.getEngine();
            final int x = col * DIM, y = engine.getEffectiveHeight() - (row + 1) * DIM;
            getPosition().set(x, y);
            final TouchButton button = new TouchButton(engine.getInteraction(), "Letter." + row + "." + col, x, y, DIM, DIM);
            engine.registerTouchButton(button);
        }
        
        private final void inactivate() {
            setMode(MODE_UNUSED, FurGuardiansGame.getBlockLetter(c));
        }
        
        private final void activate() {
            setMode(MODE_ACTIVE, FurGuardiansGame.getTranslucentBlockLetter(c));
        }
        
        private final void use() {
            setMode(MODE_USED, FurGuardiansGame.getGemLetter(c));
            //TODO shatter
        }
        
        private final void setMode(final byte mode, final Panmage img) {
            this.mode = mode;
            setView(img);
        }
        
        private final void onTap() {
            if (mode != MODE_UNUSED) {
                return;
            } else if (!(currentSelection.isEmpty() || currentSelection.get(currentSelection.size() - 1).isAdjacentTo(this))) {
                return;
            }
            activate();
            currentSelection.add(this);
        }
        
        private final boolean isAdjacentTo(final Letter letter) {
            final int otherRow = letter.row, otherCol = letter.col;
            if (row == otherRow) {
                return Math.abs(col - otherCol) == 1;
            } else if (col == otherCol) {
                return Math.abs(row - otherRow) == 1;
            }
            return false;
        }
    }
    
    protected final static PlayerContext getPlayerContext() {
        return Coltil.isEmpty(FurGuardiansGame.pcs) ? null : FurGuardiansGame.pcs.get(0);
    }
    
    protected final static void addGems(final int n) {
        final PlayerContext pc = getPlayerContext();
        if (pc != null) {
            pc.addGems(n);
        }
    }
}
