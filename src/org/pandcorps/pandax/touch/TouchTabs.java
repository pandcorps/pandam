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
package org.pandcorps.pandax.touch;

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandax.text.*;

public class TouchTabs {
    private static boolean fullScreen = false;
    private final int x;
    private final int y;
    private final int z;
    private final int buttonWidth;
    private final int buttonHeight;
    private final int numButtonsDisplayed;
    private final TouchButton leftButton;
    private final TouchButton rightButton;
    private final TouchButton[] buttons;
    private int currentFirstButton = 0;
    
    public static TouchTabs createWithOverlays(final int z, final Panmage btn, final Panmage btnAct, final Panmage left, final Panmage right, final List<TouchButton> buttons) {
        final Panple btnSize = btn.getSize(), overlaySize = left.getSize();
        return new TouchTabs(z, btn, btnAct, left, btn, btnAct, right, off(btnSize.getX(), overlaySize.getX()), off(btnSize.getY(), overlaySize.getY()), toArray(buttons));
    }
    
    public final static int off(final float btn, final float overlay) {
        return Math.round((btn - overlay) / 2f);
    }
    
    public TouchTabs(final int z, final Panmage left, final Panmage leftAct, final Panmage right, final Panmage rightAct, final List<TouchButton> buttons) {
    	this(z, left, leftAct, right, rightAct, toArray(buttons));
    }
    
    private final static TouchButton[] toArray(final List<TouchButton> buttons) {
        return buttons.toArray(new TouchButton[buttons.size()]);
    }
    
    public TouchTabs(final int z, final Panmage left, final Panmage leftAct, final Panmage right, final Panmage rightAct, final TouchButton... buttons) {
        this(z, left, leftAct, null, right, rightAct, null, 0, 0, buttons);
    }
    
    private TouchTabs(final int z, final Panmage left, final Panmage leftAct, final Panmage leftOverlay, final Panmage right, final Panmage rightAct, final Panmage rightOverlay, final int xOverlay, final int yOverlay, final TouchButton... buttons) {
        final Pangine engine = Pangine.getEngine();
        final Panple buttonSize = left.getSize();
        final int screenHeight = engine.getEffectiveHeight();
        buttonHeight = (int) buttonSize.getY();
        this.z = z;
        buttonWidth = (int) buttonSize.getX();
        this.buttons = buttons;
        final int screenWidth = engine.getEffectiveWidth();
        final int btnsPerRow = screenWidth / buttonWidth, total = buttons.length;
        final int btnsPerCol = fullScreen ? (screenHeight / buttonHeight) : 1;
        final int max = btnsPerRow * btnsPerCol;
        final int totalDisplayedPerRow = Math.min(btnsPerRow, total);
        final int totalWidth = totalDisplayedPerRow * buttonWidth;
        final int bottom;
        x = (screenWidth - totalWidth) / 2;
        if (fullScreen) {
            final int totalDisplayedPerCol = Math.min(btnsPerCol, (total / btnsPerRow) + (((total % btnsPerRow) == 0) ? 0 : 1));
            final int totalHeight = totalDisplayedPerCol * buttonHeight;
            bottom = ((screenHeight - totalHeight) / 2);
            y = bottom + ((totalDisplayedPerCol - 1) * buttonHeight);
        } else {
            y = screenHeight - buttonHeight;
            bottom = y;
        }
        if (total > max) {
            numButtonsDisplayed = max - 2;
            final Panlayer layer = buttons[0].getLayer();
            final String id = Pantil.vmid();
            leftButton = newButton(layer, "left." + id, x, y, z, left, leftAct, leftOverlay, xOverlay, yOverlay, null, null, 0, 0, true, new Runnable() {
                @Override public final void run() {
                    left(); }});
            rightButton = newButton(layer, "right." + id, x + (max - 1) * buttonWidth, bottom, z, right, rightAct, rightOverlay, xOverlay, yOverlay, null, null, 0, 0, true, new Runnable() {
                @Override public final void run() {
                    right(); }});
        } else {
            numButtonsDisplayed = total;
            leftButton = null;
            rightButton = null;
        }
        initButtons();
    }
    
    public final static TouchButton newButton(final Panlayer layer, final String name, final Panmage img, final Panmage imgAct, final Runnable listener) {
        return newButton(layer, name, 0, 0, 0, img, imgAct, null, 0, 0, null, null, 0, 0, false, listener);
    }
    
    public final static TouchButton newButton(final Panlayer layer, final String name, final Panmage img, final Panmage imgAct,
                                              final Panmage imgOverlay, final int xOverlay, final int yOverlay,
                                              final MultiFont fonts, final CharSequence txt, final int xText, final int yText, final Runnable listener) {
        return newButton(layer, name, 0, 0, 0, img, imgAct, imgOverlay, xOverlay, yOverlay, fonts, txt, xText, yText, false, listener);
    }
    
    private final static TouchButton newButton(final Panlayer layer, final String name, final int x, final int y, final float z,
                                               final Panmage img, final Panmage imgAct, final Panmage imgOverlay, final int xOverlay, final int yOverlay,
                                               final MultiFont fonts, final CharSequence txt, final int xText, final int yText,
                                               final boolean active, final Runnable listener) {
        final Pangine engine = Pangine.getEngine();
        final TouchButton button = new TouchButton(engine.getInteraction(), layer, name, x, y, z, img, imgAct, imgOverlay, xOverlay, yOverlay, fonts, txt, xText, yText, true);
        final Panctor actor = button.getActor();
        if (active) {
            engine.registerTouchButton(button);
        } else {
            actor.detach();
        }
        actor.register(button, Actions.newEndListener(listener));
        return button;
    }
    
    private final void initButtons() {
        int x = this.x + ((leftButton == null) ? 0 : buttonWidth), y = this.y;
        final int size = buttons.length;
        final int screenWidth = Pangine.getEngine().getEffectiveWidth();
        for (int i = 0; i < size; i++) {
            final int buttonIndex = (currentFirstButton + i) % size;
            final TouchButton button = buttons[buttonIndex];
            if (i >= numButtonsDisplayed) {
                button.detach();
                continue;
            } else if ((x + buttonWidth) > screenWidth) {
                y -= buttonHeight;
                x = this.x;
            }
            button.reattach();
            button.setPosition(x, y);
            x += buttonWidth;
            final Panctor actor = button.getActor();
            if (actor != null) {
                actor.getPosition().setZ(z);
            }
        }
    }
    
    private final void left() {
        currentFirstButton--;
        if (currentFirstButton < 0) {
            currentFirstButton = buttons.length - 1;
        }
        initButtons();
    }
    
    private final void right() {
        currentFirstButton++;
        if (currentFirstButton >= buttons.length) {
            currentFirstButton = 0;
        }
        initButtons();
    }
    
    public final void destroy() {
        TouchButton.destroy(leftButton);
        TouchButton.destroy(rightButton);
        for (final TouchButton button : buttons) {
            TouchButton.destroy(button);
        }
    }
    
    public final static void setFullScreen(final boolean fullScreen) {
        TouchTabs.fullScreen = fullScreen;
    }
}
