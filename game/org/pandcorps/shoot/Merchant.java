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
import java.util.List;

import org.pandcorps.pandax.text.*;
import org.pandcorps.shoot.Weapon.*;

public class Merchant extends ShooterController {
    private Shooter initiator = null;
    
	@Override
	public final boolean onInteract(final Shooter initiator) {
	    this.initiator = initiator;
		final ShooterController controller = (ShooterController) initiator.getController();
		final ArrayList<String> opts = toMenu(initiator.weapons);
		final class MerchantListener implements RadioSubmitListener {
            @Override
            public void onSubmit(final RadioSubmitEvent event) {
                final CharSequence elem = event.getElement();
                for (final Weapon weapon : initiator.weapons) {
                    if (getUpgradeLabel(weapon).equals(elem)) {
                        upgrade(event.getGroup().getLabel(), controller, weapon);
                    }
                }
            }
        }
		menu(opts, new MerchantListener(), "Upgrade Weapons");
		return true;
	}
	
	private void init(final TextItem t) {
	    final Pantext label = t.getLabel();
        label.setBorderStyle(BorderStyle.Simple);
        label.setBackground(Pantext.CHAR_SPACE);
        t.init(shooter);
	}
	
	private void menu(final ArrayList<String> opts, final RadioSubmitListener listener, final String title) {
		final RadioGroup rg = new RadioGroup(ShootGame.font, opts, listener);
		rg.setTitle(title);
        init(rg);
	}
	
	private final static String UP = ""; // "Upgrade ";
	private final static String EXIT = "Exit";
	
	private void upgrade(final Pantext parent, final ShooterController controller, final Weapon weapon) {
		final ArrayList<String> opts = toMenu(weapon.getArguments());
		final WeaponDefinition def = weapon.def;
	    final class WeaponListener implements RadioSubmitListener {
			@Override
			public void onSubmit(final RadioSubmitEvent event) {
				final CharSequence elem = event.getElement();
				for (final WeaponArgument arg : weapon.getArguments()) {
    				if (getUpgradeLabel(arg).equals(elem)) {
    					final String m = arg.upgrade(initiator) ? "Enjoy it, friend." : "Sorry, friend.";
    					msg(m, new MessageCloseListener() { @Override public void onClose(final MessageCloseEvent event) {
    						upgrade(parent, controller, weapon);}});
    					return;
    				}
				}
				if (EXIT.equals(elem)) {
				    onInteract(initiator);
				    return;
				}
				throw new IllegalStateException("Could not understand " + elem);
			}
		}
	    menu(opts, new WeaponListener(), "Upgrade " + def.name);
	}
	
	private void msg(final String text, final MessageCloseListener listener) {
		final Message m = new Message(ShootGame.font, text, listener);
		init(m);
	}
	
	private ArrayList<String> toMenu(final List<? extends Upgradeable> list) {
		final ArrayList<String> opts = new ArrayList<String>(list.size());
		for (final Upgradeable u : list) {
	        if (u.isUpgradeApplicable()) {
	            //TODO display if applicable, grey out if maxed
	            opts.add(getUpgradeLabel(u));
	        }
		}
		opts.add(EXIT);
		return opts;
	}
	
	private final static String getUpgradeLabel(final Upgradeable u) {
		final StringBuilder b = new StringBuilder();
		b.append(UP).append(u.getName());
		if (u.isUpgradePossible()) {
			if (u instanceof WeaponArgument) {
				final WeaponArgument arg = (WeaponArgument) u;
				b.append(' ').append(arg.getValue()).append('+').append(arg.parm.getUpgradeIncrement());
				final int cost = arg.getUpgradeCost();
				b.append(' ').append(ShootGame.CHAR_MONEY).append(cost);
			}
		} else {
			b.append(" (Maxed)");
		}
		return b.toString();
	}
}
