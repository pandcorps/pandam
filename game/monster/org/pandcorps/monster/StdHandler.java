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

public class StdHandler extends Handler {
    private final static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    
    private static Driver driver = null; //TODO Present list of users in menu or ask for new user; no password required for local session
    
    @Override
    public Option handle(final Option caller, final Label label, final List<? extends Option> options) {
        System.out.println(label.getName());
        final int optionsSize = options.size();
        Option chosen;
        while (true) {
            for (int i = 0; i < optionsSize; i++) {
                final Option option = options.get(i);
                if (option.isPossible()) {
                    System.out.print(i + 1); // SYNC with options.get
                } else {
                    System.out.print('X');
                }
                System.out.print(". ");
                final Label goal = option.getGoal();
                System.out.println(goal.getName());
                final List<Entity> required = option.getRequired();
                final int requiredSize = required.size();
                if (requiredSize > 0) {
                    System.out.print("\tNeed: ");
                    for (int j = 0; j < requiredSize; j++) {
                        if (j > 0) {
                            System.out.print(", ");
                        }
                        final Entity requirement = required.get(j);
                        System.out.print(requirement.getName());
                        System.out.print(" (");
                        System.out.print(requirement.isAvailable() ? 'Y' : 'N');
                        System.out.print(')');
                    }
                    System.out.println();
                }
                final List<Entity> awarded = option.getAwarded();
                final int awardedSize = awarded.size();
                if (awardedSize > 0 && (awardedSize > 1 || !awarded.get(0).equals(goal))) {
                    System.out.print("\tEarn: ");
                    for (int j = 0; j < awardedSize; j++) {
                        if (j > 0) {
                            System.out.print(", ");
                        }
                        System.out.print(awarded.get(j).getName());
                    }
                    System.out.println();
                }
            }
            int index;
            try {
                index = Integer.parseInt(in.readLine());
            } catch (final IOException e) {
                throw new RuntimeException(e);
            } catch (final NumberFormatException e) {
                index = -1;
            }
            if (index < 1 || index > optionsSize) {
                System.out.println("Enter an integer from 1 to " + optionsSize);
                continue;
            }
            chosen = options.get(index - 1); // SYNC with menu's offset
            if (!chosen.isPossible()) {
                System.out.println("Requirements are not met"); //TODO Give better message, maybe point to another task to get necessary resources
                continue;
            } else {
                break;
            }
        }
        return chosen;
    }
    
    @Override
    public Driver getDriver() {
        return driver;
    }
    
    public final static void main(final String[] args) {
        try {
            Handler.implClass = StdHandler.class;
            Handler.get(); // Must instantiate Handler to call Parser.run
            driver = new Driver(new State("Standard"));
            driver.run();
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }
}
