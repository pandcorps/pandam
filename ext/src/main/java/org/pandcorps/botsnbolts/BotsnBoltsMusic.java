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
    
    private final static int BG = PRG_SQUARE;
    private final static int FG = PRG_CLAVINET; // PRG_NEW_AGE, PRG_ELECTRIC_PIANO_2
    private final static int DRUM = PRC_CLOSED_HI_HAT, DRUM_HEAVY = PRC_BASS_DRUM_2;
    private final static int VOL_BG = 56;
    private final static int VOL_FG = 64;
    
    private static int arg = 0;
    
    static {
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
        
        final int p = DRUM;
        addRepeatedPercussions(track, 0, deltaTick, reps * 2, p, -1, -1, -1, p, p, -1, -1);
        
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
        addRepeatedNotes(track, 0, channel, vol, deltaTick, reps, m, -1, m, -1, m, -1, m, -1, m, -1, m, -1, m, -1, m, -1,
            l, -1, l, -1, l, -1, l, -1, l, -1, l, -1, l, -1, l, -1,
            m, -1, m, -1, m, -1, m, -1, m, -1, m, -1, m, -1, m, -1,
            h, -1, h, -1, h, -1, h, -1, h, -1, h, -1, h, -1, h, -1);
        addNote(track, 1536 - deltaTick, deltaTick, channel, m, SILENT);
        
        final int p = DRUM, p2 = DRUM_HEAVY;
        addRepeatedPercussions(track, 0, deltaTick, reps * 8, p2, -1, -1, -1, p, p, -1, -1);
        
        channel = 1;
        vol = VOL_FG;
        setInstrument(track, channel, FG);
        int n0 = 28, n1 = n0 + 1, n2 = n1 + 1, n3 = n2 + 1, n4 = n3 + 1, n5 = n4 + 1, n6 = n5 + 1, n7 = n6 + 1;
        next = 256;
        addNotes(track, next, channel, vol, 64, n3, n2, n3, n4);
        
        addNote(track, next, 48, channel, n3, vol);
        addNotes(track, next, channel, vol, 8, n1, n1);
        addNote(track, next, 48, channel, n2, vol);
        addNote(track, next, 16, channel, n2, vol);
        addNote(track, next, 48, channel, n3, vol);
        addNotes(track, next, channel, vol, 8, n3, n3);
        addNote(track, next, 64, channel, n4, vol);
        
        addNote(track, next, 48, channel, n5, vol);
        addNotes(track, next, channel, vol, 8, n3, n3);
        addNote(track, next, 32, channel, n4, vol);
        addNotes(track, next, channel, vol, 8, n3, n3);
        addNote(track, next, 16, channel, n4, vol);
        addNote(track, next, 48, channel, n5, vol);
        addNotes(track, next, channel, vol, 8, n5, n5);
        addNote(track, next, 64, channel, n6, vol);
        
        addNote(track, next, 48, channel, n7, vol);
        addNotes(track, next, channel, vol, 8, n5, n5);
        addNote(track, next, 48, channel, n6, vol);
        addNote(track, next, 16, channel, n4, vol);
        addNote(track, next, 32, channel, n5, vol);
        addNotes(track, next, channel, vol, 8, n3, n3);
        addNote(track, next, 16, channel, n2, vol);
        addNote(track, next, 128, channel, n1, vol);
        
        return song;
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
        final int m = 21, l = 20, h = 22;
        addRepeatedNotes(track, 0, channel, vol, deltaTick, reps, m, m, m, -1, l, -1, -1, -1, m, m, m, -1, h, h, -1, -1);
        
        final int p = DRUM;
        addRepeatedPercussions(track, 0, deltaTick, reps, p, p, -1, -1, p, -1, -1, -1, p, p, -1, -1, p, p, -1, -1);
        
        channel = 1;
        setInstrument(track, channel, FG);
        final int baseNote = 28;
        next = 0;
        final int volEcho = (VOL_FG + VOL_BG) / 2;
        for (int i = 0; i < 7; i++) {
            if (i == 3) {
                continue;
            }
            final int i2 = i % 2, o = baseNote - ((i2 == 0) ? 0 : 2), n2 = o + 2, n3 = o + 3, n4 = o + 4;
            vol = (i2 == 0) ? VOL_FG : volEcho;
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
                addNotes(track, next, channel, volEcho, 16, nl, nl, nl, nl);
                continue;
            }
        }
        final int o = baseNote, nn = o - 1, n0 = o, n1 = o + 1, n2 = o + 2, n3 = o + 3, n4 = o + 4, n5 = o + 5;
        addNote(track, next, 32, channel, n5, vol);
        addNote(track, next, 8, channel, n4, volEcho);
        addNotes(track, next, channel, volEcho, 4, n4, n4);
        addNote(track, next, 16, channel, n5, volEcho);
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
        deltaTick = 8;
        setInstrument(track, channel, BG);
        final int reps = 16;
        final int m = 21, l = 20, h = 22;
        /*addRepeatedNotes(track, 0, channel, vol, deltaTick, reps, m, -1, m, -1, m, -1, m, -1, m, -1, m, -1, m, -1, m, -1,
            l, -1, l, -1, l, -1, l, -1, l, -1, l, -1, l, -1, l, -1,
            m, -1, m, -1, m, -1, m, -1, m, -1, m, -1, m, -1, m, -1,
            h, -1, h, -1, h, -1, h, -1, h, -1, h, -1, h, -1, h, -1); // Lightning
        addNote(track, 1536 - deltaTick, deltaTick, channel, m, SILENT);*/
        //12334455
        next = 0;
        int t = m;
        for (int i = 0; i < reps; i++) {
            //addNotes(track, next, channel, vol, 8, m, m);
            //addNotes(track, next, channel, vol, 16, m, m, m);
            //addNotes(track, next, channel, vol, 8, m, l, m, l);
            //addNotes(track, next, channel, vol, 8, m, m, l, l);
            //addNotes(track, next, channel, vol, 8, m, m, h, h);
            addNotes(track, next, channel, vol, 8, m, m, m, m);
            /*addNotes(track, next, channel, vol, 8, t, t, t, t);
            if (i < 2) {
                t++;
            } else {
                t--;
            }*/
            next += 32;
            //next += 16; addNotes(track, next, channel, vol, 16, m);
        }
        
        deltaTick = 4;
        final int p = DRUM, p2 = DRUM_HEAVY;
        //addRepeatedPercussions(track, 0, deltaTick, reps * 8, p2, -1, -1, -1, p, -1, -1, -1); // Lightning
        //addRepeatedPercussions(track, 0, deltaTick, reps * 4, p2, -1, p, p, p, -1, -1, -1, p2, -1, -1, -1, p, -1, -1, -1); // New, might work
        //addRepeatedPercussions(track, 0, deltaTick, reps * 4,  p, p, p, -1, -1, -1, -1, -1, p, p, p, -1, p, -1, p, -1);
        addRepeatedPercussions(track, 0, deltaTick, reps,  p, p, p, -1, p2, -1, p, p, p, -1, p2, -1, p2, -1, -1, -1);
        
        channel = 1;
        vol = VOL_FG;
        setInstrument(track, channel, FG);
        int n0 = 28, n1 = n0 + 1, n2 = n1 + 1, n3 = n2 + 1, n4 = n3 + 1, n5 = n4 + 1, n6 = n5 + 1, n7 = n6 + 1;
        next = 0;
        addNotes(track, next, channel, vol, 64, n2, n3, n4, n3, n2, n1, -1, -1);
        addNotes(track, 512, channel, vol, 64, n2, n3, n4, n3, n2, n1, -1, -1);
        
        //addNote(track, next, 48, channel, n3, vol);
        
        return song;
    }
    
    protected final static Song newSongCyclone() throws Exception {
        final Song song = newSong("CycloneBot");
        final Track track = song.track;
        
        channel = 0;
        vol = VOL_BG;
        deltaTick = 8;
        setInstrument(track, channel, BG);
        final int reps = 6;
        final int m = 21, l = 20, h = 22;
        /*addRepeatedNotes(track, 0, channel, vol, deltaTick, reps, m, -1, m, -1, m, -1, m, -1, m, -1, m, -1, m, -1, m, -1,
            l, -1, l, -1, l, -1, l, -1, l, -1, l, -1, l, -1, l, -1,
            m, -1, m, -1, m, -1, m, -1, m, -1, m, -1, m, -1, m, -1,
            h, -1, h, -1, h, -1, h, -1, h, -1, h, -1, h, -1, h, -1);
        addNote(track, 1536 - deltaTick, deltaTick, channel, m, SILENT);*/
        addRepeatedNotes(track, 0, channel, vol, deltaTick, 128, m);
        
        deltaTick = 4;
        final int p = DRUM, p2 = DRUM_HEAVY;
        addRepeatedPercussions(track, 0, deltaTick, 128, p, -1);
        
        channel = 1;
        vol = VOL_FG;
        setInstrument(track, channel, FG);
        //int n0 = 28, n1 = n0 + 1, n2 = n1 + 1, n3 = n2 + 1, n4 = n3 + 1, n5 = n4 + 1, n6 = n5 + 1, n7 = n6 + 1;
        int n0 = 42, n1 = n0 + 1, n2 = n1 + 1, n3 = n2 + 1, n4 = n3 + 1, n5 = n4 + 1, n6 = n5 + 1, n7 = n6 + 1;
        next = 0;
        //addNotes(track, next, channel, vol, 64, n1, n2, n3, n4, n3, n2, n1, n0);
        //addNotes(track, next, channel, vol, 64, n3, n4, n3, n2, n1, n0, n1, n2);
        for (int i = 0; i < 2; i++) {
            addNotes(track, next, channel, vol, 64, n3, n4, n3, n2, n3, n4, n3, n2);
        }
        
        //addNote(track, next, 48, channel, n3, vol);
        
        return song;
    }
    
    protected final static Song newSongFlood() throws Exception {
        final Song song = newSong("FloodBot");
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
        
        final int p = DRUM;
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
        
        final int p = DRUM;
        addRepeatedPercussions(track, 0, deltaTick, reps, p, -1, -1, -1, p, -1, -1, -1, p, p, -1, -1, p, -1, -1, -1);
        
        channel = 1;
        vol = VOL_FG;
        setInstrument(track, channel, FG);
        final int o = 28;
        final int n1 = o + 1, n2 = o + 2, n3 = o + 3, n4 = o + 4;
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
        
        addNote(track, next, 32, channel, n2, vol);
        addNotes(track, next, channel, vol, 16, n1, n1);
        addNote(track, next, 32, channel, n2, vol);
        
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
        
        deltaTick = 4;
        
        channel = 1;
        vol = VOL_FG;
        setInstrument(track, channel, FG);
        final int baseNote = 28;
        final int n1 = baseNote, n2 = baseNote + 1, n3 = baseNote + 2, n4 = baseNote + 3, n5 = baseNote + 4, n7 = baseNote + 6;
        for (int i = 0; i < 2; i++) {
            final int o = i * 128;
            addNote(track, o, 32, channel, n5, vol);
            if (i < 3) {
                addNote(track, next, 24, channel, n3, vol);
                addNote(track, next, 4, channel, n1, vol);
                addNote(track, next, 4, channel, n1, vol);
                addNote(track, next, 16, channel, n3, vol);
                addNote(track, next, 16, channel, n3, vol);
                if ((i % 2) == 0) {
                    /*addNote(track, next, 8, channel, n2, vol);
                    addNote(track, next, 8, channel, n2, vol);
                    addNote(track, next, 4, channel, n1, vol);
                    addNote(track, next, 4, channel, n1, vol);*/
                    addNote(track, next, 8, channel, n1, vol);
                    addNote(track, next, 8, channel, n1, vol);
                    addNote(track, next, 4, channel, n2, vol);
                    addNote(track, next, 4, channel, n2, vol);
                } else {
                    addNote(track, next, 16, channel, n2, vol);
                }
            } else {
                addNote(track, next, 32, channel, n2, vol);
                addNote(track, next, 32, channel, n1, vol);
            }
            addNote(track, o + 128 - 4, 4, channel, n3, 1);
        }
        //addNote(track, 504, 8, channel, n3, 1);
        
        return song;
    }
    
    protected final static Song newSongFinal() throws Exception {
        final Song song = newSong("Final");
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
            song = newSongRockslide();
            if (first) {
                System.out.println("Playing " + song.name + " - " + song.track.ticks() + " ticks");
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
