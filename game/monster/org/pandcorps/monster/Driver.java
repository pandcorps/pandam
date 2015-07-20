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
import java.util.Map.*;

import org.pandcorps.core.*;
import org.pandcorps.monster.Special.*;

public class Driver implements Runnable {
    ///*package*/ final static String SPECIAL_TRADER = "Trader";
    //private final static String SPECIAL_BREEDER = "Breeder";
    //private final static String SPECIAL_LAB = "Lab";
    //private final static String SPECIAL_LIBRARY = "Library";
    
    //private final static String SPECIAL_SPLIT = "Split";
    
    private final static String SPECIAL_TRADER = Specialty.Trader.toString();
    private final static String SPECIAL_BREEDER = Specialty.Breeder.toString();
    private final static String SPECIAL_LAB = Specialty.Lab.toString();
    private final static String SPECIAL_LIBRARY = Specialty.Library.toString();
    
    private final static String SPECIAL_SPLIT = Specialty.Split.toString();
    
	private static State state;
	//private final Handler handler = Handler.get();
	/*package*/ Handler handler = Handler.get();
	protected final Stack<Option> stack = new Stack<Option>();
	private static Item track = null;
	private boolean running = true;
	
	/*static {
	    new Parser().run();
	    driver = new Driver();
	}*/
	
	public Driver(final State state) {
	    Driver.state = state;
	}
	
	public final static Driver get() {
        return Handler.get().getDriver();
    }

	/*public final static void main(final String[] args) {
		try {
			driver.run();
		} catch (final Throwable e) {
			e.printStackTrace();
		}
	}*/
	
	public final State getState() {
	    return state;
	}

	@Override
	public void run() {
	    state.parse();
	    final Location loc = state.getLocation();
	    if (loc == null) {
    		final Option option = new StartOption();
    		stack.push(option);
    		option.run();
    		visit(Location.getLocations().get(0));
	    } else {
	        stack.push(new LocationOption(loc));
	    }
		track = Item.getItem("Track");
		while (running) {
			step();
		}
	}

	public void step() {
		stack.peek().run();
	}
	
	private void visit(final Location loc) {
	    stack.pop();
	    state.visit(loc);
        state.setLocation(loc);
        stack.push(new LocationOption(loc));
	}
	
	/*private abstract class RunOption extends Option {
	    protected RunOption(final Label goal) {
	        super(goal);
	    }
	    
	    protected abstract Option handle();
	    
	    @Override
	    public final void run() {
	        final Option chosen = handle();
            if (running) {
                chosen.run();
            }
	    }
	}*/
	
	private abstract class RunOption extends Option {
        protected RunOption(final Label goal) {
            super(goal);
        }
        
        protected abstract List<Option> menu();
        
        @Override
        public final void run() {
            final List<Option> options = menu();
            final Option chosen = handle(this, goal, options);
            if (running) {
                chosen.run();
            }
        }
    }
	
	private class StartOption extends RunOption {
        public StartOption() {
            super(new Label("Choose"));
        }

        @Override
        public List<Option> menu() {
            final ArrayList<Option> options = new ArrayList<Option>();
            for (final Species s : Species.getStart()) {
                options.add(Task.createGiftTask(s));
            }
            return options;
            //handle(goal, options).run();
            /*final Option chosen = handle(goal, options);
            if (running) {
                chosen.run();
            }*/
            //handle(goal, options);
        }
    }
	
	public class TravelOption extends RunOption {
	    private final List<Location> available;
	    
        public TravelOption(final List<Location> available) {
            //TODO Standardize location as argument or from state
            super(new Label(state.getLocation().getName() + " - Travel"));
            this.available = available;
        }

        @Override
        public List<Option> menu() {
            final ArrayList<Option> options = new ArrayList<Option>(available.size());
            final Location curr = state.getLocation();
            for (final Location l : available) {
                if (curr.equals(l)) {
                    continue;
                }
                final ArrayList<Entity> requirements = new ArrayList<Entity>();
                final Item access = l.getAccess();
                if (access != null) {
                    final Species chosen = state.chooseIfNecessary(access); //TODO Harmonize with Location.getAvailable which uses isMove
                    if (chosen != null) {
                        requirements.add(chosen);
                    }
                    requirements.add(access);
                }
                //options.add(new Task(l, requirements, Arrays.asList(l)));
                options.add(new TravelTask(l, requirements));
            }
            return options;
        }
    }
	
	public class TravelTask extends Task {
	    public TravelTask(final Location l, final List<Entity> requirements) {
	        super(l, requirements, null /*Arrays.asList(l)*/); // award prevents travelling in Test.runSim
	    }
	    
	    @Override
        public void run() {
	        //super.run(); // state.visit
	        stack.pop(); // Pop the TravelOption; visit will pop the current location
	        visit((Location) goal);
	    }
	}

	public class LocationOption extends RunOption {
		private final Location location;

		public LocationOption(final Location location) {
			super(location);
			this.location = location;
		}

		@Override
		public List<Option> menu() {
		    final ArrayList<Option> options = new ArrayList<Option>();
			//options.add(new Option(new Label("Battle")) {@Override public void run() {stack.push(new BattleOption(location));}});
			//options.add(new Option(new Label("Catch")) {@Override public void run() {stack.push(new CatchOption(location));}});
			//options.add(new BattleOption(location)); // Add directly, or push to stack in above commented-out code?
			//options.add(new CatchOption(location));
			//boolean fish = false;
			final List<Species> wild = location.getNormal();
			final List<Species> fish = location.getFish();
			//boolean move = false;
			//final LinkedHashSet<Item> specials = new LinkedHashSet<Item>();
			final Map<Item, ArrayList<Species>> specials = location.getSpecials();
			new WildOption(new Label(location.getName() + " - " + "Wild"), wild).addMenuOption(options, "Wild");
            //options.add(new MenuOption("Catch", new CatchOption(location, wild)));
			new FishOption(location, fish).addMenuOption(options, Specialty.Fish.toString());
			/*if (move) {
                options.add(new MenuOption("Special", null));
            }*/
			for (final Entry<Item, ArrayList<Species>> special : specials.entrySet()) {
			    final Item item = special.getKey();
			    if (canDisplay(item)) {
    			    if (item.isTechnique()) {
    			        options.add(new MenuOption(item.getName(), new SpecialOption(location, item, special.getValue()), state.choose(item), item)); // See TravelOption
    			    } else {
    			        options.add(new MenuOption(item.getName(), new SpecialOption(location, item, special.getValue()), item)); // See TravelOption
    			    }
			    }
			}
			options.add(new MenuOption(Data.getMorph(), new MorphOption()));
			if (state.hasInventory(track)) {
			    options.add(new MenuOption(track.getName(), new TrackOption(), state.choose(track), track));
			}
			final String special = location.getSpecial();
			if (special != null) {
				if (SPECIAL_TRADER.equals(special)) {
				    options.add(new MenuOption(SPECIAL_TRADER, new TraderOption()));
				} else if (SPECIAL_BREEDER.equals(special)) {
				    options.add(new MenuOption(SPECIAL_BREEDER, new BreederOption()));
				} else if (special.startsWith(SPECIAL_LAB)) {
                    options.add(new MenuOption(SPECIAL_LAB, new LabOption()));
                } else if (SPECIAL_LIBRARY.equals(special)) {
				    options.add(new MenuOption(SPECIAL_LIBRARY, new LibraryOption()));
				}
			}
			final List<Species> locTrained = location.getTrained();
            if (locTrained.size() > 0) {
                options.add(new MenuOption(Data.getTrainers(), new TrainedOption(new Label(location.getName() + " - " + Data.getTrainers()), location.getTrained())));
            }
            final List<Item> store = location.getStore();
            if (store.size() > 0) {
                options.add(new MenuOption(Data.getStore(), new StoreOption(location)));
            }
            options.add(new MenuOption(Data.getInventory(), new InventoryOption()));
            options.add(new MenuOption(Data.getDatabase(), new DatabaseOption()));
            final List<Location> available = Location.getAvailable();
            if (available.size() > 1) {
                options.add(new MenuOption("Travel", new TravelOption(available)));
            }
            return options;
		}
	}

	public class MenuOption extends Option {
		protected final Option option;

		public MenuOption(final String name, final Option option) {
		    this(name, option, (Entity[]) null);
		}
		
		public MenuOption(final String name, final Option option, final Entity... requirements) {
			super(new Label(name), requirements == null ? null : Arrays.asList(requirements));
			this.option = option;
		}

		@Override
		public void run() {
			stack.push(option);
		}
	}
	
	/*protected static Species getRandomOpponent(final List<Species> opponents) {
        final List<Species> defeatableOpponents = new ArrayList<Species>();
        for (final Species opponent : opponents) {
            if (state.chooseIfNecessary(opponent) != null) {
                defeatableOpponents.add(opponent);
            }
        }
        return defeatableOpponents.size() > 0 ? Mathtil.rand(defeatableOpponents) : null;
    }*/
	
	/*protected static Task getRandomTask(final List<Task> tasks) {
        final List<Task> possibleTasks = new ArrayList<Task>();
        for (final Task task : tasks) {
            if (task.isPossible()) {
                possibleTasks.add(task);
            }
        }
        return possibleTasks.size() > 0 ? Mathtil.rand(possibleTasks) : null;
    }*/
	
	/*protected static Species getRandomOpponent(final BattleOption opt, final List<Species> opponents) {
        final List<Species> defeatableOpponents = new ArrayList<Species>();
        for (final Species opponent : opponents) {
            if (state.chooseIfNecessary(opponent) != null) {
                defeatableOpponents.add(opponent);
            }
        }
        if (defeatableOpponents.size() > 0) {
            final Species species = Mathtil.rand(defeatableOpponents);
            opt.createOption(opt.choose(species), species, species.getSpecial());
        }
        return null;
    }*/
	
	private final static class SpeciesOption {
	    private final Option option;
	    private final Species species;
	    
	    private SpeciesOption(final Option option, final Species species) {
	        this.option = option;
	        this.species = species;
	    }
	}
	
	protected static SpeciesOption getRandomOption(final BattleOption opt, final List<Species> opponents) {
        final List<SpeciesOption> possibleOptions = new ArrayList<SpeciesOption>();
        for (final Species species : opponents) {
            final Species chosen = opt.choose(species);
            if (chosen == null) {
                continue;
            }
            final Option option = opt.createOption(chosen, species, species.getSpecial());
            if (option.isPossible()) {
                possibleOptions.add(new SpeciesOption(option, species));
            }
        }
        return possibleOptions.size() > 0 ? Mathtil.rand(possibleOptions) : null;
    }
	
	protected abstract class OpponentOption extends RunOption {
		private final List<Species> opponents;
		//private final Special special;

		/*public OpponentOption(final Label label, final List<Species> opponents) {
		    this(label, opponents, null);
		}*/
		
		protected OpponentOption(final Label label, final List<Species> opponents /*, final Special special*/) {
			super(label);
			this.opponents = opponents;
            //this.special = special;
		}
		
		protected void addMenuOption(final List<Option> options, final String name) {
		    if (opponents.size() > 0) {
                options.add(new MenuOption(name, this));
            }
		}

		@Override
		public List<Option> menu() {
		    final List<Species> opponents = getOpponents();
			final ArrayList<Option> options = new ArrayList<Option>(opponents.size());
			for (final Species species : opponents) {
			    final Special speciesSpecial = species.getSpecial();
			    //if (Driver.matches(speciesSpecial, special)) { // Needed?
			        options.add(createOption(choose(species), species, speciesSpecial));
			    //}
			}
			return options;
		}
		
		protected Species choose(final Species opponent) {
		    return state.choose(opponent);
		}

		protected abstract Option createOption(final Species chosen, final Species opponent, final Special special);
		
		protected List<Species> getOpponents() {
		    return opponents;
		}
	}
	
	protected abstract class BattleOption extends RunOption {
        private final List<Species> opponents;
        private final boolean catchable;
        private Option option = null;
        protected Species opponent = null;
        protected Species chosen = null;
        
        protected BattleOption(final Label label, final List<Species> opponents, final boolean catchable) {
            super(label);
            this.opponents = opponents;
            this.catchable = catchable;
            pickNextOpponent();
        }
        
        private void pickNextOpponent() {
            final SpeciesOption speciesOption = getRandomOption(this, opponents);
            if (speciesOption == null) {
                option = null;
                opponent = null;
                chosen = null;
            } else {
                option = speciesOption.option;
                opponent = speciesOption.species;
                chosen = choose(opponent);
            }
        }
        
        protected void addMenuOption(final List<Option> options, final String name) {
            if (opponent != null) {
                options.add(new MenuOption(name, this));
            }
        }

        @Override
        public List<Option> menu() {
            pickNextOpponent();
            final List<Option> options = new ArrayList<Option>(catchable ? 2 : 1);
            //options.add(createOption(chosen, opponent, opponent.getSpecial()));
            options.add(option);
            if (catchable) {
                options.add(Task.createCatchTask(chosen, opponent));
            }
            return options;
        }
        
        protected Species choose(final Species opponent) {
            return state.choose(opponent);
        }
        
        @Override
        public boolean isPossible() {
            return super.isPossible() && option != null && option.isPossible();
        }

        protected abstract Option createOption(final Species chosen, final Species opponent, final Special special);
    }

	private class WildOption extends BattleOption {
		public WildOption(final Label label, final List<Species> opponents) {
			super(label, opponents, true);
		}

		@Override
		protected Option createOption(final Species chosen, final Species opponent, final Special special) {
			return Task.createWildTask(chosen, opponent);
		}
	}
	
	private class TrainedOption extends OpponentOption {
        public TrainedOption(final Label label, final List<Species> opponents) {
            super(label, opponents);
        }

        @Override
        protected Option createOption(final Species chosen, final Species opponent, final Special special) {
            return new TrainedBattleOption(opponent, Collections.singletonList(opponent));
        }
    }
	
	private class TrainedBattleOption extends BattleOption {
        public TrainedBattleOption(final Label label, final List<Species> opponents) {
            super(label, opponents, false);
        }

        @Override
        protected Option createOption(final Species chosen, final Species opponent, final Special special) {
            return Task.createTrainedTask(chosen, opponent);
        }
    }

	/*
	private class CatchOption extends OpponentOption {
		public CatchOption(final Location location, final List<Species> opponents) {
			super(new Label(location.getName() + " - Catch"), opponents);
		}

		@Override
		protected Option createOption(final Species chosen, final Species opponent, final Special special) {
			return Task.createCatchTask(chosen, opponent);
		}
	}
	*/
	
	private class FishOption extends BattleOption {
	    public FishOption(final Location location, final List<Species> wild) {
            super(new Label(location.getName() + " - Fishing"), wild, true /*, new Special(Specialty.Fish, null)*/ );
        }

        @Override
        protected Option createOption(final Species chosen, final Species opponent, final Special special) {
            return Task.createFishTask(chosen, opponent, special.getRequirement());
        }
	}
	
	private class SpecialOption extends OpponentOption {
        public SpecialOption(final Location location, final Item requirement, final ArrayList<Species> wild) {
            super(new Label(location.getName() + " - " + requirement), wild /*, new Special(Specialty.Move, requirement)*/ );
        }

        @Override
        protected Option createOption(final Species chosen, final Species opponent, final Special special) {
            return Task.createCatchTask(chosen, opponent);
        }
    }
	
	private class TrackOption extends OpponentOption {
        public TrackOption() {
            super(new Label("Track"), getTrackable());
        }

        @Override
        protected Option createOption(final Species chosen, final Species opponent, final Special special) {
            return Task.createCatchTask(chosen, opponent);
        }
    }
	
	private final ArrayList<Species> getTrackable() {
        final ArrayList<Species> list = new ArrayList<Species>();
        for (final Species s : state.getPreferences()) {
            //TODO Option to display creatures currently in team
            if (s.canTrack() && state.hasSeen(s) && !state.hasTeam(s)) {
                list.add(s);
            }
        }
        return list;
    }
	
	private class MorphOption extends OpponentOption {
        public MorphOption() {
            super(new Label(Data.getMorph()), getMorphable());
        }

        @Override
        protected Option createOption(final Species chosen, final Species opponent, final Special special) {
            //return new MorphTask(opponent);
            return new RemoveTask(opponent, getMorphRequired(opponent), getMorphAwarded(opponent));
        }
        
        @Override
        protected Species choose(final Species opponent) {
            return null;
        }
        
        @Override
        protected List<Species> getOpponents() {
            return getMorphable(); // Generate each time menu is displayed, because it will change as it is used
        }
    }
	
	/*private class MorphTask extends Task {
	    public MorphTask(final Species goal) {
	        super(goal, getMorphRequired(goal), getMorphAwarded(goal));
	    }
	    
	    @Override
	    public void run() {
	        super.run();
	        state.removeTeam(((Species) goal).getPrecursor());
	    }
	}*/
	
	private class RemoveTask extends Task {
        public RemoveTask(final Entity goal, final Collection<? extends Entity> required, final Collection<? extends Entity> awarded) {
            super(goal, required, awarded);
        }
        
        @Override
        public void run() {
            super.run();
            for (final Entity requirement : required) {
                if (requirement instanceof Species) {
                    state.removeTeam((Species) requirement);
                }
            }
        }
    }
    
    private final ArrayList<Species> getMorphable() {
        final ArrayList<Species> list = new ArrayList<Species>();
        for (final Species s : state.getPreferences()) {
            //TODO Option to display creatures currently in team
            final Species precursor = s.getPrecursor();
            if (precursor == null || !state.hasTeam(precursor)) {
                continue;
            } else if ((!canSplit(precursor) || precursor.getMorphs().iterator().next().equals(s)) && s.getCatalyst() != null && !state.hasTeam(s)) {
                list.add(s);
            }
        }
        return list;
    }
    
    private final List<Entity> getMorphRequired(final Species goal) {
        final Species precursor = goal.getPrecursor();
        if (!canSplit(precursor)) {
            return Arrays.asList(precursor, goal.getCatalyst());
        }
        final Collection<Species> morphs = precursor.getMorphs();
        final ArrayList<Entity> required = new ArrayList<Entity>(1 + morphs.size());
        required.add(precursor);
        for (final Species morph : morphs) {
            required.add(morph.getCatalyst());
        }
        return required;
    }
    
    private final Collection<? extends Entity> getMorphAwarded(final Species goal) {
        final Species precursor = goal.getPrecursor();
        return canSplit(precursor) ? precursor.getMorphs() : Arrays.asList(goal);
    }
    
    private final boolean canSplit(final Species s) {
        return SPECIAL_SPLIT.equals(s.getSpecial());
    }
    
    /*public class TraderOption extends OpponentOption {
        public TraderOption() {
            //TODO filter out trader's creatures if they can be caught in wild or tracked after encountered with trainer?
            // And not unique.
            // Still need to be able to trade creatures that morph upon trade.
            super(new Label(SPECIAL_TRADER), state.getTrader());
        }

        @Override
        protected Option createOption(final Species chosen, final Species opponent, final Special special) {
            //return new RemoveTask(opponent, Arrays.asList(chosen), Arrays.asList(opponent));
            return new TradeTask(chosen, opponent);
        }
        
        @Override
        protected Species choose(final Species opponent) {
            final List<Species> team = state.getTeam();
            return team.get(team.size() - 1);
            //TODO Choose species which morph upon trade
        }
    }*/
    
    public class TraderOption extends RunOption {
        public TraderOption() {
            //TODO filter out trader's creatures if they can be caught in wild or tracked after encountered with trainer?
            // And not unique.
            // Still need to be able to trade creatures that morph upon trade.
            super(new Label(SPECIAL_TRADER));
        }
        
        @Override
        public List<Option> menu() {
            final LinkedHashSet<Species> give = new LinkedHashSet<Species>();
            final List<Species> team = state.getTeam();
            for (final Species s : team) {
                final Collection<Species> morphs = s.getMorphs();
                if (morphs.size() > 0 && morphs.iterator().next().getCatalyst() instanceof Entity.Trade) {
                    give.add(s);
                }
            }
            final ArrayList<Species> receive = new ArrayList<Species>(), other = new ArrayList<Species>();
            final List<Species> trader = state.getTrader();
            for (final Species s : trader) {
                if (Special.getSpecialty(s.getSpecial()) == Specialty.Trader ||
                        s.getCatalyst() instanceof Entity.Trade) { //TODO singleton Trade instance?
                    receive.add(s);
                } else {
                    other.add(s);
                }
            }
            //TODO Option to disable filtering
            final int gsize = give.size(), rsize = receive.size();
            final int size;
            final Species last;
            if (gsize < rsize) {
                size = gsize;
                Species s = null;
                for (int i = team.size() - 1; i >= 0; i--) {
                    s = team.get(i);
                    if (s.isUnique()) {
                        continue;
                    }
                    final Entity cat = s.getCatalyst();
                    if (cat instanceof Item && ((Item) cat).isUnique()) {
                        continue;
                    }
                    break;
                }
                last = s;
            } else {
                size = rsize;
                last = null;
            }
            final ArrayList<Option> options = new ArrayList<Option>(size);
            final Iterator<Species> giter = give.iterator();
            for (int i = 0; i < size; i++) {
                final Species g = giter.hasNext() ? giter.next() : last;
                final Species r = i >= rsize ? other.get(i - rsize) : receive.get(i);
                options.add(new TradeTask(g, r));
            }
            return options;
        }
    }
    
    public class TradeTask extends Task {
        public TradeTask(final Species give, final Species receive) {
            super(receive, Arrays.asList(give), Arrays.asList(receive));
        }
        
        @Override
        public void run() {
            state.trade((Species) required.get(0), (Species) awarded.get(0));
        }
    }
    
    private class BreederOption extends OpponentOption {
        public BreederOption() {
            //This is called when option is displayed, not chosen; getBreedable() called too many times; see LibraryOption for alternative
            //super(new Label(SPECIAL_BREEDER), getBreedable());
            super(new Label(SPECIAL_BREEDER), null);
        }

        @Override
        protected Option createOption(final Species chosen, final Species opponent, final Special special) {
            return new Task(opponent, Arrays.asList(chosen, Data.getShapeShifter()), Arrays.asList(opponent));
            //return new BreederTask(opponent);
        }
        
        @Override
        protected Species choose(final Species opponent) {
            final boolean canSire = opponent.canSire();
            if (canSire && state.hasTeam(opponent)) {
                return opponent;
            }
            for (final Species morph : opponent.getMorphs()) {
                final Species chosen = choose(morph);
                if (chosen != null) {
                    return chosen;
                }
            }
            return canSire ? opponent : null;
        }
        
        @Override
        protected List<Species> getOpponents() {
            return getBreedable(); // Generate each time menu is displayed, because it will shrink as it is used
        }
    }
    
    private final ArrayList<Species> getBreedable() {
        final ArrayList<Species> list = new ArrayList<Species>();
        for (final Species s : Species.getBreedable()) {
            //TODO Option to display creatures currently in team
            if (state.hasTeam(s)) {
                continue;
            } else if (s.getMorphs().size() == 0) {
                continue;
            }
            list.add(s);
        }
        return list;
    }
    
    private class LabOption extends OpponentOption {
        public LabOption() {  
            //super(new Label(SPECIAL_LAB), getLab());
            super(new Label(SPECIAL_LAB), null);
        }

        @Override
        protected Option createOption(final Species chosen, final Species opponent, final Special special) {
            //return new Task(opponent, Arrays.asList(opponent.getCatalyst()), Arrays.asList(opponent));
            //final String method = opponent.getSpecial();
            //return new Task(opponent, Arrays.asList(Item.getItem(method.substring(method.indexOf('.') + 1))), Arrays.asList(opponent));
            return new Task(opponent, Arrays.asList(special.getRequirement()), Arrays.asList(opponent));
        }
        
        @Override
        protected Species choose(final Species opponent) {
            return null;
        }
        
        @Override
        protected List<Species> getOpponents() {
            return getLab();
        }
    }
    
    private final ArrayList<Species> getLab() {
        final ArrayList<Species> list = new ArrayList<Species>();
        for (final Species s : Species.getLab()) {
            // Usually show options even if don't have all requirements, but there should be some mystery here;
            // perhaps even the goal/award should be hidden
            //if (state.hasInventory((Item) s.getCatalyst())) {
            if (canDisplay(s.getSpecial().getRequirement())) {
                list.add(s);
            }
        }
        return list;
    }
    
    private final boolean canDisplay(final Item requirement) {
        //return !requirement.isSecret() || requirement.isAvailable(); // isAvailable calls State.get() 
        return !requirement.isSecret() || state.hasInventory(requirement); // faster to use the state field
    }
    
    private class LibraryOption extends OpponentOption {
        public LibraryOption() {
            super(new Label(SPECIAL_LIBRARY), null);
        }

        @Override
        protected Option createOption(final Species chosen, final Species opponent, final Special special) {
            return new LibraryTask(opponent);
        }
        
        @Override
        protected Species choose(final Species opponent) {
            return null;
        }
        
        @Override
        protected List<Species> getOpponents() {
            return getLibrary();
        }
    }
    
    private class LibraryTask extends Task {
        public LibraryTask(final Species goal) {
            super(goal, null, null);
        }
        
        @Override
        public void run() {
            state.see((Species) goal);
        }
    }
    
    private final ArrayList<Species> getLibrary() {
        final ArrayList<Species> list = new ArrayList<Species>();
        for (final Species s : Species.getLibrary()) {
            if (!state.hasSeen(s)) {
                list.add(s);
            }
        }
        return list;
    }
    
    private class StoreOption extends RunOption {
        private final Location location;

        public StoreOption(final Location location) {
            super(new Label(location.getName() + " - " + Data.getStore()));
            this.location = location;
        }

        @Override
        public List<Option> menu() {
            final ArrayList<Option> options = new ArrayList<Option>(2);
            options.add(new MenuOption("Buy", new BuyOption(location)));
            options.add(new MenuOption("Sell", new SellOption()));
            return options;
        }
    }
    
    private class BuyOption extends RunOption {
        private final List<Item> store;

        public BuyOption(final Location location) {
            super(new Label("Buy"));
            this.store = location.getStore();
        }

        @Override
        public List<Option> menu() {
            final ArrayList<Option> options = new ArrayList<Option>(store.size());
            for (final Item product : store) {
                options.add(Task.createBuyTask(product));
            }
            return options;
        }
    }
    
    private class SellOption extends RunOption {
        public SellOption() {
            super(new Label("Sell"));
        }

        @Override
        public List<Option> menu() {
            final List<Item> inventory = state.getInventory();
            final ArrayList<Option> options = new ArrayList<Option>(inventory.size());
            for (final Item product : inventory) {
                if (product.getPrice() > 0 && !product.isUnique()) {
                    options.add(Task.createSellTask(product));
                }
            }
            return options;
        }
    }
    
    public class InventoryOption extends RunOption {
        public InventoryOption() {
            super(new Label(Data.getInventory()));
        }

        @Override
        public List<Option> menu() {
            final List<Item> inventory = state.getInventory();
            final ArrayList<Option> options = new ArrayList<Option>(inventory.size());
            final String money = Data.getMoney();
            for (final Item product : inventory) {
                // product.isUnique() // Might display this and other information
                // If we had item descriptions, we could make an option to display them
                // Maybe should allow multiple goals for multiple data fields
                options.add(new BackOption(product.getName() + " - " + product.getPrice() + money));
            }
            return options;
        }
    }
    
    public class DatabaseOption extends RunOption {
        public DatabaseOption() {
            super(new Label(Data.getDatabase()));
        }

        @Override
        public List<Option> menu() {
            final Collection<Species> prefs = state.getPreferences();
            final ArrayList<Option> options = new ArrayList<Option>(prefs.size());
            //for (final Species s : prefs) {
                // See InventoryOption notes
                // Status - unseen/seen/owned/on team
                // Types
                // Rank
                // on select, pick another species with which to swap
                //options.add(new BackOption(s));
            //}
            return options;
        }
    }
	
	/*private class SpecialOption extends Option {
        private final Location location;

        public SpecialOption(final Location location) {
            super(new Label(location + " - Special"));
            this.location = location;
        }

        @Override
        public void run() {
            List<Species> locSpecies = location.getSpecies();
            final ArrayList<Option> options = new ArrayList<Option>();
            if (locSpecies.size() > 0) {
                final LinkedHashSet<Item> specials = new LinkedHashSet<Item>();
                for (final Species s : locSpecies) {
                    final String special = s.getSpecial();
                    if (special.startsWith("Move")) {
                        specials.add(Item.getItem(special.substring(5)));
                    }
                }
                if (move) {
                    options.add(new MenuOption("Special", null));
                }
            }
            handle(location, options).run();
        }
    }*/

	//private Option handle(Option... options)
    private Option handle(final Option caller, final Label label, final List<? extends Option> baseOptions) {
        final ArrayList<Option> options = new ArrayList<Option>(baseOptions.size() + 1);
        options.addAll(baseOptions);
        if (stack.size() > 1) {
            options.add(new BackOption());
        } else {
            options.add(new ExitOption());
        }
        return handler.handle(caller, label, options);
    }
    
    public class BackOption extends Option {
        public BackOption() {
            this("Back");
        }
        
        private BackOption(final String text) {
            super(new Label(text));
        }
            
        @Override
        public void run() {
            stack.pop();
        }
    }
    
    public class ExitOption extends Option {
        public ExitOption() {
            super(new Label("Exit"));
        }
            
        @Override
        public void run() {
            exit();
        }
    }
    
    public void exit() {
        this.running = false;
        state.serialize();
        handler.exit();
    }
	
	/*public final static boolean startsWith(final String s, final String prefix) {
	    return s == null ? prefix == null : s.startsWith(prefix);
	}
	
	public final static boolean matches(final Special special, final Special criteria) {
        if (criteria == null) {
            return special == null;
        }
        final Item req = criteria.getRequirement();
        if (req != null) {
            return req.equals(special.getRequirement());
        }
        return criteria.getSpecialty() == special.getSpecialty();
    }*/
}
