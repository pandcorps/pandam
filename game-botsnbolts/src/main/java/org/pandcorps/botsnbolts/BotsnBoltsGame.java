/*
Copyright (c) 2009-2016, Andrew M. Martin
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

import org.pandcorps.botsnbolts.Player.*;
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
    protected final static String YEAR = "2016";
    protected final static String AUTHOR = "Andrew M. Martin";
    
    protected final static String RES = "org/pandcorps/botsnbolts/";
    
    protected final static int DEPTH_BG = 0;
    protected final static int DEPTH_FG = 1;
    protected final static int DEPTH_PLAYER = 2;
    protected final static int DEPTH_PROJECTILE = 3;
    protected final static int DEPTH_OVERLAY = 4;
    
    private final static FinPanple2 ng = GuyPlatform.getMin(Player.PLAYER_X);
    private final static FinPanple2 xg = GuyPlatform.getMax(Player.PLAYER_X, Player.PLAYER_H);
    protected final static FinPanple2 og = new FinPanple2(17, 1);
    protected final static FinPanple2 oj = new FinPanple2(17, 4);
    protected final static FinPanple2 oss = new FinPanple2(13, 1);
    protected final static FinPanple2 os = new FinPanple2(15, 1);
    protected final static FinPanple2 ojs = new FinPanple2(15, 4);
    protected final static FinPanple originOverlay = new FinPanple(0, 0, DEPTH_OVERLAY);
    
    protected static Queue<Runnable> loaders = new LinkedList<Runnable>();
    protected static MultiFont font = null;
    private static PlayerImages voidImages = null;
    protected static Panframe[] doorTunnel = null;
    protected static Panframe[] doorTunnelOverlay = null;
    protected static ShootableDoorDefinition doorCyan = null;
    
    protected static PlayerContext pc = null;
    
    protected static TileMap tm = null;

    @Override
    protected final boolean isFullScreen() {
        return true;
    }
    
    @Override
    protected final void init(final Panroom room) throws Exception {
        final Pangine engine = Pangine.getEngine();
        engine.setTitle(TITLE);
        engine.setEntityMapEnabled(false);
        Imtil.onlyResources = true;
        if (loaders != null) {
            loaders.add(new Runnable() {
                @Override public final void run() {
                    loadResources();
                }});
        }
        Panscreen.set(new LogoScreen(TitleScreen.class, loaders));
    }
    
    private final static void loadResources() {
        font = Fonts.getClassics(new FontRequest(8), Pancolor.WHITE, Pancolor.BLACK);
        doorTunnel = newDoor("door.tunnel", "bg/DoorTunnel.png");
        doorTunnelOverlay = toOverlay(doorTunnel);
        doorCyan = newDoorDefinition("door.cyan", "bg/DoorCyan");
        voidImages = loadPlayerImages("betabot", "Void");
        pc = new PlayerContext(new Profile(), org.pandcorps.pandax.in.ControlScheme.getDefaultKeyboard(), voidImages);
    }
    
    private final static PlayerImages loadPlayerImages(final String dir, final String name) {
        final String pre = RES + "chr/" + dir + "/" + name;
        final PlayerImagesSubSet basicSet = loadPlayerImagesSubSet(pre, name, true, og, og, oj);
        final PlayerImagesSubSet shootSet = loadPlayerImagesSubSet(pre + "Shoot", name + ".shoot", false, oss, os, ojs);
        final Pangine engine = Pangine.getEngine();
        final Panmage basicProjectile = engine.createImage(pre + "Projectile", new FinPanple2(3, 3), new FinPanple2(-3, -1), new FinPanple2(5, 3), pre + "Projectile.png");
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
        return new PlayerImages(basicSet, shootSet, null, basicProjectile, ball);
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
    
    private final static ShootableDoorDefinition newDoorDefinition(final String id, final String path) {
        final Panframe[] door = newDoor(id, path + ".png");
        final Img[] imgs = Imtil.loadStrip(RES + path + "Opening.png", 16);
        final Panframe[] open1 = newDoor(id + ".1", imgs, 0);
        final Panframe[] open2 = newDoor(id + ".2", imgs, 2);
        final Panframe[] open3 = newDoor(id + ".3", imgs, 4);
        final Panframe[][] opening = { open1, open2, open3 };
        return new ShootableDoorDefinition(door, opening);
    }
    
    private final static Panframe[] newDoor(final String id, final String path) {
        return newDoor(id, Imtil.loadStrip(RES + path, 16), 0);
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
            final Pangine engine = Pangine.getEngine();
            engine.setBgColor(new org.pandcorps.core.img.FinPancolor((short) 232, (short) 232, (short) 232));
            final Panroom room = Pangame.getGame().getCurrentRoom();
            tm = new TileMap(Pantil.vmid(), room, 16, 16);
            tm.getPosition().setZ(DEPTH_BG);
            tm.setForegroundDepth(DEPTH_FG);
            room.addActor(tm);
            final TileMapImage[][] imgMap = tm.splitImageMap(engine.createImage("bg", RES + "bg/Bg.png"));
            final int end = tm.getWidth() - 1;
            for (int i = end; i >= 0; i--) {
                tm.setBackground(i, 0, imgMap[0][1], Tile.BEHAVIOR_SOLID);
            }
            for (int j = tm.getHeight() - 1; j > 0; j--) {
                tm.setBackground(0, j, imgMap[0][0], Tile.BEHAVIOR_SOLID);
                tm.setBackground(end, j, imgMap[0][2], Tile.BEHAVIOR_SOLID);
            }
            tm.setBackground(4, 2, imgMap[1][3], Tile.BEHAVIOR_SOLID);
            tm.setBackground(4, 3, imgMap[0][3], Tile.BEHAVIOR_SOLID);
            tm.setBackground(5, 2, imgMap[1][4], Tile.BEHAVIOR_SOLID);
            tm.setBackground(5, 3, imgMap[0][4], Tile.BEHAVIOR_SOLID);
            new ShootableDoor(room, 0, 1, doorCyan);
            new ShootableDoor(room, end, 1, doorCyan);
            final Player player = new Player(pc);
            player.getPosition().set(48, 96, DEPTH_PLAYER);
            room.addActor(player);
        }
    }
    
    public final static void main(final String[] args) {
        try {
            new BotsnBoltsGame().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
