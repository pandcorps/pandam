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

import org.pandcorps.core.*;

public abstract class Panaudio {
	private final static String PROP_MUSIC_ENABLED = "org.pandcorps.pandam.musicEnabled";
    private final static String PROP_SOUND_ENABLED = "org.pandcorps.pandam.soundEnabled";
    
    private boolean musicEnabled = Pantil.isProperty(PROP_MUSIC_ENABLED, true);
	private boolean soundEnabled = Pantil.isProperty(PROP_SOUND_ENABLED, true);
	
	public abstract Pansound createSound(final String location);
	
	public abstract Pansound createMusic(final String location);
	
	public abstract Pansound createTransition(final String location);
	
	public final boolean isMusicEnabled() {
		return musicEnabled;
	}
	
	public final void setMusicEnabled(final boolean musicEnabled) {
		if (this.musicEnabled != musicEnabled) {
		    this.musicEnabled = musicEnabled;
		    setEnabled(true, musicEnabled);
		    if (musicEnabled && Pansound.currentMusic != null) {
		    	Pansound.currentMusic.startMusic();
		    }
		}
	}
	
	public final boolean isSoundEnabled() {
		return soundEnabled;
	}
	
	public final void setSoundEnabled(final boolean soundEnabled) {
		if (this.soundEnabled != soundEnabled) {
	        this.soundEnabled = soundEnabled;
	        setEnabled(false, soundEnabled);
		}
    }
	
	protected abstract void setEnabled(final boolean music, final boolean enabled);
	
	public final Pansound getMusic() {
		return Pansound.currentMusic;
	}
	
	public final void stopMusic() {
		setEnabled(true, false);
		Pansound.currentMusic = null;
	}
	
	public abstract void stop();
		
	public abstract void close();
}
