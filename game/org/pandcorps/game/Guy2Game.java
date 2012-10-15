package org.pandcorps.game;

import org.pandcorps.core.Reftil;
import org.pandcorps.core.img.scale.*;
import org.pandcorps.game.actor.Guy2;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.FinPanple;

public abstract class Guy2Game extends Pangame {
	protected final static int ROOM_W = 256;
	protected final static int ROOM_H = 192;
	
	@Override
    public void initBeforeEngine() {
        final Pangine engine = Pangine.getEngine();
        final String scalerClassName = System.getProperty("org.pandcorps.game.Scaler.impl");
        if (scalerClassName != null) {
        	engine.setImageScaler((Scaler) Reftil.newInstance(scalerClassName));
        }
        engine.setMaxZoomedDisplaySize(ROOM_W, ROOM_H);
    }
	
	@Override
    protected final FinPanple getFirstRoomSize() {
        return new FinPanple(ROOM_W, ROOM_H, 0);
    }
    
	@Override
    public final void step() {
        Guy2.step();
    }
}
