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
import org.pandcorps.board.ChessPiece.*;
import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.pandam.*;

public class ChessModule extends BoardGameModule<ChessPiece> {
    private final static int BOARD_DIM = 8;
    protected final static BoardGameGrid<ChessPiece> grid = new BoardGameGrid<ChessPiece>(BOARD_DIM);
    private final static StringBuilder checkLabel = new StringBuilder();
    private static MenuButton promoteToKnight = null;
    private static MenuButton promoteToBishop = null;
    private static MenuButton promoteToRook = null;
    private static MenuButton promoteToQueen = null;
    
    @Override
    protected final void prepareGame() {
        BoardGame.BoardGameScreen.addText(checkLabel, Pangine.getEngine().getEffectiveHeight() - 36);
        preparePromotionButtons();
    }
    
    @Override
    protected final void initGame() {
        for (int x = 0; x < BOARD_DIM; x++) {
            grid.set(x, 1, new Pawn(0));
            grid.set(x, 6, new Pawn(1));
        }
        initRow(0, 0);
        initRow(7, 1);
    }
    
    private final void initRow(final int y, final int player) {
        grid.set(0, y, new Rook(player));
        grid.set(1, y, new Knight(player));
        grid.set(2, y, new Bishop(player));
        grid.set(3, y, new Queen(player));
        grid.set(4, y, new King(player));
        grid.set(5, y, new Bishop(player));
        grid.set(6, y, new Knight(player));
        grid.set(7, y, new Rook(player));
    }
    
    @Override
    protected final BoardGameGrid<ChessPiece> getGrid() {
        return grid;
    }
    
    @Override
    protected final BoardGameCell getCell(final int x, final int y) {
        return BoardGame.getPlayerSquareInverted(x, y);
    }
    
    @Override
    protected final Pancolor getDefaultColor(final int playerIndex) {
        return (playerIndex == 0) ? Pancolor.WHITE : BoardGame.BLACK;
    }
    
    @Override
    protected final BoardGameResult processTouch(final int cellIndex) {
        return processTouchToMove(cellIndex);
    }
    
    @Override
    protected final void highlightAllowedDestinations() {
        pieceToMove.setAllowedDestinations(BoardGame.highlightSquares);
    }
    
    @Override
    protected final void pickDestination(final int cellIndex) {
        if (pieceToMove.moveToDestination(cellIndex)) {
            finishTurn();
        } else {
            BoardGame.highlightSquares.clear();
        }
    }
    
    @Override
    protected final BoardGameResult getFinalResult() {
        if (BoardGame.isExtraMenuButtonAvailable()) {
            return null;
        } else if (isCurrentPlayerInCheck()) {
            return new BoardGameResult(BoardGame.RESULT_WIN, BoardGame.getNextPlayerIndex());
        }
        return BoardGameResult.newTie();
    }
    
    @Override
    protected final String getTieLabel() {
        return "Stalemate";
    }
    
    @Override
    protected final void onLoad() {
        initTurn();
    }
    
    @Override
    protected final char serialize(final ChessPiece piece) {
        return piece.serialize();
    }
    
    @Override
    protected final ChessPiece parse(final char value, final int player) {
        return ChessPiece.parse(value, player);
    }
    
    private final void initTurn() {
        BoardGame.clearExtraMenuButtons();
        highlightMovablePieces();
        pieceToMove = null;
        Chartil.clear(checkLabel);
        if (isCurrentPlayerInCheck()) {
            checkLabel.append(BoardGame.highlightSquares.isEmpty() ? "Checkmate" : "Check");
        }
    }
    
    protected final void finishTurn() {
        BoardGame.toggleCurrentPlayer();
        initTurn();
    }
    
    private final void highlightMovablePieces() {
        BoardGame.highlightSquares.clear();
        final Set<Integer> set = new HashSet<Integer>();
        for (final ChessPiece piece : grid.getPieces(currentPlayerIndex)) {
            piece.setAllowedDestinations(set);
            if (!set.isEmpty()) {
                BoardGame.highlightSquares.add(piece.getIndexWrapped());
            }
        }
    }
    
    protected final boolean isCurrentPlayerInCheck() {
        return isPlayerInCheck(currentPlayerIndex);
    }
    
    protected final static boolean isPlayerInCheck(final int player) {
        final Set<Integer> set = new HashSet<Integer>();
        Integer kingIndex = null;
        for (final ChessPiece piece : new ArrayList<ChessPiece>(grid.grid)) {
            if (piece == null) {
                continue;
            } else if (piece.player == player) {
                if (piece instanceof King) {
                    kingIndex = piece.getIndexWrapped();
                }
            } else {
                piece.addAllowedDestinations(set);
            }
            if ((kingIndex != null) && set.contains(kingIndex)) {
                return true;
            }
        }
        return false;
    }
    
    private final static MenuButton newPromotionButton(final int x, final Panmage img, final char pieceType) {
        return new MenuButton(x, 5, img) {
            @Override
            protected final void onTouch() {
                final ChessPiece oldPiece = BoardGame.CHESS.pieceToMove;
                grid.set(oldPiece.x, oldPiece.y, BoardGame.CHESS.parse(pieceType, oldPiece.player));
                BoardGame.CHESS.finishTurn();
            }
        };
    }
    
    private final static void preparePromotionButtons() {
        if (promoteToQueen != null) {
            return;
        }
        promoteToKnight = newPromotionButton(9, BoardGame.knight, ChessPiece.VALUE_KNIGHT);
        promoteToBishop = newPromotionButton(10, BoardGame.bishop, ChessPiece.VALUE_BISHOP);
        promoteToRook = newPromotionButton(11, BoardGame.rook, ChessPiece.VALUE_ROOK);
        promoteToQueen = newPromotionButton(12, BoardGame.queen, ChessPiece.VALUE_QUEEN);
    }
    
    protected final void addPromotionButtons() {
        BoardGame.addExtraMenuButton(promoteToKnight);
        BoardGame.addExtraMenuButton(promoteToBishop);
        BoardGame.addExtraMenuButton(promoteToRook);
        BoardGame.addExtraMenuButton(promoteToQueen);
    }
}
