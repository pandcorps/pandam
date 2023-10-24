/*
Copyright (c) 2009-2023, Andrew M. Martin
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
import java.util.concurrent.*;

import org.pandcorps.botsnbolts.HudMeter.*;
import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.botsnbolts.Profile.*;
import org.pandcorps.botsnbolts.RoomLoader.*;
import org.pandcorps.botsnbolts.ShootableDoor.*;
import org.pandcorps.core.*;
import org.pandcorps.core.chr.*;
import org.pandcorps.core.img.*;
import org.pandcorps.game.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panteraction.*;
import org.pandcorps.pandam.event.*;
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
    protected final static String VERSION = "1.0.8";
    protected final static String YEAR = "2016-2023";
    protected final static String AUTHOR = "Andrew M. Martin";
    protected final static String COPYRIGHT = "Copyright " + Pantext.CHAR_COPYRIGHT + " " + YEAR;
    
    /*
    Pause info
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
    protected final static byte TILE_RAIL = 20;
    protected final static byte TILE_UPSLOPE_RAIL = 21;
    protected final static byte TILE_DOWNSLOPE_RAIL = 22;
    
    protected final static int DEPTH_PARALLAX_BG = 0;
    protected final static int DEPTH_PARALLAX_FG = 2;
    protected final static int DEPTH_TEXTURE = 4;
    protected final static int DEPTH_BEHIND = 6;
    protected final static int DEPTH_BG = 8;
    protected final static int DEPTH_BETWEEN = 10;
    protected final static int DEPTH_FG = 12;
    protected final static int DEPTH_ABOVE = 14;
    protected final static int DEPTH_ENEMY_BG_3 = 16;
    protected final static int DEPTH_ENEMY_BG_2 = 18;
    protected final static int DEPTH_ENEMY_BG = 20;
    protected final static int DEPTH_CARRIER = 22;
    protected final static int DEPTH_POWER_UP = 24;
    protected final static int DEPTH_PLAYER_BACK_2 = 26;
    protected final static int DEPTH_PLAYER_BACK = 28;
    protected final static int DEPTH_PLAYER = 30;
    protected final static int DEPTH_PLAYER_FRONT = 32;
    protected final static int DEPTH_PLAYER_FRONT_2 = 34;
    protected final static int DEPTH_ENEMY_BACK_3 = 36;
    protected final static int DEPTH_ENEMY_BACK_2 = 38;
    protected final static int DEPTH_ENEMY_BACK = 40;
    protected final static int DEPTH_ENEMY = 42;
    protected final static int DEPTH_ENEMY_FRONT = 44;
    protected final static int DEPTH_ENEMY_FRONT_2 = 46;
    protected final static int DEPTH_ENEMY_FRONT_3 = 48;
    protected final static int DEPTH_ENEMY_FRONT_4 = 50;
    protected final static int DEPTH_ENEMY_FRONT_5 = 52;
    protected final static int DEPTH_PROJECTILE = 54;
    protected final static int DEPTH_OVERLAY = 56;
    protected final static int DEPTH_BURST = 114;
    protected final static int DEPTH_DIALOGUE_BOX = 116;
    protected final static int DEPTH_DIALOGUE_TEXT = 118;
    protected final static int DEPTH_HUD = 120;
    protected final static int DEPTH_HUD_TEXT = 122;
    protected final static int DEPTH_HUD_OVERLAY = 124;
    protected final static byte Z_OFF_OVERLAY = 2;
    protected final static byte Z_OFF_TEXT = 4;
    protected final static int DEPTH_CURSOR = 126;
    
    protected final static int MAX_CAMERA_SPEED = 10;
    
    protected final static FinPanple2 TUPLE_1_1 = new FinPanple2(1, 1);
    protected final static FinPanple2 TUPLE_2_2 = CENTER_4;
    protected final static FinPanple2 MIN_32 = new FinPanple2(-13, -13);
    protected final static FinPanple2 MAX_32 = new FinPanple2(13, 13);
    protected final static FinPanple2 MIN_16 = new FinPanple2(-6, -6);
    protected final static FinPanple2 MAX_16 = new FinPanple2(6, 6);
    protected final static FinPanple2 MIN_8 = new FinPanple2(-3, -3);
    protected final static FinPanple2 MAX_8 = new FinPanple2(3, 3);
    protected final static FinPanple2 MIN_4 = new FinPanple2(-1, -1);
    protected final static FinPanple2 MAX_4 = TUPLE_1_1;
    
    protected final static FinPanple2 ng = GuyPlatform.getMin(Player.PLAYER_X);
    protected final static FinPanple2 xg = GuyPlatform.getMax(Player.PLAYER_X, Player.PLAYER_H);
    protected final static FinPanple2 og = new FinPanple2(17, 1);
    protected final static FinPanple2 oj = new FinPanple2(17, 4);
    protected final static FinPanple2 oss = new FinPanple2(13, 1);
    protected final static FinPanple2 os = new FinPanple2(15, 1);
    protected final static FinPanple2 ojs = new FinPanple2(15, 4);
    protected final static FinPanple2 oSlide = oj;
    protected final static FinPanple originOverlay = new FinPanple(0, 0, DEPTH_OVERLAY);
    protected final static FinPanple2 minCube = new FinPanple2(-5, -5);
    protected final static FinPanple2 maxCube = new FinPanple2(5, 5);
    
    protected static Queue<Runnable> loaders = new LinkedList<Runnable>();
    protected static Font font = null;
    protected static PlayerImages voidImages = null;
    protected static PlayerImages nullImages = null;
    protected static PlayerImages volatileImages = null;
    protected static PlayerImages transientImages = null;
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
    protected static Panmage rail = null;
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
    private static Tile tileSpike = null;
    protected static Panmage spike = null;
    protected static Panmage spikeTile = null;
    protected static Panmage[] sentryGun = null;
    protected static Panmage[] wallCannon = null;
    protected static Panimation propEnemy = null;
    protected static Panmage[] springEnemy = null;
    protected static Panimation crawlEnemy = null;
    protected static Panimation shieldedEnemy = null;
    protected static Panimation unshieldedEnemy = null;
    protected static Panimation bounceEnemy = null;
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
    protected static Panmage iconBlank = null;
    protected static Panmage iconBolt = null;
    protected static Panmage iconDisk = null;
    protected static Panmage iconBoss = null;
    protected static Panmage black = null;
    protected static Panmage grey64 = null;
    protected static Panmage pupil = null;
    protected static Panimation defeatOrbBoss = null;
    protected static Panmage diskGrey = null;
    protected static HudMeterImages hudMeterBlank = null;
    protected static HudMeterImages hudMeterBoss = null;
    protected static Img[] hudMeterImgs = null;
    private static Panmage empty16 = null;
    private static Panmage swordHitBox = null;
    private static Panmage swordFullHitBox = null;
    private static Panmage whipHitBox = null;
    private final static Map<String, Pansound> music = new HashMap<String, Pansound>();
    protected static Pansound musicIntro = null;
    protected static Pansound musicLevelSelect = null;
    protected static Pansound musicLevelStart = null;
    protected static Pansound musicFortressStart = null;
    protected static Pansound musicVictory = null;
    protected static Pansound musicBoss = null;
    protected static Pansound musicEnding = null;
    protected static Pansound fxMenuHover = null;
    protected static Pansound fxMenuClick = null;
    protected static Pansound fxWarp = null;
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
    
    private static Profile prf0 = null;
    protected final static List<PlayerContext> pcs = new ArrayList<PlayerContext>(1);
    private static final float defPlayerStartX = 48;
    private static final float defPlayerStartY = 32;
    private static final boolean defPlayerStartMirror = false;
    protected static float playerStartX = defPlayerStartX;
    protected static float playerStartY = defPlayerStartY;
    protected static boolean playerStartMirror = defPlayerStartMirror;
    protected static Panctor tracked = null;
    
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
    private static Notifications notifications = null;

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
        doorCyan = newDoorDefinition("door.cyan", imgsClosed, imgsOpening, null, 0, null, null, imgsBarrier, null, null);
        final short s0 = 0, s48 = 48, s64 = 64, s96 = 96, s128 = 128, s144 = 144, s192 = 192, smax = Pancolor.MAX_VALUE;
        final Pancolor cyan = Pancolor.CYAN, silver = Pancolor.GREY, darkCyan = new FinPancolor(s0, s192, s192), darkSilver = Pancolor.DARK_GREY;
        //TODO Whip should open silver door even though power isn't maximum
        doorSilver = filterDoor("door.silver", imgsClosed, imgsOpening, cyan, silver, darkCyan, darkSilver, null, 0, null,
            Integer.valueOf(Projectile.POWER_MAXIMUM), imgsBarrier, Player.SHOOT_CHARGE, null);
        final Pancolor blue = newColorBlue(), darkBlue = newColorBlueDark();
        doorBlue = filterDoor("door.blue", imgsClosed, imgsOpening, silver, blue, darkSilver, darkBlue, null, 0, null,
            Integer.valueOf(Projectile.POWER_IMPOSSIBLE), imgsBarrier, Player.SHOOT_SPREAD, new CallSequence(new Callable<String>() {
                @Override public final String call() throws Exception {
                    return (RoomLoader.getShootableButton() == null) ? "Can only open from other side" : "Aim for the switch"; }}));
        final ShootableDoorDefinition doorRed, doorRedOrange, doorOrange, doorOrangeGold, doorFlash1, doorFlash2;
        final Pancolor red = Pancolor.RED, darkRed = new FinPancolor(s192, s0, s0);
        doorRed = filterDoor("door.red", imgsClosed, imgsOpening, blue, red, darkBlue, darkRed, null, 15, Player.SHOOT_RAPID, null, imgsBarrier, Player.SHOOT_RAPID, null);
        final Pancolor redOrange = new FinPancolor(smax, s64, s0), darkRedOrange = new FinPancolor(s192, s48, s0);
        doorRedOrange = filterDoor("door.red.orange", imgsClosed, null, red, redOrange, darkRed, darkRedOrange, doorRed, 10, Player.SHOOT_RAPID, null, imgsBarrier, Player.SHOOT_RAPID, null);
        final Pancolor orange = new FinPancolor(smax, s128, s0), darkOrange = new FinPancolor(s192, s96, s0);
        doorOrange = filterDoor("door.orange", imgsClosed, null, redOrange, orange, darkRedOrange, darkOrange, doorRedOrange, 6, null, null, imgsBarrier, Player.SHOOT_RAPID, null);
        final Pancolor orangeGold = new FinPancolor(smax, s192, s0), darkOrangeGold = new FinPancolor(s192, s144, s0);
        doorOrangeGold = filterDoor("door.orange.gold", imgsClosed, null, orange, orangeGold, darkOrange, darkOrangeGold, doorOrange, 3, null, null, imgsBarrier, Player.SHOOT_RAPID, null);
        final Pancolor gold = Pancolor.YELLOW, darkGold = new FinPancolor(s192, s192, s0);
        doorGold = filterDoor("door.gold", imgsClosed, null, orangeGold, gold, darkOrangeGold, darkGold, doorOrangeGold, 1, null, null, imgsBarrier, Player.SHOOT_RAPID, null);
        // No black barrier; it's not used; all barriers use grey 96 which is the black door's light color; do last so door/barrier images stay synchronized
        final Pancolor black = new FinPancolor(s96), darkBlack = new FinPancolor(s64);
        doorBlack = filterDoor("door.black", imgsClosed, imgsOpening, gold, black, darkGold, darkBlack, null, 0, null,
            Integer.valueOf(Projectile.POWER_IMPOSSIBLE), null, null, "Clear the enemies");
        final Img[] imgsSmallClosed = Imtil.loadStrip(RES + "bg/DoorSmall.png", 16, false);
        final Img[] imgsSmallOpening = Imtil.loadStrip(RES + "bg/DoorSmallOpening.png", 16, false);
        final Pancolor colSmall = new FinPancolor(smax, s64, smax), darkColSmall = new FinPancolor(s192, s48, s192);
        filterImgs(imgsBarrier, newFilter(gold, colSmall, darkGold, darkColSmall));
        doorSmall = newDoorDefinition("door.small", imgsSmallClosed, imgsSmallOpening, null, 0, Player.SHOOT_BOMB, null, imgsBarrier, Player.SHOOT_BOMB, null);
        Img.close(imgsSmallClosed);
        Img.close(imgsSmallOpening);
        final Pancolor hidden = newColorHidden(), darkHidden = newColorHiddenDark();
        filterImgs(imgsBarrier, newFilter(colSmall, hidden, darkColSmall, darkHidden));
        barrierHidden = newBarrier("hidden", imgsBarrier);
        // Door starts with 3 shades; 2 shades in first frame of flash, 1 shade in next frame, so irreversible; do last
        final Pancolor white = Pancolor.WHITE;
        doorFlash1 = filterDoor("door.flash1", imgsClosed, imgsOpening, newFilter(black, white, darkBlack, gold), newFilter(hidden, white, darkHidden, gold), doorOrangeGold, 1, null, null, imgsBarrier, Player.SHOOT_RAPID, null);
        doorFlash2 = filterDoor("door.flash2", imgsClosed, imgsOpening, gold, white, darkGold, gold, doorOrangeGold, 1, null, null, imgsBarrier, Player.SHOOT_RAPID, null);
        doorGold.setFlash(doorGold, doorFlash1, doorFlash2);
        doorFlash1.setFlash(doorGold, doorFlash1, doorFlash2);
        doorFlash2.setFlash(doorGold, doorFlash1, doorFlash2);
        Img.close(imgsClosed);
        Img.close(imgsOpening);
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
        grey64 = engine.createImage("grey64", RES + "misc/Grey64.png");
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
        iconBlank = engine.createImage("icon.blank", RES + "misc/IconBlank.png");
        iconBolt = engine.createImage("icon.bolt", RES + "misc/IconBolt.png");
        iconDisk = engine.createImage("icon.disk", RES + "misc/IconDisk.png");
        iconBoss = engine.createImage("icon.boss", RES + "misc/IconBoss.png");
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
    
    protected final static Panmage getRail() {
        if (rail == null) {
            rail = Pangine.getEngine().createImage("rail", RES + "bg/Rail.png");
        }
        return rail;
    }
    
    protected final static Panmage getBlockSpike() {
        if (blockSpike == null) {
            blockSpike = Pangine.getEngine().createImage("block.spike", RES + "bg/BlockSpike.png");
        }
        return blockSpike;
    }
    
    protected final static Tile getTileSpike() {
        if (tileSpike == null) {
            tileSpike = tm.getTile(null, Pangine.getEngine().createFrame("frame.spike", getBlockSpike(), 1, 0, false, false, new FinPanple(0, 0, DEPTH_CARRIER - DEPTH_FG), null, null), Tile.BEHAVIOR_SOLID);
        }
        return tileSpike;
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
    
    protected final static Panmage getEmpty16() {
        if (empty16 == null) {
            empty16 = Pangine.getEngine().createEmptyImage("spawner", FinPanple.ORIGIN, FinPanple.ORIGIN, BotsnBoltsGame.CENTER_32);
        }
        return empty16;
    }
    
    protected final static Panmage getSwordHitBox() {
        if (swordHitBox == null) {
            swordHitBox = Pangine.getEngine().createEmptyImage("swordHitBox", FinPanple.ORIGIN, FinPanple.ORIGIN, new FinPanple2(38, 36));
        }
        return swordHitBox;
    }
    
    protected final static Panmage getSwordFullHitBox() {
        if (swordFullHitBox == null) {
            swordFullHitBox = Pangine.getEngine().createEmptyImage("swordHitBox", FinPanple.ORIGIN, new FinPanple2(-30, 0), getSwordHitBox().getBoundingMaximum());
        }
        return swordFullHitBox;
    }
    
    protected final static Panmage getWhipHitBox() {
        if (whipHitBox == null) {
            // Placed at player.y - 7
            whipHitBox = Pangine.getEngine().createEmptyImage("whipHitBox", FinPanple.ORIGIN, new FinPanple2(0, 17), new FinPanple2(78, 25));
        }
        return whipHitBox;
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
        final Panframe bounceEnemyFrames[] = new Panframe[8];
        final Panmage bounceEnemyImg1 = engine.createImage("bounce.enemy.1", propO, propMin, propMax, RES + "enemy/BounceEnemy1.png");
        final Panmage bounceEnemyImg2 = engine.createImage("bounce.enemy.2", propO, propMin, propMax, RES + "enemy/BounceEnemy2.png");
        final Rotator rots = new Rotator(4) {
            @Override protected final Panmage getImage1() {
                return bounceEnemyImg1; }
            @Override protected final Panmage getImage2() {
                return bounceEnemyImg2; }};
        for (int i = 0; i < 8; i++) {
            rots.getFrame(bounceEnemyFrames, i);
        }
        bounceEnemy = engine.createAnimation("bounce.enemy", bounceEnemyFrames);
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
        final short s128 = 128, s192 = 192;
        return new FinPancolor(s128, s192, Pancolor.MAX_VALUE);
    }
    
    private final static Pancolor newColorIceOutline() {
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
            final Img[] imgsBarrier, final ShootMode hintShootMode, final CharSequence hintText) {
        final PixelFilter filter = newFilter(s1, d1, s2, d2);
        return filterDoor(id, imgsClosed, imgsOpening, filter, filter, next, nextTemperature, requiredShootMode, requiredPower, imgsBarrier, hintShootMode, hintText);
    }
    
    private final static ShootableDoorDefinition filterDoor(final String id, final Img[] imgsClosed, final Img[] imgsOpening,
                                                            final PixelFilter filterDoor, final PixelFilter filterBarrier,
                                                            final ShootableDoorDefinition next, final int nextTemperature, final ShootMode requiredShootMode, final Integer requiredPower,
                                                            final Img[] imgsBarrier, final ShootMode hintShootMode, final CharSequence hintText) {
        filterImgs(imgsClosed, filterDoor);
        filterImgs(imgsOpening, filterDoor);
        filterImgs(imgsBarrier, filterBarrier);
        return newDoorDefinition(id, imgsClosed, imgsOpening, next, nextTemperature, requiredShootMode, requiredPower, imgsBarrier, hintShootMode, hintText);
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
    
    private static Pancolor lastPlayerImagesPrimary1, lastPlayerImagesPrimary2;
    private static Pancolor lastPlayerImagesEnergy1, lastPlayerImagesEnergy2;
    private static Pancolor lastPlayerImagesSkin1, lastPlayerImagesSkin2;
    
    private final static void loadPlayer() {
        final String dir = "betabot", name = "Void";
        openPlayerImages(dir, name);
        final short s0 = 0, s48 = 48, s64 = 64, s72 = 72, s96 = 96, s120 = 120, s128 = 128, s144 = 144, s160 = 160, s176 = 176, s192 = 192, s240 = 240;
        final Pancolor skin0 = new FinPancolor(s240, s160, s120), skin1 = new FinPancolor(s192, s128, s96), skin2 = new FinPancolor(s144, s96, s72);
        lastPlayerImagesPrimary1 = lastPlayerImagesEnergy1 = Pancolor.GREEN; lastPlayerImagesPrimary2 = lastPlayerImagesEnergy2 = new FinPancolor(s0, s192, s0);
        lastPlayerImagesSkin1 = skin1; lastPlayerImagesSkin2 = skin2;
        voidImages = loadPlayerImages(dir, name, "Byte", "Baud", null, true, 0, -1, 1, 3, null);
        final Pancolor darkCyan = new FinPancolor(s0, s192, s192);
        filterPlayerImages(Pancolor.GREEN, Pancolor.CYAN, new FinPancolor(s0, s192, s0), darkCyan);
        lastPlayerImagesSkin1 = skin0; lastPlayerImagesSkin2 = skin1;
        playerMirror = false;
        volatileImages = loadPlayerImages("volatile", "Volatile", "Byte", "Baud", null, true, 0, -1, 1, 3, null);
        transientImages = loadPlayerImages("transient", "Transient", "Byte", "Baud", null, true, 1, 0, 4, 4, Player.MELEE_WHIP);
        lastPlayerImagesSkin1 = new FinPancolor(s96, s128, s192); lastPlayerImagesSkin2 = new FinPancolor(s72, s96, s144);
        finalImages = loadPlayerImages("final", "Final", "Byte", "Baud", volatileImages, false, 0, -1, 1, 3, null);
        filterPlayerImages(Pancolor.CYAN, new FinPancolor(s176, s144, Pancolor.MAX_VALUE), darkCyan, new FinPancolor(s128, s64, Pancolor.MAX_VALUE));
        lastPlayerImagesPrimary1 = lastPlayerImagesEnergy2; lastPlayerImagesPrimary2 = new FinPancolor(s96, s48, s192);
        lastPlayerImagesSkin1 = skin1; lastPlayerImagesSkin2 = skin2;
        nullImages = loadPlayerImages("alphabot", "Null", "Byte", "Baud", null, true, 1, 0, 1, 3, null);
        closePlayerImages();
        prf0 = new Profile();
    }
    
    private static Img[] playerDefeatOrb = null;
    private static Img playerProjectile = null;
    private static Img playerProjectile2 = null;
    private static Img[] playerProjectile3 = null;
    private static Img playerExhaust1 = null;
    private static Img playerExhaust2 = null;
    private static Img playerExhaustDiag1 = null;
    private static Img playerExhaustDiag2 = null;
    private static Img[] playerBurst = null;
    private static Img[] playerCharge = null;
    private static Img[] playerChargeVert = null;
    private static Img[] playerCharge2 = null;
    private static Img[] playerChargeVert2 = null;
    private static Img[] playerPlasma = null;
    private static Img playerShieldVert = null;
    private static Img playerShieldDiag = null;
    private static Img playerShieldCircle = null;
    private static Img playerSwordHoriz = null;
    private static Img playerSwordDiag = null;
    private static Img playerSwordBack = null;
    private static Img[] playerSwordTrails = null;
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
        playerExhaust1 = Imtil.load(pre + "Exhaust1.png", false);
        playerExhaust2 = Imtil.load(pre + "Exhaust2.png", false);
        playerExhaustDiag1 = Imtil.load(pre + "ExhaustDiag1.png", false);
        playerExhaustDiag2 = Imtil.load(pre + "ExhaustDiag2.png", false);
        playerBurst = Imtil.loadStrip(pre + "Burst.png", 16, false);
        playerCharge = Imtil.loadStrip(pre + "Charge.png", 8, false);
        playerChargeVert = Imtil.loadStrip(pre + "ChargeVert.png", 8, false);
        playerCharge2 = Imtil.loadStrip(pre + "Charge2.png", 8, false);
        playerChargeVert2 = Imtil.loadStrip(pre + "ChargeVert2.png", 8, false);
        playerPlasma = new Img[] {
                Imtil.load(pre + "Plasma1.png", false),
                Imtil.load(pre + "Plasma2.png", false),
                Imtil.load(pre + "Plasma3.png", false),
                Imtil.load(pre + "Plasma4.png", false)
        };
        playerShieldVert = Imtil.load(pre + "ShieldVert.png", false);
        playerShieldDiag = Imtil.load(pre + "ShieldDiag.png", false);
        playerShieldCircle = Imtil.load(pre + "ShieldCircle.png", false);
        playerSwordHoriz = Imtil.load(pre + "SwordHoriz.png", false);
        playerSwordDiag = Imtil.load(pre + "SwordDiag.png", false);
        playerSwordBack = Imtil.load(pre + "SwordBack.png", false);
        playerSwordTrails = new Img[] {
                Imtil.load(pre + "SwordTrail1.png", false),
                Imtil.load(pre + "SwordTrail2.png", false),
                Imtil.load(pre + "SwordTrail3.png", false)
        };
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
        lastPlayerImagesPrimary1 = lastPlayerImagesEnergy1 = d1;
        lastPlayerImagesPrimary2 = lastPlayerImagesEnergy2 = d2;
        final PixelFilter[] f = { newFilter(s1, d1, s2, d2) };
        filterImgs(playerDefeatOrb, f);
        Imtil.filterImg(playerProjectile, f);
        Imtil.filterImg(playerProjectile2, f);
        filterImgs(playerProjectile3, f);
        Imtil.filterImg(playerExhaust1, f);
        Imtil.filterImg(playerExhaust2, f);
        Imtil.filterImg(playerExhaustDiag1, f);
        Imtil.filterImg(playerExhaustDiag2, f);
        filterImgs(playerBurst, f);
        filterImgs(playerCharge, f);
        filterImgs(playerChargeVert, f);
        filterImgs(playerCharge2, f);
        filterImgs(playerChargeVert2, f);
        filterImgs(playerPlasma, f);
        Imtil.filterImg(playerShieldVert, f);
        Imtil.filterImg(playerShieldDiag, f);
        Imtil.filterImg(playerShieldCircle, f);
        Imtil.filterImg(playerSwordHoriz, f);
        Imtil.filterImg(playerSwordDiag, f);
        Imtil.filterImg(playerSwordBack, f);
        filterImgs(playerSwordTrails, f);
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
        playerExhaust1.close();
        playerExhaust1 = null;
        playerExhaust2.close();
        playerExhaust2 = null;
        playerExhaustDiag1.close();
        playerExhaustDiag1 = null;
        playerExhaustDiag2.close();
        playerExhaustDiag2 = null;
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
        Img.close(playerPlasma);
        playerPlasma = null;
        playerShieldVert.close();
        playerShieldVert = null;
        playerShieldDiag.close();
        playerShieldDiag = null;
        playerShieldCircle.close();
        playerShieldCircle = null;
        playerSwordHoriz.close();
        playerSwordHoriz = null;
        playerSwordDiag.close();
        playerSwordDiag = null;
        playerSwordBack.close();
        playerSwordBack = null;
        Img.close(playerSwordTrails);
        playerSwordTrails = null;
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
    
    private final static PlayerImages loadPlayerImages(final String dir, final String name, final String animalName, final String birdName, final PlayerImages src, final boolean pupilNeeded,
            final int shieldRunOffsetX, final int wieldStillOffsetX, final int wieldIndexMin, final int wieldIndexMax, final MeleeMode meleeMode) {
        final String pre = getCharacterPrefix(dir, name);
        final int slideX = Player.PLAYER_X + 2;
        final Panple nSlide = GuyPlatform.getMin(slideX), xSlide = GuyPlatform.getMax(slideX, 19);
        final Panple oClimb = new FinPanple2(15, 4);
        final Img[] climbImgs = Imtil.loadStrip(pre + "Climb.png", 32);
        final Img[] climbImgsMirror = loadPlayerMirrorStrip(pre + "ClimbMirror.png");
        final Panmage climb = newPlayerImage(pre + "Climb", oClimb, climbImgs, climbImgsMirror, 0);
        final Panmage climbAim = newPlayerImage(pre + "Climb.Shoot", oClimb, climbImgs, climbImgsMirror, 1);
        final Panmage climbTop = newPlayerImage(pre + "Climb.Top", oClimb, climbImgs, climbImgsMirror, 2);
        final Img imgClimbThrow = Imtil.load(pre + "ClimbThrow.png"), imgClimbThrowMirror = playerMirror ? Imtil.load(pre + "ClimbThrowMirror.png") : null;
        final Panmage climbThrow = newPlayerImage(pre + "Climb.Throw", oClimb, imgClimbThrow, imgClimbThrowMirror);
        final Img imgWallGrab = Imtil.load(pre + "Wall.png"), imgWallGrabMirror = playerMirror ? Imtil.load(pre + "WallMirror.png") : null;
        final Panmage wallGrab = newPlayerImage(PRE_IMG + "." + name + ".wall", oj, imgWallGrab, imgWallGrabMirror);
        final Img imgWallGrabAim = Imtil.load(pre + "WallAim.png"), imgWallGrabAimMirror = playerMirror ? Imtil.load(pre + "WallAimMirror.png") : null;
        final Panmage wallGrabAim = newPlayerImage(PRE_IMG + "." + name + ".wall.aim", oj, imgWallGrabAim, imgWallGrabAimMirror);
        final Img imgWallGrabThrow = Imtil.load(pre + "WallThrow.png"), imgWallGrabThrowMirror = playerMirror ? Imtil.load(pre + "WallThrowMirror.png") : null;
        final Panmage wallGrabThrow = newPlayerImage(PRE_IMG + "." + name + ".wall.throw", oj, imgWallGrabThrow, imgWallGrabThrowMirror);
        final Img imgDash = Imtil.load(pre + "Dash.png"), imgDashMirror = playerMirror ? Imtil.load(pre + "DashMirror.png") : null;
        final Panmage dash = newPlayerImage(PRE_IMG + "." + name + ".dash", og, nSlide, xSlide, imgDash, imgDashMirror);
        final Img imgDashAim = Imtil.load(pre + "DashAim.png"), imgDashAimMirror = playerMirror ? Imtil.load(pre + "DashAimMirror.png") : null;
        final Panmage dashAim = newPlayerImage(PRE_IMG + "." + name + ".dash.Aim", os, nSlide, xSlide, imgDashAim, imgDashAimMirror);
        final Img imgDashThrow = Imtil.load(pre + "DashThrow.png"), imgDashThrowMirror = playerMirror ? Imtil.load(pre + "DashThrowMirror.png") : null;
        final Panmage dashThrow = newPlayerImage(PRE_IMG + "." + name + ".dash.Throw", os, nSlide, xSlide, imgDashThrow, imgDashThrowMirror);
        final Img imgDescend = Imtil.load(pre + "Descend.png"), imgDescendMirror = playerMirror ? Imtil.load(pre + "DescendMirror.png") : null;
        final Panmage descend = newPlayerImage(PRE_IMG + "." + name + ".descend", oj, ng, xg, imgDescend, imgDescendMirror);
        final Img imgDescendAim = Imtil.load(pre + "DescendAim.png"), imgDescendAimMirror = playerMirror ? Imtil.load(pre + "DescendAimMirror.png") : null;
        final Panmage descendAim = newPlayerImage(PRE_IMG + "." + name + ".descend.Aim", ojs, ng, xg, imgDescendAim, imgDescendAimMirror);
        final Img imgDescendThrow = Imtil.load(pre + "DescendThrow.png"), imgDescendThrowMirror = playerMirror ? Imtil.load(pre + "DescendThrowMirror.png") : null;
        final Panmage descendThrow = newPlayerImage(PRE_IMG + "." + name + ".descend.Throw", ojs, ng, xg, imgDescendThrow, imgDescendThrowMirror);
        final PlayerImagesSubSet basicSet = loadPlayerImagesSubSet(pre, name, true, og, og, oj, climb, wallGrab, dash, descend);
        final PlayerImagesSubSet shootSet = loadPlayerImagesSubSet(pre + "Shoot", name + ".shoot", false, oss, os, ojs, climbAim, wallGrabAim, dashAim, descendAim);
        final PlayerImagesSubSet throwSet = loadPlayerImagesSubSet(pre + "Throw", name + ".throw", false, oss, os, ojs, climbThrow, wallGrabThrow, dashThrow, descendThrow);
        final PlayerImagesSubSet[] wieldSets = new PlayerImagesSubSet[4];
        for (int i = wieldIndexMin; i <= wieldIndexMax; i++) {
            final Panmage climbWield = newPlayerImage(PRE_IMG + "." + name + ".climb.wield" + i, oClimb, ng, xg,
                    Imtil.load(pre + "ClimbWield" + i + ".png"), playerMirror ? Imtil.load(pre + "ClimbWield" + i + "Mirror.png") : null);
            final Panmage wallGrabWield = newPlayerImage(PRE_IMG + "." + name + ".wall.wield" + i, oj, ng, xg,
                    Imtil.load(pre + "WallWield" + i + ".png"), playerMirror ? Imtil.load(pre + "WallWield" + i + "Mirror.png") : null);
            final Panmage dashWield = newPlayerImage(PRE_IMG + "." + name + ".dash.wield" + i, os, nSlide, xSlide,
                    Imtil.load(pre + "DashWield" + i + ".png"), playerMirror ? Imtil.load(pre + "DashWield" + i + "Mirror.png") : null);
            final Panmage descendWield = newPlayerImage(PRE_IMG + "." + name + ".descend.wield" + i, oj, ng, xg,
                    Imtil.load(pre + "DescendWield" + i + ".png"), playerMirror ? Imtil.load(pre + "DescendWield" + i + "Mirror.png") : null);
            wieldSets[i - 1] = loadPlayerImagesSubSet(pre + "Wield" + i, name + ".wield" + i, false, og, og, oj, climbWield, wallGrabWield, dashWield, descendWield);
        }
        final Pangine engine = Pangine.getEngine();
        final Img imgHurt = Imtil.load(pre + "Hurt.png", false), imgHurtMirror = playerMirror ? Imtil.load(pre + "HurtMirror.png", false) : null;
        final Panmage hurt = newPlayerImage(PRE_IMG + "." + name + ".hurt", oj, imgHurt, imgHurtMirror);
        final short s64 = 64, s96 = 96;
        final Pancolor grey = Pancolor.DARK_GREY, darkGrey = new FinPancolor(s96), darkerGrey = new FinPancolor(s64);
        final Pancolor pri = lastPlayerImagesPrimary1, darkPri = lastPlayerImagesPrimary2;
        final Pancolor skin = lastPlayerImagesSkin1, darkSkin = lastPlayerImagesSkin2;
        final Pancolor frz = Pancolor.WHITE, darkFrz = newColorIce(), darkerFrz = newColorIceDark();
        final Pancolor out = Pancolor.BLACK, outFrz = newColorIceOutline();
        final ReplacePixelFilter frzFilter = new ReplacePixelFilter(grey, frz, darkGrey, darkFrz, darkerGrey, darkerFrz, pri, frz, darkPri, darkFrz, skin, frz, darkSkin, darkFrz, out, outFrz);
        if (playerMirror) {
            filterImgs(new Img[] { imgHurt, imgHurtMirror }, frzFilter);
        } else {
            Imtil.filterImg(imgHurt, frzFilter);
        }
        final Panmage frozen = newPlayerImage(PRE_IMG + "." + name + ".frozen", oj, imgHurt, imgHurtMirror);
        Img.close(imgHurt, imgHurtMirror);
        
        final Panimation defeat = newAnimation(pre + "DefeatOrb", playerDefeatOrb, CENTER_16, 6);
        
        final Img[] jumpAimImgs = Imtil.loadStrip(pre + "JumpAim.png", 32);
        final Img[] jumpAimImgsMirror = loadPlayerMirrorStrip(pre + "JumpAimMirror.png");
        final Panmage jumpAimDiag = newPlayerImage(pre + "Jump.Aim.Diag", ojs, jumpAimImgs, jumpAimImgsMirror, 0);
        final Panmage jumpAimUp = newPlayerImage(pre + "Jump.Aim.Diag", ojs, jumpAimImgs, jumpAimImgsMirror, 1);
        final Img imgGlideUp = Imtil.load(pre + "GlideUp.png"), imgGlideUpMirror = playerMirror ? Imtil.load(pre + "GlideUpMirror.png") : null;
        final Panmage glideUp = newPlayerImage(PRE_IMG + "." + name + ".glide.up", oj, imgGlideUp, imgGlideUpMirror);
        final Img imgGlideHoriz = Imtil.load(pre + "GlideHoriz.png"), imgGlideHorizMirror = playerMirror ? Imtil.load(pre + "GlideHorizMirror.png") : null;
        final Panmage glideHoriz = newPlayerImage(PRE_IMG + "." + name + ".glide.horiz", oj, new FinPanple2(ng.getX(), ng.getY() + 8), new FinPanple2(xg.getX(), xg.getY() - 8), imgGlideHoriz, imgGlideHorizMirror);
        final Img imgGlideDown = Imtil.load(pre + "GlideDown.png"), imgGlideDownMirror = playerMirror ? Imtil.load(pre + "GlideDownMirror.png") : null;
        final Panmage glideDown = newPlayerImage(PRE_IMG + "." + name + ".glide.down", oj, new FinPanple2(ng.getX(), ng.getY() + 4), new FinPanple2(xg.getX(), xg.getY() - 4), imgGlideDown, imgGlideDownMirror);
        final Img imgTalk = Imtil.load(pre + "Talk.png"), imgTalkMirror = playerMirror ? Imtil.load(pre + "TalkMirror.png") : null;
        final Panmage talk = newPlayerImage(PRE_IMG + "." + name + ".talk", og, imgTalk, imgTalkMirror);
        final Img imgSlide = Imtil.load(pre + "Slide.png"), imgSlideMirror = playerMirror ? Imtil.load(pre + "SlideMirror.png") : null;
        final Panmage slide = newPlayerImage(PRE_IMG + "." + name + ".slide", oSlide, nSlide, xSlide, imgSlide, imgSlideMirror);
        
        final Panmage basicProjectile = (src == null) ? engine.createImage(pre + "Projectile", new FinPanple2(3, 3), new FinPanple2(-3, -2), new FinPanple2(5, 3), playerProjectile) : src.basicProjectile;
        final Panimation projectile2 = (src == null) ? newFlipper(pre + "Projectile2", playerProjectile2, new FinPanple2(7, 7), new FinPanple2(-4, -5), new FinPanple2(8, 6), 4) : src.projectile2;
        final Panimation projectile3 = (src == null) ? createAnm(pre + "Projectile3", 2, new FinPanple2(23, 7), new FinPanple2(-6, -7), new FinPanple2(8, 8), playerProjectile3) : src.projectile3;
        final Panmage exhaust1 = (src == null) ? engine.createImage(pre + "Exhaust1", null, null, null, playerExhaust1) : src.exhaust1;
        final Panmage exhaust2 = (src == null) ? engine.createImage(pre + "Exhaust2", null, null, null, playerExhaust2) : src.exhaust2;
        final Panmage exhaustDiag1 = (src == null) ? engine.createImage(pre + "ExhaustDiag1", null, null, null, playerExhaustDiag1) : src.exhaustDiag1;
        final Panmage exhaustDiag2 = (src == null) ? engine.createImage(pre + "ExhaustDiag2", null, null, null, playerExhaustDiag2) : src.exhaustDiag2;
        final Panimation burst = (src == null) ? newAnimation(pre + "Burst", playerBurst, CENTER_16, new FinPanple2(-10, -10), new FinPanple2(10, 10), 2) : src.burst;
        final Panimation charge = (src == null) ? newAnimation(pre + "Charge", playerCharge, null, 1) : src.charge;
        final Panple oChargeVert = new FinPanple2(4, 0);
        final Panimation chargeVert = (src == null) ? newAnimation(pre + "ChargeVert", playerChargeVert, oChargeVert, 1) : src.chargeVert;
        final Panimation charge2 = (src == null) ? newAnimation(pre + "Charge2", playerCharge2, null, 1) : src.charge2;
        final Panimation chargeVert2 = (src == null) ? newAnimation(pre + "ChargeVert2", playerChargeVert2, oChargeVert, 1) : src.chargeVert2;
        final Panmage[] plasma = (src == null)
                ? new Panmage[] {
                        engine.createImage(pre + "Plasma1", null, TUPLE_1_1, new FinPanple2(6, 6), playerPlasma[0]),
                        engine.createImage(pre + "Plasma2", TUPLE_2_2, TUPLE_1_1, new FinPanple2(10, 10), playerPlasma[1]),
                        engine.createImage(pre + "Plasma3", null, TUPLE_1_1, new FinPanple2(14, 14), playerPlasma[2]),
                        engine.createImage(pre + "Plasma4", null, TUPLE_1_1, new FinPanple2(14, 22), playerPlasma[3])
                }
                : src.plasma;
        final Panmage shieldVert = (src == null) ? engine.createImage(pre + "ShieldVert", playerShieldVert) : src.shieldVert;
        final Panmage shieldDiag = (src == null) ? engine.createImage(pre + "ShieldDiag", playerShieldDiag) : src.shieldDiag;
        final Panmage shieldCircle = (src == null) ? engine.createImage(pre + "ShieldCircle", playerShieldCircle) : src.shieldCircle;
        final String meleeWeaponName = (meleeMode == null) ? null : meleeMode.getName();
        final Panmage swordHoriz, swordDiag, swordBack;
        final Map<String, Panmage> boltBoxes = new HashMap<String, Panmage>(Profile.UPGRADES.length + 1);
        int wox = 0, woy = 0, whox = 0, whoy = 0, wbox = 0, wboy = 0;
        if (meleeWeaponName == null) {
            swordHoriz = (src == null) ? engine.createImage(pre + "SwordHoriz", playerSwordHoriz) : src.swordHoriz;
            swordDiag = (src == null) ? engine.createImage(pre + "SwordDiag", playerSwordDiag) : src.swordDiag;
            swordBack = (src == null) ? engine.createImage(pre + "SwordBack", playerSwordBack) : src.swordBack;
        } else {
            final String meleeHorizImageLocation = pre + meleeWeaponName + "Horiz.png";
            swordHoriz = engine.createImage(meleeHorizImageLocation, meleeHorizImageLocation);
            final String meleeDiagImageLocation = pre + meleeWeaponName + "Diag.png";
            swordDiag = engine.createImage(meleeDiagImageLocation, meleeDiagImageLocation);
            final String meleeBackImageLocation = pre + meleeWeaponName + "Back.png";
            swordBack = engine.createImage(meleeBackImageLocation, meleeBackImageLocation);
            final String meleeBoxImageLocation = pre + meleeWeaponName + "Box.png";
            boltBoxes.put(meleeWeaponName, engine.createImage(meleeBoxImageLocation, meleeBoxImageLocation));
            wox = meleeMode.getAttackOffsetX();
            woy = meleeMode.getAttackOffsetY();
            whox = meleeMode.getHorizontalOffsetX();
            whoy = meleeMode.getHorizontalOffsetY();
            wbox = meleeMode.getBackOffsetX();
            wboy = meleeMode.getBackOffsetY();
        }
        final Panmage[] swordTrails = (src == null)
                ? new Panmage[] {
                        engine.createImage(pre + "SwordTrail1", playerSwordTrails[0]),
                        engine.createImage(pre + "SwordTrail2", playerSwordTrails[1]),
                        engine.createImage(pre + "SwordTrail3", playerSwordTrails[2]),
                }
                : src.swordTrails;
        
        final PlayerImageExtra stillExtra = new PlayerImageExtra(0, 0,
                new HeldExtra(shieldVert, 2, 1, DEPTH_PLAYER_FRONT, false, false, 0, null),
                new HeldExtra(swordHoriz, -13 + whox + wieldStillOffsetX, 4 + whoy, DEPTH_PLAYER_FRONT_2, false, false, 0, null));
        basicSet.stand.setExtra(stillExtra);
        basicSet.blink.setExtra(stillExtra);
        talk.setExtra(stillExtra);
        basicSet.start.setExtra(stillExtra);
        final HeldExtra jumpExtraSword = new HeldExtra(swordDiag, -9, 21, DEPTH_PLAYER_FRONT, true, false, 0, null);
        basicSet.jump.setExtra(new PlayerImageExtra(0, 0,
                new HeldExtra(shieldVert, 6, 8, DEPTH_PLAYER_FRONT, false, false, 0, shootSet.jump),
                jumpExtraSword));
        basicSet.run[0].setExtra(new PlayerImageExtra(0, 0,
                new HeldExtra(shieldDiag, 3 - shieldRunOffsetX, 18, DEPTH_PLAYER_BACK, false, true, 0, null),
                new HeldExtra(swordDiag, -8, 15, DEPTH_PLAYER_FRONT, true, true, 0, null)));
        basicSet.run[1].setExtra(new PlayerImageExtra(0, 0,
                new HeldExtra(shieldVert, 11, 18, DEPTH_PLAYER_BACK, true, true, 0, null),
                new HeldExtra(swordDiag, -7, 12, DEPTH_PLAYER_FRONT, false, true, 0, null)));
        basicSet.run[2].setExtra(new PlayerImageExtra(0, 0,
                new HeldExtra(shieldDiag, -5 + shieldRunOffsetX, 18, DEPTH_PLAYER_BACK, true, true, 0, null),
                new HeldExtra(swordDiag, 0, 5, DEPTH_PLAYER_FRONT, false, false, 0, null)));
        basicSet.dash.setExtra(new PlayerImageExtra(0, 0,
                new HeldExtra(shieldVert, 14, -4, DEPTH_PLAYER_FRONT, false, false, 1, null),
                new HeldExtra(swordDiag, -5, 16, DEPTH_PLAYER_FRONT, true, true, 0, null)));
        basicSet.descend.setExtra(new PlayerImageExtra(0, 0,
                new HeldExtra(shieldVert, 6, 8, DEPTH_PLAYER_FRONT, false, false, 0, shootSet.descend),
                jumpExtraSword));
        basicSet.wallGrab.setExtra(new PlayerImageExtra(0, 0,
                new HeldExtra(shieldVert, 6, 3, DEPTH_PLAYER_FRONT, false, false, 0, shootSet.wallGrab),
                new HeldExtra(swordDiag, 6, 20, DEPTH_PLAYER_BACK, true, true, 0, null)));
        basicSet.climb.setExtra(new PlayerImageExtra(1, 0,
                new HeldExtra(shieldDiag, -10, 2, DEPTH_PLAYER_FRONT, false, false, 0, null),
                new HeldExtra(swordBack, -6 + wbox, -8 + wboy, DEPTH_PLAYER_FRONT, false, false, 0, null)));
        climbTop.setExtra(new PlayerImageExtra(1, 0,
                new HeldExtra(shieldDiag, -8, 5, DEPTH_PLAYER_FRONT, true, false, 1, null),
                new HeldExtra(swordBack, -6 + wbox, -6 + wboy, DEPTH_PLAYER_FRONT, false, false, 0, null)));
        shootSet.climb.setExtra(new PlayerImageExtra(1, 0, null, null));
        throwSet.climb.setExtra(new PlayerImageExtra(1, 0, null, null));
        shootSet.stand.setExtra(new PlayerImageExtra(0, 0, new HeldExtra(shieldVert, -12, 19, DEPTH_PLAYER_BACK, false, true, 0, null), null));
        slide.setExtra(new PlayerImageExtra(0, 0,
                new HeldExtra(shieldDiag, -3, 19, DEPTH_PLAYER_BACK, false, true, 0, null),
                new HeldExtra(swordBack, 9, 13, DEPTH_PLAYER_BACK_2, false, true, 1, null)));
        basicSet.crouch[0].setExtra(new PlayerImageExtra(0, 0, new HeldExtra(shieldDiag, 12, 16, DEPTH_PLAYER_BACK, true, true, 0, null), null));
        final String meleeDiagonalImageSuffix = (meleeMode == null) ? null : meleeMode.getAttackImageSuffix();
        Panmage meleeAttack = swordDiag;
        if (meleeDiagonalImageSuffix != null) {
            final String meleeDiagonalImageLocation = pre + meleeDiagonalImageSuffix;
            meleeAttack = engine.createImage(meleeDiagonalImageLocation, meleeDiagonalImageLocation);
        }
        for (int wieldIndex = wieldIndexMin; wieldIndex <= wieldIndexMax; wieldIndex++) {
            final int wi = wieldIndex - 1;
            int wx, wy, trail = wi + 1;
            final boolean wm, wf;
            if (wi == 0) {
                wx = 7; wy = 14; wm = false; wf = false;
            } else if (wi == 1) {
                wx = 6; wy = 8; wm = false; wf = true;
            } else if (wi == 2) {
                wx = -10; wy = 12; wm = true; wf = false;
            } else if (wi == 3) {
                wx = 8; wy = 9; wm = false; wf = false;
            } else {
                throw new IllegalStateException("Unexpected wield array index " + wi);
            }
            wx += wox;
            wy += woy;
            final PlayerImagesSubSet wieldSet = wieldSets[wi];
            final HeldExtra wieldShieldExtra, wieldShieldRunExtra;
            if (wi == 2) {
                wieldShieldExtra = new HeldExtra(shieldVert, 6, 5, DEPTH_PLAYER_FRONT, false, false, 0, null);
                wieldShieldRunExtra = new HeldExtra(shieldVert, 5, 5, DEPTH_PLAYER_FRONT, false, false, 0, null);
            } else {
                wieldShieldExtra = new HeldExtra(shieldDiag, -1, 17, DEPTH_PLAYER_BACK, false, true, 0, null);
                wieldShieldRunExtra = new HeldExtra(shieldDiag, -2, 17, DEPTH_PLAYER_BACK, false, true, 0, null);
            }
            wieldSet.stand.setExtra(new PlayerImageExtra(0, trail,
                    wieldShieldExtra,
                    new HeldExtra(meleeAttack, wx, wy, DEPTH_PLAYER_FRONT, wm, wf, 0, null)));
            final PlayerImageExtra wieldRunExtra = new PlayerImageExtra(0, trail,
                    wieldShieldRunExtra,
                    new HeldExtra(meleeAttack, wx - 1, wy, DEPTH_PLAYER_FRONT, wm, wf, 0, null));
            wieldSet.run[0].setExtra(wieldRunExtra);
            wieldSet.run[1].setExtra(wieldRunExtra);
            wieldSet.run[2].setExtra(wieldRunExtra);
            final PlayerImageExtra wieldJumpExtra = new PlayerImageExtra(0, trail, null,
                    new HeldExtra(meleeAttack, wx, wy + 3, DEPTH_PLAYER_FRONT, wm, wf, 0, null));
            wieldSet.jump.setExtra(wieldJumpExtra);
            wieldSet.climb.setExtra(new PlayerImageExtra(1, trail, null,
                    new HeldExtra(meleeAttack, wx + 3, wy + 1, DEPTH_PLAYER_FRONT, wm, wf, 0, null)));
            wieldSet.wallGrab.setExtra(new PlayerImageExtra(0, trail, null,
                    new HeldExtra(meleeAttack, wx, wy, DEPTH_PLAYER_FRONT, wm, wf, 0, null)));
            wieldSet.dash.setExtra(new PlayerImageExtra(0, trail, null,
                    new HeldExtra(meleeAttack, wx + 2, wy - 2, DEPTH_PLAYER_FRONT, wm, wf, 0, null)));
            wieldSet.descend.setExtra(wieldJumpExtra);
        }
        
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
        pi = new PlayerImages(basicSet, shootSet, throwSet, wieldSets, hurt, frozen, defeat, climbTop, jumpAimDiag, jumpAimUp, glideUp, glideHoriz, glideDown, talk, basicProjectile, projectile2, projectile3, charge, chargeVert, charge2, chargeVert2, plasma, shieldVert, shieldDiag, shieldCircle,
            swordHoriz, swordDiag, swordBack, swordTrails, exhaust1, exhaust2, exhaustDiag1, exhaustDiag2, burst, ball, slide, warp, materialize, bomb, link, batterySml, batteryMed, batteryBig, doorBolt, bolt, disk, powerBox, boltBoxes, diskBox, highlightBox, portrait, hudMeterImages,
            name, animalName, birdName, meleeMode);
        playerImages.put(portraitLoc, pi);
        return pi;
    }
    
    private final static PlayerImagesSubSet loadPlayerImagesSubSet(final String path, final String name, final boolean startNeeded, final Panple os, final Panple o, final Panple oj,
            final Panmage climb, final Panmage wallGrab, final Panmage dash, final Panmage descend) {
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
        return new PlayerImagesSubSet(still, jump, new Panmage[] { run1, run2, run3 }, start, blink, crouch, climb, wallGrab, dash, descend);
    }
    
    private final static Img[] loadPlayerMirrorStrip(final String loc) {
        return playerMirror ? Imtil.loadStrip(loc, 32) : null;
    }
    
    private final static Panframe newSubFrame(final String pre, final int i, final Panple o, final Panple min, final Panple max, final Panmage src, final float x, final float y, final Panple size, final int dur) {
        return Pangine.getEngine().createFrame(pre + ".frm." + i, new SubPanmage(pre + ".sub." + i, o, min, max, src, x, y, size), dur);
    }
    
    private final static void postProcess() {
        final short s96 = 96;
        final PixelFilter[] greyFilter = { newFilter(lastPlayerImagesEnergy1, Pancolor.DARK_GREY, lastPlayerImagesEnergy2, new FinPancolor(s96)) };
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
        return newPlayerImage(id, o, ng, xg, img, imgMirror);
    }
    
    private final static Panmage newPlayerImage(final String id, final Panple o, final Panple n, final Panple x, final Img img, final Img imgMirror) {
        final Pangine engine = Pangine.getEngine();
        final Panmage image = engine.createImage(id, o, n, x, img);
        if (playerMirror) {
            image.setMirrorSource(engine.createImage(id + ".mirror", o, n, x, imgMirror));
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
            final Img[] imgsBarrier, final ShootMode hintShootMode, final CharSequence hintText) {
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
        return new ShootableDoorDefinition(door, opening, next, nextTemperature, requiredShootMode, requiredPower, barrier, hintShootMode, hintText);
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
        musicVictory = audio.createTransition(RES + "music/Victory.mid");
        musicBoss = audio.createMusic(RES + "music/Boss.mid");
        musicEnding = audio.createMusic(RES + "music/Ending.mid");
        fxMenuHover = audio.createSound(RES + "sound/MenuHover.mid").setReplayThreshold(4);
        fxMenuClick = audio.createSound(RES + "sound/MenuClick.mid");
        fxWarp = audio.createSound(RES + "sound/Warp.mid");
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
        fxCrumble = audio.createSound(RES + "sound/Crumble.mid").setReplayThreshold(4);
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
            room = Pangame.getGame().getCurrentRoom();
            room.addActor(actor);
            addText(room, Menu.isTouchEnabled() ? "Tap to start" : "Press anything", w2, 61);
            addText(room, COPYRIGHT, w2, 34);
            addText(room, AUTHOR, w2, 24);
            Menu.addVersion();
            actor.register(new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    //addPlayerContext(prf0, voidImages, event);
                    addPlayerContext(prf0, nullImages, event);
                    startGame();
                }});
        }
        
        @Override
        protected final void destroy() {
            title.destroy();
        }
    }
    
    protected final static PlayerContext addPlayerContext(final Profile prf, final PlayerImages pi, final ActionEndEvent event) {
        final Device device = event.getInput().getDevice();
        final ControlScheme ctrl;
        if (device instanceof Touchscreen) {
            ctrl = ControlScheme.getDefaultKeyboard();
            ctrl.setDevice(device);
        } else {
            ctrl = ControlScheme.getDefault(device);
        }
        final PlayerContext pc = new PlayerContext(pcs.size(), prf, pi);
        pcs.add(pc);
        pc.setControlScheme(ctrl);
        fxMenuClick.startSound();
        return pc;
    }
    
    protected final static void startGame() {
        if (RoomLoader.isFirstLevelFinished()) {
            Menu.goLevelSelect();
        } else {
            Menu.goOptions();
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
            engine.setEntityMapEnabled(false);
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
            newHud(room);
            for (final PlayerContext pc : pcs) {
                newPlayer(room, pc);
            }
            tracked = (pcs.size() == 1) ? PlayerContext.getPlayer(pcs.get(0)) : newPlayerMean();
            Pangine.getEngine().track(tracked);
            initPlayerStart();
        }
        
        private final static void newPlayer(final Panroom room, final PlayerContext pc) {
            Menu.addGameplayButtonInputs();
            final Player player = new Player(pc);
            player.getPosition().set(playerStartX, playerStartY, DEPTH_PLAYER);
            player.setMirror(playerStartMirror);
            room.addActor(player);
            new Warp(player);
            newHud(player);
            Menu.addGameplayButtonActors();
            player.registerInputs(pc.ctrl);
        }
        
        private final static void newHud(final Panroom room) {
            hud = createHud(room);
            hud.setClearDepthEnabled(false);
        }
        
        private final static void newHud(final Player player) {
            final int playerX = player.pc.getHudX();
            initHudMeter(player.newHealthMeter(), playerX + HEALTH_X);
            final Profile prf = player.prf;
            if (!prf.infiniteStamina) {
                initHudMeter(player.newStaminaMeter(), playerX + HEALTH_X + HEALTH_W);
            }
            if (!prf.infiniteLives) {
                final Pantext lifeCounter = player.newLifeCounter();
                lifeCounter.getPosition().set(playerX + HEALTH_X - 8, HEALTH_Y - 9, DEPTH_HUD);
                hud.addActor(lifeCounter);
            }
            Menu.addToggleButtons(new HudShootMode(player.pc), new HudJumpMode(player.pc));
        }
        
        @Override
        protected final void step() {
            RoomLoader.step();
        }
    }
    
    private final static int HEALTH_X = 24, HEALTH_Y = GAME_H - 73, HEALTH_W = 8;
    
    protected final static void initHudMeter(final Panctor healthMeter, final int x) {
        healthMeter.getPosition().set(x, HEALTH_Y, DEPTH_HUD);
        hud.addActor(healthMeter);
    }
    
    protected final static void initEnemyHealthMeter(final Panctor healthMeter) {
        initHudMeter(healthMeter, GAME_W - HEALTH_X - HEALTH_W);
    }
    
    protected final static void initPlayerStart() {
        playerStartX = defPlayerStartX;
        playerStartY = defPlayerStartY;
        playerStartMirror = defPlayerStartMirror;
    }
    
    protected final static PlayerContext getPrimaryPlayerContext() {
        return Coltil.isValued(pcs) ? pcs.get(0) : null;
    }
    
    protected final static void runPlayerContexts(final PlayerContextRunnable r) {
        for (final PlayerContext pc : pcs) {
            r.run(pc);
        }
    }
    
    protected interface PlayerContextRunnable {
        public void run(final PlayerContext pc);
    }
    
    protected final static void runPlayers(final PlayerRunnable r) {
        for (final PlayerContext pc : pcs) {
            final Player player = PlayerContext.getPlayer(pc);
            if (!Panctor.isDestroyed(player)) {
                r.run(player);
            }
        }
    }
    
    protected interface PlayerRunnable {
        public void run(final Player player);
    }
    
    protected final static Profile getPrimaryProfile() {
        final Profile prf = PlayerContext.getProfile(getPrimaryPlayerContext());
        return (prf == null) ? prf0 : prf;
    }
    
    protected final static Player getPrimaryPlayer() {
        return PlayerContext.getPlayer(getPrimaryPlayerContext());
    }
    
    protected final static PlayerContext getActivePlayerContext() {
        for (final PlayerContext pc : pcs) {
            if (!Panctor.isDestroyed(PlayerContext.getPlayer(pc))) {
                return pc;
            }
        }
        return getPrimaryPlayerContext();
    }
    
    protected final static Player getActivePlayer() {
        return PlayerContext.getPlayer(getActivePlayerContext());
    }
    
    protected final static Panctor newPlayerMean() {
        final PlayerMean mean = new PlayerMean();
        addActor(mean);
        return mean;
    }
    
    protected final static class PlayerMean extends Panctor implements StepEndListener {
        {
            getPosition().setY(112);
        }
        
        @Override
        public void onStepEnd(final StepEndEvent event) {
            float x = 0;
            int n = 0;
            for (final PlayerContext pc : pcs) {
                final Player player = PlayerContext.getPlayer(pc);
                if (Panctor.isDestroyed(player)) {
                    continue;
                }
                final Panple pos = player.getPosition();
                x += pos.getX();
                n++;
            }
            if (n > 0) {
                final Panple pos = getPosition();
                final float oldX = pos.getX(), desiredX = x / n, diff = desiredX - oldX, newX;
                if (diff > MAX_CAMERA_SPEED) {
                    newX = oldX + MAX_CAMERA_SPEED;
                } else if (diff < -MAX_CAMERA_SPEED) {
                    newX = oldX - MAX_CAMERA_SPEED;
                } else {
                    newX = desiredX;
                }
                pos.setX(newX);
            }
        }
    }
    
    protected final static Panlayer getLayer() {
        return room;
    }
    
    protected final static void addActor(final Panctor actor) {
        getLayer().addActor(actor);
    }
    
    private final static void notifyForce(final CharSequence msg) {
        if (Panctor.isDestroyed(notifications)) {
            notifications = new Notifications(Panlayer.isDetached(hud) ? room : hud, font).setDisplayTime(240).setRushedTime(105);
            notifications.getLabel().getPosition().set(8, GAME_H - 16, DEPTH_DIALOGUE_TEXT);
        }
        notifications.enqueue(Chartil.toString(msg));
    }
    
    protected final static void notify(final CharSequence msg) {
        if (Panctor.isDestroyed(notifications)) {
            notifyForce(msg);
            return;
        }
        final String currentText = notifications.getCurrentText();
        if (Chartil.equals(msg, currentText)) {
            return;
        }
        notifyForce(msg);
    }
    
    private final static void notifyForce(final CharSequence... msgs) {
        for (final CharSequence msg : msgs) {
            notifyForce(msg);
        }
    }
    
    protected final static void notify(final CharSequence... msgs) {
        if (Panctor.isDestroyed(notifications)) {
            notifyForce(msgs);
            return;
        }
        final String currentText = notifications.getCurrentText();
        for (final CharSequence msg : msgs) {
            if (Chartil.equals(msg, currentText)) {
                return;
            }
        }
        notifyForce(msgs);
    }
    
    protected final static void clearNotifications() {
        Panctor.destroy(notifications);
    }
    
    public final static void main(final String[] args) {
        try {
            new BotsnBoltsGame().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
