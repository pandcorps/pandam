/*
Copyright (c) 2009-2022, Andrew M. Martin
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

import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.tile.*;

public abstract class Chr extends GuyPlatform {
    protected final static FinPanple2 gTuple = new FinPanple2(0, g);
    public final static float gWater = -0.3f;
    public final static float gWallSlide = -0.1f;
    public final static float gWallSlideWater = -0.05f;
    protected final static long NULL_CLOCK = -1000;
    
    protected Chr(final int offX, final int h) {
        super(offX, h);
    }

    @Override
    protected boolean onFell() {
        return false;
    }

    @Override
    protected void onBump(final int t) {
    }

    @Override
    protected final TileMap getTileMap() {
        return BotsnBoltsGame.tm;
    }

    @Override
    protected final boolean isSolidBehavior(final byte b) {
        return isCustomSolidBehavior(b);
    }
    
    protected final static boolean isCustomSolidBehavior(final byte b) {
        return (b == BotsnBoltsGame.TILE_CRUMBLE) || (b == BotsnBoltsGame.TILE_CONVEYOR_LEFT) || (b == BotsnBoltsGame.TILE_CONVEYOR_RIGHT)
                || (b == BotsnBoltsGame.TILE_PRESSURE_FIRE) || (b == BotsnBoltsGame.TILE_BURSTABLE);
    }
    
    protected final static boolean isSolidIndex(final int index) {
        return isSolidTile(BotsnBoltsGame.tm.getTile(index));
    }
    
    protected final static boolean isSolidTile(final int i, final int j) {
        return isSolidTile(BotsnBoltsGame.tm.getTile(i, j));
    }
    
    protected final static boolean isSolidTile(final Tile tile) {
        return isAnySolidBehavior(Tile.getBehavior(tile));
    }
    
    protected final static boolean isAnySolidBehavior(final byte b) {
        return (b == Tile.BEHAVIOR_SOLID) || (b == TILE_ICE) || isCustomSolidBehavior(b);
    }
    
    protected final int getIdentitySolid(final int off) {
        return getSolid(off, true);
    }
    
    protected final static boolean isDangerousTile(final int i, final int j) {
        return isDangerousTile(BotsnBoltsGame.tm.getTile(i, j));
    }
    
    protected final static boolean isDangerousTile(final Tile tile) {
        return isDangerousBehavior(Tile.getBehavior(tile));
    }
    
    protected final static boolean isDangerousBehavior(final byte b) {
        return (b == BotsnBoltsGame.TILE_DEFEAT) || (b == BotsnBoltsGame.TILE_HURT);
    }
    
    @Override
    protected final boolean isFloorBehavior(final byte b) {
        return (b == BotsnBoltsGame.TILE_FLOOR) || (b == BotsnBoltsGame.TILE_LADDER_TOP);
    }
    
    protected final boolean isUnderWater() {
        return (RoomLoader.waterLevel > 0) && (getPosition().getY() < RoomLoader.waterLevel);
    }
    
    @Override
    protected float getG() {
        return isUnderWater() ? gWater : g;
    }
    
    @Override
    protected final float getMinV() {
        return isUnderWater() ? -4 : -MAX_V;
    }
    
    protected final boolean isAdjacentRoomBelow() {
        return RoomLoader.getAdjacentRoom(this, 0, -1) != null;
    }
    
    protected final void fixX() {
        if (!isJumpPossible()) {
            getPosition().addX(getMirrorMultiplier());
        }
    }
    
    protected final static long getClock() {
        return Pangine.getEngine().getClock();
    }
    
    protected static interface SpecEnemy extends CollisionListener {
        public boolean isVulnerable();
        
        public void onShot(final Projectile prj);
        
        public boolean isHarmful();
        
        public void onAttack(final Player player);
        
        public int getHealth();
        
        public void setHealth(final int health);
        
        public boolean isBurstNeeded();
        
        public void award(final Player player);
        
        public void onDefeat(final Player player);
        
        public boolean isDestroyedAfterDefeat();
    }
    
    protected static interface SpecBoss extends SpecEnemy {
        public void onHealthMaxDisplayReached();
        
        public void onAward(final Player player);
        
        public boolean isLaunchPossible();
        
        public int pickResponseToDanger();
    }
}
