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
package org.pandcorps.pandax.visual;

import java.util.List;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;

public abstract class RoomChanger extends Panctor implements StepListener {
    private final Panroom newRoom;
    private final int velX;
    private final int velY;
    private final Panctor tracked;
    
    // Might keep a constant deep background layer and a HUD layer on top
    public RoomChanger(final int velX, final int velY, final List<Panlayer> layersToKeepBeneath, final List<Panlayer> layersToKeepAbove, final List<Panctor> actorsToKeep) {
        this.velX = velX;
        this.velY = velY;
        final Pangame game = Pangame.getGame();
        final Panroom oldRoom = game.getCurrentRoom();
        setVisible(false);
        detachLayers(layersToKeepBeneath);
        detachLayers(layersToKeepAbove);
        Panctor tracked = null;
        for (final Panctor actor : actorsToKeep) {
            if (actor == oldRoom.getTracked()) {
                tracked = actor;
            }
            actor.detach();
        }
        this.tracked = tracked;
        newRoom = createRoom();
        game.setCurrentRoom(newRoom);
        Panlayer tempLayer = newRoom;
        for (final Panlayer layer : layersToKeepBeneath) {
            tempLayer.addBeneath(layer);
            tempLayer = layer;
        }
        tempLayer = newRoom;
        for (final Panlayer layer : layersToKeepAbove) {
            tempLayer.addAbove(layer);
            tempLayer = layer;
        }
        final float offX, offY;
        if (velX < 0) {
            offX = newRoom.getSize().getX();
        } else if (velX > 0) {
            offX = -oldRoom.getSize().getX();
        } else {
            offX = 0;
        }
        if (velY < 0) {
            offY = newRoom.getSize().getY();
        } else if (velY > 0) {
            offY = -oldRoom.getSize().getY();
        } else {
            offY = 0;
        }
        for (final Panctor actor : actorsToKeep) {
            newRoom.addActor(actor);
            if (velX < 0) {
                actor.getPosition().add(offX, offY);
            }
        }
        //TODO init newRoom origin
    }
    
    private final void detachLayers(final List<Panlayer> layers) {
        for (final Panlayer layer : layers) {
            layer.detach();
        }
    }
    
    @Override
    public final void onStep(final StepEvent event) {
        //TODO Apply velocity
        newRoom.getOrigin();
        //TODO Call finish() when done
    }
    
    private final void finish() {
        if (tracked != null) {
            Pangine.getEngine().track(tracked);
        }
        destroy();
    }
    
    protected abstract Panroom createRoom();
}
