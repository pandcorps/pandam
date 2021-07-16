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
package org.pandcorps.championsofslam;

import org.pandcorps.pandam.*;
import org.pandcorps.championsofslam.Champion.*;
import org.pandcorps.core.img.*;

public class Arena extends Panctor {
    private final ArenaDefinition def;
    
    public Arena(final ArenaDefinition def) {
        this.def = def;
    }
    
    @Override
    public final void renderView(final Panderer renderer) {
        final Panlayer layer = getLayer();
        final Panmage img = ChampionsOfSlamGame.imgArena;
        final FloatColor ringColor = def.ringColor, turnbuckleColor = def.turnbuckleColor, apronColor = def.apronColor, ropeColor = def.ropeColor;
        final float ringR = ringColor.getR(), ringG = ringColor.getG(), ringB = ringColor.getB();
        final float shadowR = 0.75f * ringR, shadowG = 0.75f * ringG, shadowB = 0.75f * ringB;
        final float turnbuckleR = turnbuckleColor.getR(), turnbuckleG = turnbuckleColor.getG(), turnbuckleB = turnbuckleColor.getB();
        final float apronR = apronColor.getR(), apronG = apronColor.getG(), apronB = apronColor.getB();
        final float ropeR = ropeColor.getR(), ropeG = ropeColor.getG(), ropeB = ropeColor.getB();
        for (int h = 0; h < 2; h++) {
            final boolean mirror = h > 0;
            final int screenEdge, mirrorMultiplier, crowdOffX, floorX, ringX, centerX, ropeX, ropeY, ropeZ, ropeSouthZ = 1520;
            if (mirror) {
                screenEdge = 383;
                mirrorMultiplier = -1;
                crowdOffX = 192;
                floorX = 368;
                ringX = 336;
                centerX = 192;
                ropeX = 35;
                ropeY = 212;
                ropeZ = 6;
            } else {
                screenEdge = 0;
                mirrorMultiplier = 1;
                crowdOffX = 0;
                floorX = 0;
                ringX = 16;
                centerX = 176;
                ropeX = 37;
                ropeY = 58;
                ropeZ = ropeSouthZ;
            }
            final int ropeW = 384 - (ropeX * 2);
            for (int i = 0; i < 12; i++) {
                final int x = crowdOffX + (i * 16), y, crowdH;
                if ((mirror && (i == 11)) || (!mirror && (i == 0))) {
                    y = 176;
                    crowdH = 48;
                } else {
                    y = 192;
                    crowdH = 32;
                }
                renderer.render(layer, img, x, y, 0, 0, 0, 16, crowdH, 0, mirror, false);
                // Cheering fans at z 2 handled outside of this class
            }
            renderer.render(layer, img, ringX, 179, 0, 16, 32, 32, 16, 0, mirror, false, ringR, ringG, ringB);
            renderer.render(layer, img, ringX, 187, 4, 16, 0, 32, 32, 0, mirror, false, turnbuckleR, turnbuckleG, turnbuckleB);
            for (int j = 0; j < 11; j++) {
                renderer.render(layer, img, floorX, j * 16, 0, 0, 32, 16, 16, 0, mirror, false);
            }
            renderer.rectangle(layer, screenEdge + (16 * mirrorMultiplier), 32, 0, 1, 160, 0.0f, 0.0f, 0.0f);
            renderer.rectangle(layer, screenEdge + (17 * mirrorMultiplier) - (mirror ? 1 : 0), 32, 0, 2, 160, shadowR, shadowG, shadowB);
            renderer.rectangle(layer, screenEdge + (30 * mirrorMultiplier), 64, 6, 1, 144, 0.0f, 0.0f, 0.0f);
            renderer.rectangle(layer, screenEdge + (31 * mirrorMultiplier), 64, 6, 1, 144, ropeR, ropeG, ropeB);
            renderer.render(layer, img, ringX, 32, 0, 16, 80, 32, 16, 0, mirror, false, ringR, ringG, ringB);
            renderer.render(layer, img, ringX, 0, 0, 0, 96, 32, 32, 0, mirror, false, apronR, apronG, apronB);
            renderer.render(layer, img, ringX, 33, ropeSouthZ, 16, 48, 32, 32, 0, mirror, false, turnbuckleR, turnbuckleG, turnbuckleB);
            for (int i = 0; i < 5; i++) {
                final int x = ringX + (i * 32 * mirrorMultiplier);
                renderer.render(layer, img, x, 0, 0, 48, 112, 32, 16, 0, mirror, false);
            }
            renderer.render(layer, img, centerX, 0, 0, 48, 112, 16, 16, 0, mirror, false);
            renderer.rectangle(layer, ropeX, ropeY, ropeZ, ropeW, 1, ropeR, ropeG, ropeB);
            renderer.rectangle(layer, ropeX, ropeY - 1, ropeZ, ropeW, 1, 0.0f, 0.0f, 0.0f);
            renderer.rectangle(layer, ropeX, ropeY - 7, ropeZ, ropeW, 1, ropeR, ropeG, ropeB);
            renderer.rectangle(layer, ropeX, ropeY - 8, ropeZ, ropeW, 1, 0.0f, 0.0f, 0.0f);
            renderer.rectangle(layer, ropeX, ropeY - 14, ropeZ, ropeW, 1, ropeR, ropeG, ropeB);
            renderer.rectangle(layer, ropeX, ropeY - 15, ropeZ, ropeW, 1, 0.0f, 0.0f, 0.0f);
        }
        for (int i = 0; i < 18; i++) {
            renderer.render(layer, img, 48 + (i * 16), 0, 0, 16, 96, 16, 32, 0, false, false, apronR, apronG, apronB);
        }
        renderer.rectangle(layer, 17, 192, 2, 350, 1, shadowR, shadowG, shadowB);
        renderer.rectangle(layer, 32, 189, 2, 320, 1, shadowR, shadowG, shadowB);
        renderer.rectangle(layer, 32, 35, 2, 320, 1, shadowR, shadowG, shadowB);
        renderer.rectangle(layer, 17, 32, 2, 350, 1, shadowR, shadowG, shadowB);
    }
    
    public final static class ArenaDefinition {
        protected final FloatColor ringColor = new FloatColor();
        protected final FloatColor ropeColor = new FloatColor();
        protected final FloatColor turnbuckleColor = new FloatColor();
        protected final FloatColor apronColor = new FloatColor();
    }
}
