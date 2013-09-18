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

import java.util.Set;

import org.pandcorps.platform.Player.PlayerContext;
import org.pandcorps.platform.Profile.Statistics;

public abstract class Achievement {
	protected final static Achievement[] ALL = {
		new LevelFeat("Level 1", 1), new LevelFeat("Level Champ", 50),
		new WorldFeat("World 1", 1), new WorldFeat("World Tour", 10),
		new NoEnemyFeat(), new AllEnemyFeat()
		// level w/ no damage
		// level w/ no gems
		// block milestones
		// Gem milestones
		// Pegasus, Finish Level as a winged horse
		// Babe, Finish Level as a blue bull/ox
		// Menagerie, Use each animal to finish a Level
	};
	
	private final String name;
	
	private final String desc;
	
	protected Achievement(final String name, final String desc) {
		this.name = name;
		this.desc = desc;
	}
	
	public final String getName() {
		return name;
	}
	
	public final String getDescription() {
		return desc;
	}
	
	public abstract boolean isMet(final PlayerContext pc);
	
	public final static void evaluate() {
		for (final PlayerContext pc : PlatformGame.pcs) {
			evaluate(pc);
		}
	}
	
	public final static void evaluate(final PlayerContext pc) {
		final Profile profile = pc.profile;
		final Set<Integer> achieved = profile.achievements;
		final int size = ALL.length;
		boolean any = false;
		for (int i = 0; i < size; i++) {
			final Integer key = Integer.valueOf(i);
			if (achieved.contains(key)) {
				continue;
			}
			final Achievement ach = ALL[i];
			if (ach.isMet(pc)) {
				achieved.add(key);
				// Notify
				System.out.println(pc.getName() + ": " + ach.getName());
				any = true;
			}
		}
		if (any) {
			profile.serialize();
		}
	}
	
	private abstract static class StatFeat extends Achievement {
	    protected StatFeat(final String name, final String desc) {
	        super(name, desc);
	    }
	    
	    @Override
        public final boolean isMet(final PlayerContext pc) {
	        return isMet(pc.profile.stats);
	    }
	    
	    public abstract boolean isMet(final Statistics stats);
	}
	
	private abstract static class CurrentFeat extends Achievement {
        protected CurrentFeat(final String name, final String desc) {
            super(name, desc);
        }
        
        @Override
        public final boolean isMet(final PlayerContext pc) {
            return pc.player == null ? false : isMet(pc.player);
        }
        
        public abstract boolean isMet(final Player player);
    }
	
	private final static class LevelFeat extends StatFeat {
		private final int n;
		
		protected LevelFeat(final String name, final int n) {
			super(name, "Defeat " + n + " level" + getS(n));
			this.n = n;
		}
		
		@Override
		public final boolean isMet(final Statistics stats) {
			return stats.defeatedLevels >= n;
		}
	}
	
	private final static class WorldFeat extends StatFeat {
		private final int n;
		
		protected WorldFeat(final String name, final int n) {
			super(name, "Defeat " + n + " world" + getS(n));
			this.n = n;
		}
		
		@Override
		public final boolean isMet(final Statistics stats) {
			return stats.defeatedWorlds >= n;
		}
	}
	
	private final static class NoEnemyFeat extends CurrentFeat {
	    protected NoEnemyFeat() {
            super("Dove", "Finish level without defeating any enemies");
        }
        
        @Override
        public final boolean isMet(final Player player) {
            return player.levelDefeatedEnemies == 0;
        }
	}
	
	private final static class AllEnemyFeat extends CurrentFeat {
        protected AllEnemyFeat() {
            super("Hawk", "Defeat all enemies in level");
        }
        
        @Override
        public final boolean isMet(final Player player) {
            return Level.numEnemies > 0 && player.levelDefeatedEnemies == Level.numEnemies;
        }
    }
	
	private final static String getS(final int n) {
		return n == 1 ? "" : "s";
	}
}
