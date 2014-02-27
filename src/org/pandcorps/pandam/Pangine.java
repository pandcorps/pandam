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
import java.util.concurrent.*;

import org.pandcorps.core.*;
import org.pandcorps.core.col.*;
import org.pandcorps.core.img.*;
import org.pandcorps.core.img.scale.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.event.boundary.*;
import org.pandcorps.pandam.impl.*;

// Pandam Engine
public abstract class Pangine {
	public final static String PROP_IMPL = "org.pandcorps.pandam.engineImpl";
	
	protected static Pangine engine = null;

	private final Map<String, Pantity> entities = new ConcurrentHashMap<String, Pantity>();
	private final HashMap<Class<? extends Panctor>, Pantype> types =
		new HashMap<Class<? extends Panctor>, Pantype>();
	//private final ArrayList<Panmage> images = new ArrayList<Panmage>();
	protected final static int maxDimension;
	private final static FinPanple maxRoomSize;

	private final static Class<?>[] CLASS_ARRAY_STRING = new Class<?>[] {String.class};

	private final Panderer renderer = new Panderer();
	private final Pansic music = isMusicSupported() ? new Pansic() : null;
	
	/*package*/ Map<Object, Set<Object>> collisionGroups = null;
	
	private float zoomMag = 1;
	private Scaler scaler = null;
	
	//protected int frameLength = 30;
	protected int frameLengthNano = 30000000;
	private long clock = 0;
	private boolean paused = false;
	
	private boolean imageSavingEnabled = false;
	protected String screenShotDst = null;
	protected int screenShotInd = -1;

	static {
		int i = 0;

		while (true) {
		    final float f = i;
		    if (i != (int) f) {
		        break;
		    }
		    i++;
		}
		
		maxDimension = i;
		maxRoomSize = new FinPanple(i, i, 0);
		/*
		int low, high, guess;

		for (low = 1, high = 2; ; high *= 2) {
			if (high != (int) (float) high) {
				break;
			}
			low = high;
		}
		guess = (low + high) / 2;
		while (low != high) {
			if ();
		}

		maxRoomSize = new FinPanple(low, low);
		*/
	}

	public static Pangine getEngine() {
		if (engine != null) {
			return engine;
		}
		String className = Pantil.getProperty(PROP_IMPL);
		if (className == null) {
			className = "org.pandcorps.pandam.lwjgl.LwjglPangine";
			System.setProperty(PROP_IMPL, className);
		}
		try {
			engine = (Pangine) Reftil.newInstance(className);
			return engine;
		} catch (final Exception e) {
			throw new Panception(e);
		}
	}

	protected abstract Panplementation newImplementation(Panctor actor) throws Panception;

	protected abstract Panmage newImage(String id, final Panple origin, final Panple boundMin, final Panple boundMax, String location) throws Panception;

	public final Panmage createImage(final String id, final String location) throws Panception {
	    return createImage(id, null, null, null, location);
	}
	
	public final Panmage createImage(final String id, final Panple origin, final Panple boundMin, final Panple boundMax, final String location) throws Panception {
		final Panmage image = newImage(id, origin, boundMin, boundMax, location);

		register(image);
		//images.add(image);

		return image;
	}
	
	protected abstract Panmage newImage(String id, final Panple origin, final Panple boundMin, final Panple boundMax, Img img) throws Panception;
	
	public final Panmage createImage(final String id, final Img img) throws Panception {
        return createImage(id, null, null, null, img);
	}
	
	public final Panmage createImage(final String id, final Panple origin, final Panple boundMin, final Panple boundMax, final Img img) throws Panception {
		if (imageSavingEnabled) {
			Imtil.save(img, id + ".png");
		}
        final Panmage image = newImage(id, origin, boundMin, boundMax, img);

        register(image);
        //images.add(image);

        return image;
    }
	
	public final Panmage createEmptyImage(final String id, final Panple origin, final Panple boundMin, final Panple boundMax) {
	    final Panmage image = new EmptyPanmage(id, origin, boundMin, boundMax);

        register(image);

        return image;
	}
	
	protected abstract Panmage[][] newSheet(String prefix, final Panple origin, final Panple boundMin, final Panple boundMax, String location,
	                                        final int iw, final int ih) throws Panception;
	
	public final Panmage[][] createSheet(final String prefix, final Panple origin, final Panple boundMin, final Panple boundMax, final String location,
	                                     final int iw, final int ih) throws Panception {
	    final Panmage[][] sheet = newSheet(prefix, origin, boundMin, boundMax, location, iw, ih);
	    
	    for (final Panmage[] row : sheet) {
	        for (final Panmage image : row) {
	            register(image);
	        }
	    }
	    
	    return sheet;
	}

	protected Panframe newFrame(final String id, final Panmage image, final int dur, final int rot, final boolean mirror, final boolean flip,
	                            final Panple origin, final Panple boundMin, final Panple boundMax) throws Panception {
		return new ImplPanframe(id, image, dur, rot, mirror, flip, origin, boundMin, boundMax);
	}

	public Panframe createFrame(final String id, final Panmage image, final int dur) throws Panception {
	    return createFrame(id, image, dur, 0, false, false);
	}
	
	public Panframe createFrame(final String id, final Panmage image, final int dur, final int rot, final boolean mirror, final boolean flip) throws Panception {
	    return createFrame(id, image, dur, rot, mirror, flip, null, null, null);
	}
	
	public Panframe createFrame(final String id, final Panmage image, final int dur, final int rot, final boolean mirror, final boolean flip,
	                            final Panple origin, final Panple boundMin, final Panple boundMax) throws Panception {
		final Panframe frame = newFrame(id, image, dur, rot, mirror, flip, origin, boundMin, boundMax);

		register(frame);

		return frame;
	}

	protected Panimation newAnimation(final String id, final Panframe[] frames) {
		return new ImplPanimation(id, frames);
	}

	// Should copy the array into an unmodifiable list 
	public Panimation createAnimation(final String id, final Panframe... frames) {
		final Panimation anim = newAnimation(id, frames);

		register(anim);

		return anim;
	}

	public final Pantype createType(
		final String id,
		final Class<? extends Panctor> actorClass,
		final Panmage view) {
		return createType(id, actorClass, (Panview) view);
	}

	public final Pantype createType(
		final String id,
		final Class<? extends Panctor> actorClass,
		final Panimation view) {
		return createType(id, actorClass, (Panview) view);
	}

	/*package*/ final Pantype createType(
			final String id,
			final Class<? extends Panctor> actorClass,
			final Panview view) {
		final Pantype type = new Pantype(id, actorClass, view);

		register(type);
		if (types.put(actorClass, type) != null) {
			throw new Panception("Class " + actorClass.getName() + " already registered");
		}

		return type;
	}

	/*package*/ final <P extends Panctor> P createActor(final Class<P> actorClass, final String id) {
		try {
			return actorClass.getConstructor(CLASS_ARRAY_STRING).newInstance(id);
		} catch (final Exception e) {
			throw Pantil.toRuntimeException(e);
		}
	}

	public final Panroom createRoom
		(final String id, final float width, final float height, final float depth) {
		final Panroom room = new Panroom(id, width, height, depth);

		register(room);

		return room;
	}
	
	public final Panroom createRoom
        (final String id, final FinPanple size) {
        final Panroom room = new Panroom(id, size);
    
        register(room);
    
        return room;
    }
	
	public final Panlayer createLayer
        (final String id, final float width, final float height, final float depth, final Panroom room) {
        final Panlayer layer = new Panlayer(id, width, height, depth, room);
    
        register(layer);
    
        return layer;
    }

	public Pantity getEntity(final String id) {
		return entities.get(id);
	}
	
	public final Panmage getImage(final String id) {
        return (Panmage) getEntity(id);
    }
	
	public final Panmage getImage(final String id, final Callable<Panmage> loader) {
        Panmage image = getImage(id);
        if (image != null) {
        	return image;
        }
        try {
        	image = loader.call();
        } catch (final Exception e) {
        	throw Pantil.toRuntimeException(e);
        }
        if (!image.getId().equals(id)) {
        	throw new IllegalStateException("Requested " + id + " but " + loader.getClass().getName() + " generated " + image.getId());
        }
        return image;
    }
	
	public final Panmage getImage(final String id, final String location) {
		return getImage(id, new Callable<Panmage>() { @Override public Panmage call() {
        	return createImage(id, location); }});
	}
	
	public final Panmage findImage(final String location) {
		return getImage(location, location);
	}
	
	public final Panimation getAnimation(final String id) {
        return (Panimation) getEntity(id);
    }
	
	public final Panctor getActor(final String id) {
	    return (Panctor) getEntity(id);
	}

	public final Pantype getType(Class<? extends Panctor> actorClass) {
		return types.get(actorClass);
	}
	
	public final Pansic getMusic() {
		return isMusicSupported() ? music : null;
	}
	
	public boolean isMusicSupported() {
		return false;
	}

	public abstract Panteraction getInteraction();
	
	protected Panctor getActor(final ActionListener listener) {
		return getInteraction().getActor(listener);
	}
	
	protected Panctor getActor(final ActionStartListener listener) {
        return getInteraction().getActor(listener);
    }
	
	protected Panctor getActor(final ActionEndListener listener) {
        return getInteraction().getActor(listener);
    }
	
	public abstract int getDesktopWidth();
	
	public abstract int getDesktopHeight();
	
	//TODO Add to Panml
	public abstract void setDisplaySize(final int w, final int h);
    
    public abstract int getDisplayWidth();
    
    public abstract int getDisplayHeight();
    
    public abstract void setFullScreen(final boolean fullScreen);
    
    public abstract boolean isFullScreen();
    
    public final float getGameWidth() {
        return getDisplayWidth() / zoomMag;
    }
    
    public final float getGameHeight() {
        return getDisplayHeight() / zoomMag;
    }
	
	protected final void setActive(final Panput input, final boolean active) {
	    input.active = active;
	    if (!active) {
	        input.inactivated = false;
	    }
	}
	
	protected final Button newButton(final String name) {
		return new Panput.Button(name);
	}
	
	protected final void addController(final String name, final Button l, final Button r, final Button u, final Button d, final List<Button> bs) {
		getInteraction()._controllers.add(new Panteraction.Controller(name, l, r, u, d, bs));
	}

	private void register(final Pantity entity) throws Panception {
		String id = entity.getId();

		if (id == null) {
			throw new NullPointerException("Id is null");
		}
		final Pantity prev = entities.put(id, entity);
		if (prev != null) {
			throw new Panception("Id " + id + " already registered to " + prev);
		}
	}

	public void unregister(final Pantity entity) {
		entities.remove(entity.getId());
	}

	protected void step() {
	    if (paused) {
	        return;
	    }
	    clock++;
	    final Pangame game = Pangame.getGame();
	    game.step();
	    final Panscreen screen = Panscreen.get();
	    if (screen != null) {
	    	screen.step();
	    }
		final Panroom room = game.getCurrentRoom();
		if (room == null) {
			return;
		}
		for (Panlayer layer = room.base; layer != null; layer = layer.getAbove()) {
		    step(layer);
		}
	}
	
	private abstract static class OobEvaluator {
	    private final OobEvent left, right, bottom, top;
	    /*package*/ float l, r, b, t;
	    
	    private OobEvaluator(final OobEvent left, final OobEvent right, final OobEvent bottom, final OobEvent top) {
	        this.left = left;
	        this.right = right;
	        this.bottom = bottom;
	        this.top = top;
	    }
	    
	    protected abstract boolean init(final Panctor actor);
	    
	    protected abstract void trigger(final Object listener, final Object event);
	}
	
	private final static class AnyOobEvaluator extends OobEvaluator {
        private AnyOobEvaluator() {
            super(AnyOobEvent.LEFT, AnyOobEvent.RIGHT, AnyOobEvent.BOTTOM, AnyOobEvent.TOP);
        }
        
        @Override
        protected final boolean init(final Panctor actor) {
            if (actor instanceof AnyOobListener) {
                final Panple max = actor.getBoundingMaximum();
                r = max.getX();
                t = max.getY();
                final Panple min = actor.getBoundingMinimum();
                l = min.getX();
                b = min.getY();
                return true;
            }
            return false;
        }
        
        @Override
        protected final void trigger(final Object listener, final Object event) {
            ((AnyOobListener) listener).onAnyOob((AnyOobEvent) event);
        }
    }
	
	private final static class CenterOobEvaluator extends OobEvaluator {
	    private CenterOobEvaluator() {
	        super(CenterOobEvent.LEFT, CenterOobEvent.RIGHT, CenterOobEvent.BOTTOM, CenterOobEvent.TOP);
	    }
	    
	    @Override
	    protected final boolean init(final Panctor actor) {
	        if (actor instanceof CenterOobListener) {
	            final Panple pos = actor.getPosition();
                l = pos.getX();
                r = l;
                b = pos.getY();
                t = b;
	            return true;
	        }
	        return false;
	    }
        
	    @Override
        protected final void trigger(final Object listener, final Object event) {
	        ((CenterOobListener) listener).onCenterOob((CenterOobEvent) event);
        }
	}
	
	private final static class AllOobEvaluator extends OobEvaluator {
        private AllOobEvaluator() {
            super(AllOobEvent.LEFT, AllOobEvent.RIGHT, AllOobEvent.BOTTOM, AllOobEvent.TOP);
        }
        
        @Override
        protected final boolean init(final Panctor actor) {
            if (actor instanceof AllOobListener) {
                final Panple max = actor.getBoundingMaximum();
                l = max.getX();
                b = max.getY();
                final Panple min = actor.getBoundingMinimum();
                r = min.getX();
                t = min.getY();
                return true;
            }
            return false;
        }
        
        @Override
        protected final void trigger(final Object listener, final Object event) {
            ((AllOobListener) listener).onAllOob((AllOobEvent) event);
        }
    }
	
	private final static OobEvaluator[] oobEvaluators = {new AnyOobEvaluator(), new CenterOobEvaluator(), new AllOobEvaluator()};
	
	private final void step(final Panlayer room) {
	    if (!room.isActive()) {
	        return;
	    }
		final StepEvent stepEvent = StepEvent.INSTANCE;
		final Set<Panctor> actors = room.getActors();
		if (actors == null) {
			return;
		}

		for (final Panctor actor : actors) {
			if (actor instanceof StepListener) {
				((StepListener) actor).onStep(stepEvent);
			}
		}
		
		// List will change here and maybe in onTimer, so copy
		final ArrayList<Object> timers = room.timers, _timers = new ArrayList<Object>(timers);
		final int timerSize = timers.size();
		for (int i = 0; i < timerSize; i += 2) {
		    final TimerEvent timerEvent = (TimerEvent) _timers.get(i);
		    if (timerEvent.getClockEvent() <= clock) {
		    	final TimerListener timerListener = (TimerListener) _timers.get(i + 1);
		        timerListener.onTimer(timerEvent);
		        // Only remove if onTimer didn't
		        final int n = timers.indexOf(timerListener);
		        if (n >= 0) {
		        	timers.remove(n);
		        	timers.remove(n - 1);
		        }
		    }
		}

		final Panple size = room.getSize();
		final float width = size.getX();
		final float height = size.getY();
		//TODO Move oobs/cols into instance variables and clear after each loop?
		final ArrayList<Object> oobs = new ArrayList<Object>();
		for (final OobEvaluator eval : oobEvaluators) {
		    oobs.clear();
    		for (final Panctor actor : actors) {
    			if (eval.init(actor)) {
    				//final Panple pos = actor.getPosition();
    				//final float x = pos.getX();
    				if (eval.l < 0) {
    					//((CenterOobListener) actor).onCenterOob(CenterOobEvent.LEFT);
    					oobs.add(actor);
    					oobs.add(eval.left);
    				}
    				else if (eval.r >= width) {
    					//((CenterOobListener) actor).onCenterOob(CenterOobEvent.RIGHT);
    					oobs.add(actor);
    					oobs.add(eval.right);
    				}
    				//final float y = pos.getY();
    				if (eval.b < 0) {
    					//((CenterOobListener) actor).onCenterOob(CenterOobEvent.BOTTOM);
    					oobs.add(actor);
    					oobs.add(eval.bottom);
    				}
    				else if (eval.t >= height) {
    					//((CenterOobListener) actor).onCenterOob(CenterOobEvent.TOP);
    					oobs.add(actor);
    					oobs.add(eval.top);
    				}
    			}
    		}
    		final int numOobs = oobs.size();
    		for (int i = 0; i < numOobs; i += 2) {
    		    eval.trigger(oobs.get(i), oobs.get(i + 1));
    		}
		}

		final ArrayList<FinPantry<Object, ArrayList<CollisionListener>>> colliderGroups = room.colliders;
		final int numColliderGroups = colliderGroups.size();
		final ArrayList<Collidable> cols = new ArrayList<Collidable>();
		final ArrayList<FinPantry<Object, ArrayList<Collidable>>> collidableGroups = room.collidables;
		for (int gi = 0; gi < numColliderGroups; gi++) {
			final FinPantry<Object, ArrayList<CollisionListener>> entryi = colliderGroups.get(gi);
			final ArrayList<CollisionListener> colliders = entryi.getValue();
			final int numColliders = colliders.size();
			final Object groupi = entryi.getKey();
			final Set<Object> otheris = getOtherCollisionGroups(groupi);
			for (int i = 0; i < numColliders; i++) {
				final CollisionListener coli = colliders.get(i);
				for (int gj = gi; gj < numColliderGroups; gj++) {
					final FinPantry<Object, ArrayList<CollisionListener>> entryj = colliderGroups.get(gj);
					final Object groupj = entryj.getKey();
					final boolean icaresj = caresAbout(groupi, otheris, groupj);
					final boolean jcaresi = caresAbout(groupj, getOtherCollisionGroups(groupj), groupi);
					if (icaresj || jcaresi) {
						final ArrayList<CollisionListener> colliderjs = entryj.getValue();
						final int numColliderjs = colliderjs.size();
						for (int j = gj == gi ? i + 1 : 0; j < numColliderjs; j++) {
							final CollisionListener colj = colliderjs.get(j);
							if (isCollision(coli, colj)) {
								if (icaresj) {
									cols.add(coli);
									cols.add(colj);
								}
								if (jcaresi) {
									cols.add(colj);
									cols.add(coli);
								}
							}
						}
						if (icaresj) {
							final ArrayList<Collidable> collidables = collidableGroups.get(gj).getValue();
							final int numCollidables = collidables.size();
							for (int j = 0; j < numCollidables; j++) {
								final Collidable colj = collidables.get(j);
								if (isCollision(coli, colj)) {
									cols.add(coli);
									cols.add(colj);
								}
							}
						}
					}
				}
			}
		}
		final int numCollisions = cols.size();
		for (int i = 0; i < numCollisions; i += 2) {
			final CollisionListener coli = (CollisionListener) cols.get(i);
			final Collidable colj = cols.get(i + 1);
			if (!(coli.isDestroyed() || colj.isDestroyed())) {
				coli.onCollision(new CollisionEvent(colj));
			}
		}

		for (final Panctor actor : actors) {
			actor.updateView();
		}
		for (final AnimationEndListener listener : room.animationEndListeners) {
			listener.onAnimationEnd(AnimationEndEvent.INSTANCE);
		}
		room.animationEndListeners.clear();
		
		for (final Panctor actor : actors) {
            if (actor instanceof StepEndListener) {
                ((StepEndListener) actor).onStepEnd(StepEndEvent.INSTANCE);
            }
        }
		
		room.applyActorChanges();
	}

	public final boolean hasCollision(final Panctor actor, final Object group) {
		//final Panroom room = Pangame.getGame().getCurrentRoom();
	    final Panlayer room = actor.getLayer();
		if (hasCollision(actor, room.getCollidables(group))) {
			return true;
		}
		return hasCollision(actor, room.getCollisionListeners(group));
	}

	private final boolean hasCollision(final Panctor actor, final ArrayList<? extends SpecPanctor> list) {
		for (int i = list.size() - 1; i >= 0; i--) {
			if (isCollision(actor, list.get(i))) {
				return true;
			}
		}
		return false;
	}

	private final Set<Object> getOtherCollisionGroups(final Object group) {
		return collisionGroups == null ? null : collisionGroups.get(group);
	}

	private final boolean caresAbout(final Object first, final Set<Object> firstOthers, final Object second) {
		return first == Panroom.defaultCollisionGroup || firstOthers == null || firstOthers.contains(second);
	}

	public final boolean isCollision(final SpecPanctor coli, final SpecPanctor colj) {
		/*final Panple posi = coli.getPosition();
		final Panple posj = colj.getPosition();
		final float xi = posi.getX();
		final float xj = posj.getX();
		final Pansplay disi = coli.getCurrentDisplay();
		final Pansplay disj = colj.getCurrentDisplay();*/
		final Panple mini = coli.getBoundingMinimum();
		final Panple maxj = colj.getBoundingMaximum();
		//if (mini.getX() + xi > maxj.getX() + xj) {
		if (mini.getX() > maxj.getX()) {
			return false;
		}
		/*final float yi = posi.getY();
		final float yj = posj.getY();
		if (mini.getY() + yi > maxj.getY() + yj) {*/
		if (mini.getY() > maxj.getY()) {
			return false;
		}
		final Panple maxi = coli.getBoundingMaximum();
		final Panple minj = colj.getBoundingMinimum();
		//if (maxi.getX() + xi < minj.getX() + xj) {
		if (maxi.getX() < minj.getX()) {
			return false;
		}
		//if (maxi.getY() + yi < minj.getY() + yj) {
		if (maxi.getY() < minj.getY()) {
			return false;
		}

		return true;
	}

	public final void setCollisionGroups(final Map<Object, Set<Object>> groups) {
		collisionGroups = groups;
		final Panroom room = Pangame.getGame().getCurrentRoom();
		if (room != null) {
			room.initCollisionGroups();
		}
	}

	//TODO Maybe the group key should be a special class CollisionGroup instead of just an Object
	public final void setCollisionGroup(final Collidable col, final Object group) {
		final Panroom room = Pangame.getGame().getCurrentRoom();
		final Panctor actor = (Panctor) col;
		final boolean isRoom = room != null;
		final boolean inRoom;
		if (isRoom) {
			inRoom = room.removeCol(actor);
		}
		else {
			inRoom = false;
		}
		actor.collisionGroup = group;
		/*
		If the Collidable wasn't already in the room,
		don't add it to the room's Collidables.
		Wait until it's added to the room,
		which will also add it to the room's Collidables.
		*/
		if (inRoom) {
			room.addCol(actor);
		}
	}

	protected final static Panplementation getImplementation(final Panctor actor) {
		return actor.getImplementation();
	}

	protected final void renderView(final Panctor actor) {
		actor.renderView(renderer);
	}

	/*protected final void setActive(final Panction action, final boolean active) {
		action.active = active;
	}*/

	protected abstract void init() throws Exception;

	protected abstract void loop() throws Exception;
	
	protected abstract void destroy() throws Exception;

	public final void track(final Panctor actor) {
	    actor.layer.tracked = actor;
	    actor.layer.master = null;
	}
	
	protected Panple getRawViewMinimum(final Panlayer layer) {
		return layer.rawViewMin;
	}
	
	protected Panple getRawViewMaximum(final Panlayer layer) {
		return layer.rawViewMax;
	}
	
	public final void zoom(final float mag) {
	    zoomMag = mag;
	}
	
	public final float getZoom() {
	    return zoomMag;
	}
	
	// Pre-scales each image, not the rendered screen
	public final void setImageScaler(final Scaler scaler) {
		this.scaler = scaler;
	}
	
	public final Scaler getImageScaler() {
		return scaler;
	}
	
	// Will zoom to make window size as big as possible for the current display device, making it a multiple of given dimensions
	public final void setMaxZoomedDisplaySize(final int baseWidth, final int baseHeight) {
	    final int absWidth = getDesktopWidth() - 12, absHeight = getDesktopHeight() - 24;
	    final int zoom = Math.min(getMaxDim(baseWidth, absWidth), getMaxDim(baseHeight, absHeight));
	    setDisplaySize(zoom * baseWidth, zoom * baseHeight);
        zoom(zoom);
	}
	
	private final int getMaxDim(final int base, final int abs) {
	    int zoom;
	    if (scaler == null) {
	    	for (zoom = 1; zoom * base < abs; zoom += 1);
	    	return Math.max(1, zoom - 1);
	    } else {
	    	for (zoom = 1; zoom * base < abs; zoom *= 2);
	    	return Math.max(1, zoom / 2);
	    }
	}
	
	// Will halve the display size until it can do so no longer without dropping below given dimensions
	public final void setApproximateFullScreenZoomedDisplaySize(final int minWidth, final int minHeight) {
	    final int topWidth = getDesktopWidth(), topHeight = getDesktopHeight();
	    setFullScreen(true);
	    setDisplaySize(topWidth, topHeight);
	    zoom(Math.min(getApproxDim(minWidth, topWidth), getApproxDim(minHeight, topHeight)));
    }
	
	private final int getApproxDim(final int min, final int top) {
	    int zoom;
	    // Check that top is a multiple of zoom?
        for (zoom = 1; top / zoom >= min; zoom *= 2);
        return Math.max(1, zoom / 2);
	}
	
	public final int getEffectiveWidth() {
		return (int) (getDisplayWidth() / getZoom());
	}
	
	public final int getEffectiveHeight() {
		return (int) (getDisplayHeight() / getZoom());
	}
	
	public final void setFrameLengthMilli(final int frameLengthMilli) {
	    setFrameLengthNano(frameLengthMilli * 1000000);
	}
	
	public final void setFrameLengthNano(final int frameLengthNano) {
	    this.frameLengthNano = frameLengthNano;
	}
	
	public final void setFrameRate(final int framesPerSecond) {
	    setFrameLengthNano((int) Math.round(1000000000.0 / framesPerSecond));
	}
	
	public final long getClock() {
	    return clock;
	}
	
	public final boolean isOn(final int half) {
		return (clock % (half * 2)) < half;
	}
	
	public final void togglePause() {
	    paused = !paused;
	}
	
	public final void addTimer(final Panctor actor, final long duration, final TimerListener listener) {
	    Panlayer layer = null;
	    if (actor != null) {
            getInteraction().get(actor).add(listener);
            layer = actor.getLayer();
        }
	    if (layer == null) {
	        layer = Pangame.getGame().getCurrentRoom();
	    }
	    layer.timers.add(new TimerEvent(clock + duration));
	    layer.timers.add(listener);
	}
	
	public final void removeTimer(final TimerListener listener) {
	    for (Panlayer layer = Pangame.getGame().getCurrentRoom().base; layer != null; layer = layer.getAbove()) {
	        final List<Object> timers = layer.timers;
    	    for (int i = timers.size() - 1; i > 0; i -= 2) {
    	        if (timers.get(i) == listener) {
    	            timers.remove(i);
    	            timers.remove(i - 1);
    	        }
    	    }
	    }
	}
	
	public final void removeTimers(final Iterable<TimerListener> listeners) {
		for (final TimerListener listener : Coltil.unnull(listeners)) {
			removeTimer(listener);
		}
	}
	
	public final void setImageSavingEnabled(final boolean imageSavingEnabled) {
		this.imageSavingEnabled = imageSavingEnabled;
	}
	
	public final void captureScreen() {
	    captureScreen("Pandam" + System.currentTimeMillis() + ".png");
	}
	
	public final void captureScreen(final String screenShotDst) {
	    this.screenShotDst = screenShotDst;
	}
	
	public final void startCaptureFrames() {
		screenShotInd = 0;
		screenShotDst = "Pandam" + System.currentTimeMillis() + ".";
	}
	
	public final void stopCaptureFrames() {
		screenShotInd = -1;
		screenShotDst = null;
	}
	
	public abstract void setTitle(final String title);
	
	public abstract void setIcon(final String... locations);
	
	public abstract void setBgColor(final Pancolor color);

	public abstract void exit();

	public final Panple getMaxRoomSize() {
		return maxRoomSize;
	}
}
