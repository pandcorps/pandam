/*
Copyright (c) 2009-2011, Andrew M. Martin
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
package org.pandcorps.platform;

import java.io.*;
import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.platform.Player.PlayerData;

public class Profile extends PlayerData implements Segmented {
    protected final ArrayList<Avatar> avatars = new ArrayList<Avatar>();
    protected Avatar currentAvatar = null;
    protected int gems = 0;
    protected final Statistics stats = new Statistics();
    protected final TreeSet<Integer> achievements = new TreeSet<Integer>();
    //protected int ctrl = -1; // Should store a preferred scheme for gamepads plus a preferred one for keyboards; don't know which device player will have
    
    public Avatar getAvatar(final String name) {
    	for (final Avatar avatar : avatars) {
    		if (avatar.getName().equals(name)) {
    			return avatar;
    		}
    	}
    	return null;
    }
    
    public void replaceAvatar(final Avatar avt) {
		avatars.set(avatars.indexOf(currentAvatar), avt);
		currentAvatar = avt;
    }
    
    public void load(final Segment seg) {
    	setName(seg.getValue(0));
    	gems = seg.intValue(2);
    	//ctrl = seg.intValue(3);
    }
    
    @Override
    public void save(final Segment seg) {
        seg.setName(PlatformGame.SEG_PRF);
        seg.setValue(0, getName());
        seg.setValue(1, getName(currentAvatar));
        seg.setInt(2, gems);
        //seg.setInt(3, ctrl);
    }
    
    protected void loadAchievements(final Segment seg) {
    	for (final Field f : Coltil.unnull(seg.getRepetitions(0))) {
    		achievements.add(f.getInteger());
    	}
    }
    
    private void saveAchievements(final Segment seg) {
    	seg.setName(PlatformGame.SEG_ACH);
    	for (final Integer ach : achievements) {
    		seg.addInteger(0, ach);
    	}
    }
    
    public void serialize(final Writer out) throws IOException {
        Segtil.serialize(this, out);
        Iotil.println(out);
        Segtil.serialize(stats, out);
        Iotil.println(out);
        final Segment ach = new Segment();
        saveAchievements(ach);
        ach.serialize(out);
        for (final Avatar avatar : avatars) {
        	Iotil.println(out);
            Segtil.serialize(avatar, out);
        }
    }
    
    public void serialize(final String loc) throws IOException {
    	final Writer out = Iotil.getBufferedWriter(loc);
    	try {
    		serialize(out);
    	} finally {
    		Iotil.close(out);
    	}
    }
    
    public void serialize() {
        try {
            serialize(getName() + PlatformGame.EXT_PRF);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public final static class Statistics implements Segmented {
    	protected int defeatedLevels = 0;
    	protected int defeatedWorlds = 0;
    	protected long defeatedEnemies = 0;
    	protected long bumpedBlocks = 0;
    	protected long brokenBlocks = 0;
    	protected long jumps = 0;
    	//totalGems?
    	
    	public void load(final Segment seg) {
        	defeatedLevels = seg.initInt(0);
        	defeatedWorlds = seg.initInt(1);
        	defeatedEnemies = seg.initLong(2);
        	bumpedBlocks = seg.initLong(3);
        	brokenBlocks = seg.initLong(4);
        	jumps = seg.initLong(5);
        }
    	
		@Override
		public void save(final Segment seg) {
			seg.setName(PlatformGame.SEG_STX);
	        seg.setInt(0, defeatedLevels);
	        seg.setInt(1, defeatedWorlds);
	        seg.setLong(2, defeatedEnemies);
	        seg.setLong(3, bumpedBlocks);
	        seg.setLong(4, brokenBlocks);
	        seg.setLong(5, jumps);
		}
    }
}
