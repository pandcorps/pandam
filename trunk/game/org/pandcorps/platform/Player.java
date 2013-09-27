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
import org.pandcorps.pandam.Panteraction.Device;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.ImplPanple;
import org.pandcorps.pandax.in.ControlScheme;
import org.pandcorps.pandax.tile.*;

public class Player extends Character implements CollisionListener {
	protected final static int PLAYER_X = 7;
	protected final static int PLAYER_H = 23; // 15
    private final static int VEL_WALK = 3;
	private final static int VEL_RETURN = 2;
	private final static int VEL_CATCH_UP = 8;
	private final static int VEL_JUMP = 8;
	protected final static int VEL_BUMP = 4;
	protected final static byte MODE_NORMAL = 0;
	private final static byte MODE_RETURN = 1;
	private final static byte JUMP_HIGH = 1;
	//private final static byte JUMP_DOUBLE = 2;
	//private final static byte JUMP_INFINITE = 3;
	protected final static byte JUMP_FLY = 4;
	
	public static class PlayerData {
	    private String name = null;
	    
	    public final String getName() {
            return name;
        }
	    
	    public final void setName(final String name) {
            this.name = name;
        }
	    
	    public final static String getName(final PlayerData data) {
	        return data == null ? null : data.getName();
	    }
	}
	
	// Player attributes preserved between levels
	public final static class PlayerContext {
	    protected final Profile profile;
	    protected final int index;
	    protected Player player = null;
	    
	    protected final ControlScheme ctrl;
	    
	    protected Panimation guy = null;
	    protected Panimation guyRun = null;
	    protected Panmage guyJump = null;
	    protected Panmage guyFall = null;
	    protected Panimation mapSouth = null;
	    protected Panimation mapEast = null;
	    protected Panimation mapWest = null;
	    protected Panimation mapNorth = null;
	    protected Panimation mapLadder = null;
	    protected Panmage mapPose = null;
	    protected Panmage back = null;
	    protected Panimation backJump = null;
	    protected Panimation backFall = null;
	    
	    public PlayerContext(final Profile profile, final ControlScheme ctrl, final int index) {
	        this.profile = profile;
	        this.index = index;
	        
	        this.ctrl = ctrl;
	        /*ctrl = new ControlScheme();
	        final Panteraction interaction = Pangine.getEngine().getInteraction();
	        if (profile.ctrl == 0) {
	            ctrl.setDefaultKeyboard();
	        } else {
	            final Panteraction.Controller c = Coltil.get(interaction.CONTROLLERS, 0);
	            if (c == null) {
	                ctrl.set1(interaction.KEY_2);
	                ctrl.set2(interaction.KEY_1);
	                ctrl.setSubmit(interaction.KEY_3);
	                ctrl.setDown(interaction.KEY_S);
	                ctrl.setUp(interaction.KEY_W);
	                ctrl.setLeft(interaction.KEY_A);
	                ctrl.setRight(interaction.KEY_D);
	            } else {
	                ctrl.setDefault(c);
	            }
	        }*/
	    }
	    
	    public final String getName() {
	        return profile.currentAvatar.getName();
	    }
	    
	    public final int getGems() {
	        return profile.gems;
	    }
	    
	    public final Device getDevice() {
	        return ctrl.get1().getDevice();
	    }
	    
	    public final void onFinishLevel() {
			profile.gems += player.levelGems;
			profile.stats.defeatedEnemies += player.levelDefeatedEnemies;
			profile.stats.defeatedLevels++;
		}
		
		public final void onFinishWorld() {
			profile.stats.defeatedWorlds++;
		}
	    
	    public final void destroy() {
	    	if (guy == null) {
	    		return;
	    	}
	    	guy.destroyAll();
	    	guyRun.destroyAll();
	    	guyJump.destroy();
	    	guyFall.destroy();
	    	mapSouth.destroyAll();
	    	mapEast.destroyAll();
	    	mapWest.destroyAll();
	    	mapNorth.destroyAll();
	    	mapLadder.destroyAll();
	    	mapPose.destroy();
	    	back.destroy();
	    	backJump.destroyAll();
	    	backFall.destroyAll();
	    }
	}
	
	protected final PlayerContext pc;
	private byte mode = MODE_NORMAL;
	private byte jumpMode = MODE_NORMAL;
	private boolean flying = false;
	private final Panple safe = new ImplPanple(0, 0, 0);
	private boolean safeMirror = false;
	private Panple returnDestination = null;
	private int returnVelocity = 0;
	private Player returnPlayer = null;
	private int levelGems = 0;
	protected int levelDefeatedEnemies = 0;
	private int hurtTimer = 0;
	private int stompTimer = 0;
	private int activeTimer = 0;
	private final Bubble bubble = new Bubble();
	private Panctor back = null;
	
	public Player(final PlayerContext pc) {
		super(PLAYER_X, PLAYER_H);
	    this.pc = pc;
	    pc.player = this;
	    jumpMode = pc.profile.currentAvatar.jumpMode;
		final Pangine engine = Pangine.getEngine();
		setView(pc.guy);
		PlatformGame.room.addActor(bubble);
		if (jumpMode == JUMP_FLY) {
		    back = new Panctor();
		    back.setView(pc.back);
		    PlatformGame.room.addActor(back);
		}
		final Panteraction interaction = engine.getInteraction();
		final ControlScheme ctrl = pc.ctrl;
		register(ctrl.get1(), new ActionStartListener() {
			@Override public final void onActionStart(final ActionStartEvent event) { jump(); }});
		register(ctrl.get1(), new ActionEndListener() {
			@Override public final void onActionEnd(final ActionEndEvent event) { releaseJump(); }});
		register(ctrl.getRight(), new ActionListener() {
			@Override public final void onAction(final ActionEvent event) { right(); }});
		register(ctrl.getLeft(), new ActionListener() {
			@Override public final void onAction(final ActionEvent event) { left(); }});
		
		// Debug
		register(interaction.KEY_1, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { left(); }});
		register(interaction.KEY_2, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { right(); }});
		register(interaction.KEY_9, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { addX(-1); }});
        register(interaction.KEY_0, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { addX(1); }});
        register(interaction.KEY_Q, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { jumpMode = MODE_NORMAL; }});
        register(interaction.KEY_W, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { jumpMode = JUMP_HIGH; }});
        register(interaction.KEY_E, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { jumpMode = JUMP_FLY; }});
	}
	
	private final void jump() {
	    if (jumpMode == JUMP_FLY) {
	        flying = true;
	        addV(-g);
	        return;
	    } else if (isGrounded()) {
			v = jumpMode == JUMP_HIGH ? MAX_V : VEL_JUMP;
			pc.profile.stats.jumps++;
			Pangine.getEngine().getMusic().playSound(Music.jump);
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
	
	private boolean isReturningFromScroll() {
		return mode == MODE_RETURN && returnPlayer != null;
	}
	
	private final static Player getActive() {
		Player a = null;
		for (final PlayerContext pc : PlatformGame.pcs) {
			final Player c = pc.player;
			// Tie breaker?
			if (!c.isReturningFromScroll() && a == null || a.activeTimer < c.activeTimer) {
				a = c;
			}
		}
		return a;
	}
	
	private final void startReturn(final Panple dst, final int vel, final Player player) {
	    v = 0;
	    hv = 0;
		mode = MODE_RETURN;
		returnDestination = dst;
		returnVelocity = vel;
		returnPlayer = player;
	}
	
	private final void startSafety() {
	    startReturn(safe, VEL_RETURN, null);
	    setMirror(safeMirror);
	}
	
	private final void startCatchUp(final Player active) {
		startReturn(active.getPosition(), VEL_CATCH_UP, active);
		safe.set(active.safe);
		safeMirror = active.safeMirror;
	}
	
	private final void onStepReturn() {
		final Panple pos = getPosition();
		final Panple diff = Panple.subtract(returnDestination, pos);
		final double dist = diff.getMagnitude();
		if (dist <= returnVelocity) {
		    if (returnPlayer != null) {
		        if (returnPlayer.mode == MODE_RETURN) {
		            startSafety();
		            return;
		        } else {
		            setMirror(returnPlayer.isMirror());
		        }
		    }
			pos.set(returnDestination);
			startReturn(null, 0, null);
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
				//jump(); // Only works if grounded
			    v = MAX_V;
			}
			return;
		}
		diff.multiply((float) (returnVelocity / dist));
		pos.add(diff);
	}
	
	@Override
	protected final boolean onStepCustom() {
	    if (hurtTimer > 0) {
	        hurtTimer--;
	    }
	    if (stompTimer > 0) {
	        stompTimer--;
	    }
		if (mode == MODE_RETURN) {
			onStepReturn();
			return true;
		}
		if (hv == 0) {
			if (activeTimer > 0) {
				activeTimer -= Math.max(1, activeTimer / 2);
			}
		} else {
			activeTimer++;
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
	protected final void onScrolled() {
		final Player active = getActive();
		if (active == null) {
			return;
		} else if (this == active) {
			for (final PlayerContext pc : PlatformGame.pcs) {
				final Player other = pc.player;
				if (other != this) {
					other.startCatchUp(this);
				}
			}
		} else {
			startCatchUp(active);
		}
	}
	
	@Override
	protected final void onStepEnd() {
		hv = 0;
		final Panple pos = getPosition();
		PlatformGame.setPosition(bubble, pos.getX(), pos.getY() - 1, PlatformGame.DEPTH_BUBBLE);
		bubble.setVisible(isInvincible() && Pangine.getEngine().isOn(4));
		if (back != null) {
		    PlatformGame.setPosition(back, pos.getX(), pos.getY(), PlatformGame.DEPTH_PLAYER_BACK);
		    back.setMirror(isMirror());
		}
	}
	
	@Override
	protected final void onGrounded() {
		safe.set(getPosition());
		safeMirror = isMirror();
		if (hv != 0) {
			changeView(pc.guyRun);
		} else {
			changeView(pc.guy);
		}
		if (back != null) {
			back.changeView(pc.back);
		}
	}
	
	@Override
	protected final boolean onAir() {
		changeView(v > 0 ? pc.guyJump : pc.guyFall);
		if (back != null) {
			back.changeView(flying ? pc.backJump : pc.backFall); // v > 0 doesn't flap as soon as jump is pressed
		}
		return flying;
	}
	
	@Override
	public void onCollision(final CollisionEvent event) {
		final Collidable other = event.getCollider();
		if (other instanceof Enemy) {
		    /*if (other.isDestroyed()) { // Might happen if two Players stomp same Enemy at same time
		        return; // But this is handled in Pangine
		    }*/
		    final boolean aboveEnemy = getPosition().getY() > other.getPosition().getY();
		    if (aboveEnemy && v < 0) {
				((Enemy) other).onStomp(this);
				v = VEL_BUMP;
				stompTimer = 2;
		    } else if (aboveEnemy && stompTimer > 0) {
		        /*
		        This Player just stomped two Enemies at the same time.
		        The first one was already processed, causing the bounce.
		        So the Player is no longer falling.
		        But don't fall through to call onHurt below.
		        Just ignore the second Enemy, so this case is a no-op.
		        */
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
	protected final void onBump(final Character c) {
		if (v <= 0) {
			v = VEL_BUMP;
		}
	}
	
	@Override
	protected final boolean onFell() {
		if (jumpMode != JUMP_FLY) {
			onHurt();
			startSafety();
			return true;
		}
		return false;
	}
	
	@Override
	protected final void onDestroy() {
		bubble.destroy();
		Panctor.destroy(back);
	}
	
	private final static class Bubble extends Panctor {
		{
			setView(PlatformGame.bubble);
		}
	}
}
