/*
Copyright (c) 2009-2016, Andrew M. Martin
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
package org.pandcorps.pandam.lwjgl;

import java.util.*;

import org.lwjgl.openal.*;
import org.pandcorps.pandam.*;

public class LwjglWavPanaudio extends Panaudio {
    private final List<LwjglWavPansound> sounds = new ArrayList<LwjglWavPansound>();
    
    @Override
    public final Pansound createSound(final String location) {
        try {
            final LwjglWavPansound sound = new LwjglWavPansound(location);
            sounds.add(sound);
            return sound;
        } catch (final Exception e) {
            throw Panception.get(e);
        }
    }
    
    @Override
    public final Pansound createMusic(final String location) {
        return createSound(location);
    }
    
    @Override
    public final Pansound createTransition(final String location) {
        return createSound(location);
    }
    
    @Override
    protected final void setEnabled(final boolean music, final boolean enabled) {
        if (enabled) {
            return;
        }
        for (final LwjglWavPansound sound : sounds) {
            if (sound.isMusic() == music) {
                sound.stop();
            }
        }
    }
    
    @Override
    public final void pauseMusic() {
        LwjglWavPansound.pauseMusic();
    }
    
    @Override
    public final void resumeMusic() throws Exception {
        if (!isMusicEnabled()) {
            return;
        }
        LwjglWavPansound.resumeMusic();
    }
    
    @Override
    public final void stop() {
        for (final LwjglWavPansound sound : sounds) {
            sound.stop();
        }
    }
    
    @Override
    public final void close() {
        stop();
        for (final LwjglWavPansound sound : sounds) {
            sound.destroy();
        }
        sounds.clear();
        AL.destroy();
    }
}
