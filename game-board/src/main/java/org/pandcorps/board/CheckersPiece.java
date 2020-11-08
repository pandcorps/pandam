/*
Copyright (c) 2009-2020, Andrew M. Martin
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
package org.pandcorps.board;

import java.util.*;

import org.pandcorps.board.BoardGame.*;
import org.pandcorps.board.CheckersModule.*;
import org.pandcorps.core.*;
import org.pandcorps.pandam.*;

public class CheckersPiece extends BoardGamePiece {
    private final static int[] DIRECTIONS_UP = { 1 };
    private final static int[] DIRECTIONS_DOWN = { -1 };
    private final static int[] DIRECTIONS_CROWNED = { 1, -1 };
    private final static BoardGameGrid<CheckersPiece> grid = CheckersModule.grid;
    protected boolean crowned = false;
    
    protected CheckersPiece(final int player) {
        super(player);
    }
    
    private final int[] getAllowedDirections() {
        if (crowned) {
            return DIRECTIONS_CROWNED;
        }
        //return (player == 0) ? DIRECTIONS_UP : DIRECTIONS_DOWN; // Correct if grid isn't reversed when toggling player
        return (player == BoardGame.module.currentPlayerIndex) ? DIRECTIONS_UP : DIRECTIONS_DOWN; // Current player always moving upward
    }
    
    protected final boolean isAbleToCapture() {
        for (final int dir : getAllowedDirections()) {
            if (isAbleToCapture(dir)) {
                return true;
            }
        }
        return false;
    }
    
    private final boolean isAbleToCapture(final int dir) {
        return isAbleToCapture(1, dir) || isAbleToCapture(-1, dir);
    }
    
    private final boolean isAbleToCapture(final int xDir, final int yDir) {
        final BoardGamePiece neighbor = grid.get(x + xDir, y + yDir);
        if ((neighbor == null) || (neighbor.player == player)) {
            return false;
        }
        return grid.isOpen(x + (xDir * 2), y + (yDir * 2));
    }
    
    protected final boolean isAbleToMove() {
        for (final int dir : getAllowedDirections()) {
            if (isAbleToMove(dir)) {
                return true;
            }
        }
        return false;
    }
    
    private final boolean isAbleToMove(final int dir) {
        final int yd = y + dir;
        return grid.isOpen(x + 1, yd) || grid.isOpen(x - 1, yd);
    }
    
    // Used to highlight allowed destinations for a piece that a player has selected to move
    protected final Set<Integer> getCapturableDestinations(final Set<Integer> destinations) {
        destinations.clear();
        for (final int dir : getAllowedDirections()) {
            addDestinationIfAbleToCapture(destinations, 1, dir);
            addDestinationIfAbleToCapture(destinations, -1, dir);
        }
        return destinations;
    }
    
    protected final Set<Integer> getAllowedDestinations(final Set<Integer> destinations) {
        getCapturableDestinations(destinations);
        if (!destinations.isEmpty()) {
            return destinations;
        }
        for (final int dir : getAllowedDirections()) {
            addDestinationIfAbleToMove(destinations, 1, dir);
            addDestinationIfAbleToMove(destinations, -1, dir);
        }
        return destinations;
    }
    
    private final void addDestinationIfAbleToCapture(final Set<Integer> destinations, final int xDir, final int yDir) {
        if (isAbleToCapture(xDir, yDir)) {
            destinations.add(grid.getIndexWrapped(x + (xDir * 2), y + (yDir * 2)));
        }
    }
    
    private final void addDestinationIfAbleToMove(final Set<Integer> destinations, final int xDir, final int yDir) {
        final int xd = x + xDir, yd = y + yDir;
        if (grid.isOpen(xd, yd)) {
            destinations.add(grid.getIndexWrapped(xd, yd));
        }
    }
    
    protected MoveResult moveToDestination(final int destination) {
        if (!grid.isOpen(destination)) {
            return CheckersModule.MOVE_ILLEGAL;
        }
        final Set<Integer> tmp = new HashSet<Integer>();
        CheckersModule.setMovablePieces(tmp, player);
        if (!tmp.contains(getIndexWrapped())) {
            return CheckersModule.MOVE_ILLEGAL;
        } else if (!getAllowedDestinations(tmp).contains(Integer.valueOf(destination))) {
            return CheckersModule.MOVE_ILLEGAL;
        }
        final int dx = grid.getX(destination), dy = grid.getY(destination);
        final boolean capturing = Math.abs(dx - x) > 1; // If moving more than 1 square, must be capturing an opponent piece
        if (capturing) {
            final int cx = (x + dx) / 2, cy = (y + dy) / 2;
            grid.remove(cx, cy);
        }
        grid.set(destination, this);
        final int[] allowedDirections = getAllowedDirections();
        if (allowedDirections.length == 1) {
            final int allowedDirection = allowedDirections[0];
            if (allowedDirection == 1) {
                if (y == (grid.getHeight() - 1)) {
                    crowned = true;
                }
            } else if (y == 0) {
                crowned = true;
            }
        }
        if (capturing) {
            final Set<Integer> extraCaptureDestinations = getCapturableDestinations(tmp);
            if (Coltil.isValued(extraCaptureDestinations)) {
                return new CheckersModule.MoveResult(true, extraCaptureDestinations);
            }
        }
        return CheckersModule.MOVE_SUCCESS;
    }
    
    @Override
    protected final Panmage getImage() {
        return crowned ? BoardGame.circles : BoardGame.circle;
    }
}
