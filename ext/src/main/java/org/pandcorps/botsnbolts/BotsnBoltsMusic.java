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
    
    private final static int PRG_BG = PRG_SQUARE;
    private final static int PRG_FG = PRG_CLAVINET; // PRG_NEW_AGE, PRG_ELECTRIC_PIANO_2
    
    private static int arg = 0;
    
    static {
        sequenceResolution = 128;
        whiteKeyMode = true;
    }
    
    protected final static Song newSongVolcano() throws Exception {
        final Song song = newSong("VolcanoBot");
        final Track track = song.track;
        
        channel = 0;
        vol = 56;
        deltaTick = 4;
        unspecifiedNoteDuration = deltaTick;
        volPercussion = 96;
        setInstrument(track, channel, PRG_BG);
        final int reps = 24;
        final int n = 21, h = 28;
        addRepeatedNotes(track, 0, channel, vol, deltaTick, reps, n, n, -1, -1, h, -1, n, n, n, n, -1, -1, h, -1, -1, -1);
        addNote(track, (reps * deltaTick * 16) - (deltaTick * 4), deltaTick * 4, channel, n, SILENT);
        
        final int p = PRC_BASS_DRUM_1;
        //TODO Add more drum beats, try different drum instruments, adjust volume
        addRepeatedPercussions(track, 0, deltaTick, reps / 2, -1, -1, -1, -1, p, -1, -1, -1, -1, -1, -1, -1, p, -1, -1, -1, -1, -1, -1, -1, p, -1, -1, -1, -1, -1, -1, -1, p, p, -1, -1);
        
        channel = 1;
        vol = 64;
        setInstrument(track, channel, PRG_FG);
        final int off = 0;
        final int baseNote = 28;
        for (int i = 0; i < 4; i++) {
            final int o = baseNote + ((i < 2) ? 0 : 3);
            //TODO Try lowering second note even more
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
        unspecifiedNoteDuration = deltaTick;
        final int o = baseNote;
        final int n1 = o + 3, n2 = o + 2, n3 = o + 1, n4 = o, n5 = o - 1, n6 = o - 2;
        addNotes(track, off + 4 * 256, channel, vol, deltaTick, n1, n3, n2, n4, n3, n5, n4, n6, n1, n3, n2, n4, n3, n5);
        deltaTick = 16;
        unspecifiedNoteDuration = deltaTick;
        addNotes(track, off + 4 * 256 + 14 * 32, channel, vol, deltaTick, n3, n5, n4, n6);
        
        return song;
    }
    
    protected final static Song newSongLightning() throws Exception {
        final Song song = newSong("LightningBot");
        return song;
    }
    
    protected final static Song newSongHail() throws Exception {
        final Song song = newSong("HailBot");
        return song;
    }
    
    protected final static Song newSongRockslide() throws Exception {
        final Song song = newSong("RockslideBot");
        return song;
    }
    
    protected final static Song newSongEarthquake() throws Exception {
        final Song song = newSong("EarthquakeBot");
        return song;
    }
    
    protected final static Song newSongCyclone() throws Exception {
        final Song song = newSong("CycloneBot");
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
        vol = 56;
        deltaTick = 4;
        unspecifiedNoteDuration = deltaTick;
        volPercussion = 96;
        setInstrument(track, channel, PRG_BG);
        final int reps = 24;
        final int n = 21, h = 28;
        addRepeatedNotes(track, 0, channel, vol, deltaTick, reps, n, n, n, -1, n, -1, -1, -1, n, n, n, -1, n, -1, h, h);
        
        final int p = PRC_BASS_DRUM_1, d = PRC_HAND_CLAP;
        //TODO Try different drum instruments, adjust volume
        //addRepeatedPercussions(track, 0, deltaTick, reps / 2, d, -1, p, -1, d, -1, p, -1, d, -1, p, -1, d, -1, p, -1);
        
        channel = 1;
        vol = 64;
        setInstrument(track, channel, PRG_BLOWN_BOTTLE);
        final int off = 0;
        final int baseNote = 28;
        for (int i = 0; i < 4; i++) {
            final int o = baseNote + ((i < 2) ? 0 : 3);
            //TODO Try lowering second note even more
            //final int n1 = o + 4, n2 = o + 3, n3 = o + 2, n4 = o + 1, n5 = o;
            final int start = off + (i * 256);
            /*addNote(track, start, 63, channel, 44, vol);
            addNote(track, start + 64, 15, channel, 43, vol);
            addNote(track, start + 80, 15, channel, 43, vol);
            addNote(track, start + 112, 63, channel, 44, vol);
            addNote(track, start + 240, 15, channel, 43, vol);*/
            /*addNote(track, start, 47, channel, 44, vol);
            addNote(track, start + 48, 11, channel, 43, vol);
            addNote(track, start + 60, 15, channel, 43, vol);
            addNote(track, start + 76, 47, channel, 44, vol);*/
            addNote(track, start, 47, channel, 44, vol);
            addNote(track, start + 48, 7, channel, 43, vol);
            addNote(track, start + 56, 7, channel, 43, vol);
            addNote(track, start + 64, 47, channel, 44, vol);
            addNote(track, start + 112, 15, channel, 43, vol);
            
            addNote(track, start + 128, 47, channel, 44, vol);
            addNote(track, start + 128 + 48, 7, channel, 43, vol);
            addNote(track, start + 128 + 56, 7, channel, 43, vol);
            addNote(track, start + 128 + 64, 47, channel, 44, vol);
            addNote(track, start + 128 + 112, 15, channel, 43, vol);
            /*if ((i % 2) == 0) {
                addNote(track, start + 192, 16, channel, n1, vol);
                addNote(track, start + 208, 16, channel, n2, vol);
                addNote(track, start + 224, 16, channel, n3, vol);
            } else {
                addNote(track, start + 192, 16, channel, n3, vol);
            }*/
        }
        
        return song;
    }
    
    protected final static Song newSongCity() throws Exception {
        final Song song = newSong("City");
        return song;
    }
    
    protected final static Song newSongArray() throws Exception {
        final Song song = newSong("Array");
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
            song = newSongDrought();
            if (first) {
                System.out.println("Playing " + song.name);
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
