/*
Copyright (c) 2009-2023, Andrew M. Martin
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

import org.pandcorps.botsnbolts.Boss.*;
import org.pandcorps.botsnbolts.BotsnBoltsGame.*;
import org.pandcorps.botsnbolts.Enemy.*;
import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.botsnbolts.RoomLoader.*;
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
    
    protected Extra() {
    }
    
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
    
    protected boolean isAllowed() {
        return true;
    }
    
    protected boolean isVisibleWhileRoomChanging() {
        return false;
    }
    
    protected final static Player getCollidingPlayer(final Panctor src) {
        for (final PlayerContext pc : BotsnBoltsGame.pcs) {
            final Player player = PlayerContext.getPlayer(pc);
            if (!Panctor.isDestroyed(player) && Pangine.getEngine().isCollision(player, src)) {
                return player;
            }
        }
        return null;
    }
    
    protected abstract static class EnemySpawner extends Extra implements StepListener {
        protected int x;
        protected int y;
        protected int waitTimer;
        
        protected EnemySpawner(final Segment seg) {
            super(seg, BotsnBoltsGame.DEPTH_BG);
            initTimer();
            setView(BotsnBoltsGame.getEmpty16());
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
            onWait(waitTimer);
            if (waitTimer <= 0) {
                if (isSpawningAllowed()) {
                    spawnEnemy();
                }
                initTimer();
            }
        }
        
        protected void onWait(final int waitTimer) {
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
    }
    
    protected final static class BoulderSpawner extends EnemySpawner {
        protected BoulderSpawner(final Segment seg) {
            super(seg);
        }
        
        @Override
        protected final void onWait(final int waitTimer) {
            Enemy.onBoulderWait(this, waitTimer, 8);
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
    
    protected final static String VAR_LAUNCH_RETURN_ROOM_X = "launchReturnRoomX";
    protected final static String VAR_LAUNCH_RETURN_ROOM_Y = "launchReturnRoomY";
    protected final static String VAR_LAUNCH_RETURN_POS_X = "launchReturnPosX";
    protected final static String VAR_LAUNCH_RETURN_POS_Y = "launchReturnPosY";
    protected final static String VAR_LAUNCH_RETURN_POS_MIRROR = "launchReturnPosMirror";
    
    protected final static class LaunchCapsule extends Extra implements StepListener {
        private static Panmage img = null;
        private final static Panmage[] baseImgs = new Panmage[3];
        private final TileMap tm;
        private final String activeUntil;
        private final boolean active;
        private final int dstX;
        private final int dstY;
        private boolean allowed = true;
        private Player occupant = null;
        private int zLeft;
        private int zRight;
        private boolean launched = false;
        
        protected LaunchCapsule(final Segment seg) {
            super(seg, BotsnBoltsGame.DEPTH_BG);
            tm = BotsnBoltsGame.tm;
            activeUntil = seg.getValue(5);
            active = (activeUntil == null) || !RoomLoader.levelVariables.containsKey(activeUntil);
            dstX = seg.intValue(3);
            dstY = seg.intValue(4);
            for (final Field f : Coltil.unnull(seg.getRepetitions(6))) {
                if (!RoomLoader.levelVariables.containsKey(f.getValue())) {
                    allowed = false;
                    return;
                }
            }
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
        protected final boolean isAllowed() {
            return allowed;
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
            final Player player = getCollidingPlayer(this);
            if (player != null) {
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
                if (activeUntil != null) {
                    final Panple ppos = occupant.getPosition();
                    final BotRoom room = RoomLoader.getRoom();
                    RoomLoader.levelVariables.put(VAR_LAUNCH_RETURN_ROOM_X, Integer.toString(room.x));
                    RoomLoader.levelVariables.put(VAR_LAUNCH_RETURN_ROOM_Y, Integer.toString(room.y));
                    RoomLoader.levelVariables.put(VAR_LAUNCH_RETURN_POS_X, Integer.toString(Math.round(ppos.getX())));
                    RoomLoader.levelVariables.put(VAR_LAUNCH_RETURN_POS_Y, Integer.toString(Math.round(ppos.getY())));
                    RoomLoader.levelVariables.put(VAR_LAUNCH_RETURN_POS_MIRROR, Boolean.toString(!occupant.isMirror()));
                }
                launched = true;
                occupant.launch(dstX, dstY);
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
    
    protected final static class Decoration extends Extra {
        @Override
        protected final boolean isVisibleWhileRoomChanging() {
            return true;
        }
    }
    
    protected abstract static class FinalBossSpawner extends Extra implements StepListener {
        protected final Segment seg;
        protected boolean spawned = false;
        
        protected FinalBossSpawner(final Segment seg) {
            super(seg, 0);
            this.seg = seg;
        }

        @Override
        public final void onStep(final StepEvent event) {
            onStepSpawner();
            if (spawned) {
                return;
            }
            BotsnBoltsGame.runPlayers(new PlayerRunnable() {
                @Override
                public final void run(final Player player) {
                    if (spawned) {
                        return;
                    }
                    spawnIfNeeded(player);
                }
            });
        }

        //@OverrideMe
        protected void onStepSpawner() {
        }
        
        private final void spawnIfNeeded(final Player player) {
            if (player.getPosition().getX() <= 32) {
                spawn();
                spawned = true;
                player.setMirror(false);
                Boss.setPlayerActive(false);
            }
        }
        
        protected abstract void spawn();
    }
    
    protected final static class FinalWagonSpawner extends FinalBossSpawner {
        protected FinalWagonSpawner(final Segment seg) {
            super(seg);
        }
        
        @Override
        protected final void spawn() {
            BotsnBoltsGame.addActor(new FinalWagon(seg));
            destroy();
        }
    }
    
    protected final static class FinalHeadSpawner extends FinalBossSpawner {
        private FinalHead head = null;
        private int timer = 0;
        private int clawIndex = 0;
        
        protected FinalHeadSpawner(final Segment seg) {
            super(seg);
        }
        
        @Override
        protected final boolean isVisibleWhileRoomChanging() {
            return true;
        }
        
        @Override
        protected final void onStepSpawner() {
            if (!spawned) {
                return;
            } else if (clawIndex < 3) {
                onStepClaw();
                return;
            }
            onStepOpen();
        }
        
        private final void onStepClaw() {
            if (timer < 30) {
                timer++;
                return;
            }
            timer = 0;
            clawIndex++;
        }
        
        private final void onStepOpen() {
            head.visibleSize++;
            if (head.visibleSize > 87) {
                for (int y = 0; y < BotsnBoltsGame.GAME_ROWS; y++) {
                    ShootableDoor.disableOverlay(0, y);
                    ShootableDoor.disableOverlay(23, y);
                }
                destroy();
            }
        }
        
        @Override
        protected final void spawn() {
            for (int y = 0; y < BotsnBoltsGame.GAME_ROWS; y++) {
                ShootableDoor.enableOverlay(0, y);
                ShootableDoor.enableOverlay(23, y);
            }
            BotsnBoltsGame.addActor(head = new FinalHead(seg));
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panlayer layer = getLayer();
            final Panmage wall = FinalHead.getWall();
            final int off = (head == null) ? 0 : (head.visibleSize * 2);
            final float z = BotsnBoltsGame.DEPTH_CARRIER;
            renderer.render(layer, wall, 64 - off, 32, z, 0, 0, 128, 128, 0, false, false);
            renderer.render(layer, wall, 64 - off, 160, z, 0, 0, 128, 64, 0, false, false);
            renderer.render(layer, wall, 192 + off, 32, z, 0, 64, 128, 64, 0, false, false);
            renderer.render(layer, wall, 192 + off, 96, z, 0, 0, 128, 128, 0, false, false);
            if (clawIndex < 1) {
                return;
            }
            final Panmage claw = FinalHead.getClawRip();
            final float zc = BotsnBoltsGame.DEPTH_POWER_UP;
            final int clawLeftX = 174 - off, clawBottomY = 112, clawTopY = 144;
            renderer.render(layer, claw, clawLeftX, clawBottomY, zc, 0, 0, 16, 16, 0, false, false);
            renderer.render(layer, claw, clawLeftX, clawTopY, zc, 0, 0, 16, 16, 0, false, false);
            if (clawIndex < 2) {
                return;
            }
            final int clawRightX = 194 + off;
            renderer.render(layer, claw, clawRightX, clawBottomY, zc, 0, 0, 16, 16, 0, true, false);
            renderer.render(layer, claw, clawRightX, clawTopY, zc, 0, 0, 16, 16, 0, true, false);
        }
    }
    
    protected static interface Warpable extends SpecPanctor {
        public void onMaterialized();
        
        public void onUnwarped();
    }
}
