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

import java.util.*;

import org.pandcorps.botsnbolts.Enemy.*;
import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.botsnbolts.ShootableDoor.*;
import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.visual.*;

public abstract class BlockPuzzle {
    protected final static String RES_BG = BotsnBoltsGame.RES + "bg/";
    
    protected final TileMap tm;
    protected final Panmage[] blockImgs;
    
    protected BlockPuzzle(final Panmage[] blockImgs) {
        tm = BotsnBoltsGame.tm; // Remember original TileMap even after room change
        this.blockImgs = blockImgs;
    }
    
    protected abstract void init();
    
    protected abstract void clear();
    
    protected final void fade(final int[] indicesToFadeOut, final int[] indicesToFadeIn) {
        fade(indicesToFadeOut, indicesToFadeIn, 1);
    }
    
    protected final void fade(final int[] indicesToFadeOut, final int[] indicesToFadeIn, final int step) {
        if (tm != BotsnBoltsGame.tm) {
            return;
        }
        final int numImgs = blockImgs.length;
        setTiles(indicesToFadeOut, step, true);
        setTiles(indicesToFadeIn, numImgs - step, false);
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
    
    private final void setTiles(final int[] tileIndices, final int imgIndex, final boolean fadingOut) {
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
            if (fadingOut && (Tile.getBehavior(tm.getTile(index)) == Tile.BEHAVIOR_OPEN)) {
                continue;
            }
            tm.setForeground(index, img, b);
        }
    }
    
    protected final void clear(final int[] tileIndices) {
        setTiles(tileIndices, blockImgs.length, true);
    }
    
    protected final static class TimedBlockPuzzle extends BlockPuzzle {
        private final List<int[]> steps;
        private int currentStepIndex = 0;
        
        protected TimedBlockPuzzle(final List<int[]> steps) {
            super(BotsnBoltsGame.blockTimed);
            this.steps = steps;
        }
        
        @Override
        protected final void init() {
            schedule();
        }
        
        @Override
        protected final void clear() {
            for (final int[] tileIndices : steps) {
                clear(tileIndices);
            }
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
    
    protected abstract static class BinaryBlockPuzzle extends BlockPuzzle {
        protected int[] enabledIndices;
        protected int[] disabledIndices;
        
        protected BinaryBlockPuzzle(final Panmage[] blockImgs, final int[] initiallyEnabledIndices, final int[] initiallyDisabledIndices) {
            super(blockImgs);
            enabledIndices = initiallyEnabledIndices;
            disabledIndices = initiallyDisabledIndices;
            prepare();
        }
        
        @Override
        protected final void init() {
            fade(null, enabledIndices);
        }
        
        @Override
        protected final void clear() {
            clear(enabledIndices);
            clear(disabledIndices);
        }
        
        //@OverrideMe
        protected void prepare() {
        }
        
        protected final void fade() {
            onfadeStart();
            fade(enabledIndices, disabledIndices);
            final int[] tmpIndices = enabledIndices;
            enabledIndices = disabledIndices;
            disabledIndices = tmpIndices;
        }
        
        //@OverrideMe
        protected void onfadeStart() {
        }
    }
    
    protected final static class ShootableBlockPuzzle extends BinaryBlockPuzzle {
        private List<ShootableBlock> blocks; // Setting to null here overrides super constructor's prepare() call
        
        protected ShootableBlockPuzzle(final int[] initiallyEnabledIndices, final int[] initiallyDisabledIndices) {
            super(BotsnBoltsGame.blockCyan, initiallyEnabledIndices, initiallyDisabledIndices);
        }
        
        @Override
        protected final void prepare() {
            blocks = new ArrayList<ShootableBlock>(Math.max(enabledIndices.length, disabledIndices.length));
        }
        
        @Override
        protected final void onfadeStart() {
            Panctor.destroy(blocks);
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
            initTileActor(puzzle.tm, this, index, puzzle.blockImgs[0]);
            setVisible(false);
        }

        @Override
        public final void onCollision(final CollisionEvent event) {
            if (onCollisionProjectile(event)) {
                puzzle.fade();
            }
        }
    }
    
    private final static void initTileActor(final TileMap tm, final Panctor actor, final int index, final Panmage image) {
        tm.savePosition(actor.getPosition(), index);
        tm.getLayer().addActor(actor);
        actor.setView(image);
    }
    
    private final static boolean onCollisionProjectile(final CollisionEvent event) {
        final Collidable collider = event.getCollider();
        if (collider instanceof Projectile) {
            ((Projectile) collider).burst();
            collider.destroy();
            return true;
        }
        return false;
    }
    
    protected final static class ButtonBlockPuzzle extends BinaryBlockPuzzle {
        protected ButtonBlockPuzzle(final int[] initiallyEnabledIndices, final int[] initiallyDisabledIndices) {
            super(BotsnBoltsGame.blockButton, initiallyEnabledIndices, initiallyDisabledIndices);
        }
    }
    
    protected final static class BlockShootableButtonHandler implements ShootableButtonHandler {
        private final ButtonBlockPuzzle puzzle;
        
        protected BlockShootableButtonHandler(final ButtonBlockPuzzle puzzle) {
            this.puzzle = puzzle;
        }
        
        @Override
        public final void onShootButton() {
            puzzle.fade();
        }
    }
    
    // Blocks fade in when Player approaches; fade out when Player leaves
    protected final static class HiddenBlockPuzzle extends Panctor implements StepListener {
        private final Map<Integer, Integer> indices;
        private final Set<Integer> activeIndices = new HashSet<Integer>();
        
        protected HiddenBlockPuzzle(final int[] indices) {
            this.indices = new HashMap<Integer, Integer>(indices.length);
            final TileMap tm = BotsnBoltsGame.tm;
            for (final int index : indices) {
                this.indices.put(Integer.valueOf(tm.getColumn(index)), Integer.valueOf(index));
                tm.setBehavior(index, BotsnBoltsGame.TILE_FLOOR);
            }
            tm.getLayer().addActor(this);
        }

        @Override
        public final void onStep(final StepEvent event) {
            final Player player = PlayerContext.getPlayer(BotsnBoltsGame.pc);
            if (player == null) {
                return;
            }
            final Panmage[] blockImgs = BotsnBoltsGame.blockHidden;
            final TileMap tm = BotsnBoltsGame.tm;
            final int col = tm.getContainerColumn(player.getPosition().getX());
            for (final Integer activeIndex : activeIndices) {
                tm.setForeground(activeIndex.intValue(), null);
            }
            activeIndices.clear();
            for (int i = 0; i < 4; i++) {
                for (int j = ((i == 0) ? 1 : 0); j < 2; j++) {
                    final int mult = (j == 0) ? 1 : -1;
                    final Integer tileIndex = indices.get(Integer.valueOf(col + (mult * i)));
                    if (tileIndex == null) {
                        continue;
                    }
                    tm.setForeground(tileIndex.intValue(), blockImgs[i]);
                    activeIndices.add(tileIndex);
                }
            }
        }
    }
    
    protected final static class SpikeBlockPuzzle extends Panctor implements StepListener {
        private final static int vel = 3;
        private final static int timeAdd = (15 / vel) + 1;
        private final static int timeWait = timeAdd + 30;
        private final static int timeSub = timeWait + (15 / vel);
        private SpikeBlock[] verticalBlocks;
        private SpikeBlock[] horizontalBlocks;
        private int timer = 0;
        
        protected SpikeBlockPuzzle(final int[] initiallyVerticalIndices, final int[] initiallyHorizontalIndices) {
            verticalBlocks = setTiles(initiallyVerticalIndices, 1);
            horizontalBlocks = setTiles(initiallyHorizontalIndices, 0);
            BotsnBoltsGame.tm.getLayer().addActor(this);
        }
        
        private final SpikeBlock[] setTiles(final int[] tileIndices, final int baseRot) {
            final int size = tileIndices.length;
            final SpikeBlock[] blocks = new SpikeBlock[size];
            for (int i = 0; i < size; i++) {
                blocks[i] = new SpikeBlock(tileIndices[i], baseRot);
            }
            return blocks;
        }

        @Override
        public final void onStep(final StepEvent event) {
            timer++;
            if (timer < timeAdd) {
                moveSpikes(verticalBlocks, 0, vel);
                moveSpikes(horizontalBlocks, vel, 0);
            } else if (timer < timeWait) {
                // Do nothing; just keep the Spikes out
            } else if (timer < timeSub) {
                moveSpikes(verticalBlocks, 0, -vel);
                moveSpikes(horizontalBlocks, -vel, 0);
            } else {
                rotateSpikes(verticalBlocks, -1);
                rotateSpikes(horizontalBlocks, 1);
                final SpikeBlock[] tmp = verticalBlocks;
                verticalBlocks = horizontalBlocks;
                horizontalBlocks = tmp;
                timer = 0;
            }
        }
        
        private final void moveSpikes(final SpikeBlock[] blocks, final int x, final int y) {
            for (final SpikeBlock block : blocks) {
                block.moveSpikes(x, y);
            }
        }
        
        private final void rotateSpikes(final SpikeBlock[] blocks, final int amtRot) {
            for (final SpikeBlock block : blocks) {
                block.rotateSpikes(amtRot);
            }
        }
    }
    
    protected final static class SpikeBlock {
        private final Spike positiveSpike;
        private final Spike negativeSpike;
        
        protected SpikeBlock(final int tileIndex, final int baseRot) {
            BotsnBoltsGame.tm.setForeground(tileIndex, BotsnBoltsGame.getBlockSpike(), Tile.BEHAVIOR_SOLID);
            positiveSpike = new Spike(tileIndex, baseRot);
            negativeSpike = new Spike(tileIndex, baseRot + 2);
        }
        
        protected final void moveSpikes(final int x, final int y) {
            positiveSpike.getPosition().add(x, y);
            negativeSpike.getPosition().add(-x, -y);
        }
        
        protected final void rotateSpikes(final int amtRot) {
            positiveSpike.rotate(amtRot);
            negativeSpike.rotate(amtRot);
        }
    }
    
    protected final static int DAMAGE_SPIKE = HudMeter.MAX_VALUE / 4;
    
    protected final static class Spike extends TileUnawareEnemy {
        private final float baseX;
        private final float baseY;
        
        protected Spike(final int tileIndex, final int rot) {
            super(BotsnBoltsGame.tm.getColumn(tileIndex), BotsnBoltsGame.tm.getRow(tileIndex), 1);
            final Panple pos = getPosition();
            pos.setZ(BotsnBoltsGame.DEPTH_BG);
            baseX = pos.getX();
            baseY = pos.getY();
            setDirection(rot);
            setView(BotsnBoltsGame.getSpike());
            RoomLoader.addActor(this);
        }
        
        @Override
        protected final int getInitialOffsetX() {
            return 0;
        }
        
        protected final void setDirection(final int rot) {
            setRot(rot);
            final int offX, offY;
            switch (rot) {
                case 1 :
                    offX = 7;
                    offY = 8;
                    break;
                case 2 :
                    offX = 7;
                    offY = 7;
                    break;
                case 3 :
                    offX = 8;
                    offY = 7;
                    break;
                default :
                    offX = 8;
                    offY = 8;
                    break;
            }
            getPosition().set(baseX + offX, baseY + offY);
        }
        
        protected final void rotate(final int amtRot) {
            setDirection(getRot() + amtRot);
        }
        
        @Override
        protected final int getDamage() {
            return DAMAGE_SPIKE;
        }

        @Override
        protected final void onShot(final Projectile prj) {
        }
        
        @Override
        protected final void award(final PowerUp powerUp) {
        }
    }
    
    private static Panframe spikeFloor = null;
    
    protected final static void setSpikeFloor(final int tileIndex) {
        if (spikeFloor == null) {
            spikeFloor = Pangine.getEngine().createFrame(BotsnBoltsGame.PRE_FRM + "SpikeFloor", BotsnBoltsGame.getSpikeTile(), 1, 2, false, false);
        }
        BotsnBoltsGame.tm.setForeground(tileIndex, spikeFloor, BotsnBoltsGame.TILE_HURT);
    }
    
    protected final static void setSpikeCeiling(final int tileIndex) {
        BotsnBoltsGame.tm.setForeground(tileIndex, BotsnBoltsGame.getSpikeTile(), BotsnBoltsGame.TILE_HURT);
    }
    
    protected abstract static class ActorBlock extends Panctor {
        protected ActorBlock(final int tileIndex) {
            final TileMap tm = BotsnBoltsGame.tm;
            tm.savePosition(getPosition(), tileIndex);
            tm.setForeground(tileIndex, getBlockImage(), Tile.BEHAVIOR_SOLID);
            tm.getLayer().addActor(this);
        }
        
        protected abstract Panmage getBlockImage();
    }
    
    protected abstract static class TimerBlock extends ActorBlock implements StepListener {
        private int timer;
        
        protected TimerBlock(final int tileIndex, final int timerOffset) {
            super(tileIndex);
            timer = getDurationPeriod() - (timerOffset * 16);
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            timer--;
            if (timer <= 0) {
                onTimer();
                timer = getDurationPeriod();
            }
        }
        
        protected abstract int getDurationPeriod();
        
        protected abstract void onTimer();
    }
    
    protected final static class ElectricityBlock extends TimerBlock {
        protected final static int DURATION_PERIOD = 64;
        protected static Panmage image = null;
        
        protected ElectricityBlock(final int tileIndex, final int timerOffset) {
            super(tileIndex, timerOffset);
        }
        
        @Override
        protected final int getDurationPeriod() {
            return DURATION_PERIOD;
        }
        
        @Override
        protected final void onTimer() {
            new Electricity(this, 0, -64, 4, true, false);
        }
        
        @Override
        protected final Panmage getBlockImage() {
            return (image = getImage(image, "BlockElectricity", null, null, null));
        }
    }
    
    protected final static class Electricity extends TimedEnemyProjectile {
        protected final static int DURATION_ELECTRICITY = 12;
        private final int NUM_PARTS;
        protected static Panmage image = null;
        private final int[] parts;
        private final boolean vertical;
        private final boolean flip;
        private int min = 3;
        private int max = 3;
        private final Pansplay display = new OriginPansplay(new ElectricityMinimum(), new ElectricityMaximum());
        
        protected Electricity(final Panctor src, final int ox, final int oy, final int numParts, final boolean vertical, final boolean flip) {
            super(null, src, ox, oy, DURATION_ELECTRICITY);
            NUM_PARTS = numParts;
            parts = new int[NUM_PARTS];
            this.vertical = vertical;
            this.flip = flip;
            randomize();
            setSize();
        }
        
        private final void randomize() {
            for (int i = 0; i < NUM_PARTS; i++) {
                parts[i] = Mathtil.randi(-4, 3);
            }
        }
        
        @Override
        protected final boolean isDestroyedOnImpact() {
            return false;
        }
        
        @Override
        protected final void burst(final Player player) {
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            super.onStep(event);
            setSize();
        }
        
        private final void setSize() {
            final int index = DURATION_ELECTRICITY - timer;
            if ((index == 3) || (index == 6) || (index == 9)) {
                randomize();
            }
            final int lim = NUM_PARTS - 1;
            if (index == 0) {
                min = max = lim;
            } else if (index == 1) {
                min = 1;
                max = lim;
            } else if (timer == 0) {
                min = 0;
                max = 1;
            } else {
                min = 0;
                max = lim;
            }
            if (flip) {
                final int t = lim - min;
                min = lim - max;
                max = t;
            }
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panple pos = getPosition();
            final Panlayer layer = getLayer();
            final Panmage image = getElectricityImage();
            final float x = pos.getX(), y = pos.getY();
            final int rot = vertical ? 0 : 1;
            final int xm = vertical ? 0 : 1, ym = vertical ? 1 : 0;
            for (int i = min; i <= max; i++) {
                final int p = parts[i], mag;
                final boolean m = p < 0;
                if (m) {
                    mag = (p + 1) * -1;
                } else {
                    mag = p;
                }
                final int ix = mag % 2;
                final int iy = mag / 2;
                final int off = i * 16;
                renderer.render(layer, image, x + (xm * off), y + (ym * off), BotsnBoltsGame.DEPTH_PROJECTILE, ix * 16, iy * 16, 16, 16, rot, m, false);
            }
        }
        
        @Override
        public Pansplay getCurrentDisplay() {
            return display;
        }
        
        private final class ElectricityMinimum extends UnmodPanple2 {
            @Override
            public final float getX() {
                return vertical ? 4 : (min * 16);
            }

            @Override
            public final float getY() {
                return vertical ? (min * 16) : 4;
            }
        }
        
        private final class ElectricityMaximum extends UnmodPanple2 {
            @Override
            public final float getX() {
                return vertical ? 12 : (max * 16);
            }

            @Override
            public final float getY() {
                return vertical ? (max * 16) : 12;
            }
        }
        
        protected final static Panmage getElectricityImage() {
            return (image = getImage(image, "Electricity", null, null, null));
        }
    }
    
    protected final static void crumble(final int tileIndex) {
        continueCrumble(BotsnBoltsGame.tm, tileIndex, 0);
    }
    
    private final static void continueCrumble(final TileMap tm, final int tileIndex, final int imgIndex) {
        if (imgIndex > 2) {
            finishCrumble(tm, tileIndex);
            return;
        }
        tm.setForeground(tileIndex, getCrumbleImage(imgIndex), Tile.BEHAVIOR_SOLID);
        Pangine.getEngine().addTimer(tm, 5, new TimerListener() {
            @Override public final void onTimer(final TimerEvent event) {
                if (RoomChanger.isChanging()) {
                    return;
                }
                continueCrumble(tm, tileIndex, imgIndex + 1);
            }});
    }
    
    private final static void finishCrumble(final TileMap tm, final int tileIndex) {
        tm.setForeground(tileIndex, null, Tile.BEHAVIOR_OPEN);
        DrillEnemy.removeShadow(tm, tileIndex);
        final Panple pos = new ImplPanple();
        tm.savePosition(pos, tileIndex);
        pos.addX(8);
        Player.shatter(pos, DrillEnemy.getDirtShatter());
    }
    
    private final static Panmage[] crumble = new Panmage[3];
    
    private final static Panmage getCrumbleImage(final int i) {
        Panmage img = crumble[i];
        if (img == null) {
            img = Pangine.getEngine().createImage("dirt.crumble." + i, null, null, null, BotsnBoltsGame.RES + "misc/DirtCrumble" + (i + 1) + ".png");
            crumble[i] = img;
        }
        return img;
    }
    
    // Shooting the BurstBlock will start a chain reaction, bursting adjacent blocks in the puzzle
    protected final static class BurstBlock extends Panctor implements CollisionListener {
        private static Panmage image = null;
        private final TileMap tm;
        private final int tileIndex;
        
        protected BurstBlock(final int tileIndex) {
            tm = BotsnBoltsGame.tm;
            this.tileIndex = tileIndex;
            initTileActor(tm, this, tileIndex, getBlockImage());
            tm.setBehavior(tileIndex, Tile.BEHAVIOR_SOLID);
        }
        
        @Override
        public final void onCollision(final CollisionEvent event) {
            if (onCollisionProjectile(event)) {
                new BurstingBlock(tm, tileIndex);
                destroy();
            }
        }
        
        private final static Panmage getBlockImage() {
            final Panmage ref = BotsnBoltsGame.blockCyan[0];
            return (image = getImage(image, "BlockBurster", ref.getOrigin(), ref.getBoundingMinimum(), ref.getBoundingMaximum()));
        }
    }
    
    private static Panmage burstable = null;
    
    protected final static void setBurstable(final int tileIndex) {
        burstable = getImage(burstable, "BlockBurstable", null, null, null);
        BotsnBoltsGame.tm.setForeground(tileIndex, burstable, BotsnBoltsGame.TILE_BURSTABLE);
    }
    
    protected final static class BurstingBlock extends ActorBlock implements StepListener {
        private static Panmage[] images = new Panmage[2];
        private final TileMap tm;
        private final int tileIndex;
        private int timer = 0;
        
        protected BurstingBlock(final TileMap tm, final int tileIndex) {
            super(tileIndex);
            this.tm = tm;
            this.tileIndex = tileIndex;
            tm.setBehavior(tileIndex, Tile.BEHAVIOR_OPEN);
            RoomLoader.removeShadow(tm, tm.getColumn(tileIndex), tm.getRow(tileIndex) - 1);
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            timer++;
            if (timer == 2) {
                tm.setForeground(tileIndex, getBlockImage(1));
            } else if (timer == 3) {
                tm.setForeground(tileIndex, null);
                destroy();
                final int x = tm.getColumn(tileIndex), y = tm.getRow(tileIndex);
                for (final Direction dir : Direction.values()) {
                    final int neighborIndex = tm.getNeighbor(x, y, dir);
                    if (Tile.getBehavior(tm.getTile(neighborIndex)) == BotsnBoltsGame.TILE_BURSTABLE) {
                        new BurstingBlock(tm, neighborIndex);
                    }
                }
            }
        }
        
        @Override
        protected final Panmage getBlockImage() {
            return getBlockImage(0);
        }
        
        private final static Panmage getBlockImage(final int i) {
            Panmage image = images[i];
            if (image != null) {
                return image;
            }
            image = getImage(null, "BlockBurst" + (i + 1), null, null, null);
            images[i] = image;
            return image;
        }
    }
    
    protected final static class FireTimedBlock extends TimerBlock {
        protected final static int DURATION_PERIOD = 64;
        protected static Panmage image = null;
        
        protected FireTimedBlock(final int tileIndex, final int timerOffset) {
            super(tileIndex, timerOffset);
        }
        
        @Override
        protected final int getDurationPeriod() {
            return DURATION_PERIOD;
        }
        
        @Override
        protected final void onTimer() {
            final Panple pos = getPosition();
            new Fire(pos.getX(), pos.getY(), null);
        }
        
        @Override
        protected final Panmage getBlockImage() {
            return (image = getImage(image, "BlockFireTimed", null, null, null));
        }
    }
    
    private final static Set<Integer> fireIndices = new HashSet<Integer>();
    
    protected final static class FirePressureBlock {
        protected static Panmage image = null;
        
        protected final static void init(final int tileIndex) {
            BotsnBoltsGame.tm.setForeground(tileIndex, getBlockImage(), BotsnBoltsGame.TILE_PRESSURE_FIRE);
        }
        
        protected final static void activate(final int tileIndex) {
            final Integer key = Integer.valueOf(tileIndex);
            if (!fireIndices.add(key)) {
                return;
            }
            Enemy.addRoomTimer(8, new RoomTimerListener() {
                @Override public final void onTimer() {
                    final TileMap tm = BotsnBoltsGame.tm;
                    new Fire(tm.getX(tileIndex), tm.getY(tileIndex), key);
            }});
        }
        
        protected final static Panmage getBlockImage() {
            return (image = getImage(image, "BlockFirePressure", null, null, null));
        }
    }
    
    protected final static class Fire extends TimedEnemyProjectile {
        protected static Panmage image = null;
        private int h = 0;
        private int top = 3;
        private int timer = 1;
        private boolean ascending = true;
        private final Pansplay display = new OriginPansplay(new FinPanple2(3, 0), new FireMaximum());
        private final Integer key;
        
        protected Fire(final float x, final float y, final Integer key) {
            super(null, x, y, false, 0, 0, 16, 0, 0, Integer.MAX_VALUE);
            this.key = key;
        }
        
        @Override
        protected final boolean isDestroyedOnImpact() {
            return false;
        }
        
        @Override
        protected final void burst(final Player player) {
        }
        
        @Override
        protected final void onDestroy() {
            fireIndices.remove(key);
        }
        
        @Override
        public final void onStep(final StepEvent event) {
            super.onStep(event);
            setSize();
        }
        
        private final void setSize() {
            if (timer < 1) {
                timer++;
            } else {
                timer = 0;
                if (ascending) {
                    ascend();
                } else {
                    descend();
                }
            }
        }
        
        private final void ascend() {
            if (top > 1) {
                if (h < 3) {
                    top = 0;
                    h++;
                } else {
                    ascending = false;
                    descend();
                }
            } else {
                top++;
            }
        }
        
        private final void descend() {
            if (top > 0) {
                top--;
            } else {
                top = 2;
                h--;
                if (h <= 0) {
                    destroy();
                }
            }
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panlayer layer = getLayer();
            final Panmage img = getFireImage();
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY();
            final int baseMirror = (h + (ascending ? 0 : 1)) % 2;
            for (int i = 0; i < h; i++) {
                final int ix, iy, yoff;
                final boolean mirror;
                if (i == (h - 1)) {
                    ix = (top / 2) * 16;
                    iy = (top % 2) * 16;
                    yoff = (top == 0) ? -10 : 0;
                    mirror = false;
                } else {
                    ix = iy = 16;
                    yoff = 0;
                    mirror = (i % 2) == baseMirror;
                }
                renderer.render(layer, img, x, y + (i * 16) + yoff, BotsnBoltsGame.DEPTH_PROJECTILE, ix, iy, 16, 16, 0, mirror, false);
            }
        }
        
        @Override
        public Pansplay getCurrentDisplay() {
            return display;
        }
        
        private final class FireMaximum extends UnmodPanple2 {
            @Override
            public final float getX() {
                return 13;
            }

            @Override
            public final float getY() {
                return ((h - 1) * 16) + ((top + 1) * 4);
            }
        }
        
        protected final static Panmage getFireImage() {
            return (image = getImage(image, "Fire", null, null, null));
        }
    }
    
    protected final static Panmage getImage(final Panmage img, final String name, final Panple o, final Panple min, final Panple max) {
        return Enemy.getImage(img, "puzzle.", RES_BG, name, o, min, max);
    }
    
    protected final static void onRoomChange() {
        fireIndices.clear();
    }
}
