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

import org.pandcorps.core.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.boundary.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;

public class Tiles {
    protected final static FinPanple2 g = new FinPanple2(0, Player.g);
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
		return GemBumped.create(player, index, getHandler().rndAward());
	}
    
    protected final static void bump(final Character chr, final int index) {
    	if (chr.getClass() != Player.class) {
    		return;
    	}
    	final Player player = (Player) chr;
    	final Tile t = Level.tm.getTile(index);
    	final byte b = t.getBehavior();
    	if (b == PlatformGame.TILE_BREAK) {
    		Level.tm.setForeground(index, null, Tile.BEHAVIOR_OPEN);
    		shatterTile(PlatformGame.block8, Level.tm.getPosition(index), false);
    		if (Mathtil.rand(65)) {
    		    newGemBumped(player, index); // Plays a sound
    		} // else { // Players wanted to hear crumble even if Gem pops up with its own sound
    		PlatformGame.soundCrumble.startSound();
    		//}
    		new Bump(chr, index).setVisible(false); // To bump Characters above
    		player.pc.profile.stats.brokenBlocks++;
    	} else if (b == PlatformGame.TILE_BUMP) {
    	    if (getHandler().isNormalAward(t)) {
    	        newGemBumped(player, index);
    	    } else if (!bumpLetter(player, index, t)) {
    	        GemBumped.newLevelEnd(player, index);
    	        PlatformGame.levelVictory();
    	        PlatformGame.playTransition(PlatformGame.musicLevelEnd);
    	    }
    	    bump(player, index);
    	} else {
    		final long clock = Pangine.getEngine().getClock();
    		if (player.lastThud < (clock - 2)) {
	    		player.lastThud = clock;
	    		PlatformGame.soundThud.startSound();
    		}
    	}
    }
    
    private final static boolean bumpLetter(final Player player, final int index, final Tile t) {
    	Panmage letter = null;
	    final int size = PlatformGame.blockWord.length();
	    final Object fg = DynamicTileMap.getRawForeground(t);
	    int i = 0;
	    for (; i < size; i++) {
	    	letter = PlatformGame.getBlockWordLetter(i);
	        if (fg == letter) {
	        	boolean keep = true;
	        	for (final Panctor h : Coltil.unnull(Level.collectedLetters)) {
	        		if (/*h.getView() == letter &&*/ h.getPosition().getX() == getHudLetterX(i)) {
	        			keep = false;
	        			break;
	        		}
	        	}
	        	if (keep) {
	        		break;
	        	}
	        }
	    }
	    if (i >= size) {
	    	return false;
	    }
	    newGemLetter(player, index, PlatformGame.getGemWordLetter(i));
	    //final TileActor h = new TileActor();
	    //h.setViewFromForeground(Level.tm, t);
	    Level.collectedLetters = Coltil.add(Level.collectedLetters, addLetter(i, letter));
	    Panctor.destroy(Coltil.get(Level.uncollectedLetters, i));
	    return true;
    }
    
    protected final static void newGemLetter(final Player player, final int index, final Panmage img) {
    	GemBumped.create(player, index, 0, GemBumped.TYPE_LETTER, null).setView(img);
    }
    
    private final static int getHudLetterX(final int i) {
    	return (Pangine.getEngine().getEffectiveWidth() - ImtilX.DIM * PlatformGame.blockWord.length()) / 2 + ImtilX.DIM * i;
    }
    
    private final static int getHudLetterY() {
    	return Pangine.getEngine().getEffectiveHeight() - ImtilX.DIM - 1;
    }
    
    private final static Panctor addLetter(final int i, final Panmage letter) {
    	final Panctor h = new Panctor();
	    h.setView(letter);
	    h.getPosition().set(getHudLetterX(i), getHudLetterY());
	    PlatformGame.hud.addActor(h);
	    return h;
    }
    
    protected final static void initLetters() {
    	Coltil.clear(Level.uncollectedLetters);
    	final String word = PlatformGame.blockWord;
    	final int size = word.length();
    	for (int i = 0; i < size; i++) {
    		final Panctor h = addLetter(i, PlatformGame.getTranslucentBlockWordLetter(i));
    		Level.uncollectedLetters = Coltil.add(Level.uncollectedLetters, h);
    	}
    }
    
    private final static void bump(final Player player, final int index) {
        new Bump(player, index); // Copy image before changing
        Level.tm.setForeground(index, null, Tile.BEHAVIOR_SOLID);
        player.pc.profile.stats.bumpedBlocks++;
    }
    
    public static class Faller extends Pandy implements AllOobListener {
        public Faller(final Panlayer layer, final Panmage img, final float x, final float y, final float xv, final float yv) {
            super(g);
            setView(img);
            PlatformGame.setPosition(this, x, y, PlatformGame.DEPTH_SHATTER);
            getVelocity().set(xv, yv);
            layer.addActor(this);
        }
        
        public Faller(final Panmage img, final float x, final float y, final float xv, final float yv) {
        	this(PlatformGame.room, img, x, y, xv, yv);
        }

        @Override
        public final void onAllOob(final AllOobEvent event) {
            destroy();
        }
    }
    
    private final static void shatter(final Panlayer layer, final Panmage img, final Panple pos, final boolean rot, final int xoff) {
        final float x = pos.getX() + xoff, y = pos.getY();
        new Shatter(layer, img, x, y, -2, 2).setMirror(rot);
        new Shatter(layer, img, x + 8, y, 2, 2);
        new Shatter(layer, img, x, y + 8, -1, 3).setRot(rot ? 2 : 0);
        new Shatter(layer, img, x + 8, y + 8, 1, 3).setFlip(rot);
    }
    
    protected final static void shatterCenteredActor(final Panlayer layer, final Panmage img, final Panple pos, final boolean rot) {
        shatter(layer, img, pos, rot, -8);
    }
    
    protected final static void shatterTile(final Panlayer layer, final Panmage img, final Panple pos, final boolean rot) {
        shatter(layer, img, pos, rot, 0); // 0 is good for tiles; -8 better for centered actors
    }
    
    protected final static void shatterTile(final Panmage img, final Panple pos, final boolean rot) {
    	shatterTile(PlatformGame.room, img, pos, rot);
    }
    
    private final static class Shatter extends Faller {
    	private Shatter(final Panlayer layer, final Panmage img, final float x, final float y, final int xm, final int ym) {
    		super(layer, img, x, y, xm * Mathtil.randf(0.7f, 1.3f), ym * Mathtil.randf(0.7f, 1.3f));
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
                	Level.tm.setForeground(index, getHandler().getBumpedImage());
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
