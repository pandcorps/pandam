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
package org.pandcorps.monster;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import org.pandcorps.core.*;
import org.pandcorps.core.col.*;
import org.pandcorps.monster.Special.*;

// Data which must be saved
public class State {
	private final SpeciesComparator sc = new SpeciesComparator();
	
	//private final List<Species> preferences = new ArrayList<Species>(Species.getSpecies());
	private final Set<Species> preferences = new LinkedHashSet<Species>(Species.getSpecies());
	//private final List<Location> locations = new ArrayList<Location>();
	private final Set<Location> locations = new LinkedHashSet<Location>();
	//TODO LinkedHashSet to make hasTeam (contains) faster?
	private final List<Species> team = new ArrayList<Species>(); // Creatures currently in team
	private final Set<Species> owned = new HashSet<Species>(); // Creatures that have ever been in team
	private final Set<Species> seen = new HashSet<Species>(); // Creatures that have ever been seen
	private final CountMap<Item> inventory = new CountMap<Item>();
	private final Set<Item> previousInventory = new HashSet<Item>();
	private final List<Species> trader = new ArrayList<Species>();
	private Location location = null; // Current location
	private int experience = 0; // Earned in any battle; can be used to morph a Creature to a Species of a higher rank
	private int money = 0; // Earned in battles with other trainers; can be used to buy items
	
	// Fields about where to save; not fields which must be saved
	//private final String trainerName;
    private final File file;
    //private File file = new File("state.txt");
    
    public State(final String trainerName) {
        //this.trainerName = trainerName;
        this.file = new File(trainerName + ".txt");
    }

	public final static State get() {
	    return Driver.get().getState();
	}

	public boolean canVisit(final Location location) {
	    return locations.contains(location);
	}

	public void visit(final Location location) {
	    locations.add(location);
	}
	
	public List<Species> getTeam() {
        return Collections.unmodifiableList(team);
    }

	public boolean hasTeam(final Species creature) {
	    return team.contains(creature);
	}

	public void addTeam(final Species creature) {
		final int index = Collections.binarySearch(team, creature, sc);
		team.add(index < 0 ? Math.abs(index + 1) : index, creature);
		//TODO Need to add morphs to owned/seen
		owned.add(creature);
		see(creature);
	}
	
	public void removeTeam(final Species creature) {
	    remove(team, creature);
	}
	
	private static void remove(final Collection<? extends Species> team, final Species creature) {
	    if (!team.remove(creature)) {
	        throw new IllegalArgumentException(creature.toString());
	    }
	}
	
	public boolean hasSeen(final Species creature) {
	    return seen.contains(creature);
	}
	
	public void see(final Species creature) {
	    seen.add(creature);
	}

	public Collection<Item> getInventory() {
	    return Collections.unmodifiableSet(inventory.keySet());
	}
	
	public Map<Item, Long> getInventoryMap() {
        return Collections.unmodifiableMap(inventory);
    }
	
	public boolean hasInventory(final Item item) {
	    return inventory.longValue(item) > 0;
	}
	
	public long getInventoryQuantity(final Item item) {
        return inventory.longValue(item);
    }
	
	public boolean wasInventory(final Item item) {
	    return previousInventory.contains(item);
    }

	public void useInventory(final Item item) {
		if (item.isExhaustible()) {
			if (!inventory.decIfPositive(item)) {
			    throw new RuntimeException("Inventory does not contain " + item.getName());
			}
		}
	}

	public void addInventory(final Item item) {
	    inventory.inc(item);
	    previousInventory.add(item);
	}

	private final int getPreference(final Species s) {
		/*
		final int size = preferences.size();
		for (int i = 0; i < size; i++) {
			if (preferences.get(i).equals(s)) {
				return i;
			}
		}
		return size;
		*/
		int i = 0;
		for (final Species preference : preferences) {
			if (preference.equals(s)) {
			    return i;
			}
			i++;
		}
		return i;
	}
	
	public final Collection<Species> getPreferences() {
	    return Collections.unmodifiableSet(preferences);
	}
	
	public final void setFavorite(final Species fav) {
	    final Collection<Species> copy = new ArrayList<Species>(preferences);
	    preferences.clear();
	    preferences.add(fav);
	    for (final Species s : copy) {
	        preferences.add(s); // This is a set, so won't add twice
	    }
	}
	
	public final List<Species> getTrader() {
	    //synchronized(trader) { // Would only be needed for a static trader
    	    if (trader.size() == 0) {
    	        for (final Species s : Species.getSpecies()) {
    	            //if (Driver.SPECIAL_TRADER.equals(s.getSpecial())) {
    	            if (Specialty.Trader.equals(Special.getSpecialty(s.getSpecial()))) {
    	                trader.add(s);
    	            }
    	        }
    	    }
	    //}
	    return Collections.unmodifiableList(trader);
	}
	
	public final void trade(Species give, final Species receive) {
	    removeTeam(give);
	    remove(trader, receive);
	    addTeam(receive);
	    for (final Species morph : give.getMorphs()) {
	        //if (Specialty.Trader == Special.getSpecialty(morph.getSpecial())) {
	        if (morph.getCatalyst() instanceof Entity.Trade) {
	            give = morph;
	            break;
	        }
	    }
	    trader.add(give);
	}

	public Location getLocation() {
	    return location;
	}

	public void setLocation(final Location location) {
	    this.location = location;
	}

	public int getExperience() {
	    return experience;
	}

	public void addExperience(final int experience) {
	    this.experience = add(this.experience, experience, Data.getExperience());
	}

	public int getMoney() {
	    return money;
	}

	public void addMoney(final int money) {
	    this.money = add(this.money, money, Data.getMoney());
	}

	private final static int add(final int currentValue, final int addedValue, final String label) {
		final int sum = currentValue + addedValue;
		if (sum < 0) {
		    throw new RuntimeException("Cannot afford to spend " + addedValue + " " + label + "; only have " + currentValue);
		}
		return sum;
	}

	private final class SpeciesComparator implements Comparator<Species> {
		@Override
		public int compare(Species c1, Species c2) {
			final int p1 = getPreference(c1), p2 = getPreference(c2);
			if (p1 < p2) {
			    return -1;
			} else if (p1 > p2) {
			    return 1;
			}
			final int r1 = c1.getRank(), r2 = c2.getRank();
			if (r1 > r2) {
			    return -1;
			} else if (r2 > r1) {
			    return 1;
			}
			return 0;
		}
	}

	/*public void temp(final Creature opponent) {
		for (final Creature c : creatures) {
			if (c.getAdvantage(opponent) >= 1) {
			 	// return true,c;
			}
		}
	}*/

	/*public void battle(final Creature c) { // A trainer's Creature
	}*/

	private final Species chooseIntern(final Entity entity) {
		for (final Species caught : team) { // Already sorted by preferences
			if (entity.canChoose(caught)) {
			    return caught;
			}
		}
		return null;
	}

	public final Species chooseIfNecessary(final Entity entity) {
	    /*
        for each (preferred) {
            if (owned && canDefeat) {
                choose
            }
        }
        */
		Species chosen = chooseIntern(entity);
		if (chosen != null) {
		    return chosen; // Can use this choice against this opponent
		}
		
		/*
        for each (preferred) {
            if (canDefeat && (canCatch || canMorph || canTrade || etc.?)) {
                choose
            }
        }
        */
		for (final Species preferred : preferences) {
		    // canDefeat
		    if (!entity.canChoose(preferred)) {
		        continue;
		    }
		    // canCatch
			for (final Location location : locations) {
				if (location.getWild().contains(preferred)) { // Maintain a sorted "available" list as Locations are added?
					chosen = chooseIntern(preferred);
					if (chosen != null) {
					    return preferred; // Can catch this choice for later use against this opponent
					}
				}
			}
		}
		
		/*
		for (final Creature c : creatures) {
			if (have enough experience to sufficiently increase rank) {
			 	// return false,c,requiredRank;
			}
		}
		*/
		
		/*
        for each (preferred) {
            if (canDefeat) {
                choose
            }
        }
        */
		for (final Species preferred : preferences) {
		    if (entity.canChoose(preferred)) {
		        return preferred;
		    }
        }
		
		//return chosen;
		return null;
	}
	
	public final Species choose(final Entity entity) {
	    final Species chosen = chooseIfNecessary(entity);
	    if (chosen == null) {
	        throw new RuntimeException("Could not choose for " + entity);
	    }
	    return chosen;
	}
	
	private final static String SECTION_PREFERENCES = "PREF";
	private final static String SECTION_LOCATIONS = "LOCN";
	private final static String SECTION_TEAM = "TEAM";
	private final static String SECTION_OWNED = "OWND";
	private final static String SECTION_SEEN = "SEEN";
	private final static String SECTION_INVENTORY = "INVN";
	private final static String SECTION_PREVIOUS_INVENTORY = "PREV";
	private final static String SECTION_TRADER = "TRDR";
	private final static String SECTION_LOCATION = "CURR";
	private final static String SECTION_EXPERIENCE = "EXPN";
	private final static String SECTION_MONEY = "MONY";
	private final static String EOF = "EOF";
	
	public void serialize() {
	    serialize(file);
	}
	
	public void serialize(final File f) {
	    FileOutputStream out = null;
	    try {
	        out = new FileOutputStream(f);
	        serialize(out);
	    } catch (final IOException e) {
	        throw new RuntimeException(e);
	    } finally {
	        Iotil.close(out);
	    }
	}
	
	public void serialize(final OutputStream out) {
	    final PrintStream p = Iotil.getPrintStream(out);
	    //TODO File format version number?
	    //TODO Namespace labels to prevent collisions with codes?
	    serializeSpecies(p, SECTION_PREFERENCES, preferences);
	    serializeCodes(p, SECTION_LOCATIONS, locations);
	    serializeSpecies(p, SECTION_TEAM, team);
	    serializeSpecies(p, SECTION_OWNED, owned);
	    serializeSpecies(p, SECTION_SEEN, seen);
	    serializeCodeCounts(p, SECTION_INVENTORY, inventory);
	    serializeCodes(p, SECTION_PREVIOUS_INVENTORY, previousInventory);
	    serializeSpecies(p, SECTION_TRADER, trader);
	    p.println(SECTION_LOCATION);
	    p.println(location == null ? "" : location.getCode());
	    serialize(p, SECTION_EXPERIENCE, experience);
	    serialize(p, SECTION_MONEY, money);
	    p.println(EOF);
	    if (p.checkError()) {
	        throw new RuntimeException("PrintStream error");
	    }
	}
	
	public void parse() {
    	if (file.exists()) {
            parse(file);
        }
	}
	
	public void parse(final File f) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(f);
            parse(in);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } finally {
            Iotil.close(in);
        }
    }
	
	public void parse(final InputStream in) {
	    parse(new BufferedReader(new InputStreamReader(in)));
	}
	
	public void parse(final BufferedReader b) {
	    try {
    	    assertDelimiter(b, SECTION_PREFERENCES);
    	    parseSpecies(b, SECTION_LOCATIONS, preferences);
            parseLocations(b, SECTION_TEAM, locations);
    	    parseSpecies(b, SECTION_OWNED, team);
    	    parseSpecies(b, SECTION_SEEN, owned);
    	    parseSpecies(b, SECTION_INVENTORY, seen);
    	    parseItemCounts(b, SECTION_PREVIOUS_INVENTORY, inventory);
            parseItems(b, SECTION_TRADER, previousInventory);
    	    parseSpecies(b, SECTION_LOCATION, trader);
    	    location = Location.getLocation(parse(b, SECTION_EXPERIENCE));
    	    experience = parseInt(b, SECTION_MONEY);
    	    money = parseInt(b, EOF);
	    } catch (final IOException e) {
	        throw new RuntimeException(e);
	    }
	}
	
	private final static void serializeSpecies(final PrintStream p, final String label, final Collection<? extends Species> col) {
	    p.println(label);
        for (final Species elem : col) {
            p.println(elem.getId());
        }
        p.flush();
	}
	
	private final static void serializeCodes(final PrintStream p, final String label, final Collection<? extends Code> col) {
        p.println(label);
        for (final Code elem : col) {
            p.println(elem.getCode());
        }
        p.flush();
    }
	
	private final static void serializeCodeCounts(final PrintStream p, final String label, final CountMap<? extends Code> map) {
        p.println(label);
        for (final Entry<? extends Code, Long> entry : map.entrySet()) {
            p.print(entry.getKey().getCode());
            p.print('^');
            p.println(entry.getValue());
        }
        p.flush();
    }
	
	private final static void serialize(final PrintStream p, final String label, final int value) {
        p.println(label);
        p.println(value);
    }
	
	private final static void parseSpecies(final BufferedReader b, final String nextLabel, final Collection<Species> col) throws IOException {
	    col.clear();
	    String line;
	    while (!(line = b.readLine()).equals(nextLabel)) {
	        col.add(Species.getSpecies(line));
	    }
	}
	
	private final static void parseLocations(final BufferedReader b, final String nextLabel, final Collection<Location> col) throws IOException {
        col.clear();
        String line;
        while (!(line = b.readLine()).equals(nextLabel)) {
            col.add(Location.getLocation(line));
        }
    }
	
	private final static void parseItems(final BufferedReader b, final String nextLabel, final Collection<Item> col) throws IOException {
        col.clear();
        String line;
        while (!(line = b.readLine()).equals(nextLabel)) {
            col.add(Item.getItem(line));
        }
    }
	
	private final static void parseItemCounts(final BufferedReader b, final String nextLabel, final CountMap<Item> map) throws IOException {
	    map.clear();
        String line;
        while (!(line = b.readLine()).equals(nextLabel)) {
            final int d = line.indexOf('^');
            final String item = line.substring(0, d), count = line.substring(d + 1);
            map.put(Item.getItem(item), Long.valueOf(count));
        }
    }
	
	private final static int parseInt(final BufferedReader b, final String nextLabel) throws IOException {
	    return Integer.parseInt(parse(b, nextLabel));
	}
	
	private final static String parse(final BufferedReader b, final String nextLabel) throws IOException {
	    final String line = b.readLine();
	    assertDelimiter(b, nextLabel);
	    return line;
	}
	
	private final static void assertDelimiter(final BufferedReader b, final String expected) throws IOException {
	    if (!expected.equals(b.readLine())) {
            throw new RuntimeException("Expected: " + expected);
        }
	}
}
