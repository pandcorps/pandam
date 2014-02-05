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
package org.pandcorps.pandam;

import java.util.*;

import javax.sound.midi.*;

import org.pandcorps.core.*;

// Pandam Music
public final class Pansic {
    private final static String PROP_MUSIC_ENABLED = "org.pandcorps.pandam.musicEnabled";
    private final static String PROP_SOUND_ENABLED = "org.pandcorps.pandam.soundEnabled";
    
	private final List<Sequencer> sequencers = new ArrayList<Sequencer>();
	private boolean musicEnabled = Pantil.isProperty(PROP_MUSIC_ENABLED, true);
	private boolean soundEnabled = Pantil.isProperty(PROP_SOUND_ENABLED, true);
	
	protected Pansic() {
	}
	
	public final void setMusicEnabled(final boolean musicEnabled) {
	    this.musicEnabled = musicEnabled;
	    setEnabled(true, musicEnabled);
	}
	
	public final void setSoundEnabled(final boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
        setEnabled(false, soundEnabled);
    }
	
	private final void setEnabled(final boolean music, final boolean enabled) {
	    if (enabled) {
	        return;
	    }
	    for (final Sequencer sequencer : sequencers) {
	        if (sequencer.isRunning() && (sequencer.getLoopCount() == Sequencer.LOOP_CONTINUOUSLY) == music) {
	            sequencer.stop();
	        }
	    }
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
	
	public final void playMusic(final Sequence seq) {
	    if (musicEnabled) {
	        play(seq, Sequencer.LOOP_CONTINUOUSLY);
	    }
	}
	
	public final void playSound(final Sequence seq) {
	    if (soundEnabled && seq != null) {
	        play(seq, 0);
	    }
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
