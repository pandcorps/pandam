/*
Copyright (c) 2009-2011, Andrew M. Martin
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
package org.pandcorps.animal;

import org.pandcorps.animal.Fruit.FruitType;
import org.pandcorps.core.Pantil;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandax.tile.*;

public class Tree extends TileOccupant implements StepListener {
    
    private final static int NUM_FRUIT = 3;
    
    private final FruitType fruitType;
    
    private final Fruit[] fruits = new Fruit[NUM_FRUIT];
    
    //private int timer = 0;
    private int timer = 1;
    
    protected Tree(final String id, final FruitType fruitType) {
        super(id);
        this.fruitType = fruitType;
        //blossom(); // Tree position not set yet
    }
    
    @Override
    public void onStep(final StepEvent event) {
        if (timer > 0) {
            timer--;
            if (timer == 0) {
                blossom();
            }
        }
    }
    
    @Override
    public final void onInteract(final TileWalker initiator) {
        if (fruits[0] != null) {
            final Player player = Player.getPlayer();
            for (int i = 0; i < NUM_FRUIT; i++) {
                final Fruit fruit = fruits[i];
                player.addInventory(fruit);
                fruit.transform(Poof.class);
                fruits[i] = null;
            }
            timer = 150;
        }
    }
    
    private final void blossom() {
        fruits[0] = blossom(-1, 18);
        fruits[1] = blossom(8, 25);
        fruits[2] = blossom(17, 18);
        //timer = 0;
    }
    
    private final Fruit blossom(final float offX, final float offY) {
        final Fruit fruit = new Fruit(Pantil.vmid(), fruitType);
        final Panple pos = getPosition();
        fruit.getPosition().set(pos.getX() + offX, pos.getY() + offY, pos.getZ() + 1);
        Pangame.getGame().getCurrentRoom().addActor(fruit);
        return fruit;
    }
}
