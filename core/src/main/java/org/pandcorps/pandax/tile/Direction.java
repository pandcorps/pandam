/*
Copyright (c) 2009-2017, Andrew M. Martin
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
package org.pandcorps.pandax.tile;

public enum Direction {
    South(0, -1), // A more natural sprite sheet
    East(1, 0),
    North(0, 1),
    West(-1, 0);
    
    private final int multiplierX;
    
    private final int multiplierY;
    
    private Direction(final int multiplierX, final int multiplierY) {
        this.multiplierX = multiplierX;
        this.multiplierY = multiplierY;
    }
    
    public final Direction getOpposite() {
        if (this == South) {
            return North;
        } else if (this == East) {
            return West;
        } else if (this == North) {
            return South;
        }
        return East;
    }
    
    public final Direction getClockwise() {
        if (this == South) {
            return West;
        } else if (this == East) {
            return South;
        } else if (this == North) {
            return East;
        }
        return North;
    }
    
    public final Direction getCounterclockwise() {
        if (this == South) {
            return East;
        } else if (this == East) {
            return North;
        } else if (this == North) {
            return West;
        }
        return South;
    }
    
    public final int getMultiplierX() {
        return multiplierX;
    }
    
    public final int getMultiplierY() {
        return multiplierY;
    }
}
