package org.pandcorps.game;

import java.awt.image.BufferedImage;

import org.pandcorps.core.Reftil;
import org.pandcorps.core.img.scale.*;
import org.pandcorps.game.actor.Guy2;
import org.pandcorps.game.core.ImtilX;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.FinPanple;

public abstract class Guy2Game extends Pangame {
	protected final static int ROOM_W = 256;
	protected final static int ROOM_H = 192;
	protected final static FinPanple CENTER_16 = new FinPanple(8, 8, 0);
	protected final static FinPanple CENTER_8 = new FinPanple(4, 4, 0);
	protected final static FinPanple CENTER_4 = new FinPanple(2, 2, 0);
	
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
	
	protected final static BufferedImage[] loadConstantImgs() {
	    return ImtilX.loadStrip("org/pandcorps/game/res/misc/Constants.png");
	}
	
	protected final static Panimation createBloodAnm(final BufferedImage[] constantImgs, final int dur) {
	    return createAnm("blood", constantImgs, dur, 6, 7);
	}
	
	protected final static Panimation createExplosionAnm(final BufferedImage[] constantImgs, final int dur) {
        return createAnm("explosion", constantImgs, dur, 3, 4, 5);
    }
	
	protected final static Panimation createPuffAnm(final BufferedImage[] constantImgs, final int dur) {
	    return createAnm("puff", constantImgs, dur, 8, 9, 10);
	}
	
	protected final static Panimation createAnm(final String name, final BufferedImage[] constantImgs, final int dur, final int... is) {
	    final Pangine engine = Pangine.getEngine();
	    final int size = is.length;
	    final Panframe[] frms = new Panframe[size];
	    for (int i = 0; i < size; i++) {
    	    final Panmage img = engine.createImage("img." + name + "." + i, CENTER_16, null, null, constantImgs[is[i]]);
            frms[i] = engine.createFrame("frm." + name + "." + i, img, dur);
	    }
        return engine.createAnimation("anm." + name, frms);
	}
}
