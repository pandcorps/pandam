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
package org.pandcorps.pandam.lwjgl;

import org.pandcorps.core.Chartil;
import org.pandcorps.pandam.*;

public final class LwjglPanteraction extends Panteraction {
	@Override
	public final int getKeyCount() {
		return org.lwjgl.input.Keyboard.getKeyCount();
	}
	
	@Override
	public final boolean isCapsLockEnabled() {
	    return LwjglPangine.getEngine().capsLock;
	}
	
	@Override
	public final boolean isInsEnabled() {
	    return LwjglPangine.getEngine().ins;
	}
	
	@Override
	protected final int getIndexBackslash() {
		/*
		When backslash is pressed, Keyboard returns
		0 on a Linux netbook, 43 on a Windows laptop
		System.out.println("0=" + Keyboard.getKeyName(0)); // NONE, even on netbook
		System.out.println("43=" + Keyboard.getKeyName(43)); // BACKSLASH, even on netbook
		System.out.println("\\=" + Keyboard.getKeyIndex("\\")); // 0 on netbook, also laptop
		System.out.println("KEY_BACKSLASH=" + Keyboard.KEY_BACKSLASH); // 43, even on netbook
		System.out.println("os=" + System.getProperty("os.name"));
		*/
	    final String os = Chartil.toUpperCase(System.getProperty("os.name"));
	    if (os != null && (os.contains("NIX") || os.contains("NUX") || os.contains("BSD"))) {
	        return 0;
	    }
		return 43;
	}
}
