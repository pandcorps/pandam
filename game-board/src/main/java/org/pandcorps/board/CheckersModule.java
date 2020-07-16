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

public class CheckersModule extends BoardGameModule {
    private final static int BOARD_DIM = 8;
    private final BoardGameGrid<CheckersPiece> grid = new BoardGameGrid<CheckersPiece>(BOARD_DIM);
    
    @Override
    protected final void initGame() {
        grid.clear();
        // (0, 0) is bottom left
        for (int y = 0; y < 3; y++) {
            final int xStart = (y == 1) ? 1 : 0;
            for (int x = xStart; x < BOARD_DIM; x += 2) {
                grid.set(x, y, new CheckersPiece(0));
            }
        }
        for (int y = 5; y < 8; y++) {
            final int xStart = (y == 6) ? 0 : 1;
            for (int x = xStart; x < BOARD_DIM; x += 2) {
                grid.set(x, y, new CheckersPiece(1));
            }
        }
    }
    
    // Used to highlight pieces that the player can select (an empty List would mean that the game is over)
    protected List<CheckersPiece> getMovablePieces(final int player) {
        final List<CheckersPiece> all = grid.getPieces(player);
        final List<CheckersPiece> movable = new ArrayList<CheckersPiece>();
        for (final CheckersPiece piece : all) {
            if (piece.isAbleToCapture()) {
                movable.add(piece);
            }
        }
        if (!movable.isEmpty()) {
            return movable;
        }
        for (final CheckersPiece piece : all) {
            if (piece.isAbleToMove()) {
                movable.add(piece);
            }
        }
        return movable;
    }
    
    private final class CheckersPiece extends BoardGamePiece {
        private final int[] DIRECTIONS_0 = { 1 };
        private final int[] DIRECTIONS_1 = { -1 };
        private final int[] DIRECTIONS_CROWNED = { 1, -1 };
        private boolean crowned = false;
        
        private CheckersPiece(final int player) {
            super(player);
        }
        
        /*private final int getOriginalDirection() {
            return (player == 0) ? 1 : -1;
        }*/
        
        private final int[] getAllowedDirections() {
            if (crowned) {
                return DIRECTIONS_CROWNED;
            }
            return (player == 0) ? DIRECTIONS_0 : DIRECTIONS_1;
        }
        
        private final boolean isAbleToCapture() {
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
        
        private final boolean isAbleToMove() {
            /*final int dir = getOriginalDirection();
            if (isAbleToMove(dir)) {
                return true;
            } else if (crowned && isAbleToMove(dir * -1)) {
                return true;
            }
            return false;*/
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
        protected final List<Integer> getAllowedDestinations() {
            final List<Integer> destinations = new ArrayList<Integer>();
            final int[] dirs = getAllowedDirections();
            for (final int dir : dirs) {
                addDestinationIfAbleToCapture(destinations, 1, dir);
                addDestinationIfAbleToCapture(destinations, -1, dir);
            }
            if (!destinations.isEmpty()) {
                return destinations;
            }
            for (final int dir : dirs) {
                addDestinationIfAbleToMove(destinations, 1, dir);
                addDestinationIfAbleToMove(destinations, -1, dir);
            }
            return destinations;
        }
        
        private final void addDestinationIfAbleToCapture(final List<Integer> destinations, final int xDir, final int yDir) {
            if (isAbleToCapture(xDir, yDir)) {
                destinations.add(grid.getIndexWrapped(x + (xDir * 2), y + (yDir * 2)));
            }
        }
        
        private final void addDestinationIfAbleToMove(final List<Integer> destinations, final int xDir, final int yDir) {
            final int xd = x + xDir, yd = y + yDir;
            if (grid.isOpen(xd, yd)) {
                destinations.add(grid.getIndexWrapped(xd, yd));
            }
        }
        
        protected boolean moveToDestination(final int destination) {
            if (!grid.isOpen(destination)) {
                return false;
            } else if (!getMovablePieces(player).contains(this)) {
                return false;
            } else if (!getAllowedDestinations().contains(Integer.valueOf(destination))) {
                return false;
            }
            // Capture
            grid.set(destination, this);
            // Double jumps
            return true;
        }
    }
}
