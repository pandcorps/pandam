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
package org.pandcorps.shoot;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.shoot.Shooter.ShooterDefinition;

public class Spawner extends Panctor implements StepListener {
    private final static int OFF = 20;
	private final ShooterDefinition def;
	private final int maxTotal;
	private final int maxCurrent;
	private int total = 0;
	private int current = 0;
	
	public Spawner(final Panlayer room, final float x, final ShooterDefinition def, final int maxTotal, final int maxCurrent) {
		setVisible(false);
		this.def = def;
		this.maxTotal = maxTotal;
		this.maxCurrent = maxCurrent;
		room.addActor(this);
		getPosition().setX(x);
	}
	
	@Override
	public void onStep(final StepEvent event) {
		final float dist = Ai.getDistance(this, ShootGame.shooter); // Positive when Spawner is right of Shooter
		if (dist < -Ai.DISTANCE_SCROLLED) {
			destroy();
			return;
		} else if (dist > 0) {
			return;
		} else if (current < maxCurrent) {
			spawn();
		}
	}
	
	private void spawn() {
		final Shooter enemy = new Shooter(Pantil.vmid(), getLayer(), def);
		final Panlayer layer = getLayer();
		final Panple guyMin = enemy.getMin(), guyMax = enemy.getMax();
		float x;
		if (Mathtil.rand()) {
		    x = layer.getViewMaximum().getX() + OFF;
		    if (x >= guyMax.getX()) {
		        x = layer.getViewMinimum().getX() - OFF;
		    }
		} else {
		    x = layer.getViewMinimum().getX() - OFF;
		    if (x <= guyMin.getX()) {
		        x = layer.getViewMaximum().getX() + OFF;
		    }
		}
		enemy.getPosition().set(x, Mathtil.randf(guyMin.getY() + 1, guyMax.getY() - 1));
		enemy.spawner = this;
		new Ai().setShooter(enemy);
		if (def.weapon != null) {
			final Weapon w = new Weapon(def.weapon);
			if (def.isBoss()) {
				w.setInfiniteIntern();
			}
			enemy.setWeapon(w);
		}
		total++;
		if (total >= maxTotal) {
			destroy();
		}
		current++;
	}
	
	public void remove() {
		current--;
	}
}
