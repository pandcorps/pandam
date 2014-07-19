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
import org.pandcorps.core.seg.*;
import org.pandcorps.platform.Profile.*;
import org.pandcorps.platform.Player.*;

public abstract class Goal implements Named {
	protected final byte award;
	
	protected Goal(final byte award) {
		this.award = award;
	}
	
	public abstract String getProgress(final PlayerContext pc);
	
	public abstract boolean isMet(final PlayerContext pc);
	
	public abstract Field toField();
	
	public final static void initGoals(final PlayerContext pc) {
		final Goal[] goals = pc.profile.currentGoals;
		for (byte award = 1; award <= 3; award++) {
			if (goals[award - 1] == null) {
				newGoal(award, pc);
			}
		}
	}
	
	public final static Goal newGoal(final byte award, final PlayerContext pc) {
		final int max = award < 3 ? 4 : 5, index = award - 1;
		final Goal[] goals = pc.profile.currentGoals;
		goals[index] = null;
		while (true) {
			final int r = Mathtil.randi(0, max);
			final Goal g;
			switch(r) {
				case 0: g = new LevelGoal(award, pc); break;
				case 1: g = new EnemyGoal(award, pc); break;
				case 2: g = new BreakGoal(award, pc); break;
				case 3: g = new BumpGoal(award, pc); break;
				case 4: g = new JumpGoal(award, pc); break;
				default: g = new WorldGoal(award, pc); break;
			}
			final Class<?> gc = g.getClass();
			for (final Goal c : goals) {
				if (c != null && c.getClass() == gc) {
					continue;
				}
			}
			goals[index] = g;
			return g;
		}
	}
	
	public final static Goal parseField(final Field f) {
		if (f == null) {
			return null;
		}
		final String type = f.getValue(0);
		if (Chartil.isEmpty(type) || "null".equalsIgnoreCase(type)) {
			return null;
		} else if ("LevelGoal".equals(type)) {
			return new LevelGoal(f);
		} else if ("EnemyGoal".equals(type)) {
			return new EnemyGoal(f);
		} else if ("BreakGoal".equals(type)) {
			return new BreakGoal(f);
		} else if ("BumpGoal".equals(type)) {
			return new BumpGoal(f);
		} else if ("JumpGoal".equals(type)) {
			return new JumpGoal(f);
		} else if ("WorldGoal".equals(type)) {
			return new WorldGoal(f);
		}
		throw new IllegalArgumentException(type);
	}
	
	protected abstract static class StatGoal extends Goal {
		protected final long start;
		
		protected StatGoal(final byte award, final PlayerContext pc) {
			super(award);
			this.start = getCurrentAmount(pc.profile.stats);
		}
		
		protected StatGoal(final Field f) {
			super(f.byteValue(1));
			this.start = f.longValue(2);
		}
		
		protected abstract long getAmount();
		
		protected abstract long getCurrentAmount(final Statistics stats);
		
		protected abstract String getAction();
		
		protected abstract String getLabelSingular();
		
		protected abstract String getLabelPlural();
		
		protected final String getLabel() {
			return (getAmount() == 1) ? getLabelSingular() : getLabelPlural();
		}
		
		private final long getTarget() {
			return start + getAmount();
		}
		
		@Override
		public final String getName() {
			return getAction() + " " + getAmount() + " " + getLabel();
		}

		@Override
		public final String getProgress(final PlayerContext pc) {
			return (getCurrentAmount(pc.profile.stats) - start) + " of " + getAmount();
		}
		
		@Override
		public final boolean isMet(final PlayerContext pc) {
			return getCurrentAmount(pc.profile.stats) >= getTarget();
		}
		
		@Override
		public final Field toField() {
			final Field f = new Field();
			f.setValue(0, getClass().getSimpleName());
			f.setByte(1, award);
			f.setLong(2, start);
			return f;
		}
	}
	
	protected abstract static class DefeatGoal extends StatGoal {
		protected DefeatGoal(final byte award, final PlayerContext pc) {
			super(award, pc);
		}
		
		protected DefeatGoal(final Field f) {
			super(f);
		}
		
		@Override
		protected final String getAction() {
			return "Defeat";
		}
	}
	
	public final static class WorldGoal extends DefeatGoal {
		public WorldGoal(final byte award, final PlayerContext pc) {
			super(award, pc);
		}
		
		protected WorldGoal(final Field f) {
			super(f);
		}
		
		@Override
		protected final long getAmount() {
			return 1;
		}
		
		@Override
		protected final long getCurrentAmount(final Statistics stats) {
			return stats.defeatedWorlds;
		}
		
		@Override
		protected final String getLabelSingular() {
			return "World";
		}
		
		@Override
		protected final String getLabelPlural() {
			return "Worlds";
		}
	}
	
	public final static class LevelGoal extends DefeatGoal {
		public LevelGoal(final byte award, final PlayerContext pc) {
			super(award, pc);
		}
		
		protected LevelGoal(final Field f) {
			super(f);
		}
		
		@Override
		protected final long getAmount() {
			return ((award - 1) * 2) + 1;
		}
		
		@Override
		protected final long getCurrentAmount(final Statistics stats) {
			return stats.defeatedLevels;
		}
		
		@Override
		protected final String getLabelSingular() {
			return "Level";
		}
		
		@Override
		protected final String getLabelPlural() {
			return "Levels";
		}
	}
	
	public final static class EnemyGoal extends DefeatGoal {
		public EnemyGoal(final byte award, final PlayerContext pc) {
			super(award, pc);
		}
		
		protected EnemyGoal(final Field f) {
			super(f);
		}
		
		@Override
		protected final long getAmount() {
			return award * 15;
		}
		
		@Override
		protected final long getCurrentAmount(final Statistics stats) {
			return stats.defeatedEnemies;
		}
		
		@Override
		protected final String getLabelSingular() {
			return "Enemy";
		}
		
		@Override
		protected final String getLabelPlural() {
			return "Enemies";
		}
	}
	
	public final static class BreakGoal extends StatGoal {
		public BreakGoal(final byte award, final PlayerContext pc) {
			super(award, pc);
		}
		
		protected BreakGoal(final Field f) {
			super(f);
		}
		
		@Override
		protected final long getAmount() {
			return award * 25;
		}
		
		@Override
		protected final long getCurrentAmount(final Statistics stats) {
			return stats.brokenBlocks;
		}
		
		@Override
		protected final String getAction() {
			return "Break";
		}
		
		@Override
		protected final String getLabelSingular() {
			return "Block";
		}
		
		@Override
		protected final String getLabelPlural() {
			return "Blocks";
		}
	}
	
	public final static class BumpGoal extends StatGoal {
		public BumpGoal(final byte award, final PlayerContext pc) {
			super(award, pc);
		}
		
		protected BumpGoal(final Field f) {
			super(f);
		}
		
		@Override
		protected final long getAmount() {
			return award * 25;
		}
		
		@Override
		protected final long getCurrentAmount(final Statistics stats) {
			return stats.bumpedBlocks;
		}
		
		@Override
		protected final String getAction() {
			return "Bump";
		}
		
		@Override
		protected final String getLabelSingular() {
			return "Block";
		}
		
		@Override
		protected final String getLabelPlural() {
			return "Blocks";
		}
	}
	
	public final static class JumpGoal extends StatGoal {
		public JumpGoal(final byte award, final PlayerContext pc) {
			super(award, pc);
		}
		
		protected JumpGoal(final Field f) {
			super(f);
		}
		
		@Override
		protected final long getAmount() {
			return 50 * Mathtil.pow(2, award - 1);
		}
		
		@Override
		protected final long getCurrentAmount(final Statistics stats) {
			return stats.jumps;
		}
		
		@Override
		protected final String getAction() {
			return "Jump";
		}
		
		@Override
		protected final String getLabelSingular() {
			return "Time";
		}
		
		@Override
		protected final String getLabelPlural() {
			return "Times";
		}
	}
}
