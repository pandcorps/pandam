/*
Copyright (c) 2009-2023, Andrew M. Martin
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

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.game.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.game.actor.CustomBurst.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.boundary.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.furguardians.Player.*;
import org.pandcorps.furguardians.Profile.*;
import org.pandcorps.furguardians.Spawner.*;

public class Enemy extends Character {
    protected final static byte DEFEAT_STOMP = 0;
	protected final static byte DEFEAT_BUMP = 1;
	protected final static byte DEFEAT_HIT = 2;
	private final static byte DEFEAT_ELECTROCUTE = 3;
	protected final static int DEFAULT_X = 5;
	protected final static int DEFAULT_H = 15;
	protected final static int DEFAULT_WALK = 6;
	protected final static int DEFAULT_SPLAT = 20;
	private final static int DEFAULT_HV = 1;
	private final static int MIN_TIMER = 60;
	private final static int MAX_TIMER = 90;
	
	protected final static FinPanple2 DEFAULT_O = new FinPanple2(8, 1);
	protected final static FinPanple2 DEFAULT_MIN = getMin(DEFAULT_X);
	protected final static FinPanple2 DEFAULT_MAX = getMax(DEFAULT_X, DEFAULT_H);
	private final static EnemyMenu DEFAULT_MENU = new ImplEnemyMenu();
	
	protected static int currentSplat = DEFAULT_SPLAT;
	protected static int currentWalk = DEFAULT_WALK;
	protected static Panimation currentWalkAnm = null;
	protected static Panple currentO = null;
	
	protected final static class EnemyDefinition extends FinName {
		protected String code;
		protected final Panimation walk;
		private final boolean ledgeTurn;
		protected Panimation splat;
		private final Panimation attack;
		protected Panimation extra;
		private final int avoidCount;
		private final int offX;
		private final int h;
		protected final int hv;
		protected int award = GemBumped.AWARD_DEF;
		protected Panimation projectile = null;
		protected BurstHandler splatHandler = null;
		protected InteractionHandler splatDecider = null;
		protected InteractionHandler stepHandler = null;
		protected InteractionHandler landedHandler = null;
		protected InteractionHandler hurtHandler = null;
		protected InteractionHandler stompHandler = null;
		protected InteractionHandler rewardHandler = null;
		protected InteractionHandler defeatHandler = null;
		protected Pansound wallSound = null;
		protected Pansound stompSound = null;
		protected boolean mustDestroyOffScreen = true;
		protected SpawnFactory factory = enemyFactory;
		protected EnemyMenu menu = DEFAULT_MENU;
		
		protected EnemyDefinition(final String name, final int ind, final PixelFilter f, final boolean ledgeTurn) {
		    this(name, ind, f, ledgeTurn, false, 0, DEFAULT_X, DEFAULT_H, DEFAULT_HV);
		}
		
		protected EnemyDefinition(final String name, final int ind, final PixelFilter f, final boolean ledgeTurn,
                                  final boolean splat, final int offX, final int h) {
		    this(name, ind, f, ledgeTurn, splat, 0, offX, h, DEFAULT_HV);
		}
		
		protected EnemyDefinition(final String name, final int ind, final PixelFilter f, final boolean ledgeTurn,
                                  final int avoidCount) {
		    this(name, ind, f, ledgeTurn, avoidCount, DEFAULT_HV);
		}
		
		protected EnemyDefinition(final String name, final int ind, final PixelFilter f, final boolean ledgeTurn,
                                  final int avoidCount, final int hv) {
            this(name, ind, f, ledgeTurn, false, avoidCount, DEFAULT_X, DEFAULT_H, hv);
        }
		
		protected EnemyDefinition(final String name, final int ind, final PixelFilter f, final boolean ledgeTurn,
		                          final boolean splat, final int avoidCount, final int offX, final int h, final int hv) {
			this(name, ind, f, ledgeTurn, splat, avoidCount, offX, h, hv, ImtilX.DIM);
		}
		
		protected EnemyDefinition(final String name, final int ind, final PixelFilter f, final boolean ledgeTurn,
                final boolean splat, final int avoidCount, final int offX, final int h, final int hv, final int d) {
		    super(name);
		    code = Chartil.toCode(name); // If a name is changed, explicitly assign the old code to keep stats consistent
			final Img[] strip = loadStrip(ind, d), walk;
			if (f != null) {
				final int size = strip.length;
				for (int i = 0; i < size; i++) {
					strip[i] = Imtil.filter(strip[i], f);
				}
			}
			if (splat && strip.length > 1) {
			    walk = new Img[] {strip[0], strip[1]};
			} else if (hv == 0) {
			    walk = new Img[] {strip[0]};
			} else {
			    walk = strip;
			}
			final String id = "enemy." + name;
			final Panple n, x, o;
			if (currentO == null) {
				o = (d == ImtilX.DIM) ? DEFAULT_O : new FinPanple2(d / 2, 1);
			} else {
				o = currentO;
				currentO = null;
			}
			if (offX == DEFAULT_X && h == DEFAULT_H) {
			    n = DEFAULT_MIN;
			    x = DEFAULT_MAX;
			} else {
			    n = getMin(offX);
			    x = getMax(offX, h);
			}
			if (currentWalkAnm == null) {
				this.walk = FurGuardiansGame.createAnm(id, currentWalk, o, n, x, walk);
			} else {
				this.walk = currentWalkAnm;
				currentWalkAnm = null;
			}
			currentWalk = DEFAULT_WALK;
			this.ledgeTurn = ledgeTurn;
			this.splat = splat ? FurGuardiansGame.createAnm(id + ".splat", currentSplat, o, n, x, strip[2]) : null;
			currentSplat = DEFAULT_SPLAT;
			if (strip.length > 3) {
				extra = FurGuardiansGame.createAnm(id + ".extra", 8, o, n, x, strip[3]);
			}
			this.attack = (hv == 0 && strip.length > 2) ? FurGuardiansGame.createAnm(id + ".attack", 20, o, n, x, strip[2]) : null;
			Img.close(strip);
			this.avoidCount = avoidCount;
			this.offX = offX;
			this.h = h;
			this.hv = hv;
			factory = enemyFactory;
		}
		
		protected EnemyDefinition(final String name, final int ind, final SpawnFactory spawnFactory) {
			this(name, ind, 0, spawnFactory);
		}
		
		protected EnemyDefinition(final String name, final int ind, final int hv, final SpawnFactory spawnFactory) {
			super(name);
			code = Chartil.toCode(name);
			final Img[] strip = loadStrip(ind, ImtilX.DIM);
			final int size = strip.length;
			final Panframe[] frames = new Panframe[size];
			final Pangine engine = Pangine.getEngine();
			for (int i = 0; i < size; i++) {
				final String id = name + "." + i;
				frames[i] = engine.createFrame(BaseGame.PRE_FRM + id,
						engine.createImage(BaseGame.PRE_IMG + id, DEFAULT_O, DEFAULT_MIN, DEFAULT_MAX, strip[i]),
						i == 0 ? 18 : 6);
			}
			if (currentWalkAnm == null) {
				if (size == 1) {
					walk = engine.createAnimation(BaseGame.PRE_ANM + name, frames[0]);
				} else {
					walk = engine.createAnimation(BaseGame.PRE_ANM + name, frames[0], frames[1], frames[2], frames[1]);
				}
			} else {
				walk = currentWalkAnm;
				currentWalkAnm = null;
			}
			ledgeTurn = false;
			splat = null;
			attack = null;
			Img.close(strip);
			avoidCount = 0;
			offX = 0;
			h = DEFAULT_H;
			this.hv = hv;
			factory = spawnFactory;
		}
		
		protected EnemyDefinition(final String name, final Panmage walkImg) {
		    this(name, FurGuardiansGame.createAnimation(walkImg));
		}
		
		protected EnemyDefinition(final String name, final Panimation walk) {
		    super(name);
		    code = Chartil.toCode(name);
		    this.walk = walk;
	        ledgeTurn = true;
	        splat = null;
	        attack = null;
	        extra = null;
	        avoidCount = 0;
	        offX = DEFAULT_X;
	        h = DEFAULT_H;
	        hv = DEFAULT_HV;
	        factory = null;
		}
		
		protected final void init(final EnemyDefinition ref) {
		    award = ref.award;
            stompHandler = ref.stompHandler;
            stepHandler = ref.stepHandler;
		}
		
		private final Panmage getWalkImage() {
		    return walk.getFrames()[0].getImage();
		}
	}
	
	protected final static EnemyDefinition newBigDefinition(final String name, final int ind, final PixelFilter f, final boolean ledgeTurn) {
	    return new EnemyDefinition(name, ind, f, ledgeTurn, false, 0, 8, 30, 1, 32);
	}
	
	protected final static EnemyDefinition newGiantDefinition(final String name, final int ind, final PixelFilter f, final boolean ledgeTurn) {
        return new EnemyDefinition(name, ind, f, ledgeTurn, false, 0, 26, 62, 1, 64);
    }
	
	protected final static Img[] loadStrip(final int ind, final int d) {
		return ImtilX.loadStrip(FurGuardiansGame.RES + "enemy/Enemy" + Chartil.padZero(ind, 2) + ".png", d);
	}
	
	protected final EnemyDefinition def;
	private int avoidCount = 0;
	protected int timer = 0;
	protected int timerMode = 0;
	protected boolean full = false;
	protected Player lastStomper = null; // If Enemy requires multiple stomps, keep track of last one
	protected byte defeatMode = -1;
	
	protected Enemy(final EnemyDefinition def, final Panctor ref) {
		this(def, ref.getPosition());
	}
	
	protected Enemy(final EnemyDefinition def, final Panple pos) {
		this(def, pos.getX(), pos.getY());
	}
	
	protected Enemy(final EnemyDefinition def, final float x, final float y) {
		super(def.offX, def.h);
		this.def = def;
		setView(def.walk);
		setEnemyMirror(true);
		FurGuardiansGame.room.addActor(this);
		FurGuardiansGame.setPosition(this, x, y, FurGuardiansGame.DEPTH_ENEMY);
		avoidCount = def.avoidCount;
		if (hv == 0) {
		    initTimer(0);
		}
	}
	
	protected void initTimer(final int timerMode) {
	    timer = Mathtil.randi(MIN_TIMER, MAX_TIMER);
	    this.timerMode = timerMode;
	}
	
	@Override
	protected boolean onStepCustom() {
		if (def.stepHandler != null) {
			final boolean ret = def.stepHandler.onInteract(this, null);
			if (ret) {
				checkScrolled();
			}
			return ret;
		} else if (hv == 0 && def.projectile != null) {
	        timer--;
	        if (timer < 0) {
	            switch (timerMode) {
	                case 0 :
	                    setView(def.attack);
	                    timer = 15;
	                    timerMode = 1;
	                    break;
	                case 1 :
	                    initTimer(2);
	                    throwProjectile(def.projectile, this, Mathtil.rand(FurGuardiansGame.pcs).player);
	                    break;
	                case 2 :
	                    stepTeleport();
	                    break;
	            }
	        }
	    }
	    return false;
	}
	
	private final static void throwProjectile(final Panimation projectile, final Panctor src, final Panctor dst) {
	    FurGuardiansGame.room.addActor(new Projectile(projectile, src, dst));
	    FurGuardiansGame.soundWhoosh.startSound();
	}
	
	private final void stepTeleport() {
	    setView(def.walk);
	    initTimer(0);
        teleport(Mathtil.randi(1, 8) * ImtilX.DIM);
        Boolean mirror = null;
        final float x = getPosition().getX();
        for (final PlayerContext pc : FurGuardiansGame.pcs) {
            final Boolean currMirror = Boolean.valueOf(pc.player.getPosition().getX() < x);
            if (mirror != currMirror) {
                if (mirror == null) {
                    mirror = currMirror;
                } else {
                    mirror = null;
                    break;
                }
            }
        }
        if (mirror != null) {
            setMirror(mirror.booleanValue());
        }
	}
	
	protected final boolean onStomp(final Player stomper) {
		lastStomper = stomper;
		Pansound.startSound(def.stompSound); // Plays if not null; will be null for most
		if (def.stompHandler == null || !def.stompHandler.onInteract(this, stomper)) {
			return defeat(stomper, 0, DEFEAT_STOMP);
		} else {
			return true;
		}
	}
	
	@Override
	protected final void onBump(final Character bumper) {
		defeat(bumper, Player.VEL_BUMP, DEFEAT_BUMP);
	}
	
	private final void onHit(final Character bouncer) {
		defeat(bouncer, Player.VEL_BUMP, DEFEAT_HIT);
	}
	
	protected final void onElectrocute(final Character electrocutor) {
	    defeat(electrocutor, Player.VEL_BUMP, DEFEAT_ELECTROCUTE);
	}
	
	private final static boolean isFree(final int index) {
	    final Tile t = Level.tm.getTile(index);
	    return t == null || t.getBehavior() == Tile.BEHAVIOR_OPEN;
	}
	
	private final boolean teleport(final int off) {
	    final Panple pos = getPosition();
        final int d = ImtilX.DIM;
        final float cx = pos.getX();
        final int bx = (int) cx + (getMirrorMultiplier() * off);
        float x = ((bx / d) * d) + 8;
        float y = Level.ROOM_H - d, fy = -1;
        boolean prevFree = isFree(Level.tm.getContainer(x, y));
        while (y > d) {
            y -= d;
            final boolean free = isFree(Level.tm.getContainer(x, y));
            if (prevFree && !free) {
                fy = y + d;
                break;
            }
            prevFree = free;
        }
        if (fy == -1) { // Skip this section if we want to cancel teleport when target is a pit
        	x = cx;
        	fy = getCeiling() - 1;
        }
        if (fy != -1) {
            burst(FurGuardiansGame.teleport);
            pos.set(x, fy);
            final Pangine engine = Pangine.getEngine();
            for (final PlayerContext pc : FurGuardiansGame.pcs) {
                if (engine.isCollision(this, pc.player)) {
                    /*destroy();
                    return true;*/
                	pos.set(x, getCeiling() - 1);
                }
            }
            burst(FurGuardiansGame.teleport);
            FurGuardiansGame.soundWhoosh.startSound();
            return true;
        }
        return false;
	}
	
	protected boolean defeat(final Character defeater, final int v, final byte defeatMode) {
		if (def.defeatHandler != null && !def.defeatHandler.onInteract(this, null)) {
			return false;
		} else if (avoidCount > 0 && defeatMode != DEFEAT_ELECTROCUTE) {
	        avoidCount--;
	        if (teleport(48)) {
	            return false;
	        }
	    }
		final Player player = (defeater != null && defeater.getClass() == Player.class) ? (Player) defeater : null;
		final boolean skipSplat = def.splatDecider != null && !def.splatDecider.onInteract(this, player);
		final BurstHandler splatHandler = skipSplat ? null : def.splatHandler;
		if (player != null && (v > 0 || splatHandler == null)) {
		    reward(player, defeatMode);
		}
		if (!skipSplat && v == 0 && def.splat != null) {
		    burst(def.splat, splatHandler);
		    destroy();
		} else {
		    flipAndFall(v); // Calls destroy
		}
		return true;
	}
	
	private final void reward(final Player player, final byte defeatMode) {
	    this.defeatMode = defeatMode;
	    if (isRewarded(player)) {
            reward(player, this, def, defeatMode);
        }
	}
	
	private final static void reward(final Player player, final Panctor enemy, final EnemyDefinition def, final byte defeatMode) {
	    if (def.award > 0) { // Once reached this point with a 0 reward, maybe with an empty armorBall or bounceBall; causes an Exception inside GemBumped
	        new GemBumped(player, enemy, def);
	    }
	    countDefeat(player, def, defeatMode);
	}
	
	protected final static void countDefeat(final Player player, final EnemyDefinition def, final byte defeatMode) {
	    player.levelDefeatedEnemies++;
        final Statistics stats = player.pc.profile.stats;
        switch (defeatMode) {
            case DEFEAT_STOMP :
                stats.stompedEnemies++;
                break;
            case DEFEAT_BUMP :
                stats.bumpedEnemies++;
                break;
            case DEFEAT_HIT :
                stats.hitEnemies++;
                break;
            case DEFEAT_ELECTROCUTE :
                stats.electrocutedEnemies++;
                break;
            default:
                throw new IllegalStateException("Unexpected defeatMode " + defeatMode);
        }
        stats.defeatedEnemyTypes.inc(def.code);
        player.onReward(def);
	}
	
	private final boolean isRewarded(final Player player) {
		return def.rewardHandler == null || def.rewardHandler.onInteract(this, player);
	}
	
	protected final void burst(final Panimation anm) {
		burst(anm, null);
	}
	
	protected final void burst(final Panimation anm, final BurstHandler burstHandler) {
		burst(anm, this, burstHandler);
	}
	
	protected final void burst(final Panimation anm, final Panctor dir, final BurstHandler burstHandler) {
		burst(anm, dir, burstHandler, 0);
	}
	
	protected final void burst(final Panimation anm, final Panctor dir, final BurstHandler burstHandler, final int yoff) {
		burst(this, anm, dir, burstHandler, yoff);
	}
	
	protected final static void burst(final Panctor ref, final Panimation anm, final Panctor dir, final BurstHandler burstHandler, final int yoff) {
	    final Burst b = CustomBurst.createBurst(anm, burstHandler, (ref instanceof CustomBurst) ? ((CustomBurst) ref).getContext() : ref);
	    final Panple pos = ref.getPosition();
        FurGuardiansGame.setPosition(b, pos.getX(), pos.getY() + yoff, FurGuardiansGame.DEPTH_SHATTER);
        b.setMirror(dir.isMirror());
        FurGuardiansGame.room.addActor(b);
	}
	
	protected final boolean onHurtPlayer(final Player player) {
	    final Character held = player.held;
	    if ((held != null) && held.isShieldWhenHeld() && player.isFacing(this)) {
	        defeat(player, VEL_DESTROY_HELD, DEFEAT_HIT);
	        held.destroyWhenHeld();
	        return false;
	    } else if (def.hurtHandler != null) {
	        return def.hurtHandler.onInteract(this, player);
	    }
	    return true;
	}
	
	@Override
	protected boolean isShieldWhenHeld() {
	    return true;
	}
	
	@Override
    protected void destroyWhenHeld() {
	    defeat(holder, VEL_DESTROY_HELD, DEFEAT_HIT);
	    holder.held = null;
	    holder = null;
    }
	
	@Override
	protected final void onScrolled() {
		if (isDestroyed()) {
			return;
		} else if (((def == null) || def.mustDestroyOffScreen) && destroyIfOffScreen(this, 80)) {
			onDestroyOffScreen();
		}
	}
	
	protected void onDestroyOffScreen() {
	}
	
	protected final static boolean destroyIfOffScreen(final Panctor a, final int off) {
		final float x = a.getPosition().getX();
		final Panlayer layer = a.getLayer();
		if (layer == null) {
		    return false;
		} else if ((x + off) < layer.getViewMinimum().getX() || (x - (off * 2)) > layer.getViewMaximum().getX()) {
			a.destroy();
			return true;
		}
		return false;
	}
	
	@Override
	protected final boolean onHorizontal(final int off) {
		return def.ledgeTurn && onHorizontalEdgeTurn(off);
	}
	
	@Override
    protected final void onLanded() {
	    if (def.landedHandler == null || !def.landedHandler.onInteract(this, null)) {
	        super.onLanded();
	    }
	}
	
	@Override
	protected final boolean onWall(final byte xResult) {
		hv *= -1;
		Pansound.startSound(def.wallSound);
		return true;
	}

	@Override
	protected boolean onFell() {
		if (lastStomper != null && isRewarded(lastStomper)) {
			/*
			It is annoying if Player stomps Ogre once, then Ogre falls before Player can finish the Ogre.
			So reward the Player.
			*/
			new GemBumped(lastStomper, this);
		}
		destroy();
		return true;
	}
	
	protected final void setEnemyMirror(final boolean mirror) {
		setMirror(mirror);
		initHv();
	}
	
	protected void initHv() {
		hv = getMirrorMultiplier() * def.hv;
	}
	
	protected final void facePlayers() {
		final boolean mirror = isMirror();
		final float x = getPosition().getX();
		for (final PlayerContext pc : Coltil.unnull(FurGuardiansGame.pcs)) {
			final Player p = pc.player;
			if (p == null) {
				continue;
			}
			final boolean wantMirror = p.getPosition().getX() < x;
			if (mirror == wantMirror) {
				return;
			}
		}
		setEnemyMirror(!mirror);
	}
	
	protected final Enemy spawn(final EnemyDefinition def) {
	    final Enemy enemy = new Enemy(def, this);
	    enemy.setEnemyMirror(isMirror());
	    return enemy;
	}
	
	protected final Enemy transform(final EnemyDefinition def) {
	    final Enemy enemy = spawn(def);
	    enemy.lastStomper = lastStomper;
	    destroy();
	    return enemy;
	}
	
	protected static interface InteractionHandler {
		public boolean onInteract(final Enemy enemy, final Player player);
	}
	
	private abstract static class ColliderEnemy extends Enemy implements CollisionListener {
        protected ColliderEnemy(final EnemyDefinition def, final Panctor ref) {
            super(def, ref);
        }
        
        protected ColliderEnemy(final EnemyDefinition def, final float x, final float y) {
            super(def, x, y);
        }

        @Override
        public final void onCollision(final CollisionEvent event) {
            if (holder != null) {
                return; // Collisions will be handled by the holder
            }
            final Collidable collider = event.getCollider();
            // Player handles its own collisions, so only check for Enemy
            if (collider instanceof Enemy) {
                final Enemy enemyCollider = (Enemy) collider;
                if (enemyCollider.holder != null) {
                    return;
                }
                onCollision(enemyCollider);
            }
        }
        
        protected abstract void onCollision(final Enemy collider);
	}
	
	public final static class ArmorBall extends ColliderEnemy {
	    protected ArmorBall(final Enemy ref) {
            this((Panctor) ref);
            full = ref.full;
            setMirror(ref.isMirror());
            ref.destroy();
        }
	    
        protected ArmorBall(final Panctor ref) {
            super(FurGuardiansGame.armorBall, ref);
        }
        
        protected ArmorBall(final EnemyDefinition def, final float x, final float y) {
            super(def, x, y);
        }
        
        @Override
        protected final boolean isHoldable() {
            return true;
        }
        
        @Override
        protected final void onRelease() {
            new BounceBall(this, holder);
        }
        
        @Override
        protected final void onKickUpward() {
            new BounceBall(this, holder, 0, Player.VEL_KICKED_UPWARD, false).initHorizontalVelocityOnKickUpward(holder);
        }

        @Override
        public final void onCollision(final Enemy collider) {
            if (full) {
                return;
            } else if (v < 0) {
                if (getPosition().getY() > collider.getPosition().getY()) {
                    collider.onBump(this);
                }
            } else if (collider.def == FurGuardiansGame.imp) {
                FurGuardiansGame.openArmoredImp(this, collider);
                collider.destroy();
            }
        }
        
        @Override
        protected final void initTimer(final int timerMode) {
    	    timer = 60;
    	    this.timerMode = timerMode;
    	}
    }
	
	public final static class BounceBall extends ColliderEnemy {
	    protected final boolean dangerous;
	    
        protected BounceBall(final Enemy ref, final Player bouncer) {
            this(ref, bouncer, FurGuardiansGame.bounceBall.hv, Player.VEL_BOUNCE, true);
        }
        
        protected BounceBall(final Enemy ref, final Player bouncer, final int hv, final float v, final boolean dangerous) {
            super(FurGuardiansGame.bounceBall, ref);
            this.dangerous = dangerous;
            lastStomper = bouncer;
            full = ref.full;
            setEnemyMirror(bouncer.isMirror());
            this.v = v;
            bouncer.pc.profile.stats.kicks++;
            bouncer.startKick();
            ref.destroy();
            FurGuardiansGame.soundArmor.startSound();
            this.hv = getMirrorMultiplier() * hv;
        }
        
        @Override
        protected final void initTimer(final int timerMode) {
            timer = 0;
        }
        
        @Override
        protected final void initHv() {
            hv = getMirrorMultiplier() * Math.abs(hv);
        }
        
        @Override
        protected final float getMinV() {
            return -Player.VEL_KICKED_UPWARD;
        }

        @Override
        public final void onCollision(final Enemy collider) {
            //collider.onBump(this); // Doesn't give Player Gem
        	collider.onHit(lastStomper);
        }
        
        @Override
        protected final void onWallTile(final int tileIndex) {
            onWallTileBump(lastStomper, tileIndex);
        }
    }
	
	public final static class Trio extends Enemy {
	    private final static int OFF_HEAD = 8;
		private final static int OFF_LEG = 9;
		private final Leg back;
		private final Leg front;
		
		protected Trio(final EnemyDefinition def, final float x, final float y) {
			super(def, x, y);
			back = new Leg(x - OFF_LEG, y, this);
			front = new Leg(x + OFF_LEG, y, this);
			FurGuardiansGame.setDepth(back, FurGuardiansGame.DEPTH_ENEMY_BACK);
			FurGuardiansGame.setDepth(front, FurGuardiansGame.DEPTH_ENEMY_FRONT);
			//TODO Prevent head getting stuck in block after destroying legs?
		}
		
		@Override
		public boolean onStepCustom() {
			final Panple pos = getPosition(), backPos = back.getPosition(), frontPos = front.getPosition();
			Panple.average(pos, backPos, frontPos);
			pos.addY(OFF_HEAD);
			boolean changedView = false;
			if (back.isGrounded() && front.isGrounded()) {
				if (timer == 0) {
					facePlayers();
					timer = 15;
					final boolean wantRight = isMirror();
					final boolean backRight = backPos.getX() > frontPos.getX();
					changeView(wantRight, backRight);
					changedView = true;
					final Leg toMove = (wantRight == backRight) ? back : front;
					toMove.jump();
				} else {
					timer--;
				}
			}
			if (!changedView) {
				changeView(isMirror(), backPos.getX() > frontPos.getX());
			}
			return true;
		}
		
		private void changeView(final boolean wantRight, final boolean backRight) {
			if (backRight == wantRight) {
				changeView(FurGuardiansGame.rockBack);
			} else {
				changeView(def.walk);
			}
		}
		
		@Override
		protected boolean defeat(final Character defeater, final int v, final byte defeatMode) {
		    /*
		    Don't usually reward until an Enemy is totally defeated.
		    Here a Rock Walker simply becomes a Rock Sprite.
		    But if we don't reward here, then the Player never receives credit for defeating a Rock Walker.
		    And the Player receives 2 Gems this way, 1 for defeating the Walker and another for the Sprite.
		    Previously the Player only received 1, and most enemies that require 2 stomps give 10.
		    So 2 is an improvement.
		    */
		    if ((defeater instanceof Player) && !isDestroyed()) {
		        reward((Player) defeater, this, def, defeatMode);
		    }
		    transform();
		    return true;
		}
		
		@Override
		protected void onDestroyOffScreen() {
			transform();
		}
		
		private final void transform() {
		    if (isDestroyed()) {
		        return;
		    }
		    final Enemy sprite = transform(FurGuardiansGame.rockSprite);
		    sprite.timer = -1;
		    sprite.hv = 0;
		    back.transform();
		    front.transform();
		}
	}
	
	public final static class Leg extends Enemy {
		private final Trio head;
		
		protected Leg(final float x, final float y, final Trio head) {
			super(FurGuardiansGame.rockLeg, x, y);
			this.head = head;
		}
		
		@Override
		public final boolean onStepCustom() {
			return isGrounded();
		}
		
		@Override
		protected final boolean onFell() {
		    head.transform();
		    return true;
		}
		
		@Override
        protected boolean defeat(final Character defeater, final int v, final byte defeatMode) {
            head.defeat(defeater, v, defeatMode);
            return true;
        }
		
		@Override
		protected void onDestroyOffScreen() {
			head.transform();
		}
		
		private final void transform() {
            if (isDestroyed()) {
                return;
            }
		    burst(FurGuardiansGame.puff);
		    destroy();
		}
		
		private final void jump() {
			setEnemyMirror(head.isMirror());
			v = 3.2f;
			FurGuardiansGame.soundJump.startSound();
		}
	}
	
	public final static class Wisp extends Panctor implements Collidable, StepListener, AllOobListener {
		protected final EnemyDefinition def;
		private int timer = 0;
		private final Panple vel = new ImplPanple();
		
		protected Wisp(final EnemyDefinition def, final float x, final float y) {
			this.def = def;
			setView(def.walk);
			setMirror(true);
			FurGuardiansGame.room.addActor(this);
			final float maxy = Math.min(y + 80, Level.tm.getHeight() * Level.tm.getTileHeight());
			FurGuardiansGame.setPosition(this, x, Mathtil.randf(y, maxy), FurGuardiansGame.DEPTH_ENEMY);
		}
		
		@Override
	    public final void onStep(final StepEvent event) {
			if (isDestroyed()) {
				return;
			} else if (timer <= 0) {
				if (vel.getMagnitude2() < 0.2f) {
					timer = Mathtil.randi(60, 90);
					Projectile.setVelocity(this, Mathtil.rand(FurGuardiansGame.pcs).player, vel, 1f);
				} else {
					timer = Mathtil.randi(60, 60);
					vel.set(0, 0, 0);
				}
			} else {
				getPosition().add(vel);
				timer--;
			}
	    	Enemy.destroyIfOffScreen(this, 40);
	    }
		
		protected final void onElectrocute(final Player electrocutor) {
		    reward(electrocutor, this, def, DEFEAT_ELECTROCUTE);
		    Character.flipAndFall(this, def.h, Player.VEL_BUMP);
		}
	    
	    @Override
	    public final void onAllOob(final AllOobEvent event) {
	        destroy();
	    }
	}
	
	public final static class NetherCube extends Panctor implements StepListener {
	    private final int index;
	    private int timer;
	    
	    protected NetherCube(final int x, final int y) {
	        index = Level.tm.getIndex(x, y);
	        final Panple pos = Level.tm.getPosition(index);
	        getPosition().set(pos.getX() + 8, pos.getY() + 2);
	        initTimer();
	    }
	    
	    private final void initTimer() {
	        timer = Mathtil.randi(60, 150);
	    }
	    
        @Override
        public final void onStep(final StepEvent event) {
            final Player target = getTarget();
            if (target == null) {
                return;
            }
            final float diff = getDifference(target);
            if (Math.abs(diff) >= 160) {
                return;
            }
            final Tile tile = Level.tm.getTile(index);
            final Object fg = DynamicTileMap.getRawForeground(tile);
            if (tile.isSolid() && fg != null) {
                destroy();
                return;
            } else if (diff < -4) {
                setMirror(true);
                if (fg == FurGuardiansGame.netherCube1) {
                    Level.tm.setForeground(index, FurGuardiansGame.netherCubeMirror1);
                } else if (fg == FurGuardiansGame.netherCube2) {
                    Level.tm.setForeground(index, FurGuardiansGame.netherCubeMirror2);
                } else if (fg == FurGuardiansGame.netherCube3) {
                    Level.tm.setForeground(index, FurGuardiansGame.netherCubeMirror3);
                }
            } else if (diff > 4) {
                setMirror(false);
                if (fg == FurGuardiansGame.netherCubeMirror1) {
                    Level.tm.setForeground(index, FurGuardiansGame.netherCube1);
                } else if (fg == FurGuardiansGame.netherCubeMirror2) {
                    Level.tm.setForeground(index, FurGuardiansGame.netherCube2);
                } else if (fg == FurGuardiansGame.netherCubeMirror3) {
                    Level.tm.setForeground(index, FurGuardiansGame.netherCube3);
                }
            }
            timer--;
            if (timer < 0) {
                throwProjectile(FurGuardiansGame.projectile1, this, target);
                initTimer();
            }
        }
        
        private final Player getTarget() {
            Player target = null;
            float targetDistance = -1;
            for (final PlayerContext pc : Coltil.unnull(FurGuardiansGame.pcs)) {
                final Player player = PlayerContext.getPlayer(pc);
                if (player == null) {
                    continue;
                } else if (target == null) {
                    target = player;
                } else {
                    final float distance = getDistance(player);
                    if (targetDistance < 0) {
                        targetDistance = getDistance(target);
                    }
                    if (distance < targetDistance) {
                        target = player;
                        targetDistance = distance;
                    }
                }
            }
            return target;
        }
        
        private final float getDistance(final Player player) {
            return Math.abs(getDifference(player));
        }
        
        private final float getDifference(final Player player) {
            return player.getPosition().getX() - getPosition().getX();
        }
	}
	
	protected final static SpawnFactory enemyFactory = new EnemyFactory();
	
	protected final static SpawnFactory trioFactory = new TrioFactory();
	
	protected final static SpawnFactory wispFactory = new WispFactory();
	
	protected final static SpawnFactory armorBallFactory = new ArmorBallFactory();
	
	protected static interface SpawnFactory {
		public Panctor spawn(final EnemyDefinition def, final float x, final float y);
	}
	
	private final static class EnemyFactory implements SpawnFactory {
		@Override
		public final Enemy spawn(final EnemyDefinition def, final float x, final float y) {
			return new Enemy(def, x, y);
		}
	}
	
	private final static class TrioFactory implements SpawnFactory {
		@Override
		public final Trio spawn(final EnemyDefinition def, final float x, final float y) {
			return new Trio(def, x, y);
		}
	}
	
	private final static class WispFactory implements SpawnFactory {
		@Override
		public final Wisp spawn(final EnemyDefinition def, final float x, final float y) {
			return new Wisp(def, x, y);
		}
	}
	
	private final static class ArmorBallFactory implements SpawnFactory {
        @Override
        public final ArmorBall spawn(final EnemyDefinition def, final float x, final float y) {
            return new ArmorBall(def, x, y);
        }
    }
	
	protected static interface EnemyMenu {
	    public void draw(final Panctor enemyBack, final Panctor enemy, final Panctor enemyFront, final EnemyDefinition def, final int x);
	}
	
	private final static class ImplEnemyMenu implements EnemyMenu {
        @Override
        public final void draw(final Panctor enemyBack, final Panctor enemy, final Panctor enemyFront, final EnemyDefinition def, final int x) {
            enemyBack.setView((Panmage) null);
            enemy.setView(def.getWalkImage());
            enemy.getPosition().set(x, Menu.Y_PLAYER);
            enemyFront.setView((Panmage) null);
        }
	}
	
	protected final static class TrioEnemyMenu implements EnemyMenu {
        @Override
        public final void draw(final Panctor enemyBack, final Panctor enemy, final Panctor enemyFront, final EnemyDefinition def, final int x) {
            final Panmage legImg = FurGuardiansGame.rockLeg.getWalkImage();
            enemyBack.setView(legImg);
            enemyBack.getPosition().set(x - Trio.OFF_LEG, Menu.Y_PLAYER);
            enemy.setView(def.getWalkImage());
            enemy.getPosition().set(x, Menu.Y_PLAYER + Trio.OFF_HEAD);
            enemyFront.setView(legImg);
            enemyFront.getPosition().set(x + Trio.OFF_LEG, Menu.Y_PLAYER);
        }
    }
	
	protected final static class CubeEnemyMenu implements EnemyMenu {
        @Override
        public final void draw(final Panctor enemyBack, final Panctor enemy, final Panctor enemyFront, final EnemyDefinition def, final int x) {
            enemy.setView(def.getWalkImage());
            enemy.getPosition().set(x + DEFAULT_O.getX(), Menu.Y_PLAYER - DEFAULT_O.getY());
        }
    }
	
	protected abstract static class HavocLockController extends Panctor implements StepListener {
	    private int timer = 0;
	    
	    @Override
	    public final void onStep(final StepEvent event) {
	        for (final Panctor actor : Coltil.unnull(Level.room.getActors())) {
	            if (isBoss(actor)) {
	                timer = 0;
	                return;
	            }
	        }
	        timer++;
	        if (timer > 30) {
    	        Level.unlockGoal();
    	        destroy();
	        }
	    }
	    
	    protected abstract boolean isBoss(final Panctor actor);
	}
	
	protected final static class NetherCubeHavocLockController extends HavocLockController {
	    @Override
	    protected final boolean isBoss(final Panctor actor) {
	        return actor instanceof NetherCube;
	    }
	}
	
	protected final static class NetherGlobHavocLockController extends HavocLockController {
        @Override
        protected final boolean isBoss(final Panctor actor) {
            final EnemyDefinition def;
            if (actor instanceof Enemy) {
                def = ((Enemy) actor).def;
            } else if (actor instanceof SpecificSpawner) {
                def = ((SpecificSpawner) actor).getDef();
            } else {
                return false;
            }
            return (def == FurGuardiansGame.netherGlob) || (def == FurGuardiansGame.greaterGlob) || (def == FurGuardiansGame.giantGlob);
        }
    }
}
