/*
Copyright (c) 2009-2016, Andrew M. Martin
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
package org.pandcorps.test.pandam;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.*;

public final class TestPandamTest extends PandamTest {
	public final void testPanple() {
		compareEquals(null, null);
		final Panple p1 = new ImplPanple(1, 2, 3);
		compareNotEquals(p1, null);
		final Panple p2 = new ImplPanple(1, 2, 3);
		compareEquals(p1, p2);
		p2.setX(4);
		compareNotEquals(p1, p2);
		final String diffx = "\t" + diff(new Float(1), new Float(4)) + "\n" + trace("X") + "\n";
		assertEquals(diffx, getDiffTrace(p1, p2));
		p2.setX(1);
		p2.setY(4);
		final String diffy = "\t" + diff(new Float(2), new Float(4)) + "\n" + trace("Y") + "\n";
		assertEquals(diffy, getDiffTrace(p1, p2));
		p2.setY(2);
		p2.setZ(4);
		final String diffz = "\t" + diff(new Float(3), new Float(4)) + "\n" + trace("Z") + "\n";
		assertEquals(diffz, getDiffTrace(p1, p2));
		p2.setX(4);
		assertEquals(diffx + diffz, getDiffTrace(p1, p2));
		p2.setY(4);
		assertEquals(diffx + diffy + diffz, getDiffTrace(p1, p2));
	}

	private final void compareEquals(final Object o1, final Object o2) {
		assertEquals(o1, o1);
		assertEquals(o2, o2);
		assertEquals(o1, o2);
		assertEquals(o2, o1);
		assertEquals("", getDiffTrace(o1, o2));
	}

	private final void compareNotEquals(final Object o1, final Object o2) {
		assertNotEquals(o1, o2);
		assertNotEquals(o2, o1);
		assertTrue(getDiffTrace(o1, o2).length() > 0);
	}
}
