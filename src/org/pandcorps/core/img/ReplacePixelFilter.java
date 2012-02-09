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
package org.pandcorps.core.img;

import java.util.*;

public final class ReplacePixelFilter extends PixelFilter {
	private final HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
	
	private ReplacePixelFilter() { // Can probably be public
	}
	
	public ReplacePixelFilter(final int src, final int dst) {
		put(src, dst);
	}
	
	public ReplacePixelFilter(final short sr, final short sg, final short sb, final short sa, final short dr, final short dg, final short db, final short da) {
		put(sr, sg, sb, sa, dr, dg, db, da);
	}
	
	public ReplacePixelFilter(final int src) {
		putToTransparent(src);
	}
	
	public final void put(final int src, final int dst) {
		map.put(Integer.valueOf(src), Integer.valueOf(dst));
	}
	
	public final void put(final short sr, final short sg, final short sb, final short sa, final short dr, final short dg, final short db, final short da) {
		put(getRgba(sr, sg, sb, sa), getRgba(dr, dg, db, da));
	}
	
	public final void put(final Pancolor src, final Pancolor dst) {
		put(getRgba(src), getRgba(dst));
	}
	
	public final void putToTransparent(final int src) {
		put(src, getRgba(0, 0, 0, 0));
	}
	
	@Override
	public final int filter(final int p) {
		final Integer dst = map.get(Integer.valueOf(p));
		return dst == null ? p : dst.intValue();
	}
	
	private final static ReplacePixelFilter init(final ReplacePixelFilter f) {
		return f == null ? new ReplacePixelFilter() : f;
	}
	
	public final static ReplacePixelFilter put(ReplacePixelFilter f, final Pancolor src, final Pancolor dst) {
		(f = init(f)).put(src, dst);
		return f;
	}
	
	public final static ReplacePixelFilter putToTransparent(ReplacePixelFilter f, final int src) {
		(f = init(f)).putToTransparent(src);
		return f;
	}
	
	public final static ReplacePixelFilter putIfValued(final ReplacePixelFilter f, final Pancolor src, final Pancolor dst) {
		return src == null ? f : put(f, src, dst);
	}
}
