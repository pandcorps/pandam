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
package org.pandcorps.pandam.lwjgl;

import java.awt.image.*;
import java.nio.*;
import java.util.*;

import org.lwjgl.input.*;
import org.lwjgl.opengl.*;
//import org.lwjgl.util.glu.*;
import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.event.action.*;

public final class LwjglPangine extends Pangine {
	private static LwjglPangine engine = null;
	private final static LwjglPanteraction interaction = new LwjglPanteraction();
	private final static ArrayList<LwjglPanmage> images = new ArrayList<LwjglPanmage>();
	private final HashSet<Panput> active = new HashSet<Panput>();
	private final HashSet<Panput> newActive = new HashSet<Panput>();
	private final FloatBuffer blendRectangle = Pantil.allocateDirectFloatBuffer(12);
	/*package*/ boolean capsLock = false;
	/*package*/ boolean ins = false;
	//private Panctor tracked = null;

	private LwjglPangine() {
		engine = this;
	}

	public final static LwjglPangine getEngine() {
		return engine == null ? new LwjglPangine() : engine;
	}

	@Override
	protected final Panmage newImage(final String id, final Panple origin, final Panple boundMin, final Panple boundMax, final String location) throws Panception {
		final LwjglPanmage image = new LwjglPanmage(id, origin, boundMin, boundMax, location);
		images.add(image);
		return image;
	}
	
	@Override
    protected final Panmage newImage(final String id, final Panple origin, final Panple boundMin, final Panple boundMax, final BufferedImage img) throws Panception {
	    final LwjglPanmage image = new LwjglPanmage(id, origin, boundMin, boundMax, img);
        images.add(image);
        return image;
	}
	
	@Override
	protected Panmage[][] newSheet(final String prefix, final Panple origin, final Panple boundMin, final Panple boundMax, final String location,
                                   final int iw, final int ih) throws Panception {
	    final LwjglPanmage[][] sheet = LwjglPanmage.createSheet(prefix, origin, boundMin, boundMax, location, iw, ih);
	    //TODO should we keep track of textures, not images?
	    for (final LwjglPanmage[] row : sheet) {
	        for (final LwjglPanmage image : row) {
	            images.add(image);
	        }
	    }
	    return sheet;
	}

	@Override
	protected final Panplementation newImplementation(final Panctor actor) throws Panception {
		return new LwjglPanplementation(actor);
	}

	@Override
	public final Panteraction getInteraction() {
		return interaction;
	}
	
	@Override
    public final int getDesktopWidth() {
        return Display.getDesktopDisplayMode().getWidth();
    }
    
    @Override
    public final int getDesktopHeight() {
        return Display.getDesktopDisplayMode().getHeight();
    }

    private int w = 640, h = 480;
    private boolean fullScreen = false;
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
	
	@Override
	public final int getDisplayWidth() {
	    return w;
	}
	
	@Override
	public final int getDisplayHeight() {
	    return h;
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

	@Override
	//protected final void start() throws Exception {
	protected final void init() throws Exception {
	    initialized = true;
	    if (fullScreen) {
	        Display.setFullscreen(fullScreen);
	        Mouse.setGrabbed(true); // Some games might use cursor; might also want to disable in Window; might expose in Pangine
	    } else {
	        // Should be able to set resolution in full-screen too, but only certain values would be valid
	        Display.setDisplayMode(new DisplayMode(w, h));
	    }
		/*
		Display.setFullscreen(true);
		org.lwjgl.util.Display.getAvailableDisplayModes
		org.lwjgl.util.Display.setDisplayMode
		*/
	    try {
	    	Display.create();
	    } catch (final Exception e) {
	    	System.err.println("Could not create display");
	    	System.err.println("Desktop: " + engine.getDesktopWidth() + " * " + engine.getDesktopHeight());
	    	System.err.println("DesktopDisplayMode" + Display.getDesktopDisplayMode());
	    	System.err.println("DisplayMode: " + Display.getDisplayMode());
	    	System.err.println("AvailableDisplayModes:");
	    	for (final DisplayMode dm : Display.getAvailableDisplayModes()) {
	    		System.err.println("    " + dm);
	    	}
	    	throw e;
	    }
	    
	    if (fullScreen) {
	        // setGrabbed above doesn't hide cursor in newer OS; these don't either, though 
	        final IntBuffer buf = Pantil.allocateDirectIntBuffer(4);
	        for (int i = 0; i < 4; i++) {
	            buf.put(0);
	        }
	        buf.rewind();
	        Mouse.setNativeCursor(new Cursor(1, 1, 0, 0, 1, buf, null));
	        Mouse.setGrabbed(true);
	        Mouse.setCursorPosition(0, 0);
	    }

		GL11.glEnable(GL11.GL_TEXTURE_2D); // Enable Texture Mapping
		setBgColor();
		
		GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		
		//GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);

		//GL11.glClearDepth(Double.NEGATIVE_INFINITY); // Depth Buffer Setup
		GL11.glClearDepth(Double.MAX_VALUE);
		GL11.glEnable(GL11.GL_DEPTH_TEST); // Enables Depth Testing
		GL11.glDepthFunc(GL11.GL_LESS);
		//GL11.glDepthFunc(GL11.GL_GREATER);
		GL11.glDepthMask(true);

		//GL11.glEnable(GL11.GL_BLEND); // Needed?
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0);
		
		Controllers.create();
		final int cs = Controllers.getControllerCount();
		for (int i = 0; i < cs; i++) {
			final Controller c = Controllers.getController(i);
			final int bs = c.getButtonCount();
			final List<Button> blist = new ArrayList<Button>(bs);
			for (int j = 0; j < bs; j++) {
				blist.add(newButton(c.getButtonName(j)));
			}
			addController(c.getName(), newButton("Left"), newButton("Right"), newButton("Up"), newButton("Down"), blist);
		}

		//new Thread(new Runnable() {public void run() {play();}}).start();
	}

	private boolean running = true;
	
	private final static boolean isActive(final Panctor actor) {
	    // Listeners that aren't bound to an actor are always active
	    return actor == null ? true : actor.isActive();
	}
	
	@Override
	protected final void start() throws Exception {
		while (running) {
			//final long frameStart = System.currentTimeMillis();
		    final long frameStartNano = System.nanoTime();
			//System.out.println(System.currentTimeMillis());
			Keyboard.poll();
			Keyboard.enableRepeatEvents(false);
			/*
			System.out.println("REE " + Keyboard.areRepeatEventsEnabled());
			System.out.println("EK  " + Keyboard.getEventKey());
			System.out.println("EKS " + Keyboard.getEventKeyState());
			for (int i = 0; i < 10; i++) {
				System.out.println("N " + i + " " + Keyboard.next());
			}
			*/
			/*
			while (Keyboard.next()) {
				System.out.println("EK " + f + " " + Keyboard.getEventKey() + Keyboard.getEventKeyState());
			}
			*/
			if (/*Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) ||*/ Display.isCloseRequested()) {
				exit();
			}
			
			// We can tell when caps lock pressed, but not whether it was enabled before app started
			//Keyboard.isCapsLock() // can't see anything like this
			//org.lwjgl.input.Controller/Contrllers/Cursor/Mouse // can't find anything else
			//javax.swing.KeyStroke // can't find anything in this area

			while (Keyboard.next()) {
				final Key key = interaction.getKey(Keyboard.getEventKey());
				//System.out.println(key.getIndex());
				//System.out.println(Keyboard.getEventCharacter());
				//System.out.println(Keyboard.getKeyName(key.getIndex()));
				//System.out.println(java.awt.AWTKeyStroke.getAWTKeyStroke(key.getIndex(), 0).getKeyChar()); // Always '?'
				//System.out.println(java.awt.AWTKeyStroke.getAWTKeyStroke(key.getIndex(), java.awt.event.InputEvent.BUTTON1_DOWN_MASK).getKeyChar()); // Always '?'
				//System.out.println(javax.swing.KeyStroke.getKeyStroke(key.getIndex(), java.awt.event.InputEvent.BUTTON1_DOWN_MASK).getKeyChar()); // Always '?'
				final boolean active = Keyboard.getEventKeyState();
				if (active) {
				    if (key == interaction.KEY_CAPS_LOCK) {
				        capsLock = !capsLock;
				    } else if (key == interaction.KEY_INS) {
				        /*
				         * Insert isn't a system setting like caps lock.
				         * You can enable Insert in one app,
				         * then go to another app,
				         * and it won't be enabled there.
				         */
				        ins = !ins;
				    } else if (key.isLetter()) {
				        // Using Keyboard methods instead of Pandam methods
				        // to prevent circular calls
				        capsLock = Character.isUpperCase(Keyboard.getEventCharacter());
				        if (Keyboard.isKeyDown(interaction.IND_SHIFT_LEFT) || Keyboard.isKeyDown(interaction.IND_SHIFT_RIGHT)) {
				            capsLock = !capsLock;
				        }
				    }
				}
				activate(key, active);
				//System.out.println("EK " + f + " " + Keyboard.getEventKey() + Keyboard.getEventKeyState());
			}
			
			Controllers.poll();
			while (Controllers.next()) {
				final Button button;
				final boolean a;
				final Controller src = Controllers.getEventSource();
				final Panteraction.Controller pc = interaction.CONTROLLERS.get(src.getIndex());
				/*System.out.println("Name: " + src.getName()
						+ "; AxisCount: " + src.getAxisCount() + "; ButtonCount: " + src.getButtonCount());
				System.out.println("Axis: " + Controllers.isEventAxis()
						+ "; X: " + Controllers.isEventXAxis() + "; Y: " + Controllers.isEventYAxis()
						+ "; PX: " + Controllers.isEventPovX() + "; PY: " + Controllers.isEventPovY()
						+ "; Btn: " + Controllers.isEventButton() + "; Ind: " + Controllers.getEventControlIndex());*/
				if (Controllers.isEventButton()) {
					final int ind = Controllers.getEventControlIndex();
					button = pc.BUTTONS.get(ind);
					a = src.isButtonPressed(ind);
				} else {
					final float val;
					final Button pos, neg;
					final boolean simple = src.getAxisCount() <= 2;
					if (simple && Controllers.isEventXAxis()) {
						val = src.getXAxisValue();
						pos = pc.RIGHT;
						neg = pc.LEFT;
					} else if (simple && Controllers.isEventYAxis()) {
						val = src.getYAxisValue();
						pos = pc.DOWN;
						neg = pc.UP;
					} else if (Controllers.isEventPovX()) {
						val = src.getPovX();
						pos = pc.RIGHT;
						neg = pc.LEFT;
					} else if (Controllers.isEventPovY()) {
						val = src.getPovY();
						pos = pc.DOWN;
						neg = pc.UP;
					} else {
						continue;
					}
					if (val > 0) {
						button = pos;
						// Check for immediate direction change without releasing axis
						activate(Pantil.nvl(Coltil.has(active, neg), Coltil.has(newActive, neg)), false);
						a = true;
					} else if (val < 0) {
						button = neg;
						activate(Pantil.nvl(Coltil.has(active, pos), Coltil.has(newActive, pos)), false);
						a = true;
					} else {
						button = Pantil.nvl(Coltil.has(active, pos), Coltil.has(active, neg), Coltil.has(newActive, pos), Coltil.has(newActive, neg));
						a = false;
					}
				}
				activate(button, a);
			}
			Controllers.clearEvents();
			
			for (final Panput input : active) {
				onAction(input);
			}
			active.addAll(newActive);
			newActive.clear();
			step();
			draw();
			try {
			    //final long sleepTime = frameLength - System.currentTimeMillis() + frameStart;
			    final long sleepTime = frameLengthNano - System.nanoTime() + frameStartNano;
			    //System.out.println(sleepTime);
			    if (sleepTime > 0) {
			        //Thread.sleep(sleepTime);
			        final long sleepTimeMillis = sleepTime / 1000000;
			        Thread.sleep(sleepTimeMillis, (int) (sleepTime - sleepTimeMillis * 1000000));
			    }
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }
		}
		Display.destroy();
		Controllers.destroy();
	}
	
	private final void activate(final Panput input, final boolean active) {
		if (input == null) {
			return;
		} else if (active) {
			activate(input);
		} else {
			deactivate(input);
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
	}
	
	private final void deactivate(final Panput input) {
		for (final ActionEndListener endListener : Coltil.unnull(interaction.getEndListeners(input))) {
			//endListener.onActionEnd(ActionEndEvent.INSTANCE);
		    if (!isActive(getActor(endListener))) {
                continue;
            }
		    endListener.onActionEnd(ActionEndEvent.getEvent(input));
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
		final float wz = w / zoomMag, hz = h / zoomMag;
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
			//GL11.glOrtho(x - w2, x + w2, y - h2, y + h2, near, far);
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
		GL11.glMatrixMode(GL11.GL_PROJECTION); // Select The Projection Matrix
		GL11.glLoadIdentity(); // Reset The Projection Matrix
		//GLU.gluOrtho2D(-w / 2, w / 2, -h / 2, h / 2);
		//GLU.gluOrtho2D(0, w, 0, h);
		cam(layer);
		//GL11.glOrtho(0, w, 0, h, -maxDimension, maxDimension);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);		
	}
	
	private final static int formatCam(final float c) {
	    return Math.round(c);
	}
	
	private void cam(final Panlayer layer) {
		final Camera c = cams.get(layer);
		final float xi = c.xi, xa = c.xa, yi = c.yi, ya = c.ya, zi = c.zi, za = c.za;
		GL11.glOrtho(xi, xa, yi, ya, zi, za);
		//GL11.glOrtho((int) xi, (int) xa, (int) yi, (int) ya, zi, za);
		// Formatted in Camera constructor
		//GL11.glOrtho(formatCam(xi), formatCam(xa), formatCam(yi), formatCam(ya), zi, za);
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
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		final Panroom room = Pangame.getGame().getCurrentRoom();
		cams.clear();
		for (Panlayer layer = room.getBase(); layer != null; layer = layer.getAbove()) {
			initCamera(layer);
		}
		for (Panlayer layer = room.getBase(); layer != null; layer = layer.getAbove()) {
		    draw(layer);
		}
		if (screenShotDst != null) {
		    final ByteBuffer buf = Pantil.allocateDirectByteBuffer(w * h * 3);
		    //buf.rewind();
		    GL11.glReadPixels(0, 0, w, h, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, buf); // Could read each frame and filter, but very slow
		    Imtil.save(Imtil.create(buf, w, h, BufferedImage.TYPE_INT_RGB), screenShotDst);
		    screenShotDst = null;
		}
		Display.update();
	}
	
	private final void draw(final Panlayer room) {
	    final boolean visible = room.isVisible();
	    if (room.isClearDepthEnabled()) {
	        if (!visible) {
	            throw new UnsupportedOperationException("Don't clear depth if not visible");
	        }
//if (room instanceof Panroom) {
	        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
//}
	    }
	    if (!visible) {
	        return;
	    }
		camera(room); // Must be after step() for tracking to work right
		for (final LwjglPanmage image : images) {
            image.clearAll();
        }
		if (room != null) {
			final Collection<Panctor> actors = room.getActors();
			if (actors != null) {
				for (final Panctor actor : actors) {
					renderView(actor);
				}
			}
		}
		for (final LwjglPanmage image : images) {
		    /*try { // Try to see if double buffering is enabled
                Thread.sleep(30);
            } catch (final InterruptedException e) {
                throw new RuntimeException(e);
            }*/
		    image.renderAll(room);
		}
		blend(room);
		//Display.update();
	}
	
	private final byte toByte(final short c) {
	    return (byte) (c + Byte.MIN_VALUE);
	}
	
	private void blend(final Panlayer room) {
	    // opengl.org - the depth buffer is not updated if the depth test is disabled
	    final Pancolor color = room.getBlendColor();
	    final short a = color.getA();
	    if (a == 0) {
	        return;
	    }
	    GL11.glLoadIdentity();
	    GL11.glDisable(GL11.GL_DEPTH_TEST);
	    GL11.glDisable(GL11.GL_TEXTURE_2D);
	    GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
	    GL11.glEnable(GL11.GL_BLEND);
	    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4b(toByte(color.getR()), toByte(color.getG()), toByte(color.getB()), toByte(a));
        //final int maxx = 256, maxy = 192;
        final Camera c = cams.get(room);
		final float minx = c.xi, maxx = c.xa, miny = c.yi, maxy = c.ya, z = c.za;
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
        blendRectangle.rewind();
        //GL11.glDrawElements(GL11.GL_QUADS, blendRectangle); array of indices into other arrays
        GL11.glVertexPointer(3, 0, blendRectangle);
        GL11.glDrawArrays(GL11.GL_QUADS, 0, 4); // Number of vertices
        GL11.glColor4b(Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
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
	public final void setTitle(final String title) {
		Display.setTitle(title);
	}
	
	@Override
	public final void setIcon(final String... locations) {
		final int size = locations.length;
		final ByteBuffer[] buffers = new ByteBuffer[size];
		for (int i = 0; i < size; i++) {
			final BufferedImage image = Imtil.load(locations[i]);
			//final ColorModel m = image.getColorModel(); // getRed throws Exception
			final ColorModel m = ColorModel.getRGBdefault();
			final int w = image.getWidth();
			final int h = image.getHeight();
			final ByteBuffer buffer = ByteBuffer.allocateDirect(w * h * 4);
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					final int p = image.getRGB(x, y);
					buffer.put((byte) m.getRed(p));
					buffer.put((byte) m.getGreen(p));
					buffer.put((byte) m.getBlue(p));
					buffer.put((byte) m.getAlpha(p));
				}
			}
			buffer.rewind();
			buffers[i] = buffer;
		}
		Display.setIcon(buffers);
	}
	
	@Override
	public final void setBgColor(final Pancolor color) {
	    this.clr = color.getRf();
	    this.clg = color.getGf();
	    this.clb = color.getBf();
	    this.cla = color.getAf();
	    setBgColor();
	}
	
	private final void setBgColor() {
	    GL11.glClearColor(clr, clg, clb, cla);
	}

	@Override
	public final void exit() {
		running = false;
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        getMusic().close();
	}
}
