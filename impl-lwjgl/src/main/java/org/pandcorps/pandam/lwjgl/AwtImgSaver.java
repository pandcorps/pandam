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
package org.pandcorps.pandam.lwjgl;

import java.awt.image.*;
import java.io.File;

import javax.imageio.*;

import org.pandcorps.core.*;
import org.pandcorps.core.Img.*;

public class AwtImgSaver implements ImgSaver {
    @Override
    public final void save(final Img img, final String location) throws Exception {
        final int w = img.getWidth(), h = img.getHeight();
        ColorModel cm = ColorModel.getRGBdefault();
        BufferedImage raw = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        final ImgFactory f = ImgFactory.getFactory();
        final int[] a = new int[4];
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                final int p = img.getRGB(i, j);
                a[0] = f.getRed(p); a[1] = f.getGreen(p); a[2] = f.getBlue(p); a[3] = f.getAlpha(p);
                raw.setRGB(i, j, cm.getDataElement(a, 0));
            }
        }
        ImageIO.write(raw, "png", new File(location));
    }
}
