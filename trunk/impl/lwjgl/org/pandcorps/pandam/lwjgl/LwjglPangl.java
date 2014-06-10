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
		super(GL11.GL_ALPHA_TEST, GL11.GL_BLEND, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_DEPTH_TEST, GL11.GL_GREATER, GL11.GL_LESS, GL11.GL_MODELVIEW, GL11.GL_NEAREST, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_PROJECTION, GL11.GL_QUADS, GL11.GL_RGB, GL11.GL_RGBA, GL11.GL_SRC_ALPHA, GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_COORD_ARRAY, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_TRIANGLES, GL11.GL_UNSIGNED_BYTE, GL11.GL_VERTEX_ARRAY);
	}
	
	@Override
	public final void glAlphaFunc(final int func, final float ref) {
		GL11.glAlphaFunc(func, ref);
	}
	
	@Override
	public final void glBindTexture(final int target, final int texture) {
		GL11.glBindTexture(target, texture);
	}
	
	@Override
	public final void glBlendFunc(final int sfactor, final int dfactor) {
		GL11.glBlendFunc(sfactor, dfactor);
	}
	
	@Override
	public final void glClear(final int mask) {
		GL11.glClear(mask);
	}
	
	@Override
	public final void glClearColor(final float red, final float green, final float blue, final float alpha) {
		GL11.glClearColor(red, green, blue, alpha);
	}
	
	@Override
	public final void glClearDepth(final double depth) {
		GL11.glClearDepth(depth);
	}
	
	@Override
	public final void glColor4b(final byte red, final byte green, final byte blue, final byte alpha) {
		GL11.glColor4b(red, green, blue, alpha);
	}
	
	@Override
	public final void glDeleteTextures(final int texture) {
		GL11.glDeleteTextures(texture);
	}
	
	@Override
	public final void glDepthFunc(final int func) {
		GL11.glDepthFunc(func);
	}
	
	@Override
	public final void glDepthMask(final boolean flag) {
		GL11.glDepthMask(flag);
	}
	
	@Override
	public final void glDisable(final int cap) {
		GL11.glDisable(cap);
	}
	
	@Override
	public final void glDisableClientState(final int cap) {
		GL11.glDisableClientState(cap);
	}
	
	@Override
	public final void glDrawArrays(final int mode, final int first, final int count) {
		GL11.glDrawArrays(mode, first, count);
	}
	
	@Override
	public final void glEnable(final int cap) {
		GL11.glEnable(cap);
	}
	
	@Override
	public final void glEnableClientState(final int cap) {
		GL11.glEnableClientState(cap);
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
	public final void glMatrixMode(final int mode) {
		GL11.glMatrixMode(mode);
	}
	
	@Override
	public final void glOrtho(final double left, final double right, final double bottom, final double top,
			final double zNear, final double zFar) {
		GL11.glOrtho(left, right, bottom, top, zNear, zFar);
	}
	
	@Override
	public final void glReadPixels(final int x, final int y, final int width, final int height,
			final int format, final int type, final ByteBuffer pixels) {
		GL11.glReadPixels(x, y, width, height, format, type, pixels);
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
	
	@Override
	public final void glViewport(final int x, final int y, final int width, final int height) {
		GL11.glViewport(x, y, width, height);
	}
	
	@Override
	public final boolean isQuadSupported() {
		return true;
	}
}
