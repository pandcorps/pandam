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
package org.pandcorps.pandam.lwjgl;

import java.nio.*;

import org.lwjgl.opengl.*;
import org.pandcorps.pandam.*;

public final class LwjglPangl extends Pangl {
	protected LwjglPangl() {
		super(GL11.GL_NEAREST, GL11.GL_QUADS, GL11.GL_RGBA, GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_UNSIGNED_BYTE);
	}
	
	@Override
	public final void glBindTexture(final int target, final int texture) {
		GL11.glBindTexture(target, texture);
	}
	
	@Override
	public final void glDeleteTextures(final int texture) {
		GL11.glDeleteTextures(texture);
	}
	
	@Override
	public final void glDrawArrays(final int mode, final int first, final int count) {
		GL11.glDrawArrays(mode, first, count);
	}
	
	@Override
	public final void glGenTextures(final IntBuffer textures) {
		GL11.glGenTextures(textures);
	}
	
	@Override
	public final void glLoadIdentity() {
		GL11.glLoadIdentity();
	}
	
	@Override
	public final void glTexCoordPointer(final int size, final int stride, final FloatBuffer pointer) {
		GL11.glTexCoordPointer(size, stride, pointer);
	}
	
	@Override
	public final void glTexImage2D(final int target, final int level, final int internalFormat,
			final int width, final int height, final int border, final int format, final int type, final ByteBuffer pixels) {
		GL11.glTexImage2D(target, level, internalFormat, width, height, border, format, type, pixels);
	}
	
	@Override
	public final void glTexParameteri(final int target, final int pname, final int param) {
		GL11.glTexParameteri(target, pname, param);
	}
	
	@Override
	public final void glVertexPointer(final int size, final int stride, final FloatBuffer pointer) {
		GL11.glVertexPointer(size, stride, pointer);
	}
}
