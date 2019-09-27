/*
Copyright (c) 2009-2018, Andrew M. Martin
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
package org.pandcorps.botsnbolts;

import javax.sound.midi.*;

import org.pandcorps.core.*;

public class BotsnBoltsMusic extends Mustil {
    private final static String COPYRIGHT = "Copyright (c) " + BotsnBoltsGame.YEAR + ", " + BotsnBoltsGame.AUTHOR;
    
    private final static int BG = PRG_SQUARE, BG2 = PRG_SAWTOOTH;
    private final static int FG = PRG_CLAVINET; // PRG_NEW_AGE, PRG_ELECTRIC_PIANO_2
    private final static int CHCK = PRC_CLOSED_HI_HAT, DRUM = PRC_BASS_DRUM_2;
    private final static int VOL_BG = 56;
    private final static int VOL_FG = 64;
    
    private static int arg = 0;
    
    static {
        initSong();
    }
    
    private final static void initSong() {
        sequenceResolution = 128;
        durationSameAsDelta = true;
        volPercussion = 48;
        whiteKeyMode = true;
    }
    
    protected final static Song newSongVolcano() throws Exception {
        final Song song = newSong("VolcanoBot");
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        deltaTick = 4;
        setInstrument(track, channel, BG);
        final int reps = 24;
        final int n = 21, h = 28;
        addRepeatedNotes(track, 0, channel, vol, deltaTick, reps, n, n, -1, -1, h, -1, n, n, n, n, -1, -1, h, -1, -1, -1);
        
        final int p = CHCK, p2 = DRUM;
        addRepeatedPercussions(track, 0, deltaTick, reps * 2, p, -1, p2, -1, p, p, -1, -1);
        
        channel = 1;
        vol = VOL_FG;
        setInstrument(track, channel, FG);
        final int off = 0;
        final int baseNote = 28;
        for (int i = 0; i < 4; i++) {
            final int o = baseNote + ((i < 2) ? 0 : 3);
            final int n1 = o + 4, n2 = o + 3, n3 = o + 2, n4 = o + 1, n5 = o;
            final int start = off + (i * 256);
            addNote(track, start, 16, channel, n1, vol);
            addNote(track, start + 32, 16, channel, n2, vol);
            addNote(track, start + 64, 8, channel, n1, vol);
            addNote(track, start + 72, 8, channel, n1, vol);
            addNote(track, start + 80, 8, channel, n2, vol);
            addNote(track, start + 88, 8, channel, n2, vol);
            addNote(track, start + 96, 8, channel, n3, vol);
            addNote(track, start + 104, 8, channel, n3, vol);
            addNote(track, start + 112, 4, channel, n4, vol);
            addNote(track, start + 116, 4, channel, n5, vol);
            addNote(track, start + 128, 16, channel, n1, vol);
            addNote(track, start + 160, 16, channel, n2, vol);
            if ((i % 2) == 0) {
                addNote(track, start + 192, 16, channel, n1, vol);
                addNote(track, start + 208, 16, channel, n2, vol);
                addNote(track, start + 224, 16, channel, n3, vol);
            } else {
                addNote(track, start + 192, 16, channel, n3, vol);
            }
        }
        deltaTick = 32;
        final int o = baseNote;
        final int n1 = o + 3, n2 = o + 2, n3 = o + 1, n4 = o, n5 = o - 1, n6 = o - 2;
        addNotes(track, off + 4 * 256, channel, vol, deltaTick, n1, n3, n2, n4, n3, n5, n4, n6, n1, n3, n2, n4, n3, n5);
        deltaTick = 16;
        addNotes(track, off + 4 * 256 + 14 * 32, channel, vol, deltaTick, n3, n5, n4, n6);
        
        return song;
    }
    
    protected final static Song newSongLightning() throws Exception {
        final Song song = newSong("LightningBot");
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        deltaTick = 4;
        setInstrument(track, channel, BG);
        final int size = 1536, reps = 6;
        final int m = 21, l = 20, h = 22;
        addRepeatedNotes(track, 0, channel, vol, deltaTick, reps, h, -1, h, -1, h, h, h, -1, h, h, h, -1, h, -1, h, -1,
            m, -1, m, -1, m, m, m, -1, m, m, m, -1, m, -1, m, -1,
            l, -1, l, -1, l, l, l, -1, l, l, l, -1, l, -1, l, -1,
            m, -1, m, -1, m, m, m, -1, m, m, m, -1, m, -1, m, -1);
        addNote(track, 1536 - deltaTick, deltaTick, channel, m, SILENT);
        
        final int p = CHCK, p2 = DRUM;
        addPercussionsUntil(track, 0, deltaTick, size, p, -1, p, p, p, -1, p, p, p, p, p, -1, p, -1, p2, -1);
        
        channel = 1;
        vol = VOL_FG;
        setInstrument(track, channel, FG);
        int n0 = 28, n1 = n0 + 1, n2 = n1 + 1, n3 = n2 + 1, n4 = n3 + 1, n5 = n4 + 1, n6 = n5 + 1;
        next = 0;
        addLightning64(n4); addLightning64(n3); addLightning64(n2); addLightning64(n3);
        
        addLightning48(n4);
        addNotes(track, next, channel, vol, 8, n2, n2);
        addLightning48(n3);
        addNote(track, next, 16, channel, n1, vol);
        addLightning48(n2);
        addNotes(track, next, channel, vol, 8, n2, n2);
        addLightning64(n3);
        
        addLightning48(n4);
        addNotes(track, next, channel, vol, 8, n2, n2);
        addLightning32(n3);
        addNotes(track, next, channel, vol, 8, n2, n2);
        addNote(track, next, 16, channel, n3, vol);
        addLightning48(n4);
        addNotes(track, next, channel, vol, 8, n4, n4);
        addLightning64(n5);
        
        addLightning48(n6);
        addNotes(track, next, channel, vol, 8, n4, n4);
        addLightning48(n5);
        addNote(track, next, 16, channel, n4, vol);
        addNotes(track, next, channel, vol, 8, n5, n5, n6, n6, n5, n5, n4, n4);
        addNote(track, next, 32, channel, n5, vol);
        for (int i = 0; i < 2; i++) {
            addNotes(track, next, channel, vol, 8, n3, n3);
            addNote(track, next, 16, channel, n2, vol);
            addLightning64(n1, vol); addLightning64(n1, vol - 12); addLightning64(n1, vol - 24); addLightning32(n1, vol - 36);
        }
        
        return song;
    }
    
    private final static void addLightning64(final int n) throws Exception {
        addLightning64(n, vol);
    }
    
    private final static void addLightning64(final int n, final int vol) throws Exception {
        composeAtVolume(vol, 16, n, 4, n, 4, n, 8, n, 4, n, 4, n, 8, n, 16, n);
    }
    
    private final static void addLightning48(final int n) throws Exception {
        compose(16, n, 4, n, 4, n, 24, n);
    }
    
    private final static void addLightning32(final int n) throws Exception {
        addLightning32(n, vol);
    }
    
    private final static void addLightning32(final int n, final int vol) throws Exception {
        composeAtVolume(vol, 16, n, 4, n, 4, n, 8, n);
    }
    
    protected final static Song newSongHail() throws Exception {
        final Song song = newSong("HailBot");
        return song;
    }
    
    protected final static Song newSongRockslide() throws Exception {
        final Song song = newSong("RockslideBot");
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        deltaTick = 4;
        setInstrument(track, channel, BG);
        final int reps = 16;
        final int m = 21, h = 22;
        addRepeatedNotes(track, 0, channel, vol, deltaTick, reps, m, -1, h, -1, m, -1, -1, -1, m, -1, h, -1, h, h, -1, -1);
        
        final int p = CHCK, p2 = DRUM;
        addRepeatedPercussions(track, 0, deltaTick, reps, p, p, -1, -1, p2, -1, -1, -1, p, p, -1, -1, p, p, -1, -1);
        
        channel = 1;
        vol = VOL_FG;
        setInstrument(track, channel, FG);
        final int baseNote = 28;
        next = 0;
        for (int i = 0; i < 7; i++) {
            if (i == 3) {
                continue;
            }
            final int i2 = i % 2, o = baseNote - ((i2 == 0) ? 0 : 2), n2 = o + 2, n3 = o + 3, n4 = o + 4;
            final int nf = n3 + ((i < 4) ? 0 : 2), ns = nf - 1;
            addNotes(track, next, channel, vol, 16, nf, ns);
            addNotes(track, next, channel, vol, 8, n3, n3);
            final int nl;
            if ((i == 0) || (i == 1) || (i == 4) || (i == 5)) {
                nl = n2;
            } else if ((i == 2) || (i == 3)) {
                nl = n4;
            } else {
                nl = n4;
            }
            addNote(track, next, 8, channel, nl, vol);
            addNotes(track, next, channel, vol, 4, nl, nl);
            if (i == 2) {
                addNotes(track, next, channel, vol, 16, nl, nl, nl, nl);
                continue;
            }
        }
        final int o = baseNote, nn = o - 1, n0 = o, n1 = o + 1, n2 = o + 2, n3 = o + 3, n4 = o + 4, n5 = o + 5;
        addNote(track, next, 32, channel, n5, vol);
        addNote(track, next, 8, channel, n4, vol);
        addNotes(track, next, channel, vol, 4, n4, n4);
        addNote(track, next, 16, channel, n5, vol);
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < 3; i++) {
                final int n4i = n4 - i;
                addNote(track, next, 16, channel, n4i, vol);
                addNotes(track, next, channel, vol, 8, n4i, n4i);
                addNotes(track, next, channel, vol, 16, n3 - i, n2 - i);
            }
            if (j == 0) {
                addNotes(track, next, channel, vol, 4, n1, n1);
                addNotes(track, next, channel, vol, 8, n1, n1, n1);
                addNotes(track, next, channel, vol, 4, n2, n2);
                addNote(track, next, 8, channel, n2, vol);
                addNote(track, next, 16, channel, n3, vol);
            } else {
                addNotes(track, next, channel, vol, 16, n1, n1);
                addNotes(track, next, channel, vol, 8, n0, n0);
                addNote(track, next, 16, channel, nn, vol);
            }
        }
        
        return song;
    }
    
    protected final static Song newSongEarthquake() throws Exception {
        final Song song = newSong("EarthquakeBot");
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        setInstrument(track, channel, BG);
        final int size = 1536;
        final int m = 21, h = 22;
        composeUntil(size, 4, m, 4, -1, 4, -1, 4, h, 4, -1, 4, -1, 4, m, 4, -1);
        
        final int p = CHCK, p2 = DRUM;
        addPercussionsUntil(track, 0, 4, size, p, p2, p, -1, p, p, p2, -1);
        
        channel = 1;
        vol = VOL_FG;
        next = 0;
        setInstrument(track, channel, FG);
        final int o = 28;
        final int n2 = o + 2, n3 = o + 3, n4 = o + 4;
        final int d1 = 4;
        final int d2 = d1 * 2, d4 = d1 * 4;
        for (int i = 0; i < 6; i++) {
            if (i == 0 || i == 2) {
                addEcho(d2, n4, 6, 4); addEcho(d2, n3, 6, 4);
            } else if (i == 1) {
                addEcho(d4, n4, 6, 2); addEcho(d4, n3, 6, 2);
            } else if (i == 3) {
                addEcho(d2, n2, 6, 4); addEcho(d2, n3, 6, 4);
            } else if (i == 4) {
                addEcho(d2, n4, 6, 6); compose(d4, n3);
            } else if (i == 5) {
                addEcho(d2, n2, 6, 6); compose(d4, n3);
            }
            if (i == 3) {
                addEcho(d2, n4, 6, 8);
                addEcho(d2, n4, 6, 4); addEcho(d2, n3, 6, 4);
                addEcho(d2, n2, 6, 8);
            } else {
                addEcho(d2, n2, 6, 8);
                addEcho(d2, n4, 6, 6); compose(d4, n3);
                addEcho(d2, n2, 6, 8);
            }
        }
        
        return song;
    }
    
    protected final static Song newSongCyclone() throws Exception {
        final Song song = newSong("CycloneBot");
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        setInstrument(track, channel, BG);
        final int d1 = 4, d2 = d1 * 2, d3 = d1 * 3, d4 = d1 * 4, d6 = d1 * 6;
        final int m = 21, h = 22;
        final int size = 1536;
        composeUntil(size, d1, m, d1, m, d2, h);
        
        final int p = CHCK, p2 = DRUM;
        addPercussionsUntil(track, 0, d2, size, p, p2); //TODO Fast group at end
        
        channel = 1;
        vol = VOL_FG;
        next = 0;
        setInstrument(track, channel, FG);
        final int baseNote = 28;
        final int o = baseNote;
        final int n0 = o, n1 = o + 1, n2 = o + 2, n3 = o + 3, n4 = o + 4, n5 = o + 5;
        for (int i = 0; i < 2; i++) {
            compose(d2, n2, d2, n2, d2, n2, d2, n2, d2, n2, d2, -1, d2, n1, d1, n1);
            compose(d3, n0, d2, n0, d2, n0, d2, n0, d2, n0, d6, -1);
            if (i == 0) {
                //compose(d2, n1, d1, n1, d3, n2, d2, n2, d2, n2, d2, n2, d2, n2, d2, -1, d2, n1, d2, n1); // Too busy, remove intro notes
                //compose(d4, -1, d2, n2, d2, n2, d2, n2, d2, n2, d2, n2, d2, -1, d2, n1, d2, n1); // This and else start with same pause, move out of condition
                compose(d2, n2, d2, n2, d2, n2, d2, n2, d2, n2, d2, -1, d2, n1, d2, n1);
                compose(d4, n2, d4, n3, d4, n4, d4, n5);
            } else {
                //compose(d4, -1, d4, n1, d4, n2, d2, n3, d2, n3, d4, n2);
                compose(d4, n1, d4, n2, d2, n3, d2, n3, d4, n2);
                compose(d4, n1, d4, n1, d4, n0, d4, -1);
            }
        }
        for (int i = 0; i < 4; i++) {
            compose(d2, n2, d2, n2, d2, n2, d2, n2, d2, n2, d4, -1);
            if (i == 1) {
                compose(d1, n3, d3, n4, d2, n4, d2, n4, d2, n4, d2, n4, d6, -1);
            } else {
                compose(d1, n1, d3, n0, d2, n0, d2, n0, d2, n0, d2, n0, d6, -1);
            }
        }
        final int xm = n5, xh = xm + 1, xl = xm - 1;
        for (int i = 0; i < 2; i++) {
            compose(d4, xm, d4, xm, d1, xm, d1, xm, d2, xm, d4, xm);
            compose(d2, xl, d2, xl, d4, xl, d1, xl, d1, xl, d2, xl, d2, xl, d2, -1);
            compose(d4, xm, d4, xm, d1, xm, d1, xm, d2, xm, d4, xm);
            compose(d2, xh, d2, xh, d1, xh, d1, xh, d2, xh, d4, xh, d4, -1);
        }
        /*for (int i = 0; i < 2; i++) {
            compose(d4, n2, d4, n2, d4, n3, d4, n3);
            compose(d2, n4, d2, n4, d1, n3, d1, n3, d2, n4, d4, n4, d4, -1);
            compose(d4, n3, d4, n3, d4, n2, d4, n2);
            compose(d2, n1, d2, n1, d1, n2, d1, n2, d2, n1, d4, n1, d4, -1);
        }*/
        
        return song;
    }
    
    protected final static Song newSongFlood() throws Exception {
        final Song song = newSong("FloodBot");
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        deltaTick = 4;
        setInstrument(track, channel, BG);
        final int size = 1536;
        final int n = 21, h = 28;
        //addRepeatedNotes(track, 0, channel, vol, deltaTick, reps, n, n, n, -1, n, -1, -1, -1, n, n, n, -1, -1, -1, -1, -1); // might work, untested
        //n, n, n, -1, n, n, -1, -1, n, n, n, -1, -1, -1, -1, -1
        
        final int p = CHCK, p2 = DRUM;
        addPercussionsUntil(track, 0, 8, size, p);
        
        channel = 1;
        vol = VOL_FG;
        setInstrument(track, channel, FG);
        final int baseNote = 28;
        next = 0;
        for (int i = 0; i < 16; i++) {
            final boolean low = (i % 8) < 4;
            final boolean slow = i < 8;
            final int o = baseNote + (low ? 0 : 2);
            final int n0 = o, n1 = o + 1, n2 = o + 2;
            if (slow) {
                addNotes(16, n1, n0, n1);
            } else {
                compose(16, n1, 4, n0, 12, n0, 16, n1);
            }
            final int nl = ((i % 2) == 0) ? n2 : n0;
            addNote(4, nl);
            if (slow) {
                addNote(12, nl);
            } else {
                addNote(4, nl);
                addNote(8, nl);
            }
        }
        final int o = baseNote + 2;
        final int nn = o - 1, n0 = o, n1 = o + 1, n2 = o + 2, n3 = o + 3, n4 = o + 4;
        /*for (int i = 0; i < 8; i++) {
            final int b = (i < 4) ? nn : n1;
            addNotes(track, next, channel, vol, 8, b, b, b); // 24
            addNotes(track, next, channel, vol, 4, b, b); // 8
            addNote(track, next, 32, channel, b + (((i % 2) == 0) ? 1 : -1), vol);
        }*/
        for (int i = 0; i < 4; i++) {
            final int i2 = i % 2;
            addNotes(16, n2, n2);
            addNotes(8, n1, n1, n1);
            addNote(4, n2);
            addNote(3, n2);
            next += 1;
            if (i2 == 0) {
                addNotes(16, n1, n1);
                addNotes(8, n0, n0, n0, n0);
            } else if (i == 1) {
                addNotes(16, n3, n3, n4, n4);
            } else if (i == 3) {
                addNotes(track, next, channel, vol, 16, n1, n0, nn);
                addNote(track, next, 16, channel, n1, SILENT);
            }
        }
        
        return song;
    }
    
    protected final static Song newSongDrought() throws Exception {
        final Song song = newSong("DroughtBot");
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        deltaTick = 4;
        setInstrument(track, channel, BG);
        final int reps = 24;
        final int n = 21, h = 28, l = 20;
        addRepeatedNotes(track, 0, channel, vol, deltaTick, reps, n, n, n, -1, h, -1, n, -1, n, n, n, -1, l, l, -1, -1);
        addNote(track, (reps * deltaTick * 16) - (deltaTick * 2), deltaTick * 2, channel, n, SILENT);
        
        final int p = CHCK;
        addRepeatedPercussions(track, 0, deltaTick, reps * 2, p, p, -1, -1, p, -1, p, -1);
        
        channel = 1;
        vol = VOL_FG;
        setInstrument(track, channel, FG);
        final int baseNote = 42;
        final int n1 = baseNote, n2 = baseNote + 1, n3 = baseNote + 2;
        for (int i = 0; i < 8; i++) {
            final int start = i * 128;
            final int i4 = i % 4;
            if (i4 == 1) {
                addDroughtNote(track, start - 16, 16, n2);
            } else if (i4 == 3) {
                addDroughtNote(track, start - 16, 8, n2);
                addDroughtNote(track, start - 8, 8, n1);
            }
            addDroughtNote(track, start, 32, n3);
            if ((i4 % 2) == 0) {
                addDroughtNote(track, start + 48, 8, n2);
                addDroughtNote(track, start + 56, 8, n1);
                addDroughtNote(track, start + 64, 16, n3);
                addDroughtNote(track, start + 80, 16, n2);
                addDroughtNote(track, start + 96, 16, n1);
            } else if (i4 == 1) {
                addDroughtNote(track, start + 48, 16, n2);
                addDroughtNote(track, start + 64, 16, n1);
                addDroughtNote(track, start + 112, 16, n2);
            } else if (i4 == 3) {
                addDroughtNote(track, start + 48, 16, n2);
                addDroughtNote(track, start + 64, 16, n1);
            }
        }
        final int start = 128 * 8;
        addNote(track, start, 32, channel, n3, vol);
        addNote(track, start + 48, 16, channel, n2, vol);
        addNote(track, start + 64, 32, channel, n3, vol);
        addNote(track, start + 128, 32, channel, n3, vol);
        addNote(track, start + 176, 8, channel, n2, vol);
        addNote(track, start + 184, 8, channel, n1, vol);
        addNote(track, start + 192, 32, channel, n2, vol);
        addNote(track, start + 256, 32, channel, n3, vol);
        addNote(track, start + 304, 8, channel, n1, vol);
        addNote(track, start + 312, 8, channel, n2, vol);
        addNote(track, start + 320, 32, channel, n3, vol);
        addNote(track, start + 368, 16, channel, n2, vol);
        addNote(track, start + 384, 32, channel, n3, vol);
        addNote(track, start + 432, 16, channel, n2, vol);
        addNote(track, start + 448, 32, channel, n1, vol);
        
        return song;
    }
    
    private final static void addDroughtNote(final Track track, final int tick, final int dur, final int key) throws Exception {
        if (tick < 512) {
            addNote(track, tick, dur, channel, key, vol);
        } else {
            final int dur4 = dur / 4;
            for (int i = 0; i < 4; i++) {
                addNote(track, tick + (i * dur4), dur4, channel, key, vol);
            }
        }
    }
    
    protected final static Song newSongCity() throws Exception {
        final Song song = newSong("City");
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        deltaTick = 4;
        setInstrument(track, channel, BG);
        final int reps = 24;
        final int n = 21;
        addRepeatedNotes(track, 0, channel, vol, deltaTick, reps, n, -1, n, -1, n, -1, n, -1, n, -1, n, -1, -1, n, n, -1);
        addNote(track, (reps * deltaTick * 16) - deltaTick, deltaTick, channel, n, SILENT);
        
        final int p = CHCK, p2 = DRUM;
        addRepeatedPercussions(track, 0, deltaTick, reps, p, -1, -1, -1, p2, -1, -1, -1, p, p, -1, -1, p2, -1, -1, -1);
        
        channel = 1;
        vol = VOL_FG;
        setInstrument(track, channel, FG);
        final int o = 28;
        final int n0 = o, n1 = o + 1, n2 = o + 2, n3 = o + 3, n4 = o + 4;
        next = 0;
        for (int i = 0; i < 2; i++) {
            addNotes(track, next, channel, vol, 32, n4, n3);
            addNote(track, next, 8, channel, n4, vol);
            addCityNote(track, 8, n3, i);
            addNotes(track, next, channel, vol, 8, n2, n1);
            addNotes(track, next, channel, vol, 16, n2, n1);
            
            addNotes(track, next, channel, vol, 32, n4, n3);
            addCityNote(track, 16, n4, i);
            addNote(track, next, 16, channel, n3, vol);
            addNote(track, next, 32, channel, n2, vol);
            
            addNotes(track, next, channel, vol, 32, n4, n3);
            addNote(track, next, 8, channel, n4, vol);
            addCityNote(track, 8, n4, i);
            addNotes(track, next, channel, vol, 8, n3, n3, n2, n2);
            addNote(track, next, 16, channel, n1, vol);
            
            addNote(track, next, 32, channel, n2, vol);
            addNotes(track, next, channel, vol, 16, n1, n1);
            addNote(track, next, 32, channel, n2, vol);
            next += 32;
        }
        addNotes(track, next, channel, vol, 32, n4, n3, n2, n1);
        
        addNotes(track, next, channel, vol, 16, n4, n4, n3, n3, n2, n2);
        addNote(track, next, 32, channel, n1, vol);
        
        addNotes(track, next, channel, vol, 32, n4, n3);
        addNotes(track, next, channel, vol, 16, n2, n2, n1, n1);
        
        addNote(track, next, 32, channel, n0, vol);
        addNotes(track, next, channel, vol, 16, n1, n1);
        addNote(track, next, 32, channel, n0, vol);
        
        return song;
    }
    
    private final static void addCityNote(final Track track, final int dur, final int key, final int i) throws Exception {
        if (i == 0) {
            addNote(track, next, dur, channel, key, vol);
        } else {
            final int dur2 = dur / 2;
            for (int j = 0; j < 2; j++) {
                addNote(track, next, dur2, channel, key, vol);
            }
        }
    }
    
    protected final static Song newSongArray() throws Exception {
        final Song song = newSong("Array");
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        setInstrument(track, channel, BG);
        final int reps = 4;
        final int n = 21, h = 28;
        //addRepeatedNotes(track, 0, channel, vol, 8, reps, n, -1, n, -1, n, -1, n, -1, n, -1, n, -1, n, -1, h, -1);
        //addRepeatedNotes(track, 0, channel, vol, 4, reps, n, -1, -1, -1, n, -1, n, -1, n, -1, -1, -1, n, -1, -1, -1, n, -1, n, -1, n, -1, -1, -1, n, -1, -1, -1, h, h, -1, -1);
        addRepeatedNotes(track, 0, channel, vol, 4, reps, n, -1, -1, -1, n, -1, n, -1, n, -1, -1, -1, n, -1, -1, -1, n, -1, n, -1, n, -1, -1, -1, n, -1, -1, -1, n, n, -1, -1);
        
        final int p = CHCK, p2 = DRUM;
        //addRepeatedPercussions(track, 0, 4, reps, p2, -1, -1, -1, p, -1, -1, -1, p2, -1, -1, -1, p, -1, -1, -1, p2, -1, -1, -1, p, -1, -1, -1, p2, -1, -1, -1, p, p, p, -1);
        addRepeatedPercussions(track, 0, 4, reps, p, -1, p2, -1, p, -1, p2, -1, p, -1, p2, -1, p, -1, p2, -1, p, -1, p2, -1, p, -1, p2, -1, p, -1, p2, -1, p, p, p, -1);
        
        channel = 1;
        vol = VOL_FG;
        setInstrument(track, channel, FG);
        final int o = 31;
        final int n1 = o + 1, n2 = o + 2, n3 = o + 3, n4 = o + 4;
        next = 0;
        for (int i = 0; i < 2; i++) {
            addNote(track, next, 4, channel, n2, vol);
            addNote(track, next, 12, channel, n2, vol);
            addNote(track, next, 16, channel, n3, vol);
            
            addNote(track, next, 4, channel, n2, vol);
            addNote(track, next, 12, channel, n2, vol);
            addNote(track, next, 16, channel, n1, vol);
            
            addNote(track, next, 4, channel, n2, vol);
            addNote(track, next, 12, channel, n2, vol);
            addNotes(track, next, channel, vol, 8, n3, n3, n4, n4);
            addNote(track, next, 16, channel, n3, vol);
            
            addNote(track, next, 4, channel, n2, vol);
            addNote(track, next, 12, channel, n2, vol);
            addNotes(track, next, channel, vol, 8, n3, n3, n4, n4, n3, n3);
            final int nl = (i == 0) ? n2 : n4, ns = (i == 0) ? n1 : n3;
            addNote(track, next, 16, channel, nl, vol);
            
            addNote(track, next, 4, channel, ns, vol);
            addNote(track, next, 12, channel, ns, vol);
            addNote(track, next, 32, channel, nl, vol);
            //addNotes(track, next, channel, vol, 8, n2, n2);
        }
        
        //addNote(track, next, 16, channel, n1, SILENT);
        
        return song;
    }
    
    protected final static Song newSongFinal() throws Exception {
        final Song song = newSong("Final");
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        setInstrument(track, channel, BG2);
        final int reps = 24;
        final int n = 21;
        next = 0;
        final int dur = 16, dur3 = dur * 3;
        for (int i = 0; i < reps; i++) {
            addNotes(track, next, channel, vol, dur3, n, n);
            addNotes(track, next, channel, vol, dur, n, n);
        }
        
        return song;
    }
    
    protected final static Song newSongTmp() throws Exception {
        final Song song = newSong("TmpBot");
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        setInstrument(track, channel, BG);
        //TODO Double tempo?
        //final int d1 = 4, d2 = d1 * 2, d3 = d1 * 3, d4 = d1 * 4, d6 = d1 * 6, d8 = d1 * 8, d12 = d1 * 12, dg = d1 * 16;
        final int d1 = 2, d2 = d1 * 2, d3 = d1 * 3, d4 = d1 * 4, d6 = d1 * 6, d8 = d1 * 8, d12 = d1 * 12, dc = d1 * 12, dg = d1 * 16;
        final int m = 21, h = 22;
        final int size = 1024;
        //final int size = 1536;
        //composeUntil(size, );
        
        final int p = CHCK, p2 = DRUM;
        addPercussionsUntil(track, 0, d8, size, p);
        
        channel = 1;
        vol = VOL_FG;
        next = 0;
        setInstrument(track, channel, FG);
        final int baseNote = 28;
        final int o = baseNote - 1;
        final int n0 = o, n1 = o + 1, n2 = o + 2, n3 = o + 3, n4 = o + 4, n5 = o + 5;
        for (int i = 0; i < 2; i++) {
        //for (int i = 0; i < 0; i++) {
            if (i == 0) {
                //compose(d1, n2, d4, n2, d3, -1, d12, n3, d2, n2, d2, n2, d4, n3, d4, n3);
                compose(d4, n2, d1, n2, d3, -1, d12, n3, d2, n2, d2, n2, d4, n3, d4, n3);
                compose(d8, n2, d4, n3, d8, n3, d4, n2, d8, n3);
                //compose(d1, n2, d4, n2, d3, -1, d12, n3, d2, n2, d2, n2, d4, n1, d4, n1);
                compose(d4, n2, d1, n2, d3, -1, d12, n3, d2, n2, d2, n2, d4, n1, d4, n1);
            } else {
                compose(d1, n2, dg, n2, d3, -1, d2, n2, d2, n2, d4, n3, d4, n3);
                compose(dg, n2, d4, -1, d4, n2, d8, n3);
                compose(d1, n2, dg, n2, d3, -1, d2, n2, d2, n2, d4, n1, d4, n1);
            }
            compose(d1, n3, d4, n3, d3, -1, d1, n2, d4, n2, d3, -1, d1, n1, d4, n1, d3, -1, d8, -1);
System.out.println(next); //TODO another 512 of variations of this?
        }
        /*for (int i = 0; i < 2; i++) {
            compose(d8, n3, d6, n2, d1, n1, d1, n1, d4, n3, d4, n4, d4, n3, d4, n2);
            if (i == 0) {
                compose(d8, n3, d6, n2, d1, n1, d1, n1, d4, n3, d4, n3, d4, n2, d4, -1);
            } else {
                compose(d8, n3, d8, n2, d8, n1, d8, -1);
            }
        }*/
        /*for (int i = 0; i < 2; i++) {
            compose(dg, n3, dc, n2, d2, n1, d2, n1, d8, n3, d8, n4, d8, n3, d8, n2);
            if (i == 0) {
                compose(dg, n3, dc, n2, d2, n1, d2, n1, d8, n3, d8, n3, d8, n2, d8, -1);
            } else {
                compose(dg, n3, dg, n2, dg, n1, dg, -1);
            }
        }*/
        final int x1 = n1 + 2, x2 = x1 + 1, x3 = x1 + 2, x4 = x1 + 3;
        for (int i = 0; i < 2; i++) {
            compose(dg, x3, dc, x2, d2, x1, d2, x1, d8, x3, d8, x2, d8, x3, d8, x2);
            if (i == 0) {
                compose(dg, x3, dc, x2, d2, x1, d2, x1, d8, x3, d8, x3, d8, x2, d8, -1);
            } else {
                //compose(dg, x3, dg, x2, dg, x1, dg, -1);
                compose(dg, x4, dg, x2, dg, x1, dg, -1);
            }
        }
System.out.println(next);
        
        return song;
    }
    
    protected final static Song newSongTmp2() throws Exception {
        final Song song = newSong("Tmp2Bot");
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        setInstrument(track, channel, BG);
        final int d1 = 4, d2 = d1 * 2, d3 = d1 * 3, d4 = d1 * 4, d6 = d1 * 6, d8 = d1 * 8, d12 = d1 * 12, dc = d1 * 12, dg = d1 * 16;
        final int m = 21, h = 22;
        final int size = 512;
        //composeUntil(size, );
        
        final int p = CHCK, p2 = DRUM;
        addPercussionsUntil(track, 0, dg, size, p2);
        
        channel = 1;
        vol = VOL_FG;
        next = 0;
        setInstrument(track, channel, FG);
        final int baseNote = 28;
        final int o = baseNote - 1;
        final int n0 = o, n1 = o + 1, n2 = o + 2, n3 = o + 3, n4 = o + 4, n5 = o + 5;
        /*for (int i = 0; i < 4; i++) {
            compose(dg, n2, d4, n3, d4, n2, d4, n1, d4, n0);
        }
        compose(d8, n2, d4, n1, d4, n1, dg, n2);
        next += dg;*/
        for (int i = 0; i < 3; i++) {
            /*compose(d4, n2, d4, n2, d4, n3, d4, n3);
            if (i < 2) {
                compose(d8, -1, d8, n1);
            }*/
            compose(d2, n2, d2, n2, d2, n3, d2, n3);
            if (i < 2) {
                compose(d4, -1, d4, n1);
            }
        }
        
        return song;
    }
    
    protected final static Song newSongTmp3() throws Exception {
        final Song song = newSong("Tmp3Bot");
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        setInstrument(track, channel, BG);
        final int b2 = 22, b3 = 23;
        final int size = 2048;
        final int db = 7, dp = 1;
        composeUntil(size, db, b2, dp, -1, db, b3, dp, -1, db, b2, dp, -1, db, b3, dp, -1, db, -1, dp, -1, db, -1, dp, -1, db, b2, dp, -1, db, b3, dp, -1);
        
        final int p = CHCK, p2 = DRUM;
        addPercussionsUntil(track, 0, 4, size, p, -1, p, -1, p2, -1, p, -1, p2, -1, p, -1, p, p, -1, p);
        
        channel = 1;
        vol = VOL_FG;
        next = 0;
        setInstrument(track, channel, FG);
        final int baseNote = 28;
        final int o = baseNote - 1;
        final int n0 = o, n1 = o + 1, n2 = o + 2;
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < 2; i++) {
                if (j == 0) {
                    add8(n2, 0); add8(n2, 0); add8(n1, 0); add8(n0, 0);
                } else {
                    add8(n2, 0); add8(n2, 0); add8(n1, 1); add8(n0, 2);
                }
            }
            for (int i = 0; i < 2; i++) {
                add8(n2, 3); add8(n2, 4); add8(n1, 0); add8(n0, 0);
            }
        }
        
        return song;
    }
    
    private final static void add8(final int n, final int mode) throws Exception {
        final int ns = n + ((mode == 2) ? 1 : 0); 
        final int d1 = 4, d2 = d1 * 2, d4 = d1 * 4;
        if ((mode == 0) || (mode == 1) || (mode == 3)) {
            compose(d1, ns, d1, ns);
        } else if (mode == 4) {
            compose(d2, -1);
        } else {
            compose(d2, ns);
        }
        if ((mode == 3) || (mode == 4)) {
            compose(d4, n - 1);
            compose(d4, -1);
            compose(d4, n - ((mode == 3) ? 2 : 0));
            compose(d2, -1);
            return;
        }
        final int nl = n - ((mode == 1) ? 1 : 0);
        compose(d2, ns, d2, ns, d2, ns, d2, nl, d2, nl);
        if (mode != 1) {
            compose(d4, -1);
        } else {
            compose(d2, nl, d2, nl);
        }
    }
    
    protected final static Song newSongTmp4() throws Exception {
        final Song song = newSong("Tmp4Bot");
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        deltaTick = 4;
        setInstrument(track, channel, BG);
        final int size = 1536;
        final int n = 21, h = 28;
        composeUntil(size, 4, n, 4, -1, 4, h, 4, -1, 4, -1, 4, n, 4, n, 4, -1);
        
        final int p = CHCK;
        addPercussionsUntil(track, 0, deltaTick, size, p, -1, p, p, -1, p, p, -1);
        
        channel = 1;
        vol = VOL_FG;
        next = 0;
        setInstrument(track, channel, FG);
        for (int i = 0; i < 4; i++) {
            final int i2 = i % 2;
            final int ns = (i < 2) ? 31 : -1, nt = (i < 2) ? 32 : -1;
            compose(16, 31, 16, ns, 16, nt, 16, 32, 16, -1, 8, 31, 8, 31, 4, 30, 4, 30, 4, 30, 4, 30, 8, 32, 8, 32);
            if (i2 == 0) {
                compose(16, 31, 16, ns, 16, nt, 16, 32, 16, -1, 8, -1, 8, -1, 4, 30, 4, 30, 4, 30, 4, 30, 8, 32, 8, -1);
            } else {
                compose(16, 31, 16, ns, 16, nt, 16, 32, 16, -1, 16, 31, 16, 30, 16, -1);
            }
        }
        for (int i = 0; i < 2; i++) {
            addNotes(16, 31, -1, 31, -1, -1, 31, 32, -1);
            if (i == 0) {
                addNotes(16, 31, -1, 31, -1, -1, 31, 30, -1);
            } else {
                addNotes(16, 31, -1, 31, -1, 30, -1, -1, -1);
            }
        }
        addSilent(size, 4);
        
        return song;
    }
    
    protected final static Song newSongTmp5() throws Exception {
        final Song song = newSong("Tmp5Bot");
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        deltaTick = 4;
        setInstrument(track, channel, BG);
        final int size = 1536;
        final int reps = 24;
        //final int l = 20, n = 21, h = 28;
        //composeUntil(size);
        
        final int p = CHCK, p2 = DRUM;
        addPercussionsUntil(track, 0, 16, size, p2);
        
        channel = 1;
        vol = VOL_FG;
        next = 0;
        setInstrument(track, channel, FG);
        final int off = 0;
        final int baseNote = 28;
        final int d = 8;
        //final int n = 28, h = 35, s = -1;
        final int n = 28, m = 30, h = 32, s = -1;
        for (int i = 0; i < 1; i++) {
            //compose();
            addNotes(d, n, n, n, n, n, n, m, n, n, n, n, n, n, n, h, s);
            addNotes(d, n, n, n, n, n, n, m, n, n, n, n, n, n, n, h, h);
            addNotes(d, n, n, n, n, n, n, m, n, n, n, h, n, n, n, h, s);
            addNotes(d, n, n, m, n, n, n, h, n, n, h, n, h, h, s, s, s);
            addNotes(d, n, n, n, n, n, n, h, n, n, h, n, h, h, s, s, s);
            addNotes(d, n, s, n, s, n, n, h, n, n, h, n, h, h, s, s, s);
            addNotes(d, n, n, n, s, n, n, m, n, n, n, s, n, n, n, h, s);
            addNotes(d, n, n, m, s, n, n, m, s, n, h, s, n, n, n, h, s);
            addNotes(d, n, n, m, s, n, n, m, s, n, n, s, n, n, n, h, s);
            //addNotes(d, n, h, n, h, h, s, s, s, n, h, n, h, h, s, s, s);
System.out.println(next);
        }
        addSilent(size, 4);
        
        return song;
    }
    
    protected final static Song newSongRock() throws Exception {
        final Song song = newSong("RockBot");
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        setInstrument(track, channel, BG);
        //final int reps = 16;
        final int reps = 8;
        //final int n = 21, h = 28, l = 20;
        final int n = 21, h = 22, l = 20;
        addRepeatedNotes(track, 0, channel, vol, 4, reps, n, -1, n, -1, l, -1, l, -1, h, -1, h, -1, -1, -1, -1, -1);
        addNote(track, 1020, 4, channel, n, SILENT);
        
        final int p = CHCK, p2 = DRUM;
        addRepeatedPercussions(track, 0, 4, reps, p, -1, -1, -1, p, -1, -1, -1, p, -1, p2, -1, p, -1, p, -1);
        
        channel = 1;
        vol = VOL_FG;
        next = 0;
        setInstrument(track, channel, FG);
        final int n0 = 28, n1 = n0 + 1, n2 = n0 + 2;
        //compose(8, n2, 8, n2, 8, n2, 8, n2, 4, n1, 4, n2);
        compose(16, n2, 16, n2, 16, n2, 16, n2, 8, n1, 8, n2);
        next += 48;
        compose(16, n2, 16, n2, 16, n2, 16, n2, 8, n1, 8, n2);
        
        return song;
    }
    
    protected final static Song newSongBoss() throws Exception {
        final Song song = newSong("Boss");
        return song;
    }
    
    protected final static Song newSongLevelSelect() throws Exception {
        final Song song = newSong("LevelSelect");
        return song;
    }
    
    private final static Song newSong(final String name) throws Exception {
        initSong();
        return new Song(name, COPYRIGHT);
    }
    
    private final static void run() throws Exception {
        /*final Song song = newSongVolcano();
        System.out.println("Starting " + song.name);
        play(song);
        System.out.println("Started; press enter to play sound; press x and enter to stop");
        while (!Iotil.readln().equals("x")) {
            //music.playSound(jump);
        }
        stop();
        System.out.println("End");*/
        Song song = null;
        do {
            stop();
            final boolean first = song == null;
            song = newSongFlood();
            if (first) {
                final long size = song.track.ticks();
                final int sectionLength = 512;
                if ((size % sectionLength) != 0) {
                    throw new IllegalStateException("Song not a multiple of " + sectionLength + ": " + size);
                }
                System.out.println("Playing " + song.name + " - " + size + " ticks");
                System.out.println("Press enter to adjust; press x and enter to stop");
            }
            System.out.println("arg=" + arg);
            play(song);
            arg++;
        } while (!Iotil.readln().equals("x"));
        stop();
        System.out.println("End");
    }
    
    public final static void main(final String[] args) {
        try {
            run();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
