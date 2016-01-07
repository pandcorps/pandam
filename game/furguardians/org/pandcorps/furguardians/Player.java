/*
Copyright (c) 2009-2016, Andrew M. Martin
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
package org.pandcorps.furguardians;

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panteraction.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.furguardians.Enemy.*;
import org.pandcorps.furguardians.Gem.*;
import org.pandcorps.furguardians.Level.*;
import org.pandcorps.furguardians.Profile.*;

public class Player extends Character implements CollisionListener {
	protected final static int PLAYER_X = 6;
	protected final static int PLAYER_H = 23; // 15
    private final static int _VEL_WALK = 3;
	private final static int VEL_RETURN = 2;
	private final static int VEL_CATCH_UP = 8;
	private final static int VEL_JUMP = 8;
	private final static int VEL_JUMP_DRAGON = (VEL_JUMP + MAX_V) / 2;
	protected final static int VEL_BUMP = 4;
	protected final static int OFF_BIRD = 32;
	protected final static byte MODE_NORMAL = 0;
	private final static byte MODE_RETURN = 1;
	protected final static byte MODE_DISABLED = 2;
	protected final static byte MODE_FROZEN = 3;
	protected final static byte JUMP_HIGH = 1;
	//private final static byte JUMP_DOUBLE = 2;
	//private final static byte JUMP_INFINITE = 3;
	protected final static byte JUMP_FLY = 4;
	protected final static byte JUMP_DRAGON = 5;
	protected final static byte POWER_LIGHTNING = 1;
	protected final static byte POWER_DOUBLE = 2;
	protected static byte powerMode = MODE_NORMAL;
	protected static int powerTimer = 0;
	
	protected final static void clearPower() {
	    powerMode = MODE_NORMAL;
        powerTimer = 0;
	}
	
	protected final static void setPower(final byte power) {
	    powerMode = power;
	    powerTimer = 450;
	}
	
	protected final static void step() {
	    if (Pangine.getEngine().isPaused() || !Panlayer.isActive(Level.room)) {
	        return;
	    }
	    if (powerTimer > 0) {
	        final int p, tock;
	        if (powerTimer > 210) {
	            p = 60;
	            tock = 30;
	        } else if (powerTimer > 90) {
	            p = 30;
	            tock = 15;
	        } else {
	            p = 15;
	            tock = 7;
	        }
	        final int m = powerTimer % p;
	        if (m == 0) {
	            FurGuardiansGame.soundArmor.startSound();
	        } else if (m == tock) {
	            FurGuardiansGame.soundThud.startSound();
	        }
	        powerTimer--;
	        if (powerTimer == 0) {
	            powerMode = MODE_NORMAL;
	        }
	    }
	}
	
	public static enum JumpMode implements Named { // enum can't extend FinName
	    Normal(MODE_NORMAL, "Normal", "Normal jumping", 0),
	    High(JUMP_HIGH, "Spring Heels", "Jump much higher", 10000),
	    Fly(JUMP_FLY, "Wings", "Fly as high as you want", 50000),
	    Dragon(JUMP_DRAGON, "Dragon", "Jump higher, defeat armored enemies", 100000);
	    
	    private final byte index;
	    
	    private final String name;
	    
	    private final String desc;
	    
	    private final int cost;
	    
	    private JumpMode(final byte index, final String name, final String desc, final int cost) {
	        this.index = index;
	        this.name = name;
	        this.desc = desc;
	        this.cost = cost;
	    }
	    
	    public final byte getIndex() {
	        return index;
	    }
	    
	    @Override
	    public final String getName() {
	        return name;
	    }
	    
	    public final String getDescription() {
	    	return desc;
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
	
	public static class EyeData extends PlayerData {
		protected int eye = -1;
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
	    protected Panimation bird = null;
	    
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
	    
	    public final String getBonusName() {
	        List<String> list = new ArrayList<String>(3);
	        list.add(getName());
	        final Avatar avt = profile.currentAvatar;
	    	if (avt.jumpMode == JUMP_DRAGON) {
	    		list.add(avt.dragon.getName());
	    	}
	    	if (avt.bird.kind != null) {
	    	    list.add(avt.bird.getName());
	    	}
	    	return Chartil.nvl(Mathtil.rand(list), "FUR").toUpperCase();
	    }
	    
	    public final int getGems() {
	        return profile.getGems();
	    }
	    
	    public final Device getDevice() {
	        return ctrl.getDevice();
	    }
	    
	    private final void commitGems() {
	    	addGems(player.levelGems);
	    }
	    
	    protected final void addGems(final int n) {
	    	tempGems = profile.getGems();
			profile.addGems(n);
	    }
	    
	    public final int getGemMultiplier() {
	        int m = profile.getGemMultiplier();
	        if (player != null && powerMode == POWER_DOUBLE) {
	            m *= 2;
	        }
	        return m;
	    }
	    
	    public final void onFinishLevel() {
	    	commitGems();
	    	profile.stats.addRun(player.levelGems);
	    	profile.stats.birdGems += player.levelBirdGems;
			profile.stats.defeatedEnemies += player.levelDefeatedEnemies;
			profile.stats.defeatedLevels++;
			if (Level.currLetter <= 0) {
			    profile.stats.collectedWords++;
			}
			if (Level.theme == Theme.Minecart) {
				profile.stats.playedMinecartLevels++;
			} else if (Level.theme == Theme.Cave) {
				profile.stats.playedCaveLevels++;
			}
		}
	    
	    public final void onFinishBonus() {
	    	commitGems();
	    	profile.stats.playedBonuses++;
	    	if (Cabin.cabinTileHandler instanceof Cabin.MatchTileHandler) {
	            profile.stats.playedMatchGames++;
	        }
	    }
		
		public final void onFinishWorld() {
			profile.stats.defeatedWorlds++;
		}
		
		public final boolean isAutoRunEnabled() {
		    // Check level to prevent auto-run in bonus Cabin
	        return FurGuardiansGame.level && (profile.autoRun || Level.theme == Theme.Minecart);
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
	    	Panmage.destroyAll(bird);
	    	bird = null;
	    }
	    
	    public final static Profile getProfile(final PlayerContext pc) {
	        return (pc == null) ? null : pc.profile;
	    }
	    
	    public final static Statistics getStatistics(final PlayerContext pc) {
	        return Profile.getStatistics(getProfile(pc));
	    }
	}
	
	protected static interface Ai {
		public void onStep(final Player player);
		
		public FlyerAi getFlyerAi();
	}
	
	protected static interface FlyerAi {
        public void onStep(final Flyer flyer);
    }
	
	private static FlyerAi getFlyerAi(final Ai ai) {
	    return (ai == null) ? null : ai.getFlyerAi();
	}
	
	protected final PlayerContext pc;
	protected byte mode = MODE_NORMAL;
	protected byte jumpMode = MODE_NORMAL;
	private boolean flying = false;
	private final Panple safe = new ImplPanple();
	private boolean safeMirror = false;
	private Panple returnDestination = null;
	private int returnVelocity = 0;
	private Player returnPlayer = null;
	private int currentComboSize = 0;
	private int currentComboAward = 0;
	protected int levelGems = 0;
	protected int levelFloatingGems = 0;
	protected int levelEndGems = 0;
	protected int levelBirdGems = 0;
	protected int levelBrokenBlocks = 0;
	protected int levelDefeatedEnemies = 0;
	protected int levelFalls = 0;
	protected long lastFall = -1;
	protected int levelHits = 0;
	protected final boolean level;
	private int hurtTimer = 0;
	private int stompTimer = 0;
	private int activeTimer = 0;
	private final Bubble bubble = new Bubble();
	private final PowerOverlay powerOverlay;
	private final Panctor container;
	private final Accessories acc;
	private final Flyer flyer;
	protected Ai ai = null;
	protected final boolean[] goalsMet = new boolean[Goal.NUM_ACTIVE_GOALS];
	protected long lastThud = -30;
	protected long lastDragonStomp = -30;
	private boolean firstStep = true;
	
	public Player(final PlayerContext pc, final float x, final float y) {
		super(PLAYER_X, PLAYER_H);
	    this.pc = pc;
	    pc.player = this;
	    level = Panscreen.get() instanceof FurGuardiansGame.PlatformScreen;
	    for (int i = 0; i < Goal.NUM_ACTIVE_GOALS; i++) {
	    	goalsMet[i] = false;
	    }
	    jumpMode = pc.profile.currentAvatar.jumpMode;
		final Pangine engine = Pangine.getEngine();
		setView(pc.guy);
		FurGuardiansGame.room.addActor(bubble);
		if (Level.theme == Theme.Minecart) {
			container = new Panctor();
			container.setView(FurGuardiansGame.minecart);
			FurGuardiansGame.room.addActor(container);
		} else {
			container = null;
		}
		powerOverlay = new PowerOverlay();
		FurGuardiansGame.room.addActor(powerOverlay);
		acc = new Accessories(pc);
		flyer = (pc.bird == null) ? null : new Flyer(this);
		final Panteraction interaction = engine.getInteraction();
		final ControlScheme ctrl = pc.ctrl;
		final Panput jumpInput = getJumpInput();
		register(jumpInput, new ActionStartListener() {
			@Override public final void onActionStart(final ActionStartEvent event) { jump(); }});
		register(jumpInput, new ActionEndListener() {
			@Override public final void onActionEnd(final ActionEndEvent event) { releaseJump(); }});
		register(ctrl.getRight(), new ActionListener() {
			@Override public final void onAction(final ActionEvent event) { right(); }});
		register(ctrl.getLeft(), new ActionListener() {
			@Override public final void onAction(final ActionEvent event) { left(); }});
		
		register(interaction.KEY_F1, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { engine.captureScreen(); }});
		
		if (FurGuardiansGame.debugMode) {
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
	        register(interaction.KEY_F2, new ActionStartListener() {
	            @Override public final void onActionStart(final ActionStartEvent event) { engine.startCaptureFrames(); }});
	        register(interaction.KEY_F3, new ActionStartListener() {
	            @Override public final void onActionStart(final ActionStartEvent event) { engine.stopCaptureFrames(); }});
		}
        
        FurGuardiansGame.setPosition(this, x, y);
        FurGuardiansGame.room.addActor(this);
        if (flyer != null) {
            flyer.init();
        }
	}
	
	private final void registerPause(final Panput input) {
		(Panctor.isActive(FurGuardiansGame.hudGem) ? FurGuardiansGame.hudGem : this).register(input, new ActionStartListener() {
            @Override public final void onActionStart(final ActionStartEvent event) { togglePause(); }});
	}
	
	private final void registerPause() {
		registerPause(pc.ctrl.getSubmit());
		registerPause(pc.ctrl.get2());
	}
	
	private final Panput getJumpInput() {
		return pc.ctrl.get1();
	}
	
	protected final void loadState(final Player p) {
	    if (p == null) {
	        return;
	    }
	    levelGems = p.levelGems;
	    levelFloatingGems = p.levelFloatingGems;
	    levelEndGems = p.levelEndGems;
	    levelBirdGems = p.levelBirdGems;
	    levelBrokenBlocks = p.levelBrokenBlocks;
	    levelFalls = p.levelFalls;
	    levelHits = p.levelHits;
	}
	
	protected final void clearState() {
	    levelGems = 0;
	    levelFloatingGems = 0;
	    levelEndGems = 0;
	    levelBirdGems = 0;
	    levelBrokenBlocks = 0;
        levelFalls = 0;
        levelHits = 0;
	}
	
	private final void clearCombo() {
	    currentComboSize = 0;
	    currentComboAward = 0;
	}
	
	private final void evaluateCombo() {
	    if (currentComboSize > 1) {
            /*
            The award was given once gradually throughout the combo.
            Would make sense to reduce multiplier by one when giving the final bonus.
            Would be more confusing to Player though.
            */
            addGems(currentComboSize * currentComboAward);
            final Statistics stats = PlayerContext.getStatistics(pc);
            if (stats != null) {
                stats.combos++;
                if (currentComboSize > stats.longestCombo) {
                    stats.longestCombo = currentComboSize;
                }
            }
            FurGuardiansGame.notify(pc, "Combo: " + currentComboSize + " x " + currentComboAward);
        }
        clearCombo();
	}
	
	private final boolean isInputDisabled() {
		return mode == MODE_DISABLED || mode == MODE_FROZEN;
	}
	
	private final boolean isAutoRunEnabled() {
	    return pc.isAutoRunEnabled();
	}
	
	private final byte getCurrentJumpMode() {
		return (Level.theme == Theme.Minecart) ? MODE_NORMAL : jumpMode;
	}
	
	private final void jump() {
		if (isInputDisabled()) {
			return;
		} else if (isAutoRunEnabled()) {
		    this.activeTimer += 8;
		}
		final byte jumpMode = getCurrentJumpMode();
		if (jumpMode == JUMP_FLY) {
			if (isGrounded()) {
				pc.profile.stats.jumps++;
				FurGuardiansGame.soundJump.startSound();
			}
	        flying = true;
	        addV(-getG());
	        return;
	    } else if (isGrounded()) {
	    	final Pansound sound;
	        if (jumpMode == JUMP_HIGH) {
	            showSprings();
	            sound = FurGuardiansGame.soundBounce;
	        } else {
	            sound = FurGuardiansGame.soundJump;
	        }
	        v = getVelocityJump();
	        if (sanded) {
	        	v -= 2;
	        }
			pc.profile.stats.jumps++;
			sound.startSound();
		}
	}
	
	private final void showSprings() {
		acc.back.setView(pc.backJump);
	}
	
	private final int getVelocityJump() {
		switch (getCurrentJumpMode()) {
			case MODE_NORMAL : return VEL_JUMP;
			case JUMP_HIGH : return MAX_V;
			case JUMP_DRAGON : return VEL_JUMP_DRAGON;
			//case JUMP_FLY : return -getG();
			default : throw new UnsupportedOperationException("Cannot determine velocity for jumpMode " + jumpMode);
		}
	}
	
	private final void releaseJump() {
		final byte jumpMode = getCurrentJumpMode();
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
		hv = getVelWalk();
	}
	
	private final void left() {
		if (isInputDisabled()) {
			return;
		}
		hv = -getVelWalk();
	}
	
	protected final int getVelWalk() {
		return (Level.theme == Theme.Minecart) ? (_VEL_WALK + 1) : _VEL_WALK;
	}
	
	private final void togglePause() {
		final Pangine engine = Pangine.getEngine();
		if (engine.isMouseSupported()) {
			Menu.PlayerScreen.togglePromptQuit(FurGuardiansGame.hud);
		} else {
			engine.togglePause();
		}
	}
	
	protected boolean isDragonStomping() {
		return (jumpMode == JUMP_DRAGON) || pc.profile.isDragonStomping() || Level.theme == Theme.Minecart;
	}
	
	private final void evaluateDragonStomp() {
		if (isDragonStomping() && Level.theme != Theme.Minecart) {
			final long clock = Pangine.getEngine().getClock();
			if (lastDragonStomp >= (clock - 3)) {
				return;
			}
			lastDragonStomp = clock;
			final int r = 6, d = r * 2;
			for (int i = 0; i < 2; i++) {
				burst(FurGuardiansGame.puff, (i * d) - r, 0, i > 0);
			}
			FurGuardiansGame.soundWhoosh.startSound();
		}
	}
	
	private final Burst burst(final Panimation anm, final float xoff, final float yoff, final boolean mirror) {
	    final Burst b = new Burst(anm);
	    final Panple pos = getPosition();
        FurGuardiansGame.setPosition(b, pos.getX() + xoff, pos.getY() + yoff, FurGuardiansGame.DEPTH_SPARK);
        b.setMirror(mirror);
        FurGuardiansGame.room.addActor(b);
        return b;
	}
	
	private final boolean isBubbleEnabled() {
	    return hurtTimer > 0 || mode == MODE_RETURN;
	}
	
	private final boolean isInvincible() {
		return isBubbleEnabled() || mode == MODE_FROZEN || isElectric();
	}
	
	private final boolean isElectric() {
	    return powerMode == POWER_LIGHTNING;
	}
	
	private final boolean isReturningFromScroll() {
		return mode == MODE_RETURN && returnPlayer != null;
	}
	
	private final static Player getActive() {
		Player a = null;
		for (final PlayerContext pc : FurGuardiansGame.pcs) {
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
		return a == null ? FurGuardiansGame.pcs.get(0).player : a;
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
	    startReturn(safe, isAutoRunEnabled() ? (VEL_RETURN * 5 / 2) : VEL_RETURN, null);
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
		    enableTemporaryInvincibility();
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
		if (firstStep) {
			registerPause();
			firstStep = false;
		}
		onStepPower();
	    if (hurtTimer > 0) {
	        hurtTimer--;
	        if (hurtTimer == 0 && mode == MODE_FROZEN) {
	        	mode = MODE_NORMAL;
	        	if (getView() != FurGuardiansGame.burn) {
	        		Tiles.shatterCenteredActor(getLayer(), FurGuardiansGame.blockIce8, getPosition(), false);
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
		final boolean auto = isAutoRunEnabled();
		if (auto && !Level.victory && mode != MODE_FROZEN && mode != MODE_DISABLED) { // Check disabled to prevent running on ThroneScreen
		    hv = getVelWalk();
		} else if (container != null && Level.victory) {
			container.setView(FurGuardiansGame.minecart.getFrames()[0].getImage());
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
	
	private final void onStepPower() {
	    if (powerTimer <= 0 || powerMode == MODE_NORMAL) {
	        return;
	    } else if (powerMode == POWER_DOUBLE) {
	        final boolean spark;
	        if (powerTimer > 240) {
	            spark = (powerTimer % 10) == 0;
	        } else if (powerTimer > 120) {
	            spark = (powerTimer % 20) == 0;
	        } else if (powerTimer > 60) {
	            spark = (powerTimer % 30) == 0;
	        } else {
	            spark = false;
	        }
	        if (spark) {
	            burst(FurGuardiansGame.doubleFx, Mathtil.randi(-10, 10), Mathtil.randi(0, 24), isMirror());
	        }
	    }
	}

	@Override
	protected final void onCollide(final int index) {
		/*final TileOccupant o = Level.tm.getOccupant(index);
		if (o == null) {
			return;
		}
		((Gem) o).onCollide(this);*/
		final byte b = getBehavior(index);
		if (FurGuardiansGame.TILE_GEM == b) {
			Gem.onCollide(Level.tm, index, this);
			levelFloatingGems++;
		} else if (FurGuardiansGame.TILE_HURT == b) {
			startHurt();
		}
	}
	
	private final static byte getBehavior(final int index) {
		return Tile.getBehavior(Level.tm.getTile(index));
	}
	
	@Override
	protected final boolean isNearCheckNeeded() {
		return pc.profile.isGemMagnetActive();
	}
	
	@Override
	protected final void onNear(final int index) {
		if (FurGuardiansGame.TILE_GEM == getBehavior(index)) {
			new GemAttracted(index, this);
			levelFloatingGems++; // Only used for Achievement, OK to give credit before it reaches Player
		}
	}
	
	public final int getCurrentLevelGems() {
        return levelGems;
    }
	
	public final void addGems(final int gems) {
	    final int addedGems = (gems * pc.getGemMultiplier());
        levelGems += addedGems;
        if (powerMode == POWER_DOUBLE) {
            final Statistics stats = Profile.getStatistics(pc.profile);
            if (stats != null) {
                stats.doubledGems += addedGems;
            }
        }
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
		final int depth = FurGuardiansGame.getDepthPlayer(jumpMode), depthBack = FurGuardiansGame.getDepthPlayerBack(jumpMode);
		pos.setZ(fd + depth);
		if (backPos != null) {
			backPos.setZ(fd + depthBack);
		}
		if (v == 0) {
			final float px = pos.getX(), py = pos.getY(), py1 = py + OFF_GROUNDED;
			final float pl = px + getOffLeft(), pr = px + getOffRight();
			final byte left = Tile.getBehavior(Level.tm.getTile(Level.tm.getContainer(pl, py)));
			final byte right = Tile.getBehavior(Level.tm.getTile(Level.tm.getContainer(pr, py)));
			final byte belowLeft = Tile.getBehavior(Level.tm.getTile(Level.tm.getContainer(pl, py1)));
			final byte belowRight = Tile.getBehavior(Level.tm.getTile(Level.tm.getContainer(pr, py1)));
			final boolean sand = left == FurGuardiansGame.TILE_SAND || right == FurGuardiansGame.TILE_SAND;
			final boolean belowSand = belowLeft == FurGuardiansGame.TILE_SAND || belowRight == FurGuardiansGame.TILE_SAND;
			if (sand || belowSand) {
				if (belowSand) {
					pos.addY(-1);
				}
			    thv = (hv == 0) ? 0 : (hv / Math.abs(hv));
			    chv = thv;
			    sanded = true;
			    pos.setZ(depth);
				if (backPos != null) {
					backPos.setZ(depthBack);
				}
			} else if (belowLeft == FurGuardiansGame.TILE_ICE || belowRight == FurGuardiansGame.TILE_ICE) {
				final float dif = hv - chv;
				if (dif > 0) {
					chv += 0.125f;
				} else if (dif < 0) {
					chv -= 0.125f;
				}
				thv = Math.round(chv);
			} else if (hv != 0 && isGrounded()) {
				if (hv > 0) {
					if (chv <= 0) {
						chv = 1;
					} else {
						chv = (chv < hv) ? (chv + 1) : hv;
					}
				} else {
					if (chv >= 0) {
						chv = -1;
					} else {
						chv = (chv > hv) ? (chv - 1) : hv;
					}
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
		        addV(-getG());
		    }
		}
	}
	
	@Override
	protected final void onScrolled() {
		final Player active = getActive();
		if (active == null) {
			return;
		} else if (this == active) {
			for (final PlayerContext pc : FurGuardiansGame.pcs) {
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
		final float x = pos.getX(), y = pos.getY();
		final int depthBubble = FurGuardiansGame.getDepthBubble(jumpMode);
		FurGuardiansGame.setPosition(bubble, x, y - 1, depthBubble);
		bubble.onStepEnd(this);
		FurGuardiansGame.setPosition(powerOverlay, x, y, depthBubble);
		powerOverlay.onStepEnd(this);
		if (container != null) {
			FurGuardiansGame.setPosition(container, x, y, FurGuardiansGame.getDepthContainer(jumpMode));
			container.setMirror(isMirror());
		}
		acc.onStepEnd(this);
	}
	
	private final boolean isAnimated() {
		return container == null;
	}
	
	@Override
	protected final void onGrounded() {
		safe.set(getPosition());
		safeMirror = isMirror();
		evaluateCombo();
		if (mode != MODE_FROZEN) {
			if (hv != 0 && isAnimated()) {
				changeView(pc.guyRun);
			} else {
				changeView(pc.guy);
			}
		}
		if (acc.back != null) {
			if (hv == 0 || pc.backRun == null || !isAnimated()) {
				acc.back.changeView(pc.back);
			} else {
				acc.back.changeView(pc.backRun);
			}
		}
	}
	
	@Override
	protected void onLanded() {
		super.onLanded();
		evaluateDragonStomp();
	}
	
	@Override
	protected final boolean onAir() {
		if (!isAnimated()) {
			return flying;
		} else if (mode != MODE_FROZEN) {
			changeView(v > 0 ? pc.guyJump : pc.guyFall);
		}
		if (acc.back != null) {
			final byte jumpMode = getCurrentJumpMode();
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
		    final Enemy enemy = (Enemy) other;
		    if (isElectric()) {
		        enemy.onElectrocute(this);
		        return;
		    }
		    final boolean aboveEnemy = getPosition().getY() > other.getPosition().getY();
		    if (aboveEnemy && isBouncePossible()) {
				if (enemy.onStomp(this)) {
					bounce();
				}
		    } else if (aboveEnemy && stompTimer > 0) {
		        /*
		        This Player just stomped two Enemies at the same time.
		        The first one was already processed, causing the bounce.
		        So the Player is no longer falling.
		        But don't fall through to call onHurt below.
		        Just ignore the second Enemy, so this case is a no-op.
		        */
			} else if (enemy.onHurtPlayer(this)) {
			    startHurt();
			}
		} else if (other instanceof Projectile) {
		    startHurt();
		} else if (other instanceof Wisp) {
		    final Wisp wisp = (Wisp) other;
		    if (isElectric()) {
		        wisp.onElectrocute(this);
		    } else {
		        startFreeze(wisp);
		    }
		} else if (other instanceof Bouncer) {
		    if (isBouncePossible()) {
		        bounce();
		        FurGuardiansGame.soundBounce.startSound();
		        final Statistics stats = PlayerContext.getStatistics(pc);
		        if (stats != null) {
		            stats.bounces++;
		        }
		    }
		}
	}
	
	protected final void onReward(final EnemyDefinition def) {
	    if (!isGrounded()) {
	        currentComboSize++;
	        currentComboAward += def.award;
	        if (currentComboSize > 1) {
	            final Info info = new Info(FurGuardiansGame.font, "x" + currentComboSize, 1, 0);
	            final Panple pos = getPosition();
	            FurGuardiansGame.setPosition(info, pos.getX(), pos.getY() + 28, FurGuardiansGame.DEPTH_TEXT);
	            info.centerX();
	            FurGuardiansGame.room.addActor(info);
	            if (currentComboSize >= 5) {
	                evaluateCombo(); // Maximum mostly exists so that winged Players can't get huge combos
	            }
	        }
	    }
	}
	
	private final boolean isBouncePossible() {
	    return v < 4 && !isGrounded();
	}
	
	private final void bounce() {
	    final byte jumpMode = getCurrentJumpMode();
        if ((jumpMode != JUMP_FLY && getJumpInput().isActive())) {
            v = getVelocityJump();
            if (jumpMode == JUMP_HIGH) {
                showSprings();
            }
        } else {
            v = VEL_BUMP;
        }
        stompTimer = 2;
        evaluateDragonStomp();
	}
	
	private final boolean isHurtable() {
		return !(isInvincible() || pc.profile.isInvincible());
	}
	
	protected final void startHurt() {
	    if (isHurtable()) {
	    	levelHits++;
            onHurt();
            enableTemporaryInvincibility();
        }
	}
	
	private final void enableTemporaryInvincibility() {
		hurtTimer = 60;
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
		setView(FurGuardiansGame.frozen);
		FurGuardiansGame.soundWhoosh.startSound();
	}
	
	protected final void startBurn() {
		hurtTimer = 20;
		setView(FurGuardiansGame.burn);
		FurGuardiansGame.soundWhoosh.startSound();
	}
	
	public final void onHurt() {
	    final Profile prf = pc.profile;
	    final boolean noGems = levelGems == 0, inv = prf.isInvincible();
	    if (noGems && prf.endLevelIfHurtWithNoGems && !inv) {
	        flipAndFall(6);
	        FurGuardiansGame.playTransition(FurGuardiansGame.soundWhoosh);
	        FurGuardiansGame.goMap();
	        return;
	    } else if (noGems || inv) {
        	FurGuardiansGame.soundWhoosh.startSound(); // Skipping shatter, so play another sound
            return;
        }
        levelGems -= (Math.max(1, (int) (levelGems * prf.getDamageMultiplier())));
        GemBumped.newShatter(this);
    }
	
	@Override
    protected final void onEnd() {
	    if (!Level.victory && isAutoRunEnabled()) {
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
		if (getCurrentJumpMode() == JUMP_FLY) {
			final long clock = Pangine.getEngine().getClock();
			if (lastFall < 0 || clock > (lastFall + 1)) {
				levelFalls++;
			}
			lastFall = clock;
		} else {
		    if (isAutoRunEnabled()) {
		        final Panple pos = getPosition();
		        final float x = pos.getX();
		        float y = getCeiling() - 1;
		        if (Level.theme == Level.Theme.Cave) {
		            y = (Level.tm.getHeight() * Level.tm.getTileHeight()) - 1 - H - (Level.MAX_CAVE_CEILING_SIZE * 16);
		        }
		        safe.set(x, y, pos.getZ());
		        if (Level.theme == Theme.Minecart) {
		            final TileMap tm = Level.tm;
		            final int pcol = tm.getContainerColumn(x);
		            final int lastCol = Math.min(pcol + 16, tm.getWidth() - 1);
		            for (int col = pcol; col <= lastCol; col++) {
		                if (getTrackRow(col) >= 0) {
		                    continue;
		                }
		                for (int nextCol = col + 1; nextCol <= lastCol; nextCol++) {
		                    final int trackRow = getTrackRow(nextCol);
		                    if (trackRow >= 0) {
		                        for (int fillCol = col; fillCol < nextCol; fillCol++) {
		                        	tm.setTile(fillCol, trackRow + 1, Level.tileTrackTop);
		                            tm.setTile(fillCol, trackRow, Level.tileTrackBase);
		                        }
		                        col = nextCol + 1;
		                        break;
		                    }
		                }
		            }
		        }
		    }
		    levelFalls++;
			onHurt();
			startSafety();
			return true;
		}
		return false;
	}
	
	private final static int getTrackRow(final int col) {
	    final TileMap tm = Level.tm;
	    final int h = tm.getHeight();
	    for (int row = 0; row < h; row++) {
	        final Tile tile = tm.getTile(col, row);
	        if (tile != null && tile.isSolid()) {
	            return row;
	        }
	    }
	    return -1;
	}
	
	@Override
	protected final float getG() {
		return (getCurrentJumpMode() == JUMP_FLY) ? gFlying : g;
	}
	
	protected final void onLevelVictory() {
	    evaluateCombo();
	    levelEndGems = levelGems; // Capture final total before level-end award, used by NoGemsFeat
	}
	
	@Override
	protected final void onDestroy() {
		bubble.destroy();
		Panctor.destroy(container);
		acc.destroy();
		Panctor.destroy(flyer);
	}
	
	protected final static class Bubble extends Panctor {
		{
			setView(FurGuardiansGame.bubble);
		}
		
		protected void onStepEnd(final boolean visible) {
			setVisible(visible && Pangine.getEngine().isOn(4));
		}
		
		protected void onStepEnd(final Player p) {
			onStepEnd(p.isBubbleEnabled() && p.mode != MODE_FROZEN);
			setMirror(p.isMirror());
		}
	}
	
	protected final static class PowerOverlay extends Panctor {
	    protected final void onStepEnd(final Player p) {
	        if (powerMode != POWER_LIGHTNING) {
	            setVisible(false);
	            return;
	        }
	        setMirror(p.isMirror());
	        changeView(FurGuardiansGame.electric);
	        final boolean visible;
	        if (powerTimer >= 90) {
	            visible = true;
	        } else if (powerTimer > 30) { // 31 - 89
	            visible = (powerTimer % 30) < 15; // Off for 45-59 and 75-89
	        } else if (powerTimer > 20) { // 21 - 30
	            visible = false;
	        } else if (powerTimer > 10) { // 11 - 20
	            visible = true;
	        } else {
	            visible = false;
	        }
            setVisible(visible);
        }
	}
	
	protected final static class Accessories {
		private final Panctor back;
		
		protected Accessories(final PlayerContext pc) {
			final byte jm = pc.profile.currentAvatar.jumpMode;
			if (jm == JUMP_FLY || jm == JUMP_HIGH || jm == JUMP_DRAGON) {
			    back = jm == JUMP_HIGH ? new Back() : new Panctor();
			    back.setView(pc.back);
			    FurGuardiansGame.room.addActor(back);
			    FurGuardiansGame.setPosition(back, 0, 0, FurGuardiansGame.getDepthPlayerBack(jm));
			} else {
				back = null;
			}
		}
		
		protected void onStepEnd(final Panctor act) {
			if (back != null) {
				final Panple pos = act.getPosition();
				// Sand can change z, so only set z once in constructor
			    //FurGuardiansGame.setPosition(back, pos.getX(), pos.getY(), FurGuardiansGame.DEPTH_PLAYER_BACK);
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
	
	protected final static class Flyer extends Panctor implements StepListener {
	    private final static float dthresh = 44;
	    private final static float dmax = 120;
	    private final static float abase = 0.5f;
	    private final static float rmin = 0.05f;
	    private final static float rmax = 0.15f;
	    private final static float tthresh = 24;
	    private final static float nonLevelThreshold = 32;
	    protected final Player player;
	    private float vx = 0;
	    private float vy = 0;
	    private float ax = abase;
	    private float ay = abase;
	    private float rx = rndR();
        private float ry = rndR();
        private int targetIndex = -1;
        private final Panple target = new ImplPanple();
	    
	    private Flyer(final Player player) {
	        this.player = player;
	        setView(player.pc.bird);
	        FurGuardiansGame.room.addActor(this);
	    }
	    
	    private void init() {
	        final Panple ppos = player.getPosition();
	        FurGuardiansGame.setPosition(this, ppos.getX() + 16, ppos.getY() + OFF_BIRD, FurGuardiansGame.getDepthBubble(player.jumpMode));
	    }
	    
	    private Panple getTarget() {
	        final Panple pos = getPosition(), ppos = player.getPosition();
	        if (Math.abs(pos.getX() - ppos.getX()) > dmax || Math.abs(pos.getY() - ppos.getY()) > dmax) {
                targetIndex = -1;
                return ppos;
            } else if (targetIndex >= 0) {
	            if (getBehavior(targetIndex) != FurGuardiansGame.TILE_GEM) {
	                targetIndex = -1;
	            } else {
	                return target;
	            }
	        }
	        for (int i = 1; i < 4; i++) {
	            final int scope = 24 * i;
    	        target.set(pos.getX() + Mathtil.randi(-scope, scope), pos.getY() + Mathtil.randi(-scope, scope));
    	        final int scopeIndex = Level.tm.getContainer(target);
    	        if (FurGuardiansGame.TILE_GEM == getBehavior(scopeIndex)) {
    	            targetIndex = scopeIndex;
                    return target;
    	        }
	        }
	        return ppos;
	    }
	    
	    private final float getDistanceThreshold() {
	        return (targetIndex >= 0) ? tthresh : (FurGuardiansGame.level ? dthresh : nonLevelThreshold);
	    }
	    
        @Override
        public final void onStep(final StepEvent event) {
            final FlyerAi ai = getFlyerAi(player.ai);
            if (ai != null) {
                ai.onStep(this);
                return;
            } else if (player.mode == MODE_DISABLED && player.ai == null) {
                return;
            }
            final Panple pos = getPosition(), ppos = getTarget();
            final float px = pos.getX(), py = pos.getY(), thresh = getDistanceThreshold();
            rx = fixR(rx, px, ppos.getX(), thresh);
            ry = fixR(ry, py, ppos.getY(), thresh);
            ax = addAcc(ax, rx, abase);
            ay = addAcc(ay, ry, abase);
            final int vw = player.getVelWalk();
            vx = addAcc(vx, ax, vw + 1);
            vy = addAcc(vy, ay, vw);
            pos.addX(vx);
            pos.addY(vy);
            if (vx < 0) {
                setMirror(true);
            } else if (vx > 0) {
                setMirror(false);
            }
            for (int i = 0; i < 2; i++) {
                final float pxi = px - 7 + (i * 14);
                for (int j = 0; j < 2; j++) {
                    final int index = Level.tm.getContainer(pxi, py + (j * 14));
                    if (FurGuardiansGame.TILE_GEM == getBehavior(index)) {
                        Gem.onCollide(Level.tm, index, player);
                        player.levelBirdGems++;
                    }
                }
            }
        }
        
        private final static float fixR(final float r, final float p, final float pp, final float thresh) {
            final float d = p - pp;
            if (d > thresh) {
                return (r < 0) ? r : -rndR();
            } else if (d < -thresh) {
                return (r > 0) ? r : rndR();
            }
            return r;
        }
        
        private final static float addAcc(float v, final float a, final float vmax) {
            v += a;
            if (v > vmax) {
                v = vmax;
            } else if (v < -vmax) {
                v = -vmax;
            }
            return v;
        }
        
        private final static float rndR() {
            return Mathtil.randf(rmin, rmax);
        }
	}
	
	protected static class Bouncer extends Panctor implements Collidable {
	    protected Bouncer(final int x, final int y) {
            setView(FurGuardiansGame.bee);
            FurGuardiansGame.room.addActor(this);
            setMirror(true);
            final TileMap tm = Level.tm;
            FurGuardiansGame.setPosition(this,
                (tm.getTileWidth() * x) + FurGuardiansGame.BIRD_X + 1, (tm.getTileHeight() * y) + FurGuardiansGame.BIRD_Y, FurGuardiansGame.DEPTH_BACK);
        }
	}
	
	protected final static class MovingBouncer extends Bouncer implements StepListener {
	    private final int min;
	    private final int max;
	    private byte dir = 1;
	    
        protected MovingBouncer(final int x, final int y, final int numTiles) {
            super(x, y);
            min = (int) getPosition().getX();
            max = min + (numTiles - 1) * 16;
            setMirror(false);
        }

        @Override
        public final void onStep(final StepEvent event) {
            final int x = (int) getPosition().getX();
            if (x <= min) {
                dir = 1;
                setMirror(false);
            } else if (x >= max) {
                dir = -1;
                setMirror(true);
            }
            getPosition().addX(dir);
        }
    }
}
