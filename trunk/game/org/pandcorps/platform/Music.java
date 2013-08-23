/*
Copyright (c) 2009-2011, Andrew M. Martin
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
	
	static {
		try {
			gem = newFxGem();
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
	
	protected final static Sequence newFxGem() throws Exception {
		final int channel = 0, vol = 64;
		final Sequence seq = new Sequence(Sequence.SMPTE_30, 1);
		final Track track = seq.createTrack();
		Mustil.setInstrument(track, channel, Mustil.PRG_TINKLE_BELL);
		final int d = 2;
		for (int i = 0; i < 4; i++) {
			Mustil.addNote(track, i * d, d, channel, 72 + i * 4, vol);
		}
		Mustil.addNote(track, 8, 4, channel, 88, vol);
		//Mustil.setVolume(track, 10, channel, 32);
		//Mustil.setVolume(track, 12, channel, 127);
		return seq;
	}
	
	private final static void run() throws Exception {
		System.out.println("Starting");
		final Sequence seq = newSongCreepy();
		final Pansic music = Pangine.getEngine().getMusic();
		music.ensureCapacity(4);
		music.loop(seq);
		System.out.println("Started; press enter to play sound; press x and enter to stop");
		while (!Iotil.readln().equals("x")) {
			music.play(gem);
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
