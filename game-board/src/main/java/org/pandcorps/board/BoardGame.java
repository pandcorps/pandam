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

import org.pandcorps.game.*;
import org.pandcorps.pandam.*;

public class BoardGame extends BaseGame {
    @Override
    protected final void init(final Panroom room) throws Exception {
    }
    
    protected abstract static class BoardGameModule {
        protected abstract void initGame();
    }
    
    protected final static int convertScreenToGrid(final int screenVal, final int gridLim) {
        return screenVal / gridLim;
    }
    
    protected static class BoardGameGrid<P extends BoardGamePiece> {
        private final int w;
        private final int h;
        private final int numCells;
        private final List<P> grid;
        private final Map<Integer, BoardGamePieceList<P>> lists = new HashMap<Integer, BoardGamePieceList<P>>();
        
        protected BoardGameGrid(final int dim) {
            this(dim, dim);
        }
        
        protected BoardGameGrid(final int w, final int h) {
            this.w = w;
            this.h = h;
            numCells = w * h;
            grid = new ArrayList<P>(numCells);
            for (int i = 0; i < numCells; i++) {
                grid.add(null);
            }
        }
        
        protected final void clear() {
            for (int i = 0; i < numCells; i++) {
                grid.set(i, null);
            }
        }
        
        protected final int getWidth() {
            return w;
        }
        
        protected final int getHeight() {
            return h;
        }
        
        protected final List<P> getPieces(final int player) {
            final Integer key = Integer.valueOf(player);
            BoardGamePieceList<P> list = lists.get(key);
            if (list == null) {
                list = new BoardGamePieceList<P>();
                lists.put(key, list);
            }
            // Maybe track grid age in set method and BoardGamePieceList; if not stale, return externalList without rebuilding; might call this multiple times per turn
            final List<P> internal = list.internalList;
            for (final P piece : grid) {
                if ((piece != null) && (piece.player == player)) {
                    internal.add(piece);
                }
            }
            return list.externalList;
        }
        
        protected final P get(final int x, final int y) {
            return isValid(x, y) ? grid.get(getIndex(x, y)) : null;
        }
        
        protected final void set(final int x, final int y, final P piece) {
            validate(x, y);
            // Null out previous location here? Separate move method to do that?
            grid.set(getIndex(x, y), piece);
            piece.x = x;
            piece.y = y;
        }
        
        protected final void set(final int index, final P piece) {
            grid.set(index, piece);
            piece.x = getX(index);
            piece.y = getY(index);
        }
        
        /*protected final void set(final BoardGameCell cell, final P piece) {
            set(cell.x, cell.y, piece);
        }*/
        
        protected final boolean isOpen(final int x, final int y) {
            return isValid(x, y) && (get(x, y) == null);
        }
        
        protected final boolean isOpen(final int index) {
            return grid.get(index) == null;
        }
        
        /*protected final boolean isOpen(final BoardGameCell cell) {
            return isOpen(cell.x, cell.y);
        }*/
        
        protected final boolean isValid(final int x, final int y) {
            return (x >= 0) && (x < w) && (y >= 0) && (y < h);
        }
        
        protected final void validate(final int x, final int y) {
            if (!isValid(x, y)) {
                throw new IllegalArgumentException("Invalid position: " + x + ", " + y);
            }
        }
        
        protected final int getIndex(final int x, final int y) {
            validate(x, y);
            return (y * w) + x;
        }
        
        protected final Integer getIndexWrapped(final int x, final int y) {
            return Integer.valueOf(getIndex(x, y));
        }
        
        protected final int getX(final int index) {
            return index % w;
        }
        
        protected final int getY(final int index) {
            return index / w;
        }
        
        protected final int convertScreenToGridX(final int screenX) {
            return convertScreenToGrid(screenX, w);
        }
        
        protected final int convertScreenToGridY(final int screenY) {
            return convertScreenToGrid(screenY, h);
        }
    }
    
    // Just use Grid.getIndex instead
    /*protected final static class BoardGameCell {
        protected final int x;
        protected final int y;
        
        protected BoardGameCell(final int x, final int y) {
            this.x = x;
            this.y = y;
        }
        
        public final boolean equals(final Object o) {
            if (o == this) {
                return true;
            } else if (o == null) {
                return false;
            } else if (o.getClass() != BoardGameCell.class) {
                return false;
            }
            final BoardGameCell cell = (BoardGameCell) o;
            return (x == cell.x) && (y == cell.y); 
        }
    }*/
    
    protected static class BoardGamePiece {
        protected int player;
        protected int x;
        protected int y;
        
        protected BoardGamePiece(final int player) {
            this.player = player;
        }
    }
    
    private final static class BoardGamePieceList<P extends BoardGamePiece> {
        private final List<P> internalList = new ArrayList<P>();
        private final List<P> externalList = Collections.unmodifiableList(internalList);
    }
}
