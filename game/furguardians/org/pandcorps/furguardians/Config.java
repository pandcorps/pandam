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
package org.pandcorps.furguardians;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;

public class Config {
    protected final static int MIN_BUTTON_SIZE = -2;
    protected final static int MAX_BUTTON_SIZE = 2;
    protected final static boolean DEF_MUSIC_ENABLED = true;
    protected final static boolean DEF_SOUND_ENABLED = true;
    protected static String defaultProfileName = null;
    protected static int btnSize = 0;
    protected static int zoomMag = -1;
    protected static boolean musicEnabled = DEF_MUSIC_ENABLED;
    protected static boolean soundEnabled = DEF_SOUND_ENABLED;
    
    protected final static void serialize() {
        Iotil.writeFile(FurGuardiansGame.FILE_CFG, FurGuardiansGame.SEG_CFG
        		+ "|" + Chartil.unnull(defaultProfileName)
        		+ "|" + btnSize
        		+ "|" + zoomMag
        		+ "|" + musicEnabled
        		+ "|" + soundEnabled);
    }
    
    protected final static void setMusicEnabled(final boolean musicEnabled) {
    	Config.musicEnabled = musicEnabled;
    	Pangine.getEngine().getAudio().setMusicEnabled(musicEnabled);
    }
    
    protected final static void setSoundEnabled(final boolean soundEnabled) {
    	Config.soundEnabled = soundEnabled;
    	Pangine.getEngine().getAudio().setSoundEnabled(soundEnabled);
    }
}
