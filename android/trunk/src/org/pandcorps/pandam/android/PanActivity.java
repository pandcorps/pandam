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

import android.opengl.*;
import android.os.*;
import android.app.*;
import android.content.res.*;
import android.util.*;
import android.view.*;
import org.pandcorps.core.*;

/*
Title - res/values/strings.xml/app_name
*/

public class PanActivity extends Activity {
	protected static GLSurfaceView view = null;
	
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		ImgFactory.setFactory(new AndroidImgFactory());
		new AndroidPangine();
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        view = new PanSurfaceView(this);
        view.setRenderer(new PanRenderer());
        view.setRenderMode(PanSurfaceView.RENDERMODE_CONTINUOUSLY);
        
        System.setProperty("user.dir", getFilesDir().getAbsolutePath());
        
        setSize();
        setContentView(view);
	}
	
	private final void setSize() {
		if (AndroidPangine.desktopWidth > 0) {
			return;
		}
        final int w = view.getWidth();
        if (w > 0) {
        	AndroidPangine.desktopWidth = w;
        	AndroidPangine.desktopHeight = view.getHeight();
        } else {
        	final Resources r = getResources();
        	if (r != null) {
        		final DisplayMetrics dm = r.getDisplayMetrics();
        		if (dm != null) {
        			AndroidPangine.desktopWidth = dm.widthPixels;
        			AndroidPangine.desktopHeight = dm.heightPixels;
        		} else {
        			throw new RuntimeException("Cannot find device width/height");
        		}
        	}
        }
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.pan, menu);
		return true;
	}
	
	@Override
	protected final void onResume() {
		super.onResume();
		view.onResume();
	}

	@Override
	protected final void onPause() {
		super.onPause();
		view.onPause();
	}
}
