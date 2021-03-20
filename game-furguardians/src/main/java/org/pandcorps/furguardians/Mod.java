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

import java.io.*;
import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.core.io.*;
import org.pandcorps.furguardians.Player.*;
import org.pandcorps.pandam.*;

public final class Mod {
    protected final static void installFromClipboard(final Handler<CharSequence> statusHandler) {
        try {
            Pangine.getEngine().getClipboard(new Handler<String>() {
                @Override public final void handle(final String event) {
                    install(event, statusHandler);
                }});
        } catch (final Throwable e) {
            statusHandler.handle(Pantil.getAbbreviatedStackTrace(e));
        }
    }
    
    protected final static void install(final String encoded, final Handler<CharSequence> statusHandler) {
        final StringBuilder successLog = new StringBuilder();
        CharSequence status = successLog;
        try {
            final byte[] decoded = Base64Decoder.decode(encoded.trim());
            successLog.append("Zip ").append(decoded.length);
            Iotil.unzip(new ByteArrayInputStream(decoded), FurGuardiansGame.MOD, successLog);
            for (final PlayerContext pc : FurGuardiansGame.pcs) {
                pc.profile.installModChrs();
            }
            successLog.append("\nSuccess!");
        } catch (final Throwable e) {
            status = Pantil.getAbbreviatedStackTrace(e);
        }
        statusHandler.handle(status);
    }
    
    protected final static void exportChr(final String name) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Iotil.zipDir(FurGuardiansGame.MOD_CHR + FurGuardiansGame.SEP + name, out, FurGuardiansGame.CHR + FurGuardiansGame.SEP + name);
        final String encoded = Base64.getEncoder().encodeToString(out.toByteArray());
        Pangine.getEngine().setClipboard(encoded);
    }
}
