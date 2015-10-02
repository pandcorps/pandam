/*
Copyright (c) 2009-2014, Andrew M. Martin
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
package org.pandcorps.furguardians;

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.furguardians.Enemy.*;
import org.pandcorps.furguardians.Goal.*;
import org.pandcorps.furguardians.Player.*;
import org.pandcorps.furguardians.Profile.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.*;

public class GemBumped extends Pandy {
	private final static Panple tilePos = new ImplPanple();
    protected final static int AWARD_DEF = 1;
    protected final static int AWARD_2 = AWARD_DEF * 10;
    protected final static int AWARD_3 = AWARD_2 * 10;
    protected final static int AWARD_4 = AWARD_3 * 10;
    private final static int AWARD_LEVEL = AWARD_2 * 5;
    private final static int AWARD_WORLD = AWARD_LEVEL * 10;
    protected final static byte TYPE_NORMAL = 0;
    protected final static byte TYPE_END = 1;
    protected final static byte TYPE_DECORATION = 2;
    private final static Random rand = new Random();
    private final int award;
	private final byte type;
	private int age = 0;
	protected int duration = 12;
	
	protected final static int rndAward(final Player player) {
		final int r = Mathtil.randi(rand, 0, 9999);
	    if (r < 1500) {
	        return AWARD_2;
	    } else if (r < 1650) {
	        return AWARD_3;
	    }
	    final int greenThreshold;
	    if (player != null && player.pc.profile.stats.foundGreenGems == 0) {
	        greenThreshold = 1750; // First one is more likely
	    } else {
	        greenThreshold = 1665;
	    }
	    if (r < greenThreshold) {
            return AWARD_4;
	    } else if (r < 2800 && Goal.hasCurrentGoal(player, BlueGemGoal.class)) {
	        return AWARD_2;
	    }
	    return AWARD_DEF;
	}
	
	public final static GemBumped create(final Player player, final int index, final int award) {
	    return create(player, index, award, TYPE_NORMAL, getAnm(award));
	}
	
	protected final static Panimation getAnm(final int award) {
        switch (award) {
            case AWARD_DEF : return FurGuardiansGame.gemAnm;
            case AWARD_2 : return FurGuardiansGame.gemBlueAnm;
            case AWARD_3 : return FurGuardiansGame.gemCyanAnm;
            case AWARD_4 : return FurGuardiansGame.gemGreenAnm;
        }
        throw new IllegalArgumentException("Unexpected award amount " + award);
    }
	
	public GemBumped(final Player player, final Enemy defeated) {
	    this(player, defeated, defeated.def);
	}
	
	public GemBumped(final Player player, final Panctor defeated, final EnemyDefinition def) {
		this(player, defeated.getPosition().getX() - 8, defeated.getBoundingMaximum().getY() - Enemy.DEFAULT_H, def.award, TYPE_NORMAL, getAnm(def.award));
	}
	
	public final static GemBumped newLevelEnd(final Player player, final int index) {
		final int award;
		final Panimation anm;
		if (Level.isNormalTheme()) {
			award = AWARD_LEVEL;
			anm = FurGuardiansGame.gemLevelAnm;
		} else {
		    // Reward is based on number of worlds defeated; should be based on primary player
		    PlayerContext pc = Coltil.get(FurGuardiansGame.pcs, 0);
		    if (pc == null && player != null) {
		        pc = player.pc;
		    }
		    final Profile prf = (pc == null) ? null : pc.profile;
		    final Statistics stats = (prf == null) ? null : prf.stats;
		    final int world = (stats == null) ? 0 : (stats.defeatedWorlds + 1); // 0 defeated Worlds means now in World 1
		    if (world % 10000 == 0) {
		        award = AWARD_WORLD * 5;
                anm = FurGuardiansGame.gemWhiteWorldAnm;
		    } else if (world % 1000 == 0) {
                award = AWARD_WORLD * 4;
                anm = FurGuardiansGame.gemGreenWorldAnm;
		    } else if (world % 100 == 0) {
                award = AWARD_WORLD * 3;
                anm = FurGuardiansGame.gemCyanWorldAnm;
		    } else if (world % 10 == 0) {
                award = AWARD_WORLD * 2;
                anm = FurGuardiansGame.gemBlueWorldAnm;
		    } else {
    			award = AWARD_WORLD;
    			anm = FurGuardiansGame.gemWorldAnm;
		    }
		}
	    return create(player, index, award, TYPE_END, anm);
	}
	
	public final static GemBumped newShatter(final Player player) {
		final Panple pos = player.getPosition();
	    return new GemBumped(player, pos.getX() - 8, pos.getY(), -1, TYPE_NORMAL, FurGuardiansGame.gemAnm);
	}
	
	protected final static GemBumped create(final Player player, final int index, final int award, final byte type, final Panimation anm) {
		Level.tm.savePosition(tilePos, index);
	    return new GemBumped(player, tilePos.getX(), tilePos.getY(), award, type, anm);
	}
	
	private GemBumped(final Player player, final float x, final float y, final int award, final byte type, final Panimation anm) {
		this(FurGuardiansGame.room, player, x, y, award, type, anm, Tiles.g);
	}
	
	protected GemBumped(final Panlayer layer, final Player player, final float x, final float y, final int award, final byte type,
			final Panimation anm, final Panple acc) {
		super(acc);
		this.award = award;
		this.type = type;
		final boolean good = isGood();
		if (good) {
		    Gem.collect(player, award);
		}
		setView(anm);
		FurGuardiansGame.setPosition(this, x, y + ImtilX.DIM, FurGuardiansGame.DEPTH_SHATTER);
		getVelocity().set(0, 6);
        layer.addActor(this);
        if (type == TYPE_END) {
        	//FurGuardiansGame.soundGemLevel.startSound();
        	// Probably not needed; ending level plays bells
        } else if (good || type == TYPE_DECORATION) {
        	Gem.playSound();
        } else {
            // gemShatter
        	FurGuardiansGame.soundCrumble.startSound();
        }
	}
	
	@Override
	public final void onStep(final StepEvent event) {
		super.onStep(event);
		age++;
		if (age >= duration) {
		    if (type == TYPE_DECORATION || isGood()) {
		        Gem.spark(getLayer(), getPosition(), type == TYPE_END);
		    } else {
		        Tiles.shatterTile(FurGuardiansGame.gemShatter, getPosition(), true);
		    }
		    destroy();
		}
	}
	
	private final boolean isGood() {
	    return award > 0;
	}
	
	protected final static void setSeed(final long seed) {
        rand.setSeed(seed);
    }
}
