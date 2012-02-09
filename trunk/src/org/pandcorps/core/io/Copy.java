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
package org.pandcorps.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.pandcorps.core.Coltil;
import org.pandcorps.core.Iotil;

public class Copy {
	public final static void run(final String src, final String dst, final String exclude) throws IOException {
		run(new File(src), new File(dst), exclude);
	}
	
	public final static void run(final File src, final File dst, final String exclude) throws IOException {
		final String srcName = src.getName();
		if (exclude != null && exclude.equals(srcName)) {
			System.out.println("Excluding " + src);
			return;
		}
		if (!dst.isDirectory()) {
			throw new IllegalArgumentException("Expected destination to be directory " + dst);
		}
		final File dstChild = new File(dst.getAbsolutePath() + File.separator + srcName);
		if (src.isDirectory()) {
			System.out.println("Copying directory " + src + " to " + dst);
			if (!dstChild.mkdir()) {
				throw new IllegalStateException("Could not create directory " + dstChild);
			}
			for (final File srcChild : src.listFiles()) {
				run(srcChild, dstChild, exclude);
			}
		} else {
			System.out.println("Copying file " + src + " to " + dst);
			FileInputStream in = new FileInputStream(src);
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(dstChild);
				Iotil.copy(in, out);
			} finally {
				Iotil.close(in);
				Iotil.close(out);
			}
		}
	}
	
	public final static void main(final String[] args) {
		try {
			Copy.run(args[0], args[1], Coltil.get(args, 2));
		} catch (final Throwable e) {
			e.printStackTrace();
		}
	}
}
