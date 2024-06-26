/*
Copyright (c) 2009-2024, Andrew M. Martin
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
package org.pandcorps.pandam.impl;

import org.pandcorps.pandam.*;

public interface SpecPanctor extends Pantity {
    public void setView(final Panimation view);

    public void setView(Panmage view);
    
    public void setView(Panframe view);
    
    public void setView(Panctor actor);
    
    public boolean changeView(Panimation view);

    public boolean changeView(Panmage view);
    
    public boolean changeView(Panframe view);
    
	public Panple getPosition();
	
	public Panple getBoundingMinimum();

    public Panple getBoundingMaximum();
    
    public Panple getBoundingCenter();
    
    public boolean isVisible();

    public void setVisible(final boolean vis);
    
    public int getRot();
    
    public void setRot(final int rot);
    
    public boolean isMirror();

    public void setMirror(final boolean mirror);
    
    public int getMirrorMultiplier();
    
    public boolean isFlip();

    public void setFlip(final boolean flip);
    
    public Panlayer getLayer();

	public Pansplay getCurrentDisplay();
	
	public boolean isInView();

	public boolean isDestroyed();
	
	public void detach();
}
