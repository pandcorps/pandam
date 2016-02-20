/*
Copyright (c) 2009-2016, Andrew M. Martin
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
package org.pandcorps.demo.pandax.tile;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandax.tile.*;

public final class TileController extends Panctor implements RoomAddListener {

	public TileController(final String id) {
		super(id);
	}

	@Override
	public final void onRoomAdd(final RoomAddEvent event) {
		final Panroom room = event.getRoom();
		final Panple size = room.getSize();
		final int w = (int) (size.getX() / 16);
        final int h = (int) (size.getY() / 16);
		final TileMap map = new TileMap(Pantil.vmid(), w, h, 16, 16);
		final int wlim = w - 2, hlim = h - 2;
		room.addActor(map);
		final Pangine engine = Pangine.getEngine();
		for (int i = 1; i <= wlim; i++) {
		    for (int j = 1; j <= hlim; j++) {
		        final int index = map.getIndex(i, j);
		        if (i == 1 || j == 1 || i == wlim || j == hlim) {
		            map.setBackground(index, engine.getImage("WallImage"), Tile.BEHAVIOR_SOLID);
		        } else if (i == 3 && j == 3) {
		            map.setForeground(index, engine.getImage("ForegroundImage"), Tile.BEHAVIOR_OPEN);
		        } else {
		            map.setBackground(index, engine.getImage("BackgroundImage"), Tile.BEHAVIOR_OPEN);
		        }
		    }
		}
		final SquareGuyActor actor = new SquareGuyActor(Pantil.vmid());
		actor.setPosition(map, 5, 5);
        room.addActor(actor);
	}
}