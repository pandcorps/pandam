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
package org.pandcorps.rpg;

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.col.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.rpg.Chr.*;
import org.pandcorps.rpg.Enemy.*;

public class Fight {
    protected final static int optionWidth = 8;
    protected final static int pageSize = 4;
    protected final static int fontSize = RpgGame.fontSize;
    protected final static int fontMargin = 2;
    protected final static int optionHeight = fontSize + (fontMargin * 2);
    protected final static int enemyMinX = 32;
    protected final static int enemyMaxX = 224;
    protected final static int partyMaxY = 160;
    protected final static int partyOffY = 32;
    protected final static int partyMinY = partyMaxY - (partyOffY * (RpgGame.maxPartySize - 1));
    protected final static int partyH = partyMaxY - partyMinY;
    protected final static int partyMinZ = 10;
    protected static OptionText menuTitle = null;
    protected final static List<OptionText> optionTexts = new ArrayList<OptionText>(pageSize);
    protected static Panlayer layer = null;
    protected final static List<PlayerFighter> playerParty = new ArrayList<PlayerFighter>(RpgGame.maxPartySize);
    protected final static List<EnemyFighter> enemyParty = new ArrayList<EnemyFighter>();
    protected static boolean playerTurn = true;
    protected static int partyMemberIndex = 0;
    protected static ChrDefinition currentPlayerDef = null;
    protected final static Stack<FightMenuState> stack = new Stack<FightMenuState>();
    protected final static List<FightOption> partyMemberOptions = new ArrayList<FightOption>();
    protected final static List<FightOption> enemyTargetOptions = new ArrayList<FightOption>();
    protected static TargetedEnemyHandler targetedEnemyHandler = null;
    protected static Cursor cursor = null;
    
    protected final static void goFight() {
        final Panroom room = RpgGame.room;
        layer = Pangine.getEngine().createLayer("fight.layer", RpgGame.GAME_W, RpgGame.GAME_H, room.getSize().getZ(), room);
        room.addAbove(layer);
        room.setActive(false);
        room.setVisible(false);
        cursor = Cursor.addCursorIfNeeded(layer, RpgGame.cursorImage);
        initOptionTexts();
        initPlayerParty();
        initEnemyParty();
        initFight();
        startTurn();
    }
    
    protected final static void initOptionTexts() {
        if (optionTexts.isEmpty()) {
            menuTitle = new OptionText(8, 8 + (pageSize * optionHeight), null);
            for (int i = 0; i < pageSize; i++) {
                final int optionIndex = optionTexts.size();
                optionTexts.add(new OptionText(24, 8 + ((pageSize - optionIndex - 1) * 12), new ActionEndListener() {
                    @Override public final void onActionEnd(final ActionEndEvent event) {
                        final FightMenuState state = stack.peek();
                        final FightOption option = Coltil.get(state.options, state.startIndex + optionIndex);
                        if (option != null) {
                            option.onSelect(); //TODO Probably start with onHighlight, call onOnSelect if clicked again (or if separate confirm button clicked)
                        }
                    }}));
            }
        }
        menuTitle.attach();
        for (final OptionText optionText : optionTexts) {
            optionText.attach();
        }
    }
    
    protected final static TouchButton newTouchButton(final int x, final int y, final int w, final int h) {
        final Pangine engine = Pangine.getEngine();
        final Panteraction in = engine.getInteraction();
        return new TouchButton(in, Pantil.vmid(), x, y, w, h);
    }
    
    protected final static void initPlayerParty() {
        playerParty.clear();
        for (final ChrDefinition def : RpgGame.party) {
            playerParty.add(new PlayerFighter(def));
        }
    }
    
    protected final static void initEnemyParty() {
        final CountMap<EnemyDefinition> defs = new CountMap<EnemyDefinition>();
        defs.inc(Enemy.enemyMap.values().iterator().next()); //TODO Pick based on location
        Enemy.initEnemyParty(enemyParty, defs);
        Collections.sort(enemyParty);
        final int size = enemyParty.size();
        final int offY = partyH / (size + 1);
        for (int i = 0; i < size; i++) {
            enemyParty.get(i).avatar.getPosition().set(Mathtil.randi(enemyMinX, enemyMaxX), partyMaxY - (offY * (i + 1)), partyMinZ + (i * 2));
        }
    }
    
    protected final static void initFight() {
        playerTurn = true;
        partyMemberIndex = 0;
    }
    
    protected final static void closeFight() {
        menuTitle.detach();
        for (final OptionText optionText : optionTexts) {
            optionText.detach();
        }
        final Panroom room = RpgGame.room;
        room.setVisible(true);
        room.setActive(true);
        layer.destroy();
    }
    
    protected final static void startTurn() {
        if (playerTurn) {
            startPlayerTurn();
        } else {
            startEnemyTurn();
        }
    }
    
    protected final static void startPlayerTurn() {
        currentPlayerDef = RpgGame.party.get(partyMemberIndex);
        displayOptions(new FightMenuState(getCurrentPlayerName(), getPartyMemberOptions()));
    }
    
    protected final static String getCurrentPlayerName() {
        return currentPlayerDef.stats.getName();
    }
    
    protected final static void startPickingEnemyTarget(final String title, final boolean allAllowed, final TargetedEnemyHandler targetedEnemyHandler) {
        Fight.targetedEnemyHandler = targetedEnemyHandler;
        displayOptions(new FightMenuState(title, getEnemyTargetOptions(allAllowed)));
    }
    
    protected final static void startEnemyTurn() {
        //TODO
    }
    
    protected final static void incrementTurn() {
        partyMemberIndex++;
        final int partySize = playerTurn ? RpgGame.party.size() : enemyParty.size();
        if (partyMemberIndex >= partySize) {
            partyMemberIndex = 0;
            playerTurn = !playerTurn;
        }
    }
    
    protected final static void displayOptions(final FightMenuState state) {
        stack.push(state);
        displayOptions();
    }
    
    protected final static void displayOptions() {
        final FightMenuState state = stack.peek();
        final List<FightOption> options = state.options;
        final int startIndex = state.startIndex;
        final int numOptions = options.size();
        for (int i = 0; i < pageSize; i++) {
            final int optionIndex = startIndex + i;
            if (optionIndex >= numOptions) {
                break;
            }
            final FightOption option = options.get(optionIndex);
            Chartil.set(optionTexts.get(i).buffer, option.label);
        }
    }
    
    protected static abstract class FightOption {
        protected final String label;
        
        protected FightOption(final String label) {
            this.label = label;
        }
        
        //@OverrideMe
        protected void onHighlight() {
        }
        
        protected abstract void onSelect();
    }
    
    protected final static FightOption backOption = new FightOption("Back") {
        @Override protected final void onSelect() {
            stack.pop();
            displayOptions();
        }
    };
    
    protected final static FightOption attackOption = new FightOption("Attack") {
        @Override protected final void onSelect() {
            //TODO If player has multi-attack, should allow all
            startPickingEnemyTarget(getCurrentPlayerName() + " Attack", false, attackTargetedEnemyHandler);
        }
    };
    
    protected final static void attack(final EnemyFighter enemy) {
        //TODO
        incrementTurn();
    }
    
    protected final static List<FightOption> getPartyMemberOptions() {
        partyMemberOptions.clear();
        partyMemberOptions.add(attackOption);
        //TODO items/magic/defense
        return partyMemberOptions;
    }
    
    protected final static class EnemyTargetOption extends FightOption {
        private final EnemyFighter enemy;
        
        protected EnemyTargetOption(final EnemyFighter enemy) {
            super((enemy == null) ? "All" : enemy.name);
            this.enemy = enemy;
        }
        
        @Override protected final void onSelect() {
            if (enemy == null) {
                for (final EnemyFighter enemy : enemyParty) {
                    targetedEnemyHandler.handle(enemy);
                }
                return;
            }
            targetedEnemyHandler.handle(enemy);
        }
    }
    
    protected final static EnemyTargetOption allEnemyTargetOption = new EnemyTargetOption(null);
    
    protected final static List<FightOption> getEnemyTargetOptions(final boolean allAllowed) {
        enemyTargetOptions.clear();
        for (final EnemyFighter enemy : enemyParty) {
            enemyTargetOptions.add(enemy.targetOption);
        }
        if (allAllowed) {
            enemyTargetOptions.add(allEnemyTargetOption);
        }
        enemyTargetOptions.add(backOption);
        return enemyTargetOptions;
    }
    
    protected abstract static class TargetedEnemyHandler {
        protected abstract void handle(final EnemyFighter enemy);
    }
    
    protected final static TargetedEnemyHandler attackTargetedEnemyHandler = new TargetedEnemyHandler() {
        @Override protected final void handle(final EnemyFighter enemy) {
            attack(enemy);
        }
    };
    
    protected final static class OptionText {
        private final StringBuilder buffer = new StringBuilder(optionWidth);
        private final Pantext text;
        private final TouchButton button;
        
        protected OptionText(final int x, final int y, final ActionEndListener listener) {
            text = new Pantext(Pantil.vmid(), RpgGame.hudFont, buffer);
            text.getPosition().set(x, y);
            if (listener == null) {
                button = null;
            } else {
                button = newTouchButton(x, y - fontMargin, 128, optionHeight);
                text.register(button, listener);
            }
        }
        
        protected final void attach() {
            layer.addActor(text);
            if (button != null) {
                Pangine.getEngine().registerTouchButton(button);
            }
        }
        
        protected final void detach() {
            text.detach();
            if (button != null) {
                Pangine.getEngine().unregisterTouchButton(button);
            }
        }
    }
    
    protected final static class FightMenuState {
        protected final String title;
        protected final List<FightOption> options;
        protected int startIndex = 0;
        
        protected FightMenuState(final String title, final List<FightOption> options) {
            this.title = title;
            this.options = options;
        }
    }
    
    protected final static class EnemyFighter implements Comparable<EnemyFighter> {
        private final String name; // Can be enemy type + index if party contains more than 1 of given type
        private final ChrDefinition def;
        private final EnemyTargetOption targetOption;
        private final Panctor avatar; // Will be Chr for humanoid enemies
        
        protected EnemyFighter(final String name, final ChrDefinition def, final Panctor avatar) {
            this.name = name;
            this.def = def;
            targetOption = new EnemyTargetOption(this);
            this.avatar = avatar;
            layer.addActor(avatar);
        }

        @Override
        public final int compareTo(final EnemyFighter o) {
            final float h = getHeight(), oh = o.getHeight();
            if (h > oh) {
                return -1;
            } else if (h < oh) {
                return 1;
            }
            return name.compareTo(o.name);
        }
        
        protected final float getHeight() {
            return avatar.getCurrentDisplay().getBoundingMaximum().getY();
        }
    }
    
    protected final static class PlayerFighter {
        private final ChrDefinition def;
        private final Chr avatar;
        
        protected PlayerFighter(final ChrDefinition def) {
            this.def = def;
            avatar = new Chr(def);
            final int partySize = playerParty.size();
            avatar.getPosition().set(RpgGame.GAME_W - 32, partyMaxY - (partySize * partyOffY), partyMinZ + (partySize * 2));
            avatar.setDirection(Direction.West);
            layer.addActor(avatar);
        }
    }
}
