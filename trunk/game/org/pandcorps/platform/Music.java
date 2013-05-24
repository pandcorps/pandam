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

import org.pandcorps.core.Iotil;

public class Music {
	private static Sequencer sequencer = null;
	
	protected final static Sequence createSequence() throws Exception {
		// key/vel 0 - 127
		final int channel = 0, key = 64, vel = 64;
		final Sequence seq = new Sequence(Sequence.SMPTE_30, 1);
		final Track track = seq.createTrack();
		addNote(track, 0, channel, key, vel);
		addNote(track, 15, channel, 68, vel);
		addNote(track, 30, channel, 72, vel);
		return seq;
	}
	
	protected final static void start(final Sequence seq) throws Exception {
		sequencer = MidiSystem.getSequencer();
		sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
		sequencer.setSequence(seq);
		sequencer.open();
		sequencer.start();
	}
		
	protected final static void end() {
		if (sequencer != null) {
			sequencer.close();
			sequencer = null;
		}
	}
	
	private static void addNote(final Track track, final long tick, final int channel, final int key, final int vel) throws Exception {
		final ShortMessage onMessage = new ShortMessage();
		final ShortMessage offMessage = new ShortMessage();
		onMessage.setMessage(ShortMessage.NOTE_ON, channel, key, vel);
		offMessage.setMessage(ShortMessage.NOTE_OFF, channel, key, 0);
		track.add(new MidiEvent(onMessage, tick));
		track.add(new MidiEvent(offMessage, tick + 30));
	}
	
	private final static void run() throws Exception {
		System.out.println("Starting");
		final Sequence seq = createSequence();
		start(seq);
		System.out.println("Started; press enter to stop");
		Iotil.readln();
		end();
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
