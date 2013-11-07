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
package org.pandcorps.pandax.tile;

import java.io.*;
import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.io.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.tile.Tile.*;

public class TileMap extends Panctor implements Savable {
	private final static String SEG_TMP = "TMP";
    private final static String SEG_ROW = "ROW";
    
    /*package*/ final Tile[] tiles;
    
    private final int w;
    private final int h;
    
    /*package*/ final int tw;
    /*package*/ final int th;
    
    /*package*/ Object occupantDepth = null;
    private Float foregroundDepth = null;
    private Float occupantBaseDepth = null;
    
    /*package*/ Panmage imgMap = null;
    private TileMapImage[][] imgs = null;
    
    protected final ImplPansplay tileDisplay;
    
    public TileMap(final String id, final int w, final int h, final int tw, final int th) {
        super(id);
        tiles = new Tile[w * h];
        this.w = w;
        this.h = h;
        this.tw = tw;
        this.th = th;
        tileDisplay = new ImplPansplay(FinPanple.ORIGIN, FinPanple.ORIGIN, new FinPanple(tw, th, 0));
    }
    
    public TileMap(final String id, final Panlayer room, final int tw, final int th) {
        this(id, Mathtil.ceil(room.getSize().getX() / tw), Mathtil.ceil(room.getSize().getY() / th), tw, th);
    }
    
    public final boolean isBad(final int i, final int j) {
        return i < 0 || j < 0 || i >= w || j >= h;
    }
    
    private final int getIndex(final int i, final int j) {
        return j * w + i;
    }
    
    public final Tile getTile(final int i, final int j) {
        if (isBad(i, j)) {
            return null;
        }
        return tiles[getIndex(i, j)];
    }
    
    public final void removeTile(final int i, final int j) {
        tiles[getIndex(i, j)] = null;
    }
    
    public final Tile getContainer(final Panctor act) {
    	return getContainer(act.getPosition());
    }
    
    public final Tile getContainer(final Panple pos) {
    	return getContainer(pos.getX(), pos.getY());
    }
    
    public final Tile getContainer(final float x, final float y) {
    	return (x < 0 || y < 0) ? null : getTile((int) x / tw, (int) y / th);
    }
    
    public final void fillBehavior(final byte behavior) {
    	for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                initTile(i, j).setBehavior(behavior);
            }
        } 
    }
    
    public final void replaceBehavior(final byte orig, final byte replace) {
    	for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                final Tile t = initTile(i, j);
                if (t.getBehavior() == orig) {
                	t.setBehavior(replace);
                }
            }
        } 
    }
    
    public final void fillBackground(final Panmage background) {
    	fillBackgroundO(background);
    }
    
    public final void fillBackground(final Panmage background, final int y, final int h) {
    	fillBackgroundO(background, y, h);
    }
    
    public final void fillBackground(final Panmage background, final int x, final int y, final int w, final int h) {
    	fillBackgroundO(background, x, y, w, h);
    }
    
    public final void fillBackground(final TileMapImage background) {
    	fillBackgroundO(background);
    }
    
    public final void fillBackground(final TileMapImage background, final boolean solid) {
        fillBackground(background, 0, 0, w, h, solid);
    }
    
    public final void fillBackground(final TileMapImage background, final int y, final int h) {
    	fillBackgroundO(background, y, h);
    }
    
    public final void fillBackground(final TileMapImage background, final int x, final int y, final int w, final int h) {
    	fillBackgroundO(background, x, y, w, h);
    }
    
    public final void fillBackground(final TileMapImage background, final int x, final int y, final int w, final int h, final boolean solid) {
    	fillBackground(background, x, y, w, h);
    	for (int i = x; i < w; i++) {
            for (int j = y; j < h; j++) {
                getTile(i, j).setSolid(solid);
            }
        }
    }
    
    private final void fillBackgroundO(final Object background) {
    	fillBackgroundO(background, 0, 0, w, h);
    }
    
    private final void fillBackgroundO(final Object background, final int y, final int h) {
    	fillBackgroundO(background, 0, y, w, h);
    }
    
    private final void fillBackgroundO(final Object background, final int x, final int y, final int w, final int h) {
        for (int i = x + w - 1; i >= x; i--) {
            for (int j = y + h - 1; j >= y; j--) {
                initTile(i, j).setBackgroundO(background);
            }
        }
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
        final Tile t = initTile(tlX, tlY);
        final TileMapImage im = splitImageMap()[imY][imX];
        if (bg) {
            t.setBackground(im);
        } else {
            t.setForeground(im);
        }
    }
    
    public final void setBackground(final int imX, final int imY, final int tlX, final int tlY) {
        setImage(true, imX, imY, tlX, tlY);
    }
    
    public final void setForeground(final int imX, final int imY, final int tlX, final int tlY) {
        setImage(false, imX, imY, tlX, tlY);
    }
    
    public final void randBackground(final TileMapImage img, final int y, final int h, final int n) {
        for (int i = 0; i < n; i++) {
            getTile(Mathtil.randi(0, w - 1), Mathtil.randi(y, y + h - 1)).setBackground(img);
        }
    }
    
    public final Tile initTile(final int i, final int j) {
        if (isBad(i, j)) {
            throw new IllegalArgumentException(i + ", " + j + " is out of bounds");
        }
        final int index = getIndex(i, j);
        final Tile old = tiles[index];
        if (old != null) {
            //throw new IllegalArgumentException(i + ", " + j + " is already initialized");
            return old;
        }
        final Tile tile = new Tile(this, i, j);
        tiles[index] = tile;
        return tile;
    }
    
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
    	Only render tiles visible by current camera.
    	Allow some wiggle room if camera can move between call to this and actual rendering.
    	DynamicTileMap could call TileListener only for visible tiles too.
    	But listener implementations would not be able to rely on previous image.
    	They would need to be based on the clock so tiles don't get out of synch.
    	*/
        final Panple pos = getPosition();
        final float x = pos.getX();
        final float y = pos.getY();
        final float z = pos.getZ();
        final float foregroundDepth = getForegroundDepth();
        final Panlayer layer = getLayer();
        for (int j = 0; j < h; j++) {
            final int off = j * w;
            final float yjth = y + j * th;
            for (int i = 0; i < w; i++) {
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
    			tm.initTile(i, j).setImages(tm.getImage(imgMap, f, 0), tm.getImage(imgMap, f, 2), f.byteValue(4));
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
}
