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
import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.core.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.visual.*;

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
    
    protected boolean isVisibleWhileRoomChanging() {
        return false;
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
    
    protected final static class LaunchCapsule extends Extra implements StepListener {
        private static Panmage img = null;
        private final static Panmage[] baseImgs = new Panmage[3];
        private final TileMap tm;
        private final boolean active;
        private final int dstX;
        private final int dstY;
        private Player occupant = null;
        private int zLeft;
        private int zRight;
        private boolean launched = false;
        
        protected LaunchCapsule(final Segment seg) {
            super(seg, BotsnBoltsGame.DEPTH_BG);
            tm = BotsnBoltsGame.tm;
            active = true; //TODO
            dstX = seg.intValue(3);
            dstY = seg.intValue(4);
            init();
            final int x = Enemy.getX(seg), y = Enemy.getY(seg);
            for (int i = 0; i < 2; i++) {
                final int xi = x + i;
                for (int j = -1; j < 3; j += 3) {
                    tm.setBehavior(xi, y + j, Tile.BEHAVIOR_SOLID);
                }
            }
            setView(Pangine.getEngine().createEmptyImage(Pantil.vmid(), FinPanple.ORIGIN, FinPanple.ORIGIN, new FinPanple2(32, 32)));
        }
        
        @Override
        protected final boolean isVisibleWhileRoomChanging() {
            return true;
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            if (!active) {
                return;
            }
            final Player player = PlayerContext.getPlayer(BotsnBoltsGame.pc);
            if (!Panctor.isDestroyed(player) && Pangine.getEngine().isCollision(player, this)) {
                if (occupant == null) {
                    occupant = player;
                    onEntrance();
                }
            } else if (occupant != null) {
                onExit();
                occupant = null;
            }
            if (occupant != null) {
                onOccupied();
            }
        }
        
        private final void onEntrance() {
            final float center = getPosition().getX() + 16;
            if (occupant.getPosition().getX() < center) {
                zRight = BotsnBoltsGame.DEPTH_OVERLAY;
                occupant.setMaxX(center + 4);
            } else {
                zLeft = BotsnBoltsGame.DEPTH_OVERLAY;
                occupant.setMinX(center - 5);
            }
        }
        
        private final void onOccupied() {
            final float x = getPosition().getX(), px = occupant.getPosition().getX();
            if (zLeft == BotsnBoltsGame.DEPTH_OVERLAY) {
                if (px <= occupant.getMinX()) {
                    launch();
                } else {
                    occupant.setJumpAllowed(px >= (x + 28));
                }
            } else {
                if (px >= occupant.getMaxX()) {
                    launch();
                } else {
                    occupant.setJumpAllowed(px < (x + 4));
                }
            }
        }
        
        private final void launch() {
            if (!launched) {
                RoomLoader.startX = dstX;
                RoomLoader.startY = dstY;
                launched = true;
                occupant.active = false;
                new Dematerialize(occupant);
                resetOccupant();
            }
        }
        
        private final void resetOccupant() {
            occupant.setJumpAllowed(true);
            occupant.setMinX(Integer.MIN_VALUE);
            occupant.setMaxX(Integer.MAX_VALUE);
        }
        
        private final void onExit() {
            resetOccupant();
            init();
        }
        
        private final void init() {
            zLeft = BotsnBoltsGame.DEPTH_FG;
            zRight = BotsnBoltsGame.DEPTH_FG;
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panlayer layer = getLayer();
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY();
            renderer.render(layer, BotsnBoltsGame.black, x, y, BotsnBoltsGame.DEPTH_BG, 0, 0, 32, 32, 0, false, false);
            img = getImage();
            renderer.render(layer, img, x, y, zLeft, 0, 0, 16, 32, 0, false, false);
            renderer.render(layer, img, x + 16, y, zRight, 16, 0, 16, 32, 0, false, false);
            final int frameDir = 6;
            final int baseImgIndex;
            if (active && !RoomChanger.isChanging()) {
                baseImgIndex = ((int) (Pangine.getEngine().getClock() % (frameDir * 3))) / frameDir;
            } else {
                baseImgIndex = 0;
            }
            final Panmage baseImg = getBaseImage(baseImgIndex);
            renderer.render(layer, baseImg, x, y - 16, BotsnBoltsGame.DEPTH_FG, 0, 16, 32, 16, 0, false, false);
            renderer.render(layer, baseImg, x, y + 32, BotsnBoltsGame.DEPTH_FG, 0, 0, 32, 16, 0, false, false);
        }
        
        private final static Panmage getImage() {
            return (img = BlockPuzzle.getImage(img, "LaunchCapsule", null, null, null));
        }
        
        private final static Panmage getBaseImage(final int i) {
            Panmage img = baseImgs[i];
            if (img == null) {
                img = BlockPuzzle.getImage(null, "LaunchCapsuleBase" + (i + 1), null, null, null);
                baseImgs[i] = img;
            }
            return img;
        }
    }
    
    protected static interface Warpable extends SpecPanctor {
        public void onMaterialized();
        
        public void onUnwarped();
    }
}
