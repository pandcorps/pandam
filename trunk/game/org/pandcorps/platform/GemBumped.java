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
package org.pandcorps.platform;

import org.pandcorps.core.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.*;

public class GemBumped extends Pandy {
	private final static Panple tilePos = new ImplPanple(0, 0, 0);
    protected final static int AWARD_DEF = 1;
    protected final static int AWARD_2 = AWARD_DEF * 10;
    protected final static int AWARD_3 = AWARD_2 * 10;
    protected final static int AWARD_4 = AWARD_3 * 10;
    private final static int AWARD_LEVEL = AWARD_2 * 5;
    private final static int AWARD_WORLD = AWARD_LEVEL * 10;
    protected final static byte TYPE_NORMAL = 0;
    protected final static byte TYPE_END = 1;
    protected final static byte TYPE_LETTER = 2;
    private final int award;
	private final byte type;
	int age = 0;
	
	protected final static int rndAward() {
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
	
	public final static GemBumped create(final Player player, final int index, final int award) {
	    return create(player, index, award, TYPE_NORMAL, getAnm(award));
	}
	
	protected final static Panimation getAnm(final int award) {
        switch (award) {
            case AWARD_DEF : return PlatformGame.gemAnm;
            case AWARD_2 : return PlatformGame.gemBlueAnm;
            case AWARD_3 : return PlatformGame.gemCyanAnm;
            case AWARD_4 : return PlatformGame.gemGreenAnm;
        }
        throw new IllegalArgumentException("Unexpected award amount " + award);
    }
	
	public GemBumped(final Player player, final Enemy defeated) {
		this(player, defeated.getPosition().getX() - 8, defeated.getBoundingMaximum().getY() - Enemy.DEFAULT_H, defeated.def.award, TYPE_NORMAL, getAnm(defeated.def.award));
	}
	
	public final static GemBumped newLevelEnd(final Player player, final int index) {
		final int award;
		final Panimation anm;
		if (Level.isNormalTheme()) {
			award = AWARD_LEVEL;
			anm = PlatformGame.gemLevelAnm;
		} else {
			award = AWARD_WORLD;
			anm = PlatformGame.gemWorldAnm;
		}
	    return create(player, index, award, TYPE_END, anm);
	}
	
	public final static GemBumped newShatter(final Player player) {
		final Panple pos = player.getPosition();
	    return new GemBumped(player, pos.getX() - 8, pos.getY(), -1, TYPE_NORMAL, PlatformGame.gemAnm);
	}
	
	protected final static GemBumped create(final Player player, final int index, final int award, final byte type, final Panimation anm) {
		Level.tm.savePosition(tilePos, index);
	    return new GemBumped(player, tilePos.getX(), tilePos.getY(), award, type, anm);
	}
	
	private GemBumped(final Player player, final float x, final float y, final int award, final byte type, final Panimation anm) {
		super(Tiles.g);
		this.award = award;
		this.type = type;
		final boolean good = isGood();
		if (good) {
		    Gem.collect(player, award);
		}
		setView(anm);
		PlatformGame.setPosition(this, x, y + ImtilX.DIM, PlatformGame.DEPTH_SHATTER);
		getVelocity().set(0, 6);
        PlatformGame.room.addActor(this);
        if (type == TYPE_END) {
        	//if isMusicSupported Pangine.getEngine().getMusic().playSound(Music.gemLevel);
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
		    if (type == TYPE_LETTER || isGood()) {
		        Gem.spark(getPosition(), type == TYPE_END);
		    } else {
		        Tiles.shatter(PlatformGame.gemShatter, getPosition(), true);
		    }
		    destroy();
		}
	}
	
	private final boolean isGood() {
	    return award > 0;
	}
}
