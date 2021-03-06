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

import org.pandcorps.pandam.*;

public final class GrapplingHook extends Chr {
    protected final static int GRAPPLING_HOOK_X = 3;
    protected final static int GRAPPLING_HOOK_H = 6;
    
    protected final static int DISTANCE_BETWEEN_LINKS = 8;
    
    private final static int speedMultiplier = 16;
    
    private final Player player;
    protected boolean finished = false;
    private double mag = 0;
    private double oldGrapplingR = 0;
    
    protected GrapplingHook(final Player player) {
        super(GRAPPLING_HOOK_X, GRAPPLING_HOOK_H);
        this.player = player;
        v = 1;
        setMirror(player.isMirror());
        if (player.movedDuringJump || (player.hv != 0)) {
            hv = getMirrorMultiplier();
        }
        setView(player.pi.link);
        final Panple ppos = player.getPosition();
        getPosition().set(ppos.getX(), ppos.getY(), BotsnBoltsGame.DEPTH_POWER_UP);
        player.addActor(this);
    }
    
    @Override
    protected final boolean onStepCustom() {
        if (finished) {
            return true;
        }
        for (int i = 0; i < speedMultiplier; i++) {
            final byte yStatus = addY();
            if (yStatus == Y_BUMP) {
                finish();
                break;
            } else if (yStatus != Y_NORMAL) {
                cancel();
                break;
            }
            final byte xStatus = addX(hv);
            if (xStatus == X_WALL) {
                finish();
                break;
            } else if (xStatus != X_NORMAL) {
                cancel();
                break;
            }
        }
        return true;
    }
    
    @Override
    protected final void renderView(final Panderer renderer) {
        final Panlayer layer = getLayer();
        if (layer == null) {
            return;
        } else if (player.grapplingHook == null) {
            return; // Must have been destroyed this step; don't render or manipulate grappling position
        }
        final Panmage image = player.pi.link;
        final boolean mirror = player.isMirror();
        final Panple pos = getPosition();
        final Panple dir = Panple.subtract(player.getGrapplingPosition(), pos);
        final double newGrapplingR = player.grapplingR;
        if ((mag == 0) || !finished || (newGrapplingR != oldGrapplingR)) {
            mag = dir.getMagnitude2();
        }
        oldGrapplingR = newGrapplingR;
        final int numLinks = Math.max(1, (int) ((mag - 10) / DISTANCE_BETWEEN_LINKS));
        if (mag > 0) {
            dir.multiply((float) (DISTANCE_BETWEEN_LINKS / mag));
        }
        final float x = pos.getX(), y = pos.getY(), z = pos.getZ(), dx = dir.getX(), dy = dir.getY();
        for (int i = 0; i < numLinks; i++) {
            renderer.render(layer, image, x + (i * dx), y + (i * dy), z, 0, mirror, false);
            i++;
        }
    }
    
    private final void finish() {
        finished = true;
        player.onGrappleConnected();
    }
    
    private final void cancel() {
        player.endGrapple();
    }
}
