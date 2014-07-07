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

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.game.actor.CustomBurst.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.platform.Player.*;

public class Enemy extends Character {
	protected final static int DEFAULT_X = 5;
	protected final static int DEFAULT_H = 15;
	protected final static int DEFAULT_SPLAT = 20;
	private final static int DEFAULT_HV = 1;
	private final static int MIN_TIMER = 60;
	private final static int MAX_TIMER = 90;
	
	protected final static FinPanple DEFAULT_O = new FinPanple(8, 1, 0);
	private final static FinPanple DEFAULT_MIN = new FinPanple(-DEFAULT_X, 0, 0);
	private final static FinPanple DEFAULT_MAX = new FinPanple(DEFAULT_X, DEFAULT_H, 0);
	
	protected static int currentSplat = DEFAULT_SPLAT;
	
	protected final static class EnemyDefinition {
		private final Panimation walk;
		private final boolean ledgeTurn;
		protected Panimation splat;
		private final Panimation attack;
		private final int avoidCount;
		private final int offX;
		private final int h;
		private final int hv;
		protected Panimation projectile = null;
		protected BurstHandler splatHandler = null;
		protected InteractionHandler stepHandler = null;
		protected InteractionHandler landedHandler = null;
		protected InteractionHandler hurtHandler = null;
		protected InteractionHandler stompHandler = null;
		protected InteractionHandler rewardHandler = null;
		protected InteractionHandler defeatHandler = null;
		
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
			final Img[] strip = ImtilX.loadStrip("org/pandcorps/platform/res/enemy/Enemy0" + ind + ".png", d), walk;
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
			final Panple n, x, o = (d == ImtilX.DIM) ? DEFAULT_O : new FinPanple(d / 2, 1, 0);
			if (offX == DEFAULT_X && h == DEFAULT_H) {
			    n = DEFAULT_MIN;
			    x = DEFAULT_MAX;
			} else {
			    n = new FinPanple(-offX, 0, 0);
			    x = new FinPanple(offX, h, 0);
			}
			this.walk = PlatformGame.createAnm(id, 6, o, n, x, walk);
			this.ledgeTurn = ledgeTurn;
			this.splat = splat ? PlatformGame.createAnm(id + ".splat", currentSplat, o, n, x, strip[2]) : null;
			currentSplat = DEFAULT_SPLAT;
			this.attack = (hv == 0 && strip.length > 2) ? PlatformGame.createAnm(id + ".attack", 20, o, n, x, strip[2]) : null;
			Img.close(strip);
			this.avoidCount = avoidCount;
			this.offX = offX;
			this.h = h;
			this.hv = hv;
		}
	}
	
	private final EnemyDefinition def;
	private int avoidCount = 0;
	protected int timer = 0;
	protected int timerMode = 0;
	protected boolean full = false;
	
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
		PlatformGame.room.addActor(this);
		PlatformGame.setPosition(this, x, y, PlatformGame.DEPTH_ENEMY);
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
	                    PlatformGame.room.addActor(new Projectile(def.projectile, this, Mathtil.rand(PlatformGame.pcs).player));
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
        for (final PlayerContext pc : PlatformGame.pcs) {
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
		if (def.stompHandler == null || !def.stompHandler.onInteract(this, stomper)) {
			return defeat(stomper, 0);
		} else {
			return true;
		}
	}
	
	@Override
	protected final void onBump(final Character bumper) {
		defeat(bumper, Player.VEL_BUMP);
	}
	
	private final static boolean isFree(final Tile t) {
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
            burst(PlatformGame.teleport);
            pos.set(x, fy);
            final Pangine engine = Pangine.getEngine();
            for (final PlayerContext pc : PlatformGame.pcs) {
                if (engine.isCollision(this, pc.player)) {
                    destroy();
                    return true;
                }
            }
            burst(PlatformGame.teleport);
            return true;
        }
        return false;
	}
	
	private final boolean defeat(final Character defeater, final int v) {
		if (def.defeatHandler != null && !def.defeatHandler.onInteract(this, null)) {
			return false;
		} else if (avoidCount > 0) {
	        avoidCount--;
	        if (teleport(48)) {
	            return false;
	        }
	    }
		if (defeater != null && defeater.getClass() == Player.class && (v > 0 || def.splatHandler == null)) {
		    final Player player = (Player) defeater;
		    if (def.rewardHandler == null || def.rewardHandler.onInteract(this, player)) {
				new GemBumped(player, this);
				player.levelDefeatedEnemies++;
		    }
		}
		if (v == 0 && def.splat != null) {
		    burst(def.splat, def.splatHandler);
		} else {
		    final Panple pos = getPosition();
    		final Tiles.Faller f = new Tiles.Faller((Panmage) getCurrentDisplay(), pos.getX(), pos.getY() + H, 0, v);
    		f.setMirror(isMirror());
    		f.setFlip(true);
		}
		destroy();
		return true;
	}
	
	private void burst(final Panimation anm) {
		burst(anm, null);
	}
	
	protected void burst(final Panimation anm, final BurstHandler burstHandler) {
		burst(anm, this, burstHandler);
	}
	
	protected void burst(final Panimation anm, final Panctor dir, final BurstHandler burstHandler) {
	    final Burst b = CustomBurst.createBurst(anm, burstHandler);
	    final Panple pos = getPosition();
        PlatformGame.setPosition(b, pos.getX(), pos.getY(), PlatformGame.DEPTH_SHATTER);
        b.setMirror(dir.isMirror());
        PlatformGame.room.addActor(b);
	}
	
	protected final boolean onHurtPlayer(final Player player) {
	    if (def.hurtHandler != null) {
	        return def.hurtHandler.onInteract(this, player);
	    }
	    return true;
	}
	
	@Override
	protected final void onScrolled() {
		if (!isDestroyed() && getPosition().getX() + 80 < getLayer().getViewMinimum().getX()) {
			destroy();
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
	}

	@Override
	protected final boolean onFell() {
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
            } else if (collider.def == PlatformGame.imp) {
                PlatformGame.openArmoredImp(this, collider);
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
        protected BounceBall(final EnemyDefinition def, final Panctor ref) {
            super(def, ref);
        }

        @Override
        public final void onCollision(final Enemy collider) {
            collider.onBump(this);
        }
    }
}
