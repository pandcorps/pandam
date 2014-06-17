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
package org.pandcorps.fight;

import java.io.*;
import java.util.*;
import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.fight.Background.*;
import org.pandcorps.fight.Fighter.*;
import org.pandcorps.game.*;
import org.pandcorps.game.actor.*;
import org.pandcorps.game.actor.Guy2.*;
import org.pandcorps.game.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.event.*;
import org.pandcorps.pandam.event.action.*;
import org.pandcorps.pandam.impl.*;
import org.pandcorps.pandax.visual.*;
import org.pandcorps.pandax.menu.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Fonts.*;

public class FightGame extends Guy2Game {
    private final static String PROP_DEBUG = "org.pandcorps.fight.FightGame.debug";
    
    private final static String PATH_CHR = "org/pandcorps/fight/res/chr/"; // Was char; could be bad to use reserved word
    
    private final static boolean debug = Boolean.getBoolean(PROP_DEBUG);
    
    ///*package*/ final static float yb = 14;
    /*package*/ final static float yb = 1; // Currently treat bottom left as 0, 0, maybe this needs to be changed
    
    //private final static int WIN_W = 1024;
    //private final static int WIN_H = 768;
    //private final static int ZOOM = 4;
    protected final static int DIM = ImtilX.DIM;
    private final static int BOUND_MAX = 2;
    private final static int BOUND_MIN = -BOUND_MAX;
    private final static short OUTLINE_DEFAULT = 16;
    
    private static Panmage cursorImage = null;
    private static Panmage menuBackground = null;
    private static Panmage shadowImage = null;
    /*package*/ static Guy2Type type = null;
    private static Panroom room = null;
    private final static BackgroundLoader backgrounds = new BackgroundLoader();
    private static Background background = null;
    private final static FighterLoader fighters = new FighterLoader();
    private final static ArrayList<ArrayList<FighterDefinition>> characterSelect = new ArrayList<ArrayList<FighterDefinition>>();
    private static Panmage bamImage1 = null;
    /*package*/ static Panimation bamAnim = null;
    private final static FinPanple explodeOrigin = CENTER_16;
    private static Img[] explodeImgs = null;
    private final static FinPanple bloodOrigin = CENTER_16;
    private final static int bloodDuration = 3;
    private static Img[] bloodImgs = null;
    private static Panimation bloodAnim = null;
    private final static FinPanple burnOrigin = new FinPanple(9, yb, 0);
    private static Img[] burnImgs = null;
    /*package*/ static Panimation puffAnim = null;
    /*package*/ static Font fontDamage = null;
    /*package*/ static Font fontSpecial = null;
    /*package*/ static Font fontText = null;
    private static FighterDefinition playerDef = null;
    private static Panmage title = null;
    /*package*/ static int mode = 0;
    
    private static short outlineR = OUTLINE_DEFAULT;
    private static short outlineG = OUTLINE_DEFAULT;
    private static short outlineB = OUTLINE_DEFAULT;
    
    private final static void initWindow() {
        final Pangine engine = Pangine.getEngine();
        engine.setIcon("org/pandcorps/fight/res/misc/MicroMeleeIcon32.png", "org/pandcorps/fight/res/misc/MicroMeleeIcon16.png");
        engine.setBgColor(new Pancolor((short) 63, (short) 255, (short) 191, (short) 255));
    }
    
    private final static void loadConstants() {
        final Pangine engine = Pangine.getEngine();
        final Img[] menuImgs = ImtilX.loadStrip("org/pandcorps/fight/res/misc/Menu.png");
        cursorImage = engine.createImage("Cursor", new FinPanple(8, 1, 0), null, null, menuImgs[0]);
        menuBackground = engine.createImage("MenuBgImage", "org/pandcorps/fight/res/misc/MenuBackground.png");
        title = engine.createImage("TitleImg", "org/pandcorps/fight/res/misc/Title.png");
        final Img[] constantImgs = loadConstantImgs();
        Img.setTemporary(false, constantImgs);
        shadowImage = engine.createImage("Shadow", new FinPanple(8, 4, 0), null, null, constantImgs[0]);
        type = new Guy2Type(shadowImage, Fighter.DEPTH_SHADOW);
        bamAnim = createBamAnm(constantImgs, 3);
        bamImage1 = bamAnim.getFrames()[0].getImage();
        explodeImgs = new Img[] { constantImgs[3], constantImgs[4], constantImgs[5] };
        /*final Panmage explode1 = engine.createImage("Explode0", new FinPanple(8, 8, 0), null, null, constantImgs[3]);
        final Panmage explode2 = engine.createImage("Explode1", new FinPanple(8, 8, 0), null, null, constantImgs[4]);
        final Panmage explode3 = engine.createImage("Explode2", new FinPanple(8, 8, 0), null, null, constantImgs[5]);
        explodeAnim = engine.createAnimation("Explode", engine.createFrame("ExplodeF1", explode1, 3), engine.createFrame("ExplodeF2", explode2, 3), engine.createFrame("ExplodeF3", explode3, 3));*/
        bloodAnim = createBloodAnm(constantImgs, bloodDuration);
        bloodImgs = new Img[] { constantImgs[6], constantImgs[7] };
        puffAnim = createPuffAnm(constantImgs, 3);
        
        burnImgs = ImtilX.loadStrip("org/pandcorps/fight/res/misc/Burn.png");
        Img.setTemporary(false, burnImgs);
        
        //font = engine.createImage("font", "org/pandcorps/res/img/FontOutline8.png");
        fontDamage = Fonts.getOutline(new FontRequest(FontType.Number, 8), Pancolor.RED);
        fontSpecial = Fonts.getOutline(new FontRequest(FontType.Number, 8), Pancolor.CYAN);
        final short c = (short) 192;
        fontText = Fonts.getOutline(new FontRequest(FontType.Upper, 8), new FinPancolor(c, c, c, Pancolor.MAX_VALUE));
    }
    
    @Override
    protected void init(final Panroom room) throws Exception {
        //final Pangine engine = Pangine.getEngine();
        loadConstants();
        loadGame();
        initWindow();
        
        /*
        TODO
        . Panctor mirroring
        . Change facing direction
        . Panctor flipping
        . Panframe mirroring/flipping
        . Depth based on y coord
        . Panctor rotating
        . Panframe rotating
        Panctor coloring
        . Shadow trail effect
        . Logo
        . Title
        . Menu (arcade, practice, infinite, creator)
        . Character select
        . Palette swapping for primary palette
        Alternate palettes
        . Automatic darkening of outline color
        . Automatic background transparency
        . Attacks
        Automatic pause after attacks
        . Projectiles die out of bounds
        . Correct projectile depths so appears behind character when appropriate
        . Bam/pow animation for successful attacks
        . Bam configurable by Projectile (default/blood/explosion)
        . Character-specific blood color
        . Reaction configurable by Projectile (default/burn/freeze)
        . Projectile-specific burn color
        . Projectile-specific explosion color
        . Fighter shadows
        Panimation onFrame(FrameEvent(int/short/byte index))
        . Move/Attack class, wraps Panimation, creates projectiles at specified frames
        . Window size/resolution
        Zoom when characters near each other
        . Background images
        . Background boundaries
        . Moves respect boundaries
        . Panframe offsets to Panmage origin
        . Use Panframe origin offsets/overrides for jumping moves
        Panframe origin for lobbing projectiles
        Pantil sequence() method, alternative to vmid()
        java.util.logging.LogManager
        . File format for organizing frames/moves
        . AI
        . Track health
        Damage algorithm
            12 for spec, divide by emitters
            6 for strong, divide by emitters, subtract range
            2 for quick, divide by emitters, subtract range
            Subtract elec, burn
            None for freeze
        . Display health after damage above characters
        . Remove defeated characters (poof? explode? fade?)
        . End fight when only one team remains
        . Track normal attacks to award special attacks
        . Display special attack awards above characters
        Move editor
            Order of frames for animations
            Duration of frames
            Position of frames
            Flip/mirror/rotate
            Save to file
        */
        FightGame.room = room;
        createTypes();
        //showLogo();
        Panscreen.set(new LogoScreen(TitleScreen.class));
        //startFight();
    }
    
    private final static void createTypes() {
        final Pangine engine = Pangine.getEngine();
        final Panimation fighterView;
        fighterView = null;
        engine.createType("FighterType", Fighter.class, fighterView);
        engine.createType("ProjectileType", Projectile.class, bamImage1);
        engine.createType("BackgroundType", Background.class, fighterView);
    }
    
    private final static class TitleScreen extends FadeScreen {
        private TitleScreen() {
            super(Pancolor.WHITE, 60);
        }
        
        @Override
        protected final void start() {
            final Pangine engine = Pangine.getEngine();
            engine.setBgColor(Pancolor.WHITE);
            final Panctor act = new Panctor("TitleAct");
            act.setView(title);
            act.getPosition().set(64, 32);
            room.addActor(act);
        }
        
        @Override
        protected final void finish() {
            showMenu();
        }
        
        @Override
        protected final void destroy() {
            //Panmage.destroy(title);
        }
    }
    
    private final static void showMenu() {
    	Panscreen.set(new MenuScreen());
    }
    
    private final static class MenuScreen extends Panscreen {
        @Override
        protected final void load() {
        	final RadioSubmitListener listener = new RadioSubmitListener() {
				@Override
				public void onSubmit(final RadioSubmitEvent event) {
					mode = event.getIndex();
					if (mode == 2) {
					    Pangine.getEngine().exit();
					    return;
					}
					showSelect();
				}
			};
			final Panctor actor = new Panctor("TitleActor");
			actor.setView(title);
			actor.getPosition().set(64, 48);
			room.addActor(actor);
			final RadioGroup g;
        	g = new RadioGroup(fontText, Arrays.asList("Arcade", "Practice", "Exit"), listener);
        	g.setLayer(room);
        	g.setSubmit(Pangine.getEngine().getInteraction().KEY_ENTER);
        	g.init(); // Centers vertically
        	g.getLabel().getPosition().setY(24);
        }
    }
    
    private final static void showSelect() {
    	Panscreen.set(new CharacterSelectScreen());
    }
    
    private final static class CharacterSelectScreen extends Panscreen {
        @Override
        protected final void load() {
            room.addActor(new CharacterSelectGrid());
            final ScrollTexture texture = new ScrollTexture("MenuBgActor", menuBackground);
            texture.setSize(SCREEN_W, SCREEN_H);
            texture.setVelocity(1, -1);
            room.addActor(texture);
            texture.getPosition().setZ(-200);
            final Panteraction inter = Pangine.getEngine().getInteraction();
            texture.register(inter.KEY_ESCAPE, new ActionStartListener() {
            	@Override
            	public final void onActionStart(final ActionStartEvent event) {
            		showMenu();
            	}
            });
        }
    }
    
    private final static class CharacterSelectGrid extends Pangrid<FighterDefinition> {
    	private final Decoration selected = new Decoration("selected");
    	private Pantext name = null;
    	
        private CharacterSelectGrid() {
            super("CharacterSelect", characterSelect, cursorImage, -2);
            final int midH = SCREEN_H / 2;
            getPosition().set(new FinPanple(SCREEN_W / 2, midH, 0));
            room.addActor(selected);
            selected.getPosition().set(32, midH);
            change();
        }
        
        @Override
        protected final Panmage getImage(final FighterDefinition def) {
            return def.getStill().getFrames()[0].getImage();
        }
        
        @Override
        protected final void onChange(final GridChangeEvent<FighterDefinition> event) {
        	final FighterDefinition def = event.getItem();
        	selected.setView(def.getStill());
        	if (name != null) {
        		name.destroy();
        	}
        	name = new Pantext("selected.name", fontText, def.getName());
        	name.getPosition().set(16, SCREEN_H / 2 - DIM);
        	room.addActor(name);
        }
        
        @Override
        protected final void onSubmit(final GridSubmitEvent<FighterDefinition> event) throws Exception {
            playerDef = event.getItem();
            startFight();
        }
    }
    
    private final static void startFight() throws Exception {
        Panscreen.set(new FightScreen());
    }
    
    private final static class FightScreen extends Panscreen {
    	private boolean won = false;
    	
        @Override
        protected final void load() throws Exception {
        	final String bg;
        	final byte aiMode;
        	if (mode == 0) {
        		bg = "Mountain";
        		aiMode = Ai.MODE_FIGHT;
        	} else {
        		bg = "Grid";
        		aiMode = Ai.MODE_STARE;
        	}
            final BackgroundDefinition bdef = backgrounds.load("org/pandcorps/fight/res/bg/" + bg + ".txt");
            background = new Background("BAK." + bdef.name, bdef);
            room.addActor(background);
            
            final Fighter fighter, cpu;
            //fighter = new Player("FighterActor", still, walk, quick, spec1, hurt);
            //fighter = new Fighter("FighterActor", room, animStill, walk, moveQuick, moveStrong, moveSpec1, moveSpec2, animHurt, getBloodAnimation(null));
            //final SegmentStream in = openSegmentStream("org/pandcorps/fight/Def.txt");
            //try {
            fighter = new Fighter("FTR.1", room, playerDef);
            cpu = new Fighter("FTR.2", room, fighters.load(PATH_CHR + "Clive.txt"));
            //} finally {
            //    in.close();
            //}
            /*final Player player = *///new Player(fighter);
            new Player(background).setFighter(fighter);
            final float centerX, centerY;
            //centerX = ROOM_W / 2;
            //centerY = ROOM_H / 2;
            final Panple min = bdef.min, max = bdef.max;
            centerX = (min.getX() + max.getX()) / 2;
            centerY = (min.getY() + max.getY()) / 2;
            fighter.getPosition().set(centerX - 16, centerY);
            //room.addActor(fighter);
            
            //cpu = new Fighter("CpuActor", room, animStill, walk, moveQuick, moveStrong, moveSpec1, moveSpec2, animHurt, getBloodAnimation(meanFireball));
            cpu.setMirror(true);
            //cpu.setFlip(true); // Testing
            //cpu.setView(animFireball); // Testing, Fighter class should clobber anyway
            //new Ai(cpu);
            final Ai ai = new Ai();
            ai.setFighter(cpu);
            cpu.getPosition().set(centerX + 16, centerY);
            ai.mode = aiMode;
            //room.addActor(cpu);
        }
        
        @Override
        protected final void step() {
        	if (won) {
        		return;
        	}
        	int count = 0;
        	Fighter f = null;
        	for (final Panctor actor : room.getActors()) {
        		if (actor.getClass() == Fighter.class) {
        			count++;
        			f = (Fighter) actor;
        		}
        	}
        	if (count == 1) {
        		final String msg = f.def.getName() + " WINS";
        		final Pantext text = new Pantext("Win", fontText, msg);
        		text.getPosition().set(48, SCREEN_H * 7 / 8);
        		room.addActor(text);
        		text.register(60, new TimerListener() {
					@Override
					public void onTimer(final TimerEvent event) {
						showSelect();
					}});
        		won = true;
        	}
        }
    }
    
    /*package*/ final static boolean isDebug() {
        return debug;
    }
    
    /*package*/ final static Panmage getShadowImage() {
        return shadowImage;
    }
    
    /*package*/ final static Panimation getBurnAnimation(final short[] baseColor) {
        return getAnimation(baseColor, burnOrigin, 3, burnImgs);
    }
    
    /*package*/ final static Panimation getExplodeAnimation(final short[] baseColor) {
        return getAnimation(baseColor, explodeOrigin, 3, explodeImgs);
    }
    
    /*package*/ final static Panimation getBloodAnimation(final short[] baseColor) {
        if (baseColor == null) {
            return bloodAnim;
        }
        return getAnimation(baseColor, bloodOrigin, bloodDuration, bloodImgs);
    }
    
    private final static Panimation getAnimation(final short[] baseColor, final Panple o, final int dur, final Img... imgs) {
        final Pangine engine = Pangine.getEngine();
        final int size = imgs.length;
        final Panframe[] frames = new Panframe[size];
        for (int i = 0; i < size; i++) {
            final Img recolored = Imtil.recolor(imgs[i], baseColor);
            final Panmage imageI = engine.createImage(Pantil.vmid(), o, null, null, recolored);
            frames[i] = engine.createFrame(Pantil.vmid(), imageI, dur);
        }
        return engine.createAnimation(Pantil.vmid(), frames);
    }
    
    /*package*/ final static Background getBackground() {
        return background;
    }
    
    private final static class LoadImage {
        private final Img buffered;
        private final Panmage pan;
        
        private LoadImage(final Img buffered, final Panmage pan) {
            this.buffered = buffered;
            this.pan = pan;
        }
    }
    
    private final static class FighterLoader extends Loader<FighterDefinition> {
        @Override
        protected final FighterDefinition loadUncached(final SegmentStream in) throws IOException {
            return loadFighter(in);
        }
    }
    
    private final static FighterDefinition loadFighter(final SegmentStream in) throws IOException {
        final Segment ftr = in.read();
        if (ftr == null) {
            return null;
        }
        Segment pls;
        ReplacePixelFilter filter = null;
        while ((pls = in.readIf("PLS")) != null) {
        	final Segment pld = in.readRequire("PLD");
        	final List<Field> src = pls.getRepetitions(0);
        	final List<Field> dst = pld.getRepetitions(0);
        	final int size = src.size();
        	if (size != dst.size()) {
        		throw new RuntimeException("Source palette size doesn't match destination");
        	}
        	for (int i = 0; i < size; i++) {
        		filter = ReplacePixelFilter.put(filter, Pancolor.getPancolor(src.get(i)), Pancolor.getPancolor(dst.get(i)));
        	}
        }
        final String name = ftr.getValue(0);
        final Img sheet = ImtilX.loadImage(PATH_CHR + name + ".png", filter);
        final Img[] frms = Imtil.toStrip(sheet, DIM);
        Img.setTemporary(false, frms);
        Segment img;
        final HashMap<String, LoadImage> imgMap = new HashMap<String, LoadImage>();
        final IdentityHashMap<Panmage, Img> imgUnmap = new IdentityHashMap<Panmage, Img>();
        final Pangine engine = Pangine.getEngine();
        while ((img = in.readIf("IMG")) != null) {
            final String id = img.getValue(0);
            final int index = img.intValue(1);
            final FinPanple origin = FinPanple.getFinPanple(img, 2);
            final Img buffered = frms[index];
            final Panmage pan = engine.createImage(getId("IMG", name, id), origin, null, null, buffered);
            imgMap.put(id, new LoadImage(buffered, pan));
            imgUnmap.put(pan, buffered);
        }
        Segment frm;
        final HashMap<String, Panframe> frmMap = new HashMap<String, Panframe>();
        while ((frm = in.readIf("FRM")) != null) {
            final String id = frm.getValue(0);
            final Panmage image = imgMap.get(frm.getValue(1)).pan;
            final int dur = frm.intValue(2);
            final int rot = Mathtil.intValue(frm.getInteger(3));
            final boolean mirror = Pantil.booleanValue(frm.getBoolean(4));
            final boolean flip = Pantil.booleanValue(frm.getBoolean(5));
            final FinPanple o = FinPanple.getFinPanple(frm, 6);
            final FinPanple min = FinPanple.getFinPanple(frm, 7, -7, BOUND_MIN, 0);
            final FinPanple max = FinPanple.getFinPanple(frm, 8, 7, BOUND_MAX, 0);
            final Panframe frame = engine.createFrame(getId("FRM", name, id), image, dur, rot, mirror, flip, o, min, max);
            frmMap.put(id, frame);
        }
        Segment anm;
        final HashMap<String, Panimation> anmMap = new HashMap<String, Panimation>();
        while ((anm = in.readIf("ANM")) != null) {
            final String id = anm.getValue(0);
            final List<Field> reps = anm.getRepetitions(1);
            final byte type = Move.parseAnim(anm.getValue(2));
            final int mult;
            switch (type) {
                case Move.ANIM_MIRROR :
                case Move.ANIM_FLIP :
                case Move.ANIM_DIAGONAL :
                    mult = 2;
                    break;
                case Move.ANIM_RISE :
                    mult = 3;
                    break;
                default :
                    mult = 1;
                    break;
            }
            final int size = reps.size();
            final Panframe[] frames = new Panframe[size * mult];
            for (int i = 0; i < size; i++) {
                final Panframe frame = frmMap.get(reps.get(i).getValue());
                frames[i] = frame;
                final int off = i;
                final FrameAdder adder = new FrameAdder() {
                    private int j = 0;
                    @Override
                    protected void add(final Panframe f) {
                        frames[size + off + (j++)] = f;
                    }
                };
                transform(frame, type, adder);
            }
            anmMap.put(id, engine.createAnimation(getId("ANM", name, id), frames));
        }
        Segment mvd;
        final HashMap<String, Move> mvdMap = new HashMap<String, Move>(4);
        final HashMap<String, Panmage> genMap = new HashMap<String, Panmage>();
        
        final Panimation still = anmMap.get("still");
        /*
        If a Fighter has a true animation, this will cause the first frame to appear for one extra cycle.
        We could just add a pause state to Fighter.
        Then we could play the animation normally as the state changes from pause to still.
        */
        /*
        Actually, this messes up looping animations.
        We would only want to pause after the last iteration.
        This pauses after each iteration.
        */
        /*
        final Panframe frameStill = still.getFrames()[0];
        final Panframe framePause = engine.createFrame(frameStill.getId() + ".pause", frameStill.getImage(), 1);
        final MoveFrame moveFramePause = new MoveFrame(framePause, null);
        */
        
        while ((mvd = in.readUnless("EXT", "FTR")) != null) {
            final String id = mvd.getValue(0);
            final byte type = Projectile.parseType(id);
            final short[] trailBase = getColor(imgMap, mvd, 3);
            final HashMap<String, MoveFrame> mframeMap = new HashMap<String, MoveFrame>();
            Segment mvf;
            final ArrayList<MoveFrame> mframes = new ArrayList<MoveFrame>();
            while ((mvf = in.readUnless("MVD", "EXT", "FTR")) != null) {
            	final String fid = mvf.getValue(0);
                final Panframe frame = frmMap.get(fid);
                if (frame == null) {
                    throw new Panception("Could not find frame " + fid + " for character " + name);
                }
                Segment emt;
                ArrayList<Emitter> emitters = null;
                while ((emt = in.readIf("EMT")) != null) {
                    final FinPanple off = FinPanple.getFinPanple(emt, 0);
                    final float xoff = off.getX(), yoff = off.getY();
                    final byte impact = Projectile.parseImpact(emt.getValue(1));
                    final byte react = Projectile.parseReact(emt.getValue(2));
                    final FinPanple evel = FinPanple.getFinPanple(emt, 3);
                    final byte time = Mathtil.byteValue(emt.getByte(4), (byte) -1);
                    final Panimation anim = anmMap.get(emt.getValue(5));
                    final Panview view;
                    if (anim == null) {
                        final Panmage image = frame.getImage();
                        final String hitId = image.getId() + ".hit";
                        final Panmage cached = genMap.get(hitId);
                        if (cached == null) {
                            // Might also need to get this from frame
                            final Panple o = image.getOrigin();
                            //final Panple bmn = image.getBoundingMinimum(), bmx = image.getBoundingMaximum();
                            final Panple bmn = frame.getEffectiveBoundingMinimum(), bmx = frame.getEffectiveBoundingMaximum();
                            //final FinPanple min = new FinPanple(o.getX(), bmn.getY(), bmn.getZ());
                            final FinPanple min = new FinPanple(0, bmn.getY(), bmn.getZ());
                            //final FinPanple max = new FinPanple(getRightEdge(imgUnmap.get(image)), bmx.getY(), bmx.getZ());
                            final FinPanple max = new FinPanple(getRightEdge(imgUnmap.get(image)) - o.getX(), bmx.getY(), bmx.getZ());
                            // Store in map if we get collisions
                            final Panmage hImg = engine.createEmptyImage(hitId, o, min, max);
                            view = hImg;
                            genMap.put(hitId, hImg);
                        } else {
                            view = cached;
                        }
                    } else {
                        view = anim;
                    }
                    short[] base = null;
                    final boolean exp = impact == Projectile.IMPACT_EXPLOSION, brn = react == Projectile.REACT_BURN;
                    if (exp || brn) {
                    	base = getColor(imgMap, emt, 7);
                    	if (base == null) {
                    		base = Imtil.getArithmeticMeanColor(imgUnmap.get(anim.getFrames()[0].getImage()));
                    	}
                    }
                    final Panimation impactView = exp ? getExplodeAnimation(base) : null;
                    final Panimation reactView = brn ? getBurnAnimation(base) : null;
                    final boolean linked = emt.booleanValue(6);
                    final Emitter em;
                    em = new Emitter(xoff, yoff, type, impact, impactView, react, reactView, evel, time, view, linked);
                    emitters = Coltil.add(emitters, em);
                }
                final Panmage tImg;
                if (trailBase == null) {
                    tImg = null;
                } else {
                    final Panmage src = frame.getImage();
                    final String trlId = src.getId() + ".trail";
                    final Panmage cached = genMap.get(trlId);
                    if (cached == null) {
                        final Img buf = Imtil.recolor(imgUnmap.get(src), trailBase);
                        final Panple o = src.getOrigin(), min = src.getBoundingMinimum(), max = src.getBoundingMaximum();
                        // Could end up with duplicates (error from id collision); store in a Map and check there first
                        tImg = engine.createImage(trlId, o, min, max, buf);
                        genMap.put(trlId, tImg);
                    } else {
                        tImg = cached;
                    }
                }
                final FinPanple fvel = FinPanple.getFinPanple(mvf, 1);
                MoveFrame mframe = mframeMap.get(fid);
                if (mframe == null) {
                	mframe = new MoveFrame(frame, fvel, tImg, Coltil.toArray(emitters));
                	mframeMap.put(fid, mframe);
                }
                mframes.add(mframe);
            }
            final int loop = Mathtil.intValue(mvd.getInteger(1), 1);
            final boolean stopAfterHit = Pantil.booleanValue(mvd.getBoolean(2));
            final byte animType = Move.parseAnim(mvd.getValue(4));
            if (animType != Move.ANIM_NORMAL) {
                final int size = mframes.size();
                for (int i = 0; i < size; i++) {
                    final MoveFrame mframe = mframes.get(i);
                    final FrameAdder adder = new FrameAdder() {
                        //private boolean first = true;
                        @Override
                        protected void add(final Panframe f) {
                            final Emitter[] emitters;
                            if (animType == Move.ANIM_RISE) {
                                emitters = null;
                                // Only want emitters on 1st frame of move; this is 1st transformed frame, 2nd of move
                                /*if (first) {
                                    emitters = mframe.emitters;
                                    first = false;
                                } else {
                                    emitters = null;
                                }*/
                            } else {
                                emitters = mframe.emitters;
                            }
                            mframes.add(new MoveFrame(f, mframe.velocity, mframe.trail, emitters));
                        }
                    };
                    transform(mframe.pframe, animType, adder);
                }
            }
            //mframes.add(moveFramePause);
            mvdMap.put(id, new Move(getId("ANM", name, id), loop, stopAfterHit, Coltil.toArray(mframes)));
        }
        final Segment ext = in.readIf("EXT");
        final Panimation blood = getBloodAnimation(getColor(imgMap, ext, 0));
        final Panimation walk = anmMap.get("walk");
        final Move quick = mvdMap.get("quick");
        final Move strong = mvdMap.get("strong");
        final Move spec1 = mvdMap.get("spec1");
        final Move spec2 = mvdMap.get("spec2");
        final Panimation hurt = anmMap.get("hurt");
        return new FighterDefinition(name, still, walk, quick, strong, spec1, spec2, hurt, blood);
    }
    
    private final static short[] getColor(final HashMap<String, LoadImage> imgMap, final Segment seg, final int i) {
    	return seg == null ? null : getColor(imgMap, seg.getValue(i));
    }
    
    private final static short[] getColor(final HashMap<String, LoadImage> imgMap, final String val) {
    	return val == null ? null : Imtil.getArithmeticMeanColor(imgMap.get(val).buffered);
    }
    
    private final static String getId(final String type, final String name, final String id) {
        return type + '.' + name + '.' + id;
    }
    
    private final static int getRightEdge(final Img img) {
        final int w = img.getWidth() - 1, h = img.getHeight();
        final ImgFactory cm = ImgFactory.getFactory();
        for (int x = w; x >= 0; x--) {
            boolean edge = false;
            for (int y = 0; y < h; y++) {
                final int p = img.getRGB(x, y);
                if (cm.getAlpha(p) == 0) {
                    continue;
                }
                if (x < w) {
                    return x;
                }
                /*
                Check for the outline color.
                If the border has any color other than the outline, treat it as bleeding past the border.
                Return the border + 1.
                */
                if (cm.getRed(p) != outlineR || cm.getGreen(p) != outlineG || cm.getBlue(p) != outlineB) {
                    return x + 1;
                }
                edge = true;
            }
            if (edge) {
                return x;
            }
        }
        return -1;
    }
    
    private abstract static class FrameAdder {
        protected abstract void add(final Panframe f);
    }
    
    private final static void transform(final Panframe frame, final byte type, final FrameAdder adder) {
        final Panframe transformed;
        switch (type) {
            case Move.ANIM_MIRROR :
                transformed = transform(frame, true, false, 0, 1);
                break;
            case Move.ANIM_FLIP :
                transformed = transform(frame, false, true, 0, 1);
                break;
            case Move.ANIM_DIAGONAL :
                transformed = transform(frame, true, true, 0, 1);
                break;
            case Move.ANIM_RISE :
                int yoff = 0;
                for (int i = 3; i >= 1; i--) {
                    yoff += i;
                    adder.add(transform(frame, false, false, yoff, i == 1 ? 3 : 1));
                }
                return;
            default :
                return;
        }
        adder.add(transformed);
    }
    
    private final static Panframe transform(final Panframe frame, final boolean m, final boolean f, final int yoff, final int dmult) {
        final String suffix;
        if (m) {
            suffix = f ? "diag" : "mirror";
        } else if (f) {
            suffix = "flip";
        } else if (yoff != 0) {
            suffix = "rise." + yoff;
        } else {
            return frame;
        }
        final String fid = frame.getId() + '.' + suffix;
        final int dur = frame.getDuration() * dmult, rot = frame.getRot();
        boolean mirror = frame.isMirror(), flip = frame.isFlip();
        if (m) {
            mirror = !mirror;
        }
        if (f) {
            flip = !flip;
        }
        final Panple o = frame.getEffectiveOrigin();
        final float fx = o.getX(), fy = o.getY() - yoff, fz = o.getZ();
        final Panple fo = f ? new FinPanple(fx, 15 - fy, fz) : yoff == 0 ? o : new FinPanple(fx, fy, fz);
        final Panple min = frame.getBoundingMinimum(), max = frame.getBoundingMaximum();
        return Pangine.getEngine().createFrame(fid, frame.getImage(), dur, rot, mirror, flip, fo, min, max);
    }
    
    private abstract static class Loader<T> {
        private final HashMap<String, T> cache = new HashMap<String, T>();
        
        protected final T load(final String loc) throws IOException {
            T item = cache.get(loc);
            if (item == null) {
                final SegmentStream in = openSegmentStream(loc);
                try {
                    item = loadUncached(in);
                    cache.put(loc, item);
                } finally {
                    in.close();
                }
            }
            return item;
        }
        
        protected abstract T loadUncached(final SegmentStream in) throws IOException;
    }
    
    private final static class BackgroundLoader extends Loader<BackgroundDefinition> {
        @Override
        protected final BackgroundDefinition loadUncached(final SegmentStream in) throws IOException {
            final Segment bak = in.read();
            if (bak == null) {
                return null;
            }
            /*
            Redundant to have in file name and content?  Same with .java files.
            Would also be necessary if we want to allow multiple resources to be defined in one file.
            */
            final String name = bak.getValue(0);
            final Pangine engine = Pangine.getEngine();
            final Panmage bg = engine.createImage("Background." + name, "org/pandcorps/fight/res/bg/" + name + ".png");
            final float bgMinX = Mathtil.floatValue(bak.getFloat(1), 0);
            final float bgMinY = Mathtil.floatValue(bak.getFloat(2), 0);
            final float bgMaxX = Mathtil.floatValue(bak.getFloat(3), 255);
            final float bgMaxY = Mathtil.floatValue(bak.getFloat(4), 191);
            return new BackgroundDefinition(name, bg, bgMinX, bgMinY, bgMaxX, bgMaxY);
        }
    }
    
    private final static void loadGame() throws IOException {
    	final Pangine engine = Pangine.getEngine();
    	final SegmentStream in = openSegmentStream("org/pandcorps/fight/res/Game.txt");
    	try {
    		final Segment gam = in.read();
    		engine.setTitle(gam.getValue(0));
    		final Pancolor col = Pancolor.getPancolor(gam, 1);
    		if (col != null) {
    			outlineR = col.getR();
    			outlineG = col.getG();
    			outlineB = col.getB();
    			final short d = OUTLINE_DEFAULT, m = Pancolor.MAX_VALUE;
    			ImtilX.outlineSrc = new Pancolor(d, d, d, m);
    			ImtilX.outlineDst = new Pancolor(outlineR, outlineG, outlineB, m);
    		}
    		Segment sel;
    		while ((sel = in.read()) != null) {
    		    final ArrayList<FighterDefinition> row = new ArrayList<FighterDefinition>();
    		    characterSelect.add(row);
    		    for (final Field f : sel.getRepetitions(0)) {
    		        final String name = Field.getValue(f);
		            row.add(Chartil.isValued(name) ? fighters.load(PATH_CHR + name + ".txt") : null);
    		    }
    		}
    	} finally {
    		in.close();
    	}
    }
    
    private final static SegmentStream openSegmentStream(final String loc) {
        final SegmentStream in = SegmentStream.openLocation(loc);
        in.skip("COM");
        return in;
    }
    
    public final static void main(final String[] args) {
        try {
            new FightGame().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
