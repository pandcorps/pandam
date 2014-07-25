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

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.Panteraction.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Input.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.touch.*;
import org.pandcorps.platform.Profile.*;
import org.pandcorps.platform.Avatar.*;
import org.pandcorps.platform.Player.*;

public class Menu {
    protected final static byte TOUCH_FULL = 0;
    protected final static byte TOUCH_HORIZONTAL = 1;
    protected final static byte TOUCH_JUMP = 2;
    private final static short SPEED_MENU_FADE = 9;
    private final static int SIZE_FONT = 8;
    private final static String NAME_NEW = "org.pandcorps.new";
    private final static String WARN_DELETE = "Press Erase again to confirm";
    private final static String WARN_EMPTY = "Must have a name";
    private final static String WARN_DUPLICATE = "Name already used";
    private final static String INFO_SAVED = "Saved images";
    private final static char CHAR_ON = 2;
    private final static String NEW_AVATAR_NAME = "New";
    private static boolean newProfile = false;
    
	protected abstract static class PlayerScreen extends Panscreen {
		protected Panroom room;
		protected PlayerContext pc;
		protected ControlScheme ctrl = null;
		private final boolean fadeIn;
		protected final StringBuilder inf = new StringBuilder();
		protected TileMap tm = null;
		protected Panmage timg = null;
		protected Model actor = null;
		protected Pantext infLbl = null;
		protected boolean disabled = false;
		protected Panform form = null;
		protected int center = -1;
		protected final List<TouchButton> tabs;
		protected boolean tabsSupported = false;
		protected int touchRadioX = 40;
		protected int touchRadioY = 140;
		protected int touchKeyboardX = 8;
		
		protected PlayerScreen(final PlayerContext pc, final boolean fadeIn) {
			this.pc = pc;
			this.fadeIn = fadeIn;
			tabs = isTabEnabled() ? new ArrayList<TouchButton>() : null;
		}
		
		@Override
		protected final void load() throws Exception {
			final int w = PlatformGame.SCREEN_W;
			center = w / 2;
			room = PlatformGame.createRoom(w, PlatformGame.SCREEN_H);
			final Pangine engine = Pangine.getEngine();
			engine.setBgColor(new FinPancolor((short) 128, (short) 192, Pancolor.MAX_VALUE));
			
			tm = new TileMap(Pantil.vmid(), room, ImtilX.DIM, ImtilX.DIM);
			Level.tm = tm;
			timg = Level.getTileImage();
			final TileMapImage[][] imgMap = tm.splitImageMap(timg);
			tm.fillBackground(imgMap[1][1], 0, 1);
			room.addActor(tm);
			
			if (pc != null) {
				actor = addActor(pc, center);
			    ctrl = pc.ctrl;
			}
			if (tabsSupported && isTabEnabled()) {
				engine.clearTouchButtons();
				engine.getInteraction().unregisterAll();
			} else {
				initTouchButtons(room, ctrl);
			}
			form = new Panform(ctrl);
			infLbl = addTitle(inf, center, getBottom());
			form.setTabListener(new FormTabListener() {@Override public void onTab(final FormTabEvent event) {
				if (allow(event.getFocused())) {
					clearInfo();
				} else {
					event.cancel();
				}
			}});
			menu();
			if (ctrl != null) { // Null on TitleScreen
				form.init();
			}
			
			if (fadeIn) {
			    PlatformGame.fadeIn(room, SPEED_MENU_FADE);
			}
		}
		
		protected final int getTop() {
			return (Pangine.getEngine().getEffectiveHeight() / 2) + 87;
		}
		
		protected final int getBottom() {
			return 56;
		}
		
		protected final int getLeft() {
			return (Pangine.getEngine().getEffectiveWidth() / 2) - 120;
		}
		
		protected final int getTouchKeyboardY() {
			return (int) (Pangine.getEngine().getEffectiveHeight() - PlatformGame.menu.getSize().getY() - 16);
		}
		
		protected final static void initTouchButtons(final Panlayer room, final ControlScheme ctrl) {
			initTouchButtons(room, ctrl, TOUCH_FULL, true, true, null);
		}
		
		protected final static void initTouchButtons(final Panlayer room, final ControlScheme ctrl,
				final byte mode, final boolean input, final boolean act, final Panctor bound) {
			//System.out.println("initTouch for " + getClass().getName());
			if (ctrl == null) {
				return;
			}
			//System.out.println("Found ControlScheme");
			final Panput temp = ctrl.get1();
			if (temp != null && !(temp.getDevice() instanceof Touchscreen)) {
        		return;
        	}
			//System.out.println("Found touch scheme");
			final Pangine engine = Pangine.getEngine();
			if (input) {
				engine.clearTouchButtons();
			}
			final int d = PlatformGame.DIM_BUTTON, r = engine.getEffectiveWidth();
			int rx = 0, y = 0;
			TouchButton down = null, up = null, act2 = null;
			Panmage rt = PlatformGame.right2, rtIn = PlatformGame.right2In, lt = PlatformGame.left2, ltIn = PlatformGame.left2In;
			final boolean full = mode == TOUCH_FULL;
			if (full) {
			    final int rad = (d / 2) + 1, dmtr = rad * 2;
				y = rad;
				down = addDiamondButton(room, "Down", rad, 0, input, act, ctrl.getDown());
				up = addDiamondButton(room, "Up", rad, dmtr, input, act, ctrl.getUp());
				rx = dmtr;
				//act2 = addCircleButton(room, "Act2", r - d, 0, input, act, ctrl.get2());
				//sub = addCircleButton(room, "Sub", r - d, engine.getEffectiveHeight() - d, input, act, ctrl.getSubmit());
				final Panple ts = PlatformGame.menu.getSize();
				act2 = newFormButton(room, "Act2", r - (int) ts.getX(), engine.getEffectiveHeight() - (int) ts.getY(), PlatformGame.menuMenu);
				rt = lt = PlatformGame.diamond;
				rtIn = ltIn = PlatformGame.diamondIn;
			} else if (mode == TOUCH_HORIZONTAL) {
				rx = (int) (d * 1.25f);
				//sub = null;
			}
			final TouchButton left, right;
			final Panput act1;
			if (mode != TOUCH_JUMP) {
    			left = addButton(room, "Left", 0, y, input, act, ctrl.getLeft(), lt, ltIn, full);
                right = addButton(room, "Right", rx, y, input, act, ctrl.getRight(), rt, rtIn, full);
                act1 = addCircleButton(room, "Act1", r - rx, 0, input, act, ctrl.get1(), full);
			} else {
			    left = null;
			    right = null;
			    act1 = engine.getInteraction().TOUCH;
			}
			if (full) {
			    up.setOverlapMode(TouchButton.OVERLAP_BEST);
			    down.setOverlapMode(TouchButton.OVERLAP_BEST);
			    left.setOverlapMode(TouchButton.OVERLAP_BEST);
			    right.setOverlapMode(TouchButton.OVERLAP_BEST);
			}
			if (input) {
				ctrl.set(down, up, left, right, act1, act2, act2);
			}
			Panctor actor = act1 instanceof TouchButton ? ((TouchButton) act1).getActor() : null;
			if (actor == null) {
			    actor = bound;
			}
			if (!full && actor != null) {
			    registerBackPromptQuit(actor);
			}
		}
		
		private static TouchButton quitYes = null;
        private static TouchButton quitNo = null;
		
		protected final static void registerBackPromptQuit(final Panctor bound) {
			destroyPromptQuit();
		    bound.register(Pangine.getEngine().getInteraction().BACK, new ActionEndListener() {
                @Override
                public final void onActionEnd(final ActionEndEvent event) {
                    if (quitYes == null) {
                        promptQuit(bound.getLayer());
                    } else {
                        destroyPromptQuit();
                    }
                }});
		}
		
		protected final static void promptQuit(final Panlayer room) {
			destroyPromptQuit();
		    final Pangine engine = Pangine.getEngine();
		    final Panple btnSize = PlatformGame.menu.getSize();
            final int btnY = TouchTabs.off(engine.getEffectiveHeight(), btnSize.getY());
            final int btnW = (int) btnSize.getX(), btnX = TouchTabs.off(engine.getEffectiveWidth(), btnW * 2);
            quitYes = newFormButton(room, "Quit", btnX, btnY, PlatformGame.menuCheck, "Quit", new Runnable() {
                @Override public final void run() { engine.exit(); }});
            quitYes.setZ(15);
            quitNo = newFormButton(room, "No", btnX + btnW, btnY, PlatformGame.menuX, "No", new Runnable() {
                @Override public final void run() { destroyPromptQuit(); }});
            quitNo.setZ(15);
            engine.setPaused(true);
		}
		
		protected final static void destroyPromptQuit() {
			Pangine.getEngine().setPaused(false);
		    TouchButton.destroy(quitYes);
            quitYes = null;
            TouchButton.destroy(quitNo);
            quitNo = null;
		}
		
		private final static TouchButton addCircleButton(final Panlayer room, final String name, final int x, final int y,
				final boolean input, final boolean act, final Panput old, final boolean moveCancel) {
			return addButton(room, name, x, y, input, act, old, PlatformGame.button, PlatformGame.buttonIn, moveCancel);
		}
		
		private final static TouchButton addDiamondButton(final Panlayer room, final String name, final int x, final int y,
                final boolean input, final boolean act, final Panput old) {
            return addButton(room, name, x, y, input, act, old, PlatformGame.diamond, PlatformGame.diamondIn, true);
        }
		
		private final static TouchButton addButton(final Panlayer room, final String name, final int x, final int y,
				final boolean input, final boolean act, final Panput old, final Panmage img, final Panmage imgIn,
				final boolean moveCancel) {
			final TouchButton button;
			if (input) {
				final Pangine engine = Pangine.getEngine();
				final Panteraction in = engine.getInteraction();
				final int d = PlatformGame.DIM_BUTTON;
				button = new TouchButton(in, name, x, y, d, d, moveCancel);
				engine.registerTouchButton(button);
			} else {
				button = (TouchButton) old;
			}
			if (act) {
				final Panctor actor = new Panctor();
				actor.setView(img);
				button.setActor(actor, imgIn);
				actor.getPosition().set(x, y, 500);
				room.addActor(actor);
			}
			return button;
		}
		
		protected final static int offx(final Panmage img) {
			return img == null ? 0 : TouchTabs.off(PlatformGame.menu.getSize().getX(), img.getSize().getX());
		}
		
		private final static int offy(final Panmage img) {
			return img == null ? 0 : TouchTabs.off(PlatformGame.menu.getSize().getY(), img.getSize().getY());
		}
		
		protected final static int OFF_OVERLAY_Y = 10;
		protected final static int OFF_TEXT_X = 4;
		protected final static int OFF_TEXT_Y = 2;
		
		protected final TouchButton newTab(final Panmage img, final CharSequence txt, final Runnable listener) {
			final TouchButton tab = TouchTabs.newButton(getLayer(), Pantil.vmid(), PlatformGame.menu, PlatformGame.menuIn, img, offx(img), OFF_OVERLAY_Y, PlatformGame.font, txt, OFF_TEXT_X, OFF_TEXT_Y,
					new Runnable() { @Override public void run() {
						if (disabled) {
							return;
						}
						listener.run(); }});
			tab.setImageDisabled(PlatformGame.menuDisabled);
			tabs.add(tab);
			return tab;
		}
		
		protected final void newTabs() {
			TouchTabs.createWithOverlays(0, PlatformGame.menu, PlatformGame.menuIn, PlatformGame.menuLeft, PlatformGame.menuRight, tabs);
		}
		
		protected final boolean isTabEnabled() {
			return Pangine.getEngine().isTouchSupported();
			//return false;
		}
		
		protected final Panlayer getLayer() {
			Panlayer layer = form.getLayer();
			if (layer == null) {
				layer = Pangame.getGame().getCurrentRoom();
				if (layer == null) {
					throw new IllegalStateException("Cannot find current layer");
				}
			}
			return layer;
		}
		
		protected final RadioGroup addRadio(final String title, final List<String> list, final RadioSubmitListener lsn, final int x, final int y) {
			return addRadio(title, list, null, lsn, x, y);
		}
		
		protected final RadioGroup addRadio(final String title, final List<? extends CharSequence> list, final RadioSubmitListener subLsn, final RadioSubmitListener chgLsn, final int x, final int y) {
			return addRadio(title, list, subLsn, chgLsn, x, y, null);
		}
		
		protected final static int OFF_RADIO_LIST = 100;
		
		protected final RadioGroup addRadio(final String title, final List<? extends CharSequence> list, final RadioSubmitListener subLsn, final RadioSubmitListener chgLsn, final int xb, final int y, final TouchButton sub) {
			final int x;
			if (tabsSupported && isTabEnabled()) {
				final int yt = y - 100;
				final String id = Pantil.vmid();
				ctrl.setUp(newFormButton(id + ".radio.up", xb, yt + 100, PlatformGame.menuUp));
				ctrl.setDown(newFormButton(id + ".radio.down", xb, yt, PlatformGame.menuDown));
				if (subLsn != null) {
					//final TouchButton sub = newFormButton(id + ".radio.submit", x + 200, yt, PlatformGame.menuCheck);
					//final TouchButton sub = null; // Will use tab bar to simulate submit button below
					ctrl.setSubmit(sub);
					ctrl.set1(sub);
				}
				x = xb + OFF_RADIO_LIST;
			} else {
				x = xb;
			}
			final RadioGroup grp = new RadioGroup(PlatformGame.font, list, subLsn);
			if (sub == null && subLsn != null && tabsSupported && isTabEnabled()) {
				newTab(PlatformGame.menuCheck, "Done", new Runnable() {@Override public final void run() {grp.submit();}});
			}
			grp.setChangeListener(chgLsn);
			addItem(grp, x, y - 16);
			grp.addChild(addTitle(title, x, y));
			final Pantext label = grp.getLabel();
			label.setLinesPerPage(5);
			label.stretchCharactersPerLineToFit();
			return grp;
		}
		
		protected final TouchButton newRadioSubmitButton(final int x, final int y) {
			final int xr = Pangine.getEngine().getEffectiveWidth() - x - (int) PlatformGame.menu.getSize().getX();
			return newFormButton(Pantil.vmid() + ".radio.submit", xr, y - 100, PlatformGame.menuCheck);
		}
		
		protected final static TouchButton newFormButton(final Panlayer layer, final String name, final int x, final int y, final Panmage img) {
			return newFormButton(layer, name, x, y, img, (String) null);
		}
		
		protected final static TouchButton newFormButton(final Panlayer layer, final String name, final int x, final int y, final Panmage img, final String txt) {
			final Pangine engine = Pangine.getEngine();
			final TouchButton btn = new TouchButton(engine.getInteraction(), layer, name, x, y, 0, PlatformGame.menu, PlatformGame.menuIn, img, offx(img), txt == null ? offy(img) : OFF_OVERLAY_Y, PlatformGame.font, txt, OFF_TEXT_X, OFF_TEXT_Y, true);
			engine.registerTouchButton(btn);
			return btn;
		}
		
		protected final static TouchButton newFormButton(final Panlayer layer, final String name, final int x, final int y, final Panmage img, final Runnable r) {
			return newFormButton(layer, name, x, y, img, null, r);
		}
		
		protected final static TouchButton newFormButton(final Panlayer layer, final String name, final int x, final int y, final Panmage img, final String txt, final Runnable r) {
			final TouchButton btn = newFormButton(layer, name, x, y, img, txt);
			btn.getActor().register(btn, Actions.newEndListener(r));
			return btn;
		}
		
		protected final TouchButton newFormButton(final String name, final int x, final int y, final Panmage img) {
		    return newFormButton(getLayer(), name, x, y, img);
		}
		
		protected final TouchButton newFormButton(final String name, final int x, final int y, final Panmage img, final Runnable r) {
		    return newFormButton(getLayer(), name, x, y, img, r);
		}
		
		protected final List<RadioGroup> addColor(final SimpleColor col, int x, int y) {
			if (tabsSupported && isTabEnabled()) {
				addColorTouch(col);
				return null;
			} else {
				return addColorClassic(col, x, y);
			}
		}
		
		protected final void addColorTouch(final SimpleColor col) {
			final String id = Pantil.vmid();
			final Pangine engine = Pangine.getEngine();
			final Panple btnSize = PlatformGame.menu.getSize();
			final int btnW = (int) btnSize.getX(), gapW = (btnW * 5) / 6, difW = btnW + gapW;
			final int minX = (engine.getEffectiveWidth() - (btnW * 3 + gapW * 2)) / 2;
			final int btnH = (int) btnSize.getY(), difH = btnH + 16;
			final int minY = (engine.getEffectiveHeight() - (btnH + difH)) / 2;
			int x = minX, y = minY + difH;
			final int txtX = btnW / 2, txtY = y - 12;
			final StringBuilder sbR = new StringBuilder(), sbG = new StringBuilder(), sbB = new StringBuilder();
			final Pantext txtR = initCol(col.r, sbR, x + txtX, txtY);
			newFormButton(id + ".red.up", x, y, PlatformGame.redUp, new AvtRunnable() {@Override public final void go() {
				col.r = incCol(col.r, sbR, txtR); }});
			x += difW;
			final Pantext txtG = initCol(col.g, sbG, x + txtX, txtY);
			newFormButton(id + ".green.up", x, y, PlatformGame.greenUp, new AvtRunnable() {@Override public final void go() {
				col.g = incCol(col.g, sbG, txtG); }});
			x += difW;
			final Pantext txtB = initCol(col.b, sbB, x + txtX, txtY);
			newFormButton(id + ".blue.up", x, y, PlatformGame.menuUp, new AvtRunnable() {@Override public final void go() {
				col.b = incCol(col.b, sbB, txtB); }});
			x = minX;
			y = minY;
			newFormButton(id + ".red.down", x, y, PlatformGame.redDown, new AvtRunnable() {@Override public final void go() {
				col.r = decCol(col.r, sbR, txtR); }});
			x += difW;
			newFormButton(id + ".green.down", x, y, PlatformGame.greenDown, new AvtRunnable() {@Override public final void go() {
				col.g = decCol(col.g, sbG, txtG); }});
			x += difW;
			newFormButton(id + ".blue.down", x, y, PlatformGame.menuDown, new AvtRunnable() {@Override public final void go() {
				col.b = decCol(col.b, sbB, txtB); }});
		}
		
		private final float incCol(float c, final StringBuilder sb, final Pantext text) {
			c = (c >= 1) ? 0 : (c + 0.25f);
			setCol(c, sb, text);
			return c;
		}
		
		private final float decCol(float c, final StringBuilder sb, final Pantext text) {
			c = (c <= 0) ? 1 : (c - 0.25f);
			setCol(c, sb, text);
			return c;
		}
		
		private final void setCol(final float c, final StringBuilder sb) {
			Chartil.set(sb, String.valueOf((int) (c * 100)) + '%');
		}
		
		private final void setCol(final float c, final StringBuilder sb, final Pantext text) {
			text.uncenterX();
			setCol(c, sb);
			text.centerX();
		}
		
		private final Pantext initCol(final float c, final StringBuilder sb, final int x, final int y) {
			setCol(c, sb);
			final Pantext text = addTitle(sb, x, y);
			text.centerX();
			return text;
		}
		
		protected final List<RadioGroup> addColorClassic(final SimpleColor col, int x, int y) {
			final List<String> colors = Arrays.asList("0", "1", "2", "3", "4");
			final AvtListener redLsn = new ColorListener() {
				@Override public final void update(final float value) {
					col.r = value; }};
			x += 32;
			final RadioGroup redGrp = addRadio("Red", colors, redLsn, x, y);
			final AvtListener greenLsn = new ColorListener() {
				@Override public final void update(final float value) {
					col.g = value; }};
			x += 32;
			final RadioGroup grnGrp = addRadio("Grn", colors, greenLsn, x, y);
			final AvtListener blueLsn = new ColorListener() {
				@Override public final void update(final float value) {
					col.b = value; }};
			x += 32;
			final RadioGroup bluGrp = addRadio("Blu", colors, blueLsn, x, y);
			redGrp.setSelected(getLineColor(col.r));
			grnGrp.setSelected(getLineColor(col.g));
			bluGrp.setSelected(getLineColor(col.b));
			final List<RadioGroup> list = new ArrayList<RadioGroup>(3);
			list.add(redGrp);
			list.add(grnGrp);
			list.add(bluGrp);
			return list;
		}
		
		private abstract class ColorListener extends AvtListener {
			@Override
			protected final void update(final String value) {
				update(Avatar.toColor(Integer.parseInt(value)));
			}
			
			protected abstract void update(final float value);
		}
		
		protected final void addHudGems() {
		    final int gemX = center + 16, gemY = 20;
            PlatformGame.addHudGem(room, gemX, gemY);
            PlatformGame.addHud(room, pc, gemX + PlatformGame.OFF_GEM, gemY, false, false);
		}
		
		protected final Input addNameInput(final PlayerData pd, final InputSubmitListener subLsn, final int max, final int x, final int y) {
		    final InputSubmitListener chgLsn = new InputSubmitListener() {
	            @Override public final void onSubmit(final InputSubmitEvent event) {
	                pd.setName(event.toString()); }};
	        final Input in;
	        if (isTabEnabled()) {
	        	in = new KeyInput(PlatformGame.font, subLsn);
	        	new TouchKeyboard(PlatformGame.key, PlatformGame.keyIn, PlatformGame.font, y - (int) PlatformGame.key.getSize().getY() - 16);
	        	in.setProperName(true); // Might make sense for ControllerInput, but probably doesn't work right yet
	        } else {
		        final ControllerInput cin = new ControllerInput(PlatformGame.font, subLsn);
		        cin.setLetter();
		        in = cin;
	        }
	        in.setChangeListener(chgLsn);
	        in.setMax(max);
	        addItem(in, x + 40, y);
	        addTitle("Name", x, y);
	        return in;
		}
		
		protected final int addLink(final CharSequence txt, final MessageCloseListener lsn, final int x, final int y) {
		    final Message msg = new Message(PlatformGame.font, txt, lsn);
		    msg.getLabel().setUnderlineEnabled(true);
	        addItem(msg, x, y);
	        return x + (SIZE_FONT * (txt.length() + 1));
		}
		
		protected final void addItem(final TextItem item, final int x, final int y) {
			final Pantext lbl = item.getLabel();
			lbl.getPosition().set(x, y);
			lbl.setBackground(Pantext.CHAR_SPACE);
			lbl.setBorderStyle(BorderStyle.Simple);
			form.addItem(item);
		}
		
		protected final Pantext addTitleCentered(final CharSequence title, final int y) {
			final Pantext text = addTitle(title, center, y);
			text.centerX();
			return text;
		}
		
		protected final Pantext addTitle(final CharSequence title, final int x, final int y) {
			return addTitle(new Pantext(Pantil.vmid(), PlatformGame.font, title), x, y);
		}
		
		protected final Pantext addTitleTiny(final CharSequence title, final int x, final int y) {
			return addTitle(new Pantext(Pantil.vmid(), PlatformGame.fontTiny, title), x, y);
		}
		
		protected final Pantext addTitle(final Pantext tLbl, final int x, final int y) {
			tLbl.getPosition().set(x, y);
			form.getLayer().addActor(tLbl);
			return tLbl;
		}
		
		protected final int addPipe(final int x, final int y) {
			addTitle("|", x, y);
			return x + (SIZE_FONT * 2);
		}
		
		protected final Model addActor(final PlayerContext pc, final int x) {
			final Model actor = new Model(pc);
			PlatformGame.setPosition(actor, x, 16, PlatformGame.DEPTH_PLAYER);
			room.addActor(actor);
			return actor;
		}
		
		protected final Panctor addActor(final int x, final int y) {
			final Panctor actor = new Panctor();
			addActor(actor, x, y);
			return actor;
		}
		
		protected final void addActor(final Panctor actor, final int x, final int y) {
			actor.getPosition().set(x, y);
			room.addActor(actor);
		}
		
		@Override
	    protected void destroy() {
			Level.tm = null;
			timg.destroy();
		}
		
		protected abstract void menu() throws Exception;
		
		protected boolean allow(final TextItem focused) {
			return true;
		}
		
		protected final void exit() {
			if (disabled) {
                return;
            }
		    disabled = true;
		    onExit();
		}
		
		protected final void registerBackExit() {
		    tm.register(Pangine.getEngine().getInteraction().BACK, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    exit(); }});
		}
		
		protected final void registerBackNop() {
            tm.register(Pangine.getEngine().getInteraction().BACK, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) { }});
        }
		
		protected final void goMap() {
			PlatformGame.goMap(SPEED_MENU_FADE);
		}
		
		protected final void save() {
		    pc.profile.save();
		}
		
		protected final boolean isPlayer1() {
		    return pc.index == 0;
		}
		
		protected final void triggerMapLoad() {
		    if (isPlayer1()) {
		        Map.triggerLoad();
		    }
		}
		
		protected abstract void onExit();
		
		protected final void setInfo(final String val) {
			Chartil.set(inf, val);
        	infLbl.getPosition().setX(center);
        	infLbl.centerX();
		}
		
		protected final void clearInfo() {
		    inf.setLength(0);
		}
		
		protected final int addExit(final String title, final int x, final int y) {
			final MsgCloseListener savLsn = new MsgCloseListener() {
				@Override public final void onClose() {
					exit(); }};
			return addLink(title, savLsn, x, y);
		}
		
		protected final void goProfile() {
			Panscreen.set(new ProfileScreen(pc, false));
		}
		
		protected final void reloadAnimalStrip() {
			PlatformGame.reloadAnimalStrip(pc, false);
		}
		
		protected abstract class AvtListener implements RadioSubmitListener {
			@Override public final void onSubmit(final RadioSubmitEvent event) {
			    if (disabled) {
			        return;
			    }
				update(event.toString());
				reloadAnimalStrip();
				actor.load(pc);
			}
			
			protected abstract void update(final String value);
		}
		
		protected abstract class AvtRunnable implements Runnable {
			@Override public final void run() {
			    if (disabled) {
			        return;
			    }
				go();
				reloadAnimalStrip();
				actor.load(pc);
			}
			
			protected abstract void go();
		}
		
		protected abstract class MsgCloseListener implements MessageCloseListener {
			@Override
			public final void onClose(final MessageCloseEvent event) {
                if (disabled) {
                    return;
                }
                onClose();
			}
			
			protected abstract void onClose();
		}
	}
	
	protected final static class TitleScreen extends PlayerScreen {
		private final static int NUM_CHRS = 4;
		private static ArrayList<PlayerContext> tcs = new ArrayList<PlayerContext>(NUM_CHRS);
		
	    protected TitleScreen() {
            super(null, true);
        }
	    
	    @Override
        protected final void menu() {
	        PlatformGame.loaders = null;
	        final int bottom = getBottom();
	        final Pantext text = addTitleCentered("Press anything", bottom);
	        final Pangine engine = Pangine.getEngine();
	        addTitleCentered("Andrew Martin's Untitled Game" + Pantext.CHAR_TRADEMARK, engine.getEffectiveHeight() / 2);
	        addTitleCentered("Copyright " + Pantext.CHAR_COPYRIGHT + " 2014 Andrew M. Martin", bottom + 16);
	        if (engine.isTouchSupported()) {
	        	text.register(new ActionEndListener() {@Override public void onActionEnd(final ActionEndEvent event) {
		        	onAnything(event);
		        }});
	        } else {
		        text.register(new ActionStartListener() {@Override public void onActionStart(final ActionStartEvent event) {
		        	onAnything(event);
		        }});
	        }
	        for (int i = 0; i < NUM_CHRS; i++) {
	        	final PlayerContext tc = tcs.get(i);
		        final Panctor actor = addActor(tc, PlatformGame.SCREEN_W * (i + 1) / (NUM_CHRS + 1));
	        	if (i >= NUM_CHRS / 2) {
	        		actor.setMirror(true);
	        	}
	        }
	    }
	    
	    private final void onAnything(final InputEvent event) {
	    	if (disabled) {
        		return;
        	}
        	final Device device = event.getInput().getDevice();
        	if (device instanceof Touchscreen) {
        		final Touch touch = Pangine.getEngine().getInteraction().TOUCH;
        		ctrl = new ControlScheme(null, null, null, null, touch, touch, touch);
        	} else {
        		ctrl = ControlScheme.getDefault(device);
        	}
            exit();
	    }
	    
	    protected final static void generateTitleCharacters() {
	        for (int i = 0; i < NUM_CHRS; i++) {
	        	final Profile prf = new Profile();
	        	final Avatar avt = new Avatar();
	        	avt.randomize();
	        	prf.currentAvatar = avt;
	        	prf.avatars.add(avt);
	        	final PlayerContext tc = new PlayerContext(prf, null, Integer.MAX_VALUE - i);
	        	tcs.add(tc);
	        	//TODO Menu screens which show player can probably use full=false, but will need full load when done
	        	PlatformGame.reloadAnimalStrip(tc, false);
	        }
	    }
	    
	    @Override
        protected final void onExit() {
			if (Config.defaultProfileName == null) {
				final SelectScreen screen = new SelectScreen(null, false);
		        screen.ctrl = ctrl;
		        Panscreen.set(screen);
			} else {
				try {
					PlatformGame.loadProfile(Config.defaultProfileName, ctrl, PlatformGame.pcs.size());
				} catch (final Exception e) {
					throw Pantil.toRuntimeException(e); //TODO handle missing profile
				}
				PlatformGame.goMap();
			}
	    }
	    
	    @Override
	    protected final void destroy() {
	    	super.destroy();
	    	for (final PlayerContext tc : tcs) {
				tc.destroy();
			}
	    	tcs.clear();
	    	tcs = null;
	    }
	}
	
	protected final static class SelectScreen extends PlayerScreen {
		private final PlayerContext curr;
		
		protected SelectScreen() {
			this(null, true);
		}
		
		protected SelectScreen(final PlayerContext pc, final boolean fadeIn) {
			super(null, fadeIn);
			if (pc != null) {
				ctrl = pc.ctrl;
			}
			curr = pc;
			tabsSupported = true;
		}
		
		@Override
		protected final void menu() {
			if (isTabEnabled()) {
				menuTouch();
			} else {
				menuClassic();
			}
		}
		
		protected final void menuTouch() {
			if (!createProfileList(touchRadioX, touchRadioY)) {
				return;
			}
			newTab(PlatformGame.menuPlus, "New", new Runnable() {@Override public final void run() {newProfile();}});
			if (curr != null) {
				newTab(PlatformGame.menuX, "Back", new Runnable() {@Override public final void run() {exit();}});
			}
			newTabs();
		}
		
		protected final void menuClassic() {
			final int left = getLeft();
			int x = left, y = getTop();
			if (!createProfileList(x, y)) {
				return;
			}
			final MsgCloseListener newLsn = new MsgCloseListener() {
                @Override public final void onClose() {
                    newProfile(); }};
            y -= 64;
			x = addLink("New", newLsn, left, y);
			if (curr != null) { //TODO also allow if this isn't player 1, but might need extra handling to remove newly added player
				x = addPipe(x, y);
				addExit("Cancel", x, y);
			}
		}

		@Override
		protected final void onExit() {
			if (pc == null) {
				pc = curr;
			}
			goProfile();
		}
		
		private final boolean createProfileList(final int x, final int y) {
			final List<String> list = PlatformGame.getAvailableProfiles();
			if (Coltil.isValued(list)) {
				final RadioSubmitListener prfLsn = new RadioSubmitListener() {
					@Override public final void onSubmit(final RadioSubmitEvent event) {
						if (curr != null) {
							curr.destroy();
						}
						final int index = curr == null ? PlatformGame.pcs.size() : curr.index;
						try {
							PlatformGame.loadProfile(event.toString(), ctrl, index);
						} catch (final Exception e) {
							throw Pantil.toRuntimeException(e);
						}
						pc = PlatformGame.pcs.get(index);
						triggerMapLoad();
						goProfile();
				}};
				addRadio("Pick Profile", list, prfLsn, null, x, y);
				return true;
			} else {
				newProfile();
				return false;
			}
		}
		
		private final void newProfile() {
            if (curr != null) {
                curr.destroy();
            }
            final Profile prf = new Profile();
            final Avatar avt = new Avatar();
            prf.setName("New");
            avt.randomize();
            avt.setName(NEW_AVATAR_NAME);
            prf.currentAvatar = avt;
            prf.avatars.add(avt);
            //prf.ctrl = 0;
            pc = PlatformGame.newPlayerContext(prf, ctrl, curr == null ? PlatformGame.pcs.size() : curr.index);
            reloadAnimalStrip();
            triggerMapLoad();
            Panscreen.set(new NewScreen(pc, false));
		}
	}
	
	protected final static class NewScreen extends PlayerScreen {
	    private final PlayerContext curr;
	    
        protected NewScreen(final PlayerContext pc, final boolean fadeIn) {
            super(null, fadeIn);
            if (pc != null) {
				ctrl = pc.ctrl;
			}
            curr = pc;
            tabsSupported = true;
        }

        @Override
		protected final void menu() {
			if (isTabEnabled()) {
				menuTouch();
			} else {
				menuClassic();
			}
		}
		
		protected final void menuTouch() {
			createNameInput(touchKeyboardX, getTouchKeyboardY());
			newTab(PlatformGame.menuCheck, "Done", new Runnable() {@Override public final void run() {exit();}});
			newTabs();
		}
		
		protected final void menuClassic() {
	        /*final Profile prf = new Profile();
	        final Avatar avt = new Avatar();
	        avt.randomize();
	        avt.setName("New");*/
            int x = getLeft(), y = getTop() - 72;
            createNameInput(x, y);
		}
		
		private final void createNameInput(final int x, final int y) {
			final InputSubmitListener namLsn = new InputSubmitListener() {
                @Override public final void onSubmit(final InputSubmitEvent event) {
                    exit(); }};
	        addNameInput(curr.profile, namLsn, PlatformGame.MAX_NAME_PROFILE, x, y); //TODO validation unique, submit link
	    }

        @Override
        protected final void onExit() {
            if (pc == null) {
                pc = curr;
            }
            if (Coltil.size(pc.profile.avatars) == 1) {
            	final Avatar avt = pc.profile.avatars.get(0);
            	if (NEW_AVATAR_NAME.equals(avt.getName())) {
            		avt.setName(pc.profile.getName());
            	}
            }
            if (Pangine.getEngine().isTouchSupported() && Config.defaultProfileName == null) {
            	Config.defaultProfileName = pc.profile.getName();
            	Config.serialize();
            }
            save();
            newProfile = true;
            goProfile();
        }
	}
	
	protected final static class ProfileScreen extends PlayerScreen {
	    private boolean save = false;
	    private final Avatar originalAvatar;
	    
		protected ProfileScreen(final PlayerContext pc, final boolean fadeIn) {
			super(pc, fadeIn);
			originalAvatar = pc.profile.currentAvatar;
			tabsSupported = true;
		}
		
		@Override
		protected final void menu() {
			if (isTabEnabled()) {
				menuTouch();
			} else {
				menuClassic();
			}
		}
		
		protected final void menuTouch() {
			createAvatarList(touchRadioX, touchRadioY);
			newTab(PlatformGame.menuCheck, "Done", new Runnable() {@Override public final void run() {exit();}});
			newTab(PlatformGame.menuAvatar, "Edit", new Runnable() {@Override public final void run() {goAvatar();}});
			if (!newProfile) {
				newTab(PlatformGame.menuPlus, "New", new Runnable() {@Override public final void run() {newAvatar();}});
				if (getAvatarsSize() > 1) {
					newTab(PlatformGame.menuMinus, "Erase", new Runnable() {@Override public final void run() {delete();}});
				}
				newTab(PlatformGame.menuTrophy, "Info", new Runnable() {@Override public final void run() {goInfo();}});
				if (isPlayer1()) {
				    newTab(PlatformGame.menuMenu, "Menu", new Runnable() {@Override public final void run() {goOptions();}});
					newTab(PlatformGame.menuOff, "Quit", new Runnable() {@Override public final void run() {quit();}});
				}
			}
			newProfile = false;
			//TODO The other stuff from menuClassic, move newTabs() into super class
			newTabs();
		}
		
		private final int getAvatarsSize() {
			return pc.profile.avatars.size();
		}
		
		private final void createAvatarList(final int x, final int y) {
			final int size = getAvatarsSize();
			if (size <= 1) {
				return;
			}
			final List<String> avatars = new ArrayList<String>(size);
            for (final Avatar a : pc.profile.avatars) {
            	avatars.add(a.getName());
            }
			final AvtListener avtLsn = new AvtListener() {
				@Override public final void update(final String value) {
					pc.profile.currentAvatar = pc.profile.getAvatar(value); }};
			final RadioGroup avtGrp = addRadio("Pick Avatar", avatars, avtLsn, x, y);
			avtGrp.setSelected(avatars.indexOf(pc.profile.currentAvatar.getName()));
		}
		
		protected final void menuClassic() {
			final int left = getLeft();
			int x = left, y = getTop();
			createAvatarList(x, y);
			final MsgCloseListener edtLsn = new MsgCloseListener() {
                @Override public final void onClose() {
                    goAvatar(); }};
            y -= 64;
            x = addLink("Edit", edtLsn, left, y);
            final MsgCloseListener newLsn = new MsgCloseListener() {
                @Override public final void onClose() {
                    newAvatar(); }};
            x = addPipe(x, y);
            x = addLink("New", newLsn, x, y);
            if (getAvatarsSize() > 1) {
	            final MsgCloseListener delLsn = new MsgCloseListener() {
	                @Override public final void onClose() {
	                    delete(); }};
	            x = addPipe(x, y);
	            x = addLink("Erase", delLsn, x, y);
            }
            final MsgCloseListener astLsn = new MsgCloseListener() {
                @Override public final void onClose() {
                    Panscreen.set(new AssistScreen(pc)); }};
            x = addPipe(x, y);
            x = addLink("Perks", astLsn, x, y);
			final MsgCloseListener prfLsn = new MsgCloseListener() {
                @Override public final void onClose() {
                    save();
                    Panscreen.set(new SelectScreen(pc, false)); }};
            y -= 16;
            x = left;
            addTitle("Profile", x, y);
            y -= 16;
            x = left + 8;
            x = addLink("Pick", prfLsn, x, y);
            final MsgCloseListener infLsn = new MsgCloseListener() {
                @Override public final void onClose() {
                    goInfo(); }};
            x = addPipe(x, y);
            x = addLink("Info", infLsn, x, y);
            if (isPlayer1()) {
            	x = addPipe(x, y);
                addTitle("Default", x, y);
            	final StringBuilder defStr = new StringBuilder();
            	defStr.append(getDefaultProfileText());
                final MsgCloseListener defLsn = new MsgCloseListener() {
                    @Override public final void onClose() {
                        if (isDefaultProfile()) {
                        	Config.defaultProfileName = null;
                        } else {
                        	Config.defaultProfileName = pc.profile.getName();
                        }
                        Config.serialize();
                        Chartil.set(defStr, getDefaultProfileText()); }};
                x += 64;
                addLink(defStr, defLsn, x, y);
            }
            y -= 16;
            x = left;
            addTitle("Game", x, y);
            y -= 16;
            x = left + 8;
            x = addExit(Map.started ? "Back" : "Play", x, y);
            if (isPlayer1()) {
                final MsgCloseListener qutLsn = new MsgCloseListener() {
                    @Override public final void onClose() {
                        quit(); }};
                x = addPipe(x, y);
                addLink("Quit", qutLsn, x, y);
            }
			// Rename Profile //TODO
			// Drop out (if other players? if not player 1?)
            // Exit to title (if player 1)
            // Delete Profile (if player 1)
		}
		
		private final void delete() {
			if (getAvatarsSize() <= 1) {
            	return;
            } else if (!inf.toString().equals(WARN_DELETE)) {
            	setInfo(WARN_DELETE);
            	return;
            }
            clearInfo();
            pc.profile.avatars.remove(pc.profile.currentAvatar);
            pc.profile.currentAvatar = pc.profile.avatars.get(0);
            reloadAnimalStrip();
            actor.load(pc);
            save = true;
            goProfile();
		}
		
		private final boolean isDefaultProfile() {
			return pc.profile.getName().equals(Config.defaultProfileName);
		}
		
		private final String getDefaultProfileText() {
			return isDefaultProfile() ? "Y" : "N";
		}
		
		private final void goAvatar() {
		    Panscreen.set(new AvatarScreen(pc));
		}
		
		private final void newAvatar() {
			final Avatar avt = new Avatar();
            avt.randomize();
            avt.setName(NAME_NEW);
            pc.profile.avatars.add(avt);
            pc.profile.currentAvatar = avt;
            reloadAnimalStrip();
            actor.load(pc);
            AvatarScreen.currentTab = AvatarScreen.TAB_NAME;
            goAvatar();
		}
		
		private final void goInfo() {
			Panscreen.set(new InfoScreen(pc, true));
		}
		
		private final void goOptions() {
            Panscreen.set(new OptionsScreen(pc));
        }
		
		private final void quit() {
			save();
            Pangine.getEngine().exit(); // Exit to TitleScreen instead? Quit game from there? Or separate Reset link?
		}
		
		@Override
		protected void onExit() {
		    if (save || pc.profile.currentAvatar != originalAvatar) {
		        save();
		    }
		    goMap();
		}
	}
	
	private final static String getNewName(final Profile profile) {
	    for (char c = 'A' - 1; c <= 'Z'; c++) {
	        final String name = NEW_AVATAR_NAME + (c >= 'A' ? String.valueOf(c) : "");
	        if (isNameFree(profile, name)) {
	            return name;
	        }
	    }
	    return "RenameUs";
	}
	
	private final static boolean isNameFree(final Profile profile, final String name) {
        for (final Avatar avt : profile.avatars) {
            if (name.equals(avt.getName())) {
                return false;
            }
        }
        return true;
	}
	
	protected final static class AvatarScreen extends PlayerScreen {
		private final static byte TAB_ANIMAL = 0;
		private final static byte TAB_EYES = 1;
		private final static byte TAB_COLOR = 2;
		private final static byte TAB_NAME = 3;
		private static byte currentTab = TAB_ANIMAL;
	    private boolean save = true;
		private Avatar old = null;
		private Avatar avt = null;
		//private boolean newAvt = false;
		
		protected AvatarScreen(final PlayerContext pc) {
			super(pc, false);
			tabsSupported = true;
		}
		
		protected AvatarScreen(final PlayerContext pc, final Avatar old, final Avatar avt) {
		    this(pc);
		    this.old = old;
		    this.avt = avt;
		}
		
		private final void initAvatar() {
		    if (old != null) {
		        return;
		    }
			old = pc.profile.currentAvatar;
			avt = new Avatar(old);
			pc.profile.replaceAvatar(avt);
		}
		
		@Override
		protected final void menu() {
			initAvatar();
			if (NAME_NEW.equals(old.getName())) {
			    //newAvt = true;
			    avt.setName(getNewName(pc.profile)); // If old keeps NAME_NEW, then cancel can rely on that
			}
			if (isTabEnabled()) {
				menuTouch();
			} else {
				menuClassic();
			}
		}
		
		protected final void menuTouch() {
			switch (currentTab) {
				case TAB_ANIMAL :
					createAnimalList(touchRadioX, touchRadioY);
					break;
				case TAB_EYES :
					createEyeList(touchRadioX, touchRadioY);
					break;
				case TAB_COLOR :
					addColor(avt.col, 0, 0);
					break;
				case TAB_NAME :
					createNameInput(touchKeyboardX, getTouchKeyboardY());
					break;
			}
			newTab(PlatformGame.menuCheck, "Done", new Runnable() {@Override public final void run() {exit();}});
			newTab(PlatformGame.menuX, "Undo", new Runnable() {@Override public final void run() {cancel();}});
			newTab(PlatformGame.menuAnimal, "Kind", TAB_ANIMAL);
			newTab(PlatformGame.menuEyes, "Eyes", TAB_EYES);
			newTab(PlatformGame.menuColor, "Color", TAB_COLOR);
			newTab(PlatformGame.menuGear, "Gear", new Runnable() {@Override public final void run() {goGear();}});
			newTab(PlatformGame.menuKeyboard, "Name", TAB_NAME);
			newTabs();
			registerBackNop();
		}
		
		private final void newTab(final Panmage img, final CharSequence txt, final byte tab) {
			final TouchButton btn = newTab(img, txt, new Runnable() {@Override public final void run() {reload(tab);}});
			if (currentTab == tab) {
				btn.setEnabled(false);
			}
		}
		
		private void reload(final byte tab) {
			currentTab = tab;
			Panscreen.set(new AvatarScreen(pc, old, avt));
		}
		
		private final void createAnimalList(final int x, final int y) {
			final List<String> animals = PlatformGame.getAnimals();
			final AvtListener anmLsn = new AvtListener() {
				@Override public final void update(final String value) {
					avt.anm = value; }};
			final RadioGroup anmGrp = addRadio("Animal", animals, anmLsn, x, y);
			anmGrp.setSelected(animals.indexOf(avt.anm));
		}
		
		private final void createEyeList(final int x, final int y) {
			final int numEyes = PlatformGame.getNumEyes();
			final ArrayList<String> eyes = new ArrayList<String>(numEyes);
			for (int i = 1; i <= numEyes; i++) {
			    eyes.add(Integer.toString(i));
			}
			final AvtListener eyeLsn = new AvtListener() {
				@Override public final void update(final String value) {
					avt.eye = Integer.parseInt(value); }};
			final RadioGroup eyeGrp = addRadio("Eye", eyes, eyeLsn, x, y);
			eyeGrp.setSelected(avt.eye - 1);
		}
		
		private final void createNameInput(final int x, final int y) {
			final Input namIn = addNameInput(avt, null, PlatformGame.MAX_NAME_AVATAR, x, y);
			namIn.append(avt.getName());
		}
		
		private final void goGear() {
			Panscreen.set(new GearScreen(pc, old, avt));
		}
		
		protected final void menuClassic() {
			final int left = getLeft();
			int x = left, y = getTop();
			createAnimalList(x, y);
			x += 72;
			createEyeList(x, y);
			addColor(avt.col, x, y);
			y -= 64;
			x = left;
			final MsgCloseListener gearLsn = new MsgCloseListener() {
                @Override public final void onClose() {
                    goGear(); }};
			addLink("Gear", gearLsn, x, y);
			y -= 16;
			createNameInput(x, y);
			y -= 16;
			x = addExit("Save", left, y);
			final MsgCloseListener canLsn = new MsgCloseListener() {
                @Override public final void onClose() {
                	cancel(); }};
            x = addPipe(x, y);
            x = addLink("Cancel", canLsn, x, y);
            final MsgCloseListener expLsn = new MsgCloseListener() {
                @Override public final void onClose() {
                    final Pangine engine = Pangine.getEngine();
                    engine.setImageSavingEnabled(true);
                    reloadAnimalStrip();
                    engine.setImageSavingEnabled(false);
                    setInfo(INFO_SAVED);
                    actor.load(pc); }};
            x = addPipe(x, y);
            x = addLink("Export", expLsn, x, y);
		}
		
		private final void cancel() {
			if (NAME_NEW.equals(old.getName())) {
                pc.profile.avatars.remove(pc.profile.currentAvatar);
                pc.profile.currentAvatar = pc.profile.avatars.get(0);
            } else {
                pc.profile.replaceAvatar(old);
            }
            reloadAnimalStrip();
            actor.load(pc);
            save = false;
            exit();
		}
		
		@Override
		protected boolean allow(final TextItem focused) {
			final String curr = avt.getName();
			if (Chartil.isEmpty(curr)) {
				setInfo(WARN_EMPTY);
				return false;
			}
			for (final Avatar a : pc.profile.avatars) {
				if (a != avt && a.getName().equals(curr)) {
					setInfo(WARN_DUPLICATE);
					return false;
				}
			}
			return true;
		}
		
		@Override
		protected void onExit() {
		    if (save) {
		        save();
		    }
		    goProfile();
		}
	}
	
	protected final static class GearScreen extends PlayerScreen {
	    private final Avatar old;
        private final Avatar avt;
        private RadioGroup jmpRadio = null;
        private List<RadioGroup> jmpColors = null;
        
        protected GearScreen(final PlayerContext pc, final Avatar old, final Avatar avt) {
            super(pc, false);
            this.old = old;
            this.avt = avt;
            tabsSupported = true;
        }
        
        private final void reattach(final String info, final TouchButton sub, final Panmage img, final String txt) {
        	setInfo(info);
        	if (sub == null) {
        		return;
        	}
        	sub.setOverlay(img, offx(img), OFF_OVERLAY_Y + 6);
        	sub.setText(PlatformGame.font, txt, OFF_TEXT_X, OFF_TEXT_Y);
        	TouchButton.reattach(sub);
        }
        
        private final void reattachBuy(final String info, final TouchButton sub) {
        	reattach(info, sub, PlatformGame.gem[0], "Buy");
        }
        
        protected final void createJumpList(final int x, final int y) {
            addHudGems();
            final JumpMode[] jumpModes = JumpMode.values();
            final List<String> jmps = new ArrayList<String>(jumpModes.length);
            for (final JumpMode jm : jumpModes) {
                jmps.add(jm.getName());
            }
            final TouchButton sub = Pangine.getEngine().isTouchSupported() ? newRadioSubmitButton(x, y) : null;
            TouchButton.detach(sub);
            final AvtListener jmpLsn = new AvtListener() {
                @Override public final void update(final String value) {
                    final JumpMode jm = Player.get(jumpModes, value);
                    final byte index = jm.getIndex();
                    if (pc.profile.isJumpModeAvailable(index)) {
                        clearInfo();
                        TouchButton.detach(sub);
                        setJumpMode(index);
                    } else if (pc.profile.isJumpModeTryable(index) && avt.jumpMode != index) {
                    	reattach("Free trial for 1 Level?", sub, PlatformGame.gemWhite, "Try");
                    } else {
                    	reattachBuy("Buy for " + jm.getCost() + "?", sub);
                    }
                }};
            final RadioSubmitListener jmpSubLsn = new AvtListener() {
                @Override public final void update(final String value) {
                    final JumpMode jm = Player.get(jumpModes, value);
                    final byte index = jm.getIndex();
                    if (!pc.profile.isJumpModeAvailable(index)) {
                        final int cost = jm.getCost();
                        if (Chartil.charAt(inf, 0) == 'F') {
                        	setJumpMode(index);
                        	reattachBuy("Equipped! Buy for " + jm.getCost() + "?", sub);
                        } else if (pc.profile.spendGems(cost)) {
                            pc.profile.availableJumpModes.add(Integer.valueOf(index));
                            setJumpMode(index);
                            setInfo("Purchased!");
                            TouchButton.detach(sub);
                        } else {
                            setInfo("You need more Gems");
                            TouchButton.detach(sub);
                        }
                    }
                }};
            jmpRadio = addRadio("Jump Mode", jmps, jmpSubLsn, jmpLsn, x, y, sub);
            initJumpMode();
        }
        
        @Override
		protected final void menu() {
			if (isTabEnabled()) {
				menuTouch();
			} else {
				menuClassic();
			}
		}
		
		protected final void menuTouch() {
			newTab(PlatformGame.menuCheck, "Back", new Runnable() {@Override public final void run() {exit();}});
			createJumpList(touchRadioX, touchRadioY);
			newTabs();
			registerBackExit();
		}
        
        protected final void menuClassic() {
            final int left = getLeft();
            int y = getTop();
            createJumpList(left, y);
            jmpColors = addColor(avt.jumpCol, left + 88, y);
            initJumpColors();
            y -= 64;
            addExit("Back", left, y);
        }
        
        private final void setJumpMode(final byte index) {
        	avt.jumpMode = index;
        	initJumpColors();
        }
        
        private final void initJumpColors() {
        	final boolean vis = avt.jumpMode == Player.JUMP_FLY;
        	for (final RadioGroup jmpColor : Coltil.unnull(jmpColors)) {
        		jmpColor.setVisible(vis);
        	}
        }
        
        private final void initJumpMode() {
            jmpRadio.setSelected(JumpMode.get(avt.jumpMode).getName());
        }
        
        @Override
        protected boolean allow(final TextItem focused) {
            final boolean a = super.allow(focused);
            if (a) {
                initJumpMode();
            }
            return a;
        }
        
        @Override
        protected void onExit() {
            Panscreen.set(new AvatarScreen(pc, old, avt));
        }
	}
	
	protected final static class AssistScreen extends PlayerScreen {
		private List<StringBuilder> as = null;
        
        protected AssistScreen(final PlayerContext pc) {
            super(pc, false);
        }
        
        @Override
        protected final void menu() throws Exception {
            addHudGems();
            final int left = getLeft();
            int y = getTop();
            final Assist[] assists = Profile.ASSISTS;
            as = new ArrayList<StringBuilder>(assists.length);
            for (final Assist a : assists) {
                as.add(new StringBuilder("  " + a.getName()));
            }
            final RadioSubmitListener aLsn = new RadioSubmitListener() {
                @Override public final void onSubmit(final RadioSubmitEvent event) {
                    highlightAssist(event.getIndex(), getAssist(event));
                }};
            final RadioSubmitListener aSubLsn = new RadioSubmitListener() {
                @Override public final void onSubmit(final RadioSubmitEvent event) {
                    final Assist a = getAssist(event);
                    final int index = event.getIndex();
                    if (pc.profile.isAssistAvailable(index)) {
                    	toggleAssist(index);
                    } else {
                        final int cost = a.getCost();
                        if (pc.profile.spendGems(cost)) {
                            pc.profile.availableAssists.add(Integer.valueOf(index));
                            toggleAssist(index);
                            setInfo("Purchased!");
                        } else {
                            setInfo("You need more Gems");
                        }
                    }
                }};
            addRadio("Assists", as, aSubLsn, aLsn, left, y);
            initAssists();
            y -= 64;
            addExit("Back", left, y);
            highlightAssist(0, Profile.ASSISTS[0]);
        }
        
        private final Assist getAssist(final RadioSubmitEvent event) {
        	return Player.get(Profile.ASSISTS, event.toString().substring(2));
        }
        
        private final void highlightAssist(final int index, final Assist a) {
        	if (pc.profile.isAssistAvailable(index)) {
                clearInfo();
            } else {
                setInfo("Buy for " + a.getCost() + "?");
            }
        }
        
        private final void toggleAssist(final int index) {
            pc.profile.toggleAssist(index);
            initAssists();
        }
        
        private final void initAssists() {
        	final int size = as.size();
        	for (int i = 0; i < size; i++) {
        		as.get(i).setCharAt(0, getFlag(pc.profile.isAssistActive(i)));
        	}
        }
        
        @Override
        protected void onExit() {
        	save();
            goProfile();
        }
    }
	
	protected final static class InfoScreen extends PlayerScreen {
		private final static byte TAB_AWARD = 0;
		private final static byte TAB_STATS = 1;
		protected final static byte TAB_GOALS = 2;
		protected static byte currentTab = TAB_AWARD;
	    private RadioGroup achRadio = null;
	    private final StringBuilder achDesc = new StringBuilder();
	    private final StringBuilder rankDesc = new StringBuilder();
	    private final List<Panctor> goalStars = new ArrayList<Panctor>(3);
	    private final List<Panctor> rankStars = new ArrayList<Panctor>(Profile.POINTS_PER_RANK);
	    final boolean fullMenu;
	    
        protected InfoScreen(final PlayerContext pc, final boolean fullMenu) {
            super(pc, false);
            tabsSupported = true;
            this.fullMenu = fullMenu;
        }

        @Override
		protected final void menu() {
        	if (Goal.isAnyMet(pc)) {
				createGoalMet(touchRadioX, touchRadioY);
			} else if (isTabEnabled()) {
				menuTouch();
			} else {
				menuClassic();
			}
		}
		
		protected final void menuTouch() {
			if (!fullMenu) {
				currentTab = TAB_GOALS;
			}
			switch (currentTab) {
				case TAB_AWARD :
					createAchievementList(touchRadioX, touchRadioY);
					break;
				case TAB_STATS :
					createStatsList(touchRadioX, touchRadioY);
					break;
				case TAB_GOALS :
					createGoalsList(touchRadioX, touchRadioY);
					break;
			}
			newTab(PlatformGame.menuCheck, "Done", new Runnable() {@Override public final void run() {exit();}});
			if (fullMenu) {
				newTab(PlatformGame.menuTrophy, "Award", TAB_AWARD);
				newTab(PlatformGame.menuGraph, "Stats", TAB_STATS);
				newTab(null, "Goals", TAB_GOALS);
			}
			newTabs();
			registerBackExit();
		}
		
		//TODO newTab/reload almost same as AvatarScreen
		private final void newTab(final Panmage img, final CharSequence txt, final byte tab) {
			final TouchButton btn = newTab(img, txt, new Runnable() {@Override public final void run() {reload(tab);}});
			if (currentTab == tab) {
				btn.setEnabled(false);
			}
		}
		
		private void reload(final byte tab) {
			currentTab = tab;
			Panscreen.set(new InfoScreen(pc, fullMenu));
		}
		
		private final void createAchievementList(final int x, final int y) {
            final int total = Achievement.ALL.length;
            final StringBuilder b = new StringBuilder();
            final List<String> ach = new ArrayList<String>(total);
            for (int i = 0; i < total; i++) {
                Chartil.clear(b);
                b.append(getFlag(pc.profile.achievements.contains(Integer.valueOf(i)))).append(' ');
                b.append(Achievement.ALL[i].getName());
                ach.add(b.toString());
            }
            final RadioSubmitListener achLsn = new RadioSubmitListener() {
                @Override public final void onSubmit(final RadioSubmitEvent event) {
                    setAchDesc(event.toString());
            }};
            achRadio = addRadio("Achievements", ach, achLsn, x, y);
            addTitleTiny(achDesc, x + (isTabEnabled() ? OFF_RADIO_LIST : 0), y - 64);
            initAchDesc();
		}
		
		private final void createStatsList(final int x, final int y) {
			addRadio("Statistics", pc.profile.stats.toList(), null, x, y);
		}
		
		private final void createGoalsList(final int x, int y) {
			addTitle("Goals", x, y);
			y -= 16;
			Goal.initGoals(pc);
			final boolean img = isTabEnabled();
			for (final Goal g : pc.profile.currentGoals) {
				y = addGoal(g, x, y, img);
			}
			addRankPoints(x, y, img);
		}
		
		private final int addRankPoints(final int x, int y, final boolean img) {
			final Profile prf = pc.profile;
			y -= 8;
			Chartil.set(rankDesc, "Rank: " + prf.getRank());
			addTitle(rankDesc, x, y);
			y -= 8;
			final int currPoints = prf.getCurrentGoalPoints();
			if (img) {
				y -= 8;
				addStars(x, y, currPoints, Profile.POINTS_PER_RANK, rankStars);
			} else {
				final StringBuilder b = new StringBuilder();
				Chartil.appendMulti(b, '*', currPoints);
				Chartil.appendMulti(b, '.', Profile.POINTS_PER_RANK - currPoints);
				addTitle(b, x, y);
			}
			return y;
		}
		
		private final void addStars(final int x, final int y, final int currPoints, final int max, final List<Panctor> list) {
			int xc = x;
			list.clear();
			for (int i = 0; i < max; i++) {
				final Panctor star;
				if (i < currPoints) {
					star = new Gem(PlatformGame.gemGoal);
					addActor(star, xc, y);
				} else {
					star = addEmptyStar(xc, y);
				}
				list.add(star);
				xc += 17;
			}
		}
		
		private final Panctor addEmptyStar(final int x, final int y) {
			final Panctor star = addActor(x, y);
			star.setView(PlatformGame.emptyGoal);
			return star;
		}
		
		private final int addGoal(final Goal g, final int x, int y, final boolean img) {
			final byte award = g.award;
			addTitle(g.getName(), x, y);
			final int off;
			if (img) {
				y -= 17;
				addStars(x, y, award, award, goalStars);
				off = 56;
			} else {
				y -= 8;
				addTitle(award == 1 ? "*" : award == 2 ? "**" : "***", x, y);
				off = 32;
			}
			addTitleTiny(g.getProgress(pc), x + off, y);
			y -= (img ? 9 : 8);
			return y;
		}
		
		private final void createGoalMet(final int x, int y) {
			addTitle("Success!", x, y);
			y -= 16;
			final Profile prf = pc.profile;
			final Goal[] goals = prf.currentGoals;
			for (int i = 0; i < 3; i++) {
				final Goal g = goals[i];
				if (g != null && g.isMet(pc)) {
					y = addGoal(g, x, y, true);
					y = addRankPoints(x, y, true);
					addGoalPoints(i, x, y);
					break;
				}
			}
			addHudGems();
		}
		
		private final void addContinue(final int x, final int y) {
			if (isTabEnabled()) {
				newTab(PlatformGame.menuCheck, "Done", new Runnable() {
					@Override public final void run() {
						reload(TAB_GOALS);
					}});
				newTabs();
				registerBackExit();
			} else {
				addLink("Continue", new MsgCloseListener() {
					@Override protected final void onClose() {
						reload(TAB_GOALS);
					}}, x, y - 16);
			}
		}
		
		private final void addGoalTimer(final TimerListener listener) {
			Pangine.getEngine().addTimer(rankStars.get(0), 30, listener);
		}
		
		private final void addGoalPoints(final int goalIndex, final int x, final int y) {
			addGoalTimer(new TimerListener() {
				@Override public final void onTimer(final TimerEvent event) {
					final Profile prf = pc.profile;
					for (int i = goalStars.size() - 1; i >= 0; i--) {
						final Panctor goalStar = goalStars.get(i);
						if (goalStar.getClass() == Gem.class) {
							final int rank = prf.getRank();
							prf.goalPoints++;
							final int newRank = prf.getRank();
							for (int j = 0; j < Profile.POINTS_PER_RANK; j++) {
								final Panctor rankStar = rankStars.get(j);
								if (rankStar.getClass() != Gem.class) {
									goalStar.swapPositions(rankStar);
								}
							}
							if (newRank >= rank) {
								// Rank up
								addGoalTimer(new TimerListener() {
									@Override public final void onTimer(final TimerEvent event) {
										pc.addGems(1000);
										Chartil.set(rankDesc, "Reached rank " + prf.getRank() + ", 1000 Gem bonus");
										addGoalPoints(goalIndex, x, y);
										for (int j = 0; j < Profile.POINTS_PER_RANK; j++) {
											final Panctor rankStar = rankStars.get(j);
											final Panple pos = rankStar.getPosition();
											rankStars.set(j, addEmptyStar((int) pos.getX(), (int) pos.getY()));
											rankStar.destroy();
										}
									}});
							} else {
								// Add remaining points (if any)
								addGoalPoints(goalIndex, x, y);
							}
							return;
						}
						// No more points to add, so finish
						final Goal[] goals = prf.currentGoals;
						goals[goalIndex] = Goal.newGoal(goals[goalIndex].award, pc);
						save();
						addContinue(x, y);
					}
				}});
		}
        
        protected final void menuClassic() {
            final int left = getLeft();
            int y = getTop();
            createAchievementList(left, y);
            createGoalsList(left + 120, y);
            y -= 80;
            createStatsList(left, y);
            y -= 64;
            addExit("Back", left, y);
        }
        
        private final void setAchDesc(String achName) {
            final String newDesc;
            if (Chartil.isEmpty(achName)) {
                newDesc = "";
            } else {
                achName = achName.substring(2);
                final Achievement ach = Achievement.get(achName);
                if (ach == null) {
                    throw new IllegalArgumentException("Could not find Achievement " + achName);
                }
                newDesc = ach.getDescription();
            }
            Chartil.set(achDesc, newDesc);
        }
        
        private final void initAchDesc() {
            setAchDesc((String) achRadio.getSelected());
        }
        
        @Override
        protected boolean allow(final TextItem focused) {
            if (focused == achRadio) {
                initAchDesc();
            } else {
                Chartil.clear(achDesc);
            }
            return super.allow(focused);
        }
        
        @Override
        protected void onExit() {
        	goalStars.clear();
        	rankStars.clear();
        	if (fullMenu) {
        		goProfile();
        	} else {
        		goMap();
        	}
        }
	}
	
	protected final static class OptionsScreen extends PlayerScreen {
		final StringBuilder msgAuto = new StringBuilder();
	    final StringBuilder msgSpeed = new StringBuilder();
	    
        protected OptionsScreen(final PlayerContext pc) {
            super(pc, false);
            tabsSupported = true;
        }
        
        @Override
        protected final void menu() {
            if (isTabEnabled()) {
                menuTouch();
            } else {
                menuClassic();
            }
        }
        
        protected final void menuTouch() {
            final Pangine engine = Pangine.getEngine();
            final Panple btnSize = PlatformGame.menu.getSize();
            final int btnW = (int) btnSize.getX(), btnH = (int) btnSize.getY(), offY = btnH * 4 / 3;
            int x = btnW / 2, y = engine.getEffectiveHeight() - btnH - offY;
            newFormButton("AutoToggle", x, y, null, new Runnable() {@Override public final void run() {toggleAuto();}});
            addTitle(msgAuto, x + btnW + 8, y);
            setMessageAuto();
            y -= offY;
            newFormButton("SpeedDown", x, y, PlatformGame.menuLeft, new Runnable() {@Override public final void run() {incSpeed(-1);}});
            newFormButton("SpeedUp", engine.getEffectiveWidth() - x - btnW, y, PlatformGame.menuRight, new Runnable() {@Override public final void run() {incSpeed(1);}});
            setMessageSpeed();
            addTitle(msgSpeed, x + btnW + 8, y);
            newTab(PlatformGame.menuCheck, "Done", new Runnable() {@Override public final void run() {exit();}});
            newTabs();
            registerBackExit();
        }
        
        protected final void menuClassic() {
        }
        
        private final void toggleAuto() {
        	pc.profile.autoRun = !pc.profile.autoRun;
        	setMessageAuto();
        }
        
        private final void setMessageAuto() {
        	final String s;
        	if (pc.profile.autoRun) {
        		s = "Auto-run, tap to jump";
        	} else {
        		s = "On-screen buttons";
        	}
        	Chartil.set(msgAuto, s);
        }
        
        private final void incSpeed(final int dir) {
            final int amt = (Profile.DEF_FRAME_RATE - Profile.MIN_FRAME_RATE) * dir;
            int frameRate = pc.profile.frameRate + amt;
            if (frameRate > Profile.MAX_FRAME_RATE) {
                frameRate = Profile.MIN_FRAME_RATE;
            } else if (frameRate < Profile.MIN_FRAME_RATE) {
                frameRate = Profile.MAX_FRAME_RATE;
            }
            pc.profile.frameRate = frameRate;
            setMessageSpeed();
            Pangine.getEngine().setFrameRate(frameRate);
        }
        
        private final void setMessageSpeed() {
            final String s;
            switch(pc.profile.frameRate) {
                case Profile.MIN_FRAME_RATE :
                    s = "Slow";
                    break;
                case Profile.MAX_FRAME_RATE :
                    s = "Fast";
                    break;
                default :
                    s = "Medium";
            }
            Chartil.set(msgSpeed, "Game Speed: " + s);
        }
        
        @Override
        protected void onExit() {
        	save();
            goProfile();
        }
	}
	
	private final static char getFlag(final boolean b) {
		return b ? CHAR_ON : ' ';
	}
	
	private final static int getLineColor(final float c) {
		return Math.round(c * 4);
	}
	
	private final static class Model extends Panctor implements StepListener {
	    private int blinkTimer = Mathtil.randi(PlatformGame.DUR_BLINK / 4, PlatformGame.DUR_BLINK * 3 / 4);
	    private int mirrorTimer = Mathtil.randi(60, 240);
	    private boolean origDir = true;
	    private Accessories acc = null;
	    
	    private Model(final PlayerContext pc) {
	    	load(pc);
	    }
	    
	    private void load(final PlayerContext pc) {
	    	if (acc != null) {
	    		acc.destroy();
	    	}
	    	acc = new Accessories(pc);
	    	acc.onStepEnd(this);
	    	setView(pc.guy);
	    }
	    
        @Override
        public final void onStep(final StepEvent event) {
            blinkTimer--;
            if (blinkTimer <= 0) {
                setView((Panimation) getView());
                blinkTimer = Mathtil.randi(PlatformGame.DUR_BLINK * 5 / 4, PlatformGame.DUR_BLINK * 7 / 4);
            }
            mirrorTimer--;
            if (mirrorTimer <= 0) {
            	setMirror(!isMirror());
            	origDir = !origDir;
            	final int base = origDir ? 90 : 30;
            	mirrorTimer = Mathtil.randi(base, base * 2);
            }
            acc.onStepEnd(this);
        }
        
        @Override
    	protected final void onDestroy() {
    		acc.destroy();
    	}
	}
}
