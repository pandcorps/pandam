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

import java.util.*;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandax.tile.*;

public abstract class BlockPuzzle {
    protected final TileMap tm;
    protected final Panmage[] blockImgs;
    
    protected BlockPuzzle(final Panmage[] blockImgs) {
        tm = BotsnBoltsGame.tm; // Remember original TileMap even after room change
        this.blockImgs = blockImgs;
    }
    
    protected final void fade(final int[] indicesToFadeOut, final int[] indicesToFadeIn) {
        fade(indicesToFadeOut, indicesToFadeIn, 1);
    }
    
    protected final void fade(final int[] indicesToFadeOut, final int[] indicesToFadeIn, final int step) {
        if (tm != BotsnBoltsGame.tm) {
            return;
        }
        final int numImgs = blockImgs.length;
        setTiles(indicesToFadeOut, step);
        setTiles(indicesToFadeIn, numImgs - step);
        if (step < numImgs) {
            Pangine.getEngine().addTimer(tm, 1, new TimerListener() {
                @Override public final void onTimer(final TimerEvent event) {
                    fade(indicesToFadeOut, indicesToFadeIn, step + 1);
                }});
        } else {
            onFadeEnd();
        }
    }
    
    //@OverrideMe
    protected void onFadeEnd() {
    }
    
    private final void setTiles(final int[] tileIndices, final int imgIndex) {
        if (tileIndices == null) {
            return;
        }
        final Panmage img;
        final byte b;
        if (imgIndex < blockImgs.length) {
            img = blockImgs[imgIndex];
            b = BotsnBoltsGame.TILE_FLOOR;
        } else {
            img = null;
            b = Tile.BEHAVIOR_OPEN;
        }
        for (final int index : tileIndices) {
            tm.setForeground(index, img, b);
        }
    }
    
    protected final static class TimedBlockPuzzle extends BlockPuzzle {
        private final List<int[]> steps;
        private int currentStepIndex = 0;
        
        protected TimedBlockPuzzle(final List<int[]> steps) {
            super(null); //TODO pick color/image
            this.steps = steps;
            schedule();
        }
        
        private final void schedule() {
            Pangine.getEngine().addTimer(tm, 30, new TimerListener() {
                @Override public final void onTimer(final TimerEvent event) {
                    fade();
                }});
        }
        
        private final void fade() {
            final int numSteps = steps.size();
            int previousStepIndex = currentStepIndex - 2;
            if (previousStepIndex < 0) {
                previousStepIndex += numSteps;
            }
            fade(steps.get(previousStepIndex), steps.get(currentStepIndex));
            currentStepIndex++;
            if (currentStepIndex >= numSteps) {
                currentStepIndex = 0;
            }
            schedule();
        }
    }
    
    protected final static class ShootableBlockPuzzle extends BlockPuzzle {
        private final List<ShootableBlock> blocks;
        private int[] enabledIndices;
        private int[] disabledIndices;
        
        protected ShootableBlockPuzzle(final int[] initiallyEnabledIndices, final int[] initiallyDisabledIndices) {
            super(BotsnBoltsGame.blockCyan);
            enabledIndices = initiallyEnabledIndices;
            disabledIndices = initiallyDisabledIndices;
            blocks = new ArrayList<ShootableBlock>(Math.max(enabledIndices.length, disabledIndices.length));
            fade(null, enabledIndices);
        }
        
        private final void fade() {
            Panctor.destroy(blocks);
            fade(enabledIndices, disabledIndices);
            final int[] tmpIndices = enabledIndices;
            enabledIndices = disabledIndices;
            disabledIndices = tmpIndices;
        }
        
        @Override
        protected final void onFadeEnd() {
            for (final int index : enabledIndices) {
                blocks.add(new ShootableBlock(this, index));
            }
        }
    }
    
    protected final static class ShootableBlock extends Panctor implements CollisionListener {
        private final ShootableBlockPuzzle puzzle;
        
        protected ShootableBlock(final ShootableBlockPuzzle puzzle, final int index) {
            this.puzzle = puzzle;
            final Panple pos = getPosition();
            puzzle.tm.savePosition(pos, index);
            puzzle.tm.getLayer().addActor(this);
            setVisible(false);
            setView(puzzle.blockImgs[0]);
        }

        @Override
        public final void onCollision(final CollisionEvent event) {
            final Collidable collider = event.getCollider();
            if (collider instanceof Projectile) {
                puzzle.fade();
                ((Projectile) collider).burst();
                collider.destroy();
            }
        }
    }
    
    protected final static class SpikeBlockPuzzle {
        protected SpikeBlockPuzzle(final int[] initiallyVerticalIndices, final int[] initiallyHorizontalIndices) {
        }
    }
}
