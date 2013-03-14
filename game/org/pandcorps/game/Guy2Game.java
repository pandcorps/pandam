package org.pandcorps.game;

import java.awt.image.BufferedImage;

import org.pandcorps.core.Reftil;
import org.pandcorps.core.img.scale.*;
import org.pandcorps.game.actor.Guy2;
import org.pandcorps.game.core.ImtilX;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.FinPanple;

public abstract class Guy2Game extends Pangame {
	public final static int SCREEN_W = 256;
	public final static int SCREEN_H = 192;
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
        engine.setMaxZoomedDisplaySize(SCREEN_W, SCREEN_H);
    }
	
	@Override
    protected final FinPanple getFirstRoomSize() {
        return new FinPanple(SCREEN_W, SCREEN_H, 0);
    }
    
	@Override
    public final void step() {
        Guy2.step();
    }
	
	protected final static BufferedImage[] loadConstantImgs() {
	    return ImtilX.loadStrip("org/pandcorps/game/res/misc/Constants.png");
	}
	
	protected final static Panmage[] createSheet(final String name, final String path) {
	    return createSheet(name, path, ImtilX.DIM);
	}
	
	protected final static Panmage[] createSheet(final String name, final String path, final int dim) {
	    final Pangine engine = Pangine.getEngine();
	    final BufferedImage[] b = ImtilX.loadStrip(path, dim);
	    final int size = b.length;
	    final Panmage[] p = new Panmage[size];
	    for (int i = 0; i < size; i++) {
	        p[i] = engine.createImage("img." + name + "." + i, b[i]);
	    }
	    return p;
	}
	
	protected final static Panimation createAnm(final String name, final String path, final int dur) {
	    return createAnm(name, path, ImtilX.DIM, dur);
	}
	
	protected final static Panimation createAnm(final String name, final String path, final int dim, final int dur) {
	    final Pangine engine = Pangine.getEngine();
	    final Panmage[] ia = createSheet(name, path, dim);
	    final int size = ia.length;
	    final Panframe[] fa = new Panframe[size];
	    for (int i = 0; i < size; i++) {
	        final Panmage img = ia[i];
	        fa[i] = engine.createFrame("frm." + name + "." + i, img, dur);
	    }
	    return engine.createAnimation("anm." + name, fa);
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
