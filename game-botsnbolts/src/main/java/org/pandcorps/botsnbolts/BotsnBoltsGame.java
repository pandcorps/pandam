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
import java.util.Map.*;

import org.pandcorps.botsnbolts.Boss.*;
import org.pandcorps.botsnbolts.HudMeter.*;
import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.botsnbolts.Profile.*;
import org.pandcorps.botsnbolts.RoomLoader.*;
import org.pandcorps.botsnbolts.ShootableDoor.*;
import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.game.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panteraction.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Fonts.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.pandax.visual.*;

public final class BotsnBoltsGame extends BaseGame {
    protected final static String TITLE = "Bots 'n Bolts";
    protected final static String VERSION = "0.0.1";
    protected final static String YEAR = "2016-2020";
    protected final static String AUTHOR = "Andrew M. Martin";
    protected final static String COPYRIGHT = "Copyright " + Pantext.CHAR_COPYRIGHT + " " + YEAR;
    
    /*
    Pause info/menu
    */
    
    protected final static String RES = "org/pandcorps/botsnbolts/";
    
    protected final static int DIM = 16;
    protected final static int GAME_COLUMNS = 24;
    protected final static int GAME_ROWS = 14;
    protected final static int GAME_W = GAME_COLUMNS * DIM; // 384
    protected final static int GAME_H = GAME_ROWS * DIM; // 224;
    
    protected final static byte TILE_LADDER = 2; // Works like non-solid when not climbing
    protected final static byte TILE_LADDER_TOP = 3; // Works like floor when not climbing
    protected final static byte TILE_FLOOR = 4; // Used for blocks that fade in/out
    protected final static byte TILE_UPSLOPE = 5;
    protected final static byte TILE_DOWNSLOPE = 6;
    protected final static byte TILE_ICE = 7;
    protected final static byte TILE_LIFT = 8;
    protected final static byte TILE_SAND = 9;
    protected final static byte TILE_DEFEAT = 10;
    protected final static byte TILE_CRUMBLE = 11;
    protected final static byte TILE_CONVEYOR_LEFT = 12;
    protected final static byte TILE_CONVEYOR_RIGHT = 13;
    protected final static byte TILE_PRESSURE_FIRE = 14;
    protected final static byte TILE_HURT = 15;
    protected final static byte TILE_BURSTABLE = 16;
    protected final static byte TILE_WATER = 17;
    protected final static byte TILE_ACTIVATE = 18;
    protected final static byte TILE_TRACTOR_BEAM = 19;
    
    protected final static int DEPTH_PARALLAX_BG = 0;
    protected final static int DEPTH_PARALLAX_FG = 2;
    protected final static int DEPTH_TEXTURE = 4;
    protected final static int DEPTH_BEHIND = 6;
    protected final static int DEPTH_BG = 8;
    protected final static int DEPTH_BETWEEN = 10;
    protected final static int DEPTH_FG = 12;
    protected final static int DEPTH_ABOVE = 14;
    protected final static int DEPTH_CARRIER = 16;
    protected final static int DEPTH_POWER_UP = 18;
    protected final static int DEPTH_PLAYER = 20;
    protected final static int DEPTH_ENEMY_BACK_2 = 22;
    protected final static int DEPTH_ENEMY_BACK = 24;
    protected final static int DEPTH_ENEMY = 26;
    protected final static int DEPTH_ENEMY_FRONT = 28;
    protected final static int DEPTH_PROJECTILE = 30;
    protected final static int DEPTH_OVERLAY = 32;
    protected final static int DEPTH_BURST = 66;
    protected final static int DEPTH_HUD = 68;
    protected final static int DEPTH_HUD_TEXT = 70;
    protected final static int DEPTH_HUD_OVERLAY = 72;
    protected final static byte Z_OFF_OVERLAY = 2;
    protected final static byte Z_OFF_TEXT = 4;
    protected final static int DEPTH_CURSOR = 74;
    
    protected final static FinPanple2 MIN_16 = new FinPanple2(-6, -6);
    protected final static FinPanple2 MAX_16 = new FinPanple2(6, 6);
    protected final static FinPanple2 MIN_8 = new FinPanple2(-3, -3);
    protected final static FinPanple2 MAX_8 = new FinPanple2(3, 3);
    
    protected final static FinPanple2 ng = GuyPlatform.getMin(Player.PLAYER_X);
    protected final static FinPanple2 xg = GuyPlatform.getMax(Player.PLAYER_X, Player.PLAYER_H);
    protected final static FinPanple2 og = new FinPanple2(17, 1);
    protected final static FinPanple2 oj = new FinPanple2(17, 4);
    protected final static FinPanple2 oss = new FinPanple2(13, 1);
    protected final static FinPanple2 os = new FinPanple2(15, 1);
    protected final static FinPanple2 ojs = new FinPanple2(15, 4);
    protected final static FinPanple originOverlay = new FinPanple(0, 0, DEPTH_OVERLAY);
    protected final static FinPanple2 minCube = new FinPanple2(-5, -5);
    protected final static FinPanple2 maxCube = new FinPanple2(5, 5);
    
    protected static Queue<Runnable> loaders = new LinkedList<Runnable>();
    protected static Font font = null;
    protected static PlayerImages voidImages = null;
    protected static PlayerImages volatileImages = null;
    protected static PlayerImages finalImages = null;
    protected final static Map<String, PlayerImages> playerImages = new HashMap<String, PlayerImages>();
    protected static Panframe[] doorTunnel = null;
    protected static Panframe[] doorTunnelOverlay = null;
    protected static Panframe[] doorTunnelSmall = null;
    protected static Panframe[] doorTunnelSmallOverlay = null;
    protected static Panmage doorBoltGenerator = null;
    protected static ShootableDoorDefinition doorCyan = null;
    protected static ShootableDoorDefinition doorGold = null;
    protected static ShootableDoorDefinition doorSilver = null;
    protected static ShootableDoorDefinition doorBlue = null;
    protected static ShootableDoorDefinition doorBlack = null;
    protected static ShootableDoorDefinition doorSmall = null;
    protected static Panmage button = null;
    protected static Panimation buttonFlash = null;
    protected static Panmage doorBoss = null;
    protected static Panmage box = null;
    protected static Panmage ladder = null;
    protected static Panmage[] blockCyan = null;
    protected static Panmage[] blockTimed = null;
    protected static Panmage[] blockButton = null;
    protected static Panmage[] blockHidden = null;
    protected static Panmage[] barrierHidden = null;
    protected static Panimation carrier = null;
    protected static Panimation lifter = null;
    protected static Panframe[][][] conveyorBelt = null;
    protected static Tile[][] conveyorBeltTiles = null;
    protected static Panmage blockSpike = null;
    protected static Panmage spike = null;
    protected static Panmage spikeTile = null;
    protected static Panmage[] sentryGun = null;
    protected static Panmage[] wallCannon = null;
    protected static Panimation propEnemy = null;
    protected static Panmage[] springEnemy = null;
    protected static Panimation crawlEnemy = null;
    protected static Panimation shieldedEnemy = null;
    protected static Panimation unshieldedEnemy = null;
    protected static Panmage[] fireballEnemy = null;
    protected static Panmage[] flamethrowerEnemy = null;
    protected static Panmage[] henchbotEnemy = null;
    protected static Panmage[] freezeRayEnemy = null;
    protected static Panmage electricityEnemy = null;
    protected static Panmage quicksandEnemy = null;
    protected static Panmage quicksandEnemyAttack = null;
    protected static Panmage[] magentaEnemy = null;
    protected static Panmage rockEnemy = null;
    protected static Panmage enemyProjectile = null;
    protected static Panimation enemyBurst = null;
    protected static Panimation puff = null;
    protected static Panimation flash = null;
    protected static Panmage[] flame4 = null;
    protected static Panmage[] flame8 = null;
    protected static Panimation flame16 = null;
    protected static Panmage iceShatter = null;
    protected static Panmage[] bubble = null;
    protected static Panimation splash = null;
    protected static Panmage[] ripple = null;
    protected static Panmage wind = null;
    protected static Panmage black = null;
    protected static Panmage pupil = null;
    protected static Panimation defeatOrbBoss = null;
    protected static Panmage diskGrey = null;
    protected static HudMeterImages hudMeterBlank = null;
    protected static HudMeterImages hudMeterBoss = null;
    protected static Img[] hudMeterImgs = null;
    private final static Map<String, Pansound> music = new HashMap<String, Pansound>();
    protected static Pansound musicIntro = null;
    protected static Pansound musicLevelSelect = null;
    protected static Pansound musicLevelStart = null;
    protected static Pansound musicFortressStart = null;
    protected static Pansound musicBoss = null;
    protected static Pansound musicEnding = null;
    protected static Pansound fxMenuHover = null;
    protected static Pansound fxMenuClick = null;
    protected static Pansound fxAttack = null;
    protected static Pansound fxImpact = null;
    protected static Pansound fxRicochet = null;
    protected static Pansound fxJump = null;
    protected static Pansound fxCharge = null;
    protected static Pansound fxChargedAttack = null;
    protected static Pansound fxSuperCharge = null;
    protected static Pansound fxSuperChargedAttack = null;
    protected static Pansound fxHurt = null;
    protected static Pansound fxDefeat = null;
    protected static Pansound fxHealth = null;
    protected static Pansound fxText = null;
    protected static Pansound fxEnemyAttack = null;
    protected static Pansound fxCrumble = null;
    protected static Pansound fxDoor = null;
    protected static Pansound fxBossDoor = null;
    protected static Pansound fxThunder = null;
    
    protected static PlayerContext pc = null;
    private static final float defPlayerStartX = 48;
    private static final float defPlayerStartY = 32;
    private static final boolean defPlayerStartMirror = false;
    protected static float playerStartX = defPlayerStartX;
    protected static float playerStartY = defPlayerStartY;
    protected static boolean playerStartMirror = defPlayerStartMirror;
    
    protected static Panlayer hud = null;
    protected static int prevTileSize = DIM;
    protected static int tileSize = DIM;
    protected static Panroom room = null;
    protected static TileMap tm = null;
    protected static String  timgName = null;
    protected static Panmage timg = null;
    protected static Panmage timgPrev = null;
    protected static TileMapImage[][] imgMap = null;
    protected static Panlayer bgLayer = null;
    protected static TileMap bgTm = null;
    protected static Pantexture bgTexture = null;

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
        Imtil.onlyResources = true;
        Panput.TouchButton.setOverlayOffsets(Z_OFF_OVERLAY, Z_OFF_TEXT);
        initTileBehaviors();
        if (loaders != null) {
            loaders.add(new Runnable() {
                @Override public final void run() {
                    loadResources();
                }});
        }
        Panscreen.set(new LogoScreen(TitleScreen.class, loaders));
    }
    
    @Override
    protected final boolean isClockRunning() {
        return Menu.isCursorNeeded() ? !Menu.isPauseMenuEnabled() : super.isClockRunning(); // Handle same as Player.isPaused
    }
    
    private final static void initTileBehaviors() {
        Chr.TILE_UPSLOPE = TILE_UPSLOPE;
        Chr.TILE_DOWNSLOPE = TILE_DOWNSLOPE;
        Chr.TILE_ICE = TILE_ICE;
        Chr.TILE_SAND = TILE_SAND;
    }
    
    private final static void loadResources() {
        font = Fonts.getClassic(new FontRequest(FontType.Upper, 8), Pancolor.WHITE, Pancolor.WHITE, Pancolor.WHITE, null, Pancolor.BLACK);
        loadDoors();
        loadMisc();
        loadEnemies();
        loadAudio();
        loadPlayer();
        postProcess();
        Menu.loadMenu();
        RoomLoader.loadRooms();
    }
    
    private final static void loadDoors() {
        final Pangine engine = Pangine.getEngine();
        doorTunnel = newDoor("door.tunnel", "bg/DoorTunnel.png");
        doorTunnelOverlay = toOverlay(doorTunnel);
        doorTunnelSmall = newDoorSmall("door.tunnel.small", "bg/DoorTunnelSmall.png");
        doorTunnelSmallOverlay = toOverlay(doorTunnelSmall);
        final Img[] imgsClosed = Imtil.loadStrip(RES + "bg/DoorCyan.png", 16, false);
        final Img[] imgsOpening = Imtil.loadStrip(RES + "bg/DoorCyanOpening.png", 16, false);
        final Img[] imgsBarrier = Imtil.loadStrip(RES + "bg/BarrierCyan.png", 8, false);
        doorCyan = newDoorDefinition("door.cyan", imgsClosed, imgsOpening, null, 0, null, null, imgsBarrier);
        final short s0 = 0, s48 = 48, s64 = 64, s96 = 96, s128 = 128, s144 = 144, s192 = 192, smax = Pancolor.MAX_VALUE;
        final Pancolor cyan = Pancolor.CYAN, silver = Pancolor.GREY, darkCyan = new FinPancolor(s0, s192, s192), darkSilver = Pancolor.DARK_GREY;
        doorSilver = filterDoor("door.silver", imgsClosed, imgsOpening, cyan, silver, darkCyan, darkSilver, null, 0, null,
            Integer.valueOf(Projectile.POWER_MAXIMUM), imgsBarrier);
        final Pancolor blue = newColorBlue(), darkBlue = newColorBlueDark();
        doorBlue = filterDoor("door.blue", imgsClosed, imgsOpening, silver, blue, darkSilver, darkBlue, null, 0, null,
            Integer.valueOf(Projectile.POWER_IMPOSSIBLE), imgsBarrier);
        final ShootableDoorDefinition doorRed, doorRedOrange, doorOrange, doorOrangeGold;
        final Pancolor red = Pancolor.RED, darkRed = new FinPancolor(s192, s0, s0);
        doorRed = filterDoor("door.red", imgsClosed, imgsOpening, blue, red, darkBlue, darkRed, null, 15, Player.SHOOT_RAPID, null, imgsBarrier);
        final Pancolor redOrange = new FinPancolor(smax, s64, s0), darkRedOrange = new FinPancolor(s192, s48, s0);
        doorRedOrange = filterDoor("door.red.orange", imgsClosed, null, red, redOrange, darkRed, darkRedOrange, doorRed, 10, Player.SHOOT_RAPID, null, imgsBarrier);
        final Pancolor orange = new FinPancolor(smax, s128, s0), darkOrange = new FinPancolor(s192, s96, s0);
        doorOrange = filterDoor("door.orange", imgsClosed, null, redOrange, orange, darkRedOrange, darkOrange, doorRedOrange, 6, null, null, imgsBarrier);
        final Pancolor orangeGold = new FinPancolor(smax, s192, s0), darkOrangeGold = new FinPancolor(s192, s144, s0);
        doorOrangeGold = filterDoor("door.orange.gold", imgsClosed, null, orange, orangeGold, darkOrange, darkOrangeGold, doorOrange, 3, null, null, imgsBarrier);
        final Pancolor gold = Pancolor.YELLOW, darkGold = new FinPancolor(s192, s192, s0);
        doorGold = filterDoor("door.gold", imgsClosed, null, orangeGold, gold, darkOrangeGold, darkGold, doorOrangeGold, 1, null, null, imgsBarrier);
        // No black barrier; it's not used; all barriers use grey 96 which is the black door's light color; do last so door/barrier images stay synchronized
        final Pancolor black = new FinPancolor(s96), darkBlack = new FinPancolor(s64);
        doorBlack = filterDoor("door.black", imgsClosed, imgsOpening, gold, black, darkGold, darkBlack, null, 0, null,
            Integer.valueOf(Projectile.POWER_IMPOSSIBLE), null); 
        Img.close(imgsClosed);
        Img.close(imgsOpening);
        final Img[] imgsSmallClosed = Imtil.loadStrip(RES + "bg/DoorSmall.png", 16, false);
        final Img[] imgsSmallOpening = Imtil.loadStrip(RES + "bg/DoorSmallOpening.png", 16, false);
        final Pancolor colSmall = new FinPancolor(smax, s64, smax), darkColSmall = new FinPancolor(s192, s48, s192);
        filterImgs(imgsBarrier, newFilter(gold, colSmall, darkGold, darkColSmall));
        doorSmall = newDoorDefinition("door.small", imgsSmallClosed, imgsSmallOpening, null, 0, Player.SHOOT_BOMB, null, imgsBarrier);
        Img.close(imgsSmallClosed);
        Img.close(imgsSmallOpening);
        filterImgs(imgsBarrier, newFilter(colSmall, newColorHidden(), darkColSmall, newColorHiddenDark()));
        barrierHidden = newBarrier("hidden", imgsBarrier);
        Img.close(imgsBarrier);
        final Img[] btnImgs = Imtil.loadStrip(RES + "bg/Button.png", 16);
        button = engine.createImage("button", btnImgs[0]);
        buttonFlash = createAnm("button.flash", 2, null, btnImgs[1], btnImgs[2], btnImgs[3]);
    }
    
    protected final static Panmage getDoorBoss() {
        if (doorBoss == null) {
            doorBoss = Pangine.getEngine().createImage("door.boss", RES + "bg/DoorBoss.png");
        }
        return doorBoss;
    }
    
    protected final static Panmage getDoorBoltGenerator() {
        if (doorBoltGenerator == null) {
            doorBoltGenerator = Pangine.getEngine().createImage("door.bolt.generator", RES + "bg/DoorBoltGenerator.png");
        }
        return doorBoltGenerator;
    }
    
    private final static void loadMisc() {
        final Pangine engine = Pangine.getEngine();
        black = engine.createImage("black", RES + "misc/Black.png");
        pupil = engine.createImage("pupil", new FinPanple2(1, 0), null, null, RES + "misc/Pupil.png");
        hudMeterBlank = newHudMeterImages("meter.blank", RES + "misc/MeterBlank.png");
        final Img[] blockImgs = Imtil.loadStrip(RES + "bg/BlockCyan.png", 16, false);
        final Panple maxBlock = new FinPanple2(14, 16);
        final short s0 = 0, s96 = 96, s128 = 128, s144 = 144, s192 = 192, smax = Pancolor.MAX_VALUE;
        final Pancolor cyan = Pancolor.CYAN, darkCyan = new FinPancolor(s0, s192, s192);
        blockCyan = newBlock("block.cyan", blockImgs, maxBlock, null, null, null, null);
        final Pancolor timed = new FinPancolor(s128, s192, smax), darkTimed = new FinPancolor(s96, s144, s192);
        blockTimed = newBlock("block.timed", blockImgs, maxBlock, cyan, timed, darkCyan, darkTimed);
        final Pancolor btn = newColorBlue(), darkBtn = newColorBlueDark();
        blockButton = newBlock("block.button", blockImgs, maxBlock, timed, btn, darkTimed, darkBtn);
        final Pancolor hid = newColorHidden(), darkHid = newColorHiddenDark();
        blockHidden = newBlock("block.hidden", blockImgs, maxBlock, btn, hid, darkBtn, darkHid);
        Img.close(blockImgs);
        final String preCarrier = "carrier";
        final Panmage carrierAll = engine.createImage(preCarrier, RES + "misc/Carrier.png");
        final Panple oCarrier = new FinPanple2(16, 11), nCarrier = new FinPanple2(-12, -1), xCarrier = new FinPanple2(12, 1), sCarrier = new FinPanple2(32, 16);
        final int dCarrier = 4;
        carrier = engine.createAnimation(preCarrier + ".anm",
            newSubFrame(preCarrier, 0, oCarrier, nCarrier, xCarrier, carrierAll, 0, 0, sCarrier, dCarrier),
            newSubFrame(preCarrier, 1, oCarrier, nCarrier, xCarrier, carrierAll, 0, 16, sCarrier, dCarrier));
        final String preLifter = "lifter";
        final Panmage lifterAll = engine.createImage(preLifter, RES + "misc/Lifter.png");
        final Panple sLifter = sCarrier;
        final int dLifter = 4;
        lifter = engine.createAnimation(preLifter + ".anm", //TODO Inconsistent conversion from pixels to rectangles; weird vibration effect; maybe need to always render squares
            newSubFrame(preLifter, 0, new FinPanple2(0, 1), null, null, lifterAll, 0, 0, sLifter, dLifter),
            newSubFrame(preLifter, 1, null, null, null, lifterAll, 0, 16, sLifter, dLifter));
        final Panmage[] sheetCb = newSheet("conveyor.belt", RES + "misc/ConveyorBelt.png", 16);
        final String preCb = PRE_FRM + "conveyor.belt.";
        final int framesCb = 4, partsCb = 3, dirsCb = 2;
        conveyorBelt = new Panframe[framesCb][partsCb][dirsCb];
        for (int frame = 0; frame < framesCb; frame++) {
            for (int part = 0; part < partsCb; part++) {
                final int partIndex, rot;
                if (part < 2) {
                    partIndex = part;
                    rot = 0;
                } else {
                    partIndex = 0;
                    rot = 2;
                }
                final Panmage imgCb = sheetCb[(frame * 2) + partIndex];
                for (int dir = 0; dir < dirsCb; dir++) {
                    final int partDir = (dir == 1) ? (2 - part) : part;
                    conveyorBelt[frame][partDir][dir] = engine.createFrame(preCb + frame + "." + part + "." + dir, imgCb, 1, rot, dir == 1, false);
                }
            }
        }
        puff = newAnimation("puff", RES + "misc/Puff.png", 8, CENTER_8, 2);
        flash = newAnimation("flash", RES + "misc/Flash.png", 32, CENTER_32, 12);
        bubble = newSheet("bubble", RES + "misc/Bubble.png", 8, CENTER_8, null, null);
        splash = newAnimation("splash", RES + "misc/Splash.png", 16, new FinPanple2(8, 0), 3);
        ripple = newSheet("ripple", RES + "misc/Ripple.png", 16, null, null, null);
        wind = engine.createImage("wind", RES + "misc/Wind.png");
    }
    
    protected final static void initConveyorBeltTiles() {
        if (conveyorBeltTiles != null) {
            return;
        }
        final Panframe[][] cb0 = conveyorBelt[0];
        final int partsCb = cb0.length, dirsCb = cb0[0].length;
        conveyorBeltTiles = new Tile[partsCb][dirsCb];
        final TileMapImage bg = imgMap[7][0];
        for (int part = 0; part < partsCb; part++) {
            final TileMapImage bgPart = (part == 1) ? bg : null;
            for (int dir = 0; dir < dirsCb; dir++) {
                conveyorBeltTiles[part][dir] = tm.getTile(bgPart, cb0[part][dir], (dir == 1) ? TILE_CONVEYOR_RIGHT : TILE_CONVEYOR_LEFT);
            }
        }
    }
    
    protected final static void stepConveyorBelt() {
        final Panframe[][] cbCurr = conveyorBelt[(int) (Pangine.getEngine().getClock() % 4)];
        final int partsCb = conveyorBeltTiles.length, dirsCb = conveyorBeltTiles[0].length;
        for (int part = 0; part < partsCb; part++) {
            for (int dir = 0; dir < dirsCb; dir++) {
                conveyorBeltTiles[part][dir].setForeground(cbCurr[part][dir]);
            }
        }
    }
    
    protected final static Panmage getBox() {
        return box;
    }
    
    protected final static Panmage getLadder() {
        if (ladder == null) {
            ladder = Pangine.getEngine().createImage("ladder", RES + "bg/Ladder.png");
        }
        return ladder;
    }
    
    protected final static Panmage getBlockSpike() {
        if (blockSpike == null) {
            blockSpike = Pangine.getEngine().createImage("block.spike", RES + "bg/BlockSpike.png");
        }
        return blockSpike;
    }
    
    protected final static Panmage getSpike() {
        if (spike == null) {
            spike = Pangine.getEngine().createImage("spike", CENTER_16, new FinPanple2(-6, -8), new FinPanple2(6, 7), RES + "bg/Spike.png");
        }
        return spike;
    }
    
    protected final static Panmage getSpikeTile() {
        if (spikeTile == null) {
            spikeTile = Pangine.getEngine().createImage("spikeTile", RES + "bg/SpikeTile.png");
        }
        return spikeTile;
    }
    
    private final static Pancolor newColorHidden() {
        final short s0 = 0, s192 = 192;
        return new FinPancolor(s192, s0, s192);
    }
    
    private final static Pancolor newColorHiddenDark() {
        final short s0 = 0, s128 = 128;
        return new FinPancolor(s128, s0, s128);
    }
    
    private final static Pancolor newColorBlue() {
        return Pancolor.BLUE;
    }
    
    private final static Pancolor newColorBlueDark() {
        final short s0 = 0, sb = 176;
        return new FinPancolor(s0, s0, sb);
    }
    
    private final static Panmage[] newBlock(final String id, final Img[] blockImgs, final Panple maxBlock, final Pancolor s1, final Pancolor d1, final Pancolor s2, final Pancolor d2) {
        if (s1 != null) {
            filterImgs(blockImgs, newFilter(s1, d1, s2, d2));
        }
        return newSheet(id, blockImgs, FinPanple.ORIGIN, ShootableDoor.minBarrier, maxBlock);
    }
    
    private final static void loadEnemies() {
        final Pangine engine = Pangine.getEngine();
        final Img[] sentryImgs = Imtil.loadStrip(RES + "enemy/SentryGun.png", 16);
        final int sentrySize = sentryImgs.length;
        sentryGun = new Panmage[sentrySize];
        for (int i = 0; i < sentrySize; i++) {
            sentryGun[i] = engine.createImage("sentry.gun." + i, CENTER_16, minCube, maxCube, sentryImgs[i]);
        }
        wallCannon = newSheet("wall.cannon", RES + "enemy/WallCannon.png", 16, null, new FinPanple2(1, 0), new FinPanple2(10, 14));
        final Panple propO = new FinPanple2(8, 1), propMin = Chr.getMin(Enemy.PROP_OFF_X), propMax = Chr.getMax(Enemy.PROP_OFF_X, Enemy.PROP_H);
        propEnemy = newAnimation("prop.enemy", RES + "enemy/PropEnemy.png", 16, propO, propMin, propMax, 4);
        springEnemy = newSheet("spring.enemy", RES + "enemy/SpringEnemy.png", 16, new FinPanple2(8, 3), propMin, propMax);
        final Panple crawlMax = Chr.getMax(Enemy.PROP_OFF_X, Enemy.CRAWL_H);
        crawlEnemy = newAnimation("crawl.enemy", RES + "enemy/CrawlEnemy.png", 16, propO, propMin, crawlMax, 4);
        final Panple shieldedO = new FinPanple2(8, 2), shieldedMax = propMax;
        shieldedEnemy = newAnimation("shielded.enemy", RES + "enemy/ShieldedEnemy.png", 16, shieldedO, new FinPanple2(propMin.getX(), -2), shieldedMax, 3);
        unshieldedEnemy = newAnimation("unshielded.enemy", RES + "enemy/UnshieldedEnemy.png", 16, shieldedO, propMin, shieldedMax, 3);
        fireballEnemy = newSheet("fireball.enemy", RES + "enemy/FireballEnemy.png", 16, propO, propMin, crawlMax);
        final Panple henchO = new FinPanple2(15, 1), henchMin = Chr.getMin(Enemy.HENCHBOT_OFF_X), henchMax = Chr.getMax(Enemy.HENCHBOT_OFF_X, Enemy.HENCHBOT_H);
        final Img[] henchImgs = Imtil.loadStrip(RES + "enemy/Henchbot.png", 32, false);
        flamethrowerEnemy = newSheet("flamethrower.enemy", henchImgs, henchO, henchMin, henchMax);
        final short s0 = 0, s48 = 48, s64 = 64, s72 = 72, s96 = 96, s128 = 128, s144 = 144, s160 = 160, s192 = 192, smax = Pancolor.MAX_VALUE;
        final Pancolor fire = new FinPancolor(smax, s160, s0), darkFire = new FinPancolor(smax, s96, s0);
        final Pancolor grey = Pancolor.DARK_GREY, darkGrey = new FinPancolor(s96);
        final Pancolor cyan = Pancolor.CYAN, darkCyan = new FinPancolor(s0, s192, s192);
        filterImgs(henchImgs, newFilter(fire, cyan, darkFire, darkCyan));
        henchbotEnemy = newSheet("henchbot.enemy", henchImgs, henchO, henchMin, henchMax);
        final Pancolor ice = newColorIce(), darkIce = newColorIceDark();
        final Pancolor blue = new FinPancolor(s128, s128, smax), darkBlue = new FinPancolor(s96, s96, s192);
        filterImgs(henchImgs, newFilter(cyan, ice, darkCyan, darkIce, grey, blue, darkGrey, darkBlue));
        freezeRayEnemy = newSheet("freezeray.enemy", henchImgs, henchO, henchMin, henchMax);
        final Img henchStill = henchImgs[0];
        final Pancolor rock = Pancolor.GREY, darkRock = new FinPancolor(s160);
        Imtil.filterImg(henchStill, newFilter(ice, rock, darkIce, darkRock, blue, grey, darkBlue, darkGrey));
        rockEnemy = engine.createImage("rock.enemy", henchO, henchMin, henchMax, henchStill);
        Img.close(henchImgs);
        final Img henchF = Imtil.load(RES + "enemy/ElectricityEnemy.png", false);
        electricityEnemy = engine.createImage("electricity.enemy", henchO, henchMin, henchMax, henchF);
        final Pancolor yellow = Pancolor.YELLOW, darkYellow = new FinPancolor(s192, s192, s0);
        final Pancolor sand = new FinPancolor(s192, s192, s72), darkSand = new FinPancolor(s128, s128, s48);
        final Pancolor brown = new FinPancolor(s144, s96, s72), darkBrown = new FinPancolor(s96, s64, s48);
        Imtil.filterImg(henchF, newFilter(yellow, sand, darkYellow, darkSand, grey, brown, darkGrey, darkBrown));
        quicksandEnemy = engine.createImage("quicksand.enemy", henchO, henchMin, henchMax, henchF);
        final Img henchAttackF = Imtil.load(RES + "enemy/QuicksandEnemyAttack.png", false);
        quicksandEnemyAttack = engine.createImage("quicksand.enemy.attack", henchO, henchMin, henchMax, henchAttackF);
        final Pancolor magenta = new FinPancolor(s192, s0, s192), darkMagenta = new FinPancolor(s128, s0, s128);
        filterImgs(new Img[] { henchF, henchAttackF }, newFilter(sand, magenta, darkSand, darkMagenta, brown, grey, darkBrown, darkGrey));
        magentaEnemy = new Panmage[] {
                engine.createImage("magenta.enemy", henchO, henchMin, henchMax, henchF),
                engine.createImage("magenta.enemy.attack", henchO, henchMin, henchMax, henchAttackF),
                engine.createImage("magenta.enemy.jump", henchO, henchMin, henchMax, RES + "enemy/MagentaEnemyJump.png")
        };
        Img.close(henchF);
        Img.close(henchAttackF);
        enemyBurst = newAnimation("burst.enemy", RES + "enemy/EnemyBurst.png", 16, CENTER_16, minCube, maxCube, 2);
        flame4 = newSheet("flame.4.enemy", RES + "enemy/Flame4.png", 4);
        flame8 = newSheet("flame.8.enemy", RES + "enemy/Flame8.png", 8);
        flame16 = newAnimation("flame.16.enemy", RES + "enemy/Flame16.png", 16, propO, propMin, propMax, 3);
        Boss.FortCannon.getFortCannonImage(0);
    }
    
    private final static Pancolor newColorIce() {
        final short s160 = 160, s208 = 208;
        return new FinPancolor(s160, s208, Pancolor.MAX_VALUE);
    }
    
    private final static Pancolor newColorIceDark() {
        final short s96 = 96, s176 = 176;
        return new FinPancolor(s96, s176, Pancolor.MAX_VALUE);
    }
    
    protected final static Panmage getEnemyProjectile() {
        if (enemyProjectile == null) {
            enemyProjectile = Pangine.getEngine().createImage("projectile.enemy", CENTER_8, new FinPanple2(-2, -2), new FinPanple2(2, 2), RES + "enemy/EnemyProjectile.png");
        }
        return enemyProjectile;
    }
    
    protected final static Panmage getIceShatter() {
        if (iceShatter == null) {
            iceShatter = Pangine.getEngine().createImage("ice.shatter", CENTER_8, null, null, RES + "misc/IceShatter.png");
        }
        return iceShatter;
    }
    
    private final static ShootableDoorDefinition filterDoor(final String id, final Img[] imgsClosed, final Img[] imgsOpening,
            final Pancolor s1, final Pancolor d1, final Pancolor s2, final Pancolor d2,
            final ShootableDoorDefinition next, final int nextTemperature, final ShootMode requiredShootMode, final Integer requiredPower,
            final Img[] imgsBarrier) {
        final PixelFilter filter = newFilter(s1, d1, s2, d2);
        filterImgs(imgsClosed, filter);
        filterImgs(imgsOpening, filter);
        filterImgs(imgsBarrier, filter);
        return newDoorDefinition(id, imgsClosed, imgsOpening, next, nextTemperature, requiredShootMode, requiredPower, imgsBarrier);
    }
    
    private final static ReplacePixelFilter newFilter(final Pancolor s1, final Pancolor d1, final Pancolor s2, final Pancolor d2) {
        final ReplacePixelFilter filter = new ReplacePixelFilter();
        filter.put(s1, d1);
        filter.put(s2, d2);
        return filter;
    }
    
    private final static PixelFilter newFilter(final Pancolor s1, final Pancolor d1, final Pancolor s2, final Pancolor d2,
                                               final Pancolor s3, final Pancolor d3, final Pancolor s4, final Pancolor d4) {
        final ReplacePixelFilter filter = newFilter(s1, d1, s2, d2);
        filter.put(s3, d3);
        filter.put(s4, d4);
        return filter;
    }
    
    private final static void loadPlayer() {
        final String dir = "betabot", name = "Void";
        openPlayerImages(dir, name);
        voidImages = loadPlayerImages(dir, name, "Byte", "Baud", null, true);
        final short s0 = 0, s192 = 192;
        filterPlayerImages(Pancolor.GREEN, Pancolor.CYAN, new FinPancolor(s0, s192, s0), new FinPancolor(s0, s192, s192));
        playerMirror = false;
        volatileImages = loadPlayerImages("volatile", "Volatile", "Byte", "Baud", null, true);
        finalImages = loadPlayerImages("final", "Final", "Byte", "Baud", volatileImages, false);
        closePlayerImages();
        pc = new PlayerContext(new Profile(), voidImages);
    }
    
    private static Img[] playerDefeatOrb = null;
    private static Img playerProjectile = null;
    private static Img playerProjectile2 = null;
    private static Img[] playerProjectile3 = null;
    private static Img[] playerBurst = null;
    private static Img[] playerCharge = null;
    private static Img[] playerChargeVert = null;
    private static Img[] playerCharge2 = null;
    private static Img[] playerChargeVert2 = null;
    private static Img playerWarp = null;
    private static Img[] playerMaterialize = null;
    private static Img[] playerBomb = null;
    private static Img playerLink = null;
    private static Img[] playerBatterySmall = null;
    private static Img[] playerBatteryMedium = null;
    private static Img[] playerBatteryBig = null;
    private static Img playerDoorBolt = null;
    private static Img playerBolt = null;
    private static Img playerDisk = null;
    private static Img playerPowerBox = null;
    private static Map<String, Img> playerBoltBoxes = null;
    private static Img playerDiskBox = null;
    private static Img playerHighlightBox = null;
    private static boolean playerMirror = true;
    
    private final static void openPlayerImages(final String dir, final String name) {
        final String pre = getCharacterPrefix(dir, name);
        playerDefeatOrb = Imtil.loadStrip(pre + "DefeatOrb.png", 16, false);
        playerProjectile = Imtil.load(pre + "Projectile.png", false);
        playerProjectile2 = Imtil.load(pre + "Projectile2.png", false);
        playerProjectile3 = Imtil.loadStrip(pre + "Projectile3.png", 32, false);
        playerBurst = Imtil.loadStrip(pre + "Burst.png", 16, false);
        playerCharge = Imtil.loadStrip(pre + "Charge.png", 8, false);
        playerChargeVert = Imtil.loadStrip(pre + "ChargeVert.png", 8, false);
        playerCharge2 = Imtil.loadStrip(pre + "Charge2.png", 8, false);
        playerChargeVert2 = Imtil.loadStrip(pre + "ChargeVert2.png", 8, false);
        playerWarp = Imtil.load(pre + "Warp.png", false);
        playerMaterialize = Imtil.loadStrip(pre + "Materialize.png", 32, false);
        playerBomb = Imtil.loadStrip(pre + "Bomb.png", 8, false);
        playerLink = Imtil.load(pre + "Link.png", false);
        playerBatterySmall = Imtil.loadStrip(pre + "BatterySmall.png", 8, false);
        playerBatteryMedium = Imtil.loadStrip(pre + "BatteryMedium.png", 16, false);
        playerBatteryBig = Imtil.loadStrip(pre + "BatteryBig.png", 16, false);
        playerDoorBolt = Imtil.load(pre + "DoorBolt.png", false);
        playerBolt = Imtil.load(pre + "Bolt.png", false);
        playerDisk = Imtil.load(pre + "Disk.png", false);
        playerPowerBox = Imtil.load(pre + "PowerBox.png", false);
        playerBoltBoxes = new HashMap<String, Img>(Profile.UPGRADES.length);
        for (final Upgrade upgrade : Profile.UPGRADES) {
            final String upgradeName = upgrade.name;
            playerBoltBoxes.put(upgradeName, Imtil.load(pre + upgradeName + "Box.png", false));
        }
        playerDiskBox = Imtil.load(pre + "DiskBox.png", false);
        playerHighlightBox = Imtil.load(RES + "misc/Box.png", false);
        hudMeterImgs = Imtil.loadStrip(pre + "Meter.png", 8, false);
        box = Pangine.getEngine().createImage("box", playerHighlightBox);
        final short s0 = 0, s96 = 96, s192 = 192;
        Imtil.filterImg(playerHighlightBox, newFilter(Pancolor.DARK_GREY, Pancolor.GREEN, new Pancolor(s96), new Pancolor(s0, s192, s0)));
    }
    
    private final static void filterPlayerImages(final Pancolor s1, final Pancolor d1, final Pancolor s2, final Pancolor d2) {
        final PixelFilter[] f = { newFilter(s1, d1, s2, d2) };
        filterImgs(playerDefeatOrb, f);
        Imtil.filterImg(playerProjectile, f);
        Imtil.filterImg(playerProjectile2, f);
        filterImgs(playerProjectile3, f);
        filterImgs(playerBurst, f);
        filterImgs(playerCharge, f);
        filterImgs(playerChargeVert, f);
        filterImgs(playerCharge2, f);
        filterImgs(playerChargeVert2, f);
        Imtil.filterImg(playerWarp, f);
        filterImgs(playerMaterialize, f);
        filterImgs(playerBomb, f);
        Imtil.filterImg(playerLink, f);
        filterImgs(playerBatterySmall, f);
        filterImgs(playerBatteryMedium, f);
        filterImgs(playerBatteryBig, f);
        Imtil.filterImg(playerDoorBolt, f);
        Imtil.filterImg(playerBolt, f);
        Imtil.filterImg(playerDisk, f);
        Imtil.filterImg(playerPowerBox, f);
        for (final Img img : playerBoltBoxes.values()) {
            Imtil.filterImg(img, f);
        }
        Imtil.filterImg(playerDiskBox, f);
        Imtil.filterImg(playerHighlightBox, f);
        filterImgs(hudMeterImgs, f);
    }
    
    private final static void closePlayerImages() {
        playerProjectile.close();
        playerProjectile = null;
        playerProjectile2.close();
        playerProjectile2 = null;
        Img.close(playerProjectile3);
        playerProjectile3 = null;
        Img.close(playerBurst);
        playerBurst = null;
        Img.close(playerCharge);
        playerCharge = null;
        Img.close(playerChargeVert);
        playerChargeVert = null;
        Img.close(playerCharge2);
        playerCharge2 = null;
        Img.close(playerChargeVert2);
        playerChargeVert2 = null;
        playerWarp.close();
        playerWarp = null;
        Img.close(playerMaterialize);
        playerMaterialize = null;
        Img.close(playerBomb);
        playerBomb = null;
        playerLink.close();
        playerLink = null;
        Img.close(playerBatterySmall);
        playerBatterySmall = null;
        Img.close(playerBatteryMedium);
        playerBatteryMedium = null;
        Img.close(playerBatteryBig);
        playerBatteryBig = null;
        playerDoorBolt.close();
        playerDoorBolt = null;
        playerBolt.close();
        playerBolt = null;
        playerPowerBox.close();
        playerPowerBox = null;
        Img.close(playerBoltBoxes.values());
        playerBoltBoxes = null;
        playerDiskBox.close();
        playerDiskBox = null;
        playerHighlightBox.close();
        playerHighlightBox = null;
        //playerDefeatOrb/playerDisk/hudMeterImgs closed separately
    }
    
    private final static String getCharacterPrefix(final String dir, final String name) {
        return RES + "chr/" + dir + "/" + name;
    }
    
    private final static PlayerImages loadPlayerImages(final String dir, final String name, final String animalName, final String birdName, final PlayerImages src, final boolean pupilNeeded) {
        final String pre = getCharacterPrefix(dir, name);
        final PlayerImagesSubSet basicSet = loadPlayerImagesSubSet(pre, name, true, og, og, oj);
        final PlayerImagesSubSet shootSet = loadPlayerImagesSubSet(pre + "Shoot", name + ".shoot", false, oss, os, ojs);
        final Pangine engine = Pangine.getEngine();
        final Img imgHurt = Imtil.load(pre + "Hurt.png", false), imgHurtMirror = playerMirror ? Imtil.load(pre + "HurtMirror.png", false) : null;
        final Panmage hurt = newPlayerImage(PRE_IMG + "." + name + ".hurt", oj, imgHurt, imgHurtMirror);
        final short s0 = 0, s72 = 72, s96 = 96, s128 = 128, s144 = 144, s192 = 192;
        final Pancolor grey = Pancolor.DARK_GREY, darkGrey = new FinPancolor(s96);
        final Pancolor pri = Pancolor.GREEN, darkPri = new FinPancolor(s0, s192, s0);
        final Pancolor skin = new FinPancolor(s192, s128, s96), darkSkin = new FinPancolor(s144, s96, s72);
        final Pancolor frz = Pancolor.WHITE, darkFrz = newColorIce();
        final Pancolor out = Pancolor.BLACK, outFrz = newColorIceDark();
        final ReplacePixelFilter frzFilter = new ReplacePixelFilter(grey, frz, darkGrey, darkFrz, pri, frz, darkPri, darkFrz, skin, frz, darkSkin, darkFrz, out, outFrz);
        if (playerMirror) {
            filterImgs(new Img[] { imgHurt, imgHurtMirror }, frzFilter);
        } else {
            Imtil.filterImg(imgHurt, frzFilter);
        }
        final Panmage frozen = newPlayerImage(PRE_IMG + "." + name + ".frozen", oj, imgHurt, imgHurtMirror);
        Img.close(imgHurt, imgHurtMirror);
        
        final Panimation defeat = newAnimation(pre + "DefeatOrb", playerDefeatOrb, CENTER_16, 6);
        
        final Panple oClimb = new FinPanple2(15, 4);
        final Img[] climbImgs = Imtil.loadStrip(pre + "Climb.png", 32);
        final Img[] climbImgsMirror = loadPlayerMirrorStrip(pre + "ClimbMirror.png");
        final Panmage climb = newPlayerImage(pre + "Climb", oClimb, climbImgs, climbImgsMirror, 0);
        final Panmage climbShoot = newPlayerImage(pre + "Climb.Shoot", oClimb, climbImgs, climbImgsMirror, 1);
        final Panmage climbTop = newPlayerImage(pre + "Climb.Top", oClimb, climbImgs, climbImgsMirror, 2);
        final Img[] jumpAimImgs = Imtil.loadStrip(pre + "JumpAim.png", 32);
        final Img[] jumpAimImgsMirror = loadPlayerMirrorStrip(pre + "JumpAimMirror.png");
        final Panmage jumpAimDiag = newPlayerImage(pre + "Jump.Aim.Diag", ojs, jumpAimImgs, jumpAimImgsMirror, 0);
        final Panmage jumpAimUp = newPlayerImage(pre + "Jump.Aim.Diag", ojs, jumpAimImgs, jumpAimImgsMirror, 1);
        final Img imgTalk = Imtil.load(pre + "Talk.png"), imgTalkMirror = playerMirror ? Imtil.load(pre + "TalkMirror.png") : null;
        final Panmage talk = newPlayerImage(PRE_IMG + "." + name + ".talk", og, imgTalk, imgTalkMirror);
        
        final Panmage basicProjectile = (src == null) ? engine.createImage(pre + "Projectile", new FinPanple2(3, 3), new FinPanple2(-3, -2), new FinPanple2(5, 3), playerProjectile) : src.basicProjectile;
        final Panimation projectile2 = (src == null) ? newFlipper(pre + "Projectile2", playerProjectile2, new FinPanple2(7, 7), new FinPanple2(-4, -5), new FinPanple2(8, 6), 4) : src.projectile2;
        final Panimation projectile3 = (src == null) ? createAnm(pre + "Projectile3", 2, new FinPanple2(23, 7), new FinPanple2(-6, -7), new FinPanple2(8, 8), playerProjectile3) : src.projectile3;
        final Panimation burst = (src == null) ? newAnimation(pre + "Burst", playerBurst, CENTER_16, new FinPanple2(-10, -10), new FinPanple2(10, 10), 2) : src.burst;
        final Panimation charge = (src == null) ? newAnimation(pre + "Charge", playerCharge, null, 1) : src.charge;
        final Panple oChargeVert = new FinPanple2(4, 0);
        final Panimation chargeVert = (src == null) ? newAnimation(pre + "ChargeVert", playerChargeVert, oChargeVert, 1) : src.chargeVert;
        final Panimation charge2 = (src == null) ? newAnimation(pre + "Charge2", playerCharge2, null, 1) : src.charge2;
        final Panimation chargeVert2 = (src == null) ? newAnimation(pre + "ChargeVert2", playerChargeVert2, oChargeVert, 1) : src.chargeVert2;
        
        final Panframe ball[] = new Panframe[8];
        final Panple ob = new FinPanple2(8, 1), xb = GuyPlatform.getMax(Player.PLAYER_X, Player.BALL_H);
        final Panmage[] ballImgs = createSheet(pre + "Ball", pre + "Ball.png", 16, ob, ng, xb);
        final Rotator rots = new Rotator(4) {
            @Override protected final Panmage getImage1() {
                return ballImgs[0]; }
            @Override protected final Panmage getImage2() {
                return ballImgs[1]; }};
        for (int i = 0; i < 8; i++) {
            rots.getFrame(ball, i);
        }
        
        final Panmage warp = (src == null) ? engine.createImage(pre + "Warp", new FinPanple2(5, 1), null, null, playerWarp) : src.warp;
        final Panimation materialize = (src == null) ? newAnimation(pre + "Materialize", playerMaterialize, og, 3) : src.materialize;
        final Panimation bomb = (src == null) ? newAnimation(pre + "Bomb", playerBomb, CENTER_8, 5) : src.bomb;
        final Panmage link = (src == null) ? engine.createImage(pre + "Link", new FinPanple2(4, 1), null, null, playerLink) : src.link;
        final Panple oBattery = new FinPanple2(8, -1);
        final Panimation batterySml = (src == null) ? newOscillation(pre + "battery.sml", playerBatterySmall, new FinPanple2(4, -1), new FinPanple2(-2, 2), new FinPanple2(2, 6), 3, 6) : src.batterySmall;
        final Panimation batteryMed = (src == null) ? newOscillation(pre + "battery.med", playerBatteryMedium, oBattery, new FinPanple2(-4, 2), new FinPanple2(4, 10), 3, 6) : src.batteryMedium;
        final Panple minBatteryBig = new FinPanple2(-6, 2), maxBatteryBig = new FinPanple2(6, 14);
        final Panimation batteryBig = (src == null) ? newOscillation(pre + "battery.big", playerBatteryBig, oBattery, minBatteryBig, maxBatteryBig, 3, 6) : src.batteryBig;
        final Panmage doorBolt = (src == null) ? engine.createImage(pre + "DoorBolt", playerDoorBolt) : src.doorBolt;
        final Panmage bolt = (src == null) ? engine.createImage(pre + "Bolt", oBattery, minBatteryBig, maxBatteryBig, playerBolt) : src.bolt;
        final Panmage disk = (src == null) ? engine.createImage(pre + "Disk", oBattery, minBatteryBig, maxBatteryBig, playerDisk) : src.disk;
        final Panmage powerBox = (src == null) ? engine.createImage(pre + "PowerBox", CENTER_16, minCube, maxCube, playerPowerBox) : src.powerBox;
        final Map<String, Panmage> boltBoxes = new HashMap<String, Panmage>(Profile.UPGRADES.length);
        for (final Entry<String, Img> entry : playerBoltBoxes.entrySet()) {
            final String boltName = entry.getKey();
            boltBoxes.put(boltName, (src == null) ? engine.createImage(pre + boltName + "Box", CENTER_16, minCube, maxCube, entry.getValue()) : src.boltBoxes.get(boltName));
        }
        final Panmage diskBox = (src == null) ? engine.createImage(pre + "DiskBox", CENTER_16, minCube, maxCube, playerDiskBox) : src.diskBox;
        final Panmage highlightBox = (src == null) ? engine.createImage(pre + "HighlightBox", playerHighlightBox) : src.highlightBox;
        
        final String portraitLoc = pre + "Portrait.png";
        final Panmage portrait = engine.createImage(pre + "Portrait", portraitLoc);
        if (pupilNeeded) {
            Story.pupilNeededSet.add(portrait);
        }
        
        final HudMeterImages hudMeterImages = (src == null) ? newHudMeterImages(pre + "Meter", hudMeterImgs) : src.hudMeterImages;
        
        final PlayerImages pi;
        pi = new PlayerImages(basicSet, shootSet, hurt, frozen, defeat, climb, climbShoot, climbTop, jumpAimDiag, jumpAimUp, talk, basicProjectile, projectile2, projectile3, charge, chargeVert, charge2, chargeVert2,
            burst, ball, warp, materialize, bomb, link, batterySml, batteryMed, batteryBig, doorBolt, bolt, disk, powerBox, boltBoxes, diskBox, highlightBox, portrait, hudMeterImages, animalName, birdName);
        playerImages.put(portraitLoc, pi);
        return pi;
    }
    
    private final static PlayerImagesSubSet loadPlayerImagesSubSet(final String path, final String name, final boolean startNeeded, final Panple os, final Panple o, final Panple oj) {
        final String pre = PRE_IMG + "." + name + ".";
        final Img[] imgs = Imtil.loadStrip(path + ".png", 32);
        final Img[] imgsMirror = loadPlayerMirrorStrip(path + "Mirror.png");
        final Panmage still = newPlayerImage(pre + "still", os, imgs, imgsMirror, 0);
        final Panmage run1 = newPlayerImage(pre + "run.1", o, imgs, imgsMirror, 1);
        final Panmage run2 = newPlayerImage(pre + "run.2", o, imgs, imgsMirror, 2);
        final Panmage run3 = newPlayerImage(pre + "run.3", o, imgs, imgsMirror, 3);
        final Panmage jump = newPlayerImage(pre + "jump", oj, imgs, imgsMirror, 4);
        final Panmage start, blink, crouch[];
        if (startNeeded) {
            start = newPlayerImage(pre + "start", o, path + "Start");
            blink = newPlayerImage(pre + "blink", o, path + "Blink");
            final Img[] crouchImgs = Imtil.loadStrip(path + "Crouch.png", 32);
            final Img[] crouchImgsMirror = loadPlayerMirrorStrip(path + "CrouchMirror.png");
            final int crouchSize = crouchImgs.length;
            crouch = new Panmage[crouchSize];
            for (int i = 0; i < crouchSize; i++) {
                crouch[i] = newPlayerImage(pre + "crouch." + i, o, crouchImgs, crouchImgsMirror, i);
            }
        } else {
            start = null;
            blink = null;
            crouch = null;
        }
        return new PlayerImagesSubSet(still, jump, new Panmage[] { run1, run2, run3 }, start, blink, crouch);
    }
    
    private final static Img[] loadPlayerMirrorStrip(final String loc) {
        return playerMirror ? Imtil.loadStrip(loc, 32) : null;
    }
    
    private final static Panframe newSubFrame(final String pre, final int i, final Panple o, final Panple min, final Panple max, final Panmage src, final float x, final float y, final Panple size, final int dur) {
        return Pangine.getEngine().createFrame(pre + ".frm." + i, new SubPanmage(pre + ".sub." + i, o, min, max, src, x, y, size), dur);
    }
    
    private final static void postProcess() {
        final short s0 = 0, s96 = 96, s192 = 192;
        final PixelFilter[] greyFilter = { newFilter(Pancolor.CYAN, Pancolor.DARK_GREY, new FinPancolor(s0, s192, s192), new FinPancolor(s96)) };
        filterImgs(playerDefeatOrb, greyFilter);
        defeatOrbBoss = newAnimation("defeat.orb.boss", playerDefeatOrb, CENTER_16, voidImages.defeat.getFrames()[0].getDuration());
        Imtil.filterImg(playerDisk, greyFilter);
        final Panmage disk = voidImages.disk;
        diskGrey = Pangine.getEngine().createImage("disk.grey", disk.getOrigin(), disk.getBoundingMinimum(), disk.getBoundingMaximum(), playerDisk);
        filterImgs(hudMeterImgs, greyFilter);
        hudMeterBoss = newHudMeterImages("meter.boss", hudMeterImgs);
        Img.close(playerDefeatOrb);
        playerDefeatOrb = null;
        playerDisk.close();
        playerDisk = null;
        Img.close(hudMeterImgs);
        hudMeterImgs = null;
    }
    
    private final static void filterImgs(final Img[] imgs, final PixelFilter... fs) {
        if (imgs == null) {
            return;
        }
        for (final Img img : imgs) {
            Imtil.filterImg(img, fs);
        }
    }
    
    private final static Panmage[] newSheet(final String id, final String path, final int w) {
        return newSheet(id, path, w, null, null, null);
    }
    
    private final static Panmage[] newSheet(final String id, final String path, final int w, final Panple o, final Panple min, final Panple max) {
        return newSheet(id, Imtil.loadStrip(path, w), o, min, max);
    }
    
    private final static Panmage[] newSheet(final String id, final Img[] imgs) {
        return newSheet(id, imgs, null, null, null);
    }
    
    private final static Panmage[] newSheet(final String id, final Img[] imgs, final Panple o, final Panple min, final Panple max) {
        final int size = imgs.length;
        final Panmage[] sheet = new Panmage[size];
        final Pangine engine = Pangine.getEngine();
        for (int i = 0; i < size; i++) {
            sheet[i] = engine.createImage(id + "." + i, o, min, max, imgs[i]);
        }
        return sheet;
    }
    
    private final static Panimation newAnimation(final String id, final String path, final int w, final Panple o, final int dur) {
        return newAnimation(id, Imtil.loadStrip(path, w), o, dur);
    }
    
    private final static Panimation newAnimation(final String id, final Img[] imgs, final Panple o, final int dur) {
        return newAnimation(id, imgs, o, null, null, dur);
    }
    
    private final static Panimation newAnimation(final String id, final String path, final int w, final Panple o, final Panple min, final Panple max, final int dur) {
        return newAnimation(id, Imtil.loadStrip(path, w), o, min, max, dur);
    }
    
    private final static Panimation newAnimation(final String id, final Img[] imgs, final Panple o, final Panple min, final Panple max, final int dur) {
        return Pangine.getEngine().createAnimation(PRE_ANM + id, newFrames(id, imgs, o, min, max, dur, dur, dur, false));
    }
    
    private final static Panframe[] newFrames(final String id, final Img[] imgs, final Panple o, final Panple min, final Panple max,
                                              final int durStart, final int durMid, final int durEnd, final boolean oscillate) {
        final int size = imgs.length, end = size - 1;
        final Panframe[] frames = new Panframe[oscillate ? ((size * 2) - 2) : size];
        final Pangine engine = Pangine.getEngine();
        for (int i = 0; i < size; i++) {
            final String iid = id + "." + i;
            final Panmage image = engine.createImage(iid, o, min, max, imgs[i]);
            final int dur;
            if (i == 0) {
                dur = durStart;
            } else if (i == end) {
                dur = durEnd;
            } else {
                dur = durMid;
            }
            frames[i] = engine.createFrame(PRE_FRM + iid, image, dur);
        }
        return frames;
    }
    
    private final static Panimation newOscillation(final String id, final Img[] imgs, final Panple o, final Panple min, final Panple max, final int durEdge, final int durMid) {
        final Panframe[] frames = newFrames(id, imgs, o, min, max, durEdge, durMid, durEdge, true);
        final int mid = (frames.length / 2) - 1, off = mid + 2;
        for (int i = 0; i < mid; i++) {
            frames[off + i] = frames[mid - i];
        }
        return Pangine.getEngine().createAnimation(PRE_ANM + id, frames);
    }
    
    private final static Panimation newFlipper(final String id, final Img _img, final Panple o, final Panple min, final Panple max, final int dur) {
        final Pangine engine = Pangine.getEngine();
        final Panmage img = engine.createImage(id, o, min, max, _img);
        final Panframe[] frames = new Panframe[2];
        frames[0] = engine.createFrame(id + ".0", img, dur);
        frames[1] = engine.createFrame(id + ".1", img, dur, 0, false, true);
        return engine.createAnimation(PRE_ANM + id, frames);
    }
    
    private final static Panmage newPlayerImage(final String id, final Panple o, final Img[] imgs, final Img[] imgsMirror, final int i) {
        return newPlayerImage(id, o, imgs[i], playerMirror ? imgsMirror[i] : null);
    }
    
    private final static Panmage newPlayerImage(final String id, final Panple o, final String path) {
        return newPlayerImage(id, o, Imtil.load(path + ".png"), playerMirror ? Imtil.load(path + "Mirror.png") : null);
    }
    
    private final static Panmage newPlayerImage(final String id, final Panple o, final Img img, final Img imgMirror) {
        final Pangine engine = Pangine.getEngine();
        final Panmage image = engine.createImage(id, o, ng, xg, img);
        if (playerMirror) {
            image.setMirrorSource(engine.createImage(id + ".mirror", o, ng, xg, imgMirror));
        }
        return image;
    }
    
    private final static HudMeterImages newHudMeterImages(final String id, final String path) {
        return newHudMeterImages(id, Imtil.loadStrip(path, 8));
    }
    
    private final static HudMeterImages newHudMeterImages(final String id, final Img[] imgs) {
        return new HudMeterImages(newSheet(id, imgs));
    }
    
    private final static ShootableDoorDefinition newDoorDefinition(final String id, final Img[] imgsClosed, final Img[] imgsOpening,
            final ShootableDoorDefinition next, final int nextTemperature, final ShootMode requiredShootMode, final Integer requiredPower,
            final Img[] imgsBarrier) {
        final boolean small = imgsClosed.length <= 1;
        final Panframe[] door = newDoor(id, imgsClosed, 0, small);
        final Panframe[][] opening;
        if (imgsOpening == null) {
            opening = null;
        } else {
            final int n = small ? 1 : 2;
            final Panframe[] open1 = newDoor(id + ".1", imgsOpening, 0, small);
            final Panframe[] open2 = newDoor(id + ".2", imgsOpening, n, small);
            final Panframe[] open3 = newDoor(id + ".3", imgsOpening, 2 * n, small);
            opening = new Panframe[][] { open1, open2, open3 };
        }
        final Panmage[] barrier = newBarrier(id, imgsBarrier);
        return new ShootableDoorDefinition(door, opening, next, nextTemperature, requiredShootMode, requiredPower, barrier);
    }
    
    private final static Panmage[] newBarrier(final String id, final Img[] imgsBarrier) {
        return (imgsBarrier == null) ? null : newSheet(id + ".barrier", imgsBarrier);
    }
    
    private final static Panframe[] newDoor(final String id, final String path) {
        return newDoor(id, Imtil.loadStrip(RES + path, 16), 0);
    }
    
    private final static Panframe[] newDoor(final String id, final Img[] imgs, final int off, final boolean small) {
        return small ? newDoorSmall(id, imgs, off) : newDoor(id, imgs, off);
    }
    
    private final static Panframe[] newDoor(final String id, final Img[] imgs, final int off) {
        final Panframe[] door = new Panframe[8];
        final Pangine engine = Pangine.getEngine();
        final String pre = id + ".";
        final Panmage top = engine.createImage(pre + "top", imgs[off]);
        final Panmage mid = engine.createImage(pre + "mid", imgs[off + 1]);
        door[0] = engine.createFrame(pre + ".0", top, 1, 0, false, true);
        door[1] = engine.createFrame(pre + ".1", mid, 1, 0, false, true);
        door[2] = engine.createFrame(pre + ".2", mid, 1, 0, false, false);
        door[3] = engine.createFrame(pre + ".3", top, 1, 0, false, false);
        door[4] = engine.createFrame(pre + ".4", top, 1, 0, true, true);
        door[5] = engine.createFrame(pre + ".5", mid, 1, 0, true, true);
        door[6] = engine.createFrame(pre + ".6", mid, 1, 0, true, false);
        door[7] = engine.createFrame(pre + ".7", top, 1, 0, true, false);
        return door;
    }
    
    private final static Panframe[] newDoorSmall(final String id, final String path) {
        return newDoorSmall(id, Imtil.loadStrip(RES + path, 16), 0);
    }
    
    private final static Panframe[] newDoorSmall(final String id, final Img[] imgs, final int off) {
        final Panframe[] door = new Panframe[2];
        final Pangine engine = Pangine.getEngine();
        final String pre = id + ".";
        final Panmage img = engine.createImage(pre + "img", imgs[off]);
        door[0] = engine.createFrame(pre + ".0", img, 1, 0, false, false);
        door[1] = engine.createFrame(pre + ".1", img, 1, 0, true, false);
        return door;
    }
    
    private final static Panframe[] toOverlay(final Panframe[] frms) {
        final int size = frms.length;
        final Panframe[] overlay = new Panframe[size];
        for (int i = 0; i < size; i++) {
            overlay[i] = toOverlay(frms[i]);
        }
        return overlay;
    }
    
    private final static Panframe toOverlay(final Panframe frm) {
        return Pangine.getEngine().createFrame(frm.getId() + ".overlay", frm.getImage(), frm.getDuration(), frm.getRot(), frm.isMirror(), frm.isFlip(),
            originOverlay,
            frm.getBoundingMinimum(), frm.getBoundingMaximum());
    }
    
    protected final static Pansound getMusic(final String name) {
        Pansound sound = music.get(name);
        if (sound == null) {
            sound = Pangine.getEngine().getAudio().createMusic(RES + "music/" + name + ".mid");
            music.put(name, sound);
        }
        return sound;
    }
    
    private final static void loadAudio() {
        final Panaudio audio = Pangine.getEngine().getAudio();
        audio.ensureCapacity(6);
        musicIntro = audio.createMusic(RES + "music/Intro.mid");
        musicLevelSelect = audio.createMusic(RES + "music/LevelSelect.mid");
        musicLevelStart = audio.createTransition(RES + "music/LevelStart.mid");
        musicFortressStart = audio.createTransition(RES + "music/FortressStart.mid");
        musicBoss = audio.createMusic(RES + "music/Boss.mid");
        musicEnding = audio.createMusic(RES + "music/Ending.mid");
        fxMenuHover = audio.createSound(RES + "sound/MenuHover.mid");
        fxMenuClick = audio.createSound(RES + "sound/MenuClick.mid");
        fxAttack = audio.createSound(RES + "sound/Attack.mid");
        fxImpact = audio.createSound(RES + "sound/Impact.mid").setReplayThreshold(6);
        fxRicochet = audio.createSound(RES + "sound/Ricochet.mid");
        fxJump = audio.createSound(RES + "sound/Jump.mid").setReplayThreshold(4);
        fxCharge = audio.createSound(RES + "sound/Charge.mid");
        fxChargedAttack = audio.createSound(RES + "sound/ChargedAttack.mid");
        fxSuperCharge = audio.createSound(RES + "sound/SuperCharge.mid");
        fxSuperChargedAttack = audio.createSound(RES + "sound/SuperChargedAttack.mid");
        fxHurt = audio.createSound(RES + "sound/Hurt.mid");
        fxDefeat = audio.createSound(RES + "sound/Defeat.mid");
        fxHealth = audio.createSound(RES + "sound/Health.mid");
        fxText = fxHealth;
        fxEnemyAttack = audio.createSound(RES + "sound/EnemyAttack.mid");
        fxCrumble = audio.createSound(RES + "sound/Crumble.mid");
        fxDoor = audio.createSound(RES + "sound/Door.mid");
        fxBossDoor = audio.createSound(RES + "sound/BossDoor.mid");
        fxThunder = audio.createSound(RES + "sound/Thunder.mid");
    }
    
    private final static class TitleScreen extends Panscreen {
        private Panmage title = null;
        
        @Override
        protected final void load() {
            final Pangine engine = Pangine.getEngine();
            engine.setBgColor(FinPancolor.BLACK);
            title = engine.createImage("title", RES + "misc/BotsnBoltsTitle.png");
            final Panctor actor = new Panctor();
            actor.setView(title);
            final Panple size = title.getSize();
            final int w = engine.getEffectiveWidth(), w2 = w / 2;
            actor.getPosition().set((w - size.getX()) / 2, (engine.getEffectiveHeight() - size.getY()) * 3 / 4);
            final Panroom room = Pangame.getGame().getCurrentRoom();
            room.addActor(actor);
            addText(room, Menu.isTouchEnabled() ? "Tap to start" : "Press anything", w2, 56);
            addText(room, COPYRIGHT, w2, 26);
            addText(room, AUTHOR, w2, 16);
            actor.register(new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    final Device device = event.getInput().getDevice();
                    final ControlScheme ctrl;
                    if (device instanceof Touchscreen) {
                        ctrl = ControlScheme.getDefaultKeyboard();
                        ctrl.setDevice(device);
                    } else {
                        ctrl = ControlScheme.getDefault(device);
                    }
                    pc.setControlScheme(ctrl);
                    fxMenuClick.startSound();
                    Menu.goLevelSelect();
                }});
        }
        
        @Override
        protected final void destroy() {
            title.destroy();
        }
    }
    
    public final static void addText(final Panlayer room, final String s, final int x, final int y) {
        final Pantext text = new Pantext(Pantil.vmid(), font, s);
        text.getPosition().set(x, y);
        text.centerX();
        room.addActor(text);
    }
    
    protected final static class BotsnBoltsScreen extends Panscreen {
        @Override
        protected final void load() {
            //final Panroom room = Pangame.getGame().getCurrentRoom();
            //initRoom(room);
            //fillRoom(room);
            loadRoom(RoomLoader.getStartRoom());
            getMusic(RoomLoader.level.musicName).changeMusic();
        }
        
        protected final static void loadRoom(final BotRoom botRoom) {
            loadRoom(botRoom, true);
        }
        
        protected final static void loadRoom(final BotRoom botRoom, final boolean playerNeeded) {
            RoomLoader.clear();
            final Panroom room = Player.loadRoom(botRoom);
            Pangame.getGame().setCurrentRoom(room);
            if (playerNeeded) {
                newPlayer(room);
            }
            RoomLoader.onChangeFinished();
        }
        
        protected final static Panroom newRoom(final int w) {
            final Panple size = Pangame.getGame().getCurrentRoom().getSize();
            room = Pangine.getEngine().createRoom(Pantil.vmid(), w, size.getY(), size.getZ());
            initRoom(room);
            return room;
        }
        
        private final static void initRoom(final Panroom room) {
            tm = new TileMap(Pantil.vmid(), room, tileSize, tileSize);
            tm.getPosition().setZ(DEPTH_BG);
            tm.setForegroundDepth(DEPTH_FG);
            room.addActor(tm);
        }
        
        protected final static void loadTileImage(final String imgName, final String bgFileId) {
            if (imgName.equals(timgName)) {
                tm.setImageMap(timg);
                if (Panlayer.isDestroyed(bgLayer)) {
                    loadBg(bgFileId);
                } else {
                    attachBgLayer(RoomLoader.nextRoom);
                }
                return;
            }
            timgPrev = timg;
            timgName = imgName;
            final Pangine engine = Pangine.getEngine();
            timg = engine.createImage("bg", RES + "bg/" + imgName + ".png");
            if ((imgMap == null) || (tileSize != prevTileSize)) {
                imgMap = tm.splitImageMap(timg);
                initConveyorBeltTiles();
            } else {
                tm.setImageMap(timg);
            }
            loadBg(bgFileId);
        }
        
        private final static void loadBg(final String bgFileId) {
            final Pangine engine = Pangine.getEngine();
            if (Chartil.isValued(bgFileId)) {
                final Panroom room = RoomLoader.nextRoom;
                if (Panlayer.isDestroyed(bgLayer)) {
                    bgLayer = engine.createLayer("layer.bg", GAME_W, GAME_H, room.getSize().getZ(), room);
                }
                attachBgLayer(room);
                Panctor.destroy(bgTm);
                Panctor.destroy(bgTexture);
                if (bgFileId.endsWith("Tex")) {
                    RoomLoader.loadTex(bgFileId);
                    bgTexture.setSize(GAME_W, GAME_H);
                    bgTexture.getPosition().setZ(DEPTH_PARALLAX_BG);
                    bgLayer.addActor(bgTexture);
                    bgLayer.setConstant(!(bgTexture instanceof AnimTexture));
                } else {
                    bgTm = newTileMap();
                    bgTm.setImageMap(timg);
                    bgTm.getPosition().setZ(DEPTH_PARALLAX_BG);
                    bgTm.setForegroundDepth(DEPTH_PARALLAX_FG);
                    bgLayer.addActor(bgTm);
                    bgLayer.setConstant(true);
                    RoomLoader.loadBg(bgFileId);
                }
                room.setClearDepthEnabled(false);
            }
        }
        
        protected final static TileMap newTileMap() {
            return new TileMap(Pantil.vmid(), GAME_COLUMNS, GAME_ROWS, DIM, DIM);
        }
        
        private final static void attachBgLayer(final Panroom room) {
            if ((bgLayer != null) && (bgLayer.getAbove() != room)) {
                room.addBeneath(bgLayer);
            }
        }
        
        private final static void newPlayer(final Panroom room) {
            Menu.addGameplayButtonInputs();
            final Player player = new Player(pc);
            player.getPosition().set(playerStartX, playerStartY, DEPTH_PLAYER);
            player.setMirror(playerStartMirror);
            initPlayerStart();
            room.addActor(player);
            Pangine.getEngine().track(player);
            new Warp(player);
            newHud(room, player);
            Menu.addGameplayButtonActors();
            player.registerInputs(pc.ctrl);
        }
        
        private final static void newHud(final Panroom room, final Player player) {
            hud = createHud(room);
            hud.setClearDepthEnabled(false);
            initHealthMeter(player.newHealthMeter(), true);
            Menu.addToggleButtons(new HudShootMode(player.pc), new HudJumpMode(player.pc));
        }
        
        @Override
        protected final void step() {
            RoomLoader.step();
        }
    }
    
    protected final static void initHealthMeter(final Panctor healthMeter, final boolean left) {
        final Pangine engine = Pangine.getEngine();
        int x = 24;
        if (!left) {
            x = engine.getEffectiveWidth() - x - 8;
        }
        healthMeter.getPosition().set(x, engine.getEffectiveHeight() - 73, DEPTH_HUD);
        hud.addActor(healthMeter);
    }
    
    protected final static void initPlayerStart() {
        playerStartX = defPlayerStartX;
        playerStartY = defPlayerStartY;
        playerStartMirror = defPlayerStartMirror;
    }
    
    protected final static Panlayer getLayer() {
        return room;
    }
    
    protected final static void addActor(final Panctor actor) {
        getLayer().addActor(actor);
    }
    
    public final static void main(final String[] args) {
        try {
            new BotsnBoltsGame().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
