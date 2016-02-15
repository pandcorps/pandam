/*
Copyright (c) 2009-2016, Andrew M. Martin
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
package org.pandcorps.pandax.text;

import java.util.*;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandax.in.*;

public class Panform extends MenuItem {
    // Might move layer-managing code from TextItem into MenuItem
    private final ArrayList<TextItem> items = new ArrayList<TextItem>();
    private FormTabListener tabListener = null;
    private int curr = 0;
    
    public Panform(final ControlScheme ctrl) {
        this(Pangame.getGame().getCurrentRoom(), ctrl);
    }
    
    public Panform(final Panlayer layer, final ControlScheme ctrl) {
        super(new Panctor(), ctrl);
        bound.setVisible(false);
        this.layer = layer;
        layer.addActor(bound);
    }
    
    public void setTabListener(final FormTabListener tabListener) {
    	this.tabListener = tabListener;
    }
    
    public void addItem(final TextItem item) {
        items.add(item);
        item.form = this;
    }
    
    public final void init() {
    	if (items.isEmpty()) {
    		return;
    	}
    	for (final TextItem item : items) {
    	    item.setControlScheme(ctrl);
    		item.setLayer(layer);
    		layer.addActor(item.label);
    		item.label.setBorderEnabled(false);
    	}
        focus();
    }
    
    @Override
    protected final void focus() {
        bound.register(ctrl.getLeft(), new ActionStartListener() { @Override public final void onActionStart(final ActionStartEvent event) {
            back();
        }});
        bound.register(ctrl.getRight(), new ActionStartListener() { @Override public final void onActionStart(final ActionStartEvent event) {
            forward();
        }});
        focusItem();
    }
    
    @Override
    protected final void blur() {
        blurItem();
        super.blur();
    }
    
    protected void close() {
        for (final TextItem item : items) {
            item.forceClose();
        }
        items.clear();
    }
    
    private final TextItem item() {
        return items.get(curr);
    }
    
    private final TextItem blurItem() {
        final TextItem item = item();
        item.label.setBorderEnabled(false);
        item.blur();
        return item;
    }
    
    private final TextItem focusItem() {
    	final TextItem item = item();
        item.label.setBorderEnabled(true);
        item.focus();
        return item;
    }
    
    private final boolean isInvalid() {
    	return !item().isEnabled();
    }
    
    protected final void forward() {
    	do {
    		tab((curr + 1) % items.size());
    	} while (isInvalid());
    }
    
    protected final void back() {
    	do {
	        int next = curr - 1;
	        if (next < 0) {
	        	next = items.size() - 1;
	        }
	        tab(next);
    	} while (isInvalid());
    }
    
    private final void tab(final int next) {
    	if (tabListener != null) {
    		final FormTabEvent event = new FormTabEvent(item(), items.get(next));
    		tabListener.onTab(event);
    		if (!event.allowed) {
    			return;
    		}
    	}
    	blurItem();
    	curr = next;
    	focusItem();
    }
}
