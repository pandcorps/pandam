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

import java.util.ArrayList;

import org.pandcorps.pandax.text.*;
import org.pandcorps.shoot.Weapon.WeaponDefinition;

public class Merchant extends ShooterController {
	@Override
	public final boolean onInteract(final Shooter initiator) {
		final ShooterController controller = (ShooterController) initiator.getController();
		//controller.setShooter(null);
		shooter.getLayer().setActive(false); //TODO Move this and reactivate into TextItem?
		upgrade(initiator, controller, initiator.weapon); //TODO list of weapons
		return true;
	}
	
	private final static String UP_SPRAY = "Upgrade Spray";
	
	private void upgrade(final Shooter initiator, final ShooterController controller, final Weapon weapon) {
		final ArrayList<String> opts = new ArrayList<String>();
		final WeaponDefinition def = weapon.def;
		if (def.minSpray < def.maxSpray) {
			//TODO display if applicable, grey out if maxed
			//TODO Class to standardize upgrades
			opts.add(UP_SPRAY);
		}
		final class WeaponListener implements RadioSubmitListener {
			@Override
			public void onSubmit(final RadioSubmitEvent event) {
				final CharSequence elem = event.getElement();
				if (UP_SPRAY.equals(elem)) {
					weapon.upgradeSpray();
				}
				//controller.setShooter(initiator);
				shooter.getLayer().setActive(true);
			}
		}
		final RadioGroup rg = new RadioGroup(ShootGame.font, opts, new WeaponListener());
		rg.setTitle("Upgrade " + def.name);
		rg.init();
	}
}
