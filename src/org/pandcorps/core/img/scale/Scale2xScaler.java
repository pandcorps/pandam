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

public class Scale2xScaler extends Scaler {
    
    // http://scale2x.sourceforge.net/download.html
    // you are free to use the algorithm, but please call the effect "Scale2x"
    @Override
    public Img scale(final Img in) {
        final int w = in.getWidth(), h = in.getHeight();
        final int w1 = w - 1, h1 = h - 1;
        final Img out = Imtil.newImage(w * 2, h * 2);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                final int ine = in.getRGB(x, y);
                final int out0, out1, out2, out3;
                // Retrieve same pixels repeatedly
                // Repeated comparisons
                final int inb = y > 0 ? in.getRGB(x, y - 1) : ine;
                final int ind = x > 0 ? in.getRGB(x - 1, y) : ine;
                final int inf = x < w1 ? in.getRGB(x + 1, y) : ine;
                final int inh = y < h1 ? in.getRGB(x, y + 1) : ine;
                if (inb != inh && ind != inf) {
                    out0 = ind == inb ? ind : ine;
                    out1 = inb == inf ? inf : ine;
                    out2 = ind == inh ? ind : ine;
                    out3 = inh == inf ? inf : ine;
                } else {
                    out0 = ine;
                    out1 = ine;
                    out2 = ine;
                    out3 = ine;
                }
                // Repeated multiplication, could use bit shifting
                out.setRGB(x * 2, y * 2, out0);
                out.setRGB(x * 2 + 1, y * 2, out1);
                out.setRGB(x * 2, y * 2 + 1, out2);
                out.setRGB(x * 2 + 1, y * 2 + 1, out3);
            }
        }
        return out;
    }
    
    public final static void main(final String[] args) {
        try {
            final String name = args[0];
            final Scale2xScaler scaler = new Scale2xScaler();
            Imtil.save(scaler.scale(Imtil.load(name)), name + ".Scale2x.png");
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
