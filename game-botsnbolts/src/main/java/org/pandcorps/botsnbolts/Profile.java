/*
Copyright (c) 2009-2023, Andrew M. Martin
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

import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.core.*;
import org.pandcorps.core.io.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.pandam.*;

public class Profile {
    private final static String LOC_BOLT = "Bolt.txt";
    private final static String LOC_DISK = "Disk.txt";
    private final static String LOC_PROFILE = "Prof.txt";
    private final static String SEG_BOLT = "BLT";
    private final static String SEG_DISK = "DSK";
    private final static String SEG_PROFILE = "PRF";
    
    /*package*/ final Set<Upgrade> upgrades;
    /*package*/ final Set<String> disks;
    /*package*/ ShootMode shootMode = Player.SHOOT_NORMAL;
    /*package*/ JumpMode jumpMode = Player.JUMP_NORMAL;
    /*package*/ boolean autoClimb = true;
    /*package*/ boolean autoCharge = true;
    /*package*/ boolean levelSuggestions = true;
    /*package*/ boolean frequentCheckpoints = true;
    /*package*/ boolean boltUsageHints = true;
    /*package*/ boolean endureSpikes = true;
    /*package*/ boolean adaptiveBatteries = true;
    /*package*/ boolean infiniteStamina = true;
    /*package*/ boolean infiniteLives = true;
    /*package*/ boolean infiniteHealth = false;
    /*package*/ boolean stunProtection = false;
    /*package*/ boolean fallProtection = false;
    /*package*/ boolean hazardProtection = false;
    /*package*/ boolean airJump = false;
    /*package*/ ShootMode lastUsedFallbackShootMode = Player.SHOOT_NORMAL;
    /*package*/ final static int NUM_DIFFICULTY_SETTINGS = 12;
    private final Profile old;
    
    /*package*/ Profile() {
        this(false);
    }
    
    /*package*/ Profile(final boolean ai) {
        upgrades = new HashSet<Upgrade>();
        disks = new HashSet<String>();
        if (ai) {
            old = null;
            return;
        }
        loadBolts();
        loadDisks();
        loadProfile();
        old = new Profile(this);
    }
    
    protected Profile(final Profile src) {
        upgrades = src.upgrades;
        disks = src.disks;
        load(src);
        old = null;
    }
    
    private final void load(final Profile src) {
        shootMode = src.shootMode;
        jumpMode = src.jumpMode;
        autoClimb = src.autoClimb;
        autoCharge = src.autoCharge;
        levelSuggestions = src.levelSuggestions;
        frequentCheckpoints = src.frequentCheckpoints;
        boltUsageHints = src.boltUsageHints;
        endureSpikes = src.endureSpikes;
        adaptiveBatteries = src.adaptiveBatteries;
        infiniteStamina = src.infiniteStamina;
        infiniteLives = src.infiniteLives;
        infiniteHealth = src.infiniteHealth;
        stunProtection = src.stunProtection;
        fallProtection = src.fallProtection;
        hazardProtection = src.hazardProtection;
        airJump = src.airJump;
        lastUsedFallbackShootMode = src.lastUsedFallbackShootMode;
    }
    
    private final boolean isSame() {
        return (shootMode == old.shootMode) && (jumpMode == old.jumpMode) && (autoClimb == old.autoClimb) && (autoCharge == old.autoCharge)
                && (levelSuggestions == old.levelSuggestions) && (frequentCheckpoints == old.frequentCheckpoints)
                && (boltUsageHints == old.boltUsageHints) && (endureSpikes == old.endureSpikes) && (adaptiveBatteries == old.adaptiveBatteries)
                && (infiniteStamina == old.infiniteStamina) && (infiniteLives == old.infiniteLives)
                && (infiniteHealth == old.infiniteHealth) && (stunProtection == old.stunProtection)
                && (fallProtection == old.fallProtection) && (hazardProtection == old.hazardProtection)
                && (airJump == old.airJump) && (lastUsedFallbackShootMode == old.lastUsedFallbackShootMode);
    }
    
    private final void loadBolts() {
        loadValues(LOC_BOLT, SEG_BOLT, upgrades, new ValueLoader<Upgrade>() {
            @Override public final Upgrade newValue(final String s) {
                return getUpgrade(s); }});
    }
    
    private final void loadDisks() {
        loadValues(LOC_DISK, SEG_DISK, disks, new ValueLoader<String>() {
            @Override public final String newValue(final String s) {
                return s; }});
    }
    
    private static interface ValueLoader<T> {
        T newValue(final String s);
    }
    
    private final static <T> void loadValues(final String loc, final String segName, final Collection<T> values, final ValueLoader<T> loader) {
        try {
            final Segment seg = readSegment(loc, segName);
            if (seg == null) {
                return;
            }
            for (final Field f : seg.getRepetitions(0)) {
                final T elem = loader.newValue(f.getValue());
                if (elem != null) {
                    values.add(elem);
                }
            }
        } catch (final Exception e) {
            // File doesn't yet exist... or it's corrupted and unusable
        }
    }
    
    private final static Segment readSegment(final String loc, final String segName) {
        SegmentStream in = null;
        try {
            in = SegmentStream.openLocation(loc);
            return in.readIf(segName);
        } catch (final Exception e) {
            // File doesn't yet exist... or it's corrupted and unusable
            return null;
        } finally {
            Iotil.close(in);
        }
    }
    
    private final void loadProfile() {
        final Segment seg = readSegment(LOC_PROFILE, SEG_PROFILE);
        final boolean touchEnabled = Menu.isTouchEnabled();
        if (seg == null) {
            autoClimb = touchEnabled;
            autoCharge = touchEnabled;
            return;
        }
        shootMode = getShootMode(seg.getValue(0));
        jumpMode = getJumpMode(seg.getValue(1));
        autoClimb = seg.getBoolean(2, touchEnabled);
        autoCharge = seg.getBoolean(3, touchEnabled);
        levelSuggestions = seg.getBoolean(4, levelSuggestions);
        frequentCheckpoints = seg.getBoolean(5, frequentCheckpoints);
        boltUsageHints = seg.getBoolean(6, boltUsageHints);
        endureSpikes = seg.getBoolean(7, endureSpikes);
        adaptiveBatteries = seg.getBoolean(8, adaptiveBatteries);
        infiniteStamina = seg.getBoolean(9, infiniteStamina);
        infiniteLives = seg.getBoolean(10, infiniteLives);
        infiniteHealth = seg.getBoolean(11, infiniteHealth);
        stunProtection = seg.getBoolean(12, stunProtection);
        fallProtection = seg.getBoolean(13, fallProtection);
        hazardProtection = seg.getBoolean(14, hazardProtection);
        airJump = seg.getBoolean(15, airJump);
        lastUsedFallbackShootMode = getShootMode(seg.getValue(16));
    }
    
    /*package*/ final void saveBolts() {
        saveValues(LOC_BOLT, SEG_BOLT, upgrades);
    }
    
    /*package*/ final void saveDisks() {
        saveValues(LOC_DISK, SEG_DISK, disks);
    }
    
    private final static void saveValues(final String loc, final String segName, final Iterable<?> values) {
        final Segment seg = new Segment(segName);
        for (final Object value : values) {
            seg.addValue(0, value.toString());
        }
        Savtil.save(seg, loc);
    }
    
    /*package*/ final void saveProfile() {
        if (isSame()) {
            return;
        }
        final Segment seg = new Segment(SEG_PROFILE);
        seg.setValue(0, shootMode.getName());
        seg.setValue(1, jumpMode.getName());
        seg.setBoolean(2, autoClimb);
        seg.setBoolean(3, autoCharge);
        seg.setBoolean(4, levelSuggestions);
        seg.setBoolean(5, frequentCheckpoints);
        seg.setBoolean(6, boltUsageHints);
        seg.setBoolean(7, endureSpikes);
        seg.setBoolean(8, adaptiveBatteries);
        seg.setBoolean(9, infiniteStamina);
        seg.setBoolean(10, infiniteLives);
        seg.setBoolean(11, infiniteHealth);
        seg.setBoolean(12, stunProtection);
        seg.setBoolean(13, fallProtection);
        seg.setBoolean(14, hazardProtection);
        seg.setBoolean(15, airJump);
        seg.setValue(16, lastUsedFallbackShootMode.getName());
        Savtil.save(seg, LOC_PROFILE);
        old.load(this);
    }
    
    /*package*/ final int getDifficulty() {
        return toDifficulty(levelSuggestions) + toDifficulty(frequentCheckpoints) + toDifficulty(boltUsageHints) + toDifficulty(endureSpikes) +
                toDifficulty(adaptiveBatteries) + toDifficulty(infiniteStamina) + toDifficulty(infiniteLives) +
                toDifficulty(infiniteHealth) + toDifficulty(stunProtection) + toDifficulty(fallProtection) +
                toDifficulty(hazardProtection) + toDifficulty(airJump);
    }
    
    private final static int toDifficulty(final boolean setting) {
        return setting ? 0 : 1;
    }
    
    /*package*/ final ShootMode getShootMode(final String name) {
        final Upgrade upgrade = getUpgrade(name);
        return (upgrade instanceof ShootUpgrade) ? ((ShootUpgrade) upgrade).getShootMode() : Player.SHOOT_NORMAL;
    }
    
    /*package*/ final JumpMode getJumpMode(final String name) {
        final Upgrade upgrade = getUpgrade(name);
        return (upgrade instanceof JumpUpgrade) ? ((JumpUpgrade) upgrade).getJumpMode() : Player.JUMP_NORMAL;
    }
    
    /*package*/ final boolean isUpgradeAvailable(final String name) {
        return isUpgradeAvailable(getUpgrade(name));
    }
    
    /*package*/ final boolean isUpgradeAvailable(final Upgrade upgrade) {
        return upgrades.contains(upgrade);
    }
    
    /*package*/ final boolean isAttackUpgradeAvailable() {
        return isUpgradeAvailable(UPGRADE_SPREAD) || isUpgradeAvailable(UPGRADE_CHARGE) || isUpgradeAvailable(UPGRADE_RAPID)
                || isUpgradeAvailable(UPGRADE_STREAM) || isUpgradeAvailable(UPGRADE_SHIELD) || isUpgradeAvailable(UPGRADE_SWORD);
    }
    
    /*package*/ final boolean isJumpUpgradeAvailable() {
        return isUpgradeAvailable(UPGRADE_BALL) || isUpgradeAvailable(UPGRADE_GRAPPLING_BEAM) || isUpgradeAvailable(UPGRADE_SPRING)
                || isUpgradeAvailable(UPGRADE_BOARD) || isUpgradeAvailable(UPGRADE_GLIDER);
    }
    
    /*package*/ final boolean isAvailable(final String name) {
        return (disks.contains(name) || isUpgradeAvailable(name));
    }
    
    /*package*/ final static boolean isRequirementMet(final String name) {
        final Profile prf = PlayerContext.getProfile(BotsnBoltsGame.getPrimaryPlayerContext());
        return (prf != null) && prf.isAvailable(name);
    }
    
    /*package*/ final static Upgrade getUpgrade(final String name) {
        for (final Upgrade upgrade : UPGRADES) {
            if (upgrade.name.equals(name)) {
                return upgrade;
            }
        }
        return null;
    }
    
    protected final static Upgrade UPGRADE_BALL = new BallUpgrade();
    
    protected final static Upgrade UPGRADE_RAPID = new RapidUpgrade();
    
    protected final static Upgrade UPGRADE_SPREAD = new SpreadUpgrade();
    
    protected final static Upgrade UPGRADE_CHARGE = new ChargeUpgrade();
    
    protected final static Upgrade UPGRADE_STREAM = new StreamUpgrade();
    
    protected final static Upgrade UPGRADE_SHIELD = new ShieldUpgrade();
    
    protected final static Upgrade UPGRADE_SWORD = new SwordUpgrade();
    
    protected final static Upgrade UPGRADE_BOMB = new Upgrade("Bomb") {
        @Override public final String getDisplayName(final PlayerContext pc) {
            return "Shift Bomb";
        }};
    
    protected final static Upgrade UPGRADE_GRAPPLING_BEAM = new GrapplingBeamUpgrade();
    
    protected final static Upgrade UPGRADE_SPRING = new SpringUpgrade();
    
    protected final static Upgrade UPGRADE_BOARD = new BoardUpgrade();
    
    protected final static Upgrade UPGRADE_GLIDER = new GliderUpgrade();
    
    protected final static Upgrade UPGRADE_RESCUE = new Upgrade("Rescue") {
        @Override public final String getDisplayName(final PlayerContext pc) {
            return pc.pi.birdName + " Rescue";
        }};
        
    protected final static Upgrade UPGRADE_WALL_GRAB = new Upgrade("WallGrab") {
        @Override public final String getDisplayName(final PlayerContext pc) {
            return "Wall Grab";
        }};
    
    protected final static Upgrade UPGRADE_SLIDE = new Upgrade("Slide") {
        @Override public final String getDisplayName(final PlayerContext pc) {
            return "Slide";
        }};
        
    protected final static Upgrade UPGRADE_DASH = new Upgrade("Dash") {
        @Override public final String getDisplayName(final PlayerContext pc) {
            return "Dash";
        }};
    
    protected final static Upgrade BASIC_JUMP = new Upgrade("BasicJump") {
        @Override public final String getDisplayName(final PlayerContext pc) {
            return "Jump";
        }};
    
    protected final static Upgrade BASIC_ATTACK = new Upgrade("BasicAttack") {
        @Override public final String getDisplayName(final PlayerContext pc) {
            return pc.pi.name + " Cannon";
        }};
    
    protected final static Upgrade[] UPGRADES = { UPGRADE_BALL, UPGRADE_RAPID, UPGRADE_SPREAD, UPGRADE_CHARGE, UPGRADE_BOMB, UPGRADE_GRAPPLING_BEAM, UPGRADE_SPRING,
            UPGRADE_RESCUE, UPGRADE_SLIDE, UPGRADE_WALL_GRAB, UPGRADE_DASH, UPGRADE_STREAM, UPGRADE_SHIELD, UPGRADE_SWORD, UPGRADE_BOARD, UPGRADE_GLIDER, BASIC_ATTACK, BASIC_JUMP };
    
    protected abstract static class Upgrade {
        protected final String name;
        
        protected Upgrade(final String name) {
            this.name = name;
        }
        
        protected final void award(final Player player) {
            final Profile prf = player.prf;
            if (prf.upgrades.add(this)) {
                BotsnBoltsGame.notify("You got " + getDisplayName(player));
                final String[] tutorial = getTutorial();
                if (tutorial != null) {
                    BotsnBoltsGame.notify(tutorial);
                }
                prf.saveBolts();
            }
            enable(player);
        }
        
        protected void enable(final Player player) {
        }
        
        protected final Panmage getBoxImage(final PlayerContext pc) {
            return pc.pi.boltBoxes.get(getIconName(pc));
        }
        
        protected String getIconName(final PlayerContext pc) {
            return name;
        }
        
        @Override
        public final String toString() {
            return name;
        }
        
        public final String getDisplayName(final Player player) {
            return getDisplayName(player.pc);
        }
        
        public abstract String getDisplayName(final PlayerContext pc);
        
        public String[] getTutorial() {
            return null;
        }
    }
    
    protected abstract static class JumpUpgrade extends Upgrade {
        protected JumpUpgrade(final String name) {
            super(name);
        }
        
        @Override
        protected final void enable(final Player player) {
            player.prf.jumpMode = getJumpMode();
        }
        
        protected abstract JumpMode getJumpMode();
    }
    
    protected final static class BallUpgrade extends JumpUpgrade {
        protected BallUpgrade() {
            super("Ball");
        }
        
        @Override
        protected final JumpMode getJumpMode() {
            return Player.JUMP_BALL;
        }
        
        @Override
        public final String getDisplayName(final PlayerContext pc) {
            return "Shift Ball";
        }
    }
    
    protected final static class GrapplingBeamUpgrade extends JumpUpgrade {
        protected GrapplingBeamUpgrade() {
            super("GrapplingBeam");
        }
        
        @Override
        protected final JumpMode getJumpMode() {
            return Player.JUMP_GRAPPLING_HOOK;
        }
        
        @Override
        public final String getDisplayName(final PlayerContext pc) {
            return "Grappling Beam";
        }
        
        @Override
        public final String[] getTutorial() {
            return new String[] {
                    "Double-jump while running to swing",
                    "Double-jump while still to climb upward",
                    "Hold attack button to climb"
                    };
        }
    }
    
    protected final static class SpringUpgrade extends JumpUpgrade {
        protected SpringUpgrade() {
            super("Spring");
        }
        
        @Override
        protected final JumpMode getJumpMode() {
            return Player.JUMP_SPRING;
        }
        
        @Override
        public final String getDisplayName(final PlayerContext pc) {
            return pc.pi.animalName + " Spring";
        }
        
        @Override
        public final String[] getTutorial() {
            return new String[] { "Double-jump to use" };
        }
    }
    
    protected final static class BoardUpgrade extends JumpUpgrade {
        protected BoardUpgrade() {
            super("Board");
        }
        
        @Override
        protected final JumpMode getJumpMode() {
            return Player.JUMP_BOARD;
        }
        
        @Override
        public final String getDisplayName(final PlayerContext pc) {
            return pc.pi.animalName + " Board";
        }
        
        @Override
        public final String[] getTutorial() {
            return new String[] { "Double-jump to use", "Grind rails" };
        }
    }
    
    protected final static class GliderUpgrade extends JumpUpgrade {
        protected GliderUpgrade() {
            super("Glider");
        }
        
        @Override
        protected final JumpMode getJumpMode() {
            return Player.JUMP_GLIDER;
        }
        
        @Override
        public final String getDisplayName(final PlayerContext pc) {
            return pc.pi.animalName + " Glider";
        }
        
        @Override
        public final String[] getTutorial() {
            return new String[] { "Double-jump to use", "Back to pull up", "Forward to dive" };
        }
    }
    
    protected abstract static class ShootUpgrade extends Upgrade {
        protected final String displayName;
        
        protected ShootUpgrade(final String name) {
            this(name, name + " Cannon");
        }
        
        protected ShootUpgrade(final String name, final String displayName) {
            super(name);
            this.displayName = displayName;
        }
        
        @Override
        protected final void enable(final Player player) {
            final ShootMode shootMode = getShootMode();
            player.prf.shootMode = shootMode;
            shootMode.onSelect(player);
        }
        
        protected abstract ShootMode getShootMode();
        
        @Override
        public final String getDisplayName(final PlayerContext pc) {
            return this.displayName;
        }
    }
    
    protected final static class RapidUpgrade extends ShootUpgrade {
        protected RapidUpgrade() {
            super("Rapid");
        }
        
        @Override
        protected final ShootMode getShootMode() {
            return Player.SHOOT_RAPID;
        }
    }
    
    protected final static class SpreadUpgrade extends ShootUpgrade {
        protected SpreadUpgrade() {
            super("Spread");
        }
        
        @Override
        protected final ShootMode getShootMode() {
            return Player.SHOOT_SPREAD;
        }
    }
    
    protected final static class ChargeUpgrade extends ShootUpgrade {
        protected ChargeUpgrade() {
            super("Charge");
        }
        
        @Override
        protected final ShootMode getShootMode() {
            return Player.SHOOT_CHARGE;
        }
        
        @Override
        public final String[] getTutorial() {
            final Profile prf = PlayerContext.getProfile(BotsnBoltsGame.getPrimaryPlayerContext());
            return ((prf != null) && !prf.autoCharge) ? new String[] { "Hold fire button to charge" } : null;
        }
    }
    
    protected final static class StreamUpgrade extends ShootUpgrade {
        protected StreamUpgrade() {
            super("Stream");
        }
        
        @Override
        protected final ShootMode getShootMode() {
            return Player.SHOOT_STREAM;
        }
    }
    
    protected final static class ShieldUpgrade extends ShootUpgrade {
        protected ShieldUpgrade() {
            super("Shield", "Disc Shield");
        }
        
        @Override
        protected final ShootMode getShootMode() {
            return Player.SHOOT_SHIELD;
        }
    }
    
    protected final static class SwordUpgrade extends ShootUpgrade {
        protected SwordUpgrade() {
            super("Sword", "Plasma Blade");
        }
        
        @Override
        protected final ShootMode getShootMode() {
            return Player.SHOOT_SWORD;
        }
        
        @Override
        protected final String getIconName(final PlayerContext pc) {
            final MeleeMode meleeMode = pc.pi.meleeMode;
            return (meleeMode == null) ? super.getIconName(pc) : meleeMode.getName();
        }
    }
}
