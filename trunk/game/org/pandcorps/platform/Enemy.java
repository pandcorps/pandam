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
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.platform.Player.*;

public final class Enemy extends Character {
	private final static int DEFAULT_X = 5;
	private final static int DEFAULT_H = 15;
	private final static int DEFAULT_HV = 1;
	private final static int MIN_TIMER = 60;
	private final static int MAX_TIMER = 90;
	
	protected final static FinPanple DEFAULT_O = new FinPanple(8, 1, 0);
	private final static FinPanple DEFAULT_MIN = new FinPanple(-DEFAULT_X, 0, 0);
	private final static FinPanple DEFAULT_MAX = new FinPanple(DEFAULT_X, DEFAULT_H, 0);
	
	protected final static class EnemyDefinition {
		private final Panimation walk;
		private final boolean ledgeTurn;
		private final Panimation splat;
		private final Panimation attack;
		private final int avoidCount;
		private final int offX;
		private final int h;
		private final int hv;
		
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
			if (splat) {
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
			this.splat = splat ? PlatformGame.createAnm(id + ".splat", 20, o, n, x, strip[2]) : null;
			this.attack = hv == 0 ? PlatformGame.createAnm(id + ".attack", 20, o, n, x, strip[2]) : null;
			Img.close(strip);
			this.avoidCount = avoidCount;
			this.offX = offX;
			this.h = h;
			this.hv = hv;
		}
	}
	
	private final EnemyDefinition def;
	private int avoidCount = 0;
	private int timer = 0;
	private int timerMode = 0;
	
	protected Enemy(final EnemyDefinition def, final float x, final float y) {
		super(def.offX, def.h);
		this.def = def;
		setView(def.walk);
		setMirror(true);
		hv = -def.hv;
		PlatformGame.room.addActor(this);
		PlatformGame.setPosition(this, x, y, PlatformGame.DEPTH_ENEMY);
		avoidCount = def.avoidCount;
		if (hv == 0) {
		    initTimer(0);
		}
	}
	
	private final void initTimer(final int timerMode) {
	    timer = Mathtil.randi(MIN_TIMER, MAX_TIMER);
	    this.timerMode = timerMode;
	}
	
	@Override
	protected final boolean onStepCustom() {
	    if (hv == 0) {
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
	                    PlatformGame.room.addActor(new Projectile(PlatformGame.projectile1, this, Mathtil.rand(PlatformGame.pcs).player));
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
		return defeat(stomper, 0);
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
        final int bx = (int) pos.getX() + ((isMirror() ? -1 : 1) * off);
        final float x = ((bx / d) * d) + 8;
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
	    if (avoidCount > 0) {
	        avoidCount--;
	        if (teleport(48)) {
	            return false;
	        }
	    }
		if (defeater != null && defeater.getClass() == Player.class) {
		    final Player player = (Player) defeater;
			new GemBumped(player, this);
			player.levelDefeatedEnemies++;
		}
		if (v == 0 && def.splat != null) {
		    burst(def.splat);
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
	    final Burst b = new Burst(anm);
	    final Panple pos = getPosition();
        PlatformGame.setPosition(b, pos.getX(), pos.getY(), PlatformGame.DEPTH_SHATTER);
        PlatformGame.room.addActor(b);
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
	protected final void onWall() {
		hv *= -1;
	}

	@Override
	protected final boolean onFell() {
		destroy();
		return true;
	}
}
