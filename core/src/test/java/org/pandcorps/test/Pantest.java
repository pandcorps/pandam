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
package org.pandcorps.test;

import java.io.*;
import java.util.*;
import junit.framework.*;
import org.pandcorps.core.*;

// Pandcorps Test
public abstract class Pantest extends TestCase {
	private final static Pantest t = new ImplPantest();

	private final static class ImplPantest extends Pantest {
	}

	protected Pantest() {
	}

	public final static void assertEquals(final float expected, final float actual) {
		assertEquals(new Float(expected), new Float(actual));
	}

	public final static void assertEquals(final double expected, final double actual) {
		assertEquals(new Double(expected), new Double(actual));
	}

	public static void assertEquals(final Object expected, final Object actual) {
		assertEquals(t, expected, actual);
	}

	public static void assertNotEquals(final Object expected, final Object actual) {
		assertNotEquals(t, expected, actual);
	}

	protected final static void assertEquals(final Pantest t, final Object expected, final Object actual) {
		final String diff = t.getDiffTrace(expected, actual);
		if (diff.length() > 0) {
			fail(diff);
		}
	}

	protected final static void assertNotEquals(final Pantest t, final Object expected, final Object actual) {
		if (t.equals(expected, actual)) {
			fail("Expected to find a difference between " + expected + " and " + actual);
		}
	}

	protected final static void print(final Writer w, final String s, final int d) {
		try {
			for (int i = 0; i < d; i++) {
				w.write('\t');
			}
			w.write(s);
			w.write('\n');
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected final static String diff(final Object expected, final Object actual) {
		return "expected:<" + expected + "> but was:<" + actual + ">";
	}

	protected final static void printDiff(final Writer w, final Object expected, final Object actual, final int d) {
		print(w, diff(expected, actual), d);
	}

	protected final static String trace(final String field) {
		return "at " + field;
	}

	protected final static boolean printTrace(final Writer w, final String field, final int d, final boolean p) {
		if (p) {
			print(w, trace(field), d);
		}
		return p;
	}

	protected final boolean printDiffTrace(final Writer w, final float expected, final float actual, final int d) {
		if (expected != actual) {
			printDiff(w, new Float(expected), new Float(actual), d);
			return true;
		}
		return false;
	}

	protected final boolean printDiffTrace(final Writer w, final int expected, final int actual, final int d) {
		if (expected != actual) {
			printDiff(w, new Integer(expected), new Integer(actual), d);
			return true;
		}
		return false;
	}

	protected final boolean instanceOf(final Object o, final Class<?> c) {
		return o == null || c.isAssignableFrom(o.getClass());
	}

	protected final boolean instanceOf(final Object o1, final Object o2, final Class<?> c) {
		return instanceOf(o1, c) && instanceOf(o2, c);
	}

	protected final boolean printNullDiffTrace
		(final Writer w, final Object expected, final Object actual, final int d) {
		if (expected == null ? actual != null : actual == null) {
			printDiff(w, expected, actual, d);
			return true;
		}
		return false;
	}

	public boolean printDiffTrace
		(final Writer w, final Object expected, final Object actual, final int d) {
		if (expected == null && actual == null) {
			return false;
		}
		if (instanceOf(expected, actual, Object[].class)) {
			return printDiffTrace(w, (Object[]) expected, (Object[]) actual, d);
		}
		else if (instanceOf(expected, actual, Collection.class)) {
			return printDiffTrace(w, (Collection<?>) expected, (Collection<?>) actual, d);
		}
		else {
			if (expected == null ? (actual != null) : ((actual == null) ? true : !(expected.equals(actual) && actual.equals(expected)))) {
				printDiff(w, expected, actual, d);
				return true;
			}
			return false;
		}
	}

	public final void printDiffTrace(final Writer w, final Object expected, final Object actual) {
		/*
		printTrace(w,
			expected == null ? actual.getClass().getSimpleName() : expected.getClass().getSimpleName(),
			0, printDiffTrace(w, expected, actual, 1));
		*/
		printDiffTrace(w, expected, actual, 0);
	}

	public final String getDiffTrace(final Object expected, final Object actual) {
		final StringWriter w = new StringWriter();
		printDiffTrace(w, expected, actual);
		return w.toString();
	}

	public final boolean equals(final Object o1, final Object o2) {
		return getDiffTrace(o1, o2).length() == 0;
	}

	public final boolean contains(final Object expectedItem, final Collection<?> actualCollection) {
		if (actualCollection == null) {
			return false;
		}
		for (final Object a : actualCollection) {
			if (equals(expectedItem, a)) {
				return true;
			}
		}
		return false;
	}

	public final boolean containsAll
		(final Collection<?> expectedSubset, final Collection<?> actualCollection) {
		if (expectedSubset == null) {
			return true;
		}
		for (final Object e : expectedSubset) {
			if (!contains(e, actualCollection)) {
				return false;
			}
		}
		return true;
	}

	protected final boolean printDiffTrace
		(final Writer w, final Object[] expected, final Object[] actual, final int d) {
		final int cd = d + 1;
		if (printTrace(w, "length", d, printDiffTrace(w, Coltil.size(expected), Coltil.size(actual), cd))) {
			return true;
		}
		int i = 0;
		boolean ret = false;
		for (final Object e : expected) {
			ret |= printTrace(w, Integer.toString(i), d, printDiffTrace(w, e, actual[i++], cd));
		}
		return ret;
	}
	
	protected final boolean printDiffTrace
		(final Writer w, final Collection<?> expected, final Collection<?> actual, final int d) {
		if (expected == null && actual == null) {
			return false;
		}
		if (instanceOf(expected, actual, Set.class)) {
			return printDiffTrace(w, (Set<?>) expected, (Set<?>) actual, d);
		}
		else if (instanceOf(expected, actual, List.class)) {
			return printDiffTrace(w, (List<?>) expected, (List<?>) actual, d);
		}
		else {
			throw new UnsupportedOperationException(
				"Can't compare " + Reftil.getClassName(expected) +
				" to " + Reftil.getClassName(actual));
		}
	}
	
	protected final boolean printDiffTrace
		(final Writer w, final List<?> expected, final List<?> actual, final int d) {
		final int cd = d + 1;
		if (printTrace(w, "size", d, printDiffTrace(w, Coltil.size(expected), Coltil.size(actual), cd))) {
			return true;
		}
		int i = 0;
		boolean ret = false;
		for (final Object e : expected) {
			ret |= printTrace(w, Integer.toString(i), d, printDiffTrace(w, e, actual.get(i++), cd));
		}
		return ret;
	}

	protected boolean printDiffTrace
		(final Writer w, final Set<?> expected, final Set<?> actual, final int d) {
		final int cd = d + 1, size = expected.size();
		if (printTrace(w, "size", d, printDiffTrace(w, size, actual.size(), cd))) {
			return true;
		}
		boolean ret = false;
		for (final Object e : expected) {
			if (!contains(e, actual)) {
				ret |= printTrace(w, "element", d, printDiffTrace(w, e, null, cd));
			}
		}
		for (final Object a : actual) {
			if (!contains(a, expected)) {
				ret |= printTrace(w, "element", d, printDiffTrace(w, null, a, cd));
			}
		}
		return ret;
	}
}
