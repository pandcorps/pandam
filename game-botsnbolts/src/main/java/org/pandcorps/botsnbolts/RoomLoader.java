/*
Copyright (c) 2009-2024, Andrew M. Martin
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
import org.pandcorps.botsnbolts.Boss.*;
import org.pandcorps.botsnbolts.BotsnBoltsGame.*;
import org.pandcorps.botsnbolts.Carrier.*;
import org.pandcorps.botsnbolts.Enemy.*;
import org.pandcorps.botsnbolts.Extra.*;
import org.pandcorps.botsnbolts.Menu.*;
import org.pandcorps.botsnbolts.Player.*;
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
    protected final static class BotEpisode {
        //private final int episodeNumber;
        private final String path;
        private final Map<BotCell, BotRoom> rooms = new HashMap<BotCell, BotRoom>();
        protected final List<BotLevel> levels = new ArrayList<BotLevel>();
        protected final Map<String, BotLevel> levelMap = new HashMap<String, BotLevel>();
        private BotLevel firstLevel = null;
        protected final PlayerImages pi;
        protected final ShootMode[] shootModes;
        protected final JumpMode[] jumpModes;
        
        protected BotEpisode(final int episodeNumber, final PlayerImages pi, final ShootMode[] shootModes, final JumpMode[] jumpModes) {
            //this.episodeNumber = episodeNumber;
            this.path = BotsnBoltsGame.RES + "level" + ((episodeNumber == 1) ? "" : Integer.toString(episodeNumber)) + "/";
            this.pi = pi;
            this.shootModes = shootModes;
            this.jumpModes = jumpModes;
        }
    }
    protected final static List<BotEpisode> episodes = new ArrayList<>();
    protected static BotEpisode episode = null;
    private final static List<Panctor> actors = new ArrayList<Panctor>(); // Cleared when room change starts
    //protected final static List<Panctor> actorsDisplayedDuringChange = new ArrayList<Panctor>(); // Shouldn't need; extend Extra; isVisibleWhileRoomChanging() 
    private final static List<ShootableDoor> doors = new ArrayList<ShootableDoor>();
    protected final static List<TileAnimator> animators = new ArrayList<TileAnimator>();
    protected final static Set<StepHandler> stepHandlers = new HashSet<StepHandler>();
    private final static Map<Character, Tile> tiles = new HashMap<Character, Tile>();
    private final static Map<Character, Tile[][]> patterns = new HashMap<Character, Tile[][]>();
    private final static Map<Character, RoomFunction> functions = new HashMap<Character, RoomFunction>();
    private static Shader shader = null;
    protected static ShootMode shootModeForced = null;
    protected static JumpMode jumpModeForced = null;
    protected static boolean passiveShieldForced = false;
    protected final static Map<String, String> variables = new HashMap<String, String>(); // Room variables
    protected final static Map<String, String> levelVariables = new HashMap<String, String>();
    private static Character alt = null;
    protected static Panroom nextRoom = null;
    protected final static List<BossDoor> bossDoors = new ArrayList<BossDoor>(2);
    protected static BoltDoor boltDoor = null;
    protected final static List<BlockPuzzle> blockPuzzles = new ArrayList<BlockPuzzle>(1);
    protected static List<Integer> hiddenBlockIndices = null;
    protected static List<Integer> hiddenBarrierIndices = null;
    private static boolean conveyorBelt = false;
    protected static int startX = 0;
    protected static int startY = 0;
    protected static int levelVersion = 0;
    protected static BotLevel level = null;
    private static int row = 0;
    protected static int waterLevel = 0;
    private static int waterX = 0;
    private static int waterY = 0;
    private static int waterWidth = -1;
    protected static Pantexture waterTexture = null;
    protected static boolean changing = false;
    
    private static BotRoom room = null;
    protected final static Set<BotRoom> visitedRooms = new HashSet<BotRoom>();
    private static boolean revisiting = false;
    
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
        final String fileName = episode.path + fileId + ".txt";
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
                images[i] = engine.createImage(Pantil.vmid(), BotsnBoltsGame.RES + "bg/" + reps.get(i).getValue() + ".png");
            }
        } catch (final Exception e) {
            throw new Panception("Error loading " + fileName, e);
        } finally {
            Iotil.close(in);
        }
        BotsnBoltsGame.bgTexture = new AnimTexture(frameDuration, images);
    }
    
    private final static void processSegmentFile(final String fileId, final boolean ctxRequired, final TileMap tm) {
        final String fileName = episode.path + room.dir + "/" + fileId + ".txt";
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
        boolean importFound = false;
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
                importFound = true;
            }
        }
        if (ctxRequired && importFound) {
            visitRoom();
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
            } else if ("POW".equals(name)) { // If Power-up path being forced
                pow(seg, in);
            } else if ("PEL".equals(name)) { // Else If Power-up path not being forced
                pel(seg, in);
            } else if ("PND".equals(name)) { // Power-up End
                // POW/PEL will read until PND; nothing to do here
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
            } else if ("IMG".equals(name)) { // Image
                img(seg);
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
            } else if ("AIB".equals(name)) { // AiBoss
                aib(seg);
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
            } else if ("RAL".equals(name)) { // Rail
                ral(seg);
            } else if ("BRR".equals(name)) { // Barrier
                brr(seg);
            } else if ("DOR".equals(name)) { // Door
                dor(seg);
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
        addHiddenTiles();
    }
    
    private final static void visitRoom() {
        revisiting = !visitedRooms.add(room);
        BotsnBoltsGame.runPlayers(new PlayerRunnable() {
            @Override public final void run(final Player player) {
                visitRoom(player);
            }
        });
    }
    
    private final static void visitRoom(final Player p) {
        if (p != null) {
            if (p.startRoom == null) {
                p.startRoomNeeded = true;
            } else if (revisiting) {
                p.startRoomNeeded = false;
            } else if (isAutomaticCheckpoint()) {
                p.startRoomNeeded = true;
            } else if (isFrequentCheckpointEnabled(p)) {
                p.startRoomNeeded = true;
            } else {
                p.startRoomNeeded = false;
            }
        }
    }
    
    protected final static boolean isRevisiting() {
        return revisiting;
    }
    
    private final static boolean isAutomaticCheckpoint() {
        final String roomId = room.roomId;
        return (roomId != null) && roomId.endsWith("BossEntrance");
    }
    
    private final static boolean isFrequentCheckpointEnabled(final Player p) {
        final Profile prf = p.prf;
        return (prf == null) || prf.frequentCheckpoints;
    }
    
    private final static void setStartRoomNeeded(final boolean startRoomNeeded) {
        BotsnBoltsGame.runPlayers(new PlayerRunnable() {
            @Override public final void run(final Player p) {
                p.startRoomNeeded = startRoomNeeded;
            }
        });
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
        setBgColor(seg, 4);
        final Boolean startRoomNeeded = seg.toBoolean(5);
        if (startRoomNeeded != null) {
            setStartRoomNeeded(startRoomNeeded.booleanValue());
        }
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
        if (tm.isBad(tileIndex)) {
            return;
        }
        addShadow(tm, tm.getColumn(tileIndex), tm.getRow(tileIndex) - 1);
    }
    
    protected final static void removeShadow(final TileMap tm, final int x, final int y) {
        if (shader != null) {
            shader.removeShadow(tm, x, y);
        }
    }
    
    protected final static void removeShadowBelow(final TileMap tm, final int tileIndex) {
        if (tm.isBad(tileIndex)) {
            return;
        }
        removeShadow(tm, tm.getColumn(tileIndex), tm.getRow(tileIndex) - 1);
    }
    
    protected final static void addHiddenTiles() {
        if (Coltil.isEmpty(hiddenBlockIndices) && Coltil.isEmpty(hiddenBarrierIndices)) {
            return;
        }
        new HiddenBlockPuzzle(hiddenBlockIndices, hiddenBarrierIndices);
        Coltil.clear(hiddenBlockIndices);
        Coltil.clear(hiddenBarrierIndices);
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
    
    private final static boolean isPowerupPathForcingNeeded() {
        final Profile prf = BotsnBoltsGame.pcs.get(0).prf;
        return !prf.isUpgradeAvailable(level.boltName) // Player doesn't have it yet
                && prf.levelSuggestions; // Player asked for hints and has been forced to play the next level where it's possible to get the power-up
    }
    
    private final static void pow(final Segment seg, final SegmentStream in) throws Exception {
        if (isPowerupPathForcingNeeded()) {
            return; // Process the segments that follow POW normally if path forcing needed
        }
        while (true) { // Skip the segments that follow POW if path forcing not needed (will process segments following PEL instead)
            final Segment skp = in.read();
            final String name = skp.getName();
            if ("PEL".equals(name) || "PND".equals(name)) {
                break;
            }
        }
    }
    
    private final static void pel(final Segment seg, final SegmentStream in) throws Exception {
        if (!isPowerupPathForcingNeeded()) {
            return; // Process the segments that follow PEL normally if path forcing not needed
        }
        while (true) { // Skip the segments that follow PEL if path forcing needed (would have processed segments following POW instead)
            final Segment skp = in.read();
            final String name = skp.getName();
            if ("PND".equals(name)) {
                break;
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
    
    private final static int getTextureDepth(final Piped rec, final int i) {
        final String value = rec.getValue(i);
        if ("bg".equals(value)) {
            return BotsnBoltsGame.DEPTH_BG;
        } else if ("fg".equals(value)) {
            return BotsnBoltsGame.DEPTH_FG;
        } else if ("above".equals(value)) {
            return BotsnBoltsGame.DEPTH_ABOVE;
        }
        return BotsnBoltsGame.DEPTH_TEXTURE + Piped.parseInt(value, 0);
    }
    
    private final static void rpt(final Segment seg) throws Exception {
        final int d = BotsnBoltsGame.DIM;
        final TileMap tm = BotsnBoltsGame.tm;
        final int _x = seg.initInt(0), _y = seg.initInt(1);
        final int x = _x * d, y = _y * d;
        final int _w = seg.getInt(2, tm.getWidth() - _x), _h = seg.getInt(3, tm.getHeight() - _y);
        final int w = _w * d, h = _h * d;
        String src = seg.getValue(4);
        final int offX = seg.initInt(5), offY = seg.initInt(6), z = getTextureDepth(seg, 7);
        final byte b = seg.initByte(8);
        final String altSrc = seg.getValue(9);
        if ((altSrc != null) && (levelVersion > 0)) {
            src = altSrc;
        }
        final Pantexture tex = new Pantexture(getTextureImage(src));
        tex.getPosition().set(x, y, z);
        tex.setSize(w, h);
        tex.setOffset(offX, offY);
        tm.getLayer().addActor(tex);
        if (b != 0) {
            tm.fillBehavior(b, _x, _y, _w, _h);
        }
    }
    
    private final static void img(final Segment seg) {
        final int d = BotsnBoltsGame.DIM;
        final TileMap tm = BotsnBoltsGame.tm;
        final int _x = seg.initInt(0), _y = seg.initInt(1);
        final int x = _x * d, y = _y * d;
        String src = seg.getValue(2);
        final int z = getTextureDepth(seg, 3);
        final byte b = seg.initByte(4);
        final Panmage img = getTextureImage(src);
        final Panple size = img.getSize();
        final int _w = Math.round(size.getX() / d), _h = Math.round(size.getY() / d);
        final Decoration actor = new Decoration();
        actor.setView(img);
        actor.getPosition().set(x, y, z);
        tm.getLayer().addActor(actor);
        if (b != 0) {
            tm.fillBehavior(b, _x, _y, _w, _h);
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
        doors.add(new ShootableDoor(seg.intValue(0), seg.intValue(1), (levelVersion == 0) ? BotsnBoltsGame.doorCyan : BotsnBoltsGame.doorSilver, seg));
    }
    
    private final static void vbr(final Segment seg) {
        doors.add(new ShootableBarrier(seg.intValue(0), seg.intValue(1), (levelVersion == 0) ? BotsnBoltsGame.doorCyan : BotsnBoltsGame.doorSilver, seg));
    }
    
    private final static void blt(final Segment seg) throws Exception {
        final String name = seg.getValue(2);
        addActor(new BoltBox(seg, Profile.getUpgrade(name)));
    }
    
    private final static void dsk(final Segment seg) throws Exception {
        addActor(new DiskBox(seg));
    }
    
    private final static void ext(final Segment seg) throws Exception {
        final Extra extra = (Extra) getActorConstructor(Extra.class, seg.getValue(2)).newInstance(seg);
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
    
    private final static Constructor<? extends Enemy> getEnemyConstructor(final String enemyType) {
        return getEnemyConstructor(Enemy.class, enemyType);
    }
    
    @SuppressWarnings("unchecked")
    private final static Constructor<? extends Enemy> getEnemyConstructor(final Class<?> declaringClass, final String enemyType) {
        return (Constructor<? extends Enemy>) getActorConstructor(declaringClass, enemyType);
    }
    
    private final static Constructor<? extends Panctor> getActorConstructor(final Class<?> declaringClass, final String actorType) {
        return Reftil.getDeclaredClassConstructor(actorTypes, declaringClass, actorType, SEGMENT_TYPES);
    }
    
    private final static void bos(final Segment seg) throws Exception {
        addBoss(newBoss(seg));
    }
    
    private final static void addBoss(final Panctor boss) {
        setStartRoomNeeded(false);
        addActor(boss);
    }
    
    private final static Boss newBoss(final Segment seg) throws Exception {
        return (Boss) getBossConstructor(seg.getValue(2)).newInstance(seg);
    }
    
    protected final static Boss newBoss(final int x, final int y, final String bossClassName) throws Exception {
        final Segment seg = new Segment();
        seg.setInt(0, x);
        seg.setInt(1, y);
        seg.setValue(2, bossClassName);
        return newBoss(seg);
    }
    
    private final static Constructor<? extends Enemy> getBossConstructor(final String enemyType) {
        return getEnemyConstructor(Boss.class, enemyType);
    }
    
    private final static void aib(final Segment seg) throws Exception {
        addBoss(newAiBoss(seg));
    }
    
    private final static AiBoss newAiBoss(final Segment seg) throws Exception {
        return (AiBoss) getActorConstructor(Boss.class, seg.getValue(2)).newInstance(seg);
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
        roomFunction = (T) Reftil.getDeclaredClass(RoomFunction.class, functionType).newInstance();
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
    
    private final static void ral(final Segment seg) {
        final Panctor rail = new Rail(seg);
        getLayer().addActor(rail);
    }
    
    private final static void brr(final Segment seg) {
        final int x = seg.intValue(0), y = seg.intValue(1);
        final String doorType = seg.getValue(2);
        doors.add(new ShootableBarrier(x, y, ShootableDoor.getShootableDoorDefinition(doorType), seg));
    }
    
    private final static void dor(final Segment seg) {
        final int x = seg.intValue(0), y = seg.intValue(1);
        final String doorType = seg.getValue(2);
        if ("Boss".equals(doorType)) {
            bossDoors.add(new BossDoor(x, y, true));
        } else if ("BossLeft".equals(doorType)) {
            bossDoors.add(new BossDoor(x, y, false));
        } else if ("Bolt".equals(doorType)) {
            boltDoor = new BoltDoor(x, y);
        } else {
            doors.add(new ShootableDoor(x, y, ShootableDoor.getShootableDoorDefinition(doorType), seg));
        }
    }
    
    protected final static BossDoor getBossDoorExit() {
        for (final BossDoor bossDoor : bossDoors) {
            if (bossDoor.isLeftToRight()) {
                if (bossDoor.getPosition().getX() < 32) {
                    continue;
                }
            } else {
                if (bossDoor.getPosition().getX() >= (BotsnBoltsGame.tm.getLayer().getSize().getX() - 32)) {
                    continue;
                }
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
    
    protected final static ShootableButton getShootableButton() {
        final Panlayer layer = getLayer();
        if (layer == null) {
            return null;
        }
        for (final Panctor actor : Coltil.unnull(layer.getActors())) {
            if (actor instanceof ShootableButton) {
                return (ShootableButton) actor;
            }
        }
        return null;
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
                if ((door.def == BotsnBoltsGame.doorBlack) && Chartil.isEmpty(door.hintText)) {
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
            if (!(actor instanceof Enemy) || ((Enemy) actor).isAllowed()) {
                layer.addActor(actor);
            }
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
        shootModeForced = null;
        jumpModeForced = null;
        passiveShieldForced = false;
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
    
    protected final static void setShootModeForced(final ShootMode shootModeForced) {
        RoomLoader.shootModeForced = shootModeForced;
        for (final PlayerContext pc : BotsnBoltsGame.pcs) {
            if (pc.player != null) {
                pc.player.setShootMode(shootModeForced);
            }
        }
    }
    
    protected final static void setJumpModeForced(final JumpMode jumpModeForced) {
        RoomLoader.jumpModeForced = jumpModeForced;
        for (final PlayerContext pc : BotsnBoltsGame.pcs) {
            if (pc.player != null) {
                pc.player.setJumpMode(jumpModeForced);
            }
        }
    }
    
    protected final static void loadRooms(final int episodeNumber, final PlayerImages pi, final ShootMode[] shootModes, final JumpMode[] jumpModes) {
        episode = new BotEpisode(episodeNumber, pi, shootModes, jumpModes);
        episodes.add(episode);
        SegmentStream in = null;
        try {
            in = SegmentStream.openLocation(episode.path + "Rooms.txt");
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
                    if (episode.rooms.put(new BotCell(xi, y), room) != null) {
                        in.close();
                        throw new IllegalStateException("Two room cells found at (" + xi + ", " + y + ")");
                    }
                }
            }
            in.close();
            in = SegmentStream.openLocation(episode.path + "Levels.txt");
            loadLevels(in);
        } catch (final Exception e) {
            throw Pantil.toRuntimeException(e);
        } finally {
            Iotil.close(in);
        }
    }
    
    protected final static void loadLevels(final SegmentStream in) throws Exception {
        Segment seg;
        final Pancolor g64 = new FinPancolor(64), g96 = new FinPancolor(96), g128 = Pancolor.DARK_GREY, g160 = new FinPancolor(160), g192 = Pancolor.GREY;
        final PixelFilter greyFilter = new ReplacePixelFilter(
            new FinPancolor( 96,  64,  48), g64,
            new FinPancolor(144,  96,  72), g96,
            new FinPancolor(192, 128,  96), g128,
            new FinPancolor(240, 160, 120), g160,
            new FinPancolor(224, 192, 160), g160,
            new FinPancolor(255, 224, 192), g192,
            new FinPancolor( 56,  64,  72), g64,
            new FinPancolor( 80,  96, 112), g96,
            new FinPancolor(128,  24,   0), g64,
            new FinPancolor(192,  32,   0), g96,
            new FinPancolor(  0,   0, 224), g64,
            new FinPancolor( 80,  80, 255), g96,
            new FinPancolor(  0, 128, 192), g96,
            new FinPancolor(  0, 168, 255), g128,
            new FinPancolor(128, 128,  48), g96,
            new FinPancolor(192, 192,  72), g128,
            new FinPancolor(120,  96, 144), g128,
            new FinPancolor(160, 128, 192), g160,
            new FinPancolor(255, 255,   0), g128,
            new FinPancolor(192, 192,   0), g160,
            new FinPancolor( 96, 176, 255), g160,
            new FinPancolor(160, 208, 255), g192,
            new FinPancolor( 96, 144, 192), g160,
            new FinPancolor(128, 192, 255), g192
        );
        while ((seg = in.readIf("LVL")) != null) {
            episode.levels.add(new BotLevel(seg, greyFilter));
        }
        while ((seg = in.readIf("SCR")) != null) {
            loadScreen(seg);
        }
    }
    
    protected final static BotLevel getFirstLevel() {
        return episode.firstLevel;
    }
    
    protected final static BotLevel getLevel(final String bossName) {
        return episode.levelMap.get(bossName); // Might need to loop through all Episodes in some cases
    }
    
    protected final static boolean isFirstLevelFinished() {
        return getFirstLevel().isFinished();
    }
    
    protected final static Panmage getPortrait(final BotLevel level) {
        return (level == null) ? null : level.portrait;
    }
    
    protected final static BotRoom getStartRoom() {
        final BotRoom room = getRoom(startX, startY);
        if (room == null) {
            throw new IllegalStateException("Could not find room (" + startX + ", " + startY + ")");
        }
        return room;
    }
    
    protected final static BotRoom getRoom(final int x, final int y) {
        return episode.rooms.get(new BotCell(x, y));
    }
    
    protected final static BotRoomCell getAdjacentRoom(final Panctor actor, final int dirX, final int dirY) {
        final int x, y;
        if (dirX < 0) {
            x = room.x - 1;
            y = room.y;
        } else if (dirX > 0) {
            x = room.x + room.w;
            y = room.y;
        } else {
            x = room.x + Math.min(room.w - 1, Math.max(0, Mathtil.floor(actor.getPosition().getX() / BotsnBoltsGame.GAME_W)));
            y = room.y + dirY;
        }
        final BotCell cell = new BotCell(x, y);
        final BotRoom room = episode.rooms.get(cell);
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
    
    private final static void loadScreen(final Segment seg) {
        final String name = seg.getValue(0);
        final StartScreenDefinition def = StartScreenDefinition.valueOf(name);
        def.icons = seg.getIntArray(1);
        def.markers = seg.getIntArray(2);
        def.lines = seg.getIntArray(3);
    }
    
    protected final static class Rail extends Extra {
        private final List<RailSection> sections = new ArrayList<RailSection>();
        
        protected Rail(final Segment seg) {
            final TileMap tm = BotsnBoltsGame.tm;
            final List<Field> vertices = seg.getRepetitions(0);
            final int numVertices = vertices.size(), lastVertex = numVertices - 1;
            Field prevVertex = null, prevPrevVertex = null;
            for (int i = 0; i < numVertices; i++) {
                final Field vertex = vertices.get(i);
                if (prevVertex != null) {
                    final Field nextVertex = (i < lastVertex) ? vertices.get(i + 1) : null;
                    final int startX = prevVertex.intValue(0), startY = prevVertex.intValue(1);
                    final int endX = vertex.intValue(0), endY = vertex.intValue(1);
                    final int lastX = endX - 1;
                    final int deltaY = (endY > startY) ? 1 : ((endY < startY) ? -1 : 0);
                    if ((deltaY != 0) && ((endX - startX) != Math.abs(endY - startY))) {
                        throw new IllegalStateException("Bad rail");
                    }
                    if (prevPrevVertex == null) {
                        final int startCapX = startX - 1;
                        if (!Chr.isAnySolidBehavior(Tile.getBehavior(tm.getTile(startCapX, startY)))) {
                            sections.add(new RailSection(startCapX, startY, 48, 0, false));
                        }
                    }
                    if (nextVertex == null) {
                        if (!Chr.isAnySolidBehavior(Tile.getBehavior(tm.getTile(endX, endY)))) {
                            sections.add(new RailSection(endX, endY, 16, 0, false));
                        }
                    }
                    for (int x = startX, y = startY; x < endX; x++, y += deltaY) {
                        final int iyOff = ((x == startX) || (x == lastX)) ? -16 : 0;
                        if (deltaY == 0) {
                            tm.setBehavior(x, y, BotsnBoltsGame.TILE_RAIL);
                            sections.add(new RailSection(x, y, 0, 32 + iyOff, false));
                        } else if (deltaY > 0) {
                            tm.setBehavior(x, y + 1, BotsnBoltsGame.TILE_UPSLOPE_RAIL);
                            sections.add(new RailSection(x, y, 32, 32, false));
                            sections.add(new RailSection(x, y + 1, 32, 16 + iyOff, false));
                        } else {
                            tm.setBehavior(x, y, BotsnBoltsGame.TILE_DOWNSLOPE_RAIL);
                            sections.add(new RailSection(x, y - 1, 32, 32, true));
                            sections.add(new RailSection(x, y, 32, 16 + iyOff, true));
                        }
                    }
                }
                prevPrevVertex = prevVertex;
                prevVertex = vertex;
            }
        }
        
        @Override
        protected final void renderView(final Panderer renderer) {
            final Panlayer layer = getLayer();
            final Panple pos = getPosition();
            final float x = pos.getX(), y = pos.getY();
            final Panmage image = BotsnBoltsGame.getRail();
            for (final RailSection section : sections) {
                renderer.render(layer, image, x + section.x, y + section.y, BotsnBoltsGame.DEPTH_ABOVE, section.ix, section.iy, 16, 16, 0, section.mirror, false);
            }
        }
        
        @Override
        protected final boolean isVisibleWhileRoomChanging() {
            return true;
        }
    }
    
    private final static class RailSection {
        private final float x;
        private final float y;
        private final float ix;
        private final float iy;
        private final boolean mirror;
        
        private RailSection(final int x, final int y, final int ix, final int iy, final boolean mirror) {
            this.x = x * BotsnBoltsGame.DIM;
            this.y = (y * BotsnBoltsGame.DIM) + 5;
            this.ix = ix;
            this.iy = iy;
            this.mirror = mirror;
        }
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
    
    protected static enum StartScreenDefinition {
        Fortress(new FortressStartScreen());
        
        protected final Panscreen screen;
        protected int[] icons = null;
        protected int[] markers = null;
        protected int[] lines = null;
        
        private StartScreenDefinition(final Panscreen screen) {
            this.screen = screen;
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
        protected final Panmage portraitGrey;
        protected final int version;
        protected final List<String> prerequisites;
        protected final String musicName;
        protected final String bossClassName;
        protected final String bossDisplayName;
        protected final StartScreenDefinition startScreen;
        protected final int startLineSize;
        protected final int endLineSize;
        protected final Boolean portraitMirror;
        protected final String replayPrerequisite;
        protected final String boltName;
        protected final List<String> diskNames;
        protected final List<String> otherBossNames;
        
        protected BotLevel(final Segment seg, final PixelFilter greyFilter) {
            if (episode.firstLevel == null) {
                episode.firstLevel = this;
            }
            name1 = seg.getValue(0);
            name2 = seg.getValue(1);
            selectX = seg.intValue(2);
            selectY = seg.intValue(3);
            levelX = seg.intValue(4);
            levelY = seg.intValue(5);
            String portraitLoc = seg.getValue(6);
            final String fullName = Chartil.unnull(name1) + Chartil.unnull(name2);
            if (Chartil.isEmpty(portraitLoc)) {
                portraitLoc = "boss/" + Chartil.toLowerCase(name1) + Chartil.toLowerCase(name2) + "/" + fullName + "Portrait";
            }
            final String portraitPath = BotsnBoltsGame.RES + portraitLoc + ".png";
            final PlayerImages pi = BotsnBoltsGame.playerImages.get(portraitPath);
            if (pi == null) {
                final Img portraitImg = Imtil.load(portraitPath);
                portraitImg.setTemporary(false);
                final Pangine engine = Pangine.getEngine();
                portrait = engine.createImage(portraitLoc, portraitImg);
                Imtil.filterImg(portraitImg, greyFilter);
                portraitGrey = engine.createImage(portraitLoc + "Grey", portraitImg);
                portraitImg.close();
            } else {
                portrait = pi.portrait;
                portraitGrey = null;
            }
            version = seg.getInt(7, 0);
            prerequisites = seg.getValues(8);
            musicName = seg.getValue(9, fullName);
            bossDisplayName = Chartil.isEmpty(name2) ? name1 : (Chartil.isEmpty(name1) ? name2 : (name1 + " " + name2));
            final String startScreenName = seg.getValue(10);
            startScreen = (startScreenName == null) ? null : StartScreenDefinition.valueOf(startScreenName);
            startLineSize = seg.initInt(11);
            endLineSize = seg.initInt(12);
            portraitMirror = seg.toBoolean(13);
            bossClassName = seg.getValue(14, fullName);
            replayPrerequisite = seg.getValue(15);
            episode.levelMap.put(bossClassName, this);
            final boolean pupilNeeded = seg.getBoolean(16, false);
            if (pupilNeeded) {
                Story.pupilNeededSet.add(portrait);
            }
            boltName = seg.getValue(17);
            diskNames = seg.getValues(18);
            otherBossNames = seg.getValues(19);
        }
        
        protected final boolean isSpecialLevel() {
            return Chartil.isValued(replayPrerequisite);
        }
        
        protected final boolean isAllowed() {
            for (final String prerequisite : Coltil.unnull(prerequisites)) {
                if (!Profile.isRequirementMet(prerequisite)) {
                    return false;
                }
            }
            return true;
        }
        
        protected final boolean isReplayable() {
            return Chartil.isEmpty(replayPrerequisite) || BotsnBoltsGame.getPrimaryProfile().disks.contains(replayPrerequisite);
        }
        
        protected final boolean isFinished() {
            return BotsnBoltsGame.getPrimaryProfile().disks.contains(bossClassName);
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
