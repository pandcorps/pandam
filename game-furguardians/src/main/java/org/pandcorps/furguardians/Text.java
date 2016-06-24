/*
Copyright (c) 2009-2016, Andrew M. Martin
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
package org.pandcorps.furguardians;

import java.util.ResourceBundle;

public class Text {
    private static ResourceBundle bundle = getBundle();
    
    /*package*/ final static String AWARD = get("Award");
    /*package*/ final static String BACK = get("Back");
    /*package*/ final static String BIRD = get("Bird");
    /*package*/ final static String COLOR = get("Color");
    /*package*/ final static String DONE = get("Done");
    /*package*/ final static String DUMP = get("Dump");
    /*package*/ final static String EDIT = get("Edit");
    /*package*/ final static String ERASE = get("Erase");
    /*package*/ final static String EYES = get("Eyes");
    /*package*/ final static String FOES = get("Foes");
    /*package*/ final static String GEAR = get("Gear");
    /*package*/ final static String GOALS = get("Goals");
    /*package*/ final static String HAT = get("Hat");
    /*package*/ final static String INFO = get("Info");
    /*package*/ final static String KIND = get("Kind");
    /*package*/ final static String MENU = get("Menu");
    /*package*/ final static String NAME = get("Name");
    /*package*/ final static String NEW = get("New");
    /*package*/ final static String PLAY = get("Play");
    /*package*/ final static String POWER = get("Power");
    /*package*/ final static String SETUP = get("Setup");
    /*package*/ final static String SHIRT = get("Shirt");
    /*package*/ final static String STATS = get("Stats");
    /*package*/ final static String UNDO = get("Undo");
    
    static {
        bundle = null;
    }
    
    private final static String get(final String s) {
        return (bundle == null) ? s : get(s, s);
    }
    
    private final static String get(final String key, final String def) {
        return ((bundle != null) && bundle.containsKey(key)) ? bundle.getString(key) : def;
    }
    
    private final static ResourceBundle getBundle() {
        return null;
        //return ResourceBundle.getBundle(FurGuardiansGame.RES + "text/text.txt");
    }
}
