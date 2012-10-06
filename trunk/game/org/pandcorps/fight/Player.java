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
package org.pandcorps.fight;

import java.util.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.action.*;

//public final class Player extends Fighter {
public final class Player extends FighterController {
	//private final Panctor bound;
    //public Player(final String id, final Panmage still, final Panimation walk, final Panmage quick) {
        //final Fighter fighter = this;
    public Player(final Panctor bound) {
        //super(fighter);
    	//this.bound = bound; // Controller can swap between Fighters, so don't bind to one
        
        final Panteraction inter = Pangine.getEngine().getInteraction();
        
        // At first used ActionListener; don't think we'd ever want to use that for attacks
        // That would require extra work to prevent constant attacks
        //inter.register(inter.KEY_SHIFT_RIGHT, new ActionListener() {@Override public void onAction(final ActionEvent event) {
        bound.register(inter.KEY_SHIFT_RIGHT, new ActionStartListener() {@Override public void onActionStart(final ActionStartEvent event) {
            attack();
        }});
        if (FightGame.isDebug()) {
        	bound.register(inter.KEY_SLASH, new ActionStartListener() {@Override public void onActionStart(final ActionStartEvent event) {
            	// Controller subclasses should only call Controller setter/action methods
            	// Fighter methods just for debugging
                fighter.strong();
            }});
        	bound.register(inter.KEY_GRAVE, new ActionStartListener() {@Override public void onActionStart(final ActionStartEvent event) {
        		if (fighter == null) {
                    throw new NullPointerException("Player Controller's Fighter is null");
                }
                final Panlayer layer = fighter.getLayer();
                if (layer == null) {
                    // After a fight ends, input listeners must be unregistered, or this will fire for the new one and the old one
                    throw new NullPointerException("Fighter's Panlayer is null");
                }
                final Set<Panctor> actors = layer.getActors();
                if (actors == null) {
                    throw new NullPointerException("Panlayer's Panctor Set is null");
                }
            	for (final Panctor actor : actors) {
            		if (actor.getClass() == Fighter.class) {
            			final FighterController c = ((Fighter) actor).getController();
            			if (c instanceof Ai) {
            				final Ai ai = (Ai) c;
            				ai.mode = (byte) ((ai.mode + 1) % 3);
            			}
            		}
            	}
            }});
        }
        bound.register(inter.KEY_ENTER, new ActionStartListener() {@Override public void onActionStart(final ActionStartEvent event) {
            spec1();
        }});
        bound.register(inter.KEY_BACKSLASH, new ActionStartListener() {@Override public void onActionStart(final ActionStartEvent event) {
            spec2();
        }});
        
        registerPlayer(bound);
        
        bound.register(inter.KEY_TAB, new ActionStartListener() {@Override public void onActionStart(final ActionStartEvent event) {
            //TODO only allow switching among own team unless debugging
            final Set<Panctor> actors = fighter.getLayer().getRoom().getActors();
            boolean ready = false;
            for (int i = 0; i < 2; i++) {
                for (final Panctor actor : actors) {
                    if (actor == fighter) {
                        ready = true;
                    } else if (ready && actor.getClass() == Fighter.class) {
                        final Fighter tfighter = fighter, other = (Fighter) actor;
                        final FighterController oc = other.getController();
                        setFighter(other);
                        oc.setFighter(tfighter);
                        return;
                    }
                }
            }
        }});
    }
}
