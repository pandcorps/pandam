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
package org.pandcorps.pandam;

import java.nio.*;

// Pandam OpenGL interface
public abstract class Pangl {
	public final int GL_NEAREST;
	public final int GL_QUADS;
	public final int GL_RGBA;
	public final int GL_TEXTURE_2D;
	public final int GL_TEXTURE_MAG_FILTER;
	public final int GL_TEXTURE_MIN_FILTER;
	public final int GL_UNSIGNED_BYTE;
	
	protected Pangl(final int GL_NEAREST, final int GL_QUADS, final int GL_RGBA, final int GL_TEXTURE_2D, final int GL_TEXTURE_MAG_FILTER, final int GL_TEXTURE_MIN_FILTER, final int GL_UNSIGNED_BYTE) {
		this.GL_NEAREST = GL_NEAREST;
		this.GL_QUADS = GL_QUADS;
		this.GL_RGBA = GL_RGBA;
		this.GL_TEXTURE_2D = GL_TEXTURE_2D;
		this.GL_TEXTURE_MAG_FILTER = GL_TEXTURE_MAG_FILTER;
		this.GL_TEXTURE_MIN_FILTER = GL_TEXTURE_MIN_FILTER;
		this.GL_UNSIGNED_BYTE = GL_UNSIGNED_BYTE;
	}
	
	public abstract void glBindTexture(final int target, final int texture);
	
	public abstract void glDeleteTextures(final int texture);
	
	public abstract void glDrawArrays(final int mode, final int first, final int count);
	
	public abstract void glGenTextures(final IntBuffer textures);
	
	public abstract void glLoadIdentity();
	
	public abstract void glTexCoordPointer(final int size, final int stride, final FloatBuffer pointer);
	
	public abstract void glTexImage2D(final int target, final int level, final int internalFormat,
			final int width, final int height, final int border, final int format, final int type, final ByteBuffer pixels);
	
	public abstract void glTexParameteri(final int target, final int pname, final int param);
	
	public abstract void glVertexPointer(final int size, final int stride, final FloatBuffer pointer);
}
