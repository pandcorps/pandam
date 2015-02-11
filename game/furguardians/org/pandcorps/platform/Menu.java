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

import java.io.*;
import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.chr.*;
import org.pandcorps.core.img.*;
import org.pandcorps.core.io.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.Panteraction.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.event.handler.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Input.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.touch.*;
import org.pandcorps.platform.Map.*;
import org.pandcorps.platform.Enemy.*;
import org.pandcorps.platform.Profile.*;
import org.pandcorps.platform.Avatar.*;
import org.pandcorps.platform.Player.*;

public class Menu {
    protected final static byte TOUCH_FULL = 0;
    protected final static byte TOUCH_HORIZONTAL = 1;
    protected final static byte TOUCH_JUMP = 2;
    private final static short SPEED_MENU_FADE = 9;
    private final static int SIZE_FONT = 8;
    protected final static String NAME_NEW = "org.pandcorps.new";
    private final static String WARN_DELETE = "Press Erase again to confirm";
    private final static String WARN_EMPTY = "Must have a name";
    private final static String WARN_DUPLICATE = "Name already used";
    private final static String INFO_SAVED = "Saved images";
    private final static int Y_PLAYER = 16;
    private final static char CHAR_ON = 2;
    private final static String NEW_AVATAR_NAME = "New";
    private static boolean newProfile = false;
    private static int radioLinesPerPage = 5;
    
	protected abstract static class PlayerScreen extends Panscreen {
		protected Panlayer room;
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
		protected static int touchRadioX = 40;
		protected static int touchRadioY = 140;
		protected final static int touchKeyboardX = 8;
		protected static int OFF_RADIO_Y = 100;
		protected final int rankStarX;
		protected boolean initForm = true;
		protected boolean showGems = true;
		
		protected PlayerScreen(final PlayerContext pc, final boolean fadeIn) {
			radioLinesPerPage = 5;
			this.pc = pc;
			this.fadeIn = fadeIn;
			tabs = isTabEnabled() ? new ArrayList<TouchButton>() : null;
			rankStarX = getRankStarX();
			if (touchRadioY == 140) {
			    final Pangine engine = Pangine.getEngine();
				final int h = engine.getEffectiveHeight();
				// If h is 192, OFF_RADIO_Y should be 72; should increase with h, up to 100
				OFF_RADIO_Y = Math.min(h - 120, 100);
				final int menuHeight = OFF_RADIO_Y + PlatformGame.MENU_H; // 112
				final int menuBottom = (h - menuHeight) / 2; // 40
				touchRadioY = menuBottom + OFF_RADIO_Y; // 112
				// If w is 344 or less, touchRadioX should be 0; should increase with w, up to 40
				touchRadioX = Math.min(Math.max(engine.getEffectiveWidth() - 344, 0), 40);
			}
		}
		
		protected final static int getRankStarX() {
			return (Pangine.getEngine().getEffectiveWidth() - 170) / 2;
		}
		
		protected boolean isPlayerDisplayed() {
			return true;
		}
		
		@Override
		protected final void load() throws Exception {
			final int w = PlatformGame.SCREEN_W;
			center = w / 2;
			room = PlatformGame.createRoom(w, PlatformGame.SCREEN_H);
			final Pangine engine = Pangine.getEngine();
			final Pancolor bgColor = new FinPancolor((short) 128, (short) 192, Pancolor.MAX_VALUE);
			engine.setBgColor(PixelFilter.filterColor(Map.theme.getSkyFilter(), bgColor));
			
			tm = new TileMap(Pantil.vmid(), room, ImtilX.DIM, ImtilX.DIM);
			Level.tm = tm;
			timg = Level.getTileImage();
			final TileMapImage[][] imgMap = tm.splitImageMap(timg);
			tm.fillBackground(imgMap[1][1], 0, 1);
			room.addActor(tm);
			
			if (pc != null) {
				if (isPlayerDisplayed()) {
					actor = addActor(pc, center);
				}
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
			if (showGems && pc != null && pc.profile != null) {
				addHudGems();
				addHudRank();
			}
			menu();
			if (initForm && ctrl != null) { // Null on TitleScreen
				form.init();
			}
			
			if (fadeIn) {
			    PlatformGame.fadeIn(room, SPEED_MENU_FADE);
			}
			PlatformGame.playMenuMusic();
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
		
		protected final static int getTouchButtonRadius() {
			return (PlatformGame.DIM_BUTTON / 2) + 1;
		}
		
		protected final static void initTouchButtons(final Panlayer room, final ControlScheme ctrl,
				final byte mode, final boolean input, final boolean act, final Panctor bound) {
			//info("initTouch for " + getClass().getName());
			if (ctrl == null) {
				return;
			}
			//info("Found ControlScheme");
			final Panput temp = ctrl.get1();
			if (temp != null && !(temp.getDevice() instanceof Touchscreen)) {
        		return;
        	}
			//info("Found touch scheme");
			final Pangine engine = Pangine.getEngine();
			if (input) {
				engine.clearTouchButtons();
			}
			final int r = engine.getEffectiveWidth(), t = engine.getEffectiveHeight();
			int rx = 0, y = 0;
			TouchButton down = null, up = null, act2 = null;
			Panmage rt = PlatformGame.right2, rtIn = PlatformGame.right2In, lt = PlatformGame.left2, ltIn = PlatformGame.left2In;
			final boolean full = mode == TOUCH_FULL;
			if (full) {
			    final int rad = getTouchButtonRadius(), dmtr = rad * 2;
				y = rad;
				down = addDiamondButton(room, "Down", rad, 0, input, act, ctrl.getDown());
				up = addDiamondButton(room, "Up", rad, dmtr, input, act, ctrl.getUp());
				rx = dmtr;
				//act2 = addCircleButton(room, "Act2", r - d, 0, input, act, ctrl.get2());
				//sub = addCircleButton(room, "Sub", r - d, engine.getEffectiveHeight() - d, input, act, ctrl.getSubmit());
				final Panple ts = PlatformGame.menu.getSize();
				final int tw = (int) ts.getX();
				act2 = newFormButton(room, "Act2", r - tw, t - (int) ts.getY(), PlatformGame.menuOptions, "Menu");
				newFormButton(room, "Goals", r - (tw * 2), t - 19, PlatformGame.gemGoal[0], new Runnable() {
                    @Override public final void run() {
                        PlatformGame.goGoals(PlatformGame.pcs.get(0)); }}).getActorOverlay().getPosition().addY(-10);
				rt = lt = PlatformGame.diamond;
				rtIn = ltIn = PlatformGame.diamondIn;
			} else if (mode == TOUCH_HORIZONTAL) {
				rx = (int) (PlatformGame.DIM_BUTTON * 1.25f);
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
			} else if (act) {
				final TouchButton pause;
				pause = new TouchButton(engine.getInteraction(), room, "Pause", r - 17, t - 17, 0, PlatformGame.menuPause, PlatformGame.menuPause, true);
				engine.registerTouchButton(pause);
				registerPromptQuit(pause.getActor(), pause);
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
        private static TouchButton quitMenu = null;
        private static Pantext quitMsg = null;
        private static ListActorHandler quitHandler = null;
		
		protected final static void registerBackPromptQuit(final Panctor bound) {
			registerPromptQuit(bound, Pangine.getEngine().getInteraction().BACK);
		}
		
		protected final static void registerPromptQuit(final Panctor bound, final Panput input) {
			destroyPromptQuit();
		    bound.register(input, new ActionEndListener() {
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
		    final int h = engine.getEffectiveHeight();
		    final Panscreen screen = Panscreen.get();
		    final boolean platformScreen = screen instanceof PlatformGame.PlatformScreen;
		    int btnY = 0;
		    if (platformScreen) {
		        final PlayerContext pc = Coltil.get(PlatformGame.pcs, 0);
		        if (pc != null) {
    		        quitHandler = new ListActorHandler();
    		        room.setAddHandler(quitHandler);
    		        final InfoScreen iscrn = new InfoScreen(pc, false);
    		        iscrn.room = room;
    		        iscrn.form = new Panform(room, pc.ctrl);
    		        btnY = iscrn.displayGoals(getRankStarX(), h - 34, null) - PlatformGame.MENU_H - 8;
    		        room.setAddHandler(null);
		        }
		    }
		    final Panple btnSize = PlatformGame.menu.getSize();
		    if (btnY == 0) {
		    	btnY = TouchTabs.off(h, btnSize.getY());
		    }
            final boolean menuScreen = screen instanceof PlayerScreen;
            final int numButtons = menuScreen ? 2 : 3, r = engine.getEffectiveWidth();
            final int btnW = (int) btnSize.getX(), btnX = TouchTabs.off(r, btnW * numButtons);
            quitYes = newFormButton(room, "Quit", btnX + btnW * (numButtons - 1), btnY, PlatformGame.menuOff, "Quit", new Runnable() {
                @Override public final void run() { engine.exit(); }});
            quitYes.setZ(15);
            final String noLbl;
            final Panmage noImg;
            if (menuScreen) {
            	noLbl = "Menu";
            	noImg = PlatformGame.menuOptions;
            } else {
            	noLbl = "Play";
            	noImg = PlatformGame.menuRight;
            	quitMenu = newFormButton(room, "Menu", btnX + btnW, btnY, PlatformGame.menuOptions, "Menu", new Runnable() {
                    @Override public final void run() {
                    	PlatformGame.notifications.clear();
                    	destroyPromptQuit();
                    	PlatformGame.fadeOut(PlatformGame.room, new ProfileScreen(PlatformGame.pcs.get(0), true)); }});
                quitMenu.setZ(15);
                if (platformScreen) {
	                quitMsg = new Pantext(Pantil.vmid(), PlatformGame.fontTiny, "You will lose your progress in this Level if you leave");
	                quitMsg.getPosition().set(r / 2, btnY - 7, 15);
	                quitMsg.centerX();
	                room.addActor(quitMsg);
                }
            }
            quitNo = newFormButton(room, noLbl, btnX, btnY, noImg, noLbl, new Runnable() {
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
            TouchButton.destroy(quitMenu);
            quitMenu = null;
            Panctor.destroy(quitMsg);
            quitMsg = null;
            ListActorHandler.destroy(quitHandler);
            quitHandler = null;
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
				actor.getPosition().set(x, y, TOUCH_BUTTON_DEPTH);
				room.addActor(actor);
			}
			return button;
		}
		
		protected final static int TOUCH_BUTTON_DEPTH = 500;
		
		protected final static int offx(final Panmage img) {
			return img == null ? 0 : TouchTabs.off(PlatformGame.menu.getSize().getX(), img.getSize().getX());
		}
		
		protected final static int offy(final Panmage img, final CharSequence txt) {
			if (img == null) {
				return 0;
			}
			float btnH = PlatformGame.menu.getSize().getY();
			if (txt != null) {
				btnH -= 10;
			}
			final float imgH = img.getSize().getY();
			int off = TouchTabs.off(btnH, imgH);
			if (txt != null) {
				off += ((imgH < 18) ? 7 : 9);
			}
			return off;
		}
		
		protected final static int OFF_TEXT_X = 4;
		protected final static int OFF_TEXT_Y = 2;
		
		protected final TouchButton newTab(final Panmage img, final CharSequence txt, final Runnable listener) {
			final TouchButton tab = TouchTabs.newButton(getLayer(), Pantil.vmid(), PlatformGame.menu, PlatformGame.menuIn, img, offx(img), offy(img, txt), PlatformGame.font, txt, OFF_TEXT_X, OFF_TEXT_Y,
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
		
		protected final int displayGoals(final int x, int y, final List<Panctor> list) {
            Goal.initGoals(pc);
            final boolean img = isTabEnabled();
            for (final Goal g : pc.profile.currentGoals) {
                y = addGoal(g, x, y, img, list);
            }
            return y;
        }
		
		protected static boolean showNew = false;
        
        protected final int addGoal(final Goal g, final int x, int y, final boolean img, final List<Panctor> list) {
            final byte award = g.award;
            addTitle(((showNew && g.brandNew) ? "NEW " : "") + g.getName(), x, y);
            final int off;
            if (img) {
                y -= 17;
                addStars(x, y, award, award, list);
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
        
        protected final void addStars(final int x, final int y, final int currPoints, final int max, final List<Panctor> list) {
            int xc = x;
            Coltil.clear(list);
            for (int i = 0; i < max; i++) {
                final Panctor star;
                if (i < currPoints) {
                    star = new Gem(PlatformGame.gemGoal);
                    addActor(star, xc, y);
                } else {
                    star = addEmptyStar(xc, y);
                }
                if (list != null) {
                    list.add(star);
                }
                xc += 17;
            }
        }
        
        protected final Panctor addEmptyStar(final int x, final int y) {
            final Panctor star = addActor(x, y);
            star.setView(PlatformGame.emptyGoal);
            return star;
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
		
		protected final RadioGroup addRadio(final String title, final List<? extends CharSequence> list, final RadioSubmitListener subLsn, final RadioSubmitListener chgLsn, final int xb, int y, final TouchButton sub) {
			final int x;
			if (tabsSupported && isTabEnabled()) {
				final int yt = y - OFF_RADIO_Y;
				final String id = Pantil.vmid();
				ctrl.setUp(newFormButton(id + ".radio.up", xb, y, PlatformGame.menuUp));
				ctrl.setDown(newFormButton(id + ".radio.down", xb, yt, PlatformGame.menuDown));
				if (subLsn != null) {
					//final TouchButton sub = newFormButton(id + ".radio.submit", x + 200, yt, PlatformGame.menuCheck);
					//final TouchButton sub = null; // Will use tab bar to simulate submit button below
					ctrl.setSubmit(sub);
					ctrl.set1(sub);
				}
				x = xb + OFF_RADIO_LIST;
				if (OFF_RADIO_Y < 100) {
					y += (100 - OFF_RADIO_Y);
				}
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
			label.setLinesPerPage(radioLinesPerPage);
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
			final TouchButton btn = new TouchButton(engine.getInteraction(), layer, name, x, y, 0, PlatformGame.menu, PlatformGame.menuIn, img, offx(img), offy(img, txt), PlatformGame.font, txt, OFF_TEXT_X, OFF_TEXT_Y, true);
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
			return addColor(col, x, y, null);
		}
		
		protected final List<RadioGroup> addColor(final SimpleColor col, int x, int y, final String label) {
			if (tabsSupported && isTabEnabled()) {
				addColorTouch(col, label);
				return null;
			} else {
				return addColorClassic(col, x, y);
			}
		}
		
		protected final void addColorTouch(final SimpleColor col, final String label) {
			final String id = Pantil.vmid();
			final Pangine engine = Pangine.getEngine();
			final Panple btnSize = PlatformGame.menu.getSize();
			final int btnW = (int) btnSize.getX(), gapW = (btnW * 5) / 6, difW = btnW + gapW;
			final int minX = (engine.getEffectiveWidth() - (btnW * 3 + gapW * 2)) / 2;
			final int btnH = (int) btnSize.getY(), difH = btnH + 16;
			final int minY = (engine.getEffectiveHeight() - (btnH + difH)) / 2;
			int x = minX, y = minY + difH;
			if (label != null) {
				addTitle(label + " Color", x, y + btnH + 1);
			}
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
		
		protected final void createEyeList(final EyeData avt, final int numEyes, final int x, final int y) {
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
		
		protected final static int HUD_TEXT_Y = 20;
		
		protected final void addHudGems() {
		    final int gemX = center + 16, gemY = HUD_TEXT_Y;
            PlatformGame.addHudGem(room, gemX, gemY);
            PlatformGame.addHud(room, pc, gemX + PlatformGame.OFF_GEM, gemY, false, false);
		}
		
		protected final void addHudRank() {
			final int gemX = center + 96, gemY = HUD_TEXT_Y, textX = gemX + PlatformGame.OFF_GEM + 1;
			addActor(new Gem(PlatformGame.gemRank), gemX, gemY);
			addTitle("Rank", textX, gemY + 8);
			final CharSequence seq = new CallSequence() {@Override protected String call() {
				return String.valueOf(pc.profile.getRank());
			}};
			addTitle(seq, textX, gemY);
		}
		
		protected final void addHudAchievement() {
			final int gemX = center + 96, gemY = 37, textX = gemX + PlatformGame.OFF_GEM + 1;
			addActor(new Gem(PlatformGame.gemAchieve), gemX, gemY);
			addTitle("Trophies", textX, gemY + 8);
			final CharSequence seq = new CallSequence() {@Override protected String call() {
				return String.valueOf(pc.profile.achievements.size());
			}};
			addTitle(seq, textX, gemY);
		}
		
		protected final Input addNameInput(final PlayerData pd, final InputSubmitListener subLsn, final int max, final int x, final int y) {
		    final InputSubmitListener chgLsn = new InputSubmitListener() {
	            @Override public final void onSubmit(final InputSubmitEvent event) {
	            	final String name = event.toString();
	                pd.setName(name);
	                if (pc != null && pc.profile != null && "Dconsole".equalsIgnoreCase(name)) {
	                	pc.profile.consoleEnabled = true;
	                	//save();
	                }}};
	        return addInput("Name", subLsn, chgLsn, max, x, y);
		}
		
		protected final Input addInput(final String label, final InputSubmitListener subLsn, final InputSubmitListener chgLsn, final int max, final int x, final int y) {
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
	        addItem(in, x + ((label.length() + 1) * 8), y);
	        addTitle(label, x, y);
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
		
		protected final static PlayerContext generatePlayerContext(final int i) {
			final Profile prf = new Profile();
        	final Avatar avt = new Avatar();
        	prf.currentAvatar = avt;
        	prf.avatars.add(avt);
        	return new PlayerContext(prf, null, Integer.MAX_VALUE - i);
		}
		
		protected final Model addActor(final PlayerContext pc, final int x) {
			final Model actor = new Model(pc);
			PlatformGame.setPosition(actor, x, Y_PLAYER, PlatformGame.getDepthPlayer(pc.profile.currentAvatar.jumpMode));
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
		
		protected final void registerBack(final ActionEndListener listener) {
			final Panteraction interaction = Pangine.getEngine().getInteraction();
			final Panput back = interaction.BACK;
			interaction.unregister(back);
		    tm.register(back, listener);
		}
		
		protected final void registerBackExit() {
			registerBack(new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    exit(); }});
		}
		
		/*
		Back should always do something; this should not be used
		protected final void registerBackNop() {
			registerBack(new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) { }});
        }
        */
		
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
			ProfileScreen.currentTab = ProfileScreen.TAB_SELECT_AVATAR;
			PlatformGame.setScreen(new ProfileScreen(pc, false));
		}
		
		protected final void newProfile(final PlayerContext curr) {
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
            PlatformGame.setScreen(new NewScreen(pc, false));
		}
		
		protected final void goOptions() {
            PlatformGame.setScreen(new OptionsScreen(pc));
        }
		
		protected final void reloadAnimalStrip() {
			reloadAnimalStrip(pc, actor);
		}
		
		protected final static void reloadAnimalStrip(final PlayerContext pc, final Model actor) {
			PlatformGame.reloadAnimalStrip(pc, false);
			if (actor != null) {
				actor.load(pc);
			}
		}
		
		protected abstract class AvtListener implements RadioSubmitListener {
			@Override public final void onSubmit(final RadioSubmitEvent event) {
			    if (disabled) {
			        return;
			    }
				update(event.toString());
				reloadAnimalStrip();
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
		private final static String TITLE = PlatformGame.TITLE.toUpperCase();
		private static ArrayList<PlayerContext> tcs = new ArrayList<PlayerContext>(NUM_CHRS);
		private Pantext text = null;
		private Pantext trademark = null;
		int titleHeight = 0;
		
	    protected TitleScreen() {
            super(null, true);
            showGems = false;
        }
	    
	    @Override
        protected final void menu() {
	        PlatformGame.loaders = null;
	        final int bottom = getBottom();
	        final Pangine engine = Pangine.getEngine();
	        final boolean touch = engine.isTouchSupported();
	        final StringBuilder prompt = new StringBuilder();
	        if (touch) {
	            prompt.append("Tap to start");
	        } else {
	            prompt.append("Press anything");
	        }
	        text = addTitleCentered(prompt, bottom);
	        engine.addTimer(text, 360, new TimerListener() {@Override public final void onTimer(final TimerEvent event) {
	            if (text == null) {
	                return;
	            }
                setCentered(text, prompt, "Did you " + (touch ? "tap?" : "press something?"));
                engine.addTimer(text, 120, new TimerListener() {@Override public final void onTimer(final TimerEvent event) {
                    if (text == null) {
                        return;
                    }
                    setCentered(text, prompt, "Maybe you're not ready");
                    engine.addTimer(text, 120, new TimerListener() {@Override public final void onTimer(final TimerEvent event) {
                        if (text == null) {
                            return;
                        }
                        setCentered(text, prompt, "Exiting, retry when you're ready");
                        engine.addTimer(text, 120, new TimerListener() {@Override public final void onTimer(final TimerEvent event) {
                            if (text == null) {
                                return;
                            }
                            engine.exit();
                        }});
                    }});
                }});
            }});
	        titleHeight = Math.round(tm.getHeight() * 5f / 8f);
	        final int titleEnd = Cabin.CabinScreen.displayName(TITLE, titleHeight, 0);
	        final Panple titlePos = tm.getPosition(titleEnd, titleHeight);
	        trademark = addTitle("" + Pantext.CHAR_TRADEMARK, 1 + (int) titlePos.getX(), 8 + (int) titlePos.getY());
	        //addTitleCentered("Andrew Martin's Untitled Game" + Pantext.CHAR_TRADEMARK, engine.getEffectiveHeight() / 2);
	        addTitleCentered("Copyright " + Pantext.CHAR_COPYRIGHT + " " + PlatformGame.YEAR + " " + PlatformGame.AUTHOR, bottom + 16);
	        if (touch) {
	        	text.register(new ActionEndListener() {@Override public void onActionEnd(final ActionEndEvent event) {
		        	onAnything(event);
		        }});
	        } else {
		        text.register(new ActionStartListener() {@Override public void onActionStart(final ActionStartEvent event) {
		        	onAnything(event);
		        }});
	        }
	        try {
	        	String log = engine.getFatalLog();
	            if (log != null) {
	            	final String email = PlatformGame.getEmail();
	            	log = log.replace("org.pandcorps.platform.", "");
	            	log = log.replace("org.pandcorps.", "");
	            	log = log.replace(".java", "");
	            	engine.setClipboard("Please send this to " + email + Iotil.BR + PlatformGame.VERSION + Iotil.BR + log);
	            	addTitleTiny("Oh no!", 4, bottom + 48);
	            	addTitleTiny("It looks like the game crashed the last time you played.", 4, bottom + 42);
	            	addTitleTiny("We've copied an error report into your clipboard.", 4, bottom + 36);
	            	addTitleTiny("Please paste it into an email & send it to " + email + ".", 4, bottom + 30);
	            	addTitleTiny("We'll try to fix it!", 4, bottom + 24);
	            }
	        } catch (final Exception e) {
	        	// Just ignore; don't let error report generation cause another fatal error
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
	    		/*
	    		Something like this might help work around some issues.
	    		But onAnything is bound to text, which is destroyed here.
	    		So this method isn't even invoked a second time.
	    		Doing something like this would require binding onAnything to a different actor.
	    		if (FadeController.isFadingIn() || !FadeController.isFadingOut()) {
	    			disabled = false;
	    		}
	    		*/
        		return;
        	}
        	final Device device = event.getInput().getDevice();
        	if (device instanceof Touchscreen) {
        		final Touch touch = Pangine.getEngine().getInteraction().TOUCH;
        		ctrl = new ControlScheme(null, null, null, null, touch, touch, touch);
        	} else {
        		ctrl = ControlScheme.getDefault(device);
        	}
        	Panctor.destroy(text);
        	text = null;
        	Panctor.destroy(trademark);
        	trademark = null;
        	final int w = tm.getWidth();
        	Tiles.shatterBottomRight = Tiles.shatterTopRight = false;
        	int count = 0;
        	for (int i = 0; i < w; i++) {
        		final int titleIndex = tm.getIndex(i, titleHeight);
        		final Tile tile = tm.getTile(titleIndex);
        		if (tile == null) {
        			continue;
        		}
        		if (count == 1) {
	        		Tiles.shatterTopRight = true;
	        		Tiles.shatterBottomLeft = false;
        		} else if (count == (TITLE.length() - 1)) {
        			Tiles.shatterBottomRight = true;
        			Tiles.shatterTopLeft = false;
        		}
        		Tiles.newGemLetter(null, titleIndex, PlatformGame.getGemLetter(DynamicTileMap.getRawForeground(tile)));
        		tm.setTile(titleIndex, null);
        		PlatformGame.shatterLetter(tm.getPosition(titleIndex));
        		count++;
        	}
        	Tiles.shatterBottomLeft = Tiles.shatterBottomRight = Tiles.shatterTopLeft = Tiles.shatterTopRight = true;
            exit();
	    }
	    
	    private final static void setCentered(final Pantext text, final StringBuilder b, final String value) {
	    	if (text == null) {
	    		return;
	    	}
	    	text.uncenterX();
	    	Chartil.set(b, value);
	    	text.centerX();
	    }
	    
	    protected final static void generateTitleCharacters() {
	        for (int i = 0; i < NUM_CHRS; i++) {
	        	final PlayerContext tc = generatePlayerContext(i);
	        	tc.profile.currentAvatar.randomize();
	        	tcs.add(tc);
	        	//TODO Menu screens which show player can probably use full=false, but will need full load when done
	        	PlatformGame.reloadAnimalStrip(tc, false);
	        }
	    }
	    
	    @Override
        protected final void onExit() {
	        String defaultProfileName = Config.defaultProfileName;
	        final List<String> availableProfiles;
	        if (defaultProfileName == null) {
	            availableProfiles = PlatformGame.getAvailableProfiles();
	            if (Coltil.size(availableProfiles) == 1) {
	                defaultProfileName = availableProfiles.get(0);
	            }
	        } else {
	            availableProfiles = null;
	        }
			if (defaultProfileName == null) {
				final SelectScreen screen = new SelectScreen(null, false, availableProfiles);
		        screen.ctrl = ctrl;
		        PlatformGame.setScreen(screen);
			} else {
				try {
					PlatformGame.loadProfile(defaultProfileName, ctrl, PlatformGame.pcs.size());
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
	    	PlatformGame.blockWord = PlatformGame.defaultBlockWord;
	    }
	}
	
	protected final static class SelectScreen extends PlayerScreen {
		private final PlayerContext curr;
		private List<String> availableProfiles = null;
		
		protected SelectScreen(final PlayerContext pc, final boolean fadeIn, final List<String> availableProfiles) {
			super(null, fadeIn);
			if (pc != null) {
				ctrl = pc.ctrl;
			}
			curr = pc;
			this.availableProfiles = availableProfiles;
			tabsSupported = true;
			showGems = false;
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
			newToDefault = false;
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
			if (availableProfiles == null) {
			    availableProfiles = PlatformGame.getAvailableProfiles();
			}
			if (Coltil.isValued(availableProfiles)) {
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
				addRadio("Pick Profile", availableProfiles, prfLsn, null, x, y);
				return true;
			} else {
				newProfile();
				return false;
			}
		}
		
		private final void newProfile() {
			newProfile(curr);
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
            showGems = false;
        }
        
        @Override
        protected final boolean isPlayerDisplayed() {
        	return false;
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
            if (Pangine.getEngine().isTouchSupported() && Config.defaultProfileName == null && newToDefault) {
            	Config.defaultProfileName = pc.profile.getName();
            }
            Config.serialize();
            save();
            newProfile = true;
            goProfile();
        }
	}
	
	private static boolean newToDefault = true;
	
	protected final static class ProfileScreen extends PlayerScreen {
		protected final static byte TAB_SELECT_AVATAR = 0;
		private final static byte TAB_NEW = 1;
		protected static byte currentTab = TAB_SELECT_AVATAR;
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
			switch (currentTab) {
				case TAB_SELECT_AVATAR:
					createAvatarList(touchRadioX, touchRadioY);
					break;
				case TAB_NEW:
					createNewMenu(touchRadioX, touchRadioY);
					break;
			}
			newTab(PlatformGame.menuCheck, "Done", new Runnable() {@Override public final void run() {exit();}});
			newTab(PlatformGame.menuAvatar, "Edit", new Runnable() {@Override public final void run() {goAvatar();}});
			if (!newProfile) {
				//newTab(PlatformGame.menuPlus, "New", new Runnable() {@Override public final void run() {newAvatar();}});
				newTab(PlatformGame.menuPlus, "New", TAB_NEW);
				if (getAvatarsSize() > 1) {
					newTab(PlatformGame.menuMinus, "Erase", new Runnable() {@Override public final void run() {delete();}});
				}
				newTab(PlatformGame.menuInfo, "Info", new Runnable() {@Override public final void run() {goInfo();}});
				if (isPlayer1()) {
				    newTab(PlatformGame.menuMenu, "Setup", new Runnable() {@Override public final void run() {goOptions();}});
					newTab(PlatformGame.menuOff, "Quit", new Runnable() {@Override public final void run() {quit();}});
				}
			}
			newProfile = false;
			//TODO The other stuff from menuClassic, move newTabs() into super class
			newTabs();
		}
		
		private final void newTab(final Panmage img, final CharSequence txt, final byte tab) {
			final TouchButton btn = newTab(img, txt, new Runnable() {@Override public final void run() {reload(tab);}});
			if (currentTab == tab) {
				btn.setEnabled(false);
			}
		}
		
		private void reload(final byte tab) {
			currentTab = tab;
			PlatformGame.setScreen(new ProfileScreen(pc, false));
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
		
		private final void createNewMenu(final int x, final int y) {
			final RadioSubmitListener subLsn = new RadioSubmitListener() {
				@Override public final void onSubmit(final RadioSubmitEvent event) {
					final String label = event.toString();
					if ("Avatar".equals(label)) {
						newAvatar();
					} else if ("Profile".equals(label)) {
						newToDefault = false;
						Config.defaultProfileName = null;
						newProfile(pc);
					} else {
						reload(TAB_SELECT_AVATAR);
					}
				}};
			final TouchButton sub = newRadioSubmitButton(x, y);
			addRadio("New Avatar or Profile?", Arrays.asList("Avatar", "Profile", "Cancel"), subLsn, null, x, y, sub);
			final int bottom = getBottom(), tx = 92;
			addTitleTiny("A new Avatar lets one player try a different character.", tx, bottom + 23);
			addTitleTiny("The old Avatar is kept.  You can switch back and forth.", tx, bottom + 17);
			addTitleTiny("You keep your Gems and Goals when switching Avatars.", tx, bottom + 11);
			addTitleTiny("A new Profile is for a new person using this device.", tx, bottom + 5);
			addTitleTiny("Each Profile has its own set of Gems and Goals.", tx, bottom - 1);
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
                    PlatformGame.setScreen(new AssistScreen(pc)); }};
            x = addPipe(x, y);
            x = addLink("Perks", astLsn, x, y);
			final MsgCloseListener prfLsn = new MsgCloseListener() {
                @Override public final void onClose() {
                    save();
                    PlatformGame.setScreen(new SelectScreen(pc, false, null)); }};
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
		    PlatformGame.setScreen(new AvatarScreen(pc));
		}
		
		private final void newAvatar() {
			final Avatar avt = new Avatar();
            avt.randomize();
            avt.setName(NAME_NEW);
            pc.profile.avatars.add(avt);
            pc.profile.currentAvatar = avt;
            reloadAnimalStrip();
            AvatarScreen.currentTab = AvatarScreen.TAB_NAME;
            goAvatar();
		}
		
		private final void goInfo() {
			PlatformGame.setScreen(new InfoScreen(pc, true));
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
					addColor(avt.col, 0, 0, "Avatar");
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
			registerBack(new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    cancel(); }});
		}
		
		private final void newTab(final Panmage img, final CharSequence txt, final byte tab) {
			final TouchButton btn = newTab(img, txt, new Runnable() {@Override public final void run() {reload(tab);}});
			if (currentTab == tab) {
				btn.setEnabled(false);
			}
		}
		
		private void reload(final byte tab) {
			currentTab = tab;
			PlatformGame.setScreen(new AvatarScreen(pc, old, avt));
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
			createEyeList(avt, PlatformGame.getNumEyes(), x, y);
		}
		
		private final void createNameInput(final int x, final int y) {
			final Input namIn = addNameInput(avt, null, PlatformGame.MAX_NAME_AVATAR, x, y);
			namIn.append(avt.getName());
		}
		
		private final void goGear() {
			GearScreen.currentTab = GearScreen.TAB_DEFAULT;
			PlatformGame.setScreen(new GearScreen(pc, old, avt));
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
                    setInfo(INFO_SAVED); }};
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
	    private final static byte TAB_CLOTHES = 0;
	    private final static byte TAB_CLOTHES_COL = 1;
        private final static byte TAB_JUMP = 2;
        private final static byte TAB_JUMP_COL = 3;
        private final static byte TAB_HAT = 4;
	    private final static byte TAB_HAT_COL = 5;
	    private final static byte TAB_DRAGON_COL = 6;
	    private final static byte TAB_DRAGON_EYE = 7;
	    private final static byte TAB_DRAGON_NAME = 8;
        private final static byte TAB_DEFAULT = TAB_CLOTHES;
        private static byte currentTab = TAB_DEFAULT;
        private final static String DEF_CLOTHES = "None";
	    private final Avatar old;
        private final Avatar avt;
        private RadioGroup jmpRadio = null;
        private List<RadioGroup> jmpColors = null;
        private TouchButton jmpBtn = null;
        private List<RadioGroup> drgnColors = null;
        private TouchButton drgnBtn = null;
        private TouchButton drgnEyeBtn = null;
        private TouchButton drgnNameBtn = null;
        private final ClothingMenu clthMenu = new ClothingMenu();
        private final HatMenu hatMenu = new HatMenu();
        private Model clthModel = null;
        private PlayerContext mc = null;
        
        protected GearScreen(final PlayerContext pc, final Avatar old, final Avatar avt) {
            super(pc, false);
            radioLinesPerPage = isTabEnabled() ? 5 : 2;
            this.old = old;
            this.avt = avt;
            tabsSupported = true;
        }
        
        private final void reattach(final String info, final TouchButton sub, final Panmage img, final String txt) {
        	setInfo(info);
        	if (sub == null) {
        		return;
        	}
        	sub.setOverlay(img, offx(img), offy(img, txt));
        	sub.setText(PlatformGame.font, txt, OFF_TEXT_X, OFF_TEXT_Y);
        	TouchButton.reattach(sub);
        }
        
        private final void reattachBuy(final String info, final TouchButton sub) {
        	reattach(info, sub, PlatformGame.gem[0], "Buy");
        }
        
        private final static List<String> toNameList(final Named... a) {
        	return toNameList(null, a);
        }
        
        private final static List<String> toNameList(final String def, final Named... a) {
            final List<String> list = new ArrayList<String>(a.length + ((def == null) ? 0 : 1));
            Coltil.addIfValued(list, def);
            for (final Named n : a) {
                list.add(n.getName());
            }
            return list;
        }
        
        private final TouchButton newSub(final int x, final int y) {
            return Pangine.getEngine().isTouchSupported() ? newRadioSubmitButton(x, y) : null;
        }
        
        private final TouchButton newBuy(final int x, final int y) {
            final TouchButton sub = newSub(x, y);
            TouchButton.detach(sub);
            return sub;
        }
        
        private final TouchButton newTabSub(final int x, final int y, final Panmage img, final String txt, final byte tab) {
            final TouchButton sub = newSub(x, y + OFF_RADIO_Y);
            if (sub == null) {
                return null;
            }
            sub.getActor().register(sub, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    reload(tab);
                }});
            sub.setOverlay(img, offx(img), offy(img, txt));
        	sub.setText(PlatformGame.font, txt, OFF_TEXT_X, OFF_TEXT_Y);
            return sub;
        }
        
        private final TouchButton newColor(final int x, final int y, final byte tab) {
        	return newTabSub(x, y, PlatformGame.menuRgb, "Color", tab);
        }
        
        protected final void createJumpList(final int x, final int y) {
            final JumpMode[] jumpModes = JumpMode.values();
            final List<String> jmps = toNameList(jumpModes);
            final TouchButton sub = newBuy(x, y);
            jmpBtn = newColor(x, y, TAB_JUMP_COL);
            drgnBtn = newColor(x, y, TAB_DRAGON_COL);
            drgnEyeBtn = newTabSub(x + PlatformGame.MENU_W, y, PlatformGame.menuEyesDragon, "Eyes", TAB_DRAGON_EYE);
            drgnNameBtn = newTabSub(x, y - PlatformGame.MENU_H, PlatformGame.menuKeyboard, "Name", TAB_DRAGON_NAME);
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
            jmpRadio = addRadio("Power-up", jmps, jmpSubLsn, jmpLsn, x, y, sub);
            initJumpMode();
        }
        
        private final void createDragonEyeList(final int x, final int y) {
			createEyeList(avt.dragon, PlatformGame.getNumDragonEyes(), x, y);
		}
        
        private final void createDragonNameInput(final int x, final int y) {
			final Input namIn = addNameInput(avt.dragon, null, PlatformGame.MAX_NAME_AVATAR, x, y);
			namIn.append(avt.dragon.getName());
		}
        
        private final void addClothingModel() {
        	if (mc != null) {
        		return;
        	}
        	mc = generatePlayerContext(0);
        	final Avatar avt = mc.profile.currentAvatar;
        	avt.anm = "Bear";
        	avt.col.init();
        	avt.eye = 4;
        	init(avt.clothing);
        	init(avt.hat);
        	clthModel = addActor(mc, center - 32);
        	clthModel.setVisible(false);
        }
        
        private final void init(final Garb garb) {
        	final SimpleColor col = garb.col;
        	col.r = 0;
        	col.g = 0;
        	col.b = Avatar.DEF_JUMP_COL;
        }
        
        private abstract class GarbMenu {
        	protected final String name;
        	protected final Clothing[] all;
        	protected final byte tab;
        	protected RadioGroup radio = null;
        	protected List<RadioGroup> colors = null;
        	protected TouchButton btn = null;
        	
        	protected GarbMenu(final String name, final Clothing[] all, final byte tab) {
        		this.name = name;
        		this.all = all;
        		this.tab = tab;
        	}
            
        	public abstract Set<Clothing> getAvailable();
        	
        	public abstract Garb get(final Avatar avt);
        }
        
        private final class ClothingMenu extends GarbMenu {
        	protected ClothingMenu() {
        		super("Clothing", Avatar.clothings, TAB_CLOTHES_COL);
        	}
        	
        	@Override
        	public final Set<Clothing> getAvailable() {
        		return pc.profile.availableClothings;
        	}
        	
        	@Override
        	public final Garb get(final Avatar avt) {
        		return avt.clothing;
        	}
        }
        
        private final class HatMenu extends GarbMenu {
        	protected HatMenu() {
        		super("Headgear", Avatar.hats, TAB_HAT_COL);
        	}
        	
        	@Override
        	public final Set<Clothing> getAvailable() {
        		return pc.profile.availableHats;
        	}
        	
        	@Override
        	public final Garb get(final Avatar avt) {
        		return avt.hat;
        	}
        }
        
        protected final void createClothingList(final int x, final int y) {
        	createClothingList(x, y, clthMenu);
        }
        
        protected final void createHatList(final int x, final int y) {
        	createClothingList(x, y, hatMenu);
        }
        
        protected final void createClothingList(final int x, final int y, final GarbMenu menu) {
        	addClothingModel();
        	final Clothing[] clothings = menu.all;
            final List<String> clths = toNameList(DEF_CLOTHES, clothings);
            final TouchButton sub = newBuy(x, y);
            menu.btn = newColor(x, y, menu.tab);
            final AvtListener clthLsn = new AvtListener() {
                @Override public final void update(final String value) {
                    final Clothing c = Player.get(clothings, value);
                    if (Profile.isAvailable(menu.getAvailable(), c)) {
                    	clthModel.setVisible(false);
                        clearInfo();
                        TouchButton.detach(sub);
                        setClothing(menu, c);
                    } else {
                    	final Avatar avt = mc.profile.currentAvatar;
                    	avt.clothing.clth = null;
                    	avt.hat.clth = null;
                    	menu.get(avt).clth = c;
                    	reloadAnimalStrip(mc, clthModel);
                    	clthModel.setVisible(true);
                        reattachBuy("Buy for " + c.getCost() + "?", sub);
                    }
                }};
            final RadioSubmitListener clthSubLsn = new AvtListener() {
                @Override public final void update(final String value) {
                    final Clothing c = Player.get(clothings, value);
                    if (!Profile.isAvailable(menu.getAvailable(), c)) {
                        final int cost = c.getCost();
                        if (pc.profile.spendGems(cost)) {
                        	clthModel.setVisible(false);
                        	menu.getAvailable().add(c);
                            setClothing(menu, c);
                            setInfo("Purchased!");
                            TouchButton.detach(sub);
                        } else {
                            setInfo("You need more Gems");
                            TouchButton.detach(sub);
                        }
                    }
                }};
            menu.radio = addRadio(menu.name, clths, clthSubLsn, clthLsn, x, y, sub);
            initClothing(menu);
        }
        
        @Override
		protected final void menu() {
			if (isTabEnabled()) {
				menuTouch();
			} else {
				menuClassic();
			}
			initJumpColors();
            initClothingColors(clthMenu);
            initClothingColors(hatMenu);
		}
		
		protected final void menuTouch() {
		    switch (currentTab) {
                case TAB_CLOTHES :
                    createClothingList(touchRadioX, touchRadioY);
                    break;
                case TAB_CLOTHES_COL :
                    addColor(avt.clothing.col, 0, 0, "Clothing");
                    break;
                case TAB_HAT :
                    createHatList(touchRadioX, touchRadioY);
                    break;
                case TAB_HAT_COL :
                    addColor(avt.hat.col, 0, 0, "Hat");
                    break;
                case TAB_JUMP :
                    createJumpList(touchRadioX, touchRadioY);
                    break;
                case TAB_JUMP_COL :
                    addColor(avt.jumpCol, 0, 0, "Wing");
                    break;
                case TAB_DRAGON_COL :
                    addColor(avt.dragon.col, 0, 0, "Dragon");
                    break;
                case TAB_DRAGON_EYE :
                	createDragonEyeList(touchRadioX, touchRadioY);
                    break;
                case TAB_DRAGON_NAME :
                	createDragonNameInput(touchKeyboardX, getTouchKeyboardY());
                    break;
            }
			newTab(PlatformGame.menuCheck, "Back", new Runnable() {@Override public final void run() {exit();}});
			newTab(PlatformGame.menuClothing, "Shirt", TAB_CLOTHES);
			newTab(PlatformGame.menuHat, "Hat", TAB_HAT);
			newTab(PlatformGame.menuJump, "Power", TAB_JUMP);
			newTabs();
			registerBackExit();
		}
		
        private final void newTab(final Panmage img, final CharSequence txt, final byte tab) {
            final TouchButton btn = newTab(img, txt, new Runnable() {@Override public final void run() {reload(tab);}});
            if (currentTab == tab) {
                btn.setEnabled(false);
            }
        }
        
        private void reload(final byte tab) {
            currentTab = tab;
            PlatformGame.setScreen(new GearScreen(pc, old, avt));
        }
        
        protected final void menuClassic() {
            final int left = getLeft();
            int y = getTop();
            createJumpList(left, y);
            jmpColors = addColor(avt.jumpCol, left + 88, y);
            drgnColors = addColor(avt.dragon.col, left + 88, y);
            final int yoff = 24 + (8 * radioLinesPerPage);
            y -= yoff;
            //TODO Name, eyes
            y -= 16;
            createClothingList(left, y);
            clthMenu.colors = addColor(avt.clothing.col, left + 88, y);
            y -= yoff;
            createHatList(left, y);
            hatMenu.colors = addColor(avt.hat.col, left + 88, y);
            y -= yoff;
            addExit("Back", left, y);
        }
        
        private final void setJumpMode(final byte index) {
        	avt.jumpMode = index;
        	initJumpColors();
        }
        
        private final void initColors(final List<RadioGroup> colors, final TouchButton btn, final boolean vis) {
        	for (final RadioGroup jmpColor : Coltil.unnull(colors)) {
        		jmpColor.setVisible(vis);
        	}
        	TouchButton.reattach(btn, vis);
        }
        
        private final void initJumpColors() {
        	initColors(jmpColors, jmpBtn, avt.jumpMode == Player.JUMP_FLY);
        	final boolean needDragon = avt.jumpMode == Player.JUMP_DRAGON;
        	initColors(drgnColors, drgnBtn, needDragon);
        	TouchButton.reattach(drgnEyeBtn, needDragon);
        	TouchButton.reattach(drgnNameBtn, needDragon);
        }
        
        private final void initJumpMode() {
            jmpRadio.setSelected(JumpMode.get(avt.jumpMode).getName());
        }
        
        private final void setClothing(final GarbMenu menu, final Clothing c) {
            menu.get(avt).clth = c;
            initClothingColors(menu);
        }
        
        private final void initClothingColors(final GarbMenu menu) {
        	initColors(menu.colors, menu.btn, menu.get(avt).clth != null);
        }
        
        private final void initClothing(final GarbMenu menu) {
        	final Clothing clothing = menu.get(avt).clth;
            menu.radio.setSelected((clothing == null) ? DEF_CLOTHES : clothing.getName());
            clthModel.setVisible(false);
        }
        
        @Override
        protected boolean allow(final TextItem focused) {
            final boolean a = super.allow(focused);
            if (a) {
                initJumpMode();
                initClothing(clthMenu);
                initClothing(hatMenu);
            }
            return a;
        }
        
        @Override
        protected void onExit() {
        	Coltil.clear(jmpColors);
        	Coltil.clear(drgnColors);
        	Coltil.clear(clthMenu.colors);
        	Coltil.clear(hatMenu.colors);
            PlatformGame.setScreen(new AvatarScreen(pc, old, avt));
        }
	}
	
	protected final static class AssistScreen extends PlayerScreen {
		private List<StringBuilder> as = null;
        
        protected AssistScreen(final PlayerContext pc) {
            super(pc, false);
        }
        
        @Override
        protected final void menu() throws Exception {
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
		private final static byte TAB_FOES = 3;
		protected static byte currentTab = TAB_AWARD;
	    private RadioGroup achRadio = null;
	    private final StringBuilder achDesc = new StringBuilder();
	    private final StringBuilder rankDesc = new StringBuilder();
	    private final List<Panctor> goalStars = new ArrayList<Panctor>(3);
	    private final List<Panctor> rankStars = new ArrayList<Panctor>(Profile.POINTS_PER_RANK);
	    private RadioGroup enemyRadio = null;
	    private final StringBuilder enemyDesc = new StringBuilder();
	    private Panctor enemy = null;
	    final boolean fullMenu;
	    
        protected InfoScreen(final PlayerContext pc, final boolean fullMenu) {
            super(pc, false);
            tabsSupported = true;
            this.fullMenu = fullMenu;
            showGems = currentTab != TAB_FOES;
        }

        @Override
		protected final void menu() {
        	if (Goal.isAnyMet(pc)) {
        		createGoalMet();
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
					addHudAchievement();
					break;
				case TAB_STATS :
					createStatsList(touchRadioX, touchRadioY);
					break;
				case TAB_GOALS :
					createGoalsList(rankStarX, (Pangine.getEngine().getEffectiveHeight() - 124) / 2 + 116);
					break;
				case TAB_FOES :
				    createFoesList(touchRadioX, touchRadioY);
				    break;
			}
			newTab(PlatformGame.menuCheck, "Done", new Runnable() {@Override public final void run() {exit();}});
			if (fullMenu) {
				newTab(PlatformGame.menuTrophy, "Award", TAB_AWARD);
				newTab(PlatformGame.menuGraph, "Stats", TAB_STATS);
				newTab(PlatformGame.menuStar, "Goals", TAB_GOALS);
				newTab(PlatformGame.menuFoes, "Foes", TAB_FOES);
			}
			newTabs();
			registerBackExit();
		}
		
		//TODO newTab/reload almost same as AvatarScreen/GearScreen
		private final void newTab(final Panmage img, final CharSequence txt, final byte tab) {
			final TouchButton btn = newTab(img, txt, new Runnable() {@Override public final void run() {reload(tab);}});
			if (currentTab == tab) {
				btn.setEnabled(false);
			}
		}
		
		private void reload(final byte tab) {
			currentTab = tab;
			PlatformGame.setScreen(new InfoScreen(pc, fullMenu));
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
		
		private final void createFoesList(final int x, final int y) {
		    final List<String> list = new ArrayList<String>();
		    for (final EnemyDefinition def : PlatformGame.allEnemies) {
		        list.add(def.getName());
		    }
		    enemy = addActor(center + 80, Y_PLAYER);
		    enemy.setMirror(true);
		    addTitle(enemyDesc, x + OFF_RADIO_LIST, y - 72);
		    final RadioSubmitListener foeLsn = new RadioSubmitListener() {
                @Override public final void onSubmit(final RadioSubmitEvent event) {
                    setEnemy(event.toString());
            }};
            enemyRadio = addRadio("Bestiary", list, foeLsn, x, y);
            initEnemy();
        }
		
		private final void createGoalsList(final int x, int y) {
			addTitle("Goals", x, y);
			y -= 16;
			showNew = true;
			y = displayGoals(x, y, goalStars);
			showNew = false;
			addRankPoints(x, y, isTabEnabled());
			for (final Goal goal : pc.profile.currentGoals) {
				goal.brandNew = false;
			}
		}
		
		private final int addRankPoints(final int x, int y, final boolean img) {
			final Profile prf = pc.profile;
			y -= 8;
			Chartil.set(rankDesc, "Rank " + prf.getRank());
			addTitle(rankDesc, img ? (x + 17) : x, y);
			final int currPoints = prf.getCurrentGoalPoints();
			if (img) {
				addActor(new Gem(PlatformGame.gemRank), x, y);
				y -= 17;
				addStars(x, y, currPoints, Profile.POINTS_PER_RANK, rankStars);
			} else {
				y -= 8;
				final StringBuilder b = new StringBuilder();
				Chartil.appendMulti(b, '*', currPoints);
				Chartil.appendMulti(b, '.', Profile.POINTS_PER_RANK - currPoints);
				addTitle(b, x, y);
			}
			return y;
		}
		
		private final void createGoalMet() {
			final Pangine engine = Pangine.getEngine();
			final int x = rankStarX;
			final boolean tab = isTabEnabled();
			final int h = tab ? 74 : 90;
			int y = (engine.getEffectiveHeight() - h) / 2 + h - 8;
			initForm = false;
			final Pantext success = addTitle("Success!", x, y);
			y -= 16;
			final Profile prf = pc.profile;
			final Goal[] goals = prf.currentGoals;
			for (int i = 0; i < Goal.NUM_ACTIVE_GOALS; i++) {
				final Goal g = goals[i];
				if (g != null && g.isMet(pc)) {
					// If next Goal is a RunGoal, it could be met based on last level, so bypass it
					if (pc.player != null) {
						pc.player.goalsMet[i] = true;
					}
					y = addGoal(g, x, y, true, goalStars);
					y = addRankPoints(x, y, true);
					addGoalPoints(i, x, y);
					break;
				}
			}
			if (tab) {
				registerBackPromptQuit(success);
			}
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
			form.init();
		}
		
		private final void addGoalTimer(final TimerListener listener) {
			addGoalTimer(30, listener);
		}
		
		private final void addGoalTimer(final long duration, final TimerListener listener) {
			Pangine.getEngine().addTimer(tm, duration, listener);
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
									spark(goalStar.getPosition());
									goalStar.swapPositions(rankStar);
									goalStars.set(i, rankStar);
									rankStars.set(j, goalStar);
									break;
								}
							}
							if (newRank > rank) {
								// Rank up
								addGoalTimer(new TimerListener() {
									@Override public final void onTimer(final TimerEvent event) {
										pc.addGems(1000);
										final String strRank = String.valueOf(newRank);
										Chartil.set(rankDesc, "New Rank " + strRank + "   1000");
										addActor(new Gem(), x + 1 + (12 + strRank.length()) * 8, y + 17);
										addRankPoints(goalIndex, x, y);
										PlatformGame.musicLevelEnd.startSound();
									}});
							} else {
								// Add remaining points (if any)
								addGoalPoints(goalIndex, x, y);
							}
							return;
						}
					}
					// No more points to add, so finish
					final Goal goals[] = prf.currentGoals, goal = Goal.newGoal(goals[goalIndex].award, pc);
					goal.brandNew = true;
					goals[goalIndex] = goal;
					save();
					addContinue(x, y);
				}});
		}
		
		private final void addRankPoints(final int goalIndex, final int x, final int y) {
			addGoalTimer(15, new TimerListener() {
				@Override public final void onTimer(final TimerEvent event) {
					for (int j = Profile.POINTS_PER_RANK - 1; j >= 0; j--) {
						final Panctor rankStar = rankStars.get(j);
						if (rankStar.getClass() == Gem.class) {
							final Panple pos = rankStar.getPosition();
							spark(pos);
							rankStars.set(j, addEmptyStar((int) pos.getX(), (int) pos.getY()));
							rankStar.destroy();
							addRankPoints(goalIndex, x, y);
							return;
						}
					}
					addGoalPoints(goalIndex, x, y);
				}});
		}
		
		private final static void spark(final Panple pos) {
			Gem.spark(pos, false);
			PlatformGame.soundGem.startSound();
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
                newDesc = ach.getDescription() + " (" + ach.getAward() + ")";
            }
            Chartil.set(achDesc, newDesc);
        }
        
        private final void initAchDesc() {
            setAchDesc((String) achRadio.getSelected());
        }
        
        private final void setEnemy(final String name) {
        	final EnemyDefinition def = PlatformGame.getEnemy(name);
            enemy.setView(def.walk.getFrames()[0].getImage());
            Chartil.set(enemyDesc, "Defeated " + pc.profile.stats.defeatedEnemyTypes.longValue(def.code));
        }
        
        private final void initEnemy() {
            setEnemy((String) enemyRadio.getSelected());
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
	
	protected abstract static class BaseOptionsScreen extends PlayerScreen {
		protected BaseOptionsScreen(final PlayerContext pc) {
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
        
        protected abstract void menuTouch();
        
        protected final void menuClassic() {
        }
        
        @Override
        protected final boolean isPlayerDisplayed() {
			return Pangine.getEngine().getEffectiveHeight() > 204;
		}
	}
	
	protected final static class OptionsScreen extends BaseOptionsScreen {
		private final StringBuilder msgAuto = new StringBuilder();
		private final StringBuilder msgSpeed = new StringBuilder();
		private final StringBuilder msgBtnSize = new StringBuilder();
		private final int oldBtnSize = Config.btnSize;
		
		protected OptionsScreen(final PlayerContext pc) {
            super(pc);
        }
	    
		@Override
        protected final void menuTouch() {
            final Pangine engine = Pangine.getEngine();
            final Panple btnSize = PlatformGame.menu.getSize();
            final int h = engine.getEffectiveHeight();
            final int btnW = (int) btnSize.getX(), btnH = (int) btnSize.getY();
            final int offY = (h >= 240) ? (btnH * 5 / 4) : btnH;
            int x = btnW / 2, y = h - btnH - offY;
            
            newFormButton("AutoToggle", x, y, PlatformGame.menuButtons, new Runnable() {@Override public final void run() {toggleAuto();}});
            addTitle(msgAuto, x + btnW + 8, y);
            setMessageAuto();
            
            y -= offY;
            newFormButton("SpeedDown", x, y, PlatformGame.menuLeft, new Runnable() {@Override public final void run() {incSpeed(-1);}});
            newFormButton("SpeedUp", engine.getEffectiveWidth() - x - btnW, y, PlatformGame.menuRight, new Runnable() {@Override public final void run() {incSpeed(1);}});
            setMessageSpeed();
            addTitle(msgSpeed, x + btnW + 8, y);
            
            y -= offY;
            newFormButton("BtnSizeDown", x, y, PlatformGame.menuLeft, new Runnable() {@Override public final void run() {incBtnSize(-1);}});
            newFormButton("BtnSizeUp", engine.getEffectiveWidth() - x - btnW, y, PlatformGame.menuRight, new Runnable() {@Override public final void run() {incBtnSize(1);}});
            setMessageBtnSize();
            addTitle(msgBtnSize, x + btnW + 8, y);
            
            newTab(PlatformGame.menuCheck, "Done", new Runnable() {@Override public final void run() {exit();}});
            newTab(PlatformGame.menuMusic, "Music", new Runnable() {@Override public final void run() {goMusic();}});
            if (pc.profile.consoleEnabled) {
            	newTab(PlatformGame.menuKeyboard, "Debug", new Runnable() {@Override public final void run() {goConsole();}});
            }
            newTabs();
            registerBackExit();
        }
        
        private final void goMusic() {
            PlatformGame.setScreen(new MusicScreen(pc));
        }
        
        private final void goConsole() {
            PlatformGame.setScreen(new ConsoleScreen(pc));
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
        
        private final void incBtnSize(final int dir) {
            Config.btnSize += dir;
            if (Config.btnSize > Config.MAX_BUTTON_SIZE) {
                Config.btnSize = Config.MIN_BUTTON_SIZE;
            } else if (Config.btnSize < Config.MIN_BUTTON_SIZE) {
                Config.btnSize = Config.MAX_BUTTON_SIZE;
            }
            setMessageBtnSize();
        }
        
        private final void setMessageBtnSize() {
            final String s;
            switch (Config.btnSize) {
                case -2 :
                    s = "Smallest";
                    break;
                case -1 :
                    s = "Small";
                    break;
                case 0 :
                    s = "Medium";
                    break;
                case 1 :
                    s = "Large";
                    break;
                case 2 :
                    s = "Largest";
                    break;
                default :
                    throw new IllegalArgumentException("Unrecognized button size: " + Config.btnSize);
            }
            Chartil.set(msgBtnSize, "Button Size: " + s);
        }
        
        @Override
        protected void onExit() {
            if (oldBtnSize != Config.btnSize) {
                PlatformGame.reloadButtons();
                Config.serialize();
            }
        	save();
            goProfile();
        }
	}
	
	protected final static class MusicScreen extends BaseOptionsScreen {
		private final StringBuilder msgMusic = new StringBuilder();
		private final StringBuilder msgSound = new StringBuilder();
		private final boolean oldMusic = Config.musicEnabled;
		private final boolean oldSound = Config.soundEnabled;
	    
        protected MusicScreen(final PlayerContext pc) {
            super(pc);
        }
        
        @Override
        protected final void menuTouch() {
            final Pangine engine = Pangine.getEngine();
            final Panple btnSize = PlatformGame.menu.getSize();
            final int btnW = (int) btnSize.getX(), btnH = (int) btnSize.getY(), offY = btnH * 5 / 4;
            int x = btnW / 2, y = engine.getEffectiveHeight() - btnH - offY;
            
            newFormButton("MusicToggle", x, y, PlatformGame.menuMusic, new Runnable() {@Override public final void run() {toggleMusic();}});
            addTitle(msgMusic, x + btnW + 8, y);
            setMessageMusic();
            
            y -= offY;
            newFormButton("SoundToggle", x, y, PlatformGame.menuSound, new Runnable() {@Override public final void run() {toggleSound();}});
            addTitle(msgSound, x + btnW + 8, y);
            setMessageSound();
            
            newTab(PlatformGame.menuCheck, "Done", new Runnable() {@Override public final void run() {exit();}});
            newTabs();
            registerBackExit();
        }
        
        private final void toggleMusic() {
        	Config.setMusicEnabled(!Config.musicEnabled);
        	setMessageMusic();
        }
        
        private final void setMessageMusic() {
        	final String s;
        	if (Config.musicEnabled) {
        		s = "Music is on";
        	} else {
        		s = "Music is off";
        	}
        	Chartil.set(msgMusic, s);
        }
        
        private final void toggleSound() {
        	Config.setSoundEnabled(!Config.soundEnabled);
        	setMessageSound();
        }
        
        private final void setMessageSound() {
        	final String s;
        	if (Config.soundEnabled) {
        		s = "Sound is on";
        	} else {
        		s = "Sound is off";
        	}
        	Chartil.set(msgSound, s);
        }
        
        @Override
        protected void onExit() {
            if (oldMusic != Config.musicEnabled || oldSound != Config.soundEnabled) {
                Config.serialize();
            }
            goOptions();
        }
	}
	
	protected final static class ConsoleScreen extends PlayerScreen {
		private Input input = null;
		private final StringBuffer info = new StringBuffer();
		
		protected ConsoleScreen(final PlayerContext pc) {
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
			createInput(touchKeyboardX, getTouchKeyboardY());
			newTab(PlatformGame.menuCheck, "Done", new Runnable() {@Override public final void run() {exit();}});
			newTab(PlatformGame.menuExclaim, "Run", new Runnable() {@Override public final void run() {exec();}});
			newTab(PlatformGame.menuMinus, "Clear", new Runnable() {@Override public final void run() {clear();}});
			newTabs();
			registerBackExit();
		}
		
		protected final void menuClassic() {
			throw new UnsupportedOperationException();
		}
		
		private final void createInput(final int x, final int y) {
			final InputSubmitListener subLsn = new InputSubmitListener() {
                @Override public final void onSubmit(final InputSubmitEvent event) {
                	exec(); }};
	        input = addInput(">", subLsn, null, MAX_COMMAND, x, y);
	        addTitle(info, 8, HUD_TEXT_Y);
	    }
		
		private final static int MAX_COMMAND = 16;
		private final static String MSG_OK = "OK";
		private final static String MSG_RESTART = "OK, need restart";
		private final static String MSG_LIMIT = "Error, limit";
		private final static String MSG_WAIT = "Waiting";
		private final static String MSG_SKIP = "og.pandcorps.skip";
		
		private interface ExecHandler {
			public String run(final String cmd);
		}
		
		private ExecHandler execHandler = null;
		
		private final void exec() {
			//Chartil.clear(info);
			final String cmd = input.getText(), msg;
			if (execHandler != null) {
				final ExecHandler eh = execHandler;
				execHandler = null;
				msg = eh.run(cmd);
			} else if ("addgems".equalsIgnoreCase(cmd)) {
				pc.addGems(1000);
				msg = "Added 1000 Gems";
			} else if ("getzoom".equalsIgnoreCase(cmd)) {
				if (Config.zoomMag <= 0) {
					msg = "Default (" + PlatformGame.getApproximateFullScreenZoomedDisplaySize() + ")";
				} else  {
					msg = "Zoom: " + Config.zoomMag;
				}
			} else if ("zoomin".equalsIgnoreCase(cmd)) {
				if (Config.zoomMag > 0 && Config.zoomMag < PlatformGame.getApproximateFullScreenZoomedDisplaySize()) {
					msg = setZoom(Config.zoomMag + 1);
				} else {
					msg = MSG_LIMIT;
				}
			} else if ("zoomout".equalsIgnoreCase(cmd)) {
				int z = Config.zoomMag;
				if (z < 0) {
					z = PlatformGame.getApproximateFullScreenZoomedDisplaySize();
				}
				if (z > 1) {
					msg = setZoom(z - 1);
				} else {
					msg = MSG_LIMIT;
				}
			} else if ("zoomdef".equalsIgnoreCase(cmd)) {
				msg = setZoom(-1);
			} else if ("addclothes".equalsIgnoreCase(cmd)) {
			    boolean added = false;
			    for (final Clothing c : Avatar.clothings) {
			        if (pc.profile.availableClothings.add(c)) {
			            added = true;
			        }
			    }
			    msg = added ? MSG_OK : MSG_LIMIT;
			} else if ("addhats".equalsIgnoreCase(cmd)) {
			    boolean added = false;
			    for (final Clothing c : Avatar.hats) {
			        if (pc.profile.availableHats.add(c)) {
			            added = true;
			        }
			    }
			    msg = added ? MSG_OK : MSG_LIMIT;
			} else if ("addjumps".equalsIgnoreCase(cmd)) {
			    boolean added = false;
                for (final JumpMode jm : JumpMode.values()) {
                    if (pc.profile.availableJumpModes.add(Integer.valueOf(jm.getIndex()))) {
                        added = true;
                    }
                }
                msg = added ? MSG_OK : MSG_LIMIT;
			} else if ("addwings".equalsIgnoreCase(cmd)) {
			    msg = pc.profile.availableJumpModes.add(Integer.valueOf(Player.JUMP_FLY)) ? MSG_OK : MSG_LIMIT;
			} else if ("addarmor".equalsIgnoreCase(cmd)) {
				msg = pc.profile.availableClothings.add(Avatar.getClothing("Armor")) ? MSG_OK : MSG_LIMIT;
			} else if ("setmapnorm".equalsIgnoreCase(cmd)) {
				Map.modeMove = Map.MOVE_NORMAL;
				msg = MSG_OK;
			} else if ("setmapfree".equalsIgnoreCase(cmd)) {
				Map.modeMove = Map.MOVE_ANY_PATH;
				msg = MSG_OK;
			} else if ("setmaptile".equalsIgnoreCase(cmd)) {
				Map.modeMove = Map.MOVE_ANY_TILE;
				msg = MSG_OK;
			} else if ("setsnow".equalsIgnoreCase(cmd)) {
				msg = setMapTheme(MapTheme.Snow);
			} else if ("setsand".equalsIgnoreCase(cmd)) {
				msg = setMapTheme(MapTheme.Sand);
			} else if ("setgrass".equalsIgnoreCase(cmd)) {
				msg = setMapTheme(MapTheme.Normal);
			} else if ("noconsole".equalsIgnoreCase(cmd)) {
				pc.profile.consoleEnabled = false;
				save();
				msg = MSG_OK;
			} else if ("addmusic".equalsIgnoreCase(cmd)) {
				Config.setMusicEnabled(true);
				Config.serialize();
				msg = MSG_OK;
			} else if ("nomusic".equalsIgnoreCase(cmd)) {
				Config.setMusicEnabled(false);
				Config.serialize();
				msg = MSG_OK;
			} else if ("addsound".equalsIgnoreCase(cmd)) {
				Config.setSoundEnabled(true);
				Config.serialize();
				msg = MSG_OK;
			} else if ("nosound".equalsIgnoreCase(cmd)) {
				Config.setSoundEnabled(false);
				Config.serialize();
				msg = MSG_OK;
			} else if ("export".equalsIgnoreCase(cmd)) {
				Pangine.getEngine().setClipboard(Savtil.toString(pc.profile));
				msg = MSG_OK;
			} else if ("import".equalsIgnoreCase(cmd)) {
				setMsg(MSG_WAIT);
				final Pangine engine = Pangine.getEngine();
				engine.getClipboard(new Handler<String>() {
					@Override
					public void handle(final String prf) {
						final String msg;
						if (Chartil.isEmpty(prf)) {
							msg = "Missing";
						} else if (!prf.startsWith(PlatformGame.SEG_PRF)) {
							msg = "Invalid";
						} else {
						    final Profile tprf = new Profile();
						    try {
						        tprf.load(SegmentStream.openString(prf).readRequire(PlatformGame.SEG_PRF));
						    } catch (final IOException e) {
						        throw new RuntimeException(e);
						    }
						    final String importedName = tprf.getName();
							Iotil.writeFile(Profile.getFileName(importedName), prf);
							Iotil.delete(Profile.getMapFileName(importedName));
							if (importedName.equals(pc.profile.getName())) {
							    engine.exit();
							}
							msg = MSG_OK;
						}
						setMsg(msg);
					}});
				msg = MSG_SKIP;
			} else if ("delete".equalsIgnoreCase(cmd)) {
				execHandler = new ExecHandler() {
					@Override public final String run(final String cmd) {
						if (cmd.equalsIgnoreCase(pc.profile.getName())) {
							return "Cannot delete current";
						}
						// Be careful if combining these calls into a boolean expression; still run 2nd if 1st returns true
						final StringBuilder msg = new StringBuilder();
						msg.append("Prf: ");
						msg.append(Iotil.delete(Profile.getFileName(cmd)) ? MSG_OK : "No");
						msg.append("; Map: ");
						msg.append(Iotil.delete(Profile.getMapFileName(cmd)) ? MSG_OK : "No");
						return msg.toString();
					}};
				msg = "Which?";
			} else if ("save".equalsIgnoreCase(cmd)) {
			    save();
			    msg = MSG_OK;
			} else {
				msg = "Unknown command";
			}
			if (!MSG_SKIP.equals(msg)) {
				setMsg(msg);
			}
		}
		
		private final void setMsg(final String msg) {
			Chartil.set(info, msg);
		}
		
		private final static String setZoom(final int zoomMag) {
			Config.zoomMag = zoomMag;
			Config.serialize();
			return MSG_RESTART;
		}
		
		private final static String setMapTheme(final MapTheme theme) {
			Map.theme = theme;
			Map.saveMap();
			return MSG_RESTART;
		}
		
		private final void clear() {
			input.clear();
		}
		
		@Override
        protected void onExit() {
			goOptions();
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
