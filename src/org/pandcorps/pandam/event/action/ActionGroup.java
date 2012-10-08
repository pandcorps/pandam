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
