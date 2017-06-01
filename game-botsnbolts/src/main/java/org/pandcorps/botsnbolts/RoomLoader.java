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

import java.lang.reflect.Constructor;
import java.util.*;

import org.pandcorps.botsnbolts.BlockPuzzle.*;
import org.pandcorps.botsnbolts.Enemy.*;
import org.pandcorps.botsnbolts.ShootableDoor.*;
import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandax.tile.*;
import org.pandcorps.pandax.tile.Tile.*;

public abstract class RoomLoader {
    private final static int OFF_ALT = 256;
    private final static Map<BotCell, BotRoom> rooms = new HashMap<BotCell, BotRoom>();
    private final static List<Enemy> enemies = new ArrayList<Enemy>();
    private final static List<ShootableDoor> doors = new ArrayList<ShootableDoor>();
    private final static List<TileAnimator> animators = new ArrayList<TileAnimator>();
    private final static Map<Character, Tile> tiles = new HashMap<Character, Tile>();
    private static Character alt = null;
    protected static BossDoor bossDoor = null;
    protected static int startX = 0;
    protected static int startY = 0;
    private static int row = 0;
    
    private static BotRoom room = null;
    
    protected final static void setRoom(final BotRoom room) {
        RoomLoader.room = room;
    }
    
    protected abstract Panroom newRoom();
    
    /*
    TODO
    Validation
    no overlapping cells
    no ladders between left side of one long room and right side of another
    */
    protected final static class ScriptRoomLoader extends RoomLoader {
        @Override
        protected final Panroom newRoom() {
            final Panroom room = BotsnBoltsGame.BotsnBoltsScreen.newRoom(RoomLoader.room.w * BotsnBoltsGame.GAME_W);
            row = BotsnBoltsGame.tm.getHeight() - 1;
            processSegmentFile(RoomLoader.room.roomId, true);
            return room;
        }
    }
    
    private final static void processSegmentFile(final String fileId, final boolean ctxRequired) {
        final String fileName = BotsnBoltsGame.RES + "/level/" + fileId + ".txt";
        SegmentStream in = null;
        try {
            in = SegmentStream.openLocation(fileName);
            processSegments(in, true);
        } catch (final Exception e) {
            throw new Panception("Error loading " + fileName, e);
        } finally {
            Iotil.close(in);
        }
    }
    
    private final static void processSegments(final SegmentStream in, final boolean ctxRequired) throws Exception {
        Segment seg = in.readIf("CTX"); // Context
        if (seg != null) {
            ctx(seg);
        } else {
            imp(in.readRequire("IMP"), true); // Import
        }
        while ((seg = in.read()) != null) {
            final String name = seg.getName();
            if ("IMP".equals(name)) { // Import
                imp(seg, false);
            } else if ("ANM".equals(name)) { // Animator
                anm(seg);
            } else if ("ALT".equals(name)) { // Alternate Character
                alt(seg);
            } else if ("PUT".equals(name)) { // Put
                put(seg);
            } else if ("PLT".equals(name)) { // Put Alternate
                plt(seg);
            } else if ("M".equals(name)) { // Map
                m(seg);
            } else if ("RCT".equals(name)) { // Rectangle
                rct(seg.intValue(0), seg.intValue(1), seg.intValue(2), seg.intValue(3), seg, 4);
            } else if ("ROW".equals(name)) { // Row
                row(seg);
            } else if ("COL".equals(name)) { // Column
                col(seg);
            } else if ("TIL".equals(name)) { // Tile
                til(seg);
            } else if ("BOX".equals(name)) { // Power-up Box
                box(seg.intValue(0), seg.intValue(1));
            } else if ("ENM".equals(name)) { // Enemy
                enm(seg.intValue(0), seg.intValue(1), seg.getValue(2));
            } else if ("BOS".equals(name)) { // Boss
                bos(seg.intValue(0), seg.intValue(1), seg.getValue(2));
            } else if ("SHP".equals(name)) { // Shootable Block Puzzle
                shp(seg);
            } else if ("TMP".equals(name)) { // Timed Block Puzzle
                tmp(in);
            } else if ("HDP".equals(name)) { // Hidden Block Puzzle
                hdp(seg);
            } else if ("SPP".equals(name)) { // Spike Block Puzzle
                spp(seg);
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
            } else if ("DEF".equals(name)) { // Definition
            }
        }
    }
    
    private final static void ctx(final Segment seg) {
        BotsnBoltsGame.BotsnBoltsScreen.loadTileImage(seg.getValue(0));
        //TODO Add bg image
        Pangine.getEngine().setBgColor(toColor(seg.getField(2)));
    }
    
    private final static void imp(final Segment seg, final boolean ctxRequired) {
        //TODO Could cache imported files
        processSegmentFile(seg.getValue(0), ctxRequired);
    }
    
    private final static void anm(final Segment seg) {
        final TileMap tm = BotsnBoltsGame.tm;
        Tile tile = null;
        final byte b = seg.byteValue(0);
        final boolean bg = seg.booleanValue(1);
        final int period = seg.intValue(2);
        final List<Field> fields = seg.getRepetitions(3);
        final int size = fields.size();
        final TileFrame[] frames = new TileFrame[size];
        for (int i = 0; i < size; i++) {
            final Field field = fields.get(i);
            final TileMapImage image = getTileMapImage(field);
            final int duration = field.intValue(6);
            frames[i] = new TileFrame(image, duration);
            if (i == 0) {
                final TileMapImage background, foreground;
                if (bg) {
                    background = image;
                    foreground = null;
                } else {
                    background = null;
                    foreground = image;
                }
                tile = tm.getTile(background, foreground, b);
            }
        }
        animators.add(new TileAnimator(tile, bg, period, frames));
    }
    
    private final static void alt(final Segment seg) {
        alt = seg.toCharacter(0);
    }
    
    private final static void put(final Segment seg) {
        tiles.put(seg.toCharacter(0), getTile(seg, 1));
    }
    
    private final static void plt(final Segment seg) {
        tiles.put(Character.valueOf((char) (seg.charValue(0) + OFF_ALT)), getTile(seg, 1));
    }
    
    private final static void m(final Segment seg) {
        final TileMap tm = BotsnBoltsGame.tm;
        final String value = seg.getValue(0);
        final int size = Chartil.size(value);
        int x = 0;
        for (int i = 0; i < size; i++) {
            final char c = value.charAt(i);
            final char key;
            if ((alt != null) && (alt.charValue() == c)) {
                i++;
                key = (char) (value.charAt(i) + OFF_ALT);
            } else {
                key = c;
            }
            tm.setTile(x, row, tiles.get(Character.valueOf(key)));
            x++;
        }
        row--;
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
    
    private final static void box(final int x, final int y) {
        enemies.add(new PowerBox(x, y));
    }
    
    private final static void enm(final int x, final int y, final String enemyType) throws Exception {
        enemies.add(getEnemyConstructor(enemyType).newInstance(Integer.valueOf(x), Integer.valueOf(y)));
    }
    
    private final static Map<String, Constructor<? extends Enemy>> enemyTypes = new HashMap<String, Constructor<? extends Enemy>>();
    
    private final static Constructor<? extends Enemy> getEnemyConstructor(final String enemyType) throws Exception {
        return getEnemyConstructor(Enemy.class.getDeclaredClasses(), enemyType);
    }
    
    @SuppressWarnings("unchecked")
    private final static Constructor<? extends Enemy> getEnemyConstructor(final Class<?>[] classes, final String enemyType) throws Exception {
        Constructor<? extends Enemy> constructor = enemyTypes.get(enemyType);
        if (constructor != null) {
            return constructor;
        }
        for (final Class<?> c : classes) {
            final String name = c.getName();
            if (name.endsWith(enemyType) && name.charAt(name.length() - enemyType.length() - 1) == '$') {
                constructor = (Constructor<? extends Enemy>) c.getDeclaredConstructor(Integer.TYPE, Integer.TYPE);
                enemyTypes.put(enemyType, constructor);
                return constructor;
            }
        }
        throw new IllegalArgumentException("Unrecognized enemyType " + enemyType);
    }
    
    private final static void bos(final int x, final int y, final String enemyType) throws Exception {
        enemies.add(getBossConstructor(enemyType).newInstance(Integer.valueOf(x), Integer.valueOf(y)));
    }
    
    private final static Constructor<? extends Enemy> getBossConstructor(final String enemyType) throws Exception {
        return getEnemyConstructor(Boss.class.getDeclaredClasses(), enemyType);
    }
    
    private final static void shp(final Segment seg) {
        new ShootableBlockPuzzle(getTileIndexArray(seg, 0), getTileIndexArray(seg, 1));
    }
    
    private final static int[] getTileIndexArray(final Segment seg, final int fieldIndex) {
        final TileMap tm = BotsnBoltsGame.tm;
        final List<Field> reps = seg.getRepetitions(fieldIndex);
        final int size = reps.size();
        final int[] indices = new int[size];
        for (int i = 0; i < size; i++) {
            final Field f = reps.get(i);
            indices[i] = tm.getIndex(f.intValue(0), f.intValue(1));
        }
        return indices;
    }
    
    private final static void tmp(final SegmentStream in) throws Exception {
        final List<int[]> steps = new ArrayList<int[]>();
        Segment seg;
        while ((seg = in.readIf("TMS")) != null) {
            steps.add(getTileIndexArray(seg, 0));
        }
        new TimedBlockPuzzle(steps);
    }
    
    private final static void hdp(final Segment seg) {
        new HiddenBlockPuzzle(getTileIndexArray(seg, 0));
    }
    
    private final static void spp(final Segment seg) {
        new SpikeBlockPuzzle(getTileIndexArray(seg, 0), getTileIndexArray(seg, 1));
    }
    
    private final static ButtonBlockPuzzle btp(final Segment seg) {
        return new ButtonBlockPuzzle(getTileIndexArray(seg, 0), getTileIndexArray(seg, 1));
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
            bossDoor = new BossDoor(x, y);
        } else {
            doors.add(new ShootableDoor(x, y, ShootableDoor.getShootableDoorDefinition(doorType)));
        }
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
        new ShootableButton(seg.intValue(0), seg.intValue(1), handler);
    }
    
    private final static void crr(final Segment seg) throws Exception {
        new Carrier(seg.intValue(0), seg.intValue(1), seg.intValue(2), seg.intValue(3), seg.intValue(4));
    }
    
    private final static TileMapImage getTileMapImage(final Segment seg, final int imageOffset) {
        return getTileMapImage(seg.getField(imageOffset));
    }
    
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
        if ((offZ != 0) || (rot != 0) || mirror || flip) {
            return new AdjustedTileMapImage(img, offZ, rot, mirror, flip);
        }
        return img;
    }
    
    private final static Pancolor toColor(final Field fld) {
        return fld == null ? FinPancolor.BLACK : new FinPancolor(fld.shortValue(0), fld.shortValue(1), fld.shortValue(2));
    }
    
    protected final static void onEnemyDefeated() {
        if (BotsnBoltsGame.tm == null) {
            return;
        }
        Pangine.getEngine().addTimer(BotsnBoltsGame.tm, 1, new TimerListener() {
            @Override public final void onTimer(final TimerEvent event) {
                checkEnemies();
            }});
    }
    
    protected final static Panlayer getLayer() {
        return (BotsnBoltsGame.tm == null) ? null : BotsnBoltsGame.tm.getLayer();
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
    
    protected final static void onChangeFinished() {
        final Panlayer layer = BotsnBoltsGame.tm.getLayer();
        for (final Enemy enemy : enemies) {
            layer.addActor(enemy);
        }
        for (final ShootableDoor door : doors) {
            door.closeDoor();
        }
        clearChangeFinished();
    }
    
    protected final static void step() {
        for (final TileAnimator animator : animators) {
            animator.step();
        }
    }
    
    private final static void clearChangeFinished() {
        enemies.clear();
        doors.clear();
    }
    
    protected final static void clear() {
        clearChangeFinished();
        animators.clear();
        tiles.clear();
        alt = null;
        bossDoor = null;
    }
    
    protected final static void loadRooms() {
        SegmentStream in = null;
        try {
            in = SegmentStream.openLocation(BotsnBoltsGame.RES + "/level/Rooms.txt");
            final Segment ctx = in.readIf("CTX");
            if (ctx != null) {
                startX = ctx.intValue(0);
                startY = ctx.intValue(1);
            }
            Segment seg;
            while ((seg = in.read()) != null) {
                final int x = seg.intValue(0), y = seg.intValue(1), w = seg.intValue(2);
                final BotRoom room = new BotRoom(x, y, w, seg.getValue(3));
                for (int i = 0; i < w; i++) {
                    rooms.put(new BotCell(x + i, y), room);
                }
            }
        } catch (final Exception e) {
            throw Pantil.toRuntimeException(e);
        } finally {
            Iotil.close(in);
        }
    }
    
    protected final static BotRoom getStartRoom() {
        return getRoom(startX, startY);
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
            x = (player.getPosition().getX() < BotsnBoltsGame.GAME_W) ? room.x : (room.x + room.w - 1);
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
        BotsnBoltsGame.BotsnBoltsScreen.loadRoom(room);
    }
    
    protected final static class TileAnimator {
        private final Tile tile;
        private final boolean bg;
        private final int period;
        private final TileFrame[] frames;
        
        protected TileAnimator(final Tile tile, final boolean bg, final int period, final TileFrame[] frames) {
            this.tile = tile;
            this.bg = bg;
            this.period = period;
            this.frames = frames;
        }
        
        protected final void step() {
            final long index = Pangine.getEngine().getClock() % period;
            int limit = 0;
            for (final TileFrame frame : frames) {
                limit += frame.duration;
                if (index < limit) {
                    if (bg) {
                        tile.setBackground(frame.image);
                    } else {
                        tile.setForeground(frame.image);
                    }
                    break;
                }
            }
        }
    }
    
    protected final static class TileFrame {
        private final Object image;
        private final int duration;
        
        protected TileFrame(final Object image, final int duration) {
            this.image = image;
            this.duration = duration;
        }
    }
    
    protected final static class BotRoom {
        protected final int x;
        protected final int y;
        protected final int w;
        protected final String roomId;
        
        protected BotRoom(final int x, final int y, final int w, final String roomId) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.roomId = roomId;
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
    
    protected final static class DemoRoomLoader extends RoomLoader {
        @Override
        protected final Panroom newRoom() {
            return BotsnBoltsGame.BotsnBoltsScreen.newDemoRoom();
        }
    }
}
