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

public final class GrapplingHook extends Chr {
    protected final static int GRAPPLING_HOOK_X = 3;
    protected final static int GRAPPLING_HOOK_H = 6;
    
    protected final static int DISTANCE_BETWEEN_LINKS = 8;
    
    private final static int speedMultiplier = 16;
    
    private final Player player;
    
    protected GrapplingHook(final Player player) {
        super(GRAPPLING_HOOK_X, GRAPPLING_HOOK_H);
        this.player = player;
        v = 1;
        if (player.isMirror()) {
            setMirror(true);
            hv = -1;
        } else {
            hv = 1;
        }
    }
    
    @Override
    protected final boolean onStepCustom() {
        for (int i = 0; i < speedMultiplier; i++) {
            if (addY() != Y_NORMAL) {
                finish();
                break;
            } else if (addX(hv) != X_NORMAL) {
                finish();
                break;
            }
        }
        return true;
    }
    
    @Override
    protected final void renderView(final Panderer renderer) {
        final Panple dir = Panple.subtract(getPosition(), player.getPosition());
        final double mag = dir.getMagnitude2();
        if (mag > 0) {
            dir.multiply((float) (DISTANCE_BETWEEN_LINKS / mag));
        }
        for (int i = 0; i < mag; i++) {
            //renderer.render(layer, image, x, y, z);
            i+= DISTANCE_BETWEEN_LINKS;
        }
    }
    
    private final void finish() {
    }
}
