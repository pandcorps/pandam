/*
Copyright (c) 2009-2018, Andrew M. Martin
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
package org.pandcorps.championsofslam;

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;

public class Cpu extends Champion {
    private CpuState state = null;
    private int stateTimer = 0;
    private Champion target = null;
    private int randomV = 0;
    private int randomHv = 0;
    private int randomTimer = 0;
    
    public Cpu(final ChampionDefinition def, final Set<Champion> team) {
        super(def, team);
    }
    
    @Override
    protected final void onStepStart() {
        if (ChampionsOfSlamGame.paused) {
            return;
        } else if (Panctor.isDestroyed(target) || target.isPaused() || target.isDefeated()) {
            pickTarget();
            stateTimer = 0;
        }
        if (stateTimer <= 0) {
            if (state == stateWait) {
                final int r = Mathtil.randi(0, 9999);
                final int advanceThreshold = (champions.size() < 8) ? 6500 : 4000, retreatThreshold = (10000 + advanceThreshold) / 2;
                if (r < advanceThreshold) {
                    state = stateAdvance;
                } else if (r < retreatThreshold) {
                    state = stateRetreat;
                } else {
                    state = stateRandom;
                }
            } else {
                state = stateWait;
            }
            stateTimer = state.init(this);
        }
        if (state.onStep(this)) {
            stateTimer = 0;
        } else {
            stateTimer--;
        }
    }
    
    @Override
    protected final void onMinX() {
        state.onMinX(this);
    }
    
    @Override
    protected final void onMaxX() {
        state.onMaxX(this);
    }
    
    @Override
    protected final void onMinY() {
        state.onMinY(this);
    }
    
    @Override
    protected final void onMaxY() {
        state.onMaxY(this);
    }
    
    @Override
    protected final void onHurt(final HitBox hitBox) {
        target = hitBox.src;
    }
    
    private final void pickTarget() {
        final Panlayer layer = getLayer();
        if (layer == null) {
            return;
        }
        Champion closest = null;
        double closestDistance = -1;
        final Panple pos = getPosition();
        for (final Champion champion : champions) {
            if (champion == this) {
                continue;
            } else if ((team != null) && team.contains(champion)) {
                continue;
            } else if (champion.isPaused()) {
                continue;
            }
            final double distance = champion.getPosition().getDistance2(pos);
            if ((closest == null) || (distance < closestDistance)) {
                closest = champion;
                closestDistance = distance;
            }
        }
        target = closest;
    }
    
    public abstract static class CpuState {
        public abstract boolean onStep(final Cpu cpu);
        
        public abstract int init(final Cpu cpu);
        
        protected void onMinX(final Cpu cpu) {
        }
        
        protected void onMaxX(final Cpu cpu) {
        }
        
        protected void onMinY(final Cpu cpu) {
        }
        
        protected void onMaxY(final Cpu cpu) {
        }
    }
    
    private final static CpuState stateWait = new CpuState() {
        @Override
        public final boolean onStep(final Cpu cpu) {
            return false;
        }
        
        @Override
        public final int init(final Cpu cpu) {
            return Mathtil.randi(15, 90);
        }
    };
    
    private final static CpuState stateAdvance = new CpuState() {
        @Override
        public final boolean onStep(final Cpu cpu) {
            return onStepAdvance(cpu, true);
        }
        
        @Override
        public final int init(final Cpu cpu) {
            return Mathtil.randi(30, 120);
        }
    };
    
    public final static boolean onStepAdvance(final Cpu cpu, final boolean attackAllowed) {
        final Champion target = cpu.target;
        if (Panctor.isDestroyed(target)) {
            return true;
        }
        final Panple pos = cpu.getPosition(), tarPos = target.getPosition();
        final float x = pos.getX(), y = pos.getY(), tarX = tarPos.getX(), tarY = tarPos.getY();
        if (attackAllowed && (Math.abs(tarX - x) < 8) && (Math.abs(tarY - y) < 6)) {
            cpu.state = stateRandom;
            stateRandom.init(cpu);
        } else if (x < tarX) {
            if ((x < (tarX - 10)) || cpu.isMirror()) {
                cpu.hv = 1;
            }
        } else if (x > tarX) {
            if ((x > (tarX + 10)) || !cpu.isMirror()) {
                cpu.hv = -1;
            }
        }
        if (y < (tarY - 2)) {
            cpu.v = 1;
        } else if (y > (tarY + 2)) {
            cpu.v = -1;
        }
        if ((cpu.hv == 0) && (cpu.v == 0) && Mathtil.rand(5)) {
            cpu.onAttack();
        }
        return false;
    }
    
    private final static CpuState stateRetreat = new CpuState() {
        @Override
        public final boolean onStep(final Cpu cpu) {
            if (onStepAdvance(cpu, false)) {
                return true;
            }
            cpu.hv *= -1;
            cpu.v *= -1;
            return false;
        }
        
        @Override
        public final int init(final Cpu cpu) {
            return Mathtil.randi(30, 60);
        }
        
        @Override
        protected final void onMinX(final Cpu cpu) {
            cpu.stateTimer = 0;
        }
        
        @Override
        protected final void onMaxX(final Cpu cpu) {
            cpu.stateTimer = 0;
        }
        
        @Override
        protected final void onMinY(final Cpu cpu) {
            cpu.stateTimer = 0;
        }
        
        @Override
        protected final void onMaxY(final Cpu cpu) {
            cpu.stateTimer = 0;
        }
    };
    
    private final static CpuState stateRandom = new CpuState() {
        @Override
        public final boolean onStep(final Cpu cpu) {
            cpu.hv = cpu.randomHv;
            cpu.v = cpu.randomV;
            cpu.randomTimer--;
            if (cpu.randomTimer <= 0) {
                if (Mathtil.rand()) {
                    cpu.randomHv = changeV(cpu.randomHv, cpu.randomV);
                } else {
                    cpu.randomV = changeV(cpu.randomV, cpu.randomHv);
                }
                initRandomTimer(cpu);
            }
            return false;
        }
        
        @Override
        public final int init(final Cpu cpu) {
            final int r1 = randomV(), r2 = randomMovingV();
            if (Mathtil.rand()) {
                cpu.randomHv = r1;
                cpu.randomV = r2;
            } else {
                cpu.randomHv = r2;
                cpu.randomV = r1;
            }
            initRandomTimer(cpu);
            return Mathtil.randi(60, 120);
        }
        
        private final void initRandomTimer(final Cpu cpu) {
            cpu.randomTimer = Mathtil.randi(20, 45);
        }
        
        private final int randomV() {
            return Mathtil.randi(-1, 1);
        }
        
        private final int randomMovingV() {
            return (Mathtil.randi(0, 1) * 2) - 1;
        }
        
        private final int changeV(final int curr, final int other) {
            if ((curr == 0) || (other == 0)) {
                return randomMovingV();
            } else {
                return (curr < 0) ? Mathtil.randi(0, 1) : Mathtil.randi(-1, 0);
            }
        }
        
        @Override
        protected final void onMinX(final Cpu cpu) {
            cpu.randomHv = 1;
        }
        
        @Override
        protected final void onMaxX(final Cpu cpu) {
            cpu.randomHv = -1;
        }
        
        @Override
        protected final void onMinY(final Cpu cpu) {
            cpu.randomV = 1;
        }
        
        @Override
        protected final void onMaxY(final Cpu cpu) {
            cpu.randomV = -1;
        }
    };
}
