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

import java.io.*;
import java.util.*;
import java.util.Map.*;

import org.pandcorps.core.*;
import org.pandcorps.core.col.*;
import org.pandcorps.core.io.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.platform.Avatar.*;
import org.pandcorps.platform.Enemy.*;
import org.pandcorps.platform.Player.*;

public class Profile extends PlayerData implements Segmented, Savable {
	/*package*/ final static int MIN_FRAME_RATE = 24;
	/*package*/ final static int DEF_FRAME_RATE = 30;
	/*package*/ final static int MAX_FRAME_RATE = (DEF_FRAME_RATE * 2) - MIN_FRAME_RATE;
	/*package*/ final static int POINTS_PER_RANK = 10;
    protected final ArrayList<Avatar> avatars = new ArrayList<Avatar>();
    protected Avatar currentAvatar = null;
    private int gems = 0;
    protected final TreeSet<Integer> availableJumpModes = new TreeSet<Integer>(); // Index stored as byte in JumpMode
    protected final TreeSet<Integer> triedJumpModes = new TreeSet<Integer>();
    private final TreeSet<Integer> availableAssists = new TreeSet<Integer>();
    private final TreeSet<Integer> activeAssists = new TreeSet<Integer>();
    protected boolean autoRun = false;
    protected int frameRate = DEF_FRAME_RATE;
    protected final Statistics stats = new Statistics();
    protected final TreeSet<Integer> achievements = new TreeSet<Integer>();
    protected final Goal[] currentGoals = new Goal[Goal.NUM_ACTIVE_GOALS];
    protected int goalPoints = 0;
    protected final Set<Clothing> availableClothings = new HashSet<Clothing>();
    protected boolean consoleEnabled = false;
    protected final Set<Clothing> availableHats = new HashSet<Clothing>();
    //private String version = null; // Currently only write version to save file in case we want to read it later
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
//DEBUG
/*if (gems < 1000000) {
gems = 1000000;
}*/
    	addAll(availableJumpModes, seg, 3);
    	addAll(triedJumpModes, seg, 4);
    	addAll(availableAssists, seg, 5);
    	addAll(activeAssists, seg, 6);
    	autoRun = seg.getBoolean(7, false);
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
    	//ctrl = seg.intValue(3);
    }
    
    @Override
    public void save(final Segment seg) {
        seg.setName(PlatformGame.SEG_PRF);
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
        seg.setValue(14, PlatformGame.VERSION);
        //seg.setInt(3, ctrl);
    }
    
    protected void loadAchievements(final Segment seg) {
        addAll(achievements, seg, 0);
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
    
    private void saveAchievements(final Segment seg) {
    	seg.setName(PlatformGame.SEG_ACH);
    	addAll(seg, 0, achievements);
    }
    
    private void saveLocation(final Segment seg) {
        seg.setName(PlatformGame.SEG_LOC);
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
    	protected final CountMap<String> defeatedEnemyTypes = new CountMap<String>(PlatformGame.allEnemies.size());
    	protected int foundBlueGems = 0;
    	protected int foundCyanGems = 0;
    	protected int foundGreenGems = 0;
    	private final List<Integer> bestRuns = new ArrayList<Integer>(BEST_RUN_SIZE);
    	
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
        	defeatedEnemies = Math.max(defeatedEnemies, stompedEnemies + bumpedEnemies + hitEnemies);
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
        }
    	
		@Override
		public void save(final Segment seg) {
			seg.setName(PlatformGame.SEG_STX);
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
		}
		
		public List<String> toList() {
			final List<String> list = new ArrayList<String>(6);
			list.add("Levels defeated: " + defeatedLevels);
			list.add("Worlds defeated: " + defeatedWorlds);
			list.add("Enemies defeated: " + defeatedEnemies);
			list.add("Enemies stomped: " + stompedEnemies);
			list.add("Enemies bumped: " + bumpedEnemies);
			list.add("Enemies hit by object: " + hitEnemies);
			for (final EnemyDefinition def : PlatformGame.allEnemies) {
				list.add(def.getName() + " defeated: " + defeatedEnemyTypes.longValue(def.code));
			}
			list.add("Blocks bumped: " + bumpedBlocks);
			list.add("Blocks broken: " + brokenBlocks);
			list.add("Jumps: " + jumps);
			list.add("Bonus games played: " + playedBonuses);
			list.add("Bonus words collected: " + collectedWords);
			list.add("Blue Gems found: " + foundBlueGems);
			list.add("Cyan Gems found: " + foundCyanGems);
			list.add("Green Gems found: " + foundGreenGems);
			final int runSize = Math.min(bestRuns.size(), BEST_RUN_SIZE);
            for (int i = 0; i < runSize; i++) {
                list.add("Best run " + (i + 1) + ": " + bestRuns.get(i));
            }
			list.add("Total Gems: " + totalGems);
			list.add("Objects kicked: " + kicks);
			return list;
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
    
    private final static Assist[] ASSISTS = new Assist[] {
        new GemAssist(0, 2), // Don't change order; save file refers to these indices
        new GemAssist(1, 4),
        new GemAssist(2, 8),
        new GemAssist(3, 16), // Combine for a max 1024 multiplier
        new Assist("Invincibility", "Cannot be hurt, will not lose Gems", 4, 1000000),
        new Assist("Dragon Stomp", "Defeat armored/spiked enemies without needing a Dragon", 5, 150000)
    };
    
    protected final static Assist[] PUBLIC_ASSISTS = new Assist[] {
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
    
    public final boolean isDragonStomping() {
        return activeAssists.contains(ASSIST_DRAGON_STOMP);
    }
    
    public final int getRank() {
    	return (goalPoints / POINTS_PER_RANK) + 1;
    }
    
    public final static String getFileName(final String pname) {
    	return pname + PlatformGame.EXT_PRF;
    }
    
    public final String getFileName() {
    	return getFileName(getName());
    }
    
    public final static String getMapFileName(final String pname) {
    	return pname + PlatformGame.EXT_MAP;
    }
    
    public final String getMapFileName() {
    	return getMapFileName(getName());
    }
}
