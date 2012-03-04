package org.pandcorps.pandax.menu;

public abstract class GridEvent<I> {
	private final Pangrid<I> grid;
	
	/*package*/ GridEvent(final Pangrid<I> grid) {
        this.grid = grid;
    }
    
    public final int getRow() {
        return grid.curRow;
    }
    
    public final int getColumn() {
        return grid.curCol;
    }
    
    public final I getItem() {
        return grid.getCurrentItem();
    }
}
