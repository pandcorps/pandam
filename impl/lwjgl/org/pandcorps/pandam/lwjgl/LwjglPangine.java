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

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.nio.ByteBuffer;
import java.util.*;
//import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
//import org.lwjgl.util.glu.GLU;
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
    public final int getScreenWidth() {
        return Display.getDesktopDisplayMode().getWidth();
    }
    
    @Override
    public final int getScreenHeight() {
        return Display.getDesktopDisplayMode().getHeight();
    }

	private int w = 640, h = 480;
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
	//protected final void start() throws Exception {
	protected final void init() throws Exception {
	    initialized = true;
		Display.setDisplayMode(new DisplayMode(w, h));
		/*
		Display.setFullscreen(true);
		org.lwjgl.util.Display.getAvailableDisplayModes
		org.lwjgl.util.Display.setDisplayMode
		*/
		Display.create();

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

		//new Thread(new Runnable() {public void run() {play();}}).start();
	}

	private boolean running = true;

	//private final void play() {
	@Override
	protected final void start() throws Exception {
		//int f = 0;
		while (running) {
			try {
				Thread.sleep(30);
			} catch (final InterruptedException e) {
				throw new RuntimeException(e);
			}
			//System.out.println(System.currentTimeMillis());
			Keyboard.poll();
			//Keyboard.enableRepeatEvents(true);
			Keyboard.enableRepeatEvents(false);
			//org.lwjgl.input.
			/*
			System.out.println("REE " + Keyboard.areRepeatEventsEnabled());
			System.out.println("EK  " + Keyboard.getEventKey());
			System.out.println("EKS " + Keyboard.getEventKeyState());
			for (int i = 0; i < 10; i++) {
				System.out.println("N " + i + " " + Keyboard.next());
			}
			*/
			//f++;
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
				if (Keyboard.getEventKeyState()) {
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
					for (final ActionStartListener startListener : Coltil.unnull(interaction.getStartListeners(key))) {
						//startListener.onActionStart(ActionStartEvent.INSTANCE);
					    //startListener.onActionStart(ActionStartEvent.getEvent(key, Character.valueOf(Keyboard.getEventCharacter())));
					    startListener.onActionStart(ActionStartEvent.getEvent(key));
					}
					/*final Panction action = interaction.getAction(key);
					if (action != null) {
						setActive(action, true);
					}*/
					onAction(key);
					newActive.add(key);
					setActive(key, true);
				}
				else {
					for (final ActionEndListener endListener : Coltil.unnull(interaction.getEndListeners(key))) {
						//endListener.onActionEnd(ActionEndEvent.INSTANCE);
					    endListener.onActionEnd(ActionEndEvent.getEvent(key));
					}
					/*
					TODO
					If a key is pressed and released during the same frame,
					then action.isActive() will return false for that frame.
					This is different than an ActionListener,
					which would still fire its event for that one frame.
					*/
					/*final Panction action = interaction.getAction(key);
					if (action != null) {
						setActive(action, false);
					}*/
					active.remove(key);
					newActive.remove(key);
					setActive(key, false);
				}
				//System.out.println("EK " + f + " " + Keyboard.getEventKey() + Keyboard.getEventKeyState());
			}
			for (final Panput input : active) {
				onAction(input);
			}
			active.addAll(newActive);
			newActive.clear();
			step();
			draw();
		}
		Display.destroy();
	}
	
	private final static int near = -1000, far = 1000;
	
	private final float[] cr = new float[2];

	private final void camera(final Panlayer layer) {
//if (layer.getClass() != Panroom.class) return;
		GL11.glMatrixMode(GL11.GL_PROJECTION); // Select The Projection Matrix
		GL11.glLoadIdentity(); // Reset The Projection Matrix
		//GLU.gluOrtho2D(-w / 2, w / 2, -h / 2, h / 2);
		//GLU.gluOrtho2D(0, w, 0, h);
		final Panctor tracked = layer.getTracked();
		final float zoomMag = getZoom();
		final float wz = w / zoomMag, hz = h / zoomMag;
		if (tracked == null) {
			//GL11.glOrtho(0, w, 0, h, near, far);
		    GL11.glOrtho(0, wz, 0, hz, near, far);
		}
		else {
			final Panple pos = tracked.getPosition();
			final int x = Math.round(pos.getX());
			final int y = Math.round(pos.getY());
			//final int w2 = w / 2, h2 = h / 2;
			//final int w2 = w / 4, h2 = h / 4;
			//final float w2 = wz / 2, h2 = hz / 2;
			// Does this work if w or h is odd?
			//GL11.glOrtho(x - w2, x + w2, y - h2, y + h2, near, far);
			final Panple lsize = layer.getSize();
			checkCamRange(x, wz, lsize.getX());
			final float xc1 = cr[0], xc2 = cr[1];
			checkCamRange(y, hz, lsize.getY());
			final float yc1 = cr[0], yc2 = cr[1];
			GL11.glOrtho(xc1, xc2, yc1, yc2, near, far);
		}
		//GL11.glOrtho(0, w, 0, h, -maxDimension, maxDimension);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);		
	}
	
	private final void checkCamRange(final float p, final float sz, final float sl) {
	    final float s2 = sz / 2;
	    float pc1 = p - s2, pc2;
        if (pc1 < 0) {
            pc1 = 0;
            pc2 = sz;
        } else {
            pc2 = p + s2;
            if (pc2 > sl) {
                pc1 = sl - sz;
                pc2 = sl;
            }
        }
        cr[0] = pc1;
        cr[1] = pc2;
	}

	private final void draw() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		final Panroom room = Pangame.getGame().getCurrentRoom();
		for (Panlayer layer = room.getBase(); layer != null; layer = layer.getAbove()) {
		    draw(layer);
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
		//Display.update();
	}

	private final void onAction(final Panput input) {
		for (final ActionListener listener : Coltil.unnull(interaction.getListeners(input))) {
			//listener.onAction(ActionEvent.INSTANCE);
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
	}
}
