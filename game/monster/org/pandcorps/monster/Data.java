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
package org.pandcorps.monster;

public class Data {
    private static String experience;
    private static String money;
    private static String morph;
    private static String store;
    private static String trainers;
    private static String database;
    private static String inventory;
    private static Species shapeShifter;
    
    static {
        init();
    }
    
    protected static void init() {
        experience = "Experience";
        money = "Money";
        morph = "Morph";
        store = "Store";
        trainers = "Trainers";
        database = "Database";
        inventory = "Inventory";
        shapeShifter = null;
    }
    
	public final static String getExperience() {
	    return experience;
	}

	public final static String getMoney() {
	    return money;
	}
	
	public final static String getMorph() {
	    return morph;
	}
	
	public final static String getStore() {
	    return store;
	}
	
	public final static String getTrainers() {
	    return trainers;
	}
	
	public final static String getDatabase() {
        return database;
    }
	
	public final static String getInventory() {
        return inventory;
    }
	
	public final static Species getShapeShifter() {
        return shapeShifter;
    }
	
	protected static void set(final String key, final String value) {
	    if ("expnc".equals(key)) {
	        experience = value;
	    } else if ("money".equals(key)) {
            money = value;
        } else if ("morph".equals(key)) {
            morph = value;
        } else if ("store".equals(key)) {
            store = value;
        } else if ("train".equals(key)) {
            trainers = value;
        } else if ("dbase".equals(key)) {
            database = value;
        } else if ("invty".equals(key)) {
            inventory = value;
        } else if ("shift".equals(key)) {
            shapeShifter = Species.getSpecies(value);
        }
	}
	
	/*public String get(String key) {
	    //new ResourceBundle()
	    new Properties(
	}*/
}
