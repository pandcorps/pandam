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
package org.pandcorps.pandam;

import org.pandcorps.core.*;
import org.pandcorps.pandam.impl.*;

public abstract class Pangame {
	/*package*/ static volatile boolean initializingRoom = true;
	
	private static volatile Pangame game = null;

	private static volatile Panroom currentRoom = null;

	// Could imagine a game with two different modes,
	// like flying a ship and walking on a planet surface,
	// controlled by two different Pangames within a single session.
	// That could require a Pangame constructor
	// which accepts a parent Pangame parameter.
	public Pangame() {
		if (game != null) {
			throw new IllegalStateException("Creating instance of " + getClass() + " when already had instance of " + game.getClass());
		}
		game = this;
	}

	public static Pangame getGame() {
		return game;
	}
	
	// Should only be used by UnitPangine
	/*package*/ static void clearGame() {
	    game = null;
	}
	
	protected abstract FinPanple getFirstRoomSize();

	protected abstract void init(final Panroom room) throws Exception;
	
	protected boolean isClockRunning() {
        return true;
    }
	
	protected Panroom getFirstRoom() throws Exception {
	    /*
	    Games can override this and retrieve the first room however they want.
	    They can throw UnsupportedOperationExceptions in getFirstRoomSize and init if they don't want to use them.
	    Nothing else will.
	    This method allows init to call methods that use Pangame.getCurrentRoom().
	    If it's overridden, the game will need to be careful about that.
	    */
	    final Panroom room = Pangine.getEngine().createRoom(Pantil.vmid(), getFirstRoomSize());
	    setCurrentRoomIfNeeded(room);
        init(room);
        return room;
	}
	
	/*package*/ final void setCurrentRoomIfNeeded(final Panroom room) {
		if (currentRoom == null) {
	        currentRoom = room;
	    }
	}

	public Panroom getCurrentRoom() {
		if (currentRoom == null) {
			try {
				currentRoom = getFirstRoom();
			} catch (final Exception e) {
				throw Pantil.toRuntimeException(e);
			}
		}
		initializingRoom = false;
		return currentRoom;
	}
	
	public void setCurrentRoom(final Panroom room) {
		if (room == null) {
			throw new NullPointerException("Attempted to assign a null Panroom");
		}
		currentRoom = room;
	}

	public final void beforeLoop() throws Exception {
		final Pangine engine = Pangine.getEngine();
		initBeforeEngine();
		engine.init();
		engine.setIcon(Pantil.RES + "img/PandcorpsIcon32.png", Pantil.RES + "img/PandcorpsIcon16.png");
		init(); // Don't know why this happens after engine.init; can't set window size here; don't know what steps should happen here; adding initBeforeEngine
	}
	
	public final void recreate() throws Exception {
		Pangine.getEngine().recreate();
	}
	
	public final void start() throws Panception {
		final Pangine engine = Pangine.getEngine();
		try {
			beforeLoop();
			engine.loop();
		} catch (final Exception e) {
			fatal(engine);
			throw new Panception(e);
		} finally {
			destroy(engine);
		}
	}
	
	private final void fatal(final Pangine engine) {
		try {
			engine.exit();
		} catch (final Exception e) {
			//e.printStackTrace();
		}
	}
	
	private final void destroy(final Pangine engine) {
		try {
			engine.destroy();
		} catch (final Exception e) {
			//e.printStackTrace();
		}
	}
	
	public void initBeforeEngine() {
	    // Can call Pangine.getEngine().setDisplaySize here
	}

	public void init() {
	}
	
	public void step() {
	}
	
	//@OverrideMe
	public boolean onPause() {
		return false;
	}
}
