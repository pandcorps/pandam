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
package org.pandcorps.platform;

import org.pandcorps.core.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandax.*;
import org.pandcorps.pandax.tile.Tile;

public class GemBumped extends Pandy {
    protected final static int AWARD_DEF = 1;
    private final static int AWARD_2 = AWARD_DEF * 10;
    private final static int AWARD_3 = AWARD_2 * 10;
    private final static int AWARD_4 = AWARD_3 * 10;
    private final static int AWARD_LEVEL = AWARD_2;
    private final int award;
	private final boolean end;
	int age = 0;
	
	public GemBumped(final Player player, final Tile tile) {
		this(player, tile, rndAward());
	}
	
	private final static int rndAward() {
	    final int r = Mathtil.randi(0, 9999);
	    if (r < 1000) {
	        return AWARD_2;
	    } else if (r < 1100) {
	        return AWARD_3;
	    } else if (r < 1110) {
            return AWARD_4;
	    }
	    return AWARD_DEF;
	}
	
	private GemBumped(final Player player, final Tile tile, final int award) {
	    this(player, tile, award, false, getAnm(award));
	}
	
	private final static Panimation getAnm(final int award) {
        switch (award) {
            case AWARD_DEF : return PlatformGame.gemAnm;
            case AWARD_2 : return PlatformGame.gemBlueAnm;
            case AWARD_3 : return PlatformGame.gemCyanAnm;
            case AWARD_4 : return PlatformGame.gemGreenAnm;
        }
        throw new IllegalArgumentException("Unexpected award amount " + award);
    }
	
	public GemBumped(final Player player, final Enemy defeated) {
		this(player, defeated.getBoundingMinimum(), AWARD_DEF, false, PlatformGame.gemAnm);
	}
	
	public static GemBumped newLevelEnd(final Player player, final Tile tile) {
	    return new GemBumped(player, tile, AWARD_LEVEL, true, PlatformGame.gemLevelAnm);
	}
	
	public static GemBumped newShatter(final Player player) {
	    return new GemBumped(player, player.getBoundingMinimum(), -1, false, PlatformGame.gemAnm);
	}
	
	private GemBumped(final Player player, final Tile tile, final int award, final boolean end, final Panimation anm) {
	    this(player, tile.getPosition(), award, end, anm);
	}
	
	private GemBumped(final Player player, final Panple pos, final int award, final boolean end, final Panimation anm) {
		super(Tiles.g);
		this.award = award;
		this.end = end;
		final boolean good = isGood();
		if (good) {
		    Gem.collect(player, award);
		}
		setView(anm);
		PlatformGame.setPosition(this, pos.getX(), pos.getY() + ImtilX.DIM, PlatformGame.DEPTH_SHATTER);
		getVelocity().set(0, 6);
        PlatformGame.room.addActor(this);
        if (end) {
        	Pangine.getEngine().getMusic().playSound(Music.gemLevel);
        } else if (good) {
        	Gem.playSound();
        } else {
            // gemShatter
        }
	}
	
	@Override
	public final void onStep(final StepEvent event) {
		super.onStep(event);
		age++;
		if (age >= 12) {
		    if (isGood()) {
		        Gem.spark(this, end);
		    } else {
		        Tiles.shatter(PlatformGame.gemShatter, getPosition(), true);
		        destroy();
		    }
		}
	}
	
	private final boolean isGood() {
	    return award > 0;
	}
}
