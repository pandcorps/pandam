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
package org.pandcorps.platform;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panteraction.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.platform.Enemy.*;

public class Player extends Character implements CollisionListener {
	protected final static int PLAYER_X = 6;
	protected final static int PLAYER_H = 23; // 15
    protected final static int VEL_WALK = 3;
	private final static int VEL_RETURN = 2;
	private final static int VEL_CATCH_UP = 8;
	private final static int VEL_JUMP = 8;
	protected final static int VEL_BUMP = 4;
	protected final static byte MODE_NORMAL = 0;
	private final static byte MODE_RETURN = 1;
	protected final static byte MODE_DISABLED = 2;
	protected final static byte MODE_FROZEN = 3;
	protected final static byte JUMP_HIGH = 1;
	//private final static byte JUMP_DOUBLE = 2;
	//private final static byte JUMP_INFINITE = 3;
	protected final static byte JUMP_FLY = 4;
	protected final static byte JUMP_DRAGON = 5;
	
	public static enum JumpMode implements Named { // enum can't extend FinName
	    Normal(MODE_NORMAL, "Normal", 0),
	    High(JUMP_HIGH, "Spring Heels", 10000),
	    Fly(JUMP_FLY, "Wings", 50000),
	    Dragon(JUMP_DRAGON, "Dragon", 100000);
	    
	    private final byte index;
	    
	    private final String name;
	    
	    private final int cost;
	    
	    private JumpMode(final byte index, final String name, final int cost) {
	        this.index = index;
	        this.name = name;
	        this.cost = cost;
	    }
	    
	    public final byte getIndex() {
	        return index;
	    }
	    
	    @Override
	    public final String getName() {
	        return name;
	    }
	    
	    public final int getCost() {
	        return cost;
	    }
	    
	    public final static JumpMode get(final int index) {
	        for (final JumpMode jm : values()) {
	            if (jm.index == index) {
	                return jm;
	            }
	        }
	        return null;
	    }
	}
	
	public static interface Named {
	    public String getName();
	}
	
	public static class FinName implements Named {
	    private final String name;
	    
	    protected FinName(final String name) {
	        this.name = name;
	    }
	    
	    @Override
	    public final String getName() {
	        return name;
	    }
	}
	
	public static class PlayerData implements Named {
	    private String name = null;
	    
	    @Override
	    public final String getName() {
            return name;
        }
	    
	    public final void setName(final String name) {
            this.name = name;
        }
	}
	
	public final static String getName(final Named data) {
        return data == null ? null : data.getName();
    }
	
	public final static <E extends Named> E get(final E[] a, final String name) {
        for (final E e : a) {
            if (e.getName().equals(name)) {
                return e;
            }
        }
        return null;
    }
	
	// Player attributes preserved between levels
	public final static class PlayerContext implements Named {
	    protected final Profile profile;
	    protected final int index;
	    protected Player player = null;
	    protected int tempGems = -1;
	    
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
	    protected Panimation backRun = null;
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
	    
	    @Override
	    public final String getName() {
	        return profile.currentAvatar.getName();
	    }
	    
	    public final int getGems() {
	        return profile.getGems();
	    }
	    
	    public final Device getDevice() {
	        return ctrl.get1().getDevice();
	    }
	    
	    private final void commitGems() {
	    	addGems(player.levelGems);
	    }
	    
	    protected final void addGems(final int n) {
	    	tempGems = profile.getGems();
			profile.addGems(n);
	    }
	    
	    public final void onFinishLevel() {
	    	commitGems();
			profile.stats.defeatedEnemies += player.levelDefeatedEnemies;
			profile.stats.defeatedLevels++;
			if (Level.currLetter <= 0) {
			    profile.stats.collectedWords++;
			}
		}
	    
	    public final void onFinishBonus() {
	    	commitGems();
	    	profile.stats.playedBonuses++;
	    }
		
		public final void onFinishWorld() {
			profile.stats.defeatedWorlds++;
		}
	    
	    public final void destroy() {
	    	if (guy == null) {
	    		return;
	    	}
	    	guy.destroyAll();
	    	Panmage.destroyAll(guyRun);
	    	guyRun = null;
	    	Panmage.destroy(guyJump);
	    	guyJump = null;
	    	Panmage.destroy(guyFall);
	    	guyFall = null;
	    	Panmage.destroyAll(mapSouth);
	    	mapSouth = null;
	    	Panmage.destroyAll(mapEast);
	    	mapEast = null;
	    	Panmage.destroyAll(mapWest);
	    	mapWest = null;
	    	Panmage.destroyAll(mapNorth);
	    	mapNorth = null;
	    	Panmage.destroyAll(mapLadder);
	    	mapLadder = null;
	    	Panmage.destroy(mapPose);
	    	mapPose = null;
	    	Panmage.destroy(back);
	    	back = null;
	    	Panmage.destroyAll(backRun);
	    	backRun = null;
	    	Panmage.destroyAll(backJump);
	    	backJump = null;
	    	Panmage.destroyAll(backFall);
	    	backFall = null;
	    }
	}
	
	protected static interface Ai {
		public void onStep(final Player player);
	}
	
	protected final PlayerContext pc;
	protected byte mode = MODE_NORMAL;
	private byte jumpMode = MODE_NORMAL;
	private boolean flying = false;
	private final Panple safe = new ImplPanple(0, 0, 0);
	private boolean safeMirror = false;
	private Panple returnDestination = null;
	private int returnVelocity = 0;
	private Player returnPlayer = null;
	private int levelGems = 0;
	protected int levelDefeatedEnemies = 0;
	protected int levelFalls = 0;
	protected long lastFall = -1;
	protected int levelHits = 0;
	protected final boolean level;
	private int hurtTimer = 0;
	private int stompTimer = 0;
	private int activeTimer = 0;
	private final Bubble bubble = new Bubble();
	private final Accessories acc;
	protected Ai ai = null;
	protected final boolean[] goalsMet = new boolean[Goal.NUM_ACTIVE_GOALS];
	
	public Player(final PlayerContext pc) {
		super(PLAYER_X, PLAYER_H);
	    this.pc = pc;
	    pc.player = this;
	    level = Panscreen.get() instanceof PlatformGame.PlatformScreen;
	    for (int i = 0; i < Goal.NUM_ACTIVE_GOALS; i++) {
	    	goalsMet[i] = false;
	    }
	    jumpMode = pc.profile.currentAvatar.jumpMode;
		final Pangine engine = Pangine.getEngine();
		setView(pc.guy);
		PlatformGame.room.addActor(bubble);
		acc = new Accessories(pc);
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
		register(ctrl.getSubmit(), new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { engine.togglePause(); }});
		
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
        register(interaction.KEY_F1, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { engine.captureScreen(); }});
        register(interaction.KEY_F2, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { engine.startCaptureFrames(); }});
        register(interaction.KEY_F3, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { engine.stopCaptureFrames(); }});
	}
	
	private final boolean isInputDisabled() {
		return mode == MODE_DISABLED || mode == MODE_FROZEN;
	}
	
	private final void jump() {
		if (isInputDisabled()) {
			return;
		} else if (pc.profile.autoRun) {
		    this.activeTimer += 8;
		}
		if (jumpMode == JUMP_FLY) {
			if (isGrounded()) {
				pc.profile.stats.jumps++;
			}
	        flying = true;
	        addV(-g);
	        return;
	    } else if (isGrounded()) {
	        if (jumpMode == JUMP_HIGH) {
	            v = MAX_V;
	            acc.back.setView(pc.backJump);
	        } else {
	            v = VEL_JUMP;
	        }
	        if (sanded) {
	        	v -= 2;
	        }
			pc.profile.stats.jumps++;
			//if isMusicSupported Pangine.getEngine().getMusic().playSound(Music.jump);
		}
	}
	
	private final void releaseJump() {
	    if (jumpMode == JUMP_FLY) {
            flying = false;
            return;
        } else if (v > 0 && stompTimer == 0) {
			v = 0;
			if (jumpMode == JUMP_HIGH) {
				acc.back.setView((Panmage) null);
			}
		}
	}
	
	private final void right() {
		if (isInputDisabled()) {
			return;
		}
		hv = VEL_WALK;
	}
	
	private final void left() {
		if (isInputDisabled()) {
			return;
		}
		hv = -VEL_WALK;
	}
	
	private boolean isInvincible() {
		return hurtTimer > 0 || mode == MODE_RETURN || mode == MODE_FROZEN;
	}
	
	private boolean isReturningFromScroll() {
		return mode == MODE_RETURN && returnPlayer != null;
	}
	
	private final static Player getActive() {
		Player a = null;
		for (final PlayerContext pc : PlatformGame.pcs) {
			if (pc == null) {
				continue;
			}
			final Player c = pc.player;
			if (c == null) {
				continue;
			}
			// Tie breaker?
			if (!c.isReturningFromScroll() && (a == null || a.activeTimer < c.activeTimer)) {
				a = c;
			}
		}
		return a == null ? PlatformGame.pcs.get(0).player : a;
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
	        if (hurtTimer == 0 && mode == MODE_FROZEN) {
	        	mode = MODE_NORMAL;
	        	if (getView() != PlatformGame.burn) {
	        		Tiles.shatterCenteredActor(getLayer(), PlatformGame.blockIce8, getPosition(), false);
	        	}
	        	setView(pc.guy);
	        	if (acc.back != null) {
					acc.back.setVisible(true);
				}
	        	startHurt();
	        }
	    }
	    if (stompTimer > 0) {
	        stompTimer--;
	    }
		if (mode == MODE_RETURN) {
			onStepReturn();
			return true;
		} else if (mode == MODE_DISABLED) {
			if (ai != null) {
				ai.onStep(this);
			}
			//return true; // Let falling Player keep falling; just don't allow new input
		}
		final boolean auto = pc.profile.autoRun;
		if (auto && !Level.victory && mode != MODE_FROZEN) {
		    hv = VEL_WALK;
		}
		if (auto || hv == 0) {
			if (activeTimer > 0) {
				activeTimer -= Math.max(1, activeTimer / 2);
			}
		} else {
			activeTimer++;
		}
		return false;
	}

	@Override
	protected final void onCollide(final int index) {
		/*final TileOccupant o = Level.tm.getOccupant(index);
		if (o == null) {
			return;
		}
		((Gem) o).onCollide(this);*/
		final TileMap tm = Level.tm;
		if (PlatformGame.TILE_GEM == Tile.getBehavior(tm.getTile(index))) {
			Gem.onCollide(tm, index, this);
		}
	}
	
	public final int getCurrentLevelGems() {
        return levelGems;
    }
	
	public final void addGems(final int gems) {
        levelGems += (gems * pc.profile.getGemMultiplier());
    }
	
	private boolean sanded = false;
	
	@Override
	protected final int initCurrentHorizontalVelocity() {
		//TODO Print each thv; make sure same going left or right
		//TODO If ice is in air, Player can change direction immediately by sliding to very edge
		sanded = false;
		final int thv;
		final float fd = Level.tm.getForegroundDepth();
		final Panple pos = getPosition(), backPos = acc.back == null ? null : acc.back.getPosition();
		pos.setZ(fd + PlatformGame.DEPTH_PLAYER);
		if (backPos != null) {
			backPos.setZ(fd + PlatformGame.DEPTH_PLAYER_BACK);
		}
		if (v == 0) {
			final float px = pos.getX(), py = pos.getY(), py1 = py + OFF_GROUNDED;
			final float pl = px + getOffLeft(), pr = px + getOffRight();
			final byte left = Tile.getBehavior(Level.tm.getTile(Level.tm.getContainer(pl, py)));
			final byte right = Tile.getBehavior(Level.tm.getTile(Level.tm.getContainer(pr, py)));
			final byte belowLeft = Tile.getBehavior(Level.tm.getTile(Level.tm.getContainer(pl, py1)));
			final byte belowRight = Tile.getBehavior(Level.tm.getTile(Level.tm.getContainer(pr, py1)));
			final boolean sand = left == PlatformGame.TILE_SAND || right == PlatformGame.TILE_SAND;
			final boolean belowSand = belowLeft == PlatformGame.TILE_SAND || belowRight == PlatformGame.TILE_SAND;
			if (sand || belowSand) {
				if (belowSand) {
					pos.addY(-1);
				}
			    thv = (hv == 0) ? 0 : (hv / Math.abs(hv));
			    chv = thv;
			    sanded = true;
			    pos.setZ(PlatformGame.DEPTH_PLAYER);
				if (backPos != null) {
					backPos.setZ(PlatformGame.DEPTH_PLAYER_BACK);
				}
			} else if (belowLeft == PlatformGame.TILE_ICE || belowRight == PlatformGame.TILE_ICE) {
				final float dif = hv - chv;
				if (dif > 0) {
					chv += 0.125f;
				} else if (dif < 0) {
					chv -= 0.125f;
				}
				thv = Math.round(chv);
			} else {
				chv = hv;
				thv = hv;
			}
		} else {
			chv = hv;
			thv = hv;
		}
		return thv;
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
				if (other != null && other != this) {
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
		bubble.onStepEnd(this);
		acc.onStepEnd(this);
	}
	
	@Override
	protected final void onGrounded() {
		safe.set(getPosition());
		safeMirror = isMirror();
		if (mode != MODE_FROZEN) {
			if (hv != 0) {
				changeView(pc.guyRun);
			} else {
				changeView(pc.guy);
			}
		}
		if (acc.back != null) {
			if (hv == 0 || pc.backRun == null) {
				acc.back.changeView(pc.back);
			} else {
				acc.back.changeView(pc.backRun);
			}
		}
	}
	
	@Override
	protected final boolean onAir() {
		if (mode != MODE_FROZEN) {
			changeView(v > 0 ? pc.guyJump : pc.guyFall);
		}
		if (acc.back != null) {
			if (jumpMode == JUMP_FLY) {
				acc.back.changeView((flying || getPosition().getY() <= MIN_Y) ? pc.backJump : pc.backFall);
				// v > 0 doesn't flap as soon as jump is pressed
			} else if (jumpMode == JUMP_DRAGON) {
				acc.back.changeView((v > 0) ? pc.backJump : pc.backFall);
			}
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
		    if (aboveEnemy && v < 4 && !isGrounded()) {
				if (((Enemy) other).onStomp(this)) {
    				v = VEL_BUMP;
    				stompTimer = 2;
				}
		    } else if (aboveEnemy && stompTimer > 0) {
		        /*
		        This Player just stomped two Enemies at the same time.
		        The first one was already processed, causing the bounce.
		        So the Player is no longer falling.
		        But don't fall through to call onHurt below.
		        Just ignore the second Enemy, so this case is a no-op.
		        */
			} else if (((Enemy) other).onHurtPlayer(this)) {
			    startHurt();
			}
		} else if (other instanceof Projectile) {
		    startHurt();
		} else if (other instanceof Wisp) {
		    startFreeze((Wisp) other);
		}
	}
	
	private final boolean isHurtable() {
		return !(isInvincible() || pc.profile.isInvincible());
	}
	
	protected final void startHurt() {
	    if (isHurtable()) {
	    	levelHits++;
            onHurt();
            hurtTimer = 60; // Enable temporary invincibility
        }
	}
	
	private final void startFreeze(final Wisp wisp) {
		if (isHurtable()) {
			wisp.def.hurtHandler.onInteract(null, this);
			flying = false;
			mode = MODE_FROZEN;
			if (acc.back != null) {
				acc.back.setVisible(false);
			}
		}
	}
	
	protected final void startFreeze() {
		hurtTimer = 60;
		setView(PlatformGame.frozen);
	}
	
	protected final void startBurn() {
		hurtTimer = 20;
		setView(PlatformGame.burn);
	}
	
	public final void onHurt() {
        if (levelGems == 0 || pc.profile.isInvincible()) {
            return;
        }
        levelGems -= (Math.max(1, levelGems / 10));
        GemBumped.newShatter(this);
    }
	
	@Override
    protected final void onEnd() {
	    if (!Level.victory && pc.profile.autoRun) {
	        Tiles.bump(this, Level.goalIndex);
	    }
    }
	
	@Override
	protected final void onBump(final Character c) {
		if (v <= 0) {
			v = VEL_BUMP;
		}
	}
	
	@Override
	protected final boolean onFell() {
		if (jumpMode == JUMP_FLY) {
			final long clock = Pangine.getEngine().getClock();
			if (lastFall < 0 || clock > (lastFall + 1)) {
				levelFalls++;
			}
			lastFall = clock;
		} else {
		    if (pc.profile.autoRun) {
		        final Panple pos = getPosition();
		        safe.set(pos.getX(), getCeiling() - 1, pos.getZ());
		    }
		    levelFalls++;
			onHurt();
			startSafety();
			return true;
		}
		return false;
	}
	
	@Override
	protected final void onDestroy() {
		bubble.destroy();
		acc.destroy();
	}
	
	protected final static class Bubble extends Panctor {
		{
			setView(PlatformGame.bubble);
		}
		
		protected void onStepEnd(final boolean visible) {
			setVisible(visible && Pangine.getEngine().isOn(4));
		}
		
		protected void onStepEnd(final Player p) {
			onStepEnd(p.mode != MODE_FROZEN && p.isInvincible());
			setMirror(p.isMirror());
		}
	}
	
	protected final static class Accessories {
		private final Panctor back;
		
		protected Accessories(final PlayerContext pc) {
			final byte jm = pc.profile.currentAvatar.jumpMode;
			if (jm == JUMP_FLY || jm == JUMP_HIGH || jm == JUMP_DRAGON) {
			    back = jm == JUMP_HIGH ? new Back() : new Panctor();
			    back.setView(pc.back);
			    PlatformGame.room.addActor(back);
			    PlatformGame.setPosition(back, 0, 0, PlatformGame.DEPTH_PLAYER_BACK);
			} else {
				back = null;
			}
		}
		
		protected void onStepEnd(final Panctor act) {
			if (back != null) {
				final Panple pos = act.getPosition();
				// Sand can change z, so only set z once in constructor
			    //PlatformGame.setPosition(back, pos.getX(), pos.getY(), PlatformGame.DEPTH_PLAYER_BACK);
				back.getPosition().set(pos.getX(), pos.getY());
			    back.setMirror(act.isMirror());
			}
		}
		
		protected void destroy() {
			Panctor.destroy(back);
		}
	}
	
	private final static class Back extends Panctor implements AnimationEndListener {
	    @Override
        public final void onAnimationEnd(final AnimationEndEvent event) {
            setView((Panmage) null);
        }
	}
}
