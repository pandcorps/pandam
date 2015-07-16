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

public class Type {
	private static List<Type> types = null;
	private final String code;
	private final String name;

	public Type(final String code, final String name) {
		this.code = code;
		this.name = name;
	}

	public final String getCode() {
		return code;
	}

	public final String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	public final static List<Type> getTypes() {
		return types;
	}

	/*package*/ final static void setTypes(final List<Type> types) {
		Type.types = Entity.unmod(types);
	}

	public final static Type getType(final String type) {
		for (final Type t : types) {
			if (t.code.equalsIgnoreCase(type) || t.name.equalsIgnoreCase(type)) {
				return t;
			}
		}
		throw new IllegalArgumentException(type);
	}
	
	@Override
    public boolean equals(final Object o) {
        if (!(o instanceof Type)) {
            return false;
        }
        return name.equals(((Type) o).getName());
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
