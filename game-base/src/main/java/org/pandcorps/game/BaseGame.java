/*
Copyright (c) 2009-2023, Andrew M. Martin
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

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.core.img.process.*;
import org.pandcorps.core.img.scale.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.Pandy.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;

public abstract class BaseGame extends Pangame {
    /*
    Sample monitor - 1920 x 1200 (8 x 5)
    1080p - 1920 x 1080 (16 x 9)
    720p - 1280 x 720 (16 x 9)
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
	public final static FinPanple2 CENTER_32 = new FinPanple2(16, 16);
	public final static FinPanple2 CENTER_16 = new FinPanple2(8, 8);
	public final static FinPanple2 CENTER_8 = new FinPanple2(4, 4);
	public final static FinPanple2 CENTER_4 = new FinPanple2(2, 2);
	public final static String PRE_IMG = "img.";
	public final static String PRE_FRM = "frm.";
	public final static String PRE_ANM = "anm.";
	protected static int zoomMag = -1;
	
	protected boolean isFullScreen() {
	    return false;
	}
	
	//@OverrideMe
	protected int getGameWidth() {
		return 0;
	}
	
	//@OverrideMe
	protected int getGameHeight() {
		return 0;
	}
	
	//@OverrideMe
	protected void initEarliest() {
	}
	
	@Override
    public void initBeforeEngine() {
	    initEarliest();
        final Pangine engine = Pangine.getEngine();
        final String scalerClassName = Pantil.getProperty("org.pandcorps.game.scalerImpl");
        if (scalerClassName != null) {
        	engine.setImageScaler((Scaler) Reftil.newInstance(scalerClassName));
        }
        final int gameWidth = getGameWidth();
        if (gameWidth > 0) {
            final int gameHeight = getGameHeight();
            if (isFullScreen()) {
                engine.setFullScreenEffectiveSize(gameWidth, gameHeight);
            } else {
                engine.setApproximateZoomedDisplaySize(gameWidth, gameHeight, SCREEN_W, SCREEN_H, false);
            }
        	SCREEN_W = engine.getEffectiveWidth();
        	SCREEN_H = engine.getEffectiveHeight();
        } else if (isFullScreen()) {
            if (zoomMag <= 0) {
                engine.setApproximateFullScreenZoomedDisplaySize(SCREEN_W, SCREEN_H, false);
            } else {
                engine.setFullScreenZoomed(zoomMag);
            }
            final float zoom = engine.getZoom();
            SCREEN_W = (int) (engine.getDesktopWidth() / zoom);
            SCREEN_H = (int) (engine.getDesktopHeight() / zoom);
        } else {
            engine.setMaxZoomedDisplaySize(SCREEN_W, SCREEN_H);
        }
        Panscreen.saveCurrentZoomAsDefault();
        Locale.setDefault(Locale.US); // toUpperCase can lead to characters outside of image fonts in other Locales
    }
	
	public final static int getApproximateFullScreenZoomedDisplaySize() {
		return Pangine.getEngine().getApproximateFullScreenZoomedDisplaySize(SCREEN_W, SCREEN_H, false);
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
	
	public final static Panmage[] createSheet(final String name, final Panple o, final Img... b) {
		return createSheet(name, o, null, null, b);
	}
	
	public final static Panmage[] createSheet(final String name, final Panple o, final Panple n, final Panple x, final Img... b) {
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
	    return createFrames(name, dur, Arrays.asList(ia));
	}
	
	public final static Panframe[] createFrames(final String name, final int dur, final List<Panmage> ia) {
		final Pangine engine = Pangine.getEngine();
	    final int size = ia.size();
	    final Panframe[] fa = new Panframe[size];
	    for (int i = 0; i < size; i++) {
	        final Panmage img = ia.get(i);
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
	
	public final static Panimation createAnm(final String name, final int dur, final Panple o, final Img... a) {
		return Pangine.getEngine().createAnimation(PRE_ANM + name, createFrames(name, dur, createSheet(name, o, a)));
	}
	
	public final static Panimation createAnm(final String name, final int dur, final Panple o, final Panple n, final Panple x, final Img... a) {
		return Pangine.getEngine().createAnimation(PRE_ANM + name, createFrames(name, dur, createSheet(name, o, n, x, a)));
	}
	
	public final static Panimation createAnm(final String name, final int dur, final Panmage... ia) {
	    return Pangine.getEngine().createAnimation(PRE_ANM + name, createFrames(name, dur, ia));
	}
	
	public final static Panimation createAnm(final String name, final int dur, final List<Panmage> ia) {
        return Pangine.getEngine().createAnimation(PRE_ANM + name, createFrames(name, dur, ia));
    }
	
	public final static Panlayer createHud(final Panroom room) {
		final Pangine engine = Pangine.getEngine();
		final Panlayer hud = engine.createLayer("layer.hud", engine.getEffectiveWidth(), engine.getEffectiveHeight(), 1, room);
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
        bg.setConstant(true);
        return bg;
	}
	
	public final static Panmage[] getDiamonds(final int d, final Pancolor f) {
	    final Pangine engine = Pangine.getEngine();
	    final Img dia = Imtil.newImage(d, d);
        Imtil.drawDiamond(dia, Pancolor.BLACK, Pancolor.BLACK, f);
        ImtilX.highlight(dia, 2);
        final Img diaIn = ImtilX.indent(dia);
        Imtil.setPseudoTranslucent(dia);
        Imtil.setPseudoTranslucent(diaIn);
        return new Panmage[] { engine.createImage(Pantil.vmid(), dia), engine.createImage(Pantil.vmid(), diaIn) };
	}
	
	// btnSize: -2 = smallest, 0 = default, 2 = largest
	public final static int getButtonSize(final int btnSize) {
	    final Pangine engine = Pangine.getEngine();
	    return (Math.min(60 * engine.getEffectiveWidth() / 400, 60 * engine.getEffectiveHeight() / 240) / 4 + btnSize) * 4 - 1;
	}
	
	public final static void createControlDiamond(final Panlayer layer, final Panmage diamond, final Panmage diamondIn, final ControlScheme ctrl, final float z) {
	    
	    final int h = (int) diamond.getSize().getX() / 2 + 1, d = h * 2;
	    ctrl.setLeft(createTouchButton(layer, "left", 0, h, z, diamond, diamondIn, TouchButton.OVERLAP_BEST));
	    ctrl.setDown(createTouchButton(layer, "down", h, 0, z, diamond, diamondIn, TouchButton.OVERLAP_BEST));
	    ctrl.setUp(createTouchButton(layer, "up", h, d, z, diamond, diamondIn, TouchButton.OVERLAP_BEST));
	    ctrl.setRight(createTouchButton(layer, "right", d, h, z, diamond, diamondIn, TouchButton.OVERLAP_BEST));
	}
	
	public final static TouchButton createTouchButton(final Panlayer layer, final String name, final int x, final int y, final float z,
	                                                  final Panmage img, final Panmage imgActive) {
	    return createTouchButton(layer, name, x, y, z, img, imgActive, TouchButton.OVERLAP_ANY);
	}
	
	public final static TouchButton createTouchButton(final Panlayer layer, final String name, final int x, final int y, final float z,
                                                      final Panmage img, final Panmage imgActive, final byte overlapMode) {
	    final Pangine engine = Pangine.getEngine();
        final Panteraction in = engine.getInteraction();
	    final TouchButton btn = new TouchButton(in, layer, name, x, y, z, img, imgActive);
	    btn.setOverlapMode(overlapMode);
	    engine.registerTouchButton(btn);
	    return btn;
	}
	
	public final static int getX(final Cursor cursor, final Touch touch) {
	    if (cursor == null) {
            return touch.getX();
        } else {
            return Math.round(cursor.getPosition().getX());
        }
	}
	
	public final static int getY(final Cursor cursor, final Touch touch) {
        if (cursor == null) {
            return touch.getY();
        } else {
            return Math.round(cursor.getPosition().getY());
        }
    }
	
	public final static String getEmail() {
		final StringBuilder b = new StringBuilder();
		b.append("ps");
		b.append("@g");
		b.append('m');
		b.append("ail.c");
		return "pandcor" + b + "om";
	}
	
	private final static void startScreenSaver(final ScreenSaver screenSaver) {
	    final Pangine engine = Pangine.getEngine();
	    final Panroom room = Pangame.getGame().getCurrentRoom();
	    final ScreenSaverIteration iteration = new ScreenSaverIteration();
	    Panlayer.iterateLayers(iteration);
	    final List<Panlayer> layersToActivate = iteration.layersToActivate;
        final Panlayer top = iteration.top;
	    final List<Mover> movers = screenSaver.movers;
	    final Panlayer layer = engine.createLayer(Pantil.vmid(), engine.getEffectiveWidth(), engine.getEffectiveHeight(), room.getSize().getZ(), room);
	    top.addAbove(layer);
	    final ScreenSaverBackground bg = new ScreenSaverBackground(screenSaver);
        layer.addActor(bg);
        for (final Mover mover : movers) {
    	    layer.addActor(mover);
    	    mover.unregisterListeners();
        }
	    movers.get(0).register(new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) {
                for (final Panlayer layerToActivate : layersToActivate) {
                    layerToActivate.setActive(true);
                }
                for (final Mover mover : movers) {
                    mover.detach();
                }
                layer.destroy();
                event.getInput().inactivate();
            }});
	}
	
	public final static class ScreenSaver {
	    private final List<Mover> movers = new ArrayList<Mover>();
	    private final Panmage black;
	    private final float z;
	    
	    public ScreenSaver(final Panmage black, final float z) {
	        this.black = black;
	        this.z = z;
	    }
	    
	    public ScreenSaver addMover(final Mover mover) {
	        movers.add(mover);
	        return this;
	    }
	    
	    public ScreenSaver addMover(final Panctor subject, final float x, final float y, final float z, final float vx, final float vy) {
	        final Mover mover = new Mover(subject);
	        subject.getPosition().set(x, y, z);
	        mover.getVelocity().set(vx, vy);
            return addMover(mover);
        }
	    
	    public final void start() {
	        startScreenSaver(this);
	    }
	    
	    public final void register() {
	        final ScreenSaverStarter starter = new ScreenSaverStarter(this);
	        Pangame.getGame().getCurrentRoom().addActor(starter);
	        starter.register(new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    starter.onInput();
                }});
	    }
	}
	
	private final static class ScreenSaverStarter extends Panctor implements StepListener {
	    private final static int threshold = 30 * 60 * 5;
	    private final ScreenSaver screenSaver;
	    private long timer = 0;
	    
	    private ScreenSaverStarter(final ScreenSaver screenSaver) {
	        this.screenSaver = screenSaver;
	        setVisible(false);
	    }
	    
	    private final void onInput() {
	        timer = 0;
	    }

        @Override
        public final void onStep(final StepEvent event) {
            timer++;
            if (timer > threshold) {
                final long cursorInactiveTimer = Cursor.getInactiveTimer();
                if (cursorInactiveTimer > threshold) {
                    timer = 0;
                    screenSaver.start();
                } else {
                    timer = cursorInactiveTimer;
                }
            }
        }
	}
	
	private final static class ScreenSaverBackground extends Panctor {
	    private final ScreenSaver screenSaver;
	    
	    private ScreenSaverBackground(final ScreenSaver screenSaver) {
	        this.screenSaver = screenSaver;
	    }
	    
	    @Override
	    protected final void renderView(final Panderer renderer) {
	        final Pangine engine = Pangine.getEngine();
	        renderer.render(getLayer(), screenSaver.black, 0, 0, screenSaver.z, 0, 0, engine.getEffectiveWidth(), engine.getEffectiveHeight(), 0, false, false);
	    }
	}
	
	private final static class ScreenSaverIteration implements Iteration<Panlayer> {
	    private final List<Panlayer> layersToActivate = new ArrayList<Panlayer>();
	    private Panlayer top = null;
        
	    @Override
	    public final boolean step(final Panlayer elem) {
            if (elem.getAbove() == null) {
                top = elem;
            }
            if (elem.isActive()) {
                elem.setActive(false);
                layersToActivate.add(elem);
            }
            return true;
        }
	}
	
	public final static ButtonImages newCircleImages(final int white, final int grey, final int darkGrey, final int black, final PixelFilter clearFilter, final int d) {
    	final Img circle = newButtonImg(d);
        Imtil.drawCircle(circle, black, black, grey, true);
        ImtilX.highlight(circle, new int[] { white }, true);
        ImtilX.highlight(circle, new int[] { darkGrey, grey, grey, grey, white, black, darkGrey }, false);
        return newButtonImages(circle, clearFilter, d, black, null);
	}
	
	public final static ButtonImages newRightImages(final int white, final int grey, final int darkGrey, final int black, final PixelFilter clearFilter, final int d) {
        final int d1 = d - 1, d3 = d1 - 2;
        final Img img = newButtonImg(d);
        for (int y = 3; y < d3; y++) {
            img.setRGB(0, y, black);
        }
        for (int x = 1; x < d1; x++) {
            final int y;
            if (x == 1) {
                y = 2;
            } else if (x < 4) {
                y = 1;
            } else if (x < 8) {
                y = 0;
            } else {
                y = (x - 6) / 2;
            }
            img.setRGB(x, y, black);
            img.setRGB(x, d1 - y, black);
            final int d1y1 = d1 - y - 1;
            for (int j = y + 2; j < d1y1; j++) {
                img.setRGB(x, j, grey);
            }
            img.setRGB(x, d1y1, darkGrey);
            img.setRGB(x, d1y1 - 4, white);
            img.setRGB(x, d1y1 - 5, black);
            if (x < d3) {
                img.setRGB(x, y + 1, white);
                img.setRGB(x, d1y1 - 6, darkGrey);
            } else {
                img.setRGB(x, y + 1, grey);
            }
        }
        final int y = (d1 - 6) / 2;
        for (int j = 0; j < 7; j++) {
            img.setRGB(d1, y + j, black);
        }
        return newButtonImages(img, clearFilter, d, black, null);
    }
	
	public final static ButtonImages newUpImages(final int white, final int grey, final int darkGrey, final int black) {
        final Img img = newButtonImg(32);
        for (int x = 3; x < 28; x++) {
            img.setRGB(x, 31, black);
            img.setRGB(x, 30, darkGrey);
            img.setRGB(x, 26, white);
            img.setRGB(x, 25, black);
            img.setRGB(x, 24, darkGrey);
        }
        for (int x = 3; x < 16; x++) {
            img.setRGB(x, 16 - x, black);
            img.setRGB(x, 17 - x, white);
        }
        for (int x = 16; x < 28; x++) {
            img.setRGB(x, x - 14, black);
            img.setRGB(x, x - 13, white);
        }
        for (int y = 18; y < 28; y++) {
            img.setRGB(0, y, black);
            img.setRGB(30, y, black);
        }
        for (int i = 0; i < 2; i++) {
            final int x = (i == 0) ? 1 : 29;
            for (int j = 0; j < 2; j++) {
                final int y = (j == 0) ? 16 : 22;
                img.setRGB(x, y, black);
                img.setRGB(x, y + 1, black);
                img.setRGB(x, y + 2, white);
                img.setRGB(x, y + 5, darkGrey);
            }
            img.setRGB(x, 28, black);
            img.setRGB(x, 29, black);
            final int x1 = (i == 0) ? 2 : 28;
            img.setRGB(x1, 14, black);
            img.setRGB(x1, 15, black);
            img.setRGB(x1, 16, white);
            img.setRGB(x1, 23, darkGrey);
            img.setRGB(x1, 24, black);
            img.setRGB(x1, 25, white);
            img.setRGB(x1, 29, darkGrey);
            img.setRGB(x1, 30, black);
        }
        return newButtonImages(img, null, 31, black, null);
    }
	
	public final static ButtonImages newDownImages(final int white, final int grey, final int darkGrey, final int black) {
        final Img img = newButtonImg(32);
        for (int x = 3; x < 28; x++) {
            img.setRGB(x, 1, black);
            img.setRGB(x, 2, white);
            final int y = (x < 16) ? (x + 9) : (39 - x);
            img.setRGB(x, y, darkGrey);
            img.setRGB(x, y + 1, black);
            img.setRGB(x, y + 2, white);
            img.setRGB(x, y + 6, darkGrey);
            img.setRGB(x, y + 7, black);
        }
        for (int y = 26; y < 31; y++) {
            img.setRGB(15, y, black);
        }
        for (int y = 5; y < 15; y++) {
            img.setRGB(0, y, black);
            img.setRGB(30, y, black);
        }
        for (int i = 0; i < 2; i++) {
            final int x = (i == 0) ? 1 : 29;
            for (int j = 0; j < 2; j++) {
                final int y = (j == 0) ? 3 : 9;
                img.setRGB(x, y, black);
                img.setRGB(x, y + 1, black);
                img.setRGB(x, y + 2, white);
                img.setRGB(x, y + 5, darkGrey);
            }
            img.setRGB(x, 15, black);
            img.setRGB(x, 16, black);
            final int x1 = (i == 0) ? 2 : 28;
            img.setRGB(x1, 2, black);
            img.setRGB(x1, 3, white);
            img.setRGB(x1, 10, darkGrey);
            img.setRGB(x1, 11, black);
            img.setRGB(x1, 12, black);
            img.setRGB(x1, 13, white);
            img.setRGB(x1, 16, darkGrey);
            img.setRGB(x1, 17, black);
            img.setRGB(x1, 18, black);
        }
        return newButtonImages(img, null, 31, black, new ImgProcessor() {
            @Override public final void process(final Img img) {
                final int c = PixelTool.getRgba(Pancolor.CLEAR);
                for (int y = 25; y < 28; y++) {
                    img.setRGB(15, y, c);
                }
                img.setRGB(15, 28, darkGrey);
            }});
    }
	
	public final static Img newButtonImg(final int d) {
        final Img img = Imtil.newImage(d, d);
        img.setTemporary(false);
        return img;
    }
	
	public final static ButtonImages newButtonImages(final Img img, final PixelFilter clearFilter, final int w, final int black, final ImgProcessor postIndentProcessor) {
        final Pangine engine = Pangine.getEngine();
        final Panmage full = engine.createImage(Pantil.vmid(), img);
        final Panmage base;
        if (clearFilter == null) {
            base = full;
        } else {
            Imtil.filterImg(img, clearFilter);
            base = engine.createImage(Pantil.vmid(), img);
        }
        ImtilX.indent2(img, 4, w, black);
        ImgProcessor.process(postIndentProcessor, img);
        final Panmage pressed = engine.createImage(Pantil.vmid(), img);
        img.close();
        return new ButtonImages(full, base, pressed);
    }
	
	public final static class ButtonImages {
	    public final Panmage full;
	    public final Panmage base;
	    public final Panmage pressed;
        
        public ButtonImages(final Panmage full, final Panmage base, final Panmage pressed) {
            this.full = full;
            this.base = base;
            this.pressed = pressed;
        }
    }
	
	protected final static Panframe getFrame(final Panframe[] frames, final Rotator rots, final int frameIndex) {
        Panframe frame = frames[frameIndex];
        if (frame == null) {
            final boolean basedOnImg1 = ((frameIndex % 2) == 0);
            final Panmage img = basedOnImg1 ? rots.getImage1() : rots.getImage2();
            final int rot = (4 - (frameIndex / 2)) % 4;
            final Panple o, min, max;
            if (basedOnImg1) {
                final Panple oBase = img.getOrigin();
                final Panple minBase = img.getBoundingMinimum();
                final Panple maxBase = img.getBoundingMaximum();
                final int end = rots.getDim(img) - 1;
                if (rot == 0) {
                    o = oBase;
                    min = minBase;
                    max = maxBase;
                } else if (rot == 3) {
                    o = new FinPanple2(end - oBase.getY(), oBase.getX());
                    min = new FinPanple2(-maxBase.getY(), minBase.getX());
                    max = new FinPanple2(-minBase.getY(), maxBase.getX());
                } else if (rot == 2) {
                    o = new FinPanple2(end - oBase.getX(), end - oBase.getY());
                    min = new FinPanple2(-maxBase.getX(), -maxBase.getY());
                    max = new FinPanple2(-minBase.getX(), -minBase.getY());
                } else if (rot == 1) {
                    o = new FinPanple2(oBase.getY(), end - oBase.getX());
                    min = new FinPanple2(minBase.getY(), -maxBase.getX());
                    max = new FinPanple2(maxBase.getY(), -minBase.getX());
                } else {
                    throw new IllegalStateException("Unexpected rotation " + rot);
                }
            } else {
                final Panframe prev = frames[frameIndex - 1];
                o = prev.getOrigin();
                min = prev.getBoundingMinimum();
                max = prev.getBoundingMaximum();
            }
            frame = Pangine.getEngine().createFrame(PRE_FRM + rots.getClass().getSimpleName() + "." + frameIndex, img, rots.frameDuration, rot, false, false, o, min, max);
            frames[frameIndex] = frame;
        }
        return frame;
    }
	
	public abstract static class Rotator {
        public final static int numFrames = 8;
        private final int frameDuration;
        private int frameIndex = 0;
        private int frameTimer = 0;
        
        protected Rotator(final int frameDuration) {
            this.frameDuration = frameDuration;
        }
        
        public void init() {
            frameIndex = 0;
            frameTimer = 0;
        }
        
        public final void onStep(final Panctor actor, final Panframe[] frames) {
            frameTimer++;
            if (frameTimer >= frameDuration) {
                frameTimer = 0;
                frameIndex++;
                if (frameIndex >= numFrames) {
                    frameIndex = 0;
                }
                actor.setView(getFrame(frames));
            }
        }
        
        public final Panframe getFrame(final Panframe[] frames) {
            return getFrame(frames, frameIndex);
        }
        
        public final Panframe getFrame(final Panframe[] frames, final int frameIndex) {
            return BaseGame.getFrame(frames, this, frameIndex);
        }
        
        protected int getDim(final Panmage img) {
            return Math.round(img.getSize().getX());
        }
        
        protected abstract Panmage getImage1();
        
        protected abstract Panmage getImage2();
    }
}
