package org.pandcorps.pandam.event.action;

import java.util.ArrayList;

import org.pandcorps.core.Coltil;
import org.pandcorps.pandam.Pangine;
import org.pandcorps.pandam.Panput;
import org.pandcorps.pandam.Panteraction;

public class ActionGroup {
	private ArrayList<ActionListener> listeners = null;
    private ArrayList<ActionStartListener> startListeners = null;
    private ArrayList<ActionEndListener> endListeners = null;
    
    public void register(final Panput input, final ActionListener listener) {
        final Panteraction inter = Pangine.getEngine().getInteraction();
        inter.register(null, input, listener);
        add(listener);
    }
    
    public void add(final ActionListener listener) {
        listeners = Coltil.add(listeners, listener);
    }
    
    public void register(final Panput input, final ActionStartListener listener) {
        final Panteraction inter = Pangine.getEngine().getInteraction();
        inter.register(null, input, listener);
        add(listener);
    }
    
    public void add(final ActionStartListener listener) {
        startListeners = Coltil.add(startListeners, listener);
    }
    
    public void register(final Panput input, final ActionEndListener listener) {
        final Panteraction inter = Pangine.getEngine().getInteraction();
        inter.register(null, input, listener);
        add(listener);
    }
    
    public void add(final ActionEndListener listener) {
        endListeners = Coltil.add(endListeners, listener);
    }
    
    public void unregister() {
    	final Panteraction inter = Pangine.getEngine().getInteraction();
        inter.unregisterAllStart(startListeners);
        startListeners = null;
        inter.unregisterAll(listeners);
        listeners = null;
    }
}
