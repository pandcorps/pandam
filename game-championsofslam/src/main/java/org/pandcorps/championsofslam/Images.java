/*
Copyright (c) 2009-2020, Andrew M. Martin
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
package org.pandcorps.championsofslam;

import java.util.*;

import org.pandcorps.championsofslam.Champion.*;
import org.pandcorps.core.seg.*;

public class Images {
    protected final static int CELLS_PER_ROW = 32;
    //protected final static int NUM_ROWS = 32;
    protected final static int CELL_DIM = 32;
    //TODO Loader will need a Map; would a List be better after it's done?
    // If they're kept in this Map, will we need indices at all? Maybe don't need ClothingStyle sub-classes anymore
    protected final static Map<String, ChampionFrameComponent> bodyComponents = new HashMap<String, ChampionFrameComponent>();
    protected final static Map<String, ClothingStyle> shirtStyles = new HashMap<String, ClothingStyle>();
    protected final static Map<String, ClothingStyle> pantsStyles = new HashMap<String, ClothingStyle>();
    protected final static Map<String, ClothingStyle> bootsStyles = new HashMap<String, ClothingStyle>();
    protected final static List<ChampionFrameComponent> hairComponents = new ArrayList<ChampionFrameComponent>();
    protected final static List<ChampionFrameComponent> eyesComponents = new ArrayList<ChampionFrameComponent>();
    protected final static List<ChampionFrameComponent> mouthComponents = new ArrayList<ChampionFrameComponent>();
    
    protected final static void load(final SegmentStream in) throws Exception {
        int x = 0, iy = 0;
        Segment seg;
        while ((seg = in.read()) != null) {
            final int ix = x * CELL_DIM;
            final int offX = seg.initInt(0);
            final ChampionFrameComponent component = new ChampionFrameComponent(ix, iy, offX);
            final String name = seg.getName();
            if ("BDY".equals(name)) { // Body
                bodyComponents.put(seg.getValue(1), component);
            } else if ("SHR".equals(name)) { // Shirt
                updateClothingStyle(seg, shirtStyles, component);
            } else if ("PNT".equals(name)) { // Pants
                updateClothingStyle(seg, pantsStyles, component);
            } else if ("BOT".equals(name)) { // Boots
                updateClothingStyle(seg, bootsStyles, component);
            } else if ("HAR".equals(name)) { // Hair
                hairComponents.add(component);
            } else if ("EYE".equals(name)) { // Eyes
                eyesComponents.add(component);
            } else if ("MTH".equals(name)) { // Mouth
                mouthComponents.add(component);
            }
            x++;
            if (x >= CELLS_PER_ROW) {
                x = 0;
                iy += CELL_DIM;
            }
        }
    }
    
    private final static void updateClothingStyle(
            final Segment seg, final Map<String, ClothingStyle> styles, final ChampionFrameComponent component) throws Exception {
        final String styleName = seg.getValue(2), frameName = seg.getValue(1);
        ClothingStyle style = styles.get(styleName);
        if (style == null) {
            style = new ClothingStyle(styleName);
            styles.put(styleName, style);
        }
        style.frames.put(frameName, component);
    }
}
