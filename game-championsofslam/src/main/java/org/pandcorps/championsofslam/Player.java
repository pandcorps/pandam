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
package org.pandcorps.championsofslam;

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.col.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.text.*;

public class Player extends Champion {
    protected final static Set<Player> players = new IdentityHashSet<Player>();
    private final static SlamOption fileOption = new SlamOption("", "") {
        @Override protected final Object getValue(final PlayerContext pc) {
            return (pc.fileIndex == -1) ? "NEW?" : "PICK?"; }};
    private final static ChampionOption[] pausedOptions = {
            new ChampionOption("", "PAUSE") {
                @Override protected final Object getValue(ChampionDefinition def) {
                    return null; }},
            new ChampionOption("BODY", "R ") {
                @Override protected final Object getValue(ChampionDefinition def) {
                    return getColorKey(def.bodyColor.getR()); }},
            new ChampionOption("BODY", "G ") {
                @Override protected final Object getValue(ChampionDefinition def) {
                    return getColorKey(def.bodyColor.getG()); }},
            new ChampionOption("BODY", "B ") {
                @Override protected final Object getValue(ChampionDefinition def) {
                    return getColorKey(def.bodyColor.getB()); }},
            new ChampionOption("EYES", "") {
                @Override protected final Object getValue(ChampionDefinition def) {
                    return getIndexKey(def.eyesIndex, 0); }},
            new ChampionOption("HAIR", "") {
                @Override protected final Object getValue(ChampionDefinition def) {
                    return getIndexKey(def.hairIndex, 1); }},
            new ChampionOption("HAIR", "R ") {
                @Override protected final Object getValue(ChampionDefinition def) {
                    return getColorKey(def.hairColor.getR()); }},
            new ChampionOption("HAIR", "G ") {
                @Override protected final Object getValue(ChampionDefinition def) {
                    return getColorKey(def.hairColor.getG()); }},
            new ChampionOption("HAIR", "B ") {
                @Override protected final Object getValue(ChampionDefinition def) {
                    return getColorKey(def.hairColor.getB()); }},
            new ChampionOption("SHIRT", "") {
                @Override protected final Object getValue(ChampionDefinition def) {
                    return getIndexKey(def.shirt.style.styleIndex, 0); }},
            new ChampionOption("SHIRT", "R ") {
                @Override protected final Object getValue(ChampionDefinition def) {
                    return getColorKey(def.shirt.color.getR()); }},
            new ChampionOption("SHIRT", "G ") {
                @Override protected final Object getValue(ChampionDefinition def) {
                    return getColorKey(def.shirt.color.getG()); }},
            new ChampionOption("SHIRT", "B ") {
                @Override protected final Object getValue(ChampionDefinition def) {
                    return getColorKey(def.shirt.color.getB()); }},
            new ChampionOption("PANTS", "") {
                @Override protected final Object getValue(ChampionDefinition def) {
                    return getIndexKey(def.pants.style.styleIndex, 0); }},
            new ChampionOption("PANTS", "R ") {
                @Override protected final Object getValue(ChampionDefinition def) {
                    return getColorKey(def.pants.color.getR()); }},
            new ChampionOption("PANTS", "G ") {
                @Override protected final Object getValue(ChampionDefinition def) {
                    return getColorKey(def.pants.color.getG()); }},
            new ChampionOption("PANTS", "B ") {
                @Override protected final Object getValue(ChampionDefinition def) {
                    return getColorKey(def.pants.color.getB()); }},
            new ChampionOption("BOOTS", "R ") {
                @Override protected final Object getValue(ChampionDefinition def) {
                    return getColorKey(def.boots.color.getR()); }},
            new ChampionOption("BOOTS", "G ") {
                @Override protected final Object getValue(ChampionDefinition def) {
                    return getColorKey(def.boots.color.getG()); }},
            new ChampionOption("BOOTS", "B ") {
                @Override protected final Object getValue(ChampionDefinition def) {
                    return getColorKey(def.boots.color.getB()); }}
    };
    private static int numPlayers = 0;
    
    private final PlayerContext pc;
    private PlayerState state = stateNormal;
    private boolean paused = false;
    private int pausedOption = 0;
    private Pantext pausedText = null;
    private List<StringBuilder> pausedSequences = null;
    
    public Player(final Panlayer layer, final PlayerContext pc, final Set<Champion> team) {
        super(pc.def, team);
        this.pc = pc;
        final ControlScheme ctrl = pc.ctrl;
        getPosition().set(getPausedX(), minY);
        layer.addActor(this);
        final Panput left = ctrl.getLeft(), right = ctrl.getRight(), up = ctrl.getUp(), down = ctrl.getDown();
        register(left, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) {
                onLeftStart();
            }});
        register(right, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) {
                onRightStart();
            }});
        register(up, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) {
                onUpStart();
            }});
        register(down, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) {
                onDownStart();
            }});
        register(left, new ActionListener() {
            @Override public final void onAction(final ActionEvent event) {
                onLeft();
            }});
        register(right, new ActionListener() {
            @Override public final void onAction(final ActionEvent event) {
                onRight();
            }});
        register(up, new ActionListener() {
            @Override public final void onAction(final ActionEvent event) {
                onUp();
            }});
        register(down, new ActionListener() {
            @Override public final void onAction(final ActionEvent event) {
                onDown();
            }});
        ctrl.registerActionInputs(this, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) {
                onAttackStart();
            }});
        ctrl.registerMenuInputs(this, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) {
                onPause();
            }});
        players.add(this);
        togglePause();
    }
    
    protected final void onLeftStart() {
        state.onLeftStart(this);
    }
    
    protected final void onRightStart() {
        state.onRightStart(this);
    }
    
    protected final void onUpStart() {
        state.onUpStart(this);
    }
    
    protected final void onDownStart() {
        state.onDownStart(this);
    }
    
    protected final void onLeft() {
        state.onLeft(this);
    }
    
    protected final void onRight() {
        state.onRight(this);
    }
    
    protected final void onUp() {
        state.onUp(this);
    }
    
    protected final void onDown() {
        state.onDown(this);
    }
    
    protected final void onAttackStart() {
        state.onAttackStart(this);
    }
    
    @Override
    protected final Panple getChampionPosition() {
        return state.getPosition(this);
    }
    
    protected final void onPause() {
        if (!pc.fileSelected) {
            initFile();
            return;
        }
        togglePause();
    }
    
    private final void togglePause() {
        paused = !paused;
        state = paused ? statePaused : stateNormal;
        state.onPause(this);
        clearWalk();
    }
    
    private final void initFile() {
        if (pc.fileIndex < 0) {
            pc.fileIndex = ChampionsOfSlamGame.saveFiles.size();
            ChampionsOfSlamGame.saveFiles.add(null);
        }
        pc.fileSelected = true;
        setPausedText();
    }
    
    private final void saveIfNeeded() {
        final String content = def.toString();
        final int fileIndex = pc.fileIndex;
        if (content.equals(ChampionsOfSlamGame.saveFiles.get(fileIndex))) {
            return;
        }
        ChampionsOfSlamGame.saveFiles.set(fileIndex, content);
        Iotil.writeFile(ChampionsOfSlamGame.getFileName(fileIndex), content);
    }
    
    @Override
    protected final boolean isPaused() {
        return paused;
    }
    
    @Override
    protected final boolean isInvincible() {
        return true;
    }
    
    protected final void onVictory() {
        atk = atkUppercut;
        atkTimer = 60;
    }
    
    @Override
    protected final void onDestroy() {
        super.onDestroy();
        players.remove(this);
    }
    
    private final SlamOption getPausedOption() {
        return pc.fileSelected ? pausedOptions[pausedOption] : fileOption;
    }
    
    private final void setPausedText() {
        pausedText.uncenterX();
        getPausedOption().append(pausedSequences, pc);
        pausedText.centerX();
    }
    
    protected final int getPausedX() {
        if (numPlayers == 1) {
            return ChampionsOfSlamGame.GAME_W / 2;
        }
        final float n = (pc.index) * (maxX - minX);
        final float d = numPlayers - 1;
        final int x = Math.round(minX + (n / d));
        return ((x % 2) == 1) ? (x + 1) : x;
    }
    
    private final boolean isFileAvailable() {
        final int fileIndex = pc.fileIndex;
        if (fileIndex < 0) {
            return true;
        }
        for (final Player player : players) {
            if (player == this) {
                continue;
            } else if (player.pc.fileIndex == fileIndex) {
                return false;
            }
        }
        return true;
    }
    
    protected final static Integer getIndexKey(final int index, final int off) {
        return Integer.valueOf(index + off);
    }
    
    protected final static Integer getColorKey(final float color) {
        return Integer.valueOf(getColorInt(color));
    }
    
    protected final static boolean isAllPaused() {
        for (final Player player : players) {
            if (!player.isPaused()) {
                return false;
            }
        }
        return true;
    }
    
    public final static class PlayerContext {
        private final ChampionDefinition def;
        private final ControlScheme ctrl;
        private final int index;
        private final Panple pausedPosition = new ImplPanple();
        private int fileIndex = -1;
        private boolean fileSelected = false;
        
        public PlayerContext(final ChampionDefinition def, final ControlScheme ctrl) {
            this.def = def;
            this.ctrl = ctrl;
            index = numPlayers;
            numPlayers++;
            pausedPosition.setY(0);
        }
    }
    
    public static interface PlayerState {
        public void onLeftStart(Player player);
        
        public void onRightStart(Player player);
        
        public void onUpStart(Player player);
        
        public void onDownStart(Player player);
        
        public void onLeft(Player player);
        
        public void onRight(Player player);
        
        public void onUp(Player player);
        
        public void onDown(Player player);
        
        public void onAttackStart(Player player);
        
        public Panple getPosition(Player player);
        
        public void onPause(final Player player);
    }
    
    public final static PlayerState stateNormal = new PlayerState() {
        @Override
        public final void onLeftStart(final Player player) {
        }
        
        @Override
        public final void onRightStart(final Player player) {
        }
        
        @Override
        public final void onUpStart(final Player player) {
        }
        
        @Override
        public final void onDownStart(final Player player) {
        }
        
        @Override
        public final void onLeft(final Player player) {
            player.hv = -VEL;
        }
        
        @Override
        public final void onRight(final Player player) {
            player.hv = VEL;
        }
        
        @Override
        public final void onUp(final Player player) {
            player.v = VEL;
        }
        
        @Override
        public final void onDown(final Player player) {
            player.v = -VEL;
        }
        
        @Override
        public final void onAttackStart(final Player player) {
            player.onAttack();
        }
        
        @Override
        public final Panple getPosition(Player player) {
            return player.getPosition();
        }
        
        @Override
        public final void onPause(final Player player) {
            player.saveIfNeeded();
            Panctor.detach(player.pausedText);
            final Panple pos = player.getPosition();
            if (pos.getY() <= minY) {
                pos.setX(player.getPausedX());
            }
        }
    };
    
    public final static PlayerState statePaused = new PlayerState() {
        @Override
        public final void onLeftStart(final Player player) {
            increment(player, -1);
        }
        
        @Override
        public final void onRightStart(final Player player) {
            increment(player, 1);
        }
        
        @Override
        public final void onUpStart(final Player player) {
            incrementOption(player, -1);
        }
        
        @Override
        public final void onDownStart(final Player player) {
            incrementOption(player, 1);
        }
        
        private final void incrementOption(final Player player, final int dir) {
            int pausedOption = player.pausedOption;
            if (dir < 0) {
                pausedOption = ((pausedOption < 1) ? pausedOptions.length : pausedOption) - 1;
            } else {
                pausedOption = (pausedOption > (pausedOptions.length - 2)) ? 0 : (pausedOption + 1);
            }
            player.pausedOption = pausedOption;
            player.setPausedText();
        }
        
        private final void increment(final Player player, final int dir) {
            final PlayerContext pc = player.pc;
            final ChampionDefinition def = pc.def;
            if (!pc.fileSelected) {
                do {
                    pc.fileIndex = increment(pc.fileIndex, -1, ChampionsOfSlamGame.saveFiles.size(), dir);
                } while (!player.isFileAvailable());
                if (pc.fileIndex < 0) {
                    pc.fileIndex = -1;
                    randomize(def);
                } else {
                    def.load(ChampionsOfSlamGame.saveFiles.get(pc.fileIndex));
                }
                player.setPausedText();
                return;
            }
            switch (player.pausedOption) {
                case 0:
                    // Just paused
                    break;
                case 1:
                    def.bodyColor.setR(increment(def.bodyColor.getR(), dir));
                    break;
                case 2:
                    def.bodyColor.setG(increment(def.bodyColor.getG(), dir));
                    break;
                case 3:
                    def.bodyColor.setB(increment(def.bodyColor.getB(), dir));
                    break;
                case 4:
                    def.eyesIndex = increment(def.eyesIndex, NUM_EYES, dir);
                    break;
                case 5:
                    def.hairIndex = increment(def.hairIndex, -1, NUM_HAIR, dir);
                    break;
                case 6:
                    def.hairColor.setR(increment(def.hairColor.getR(), dir));
                    break;
                case 7:
                    def.hairColor.setG(increment(def.hairColor.getG(), dir));
                    break;
                case 8:
                    def.hairColor.setB(increment(def.hairColor.getB(), dir));
                    break;
                case 9:
                    increment(def.shirt, Images.shirtStyles, dir);
                    break;
                case 10:
                    def.shirt.color.setR(increment(def.shirt.color.getR(), dir));
                    break;
                case 11:
                    def.shirt.color.setG(increment(def.shirt.color.getG(), dir));
                    break;
                case 12:
                    def.shirt.color.setB(increment(def.shirt.color.getB(), dir));
                    break;
                case 13:
                    increment(def.pants, Images.pantsStyles, dir);
                    break;
                case 14:
                    def.pants.color.setR(increment(def.pants.color.getR(), dir));
                    break;
                case 15:
                    def.pants.color.setG(increment(def.pants.color.getG(), dir));
                    break;
                case 16:
                    def.pants.color.setB(increment(def.pants.color.getB(), dir));
                    break;
                case 17:
                    def.boots.color.setR(increment(def.boots.color.getR(), dir));
                    break;
                case 18:
                    def.boots.color.setG(increment(def.boots.color.getG(), dir));
                    break;
                case 19:
                    def.boots.color.setB(increment(def.boots.color.getB(), dir));
                    break;
            }
            player.setPausedText();
        }
        
        private final float increment(final float color, final int dir) {
            if (dir < 0) {
                return (color < 0.05f) ? 1.0f : color - INC_COLOR;
            } else {
                return (color > 0.95f) ? 0.0f : color + INC_COLOR;
            }
        }
        
        private final int increment(int i, final int n, final int dir) {
            return increment(i, 0, n, dir);
        }
        
        private final int increment(int i, final int min, final int n, final int dir) {
            i += dir;
            if (i < min) {
                return n - 1;
            } else if (i >= n) {
                return min;
            }
            return i;
        }
        
        private final void increment(final Clothing clothing, final Map<Object, ClothingStyle> styles, final int dir) {
            clothing.style = styles.get(increment(clothing.style.styleIndex, styles.size(), dir));
        }
        
        @Override
        public final void onLeft(final Player player) {
        }
        
        @Override
        public final void onRight(final Player player) {
        }
        
        @Override
        public final void onUp(final Player player) {
        }
        
        @Override
        public final void onDown(final Player player) {
        }
        
        @Override
        public final void onAttackStart(final Player player) {
        }
        
        @Override
        public final Panple getPosition(Player player) {
            final float x = player.getPausedX();
            final Panple pos = player.pc.pausedPosition;
            pos.setX(x);
            final Pantext pausedText = player.pausedText;
            pausedText.getPosition().setX(x);
            pausedText.centerX();
            return pos;
        }
        
        @Override
        public final void onPause(final Player player) {
            Pantext pausedText = player.pausedText;
            if (pausedText == null) {
                final List<StringBuilder> pausedSequences = Arrays.asList(new StringBuilder(), new StringBuilder());
                final SlamOption pausedOption = player.getPausedOption();
                pausedOption.append(pausedSequences, player.pc);
                player.pausedSequences = pausedSequences;
                pausedText = new Pantext(Pantil.vmid(), ChampionsOfSlamGame.font, pausedSequences);
                pausedText.getPosition().set(0, 41, DEPTH_TEXT);
                player.pausedText = pausedText;
                getPosition(player); // Update text position
            }
            player.getLayer().addActor(pausedText);
        }
    };
    
    public abstract static class SlamOption {
        private final String label;
        private final String detail;
        
        SlamOption(final String label, final String detail) {
            this.label = label;
            this.detail = detail;
        }
        
        public final void append(final List<StringBuilder> sequences, final PlayerContext pc) {
            final StringBuilder labelSequence = sequences.get(0);
            labelSequence.setLength(0);
            labelSequence.append(label);
            final StringBuilder detailSequence = sequences.get(1);
            detailSequence.setLength(0);
            detailSequence.append(detail);
            final Object value = getValue(pc);
            if (value != null) {
                detailSequence.append(value);
            }
        }
        
        protected abstract Object getValue(final PlayerContext pc);
    }
    
    public abstract static class ChampionOption extends SlamOption {
        ChampionOption(final String label, final String detail) {
            super(label, detail);
        }
        
        @Override
        protected final Object getValue(final PlayerContext pc) {
            return getValue(pc.def);
        }
        
        protected abstract Object getValue(final ChampionDefinition def);
    }
}
