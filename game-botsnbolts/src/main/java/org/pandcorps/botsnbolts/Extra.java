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

import org.pandcorps.botsnbolts.Enemy.*;
import org.pandcorps.core.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.*;

// Actors designed to be placed in levels; could be spawners, controllers, or just decorations
public abstract class Extra extends Panctor {
    protected final static int TIMER_SPAWNER = 90;
    
    protected Extra(final Segment seg, final int z) {
        final Panple pos = getPosition();
        final int x = Enemy.getX(seg), y = Enemy.getY(seg);
        BotsnBoltsGame.tm.savePosition(pos, x, y);
        pos.setZ(z);
        initTileCoordinates(x, y);
    }
    
    //@OverrideMe
    protected void initTileCoordinates(final int x, final int y) {
    }
    
    protected abstract static class EnemySpawner extends Extra implements StepListener {
        protected int x;
        protected int y;
        protected int waitTimer;
        private static Panmage img = null;
        
        protected EnemySpawner(final Segment seg) {
            super(seg, BotsnBoltsGame.DEPTH_BG);
            initTimer();
            setView(getImage());
        }
        
        @Override
        protected final void initTileCoordinates(final int x, final int y) {
            this.x = x;
            this.y = y;
        }
        
        protected void initTimer() {
            waitTimer = TIMER_SPAWNER;
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            waitTimer--;
            if (waitTimer <= 0) {
                if (isSpawningAllowed()) {
                    spawnEnemy();
                }
                initTimer();
            }
        }
        
        protected boolean isSpawningAllowed() {
            return isInView();
        }
        
        protected final Enemy spawnEnemy() {
            final Panlayer layer = getLayer();
            if (layer == null) {
                return null;
            }
            final Enemy enemy = newEnemy();
            layer.addActor(enemy);
            return enemy;
        }
        
        protected abstract Enemy newEnemy();
        
        private final static Panmage getImage() {
            if (img == null) {
                img = Pangine.getEngine().createEmptyImage("spawner", FinPanple.ORIGIN, FinPanple.ORIGIN, BotsnBoltsGame.CENTER_32);
            }
            return img;
        }
    }
    
    protected final static class BoulderSpawner extends EnemySpawner {
        protected BoulderSpawner(final Segment seg) {
            super(seg);
        }
        
        @Override
        protected final Enemy newEnemy() {
            return new BoulderEnemy(x, y);
        }
    }
    
    protected final static class RocketSpawner extends EnemySpawner {
        private final boolean vertical;
        private final int max;
        
        protected RocketSpawner(final Segment seg) {
            super(seg);
            vertical = (y == 0);
            max = seg.getInt(3, 1);
            waitTimer -= seg.getInt(4, 0);
        }
        
        @Override
        protected final boolean isSpawningAllowed() {
            if (x >= BotsnBoltsGame.tm.getWidth()) {
                return true;
            }
            return vertical ? isInView() : !isInView();
        }
        
        @Override
        protected final Enemy newEnemy() {
            return new Rocket(x, (max == 1) ? y : (y + Mathtil.randi(0, max - 1)));
        }
    }
    
    protected static interface Warpable extends SpecPanctor {
        public void onMaterialized();
    }
}
