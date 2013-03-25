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
package org.pandcorps.game;

import java.awt.image.BufferedImage;

import org.pandcorps.core.Reftil;
import org.pandcorps.core.img.scale.Scaler;
import org.pandcorps.game.core.ImtilX;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.FinPanple;

public abstract class BaseGame extends Pangame {
	public final static int SCREEN_W = 256;
	public final static int SCREEN_H = 192;
	protected final static FinPanple CENTER_16 = new FinPanple(8, 8, 0);
	protected final static FinPanple CENTER_8 = new FinPanple(4, 4, 0);
	protected final static FinPanple CENTER_4 = new FinPanple(2, 2, 0);
	
	@Override
    public void initBeforeEngine() {
        final Pangine engine = Pangine.getEngine();
        final String scalerClassName = System.getProperty("org.pandcorps.game.Scaler.impl");
        if (scalerClassName != null) {
        	engine.setImageScaler((Scaler) Reftil.newInstance(scalerClassName));
        }
        engine.setMaxZoomedDisplaySize(SCREEN_W, SCREEN_H);
    }
	
	@Override
    protected final FinPanple getFirstRoomSize() {
        return new FinPanple(SCREEN_W, SCREEN_H, 0);
    }
	
	protected final static Panmage[] createSheet(final String name, final String path) {
	    return createSheet(name, path, ImtilX.DIM);
	}
	
	protected final static Panmage[] createSheet(final String name, final String path, final int dim) {
	    final Pangine engine = Pangine.getEngine();
	    final BufferedImage[] b = ImtilX.loadStrip(path, dim);
	    final int size = b.length;
	    final Panmage[] p = new Panmage[size];
	    for (int i = 0; i < size; i++) {
	        p[i] = engine.createImage("img." + name + "." + i, b[i]);
	    }
	    return p;
	}
	
	protected final static Panimation createAnm(final String name, final String path, final int dur) {
	    return createAnm(name, path, ImtilX.DIM, dur);
	}
	
	protected final static Panimation createAnm(final String name, final String path, final int dim, final int dur) {
	    final Pangine engine = Pangine.getEngine();
	    final Panmage[] ia = createSheet(name, path, dim);
	    final int size = ia.length;
	    final Panframe[] fa = new Panframe[size];
	    for (int i = 0; i < size; i++) {
	        final Panmage img = ia[i];
	        fa[i] = engine.createFrame("frm." + name + "." + i, img, dur);
	    }
	    return engine.createAnimation("anm." + name, fa);
	}
}
