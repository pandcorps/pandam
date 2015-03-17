/*
Copyright (c) 2009-2014, Andrew M. Martin
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
package org.pandcorps.pandax.tile;

import java.io.*;
import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.col.*;
import org.pandcorps.core.io.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.tile.Tile.*;

public class TileMap extends Panctor implements Savable {
	private final static String SEG_TMP = "TMP";
    private final static String SEG_ROW = "ROW";
    
    /*package*/ final Tile[] tiles;
    /*package*/ final Map<Integer, TileOccupant> occupants = new HashMap<Integer, TileOccupant>();
    
    private final int w;
    private final int h;
    
    /*package*/ final int tw;
    /*package*/ final int th;
    
    /*package*/ Object occupantDepth = null;
    private Float foregroundDepth = null;
    private Float occupantBaseDepth = null;
    
    /*package*/ Panmage imgMap = null;
    private TileMapImage[][] imgs = null;
    private final Map<Tile, Tile> map = new HashMap<Tile, Tile>();
    private static Tile scratch = null;
    
    protected final ImplPansplay tileDisplay;
    
    public TileMap(final String id, final int w, final int h, final int tw, final int th) {
        super(id);
        tiles = new Tile[w * h];
        this.w = w;
        this.h = h;
        this.tw = tw;
        this.th = th;
        tileDisplay = new ImplPansplay(FinPanple.ORIGIN, FinPanple.ORIGIN, new FinPanple2(tw, th));
    }
    
    public TileMap(final String id, final Panlayer room, final int tw, final int th) {
        this(id, Mathtil.ceil(room.getSize().getX() / tw), Mathtil.ceil(room.getSize().getY() / th), tw, th);
    }
    
    public final boolean isBad(final int i, final int j) {
        return i < 0 || j < 0 || i >= w || j >= h;
    }
    
    public final boolean isBad(final int index) {
        return index < 0 || index >= tiles.length;
    }
    
    public final int getIndex(final int i, final int j) {
        return isBad(i, j) ? -1 : (j * w + i);
    }
    
    public final int getIndexRequired(final int i, final int j) {
        if (isBad(i, j)) {
            throw new IllegalArgumentException("Invalid tile index (" + i + ", " + j + ")");
        }
        return j * w + i;
    }
    
    public final int getRow(final int index) {
        return index / w;
    }
    
    public final int getColumn(final int index) {
        return index % w;
    }
    
    public final Tile getTile(final int i, final int j) {
        return getTile(getIndex(i, j));
    }
    
    public final Tile getTile(final int index) {
        return isBad(index) ? null : tiles[index];
    }
    
    public final void removeTile(final int i, final int j) {
        tiles[getIndexRequired(i, j)] = null;
    }
    
    public final int getContainer(final Panctor act) {
    	return getContainer(act.getPosition());
    }
    
    public final int getContainer(final Panple pos) {
    	return getContainer(pos.getX(), pos.getY());
    }
    
    public final int getContainer(final float x, final float y) {
    	return (x < 0 || y < 0) ? -1 : getIndex((int) x / tw, (int) y / th);
    }
    
    public final int getNeighbor(final int i, final int j, final Direction dir) {
        final int ni = i + (dir == Direction.East ? 1 : dir == Direction.West ? -1 : 0);
        final int nj = j + (dir == Direction.North ? 1 : dir == Direction.South ? -1 : 0);
        return getIndex(ni, nj);
    }
    
    public final int getNeighbor(final int index, final Direction dir) {
        return getNeighbor(getColumn(index), getRow(index), dir);
    }
    
    public final int getRelative(final int i, final int j, final int offX, final int offY) {
        return getIndex(i + offX, j + offY);
    }
    
    public final Panple getPosition(final int i, final int j) {
        final Panple mapPos = getPosition();
        return new FinPanple(mapPos.getX() + i * tw, mapPos.getY() + j * th, mapPos.getZ());
    }
    
    public final Panple getPosition(final int index) {
        return getPosition(getColumn(index), getRow(index));
    }
    
    public final void savePosition(final Panple pos, final int i, final int j) {
        final Panple mapPos = getPosition();
        pos.set(mapPos.getX() + i * tw, mapPos.getY() + j * th, mapPos.getZ());
    }
    
    public final void savePosition(final Panple pos, final int index) {
        savePosition(pos, getColumn(index), getRow(index));
    }
    
    public final TileOccupant getOccupant(final int index) {
        return occupants.get(Integer.valueOf(index));
    }
    
    /*package*/ final void setOccupant(final int index, final TileOccupant occupant) {
        final Integer key = Integer.valueOf(index);
        if (occupant == null) {
            occupants.remove(key);
        } else {
            occupants.put(key, occupant);
        }
    }
    
    public final void fillBehavior(final byte behavior) {
    	for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                setBehavior(i, j, behavior);
            }
        } 
    }
    
    public final void replaceBehavior(final byte orig, final byte replace) {
    	for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
            	final int index = getIndex(i, j);
                final Tile t = getTile(index);
                if (Tile.getBehavior(t) == orig) {
                	setBehavior(index, replace);
                }
            }
        } 
    }
    
    public final void fillBackground(final Object background, final boolean solid) {
        fillBackground(background, 0, 0, w, h, solid);
    }
    
    public final void fillBackground(final Object background, final int x, final int y, final int w, final int h, final boolean solid) {
    	fillBackground(background, x, y, w, h);
    	fillBackground(x, y, w, h, solid);
    }
    
    public final void fillBackground(final int x, final int y, final int w, final int h, final boolean solid) {
    	final int right = x + w, top = y + h;
    	final byte behavior = Tile.getSolidBehavior(solid);
    	for (int i = x; i < right; i++) {
            for (int j = y; j < top; j++) {
                setBehavior(i, j, behavior);
            }
        }
    }
    
    public final void setBehavior(final int i, final int j, final byte behavior) {
    	setBehavior(getIndex(i, j), behavior);
    }
    
    public final void setBehavior(final int index, final byte behavior) {
        final Tile tile = getTile(index);
        final Object background, foreground;
        if (tile == null) {
            background = null;
            foreground = null;
        } else {
            background = tile.background;
            foreground = tile.foreground;
        }
        setTile(index, getTile(background, foreground, behavior));
    }
    
    public final void fillBackground(final Object background) {
    	fillBackground(background, 0, 0, w, h);
    }
    
    public final void fillBackground(final Object background, final int y, final int h) {
    	fillBackground(background, 0, y, w, h);
    }
    
    public final void fillBackground(final Object background, final int x, final int y, final int w, final int h) {
        for (int i = x + w - 1; i >= x; i--) {
            for (int j = y + h - 1; j >= y; j--) {
                setBackground(i, j, background);
            }
        }
    }
    
    public final void setBackground(final int i, final int j, final Object background) {
    	setBackground(getIndex(i, j), background);
    }
    
    public final void setBackground(final int index, final Object background) {
        final Tile tile = getTile(index);
        final Object foreground;
        final byte behavior;
        if (tile == null) {
            foreground = null;
            behavior = Tile.BEHAVIOR_DEFAULT;
        } else {
            foreground = tile.foreground;
            behavior = tile.behavior;
        }
        setTile(index, getTile(background, foreground, behavior));
    }
    
    public final void setBackground(final int i, final int j, final Object background, final byte behavior) {
    	setBackground(getIndex(i, j), background, behavior);
    }
    
    public final void setBackground(final int index, final Object background, final byte behavior) {
        final Tile tile = getTile(index);
        final Object foreground = tile == null ? null : tile.foreground;
        setTile(index, getTile(background, foreground, behavior));
    }
    
    public final void setForeground(final int i, final int j, final Object foreground) {
    	setForeground(getIndex(i, j), foreground);
    }
    
    public final void setForeground(final int index, final Object foreground) {
        final Tile tile = getTile(index);
        final Object background;
        final byte behavior;
        if (tile == null) {
        	background = null;
            behavior = Tile.BEHAVIOR_DEFAULT;
        } else {
        	background = tile.background;
            behavior = tile.behavior;
        }
        setTile(index, getTile(background, foreground, behavior));
    }
    
    public final void setForeground(final int i, final int j, final Object foreground, final byte behavior) {
    	setForeground(getIndex(i, j), foreground, behavior);
    }
    
    public final void setForeground(final int index, final Object foreground, final byte behavior) {
        final Tile tile = getTile(index);
        final Object background = tile == null ? null : tile.background;
        setTile(index, getTile(background, foreground, behavior));
    }
    
    public final void setImages(final int i, final int j, final Object background, final Object foreground) {
    	setImages(getIndex(i, j), background, foreground);
    }
    
    public final void setImages(final int index, final Object background, final Object foreground) {
        final Tile tile = getTile(index);
        final byte behavior = tile == null ? Tile.BEHAVIOR_DEFAULT : tile.behavior;
        setTile(index, getTile(background, foreground, behavior));
    }
    
    private final void rectangle(final boolean bg, final int imX, final int imY, final int tlX, final int tlY, final int w, final int h) {
        for (int j = 0; j < h; j++) {
            final int tlJ = tlY + j, imJ = imY - j;
            for (int i = 0; i < w; i++) {
                setImage(bg, imX + i, imJ, tlX + i, tlJ);
            }
        }
    }
    
    public final void rectangleBackground(final int imX, final int imY, final int tlX, final int tlY, final int w, final int h) {
        rectangle(true, imX, imY, tlX, tlY, w, h);
    }
    
    public final void rectangleForeground(final int imX, final int imY, final int tlX, final int tlY, final int w, final int h) {
        rectangle(false, imX, imY, tlX, tlY, w, h);
    }
    
    private final void setImage(final boolean bg, final int imX, final int imY, final int tlX, final int tlY) {
        final int index = getIndex(tlX, tlY);
        final Tile t = getTile(index);
        final TileMapImage im = splitImageMap()[imY][imX];
        Object background = null, foreground = null;
        byte behavior = Tile.BEHAVIOR_DEFAULT;
        if (t != null) {
            background = t.background;
            foreground = t.foreground;
            behavior = t.behavior;
        }
        if (bg) {
            background = im;
        } else {
            foreground = im;
        }
        setTile(index, getTile(background, foreground, behavior));
    }
    
    public final void setBackground(final int imX, final int imY, final int tlX, final int tlY) {
        setImage(true, imX, imY, tlX, tlY);
    }
    
    public final void setForeground(final int imX, final int imY, final int tlX, final int tlY) {
        setImage(false, imX, imY, tlX, tlY);
    }
    
    public final Tile getTile(final Object background, final Object foreground, final byte behavior) {
        if (scratch == null) {
            scratch = new Tile();
        }
        scratch.background = background;
        scratch.foreground = foreground;
        scratch.behavior = behavior;
        Tile m = map.get(scratch);
        if (m != null) {
            return m;
        }
        m = scratch;
        map.put(m, m);
        scratch = null;
        return m;
    }
    
    public final void setTile(final int x, final int y, final Tile tile) {
        tiles[getIndexRequired(x, y)] = tile;
    }
    
    public final void setTile(final int index, final Tile tile) {
        tiles[index] = tile;
    }
    
    public final void setTile(final int x, final int y, final Object background, final Object foreground, final byte behavior) {
        tiles[getIndexRequired(x, y)] = getTile(background, foreground, behavior);
    }
    
    public final void setTile(final int index, final Object background, final Object foreground, final byte behavior) {
        tiles[index] = getTile(background, foreground, behavior);
    }
    
    public final void randBackground(final Object img, final int y, final int h, final int n) {
        for (int i = 0; i < n; i++) {
            setBackground(Mathtil.randi(0, w - 1), Mathtil.randi(y, y + h - 1), img);
        }
    }
    
    /*public final Tile initTile(final int i, final int j) {
        final int index = getIndex(i, j);
        if (isBad(index)) {
            throw new IllegalArgumentException(i + ", " + j + " is out of bounds");
        }
        final Tile old = tiles[index];
        if (old != null) {
            //throw new IllegalArgumentException(i + ", " + j + " is already initialized");
            return old;
        }
        final Tile tile = new Tile();
        tiles[index] = tile;
        return tile;
    }*/
    
    @Override
    protected void updateView() {       
    }

    @Override
    public Pansplay getCurrentDisplay() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void renderView(final Panderer renderer) {
    	/*
    	TODO
    	DynamicTileMap could call TileListener only for visible tiles too.
    	But listener implementations would not be able to rely on previous image.
    	They would need to be based on the clock so tiles don't get out of synch.
    	*/
    	/*
    	GlPangine.draw(Panlayer) calls camera(Panlayer) which sets the layer's view min/max.
    	Then it calls renderView for each Panctor in the Panlayer.
    	So this can trust the view window.
    	*/
        final Panple pos = getPosition();
        final float x = pos.getX(), y = pos.getY(), z = pos.getZ();
        final float foregroundDepth = getForegroundDepth();
        final Panlayer layer = getLayer();
        final Panple min = layer.getViewMinimum(), max = layer.getViewMaximum();
        final int j0, i0, jh, iw;
        if (layer.isBuffered()) {
        	j0 = 0;
        	i0 = 0;
        	jh = h;
        	iw = w;
        } else {
	        j0 = Math.max(0, (int) ((min.getY() - y) / th));
	        i0 = Math.max(0, (int) ((min.getX() - x) / tw));
	        jh = Math.min(h, 1 + (int) ((max.getY() - y) / th));
	        iw = Math.min(w, 1 + (int) ((max.getX() - x) / tw));
        }
        for (int j = j0; j < jh; j++) {
            final int off = j * w;
            final float yjth = y + j * th;
            for (int i = i0; i < iw; i++) {
                final Tile tile = tiles[off + i];
                if (tile == null) {
                    continue;
                }
                final float xitw = x + (i * tw);
                render(renderer, layer, tile.foreground, xitw, yjth, foregroundDepth);
                render(renderer, layer, tile.background, xitw, yjth, z);
            }
        }
    }
    
    protected final void render(final Panderer renderer, final Panlayer layer, final Object img, final float xitw, final float yjth, final float z) {
    	if (img == null) {
    		return;
    	}
    	final Panmage imgMap;
    	final TileMapImage timg;
    	final Class<?> imgClass = img.getClass();
    	if (imgClass == TileImage.class) {
    		final TileImage t = (TileImage) img;
    		timg = t;
    		imgMap = t.img;
    	} else if (imgClass == TileMapImage.class) {
    		timg = (TileMapImage) img;
    		imgMap = this.imgMap;
    	} else {
    		imgMap = null;
    		timg = null;
    	}
    	if (imgMap == null) {
    		renderer.render(layer, (Panmage) img, xitw, yjth, z);
    	} else {
    		renderer.render(layer, imgMap, xitw, yjth, z, timg.ix, timg.iy, tw, th);
    	}
    }
    
    public void setOccupantDepth(final float occupantDepth) {
        this.occupantDepth = Float.valueOf(occupantDepth);
    }
    
    public void setOccupantDepth(final DepthMode occupantDepth) {
        this.occupantDepth = occupantDepth;
    }
    
    public void setImageMap(final Panmage imgMap) {
    	this.imgMap = imgMap;
    }
    
    public void setImageMap(final TileMap src) {
    	this.imgMap = src.imgMap;
    	this.imgs = src.imgs;
    }
    
    public float getForegroundDepth() {
    	if (foregroundDepth != null) {
    		return foregroundDepth.floatValue();
    	}
        //return Float.MAX_VALUE;
        //return z + 1;
        return getPosition().getZ() + (h * th) + 1;
    }
    
    public final void setForegroundDepth(final float foregroundDepth) {
    	this.foregroundDepth = Float.valueOf(foregroundDepth);
    }
    
    public float getOccupantBaseDepth() {
    	if (occupantBaseDepth != null) {
    		return occupantBaseDepth.floatValue();
    	}
    	return getForegroundDepth() - 1;
    }
    
    public final void setOccupantBaseDepth(final float occupantBaseDepth) {
    	this.occupantBaseDepth = Float.valueOf(occupantBaseDepth);
    }
    
    public final int getWidth() {
        return w;
    }
    
    public final int getHeight() {
        return h;
    }
    
    public final int getTileWidth() {
    	return tw;
    }
    
    public final int getTileHeight() {
    	return th;
    }
    
    public TileMapImage[][] splitImageMap() {
    	if (imgs != null) {
    		return imgs;
    	}
    	final Panple idim = imgMap.getBoundingMaximum(); // or getSize()?
    	final int iw = (int) idim.getX() / tw, ih = (int) idim.getY() / th;
    	final TileMapImage[][] t = new TileMapImage[ih][];
    	for (int j = 0; j < ih; j++) {
    		final TileMapImage[] r = new TileMapImage[iw];
    		t[j] = r;
    		final int jth = j * th;
    		for (int i = 0; i < iw; i++) {
    			r[i] = new TileMapImage(i * tw, jth);
        	}
    	}
    	imgs = t;
    	return t;
    }
    
    public TileMapImage[][] splitImageMap(final Panmage imgMap) {
    	setImageMap(imgMap);
    	return splitImageMap();
    }
    
    public final static <T extends TileMap> T load(final Class<T> c, final SegmentStream in, final Panmage timg) throws IOException {
    	final Segment tmp = in.readRequire(SEG_TMP);
    	final int w = tmp.intValue(0), h = tmp.intValue(1);
    	final int tw = tmp.intValue(2), th = tmp.intValue(3);
    	final T tm = Reftil.newInstance(Reftil.getConstructor(c, String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE),
    			Pantil.vmid(), Integer.valueOf(w), Integer.valueOf(h), Integer.valueOf(tw), Integer.valueOf(th));
    	tm.setImageMap(timg);
		final TileMapImage[][] imgMap = tm.splitImageMap();
    	for (int j = 0; j < h; j++) {
    		final Segment row = in.readRequire(SEG_ROW);
    		final List<Field> list = row.getRepetitions(0);
    		for (int i = 0; i < w; i++) {
    			final Field f = Coltil.get(list, i);
    			if (f == null) {
    				continue;
    			}
    			tm.setTile(i, j, tm.getTile(tm.getImage(imgMap, f, 0), tm.getImage(imgMap, f, 2), f.byteValue(4)));
    		}
    	}
    	return tm;
    }
    
    private final TileMapImage getImage(final TileMapImage[][] imgMap, final Field f, final int i) {
    	final String v = f.getValue(i);
    	if (v == null) {
    		return null;
    	}
    	return imgMap[f.intValue(i + 1)][Field.parseInt(v)];
    }
    
    @Override
    public void save(final Writer out) throws IOException {
    	final Segment tmp = new Segment(SEG_TMP);
    	tmp.setInt(0, w);
    	tmp.setInt(1, h);
    	tmp.setInt(2, tw);
    	tmp.setInt(3, th);
    	tmp.save(out);
    	final Segment row = new Segment(SEG_ROW);
    	final ArrayList<Field> list = new ArrayList<Field>(w);
    	row.setRepetitions(0, list);
        for (int j = 0; j < h; j++) {
        	Iotil.println(out);
            for (int i = 0; i < w; i++) {
            	final Tile t = getTile(i, j);
            	Field f = Coltil.get(list, i);
            	Field.clear(f);
            	if (t != null) {
            		if (f == null) {
            			f = new Field();
            			Coltil.set(list, i, f);
            		}
            		setImage(f, 0, t.background);
            		setImage(f, 2, t.foreground);
            		f.setByte(4, t.behavior);
            	}
            }
            row.save(out);
        }
    }
    
    private void setImage(final Field f, final int i, final Object img) {
    	if (img == null) {
    		return;
    	}
    	// Currently don't support other images here
    	final TileMapImage tmimg = (TileMapImage) img;
    	f.setInt(i, (int) tmimg.ix / tw);
    	f.setInt(i + 1, (int) tmimg.iy / th);
    }
    
    public final void info() {
    	System.out.println("TileMap " + getId());
    	System.out.println("Number of cells: " + tiles.length);
    	int valued = 0;
    	final IdentityHashSet<Tile> set = new IdentityHashSet<Tile>();
    	for (final Tile t : tiles) {
    		if (t != null) {
    			valued++;
    			set.add(t);
    		}
    	}
    	System.out.println("Number of valued cells: " + valued);
    	System.out.println("Number of distinct Tiles: " + set.size());
    	System.out.println("Tile cache size: " + map.size());
    }
}
