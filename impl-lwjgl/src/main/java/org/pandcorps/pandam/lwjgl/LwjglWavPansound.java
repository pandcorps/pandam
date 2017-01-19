/*
Copyright (c) 2009-2017, Andrew M. Martin
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

import java.io.*;
import java.nio.*;

import org.lwjgl.*;
import org.lwjgl.openal.*;
import org.lwjgl.util.*;
import org.pandcorps.core.*;
import org.pandcorps.pandam.*;

public final class LwjglWavPansound extends Pansound {
    private static boolean initialized = false;
    private static LwjglWavPansound pausedSound = null;
    private final IntBuffer buffers;
    private final IntBuffer sources;
    
    public LwjglWavPansound(final String location) throws Exception {
        initGlobal();
        
        buffers = BufferUtils.createIntBuffer(1);
        AL10.alGenBuffers(buffers);
        final int buffer = buffers.get(0);
        BufferedInputStream in = null;
        final WaveData waveData; 
        try {
            in = Iotil.getBufferedInputStream(location); // WaveData.create returns null for raw FileInputStream
            waveData = WaveData.create(in);
        } finally {
            Iotil.close(in);
        }
        AL10.alBufferData(buffer, waveData.format, waveData.data, waveData.samplerate);
        waveData.dispose();
        
        sources = BufferUtils.createIntBuffer(1);
        AL10.alGenSources(sources);
        final int source = sources.get(0);
        AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
        AL10.alSourcef(source, AL10.AL_PITCH, 1.0f);
        AL10.alSourcef(source, AL10.AL_GAIN, 1.0f);
        final FloatBuffer zeroBuffer = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });
        zeroBuffer.rewind();
        AL10.alSource (source, AL10.AL_POSITION, zeroBuffer);
        AL10.alSource (source, AL10.AL_VELOCITY, zeroBuffer);
    }
    
    private final static void initGlobal() throws Exception {
        if (initialized) {
            return;
        }
        
        AL.create();
        
        final FloatBuffer zeroBuffer = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0f, 0.0f, 0.0f });
        final FloatBuffer orientation = BufferUtils.createFloatBuffer(6).put(new float[] { 0.0f, 0.0f, -1.0f,  0.0f, 1.0f, 0.0f });
        zeroBuffer.rewind();
        AL10.alListener(AL10.AL_POSITION, zeroBuffer);
        AL10.alListener(AL10.AL_VELOCITY, zeroBuffer);
        orientation.rewind();
        AL10.alListener(AL10.AL_ORIENTATION, orientation);
        
        initialized = true;
    }
    
    @Override
    protected final void runMusic() throws Exception {
        runSound(); //TODO Can we loop this?
    }

    @Override
    protected final void runSound() throws Exception {
        AL10.alSourcePlay(sources.get(0));
    }
    
    protected final void pause() {
        AL10.alSourcePause(sources.get(0));
    }
    
    protected final static void pauseMusic() {
        if (currentMusic instanceof LwjglWavPansound) {
            pausedSound = (LwjglWavPansound) currentMusic;
            pausedSound.pause();
        }
    }
    
    protected final static void resumeMusic() throws Exception {
        if (pausedSound == null) {
            return;
        }
        pausedSound.runMusic();
    }
    
    protected final boolean isMusic() {
        return this == currentMusic;
    }
    
    protected final void stop() {
        AL10.alSourceStop(sources.get(0));
    }
    
    @Override
    protected final void runDestroy() {
        AL10.alDeleteSources(sources);
        AL10.alDeleteBuffers(buffers);
        LwjglWavPanaudio.sounds.remove(this);
    }
    
    public final static void main(final String[] args) throws Exception {
        final LwjglWavPansound sound = new LwjglWavPansound(args[0]);
        do {
            sound.runSound();
            System.out.print("> ");
        } while (!Iotil.readln().equalsIgnoreCase("exit"));
        sound.runDestroy();
        AL.destroy();
    }
}
