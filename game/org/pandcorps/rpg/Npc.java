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
package org.pandcorps.rpg;

import java.awt.image.*;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.game.actor.Guy4Controller.NpcController;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.tile.*;

public class Npc extends Character {
    private final static FinPancolor col1 = new FinPancolor((short) 140);
    private final static FinPancolor col2 = new FinPancolor((short) 160);
    private final static FinPancolor col3 = new FinPancolor((short) 180);
    private final static FinPancolor col4 = new FinPancolor((short) 200);
    
    private final String name = "GUY";
    private NpcController controller = null;
    
    protected Npc(final String id, final NpcController controller) {
        super(id, getSheet());
        this.controller = controller;
    }
    
    private final static Panmage[] getSheet() {
        final BufferedImage[] body = ImtilX.loadStrip("org/pandcorps/rpg/res/chr/MBody.png");
        final BufferedImage[] face = ImtilX.loadStrip("org/pandcorps/rpg/res/chr/MFace01.png", 8, false);
        final BufferedImage[] eyes = ImtilX.loadStrip("org/pandcorps/rpg/res/chr/Eyes00.png", 8, false);
        final PixelFilter skinFilter = getFilter(new Pancolor(180, 130, 90), new Pancolor(200, 150, 110), new Pancolor(220, 170, 130), new Pancolor(240, 190, 150));
        final BufferedImage eyeSide = eyes.length < 2 ? eyes[0] : eyes[1];
        for (int i = 0; i < 5; i += 4) {
            Imtil.copy(face[0], body[i], 0, 0, 8, 8, 4, 1, Imtil.COPY_FOREGROUND);
            body[i] = Imtil.filter(body[i], skinFilter);
            Imtil.copy(eyes[0], body[i], 0, 0, 8, 4, 4, 5, Imtil.COPY_FOREGROUND);
            Imtil.copy(face[1], body[i + 1], 0, 0, 8, 8, 4, 1, Imtil.COPY_FOREGROUND);
            body[i + 1] = Imtil.filter(body[i + 1], skinFilter);
            Imtil.copy(eyeSide, body[i + 1], 0, 0, 8, 4, 5, 5, Imtil.COPY_FOREGROUND);
            body[i + 2] = Imtil.filter(body[i + 2], skinFilter);
        }
        Imtil.mirror(face[1]);
        Imtil.mirror(eyeSide);
        for (int i = 0; i < 5; i += 4) {
            Imtil.copy(face[1], body[i + 3], 0, 0, 8, 8, 4, 1, Imtil.COPY_FOREGROUND);
            body[i + 3] = Imtil.filter(body[i + 3], skinFilter);
            Imtil.copy(eyeSide, body[i + 3], 0, 0, 8, 4, 3, 5, Imtil.COPY_FOREGROUND);
        }
        final Panmage[] sheet = new Panmage[8];
        final Pangine engine = Pangine.getEngine();
        for (int i = 0; i < 8; i++) {
            sheet[i] = engine.createImage(Pantil.vmid(), o, null, null, body[i]);
        }
        return sheet;
    }
    
    private final static PixelFilter getFilter(final Pancolor c1, final Pancolor c2, final Pancolor c3, final Pancolor c4) {
        final ReplacePixelFilter filter = new ReplacePixelFilter();
        filter.put(col1, c1);
        filter.put(col2, c2);
        filter.put(col3, c3);
        filter.put(col4, c4);
        return filter;
    }
    
    @Override
    protected void onStill() {
        Guy4Controller.onStill(this, controller);
    }
    
    @Override
    public void onInteract(final TileWalker initiator) {
        face(initiator.getDirection().getOpposite());
    }
    
    @Override
    public String getInteractLabel() {
        return "TALK TO " + name;
    }
    
    //TODO
    public final static class CounterController implements NpcController {
        private CounterController() {
        }
        
        @Override
        public final void onStill(final Guy4 guy) {
            // Make sure next position is still touching a Counter
        }
    }
}
