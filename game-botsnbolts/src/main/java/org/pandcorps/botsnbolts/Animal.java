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
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.tile.*;

public class Animal {
    private final static int ANM_OFF_X = 9, ANM_H = 20;
    private final static Panple ANM_O = BotsnBoltsGame.og;
    private final static Panple BIRD_O = new FinPanple2(ANM_O.getX(), ANM_O.getY() + 6);
    private final static Panple ANM_MIN = Chr.getMin(ANM_OFF_X);
    private final static Panple ANM_MAX = Chr.getMax(ANM_OFF_X, ANM_H);
    
    private final static Map<String, Panmage> cache = new HashMap<String, Panmage>();
    
    private final static Panmage getImage(final String name, final Panple o, final Panple min, final Panple max) {
        Panmage img = cache.get(name);
        if (img == null) {
            final String path = "chr/animal/" + name;
            img = Pangine.getEngine().createImage(BotsnBoltsGame.PRE_IMG + path, o, min, max, BotsnBoltsGame.RES + path + ".png");
        }
        return img;
    }
    
    protected final static Panmage getAnimalImage(final PlayerImages pi) {
        return getImage(pi.animalName);
    }
    
    protected final static Panmage getBirdImage(final PlayerImages pi) {
        return getBirdImage(pi.birdName);
    }
    
    private final static Panmage getImage(final String name) {
        return getImage(name, ANM_O, ANM_MIN, ANM_MAX);
    }
    
    private final static Panmage getBirdImage(final String name) {
        return getImage(name, BIRD_O, ANM_MIN, ANM_MAX);
    }
    
    protected final static class Spring extends Chr implements Warpable, CollisionListener {
        private final PlayerImages pi;
        private final Panmage img;
        private Panmage imgActive;
        private int timer = 0;
        
        protected Spring(final Player p) {
            super(ANM_OFF_X, ANM_H);
            pi = p.pi;
            this.img = getAnimalImage(pi);
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
            getPosition().set(x, (j + 1) * BotsnBoltsGame.tileSize, BotsnBoltsGame.DEPTH_ENEMY);
            setMirror(p.isMirror());
            p.addActor(this);
            new Warp(this, pi);
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
            BotsnBoltsGame.fxJump.startSound();
        }

        @Override
        public final boolean onStepCustom() {
            if (timer > 0) {
                timer--;
                if (timer == 0) {
                    changeView(img);
                }
            }
            return false;
        }
        
        @Override
        protected final boolean onFell() {
            destroy();
            return true;
        }

        @Override
        public final void onMaterialized() {
            setVisible(true);
        }
        
        @Override
        public final void onUnwarped() {
        }
        
        private final Panmage getActive() {
            if (imgActive != null) {
                return imgActive;
            }
            return (imgActive = getImage(pi.animalName + "Spring"));
        }
    }
    
    protected final static class Rescue extends Panctor implements StepListener {
        protected final static double SPEED_DIVE = 12;
        protected final static double SPEED_FLAP = 4;
        protected final static float SPEED_EXIT = 3; // (float) Math.sqrt((SPEED_FLAP * SPEED_FLAP) / 2.0);
        private final static int OFF_X = -6;
        private final static int OFF_Y = 18;
        private final static byte MODE_DIVE = 0;
        private final static byte MODE_CARRY = 1;
        private final static byte MODE_EXIT = 2;
        private final Player p;
        private final Panmage[] imgs = new Panmage[3];
        private final Panple dst;
        private float vx = 0;
        private float vy = 0;
        private byte mode = MODE_DIVE;
        private int timer = 0;
        
        protected Rescue(final Player p) {
            this.p = p;
            final Panlayer layer = p.getLayer();
            if (layer == null) {
                dst = null;
                destroy();
                return;
            }
            final Panmage img = getBirdImage(p.pi);
            imgs[0] = img;
            setView(img);
            final Panple pos = getPosition(), ppos = p.getPosition();
            pos.set(layer.getViewMinimum().getX(), BotsnBoltsGame.GAME_H - ANM_H, BotsnBoltsGame.DEPTH_CARRIER);
            dst = new FinPanple2(getDstX(ppos), getDstY(ppos));
            final Panple diff = Panple.subtract(dst, pos);
            diff.setMagnitude2(SPEED_DIVE);
            vx = diff.getX();
            vy = diff.getY();
            p.addActor(this);
        }
        
        private final float getDstX(final Panple pos) {
            return pos.getX() + (p.getMirrorMultiplier() * OFF_X);
        }
        
        private final float getDstY(final Panple pos) {
            return pos.getY() + OFF_Y;
        }

        @Override
        public final void onStep(final StepEvent event) {
            if (mode == MODE_CARRY) {
                flap();
            } else {
                final Panple pos = getPosition();
                pos.add(vx, vy);
                if (mode == MODE_DIVE) {
                    if (pos.getDistance2(dst) <= SPEED_DIVE) {
                        startCarry();
                    }
                } else if (isInView()) {
                    flap();
                } else {
                    destroy();
                }
            }
        }
        
        private final void startCarry() {
            mode = MODE_CARRY;
            getPosition().set(dst.getX(), dst.getY());
            p.startRescued(this);
            BotsnBoltsGame.fxJump.startSound();
        }
        
        protected final void onCarrying() {
            final Panple ppos = p.getPosition();
            getPosition().set(getDstX(ppos), getDstY(ppos));
        }
        
        protected final void startExit() {
            mode = MODE_EXIT;
            setMirror(false);
            vx = SPEED_EXIT;
            vy = SPEED_EXIT;
        }
        
        private final void flap() {
            timer++;
            final Panmage img;
            if (timer < 12) {
                img = getImage(1);
            } else if (timer < 17) {
                img = getImage(2);
            } else {
                img = getImage(0);
                if (timer >= 24) {
                    timer = 0;
                }
            }
            changeView(img);
        }
        
        private final Panmage getImage(final int i) {
            final Panmage img = imgs[i];
            if (img != null) {
                return img;
            }
            return (imgs[i] = getBirdImage(p.pi.birdName + (i + 1)));
        }
    }
}
