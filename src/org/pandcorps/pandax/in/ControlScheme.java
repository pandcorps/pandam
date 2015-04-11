/*
Copyright (c) 2009-2014, Andrew M. Martin
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
package org.pandcorps.pandax.in;

import org.pandcorps.pandam.*;
import org.pandcorps.pandam.Panput.*;
import org.pandcorps.pandam.Panteraction.*;

public class ControlScheme {
	// Each input below could be a key, but device could be a touch screen mapping buttons to keys
	private Device device = null;
    private Panput down = null;
    private MappableInput originalDown = null;
    private Panput up = null;
    private MappableInput originalUp = null;
    private Panput left = null;
    private MappableInput originalLeft = null;
    private Panput right = null;
    private MappableInput originalRight = null;
    private Panput act1 = null;
    private MappableInput original1 = null;
    private Panput act2 = null;
    private MappableInput original2 = null;
    private Panput sub = null;
    private MappableInput originalSub = null;
    
    public ControlScheme() {
    }
    
    public ControlScheme(final Panput down, final Panput up, final Panput left, final Panput right, final Panput act1, final Panput act2, final Panput sub) {
        set(down, up, left, right, act1, act2, sub);
    }
    
    public final static ControlScheme getDefaultKeyboard() {
        final ControlScheme ctrl = new ControlScheme();
        ctrl.setDefaultKeyboard();
        return ctrl;
    }
    
    public final static ControlScheme getDefault(final Device d) {
        final ControlScheme ctrl = new ControlScheme();
        ctrl.setDefault(d);
        return ctrl;
    }
    
    public final void setDefaultKeyboard() {
        final Panteraction in = Pangine.getEngine().getInteraction();
        set(in.KEY_DOWN, in.KEY_UP, in.KEY_LEFT, in.KEY_RIGHT, in.KEY_SPACE, in.KEY_ESCAPE, in.KEY_ENTER);
    }
    
    public final void setDefault(final Device d) {
        if (d instanceof Controller) {
            final Controller c = (Controller) d;
            if (d.getName().startsWith("Controller")) {
            	set(c.DOWN, c.UP, c.LEFT, c.RIGHT, c.BUTTON_0, c.BUTTONS.get(2), c.BUTTONS.get(c.BUTTONS.size() - 3));
            } else {
            	set(c.DOWN, c.UP, c.LEFT, c.RIGHT, c.BUTTON_1, c.BUTTON_0, c.BUTTONS.get(c.BUTTONS.size() - 1));
            }
        } else if (d instanceof Keyboard) {
            setDefaultKeyboard();
        //} else if (d instanceof Touchscreen) {
        	//final Panteraction in = Pangine.getEngine().getInteraction();
        	//set(in.TOUCH, in.KEY_UP, in.KEY_LEFT, in.KEY_RIGHT, in.KEY_SPACE, in.KEY_ESCAPE, in.KEY_ENTER);
        } else if (d == null) {
            throw new NullPointerException("Requested ControlScheme for null Device");
        } else {
        	throw new UnsupportedOperationException("No default ControlScheme for Device " + d.getName());
        }
    }
    
    public void set(final Panput down, final Panput up, final Panput left, final Panput right, final Panput act1, final Panput act2, final Panput sub) {
        this.down = down;
        this.up = up;
        this.left = left;
        this.right = right;
        this.act1 = act1;
        this.act2 = act2;
        this.sub = sub;
    }
    
    public void map(final MappableInput down, final MappableInput up, final MappableInput left, final MappableInput right, final MappableInput act1, final MappableInput act2, final MappableInput sub) {
    	mapDown(down);
    	mapUp(up);
    	mapLeft(left);
    	mapRight(right);
    	map1(act1);
    	map2(act2);
    	mapSubmit(sub);
    }
    
    public final Device getDevice() {
    	return device == null ? get1().getDevice() : device;
    }
    
    public final void setDevice(final Device device) {
    	this.device = device;
    }
    
    public final Panput getDown() {
        return down;
    }
    
    public final Panput getOriginalDown() {
        return originalDown == null ? down : originalDown;
    }
    
    public final void setDown(final Panput down) {
        this.down = down;
    }
    
    public final void mapDown(final MappableInput down) {
    	MappableInput.setMappedInput(down, this.down);
    }
    
    public final Panput getUp() {
        return up;
    }
    
    public final Panput getOriginalUp() {
        return originalUp == null ? up : originalUp;
    }
    
    public final void setUp(final Panput up) {
        this.up = up;
    }
    
    public final void mapUp(final MappableInput up) {
    	MappableInput.setMappedInput(up, this.up);
    }
    
    public final Panput getLeft() {
        return left;
    }
    
    public final Panput getOriginalLeft() {
        return originalLeft == null ? left : originalLeft;
    }
    
    public final void setLeft(final Panput left) {
        this.left = left;
    }
    
    public final void mapLeft(final MappableInput left) {
    	MappableInput.setMappedInput(left, this.left);
    }
    
    public final Panput getRight() {
        return right;
    }
    
    public final Panput getOriginalRight() {
        return originalRight == null ? right : originalRight;
    }
    
    public final void setRight(final Panput right) {
        this.right = right;
    }
    
    public final void mapRight(final MappableInput right) {
    	MappableInput.setMappedInput(right, this.right);
    }
    
    public final Panput get1() {
        return act1;
    }
    
    public final Panput getOriginal1() {
        return original1 == null ? act1 : original1;
    }
    
    public final void set1(final Panput act1) {
        this.act1 = act1;
    }
    
    public final void map1(final MappableInput act1) {
    	MappableInput.setMappedInput(act1, this.act1);
    }
    
    public final Panput get2() {
        return act2;
    }
    
    public final Panput getOriginal2() {
        return original2 == null ? act2 : original2;
    }
    
    public final void set2(final Panput act2) {
        this.act2 = act2;
    }
    
    public final void map2(final MappableInput act2) {
    	MappableInput.setMappedInput(act2, this.act2);
    }
    
    public final Panput getSubmit() {
        return sub;
    }
    
    public final Panput getOriginalSubmit() {
        return originalSub == null ? sub : originalSub;
    }
    
    public final void setSubmit(final Panput sub) {
        this.sub = sub;
    }
    
    public final void mapSubmit(final MappableInput sub) {
    	MappableInput.setMappedInput(sub, this.sub);
    }
}
