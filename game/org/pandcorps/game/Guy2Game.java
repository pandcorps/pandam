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
	    final Pangine engine = Pangine.getEngine();
	    final Panmage bld1Img = engine.createImage("img.blood.1", CENTER_16, null, null, constantImgs[6]);
        final Panmage bld2Img = engine.createImage("img.blood.2", CENTER_16, null, null, constantImgs[7]);
        final Panframe bld1Frm = engine.createFrame("frm.blood.1", bld1Img, dur);
        final Panframe bld2Frm = engine.createFrame("frm.blood.2", bld2Img, dur);
        return engine.createAnimation("anm.blood", bld1Frm, bld2Frm);
	}
}
