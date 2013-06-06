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
package org.pandcorps.platform;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.ImplPanple;
import org.pandcorps.pandax.tile.*;

public class Player extends Character implements CollisionListener {
	protected final static int PLAYER_X = 7;
	protected final static int PLAYER_H = 23; // 15
    private final static int VEL_WALK = 3;
	private final static int VEL_RETURN = 2;
	private final static int VEL_JUMP = 8;
	protected final static int VEL_BUMP = 4;
	private final static byte MODE_NORMAL = 0;
	private final static byte MODE_RETURN = 1;
	private final static byte JUMP_HIGH = 1;
	//private final static byte JUMP_DOUBLE = 2;
	//private final static byte JUMP_INFINITE = 3;
	private final static byte JUMP_FLY = 4;
	
	// Player attributes preserved between levels
	public final static class PlayerContext {
	    protected transient Player player = null;
	    
	    private String name = null;
	    private int gems = 0;
	    
	    protected Panput inJump = null;
	    protected Panput inLeft = null;
	    protected Panput inRight = null;
	    
	    protected Panmage guy = null;
	    protected Panimation guyRun = null;
	    protected Panmage guyJump = null;
	    protected Panimation guySouth = null;
	    protected Panimation guyEast = null;
	    protected Panimation guyWest = null;
	    protected Panimation guyNorth = null;
	    
	    public PlayerContext(final String name) {
	        this.name = name;
	    }
	    
	    public final String getName() {
	        return name;
	    }
	    
	    public final int getGems() {
	        return gems;
	    }
	}
	
	protected final PlayerContext pc;
	private byte mode = MODE_NORMAL;
	private byte jumpMode = MODE_NORMAL;
	private boolean flying = false;
	private final Panple safe = new ImplPanple(0, 0, 0);
	private int levelGems = 0;
	private int hurtTimer = 0;
	private final Bubble bubble = new Bubble();
	
	public Player(final PlayerContext pc) {
		super(PLAYER_X, PLAYER_H);
	    this.pc = pc;
	    pc.player = this;
		final Pangine engine = Pangine.getEngine();
		setView(pc.guy);
		PlatformGame.room.addActor(bubble);
		final Panteraction interaction = engine.getInteraction();
		interaction.register(this, pc.inJump, new ActionStartListener() {
			@Override public final void onActionStart(final ActionStartEvent event) { jump(); }});
		interaction.register(this, pc.inJump, new ActionEndListener() {
			@Override public final void onActionEnd(final ActionEndEvent event) { releaseJump(); }});
		interaction.register(this, pc.inRight, new ActionListener() {
			@Override public final void onAction(final ActionEvent event) { right(); }});
		interaction.register(this, pc.inLeft, new ActionListener() {
			@Override public final void onAction(final ActionEvent event) { left(); }});
		
		// Debug
		interaction.register(this, interaction.KEY_1, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { left(); }});
		interaction.register(this, interaction.KEY_2, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { right(); }});
		interaction.register(this, interaction.KEY_9, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { addX(-1); }});
        interaction.register(this, interaction.KEY_0, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { addX(1); }});
        interaction.register(this, interaction.KEY_Q, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { jumpMode = MODE_NORMAL; }});
        interaction.register(this, interaction.KEY_W, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { jumpMode = JUMP_HIGH; }});
        interaction.register(this, interaction.KEY_E, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { jumpMode = JUMP_FLY; }});
	}
	
	private final void jump() {
	    if (jumpMode == JUMP_FLY) {
	        flying = true;
	        addV(-g);
	        return;
	    } else if (isGrounded()) {
			v = jumpMode == JUMP_HIGH ? MAX_V : VEL_JUMP;
		}
	}
	
	private final void releaseJump() {
	    if (jumpMode == JUMP_FLY) {
            flying = false;
            return;
        } else if (v > 0) {
			v = 0;
		}
	}
	
	private final void right() {
		hv = VEL_WALK;
	}
	
	private final void left() {
		hv = -VEL_WALK;
	}
	
	private boolean isInvincible() {
		return hurtTimer > 0 || mode == MODE_RETURN;
	}
	
	private final void onStepReturn() {
		final Panple pos = getPosition();
		final Panple diff = Panple.subtract(safe, pos);
		final double dist = diff.getMagnitude();
		if (dist <= VEL_RETURN) {
			pos.set(safe);
			mode = MODE_NORMAL;
			if (!isGrounded()) {
				/*
				Previously safe spot might have been a block that was broken after player left it
				(or causing player to leave it).
				So if the safe spot is no longer safe, bounce the player to give a chance to find a new safe spot.
				Still might be possible, though.
				So if we reach this point twice in a row, we might want to do something more drastic,
				like allowing the player to float until a new safe spot is found.
				*/
				jump();
			}
			return;
		}
		diff.multiply((float) (VEL_RETURN / dist));
		pos.add(diff);
	}
	
	@Override
	protected final boolean onStepCustom() {
	    if (hurtTimer > 0) {
	        hurtTimer--;
	    }
		if (mode == MODE_RETURN) {
			onStepReturn();
			return true;
		}
		return false;
	}

	@Override
	protected final void onCollide(final Tile tile) {
		final TileOccupant o = Tile.getOccupant(tile);
		if (o == null) {
			return;
		}
		((Gem) o).onCollide(this);
	}
	
	public final int getCurrentLevelGems() {
        return levelGems;
    }
	
	public final void addGem() {
        levelGems++;
    }
	
	@Override
	protected final void onStepping() {
		if (flying) {
		    if (jumpMode != JUMP_FLY) {
		        flying = false;
		    } else {
		        addV(-g);
		    }
		}
	}
	
	@Override
	protected final void onStepEnd() {
		hv = 0;
		final Panple pos = getPosition();
		PlatformGame.setPosition(bubble, pos.getX(), pos.getY() - 1, PlatformGame.DEPTH_BUBBLE);
		bubble.setVisible(isInvincible() && Pangine.getEngine().isOn(4));
	}
	
	@Override
	protected final void onGrounded() {
		safe.set(getPosition());
		if (hv != 0) {
			changeView(pc.guyRun);
		} else {
			changeView(pc.guy);
		}
	}
	
	@Override
	protected final boolean onAir() {
		changeView(pc.guyJump);
		return flying;
	}
	
	@Override
	public void onCollision(final CollisionEvent event) {
		final Collidable other = event.getCollider();
		if (other instanceof Enemy) {
			if (v < 0 && getPosition().getY() > other.getPosition().getY()) {
				((Enemy) other).onStomp();
				v = VEL_BUMP;
			} else if (!isInvincible()) {
				onHurt();
				hurtTimer = 60; // Enable temporary invincibility
			}
		}
	}
	
	public final void onHurt() {
        if (levelGems == 0) {
            return;
        }
        levelGems -= (Math.max(1, levelGems / 10));
    }
	
	@Override
	protected final void onBump() {
		if (v <= 0) {
			v = VEL_BUMP;
		}
	}
	
	@Override
	protected final void onFell() {
		if (jumpMode != JUMP_FLY) {
			onHurt();
			mode = MODE_RETURN;
			return;
		}
	}
	
	public final void onFinishLevel() {
		pc.gems += levelGems;
	}
	
	@Override
	protected final void onDestroy() {
		bubble.destroy();
	}
	
	private final static class Bubble extends Panctor {
		{
			setView(PlatformGame.bubble);
		}
	}
}
