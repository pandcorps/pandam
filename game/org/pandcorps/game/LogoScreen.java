package org.pandcorps.game;

import org.pandcorps.core.img.Pancolor;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.visual.FadeScreen;

public abstract class LogoScreen extends FadeScreen {
    private Panmage font = null;
    private Panmage icon = null;
    
    public LogoScreen() {
        super(Pancolor.WHITE, 30);
    }
    
    @Override
    protected final void start() {
        final Pangine engine = Pangine.getEngine();
        engine.setBgColor(Pancolor.WHITE);
        font = engine.createImage("PandcorpsFont", "org/pandcorps/res/img/FontGradient16.png");
        final Pantext text = new Pantext("PandcorpsLogo", new ByteFont(font), "PANDCORPS");
        text.getPosition().set(48, 88);
        final Panroom room = Pangame.getGame().getCurrentRoom();
        room.addActor(text);
        icon = engine.createImage("PandcorpsIcon", "org/pandcorps/res/img/PandcorpsIcon16.png");
        final Panctor img = new Panctor("PandcorpsImage");
        img.setView(icon);
        img.getPosition().set(192, 88);
        room.addActor(img);
    }
    
    @Override
    protected final void destroy() {
        Panmage.destroy(font);
        Panmage.destroy(icon);
    }
}
