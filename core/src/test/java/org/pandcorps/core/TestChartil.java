/*
Copyright (c) 2009-2021, Andrew M. Martin
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
package org.pandcorps.core;

import org.pandcorps.test.*;

public class TestChartil extends Pantest {
    public final void testToUpperCaseString() {
        runToUpperCaseString(null, null);
        runToUpperCaseString("", "");
        runToUpperCaseString(" ", " ");
        runToUpperCaseString("A", "A");
        runToUpperCaseString("A", "a");
        runToUpperCaseString("  ", "  ");
        runToUpperCaseString("BC", "BC");
        runToUpperCaseString("BC", "Bc");
        runToUpperCaseString("BC", "bC");
        runToUpperCaseString("BC", "bc");
        runToUpperCaseString("   ", "   ");
        runToUpperCaseString("DEF", "DEF");
        runToUpperCaseString("DEF", "DeF");
        runToUpperCaseString("DEF", "def");
    }
    
    private final void runToUpperCaseString(final String ex, final String raw) {
        assertEquals(ex, Chartil.toUpperCase(raw));
    }
    
    public final void testToUpperCaseChar() {
        runToUpperCaseChar(' ', ' ');
        runToUpperCaseChar('A', 'A');
        runToUpperCaseChar('A', 'a');
        runToUpperCaseChar('Z', 'z');
    }
    
    private final void runToUpperCaseChar(final char ex, final char raw) {
        assertEquals(ex, Chartil.toUpperCase(raw));
    }
    
    public final void testIsUpperCase() {
        runIsUpperCase(false, ' ');
        runIsUpperCase(false, 'a');
        runIsUpperCase(false, '1');
        runIsUpperCase(false, '.');
        runIsUpperCase(false, '!');
        runIsUpperCase(true, 'A');
        runIsUpperCase(true, 'B');
        runIsUpperCase(true, 'Y');
        runIsUpperCase(true, 'Z');
    }
    
    private final void runIsUpperCase(final boolean ex, final char c) {
        assertEquals(ex, Chartil.isUpperCase(c));
    }
    //
    public final void testToLowerCaseString() {
        runToLowerCaseString(null, null);
        runToLowerCaseString("", "");
        runToLowerCaseString(" ", " ");
        runToLowerCaseString("a", "a");
        runToLowerCaseString("a", "A");
        runToLowerCaseString("  ", "  ");
        runToLowerCaseString("bc", "bc");
        runToLowerCaseString("bc", "bC");
        runToLowerCaseString("bc", "Bc");
        runToLowerCaseString("bc", "BC");
        runToLowerCaseString("   ", "   ");
        runToLowerCaseString("def", "def");
        runToLowerCaseString("def", "dEf");
        runToLowerCaseString("def", "DeF");
    }
    
    private final void runToLowerCaseString(final String ex, final String raw) {
        assertEquals(ex, Chartil.toLowerCase(raw));
    }
    
    public final void testToLowerCaseChar() {
        runToLowerCaseChar(' ', ' ');
        runToLowerCaseChar('a', 'a');
        runToLowerCaseChar('a', 'A');
        runToLowerCaseChar('z', 'Z');
    }
    
    private final void runToLowerCaseChar(final char ex, final char raw) {
        assertEquals(ex, Chartil.toLowerCase(raw));
    }
    
    public final void testIsLowerCase() {
        runIsLowerCase(false, ' ');
        runIsLowerCase(false, 'A');
        runIsLowerCase(false, '1');
        runIsLowerCase(false, '.');
        runIsLowerCase(false, '!');
        runIsLowerCase(true, 'a');
        runIsLowerCase(true, 'b');
        runIsLowerCase(true, 'y');
        runIsLowerCase(true, 'z');
    }
    
    private final void runIsLowerCase(final boolean ex, final char c) {
        assertEquals(ex, Chartil.isLowerCase(c));
    }
}
