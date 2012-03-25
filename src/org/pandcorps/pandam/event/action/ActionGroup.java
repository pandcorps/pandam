package org.pandcorps.pandam.event.action;

import java.util.ArrayList;

import org.pandcorps.pandam.Pangine;
import org.pandcorps.pandam.Panput;
import org.pandcorps.pandam.Panteraction;

public class ActionGroup {
	private final ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
    private final ArrayList<ActionStartListener> startListeners = new ArrayList<ActionStartListener>();
    
    public void register(final Panput input, final ActionListener listener) {
        final Panteraction inter = Pangine.getEngine().getInteraction();
        inter.register(input, listener);
        listeners.add(listener);
    }
    
    public void register(final Panput input, final ActionStartListener listener) {
        final Panteraction inter = Pangine.getEngine().getInteraction();
        inter.register(input, listener);
        startListeners.add(listener);
    }
    
    public void unregister() {
    	final Panteraction inter = Pangine.getEngine().getInteraction();
        inter.unregisterAllStart(startListeners);
        inter.unregisterAll(listeners);
    }
}
