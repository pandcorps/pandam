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

import org.pandcorps.core.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.furguardians.Profile.*;
import org.pandcorps.furguardians.Player.*;

public abstract class Goal implements Named {
	protected final static int NUM_ACTIVE_GOALS = 3;
	protected final byte award;
	protected boolean brandNew = false;
	
	protected Goal(final byte award) {
		this.award = award;
	}
	
	protected abstract long getAmount();
	
	protected abstract String getAction();
	
	protected abstract String getLabelSingular();
	
	protected String getLabelPlural() {
	    return getLabelSingular() + "s";
	}
	
	protected final String getLabel() {
		return (getAmount() == 1) ? getLabelSingular() : getLabelPlural();
	}
	
	@Override
	public String getName() {
		return getAction() + " " + getAmount() + " " + getLabel();
	}
	
	public abstract String getProgress(final PlayerContext pc);
	
	public final boolean isMet(final PlayerContext pc) {
		return !brandNew && met(pc);
	}
	
	protected abstract boolean met(final PlayerContext pc);
	
	public abstract Field toField();
	
	public final static void initGoals(final PlayerContext pc) {
		final Goal[] goals = pc.profile.currentGoals;
		for (byte award = 1; award <= NUM_ACTIVE_GOALS; award++) {
			if (goals[award - 1] == null) {
				newGoal(award, pc);
			}
		}
	}
	
	public final static Goal newGoal(final byte award, final PlayerContext pc) {
		final int max, index = award - 1;
		if (award < 3) {
			max = 9;
		} else if (Map.isOnLastLevel()) {
			max = 10;
		} else {
			max = 11;
		}
		final Goal[] goals = pc.profile.currentGoals;
		//goals[index] = null; Don't reuse same Goal when assigning a new one; don't null out before checking
		while (true) {
			final int r = Mathtil.randi(0, max);
			final Goal g;
			switch(r) {
				case 0: g = new LevelGoal(award, pc); break;
				case 1: g = new EnemyGoal(award, pc); break;
				case 2: g = new BreakGoal(award, pc); break;
				case 3: g = new BumpGoal(award, pc); break;
				case 4: g = new JumpGoal(award, pc); break;
				case 5: g = new GemGoal(award); break;
				case 6: g = new FallGoal(award); break;
				case 7: g = new HitGoal(award); break;
				case 8: g = new WordGoal(award, pc); break;
				case 9: g = new BlueGemGoal(award, pc); break;
				case 10: g = new BonusGoal(award, pc); break;
				default: g = new WorldGoal(award, pc); break;
			}
			if (hasCurrentGoal(goals, g.getClass())) {
				continue;
			}
			goals[index] = g;
			return g;
		}
	}
	
	public final static boolean hasCurrentGoal(final Goal[] goals, final Class<? extends Goal> gc) {
        for (final Goal c : goals) {
            if (c != null && c.getClass() == gc) {
                return true;
            }
        }
        return false;
    }
	
	public final static boolean hasCurrentGoal(final Profile prf, final Class<? extends Goal> gc) {
        return prf != null && hasCurrentGoal(prf.currentGoals, gc);
    }
	
    public final static boolean hasCurrentGoal(final PlayerContext pc, final Class<? extends Goal> gc) {
        return pc != null && hasCurrentGoal(pc.profile, gc);
    }
    
    public final static boolean hasCurrentGoal(final Player p, final Class<? extends Goal> gc) {
        return p != null && hasCurrentGoal(p.pc, gc);
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
		} else if ("GemGoal".equals(type)) {
			return new GemGoal(f);
		} else if ("FallGoal".equals(type)) {
			return new FallGoal(f);
		} else if ("HitGoal".equals(type)) {
			return new HitGoal(f);
		} else if ("WordGoal".equals(type)) {
            return new WordGoal(f);
		} else if ("BlueGemGoal".equals(type)) {
            return new BlueGemGoal(f);
		} else if ("BonusGoal".equals(type)) {
			return new BonusGoal(f);
		} else if ("WorldGoal".equals(type)) {
			return new WorldGoal(f);
		}
		throw new IllegalArgumentException(type);
	}
	
	public final static boolean isAnyMet(final PlayerContext pc) {
		for (final Goal g : pc.profile.currentGoals) {
			if (g != null && g.isMet(pc)) {
				return true;
			}
		}
		return false;
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
		
		protected abstract long getCurrentAmount(final Statistics stats);
		
		private final long getTarget() {
			return start + getAmount();
		}
		
		@Override
		public final String getProgress(final PlayerContext pc) {
			final long amount = getAmount();
			return Math.min(amount, (getCurrentAmount(pc.profile.stats) - start)) + " of " + amount;
		}
		
		@Override
		protected final boolean met(final PlayerContext pc) {
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
	
	protected abstract static class RunGoal extends Goal {
		protected RunGoal(final byte award) {
			super(award);
		}
		
		protected RunGoal(final Field f) {
			super(f.byteValue(1));
		}
		
		protected abstract long getCurrentAmount(final Player player);
		
		@Override
		public final String getName() {
			return super.getName() + " in 1 run";
		}
		
		@Override
		public final String getProgress(final PlayerContext pc) {
			return "";
		}
		
		@Override
		protected final boolean met(final PlayerContext pc) {
			final Player player = pc.player;
			return player != null && !player.goalsMet[award - 1] && getCurrentAmount(player) >= getAmount();
		}
		
		@Override
		public final Field toField() {
			final Field f = new Field();
			f.setValue(0, getClass().getSimpleName());
			f.setByte(1, award);
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
	}
	
	public final static class BonusGoal extends StatGoal {
		public BonusGoal(final byte award, final PlayerContext pc) {
			super(award, pc);
		}
		
		protected BonusGoal(final Field f) {
			super(f);
		}
		
		@Override
		protected final long getAmount() {
			return 1;
		}
		
		@Override
		protected final long getCurrentAmount(final Statistics stats) {
			return stats.playedBonuses;
		}
		
		@Override
		protected String getAction() {
			return "Play";
		}
		
		@Override
		protected final String getLabelSingular() {
			return "Bonus Game";
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
			return award * 5;
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
			return award * 10;
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
			return award * 10;
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
			return 25 * Mathtil.pow(2, award - 1);
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
	}
	
	public final static class GemGoal extends RunGoal {
		public GemGoal(final byte award) {
			super(award);
		}
		
		protected GemGoal(final Field f) {
			super(f);
		}
		
		@Override
		protected final long getAmount() {
			return award * 100;
		}
		
		@Override
		protected final long getCurrentAmount(final Player player) {
			return FurGuardiansGame.level ? player.levelGems : 0;
		}
		
		@Override
		protected final String getAction() {
			return "Collect";
		}
		
		@Override
		protected final String getLabelSingular() {
			return "Gem";
		}
	}
	
	public final static class WordGoal extends StatGoal {
        public WordGoal(final byte award, final PlayerContext pc) {
            super(award, pc);
        }
        
        protected WordGoal(final Field f) {
            super(f);
        }
        
        @Override
        protected final long getAmount() {
            return award;
        }
        
        @Override
        protected final long getCurrentAmount(final Statistics stats) {
            return stats.collectedWords;
        }
        
        @Override
        protected final String getAction() {
            return "Collect";
        }
        
        @Override
        protected final String getLabelSingular() {
            return "Bonus Word";
        }
    }
	
	public final static class BlueGemGoal extends StatGoal {
        public BlueGemGoal(final byte award, final PlayerContext pc) {
            super(award, pc);
        }
        
        protected BlueGemGoal(final Field f) {
            super(f);
        }
        
        @Override
        protected final long getAmount() {
            return Mathtil.pow(2, award);
        }
        
        @Override
        protected final long getCurrentAmount(final Statistics stats) {
            return stats.foundBlueGems;
        }
        
        @Override
        protected final String getAction() {
            return "Collect";
        }
        
        @Override
        protected final String getLabelSingular() {
            return "Blue Gem";
        }
    }
	
	public final static class FallGoal extends RunGoal {
		public FallGoal(final byte award) {
			super(award);
		}
		
		protected FallGoal(final Field f) {
			super(f);
		}
		
		@Override
		protected final long getAmount() {
			return 2 * award;
		}
		
		@Override
		protected final long getCurrentAmount(final Player player) {
			return player.levelFalls;
		}
		
		@Override
		protected final String getAction() {
			return "Fall";
		}
		
		@Override
		protected final String getLabelSingular() {
			return "Time";
		}
	}
	
	public final static class HitGoal extends RunGoal {
		public HitGoal(final byte award) {
			super(award);
		}
		
		protected HitGoal(final Field f) {
			super(f);
		}
		
		@Override
		protected final long getAmount() {
			return award;
		}
		
		@Override
		protected final long getCurrentAmount(final Player player) {
			return player.levelHits;
		}
		
		@Override
		protected final String getAction() {
			return "Get hit";
		}
		
		@Override
		protected final String getLabelSingular() {
			return "Time";
		}
	}
}
