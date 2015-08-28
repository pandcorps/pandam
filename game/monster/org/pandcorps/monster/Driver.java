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
    private final static String SPECIAL_TRADER = Specialty.Trader.toString();
    private final static String SPECIAL_BREEDER = Specialty.Breeder.toString();
    private final static String SPECIAL_LAB = Specialty.Lab.toString();
    private final static String SPECIAL_LIBRARY = Specialty.Library.toString();
    
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
	    try {
	        loop();
	    } catch (final Exception e) {
	        handler.fatal(e);
	    }
	}
	
	private void loop() {
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
	    while (stack.size() > 0) {
	        stack.pop();
	    }
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
            final List<Option> options = new ArrayList<Option>();
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
	    private final boolean skipCurrent;
	    
        public TravelOption(final List<Location> available, final boolean skipCurrent) {
            //TODO Standardize location as argument or from state
            super(new Label(state.getLocation().getName() + " - Travel"));
            this.available = available;
            this.skipCurrent = skipCurrent;
        }

        @Override
        public List<Option> menu() {
            final List<Option> options = new ArrayList<Option>(available.size());
            addOptions(options);
            return options;
        }
        
        public void addOptions(final List<Option> options) {
            final Location curr = state.getLocation();
            for (final Location l : available) {
                if (skipCurrent && curr.equals(l)) {
                    continue;
                } else if (!l.isCity()) {
                    continue;
                }
                final List<Entity> requirements = new ArrayList<Entity>();
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
	
	public class WorldOption extends RunOption {
	    public WorldOption() {
	        super(new Label("World"));
	    }
	    
        @Override
        protected List<Option> menu() {
            final List<Option> options = new ArrayList<Option>();
            for (final Location loc : Location.getLocations()) {
                new WildOption(loc, loc.getNormal()).addMenuOption(options, "Wild");
                new FishOption(loc, loc.getFish()).addMenuOption(options, Specialty.Fish.toString());
                new TrackOption(loc).addMenuOption(options);
                addSpecialOptions(options, loc);
            }
            new TravelOption(Location.getLocations(), false).addOptions(options); // For walking to cities, not menu fast travel
            addMenuOption(options, false); // Includes fast travel option
            return options;
        }
	}

	public class LocationOption extends RunOption {
		protected final Location location;

		public LocationOption(final Location location) {
			super(location);
			this.location = location;
		}

		@Override
		public List<Option> menu() {
		    final List<Option> options = new ArrayList<Option>();
			//options.add(new Option(new Label("Battle")) {@Override public void run() {stack.push(new BattleOption(location));}});
			//options.add(new Option(new Label("Catch")) {@Override public void run() {stack.push(new CatchOption(location));}});
			//options.add(new BattleOption(location)); // Add directly, or push to stack in above commented-out code?
			//options.add(new CatchOption(location));
			//boolean fish = false;
			//final List<Species> wild = location.getNormal();
			//final List<Species> fish = location.getFish();
			//boolean move = false;
			//final LinkedHashSet<Item> specials = new LinkedHashSet<Item>();
			//final Map<Item, ArrayList<Species>> specials = location.getSpecials();
			//new WildOption(new Label(location.getName() + " - " + "Wild"), wild).addMenuOption(options, "Wild");
            //options.add(new MenuOption("Catch", new CatchOption(location, wild)));
			//new FishOption(location, fish).addMenuOption(options, Specialty.Fish.toString());
			/*if (move) {
                options.add(new MenuOption("Special", null));
            }*/
			//addSpecialOptions(options, location);
			addMenuOption(options, true);
			options.add(new MenuOption(Data.getMorph(), new MorphOption()));
			//new TrackOption(location).addMenuOption(options);
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
                final String trainersName = location.getTrainersName();
                final String wrapper, wrapped;
                if (trainersName == null) {
                    wrapper = Data.getTrainers();
                    wrapped = location.getName() + " - " + wrapper;
                } else {
                    wrapper = wrapped = trainersName;
                }
                options.add(new MenuOption(wrapper, new TrainedOption(new Label(wrapped), location.getTrained())));
            }
            final List<Item> store = location.getStore();
            if (store.size() > 0) {
                options.add(new MenuOption(Data.getStore(), new StoreOption(location)));
            }
            //addTravelOption(options);
            options.add(new MenuOption("World", new WorldOption()));
            return options;
		}
	}
	
	private void addSpecialOptions(final List<Option> options, final Location location) {
	    for (final Entry<Item, ArrayList<Species>> special : location.getSpecials().entrySet()) {
            final Item item = special.getKey();
            if (canDisplay(item)) {
                if (item.isTechnique()) {
                    options.add(new MenuOption(item.getName(), new SpecialOption(location, item, special.getValue()), state.choose(item), item)); // See TravelOption
                } else {
                    options.add(new MenuOption(item.getName(), new SpecialOption(location, item, special.getValue()), item)); // See TravelOption
                }
            }
        }
	}
	
	public void addTravelOption(final List<Option> options, final boolean skipCurrentLocation) {
	    final List<Location> available = Location.getVisited(); // Location.getAvailable();
	    final int min = skipCurrentLocation ? 1 : 0;
        if (available.size() > min) {
            options.add(new MenuOption("Travel", new TravelOption(available, skipCurrentLocation)));
        }
	}
	
	public void addMenuOption(final List<Option> options, final boolean skipCurrentLocation) {
	    options.add(new MenuOption("Menu", new MainMenuOption(skipCurrentLocation)));
	}
	
	public class MainMenuOption extends RunOption {
	    private final boolean skipCurrentLocation;
	    
        protected MainMenuOption(final boolean skipCurrentLocation) {
            super(new Label("Menu"));
            this.skipCurrentLocation = skipCurrentLocation;
        }

        @Override
        protected List<Option> menu() {
            final List<Option> options = new ArrayList<Option>();
            //options.add(new MenuOption(Data.getMorph(), new MorphOption()));
            options.add(new MenuOption(Data.getInventory(), new InventoryOption()));
            options.add(new MenuOption(Data.getDatabase(), new DatabaseOption()));
            addTravelOption(options, skipCurrentLocation);
            //options.add(new ExitOption());
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
	
	private class ValidatorMenuOption extends MenuOption {
	    public ValidatorMenuOption(final String name, final Option option) {
	        super(name, option);
        }
        
        public ValidatorMenuOption(final String name, final Option option, final Entity... requirements) {
            super(name, option, requirements);
        }
        
        @Override
        public boolean isPossible() {
            return super.isPossible() && Option.isPossible(option);
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
        /*
        First iteration tries to be helfpul, looking for a Species not on the player's team.
        We have a 75% chance of using that iteration and a 25% chance of skipping it to be totally random.
        */
        final int start = (Mathtil.randi(0, 10000) < 7500) ? 0 : 1;
        for (int i = start; i < 2; i++) {
            for (final Species species : Coltil.unnull(opponents)) {
                final Species chosen = opt.choose(species);
                if (chosen == null) {
                    continue;
                } else if (i == 0 && state.hasTeam(species)) {
                    continue;
                }
                final Option option = opt.createOption(chosen, species, species.getSpecial());
                if (option.isPossible()) {
                    possibleOptions.add(new SpeciesOption(option, species));
                }
            }
            if (possibleOptions.size() > 0) {
                break;
            }
        }
        return possibleOptions.size() > 0 ? Mathtil.rand(possibleOptions) : null;
    }
	
	protected abstract class OpponentOption extends RunOption {
		protected final Collection<Species> opponents;
		//private final Special special;

		/*public OpponentOption(final Label label, final List<Species> opponents) {
		    this(label, opponents, null);
		}*/
		
		protected OpponentOption(final Label label, final Collection<Species> opponents /*, final Special special*/) {
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
		    final Collection<Species> opponents = getOpponents();
			final List<Option> options = new ArrayList<Option>(opponents.size());
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
		
		protected Collection<Species> getOpponents() {
		    return opponents;
		}
	}
	
	protected class AwardOption extends RunOption {
	    private final Task task;
	    
        protected AwardOption(final Task task) {
            super(new Label("You got:"));
            this.task = task;
        }

        @Override
        protected List<Option> menu() {
            final List<Entity> awarded = task.getAwarded();
            final List<Option> options = new ArrayList<Option>(awarded.size());
            for (final Entity award : awarded) {
                final BackOption opt;
                if (award instanceof Amount) {
                    final Amount amount = (Amount) award;
                    opt = new BackOption(amount.getUnits());
                    opt.setInfo(String.valueOf(amount.getValue()));
                } else {
                    opt = new BackOption(award.getName());
                }
                options.add(opt);
            }
            return options;
        }
	}
	
	protected abstract class WrapperOption extends RunOption {
	    protected Option option = null;
	    
	    protected WrapperOption(final Label label) {
            super(label);
	    }
	    
	    @Override
        public boolean isPossible() {
            return super.isPossible() && Option.isPossible(option);
        }
	}
	
	protected abstract class BattleOption extends WrapperOption {
	    protected final Location location;
	    protected final List<Species> opponents;
        private final boolean catchable;
        protected Species opponent = null;
        protected Species chosen = null;
        
        protected BattleOption(final Label label, final Location loc, final List<Species> opponents, final boolean catchable) {
            super(label);
            location = loc;
            this.opponents = opponents;
            this.catchable = catchable;
            pickNextOpponent();
            setAutoBackEnabled(true);
        }
        
        @Override
        public boolean isPossible() {
            return (getRandomOption(this, opponents) != null) && super.isPossible();
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
            if (option == null) {
                return options;
            }
            //options.add(createOption(chosen, opponent, opponent.getSpecial()));
            options.add(option);
            if (catchable && !state.hasTeam(opponent)) {
                options.add(Task.createCatchTask(chosen, opponent));
            }
            return options;
        }
        
        protected Species choose(final Species opponent) {
            return state.choose(opponent);
        }
        
        protected abstract Option createOption(final Species chosen, final Species opponent, final Special special);
    }

	protected class WildOption extends BattleOption {
		public WildOption(final Location loc, final List<Species> opponents) {
			super(new Label(loc.getName() + " - " + "Wild"), loc, opponents, true);
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
            super(label, null, opponents, false);
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
	
	protected class FishOption extends BattleOption {
	    public FishOption(final Location location, final List<Species> wild) {
            super(new Label(location.getName() + " - Fishing"), location, wild, true /*, new Special(Specialty.Fish, null)*/ );
        }

        @Override
        protected Option createOption(final Species chosen, final Species opponent, final Special special) {
            return Task.createFishTask(chosen, opponent, special.getRequirement());
        }
	}
	
	//private class SpecialOption extends OpponentOption {
	protected class SpecialOption extends BattleOption { // Makes sense for surf, maybe not all special options
	    protected final Item requirement;
	    
        public SpecialOption(final Location location, final Item requirement, final List<Species> wild) {
            super(new Label(location.getName() + " - " + requirement), location, wild, true /*, new Special(Specialty.Move, requirement)*/ );
            this.requirement = requirement;
        }

        @Override
        protected Option createOption(final Species chosen, final Species opponent, final Special special) {
            //return Task.createCatchTask(chosen, opponent); // Automatically available for BattleOption
            return Task.createWildTask(chosen, opponent); // Need a separate option? This might be ok for surf
        }
    }
	
	protected class TrackOption extends BattleOption {
	    protected final Location loc;
	    
        public TrackOption(final Location loc) {
            super(new Label("Track"), loc, getTrackable(loc), true);
            this.loc = loc;
        }

        @Override
        protected Option createOption(final Species chosen, final Species opponent, final Special special) {
            return Task.createTrackTask(chosen, opponent);
        }
        
        protected void addMenuOption(final List<Option> options) {
            if (Coltil.isEmpty(opponents)) {
                return;
            } else if (!state.hasInventory(track)) {
                return;
            }
            final Species chosen = state.chooseIfNecessary(track);
            if (chosen == null || !state.hasTeam(chosen)) {
                return;
            }
            options.add(new ValidatorMenuOption(track.getName(), this, chosen, track));
        }
    }
	
	private final List<Species> getTrackable(final Location loc) {
        final List<Species> list = new ArrayList<Species>();
        for (final Species s : loc.getTrackable()) {
            if (isTrackable(s)) {
                list.add(s);
            }
        }
        return list;
    }
	
	protected final static boolean isTrackable(final Species s) {
	    //TODO Option to display creatures currently in team
	    return state.hasSeen(s) && !state.hasTeam(s) && !(s.isUnique() && state.hasOwned(s));
	}
	
	protected class MorphOption extends OpponentOption {
        public MorphOption() {
            super(new Label(Data.getMorph()), getMorphable());
        }
        
        @Override
        protected Option createOption(final Species chosen, final Species opponent, final Special special) {
            final Species precursor = opponent.getPrecursor();
            if (!canSplit(precursor)) {
                final Collection<Species> morphs = precursor.getMorphs();
                if (Coltil.size(morphs) > 1) {
                    return new MenuOption(precursor.getName(), new MorphChoiceOption(morphs));
                }
            }
            return createMorphDetailOption(precursor, opponent);
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
	
	private final DetailOption createMorphDetailOption(final Entity goal, final Species opponent) {
	    return new DetailOption(new RemoveTask(goal, getMorphRequired(opponent), getMorphAwarded(opponent)));
	}
	
	protected class MorphChoiceOption extends OpponentOption {
	    public MorphChoiceOption(final Collection<Species> morphs) {
	        super(morphs.iterator().next().getPrecursor(), morphs);
	        setAutoBackEnabled(true);
	        setAutoBackPlusButton(true);
	    }

        @Override
        protected Option createOption(final Species chosen, final Species opponent, final Special special) {
            return createMorphDetailOption(opponent, opponent);
        }
        
        @Override
        protected Species choose(final Species opponent) {
            return null;
        }
	}
	
	protected class DetailOption extends WrapperOption {
	    public DetailOption(final Task task) {
	        super(task.getGoal());
	        option = task;
	        setAutoBackEnabled(true);
	    }

        @Override
        protected List<Option> menu() {
            stack.push(this);
            final List<Option> options = new ArrayList<Option>();
            options.add(option);
            options.add(new BackOption("Cancel"));
            return options;
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
	
	protected class RemoveTask extends Task {
        public RemoveTask(final Entity goal, final Collection<? extends Entity> required, final Collection<? extends Entity> awarded) {
            super(goal, required, awarded);
            pushAwardOptionAlways = true;
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
    
    private final List<Species> getMorphable() {
        final Set<Species> precursors = new HashSet<Species>();
        final List<Species> list = new ArrayList<Species>();
        for (final Species s : state.getPreferences()) {
            //TODO Option to display creatures currently in team
            final Species precursor = s.getPrecursor();
            if (precursor == null || !state.hasTeam(precursor) || precursors.contains(precursor)) {
                continue;
            } else if ((!canSplit(precursor) || precursor.getMorphs().iterator().next().equals(s)) && s.getCatalyst() != null && !state.hasTeam(s)) {
                precursors.add(precursor);
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
        final List<Entity> required = new ArrayList<Entity>(1 + morphs.size());
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
        final Special special = s.getSpecial();
        return (special != null) && special.getSpecialty() == Specialty.Split;
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
            if (give.isEmpty()) {
                final ListIterator<Species> iter = team.listIterator(team.size());
                while (iter.hasPrevious()) {
                    final Species pref = iter.previous();
                    if (isTradeable(pref)) {
                        give.add(pref);
                        if (give.size() >= 5) {
                            break;
                        }
                    }
                }
            }
            final List<Option> options = new ArrayList<Option>(give.size());
            for (final Species g : give) {
                options.add(new OfferOption(g));
            }
            return options;
        }
    }
    
    public class OfferOption extends RunOption {
        private final Species offered;
        
        protected OfferOption(final Species offered) {
            super(offered);
            this.offered = offered;
        }

        @Override
        protected List<Option> menu() {
            final List<Species> trader = state.getTrader();
            /*for (final Species s : trader) {
                if (Special.getSpecialty(s.getSpecial()) == Specialty.Trader ||
                        s.getCatalyst() instanceof Entity.Trade) { //TODO singleton Trade instance?
                    receive.add(s);
                } else {
                    other.add(s);
                }
            }*/
            //TODO Option to disable filtering
            final List<Option> options = new ArrayList<Option>(trader.size());
            for (final Species r : trader) {
                options.add(new DetailOption(new TradeTask(offered, r)));
            }
            return options;
        }
    }
    
    private final boolean isTradeable(final Species s) {
        if (s.isUnique()) {
            return false;
        }
        final Entity cat = s.getCatalyst();
        return !(cat instanceof Item && ((Item) cat).isUnique());
    }
    
    public class TradeTask extends Task {
        public TradeTask(final Species give, final Species receive) {
            super(receive, Arrays.asList(give), Arrays.asList(receive));
            setAutoBackEnabled(true);
        }
        
        @Override
        public void run() {
            state.trade((Species) required.get(0), (Species) awarded.get(0));
            pushAwardForce();
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
            return new DetailOption(new BreedTask(chosen, Arrays.asList(chosen, Data.getShapeShifter()), Arrays.asList(opponent)));
            //return new BreederTask(opponent);
        }
        
        @Override
        protected Species choose(final Species opponent) {
            final boolean canSire = opponent.canSire();
            if (canSire && state.hasTeam(opponent)) {
                return opponent;
            }
            for (final Species morph : opponent.getAllMorphs()) {
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
    
    protected final static class BreedTask extends Task {
        public BreedTask(final Label goal, final Collection<? extends Entity> required, final Collection<? extends Entity> awarded) {
            super(goal, required, awarded);
        }
    }
    
    private final List<Species> getBreedable() {
        final List<Species> list = new ArrayList<Species>();
        for (final Species s : Species.getBreedable()) {
            //TODO Option to display creatures currently in team
            if (state.hasTeam(s)) {
                continue;
            } else if (s.getMorphs().size() == 0) {
                continue;
            }
            boolean hasParent = false;
            for (final Species p : s.getAllMorphs()) {
                if (state.hasTeam(p)) {
                    hasParent = true;
                    break;
                }
            }
            if (hasParent) {
                list.add(s);
            }
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
            final Item req = special.getRequirement();
            return new Task(req, Arrays.asList(req), Arrays.asList(opponent));
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
    
    private final List<Species> getLab() {
        final List<Species> list = new ArrayList<Species>();
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
            final Species species = (Species) goal;
            state.see(species);
            stack.push(new ViewOption(species));
        }
    }
    
    protected class ViewOption extends RunOption {
        protected final Species species;
        
        protected ViewOption(final Species species) {
            super(new Label("Library: " + species));
            this.species = species;
        }

        @Override
        protected List<Option> menu() {
            final List<Option> options = new ArrayList<Option>();
            return options;
        }
    }
    
    private final List<Species> getLibrary() {
        final List<Species> list = new ArrayList<Species>();
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
            final List<Option> options = new ArrayList<Option>(2);
            options.add(new MenuOption("Buy", new BuyOption(location)));
            options.add(new MenuOption("Sell", new SellOption()));
            final Option moneyOption = new NonOption(Data.getMoney());
            moneyOption.setInfo(String.valueOf(state.getMoney()));
            options.add(moneyOption);
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
            final List<Option> options = new ArrayList<Option>(store.size());
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
            final Map<Item, Long> inventory = state.getInventoryMap();
            final List<Option> options = new ArrayList<Option>(inventory.size());
            for (final Entry<Item, Long> entry : inventory.entrySet()) {
                final Item product = entry.getKey();
                final long amount = entry.getValue().longValue();
                if (amount > 0 && product.getPrice() > 0 && !product.isUnique()) {
                    options.add(Task.createSellTask(product, amount));
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
            final Set<Entry<Item, Long>> inventory = state.getInventoryMap().entrySet();
            final List<Option> options = new ArrayList<Option>(inventory.size());
            addItem(options, new Label(Data.getMoney()), state.getMoney());
            addItem(options, new Label(Data.getExperience()), state.getExperience());
            for (final Entry<Item, Long> entry : inventory) {
                final Item product = entry.getKey();
                // product.isUnique() // Might display this and other information
                // If we had item descriptions, we could make an option to display them
                // Maybe should allow multiple goals for multiple data fields
                final long amount = entry.getValue().longValue();
                if (amount <= 0) {
                    continue;
                }
                addItem(options, product, amount);
            }
            return options;
        }
        
        private final void addItem(final List<Option> options, final Label product, final long amount) {
            final BackOption opt = new BackOption(product);
            //Task.setItemInfo(opt, entry.getValue().longValue(), product.getPrice());
            opt.setInfo(String.valueOf(amount));
            options.add(opt);
        }
    }
    
    public class DatabaseOption extends RunOption {
        public DatabaseOption() {
            super(new Label(Data.getDatabase()));
        }

        @Override
        public List<Option> menu() {
            final Collection<Species> prefs = state.getPreferences();
            final List<Option> options = new ArrayList<Option>(prefs.size());
            final Set<Species> team = new HashSet<Species>(state.getTeam());
            for (final Species s : prefs) {
                if (!team.contains(s)) { //TODO Option to show owned and seen instead of team
                    continue;
                }
                //options.add(new BackOption(s));
                options.add(new PreferenceOption(s));
            }
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
    
    private class PreferenceOption extends Option {
        protected PreferenceOption(final Label goal) {
            super(goal);
        }

        @Override
        public void run() {
            final Species goal = (Species) getGoal();
            // If first, then move to last; otherwise move to first
            state.setExtremePreference(goal, goal != state.getFavoritePreference());
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
            final List<Option> options = new ArrayList<Option>();
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
        final List<Option> options = new ArrayList<Option>(baseOptions.size() + 1);
        options.addAll(baseOptions);
        if (stack.size() > 1 && caller.isBackButtonNeeded()) {
            options.add(new BackOption());
        }
        //} else {
        if (caller instanceof MainMenuOption) {
            options.add(new ExitOption());
        }
        return handler.handle(caller, label, options);
    }
    
    public class BackOption extends Option {
        public BackOption() {
            this("Back");
        }
        
        private BackOption(final String text) {
            this(new Label(text));
        }
        
        private BackOption(final Label label) {
            super(label);
        }
            
        @Override
        public void run() {
            Option opt = null;
            do {
                stack.pop();
                opt = stack.peek();
            } while (!opt.isPossible() || opt.isAutoBackEnabled());
        }
    }
    
    public class NonOption extends BackOption {
        private NonOption(final String text) {
            super(text);
        }
        
        @Override
        public boolean isPossible() {
            return false;
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
        running = false;
        state.serialize();
        handler.exit();
    }
    
    public void fatal() {
        running = false;
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
