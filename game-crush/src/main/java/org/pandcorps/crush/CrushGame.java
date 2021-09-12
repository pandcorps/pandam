/*
Copyright (c) 2009-2021, Andrew M. Martin
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
package org.pandcorps.crush;

import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.crush.Chr.*;
import org.pandcorps.game.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.in.*;
import org.pandcorps.pandax.text.*;

public class CrushGame extends BaseGame {
    protected final static String TITLE = "Crews of Crush City";
    protected final static String VERSION = "0.0.1";
    protected final static String YEAR = "2021";
    protected final static String AUTHOR = "Andrew M. Martin";
    protected final static String COPYRIGHT = "Copyright " + Pantext.CHAR_COPYRIGHT + " " + YEAR;
    
    protected final static String RES = "org/pandcorps/crush/";
    protected final static String RES_CHR = RES + "chr/";
    
    protected final static int DIM = 16;
    protected final static int GAME_COLUMNS = 24;
    protected final static int GAME_ROWS = 14;
    protected final static int GAME_W = GAME_COLUMNS * DIM; // 384
    protected final static int GAME_H = GAME_ROWS * DIM; // 224;
    
    protected final static int CHR_DIM = 32;
    protected final static int BOX_H = 4;
    protected final static Panple chrO = new FinPanple2(13, 1);
    protected final static Panple chrMin = new FinPanple2(-6, 0);
    protected final static Panple chrMax = new FinPanple2(6, BOX_H);
    protected final static Panple chrSize = new FinPanple2(CHR_DIM, CHR_DIM);
    
    protected static Queue<Runnable> loaders = new LinkedList<Runnable>();
    protected static ChrImages chrImages = null;
    protected static Panmage hitBox = null;
    
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
        if (loaders != null) {
            loaders.add(new Runnable() {
                @Override public final void run() {
                    loadResources();
                }});
        }
        Panscreen.set(new LogoScreen(CrushScreen.class, loaders));
    }
    
    private final static void loadResources() {
        final List<Panmage> walkImages = new ArrayList<Panmage>();
        final List<Panmage> attackImages = new ArrayList<Panmage>();
        loadChrImages(walkImages, "Walk01.png");
        loadChrImages(attackImages, "Attack01.png");
        chrImages = newChrImages(walkImages, attackImages, 0);
        hitBox = Pangine.getEngine().createEmptyImage("hit.box", null, null, new FinPanple2(8, BOX_H));
    }
    
    private final static void loadChrImages(final List<Panmage> images, final String loc) {
        final Panmage full = Pangine.getEngine().createImage(loc, RES_CHR + loc);
        final int n = Math.round(full.getSize().getX()) / CHR_DIM;
        for (int y = 0; y < n; y++) {
            final float subY = y * CHR_DIM;
            for (int x = 0; x < n; x++) {
                images.add(new SubPanmage(loc + "." + x + "." + y, chrO, chrMin, chrMax, full, x * CHR_DIM, subY, chrSize));
            }
        }
    }
    
    private final static ChrImages newChrImages(final List<Panmage> walkImages, final List<Panmage> attackImages, final int index) {
        final int walkIndex = index * 4;
        final int attackIndex = index * 16;
        final Panmage still = walkImages.get(walkIndex);
        final Panmage walk2 = walkImages.get(walkIndex + 2);
        final Panmage[] walk = { walkImages.get(walkIndex + 1), walk2, walkImages.get(walkIndex + 3), walk2 };
        final Panmage hurt = attackImages.get(attackIndex);
        final Panmage attack2 = attackImages.get(attackIndex + 1);
        final Panmage[] attack = { attack2, attack2, attackImages.get(attackIndex + 2), attackImages.get(attackIndex + 3), attackImages.get(attackIndex + 4) };
        final Panmage stomp = attackImages.get(attackIndex + 5);
        final Panmage down = attackImages.get(attackIndex + 6);
        final Panmage stomped = attackImages.get(attackIndex + 7);
        final Panmage grab = attackImages.get(attackIndex + 8);
        final Panmage carry2 = walkImages.get(walkIndex + 10);
        final Panmage[] carry = { walkImages.get(walkIndex + 9), carry2, walkImages.get(walkIndex + 11), carry2 };
        return new ChrImages(still, walk, attack, hurt, stomp, grab, carry, down, stomped);
    }
    
    protected final static class CrushScreen extends Panscreen {
        @Override
        protected final void load() throws Exception {
            final Chr chr = new Chr();
            Pangame.getGame().getCurrentRoom().addActor(chr);
            chr.registerPlayer(ControlScheme.getDefaultKeyboard());
        }
    }
    
    public final static void main(final String[] args) {
        try {
            new CrushGame().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
