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
import java.util.ArrayList;

import org.pandcorps.core.*;
import org.pandcorps.core.img.scale.Scaler;
import org.pandcorps.game.core.ImtilX;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.FinPanple;

public abstract class BaseGame extends Pangame {
    /*
    Sample monitor - 1920 x 1200
    1080p - 1920 x 1080
    720p - 1280 x 720
    1080p / 2 - 960 x 540
    720p / 2 - 640 x 360
    Sample monitor / 4 - 480 x 300
    1080p / 4 - 480 x 270
    16-bit (and effective 8-bit) - 256 x 224
    Portable - 256 x 192
    Sample phone - 192 x 192
    */
	public static int SCREEN_W = 256;
	public static int SCREEN_H = 192;
	protected final static FinPanple CENTER_16 = new FinPanple(8, 8, 0);
	protected final static FinPanple CENTER_8 = new FinPanple(4, 4, 0);
	protected final static FinPanple CENTER_4 = new FinPanple(2, 2, 0);
	public final static String PRE_IMG = "img.";
	public final static String PRE_FRM = "frm.";
	public final static String PRE_ANM = "anm.";
	
	protected boolean isFullScreen() {
	    return false;
	}
	
	@Override
    public void initBeforeEngine() {
        final Pangine engine = Pangine.getEngine();
        final String scalerClassName = Pantil.getProperty("org.pandcorps.game.scalerImpl");
        if (scalerClassName != null) {
        	engine.setImageScaler((Scaler) Reftil.newInstance(scalerClassName));
        }
        if (isFullScreen()) {
            engine.setApproximateFullScreenZoomedDisplaySize(SCREEN_W, SCREEN_H);
            final float zoom = engine.getZoom();
            SCREEN_W = (int) (engine.getDesktopWidth() / zoom);
            SCREEN_H = (int) (engine.getDesktopHeight() / zoom);
        } else {
            engine.setMaxZoomedDisplaySize(SCREEN_W, SCREEN_H);
        }
    }
	
	@Override
    protected final FinPanple getFirstRoomSize() {
        return new FinPanple(SCREEN_W, SCREEN_H, 0);
    }
	
	public final static Panmage createImage(final String name, final String path, final int dim) {
		return createImage(name, path, dim, null);
	}
	
	public final static Panmage createImage(final String name, final String path, final int dim, final Panple o) {
	    return createImage(name, path, dim, o, null, null);
	}
	
	public final static Panmage createImage(final String name, final String path, final int dim, final Panple o, final Panple n, final Panple x) {
		final Pangine engine = Pangine.getEngine();
		final String in = PRE_IMG + name;
		final Panmage img = engine.getImage(in);
		return img == null ? engine.createImage(in, o, n, x, ImtilX.loadImage(path, dim, null)) : img;
	}
	
	public final static Panmage[] createSheet(final String name, final String path) {
	    return createSheet(name, path, ImtilX.DIM);
	}
	
	public final static Panmage[] createSheet(final String name, final String path, final int dim) {
	    return createSheet(name, path, dim, null);
	}
	
	public final static Panmage[] createSheet(final String name, final String path, final int dim, final Panple o) {
		return createSheet(name, path, dim, o, null, null);
	}
	
	public final static Panmage[] createSheet(final String name, final String path, final int dim, final Panple o, final Panple n, final Panple x) {
	    final Pangine engine = Pangine.getEngine();
	    Panmage t;
	    ArrayList<Panmage> list = null;
	    for (int i = 0; (t = engine.getImage(PRE_IMG + name + "." + i)) != null; i++) {
	    	if (list == null) {
	    		list = new ArrayList<Panmage>();
	    	}
	    	list.add(t);
	    }
	    if (list != null) {
	    	return list.toArray(new Panmage[list.size()]);
	    }
	    return createSheet(name, o, n, x, ImtilX.loadStrip(path, dim));
	}
	
	public final static Panmage[] createSheet(final String name, final Panple o, final BufferedImage... b) {
		return createSheet(name, o, null, null, b);
	}
	
	public final static Panmage[] createSheet(final String name, final Panple o, final Panple n, final Panple x, final BufferedImage... b) {
		final Pangine engine = Pangine.getEngine();
	    final int size = b.length;
	    final Panmage[] p = new Panmage[size];
	    for (int i = 0; i < size; i++) {
	        p[i] = engine.createImage(PRE_IMG + name + "." + i, o, n, x, b[i]);
	    }
	    return p;
	}
	
	public final static Panimation createAnm(final String name, final String path, final int dur) {
	    return createAnm(name, path, ImtilX.DIM, dur);
	}
	
	public final static Panframe[] createFrames(final String name, final String path, final int dim, final int dur) {
	    return createFrames(name, dur, createSheet(name, path, dim));
	}
	
	public final static Panframe[] createFrames(final String name, final String path, final int dim, final int dur, final Panple o) {
		return createFrames(name, path, dim, dur, o, null, null);
	}
	
	public final static Panframe[] createFrames(final String name, final String path, final int dim, final int dur, final Panple o, final Panple n, final Panple x) {
	    return createFrames(name, dur, createSheet(name, path, dim, o, n, x));
	}
	
	public final static Panframe[] createFrames(final String name, final int dur, final Panmage... ia) {
		final Pangine engine = Pangine.getEngine();
	    final int size = ia.length;
	    final Panframe[] fa = new Panframe[size];
	    for (int i = 0; i < size; i++) {
	        final Panmage img = ia[i];
	        fa[i] = engine.createFrame(PRE_FRM + name + "." + i, img, dur);
	    }
	    return fa;
	}
	
	public final static Panimation createAnm(final String name, final String path, final int dim, final int dur) {
	    return Pangine.getEngine().createAnimation(PRE_ANM + name, createFrames(name, path, dim, dur));
	}
	
	public final static Panimation createAnm(final String name, final String path, final int dim, final int dur, final Panple o) {
		return createAnm(name, path, dim, dur, o, null, null);
	}
	
	public final static Panimation createAnm(final String name, final String path, final int dim, final int dur, final Panple o, final Panple n, final Panple x) {
	    return Pangine.getEngine().createAnimation(PRE_ANM + name, createFrames(name, path, dim, dur, o, n, x));
	}
	
	public final static Panimation createAnm(final String name, final int dur, final Panple o, final BufferedImage... a) {
		return Pangine.getEngine().createAnimation(PRE_ANM + name, createFrames(name, dur, createSheet(name, o, a)));
	}
	
	public final static Panimation createAnm(final String name, final int dur, final Panple o, final Panple n, final Panple x, final BufferedImage... a) {
		return Pangine.getEngine().createAnimation(PRE_ANM + name, createFrames(name, dur, createSheet(name, o, n, x, a)));
	}
	
	public final static Panlayer createHud(final Panroom room) {
		final Pangine engine = Pangine.getEngine();
		final Panlayer hud = engine.createLayer("layer.hud", engine.getGameWidth(), engine.getGameHeight(), 1, room);
		room.addAbove(hud);
		return hud;
	}
	
	public final static Panlayer createParallax(final Panlayer masterAbove, final int motionDivisor) {
	    return createParallax(masterAbove, masterAbove, motionDivisor);
	}
	
	public final static Panlayer createParallax(final Panlayer master, final Panlayer above, final int motionDivisor) {
	    final Pangine engine = Pangine.getEngine();
	    final int ew = engine.getEffectiveWidth(), eh = engine.getEffectiveHeight();
	    final Panple ms = master.getSize();
	    final float w = ew + ((ms.getX() - ew) / motionDivisor), h = eh + ((ms.getY() - eh) / motionDivisor);
	    final Panlayer bg = engine.createLayer(Pantil.vmid(), w, h, 1, master.getRoom());
        above.addBeneath(bg);
        bg.setMaster(master);
        return bg;
	}
}
