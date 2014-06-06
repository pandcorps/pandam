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

import java.util.*;
import java.util.Map.*;

import org.pandcorps.core.*;
import org.pandcorps.core.col.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.event.action.*;

// Pandam Interaction
public abstract class Panteraction {
	private final Key[] keys;
	private final HashMultimap<Panput, ActionStartListener> startListeners = new HashMultimap<Panput, ActionStartListener>();
	private final HashMultimap<Panput, ActionListener> listeners = new HashMultimap<Panput, ActionListener>();
	private final HashMultimap<Panput, ActionEndListener> endListeners = new HashMultimap<Panput, ActionEndListener>();
	//private final HashMultimap<Panput, Panction> actions = new HashMultimap<Panput, Panction>();
	public final Key KEY_ESCAPE;
	public final Key KEY_1;
	public final Key KEY_2;
	public final Key KEY_3;
	public final Key KEY_4;
	public final Key KEY_5;
	public final Key KEY_6;
	public final Key KEY_7;
	public final Key KEY_8;
	public final Key KEY_9;
	public final Key KEY_0;
	public final Key KEY_MINUS;
	public final Key KEY_EQUALS;
	public final Key KEY_BACKSPACE;
	public final Key KEY_TAB;
	public final Key KEY_Q;
	public final Key KEY_W;
	public final Key KEY_E;
	public final Key KEY_R;
	public final Key KEY_T;
	public final Key KEY_Y;
	public final Key KEY_BRACKET_LEFT;
	public final Key KEY_BRACKET_RIGHT;
	public final Key KEY_ENTER;
	public final Key KEY_CTRL_LEFT;
	public final Key KEY_A;
	public final Key KEY_S;
	public final Key KEY_D;
	public final Key KEY_F;
	public final Key KEY_SEMICOLON;
	public final Key KEY_APOSTROPHE;
	public final Key KEY_GRAVE; // tilde w/ shift
	public final Key KEY_SHIFT_LEFT;
	public final Key KEY_BACKSLASH;
	public final Key KEY_Z;
	public final Key KEY_COMMA;
	public final Key KEY_PERIOD;
	public final Key KEY_SLASH;
	public final Key KEY_SHIFT_RIGHT;
	public final Key KEY_ALT_LEFT;
	public final Key KEY_SPACE;
	public final Key KEY_CAPS_LOCK;
	public final Key KEY_F1;
	public final Key KEY_F2;
	public final Key KEY_F3;
	public final Key KEY_CTRL_RIGHT;
	public final Key KEY_ALT_RIGHT;
	public final Key KEY_HOME;
	public final Key KEY_UP;
	public final Key KEY_PG_UP;
	public final Key KEY_LEFT;
	public final Key KEY_RIGHT;
	public final Key KEY_END;
	public final Key KEY_DOWN;
	public final Key KEY_PG_DN;
	public final Key KEY_INS;
	public final Key KEY_DEL;
	
	public final Touch TOUCH;
	
	public abstract static class Device {
		private final String name;
		
		protected Device(final String name) {
			this.name = name;
		}
		
		public final String getName() {
			return name;
		}
	}
	
	public final static class Keyboard extends Device {
		private Keyboard() {
			super("Keyboard");
		}
	}
	
	public final Keyboard KEYBOARD = new Keyboard(); //TODO Should device constants be null when absent?
	
	public final static class Controller extends Device {
		public final Button LEFT;
		public final Button RIGHT;
		public final Button UP;
		public final Button DOWN;
		public final List<Button> BUTTONS;
		public final Button BUTTON_0;
		public final Button BUTTON_1; // For convenience
		
		protected Controller(final String name, final Button l, final Button r, final Button u, final Button d, final List<Button> bs) {
			super(name);
			l.device = this;
			r.device = this;
			u.device = this;
			d.device = this;
			for (final Button b : bs) {
				b.device = this;
			}
			LEFT = l;
			RIGHT = r;
			UP = u;
			DOWN = d;
			BUTTONS = Collections.unmodifiableList(bs);
			BUTTON_0 = Coltil.get(BUTTONS, 0);
			BUTTON_1 = Coltil.get(BUTTONS, 1);
		}
	}
	
	protected final List<Controller> _controllers = new ArrayList<Controller>();
	public final List<Controller> CONTROLLERS = Collections.unmodifiableList(_controllers);
	
	public final static class Touchscreen extends Device {
		private Touchscreen() {
			super("Touchscreen");
		}
	}
	
	public final Touchscreen TOUCHSCREEN = new Touchscreen();
	
	public final int IND_ESCAPE = 1;
	public final int IND_1 = 2;
	public final int IND_BACKSPACE = 14;
	public final int IND_TAB = 15;
	public final int IND_Q = 16;
	public final int IND_ENTER = 28;
	public final int IND_CTRL_LEFT = 29;
	public final int IND_A = 30;
	public final int IND_GRAVE = 41;
	public final int IND_SHIFT_LEFT = 42;
	public final int IND_Z = 44;
    public final int IND_SHIFT_RIGHT = 54;
    public final int IND_ALT_LEFT = 56;
    public final int IND_SPACE = 57;
    public final int IND_CAPS_LOCK = 58;
    public final int IND_F1 = 59;
    public final int IND_CTRL_RIGHT = 157;
    public final int IND_ALT_RIGHT = 184;
    public final int IND_HOME = 199;
    public final int IND_UP = 200;
    public final int IND_PG_UP = 201;
    public final int IND_LEFT = 203;
    public final int IND_RIGHT = 205;
    public final int IND_END = 207;
    public final int IND_DOWN = 208;
    public final int IND_PG_DN = 209;
    public final int IND_INS = 210;
    public final int IND_DEL = 211;
    
    private final IdentityHashMap<Panctor, ActionGroup> actors = new IdentityHashMap<Panctor, ActionGroup>();

	public Panteraction() {
		//final int size = 209;//getKeyCount();
	    final int size = 256;//del=211
		keys = new Key[size];
		final char[][][] kb = {
		    {{'`'}, // Grave/tilde is on same row of keyboard as numbers, but its index is different
		    {'~'}},
		    {{'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '-', '='},
		    {'!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+'}},
		    {{'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', '[', ']'},
		    {'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', '{', '}'}},
		    {{'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', ';', '\''},
		    {'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', ':', '"'}},
		    {{'z', 'x', 'c', 'v', 'b', 'n', 'm', ',', '.', '/'},
		    {'Z', 'X', 'C', 'V', 'B', 'N', 'M', '<', '>', '?'}},
		    {{' '},
		    {' '}},
		    {{'\\'},
		    {'|'}}
		    };
		final int indBackslash = getIndexBackslash();
        final int[] offs = {IND_GRAVE, IND_1, IND_Q, IND_A, IND_Z, IND_SPACE, indBackslash};
		for (int i = 0; i < size; i++) {
		    /*final char b, s;
		    final boolean l;
		    final int j;*/
		    char[][] thePair = null;
		    int theOff = 0;
		    for (int row = 0; row < kb.length; row++) {
		        final int off = offs[row];
		        final char[][] pair = kb[row];
    		    if (i >= off && i < off + pair[0].length) {
    		        thePair = pair;
    		        theOff = off;
    		        break;
    		    }
		    }
		    final Character bc, sc;
		    final boolean l;
		    if (thePair == null) {
		        bc = null;
		        sc = null;
		        l = false;
		    } else {
		        final int ind = i - theOff;
		        final char b = thePair[0][ind];
		        bc = Character.valueOf(b);
		        sc = Character.valueOf(thePair[1][ind]);
		        l = Character.isLetter(b);
		    }
			keys[i] = new Key(this, i, bc, sc, l, getName(i));
		}
		KEY_ESCAPE = keys[IND_ESCAPE];
		KEY_1 = keys[IND_1];
		KEY_2 = keys[IND_1 + 1];
		KEY_3 = keys[IND_1 + 2];
		KEY_4 = keys[IND_1 + 3];
		KEY_5 = keys[IND_1 + 4];
		KEY_6 = keys[IND_1 + 5];
		KEY_7 = keys[IND_1 + 6];
		KEY_8 = keys[IND_1 + 7];
		KEY_9 = keys[IND_1 + 8];
		KEY_0 = keys[IND_1 + 9];
		KEY_MINUS = keys[12];
		KEY_EQUALS = keys[13];
		KEY_BACKSPACE = keys[IND_BACKSPACE];
        KEY_TAB = keys[IND_TAB];
        KEY_Q = keys[IND_Q];
        KEY_W = keys[IND_Q + 1];
        KEY_E = keys[IND_Q + 2];
        KEY_R = keys[IND_Q + 3];
        KEY_T = keys[IND_Q + 4];
        KEY_Y = keys[IND_Q + 5];
        KEY_BRACKET_LEFT = keys[26];
        KEY_BRACKET_RIGHT = keys[27];
		KEY_ENTER = keys[IND_ENTER];
        KEY_CTRL_LEFT = keys[IND_CTRL_LEFT];
        KEY_A = keys[IND_A];
        KEY_S = keys[IND_A + 1];
        KEY_D = keys[IND_A + 2];
        KEY_F = keys[IND_A + 3];
        KEY_SEMICOLON = keys[39];
        KEY_APOSTROPHE = keys[40];
        KEY_GRAVE = keys[IND_GRAVE]; // tilde with shift
        KEY_SHIFT_LEFT = keys[IND_SHIFT_LEFT];
        KEY_BACKSLASH = keys[indBackslash];
        KEY_Z = keys[IND_Z];
        KEY_COMMA = keys[51];
        KEY_PERIOD = keys[52];
        KEY_SLASH = keys[53];
        KEY_SHIFT_RIGHT = keys[IND_SHIFT_RIGHT];
        KEY_ALT_LEFT = keys[IND_ALT_LEFT];
		KEY_SPACE = keys[IND_SPACE];
        KEY_CAPS_LOCK = keys[IND_CAPS_LOCK];
        KEY_F1 = keys[IND_F1];
        KEY_F2 = keys[IND_F1 + 1];
        KEY_F3 = keys[IND_F1 + 2];
        KEY_CTRL_RIGHT = keys[IND_CTRL_RIGHT];
        KEY_ALT_RIGHT = keys[IND_ALT_RIGHT];
        KEY_HOME = keys[IND_HOME];
		KEY_UP = keys[IND_UP];
        KEY_PG_UP = keys[IND_PG_UP];
		KEY_LEFT = keys[IND_LEFT];
		KEY_RIGHT = keys[IND_RIGHT];
		KEY_END = keys[IND_END];
		KEY_DOWN = keys[IND_DOWN];
		KEY_PG_DN = keys[IND_PG_DN];
		KEY_INS = keys[IND_INS];
		KEY_DEL = keys[IND_DEL];
		
		TOUCH = new Touch(this);
	}
	
	private final String getName(final int i) {
		switch(i) {
	    	case IND_ESCAPE : return "Escape";
	    	case IND_BACKSPACE : return "Backspace";
	    	case IND_TAB : return "Tab";
	    	case IND_ENTER : return "Enter";
	    	case IND_CTRL_LEFT : return "Ctrl-Left";
	    	case IND_SHIFT_LEFT : return "Shift-Left";
	    	case IND_SHIFT_RIGHT : return "Shift-Right";
	    	case IND_ALT_LEFT : return "Alt-Left";
	    	case IND_SPACE : return "Space";
	    	case IND_CAPS_LOCK : return "Caps-Lock";
	    	case IND_F1 : return "F1";
	    	case IND_F1 + 1 : return "F2";
	    	case IND_F1 + 2 : return "F3";
	    	case IND_F1 + 3 : return "F4";
	    	case IND_F1 + 4 : return "F5";
	    	case IND_F1 + 5 : return "F6";
	    	case IND_F1 + 6 : return "F7";
	    	case IND_F1 + 7 : return "F8";
	    	case IND_F1 + 8 : return "F9";
	    	case IND_CTRL_RIGHT : return "Ctrl-Right";
	    	case IND_ALT_RIGHT : return "Alt-Right";
	    	case IND_HOME : return "Home";
	    	case IND_UP : return "Up";
	    	case IND_PG_UP : return "Pg-Up";
	    	case IND_LEFT : return "Left";
	    	case IND_RIGHT : return "Right";
	    	case IND_END : return "End";
	    	case IND_DOWN : return "Down";
	    	case IND_PG_DN : return "Pg-Dn";
	    	case IND_INS : return "Ins";
	    	case IND_DEL : return "Del";
	    }
		return null;
	}
	
	public boolean isShiftActive() {
	    return KEY_SHIFT_LEFT.isActive() || KEY_SHIFT_RIGHT.isActive();
	}
	
	public boolean isCtrlActive() {
        return KEY_CTRL_LEFT.isActive() || KEY_CTRL_RIGHT.isActive();
    }
	
	public boolean isAltActive() {
        return KEY_ALT_LEFT.isActive() || KEY_ALT_RIGHT.isActive();
    }
	
	// Active means pressed
	// Could create key event, java.awt.Robot, KeyEvent.VK_A
	public abstract boolean isCapsLockEnabled();
	
	public abstract boolean isInsEnabled();

	public abstract int getKeyCount();
	
	protected abstract int getIndexBackslash();

	public final Key getKey(final int index) {
		return keys[index];
	}
	
	public final Key getKey(final char c) {
	    for (final Key key : keys) {
	        final Character base = key.getBaseCharacter();
	        if (base != null && base.charValue() == c) {
	            return key;
	        }
	    }
	    return null;
	}
	
	public final void inactivateAll() {
        Panput.inactivate(keys);
    }

	public final void register(final Panctor actor, final ActionStartListener listener) {
	    register(actor, Panput.any, listener);
	}
	
	public final void register(final Panctor actor, final Panput input, final ActionStartListener listener) {
		startListeners.add(input, listener);
		if (actor != null) {
			get(actor).add(listener);
		}
	}
	
	/*public final void push(final Panctor actor, final Panput input, final ActionStartListener listener) {
		startListeners.remove(input); // push somewhere
		register(actor, input, listener);
	}*/
	
	/*package*/ final ActionGroup get(final Panctor actor) {
		// Would be faster to store in Panctor, but would take more RAM for a mostly null extra field
		ActionGroup g = actors.get(actor);
		if (g == null) {
			g = new ActionGroup();
			actors.put(actor, g);
		}
		return g;
	}
	
	/*package*/ final Panctor getActor(final ActionListener listener) {
		for (final Entry<Panctor, ActionGroup> entry : actors.entrySet()) {
			if (Coltil.contains(entry.getValue().getListeners(), listener)) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	/*package*/ final Panctor getActor(final ActionStartListener listener) {
		for (final Entry<Panctor, ActionGroup> entry : actors.entrySet()) {
			if (Coltil.contains(entry.getValue().getStartListeners(), listener)) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	/*package*/ final Panctor getActor(final ActionEndListener listener) {
        for (final Entry<Panctor, ActionGroup> entry : actors.entrySet()) {
            if (Coltil.contains(entry.getValue().getEndListeners(), listener)) {
                return entry.getKey();
            }
        }
        return null;
    }
	
	/*package*/ final void unregister(final Panctor actor) {
		final ActionGroup g = actors.remove(actor);
		if (g != null) {
			g.unregister();
		}
	}

	public final Iterable<ActionStartListener> getStartListeners(final Panput input) {
		//return startListeners.get(input);
	    return input == Panput.any ? startListeners.get(input) : SequenceIterable.create(startListeners.get(input), startListeners.get(Panput.any));
	}
	
	public final void unregister(final ActionStartListener listener) {
	    unregister(startListeners, listener);
	}
	
	public final void unregisterAllStart(final Iterable<ActionStartListener> list) {
        unregister(startListeners, list);
    }
	
	private final static <T> void unregister(final HashMultimap<?, T> map, final Iterable<T> list) {
	    for (final T listener : Coltil.unnull(list)) {
	        unregister(map, listener);
	    }
	}
	
	private final static <T> void unregister(final HashMultimap<?, T> listeners, final T listener) {
	    for (final ArrayList<T> list : listeners.values()) {
    	    final Iterator<T> iter = list.iterator();
    	    while (iter.hasNext()) {
    	        if (listener == iter.next()) {
    	            iter.remove();
    	        }
    	    }
	    }
	}

	public final void register(final Panctor actor, final Panput input, final ActionListener listener) {
		listeners.add(input, listener);
		if (actor != null) {
			get(actor).add(listener);
		}
	}

	public final Iterable<ActionListener> getListeners(final Panput input) {
		return listeners.get(input);
	}
	
	public final void unregister(final ActionListener listener) {
        unregister(listeners, listener);
    }
	
	public final void unregisterAll(final Iterable<ActionListener> list) {
        unregister(listeners, list);
    }

	public final void register(final Panctor actor, final Panput input, final ActionEndListener listener) {
		endListeners.add(input, listener);
		if (actor != null) {
			get(actor).add(listener);
		}
	}

	public final Iterable<ActionEndListener> getEndListeners(final Panput input) {
		return endListeners.get(input);
	}
	
	public final void unregister(final ActionEndListener listener) {
        unregister(endListeners, listener);
    }
	
	public final void unregisterAllEnd(final Iterable<ActionEndListener> list) {
        unregister(endListeners, list);
    }
	
	public final void unregister(final Panput input) {
		unregisterAllStart(startListeners.get(input));
		unregisterAll(listeners.get(input));
		unregisterAllEnd(endListeners.get(input));
	}
	
	public final void unregisterAll() {
	    startListeners.clear();
	    listeners.clear();
	    endListeners.clear();
	}

	/*public final void register(final Panput input, final Panction action) {
		actions.add(input, action);
	}

	public final List<Panction> getActions(final Panput input) {
		return actions.get(input);
	}*/
}
