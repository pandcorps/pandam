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

public class Enemy extends Character {
	private final static byte DEFEAT_STOMP = 0;
	private final static byte DEFEAT_BUMP = 1;
	private final static byte DEFEAT_HIT = 2;
	protected final static int DEFAULT_X = 5;
	protected final static int DEFAULT_H = 15;
	protected final static int DEFAULT_WALK = 6;
	protected final static int DEFAULT_SPLAT = 20;
	private final static int DEFAULT_HV = 1;
	private final static int MIN_TIMER = 60;
	private final static int MAX_TIMER = 90;
	
	protected final static FinPanple2 DEFAULT_O = new FinPanple2(8, 1);
	private final static FinPanple2 DEFAULT_MIN = getMin(DEFAULT_X);
	private final static FinPanple2 DEFAULT_MAX = getMax(DEFAULT_X, DEFAULT_H);
	
	protected static int currentSplat = DEFAULT_SPLAT;
	protected static int currentWalk = DEFAULT_WALK;
	
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
		private final int hv;
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
		protected final SpawnFactory factory;
		
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
			final Panple n, x, o = (d == ImtilX.DIM) ? DEFAULT_O : new FinPanple2(d / 2, 1);
			if (offX == DEFAULT_X && h == DEFAULT_H) {
			    n = DEFAULT_MIN;
			    x = DEFAULT_MAX;
			} else {
			    n = getMin(offX);
			    x = getMax(offX, h);
			}
			this.walk = FurGuardiansGame.createAnm(id, currentWalk, o, n, x, walk);
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
		
		protected EnemyDefinition(final String name, final int ind) {
			super(name);
			code = Chartil.toCode(name);
			final Img[] strip = loadStrip(ind, ImtilX.DIM);
			final Panframe[] frames = new Panframe[3];
			final Pangine engine = Pangine.getEngine();
			for (int i = 0; i < 3; i++) {
				final String id = name + "." + i;
				frames[i] = engine.createFrame(BaseGame.PRE_FRM + id,
						engine.createImage(BaseGame.PRE_IMG + id, DEFAULT_O, DEFAULT_MIN, DEFAULT_MAX, strip[i]),
						i == 0 ? 18 : 6);
			}
			this.walk = engine.createAnimation(BaseGame.PRE_ANM + name, frames[0], frames[1], frames[2], frames[1]);
			ledgeTurn = false;
			splat = null;
			attack = null;
			avoidCount = 0;
			offX = 0;
			h = 0;
			hv = 0;
			factory = wispFactory;
		}
		
		protected final void init(final EnemyDefinition ref) {
		    award = ref.award;
            stompHandler = ref.stompHandler;
            stepHandler = ref.stepHandler;
		}
	}
	
	protected final static Img[] loadStrip(final int ind, final int d) {
		return ImtilX.loadStrip("org/pandcorps/platform/res/enemy/Enemy" + Chartil.padZero(ind, 2) + ".png", d);
	}
	
	protected final EnemyDefinition def;
	private int avoidCount = 0;
	protected int timer = 0;
	protected int timerMode = 0;
	protected boolean full = false;
	protected Player lastStomper = null; // If Enemy requires multiple stomps, keep track of last one
	
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
	protected final boolean onStepCustom() {
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
	                    FurGuardiansGame.room.addActor(new Projectile(def.projectile, this, Mathtil.rand(FurGuardiansGame.pcs).player));
	                    FurGuardiansGame.soundWhoosh.startSound();
	                    break;
	                case 2 :
	                    stepTeleport();
	                    break;
	            }
	        }
	    }
	    return false;
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
		Pansound.startSound(def.stompSound);
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
	
	private final static boolean isFree(final int index) {
	    final Tile t = Level.tm.getTile(index);
	    return t == null || t.getBehavior() == Tile.BEHAVIOR_OPEN;
	}
	
	private final boolean teleport(final int off) {
	    final Panple pos = getPosition();
        final int d = ImtilX.DIM;
        final float cx = pos.getX();
        final int bx = (int) cx + ((isMirror() ? -1 : 1) * off);
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
	
	private final boolean defeat(final Character defeater, final int v, final byte defeatMode) {
		if (def.defeatHandler != null && !def.defeatHandler.onInteract(this, null)) {
			return false;
		} else if (avoidCount > 0) {
	        avoidCount--;
	        if (teleport(48)) {
	            return false;
	        }
	    }
		final Player player = (defeater != null && defeater.getClass() == Player.class) ? (Player) defeater : null;
		final boolean skipSplat = def.splatDecider != null && !def.splatDecider.onInteract(this, player);
		final BurstHandler splatHandler = skipSplat ? null : def.splatHandler;
		if (player != null && (v > 0 || splatHandler == null)) {
		    if (isRewarded(player)) {
				new GemBumped(player, this);
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
					default:
						throw new IllegalStateException("Unexpected defeatMode " + defeatMode);
				}
				stats.defeatedEnemyTypes.inc(def.code);
		    }
		}
		if (!skipSplat && v == 0 && def.splat != null) {
		    burst(def.splat, splatHandler);
		} else {
		    final Panple pos = getPosition();
    		final Tiles.Faller f = new Tiles.Faller((Panmage) getCurrentDisplay(), pos.getX(), pos.getY() + H, 0, v);
    		f.setMirror(isMirror());
    		f.setFlip(true);
		}
		destroy();
		return true;
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
	    final Burst b = CustomBurst.createBurst(anm, burstHandler);
	    final Panple pos = ref.getPosition();
        FurGuardiansGame.setPosition(b, pos.getX(), pos.getY() + yoff, FurGuardiansGame.DEPTH_SHATTER);
        b.setMirror(dir.isMirror());
        FurGuardiansGame.room.addActor(b);
	}
	
	protected final boolean onHurtPlayer(final Player player) {
	    if (def.hurtHandler != null) {
	        return def.hurtHandler.onInteract(this, player);
	    }
	    return true;
	}
	
	@Override
	protected final void onScrolled() {
		if (isDestroyed()) {
			return;
		}
		destroyIfOffScreen(this, 80);
	}
	
	protected final static void destroyIfOffScreen(final Panctor a, final int off) {
		final float x = a.getPosition().getX();
		final Panlayer layer = a.getLayer();
		if ((x + off) < layer.getViewMinimum().getX() || (x - (off * 2)) > layer.getViewMaximum().getX()) {
			a.destroy();
		}
	}
	
	@Override
	protected final boolean onHorizontal(final int off) {
		if (!def.ledgeTurn) {
			return false;
		} else if (!isGrounded()) { // Don't change direction if already in air
			return false;
		}
		final Panple pos = getPosition();
		final float x = pos.getX(), y = pos.getY();
		pos.addX(off);
		try {
			if (!isGrounded()) {
				pos.addY(-1);
				if (!isGrounded()) {
					hv *= -1;
					/*info("pos " + getPosition() + " bmin " + getBoundingMinimum() + " bmax " + getBoundingMaximum()
							+ " ol " + getOffLeft() + " or " + getOffRight());*/
					return true;
				}
			}
		} finally {
			pos.set(x, y);
		}
		return false;
	}
	
	@Override
    protected final void onLanded() {
	    if (def.landedHandler == null || !def.landedHandler.onInteract(this, null)) {
	        super.onLanded();
	    }
	}
	
	@Override
	protected final void onWall() {
		hv *= -1;
		Pansound.startSound(def.wallSound);
	}

	@Override
	protected final boolean onFell() {
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
		hv = (mirror ? -1 : 1) * def.hv;
	}
	
	protected static interface InteractionHandler {
		public boolean onInteract(final Enemy enemy, final Player player);
	}
	
	private abstract static class ColliderEnemy extends Enemy implements CollisionListener {
        protected ColliderEnemy(final EnemyDefinition def, final Panctor ref) {
            super(def, ref);
        }

        @Override
        public final void onCollision(final CollisionEvent event) {
            final Collidable collider = event.getCollider();
            // Player handles its own collisions, so only check for Enemy
            if (collider instanceof Enemy) {
                onCollision((Enemy) collider);
            }
        }
        
        protected abstract void onCollision(final Enemy collider);
	}
	
	public final static class ArmorBall extends ColliderEnemy {
        protected ArmorBall(final EnemyDefinition def, final Panctor ref) {
            super(def, ref);
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
        protected BounceBall(final EnemyDefinition def, final Panctor ref, final Player bouncer) {
            super(def, ref);
            lastStomper = bouncer;
        }

        @Override
        public final void onCollision(final Enemy collider) {
            //collider.onBump(this); // Doesn't give Player Gem
        	collider.onHit(lastStomper);
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
	    
	    @Override
	    public final void onAllOob(final AllOobEvent event) {
	        destroy();
	    }
	}
	
	protected final static SpawnFactory enemyFactory = new EnemyFactory();
	
	protected final static SpawnFactory wispFactory = new WispFactory();
	
	protected static interface SpawnFactory {
		public Panctor spawn(final EnemyDefinition def, final float x, final float y);
	}
	
	private final static class EnemyFactory implements SpawnFactory {
		@Override
		public final Enemy spawn(final EnemyDefinition def, final float x, final float y) {
			return new Enemy(def, x, y);
		}
	}
	
	private final static class WispFactory implements SpawnFactory {
		@Override
		public final Wisp spawn(final EnemyDefinition def, final float x, final float y) {
			return new Wisp(def, x, y);
		}
	}
}
