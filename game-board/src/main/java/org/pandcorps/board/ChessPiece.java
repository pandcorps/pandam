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
import org.pandcorps.core.*;
import org.pandcorps.pandam.*;

public abstract class ChessPiece extends BoardGamePiece {
    protected final static char VALUE_PAWN = 'P';
    protected final static char VALUE_ROOK = 'R';
    protected final static char VALUE_KNIGHT = 'N'; // K used for King
    protected final static char VALUE_BISHOP = 'B';
    protected final static char VALUE_QUEEN = 'Q';
    protected final static char VALUE_KING = 'K';
    
    private final static BoardGameGrid<ChessPiece> grid = ChessModule.grid;
    
    protected boolean moved = false;
    
    protected ChessPiece(final int player) {
        super(player);
    }
    
    protected char serialize() {
        final char value = getValue();
        return moved ? value : Chartil.toLowerCase(value);
    }
    
    protected abstract char getValue();
    
    protected final void setAllowedDestinations(final Set<Integer> set) {
        set.clear();
        addAllowedDestinations(set);
    }
    
    protected abstract void addAllowedDestinations(final Set<Integer> set);
    
    protected final void add(final Set<Integer> set, final int index) {
        if (grid.isValid(index)) {
            final ChessPiece piece = grid.get(index);
            if ((piece == null) || (piece.player != player)) {
                if ((player != BoardGame.module.currentPlayerIndex) || !wouldBeInCheck(index)) {
                    set.add(Integer.valueOf(index));
                }
            }
        }
    }
    
    protected final void add(final Set<Integer> set, final int x, final int y) {
        add(set, grid.getIndexOptional(x, y));
    }
    
    protected final boolean addIfEmpty(final Set<Integer> set, final int x, final int y) {
        final int index = grid.getIndexOptional(x, y);
        if (grid.isValid(index) && grid.get(index) == null) {
            add(set, index);
            return true;
        }
        return false;
    }
    
    protected final boolean addIfOccupiedByOpponent(final Set<Integer> set, final int x, final int y) {
        final int index = grid.getIndexOptional(x, y);
        if (!grid.isValid(index)) {
            return false;
        }
        final ChessPiece piece = grid.get(index);
        if ((piece != null) && (piece.player != player)) {
            add(set, index);
            return true;
        }
        return false;
    }
    
    protected final void addLine(final Set<Integer> set, final int xd, final int yd) {
        int xc = x, yc = y;
        while (true) {
            xc += xd; yc += yd;
            final int index = grid.getIndexOptional(xc, yc);
            if (!grid.isValid(index)) {
                break;
            }
            final ChessPiece piece = grid.get(index);
            if (piece == null) {
                add(set, index);
            } else {
                if (piece.player != player) {
                    add(set, index);
                }
                break;
            }
        }
    }
    
    protected final void addRookDestinations(final Set<Integer> set) {
        addLine(set, -1, 0);
        addLine(set, 1, 0);
        addLine(set, 0, -1);
        addLine(set, 0, 1);
    }
    
    protected final void addBishopDestinations(final Set<Integer> set) {
        addLine(set, -1, -1);
        addLine(set, -1, 1);
        addLine(set, 1, -1);
        addLine(set, 1, 1);
    }
    
    protected final boolean wouldBeInCheck(final int dstIndex) {
        return wouldBeInCheck(grid.getX(dstIndex), grid.getY(dstIndex));
    }
    
    protected final boolean wouldBeInCheck(final int dx, final int dy) {
        if ((dx == x) && (dy == y)) {
            return isThisPlayerInCheck();
        }
        final int ox = x, oy = y;
        final ChessPiece oldPiece = grid.get(dx, dy);
        try {
            grid.set(dx, dy, this);
            return isThisPlayerInCheck();
        } finally {
            grid.set(ox, oy, this);
            grid.set(dx, dy, oldPiece);
        }
    }
    
    protected final boolean isThisPlayerInCheck() {
        return ChessModule.isPlayerInCheck(player);
    }
    
    protected boolean moveToDestination(final int cellIndex) {
        setLastCapturedPiece(grid.set(cellIndex, this));
        moved = true;
        return true;
    }
    
    protected final void setLastCapturedPiece(final ChessPiece piece) {
        BoardGame.CHESS.lastCapturedPiece = BoardGame.CHESS.copy(piece);
    }
    
    protected final static ChessPiece parse(final char value, final int player) {
        final char upper = Chartil.toUpperCase(value);
        final ChessPiece piece;
        if (upper == VALUE_PAWN) {
            piece = new Pawn(player);
        } else if (upper == VALUE_ROOK) {
            piece = new Rook(player);
        } else if (upper == VALUE_KNIGHT) {
            piece = new Knight(player);
        } else if (upper == VALUE_BISHOP) {
            piece = new Bishop(player);
        } else if (upper == VALUE_QUEEN) {
            piece = new Queen(player);
        } else if (upper == VALUE_KING) {
            piece = new King(player);
        } else {
            throw new IllegalArgumentException("Unknown ChessPiece value " + value);
        }
        piece.moved = Chartil.isUpperCase(value) ? true : false;
        return piece;
    }
    
    @Override
    public final String toString() {
        return getClass().getSimpleName() + " (" + x + ", " + y + ")";
    }
    
    protected final static class Pawn extends ChessPiece {
        protected Pawn(final int player) {
            super(player);
        }
        
        @Override
        protected final char getValue() {
            return VALUE_PAWN;
        }
        
        @Override
        protected final void addAllowedDestinations(final Set<Integer> set) {
            final int dir = getDirection(), yd = y + dir;
            final boolean empty = addIfEmpty(set, x, yd);
            if (empty && !moved) {
                addIfEmpty(set, x, y + (dir * 2));
            }
            addDiagonalDestination(set, x - 1, yd);
            addDiagonalDestination(set, x + 1, yd);
        }
        
        private final void addDiagonalDestination(final Set<Integer> set, final int xd, final int yd) {
            if (addIfOccupiedByOpponent(set, xd, yd)) {
                return;
            }
            // Check for opportunity to capture a pawn en passant
            final BoardGamePiece neighbor = grid.get(xd, y);
            if ((neighbor == null) || (neighbor.player == player) || !(neighbor instanceof Pawn) || !((Pawn) neighbor).isVulnerableToCaptureEnPassant()) {
                return;
            }
            add(set, xd, yd);
        }
        
        @Override
        protected final boolean moveToDestination(final int cellIndex) {
            final int oldX = x, oldY = y;
            super.moveToDestination(cellIndex);
            if ((x != oldX) && (BoardGame.CHESS.lastCapturedPiece == null)) {
                // If moved diagonally without capturing, then must be capturing en passant
                setLastCapturedPiece(grid.remove(x, oldY));
            }
            if (grid.isValid(x, y + getDirection())) {
                return true;
            }
            BoardGame.CHESS.addPromotionButtons();
            return false;
        }
        
        @Override
        protected final Panmage getImage() {
            return BoardGame.pawn;
        }
        
        protected final int getDirection() {
            //return (player == 0) ? 1 : -1; // Correct if grid isn't reversed when toggling player
            return (player == BoardGame.CHESS.currentPlayerIndex) ? 1 : -1; // Current player always moving upward
        }
        
        protected final boolean isVulnerableToCaptureEnPassant() {
            if (player == BoardGame.CHESS.currentPlayerIndex) {
                return false;
            } else if (Coltil.size(BoardGame.CHESS.pieceToMoveTurnIndices) == 2) {
                return isVulnerableToCaptureEnPassant(BoardGame.CHESS.pieceToMoveTurnIndices);
            }
            final BoardGameState<ChessPiece> state = grid.getCurrentState();
            if (state == null) {
                return false;
            }
            final List<Integer> turnIndices = state.previousTurnIndices;
            if (Coltil.size(turnIndices) != 2) {
                return false; // No previous turn; this must be first turn
            }
            return isVulnerableToCaptureEnPassant(turnIndices);
        }
        
        private final boolean isVulnerableToCaptureEnPassant(final List<Integer> turnIndices) {
            if (getIndexRequired() != turnIndices.get(1).intValue()) {
                return false; // Previous turn didn't end here, so this piece wasn't moved last turn
            }
            final int prevY = grid.getY(turnIndices.get(0));
            final int distance = Math.abs(y - prevY);
            return distance == 2;
        }
    }
    
    protected final static class Rook extends ChessPiece {
        protected Rook(final int player) {
            super(player);
        }
        
        @Override
        protected final char getValue() {
            return VALUE_ROOK;
        }
        
        @Override
        protected final void addAllowedDestinations(final Set<Integer> set) {
            addRookDestinations(set);
        }
        
        @Override
        protected final Panmage getImage() {
            return BoardGame.rook;
        }
    }
    
    protected final static class Knight extends ChessPiece {
        protected Knight(final int player) {
            super(player);
        }
        
        @Override
        protected final char getValue() {
            return VALUE_KNIGHT;
        }
        
        @Override
        protected final void addAllowedDestinations(final Set<Integer> set) {
            add(set, x - 2, y - 1);
            add(set, x - 2, y + 1);
            add(set, x - 1, y - 2);
            add(set, x - 1, y + 2);
            add(set, x + 1, y - 2);
            add(set, x + 1, y + 2);
            add(set, x + 2, y - 1);
            add(set, x + 2, y + 1);
        }
        
        @Override
        protected final Panmage getImage() {
            return BoardGame.knight;
        }
    }
    
    protected final static class Bishop extends ChessPiece {
        protected Bishop(final int player) {
            super(player);
        }
        
        @Override
        protected final char getValue() {
            return VALUE_BISHOP;
        }
        
        @Override
        protected final void addAllowedDestinations(final Set<Integer> set) {
            addBishopDestinations(set);
        }
        
        @Override
        protected final Panmage getImage() {
            return BoardGame.bishop;
        }
    }
    
    protected final static class Queen extends ChessPiece {
        protected Queen(final int player) {
            super(player);
        }
        
        @Override
        protected final char getValue() {
            return VALUE_QUEEN;
        }
        
        @Override
        protected final void addAllowedDestinations(final Set<Integer> set) {
            addRookDestinations(set);
            addBishopDestinations(set);
        }
        
        @Override
        protected final Panmage getImage() {
            return BoardGame.queen;
        }
    }
    
    protected final static class King extends ChessPiece {
        protected King(final int player) {
            super(player);
        }
        
        @Override
        protected final char getValue() {
            return VALUE_KING;
        }
        
        @Override
        protected final void addAllowedDestinations(final Set<Integer> set) {
            final int xm = x - 1, xp = x + 1, ym = y - 1, yp = y + 1;
            add(set, xm, ym);
            add(set, xm, y);
            add(set, xm, yp);
            add(set, x,  ym);
            add(set, x,  yp);
            add(set, xp, ym);
            add(set, xp, y);
            add(set, xp, yp);
            addCastling(set, 2, 0);
            addCastling(set, 6, 7);
        }
        
        private final void addCastling(final Set<Integer> set, final int dx, final int rx) {
            // Assert that King has not moved (and is not in the middle of evaluating a potential move)
            if (moved || (x != 4)) {
                return;
            }
            final ChessPiece rook = grid.get(rx, y);
            // Assert that Rook has not moved
            if ((rook == null) || (rook.player != player) || !(rook instanceof Rook) || rook.moved) {
                return;
            }
            // Assert that no pieces are between the King and Rook
            if (isAnyPieceBetween(rx)) {
                return;
            }
            // Assert that King is not in check before/during/after move
            if (isAnyInCheck(dx)) {
                return;
            }
            add(set, dx, y);
        }
        
        private final boolean isAnyPieceBetween(final int rx) {
            final int left = Math.min(x, rx), right = Math.max(x, rx);
            for (int bx = left + 1; bx < right; bx++) {
                if (grid.get(bx, y) != null) {
                    return true;
                }
            }
            return false;
        }
        
        private final boolean isAnyInCheck(final int dx) {
            final int left = Math.min(x, dx), right = Math.max(x, dx);
            final boolean oldMoved = moved;
            try {
                moved = true;
                for (int bx = left; bx <= right; bx++) {
                    if (wouldBeInCheck(bx, y)) {
                        return true;
                    }
                }
            } finally {
                moved = oldMoved;
            }
            return false;
        }
        
        @Override
        protected final boolean moveToDestination(final int cellIndex) {
            final int oldX = x;
            super.moveToDestination(cellIndex);
            if (Math.abs(x - oldX) > 1) { // Must be castling if moving more than 1 cell
                final int rookOldX, rookNewX;
                if (x > oldX) { // Moving to the right (kingside)
                    rookOldX = 7;
                    rookNewX = oldX + 1;
                } else { // Moving to the left (queenside)
                    rookOldX = 0;
                    rookNewX = oldX - 1;
                }
                final ChessPiece rook = grid.get(rookOldX, y);
                rook.moveToDestination(grid.getIndexRequired(rookNewX, y));
            }
            return true;
        }
        
        @Override
        protected final Panmage getImage() {
            return BoardGame.king;
        }
    }
}
