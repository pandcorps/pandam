/*
Copyright (c) 2009-2021, Andrew M. Martin
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
import org.pandcorps.core.img.*;

public class CheckersModule extends BoardGameModule<CheckersPiece> {
    private final static int BOARD_DIM = 8;
    protected final static BoardGameGrid<CheckersPiece> grid = new BoardGameGrid<CheckersPiece>(BOARD_DIM);
    
    @Override
    protected final void initGame() {
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
    
    private final void initTurn() {
        highlightMovablePieces();
        pieceToMove = null;
    }
    
    @Override
    protected final BoardGameGrid<CheckersPiece> getGrid() {
        return grid;
    }
    
    @Override
    protected final BoardGameCell getCell(final int x, final int y) {
        return BoardGame.getPlayerSquare(x, y);
    }
    
    @Override
    protected final Pancolor getDefaultColor(final int playerIndex) {
        return (playerIndex == 0) ? BoardGame.BLACK : Pancolor.RED;
    }
    
    @Override
    protected final BoardGameResult processTouch(final int cellIndex) {
        return processTouchToMove(cellIndex);
    }
    
    @Override
    protected final void pickDestination(final int cellIndex) {
        final MoveResult result = pieceToMove.moveToDestination(cellIndex);
        if (result.success) {
            addTurnIndex(cellIndex);
            if (Coltil.isValued(result.extraCaptureDestinations)) {
                BoardGame.setHighlightSquares(result.extraCaptureDestinations);
                return;
            }
            BoardGame.toggleCurrentPlayer();
        }
        initTurn();
    }
    
    @Override
    protected final void onLoad() {
        initTurn();
    }
    
    @Override
    protected final char serialize(final CheckersPiece piece) {
        return piece.crowned ? 'C' : 'U';
    }
    
    @Override
    protected final CheckersPiece parse(final char value, final int player) {
        final CheckersPiece piece = new CheckersPiece(player);
        if (value == 'C') {
            piece.crowned = true;
        }
        return piece;
    }
    
    protected final void highlightMovablePieces() {
        setMovablePieces(BoardGame.highlightSquares, currentPlayerIndex);
    }
    
    @Override
    protected final void highlightAllowedDestinations() {
        pieceToMove.getAllowedDestinations(BoardGame.highlightSquares);
    }
    
    // Used to highlight pieces that the player can select (an empty List would mean that the game is over)
    protected final static void setMovablePieces(final Set<Integer> movable, final int player) {
        movable.clear();
        final List<CheckersPiece> all = grid.getPieces(player);
        for (final CheckersPiece piece : all) {
            if (piece.isAbleToCapture()) {
                movable.add(piece.getIndexWrapped());
            }
        }
        if (!movable.isEmpty()) {
            return;
        }
        for (final CheckersPiece piece : all) {
            if (piece.isAbleToMove()) {
                movable.add(piece.getIndexWrapped());
            }
        }
        return;
    }
    
    @Override
    protected final BoardGameResult getFinalResult() {
        final Set<Integer> players = new HashSet<Integer>(2);
        for (final CheckersPiece piece : grid.grid) {
            if (piece == null) {
                continue;
            }
            players.add(Integer.valueOf(piece.player));
            if (players.size() == 2) {
                /*
                A player forfeits if there are no possible moves.
                Player has already been toggled at this point.
                So current player forfeits in this scenario, and the other player is the winner.
                */
                return new BoardGameResult(BoardGame.RESULT_WIN, BoardGame.getNextPlayerIndex());
            }
        }
        return new BoardGameResult(BoardGame.RESULT_WIN, players.iterator().next().intValue());
    }
    
    @Override
    protected final boolean isReverseRequired() {
        return true;
    }
    
    protected final static class MoveResult {
        private final boolean success;
        private final Set<Integer> extraCaptureDestinations;
        
        protected MoveResult(final boolean success, final Set<Integer> extraCaptureDestinations) {
            this.success = success;
            this.extraCaptureDestinations = extraCaptureDestinations;
        }
    }
    
    protected final static MoveResult MOVE_SUCCESS = new MoveResult(true, null);
    protected final static MoveResult MOVE_ILLEGAL = new MoveResult(false, null);
}
