/*
Copyright (c) 2009-2020, Andrew M. Martin
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
package org.pandcorps.board;

import org.pandcorps.board.BoardGame.*;
import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Input.*;
import org.pandcorps.pandax.touch.*;

public class Menu {
    private final static int buttonLeft = 4;
    private final static int buttonBottom = 4;
    private final static int textLeft = 24;
    private final static int pairOffsetButton = 4;
    private final static int nameMaxCharacters = 8;
    private final static int maxSavedGamesPerModule = 5;
    private static int profileY = 0;
    private static BoardGamePlayer player;
    private static BoardGameProfile profile;
    private static Variable<Pancolor> color;
    
    protected final static void goModule() {
        Panscreen.set(new ModuleScreen());
    }
    
    protected final static class ModuleScreen extends BaseScreen {
        @Override
        protected final void afterBaseLoad() {
            final int d = BoardGame.DIM;
            final int left = buttonLeft + (d * 2), right = getButtonRight() - (d * 3);
            final int bottom = buttonBottom, top = getButtonTop() - d;
            final Panmage hi = new AdjustedPanmage(Pantil.vmid(), BoardGame.square, Pancolor.CYAN);
            final Panmage white = BoardGame.square;
            final Panmage black = new AdjustedPanmage(Pantil.vmid(), BoardGame.square, BoardGame.BLACK);
            final Panmage red = new AdjustedPanmage(Pantil.vmid(), BoardGame.square, Pancolor.RED);
            final Panmage green = new AdjustedPanmage(Pantil.vmid(), BoardGame.square, Pancolor.GREEN);
            final Panmage blue = new AdjustedPanmage(Pantil.vmid(), BoardGame.verticalSquare, FourInARowModule.COLOR_BG);
            final Panmage circleWhite = BoardGame.circle;
            final Panmage circleBlack = new AdjustedPanmage(Pantil.vmid(), BoardGame.circle, BoardGame.BLACK);
            final Panmage circleRed = new AdjustedPanmage(Pantil.vmid(), BoardGame.circle, Pancolor.RED);
            final Panmage vertRed = new AdjustedPanmage(Pantil.vmid(), BoardGame.verticalCircle, Pancolor.RED);
            final Panmage vertYellow = new AdjustedPanmage(Pantil.vmid(), BoardGame.verticalCircle, Pancolor.YELLOW);
            final Panmage queenWhite = BoardGame.queen;
            final Panmage kingWhite = BoardGame.king;
            final Panmage queenBlack = new AdjustedPanmage(Pantil.vmid(), BoardGame.queen, BoardGame.BLACK);
            final Panmage kingBlack = new AdjustedPanmage(Pantil.vmid(), BoardGame.king, BoardGame.BLACK);
            addModule(left, top, black, circleBlack, red, null, red, null, black, circleRed, hi, BoardGame.CHECKERS);
            addModule(left, bottom, blue, vertYellow, blue, vertRed, blue, vertYellow, blue, vertRed, hi, BoardGame.FOUR_IN_A_ROW);
            addModule(right, top, white, queenWhite, black, kingWhite, black, queenBlack, white, kingBlack, hi, BoardGame.CHESS);
            addModule(right, bottom, green, circleBlack, green, circleWhite, green, circleWhite, green, circleBlack, hi, BoardGame.OTHELLO);
            addExit(new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    goModule();
                }});
        }
    }
    
    protected final static void addModule(final int x, final int y,
            final Panmage bg0, final Panmage fg0, final Panmage bg1, final Panmage fg1, final Panmage bg2, final Panmage fg2, final Panmage bg3, final Panmage fg3,
            final Panmage hi, final BoardGameModule<?> module) {
        final String name = module.getName();
        final int d = BoardGame.DIM, xd = x + d, yd = y + d;
        final ActionEndListener listener = newModuleListener(module);
        addButton(name + "0", x, y, bg0, hi, fg0, listener);
        addButton(name + "1", xd, y, bg1, hi, fg1, listener);
        addButton(name + "2", x, yd, bg2, hi, fg2, listener);
        addButton(name + "3", xd, yd, bg3, hi, fg3, listener);
    }
    
    protected final static ActionEndListener newModuleListener(final BoardGameModule<?> module) {
        return new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) {
                BoardGame.module = module;
                BoardGame.goGame();
            }};
    }
    
    protected final static void goMenu() {
        Panscreen.set(new MenuScreen());
    }
    
    protected final static void addBackListener(final TouchButton btn, final ActionEndListener listener) {
        addBackListener(btn.getActor(), listener);
    }
    
    protected final static void addBackListener(final Panctor actor, final ActionEndListener listener) {
        final Panteraction interaction = Pangine.getEngine().getInteraction();
        actor.register(interaction.BACK, listener);
        actor.register(interaction.KEY_ESCAPE, listener);
    }
    
    protected final static class MenuScreen extends BaseScreen {
        @Override
        protected final void afterBaseLoad() {
            player = null;
            profile = null;
            profileY = 0;
            for (int i = BoardGame.module.numPlayers - 1; i >= 0; i--) {
                addProfile(BoardGame.module.players[i]);
            }
            final int top = getButtonTop();
            addPair(top - pairOffsetButton, "Save Game", BoardGame.imgSave, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    goSaveGame();
                }});
            addPair(top - pairOffsetButton - 24, "Load Game", BoardGame.imgOpen, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    goLoadGame();
                }});
            addPair(top - pairOffsetButton - 48, "Change Game", BoardGame.imgMenu, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    goModule();
                }});
            addDone(false, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    BoardGame.goGame();
                }});
            addExit(new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    goMenu();
                }});
        }
    }
    
    protected final static void addExit(final ActionEndListener noListener) {
        final ActionEndListener exitListener = new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) {
                goPrompt(
                        "Exit?", true,
                        new ActionEndListener() {
                            @Override public final void onActionEnd(final ActionEndEvent event) {
                                Pangine.getEngine().exit();
                            }},
                        noListener);
            }};
        final TouchButton btnExit = addTopRight("Exit", BoardGame.imgExit, exitListener);
        addBackListener(btnExit, exitListener);
    }
    
    protected final static void addDone(final ActionEndListener listener) {
        addDone(true, listener);
    }
    
    protected final static void addDone(final boolean backButton, final ActionEndListener listener) {
        final TouchButton btn = addButton("Done", getButtonRight(), buttonBottom, BoardGame.imgDone, listener);
        if (backButton) {
            addBackListener(btn, listener);
        }
    }
    
    protected final static void addDoneGoMenu() {
        addDone(new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) {
                goMenu();
            }});
    }
    
    protected final static TouchButton addTopRight(final String name, final Panmage img, final ActionEndListener listener) {
        return addButton(name, getButtonRight(), getButtonTop(), img, listener);
    }
    
    protected final static int getButtonRight() {
        return Pangine.getEngine().getEffectiveWidth() - 20;
    }
    
    protected final static int getButtonTop() {
        return Pangine.getEngine().getEffectiveHeight() - 20;
    }
    
    protected final static void addProfile(final BoardGamePlayer player) {
        final BoardGameProfile profile = player.profile;
        addPair(profileY, profile.name, BoardGame.imgEdit, new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) {
                Menu.player = player;
                Menu.profile = profile;
                goProfile();
            }});
        profileY += 24;
    }
    
    protected final static TouchButton addPair(final int y, final String name, final Panmage img, final ActionEndListener listener) {
        addText(textLeft, y + 8, name);
        return addButton("Edit." + name, buttonLeft, y + pairOffsetButton, img, listener);
    }
    
    protected final static void addPair(final int y, final String name, final Panmage img, final boolean enabled, final ActionEndListener listener) {
        setEnabled(addPair(y, name, img, listener), enabled);
    }
    
    protected final static TouchButton addButton(final String name, final int x, final int y, final Panmage img, final ActionEndListener listener) {
        return addButton(name, x, y, BoardGame.square, getSquareActive(), img, listener);
    }
    
    protected final static TouchButton addButton(final String name, final int x, final int y,
            final Panmage baseInactive, final Panmage baseActive,final Panmage img, final ActionEndListener listener) {
        final Panroom room = Pangame.getGame().getCurrentRoom();
        final TouchButton button = new TouchButton(null, room, name, x, y, BoardGame.DEPTH_CELL, baseInactive, baseActive, img, 0, 0, null, null, 0, 0, true);
        Pangine.getEngine().registerTouchButton(button);
        button.getActor().register(button, listener);
        return button;
    }
    
    protected final static TouchButton addButton(final String name, final int x, final int y, final Panmage img,
            final boolean enabled, final ActionEndListener listener) {
        return setEnabled(addButton(name, x, y, img, listener), enabled);
    }
    
    protected final static TouchButton setEnabled(final TouchButton btn, final boolean enabled) {
        btn.setImageDisabled(BoardGame.squareBlack);
        btn.setEnabled(enabled);
        return btn;
    }
    
    protected final static Panmage getSquareActive() {
        final BoardGameProfile profile = getProfile();
        if (profile == null) {
            return getSquareActiveDefault();
        }
        final Panmage active1 = getSquareActive(profile.color1);
        if (active1 != null) {
            return active1;
        }
        final Panmage active2 = getSquareActive(profile.color2);
        if (active2 != null) {
            return active2;
        }
        return getSquareActiveDefault();
    }
    
    protected final static Panmage getSquareActiveDefault() {
        return getSquare(Pancolor.CYAN);
    }
    
    protected final static Panmage getSquareActive(final Pancolor color) {
        return isAllowedActiveColor(color) ? getSquare(color) : null;
    }
    
    protected final static Panmage getSquare(final Pancolor color) {
        return new AdjustedPanmage(Pantil.vmid(), BoardGame.square, color);
    }
    
    protected final static boolean isAllowedActiveColor(final Pancolor color) {
        return (color != null) && !color.equals(Pancolor.WHITE) && !color.equals(BoardGame.BLACK);
    }
    
    protected final static BoardGameProfile getProfile() {
        if ((profile == null) && (BoardGame.module != null)) {
            return BoardGame.module.players[0].profile;
        }
        return profile;
    }
    
    protected final static Pantext addText(final int x, final int y, final String value) {
        final Pantext text = new Pantext(Pantil.vmid(), BoardGame.font, value);
        text.getPosition().set(x, y, BoardGame.DEPTH_CELL);
        Pangame.getGame().getCurrentRoom().addActor(text);
        return text;
    }
    
    protected final static void goProfile() {
        Panscreen.set(new ProfileScreen());
    }
    
    protected final static void goProfile(final BoardGameProfile profile) {
        player.profile = Menu.profile = profile;
        goProfile();
    }
    
    protected final static class ProfileScreen extends BaseScreen {
        @Override
        protected final void afterBaseLoad() {
            addPair(getButtonTop() - pairOffsetButton, profile.name, BoardGame.imgEdit, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    Panscreen.set(new NameScreen());
                }});
            final int bottom = buttonBottom - pairOffsetButton;
            addColor(bottom + 32, "Primary", new Variable<Pancolor>() {
                @Override public final Pancolor get() { return profile.color1; }
                @Override public final void set(final Pancolor t) { profile.color1 = t; }});
            addColor(bottom, "Alternate", new Variable<Pancolor>() {
                @Override public final Pancolor get() { return profile.color2; }
                @Override public final void set(final Pancolor t) { profile.color2 = t; }});
            addDoneGoMenu();
            if (BoardGame.getActiveProfilesSize() <= 2) {
                addNewProfileButton();
            } else {
                addTopRight("Load", BoardGame.imgOpen, new ActionEndListener() {
                    @Override public final void onActionEnd(final ActionEndEvent event) {
                        goProfileSelect(0);
                    }});
            }
        }
    }
    
    protected final static void addNewProfileButton() {
        addTopRight("New", BoardGame.imgPlus, new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) {
                newProfile();
            }});
    }
    
    protected final static void addColor(final int y, final String name, final Variable<Pancolor> color) {
        addPair(y, name, getImage(color.get(), BoardGame.imgEdit), new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) {
                Menu.color = color;
                Panscreen.set(new ColorScreen());
            }});
    }
    
    protected final static Panmage getImage(final Pancolor color, final Panmage def) {
        return (color == null) ? def : new AdjustedPanmage(Pantil.vmid(), BoardGame.circle, color);
    }
    
    protected final static class ColorScreen extends BaseScreen {
        @Override
        protected final void afterBaseLoad() {
            final Pangine engine = Pangine.getEngine();
            final int w = engine.getEffectiveWidth(), h = engine.getEffectiveHeight();
            final int x1 = w / 6, x2 = w * 2 / 6, x3 = w * 3 / 6, x4 = w * 4 / 6, x5 = w * 5 / 6;
            final int y2 = h / 3, y1 = h * 2 / 3;
            addColorOption(x1, y1, Pancolor.WHITE);
            addColorOption(x1, y2, BoardGame.BLACK);
            addColorOption(x2, y1, Pancolor.RED);
            addColorOption(x2, y2, BoardGame.ORANGE);
            addColorOption(x3, y1, Pancolor.YELLOW);
            addColorOption(x3, y2, Pancolor.GREEN);
            addColorOption(x4, y1, Pancolor.CYAN);
            addColorOption(x4, y2, Pancolor.BLUE);
            addColorOption(x5, y1, Pancolor.MAGENTA);
            addColorOption(x5, y2, null);
            addDone(new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    goProfile();
                }});
        }
    }
    
    protected final static void addColorOption(final int x, final int y, final Pancolor option) {
        addButton(Chartil.toString(option), x - 8, y - 8, getImage(option, null), new ActionEndListener() {
            @Override public final void onActionEnd(final ActionEndEvent event) {
                color.set(option);
                finishProfileChange();
            }});
    }
    
    protected final static void finishProfileChange() {
        profile.save();
        goProfile();
    }
    
    protected final static class NameScreen extends BaseScreen {
        @Override
        protected final void afterBaseLoad() {
            new TouchKeyboard(BoardGame.square, getSquareActive(), BoardGame.font);
            final Panform form = new Panform(ControlScheme.getDefaultKeyboard());
            final Input input = new KeyInput(BoardGame.font, new InputSubmitListener() {
                @Override public final void onSubmit(final InputSubmitEvent event) {
                    changeName(event.toString());
                }});
            addDone(new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    changeName(input.getText());
                }});
            input.setMax(nameMaxCharacters);
            final Pantext lbl = input.getLabel();
            lbl.getPosition().set(Pangine.getEngine().getEffectiveWidth() / 2 - 32, 4);
            input.append(profile.name);
            form.addItem(input);
            form.init();
        }
        
        protected final void changeName(final String name) {
            profile.changeName(name);
            finishProfileChange();
        }
    }
    
    private final static void goProfileSelect(final int firstIndex) {
        ProfileSelectScreen.firstIndex = firstIndex;
        Panscreen.set(new ProfileSelectScreen());
    }
    
    protected final static class ProfileSelectScreen extends BaseScreen {
        private final static int profilesPerPage = 4;
        private static int firstIndex = 0;
        
        @Override
        protected final void afterBaseLoad() {
            final int size = BoardGame.getActiveProfilesSize();
            final int h = Pangine.getEngine().getEffectiveHeight();
            final int xDelete = buttonLeft + 24 + (8 * nameMaxCharacters);
            for (int i = 0; i < profilesPerPage; i++) {
                final int currentIndex = firstIndex + i;
                if (currentIndex >= size) {
                    break;
                }
                final BoardGameProfile currentProfile = BoardGame.getActiveProfile(currentIndex);
                final int y = h - (24 * (i + 1));
                final String name = currentProfile.name;
                addPair(y, name, BoardGame.imgOpen, new ActionEndListener() {
                    @Override public final void onActionEnd(final ActionEndEvent event) {
                        for (final BoardGamePlayer otherPlayer : BoardGame.module.players) {
                            if (otherPlayer.equals(player)) {
                                continue;
                            } else if (otherPlayer.profile.profileIndex == currentIndex) {
                                /*
                                If the new profile picked for this player matches the other player's current profile,
                                then swap the profiles.
                                Set the other player's profile to this player's profile.
                                Then this player's profile will be set to the select profile below as always.
                                */
                                otherPlayer.profile = profile;
                                break;
                            }
                        }
                        goProfile(currentProfile);
                    }});
                addButton(name + ".delete", xDelete, y + pairOffsetButton, BoardGame.imgDelete, new ActionEndListener() {
                    @Override public final void onActionEnd(final ActionEndEvent event) {
                        goPrompt(
                                "Delete " + currentProfile.name + "?",
                                new ActionEndListener() {
                                    @Override public final void onActionEnd(final ActionEndEvent event) {
                                        currentProfile.delete();
                                        goProfileSelect(0);
                                    }},
                                new ActionEndListener() {
                                    @Override public final void onActionEnd(final ActionEndEvent event) {
                                        goProfileSelect(firstIndex);
                                    }});
                    }});
            }
            addButton("Back", buttonLeft, buttonBottom, BoardGame.imgUndo, firstIndex > 0, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    goProfileSelect(firstIndex - profilesPerPage);
                }});
            addButton("Forward", xDelete, buttonBottom, BoardGame.imgRedo, (firstIndex + profilesPerPage) < size, new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    goProfileSelect(firstIndex + profilesPerPage);
                }});
            addDone(new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    goProfile();
                }});
            addNewProfileButton();
        }
    }
    
    protected final static void newProfile() {
        goProfile(BoardGame.newProfile());
    }
    
    protected final static void goPrompt(final String label, final ActionEndListener yesListener, final ActionEndListener noListener) {
        goPrompt(label, false, yesListener, noListener);
    }
    
    protected final static void goPrompt(final String label, final boolean backTreatedAsYes, final ActionEndListener yesListener, final ActionEndListener noListener) {
        Panscreen.set(new PromptScreen(label, backTreatedAsYes, yesListener, noListener));
    }
    
    protected final static class PromptScreen extends BaseScreen {
        private final String label;
        private final boolean backTreatedAsYes;
        private final ActionEndListener yesListener;
        private final ActionEndListener noListener;
        
        protected PromptScreen(final String label, final boolean backTreatedAsYes, final ActionEndListener yesListener, final ActionEndListener noListener) {
            this.label = label;
            this.backTreatedAsYes = backTreatedAsYes;
            this.yesListener = yesListener;
            this.noListener = noListener;
        }
        
        @Override
        protected final void afterBaseLoad() {
            final Pangine engine = Pangine.getEngine();
            final int w2 = (engine.getEffectiveWidth() / 2), x = w2 - 8;
            final int y = (engine.getEffectiveHeight() / 2) - 16;
            addText(w2, y + 24, label).centerX();
            final TouchButton btnYes = addButton("Yes", x - 16, y, BoardGame.imgDone, yesListener);
            addButton("No", x + 16, y, BoardGame.imgDelete, noListener);
            addBackListener(btnYes, backTreatedAsYes ? yesListener : noListener);
        }
    }
    
    protected final static void goSaveGame() {
        Panscreen.set(new SaveScreen(BoardGame.imgSave, true, new SaveGameHandler() {
            @Override public final void handle(final String fileName) {
                BoardGame.save(fileName);
                goSaveGame();
            }}));
    }
    
    protected final static void goLoadGame() {
        Panscreen.set(new SaveScreen(BoardGame.imgOpen, false, new SaveGameHandler() {
            @Override public final void handle(final String fileName) {
                load(fileName);
            }}));
    }
    
    protected final static void load(final String fileName) {
        try {
            BoardGame.module.load(fileName);
            BoardGame.goGame();
        } catch (final Exception e) {
            Iotil.delete(fileName);
            goLoadGame();
        }
    }
    
    protected final static class SaveScreen extends BaseScreen {
        private final Panmage img;
        private final boolean handleEmptyAllowed;
        private final SaveGameHandler handler;
        
        protected SaveScreen(final Panmage img, final boolean handleEmptyAllowed, final SaveGameHandler handler) {
            this.img = img;
            this.handler = handler;
            this.handleEmptyAllowed = handleEmptyAllowed;
        }
        
        @Override
        protected final void afterBaseLoad() {
            final BoardGameModule<?> module = BoardGame.module;
            final String moduleName = module.getName();
            final int top = getButtonTop() - pairOffsetButton;
            for (int i = 0; i < maxSavedGamesPerModule; i++) {
                final String fileName = moduleName + i + BoardGame.EXT_SAVE;
                final String parsed = parseGameContext(fileName);
                final String label = Chartil.nvl(parsed, "Empty");
                addPair(top - (26 * i), label, img, handleEmptyAllowed || (parsed != null), new ActionEndListener() {
                    @Override public final void onActionEnd(final ActionEndEvent event) {
                        handler.handle(fileName);
                    }});
            }
            addDoneGoMenu();
        }
        
        protected final static String parseGameContext(final String loc) {
            if (!Iotil.exists(loc)) {
                return null;
            }
            SegmentStream in = null;
            try {
                in = BoardGame.openSegmentStream(loc);
                final BoardGameContext context = BoardGame.module.parseGameContext(in);
                final StringBuilder b = new StringBuilder();
                for (final int profileIndex : context.profileIndices) {
                    final BoardGameProfile profile = BoardGame.profiles.get(profileIndex);
                    if (profile.deleted) {
                        return null;
                    } else if (b.length() > 0) {
                        b.append('/');
                    }
                    b.append(profile.name);
                }
                return b.toString();
            } catch (final Exception e) {
                return null;
            } finally {
                in.close();
            }
        }
    }
    
    protected static interface SaveGameHandler {
        public void handle(final String fileName);
    }
}
