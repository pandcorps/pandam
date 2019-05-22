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

import java.lang.reflect.Constructor;
import java.util.*;

import org.pandcorps.botsnbolts.BlockPuzzle.*;
import org.pandcorps.botsnbolts.Carrier.*;
import org.pandcorps.botsnbolts.Enemy.*;
import org.pandcorps.botsnbolts.RoomFunction.*;
import org.pandcorps.botsnbolts.ShootableDoor.*;
import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;
import org.pandcorps.pandax.visual.*;

public abstract class RoomLoader {
    private final static int OFF_ALT = 256;
    private final static Class<?>[] SEGMENT_TYPES = { Segment.class };
    private final static Map<BotCell, BotRoom> rooms = new HashMap<BotCell, BotRoom>();
    protected final static List<BotLevel> levels = new ArrayList<BotLevel>();
    private final static List<Panctor> actors = new ArrayList<Panctor>();
    private final static List<ShootableDoor> doors = new ArrayList<ShootableDoor>();
    protected final static List<TileAnimator> animators = new ArrayList<TileAnimator>();
    protected final static Set<StepHandler> stepHandlers = new HashSet<StepHandler>();
    private final static Map<Character, Tile> tiles = new HashMap<Character, Tile>();
    private final static Map<Character, Tile[][]> patterns = new HashMap<Character, Tile[][]>();
    private final static Map<Character, RoomFunction> functions = new HashMap<Character, RoomFunction>();
    private static Shader shader = null;
    protected final static Map<String, String> variables = new HashMap<String, String>(); // Room variables
    protected final static Map<String, String> levelVariables = new HashMap<String, String>();
    private static Character alt = null;
    protected static Panroom nextRoom = null;
    protected final static List<BossDoor> bossDoors = new ArrayList<BossDoor>(2);
    protected static BoltDoor boltDoor = null;
    protected final static List<BlockPuzzle> blockPuzzles = new ArrayList<BlockPuzzle>(1);
    private static boolean conveyorBelt = false;
    protected static int startX = 0;
    protected static int startY = 0;
    protected static int levelVersion = 0;
    private static int row = 0;
    protected static int waterLevel = 0;
    private static int waterX = 0;
    private static int waterY = 0;
    private static int waterWidth = -1;
    protected static Pantexture waterTexture = null;
    protected static boolean changing = false;
    
    private static BotRoom room = null;
    
    protected final static void setRoom(final BotRoom room) {
        RoomLoader.room = room;
    }
    
    protected final static BotRoom getRoom() {
        return room;
    }
    
    protected abstract Panroom newRoom();
    
    /*
    TODO
    Validation
    no ladders between left side of one long room and right side of another
    */
    protected final static class ScriptRoomLoader extends RoomLoader {
        @Override
        protected final Panroom newRoom() {
            BotsnBoltsGame.prevTileSize = BotsnBoltsGame.tileSize;
            BotsnBoltsGame.tileSize = room.tileSize;
            nextRoom = BotsnBoltsGame.BotsnBoltsScreen.newRoom(room.w * BotsnBoltsGame.GAME_W);
            init();
            processSegmentFile(room.roomId, true, BotsnBoltsGame.tm);
            return nextRoom;
        }
    }
    
    protected final static void init() {
        row = BotsnBoltsGame.tm.getHeight() - 1;
    }
    
    protected final static void loadBg(final String fileId) {
        processSegmentFile(fileId, false, BotsnBoltsGame.bgTm);
        clear();
        init();
    }
    
    protected final static void loadTex(final String fileId) {
        final String fileName = BotsnBoltsGame.RES + "level/" + fileId + ".txt";
        SegmentStream in = null;
        final int frameDuration;
        final Panmage images[];
        try {
            in = SegmentStream.openLocation(fileName);
            final Segment tex = in.readRequire("TEX");
            frameDuration = tex.intValue(0);
            final List<Field> reps = tex.getRepetitions(1);
            final int size = reps.size();
            images = new Panmage[size];
            final Pangine engine = Pangine.getEngine();
            for (int i = 0; i < size; i++) {
                images[i] = engine.createImage(Pantil.vmid(), BotsnBoltsGame.RES + "/bg/" + reps.get(i).getValue() + ".png");
            }
        } catch (final Exception e) {
            throw new Panception("Error loading " + fileName, e);
        } finally {
            Iotil.close(in);
        }
        BotsnBoltsGame.bgTexture = new AnimTexture(frameDuration, images);
    }
    
    private final static void processSegmentFile(final String fileId, final boolean ctxRequired, final TileMap tm) {
        final String fileName = BotsnBoltsGame.RES + "level/" + room.dir + "/" + fileId + ".txt";
        SegmentStream in = null;
        try {
            in = SegmentStream.openLocation(fileName);
            processSegments(in, ctxRequired, tm);
        } catch (final Exception e) {
            throw new Panception("Error loading " + fileName, e);
        } finally {
            Iotil.close(in);
        }
    }
    
    private final static void processSegments(final SegmentStream in, final boolean ctxRequired, final TileMap tm) throws Exception {
        Segment seg = in.readIf("CTX"); // Context
        if (seg != null) {
            ctx(seg, in);
        } else {
            final Segment segImp = in.readIf("IMP");
            if (segImp == null) {
                if (ctxRequired) {
                    throw new IllegalStateException("No CTX found");
                }
            } else {
                imp(segImp, ctxRequired, tm); // Import
            }
        }
        while ((seg = in.read()) != null) {
            final String name = seg.getName();
            if ("IMP".equals(name)) { // Import
                imp(seg, false, tm);
            } else if ("ROM".equals(name)) { // Room Information
                rom(seg);
            } else if ("ANM".equals(name)) { // Animator
                anm(seg);
            } else if ("STP".equals(name)) { // Step Handler
                stp(seg);
            } else if ("ALT".equals(name)) { // Alternate Character
                alt(seg);
            } else if ("PUT".equals(name)) { // Put
                put(seg);
            } else if ("PLT".equals(name)) { // Put Alternate
                plt(seg);
            } else if ("PAT".equals(name)) { // Put Pattern
                pat(seg);
            } else if ("PAN".equals(name)) { // Put Animation
                pan(seg);
            } else if ("FNC".equals(name)) { // Function
                fnc(seg);
            } else if ("SHD".equals(name)) { // Shader
                shd(seg);
            } else if ("VAR".equals(name)) { // Variable
                var(seg);
            } else if ("VER".equals(name)) { // Version
                ver(seg, in);
            } else if ("VND".equals(name)) { // Version End
                // VER reads until VND when version doesn't match; nothing to do here if version does match
            } else if ("M".equals(name)) { // Map
                m(seg, tm);
            } else if ("CEL".equals(name)) { // Cell
                cel(seg, tm);
            } else if ("RCT".equals(name)) { // Rectangle
                rct(seg.intValue(0), seg.intValue(1), seg.intValue(2), seg.intValue(3), seg, 4);
            } else if ("ROW".equals(name)) { // Row
                row(seg);
            } else if ("COL".equals(name)) { // Column
                col(seg);
            } else if ("TIL".equals(name)) { // Tile
                til(seg);
            } else if ("RPT".equals(name)) { // Repeating Texture
                rpt(seg);
            } else if ("BOX".equals(name)) { // Power-up Box
                box(seg);
            } else if ("VBX".equals(name)) { // Versioned Box
                vbx(seg);
            } else if ("VDR".equals(name)) { // Versioned Door
                vdr(seg);
            } else if ("VBR".equals(name)) { // Versioned Barrier
                vbr(seg);
            } else if ("BLT".equals(name)) { // Upgrade Bolt Box
                blt(seg);
            } else if ("DSK".equals(name)) { // Disk Box
                dsk(seg);
            } else if ("EXT".equals(name)) { // Extra Actor
                ext(seg);
            } else if ("ENM".equals(name)) { // Enemy
                enm(seg);
            } else if ("BOS".equals(name)) { // Boss
                bos(seg);
            } else if ("SHP".equals(name)) { // Shootable Block Puzzle
                shp(seg);
            } else if ("TMP".equals(name)) { // Timed Block Puzzle
                tmp(seg, in);
            } else if ("HDP".equals(name)) { // Hidden Block Puzzle
                hdp(seg);
            } else if ("SPP".equals(name)) { // Spike Block Puzzle
                spp(seg);
            } else if ("ELB".equals(name)) { // Electricity Block
                elb(seg);
            } else if ("FTB".equals(name)) { // Fire Timed Block
                ftb(seg);
            } else if ("FPB".equals(name)) { // Fire Pressure Block
                fpb(seg);
            } else if ("LDR".equals(name)) { // Ladder
                ldr(seg.intValue(0), seg.intValue(1), seg.intValue(2));
            } else if ("BRR".equals(name)) { // Barrier
                brr(seg.intValue(0), seg.intValue(1), seg.getValue(2));
            } else if ("DOR".equals(name)) { // Door
                dor(seg.intValue(0), seg.intValue(1), seg.getValue(2));
            } else if ("SBT".equals(name)) { // Shootable Button
                sbt(seg, in);
            } else if ("CRR".equals(name)) { // Carrier
                crr(seg);
            } else if ("LFT".equals(name)) { // Lift
                lft(seg);
            } else if ("CNV".equals(name)) { // Conveyor Belt
                cnv(seg);
            } else if ("WTR".equals(name)) { // Water
                wtr(seg);
            } else if ("DEF".equals(name)) { // Definition
            }
        }
        postprocess();
    }
    
    private final static void postprocess() {
        addShadows();
    }
    
    private final static void ctx(Segment seg, final SegmentStream in) throws Exception {
        int version = seg.getInt(3, 0);
        while (version != levelVersion) {
            seg = in.readRequire("CTX");
            version = seg.intValue(3);
        }
        BotsnBoltsGame.BotsnBoltsScreen.loadTileImage(seg.getValue(0), seg.getValue(1));
        setBgColor(seg, 2);
        while (in.readIf("CTX") != null);
    }
    
    private final static void setBgColor(final Segment seg, final int i) {
        final Field field = seg.getField(i);
        if (field != null) {
            Pangine.getEngine().setBgColor(toColor(field));
        }
    }
    
    private final static void imp(final Segment seg, final boolean ctxRequired, final TileMap tm) {
        //TODO Could cache imported files
        processSegmentFile(seg.getValue(0), ctxRequired, tm);
    }
    
    private final static void rom(final Segment seg) {
        final boolean launchReturn = seg.getBoolean(3, false);
        if (launchReturn) {
            final String launchReturnPosX = levelVariables.get(Extra.VAR_LAUNCH_RETURN_POS_X);
            if (launchReturnPosX != null) {
                BotsnBoltsGame.playerStartX = Integer.parseInt(launchReturnPosX);
                BotsnBoltsGame.playerStartY = Integer.parseInt(levelVariables.get(Extra.VAR_LAUNCH_RETURN_POS_Y));
                BotsnBoltsGame.playerStartMirror = Boolean.parseBoolean(levelVariables.get(Extra.VAR_LAUNCH_RETURN_POS_MIRROR));
                return;
            }
        }
        final String playerStartX = seg.getValue(0);
        if (Chartil.isValued(playerStartX)) {
            BotsnBoltsGame.playerStartX = Segment.parseInt(playerStartX) * BotsnBoltsGame.DIM;
        }
        final String playerStartY = seg.getValue(1);
        if (Chartil.isValued(playerStartY)) {
            BotsnBoltsGame.playerStartY = Segment.parseInt(playerStartY) * BotsnBoltsGame.DIM;
        }
        final String playerStartMirror = seg.getValue(2);
        if (Chartil.isValued(playerStartMirror)) {
            BotsnBoltsGame.playerStartMirror = Segment.parseBoolean(playerStartMirror);
        }
        setBgColor(seg, 4);
    }
    
    private final static void anm(final Segment seg) {
        final TileMap tm = BotsnBoltsGame.tm;
        Tile tile = null;
        final byte b = seg.byteValue(0);
        final boolean bg = seg.booleanValue(1);
        final List<Field> fields = seg.getRepetitions(2);
        final int size = fields.size();
        final TileFrame[] frames = new TileFrame[size];
        for (int i = 0; i < size; i++) {
            final Field field = fields.get(i);
            final TileMapImage image = getTileMapImage(field);
            final int duration = field.intValue(LAST_TILE_MAP_IMAGE_FIELD_INDEX + 1);
            final TileMapImage background, foreground;
            if (bg) {
                background = image;
                foreground = null;
            } else {
                background = null;
                foreground = image;
            }
            frames[i] = new TileFrame(background, foreground, duration);
            if (i == 0) {
                tile = tm.getTile(background, foreground, b);
            }
        }
        animators.add(new TileAnimator(tile, frames));
    }
    
    private final static void pan(final Segment seg) {
        final Character key = seg.toCharacter(0);
        final Field f1 = seg.getField(1);
        final byte b = f1.byteValue(0), rm = f1.getByte(1, TileMap.RETRIEVE_ANY);
        final int segSize = seg.size();
        final int anmSize = (segSize - 2) / 3;
        final TileFrame[] frames = new TileFrame[anmSize];
        Tile tile = null;
        for (int i = 2, f = 0; i < segSize; i += 3, f++) {
            final TileMapImage bg = getTileMapImage(seg, i);
            final TileMapImage fg = getTileMapImage(seg, i + 1);
            final int dur = seg.intValue(i + 2);
            frames[f] = new TileFrame(bg, fg, dur);
            if (tile == null) {
                tile = BotsnBoltsGame.tm.getTile(bg, fg, b, rm);
            }
        }
        tiles.put(key, tile);
        animators.add(new TileAnimator(tile, frames));
    }
    
    protected final static TileAnimator getAnimator(final Object img, final boolean bg) {
        for (final TileAnimator animator : animators) {
            for (final TileFrame frame : animator.frames) {
                if (img.equals(bg ? frame.bg : frame.fg)) {
                    return animator;
                }
            }
        }
        return null;
    }
    
    private final static void stp(final Segment seg) throws Exception {
        final String handlerType = seg.getValue(0);
        if (Chartil.isValued(handlerType)) {
            stepHandlers.add(getStepHandler(handlerType));
        } else {
            stepHandlers.clear();
        }
    }
    
    private final static void alt(final Segment seg) {
        alt = seg.toCharacter(0);
    }
    
    private final static void put(final Segment seg) {
        tiles.put(seg.toCharacter(0), getTile(seg, 1));
    }
    
    protected final static Tile getTile(final char c) {
        return tiles.get(Character.valueOf(c));
    }
    
    private final static void plt(final Segment seg) {
        tiles.put(Character.valueOf((char) (seg.charValue(0) + OFF_ALT)), getTile(seg, 1));
    }
    
    private final static void pat(final Segment seg) {
        final Character key = seg.toCharacter(0);
        final int w = seg.intValue(1), h = seg.intValue(2);
        final Tile[][] pattern = new Tile[h][w];
        int tileOffset = 3;
        for (int j = 0; j < h; j++) {
            final Tile[] row = pattern[j];
            for (int i = 0; i < w; i++) {
                row[i] = getTile(seg, tileOffset);
                tileOffset += 3;
            }
        }
        patterns.put(key, pattern);
    }
    
    private final static void fnc(final Segment seg) throws Exception {
        functions.put(seg.toCharacter(0), getRoomFunction(seg.getValue(1)));
    }
    
    private final static void shd(final Segment seg) throws Exception {
        shader = new Shader(getTileMapImage(seg.getField(0)), getTileMapImage(seg.getField(1)), getTileMapImage(seg.getField(2)),
            getTileMapImage(seg.getField(3)), getTileMapImage(seg.getField(4)), getTileMapImage(seg.getField(5)));
    }
    
    private final static void addShadows() {
        if (shader == null) {
            return;
        }
        final TileMap tm = BotsnBoltsGame.tm;
        final int w = tm.getWidth(), h = tm.getHeight();
        for (int y = 1; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (Tile.getBehavior(tm.getTile(x, y)) == BotsnBoltsGame.TILE_BURSTABLE) {
                    shader.addShadow(tm, x, y - 1);
                }
            }
        }
    }
    
    protected final static void addShadow(final TileMap tm, final int x, final int y) {
        if (shader != null) {
            shader.addShadow(tm, x, y);
        }
    }
    
    protected final static void addShadowBelow(final TileMap tm, final int tileIndex) {
        addShadow(tm, tm.getColumn(tileIndex), tm.getRow(tileIndex) - 1);
    }
    
    protected final static void removeShadow(final TileMap tm, final int x, final int y) {
        if (shader != null) {
            shader.removeShadow(tm, x, y);
        }
    }
    
    protected final static void removeShadowBelow(final TileMap tm, final int tileIndex) {
        removeShadow(tm, tm.getColumn(tileIndex), tm.getRow(tileIndex) - 1);
    }
    
    private final static void var(final Segment seg) throws Exception {
        variables.put(seg.getValue(0), seg.getValue(1));
    }
    
    private final static void ver(final Segment seg, final SegmentStream in) throws Exception {
        if (seg.intValue(0) != levelVersion) {
            while (true) {
                final Segment skp = in.read(); // Skip the next Segment if current version is not the desired version
                if ("VND".equals(skp.getName())) {
                    break;
                }
            }
        }
    }
    
    private final static void m(final Segment seg, final TileMap tm) {
        final String value = seg.getValue(0);
        final int size = Chartil.size(value);
        int x = seg.initInt(1);
        final int _y = seg.getInt(2, -1), y = (_y < 0) ? row : _y;
        final boolean fg = seg.getBoolean(3, false);
        for (int i = 0; i < size; i++) {
            try {
                i = cel(value, i, tm, x, y, fg);
            } catch (final Exception e) {
                throw new IllegalStateException("Error setting tiles: " + seg, e);
            }
            x++;
        }
        if (_y < 0) {
            row--;
        }
    }
    
    private final static int cel(final String value, int i, final TileMap tm, final int x, final int row, final boolean fg) {
        if (value != null) {
            final char c = value.charAt(i);
            final char key;
            if ((alt != null) && (alt.charValue() == c)) {
                i++;
                key = (char) (value.charAt(i) + OFF_ALT);
            } else {
                key = c;
            }
            final Character keyW = Character.valueOf(key);
            if (!tiles.containsKey(keyW)) {
                final Tile[][] pattern = patterns.get(keyW);
                if (pattern == null) {
                    final RoomFunction function = functions.get(keyW);
                    if (function != null) {
                        function.build(tm, x, row);
                    }
                } else {
                    int patRow = row;
                    for (final Tile[] tileRow : pattern) {
                        int patX = x;
                        for (final Tile patTile : tileRow) {
                            if (fg) {
                                tm.setForeground(patX, patRow, DynamicTileMap.getRawForeground(patTile), patTile.getBehavior());
                            } else {
                                tm.setTileOptional(patX, patRow, patTile);
                            }
                            patX++;
                        }
                        patRow--;
                    }
                }
            } else {
                final Tile tile = tiles.get(keyW);
                if (fg) {
                    tm.setForeground(x, row, DynamicTileMap.getRawForeground(tile), tile.getBehavior());
                } else {
                    tm.setTile(x, row, tile);
                }
            }
        }
        return i;
    }
    
    private final static void cel(final Segment seg, final TileMap tm) {
        final String value = seg.getValue(2);
        final int i = cel(value, 0, tm, seg.intValue(0), seg.intValue(1), false);
        if (i != Chartil.size(value) - 1) {
            throw new IllegalStateException("Could not process cell " + value);
        }
    }
    
    private final static Tile getTile(final Segment seg, final int tileOffset) {
        final TileMapImage bg = getTileMapImage(seg, tileOffset), fg = getTileMapImage(seg, tileOffset + 1);
        final byte b = seg.getByte(tileOffset + 2, Tile.BEHAVIOR_OPEN);
        return (bg == null && fg == null && b == Tile.BEHAVIOR_OPEN) ? null : BotsnBoltsGame.tm.getTile(bg, fg, b);
    }
    
    private final static void rct(final int x, final int y, final int w, final int h, final Segment seg, final int tileOffset) throws Exception {
        final TileMap tm = BotsnBoltsGame.tm;
        final Tile tile = getTile(seg, tileOffset);
        for (int i = 0; i < w; i++) {
            final int currX = x + i;
            for (int j = 0; j < h; j++) {
                final int currY = y + j;
                tm.setTile(currX, currY, tile);
            }
        }
    }
    
    private final static void row(final Segment seg) throws Exception {
        rct(0, seg.intValue(0), BotsnBoltsGame.tm.getWidth(), 1, seg, 1);
    }
    
    private final static void col(final Segment seg) throws Exception {
        rct(seg.intValue(0), 0, 1, BotsnBoltsGame.tm.getHeight(), seg, 1);
    }
    
    private final static void til(final Segment seg) throws Exception {
        rct(seg.intValue(0), seg.intValue(1), 1, 1, seg, 2);
    }
    
    private final static void rpt(final Segment seg) throws Exception {
        final int d = BotsnBoltsGame.DIM;
        final TileMap tm = BotsnBoltsGame.tm;
        final int _x = seg.initInt(0), _y = seg.initInt(1);
        final int x = _x * d, y = _y * d;
        final int _w = seg.getInt(2, tm.getWidth() - _x);
        final int _h = seg.getInt(3, tm.getHeight() - _y);
        final int w = _w * d, h = _h * d;
        String src = seg.getValue(4);
        final int offX = seg.initInt(5), offY = seg.initInt(6), offZ = seg.initInt(7);
        final byte b = seg.initByte(8);
        final String altSrc = seg.getValue(9);
        if ((altSrc != null) && (levelVersion > 0)) {
            src = altSrc;
        }
        final Pantexture tex = new Pantexture(getTextureImage(src));
        tex.getPosition().set(x, y, BotsnBoltsGame.DEPTH_TEXTURE + offZ);
        tex.setSize(w, h);
        tex.setOffset(offX, offY);
        tm.getLayer().addActor(tex);
        if (b != 0) {
            for (int i = 0; i < _w; i++) {
                final int xi = _x + i;
                for (int j = 0; j < _h; j++) {
                    tm.setBehavior(xi, _y + j, b);
                }
            }
        }
    }
    
    private final static Map<String, Panmage> textures = new HashMap<String, Panmage>();
    
    private final static Panmage getTextureImage(final String name) {
        Panmage img = textures.get(name);
        if (img == null) {
            final String loc = BotsnBoltsGame.RES + "bg/Tex" + name + ".png";
            img = Pangine.getEngine().createImage("tex." + name, loc);
            textures.put(name, img);
        }
        return img;
    }
    
    private final static void box(final Segment seg) {
        addActor(new PowerBox(seg));
    }
    
    private final static void vbx(final Segment seg) {
        if (levelVersion == 0) {
            box(seg);
        } else {
            addActor(new SentryGun(seg));
        }
    }
    
    private final static void vdr(final Segment seg) {
        doors.add(new ShootableDoor(seg.intValue(0), seg.intValue(1), (levelVersion == 0) ? BotsnBoltsGame.doorCyan : BotsnBoltsGame.doorSilver));
    }
    
    private final static void vbr(final Segment seg) {
        doors.add(new ShootableBarrier(seg.intValue(0), seg.intValue(1), (levelVersion == 0) ? BotsnBoltsGame.doorCyan : BotsnBoltsGame.doorSilver));
    }
    
    private final static void blt(final Segment seg) throws Exception {
        final String name = seg.getValue(2);
        addActor(new BoltBox(seg, Profile.getUpgrade(name)));
    }
    
    private final static void dsk(final Segment seg) throws Exception {
        addActor(new DiskBox(seg));
    }
    
    private final static void ext(final Segment seg) throws Exception {
        final Extra extra = (Extra) getActorConstructor(Extra.class.getDeclaredClasses(), seg.getValue(2)).newInstance(seg);
        if (!extra.isAllowed()) {
            return;
        } else if (extra.isVisibleWhileRoomChanging()) {
            getLayer().addActor(extra);
        } else {
            addActor(extra);
        }
    }
    
    private final static void enm(final Segment seg) throws Exception {
        addActor(getEnemyConstructor(seg.getValue(2)).newInstance(seg));
    }
    
    protected final static void addActor(final Panctor actor) {
        actors.add(actor);
    }
    
    private final static Map<String, Constructor<? extends Panctor>> actorTypes = new HashMap<String, Constructor<? extends Panctor>>();
    
    private final static Constructor<? extends Enemy> getEnemyConstructor(final String enemyType) throws Exception {
        return getEnemyConstructor(Enemy.class.getDeclaredClasses(), enemyType);
    }
    
    @SuppressWarnings("unchecked")
    private final static Constructor<? extends Enemy> getEnemyConstructor(final Class<?>[] classes, final String enemyType) throws Exception {
        return (Constructor<? extends Enemy>) getActorConstructor(classes, enemyType);
    }
    
    @SuppressWarnings("unchecked")
    private final static Constructor<? extends Panctor> getActorConstructor(final Class<?>[] classes, final String actorType) throws Exception {
        Constructor<? extends Panctor> constructor = actorTypes.get(actorType);
        if (constructor != null) {
            return constructor;
        }
        final Class<?> c = getDeclaredClass(classes, actorType);
        constructor = (Constructor<? extends Panctor>) c.getDeclaredConstructor(SEGMENT_TYPES);
        actorTypes.put(actorType, constructor);
        return constructor;
    }
    
    private final static Class<?> getDeclaredClass(final Class<?>[] classes, final String actorType) {
        for (final Class<?> c : classes) {
            final String name = c.getName();
            if (name.endsWith(actorType) && name.charAt(name.length() - actorType.length() - 1) == '$') {
                return c;
            }
        }
        throw new IllegalArgumentException("Unrecognized actorType " + actorType);
    }
    
    private final static void bos(final Segment seg) throws Exception {
        addActor(getBossConstructor(seg.getValue(2)).newInstance(seg));
    }
    
    private final static Constructor<? extends Enemy> getBossConstructor(final String enemyType) throws Exception {
        return getEnemyConstructor(Boss.class.getDeclaredClasses(), enemyType);
    }
    
    private final static Map<String, RoomFunction> functionTypes = new HashMap<String, RoomFunction>();
    
    private final static RoomFunction getRoomFunction(final String functionType) throws Exception {
        return getRoomFunction(functionTypes, functionType);
    }
    
    @SuppressWarnings("unchecked")
    private final static <T> T getRoomFunction(final Map<String, T> functionTypes, final String functionType) throws Exception {
        T roomFunction = functionTypes.get(functionType);
        if (roomFunction != null) {
            return roomFunction;
        }
        roomFunction = (T) getDeclaredClass(RoomFunction.class.getDeclaredClasses(), functionType).newInstance();
        functionTypes.put(functionType, roomFunction);
        return roomFunction;
    }
    
    private final static Map<String, StepHandler> handlerTypes = new HashMap<String, StepHandler>();
    
    private final static StepHandler getStepHandler(final String handlerType) throws Exception {
        return getRoomFunction(handlerTypes, handlerType);
    }
    
    private final static void shp(final Segment seg) {
        blockPuzzles.add(new ShootableBlockPuzzle(getTileIndexArray(seg, 0), getTileIndexArray(seg, 1)));
    }
    
    private final static int[] getTileIndexArray(final Segment seg, final int fieldIndex) {
        final TileMap tm = BotsnBoltsGame.tm;
        final List<Field> reps = seg.getRepetitions(fieldIndex);
        final int size = Coltil.size(reps);
        final int[] indices = new int[size];
        for (int i = 0; i < size; i++) {
            final Field f = reps.get(i);
            indices[i] = tm.getIndexRequired(f.intValue(0), f.intValue(1));
        }
        return indices;
    }
    
    private final static void tmp(final Segment tmp, final SegmentStream in) throws Exception {
        final List<int[]> steps = new ArrayList<int[]>();
        Segment seg;
        while ((seg = in.readIf("TMS")) != null) {
            steps.add(getTileIndexArray(seg, 0));
        }
        blockPuzzles.add(new TimedBlockPuzzle(tmp, steps));
    }
    
    private final static void hdp(final Segment seg) {
        new HiddenBlockPuzzle(getTileIndexArray(seg, 0), getTileIndexArray(seg, 1));
    }
    
    private final static void spp(final Segment seg) {
        new SpikeBlockPuzzle(getTileIndexArray(seg, 0), getTileIndexArray(seg, 1));
    }
    
    private final static void elb(final Segment seg) {
        new ElectricityBlock(BotsnBoltsGame.tm.getIndex(seg.intValue(0), seg.intValue(1)), seg.getInt(2, 0));
    }
    
    private final static void ftb(final Segment seg) {
        new FireTimedBlock(BotsnBoltsGame.tm.getIndex(seg.intValue(0), seg.intValue(1)), seg.getInt(2, 0));
    }
    
    private final static void fpb(final Segment seg) {
        FirePressureBlock.init(BotsnBoltsGame.tm.getIndex(seg.intValue(0), seg.intValue(1)));
    }
    
    private final static ButtonBlockPuzzle btp(final Segment seg) {
        final ButtonBlockPuzzle puzzle = new ButtonBlockPuzzle(getTileIndexArray(seg, 0), getTileIndexArray(seg, 1));
        blockPuzzles.add(puzzle);
        return puzzle;
    }
    
    private final static void ldr(final int x, final int y, final int h) {
        final TileMap tm = BotsnBoltsGame.tm;
        final int end = h - 1;
        final Panmage ladder = BotsnBoltsGame.getLadder();
        for (int j = 0; j < h; j++) {
            final byte b;
            if (j == end) {
                if (j < (tm.getHeight() - 1)) {
                    b = BotsnBoltsGame.TILE_LADDER_TOP;
                } else {
                    b = BotsnBoltsGame.TILE_LADDER;
                }
            } else {
                b = BotsnBoltsGame.TILE_LADDER;
            }
            tm.setForeground(x, y + j, ladder, b);
        }
    }
    
    private final static void brr(final int x, final int y, final String doorType) {
        doors.add(new ShootableBarrier(x, y, ShootableDoor.getShootableDoorDefinition(doorType)));
    }
    
    private final static void dor(final int x, final int y, final String doorType) {
        if ("Boss".equals(doorType)) {
            bossDoors.add(new BossDoor(x, y));
        } else if ("Bolt".equals(doorType)) {
            boltDoor = new BoltDoor(x, y);
        } else {
            doors.add(new ShootableDoor(x, y, ShootableDoor.getShootableDoorDefinition(doorType)));
        }
    }
    
    protected final static BossDoor getBossDoorExit() {
        for (final BossDoor bossDoor : bossDoors) {
            if (bossDoor.getPosition().getX() < 32) {
                continue;
            }
            return bossDoor;
        }
        return null;
    }
    
    protected final static boolean isBossDoorClosing() {
        for (final BossDoor bossDoor : bossDoors) {
            if (bossDoor.isClosing()) {
                return true;
            }
        }
        return false;
    }
    
    protected final static Collection<ShootableDoor> getButtonDoors() {
        final Set<ShootableDoor> set = new HashSet<ShootableDoor>();
        for (final ShootableDoor door : doors) {
            final Integer req = door.def.requiredPower;
            if ((req != null) && (req.intValue() == Projectile.POWER_IMPOSSIBLE)) {
                set.add(door);
            }
        }
        return set;
    }
    
    private final static void sbt(final Segment seg, final SegmentStream in) throws Exception {
        final String handlerType = seg.getValue(2);
        final ShootableButtonHandler handler;
        if ("Door".equals(handlerType)) {
            handler = new DoorShootableButtonHandler();
        } else if ("Block".equals(handlerType)) {
            final ButtonBlockPuzzle puzzle = btp(in.readRequire("BTP"));
            handler = new BlockShootableButtonHandler(puzzle);
        } else {
            throw new IllegalArgumentException("Unexpected ShootableButtonHandler type " + handlerType);
        }
        addActor(new ShootableButton(seg.intValue(0), seg.intValue(1), handler));
    }
    
    private final static void crr(final Segment seg) throws Exception {
        addActor(new Carrier(seg.intValue(0), seg.intValue(1), seg.intValue(2), seg.intValue(3), seg.intValue(4)));
    }
    
    private final static void lft(final Segment seg) throws Exception {
        final int x = seg.intValue(0), y = seg.intValue(1);
        final TileMap tm = BotsnBoltsGame.tm;
        final int h = tm.getHeight();
        addActor(new Lifter(seg.intValue(0), seg.intValue(1)));
        for (int i = 0; i < 2; i++) {
            final int xi = x + i;
            tm.setBehavior(xi, y, Tile.BEHAVIOR_SOLID);
            for (int yj = y + 1; yj < h; yj++) {
                final int index = tm.getIndex(xi, yj);
                if (Tile.getBehavior(tm.getTile(index)) != Tile.BEHAVIOR_OPEN) {
                    break;
                }
                tm.setBehavior(index, BotsnBoltsGame.TILE_LIFT);
            }
        }
    }
    
    private final static void cnv(final Segment seg) throws Exception {
        final int x = seg.intValue(0), y = seg.intValue(1), w = seg.intValue(2), w1 = w - 1;
        final boolean right = seg.booleanValue(3);
        final int dirIndex = right ? 1 : 0;
        final TileMap tm = BotsnBoltsGame.tm;
        for (int i = 0; i < w; i++) {
            final int partIndex;
            if (i == 0) {
                partIndex = 0;
            } else if (i == w1) {
                partIndex = 2;
            } else {
                partIndex = 1;
            }
            tm.setTile(x + i, y, BotsnBoltsGame.conveyorBeltTiles[partIndex][dirIndex]);
        }
        conveyorBelt = true;
    }
    
    private final static void wtr(final Segment seg) throws Exception {
        setWaterTile(seg.intValue(0), false, false, seg.initInt(1), seg.initInt(2), seg.getInt(3, -1), seg.initInt(4));
    }
    
    protected final static void setWaterTile(final int waterTile, final boolean anyOpen, final boolean replaceWholeTile) {
        setWaterTile(waterTile, anyOpen, replaceWholeTile, waterX, waterY, waterWidth, 0);
    }
    
    protected final static void setWaterTile(final int waterTile, final boolean anyOpen, final boolean replaceWholeTile, final int waterX, final int waterY, final int waterWidth, final int offZ) {
        waterLevel = waterTile * BotsnBoltsGame.DIM;
        RoomLoader.waterX = waterX;
        RoomLoader.waterY = waterY;
        RoomLoader.waterWidth = waterWidth;
        if (waterTile <= 0) {
            return;
        }
        final TileMap tm = BotsnBoltsGame.tm;
        final int w = tm.getWidth(), max = Math.min(waterTile, tm.getHeight() - 1);
        final TileMapImage[][] imgMap = BotsnBoltsGame.imgMap;
        if (waterX != -1) {
            if (waterTexture == null) {
                waterTexture = new Pantexture(getTextureImage("Water"));
                waterTexture.getPosition().setZ(BotsnBoltsGame.DEPTH_TEXTURE + offZ);
            }
            tm.getLayer().addActor(waterTexture);
            final int yd = waterY * BotsnBoltsGame.DIM;
            waterTexture.getPosition().set(waterX * BotsnBoltsGame.DIM, yd);
            waterTexture.setSize(((waterWidth < 0) ? w : waterWidth) * BotsnBoltsGame.DIM, waterLevel - yd);
        }
        final Tile tile5 = getWaterTile(imgMap, tm, 5);
        final Tile tile6 = getWaterTile(imgMap, tm, 6);
        for (int j = waterY; j <= max; j++) {
            final Tile tile;
            if (j == waterTile) {
                tile = tile5;
            } else if (j == (waterTile - 1)) {
                tile = tile6;
            } else {
                tile = replaceWholeTile ? tm.getTile(null, null, BotsnBoltsGame.TILE_WATER) : null;
            }
            for (int i = 0; i < w; i++) {
                final int index = tm.getIndex(i, j);
                if (isWaterAllowed(index, anyOpen) && ((j < waterTile) || isOpenAbove(i, j))) {
                    if (tile == null) {
                        if ((i > 1) && (i < (tm.getWidth() - 2)) && Chr.isSolidTile(i, j + 1)) {
                            tm.setTile(index, tile6);
                        } else {
                            tm.setBackground(index, null, BotsnBoltsGame.TILE_WATER);
                        }
                    } else {
                        tm.setTile(index, tile);
                    }
                }
            }
        }
    }
    
    private final static Tile getWaterTile(final TileMapImage[][] imgMap, final TileMap tm, final int imgRow) {
        final TileMapImage tmp = imgMap[imgRow][0];
        final TileAnimator animator = getAnimator(tmp, false);
        return (animator == null) ? tm.getTile(null, tmp, Tile.BEHAVIOR_OPEN) : animator.tile;
    }
    
    private final static boolean isWaterAllowed(final int index, final boolean anyOpen) {
        final Tile tile = BotsnBoltsGame.tm.getTile(index);
        if (anyOpen) {
            return !Chr.isSolidTile(tile);
        }
        return tile == null;
    }
    
    protected final static int getWaterTile() {
        return waterLevel / 16;
    }
    
    protected final static void raiseWaterTile() {
        setWaterTile(getWaterTile() + 1, true, true);
    }
    
    private final static boolean isOpenAbove(final int x, final int y) {
        final int h = BotsnBoltsGame.tm.getHeight();
        for (int j = y + 1; j < h; j++) {
            if (Chr.isSolidTile(x, j)) {
                return false;
            }
        }
        return true;
    }
    
    private final static TileMapImage getTileMapImage(final Segment seg, final int imageOffset) {
        return getTileMapImage(seg.getField(imageOffset));
    }
    
    private final static int LAST_TILE_MAP_IMAGE_FIELD_INDEX = 7;
    
    private final static TileMapImage getTileMapImage(final Field field) {
        if (field == null) {
            return null;
        }
        final int imgX = field.getInt(0, -1);
        if (imgX < 0) {
            return null;
        }
        final TileMapImage img = BotsnBoltsGame.imgMap[field.intValue(1)][imgX];
        final boolean overlay = field.getBoolean(2, false);
        final float offZ = overlay ? BotsnBoltsGame.DEPTH_OVERLAY : 0;
        //final float offZ = field.initFloat(2);
        final int rot = field.initInt(3);
        final boolean mirror = field.getBoolean(4, false);
        final boolean flip = field.getBoolean(5, false);
        final int w = field.initInt(6);
        final int h = field.initInt(LAST_TILE_MAP_IMAGE_FIELD_INDEX);
        if (w != 0) {
            return new MultiTileMapImage(img, offZ, rot, mirror, flip, w, h);
        } else if ((offZ != 0) || (rot != 0) || mirror || flip) {
            return new AdjustedTileMapImage(img, offZ, rot, mirror, flip);
        }
        return img;
    }
    
    private final static Pancolor toColor(final Field fld) {
        return fld == null ? FinPancolor.BLACK : new FinPancolor(fld.shortValue(0), fld.shortValue(1), fld.shortValue(2));
    }
    
    protected final static void onEnemyDefeated(final Enemy enemy) {
        final TileMap tm = BotsnBoltsGame.tm;
        if ((tm == null) || (tm.getLayer() != enemy.getLayer())) {
            return;
        }
        Pangine.getEngine().addTimer(tm, 1, new TimerListener() {
            @Override public final void onTimer(final TimerEvent event) {
                if (tm == BotsnBoltsGame.tm) {
                    checkEnemies();
                }
            }});
    }
    
    protected final static Panlayer getLayer() {
        return BotsnBoltsGame.room;
    }
    
    protected final static Iterable<Panctor> getActors() {
        final Panlayer layer = getLayer();
        return Coltil.unnull(layer == null ? null : layer.getActors());
    }
    
    protected final static void checkEnemies() {
        for (final Panctor actor : getActors()) {
            if (actor instanceof Enemy) {
                return;
            }
        }
        onEnemiesCleared();
    }
    
    protected final static void onEnemiesCleared() {
        for (final Panctor actor : getActors()) {
            if (actor instanceof ShootableDoor) {
                final ShootableDoor door = (ShootableDoor) actor;
                if (door.def == BotsnBoltsGame.doorBlack) {
                    door.setDefinition(BotsnBoltsGame.doorCyan);
                }
            }
        }
    }
    
    protected final static void onChangeStarted() {
        clear();
        BlockPuzzle.onRoomChange();
        changing = true;
    }
    
    protected final static void onChangeFinished() {
        final Panlayer layer = getLayer();
        for (final Panctor actor : actors) {
            layer.addActor(actor);
        }
        for (final ShootableDoor door : doors) {
            door.closeDoor();
        }
        for (final BossDoor bossDoor : bossDoors) {
            bossDoor.close();
        }
        if (boltDoor != null) {
            boltDoor.close();
        }
        for (final BlockPuzzle blockPuzzle : blockPuzzles) {
            blockPuzzle.init();
        }
        for (final StepHandler stepHandler : stepHandlers) {
            stepHandler.init();
        }
        clearChangeFinished();
        changing = false;
    }
    
    protected final static void step() {
        if (Player.isPaused()) {
            return;
        }
        for (final TileAnimator animator : animators) {
            animator.step();
        }
        for (final StepHandler stepHandler : stepHandlers) {
            stepHandler.step();
        }
        if (conveyorBelt) {
            BotsnBoltsGame.stepConveyorBelt();
        }
    }
    
    private final static void clearChangeFinished() {
        actors.clear();
        doors.clear();
    }
    
    protected final static void clear() {
        clearChangeFinished();
        animators.clear();
        for (final StepHandler stepHandler : stepHandlers) {
            stepHandler.finish();
        }
        stepHandlers.clear();
        for (final BlockPuzzle blockPuzzle : blockPuzzles) { // Depends on shader; clear puzzles before nulling shader
            blockPuzzle.clear();
        }
        blockPuzzles.clear();
        tiles.clear();
        patterns.clear();
        functions.clear();
        shader = null;
        variables.clear();
        alt = null;
        bossDoors.clear();
        boltDoor = null;
        conveyorBelt = false;
        waterLevel = 0;
        waterX = 0;
        waterY = 0;
        waterWidth = -1;
        waterTexture = null;
    }
    
    protected final static void activate() {
        for (final BlockPuzzle blockPuzzle : blockPuzzles) {
            blockPuzzle.activate();
        }
    }
    
    protected final static void loadRooms() {
        SegmentStream in = null;
        try {
            in = SegmentStream.openLocation(BotsnBoltsGame.RES + "level/Rooms.txt");
            Segment seg;
            seg = in.readIf("CTX");
            /*
            if (seg != null) {
                startX = seg.intValue(0);
                startY = seg.intValue(1);
            }
            */
            while ((seg = in.read()) != null) {
                final int x = seg.intValue(0), y = seg.intValue(1), w = seg.intValue(2);
                final BotRoom room = new BotRoom(x, y, w, seg.getValue(3), seg.getValue(4), seg.getInt(5, BotsnBoltsGame.DIM));
                for (int i = 0; i < w; i++) {
                    final int xi = x + i;
                    if (rooms.put(new BotCell(xi, y), room) != null) {
                        in.close();
                        throw new IllegalStateException("Two room cells found at (" + xi + ", " + y + ")");
                    }
                }
            }
            in.close();
            in = SegmentStream.openLocation(BotsnBoltsGame.RES + "level/Levels.txt");
            loadLevels(in);
        } catch (final Exception e) {
            throw Pantil.toRuntimeException(e);
        } finally {
            Iotil.close(in);
        }
    }
    
    protected final static void loadLevels(final SegmentStream in) throws Exception {
        Segment seg;
        while ((seg = in.read()) != null) {
            levels.add(new BotLevel(seg));
        }
    }
    
    protected final static BotRoom getStartRoom() {
        final BotRoom room = getRoom(startX, startY);
        if (room == null) {
            throw new IllegalStateException("Could not find room (" + startX + ", " + startY + ")");
        }
        return room;
    }
    
    protected final static BotRoom getRoom(final int x, final int y) {
        return rooms.get(new BotCell(x, y));
    }
    
    protected final static BotRoomCell getAdjacentRoom(final Player player, final int dirX, final int dirY) {
        final int x, y;
        if (dirX < 0) {
            x = room.x - 1;
            y = room.y;
        } else if (dirX > 0) {
            x = room.x + room.w;
            y = room.y;
        } else {
            x = room.x + Math.min(room.w - 1, Math.max(0, Mathtil.floor(player.getPosition().getX() / BotsnBoltsGame.GAME_W)));
            y = room.y + dirY;
        }
        final BotCell cell = new BotCell(x, y);
        final BotRoom room = rooms.get(cell);
        if (room == null) {
            return null;
        }
        return new BotRoomCell(room, cell);
    }
    
    protected final static BotRoom getCurrentRoom() {
        return room;
    }
    
    protected final static void reloadCurrentRoom() {
        loadRoom(room);
    }
    
    protected final static void loadRoom(final BotRoom room) {
        BotsnBoltsGame.BotsnBoltsScreen.loadRoom(room);
    }
    
    protected final static class TileAnimator {
        protected final Tile tile;
        private final int period;
        private final TileFrame[] frames;
        
        protected TileAnimator(final Tile tile, final TileFrame... frames) {
            this.tile = tile;
            this.period = getPeriod(frames);
            this.frames = frames;
        }
        
        protected final static int getPeriod(final TileFrame... frames) {
            int p = 0;
            for (final TileFrame frame : frames) {
                p += frame.duration;
            }
            return p;
        }
        
        protected final void step() {
            final long index = Pangine.getEngine().getClock() % period;
            int limit = 0;
            for (final TileFrame frame : frames) {
                limit += frame.duration;
                if (index < limit) {
                    tile.setBackground(frame.bg);
                    tile.setForeground(frame.fg);
                    break;
                }
            }
        }
    }
    
    protected final static class TileFrame {
        private final Object bg;
        private final Object fg;
        private final int duration;
        
        protected TileFrame(final Object bg, final Object fg, final int duration) {
            this.bg = bg;
            this.fg = fg;
            this.duration = duration;
        }
    }
    
    protected final static class BotLevel {
        protected final String name1;
        protected final String name2;
        protected final int selectX;
        protected final int selectY;
        protected final int levelX;
        protected final int levelY;
        protected final Panmage portrait;
        protected final int version;
        protected final List<String> prerequisites;
        
        protected BotLevel(final Segment seg) {
            name1 = seg.getValue(0);
            name2 = seg.getValue(1);
            selectX = seg.intValue(2);
            selectY = seg.intValue(3);
            levelX = seg.intValue(4);
            levelY = seg.intValue(5);
            String portraitLoc = seg.getValue(6);
            if (Chartil.isEmpty(portraitLoc)) {
                portraitLoc = "boss/" + name1.toLowerCase() + name2.toLowerCase() + "/" + name1 + name2 + "Portrait";
            }
            portrait = Pangine.getEngine().createImage(portraitLoc, BotsnBoltsGame.RES + portraitLoc + ".png");
            version = seg.getInt(7, 0);
            final List<Field> prerequisiteFields = seg.getRepetitions(8);
            final int prerequisitesSize = Coltil.size(prerequisiteFields);
            prerequisites = new ArrayList<String>(prerequisitesSize);
            for (int i = 0; i < prerequisitesSize; i++) {
                prerequisites.add(prerequisiteFields.get(i).getValue());
            }
        }
        
        protected final boolean isAllowed() {
            final Profile prf = BotsnBoltsGame.pc.prf;
            for (final String prerequisite : prerequisites) {
                if (!(prf.disks.contains(prerequisite) || prf.isUpgradeAvailable(Profile.getUpgrade(prerequisite)))) {
                    return false;
                }
            }
            return true;
        }
    }
    
    protected final static class BotRoom {
        protected final int x;
        protected final int y;
        protected final int w;
        protected final String dir;
        protected final String roomId;
        protected final int tileSize;
        
        protected BotRoom(final int x, final int y, final int w, final String dir, final String roomId, final int tileSize) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.dir = dir;
            this.roomId = roomId;
            this.tileSize = tileSize;
        }
        
        @Override
        public final String toString() {
            return roomId;
        }
    }
    
    protected final static class BotCell {
        protected final int x;
        protected final int y;
        
        protected BotCell(final int x, final int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public final int hashCode() {
            return (y * 10000) + x;
        }
        
        @Override
        public final boolean equals(final Object o) {
            if (!(o instanceof BotCell)) {
                return false;
            }
            final BotCell c = (BotCell) o;
            return (x == c.x) && (y == c.y);
        }
    }
    
    protected final static class BotRoomCell {
        protected final BotRoom room;
        protected final BotCell cell;
        
        protected BotRoomCell(final BotRoom room, final BotCell cell) {
            this.room = room;
            this.cell = cell;
        }
    }
}
