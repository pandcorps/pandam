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
package org.pandcorps.platform;

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.game.core.ImtilX;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandax.in.ControlScheme;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.platform.Player.*;

public class Menu {
    private final static short SPEED_MENU_FADE = 6;
    private final static String NAME_NEW = "org.pandcorps.new";
    private final static String WARN_DELETE = "Press Erase again to confirm";
    private final static String WARN_EMPTY = "Must have a name";
    private final static String WARN_DUPLICATE = "Name already used";
    
	protected abstract static class PlayerScreen extends Panscreen {
		protected Panroom room;
		protected PlayerContext pc;
		protected ControlScheme ctrl = null;
		private final boolean fadeIn;
		protected final StringBuilder wrn = new StringBuilder();
		protected TileMap tm = null;
		protected Panmage timg = null;
		protected Panctor actor = null;
		protected Pantext wrnLbl = null;
		protected boolean disabled = false;
		protected Panform form = null;
		protected int center = -1;
		
		protected PlayerScreen(final PlayerContext pc, final boolean fadeIn) {
			this.pc = pc;
			this.fadeIn = fadeIn;
		}
		
		@Override
		protected final void load() throws Exception {
			final int w = PlatformGame.SCREEN_W;
			center = w / 2;
			room = PlatformGame.createRoom(w, PlatformGame.SCREEN_H);
			Pangine.getEngine().setBgColor(new FinPancolor((short) 128, (short) 192, Pancolor.MAX_VALUE));
			
			tm = new TileMap(Pantil.vmid(), room, ImtilX.DIM, ImtilX.DIM);
			timg = Level.getTileImage();
			tm.setImageMap(timg);
			final TileMapImage[][] imgMap = tm.splitImageMap();
			tm.fillBackground(imgMap[1][1], 0, 1);
			room.addActor(tm);
			
			if (pc != null) {
				actor = addActor(pc, center);
			    ctrl = pc.ctrl;
			}
			form = new Panform(ctrl);
			wrnLbl = addTitle(form, wrn, center, getBottom());
			form.setTabListener(new FormTabListener() {@Override public void onTab(final FormTabEvent event) {
				if (allow()) {
					wrn.setLength(0);
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
		
		protected final Panctor addActor(final PlayerContext pc, final int x) {
			final Panctor actor = new Panctor();
			actor.setView(pc.guy);
			actor.getPosition().set(x, 16, tm.getForegroundDepth() + 1);
			room.addActor(actor);
			return actor;
		}
		
		@Override
	    protected void destroy() {
			timg.destroy();
		}
		
		protected abstract void menu() throws Exception;
		
		protected boolean allow() {
			return true;
		}
		
		protected final void exit() {
		    disabled = true;
		    onExit();
		}
		
		protected final void save() {
		    pc.profile.serialize();
		}
		
		protected abstract void onExit();
		
		protected final void setWarning(final String val) {
			Chartil.set(wrn, val);
        	wrnLbl.getPosition().setX(center);
        	wrnLbl.centerX();
		}
		
		protected final void addExit(final String title, final int x, final int y) {
			final MessageCloseListener savLsn = new MessageCloseListener() {
				@Override public final void onClose(final MessageCloseEvent event) {
				    if (disabled) {
	                    return;
	                }
					exit(); }};
			addLink(form, title, savLsn, x, y);
		}
		
		protected final void goProfile() {
			Panscreen.set(new ProfileScreen(pc, false));
		}
		
		protected abstract class AvtListener implements RadioSubmitListener {
			@Override public final void onSubmit(final RadioSubmitEvent event) {
			    if (disabled) {
			        return;
			    }
				update(event.toString());
				PlatformGame.reloadAnimalStrip(pc);
				actor.setView(pc.guy);
			}
			
			protected abstract void update(final String value);
		}
	}
	
	protected final static class TitleScreen extends PlayerScreen {
		private final static int NUM_CHRS = 4;
		private final ArrayList<PlayerContext> tcs = new ArrayList<PlayerContext>(NUM_CHRS);
		
	    protected TitleScreen() {
            super(null, true);
        }
	    
	    @Override
        protected final void menu() {
	        final Pantext text = addTitle(form, "Press anything", center, getBottom());
	        text.centerX();
	        text.register(new ActionStartListener() {@Override public void onActionStart(final ActionStartEvent event) {
	            ctrl = ControlScheme.getDefault(event.getInput().getDevice());
	            exit();
	        }});
	        for (int i = 0; i < NUM_CHRS; i++) {
	        	final Profile prf = new Profile();
	        	final Avatar avt = new Avatar();
	        	avt.randomize();
	        	prf.currentAvatar = avt;
	        	prf.avatars.add(avt);
	        	final PlayerContext tc = new PlayerContext(prf, null, Integer.MAX_VALUE - i);
	        	PlatformGame.reloadAnimalStrip(tc);
	        	final Panctor actor = addActor(tc, PlatformGame.SCREEN_W * (i + 1) / (NUM_CHRS + 1));
	        	if (i >= NUM_CHRS / 2) {
	        		actor.setMirror(true);
	        	}
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
		}
		
		@Override
		protected final void menu() {
			final List<String> list = PlatformGame.getAvailableProfiles();
			int y = getTop();
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
						goProfile();
				}};
				addRadio(form, "Pick Profile", list, prfLsn, null, 8, y);
			}
			final MessageCloseListener newLsn = new MessageCloseListener() {
                @Override public final void onClose(final MessageCloseEvent event) {
                    if (disabled) {
                        return;
                    }
                    if (curr != null) {
                        curr.destroy();
                    }
                    final Profile prf = new Profile();
                    final Avatar avt = new Avatar();
                    prf.setName("New");
                    avt.randomize();
                    avt.setName("New");
                    prf.currentAvatar = avt;
                    prf.avatars.add(avt);
                    //prf.ctrl = 0;
                    pc = PlatformGame.newPlayerContext(prf, ctrl, curr == null ? PlatformGame.pcs.size() : curr.index);
                    PlatformGame.reloadAnimalStrip(pc);
                    Panscreen.set(new NewScreen(pc, false)); }};
            y -= 56;
			addLink(form, "New", newLsn, 8, y);
			if (curr != null) {
				addExit("Cancel", 40, y);
			}
		}

		@Override
		protected final void onExit() {
			if (pc == null) {
				pc = curr;
			}
			goProfile();
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
        }

        @Override
        protected final void menu() {
	        /*final Profile prf = new Profile();
	        final Avatar avt = new Avatar();
	        avt.randomize();
	        avt.setName("New");*/
	        final InputSubmitListener namLsn = new InputSubmitListener() {
                @Override public final void onSubmit(final InputSubmitEvent event) {
                    exit(); }};
            int y = getTop() - 72;
	        addNameInput(form, curr.profile, namLsn, PlatformGame.MAX_NAME_PROFILE, 8, y); //TODO validation unique, submit link
	    }

        @Override
        protected final void onExit() {
            if (pc == null) {
                pc = curr;
            }
            save();
            goProfile();
        }
	}
	
	protected final static class ProfileScreen extends PlayerScreen {
	    private boolean save = false;
	    private final Avatar originalAvatar;
	    
		protected ProfileScreen(final PlayerContext pc, final boolean fadeIn) {
			super(pc, fadeIn);
			originalAvatar = pc.profile.currentAvatar;
		}
		
		@Override
		protected final void menu() {
			final List<String> avatars = new ArrayList<String>(pc.profile.avatars.size());
            for (final Avatar a : pc.profile.avatars) {
            	avatars.add(a.getName());
            }
			final AvtListener avtLsn = new AvtListener() {
				@Override public final void update(final String value) {
					pc.profile.currentAvatar = pc.profile.getAvatar(value); }};
			int y = getTop();
			final RadioGroup avtGrp = addRadio(form, "Pick Avatar", avatars, avtLsn, 8, 184);
			avtGrp.setSelected(avatars.indexOf(pc.profile.currentAvatar.getName()));
			final MessageCloseListener edtLsn = new MessageCloseListener() {
                @Override public final void onClose(final MessageCloseEvent event) {
                    if (disabled) {
                        return;
                    }
                    goAvatar(); }};
            int x = 8;
            y -= 72;
            x = addLink(form, "Edit", edtLsn, x, y);
            final MessageCloseListener newLsn = new MessageCloseListener() {
                @Override public final void onClose(final MessageCloseEvent event) {
                    if (disabled) {
                        return;
                    }
                    final Avatar avt = new Avatar();
                    avt.randomize();
                    avt.setName(NAME_NEW);
                    pc.profile.avatars.add(avt);
                    pc.profile.currentAvatar = avt;
                    PlatformGame.reloadAnimalStrip(pc);
                    actor.setView(pc.guy);
                    goAvatar(); }};
            x = addLink(form, "New", newLsn, x, y);
            if (pc.profile.avatars.size() > 1) {
	            final MessageCloseListener delLsn = new MessageCloseListener() {
	                @Override public final void onClose(final MessageCloseEvent event) {
	                    if (disabled) {
	                        return;
	                    } else if (pc.profile.avatars.size() == 1) {
	                    	return;
	                    }
	                    if (!wrn.toString().equals(WARN_DELETE)) {
	                    	setWarning(WARN_DELETE);
	                    	return;
	                    }
	                    wrn.setLength(0);
	                    pc.profile.avatars.remove(pc.profile.currentAvatar);
	                    pc.profile.currentAvatar = pc.profile.avatars.get(0);
	                    PlatformGame.reloadAnimalStrip(pc);
	                    actor.setView(pc.guy);
	                    save = true;
	                    goProfile(); }};
	            x = addLink(form, "Erase", delLsn, x, y);
            }
			addExit(Map.started ? "Back" : "Start", x, y);
			final MessageCloseListener prfLsn = new MessageCloseListener() {
                @Override public final void onClose(final MessageCloseEvent event) {
                    if (disabled) {
                        return;
                    }
                    Panscreen.set(new SelectScreen(pc, false)); }};
            y -= 16;
            addLink(form, "Pick Profile", prfLsn, 8, y);
            y -= 16;
            if (pc.index == 0) {
                addTitle(form, "Default Profile:", 8, y);
            	final StringBuilder defStr = new StringBuilder();
            	defStr.append(getDefaultProfileText());
                final MessageCloseListener defLsn = new MessageCloseListener() {
                    @Override public final void onClose(final MessageCloseEvent event) {
                        if (disabled) {
                            return;
                        }
                        if (isDefaultProfile()) {
                        	Config.defaultProfileName = null;
                        } else {
                        	Config.defaultProfileName = pc.profile.getName();
                        }
                        Config.serialize();
                        Chartil.set(defStr, getDefaultProfileText()); }};
                addLink(form, defStr, defLsn, 144, y);
                y -= 16;
                final MessageCloseListener qutLsn = new MessageCloseListener() {
                    @Override public final void onClose(final MessageCloseEvent event) {
                        if (disabled) {
                            return;
                        }
                        Pangine.getEngine().exit(); }}; // Exit to TitleScreen instead? Quit game from there?
                addLink(form, "Quit Game", qutLsn, 8, y);
            }
			// Rename Profile //TODO
			// Drop out (if other players? if not player 1?)
            // Exit to title (if player 1)
            // Delete Profile (if player 1)
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
		
		@Override
		protected void onExit() {
		    if (save || pc.profile.currentAvatar != originalAvatar) {
		        save();
		    }
		    PlatformGame.goMap(SPEED_MENU_FADE);
		}
	}
	
	private final static String getNewName(final Profile profile) {
	    for (char c = 'A' - 1; c <= 'Z'; c++) {
	        final String name = "New" + (c >= 'A' ? String.valueOf(c) : "");
	        boolean missing = true;
	        for (final Avatar avt : profile.avatars) {
	            if (name.equals(avt.getName())) {
	                missing = false;
	                break;
	            }
	        }
	        if (missing) {
	            return name;
	        }
	    }
	    return "RenameUs";
	}
	
	protected final static class AvatarScreen extends PlayerScreen {
	    private boolean save = true;
		private Avatar old = null;
		private Avatar avt = null;
		//private boolean newAvt = false;
		
		protected AvatarScreen(final PlayerContext pc) {
			super(pc, false);
		}
		
		private final void initAvatar() {
			old = pc.profile.currentAvatar;
			avt = new Avatar(old);
			pc.profile.replaceAvatar(avt);
		}
		
		@Override
		protected final void menu() throws Exception {
			initAvatar();
			if (NAME_NEW.equals(old.getName())) {
			    //newAvt = true;
			    avt.setName(getNewName(pc.profile)); // If old keeps NAME_NEW, then cancel can rely on that
			}
			final List<String> animals = PlatformGame.getAnimals();
			final AvtListener anmLsn = new AvtListener() {
				@Override public final void update(final String value) {
					avt.anm = value; }};
			int y = getTop();
			final RadioGroup anmGrp = addRadio(form, "Animal", animals, anmLsn, 8, y);
			final int numEyes = PlatformGame.getNumEyes();
			final ArrayList<String> eyes = new ArrayList<String>(numEyes);
			for (int i = 1; i <= numEyes; i++) {
			    eyes.add(Integer.toString(i));
			}
			final AvtListener eyeLsn = new AvtListener() {
				@Override public final void update(final String value) {
					avt.eye = Integer.parseInt(value); }};
			final RadioGroup eyeGrp = addRadio(form, "Eye", eyes, eyeLsn, 80, y);
			final List<String> colors = Arrays.asList("0", "1", "2", "3", "4");
			final AvtListener redLsn = new ColorListener() {
				@Override public final void update(final float value) {
					avt.r = value; }};
			final RadioGroup redGrp = addRadio(form, "Red", colors, redLsn, 112, y);
			final AvtListener greenLsn = new ColorListener() {
				@Override public final void update(final float value) {
					avt.g = value; }};
			final RadioGroup grnGrp = addRadio(form, "Grn", colors, greenLsn, 144, y);
			final AvtListener blueLsn = new ColorListener() {
				@Override public final void update(final float value) {
					avt.b = value; }};
			final RadioGroup bluGrp = addRadio(form, "Blu", colors, blueLsn, 176, y);
			y -= 72;
			final ControllerInput namIn = addNameInput(form, avt, null, PlatformGame.MAX_NAME_AVATAR, 8, y);
			y -= 16;
			addExit("Save", 8, y);
			final MessageCloseListener canLsn = new MessageCloseListener() {
                @Override public final void onClose(final MessageCloseEvent event) {
                    if (disabled) {
                        return;
                    } else if (NAME_NEW.equals(old.getName())) {
                        pc.profile.avatars.remove(pc.profile.currentAvatar);
                        pc.profile.currentAvatar = pc.profile.avatars.get(0);
                    } else {
                        pc.profile.replaceAvatar(old);
                    }
                    PlatformGame.reloadAnimalStrip(pc);
                    actor.setView(pc.guy);
                    save = false;
                    exit(); }};
            addLink(form, "Cancel", canLsn, 48, y);
			anmGrp.setSelected(animals.indexOf(avt.anm));
			eyeGrp.setSelected(avt.eye - 1);
			redGrp.setSelected(getLineColor(avt.r));
			grnGrp.setSelected(getLineColor(avt.g));
			bluGrp.setSelected(getLineColor(avt.b));
			namIn.append(avt.getName());
		}
		
		@Override
		protected boolean allow() {
			final String curr = avt.getName();
			if (Chartil.isEmpty(curr)) {
				setWarning(WARN_EMPTY);
				return false;
			}
			for (final Avatar a : pc.profile.avatars) {
				if (a != avt && a.getName().equals(curr)) {
					setWarning(WARN_DUPLICATE);
					return false;
				}
			}
			return true;
		}
		
		private abstract class ColorListener extends AvtListener {
			@Override
			protected final void update(final String value) {
				update(Avatar.toColor(Integer.parseInt(value)));
			}
			
			protected abstract void update(final float value);
		}
		
		@Override
		protected void onExit() {
		    if (save) {
		        save();
		    }
		    goProfile();
		}
	}
	
	private final static int getLineColor(final float c) {
		return Math.round(c * 4);
	}
	
	private final static RadioGroup addRadio(final Panform form, final String title, final List<String> list, final RadioSubmitListener lsn, final int x, final int y) {
		return addRadio(form, title, list, null, lsn, x, y);
	}
	
	private final static RadioGroup addRadio(final Panform form, final String title, final List<String> list, final RadioSubmitListener subLsn, final RadioSubmitListener chgLsn, final int x, final int y) {
		final RadioGroup grp = new RadioGroup(PlatformGame.font, list, subLsn);
		grp.setChangeListener(chgLsn);
		addItem(form, grp, x, y - 16);
		addTitle(form, title, x, y);
		return grp;
	}
	
	private final static ControllerInput addNameInput(final Panform form, final PlayerData pd, final InputSubmitListener subLsn, final int max, final int x, final int y) {
	    final InputSubmitListener chgLsn = new InputSubmitListener() {
            @Override public final void onSubmit(final InputSubmitEvent event) {
                pd.setName(event.toString()); }};
        final ControllerInput in = new ControllerInput(PlatformGame.font, subLsn);
        in.setChangeListener(chgLsn);
        in.setMax(max);
        addItem(form, in, x + 40, y);
        addTitle(form, "Name", x, y);
        in.setLetter();
        return in;
	}
	
	private final static int addLink(final Panform form, final CharSequence txt, final MessageCloseListener lsn, final int x, final int y) {
	    final Message msg = new Message(PlatformGame.font, txt, lsn);
	    msg.getLabel().setUnderlineEnabled(true);
        addItem(form, msg, x, y);
        return x + (8 * (txt.length() + 1));
	}
	
	private final static void addItem(final Panform form, final TextItem item, final int x, final int y) {
		final Pantext lbl = item.getLabel();
		lbl.getPosition().set(x, y);
		lbl.setBackground(Pantext.CHAR_SPACE);
		lbl.setBorderStyle(BorderStyle.Simple);
		form.addItem(item);
	}
	
	private final static Pantext addTitle(final Panform form, final CharSequence title, final int x, final int y) {
		final Pantext tLbl = new Pantext(Pantil.vmid(), PlatformGame.font, title);
		tLbl.getPosition().set(x, y);
		form.getLayer().addActor(tLbl);
		return tLbl;
	}
}
