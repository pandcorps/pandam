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
package org.pandcorps.championsofslam;

import javax.sound.midi.*;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;

public class SlamMusic {
    private final static String COPYRIGHT = "Copyright (c) " + ChampionsOfSlamGame.YEAR + ", " + ChampionsOfSlamGame.AUTHOR;
	
	private final static int SILENT = 1;
	
	private static int channel = 0, key, vol, deltaTick;
	private static long tick = 0;
	
	protected final static Song newSongCave() throws Exception {
		final Song song = new Song("Cave");
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
	    final Song song = new Song("Night");
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
        final Song song = new Song("Bridge");
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
        final Song song = new Song("Snow");
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
        final Song song = new Song("Sand");
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
        final Song song = new Song("Rock");
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
        final Song song = new Song("Hive");
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
        final Song song = new Song("Jungle");
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
	
	protected final static Sequence newSequence() throws Exception {
		return new Sequence(Sequence.PPQ, 96);
	}
	
	protected final static Track newTrack(final Sequence seq, final String name) throws Exception {
		final Track track = seq.createTrack();
		Mustil.setName(track, name);
		Mustil.setCopyright(track, COPYRIGHT);
		//Mustil.setTimeSignature(track, 4, 2, 30, 8);
		Mustil.setDefaultTempo(track);
		return track;
	}
	
	private final static class Song {
		private final String name;
		private final Sequence seq;
		private final Track track;
		
		private Song(final String name) throws Exception {
			this.name = name;
			seq = newSequence();
			track = newTrack(seq, name);
		}
	}
	
	protected final static Song newSongSlam() throws Exception {
		final Song song = new Song("Champions of Slam");
		final Track track = song.track;
		int dur, keys[];
		final int r = 32;
		
		channel = 0;
		vol = 60;
		Mustil.setInstrument(track, channel, Mustil.PRG_ELECTRIC_BASS_FINGER);
		tick = 0;
		dur = 8;
		Mustil.unspecifiedNoteDuration = 8;
		keys = new int[] {38, 36};
		tick = Mustil.addRepeatedNotes(track, tick, channel, vol, dur, r, keys);
		tick = 0;
		
		return song;
	}
	
	protected final static Song newSongTest() throws Exception {
		final Song song = new Song("Test");
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
	
	private final static void run(final String[] args) throws Exception {
		if ("load".equalsIgnoreCase(Coltil.get(args, 0))) {
			runLoad();
		} else {
			runGen();
		}
	}
	
	private final static void runGen() throws Exception {
		final Song song = newSongSlam();
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
		Pangine.getEngine().getAudio().createMusic("C:\\Personal\\Research\\Slam.mid").startMusic();
	}
	
	public final static void main(final String[] args) {
		try {
			run(args);
		} catch (final Throwable e) {
			e.printStackTrace();
		}
	}
}
