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
package org.pandcorps.demo.pandax.text.sequence;

import org.pandcorps.core.*;
import org.pandcorps.core.img.*;
import org.pandcorps.demo.DemoGame;
import org.pandcorps.pandam.*;
import org.pandcorps.pandax.text.*;
import org.pandcorps.pandax.text.Fonts.*;

public class TextScreenSequenceDemo extends DemoGame {
    private static Font font = null;
    
    private final static class DemoSequence extends TextScreenSequence {
        @Override
        protected final void cancel() {
            Panscreen.set(new DoneScreen());
        }
    }
    
    private abstract static class DemoScreen extends TextScreen {
        private final String img;
        
        private DemoScreen(final String msg, final String img) {
            this(null, msg, img);
        }
        
        private DemoScreen(final TextScreenSequence sequence, final String msg, final String img) {
            super(sequence, new Pantext(Pantil.vmid(), font, msg));
            text.getPosition().set(240, 200);
            this.img = img;
        }
        
        @Override
        protected final void start() {
            final Pangine engine = Pangine.getEngine();
            final Panmage image = engine.createImage(Pantil.vmid(), "org/pandcorps/demo/res/img/" + img);
            final Panctor actor = new Panctor("ImageActor");
            actor.getPosition().set(320, 240);
            actor.setView(image);
            Pangame.getGame().getCurrentRoom().addActor(actor);
        }
    }
    
    private final static class FirstScreen extends DemoScreen {
        private FirstScreen(final TextScreenSequence sequence) {
            super(sequence, getMsg(), "Guy1.png");
        }
        
        private static String getMsg() {
            final StringBuilder sb = new StringBuilder();
            sb.append("Here's text");
            return sb.toString();
        }
        
        @Override
        protected final void finish() {
            Panscreen.set(new SecondScreen(sequence));
        }
    }
    
    private final static class SecondScreen extends DemoScreen {
        private SecondScreen(final TextScreenSequence sequence) {
            super(sequence, getMsg(), "SquareGuy.gif");
        }
        
        private static String getMsg() {
            final StringBuilder sb = new StringBuilder();
            sb.append("More text");
            return sb.toString();
        }
        
        @Override
        protected final void finish() {
            Panscreen.set(new LastScreen(sequence));
        }
    }
    
    private final static class LastScreen extends DemoScreen {
        private LastScreen(final TextScreenSequence sequence) {
            super(sequence, getMsg(), "BigStar1.png");
        }
        
        private static String getMsg() {
            final StringBuilder sb = new StringBuilder();
            sb.append("The end");
            return sb.toString();
        }
        
        @Override
        protected final void finish() {
            cancel();
        }
    }
    
    private final static class DoneScreen extends DemoScreen {
        private DoneScreen() {
            super(getMsg(), "BlueSquare.gif");
        }
        
        private static String getMsg() {
            final StringBuilder sb = new StringBuilder();
            sb.append("Out of sequence");
            return sb.toString();
        }
        
        @Override
        protected final void finish() {
            Pangine.getEngine().exit();
        }
    }
    
    @Override
    protected final void init(final Panroom room) {
        font = Fonts.getSimple(new FontRequest(FontType.Upper, 8), Pancolor.CYAN);
        final TextScreenSequence sequence = new DemoSequence();
        final TextScreen first = new FirstScreen(sequence);
        Panscreen.set(first);
    }

    public final static void main(final String[] args) {
        try {
            new TextScreenSequenceDemo().start();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
