/*
Copyright (c) 2009-2018, Andrew M. Martin
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

import org.pandcorps.botsnbolts.Boss.*;
import org.pandcorps.botsnbolts.Menu.*;
import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;

public class Story {
    protected final static TextTyper newTextTyper(final CharSequence msg, final int charactersPerLine) {
        final TextTyper typer = new TextTyper(BotsnBoltsGame.font, msg, charactersPerLine).enableWaitForInput();
        typer.setLinesPerPage(6);
        typer.setGapY(2);
        typer.getPosition().setZ(BotsnBoltsGame.DEPTH_HUD_TEXT);
        return typer;
    }
    
    protected final static DialogueBox dialogue(final Panmage portrait, final boolean portraitLeft, final CharSequence msg) {
        final TextTyper typer = newTextTyper(msg, 29);
        final DialogueBox box = new DialogueBox(typer, portrait, portraitLeft);
        typer.getPosition().set(box.xText + 12, 197);
        typer.setFinishHandler(new Runnable() {
            @Override public final void run() {
                box.finish();
            }});
        return box;
    }
    
    protected final static class DialogueBox extends Panctor {
        protected final TextTyper typer;
        private final Panmage portrait;
        private final boolean portraitLeft;
        private final int xText;
        private final int xPortrait;
        private Runnable finishHandler = null;
        
        protected DialogueBox(final TextTyper typer, final Panmage portrait, final boolean portraitLeft) {
            this.typer = typer;
            this.portrait = portrait;
            this.portraitLeft = portraitLeft;
            if (portraitLeft) {
                xText = 88;
                xPortrait = 40;
            } else {
                xText = 40;
                xPortrait = 296;
            }
        }
        
        protected final DialogueBox add() {
            BotsnBoltsGame.addActor(this);
            BotsnBoltsGame.addActor(typer);
            typer.registerAdvanceListener();
            return this;
        }
        
        protected final DialogueBox setFinishHandler(final Runnable finishHandler) {
            this.finishHandler = finishHandler;
            return this;
        }
        
        protected final DialogueBox setNext(final Panmage portrait, final boolean portraitLeft, final CharSequence msg) {
            final DialogueBox next = dialogue(portrait, portraitLeft, msg);
            setFinishHandler(new Runnable() {
                @Override public final void run() {
                    next.add();
                }});
            return next;
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panlayer layer = getLayer();
            final Panmage box = BotsnBoltsGame.getBox();
            Menu.LevelSelectGrid.renderBox(renderer, layer, xText, 136, BotsnBoltsGame.DEPTH_HUD, box, 15, 4);
            Menu.LevelSelectGrid.renderBox(renderer, layer, xPortrait, 168, BotsnBoltsGame.DEPTH_HUD, box, 2, 2);
            renderer.render(layer, portrait, xPortrait + 8, 176, BotsnBoltsGame.DEPTH_HUD_TEXT, 0, 0, 32, 32, 0, !portraitLeft, false);
            renderer.render(layer, BotsnBoltsGame.black, xText + 8, 144, BotsnBoltsGame.DEPTH_HUD, 0, 0, 240, 64);
        }
        
        private final void finish() {
            Panctor.destroy(typer);
            destroy();
            if (finishHandler != null) {
                finishHandler.run();
            }
        }
    }
    
    protected abstract static class LabScreen extends Panscreen {
        @Override
        protected final void load() {
            BotsnBoltsGame.BotsnBoltsScreen.loadRoom(RoomLoader.getRoom(0, 550), false);
            Player.registerCapture(BotsnBoltsGame.tm);
            loadLab();
        }
        
        //@OverrideMe
        protected void loadLab() {
        }
        
        @Override
        protected final void step() {
            RoomLoader.step();
            stepLab();
        }
        
        //@OverrideMe
        protected void stepLab() {
        }
    }
    
    protected abstract static class TextScreen extends Panscreen {
        @Override
        protected final void load() {
            Pangine.getEngine().setBgColor(Pancolor.BLACK);
            loadText();
        }
        
        protected abstract void loadText();
    }
    
    protected final static class LabScreen1 extends LabScreen {
        private Talker drRoot = null;
        private Talker drFinal = null;
        
        @Override
        protected final void loadLab() {
            initActor(drRoot = newRootTalker(), 96, false);
            initActor(drFinal = newFinalMaskTalker(), 128, true);
            stepLab();
            // from far away... learn from him... too many froms
            newLabTextTyper("20XX\nDr. Root is the nation's foremost expert in the fields of robotics and artificial intelligence.  Scientists travel from far away to learn from him.  His brightest apprentice is Dr. Finnell.  Dr. Root teaches everything that he knows to Dr. Finnell.")
                .setFinishHandler(newScreenRunner(new TextScreen1()));
        }
        
        @Override
        protected final void stepLab() {
            final boolean rootTalk = Pangine.getEngine().isOn(64);
            drRoot.setTalking(rootTalk);
            drFinal.setTalking(!rootTalk);
        }
    }
    
    protected final static class TextScreen1 extends TextScreen {
        @Override
        protected final void loadText() {
            newLabTextTyper("But one day...").setFinishHandler(newScreenRunner(new LabScreen2()));
        }
    }
    
    protected final static class LabScreen2 extends LabScreen {
        @Override
        protected final void loadLab() {
            final Talker drRoot = newRootTalker().setTalking(false);
            initActor(drRoot, 128, false);
            final Talker drFinal = newFinalMaskTalker().setTalking(false);
            initActor(drFinal, 188, 130, true);
            drFinal.setVisible(false);
            addTimer(60, new TimerListener() { @Override public final void onTimer(final TimerEvent event) {
                RoomFunction.LabBackground.setActive(false);
                final TextTyper typer = new TextTyper(BotsnBoltsGame.font, "Init\ncall\n...");
                typer.getPosition().set(176, 150, BotsnBoltsGame.DEPTH_HUD_TEXT);
                typer.setLinesPerPage(4);
                typer.setGapY(2);
                BotsnBoltsGame.addActor(typer);
                typer.setFinishHandler(new Runnable() { @Override public final void run() {
                    typer.destroy();
                    setScreen(3);
                    drFinal.setVisible(true);
                    startLabConversation(drRoot, drFinal, new CharSequence[] {
                        "\"Hello Dr. Finnell.  How can I help you?\"",
                        "\"You fool!  You've taught me all of your secrets, and now I'll use them to destroy you!  I'm not really Dr. Finnell.\"" },
                        new Runnable() { @Override public final void run() {
                            setScreen(5);
                            final Panctor drFinalUnmasking = new Panctor();
                            replaceActor(drFinal, drFinalUnmasking);
                            drFinalUnmasking.setView(getFinalUnmask(0));
                            unmask(drFinalUnmasking, new TimerListener() { @Override public final void onTimer(final TimerEvent event) {
                                setScreen(3);
                                final Talker drFinalUnmasked = newFinalTalker().setTalking(false);
                                replaceActor(drFinalUnmasking, drFinalUnmasked);
                                startLabConversation(drFinalUnmasked, drRoot, new CharSequence[] {
                                    "\"My name is Final, and I've come here to conquer your world!\"",
                                    "\"How could you?  I trusted you, Final!\"",
                                    "\"That's Dr. Final!  I'm an alien invader, but I still earned my PhD while I was studying your planet!\"" },
                                    newScreenRunner(new TextScreen2()));
                            }});
                        }});
                }});
            }});
        }
    }
    
    protected final static void setScreen(final int imgX) {
        RoomFunction.LabBackground.setActive(false);
        final TileMap tm = BotsnBoltsGame.tm;
        final TileMapImage[][] imgMap = BotsnBoltsGame.imgMap;
        for (int y = 0; y < 2; y++) {
            final TileMapImage[] imgs = imgMap[5 - y];
            final int tmY = y + 8;
            for (int x = 0; x < 2; x++) {
                tm.setBackground(x + 11, tmY, imgs[imgX + x]);
            }
        }
    }
    
    protected final static class TextScreen2 extends TextScreen {
        @Override
        protected final void loadText() {
            newLabTextTyper("So Dr. Root began a search for Dr. Final's secret base of operations, developing plans and contingency plans to stop him.")
                .setFinishHandler(newScreenRunner(new VoidBootScreen()));
        }
    }
    
    protected abstract static class BootScreen extends TextScreen {
        @Override
        protected final void loadText() {
            newBootTextTyper(
                "Initiating boot sequence\n" +
                "* Dr. Root robotics " + getName() + " Test\n" +
                "* Version " + getVersion() + "\n" +
                "Validating components\n" +
                "* CPU... OK\n" +
                "* RAM... OK\n" +
                "* DISK... OK\n" +
                "Loading core routines\n" +
                "* Language... DONE\n" +
                "* Cognition... DONE\n" +
                "* Combat... DONE\n" +
                "Loading memory image from disk\n" +
                "Activating hardware systems\n" +
                "* Ambulatory... READY\n" +
                "* Audio input... READY\n" +
                "* Visual input... READY\n") // Last line, eyes open after visual input is activated
                .setFinishHandler(getFinishHandler());
        }
        
        protected abstract String getName();
        
        protected abstract String getVersion();
        
        protected abstract Runnable getFinishHandler();
    }
    
    protected final static class VoidBootScreen extends BootScreen {
        @Override
        protected final String getName() {
            return "Beta";
        }
        
        @Override
        protected final String getVersion() {
            return "0.9.0";
        }
        
        @Override
        protected final Runnable getFinishHandler() {
            return newScreenRunner(new LabScreen3());
        }
    }
    
    protected final static class LabScreen3 extends LabScreen {
        @Override
        protected final void loadLab() {
            final Talker drRoot = newRootTalker().setTalking(false);
            initActor(drRoot, 160, false);
            final PlayerImages pi = BotsnBoltsGame.pc == null ? BotsnBoltsGame.voidImages : BotsnBoltsGame.pc.pi;
            final Panmage playerStand = pi.basicSet.stand, playerBlink = pi.basicSet.blink;
            final Panctor player = newActor(playerBlink, 224, true);
            openEyes(player, playerBlink, playerStand, new TimerListener() { @Override public final void onTimer(final TimerEvent event) {
                drRoot.setTalking(true);
                final TextTyper typer = newLabTextTyper("\"I know that you're still just a beta test, but there's no time to finish you.  You need to stop Dr. Final.  I'm counting on you, Void!\"");
                typer.setFinishHandler(new Runnable() { @Override public final void run() {
                    typer.destroy();
                    drRoot.setTalking(false);
                    player.setView(pi.shootSet.stand);
                    lookAround(player, new TimerListener() { @Override public final void onTimer(final TimerEvent event) {
                        player.setView(playerStand);
                        addTimer(30, new TimerListener() { @Override public final void onTimer(final TimerEvent event) {
                            final Player p = new Player(BotsnBoltsGame.pc) { @Override protected final void onLanded() {
                                destroy();
                                dematerialize(newScreenRunner(new LabScreen1()));
                            }};
                            replaceActor(player, p);
                            p.startScript(new StillAi(), null);
                            p.stateHandler.onJump(p);
                        }});
                    }});
                }});
            }});
        }
    }
    
    protected abstract static class CharacterScreen extends Panscreen {
        private final String title;
        private final boolean mirror;
        private final int textLines;
        private final CharacterDefinition[] defs;
        
        protected CharacterScreen(final String title, final boolean mirror, final int textLines, final CharacterDefinition... defs) {
            this.title = title;
            this.mirror = mirror;
            this.textLines = textLines;
            this.defs = defs;
        }
        
        @Override
        protected final void load() throws Exception {
            BotsnBoltsGame.room = Pangame.getGame().getCurrentRoom();
            Pangine.getEngine().setBgColor(new FinPancolor(96, 96, 96));
            BotsnBoltsGame.addActor(new CharacterBg());
            final Pantext text = new Pantext(Pantil.vmid(), BotsnBoltsGame.font, title);
            text.getPosition().set(192, 159, BotsnBoltsGame.DEPTH_HUD_TEXT);
            text.centerX();
            BotsnBoltsGame.addActor(text);
            addCharacter(0);
        }
        
        private final void addCharacter(final int index) {
            final int n = defs.length;
            final int textHeight = textLines * 9;
            final Panctor actor = new Panctor();
            final CharacterDefinition def = defs[index];
            actor.setView(def.getImage());
            final float num = BotsnBoltsGame.GAME_W * (index + 1);
            final float den = n + 1;
            final float x = Math.round(num / den);
            actor.getPosition().set(x, 58 + textHeight, BotsnBoltsGame.DEPTH_ENEMY);
            actor.setMirror(mirror);
            BotsnBoltsGame.addActor(actor);
            final TextTyper typer = new TextTyper(BotsnBoltsGame.font, def.name)
            .registerAdvanceListener()
            .setFinishHandler(new Runnable() { @Override public final void run() {
                final int nextIndex = index + 1;
                if (nextIndex < n) {
                    addCharacter(nextIndex);
                } else {
                    Pangine.getEngine().addTimer(actor, 60, new TimerListener() { @Override public final void onTimer(final TimerEvent event) {
                        finish();
                    }});
                }
            }});
            typer.setLinesPerPage(3);
            typer.setGapY(1);
            typer.getPosition().set(x, 48 + textHeight, BotsnBoltsGame.DEPTH_HUD_TEXT);
            typer.centerX();
            BotsnBoltsGame.addActor(typer);
        }
        
        protected abstract void finish();
    }
    
    private final static class CharacterBg extends Panctor {
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panlayer layer = getLayer();
            LevelSelectGrid.renderBg(renderer, layer, 24);
            LevelSelectGrid.renderBg(renderer, layer, 168);
        }
    }
    
    private abstract static class CharacterDefinition {
        private final String name;
        
        private CharacterDefinition(final String name) {
            this.name = name;
        }
        
        protected abstract Panmage getImage();
    }
    
    protected final static class RootFamilyScreen extends CharacterScreen {
        protected RootFamilyScreen() {
            super("The Root Family Tree", false, 1,
                new CharacterDefinition("Dr. Root") { @Override protected final Panmage getImage() { return getRoot(); }},
                new CharacterDefinition("Byte") { @Override protected final Panmage getImage() { return Animal.getAnimalImage(BotsnBoltsGame.voidImages); }},
                new CharacterDefinition("Baud") { @Override protected final Panmage getImage() { return Animal.getBirdImage(BotsnBoltsGame.voidImages); }},
                new CharacterDefinition("Void") { @Override protected final Panmage getImage() { return BotsnBoltsGame.voidImages.basicSet.stand; }}
            );
        }
        
        @Override
        protected final void finish() {
            Panscreen.set(new InnerLoopScreen());
        }
    }
    
    protected final static class InnerLoopScreen extends CharacterScreen {
        protected InnerLoopScreen() {
            super("The Inner Loop", true, 1,
                new CharacterDefinition("Cyan Titan") { @Override protected final Panmage getImage() { return CyanTitan.getImage(); }},
                new CharacterDefinition("Volatile") { @Override protected final Panmage getImage() { return BotsnBoltsGame.volatileImages.basicSet.stand; }},
                new CharacterDefinition("Dr. Final") { @Override protected final Panmage getImage() { return Final.getCoat(); }}
            );
        }
        
        @Override
        protected final void finish() {
            Panscreen.set(new RootFamilyScreen());
        }
    }
    
    protected final static class ShipScreen extends TextScreen {
        @Override
        protected final void loadText() {
            newLabTextTyper(
                "Navigation system online\n" +
                "Proximity alert\n" +
                "Approaching target\n" +
                "Decelerating\n" +
                "Enabling sensors\n" +
                "Scanning for landing site\n" +
                "Plotting final course\n" +
                "Engaging engines\n" +
                "Correcting current trajectory\n" +
                "Contacting Tree\n" +
                "* Failed\n" +
                "Attempting auxiliary communication array\n" +
                "Contacting Tree\n" +
                "* Success\n" +
                "Activating payload")
                .setFinishHandler(newScreenRunner(new NullBootScreen()));
        }
    }
    
    protected final static class NullBootScreen extends BootScreen {
        @Override
        protected final String getName() {
            return "Alpha";
        }
        
        @Override
        protected final String getVersion() {
            return "0.8.0";
        }
        
        @Override
        protected final Runnable getFinishHandler() {
            return null;
        }
    }
    
    protected final static TextTyper newLabTextTyper(final CharSequence msg) {
        return newStoryTyper(msg, 53);
    }
    
    protected final static TextTyper newBootTextTyper(final CharSequence msg) {
        final TextTyper typer = newStoryTyper(msg, 180);
        typer.setLinesPerPage(16);
        return typer;
    }
    
    protected final static TextTyper newStoryTyper(final CharSequence msg, final float y) {
        final TextTyper typer = newTextTyper(msg, 32).registerAdvanceListener();
        typer.getPosition().set(64, y);
        BotsnBoltsGame.addActor(typer);
        return typer;
    }
    
    protected final static void startLabConversation(final Talker talker1, final Talker talker2, final CharSequence[] msgs, final Runnable finishHandler) {
        talker1.setTalking(false); // continueLabConversion will invert these
        talker2.setTalking(true);
        continueLabConversation(talker1, talker2, finishHandler, 0, msgs);
    }
    
    protected final static void continueLabConversation(final Talker talker1, final Talker talker2, final Runnable finishHandler, final int msgIndex, final CharSequence... msgs) {
        talker1.toggleTalking();
        talker2.toggleTalking();
        final TextTyper typer = newLabTextTyper(msgs[msgIndex]);
        typer.setFinishHandler(new Runnable() {
            @Override public final void run() {
                typer.destroy();
                final int nextIndex = msgIndex + 1;
                if (nextIndex >= msgs.length) {
                    finishHandler.run();
                } else {
                    continueLabConversation(talker1, talker2, finishHandler, nextIndex, msgs);
                }
            }});
    }
    
    protected final static Panctor newActor(final Panmage img, final float x, final boolean mirror) {
        final Panctor actor = new Panctor();
        actor.setView(img);
        initActor(actor, x, mirror);
        return actor;
    }
    
    private final static void initActor(final Panctor actor, final float x, final boolean mirror) {
        initActor(actor, x, 96, mirror);
    }
    
    private final static void initActor(final Panctor actor, final float x, final float y, final boolean mirror) {
        actor.getPosition().set(x, y, BotsnBoltsGame.DEPTH_ENEMY);
        actor.setMirror(mirror);
        BotsnBoltsGame.addActor(actor);
    }
    
    private final static void replaceActor(final Panctor oldActor, final Panctor newActor) {
        final Panple oldPos = oldActor.getPosition();
        initActor(newActor, oldPos.getX(), oldPos.getY(), oldActor.isMirror());
        oldActor.destroy();
    }
    
    protected abstract static class Talker extends Panctor implements StepListener {
        private boolean talking = true;
        
        @Override
        public final void onStep(final StepEvent event) {
            step();
        }
        
        private final void step() {
            if (talking) {
                changeView(Pangine.getEngine().isOn(4) ? getMouthOpen() : getMouthClosed());
            } else {
                changeView(getMouthClosed());
            }
        }
        
        protected final Talker setTalking(final boolean talking) {
            this.talking = talking;
            step();
            return this;
        }
        
        protected final Talker toggleTalking() {
            return setTalking(!talking);
        }
        
        protected abstract Panmage getMouthClosed();
        
        protected abstract Panmage getMouthOpen();
    }
    
    protected final static Talker newRootTalker() {
        return new Talker() {
            @Override final protected Panmage getMouthClosed() {
                return getRoot();
            }
            @Override final protected Panmage getMouthOpen() {
                return getRootTalk();
            }};
    }
    
    protected final static Talker newFinalMaskTalker() {
        return new Talker() {
            @Override final protected Panmage getMouthClosed() {
                return getFinalMask();
            }
            @Override final protected Panmage getMouthOpen() {
                return getFinalMaskTalk();
            }};
    }
    
    protected final static void unmask(final Panctor drFinal, final TimerListener finishHandler) {
        unmask(drFinal, 30, 1, finishHandler);
    }
    
    protected final static void unmask(final Panctor drFinal, final long duration, final int i, final TimerListener finishHandler) {
        addTimer(duration, new TimerListener() {
            @Override public final void onTimer(final TimerEvent event) {
                drFinal.setView(getFinalUnmask(i));
                if (i < 11) {
                    unmask(drFinal, 4, i + 1, finishHandler);
                } else {
                    addTimer(30, finishHandler);
                }
            }});
    }
    
    protected final static void openEyes(final Panctor actor, final Panmage closed, final Panmage open, final TimerListener finishHandler) {
        openEyes(actor, closed, open, 0, finishHandler);
    }
    
    protected final static void openEyes(final Panctor actor, final Panmage curr, final Panmage next, final int index, final TimerListener finishHandler) {
        final boolean last = index == 9;
        final long dur = (last || (index == 0)) ? 30 : 6;
        addTimer(dur, last ? finishHandler : new TimerListener() { @Override public final void onTimer(final TimerEvent event) {
            actor.setView(next);
            openEyes(actor, next, curr, index + 1, finishHandler);
        }});
    }
    
    protected final static void lookAround(final Panctor actor, final TimerListener finishHandler) {
        lookAround(actor, 0, finishHandler);
    }
    
    protected final static void lookAround(final Panctor actor, final int index, final TimerListener finishHandler) {
        addTimer(24, (index == 4) ? finishHandler : new TimerListener() { @Override public final void onTimer(final TimerEvent event) {
            actor.setMirror(!actor.isMirror());
            lookAround(actor, index + 1, finishHandler);
        }});
    }
    
    protected final static Talker newFinalTalker() {
        return new Talker() {
            @Override final protected Panmage getMouthClosed() {
                return Final.getCoat();
            }
            @Override final protected Panmage getMouthOpen() {
                return Final.getCoatTalk();
            }};
    }
    
    protected final static Runnable newScreenRunner(final Panscreen screen) {
        return new Runnable() {
            @Override public final void run() {
                Panscreen.set(screen);
            }};
    }
    
    private static Panmage root = null, rootTalk = null;
    
    protected final static Panmage getRoot() {
        return (root = getImage(root, "chr/root/Root", BotsnBoltsGame.og));
    }
    
    protected final static Panmage getRootTalk() {
        return (rootTalk = getImage(rootTalk, "chr/root/RootTalk", BotsnBoltsGame.og));
    }
    
    private static Panmage finalMask = null, finalMaskTalk = null;
    
    protected final static Panmage getFinalMask() {
        return (finalMask = getImage(finalMask, "boss/final/FinalMask", BotsnBoltsGame.og));
    }
    
    protected final static Panmage getFinalMaskTalk() {
        return (finalMaskTalk = getImage(finalMaskTalk, "boss/final/FinalMaskTalk", BotsnBoltsGame.og));
    }
    
    private final static Panmage finalUnmask[] = new Panmage[12];
    
    protected final static Panmage getFinalUnmask(final int i) {
        Panmage img = finalUnmask[i];
        if (img == null) {
            img = getImage(img, "boss/final/FinalUnmask" + (i + 1), BotsnBoltsGame.og);
            finalUnmask[i] = img;
        }
        return img;
    }
    
    protected final static Panmage getImage(final Panmage img, final String name, final Panple o) {
        return (img == null) ? Pangine.getEngine().createImage(name, o, null, null, BotsnBoltsGame.RES + name + ".png") : img;
    }
    
    protected final static void addTimer(final long duration, final TimerListener listener) {
        Pangine.getEngine().addTimer(BotsnBoltsGame.tm, duration, listener);
    }
}