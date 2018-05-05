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

import java.lang.reflect.*;

import org.pandcorps.botsnbolts.Enemy.*;
import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.*;

// Actors designed to be placed in levels; could be spawners, controllers, or just decorations
public abstract class Extra extends Panctor {
    protected final static int TIMER_SPAWNER = 90;
    
    protected Extra(final int x, final int y, final int z) {
        final Panple pos = getPosition();
        BotsnBoltsGame.tm.savePosition(pos, x, y);
        pos.setZ(z);
    }
    
    protected abstract static class EnemySpawner extends Extra implements StepListener {
        private final Constructor<? extends Enemy> constructor;
        private final int x;
        private final int y;
        private int waitTimer;
        private static Panmage img = null;
        
        protected EnemySpawner(final Constructor<? extends Enemy> constructor, final int x, final int y) {
            super(x, y, BotsnBoltsGame.DEPTH_BG);
            this.constructor = constructor;
            this.x = x;
            this.y = y;
            initTimer();
            setView(getImage());
        }
        
        protected void initTimer() {
            waitTimer = TIMER_SPAWNER;
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            waitTimer--;
            if (waitTimer <= 0) {
                if (isInView()) {
                    newEnemy();
                }
                initTimer();
            }
        }
        
        protected final Enemy newEnemy() {
            final Panlayer layer = getLayer();
            if (layer == null) {
                return null;
            }
            final Enemy enemy = RoomLoader.newActor(constructor, x, y);
            layer.addActor(enemy);
            return enemy;
        }
        
        private final static Panmage getImage() {
            if (img == null) {
                img = Pangine.getEngine().createEmptyImage("spawner", FinPanple.ORIGIN, FinPanple.ORIGIN, BotsnBoltsGame.CENTER_32);
            }
            return img;
        }
    }
    
    protected final static class BoulderSpawner extends EnemySpawner {
        private static Constructor<BoulderEnemy> constructor = null;
        
        protected BoulderSpawner(final int x, final int y) {
            super(getBoulderConstructor(), x, y);
        }
        
        private final static Constructor<BoulderEnemy> getBoulderConstructor() {
            return (constructor = getEnemyConstructor(constructor, BoulderEnemy.class));
        }
    }
    
    protected static <T extends Enemy> Constructor<T> getEnemyConstructor(final Constructor<T> constructor, final Class<T> c) {
        if (constructor != null) {
            return constructor;
        }
        try {
            return c.getDeclaredConstructor(Integer.TYPE, Integer.TYPE);
        } catch (final Exception e) {
            throw Pantil.toRuntimeException(e);
        }
    }
    
    protected static interface Warpable extends SpecPanctor {
        public void onMaterialized();
    }
}
