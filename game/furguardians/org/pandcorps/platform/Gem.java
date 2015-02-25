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

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.tile.*;

public class Gem extends TileOccupant implements StepListener {
	private final static Panple sparkPos = new ImplPanple();
	private static long lastSound = -1;
	private final Panmage[] gem;
	
	public Gem() {
		this(FurGuardiansGame.gem);
	}
	
	public Gem(final Panmage[] gem) {
		this.gem = gem;
		setView(gem[0]);
	}
	
	@Override
	public final void onStep(final StepEvent event) {
		// Panimation would allow flashes to be out of synch for gems created at different times
		final long tick = Pangine.getEngine().getClock() % FurGuardiansGame.TIME_FLASH;
		if (tick < 3) {
			setView(gem[(((int) tick) + 1) % 3]);
		}
	}
	
	/*protected final void onCollide(final Player player) {
		if (isDestroyed()) {
			return;
		}
		collect(player, GemBumped.AWARD_DEF);
		spark();
	}*/
	
	protected final static void onCollide(final TileMap tm, final int index, final Player player) {
		collect(player, GemBumped.AWARD_DEF); // Also see GemAttracted
		spark(tm, index);
	}
	
	protected final static void collect(final Player player, final int gems) {
		player.addGems(gems);
	}
	
	protected final static void spark(final TileMap tm, final int index) {
		tm.setTile(index, null);
		tm.savePosition(sparkPos, index);
	    spark(sparkPos, false);
	    playSound();
	}
	
	protected final static void spark(final Panple pos, final boolean end) {
		spark(FurGuardiansGame.room, pos, end);
	}
	
	protected final static void spark(final Panlayer layer, final Panple pos, final boolean end) {
		new Spark(layer, Spark.DEF_COUNT, pos.getX() + 8, pos.getY() + 8, end);
	}
	
	protected final static void playSound() {
		final long clock = Pangine.getEngine().getClock();
		if (clock > lastSound) {
			FurGuardiansGame.soundGem.startSound();
			lastSound = clock + 1;
		}
	}
	
	protected final static class GemAttracted extends Panctor implements StepListener {
		private final double speed;
		private final Player dst;
		private final Panple viewPos = new ImplPanple();
		private final Panple vel = new ImplPanple();
		
		protected GemAttracted(final int index, final Player dst) {
			speed = dst.getVelWalk() + 2;
			this.dst = dst;
			setView(FurGuardiansGame.gemAnm.getFrames()[0].getImage());
			Level.tm.savePosition(getPosition(), index);
			FurGuardiansGame.setDepth(this, FurGuardiansGame.DEPTH_SHATTER);
			Level.tm.setTile(index, null);
			FurGuardiansGame.room.addActor(this);
		}

		@Override
		public final void onStep(final StepEvent event) {
			final Panple rawPos = getPosition();
			viewPos.set(rawPos);
			viewPos.add(8, -4);
			Panple.subtract(vel, dst.getPosition(), viewPos);
			final float mag = (float) vel.getMagnitude2();
			if (mag <= (speed + 0.5)) {
				spark(viewPos, false);
				collect(dst, GemBumped.AWARD_DEF);
				playSound();
				destroy();
				return;
			}
			vel.setMagnitude2(speed);
			vel.setZ(0);
			rawPos.add(vel);
		}
	}
}
