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
package org.pandcorps.monster;

import java.util.*;

public abstract class Option implements Runnable {
	protected final static ArrayList<Entity> EMPTY = new ArrayList<Entity>(0);
	protected final Label goal; // Same as awarded for a catch or buy task; for battle goal will be opponent creature, and awarded will be experience/money
	protected final List<Entity> required;
	private String info = null;
	private boolean autoBackEnabled = false;

	protected Option(final Label goal) {
	    this(goal, null);
	}

	protected Option(final Label goal, final Collection<? extends Entity> required) {
	    this.goal = goal;
	    this.required = init(required);
	}
	
	protected final static ArrayList<Entity> init(final Collection<? extends Entity> list) {
	    return list == null ? EMPTY : new ArrayList<Entity>(list);
	}

	public final Label getGoal() {
		return goal;
	}

	public final List<Entity> getRequired() {
		return required;
	}

	public List<Entity> getAwarded() {
		return EMPTY;
	}
	
	public final String getInfo() {
	    return info;
	}
	
	public final void setInfo(final String info) {
	    this.info = info;
	}
	
	public final boolean isAutoBackEnabled() {
	    return autoBackEnabled;
	}
	
	public final void setAutoBackEnabled(final boolean autoBackEnabled) {
	    this.autoBackEnabled = autoBackEnabled;
	}

	public boolean isPossible() {
        for (final Entity requirement : required) {
            if (!requirement.isAvailable()) {
                return false;
            }
        }
        return true;
    }
	
	@Override
	public String toString() {
	    return goal.toString();
	}
	
	@Override
    public boolean equals(final Object o) {
        if (!(o instanceof Option)) {
            return false;
        } else if (!getClass().equals(o.getClass())) {
            return false;
        }
        final Option opt = (Option) o;
        if (!goal.equals(opt.goal)) {
            return false;
        } else if (!getAwarded().equals(opt.getAwarded())) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        return goal.hashCode();
    }
}
