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
package org.pandcorps.furguardians;

import org.pandcorps.core.*;
import org.pandcorps.furguardians.Player.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;

public final class BombScreen extends MiniGameScreen {
    private final static int DIM = 16;
    private final static int PLAYABLE_COLS = 13;
    private final static int PLAYABLE_ROWS = 11;
    private final static int TOTAL_COLS = PLAYABLE_COLS + 2; // Playing field + left border + right border
    private final static int TOTAL_ROWS = PLAYABLE_ROWS + 3; // Playing field + top border + low border + HUD
    private final static int SCREEN_H = 224;
    private final static int DEPTH_BG = 0;
    private final static int DEPTH_BOMB = 1;
    private final static int DEPTH_PLAYER = 2;
    private final static int DEPTH_BURST = 3;
    private static Panroom room = null;
    private static Panmage img = null;
    private static TileMap tm = null;
    private static TileMapImage[][] imgMap = null;
    
    @Override
    protected final void load() throws Exception {
        room = initMiniZoom(SCREEN_H);
        img = Pangine.getEngine().createImage(Pantil.vmid(), FurGuardiansGame.RES + "bg/Tiles.png");
        tm = new TileMap(Pantil.vmid(), TOTAL_COLS, TOTAL_ROWS, DIM, DIM);
        imgMap = tm.splitImageMap(img);
        room.addActor(tm);
        tm.getPosition().set((Pangine.getEngine().getEffectiveWidth() - (TOTAL_COLS * DIM)) / 2, 0, DEPTH_BG);
        buildBorder();
    }
    
    @Override
    protected final void destroy() {
        Panmage.destroy(img);
    }
    
    private final void buildBorder() {
        final int yMin = 0, yMax = PLAYABLE_ROWS + 1, xMax = PLAYABLE_COLS + 1;
        final Tile tile = tm.getTile(imgMap[0][4], null, Tile.BEHAVIOR_SOLID);
        for (int i = 1; i < xMax; i++) {
            tm.setTile(i, yMin, tile);
            tm.setTile(i, yMax, tile);
        }
        for (int j = yMin; j <= yMax; j++) {
            tm.setTile(0, j, tile);
            tm.setTile(xMax, j, tile);
        }
    }
    
    private abstract static class BurstListener extends Panctor implements CollisionListener {
        @Override
        public final void onCollision(final CollisionEvent event) {
            final Collidable collider = event.getCollider();
            if (collider.getClass() == Burst.class) {
                onBurst((Burst) collider);
            }
        }
        
        protected abstract void onBurst(final Burst burst);
    }
    
    protected final static class BombGuy extends BurstListener implements StepListener {
        private final PlayerContext pc;
        private int radius = 1;
        private Direction dir = Direction.South;
        private Panimation anm = null;
        private boolean moving = false;
        
        protected BombGuy(final PlayerContext pc, final int x, final int y) {
            this.pc = pc;
            setView(pc.mapSouth);
            setDir(Direction.South, pc.mapSouth);
            final Panple pos = getPosition();
            tm.savePosition(pos, x, y);
            pos.setZ(DEPTH_PLAYER);
            room.addActor(this);
            initControlScheme();
        }
        
        private final void initControlScheme() {
            final ControlScheme ctrl = pc.ctrl;
            register(ctrl.getLeft(), new ActionListener() {
                @Override public final void onAction(final ActionEvent event) { onLeft(); }});
            register(ctrl.getRight(), new ActionListener() {
                @Override public final void onAction(final ActionEvent event) { onRight(); }});
            register(ctrl.getUp(), new ActionListener() {
                @Override public final void onAction(final ActionEvent event) { onUp(); }});
            register(ctrl.getDown(), new ActionListener() {
                @Override public final void onAction(final ActionEvent event) { onDown(); }});
        }
        
        private final void onLeft() {
            move(Direction.West, pc.mapWest);
        }
        
        private final void onRight() {
            move(Direction.East, pc.mapEast);
        }
        
        private final void onUp() {
            move(Direction.North, pc.mapNorth);
        }
        
        private final void onDown() {
            move(Direction.South, pc.mapSouth);
        }
        
        private final void move(final Direction dir, final Panimation anm) {
            setDir(dir, anm);
            moving = true;
        }
        
        private final void setDir(final Direction dir, final Panimation anm) {
            this.dir = dir;
            this.anm = anm;
        }
        
        @Override
        protected final void onBurst(final Burst burst) {
        }

        @Override
        public final void onStep(final StepEvent event) {
            if (moving) {
                onMoving();
                moving = false;
            } else {
                onStill();
            }
        }
        
        private final void onMoving() {
            changeView(anm);
        }
        
        private final void onStill() {
            changeView(anm.getFrames()[0].getImage());
        }
    }
    
    protected final static class Bomb extends BurstListener {
        protected final BombGuy guy;
        
        protected Bomb(final BombGuy guy) {
            this.guy = guy;
            final Panple pos = getPosition();
            tm.savePosition(pos, tm.getContainer(guy));
            pos.setZ(DEPTH_BOMB);
            room.addActor(this);
        }
        
        @Override
        protected final void onBurst(final Burst burst) {
        }
    }
    
    protected final static class Burst extends Panctor implements Collidable {
        protected final BombGuy guy;
        
        protected Burst(final BombGuy guy) {
            this.guy = guy;
            room.addActor(this);
        }
        
        private final void scheduleGrow(final int radius, final Direction dir) {
            if (radius > 0) {
                Pangine.getEngine().addTimer(this, 2, new TimerListener() {
                    @Override public final void onTimer(final TimerEvent event) {
                        grow(radius, dir);
                    }});
            }
        }
        
        private final void grow(final int radius, final Direction dir) {
            final int index = tm.getContainer(this), nextRadius = radius - 1;
            if (dir == null) {
                for (final Direction d : Direction.values()) {
                    newBurst(index, guy, nextRadius, d);
                }
            } else {
                newBurst(index, guy, nextRadius, dir);
            }
        }
        
        protected Burst(final Bomb bomb) {
            this(bomb.guy);
            final Panple pos = getPosition();
            pos.set(bomb.getPosition());
            pos.setZ(DEPTH_BURST);
            scheduleGrow(bomb.guy.radius, null);
        }
        
        protected final static Burst newBurst(final int index, final BombGuy guy, final int radius, final Direction dir) {
            final int nextIndex = tm.getNeighbor(index, dir);
            if (Tile.getBehavior(tm.getTile(nextIndex)) == Tile.BEHAVIOR_SOLID) {
                return null;
            }
            final Burst burst = new Burst(guy);
            final Panple pos = burst.getPosition();
            tm.savePosition(pos, nextIndex);
            pos.setZ(DEPTH_BURST);
            burst.scheduleGrow(radius, dir);
            return burst;
        }
    }
}
