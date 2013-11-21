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

import java.awt.image.*;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.*;

public final class Enemy extends Character {
	protected final static int ENEMY_X = 5;
	protected final static int ENEMY_H = 15;
	
	private final static FinPanple O = new FinPanple(8, 1, 0);
	private final static FinPanple MIN = new FinPanple(-ENEMY_X, 0, 0);
	private final static FinPanple MAX = new FinPanple(ENEMY_X, ENEMY_H, 0);
	
	protected final static class EnemyDefinition {
		private final Panimation walk;
		private final boolean ledgeTurn;
		private final Panimation splat;
		
		protected EnemyDefinition(final String name, final int ind, final PixelFilter f, final boolean ledgeTurn) {
		    this(name, ind, f, ledgeTurn, false);
		}
		
		protected EnemyDefinition(final String name, final int ind, final PixelFilter f, final boolean ledgeTurn, final boolean splat) {
			final BufferedImage[] strip = ImtilX.loadStrip("org/pandcorps/platform/res/enemy/Enemy0" + ind + ".png"), walk;
			if (f != null) {
				final int size = strip.length;
				for (int i = 0; i < size; i++) {
					strip[i] = Imtil.filter(strip[i], f);
				}
			}
			walk = splat ? new BufferedImage[] {strip[0], strip[1]} : strip;
			final String id = "enemy." + name;
			this.walk = PlatformGame.createAnm(id, 6, O, MIN, MAX, walk);
			this.ledgeTurn = ledgeTurn;
			this.splat = splat ? PlatformGame.createAnm(id + ".splat", 20, O, MIN, MAX, strip[2]) : null;
		}
	}
	
	private final EnemyDefinition def;
	private int avoidCount = 0;
	
	protected Enemy(final EnemyDefinition def, final float x, final float y) {
		super(ENEMY_X, ENEMY_H);
		this.def = def;
		setView(def.walk);
		hv = -1;
		PlatformGame.room.addActor(this);
		PlatformGame.setPosition(this, x, y, PlatformGame.DEPTH_ENEMY);
	}
	
	protected final boolean onStomp(final Player stomper) {
		return defeat(stomper, 0);
	}
	
	@Override
	protected final void onBump(final Character bumper) {
		defeat(bumper, Player.VEL_BUMP);
	}
	
	private final boolean defeat(final Character defeater, final int v) {
	    if (avoidCount > 0) {
	        avoidCount--;
	        burst(PlatformGame.teleport);
	        getPosition().addY(64); //TODO smarter
	        return false;
	    }
		if (defeater != null && defeater.getClass() == Player.class) {
		    final Player player = (Player) defeater;
			new GemBumped(player, this);
			player.levelDefeatedEnemies++;
		}
		if (v == 0 && def.splat != null) {
		    burst(def.splat);
		} else {
		    final Panple pos = getPosition();
    		final Tiles.Faller f = new Tiles.Faller((Panmage) getCurrentDisplay(), pos.getX(), pos.getY() + H, 0, v);
    		f.setMirror(isMirror());
    		f.setFlip(true);
		}
		destroy();
		return true;
	}
	
	private void burst(final Panimation anm) {
	    final Burst b = new Burst(anm);
	    final Panple pos = getPosition();
        PlatformGame.setPosition(b, pos.getX(), pos.getY(), PlatformGame.DEPTH_SHATTER);
        PlatformGame.room.addActor(b);
	}
	
	@Override
	protected final void onScrolled() {
		if (!isDestroyed() && getPosition().getX() + 80 < getLayer().getViewMinimum().getX()) {
			destroy();
		}
	}
	
	@Override
	protected final boolean onHorizontal(final int off) {
		if (!def.ledgeTurn) {
			return false;
		}
		final Panple pos = getPosition();
		final float x = pos.getX(), y = pos.getY();
		pos.addX(off);
		try {
			if (!isGrounded()) {
				pos.addY(-1);
				if (!isGrounded()) {
					hv *= -1;
					return true;
				}
			}
		} finally {
			pos.set(x, y);
		}
		return false;
	}
	
	@Override
	protected final void onWall() {
		hv *= -1;
	}

	@Override
	protected final boolean onFell() {
		destroy();
		return true;
	}
}
