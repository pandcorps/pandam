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

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;

public abstract class RoomChanger extends Panctor implements StepListener {
    private static RoomChanger activeChanger = null;
    private Panroom newRoom = null;
    private final int nextX;
    private final int nextY;
    private final int velX;
    private final int velY;
    private Panctor tracked = null;
    private final List<? extends Panctor> actorsToDestroy;
    private int age = 0;
    
    // Might keep a constant deep background layer and a HUD layer on top
    public RoomChanger(final int nextX, final int nextY, final int velX, final int velY, final List<Panlayer> layersToKeepBeneath, final List<Panlayer> layersToKeepAbove,
                       final List<? extends Panctor> actorsToKeep, final List<? extends Panctor> actorsToDestroy) {
        Panctor.destroy(activeChanger);
        activeChanger = this;
        this.nextX = nextX;
        this.nextY = nextY;
        this.velX = velX;
        this.velY = velY;
        this.actorsToDestroy = actorsToDestroy;
        Pangine.getEngine().executeInGameThread(new Runnable() {
            @Override public final void run() {
                start(layersToKeepBeneath, layersToKeepAbove, actorsToKeep);
            }});
    }
    
    private final void start(final List<Panlayer> layersToKeepBeneath, final List<Panlayer> layersToKeepAbove,
                             final List<? extends Panctor> actorsToKeep) {
        final Pangine engine = Pangine.getEngine();
        final Pangame game = Pangame.getGame();
        final Panroom oldRoom = game.getCurrentRoom();
        setVisible(false);
        detachLayers(layersToKeepBeneath);
        detachLayers(layersToKeepAbove);
        Panctor tracked = null;
        for (final Panctor actor : Coltil.unnull(actorsToKeep)) {
            if (actor == oldRoom.getTracked()) {
                tracked = actor;
            }
            actor.detach();
        }
        this.tracked = tracked;
        newRoom = createRoom();
        game.setCurrentRoom(newRoom);
        oldRoom.destroy();
        Panlayer tempLayer = newRoom;
        for (final Panlayer layer : Coltil.unnull(layersToKeepBeneath)) {
            tempLayer.addBeneath(layer);
            tempLayer = layer;
        }
        tempLayer = newRoom;
        for (final Panlayer layer : Coltil.unnull(layersToKeepAbove)) {
            tempLayer.addAbove(layer);
            tempLayer = layer;
        }
        final float offX, offY, roomX, roomY;
        if (velX < 0) {
            offX = newRoom.getSize().getX();
            roomX = offX; // engine.getEffectiveWidth();
        } else if (velX > 0) {
            offX = -oldRoom.getSize().getX();
            roomX = -engine.getEffectiveWidth();
        } else {
            offX = nextX - oldRoom.getViewMinimum().getX();
            roomX = nextX;
        }
        if (velY < 0) {
            offY = newRoom.getSize().getY();
            roomY = offY; // engine.getEffectiveHeight();
        } else if (velY > 0) {
            offY = -oldRoom.getSize().getY();
            roomY = -engine.getEffectiveHeight();
        } else {
            offY = nextY - oldRoom.getViewMinimum().getY();
            roomY = nextY;
        }
        for (final Panctor actor : Coltil.unnull(actorsToKeep)) {
            newRoom.addActor(actor);
            actor.getPosition().add(offX, offY);
        }
        newRoom.addActor(this);
        newRoom.getOrigin().set(roomX, roomY);
    }
    
    private final void detachLayers(final List<Panlayer> layers) {
        for (final Panlayer layer : Coltil.unnull(layers)) {
            layer.detach();
        }
    }
    
    @Override
    public final void onStep(final StepEvent event) {
        final Panple o = newRoom.getOrigin();
        o.add(velX, velY);
        age++;
        boolean finished = false;
        if (velX > 0) {
            if (o.getX() >= 0) {
                finished = true;
            }
        } else if (velX < 0) {
            if (o.getX() <= (newRoom.getSize().getX() - Pangine.getEngine().getEffectiveWidth())) {
                finished = true;
            }
        } else if (velY > 0) {
            if (o.getY() >= 0) {
                finished = true;
            }
        } else if (velY < 0) {
            if (o.getY() <= (newRoom.getSize().getY() - Pangine.getEngine().getEffectiveHeight())) {
                finished = true;
            }
        }
        if (finished) {
            o.set(0, 0);
            finish();
        }
    }
    
    private final void finish() {
        if (tracked != null) {
            Pangine.getEngine().track(tracked);
        }
        for (final Panctor actor : Coltil.unnull(actorsToDestroy)) {
            actor.destroy();
        }
        if (activeChanger == this) {
            activeChanger = null;
        }
        onFinished();
        destroy();
    }
    
    public final int getVelocityX() {
        return velX;
    }
    
    public final int getVelocityY() {
        return velY;
    }
    
    public final int getAge() {
        return age;
    }
    
    public final static RoomChanger getActiveChanger() {
        return activeChanger;
    }
    
    public final static boolean isChanging() {
        return activeChanger != null && !activeChanger.isDestroyed() && activeChanger.getLayer() != null;
    }
    
    protected abstract Panroom createRoom();
    
    //@OverrideMe
    protected void onFinished() {
    }
}
