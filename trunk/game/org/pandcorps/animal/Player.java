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
package org.pandcorps.animal;

import org.pandcorps.pandam.Pangine;
import org.pandcorps.pandam.Panteraction;
import org.pandcorps.pandax.tile.Direction;
import org.pandcorps.pandax.tile.TileOccupant;

public class Player extends Animal {
    private static Player player = null;
    
    protected Player(final String id) {
        super(id);
        player = this;
    }
    
    protected final static Player getPlayer() {
        return player;
    }
    
    @Override
    protected void onStill() {
        final Panteraction interaction = Pangine.getEngine().getInteraction();
        if (interaction.KEY_DOWN.isActive()) {
            walk(Direction.South);
        } else if (interaction.KEY_UP.isActive()) {
            walk(Direction.North);
        } else if (interaction.KEY_LEFT.isActive()) {
            walk(Direction.West);
        } else if (interaction.KEY_RIGHT.isActive()) {
            walk(Direction.East);
        } else if (interaction.KEY_SPACE.isActive()) {
            final TileOccupant neighbor = getNeighbor(getDirection());
            if (neighbor instanceof Tree) {
                ((Tree) neighbor).onShake();
            } else if (neighbor instanceof Neighbor) {
                ((Neighbor) neighbor).onInteract();
            }
        }
    }
}