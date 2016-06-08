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
package org.pandcorps.monster;

//import java.io.*;
//import java.nio.*;
import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.monster.Special.*;

public class Species extends Entity {
	private static List<Species> species = null;
	private static Map<String, Species> map = null;
	private static List<Species> breedable = null;
	private static List<Species> library = null;
	private static List<Species> lab = null;
	private static float[][] typeChart;

	private final int id;
	private final int rank;
	private final Type[] types;
	private final int precursorId;
	private final Entity catalyst;
	private final String wild;
	private final String trained;
	private final Item award;
	private final float height; // In meters
	private final float mass; // In kilograms
	private final boolean start;
	private final boolean sire;
	private final boolean track;
	private final boolean unique;
	//private final String special;
	private final Special special;
	private final Set<Technique> techniques = new HashSet<Technique>();
	private Collection<Species> morphs = null;

	/*package*/ Species(
	    final int id, final String name,
	    final int rank, final Type[] types,
	    final int precursorId, final Entity catalyst,
	    final String wild, final String trained, final Item award,
	    final float height, final float mass,
	    final boolean start, final boolean sire, final boolean track, final boolean unique,
	    final Special special, final List<Integer> usableIndices) {
		super(name);
		this.id = id;
		this.rank = rank;
		this.types = types;
		this.precursorId = precursorId;
		this.catalyst = catalyst;
		this.wild = wild;
		this.trained = trained;
		this.award = award;
		this.height = height;
		this.mass = mass;
		this.start = start;
		this.sire = sire;
		this.track = track;
		this.unique = unique;
		this.special = special;
		for (final Integer usableIndex : usableIndices) {
		    techniques.add(Technique.getTechnique(usableIndex.intValue() + 1));
		}
		//initInstance(); // Can't be called until all Species have been created
	}

	public final int getId() {
		return id;
	}

	public final int getRank() {
		return rank;
	}

	public final Type[] getTypes() {
		return types;
	}

	public Species getPrecursor() {
	    return Species.getSpecies(precursorId);
	}
	
	public Collection<Species> getMorphs() {
	    //initInstance();
	    return this.morphs;
	}
	
	public Collection<Species> getAllMorphs() {
	    final Collection<Species> direct = getMorphs();
	    Collection<Species> all = direct;
	    for (final Species s : Coltil.unnull(direct)) {
	        final Collection<Species> extra = s.getAllMorphs();
	        if (Coltil.isValued(extra)) {
	            if (all == direct) {
	                all = new HashSet<Species>(direct);
	            }
	            all.addAll(extra);
	        }
	    }
	    return all;
	}
	
	private final void initInstance() {
	    if (this.morphs == null) {
            final List<Species> morphs = new ArrayList<Species>();
            for (final Species morph : getSpecies()) {
                if (morph.precursorId == id) {
                    morphs.add(morph);
                }
            }
            this.morphs = Collections.unmodifiableList(morphs);
        }
	}
	
	public Entity getCatalyst() {
	    if (catalyst instanceof Experience && ((Experience) catalyst).value == 0) {
	        return new Experience(getCatalystExperience());
	    }
	    return catalyst;
	}
	
	public Location getWild() {
	    return Location.getLocation(wild);
	}
	
	public Location getTrained() {
        return Location.getLocation(trained);
    }
	
	public Item getAward() {
        return award;
    }
	
	public float getHeight() {
        return height;
    }
	
	public float getMass() {
        return mass;
    }
	
	public boolean canStart() {
	    return start;
	}
	
	public boolean canSire() {
        return sire;
    }
	
	public boolean canTrack() {
        return track;
    }
	
	public boolean isUnique() {
        return unique;
    }
	
	/*public String getSpecial() {
	    return special;
	}*/
	
	public Special getSpecial() {
        return special;
    }
	
	//TODO change arg to Technique?
	public boolean canUse(final Item item) {
	    return techniques.contains(item);
	}

	public final static int getAwardedExperience(final int rank) {
	    // 1-1; 2-2; 3-4; 4-8; 5-16
	    return (int) Math.pow(2, rank - 1); // Similar to getAdvantage/CatalystExperience
	}
	
	public int getAwardedExperience() {
		return getAwardedExperience(rank);
	}

	public int getAwardedMoney() {
		return getAwardedExperience() * 25;
	}
	
	public int getCatalystExperience() {
	    // 1-2=10; 2-3=20; 3-4=40; 4-5=80; 2-4=2-3+3-4=20+40=60; 3-5=3-4+4-5=40+80=120
	    int total = 0;
	    for (int i = getPrecursor().getRank(); i < rank; i++) {
	        total = total + (10 * getAwardedExperience(i));
	    }
	    return total;
	}

	/*private final static int getIndex(final Type[] types) {
		if (types.length > 1) {
			throw new UnsupportedOperationException("Multiple types");
		}
		return getIndex(types[0]);
	}*/

	private final static int getIndex(final Type type) {
		/*final Type[] values = Type.values();
		for (int i = values.length - 1; i >= 0; i--) {
			if (values[i] == type) {
				return i;
			}
		}*/
		final List<Type> types = Type.getTypes();
		for (int i = types.size() - 1; i >= 0; i--) {
			if (types.get(i).equals(type)) {
				return i;
			}
		}
		throw new IllegalArgumentException(type.toString());
	}

	public final float getAttackMultiplier(final Species s) {
		//return typeChart[getIndex(types)][getIndex(s.types)];
		float maxMultiplier = -1;
		for (final Type attackerType : types) {
			float multiplier = 1;
			/*
			We use the product of multipliers for all defense types.
			That's intuitive, and it's probably correct.
			There are dual type charts.
			At a glance the results look consistent.
			There are even dual type charts for unused combinations,
			suggesting a simple formula can be used to derive the values.
			Later it might be good to parse one of these
			and confirm the results.
			*/
			for (final Type defenderType : s.types) {
				multiplier *= typeChart[getIndex(attackerType)][getIndex(defenderType)];
			}
			/*
			An attack uses only one type.
			We look through all of the attacker's types
			to find the one best suited against the defender's types.
			It could be possible to teach a creature
			an attack of another type.
			A water-type creature could be taught an ice-type attack.
			The creature's defense is still just water-type.
			So we might store a creature's defense types and attack types separately.
			The current model is a simplification
			that does not allow new attacks to be taught.
			It just assumes that each creature has equal attacks for each natural type.
			*/
			if (multiplier > maxMultiplier) {
				maxMultiplier = multiplier;
			}
		}
		return maxMultiplier;
	}

	public final boolean canBattle(final Species s) {
		return canBattle(getAttackMultiplier(s), s.getAttackMultiplier(this));
	}

	private final static boolean canBattle(final float adv, final float dis) {
		return adv != 0 || dis != 0;
	}

	public final boolean canDefeat(final Species s) {
		return canBattle(s) && getAdvantage(s) >= 1;
	}

	public final float getAdvantage(final Species s) {
		final float adv = getAttackMultiplier(s), dis = s.getAttackMultiplier(this);
		if (!canBattle(adv, dis)) {
			throw new IllegalArgumentException("Neither of these species can hurt the other");
		}
		//return dis == 0 ? Float.POSITIVE_INFINITY : adv / dis; // Rank multiplier applied in Creature
		if (dis == 0) {
			return Float.POSITIVE_INFINITY;
		}
		return (float) (Math.pow(2, this.rank - s.rank) * adv / dis);
	}
	
	public boolean isStart() {
	    return getStart().contains(this);
	}

	@Override
	public boolean isAvailable() {
		return State.get().hasTeam(this);
	}

	@Override
	public void add() {
		State.get().addTeam(this);
	}

	public final static List<Species> getSpecies() {
		return species;
	}

	/*package*/ final static void setSpecies(final List<Species> species) {
	    Species.species = unmod(species);
	    map = map(species);
	    for (final Species s : species) {
	        map.put(format(s.id), s);
	    }
	    init();
	}

	public final static Species getSpecies(final int id) {
	    /*for (final Species s : species) {
	        if (s.id == id) {
	            return s;
	        }
	    }
	    return null;*/
	    return getSpecies(format(id));
	}
	
	public final static Species getSpecies(final String name) {
	    return get(map, name);
    }
	
	public final static List<Species> getStart() {
	    final List<Species> start = new ArrayList<Species>();
	    for (final Species s : species) {
	        if (s.canStart()) {
	            start.add(s);
	        }
	    }
	    return Collections.unmodifiableList(start);
	}
	
	public final static List<Species> getBreedable() {
	    //init(); // If we init on the fly, we must synchronize; so we init in setSpecies, called once after parsing
	    return breedable;
	}
	
	public final static List<Species> getLibrary() {
        //init();
        return library;
    }
	
	public final static List<Species> getLab() {
        //init();
        return lab;
    }
	
	private final static void init() {
	    if (breedable == null) {
	        for (final Species s : species) {
	            s.initInstance(); // Below code depends on morphs loaded by initInstance
	        }
	        
	        breedable = new ArrayList<Species>();
	        library = new ArrayList<Species>();
	        lab = new ArrayList<Species>();
            //for (final Species s : state.getPreferences()) { // Can't cache something based on prefs which might change
    	    for (final Species s : species) {
    	        final Specialty specialty = Special.getSpecialty(s.getSpecial());
    	        if (specialty == Specialty.Library) {
    	            library.add(s);
    	        }
    	        if (specialty == Specialty.Lab) {
    	            lab.add(s);
    	        }
                if (s.getPrecursor() != null) {
                    continue;
                }
                if (s.canSire()) {
                    breedable.add(s);
                } else {
                    final Iterator<Species> morphs = s.getMorphs().iterator();
                    if (morphs.hasNext() && morphs.next().canSire()) {
                        breedable.add(s);
                    }
                }
            }
    	    breedable = Collections.unmodifiableList(breedable);
    	    library = Collections.unmodifiableList(library);
    	    lab = Collections.unmodifiableList(lab);
	    }
	}
	
	@Override
	protected boolean canChoose(final Species s) {
        return s.canDefeat(this);
    }

	/*package*/ final static float[][] getTypeChart() {
		return typeChart;
	}

	/*package*/ final static void setTypeChart(final float[][] typeChart) {
		Species.typeChart = typeChart;
	}
}
