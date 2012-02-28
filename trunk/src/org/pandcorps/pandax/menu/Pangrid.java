/*
Copyright (c) 2009-2011, Andrew M. Martin
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
package org.pandcorps.pandax.menu;

import java.util.*;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.action.*;

public abstract class Pangrid<I> extends Panctor {
    private final List<? extends List<I>> rows;
    private final Panmage cursor;
    private final float dimX;
    private final float dimY;
    private final float curOff;
    private int curRow = 0;
    private int curCol = 0;
    
    public Pangrid(final String id, final List<? extends List<I>> rows, final Panmage cursor, final float cursorOff) {
        super(id);
        this.rows = rows;
        this.cursor = cursor;
        final List<I> firstRow = rows.get(0);
        final I firstItem = firstRow.get(0);
        final Panmage first = getImage(firstItem);
        final Panple dim = first.getSize();
        dimX = dim.getX();
        dimY = dim.getY();
        curOff = cursorOff;
        if (getCurrentItem() == null) {
            right();
        }
        enable();
    }
    
    private final void enable() {
        final Pangine engine = Pangine.getEngine();
        final Panteraction interaction = engine.getInteraction();
        final Panput submit = interaction.KEY_ENTER, up = interaction.KEY_UP, down = interaction.KEY_DOWN;
        final Panput left = interaction.KEY_LEFT, right = interaction.KEY_RIGHT;
        Panput.inactivate(submit, up, down);
        final ActionStartListener upListener = new ActionStartListener() {
            @Override
            public void onActionStart(final ActionStartEvent event) {
                do {
                    curRow--;
                    if (curRow < 0) {
                        curRow = rows.size() - 1;
                    }
                } while (rows.get(curRow).get(curCol) == null);
            }};
        final ActionStartListener downListener = new ActionStartListener() {
            @Override
            public void onActionStart(final ActionStartEvent event) {
                do {
                    curRow++;
                    if (curRow >= rows.size()) {
                        curRow = 0;
                    }
                } while (rows.get(curRow).get(curCol) == null);
            }};
        final ActionStartListener leftListener = new ActionStartListener() {
            @Override
            public void onActionStart(final ActionStartEvent event) {
                final List<I> row = rows.get(curRow);
                do {
                    curCol--;
                    if (curCol < 0) {
                        curCol = row.size() - 1;
                    }
                } while (row.get(curCol) == null);
            }};
        final ActionStartListener rightListener = new ActionStartListener() {
            @Override
            public void onActionStart(final ActionStartEvent event) {
                right();
            }};
        final ActionStartListener submitListener = new ActionStartListener() {
            @Override
            public void onActionStart(final ActionStartEvent event) {
                interaction.unregister(this);
                interaction.unregister(upListener);
                interaction.unregister(downListener);
                interaction.unregister(leftListener);
                interaction.unregister(rightListener);
                Panput.inactivate(submit, up, down, left, right);
                onSubmit(getCurrentItem());
            }};
        interaction.register(submit, submitListener);
        interaction.register(up, upListener);
        interaction.register(down, downListener);
        interaction.register(left, leftListener);
        interaction.register(right, rightListener);
    }
    
    private final I getCurrentItem() {
        return rows.get(curRow).get(curCol);
    }
    
    private final void right() {
        final List<I> row = rows.get(curRow);
        do {
            curCol++;
            if (curCol >= row.size()) {
                curCol = 0;
            }
        } while (row.get(curCol) == null);
    }
    
    @Override
    protected final void updateView() {       
    }

    @Override
    public final Pansplay getCurrentDisplay() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected final void renderView(final Panderer renderer) {
        final Panple center = getPosition();
        final float offX = center.getX() - (0.5f * dimX * rows.get(0).size());
        float y = center.getY() + (0.5f * dimY * rows.size());
        final float z = center.getZ();
        final Panlayer layer = getLayer();
        int rowIndex = 0;
        for (final List<I> row : rows) {
            int colIndex = 0;
            float x = offX;
            for (final I item : row) {
                if (item != null) {
                    renderer.render(layer, getImage(item), x, y, z);
                    if (rowIndex == curRow && colIndex == curCol) {
                        renderer.render(layer, cursor, x, y, z + curOff);
                    }
                }
                x += dimX;
                colIndex++;
            }
            y -= dimY;
            rowIndex++;
        }
    }
    
    protected abstract Panmage getImage(final I item);
    
    protected abstract void onSubmit(final I item);
}
