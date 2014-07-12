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
	public final int GL_ALPHA_TEST;
	public final int GL_ARRAY_BUFFER;
	public final int GL_ARRAY_BUFFER_BINDING;
	public final int GL_BLEND;
	public final int GL_COLOR_BUFFER_BIT;
	public final int GL_DEPTH_BUFFER_BIT;
	public final int GL_DEPTH_TEST;
	public final int GL_FLOAT;
	public final int GL_GREATER;
	public final int GL_LESS;
	public final int GL_MODELVIEW;
	public final int GL_NEAREST;
	public final int GL_ONE_MINUS_SRC_ALPHA;
	public final int GL_PROJECTION;
	public final int GL_QUADS;
	public final int GL_RGB;
	public final int GL_RGBA;
	public final int GL_SRC_ALPHA;
	public final int GL_STATIC_DRAW;
	public final int GL_TEXTURE_2D;
	public final int GL_TEXTURE_COORD_ARRAY;
	public final int GL_TEXTURE_MAG_FILTER;
	public final int GL_TEXTURE_MIN_FILTER;
	public final int GL_TRIANGLES;
	public final int GL_UNSIGNED_BYTE;
	public final int GL_VERTEX_ARRAY;
	
	protected Pangl(final int GL_ALPHA_TEST, final int GL_ARRAY_BUFFER, final int GL_ARRAY_BUFFER_BINDING, final int GL_BLEND, final int GL_COLOR_BUFFER_BIT, final int GL_DEPTH_BUFFER_BIT, final int GL_DEPTH_TEST, final int GL_FLOAT, final int GL_GREATER, final int GL_LESS, final int GL_MODELVIEW, final int GL_NEAREST, final int GL_ONE_MINUS_SRC_ALPHA, final int GL_PROJECTION, final int GL_QUADS, final int GL_RGB, final int GL_RGBA, final int GL_SRC_ALPHA, final int GL_STATIC_DRAW, final int GL_TEXTURE_2D, final int GL_TEXTURE_COORD_ARRAY, final int GL_TEXTURE_MAG_FILTER, final int GL_TEXTURE_MIN_FILTER, final int GL_TRIANGLES, final int GL_UNSIGNED_BYTE, final int GL_VERTEX_ARRAY) {
		this.GL_ALPHA_TEST = GL_ALPHA_TEST;
		this.GL_ARRAY_BUFFER = GL_ARRAY_BUFFER;
		this.GL_ARRAY_BUFFER_BINDING = GL_ARRAY_BUFFER_BINDING;
		this.GL_BLEND = GL_BLEND;
		this.GL_COLOR_BUFFER_BIT = GL_COLOR_BUFFER_BIT;
		this.GL_DEPTH_BUFFER_BIT = GL_DEPTH_BUFFER_BIT;
		this.GL_DEPTH_TEST = GL_DEPTH_TEST;
		this.GL_FLOAT = GL_FLOAT;
		this.GL_GREATER = GL_GREATER;
		this.GL_LESS = GL_LESS;
		this.GL_MODELVIEW = GL_MODELVIEW;
		this.GL_NEAREST = GL_NEAREST;
		this.GL_ONE_MINUS_SRC_ALPHA = GL_ONE_MINUS_SRC_ALPHA;
		this.GL_PROJECTION = GL_PROJECTION;
		this.GL_QUADS = GL_QUADS;
		this.GL_RGB = GL_RGB;
		this.GL_RGBA = GL_RGBA;
		this.GL_SRC_ALPHA = GL_SRC_ALPHA;
		this.GL_STATIC_DRAW = GL_STATIC_DRAW;
		this.GL_TEXTURE_2D = GL_TEXTURE_2D;
		this.GL_TEXTURE_COORD_ARRAY = GL_TEXTURE_COORD_ARRAY;
		this.GL_TEXTURE_MAG_FILTER = GL_TEXTURE_MAG_FILTER;
		this.GL_TEXTURE_MIN_FILTER = GL_TEXTURE_MIN_FILTER;
		this.GL_TRIANGLES = GL_TRIANGLES;
		this.GL_UNSIGNED_BYTE = GL_UNSIGNED_BYTE;
		this.GL_VERTEX_ARRAY = GL_VERTEX_ARRAY;
	}
	
	public abstract void glAlphaFunc(final int func, final float ref);
	
	public abstract void glBindBuffer(final int target, final int buffer);
	
	public abstract void glBindTexture(final int target, final int texture);
	
	public abstract void glBlendFunc(final int sfactor, final int dfactor);
	
	public abstract void glBufferData(final int target, final FloatBuffer data, final int usage);
	
	public abstract void glClear(final int mask);
	
	public abstract void glClearColor(final float red, final float green, final float blue, final float alpha);
	
	public abstract void glClearDepth(final double depth);
	
	public abstract void glColor4b(final byte red, final byte green, final byte blue, final byte alpha);
	
	public abstract void glDeleteBuffers(final int buffer);
	
	public abstract void glDeleteTextures(final int texture);
	
	public abstract void glDepthFunc(final int func);
	
	public abstract void glDepthMask(final boolean flag);
	
	public abstract void glDisable(final int cap);
	
	public abstract void glDisableClientState(final int cap);
	
	public abstract void glDrawArrays(final int mode, final int first, final int count);
	
	public abstract void glEnable(final int cap);
	
	public abstract void glEnableClientState(final int cap);
	
	public abstract void glGenBuffers(final IntBuffer buffers);
	
	public abstract void glGenTextures(final IntBuffer textures);
	
	public abstract void glLoadIdentity();
	
	public abstract void glMatrixMode(final int mode);
	
	public abstract void glOrtho(final double left, final double right, final double bottom, final double top,
			final double zNear, final double zFar);
	
	public abstract void glReadPixels(final int x, final int y, final int width, final int height,
			final int format, final int type, final ByteBuffer pixels);
	
	public abstract void glTexCoordPointer(final int size, final int stride, final FloatBuffer pointer);
	
	public abstract void glTexCoordPointer(final int size, final int type, final int stride, final int offset);
	
	public abstract void glTexImage2D(final int target, final int level, final int internalFormat,
			final int width, final int height, final int border, final int format, final int type, final ByteBuffer pixels);
	
	public abstract void glTexParameteri(final int target, final int pname, final int param);
	
	public abstract void glVertexPointer(final int size, final int stride, final FloatBuffer pointer);
	
	public abstract void glVertexPointer(final int size, final int type, final int stride, final int offset);
	
	public abstract void glViewport(final int x, final int y, final int width, final int height);
	
	public abstract boolean isQuadSupported();
}
