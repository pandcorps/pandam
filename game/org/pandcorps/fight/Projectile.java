/*
Copyright (c) 2009-2011, Andrew M. Martin
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
package org.pandcorps.fight;

import org.pandcorps.core.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.boundary.*;

/*
 * Normal attack - invisible, linked, no velocity, time = 1
 * Stretched limb attack - visible, linked, no velocity, time? visible for multiple frames, painful for only first?
 * Fireball - visible, unlinked, velocity, time = -1
 */
public class Projectile extends org.pandcorps.game.actor.Projectile implements StepListener, AllOobListener, Collidable /*Or CollisionListener if we want two Projectiles to collide with each other*/ {
    /*package*/ final static byte TYPE_QUICK = 0; // Do we need TYPE_STRONG?  If Strong is treated as quick, does that allow QQSQS?
    /*package*/ final static byte TYPE_SPEC = 1; // So far this is just for knowing when to count quicks and when to clear count, so STRONG/SPEC would be the same
    
    /*package*/ final static byte IMPACT_SPARK = 0;
    /*package*/ final static byte IMPACT_EXPLOSION = 1;
    /*package*/ final static byte IMPACT_BLOOD = 2;
    
    /*package*/ final static byte REACT_HURT = 0;
    /*package*/ final static byte REACT_BURN = 1;
    ///*package*/ final static byte REACT_ELECTRIC = 2;
    ///*package*/ final static byte REACT_FREEZE = 3;
    
    /*package*/ final Fighter fighter;
    ///*package*/ final byte type; // Maybe should just store Emitter instead of type, impact, and react
    ///*package*/ final byte impact;
    ///*package*/ final byte react;
    /*package*/ final Emitter emitter;
    private byte lastCanHit = 1;
    
    //public Projectile(final String id, final Fighter fighter, final byte type, final byte impact, final byte react, final Panple vel, final byte time, final Panimation anim) {
    public Projectile(final Fighter fighter, final Emitter emitter, final boolean mirror) {
        super(fighter, emitter, mirror);
        this.fighter = fighter;
        this.emitter = emitter;
    }
    
    /*package*/ final static byte parseType(final String val) {
        return "quick".equalsIgnoreCase(val) ? TYPE_QUICK : TYPE_SPEC;
    }
    
    /*package*/ final static byte parseImpact(final String val) {
        if (Chartil.isEmpty(val)) {
            return IMPACT_SPARK;
        } else if (Chartil.startsWithIgnoreCase(val, "explo")) {
            return IMPACT_EXPLOSION;
        } else if ("blood".equalsIgnoreCase(val)) {
            return IMPACT_BLOOD;
        }
        return IMPACT_SPARK;
    }
    
    /*package*/ final static byte parseReact(final String val) {
        if ("burn".equalsIgnoreCase(val)) {
            return REACT_BURN;
        /*} else if (Chartil.startsWithIgnoreCase(val, "elec")) {
            return REACT_ELEC;
        } else if (Chartil.startsWithIgnoreCase(val, "fr")) {
            return REACT_FREEZE;*/
        }
        return REACT_HURT;
    }
    
    @Override
    public final void die() {
        fighter.linkedProjectiles.remove(this);
        dieWithoutUnlink();
    }
    
    /*package*/ final void dieWithoutUnlink() {
        // Should we destroy here or set a flag to destroy at end of step?
        // If a fireball hits two people, we don't want destruction to prevent the second hit.
        destroy();
    }
    
    /*package*/ final void hit(final Fighter fighter) {
        if (age < lastCanHit) {
            lastCanHit = age;
        }
        if (!isLinked()) {
            // Skipping unlink because we know this is not linked
            dieWithoutUnlink();
        } // Else hurtTime should probably be 1
        //die(); // If linked (like a long limb image), keep showing it
    }
    
    /*package*/ final boolean canHit() {
        //return age == 0 || isLinked();
        //return age <= lastCanHit || isLinked();
        
        /*
        Why did above conditions work when isLinked?
        An unlinked attack would be something like a fireball.
        That would need more chances to hit when firing from long range.
        */
        return age <= lastCanHit || !isLinked();
    }
    
    private final boolean isLinked() {
        return fighter.linkedProjectiles.contains(this);
    }
    
    //TODO destroy after time and/or contact
    //TODO owner, avoid friendly fire
}
