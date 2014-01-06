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
package org.pandcorps.test.core;

import org.pandcorps.core.*;
import org.pandcorps.test.*;

public final class TestMathtil extends Pantest {
	public final void testRandf() {
		runRandfint(0, 1);
		runRandfint(0, 10);
		runRandfint(-10, 10);
		runRandfint(10, 20);
		runRandf(0, .5f);
		runRandf(.25f, .75f);
	}

	private final void runRandfint(final int min, final int max) {
		runRandf(min, max);
		runRand(new FloatRander(min, max), min, max);
	}

	private final void runRandf(final float min, final float max) {
		for (int i = 0; i < 25; i++) {
			final float f = Mathtil.randf(min, max);
			assertTrue(f >= min);
			assertTrue(f <= max);
		}
	}

	public final void testRandi() {
		runRandi(0, 1);
		runRandi(0, 4);
		runRandi(2, 10);
		runRandi(-5, 5);
	}

	private final void runRandi(final int min, final int max) {
		runRand(new IntRander(min, max), min, max);
	}

	private final void runRand(final Rander rander, final int min, final int max) {
		final int valueCounts[] = new int[max - min + 1];

		do {
			final int roll = rander.rand();
			assertTrue(roll >= min);
			assertTrue(roll <= max);
			valueCounts[roll - min]++;
		}
		while (anyZeros(valueCounts)); // If this loops forever, then we're not generating all possible roll values
	}

	private final boolean anyZeros(final int valueCounts[]) {
		for (int count : valueCounts) {
			if (count == 0) {
				return true;
			}
		}

		return false;
	}

	interface Rander {
		public int rand();
	}

	final class IntRander implements Rander {
		private final int min;
		private final int max;

		public IntRander(final int min, final int max) {
			this.min = min;
			this.max = max;
		}

		@Override
		public final int rand() {
			return Mathtil.randi(min, max);
		}
	}

	final class FloatRander implements Rander {
		private final float min;
		private final float max;

		public FloatRander(final float min, final float max) {
			this.min = min;
			this.max = max;
		}

		@Override
		public final int rand() {
			return Math.round(Mathtil.randf(min, max));
		}
	}
}
