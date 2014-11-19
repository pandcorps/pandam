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
package org.pandcorps.pandam;

import java.util.*;
import java.util.Map.*;
import org.pandcorps.core.*;
import org.pandcorps.core.col.*;
import org.pandcorps.core.img.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.handler.*;
import org.pandcorps.pandam.impl.*;

public class Panlayer extends BasePantity {
    /*package*/ final static Object defaultCollisionGroup = new Object();

    private static Boolean buffersSupported = null;
    private final FinPanple size;
    private final IdentityHashMap<Panctor, Object> actors
        = new IdentityHashMap<Panctor, Object>();
    private final Set<Panctor> pactors = Collections.unmodifiableSet(actors.keySet());
    private IdentityHashSet<Panctor> addedActors = new IdentityHashSet<Panctor>();
    private IdentityHashSet<Panctor> removedActors = new IdentityHashSet<Panctor>();
    private IdentityHashSet<Panctor> addedActorsBack = new IdentityHashSet<Panctor>();
    private IdentityHashSet<Panctor> removedActorsBack = new IdentityHashSet<Panctor>();
    /*package*/ final ArrayList<FinPantry<Object, ArrayList<CollisionListener>>> colliders
        = new ArrayList<FinPantry<Object, ArrayList<CollisionListener>>>();
    /*package*/ final ArrayList<FinPantry<Object, ArrayList<Collidable>>> collidables
        = new ArrayList<FinPantry<Object, ArrayList<Collidable>>>();
    /*package*/ final ArrayList<Object> timers = new ArrayList<Object>();
    /*package*/ final ArrayList<AnimationEndListener> animationEndListeners =
        new ArrayList<AnimationEndListener>();
    /*package*/ Panctor tracked = null;
    private final Panple origin = new ImplPanple(0, 0, 0);
    /*package*/ final Panple rawViewMin = new ImplPanple(0, 0, 0);
    /*package*/ final Panple rawViewMax = new ImplPanple(0, 0, 0);
    private final WrapPanple unmodViewMin = new WrapPanple(rawViewMin);
    private final WrapPanple unmodViewMax = new WrapPanple(rawViewMax);
    private final Panroom room;
    private final Pancolor blendColor = new Pancolor(Pancolor.MIN_VALUE, Pancolor.MIN_VALUE, Pancolor.MIN_VALUE, Pancolor.MIN_VALUE);
    private Panlayer beneath = null;
    private Panlayer above = null;
    /*package*/ Panlayer master = null; // Camera for this layer will be derived from master layer for parallax scrolling
    private boolean clearDepthEnabled = true;
    private boolean visible = true;
    private boolean active = true;
    private boolean constant = false;
    private boolean built = false;
    private ActorHandler addHandler = null;
    
    /*package*/ Panlayer(
        final String id,
        final float width, final float height, final float depth, final Panroom room) {
        this(id, new FinPanple(width, height, depth), room);
    }
    
    /*package*/ Panlayer(final String id, final FinPanple size, final Panroom room) {
        super(id);
        this.size = size;
        initCollisionGroups();
        this.room = room == null ? (Panroom) this : room;
    }
    
    /*package*/ void initCollisionGroups() {
        final ArrayList<Object> cols = new ArrayList<Object>();
        addAll(cols, colliders);
        colliders.clear();
        addAll(cols, collidables);
        collidables.clear();
        addCollisionGroup(defaultCollisionGroup);
        final Map<Object, Set<Object>> groups = Pangine.getEngine().collisionGroups;
        if (groups != null) {
            final HashSet<Object> accounted = new HashSet<Object>();
            for (final Entry<Object, Set<Object>> entry : groups.entrySet()) {
                addCollisionGroup(accounted, entry.getKey());
                for (final Object value : entry.getValue()) {
                    addCollisionGroup(accounted, value);
                }
            }
        }
        final int size = cols.size();
        for (int i = 0; i < size; i++) {
            addCol((Panctor) cols.get(i));
        }
    }

    private final void addCollisionGroup(final HashSet<Object> accounted, final Object group) {
        if (!accounted.contains(group)){
            accounted.add(group);
            addCollisionGroup(group);
        }
    }

    private final void addCollisionGroup(final Object group) {
        colliders.add(
            new FinPantry<Object, ArrayList<CollisionListener>>(
                group, new ArrayList<CollisionListener>()));
        collidables.add(
            new FinPantry<Object, ArrayList<Collidable>>(
                group, new ArrayList<Collidable>()));
    }

    private final <L extends ArrayList<? extends FinPantry<Object, ? extends ArrayList<?>>>>
        void addAll(final ArrayList<Object> dst, final L src) {
        final int size = src.size();
        for (int i = 0; i < size; i++) {
            dst.addAll(src.get(i).getValue());
        }
    }

    public final Panple getSize() {
        return size;
    }
    
    public final Panroom getRoom() {
        return room;
    }
    
    public final Pancolor getBlendColor() {
        return blendColor;
    }

    //public ArrayList<Panctor> getActors() {
    public final Set<Panctor> getActors() {
        return pactors;
    }

    public final void addActor(final Panctor actor) {
    	/*
    	If we remove an actor and add it back in the same frame, removedActors gets handled first.
    	So we must remove the actor from removedActors so that applyActorChanges won't remove it from the layer.
    	*/
    	removedActors.remove(actor);
        addedActors.add(actor);
        actor.layer = this;
        if (addHandler != null) {
            addHandler.run(actor);
        }
    }
    
    private final void applyAddActor(final Panctor actor) {
        if (actor.isDestroyed() || actors.put(actor, "") != null) {
            return;
        } else if (actor instanceof RoomAddListener) {
            ((RoomAddListener) actor).onRoomAdd(room.addEvent);
        }
        addCol(actor);
    }

    public final void removeActor(final Panctor actor) {
    	addedActors.remove(actor);
        removedActors.add(actor);
    }
    
    /*public final void removeAllActors() { // Would we ever want this?
        for (final Panctor actor : pactors) {
            removeActor(actor);
        }
    }*/
    
    public final boolean destroyActors(final Criterion<Panctor> c) {
        final boolean d1 = destroyActors(actors.keySet(), c, false);
        final boolean d2 = destroyActors(addedActors, c, true);
        return d1 || d2;
    }
    
    private final boolean destroyActors(final Collection<Panctor> col, final Criterion<Panctor> c, final boolean separateLoop) {
        boolean destroyed = false;
        List<Panctor> toDestroy = null;
        for (final Panctor actor : col) {
            if (c.isMet(actor)) {
                if (separateLoop) {
                    toDestroy = Coltil.add(toDestroy, actor);
                } else {
                    actor.destroy();
                }
                destroyed = true;
            }
        }
        for (final Panctor actor : Coltil.unnull(toDestroy)) {
            actor.destroy();
        }
        return destroyed;
    }
    
    public final void destroyAllActors() {
    	tracked = null;
        applyActorChanges(); // If we detach an actor before clearing the layer, this prevents its destruction
    	destroy(actors.keySet());
        destroy(addedActors);
    }
    
    private final void applyRemoveActor(final Panctor actor) {
        actors.remove(actor);
        removeCol(actor);
    }
    
    /*package*/ final void applyActorChanges() {
        IdentityHashSet<Panctor> buf;
        built = true;
        
        buf = addedActors;
        addedActors = addedActorsBack;
        addedActorsBack = buf;
        for (final Panctor actor : buf) {
        	built = false;
            applyAddActor(actor);
        }
        buf.clear();
        
        buf = removedActors;
        removedActors = removedActorsBack;
        removedActorsBack = buf;
        for (final Panctor actor : buf) {
        	built = false;
            applyRemoveActor(actor);
        }
        buf.clear();
    }

    /*package*/ final void addCol(final Panctor actor) {
        if (actor instanceof CollisionListener) {
            getCols(colliders, actor).add((CollisionListener) actor);
        } else if (actor instanceof Collidable) {
            // Every CollisionListener is also Collidable, but they should not be put in collidables
            getCols(collidables, actor).add((Collidable) actor);
        }
    }

    /*package*/ final boolean removeCol(final Panctor actor) {
        if (actor instanceof CollisionListener) {
            return getCols(colliders, actor).remove(actor);
        } else if (actor instanceof Collidable) {
            return getCols(collidables, actor).remove(actor);
        }
        return false;
    }

    private final static <E extends Collidable> ArrayList<E> getCols(
        final ArrayList<FinPantry<Object, ArrayList<E>>> cols, final Panctor actor) {
        return getCols(cols, actor.collisionGroup);
    }

    private final static <E extends Collidable> ArrayList<E> getCols(
        final ArrayList<FinPantry<Object, ArrayList<E>>> cols, final Object group) {
        for (int i = cols.size() - 1; i >= 1; i--) {
            final FinPantry<Object, ArrayList<E>> entry = cols.get(i);
            if (Pantil.equals(group, entry.getKey())) {
                return entry.getValue();
            }
        }
        return cols.get(0).getValue();
    }

    /*package*/ final ArrayList<CollisionListener> getCollisionListeners(final Object group) {
        return getCols(colliders, group);
    }

    /*package*/ final ArrayList<Collidable> getCollidables(final Object group) {
        return getCols(collidables, group);
    }
    
    public final Panctor getTracked() {
        return tracked;
    }
    
    public final Panple getOrigin() {
        return origin;
    }
    
    public final boolean center() {
        final Pangine engine = Pangine.getEngine();
        final int maxW = engine.getEffectiveWidth(), maxH = engine.getEffectiveHeight();
        final float layerW = size.getX(), layerH = size.getY();
        if (layerW < maxW || layerH < maxH) {
            origin.set((layerW < maxW) ? ((layerW - maxW) / 2) : 0, (layerH < maxH) ? ((layerH - maxH) / 2) : 0);
            return true;
        }
        return false;
    }
    
    public final UnmodPanple getViewMinimum() {
    	return unmodViewMin;
    }
    
    public final UnmodPanple getViewMaximum() {
    	return unmodViewMax;
    }
    
    public final Panlayer getBeneath() {
        return beneath;
    }
    
    public final Panlayer getAbove() {
        return above;
    }
    
    public final void addBeneath(final Panlayer layer) {
        layer.detach();
        /*final Panlayer t = beneath;
        beneath = layer;
        layer.above = t;*/
        if (beneath != null) {
            beneath.above = layer;
        }
        layer.beneath = beneath;
        layer.above = this;
        beneath = layer;
        if (this == room.base) {
            room.base = layer;
        }
    }
    
    public final void addAbove(final Panlayer layer) {
        //System.out.println("Added layer - " + layer);
        layer.detach();
        /*final Panlayer t = above;
        above = layer;
        layer.beneath = t;*/
        if (above != null) {
            above.beneath = layer;
        }
        layer.above = above;
        layer.beneath = this;
        above = layer;
    }
    
    public final void detach() {
        /*if (above != null || beneath != null) {
            throw new UnsupportedOperationException();
        }*/
        if (this == room.base) {
            room.base = above;
        }
        if (above != null) {
            above.beneath = beneath;
        }
        if (beneath != null) {
            beneath.above = above;
        }
        above = null;
        beneath = null;
    }
    
    @Override
    public void destroy() {
        destroyAllActors();
        master = null;
        detach();
        super.destroy();
        Pangine.getEngine().destroyLayer(this);
    }
    
    private final void destroy(final Collection<Panctor> actors) {
        for (final Panctor actor : actors) {
            actor.destroy(); // will call removeActor
        }
        actors.clear();
    }
    
    public final void setClearDepthEnabled(final boolean clearDepthEnabled) {
        this.clearDepthEnabled = clearDepthEnabled;
    }
    
    public final boolean isClearDepthEnabled() {
        return clearDepthEnabled;
    }
    
    public final void setVisible(final boolean visible) {
        this.visible = visible;
    }
    
    public final boolean isVisible() {
        return visible;
    }
    
    public final void setActive(final boolean active) {
        this.active = active;
    }
    
    public final boolean isActive() {
        return active;
    }
    
    public final void setConstant(final boolean constant) {
        this.constant = constant;
    }
    
    public final boolean isConstant() {
        return constant;
    }
    
    public final boolean isBuffered() {
    	if (constant) {
    	    if (buffersSupported == null) {
    	        buffersSupported = Boolean.valueOf(Pangine.getEngine().enableBuffers());
    	    }
    	    return buffersSupported.booleanValue();
    	}
    	return false;
    }
    
    public final boolean isBuilt() {
    	return built;
    }
    
    public final void setAddHandler(final ActorHandler addHandler) {
        this.addHandler = addHandler;
    }
    
    public Panlayer getBase() {
        return room.base;
    }
    
    public final Panlayer getTop() {
        Panlayer curr = this, next;
        while ((next = curr.above) != null) {
            curr = next;
        }
        return curr;
    }
    
    /*
    Scroll ratio
    1 pixel of sub movement for every n pixels of master movement
    1 / n = (sub - screen) / (master - screen)
    sub = screen + ((master - screen) / n)
    */
    public final void setMaster(final Panlayer master) {
    	this.master = master;
    	tracked = null;
    }
    
    public final Panlayer getMaster() {
    	return master;
    }
    
    public final static void iterateLayers(final Iteration<Panlayer> iteration) {
    	final Panroom room = Pangame.getGame().getCurrentRoom();
    	if (room == null) {
    		return;
    	} else if (!iteration.step(room)) {
    		return;
    	}
    	Panlayer layer = room;
    	while ((layer = layer.getAbove()) != null) {
    		if (!iteration.step(layer)) {
    			return;
    		}
    	}
    	layer = room;
    	while ((layer = layer.getBeneath()) != null) {
    		if (!iteration.step(layer)) {
    			return;
    		}
    	}
    }
}
