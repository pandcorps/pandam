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

public abstract class Boss extends Enemy {
    private final static String RES_BOSS = BotsnBoltsGame.RES + "boss/";
    private int waitTimer = 0;
    
    protected Boss(int offX, int h, int x, int y) {
        super(offX, h, x, y, HudMeter.MAX_VALUE);
    }
    
    @Override
    protected final boolean onStepCustom() {
        if (waitTimer > 0) {
            waitTimer--;
            return false;
        }
        return onReady();
    }
    
    protected abstract boolean onReady();

    @Override
    protected final void award(final PowerUp powerUp) {
    }
    
    protected final static Panmage getImage(final Panmage img, final String name) {
        if (img != null) {
            return img;
        }
        return Pangine.getEngine().createImage("boss." + name, RES_BOSS + name + ".png");
    }
    
    protected final static int VOLCANO_OFF_X = 20, VOLCANO_H = 48; //TODO
    
    protected final static class VolcanoBot extends Boss {
        protected VolcanoBot(int x, int y) {
            super(VOLCANO_OFF_X, VOLCANO_H, x, y);
        }
        
        @Override
        protected final boolean onReady() {
            return false;
        }
        
        @Override
        protected final void onGrounded() {
            hv = 0;
        }
    }
}
