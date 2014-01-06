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
package org.pandcorps.pandam.event.action;

import java.util.ArrayList;
import java.util.List;

import org.pandcorps.core.Coltil;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.TimerListener;

public class ActionGroup {
	private ArrayList<ActionListener> listeners = null;
    private ArrayList<ActionStartListener> startListeners = null;
    private ArrayList<ActionEndListener> endListeners = null;
    private ArrayList<TimerListener> timerListeners = null;
    
    public void register(final Panput input, final ActionListener listener) {
        final Panteraction inter = Pangine.getEngine().getInteraction();
        inter.register(null, input, listener);
        add(listener);
    }
    
    public void add(final ActionListener listener) {
        listeners = Coltil.add(listeners, listener);
    }
    
    public List<ActionListener> getListeners() {
    	return Coltil.unmodifiableList(listeners);
    }
    
    public void register(final Panput input, final ActionStartListener listener) {
        final Panteraction inter = Pangine.getEngine().getInteraction();
        inter.register(null, input, listener);
        add(listener);
    }
    
    public void add(final ActionStartListener listener) {
        startListeners = Coltil.add(startListeners, listener);
    }
    
    public List<ActionStartListener> getStartListeners() {
    	return Coltil.unmodifiableList(startListeners);
    }
    
    public void register(final Panput input, final ActionEndListener listener) {
        final Panteraction inter = Pangine.getEngine().getInteraction();
        inter.register(null, input, listener);
        add(listener);
    }
    
    public void add(final ActionEndListener listener) {
        endListeners = Coltil.add(endListeners, listener);
    }
    
    public List<ActionEndListener> getEndListeners() {
        return Coltil.unmodifiableList(endListeners);
    }
    
    public void register(final long duration, final TimerListener listener) {
        Pangine.getEngine().addTimer(null, duration, listener);
        add(listener);
    }
    
    public void add(final TimerListener listener) {
        timerListeners = Coltil.add(timerListeners, listener);
    }
    
    public void unregister() {
    	final Pangine engine = Pangine.getEngine();
    	final Panteraction inter = engine.getInteraction();
        inter.unregisterAllStart(startListeners);
        startListeners = null;
        inter.unregisterAll(listeners);
        listeners = null;
        inter.unregisterAllEnd(endListeners);
        endListeners = null;
        engine.removeTimers(timerListeners);
        timerListeners = null;
    }
}
