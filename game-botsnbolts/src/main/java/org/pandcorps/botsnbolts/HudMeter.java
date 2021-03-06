/*
Copyright (c) 2009-2021, Andrew M. Martin
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
package org.pandcorps.botsnbolts;

import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.botsnbolts.Profile.*;
import org.pandcorps.pandam.*;

public abstract class HudMeter extends Panctor {
    protected final static int MAX_VALUE = 28;
    protected final static int HUD_ICON_X = 3;
    protected final static int HUD_ICON_TOP_YOFF = 36;
    
    private final HudMeterImages images;
    private int displayValue = getValue();
    private boolean soundAlwaysRequired = true;
    
    protected HudMeter(final HudMeterImages images) {
        this.images = images;
    }
    
    protected final HudMeter setSoundAlwaysRequired(final boolean soundAlwaysRequired) {
        this.soundAlwaysRequired = soundAlwaysRequired;
        return this;
    }
    
    @Override
    protected final void renderView(final Panderer renderer) {
        final Panlayer layer = getLayer();
        if ((layer == null) || !isNeeded()) {
            return;
        }
        final Panple pos = getPosition();
        final float x = pos.getX(), y = pos.getY(), zBlank = pos.getZ(), zValued = zBlank + 2;
        final int actualValue = getValue();
        if (actualValue < displayValue) {
            displayValue--;
        } else if (actualValue > displayValue) {
            displayValue++;
            if (displayValue == MAX_VALUE) {
                onMaxDisplayReached();
            }
            if (soundAlwaysRequired || (displayValue < actualValue)) { // If incremented displayValue still less than actual, then it was raised by a power-up, not timer, so play it
                BotsnBoltsGame.fxHealth.startSound();
            }
        }
        final int value = displayValue, end = MAX_VALUE - 1;
        renderer.render(layer, BotsnBoltsGame.hudMeterBlank.middle, x, y, zBlank, 0, 0, 8, end * 2);
        if (value > 0) {
            renderer.render(layer, images.bottom, x, y, zValued);
        }
        final int valuedMiddleHeight = Math.min(value - 1, MAX_VALUE - 2) * 2;
        if (valuedMiddleHeight > 0) {
            renderer.render(layer, images.middle, x, y + 2, zValued, 0, 0, 8, valuedMiddleHeight);
        }
        renderer.render(layer, ((value <= end) ? BotsnBoltsGame.hudMeterBlank : images).top, x, y + (end * 2), zValued);
    }
    
    protected abstract int getValue();
    
    protected boolean isNeeded() {
        return true;
    }
    
    protected void onMaxDisplayReached() {
    }
    
    protected final static class HudMeterImages {
        private final Panmage bottom;
        private final Panmage middle;
        private final Panmage top;
        
        protected HudMeterImages(final Panmage[] images) {
            this(images[images.length - 1], images[1], images[0]);
        }
        
        protected HudMeterImages(final Panmage bottom, final Panmage middle, final Panmage top) {
            this.bottom = bottom;
            this.middle = middle;
            this.top = top;
        }
    }
    
    protected abstract static class HudIcon extends Panctor {
        protected final PlayerContext pc;
        
        protected HudIcon(final PlayerContext pc, final int yOff) {
            this.pc = pc;
            getPosition().set(pc.getHudX() + HUD_ICON_X, Pangine.getEngine().getEffectiveHeight() - yOff, BotsnBoltsGame.DEPTH_HUD);
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            Upgrade upgrade = getMode().getRequiredUpgrade();
            if (upgrade == null) {
                if (isBasicIconNeeded()) {
                    upgrade = getBasic();
                } else {
                    return;
                }
            }
            final Panlayer layer = getLayer();
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY(), z = BotsnBoltsGame.DEPTH_HUD, x1 = x + 1, y1 = y + 1;
            final Panmage img = upgrade.getBoxImage(pc);
            renderer.render(layer, img, x + 1, y + 1, z, 0, 0, 16, 16, 0, false, false);
            for (int i = 0; i < 18; i += 17) {
                renderer.render(layer, BotsnBoltsGame.black, x + i, y1, z, 0, 0, 1, 16, 0, false, false);
                renderer.render(layer, BotsnBoltsGame.black, x1, y + i, z, 0, 0, 16, 1, 0, false, false);
            }
        }
        
        protected abstract InputMode getMode();
        
        protected abstract boolean isBasicIconNeeded();
        
        protected abstract Upgrade getBasic();
    }
    
    protected final static class HudShootMode extends HudIcon {
        protected HudShootMode(final PlayerContext pc) {
            super(pc, HUD_ICON_TOP_YOFF);
        }
        
        @Override
        protected final InputMode getMode() {
            return pc.prf.shootMode;
        }
        
        @Override
        protected final boolean isBasicIconNeeded() {
            final Profile prf = pc.prf;
            return prf.isAttackUpgradeAvailable();
        }
        
        @Override
        protected final Upgrade getBasic() {
            return Profile.BASIC_ATTACK;
        }
    }
    
    protected final static class HudJumpMode extends HudIcon {
        protected HudJumpMode(final PlayerContext pc) {
            super(pc, 73);
        }
        
        @Override
        protected final InputMode getMode() {
            return pc.prf.jumpMode;
        }
        
        @Override
        protected final boolean isBasicIconNeeded() {
            final Profile prf = pc.prf;
            return prf.isJumpUpgradeAvailable();
        }
        
        @Override
        protected final Upgrade getBasic() {
            return Profile.BASIC_JUMP;
        }
    }
}
