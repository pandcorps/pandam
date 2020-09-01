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
package org.pandcorps.championsofslam;

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.col.*;
import org.pandcorps.core.img.*;
import org.pandcorps.game.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panteraction.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Fonts.*;
import org.pandcorps.championsofslam.Arena.*;
import org.pandcorps.championsofslam.Player.*;
import org.pandcorps.championsofslam.Champion.*;

public final class ChampionsOfSlamGame extends BaseGame {
    protected final static String TITLE = "Champions of Slam";
    protected final static String VERSION = "0.0.1";
    protected final static String YEAR = "2018";
    protected final static String AUTHOR = "Andrew M. Martin";
    
    protected final static String RES = "org/pandcorps/championsofslam/";
    
    protected final static int DIM = 16;
    protected final static int GAME_COLUMNS = 24;
    protected final static int GAME_ROWS = 14;
    protected final static int GAME_W = GAME_COLUMNS * DIM; // 384
    protected final static int GAME_H = GAME_ROWS * DIM; // 224;
    protected final static int INITIAL_OPPONENTS = Pantil.getProperty("org.pandcorps.championsofslam.opponents", 1);
    
    protected static Queue<Runnable> loaders = new LinkedList<Runnable>();
    protected static Panroom room = null;
    protected static Arena arena = null;
    protected static Font font = null;
    protected static Panmage imgArena = null;
    protected static Panmage imgChampion = null;
    protected static Pansound soundJab = null;
    protected static Pansound soundUppercut = null;
    protected static Panmage boundingBox = null;
    protected static boolean paused = true;
    private static boolean initialized = false;
    protected static List<String> saveFiles = null;
    private final static Set<Device> devices = new IdentityHashSet<Device>();
    private final static Set<Champion> team = new IdentityHashSet<Champion>();
    
    @Override
    protected final boolean isFullScreen() {
        return true;
    }
    
    @Override
    protected final int getGameWidth() {
        return GAME_W; // 24 tiles
    }
    
    @Override
    protected final int getGameHeight() {
        return GAME_H; // 14 tiles
    }
    
    @Override
    protected final void init(final Panroom room) throws Exception {
        final Pangine engine = Pangine.getEngine();
        engine.setTitle(TITLE);
        engine.setEntityMapEnabled(false);
        Pansound.setDefaultReplayThreshold(3);
        Imtil.onlyResources = true;
        if (loaders != null) {
            loaders.add(new Runnable() {
                @Override public final void run() {
                    loadResources();
                }});
        }
        Panscreen.set(new LogoScreen(ChampionsOfSlamScreen.class, loaders));
    }
    
    private final static void loadResources() {
        final Pangine engine = Pangine.getEngine();
        font = Fonts.getClassic(new FontRequest(FontType.Upper, 8), Pancolor.WHITE, Pancolor.WHITE, Pancolor.WHITE, null, Pancolor.BLACK);
        imgArena = engine.createImage(PRE_IMG + "arena", RES + "Arena.png");
        imgChampion = engine.createImage(PRE_IMG + "champion", RES + "Champion.png");
        boundingBox = engine.createEmptyImage(PRE_IMG + "boundingBox", null, new FinPanple2(-10, -2), new FinPanple2(10, 5));
        Images.load();
        loadSounds();
        findFiles();
    }
    
    private final static void loadSounds() {
        final Panaudio audio = Pangine.getEngine().getAudio();
        soundJab = audio.createSound(RES + "Jab.mid");
        soundUppercut = audio.createSound(RES + "Uppercut.mid");
    }
    
    protected final static String getFileName(final int fileIndex) {
        return fileIndex + ".champion.txt";
    }
    
    private final static void findFiles() {
        int n = 0;
        while (Iotil.exists(getFileName(n))) {
            n++;
        }
        saveFiles = new ArrayList<String>(n + 1);
        for (int i = 0; i < n; i++) {
            saveFiles.add(Iotil.read(getFileName(i)));
        }
    }
    
    protected final static void initOpponents() {
        final int minX = Math.round(Champion.minX) / 2, maxX = Math.round(Champion.maxX) / 2, minY = Math.round(Champion.minY) / 2, maxY = Math.round(Champion.maxY) / 2;
        for (int i = 0; i < INITIAL_OPPONENTS; i++) {
            final ChampionDefinition d2 = Champion.randomChampionDefinition();
            final Cpu cpu = new Cpu(d2, null);
            cpu.getPosition().set(Mathtil.randi(minX, maxX) * 2, Mathtil.randi(minY, maxY) * 2);
            cpu.setMirror(Mathtil.rand());
            room.addActor(cpu);
        }
    }
    
    protected final static class ChampionsOfSlamScreen extends Panscreen {
        @Override
        protected final void load() {
            final Pangine engine = Pangine.getEngine();
            engine.setRangeZ(-1, 1999);
            engine.enableColorArray();
            room = Pangame.getGame().getCurrentRoom();
            final Panlayer bg = engine.createLayer("bg", GAME_W, GAME_H, 0, room);
            room.addBeneath(bg);
            final ArenaDefinition arenaDef = new ArenaDefinition();
            arenaDef.ropeColor.r = arenaDef.ropeColor.g = 0.0f;
            arenaDef.turnbuckleColor.r = arenaDef.turnbuckleColor.g = arenaDef.turnbuckleColor.b = 0.5f;
            arenaDef.apronColor.r = arenaDef.apronColor.g = 0.5f;
            engine.setBgColor(arenaDef.ringColor.r, arenaDef.ringColor.g, arenaDef.ringColor.b);
            arena = new Arena(arenaDef);
            bg.addActor(arena);
            bg.setConstant(true);
            room.setClearDepthEnabled(false);
            final Panteraction interaction = engine.getInteraction();
            final Panput f1 = interaction.KEY_F1;
            final Panput f2 = interaction.KEY_F2;
            final Panput f3 = interaction.KEY_F3;
            arena.register(new ActionStartListener() {
                @Override public final void onActionStart(final ActionStartEvent event) {
                    final Panput input = event.getInput();
                    if (f1.equals(input)) {
                        engine.captureScreen();
                        return;
                    } else if (f2.equals(input)) {
                        engine.startCaptureFrames();
                        return;
                    } else if (f3.equals(input)) {
                        engine.stopCaptureFrames();
                        return;
                    }
                    final Device device = event.getDevice();
                    if (!devices.add(device)) {
                        return;
                    }
                    final ControlScheme ctrl = ControlScheme.getDefault(device);
                    if (!(ctrl.isMenuInput(input) || ctrl.isActionInput(input))) {
                        devices.remove(device);
                        return;
                    }
                    final PlayerContext pc = new PlayerContext(Champion.randomChampionDefinition(), ctrl);
                    team.add(new Player(room, pc, team));
                }});
            soundJab.startSound();
            soundUppercut.startSound();
        }
        
        @Override
        public final void step() {
            paused = Player.isAllPaused();
            if (initialized) {
                return;
            }
            initOpponents();
            initialized = true;
        }
    }
    
    public final static void main(final String[] args) {
        try {
            new ChampionsOfSlamGame().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
