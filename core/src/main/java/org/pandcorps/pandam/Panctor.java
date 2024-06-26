/*
Copyright (c) 2009-2024, Andrew M. Martin
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

import org.pandcorps.core.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.*;

// Pandam Actor
public class Panctor extends BasePantity implements SpecPanctor {
	private final Panplementation impl;
	private final Pantype type;
	private final MinPanple min;
	private final MaxPanple max;
	private CenterPanple center = null;
	private boolean destroyed = false;
	/*package*/ Object collisionGroup = Panroom.defaultCollisionGroup;
	/*package*/ Panlayer layer = null;
	
	public Panctor() {
		this(Pantil.vmid());
	}

	public Panctor(final String id) {
		super(id);
		final Pangine engine = Pangine.getEngine();
		impl = engine.newImplementation(this);
		//type = engine.getType(getClass());
		final Class<? extends Panctor> c = getClass();
		Pantype t = engine.getType(c);
		if (t == null) {
			t = engine.createType(Panctor.class.getName() + ".auto." + c.getName(), c, (Panmage) null);
		}
		type = t;
		impl.setView(type.getView());
		min = new MinPanple();
		max = new MaxPanple();
		
		boundPos = getPosition();
	}

	// Can be overridden to prepare for overridden renderView/getCurrentView
	protected void updateView() {
		impl.updateView();
	}

	// Can be overridden to provide bounding box information
	@Override
	public Pansplay getCurrentDisplay() {
		return impl.getCurrentDisplay();
	}
	
	public final Object getCurrentDisplayExtra() {
	    return Panmage.getExtra(getCurrentDisplay());
	}

	protected void renderView(final Panderer renderer) {
		impl.renderView();
	}

	/*package*/ final Panplementation getImplementation() {
		return impl;
	}

	public final Pantype getType() {
		return type;
	}

	public final Panview getView() {
		return impl.getView();
	}

	@Override
	public final void setView(final Panimation view) {
		impl.setView(view);
	}

	@Override
	public final void setView(final Panmage view) {
		impl.setView(view);
	}
	
	@Override
	public final void setView(final Panframe view) {
        impl.setView(view);
    }
	
	@Override
	public final void setView(final Panctor actor) {
	    impl.setView(actor.impl);
	}
	
	@Override
	public final boolean changeView(final Panimation view) {
        return impl.changeView(view);
    }

	@Override
    public final boolean changeView(final Panmage view) {
        return impl.changeView(view);
    }
    
	@Override
    public final boolean changeView(final Panframe view) {
        return impl.changeView(view);
    }

	@Override
	public final Panple getPosition() {
		return impl.getPosition();
	}
	
    private final Panple boundPos; // = getPosition();
    private boolean boundMirror;
    private boolean boundFlip;
    private int boundRot;
    //private Pansplay boundDisplay;
    private Panple boundMin;
    private Panple boundMax;
    private long boundClock = -1;
    private Panview boundView = null;
	    
    private final void initBound() {
        // Might be better to add modCount to Panple and check for pos/min/max changes
        final long ec = Pangine.getEngine().getClock();
        final Panview view = impl.getView();
        if ((ec == boundClock) && (view == boundView)) {
            return;
        }
        boundClock = ec;
        boundView = view;
        final boolean fmir, fflp;
        final int frot;
        final Panple fmin, fmax;
        Panframe frame = null;
        if (view instanceof Panimation) {
            frame = ((Panimation) view).getFrames()[getCurrentFrame()];
        } else if (view instanceof Panframe) {
            frame = (Panframe) view;
        }
        if (frame != null) {
            fmir = frame.isMirror();
            fflp = frame.isFlip();
            frot = frame.getRot();
            fmin = frame.getBoundingMinimum(); // Need to check current display anyway, so don't use effective boundaries
            fmax = frame.getBoundingMaximum();
        } else {
            fmir = false;
            fflp = false;
            frot = 0;
            fmin = null;
            fmax = null;
        }
        boundMirror = isMirror() ^ fmir;
        boundFlip = isFlip() ^ fflp;
        boundRot = (getRot() + frot) % 4;
        final Pansplay boundDisplay = getCurrentDisplay();
        if (boundDisplay == null) {
            throw new NullPointerException(getClass() + " boundary needed, but no display found");
        }
        boundMin = fmin == null ? boundDisplay.getBoundingMinimum() : fmin;
        boundMax = fmax == null ? boundDisplay.getBoundingMaximum() : fmax;
    }
	
	private final class MinPanple extends UnmodPanple {
        @Override
        public float getX() {
            //return pos.getX() + getCurrentDisplay().getBoundingMinimum().getX();
            
            //final Pansplay display = getCurrentDisplay();
            //return pos.getX() + display.getBoundingMinimum().getX() - display.getOrigin().getX();
            
            /*
            Do we need to account for origin here?
            pos is the position of the origin.
            If boundaries are relative to the origin,
            then we should just offset the position by the boundaries.
            We wouldn't need to consider the origin
            unless the boundaries are relative to pixel (0, 0) of the image.
            
            changes.txt - b-0006
            Added Panctor/SpecPanctor.getBoundingMin/Maximum
            Modified Pangine.isCollision to use Panctor.getBoundingMin/Maximum (now uses Pansplay.origin)
            
            So at that point it seemed like an improvement.
            
            The Panmage constructor should be some of the oldest code.
            When imputing the default boundaries, they were relative to the origin:
            new FinPanple(-this.origin.getX(), -this.origin.getY(), -this.origin.getZ())
            So it shouldn't be necessary to account for the origin again here.
            
            See Panmage constructor comments.
            */
            
            initBound();
            /*if (boundMirror) {
                //return boundPos.getX() + boundDisplay.getOrigin().getX() - boundDisplay.getBoundingMaximum().getX();
                return boundPos.getX() - boundMax.getX();
            } else {
                //return boundPos.getX() + boundDisplay.getBoundingMinimum().getX() - boundDisplay.getOrigin().getX();
                return boundPos.getX() + boundMin.getX();
            }*/
            final float off;
            switch (boundRot % 4) {
                case 0 :
                    off = boundMirror ? -boundMax.getX() : boundMin.getX();
                    break;
                case 1 : // counter-clockwise
                    off = boundMirror ? boundMin.getY() : -boundMax.getY();
                    break;
                case 2 :
                    off = boundMirror ? boundMin.getX() : -boundMax.getX();
                    break;
                default :
                    off = boundMirror ? -boundMax.getY() : boundMin.getY();
                    break;
            }
            return boundPos.getX() + off;
        }

        @Override
        public float getY() {
            //final Pansplay display = getCurrentDisplay();
            //return boundPos.getY() + boundDisplay.getBoundingMinimum().getY() - boundDisplay.getOrigin().getY();
            //return boundPos.getY() + boundMin.getY();
            final float off;
            switch (boundRot % 4) {
                case 0 :
                    off = boundFlip ? -boundMax.getY() : boundMin.getY();
                    break;
                case 1 : // counter-clockwise
                    off = boundFlip ? -boundMax.getX() : boundMin.getX();
                    break;
                case 2 :
                    off = boundFlip ? boundMin.getY() : -boundMax.getY();
                    break;
                default :
                    off = boundFlip ? boundMin.getX() : -boundMax.getX();
                    break;
            }
            return boundPos.getY() + off;
        }

        @Override
        public float getZ() {
            //final Pansplay display = getCurrentDisplay();
            //return boundPos.getZ() + boundDisplay.getBoundingMinimum().getZ() - boundDisplay.getOrigin().getZ();
            return boundPos.getZ() + boundMin.getZ();
        }
	}
	
	@Override
	public final Panple getBoundingMinimum() {
	    return min;
    }
	
	private final class MaxPanple extends UnmodPanple {
        @Override
        public float getX() {
            //return pos.getX() + getCurrentDisplay().getBoundingMaximum().getX();
            
            //final Pansplay display = getCurrentDisplay();
            //return pos.getX() + display.getBoundingMaximum().getX() - display.getOrigin().getX();
            
            initBound();
            /*if (boundMirror) {
                //return boundPos.getX() + boundDisplay.getOrigin().getX() - boundDisplay.getBoundingMinimum().getX();
                return boundPos.getX() - boundMin.getX();
            } else {
                //return boundPos.getX() + boundDisplay.getBoundingMaximum().getX() - boundDisplay.getOrigin().getX();
                return boundPos.getX() + boundMax.getX();
            }*/
            final float off;
            switch (boundRot % 4) {
                case 0 :
                    off = boundMirror ? -boundMin.getX() : boundMax.getX();
                    break;
                case 1 : // counter-clockwise
                    off = boundMirror ? boundMax.getY() : -boundMin.getY();
                    break;
                case 2 :
                    off = boundMirror ? boundMax.getX() : -boundMin.getX();
                    break;
                default :
                    off = boundMirror ? -boundMin.getY() : boundMax.getY();
                    break;
            }
            return boundPos.getX() + off;
        }

        @Override
        public float getY() {
            //final Pansplay display = getCurrentDisplay();
            //return boundPos.getY() + boundDisplay.getBoundingMaximum().getY() - boundDisplay.getOrigin().getY();
            //return boundPos.getY() + boundMax.getY();
            final float off;
            switch (boundRot % 4) {
                case 0 :
                    off = boundFlip ? -boundMin.getY() : boundMax.getY();
                    break;
                case 1 : // counter-clockwise
                    off = boundFlip ? -boundMin.getX() : boundMax.getX();
                    break;
                case 2 :
                    off = boundFlip ? boundMax.getY() : -boundMin.getY();
                    break;
                default :
                    off = boundFlip ? boundMax.getX() : -boundMin.getX();
                    break;
            }
            return boundPos.getY() + off;
        }

        @Override
        public float getZ() {
            //final Pansplay display = getCurrentDisplay();
            //return boundPos.getZ() + boundDisplay.getBoundingMaximum().getZ() - boundDisplay.getOrigin().getZ();
            return boundPos.getZ() + boundMax.getZ();
        }
    }

	@Override
    public final Panple getBoundingMaximum() {
	    return max;
    }
	
	private final class CenterPanple extends UnmodPanple {
        @Override
        public float getX() {
            return (getBoundingMinimum().getX() + getBoundingMaximum().getX()) / 2.0f;
        }
        
        @Override
        public float getY() {
            return (getBoundingMinimum().getY() + getBoundingMaximum().getY()) / 2.0f;
        }
        
        @Override
        public float getZ() {
            return (getBoundingMinimum().getZ() + getBoundingMaximum().getZ()) / 2.0f;
        }
	}
	
	@Override
	public final Panple getBoundingCenter() {
	    if (center == null) {
	        center = new CenterPanple();
	    }
	    return center;
	}

	@Override
	public final boolean isVisible() {
		return impl.isVisible();
	}

	@Override
	public final void setVisible(final boolean vis) {
		impl.setVisible(vis);
	}
	
	// rot = number of counter-clockwise 90-degree rotations, see setRot
	@Override
	public final int getRot() {
	    return impl.getRot();
	}
	
	/*
	rot = number of counter-clockwise 90-degree rotations.
	1 = 90, 2 = 180, 3 = 270.
	Clockwise rotations might be more intuitive in some cases.
	But geometry usually goes counter-clockwise.
	(1, 0) = 0, (0, 1) = 90, (-1, 0) = 180, (0, -1) = 270
	*/
	@Override
	public final void setRot(final int rot) {
	    impl.setRot(rot);
	}
	
	@Override
	public final boolean isMirror() {
        return impl.isMirror();
    }

	@Override
    public final void setMirror(final boolean mirror) {
        impl.setMirror(mirror);
    }
    
	@Override
    public final int getMirrorMultiplier() {
        return getMirrorMultiplier(isMirror());
    }
    
    public final static int getMirrorMultiplier(final boolean mirror) {
        return mirror ? -1 : 1;
    }
    
    @Override
    public final boolean isFlip() {
        return impl.isFlip();
    }

    @Override
    public final void setFlip(final boolean flip) {
        impl.setFlip(flip);
    }
    
    public final int getCurrentFrame() {
        return impl.currFrame;
    }
    
    public final int getCurrentFrameDur() {
        return impl.currFrameDur;
    }
	
    @Override
	public final Panlayer getLayer() {
	    return layer;
	}
	
	public final void register(final ActionStartListener listener) {
		Pangine.getEngine().getInteraction().register(this, listener);
	}
	
	public final void register(final Panput input, final ActionStartListener listener) {
		Pangine.getEngine().getInteraction().register(this, input, listener);
	}
	
	public final void register(final Panput[] inputs, final ActionStartListener listener) {
        for (final Panput input : inputs) {
            register(input, listener);
        }
    }
	
	public final void register(final Panput input, final ActionListener listener) {
		Pangine.getEngine().getInteraction().register(this, input, listener);
	}
	
	public final void register(final Panput[] inputs, final ActionListener listener) {
        for (final Panput input : inputs) {
            register(input, listener);
        }
    }
	
	public final void register(final ActionEndListener listener) {
		Pangine.getEngine().getInteraction().register(this, listener);
	}
	
	public final void register(final Panput input, final ActionEndListener listener) {
		Pangine.getEngine().getInteraction().register(this, input, listener);
	}
	
	public final void register(final Panput[] inputs, final ActionEndListener listener) {
        for (final Panput input : inputs) {
            register(input, listener);
        }
    }
	
	public final void register(final long duration, final TimerListener listener) {
		Pangine.getEngine().addTimer(this, duration, listener);
	}
	
	public final void unregisterListeners() {
		Pangine.getEngine().getInteraction().unregister(this);
	}

	@Override
	public final void destroy() {
	    if (destroyed) {
	        return;
	    }
	    onDestroy();
	    unregisterListeners();
		//Pangame.getGame().getCurrentRoom().removeActor(this);
	    detachIntern();
		destroyed = true;
	}
	
	protected void onDestroy() {
	}

	//TODO transform is messy; what if the Panctor instance always stayed the same;
	// what if Panctor had a state field and that's what we changed?
	// We'd need to move most Panctor things into there.
	@Override
	public final boolean isDestroyed() {
		return destroyed;
	}
	
	@Override
	public final void detach() {
	    onDetach();
	    detachIntern();
	}
	
	private final void detachIntern() {
        if (layer != null) {
            layer.removeActor(this);
            layer = null;
        }
    }
	
	protected void onDetach() {
    }
	
	public final boolean isActive() {
        final Panlayer layer = getLayer();
        return !isDestroyed() && (layer != null) && layer.isActive();
    }
	
	@Override
	public final boolean isInView() {
		final Panlayer layer = getLayer();
		if (layer == null) {
			return false;
		}
		final Panple vn = layer.getViewMinimum(), vx = layer.getViewMaximum();
		final Panple bn = getBoundingMinimum(), bx = getBoundingMaximum();
		if (bn.getX() > vx.getX()) {
			return false;
		} else if (bx.getX() < vn.getX()) {
			return false;
		} else if (bn.getY() > vx.getY()) {
			return false;
		} else if (bx.getY() < vn.getY()) {
			return false;
		}
		return true;
	}

	public final <P extends Panctor> P transform(final Class<P> newClass) {
		final Pangine engine = Pangine.getEngine();
		engine.unregister(this);
		destroy();
		final P newActor = engine.createActor(newClass, getId());
		newActor.getPosition().set(getPosition());
		Pangame.getGame().getCurrentRoom().addActor(newActor);
		return newActor;
	}
	
	public final void swapPositions(final Panctor a2) {
		final Panple p1 = getPosition(), p2 = a2.getPosition();
		final float x = p1.getX(), y = p1.getY(), z = p1.getZ();
		p1.set(p2);
		p2.set(x, y, z);
	}

	/*
	@Override
	public final boolean equals(final Object o) {
		return super.equals(o); // Same as super, but final so can't be overridden
	}

	@Override
	public final int hashCode() {
		return super.hashCode(); // Same as super, but final so can't be overridden
	}
	*/
	
	public final static boolean isActive(final Panctor actor) {
		return actor != null && actor.isActive();
	}
	
	public final static void destroy(final Panctor actor) {
		if (actor != null) {
			actor.destroy();
		}
	}
	
	public final static void destroy(final Collection<? extends Panctor> actors) {
		if (actors == null) {
			return;
		}
		for (final Panctor actor : actors) {
			destroy(actor);
		}
		actors.clear();
	}
	
	public final static void destroy(final Panctor[] actors) {
	    if (actors == null) {
	        return;
	    }
	    for (final Panctor actor : actors) {
	        destroy(actor);
	    }
	    Coltil.wipe(actors);
	}
	
	public final static boolean isDestroyed(final SpecPanctor actor) {
	    return (actor == null) || actor.isDestroyed();
	}
	
	public final static void detach(final SpecPanctor actor) {
        if (actor != null) {
            actor.detach();
        }
    }
	
	public final static void detach(final Collection<? extends SpecPanctor> actors) {
		if (actors == null) {
			return;
		}
		for (final SpecPanctor actor : actors) {
			detach(actor);
		}
	}
	
	public final static void detach(final SpecPanctor... actors) {
        if (actors == null) {
            return;
        }
        for (final SpecPanctor actor : actors) {
            detach(actor);
        }
    }
	
	public final static boolean isAttached(final SpecPanctor actor) {
	    return !isDestroyed(actor) && (actor.getLayer() != null);
	}
	
	public final static void setInvisible(final Panctor actor) {
		if (actor != null) {
			actor.setVisible(false);
		}
	}
	
	public final static boolean isVisible(final Panctor actor) {
	    return (actor != null) && actor.isVisible();
	}
}
