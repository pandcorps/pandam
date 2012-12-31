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
package org.pandcorps.shoot;

public abstract class Attribute {
	
	private int value;
	
	public Attribute(final int value) {
		this.value = value;
	}
	
	public final int get() {
		return value;
	}
	
	public final void set(final int value) {
		this.value = value;
	}
	
	public final boolean inc(final int amount) {
		if (amount <= 0) {
			throw new IllegalArgumentException("Cannot add " + amount); // + " " + label
		}
		final int old = value;
		value = value + amount;
		final int max = max();
		value = value < 0 ? max : Math.min(value, max);
		return value != old;
	}
	
	public final void dec() {
		dec(1);
	}
	
	public final void dec(final int amount) {
		if (isFinite()) {
            value -= amount;
		}
		/*
		Illegal for ammunition; must be checked ahead of time.
		Legal for health; will check for defeat after this.
		if (value < 0) {
			throw new IllegalStateException("Negative value " + value);
		}
		*/
	}
	
	public final boolean isInfinite() {
		return value == Weapon.INF;
	}
	
	public final boolean isFinite() {
		return value != Weapon.INF;
	}
	
	public abstract int max();
}
