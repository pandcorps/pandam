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
package org.pandcorps.core.img.scale;

import org.pandcorps.core.*;

public class EagleScaler extends Scaler {
    
    @Override
    public Img scale(final Img in) {
        final int w = in.getWidth(), h = in.getHeight();
        final int w1 = w - 1, h1 = h - 1;
        final Img out = Imtil.newImage(w * 2, h * 2);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                final int xm1 = x > 0 ? x - 1 : x, xp1 = x < w1 ? x + 1 : x;
                final int ym1 = y > 0 ? y - 1 : y, yp1 = y < h1 ? y + 1 : y;
                final int ina = in.getRGB(xm1, ym1);
                final int inb = in.getRGB(x, ym1);
                final int inc = in.getRGB(xp1, ym1);
                final int ind = in.getRGB(xm1, y);
                final int ine = in.getRGB(x, y);
                final int inf = in.getRGB(xp1, y);
                final int ing = in.getRGB(xm1, yp1);
                final int inh = in.getRGB(x, yp1);
                final int ini = in.getRGB(xp1, yp1);
                out.setRGB(x * 2, y * 2, ina == inb && ina == ind ? ina : ine);
                out.setRGB(x * 2 + 1, y * 2, inc == inb && inc == inf ? inc : ine);
                out.setRGB(x * 2, y * 2 + 1, ing == inh && ing == ind ? ing : ine);
                out.setRGB(x * 2 + 1, y * 2 + 1, ini == inh && ini == inf ? ini : ine);
            }
        }
        return out;
    }
    
    public final static void main(final String[] args) {
        try {
            final String name = args[0];
            final EagleScaler scaler = new EagleScaler();
            Imtil.save(scaler.scale(Imtil.load(name)), name + ".Eagle.png");
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
