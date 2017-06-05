/*
Copyright (c) 2009-2017, Andrew M. Martin
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

import org.pandcorps.botsnbolts.BlockPuzzle.*;
import org.pandcorps.botsnbolts.Enemy.*;
import org.pandcorps.botsnbolts.HudMeter.*;
import org.pandcorps.botsnbolts.Player.*;
import org.pandcorps.botsnbolts.RoomLoader.*;
import org.pandcorps.botsnbolts.ShootableDoor.*;
import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.game.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Fonts.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;

public final class BotsnBoltsGame extends BaseGame {
    protected final static String TITLE = "Bots 'n Bolts";
    protected final static String VERSION = "0.0.1";
    protected final static String YEAR = "2016-2017";
    protected final static String AUTHOR = "Andrew M. Martin";
    
    protected final static String RES = "org/pandcorps/botsnbolts/";
    
    protected final static int GAME_COLUMNS = 24;
    protected final static int GAME_ROWS = 14;
    protected final static int GAME_W = GAME_COLUMNS * 16; // 384
    protected final static int GAME_H = GAME_ROWS * 16; // 224;
    
    protected final static byte TILE_LADDER = 2; // Works like non-solid when not climbing
    protected final static byte TILE_LADDER_TOP = 3; // Works like floor when not climbing
    protected final static byte TILE_FLOOR = 4; // Used for blocks that fade in/out
    
    protected final static int DEPTH_BG = 0;
    protected final static int DEPTH_FG = 2;
    protected final static int DEPTH_CARRIER = 4;
    protected final static int DEPTH_POWER_UP = 6;
    protected final static int DEPTH_PLAYER = 8;
    protected final static int DEPTH_ENEMY = 10;
    protected final static int DEPTH_PROJECTILE = 12;
    protected final static int DEPTH_OVERLAY = 14;
    protected final static int DEPTH_BURST = 16;
    protected final static int DEPTH_HUD = 18;
    
    protected final static FinPanple2 MIN_16 = new FinPanple2(-6, -6);
    protected final static FinPanple2 MAX_16 = new FinPanple2(6, 6);
    protected final static FinPanple2 MIN_8 = new FinPanple2(-3, -3);
    protected final static FinPanple2 MAX_8 = new FinPanple2(3, 3);
    
    private final static FinPanple2 ng = GuyPlatform.getMin(Player.PLAYER_X);
    private final static FinPanple2 xg = GuyPlatform.getMax(Player.PLAYER_X, Player.PLAYER_H);
    protected final static FinPanple2 og = new FinPanple2(17, 1);
    protected final static FinPanple2 oj = new FinPanple2(17, 4);
    protected final static FinPanple2 oss = new FinPanple2(13, 1);
    protected final static FinPanple2 os = new FinPanple2(15, 1);
    protected final static FinPanple2 ojs = new FinPanple2(15, 4);
    protected final static FinPanple originOverlay = new FinPanple(0, 0, DEPTH_OVERLAY);
    private final static FinPanple2 minCube = new FinPanple2(-5, -5);
    private final static FinPanple2 maxCube = new FinPanple2(5, 5);
    
    protected static Queue<Runnable> loaders = new LinkedList<Runnable>();
    protected static MultiFont font = null;
    private static PlayerImages voidImages = null;
    protected static Panframe[] doorTunnel = null;
    protected static Panframe[] doorTunnelOverlay = null;
    protected static Panframe[] doorTunnelSmall = null;
    protected static Panframe[] doorTunnelSmallOverlay = null;
    protected static ShootableDoorDefinition doorCyan = null;
    protected static ShootableDoorDefinition doorGold = null;
    protected static ShootableDoorDefinition doorSilver = null;
    protected static ShootableDoorDefinition doorBlue = null;
    protected static ShootableDoorDefinition doorBlack = null;
    protected static ShootableDoorDefinition doorSmall = null;
    protected static Panmage button = null;
    protected static Panimation buttonFlash = null;
    protected static Panmage doorBoss = null;
    protected static Panmage ladder = null;
    protected static Panmage[] blockCyan = null;
    protected static Panmage[] blockTimed = null;
    protected static Panmage[] blockButton = null;
    protected static Panmage[] blockHidden = null;
    protected static Panmage[] barrierHidden = null;
    protected static Panimation carrier = null;
    protected static Panmage blockSpike = null;
    protected static Panmage spike = null;
    protected static Panmage[] cube = null;
    protected static Panmage[] sentryGun = null;
    protected static Panmage[] wallCannon = null;
    protected static Panimation propEnemy = null;
    protected static Panmage[] springEnemy = null;
    protected static Panimation crawlEnemy = null;
    protected static Panimation shieldedEnemy = null;
    protected static Panimation unshieldedEnemy = null;
    protected static Panmage[] fireballEnemy = null;
    protected static Panmage[] flamethrowerEnemy = null;
    protected static Panmage[] freezeRayEnemy = null;
    protected static Panmage enemyProjectile = null;
    protected static Panimation enemyBurst = null;
    protected static Panimation puff = null;
    protected static Panimation flash = null;
    protected static Panmage[] flame4 = null;
    protected static Panmage[] flame8 = null;
    protected static Panimation flame16 = null;
    protected static Panmage iceShatter = null;
    protected static HudMeterImages hudMeterBlank = null;
    protected static HudMeterImages hudMeterBoss = null;
    protected static Img[] hudMeterImgs = null;
    
    protected static PlayerContext pc = null;
    
    protected static Panlayer hud = null;
    protected static TileMap tm = null;
    protected static String  timgName = null;
    protected static Panmage timg = null;
    protected static Panmage timgPrev = null;
    protected static TileMapImage[][] imgMap = null;

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
        initTileBehaviors();
        if (loaders != null) {
            loaders.add(new Runnable() {
                @Override public final void run() {
                    loadResources();
                }});
        }
        Panscreen.set(new LogoScreen(TitleScreen.class, loaders));
    }
    
    private final static void initTileBehaviors() {
    }
    
    private final static void loadResources() {
        font = Fonts.getClassics(new FontRequest(8), Pancolor.WHITE, Pancolor.BLACK); //TODO 64 characters
        loadDoors();
        loadMisc();
        loadEnemies();
        loadPlayer();
        postProcess();
        RoomLoader.loadRooms();
    }
    
    private final static void loadDoors() {
        final Pangine engine = Pangine.getEngine();
        doorTunnel = newDoor("door.tunnel", "bg/DoorTunnel.png");
        doorTunnelOverlay = toOverlay(doorTunnel);
        doorTunnelSmall = newDoorSmall("door.tunnel.small", "bg/DoorTunnelSmall.png");
        doorTunnelSmallOverlay = toOverlay(doorTunnelSmall);
        final Img[] imgsClosed = Imtil.loadStrip(RES + "bg/DoorCyan.png", 16);
        Img.setTemporary(false, imgsClosed);
        final Img[] imgsOpening = Imtil.loadStrip(RES + "bg/DoorCyanOpening.png", 16);
        Img.setTemporary(false, imgsOpening);
        final Img[] imgsBarrier = Imtil.loadStrip(RES + "bg/BarrierCyan.png", 8);
        Img.setTemporary(false, imgsBarrier);
        doorCyan = newDoorDefinition("door.cyan", imgsClosed, imgsOpening, null, 0, null, null, imgsBarrier);
        final short s0 = 0, s48 = 48, s64 = 64, s96 = 96, s128 = 128, s144 = 144, s192 = 192, smax = Pancolor.MAX_VALUE;
        final Pancolor cyan = Pancolor.CYAN, silver = Pancolor.GREY, darkCyan = new FinPancolor(s0, s192, s192), darkSilver = Pancolor.DARK_GREY;
        doorSilver = filterDoor("door.silver", imgsClosed, imgsOpening, cyan, silver, darkCyan, darkSilver, null, 0, null,
            Integer.valueOf(Projectile.POWER_MAXIMUM), imgsBarrier);
        final Pancolor blue = newColorBlue(), darkBlue = newColorBlueDark();
        doorBlue = filterDoor("door.blue", imgsClosed, imgsOpening, silver, blue, darkSilver, darkBlue, null, 0, null,
            Integer.valueOf(Projectile.POWER_IMPOSSIBLE), imgsBarrier);
        final Pancolor black = new FinPancolor(s96), darkBlack = new FinPancolor(s64);
        doorBlack = filterDoor("door.black", imgsClosed, imgsOpening, blue, black, darkBlue, darkBlack, null, 0, null,
            Integer.valueOf(Projectile.POWER_IMPOSSIBLE), imgsBarrier);
        final ShootableDoorDefinition doorRed, doorRedOrange, doorOrange, doorOrangeGold;
        final Pancolor red = Pancolor.RED, darkRed = new FinPancolor(s192, s0, s0);
        doorRed = filterDoor("door.red", imgsClosed, imgsOpening, black, red, darkBlack, darkRed, null, 15, Player.SHOOT_RAPID, null, imgsBarrier);
        final Pancolor redOrange = new FinPancolor(smax, s64, s0), darkRedOrange = new FinPancolor(s192, s48, s0);
        doorRedOrange = filterDoor("door.red.orange", imgsClosed, null, red, redOrange, darkRed, darkRedOrange, doorRed, 10, Player.SHOOT_RAPID, null, imgsBarrier);
        final Pancolor orange = new FinPancolor(smax, s128, s0), darkOrange = new FinPancolor(s192, s96, s0);
        doorOrange = filterDoor("door.orange", imgsClosed, null, redOrange, orange, darkRedOrange, darkOrange, doorRedOrange, 6, null, null, imgsBarrier);
        final Pancolor orangeGold = new FinPancolor(smax, s192, s0), darkOrangeGold = new FinPancolor(s192, s144, s0);
        doorOrangeGold = filterDoor("door.orange.gold", imgsClosed, null, orange, orangeGold, darkOrange, darkOrangeGold, doorOrange, 3, null, null, imgsBarrier);
        final Pancolor gold = Pancolor.YELLOW, darkGold = new FinPancolor(s192, s192, s0);
        doorGold = filterDoor("door.gold", imgsClosed, null, orangeGold, gold, darkOrangeGold, darkGold, doorOrangeGold, 1, null, null, imgsBarrier);
        Img.close(imgsClosed);
        Img.close(imgsOpening);
        final Img[] imgsSmallClosed = Imtil.loadStrip(RES + "bg/DoorSmall.png", 16);
        Img.setTemporary(false, imgsSmallClosed);
        final Img[] imgsSmallOpening = Imtil.loadStrip(RES + "bg/DoorSmallOpening.png", 16);
        Img.setTemporary(false, imgsSmallOpening);
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
    
    private final static void loadMisc() {
        final Pangine engine = Pangine.getEngine();
        hudMeterBlank = newHudMeterImages("meter.blank", RES + "misc/MeterBlank.png", false);
        cube = newSheet("cube", RES + "misc/Cube.png", 16);
        final Img[] blockImgs = Imtil.loadStrip(RES + "bg/BlockCyan.png", 16);
        Img.setTemporary(false, blockImgs);
        final Panple maxBlock = new FinPanple2(14, 16);
        final short s0 = 0, s64 = 64, s96 = 96, s128 = 128, s144 = 144, s192 = 192;
        final Pancolor cyan = Pancolor.CYAN, darkCyan = new FinPancolor(s0, s192, s192);
        blockCyan = newBlock("block.cyan", blockImgs, maxBlock, null, null, null, null);
        final Pancolor timed = new FinPancolor(s144, s192, s96), darkTimed = new FinPancolor(s96, s128, s64);
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
        puff = newAnimation("puff", RES + "misc/Puff.png", 8, CENTER_8, 2);
        flash = newAnimation("flash", RES + "misc/Flash.png", 32, CENTER_32, 12);
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
            spike = Pangine.getEngine().createImage("spike", CENTER_16, MIN_16, MAX_16, RES + "bg/Spike.png");
        }
        return spike;
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
        return newSheet("block.timed", blockImgs, FinPanple.ORIGIN, ShootableDoor.minBarrier, maxBlock);
    }
    
    private final static void loadEnemies() {
        final Pangine engine = Pangine.getEngine();
        final Img[] sentryImgs = Imtil.loadStrip(RES + "enemy/SentryGun.png", 16);
        final int sentrySize = sentryImgs.length;
        sentryGun = new Panmage[sentrySize];
        for (int i = 0; i < sentrySize; i++) {
            sentryGun[i] = engine.createImage("sentry.gun." + i, CENTER_16, minCube, maxCube, sentryImgs[i]);
        }
        wallCannon = null; //TODO
        final Panple propO = new FinPanple2(8, 1), propMin = Chr.getMin(Enemy.PROP_OFF_X), propMax = Chr.getMax(Enemy.PROP_OFF_X, Enemy.PROP_H);
        propEnemy = newAnimation("prop.enemy", RES + "enemy/PropEnemy.png", 16, propO, propMin, propMax, 4);
        springEnemy = newSheet("spring.enemy", RES + "enemy/SpringEnemy.png", 16, new FinPanple2(8, 3), propMin, propMax);
        final Panple crawlMax = Chr.getMax(Enemy.PROP_OFF_X, Enemy.CRAWL_H);
        crawlEnemy = newAnimation("crawl.enemy", RES + "enemy/CrawlEnemy.png", 16, propO, propMin, crawlMax, 4);
        final Panple shieldedO = new FinPanple2(8, 2);
        shieldedEnemy = newAnimation("shielded.enemy", RES + "enemy/ShieldedEnemy.png", 16, shieldedO, propMin, crawlMax, 3);
        unshieldedEnemy = newAnimation("unshielded.enemy", RES + "enemy/UnshieldedEnemy.png", 16, shieldedO, propMin, crawlMax, 3);
        fireballEnemy = newSheet("fireball.enemy", RES + "enemy/FireballEnemy.png", 16, propO, propMin, crawlMax);
        final Panple henchO = new FinPanple2(15, 1), henchMin = Chr.getMin(Enemy.HENCHBOT_OFF_X), henchMax = Chr.getMax(Enemy.HENCHBOT_OFF_X, Enemy.HENCHBOT_H);
        final Img[] henchImgs = Imtil.loadStrip(RES + "enemy/Henchbot.png", 32);
        Img.setTemporary(false, henchImgs);
        flamethrowerEnemy = newSheet("flamethrower.enemy", henchImgs, henchO, henchMin, henchMax);
        final short s0 = 0, s96 = 96, s128 = 128, s160 = 160, s192 = 192, smax = Pancolor.MAX_VALUE;
        final Pancolor fire = new FinPancolor(smax, s160, s0), darkFire = new FinPancolor(smax, s96, s0);
        final Pancolor grey = Pancolor.DARK_GREY, darkGrey = new FinPancolor(s96);
        final Pancolor ice = newColorIce(), darkIce = newColorIceDark();
        final Pancolor blue = new FinPancolor(s128, s128, smax), darkBlue = new FinPancolor(s96, s96, s192);
        filterImgs(henchImgs, newFilter(fire, ice, darkFire, darkIce, grey, blue, darkGrey, darkBlue));
        freezeRayEnemy = newSheet("freezeray.enemy", henchImgs, henchO, henchMin, henchMax);
        Img.close(henchImgs);
        enemyBurst = newAnimation("burst.enemy", RES + "enemy/EnemyBurst.png", 16, CENTER_16, 2);
        flame4 = newSheet("flame.4.enemy", RES + "enemy/Flame4.png", 4);
        flame8 = newSheet("flame.8.enemy", RES + "enemy/Flame8.png", 8);
        flame16 = newAnimation("flame.16.enemy", RES + "enemy/Flame16.png", 16, propO, propMin, propMax, 3);
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
        voidImages = loadPlayerImages("betabot", "Void");
        pc = new PlayerContext(new Profile(), org.pandcorps.pandax.in.ControlScheme.getDefaultKeyboard(), voidImages);
    }
    
    private final static PlayerImages loadPlayerImages(final String dir, final String name) {
        final String pre = RES + "chr/" + dir + "/" + name;
        final PlayerImagesSubSet basicSet = loadPlayerImagesSubSet(pre, name, true, og, og, oj);
        final PlayerImagesSubSet shootSet = loadPlayerImagesSubSet(pre + "Shoot", name + ".shoot", false, oss, os, ojs);
        final Pangine engine = Pangine.getEngine();
        final Img imgHurt = Imtil.load(pre + "Hurt.png"), imgHurtMirror = Imtil.load(pre + "HurtMirror.png");
        Img.setTemporary(false, imgHurt, imgHurtMirror);
        final Panmage hurt = newPlayerImage(PRE_IMG + "." + name + ".hurt", oj, imgHurt, imgHurtMirror);
        final short s0 = 0, s72 = 72, s96 = 96, s128 = 128, s144 = 144, s192 = 192;
        final Pancolor grey = Pancolor.DARK_GREY, darkGrey = new FinPancolor(s96);
        final Pancolor pri = Pancolor.GREEN, darkPri = new FinPancolor(s0, s192, s0);
        final Pancolor skin = new FinPancolor(s192, s128, s96), darkSkin = new FinPancolor(s144, s96, s72);
        final Pancolor frz = Pancolor.WHITE, darkFrz = newColorIce();
        final Pancolor out = Pancolor.BLACK, outFrz = newColorIceDark();
        final ReplacePixelFilter frzFilter = new ReplacePixelFilter(grey, frz, darkGrey, darkFrz, pri, frz, darkPri, darkFrz, skin, frz, darkSkin, darkFrz, out, outFrz);
        filterImgs(new Img[] { imgHurt, imgHurtMirror }, frzFilter);
        final Panmage frozen = newPlayerImage(PRE_IMG + "." + name + ".frozen", oj, imgHurt, imgHurtMirror);
        Img.close(imgHurt, imgHurtMirror);
        final Panimation defeat = newAnimation(pre + "DefeatOrb", pre + "DefeatOrb.png", 16, CENTER_16, 6);
        final Panple oClimb = new FinPanple2(15, 4);
        final Img[] climbImgs = Imtil.loadStrip(pre + "Climb.png", 32);
        final Img[] climbImgsMirror = Imtil.loadStrip(pre + "ClimbMirror.png", 32);
        final Panmage climb = newPlayerImage(pre + "Climb", oClimb, climbImgs, climbImgsMirror, 0);
        final Panmage climbShoot = newPlayerImage(pre + "Climb.Shoot", oClimb, climbImgs, climbImgsMirror, 1);
        final Panmage climbTop = newPlayerImage(pre + "Climb.Top", oClimb, climbImgs, climbImgsMirror, 2);
        final Img[] jumpAimImgs = Imtil.loadStrip(pre + "JumpAim.png", 32);
        final Img[] jumpAimImgsMirror = Imtil.loadStrip(pre + "JumpAimMirror.png", 32);
        final Panmage jumpAimDiag = newPlayerImage(pre + "Jump.Aim.Diag", ojs, jumpAimImgs, jumpAimImgsMirror, 0);
        final Panmage jumpAimUp = newPlayerImage(pre + "Jump.Aim.Diag", ojs, jumpAimImgs, jumpAimImgsMirror, 1);
        final Panmage basicProjectile = engine.createImage(pre + "Projectile", new FinPanple2(3, 3), new FinPanple2(-3, -2), new FinPanple2(5, 3), pre + "Projectile.png");
        final Panimation projectile2 = newFlipper(pre + "Projectile2", pre + "Projectile2.png", new FinPanple2(7, 7), new FinPanple2(-4, -5), new FinPanple2(8, 6), 4);
        final Panimation projectile3 = newProjectile3(pre);
        final Panimation burst = newAnimation(pre + "Burst", pre + "Burst.png", 16, CENTER_16, 2);
        final Panimation charge = newAnimation(pre + "Charge", pre + "Charge.png", 8, null, 1);
        final Panple oChargeVert = new FinPanple2(4, 0);
        final Panimation chargeVert = newAnimation(pre + "ChargeVert", pre + "ChargeVert.png", 8, oChargeVert, 1);
        final Panimation charge2 = newAnimation(pre + "Charge2", pre + "Charge2.png", 8, null, 1);
        final Panimation chargeVert2 = newAnimation(pre + "ChargeVert2", pre + "ChargeVert2.png", 8, oChargeVert, 1);
        final Img[] ballImgs = Imtil.loadStrip(pre + "Ball.png", 16);
        final Panmage ball[] = new Panmage[8];
        final Panple ob = new FinPanple2(8, 1), xb = GuyPlatform.getMax(Player.PLAYER_X, Player.BALL_H);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; j++) {
                final int index = i * 2 + j;
                final Img ballImg = ballImgs[j];
                ballImg.setTemporary(false);
                ball[index] = engine.createImage(pre + "Ball." + index, ob, ng, xb, ballImg);
                if (i < 3) {
                    Imtil.rotate(ballImg);
                } else {
                    ballImg.close();
                }
            }
        }
        final Panmage warp = engine.createImage(pre + "Warp", new FinPanple2(5, 1), null, null, pre + "Warp.png");
        final Panimation materialize = newAnimation(pre + "Materialize", pre + "Materialize.png", 32, og, 3);
        final Panimation bomb = newAnimation(pre + "Bomb", pre + "Bomb.png", 8, CENTER_8, 5);
        final Panmage link = engine.createImage(pre + "Link", new FinPanple2(4, 1), null, null, pre + "Link.png");
        final Panple oBattery = new FinPanple2(8, -1);
        final Panimation batterySml = newOscillation(pre + "battery.sml", pre + "BatterySmall.png", 8, new FinPanple2(4, -1), new FinPanple2(-2, 2), new FinPanple2(2, 6), 3, 6);
        final Panimation batteryMed = newOscillation(pre + "battery.med", pre + "BatteryMedium.png", 16, oBattery, new FinPanple2(-4, 2), new FinPanple2(4, 10), 3, 6);
        final Panimation batteryBig = newOscillation(pre + "battery.big", pre + "BatteryBig.png", 16, oBattery, new FinPanple2(-6, 2), new FinPanple2(6, 14), 3, 6);
        final Panmage bolt = null; //TODO
        final Panmage byteDisk = null; //TODO
        final Panmage powerBox = engine.createImage(pre + "PowerBox", CENTER_16, minCube, maxCube, pre + "PowerBox.png");
        final Panmage byteBox = null; //TODO
        final HudMeterImages hudMeterImages = newHudMeterImages(pre + "Meter", pre + "Meter.png", true);
        return new PlayerImages(basicSet, shootSet, hurt, frozen, defeat, climb, climbShoot, climbTop, jumpAimDiag, jumpAimUp, basicProjectile, projectile2, projectile3, charge, chargeVert, charge2, chargeVert2,
            burst, ball, warp, materialize, bomb, link, batterySml, batteryMed, batteryBig, bolt, byteDisk, powerBox, byteBox, hudMeterImages);
    }
    
    private final static PlayerImagesSubSet loadPlayerImagesSubSet(final String path, final String name, final boolean startNeeded, final Panple os, final Panple o, final Panple oj) {
        final String pre = PRE_IMG + "." + name + ".";
        final Img[] imgs = Imtil.loadStrip(path + ".png", 32);
        final Img[] imgsMirror = Imtil.loadStrip(path + "Mirror.png", 32);
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
            final Img[] crouchImgsMirror = Imtil.loadStrip(path + "CrouchMirror.png", 32);
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
    
    private final static Panimation newProjectile3(final String pre) {
        final Pangine engine = Pangine.getEngine();
        final Img[] imgs = Imtil.loadStrip(pre + "Projectile3.png", 32);
        final Panmage img0 = engine.createImage(pre + ".0", imgs[0]);
        final Panmage img1 = engine.createImage(pre + ".1", imgs[1]);
        final Panple o = new FinPanple2(23, 7), min = new FinPanple2(-6, -7), max = new FinPanple2(8, 8), size = new FinPanple2(32, 16);
        return engine.createAnimation(pre + ".anm",
            newProjectile3Frame(pre, 0, o, min, max, img0, 0, 0, size),
            newProjectile3Frame(pre, 1, o, min, max, img0, 0, 16, size),
            newProjectile3Frame(pre, 2, o, min, max, img1, 0, 0, size),
            newProjectile3Frame(pre, 3, o, min, max, img1, 0, 16, size));
    }
    
    private final static Panframe newProjectile3Frame(final String pre, final int i, final Panple o, final Panple min, final Panple max, final Panmage src, final float x, final float y, final Panple size) {
        return newSubFrame(pre, i, o, min, max, src, x, y, size, 2);
    }
    
    private final static Panframe newSubFrame(final String pre, final int i, final Panple o, final Panple min, final Panple max, final Panmage src, final float x, final float y, final Panple size, final int dur) {
        return Pangine.getEngine().createFrame(pre + ".frm." + i, new SubPanmage(pre + ".sub." + i, o, min, max, src, x, y, size), dur);
    }
    
    private final static void postProcess() {
        final short s0 = 0, s96 = 96, s192 = 192;
        filterImgs(hudMeterImgs, newFilter(Pancolor.GREEN, Pancolor.DARK_GREY, new FinPancolor(s0, s192, s0), new FinPancolor(s96)));
        hudMeterBoss = newHudMeterImages("meter.boss", hudMeterImgs);
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
        return newAnimation(id, path, w, o, null, null, dur);
    }
    
    private final static Panimation newAnimation(final String id, final String path, final int w, final Panple o, final Panple min, final Panple max, final int dur) {
        return Pangine.getEngine().createAnimation(PRE_ANM + id, newFrames(id, path, w, o, min, max, dur, dur, dur, false));
    }
    
    private final static Panframe[] newFrames(final String id, final String path, final int w, final Panple o, final Panple min, final Panple max,
                                              final int durStart, final int durMid, final int durEnd, final boolean oscillate) {
        final Img[] imgs = Imtil.loadStrip(path, w);
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
    
    private final static Panimation newOscillation(final String id, final String path, final int w, final Panple o, final Panple min, final Panple max, final int durEdge, final int durMid) {
        final Panframe[] frames = newFrames(id, path, w, o, min, max, durEdge, durMid, durEdge, true);
        final int mid = (frames.length / 2) - 1, off = mid + 2;
        for (int i = 0; i < mid; i++) {
            frames[off + i] = frames[mid - i];
        }
        return Pangine.getEngine().createAnimation(PRE_ANM + id, frames);
    }
    
    private final static Panimation newFlipper(final String id, final String path, final Panple o, final Panple min, final Panple max, final int dur) {
        final Pangine engine = Pangine.getEngine();
        final Panmage img = engine.createImage(id, o, min, max, path);
        final Panframe[] frames = new Panframe[2];
        frames[0] = engine.createFrame(id + ".0", img, dur);
        frames[1] = engine.createFrame(id + ".1", img, dur, 0, false, true);
        return engine.createAnimation(PRE_ANM + id, frames);
    }
    
    private final static Panmage newPlayerImage(final String id, final Panple o, final Img[] imgs, final Img[] imgsMirror, final int i) {
        return newPlayerImage(id, o, imgs[i], imgsMirror[i]);
    }
    
    private final static Panmage newPlayerImage(final String id, final Panple o, final String path) {
        return newPlayerImage(id, o, Imtil.load(path + ".png"), Imtil.load(path + "Mirror.png"));
    }
    
    private final static Panmage newPlayerImage(final String id, final Panple o, final Img img, final Img imgMirror) {
        final Pangine engine = Pangine.getEngine();
        final Panmage image = engine.createImage(id, o, ng, xg, img);
        image.setMirrorSource(engine.createImage(id + ".mirror", o, ng, xg, imgMirror));
        return image;
    }
    
    private final static HudMeterImages newHudMeterImages(final String id, final String path, final boolean saveImgs) {
        final Img[] imgs = Imtil.loadStrip(path, 8);
        if (saveImgs) {
            Img.setTemporary(false, imgs);
            hudMeterImgs = imgs;
        }
        return newHudMeterImages(id, imgs);
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
            addText(room, "Press anything", w2, 56);
            addText(room, "Copyright " + Pantext.CHAR_COPYRIGHT + " " + YEAR, w2, 26);
            addText(room, AUTHOR, w2, 16);
            actor.register(new ActionEndListener() {
                @Override public final void onActionEnd(final ActionEndEvent event) {
                    Panscreen.set(new BotsnBoltsScreen());
                }});
        }
        
        private final void addText(final Panroom room, final String s, final int x, final int y) {
            final Pantext text = new Pantext(Pantil.vmid(), font, s);
            text.getPosition().set(x, y);
            text.centerX();
            room.addActor(text);
        }
        
        @Override
        protected final void destroy() {
            title.destroy();
        }
    }
    
    protected final static class BotsnBoltsScreen extends Panscreen {
        @Override
        protected final void load() {
            //final Panroom room = Pangame.getGame().getCurrentRoom();
            //initRoom(room);
            //fillRoom(room);
            loadRoom(RoomLoader.getStartRoom());
        }
        
        protected final static void loadRoom(final BotRoom botRoom) {
            final Panroom room = Player.loadRoom(botRoom);
            Pangame.getGame().setCurrentRoom(room);
            newPlayer(room);
            RoomLoader.onChangeFinished();
        }
        
        protected final static Panroom newRoom(final int w) {
            final Panple size = Pangame.getGame().getCurrentRoom().getSize();
            final Panroom room = Pangine.getEngine().createRoom(Pantil.vmid(), w, size.getY(), size.getZ());
            initRoom(room);
            return room;
        }
        
        protected final static Panroom newDemoRoom() {
            final Panroom room = newRoom(GAME_W);
            fillRoom(room);
            return room;
        }
        
        protected final static void initRoom(final Panroom room) {
            tm = new TileMap(Pantil.vmid(), room, 16, 16);
            tm.getPosition().setZ(DEPTH_BG);
            tm.setForegroundDepth(DEPTH_FG);
            room.addActor(tm);
        }
        
        protected final static void loadTileImage(final String imgName) {
            if (imgName.equals(timgName)) {
                tm.setImageMap(timg);
                return;
            }
            timgPrev = timg;
            timgName = imgName;
            timg = Pangine.getEngine().createImage("bg", RES + "bg/" + imgName + ".png");
            if (imgMap == null) {
                imgMap = tm.splitImageMap(timg);
            } else {
                tm.setImageMap(timg);
            }
        }
        
        protected final static void fillRoom(final Panroom room) {
            final Pangine engine = Pangine.getEngine();
            engine.setBgColor(new FinPancolor((short) 232, (short) 232, (short) 232));
            loadTileImage("Bg");
            final int end = tm.getWidth() - 1;
            for (int i = end; i >= 0; i--) {
                tm.setBackground(i, 0, imgMap[0][1], Tile.BEHAVIOR_SOLID);
            }
            for (int j = tm.getHeight() - 1; j > 4; j--) {
                tm.setBackground(0, j, imgMap[0][0], Tile.BEHAVIOR_SOLID);
                tm.setBackground(end, j, imgMap[0][2], Tile.BEHAVIOR_SOLID);
            }
            Enemy.newCube(4, 2);
            //new ShootableDoor(0, 1, doorCyan);
            //tm.setBackground(1, 2, imgMap[1][4], Tile.BEHAVIOR_SOLID);
            //new ShootableDoor(0, 1, doorSmall);
            //new ShootableDoor(end, 1, doorCyan);
            //new ShootableDoor(end, 1, doorGold);
            //new ShootableDoor(end, 1, doorSilver);
            //tm.setBackground(end - 1, 2, imgMap[1][3], Tile.BEHAVIOR_SOLID);
            //new ShootableDoor(end, 1, doorSmall);
            //new SentryGun(11, 1);
            //new SentryGun(8, 3);
            new PropEnemy(6, 2);
            //final BigBattery battery = new BigBattery();
            //battery.getPosition().set(200, 96, DEPTH_POWER_UP);
            //room.addActor(battery);
            //new PowerBox(12, 1);
            //new ShootableBarrier(6, 1, doorCyan);
            //new ShootableBarrier(5, 1, doorSmall);
            //final int px = 3, px2 = px + 4, py = 2; // 14, 4
            //new ShootableBlockPuzzle(
            //    new int[] { tm.getIndex(px, py), tm.getIndex(px2, py + 4), tm.getIndex(px, py + 4), tm.getIndex(px2, py + 8) },
            //    new int[] { tm.getIndex(px, py + 2), tm.getIndex(px2, py + 6), tm.getIndex(px, py + 6) });
            //Enemy.newCube(1, 3);
            //new ShootableBlockPuzzle(
            //    new int[] { tm.getIndex(4, 2), tm.getIndex(10, 5) },
            //    new int[] { tm.getIndex(6, 6) });
            //new SpikeBlockPuzzle(
            //    new int[] { tm.getIndex(4, 3) },
            //    new int[] { tm.getIndex(7, 4) });
            /*new TimedBlockPuzzle(Arrays.asList(
                new int[] { tm.getIndex(4, 3), tm.getIndex(4, 9) },
                new int[] { tm.getIndex(6, 5) },
                new int[] { tm.getIndex(8, 7) }));*/
            final int ladderX = 15;
            for (int j = 4; j < 9; j++) {
                tm.setForeground(ladderX, j, imgMap[0][1], (j == 8) ? TILE_LADDER_TOP : TILE_LADDER);
            }
            for (int i = ladderX + 1; i <= (ladderX + 3); i++) {
                tm.setTile(i, 0, null);
            }
            for (int j = 0; j < 14; j++) {
                tm.setForeground(ladderX + 3, j, imgMap[0][1], (j == 12) ? TILE_LADDER_TOP : TILE_LADDER);
            }
        }
        
        private final static void newPlayer(final Panroom room) {
            final Player player = new Player(pc);
            player.getPosition().set(48, 32, DEPTH_PLAYER);
            room.addActor(player);
            Pangine.getEngine().track(player);
            new Warp(player);
            newHud(room, player);
        }
        
        private final static void newHud(final Panroom room, final Player player) {
            hud = createHud(room);
            hud.setClearDepthEnabled(false);
            initHealthMeter(player.newHealthMeter(), true);
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
    
    protected final static Panlayer getLayer() {
        return tm.getLayer();
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
