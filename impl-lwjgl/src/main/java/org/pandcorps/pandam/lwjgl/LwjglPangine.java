/*
Copyright (c) 2009-2021, Andrew M. Martin
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

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.nio.*;
import java.util.*;

import org.lwjgl.input.*;
import org.lwjgl.opengl.*;
import org.pandcorps.core.*;
import org.pandcorps.core.col.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.impl.*;

public final class LwjglPangine extends GlPangine {
	private static LwjglPangine engine = null;
	private static boolean mouseTouchEnabled = false;
	private final static float axisThreshold = 0.75f;

	private LwjglPangine() {
		super(new LwjglPanteraction());
		engine = this;
		gl = new LwjglPangl();
		final MultiPanaudio multiAudio = new MultiPanaudio();
		final Panaudio midAudio = new JavaxMidiPanaudio(), wavAudio = new LwjglWavPanaudio();
		multiAudio.addMapping("mid", midAudio);
		multiAudio.addMapping("wav", wavAudio);
		audio = multiAudio;
		Img.saver = new AwtImgSaver();
	}

	public final static LwjglPangine getEngine() {
		return engine == null ? new LwjglPangine() : engine;
	}

	@Override
    public final int getDesktopWidth() {
        return Display.getDesktopDisplayMode().getWidth();
    }
    
    @Override
    public final int getDesktopHeight() {
        return Display.getDesktopDisplayMode().getHeight();
    }
    
    @Override
    protected final boolean getDefaultFullScreeen() {
    	return false;
    }

    @Override
    protected final void initDisplay() throws Exception {
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
	        //Mouse.setCursorPosition(0, 0);
	    }
	}
	
    @Override
	protected void initInput() throws Exception {
        Mouse.create();
        final int ms = Mouse.getButtonCount();
        for (int i = 0; i < ms; i++) {
            addMouseButton(Mouse.getButtonName(i), i);
        }
        
		Controllers.create();
		final int cs = Controllers.getControllerCount();
		for (int i = 0; i < cs; i++) {
			final Controller c = Controllers.getController(i);
			final int bs = c.getButtonCount();
			final List<Button> blist = new ArrayList<Button>(bs);
			for (int j = 0; j < bs; j++) {
				blist.add(newButton(c.getButtonName(j)));
			}
			final int as = c.getAxisCount();
			for (int j = 0; j < as; j++) {
			    final String name = c.getAxisName(j);
			    if (isAxisBad(name) || isAxisX(name) || isAxisY(name)) {
			        continue;
			    }
			    blist.add(newButton(name + "-"));
			    blist.add(newButton(name + "+"));
			}
			addController(c.getName(), newButton("Left"), newButton("Right"), newButton("Up"), newButton("Down"), blist);
		}
	}
    
    private final boolean isAxisBad(final String name) {
        return Chartil.isEmpty(name);
    }
    
    private final boolean isAxisX(final String name) {
        return (name != null) && (name.startsWith("X") || name.equals("rx"));
    }
    
    private final boolean isAxisY(final String name) {
        return (name != null) && (name.startsWith("Y") || name.equals("ry"));
    }

    @Override
	protected void stepControl() throws Exception {
		Keyboard.poll();
		Keyboard.enableRepeatEvents(false);
		/*
		info("REE " + Keyboard.areRepeatEventsEnabled());
		info("EK  " + Keyboard.getEventKey());
		info("EKS " + Keyboard.getEventKeyState());
		for (int i = 0; i < 10; i++) {
			info("N " + i + " " + Keyboard.next());
		}
		*/
		/*
		while (Keyboard.next()) {
			info("EK " + f + " " + Keyboard.getEventKey() + Keyboard.getEventKeyState());
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
			//info(key.getIndex());
			//info(Keyboard.getEventCharacter());
			//info(Keyboard.getKeyName(key.getIndex()));
			//info(java.awt.AWTKeyStroke.getAWTKeyStroke(key.getIndex(), 0).getKeyChar()); // Always '?'
			//info(java.awt.AWTKeyStroke.getAWTKeyStroke(key.getIndex(), java.awt.event.InputEvent.BUTTON1_DOWN_MASK).getKeyChar()); // Always '?'
			//info(javax.swing.KeyStroke.getKeyStroke(key.getIndex(), java.awt.event.InputEvent.BUTTON1_DOWN_MASK).getKeyChar()); // Always '?'
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
			//info("EK " + f + " " + Keyboard.getEventKey() + Keyboard.getEventKeyState());
		}
		
		Controllers.poll();
		while (Controllers.next()) {
			final Button button;
			final boolean a;
			final Controller src = Controllers.getEventSource();
			final Panteraction.Controller pc = interaction.CONTROLLERS.get(src.getIndex());
			/*info("Name: " + src.getName()
					+ "; AxisCount: " + src.getAxisCount() + "; ButtonCount: " + src.getButtonCount());
			info("Axis: " + Controllers.isEventAxis()
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
				    final int size = src.getAxisCount();
				    boolean foundX = false, foundY = false;
				    for (int i = 0; i < size; i++) {
				        final String name = src.getAxisName(i);
                        if (isAxisBad(name)) {
                            continue;
                        }
                        final float value = src.getAxisValue(i);
                        if (isAxisX(name)) {
                            if (foundX) {
                                continue;
                            }
                            foundX = true;
                            handleUnspecifiedAxis(value, pc.RIGHT, pc.LEFT);
                            if (foundY) {
                                continue;
                            }
                        } else if (isAxisY(name)) {
                            if (foundY) {
                                continue;
                            }
                            foundY = true;
                            handleUnspecifiedAxis(value, pc.DOWN, pc.UP);
                            if (foundX) {
                                continue;
                            }
                        } else {
                            final Button n = pc.getButton(name + "+"), p = pc.getButton(name + "-");
                            if ((p != null) && (n != null)) {
                                handleUnspecifiedAxis(value, p, n);
                            }
                        }
				    }
					continue;
				}
				if (val > 0) {
					button = pos;
					// Check for immediate direction change without releasing axis
					activate(ifActive(neg), false);
					a = true;
				} else if (val < 0) {
					button = neg;
					activate(ifActive(pos), false);
					a = true;
				} else {
					button = Pantil.nvl(Coltil.has(active, pos), Coltil.has(active, neg), Coltil.has(newActive, pos), Coltil.has(newActive, neg));
					a = false;
				}
			}
			activate(button, a);
		}
		Controllers.clearEvents();
		
		Mouse.poll();
		while (Mouse.next()) {
			if (!mouseTouchEnabled) {
				continue;
			}
			final int btn = Mouse.getEventButton();
			final byte type;
			if (btn == -1) {
				if (Mouse.isButtonDown(0)) {
					type = Panput.TOUCH_MOVE;
				} else {
					continue;
				}
			} else if (btn == 0) {
				type = Mouse.getEventButtonState() ? Panput.TOUCH_DOWN : Panput.TOUCH_UP;
			} else {
				continue;
			}
			addTouchEvent(0, type, Mouse.getEventX(), Mouse.getEventY());
		}
	}
    
    private final static Set<Button> activatedUnspecifiedAxis = new IdentityHashSet<Button>();
    
    private final void handleUnspecifiedAxis(final float val, final Button pos, final Button neg) {
        if (val > axisThreshold) {
            // Check for immediate direction change without releasing axis
            activate(ifActive(neg), false);
            activate(ifInactive(pos), true);
            activatedUnspecifiedAxis.add(pos);
        } else if (val < -axisThreshold) {
            activate(ifActive(pos), false);
            activate(ifInactive(neg), true);
            activatedUnspecifiedAxis.add(pos);
        } else if (activatedUnspecifiedAxis.remove(pos)) {
            activate(ifActive(pos), false);
            activate(ifActive(neg), false);
        }
    }
    
    private final Button ifActive(final Button btn) {
        return Pantil.nvl(Coltil.has(active, btn), Coltil.has(newActive, btn));
    }
    
    private final Button ifInactive(final Button btn) {
        return (active.contains(btn) || newActive.contains(btn)) ? null : btn;
    }
    
    @Override
    public final int getMouseX() {
    	return getEffectiveCoordinateX(Mouse.getX());
    }
    
    @Override
    public final int getMouseY() {
    	return getEffectiveCoordinateY(Mouse.getY());
    }
    
    @Override
    public final boolean isMouseSupported() {
		return true;
	}
    
    @Override
    public final void setMouseTouchEnabled(final boolean mouseTouchEnabled) {
    	LwjglPangine.mouseTouchEnabled = mouseTouchEnabled;
    }
    
    @Override
    public final boolean isTouchSupported() {
    	return mouseTouchEnabled;
    }
    
    @Override
	protected void onDestroy() {
		Display.destroy();
		Mouse.destroy();
		Controllers.destroy();
	}
	
    @Override
	protected void update() {
		Display.update();
	}
    
    private final static class LwjglOwner implements ClipboardOwner {
        @Override
        public final void lostOwnership(final Clipboard clipboard, final Transferable contents) {
        }
    }
    
    private final LwjglOwner clipboardOwner = new LwjglOwner();
    
    @Override
    public final void getClipboard(final Handler<String> handler) {
        //return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        final Transferable value = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(clipboardOwner);
        final String clip;
        if (value == null) {
            clip = null;
        } else if (value.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                clip = (String) value.getTransferData(DataFlavor.stringFlavor);
            } catch (final Exception e) {
                throw Pantil.toRuntimeException(e);
            }
        } else {
        	clip = value.toString();
        }
        handler.handle(clip);
    }
    
    @Override
    public final void setClipboard(final String value) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(value), clipboardOwner);
    }
	
	@Override
	public final void setTitle(final String title) {
		Display.setTitle(title);
	}
	
	@Override
	public final void setIcon(final String... locations) {
		final int size = locations.length;
		final ByteBuffer[] buffers = new ByteBuffer[size];
		for (int i = 0; i < size; i++) {
			final Img image = Imtil.load(locations[i]);
			//final ColorModel m = image.getColorModel(); // getRed throws Exception
			final ImgFactory m = ImgFactory.getFactory();
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
			image.close();
		}
		Display.setIcon(buffers);
	}
}
