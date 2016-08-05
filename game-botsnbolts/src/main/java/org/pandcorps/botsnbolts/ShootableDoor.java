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
package org.pandcorps.botsnbolts;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandax.tile.*;

public class ShootableDoor extends Panctor implements CollisionListener {
    private final int x;
    private final int y;
    private final int doorX;
    
    protected ShootableDoor(final Panlayer layer, final int x, final int y) {
        setVisible(false);
        final TileMap tm = BotsnBoltsGame.tm;
        tm.getLayer().addActor(this);
        this.x = x;
        this.y = y;
        if (x == 0) {
            doorX = 1;
        } else {
            doorX = x - 1;
            setMirror(true);
        }
        setDoorTiles(x, BotsnBoltsGame.doorTunnel, Tile.BEHAVIOR_SOLID, true);
        closeDoor();
    }
    
    private final void setDoorTiles(final int x, final Panframe[] door, final byte behavior, final boolean bg) {
        final TileMap tm = BotsnBoltsGame.tm;
        final int base = isMirror() ? 4 : 0;
        for (int j = 0; j < 4; j++) {
            final int index = tm.getIndex(x, y + j);
            final Panframe frm = door[base + j];
            if (bg) {
                tm.setBackground(index, frm, behavior);
            } else  {
                tm.setForeground(index, frm, behavior);
            }
        }
    }
    
    private final void closeDoor() {
        setDoorTiles(doorX, BotsnBoltsGame.doorCyan, Tile.BEHAVIOR_OPEN, false);
    }
    
    private final void openDoor() {
        final TileMap tm = BotsnBoltsGame.tm;
        for (int j = 0; j < 4; j++) {
            final int yj = y + j;
            tm.setBehavior(x, yj, Tile.BEHAVIOR_OPEN);
            tm.setForeground(doorX, y, null);
        }
    }

    @Override
    public final void onCollision(final CollisionEvent event) {
        
    }
}
