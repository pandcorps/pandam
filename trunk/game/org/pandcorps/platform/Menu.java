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
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandax.in.ControlScheme;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.platform.Player.*;

public class Menu {
    private final static short SPEED_MENU_FADE = 9;
    private final static int SIZE_FONT = 8;
    private final static String NAME_NEW = "org.pandcorps.new";
    private final static String WARN_DELETE = "Press Erase again to confirm";
    private final static String WARN_EMPTY = "Must have a name";
    private final static String WARN_DUPLICATE = "Name already used";
    private final static String INFO_SAVED = "Saved images";
    
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
			Level.tm = tm;
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
			infLbl = addTitle(inf, center, getBottom());
			form.setTabListener(new FormTabListener() {@Override public void onTab(final FormTabEvent event) {
				if (allow(event.getFocused())) {
					inf.setLength(0);
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
		
		protected final RadioGroup addRadio(final String title, final List<String> list, final RadioSubmitListener lsn, final int x, final int y) {
			return addRadio(title, list, null, lsn, x, y);
		}
		
		protected final RadioGroup addRadio(final String title, final List<String> list, final RadioSubmitListener subLsn, final RadioSubmitListener chgLsn, final int x, final int y) {
			final RadioGroup grp = new RadioGroup(PlatformGame.font, list, subLsn);
			grp.setChangeListener(chgLsn);
			addItem(grp, x, y - 16);
			addTitle(title, x, y);
			final Pantext label = grp.getLabel();
			label.setLinesPerPage(5);
			label.stretchCharactersPerLineToFit();
			return grp;
		}
		
		protected final ControllerInput addNameInput(final PlayerData pd, final InputSubmitListener subLsn, final int max, final int x, final int y) {
		    final InputSubmitListener chgLsn = new InputSubmitListener() {
	            @Override public final void onSubmit(final InputSubmitEvent event) {
	                pd.setName(event.toString()); }};
	        final ControllerInput in = new ControllerInput(PlatformGame.font, subLsn);
	        in.setChangeListener(chgLsn);
	        in.setMax(max);
	        addItem(in, x + 40, y);
	        addTitle("Name", x, y);
	        in.setLetter();
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
		
		protected final Pantext addTitle(final CharSequence title, final int x, final int y) {
			return addTitle(new Pantext(Pantil.vmid(), PlatformGame.font, title), x, y);
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
		    disabled = true;
		    onExit();
		}
		
		protected final void save() {
		    pc.profile.serialize();
		}
		
		protected abstract void onExit();
		
		protected final void setInfo(final String val) {
			Chartil.set(inf, val);
        	infLbl.getPosition().setX(center);
        	infLbl.centerX();
		}
		
		protected final int addExit(final String title, final int x, final int y) {
			final MessageCloseListener savLsn = new MessageCloseListener() {
				@Override public final void onClose(final MessageCloseEvent event) {
				    if (disabled) {
	                    return;
	                }
					exit(); }};
			return addLink(title, savLsn, x, y);
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
				actor.load(pc);
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
	        final Pantext text = addTitle("Press anything", center, getBottom());
	        text.centerX();
	        text.register(new ActionStartListener() {@Override public void onActionStart(final ActionStartEvent event) {
	        	if (disabled) {
	        		return;
	        	}
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
	        /*final Panmage tmpImg = Pangine.getEngine().createImage("img.tmp", "Tall.png"); // Must destroy
	        final Panctor tmp = new Panctor();
	        tmp.setView(tmpImg);
	        System.out.println(tmpImg.getSize());
	        tmp.getPosition().set(8, 24);
	        room.addActor(tmp);*/
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
			final int left = getLeft();
			int x = left, y = getTop();
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
				addRadio("Pick Profile", list, prfLsn, null, x, y);
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
            y -= 64;
			x = addLink("New", newLsn, left, y);
			if (curr != null) {
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
            int x = getLeft(), y = getTop() - 72;
	        addNameInput(curr.profile, namLsn, PlatformGame.MAX_NAME_PROFILE, x, y); //TODO validation unique, submit link
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
			final int left = getLeft();
			int x = left, y = getTop();
			final RadioGroup avtGrp = addRadio("Pick Avatar", avatars, avtLsn, x, y);
			avtGrp.setSelected(avatars.indexOf(pc.profile.currentAvatar.getName()));
			final MessageCloseListener edtLsn = new MessageCloseListener() {
                @Override public final void onClose(final MessageCloseEvent event) {
                    if (disabled) {
                        return;
                    }
                    goAvatar(); }};
            y -= 64;
            x = addLink("Edit", edtLsn, left, y);
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
                    actor.load(pc);
                    goAvatar(); }};
            x = addPipe(x, y);
            x = addLink("New", newLsn, x, y);
            if (pc.profile.avatars.size() > 1) {
	            final MessageCloseListener delLsn = new MessageCloseListener() {
	                @Override public final void onClose(final MessageCloseEvent event) {
	                    if (disabled) {
	                        return;
	                    } else if (pc.profile.avatars.size() == 1) {
	                    	return;
	                    }
	                    if (!inf.toString().equals(WARN_DELETE)) {
	                    	setInfo(WARN_DELETE);
	                    	return;
	                    }
	                    inf.setLength(0);
	                    pc.profile.avatars.remove(pc.profile.currentAvatar);
	                    pc.profile.currentAvatar = pc.profile.avatars.get(0);
	                    PlatformGame.reloadAnimalStrip(pc);
	                    actor.load(pc);
	                    save = true;
	                    goProfile(); }};
	            x = addPipe(x, y);
	            x = addLink("Erase", delLsn, x, y);
            }
			final MessageCloseListener prfLsn = new MessageCloseListener() {
                @Override public final void onClose(final MessageCloseEvent event) {
                    if (disabled) {
                        return;
                    }
                    Panscreen.set(new SelectScreen(pc, false)); }};
            y -= 16;
            x = left;
            addTitle("Profile", x, y);
            y -= 16;
            x = left + 8;
            x = addLink("Pick", prfLsn, x, y);
            final MessageCloseListener infLsn = new MessageCloseListener() {
                @Override public final void onClose(final MessageCloseEvent event) {
                    if (disabled) {
                        return;
                    }
                    Panscreen.set(new InfoScreen(pc)); }};
            x = addPipe(x, y);
            x = addLink("Info", infLsn, x, y);
            if (pc.index == 0) {
            	x = addPipe(x, y);
                addTitle("Default", x, y);
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
                x += 64;
                addLink(defStr, defLsn, x, y);
            }
            y -= 16;
            x = left;
            addTitle("Game", x, y);
            y -= 16;
            x = left + 8;
            x = addExit(Map.started ? "Back" : "Play", x, y);
            if (pc.index == 0) {
                final MessageCloseListener qutLsn = new MessageCloseListener() {
                    @Override public final void onClose(final MessageCloseEvent event) {
                        if (disabled) {
                            return;
                        }
                        Pangine.getEngine().exit(); }}; // Exit to TitleScreen instead? Quit game from there? Or separate Reset link?
                x = addPipe(x, y);
                addLink("Quit", qutLsn, x, y);
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
			final int left = getLeft();
			int x = left, y = getTop();
			final RadioGroup anmGrp = addRadio("Animal", animals, anmLsn, x, y);
			final int numEyes = PlatformGame.getNumEyes();
			final ArrayList<String> eyes = new ArrayList<String>(numEyes);
			for (int i = 1; i <= numEyes; i++) {
			    eyes.add(Integer.toString(i));
			}
			final AvtListener eyeLsn = new AvtListener() {
				@Override public final void update(final String value) {
					avt.eye = Integer.parseInt(value); }};
			x += 72;
			final RadioGroup eyeGrp = addRadio("Eye", eyes, eyeLsn, x, y);
			final List<String> colors = Arrays.asList("0", "1", "2", "3", "4");
			final AvtListener redLsn = new ColorListener() {
				@Override public final void update(final float value) {
					avt.r = value; }};
			x += 32;
			final RadioGroup redGrp = addRadio("Red", colors, redLsn, x, y);
			final AvtListener greenLsn = new ColorListener() {
				@Override public final void update(final float value) {
					avt.g = value; }};
			x += 32;
			final RadioGroup grnGrp = addRadio("Grn", colors, greenLsn, x, y);
			final AvtListener blueLsn = new ColorListener() {
				@Override public final void update(final float value) {
					avt.b = value; }};
			x += 32;
			final RadioGroup bluGrp = addRadio("Blu", colors, blueLsn, x, y);
			y -= 64;
			x = left;
			final MessageCloseListener gearLsn = new MessageCloseListener() {
                @Override public final void onClose(final MessageCloseEvent event) {
                    if (disabled) {
                        return;
                    }
                    Panscreen.set(new GearScreen(pc, old, avt)); }};
			addLink("Gear", gearLsn, x, y);
			y -= 16;
			final ControllerInput namIn = addNameInput(avt, null, PlatformGame.MAX_NAME_AVATAR, x, y);
			y -= 16;
			x = addExit("Save", left, y);
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
                    actor.load(pc);
                    save = false;
                    exit(); }};
            x = addPipe(x, y);
            x = addLink("Cancel", canLsn, x, y);
            final MessageCloseListener expLsn = new MessageCloseListener() {
                @Override public final void onClose(final MessageCloseEvent event) {
                    if (disabled) {
                        return;
                    }
                    final Pangine engine = Pangine.getEngine();
                    engine.setImageSavingEnabled(true);
                    PlatformGame.reloadAnimalStrip(pc);
                    engine.setImageSavingEnabled(false);
                    setInfo(INFO_SAVED);
                    actor.load(pc); }};
            x = addPipe(x, y);
            x = addLink("Export", expLsn, x, y);
			anmGrp.setSelected(animals.indexOf(avt.anm));
			eyeGrp.setSelected(avt.eye - 1);
			redGrp.setSelected(getLineColor(avt.r));
			grnGrp.setSelected(getLineColor(avt.g));
			bluGrp.setSelected(getLineColor(avt.b));
			namIn.append(avt.getName());
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
	
	protected final static class GearScreen extends PlayerScreen {
	    private final Avatar old;
        private final Avatar avt;
        
        protected GearScreen(final PlayerContext pc, final Avatar old, final Avatar avt) {
            super(pc, false);
            this.old = old;
            this.avt = avt;
        }
        
        @Override
        protected final void menu() throws Exception {
            final int left = getLeft();
            int y = getTop();
            final JumpMode[] jumpModes = Player.JumpMode.values();
            final List<String> jmps = new ArrayList<String>(jumpModes.length);
            for (final JumpMode jm : jumpModes) {
                jmps.add(jm.getName());
            }
            final AvtListener jmpLsn = new AvtListener() {
                @Override public final void update(final String value) {
                    avt.jumpMode = Player.get(jumpModes, value).getIndex(); }};
            addRadio("Jump Mode", jmps, jmpLsn, left, y);
            //TODO Set correct radio line for current mode
            y -= 64;
            addExit("Back", left, y);
        }
        
        @Override
        protected void onExit() {
            Panscreen.set(new AvatarScreen(pc, old, avt));
        }
	}
	
	protected final static class InfoScreen extends PlayerScreen {
	    private RadioGroup achRadio = null;
	    private final StringBuilder achDesc = new StringBuilder();
	    
        protected InfoScreen(final PlayerContext pc) {
            super(pc, false);
        }
        
        @Override
        protected final void menu() throws Exception {
            final int total = Achievement.ALL.length;
            final StringBuilder b = new StringBuilder();
            final List<String> ach = new ArrayList<String>(total);
            for (int i = 0; i < total; i++) {
                Chartil.clear(b);
                b.append(pc.profile.achievements.contains(Integer.valueOf(i)) ? (char) 2 : ' ').append(' ');
                b.append(Achievement.ALL[i].getName());
                ach.add(b.toString());
            }
            final RadioSubmitListener achLsn = new RadioSubmitListener() {
                @Override public final void onSubmit(final RadioSubmitEvent event) {
                    setAchDesc(event.toString());
            }};
            final int left = getLeft();
            int y = getTop();
            achRadio = addRadio("Achievements", ach, achLsn, left, y);
            y -= 64;
            addTitle(new Pantext(Pantil.vmid(), PlatformGame.fontTiny, achDesc), left, y);
            y -= 16;
            addRadio("Statistics", pc.profile.stats.toList(), null, left, y);
            y -= 64;
            addExit("Back", left, y);
            initAchDesc();
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
            goProfile();
        }
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
