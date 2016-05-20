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

import java.io.*;
import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.furguardians.Player.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.text.*;

public final class WordScreen extends Panscreen {
    private final static int DIM = 16;
    private final static int SIZE = 4;
    private final static String[] SKIP = { "ETBJ", "RGHS" }; //TODO
    private Panroom room = null;
    private List<Word> words = null;
    private Letter[][] grid = null;
    private final List<Letter> currentSelection = new ArrayList<Letter>(SIZE * SIZE);
    
    public final static void main(final String[] args) {
        try {
            validateWordFile();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected final void load() throws Exception {
        final Pangine engine = Pangine.getEngine();
        engine.zoomToMinimum(64);
        engine.setBgColor(Pancolor.BLACK);
        room = FurGuardiansGame.createRoom(engine.getEffectiveWidth(), engine.getEffectiveHeight());
        final Cursor cursor = FurGuardiansGame.addCursor(room);
        if (cursor != null) {
            cursor.getPosition().setZ(20);
        }
        initWords();
    }
    
    private final void initWords() {
        destroyAll();
        words = new ArrayList<Word>();
        new Word("ABCD");
        new Word("EFGH");
        new Word("IJKL");
        new Word("MNOP");
        grid = new Letter[SIZE][SIZE];
        new Letter(0, 0, 'A'); new Letter(0, 1, 'B'); new Letter(0, 2, 'C'); new Letter(0, 3, 'D');
        new Letter(1, 0, 'E'); new Letter(1, 1, 'L'); new Letter(1, 2, 'M'); new Letter(1, 3, 'N');
        new Letter(2, 0, 'F'); new Letter(2, 1, 'K'); new Letter(2, 2, 'J'); new Letter(2, 3, 'O');
        new Letter(3, 0, 'G'); new Letter(3, 1, 'H'); new Letter(3, 2, 'I'); new Letter(3, 3, 'P');
        currentSelection.clear();
    }
    
    @Override
    public final void step() {
        if (currentSelection.size() > 0 && !isTouchActive()) {
            onRelease();
        }
    }
    
    private final boolean isTouchActive() {
        for (final Letter[] row : grid) {
            for (final Letter letter : row) {
                if (letter.button.isActive()) {
                    return true;
                }
            }
        }
        return Pangine.getEngine().getInteraction().TOUCH.isActive();
    }
    
    private final void onRelease() {
        final StringBuilder b = new StringBuilder(currentSelection.size());
        for (final Letter letter : currentSelection) {
            b.append(letter.c);
        }
        final String currentWord = b.toString();
        for (final Word word : words) {
            if (currentWord.equals(word.value)) {
                award(word);
                return;
            }
        }
        clearCurrentSelection();
    }
    
    private final void award(final Word word) {
        for (final Letter letter : currentSelection) {
            letter.use();
        }
        currentSelection.clear();
        if (isVictory()) {
            victory();
        }
        Chartil.set(word.b, word.value);
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
        FurGuardiansGame.setScreen(new WordScreen());
    }
    
    private final void clearCurrentSelection() {
        for (final Letter letter : currentSelection) {
            letter.inactivate();
        }
        currentSelection.clear();
    }
    
    private final void destroyAll() {
        destroyWords();
        destroyGrid();
    }
    
    private final void destroyWords() {
        if (words == null) {
            return;
        }
        for (final Word word : words) {
            if (word == null) {
                continue;
            }
            Panctor.destroy(word.text);
        }
        words = null;
    }
    
    private final void destroyGrid() {
        if (grid == null) {
            return;
        }
        for (final Letter[] row : grid) {
            if (row == null) {
                continue;
            }
            for (final Letter letter : row) {
                Panctor.destroy(letter);
            }
        }
        grid = null;
    }
    
    private final void loadWordFile() throws Exception {
        BufferedReader in = null;
        try {
            in = openWordFileReader();
            String word;
            while ((word = in.readLine()) != null) {
                
            }
        } finally {
            Iotil.close(in);
        }
    }
    
    private final static byte MODE_UNUSED = 0;
    private final static byte MODE_ACTIVE = 1;
    private final static byte MODE_USED = 2;
    
    private final class Letter extends Panctor {
        private final int row;
        private final int col;
        private final char c;
        private final TouchButton button;
        private byte mode = MODE_UNUSED;
        
        private Letter(final int row, final int col, final char c) {
            this.row = row;
            this.col = col;
            this.c = c;
            inactivate();
            room.addActor(this);
            final Letter[] gridRow = grid[row];
            Panctor.destroy(gridRow[col]);
            gridRow[col] = this;
            final Pangine engine = Pangine.getEngine();
            final int x = col * DIM, y = engine.getEffectiveHeight() - (row + 1) * DIM;
            getPosition().set(x, y);
            button = new TouchButton(engine.getInteraction(), "Letter." + row + "." + col, x, y, DIM, DIM);
            engine.registerTouchButton(button);
            register(button, new ActionStartListener() {
                @Override public final void onActionStart(final ActionStartEvent event) {
                    onTap();
                }});
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
    
    private final class Word {
        private final String value;
        private final Pantext text;
        private final StringBuilder b;
        
        private Word(final String value) {
            this.value = value;
            b = new StringBuilder();
            Chartil.appendMulti(b, '.', value.length());
            text = new Pantext(Pantil.vmid(), FurGuardiansGame.fontTiny, b);
            text.getPosition().set(DIM * SIZE + 1, Pangine.getEngine().getEffectiveHeight() - (words.size() + 1) * DIM + 5);
            room.addActor(text);
            words.add(this);
        }
    }
    
    private final static boolean isVowel(final char _c) {
        final char c = java.lang.Character.toLowerCase(_c);
        return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u' || c == 'y';
    }
    
    private final static BufferedReader openWordFileReader() {
        return Iotil.getBufferedReader(FurGuardiansGame.RES + "text/words.txt");
    }
    
    private final static void validateWordFile() throws Exception {
        BufferedReader in = null;
        try {
            in = openWordFileReader();
            String prev = null, word;
            final Set<java.lang.Character> vowels = new HashSet<java.lang.Character>(3);
            while ((word = in.readLine()) != null) {
                final int size = word.length();
                if (size < 3) {
                    throw new Exception(word + " was less than 3 letters");
                } else if (size > 5) {
                    throw new Exception(word + " was more than 5 letters");
                } else if (prev != null) {
                    if (prev.compareTo(word) >= 0) {
                        throw new Exception(word + " should not follow " + prev);
                    }
                    final char p0 = prev.charAt(0), ex = (char) (p0 + 1), w0 = word.charAt(0);
                    if ((w0 != p0) && (ex != 'x') && (w0 != ex)) {
                        throw new Exception("Letter " + p0 + " was followed by " + w0 + " instead of " + ex);
                    }
                }
                vowels.clear();
                for (int i = 0; i < size; i++) {
                    final char c = word.charAt(i);
                    if ((c < 'a') || (c > 'z')) {
                        throw new Exception(word + " contains " + c);
                    } else if (isVowel(c)) {
                        vowels.add(java.lang.Character.valueOf(c));
                        if (vowels.size() > 2) {
                            throw new Exception(word + " contained " + vowels);
                        }
                    }
                }
                //tODO Check that it's possible to find 3 other words to use with this word
                prev = word;
            }
        } finally {
            Iotil.close(in);
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
