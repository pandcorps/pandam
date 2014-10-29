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
package org.pandcorps.pandam.android;

import java.io.*;
import java.util.*;

import org.pandcorps.core.*;
import org.pandcorps.pandam.*;
import org.pandcorps.pandam.impl.*;

public class AndroidPangine extends GlPangine {
	protected static AndroidPangine engine = null;
	protected static PanActivity context = null;
	private static PanClipboard clip = null;
	protected static int desktopWidth = 0;
	protected static int desktopHeight = 0;
	private static Set<String> cacheFiles = null;
	
	protected AndroidPangine() {
		super(new AndroidPanteraction());
		Pangine.engine = this;
		engine = this;
		audio = new AndroidPanaudio();
	}
	
	@Override
    public final int getDesktopWidth() {
        return desktopWidth;
    }
    
    @Override
    public final int getDesktopHeight() {
    	return desktopHeight;
    }

    @Override
    protected final void initDisplay() throws Exception {
	    if (!fullScreen) {
	        throw new Exception("AndroidPangine requires full-screen mode");
	    }
	}
	
    @Override
	protected void initInput() throws Exception {
	}

    @Override
	protected void stepControl() throws Exception {
		/*if (isCloseRequested()) {
			exit();
		}*/
    	stepTouch();
	}
    
    @Override
    public final boolean isTouchSupported() {
    	return true;
    }
    
    //public final void playMusic(/*final*/ String loc) {
    	/*InputStream in = null;
    	Parcel parcel = null;
    	ParcelFileDescriptor pfd = null;
    	AssetFileDescriptor afd = null;
    	try {
    		parcel = Parcel.obtain();
    		final int len = 1024;
    		int ret;
    		final byte[] buf = new byte[len];
    		in = Iotil.getResourceInputStream(loc);
    		while ((ret = in.read(buf)) >= 0) {
    			parcel.writeByteArray(buf, 0, ret);
    		}
    		in.close();
    		parcel.setDataPosition(0);
    		System.out.println("parcel data size: " + parcel.dataSize());
    		pfd = parcel.readFileDescriptor();
    		System.out.println("pfd: " + pfd);
	    	//final File f = new File(Iotil.class.getClassLoader().getResource(loc).toURI());
	    	//pfd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
	    	afd = new AssetFileDescriptor(pfd, 0, AssetFileDescriptor.UNKNOWN_LENGTH);
	    	System.out.println("afd: " + afd);
	    	final int soundId = soundPool.load(afd, 1);
	    	musicStreamId = soundPool.play(soundId, 1, 1, 1, -1, 1);
    	} catch (final Exception e) {
    		throw Panception.get(e);
    	} finally {
    		Iotil.close(in);
    		//Iotil.close(afd); // Compiles, but run-time error
    		close(afd);
    		Iotil.close(pfd);
    		recycle(parcel);
    	}*/
    	
    	/*InputStream in = null;
    	ParcelFileDescriptor pfd = null, pfdOut = null;
    	AssetFileDescriptor afd = null;
    	OutputStream out = null;
    	try {
    		final ParcelFileDescriptor[] pfds = ParcelFileDescriptor.createPipe();
    		pfd = pfds[0];
    		pfdOut = pfds[1];
    		out = new ParcelFileDescriptor.AutoCloseOutputStream(pfdOut);
    		final int len = 1024;
    		int ret;
    		final byte[] buf = new byte[len];
    		//in = Iotil.getResourceInputStream(loc);
    		System.out.println("Getting resource input stream");
    		in = Iotil.getResourceInputStream("org/pandcorps/platform/res/music/chimes.wav");
    		long size = 0;
    		while ((ret = in.read(buf)) >= 0) {
    			out.write(buf, 0, ret);
    			size += ret;
    			System.out.println("Piping, size " + size);
    		}
    		in.close();
    		out.close();
    		System.out.println("Opening AssetFileDescriptor");
	    	afd = new AssetFileDescriptor(new PanParcelFileDescriptor(pfd, size), 0, size);
	    	System.out.println("afd: " + afd);
	    	//final int soundId = soundPool.load(afd, 1);
	    	soundPool.load(afd, 1);
	    	soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
				@Override
				public final void onLoadComplete(final SoundPool soundPool, final int sampleId, final int status) {
					System.out.println("Load complete, trying to play");
					musicStreamId = soundPool.play(sampleId, 1, 1, 1, -1, 1);
				}});
	    	//soundPool.load(context, R.raw.chimes, 1); // Works
	    	//soundPool.load(context, R.raw.happy, 1); // Unable to load sample: (null)|Load complete, trying to play|sample 1 not READY
	    	//musicStreamId = soundPool.play(soundId, 1, 1, 1, -1, 1);
    	} catch (final Exception e) {
    		throw Panception.get(e);
    	} finally {
    		Iotil.close(in);
    		Iotil.close(out);
    		//Iotil.close(afd); // Compiles, but run-time error
    		//Iotil.close(pfd);
    		//Iotil.close(pfdOut);
    		close(afd);
    		close(pfd);
    		close(pfdOut);
    	}*/
    //}
    
    //private FileInputStream fin = null;
    
    protected final static class CopyResult {
    	protected final String fileName;
    	protected final long size;
    	
    	private CopyResult(final String fileName, final long size) {
    		this.fileName = fileName;
    		this.size = size;
    	}
    	
    	protected final FileInputStream openInputStream() throws Exception {
    		//return context.openFileInput(fileName);
    		return new FileInputStream(fileName); // We use cache directory, not app's private directory
    	}
    }
    
    protected final static CopyResult copyResourceToFile(final String loc) throws Exception {
    	InputStream in = null;
    	OutputStream out = null;
    	try {
    		String tmpFileName = loc.replace('/', '_');
    		
    		//out = context.openFileOutput(tmpFileName, Context.MODE_PRIVATE);
    		String dir = context.getCacheDir().getAbsolutePath();
    		if (!dir.endsWith("/")) {
    			dir += "/";
    		}
    		tmpFileName = dir + tmpFileName;
    		out = new FileOutputStream(tmpFileName);
    		
    		final int len = 1024;
    		int ret;
    		final byte[] buf = new byte[len];
    		in = Iotil.getResourceInputStream(loc);
    		System.out.println("Getting resource input stream");
    		long size = 0;
    		while ((ret = in.read(buf)) >= 0) {
    			out.write(buf, 0, ret);
    			size += ret;
    			System.out.println("Piping, size " + size);
    		}
    		if (cacheFiles == null) {
    			cacheFiles = new HashSet<String>();
    		}
    		cacheFiles.add(tmpFileName);
    		return new CopyResult(tmpFileName, size);
    	} finally {
    		Iotil.close(in);
    		Iotil.close(out);
    	}
    }
    
    /*private final static class PanParcelFileDescriptor extends ParcelFileDescriptor {
    	private final long size;
    	
		public PanParcelFileDescriptor(final ParcelFileDescriptor wrapped, final long size) {
			super(wrapped);
			this.size = size;
		}
		
		@Override
		public final long getStatSize() {
			return size;
		}
    }
    
    private final static void close(final AssetFileDescriptor fd) {
    	if (fd != null) {
    		try {
    			fd.close();
    		} catch (final Exception e) {
    			//throw Pantil.toRuntimeException(e);
    		}
    	}
    }
    
    private final static void close(final ParcelFileDescriptor fd) {
    	if (fd != null) {
    		try {
    			fd.close();
    		} catch (final Exception e) {
    			//throw Pantil.toRuntimeException(e);
    		}
    	}
    }*/
    
    /*private final static void recycle(final Parcel parcel) {
    	if (parcel != null) {
			parcel.recycle();
    	}
    }*/
    
    //public final void stopMusic() {
    	/*mediaPlayer.stop();
		mediaPlayer.reset();*/
    	//soundPool.stop(musicStreamId); // Also releases sound from memory?
    //}
	
    @Override
	protected void onDestroy() {
    	for (final String cacheFile : Coltil.unnull(cacheFiles)) {
    		new File(cacheFile).delete();
    	}
    	/*if (mediaPlayer != null) {
    		mediaPlayer.release();
    	}*/
	}
	
    @Override
	protected void update() {
	}
    
    private final PanClipboard getClip() {
    	if (clip == null) {
    		try {
    			Class.forName("android.content.ClipboardManager");
    			clip = new ContentClipboard();
    		} catch (final Throwable e) {
    			clip = new TextClipboard();
    		}
    	}
    	return clip;
    }
    
    @Override
    public final String getClipboard() {
    	return getClip().getClipboard();
	}
	
	@Override
	public final void setClipboard(final String value) {
    	getClip().setClipboard(value);
	}
	
	@Override
	public final void setTitle(final String title) {
	}
	
	@Override
	public final void setIcon(final String... locations) {
	}
	
	protected final boolean isRunning() {
		return running;
	}
	
	protected final void runDestroy() throws Exception {
		destroy();
	}
}
