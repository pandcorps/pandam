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
import org.pandcorps.core.Mustil.*;
import org.pandcorps.pandam.*;

public class SlamMusic {
    private final static String COPYRIGHT = "Copyright (c) " + ChampionsOfSlamGame.YEAR + ", " + ChampionsOfSlamGame.AUTHOR;
	
	private static int channel = 0, vol, deltaTick;
	private static long tick = 0;
	
	protected final static Song newSongSlam() throws Exception {
		final Song song = newSong("Champions of Slam");
		final Track track = song.track;
		int dur, keys[];
		long start = -1;
		final int r = 8;
		
		tick = 0;
		for (int j = 0; j < 5; j++) {
		for (int i = 0; i < 4; i++) {
    		channel = 0;
    		vol = 56;
    		Mustil.setInstrument(track, channel, Mustil.PRG_ELECTRIC_BASS_FINGER);
    		dur = 4;
    		Mustil.unspecifiedNoteDuration = 4;
    		if (i == 1) {
    		    keys = new int[] {38, 36};
    		} else if (i == 3) {
    		    keys = new int[] {43, 45};
    		} else {
    		    keys = new int[] {40, 41};
    		}
    		tick = Mustil.addRepeatedNotes(track, tick, channel, vol, dur, r, keys);
		}
		if (start < 0) start = tick;
		}
		tick = 0;
		
		channel = 1;
        vol = 64;
        Mustil.setInstrument(track, channel, Mustil.PRG_ELECTRIC_GUITAR_MUTED);
        Mustil.addNote(track, start, 64, channel, 55, vol);
        Mustil.addNote(track, start + 64, 64, channel, 53, vol);
        Mustil.addNote(track, start + 128, 64, channel, 55, vol);
        Mustil.addNote(track, start + 192, 64, channel, 57, vol);
        start += 256;
        Mustil.addNote(track, start, 64, channel, 59, vol);
        Mustil.addNote(track, start + 64, 64, channel, 57, vol);
        Mustil.addNote(track, start + 128, 64, channel, 59, vol);
        Mustil.addNote(track, start + 192, 64, channel, 60, vol);
        start += 256;
        Mustil.addNote(track, start, 64, channel, 62, vol);
        Mustil.addNote(track, start + 64, 64, channel, 60, vol);
        Mustil.addNote(track, start + 128, 64, channel, 59, vol);
        Mustil.addNote(track, start + 192, 64, channel, 57, vol);
        start += 256;
        Mustil.addNote(track, start, 64, channel, 59, vol);
        Mustil.addNote(track, start + 64, 64, channel, 57, vol);
        Mustil.addNote(track, start + 128, 64, channel, 55, vol);
        Mustil.addNote(track, start + 192, 64, channel, 53, vol);
		
		/*
		channel = 1;
		vol = 64;
		Mustil.setInstrument(track, channel, Mustil.PRG_ELECTRIC_GUITAR_MUTED);
		tick = 64;
		Mustil.unspecifiedNoteDuration = dur = 16;
		final int h = 53, h1 = 55, h2 = 57, h3 = 59;
		tick = Mustil.addNotes(track, tick, channel, vol, dur, h, -1, h1, -1, h2, -1, h3, -1, h2);
		Mustil.addNote(track, tick + 8, 8, channel, h1, vol);
		Mustil.addNote(track, tick + 16, dur, channel, h, vol);
		
		//final long end = 320 + tick - 40;
		tick = 320;
		Mustil.unspecifiedNoteDuration = dur = 4;
		tick = Mustil.addNotes(track, tick, channel, vol, dur, h2, h2, h1, h1, h, -1, -1, -1, h2, h2, h1, h1, h);
		tick += 4;
		Mustil.addNote(track, tick + 8, 8, channel, h2, vol);
		Mustil.addNote(track, tick + 16, 8, channel, h1, vol);
        Mustil.addNote(track, tick + 24, dur, channel, h2, vol);
        */
		
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
