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
import android.media.SoundPool.*;

public class SoundPoolPansound extends Pansound {
	protected static SoundPool soundPool = null;
	private FileInputStream fin = null;
	private int sampleId = -1;
	
	protected SoundPoolPansound(final String loc) {
		if (soundPool == null) {
    		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
    	}
    	try {
    		final CopyResult cr = AndroidPangine.copyResourceToFile(loc);
    		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
				@Override
				public final void onLoadComplete(final SoundPool soundPool, final int sampleId, final int status) {
					Iotil.close(fin);
					fin = null;
					SoundPoolPansound.this.sampleId = sampleId;
				}});
    		fin = cr.openInputStream();
    		soundPool.load(fin.getFD(), 0, cr.size, 1);
    		//soundPool.load(context.getFilesDir().getAbsolutePath() + "/" + tmpFileName, 1);
    	} catch (final Exception e) {
    		throw Panception.get(e);
    	}
	}
	
	@Override
	protected final void runMusic() throws Exception {
		run(-1);
	}

	@Override
	protected final void runSound() throws Exception {
		run(0);
	}
	
	private final void run(final int repeatCount) {
		if (sampleId == -1) {
			System.out.println("Tried to play sound before it was loaded");
			return;
		}
		System.out.println("Load complete, trying to play");
		//streamId = soundPool.play(sampleId, 1, 1, 1, -1, 1);
		final int streamId = soundPool.play(sampleId, 1, 1, 1, 0, 1);
		if (repeatCount != 0) {
			System.out.println("Started playing, changing loop");
			soundPool.setLoop(streamId, repeatCount);
		}
	}
}
