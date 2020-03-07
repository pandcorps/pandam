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
package org.pandcorps.botsnbolts;

import java.util.*;

import org.pandcorps.botsnbolts.Boss.*;
import org.pandcorps.botsnbolts.Enemy.*;
import org.pandcorps.botsnbolts.Menu.*;
import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.botsnbolts.RoomLoader.*;
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
        typer.getPosition().setZ(BotsnBoltsGame.DEPTH_DIALOGUE_TEXT);
        return typer;
    }
    
    protected final static DialogueBox dialogue(final Panmage portrait, final boolean portraitLeft, final int yOff, final CharSequence msg) {
        final TextTyper typer = newTextTyper(msg, 29);
        final DialogueBox box = new DialogueBox(typer, portrait, portraitLeft, yOff);
        typer.getPosition().set(box.xText + 12, 197 + yOff);
        typer.setSound(BotsnBoltsGame.fxText).setFinishHandler(new Runnable() {
            @Override public final void run() {
                box.finish();
            }});
        return box;
    }
    
    protected final static DialogueBox portrait(final Panmage portrait, final int x, final int y, final boolean mirror) {
        final DialogueBox box = new DialogueBox(portrait, x, y, mirror);
        BotsnBoltsGame.addActor(box);
        return box;
    }
    
    protected final static Set<Panmage> pupilNeededSet = new HashSet<Panmage>();
    
    protected final static boolean isPupilNeeded(final Panmage portrait) {
        return pupilNeededSet.contains(portrait);
    }
    
    protected final static class DialogueBox extends Panctor {
        protected final TextTyper typer;
        private final Panmage portrait;
        private final boolean portraitLeft;
        private final int yOff;
        private final boolean pupils;
        private final int xText;
        private final int xPortrait;
        private final int yPortrait;
        private Runnable finishHandler = null;
        
        protected DialogueBox(final TextTyper typer, final Panmage portrait, final boolean portraitLeft, final int yOff) {
            this.typer = typer;
            this.portrait = portrait;
            this.portraitLeft = portraitLeft;
            this.yOff = yOff;
            this.pupils = isPupilNeeded(portrait);
            if (portraitLeft) {
                xText = 88;
                xPortrait = 40;
            } else {
                xText = 40;
                xPortrait = 296;
            }
            yPortrait = 168 + yOff;
        }
        
        private DialogueBox(final Panmage portrait, final int x, final int y, final boolean mirror) {
            this.portrait = portrait;
            xPortrait = x;
            yPortrait = y;
            portraitLeft = !mirror;
            yOff = 0;
            pupils = isPupilNeeded(portrait);
            typer = null;
            xText = 0;
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
            final DialogueBox next = dialogue(portrait, portraitLeft, yOff, msg);
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
            if (typer != null) {
                Menu.LevelSelectGrid.renderBox(renderer, layer, xText, 136 + yOff, BotsnBoltsGame.DEPTH_DIALOGUE_BOX, box, 15, 4);
            }
            final int xp = xPortrait + 8, yp = yPortrait + 8;
            Menu.LevelSelectGrid.renderBox(renderer, layer, xPortrait, yPortrait, BotsnBoltsGame.DEPTH_DIALOGUE_BOX, box, 2, 2);
            final float ps = portrait.getSize().getX(), off = (32.0f - ps) / 2.0f;
            renderer.render(layer, portrait, xp + off, yp + off, BotsnBoltsGame.DEPTH_DIALOGUE_TEXT, 0, 0, ps, ps, 0, !portraitLeft, false);
            if (pupils) {
                Pupils.renderView(renderer, layer, xp, yp, 0, 0, 0);
            }
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
            BotsnBoltsGame.room = Pangame.getGame().getCurrentRoom();
            Pangine.getEngine().setBgColor(Pancolor.BLACK);
            Player.registerCapture(loadText());
        }
        
        protected abstract TextTyper loadText();
    }
    
    protected final static class LabScreen1 extends LabScreen {
        private Talker drRoot = null;
        private Talker drFinal = null;
        
        @Override
        protected final void loadLab() {
            BotsnBoltsGame.musicIntro.startMusic();
            initActor(drRoot = newRootTalker(), 96, false);
            initActor(drFinal = newFinalMaskTalker(), 128, true);
            stepLab();
            newLabTextTyper("20XX\nDr. Root is the nation's foremost expert in the fields of robotics and artificial intelligence.  Scientists travel from far away to seek his guidance.  His brightest apprentice is Dr. Finnell.  Dr. Root teaches everything that he knows to Dr. Finnell.")
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
        protected final TextTyper loadText() {
            return newLabTextTyper("But one day...").setFinishHandler(newScreenRunner(new LabScreen2()));
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
                final TextTyper typer = newScreenTyper("Init\ncall\n...");
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
        protected final TextTyper loadText() {
            return newLabTextTyper("So Dr. Root began a search for Dr. Final's secret base of operations, developing plans and contingency plans to stop him.")
                .setFinishHandler(newScreenRunner(new VoidBootScreen()));
        }
    }
    
    protected abstract static class BootScreen extends TextScreen {
        @Override
        protected final TextTyper loadText() {
            Pangine.getEngine().getAudio().stopMusic();
            return newBootTextTyper(
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
                .setSound(BotsnBoltsGame.fxText).setFinishHandler(getFinishHandler());
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
            return "0.2.0";
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
                final TextTyper typer = newLabTextTyper("\"I know that you're still just a beta test, but there's no time to finish you.  You need to stop Dr. Final.  I'm counting on you, Void!\"")
                        .setSound(BotsnBoltsGame.fxText);
                typer.setFinishHandler(new Runnable() { @Override public final void run() {
                    BotsnBoltsGame.musicLevelStart.startSound();
                    typer.destroy();
                    drRoot.setTalking(false);
                    player.setView(pi.shootSet.stand);
                    lookAround(player, new TimerListener() { @Override public final void onTimer(final TimerEvent event) {
                        player.setView(playerStand);
                        addTimer(30, new TimerListener() { @Override public final void onTimer(final TimerEvent event) {
                            final Player p = new Player(BotsnBoltsGame.pc) { @Override protected final void onLanded() {
                                destroy();
                                dematerialize(newLevelRunner(RoomLoader.getFirstLevel()));
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
    
    protected final static class LabScreenEnding1 extends LabScreen {
        @Override
        protected final void loadLab() {
            final Talker drRoot = newRootTalker().setTalking(false);
            initActor(drRoot, 96, true);
            addTimer(60, new TimerListener() { @Override public final void onTimer(final TimerEvent event) {
                final Player p = new Player(BotsnBoltsGame.pc) { @Override public final void onMaterialized() {
                    destroy();
                    final Talker player = newPlayerTalker().setTalking(false);
                    replaceActor(this, player);
                    addTimer(60, new TimerListener() { @Override public final void onTimer(final TimerEvent event) {
                        drRoot.setTalking(true).setMirror(false);
                        newLabTextTyper("\"Welcome back, Void!  You defeated Dr. Final and forced him to retreat to his base offworld.  Our whole world can breath a sigh of relief.  I'm so proud of you, Void!\"").setFinishHandler(newScreenRunner(new RootFamilyScreen()));
                    }});
                }};
                initActor(p, 128, true);
                new Warp(p);
            }});
        }
    }
    
    private final static void enableCharacterBg() {
        Pangine.getEngine().setBgColor(new FinPancolor(96, 96, 96));
        BotsnBoltsGame.addActor(new CharacterBg());
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
            enableCharacterBg();
            final Pantext text = new Pantext(Pantil.vmid(), BotsnBoltsGame.font, title);
            text.getPosition().set(192, 159, BotsnBoltsGame.DEPTH_HUD_TEXT);
            text.centerX();
            BotsnBoltsGame.addActor(text);
            Player.registerCapture(text);
            addCharacter(0);
        }
        
        private final void addCharacter(final int index) {
            final int n = defs.length;
            final int textHeight = textLines * 9;
            final Panctor actor = new Panctor();
            final CharacterDefinition def = defs[index];
            actor.setView(def.getImage());
            final float num = BotsnBoltsGame.GAME_W * (index + 0.5f), den = n;
            final int x = Math.round(num / den), y = 58 + textHeight;
            final Panmage portrait = def.getPortrait();
            final int xOff;
            if (portrait == null) {
                xOff = 0;
            } else {
                xOff = (n == 1) ? 96 : 32;
                portrait(portrait, x - xOff - 24, y, def.isPortraitMirror());
            }
            actor.getPosition().set(x + def.x + xOff, y + def.y, BotsnBoltsGame.DEPTH_ENEMY);
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
            typer.getPosition().set(x, y - 10, BotsnBoltsGame.DEPTH_HUD_TEXT);
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
        private final BotLevel level;
        private int x = 0;
        private int y = 0;
        
        private CharacterDefinition(final String name) {
            this(name, null);
        }
        
        private CharacterDefinition(final String name, String levelBossName) {
            this.name = name;
            if (levelBossName == null) {
                final StringBuilder b = new StringBuilder();
                final int size = name.length();
                for (int i = 0; i < size; i++) {
                    final char c = name.charAt(i);
                    if (c == ']') {
                        Chartil.clear(b);
                    } else if (!Character.isWhitespace(c)) {
                        b.append(c);
                    }
                }
                levelBossName = b.toString();
            }
            level = RoomLoader.levelMap.get(levelBossName);
        }
        
        protected final CharacterDefinition setX(final int x) {
            this.x = x;
            return this;
        }
        
        protected final CharacterDefinition setY(final int y) {
            this.y = y;
            return this;
        }
        
        protected final Panmage getPortrait() {
            return RoomLoader.getPortrait(level);
        }
        
        protected final boolean isPortraitMirror() {
            final Boolean portraitMirror = (level == null) ? null : level.portraitMirror;
            return (portraitMirror == null) || portraitMirror.booleanValue();
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
            Panscreen.set(new DroneScreen1());
        }
    }
    
    protected abstract static class DroneScreen extends CharacterScreen {
        protected DroneScreen(final CharacterDefinition... defs) {
            super("The Drones", true, 2, defs);
        }
    }
    
    protected final static class DroneScreen1 extends DroneScreen {
        protected DroneScreen1() {
            super(
                new CharacterDefinition("Saucer\nDrone") { @Override protected final Panmage getImage() { return SaucerEnemy.getImage(0); }},
                new CharacterDefinition("Copter\nDrone") { @Override protected final Panmage getImage() { return BotsnBoltsGame.propEnemy.getImage(1); }},
                new CharacterDefinition("Spring\nDrone") { @Override protected final Panmage getImage() { return BotsnBoltsGame.springEnemy[1]; }}.setY(2),
                new CharacterDefinition("Crawl\nDrone") { @Override protected final Panmage getImage() { return BotsnBoltsGame.crawlEnemy.getImage(1); }},
                new CharacterDefinition("Shield\nDrone") { @Override protected final Panmage getImage() { return BotsnBoltsGame.shieldedEnemy.getImage(); }}.setY(1)
            );
        }
        
        @Override
        protected final void finish() {
            Panscreen.set(new DroneScreen2());
        }
    }
    
    protected final static class DroneScreen2 extends DroneScreen {
        protected DroneScreen2() {
            super(
                new CharacterDefinition("Drill\nDrone") { @Override protected final Panmage getImage() { return DrillEnemy.getImage(0); }},
                new CharacterDefinition("Walker\nDrone") { @Override protected final Panmage getImage() { return WalkerEnemy.getImage(0); }},
                new CharacterDefinition("Glider\nDrone") { @Override protected final Panmage getImage() { return GliderEnemy.getImage(2); }}.setX(7).setY(-1),
                new CharacterDefinition("Sub\nDrone") { @Override protected final Panmage getImage() { return SubEnemy.getImage(1); }},
                new CharacterDefinition("Ring\nDrone") { @Override protected final Panmage getImage() { return RingEnemy.getImage(6); }}.setX(7).setY(-1)
            );
        }
        
        @Override
        protected final void finish() {
            Panscreen.set(new HenchbotScreen1());
        }
    }
    
    protected abstract static class HenchbotScreen extends CharacterScreen {
        protected HenchbotScreen(final CharacterDefinition... defs) {
            super("The Henchbots", true, 2, defs);
        }
    }
    
    protected final static class HenchbotScreen1 extends HenchbotScreen {
        protected HenchbotScreen1() {
            super(
                new CharacterDefinition("Cyan\nHenchbot") { @Override protected final Panmage getImage() { return BotsnBoltsGame.henchbotEnemy[1]; }},
                new CharacterDefinition("Electric\nHenchbot") { @Override protected final Panmage getImage() { return ElectricityEnemy.getStrike(); }},
                new CharacterDefinition("Fire\nHenchbot") { @Override protected final Panmage getImage() { return BotsnBoltsGame.flamethrowerEnemy[0]; }},
                new CharacterDefinition("Jetpack\nHenchbot") { @Override protected final Panmage getImage() { return JetpackEnemy.getJetpackImage(JetpackEnemy.flyImgs, 0); }}
            );
        }
        
        @Override
        protected final void finish() {
            Panscreen.set(new HenchbotScreen2());
        }
    }
    
    protected final static class HenchbotScreen2 extends HenchbotScreen {
        protected HenchbotScreen2() {
            super(
                new CharacterDefinition("Shovel\nHenchbot") { @Override protected final Panmage getImage() { return ShovelEnemy.getImage(0); }},
                new CharacterDefinition("Jackhammer\nHenchbot") { @Override protected final Panmage getImage() { return JackhammerEnemy.getImage(0); }},
                new CharacterDefinition("Freeze\nHenchbot") { @Override protected final Panmage getImage() { return BotsnBoltsGame.freezeRayEnemy[0]; }},
                new CharacterDefinition("Quicksand\nHenchbot") { @Override protected final Panmage getImage() { return BotsnBoltsGame.quicksandEnemyAttack; }}
            );
        }
        
        @Override
        protected final void finish() {
            Panscreen.set(new HenchbotScreen3());
        }
    }
    
    protected final static class HenchbotScreen3 extends HenchbotScreen {
        protected HenchbotScreen3() {
            super(
                new CharacterDefinition("Swimmer\nHenchbot") { @Override protected final Panmage getImage() { return SwimEnemy.getSwimImage(0); }},
                new CharacterDefinition("Rock\nHenchbot") { @Override protected final Panmage getImage() { return RockEnemy.getRockThrow(); }},
                new CharacterDefinition("Magenta\nHenchbot") { @Override protected final Panmage getImage() { return BotsnBoltsGame.magentaEnemy[2]; }}
            );
        }
        
        @Override
        protected final void finish() {
            Panscreen.set(new ArrayScreen1());
        }
    }
    
    protected abstract static class ArrayScreen extends CharacterScreen {
        protected ArrayScreen(final CharacterDefinition... defs) {
            super("The Array", true, 3, defs);
        }
    }
    
    protected final static class ArrayScreen1 extends ArrayScreen {
        protected ArrayScreen1() {
            super(
                new CharacterDefinition("DFA[0][0]\nVolcano\nBot") { @Override protected final Panmage getImage() { return VolcanoBot.getImage(); }},
                new CharacterDefinition("DFA[0][1]\nHail\nBot") { @Override protected final Panmage getImage() { return HailBot.getImage(); }}
            );
        }
        
        @Override
        protected final void finish() {
            Panscreen.set(new ArrayScreen2());
        }
    }
    
    protected final static class ArrayScreen2 extends ArrayScreen {
        protected ArrayScreen2() {
            super(
                new CharacterDefinition("DFA[0][2]\nRockslide\nBot") { @Override protected final Panmage getImage() { return RockslideBot.getImage(); }},
                new CharacterDefinition("DFA[0][3]\nLightning\nBot") { @Override protected final Panmage getImage() { return LightningBot.getImage(); }}
            );
        }
        
        @Override
        protected final void finish() {
            Panscreen.set(new ArrayScreen3());
        }
    }
    
    protected final static class ArrayScreen3 extends ArrayScreen {
        protected ArrayScreen3() {
            super(
                new CharacterDefinition("DFA[0][4]\nEarthquake\nBot") { @Override protected final Panmage getImage() { return EarthquakeBot.getImage(); }},
                new CharacterDefinition("DFA[0][5]\nCyclone\nBot") { @Override protected final Panmage getImage() { return CycloneBot.getImage(); }}
            );
        }
        
        @Override
        protected final void finish() {
            Panscreen.set(new ArrayScreen4());
        }
    }
    
    protected final static class ArrayScreen4 extends ArrayScreen {
        protected ArrayScreen4() {
            super(
                new CharacterDefinition("DFA[0][6]\nFlood\nBot") { @Override protected final Panmage getImage() { return FloodBot.getStart1(); }},
                new CharacterDefinition("DFA[0][7]\nDrought\nBot") { @Override protected final Panmage getImage() { return DroughtBot.getImage(); }}
            );
        }
        
        @Override
        protected final void finish() {
            Panscreen.set(new InnerLoopScreen1());
        }
    }
    
    protected abstract static class InnerLoopScreen extends CharacterScreen {
        protected InnerLoopScreen(final CharacterDefinition... defs) {
            super("The Inner Loop", true, 1, defs);
        }
    }
    
    protected final static class InnerLoopScreen1 extends InnerLoopScreen {
        protected InnerLoopScreen1() {
            super(new CharacterDefinition("Cyan Titan") { @Override protected final Panmage getImage() { return CyanTitan.getImage(); }});
        }
        
        @Override
        protected final void finish() {
            Panscreen.set(new InnerLoopScreen2());
        }
    }
    
    protected final static class InnerLoopScreen2 extends InnerLoopScreen {
        protected InnerLoopScreen2() {
            super(new CharacterDefinition("Volatile", "Volatile2") { @Override protected final Panmage getImage() { return BotsnBoltsGame.volatileImages.basicSet.stand; }});
        }
        
        @Override
        protected final void finish() {
            Panscreen.set(new InnerLoopScreen3());
        }
    }
    
    protected final static class InnerLoopScreen3 extends InnerLoopScreen {
        protected InnerLoopScreen3() {
            super(new CharacterDefinition("Dr. Final") { @Override protected final Panmage getImage() { return Final.getCoat(); }});
        }
        
        @Override
        protected final void finish() {
            Panscreen.set(new CreditsScreen());
        }
    }
    
    protected final static class CreditsScreen extends TextScreen {
        @Override
        protected final TextTyper loadText() {
            enableCharacterBg();
            final TextTyper typer = newStoryTyper(
                BotsnBoltsGame.TITLE + "\n" +
                "Created by:\n" +
                BotsnBoltsGame.AUTHOR + "\n" +
                BotsnBoltsGame.COPYRIGHT, 153)
                .setFinishHandler(newScreenRunner(new ProgressScreen()));
            typer.setLinesPerPage(16);
            return typer;
        }
    }
    
    protected final static class ProgressScreen extends TextScreen {
        @Override
        protected final TextTyper loadText() {
            enableCharacterBg();
            final Profile prf = BotsnBoltsGame.pc.prf;
            final int numUpgrades = prf.upgrades.size(), numDisks = prf.disks.size();
            final float percentage = Math.round((numUpgrades + numDisks) * 1000.0f / 36.0f) / 10.0f;
            final TextTyper typer = newStoryTyper(
                "Status Report\n" +
                "-------------\n" +
                "Upgrades:\n" +
                numUpgrades + " / " + 8 + "\n" +
                "-------------\n" +
                "Disks:\n" +
                numDisks + " / " + 28 + "\n" +
                "-------------\n" +
                "Percentage:\n" +
                percentage + "%", 153)
                .setFinishHandler(newScreenRunner(new LabScreenStinger1()));
            typer.setLinesPerPage(16);
            return typer;
        }
    }
    
    protected final static class LabScreenStinger1 extends LabScreen {
        @Override
        protected final void loadLab() {
            Pangine.getEngine().getAudio().stopMusic();
            final Talker drRoot = newRootTalker().setTalking(false);
            initActor(drRoot, 96, true);
            final Talker player = newPlayerTalker().setTalking(false);
            initActor(player, 128, true);
            addTimer(60, new TimerListener() { @Override public final void onTimer(final TimerEvent event) {
                RoomFunction.LabBackground.setActive(false);
                newScreenTyper("....\n....\n....").setFinishHandler(new Runnable() { @Override public final void run() {
                    player.setMirror(false);
                    addTimer(60, new TimerListener() { @Override public final void onTimer(final TimerEvent event) {
                        drRoot.setMirror(false);
                        player.setTalking(true).setMirror(true);
                        newLabTextTyper("\"Dr. Root...  It looks like we're receiving some kind of transmission.\"").setSound(BotsnBoltsGame.fxText).setFinishHandler(newScreenRunner(new ShipScreen()));
                    }});
                }});
            }});
        }
    }
    
    protected final static class ShipScreen extends TextScreen {
        @Override
        protected final TextTyper loadText() {
            Pangine.getEngine().getAudio().stopMusic();
            return newBootTextTyper(
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
                "Attempting auxiliary communication unit\n" +
                "Contacting Tree\n" +
                "* Success\n" +
                "Activating payload")
                .setSound(BotsnBoltsGame.fxText).setFinishHandler(newScreenRunner(new NullBootScreen()));
        }
    }
    
    protected final static class NullBootScreen extends BootScreen {
        @Override
        protected final String getName() {
            return "Alpha";
        }
        
        @Override
        protected final String getVersion() {
            return "0.1.0";
        }
        
        @Override
        protected final Runnable getFinishHandler() {
            return newScreenRunner(new LabScreenStinger2());
        }
    }
    
    protected final static class LabScreenStinger2 extends LabScreen {
        @Override
        protected final void loadLab() {
            final Talker drRoot = newRootTalker().setTalking(false);
            initActor(drRoot, 160, false);
            final Talker player = newPlayerTalker().setTalking(true);
            initActor(player, 224, true);
            RoomFunction.LabBackground.setActive(false);
            initScreenText(new Pantext(Pantil.vmid(), BotsnBoltsGame.font, "....\n....\n...."));
            startLabConversation(drRoot, player, new CharSequence[] {
                "\"This is amazing!  When I first lost contact, I feared the worst.\"",
                "\"Lost contact with what?\"",
                "\"But it was simply the primary communication unit that failed.  All other systems are still functioning!  The auxiliary unit is only used for high priority messages.  That's why I haven't heard anything else until now!\"",
                "\"Dr. Root?  I don't understand.  What are you saying?\"",
                "\"Void...  I need to tell you something...  Something that I should have told you long ago...\"",
                "\"Dr. Root?\"",
                "\"Void...  You were not my first child.\"" },
                newScreenRunner(new ThankYouScreen()), BotsnBoltsGame.fxText);
        }
    }
    
    protected final static class ThankYouScreen extends TextScreen {
        @Override
        protected final TextTyper loadText() {
            enableCharacterBg();
            final TextTyper typer = newStoryTyper(
                "Thank you for playing " + BotsnBoltsGame.TITLE + "!\n" +
                "It means so much to me.\n" +
                "I hope that you enjoyed it.\n" +
                " - " + BotsnBoltsGame.AUTHOR, 153)
                .setFinishHandler(newScreenRunner(new LevelSelectScreen()));
            typer.setLinesPerPage(16);
            return typer;
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
        startLabConversation(talker1, talker2, msgs, finishHandler, null);
    }
    
    protected final static void startLabConversation(final Talker talker1, final Talker talker2, final CharSequence[] msgs, final Runnable finishHandler, final Pansound sound) {
        talker1.setTalking(false); // continueLabConversion will invert these
        talker2.setTalking(true);
        continueLabConversation(talker1, talker2, finishHandler, sound, 0, msgs);
    }
    
    protected final static void continueLabConversation(final Talker talker1, final Talker talker2, final Runnable finishHandler, final Pansound sound, final int msgIndex, final CharSequence... msgs) {
        talker1.toggleTalking();
        talker2.toggleTalking();
        final TextTyper typer = newLabTextTyper(msgs[msgIndex]);
        typer.setSound(sound).setFinishHandler(new Runnable() {
            @Override public final void run() {
                typer.destroy();
                final int nextIndex = msgIndex + 1;
                if (nextIndex >= msgs.length) {
                    finishHandler.run();
                } else {
                    continueLabConversation(talker1, talker2, finishHandler, sound, nextIndex, msgs);
                }
            }});
    }
    
    protected final static TextTyper newScreenTyper(final CharSequence msg) {
        final TextTyper typer = new TextTyper(BotsnBoltsGame.font, msg);
        initScreenText(typer);
        return typer;
    }
    
    protected final static void initScreenText(final Pantext text) {
        text.getPosition().set(176, 150, BotsnBoltsGame.DEPTH_HUD_TEXT);
        text.setLinesPerPage(4);
        text.setGapY(2);
        BotsnBoltsGame.addActor(text);
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
    
    protected final static Talker newPlayerTalker() {
        return new Talker() {
            @Override final protected Panmage getMouthClosed() {
                return BotsnBoltsGame.pc.pi.basicSet.stand;
            }
            @Override final protected Panmage getMouthOpen() {
                return BotsnBoltsGame.pc.pi.talk;
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
    
    protected final static Runnable newLevelRunner(final BotLevel level) {
        return new Runnable() {
            @Override public final void run() {
                Menu.prepareLevel(level);
                Menu.startLevelGameplay();
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
