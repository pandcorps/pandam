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
    
    protected static Upgrade UPGRADE_BALL = new Upgrade("Ball");
    
    protected static Upgrade UPGRADE_RAPID = new Upgrade("Rapid");
    
    protected static Upgrade UPGRADE_SPREAD = new Upgrade("Spread");
    
    protected static Upgrade UPGRADE_CHARGE = new Upgrade("Charge");
    
    protected static Upgrade UPGRADE_BOMB = new Upgrade("Bomb");
    
    protected static Upgrade UPGRADE_GRAPPLING_BEAM = new Upgrade("GrapplingBeam");
    
    protected final static Upgrade[] UPGRADES = { UPGRADE_BALL, UPGRADE_RAPID, UPGRADE_SPREAD, UPGRADE_CHARGE, UPGRADE_BOMB, UPGRADE_GRAPPLING_BEAM };
    
    protected static class Upgrade {
        protected final String name;
        protected Panmage boxImage = null;
        
        protected Upgrade(final String name) {
            this.name = name;
        }
        
        protected final Panmage getBoxImage() {
            if (boxImage != null) {
                return boxImage;
            }
            boxImage = Pangine.getEngine().createImage("bolt." + name, BotsnBoltsGame.CENTER_16, BotsnBoltsGame.minCube, BotsnBoltsGame.maxCube, BotsnBoltsGame.RES + "misc/Bolt" + name + ".png");
            return boxImage;
        }
    }
}
