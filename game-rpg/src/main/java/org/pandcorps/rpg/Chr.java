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
import org.pandcorps.core.img.*;
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
        if ((armor == null) || (armor.renderData == null)) {
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
        private Race race;
        private Subrace subrace;
        
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
            race = getRace(seg.getValue(22));
            subrace = getSubrace(seg.getValue(23));
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
        
        public final int getDefenseRating() {
            final int endurance = getEffective(STAT_ENDURANCE);
            final Armor armor = stats.getArmor();
            final int armorQuality = armor.getQuality();
            final int armorContribution = armorQuality * gearTypeArmor.getMultiplier();
            final Shield shield = stats.getShield();
            final int shieldQuality = (shield == null) ? 0 : shield.getQuality();
            final int shieldContribution = shieldQuality * gearTypeShield.getMultiplier();
            final int gearContribution = armorContribution + shieldContribution;
            return endurance * gearContribution;
        }
        
        public final int getAttackRating(final Weapon weapon) {
            final int strength = getEffective(Chr.STAT_STRENGTH);
            final int weaponQuality = weapon.getQuality();
            final int weaponContribution = weaponQuality * gearTypeWeapon.getMultiplier();
            return strength * weaponContribution;
        }
        
        public final boolean attack(final int damage) {
            health = Math.max(0, health - damage);
            return health <= 0;
        }
        
        protected final void setRace(final Race race, final Subrace subrace) {
            this.race = race;
            this.subrace = subrace;
        }
        
        protected final void randomizeAppearance() {
            body.x = 0; //TODO Pick from available types
            body.y = 0;
            final FloatColor baseBodyColor = subrace.baseColor;
            //body.color.set(subrace.baseColor);
            //body.color.addAll(Mathtil.randi(0, 4) * 0.03125f); // 8.0f / 256.0f
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
    
    // Water/Fire/etc.
    protected final static class Element implements Named {
        private final String name;
        private final Map<String, String> advantages = new HashMap<String, String>();
        
        protected Element(final Segment seg) {
            name = seg.getValue(0);
            final List<Field> advantageFields = seg.getRepetitions(1);
            for (final Field field : Coltil.unnull(advantageFields)) {
                final String verb = field.getValue(0);
                final String targetElement = field.getValue(1);
                advantages.put(targetElement, verb);
            }
        }
        
        @Override
        public final String getName() {
            return name;
        }
    }
    
    private final static Map<String, Element> elementMap = new LinkedHashMap<String, Element>();
    
    protected final static Element getElement(final String name) {
        return get(elementMap, name);
    }
    
    protected final static class Race implements Named {
        private final Names names;
        private final Names elementalNames; // If a different name should be used for individuals with an elemental type
        private final boolean elementalSubracePossible;
        private final List<Subrace> subraces = new ArrayList<Subrace>();
        
        protected Race(final Segment seg) {
            names = new Names(seg.getField(0));
            final Field elementalField = seg.getField(1);
            elementalNames = (elementalField == null) ? names : new Names(elementalField);
            elementalSubracePossible = seg.booleanValue(2);
        }
        
        @Override
        public final String getName() {
            return names.getName();
        }
        
        public final Names getNames() {
            return names;
        }
        
        public final Names getElementalNames() {
            return elementalNames;
        }
        
        public final List<Subrace> getSubraces() {
            return subraces;
        }
        
        public final List<Subrace> getElementalSubraces() {
            return elementalSubracePossible ? elementalSubraces : null;
        }
    }
    
    private final static Map<String, Race> raceMap = new LinkedHashMap<String, Race>();
    
    protected final static Race getRace(final String name) {
        return get(raceMap, name);
    }
    
    protected final static class Subrace implements Named {
        private final String name; // Term for an individual member of subrace
        private final boolean prefix;
        private final Element element;
        private final Race race;
        private final String groupName; // Term for the subrace as a group
        private final FloatColor baseColor = new FloatColor();
        
        protected Subrace(final Segment seg) {
            name = seg.getValue(0);
            prefix = seg.booleanValue(1);
            element = getElement(seg.getValue(2));
            if (element != null) {
                elementalSubraces.add(this);
            }
            race = Chr.getRace(seg.getValue(3));
            if (race != null) {
                race.subraces.add(this);
            }
            groupName = seg.getValue(4);
            baseColor.set(seg, 5);
        }
        
        @Override
        public final String getName() {
            return name;
        }
        
        public final String getName(final Race race) {
            if (prefix) {
                final String raceName = (element == null) ? race.getName() : race.getElementalNames().getName();
                return (name + " " + raceName);
            }
            return name;
        }
        
        public final Race getRace() {
            return race;
        }
    }
    
    private final static Map<String, Subrace> subraceMap = new LinkedHashMap<String, Subrace>();
    
    protected final static Subrace getSubrace(final String name) {
        return get(subraceMap, name);
    }
    
    private final static List<Subrace> elementalSubraces = new ArrayList<Subrace>();
    
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
        private final Gear[] gears = new Gear[GEAR_SLOTS_SIZE];
        private final Map<String, Skill> skills = new LinkedHashMap<String, Skill>();
        
        protected ChrStats(final Segment seg) {
            super(seg); // 0 - name, 1 - stats
            seg.getField(2); //TODO attributes
            final List<Field> gearFields = seg.getRepetitions(3);
            final int gearSize = gearFields.size();
            for (int i = 0; i < gearSize; i++) {
                gears[i] = getGear(gearFields.get(i).getValue());
            }
            //TODO skills
        }
        
        protected final Armor getArmor() {
            final Gear gear = gears[GEAR_SLOT_ARMOR];
            if (gear == null) {
                return armorNone;
            }
            return (Armor) gear;
        }
        
        protected final void getWeapons(final List<Weapon> weapons) {
            weapons.clear();
            getWeapon(weapons, GEAR_SLOT_HAND1);
            getWeapon(weapons, GEAR_SLOT_HAND2);
            if (weapons.isEmpty()) {
                weapons.add(weaponNone);
            }
        }
        
        private final void getWeapon(final List<Weapon> weapons, final int slot) {
            final Gear gear = gears[slot];
            if (gear instanceof Weapon) {
                weapons.add((Weapon) gear);
            }
        }
        
        protected final Shield getShield() {
            for (int slot = GEAR_SLOT_HAND1; slot <= GEAR_SLOT_HAND2; slot++) {
                final Gear gear = gears[slot];
                if (gear instanceof Shield) {
                    return (Shield) gear;
                }
            }
            return null;
        }
        
        protected void equip(final Player player, final Gear gear, final int slot) {
            final boolean hand = (slot == GEAR_SLOT_HAND1) || (slot == GEAR_SLOT_HAND2);
            if (hand && (isTwoHanded(gear) || isTwoHanded(gears[GEAR_SLOT_HAND1]) || isTwoHanded(gears[GEAR_SLOT_HAND2]))) {
                unequip(player, GEAR_SLOT_HAND1);
                unequip(player, GEAR_SLOT_HAND2);
            } else {
                unequip(player, slot);
            }
            player.removeInventory(gear, 1);
            gears[slot] = gear;
        }
        
        protected void unequip(final Player player, final int slot) {
            player.addInventory(gears[slot], 1);
            gears[slot] = null;
        }
    }
    
    protected final static class SkillCategory implements Named {
        private final String name;
        
        protected SkillCategory(final String name) {
            this.name = name;
        }
        
        @Override
        public final String getName() {
            return name;
        }
    }
    
    private final static Map<String, SkillCategory> skillCategoryMap = new LinkedHashMap<String, SkillCategory>();
    
    protected final static SkillCategory getSkillCategory(final String name) {
        return get(skillCategoryMap, name);
    }
    
    protected final static class Skill {
        private int level;
        private int experience;
        
        protected final int getLevel() {
            return level;
        }
        
        protected final int getExperience() {
            return experience;
        }
    }
    
    protected static interface Item extends Named {
        public boolean isUsable();
        
        public boolean isEquippable();
    }
    
    protected abstract static class Gear extends BaseStats implements Item {
        /*
        Gear might augment base stats, but attack power isn't just added to base strength when equipped.
        Otherwise, a character that does consecutive attacks with 2 weapons would combine attack power of each weapon with each strike.
        Keeping them separate, the strike with the first weapon can incorporate the character's strength with only the first weapon's quality.
        Then the strike with the second weapon incorporates the base strength with only the second weapon's quality.
        */
        private final SmithingQuality quality;
        private final Material material;
        private final GearSubtype subtype;
        
        protected Gear(final String name, final SmithingQuality quality, final Material material, final GearSubtype subtype) {
            super(name);
            this.quality = quality;
            this.material = material;
            this.subtype = subtype;
        }
        
        protected Gear(final Segment seg) {
            super(seg);
            this.quality = getSmithingQuality(seg.getValue(2)); // 0 - name, 1 - stats
            this.material = getMaterial(seg.getValue(3));
            this.subtype = getGearSubtype(seg.getValue(4));
        }
        
        public final GearSubtype getSubtype() {
            return subtype;
        }
        
        public final int getQuality() {
            return getMultiplier(quality) * getMultiplier(material);
        }
        
        @Override
        public final boolean isUsable() {
            return false;
        }
        
        @Override
        public final boolean isEquippable() {
            return true;
        }
    }
    
    protected final static boolean isTwoHanded(final Gear gear) {
        return (gear != null) && gear.subtype.twoHanded;
    }
    
    protected final static class Armor extends Gear {
        private final ChrComponent renderData;
        
        protected Armor(final String name, final SmithingQuality quality, final Material material, final GearSubtype subtype, final ChrComponent renderData) {
            super(name, quality, material, subtype);
            this.renderData = renderData;
        }
        
        protected Armor(final Segment seg) {
            super(seg);
            this.renderData = new ChrComponent(seg, 5);
        }
    }
    
    private static Armor armorNone = null;
    
    protected final static class Weapon extends Gear {
        protected Weapon(final String name, final SmithingQuality quality, final Material material, final GearSubtype subtype) {
            super(name, quality, material, subtype);
        }
        
        protected Weapon(final Segment seg) {
            super(seg);
        }
    }
    
    private static Weapon weaponNone = null;
    
    protected final static class Shield extends Gear {
        protected Shield(final String name, final SmithingQuality quality, final Material material, final GearSubtype subtype) {
            super(name, quality, material, subtype);
        }
        
        protected Shield(final Segment seg) {
            super(seg);
        }
    }
    
    private final static Map<String, Gear> gearMap = new LinkedHashMap<String, Gear>();
    
    protected final static Gear getGear(final String name) {
        return get(gearMap, name);
    }
    
    protected abstract static class GearAttribute implements Named {
        private final String name;
        private final int multiplier;
        
        protected GearAttribute(final Segment seg) {
            this(seg.getValue(0), seg.intValue(1));
        }
        
        protected GearAttribute(final String name, final int multiplier) {
            this.name = name;
            this.multiplier = multiplier;
        }
        
        @Override
        public final String getName() {
            return name;
        }
        
        public final int getMultiplier() {
            return multiplier;
        }
    }
    
    protected final static int getMultiplier(final GearAttribute a) {
        return (a == null) ? 1 : a.multiplier;
    }
    
    protected final static char GEAR_TYPE_ARMOR = 'A';
    protected final static char GEAR_TYPE_WEAPON = 'W';
    protected final static char GEAR_TYPE_SHIELD = 'C';
    
    protected final static class GearType extends GearAttribute {
        // Multiplier specifies importance of body armor vs. shield
        protected GearType(final Segment seg) {
            super(seg);
        }
        
        public final char getCode() {
            return getName().charAt(0);
        }
    }
    
    private static GearType gearTypeArmor = null;
    private static GearType gearTypeWeapon = null;
    private static GearType gearTypeShield = null;
    private final static Map<String, GearType> gearTypeMap = new LinkedHashMap<String, GearType>();
    
    protected final static GearType getGearType(final String name) {
        return get(gearTypeMap, name);
    }
    
    // Poor/fair/superior/flawless/etc.
    protected final static class SmithingQuality extends GearAttribute {
        
        protected SmithingQuality(final Segment seg) {
            super(seg);
        }
    }
    
    private final static Map<String, SmithingQuality> smithingQualityMap = new LinkedHashMap<String, SmithingQuality>();
    
    protected final static SmithingQuality getSmithingQuality(final String name) {
        return get(smithingQualityMap, name);
    }
    
    // Metal/wood/etc.
    protected final static class MaterialCategory implements Named {
        private final String name;
        private final List<Material> materials = new ArrayList<Material>();
        
        protected MaterialCategory(final Segment seg) {
            name = seg.getValue(0);
        }
        
        @Override
        public final String getName() {
            return name;
        }
    }
    
    private final static Map<String, MaterialCategory> materialCategoryMap = new LinkedHashMap<String, MaterialCategory>();
    
    protected final static MaterialCategory getMaterialCategory(final String name) {
        return get(materialCategoryMap, name);
    }
    
    // Copper/iron/etc.
    protected final static class Material extends GearAttribute {
        private final MaterialCategory category;
        private final float r;
        private final float g;
        private final float b;
        
        protected Material(final Segment seg) {
            super(seg);
            category = getMaterialCategory(seg.getValue(2));
            category.materials.add(this);
            r = seg.floatValue(3);
            g = seg.floatValue(4);
            b = seg.floatValue(5);
        }
    }
    
    private final static Map<String, Material> materialMap = new LinkedHashMap<String, Material>();
    
    protected final static Material getMaterial(final String name) {
        return get(materialMap, name);
    }
    
    // Ingots, etc.
    protected final static class MaterialItem implements Item {
        private final Material material;
        
        protected MaterialItem(final Material material) {
            this.material = material;
        }
        
        @Override
        public final String getName() {
            return material.getName(); // + material.category.getItemName()?
        }
        
        @Override
        public final boolean isUsable() {
            return false;
        }
        
        @Override
        public final boolean isEquippable() {
            return false;
        }
    }
    
    private final static Map<Material, MaterialItem> materialItemMap = new LinkedHashMap<Material, MaterialItem>();
    
    protected final static MaterialItem getMaterialItem(final Material material) {
        return materialItemMap.get(material);
    }
    
    // Weapon sub-types like dagger/sword/spear/bow/etc.
    protected static class GearSubtype implements Named {
        private final String name;
        private final GearType type;
        private final MaterialCategory materialCategory;
        private final int materialRequiredToCraft; // Also determines price
        private final float attackDamageMultiplier; // These multipliers applied after base calculation (which use GearType.multiplier)
        private final float receivedDamageMultiplier;
        private final float chanceOfBeingHitMultiplier;
        private final boolean twoHanded;
        private final float renderX;
        private final float renderY;
        
        protected GearSubtype(final Segment seg) {
            name = seg.getValue(0);
            type = getGearType(seg.getValue(1));
            materialCategory = getMaterialCategory(seg.getValue(2));
            materialRequiredToCraft = seg.intValue(3);
            attackDamageMultiplier = seg.getFloat(4, 1.0f);
            receivedDamageMultiplier = seg.getFloat(5, 1.0f);
            chanceOfBeingHitMultiplier = seg.getFloat(6, 1.0f);
            twoHanded = seg.getBoolean(7, false);
            renderX = seg.getFloat(8, -1.0f);
            renderY = seg.getFloat(9, -1.0f);
        }
        
        @Override
        public final String getName() {
            return name;
        }
        
        public final int getMaterialRequiredToCraft() {
            return materialRequiredToCraft;
        }
        
        public final float getAttackDamageMultiplier() {
            return attackDamageMultiplier;
        }
        
        public final float getReceivedDamageMultiplier() {
            return receivedDamageMultiplier;
        }
        
        public final float getChanceOfBeingHitMultiplier() {
            return chanceOfBeingHitMultiplier;
        }
        
        public final char getTypeCode() {
            return type.getCode();
        }
    }
    
    private static GearSubtype gearSubtypeArmorNone = null;
    private static GearSubtype gearSubtypeWeaponNone = null;
    
    private final static Map<String, GearSubtype> gearSubtypeMap = new LinkedHashMap<String, GearSubtype>();
    
    protected final static GearSubtype getGearSubtype(final String name) {
        return get(gearSubtypeMap, name);
    }
    
    protected static interface Named {
        public String getName();
    }
    
    protected final static class Names implements Named {
        private final String name;
        private final String plural;
        
        protected Names(final Field field) {
            name = field.getValue(0);
            plural = Chartil.nvl(field.getValue(1), name + "s");
        }
        
        @Override
        public final String getName() {
            return name;
        }
        
        public final String getPlural() {
            return plural;
        }
    }
    
    protected final static void loadData() {
        SegmentStream in = null;
        try {
            in = SegmentStream.openLocation(RpgGame.RES + "Data.txt");
            Segment seg = null;
            while ((seg = in.readIf("ELE")) != null) {
                put(elementMap, new Element(seg));
            }
            while ((seg = in.readIf("RAC")) != null) {
                put(raceMap, new Race(seg));
            }
            while ((seg = in.readIf("RA2")) != null) {
                put(subraceMap, new Subrace(seg));
            }
            while ((seg = in.readIf("QTY")) != null) {
                put(smithingQualityMap, new SmithingQuality(seg));
            }
            while ((seg = in.readIf("MAC")) != null) {
                put(materialCategoryMap, new MaterialCategory(seg));
            }
            while ((seg = in.readIf("MAT")) != null) {
                put(materialMap, new Material(seg));
            }
            while ((seg = in.readIf("TYP")) != null) {
                final GearType type = new GearType(seg);
                final char code = type.getCode();
                if (code == GEAR_TYPE_ARMOR) {
                    gearTypeArmor = type;
                } else if (code == GEAR_TYPE_WEAPON) {
                    gearTypeWeapon = type;
                } else if (code == GEAR_TYPE_SHIELD) {
                    gearTypeShield = type;
                }
                put(gearTypeMap, type);
            }
            while ((seg = in.readIf("SUB")) != null) {
                final GearSubtype subtype = new GearSubtype(seg);
                final char code = subtype.getTypeCode();
                if (subtype.materialCategory == null) {
                    if (code == GEAR_TYPE_ARMOR) {
                        gearSubtypeArmorNone = subtype;
                    } else if (code == GEAR_TYPE_WEAPON) {
                        gearSubtypeWeaponNone = subtype;
                    } else {
                        throw new IllegalStateException("No Material found for " + seg);
                    }
                } else {
                    put(gearSubtypeMap, subtype);
                    if (code == GEAR_TYPE_WEAPON) {
                        put(skillCategoryMap, new SkillCategory(subtype.getName()));
                    }
                }
            }
            generateGear();
            Enemy.loadEnemyData(in);
            World.loadWorldData(in);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        } finally {
            Iotil.close(in);
        }
    }
    
    protected final static void generateGear() {
        for (final GearSubtype subtype : gearSubtypeMap.values()) {
            for (final Material material : subtype.materialCategory.materials) {
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
        armorNone = new Armor("None", null, null, gearSubtypeArmorNone, null);
        weaponNone = new Weapon("Fists", null, null, gearSubtypeWeaponNone);
        for (final Material material : materialMap.values()) {
            materialItemMap.put(material, new MaterialItem(material));
        }
    }
    
    protected final static String getGearName(final SmithingQuality quality, final Material material, final GearSubtype subtype) {
        return quality.getName() + " " + material.getName() + " " + subtype.getName();
    }
    
    protected final static Gear newGear(final SmithingQuality quality, final Material material, final GearSubtype subtype, final ChrComponent renderData) {
        final String name = getGearName(quality, material, subtype);
        final char type = subtype.getTypeCode();
        if (type == GEAR_TYPE_ARMOR) {
            return new Armor(name, quality, material, subtype, renderData);
        } else if (type == GEAR_TYPE_WEAPON) {
            return new Weapon(name, quality, material, subtype);
        } else if (type == GEAR_TYPE_SHIELD) {
            return new Shield(name, quality, material, subtype);
        }
        throw new IllegalStateException("Unexpected Gear type " + subtype.type + " for " + name);
    }
    
    protected final static <V extends Named> V get(final Map<String, V> map, final String key) {
        if (key == null) {
            return null;
        }
        final V value = map.get(key);
        if (value == null) {
            throw new IllegalStateException(key + " not found in " + map.values().iterator().next().getClass().getSimpleName() + " map");
        }
        return value;
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
