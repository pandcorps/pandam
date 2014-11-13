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
package org.pandcorps.pandam.impl;

import java.nio.*;
import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.col.*;
import org.pandcorps.core.img.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.GlPanmage.*;

public abstract class GlPangine extends Pangine {
	public static Pangl gl = null;
	protected final static ArrayList<GlPanmage> images = new ArrayList<GlPanmage>();
	private final static List<GlPanmage> newImages = Coltil.newSafeList();
	protected final Panteraction interaction;
	protected final Set<Panput> active = Coltil.newSafeSet();
	protected final Set<Panput> newActive = Coltil.newSafeSet();
	protected final Set<Panput> ended = Coltil.newSafeSet();
	protected final static List<TouchEvent> touchEvents = Coltil.newSafeList();
	protected final static List<TouchButton> touchButtons = Coltil.newSafeList();
	private final static Map<Integer, Panput> touchMap = new HashMap<Integer, Panput>();
	protected final static List<InputEvent> inputEvents = Coltil.newSafeList();
	private FloatBuffer blendRectangle = null;
	public boolean capsLock = false;
	public boolean ins = false;
	//private long frameStartNano = System.nanoTime();
	private long frameStartNano = -frameLengthNano;
	//private Panctor tracked = null;
	
	protected GlPangine(final Panteraction interaction) {
		this.interaction = interaction;
	}
	
	@Override
	public final Panteraction getInteraction() {
		return interaction;
	}
	
	@Override
	protected final Panplementation newImplementation(final Panctor actor) throws Panception {
		return new ImplPanplementation(actor);
	}
	
	@Override
	protected final Panmage newImage(final String id, final Panple origin, final Panple boundMin, final Panple boundMax, final String location) throws Panception {
		final GlPanmage image = new GlPanmage(id, origin, boundMin, boundMax, location);
		newImages.add(image); // Don't add directly to images; might create images in one thread and rendering in another
		return image;
	}
	
	@Override
    protected final Panmage newImage(final String id, final Panple origin, final Panple boundMin, final Panple boundMax, final Img img) throws Panception {
	    final GlPanmage image = new GlPanmage(id, origin, boundMin, boundMax, img);
	    newImages.add(image);
        return image;
	}
	
	@Override
	protected Panmage[][] newSheet(final String prefix, final Panple origin, final Panple boundMin, final Panple boundMax, final String location,
                                   final int iw, final int ih) throws Panception {
	    final GlPanmage[][] sheet = GlPanmage.createSheet(prefix, origin, boundMin, boundMax, location, iw, ih);
	    //TODO should we keep track of textures, not images?
	    for (final GlPanmage[] row : sheet) {
	        for (final GlPanmage image : row) {
	            newImages.add(image);
	        }
	    }
	    return sheet;
	}
	
	protected int w = 640, h = 480;
	protected int truncatedWidth = w, truncatedHeight = h;
    protected boolean fullScreen = false;
	private boolean initialized = false;
	private float clr = 0.0f, clg = 0.0f, clb = 0.0f, cla = 0.0f;
	
	@Override
	public final void setDisplaySize(final int w, final int h) {
	    if (initialized) {
	        throw new UnsupportedOperationException("Cannot change size after initialization");
	    }
	    this.w = w;
	    this.h = h;
	}
	
	/*
	Size of window.
	Same as desktopWidth if game is in full-screen mode.
	*/
	@Override
	public final int getDisplayWidth() {
	    return w;
	}
	
	@Override
	public final int getDisplayHeight() {
	    return h;
	}
	
	/*
	Size of viewport within window.
	Portion of displayWidth that will be used for rendering.
	Same as displayWidth if displayWidth is a multiple of zoomMag (or zooming is disabled).
	If zoom leaves partial effective pixels on edges, then the edges will be black.
	*/
	@Override
	public final int getTruncatedWidth() {
	    return truncatedWidth;
	}
	
	@Override
	public final int getTruncatedHeight() {
        return truncatedHeight;
    }
	
	@Override
    public final void setFullScreen(final boolean fullScreen) {
        if (initialized) {
            throw new UnsupportedOperationException("Cannot change full-screen after initialization");
        }
        this.fullScreen = fullScreen;
    }
	
	@Override
	public final boolean isFullScreen() {
	    return fullScreen;
	}
	
	protected abstract void initDisplay() throws Exception;
	
	@Override
	//protected final void start() throws Exception {
	protected final void init() throws Exception {
	    initialized = true;
	    initDisplay();
	    
		gl.glEnable(gl.GL_TEXTURE_2D); // Enable Texture Mapping
		setBgColor();
		
		gl.glEnableClientState(gl.GL_TEXTURE_COORD_ARRAY);
		gl.glEnableClientState(gl.GL_VERTEX_ARRAY);
		
		//gl.glPolygonMode(gl.GL_FRONT_AND_BACK, gl.GL_FILL);

		//gl.glClearDepth(Double.NEGATIVE_INFINITY); // Depth Buffer Setup
		gl.glClearDepth(Double.MAX_VALUE);
		gl.glEnable(gl.GL_DEPTH_TEST); // Enables Depth Testing
		gl.glDepthFunc(gl.GL_LESS);
		//gl.glDepthFunc(gl.GL_GREATER);
		gl.glDepthMask(true);

		//gl.glEnable(gl.GL_BLEND); // Needed?
		gl.glEnable(gl.GL_ALPHA_TEST);
		gl.glAlphaFunc(gl.GL_GREATER, 0);
		
		initInput();
		initViewport();
	}
	
	@Override
	protected final void destroyLayer(final Panlayer layer) {
		for (final GlPanmage image : images) {
			image.destroyLayer(layer);
		}
	}
	
	private final void initViewport() {
		int w = getDesktopWidth(), h = getDesktopHeight();
		final float z1 = getZoom();
		final int z = (int) z1;
		final float z2 = z;
		if (z1 == z2) {
			w = (w / z) * z;
			h = (h / z) * z;
		}
		truncatedWidth = w;
		truncatedHeight = h;
		gl.glViewport(0, 0, w, h);
	}
	
	@Override
	protected final void recreate() throws Exception {
		init();
		final IdentityHashSet<Texture> textures = new IdentityHashSet<Texture>();
		for (final GlPanmage image : images) {
			if (textures.add(image.tex)) { // A sheet of images can share same Texture
				image.tex.scratch.rewind();
				image.rebindTexture();
			}
		}
	}
	
	protected abstract void initInput() throws Exception;
	
	protected volatile boolean running = true;
	
	protected volatile Throwable exitCause = null;
	
	private final static boolean isActive(final Panctor actor) {
	    // Listeners that aren't bound to an actor are always active
	    return actor == null ? true : actor.isActive();
	}
	
	protected abstract void stepControl() throws Exception;
	
	protected final void stepTouch() {
		final int size = touchEvents.size();
		//int size;
    	//while ((size = touchEvents.size()) > 0) {
		for (int i = 0; i < size; i++) {
    		//final TouchEvent event = touchEvents.remove(size - 1);
			final TouchEvent event = touchEvents.get(i);
    		final Integer key = Integer.valueOf(event.getId());
    		final int x = event.getX(), y = event.getY();
    		Panput input = interaction.TOUCH;
    		float bestDist = Float.MAX_VALUE;
    		for (final TouchButton button : touchButtons) {
    			if (button.isEnabled() && button.contains(x, y)) {
    				if (button.getOverlapMode() == TouchButton.OVERLAP_ANY) {
    				    input = button;
    				    break;
    				}
    				final float diffX = button.getCenterX() - x, diffY = button.getCenterY() - y;
    				final float currDist = (diffX * diffX) + (diffY * diffY);
    				if (currDist < bestDist) {
    				    input = button;
    				    bestDist = currDist;
    				}
    			}
    		}
    		final byte type = event.getType();
    		if (type == Panput.TOUCH_MOVE) {
    			final Panput old = touchMap.put(key, input);
    			if (input != old) {
    			    activateMove(old, false);
    				activateMove(input, true);
    			}
    		} else {
    			if (type == Panput.TOUCH_DOWN) {
    				activate(input);
    				touchMap.put(key, input);
    			} else {
    				deactivate(input);
    				touchMap.remove(key);
    			}
    		}
    	}
		clear(touchEvents, size);
		
		stepInputs();
	}
	
	private final static void clear(final List<?> v, final int size) {
		for (int i = size - 1; i >= 0; i--) {
			v.remove(i);
		}
	}
	
	private final void stepInputs() {
		final int size = inputEvents.size();
		for (int i = 0; i < size; i++) {
			final InputEvent event = inputEvents.get(i);
			activate(event.input, event.active);
		}
		clear(inputEvents, size);
	}
	
	private final void activateMove(final Panput input, final boolean active) {
	    if (input == null) {
	        return;
	    } else if (input.getClass() == TouchButton.class) {
	        final TouchButton button = (TouchButton) input;
	        if (button.isMoveInterpretedAsCancel()) {
	            button.activate(active);
	        } else {
	            activate(input, active);
	        }
        } else {
            activate(input, active);
        }
	}
	
	public final void addTouchEvent(final int id, final byte type, final float x, final float y) {
		final float zoom = getZoom();
		//touchEvents.add(new TouchEvent(id, type, Math.round(x / zoom), Math.round((getTruncatedHeight() - y) / zoom)));
		touchEvents.add(new TouchEvent(id, type, Math.round(x / zoom), Math.round((getDisplayHeight() - 1 - y) / zoom)));
		/*
		If bottom row is touched, incoming y will be displayHeight - 1.  We want to convert that to 0.
		If top row is touched, incoming y will be 0.  We'd convert that to displayHeight - 1.
		If top visible row after truncation is touched, incoming y will be [edgeSize].
		We'd want to convert that to (truncatedHeight - 1) = (displayHeight - [edgeSize] - 1).
		This appears to be one case where we still want to use displayHeight instead of truncatedHeight.
		*/
	}
	
	public final void addInputEvent(final Panput input, final boolean active) {
		inputEvents.add(new InputEvent(input, active));
	}
	
	@Override
	public final void registerTouchButton(final TouchButton button) {
		touchButtons.add(button);
	}
	
	@Override
	public final void unregisterTouchButton(final TouchButton button) {
	    touchButtons.remove(button);
	    //for (final Map.Entry<Integer, Panput> entry : touchMap.entrySet()) {
	        // Remove this button, but are there thread-safety issues?
	    //}
	}
	
	@Override
	public final boolean isTouchButtonRegistered(final TouchButton button) {
	    return touchButtons.contains(button);
	}
	
	@Override
	public final void clearTouchButtons() {
		touchButtons.clear();
		interaction.unregister(interaction.TOUCH);
		//touchMap.clear(); // Thread-safety?
	}
	
	@Override
	protected final void loop() throws Exception {
		while (running) {
			frame();
		}
	}
	
	public final void frame() throws Exception {
	    try {
	        onFrame();
	    } catch (final Exception e) {
	        onFatal(e);
	        throw e;
	    }
	}
	
	public final void onFrame() throws Exception {
		//final long frameStart = System.currentTimeMillis();
	    //final long frameStartNano = System.nanoTime();
		//System.out.println(System.currentTimeMillis());
		sleep();
	    
	    if (exitCause != null) {
	        exitCause.printStackTrace();
	        exit();
	        throw Pantil.toException(exitCause);
	    }
	    
	    ended.clear();
	    stepControl();
		for (final Panput input : active) {
			onAction(input);
		}
		active.addAll(newActive);
		newActive.clear();
		step();
		draw();
	}
	
	private final void sleep() {
		try {
		    //final long sleepTime = frameLength - System.currentTimeMillis() + frameStart;
		    final long sleepTime = frameLengthNano - System.nanoTime() + frameStartNano;
		    //System.out.println(sleepTime);
		    if (sleepTime > 0) {
		        //Thread.sleep(sleepTime);
		        final long sleepTimeMillis = sleepTime / 1000000;
		        Thread.sleep(sleepTimeMillis, (int) (sleepTime - sleepTimeMillis * 1000000));
		    }
		    frameStartNano = System.nanoTime();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
	}
	
	protected final void activate(final Panput input, final boolean active) {
		if (input == null) {
			return;
		} else if (active) {
			activate(input);
		} else {
			deactivate(input);
		}
	}
	
	private final void activateTouch(final Panput input, final boolean active) {
		if (input.getClass() == TouchButton.class) {
		    final TouchButton btn = (TouchButton) input;
			btn.activate(active);
			activate(btn.getMappedInput(), active);
		}
	}
	
	private final void activate(final Panput input) {
		// copy to prevent ConcurrentModificationException
		for (final ActionStartListener startListener : Coltil.copy(interaction.getStartListeners(input))) {
			//startListener.onActionStart(ActionStartEvent.INSTANCE);
		    //startListener.onActionStart(ActionStartEvent.getEvent(input, Character.valueOf(Keyboard.getEventCharacter())));
		    if (!isActive(getActor(startListener))) {
		    	continue;
		    }
			startListener.onActionStart(ActionStartEvent.getEvent(input));
		}
		/*final Panction action = interaction.getAction(input);
		if (action != null) {
			setActive(action, true);
		}*/
		onAction(input);
		newActive.add(input);
		setActive(input, true);
		activateTouch(input, true);
	}
	
	private final void deactivate(final Panput input) {
		boolean uncaught = true;
		for (final ActionEndListener endListener : Coltil.unnull(interaction.getEndListeners(input))) {
			//endListener.onActionEnd(ActionEndEvent.INSTANCE);
		    if (!isActive(getActor(endListener))) {
                continue;
            }
		    endListener.onActionEnd(ActionEndEvent.getEvent(input));
		    uncaught = false;
		}
		if (uncaught && input == interaction.BACK) {
			if (uncaughtBackHandler == null) {
				System.out.println("UNCAUGHT BACK, EXITING");
				exit();
			} else {
				uncaughtBackHandler.run();
			}
		}
		/*
		TODO
		If a key is pressed and released during the same frame,
		then action.isActive() will return false for that frame.
		This is different than an ActionListener,
		which would still fire its event for that one frame.
		*/
		/*final Panction action = interaction.getAction(input);
		if (action != null) {
			setActive(action, false);
		}*/
		active.remove(input);
		newActive.remove(input);
		setActive(input, false);
		ended.add(input);
		activateTouch(input, false);
	}
	
	@Override
	protected final boolean isEnded(final Panput input) {
	    return ended.contains(input);
	}
	
	private final static int near = -1000, far = 1000;
	
	private final float[] cr = new float[2];
	
	private final static class Camera {
		final float xi, xa, yi, ya, zi, za;
		
		private Camera(final float xi, final float xa, final float yi, final float ya, final float zi, final float za) {
		    /*
		    Format right away so that dependent layers are based on the final values.
		    Otherwise it might be possible to move layers 1 and 3 but not 2, which looks wrong.
		    */
			this.xi = formatCam(xi);
			this.xa = formatCam(xa);
			this.yi = formatCam(yi);
			this.ya = formatCam(ya);
			this.zi = formatCam(zi);
			this.za = formatCam(za);
		}
	}
	
	private final IdentityHashMap<Panlayer, Camera> cams = new IdentityHashMap<Panlayer, Camera>();

	private final void initCamera(final Panlayer layer) {
		if (cams.containsKey(layer)) {
			return;
		}
		final float zoomMag = getZoom();
		final float wz = truncatedWidth / zoomMag, hz = truncatedHeight / zoomMag;
		final Panlayer master = layer.getMaster();
		if (master != null) {
			initCamera(master);
			final Camera mcam = cams.get(master);
			final Panple lsize = layer.getSize(), msize = master.getSize();
			final float xc1 = getCamMin(mcam.xi, lsize.getX(), msize.getX(), wz);
			final float yc1 = getCamMin(mcam.yi, lsize.getY(), msize.getY(), hz);
			cams.put(layer, new Camera(xc1, xc1 + wz, yc1, yc1 + hz, near, far));
			return;
		}
		final Panctor tracked = layer.getTracked();
		final Panple origin = layer.getOrigin();
		final float ox = origin.getX(), oy = origin.getY();
		if (tracked == null) {
			//cams.put(layer, new Camera(0, w, 0, h, near, far));
		    cams.put(layer, new Camera(ox, ox + wz, oy, oy + hz, near, far));
		} else {
			final Panple pos = tracked.getPosition();
			final int x = Math.round(pos.getX());
			final int y = Math.round(pos.getY());
			//final int w2 = w / 2, h2 = h / 2;
			//final int w2 = w / 4, h2 = h / 4;
			//final float w2 = wz / 2, h2 = hz / 2;
			// Does this work if w or h is odd?
			//gl.glOrtho(x - w2, x + w2, y - h2, y + h2, near, far);
			final Panple lsize = layer.getSize();
			checkCamRange(x, ox, wz, lsize.getX());
			final float xc1 = cr[0], xc2 = cr[1];
			checkCamRange(y, oy, hz, lsize.getY());
			final float yc1 = cr[0], yc2 = cr[1];
			cams.put(layer, new Camera(xc1, xc2, yc1, yc2, near, far));
		}
	}
	
	private final void camera(final Panlayer layer) {
//if (layer.getClass() != Panroom.class) return;
		gl.glMatrixMode(gl.GL_PROJECTION); // Select The Projection Matrix
		gl.glLoadIdentity(); // Reset The Projection Matrix
		//GLU.gluOrtho2D(-w / 2, w / 2, -h / 2, h / 2);
		//GLU.gluOrtho2D(0, w, 0, h);
		cam(layer);
		//gl.glOrtho(0, w, 0, h, -maxDimension, maxDimension);
		gl.glMatrixMode(gl.GL_MODELVIEW);
	}
	
	private final static int formatCam(final float c) {
	    return Math.round(c);
	}
	
	private void cam(final Panlayer layer) {
		final Camera c = cams.get(layer);
		final float xi = c.xi, xa = c.xa, yi = c.yi, ya = c.ya, zi = c.zi, za = c.za;
		gl.glOrtho(xi, xa, yi, ya, zi, za);
		//gl.glOrtho((int) xi, (int) xa, (int) yi, (int) ya, zi, za);
		// Formatted in Camera constructor
		//gl.glOrtho(formatCam(xi), formatCam(xa), formatCam(yi), formatCam(ya), zi, za);
		getRawViewMinimum(layer).set(xi, yi, zi);
		getRawViewMaximum(layer).set(xa, ya, za);
	}
	
	private final void checkCamRange(final float p, final float o, final float sz, final float _sl) {
	    final float s2 = sz / 2;
	    float pc1 = p - s2, pc2;
        if (pc1 < o) {
            pc1 = o;
            pc2 = o + sz;
        } else {
            pc2 = p + s2;
            final float sl = Math.max(_sl, o + sz);
            if (pc2 > sl) {
                pc1 = sl - sz;
                pc2 = sl;
            }
        }
        cr[0] = pc1;
        cr[1] = pc2;
	}
	
	private final float getCamMin(final float i, final float lp, final float mp, final float sz) {
		return lp == mp ? i : (i * (lp - sz) / (mp - sz));
	}

	private final void draw() {
		gl.glClear(gl.GL_COLOR_BUFFER_BIT);
		final Panroom room = Pangame.getGame().getCurrentRoom();
		cams.clear();
		for (Panlayer layer = room.getBase(); layer != null; layer = layer.getAbove()) {
			initCamera(layer);
		}
		for (Panlayer layer = room.getBase(); layer != null; layer = layer.getAbove()) {
		    draw(layer);
		}
		edge();
		if (screenShotDst != null) {
		    final ByteBuffer buf = Pantil.allocateDirectByteBuffer(w * h * 3);
		    //buf.rewind();
		    gl.glReadPixels(0, 0, w, h, gl.GL_RGB, gl.GL_UNSIGNED_BYTE, buf); // Could read each frame and filter, but very slow
		    final String dst;
		    if (screenShotInd >= 0) {
		    	dst = screenShotDst + screenShotInd + ".png";
		    	screenShotInd++;
		    } else {
		    	dst = screenShotDst;
		    	screenShotDst = null;
		    }
		    final Img img = Imtil.create(buf, w, h, Imtil.TYPE_INT_RGB);
		    Imtil.save(img, dst);
		    img.close();
		}
		update();
	}
	
	protected abstract void update();
	
	private final void draw(final Panlayer room) {
	    final boolean visible = room.isVisible();
	    if (room.isClearDepthEnabled()) {
	        if (!visible) {
	            throw new UnsupportedOperationException("Don't clear depth if not visible");
	        }
//if (room instanceof Panroom) {
	        gl.glClear(gl.GL_DEPTH_BUFFER_BIT);
//}
	    }
	    if (!visible) {
	        return;
	    }
		camera(room); // Must be after step() for tracking to work right
		final boolean hasNew = !newImages.isEmpty();
		while (!newImages.isEmpty()) {
		    images.add(newImages.remove(newImages.size() - 1));
		}
		final int size = images.size();
		//for (final GlPanmage image : images) { // ConcurrentModificationException if bg Thread loads images at same time
		if (hasNew || !room.isBuffered() || !room.isBuilt()) {
			for (int i = 0; i < size; i++) { // If size has grown, we shouldn't be drawing new images yet anyway
	            images.get(i).clear(room);
	        }
			final Collection<Panctor> actors = room.getActors();
			if (actors != null) {
				for (final Panctor actor : actors) {
					renderView(actor);
				}
			}
		}
		//for (final GlPanmage image : images) { // See above
		for (int i = 0; i < size; i++) {
		    /*try { // Try to see if double buffering is enabled
                Thread.sleep(30);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }*/
		    images.get(i).renderAll(room);
		}
		blend(room);
		//Display.update();
	}
	
	private final byte toByte(final short c) {
	    return (byte) (c + Byte.MIN_VALUE);
	}
	
	private final void edge() {
		final int w = getDisplayWidth(), h = getDisplayHeight();
		if (truncatedWidth < w || truncatedHeight < h) {
			gl.glViewport(truncatedWidth, 0, w, h);
			quad(Pancolor.BLACK, 0, w, 0, h, 0);
			gl.glViewport(0, truncatedHeight, w, h);
			quad(Pancolor.BLACK, 0, w, 0, h, 0);
			gl.glViewport(0, 0, truncatedWidth, truncatedHeight);
		}
	}
	
	private final void blend(final Panlayer room) {
	    // opengl.org - the depth buffer is not updated if the depth test is disabled
	    final Pancolor color = room.getBlendColor();
	    final short a = color.getA();
	    if (a == 0) {
	        return;
	    }
	    final Camera c = cams.get(room);
	    quad(color, c.xi, c.xa, c.yi, c.ya, c.za);
	}
	
	private final void quad(final Pancolor color, final float minx, final float maxx, final float miny, final float maxy, final float z) {
	    gl.glLoadIdentity();
	    gl.glDisable(gl.GL_DEPTH_TEST);
	    gl.glDisable(gl.GL_TEXTURE_2D);
	    gl.glDisableClientState(gl.GL_TEXTURE_COORD_ARRAY);
	    final short a = color.getA();
	    final boolean blending = a < Pancolor.MAX_VALUE;
	    if (blending) {
		    gl.glEnable(gl.GL_BLEND);
		    gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);
	    }
        gl.glColor4b(toByte(color.getR()), toByte(color.getG()), toByte(color.getB()), toByte(a));
        //final int maxx = 256, maxy = 192;
		final boolean quad = gl.isQuadSupported();
		if (blendRectangle == null) {
			blendRectangle = Pantil.allocateDirectFloatBuffer(quad ? 12 : 18);
		}
        blendRectangle.rewind();
        blendRectangle.put(maxx);
        blendRectangle.put(maxy);
        blendRectangle.put(z);
        blendRectangle.put(minx);
        blendRectangle.put(maxy);
        blendRectangle.put(z);
        blendRectangle.put(minx);
        blendRectangle.put(miny);
        blendRectangle.put(z);
        blendRectangle.put(maxx);
        blendRectangle.put(miny);
        blendRectangle.put(z);
        if (!quad) {
        	blendRectangle.put(maxx);
            blendRectangle.put(maxy);
            blendRectangle.put(z);
            blendRectangle.put(minx);
            blendRectangle.put(miny);
            blendRectangle.put(z);
        }
        blendRectangle.rewind();
        //gl.glDrawElements(gl.GL_QUADS, blendRectangle); array of indices into other arrays
        gl.glVertexPointer(3, 0, blendRectangle);
        gl.glDrawArrays(quad ? gl.GL_QUADS : gl.GL_TRIANGLES, 0, quad ? 4 : 6); // Number of vertices
        gl.glColor4b(Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE);
        if (blending) {
        	gl.glDisable(gl.GL_BLEND);
        }
        gl.glEnableClientState(gl.GL_TEXTURE_COORD_ARRAY);
        gl.glEnable(gl.GL_TEXTURE_2D);
        gl.glEnable(gl.GL_DEPTH_TEST);
	}

	private final void onAction(final Panput input) {
		for (final ActionListener listener : Coltil.unnull(interaction.getListeners(input))) {
			//listener.onAction(ActionEvent.INSTANCE);
			if (!isActive(getActor(listener))) {
		    	continue;
		    }
		    listener.onAction(ActionEvent.getEvent(input));
		}
	}

	//@Override
	//public final void track(final Panctor actor) {
	//	tracked = actor;
	//}
	
	@Override
	public final void setBgColor(final Pancolor color) {
	    this.clr = color.getRf();
	    this.clg = color.getGf();
	    this.clb = color.getBf();
	    this.cla = color.getAf();
	    setBgColor();
	}
	
	private final void setBgColor() {
	    gl.glClearColor(clr, clg, clb, cla);
	}
	
	@Override
	public final boolean enableBuffers() {
	    try {
	        gl.glEnable(gl.GL_ARRAY_BUFFER_BINDING);
	        return true;
	    } catch (final Exception e) {
	        return false;
	    }
    }

	@Override
	public final void exit() {
	    // Should allow exit to be called from any Thread, so do gl stuff in destroy which is called by main Thread
		running = false;
	}
	
	@Override
	public final void exit(final Throwable cause) {
	    // If a fatal problem occurs in another Thread, it can call this method to trigger an exit from the main Thread
	    exitCause = cause;
	}
	
	@Override
    protected final void destroy() throws Exception {
	    gl.glDisableClientState(gl.GL_VERTEX_ARRAY);
        gl.glDisableClientState(gl.GL_TEXTURE_COORD_ARRAY);
        getAudio().close();
        onDestroy();
    }
    
    protected abstract void onDestroy() throws Exception;
    
    private final static class InputEvent {
    	private final Panput input;
    	private final boolean active;
    	
    	private InputEvent(final Panput input, final boolean active) {
    		this.input = input;
    		this.active = active;
    	}
    }
}
