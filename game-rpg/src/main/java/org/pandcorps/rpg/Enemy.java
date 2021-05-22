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

import java.io.*;
import java.util.*;
import java.util.Map.*;

import org.pandcorps.core.col.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.rpg.Chr.*;
import org.pandcorps.rpg.Fight.*;

public class Enemy {
    protected final static Map<String, EnemyAction> actionMap = new HashMap<String, EnemyAction>();
    protected final static Map<String, EnemyDefinition> enemyMap = new HashMap<String, EnemyDefinition>();
    
    protected final static void loadEnemyData(final SegmentStream in) throws IOException {
        Segment seg = null;
        while ((seg = in.readIf("ENM")) != null) {
            final Segment extraSeg = in.read();
            final EnemyDefinition def = newEnemyDefinition(seg, extraSeg);
            Chr.put(enemyMap, def);
        }
    }
    
    protected final static EnemyDefinition newEnemyDefinition(final Segment statsSeg, final Segment extraSeg) {
        final String name = extraSeg.getName();
        if ("ENS".equals(name)) {
            return new SimpleEnemyDefinition(statsSeg, extraSeg);
        } else if ("ENC".equals(name)) {
            return new ChrEnemyDefinition(statsSeg, extraSeg);
        }
        throw new IllegalArgumentException("Unrecognized EnemyDefinition type " + name);
    }
    
    protected final static void initEnemyParty(final List<EnemyFighter> party, final CountMap<EnemyDefinition> defs) {
        for (final Entry<EnemyDefinition, Long> entry : defs.entrySet()) {
            final EnemyDefinition def = entry.getKey();
            String baseName = def.getName();
            final int size = entry.getValue().intValue();
            final boolean multiple = size > 1;
            if (multiple) {
                baseName += " ";
            }
            for (int i = 1; i <= size; i++) {
                final String name = multiple ? (baseName + i) : baseName;
                party.add(def.spawn(name));
            }
        }
    }
    
    protected abstract static class EnemyDefinition implements Named {
        // Might have more than needed for simple enemies, but BaseStats doesn't have enough (like health); makes things easy to use this for all enemies
        protected final ChrStats stats;
        protected final List<EnemyAction> actions;
        
        protected EnemyDefinition(final Segment statsSeg, final Segment extraSeg) {
            stats = new ChrStats(statsSeg);
            final List<Field> actionFields = extraSeg.getRepetitions(0);
            actions = new ArrayList<EnemyAction>(actionFields.size());
            for (final Field field : actionFields) {
                actions.add(actionMap.get(field.getValue()));
            }
        }
        
        @Override
        public final String getName() {
            return getStats().getName();
        }
        
        protected final ChrStats getStats() {
            return stats;
        }
        
        protected abstract EnemyFighter spawn(final String name);
        
        @Override
        public final boolean equals(final Object o) {
            return (this == o) || getName().equals(((EnemyDefinition) o).getName());
        }
        
        @Override
        public final int hashCode() {
            return getName().hashCode();
        }
    }
    
    protected static class SimpleEnemyDefinition extends EnemyDefinition {
        // simple animal/monster enemies
        protected final Panmage image;
        
        protected SimpleEnemyDefinition(final Segment statsSeg, final Segment extraSeg) {
            super(statsSeg, extraSeg);
            final String name = getName();
            image = Pangine.getEngine().createImage("enemy." + name, RpgGame.RES + "enemy/" + name + ".png");
        }
        
        @Override
        protected final EnemyFighter spawn(final String name) {
            throw new UnsupportedOperationException(); //TODO
        }
    }
    
    protected static class ChrEnemyDefinition extends EnemyDefinition {
        // Will customize hair style/color, skin color, so can't use ChrDefinition
        // Might want to customize gear, so maybe ChrStats doens't work well; or maybe just allow overrides
        // Probably have similar definitions with different adjectives for different skill levels; look similar but better stats/gear
        // If can't think of anything else to add here, just use ChrStats instead of this wrapper?
        
        protected ChrEnemyDefinition(final Segment statsSeg, final Segment extraSeg) {
            super(statsSeg, extraSeg);
        }
        
        @Override
        protected final EnemyFighter spawn(final String name) {
            final ChrDefinition def = new ChrDefinition(stats);
            def.randomizeAppearance();
            final Chr chr = new Chr(def);
            chr.setDirection(Direction.East);
            return new EnemyFighter(name, def, chr); // Name could be type plus index if party has more than one of same type
        }
    }
    
    protected static class EnemyAction {
        // Weight?
        protected boolean isAllowed() {
            throw new UnsupportedOperationException(); //TODO check if possible (has enough MP) and if helpful (don't heal if at full health)
        }
        
        protected void perform() {
            throw new UnsupportedOperationException(); //TODO
        }
    }
}
