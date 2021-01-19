/*
Copyright (c) 2009-2021, Andrew M. Martin
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

import java.util.*;

public class Text {
    private static ResourceBundle bundle = getBundle();
    
    /*package*/ final static String AWARD = get("Award");
    /*package*/ final static String BACK = get("Back");
    /*package*/ final static String BIRD = get("Bird");
    /*package*/ final static String BUY = get("Buy");
    /*package*/ final static String CLEAR = get("Clear");
    /*package*/ final static String COLOR = get("Color");
    /*package*/ final static String DEBUG = get("Debug");
    /*package*/ final static String DONE = get("Done");
    /*package*/ final static String DOWN = get("Down");
    /*package*/ final static String DUMP = get("Dump");
    /*package*/ final static String EASY = get("Easy");
    /*package*/ final static String EDIT = get("Edit");
    /*package*/ final static String ERASE = get("Erase");
    /*package*/ final static String EYES = get("Eyes");
    /*package*/ final static String FOES = get("Foes");
    /*package*/ final static String GAME = get("Game");
    /*package*/ final static String GEAR = get("Gear");
    /*package*/ final static String GOALS = get("Goals");
    /*package*/ final static String HAT = get("Hat");
    /*package*/ final static String INFO = get("Info");
    /*package*/ final static String KIND = get("Kind");
    /*package*/ final static String LEFT = get("Left");
    /*package*/ final static String MAIN = get("Main");
    /*package*/ final static String MENU = get("Menu");
    /*package*/ final static String MUSIC = get("Music");
    /*package*/ final static String NAME = get("Name");
    /*package*/ final static String NEW = get("New");
    /*package*/ final static String NEXT = get("Next");
    /*package*/ final static String OTHER = get("Other");
    /*package*/ final static String PERKS = get("Perks");
    /*package*/ final static String PICK = get("Pick");
    /*package*/ final static String PLAY = get("Play");
    /*package*/ final static String POWER = get("Power");
    /*package*/ final static String QUIT = get("Quit");
    /*package*/ final static String RANK = get("Rank");
    /*package*/ final static String RIGHT = get("Right");
    /*package*/ final static String RUN = get("Run");
    /*package*/ final static String SAVE = get("Save");
    /*package*/ final static String SETUP = get("Setup");
    /*package*/ final static String SHIRT = get("Shirt");
    /*package*/ final static String STATS = get("Stats");
    /*package*/ final static String UNDO = get("Undo");
    /*package*/ final static String UP = get("Up");
    /*package*/ final static String WORLD = get("World");
    
    /*package*/ final static String ACHIEVEMENTS = get("Achievements");
    /*package*/ final static String ANIMAL = get("Animal");
    /*package*/ final static String ASSISTS = get("Assists");
    /*package*/ final static String AVATAR = get("Avatar");
    /*package*/ final static String BESTIARY = get("Bestiary");
    /*package*/ final static String CANCEL = get("Cancel");
    /*package*/ final static String CONTINUE = get("Continue");
    /*package*/ final static String DEFAULT = get("Default");
    /*package*/ final static String EXPORT = get("Export");
    /*package*/ final static String PROFILE = get("Profile");
    /*package*/ final static String SECONDARY = get("Secondary");
    /*package*/ final static String SELECTED = get("Selected");
    /*package*/ final static String STATISTICS = get("Statistics");
    /*package*/ final static String THEMES = get("Themes");
    /*package*/ final static String TROPHIES = get("Trophies");
    /*package*/ final static String UNSELECTED = get("Unselected");
    
    /*package*/ final static String MINI_GAMES = get("MiniGames", "Mini-games");
    /*package*/ final static String POWER_UP = get("PowerUp", "Power-up");
    
    /*package*/ final static String TITLE_PROMPT_TAP = get("Title.Prompt.Tap", "Tap to start");
    /*package*/ final static String TITLE_PROMPT_ANY = get("Title.Prompt.Any", "Press anything");
    
    /*package*/ final static String NEW_INFO_1 = get("New.Info.1", "A new Avatar lets one player try a different character.");
    /*package*/ final static String NEW_INFO_2 = get("New.Info.2", "The old Avatar is kept.  You can switch back and forth.");
    /*package*/ final static String NEW_INFO_3 = get("New.Info.3", "You keep your Gems and Goals when switching Avatars.");
    /*package*/ final static String NEW_INFO_4 = get("New.Info.4", "A new Profile is for a new person using this device.");
    /*package*/ final static String NEW_INFO_5 = get("New.Info.5", "Each Profile has its own set of Gems and Goals.");
    
    /*package*/ final static String NAME_EMPTY = get("Name.Empty", "Must have a name");
    /*package*/ final static String NAME_DUPLICATE = get("Name.Duplicate", "Name already used");
    
    /*package*/ final static String DELETE_WARN = get("Delete.Warn", "Press Erase again to confirm");
    
    /*package*/ final static String ASSISTS_NOTE = get("Assists.Note", "Can equip multiple");
    /*package*/ final static String BIRD_NOTE = get("Bird.Note", "Can collect Gems");
    /*package*/ final static String POWER_UP_NOTE = get("PowerUp.Note", "Equip one at a time");
    /*package*/ final static String THEMES_NOTE = get("Themes.Note", "Can select multiple");
    
    /*package*/ final static String CABIN_PICK = get("Cabin.Pick", "Hoo! Hoo! Pick one!");
    /*package*/ final static String CABIN_HIT_1 = get("Cabin.Hit.1", "Hoo! Hoo! Hit the block!");
    /*package*/ final static String CABIN_HIT_2 = get("Cabin.Hit.2", "Hoo! Hoo! Hit them!");
    /*package*/ final static String CABIN_MATCH = get("Cabin.Match", "Hoo! Hoo! Match them!");
    
    /*package*/ final static String CASTLE_INTRO_1 = get("Castle.Intro.1", "A portal has appeared outside");
    /*package*/ final static String CASTLE_INTRO_2 = get("Castle.Intro.2", "the castle. Monsters from the");
    /*package*/ final static String CASTLE_INTRO_3 = get("Castle.Intro.3", "Realm of Chaos have invaded");
    /*package*/ final static String CASTLE_INTRO_4 = get("Castle.Intro.4", "the kingdom! Please close the");
    /*package*/ final static String CASTLE_INTRO_5 = get("Castle.Intro.5", "gateway from the other side.");
    
    /*package*/ final static String CASTLE_WIN_1 = get("Castle.Win.1", "You destroyed the Havoc Stone,");
    /*package*/ final static String CASTLE_WIN_2 = get("Castle.Win.2", "sending everything that passed");
    /*package*/ final static String CASTLE_WIN_3 = get("Castle.Win.3", "through the Chaos Gate back to");
    /*package*/ final static String CASTLE_WIN_4 = get("Castle.Win.4", "the original side! I am");
    /*package*/ final static String CASTLE_WIN_5 = get("Castle.Win.5", "forever in your debt.");
    
    /*package*/ final static String MAP_TIP_1 = get("Map.Tip.1", "You can turn music and sounds on/off in Menu/Setup/Music");
    /*package*/ final static String MAP_TIP_2 = get("Map.Tip.2", "You can change your Avatar's appearance in Menu/Edit");
    /*package*/ final static String MAP_TIP_3 = get("Map.Tip.3", "You can spend Gems on clothing and powerups in Menu/Edit/Gear");
    /*package*/ final static String MAP_TIP_4 = get("Map.Tip.4", "You can try powerups for 1 Level without spending any Gems");
    /*package*/ final static String MAP_TIP_5 = get("Map.Tip.5", "You can change the color of clothing that you have purchased");
    /*package*/ final static String MAP_TIP_6 = get("Map.Tip.6", "You can switch between full control and auto-run in Menu/Setup");
    /*package*/ final static String MAP_TIP_7 = get("Map.Tip.7", "You can add a Profile for a new user of this device in Menu/Add");
    /*package*/ final static String MAP_TIP_8 = get("Map.Tip.8", "Spending Gems in one Profile will not take Gems from other Profiles");
    /*package*/ final static String MAP_TIP_9 = get("Map.Tip.9", "Items purchased in one Profile are unavailable to other Profiles");
    /*package*/ final static String MAP_TIP_10 = get("Map.Tip.10", "You can create a new Avatar for yourself in Menu/Add");
    /*package*/ final static String MAP_TIP_11 = get("Map.Tip.11", "A single Profile can have multiple Avatars");
    /*package*/ final static String MAP_TIP_12 = get("Map.Tip.12", "Items purchased for one Avatar can be used by other Avatars of that Profile");
    /*package*/ final static String MAP_TIP_13 = get("Map.Tip.13", "You can spend Gems on new abilities in Menu/Setup/Perks");
    /*package*/ final static String MAP_TIP_14 = get("Map.Tip.14", "You can adjust the game's difficulty in Menu/Setup/Easy-Hard");
    
    /*package*/ final static String MAP_TIP_COMPUTER_1 = get("Map.Tip.Computer.1", "You can enter the menu by pressing Esc on the map");
    /*package*/ final static String MAP_TIP_COMPUTER_2 = get("Map.Tip.Computer.2", "You can view your current Goals by pressing G on the map");
    
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
