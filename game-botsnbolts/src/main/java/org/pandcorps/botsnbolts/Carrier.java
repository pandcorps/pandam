/*
Copyright (c) 2009-2017, Andrew M. Martin
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

public final class Carrier extends Panctor implements StepListener, CollisionListener {
    protected Player carried;
    private final int velX;
    private final int velY;
    private final int duration;
    private int dir = 1;
    private int timer = 0;
    
    protected Carrier(final int x, final int y, final int velX, final int velY, final int duration) {
        final Panple pos = getPosition();
        final TileMap tm = BotsnBoltsGame.tm;
        tm.savePosition(pos, x, y);
        pos.setZ(BotsnBoltsGame.DEPTH_CARRIER);
        this.velX = velX;
        this.velY = velY;
        this.duration = duration;
        setView(BotsnBoltsGame.carrier);
        tm.getLayer().addActor(this);
    }

    @Override
    public final void onStep(final StepEvent event) {
        getPosition().add(dir * velX, dir * velY);
        timer++;
        if (timer >= duration) {
            dir *= -1;
            timer = 0;
        }
        if (carried != null) {
            final Panple pos = getPosition();
            final int off = carried.isMirror() ? -1 : 1;
            carried.getPosition().set(pos.getX() + off, pos.getY());
        }
    }
    
    @Override
    public final void onCollision(final CollisionEvent event) {
        if (carried != null) {
            return;
        }
        final Collidable collider = event.getCollider();
        if (collider.getClass() != Player.class) {
            return;
        }
        final Player player = (Player) collider;
        if (player.v > 0) {
            return;
        } else if (player.getPosition().getY() < (getPosition().getY() - Player.MAX_V)) {
            return;
        } else if (player.isGrounded()) {
            return;
        }
        player.startCarried(this);
    }
}
