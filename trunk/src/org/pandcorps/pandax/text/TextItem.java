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
package org.pandcorps.pandax.text;

import org.pandcorps.core.Pantil;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.in.*;

public abstract class TextItem extends MenuItem {
    protected final Pantext label;
    protected boolean ownLayer = true;
    protected Panctor parent = null;
    protected Panform form = null;
    
    // Should we add ControlScheme to constructor?  Also Panlayer?
    protected TextItem(final Pantext label) {
        super(label);
        //this.font = font; // in label
        this.label = label;
        //label.setItem(this);
        //label.getPosition().set(label.fontSize, 240 - label.fontSize);
        //label.getPosition().set(label.fontSize, 120 - label.fontSize);
        //setPos();
        //label.setBorder(true);
        if (label.f instanceof ByteFont) {
        	label.setBackground(Pantext.CHAR_DARK);
        	label.setBorderStyle(BorderStyle.Distinct);
        }
    }
    
    private void setPos() {
    	final Pangine engine = Pangine.getEngine();
    	final float lx, ly;
        lx = (engine.getGameWidth() - label.size.getX()) / 2 + label.fontWidth;
        ly = (engine.getGameHeight() / 2) - label.fontHeight;
        label.getPosition().set(lx, ly);
    }
    
    public final void setTitle(final String title) {
        label.setTitle(title);
        //setPos();
    }
    
    public final void setControlScheme(final ControlScheme ctrl) {
        this.ctrl = ctrl;
    }
    
    public final void setLayer(final Panlayer layer) {
        this.layer = layer;
        ownLayer = false;
    }
    
    public final void init() {
        init(null);
    }
    
    public final void init(final Panctor parent) {
        if (ownLayer) {
            final Panroom room = Pangame.getGame().getCurrentRoom();
            //final Panple size = room.getSize();
            //TODO make sure zoom for overlayer matches main layer
            final Panple size = label.size;
            layer = Pangine.getEngine().createLayer(Pantil.vmid(), size.getX(), size.getY(), size.getZ(), room);
            layer.setActive(true);
            layer.setClearDepthEnabled(true);
            layer.setVisible(true);
            room.getTop().addAbove(layer);
        }
        layer.addActor(label);
        setPos();
        focus();
        this.parent = parent;
        activateParent(false);
    }
    
    protected void close() {
    	if (form == null) {
    		forceClose();
    	}
    }
    
    protected void forceClose() {
        label.destroy();
        if (ownLayer) {
            layer.destroy();
        }
        activateParent(true);
    }
    
    protected void activateParent(final boolean active) {
        if (parent != null) {
            parent.getLayer().setActive(active);
        }
    }
    
    public final Pantext getLabel() {
    	return label;
    }
}
