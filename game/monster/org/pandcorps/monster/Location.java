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

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.monster.Special.*;

public class Location extends Code {
	/*
	private final List<Species> species;

	public Location(final String name, final List<Species> species) {
		super(name);
		this.species = Collections.unmodifiableList(species);
	}

	public final List<Species> getSpecies() {
		return species;
	}*/
    private static List<Location> locations = null;
    private static Map<String, Location> map = null;
    
    //private final int id;
    private final List<Item> store;
    private final Item access;
    private final String special;
    private List<Species> wild = null;
    private List<Species> trained = null;
    private List<Species> normal = null;
    private List<Species> fish = null;
    private Map<Item, ArrayList<Species>> specials = null;
    private int x = -1;
    private int y = -1;
    private int wildImgX = -1;
    private int wildImgY = -1;
    
    /*package*/ Location(/*final int id,*/ final String code, final String name, final List<Item> store, final Item access, final String special,
            final int x, final int y, final int wildImgX, final int wildImgY) {
        super(code, name);
        //this.id = id;
        this.store = unmod(store);
        this.access = access;
        this.special = special;
        this.x = x;
        this.y = y;
        this.wildImgX = wildImgX;
        this.wildImgY = wildImgY;
    }
    
    /*public int getId() {
        return id;
    }*/
    
    public final List<Item> getStore() {
        return store;
    }
    
    public final Item getAccess() {
        return access;
    }
    
    public String getSpecial() {
        return special;
    }
    
    public final int getX() {
        return x;
    }
    
    public final int getY() {
        return y;
    }
    
    public final int getWildImgX() {
        return wildImgX;
    }
    
    public final int getWildImgY() {
        return wildImgY;
    }
    
    public final List<Species> getWild() {
        //initInstance(); // Called by Parser
        return wild;
    }
    
    public final List<Species> getTrained() {
        //initInstance();
        return trained;
    }
    
    public final List<Species> getNormal() {
        //initInstance();
        return normal;
    }
    
    public final List<Species> getFish() {
        //initInstance();
        return fish;
    }
    
    public final Map<Item, ArrayList<Species>> getSpecials() {
        //initInstance();
        return specials;
    }
    
    protected final void initInstance() {
        if (wild != null) {
            return;
        }
        wild = new ArrayList<Species>();
        trained = new ArrayList<Species>();
        normal = new ArrayList<Species>();
        fish = new ArrayList<Species>();
        specials = new LinkedHashMap<Item, ArrayList<Species>>();
        for (final Species s : Species.getSpecies()) {
            if (s.getWild() == this) {
                wild.add(s);
                //final String special = s.getSpecial();
                final Special spec = s.getSpecial();
                if (spec == null) {
                    normal.add(s);
                    continue;
                }
                final Specialty specialty = spec.getSpecialty();
                if (specialty == Specialty.Split) {
                    normal.add(s);
                    continue;
                }
                //if (special.startsWith("Fish")) {
                else if (specialty == Specialty.Fish) {
                    //fish = true;
                    fish.add(s);
                //} else if (special.startsWith("Move")) {
                } else if (specialty == Specialty.Condition) {
                    //move = true;
                    //specials.add(Item.getItem(special.substring(5)));
                    //final Item item = Item.getItem(special.substring(5));
                    final Item item = spec.getRequirement();
                    ArrayList<Species> list = specials.get(item);
                    if (list == null) {
                        list = new ArrayList<Species>();
                        specials.put(item, list);
                    }
                    list.add(s);
                } else {
                    throw new IllegalArgumentException(s.toString());
                }
            }
            if (s.getTrained() == this) {
                trained.add(s);
            }
        }
        wild = Collections.unmodifiableList(wild);
        
        Collections.sort(trained, new TrainedComparator());
        trained = Collections.unmodifiableList(trained);
        
        normal = Collections.unmodifiableList(normal);
        fish = Collections.unmodifiableList(fish);
        specials = Collections.unmodifiableMap(specials);
    }
    
    protected final static void init() {
        for (final Location l : locations) {
            l.initInstance();
        }
    }
    
    private final static class TrainedComparator implements Comparator<Species> {

        private final static int getValue(final Item i) {
            if (i == null) {
                return 0;
            }
            //if (i.isUnique()) {
            //}
            final int p = i.getPrice();
            return p <= 0 ? Integer.MAX_VALUE : p;
        }
        
        @Override
        public final int compare(final Species o1, final Species o2) {
            int c = Pantil.compare(o1.getRank(), o2.getRank());
            if (c != 0) {
                return c;
            }
            c = Pantil.compare(getValue(o1.getAward()), getValue(o2.getAward()));
            if (c != 0) {
                return c;
            }
            return Pantil.compare(o1.getId(), o2.getId());
        }
    }

	@Override
	public boolean isAvailable() {
		return State.get().canVisit(this);
	}

	@Override
	public void add() {
		State.get().visit(this);
	}

	public final static List<Location> getLocations() {
		return locations;
	}
	
	/*package*/ final static void setLocations(final List<Location> locations) {
	    Location.locations = unmod(locations);
	    map = map(locations);
    }
	
	/*public final static Location getLocation(final int id) {
        for (final Location l : locations) {
            if (l.id == id) {
                return l;
            }
        }
        return null;
    }*/
	
	public final static Location getLocation(final String loc) {
	    return get(map, loc);
	}
    /*
	    if (loc == null) {
	        return null;
	    }
        for (final Location l : locations) {
            if (/*Integer.toString(l.id).equals(loc) ||*/ /*l.name.equals(loc) || l.code.equals(loc)) {
                return l;
            }
        }
        throw new IllegalArgumentException(loc);
    }*/
	
	public final static List<Location> getAvailable() {
	    final List<Location> available = new ArrayList<Location>();
	    for (final Location loc : locations) {
	        final Item access = loc.access;
	        available.add(loc);
	        if (!(access == null || (access.isAvailable() && (!access.isTechnique() || State.get().choose(access).isAvailable())))) {
	            break;
	        }
	    }
	    return Collections.unmodifiableList(available);
    }
}
