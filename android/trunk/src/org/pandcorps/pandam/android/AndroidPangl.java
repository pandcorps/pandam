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
package org.pandcorps.pandam.android;

import java.nio.*;
import javax.microedition.khronos.opengles.*;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;

public final class AndroidPangl extends Pangl {
	private final GL10 gl;
	private final GL11 gl11;
	
	protected AndroidPangl(final GL10 gl) {
		super(GL10.GL_ALPHA_TEST, GL11.GL_ARRAY_BUFFER, GL11.GL_ARRAY_BUFFER_BINDING, GL10.GL_BLEND, GL10.GL_COLOR_BUFFER_BIT, GL10.GL_DEPTH_BUFFER_BIT, GL10.GL_DEPTH_TEST, GL10.GL_FLOAT, GL10.GL_GREATER, GL10.GL_LESS, GL10.GL_MODELVIEW, GL10.GL_NEAREST, GL10.GL_ONE_MINUS_SRC_ALPHA, GL10.GL_PROJECTION, -1, GL10.GL_RGB, GL10.GL_RGBA, GL10.GL_SRC_ALPHA, GL11.GL_STATIC_DRAW, GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_COORD_ARRAY, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_TRIANGLES, GL10.GL_UNSIGNED_BYTE, GL10.GL_VERTEX_ARRAY);
		this.gl = gl;
		gl11 = gl instanceof GL11 ? (GL11) gl : null;
	}
	
	@Override
	public final void glAlphaFunc(final int func, final float ref) {
		gl.glAlphaFunc(func, ref);
	}
	
	@Override
	public final void glBindBuffer(final int target, final int buffer) {
		gl11.glBindBuffer(target, buffer);
	}
	
	@Override
	public final void glBindTexture(final int target, final int texture) {
		gl.glBindTexture(target, texture);
	}
	
	@Override
	public final void glBlendFunc(final int sfactor, final int dfactor) {
		gl.glBlendFunc(sfactor, dfactor);
	}
	
	@Override
	public final void glBufferData(final int target, final FloatBuffer data, final int usage) {
		gl11.glBufferData(target, data.capacity() * 4, data, usage);
	}
	
	@Override
	public final void glClear(final int mask) {
		gl.glClear(mask);
	}
	
	@Override
	public final void glClearColor(final float red, final float green, final float blue, final float alpha) {
		gl.glClearColor(red, green, blue, alpha);
	}
	
	@Override
	public final void glClearDepth(final double depth) {
		gl.glClearDepthf((float) depth);
	}
	
	@Override
	public final void glColor4b(final byte red, final byte green, final byte blue, final byte alpha) {
		//gl.glColor4ub(red, green, blue, alpha);
		//gl.glColor4x(red, green, blue, alpha); // Would maybe work with bit shifting? Or does it only expect 0 or 1?
		gl.glColor4f(toFloat(red), toFloat(green), toFloat(blue), toFloat(alpha));
	}
	
	private final static float toFloat(final float f) {
		return (f - Byte.MIN_VALUE) / 255f;
	}
	
	@Override
	public final void glDeleteTextures(final int texture) {
		final IntBuffer textures = Pantil.allocateDirectIntBuffer(1);
		textures.put(texture);
		textures.rewind();
		gl.glDeleteTextures(1, textures);
	}
	
	@Override
	public final void glDepthFunc(final int func) {
		gl.glDepthFunc(func);
	}
	
	@Override
	public final void glDepthMask(final boolean flag) {
		gl.glDepthMask(flag);
	}
	
	@Override
	public final void glDisable(final int cap) {
		gl.glDisable(cap);
	}
	
	@Override
	public final void glDisableClientState(final int cap) {
		gl.glDisableClientState(cap);
	}
	
	@Override
	public final void glDrawArrays(final int mode, final int first, final int count) {
		gl.glDrawArrays(mode, first, count);
	}
	
	@Override
	public final void glEnable(final int cap) {
		gl.glEnable(cap);
	}
	
	@Override
	public final void glEnableClientState(final int cap) {
		gl.glEnableClientState(cap);
	}
	
	@Override
	public final void glGenBuffers(final IntBuffer buffers) {
		gl11.glGenBuffers(buffers.limit(), buffers);
	}
	
	@Override
	public final void glGenTextures(final IntBuffer textures) {
		gl.glGenTextures(textures.limit(), textures);
	}
	
	@Override
	public final void glLoadIdentity() {
		gl.glLoadIdentity();
	}
	
	@Override
	public final void glMatrixMode(final int mode) {
		gl.glMatrixMode(mode);
	}
	
	@Override
	public final void glOrtho(final double left, final double right, final double bottom, final double top,
			final double zNear, final double zFar) {
		gl.glOrthof((float) left, (float) right, (float) bottom, (float) top, (float) zNear, (float) zFar);
	}
	
	@Override
	public final void glReadPixels(final int x, final int y, final int width, final int height,
			final int format, final int type, final ByteBuffer pixels) {
		gl.glReadPixels(x, y, width, height, format, type, pixels);
	}
	
	@Override
	public final void glTexCoordPointer(final int size, final int stride, final FloatBuffer pointer) {
		gl.glTexCoordPointer(size, GL10.GL_FLOAT, stride, pointer);
	}
	
	@Override
	public final void glTexCoordPointer(final int size, final int type, final int stride, final int offset) {
		gl11.glTexCoordPointer(size, type, stride, offset);
	}
	
	@Override
	public final void glTexImage2D(final int target, final int level, final int internalFormat,
			final int width, final int height, final int border, final int format, final int type, final ByteBuffer pixels) {
		gl.glTexImage2D(target, level, internalFormat, width, height, border, format, type, pixels);
	}
	
	@Override
	public final void glTexParameteri(final int target, final int pname, final int param) {
		gl.glTexParameterx(target, pname, param);
	}
	
	@Override
	public final void glVertexPointer(final int size, final int stride, final FloatBuffer pointer) {
		gl.glVertexPointer(size, GL10.GL_FLOAT, stride, pointer);
	}
	
	@Override
	public final void glVertexPointer(final int size, final int type, final int stride, final int offset) {
		gl11.glVertexPointer(size, type, stride, offset);
	}
	
	@Override
	public final void glViewport(final int x, final int y, final int width, final int height) {
		gl.glViewport(x, y, width, height);
	}
	
	@Override
	public final boolean isQuadSupported() {
		return false;
	}
}
