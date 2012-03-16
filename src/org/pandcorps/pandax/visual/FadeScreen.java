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
package org.pandcorps.pandax.visual;

import org.pandcorps.core.Pantil;
import org.pandcorps.core.img.Pancolor;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;

public abstract class FadeScreen extends Panscreen {
    private final FadeController c = new FadeScreenController();
    private final Panroom room;
    private final Pancolor color;
    private final short oldAlpha;
    private final int time;
    
    protected FadeScreen(final Pancolor color, final int time) {
        room = Pangame.getGame().getCurrentRoom();
        this.color = room.getBlendColor();
        oldAlpha = this.color.getA();
        this.color.set(color);
        this.time = time;
    }
    
    @Override
    protected final void load() throws Exception {
        room.addActor(c);
        color.setA(Pancolor.MAX_VALUE);
        c.setVelocity((short) -4);
        start();
    }
    
    protected abstract void start();
    
    protected abstract void finish();
    
    private final class FadeScreenController extends FadeController {
        private FadeScreenController() {
            super(Pantil.vmid());
        }
        
        @Override
        protected final void onFadeEnd() {
            if (color.getA() == 0) {
                final Pangine engine = Pangine.getEngine();
                engine.addTimer(time, new TimerListener() { @Override public final void onTimer(final TimerEvent event) {
                    setVelocity((short) 4);
                }} );
            } else {
                color.setA(oldAlpha);
                finish(); // Might open a new FadeScreen, so revert alpha first
            }
        }
    }
}
