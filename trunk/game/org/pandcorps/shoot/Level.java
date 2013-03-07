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
package org.pandcorps.shoot;

import java.awt.image.*;
import java.util.*;
import java.util.concurrent.Callable;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.shoot.PowerUp.*;

public abstract class Level {
    private final static BufferedImage createBlueCityImage() {
        final BufferedImage raw = Imtil.load("org/pandcorps/shoot/res/bg/TileCity.png");
        final HashMap<Pancolor, Pancolor> map = new HashMap<Pancolor, Pancolor>();
        putBlue(map, 40, 40, 24);
        putBlue(map, 56, 64, 0);
        putBlue(map, 64, 72, 0);
        putBlue(map, 72, 80, 0);
        putBlue(map, 80, 88, 0);
        putBlue(map, 80, 88, 8);
        putBlue(map, 88, 96, 0);
        putBlue(map, 96, 104, 0);
        putBlue(map, 96, 104, 8);
        putBlue(map, 96, 104, 24);
        putBlue(map, 104, 112, 0);
        putBlue(map, 104, 112, 8);
        putBlue(map, 104, 112, 24);
        putBlue(map, 104, 112, 32);
        putBlue(map, 112, 120, 8);
        putBlue(map, 112, 120, 24);
        putBlue(map, 112, 120, 32);
        putBlue(map, 112, 120, 40);
        putBlue(map, 120, 128, 0);
        putBlue(map, 120, 128, 8);
        putBlue(map, 120, 128, 24);
        putBlue(map, 120, 128, 40);
        putBlue(map, 128, 136, 8);
        putBlue(map, 128, 136, 24);
        putBlue(map, 128, 136, 40);
        putBlue(map, 128, 144, 16);
        putBlue(map, 136, 144, 24);
        putBlue(map, 136, 144, 40);
        putBlue(map, 136, 152, 16);
        putBlue(map, 144, 152, 24);
        putBlue(map, 144, 152, 40);
        putBlue(map, 144, 160, 32);
        putBlue(map, 152, 160, 40);
        putBlue(map, 152, 160, 48);
        putBlue(map, 152, 160, 56);
        putBlue(map, 152, 168, 32);
        putBlue(map, 160, 168, 40);
        putBlue(map, 160, 168, 48);
        putBlue(map, 160, 168, 56);
        putBlue(map, 160, 168, 72);
        putBlue(map, 160, 176, 48);
        putBlue(map, 168, 176, 48);
        putBlue(map, 168, 176, 56);
        putBlue(map, 168, 176, 72);
        putBlue(map, 168, 184, 48);
        putBlue(map, 168, 184, 56);
        putBlue(map, 176, 184, 56);
        putBlue(map, 176, 192, 64);
        putBlue(map, 184, 192, 72);
        putBlue(map, 192, 200, 72);
        putBlue(map, 192, 208, 80);
        putBlue(map, 200, 216, 80);
        final ReplacePixelFilter filter = new ReplacePixelFilter(map);
        return Imtil.filter(raw, filter);
    }
    
    private final static void putBlue(final HashMap<Pancolor, Pancolor> map, final int r, final int g, final int b) {
        final short rs = (short) r, gs = (short) g;
        map.put(new Pancolor(rs, gs, (short) b), new Pancolor((short) ((b + g) / 2), rs, gs));
        
    }
    
    protected void start() {
        init();
    }
    
    protected abstract void init();
    
    /*package*/ final static class E1M1 extends Level {
        @Override
        protected final void init() {
            final Panroom room = ShootGame.room;
            final TileMap tm = new TileMap("act.bg", room, 16, 16);
            ShootGame.tm = tm;
            final int w = tm.getWidth(), s = w / 16;
            final Pangine engine = Pangine.getEngine();
            final String tmName = "img.bg.city";
            tm.setImageMap(engine.getImage(tmName, new Callable<Panmage>() { @Override public Panmage call() {
            	return engine.createImage(tmName, "org/pandcorps/shoot/res/bg/TileCity.png"); }}));
            final TileMapImage[][] imgMap = tm.splitImageMap();
            tm.fillBackground(imgMap[7][0], 0, 2);
            tm.fillBackground(imgMap[6][0], 2, 1);
            tm.fillBackground(imgMap[5][0], 3, 2);
            tm.fillBackground(imgMap[4][1], 5, 1);
            tm.fillBackground(imgMap[3][1], 6, 6);
            
            tm.randBackground(imgMap[3][4], 6, 6, s * 4); // Chipped brick
            tm.randBackground(imgMap[3][5], 6, 6, s * 5); // Cracked brick
            
            tm.getTile(2, 6).setBackground(imgMap[3][2]); // Black storefront
            tm.getTile(3, 6).setBackground(imgMap[1][4]);
            tm.getTile(4, 6).setBackground(imgMap[1][5]);
            tm.getTile(5, 6).setBackground(imgMap[1][3]);
            tm.getTile(6, 6).setBackground(imgMap[0][6]);
            tm.getTile(7, 6).setBackground(imgMap[1][4]);
            tm.getTile(8, 6).setBackground(imgMap[0][3]);
            tm.getTile(9, 6).setBackground(imgMap[0][4]);
            tm.getTile(10, 6).setBackground(imgMap[0][5]);
            tm.getTile(11, 6).setBackground(imgMap[3][0]);
            
            tm.getTile(13, 6).setBackground(imgMap[2][6]); // Pipe
            tm.getTile(13, 7).setBackground(imgMap[2][6]);
            tm.getTile(13, 8).setBackground(imgMap[1][6]);
            
            tm.getTile(15, 6).setBackground(imgMap[3][2]); // Blue storefront
            tm.getTile(16, 6).setBackground(imgMap[2][3]);
            tm.getTile(17, 6).setBackground(imgMap[2][5]);
            tm.getTile(18, 6).setBackground(imgMap[2][4]);
            tm.getTile(19, 6).setBackground(imgMap[3][0]);
            
            tm.randBackground(imgMap[7][3], 0, 2, s * 2); // Street crack
            tm.randBackground(imgMap[7][4], 3, 2, s * 2); // Sidewalk crack
            for (int i = 1; i < w; i += 3) { // Street paint
                tm.getTile(i, 1).setBackground(imgMap[7][1]);
            }
            for (int i = 2; i < w; i += 9) { // Manhole
                tm.getTile(i, 0).setBackground(imgMap[7][2]);
            }
            for (int i = 5; i < w; i += 9) { // Grate
                tm.getTile(i, 2).setBackground(imgMap[6][2]);
            }
            for (int i = 8; i < w; i += 9) { // Vent
                tm.getTile(i, 4).setBackground(imgMap[5][2]);
            }
            
            tm.getTile(21, 2).setBackground(imgMap[6][3]);
            tm.getTile(22, 2).setBackground(imgMap[7][0]);
            tm.getTile(23, 2).setBackground(imgMap[7][0]);
            tm.getTile(24, 2).setBackground(imgMap[7][0]);
            tm.getTile(25, 2).setBackground(imgMap[6][4]);
            tm.getTile(21, 3).setBackground(imgMap[5][3]);
            tm.getTile(22, 3).setBackground(imgMap[6][3]);
            tm.getTile(23, 3).setBackground(imgMap[7][0]);
            tm.getTile(24, 3).setBackground(imgMap[7][0]);
            tm.getTile(25, 3).setBackground(imgMap[5][4]);
            tm.getTile(22, 4).setBackground(imgMap[5][3]);
            tm.getTile(23, 4).setBackground(imgMap[6][3]);
            tm.getTile(24, 4).setBackground(imgMap[7][0]);
            tm.getTile(25, 4).setBackground(imgMap[7][0]);
            tm.getTile(26, 4).setBackground(imgMap[5][4]);
            tm.getTile(23, 5).setBackground(imgMap[4][2]);
            tm.getTile(24, 5).setBackground(imgMap[4][3]);
            tm.getTile(25, 5).setBackground(imgMap[4][3]);
            tm.getTile(26, 5).setBackground(imgMap[4][3]);
            tm.getTile(27, 5).setBackground(imgMap[4][4]);
            tm.getTile(23, 6).setBackground(imgMap[3][2]);
            tm.getTile(24, 6).setBackground(imgMap[3][3]);
            tm.getTile(25, 6).setBackground(imgMap[3][3]);
            tm.getTile(26, 6).setBackground(imgMap[3][3]);
            tm.getTile(27, 6).setBackground(imgMap[3][3]);
            tm.getTile(28, 6).setBackground(imgMap[3][0]);
            tm.getTile(23, 7).setBackground(imgMap[3][2]);
            tm.getTile(24, 7).setBackground(imgMap[3][3]);
            tm.getTile(25, 7).setBackground(imgMap[3][3]);
            tm.getTile(26, 7).setBackground(imgMap[3][3]);
            tm.getTile(27, 7).setBackground(imgMap[3][3]);
            tm.getTile(28, 7).setBackground(imgMap[3][0]);
            tm.getTile(23, 8).setBackground(imgMap[3][2]);
            tm.getTile(24, 8).setBackground(imgMap[3][3]);
            tm.getTile(25, 8).setBackground(imgMap[3][3]);
            tm.getTile(26, 8).setBackground(imgMap[3][3]);
            tm.getTile(27, 8).setBackground(imgMap[3][3]);
            tm.getTile(28, 8).setBackground(imgMap[3][0]);
            
            tm.fillBackground(imgMap[0][7], 0, 10, 21, 2); // Sky
            tm.fillBackground(imgMap[0][1], 0, 9, 20, 1); // Roof
            tm.getTile(20, 9).setBackground(imgMap[0][2]);
            tm.fillBackground(imgMap[3][2], 20, 6, 1, 3);
            
            tm.fillBackground(imgMap[3][0], 21, 6, 1, 5);
            tm.getTile(21, 11).setBackground(imgMap[0][0]);
            tm.fillBackground(imgMap[0][1], 22, 11, 9, 1);
            
            tm.getTile(29, 8).setBackground(imgMap[1][1]); // Flag
            tm.getTile(30, 8).setBackground(imgMap[1][2]);
            tm.getTile(29, 7).setBackground(imgMap[2][1]);
            tm.getTile(30, 7).setBackground(imgMap[2][2]);
            
            tm.getTile(1, 8).setBackground(imgMap[1][0]); // Banner
            tm.getTile(1, 7).setBackground(imgMap[2][0]);
            
            tm.getTile(10, 8).setBackground(imgMap[1][0]);
            tm.getTile(10, 7).setBackground(imgMap[2][0]);
            
            tm.getTile(19, 8).setBackground(imgMap[1][0]);
            tm.getTile(19, 7).setBackground(imgMap[2][0]);
            
            tm.fillBackground(imgMap[3][2], 31, 6, 1, 5);
            tm.getTile(31, 11).setBackground(imgMap[0][2]);
            
            tm.fillBackground(imgMap[3][7], 32, 6, 8, 1); // Fence
            tm.fillBackground(imgMap[2][7], 32, 7, 8, 1);
            tm.fillBackground(imgMap[1][7], 32, 8, 8, 1);
            tm.fillBackground(imgMap[0][7], 32, 9, 8, 3);
            
            //new Spawner(room, 65, ShootGame.trooperDefs[0], 5, 1);
            //new Spawner(room, 65, ShootGame.trooperDefs[7], 2, 1);
            new Spawner(room, 65, ShootGame.trooperDefs[6], 1, 1);
            new Spawner(room, 400, ShootGame.bossDefs[0], 1, 1);
            
            final Shooter merchant = new Shooter("MER", room, ShootGame.merchantDef);
            merchant.getPosition().set(224, 88);
            merchant.setMirror(true);
            new Merchant().setShooter(merchant);
            
            new Money(50, 8, 80);
            new Ammo(ShootGame.weaponDefs[2], 32, 80);
            new Ammo(ShootGame.weaponDefs[3], 56, 80);
            new Ammo(ShootGame.weaponDefs[4], 80, 80);
            new Ammo(ShootGame.weaponDefs[5], 104, 80);
            new Health(50, 128, 80);
        }
    }
    
    /*package*/ final static class E1M2 extends Level {
        @Override
        protected final void init() {
            // Add rain effect
            final Panroom room = ShootGame.room;
            final TileMap tm = new TileMap("act.bg", room, 16, 16);
            ShootGame.tm = tm;
            final Pangine engine = Pangine.getEngine();
            final String tmName = "img.bg.city2";
            tm.setImageMap(engine.getImage(tmName, new Callable<Panmage>() { @Override public Panmage call() {
            	return engine.createImage(tmName, createBlueCityImage()); }}));
            final TileMapImage[][] imgMap = tm.splitImageMap();
            tm.fillBackground(imgMap[7][0], 0, 2);
            tm.fillBackground(imgMap[6][0], 2, 1);
            tm.fillBackground(imgMap[5][0], 3, 2);
            tm.fillBackground(imgMap[4][1], 5, 1);
            tm.fillBackground(imgMap[3][1], 6, 6);
            
            tm.getTile(3, 6).setBackground(imgMap[3][2]);
            tm.getTile(4, 6).setBackground(imgMap[7][7]);
            tm.getTile(5, 6).setBackground(imgMap[5][7]);
            tm.getTile(6, 6).setBackground(imgMap[7][7]);
            tm.getTile(7, 6).setBackground(imgMap[3][0]);
            
            tm.fillBackground(imgMap[3][2], 9, 6, 1, 2);
            tm.fillBackground(imgMap[7][7], 10, 6, 1, 2);
            tm.getTile(11, 6).setBackground(imgMap[4][7]);
            tm.getTile(11, 7).setBackground(imgMap[7][7]);
            tm.fillBackground(imgMap[7][7], 12, 6, 1, 2);
            tm.getTile(13, 6).setBackground(imgMap[5][7]);
            tm.getTile(13, 7).setBackground(imgMap[7][7]);
            tm.fillBackground(imgMap[7][7], 14, 6, 1, 2);
            tm.fillBackground(imgMap[3][0], 15, 6, 1, 2);
            
            tm.getTile(16, 6).setBackground(imgMap[3][2]);
            tm.getTile(17, 6).setBackground(imgMap[6][7]);
            tm.getTile(18, 6).setBackground(imgMap[5][7]);
            tm.getTile(19, 6).setBackground(imgMap[3][0]);
        }
    }
}
