/*
Copyright (c) 2009-2016, Andrew M. Martin
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
package org.pandcorps.furguardians;

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
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Input.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.touch.*;
import org.pandcorps.furguardians.Map.*;
import org.pandcorps.furguardians.Enemy.*;
import org.pandcorps.furguardians.Profile.*;
import org.pandcorps.furguardians.Avatar.*;
import org.pandcorps.furguardians.Player.*;

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
    private final static String LABEL_COLOR = "Main";
    private final static String LABEL_COLOR2 = "Other";
    protected final static int Y_PLAYER = 16;
    private final static char CHAR_ON = 2;
    protected final static String NEW_AVATAR_NAME = "New";
    private static boolean newProfile = false;
    private static int radioLinesPerPage = 5;
    
	protected abstract static class PlayerScreen extends Panscreen {
		protected Panlayer room;
		protected PlayerContext pc;
		protected ControlScheme ctrl = null;
		private final boolean fadeIn;
		protected final StringBuilder inf = new StringBuilder();
		private final StringBuilder desc = new StringBuilder();
		private final StringBuilder desc2 = new StringBuilder();
		private final StringBuilder desc3 = new StringBuilder();
		protected TileMap tm = null;
		protected Panmage timg = null;
		protected Model actor = null;
		protected Pantext infLbl = null;
		protected Pantext descLbl = null;
		protected Pantext desc2Lbl = null;
		protected Pantext desc3Lbl = null;
		protected boolean disabled = false;
		protected Panform form = null;
		protected int center = -1;
		protected final List<TouchButton> tabs;
		protected boolean tabsSupported = false;
		protected static int touchRadioX = 40;
		protected static int touchRadioY = 140;
		protected final static int touchKeyboardX = 8;
		protected static int OFF_RADIO_LIST = 100;
		protected static int OFF_RADIO_Y = 100;
		protected final int rankStarX;
		protected boolean initForm = true;
		protected boolean showGems = true;
		protected boolean showRank = true;
		protected Model clthModel = null;
		protected PlayerContext mc = null;
		
		protected PlayerScreen(final PlayerContext pc, final boolean fadeIn) {
			radioLinesPerPage = 5;
			this.pc = pc;
			this.fadeIn = fadeIn;
			tabs = isTabEnabled() ? new ArrayList<TouchButton>() : null;
			rankStarX = getRankStarX();
			if (touchRadioY == 140) {
			    final Pangine engine = Pangine.getEngine();
				final int h = engine.getEffectiveTop();
				// If h is 192, OFF_RADIO_Y should be 72; should increase with h, up to 100
				OFF_RADIO_Y = Math.min(h - 120, 100);
				final int menuHeight = OFF_RADIO_Y + FurGuardiansGame.MENU_H; // 112
				final int menuBottom = (h - menuHeight) / 2; // 40
				touchRadioY = menuBottom + OFF_RADIO_Y; // 112
				// If w is 344 or less, touchRadioX should be 0; should increase with w, up to 40
				touchRadioX = Math.min(Math.max(engine.getEffectiveWidth() - 344, 0), 40);
				// If w is 320 or less, OFF_RADIO_LIST should be 8 + MENU_W; should increase with w, up to 100
				OFF_RADIO_LIST = Math.min(Math.max(engine.getEffectiveWidth() - 320, 0) + 8 + FurGuardiansGame.MENU_W, 100);
			}
		}
		
		protected final static int getRankStarX() {
			return (Pangine.getEngine().getEffectiveWidth() - 170) / 2;
		}
		
		protected boolean isCursorDisplayed() {
			return true;
		}
		
		protected boolean isPlayerDisplayed() {
			return true;
		}
		
		protected final static Cursor addCursor(final Panlayer room) {
			final Cursor cursor = Cursor.addCursor(room, FurGuardiansGame.menuCursor);
			if (cursor != null) {
				FurGuardiansGame.setDepth(cursor, 20);
			}
			return cursor;
		}
		
		@Override
		protected final void load() throws Exception {
			final int w = FurGuardiansGame.SCREEN_W;
			center = w / 2;
			room = FurGuardiansGame.createRoom(w, FurGuardiansGame.SCREEN_H);
			final Pangine engine = Pangine.getEngine();
			final Pancolor bgColor = new FinPancolor((short) 128, (short) 192, Pancolor.MAX_VALUE);
			final MapTheme theme = Map.theme;
			engine.setBgColor(PixelFilter.filterColor(theme.getSkyFilter(), bgColor));
			Level.initTheme();
			
			tm = new TileMap(Pantil.vmid(), room, ImtilX.DIM, ImtilX.DIM);
			Level.tm = tm;
			timg = Level.getTileImage();
			final TileMapImage[][] imgMap = tm.splitImageMap(timg);
			tm.fillBackground(imgMap[theme.getMenuTileRow()][theme.getMenuTileColumn()], 0, 1);
			room.addActor(tm);
			
			if (isCursorDisplayed()) {
				addCursor(room);
			}
			
			if (pc != null) {
				if (isPlayerDisplayed()) {
					actor = addActor(pc, center);
				}
			    ctrl = pc.ctrl;
			}
			final boolean tabs = tabsSupported && isTabEnabled();
			if (tabs) {
				engine.clearTouchButtons();
				engine.getInteraction().unregisterAll();
			} else {
				initTouchButtons(room, ctrl);
			}
			form = new Panform(ctrl);
			infLbl = addTitle(inf, center, getBottom());
			infLbl.getPosition().addZ(2);
			if (!tabs) {
    			form.setTabListener(new FormTabListener() {@Override public void onTab(final FormTabEvent event) {
    				if (allow(event.getFocused())) {
    					clearInfo();
    				} else {
    					event.cancel();
    				}
    			}});
			}
			if (showGems && pc != null && pc.profile != null) {
				addHudGems();
				if (showRank) {
					addHudRank();
				}
			}
			menu();
			if (initForm && ctrl != null) { // Null on TitleScreen
				form.init();
			}
			addTitleTiny(FurGuardiansGame.VERSION, 9, 8);
			
			if (fadeIn) {
			    FurGuardiansGame.fadeIn(room, SPEED_MENU_FADE);
			}
			FurGuardiansGame.playMenuMusic();
		}
		
		protected final int getTop() {
			return (Pangine.getEngine().getEffectiveTop() / 2) + 87;
		}
		
		protected final int getBottom() {
			return 56;
		}
		
		protected final int getLeft() {
			return (Pangine.getEngine().getEffectiveWidth() / 2) - 120;
		}
		
		protected final int getTouchKeyboardY() {
			return (int) (Pangine.getEngine().getEffectiveTop() - FurGuardiansGame.menu.getSize().getY() - 16);
		}
		
		protected final static void initTouchButtons(final Panlayer room, final ControlScheme ctrl) {
			initTouchButtons(room, ctrl, TOUCH_FULL, true, true, null);
		}
		
		protected final static int getTouchButtonRadius() {
			return (FurGuardiansGame.DIM_BUTTON / 2) + 1;
		}
		
		protected final static void initTouchButtons(final Panlayer room, final ControlScheme ctrl,
				final byte mode, final boolean input, final boolean act, final Panctor bound) {
			//info("initTouch for " + getClass().getName());
			if (ctrl == null) {
				return;
			}
			//info("Found ControlScheme");
			if (!(ctrl.getDevice() instanceof Touchscreen)) {
        		return;
        	}
			//info("Found touch scheme");
			final Pangine engine = Pangine.getEngine();
			if (input) {
				engine.clearTouchButtons();
			}
			final int r = engine.getEffectiveWidth(), t = engine.getEffectiveTop();
			int rx = 0, y = 0;
			TouchButton down = null, up = null, act2 = null;
			Panmage rt = FurGuardiansGame.right2, rtIn = FurGuardiansGame.right2In, lt = FurGuardiansGame.left2, ltIn = FurGuardiansGame.left2In;
			final boolean full = mode == TOUCH_FULL;
			if (full) {
			    final int rad = getTouchButtonRadius(), dmtr = rad * 2;
				y = rad;
				down = addDiamondButton(room, "Down", rad, 0, input, act, ctrl.getOriginalDown());
				up = addDiamondButton(room, "Up", rad, dmtr, input, act, ctrl.getOriginalUp());
				rx = dmtr;
				//act2 = addCircleButton(room, "Act2", r - d, 0, input, act, ctrl.getOriginal2());
				//sub = addCircleButton(room, "Sub", r - d, engine.getEffectiveTop() - d, input, act, ctrl.getOriginalSubmit());
				final Panple ts = FurGuardiansGame.menu.getSize();
				final int tw = (int) ts.getX();
				act2 = newFormButton(room, "Act2", r - tw, t - (int) ts.getY(), FurGuardiansGame.menuOptions, "Menu");
				newFormButton(room, "Goals", r - (tw * 2), t - 19, FurGuardiansGame.gemGoal[0], new Runnable() {
                    @Override public final void run() {
                        FurGuardiansGame.goGoals(FurGuardiansGame.pcs.get(0)); }}).getActorOverlay().getPosition().addY(-10);
				rt = lt = FurGuardiansGame.diamond;
				rtIn = ltIn = FurGuardiansGame.diamondIn;
			} else if (mode == TOUCH_HORIZONTAL) {
				rx = (int) (FurGuardiansGame.DIM_BUTTON * 1.25f);
				//sub = null;
			}
			final TouchButton left, right;
			final MappableInput act1;
			if (mode != TOUCH_JUMP) {
    			left = addButton(room, "Left", 0, y, input, act, ctrl.getOriginalLeft(), lt, ltIn, full);
                right = addButton(room, "Right", rx, y, input, act, ctrl.getOriginalRight(), rt, rtIn, full);
                act1 = addCircleButton(room, "Act1", r - rx, 0, input, act, ctrl.getOriginal1(), full);
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
				pause = new TouchButton(engine.getInteraction(), room, "Pause", r - 17, t - 17, 0, FurGuardiansGame.menuPause, FurGuardiansGame.menuPause, true);
				engine.registerTouchButton(pause);
				registerPromptQuit(pause.getActor(), pause);
			}
			if (input) {
				ctrl.map(down, up, left, right, act1, act2, null);
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
        private static Cursor quitCursor = null;
		
		protected final static void registerBackPromptQuit(final Panctor bound) {
			registerPromptQuit(bound, Pangine.getEngine().getInteraction().BACK);
		}
		
		protected final static void registerPromptQuit(final Panctor bound, final Panput input) {
			destroyPromptQuit();
		    bound.register(input, new ActionEndListener() {
                @Override
                public final void onActionEnd(final ActionEndEvent event) {
                    togglePromptQuit(bound.getLayer());
                }});
		}
		
		protected final static void togglePromptQuit(final Panlayer layer) {
			if (quitYes == null) {
                promptQuit(layer);
            } else {
                destroyPromptQuit();
            }
		}
		
		protected final static void promptQuit(final Panlayer room) {
			destroyPromptQuit();
		    final Pangine engine = Pangine.getEngine();
		    final int h = engine.getEffectiveTop();
		    final Panscreen screen = Panscreen.get();
		    final boolean platformScreen = screen instanceof FurGuardiansGame.PlatformScreen;
		    int btnY = 0;
		    if (platformScreen) {
		        final PlayerContext pc = Coltil.get(FurGuardiansGame.pcs, 0);
		        if (pc != null) {
    		        quitHandler = new ListActorHandler();
    		        room.setAddHandler(quitHandler);
    		        final InfoScreen iscrn = new InfoScreen(pc, false);
    		        iscrn.room = room;
    		        iscrn.form = new Panform(room, pc.ctrl);
    		        btnY = iscrn.displayGoals(getRankStarX(), h - 34, null) - FurGuardiansGame.MENU_H - 8;
    		        room.setAddHandler(null);
		        }
		    }
		    final Panple btnSize = FurGuardiansGame.menu.getSize();
		    if (btnY == 0) {
		    	btnY = TouchTabs.off(h, btnSize.getY());
		    }
            final boolean menuScreen = screen instanceof PlayerScreen;
            final int numButtons = menuScreen ? 2 : 3, r = engine.getEffectiveWidth();
            final int btnW = (int) btnSize.getX(), btnX = TouchTabs.off(r, btnW * numButtons);
            quitYes = newFormButton(room, "Quit", btnX + btnW * (numButtons - 1), btnY, FurGuardiansGame.menuOff, "Quit", new Runnable() {
                @Override public final void run() { engine.exit(); }});
            quitYes.setZ(15);
            final String noLbl;
            final Panmage noImg;
            if (menuScreen) {
            	noLbl = "Menu";
            	noImg = FurGuardiansGame.menuOptions;
            } else {
            	noLbl = "Play";
            	noImg = FurGuardiansGame.menuRight;
            	quitMenu = newFormButton(room, "Menu", btnX + btnW, btnY, FurGuardiansGame.menuOptions, "Menu", new Runnable() {
                    @Override public final void run() {
                    	FurGuardiansGame.notifications.clear();
                    	destroyPromptQuit();
                    	for (final PlayerContext pc : Coltil.unnull(FurGuardiansGame.pcs)) {
                        	if (pc != null && pc.player != null) {
                        	    pc.player.clearState();
                        	}
                    	}
                    	FurGuardiansGame.fadeOut(FurGuardiansGame.room, new ProfileScreen(FurGuardiansGame.pcs.get(0), true)); }});
                quitMenu.setZ(15);
                if (platformScreen) {
	                quitMsg = new Pantext(Pantil.vmid(), FurGuardiansGame.fontTiny, "You will lose your progress in this Level if you leave");
	                quitMsg.getPosition().set(r / 2, btnY - 7, 15);
	                quitMsg.centerX();
	                room.addActor(quitMsg);
                }
            }
            quitNo = newFormButton(room, noLbl, btnX, btnY, noImg, noLbl, new Runnable() {
                @Override public final void run() { destroyPromptQuit(); }});
            quitNo.setZ(15);
            if (engine.isMouseSupported()) {
            	quitCursor = addCursor(room);
            	Panlayer.setActive(Level.room, false);
            } else {
            	engine.setPaused(true);
            }
		}
		
		protected final static void destroyPromptQuit() {
			Pangine.getEngine().setPaused(false);
			Panlayer.setActive(Level.room, true);
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
            Panctor.destroy(quitCursor);
            quitCursor = null;
		}
		
		private final static TouchButton addCircleButton(final Panlayer room, final String name, final int x, final int y,
				final boolean input, final boolean act, final Panput old, final boolean moveCancel) {
			return addButton(room, name, x, y, input, act, old, FurGuardiansGame.button, FurGuardiansGame.buttonIn, moveCancel);
		}
		
		private final static TouchButton addDiamondButton(final Panlayer room, final String name, final int x, final int y,
                final boolean input, final boolean act, final Panput old) {
            return addButton(room, name, x, y, input, act, old, FurGuardiansGame.diamond, FurGuardiansGame.diamondIn, true);
        }
		
		private final static TouchButton addButton(final Panlayer room, final String name, final int x, final int y,
				final boolean input, final boolean act, final Panput old, final Panmage img, final Panmage imgIn,
				final boolean moveCancel) {
			final TouchButton button;
			if (input) {
				final Pangine engine = Pangine.getEngine();
				final Panteraction in = engine.getInteraction();
				final int d = FurGuardiansGame.DIM_BUTTON;
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
			return img == null ? 0 : TouchTabs.off(FurGuardiansGame.menu.getSize().getX(), img.getSize().getX());
		}
		
		protected final static int offy(final Panmage img, final CharSequence txt) {
			if (img == null) {
				return 0;
			}
			float btnH = FurGuardiansGame.menu.getSize().getY();
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
			final TouchButton tab = TouchTabs.newButton(getLayer(), Pantil.vmid(), FurGuardiansGame.menu, FurGuardiansGame.menuIn, img, offx(img), offy(img, txt), FurGuardiansGame.font, txt, OFF_TEXT_X, OFF_TEXT_Y,
					new Runnable() { @Override public void run() {
						if (disabled) {
							return;
						}
						listener.run(); }});
			tab.setImageDisabled(FurGuardiansGame.menuDisabled);
			tabs.add(tab);
			return tab;
		}
		
		protected final void newTabs() {
			TouchTabs.createWithOverlays(0, FurGuardiansGame.menu, FurGuardiansGame.menuIn, FurGuardiansGame.menuLeft, FurGuardiansGame.menuRight, tabs);
		}
		
		protected final boolean isTabEnabled() {
			final Pangine engine = Pangine.getEngine();
			return engine.isTouchSupported() || engine.isMouseSupported();
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
                    star = new Gem(FurGuardiansGame.gemGoal);
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
            star.setView(FurGuardiansGame.emptyGoal);
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
		
		protected final RadioGroup addRadio(final String title, final List<? extends CharSequence> list, final RadioSubmitListener subLsn, final RadioSubmitListener chgLsn, final int xb, int y, final TouchButton sub) {
			final int x;
			final boolean tab = tabsSupported && isTabEnabled();
			if (tab) {
				final int yt = y - OFF_RADIO_Y;
				final String id = Pantil.vmid();
				ctrl.mapUp(newFormButton(id + ".radio.up", xb, y, FurGuardiansGame.menuUp));
				ctrl.mapDown(newFormButton(id + ".radio.down", xb, yt, FurGuardiansGame.menuDown));
				if (subLsn != null) {
					//final TouchButton sub = newFormButton(id + ".radio.submit", x + 200, yt, FurGuardiansGame.menuCheck);
					//final TouchButton sub = null; // Will use tab bar to simulate submit button below
					ctrl.mapSubmit(sub);
					ctrl.map1(sub);
				}
				x = xb + OFF_RADIO_LIST;
				y = adjustRadioY(y);
			} else {
				x = xb;
			}
			final RadioGroup grp = new RadioGroup(FurGuardiansGame.font, list, subLsn);
			if (tab) {
				if (sub == null && subLsn != null) {
					newTab(FurGuardiansGame.menuCheck, "Done", new Runnable() {@Override public final void run() {grp.submit();}});
				}
				grp.setReactOnEnd(true);
			}
			grp.setChangeListener(chgLsn);
			addItem(grp, x, y - 16);
			grp.addChild(addTitle(title, x, y));
			final Pantext label = grp.getLabel();
			label.setLinesPerPage(radioLinesPerPage);
			label.stretchCharactersPerLineToFit();
			return grp;
		}
		
		private final int adjustRadioY(final int y) {
			if (tabsSupported && isTabEnabled() && OFF_RADIO_Y < 100) {
				return y + (100 - OFF_RADIO_Y);
			}
			return y;
		}
		
		protected final TouchButton newRadioSubmitButton(final int x, final int y) {
			final int xr = Pangine.getEngine().getEffectiveWidth() - x - (int) FurGuardiansGame.menu.getSize().getX();
			return newFormButton(Pantil.vmid() + ".radio.submit", xr, y - OFF_RADIO_Y, FurGuardiansGame.menuCheck);
		}
		
		protected final static TouchButton newFormButton(final Panlayer layer, final String name, final int x, final int y, final Panmage img) {
			return newFormButton(layer, name, x, y, img, (String) null);
		}
		
		protected final static TouchButton newFormButton(final Panlayer layer, final String name, final int x, final int y, final Panmage img, final String txt) {
			final Pangine engine = Pangine.getEngine();
			final TouchButton btn = new TouchButton(engine.getInteraction(), layer, name, x, y, 0, FurGuardiansGame.menu, FurGuardiansGame.menuIn, img, offx(img), offy(img, txt), FurGuardiansGame.font, txt, OFF_TEXT_X, OFF_TEXT_Y, true);
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
		
		protected final TouchButton newSub(final int x, final int y) {
            return isTabEnabled() ? newRadioSubmitButton(x, y) : null;
        }
        
		protected final TouchButton newBuy(final int x, final int y) {
            final TouchButton sub = newSub(x, y);
            TouchButton.detach(sub);
            return sub;
        }
		
		protected final void reattach(final String info, final TouchButton sub, final Panmage img, final String txt) {
        	setInfo(info);
        	if (sub == null) {
        		return;
        	}
        	sub.setOverlay(img, offx(img), offy(img, txt));
        	sub.setText(FurGuardiansGame.font, txt, OFF_TEXT_X, OFF_TEXT_Y);
        	TouchButton.reattach(sub);
        }
        
		protected final void reattachBuy(final String info, final TouchButton sub) {
        	reattach(info, sub, FurGuardiansGame.gem[0], "Buy");
        }
		
		protected final List<RadioGroup> addColor(final SimpleColor col, int x, int y) {
			return addColor(col, x, y, null);
		}
		
		protected final List<RadioGroup> addColor(final SimpleColor col, int x, int y, final String label) {
		    return addColor(col, x, y, label, null, null);
		}
		
		protected final List<RadioGroup> addColor(final SimpleColor col, int x, int y, final String label, final Runnable otherReloader, final String otherLabel) {
			if (tabsSupported && isTabEnabled()) {
				addColorTouch(col, label, otherReloader, otherLabel);
				return null;
			} else {
				return addColorClassic(col, x, y);
			}
		}
		
		protected final void addColorTouch(final SimpleColor col, final String label, final Runnable otherReloader, final String otherLabel) {
			final String id = Pantil.vmid();
			final Pangine engine = Pangine.getEngine();
			final Panple btnSize = FurGuardiansGame.menu.getSize();
			final int btnW = (int) btnSize.getX(), gapW = (btnW * 5) / 6, difW = btnW + gapW;
			final int minX = (engine.getEffectiveWidth() - (btnW * 3 + gapW * 2)) / 2;
			final int btnH = (int) btnSize.getY(), difH = btnH + 16;
			final int minY = (engine.getEffectiveTop() - (btnH + difH)) / 2;
			int x = minX, y = minY + difH;
			if (label != null) {
				addTitle(label + " Color", x, y + btnH + 1);
			}
			final int txtX = btnW / 2, txtY = y - 12;
			final StringBuilder sbR = new StringBuilder(), sbG = new StringBuilder(), sbB = new StringBuilder();
			final Pantext txtR = initCol(col.r, sbR, x + txtX, txtY);
			newFormButton(id + ".red.up", x, y, FurGuardiansGame.redUp, new AvtRunnable() {@Override public final void go() {
				col.r = incCol(col.r, sbR, txtR); }});
			x += difW;
			final Pantext txtG = initCol(col.g, sbG, x + txtX, txtY);
			newFormButton(id + ".green.up", x, y, FurGuardiansGame.greenUp, new AvtRunnable() {@Override public final void go() {
				col.g = incCol(col.g, sbG, txtG); }});
			x += difW;
			final Pantext txtB = initCol(col.b, sbB, x + txtX, txtY);
			newFormButton(id + ".blue.up", x, y, FurGuardiansGame.menuUp, new AvtRunnable() {@Override public final void go() {
				col.b = incCol(col.b, sbB, txtB); }});
			x = minX;
			y = minY;
			newFormButton(id + ".red.down", x, y, FurGuardiansGame.redDown, new AvtRunnable() {@Override public final void go() {
				col.r = decCol(col.r, sbR, txtR); }});
			x += difW;
			newFormButton(id + ".green.down", x, y, FurGuardiansGame.greenDown, new AvtRunnable() {@Override public final void go() {
				col.g = decCol(col.g, sbG, txtG); }});
			x += difW;
			newFormButton(id + ".blue.down", x, y, FurGuardiansGame.menuDown, new AvtRunnable() {@Override public final void go() {
				col.b = decCol(col.b, sbB, txtB); }});
			if (otherReloader != null) {
			    newFormButton(getLayer(), id + ".other", minX, (y - FurGuardiansGame.MENU_H) / 2, FurGuardiansGame.menuRgb, otherLabel, otherReloader);
			}
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
			eyeGrp.setSelected(FurGuardiansGame.fixEye(avt.eye) - 1);
		}
		
		protected final static int HUD_TEXT_Y = 20;
		
		protected final void addHudGems() {
		    final int gemX = center + 16, gemY = HUD_TEXT_Y;
            FurGuardiansGame.addHudGem(room, gemX, gemY);
            FurGuardiansGame.addHud(room, pc, gemX + FurGuardiansGame.OFF_GEM, gemY, false, false);
		}
		
		protected final void addHudRank() {
			final int gemX = center - 16 - FurGuardiansGame.OFF_GEM, gemY = HUD_TEXT_Y, textX = gemX - 1;
			addActor(new Gem(FurGuardiansGame.gemRank), gemX, gemY);
			addTitle("Rank", textX, gemY + 8).setRightJustified(true);
			final CharSequence seq = new CallSequence() {@Override protected String call() {
				return String.valueOf(pc.profile.getRank());
			}};
			addTitle(seq, textX, gemY).setRightJustified(true);
		}
		
		protected final void addHudAchievement() {
			final int gemX = center - 16 - FurGuardiansGame.OFF_GEM, gemY = 37, textX = gemX - 1;
			addActor(new Gem(FurGuardiansGame.gemAchieve), gemX, gemY);
			addTitle("Trophies", textX, gemY + 8).setRightJustified(true);
			final CharSequence seq = new CallSequence() {@Override protected String call() {
				return String.valueOf(pc.profile.getAchievedSize() + "/" + Achievement.ALL.length);
			}};
			addTitle(seq, textX, gemY).setRightJustified(true);
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
	        	in = new KeyInput(FurGuardiansGame.font, subLsn);
	        	new TouchKeyboard(FurGuardiansGame.key, FurGuardiansGame.keyIn, FurGuardiansGame.font, y - (int) FurGuardiansGame.key.getSize().getY() - 16);
	        	in.setProperName(true); // Might make sense for ControllerInput, but probably doesn't work right yet
	        } else {
		        final ControllerInput cin = new ControllerInput(FurGuardiansGame.font, subLsn);
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
		    final Message msg = new Message(FurGuardiansGame.font, txt, lsn);
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
			return addTitle(new Pantext(Pantil.vmid(), FurGuardiansGame.font, title), x, y);
		}
		
		protected final Pantext addTitleTiny(final CharSequence title, final int x, final int y) {
			return addTitle(new Pantext(Pantil.vmid(), FurGuardiansGame.fontTiny, title), x, y);
		}
		
		protected final Pantext addTitle(final Pantext tLbl, final int x, final int y) {
			tLbl.getPosition().set(x, y);
			form.getLayer().addActor(tLbl);
			return tLbl;
		}
		
		protected final void addNote(final CharSequence note) {
			if (!isTabEnabled()) {
				return;
			}
			addTitle(note, touchRadioX + FurGuardiansGame.MENU_W + 2, Pangine.getEngine().getEffectiveTop() - FurGuardiansGame.MENU_H - 10);
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
			FurGuardiansGame.setPosition(actor, x, Y_PLAYER, FurGuardiansGame.getDepthPlayer(pc.profile.currentAvatar.jumpMode));
			room.addActor(actor);
			actor.init();
			return actor;
		}
		
		protected final void addClothingModel() {
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
            if (garb == null) {
                return;
            }
        	init(garb.col);
        	garb.col2.init();
        }
        
        protected final void init(final SimpleColor col) {
            col.r = 0;
            col.g = 0;
            col.b = Avatar.DEF_JUMP_COL;
        }
        
        protected final void clearClothingModel(final TouchButton sub) {
            clthModel.setVisible(false);
            clearBuy(sub);
        }
        
        protected final void clearBuy(final TouchButton sub) {
            clearInfo();
            TouchButton.detach(sub);
        }
        
        protected final void displayClothingModel(final TouchButton sub, final int cost) {
            reloadAnimalStrip(mc, clthModel, false);
            clthModel.setVisible(true);
            reattachBuy(sub, cost);
        }
        
        protected final void reattachBuy(final TouchButton sub, final int cost) {
            reattachBuy("Buy for " + cost + "?", sub);
        }
        
        protected final boolean purchase(final TouchButton sub, final int cost) {
            if (pc.profile.spendGems(cost)) {
            	Panctor.setInvisible(clthModel);
                setInfo("Purchased!");
                TouchButton.detach(sub);
                return true;
            } else {
                setInfo("You need more Gems");
                TouchButton.detach(sub);
                return false;
            }
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
			FurGuardiansGame.goMap(SPEED_MENU_FADE);
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
			center(infLbl);
		}
		
		private final void center(final Pantext lbl) {
			lbl.getPosition().setX(center);
			lbl.centerX();
		}
		
		protected final void clearInfo() {
			Chartil.clear(inf);
		}
		
		protected final void addDescription(final int x, final int y) {
			descLbl = addTitleTiny(desc, x + (isTabEnabled() ? OFF_RADIO_LIST : 0), adjustRadioY(y) - (24 + (radioLinesPerPage * 8)));
		}
		
		protected final void setDescription(final String val) {
			Chartil.set(desc, val);
			if (isTabEnabled()) {
				center(descLbl);
			}
		}
		
		protected final void clearDescription() {
			Chartil.clear(desc);
		}
		
		protected final Pantext newExtraDescription(final StringBuilder desc, final int x, final int y, final int off) {
		    if (!isTabEnabled()) {
		        return null;
		    }
            return addTitleTiny(desc, x + OFF_RADIO_LIST, adjustRadioY(y) - (24 + (off * 6) + (radioLinesPerPage * 8)));
        }
		
		protected final void addDescription2(final int x, final int y) {
			desc2Lbl = newExtraDescription(desc2, x, y, 1);
		}
        
        protected final void setExtraDescription(final Pantext descLbl, final StringBuilder desc, final String val) {
            if (descLbl == null) {
                return;
            }
            Chartil.set(desc, val);
            center(descLbl);
        }
        
        protected final void setDescription2(final String val) {
        	setExtraDescription(desc2Lbl, desc2, val);
        }
        
        protected final void addDescription3(final int x, final int y) {
			desc3Lbl = newExtraDescription(desc3, x, y, 2);
		}
        
        protected final void setDescription3(final String val) {
        	setExtraDescription(desc3Lbl, desc3, val);
        }
		
		protected final int addExit(final String title, final int x, final int y) {
			final MsgCloseListener savLsn = new MsgCloseListener() {
				@Override public final void onClose() {
					exit(); }};
			return addLink(title, savLsn, x, y);
		}
		
		protected final void goProfile() {
			ProfileScreen.currentTab = ProfileScreen.TAB_SELECT_AVATAR;
			FurGuardiansGame.setScreen(new ProfileScreen(pc, false));
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
            pc = FurGuardiansGame.newPlayerContext(prf, ctrl, curr == null ? FurGuardiansGame.pcs.size() : curr.index);
            reloadAnimalStrip();
            triggerMapLoad();
            FurGuardiansGame.setScreen(new NewScreen(pc, false));
		}
		
		protected final void goOptions() {
            FurGuardiansGame.setScreen(new OptionsScreen(pc));
        }
		
		protected final void goPerks() {
            FurGuardiansGame.setScreen(new AssistScreen(pc));
        }
		
		protected final void goPreferredTheme() {
		    FurGuardiansGame.setScreen(new ThemeScreen(pc));
		}
		
		protected final void reloadAnimalStrip() {
			reloadAnimalStrip(pc, actor, false);
		}
		
		protected final static void reloadAnimalStrip(final PlayerContext pc, final Model actor, final boolean full) {
			FurGuardiansGame.reloadAnimalStrip(pc, full);
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
				finish();
			}
			
			protected abstract void update(final String value);
			
			protected void finish() {
			}
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
		private final static String TITLE = FurGuardiansGame.TITLE.toUpperCase();
		private static ArrayList<PlayerContext> tcs = new ArrayList<PlayerContext>(NUM_CHRS);
		private Pantext text = null;
		private Pantext trademark = null;
		int titleHeight = 0;
		
	    protected TitleScreen() {
            super(null, true);
            showGems = false;
        }
	    
	    @Override
	    protected final boolean isCursorDisplayed() {
			return false;
		}
	    
	    @Override
        protected final void menu() {
	        FurGuardiansGame.loaders = null;
	        final int bottom = getBottom();
	        final Pangine engine = Pangine.getEngine();
	        final boolean touch = engine.isTouchSupported();
	        final StringBuilder prompt = new StringBuilder();
	        if (touch) {
	            prompt.append("Tap to start");
	        } else {
	            prompt.append("Press anything");
	        }
	        text = addTitleCentered(prompt, bottom - 4);
	        /*engine.addTimer(text, 360, new TimerListener() {@Override public final void onTimer(final TimerEvent event) {
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
            }});*/
	        titleHeight = Math.round(tm.getHeight() * 5f / 8f);
	        final int titleEnd = Cabin.CabinScreen.displayName(TITLE, titleHeight, 0);
	        final Panple titlePos = tm.getPosition(titleEnd, titleHeight);
	        trademark = addTitle("" + Pantext.CHAR_TRADEMARK, 1 + (int) titlePos.getX(), 8 + (int) titlePos.getY());
	        addTitleCentered("Copyright " + Pantext.CHAR_COPYRIGHT + " " + FurGuardiansGame.YEAR, bottom + 21);
	        addTitleCentered(FurGuardiansGame.AUTHOR, bottom + 11);
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
	            	final String email = FurGuardiansGame.getEmail();
	            	log = log.replace("org.pandcorps.furguardians.", "");
	            	log = log.replace("org.pandcorps.", "");
	            	log = log.replace(".java", "");
	            	engine.setClipboard("Please send this to " + email + Iotil.BR + FurGuardiansGame.VERSION + Iotil.BR + log);
	            	addTitleTiny("Oh no!", 4, bottom + 48);
	            	addTitleTiny("It looks like the game crashed the last time you played.", 4, bottom + 42);
	            	addTitleTiny("We've copied an error report into your clipboard.", 4, bottom + 36);
	            	addTitleTiny("Please paste it into an email & send it to " + email + ".", 4, bottom + 30);
	            	addTitleTiny("We'll try to fix it!", 4, bottom + 24);
	            }
	        } catch (final Exception e) {
	        	// Just ignore; don't let error report generation cause another fatal error
	        }
	        final String[] ads = Mathtil.rand(FurGuardiansGame.ads);
	        final int adSize = ads.length;
	        for (int i = 0; i < adSize; i++) {
	            final String ad = ads[i];
	            addTitle(ad, engine.getEffectiveWidth() - (8 * ad.length()) - 1, engine.getEffectiveTop() - (9 * (i + 1)));
	        }
	        for (int i = 0; i < NUM_CHRS; i++) {
	        	final PlayerContext tc = tcs.get(i);
		        final Panctor actor = addActor(tc, FurGuardiansGame.SCREEN_W * (i + 1) / (NUM_CHRS + 1));
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
        		//final Touch touch = Pangine.getEngine().getInteraction().TOUCH;
        		//ctrl = new ControlScheme(null, null, null, null, touch, touch, touch);
        		// Will map touch buttons to keys instead of binding actions directly to touch buttons
        		ctrl = ControlScheme.getDefaultKeyboard();
        		ctrl.setDevice(device);
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
        		Tiles.newGemDecoration(null, titleIndex, FurGuardiansGame.getGemLetter(DynamicTileMap.getRawForeground(tile)));
        		tm.setTile(titleIndex, null);
        		FurGuardiansGame.shatterLetter(tm.getPosition(titleIndex));
        		count++;
        	}
        	Tiles.shatterBottomLeft = Tiles.shatterBottomRight = Tiles.shatterTopLeft = Tiles.shatterTopRight = true;
            exit();
	    }
	    
	    /*private final static void setCentered(final Pantext text, final StringBuilder b, final String value) {
	    	if (text == null) {
	    		return;
	    	}
	    	text.uncenterX();
	    	Chartil.set(b, value);
	    	text.centerX();
	    }*/
	    
	    protected final static void generateTitleCharacters() {
	        for (int i = 0; i < NUM_CHRS; i++) {
	        	final PlayerContext tc = generatePlayerContext(i);
	        	tc.profile.currentAvatar.randomize();
	        	tcs.add(tc);
	        	//TODO Menu screens which show player can probably use full=false, but will need full load when done
	        	FurGuardiansGame.reloadAnimalStrip(tc, false);
	        }
	    }
	    
	    @Override
        protected final void onExit() {
	        String defaultProfileName = Config.defaultProfileName;
	        final List<String> availableProfiles;
	        if (defaultProfileName == null) {
	            availableProfiles = FurGuardiansGame.getAvailableProfiles();
	            if (Coltil.size(availableProfiles) == 1) {
	                defaultProfileName = availableProfiles.get(0);
	            }
	        } else {
	            availableProfiles = null;
	        }
			if (defaultProfileName == null) {
				final SelectScreen screen = new SelectScreen(null, false, availableProfiles);
		        screen.ctrl = ctrl;
		        FurGuardiansGame.fadeOut(FurGuardiansGame.room, screen);
			} else {
				try {
					FurGuardiansGame.loadProfile(defaultProfileName, ctrl, FurGuardiansGame.pcs.size());
				} catch (final Exception e) {
					throw Pantil.toRuntimeException(e); //TODO handle missing profile
				}
				FurGuardiansGame.goMap();
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
	    	FurGuardiansGame.blockWord = FurGuardiansGame.defaultBlockWord;
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
			newTab(FurGuardiansGame.menuPlus, "New", new Runnable() {@Override public final void run() {newProfile();}});
			if (curr != null) {
				newTab(FurGuardiansGame.menuX, "Back", new Runnable() {@Override public final void run() {exit();}});
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
			    availableProfiles = FurGuardiansGame.getAvailableProfiles();
			}
			if (Coltil.isValued(availableProfiles)) {
				final RadioSubmitListener prfLsn = new RadioSubmitListener() {
					@Override public final void onSubmit(final RadioSubmitEvent event) {
						if (curr != null) {
							curr.destroy();
						}
						final int index = curr == null ? FurGuardiansGame.pcs.size() : curr.index;
						try {
							FurGuardiansGame.loadProfile(event.toString(), ctrl, index);
						} catch (final Exception e) {
							throw Pantil.toRuntimeException(e);
						}
						pc = FurGuardiansGame.pcs.get(index);
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
			newTab(FurGuardiansGame.menuCheck, "Done", new Runnable() {@Override public final void run() {exit();}});
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
	        addNameInput(curr.profile, namLsn, FurGuardiansGame.MAX_NAME_PROFILE, x, y); //TODO validation unique, submit link
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
            if (isTabEnabled() && Config.defaultProfileName == null && newToDefault) {
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
			newTab(FurGuardiansGame.menuCheck, "Done", new Runnable() {@Override public final void run() {exit();}});
			newTab(FurGuardiansGame.menuAvatar, "Edit", new Runnable() {@Override public final void run() {goAvatar();}});
			if (!newProfile) {
				//newTab(FurGuardiansGame.menuPlus, "New", new Runnable() {@Override public final void run() {newAvatar();}});
				newTab(FurGuardiansGame.menuPlus, "New", TAB_NEW);
				if (getAvatarsSize() > 1) {
					newTab(FurGuardiansGame.menuMinus, "Erase", new Runnable() {@Override public final void run() {delete();}});
				}
				newTab(FurGuardiansGame.menuInfo, "Info", new Runnable() {@Override public final void run() {goInfo();}});
				if (isPlayer1()) {
				    newTab(FurGuardiansGame.menuMenu, "Setup", new Runnable() {@Override public final void run() {goOptions();}});
					newTab(FurGuardiansGame.menuOff, "Quit", new Runnable() {@Override public final void run() {quit();}});
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
			FurGuardiansGame.setScreen(new ProfileScreen(pc, false));
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
					pc.profile.setCurrentAvatar(value); }};
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
                    goPerks(); }};
            x = addPipe(x, y);
            x = addLink("Perks", astLsn, x, y);
			final MsgCloseListener prfLsn = new MsgCloseListener() {
                @Override public final void onClose() {
                    save();
                    FurGuardiansGame.setScreen(new SelectScreen(pc, false, null)); }};
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
		    FurGuardiansGame.setScreen(new AvatarScreen(pc));
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
			FurGuardiansGame.setScreen(new InfoScreen(pc, true));
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
	
	protected final static String getNewName(final Profile prf) {
	    for (char c = ('A' - 1); c <= 'Z'; c++) {
	        final String name = NEW_AVATAR_NAME + ((c >= 'A') ? String.valueOf(c) : "");
	        if (isNameFree(prf, name)) {
	            return name;
	        }
	    }
	    return "RenameUs";
	}
	
	private final static boolean isNameFree(final Profile prf, final String name) {
        for (final Avatar avt : prf.avatars) {
            if (Pantil.equals(name, avt.getName())) {
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
		private final static byte TAB_COLOR2 = 4;
		private static byte currentTab = TAB_ANIMAL;
	    private boolean save = true;
		private Avatar old = null;
		private Avatar avt = null;
		//private boolean newAvt = false;
		
		protected AvatarScreen(final PlayerContext pc) {
			super(pc, false);
			tabsSupported = true;
			showRank = false;
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
				    final Runnable otherReloader;
				    final String otherLabel;
				    if (avt.getAnimal() == null) {
                        otherReloader = null;
                        otherLabel = null;
				    } else {
				        otherReloader = newReloader(TAB_COLOR2);
                        otherLabel = LABEL_COLOR2;
				    }
					addColor(avt.col, 0, 0, "Avatar", otherReloader, otherLabel);
					break;
				case TAB_COLOR2 :
                    addColor(avt.col2, 0, 0, "Secondary", newReloader(TAB_COLOR), LABEL_COLOR);
                    break;
				case TAB_NAME :
					createNameInput(touchKeyboardX, getTouchKeyboardY());
					break;
			}
			newTab(FurGuardiansGame.menuCheck, "Done", new Runnable() {@Override public final void run() {exit();}});
			newTab(FurGuardiansGame.menuX, "Undo", new Runnable() {@Override public final void run() {cancel();}});
			newTab(FurGuardiansGame.menuAnimal, "Kind", TAB_ANIMAL);
			newTab(FurGuardiansGame.menuEyes, "Eyes", TAB_EYES);
			newTab(FurGuardiansGame.menuColor, "Color", TAB_COLOR);
			newTab(FurGuardiansGame.menuGear, "Gear", new Runnable() {@Override public final void run() {goGear();}});
			newTab(FurGuardiansGame.menuKeyboard, "Name", TAB_NAME);
			if (FurGuardiansGame.debugMode) {
				newTab(FurGuardiansGame.menuRgb, "Dump", new Runnable() {@Override public final void run() {exportGraphics();}});
			}
			newTabs();
			registerBack(new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    cancel(); }});
		}
		
		private final Runnable newReloader(final byte tab) {
		    return new Runnable() {@Override public final void run() {reload(tab);}};
		}
		
		private final void newTab(final Panmage img, final CharSequence txt, final byte tab) {
			final TouchButton btn = newTab(img, txt, newReloader(tab));
			if (currentTab == tab) {
				btn.setEnabled(false);
			}
		}
		
		private void reload(final byte tab) {
			currentTab = tab;
			FurGuardiansGame.setScreen(new AvatarScreen(pc, old, avt));
		}
		
		private final void createAnimalList(final int x, final int y) {
		    addClothingModel();
		    init(mc.profile.currentAvatar.col);
		    mc.profile.currentAvatar.col2.set(1, 1, 1);
			final List<String> animals = FurGuardiansGame.getAnimals();
			final TouchButton sub = newBuy(x, y);
			final AvtListener anmLsn = new AvtListener() {
				@Override public final void update(final String value) {
				    final Animal animal = Avatar.getSpecialAnimal(value);
				    if (pc.profile.isAnimalAvailable(animal)) {
				        avt.anm = value;
				        clearClothingModel(sub);
				    } else {
				    	mc.profile.currentAvatar.anm = value;
				        displayClothingModel(sub, animal.getCost());
				    }
				}};
			final AvtListener anmSubLsn = new AvtListener() {
                @Override public final void update(final String value) {
                	final Animal animal = Avatar.getSpecialAnimal(value);
                    if (!pc.profile.isAnimalAvailable(animal) && purchase(sub, animal.getCost())) {
                    	pc.profile.availableSpecialAnimals.add(animal);
                    	avt.anm = value;
                    }
                }};
			final RadioGroup anmGrp = addRadio("Animal", animals, anmSubLsn, anmLsn, x, y, sub);
			anmGrp.setSelected(animals.indexOf(avt.anm));
		}
		
		private final void createEyeList(final int x, final int y) {
			createEyeList(avt, FurGuardiansGame.getNumEyes(), x, y);
		}
		
		private final void createNameInput(final int x, final int y) {
			final Input namIn = addNameInput(avt, null, FurGuardiansGame.MAX_NAME_AVATAR, x, y);
			namIn.append(avt.getName());
		}
		
		private final void goGear() {
			GearScreen.currentTab = GearScreen.TAB_DEFAULT;
			FurGuardiansGame.setScreen(new GearScreen(pc, old, avt));
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
                    exportGraphics(); }};
            x = addPipe(x, y);
            x = addLink("Export", expLsn, x, y);
		}
		
		private final void exportGraphics() {
			final Pangine engine = Pangine.getEngine();
            engine.setImageSavingEnabled(true);
            reloadAnimalStrip(pc, actor, true);
            engine.setImageSavingEnabled(false);
            setInfo(INFO_SAVED);
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
		    	if (pc != null) {
		    		final Profile prf = pc.profile;
		    		if (prf != null) {
		    			final Avatar avt = prf.currentAvatar;
		    			if (avt != null && Chartil.isEmpty(avt.getName())) {
		    				avt.setName(getNewName(prf));
		    			}
		    		}
		    	}
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
	    private final static byte TAB_BIRD = 9;
	    private final static byte TAB_BIRD_EYE = 10;
	    private final static byte TAB_BIRD_NAME = 11;
	    private final static byte TAB_HAT_COL2 = 12;
        private final static byte TAB_DEFAULT = TAB_CLOTHES;
        private static byte currentTab = TAB_DEFAULT;
        private final static String DEF_CLOTHES = "None";
	    private final Avatar old;
        private final Avatar avt;
        private RadioGroup jmpRadio = null;
        private List<RadioGroup> jmpColors = null;
        private TouchButton jmpBtn = null;
        private RadioGroup brdRadio = null;
        private List<RadioGroup> drgnColors = null;
        private TouchButton drgnBtn = null;
        private TouchButton drgnEyeBtn = null;
        private TouchButton drgnNameBtn = null;
        private TouchButton birdEyeBtn = null;
        private TouchButton birdNameBtn = null;
        private final ClothingMenu clthMenu = new ClothingMenu();
        private final HatMenu hatMenu = new HatMenu();
        private Panctor egg = null;
        
        protected GearScreen(final PlayerContext pc, final Avatar old, final Avatar avt) {
            super(pc, false);
            radioLinesPerPage = isTabEnabled() ? 5 : 2;
            this.old = old;
            this.avt = avt;
            tabsSupported = true;
            showRank = false;
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
        	sub.setText(FurGuardiansGame.font, txt, OFF_TEXT_X, OFF_TEXT_Y);
            return sub;
        }
        
        private final TouchButton newColor(final int x, final int y, final byte tab) {
        	return newTabSub(x, y, FurGuardiansGame.menuRgb, "Color", tab);
        }
        
        private final TouchButton newEye(final int x, final int y, final byte tab) {
            return newTabSub(x + FurGuardiansGame.MENU_W, y, FurGuardiansGame.menuEyesDragon, "Eyes", tab);
        }
        
        private final TouchButton newName(final int x, final int y, final byte tab) {
            return newTabSub(x, y - FurGuardiansGame.MENU_H, FurGuardiansGame.menuKeyboard, "Name", tab);
        }
        
        protected final void createJumpList(final int x, final int y) {
            final JumpMode[] jumpModes = JumpMode.values();
            final List<String> jmps = toNameList(jumpModes);
            final TouchButton sub = newBuy(x, y);
            jmpBtn = newColor(x, y, TAB_JUMP_COL);
            drgnBtn = newColor(x, y, TAB_DRAGON_COL);
            drgnEyeBtn = newEye(x, y, TAB_DRAGON_EYE);
            drgnNameBtn = newName(x, y, TAB_DRAGON_NAME);
            final AvtListener jmpLsn = new AvtListener() {
                @Override public final void update(final String value) {
                    final JumpMode jm = Player.get(jumpModes, value);
                    setDescription(jm.getDescription());
                    final byte index = jm.getIndex();
                    if (pc.profile.isJumpModeAvailable(index)) {
                        clearInfo();
                        TouchButton.detach(sub);
                        setJumpMode(index);
                    } else if (pc.profile.isJumpModeTryable(index) && avt.jumpMode != index) {
                    	reattach("Free trial for 1 Level?", sub, FurGuardiansGame.gemWhite, "Try");
                    } else {
                    	reattachBuy("Buy for " + jm.getCost() + "?", sub);
                    }
                }};
            final AvtListener jmpSubLsn = new AvtListener() {
                @Override public final void update(final String value) {
                    final JumpMode jm = Player.get(jumpModes, value);
                    final byte index = jm.getIndex();
                    if (!pc.profile.isJumpModeAvailable(index)) {
                        final int cost = jm.getCost();
                        if (Chartil.charAt(inf, 0) == 'F') {
                        	setJumpMode(index);
                        	reattachBuy("Equipped! Buy for " + jm.getCost() + "?", sub);
                        } else if (purchase(sub, cost)) {
                            pc.profile.availableJumpModes.add(Integer.valueOf(index));
                            setJumpMode(index);
                        }
                    }
                }};
            addNote("Equip one at a time");
            jmpRadio = addRadio("Power-up", jmps, jmpSubLsn, jmpLsn, x, y, sub);
            addDescription(x, y);
            initJumpMode();
        }
        
        protected final void createBirdList(final int x, final int y) {
            final List<String> brds = new ArrayList<String>();
            brds.add("None");
            if (Coltil.isEmpty(pc.profile.availableBirds)) {
                //brds.add(Avatar.FIRST_BIRD.getName());
                brds.add(Avatar.FIRST_BIRD_NAME);
            } else {
                brds.addAll(Avatar.BIRDS.keySet());
            }
            final TouchButton sub = newBuy(x, y);
            birdEyeBtn = newEye(x, y, TAB_BIRD_EYE);
            birdNameBtn = newName(x, y, TAB_BIRD_NAME);
            final AvtListener brdLsn = new AvtListener() {
                private String tempKind = null;
                @Override public final void update(final String value) {
                    tempKind = null;
                    Panctor.setInvisible(egg);
                    final BirdKind bird = Avatar.getBird(value);
                    if (pc.profile.isBirdAvailable(bird)) {
                        setBird("None".equals(value) ? null : value);
                        clearBuy(sub);
                    } else {
                        tempKind = value;
                        reattachBuy(sub, bird.getCost());
                    }
                }
                @Override public final void finish() {
                    if (tempKind == null) {
                        return;
                    } else if (Coltil.isEmpty(pc.profile.availableBirds)) {
                        if (egg == null) {
                            egg = new Panctor();
                            egg.setView(FurGuardiansGame.getEgg());
                            egg.getPosition().set(actor.getPosition().getX() - 24, Y_PLAYER);
                            room.addActor(egg);
                        } else {
                            egg.setVisible(true);
                        }
                    } else {
                        actor.bird.load(FurGuardiansGame.getBirdAnm(PRE_TMP_BIRD, tempKind, avt.bird.eye));
                    }
                }};
            final AvtListener brdSubLsn = new AvtListener() {
                @Override public final void update(final String value) {
                    final BirdKind bird = Avatar.getBird(value);
                    if (!pc.profile.isBirdAvailable(bird) && purchase(sub, bird.getCost())) {
                        pc.profile.availableBirds.add(bird);
                        setBird(bird.getName()); // value can be "Egg", so use bird.getName
                        if (egg != null) {
                            final Panple pos = egg.getPosition(), bpos = actor.bird.getPosition();
                            actor.bird.dst = new ImplPanple(bpos);
                            actor.bird.list = brds;
                            actor.bird.radio = brdRadio;
                            bpos.set(pos);
                            Tiles.shatterCenteredActor(room, FurGuardiansGame.getEgg8(), pos, true);
                            FurGuardiansGame.soundCrumble.startSound();
                            egg.destroy();
                            egg = null;
                        }
                    }
                }};
            addNote("Can collect Gems");
            brdRadio = addRadio("Bird", brds, brdSubLsn, brdLsn, x, y, sub);
            initBird();
        }
        
        private final void createBirdEyeList(final int x, final int y) {
            createEyeList(avt.bird, FurGuardiansGame.getNumEyes(), x, y);
        }
        
        private final void createBirdNameInput(final int x, final int y) {
            final Input namIn = addNameInput(avt.bird, null, FurGuardiansGame.MAX_NAME_AVATAR, x, y);
            namIn.append(avt.bird.getName());
        }
        
        private final void createDragonEyeList(final int x, final int y) {
			createEyeList(avt.dragon, FurGuardiansGame.getNumDragonEyes(), x, y);
		}
        
        private final void createDragonNameInput(final int x, final int y) {
			final Input namIn = addNameInput(avt.dragon, null, FurGuardiansGame.MAX_NAME_AVATAR, x, y);
			namIn.append(avt.dragon.getName());
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
                        clearClothingModel(sub);
                        setClothing(menu, c);
                    } else {
                    	final Avatar avt = mc.profile.currentAvatar;
                    	avt.clothing.clth = null;
                    	avt.hat.clth = null;
                    	menu.get(avt).clth = c;
                    	displayClothingModel(sub, c.getCost());
                    }
                }};
            final AvtListener clthSubLsn = new AvtListener() {
                @Override public final void update(final String value) {
                    final Clothing c = Player.get(clothings, value);
                    if (!Profile.isAvailable(menu.getAvailable(), c) && purchase(sub, c.getCost())) {
                        menu.getAvailable().add(c);
                        setClothing(menu, c);
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
                    final Runnable otherReloader;
                    final String otherLabel;
                    if ((avt.hat.clth != null) && avt.hat.clth.isSecondaryColorSupported()) {
                        otherReloader = newReloader(TAB_HAT_COL2);
                        otherLabel = LABEL_COLOR2;
                    } else {
                        otherReloader = null;
                        otherLabel = null;
                    }
                    addColor(avt.hat.col, 0, 0, "Hat", otherReloader, otherLabel);
                    break;
                case TAB_HAT_COL2 :
                    addColor(avt.hat.col2, 0, 0, "Hat Secondary", newReloader(TAB_HAT_COL), LABEL_COLOR);
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
                case TAB_BIRD :
                    createBirdList(touchRadioX, touchRadioY);
                    break;
                case TAB_BIRD_EYE :
                    createBirdEyeList(touchRadioX, touchRadioY);
                    break;
                case TAB_BIRD_NAME :
                    createBirdNameInput(touchKeyboardX, getTouchKeyboardY());
                    break;
            }
			newTab(FurGuardiansGame.menuCheck, "Back", new Runnable() {@Override public final void run() {exit();}});
			newTab(FurGuardiansGame.menuClothing, "Shirt", TAB_CLOTHES);
			newTab(FurGuardiansGame.menuHat, "Hat", TAB_HAT);
			newTab(FurGuardiansGame.menuJump, "Power", TAB_JUMP);
			newTab(FurGuardiansGame.menuBird, "Bird", TAB_BIRD);
			newTabs();
			registerBackExit();
		}
		
		private final Runnable newReloader(final byte tab) {
            return new Runnable() {@Override public final void run() {reload(tab);}};
        }
        
        private final void newTab(final Panmage img, final CharSequence txt, final byte tab) {
            final TouchButton btn = newTab(img, txt, newReloader(tab));
            if (currentTab == tab) {
                btn.setEnabled(false);
            }
        }
        
        private void reload(final byte tab) {
            currentTab = tab;
            FurGuardiansGame.setScreen(new GearScreen(pc, old, avt));
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
        	final JumpMode jm = JumpMode.get(avt.jumpMode);
            jmpRadio.setSelected(jm.getName());
            setDescription(jm.getDescription());
        }
        
        private final void initBird() {
            brdRadio.setSelected(Chartil.nvl(avt.bird.kind, "None"));
            prepareBird();
        }
        
        private final void setBird(final String kind) {
        	avt.bird.kind = kind;
        	prepareBird();
        }
        
        private final void prepareBird() {
        	final boolean needBird = avt.bird.kind != null;
        	TouchButton.reattach(birdEyeBtn, needBird);
        	TouchButton.reattach(birdNameBtn, needBird);
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
            FurGuardiansGame.setScreen(new AvatarScreen(pc, old, avt));
        }
	}
	
	protected final static class AssistScreen extends PlayerScreen {
		private List<StringBuilder> as = null;
        
        protected AssistScreen(final PlayerContext pc) {
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
			createAssistList(touchRadioX, touchRadioY);
			newTab(FurGuardiansGame.menuCheck, "Done", new Runnable() {@Override public final void run() {exit();}});
            newTabs();
            registerBackExit();
		}
		
		private final void createAssistList(final int x, final int y) {
			final TouchButton sub = newBuy(x, y);
            final Assist[] assists = Profile.PUBLIC_ASSISTS;
            as = new ArrayList<StringBuilder>(assists.length);
            for (final Assist a : assists) {
                as.add(new StringBuilder("  " + a.getName()));
            }
            final RadioSubmitListener aLsn = new RadioSubmitListener() {
                @Override public final void onSubmit(final RadioSubmitEvent event) {
                    highlightAssist(getAssist(event), sub);
                }};
            final RadioSubmitListener aSubLsn = new RadioSubmitListener() {
                @Override public final void onSubmit(final RadioSubmitEvent event) {
                    final Assist a = getAssist(event);
                    if (pc.profile.isAssistAvailable(a)) {
                    	toggleAssist(a);
                    } else {
                        final int cost = a.getCost();
                        if (pc.profile.spendGems(cost)) {
                            pc.profile.addAvailableAssist(a);
                            toggleAssist(a);
                            setInfo("Purchased!");
                        } else {
                            setInfo("You need more Gems");
                        }
                    }
                }};
            final String label;
            if (isTabEnabled()) {
            	label = "Assists";
            	addNote("Can equip multiple");
            } else {
            	label = "Assists (can equip multiple)";
            }
            addRadio(label, as, aSubLsn, aLsn, x, y, sub);
            addDescription(x, y);
            initAssists();
            highlightAssist(assists[0], sub);
        }
		
		protected final void menuClassic() {
			final int left = getLeft();
            int y = getTop();
            createAssistList(left, y);
            y -= 80;
            addExit("Back", left, y);
		}
        
        private final Assist getAssist(final Object event) {
        	return Profile.getAssist(event.toString().substring(2));
        }
        
        private final String getEquipped(final Assist a) {
        	return pc.profile.isAssistActive(a) ? "Equipped" : "Unequipped";
        }
        
        private final void highlightAssist(final Assist a, final TouchButton sub) {
        	if (pc.profile.isAssistAvailable(a)) {
        		reattach(getEquipped(a), sub, FurGuardiansGame.menuExclaim, "Equip");
            } else {
                reattachBuy("Buy for " + a.getCost() + "?", sub);
            }
        	setDescription(a.getDescription());
        }
        
        private final void toggleAssist(final Assist a) {
            pc.profile.toggleAssist(a);
            initAssists();
            setInfo(getEquipped(a));
        }
        
        private final void initAssists() {
        	for (final StringBuilder a : as) {
        		a.setCharAt(0, getFlag(pc.profile.isAssistActive(getAssist(a))));
        	}
        }
        
        @Override
        protected void onExit() {
        	save();
        	if (isTabEnabled()) {
        		goOptions();
        	} else {
        		goProfile();
        	}
        }
    }
	
	protected final static class ThemeScreen extends PlayerScreen {
	    private List<StringBuilder> ts = null;
	    
	    protected ThemeScreen(final PlayerContext pc) {
            super(pc, false);
            tabsSupported = true;
        }
        
        @Override
        protected final void menu() {
            pc.profile.initThemes();
            if (isTabEnabled()) {
                menuTouch();
            } else {
                menuClassic();
            }
        }
        
        protected final void menuTouch() {
            createThemeList(touchRadioX, touchRadioY);
            newTab(FurGuardiansGame.menuCheck, "Done", new Runnable() {@Override public final void run() {exit();}});
            newTabs();
            registerBackExit();
        }
        
        private final void createThemeList(final int x, final int y) {
            final TouchButton sub = newSub(x, y);
            final MapTheme[] themes = Map.themes;
            ts = new ArrayList<StringBuilder>(themes.length);
            for (final MapTheme t : themes) {
                ts.add(new StringBuilder("  " + t.name));
            }
            /*
            AssistScreen lets you select multiple.
            Could do same thing here, changing Profile.preferredTheme into a List.
            If left as a single preference, must add a "None" option to this Menu List.
            */
            final RadioSubmitListener tLsn = new RadioSubmitListener() {
                @Override public final void onSubmit(final RadioSubmitEvent event) {
                    highlightTheme(getTheme(event), sub);
                }};
            final RadioSubmitListener tSubLsn = new RadioSubmitListener() {
                @Override public final void onSubmit(final RadioSubmitEvent event) {
                    toggleTheme(getTheme(event));
                }};
            final String label;
            if (isTabEnabled()) {
                label = "Themes";
                addNote("Can select multiple");
            } else {
                label = "Themes (can select multiple)";
            }
            addRadio(label, ts, tSubLsn, tLsn, x, y, sub);
            initThemes();
            highlightTheme(themes[0], sub);
        }
        
        protected final void menuClassic() {
            final int left = getLeft();
            int y = getTop();
            createThemeList(left, y);
            y -= 80;
            addExit("Back", left, y);
        }
        
        private final MapTheme getTheme(final Object event) {
            return Map.getTheme(event.toString().substring(2));
        }
        
        private final String getSelected(final MapTheme t) {
            return pc.profile.isThemeSelected(t) ? "Selected" : "Unselected";
        }
        
        private final void highlightTheme(final MapTheme t, final TouchButton sub) {
            reattach(getSelected(t), sub, FurGuardiansGame.menuExclaim, "Pick");
        }
        
        private final void toggleTheme(final MapTheme t) {
            pc.profile.toggleTheme(t);
            initThemes();
            setInfo(getSelected(t));
        }
        
        private final void initThemes() {
            for (final StringBuilder t : ts) {
                t.setCharAt(0, getFlag(pc.profile.isThemeSelected(getTheme(t))));
            }
        }
        
        @Override
        protected void onExit() {
            pc.profile.initThemes();
            save();
            goOptions();
        }
	}
	
	protected final static class InfoScreen extends PlayerScreen {
		private final static byte TAB_AWARD = 0;
		private final static byte TAB_STATS = 1;
		protected final static byte TAB_GOALS = 2;
		private final static byte TAB_FOES = 3;
		protected static byte currentTab = TAB_AWARD;
	    private RadioGroup achRadio = null;
	    private final StringBuilder rankDesc = new StringBuilder();
	    private final List<Panctor> goalStars = new ArrayList<Panctor>(3);
	    private final List<Panctor> rankStars = new ArrayList<Panctor>(Profile.POINTS_PER_RANK);
	    private RadioGroup enemyRadio = null;
	    private final StringBuilder enemyDesc = new StringBuilder();
	    private Panctor enemyBack = null;
	    private Panctor enemy = null;
	    private Panctor enemyFront = null;
	    private Gem rankOrb = null;
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
					createGoalsList(rankStarX, (Pangine.getEngine().getEffectiveTop() - 124) / 2 + 116);
					break;
				case TAB_FOES :
				    createFoesList(touchRadioX, touchRadioY);
				    break;
			}
			final Runnable r = new Runnable() {@Override public final void run() {exit();}};
			if (fullMenu) {
				newTab(FurGuardiansGame.menuCheck, "Done", r);
				newTab(FurGuardiansGame.menuTrophy, "Award", TAB_AWARD);
				newTab(FurGuardiansGame.menuGraph, "Stats", TAB_STATS);
				newTab(FurGuardiansGame.menuStar, "Goals", TAB_GOALS);
				newTab(FurGuardiansGame.menuFoes, "Foes", TAB_FOES);
			} else {
				registerDoneAll(r);
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
			FurGuardiansGame.setScreen(new InfoScreen(pc, fullMenu));
		}
		
		private final void createAchievementList(final int x, final int y) {
            final StringBuilder b = new StringBuilder();
            final List<String> ach = new ArrayList<String>(Achievement.ALL.length);
            for (final Achievement a : Achievement.ALL) {
                Chartil.clear(b);
                b.append(getFlag(pc.profile.isAchieved(a))).append(' ');
                b.append(a.getName());
                ach.add(b.toString());
            }
            final RadioSubmitListener achLsn = new RadioSubmitListener() {
                @Override public final void onSubmit(final RadioSubmitEvent event) {
                    setAchDesc(event.toString());
            }};
            achRadio = addRadio("Achievements", ach, achLsn, x, y);
            addDescription(x, y);
            addDescription2(x, y);
            addDescription3(x, y);
            initAchDesc();
		}
		
		private final void createStatsList(final int x, final int y) {
			addRadio("Statistics", pc.profile.stats.toList(pc.profile), null, x, y);
		}
		
		private final void createFoesList(final int x, final int y) {
		    final List<String> list = new ArrayList<String>();
		    for (final EnemyDefinition def : FurGuardiansGame.allEnemies) {
		        list.add(def.getName());
		    }
		    enemyBack = addEnemy(0);
		    enemy = addEnemy(1);
		    enemyFront = addEnemy(2);
		    addTitle(enemyDesc, x + OFF_RADIO_LIST, y - 72);
		    final RadioSubmitListener foeLsn = new RadioSubmitListener() {
                @Override public final void onSubmit(final RadioSubmitEvent event) {
                    setEnemy(event.toString());
            }};
            enemyRadio = addRadio("Bestiary", list, foeLsn, x, y);
            initEnemy();
        }
		
		private final Panctor addEnemy(final int z) {
		    final Panctor enemy = addActor(getEnemyX(), Y_PLAYER);
            enemy.setMirror(true);
            enemy.getPosition().addZ(z);
            return enemy;
		}
		
		private final int getEnemyX() {
		    return center + 80;
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
				rankOrb = new Gem(FurGuardiansGame.gemRank);
				addActor(rankOrb, x, y);
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
			int y = (engine.getEffectiveTop() - h) / 2 + h - 8;
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
		
		private final void registerDoneAll(final Runnable r) {
			newTab(FurGuardiansGame.menuCheck, "Done", r);
			for (final Panput input : new Panput[] {ctrl.getSubmit(), ctrl.get1(), ctrl.get2()}) {
				tm.register(input, new ActionStartListener() {
					@Override public final void onActionStart(final ActionStartEvent event) {
						r.run();
					}});
			}
		}
		
		private final void addContinue(final int x, final int y) {
			if (isTabEnabled()) {
				registerDoneAll(new Runnable() {
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
									    final int gemBonus = getRankPromotionGemBonus(newRank);
									    if (rankOrb != null) {
    									    if (gemBonus == RANK_BONUS_10) {
    									        rankOrb.setGem(FurGuardiansGame.gemBlueRank);
    									    } else if (gemBonus == RANK_BONUS_100) {
                                                rankOrb.setGem(FurGuardiansGame.gemCyanRank);
    									    } else if (gemBonus >= RANK_BONUS_1000) {
                                                rankOrb.setGem(FurGuardiansGame.gemGreenRank);
    									    } else if (gemBonus >= RANK_BONUS_10000) {
                                                rankOrb.setGem(FurGuardiansGame.gemWhiteRank);
                                            }
									    }
										pc.addGems(gemBonus);
										final String strRank = String.valueOf(newRank);
										Chartil.set(rankDesc, "New Rank " + strRank + "   " + gemBonus);
										addActor(new Gem(), x + 1 + (12 + strRank.length()) * 8, y + 17);
										addRankPoints(goalIndex, x, y);
										FurGuardiansGame.musicLevelEnd.startSound();
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
		
		private final static int RANK_BONUS_10 = 2000;
		private final static int RANK_BONUS_100 = 3000;
		private final static int RANK_BONUS_1000 = 4000;
		private final static int RANK_BONUS_10000 = 5000;
		
		private final int getRankPromotionGemBonus(final int newRank) {
		    if (newRank % 10000 == 0) {
		        return RANK_BONUS_10000;
		    } else if (newRank % 1000 == 0) {
                return RANK_BONUS_1000;
		    } else if (newRank % 100 == 0) {
                return RANK_BONUS_100;
		    } else if (newRank % 10 == 0) {
		        return RANK_BONUS_10;
		    }
		    return 1000;
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
			FurGuardiansGame.soundGem.startSound();
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
            final String newDesc, newProg, newInf;
            if (Chartil.isEmpty(achName)) {
                newDesc = "";
                newProg = "";
                newInf = "";
            } else {
                achName = achName.substring(2);
                final Achievement ach = Achievement.get(achName);
                if (ach == null) {
                    throw new IllegalArgumentException("Could not find Achievement " + achName);
                }
                newDesc = ach.getDescription() + " (" + ach.getAward() + ")";
                newProg = ach.isMet(pc) ? null : ach.getProgress(pc);
                newInf = ach.getNote();
            }
            setDescription(newDesc);
            setDescription2(newProg);
            setDescription3(newInf);
        }
        
        private final void initAchDesc() {
            setAchDesc((String) achRadio.getSelected());
        }
        
        private final void setEnemy(final String name) {
        	final EnemyDefinition def = FurGuardiansGame.getEnemy(name);
            def.menu.draw(enemyBack, enemy, enemyFront, def, getEnemyX());
            Chartil.set(enemyDesc, "Defeated " + pc.profile.stats.getDefeatedCount(def));
        }
        
        private final void initEnemy() {
            setEnemy((String) enemyRadio.getSelected());
        }
        
        @Override
        protected boolean allow(final TextItem focused) {
            if (focused == achRadio) {
                initAchDesc();
            } else {
                clearDescription();
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
			return Pangine.getEngine().getEffectiveTop() > 204;
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
            final Panple btnSize = FurGuardiansGame.menu.getSize();
            final int h = engine.getEffectiveTop();
            final int btnW = (int) btnSize.getX(), btnH = (int) btnSize.getY();
            final int offY = (h >= 240) ? (btnH * 5 / 4) : btnH;
            int x = btnW / 2, y = h - btnH - offY;
            
            if (FurGuardiansGame.isMultiTouchSupported()) {
                newFormButton("AutoToggle", x, y, FurGuardiansGame.menuButtons, new Runnable() {@Override public final void run() {toggleAuto();}});
                addTitle(msgAuto, x + btnW + 8, y);
                setMessageAuto();
            }
            
            y -= offY;
            newFormButton("SpeedDown", x, y, FurGuardiansGame.menuLeft, new Runnable() {@Override public final void run() {incSpeed(-1);}});
            newFormButton("SpeedUp", engine.getEffectiveWidth() - x - btnW, y, FurGuardiansGame.menuRight, new Runnable() {@Override public final void run() {incSpeed(1);}});
            setMessageSpeed();
            addTitle(msgSpeed, x + btnW + 8, y);
            
            if (!engine.isMouseSupported()) {
	            y -= offY;
	            newFormButton("BtnSizeDown", x, y, FurGuardiansGame.menuLeft, new Runnable() {@Override public final void run() {incBtnSize(-1);}});
	            newFormButton("BtnSizeUp", engine.getEffectiveWidth() - x - btnW, y, FurGuardiansGame.menuRight, new Runnable() {@Override public final void run() {incBtnSize(1);}});
	            setMessageBtnSize();
	            addTitle(msgBtnSize, x + btnW + 8, y);
            }
            
            newTab(FurGuardiansGame.menuCheck, "Done", new Runnable() {@Override public final void run() {exit();}});
            newTab(FurGuardiansGame.menuMusic, "Music", new Runnable() {@Override public final void run() {goMusic();}});
            newTab(FurGuardiansGame.menuQuestion, "Perks", new Runnable() {@Override public final void run() {goPerks();}});
            newTab(FurGuardiansGame.menuWorld, "World", new Runnable() {@Override public final void run() {goPreferredTheme();}});
            newTab(FurGuardiansGame.menuDifficulty, "Easy", new Runnable() {@Override public final void run() {goDifficulty();}});
            if (pc.profile.consoleEnabled) {
            	newTab(FurGuardiansGame.menuKeyboard, "Debug", new Runnable() {@Override public final void run() {goConsole();}});
            }
            newTabs();
            registerBackExit();
        }
        
        private final void goMusic() {
            FurGuardiansGame.setScreen(new MusicScreen(pc));
        }
        
        private final void goDifficulty() {
            FurGuardiansGame.setScreen(new DifficultyScreen(pc));
        }
        
        private final void goConsole() {
            FurGuardiansGame.setScreen(new ConsoleScreen(pc));
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
        		s = Pangine.getEngine().isMouseSupported() ? "Full control" : "On-screen buttons";
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
                FurGuardiansGame.reloadButtons();
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
            final Panple btnSize = FurGuardiansGame.menu.getSize();
            final int btnW = (int) btnSize.getX(), btnH = (int) btnSize.getY(), offY = btnH * 5 / 4;
            int x = btnW / 2, y = engine.getEffectiveTop() - btnH - offY;
            
            newFormButton("MusicToggle", x, y, FurGuardiansGame.menuMusic, new Runnable() {@Override public final void run() {toggleMusic();}});
            addTitle(msgMusic, x + btnW + 8, y);
            setMessageMusic();
            
            y -= offY;
            newFormButton("SoundToggle", x, y, FurGuardiansGame.menuSound, new Runnable() {@Override public final void run() {toggleSound();}});
            addTitle(msgSound, x + btnW + 8, y);
            setMessageSound();
            
            newTab(FurGuardiansGame.menuCheck, "Done", new Runnable() {@Override public final void run() {exit();}});
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
	
	protected final static class DifficultyScreen extends BaseOptionsScreen {
		private final StringBuilder msgLoss = new StringBuilder();
		private final StringBuilder msgDeath = new StringBuilder();
		
		protected DifficultyScreen(final PlayerContext pc) {
            super(pc);
        }
        
        @Override
        protected final void menuTouch() {
        	final Pangine engine = Pangine.getEngine();
            final Panple btnSize = FurGuardiansGame.menu.getSize();
            final int btnW = (int) btnSize.getX(), btnH = (int) btnSize.getY(), offY = btnH * 5 / 4;
            int x = btnW / 2, y = engine.getEffectiveTop() - btnH - offY;
            
        	newFormButton("LossDown", x, y, FurGuardiansGame.menuLeft, new Runnable() {@Override public final void run() {incLoss(-1);}});
            newFormButton("LossUp", engine.getEffectiveWidth() - x - btnW, y, FurGuardiansGame.menuRight, new Runnable() {@Override public final void run() {incLoss(1);}});
            setMessageLoss();
            addTitle(msgLoss, x + btnW + 8, y);
            
            y -= offY;
            newFormButton("DeathToggle", x, y, FurGuardiansGame.menuDefeat, new Runnable() {@Override public final void run() {toggleDeath();}});
            addTitle(msgDeath, x + btnW + 8, y);
            setMessageDeath();
            
            newTab(FurGuardiansGame.menuCheck, "Done", new Runnable() {@Override public final void run() {exit();}});
            newTabs();
            registerBackExit();
        }
        
        private final void incLoss(final int dir) {
        	final int curr = pc.profile.damagePercentage, next;
        	if (dir < 0) {
        		if (curr <= Profile.MIN_DAMAGE_PERCENTAGE) {
        			next = Profile.MAX_DAMAGE_PERCENTAGE;
        		} else if (curr >= Profile.MAX_DAMAGE_PERCENTAGE) {
        			next = Profile.MID_DAMAGE_PERCENTAGE;
        		} else {
        			next = Profile.MIN_DAMAGE_PERCENTAGE;
        		}
        	} else {
        		if (curr <= Profile.MIN_DAMAGE_PERCENTAGE) {
        			next = Profile.MID_DAMAGE_PERCENTAGE;
        		} else if (curr >= Profile.MAX_DAMAGE_PERCENTAGE) {
        			next = Profile.MIN_DAMAGE_PERCENTAGE;
        		} else {
        			next = Profile.MAX_DAMAGE_PERCENTAGE;
        		}
        	}
            pc.profile.damagePercentage = next;
            setMessageLoss();
        }
        
        private final void setMessageLoss() {
            Chartil.set(msgLoss, "Damage rate: " + pc.profile.damagePercentage + "%");
        }
        
        private final void toggleDeath() {
            pc.profile.endLevelIfHurtWithNoGems = !pc.profile.endLevelIfHurtWithNoGems;
            setMessageDeath();
        }
        
        private final void setMessageDeath() {
            final String s;
            if (pc.profile.endLevelIfHurtWithNoGems) {
                s = "Can be defeated";
            } else {
                s = "Can't lose";
            }
            Chartil.set(msgDeath, s);
        }
        
        @Override
        protected void onExit() {
        	save();
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
			newTab(FurGuardiansGame.menuCheck, "Done", new Runnable() {@Override public final void run() {exit();}});
			newTab(FurGuardiansGame.menuExclaim, "Run", new Runnable() {@Override public final void run() {exec();}});
			newTab(FurGuardiansGame.menuMinus, "Clear", new Runnable() {@Override public final void run() {clear();}});
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
					msg = "Default (" + FurGuardiansGame.getApproximateFullScreenZoomedDisplaySize() + ")";
				} else  {
					msg = "Zoom: " + Config.zoomMag;
				}
			} else if ("zoomin".equalsIgnoreCase(cmd)) {
				if (Config.zoomMag > 0 && Config.zoomMag < FurGuardiansGame.getApproximateFullScreenZoomedDisplaySize()) {
					msg = setZoom(Config.zoomMag + 1);
				} else {
					msg = MSG_LIMIT;
				}
			} else if ("zoomout".equalsIgnoreCase(cmd)) {
				int z = Config.zoomMag;
				if (z < 0) {
					z = FurGuardiansGame.getApproximateFullScreenZoomedDisplaySize();
				}
				if (z > 1) {
					msg = setZoom(z - 1);
				} else {
					msg = MSG_LIMIT;
				}
			} else if ("zoomdef".equalsIgnoreCase(cmd)) {
				msg = setZoom(-1);
			} else if ("addclothes".equalsIgnoreCase(cmd)) {
			    msg = getAddedMsg(addClothes());
			} else if ("addhats".equalsIgnoreCase(cmd)) {
			    msg = getAddedMsg(addHats());
			} else if ("addjumps".equalsIgnoreCase(cmd)) {
                msg = getAddedMsg(addJumps());
			} else if ("addwings".equalsIgnoreCase(cmd)) {
			    msg = getAddedMsg(pc.profile.availableJumpModes.add(Integer.valueOf(Player.JUMP_FLY)));
			} else if ("addarmor".equalsIgnoreCase(cmd)) {
				msg = getAddedMsg(pc.profile.availableClothings.add(Avatar.getClothing("Armor")));
			} else if ("addperks".equalsIgnoreCase(cmd)) {
				msg = getAddedMsg(addPerks());
			} else if ("addanimals".equalsIgnoreCase(cmd)) {
				msg = getAddedMsg(addAnimals());
			} else if ("addbirds".equalsIgnoreCase(cmd)) {
                msg = getAddedMsg(addBirds());
			} else if ("addall".equalsIgnoreCase(cmd)) {
				msg = getAddedMsg(addClothes() | addHats() | addJumps() | addPerks() | addAnimals() | addBirds());
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
			} else if ("setrock".equalsIgnoreCase(cmd)) {
				msg = setMapTheme(MapTheme.Rock);
			} else if ("sethive".equalsIgnoreCase(cmd)) {
                msg = setMapTheme(MapTheme.Hive);
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
						} else if (!prf.startsWith(FurGuardiansGame.SEG_PRF)) {
							msg = "Invalid";
						} else {
						    final Profile tprf = new Profile();
						    try {
						        tprf.load(SegmentStream.openString(prf).readRequire(FurGuardiansGame.SEG_PRF));
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
		
		private final static String getAddedMsg(final boolean added) {
			return added ? MSG_OK : MSG_LIMIT;
		}
		
		private final boolean addClothes() {
			boolean added = false;
		    for (final Clothing c : Avatar.clothings) {
		        if (pc.profile.availableClothings.add(c)) {
		            added = true;
		        }
		    }
		    return added;
		}
		
		private final boolean addHats() {
			boolean added = false;
		    for (final Clothing c : Avatar.hats) {
		        if (pc.profile.availableHats.add(c)) {
		            added = true;
		        }
		    }
		    return added;
		}
		
		private final boolean addJumps() {
			boolean added = false;
            for (final JumpMode jm : JumpMode.values()) {
                if (pc.profile.availableJumpModes.add(Integer.valueOf(jm.getIndex()))) {
                    added = true;
                }
            }
            return added;
		}
		
		private final boolean addPerks() {
			boolean added = false;
			final Profile prf = pc.profile;
			for (final Assist a : Profile.PUBLIC_ASSISTS) {
				if (!prf.isAssistAvailable(a)) {
					prf.addAvailableAssist(a);
					added = true;
				}
			}
			return added;
		}
		
		private final boolean addAnimals() {
			boolean added = false;
			final Profile prf = pc.profile;
			for (final Animal a : Avatar.SPECIAL_ANIMALS.values()) {
				if (!prf.isAnimalAvailable(a)) {
					prf.availableSpecialAnimals.add(a);
					added = true;
				}
			}
			return added;
		}
		
		private final boolean addBirds() {
            boolean added = false;
            final Profile prf = pc.profile;
            for (final BirdKind b : Avatar.BIRDS.values()) {
                if (!prf.isBirdAvailable(b)) {
                    prf.availableBirds.add(b);
                    added = true;
                }
            }
            return added;
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
	    private int blinkTimer = Mathtil.randi(FurGuardiansGame.DUR_BLINK / 4, FurGuardiansGame.DUR_BLINK * 3 / 4);
	    private int mirrorTimer = Mathtil.randi(60, 240);
	    private boolean origDir = true;
	    private Accessories acc = null;
	    private final BirdModel bird;
	    
	    private Model(final PlayerContext pc) {
	        bird = new BirdModel(pc);
	    	load(pc);
	    }
	    
	    private void load(final PlayerContext pc) {
	    	if (acc != null) {
	    		acc.destroy();
	    	}
	    	acc = new Accessories(pc);
	    	acc.onStepEnd(this);
	    	setView(pc.guy);
	    	bird.load(pc);
	    }
	    
	    private final void init() {
	        final Panple pos = getPosition();
            bird.getPosition().set(pos.getX() + 20, pos.getY() + 22, pos.getZ() + 10);
            getLayer().addActor(bird);
        }
	    
        @Override
        public final void onStep(final StepEvent event) {
            blinkTimer--;
            if (blinkTimer <= 0) {
                setView((Panimation) getView());
                blinkTimer = Mathtil.randi(FurGuardiansGame.DUR_BLINK * 5 / 4, FurGuardiansGame.DUR_BLINK * 7 / 4);
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
    		bird.destroy();
    	}
	}
	
	private final static String PRE_TMP_BIRD = "img.tmp.bird.";
	
	private final static class BirdModel extends Panctor implements StepListener {
	    private int mirrorTimer = Mathtil.randi(90, 180);
	    private Panimation anm;
	    private Panple dst = null;
	    private List<String> list = null;
	    private RadioGroup radio = null;
	    
	    private BirdModel(final PlayerContext pc) {
	        load(pc);
	    }
	    
	    private void load(final PlayerContext pc) {
	        load(pc.bird);
	    }
	    
	    private void load(final Panimation anm) {
	        if (anm != this.anm) {
    	        Panmage.destroyAll(this.anm);
    	        this.anm = anm;
	        }
	        if (anm == null) {
	            setVisible(false);
	        } else {
	            setVisible(true);
	            changeView(anm);
	        }
	    }
	    
	    @Override
        public final void onStep(final StepEvent event) {
	        if (dst != null) {
	            final Panple pos = getPosition(), dif = Panple.subtract(dst, pos);
	            if (dif.getMagnitude2() < 2) {
	                pos.set(dst);
	                dst = null;
	                if (Coltil.size(list) < 3 && list.remove(Avatar.FIRST_BIRD_NAME)) {
	                    list.addAll(Avatar.BIRDS.keySet());
	                    radio.getLabel().stretchCharactersPerLineToFit();
	                }
	                return;
	            }
	            dif.setMagnitude2(1);
	            pos.add(dif);
	            setMirror(dif.getX() < 0);
	            return;
	        }
	        mirrorTimer--;
	        if (mirrorTimer <= 0) {
	            mirrorTimer = Mathtil.randi(60, 180);
	            setMirror(!isMirror());
	        }
	    }
	    
	    @Override
	    public final void onDestroy() {
	        if (Chartil.startsWith(BasePantity.getId(this.anm), PRE_TMP_BIRD)) {
	            Panmage.destroyAll(this.anm);
	        }
	        super.onDestroy();
	    }
	}
}
