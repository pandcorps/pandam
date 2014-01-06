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
package org.pandcorps.game.actor;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;

// A Guy that can look in 2 directions, left or right
public abstract class Guy2 extends Panctor implements StepListener {
	protected final static byte MODE_STILL = 0;
	protected final static byte MODE_WALK = 1;
    
	private final static float speed = 2;
	
	//private static boolean shadowVisible = true;
    private static byte shadowTimer = 0;
	
	protected final Decoration shadow;
    protected byte mode = MODE_STILL;
    protected float dx = 0;
    protected float dy = 0;
    protected Guy2Controller controller = null;
    
    public final static class Guy2Type {
    	private final Panmage shadowImage;
    	private final int depthShadow;
    	
    	public Guy2Type(final Panmage shadowImage, final int depthShadow) {
    		this.shadowImage = shadowImage;
    		this.depthShadow = depthShadow;
    	}
    	
    	public final int getDepthShadow() {
    		return depthShadow;
    	}
    }
	
	protected Guy2(final String id, final Panlayer room, final Guy2Type type) {
		super(id);
		shadow = new Decoration(id + ".shadow");
        shadow.setView(type.shadowImage);
        shadow.getPosition().setZ(type.depthShadow);
        attach(room);
	}
	
	@Override
	protected void onDetach() {
	    shadow.detach();
	}
	
	public void attach(final Panlayer room) {
	    room.addActor(shadow);
        room.addActor(this);
	}
	
	@Override
    public void onStep(final StepEvent event) {
		if (controller == null) {
			mode = MODE_STILL;
		} else {
			controller.step();
		}
        final Panple pos = getPosition();
        switch(mode) {
            case MODE_WALK :
                /*
                 * We don't want to walk while attacking.
                 * If we register an attack input event after a walk input event,
                 * we don't want to process the walk event.
                 * So we put mirror/position logic here, after all events have been processed.
                 */
                if (dx != 0) {
                    setMirror(dx < 0);
                }
                //pos.add(dx, dy);
                //final Panple roomSize = Pangame.getGame().getCurrentRoom().getSize();
                //pos.add(dx, dy, 0, 0, roomSize.getX(), roomSize.getY());
                //final Background background = FightGame.getBackground();
                //pos.add(dx, dy, background.minX, background.minY, background.maxX, background.maxY);
                // move can change pos, evaluate below
                changeView(getWalk());
                mode = MODE_STILL;
                break;
            case MODE_STILL :
                changeView(getStill());
                break;
            default :
            	handleMode(mode);
            	break;
        }
        final Panple min = getMin(), max = getMax();
        pos.add(dx, dy, min.getX(), min.getY(), max.getX(), max.getY());
        setZ(pos);
        //shadow.getPosition().set(pos);
        shadow.getPosition().set(pos.getX(), pos.getY());
        shadow.setVisible(areShadowsVisible());
        shadow.setMirror(isMirror());
        dx = 0;
        dy = 0;
    }
	
	public final static void setZ(final Panple pos) {
		pos.setZ(-pos.getY());
	}
	
	protected void handleMode(final byte mode) {
		// Can override
	}
	
	protected abstract Panimation getStill();
	
	protected abstract Panimation getWalk();
	
	protected abstract Panple getMin();
	
	protected abstract Panple getMax();
	
	public final void walkDown() {
        walk(0, -speed);
    }
    
	public final void walkUp() {
        walk(0, speed);
    }
    
	public final void walkLeft() {
        walk(-speed, 0);
        //setMirror(true);
    }
    
	public final void walkRight() {
        walk(speed, 0);
        //setMirror(false);
    }
    
    private final void walk(final float dx, final float dy) {
        if (isFree()) {
            this.dx += dx;
            this.dy += dy;
            //getPosition().add(dx, dy);
            mode = MODE_WALK;
        }
    }
    
    protected final boolean isFree() {
        return mode == MODE_STILL || mode == MODE_WALK;
    }
    
    public final static void step() {
    	//shadowVisible = !shadowVisible;
        shadowTimer++;
        if (shadowTimer > 4) {
            shadowTimer = 0;
        }
    }
    
    public final static boolean areShadowsVisible() {
        //return shadowVisible;
        //return shadowTimer <= 1;
        return shadowTimer == 0;
    }
    
    public Guy2Controller getController() {
    	return controller;
    }
    
    public float getSpeed() {
        return speed;
    }
    
    public final void add(final Panctor actor, final float xo, final float yo, final float zo) {
        final Panple pos = getPosition();
        final int mult = isMirror() ? -1 : 1;
        actor.getPosition().set(pos.getX() + (mult * xo), pos.getY() + yo, pos.getZ() + zo);
        Pangame.getGame().getCurrentRoom().addActor(actor);
    }
    
    @Override
    protected void onDestroy() {
    	shadow.destroy();
    }
}
