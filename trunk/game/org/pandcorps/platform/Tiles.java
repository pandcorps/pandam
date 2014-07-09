/*
Copyright (c) 2009-2014, Andrew M. Martin
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
package org.pandcorps.platform;

//import javax.sound.midi.*;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.boundary.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;

public class Tiles {
    protected final static FinPanple g = new FinPanple(0, Player.g, 0);
    private final static TileHandler defHandler = new TileHandler();
    
    protected static class TileHandler {
    	protected boolean isNormalAward(final Tile t) {
    		return Level.isFlash(t);
    	}
    	
    	protected int rndAward() {
    		return GemBumped.rndAward();
    	}
    	
    	protected TileMapImage getBumpedImage() {
    		return Level.imgMap[0][4];
    	}
    }
    
    protected final static TileHandler getHandler() {
    	if (Panscreen.get().getClass() == Cabin.CabinScreen.class) {
    		return Cabin.cabinTileHandler;
    	}
    	return defHandler;
    }
    
    private final static GemBumped newGemBumped(final Player player, final int index) {
		return new GemBumped(player, index, getHandler().rndAward());
	}
    
    protected final static void bump(final Character chr, final int index) {
    	if (chr.getClass() != Player.class) {
    		return;
    	}
    	final Player player = (Player) chr;
    	final Tile t = Level.tm.getTile(index);
    	final byte b = t.getBehavior();
    	//if isMusicSupported final Sequence seq;
    	if (b == PlatformGame.TILE_BREAK) {
    		t.setForeground(null, false);
    		shatter(PlatformGame.block8, Level.tm.getPosition(index), false);
    		if (Mathtil.rand()) {
    		    newGemBumped(player, index); // Plays a sound
    		    //if isMusicSupported seq = null;
    		} else {
    			//if isMusicSupported seq = Music.crumble;
    		}
    		new Bump(chr, index).setVisible(false); // To bump Characters above
    		player.pc.profile.stats.brokenBlocks++;
    	} else if (b == PlatformGame.TILE_BUMP) {
    	    new Bump(chr, index); // Copy image before changing
    	    if (getHandler().isNormalAward(t)) {
    	        newGemBumped(player, index);
    	    } else {
    	        GemBumped.newLevelEnd(player, index);
    	        PlatformGame.levelVictory();
    	    }
    		t.setForeground(null, true);
    		player.pc.profile.stats.bumpedBlocks++;
    		//if isMusicSupported seq = null;
    	} else {
    		//if isMusicSupported seq = Music.thud;
    	}
    	//if isMusicSupported Pangine.getEngine().getMusic().playSound(seq);
    }
    
    public static class Faller extends Pandy implements AllOobListener {
        public Faller(final Panmage img, final float x, final float y, final float xv, final float yv) {
            super(g);
            setView(img);
            PlatformGame.setPosition(this, x, y, PlatformGame.DEPTH_SHATTER);
            getVelocity().set(xv, yv);
            PlatformGame.room.addActor(this);
        }

        @Override
        public final void onAllOob(final AllOobEvent event) {
            destroy();
        }
    }
    
    protected final static void shatter(final Panmage img, final Panple pos, final boolean rot) {
        final float x = pos.getX(), y = pos.getY();
        new Shatter(img, x, y, -2, 2).setMirror(rot);
        new Shatter(img, x + 8, y, 2, 2);
        new Shatter(img, x, y + 8, -1, 3).setRot(rot ? 2 : 0);
        new Shatter(img, x + 8, y + 8, 1, 3).setFlip(rot);
    }
    
    private final static class Shatter extends Faller {
    	private Shatter(final Panmage img, final float x, final float y, final int xm, final int ym) {
    		super(img, x, y, xm * Mathtil.randf(0.7f, 1.3f), ym * Mathtil.randf(0.7f, 1.3f));
    	}
    }
    
    private final static class Bump extends TileActor implements StepListener, CollisionListener {
    	private final Character bumper;
    	private final int index;
        private int age = 0;
        
        private Bump(final Character bumper, final int index) {
        	this.bumper = bumper;
        	this.index = index;
        	setViewFromForeground(Level.tm, Level.tm.getTile(index));
            final Panple pos = Level.tm.getPosition(index);
            PlatformGame.setPosition(this, pos.getX(), pos.getY() + 2, PlatformGame.DEPTH_SHATTER);
            PlatformGame.room.addActor(this);
        }

        @Override
        public final void onStep(final StepEvent event) {
            if (age < 2) {
                getPosition().addY(2);
            } else if (age < 5) {
                getPosition().addY(-1);
            } else {
                destroy();
                if (isVisible()) {
                	Level.tm.getTile(index).setForeground(getHandler().getBumpedImage());
                }
                return;
            }
            age++;
        }
        
        @Override
        public final void onCollision(final CollisionEvent event) {
            if (age > 1) {
                return; // Looks strange if Enemy walks onto tile shortly after bump and gets hit
            }
        	final Collidable c = event.getCollider();
        	if (c instanceof Character) {
        		((Character) c).onBump(bumper);
        	}
        }
    }
}
