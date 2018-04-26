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
    private final Profile old;
    
    /*package*/ Profile() {
        upgrades = new HashSet<Upgrade>();
        disks = new HashSet<String>();
        loadBolts();
        loadDisks();
        loadProfile();
        old = new Profile(this);
    }
    
    private Profile(final Profile src) {
        upgrades = null;
        disks = null;
        load(src);
        old = null;
    }
    
    private final void load(final Profile src) {
        shootMode = src.shootMode;
        jumpMode = src.jumpMode;
        autoClimb = src.autoClimb;
        autoCharge = src.autoCharge;
    }
    
    private final boolean isSame() {
        return (shootMode == old.shootMode) && (jumpMode == old.jumpMode) && (autoClimb == old.autoClimb) && (autoCharge == old.autoCharge);
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
        if (seg == null) {
            return;
        }
        shootMode = getShootMode(seg.getValue(0));
        jumpMode = getJumpMode(seg.getValue(1));
        autoClimb = seg.booleanValue(2);
        autoCharge = seg.booleanValue(3);
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
        Savtil.save(seg, LOC_PROFILE);
        old.load(this);
    }
    
    /*package*/ final ShootMode getShootMode(final String name) {
        final Upgrade upgrade = getUpgrade(name);
        return (upgrade instanceof ShootUpgrade) ? ((ShootUpgrade) upgrade).getShootMode() : Player.SHOOT_NORMAL;
    }
    
    /*package*/ final JumpMode getJumpMode(final String name) {
        final Upgrade upgrade = getUpgrade(name);
        return (upgrade instanceof JumpUpgrade) ? ((JumpUpgrade) upgrade).getJumpMode() : Player.JUMP_NORMAL;
    }
    
    /*package*/ final boolean isUpgradeAvailable(final Upgrade upgrade) {
        return upgrades.contains(upgrade);
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
    
    protected final static Upgrade UPGRADE_BOMB = new Upgrade("Bomb");
    
    protected final static Upgrade UPGRADE_GRAPPLING_BEAM = new GrapplingBeamUpgrade();
    
    protected final static Upgrade BASIC_JUMP = new Upgrade("BasicJump");
    
    protected final static Upgrade BASIC_ATTACK = new Upgrade("BasicAttack");
    
    protected final static Upgrade[] UPGRADES = { UPGRADE_BALL, UPGRADE_RAPID, UPGRADE_SPREAD, UPGRADE_CHARGE, UPGRADE_BOMB, UPGRADE_GRAPPLING_BEAM,
            BASIC_ATTACK, BASIC_JUMP };
    
    protected static class Upgrade {
        protected final String name;
        
        protected Upgrade(final String name) {
            this.name = name;
        }
        
        protected final void award(final Player player) {
            final Profile prf = player.prf;
            if (prf.upgrades.add(this)) {
                prf.saveBolts();
            }
            enable(player);
        }
        
        protected void enable(final Player player) {
        }
        
        protected final Panmage getBoxImage(final PlayerContext pc) {
            return pc.pi.boltBoxes.get(name);
        }
        
        @Override
        public final String toString() {
            return name;
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
    }
    
    protected final static class GrapplingBeamUpgrade extends JumpUpgrade {
        protected GrapplingBeamUpgrade() {
            super("GrapplingBeam");
        }
        
        @Override
        protected final JumpMode getJumpMode() {
            return Player.JUMP_GRAPPLING_HOOK;
        }
    }
    
    protected abstract static class ShootUpgrade extends Upgrade {
        protected ShootUpgrade(final String name) {
            super(name);
        }
        
        @Override
        protected final void enable(final Player player) {
            final ShootMode shootMode = getShootMode();
            player.prf.shootMode = shootMode;
            shootMode.onSelect(player);
        }
        
        protected abstract ShootMode getShootMode();
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
    }
}
