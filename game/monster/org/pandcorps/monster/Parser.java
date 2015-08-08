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

import java.io.*;
import java.util.*;

import org.pandcorps.core.*;

public class Parser {
    protected static String LOC = null;
    
    public final static synchronized void parse() {
        if (Coltil.isValued(Species.getSpecies())) {
            return;
        }
        new Parser().run();
    }
    
	private final void run() {
	    LOC = Iotil.formatDirectory(System.getProperty("org.pandcorps.monster.def"));
		try {
			runType();
			new ItemRunner().run();
			new LocationRunner().run();
			new SpeciesRunner().run();
			Location.init(); // References species
			runData(); // References Species
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	private final void runType() throws IOException {
		//final int typesSize = Type.values().length;
		float[][] tc = null;
		final BufferedReader in = open("type");
		String line;
		int i = 0;
		final List<Type> types = new ArrayList<Type>();
		//for (int i = 0; i < typesSize; i++)
		while ((line = in.readLine()) != null) {
			final List<String> tokens = getTokens(line, "TYP");
			if (tokens == null) {
				continue;
			}
			final String code = getString(tokens, 1);
			final String name = getString(tokens, 3);
			final Type type = new Type(code, name);
			types.add(type);
			final List<String> multipliers = split(getString(tokens, 2), '~');
			final int typesSize = multipliers.size();
			if (tc == null) {
				tc = new float[typesSize][];
			}
			final float[] typeRow = new float[typesSize];
			tc[i] = typeRow;
			for (int j = 0; j < typesSize; j++) {
				typeRow[j] = Float.parseFloat(multipliers.get(j));
			}
			i++;
			/*new float[typesSize][];
			final float[] typeRow = new float[typesSize];
			i++;
			tc[i] = typeRow;
			//final String line = in.readLine();
			final String cells = line.substring(8, line.lastIndexOf('|'));
			final StringTokenizer tokenizer = new StringTokenizer(cells, "~");
			for (int j = 0; j < typesSize; j++) {
				typeRow[j] = Float.parseFloat(tokenizer.nextToken());
			}*/
		}
		in.close();
		Type.setTypes(types);
		Species.setTypeChart(tc);
	}

	private static abstract class Runner<T> {
	    public abstract String getFileName();
	    
	    public abstract String getSegmentName();
	    
	    public abstract T parse(final List<String> tokens);
	    
	    public abstract void store(final List<T> list);
	    
	    public final void run() throws IOException {
	        final BufferedReader in = open(getFileName());
	        String line;
	        final List<T> list = new ArrayList<T>();
	        final String seg = getSegmentName();
	        while ((line = in.readLine()) != null) {
	            try {
	                //final StringTokenizer t = new StringTokenizer(line, "|");
	                /*while (t.hasMoreTokens()) {
	                    t.nextToken();
	                }*/
	                /*if (!"CRE".equals(t.nextToken())) {
	                    continue;
	                }
	                final int id = Integer.parseInt(t.nextToken());
	                final String name = t.nextToken();
	                final int rank = Integer.parseInt(t.nextToken());*/
	                final List<String> tokens = getTokens(line, seg);
	                if (tokens == null) {
	                    continue;
	                }
	                list.add(parse(tokens));
	            } catch (final Exception e) {
	                throw new IOException("Error for line \"" + line + '"', e);
	            }
	        }
	        store(list);
	    }
	}
	
	private final static BufferedReader open(final String fileName) {
	    return Iotil.getBufferedReader(LOC + fileName + ".txt");
	}
	
	private final static List<String> getTokens(final String line, final String seg) {
	    if (line.length() == 0) {
            return null;
        }
        final List<String> tokens = split(line, '|');
        final String segment = getString(tokens, 0);
        if ("COM".equals(segment)) {
            return null;
        } if (seg != null && !seg.equals(getString(tokens, 0))) {
            throw new IllegalArgumentException(line);
        }
        return tokens;
	}
	
	private final void runData() throws IOException {
	    final BufferedReader in = open("data");
	    String line;
        while ((line = in.readLine()) != null) {
            final List<String> tokens = getTokens(line, "DAT");
            if (tokens == null) {
                continue;
            }
            Data.set(tokens.get(1), tokens.get(2));
        }
	}
	
	private final static class ItemRunner extends Runner<Item> {
        @Override
        public String getFileName() {
            return "item";
        }
        
        @Override
        public String getSegmentName() {
            //return "ITM";
            return null;
        }
        
        @Override
        public Item parse(final List<String> tokens) {
            int i = 0;
            final String segment = getString(tokens, i++);
            final String code = getString(tokens, i++);
            final String name = getString(tokens, i++);
            final int price = getInt(tokens, i++);
            final boolean exhaust = getBoolean(tokens, i++);
            final boolean unique = getBoolean(tokens, i++);
            final boolean secret = getBoolean(tokens, i++);
            final Item item;
            if ("ITM".equals(segment)) {
                item = new Item(code, name, price, exhaust, unique, secret);
            } else if ("CON".equals(segment)) {
                final int rank = getInt(tokens, i++);
                item = new Container(code, name, price, exhaust, unique, secret, rank);
            } else if ("TEC".equals(segment)) {
                final int id = getInt(tokens, i++);
                item = new Technique(code, name, price, exhaust, unique, secret, id);
            } else {
                throw new IllegalArgumentException(segment);
            }
            return item;
        }
        
        @Override
        public void store(final List<Item> list) {
            Item.setItems(list);
        }
    }
	
	private final static class LocationRunner extends Runner<Location> {
        @Override
        public String getFileName() {
            return "location";
        }
        
        @Override
        public String getSegmentName() {
            return "LOC";
        }
        
        @Override
        public Location parse(final List<String> tokens) {
            int i = 1;
            //final int id = getInt(tokens, i++);
            final String code = getString(tokens, i++);
            final String name = getString(tokens, i++);
            final List<Item> store = new ArrayList<Item>();
            for (final String product : split(getString(tokens, i++), '~')) {
                store.add(Item.getItem(product));
            }
            //i++; // reward
            final Item access = Item.getItem(getString(tokens, i++)); // method
            final String special = getString(tokens, i++);
            final int x = getInt(tokens, i++);
            final int y = getInt(tokens, i++);
            return new Location(/*id,*/ code, name, store, access, special, x, y);
        }
        
        @Override
        public void store(final List<Location> list) {
            Location.setLocations(list);
        }
    }

	private final static class SpeciesRunner extends Runner<Species> {
	    @Override
	    public String getFileName() {
	        return "species";
	    }
	    
	    @Override
        public String getSegmentName() {
            return "CRE";
        }
	    
	    @Override
	    public Species parse(final List<String> tokens) {
	        final int id = getInt(tokens, 1);
            final String name = getString(tokens, 2);
            final int rank = getInt(tokens, 3);
            final List<String> typeTokens = split(getString(tokens, 4), '~');
            final int typeSize = typeTokens.size();
            final Type[] types = new Type[typeSize];
            for (int i = 0; i < typeSize; i++) {
                types[i] = Type.getType(typeTokens.get(i));
            }
            final int precursorId = getInt(tokens, 5);
            final Entity catalyst = Entity.getEntity(getString(tokens, 6));
            final String wild = getString(tokens, 7);
            final String trained = getString(tokens, 8);
            final Item award = Item.getItem(getString(tokens, 9));
            final float height = getFloat(tokens, 10);
            final float mass = getFloat(tokens, 11);
            final boolean start = getBoolean(tokens, 12);
            final boolean sire = getBoolean(tokens, 13);
            final boolean track = getBoolean(tokens, 14);
            final boolean unique = getBoolean(tokens, 15);
            //final String special = getString(tokens, 16);
            final Special special = Special.getSpecial(getString(tokens, 16));
            final List<Integer> usable = new ArrayList<Integer>();
            final List<String> useTokens = split(getString(tokens, 17), '~');
            final int useSize = useTokens.size();
            for (int i = 0; i < useSize; i++) {
                if ("Y".equals(useTokens.get(i))) {
                    usable.add(Integer.valueOf(i));
                }
            }
            return new Species(id, name, rank, types, precursorId, catalyst,
                wild, trained, award, height, mass,
                start, sire, track, unique, special, usable);
	    }
        
	    @Override
        public void store(final List<Species> list) {
	        Species.setSpecies(list);
	    }
	}

	private final static List<String> split(final String src, final char d) {
		//return src.split(d); // reg ex
		/*final StringTokenizer t = new StringTokenizer(src, Character.toString(d)); // Seems to skip empty tokens
		final List<String> list = new ArrayList<String>();
		while (t.hasMoreTokens()) {
			list.add(t.nextToken());
		}
		return list;*/
	    final List<String> tokens = new ArrayList<String>();
	    if (src == null) {
	        return tokens;
	    }
	    final int size = src.length();
	    int start = 0;
	    for (int i = 0; i < size; i++) {
	        if (src.charAt(i) == d) {
	            tokens.add(src.substring(start, i));
	            //i++;
	            //start = i;
	            start = i + 1;
	        }
	    }
	    if (start < size) {
	        tokens.add(src.substring(start, size));
	    }
	    return tokens;
	}
	
	private final static String getString(final List<String> tokens, final int i) {
	    if (i >= tokens.size()) {
	        return null;
	    }
	    final String token = tokens.get(i);
	    return token.length() == 0 ? null : token;
	}

	private final static int getInt(final List<String> tokens, final int i) {
	    final String token = getString(tokens, i);
	    return token == null ? -1 : Integer.parseInt(token);
    }
	
	private final static float getFloat(final List<String> tokens, final int i) {
        final String token = getString(tokens, i);
        return token == null ? -1 : Float.parseFloat(token);
    }

	private final static boolean getBoolean(final List<String> tokens, final int i) {
	    final String token = getString(tokens, i);
	    if ("Y".equals(token)) {
	        return true;
	    } else if ("N".equals(token)) {
	        return false;
	    } else {
	        throw new RuntimeException("Expected Y|N but found " + token);
	    }
	}
}
