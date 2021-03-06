/*
Copyright (c) 2009-2021, Andrew M. Martin
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
import org.pandcorps.furguardians.Profile.*;
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
    	protected boolean isNormalAward(final int index, final Tile t) {
    		return Level.isFlash(t);
    	}
    	
    	protected boolean handle(final int index, final Tile t) {
    	    return false;
    	}
    	
    	protected int rndAward(final Player player) {
    		return GemBumped.rndAward(player);
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
        final int award = getHandler().rndAward(player);
        if (FurGuardiansGame.level) {
            switch (award) {
                case GemBumped.AWARD_2 :
                    player.pc.profile.stats.foundBlueGems++;
                    break;
                case GemBumped.AWARD_3 :
                    player.pc.profile.stats.foundCyanGems++;
                    break;
                case GemBumped.AWARD_4 :
                    player.pc.profile.stats.foundGreenGems++;
                    break;
            }
        }
		return GemBumped.create(player, index, award);
	}
    
    protected final static byte bump(final Character chr, final int index) {
    	if (chr.getClass() != Player.class) {
    		return Tile.BEHAVIOR_OPEN;
    	}
    	final Player player = (Player) chr;
    	final Tile t = Level.tm.getTile(index);
    	final byte b = t.getBehavior();
    	if (b == FurGuardiansGame.TILE_BREAK) {
    	    final Statistics stats = player.pc.profile.stats;
    	    final Panmage shatterImg;
    	    final boolean shatterRot;
    	    if (DynamicTileMap.getRawForeground(t) != Level.breakableImg) {
    	        Level.tm.setForeground(index, Level.breakableImg);
    	        shatterImg = FurGuardiansGame.vineShatter;
    	        newGemBumped(player, index);
    	        stats.clearedVineBlocks++;
    	        shatterRot = true;
    	    } else {
        		Level.tm.setForeground(index, null, Tile.BEHAVIOR_OPEN);
        		shatterImg = FurGuardiansGame.block8;
        		if (Mathtil.rand(Level.breakableAwardProbability)) {
        		    newGemBumped(player, index); // Plays a sound
        		} // else { // Used to play soundCrumble in this else, but Players wanted to hear crumble even if Gem pops up with its own sound
        		stats.brokenBlocks++;
        		player.levelBrokenBlocks++;
        		shatterRot = false;
    	    }
    	    shatterTile(shatterImg, Level.tm.getPosition(index), shatterRot);
    	    FurGuardiansGame.soundCrumble.startSound();
            new Bump(chr, index, null).setVisible(false); // To bump Characters above
    	} else if (b == FurGuardiansGame.TILE_BUMP) {
    	    final TileHandler handler = getHandler();
    	    final Object fg = DynamicTileMap.getRawForeground(t);
    	    Object nextImage = null;
    	    if (fg == FurGuardiansGame.blockPower) {
    	        final Panmage orbImg;
    	        final byte power;
    	        final Statistics stats = player.pc.profile.stats;
    	        final int r;
    	        if (Level.goalLocked) {
    	            r = 1500;
    	        } else if (stats.foundLightningOrbs == 0) {
    	            r = 500;
    	        } else if (stats.foundDoubleOrbs == 0) {
    	            r = 1500;
    	        } else if (stats.electrocutedEnemies == 0) {
    	            r = 500;
    	        } else if (stats.doubledGems == 0) {
    	            r = 1500;
    	        } else {
    	            r = Mathtil.randi(0, 1999);
    	        }
    	        if (r < 1000) {
    	            orbImg = FurGuardiansGame.lightningOrb;
    	            power = Player.POWER_LIGHTNING;
    	            stats.foundLightningOrbs++;
    	        } else {
                    orbImg = FurGuardiansGame.doubleOrb;
                    power = Player.POWER_DOUBLE;
                    stats.foundDoubleOrbs++;
                }
    	        final GemBumped orb = newGemDecoration(player, index, orbImg);
    	        orb.duration += 12;
    	        orb.getVelocity().addY(2);
    	        Player.setPower(power);
    	    } else if (fg == FurGuardiansGame.netherCube1) {
    	        nextImage = FurGuardiansGame.netherCube2;
    	        bumpNetherCube(player, index);
    	    } else if (fg == FurGuardiansGame.netherCubeMirror1) {
                nextImage = FurGuardiansGame.netherCubeMirror2;
                bumpNetherCube(player, index);
    	    } else if (fg == FurGuardiansGame.netherCube2) {
    	        nextImage = FurGuardiansGame.netherCube3;
    	        bumpNetherCube(player, index);
    	    } else if (fg == FurGuardiansGame.netherCubeMirror2) {
                nextImage = FurGuardiansGame.netherCubeMirror3;
                bumpNetherCube(player, index);
    	    } else if (fg == FurGuardiansGame.netherCube3) {
    	        defeatNetherCube(player, index);
    	    } else if (fg == FurGuardiansGame.netherCubeMirror3) {
    	        defeatNetherCube(player, index);
    	    } else if (handler.isNormalAward(index, t)) {
    	        newGemBumped(player, index);
    	    } else if (!(handler.handle(index, t) || bumpLetter(player, index, t))) {
    	        player.onLevelVictory();
    	        GemBumped.newLevelEnd(player, index);
    	        FurGuardiansGame.levelVictory();
    	        FurGuardiansGame.playTransition(FurGuardiansGame.musicLevelEnd);
    	    }
    	    bump(player, index, nextImage);
    	} else {
    		final long clock = Pangine.getEngine().getClock();
    		if (player.lastThud < (clock - 2)) {
	    		player.lastThud = clock;
	    		FurGuardiansGame.soundThud.startSound();
    		}
    	}
    	return b;
    }
    
    private final static void bumpNetherCube(final Player player, final int index) {
        GemBumped.create(player, index, GemBumped.AWARD_2);
    }
    
    private final static void defeatNetherCube(final Player player, final int index) {
        bumpNetherCube(player, index);
        Enemy.countDefeat(player, FurGuardiansGame.netherCube, Enemy.DEFEAT_BUMP);
    }
    
    protected final static int getLetterIndex(final Panmage[] letters, final Tile t) {
    	Panmage letter = null;
	    final int size = FurGuardiansGame.blockWord.length();
	    final Object fg = DynamicTileMap.getRawForeground(t);
	    int i = 0;
	    for (; i < size; i++) {
	    	letter = FurGuardiansGame.getImageWordLetter(letters, i);
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
	    return (i >= size) ? -1 : i;
    }
    
    private final static boolean bumpLetter(final Player player, final int index, final Tile t) {
        final int i = getLetterIndex(FurGuardiansGame.blockLetters, t);
        if (i < 0) {
            return false;
        }
	    newGemDecoration(player, index, FurGuardiansGame.getGemWordLetter(i), i);
	    //final TileActor h = new TileActor();
	    //h.setViewFromForeground(Level.tm, t);
	    collectLetter(i);
	    return true;
    }
    
    protected final static void collectLetter(final int i) {
	    Level.collectedLetters = Coltil.add(Level.collectedLetters, addLetter(i, FurGuardiansGame.getBlockWordLetter(i)));
	    Panctor.destroy(Coltil.get(Level.uncollectedLetters, i));
    }
    
    protected final static GemBumped newGemDecoration(final Player player, final int index, final Panmage img) {
        return newGemDecoration(player, index, img, -1);
    }
    
    protected final static GemBumped newGemDecoration(final Player player, final int index, final Panmage img, final int letterIndex) {
    	final GemBumped gem = GemBumped.create(player, index, 0, GemBumped.TYPE_DECORATION, null, letterIndex);
    	gem.setView(img);
    	return gem;
    }
    
    private final static int getHudLetterX(final int i) {
    	return (Pangine.getEngine().getEffectiveWidth() - ImtilX.DIM * FurGuardiansGame.blockWord.length()) / 2 + ImtilX.DIM * i;
    }
    
    private final static int getHudLetterY() {
    	return Pangine.getEngine().getEffectiveTop() - ImtilX.DIM - 1;
    }
    
    private final static Panctor addLetter(final int i, final Panmage letter) {
    	final Panctor h = new Panctor();
	    h.setView(letter);
	    h.getPosition().set(getHudLetterX(i), getHudLetterY());
	    FurGuardiansGame.hud.addActor(h);
	    return h;
    }
    
    protected final static void initLetters() {
    	Coltil.clear(Level.uncollectedLetters);
    	final String word = FurGuardiansGame.blockWord;
    	final int size = word.length();
    	for (int i = 0; i < size; i++) {
    		final Panctor h = addLetter(i, FurGuardiansGame.getTranslucentBlockWordLetter(i));
    		Level.uncollectedLetters = Coltil.add(Level.uncollectedLetters, h);
    	}
    }
    
    private final static void bump(final Player player, final int index, final Object nextImage) {
        new Bump(player, index, nextImage); // Copy image before changing
        Level.tm.setForeground(index, null, Tile.BEHAVIOR_SOLID);
        player.pc.profile.stats.bumpedBlocks++;
    }
    
    //TODO See org.pandcorps.game.actor.Diver
    public static class Faller extends Pandy implements AllOobListener {
        public Faller(final Panlayer layer, final Panmage img, final float x, final float y, final float xv, final float yv) {
            super(g);
            setView(img);
            FurGuardiansGame.setPosition(this, x, y, FurGuardiansGame.DEPTH_SHATTER);
            getVelocity().set(xv, yv);
            layer.addActor(this);
        }
        
        public Faller(final Panmage img, final float x, final float y, final float xv, final float yv) {
        	this(FurGuardiansGame.room, img, x, y, xv, yv);
        }

        @Override
        public final void onAllOob(final AllOobEvent event) {
            destroy();
        }
    }
    
    protected static boolean shatterBottomLeft = true;
    protected static boolean shatterBottomRight = true;
    protected static boolean shatterTopLeft = true;
    protected static boolean shatterTopRight = true;
    
    private final static void shatter(final Panlayer layer, final Panmage img, final Panple pos, final boolean rot, final int xoff) {
        final float x = pos.getX() + xoff, y = pos.getY();
        if (shatterBottomLeft) {
        	new Shatter(layer, img, x, y, -2, 2).setMirror(rot);
        }
        if (shatterBottomRight) {
        	new Shatter(layer, img, x + 8, y, 2, 2);
        }
        if (shatterTopLeft) {
        	new Shatter(layer, img, x, y + 8, -1, 3).setRot(rot ? 2 : 0);
        }
        if (shatterTopRight) {
        	new Shatter(layer, img, x + 8, y + 8, 1, 3).setFlip(rot);
        }
    }
    
    protected final static void shatterCenteredActor(final Panlayer layer, final Panmage img, final Panple pos, final boolean rot) {
        shatter(layer, img, pos, rot, -8);
    }
    
    protected final static void shatterTile(final Panlayer layer, final Panmage img, final Panple pos, final boolean rot) {
        shatter(layer, img, pos, rot, 0); // 0 is good for tiles; -8 better for centered actors
    }
    
    protected final static void shatterTile(final Panmage img, final Panple pos, final boolean rot) {
    	shatterTile(FurGuardiansGame.room, img, pos, rot);
    }
    
    private final static class Shatter extends Faller {
    	private Shatter(final Panlayer layer, final Panmage img, final float x, final float y, final int xm, final int ym) {
    		super(layer, img, x, y, xm * Mathtil.randf(0.7f, 1.3f), ym * Mathtil.randf(0.7f, 1.3f));
    	}
    }
    
    private final static class Bump extends TileActor implements StepListener, CollisionListener {
    	private final Character bumper;
    	private final int index;
    	private final Object nextImage;
        private int age = 0;
        
        private Bump(final Character bumper, final int index, final Object nextImage) {
        	this.bumper = bumper;
        	this.index = index;
        	this.nextImage = nextImage;
        	setViewFromForeground(Level.tm, Level.tm.getTile(index));
            final Panple pos = Level.tm.getPosition(index);
            FurGuardiansGame.setPosition(this, pos.getX(), pos.getY() + 2, FurGuardiansGame.DEPTH_SHATTER);
            FurGuardiansGame.room.addActor(this);
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
                    if (nextImage != null) {
                        Level.tm.setForeground(index, nextImage, FurGuardiansGame.TILE_BUMP);
                    } else {
                        Level.tm.setForeground(index, getHandler().getBumpedImage());
                    }
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
    
    protected abstract static class TilePuzzle {
        protected final TileMap tm;
        
        protected TilePuzzle() {
            tm = Level.tm;
        }
        
        protected final void setPuzzleBlock(final int tileIndex) {
            tm.setForeground(tileIndex, FurGuardiansGame.blockPuzzle, Tile.BEHAVIOR_SOLID);
        }
        
        protected final void setPuzzleBlocks(final int[] tileIndices) {
            for (final int tileIndex : tileIndices) {
                setPuzzleBlock(tileIndex);
            }
        }
        
        protected final void clearPuzzleBlock(final int tileIndex) {
            tm.setForeground(tileIndex, null, Tile.BEHAVIOR_OPEN);
        }
        
        protected final void clearPuzzleBlocks(final int[] tileIndices) {
            for (final int tileIndex : tileIndices) {
                clearPuzzleBlock(tileIndex);
            }
        }
        
        protected final void addTimer(final long duration, final TimerListener listener) {
            Pangine.getEngine().addTimer(tm, duration, listener);
        }
    }
    
    protected final static class TileTrack extends TilePuzzle {
        private final int[] tileIndices;
        private final int activeSize;
        private int currentActiveStart = 0;
        
        protected TileTrack(final int[] tileIndices, final int activeSize) {
            this.tileIndices = tileIndices;
            this.activeSize = activeSize;
            initTiles();
            scheduleAdvance();
        }
        
        protected final void initTiles() {
            for (int i = 0; i < activeSize; i++) {
                setTile(i);
            }
        }
        
        private final void setTile(final int activeTrackPosition) {
            setPuzzleBlock(tileIndices[activeTrackPosition]);
        }
        
        protected final void advance() {
            clearPuzzleBlock(tileIndices[currentActiveStart]);
            currentActiveStart++;
            setTile(currentActiveStart);
            scheduleAdvance();
        }
        
        protected final void scheduleAdvance() {
            addTimer(4, new TimerListener() {
                @Override
                public final void onTimer(final TimerEvent event) {
                    advance();
                }});
        }
    }
    
    protected final static class AlternatorPuzzle extends TilePuzzle {
        private final int[][] tileGroups;
        private int currentGroupIndex = 0;
        
        protected AlternatorPuzzle(final int[][] tileGroups) {
            this.tileGroups = tileGroups;
            enableTiles(0);
        }
        
        protected final void enableTiles(final int groupIndex) {
            setPuzzleBlocks(tileGroups[groupIndex]);
        }
        
        protected final void enableTiles() {
            enableTiles(getNextGroupIndex());
            scheduleDisable();
        }
        
        protected final void disableTiles() {
            clearPuzzleBlocks(tileGroups[currentGroupIndex]);
            currentGroupIndex = getNextGroupIndex();
            scheduleEnable();
        }
        
        protected final int getNextGroupIndex() {
            return (currentGroupIndex + 1) % tileGroups.length;
        }
        
        protected final void scheduleEnable() {
            addTimer(25, new TimerListener() {
                @Override
                public final void onTimer(final TimerEvent event) {
                    enableTiles();
                }});
        }
        
        protected final void scheduleDisable() {
            addTimer(5, new TimerListener() {
                @Override
                public final void onTimer(final TimerEvent event) {
                    disableTiles();
                }});
        }
    }
}
