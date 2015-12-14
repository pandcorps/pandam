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

import java.io.*;
import java.util.*;
import java.util.Map.*;

import org.pandcorps.core.*;
import org.pandcorps.core.col.*;
import org.pandcorps.core.io.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.furguardians.Map.*;
import org.pandcorps.furguardians.Avatar.*;
import org.pandcorps.furguardians.Enemy.*;
import org.pandcorps.furguardians.Player.*;

public class Profile extends PlayerData implements Segmented, Savable {
	/*package*/ final static int MIN_FRAME_RATE = 24;
	/*package*/ final static int DEF_FRAME_RATE = 30;
	/*package*/ final static int MAX_FRAME_RATE = (DEF_FRAME_RATE * 2) - MIN_FRAME_RATE;
	/*package*/ final static int POINTS_PER_RANK = 10;
	/*package*/ final static int MIN_DAMAGE_PERCENTAGE = 10;
	/*package*/ final static int MID_DAMAGE_PERCENTAGE = 50;
	/*package*/ final static int MAX_DAMAGE_PERCENTAGE = 100;
	private final static int DEF_DAMAGE_PERCENTAGE = MIN_DAMAGE_PERCENTAGE;
    protected final ArrayList<Avatar> avatars = new ArrayList<Avatar>();
    protected Avatar currentAvatar = null;
    private int gems = 0;
    protected final TreeSet<Integer> availableJumpModes = new TreeSet<Integer>(); // Index stored as byte in JumpMode
    protected final TreeSet<Integer> triedJumpModes = new TreeSet<Integer>();
    protected final TreeSet<Integer> availableAssists = new TreeSet<Integer>();
    private final TreeSet<Integer> activeAssists = new TreeSet<Integer>();
    protected boolean autoRun = false;
    protected int frameRate = DEF_FRAME_RATE;
    protected final Statistics stats = new Statistics();
    private final TreeSet<String> achievements = new TreeSet<String>();
    protected final Goal[] currentGoals = new Goal[Goal.NUM_ACTIVE_GOALS];
    protected int goalPoints = 0;
    protected final Set<Clothing> availableClothings = new HashSet<Clothing>();
    protected boolean consoleEnabled = false;
    protected final Set<Clothing> availableHats = new HashSet<Clothing>();
    //private String version = null; // Currently only write version to save file in case we want to read it later
    protected int damagePercentage = DEF_DAMAGE_PERCENTAGE;
    protected boolean endLevelIfHurtWithNoGems = false;
    protected final Set<Animal> availableSpecialAnimals = new HashSet<Animal>();
    protected final Set<BirdKind> availableBirds = new HashSet<BirdKind>();
    protected MapTheme preferredTheme = null;
    protected int column = -1;
	protected int row = -1;
	protected final HashMap<Pair<Integer, Integer>, Boolean> open = new HashMap<Pair<Integer, Integer>, Boolean>();
    //protected int ctrl = -1; // Should store a preferred scheme for gamepads plus a preferred one for keyboards; don't know which device player will have
    
    {
        availableJumpModes.add(Integer.valueOf(0));
    }
    
    private Avatar getAvatar(String name) {
    	for (int i = 0; i < 2; i++) {
	    	for (final Avatar avatar : avatars) {
	    		if (Pantil.equals(avatar.getName(), name)) {
	    			if (Chartil.isEmpty(name)) {
	    				avatar.setName(Menu.getNewName(this));
	    			}
	    			return avatar;
	    		}
	    	}
	    	if (name == null) {
	    		name = Menu.NEW_AVATAR_NAME;
	    	} else if (Menu.NEW_AVATAR_NAME.equals(name)) {
	    		name = null;
	    	} else {
	    		break;
	    	}
    	}
    	return null;
    }
    
    public void setCurrentAvatar(final String name) {
    	final Avatar avt = getAvatar(name);
    	if (avt == null) {
    		if (currentAvatar == null) {
    			currentAvatar = Coltil.get(avatars, 0);
    		}
    		return;
    	}
    	currentAvatar = avt;
    }
    
    public void replaceAvatar(final Avatar avt) {
		avatars.set(avatars.indexOf(currentAvatar), avt);
		currentAvatar = avt;
    }
    
    public void load(final Segment seg) {
    	setName(seg.getValue(0));
    	gems = seg.intValue(2);
    	addAll(availableJumpModes, seg, 3);
    	addAll(triedJumpModes, seg, 4);
    	addAll(availableAssists, seg, 5);
    	addAll(activeAssists, seg, 6);
    	autoRun = !FurGuardiansGame.isMultiTouchSupported() || seg.getBoolean(7, false);
    	frameRate = seg.getInt(8, DEF_FRAME_RATE);
    	int i = 0;
    	for (final Field f : Coltil.unnull(seg.getRepetitions(9))) {
    		currentGoals[i] = Goal.parseField(f);
    		i++;
    	}
    	goalPoints = seg.getInt(10, 0);
    	for (final Field f : Coltil.unnull(seg.getRepetitions(11))) {
    	    availableClothings.add(Avatar.getClothing(f.getValue()));
        }
    	consoleEnabled = seg.getBoolean(12, false);
    	for (final Field f : Coltil.unnull(seg.getRepetitions(13))) {
    	    availableHats.add(Avatar.getHat(f.getValue()));
        }
    	//version = seg.getValue(14); // Currently only write version to save file in case we want to read it later
    	damagePercentage = seg.getInt(15, DEF_DAMAGE_PERCENTAGE);
    	endLevelIfHurtWithNoGems = seg.getBoolean(16, false);
    	for (final Field f : Coltil.unnull(seg.getRepetitions(17))) {
            availableSpecialAnimals.add(Avatar.getSpecialAnimal(f.getValue()));
        }
    	for (final Field f : Coltil.unnull(seg.getRepetitions(18))) {
            availableBirds.add(Avatar.getBird(f.getValue()));
        }
    	preferredTheme = Map.getThemeOrNull(seg.getValue(19));
    	//ctrl = seg.intValue(3);
    }
    
    @Override
    public void save(final Segment seg) {
        seg.setName(FurGuardiansGame.SEG_PRF);
        seg.setValue(0, getName());
        seg.setValue(1, Player.getName(currentAvatar));
        seg.setInt(2, gems);
        addAll(seg, 3, availableJumpModes);
        addAll(seg, 4, triedJumpModes);
        addAll(seg, 5, availableAssists);
        addAll(seg, 6, activeAssists);
        seg.setBoolean(7, autoRun);
        seg.setInt(8, frameRate);
        for (final Goal g : currentGoals) {
        	seg.addField(9, g == null ? null : g.toField());
        }
        seg.setInt(10, goalPoints);
        for (final Clothing c : Coltil.unnull(availableClothings)) {
            seg.addValue(11, c.res);
        }
        seg.setBoolean(12, consoleEnabled);
        for (final Clothing c : Coltil.unnull(availableHats)) {
            seg.addValue(13, c.res);
        }
        seg.setValue(14, FurGuardiansGame.VERSION);
        seg.setInt(15, damagePercentage);
        seg.setBoolean(16, endLevelIfHurtWithNoGems);
        for (final Animal a : Coltil.unnull(availableSpecialAnimals)) {
            seg.addValue(17, a.getName());
        }
        for (final BirdKind b : Coltil.unnull(availableBirds)) {
            seg.addValue(18, b.getName());
        }
        seg.setValue(19, (preferredTheme == null) ? null : preferredTheme.name);
        //seg.setInt(3, ctrl);
    }
    
    protected void loadAchievements(final Segment seg) {
        addAllStrings(achievements, seg, 0);
    }
    
    protected void loadLocation(final Segment seg) {
        column = seg.intValue(0);
        row = seg.intValue(1);
        open.clear();
        for (final Field f : Coltil.unnull(seg.getRepetitions(2))) {
        	open.put(Pair.get(f.toInteger(0), f.toInteger(1)), f.toBoolean(2));
        }
    }
    
    private void addAll(final Collection<Integer> values, final Segment seg, final int i) {
    	for (final Field f : Coltil.unnull(seg.getRepetitions(i))) {
    		values.add(f.getInteger());
    	}
    }
    
    private void addAllStrings(final Collection<String> values, final Segment seg, final int i) {
        for (final Field f : Coltil.unnull(seg.getRepetitions(i))) {
            values.add(f.getValue());
        }
    }
    
    private void saveAchievements(final Segment seg) {
    	seg.setName(FurGuardiansGame.SEG_ACH);
    	addAllStrings(seg, 0, achievements);
    }
    
    private void saveLocation(final Segment seg) {
        seg.setName(FurGuardiansGame.SEG_LOC);
        seg.setInt(0, column);
        seg.setInt(1, row);
        final ArrayList<Field> list = new ArrayList<Field>(open.size());
        for (final Entry<Pair<Integer, Integer>, Boolean> entry : open.entrySet()) {
        	final Field f = new Field();
        	final Pair<Integer, Integer> key = entry.getKey();
        	f.setInteger(0, key.get1());
        	f.setInteger(1, key.get2());
        	f.setBoolean(2, entry.getValue());
        	list.add(f);
        }
        seg.setRepetitions(2, list);
    }
    
    private void addAll(final Segment seg, final int i, final Collection<Integer> values) {
    	for (final Integer ach : Coltil.unnull(values)) {
    		seg.addInteger(i, ach);
    	}
    }
    
    private void addAllStrings(final Segment seg, final int i, final Collection<String> values) {
        for (final String ach : Coltil.unnull(values)) {
            seg.addValue(i, ach);
        }
    }
    
    @Override
    public void save(final Writer out) throws IOException {
        Segtil.saveln(this, out);
        Segtil.saveln(stats, out);
        final Segment ach = new Segment();
        saveAchievements(ach);
        ach.saveln(out);
        final Segment loc = new Segment();
        saveLocation(loc);
        loc.save(out);
        for (final Avatar avatar : avatars) {
        	Iotil.println(out);
            Segtil.save(avatar, out);
        }
    }
    
    public void save() {
        Savtil.save(this, getFileName());
    }
    
    public final static class Statistics implements Segmented {
        private final static int BEST_RUN_SIZE = 5;
    	protected int defeatedLevels = 0;
    	protected int defeatedWorlds = 0;
    	protected long defeatedEnemies = 0;
    	protected long bumpedBlocks = 0;
    	protected long brokenBlocks = 0;
    	protected long jumps = 0;
    	protected int playedBonuses = 0;
    	protected long totalGems = 0;
    	protected int collectedWords = 0;
    	protected long kicks = 0;
    	protected long stompedEnemies = 0;
    	protected long bumpedEnemies = 0;
    	protected long hitEnemies = 0;
    	protected final CountMap<String> defeatedEnemyTypes = new CountMap<String>(FurGuardiansGame.allEnemies.size());
    	protected int foundBlueGems = 0;
    	protected int foundCyanGems = 0;
    	protected int foundGreenGems = 0;
    	private final List<Integer> bestRuns = new ArrayList<Integer>(BEST_RUN_SIZE);
    	protected int playedMinecartLevels = 0;
    	protected int playedCaveLevels = 0;
    	protected int playedRockWorlds = 0;
    	protected long birdGems = 0;
    	protected int playedMatchGames = 0;
    	protected long bounces = 0;
    	protected int foundLightningOrbs = 0;
    	protected long electrocutedEnemies = 0;
    	protected int playedHiveWorlds = 0;
    	protected int foundDoubleOrbs = 0;
    	protected long doubledGems = 0;
    	protected long combos = 0;
    	protected int longestCombo = 0;
    	
    	public void load(final Segment seg, final int currGems) {
        	defeatedLevels = seg.initInt(0);
        	defeatedWorlds = seg.initInt(1);
        	defeatedEnemies = seg.initLong(2);
        	bumpedBlocks = seg.initLong(3);
        	brokenBlocks = seg.initLong(4);
        	jumps = seg.initLong(5);
        	playedBonuses = seg.initInt(6);
        	totalGems = seg.getLong(7, currGems);
        	collectedWords = seg.initInt(8);
        	kicks = seg.initLong(9);
        	stompedEnemies = seg.initLong(10);
        	bumpedEnemies = seg.initLong(11);
        	hitEnemies = seg.initLong(12);
        	defeatedEnemies = Math.max(defeatedEnemies, stompedEnemies + bumpedEnemies + hitEnemies + electrocutedEnemies);
        	defeatedEnemyTypes.clear();
        	for (final Field f : Coltil.unnull(seg.getRepetitions(13))) {
        		defeatedEnemyTypes.put(f.getValue(0), f.toLong(1));
        	}
        	foundBlueGems = seg.initInt(14);
        	foundCyanGems = seg.initInt(15);
        	foundGreenGems = seg.initInt(16);
        	bestRuns.clear();
        	for (final Field f : Coltil.unnull(seg.getRepetitions(17))) {
        	    bestRuns.add(f.getInteger());
        	    if (bestRuns.size() >= BEST_RUN_SIZE) {
        	        break;
        	    }
        	}
        	playedMinecartLevels = seg.initInt(18);
        	playedCaveLevels = seg.initInt(19);
        	playedRockWorlds = seg.initInt(20);
        	birdGems = seg.initLong(21);
        	playedMatchGames = seg.initInt(22);
        	bounces = seg.initLong(23);
        	foundLightningOrbs = seg.initInt(24);
        	electrocutedEnemies = seg.initLong(25);
        	playedHiveWorlds = seg.initInt(26);
        	foundDoubleOrbs = seg.initInt(27);
        	doubledGems = seg.initLong(28);
        	combos = seg.initLong(29);
        	longestCombo = seg.initInt(30);
        }
    	
		@Override
		public void save(final Segment seg) {
			seg.setName(FurGuardiansGame.SEG_STX);
	        seg.setInt(0, defeatedLevels);
	        seg.setInt(1, defeatedWorlds);
	        seg.setLong(2, defeatedEnemies);
	        seg.setLong(3, bumpedBlocks);
	        seg.setLong(4, brokenBlocks);
	        seg.setLong(5, jumps);
	        seg.setInt(6, playedBonuses);
	        seg.setLong(7, totalGems);
	        seg.setInt(8, collectedWords);
	        seg.setLong(9, kicks);
	        seg.setLong(10, stompedEnemies);
	        seg.setLong(11, bumpedEnemies);
	        seg.setLong(12, hitEnemies);
	        final List<Field> enemyReps = new ArrayList<Field>(defeatedEnemyTypes.size());
	        for (final Entry<String, Long> entry : defeatedEnemyTypes.entrySet()) {
	        	final Field f = new Field();
	        	f.setValue(0, entry.getKey());
	        	f.setLong(1, entry.getValue());
	        	enemyReps.add(f);
	        }
	        seg.setRepetitions(13, enemyReps);
	        seg.setInt(14, foundBlueGems);
	        seg.setInt(15, foundCyanGems);
	        seg.setInt(16, foundGreenGems);
	        final List<Field> runReps = new ArrayList<Field>(bestRuns.size());
	        for (final Integer run : bestRuns) {
	            final Field f = new Field();
	            f.setInteger(0, run);
	            runReps.add(f);
	        }
	        seg.setRepetitions(17, runReps);
	        seg.setInt(18, playedMinecartLevels);
	        seg.setInt(19, playedCaveLevels);
	        seg.setInt(20, playedRockWorlds);
	        seg.setLong(21, birdGems);
	        seg.setInt(22, playedMatchGames);
	        seg.setLong(23, bounces);
	        seg.setInt(24, foundLightningOrbs);
	        seg.setLong(25, electrocutedEnemies);
	        seg.setInt(26, playedHiveWorlds);
	        seg.setInt(27, foundDoubleOrbs);
	        seg.setLong(28, doubledGems);
	        seg.setLong(29, combos);
	        seg.setInt(30, longestCombo);
		}
		
		public List<String> toList(final Profile prf) {
			final List<String> list = new ArrayList<String>(6);
			list.add("Levels defeated: " + defeatedLevels);
			list.add("Worlds defeated: " + defeatedWorlds);
			list.add("Enemies defeated: " + defeatedEnemies);
			list.add("Enemies stomped: " + stompedEnemies);
			list.add("Enemies bumped: " + bumpedEnemies);
			list.add("Enemies hit by object: " + hitEnemies);
			list.add("Enemies electrocuted: " + electrocutedEnemies);
			int enemyTypesDefeated = 0;
			for (final EnemyDefinition def : FurGuardiansGame.allEnemies) {
			    final long defeatedCount = defeatedEnemyTypes.longValue(def.code);
				list.add(def.getName() + " defeated: " + defeatedCount);
				if (defeatedCount > 0) {
				    enemyTypesDefeated++;
				}
			}
			list.add("Combos: " + combos);
			list.add("Longest Combo: " + longestCombo);
			list.add("Blocks bumped: " + bumpedBlocks);
			list.add("Blocks broken: " + brokenBlocks);
			list.add("Jumps: " + jumps);
			list.add("Cave Levels played: " + playedCaveLevels);
			list.add("Minecart Levels played: " + playedMinecartLevels);
			list.add("Bonus Games played: " + playedBonuses);
			list.add("Bonus Words collected: " + collectedWords);
			list.add("Blue Gems found: " + foundBlueGems);
			list.add("Cyan Gems found: " + foundCyanGems);
			list.add("Green Gems found: " + foundGreenGems);
			list.add("Gems found by Bird: " + birdGems);
			final int runSize = Math.min(bestRuns.size(), BEST_RUN_SIZE);
            for (int i = 0; i < runSize; i++) {
                list.add("Best run " + (i + 1) + ": " + bestRuns.get(i));
            }
			list.add("Total Gems: " + totalGems);
			list.add("Lightning Orbs found: " + foundLightningOrbs);
			list.add("Double Gem Orbs found: " + foundDoubleOrbs);
			list.add("Doubled Gems found: " + doubledGems);
			list.add("Objects kicked: " + kicks);
			list.add("Bee bounces: " + bounces);
			add(list, "Shirts bought", Coltil.size(prf.availableClothings), Avatar.clothings.length);
			add(list, "Hats bought", Coltil.size(prf.availableHats), Avatar.hats.length);
			add(list, "Powers bought", (Coltil.size(prf.availableJumpModes) - 1), (JumpMode.values().length - 1));
			add(list, "Assists bought", Coltil.size(prf.availableAssists), Profile.PUBLIC_ASSISTS.length);
			add(list, "Animals bought", Coltil.size(prf.availableSpecialAnimals), Avatar.SPECIAL_ANIMALS.size());
			add(list, "Birds bought", Coltil.size(prf.availableBirds), Avatar.BIRDS.size());
			final int totalPurchases = Achievement.BuyFeat.getPurchases(prf), availablePurchases = getTotalItemsForSale();
			add(list, "Total purchases", totalPurchases, availablePurchases);
			final int availableEnemyTypes = FurGuardiansGame.allEnemies.size();
			add(list, "Total Enemy types", enemyTypesDefeated, availableEnemyTypes);
			final int totalDefeatTechniques = getDefeatTechniques(), availableDefeatTechniques = getAvailableDefeatTechniques();
			add(list, "Total defeat styles", totalDefeatTechniques, availableDefeatTechniques);
			final int totalOrbTypes = getDiscoveredOrbTypes(), availableOrbTypes = getAvailableOrbTypes();
			add(list, "Total Orb types", totalOrbTypes, availableOrbTypes);
			final int totalWorldTypes = getDefeatedWorldTypeCount(), availableWorldTypes = Map.themes.length;
            add(list, "Total World types", totalWorldTypes, availableWorldTypes);
			final int totalTrophies = prf.getAchievedSize(), availableTrophies = Achievement.ALL.length;
			add(list, "Total Trophies", totalTrophies, availableTrophies);
			final int total = totalPurchases + enemyTypesDefeated + totalDefeatTechniques + totalOrbTypes + totalWorldTypes + totalTrophies;
			final int available = availablePurchases + availableEnemyTypes + availableDefeatTechniques + availableOrbTypes + availableWorldTypes + availableTrophies;
			add(list, "Total checklist", total, available);
			return list;
		}
		
		private final static void add(final List<String> list, final String label, final int total, final int available) {
		    list.add(label + ": " + total + "/" + available);
		}
		
		private final static int getTotalItemsForSale() {
		    return Avatar.clothings.length + Avatar.hats.length + (JumpMode.values().length - 1) + Profile.PUBLIC_ASSISTS.length
		            + Avatar.SPECIAL_ANIMALS.size() + Avatar.BIRDS.size();
		}
		
		public void addRun(final int runGems) {
		    final int size = bestRuns.size();
		    int i = 0;
		    for (; i < size; i++) {
		        if (runGems > bestRuns.get(i).intValue()) {
		            break;
		        }
		    }
		    if (i < BEST_RUN_SIZE) {
		        if (size >= BEST_RUN_SIZE) {
		            bestRuns.remove(size - 1);
		        }
		        bestRuns.add(i, Integer.valueOf(runGems));
		    }
		}
		
		public final long getDefeatedCount(final EnemyDefinition def) {
			return defeatedEnemyTypes.longValue(def.code);
		}
		
		public final long getDefeatedGiants() {
			return getDefeatedCount(FurGuardiansGame.trollColossus) + getDefeatedCount(FurGuardiansGame.ogreBehemoth);
		}
		
		public final long getDefeatedWisps() {
            return getDefeatedCount(FurGuardiansGame.iceWisp) + getDefeatedCount(FurGuardiansGame.fireWisp);
        }
		
		public final long getDefeatedImps() {
            return getDefeatedCount(FurGuardiansGame.imp)
                    + getDefeatedCount(FurGuardiansGame.armoredImp)
                    + getDefeatedCount(FurGuardiansGame.spikedImp);
        }
		
		public final int getFoundOrbs() {
		    return foundLightningOrbs + foundDoubleOrbs;
		}
		
		public final int getDefeatedWorldTypeCount() {
		    int count = 0;
		    for (final MapTheme theme : Map.themes) {
		        if (theme.hasBeenDefeated(this)) {
		            count++;
		        }
		    }
		    return count;
		}
		
		public final int getDefeatTechniques() {
		    int n = 0;
		    n += toFlag(stompedEnemies);
		    n += toFlag(bumpedEnemies);
		    n += toFlag(hitEnemies);
		    n += toFlag(electrocutedEnemies);
		    return n; // Must be consistent with getAvailableDefeatTechniques()
		}
		
		public final static int getAvailableDefeatTechniques() {
		    return 4; // Must be consistent with getDefeatTechniques()
		}
		
		public final int getDiscoveredOrbTypes() {
		    int n = 0;
		    n += toFlag(foundLightningOrbs);
		    n += toFlag(foundDoubleOrbs);
		    return n; // Must be consistent with getAvailableOrbTypes()
		}
		
		public final static int getAvailableOrbTypes() {
		    return 2; // Must be consistent with getDiscoveredOrbTypes()
		}
		
		protected final static int toFlag(final long n) {
		    return (n > 0) ? 1 : 0;
		}
    }
    
    public final boolean isHardMode() {
    	return endLevelIfHurtWithNoGems && damagePercentage >= 100;
    }
    
    public final boolean isJumpModeAvailable(final byte index) {
        return availableJumpModes.contains(Integer.valueOf(index));
    }
    
    public final boolean isJumpModeTryable(final byte index) {
    	return !triedJumpModes.contains(Integer.valueOf(index));
    }
    
    public final static boolean isAvailable(final Set<Clothing> available, final Clothing c) {
        return c == null || available.contains(c);
    }
    
    private final static Integer ASSIST_INVINCIBILITY = Integer.valueOf(4);
    private final static Integer ASSIST_DRAGON_STOMP = Integer.valueOf(5);
    private final static Integer ASSIST_GEM_MAGNET = Integer.valueOf(6);
    
    private final static Assist[] ASSISTS = new Assist[] {
        new GemAssist(0, 2), // Don't change order; save file refers to these indices
        new GemAssist(1, 4),
        new GemAssist(2, 8),
        new GemAssist(3, 16), // Combine for a max 1024 multiplier
        new Assist("Invincibility", "Cannot be hurt, will not lose Gems", 4, 1000000),
        new Assist("Dragon Stomp", "Defeat armored enemies even without a Dragon", 5, 150000),
        new Assist("Gem Magnet", "Attract nearby Gems", 6, 15000)
    };
    
    protected final static Assist[] PUBLIC_ASSISTS = new Assist[] {
    	ASSISTS[6],
    	ASSISTS[5]
    };
    
    public static class Assist extends FinName {
    	private final String desc;
    	private final int index;
        private final int cost;
        
        private Assist(final String name, final String desc, final int index, final int cost) {
            super(name);
            this.desc = desc;
            this.index = index;
            this.cost = cost;
        }
        
        public final String getDescription() {
            return desc;
        }
        
        public final int getIndex() {
            return index;
        }
        
        private final Integer getKey() {
        	return Integer.valueOf(index);
        }
        
        public final int getCost() {
            return cost;
        }
    }
    
    private final static class GemAssist extends Assist {
        private final int n;
        
        private GemAssist(final int index, final int n) {
            super("Gems x " + n, "Earn " + n + "x as many Gems", index, n * 50000);
            this.n = n;
        }
    }
    
    public final static Assist getAssist(final String name) {
    	return Player.get(ASSISTS, name);
    }
    
    public final boolean isAssistAvailable(final Assist a) {
        return availableAssists.contains(a.getKey());
    }
    
    public final void addAvailableAssist(final Assist a) {
    	availableAssists.add(a.getKey());
    }
    
    public final boolean isAssistActive(final Assist a) {
        return activeAssists.contains(a.getKey());
    }
    
    public final void toggleAssist(final Assist a) {
        final Integer key = a.getKey();
        if (!activeAssists.remove(key)) {
            activeAssists.add(key);
        }
    }
    
    public final boolean isAnimalAvailable(final Animal animal) {
    	return animal == null || availableSpecialAnimals.contains(animal);
    }
    
    public final boolean isBirdAvailable(final BirdKind bird) {
        return bird == null || availableBirds.contains(bird);
    }
    
    public final boolean isAchieved(final Achievement a) {
        return achievements.contains(a.code);
    }
    
    public final int getAchievedSize() {
        return achievements.size();
    }
    
    public final void addAchievement(final Achievement a) {
        achievements.add(a.code);
    }
    
    public final int getGemMultiplier() {
        int m = 1;
        for (final Integer key : activeAssists) {
            final Assist a = ASSISTS[key.intValue()];
            if (a.getClass() == GemAssist.class) {
                m *= ((GemAssist) a).n;
            }
        }
        return m;
    }
    
    public final void addGems(final int n) {
    	gems += n;
    	stats.totalGems += n;
    }
    
    public final int getGems() {
    	return gems;
    }
    
    public final boolean spendGems(final int cost) {
    	if (gems >= cost) {
            gems -= cost;
            return true;
    	}
    	return false;
    }
    
    public final int getCurrentGoalPoints() {
    	return goalPoints % POINTS_PER_RANK;
    }
    
    public final boolean isInvincible() {
        return activeAssists.contains(ASSIST_INVINCIBILITY);
    }
    
    public final float getDamageMultiplier() {
    	return damagePercentage / 100f;
    }
    
    public final boolean isDragonStomping() {
        return activeAssists.contains(ASSIST_DRAGON_STOMP);
    }
    
    public final boolean isGemMagnetActive() {
        return activeAssists.contains(ASSIST_GEM_MAGNET);
    }
    
    public final int getRank() {
    	return (goalPoints / POINTS_PER_RANK) + 1;
    }
    
    public final static String getFileName(final String pname) {
    	return pname + FurGuardiansGame.EXT_PRF;
    }
    
    public final String getFileName() {
    	return getFileName(getName());
    }
    
    public final static String getMapFileName(final String pname) {
    	return pname + FurGuardiansGame.EXT_MAP;
    }
    
    public final String getMapFileName() {
    	return getMapFileName(getName());
    }
    
    public final static Statistics getStatistics(final Profile prf) {
        return (prf == null) ? null : prf.stats;
    }
}
