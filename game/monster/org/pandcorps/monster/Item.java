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

public class Item extends Code {
    private static List<Item> items = null;
    private static Map<String, Item> map = null;

	private final int price;
	private final boolean exhaustible;
	private final boolean unique;
	private final boolean secret;

	/*package*/ Item(final String code, final String name, final int price, final boolean exhaustible, final boolean unique, final boolean secret) {
		super(code, name);
		this.price = price;
		this.exhaustible = exhaustible;
		this.unique = unique;
		this.secret = secret;
	}

	public final int getPrice() {
	    return price;
	}

	public final boolean isExhaustible() {
	    return exhaustible;
	}
	
	public final boolean isUnique() {
	    return unique;
    }
	
	public final boolean isSecret() {
        return secret;
    }
	
	public boolean isTechnique() {
	    return this instanceof Technique;
	}

	@Override
	public boolean isAvailable() {
	    return State.get().hasInventory(this);
	}

	@Override
	public void use() {
	    State.get().useInventory(this);
	}

	@Override
	public void add() {
	    State.get().addInventory(this);
	}

	public final static List<Item> getItems() {
	    return items;
	}

    /*package*/ final static void setItems(final List<Item> items) {
        Item.items = unmod(items);
        map = map(items);
        Container.setContainers(items);
        Technique.setTechniques(items);
    }

	public final static Item getItem(final String name) {
	    //return getEntity(items, name);
	    //return getCode(items, name);
	    return get(map, name);
	}
	
	@Override
	protected boolean canChoose(final Species s) {
        return s.canUse(this);
    }
}
