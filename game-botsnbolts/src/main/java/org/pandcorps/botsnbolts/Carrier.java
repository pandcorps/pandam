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
package org.pandcorps.botsnbolts;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandax.tile.*;

public final class Carrier extends Panctor implements StepListener, CollisionListener {
    private final int velX;
    private final int velY;
    private final int duration;
    private int dir = 1;
    private int timer = 0;
    
    protected Carrier(final int x, final int y, final int velX, final int velY, final int duration) {
        setPosition(this, x, y + 1, BotsnBoltsGame.DEPTH_CARRIER);
        this.velX = velX;
        this.velY = velY;
        this.duration = duration;
        setView(BotsnBoltsGame.carrier);
    }
    
    protected final static void setPosition(final Panctor actor, final int x, final int y, final int z) {
        final Panple pos = actor.getPosition();
        final TileMap tm = BotsnBoltsGame.tm;
        tm.savePosition(pos, x, y);
        pos.setZ(z);
    }

    @Override
    public final void onStep(final StepEvent event) {
        getPosition().add(dir * velX, dir * velY);
        timer++;
        if (timer >= duration) {
            dir *= -1;
            timer = 0;
        }
    }
    
    protected final void onStepCarried(final Player carried) {
        if (carried != null) {
            final Panple pos = getPosition(), cpos = carried.getPosition();
            final float carrierX = carried.carrierX;
            if (carrierX != Player.NULL_COORD) {
                final float diff = cpos.getX() - carrierX;
                carried.carrierOff += diff;
            }
            final float carrierOff = carried.carrierOff;
            if (Math.abs(carrierOff) > 22) {
                carried.v = 0;
                carried.walkedOffCarrier = this;
                carried.endCarried();
                return;
            }
            final float cx = pos.getX() + carrierOff;
            cpos.set(cx, pos.getY());
            carried.carrierX = cx;
        }
    }
    
    @Override
    public final void onCollision(final CollisionEvent event) {
        final Collidable collider = event.getCollider();
        if (collider.getClass() != Player.class) {
            return;
        }
        final Player player = (Player) collider;
        if (player.v > 0) {
            return;
        } else if (player.carrier != null) {
            return;
        } else if (player.walkedOffCarrier == this) {
            return;
        }
        final Panple ppos = player.getPosition(), pos = getPosition();
        if (ppos.getY() < (pos.getY() - Player.MAX_V)) {
            return;
        } else if (player.isGrounded()) {
            return;
        } else if (player.jumpStartedOnCarrier == this) {
            final float px = ppos.getX(), x = pos.getX();
            final float phv = player.chv;
            if ((phv < 0) && (px < (x - 6))) {
                return;
            } else if ((phv > 0) && (px > (x + 6))) {
                return;
            }
        }
        player.startCarried(this);
    }
    
    protected final static class Lifter extends Panctor implements StepListener {
        private final static int DURATION = 4;
        
        private int timer = DURATION;
        
        protected Lifter(final int x, final int y) {
            setPosition(this, x, y, BotsnBoltsGame.DEPTH_CARRIER);
            setView(BotsnBoltsGame.lifter);
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            timer--;
            if (timer <= 0) {
                final Panple pos = getPosition();
                new Wind(pos.getX() + Mathtil.randi(0, 29), pos.getY() + Mathtil.randi(15, 29));
                timer = DURATION;
            }
        }
    }
    
    protected final static class Wind extends Panctor implements StepListener {
        private int timer = 8;
        
        protected Wind(final float x, final float y) {
            getPosition().set(x, y, BotsnBoltsGame.DEPTH_BURST);
            setView(BotsnBoltsGame.wind);
            BotsnBoltsGame.tm.getLayer().addActor(this);
        }

        @Override
        public final void onStep(final StepEvent event) {
            if (timer < 0) {
                destroy();
                return;
            }
            getPosition().addY(6);
            timer--;
        }
    }
}
