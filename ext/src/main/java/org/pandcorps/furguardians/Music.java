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
package org.pandcorps.furguardians;

import javax.sound.midi.*;

import org.pandcorps.core.*;
import org.pandcorps.core.Mustil.*;
import org.pandcorps.pandam.*;

public class Music {
	private final static String COPYRIGHT = "Copyright (c) " + FurGuardiansGame.YEAR + ", " + FurGuardiansGame.AUTHOR;
	
	private final static int SILENT = 1;
	
	protected final static Sequence newSongCreepy() throws Exception {
		// channel 0 - 15; key/vol 0 - 127
		final int channel = 0, vol = 64;
		final Sequence seq = new Sequence(Sequence.SMPTE_30, 1);
		final Track track = seq.createTrack();
		// Track has 16 channels for different instruments; they can be changed to 128 possible instruments
		//Mustil.setInstrument(track, channel, Mustil.PRG_BIRD_TWEET); // PRG_CRYSTAL
		addCreepy(track, 0, channel, vol);
		return seq;
	}
	
	private final static void addCreepy(final Track track, final int tick, final int channel, final int vol) throws Exception {
		Mustil.addNote(track, tick, channel, 65, vol);
		Mustil.addNote(track, tick + 15, channel, 69, vol);
		Mustil.addNote(track, tick + 30, channel, 72, vol);
	}
	
	private static int channel = 0, key, vol, deltaTick;
	private static long tick = 0;
	
	protected final static Sequence newSongHappy() throws Exception {
		channel = 0;
		key = 48;
		vol = 64;
		final Sequence seq = new Sequence(Sequence.SMPTE_30, 1);
		final Track track = seq.createTrack();
		Mustil.setInstrument(track, channel, Mustil.PRG_ELECTRIC_GUITAR_CLEAN);
		tick = 0;
		key = 48;
		deltaTick = 4;
		tick = addIntroHappy(track);
		key = 56;
		tick = addMainHappy(track);
		key = 48;
		tick = addMainHappy(track, 2);
		key = 56;
		tick = addFastHappy(track);
		key = 48;
		tick = addFastHappy2(track);
		/*key = 52;
		tick = Mustil.addNotes(track, tick, channel, vol, 4,
				key, key - 4, key, key + 4, key, key + 4, key + 8, key + 4, key + 8, key + 12, key + 8, key + 12,
				key + 16, key + 12, key + 8, key + 12, key + 8, key + 4, key + 8, key + 12, key + 8, key + 12,
				key + 8, key + 12, key + 16);
		tick += 24;*/
		key = 56;
		tick = addMainHappy(track);
		key = 60;
		tick = addEndHappy(track);
		addIntroHappy(track);
		return seq;
	}
	
	protected final static long addIntroHappy(final Track track) throws Exception {
		/*if (key > 0) {
			return tick;
		}*/
		key = 48;
		tick = Mustil.addRise(track, tick, channel, key, vol, deltaTick, 4, 5);
		tick += 12;
		tick = Mustil.addRise(track, tick, channel, key + 8, vol, deltaTick, 4, 5);
		tick += 12;
		tick = Mustil.addRise(track, tick, channel, key + 4, vol, 8, 4, 2, 2);
		Mustil.addNote(track, tick, 30, channel, key + 12, vol);
		tick += 30;
		return tick;
	}
	
	protected final static long addMainHappy(final Track track) throws Exception {
		return addMainHappy(track, 1);
	}
	
	protected final static long addMainHappy(final Track track, final int multPeak) throws Exception {
		tick = Mustil.addNotes(track, tick, channel, vol, 8,
				key, key, key, key - 2, key + 2, key + 2, key + 3);
		tick += 8;
		tick = Mustil.addNotes(track, tick, channel, vol, 8,
				//key, key, key + (4 * multPeak), key + (8 * multPeak), key + (12 * multPeak));
				key, key, key + 2, key + 3, key + 5);
		tick += 24;
		return tick;
	}
	
	protected final static long addFastHappy(final Track track) throws Exception {
		tick = Mustil.addNotes(track, tick, channel, vol, 4,
				key, -1, key, key, -1, key, key, -1, key + 2, -1, key + 2);
		tick += 20;
		tick = Mustil.addNotes(track, tick, channel, vol, 4,
				key, -1, key, key, -1, key, key, -1, key + 3);
		tick += 28;
		return tick;
	}
	
	protected final static long addFastHappy2(final Track track) throws Exception {
		tick = Mustil.addNotes(track, tick, channel, vol, 4,
				key, -1, key, key, -1, key, key, -1, key + 2, -1, key + 2);
		tick += 20;
		tick = Mustil.addNotes(track, tick, channel, vol, 4,
				key, -1, key + 3, key + 3, -1, key + 5, key + 5, -1, key + 7);
		tick += 28;
		return tick;
	}
	
	protected final static long addEndHappy(final Track track) throws Exception {
		tick = Mustil.addNotes(track, tick, channel, vol, 8,
				key + 2, key, key, key - 2, key - 2, key, key + 2);
		tick += 8;
		tick = Mustil.addNotes(track, tick, channel, vol, 4,
				key + 2, -1, key, key, key + 2, -1, key + 2, -1);
		Mustil.addNote(track, tick, 32, channel, key, vol);
		tick += 32;
		return tick;
	}
	
	protected final static Song newSongCave() throws Exception {
		final Song song = newSong("Cave");
		final Track track = song.track;
		//final int length = 2048;
		int dur, keys[];
		/*channel = 3;
		Mustil.setInstrument(track, channel, Mustil.PRG_FRETLESS_BASS);
		dur = 2;
		Mustil.unspecifiedNoteDuration = dur;
		keys = {36, 44};
		Mustil.addRepeatedNotes(track, 0, channel, 48, dur, length / keys.length / dur, keys);*/
		/*channel = 1;
		Mustil.setInstrument(track, channel, Mustil.PRG_HALO);
		dur = 32;
		Mustil.unspecifiedNoteDuration = dur;
		keys = new int[] {-1, -1, 56, -1};
		Mustil.addRepeatedNotes(track, 0, channel, 56, dur, length / keys.length / dur, keys);*/
		/*channel = 2;
		Mustil.setInstrument(track, channel, Mustil.PRG_WARM);
		dur = 8;
		Mustil.unspecifiedNoteDuration = 4;
		keys = new int[] {-1, -1, -1, -1, 63, 65, -1, -1};
		Mustil.addRepeatedNotes(track, 0, channel, 58, dur, length / keys.length / dur, keys);*/
		channel = 0;
		vol = 72;
		tick = 0; //128;
		// Underground = PRG_GLOCKENSPIEL, b=56?
		Mustil.setInstrument(track, channel, Mustil.PRG_GLOCKENSPIEL);
		//Mustil.setInstrument(track, channel, Mustil.PRG_XYLOPHONE); // PRG_CRYSTAL
		//for (int k = 0; k < 2; k++) {
			//for (int j = 0; j < 4; j++) {
			for (int j = 0; j < 2; j++) {
				//final int b = 56;
				//final int n = ((j % 2) == 0) ? b : ((j == 1) ? (b + 4) : (b - 4)), n4 = n + 4, n8 = n + 8;
				final int n = (j == 0) ? 55 : 53, n4 = n + 2, n8 = n + 4;
				/*if (k == 1) {
					dur = 4;
					Mustil.unspecifiedNoteDuration = 8;
					keys = new int[] {n, -1, n, n, n4, -1, n, n, n8, -1, n8, -1, n4, -1, -1, -1,
							n, -1, n, n, n4, -1, n4, n4, n, -1, -1, -1, n, -1, -1, -1};
				} else {*/
					dur = 8;
					Mustil.unspecifiedNoteDuration = dur;
					keys = new int[] {n, n, n4, n, n8, n8, n4, -1, n, n, n4, n4, n, -1, n, -1};
					//keys = new int[] {n, n, n4, n, n8, n8, n4, -1, n, n, n4, n4, n, -1};
				//}
				for (int i = 0; i < 2; i++) {
					tick = Mustil.addNotes(track, tick, channel, vol, dur, keys);
					//tick = Mustil.addNotes(track, tick, channel, vol, dur * 2, new int[] {n});
				}
			}
		//}
		addCaveStrings(track, 0, 1);
		return song;
	}
	
	private final static void addCaveStrings(final Track track, final int tick, final int channel) throws Exception {
		Mustil.setInstrument(track, channel, Mustil.PRG_STRING_ENSEMBLE_1);
		final int vol = 60;
		Mustil.addNote(track, tick, 128, channel, 53, vol);
		Mustil.addNote(track, tick + 128, 128, channel, 52, vol);
		Mustil.addNote(track, tick + 256, 128, channel, 50, vol);
		Mustil.addNote(track, tick + 384, 128, channel, 48, vol);
	}
	
	protected final static Song newSongNight() throws Exception {
	    final Song song = newSong("Night");
        final Track track = song.track;
        channel = 0;
        vol = 68; // 72 for old mid
        deltaTick = 64;
        final int n1 = 64, n2 = 69, n3 = 72, n4 = 76;
        Mustil.unspecifiedNoteDuration = 32;
        Mustil.setInstrument(track, channel, Mustil.PRG_ACOUSTIC_GRAND_PIANO);
        Mustil.addNotes(track, 32, channel, vol, deltaTick,
            n2, n3, n2, n1, n2, n3, n4, n3);
        /*Mustil.unspecifiedNoteDuration = Mustil.DEF_NOTE_DURATION;
        addCreepy(track, 544, channel, vol);*/
        addCaveStrings(track, 0, 1); // 512
        return song;
	}
	
	protected final static Song newSongBridge() throws Exception {
        final Song song = newSong("Bridge");
        final Track track = song.track;
        channel = 0;
        vol = 80;
        final int d = 4, line = d * 16, section = 4 * line, volDrum = 96, channelWind = 1, volWind = 52;
        final int db = Mustil.PRC_BASS_DRUM_1, dm = Mustil.PRC_MID_TOM_1;
        Mustil.setInstrument(track, channel, Mustil.PRG_PICCOLO);
        Mustil.setInstrument(track, channelWind, Mustil.PRG_FLUTE);
        Mustil.unspecifiedNoteDuration = d;
        for (int j = 0; j < 4; j++) {
            final int b = j * section;
            for (int i = 0; i < 4; i++) {
                final int f = b + (i * line), b1;
                if (i == 1) {
                    b1 = 69;
                } else if (i == 3) {
                    b1 = 65;
                } else {
                    b1 = 67;
                }
                final int b2 = b1 + 2;
                Mustil.addNotes(track, f, channel, vol, d,
                    b1, b2, b1, b2, b1, -1, b2, -1, b1);
                if ((i % 2) == 0) {
                    Mustil.addNotes(track, f + (d * 10), channel, vol, d,
                        b2, -1, b1);
                }
                if ((j == 1) || (j == 2)) {
                    Mustil.addPercussionsAtVolume(track, f, volDrum, d,
                        db, -1, -1, -1, dm, -1, -1, -1, db, db, -1, db, dm);
                }
                if (j > 1) {
                    Mustil.addNote(track, f, line, channelWind, b1 + 12, volWind); // No +12 for old mid
                }
            }
        }
        return song;
	}
	
	protected final static Song newSongSnow() throws Exception {
        final Song song = newSong("Snow");
        final Track track = song.track;
        channel = 0;
        final int channel2 = 1;
        vol = 64;
        final int vol2 = 48;
        tick = 0;
        final int dur = 12, dur2 = 24;
        Mustil.setInstrument(track, channel, Mustil.PRG_TINKLE_BELL);
        Mustil.setInstrument(track, channel2, Mustil.PRG_CRYSTAL);
        for (int i = 0; i < 4; i++) {
            for (int k = 0; k < 4; k++) {
                if (k == 1) {
                    key = 79;
                } else if (k == 3) {
                    key = 83;
                } else {
                    key = 81;
                }
                if (i > 1) {
                    for (int j = 0; j < 2; j++) {
                        final long tj2 = tick + (j * 2 * dur2);
                        Mustil.addNote(track, tj2, dur2, channel2, key, vol2);
                        Mustil.addNote(track, tj2 + dur2, dur2, channel2, key - 2, vol2);
                    }
                }
                for (int v = 0; v < 5; v++) {
                    final int d = (v < 4) ? dur : (dur * 4);
                    Mustil.addNote(track, tick, d, channel, key, vol - (v * 4));
                    tick += d;
                }
            }
        }
        return song;
	}
	
	protected final static Song newSongSand() throws Exception {
        final Song song = newSong("Sand");
        final Track track = song.track;
        
        channel = Mustil.CHN_PERCUSSION;
        vol = 96; // 80 for old mid; 96 for new jet
        final int n = 16, p = 64, d = 8;
        final int p1 = Mustil.PRC_LOW_BONGO, p2 = Mustil.PRC_MID_TOM_1;
        for (int i = 0; i < n; i++) {
            Mustil.addPercussionsAtVolume(track, p * i, vol, d, p1, p2, p2, p1);
        }
        for (int i = 8; i <= n; i += 8) {
            Mustil.addPercussionsAtVolume(track, (p * i) - (d * 2), vol, d, p2, p2);
        }
        
        channel = 0;
        Mustil.setInstrument(track, channel, Mustil.PRG_BLOWN_BOTTLE);
        vol = 112; // 64 for old mid; 112 for new jet
        final int pb = p * 4;
        for (int i = 0; i < 2; i++) {
            final int b = i * pb;
            Mustil.addNote(track, b, 48, channel, 76, vol);
            Mustil.addNote(track, b + 49, 15, channel, 74, vol);
            Mustil.addNote(track, b + 65, 16, channel, 76, vol);
            
            tick = b + (p * 2) - 16;
            Mustil.addNote(track, tick, 15, channel, 77, vol);
            tick += 16;
            Mustil.addNote(track, tick, 48, channel, 76, vol);
            tick += 49;
            Mustil.addNote(track, tick, 15, channel, 74, vol);
            tick += 16;
            Mustil.addNote(track, tick, 16, channel, 76, vol);
        }
        final int h = 2;
        final int x = 16;
        int b = h * pb;
        Mustil.addNote(track, b, 64, channel, 77, vol);
        Mustil.addNote(track, b + 65, x, channel, 74, vol);
        final int t = 16, t1 = t - 1;
        tick = b + (p * 2) - (t * 2);
        Mustil.addNote(track, tick, t1, channel, 77, vol);
        tick += t;
        Mustil.addNote(track, tick, t1, channel, 76, vol);
        tick += t;
        Mustil.addNote(track, tick, 64, channel, 77, vol);
        tick += 65;
        Mustil.addNote(track, tick, 16, channel, 74, vol);
        
        b = (h + 1) * pb;
        Mustil.addNote(track, b, 64, channel, 77, vol);
        Mustil.addNote(track, b + 65, x, channel, 74, vol);
        tick = b + (p * 2) - (t * 2);
        Mustil.addNote(track, tick, t1, channel, 77, vol);
        tick += t;
        Mustil.addNote(track, tick, t1, channel, 76, vol);
        tick += t;
        Mustil.addNote(track, tick, 16, channel, 74, vol);
        
        return song;
	}
	
	protected final static Song newSongRock() throws Exception {
        final Song song = newSong("Rock");
        final Track track = song.track;
        channel = Mustil.CHN_PERCUSSION;
        vol = 120; // 64 for old mid
        final int cl = Mustil.PRC_MID_TOM_1; // HAND_CLAP for old mid
        final int lt = Mustil.PRC_LOW_TOM_1, ht = Mustil.PRC_HIGH_TOM_1;
        final int n = 9;
        for (int i = 0; i < n; i++) {
            Mustil.addPercussionsAtVolume(track, 128 * i, vol, 8,
                lt, -1, ht, ht, cl, ht, ht, -1,
                lt, -1, ht, ht, -1, lt, -1, -1);
        }
        
        channel = 0;
        vol = 76; // 64 for old mid
        final int dur = 48;
        Mustil.setInstrument(track, channel, Mustil.PRG_ACOUSTIC_GRAND_PIANO);
        Mustil.addNote(track, 128, dur, channel, 60, vol);
        Mustil.addNote(track, 256, dur, channel, 64, vol);
        Mustil.addNote(track, 384, dur, channel, 67, vol);
        Mustil.addNote(track, 504, 8, channel, 64, vol);
        Mustil.addNote(track, 512, dur, channel, 62, vol);
        
        Mustil.addNote(track, 640, dur, channel, 65, vol);
        Mustil.addNote(track, 760, 8, channel, 65, vol);
        Mustil.addNote(track, 768, dur, channel, 67, vol);
        Mustil.addNote(track, 896, dur, channel, 69, vol);
        Mustil.addNote(track, 1008, 8, channel, 64, vol);
        Mustil.addNote(track, 1016, 8, channel, 64, vol);
        Mustil.addNote(track, 1024, dur, channel, 62, vol);
        Mustil.addNote(track, (128 * n - 8), 8, channel, 48, SILENT);
        return song;
    }
	
	protected final static Song newSongHive() throws Exception {
        final Song song = newSong("Hive");
        final Track track = song.track;
        channel = 0;
        vol = 84;
        deltaTick = 5;
        final int n = 72, n2 = 74, n3 = 76;
        final int len = 64 * deltaTick;
        final int size = 2;
        Mustil.unspecifiedNoteDuration = deltaTick;
        Mustil.setInstrument(track, channel, Mustil.PRG_RECORDER);
        for (int i = 0; i < size; i++) {
            Mustil.addNotes(track, i * len, channel, vol, deltaTick,
                n2, -1, n2, -1, n, -1, n2, n2, -1, n, n, -1, -1, -1, -1, -1,
                n3, -1, n3, -1, n2, -1, n3, n3, -1, n2, n2, -1, -1, -1, -1, -1,
                n2, n2, -1, n, -1, -1, -1, -1,
                n3, n3, -1, n2, -1, -1, -1, -1,
                n2, -1, n2, -1, n, -1, n2, n2,
                -1, -1, -1, -1, -1, -1, -1, -1);
        }
        
        channel = Mustil.CHN_PERCUSSION;
        for (int i = 0; i < size; i++) {
            final int b = (i * len);
            int tick;
            tick = b + (deltaTick * 12);
            Mustil.addPercussion(track, tick, deltaTick, Mustil.PRC_BASS_DRUM_2);
            Mustil.addPercussion(track, tick + deltaTick, deltaTick, Mustil.PRC_BASS_DRUM_2);
            Mustil.addPercussion(track, b + (deltaTick * 28), deltaTick, Mustil.PRC_BASS_DRUM_2);
            Mustil.addPercussion(track, b + (deltaTick * 28) + deltaTick, deltaTick, Mustil.PRC_BASS_DRUM_2);
            Mustil.addPercussion(track, b + (deltaTick * 38), deltaTick, Mustil.PRC_BASS_DRUM_2);
            Mustil.addPercussion(track, b + (deltaTick * 46), deltaTick, Mustil.PRC_BASS_DRUM_2);
            tick = b + (len - (deltaTick * 5));
            Mustil.addPercussion(track, tick, deltaTick, Mustil.PRC_BASS_DRUM_2);
            Mustil.addPercussion(track, tick + deltaTick, deltaTick, Mustil.PRC_BASS_DRUM_2);
            Mustil.addPercussion(track, tick + (deltaTick * 3), deltaTick, Mustil.PRC_BASS_DRUM_1);
        }
        
        channel = 1;
        vol = 96; // 72 for old mid
        final int first = len, b = 59, amt = 2;
        Mustil.setInstrument(track, channel, Mustil.PRG_SLAP_BASS_2);
        spring(track, first, 64, channel, b, amt, vol);
        spring(track, first + 80, 64, channel, b, amt, vol);
        spring(track, first + 160, 32, channel, b, amt, vol);
        spring(track, first + 200, 32, channel, b, amt, vol);
        spring(track, first + 240, 16, channel, b, amt, vol);
        spring(track, first + 260, 16, channel, b, amt, vol);
        spring(track, first + 280, 16, channel, b, amt, vol);
        spring(track, first + 300, 16, channel, b, amt, vol);
        
        channel = 2;
        vol = 1;
        Mustil.setInstrument(track, channel, Mustil.PRG_ACOUSTIC_GRAND_PIANO);
        Mustil.addNote(track, (len * size) - deltaTick, deltaTick, channel, 48, vol);
        return song;
	}
	
	private final static void spring(final Track track, final int tick, final int dur, final int channel, final int key, final int amt, final int vol) throws Exception {
	    if (amt * (dur - 1) > 127) {
	        throw new IllegalArgumentException("Bend amount " + amt + " exceeds 127 for duration " + dur);
	    }
        Mustil.addNote(track, tick, dur, channel, key, vol);
        for (int i = 0; i < dur; i++) {
            Mustil.setPitch(track, tick + i, channel, i * amt);
        }
    }
	
	protected final static Song newSongJungle() throws Exception {
        final Song song = newSong("Jungle");
        final Track track = song.track;
        final int base = 8;
        channel = Mustil.CHN_PERCUSSION;
        deltaTick = base;
        Mustil.unspecifiedNoteDuration = deltaTick;
        final int lo = Mustil.PRC_LOW_CONGA, hi = Mustil.PRC_OPEN_HIGH_CONGA;
        Mustil.addRepeatedPercussions(track, 0, deltaTick, 16, lo, lo, hi, lo, hi, hi, -1, -1);
        channel = 0;
        vol = 40; // 48 for old mid
        deltaTick = base * 8;
        Mustil.unspecifiedNoteDuration = deltaTick;
        final int n1 = 84, n2 = n1 + 2, n3 = n1 + 4; // n1 = 60 for old mid
        Mustil.setInstrument(track, channel, Mustil.PRG_SHAKUHACHI);
        Mustil.addRepeatedNotes(track, 0, channel, vol, deltaTick, 2, n1, -1, n2, -1, n3, -1, n2, -1);
        channel = 1;
        vol = 64; // 72 for old mid
        deltaTick = base * 2;
        Mustil.unspecifiedNoteDuration = deltaTick;
        final int b = 72;
        Mustil.setInstrument(track, channel, Mustil.PRG_BIRD_TWEET);
        for (int i = 0; i < 4; i++) {
            Mustil.addNotes(track, base * 24 + (base * 32 * i), channel, vol, deltaTick, -1, b, -1, b);
        }
        channel = 2;
        vol = 68;
        tick = base * 64;
        final int h = base * 4, q = base * 2, e = base;
        final int p1 = 72, p2 = p1 + 2, p3 = p1 + 4, p4 = p1 + 7;
        Mustil.setInstrument(track, channel, Mustil.PRG_BRIGHT_ACOUSTIC_PIANO);
        Mustil.addNote(track, tick, h, channel, p2, vol);
        Mustil.addNote(track, tick + h, h, channel, p3, vol);
        Mustil.addNote(track, tick + 2 * h, q, channel, p2, vol);
        Mustil.addNote(track, tick + 2 * h + q, e, channel, p2, vol);
        Mustil.addNote(track, tick + 2 * h + q + e, e, channel, p2, vol);
        Mustil.addNote(track, tick + 3 * h, e, channel, p1, vol);
        Mustil.addNote(track, tick + 3 * h + e, e, channel, p1, vol);
        Mustil.addNote(track, tick + 4 * h, h, channel, p2, vol);
        Mustil.addNote(track, tick + 5 * h, h, channel, p3, vol);
        Mustil.addNote(track, tick + 6 * h, q, channel, p2, vol);
        Mustil.addNote(track, tick + 6 * h + q, q, channel, p2, vol);
        Mustil.addNote(track, tick + 7 * h, q, channel, p1, vol);
        Mustil.addNote(track, tick + 8 * h, h, channel, p2, vol);
        Mustil.addNote(track, tick + 9 * h, h, channel, p3, vol);
        Mustil.addNote(track, tick + 10 * h, h, channel, p4, vol);
        Mustil.addNote(track, tick + 11 * h, h, channel, p3, vol);
        Mustil.addNote(track, tick + 12 * h, h, channel, p2, vol);
        Mustil.addNote(track, tick + 13 * h, h, channel, p3, vol);
        Mustil.addNote(track, tick + 14 * h, h, channel, p2, vol);
        Mustil.addNote(track, tick + 15 * h, h, channel, p1, SILENT);
        return song;
	}
	
	protected final static Sequence newSongHappy2() throws Exception {
		final Sequence seq = new Sequence(Sequence.SMPTE_30, 1);
		final Track track = seq.createTrack();
		int dur, keys[];
		channel = 0;
		vol = 64;
		tick = 0;
		Mustil.setInstrument(track, channel, Mustil.PRG_TRUMPET);
		for (int k = 0; k < 2; k++) {
			for (int j = 0; j < 4; j++) {
				final int b = 60;
				final int n = ((j % 2) == 0) ? b : ((j == 1) ? (b + 4) : (b - 4)), n4 = n + 4, n8 = n + 8;
				if (k == 0) {
					dur = 4;
					Mustil.unspecifiedNoteDuration = 8;
					keys = new int[] {n, -1, n, -1, n8, -1, n4, -1, n, n, -1, n8, -1, -1, -1, -1,
							n, n, -1, n, -1, n, n, -1, n4, -1, -1, -1, -1, -1, -1, -1};
				} else {
					dur = 8;
					Mustil.unspecifiedNoteDuration = dur;
					keys = new int[] {n, n, n4, n, n8, n8, n4, -1, n, n, n4, n4, n, -1, n, -1};
				}
				for (int i = 0; i < 2; i++) {
					tick = Mustil.addNotes(track, tick, channel, vol, dur, keys);
				}
			}
		}
		return seq;
	}
	
	protected final static Sequence newSongHappy3() throws Exception {
		final Sequence seq = new Sequence(Sequence.SMPTE_30, 1);
		final Track track = seq.createTrack();
		int dur, keys[];
		channel = Mustil.CHN_PERCUSSION;
		vol = 64;
		tick = 0;
		dur = 4;
		/*keys = new int[] {
				Mustil.PRC_CLOSED_HI_HAT, -1, -1, Mustil.PRC_CLOSED_HI_HAT, Mustil.PRC_RIDE_CYMBAL_1, -1, -1, -1};
		tick = Mustil.addPercussions(track, tick, dur, keys);*/
		keys = new int[] {
				Mustil.PRC_CLOSED_HI_HAT, -1, -1, Mustil.PRC_CLOSED_HI_HAT, Mustil.PRC_RIDE_CYMBAL_1, -1, -1, -1,
				Mustil.PRC_CLOSED_HI_HAT, -1, -1, Mustil.PRC_CLOSED_HI_HAT, Mustil.PRC_RIDE_CYMBAL_1, -1, -1, -1,
				Mustil.PRC_CLOSED_HI_HAT, -1, -1, Mustil.PRC_CLOSED_HI_HAT, Mustil.PRC_RIDE_CYMBAL_1, -1, -1, -1,
				Mustil.PRC_CLOSED_HI_HAT, -1, -1, Mustil.PRC_CLOSED_HI_HAT, Mustil.PRC_RIDE_CYMBAL_1, -1, -1, -1,
				Mustil.PRC_CLOSED_HI_HAT, -1, -1, -1, -1, -1, -1, -1};
		tick = Mustil.addRepeatedPercussions(track, tick, dur, 8, keys);
		channel = 0;
		Mustil.setInstrument(track, channel, Mustil.PRG_HONKY_TONK_PIANO);
		final int n = 60, n4 = n + 4, n8 = n + 8;
		tick = 0; //128;
		dur = 2;
		Mustil.unspecifiedNoteDuration = 8;
		/*
		1010001101230000
		
		1010001102203000
		1201201200102000
		1012000110220300
		2301202301020000
		*/
		keys = new int[] {n, -1, -1, -1, -1, -1, -1, -1, n, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
				n, -1, -1, n, -1, -1, -1, n, -1, -1, -1, n4, -1, -1, -1, n8, -1, -1, -1,
				-1, -1, -1, -1, -1, -1, -1};
		tick = Mustil.addNotes(track, tick, channel, vol, dur, keys);
		return seq;
	}
	
	protected final static void addPercussionHappy(final Track track, final int r, final boolean full) throws Exception {
		vol = 56;
		channel = Mustil.CHN_PERCUSSION;
		final int dur = 4, c = Mustil.PRC_RIDE_CYMBAL_1, d = Mustil.PRC_CLOSED_HI_HAT, f = full ? d : -1, keys[];
		keys = new int[] {
				c, -1, d, d,
				c, -1, f, f,
				c, -1, d, d,
				c, -1, f, f,
				c, -1, d, d,
				c, -1, f, f,
				c, -1, -1, -1,
				-1, -1, -1, -1};
		tick = Mustil.addRepeatedPercussions(track, tick, dur, r, keys);
	}
	
	protected final static void addBell(final Track track, final int r) throws Exception {
		vol = 76;
		channel = 2;
		Mustil.unspecifiedNoteDuration = 16;
		Mustil.setInstrument(track, channel, Mustil.PRG_TUBULAR_BELLS);
		final int dur = 8;
		final int[] keys = new int[] {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 96, -1};
		tick = Mustil.addRepeatedNotes(track, tick, channel, vol, dur, r, keys);
	}
	
	protected final static Song newSongMinecart() throws Exception {
		final Song song = newSong("Minecart");
		final Track track = song.track;
		int dur, keys[];
		final int r = 6;
		addPercussionHappy(track, r, true);
		
		channel = 0;
		vol = 60;
		Mustil.setInstrument(track, channel, Mustil.PRG_TUBA);
		final int n = 48, n2 = n + 2, n4 = n + 4; // n = 36 for old mid
		tick = 0;
		dur = 8;
		Mustil.unspecifiedNoteDuration = 8;
		keys = new int[] {n4, -1, n, -1, n4, -1, n, -1, n4, -1, n, -1, n4, n2, n, n2}; // n4, n, n, n4
		tick = Mustil.addRepeatedNotes(track, tick, channel, vol, dur, r, keys);
		tick = 0;
		addBell(track, r); // channel 2
		
		/*tick = 0;
		addBell(track, r);
		Mustil.unspecifiedNoteDuration = 8;
		tick = 128;
		vol = 60;
		channel = 3;
		Mustil.setInstrument(track, channel, Mustil.PRG_HONKY_TONK_PIANO);
		key = 62; // 68
		tick = addMainHappy(track);
		key = 50; // 56
		tick = addMainHappy(track, 2);
		key = 62; // 68
		tick = addFastHappy(track);
		key = 50; // 56
		tick = addFastHappy2(track);
		key = 62; // 68
		tick = addMainHappy(track);
		key = 62; // 68
		tick = addEndHappy(track);*/
		
		tick = 256;
		dur = 32;
		Mustil.unspecifiedNoteDuration = 16;
		vol = 48;
		channel = 1;
		Mustil.setInstrument(track, channel, Mustil.PRG_TROMBONE);
		keys = new int[] {65, 67, 69, 71};
		tick = Mustil.addRepeatedNotes(track, tick, channel, vol, dur, 3, keys);
		keys = new int[] {74, 69, 67, 65};
		tick = Mustil.addNotes(track, tick, channel, vol, dur, keys);
		return song;
	}
	
	protected final static Song newSongGrass() throws Exception {
		final Song song = newSong("Happy");
		final Track track = song.track;
		addPercussionHappy(track, 8, true);
		//addPercussionHappy(track, 4, true);
		//addPercussionHappy(track, 4, false);
		final int len = 512;
		for (int instr = 0; instr < 2; instr++) {
			tick = instr * len + 384;
			addBell(track, 1);
		}
		Mustil.unspecifiedNoteDuration = 16;
		vol = 60;
		deltaTick = 8;
		for (int instr = 0; instr < 2; instr++) {
			final int prg, start = instr * len;
			if (instr == 0) {
				channel = 1;
				prg = Mustil.PRG_HONKY_TONK_PIANO;
			} else {
				//Mustil.unspecifiedNoteDuration = 4;
				channel = 3;
				prg = Mustil.PRG_XYLOPHONE;
			}
			Mustil.setInstrument(track, channel, prg);
			for (int i = 0; i < 3; i++) {
				tick = start + i * 128;
				final int o = (i == 1) ? 67 : 60;
				final int a = o + 9, g = o + 7, f = o + 5;
				tick = Mustil.addNotes(track, tick, channel, vol, 16, a, g, f);
				tick = Mustil.addNotes(track, tick, channel, vol, 4, g, a);
				tick = Mustil.addNotes(track, tick, channel, vol, 8, a, a);
			}
			final int a = 69, g = 67; // f = 65
			for (int i = 0; i < 2; i++) {
				tick = start + 368 + (64 * i);
				tick = Mustil.addNotes(track, tick, channel, vol, 4, g, a);
				tick = Mustil.addNotes(track, tick, channel, vol, 8, a, a);
			}
		}
		return song;
	}
	
	// Map/Menu - 2 0 2 0 2 2 2 0 3 00000 2 0 2 0 2 2 2 0 1 00000 2 0 2 0 2 2 2 0 3 0 0 0 2 2 2 0 3
	
	protected final static Song newSongMenu() throws Exception {
		final Song song = newSong("Menu");
		final Track track = song.track;
		//int dur, keys[];
		addPercussionHappy(track, 2, false);
		tick = 128;
		addBell(track, 1);
		/*channel = 0;
		vol = 40;
		Mustil.setInstrument(track, channel, Mustil.PRG_HONKY_TONK_PIANO);
		final int d = 4;
		final int n = 64, n1 = n + d, n2 = n1 + d, n3 = n2 + d, n4 = n3 + d;
		tick = 0;
		dur = 8;
		Mustil.unspecifiedNoteDuration = 8;
		keys = new int[] {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, n, n2, n3, n4,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, n4, n3, n2, n,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, n, n2, n3, n4,
				-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, n4, n3, n2, n};
		tick = Mustil.addNotes(track, tick, channel, vol, dur, keys);*/
		return song;
	}
	
	protected final static Song newSongHeartbeat() throws Exception {
		final Song song = newSong("Heartbeat");
		final Track track = song.track;
		channel = 0;
		vol = Mustil.VOL_MAX;
		deltaTick = 8;
		tick = 0;
		for (int i = 0; i < 8; i++) {
			addHeartbeat(track);
			tick += 64;
		}
		channel = 2;
		Mustil.setInstrument(track, channel, Mustil.PRG_STRING_ENSEMBLE_1);
		Mustil.addNote(track, 264, 128, channel, 52, 104);
		//Mustil.addNote(track, 264, 120, channel, 52, 104);
		Mustil.addNote(track, 392, 120, channel, 50, 100);
		return song;
	}
	
	private final static void addHeartbeat(final Track track) throws Exception {
		for (int i = 0; i < 2; i++) {
			channel = i;
			final int prg, n;
			if (i == 0) {
				prg = Mustil.PRG_ACOUSTIC_BASS; // PRG_ELECTRIC_PIANO_1
				n = 32;
				Mustil.unspecifiedNoteDuration = 56;
			/*} else if (i == 1) {
				prg = Mustil.PRG_ELECTRIC_BASS_FINGER;
				n = 32;
				Mustil.unspecifiedNoteDuration = 48;*/
			} else {
				prg = Mustil.PRG_ACOUSTIC_GRAND_PIANO;
				n = 32;
				Mustil.unspecifiedNoteDuration = 48;
			}
			Mustil.setInstrument(track, channel, prg);
			Mustil.addNotes(track, tick, channel, vol, deltaTick,
					n, n, -1, -1, -1, -1, -1, -1);
		}
		Mustil.addPercussionsAtVolume(track, tick, 64, deltaTick,
				-1, -1, -1, -1, -1, Mustil.PRC_HIGH_BONGO, -1, -1);
	}
	
	protected final static Song newSongChant() throws Exception {
		final Song song = newSong("Chant");
		final Track track = song.track;
		Mustil.unspecifiedNoteDuration = 60;
		channel = 0;
		vol = 64;
		deltaTick = 60;
		Mustil.setInstrument(track, channel, Mustil.PRG_SOUNDTRACK); // PRG_CHOIR_AAHS
		tick = 0;
		tick = Mustil.addNotes(track, tick, channel, vol, deltaTick, 56, 48);
		Mustil.unspecifiedNoteDuration = 15;
		tick = Mustil.addNotes(track, tick, channel, vol, Mustil.unspecifiedNoteDuration, 56, 58, 60, 56);
		Mustil.unspecifiedNoteDuration = 60;
		tick = Mustil.addNotes(track, tick, channel, vol, deltaTick, 48, 56, 48);
		Mustil.unspecifiedNoteDuration = 30;
		tick = Mustil.addNotes(track, tick, channel, vol, Mustil.unspecifiedNoteDuration, 56, 58);
		Mustil.unspecifiedNoteDuration = 60;
		tick = Mustil.addNotes(track, tick, channel, vol, deltaTick, 48);
		return song;
	}
	
	protected final static Song newSongOcarina() throws Exception {
		final Song song = newSong("Ocarina");
		final Track track = song.track;
		Mustil.unspecifiedNoteDuration = 7;
		channel = 0;
		vol = 96;
		deltaTick = 8;
		Mustil.setInstrument(track, channel, Mustil.PRG_OCARINA);
		tick = 0;
		final int n1 = 68, n2 = 71, n3 = 74;
		tick = Mustil.addNotes(track, tick, channel, vol, deltaTick, true, 
				n1, -1, n2, -1, n1, n1, n2, -1,
				n1, n1, n2, n2, n3, n3, n2, -1,
				n1, -1, n2, -1, n1, n1, n2, -1,
				n1, -1, n2, -1, n3, -1, -1, -1);
		tick = 0;
		deltaTick = 16;
		final int p1 = Mustil.PRC_HAND_CLAP, p2 = Mustil.PRC_HIGH_BONGO;
		tick = Mustil.addPercussionsAtVolume(track, tick, 64, deltaTick,
				p1, p2, p1, p2, p1, p2, p1, p2,
				p1, p2, p1, p2, p1, p2, p1, p2);
		return song;
	}
	
	protected final static Song newSongLevelStart() throws Exception {
		final Song song = newSong("LevelStart");
		final Track track = song.track;
		Mustil.unspecifiedNoteDuration = 2;
		channel = 0;
		vol = 52;
		deltaTick = 2;
		Mustil.setInstrument(track, channel, Mustil.PRG_XYLOPHONE);
		tick = 0;
		final int n1 = 68, n2 = n1 + 4, n3 = n2 + 4, n4 = n3 + 4, n5 = n4 + 4;
		tick = Mustil.addNotes(track, tick, channel, vol, deltaTick,
				n1, n2, n3, n2,
				n1, n2, n3, n2,
				n1, n2, n3, n4);
		Mustil.addNote(track, tick, 8, channel, n5, vol);
		return song;
	}
	
	protected final static Song newSongLevelEnd() throws Exception {
		final Song song = newSong("LevelEnd");
		final Track track = song.track;
		Mustil.unspecifiedNoteDuration = 60;
		channel = 0;
		vol = 72;
		deltaTick = 30;
		Mustil.setInstrument(track, channel, Mustil.PRG_TUBULAR_BELLS);
		/*tick = Mustil.addNotes(track, 0, channel, vol, deltaTick,
				64, 72, 68);*/
		Mustil.addNote(track, 0, channel, 64, vol);
		Mustil.addNote(track, 8, channel, 65, vol);
		Mustil.addNote(track, 38, channel, 65, 52);
		Mustil.addNote(track, 68, channel, 65, 32);
		return song;
	}
	
	protected final static Song newSongLevelEnd3() throws Exception {
		final Song song = newSong("LevelEnd");
		final Track track = song.track;
		Mustil.unspecifiedNoteDuration = 4;
		channel = 0;
		vol = 64;
		deltaTick = 4;
		Mustil.setInstrument(track, channel, Mustil.PRG_XYLOPHONE);
		Mustil.setInstrument(track, 1, Mustil.PRG_TUBULAR_BELLS);
		final int n1 = 68, n2 = n1 + 4, n3 = n2 + 4, n4 = n3 + 4, n5 = n4 + 4;
		Mustil.addNote(track, 0, 30, 1, 64, 72);
		tick = Mustil.addNotes(track, 30, channel, vol, deltaTick,
				n1, n2, -1, n3,
				n2, n3, -1, n4,
				n1, n2, n3, n4);
		Mustil.addNote(track, tick, 16, channel, n5, vol);
		return song;
	}
	
	protected final static Song newSongLevelEnd2() throws Exception {
		final Song song = newSong("LevelEnd");
		final Track track = song.track;
		Mustil.unspecifiedNoteDuration = 60;
		channel = 0;
		vol = Mustil.VOL_MAX;
		deltaTick = 8;
		Mustil.setInstrument(track, channel, Mustil.PRG_TUBULAR_BELLS);
		tick = 0;
		/*tick = Mustil.addNotes(track, tick, channel, vol, deltaTick,
				64, 56, -1, 56, 64, -1, 56, 64, 72);*/
		tick = Mustil.addNotes(track, tick, channel, vol, deltaTick,
				56);
		vol = 112;
		tick = Mustil.addNotes(track, tick, channel, vol, deltaTick,
				-1, -1, -1, 64);
		vol = 96;
		tick = Mustil.addNotes(track, tick, channel, vol, deltaTick,
				-1, -1, -1, 56, 56, 64, 56, 72
				, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1);
		/*tick = Mustil.addNotes(track, tick, channel, vol, deltaTick, 64);
		Mustil.unspecifiedNoteDuration = 15;
		channel = 1;
		vol = 72;
		deltaTick = 4;
		Mustil.setInstrument(track, channel, Mustil.PRG_STRING_ENSEMBLE_1);
		tick = 0;
		tick = Mustil.addNotes(track, tick, channel, vol, deltaTick,
				//-1, -1,
				64, -1, 60, 56, 60, -1, 56, 60, 64, 68, 64, -1, 56, 60, 64, 68, 72, 76
				, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1
				, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
				);*/
		Mustil.unspecifiedNoteDuration = 24;
		channel = 1;
		vol = 72;
		Mustil.setInstrument(track, channel, Mustil.PRG_TRUMPET);
		tick = 0;
		tick = Mustil.addNotes(track, tick, channel, vol, deltaTick,
				-1, -1, 60, 62, -1, -1, 64, 66, -1, -1, 68, -1, 72);
		return song;
	}
	
	protected final static Song newSongTest() throws Exception {
		final Song song = newSong("Test");
		final Track track = song.track;
		Mustil.unspecifiedNoteDuration = 30;
		channel = 0;
		vol = 64;
		deltaTick = 30;
		Mustil.setInstrument(track, channel, Mustil.PRG_ACOUSTIC_GRAND_PIANO);
		tick = 0;
		tick = Mustil.addNotes(track, tick, channel, vol, deltaTick,
				60, 62, 64, 65, 67, 69, 71, 72
				-1, -1, -1, -1,
				60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72
				-1, -1, -1, -1);
		return song;
	}
	
	protected final static Song newFxGem(/*final int mag*/) throws Exception {
		final int channel = 0, vol = 64;
		final Song song = newSong("Gem");
		final Track track = song.track;
		/*Mustil.setInstrument(track, channel, Mustil.PRG_TINKLE_BELL);
		final int d = 2;
		for (int j = 0; j <= mag; j++) {
			for (int i = 0; i < 4; i++) {
				Mustil.addNote(track, (j * 6 + i) * d, d, channel, 72 + i * 4, vol);
			}
			if (j == mag) {
				Mustil.addNote(track, (j * 6 + 4) * d, 4, channel, 88, vol);
				//Mustil.setVolume(track, 10, channel, 32);
				//Mustil.setVolume(track, 12, channel, 127);
			} else {
				for (int i = 0; i < 2; i++) {
					Mustil.addNote(track, (j * 6 + 4 + i) * d, d, channel, 80 - i * 4, vol);
				}
			}
		}*/
		Mustil.setInstrument(track, channel, Mustil.PRG_MUSIC_BOX);
		Mustil.addNote(track, 0, 8, channel, 64, vol);
		return song;
	}
	
	protected final static Song newFxCrumble() throws Exception {
		final int channel = 0, vol = 80;
		final Song song = newSong("Crumble");
		final Track track = song.track;
		Mustil.setInstrument(track, channel, Mustil.PRG_MELODIC_TOM);
		for (int i = 0; i < 6; i++) {
			Mustil.addNote(track, i, 1, channel, 62 - i * 4, vol);
			Mustil.addNote(track, i + 1, 1, channel, 66 - i * 4, vol);
		}
		return song;
	}
	
	protected final static Song newFxThud() throws Exception {
		final Song song = newSong("Thud");
		final Track track = song.track;
		Mustil.addPercussion(track, 0, Mustil.PRC_HIGH_BONGO);
		Mustil.setInstrument(track, channel, Mustil.PRG_MUSIC_BOX);
		Mustil.addNote(track, 0, 8, channel, 28, 56);
		channel = 1;
		Mustil.setInstrument(track, channel, Mustil.PRG_XYLOPHONE);
		Mustil.addNote(track, 0, 8, channel, 28, Mustil.VOL_MAX);
		return song;
	}
	
	protected final static Song newFxJump() throws Exception {
		final int channel = 0, vol = 40;
		final Song song = newSong("Jump");
		final Track track = song.track;
		Mustil.setInstrument(track, channel, Mustil.PRG_WHISTLE); // PRG_SLAP_BASS_2
		Mustil.addNote(track, 0, 8, channel, 77, vol);
		//Mustil.addNote(track, 0, 8, channel, 54, vol);
		//Mustil.setPitch(track, 2, channel, 80);
		//Mustil.setPitch(track, 4, channel, 96);
		//Mustil.setPitch(track, 6, channel, 80);
		Mustil.setPitch(track, 4, channel, 88);
		return song;
	}
	
	protected final static Song newFxBounce() throws Exception {
		final int channel = 0;
		final Song song = newSong("Bounce");
		final Track track = song.track;
		Mustil.setInstrument(track, channel, Mustil.PRG_SLAP_BASS_2);
		Mustil.addNote(track, 0, 8, channel, 48, 72);
		Mustil.setPitch(track, 4, channel, 88);
		return song;
	}
	
	protected final static Song newFxArmor() throws Exception {
		final Song song = newSong("Armor");
		final Track track = song.track;
		Mustil.addPercussionAtVolume(track, 0, 8, Mustil.PRC_HIGH_BONGO, 96);
		return song;
	}
	
	protected final static Song newFxWhoosh() throws Exception {
		final Song song = newSong("Whoosh");
		final Track track = song.track;
		/*Mustil.setInstrument(track, channel, Mustil.PRG_BLOWN_BOTTLE);
		Mustil.addNote(track, 0, 2, channel, 68, 60);
		Mustil.addNote(track, 2, 2, channel, 64, 45);
		Mustil.addNote(track, 4, 4, channel, 60, 30);
		Mustil.setInstrument(track, 1, Mustil.PRG_PAN_FLUTE);
		Mustil.addNote(track, 0, 8, 1, 64, 30);
		Mustil.setInstrument(track, 2, Mustil.PRG_CHOIR_AAHS);
		Mustil.addNote(track, 0, 8, 2, 64, 25);*/
		Mustil.setInstrument(track, channel, Mustil.PRG_PAN_FLUTE);
		Mustil.addNote(track, 0, 8, channel, 40, Mustil.VOL_MAX);
		Mustil.setInstrument(track, 1, Mustil.PRG_CHOIR_AAHS);
		Mustil.addNote(track, 0, 8, 1, 40, 112);
		return song;
	}
	
	private final static Song newSong(final String name) throws Exception {
        return new Song(name, COPYRIGHT);
    }
	
	private final static void run(final String[] args) throws Exception {
		if ("load".equalsIgnoreCase(Coltil.get(args, 0))) {
			runLoad();
		} else {
			runGen();
		}
	}
	
	private final static void runGen() throws Exception {
		final Song song = newSongMinecart();
		System.out.println("Starting " + song.name);
		//Mustil.save(song.seq, song.name.toLowerCase() + ".mid");
		final Panaudio music = Pangine.getEngine().getAudio();
		//music.ensureCapacity(4);
		//music.playMusic(seq);
		//new org.pandcorps.pandam.lwjgl.JavaxMidiPansound(song.seq).startMusic();
		System.out.println("Started; press enter to play sound; press x and enter to stop");
		while (!Iotil.readln().equals("x")) {
			//music.playSound(jump);
		}
		music.close();
		System.out.println("End");
	}
	
	private final static void runLoad() throws Exception {
		Pangine.getEngine().getAudio().createMusic(FurGuardiansGame.RES + "music/happy.mid").startMusic();
	}
	
	public final static void main(final String[] args) {
		try {
			run(args);
		} catch (final Throwable e) {
			e.printStackTrace();
		}
	}
}
