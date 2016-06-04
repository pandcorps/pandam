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
package org.pandcorps.rpg;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.*;

public class Character extends Guy4 {
    /*package*/ final static FinPanple2 o = new FinPanple2(0, -5);
    private final static FinPancolor col1 = new FinPancolor((short) 140);
    private final static FinPancolor col2 = new FinPancolor((short) 160);
    private final static FinPancolor col3 = new FinPancolor((short) 180);
    private final static FinPancolor col4 = new FinPancolor((short) 200);
    
    protected final static class CharacterLayer {
    	private int i;
    	private Pancolor c1;
    	private Pancolor c2;
    	private Pancolor c3;
    	private Pancolor c4;
    	
    	protected CharacterLayer(final int i, final int r1, final int g1, final int b1, final int r2, final int g2, final int b2, final int r3, final int g3, final int b3, final int r4, final int g4, final int b4) {
    		this.i = i;
    		c1 = new Pancolor(r1, g1, b1);
    		c2 = new Pancolor(r2, g2, b2);
    		c3 = new Pancolor(r3, g3, b3);
    		c4 = new Pancolor(r4, g4, b4);
    	}
    }
    
    protected final static class CharacterDefinition {
    	// gender
    	private CharacterLayer face;
    	private int eyes;
    	private CharacterLayer hair;
    	private CharacterLayer legs;
    	private CharacterLayer feet;
    	private CharacterLayer torso;
    	
    	protected CharacterDefinition(final CharacterLayer face, final int eyes, final CharacterLayer hair, final CharacterLayer legs, final CharacterLayer feet, final CharacterLayer torso) {
    		this.face = face;
    		this.eyes = eyes;
    		this.hair = hair;
    		this.legs = legs;
    		this.feet = feet;
    		this.torso = torso;
    	}
    }
    
    protected Character(final String id, final CharacterDefinition def) {
        super(id);
        setView(getSheet(def));
    }
    
    private final static String fname(final String pre, final int i) {
    	return "org/pandcorps/rpg/res/chr/" + pre + "0" + i + ".png";
    }
    
    private final static Panmage[] getSheet(final CharacterDefinition def) {
        final Img[] body = ImtilX.loadStrip("org/pandcorps/rpg/res/chr/MBody.png");
        final Img[] face = ImtilX.loadStrip(fname("MFace", def.face.i), 8, false);
        final Img[] eyes = ImtilX.loadStrip(fname("Eyes", def.eyes), 8, false);
        final Img[] hair = ImtilX.loadStrip(fname("MHair", def.hair.i), 16, false);
        final Img[] legs = ImtilX.loadStrip(fname("MLegs", def.legs.i), 16, false);
        final Img[] feet = ImtilX.loadStrip(fname("MFeet", def.feet.i), 16, false);
        final Img[] torso = ImtilX.loadStrip(fname("MTorso", def.torso.i), 16, false);
        filter(hair, def.hair);
        filter(legs, def.legs);
        filter(feet, def.feet);
        filter(torso, def.torso);
        final PixelFilter skinFilter = getFilter(def.face);
        final Img eyeSide = eyes.length < 2 ? eyes[0] : eyes[1];
        for (int i = 0; i < 5; i += 4) {
            Imtil.copy(face[0], body[i], 0, 0, 8, 8, 4, 1, Imtil.COPY_FOREGROUND);
            body[i] = Imtil.filter(body[i], skinFilter);
            Imtil.copy(eyes[0], body[i], 0, 0, 8, 4, 4, 5, Imtil.COPY_FOREGROUND);
            Imtil.copy(hair[0], body[i], 0, 0, 16, hair[0].getHeight(), 0, 0, Imtil.COPY_FOREGROUND);
            Imtil.copy(legs[i], body[i], 0, 0, 16, legs[i].getHeight(), 0, 11, Imtil.COPY_FOREGROUND);
            Imtil.copy(feet[i], body[i], 0, 0, 16, feet[i].getHeight(), 0, 12, Imtil.COPY_FOREGROUND);
            Imtil.copy(torso[i], body[i], 0, 0, 16, torso[i].getHeight(), 0, 0, Imtil.COPY_FOREGROUND);
            Imtil.copy(face[1], body[i + 1], 0, 0, 8, 8, 4, 1, Imtil.COPY_FOREGROUND);
            body[i + 1] = Imtil.filter(body[i + 1], skinFilter);
            Imtil.copy(eyeSide, body[i + 1], 0, 0, 8, 4, 5, 5, Imtil.COPY_FOREGROUND);
            Imtil.copy(hair[1], body[i + 1], 0, 0, 16, hair[1].getHeight(), 0, 0, Imtil.COPY_FOREGROUND);
            Imtil.copy(legs[i + 1], body[i + 1], 0, 0, 16, legs[i + 1].getHeight(), 0, 11, Imtil.COPY_FOREGROUND);
            Imtil.copy(feet[i + 1], body[i + 1], 0, 0, 16, feet[i + 1].getHeight(), 0, 12, Imtil.COPY_FOREGROUND);
            Imtil.copy(torso[i + 1], body[i + 1], 0, 0, 16, torso[i + 1].getHeight(), 0, 0, Imtil.COPY_FOREGROUND);
            body[i + 2] = Imtil.filter(body[i + 2], skinFilter);
            Imtil.copy(hair[2], body[i + 2], 0, 0, 16, hair[2].getHeight(), 0, 0, Imtil.COPY_FOREGROUND);
            Imtil.copy(legs[i + 2], body[i + 2], 0, 0, 16, legs[i + 2].getHeight(), 0, 11, Imtil.COPY_FOREGROUND);
            Imtil.copy(feet[i + 2], body[i + 2], 0, 0, 16, feet[i + 2].getHeight(), 0, 12, Imtil.COPY_FOREGROUND);
            Imtil.copy(torso[i + 2], body[i + 2], 0, 0, 16, torso[i + 2].getHeight(), 0, 0, Imtil.COPY_FOREGROUND);
        }
        Imtil.mirror(face[1]);
        Imtil.mirror(eyeSide);
        Imtil.mirror(hair[1]);
        for (int i = 0; i < 5; i += 4) {
            Imtil.copy(face[1], body[i + 3], 0, 0, 8, 8, 4, 1, Imtil.COPY_FOREGROUND);
            body[i + 3] = Imtil.filter(body[i + 3], skinFilter);
            Imtil.copy(eyeSide, body[i + 3], 0, 0, 8, 4, 3, 5, Imtil.COPY_FOREGROUND);
            Imtil.copy(hair[1], body[i + 3], 0, 0, 16, hair[1].getHeight(), 0, 0, Imtil.COPY_FOREGROUND);
            Imtil.copy(legs[i + 3], body[i + 3], 0, 0, 16, legs[i + 3].getHeight(), 0, 11, Imtil.COPY_FOREGROUND);
            Imtil.copy(feet[i + 3], body[i + 3], 0, 0, 16, feet[i + 3].getHeight(), 0, 12, Imtil.COPY_FOREGROUND);
            Imtil.copy(torso[i + 3], body[i + 3], 0, 0, 16, torso[i + 3].getHeight(), 0, 0, Imtil.COPY_FOREGROUND);
        }
        final Panmage[] sheet = new Panmage[8];
        final Pangine engine = Pangine.getEngine();
        for (int i = 0; i < 8; i++) {
            sheet[i] = engine.createImage(Pantil.vmid(), o, null, null, body[i]);
        }
        return sheet;
    }
    
    private final static PixelFilter getFilter(final CharacterLayer layer) {
        final ReplacePixelFilter filter = new ReplacePixelFilter();
        filter.put(col1, layer.c1);
        filter.put(col2, layer.c2);
        filter.put(col3, layer.c3);
        filter.put(col4, layer.c4);
        return filter;
    }
    
    private final static void filter(final Img[] a, final CharacterLayer layer) {
    	final PixelFilter filter = getFilter(layer);
    	final int size = a.length;
        for (int i = 0; i < size; i++) {
        	a[i] = Imtil.filter(a[i], filter);
        }
    }
}
