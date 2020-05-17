/*
Copyright (c) 2009-2020, Andrew M. Martin
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
package org.pandcorps.pandam.impl;

import java.util.*;

import org.pandcorps.pandam.*;

public class MultiPanaudio extends Panaudio {
    private final Map<String, Panaudio> map = new HashMap<String, Panaudio>();
    
    public final void addMapping(final String extension, final Panaudio audio) {
        map.put(formatExtension(extension), audio);
    }
    
    protected Panaudio getNecessaryAudio(final String location) {
        final Panaudio audio = map.get(getExtension(location));
        if (audio == null) {
            throw new IllegalArgumentException("No audio support for " + location);
        }
        return audio;
    }
    
    private final String getExtension(final String location) {
        return formatExtension(location.substring(location.lastIndexOf('.') + 1));
    }
    
    private final String formatExtension(final String extension) {
        return extension.toLowerCase();
    }
    
    @Override
    public final Pansound createSound(final String location) {
        return getNecessaryAudio(location).createSound(location);
    }
    
    @Override
    public final Pansound createMusic(final String location) {
        return getNecessaryAudio(location).createMusic(location);
    }
    
    @Override
    public final Pansound createTransition(final String location) {
        return getNecessaryAudio(location).createTransition(location);
    }
    
    @Override
    protected final void setEnabled(final boolean music, final boolean enabled) {
        for (final Panaudio audio : map.values()) {
            setEnabled(audio, music, enabled);
        }
    }
    
    @Override
    public final void pauseMusic() {
        for (final Panaudio audio : map.values()) {
            audio.pauseMusic();
        }
    }
    
    @Override
    public final void resumeMusic() throws Exception {
        for (final Panaudio audio : map.values()) {
            audio.resumeMusic();
        }
    }
    
    @Override
    public final void stop() {
        for (final Panaudio audio : map.values()) {
            audio.stop();
        }
    }
    
    @Override
    public final void close() {
        for (final Panaudio audio : map.values()) {
            audio.close();
        }
    }
}
