package org.pandcorps.game;

import java.awt.image.BufferedImage;

import org.pandcorps.game.actor.Guy2;
import org.pandcorps.game.core.ImtilX;
import org.pandcorps.pandam.*;

public abstract class Guy2Game extends BaseGame {
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
	
	protected final static Panimation createBamAnm(final BufferedImage[] constantImgs, final int dur) {
	    return createAnm("bam", constantImgs, dur, 1, 2);
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
