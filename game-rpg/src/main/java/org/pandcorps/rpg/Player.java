/*
Copyright (c) 2009-2021, Andrew M. Martin
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
package org.pandcorps.rpg;

import org.pandcorps.core.col.*;
import org.pandcorps.game.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.pandax.tile.*;

public class Player extends Chr {
    private int worldX = 0; // Will be different than TileMap coordinates if current TileMap is a town or cave
    private int worldY = 0;
    private final CountMap<Item> inventory = new CountMap<Item>();
    
    /*package*/ boolean active = true;
    
	protected Player(final ChrDefinition def) {
		super(def);
	}
	
	@Override
    protected void onWalked() {
	    if (!active) {
            return;
        }
	    handleCoordinates();
	    handleRandomEncounters();
    }
	
	protected void handleCoordinates() {
	    if (!isInWorldMap()) {
	        return;
	    }
	    final TileMap tm = getTileMap();
	    final int index = getIndex();
	    worldX = tm.getColumn(index);
	    worldY = tm.getRow(index);
	}
	
	protected boolean isInWorldMap() {
	    return true; //TODO
	}
	
	protected void handleRandomEncounters() {
	    Fight.goFight(); //TODO Randomness, allowed by area
	}

	@Override
    protected void onStill() {
		if (!active) {
			return;
		}
Fight.goFight(); //TODO REMOVE, Totally temporary
        Guy4Controller.onStillPlayer(this);
        final String label = TileOccupant.getInteractLabel(getFacing());
        if (label == null) {
        	RpgGame.hudInteract.setVisible(false);
        } else if (!(RpgGame.hudInteract.isVisible() && label.contentEquals(RpgGame.hudInteractText))) {
        	RpgGame.hudInteract.setVisible(true);
        	RpgGame.hudInteractText.setLength(0);
        	RpgGame.hudInteractText.append(label);
        	RpgGame.hudInteract.getPosition().set(BaseGame.SCREEN_W / 2, 12);
        	RpgGame.hudInteract.centerX();
        }
	}
	
	public final int getWorldX() {
	    return worldX;
	}
	
	public final int getWorldY() {
        return worldY;
    }
	
	public final void addInventory(final Item item, final int amount) {
	    if (item == null) {
	        return;
	    }
	    inventory.add(item, amount);
	}
	
	public final void removeInventory(final Item item, final int amount) {
	    inventory.add(item, -amount);
	}
	
	// Crafting menu might be based on inventory, so might make more sense to pass MaterialItem
	public final Gear craft(final Material material, final GearSubtype subtype) {
	    final MaterialItem item = Chr.getMaterialItem(material);
	    final int amount = subtype.getMaterialRequiredToCraft();
	    final SmithingQuality quality = null; //TODO based on skill level
	    inventory.add(item, -amount); //TODO ensure sufficient quantity
	    final Gear gear = Chr.getGear(getGearName(quality, material, subtype));
	    inventory.inc(gear);
	    return gear;
	}
}
