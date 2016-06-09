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

import java.io.BufferedReader;
import java.util.HashSet;
import java.util.Set;

import org.pandcorps.core.Iotil;
import org.pandcorps.test.*;

public class TestWordScreen extends Pantest {
    public final void testSkip() {
        runSkip(false, "abcdefghijklmnop");
        runSkip(true, "abzdkcufihylmnop");
        runSkip(true, "cbzdufghnhkltyop");
        runSkip(true, "nbcymfghahkldzop");
        runSkip(true, "sbcdehgyzhilmnot");
        runSkip(true, "kbqwecgyzhilmnod");
        runSkip(true, "dbcaefgsihksmnop");
        runSkip(true, "abdcefguihkmznop");
        runSkip(true, "abdcefgoihzcmnok");
        runSkip(true, "abcnefyizjkgmnog");
        runSkip(true, "ybcfezxaijkgmnop");
        runSkip(true, "zbcdefgaijksmnos");
        runSkip(true, "zbcdefgsijksmnoa");
        runSkip(true, "zbcsefgsijkamnop");
        runSkip(true, "abcdpmipyjklmnoz");
        runSkip(true, "aycdgnobijklmzop");
        runSkip(true, "abcdssipzjklmnop");
        runSkip(true, "abcdllehijkzmnop");
        runSkip(true, "abcdnropijklmxyz");
    }
    
    public final void testSkip5() {
        runSkip(false, "abcdefghijklmnopqrstuvwxy");
        runSkip(true, "afzdehctibklmnopqrsguvwxy");
        runSkip(true, "abczeodlidkgmnfpqrstuvwxy");
        runSkip(true, "abcdzsinepklmgofqrhtuvwxy");
        runSkip(true, "zbssafghijklmnopqrctuvwxy");
        runSkip(true, "zssaefghijklmnopqrctuvwxy");
        runSkip(true, "ssadefghijklmnopqrctuvwxy");
        runSkip(true, "akcufzghijblmnopqrstevwxy");
        runSkip(true, "kcufezghijblmnopqrstavwxy");
        runSkip(true, "abcdnfmhzjreggipqostuvwxy");
    }
    
    private final void runSkip(final boolean ex, final String grid) {
        assertEquals(ex, WordScreen.isSkipped(toGrid(grid)));
    }
    
    public final void testSkip2() {
        runSkip2(false, "abcdefghijklmnop");
        runSkip2(true, "wxyzefghijklmnop");
        runSkip2(true, "abcdwxyzijklmnop");
        runSkip2(true, "abcdefghwxyzmnop");
        runSkip2(true, "abcdefghijklwxyz");
        runSkip2(true, "zyxwefghijklmnop");
        runSkip2(true, "abcdzyxwijklmnop");
        runSkip2(true, "abcdefghzyxwmnop");
        runSkip2(true, "abcdefghijklzyxw");
        runSkip2(true, "wbcdxfghyjklznop");
        runSkip2(true, "awcdexghiyklmzop");
        runSkip2(true, "abwdefxhijylmnzp");
        runSkip2(true, "abcwefgxijkymnoz");
        runSkip2(true, "zbcdyfghxjklwnop");
        runSkip2(true, "azcdeyghixklmwop");
        runSkip2(true, "abzdefyhijxlmnwp");
        runSkip2(true, "abczefgyijkxmnow");
        runSkip2(true, "wbcdexghijylmnoz");
        runSkip2(true, "zbcdeyghijxlmnow");
        runSkip2(true, "abcwefxhiyklznop");
        runSkip2(true, "abczefyhixklwnop");
    }
    
    private final void runSkip2(final boolean ex, final String grid) {
        assertEquals(ex, WordScreen.isSkipped(toGrid(grid), "VWXY"));
    }
    
    private final char[][] toGrid(final String grid) {
        final int len = grid.length();
        final int size = (int) Math.round(Math.sqrt(len));
        final char[][] g = new char[size][size];
        int row = 0, col = 0;
        for (int i = 0; i < len; i++) {
            g[row][col] = java.lang.Character.toUpperCase(grid.charAt(i));
            col++;
            if (col >= size) {
                col = 0;
                row++;
            }
        }
        return g;
    }
    
    public final void testWordFile() throws Exception {
        runWordFile("words", 3, 5, 2, true);
    }
    
    public final void testLongWordFile() throws Exception {
        runWordFile("words2", 6, 8, 1, false);
    }
    
    public final void testShortWordFile() throws Exception {
        runWordFile("wordsShort", 2, 2, 1, false);
    }
    
    private final void runWordFile(final String name, final int min, final int max, final int maxVowels, final boolean checkMissing) throws Exception {
        //TODO Long words must have at least one duplicate letter
        BufferedReader in = null;
        try {
            in = WordScreen.openWordFileReader(name);
            String prev = null, word;
            final Set<java.lang.Character> vowels = new HashSet<java.lang.Character>(3);
            while ((word = in.readLine()) != null) {
                final int size = word.length();
                assertTrue(word + " was less than " + min + " letters", size >= min);
                assertTrue(word + " was more than " + max + " letters", size <= max);
                if (prev != null) {
                    assertTrue(word + " should not follow " + prev, prev.compareTo(word) < 0);
                    final char p0 = prev.charAt(0), ex = (char) (p0 + 1), w0 = word.charAt(0);
                    if (checkMissing && (w0 != p0) && (ex != 'x')) {
                        assertEquals("Letter " + p0 + " was followed by " + w0 + " instead of " + ex, w0, ex);
                    }
                }
                vowels.clear();
                for (int i = 0; i < size; i++) {
                    final char c = word.charAt(i);
                    assertTrue(word + " contains " + c, (c >= 'a') && (c <= 'z'));
                    if (WordScreen.isVowel(c)) {
                        vowels.add(java.lang.Character.valueOf(c));
                        assertTrue(word + " contained " + vowels, vowels.size() <= maxVowels);
                    }
                }
                //TODO Check that it's possible to find 3 other words to use with this word
                prev = word;
            }
        } finally {
            Iotil.close(in);
        }
    }
}
