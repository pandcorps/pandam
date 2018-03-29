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
import org.pandcorps.pandam.*;

public class Profile {
    /*package*/ final Set<Upgrade> upgrades = new HashSet<Upgrade>();
    /*package*/ ShootMode shootMode = Player.SHOOT_NORMAL;
    /*package*/ JumpMode jumpMode = Player.JUMP_GRAPPLING_HOOK;
    /*package*/ boolean autoClimb = true;
    
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
    
    protected final static Upgrade UPGRADE_BALL = new Upgrade("Ball");
    
    protected final static Upgrade UPGRADE_RAPID = new RapidUpgrade();
    
    protected final static Upgrade UPGRADE_SPREAD = new SpreadUpgrade();
    
    protected final static Upgrade UPGRADE_CHARGE = new ChargeUpgrade();
    
    protected final static Upgrade UPGRADE_BOMB = new Upgrade("Bomb");
    
    protected final static Upgrade UPGRADE_GRAPPLING_BEAM = new Upgrade("GrapplingBeam");
    
    protected final static Upgrade BASIC_ATTACK = new Upgrade("BasicAttack");
    
    protected final static Upgrade[] UPGRADES = { UPGRADE_BALL, UPGRADE_RAPID, UPGRADE_SPREAD, UPGRADE_CHARGE, UPGRADE_BOMB, UPGRADE_GRAPPLING_BEAM,
            BASIC_ATTACK };
    
    protected static class Upgrade {
        protected final String name;
        
        protected Upgrade(final String name) {
            this.name = name;
        }
        
        protected final void award(final Player player) {
            player.prf.upgrades.add(this);
            enable(player);
        }
        
        protected void enable(final Player player) {
        }
        
        protected final Panmage getBoxImage(final PlayerContext pc) {
            return pc.pi.boltBoxes.get(name);
        }
    }
    
    protected abstract static class ShootUpgrade extends Upgrade {
        protected ShootUpgrade(final String name) {
            super(name);
        }
        
        @Override
        protected final void enable(final Player player) {
            player.prf.shootMode = getShootMode();
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
