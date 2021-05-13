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

import org.pandcorps.core.seg.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.tile.*;

public class Chr extends Guy4 {
    private ChrDefinition def = null;
    
    protected Chr(final ChrDefinition def) {
        this.def = def;
        setSpeed(1);
        face(Direction.South);
    }
    
    protected final static ChrDefinition newSampleDefinition() {
        final ChrDefinition def = new ChrDefinition(null);
        /*def.bodyTypeX = 0;
        def.bodyTypeY = 0;
        def.armorX = 0;
        def.armorY = 32;*/
        def.body = new ChrComponent();
        def.eyeLeft = new Eye();
        def.eyeRight = new Eye();
        def.hair = new ChrComponent();
        def.armor = new ChrComponent();
        def.body.x = 0;
        def.body.y = 0;
        def.body.r = 0;
        def.body.g = 1;
        def.body.b = 1;
        def.eyeLeft.x = 0;
        def.eyeLeft.y = 0;
        def.eyeRight.x = 0;
        def.eyeRight.y = 0;
        def.hair.x = 0;
        def.hair.y = 16;
        def.hair.r = 0;
        def.hair.g = 0;
        def.hair.b = 1;
        def.armor.x = 0;
        def.armor.y = 32;
        def.armor.r = 0.5f;
        def.armor.g = 0.5f;
        def.armor.b = 0.5f;
        return def;
    }
    
    public final void setDirection(final Direction dir) {
        face(dir);
    }
    
    @Override
    protected final void renderView(final Panderer renderer) {
        final Panlayer layer = getLayer();
        final Panple pos = getPosition();
        final float x = pos.getX(), y = pos.getY(), z = pos.getZ();
        //renderer.render(layer, RpgGame.chrImage, x - 8, y - 4, z, def.armorX, def.armorY, 32, 32, 0, false, false, 0.5f, 0.5f, 0.5f);
        //renderer.render(layer, RpgGame.chrImage, x - 8, y - 4, z, def.bodyTypeX, def.bodyTypeY, 32, 32, 0, false, false, 0, 1, 1);
        def.hair.renderHair(renderer, this);
        final Direction dir = getDirection();
        if (dir != Direction.North) {
            final boolean m = dir == Direction.West;
            final float lxo, rxo;
            if (dir == Direction.South) {
                lxo = 4.0f;
                rxo = 8.0f;
            } else if (dir == Direction.East) {
                lxo = 5.0f;
                rxo = 9.0f;
            } else {
                lxo = 7.0f;
                rxo = 3.0f;
            }
            renderer.render(layer, RpgGame.eyesImage, x + lxo, y + 15, z, def.eyeLeft.x, def.eyeLeft.y, 4, 4, 0, m, false, 1, 1, 1);
            renderer.render(layer, RpgGame.eyesImage, x + rxo, y + 15, z, def.eyeRight.x, def.eyeRight.y, 4, 4, 0, !m, false, 1, 1, 1);
        }
        def.armor.render(renderer, this);
        def.body.render(renderer, this);
    }
    
    protected final static class ChrDefinition {
        //private int bodyTypeX; //TODO group these into single object?
        //private int bodyTypeY;
        //private int armorX;
        //private int armorY;
        private ChrComponent body;
        private Eye eyeLeft;
        private Eye eyeRight;
        private ChrComponent hair;
        private ChrComponent armor;
        protected final ChrStats stats;
        private int health;
        private int magic;
        private int experience; // Money/inventory tied to party, not a specific character
        
        protected ChrDefinition(final ChrStats stats) {
            this.stats = stats;
        }
        
        public final int getEffective(final int statType) {
            int total = stats.get(statType);
            for (final Gear gear : stats.gears) {
                if (gear != null) {
                    total += gear.get(statType);
                }
            }
            //TODO temporary status effects
            return total;
        }
    }
    
    protected final static int STAT_MAX_HEALTH = 0;
    protected final static int STAT_MAX_MAGIC = 1;
    protected final static int STAT_ATTACK = 2;
    protected final static int STAT_DEFENSE = 3;
    protected final static String[] STAT_NAMES = { "Max Health", "Max Magic", "Attack", "Defense" };
    protected final static int STATS_SIZE = STAT_NAMES.length;
    
    protected abstract static class BaseStats {
        private String name;
        private final int[] values = new int[STATS_SIZE];
        
        protected BaseStats(final Segment seg) {
            name = seg.getValue(0);
            final List<Field> valueFields = seg.getRepetitions(1);
            for (int i = 0; i < STATS_SIZE; i++) {
                set(i, valueFields.get(i).intValue());
            }
        }
        
        public final String getName() {
            return name;
        }
        
        public final void setName(final String name) {
            this.name = name;
        }
        
        public final int get(final int statType) {
            return values[statType];
        }
        
        public final void set(final int statType, final int value) {
            values[statType] = value;
        }
    }
    
    protected final static int GEAR_SLOT_ARMOR = 0;
    protected final static int GEAR_SLOT_HAND1 = 1;
    protected final static int GEAR_SLOT_HAND2 = 2;
    protected final static String[] GEAR_SLOT_NAMES = { "Armor", "Hand 1", "Hand 2" };
    protected final static int GEAR_SLOTS_SIZE = GEAR_SLOT_NAMES.length;
    
    protected final static class ChrStats extends BaseStats {
        //TODO race, element
        private final Gear[] gears = new Gear[GEAR_SLOTS_SIZE];
        
        protected ChrStats(final Segment seg) {
            super(seg);
            seg.getField(2); //TODO attributes
            seg.getRepetitions(3); //TODO gear
        }
    }
    
    protected final static int GEAR_TYPE_ARMOR = 1;
    protected final static int GEAR_TYPE_WEAPON = 2;
    protected final static int GEAR_TYPE_SHIELD = 3;
    
    protected static class Gear extends BaseStats {
        private int type;
        
        protected Gear(final Segment seg) {
            super(seg);
            type = seg.intValue(2);
        }
        
        public int getType() {
            return type;
        }
        
        public void setType(final int type) {
            this.type = type;
        }
    }
    
    protected final static class ChrComponent {
        private float x;
        private float y;
        private float r;
        private float g;
        private float b;
        
        private final void render(final Panderer renderer, final Chr chr) {
            //render(renderer, RpgGame.chrImage, chr, -8.0f, -4.0f, 64.0f, 160.0f, 32.0f);
            final Panple pos = chr.getPosition();
            final float px = pos.getX(), py = pos.getY();
            final Direction dir = chr.getDirection();
            final float io;
            final boolean m;
            if (dir == Direction.South) {
                final int y16 = Math.round(py) % 16;
                if (y16 >= 12) {
                    io = 32.0f; m = false;
                } else if ((y16 >= 4) && (y16 < 8)) {
                    io = 32.0f; m = true;
                } else {
                    io = 0.0f; m = false;
                }
            } else if ((dir == Direction.West) || (dir == Direction.East)) {
                final int _x16 = Math.round(px) % 16;
                final int x16 = (dir == Direction.East) ? _x16 : (16 - _x16);
                if ((x16 > 0) && x16 <= 4) {
                    io = 96.0f;
                } else if ((x16 > 8) && (x16 <= 12)) {
                    io = 128.0f;
                } else {
                    io = 64.0f;
                }
                m = dir == Direction.West;
            } else {
                final int y16 = Math.round(py) % 16;
                if ((y16 > 0) && (y16 <= 4)) {
                    io = 192.0f; m = false;
                } else if ((y16 > 8) && (y16 <= 12)) {
                    io = 192.0f; m = true;
                } else {
                    io = 160.0f; m = false;
                }
            }
            renderer.render(chr.getLayer(), RpgGame.chrImage, px - 8.0f, py - 4.0f, pos.getZ(), x + io, y, 32.0f, 32.0f, 0, m, false, r, g, b);
        }
        
        private final void renderHair(final Panderer renderer, final Chr chr) {
            //render(renderer, RpgGame.hairImage, chr, 0.0f, 8.0f, 16.0f, 32.0f, 16.0f);
            final Panple pos = chr.getPosition();
            final Direction dir = chr.getDirection();
            final float io;
            if (dir == Direction.South) {
                io = 0.0f;
            } else if ((dir == Direction.West) || (dir == Direction.East)) {
                io = 16.0f;
            } else {
                io = 32.0f;
            }
            renderer.render(chr.getLayer(), RpgGame.hairImage, pos.getX() + 0.0f, pos.getY() + 8.0f, pos.getZ(), x + io, y, 16.0f, 16.0f, 0, dir == Direction.West, false, r, g, b);
        }
        
        /*private final void render(final Panderer renderer, final Panmage image, final Chr chr, final float xo, final float yo, final float ioEast, final float ioNorth, final float d) {
            final Panple pos = chr.getPosition();
            final Direction dir = chr.getDirection();
            final float io;
            if (dir == Direction.South) {
                io = 0.0f;
            } else if ((dir == Direction.West) || (dir == Direction.East)) {
                io = ioEast;
            } else {
                io = ioNorth;
            }
            renderer.render(chr.getLayer(), image, pos.getX() + xo, pos.getY() + yo, pos.getZ(), x + io, y, d, d, 0, dir == Direction.West, false, r, g, b);
        }*/
    }
    
    protected final static class Eye {
        private float x;
        private float y;
    }
}
