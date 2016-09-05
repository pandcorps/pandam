/*
Copyright (c) 2009-2016, Andrew M. Martin
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

import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.tile.*;

public class ShootableDoor extends Panctor implements StepListener, CollisionListener {
    private final static Panple min = new FinPanple2(-12, 0);
    private final static Panple max = new FinPanple2(12, 64);
    private final static Panple maxSmall = new FinPanple2(12, 16);
    private final static Panple minBarrier = new FinPanple2(2, 0);
    private final static Panple maxBarrier = new FinPanple2(14, 32);
    private final static DoorDisplay display = new DoorDisplay();
    private final static SmallDoorDisplay displaySmall = new SmallDoorDisplay();
    private final static BarrierDisplay displayBarrier = new BarrierDisplay();
    protected final int x;
    protected final int y;
    private final int doorX;
    protected ShootableDoorDefinition def = null;
    private int temperature = 0;
    
    protected ShootableDoor(final int x, final int y, ShootableDoorDefinition def) {
        final TileMap tm = BotsnBoltsGame.tm;
        tm.getLayer().addActor(this);
        this.x = x;
        this.y = y;
        this.def = def;
        final Panple pos = getPosition();
        tm.savePosition(pos, x, y);
        initPosition(pos);
        doorX = getDoorX();
        init();
        closeDoor();
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
        setDoorTiles(x, isSmall() ? BotsnBoltsGame.doorTunnelSmall : BotsnBoltsGame.doorTunnel, Tile.BEHAVIOR_SOLID, true);
    }
    
    private final boolean isSmall() {
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
    }
    
    private final void setDoorEnergyTiles(final Panframe[] door) {
        setDoorTiles(doorX, door, Tile.BEHAVIOR_OPEN, false);
    }
    
    protected void openDoor() {
        final TileMap tm = BotsnBoltsGame.tm;
        final int base = getBaseFrameIndex();
        final Panframe[] opening = def.opening[0];
        final Panframe[] doorTunnelOverlay = isSmall() ? BotsnBoltsGame.doorTunnelSmallOverlay : BotsnBoltsGame.doorTunnelOverlay;
        final int n = doorTunnelOverlay.length / 2;
        for (int j = 0; j < n; j++) {
            final int yj = y + j, basej = base + j;
            tm.setBackground(x, yj, doorTunnelOverlay[basej], Tile.BEHAVIOR_OPEN);
            tm.setForeground(doorX, yj, opening[basej]);
        }
        addOpenTimer(1);
        destroy();
    }
    
    private final void addOpenTimer(final int nextIndex) {
        Pangine.getEngine().addTimer(BotsnBoltsGame.tm, 1, new TimerListener() {
            @Override public final void onTimer(final TimerEvent event) {
                if (nextIndex >= def.opening.length) {
                    for (int j = 0; j < 4; j++) {
                        BotsnBoltsGame.tm.setForeground(doorX, y + j, null);
                    }
                } else {
                    setDoorEnergyTiles(def.opening[nextIndex]);
                    addOpenTimer(nextIndex + 1);
                }
            }});
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
        if (collider.getClass() == Projectile.class) {
            final Projectile projectile = (Projectile) collider;
            final int projectilePower = projectile.power;
            if (projectilePower <= 0) {
                return;
            }
            temperature += (5 * projectilePower);
            if (temperature >= def.nextTemperature
                    && (def.requiredShootMode == null || def.requiredShootMode == projectile.shootMode)
                    && (def.requiredPower == null || def.requiredPower.intValue() <= projectilePower)) {
                if (def.next == null) {
                    openDoor();
                } else {
                    setDefinition(def.next);
                }
            } else {
                temperature = def.nextTemperature;
            }
            projectile.burst();
            collider.destroy();
        }
    }
    
    private final void setDefinition(final ShootableDoorDefinition def) {
        this.def = def;
        closeDoor();
    }
    
    @Override
    public Pansplay getCurrentDisplay() {
        return isSmall() ? displaySmall : display;
    }
    
    private static class DoorDisplay implements Pansplay {
        @Override
        public final Panple getOrigin() {
            return FinPanple.ORIGIN;
        }
    
        @Override
        public final Panple getBoundingMinimum() {
            return min;
        }
    
        @Override
        public Panple getBoundingMaximum() {
            return max;
        }
    }
    
    private final static class SmallDoorDisplay extends DoorDisplay {
        @Override
        public final Panple getBoundingMaximum() {
            return maxSmall;
        }
    }
    
    protected final static class ShootableDoorDefinition {
        private final Panframe[] door;
        private final Panframe[][] opening;
        private final ShootableDoorDefinition next;
        private final int nextTemperature;
        private final ShootMode requiredShootMode;
        private final Integer requiredPower;
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
        private int openIndex = 0;
        
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
            tm.setBehavior(x, y + 1, b);
        }
        
        @Override
        protected final void closeDoor() {
        }
        
        @Override
        protected final void openDoor() {
            setBehavior(Tile.BEHAVIOR_OPEN);
            openIndex = 2;
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
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
            final Panlayer layer = getLayer();
            final Panmage img = def.barrierImgs[imgIndex];
            final Panple pos = getPosition();
            final float x = pos.getX() + off, y = pos.getY();
            for (int j = 0; j < 4; j++) {
                renderer.render(layer, img, x, y + (j * 8), BotsnBoltsGame.DEPTH_FG);
            }
        }
        
        @Override
        public final Pansplay getCurrentDisplay() {
            return displayBarrier;
        }
    }
    
    private static class BarrierDisplay implements Pansplay {
        @Override
        public final Panple getOrigin() {
            return FinPanple.ORIGIN;
        }
    
        @Override
        public final Panple getBoundingMinimum() {
            return minBarrier;
        }
    
        @Override
        public Panple getBoundingMaximum() {
            return maxBarrier;
        }
    }
}
