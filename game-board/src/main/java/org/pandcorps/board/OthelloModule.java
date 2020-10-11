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
import org.pandcorps.core.img.*;
import org.pandcorps.pandam.*;

public class OthelloModule extends BoardGameModule<OthelloPiece> {
    private final static int BOARD_DIM = 8;
    protected final static BoardGameGrid<OthelloPiece> grid = new BoardGameGrid<OthelloPiece>(BOARD_DIM);
    private static BoardGameCell cellBackground = null;
    
    protected OthelloModule() {
        super(BOARD_DIM);
    }
    
    @Override
    protected final void initGame() {
        grid.set(3, 3, new OthelloPiece(0));
        grid.set(4, 4, new OthelloPiece(0));
        grid.set(3, 4, new OthelloPiece(1));
        grid.set(4, 3, new OthelloPiece(1));
    }
    
    @Override
    protected final BoardGameGrid<OthelloPiece> getGrid() {
        return grid;
    }
    
    @Override
    protected final void pickColors() {
        boolean defaultColors = true;
        for (final BoardGamePlayer player : players) {
            if (player.profile.color1 != null) {
                defaultColors = false;
                break;
            }
        }
        final Pancolor color = defaultColors ? Pancolor.GREEN : BoardGame.pickNonPlayerColor();
        cellBackground = new BoardGameCell() {
            @Override public final Panmage getImage() { return BoardGame.square; }
            @Override public final Pancolor getColor() { return color; }};
    }
    
    @Override
    protected final BoardGameCell getCell(final int x, final int y) {
        if (BoardGame.isHighlightSquare(x, y)) {
            return BoardGame.squareC;
        }
        return cellBackground;
    }
    
    @Override
    protected final Pancolor getDefaultColor(final int playerIndex) {
        return (playerIndex == 0) ? BoardGame.BLACK : Pancolor.WHITE;
    }
    
    @Override
    protected final BoardGameResult processTouch(final int cellIndex) {
        if (!BoardGame.isHighlight(cellIndex)) {
            return null;
        }
        grid.set(cellIndex, new OthelloPiece(currentPlayerIndex));
        flipOutflankedPieces(grid.getX(cellIndex), grid.getY(cellIndex));
        try {
            for (int i = 0; i < numPlayers; i++) {
                // If a player's turn is skipped, just peak so that no state is added for the skipped turn
                BoardGame.toggleCurrentPlayerPeak();
                highlightPossibleCells();
                if (!BoardGame.highlightSquares.isEmpty()) {
                    return null;
                }
            }
            return getFinalResult();
        } finally {
            BoardGame.toggleCurrentPlayerCommit();
        }
    }
    
    @Override
    protected final void onLoad() {
        highlightPossibleCells();
    }
    
    @Override
    protected final char serialize(final OthelloPiece piece) {
        return 'O';
    }
    
    @Override
    protected final OthelloPiece parse(final char value, final int player) {
        return new OthelloPiece(player);
    }
    
    private final void highlightPossibleCells() {
        BoardGame.highlightSquares.clear();
        getPossibleCells(currentPlayerIndex, BoardGame.highlightSquares);
    }
    
    private final void getPossibleCells(final int player, final Set<Integer> set) {
        final int w = grid.getWidth(), h = grid.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (isPossibleCell(x, y)) {
                    set.add(grid.getIndexWrapped(x, y));
                }
            }
        }
    }
    
    private final boolean isPossibleCell(final int x, final int y) {
        final OthelloPiece piece = grid.get(x, y);
        if (piece != null) {
            return false;
        }
        for (int yd = -1; yd <= 1; yd++) {
            for (int xd = -1; xd <= 1; xd++) {
                if ((xd == 0) && (yd == 0)) {
                    continue;
                } else if (isOpponentAndOutflankable(x, y, xd, yd)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private final boolean isOpponentAndOutflankable(final int x, final int y, final int xd, final int yd) {
        final int xo = x + xd, yo = y + yd;
        final OthelloPiece piece = grid.get(xo, yo);
        if (piece == null) {
            return false;
        } else if (piece.player == currentPlayerIndex) {
            return false;
        }
        return isCurrentPlayerAlongLine(xo, yo, xd, yd);
    }
    
    private final boolean isCurrentPlayerAlongLine(final int xo, final int yo, final int xd, final int yd) {
        int x = xo, y = yo;
        while (true) {
            x += xd;
            y += yd;
            final OthelloPiece piece = grid.get(x, y);
            if (piece == null) {
                return false;
            } else if (piece.player == currentPlayerIndex) {
                return true;
            }
        }
    }
    
    private final void flipOutflankedPieces(final int x, final int y) {
        for (int yd = -1; yd <= 1; yd++) {
            for (int xd = -1; xd <= 1; xd++) {
                if ((xd == 0) && (yd == 0)) {
                    continue;
                }
                flipOutflankedPieces(x, y, xd, yd);
            }
        }
    }
    
    private final void flipOutflankedPieces(final int x, final int y, final int xd, final int yd) {
        if (!isOpponentAndOutflankable(x, y, xd, yd)) {
            return;
        }
        int xo = x;
        int yo = y;
        while (true) {
            xo += xd;
            yo += yd;
            final OthelloPiece piece = grid.get(xo, yo);
            if (piece.player == currentPlayerIndex) {
                return;
            }
            piece.player = currentPlayerIndex;
        }
    }
    
    protected final BoardGameResult getFinalResult() {
        final int[] counts = new int[numPlayers];
        final int w = grid.getWidth(), h = grid.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final OthelloPiece piece = grid.get(x, y);
                if (piece == null) {
                    continue;
                }
                counts[piece.player]++;
            }
        }
        int maxPlayer = -1, maxCount = -1;
        for (int playerIndex = 0; playerIndex < numPlayers; playerIndex++) {
            final int count = counts[playerIndex];
            if (count == maxCount) {
                return BoardGameResult.newTie();
            } else if (count > maxCount) {
                maxPlayer = playerIndex;
                maxCount = count;
            }
        }
        return new BoardGameResult(BoardGame.RESULT_WIN, maxPlayer);
    }
}
