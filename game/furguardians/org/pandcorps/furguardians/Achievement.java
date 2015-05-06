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

import org.pandcorps.furguardians.Player.*;
import org.pandcorps.furguardians.Profile.*;

public abstract class Achievement extends FinName {
	protected final static Achievement[] ALL = {
		new LevelFeat("Level 1", 1), new LevelFeat("Level Champ", 50),
		new WorldFeat("World 1", 1), new WorldFeat("World Tour", 10),
		new NoEnemyFeat(), new AllEnemyFeat(),
		new RankFeat("Promoted", 2), new RankFeat("Knighted", 25),
		new WordFeat("Wordsmith", 5), new WordFeat("Lexicon", 30),
		new MonarchFeat(), new PegasusFeat(),
		new GemFeat("Entrepreneur", 10000), new GemFeat("Tycoon", 100000), new GemFeat("Millionaire", 1000000),
		new GreenGemFeat(),
		new BumpFeat("Thud", 1000), new BumpFeat("Thump", 5000),
		new BreakFeat("Wreckage", 600), new BreakFeat("Devastation", 1500),
		new EnemyFeat("Monster Hunter", 500), new EnemyFeat("Monster Slayer", 3000),
		new JumpFeat("Leapfrog", 3000), new HitFeat("Eagle-eyed", 10), new MonsterBumpFeat("Sneak Attack", 50),
		new BonusLevelFeat("Roll the Dice", 5), new KickFeat("Kick the Ball", 20),
		new NoGemsFeat(), new AllGemsFeat(), new AllBrokenFeat(),
		new GiantFeat("Giant Slayer", 50), new HardFeat()
		// level w/ no damage
		// Beyond Belief, Finish level as a flying pig
		// Babe, Finish level as a blue bull/ox
		// Menagerie (Zoologist), Use each animal to finish a level
		// Play a bonus level
		// Buy a JumpMode
		// Buy an Assist
		// Defeat all levels within a World (including optional one)
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
	
	//@OverrideMe
	public String getProgress(final PlayerContext pc) {
	    return null;
	}
	
	//@OverrideMe
	public String getNote() {
	    return null;
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
	
	private abstract static class CountFeat extends ProfileFeat {
	    private final int n;
	    
	    protected CountFeat(final String name, final int n, final String desc, final int award) {
	        super(name, desc, award);
	        this.n = n;
	    }
	    
	    @Override
        public final boolean isMet(final Profile prf) {
            return getCurrent(prf) >= n;
        }
	    
	    @Override
        public final String getProgress(final PlayerContext pc) {
	        final Profile prf = pc.profile;
            return (prf == null) ? null : (getCurrent(prf) + " of " + n);
        }
	    
	    public abstract long getCurrent(final Profile prf);
	}
	
	private abstract static class StatFeat extends CountFeat {
	    protected StatFeat(final String name, final int n, final String desc, final int award) {
	        super(name, n, desc, award);
	    }
	    
	    @Override
	    public final long getCurrent(final Profile prf) {
	        final Statistics stats = prf.stats;
            return (stats == null) ? 0 : getCurrent(stats);
	    }
	    
	    public abstract long getCurrent(final Statistics stats);
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
		protected LevelFeat(final String name, final int n) {
			super(name, n, "Defeat " + n + " level" + getS(n), n * 25);
		}
		
		@Override
		public final long getCurrent(final Statistics stats) {
			return stats.defeatedLevels;
		}
	}
	
	private final static class WorldFeat extends StatFeat {
		protected WorldFeat(final String name, final int n) {
			super(name, n, "Defeat " + n + " world" + getS(n), n * 150);
		}
		
		@Override
		public final long getCurrent(final Statistics stats) {
			return stats.defeatedWorlds;
		}
	}
	
	private final static class WordFeat extends StatFeat {
        protected WordFeat(final String name, final int n) {
            super(name, n, "Collect " + n + " bonus word" + getS(n), n * 30);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.collectedWords;
        }
    }
	
	private final static class GemFeat extends StatFeat {
        protected GemFeat(final String name, final int n) {
            super(name, n, "Get " + n + " total Gem" + getS(n), n / 40);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.totalGems;
        }
        
        @Override
        public final String getNote() {
            return "Total ever found, spending won't hurt";
        }
    }
	
	private final static class GreenGemFeat extends StatFeat {
        protected GreenGemFeat() {
            super("Serendipity", 1, "Find 1 green Gem in a level", 1000);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.foundGreenGems;
        }
	}
	
	private final static class BumpFeat extends StatFeat {
        protected BumpFeat(final String name, final int n) {
            super(name, n, "Bump " + n + " block" + getS(n), n / 4);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.bumpedBlocks;
        }
	}
	
	private final static class BreakFeat extends StatFeat {
        protected BreakFeat(final String name, final int n) {
            super(name, n, "Break " + n + " block" + getS(n), n / 3);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.brokenBlocks;
        }
    }
	
	private final static class EnemyFeat extends StatFeat {
        protected EnemyFeat(final String name, final int n) {
            super(name, n, "Defeat " + n + " enemies", n / 2);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.defeatedEnemies;
        }
    }
	
	private final static class GiantFeat extends StatFeat {
        protected GiantFeat(final String name, final int n) {
            super(name, n, "Defeat " + n + " giant" + getS(n), n * 6);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.getDefeatedGiants();
        }
    }
	
	private final static class JumpFeat extends StatFeat {
        protected JumpFeat(final String name, final int n) {
            super(name, n, "Jump " + n + " time" + getS(n), n / 10);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.jumps;
        }
    }
	
	private final static class HitFeat extends StatFeat {
        protected HitFeat(final String name, final int n) {
            super(name, n, "Hit " + n + " enemies with an object", n * 10);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.hitEnemies;
        }
    }
	
	private final static class MonsterBumpFeat extends StatFeat {
        protected MonsterBumpFeat(final String name, final int n) {
            super(name, n, "Defeat " + n + " enemies by hitting their block", n * 4);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.bumpedEnemies;
        }
    }
	
	private final static class BonusLevelFeat extends StatFeat {
        protected BonusLevelFeat(final String name, final int n) {
            super(name, n, "Play " + n + " bonus level" + getS(n), n * 100);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.playedBonuses;
        }
    }
	
	private final static class KickFeat extends StatFeat {
        protected KickFeat(final String name, final int n) {
            super(name, n, "Kick " + n + " object" + getS(n), n * 5);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.kicks;
        }
    }
	
	private final static class RankFeat extends CountFeat {
		protected RankFeat(final String name, final int n) {
			super(name, n, "Reach rank " + n, n * 100);
		}
		
		@Override
		public final long getCurrent(final Profile prf) {
			return prf.getRank();
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
            return Level.numEnemies > 0 && player.levelDefeatedEnemies == 0;
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
	
	private final static class NoGemsFeat extends CurrentFeat {
        protected NoGemsFeat() {
            super("Bear Market", "Finish a level with no gems", 250);
        }
        
        @Override
        public final boolean isMet(final Player player) {
            return Level.numGems > 0 && player.levelEndGems == 0;
        }
    }
	
	private final static class AllGemsFeat extends CurrentFeat {
        protected AllGemsFeat() {
            super("Bull Market", "Collect all gems in a level", 500);
        }
        
        @Override
        public final boolean isMet(final Player player) {
            return Level.numGems > 0 && player.levelFloatingGems == Level.numGems;
        }
    }
	
	private final static class AllBrokenFeat extends CurrentFeat {
        protected AllBrokenFeat() {
            super("Juggernaut", "Break all blocks in a level", 150);
        }
        
        @Override
        public final boolean isMet(final Player player) {
            return Level.numBreakable > 0 && player.levelBrokenBlocks == Level.numBreakable;
        }
    }
	
	private final static class HardFeat extends CurrentFeat {
	    protected HardFeat() {
	        super("Lionheart", "Play a level on highest difficulty", 2000);
	    }
	    
	    @Override
        public final boolean isMet(final Player player) {
            return Level.numEnemies > 0 && player.pc != null && player.pc.profile != null && player.pc.profile.isHardMode();
        }
	}
	
	private final static String getS(final int n) {
		return n == 1 ? "" : "s";
	}
	
	public final static Achievement get(final String name) {
	    return Player.get(ALL, name);
	}
}
