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
package org.pandcorps.pandam.android;

import java.io.*;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.android.AndroidPangine.*;

import android.media.*;

public class MediaPlayerPansound extends Pansound {
	private final MediaPlayer mediaPlayer = new MediaPlayer();
	
	protected MediaPlayerPansound(final String loc) {
		FileInputStream in = null;
    	try {
    		final CopyResult cr = AndroidPangine.copyResourceToFile(loc);
    		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    		//final String uri = Iotil.class.getClassLoader().getResource(loc).toURI().toString();
    		//mediaPlayer.setDataSource(context, Uri.parse(uri));
    		in = cr.openInputStream();
    		mediaPlayer.setDataSource(in.getFD());
    		in.close();
    		mediaPlayer.prepare(); // prepareAsync()
    	} catch (final Exception e) {
    		throw Panception.get(e);
    	} finally {
    		Iotil.close(in);
    	}
	}
	
	@Override
	protected final void runMusic() throws Exception {
		run(true);
	}

	@Override
	protected final void runSound() throws Exception {
		run(false);
	}
	
	private final void run(final boolean looping) {
		mediaPlayer.setLooping(looping); // Has gap
		//mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
		//	@Override public final void onCompletion(final MediaPlayer mp) {
		//		mediaPlayer.start();
		//	}}); // Still has gap
		mediaPlayer.seekTo(0);
		mediaPlayer.start();
	}
}
