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
    
    private final static long DEF_SIZE = 1536;
    private final static int BG = PRG_SQUARE, BG2 = PRG_SAWTOOTH;
    private final static int FG = PRG_CLAVINET; // PRG_NEW_AGE, PRG_ELECTRIC_PIANO_2
    private final static int CHCK = PRC_CLOSED_HI_HAT, DRUM = PRC_BASS_DRUM_2;
    private final static int VOL_BG = 56;
    private final static int VOL_FG = 64;
    
    private static int arg = 0;
    
    static {
        initSong(0);
    }
    
    private final static void initSong(final long size) {
        sequenceResolution = 128;
        durationSameAsDelta = true;
        volPercussion = 48;
        whiteKeyMode = true;
        Mustil.size = size;
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
        final int reps = 6;
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
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        setInstrument(track, channel, BG2);
        final int reps = 16;
        final int l = 22, m = 23, h = 24;
        composeUntil(size, 128, m, 64, h, 64, l, 128, m, 32, h, 32, m, 64, h);
        
        final int p = CHCK, p2 = DRUM;
        addPercussionsUntil(track, 0, 4, size, p2, p2, p2, -1, p, -1, -1, -1, p2, p2, p2, -1, -1, -1, -1, -1);
        
        channel = 1;
        vol = VOL_FG;
        setInstrument(track, channel, FG);
        final int baseNote = 28;
        next = 0;
        /*for (int i = 0; i < 16; i++) {
            addHail(32, 52);
        }*/
        addHailEcho(8, 52); next += 128; addHailEcho(8, 52); next += 128;
        //addHailEcho(8, 52); addHailEcho(8, 49); addHailEcho(8, 49); addHailEcho(8, 49);
        addHailEcho(8, 52); addHailEcho(8, 49); addHailEcho(8, 52); addHailEcho(8, 49);
        //addHailEcho(8, 52); addHailEcho(4, 49); addHailEcho(4, 52); addHailEcho(8, 49); addHailEcho(4, 49); addHailEcho(4, 48);
        addHailEcho(4, 52); addHailEcho(4, 49); addHailEcho(4, 52); addHailEcho(4, 49); addHailEcho(8, 52); addHailEcho(8, 49);
System.out.println(next);
        /*addHails(32, 52, 51, 50, 49);
        addHails(32, 52, 51, 50, -1);
        addHails(16, 52, -1, 51, 51, 50, -1, 49, 49);
        addHails(32, 52, 51, 50, -1);*/
        
        return song;
    }
    
    private final static void addHailEcho(final int reps, final int key) throws Exception {
        final int old = vol;
        for (int i = 0; i < reps; i++) {
            addHail(16, key);
            vol -= 6;
        }
        vol = old;
    }
    
    private final static void addHails(final int dur, final int... keys) throws Exception {
        for (final int key : keys) {
            addHail(dur, key);
        }
    }
    
    private final static void addHail(final int dur, final int n) throws Exception {
        //addNotes(1, 45, 46, 47, 48);
        //final int n = 52;
        if (n < 0) {
            addNote(dur, n);
            return;
        }
        addNotes(1, n, n + 1, n + 2, n + 3);
        next += (dur - 4);
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
            addNotes(16, nf, ns);
            addNotes(8, n3, n3);
            final int nl;
            if ((i == 0) || (i == 1) || (i == 4) || (i == 5)) {
                nl = n2;
            } else if ((i == 2) || (i == 3)) {
                nl = n4;
            } else {
                nl = n4;
            }
            addNote(8, nl);
            addNotes(4, nl, nl);
            if (i == 2) {
                addNotes(16, nl, nl, nl, nl);
                continue;
            }
        }
        final int o = baseNote, nn = o - 1, n0 = o, n1 = o + 1, n2 = o + 2, n3 = o + 3, n4 = o + 4, n5 = o + 5;
        addNote(32, n5);
        addNote(8, n4);
        addNotes(4, n4, n4);
        addNote(16, n5);
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < 3; i++) {
                final int n4i = n4 - i;
                addNote(16, n4i);
                addNotes(8, n4i, n4i);
                addNotes(16, n3 - i, n2 - i);
            }
            if (j == 0) {
                addNotes(4, n1, n1);
                addNotes(8, n1, n1, n1);
                addNotes(4, n2, n2);
                addNote(8, n2);
                addNote(16, n3);
            } else {
                addNotes(16, n1, n1);
                addNotes(8, n0, n0);
                addNote(16, nn);
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
        final int n = 21, h = 28;
        //addRepeatedNotes(track, 0, channel, vol, deltaTick, reps, n, n, n, -1, n, -1, -1, -1, n, n, n, -1, -1, -1, -1, -1); //TODO might work, untested
        //n, n, n, -1, n, n, -1, -1, n, n, n, -1, -1, -1, -1, -1
        
        final int p = CHCK, p2 = DRUM;
        addPercussionsUntil(track, 0, 8, size, p); //TODO
        
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
        /*for (int i = 0; i < 4; i++) {
            final int i2 = i % 2;
            addNotes(16, n2, n2);
            addNotes(8, n1, n1, n1);
            addNote(4, n1);
            addNote(3, n1);
            next += 1;
            if (i2 == 0) {
                addNotes(16, n0, n0);
                addNotes(8, nn, nn, nn, nn);
            } else if (i == 1) {
                addNotes(16, n3, n3, n4, n4);
            } else if (i == 3) {
                addNotes(track, next, channel, vol, 16, n0, n0, nn);
                addNote(track, next, 16, channel, n1, SILENT);
            }
        }*/
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < 3; i++) {
                final int ne = n2 - i;
                compose(16, ne, 16, ne, 4, ne, 4, ne, 8, ne, 8, ne, 8, -1);
            }
            if (j == 0) {
                addNotes(16, nn, nn, n0, n1);
            } else {
                addNotes(16, nn, nn, nn - 1, nn - 2);
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
        final Song song = newSong("Final", 512 * 7);
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        setInstrument(track, channel, BG2);
        final int n = 21;
        next = 0;
        final int dur = 16, dur3 = dur * 3;
        composeUntil(size, dur3, n, dur3, n, dur, n, dur, n);
        
        addPercussionsUntil(track, 0, 128, size, DRUM);
        
        channel = 1;
        vol = VOL_FG;
        next = 0;
        setInstrument(track, channel, FG);
        next = 512;
        final int d1 = 4, d2 = d1 * 2, d4 = d1 * 4, d8 = d1 * 8;
        final int n0 = 30, nn = n0 - 1, n1 = n0 + 1, n2 = n0 + 2, n3 = n0 + 3, n4 = n0 + 4;
        for (int i = 0; i < 2; i++) {
            addEcho(d8, n3, 6, 8);
            addEcho(d8, n2, 6, 8);
        }
        for (int i = 0; i < 2; i++) {
            addNotes(d8, n3, n2, n1, n2, n3); addNotes(d4, n2, n2); addNotes(d8, n1, n0);
            if (i == 0) {
                addNotes(d8, n3, n2, n1, n2, n3, n2, n1, -1);
            } else {
                addNotes(d8, n3, n2, n1, n0, n1, n0, nn, -1);
            }
        }
        for (int i = 0; i < 2; i++) {
            compose(d8, n3, d8, n2, d2, n3, d2, n3, d2, n2, d2, n2, d8, n1, d4, n3, d4, n3, d4, n2, d4, n2, d2, n1, d2, n1, d2, n0, d2, n0, d8, nn); // d8 * 8 = 256
            if (i == 0)  {
                compose(d2, n3, d2, n3, d2, n3, d2, n3, d4, n2, d4, n2, d2, n3, d2, n3, d2, n3, d2, n3, d8, n2, d4, n3, d4, n3, d4, n2, d4, n2, d8, n1, d8, -1);
            } else {
                compose(d2, n3, d2, n3, d2, n2, d2, n2, d2, n3, d2, n3, d2, n4, d2, n4, d4, n3, d4, n2, d4, n3, d4, n2, d2, n1, d2, n1, d2, n2, d2, n2, d4, n1, d4, n0, d8, nn, d8, -2);
            }
        }
        return song;
    }
    
    protected final static Song newSongTmp() throws Exception {
        final Song song = newSong("TmpBot", 1024);
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        setInstrument(track, channel, BG);
        //TODO Double tempo?
        //final int d1 = 4, d2 = d1 * 2, d3 = d1 * 3, d4 = d1 * 4, d6 = d1 * 6, d8 = d1 * 8, d12 = d1 * 12, dg = d1 * 16;
        final int d1 = 2, d2 = d1 * 2, d3 = d1 * 3, d4 = d1 * 4, d6 = d1 * 6, d8 = d1 * 8, d12 = d1 * 12, dc = d1 * 12, dg = d1 * 16;
        final int m = 21, h = 22;
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
        final Song song = newSong("Tmp2Bot", 512);
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        setInstrument(track, channel, BG);
        final int d1 = 4, d2 = d1 * 2, d3 = d1 * 3, d4 = d1 * 4, d6 = d1 * 6, d8 = d1 * 8, d12 = d1 * 12, dc = d1 * 12, dg = d1 * 16;
        final int m = 21, h = 22;
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
        final Song song = newSong("Tmp3Bot", 2048);
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        setInstrument(track, channel, BG);
        final int b2 = 22, b3 = 23;
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
        final int n = 21, h = 28;
        composeUntil(size, 4, n, 4, -1, 4, h, 4, -1, 4, -1, 4, n, 4, n, 4, -1);
        
        final int p = CHCK;
        addPercussionsUntil(track, 0, deltaTick, size, p, -1, p, p, -1, p, p, -1);
        
        channel = 1;
        vol = VOL_FG;
        next = 0;
        setInstrument(track, channel, FG);
        final int dl = 30;
        for (int i = 0; i < 4; i++) {
            final int i2 = i % 2;
            final int ns = (i < 2) ? 31 : -1, nt = (i < 2) ? 32 : -1;
            final int df = (i < 2) ? 16 : dl, dsa = (i < 2) ? 12 : 1, dsb = (i < 2) ? 4 : 1;
            /*compose(16, 31, 16, ns, 16, nt, 16, 32, 16, -1, 8, 31, 8, 31, 4, 30, 4, 30, 4, 30, 4, 30, 8, 32, 8, 32);
            if (i2 == 0) {
                compose(16, 31, 16, ns, 16, nt, 16, 32, 16, -1, 8, -1, 8, -1, 4, 30, 4, 30, 4, 30, 4, 30, 8, 32, 8, -1);
            } else {
                compose(16, 31, 16, ns, 16, nt, 16, 32, 16, -1, 16, 31, 16, 30, 16, -1);
            }*/
            /*compose(df, 31, dsa, ns, dsb, ns, 12, nt, 4, nt, 16, 32, 16, -1, 8, 31, 8, 31, 4, 30, 4, 30, 4, 30, 4, 30, 8, 32, 8, 32);
            if (i2 == 0) {
                compose(df, 31, dsa, ns, dsb, ns, 12, nt, 4, nt, 16, 32, 16, -1, 8, -1, 8, -1, 4, 30, 4, 30, 4, 30, 4, 30, 8, 32, 8, -1);
            } else {
                compose(df, 31, dsa, ns, dsb, ns, 12, nt, 4, nt, 16, 32, 16, -1, 16, 31, 16, 30, 16, -1);
            }*/
        }
        final int d1 = 4, d2 = d1 * 2, d3 = d1 * 3, d4 = d1 * 4, d8 = d1 * 8;
        compose(d4, 31, d3, 31, d1, 31, d3, 32, d1, 32, d4, 32, d4, -1, d2, 31, d2, 31, d1, 30, d1, 30, d1, 30, d1, 30, d2, 32, d2, 32);
        compose(d4, 31, d3, 31, d1, 31, d3, 32, d1, 32, d4, 32, d4, -1, d2, -1, d2, -1, d1, 30, d1, 30, d1, 30, d1, 30, d2, 32, d2, -1);
        compose(d4, 31, d3, 31, d1, 31, d3, 32, d1, 32, d4, 32, d4, -1, d2, 31, d2, 31, d1, 30, d1, 30, d1, 30, d1, 30, d2, 32, d2, 32);
        compose(d4, 31, d3, 31, d1, 31, d3, 32, d1, 32, d4, 32, d4, -1, d4, 31, d4, 30, d4, -1);
        
        compose(d8, 31, d4, -1, d4, 32, d4, -1, d2, 31, d2, 31, d1, 30, d1, 30, d1, 30, d1, 30, d2, 32, d2, 32);
        compose(d8, 31, d4, -1, d4, 32, d4, -1, d2, -1, d2, -1, d1, 30, d1, 30, d1, 30, d1, 30, d2, 32, d2, -1);
        compose(d8, 31, d4, -1, d4, 32, d4, -1, d2, 31, d2, 31, d1, 30, d1, 30, d1, 30, d1, 30, d2, 32, d2, 32);
        compose(d8, 31, d4, -1, d4, 32, d4, -1, d4, 31, d4, 30, d4, -1);
        for (int i = 0; i < 2; i++) {
            /*addNotes(16, 31, -1, 31, -1, -1, 31, 32, -1);
            if (i == 0) {
                addNotes(16, 31, -1, 31, -1, -1, 31, 30, -1);
            } else {
                addNotes(16, 31, -1, 31, -1, 30, -1, -1, -1);
            }*/
            /*compose(dl, 31, 2, -1, dl, 31, 2, -1, 16, -1, 16, 31, dl, 32, 2, -1);
            if (i == 0) {
                compose(dl, 31, 2, -1, dl, 31, 2, -1, 16, -1, 16, 31, dl, 30, 2, -1);
            } else {
                compose(dl, 31, 2, -1, dl, 31, 2, -1, dl, 30, 2, -1, 32, -1);
            }*/
            compose(d8, 31, d8, 31, d4, -1, d4, 31, d8, 32);
            if (i == 0) {
                compose(d8, 31, d8, 31, d4, -1, d4, 31, d8, 30);
            } else {
                compose(d8, 31, d8, 31, d8, 30, d8, -1);
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
    
    protected final static Song newSongTmp6() throws Exception {
        final Song song = newSong("Tmp6Bot", 512);
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        deltaTick = 4;
        setInstrument(track, channel, BG);
        //final int l = 20, n = 21, h = 28;
        //composeUntil(size);
        
        final int p = CHCK, p2 = DRUM;
        addPercussionsUntil(track, 0, 16, size, p2);
        
        channel = 1;
        vol = VOL_FG;
        next = 0;
        setInstrument(track, channel, FG);
        final int d = 8;
        //final int n = 28, h = 35, s = -1;
        final int l = 27, n = 28, m = 30, h = 32, s = -1;
        compose(16, n, 16, n, 16, n, 16, -1, 4, l, 12, l, 48, -1);
        compose(16, l, 16, l, 16, l, 16, -1, 4, n, 4, n, 8, n, 48, -1);
        compose(16, n, 16, n, 16, n, 16, -1, 4, l, 12, l, 48, -1);
        compose(16, l, 16, l, 16, l, 16, -1, 4, n, 4, n, 8, n, 48, -1);
System.out.println(next);
        addSilent(size, 4);
        
        return song;
    }
    
    protected final static Song newSongTmp7() throws Exception {
        final Song song = newSong("Tmp7Bot");
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        deltaTick = 4;
        setInstrument(track, channel, BG);
        final int l = 20, n = 21;
        composeUntil(size, 3, n, 1, -1, 3, l, 1, -1);
        
        final int p = CHCK, p2 = DRUM;
        addPercussionsUntil(track, 0, 16, size, p2); //TODO
        
        channel = 1;
        vol = VOL_FG;
        next = 0;
        setInstrument(track, channel, FG);
        final int o = 28, n0 = o, n1 = o + 1, n2 = o + 2, n3 = o + 3;
        final int nfa = n1, nfb = n0, nsa = n3, nsb = n2;
        for (int i = 0; i < 4; i++) {
            compose(8, nfa, 4, nfb, 4, nfb, 16, nfa, 32, -1);
        }
        for (int i = 0; i < 4; i++) {
            compose(8, nsa, 4, nsb, 4, nsb, 16, nsa, 32, -1);
        }
        
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < 2; i++) {
                compose(8, nfa, 4, nfb, 4, nfb, 16, nfa, 16, -1, 8, nfb, 8, nfb);
            }
            for (int i = 0; i < 2; i++) {
                compose(8, nsa, 4, nsb, 4, nsb, 16, nsa, 16, -1, 8, nsb, 8, nsb);
            }
        }

        addNotes(16, n1, n1, n0, n0, n1, n1, n0, n1);
        addEcho(16, n2, 6, 8);
        addNotes(16, n1, n1, n0, n0, n1, n1, n0, n1);
        addEcho(16, n0, 6, 8);
        
        return song;
    }
    
    protected final static Song newSongTmp8() throws Exception {
        //final int d1 = 8;
        //final int d1 = 6;
        final int d1 = 4;
        final int dh = d1 / 2, d2 = d1 * 2, d3 = d1 * 3, d4 = d1 * 4, d8 = d1 * 8;
        //final int sectionLength = 64 * d1;
        //final int sectionLength = 128 * d1;
        //final Song song = newSong("Tmp8Bot", 3 * sectionLength);
        final Song song = newSong("Tmp8Bot", 512);
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        deltaTick = 4;
        setInstrument(track, channel, BG);
        final int l = 20, n = 21;
        //composeUntil(size, d1, n, d1, -1, d1, n, d1, -1, d1, l, d1, -1, d1, l, d1, -1);
        {
            final int n0 = 20, n1 = n0 + 1, n2 = n0 + 2, n3 = n0 + 3;
            //composeAll(d3, n0, d1, n2, d2, n3, d2, n2, d2, n3, d1, n2, d1, n2, d4, n3, d3, n0, d1, n2, d2, n3, d1, n2, d1, n2, d2, n3, d2, n2, d4, n1);
            //composeAll(d3, -1, d1, n2, d1, n3, d1, -1, d1, n2, d1, -1, d1, n3, d1, -1, d1, n2, d1, n2, d1, n3, d3, -1,
            //           d3, -1, d1, n2, d1, n3, d1, -1, d1, n2, d1, n2, d1, n3, d1, -1, d1, n2, d1, -1, d1, n1, d3, -1);
            composeAll(d1, n0, d2, -1, d1, n2, d1, n3, d1, -1, d1, n2, d1, -1, d1, n3, d1, -1, d1, n2, d1, n2, d1, n3, d3, -1,
                       d1, n0, d2, -1, d1, n2, d1, n3, d1, -1, d1, n2, d1, n2, d1, n3, d1, -1, d1, n2, d1, -1, d1, n1, d3, -1);
        }
        //composeUntil(size, dh, n, dh, -1, dh, n, dh, -1, dh, l, dh, -1, dh, l, dh, -1);
        //composeUntil(size, d1, l, d1, l, d1, n, d1, -1);
        //composeUntil(size, dh, l, dh, l, dh, n, dh, -1);
        
        final int p = CHCK, p2 = DRUM;
        //addPercussionsUntil(track, 0, d1, size, p, p2, p2, -1, p, -1, p, -1, p, p2, p2, -1, p, -1, -1, -1);
        addPercussionsUntil(track, 0, d1, size, p, p2, p2, -1, p, p2, p, -1, p, p2, p2, -1, p, p2, p2, -1);
        
        channel = 1;
        vol = VOL_FG;
        next = 0;
        setInstrument(track, channel, FG);
        final int o = 28;
        final int nn = o - 1, n0 = o, n1 = o + 1, n2 = o + 2, n3 = o + 3;
        //next = 64;
        ////compose(d2, n0, d2, n1, d2, n2, d2, n3, d2, n2, d2, n1, d2, n0, d4, n3, d2, n0, d2, n1, d2, n2, d4, n3, d4, n3);
        ////compose(d2, n0, d2, n0, d2, n1, d2, n1, d2, n0, d2, n0, d2, n1, d4, n2, d2, n0, d2, n0, d2, n0, d4, n1, d4, n1);
        //compose(d4, n0, d4, n1, d2, n0, d2, n0, d2, n1, d4, n2, d2, n0, d2, n0, d2, n1, d4, n2, d4, n2);
        ////compose(d4, n0, d4, n1, d2, n0, d2, n0, d2, n1, d4, -1, d2, n0, d2, n0, d2, n0, d8, nn);
        //compose(d4, n0, d4, n1, d2, n0, d2, n0, d2, n1, d4, n0, d2, n1, d2, n1, d2, n0, d4, nn, d4, -1);
        composeAll(d4, n0, d4, n1, d2, n0, d2, n0, d2, n1, d4, n2, d2, n0, d2, n0, d2, n1, d4, n2, d4, n2, d4, n0, d4, n1, d2, n0, d2, n0, d2, n1, d4, n0, d2, n1, d2, n1, d2, n0, d4, nn, d4, -1,
                   d4, n0, d4, n1, d4, n0, d2, n1, d4, n2, d4, n0, d2, n1, d4, n2, d4, n2, d4, n0, d4, n1, d4, n0, d2, n1, d4, n0, d4, n1, d2, n0, d4, nn, d4, -1);
        //compose(d2, n3, d2, n2, d2, n3, d1, n2, d1, n2, d4, n3, d3, -1, d1, n2, d2, n3, d1, n2, d1, n2, d2, n3, d2, n2, d4, n1, d3, -1, d1, n2);
        //compose(d3, -1, d1, n2, d2, n3, d2, n2, d2, n3, d1, n2, d1, n2, d4, n3, d3, -1, d1, n2, d2, n3, d1, n2, d1, n2, d2, n3, d2, n2, d4, n1);
        //composeAll(d3, n0, d1, n2, d2, n3, d2, n2, d2, n3, d1, n2, d1, n2, d4, n3, d3, n0, d1, n2, d2, n3, d1, n2, d1, n2, d2, n3, d2, n2, d4, n1);
        /*compose(d2, n3, d2, n2, d2, n3, d1, n2, d1, n2, d4, n3, d3, -1, d1, n2);
        compose(d2, n3, d1, n2, d1, n2, d2, n3, d2, n2, d4, n1, d3, -1, d1, n2);
        compose(d2, n3, d2, n2, d2, n3, d1, n2, d1, n2, d4, n3, d3, -1, d1, n2);
        compose(d2, n3, d1, n2, d1, n2, d2, n3, d2, n2, d4, n1, d3, -1, d1, n2);
        compose(d4, n3, d2, n3, d1, n2, d1, n2, d4, n3, d3, -1, d1, n2);
        compose(d2, n3, d1, n2, d1, n2, d4, n3, d4, n1, d3, -1, d1, n2);
        compose(d4, n3, d2, n3, d1, n2, d1, n2, d4, n3, d3, -1, d1, n2);
        compose(d2, n3, d1, n2, d1, n2, d4, n3, d4, n1, d3, -1, d1, n2);
        compose(d4, n3, d4, n2, d4, n3, d3, -1, d1, n2);
        compose(d4, n3, d4, n2, d4, n1, d3, -1, d1, n2);
        compose(d4, n3, d4, n2, d4, n3, d3, -1, d1, n2);
        compose(d4, n3, d4, n2, d4, n1, d3, -1, d1, n2);*/
System.out.println(next);
//addSilent(size, 4);
        
        return song;
    }
    
    protected final static Song newSongTmp9() throws Exception {
        final Song song = newSong("Tmp9Bot", 512);
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        setInstrument(track, channel, BG);
        final int n = 21, h = 28;
        //addNotesAll(4, n, -1, n, -1, h, -1, n, -1, n, n, n, -1, h, -1, -1, -1); // Used in Tmp10
        
        next = 0;
        final int p = CHCK, p2 = DRUM;
        //addPercussionsAll(4, p, -1, p, p, p, -1, p2, -1); // Used in Tmp10
        
        channel = 1;
        vol = VOL_FG;
        setInstrument(track, channel, FG);
        next = 0;
        final int n0 = 28, n1 = n0 + 1, n2 = n0 + 2, n3 = n0 + 3, n4 = n0 + 4;
        compose(16, n3, 16, -1, 16, n4, 16, -1, 4, n2, 4, n2, 8, n2, 8, n3, 8, n3, 16, n4, 16, -1);
        compose(16, n3, 16, n2, 16, n4, 16, -1, 8, n2, 8, n2, 8, n3, 8, n3, 16, n4, 16, -1);
        compose(16, n3, 16, n2, 16, n3, 16, n4, 16, -1, 8, n3, 8, n3, 16, n4, 16, n4);
        compose(16, n3, 16, n3, 16, n2, 16, n2, 16, n1, 16 * 3, -1);
        addSilent(size, 4);
        
        return song;
    }
    
    protected final static Song newSongTmp10() throws Exception {
        final Song song = newSong("Tmp10Bot", 512);
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        setInstrument(track, channel, BG);
        final int n = 21, h = 28;
        addNotesAll(4, n, -1, n, -1, h, -1, n, -1, n, n, n, -1, h, -1, -1, -1);
        
        next = 0;
        final int p = CHCK, p2 = DRUM;
        addPercussionsAll(4, p, -1, p, p, p, -1, p2, -1);
        
        channel = 1;
        vol = VOL_FG;
        setInstrument(track, channel, FG);
        next = 0;
        final int n0 = 28, n1 = n0 + 1, n2 = n0 + 2, n3 = n0 + 3, n4 = n0 + 4;
        compose(16, n4, 16, n4, 16, n3, 8, n3, 8, n3, 16, n4, 16, n3, 16, n2, 16, -1);
        compose(16, n4, 16, n4, 16, n3, 16, -1, 16, -1, 16, n3, 16, n2, 16, -1);
        compose(8, n4, 8, n4, 16, -1, 8, n2, 8, n2, 16, -1, 16, n3, 16, -1, 16, n1, 16, -1);
        compose(16, n4, 16, n3, 16, n2, 16, n1, 16, n0, 16, -1, 16, -1, 16, -1);
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
        return newSong(name, DEF_SIZE);
    }
    
    private final static Song newSong(final String name, final long size) throws Exception {
        initSong(size);
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
            song = newSongTmp10();
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
