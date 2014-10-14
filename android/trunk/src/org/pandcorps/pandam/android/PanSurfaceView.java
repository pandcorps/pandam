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
package org.pandcorps.pandam.android;

import org.pandcorps.pandam.*;

import android.opengl.*;
import android.view.*;

public class PanSurfaceView extends GLSurfaceView {
	public PanSurfaceView(final PanActivity context) {
		super(context);
		AndroidPangine.context = context;
		setClickable(true);
		setFocusable(true);
		setFocusableInTouchMode(true);
	}
	
	@Override
	public final boolean onTouchEvent(final MotionEvent event) {
		final int action = event.getActionMasked();
		final byte type;
		switch (action) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				type = Panput.TOUCH_DOWN;
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				//TODO Should link this to original DOWN position
				type = Panput.TOUCH_UP;
				break;
			case MotionEvent.ACTION_MOVE:
				final int size = event.getPointerCount();
				for (int index = 0; index < size; index++) {
					addTouchEvent(Panput.TOUCH_MOVE, event, index);
				}
				return true;
			default:
				System.out.println("PanInput " + action);
				return true;
		}
		// bottom-left ~ 45.0, 446.0 (would be top-left if device hadn't been rotated)
		// top-right ~ 775.0, 50.0 (would be bottom-right if device hadn't been rotated)
		// Looks like coordinates are based on the landscape orientation, but origin is top-left instead of bottom-left
		addTouchEvent(type, event, event.getActionIndex());
		return true;
	}
	
	private final void addTouchEvent(final byte type, final MotionEvent event, final int index) {
		AndroidPangine.engine.addTouchEvent(event.getPointerId(index), type, event.getX(index), event.getY(index));
	}
	
	// Input events can also be handled in activity
	@Override
	public final boolean onKeyDown(final int keyCode, final KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK :
				keyDown(event, AndroidPangine.engine.getInteraction().BACK);
				return true;
			case KeyEvent.KEYCODE_MENU : // Thought this would be KEYCODE_SETTINGS; that's something else
				keyDown(event, AndroidPangine.engine.getInteraction().MENU);
				return true;
			case KeyEvent.KEYCODE_SEARCH :
				keyDown(event, AndroidPangine.engine.getInteraction().SEARCH);
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private final void keyDown(final KeyEvent event, final Panput input) {
		if (event.getRepeatCount() == 0) {
			AndroidPangine.engine.addInputEvent(input, true);
		}
	}
	
	@Override
	public final boolean onKeyUp(final int keyCode, final KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK :
				AndroidPangine.engine.addInputEvent(AndroidPangine.engine.getInteraction().BACK, false);
				return true;
			case KeyEvent.KEYCODE_MENU :
				AndroidPangine.engine.addInputEvent(AndroidPangine.engine.getInteraction().MENU, false);
				return true;
			case KeyEvent.KEYCODE_SEARCH :
				AndroidPangine.engine.addInputEvent(AndroidPangine.engine.getInteraction().SEARCH, false);
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
}
