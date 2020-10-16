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

import org.pandcorps.board.BoardGame.*;
import org.pandcorps.core.img.*;
import org.pandcorps.pandam.*;

public class FourInARowModule extends BoardGameModule<FourInARowPiece> {
    private final static int BOARD_WIDTH = 7;
    private final static int BOARD_HEIGHT = 6;
    private final static int MAX_PIECES = BOARD_WIDTH * BOARD_HEIGHT;
    private final static int VICTORY_LENGTH = 4;
    private final static int NULL_PLAYER = -1;
    private final static int TIE_PLAYER = -2;
    protected final static Pancolor COLOR_BG = new FinPancolor(0, 128, Pancolor.MAX_VALUE);
    protected final static BoardGameGrid<FourInARowPiece> grid = new BoardGameGrid<FourInARowPiece>(BOARD_WIDTH, BOARD_HEIGHT);
    private static BoardGameCell cellBackground = null;
    
    @Override
    protected final void initGame() {
    }
    
    @Override
    protected final BoardGameGrid<FourInARowPiece> getGrid() {
        return grid;
    }
    
    @Override
    protected final void pickColors() {
        final Pancolor color = isEachPlayerDefaultColor() ? COLOR_BG : BoardGame.pickNonPlayerColor();
        cellBackground = new BoardGameCell() {
            @Override public final Panmage getImage() { return BoardGame.verticalSquare; }
            @Override public final Pancolor getColor() { return color; }};
    }
    
    @Override
    protected final BoardGameCell getCell(final int x, final int y) {
        if (BoardGame.isHighlightSquare(x, y)) {
            return BoardGame.verticalSquareW;
        }
        return cellBackground;
    }
    
    @Override
    protected final Pancolor getDefaultColor(final int playerIndex) {
        return (playerIndex == 0) ? Pancolor.YELLOW : Pancolor.RED;
    }
    
    @Override
    protected final BoardGameResult processTouch(final int cellIndex) {
        final int x = grid.getX(cellIndex);
        int y = 0;
        while (true) {
            final int lowIndex = grid.getIndexOptional(x, y);
            if (!grid.isValid(lowIndex)) {
                return null;
            } else if (grid.get(lowIndex) == null) {
                grid.set(lowIndex, new FourInARowPiece(currentPlayerIndex));
                BoardGame.toggleCurrentPlayer();
                final int winner = highlightVictory();
                if (winner == NULL_PLAYER) {
                    return null;
                } else if (winner == TIE_PLAYER) {
                    return BoardGameResult.newTie();
                }
                return new BoardGameResult(BoardGame.RESULT_WIN, winner);
            }
            y++;
        }
    }
    
    @Override
    protected final void onLoad() {
        highlightVictory();
    }
    
    @Override
    protected final char serialize(final FourInARowPiece piece) {
        return 'F';
    }
    
    @Override
    protected final FourInARowPiece parse(final char value, final int player) {
        return new FourInARowPiece(player);
    }
    
    private final int highlightVictory() {
        BoardGame.highlightSquares.clear();
        int winner = NULL_PLAYER, pieces = 0;
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                final FourInARowPiece piece = grid.get(x, y);
                if (piece == null) {
                    continue;
                }
                pieces++;
                final int player = piece.player;
                winner = highlightVictoryRow(player, x, y, 1, 0, winner);
                winner = highlightVictoryRow(player, x, y, 1, 1, winner);
                winner = highlightVictoryRow(player, x, y, 0, 1, winner);
                winner = highlightVictoryRow(player, x, y, -1, 1, winner);
            }
        }
        if (winner == NULL_PLAYER) {
            return (pieces == MAX_PIECES) ? TIE_PLAYER : NULL_PLAYER;
        }
        return winner;
    }
    
    private final int highlightVictoryRow(final int player, final int x, final int y, final int dx, final int dy, final int winner) {
        for (int i = 1; i < VICTORY_LENGTH; i++) {
            final FourInARowPiece piece = grid.get(x + (dx * i), y + (dy * i));
            if ((piece == null) || (piece.player != player)) {
                return winner;
            }
        }
        for (int i = 0; i < VICTORY_LENGTH; i++) {
            BoardGame.highlightSquares.add(grid.getIndexWrapped(x + (dx * i), y + (dy * i)));
        }
        if ((player != winner) && (winner != NULL_PLAYER)) {
            throw new IllegalStateException("Players " + player + " and " + winner + " both won");
        }
        return player;
    }
}
