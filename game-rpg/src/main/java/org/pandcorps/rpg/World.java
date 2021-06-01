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
package org.pandcorps.rpg;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import org.pandcorps.core.*;
import org.pandcorps.core.col.*;
import org.pandcorps.core.seg.*;
import org.pandcorps.rpg.Chr.*;

public class World {
    private final static InverseDistanceWeightedSets interpolator = new InverseDistanceWeightedSets(2);
    
    protected final static void loadWorldData(final SegmentStream in) throws IOException {
        Segment seg = null;
        while ((seg = in.readIf("CTY")) != null) {
            cities.add(new City(seg));
        }
    }
    
    protected final static class City {
        protected final String name;
        protected final int x;
        protected final int y;
        protected final WeightedSet<Race> raceDistribution; // Only used for subraces shared by multiple races (Elementals)
        protected final WeightedSet<Subrace> subraceDistribution; // Will usually indicate the race
        
        private City() {
            name = null;
            x = y = 0;
            raceDistribution = new WeightedSet<Race>();
            subraceDistribution = new WeightedSet<Subrace>();
        }
        
        protected City(final Segment seg) {
            name = seg.getValue(0);
            x = seg.intValue(1);
            y = seg.intValue(2);
            final List<Field> raceFields = seg.getRepetitions(3);
            raceDistribution = new WeightedSet<Race>(raceFields.size());
            for (final Field field : raceFields) {
                raceDistribution.add(Chr.getRace(field.getValue(0)), field.intValue(1));
            }
            final List<Field> subraceFields = seg.getRepetitions(4);
            subraceDistribution = new WeightedSet<Subrace>(subraceFields.size());
            for (final Field field : subraceFields) {
                subraceDistribution.add(Chr.getSubrace(field.getValue(0)), field.intValue(1));
            }
        }
    }
    
    private final static List<City> cities = new ArrayList<City>();
    
    protected final static City getInterpolatedCity() {
        interpolator.clear();
        for (final City city : cities) {
            //interpolator.add(city, distance); //TODO
        }
        return interpolator.getInterpolatedCity();
    }
    
    protected final static class InverseDistanceWeightedSets {
        private final int powerParameter;
        private final Map<City, Double> cities = new HashMap<City, Double>();
        private double sum = 0;
        private boolean stale = false;
        private final City interpolatedCity = new City();
        private final Map<Object, Double> numerators = new HashMap<Object, Double>();
        
        public InverseDistanceWeightedSets(final int powerParameter) {
            if (powerParameter < 1) {
                throw new IllegalArgumentException("Power parameter must be >= 1 but found " + powerParameter);
            }
            this.powerParameter = powerParameter;
        }
        
        public final void add(final City city, final int distance) {
            final double weight;
            if (distance == 0) {
                weight = 0;
            } else {
                final double denominator = Mathtil.pow(distance, powerParameter);
                weight = 1 / denominator;
            }
            final Double old = cities.put(city, Double.valueOf(weight));
            if (old != null) {
                throw new IllegalArgumentException("Duplicate entries for " + city + ", " + weight + " and " + old);
            }
            sum += weight;
            stale = true;
        }
        
        public final City getInterpolatedCity() {
            interpolate();
            return interpolatedCity;
        }
        
        private final void interpolate() {
            if (!stale) {
                return;
            }
            clearInterpolatedCity();
            interpolate(raceDistributionGetter);
            interpolate(subraceDistributionGetter);
        }
        
        private final <E> void interpolate(Getter<City, WeightedSet<E>> getter) {
            numerators.clear();
            final WeightedSet<E> interpolatedSet = getter.get(interpolatedCity);
            for (final Entry<City, Double> entry : cities.entrySet()) {
                final City city = entry.getKey();
                final WeightedSet<E> set = getter.get(city);
                final double weight = entry.getValue().doubleValue();
                if (weight == 0) {
                    interpolatedSet.setAll(set);
                    return;
                }
                for (final Entry<E, Integer> elementEntry : set.getWeights().entrySet()) {
                    final E element = elementEntry.getKey();
                    final double sum = Mathtil.doubleValue(numerators.get(element));
                    numerators.put(element, Double.valueOf(sum + (weight * elementEntry.getValue().intValue())));
                }
            }
            for (final Entry<Object , Double> entry : numerators.entrySet()) {
                interpolatedSet.add((E) entry.getKey(), (int) Math.round(entry.getValue().doubleValue() / sum));
            }
            stale = false;
        }
        
        public final void clear() {
            cities.clear();
            sum = 0;
            clearInterpolatedCity();
        }
        
        private void clearInterpolatedCity() {
            interpolatedCity.raceDistribution.clear();
            interpolatedCity.subraceDistribution.clear();
        }
    }
    
    private final static Getter<City, WeightedSet<Race>> raceDistributionGetter = new Getter<City, WeightedSet<Race>>() {
        @Override public final WeightedSet<Race> get(final City source) {
            return source.raceDistribution;
        }
    };
    
    private final static Getter<City, WeightedSet<Subrace>> subraceDistributionGetter = new Getter<City, WeightedSet<Subrace>>() {
        @Override public final WeightedSet<Subrace> get(final City source) {
            return source.subraceDistribution;
        }
    };
    
    private static interface Getter<S, V> {
        public V get(final S source);
    }
}
