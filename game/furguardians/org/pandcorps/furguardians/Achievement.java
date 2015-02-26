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

import java.util.*;

import org.pandcorps.platform.Player.*;
import org.pandcorps.platform.Profile.*;

public abstract class Achievement extends FinName {
	protected final static Achievement[] ALL = {
		new LevelFeat("Level 1", 1), new LevelFeat("Level Champ", 50),
		new WorldFeat("World 1", 1), new WorldFeat("World Tour", 10),
		new NoEnemyFeat(), new AllEnemyFeat(),
		new RankFeat("Promoted", 2), new RankFeat("Knighted", 25),
		new WordFeat("Wordsmith", 5), new WordFeat("Lexicon", 30),
		new MonarchFeat(), new PegasusFeat()
		// level w/ no damage
		// Bear Market, Finish Level w/ no gems
		// Bull Market, Collect all gems in a Level
		// Juggernaut, Break all blocks in a Level
		// block milestones
		// Gem milestones
		// Beyond Belief, Finish Level as a flying pig
		// Babe, Finish Level as a blue bull/ox
		// Menagerie (Zoologist), Use each animal to finish a Level
		// Collect a green gem
		// Play a bonus level
		// Buy a JumpMode
		// Buy an Assist
		// Defeat all Levels within a World (including optional one)
	};
	
	private final String desc;
	private final int award;
	
	protected Achievement(final String name, final String desc, final int award) {
		super(name);
		this.desc = desc;
		this.award = award;
	}
	
	public final String getDescription() {
		return desc;
	}
	
	public final int getAward() {
	    return award;
	}
	
	public abstract boolean isMet(final PlayerContext pc);
	
	public final static void evaluate() {
		for (final PlayerContext pc : FurGuardiansGame.pcs) {
			evaluate(pc);
		}
	}
	
	public final static void evaluate(final PlayerContext pc) {
		if (pc == null) {
			return;
		}
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
				final int award = ach.award;
				FurGuardiansGame.notify(pc, ach.getName() + ", " + award + " Gem bonus", new Gem(FurGuardiansGame.gemAchieve));
				pc.addGems(award);
				any = true;
			}
		}
		if (any) {
			profile.save();
		}
	}
	
	private abstract static class ProfileFeat extends Achievement {
	    protected ProfileFeat(final String name, final String desc, final int award) {
	        super(name, desc, award);
	    }
	    
	    @Override
        public final boolean isMet(final PlayerContext pc) {
	    	final Profile prf = pc.profile;
	        return (prf != null) && isMet(prf);
	    }
	    
	    public abstract boolean isMet(final Profile prf);
	}
	
	private abstract static class StatFeat extends ProfileFeat {
	    protected StatFeat(final String name, final String desc, final int award) {
	        super(name, desc, award);
	    }
	    
	    @Override
        public final boolean isMet(final Profile prf) {
	    	final Statistics stats = prf.stats;
	        return (stats != null) && isMet(stats);
	    }
	    
	    public abstract boolean isMet(final Statistics stats);
	}
	
	private abstract static class AvatarFeat extends ProfileFeat {
        protected AvatarFeat(final String name, final String desc, final int award) {
            super(name, desc, award);
        }
        
        @Override
        public final boolean isMet(final Profile prf) {
            final Avatar avt = prf.currentAvatar;
            return (avt != null) && isMet(avt);
        }
        
        public abstract boolean isMet(final Avatar avt);
    }
	
	private abstract static class CurrentFeat extends Achievement {
        protected CurrentFeat(final String name, final String desc, final int award) {
            super(name, desc, award);
        }
        
        @Override
        public final boolean isMet(final PlayerContext pc) {
        	final Player player = pc.player;
            return (player != null) && player.level && isMet(player);
        }
        
        public abstract boolean isMet(final Player player);
    }
	
	private final static class LevelFeat extends StatFeat {
		private final int n;
		
		protected LevelFeat(final String name, final int n) {
			super(name, "Defeat " + n + " level" + getS(n), n * 25);
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
			super(name, "Defeat " + n + " world" + getS(n), n * 150);
			this.n = n;
		}
		
		@Override
		public final boolean isMet(final Statistics stats) {
			return stats.defeatedWorlds >= n;
		}
	}
	
	private final static class WordFeat extends StatFeat {
        private final int n;
        
        protected WordFeat(final String name, final int n) {
            super(name, "Collect " + n + " bonus word" + getS(n), n * 30);
            this.n = n;
        }
        
        @Override
        public final boolean isMet(final Statistics stats) {
            return stats.collectedWords >= n;
        }
    }
	
	private final static class RankFeat extends ProfileFeat {
		private final int n;
		
		protected RankFeat(final String name, final int n) {
			super(name, "Reach rank " + n, n * 100);
			this.n = n;
		}
		
		@Override
		public final boolean isMet(final Profile prf) {
			return prf.getRank() >= n;
		}
	}
	
	private final static class MonarchFeat extends AvatarFeat {
        protected MonarchFeat() {
            super("Monarch", "Wear the Royal Robe and Crown", 5000);
        }
        
        @Override
        public final boolean isMet(final Avatar avt) {
            if (avt.clothing == null || avt.hat == null) {
                return false;
            }
            return Avatar.CLOTHING_ROYAL_ROBE.equals(Player.getName(avt.clothing)) && Avatar.HAT_CROWN.equals(Player.getName(avt.hat));
        }
    }
	
	private final static class PegasusFeat extends AvatarFeat {
        protected PegasusFeat() {
            super("Pegasus", "Add Wings to a Horse", 1250);
        }
        
        @Override
        public final boolean isMet(final Avatar avt) {
            return "Horse".equals(avt.anm) && avt.jumpMode == Player.JUMP_FLY;
        }
    }
	
	private final static class NoEnemyFeat extends CurrentFeat {
	    protected NoEnemyFeat() {
            super("Dove", "Finish a level without defeating any enemies", 200);
        }
        
        @Override
        public final boolean isMet(final Player player) {
            return player.levelDefeatedEnemies == 0;
        }
	}
	
	private final static class AllEnemyFeat extends CurrentFeat {
        protected AllEnemyFeat() {
            super("Hawk", "Defeat all enemies in a level", 300);
        }
        
        @Override
        public final boolean isMet(final Player player) {
            return Level.numEnemies > 0 && player.levelDefeatedEnemies == Level.numEnemies;
        }
    }
	
	private final static String getS(final int n) {
		return n == 1 ? "" : "s";
	}
	
	public final static Achievement get(final String name) {
	    return Player.get(ALL, name);
	}
}
