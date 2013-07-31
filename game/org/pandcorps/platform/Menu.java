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
		protected PlayerContext pc;
		private final boolean fadeIn;
		protected final StringBuilder wrn = new StringBuilder();
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
			final Panroom room = PlatformGame.createRoom(w, PlatformGame.SCREEN_H);
			Pangine.getEngine().setBgColor(new FinPancolor((short) 128, (short) 192, Pancolor.MAX_VALUE));
			
			final TileMap tm = new TileMap(Pantil.vmid(), room, ImtilX.DIM, ImtilX.DIM);
			timg = Level.getTileImage();
			tm.setImageMap(timg);
			final TileMapImage[][] imgMap = tm.splitImageMap();
			tm.fillBackground(imgMap[1][1], 0, 1);
			room.addActor(tm);
			
			if (pc != null) {
				actor = new Panctor();
				actor.setView(pc.guy);
				center = w / 2;
				actor.getPosition().set(center, 16, tm.getForegroundDepth() + 1);
				room.addActor(actor);
			}
			
			form = new Panform();
			wrnLbl = addTitle(form, wrn, center, 64);
			form.setTabListener(new FormTabListener() {@Override public void onTab(final FormTabEvent event) {
				if (allow()) {
					wrn.setLength(0);
				} else {
					event.cancel();
				}
			}});
			menu();
			form.init();
			
			if (fadeIn) {
			    PlatformGame.fadeIn(room, SPEED_MENU_FADE);
			}
		}
		
		@Override
	    protected final void destroy() {
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
			wrn.setLength(0);
        	wrn.append(val);
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
	
	protected final static class SelectScreen extends PlayerScreen {
		private final PlayerContext curr;
		
		protected SelectScreen() {
			this(null, true);
		}
		
		protected SelectScreen(final PlayerContext pc, final boolean fadeIn) {
			super(null, fadeIn);
			curr = pc;
		}
		
		@Override
		protected final void menu() {
			final List<String> list = PlatformGame.getAvailableProfiles();
			if (Coltil.isValued(list)) {
				final RadioSubmitListener prfLsn = new RadioSubmitListener() {
					@Override public final void onSubmit(final RadioSubmitEvent event) {
						if (curr != null) {
							curr.destroy();
						}
						final int index = curr == null ? PlatformGame.pcs.size() : curr.index;
						try {
							PlatformGame.loadProfile(event.toString(), index);
						} catch (final Exception e) {
							throw Pantil.toRuntimeException(e);
						}
						pc = PlatformGame.pcs.get(index);
						goProfile();
				}};
				addRadio(form, "Pick Profile", list, prfLsn, null, 8, 184);
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
                    pc = PlatformGame.newPlayerContext(prf, curr == null ? PlatformGame.pcs.size() : curr.index);
                    PlatformGame.reloadAnimalStrip(pc);
                    Panscreen.set(new NewScreen(pc, false)); }};
			addLink(form, "New", newLsn, 8, 128);
			if (curr != null) {
				addExit("Cancel", 40, 128);
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
	        addNameInput(form, curr.profile, namLsn, 8, 112); //TODO validation length, unique, submit link
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
			final RadioGroup avtGrp = addRadio(form, "Pick Avatar", avatars, avtLsn, 8, 184);
			avtGrp.setSelected(avatars.indexOf(pc.profile.currentAvatar.getName()));
			final MessageCloseListener edtLsn = new MessageCloseListener() {
                @Override public final void onClose(final MessageCloseEvent event) {
                    if (disabled) {
                        return;
                    }
                    goAvatar(); }};
            int x = 8;
            x = addLink(form, "Edit", edtLsn, x, 112);
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
            x = addLink(form, "New", newLsn, x, 112);
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
	            x = addLink(form, "Erase", delLsn, x, 112);
            }
			addExit("Exit", x, 112); //TODO Label Start if in Menu before seeing Map
			final MessageCloseListener prfLsn = new MessageCloseListener() {
                @Override public final void onClose(final MessageCloseEvent event) {
                    if (disabled) {
                        return;
                    }
                    Panscreen.set(new SelectScreen(pc, false)); }};
            addLink(form, "Pick Profile", prfLsn, 8, 96);
			// Rename Profile //TODO
			// Drop out (if other players? if not player 1?)
            // Exit to title (if player 1)
            // Delete Profile (if player 1)
            // Make default Profile (if player 1)
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
			final RadioGroup anmGrp = addRadio(form, "Animal", animals, anmLsn, 8, 184);
			final int numEyes = PlatformGame.getNumEyes();
			final ArrayList<String> eyes = new ArrayList<String>(numEyes);
			for (int i = 1; i <= numEyes; i++) {
			    eyes.add(Integer.toString(i));
			}
			final AvtListener eyeLsn = new AvtListener() {
				@Override public final void update(final String value) {
					avt.eye = Integer.parseInt(value); }};
			final RadioGroup eyeGrp = addRadio(form, "Eye", eyes, eyeLsn, 80, 184);
			final List<String> colors = Arrays.asList("0", "1", "2", "3", "4");
			final AvtListener redLsn = new ColorListener() {
				@Override public final void update(final float value) {
					avt.r = value; }};
			final RadioGroup redGrp = addRadio(form, "Red", colors, redLsn, 112, 184);
			final AvtListener greenLsn = new ColorListener() {
				@Override public final void update(final float value) {
					avt.g = value; }};
			final RadioGroup grnGrp = addRadio(form, "Grn", colors, greenLsn, 144, 184);
			final AvtListener blueLsn = new ColorListener() {
				@Override public final void update(final float value) {
					avt.b = value; }};
			final RadioGroup bluGrp = addRadio(form, "Blu", colors, blueLsn, 176, 184);
			final ControllerInput namIn = addNameInput(form, avt, null, 8, 112);
			addExit("Save", 8, 96);
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
            addLink(form, "Cancel", canLsn, 48, 96);
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
	
	private final static ControllerInput addNameInput(final Panform form, final PlayerData pd, final InputSubmitListener subLsn, final int x, final int y) {
	    final InputSubmitListener chgLsn = new InputSubmitListener() {
            @Override public final void onSubmit(final InputSubmitEvent event) {
                pd.setName(event.toString()); }};
        final ControllerInput in = new ControllerInput(PlatformGame.font, subLsn);
        in.setChangeListener(chgLsn);
        in.setMax(PlatformGame.MAX_NAME_AVATAR);
        addItem(form, in, x + 40, y);
        addTitle(form, "Name", x, y);
        in.setLetter();
        return in;
	}
	
	private final static int addLink(final Panform form, final String txt, final MessageCloseListener lsn, final int x, final int y) {
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
