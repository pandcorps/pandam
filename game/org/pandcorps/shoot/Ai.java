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
package org.pandcorps.shoot;

import org.pandcorps.core.Mathtil;
import org.pandcorps.game.actor.Burst;
import org.pandcorps.pandam.*;

public class Ai extends ShooterController {
	protected final static float DISTANCE_SCROLLED = ShootGame.SCREEN_W * 2.5f;
	
	private final static byte ACTION_STILL = 0;
	private final static byte ACTION_ADVANCE = 1;
	private final static byte ACTION_RETREAT = 2;
	private final static byte ACTION_SCROLLED = 3;
	
    private byte timer;
    private byte action;
    
	/*package*/ static int bamDelay;
	private int bamTimer = 0;
	
	private byte attackTimer;
	private boolean attacking;
	
	{
		clear();
		clearAttack();
	}
	
	private final void clear() {
		timer = 0;
	    action = ACTION_STILL;
	}
	
	private final void clearAttack() {
	    attackTimer = 0;
	    attacking = false;
	}
	
	private final void advance() {
	    setAction(ACTION_ADVANCE, (byte) 15, (byte) 40);
	}
	
	private final void retreat() {
		retreat((byte) 10, (byte) 30);
	}
	
	private final void retreat(final byte min, final byte max) {
		setAction(ACTION_RETREAT, min, max);
	}
	
	private final void setAction(final byte act, final byte min, final byte max) {
		action = act;
		timer = Mathtil.randb(min, max);
	}
	
	protected static float getDistance(final Panctor source, final Panctor target) {
		return target == null ? 0 : (source.getPosition().getX() - target.getPosition().getX());
	}
	
	@Override
    public final void step() {
		final Shooter target = getTarget();
		final float dist = Math.abs(getDistance(shooter, target));
		if (dist > DISTANCE_SCROLLED) {
		    action = ACTION_SCROLLED;
		    shooter.destroy(); // Very distant off-screen enemies should be destroyed
		    return;
		}
		if (bamTimer > 0) {
            bamTimer--;
        }
		if (timer > 0) {
			timer--;
		} else {
		    if (dist > ShootGame.SCREEN_W) {
		        advance(); // Off-screen enemies should try to get on-screen
		    } else {
    			final int r = Mathtil.randi(0, 99);
    			if (r < 50) {
    				setAction(ACTION_STILL, (byte) 15, (byte) 50);
    			} else if (r < 80) {
    				advance();
    			} else {
    				retreat();
    			}
		    }
		}
		if (action == ACTION_ADVANCE) {
			stepAdvance(target);
		} else if (action == ACTION_RETREAT) {
			if (!stepRetreat(target)) {
				clear();
			}
		}
		if (shooter.weapon != null) {
		    if (dist > (ShootGame.SCREEN_W * 1.5f)) {
		        clearAttack(); // Distant off-screen enemies should not shoot
		    } else {
    			if (attackTimer > 0) {
    				attackTimer--;
    			} else {
    				if (attacking) {
    					attacking = false;
    				} else {
    					attacking = Mathtil.rand(25);
    					if (attacking) {
    						attack();
    					}
    				}
    				attackTimer = Mathtil.randb((byte) 15, (byte) 30);
    			}
    			if (attacking) {
    				attacking();
    			}
		    }
		}
	}
	
	@Override
	/*package*/ final void onCollision(final Shooter other) {
		if (other != getTarget()) {
			return;
		}
		if (shooter.weapon == null && bamTimer == 0) {
			final Burst bam = new Burst(ShootGame.bam);
			final Panple pos = bam.getPosition();
			pos.set(other.getPosition());
			pos.add(guy.getPosition());
			pos.multiply(0.5f);
			pos.addY(Shooter.OFF_ADD_Y);
			pos.addZ(10);
			other.getLayer().addActor(bam);
			other.onHurt(shooter.def.melee);
			bamTimer = bamDelay;
			clear();
		}
		retreat((byte) 3, (byte) 12);
	}
	
	@Override
	/*package*/ final void onHurt(final Projectile p) {
		if (ShootGame.chainsaw.equals(p.weapon.def)) {
			retreat();
		}
	}
	
	@Override
	/*package*/ void onDestroy() {
	    if (action == ACTION_SCROLLED) {
	        return;
	    }
		PowerUp.newPowerUp(shooter);
	}
	
	private final Shooter getTarget() {
		return ShootGame.shooter;
	}
}
