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

import org.pandcorps.core.Mathtil;
import org.pandcorps.fight.Background.BackgroundDefinition;
import org.pandcorps.pandam.*;

public final class Ai extends Controller {
	/*package*/ final static byte MODE_LOCK = 0;
	/*package*/ final static byte MODE_STARE = 1;
	/*package*/ final static byte MODE_FIGHT = 2;
	
	private final static byte ACTION_STILL = 0;
	private final static byte ACTION_ADVANCE = 1;
	private final static byte ACTION_RETREAT = 2;
	private final static byte ACTION_ATTACK = 3;
	
    private Fighter target = null;
    /*package*/ byte mode = MODE_FIGHT;
    
    // Likelihood to...:
    private byte walk = 70;
    private byte advance = 80;
    private byte attack = 80;
    
    private byte timer = 0;
    private byte action = ACTION_STILL;
    
    public Ai() {
        //super(fighter);
    }
    
    @Override
    protected final void setFighter(final Fighter fighter) {
        super.setFighter(fighter);
        target = null;
    }
    
    @Override
    protected final void step() {
        if (target == null) {
            target = getTarget(fighter);
        }
        switch(mode) {
        	case MODE_FIGHT :
        		stepFight();
        		break;
        	case MODE_STARE :
        		stepStare();
        		break;
        	case MODE_LOCK :
        		stepLock();
        		break;
        	default :
        		throw new RuntimeException("Unexpected mode: " + mode);
        }
    }
    
    private final void stepFight() {
    	if (timer <= 0) {
    		if (canAttack()) {
    			if (choose(attack)) {
    				action = ACTION_ATTACK;
    			} else if (!choose(advance)) {
    				action = ACTION_RETREAT;
    			} else {
    				action = ACTION_STILL;
    			}
    		} else if (choose(walk)) {
    			if (choose(advance)) {
    				action = ACTION_ADVANCE;
    			} else {
    				action = ACTION_RETREAT;
    			}
    		}
    		if (action == ACTION_ATTACK) {
    			timer = 15; // Will be set to 0 after attack anyway
    			//timer = (byte) Mathtil.randi(8, 18);
    		} else {
    			// Walk or stand for .5 to 1.5 seconds
    			timer = (byte) Mathtil.randi(15, 45);
    		}
    	}
    	timer--;
    	final Panple pos = fighter.getPosition();
		final Panple tpos = target.getPosition();
		final float x = pos.getX(), y = pos.getY();
		final float tx = tpos.getX(), ty = tpos.getY();
    	if (action == ACTION_ADVANCE) {
    		if (canAttack()) {
    			timer = 0;
    		} else {
    			if (tx < x - 1) {
    				walkLeft();
    			} else if (tx > x + 1) {
    				walkRight();
    			}
    			if (ty < y - 1) { // Without +/- 1, can get rapid up/down alternating
    				walkDown();
    			} else if (ty > y + 1) {
    				walkUp();
    			}
    		}
    	} else if (action == ACTION_RETREAT) {
    		//Change behavior if hit boundary, but hitting x boundary wouldn't need to mean to stop y movement.
    		final BackgroundDefinition bg = FightGame.getBackground().def;
    		if (x <= bg.minX || x >= bg.maxX || y <= bg.minY || y >= bg.maxY) {
    			action = ACTION_STILL;
    		} else {
	    		if (tx < x) {
					walkRight();
				} else if (tx > x) {
					walkLeft();
				} //TODO else walk toward center of bg
				if (ty < y) {
					walkUp();
				} else if (ty > y) {
					walkDown();
				} //TODO else walk toward center of bg
    		}
    	} else if (action == ACTION_ATTACK) {
    		//TODO special attacks
    		attack();
    		action = ACTION_STILL; // Without this, attacks happen very quickly
    		timer = 0; // Force new decision right away
    	}
    }
    
    private final static boolean choose(final byte likelihood) {
    	return rand() < likelihood;
    }
    
    private final static int rand() {
    	return Mathtil.randi(0, 100);
    }
    
    private final boolean canAttack() {
    	final Panple pos = fighter.getPosition(), tpos = target.getPosition();
    	final float x = pos.getX(), y = pos.getY();
    	final float tx = tpos.getX(), ty = tpos.getY();
    	if (Math.abs(x - tx) > FightGame.DIM) {
    		return false;
    	}
    	return (Math.abs(y - ty) < FightGame.DIM);
    }
    
    private final void stepStare() {
        final boolean m = fighter.isMirror();
        final float x = fighter.getPosition().getX();
        final float tx = target.getPosition().getX();
        if (tx < x && !m) {
            walkLeft();
        } else if (tx > x && m) {
            walkRight();
        }
    }
    
    private final void stepLock() {
    }
    
    /*package*/ final static Fighter getTarget(final Fighter fighter) {
        for (final Panctor actor : fighter.getLayer().getRoom().getActors()) {
            if (actor.getClass() == Fighter.class && actor != fighter) {
                //TODO Look for closest opponent
                return (Fighter) actor;
            }
        }
        return null;
    }
}
