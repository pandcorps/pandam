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
package org.pandcorps.pandam;

import java.io.*;
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

	private final Map<String, Pantity> entities = Coltil.newSafeMap();
	private boolean entityMapEnabled = false;
	private final HashMap<Class<? extends Panctor>, Pantype> types =
		new HashMap<Class<? extends Panctor>, Pantype>();
	//private final ArrayList<Panmage> images = new ArrayList<Panmage>();
	protected final static int maxDimension;
	private final static FinPanple2 maxRoomSize;

	private final static Class<?>[] CLASS_ARRAY_STRING = new Class<?>[] {String.class};

	private final Panderer renderer = new Panderer();
	protected Panaudio audio = null;
	
	/*package*/ Map<Object, Set<Object>> collisionGroups = null;
	
	private int topOffset = 0;
	private float zoomMag = 1;
	private boolean zoomInteger = true;
	private Runnable zoomChangeHandler = null;
	private Scaler scaler = null;
	
	//protected int frameLength = 30;
	protected int frameLengthNano = 30000000;
	private long clock = 0;
	private final static byte PAUSED_NO = 0;
	private final static byte PAUSED_NEW = 1;
	private final static byte PAUSED_YES = 2;
	private byte paused = PAUSED_NO;
	private final Queue<Runnable> queuedJobs = new ConcurrentLinkedQueue<Runnable>();
	
	private boolean imageSavingEnabled = false;
	protected String screenShotDst = null;
	protected int screenShotInd = -1;
	protected int screenShotX = -1;
	protected int screenShotY = -1;
	protected int screenShotW = -1;
	protected int screenShotH = -1;
	protected int screenShotZoom = 1;
	
	protected SwipeListener swipeListener = null;
	
	private final static String LOG_FATAL = "log.fatal.txt";
	
	protected Runnable uncaughtBackHandler = null;
	private boolean fatalLogged = false;
	
	private String debugInfo = null;

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
		maxRoomSize = new FinPanple2(i, i);
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

		maxRoomSize = new FinPanple2(low, low);
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
	
	// rot = number of counter-clockwise 90-degree rotations, see Panctor.setRot
	public Panframe createFrame(final String id, final Panmage image, final int dur, final int rot, final boolean mirror, final boolean flip) throws Panception {
	    return createFrame(id, image, dur, rot, mirror, flip, null, null, null);
	}
	
	// rot = number of counter-clockwise 90-degree rotations, see Panctor.setRot
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
	
	public Panimation createReverseAnimation(final String id, final Panimation src) {
	    final Panframe[] srcFrames = src.getFrames();
	    final int size = srcFrames.length;
	    final Panframe[] frames = new Panframe[size];
	    for (int i = 0; i < size; i++) {
	        frames[i] = srcFrames[size - i - 1];
	    }
	    return createAnimation(id, frames);
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
	
	public final Panaudio getAudio() {
		return audio;
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
	
	/*
    Size of device's screen.
    */
	public abstract int getDesktopWidth();
	
	public abstract int getDesktopHeight();
	
	//TODO Add to Panml
	public abstract void setDisplaySize(final int w, final int h);
    
	/*
    Size of window.
    Same as desktopWidth if game is in full-screen mode.
    */
    public abstract int getDisplayWidth();
    
    public abstract int getDisplayHeight();
    
    /*
    Size of viewport within window.
    Portion of displayWidth that will be used for rendering.
    Same as displayWidth if displayWidth is a multiple of zoomMag (or zooming is disabled).
    If zoom leaves partial effective pixels on edges, then the edges will be black.
    */
    public abstract int getTruncatedWidth();
    
    public abstract int getTruncatedHeight();
    
    public abstract void setFullScreen(final boolean fullScreen);
    
    public abstract boolean isFullScreen();
    
    protected final void setActive(final Panput input, final boolean active) {
	    input.active = active;
	    if (!active) {
	        input.inactivated = false;
	    }
	}
    
    protected final void setDeactivating(final Panput input, final boolean deactivating) {
        input.deactivating = deactivating;
    }
    
    public void deactivate(final Panput input) {
    }
	
	protected abstract boolean isEnded(final Panput input);
	
	public abstract Set<Panput> getActiveInputs();
	
	public final void inactivateAllInputs() {
	    for (final Panput input : Coltil.unnull(getActiveInputs())) {
	        input.inactivate();
	    }
	}
	
	protected final void addMouseButton(final String name, final int i) {
	    final Panteraction inter = getInteraction();
	    inter.MOUSE._BUTTONS.add(new MouseButton(inter, name, i));
	}
	
	public int getMouseX() {
    	return 0;
    }
    
    public int getMouseY() {
    	return 0;
    }
	
	public boolean isMouseSupported() {
		return false;
	}
	
	//@OverrideMe
	public void setMouseTouchEnabled(final boolean mouseTouchEnabled) {
	}
	
	protected final Button newButton(final String name) {
		return new Panput.Button(name);
	}
	
	protected final void addController(final String name, final Button l, final Button r, final Button u, final Button d, final List<Button> bs) {
		getInteraction()._controllers.add(new Panteraction.Controller(name, l, r, u, d, bs));
	}
	
	public abstract void registerTouchButton(final TouchButton button);
	
	public abstract boolean unregisterTouchButton(final TouchButton button);
	
	public abstract boolean isTouchButtonRegistered(final TouchButton button);
	
	public abstract void clearTouchButtons();
	
	public abstract void clearTouchEvents();
	
	public abstract boolean isTouchSupported();
	
	//@OverrideMe
	public boolean isMultiTouchSupported() {
	    return false;
	}
	
	protected void setTouch(final int x, final int y) {
	    final Touch touch = getInteraction().TOUCH;
	    touch.x = x;
	    touch.y = y;
	}
	
	public void setRangeZ(final int minZ, final int maxZ) {
	}
	
	public final boolean isEntityMapEnabled() {
	    return entityMapEnabled;
	}
	
	public void setEntityMapEnabled(final boolean entityMapEnabled) {
		this.entityMapEnabled = entityMapEnabled;
		if (!entityMapEnabled) {
			entities.clear();
		}
	}

	private void register(final Pantity entity) throws Panception {
		if (entityMapEnabled) {
			final String id = entity.getId();
			if (id == null) {
				throw new NullPointerException("Id is null");
			}
			final Pantity prev = entities.put(id, entity);
			if (prev != null) {
				throw new Panception("Id " + id + " already registered to " + prev);
			}
		}
	}

	public void unregister(final Pantity entity) {
		entities.remove(entity.getId());
	}
	
	private final Set<Panlayer> steppedLayers = new IdentityHashSet<Panlayer>();

	protected void step() {
		// Input might add a button and then pause; want button displayed; finish current frame
		if (paused == PAUSED_NEW) {
			paused = PAUSED_YES;
			//getAudio().pauseMusic();
		} else if (paused == PAUSED_YES) {
			return;
		}
		final Pangame game = Pangame.getGame();
		if (game.isClockRunning()) {
		    clock++;
		}
		executeJobs();
	    game.step();
	    final Panscreen screen = Panscreen.get();
	    if (screen != null) {
	    	screen.step();
	    }
		final Panroom room = game.getCurrentRoom();
		if (room == null) {
			return;
		}
		steppedLayers.clear();
		for (Panlayer layer = room.base; layer != null; layer = layer.getAbove()) {
			step(layer);
			steppedLayers.add(layer);
		}
		final Panroom newRoom = game.getCurrentRoom(); // This step could have changed the current room; start rendering its actors right away
		if (newRoom == null) {
		    return;
		}
		for (Panlayer layer = newRoom.base; layer != null; layer = layer.getAbove()) {
		    if (!steppedLayers.contains(layer)) {
		        layer.applyActorChanges(true);
		    }
		}
		steppedLayers.clear();
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
		    // An actor could be destroyed during this loop and wouldn't be removed from the List yet, so check
			if (!actor.isDestroyed() && actor instanceof StepListener) {
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
		
		room.applyActorChanges(true);
		
		/*
		Used to run onStepEnd at very end of this method.
		But it's generally used to update the position of a child actor after its parent's position was updated in onStep.
		That must be done before collision checking.
		Collision events generally destroy actors without changing their position.
		So there shouldn't ever be any benefit to running onStepEnd after collision checking.
		*/
		for (final Panctor actor : actors) {
            if (actor instanceof StepEndListener) {
                ((StepEndListener) actor).onStepEnd(StepEndEvent.INSTANCE);
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
		
		room.applyActorChanges(false);
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
	    if (actor.isVisible()) {
	        actor.renderView(renderer);
	    }
	}

	/*protected final void setActive(final Panction action, final boolean active) {
		action.active = active;
	}*/

	protected abstract void init() throws Exception;

	protected abstract void loop() throws Exception;
	
	protected abstract void destroy() throws Exception;
	
	protected void destroyLayer(final Panlayer layer) {
	}
	
	protected void recreate() throws Exception {
	}

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
	
	public void setTopOffset(final int topOffset) {
		this.topOffset = topOffset;
	}
	
	public final int getTopOffset() {
		return topOffset;
	}
	
	public final int getEffectiveTopOffset() {
		return (int) (getTopOffset() / getZoom());
	}
	
	public final int getEffectiveTop() {
		return getEffectiveHeight() - getEffectiveTopOffset();
	}
	
	public void zoom(final float mag) {
	    if (zoomMag != mag) {
	        zoomMag = mag;
	        final int izoom = (int) mag;
	        final float fzoom = izoom;
	        zoomInteger = mag == fzoom;
	        if (zoomChangeHandler != null) {
	            zoomChangeHandler.run();
	        }
	    }
	}
	
	public final float getZoom() {
	    return zoomMag;
	}
	
	protected final boolean isZoomInteger() {
	    return zoomInteger;
	}
	
	public final void setZoomChangeHandler(final Runnable zoomChangeHandler) {
		this.zoomChangeHandler = zoomChangeHandler;
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
	    	for (zoom = 1; zoom * base < abs; zoom++);
	    	return Math.max(1, zoom - 1);
	    } else {
	    	for (zoom = 1; zoom * base < abs; zoom *= 2);
	    	return Math.max(1, zoom / 2);
	    }
	}
	
	// Will halve the display size until it can do so no longer without dropping below given dimensions
	public final void setApproximateFullScreenZoomedDisplaySize(final int minWidth, final int minHeight) {
		setApproximateFullScreenZoomedDisplaySize(minWidth, minHeight, true);
	}
	
	public final void setFullScreenZoomed(final float mag) {
	    setFullScreen(true);
	    setDisplaySize(getDesktopWidth(), getDesktopHeight());
	    zoom(mag);
	}
	
	public final void setFullScreenEffectiveSize(final int w, final int h) {
        setFullScreen(true);
        setDisplaySize(getDesktopWidth(), getDesktopHeight()); // Should this be in setFullScreen? Do we ever not want this in full-screen mode?
        setEffectiveSize(w, h);
        setRenderSmallAndThenEnlarge(true);
    }
	
	public final void setApproximateFullScreenZoomedDisplaySize(final int minWidth, final int minHeight, final boolean pow2) {
	    setFullScreenZoomed(getApproximateFullScreenZoomedDisplaySize(minWidth, minHeight, pow2));
    }
	
	public final void setApproximateZoomedDisplaySize(final int dispWidth, final int dispHeight,
			final int minWidth, final int minHeight, final boolean pow2) {
		setDisplaySize(dispWidth, dispHeight);
        zoom(getApproximateZoomedDisplaySize(dispWidth, dispHeight, minWidth, minHeight, pow2));
	}
	
	public final int getApproximateFullScreenZoomedDisplaySize(final int minWidth, final int minHeight, final boolean pow2) {
		return getApproximateZoomedDisplaySize(getDesktopWidth(), getDesktopHeight(), minWidth, minHeight, pow2);
	}
	
	public final int getApproximateZoomedDisplaySize(final int dispWidth, final int dispHeight,
			final int minWidth, final int minHeight, final boolean pow2) {
		return Math.min(getApproxDim(minWidth, dispWidth, pow2), getApproxDim(minHeight, dispHeight, pow2));
	}
	
	private final int getApproxDim(final int min, final int top, final boolean pow2) {
	    int zoom;
	    // Check that top is a multiple of zoom?
	    if (pow2) {
	        for (zoom = 1; top / zoom >= min; zoom *= 2);
	        return Math.max(1, zoom / 2);
	    } else {
	    	for (zoom = 1; top / zoom >= min; zoom++);
	        return Math.max(1, zoom - 1);
	    }
	}
	
	public final void zoomToWidth(final int w) {
	    zoom(((float) getDisplayWidth()) / w);
	}
	
	public final void zoomToHeight(final int h) {
	    zoom(((float) getDisplayHeight()) / h);
	}
	
	public final void zoomToMinimum(final int min) {
	    if (getDisplayHeight() > getDisplayWidth()) {
	        zoomToWidth(min);
	    } else {
	        zoomToHeight(min);
	    }
	}
	
	//@OverrideMe
	protected void initScreen() {
	}
	
	/*
    Effective size of the game.
    If zooming, then "effective" pixels will be z*z squares,
    and the effective screen resolution dimensions will be smaller.
    Same as truncatedWidth when zooming is disabled.
    */
	public abstract int getEffectiveWidth();
	
	public abstract int getEffectiveHeight();
	
	public abstract void setEffectiveSize(final int w, final int h);
	
	public abstract void setRenderSmallAndThenEnlarge(final boolean renderSmallAndThenEnlarge);
	
	/*
	// Used to have these, but getEffectiveWidth/Height are better
	public final float getGameWidth() {
        return getDisplayWidth() / zoomMag;
    }
    
    public final float getGameHeight() {
        return getDisplayHeight() / zoomMag;
    }
    */
	
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
	
	public final boolean isPaused() {
		return paused != PAUSED_NO;
	}
	
	public final void setPaused(final boolean paused) {
		if (paused) {
			pause();
		} else {
			unpause();
		}
	}
	
	public final void togglePause() {
		if (paused == PAUSED_NO) {
			pause();
		} else {
			unpause();
		}
	}
	
	private final void pause() {
		if (paused != PAUSED_NO) {
			return;
		}
		paused = PAUSED_NEW;
		getAudio().pauseMusic();
	}
	
	private final void unpause() {
		if (paused == PAUSED_NO) {
			return;
		}
		//if (paused == PAUSED_YES) {
			try {
				getAudio().resumeMusic();
			} catch (final Exception e) {
				throw Panception.get(e);
			}
		//}
		inactivateAllInputs();
		clearTouchEvents();
		paused = PAUSED_NO;
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
	
	public final void executeInGameThread(final Runnable r) {
	    queuedJobs.offer(r); // Always enqueue it, even if already in game thread; callers expect the job to be executed at a certain point during the game loop
	}
	
	private final void executeJobs() {
	    Runnable r;
	    while ((r = queuedJobs.poll()) != null) {
	        r.run();
	    }
	}
	
	public final void setImageSavingEnabled(final boolean imageSavingEnabled) {
		this.imageSavingEnabled = imageSavingEnabled;
	}
	
	public final void captureScreen() {
	    captureScreen("Pandam" + System.currentTimeMillis() + ".png");
	}
	
	public final void captureScreen(final String screenShotDst) {
	    initCapture();
	    this.screenShotDst = screenShotDst;
	}
	
	public final void startCaptureFrames() {
	    if (screenShotInd >= 0) {
	        return;
	    }
	    initCapture();
		screenShotInd = 0;
		screenShotDst = "Pandam" + System.currentTimeMillis() + ".";
	}
	
	public final void stopCaptureFrames() {
		screenShotInd = -1;
		screenShotDst = null;
	}
	
	private final void initCapture() {
	    if (screenShotX >= 0) {
	        return;
	    }
	    final int zoom = Math.round(getZoom());
	    final String propW = Pantil.getProperty("org.pandcorps.pandam.capture.w");
	    final String propH = Pantil.getProperty("org.pandcorps.pandam.capture.h");
	    screenShotW = Chartil.isValued(propW) ? (Integer.parseInt(propW) * zoom) : -1;
	    screenShotH = Chartil.isValued(propH) ? (Integer.parseInt(propH) * zoom) : -1;
	    final String propX = Pantil.getProperty("org.pandcorps.pandam.capture.x");
	    final String propY = Pantil.getProperty("org.pandcorps.pandam.capture.y");
        screenShotX = Chartil.isValued(propX) ? (Integer.parseInt(propX) * zoom) : 0;
        screenShotY = Chartil.isValued(propY) ? (Integer.parseInt(propY) * zoom) : 0;
	    final String propZoom = Pantil.getProperty("org.pandcorps.pandam.capture.zoom");
	    if (Chartil.isValued(propZoom)) {
	        screenShotZoom = Integer.parseInt(propZoom);
	    }
	}
	
	public abstract void getClipboard(final Handler<String> handler);
	
	public abstract void setClipboard(final String value);
	
	public abstract void setTitle(final String title);
	
	public abstract void setIcon(final String... locations);
	
	public abstract void setBgColor(final Pancolor color);
	
	public final void setBgColor(final float r, final float g, final float b) {
	    setBgColor(r, g, b, 1.0f);
	}
	
	public abstract void setBgColor(final float r, final float g, final float b, final float a);
	
	public abstract void enableColorArray();
	
	public boolean enableBuffers() {
	    return false;
	}
	
	public final void setSwipeListener(final SwipeListener swipeListener) {
	    this.swipeListener = swipeListener;
	}
	
	public final void setUncaughtBackHandler(final Runnable uncaughtBackHandler) {
		this.uncaughtBackHandler = uncaughtBackHandler;
	}
	
	public final void setFatalLogged(final boolean fatalLogged) {
	    this.fatalLogged = fatalLogged;
	}
	
	public final void onFatal(final Throwable cause) {
	    if (!fatalLogged) {
	        return;
	    }
	    PrintWriter w = null;
	    try {
    	    w = Iotil.getPrintWriter(LOG_FATAL);
    	    cause.printStackTrace(w);
	    } finally {
	        Iotil.close(w);
	    }
    }
	
	public final String getFatalLog() {
	    if (Iotil.exists(LOG_FATAL)) {
	        final String log = Iotil.read(LOG_FATAL);
	        Iotil.delete(LOG_FATAL);
	        return log;
	    }
	    return null;
	}
	
	public abstract boolean isRunning();

	public abstract void exit();
	
	public abstract void exit(final Throwable cause);
	
	public final Panple getMaxRoomSize() {
		return maxRoomSize;
	}
	
	public final void setDebugInfo(final String debugInfo) {
		this.debugInfo = debugInfo;
	}
	
	public final void appendDebugInfo(final String debugInfo) {
		if (Chartil.isEmpty(this.debugInfo)) {
			setDebugInfo(debugInfo);
		} else {
			this.debugInfo = this.debugInfo + " " + debugInfo;
		}
	}
	
	public final String getDebugInfo() {
		return debugInfo;
	}
}
