/*
Copyright (c) 2009-2018, Andrew M. Martin
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

import java.util.*;

import org.pandcorps.botsnbolts.Extra.*;
import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandax.tile.TileMap;

public class Animal {
    private final static Panple ANM_O = null; //TODO
    private final static Panple ANM_MIN = null;
    private final static Panple ANM_MAX = null;
    
    private final static Map<String, Panmage> cache = new HashMap<String, Panmage>();
    
    private final static Panmage getImage(final String name, final Panple o, final Panple min, final Panple max) {
        Panmage img = cache.get(name);
        if (img == null) {
            final String path = "chr/animal/" + name;
            img = Pangine.getEngine().createImage(BotsnBoltsGame.PRE_IMG + path, o, min, max, BotsnBoltsGame.RES + path + ".png");
        }
        return img;
    }
    
    private final static Panmage getImage(final String name) {
        return getImage(name, ANM_O, ANM_MIN, ANM_MAX);
    }
    
    protected final static class Spring extends Panctor implements Warpable, StepListener, CollisionListener {
        private final PlayerImages pi;
        private final Panmage img;
        private Panmage imgActive;
        private int timer = 0;
        
        protected Spring(final Player p) {
            pi = p.pi;
            this.img = getImage(pi.animalName);
            setView(img);
            setVisible(false);
            final Panple ppos = p.getPosition();
            final float x = ppos.getX(), y = ppos.getY();
            final TileMap tm = BotsnBoltsGame.tm;
            final int i = tm.getContainerColumn(x);
            int j = tm.getContainerRow(y);
            for (; j >= 0; j--) {
                if (Chr.isSolidTile(i, j)) {
                    break;
                }
            }
            getPosition().set(x, (j + 1) * BotsnBoltsGame.DIM, BotsnBoltsGame.DEPTH_CARRIER);
            p.addActor(this);
            new Warp(this, pi.warp, pi.materialize);
        }
        
        @Override
        public final void onCollision(final CollisionEvent event) {
            if (!isVisible()) {
                return;
            }
            final Collidable collider = event.getCollider();
            if (collider.getClass() != Player.class) {
                return;
            }
            final Player p = (Player) collider;
            if (p.onSpring()) {
                startSpring();
            }
        }
        
        private final void startSpring() {
            changeView(getActive());
            timer = 15;
        }

        @Override
        public final void onStep(final StepEvent event) {
            if (timer > 0) {
                timer--;
                if (timer == 0) {
                    changeView(img);
                }
            }
        }

        @Override
        public final void onMaterialized() {
            setVisible(true);
        }
        
        private final Panmage getActive() {
            if (imgActive != null) {
                return imgActive;
            }
            return (imgActive = getImage(pi.animalName + "Spring"));
        }
    }
}
