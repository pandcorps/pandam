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

import java.util.*;

public class Task extends Option {
	protected final List<Entity> awarded;
	private boolean pushAwardOptionIfNeeded = true;
	protected boolean pushAwardOptionAlways = false;

	public Task(final Label goal, final Collection<? extends Entity> required, final Collection<? extends Entity> awarded) {
		super(goal, required);
		this.awarded = init(awarded);
	}
	
	private static class FightTask extends Task {
	    protected final Species opponent;
	    
	    public FightTask(final Species opponent, final Collection<? extends Entity> required, final Collection<? extends Entity> awarded) {
	        super(new Label("Fight"), required, awarded);
	        this.opponent = opponent;
	    }
	    
	    @Override
	    public final void run() {
	        super.run();
	        State.get().see(opponent);
	    }
	}
	
	private final static class TrackTask extends FightTask {
	    public TrackTask(final Species opponent, final Collection<? extends Entity> required, final Collection<? extends Entity> awarded) {
	        super(opponent, required, awarded);
	    }
	    
	    @Override
	    public boolean isPossible() {
	        return super.isPossible() && Driver.isTrackable(opponent);
	    }
	}

	public final static Task createGiftTask(final Entity gift) {
        return new Task(gift, null, Arrays.asList(gift));
    }
	
	public final static Task createTrainedTask(final Species chosen, final Species opponent) {
		return new FightTask(opponent, Arrays.asList(chosen), award(opponent, new Experience(opponent.getAwardedExperience()), new Money(opponent.getAwardedMoney())));
	}
	
	public final static Task createWildTask(final Species chosen, final Species opponent) {
        return new FightTask(opponent, Arrays.asList(chosen), award(opponent, new Experience(opponent.getAwardedExperience())));
    }
	
	public final static Task createTrackTask(final Species chosen, final Species opponent) {
        return new TrackTask(opponent, Arrays.asList(chosen), award(opponent, new Experience(opponent.getAwardedExperience())));
    }

	public final static Task createCatchTask(final Species chosen, final Species opponent) {
	    final Container container = Container.getContainer(opponent);
		return new Task(container, Arrays.asList(chosen, container), award(opponent, opponent));
	}

	//TODO Option to fight when fishing/surfing instead of just catch?
	public final static Task createFishTask(final Species chosen, final Species opponent, final Item rod) {
        //return new Task(new Label("Fight"), Arrays.asList(chosen, Container.getContainer(opponent), rod), award(opponent, opponent));
	    return new FightTask(opponent, Arrays.asList(chosen, rod), award(opponent, new Experience(opponent.getAwardedExperience())));
    }
	
	private final static List<Entity> award(final Species opponent, final Entity... awarded) {
	    List<Entity> list = Arrays.asList(awarded);
	    final Item extra = opponent.getAward();
	    if (extra != null && !(extra.isUnique() && State.get().wasInventory(extra))) {
	        list = new ArrayList<Entity>(list);
	        list.add(extra);
	    }
	    return list;
	}
	
	//public final static Task createSpecialTask(final Species chosen, final Species opponent, final Item rod) {
    //    return new Task(opponent, Arrays.asList(chosen, Item.getBall(opponent), rod), Arrays.asList(opponent));
    //}

	public final static Task createBuyTask(final Item item) {
	    final int buyPrice = item.getPrice();
		final Task task = new Task(item, Arrays.asList(new Money(buyPrice)), Arrays.asList(item));
		setItemInfo(task, State.get().getInventoryQuantity(item), buyPrice);
		return task;
	}
	
	public final static Task createSellTask(final Item item, final long qty) {
	    final int sellPrice = item.getPrice() / 2;
        final Task task = new Task(item, Arrays.asList(item), Arrays.asList(new Money(sellPrice)));
        setItemInfo(task, qty, sellPrice);
        task.pushAwardOptionIfNeeded = false;
        return task;
    }
	
	protected final static void setItemInfo(final Option opt, final long qty, final int price) {
	    opt.setInfo("Qty: " + qty + ((price > 0) ? ("; " + price) : ""));
	}

	@Override
	public List<Entity> getAwarded() {
		return awarded;
	}

	@Override
	public void run() {
		if (!isPossible()) {
			throw new RuntimeException("Impossible");
		}
		for (final Entity requirement : required) {
			requirement.use();
		}
		for (final Entity award : awarded) {
			award.add();
		}
		if (goal instanceof Species) {
		    State.get().see((Species) goal);
		}
		pushAwardIfNeeded();
	}
	
	protected void pushAwardIfNeeded() {
	    final int awardedSize = awarded.size();
	    if (awardedSize > 0) {
	        State.get().serialize();
	    }
		if (pushAwardOptionIfNeeded) {
	        if (pushAwardOptionAlways || (awardedSize > 1) || ((awardedSize == 1) && (awarded.get(0) != goal))) {
	            pushAwardForce();
	        }
		}
	}
	
	protected void pushAwardForce() {
	    final Driver driver = Driver.get();
        driver.stack.push(driver.new AwardOption(this));
	}

	/*private final int requiredExperience; // To morph a creature to a species of higher rank
	private final int requiredMoney; // To buy an item
	private final List<Item> requiredItems; // Perhaps a capture ball and a fishing pole
	private final Species requiredSpecies;
	private final int awardedExperience;
	private final int awardedMoney;
	private final Item awardedItem;
	private final Species awardedSpecies;

	public Task(final Entity goal, final List<Item> requiredItems, final Species requiredSpecies,
		final int awardedExperience, final int awardedMoney, final Item awardedItem, final Species awardedSpecies) {
		this.goal = goal;
		this.requiredItems = requiredItems;
		this.requiredSpecies = requiredSpecies;
		this.awardedExperience = awardedExperience;
		this.awardedMoney = awardedMoney;
		this.awardedItem = awardedItem;
		this.awardedSpecies = awardedSpecies;
	}

	public final static Task createBattleTask(final Species chosen, final Species opponent) {
		return new Task(opponent, null, chosen, opponent.getAwardedExperience(), opponent.getAwardedMoney(), null, null);
	}

	public final static Task createCatchTask(final Species chosen, final Species opponent) {
		return new Task(opponent, null, chosen, 0, 0, null, opponent);
	}

	public final static Task createBuyTask(final Item item) {
		return new Task(item, null, null, 0, 0, item, null); // Need requiredMoney = item.getPrice()
	}

	public final List<Item> getRequiredItems() {
		return requiredItems;
	}

	public final Species getRequiredSpecies() {
		return requiredSpecies;
	}

	public final int getAwardedExperience() {
		return awardedExperience;
	}

	public final int getAwardedMoney() {
		return awardedMoney;
	}

	public final Item getAwardedItem() {
		return awardedItem;
	}

	public final Species getAwardedSpecies() {
		return awardedSpecies;
	}*/
}
