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
package org.pandcorps.core.img.scale;

import org.pandcorps.core.*;

public class NearestNeighborScaler extends Scaler {
    
    private final int mag;
    
    public NearestNeighborScaler() {
        this(2);
    }
    
    public NearestNeighborScaler(final int mag) {
        this.mag = mag;
    }
    
    @Override
    public Img scale(final Img in) {
        final int w = in.getWidth(), h = in.getHeight();
        final Img out = Imtil.newImage(w * mag, h * mag);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                final int ine = in.getRGB(x, y), xm = x * mag, ym = y * mag;
                for (int j = 0; j < mag; j++) {
                    final int ymj = ym + j;
                    for (int i = 0; i < mag; i++) {
                        out.setRGB(xm + i, ymj, ine);
                    }
                }
            }
        }
        return out;
    }
    
    public final static void main(final String[] args) {
        try {
            final String name = args[0];
            final NearestNeighborScaler scaler = new NearestNeighborScaler();
            Imtil.save(scaler.scale(Imtil.load(name)), name + ".NearestNeighbor.png");
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
