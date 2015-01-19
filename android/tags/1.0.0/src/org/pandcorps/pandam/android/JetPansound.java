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

import org.pandcorps.pandam.*;

import android.media.*;

public final class JetPansound extends Pansound {
	protected static JetPlayer jetPlayer = null;
	private final String fileName;
	
	protected JetPansound(String loc) {
    	try {
    		loc = loc.substring(0, loc.length() - 3) + "jet";
    		fileName = AndroidPangine.copyResourceToFile(loc).fileName;
    	} catch (final Exception e) {
    		throw Panception.get(e);
    	}
	}
	
	@Override
	protected final void runMusic() throws Exception {
		AndroidPanaudio.pausedMusic = null;
		run(-1);
	}

	@Override
	protected final void runSound() throws Exception {
		run(0);
	}
	
	private final void run(final int repeatCount) {
		if (jetPlayer == null) {
    		jetPlayer = JetPlayer.getJetPlayer();
    	}
		if (!jetPlayer.loadJetFile(fileName)) {
			throw new Panception("Failed to load Jet file " + fileName);
		}
		if (!jetPlayer.queueJetSegment(0, -1, repeatCount, 0, 0, (byte) 0)) {
			throw new Panception("Failed to queue Jet segment");
		}
		if (!jetPlayer.play()) {
			throw new Panception("Failed to play Jet file");
		}
	}
}
