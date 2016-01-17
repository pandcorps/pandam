/*
Copyright (c) 2009-2016, Andrew M. Martin
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
import org.pandcorps.furguardians.Player.*;
import org.pandcorps.furguardians.Profile.*;

public abstract class Achievement extends FinName {
	protected final static Achievement[] ALL = {
		new LevelFeat("Level 1", "0", 1), new LevelFeat("Level Champ", "1", 50), new LevelFeat("Level Master", "lvl.mstr", 250),
		new WorldFeat("World 1", "2", 1), new WorldFeat("World Tour", "3", 10), new WorldFeat("World Master", "wrld.mstr", 30),
		new NoEnemyFeat("4"), new AllEnemyFeat("5"),
		new RankFeat("Promoted", "6", 2), new RankFeat("Knighted", "7", 25), new RankFeat("Revered", "rvrd", 125),
		new WordFeat("Wordsmith", "8", 5), new WordFeat("Lexicon", "9", 30),
		new MonarchFeat("10"), new UnicornFeat("ncrn"), new PegasusFeat("11"),
		new GemFeat("Entrepreneur", "12", 10000), new GemFeat("Tycoon", "13", 100000), new GemFeat("Millionaire", "14", 1000000),
		new GreenGemFeat("15"),
		new BumpFeat("Thud", "16", 1000), new BumpFeat("Thump", "17", 5000), new BumpFeat("Thunk", "thnk", 15000),
		new BreakFeat("Wreckage", "18", 600), new BreakFeat("Devastation", "19", 1500), new BreakFeat("Ruination", "rntn", 3000),
		new EnemyFeat("Monster Hunter", "20", 500), new EnemyFeat("Monster Slayer", "21", 3000), new EnemyFeat("Monster Destroyer", "mnstr.dstryr", 10000),
		new ImpFeat("Imp Hunter", "imp.hntr", 32), new ImpFeat("Imp Slayer", "imp.slyr", 160), new ImpFeat("Imp Destroyer", "imp.dstryr", 800),
		new GiantFeat("Giant Hunter", "gnt.hntr", 12), new GiantFeat("Giant Slayer", "30", 50), new GiantFeat("Giant Destroyer", "gnt.dstryr", 200),
		new WispFeat("Wisp Hunter", "wsp.hntr", 2), new WispFeat("Wisp Slayer", "37", 10), new WispFeat("Wisp Destroyer", "wsp.dstryr", 35),
		new HitFeat("Eagle-eyed", "23", 10), new MonsterBumpFeat("Sneak Attack", "24", 50),
		new ComboFeat("Combo Commander", "cmbo.cmndr", 80), new ComboFeat("Combo King", "cmbo.kng", 240), new ComboLengthFeat("Triple Combo", "trpl.cmbo", 3),
		new JumpFeat("Leapfrog", "22", 3000),
		new BonusLevelFeat("Roll the Dice", "25", 5), new KickFeat("Kick the Ball", "26", 20),
		new NoGemsFeat("27"), new AllGemsFeat("28"), new AllBrokenFeat("29"),
		new HardFeat("31"),
		new BuyFeat("Consumer", "32", 1), new BuyFeat("Demander", "33", 5), new BuyFeat("Collector", "34", 15), new BuyFeat("Obsessor", "obsssr", 30),
		new NoBirdGemsFeat("35"), new BirdGemFeat("Nest Egg", "36", 200),
		new OrbFeat("Orb Wielder", "38", 25), new MonsterElectrocuteFeat("Lightning Storm", "39", 150),
		new DoubledGemFeat("Double Down", "40", 7500), new BounceFeat("Busy Bee", "41", 400)
		// Level w/ no damage
		// Beyond Belief, Finish Level as a flying Pig
		// Babe, Finish Level as a blue Bull/Ox
		// Menagerie (Zoologist), Use each Animal to finish a Level
		// Defeat all Levels within a World (including optional one)
		// Bee-line
	};
	
	protected final String code;
	private final String desc;
	private final int award;
	
	protected Achievement(final String name, final String code, final String desc, final int award) {
		super(name);
		this.code = code;
		this.desc = desc;
		this.award = award;
	}
	
	protected final static void validateAchievements() {
		if (ALL.length < 41) {
			err("All Achievements not found");
		}
		final Set<String> codes = new HashSet<String>();
		for (final Achievement a : ALL) {
			final String code = a.code;
			if (Chartil.isEmpty(code)) {
				err("Found Achievement with no code");
			} else if (Chartil.isEmpty(a.getName())) {
				err("Found Achievement with no name");
			} else if (!codes.add(code)) {
				err("Found duplicate Achievement code " + code);
			}
		}
		if (codes.size() != ALL.length) {
			err("Found " + codes.size() + " Achievement codes but expected " + ALL.length);
		}
	}
	
	private final static void err(final String s) {
		throw new IllegalStateException("Achievement validation failed: " + s);
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
		boolean any = false;
		for (final Achievement ach : ALL) {
			if (profile.isAchieved(ach)) {
				continue;
			}
			if (ach.isMet(pc)) {
			    profile.addAchievement(ach);
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
	    protected ProfileFeat(final String name, final String code, final String desc, final int award) {
	        super(name, code, desc, award);
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
	    
	    protected CountFeat(final String name, final String code, final int n, final String desc, final int award) {
	        super(name, code, desc, award);
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
	    protected StatFeat(final String name, final String code, final int n, final String desc, final int award) {
	        super(name, code, n, desc, award);
	    }
	    
	    @Override
	    public final long getCurrent(final Profile prf) {
	        final Statistics stats = prf.stats;
            return (stats == null) ? 0 : getCurrent(stats);
	    }
	    
	    public abstract long getCurrent(final Statistics stats);
	}
	
	private abstract static class AvatarFeat extends ProfileFeat {
        protected AvatarFeat(final String name, final String code, final String desc, final int award) {
            super(name, code, desc, award);
        }
        
        @Override
        public final boolean isMet(final Profile prf) {
            final Avatar avt = prf.currentAvatar;
            return (avt != null) && isMet(avt);
        }
        
        public abstract boolean isMet(final Avatar avt);
    }
	
	private abstract static class CurrentFeat extends Achievement {
        protected CurrentFeat(final String name, final String code, final String desc, final int award) {
            super(name, code, desc, award);
        }
        
        @Override
        public final boolean isMet(final PlayerContext pc) {
        	final Player player = pc.player;
            return (player != null) && player.level && isMet(player);
        }
        
        public abstract boolean isMet(final Player player);
    }
	
	private final static class LevelFeat extends StatFeat {
		protected LevelFeat(final String name, final String code, final int n) {
			super(name, code, n, "Defeat " + n + " Level" + getS(n), n * 25);
		}
		
		@Override
		public final long getCurrent(final Statistics stats) {
			return stats.defeatedLevels;
		}
	}
	
	private final static class WorldFeat extends StatFeat {
		protected WorldFeat(final String name, final String code, final int n) {
			super(name, code, n, "Defeat " + n + " World" + getS(n), n * 150);
		}
		
		@Override
		public final long getCurrent(final Statistics stats) {
			return stats.defeatedWorlds;
		}
	}
	
	private final static class WordFeat extends StatFeat {
        protected WordFeat(final String name, final String code, final int n) {
            super(name, code, n, "Collect " + n + " Bonus Word" + getS(n), n * 30);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.collectedWords;
        }
    }
	
	private final static class OrbFeat extends StatFeat {
        protected OrbFeat(final String name, final String code, final int n) {
            super(name, code, n, "Collect " + n + " Power Orb" + getS(n), n * 40);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.getFoundOrbs();
        }
    }
	
	private final static class GemFeat extends StatFeat {
        protected GemFeat(final String name, final String code, final int n) {
            super(name, code, n, "Get " + n + " total Gem" + getS(n), n / 40);
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
	
	private final static class BirdGemFeat extends StatFeat {
        protected BirdGemFeat(final String name, final String code, final int n) {
            super(name, code, n, "Let a bird get " + n + " total Gem" + getS(n), n * 15);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.birdGems;
        }
    }
	
	private final static class GreenGemFeat extends StatFeat {
        protected GreenGemFeat(final String code) {
            super("Serendipity", code, 1, "Find 1 green Gem in a level", 1000);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.foundGreenGems;
        }
	}
	
	private final static class DoubledGemFeat extends StatFeat {
        protected DoubledGemFeat(final String name, final String code, final int n) {
            super(name, code, n, "Find " + n + " Gem" + getS(n) + " using Double Gem Orbs", (n * 2) / 3);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.doubledGems;
        }
    }
	
	private final static class BumpFeat extends StatFeat {
        protected BumpFeat(final String name, final String code, final int n) {
            super(name, code, n, "Bump " + n + " Block" + getS(n), n / 4);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.bumpedBlocks;
        }
	}
	
	private final static class BreakFeat extends StatFeat {
        protected BreakFeat(final String name, final String code, final int n) {
            super(name, code, n, "Break " + n + " Block" + getS(n), n / 3);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.brokenBlocks;
        }
    }
	
	private final static class EnemyFeat extends StatFeat {
        protected EnemyFeat(final String name, final String code, final int n) {
            super(name, code, n, "Defeat " + n + " Enemies", n / 2);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.defeatedEnemies;
        }
    }
	
	private final static class ImpFeat extends StatFeat {
        protected ImpFeat(final String name, final String code, final int n) {
            super(name, code, n, "Defeat " + n + " Imp" + getS(n), (n * 5) / 4);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.getDefeatedImps();
        }
    }
	
	private final static class GiantFeat extends StatFeat {
        protected GiantFeat(final String name, final String code, final int n) {
            super(name, code, n, "Defeat " + n + " Giant" + getS(n), n * 6);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.getDefeatedGiants();
        }
    }
	
	private final static class WispFeat extends StatFeat {
        protected WispFeat(final String name, final String code, final int n) {
            super(name, code, n, "Defeat " + n + " Wisp" + getS(n), n * 200);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.getDefeatedWisps();
        }
    }
	
	private final static class ComboFeat extends StatFeat {
        protected ComboFeat(final String name, final String code, final int n) {
            super(name, code, n, "Perform " + n + " Combo" + getS(n), n * 8);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.combos;
        }
    }
	
	private final static class ComboLengthFeat extends StatFeat {
        protected ComboLengthFeat(final String name, final String code, final int n) {
            super(name, code, n, "Perform a x" + n + " Combo", n * 250);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.longestCombo;
        }
    }
	
	private final static class JumpFeat extends StatFeat {
        protected JumpFeat(final String name, final String code, final int n) {
            super(name, code, n, "Jump " + n + " time" + getS(n), n / 10);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.jumps;
        }
    }
	
	private final static class HitFeat extends StatFeat {
        protected HitFeat(final String name, final String code, final int n) {
            super(name, code, n, "Hit " + n + " Enemies with an object", n * 10);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.hitEnemies;
        }
    }
	
	private final static class MonsterBumpFeat extends StatFeat {
        protected MonsterBumpFeat(final String name, final String code, final int n) {
            super(name, code, n, "Defeat " + n + " Enemies by hitting their Block", n * 4);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.bumpedEnemies;
        }
    }
	
	private final static class MonsterElectrocuteFeat extends StatFeat {
        protected MonsterElectrocuteFeat(final String name, final String code, final int n) {
            super(name, code, n, "Defeat " + n + " Enemies with a Lightning Orb", n * 20);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.electrocutedEnemies;
        }
    }
	
	private final static class BonusLevelFeat extends StatFeat {
        protected BonusLevelFeat(final String name, final String code, final int n) {
            super(name, code, n, "Play " + n + " Bonus Game" + getS(n), n * 100);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.playedBonuses;
        }
    }
	
	private final static class KickFeat extends StatFeat {
        protected KickFeat(final String name, final String code, final int n) {
            super(name, code, n, "Kick " + n + " object" + getS(n), n * 5);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.kicks;
        }
    }
	
	private final static class BounceFeat extends StatFeat {
        protected BounceFeat(final String name, final String code, final int n) {
            super(name, code, n, "Bounce on a Bee " + n + " time" + getS(n), (n * 5) / 2);
        }
        
        @Override
        public final long getCurrent(final Statistics stats) {
            return stats.bounces;
        }
    }
	
	private final static class RankFeat extends CountFeat {
		protected RankFeat(final String name, final String code, final int n) {
			super(name, code, n, "Reach rank " + n, n * 100);
		}
		
		@Override
		public final long getCurrent(final Profile prf) {
			return prf.getRank();
		}
	}
	
	protected final static class BuyFeat extends CountFeat {
        protected BuyFeat(final String name, final String code, final int n) {
            super(name, code, n, "Buy " + n + " item" + getS(n), n * 1000);
        }
        
        @Override
        public final long getCurrent(final Profile prf) {
            return getPurchases(prf);
        }
        
        protected final static int getPurchases(final Profile prf) {
            // Start with normal jump mode, so subtract one for that
            return Coltil.size(prf.availableClothings) + Coltil.size(prf.availableHats)
            		+ Coltil.size(prf.availableJumpModes) + Coltil.size(prf.availableAssists)
            		+ Coltil.size(prf.availableSpecialAnimals) + Coltil.size(prf.availableBirds) - 1;
        }
    }
	
	private final static class MonarchFeat extends AvatarFeat {
        protected MonarchFeat(final String code) {
            super("Monarch", code, "Wear Royal clothing and a Crown", 5000);
        }
        
        @Override
        public final boolean isMet(final Avatar avt) {
            return Chartil.startsWith(Player.getName(avt.clothing), "Royal ") && Avatar.HAT_CROWN.equals(Player.getName(avt.hat));
        }
    }
	
	private final static class UnicornFeat extends AvatarFeat {
        protected UnicornFeat(final String code) {
            super("Unicorn", code, "Add an Alicorn to a Horse", 625);
        }
        
        @Override
        public final boolean isMet(final Avatar avt) {
            return "Horse".equals(avt.anm) && Avatar.HAT_ALICORN.equals(Player.getName(avt.hat));
        }
    }
	
	private final static class PegasusFeat extends AvatarFeat {
        protected PegasusFeat(final String code) {
            super("Pegasus", code, "Add Wings to a Horse", 1250);
        }
        
        @Override
        public final boolean isMet(final Avatar avt) {
            return "Horse".equals(avt.anm) && avt.jumpMode == Player.JUMP_FLY;
        }
    }
	
	private final static class NoEnemyFeat extends CurrentFeat {
	    protected NoEnemyFeat(final String code) {
            super("Dove", code, "Finish a Level without defeating any Enemies", 200);
        }
        
        @Override
        public final boolean isMet(final Player player) {
            return Level.numEnemies > 0 && player.levelDefeatedEnemies == 0;
        }
	}
	
	private final static class AllEnemyFeat extends CurrentFeat {
        protected AllEnemyFeat(final String code) {
            super("Hawk", code, "Defeat all Enemies in a Level", 300);
        }
        
        @Override
        public final boolean isMet(final Player player) {
            return Level.numEnemies > 0 && player.levelDefeatedEnemies == Level.numEnemies;
        }
    }
	
	private final static class NoGemsFeat extends CurrentFeat {
        protected NoGemsFeat(final String code) {
            super("Bear Market", code, "Finish a Level with no Gems", 250);
        }
        
        @Override
        public final boolean isMet(final Player player) {
            return Level.numGems > 0 && player.levelEndGems == 0;
        }
    }
	
	private final static class AllGemsFeat extends CurrentFeat {
        protected AllGemsFeat(final String code) {
            super("Bull Market", code, "Collect all Gems in a Level", 500);
        }
        
        @Override
        public final boolean isMet(final Player player) {
            return Level.numGems > 0 && player.levelFloatingGems == Level.numGems;
        }
    }
	
	private final static class NoBirdGemsFeat extends CurrentFeat {
        protected NoBirdGemsFeat(final String code) {
            super("Empty Nest", code, "Don't let your Bird get any Gems in a Level", 2500);
        }
        
        @Override
        public final boolean isMet(final Player player) {
            return Level.numGems > 0 && player.pc.bird != null && player.levelBirdGems == 0;
        }
    }
	
	private final static class AllBrokenFeat extends CurrentFeat {
        protected AllBrokenFeat(final String code) {
            super("Juggernaut", code, "Break all Blocks in a Level", 150);
        }
        
        @Override
        public final boolean isMet(final Player player) {
            return Level.numBreakable > 0 && player.levelBrokenBlocks == Level.numBreakable;
        }
    }
	
	private final static class HardFeat extends CurrentFeat {
	    protected HardFeat(final String code) {
	        super("Lionheart", code, "Play a Level on highest difficulty", 2000);
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
