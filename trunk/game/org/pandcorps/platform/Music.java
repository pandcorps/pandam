/*
Copyright (c) 2009-2014, Andrew M. Martin
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
package org.pandcorps.platform;

import javax.sound.midi.*;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;

public class Music {
	private final static String COPYRIGHT = "Copyright (c) 2014, Andrew M. Martin";
	
	protected final static Sequence gem;
	protected final static Sequence gemLevel;
	protected final static Sequence crumble;
	protected final static Sequence thud;
	protected final static Sequence jump;
	
	static {
		try {
			gem = newFxGem(0);
			gemLevel = newFxGem(1);
			crumble = newFxCrumble();
			thud = newFxThud();
			jump = newFxJump();
		} catch (final Exception e) {
			throw Pantil.toRuntimeException(e);
		}
	}
	
	protected final static Sequence newSongCreepy() throws Exception {
		// channel 0 - 15; key/vol 0 - 127
		final int channel = 0, key = 64, vol = 64;
		final Sequence seq = new Sequence(Sequence.SMPTE_30, 1);
		final Track track = seq.createTrack();
		// Track has 16 channels for different instruments; they can be changed to 128 possible instruments
		//Mustil.setInstrument(track, channel, Mustil.PRG_BIRD_TWEET); // PRG_CRYSTAL
		Mustil.addNote(track, 0, channel, key, vol);
		Mustil.addNote(track, 15, channel, 68, vol);
		Mustil.addNote(track, 30, channel, 72, vol);
		return seq;
	}
	
	private static int channel, key, vol, deltaTick;
	private static long tick;
	
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
				key, key, key, key - 4, key + 4, key + 4, key + 8);
		tick += 8;
		tick = Mustil.addNotes(track, tick, channel, vol, 8,
				key, key, key + (4 * multPeak), key + (8 * multPeak), key + (12 * multPeak));
		tick += 24;
		return tick;
	}
	
	protected final static long addFastHappy(final Track track) throws Exception {
		tick = Mustil.addNotes(track, tick, channel, vol, 4,
				key, -1, key, key, -1, key, key, -1, key + 4, -1, key + 4);
		tick += 20;
		tick = Mustil.addNotes(track, tick, channel, vol, 4,
				key, -1, key, key, -1, key, key, -1, key + 8);
		tick += 28;
		return tick;
	}
	
	protected final static long addFastHappy2(final Track track) throws Exception {
		tick = Mustil.addNotes(track, tick, channel, vol, 4,
				key, -1, key, key, -1, key, key, -1, key + 4, -1, key + 4);
		tick += 20;
		tick = Mustil.addNotes(track, tick, channel, vol, 4,
				key, -1, key + 8, key + 8, -1, key + 16, key + 16, -1, key + 24);
		tick += 28;
		return tick;
	}
	
	protected final static long addEndHappy(final Track track) throws Exception {
		tick = Mustil.addNotes(track, tick, channel, vol, 8,
				key, key, key, key - 4, key - 8, key - 4, key);
		tick += 8;
		tick = Mustil.addNotes(track, tick, channel, vol, 4,
				key, -1, key - 4, key - 4, key, -1, key, -1);
		Mustil.addNote(track, tick, 32, channel, key - 4, vol);
		tick += 32;
		return tick;
	}
	
	protected final static Sequence newSongTechno() throws Exception {
		final Sequence seq = new Sequence(Sequence.SMPTE_30, 1);
		final Track track = seq.createTrack();
		//final int length = 2048;
		int dur, keys[];
		/*channel = 0;
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
		channel = 3;
		vol = 64;
		tick = 0; //128;
		// Underground = PRG_GLOCKENSPIEL, b=56?
		Mustil.setInstrument(track, channel, Mustil.PRG_XYLOPHONE); // PRG_CRYSTAL
		for (int k = 0; k < 2; k++) {
			for (int j = 0; j < 4; j++) {
				final int b = 56;
				final int n = ((j % 2) == 0) ? b : ((j == 1) ? (b + 4) : (b - 4)), n4 = n + 4, n8 = n + 8;
				if (k == 1) {
					dur = 4;
					Mustil.unspecifiedNoteDuration = 8;
					keys = new int[] {n, -1, n, n, n4, -1, n, n, n8, -1, n8, -1, n4, -1, -1, -1,
							n, -1, n, n, n4, -1, n4, n4, n, -1, -1, -1, n, -1, -1, -1};
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
	
	protected final static void addPercussionHappy(final Track track, final int r) throws Exception {
		vol = 64;
		channel = Mustil.CHN_PERCUSSION;
		tick = 0;
		final int dur = 4;
		final int[] keys = new int[] {
				Mustil.PRC_RIDE_CYMBAL_1, -1, Mustil.PRC_CLOSED_HI_HAT, Mustil.PRC_CLOSED_HI_HAT,
				Mustil.PRC_RIDE_CYMBAL_1, -1, Mustil.PRC_CLOSED_HI_HAT, Mustil.PRC_CLOSED_HI_HAT,
				Mustil.PRC_RIDE_CYMBAL_1, -1, Mustil.PRC_CLOSED_HI_HAT, Mustil.PRC_CLOSED_HI_HAT,
				Mustil.PRC_RIDE_CYMBAL_1, -1, Mustil.PRC_CLOSED_HI_HAT, Mustil.PRC_CLOSED_HI_HAT,
				Mustil.PRC_RIDE_CYMBAL_1, -1, Mustil.PRC_CLOSED_HI_HAT, Mustil.PRC_CLOSED_HI_HAT,
				Mustil.PRC_RIDE_CYMBAL_1, -1, Mustil.PRC_CLOSED_HI_HAT, Mustil.PRC_CLOSED_HI_HAT,
				Mustil.PRC_RIDE_CYMBAL_1, -1, -1, -1,
				-1, -1, -1, -1};
		tick = Mustil.addRepeatedPercussions(track, tick, dur, r, keys);
	}
	
	protected final static void addBell(final Track track, final int r) throws Exception {
		vol = 88;
		channel = 2;
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
	
	protected final static Song newSongHappy4() throws Exception {
		final Song song = new Song("Happy");
		final Track track = song.track;
		int dur, keys[];
		final int r = 7;
		addPercussionHappy(track, r);
		channel = 0;
		vol = 56;
		Mustil.setInstrument(track, channel, Mustil.PRG_TUBA);
		final int d = 1;
		final int n = 48, n1 = n + d, n2 = n1 + d, n3 = n2 + d, n4 = n3 + d;
		tick = 0;
		dur = 8;
		Mustil.unspecifiedNoteDuration = 8;
		keys = new int[] {n4, -1, n, -1, n4, -1, n, -1, n4, -1, n, -1, n4, n2, n, n2}; // n4, n, n, n4
		tick = Mustil.addRepeatedNotes(track, tick, channel, vol, dur, r, keys);
		tick = 128;
		addBell(track, r - 1);
		tick = 128;
		vol = 80;
		channel = 1;
		Mustil.setInstrument(track, channel, Mustil.PRG_HONKY_TONK_PIANO);
		key = 68;
		tick = addMainHappy(track);
		key = 56;
		tick = addMainHappy(track, 2);
		key = 68;
		tick = addFastHappy(track);
		key = 56;
		tick = addFastHappy2(track);
		key = 68;
		tick = addMainHappy(track);
		key = 72;
		tick = addEndHappy(track);
		/*tick = 0;
		dur = 32;
		Mustil.unspecifiedNoteDuration = 16;
		vol = 16;
		channel = 3;
		Mustil.setInstrument(track, channel, Mustil.PRG_TROMBONE);
		keys = new int[] {60, 64, 68, 72};
		tick = Mustil.addRepeatedNotes(track, tick, channel, vol, dur, r, keys);*/
		return song;
	}
	
	// Map/Menu - 2 0 2 0 2 2 2 0 3 00000 2 0 2 0 2 2 2 0 1 00000 2 0 2 0 2 2 2 0 3 0 0 0 2 2 2 0 3
	
	protected final static Sequence newSongMenu() throws Exception {
		final Sequence seq = new Sequence(Sequence.SMPTE_30, 1);
		final Track track = seq.createTrack();
		//int dur, keys[];
		final int r = 1;
		addPercussionHappy(track, r);
		tick = 0;
		Mustil.unspecifiedNoteDuration = 16;
		addBell(track, r);
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
		return seq;
	}
	
	protected final static Song newSongHeartbeat() throws Exception {
		final Song song = new Song("Heartbeat");
		final Track track = song.track;
		Mustil.unspecifiedNoteDuration = 56;
		channel = 0;
		vol = Mustil.VOL_MAX;
		deltaTick = 8;
		Mustil.setInstrument(track, channel, Mustil.PRG_ELECTRIC_PIANO_1);
		final int n = 32;
		Mustil.addNotes(track, 0, channel, vol, deltaTick,
				n, n, -1, -1, -1, -1, -1, -1);
		Mustil.addPercussionsAtVolume(track, 0, 48, deltaTick,
				-1, -1, -1, -1, -1, Mustil.PRC_HIGH_BONGO, -1, -1);
		return song;
	}
	
	private final static Sequence newFxGem(final int mag) throws Exception {
		//Mustil.PRG_MUSIC_BOX, key = 64, dur = 8
		final int channel = 0, vol = 64;
		final Sequence seq = new Sequence(Sequence.SMPTE_30, 1);
		final Track track = seq.createTrack();
		Mustil.setInstrument(track, channel, Mustil.PRG_TINKLE_BELL);
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
		}
		return seq;
	}
	
	private final static Sequence newFxCrumble() throws Exception {
		final int channel = 0, vol = 80;
		final Sequence seq = new Sequence(Sequence.SMPTE_30, 1);
		final Track track = seq.createTrack();
		Mustil.setInstrument(track, channel, Mustil.PRG_MELODIC_TOM);
		for (int i = 0; i < 6; i++) {
			Mustil.addNote(track, i, 1, channel, 62 - i * 4, vol);
			Mustil.addNote(track, i + 1, 1, channel, 66 - i * 4, vol);
		}
		return seq;
	}
	
	private final static Sequence newFxThud() throws Exception {
		final Sequence seq = new Sequence(Sequence.SMPTE_30, 1);
		final Track track = seq.createTrack();
		Mustil.addPercussion(track, 0, Mustil.PRC_HIGH_BONGO);
		return seq;
	}
	
	private final static Sequence newFxJump() throws Exception {
		final int channel = 0, vol = 32;
		final Sequence seq = new Sequence(Sequence.SMPTE_30, 1);
		final Track track = seq.createTrack();
		Mustil.setInstrument(track, channel, Mustil.PRG_SLAP_BASS_2);
		Mustil.addNote(track, 0, 8, channel, 66, vol);
		Mustil.setPitch(track, 2, channel, 80);
		Mustil.setPitch(track, 4, channel, 96);
		Mustil.setPitch(track, 6, channel, 80);
		return seq;
	}
	
	private final static void run(final String[] args) throws Exception {
		if ("load".equalsIgnoreCase(Coltil.get(args, 0))) {
			runLoad();
		} else {
			runGen();
		}
	}
	
	private final static void runGen() throws Exception {
		System.out.println("Starting");
		final Song song = newSongHeartbeat(); //newSongHappy4();
		Mustil.save(song.seq, song.name.toLowerCase() + ".mid");
		final Panaudio music = Pangine.getEngine().getAudio();
		//music.ensureCapacity(4);
		//music.playMusic(seq);
		new org.pandcorps.pandam.lwjgl.JavaxMidiPansound(song.seq).startMusic();
		System.out.println("Started; press enter to play sound; press x and enter to stop");
		while (!Iotil.readln().equals("x")) {
			//music.playSound(jump);
		}
		music.close();
		System.out.println("End");
	}
	
	private final static void runLoad() throws Exception {
		Pangine.getEngine().getAudio().createMusic("org/pandcorps/platform/res/music/happy.mid").startMusic();
	}
	
	public final static void main(final String[] args) {
		try {
			run(args);
		} catch (final Throwable e) {
			e.printStackTrace();
		}
	}
}
