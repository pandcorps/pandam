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
package org.pandcorps.pandam;

import org.pandcorps.core.*;
import org.pandcorps.pandam.Panteraction.*;
import org.pandcorps.pandax.text.*;

// Pandam Input
public abstract class Panput {
	public final static byte TOUCH_DOWN = 0;
	public final static byte TOUCH_UP = 1;
	public final static byte TOUCH_MOVE = 2;
	
    /*package*/ final static Any any = new Any();
    /*package*/ Device device;
    private final String name;
    /*package*/ boolean active = false;
    /*package*/ boolean inactivated = false;
    
    protected Panput(final Device device, final String name) {
    	this.device = device;
    	this.name = name;
    }
    
    public final Device getDevice() {
    	return device;
    }
    
    public final String getName() {
    	return name;
    }
    
    @Override
	public String toString() {
		return name;
	}
    
    // We allow event-based input and polling
    //TODO Do we need this and Panction?
    public final boolean isActive() {
        return active && !inactivated;
    }
    
    public final boolean isEnded() {
        return Pangine.getEngine().isEnded(this);
    }
    
    public final boolean isActive(final boolean endListener) {
        return endListener ? isEnded() : isActive();
    }
    
    //TODO Support inactivate in Listeners and Panction
    public final void inactivate() {
        // I think inactivated is cleared during the key release event;
        // I don't think anything checks after every frame to clear this if it's not pressed.
        // So I think we need to check active before setting inactivated.
        if (active) {
            inactivated = true;
        }
    }
    
    public final static void inactivate(final Panput... inputs) {
        for (final Panput input : inputs) {
        	if (input != null) {
        		input.inactivate();
        	}
        }
    }
    
	public final static class Key extends Panput {
	    private final Panteraction interaction;
		private final int index;
		private final Character base;
		private final Character shift;
		private final boolean letter;
		/*package*/ Key(final Panteraction interaction, final int index, final Character base, final Character shift, final boolean letter, final String s) {
			super(interaction.KEYBOARD, s == null ? Chartil.toString(base) : s);
			/*if (getName() == null) {
				System.err.println("No name for key " + index);
			}*/
		    this.interaction = interaction;
			this.index = index;
			this.base = base;
			this.shift = shift;
			this.letter = letter;
		}
		public final int getIndex() {
			return index;
		}
		public final Character getBaseCharacter() {
		    return base;
		}
		public final Character getCurrentCharacter() {
            return interaction.isShiftActive() ^ (letter && interaction.isCapsLockEnabled())? shift : base;
        }
		public final boolean isLetter() {
		    return letter;
		}
		@Override
		public final int hashCode() {
			return index;
		}
		// We shouldn't allow two instances of same key, so this is unnecessary work
		//@Override
		//public final boolean equals(final Object o) {
		//	return o instanceof Key ? ((Key) o).index == index : false;
		//}
	}
	
	public final static class Button extends Panput {
		/*package*/ Button(final String name) {
			super(null, name); // Buttons are created before Controller and passed to Controller constructor which assigns device
		}
	}
	
	public final static class Touch extends Panput {
		public Touch(final Panteraction interaction) {
			super(interaction.TOUCHSCREEN, "Touch");
		}
	}
	
	public static class TouchButton extends Panput {
	    // If user touches area overlapped by multiple TouchButtons...
	    public final static byte OVERLAP_ANY = 0; // Choose any TouchButton
	    public final static byte OVERLAP_BEST = 1; // Choose closest TouchButton
	    //public final static byte OVERLAP_ALL = 2; // Activate all TouchButtons
		private int xMin;
		private int yMin;
		private int xMax;
		private int yMax;
		private Panctor actor = null;
		private Panmage imgActive = null;
		private Panmage imgInactive = null;
		private Panmage imgDisabled = null;
		private Panctor actorOverlay = null;
		private Pantext text = null;
		private Panlayer layer = null;
		private byte overlapMode = OVERLAP_ANY;
		private boolean enabled = true;
		private Panput mappedInput = null;
		private final boolean moveCancel;
		
		public TouchButton(final Panteraction interaction, final String name, final int x, final int y, final int w, final int h) {
		    this(interaction, name, x, y, w, h, false);
		}
		
		public TouchButton(final Panteraction interaction, final String name, final int x, final int y, final int w, final int h, final boolean moveCancel) {
			super(interaction.TOUCHSCREEN, name);
			xMin = x;
			yMin = y;
			xMax = x + w;
			yMax = y + h;
			this.moveCancel = moveCancel;
		}
		
		public TouchButton(final Panteraction interaction, final Panlayer layer, final String name, final int x, final int y, final float z, final Panmage img, final Panmage imgActive) {
		    this(interaction, layer, name, x, y, z, img, imgActive, false);
		}
		
		public TouchButton(final Panteraction interaction, final Panlayer layer, final String name, final int x, final int y, final float z,
		                   final Panmage img, final Panmage imgActive, final boolean moveCancel) {
		    this(interaction, layer, name, x, y, z, img, imgActive, null, 0, 0, null, null, 0, 0, moveCancel);
		}
		
		public TouchButton(final Panteraction interaction, final Panlayer layer, final String name, final int x, final int y, final float z,
                           final Panmage img, final Panmage imgActive, final Panmage imgOverlay, final int xOverlay, final int yOverlay,
                           final MultiFont fonts, final CharSequence txt, final int xText, final int yText, final boolean moveCancel) {
		    this(interaction, name, x, y, (int) img.getSize().getX(), (int) img.getSize().getY(), moveCancel);
		    initActor(layer, z, img, imgActive, imgOverlay, xOverlay, yOverlay, fonts, txt, xText, yText);
		}
		
		public final void setPosition(final int x, final int y) {
		    if (actor != null) {
		        final Panple pos = actor.getPosition();
		        // Trying to handle images offset from touch box; this isn't right, though
		        //pos.set(x + pos.getX() - xMin, y + pos.getY() - yMin);
		        pos.set(x, y);
		        setPosition(actorOverlay, x, y);
		        setPosition(text, x, y);
		    }
		    xMax = x + xMax - xMin;
		    yMax = y + yMax - yMin;
		    xMin = x;
		    yMin = y;
		}
		
		private final void setPosition(final Panctor a, final float x, final float y) {
		    if (a != null) {
		        //a.getPosition().set(x, y);
		        final Panple pos = a.getPosition();
		        pos.set(x + pos.getX() - xMin, y + pos.getY() - yMin);
		    }
		}
		
		public final void initActor(final Panlayer layer, final float z, final Panmage img, final Panmage imgActive) {
		    initActor(layer, z, img, imgActive, null, 0, 0, null, null, 0, 0);
		}
		
		public final void initActor(final Panlayer layer, final float z, final Panmage img, final Panmage imgActive,
		                            final Panmage imgOverlay, final int xOverlay, final int yOverlay,
		                            final MultiFont fonts, final CharSequence txt, final int xText, final int yText) {
		    setActor(addActor(layer, xMin, yMin, z, img), imgActive);
		    if (imgOverlay != null) {
		        actorOverlay = addActor(layer, xMin + xOverlay, yMin + yOverlay, z + 1, imgOverlay);
		    }
		    if (txt != null) {
		        text = new Pantext(Pantil.vmid(), fonts, txt);
		        text.getPosition().set(xMin + xText, yMin + yText, z + 2);
		        layer.addActor(text);
		    }
		}
		
		private final static Panctor addActor(final Panlayer layer, final float x, final float y, final float z, final Panmage img) {
		    final Panctor actor = new Panctor();
            actor.setView(img);
            actor.getPosition().set(x, y, z);
            layer.addActor(actor);
            return actor;
		}
		
		public final void setActor(final Panctor actor, final Panmage imgActive) {
			this.actor = actor;
			this.imgActive = imgActive;
			this.imgInactive = (Panmage) actor.getView();
			layer = actor.getLayer();
		}
		
		public final void setImageDisabled(final Panmage imgDisabled) {
			this.imgDisabled = imgDisabled;
		}
		
		public final Panctor getActor() {
		    return actor;
		}
		
		public final Panlayer getLayer() {
			return layer;
		}
		
		public final boolean isMoveInterpretedAsCancel() {
		    /*
		    If true, then touching a button and then moving elsewhere on the screen cancels the button touch.
		    There would have been an ActionStartEvent with no ActionEndEvent for the button.
		    If the touch is lifted on the second button, it would have an ActionEndEvent with no ActionStartEvent.
		    This is useful for menus where the user might see that the wrong button has been touched.
		    The user could then move to the correct button.
		    If the button responds to ActionEndEvent, then the first button would be canceled, ignored.
		    If false, then moving from one button to the other will conclude the first with an ActionEndEvent
		    and activate the second with an ActionStartEvent.
		    This is useful for movement arrow buttons where the user can change direction without lifting the touch.
		    */
		    return moveCancel;
		}
		
		public boolean contains(final int x, final int y) {
			return x >= xMin && x < xMax && y >= yMin && y < yMax;
		}
		
		public float getCenterX() {
		    return (xMin + xMax) / 2f;
		}
		
		public float getCenterY() {
		    return (yMin + yMax) / 2f;
        }
		
		public byte getOverlapMode() {
		    return overlapMode;
		}
		
		public void setOverlapMode(final byte overlapMode) {
		    this.overlapMode = overlapMode;
		}
		
		public boolean isEnabled() {
			return enabled;
		}
		
		public void setEnabled(final boolean enabled) {
			this.enabled = enabled;
			if (imgDisabled != null) {
				actor.setView(enabled ? imgInactive : imgDisabled);
			}
		}
		
		public Panput getMappedInput() {
		    return mappedInput;
		}
		
		public void setMappedInput(final Panput mappedInput) {
		    this.mappedInput = mappedInput;
		}
		
		public void activate(final boolean active) {
			if (enabled && actor != null && imgActive != null) {
				actor.setView(active ? imgActive : imgInactive);
			}
		}
		
		public final void detach() {
		    Pangine.getEngine().unregisterTouchButton(this);
		    Panctor.detach(actor);
		    Panctor.detach(actorOverlay);
		    Panctor.detach(text);
		}
		
		public final static void detach(final TouchButton button) {
			if (button != null) {
				button.detach();
			}
		}
		
		public final void reattach() {
			final Pangine engine = Pangine.getEngine();
			if (engine.isTouchButtonRegistered(this)) {
				return;
			}
		    engine.registerTouchButton(this);
		    if (layer != null) {
		        layer.addActor(actor);
		        if (actorOverlay != null) {
		            layer.addActor(actorOverlay);
		        }
		        if (text != null) {
		            layer.addActor(text);
		        }
		    }
		}
		
		public final static void reattach(final TouchButton button) {
			if (button != null) {
				button.reattach();
			}
		}
		
		@Override
		public final String toString() {
			return getName() + " (" + xMin + ", " + yMin + ") - (" + xMax + ", " + yMax + ")";
		}
		
		public final void destroy() {
		    detach();
		    Panctor.destroy(actor);
		    Panctor.destroy(actorOverlay);
		    Panctor.destroy(text);
		}
		
		public final static void destroy(final TouchButton button) {
		    if (button != null) {
		        button.destroy();
		    }
		}
	}
	
	public final static class TouchEvent {
		protected final int id;
		protected final byte type;
		protected final int x;
		protected final int y;
		
		public TouchEvent(final int id, final byte type, final int x, final int y) {
			this.id = id;
			this.type = type;
			this.x = x;
			this.y = y;
		}
		
		public final int getId() {
			return id;
		}
		
		public final byte getType() {
			return type;
		}
		
		public final int getX() {
			return x;
		}
		
		public final int getY() {
			return y;
		}
	}
	
	/*package*/ final static class Any extends Panput {
	    private Any() {
	    	super(null, "Any");
	    }
	}
}
