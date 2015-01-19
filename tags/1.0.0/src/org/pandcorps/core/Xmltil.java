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
package org.pandcorps.core;

import java.io.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;
import org.w3c.dom.*;

// XML Utility
public final class Xmltil {
	private final static DocumentBuilder parser;
	private final static SchemaFactory schemaParser;

	static {
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(true);
			factory.setIgnoringElementContentWhitespace(true);
			factory.setNamespaceAware(true);
			parser = factory.newDocumentBuilder();

			schemaParser = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		} catch (final Exception e) {
			throw new Error(e);
		}
	}

	private Xmltil() {
		throw new Error();
	}

	public final static Document parse(final String location) {
		final InputStream in = Iotil.getInputStream(location);

		try {
			return parser.parse(in);
		} catch (final Exception e) {
			throw Pantil.toRuntimeException(e);
		} finally {
			Iotil.close(in);
		}
	}

	public final static Schema parseSchema(final String location) {
		final InputStream in = Iotil.getInputStream(location);

		try {
			return schemaParser.newSchema(new StreamSource(in));
		} catch (final Exception e) {
			throw Pantil.toRuntimeException(e);
		} finally {
			Iotil.close(in);
		}
	}

	public final static void validate(final String docLocation, final String schemaLocation) {
		final InputStream in = Iotil.getInputStream(docLocation);

		try {
			parseSchema(schemaLocation).newValidator().validate(new StreamSource(in));
		} catch (final Exception e) {
			throw Pantil.toRuntimeException(e);
		} finally {
			Iotil.close(in);
		}
	}

	public final static void validate(final Document doc, final Schema schema) {
		validate(doc, schema.newValidator());
	}

	public final static void validate(final Document doc, final Validator validator) {
		try {
			validator.validate(new DOMSource(doc));
		} catch (final Exception e) {
			throw Pantil.toRuntimeException(e);
		}
	}
}
