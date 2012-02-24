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
package org.pandcorps.pandam;

import java.util.*;
import org.pandcorps.core.*;
import org.pandcorps.core.xml.*;
import org.pandcorps.pandam.impl.*;
import org.w3c.dom.*;

// Pandam Markup Language Game
public final class PanmlGame extends Pangame {
	public final static String NS_URI = "pandcorps.org/pandam";
	public final static String XSD = "org/pandcorps/pandam/panml.xsd";

	private final static Panframe[] EMPTY_ARRAY_FRAME = new Panframe[0];

	private final Pangine engine = Pangine.getEngine();
	private Panroom firstRoom;
	private final String location;

	public PanmlGame(final String location) {
		this.location = location;
	}

	@Override
	public void init()
	{
		//super.start();
		final Document doc = Xmltil.parse(location);
		Xmltil.validate(doc, Xmltil.parseSchema(XSD));
		final ChildElementIter iter = getElements(doc.getDocumentElement(), "Game");

		loadImages(getNext(iter, "Images"));
		Element next = iter.next();
		if (isNamed(next, "Frames")) {
			loadFrames(next);
			next = iter.next();
		}
		if (isNamed(next, "Animations")) {
			loadAnimations(next);
			next = iter.next();
		}
		loadTypes(next);
		/*firstRoom =*/ loadRooms(getNext(iter, "Rooms"));
	}

	private final void loadImages(final Element images) {
		for (final Element image : getElements(images, "Images")) {
			loadImage(image);
		}
	}

	private final void loadImage(final Element image) {
		final ChildElementIter iter = getElements(image, "Image");
		final String id = getText(iter, "Id"), location = getText(iter, "Location");
		final Panple origin;
		if (iter.hasNext()) {
		    origin = getTuple(iter, "Origin");
		} else {
		    origin = null;
		}
		//TODO boundMin/Max
		engine.createImage(id, origin, null, null, location);
	}

	private final void loadFrames(final Element frames) {
		for (final Element frame : getElements(frames, "Frames")) {
			loadFrame(frame);
		}
	}

	private final void loadFrame(final Element frame) {
		final ChildElementIter iter = getElements(frame, "Frame");
		engine.createFrame(
			getText(iter, "Id"),
			(Panmage) getEntity(iter, "ImageId"),
			getInt(iter, "Duration"));
	}

	private final void loadAnimations(final Element anims) {
		for (final Element anim : getElements(anims, "Animations")) {
			loadAnimation(anim);
		}
	}

	private final void loadAnimation(final Element anim) {
		final ChildElementIter iter = getElements(anim, "Animation");
		final String id = getText(iter, "Id");
		final ChildElementIter frameIter = getElements(iter, "FrameIds");
		final List<Panframe> frames = new ArrayList<Panframe>();
		while (frameIter.hasNext()) {
			frames.add((Panframe) getEntity(frameIter, "FrameId"));
		}
		engine.createAnimation(id, frames.toArray(EMPTY_ARRAY_FRAME));
	}

	private final void loadTypes(final Element types) {
		for (final Element type : getElements(types, "Types")) {
			loadType(type);
		}
	}

	/*
	TODO
	AnimationId doesn't make sense for images.
	Could change to ViewId.
	How will the Renderer (Panderer?) Panview be handled?
	If it needs to be handled differently,
	then we could keep AnimationId and add ImageId. 
	*/
	//TODO Add constants for element names
	@SuppressWarnings("unchecked")
	private final void loadType(final Element type) {
		final ChildElementIter iter = getElements(type, "Type");
		engine.createType(
			getText(iter, "Id"),
			(Class<? extends Panctor>) getClass(iter, "ActorClass"),
			(Panview) getEntity(iter, "AnimationId"));
	}

	private final void /*Panroom*/ loadRooms(final Element rooms) {
		final ChildElementIter iter = getElements(rooms, "Rooms");
		///*final Panroom*/ firstRoom = loadRoom(iter.next());
		for (Element room : iter) {
			loadRoom(room);
		}
		//return firstRoom;
	}

	private final Panroom loadRoom(final Element room) {
		final ChildElementIter iter = getElements(room, "Room");
		final String id = getText(iter, "Id");
		final ChildElementIter posList = getElements(iter, "Size");
		final Panroom r = engine.createRoom(
			id, getFloat(posList, "X"), getFloat(posList, "Y"), getFloat(posList, "Z"));
		if (firstRoom == null) {
		    firstRoom = r;
		}
		loadActors(getElements(iter, "Actors"), r);
		return r;
	}

	private final void loadActors(final ChildElementIter actors, final Panroom r) {
		for (final Element actor : actors) {
			loadActor(actor, r);
		}
	}

	private final void loadActor(final Element actor, final Panroom r) {
		final ChildElementIter iter = getElements(actor, "Actor");
		final String id = getText(iter, "Id"); // Need to read Id before TypeId
		final Panctor a = Pangine.getEngine().createActor(
			((Pantype) getEntity(iter, "TypeId")).getActorClass(),
			id);
		setTuple(a.getPosition(), iter, "Position");
		a.setVisible(getBoolean(iter, "Visible"));
		r.addActor(a);
	}
	
	private final void setTuple(final Panple tuple, final ChildElementIter iter, final String name) {
	    final ChildElementIter posIter = getElements(iter, name);
        tuple.set(
            getFloat(posIter, "X"), getFloat(posIter, "Y"), getFloat(posIter, "Z"));
	}
	
	private final Panple getTuple(final ChildElementIter iter, final String name) {
	    final Panple tuple = new ImplPanple(0, 0, 0);
	    setTuple(tuple, iter, name);
	    return tuple;
	}

	private final static ChildElementIter getElements
		(final Element node, final String localName) {
		assertNode(node, localName);
		return new ChildElementIter(node);
	}

	private final static Element getNext(final ChildElementIter iter, final String localName) {
		final Element elem = iter.next();
		assertNode(elem, localName);
		return elem;
	}

	private final static ChildElementIter getElements
		(final ChildElementIter iter, final String localName) {
		final Element node = getNext(iter, localName);
		return new ChildElementIter(node);
	}

	private final static String getText(final ChildElementIter iter, final String localName) {
		final Element node = getNext(iter, localName);
		return node.getTextContent();
	}

	private final Pantity getEntity(final ChildElementIter iter, final String localName) {
		return engine.getEntity(getText(iter, localName));
	}

	private final Class<?> getClass(final ChildElementIter iter, final String localName) {
		try {
			return Class.forName(getText(iter, localName));
		} catch (final ClassNotFoundException e) {
			throw Pantil.toRuntimeException(e);
		}
	}

	//private final void set(final Panple<?>)

	private final int getInt(final ChildElementIter iter, final String localName) {
		return Integer.parseInt(getText(iter, localName));
	}

	private final float getFloat(final ChildElementIter iter, final String localName) {
		return Float.parseFloat(getText(iter, localName));
	}

	private final boolean getBoolean(final ChildElementIter iter, final String localName) {
		return Boolean.parseBoolean(getText(iter, localName));
	}

	private final static void assertNode(final Element node, final String localName) {
		if (!isNamed(node, localName)) {
			throw new Panception(
				"Expected " + NS_URI + ':' + localName +
				" but found " + node.getNamespaceURI() + ':' + node.getLocalName());
		}
	}

	private final static boolean isNamed(final Element node, final String localName) {
		return localName.equals(node.getLocalName()) && NS_URI.equals(node.getNamespaceURI());
	}
	
	@Override
	protected final FinPanple getFirstRoomSize() {
	    throw new UnsupportedOperationException();
	}

	@Override
    protected final void init(final Panroom room) throws Exception {
        throw new UnsupportedOperationException();
    }

	@Override
	protected final Panroom getFirstRoom() {
		return firstRoom;
	}
}
