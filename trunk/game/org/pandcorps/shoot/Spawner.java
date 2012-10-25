package org.pandcorps.shoot;

import org.pandcorps.core.Pantil;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.shoot.Shooter.ShooterDefinition;

public class Spawner extends Panctor implements StepListener {
	private final ShooterDefinition def;
	private final int maxTotal;
	private final int maxCurrent;
	private int total = 0;
	private int current = 0;
	
	public Spawner(final Panlayer room, final ShooterDefinition def, final int maxTotal, final int maxCurrent) {
		setVisible(false);
		this.def = def;
		this.maxTotal = maxTotal;
		this.maxCurrent = maxCurrent;
		room.addActor(this);
	}
	
	@Override
	public void onStep(final StepEvent event) {
		if (current < maxCurrent) {
			spawn();
		}
	}
	
	private void spawn() {
		final Shooter enemy = new Shooter(Pantil.vmid(), getLayer(), def);
		enemy.getPosition().set(192, 64);
		enemy.setMirror(true);
		enemy.spawner = this;
		new Ai().setShooter(enemy);
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
