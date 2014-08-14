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
package org.pandcorps.platform;

import org.pandcorps.core.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.platform.Player.*;

public class Avatar extends PlayerData implements Segmented {
	private final static float DEF_JUMP_COL = 1;
    protected String anm = null;
    protected int eye = -1;
    protected final SimpleColor col = new SimpleColor();
    protected byte jumpMode = -1;
    protected final SimpleColor jumpCol = new SimpleColor();
    protected Clothing clothing = null;
    protected final SimpleColor clothingCol = new SimpleColor();
    
    protected final static class SimpleColor {
    	protected float r = -1; // These should probably be multiples of 0.25
        protected float g = -1;
        protected float b = -1;
        
        protected final void load(final SimpleColor col) {
        	r = col.r;
        	g = col.g;
        	b = col.b;
        }
        
        protected void load(final Segment seg, final int i) {
        	r = seg.getFloat(i, DEF_JUMP_COL);
        	g = seg.getFloat(i + 1, DEF_JUMP_COL);
        	b = seg.getFloat(i + 2, DEF_JUMP_COL);
        }
        
        protected void save(final Segment seg, final int i) {
        	seg.setFloat(i, r);
        	seg.setFloat(i + 1, g);
        	seg.setFloat(i + 2, b);
        }
    }
    
    protected final static class Clothing extends FinName {
        protected final String res;
        private final int cost;
        protected Img[] imgs = null;
        protected Img[] mapImgs = null;
        
        protected Clothing(final String name, final String res, final int cost) {
            super(name);
            this.res = res;
            this.cost = cost;
        }
        
        public final void init() {
            if (imgs != null) {
                return;
            }
            imgs = PlatformGame.loadChrStrip("clothes/" + res + ".png", 32, true);
            mapImgs = PlatformGame.loadChrStrip("clothes/" + res + "Map.png", 32, true);
            Img.setTemporary(false, imgs);
            Img.setTemporary(false, mapImgs);
        }
        
        public final int getCost() {
            return cost;
        }
    }
    
    protected final static Clothing[] clothings = {
        new Clothing("Sleeveless", "AShirt", 1000),
        new Clothing("Short Sleeves", "TShirt", 1500),
        new Clothing("Long Sleeves", "LongShirt", 2000)
    };
    
    protected final static Clothing getClothing(final String name) {
        for (final Clothing c : clothings) {
            if (c.res.equals(name)) {
                return c;
            }
        }
        return null;
    }
    
    public Avatar() {
    }
    
    public Avatar(final Avatar src) {
        load(src);
    }
    
    public void randomize() {
        anm = Mathtil.rand(PlatformGame.getAnimals());
        eye = Mathtil.randi(1, PlatformGame.getNumEyes());
        do {
            col.r = randColor();
            col.g = randColor();
            col.b = randColor();
        } while (col.r == 0 && col.g == 0 && col.b == 0);
        jumpMode = Player.MODE_NORMAL;
        jumpCol.r = jumpCol.g = jumpCol.b = DEF_JUMP_COL;
        clothing = null;
    }
    
    public void load(final Avatar src) {
        setName(src.getName());
        anm = src.anm;
        eye = src.eye;
        col.load(src.col);
        jumpMode = src.jumpMode;
        jumpCol.load(src.jumpCol);
        clothing = src.clothing;
        clothingCol.load(src.clothingCol);
    }
    
    public void load(final Segment seg) {
    	setName(seg.getValue(0));
    	anm = seg.getValue(1);
    	eye = seg.intValue(2);
    	col.load(seg, 3);
    	jumpMode = seg.getByte(6, Player.MODE_NORMAL);
    	jumpCol.load(seg, 7); // 7-9
    	clothing = getClothing(seg.getValue(10));
    	clothingCol.load(seg, 11); // 11-13
    }
    
    @Override
    public void save(final Segment seg) {
        seg.setName(PlatformGame.SEG_AVT);
    	seg.setValue(0, getName());
    	seg.setValue(1, anm);
    	seg.setInt(2, eye);
    	col.save(seg, 3);
    	seg.setInt(6, jumpMode);
    	jumpCol.save(seg, 7);
    	seg.setValue(10, (clothing == null) ? "" : clothing.res);
    	clothingCol.save(seg, 11);
    }
    
    private final static float randColor() {
        return toColor(Mathtil.randi(0, 4));
    }
    
    protected final static float toColor(final int i) {
        return i / 4f;
    }
}
