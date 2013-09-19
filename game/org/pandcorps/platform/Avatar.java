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
package org.pandcorps.platform;

import org.pandcorps.core.Mathtil;
import org.pandcorps.core.seg.*;
import org.pandcorps.platform.Player.PlayerData;

public class Avatar extends PlayerData implements Segmented {
    protected String anm = null;
    protected int eye = -1;
    protected float r = -1; // These should probably be multiples of 0.25
    protected float g = -1;
    protected float b = -1;
    
    public Avatar() {
    }
    
    public Avatar(final Avatar src) {
        load(src);
    }
    
    public void randomize() {
        anm = Mathtil.rand(PlatformGame.getAnimals());
        eye = Mathtil.randi(1, PlatformGame.getNumEyes());
        do {
            r = randColor();
            g = randColor();
            b = randColor();
        } while (r == 0 && g == 0 && b == 0);
    }
    
    public void load(final Avatar src) {
        setName(src.getName());
        anm = src.anm;
        eye = src.eye;
        r = src.r;
        g = src.g;
        b = src.b;
    }
    
    public void load(final Segment seg) {
    	setName(seg.getValue(0));
    	anm = seg.getValue(1);
    	eye = seg.intValue(2);
    	r = seg.floatValue(3);
    	g = seg.floatValue(4);
    	b = seg.floatValue(5);
    }
    
    @Override
    public void save(final Segment seg) {
        seg.setName(PlatformGame.SEG_AVT);
    	seg.setValue(0, getName());
    	seg.setValue(1, anm);
    	seg.setInt(2, eye);
    	seg.setFloat(3, r);
    	seg.setFloat(4, g);
    	seg.setFloat(5, b);
    }
    
    private final static float randColor() {
        return toColor(Mathtil.randi(0, 4));
    }
    
    protected final static float toColor(final int i) {
        return i / 4f;
    }
}
