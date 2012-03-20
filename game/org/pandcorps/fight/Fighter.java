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
package org.pandcorps.fight;

import org.pandcorps.core.*;
import org.pandcorps.core.col.*;
import org.pandcorps.fight.Background.BackgroundDefinition;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.impl.FinPanple;

public final class Fighter extends Panctor implements StepListener, CollisionListener, AnimationEndListener {
    
    /*package*/ final static int DEPTH_SHADOW = -480;
    /*package*/ final static int DEPTH_BG = -481;
    
    private final static byte MODE_STILL = 0;
    private final static byte MODE_WALK = 1;
    //private final static byte MODE_QUICK = 2;
    private final static byte MODE_MOVE = 2;
    private final static byte MODE_HURT = 3;
    private final static byte MODE_BURN = 4;
    
    private final static byte THRESHOLD_QUICK = 12; // Two quick attacks must be performed within this many frames to be considered a combo building to a strong attack
    private final static byte THRESHOLD_STRONG = 16; // A strong attack must be performed within this many frames after the last quick attack
    
    /*package*/ Controller controller = null;
    /*package*/ final IdentityHashSet<Projectile> linkedProjectiles = new IdentityHashSet<Projectile>();
    
    private final static float speed = 2;
    
    public final static class FighterDefinition {
        private final String name;
        private final Panimation still;
        private final Panimation walk;
        //private final Panmage quick;
        private final Move quick;
        private final Move strong;
        private final Move spec1;
        private final Move spec2;
        //private final Panmage hurt;
        /*
        This will likely be one frame.
        The hurt status won't end when the animation ends.
        It will just loop.
        The animation is not intended to show the Fighter reeling further back with each frame.
        The intent is to allow a Fighter with some intrinsic animation.
        Like a flashing light, puffs of smoke, fire, etc.
        */
        private final Panimation hurt;
        private final Panimation blood;
        
        public FighterDefinition(final String name, final Panimation still, final Panimation walk,
                                 final Move quick, final Move strong, final Move spec1, final Move spec2,
                                 final Panimation hurt, final Panimation blood) {
            this.name = name;
            this.still = still;
            this.walk = walk;
            this.quick = quick;
            this.strong = strong;
            this.spec1 = spec1;
            this.spec2 = spec2;
            this.hurt = hurt;
            this.blood = blood;
        }
        
        public final String getName() {
            return name;
        }
        
        public final Panimation getStill() {
            return still;
        }
    }
    
    /*package*/ final FighterDefinition def;
    private final Decoration shadow;
    private byte mode = MODE_STILL;
    private Panimation reactView = null;
    private Move move = null;
    private int moveLoop = 0;
    private float dx = 0;
    private float dy = 0;
    private byte hurtTime = 0;
    private byte hits = 0;
    private long lastHit = 0;
    private int health = 256;
    
    public Fighter(final String id, final Panroom room, final FighterDefinition def) {
        super(id);

        this.def = def;
        setView(def.still);
        shadow = new Decoration(id + ".shadow");
        shadow.setView(FightGame.getShadowImage());
        room.addActor(this);
        room.addActor(shadow);
        shadow.getPosition().setZ(DEPTH_SHADOW);
    }
    
    @Override
    public void onStep(final StepEvent event) {
        controller.step();
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
                changeView(def.walk);
                mode = MODE_STILL;
                break;
            case MODE_MOVE :
                changeView(move.anim);
                move();
                //changeView(quick.anim);
                //mode = MODE_STILL;
                break;
            case MODE_HURT :
                changeView(def.hurt);
                duringHurt();
                break;
            case MODE_BURN :
                changeView(reactView);
                duringHurt();
                break;
            default :
                changeView(def.still);
                break;
        }
        final BackgroundDefinition background = FightGame.getBackground().def;
        pos.add(dx, dy, background.minX, background.minY, background.maxX, background.maxY);
        final float px = pos.getX();
        if (mode == MODE_MOVE && (px < 0 || px > Pangame.getGame().getCurrentRoom().getSize().getX())) {
            moveLoop = 1;
        }
        pos.setZ(-pos.getY());
        //shadow.getPosition().set(pos);
        shadow.getPosition().set(px, pos.getY());
        shadow.setVisible(FightGame.isShadowVisible());
        shadow.setMirror(isMirror());
        dx = 0;
        dy = 0;
    }
    
    private final void duringHurt() {
        hurtTime--;
        if (hurtTime <= 0) {
            mode = MODE_STILL;
            reactView = null;
        }
    }
    
    @Override
    public void onAnimationEnd(final AnimationEndEvent event) {
        if (moveLoop > 0) {
            moveLoop--;
            if (moveLoop == 0) {
                mode = MODE_STILL;
                move = null;
                moveLoop = 0;
            }
        }
        changeView(def.still);
    }
    
    @Override
    public void onCollision(final CollisionEvent event) {
        if (event.getCollider().getClass() == Projectile.class) {
            final Projectile projectile = (Projectile) event.getCollider();
            final Emitter emitter = projectile.emitter;
            final Fighter pfighter = projectile.fighter;
            if (pfighter != this && projectile.canHit()) {
//System.out.println("Hit");
                switch (emitter.react) {
                    case Projectile.REACT_HURT :
                        mode = MODE_HURT;
                        hurtTime = 4;
                        break;
                    case Projectile.REACT_BURN :
                        mode = MODE_BURN;
                        reactView = emitter.reactView;
                        hurtTime = 12;
                        break;
                }
                projectile.hit(this);
                // Clear linkedProjectiles
                for (final Projectile p : linkedProjectiles) {
                    //p.die(); // Removes from linkedProjectiles
                    //p.destroy(); // Should be only other operation in p.die()
                    p.dieWithoutUnlink();
                }
                linkedProjectiles.clear();
                if (emitter.type == Projectile.TYPE_QUICK) {
                    final long clock = Pangine.getEngine().getClock();
                    if (clock != pfighter.lastHit) {
                        if (clock - pfighter.lastHit < THRESHOLD_QUICK) {
                            pfighter.hits++;
                        } else {
                            pfighter.hits = 1;
                        }
                        pfighter.lastHit = clock;
                    }
//System.out.println("Hit - hits = " + pfighter.hits + "; lastHit = " + pfighter.lastHit);
                }
                if (pfighter.mode == MODE_MOVE && pfighter.move.stopAfterHit) {
                    pfighter.moveLoop = 1;
                }
                final Panimation impactAnim;
                switch (emitter.impact) {
                    case Projectile.IMPACT_SPARK :
                        impactAnim = FightGame.bamAnim;
                        break;
                    case Projectile.IMPACT_EXPLOSION :
                        impactAnim = emitter.impactView; // Store projectile-specific color of anim in Emitter
                        break;
                    default :
                        impactAnim = def.blood; // Store character-specific color of anim in Fighter
                        break;
                }
                final float xo = Mathtil.randf(-6, 6), yo = Mathtil.randf(-6, 6); // random offset for x/y
                addBurst(impactAnim, xo, yo);
                health -= projectile.emitter.damage;
                if (health <= 0) {
                	health = 0;
                	addBurst(FightGame.puffAnim, 0, 0);
                	destroy();
                	shadow.destroy(); // linked projectiles destroyed when hit, regardless of defeat
                }
                final String text = Integer.toString(health);
                final Info info = new Info(text);
                add(info, 0, 4, -1); // - text.length() * 4
                info.centerX();
            }
        }
    }
    
    private final void addBurst(final Panimation anim, final float xo, final float yo) {
    	add(new Burst(Pantil.vmid(), anim), xo, 6 + yo, 1);
    }
    
    private final void add(final Panctor actor, final float xo, final float yo, final float zo) {
        final Panple pos = getPosition();
        actor.getPosition().set(pos.getX() + xo, pos.getY() + yo, pos.getZ() + zo);
        Pangame.getGame().getCurrentRoom().addActor(actor);
    }
    
    private final boolean canAttack() {
        if (isFree()) {
            //mode = MODE_QUICK;
            mode = MODE_MOVE;
            return true;
        }
        return false;
    }
    
    protected final void attack() {
        if (canAttack()) {
//System.out.println("Attack - hits = " + hits + "; clock = " + Pangine.getEngine().getClock() + "; lastHit = " + lastHit);
            final Move move;
            if (hits < 2) {
                move = def.quick;
            } else {
                hits = 0;
                final long clock = Pangine.getEngine().getClock();
                if (clock - lastHit < THRESHOLD_STRONG) {
                    move = def.strong;
                } else {
                    move = def.quick;
                }
            }
//System.out.println("Move = " + (move == quick ? "quick" : "strong"));
            startMove(move);
            /*final Projectile projectile = new Projectile(Pantil.vmid(), this, Projectile.TYPE_QUICK, (byte) 1);
            projectile.setVisible(false);
            final Panple pos = getPosition();
            projectile.getPosition().set(pos.getX() + (16 * (isMirror() ? -1 : 1)), pos.getY());
            this.getLayer().addActor(projectile);
            linkedProjectiles.add(projectile);*/
        }
    }
    
    private final void startMove(final Move move) {
        this.move = move;
        moveLoop = move.loop;
    }
    
    protected final void strong() {
        startMoveIfCanAttack(def.strong);
    }
    
    private final void move() {
        final int currentFrame = getCurrentFrame();
        final int currentFrameDur = getCurrentFrameDur();
        final Panple pos = getPosition();
        //final boolean mirror = isMirror();
        final boolean tmirror = isMirror();
        
        final int prevFrameIndex;
        if (currentFrameDur > 0) {
            prevFrameIndex = currentFrame;
        } else if (currentFrame > 0) {
            prevFrameIndex = currentFrame - 1;
        //TODO
        // } else if (first dur of first frame of non-first loop) {
        //    prevFrameIndex = move.mframes.length - 1;
        } else { // Start of Move
            prevFrameIndex = -1;
        }

        if (prevFrameIndex >= 0) {
            final MoveFrame prevframe = move.mframes[prevFrameIndex];
            if (prevframe.trail != null) {
                final Trail t = new Trail(Pantil.vmid(), prevframe.trail);
                t.getPosition().set(pos);
                // Must combine Fighter mirror with previous MoveFrame mirror
                t.setMirror(tmirror ^ prevframe.pframe.isMirror());
                getLayer().addActor(t);
            }
        }
        
        final MoveFrame mframe = move.mframes[currentFrame];
        /*
        At first we used this.mirror for projectile steps below.
        We only combined with frame.mirror for this trail step above (previous frame).
        I think we always want to use the combined mirror for projectiles (current frame).
        A spinning kick should attack the other side when mirrored.
        Velocity should just use this.mirror.
        A sliding spinning kick shouldn't go back and forth.
        */
        final int tmult = tmirror ? -1 : 1;
        final boolean mirror = tmirror ^ mframe.pframe.isMirror();
        final int mult = mirror ? -1 : 1; // Maybe add Panctor.getMirrorMultiplier()
        
        /*
        When position change is before the frameDur exit check,
        it happens for each tick, even if the animation frame doesn't change.
        That seems right for a sliding animating attack,
        like a sliding spinning kick.
        Maybe there are other moves where we would only expect the position
        change along with an animation frame change.
        For that we would need to do the position change after the exit check.
        */
        final FinPanple vel = mframe.velocity;
        //pos.add(vel.getX() * mult, vel.getY(), vel.getZ()); // Ignores boundaries, Z would be clobbered anyway
        dx = vel.getX() * tmult;
        dy = vel.getY();
        
        // Could add Panimation callback to evaluate each MoveFrame
        if (currentFrameDur != 0) {
            return;
        }
        
        if (mframe.emitters != null) {
            for (final Emitter em : mframe.emitters) {
                //final Projectile p = new Projectile(Pantil.vmid(), this, em.type, em.impact, em.react, mirror ? em.mirVel : em.vel, em.time, em.anim);
                final Projectile p = new Projectile(Pantil.vmid(), this, em, mirror ? em.mirVel : em.vel);
                p.getPosition().set(pos.getX() + (em.xoff * mult), pos.getY() + em.yoff, pos.getZ());
                p.setMirror(mirror);
                getLayer().addActor(p);
                if (em.linked) {
                    linkedProjectiles.add(p);
                }
/*System.out.println("self pos: " + getPosition());
System.out.println("self min: " + getBoundingMinimum());
System.out.println("self max: " + getBoundingMaximum());*/
/*System.out.println("proj pos: " + p.getPosition());
System.out.println("proj min: " + p.getBoundingMinimum());
System.out.println("proj max: " + p.getBoundingMaximum());*/
/*final Fighter t = Ai.getTarget(this);
System.out.println("targ pos: " + t.getPosition());
System.out.println("targ min: " + t.getBoundingMinimum());
System.out.println("targ max: " + t.getBoundingMaximum());*/
            }
        }
    }
    
    // Maybe add Trail and Projectile with same method
    //private final void addActor(final Panctor actor, final Panple pos, final
    
    protected final void spec1() {
        spec(def.spec1);
    }
    
    protected final void spec2() {
        spec(def.spec2);
    }
    
    private final void spec(final Move m) {
        //TODO normal attacks fill a meter which enables specials
        startMoveIfCanAttack(m);
    }
    
    private final void startMoveIfCanAttack(final Move m) {
        if (canAttack()) {
            startMove(m);
        }
    }
    
    protected final void walkDown() {
        walk(0, -speed);
    }
    
    protected final void walkUp() {
        walk(0, speed);
    }
    
    protected final void walkLeft() {
        walk(-speed, 0);
        //setMirror(true);
    }
    
    protected final void walkRight() {
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
    
    final boolean isFree() {
        return mode == MODE_STILL || mode == MODE_WALK;
    }
    
    @Override
    protected final void onDestroy() {
        controller.destroy();
    }
}
