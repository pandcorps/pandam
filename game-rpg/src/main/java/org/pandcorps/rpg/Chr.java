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

import org.pandcorps.core.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.tile.*;

public class Chr extends Guy4 {
    private ChrDefinition def = null;
    
    protected Chr(final ChrDefinition def) {
        this.def = def;
        setView(RpgGame.chrBox);
        setSpeed(1);
        face(Direction.South);
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
        final Armor armor = def.stats.getArmor();
        if (armor == null) {
            def.clothing.render(renderer, this); // Might make sense to render this under armor if armor present
        } else {
            armor.renderData.render(renderer, this);
        }
        def.body.render(renderer, this);
    }
    
    protected final static class ChrDefinition {
        //private int bodyTypeX; //TODO group these into single object?
        //private int bodyTypeY;
        //private int armorX;
        //private int armorY;
        protected final ChrComponent body = new ChrComponent();
        protected final Eye eyeLeft = new Eye();
        protected final Eye eyeRight = new Eye();
        protected final ChrComponent hair = new ChrComponent();
        protected final ChrComponent clothing = new ChrComponent();
        protected final ChrStats stats;
        private int health;
        private int magic;
        private int experience; // Money/inventory tied to party, not a specific character
        
        protected ChrDefinition(final ChrStats stats) {
            this.stats = stats;
        }
        
        protected void load(final Segment seg) {
            body.load(seg, 0);
            eyeLeft.x = seg.floatValue(5);
            eyeLeft.y = seg.floatValue(6);
            eyeRight.x = seg.floatValue(7);
            eyeRight.y = seg.floatValue(8);
            hair.load(seg, 9);
            clothing.load(seg, 14);
            health = seg.intValue(19);
            magic = seg.intValue(20);
            experience = seg.intValue(21);
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
        
        protected final void randomizeAppearance() {
            body.x = 0; //TODO Pick from available types
            body.y = 0;
            body.r = 0.8f; //TODO Use thresholds from race/sub-race
            body.g = 0.6f;
            body.b = 0.4f;
            eyeLeft.x = 0; //TODO Pick from available eye types
            eyeLeft.y = 0;
            eyeRight.x = 0;
            eyeRight.y = 0;
            hair.x = 0; //TODO Pick from available hair styles
            hair.y = 16;
            hair.r = 0.5f;
            hair.g = 0.25f;
            hair.b = 0.125f;
            clothing.x = 0; //TODO Pick from available clothing styles
            clothing.y = 32;
            clothing.r = 0.25f;
            clothing.g = 0.75f;
            clothing.b = 0.5f;
        }
    }
    
    protected final static int STAT_MAX_HEALTH = 0;
    protected final static int STAT_MAX_MAGIC = 1;
    protected final static int STAT_STRENGTH = 2; // Base attack, independent of weapon quality
    protected final static int STAT_ENDURANCE = 3; // Base defense, independent of armor quality 
    protected final static String[] STAT_NAMES = { "Max Health", "Max Magic", "Attack", "Defense" };
    protected final static int STATS_SIZE = STAT_NAMES.length;
    
    protected abstract static class BaseStats implements Named {
        private String name;
        private final int[] values = new int[STATS_SIZE];
        
        protected BaseStats(final String name) {
            this.name = name;
        }
        
        protected BaseStats(final Segment seg) {
            name = seg.getValue(0);
            final List<Field> valueFields = seg.getRepetitions(1);
            for (int i = 0; i < STATS_SIZE; i++) {
                set(i, valueFields.get(i).intValue());
            }
        }
        
        @Override
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
            super(seg); // 0 - name, 1 - stats
            seg.getField(2); //TODO attributes
            final List<Field> gearFields = seg.getRepetitions(3);
            final int gearSize = gearFields.size();
            for (int i = 0; i < gearSize; i++) {
                gears[i] = getGear(gearFields.get(i).getValue());
            }
        }
        
        protected final Armor getArmor() {
            return (Armor) gears[GEAR_SLOT_ARMOR];
        }
    }
    
    protected final static int GEAR_TYPE_ARMOR = 1;
    protected final static int GEAR_TYPE_WEAPON = 2;
    protected final static int GEAR_TYPE_SHIELD = 3;
    
    protected abstract static class Gear extends BaseStats {
        /*
        Gear might augment base stats, but attack power isn't just added to base strength when equipped.
        Otherwise, a character that does consecutive attacks with 2 weapons would combine attack power of each weapon with each strike.
        Keeping them separate, the strike with the first weapon can incorporate the character's strength with only the first weapon's quality.
        Then the strike with the second weapon incorporates the base strength with only the second weapon's quality.
        */
        private final int type;
        private final SmithingQuality quality;
        private final Material material;
        private final GearSubtype subtype;
        
        protected Gear(final String name, final int type, final SmithingQuality quality, final Material material, final GearSubtype subtype) {
            super(name);
            this.type = type;
            this.quality = quality;
            this.material = material;
            this.subtype = subtype;
        }
        
        protected Gear(final Segment seg, final int type) {
            super(seg);
            this.type = type;
            this.quality = getSmithingQuality(seg.getValue(2)); // 0 - name, 1 - stats
            this.material = getMaterial(seg.getValue(3));
            this.subtype = getGearSubtype(seg.getValue(4));
        }
        
        public final int getType() {
            return type;
        }
        
        public final GearSubtype getSubtype() {
            return subtype;
        }
        
        public final int getQuality() {
            return quality.getMultiplier() * material.getMultiplier() * subtype.getMultiplier();
        }
    }
    
    protected final static class Armor extends Gear {
        private final ChrComponent renderData;
        
        protected Armor(final String name, final SmithingQuality quality, final Material material, final GearSubtype subtype, final ChrComponent renderData) {
            super(name, GEAR_TYPE_ARMOR, quality, material, subtype);
            this.renderData = renderData;
        }
        
        protected Armor(final Segment seg) {
            super(seg, GEAR_TYPE_ARMOR);
            this.renderData = new ChrComponent(seg, 5);
        }
    }
    
    protected final static class Weapon extends Gear {
        protected Weapon(final String name, final SmithingQuality quality, final Material material, final GearSubtype subtype) {
            super(name, GEAR_TYPE_WEAPON, quality, material, subtype);
        }
        
        protected Weapon(final Segment seg) {
            super(seg, GEAR_TYPE_WEAPON);
        }
    }
    
    protected final static class Shield extends Gear {
        protected Shield(final String name, final SmithingQuality quality, final Material material, final GearSubtype subtype) {
            super(name, GEAR_TYPE_SHIELD, quality, material, subtype);
        }
        
        protected Shield(final Segment seg) {
            super(seg, GEAR_TYPE_SHIELD);
        }
    }
    
    private final static Map<String, Gear> gearMap = new LinkedHashMap<String, Gear>();
    
    protected final static Gear getGear(final String name) {
        return gearMap.get(name);
    }
    
    protected abstract static class GearAttribute implements Named {
        private final String name;
        private final int multiplier;
        
        protected GearAttribute(final Segment seg) {
            name = seg.getValue(0);
            multiplier = seg.intValue(1);
        }
        
        @Override
        public final String getName() {
            return name;
        }
        
        public final int getMultiplier() {
            return multiplier;
        }
    }
    
    // Poor/fair/superior/flawless/etc.
    protected final static class SmithingQuality extends GearAttribute {
        
        protected SmithingQuality(final Segment seg) {
            super(seg);
        }
    }
    
    private final static Map<String, SmithingQuality> smithingQualityMap = new LinkedHashMap<String, SmithingQuality>();
    
    protected final static SmithingQuality getSmithingQuality(final String name) {
        return smithingQualityMap.get(name);
    }
    
    // Copper/iron/etc.
    protected final static class Material extends GearAttribute {
        private final float r;
        private final float g;
        private final float b;
        
        protected Material(final Segment seg) {
            super(seg);
            r = seg.floatValue(2);
            g = seg.floatValue(3);
            b = seg.floatValue(4);
        }
    }
    
    private final static Map<String, Material> materialMap = new LinkedHashMap<String, Material>();
    
    protected final static Material getMaterial(final String name) {
        return materialMap.get(name);
    }
    
    // Weapon sub-types like dagger/sword/spear/bow/etc.
    protected static class GearSubtype extends GearAttribute {
        private final int type;
        private final float renderX;
        private final float renderY;
        //TODO stats/restrictions, dagger increases evade ability but can't be used with heavy armor
        
        protected GearSubtype(final Segment seg) {
            super(seg);
            type = seg.intValue(2); // 0 - name, 1 - multiplier
            renderX = seg.getFloat(3, -1);
            renderY = seg.getFloat(4, -1);
        }
    }
    
    private final static Map<String, GearSubtype> gearSubtypeMap = new LinkedHashMap<String, GearSubtype>();
    
    protected final static GearSubtype getGearSubtype(final String name) {
        return gearSubtypeMap.get(name);
    }
    
    protected static interface Named {
        public String getName();
    }
    
    protected final static void loadData() {
        SegmentStream in = null;
        try {
            in = SegmentStream.openLocation(RpgGame.RES + "Data.txt");
            Segment seg = null;
            while ((seg = in.readIf("QTY")) != null) {
                put(smithingQualityMap, new SmithingQuality(seg));
            }
            while ((seg = in.readIf("MAT")) != null) {
                put(materialMap, new Material(seg));
            }
            while ((seg = in.readIf("SUB")) != null) {
                put(gearSubtypeMap, new GearSubtype(seg));
            }
            generateGear();
            Enemy.loadEnemyData(in);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        } finally {
            Iotil.close(in);
        }
    }
    
    protected final static void generateGear() {
        for (final Material material : materialMap.values()) {
            for (final GearSubtype subtype : gearSubtypeMap.values()) {
                final ChrComponent renderData;
                if (subtype.renderX < 0) {
                    renderData = null;
                } else {
                    renderData = new ChrComponent(subtype.renderX, subtype.renderY, material.r, material.g, material.b);
                }
                for (final SmithingQuality quality : smithingQualityMap.values()) {
                    put(gearMap, newGear(quality, material, subtype, renderData));
                }
            }
        }
    }
    
    protected final static Gear newGear(final SmithingQuality quality, final Material material, final GearSubtype subtype, final ChrComponent renderData) {
        final String name = quality.getName() + " " + material.getName() + " " + subtype.getName();
        switch (subtype.type) {
            case GEAR_TYPE_ARMOR:
                return new Armor(name, quality, material, subtype, renderData);
            case GEAR_TYPE_WEAPON:
                return new Weapon(name, quality, material, subtype);
            case GEAR_TYPE_SHIELD:
                return new Shield(name, quality, material, subtype);
        }
        throw new IllegalStateException("Unexpected Gear type " + subtype.type + " for " + name);
    }
    
    protected final static <V extends Named> void put(final Map<String, V> map, final V value) {
        final V old = map.put(value.getName(), value);
        if (old != null) {
            throw new IllegalStateException("Found multiple " + value.getClass().getSimpleName() + " instances for " + value.getName());
        }
    }
    
    protected final static class ChrComponent {
        private float x;
        private float y;
        private float r;
        private float g;
        private float b;
        
        protected ChrComponent() {
        }
        
        protected ChrComponent(final float x, final float y, final float r, final float g, final float b) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.g = g;
            this.b = b;
        }
        
        protected ChrComponent(final Segment seg, final int i) {
            load(seg, i);
        }
        
        protected void load(final Segment seg, final int i) {
            this.x = seg.floatValue(i);
            this.y = seg.floatValue(i + 1);
            this.r = seg.floatValue(i + 2);
            this.g = seg.floatValue(i + 3);
            this.b = seg.floatValue(i + 4);
        }
        
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
    
    protected final static ChrComponent newChrComponent(final Segment seg, final int i) {
        return Chartil.isEmpty(seg.getValue(i)) ? null : new ChrComponent(seg, i);
    }
    
    protected final static class Eye {
        private float x;
        private float y;
    }
}
