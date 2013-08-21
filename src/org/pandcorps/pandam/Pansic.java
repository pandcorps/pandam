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
package org.pandcorps.pandam;

import java.util.*;

import javax.sound.midi.*;

import org.pandcorps.core.Pantil;

// Pandam Music
public final class Pansic {
	private final static List<Sequencer> sequencers = new ArrayList<Sequencer>();
	
	protected Pansic() {
	}
	
	private Sequencer newSequencer() {
		final Sequencer sequencer;
		try {
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
		} catch (final Exception e) {
			throw Pantil.toRuntimeException(e);
		}
		sequencers.add(sequencer);
		return sequencer;
	}
	
	public final void ensureCapacity(final int capacity) {
		while (sequencers.size() < capacity) {
			newSequencer();
		}
	}
	
	// For music
	public final void loop(final Sequence seq) {
		play(seq, Sequencer.LOOP_CONTINUOUSLY);
	}
	
	// For sound effects
	public final void play(final Sequence seq) {
		play(seq, 0);
	}
	
	public final void play(final Sequence seq, final int loopCount) {
		Sequencer sequencer = null;
		for (final Sequencer s : sequencers) {
			if (!s.isRunning()) {
				sequencer = s;
				break;
			}
		}
		if (sequencer == null) {
			// Could set absolute limit and ignore requests after that
			sequencer = newSequencer();
		}
		sequencer.setTickPosition(0);
		sequencer.setLoopCount(loopCount);
		try {
			sequencer.setSequence(seq);
		} catch (final Exception e) {
			throw Pantil.toRuntimeException(e);
		}
		sequencer.start();
	}
	
	public final void stop() {
		for (final Sequencer sequencer : sequencers) {
			sequencer.stop();
		}
	}
		
	public final void close() {
		stop();
		for (final Sequencer sequencer : sequencers) {
			sequencer.close();
		}
		sequencers.clear();
	}
}
