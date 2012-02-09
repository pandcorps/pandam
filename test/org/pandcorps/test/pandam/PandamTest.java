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
package org.pandcorps.test.pandam;

import java.io.*;
import java.util.*;
import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.test.*;

public abstract class PandamTest extends Pantest {
	private final static PandamTest t = new ImplPandamTest();

	private final static class ImplPandamTest extends PandamTest {
	}

	static {
		System.setProperty(Pangine.PROP_IMPL, UnitPangine.class.getName());
	}

	protected PandamTest() {
	}

	@Override
	public final boolean printDiffTrace
		(final Writer w, final Object expected, final Object actual, final int d) {
		if (expected == null && actual == null) {
			return false;
		}
		if (instanceOf(expected, actual, Pantity.class)) {
			return printDiffTrace(w, (Pantity) expected, (Pantity) actual, d);
		}
		else if (instanceOf(expected, actual, Pangame.class)) {
			return printDiffTrace(w, (Pangame) expected, (Pangame) actual, d);
		}
		else if (instanceOf(expected, actual, Panple.class)) {
			return printDiffTrace(w, (Panple) expected, (Panple) actual, d);
		}
		else {
			return super.printDiffTrace(w, expected, actual, d);
		}
	}

	public final static void assertEquals(final Object expected, final Object actual) {
		assertEquals(t, expected, actual);
	}

	public final static void assertNotEquals(final Object expected, final Object actual) {
		assertNotEquals(t, expected, actual);
	}

	@Override
	protected final boolean printDiffTrace
		(final Writer w, final Set<?> expected, final Set<?> actual, final int d) {
		final int cd = d + 1, size = Coltil.size(expected);
		if (printTrace(w, "size", d, printDiffTrace(w, size, Coltil.size(actual), cd))) {
			return true;
		}
		if (!(allPantity(expected) && allPantity(actual))) {
			return super.printDiffTrace(w, expected, actual, d);
		}
		final HashMap<String, Pantity> actualMap = new HashMap<String, Pantity>(size);
		for (final Object o : actual) {
			final Pantity p = (Pantity) o;
			actualMap.put(p.getId(), p);
		}
		boolean ret = false;
		for (final Object o : expected) {
			final Pantity expectedEntity = (Pantity) o;
			final String id = expectedEntity.getId();
			ret |= printTrace(w, id, d, printDiffTrace(w, expectedEntity, actualMap.get(id), cd));
		}
		return ret;
	}

	private final boolean allPantity(final Collection<?> c) {
		if (c == null) {
			return true;
		}
		for (final Object o : c) {
			if (!instanceOf(o, Pantity.class)) {
				return false;
			}
		}
		return true;
	}

	protected final boolean printDiffTrace(final Writer w, final Panple expected, final Panple actual, final int d) {
		if (expected == null && actual == null) {
			return false;
		}
		if (printNullDiffTrace(w, expected, actual, d)) {
			return true;
		}
		final int cd = d + 1;
		boolean ret = false;
		ret |= printTrace(w, "X", d, printDiffTrace(w, expected.getX(), actual.getX(), cd));
		ret |= printTrace(w, "Y", d, printDiffTrace(w, expected.getY(), actual.getY(), cd));
		ret |= printTrace(w, "Z", d, printDiffTrace(w, expected.getZ(), actual.getZ(), cd));
		return ret;
	}

	private final boolean printDiffTrace(final Writer w, final Pantity expected, final Pantity actual, final int d) {
		if (expected == null && actual == null) {
			return false;
		}
		if (printNullDiffTrace(w, expected, actual, d)) {
			return true;
		}
		if (instanceOf(expected, actual, Panmage.class)) {
			return printDiffTrace(w, (Panmage) expected, (Panmage) actual, d);
		}
		else if (instanceOf(expected, actual, Panframe.class)) {
			return printDiffTrace(w, (Panframe) expected, (Panframe) actual, d);
		}
		else if (instanceOf(expected, actual, Panimation.class)) {
			return printDiffTrace(w, (Panimation) expected, (Panimation) actual, d);
		}
		else if (instanceOf(expected, actual, Pantype.class)) {
			return printDiffTrace(w, (Pantype) expected, (Pantype) actual, d);
		}
		else if (instanceOf(expected, actual, Panctor.class)) {
			return printDiffTrace(w, (Panctor) expected, (Panctor) actual, d);
		}
		else if (instanceOf(expected, actual, Panroom.class)) {
			return printDiffTrace(w, (Panroom) expected, (Panroom) actual, d);
		}
		else {
			throw new Panception(
				"Can't compare " + expected.getClass().getName() +
				" to " + actual.getClass().getName());
		}
	}

	private final boolean printDiffTraceSuper
		(final Writer w, final Pantity expected, final Pantity actual, final int d) {
		if (expected == null && actual == null) {
			return false;
		}
		return printTrace(w, "Id", d, printDiffTrace(w, expected.getId(), actual.getId(), d + 1));
	}

	private final boolean printDiffTrace
		(final Writer w, final Panmage expected, final Panmage actual, final int d) {
		final int cd = d + 1;
		boolean ret = false;
		ret |= printDiffTraceSuper(w, expected, actual, cd);
		ret |= printTrace(w, "Size", d, printDiffTrace(w, expected.getSize(), actual.getSize(), cd));
		return ret;
	}

	private final boolean printDiffTrace
		(final Writer w, final Panframe expected, final Panframe actual, final int d) {
		final int cd = d + 1;
		boolean ret = false;
		ret |= printDiffTraceSuper(w, expected, actual, cd);
		ret |= printTrace(w, "Duration", d, printDiffTrace(w, expected.getDuration(), actual.getDuration(), cd));
		ret |= printTrace(w, "Image", d, printDiffTrace(w, expected.getImage(), actual.getImage(), cd));
		return ret;
	}

	private final boolean printDiffTrace
		(final Writer w, final Panimation expected, final Panimation actual, final int d) {
		final int cd = d + 1;
		boolean ret = false;
		ret |= printDiffTraceSuper(w, expected, actual, cd);
		final Panframe[] expectedFrames = expected.getFrames();
		ret |= printTrace(w, "Frames", d, printDiffTrace(w, expectedFrames, actual.getFrames(), cd));
		return ret;
	}

	//TODO Harmonze "Animation" label with Panview
	private final boolean printDiffTrace
		(final Writer w, final Pantype expected, final Pantype actual, final int d) {
		final int cd = d + 1;
		boolean ret = false;
		ret |= printDiffTraceSuper(w, expected, actual, cd);
		ret |= printTrace(w, "Animation", d, printDiffTrace(w, expected.getView(), actual.getView(), cd));
		ret |= printTrace(w, "ActorClass", d, printDiffTrace(w, expected.getActorClass(), actual.getActorClass(), cd));
		return ret;
	}

	private final boolean printDiffTrace
		(final Writer w, final Panctor expected, final Panctor actual, final int d) {
		final int cd = d + 1;
		boolean ret = false;
		ret |= printDiffTraceSuper(w, expected, actual, cd);
		ret |= printTrace(w, "Animation", d, printDiffTrace(w, expected.getView(), actual.getView(), cd));
		ret |= printTrace(w, "Position", d, printDiffTrace(w, expected.getPosition(), actual.getPosition(), cd));
		ret |= printTrace(w, "Type", d, printDiffTrace(w, expected.getType(), actual.getType(), cd));
		return ret;
	}

	private final boolean printDiffTrace
		(final Writer w, final Panroom expected, final Panroom actual, final int d) {
		final int cd = d + 1;
		boolean ret = false;
		ret |= printDiffTraceSuper(w, expected, actual, cd);
		ret |= printTrace(w, "Actors", d, printDiffTrace(w, expected.getActors(), actual.getActors(), cd));
		ret |= printTrace(w, "Size", d, printDiffTrace(w, expected.getSize(), actual.getSize(), cd));
		return ret;
	}

	private final boolean printDiffTrace
		(final Writer w, final Pangame expected, final Pangame actual, final int d) {
		return printTrace(w, "CurrentRoom", d, printDiffTrace(w, expected.getCurrentRoom(), actual.getCurrentRoom(), d + 1));
	}
}
