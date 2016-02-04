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
package org.pandcorps.core.io;

import java.util.*;
import java.util.jar.*;

import org.pandcorps.core.*;

public class JarComparator {
    public final static void main(final String[] args) {
        try {
            run(args);
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
    
    private final static void run(final String[] args) throws Exception {
        final String name1 = args[0], name2 = args[1];
        final List<JarEntry> list1 = getEntries(name1), list2 = getEntries(name2);
        final Iterator<JarEntry> iter1 = list1.iterator(), iter2 = list2.iterator();
        while (iter1.hasNext() || iter2.hasNext()) {
            JarEntry en1 = Coltil.next(iter1), en2 = Coltil.next(iter2);
            int c = cmp.compare(en1, en2);
            while (c != 0) {
                if (c < 0) {
                    System.out.println(name2 + " was missing " + en1.getName());
                    en1 = iter1.next();
                } else {
                    System.out.println(name1 + " was missing " + en2.getName());
                    en2 = iter2.next();
                }
            }
        }
    }
    
    private final static List<JarEntry> getEntries(final String loc) throws Exception {
        JarInputStream in = null;
        final List<JarEntry> list = new ArrayList<JarEntry>();
        try {
            in = getJar(loc);
            while (true) {
                final JarEntry en = in.getNextJarEntry();
                if (en == null) {
                    break;
                }
                list.add(en);
                //System.out.println(en1.getName() + " - " + en1.isDirectory()); // Name includes full path within jar
            }
        } finally {
            Iotil.close(in);
        }
        Collections.sort(list, cmp);
        return list;
    }
    
    private final static JarEntryComparator cmp = new JarEntryComparator();
    
    private final static class JarEntryComparator implements Comparator<JarEntry> {
        @Override
        public final int compare(final JarEntry en1, final JarEntry en2) {
            return en1.getName().compareTo(en2.getName());
        }
    }
    
    private final static JarInputStream getJar(final String loc) throws Exception {
        return new JarInputStream(Iotil.getInputStream(loc));
    }
}
