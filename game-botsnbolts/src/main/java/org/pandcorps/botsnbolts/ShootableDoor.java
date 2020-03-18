/*
Copyright (c) 2009-2020, Andrew M. Martin
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

import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.pandax.visual.*;

public class ShootableDoor extends Panctor implements StepListener, CollisionListener {
    protected final static Panple minBarrier = new FinPanple2(1, 0);
    private final static Pansplay display;
    private final static Pansplay displaySmall;
    private final static Pansplay displayBarrier = new ImplPansplay(FinPanple.ORIGIN, minBarrier, new FinPanple2(14, 32));
    private final static Pansplay displayBarrierSmall = new ImplPansplay(FinPanple.ORIGIN, minBarrier, new FinPanple2(14, 16));
    private final static Pansplay displayBoss = new ImplPansplay(FinPanple.ORIGIN, minBarrier, new FinPanple2(14, 64));
    private final static Pansplay displayBolt = new ImplPansplay(FinPanple.ORIGIN, new FinPanple2(1, 32), new FinPanple2(30, 96));
    private final TileMap tm;
    protected final int x;
    protected final int y;
    private final int doorX;
    protected ShootableDoorDefinition def = null;
    private int temperature = 0;
    
    static {
        final Panple min = new FinPanple2(-12, 0);
        display = new ImplPansplay(FinPanple.ORIGIN, min, new FinPanple2(12, 64));
        displaySmall = new ImplPansplay(FinPanple.ORIGIN, min, new FinPanple2(12, 16));
    }
    
    protected ShootableDoor(final int x, final int y, ShootableDoorDefinition def) {
        tm = BotsnBoltsGame.tm;
        tm.getLayer().addActor(this);
        this.x = x;
        this.y = y;
        this.def = def;
        final Panple pos = getPosition();
        tm.savePosition(pos, x, y);
        initPosition(pos);
        doorX = getDoorX();
        init();
    }
    
    protected void initPosition(final Panple pos) {
        pos.addX(8);
    }
    
    protected int getDoorX() {
        if (x == 0) {
            return 1;
        } else {
            setMirror(true);
            return x - 1;
        }
    }
    
    protected void init() {
        setVisible(false);
        closeTunnel();
        openTunnel();
    }
    
    private final void closeTunnel() {
        final boolean small = isSmall();
        setDoorTiles(x, small ? BotsnBoltsGame.doorTunnelSmall : BotsnBoltsGame.doorTunnel, Tile.BEHAVIOR_SOLID, true);
        disableOverlay(x, y - 1);
        disableOverlay(x, y + (small ? 1 : 4));
    }
    
    protected final boolean isSmall() {
        return def.door.length <= 2;
    }
    
    private final int getBaseFrameIndex() {
        return isMirror() ? (def.door.length / 2) : 0;
    }
    
    private final void setDoorTiles(final int x, final Panframe[] door, final byte behavior, final boolean bg) {
        final TileMap tm = BotsnBoltsGame.tm;
        final int base = getBaseFrameIndex(), n = door.length / 2;
        for (int j = 0; j < n; j++) {
            final int index = tm.getIndex(x, y + j);
            final Panframe frm = door[base + j];
            if (bg) {
                tm.setBackground(index, frm, behavior);
            } else  {
                tm.setForeground(index, frm, behavior);
            }
        }
    }
    
    protected void closeDoor() {
        setDoorEnergyTiles(def.door);
        closeTunnel();
    }
    
    private final void setDoorEnergyTiles(final Panframe[] door) {
        setDoorTiles(doorX, door, Tile.BEHAVIOR_OPEN, false);
    }
    
    protected int openTunnel() {
        final TileMap tm = BotsnBoltsGame.tm;
        final int base = getBaseFrameIndex();
        final Panframe[] doorTunnelOverlay = isSmall() ? BotsnBoltsGame.doorTunnelSmallOverlay : BotsnBoltsGame.doorTunnelOverlay;
        final int n = doorTunnelOverlay.length / 2;
        for (int j = 0; j < n; j++) {
            tm.setBackground(x, y + j, doorTunnelOverlay[base + j], Tile.BEHAVIOR_OPEN);
        }
        enableOverlay(x, y - 1);
        enableOverlay(x, y + n);
        return n;
    }
    
    protected final void enableOverlay(final int x, final int y) {
        final TileMap tm = BotsnBoltsGame.tm;
        final int index = tm.getIndex(x, y);
        final Object bgRaw = DynamicTileMap.getRawBackground(tm.getTile(index));
        if ((bgRaw == null) || (bgRaw.getClass() != TileMapImage.class)) {
            return;
        }
        final TileMapImage bg = (TileMapImage) bgRaw;
        tm.setBackground(index, new AdjustedTileMapImage(bg, BotsnBoltsGame.DEPTH_OVERLAY, 0, false, false));
    }
    
    protected final void disableOverlay(final int x, final int y) {
        final TileMap tm = BotsnBoltsGame.tm;
        final int index = tm.getIndex(x, y);
        final Object bgRaw = DynamicTileMap.getRawBackground(tm.getTile(index));
        if ((bgRaw == null) || (bgRaw.getClass() != AdjustedTileMapImage.class)) {
            return;
        }
        final AdjustedTileMapImage bg = (AdjustedTileMapImage) bgRaw;
        tm.setBackground(index, bg.getRaw());
    }
    
    protected void openDoor() {
        destroy(); // Do before changing tiles; onDestroy will clobber tile changes in this method
        final int n = openTunnel();
        final TileMap tm = BotsnBoltsGame.tm;
        final int base = getBaseFrameIndex();
        final Panframe[] opening = def.opening[0];
        for (int j = 0; j < n; j++) {
            tm.setForeground(doorX, y + j, opening[base + j]);
        }
        addOpenTimer(1);
        BotsnBoltsGame.fxDoor.startSound();
    }
    
    private final void addOpenTimer(final int nextIndex) {
        Pangine.getEngine().addTimer(BotsnBoltsGame.tm, 1, new TimerListener() {
            @Override public final void onTimer(final TimerEvent event) {
                if (nextIndex >= def.opening.length) {
                    setOpened(BotsnBoltsGame.tm);
                } else {
                    setDoorEnergyTiles(def.opening[nextIndex]);
                    addOpenTimer(nextIndex + 1);
                }
            }});
    }
    
    private final void setOpened(final TileMap tm) {
        final int n = isSmall() ? 1 : 4;
        for (int j = 0; j < n; j++) {
            tm.setForeground(doorX, y + j, null);
        }
    }
    
    @Override
    public void onDestroy() {
        if (tm != null) {
            setOpened(tm);
        }
    }
    
    @Override
    public final void onStep(final StepEvent event) {
        if (def.prev != null) {
            temperature--;
            if (temperature < def.prev.nextTemperature) {
                setDefinition(def.prev);
            }
        }
    }
    
    @Override
    public final void onCollision(final CollisionEvent event) {
        final Collidable collider = event.getCollider();
        if (collider instanceof Projectile) { // Projectile can have sub-classes like Explosion
            final Projectile projectile = (Projectile) collider;
            final int projectilePower = projectile.power;
            if (projectilePower <= 0) {
                return;
            }
            temperature += (5 * projectilePower);
            if (temperature >= def.nextTemperature
                    && (def.requiredShootMode == null || def.requiredShootMode == projectile.shootMode)) {
                if ((def.requiredPower != null && def.requiredPower.intValue() > projectilePower)) {
                    projectile.bounce();
                    return;
                } else if (def.next == null) {
                    openDoor();
                } else {
                    BotsnBoltsGame.fxImpact.startSound();
                    setDefinition(def.next);
                }
            } else {
                temperature = def.nextTemperature;
            }
            projectile.burst();
        }
    }
    
    protected final void setDefinition(final ShootableDoorDefinition def) {
        this.def = def;
        closeDoor();
    }
    
    @Override
    public Pansplay getCurrentDisplay() {
        return isSmall() ? displaySmall : display;
    }
    
    protected final static ShootableDoorDefinition getShootableDoorDefinition(final String doorType) {
        if ("Cyan".equals(doorType)) {
            return BotsnBoltsGame.doorCyan;
        } else if ("Gold".equals(doorType)) {
            return BotsnBoltsGame.doorGold;
        } else if ("Silver".equals(doorType)) {
            return BotsnBoltsGame.doorSilver;
        } else if ("Blue".equals(doorType)) {
            return BotsnBoltsGame.doorBlue;
        } else if ("Black".equals(doorType)) {
            return BotsnBoltsGame.doorBlack;
        } else if ("Small".equals(doorType)) {
            return BotsnBoltsGame.doorSmall;
        }
        throw new IllegalArgumentException("Unrecognized door type: " + doorType);
    }
    
    protected final static boolean isBossEntranceRoom() {
        return Coltil.size(RoomLoader.bossDoors) > 1;
    }
    
    private final static Player getPlayer() {
        return PlayerContext.getPlayer(BotsnBoltsGame.pc);
    }
    
    protected final static class ShootableDoorDefinition {
        private final Panframe[] door;
        private final Panframe[][] opening;
        private final ShootableDoorDefinition next;
        private final int nextTemperature;
        private final ShootMode requiredShootMode;
        protected final Integer requiredPower;
        private ShootableDoorDefinition prev = null;
        private final Panmage[] barrierImgs;
        
        protected ShootableDoorDefinition(final Panframe[] door, final Panframe[][] opening, final ShootableDoorDefinition next,
                                          final int nextTemperature, final ShootMode requiredShootMode, final Integer requiredPower,
                                          final Panmage[] barrierImgs) {
            this.door = door;
            this.opening = opening;
            this.next = next;
            this.nextTemperature = nextTemperature;
            this.requiredShootMode = requiredShootMode;
            this.requiredPower = requiredPower;
            this.barrierImgs = barrierImgs;
            if (next != null) {
                next.prev = this;
            }
        }
    }
    
    protected final static class ShootableBarrier extends ShootableDoor {
        private int openIndex = -1;
        
        protected ShootableBarrier(final int x, final int y, final ShootableDoorDefinition def) {
            super(x, y, def);
        }
        
        @Override
        protected final void initPosition(final Panple pos) {
        }
        
        @Override
        protected final int getDoorX() {
            return 0;
        }
        
        @Override
        protected final void init() {
            setBehavior(Tile.BEHAVIOR_SOLID);
        }
        
        private final void setBehavior(final byte b) {
            final TileMap tm = BotsnBoltsGame.tm;
            tm.setBehavior(x, y, b);
            if (!isSmall()) {
                tm.setBehavior(x, y + 1, b);
            }
        }
        
        @Override
        protected final void closeDoor() {
            openIndex = 0;
        }
        
        @Override
        protected final void openDoor() {
            setBehavior(Tile.BEHAVIOR_OPEN);
            openIndex = 2;
            BotsnBoltsGame.fxDoor.startSound();
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            if (openIndex < 0) {
                return;
            }
            final int indexLeft, indexRight;
            if (openIndex > 0) {
                if (openIndex >= def.barrierImgs.length) {
                    destroy();
                    return;
                }
                indexLeft = openIndex;
                indexRight = openIndex;
                openIndex++;
            } else {
                indexLeft = 0;
                indexRight = 1;
            }
            renderColumn(renderer, 0, indexLeft);
            renderColumn(renderer, 8, indexRight);
        }
        
        private final void renderColumn(final Panderer renderer, final int off, final int imgIndex) {
            final Panple pos = getPosition();
            renderColumn(renderer, getLayer(), def.barrierImgs, imgIndex, pos.getX() + off, pos.getY(), isSmall() ? 2 : 4);
        }
        
        protected final static void renderColumn(final Panderer renderer, final Panlayer layer, final Panmage[] imgs, final int imgIndex, final float x, final float y, final int h) {
            final Panmage img = imgs[imgIndex];
            for (int j = 0; j < h; j++) {
                renderer.render(layer, img, x, y + (j * 8), BotsnBoltsGame.DEPTH_FG);
            }
        }
        
        @Override
        public final Pansplay getCurrentDisplay() {
            return isSmall() ? displayBarrierSmall : displayBarrier;
        }
        
        @Override
        public final void onDestroy() {
        }
    }
    
    // Hidden/invisible barrier implemented in BlockPuzzle.HiddenBlockPuzzle
    
    protected final static class ShootableButton extends Panctor implements CollisionListener, AnimationEndListener {
        private final ShootableButtonHandler handler;
        
        protected ShootableButton(final int x, final int y, final ShootableButtonHandler handler) {
            this.handler = handler;
            setImage();
            final TileMap tm = BotsnBoltsGame.tm;
            tm.savePosition(getPosition(), x, y);
        }
        
        private final void setImage() {
            setView(BotsnBoltsGame.button);
        }
        
        @Override
        public final void onCollision(final CollisionEvent event) {
            final Collidable collider = event.getCollider();
            if (collider instanceof Projectile) {
                setView(BotsnBoltsGame.buttonFlash);
                final Projectile projectile = (Projectile) collider;
                handler.onShootButton();
                projectile.burst();
            }
        }

        @Override
        public final void onAnimationEnd(final AnimationEndEvent event) {
            setImage();
        }
    }
    
    protected static interface ShootableButtonHandler {
        public void onShootButton();
    }
    
    protected final static class DoorShootableButtonHandler implements ShootableButtonHandler {
        private final Collection<ShootableDoor> doors;
        
        protected DoorShootableButtonHandler() {
            this(RoomLoader.getButtonDoors());
        }
        
        protected DoorShootableButtonHandler(final Collection<ShootableDoor> doors) {
            this.doors = doors;
        }
        
        @Override
        public final void onShootButton() {
            for (final ShootableDoor door : doors) {
                door.openDoor();
            }
            doors.clear();
        }
    }
    
    protected final static class BossDoor extends Panctor {
        private final int h = 16;
        protected final int x;
        protected final int y;
        private final boolean leftToRight;
        private int base;
        private int vel = 0;
        private long startTime;
        
        protected BossDoor(final int x, final int y, final boolean leftToRight) {
            final TileMap tm = BotsnBoltsGame.tm;
            tm.getLayer().addActor(this);
            this.x = x;
            this.y = y;
            this.leftToRight = leftToRight;
            tm.savePosition(getPosition(), x, y);
            if (RoomLoader.changing && isEntryPoint()) {
                setBehavior(Tile.BEHAVIOR_OPEN);
                base = h;
            } else {
                setBehavior(Tile.BEHAVIOR_SOLID);
                base = 0;
            }
        }
        
        protected boolean isEntryPoint() {
            return isLeftToRight() ? (x == 0) : (x > 0);
        }
        
        protected boolean isLeftToRight() {
            return leftToRight;
        }
        
        private final void setBehavior(final byte b) {
            final TileMap tm = BotsnBoltsGame.tm;
            for (int j = 0; j < 4; j++) {
                tm.setBehavior(x, y + j, b);
            }
        }
        
        private final void start(final int vel) {
            this.vel = vel;
            startTime = Pangine.getEngine().getClock();
        }
        
        protected final void open() {
            if (isBossEntranceRoom() && !Boss.isLevelMusicPlayedDuringBoss()) {
                Pangine.getEngine().getAudio().stopMusic();
            }
            start(1);
            setBehavior(Tile.BEHAVIOR_OPEN);
        }
        
        protected final boolean isOpening() {
            return vel > 0;
        }
        
        protected final void close() {
            if (isClosed()) {
                return;
            }
            start(-1);
            setBehavior(Tile.BEHAVIOR_SOLID);
        }
        
        protected final boolean isClosing() {
            return vel < 0;
        }
        
        protected final boolean isClosed() {
            return base <= 0;
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panlayer layer = getLayer();
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY();
            if (vel != 0 && (Pangine.getEngine().getClock() - startTime) % 3 == 0) {
                base += vel;
                if (base < 0) {
                    base = 0;
                    vel = 0;
                } else if (base > h) {
                    base = h;
                    vel = 0;
                    getPlayer().onBossDoorOpened(this);
                } else {
                    BotsnBoltsGame.fxBossDoor.startSound();
                }
            }
            for (int j = base; j < h; j++) {
                renderer.render(layer, BotsnBoltsGame.getDoorBoss(), x, y + (j * 4), BotsnBoltsGame.DEPTH_FG);
            }
        }
        
        @Override
        public final Pansplay getCurrentDisplay() {
            return displayBoss;
        }
    }
    
    protected final static boolean isBigTileMode() {
        return BotsnBoltsGame.tm.getHeight() <= 7;
    }
    
    protected final static class BoltDoor extends Panctor implements CollisionListener {
        private final static int maxSize = 3;
        private final int x;
        private final int y;
        private final boolean m;
        private int size = 0;
        private boolean closeNeeded = true;
        
        protected BoltDoor(final int x, final int y) {
            final TileMap tm = BotsnBoltsGame.tm;
            tm.getLayer().addActor(this);
            this.x = x;
            this.y = y;
            tm.savePosition(getPosition(), x, y);
            m = (x > 0); // Panctor.setMirror impacts the bounding box; this doesn't
            final Tile tile = tm.getTile(null, null, Tile.BEHAVIOR_SOLID);
            setBehavior(0, tile, Tile.BEHAVIOR_SOLID);
            setBehavior(3, tile, Tile.BEHAVIOR_SOLID);
        }
        
        private final void setBehavior(final int yoff, final Tile tile, final byte b) {
            if (isBigTileMode()) {
                setBehavior(x, y + yoff, tile, b);
            } else {
                final int yBase = y + (yoff * 2);
                for (int j = 0; j < 2; j++) {
                    final int yj = yBase + j;
                    for (int i = 0; i < 2; i++) {
                        setBehavior(x + i, yj, tile, b);
                    }
                }
            }
        }
        
        private final static void setBehavior(final int x, final int y, final Tile tile, final byte b) {
            if (tile == null) {
                BotsnBoltsGame.tm.setBehavior(x, y, b);
            } else {
                BotsnBoltsGame.tm.setTile(x, y, tile);
            }
        }
        
        private final boolean isCloseable() {
            if (!closeNeeded) {
                return false;
            } else if (RoomChanger.isChanging()) {
                return false;
            }
            final Player player = getPlayer();
            final float doorCenterX = getPosition().getX() + 16;
            return (player == null) || (Math.abs(player.getPosition().getX() - doorCenterX) > 48);
        }
        
        protected void close() {
            if (!isCloseable()) {
                return;
            }
            setDoorBehavior(Tile.BEHAVIOR_SOLID);
            size = maxSize;
            closeNeeded = false;
        }
        
        private final void open() {
            setDoorBehavior(Tile.BEHAVIOR_OPEN);
            if (isClosed()) {
                size = maxSize - 1;
            }
            BotsnBoltsGame.fxDoor.startSound();
        }
        
        private final boolean isClosed() {
            return size >= maxSize;
        }
        
        private final void setDoorBehavior(final byte b) {
            for (int y = 1; y < 3; y++) {
                setBehavior(y, null, b);
            }
        }
        
        @Override
        public final void onCollision(final CollisionEvent event) {
            if (!isClosed()) {
                return;
            }
            final Collidable collider = event.getCollider();
            if (collider instanceof Projectile) {
                open();
                ((Projectile) collider).burst();
            }
        }
        
        @Override
        public Pansplay getCurrentDisplay() {
            return displayBolt;
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panlayer layer = getLayer();
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY();
            final int out, in;
            if (m) {
                out = 0;
                in = 16;
            } else {
                out = 16;
                in = 0;
            }
            final float xOut = x + out, xIn = x + in, y32 = y + 32;
            final Panmage gen = BotsnBoltsGame.getDoorBoltGenerator(), door = BotsnBoltsGame.pc.pi.doorBolt;
            renderer.render(layer, gen, x, y, BotsnBoltsGame.DEPTH_FG, 0, 0, 32, 32, 0, m, false);
            if (size > 0) {
                final boolean full = size >= 3;
                if (full) {
                    renderer.render(layer, door, xOut, y32, BotsnBoltsGame.DEPTH_FG, 0, 0, 16, 16, 0, m, false);
                    for (int i = 0; i < 16; i += 8) {
                        renderer.render(layer, door, xIn + i, y32, BotsnBoltsGame.DEPTH_FG, 0, 8, 8, 8, 0, false, false);
                    }
                }
                final int off = 8 * (3 - size), stop = 88 - off;
                for (int j = 40 + off; j < stop; j += 8) {
                    final float yj = y + j;
                    if (j > 40 && j < 80) {
                        renderer.render(layer, door, xOut, yj, BotsnBoltsGame.DEPTH_FG, 0, 0, 16, 8, 0, m, false);
                    }
                    for (int i = 0; i < 16; i += 8) {
                        renderer.render(layer, door, xIn + i, yj, BotsnBoltsGame.DEPTH_FG, 0, 0, 8, 8, 0, false, false);
                    }
                }
                if (full) {
                    renderer.render(layer, door, xOut, y + 80, BotsnBoltsGame.DEPTH_FG, 0, 0, 16, 16, 0, m, true);
                    for (int i = 0; i < 16; i += 8) {
                        renderer.render(layer, door, xIn + i, y + 88, BotsnBoltsGame.DEPTH_FG, 0, 8, 8, 8, 0, false, true);
                    }
                } else {
                    size--;
                }
            } else {
                close();
            }
            renderer.render(layer, gen, x, y + 96, BotsnBoltsGame.DEPTH_FG, 0, 0, 32, 32, 0, m, false);
        }
    }
}
