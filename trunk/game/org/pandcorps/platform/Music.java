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
		tick = addMainHappy(track);
		key = 48;
		tick = Mustil.addNotes(track, tick, channel, vol, 8,
				key, key, key, key - 4, key + 4, key + 4, key + 8);
		tick += 8;
		tick = Mustil.addNotes(track, tick, channel, vol, 8,
				key, key, key + 8, key + 16, key + 24);
		tick += 24;
		key = 56;
		tick = Mustil.addNotes(track, tick, channel, vol, 4,
				key, -1, key, key, -1, key, key, -1, key + 4, -1, key + 4);
		tick += 20;
		tick = Mustil.addNotes(track, tick, channel, vol, 4,
				key, -1, key, key, -1, key, key, -1, key + 8);
		tick += 28;
		key = 48;
		tick = Mustil.addNotes(track, tick, channel, vol, 4,
				key, -1, key, key, -1, key, key, -1, key + 4, -1, key + 4);
		tick += 20;
		tick = Mustil.addNotes(track, tick, channel, vol, 4,
				key, -1, key + 8, key + 8, -1, key + 16, key + 16, -1, key + 24);
		tick += 28;
		/*key = 52;
		tick = Mustil.addNotes(track, tick, channel, vol, 4,
				key, key - 4, key, key + 4, key, key + 4, key + 8, key + 4, key + 8, key + 12, key + 8, key + 12,
				key + 16, key + 12, key + 8, key + 12, key + 8, key + 4, key + 8, key + 12, key + 8, key + 12,
				key + 8, key + 12, key + 16);
		tick += 24;*/
		addMainHappy(track);
		key = 60;
		tick = Mustil.addNotes(track, tick, channel, vol, 8,
				key, key, key, key - 4, key - 8, key - 4, key);
		tick += 8;
		tick = Mustil.addNotes(track, tick, channel, vol, 4,
				key, -1, key - 4, key - 4, key, -1, key, -1);
		Mustil.addNote(track, tick, 30, channel, key + 4, vol);
		tick += 30;
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
		key = 56;
		tick = Mustil.addNotes(track, tick, channel, vol, 8,
				key, key, key, key - 4, key + 4, key + 4, key + 8);
		tick += 8;
		tick = Mustil.addNotes(track, tick, channel, vol, 8,
				key, key, key + 4, key + 8, key + 12);
		tick += 24;
		return tick;
	}
	
	protected final static Sequence newSongTechno() throws Exception {
		channel = 0;
		final Sequence seq = new Sequence(Sequence.SMPTE_30, 1);
		final Track track = seq.createTrack();
		Mustil.setInstrument(track, channel, Mustil.PRG_FRETLESS_BASS);
		final int length = 512; //4096;
		int dur = 2;
		Mustil.unspecifiedNoteDuration = dur;
		int[] keys = {36, 44};
		Mustil.addRepeatedNotes(track, 0, channel, 48, dur, length / keys.length / dur, keys);
		channel = 1;
		Mustil.setInstrument(track, channel, Mustil.PRG_HALO);
		dur = 32;
		Mustil.unspecifiedNoteDuration = dur;
		keys = new int[] {-1, -1, 56, -1};
		Mustil.addRepeatedNotes(track, 0, channel, 56, dur, length / keys.length / dur, keys);
		channel = 2;
		Mustil.setInstrument(track, channel, Mustil.PRG_WARM);
		dur = 8;
		Mustil.unspecifiedNoteDuration = 4;
		keys = new int[] {-1, -1, -1, -1, 63, 65, -1, -1};
		Mustil.addRepeatedNotes(track, 0, channel, 58, dur, length / keys.length / dur, keys);
		channel = 3;
		vol = 64;
		tick = 0; //128;
		Mustil.setInstrument(track, channel, Mustil.PRG_CRYSTAL);
		dur = 8;
		Mustil.unspecifiedNoteDuration = dur;
		key = 56;
		tick = Mustil.addNotes(track, tick, channel, vol, dur, key, key, key + 4, key, key + 8, key + 8, key + 4);
		tick += dur;
		tick = Mustil.addNotes(track, tick, channel, vol, dur, key, key, key + 4, key + 4, key, -1, key);
		tick += dur;
		tick = Mustil.addNotes(track, tick, channel, vol, dur, key, key, key + 4, key, key + 8, key + 8, key + 4);
		tick += dur;
		tick = Mustil.addNotes(track, tick, channel, vol, dur, key, key, key + 4, key + 4, key, -1, key);
		tick += dur;
		return seq;
	}
	
	private final static Sequence newFxGem(final int mag) throws Exception {
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
	
	private final static void run() throws Exception {
		System.out.println("Starting");
		final Sequence seq = newSongTechno();
		final Pansic music = Pangine.getEngine().getMusic();
		music.ensureCapacity(4);
		music.playMusic(seq);
		System.out.println("Started; press enter to play sound; press x and enter to stop");
		while (!Iotil.readln().equals("x")) {
			music.playSound(jump);
		}
		music.close();
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
