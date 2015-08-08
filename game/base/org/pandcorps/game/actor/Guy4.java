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
package org.pandcorps.game.actor;

import org.pandcorps.pandam.*;
import org.pandcorps.pandax.tile.*;

public class Guy4 extends TileWalker {
    protected final static Direction[] directions;
    protected final static int[] weights = {1, 1, 1, 1, 150};
    
	protected Panmage[] stills = new Panmage[4];
	protected Panimation[] walks = new Panimation[4];
	
	static {
        final Direction[] d = Direction.values();
        final int size = d.length;
        directions = new Direction[size + 1];
        System.arraycopy(d, 0, directions, 0, size);
        directions[size] = null;
    }
	
	protected Guy4() {
	    setSpeed(2);
	}
    
    protected Guy4(final String id) {
        super(id);
        setSpeed(2);
    }
    
    protected final void setView(final Panmage[] strip) {
    	final Panmage[][] sheet = new Panmage[2][];
    	for (int i = 0; i < 2; i++) {
    		final Panmage[] a = new Panmage[4];
    		sheet[i] = a;
    		final int o = i * 4;
    		for (int j = 0; j < 4; j++) {
    			a[j] = strip[o + j];
    		}
    	}
    	setView(sheet);
    }
    
    protected final void setView(final Panmage[][] sheet) {
        final Pangine engine = Pangine.getEngine();
        final String id = getId() + "-";
        for (int i = 0; i < 4; i++) {
            final Panmage still = sheet[0][i];
            stills[i] = still;
            walks[i] = engine.createAnimation(
                id + "Animation-" + i,
                engine.createFrame(id + "Frame-" + i + "-" + 0, still, 4),
                engine.createFrame(id + "Frame-" + i + "-" + 1, sheet[1][i], 4));
        }
        face(Direction.South);
    }
    
    protected final void setView(final Panimation[] walks) {
        this.walks = walks;
        for (int i = 0; i < 4; i++) {
            stills[i] = walks[i].getFrames()[0].getImage();
        }
        face(Direction.South);
    }
    
    @Override
    protected final void onFace(final Direction oldDir, final Direction newDir) {
        setImage(newDir);
    }
    
    @Override
    protected void onWalk() {
        setView(walks[getDirection().ordinal()]);
    }
    
    @Override
    protected void onWalked() {
        setImage(getDirection());
        onStop();
    }
    
    private final void setImage(final Direction dir) {
        setView(stills[dir.ordinal()]);
    }
    
    protected final boolean go(final Direction dir) {
    	if (walk(dir)) {
    	    return true;
    	}
    	onBump();
    	return false;
    }
    
    //@OverrideMe
    protected void onStop() {
    }
    
    //@OverrideMe
    protected void onBump() {
    }
}
