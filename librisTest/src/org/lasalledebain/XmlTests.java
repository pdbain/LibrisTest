package org.lasalledebain;

import junit.framework.TestCase;

import org.lasalledebain.libris.xmlUtils.ElementWriter;


public class XmlTests extends TestCase {

	public void testXmlOutput () {
		try {
			ElementWriter w = ElementWriter.eventWriterFactory(System.out);
			w.writeStartElement("foo");
			w.flush();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	public void testXmlOutputWithAttributes () {
		try {
			ElementWriter w = ElementWriter.eventWriterFactory(System.out);
			w.writeStartElement("foo");
			w.flush();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
