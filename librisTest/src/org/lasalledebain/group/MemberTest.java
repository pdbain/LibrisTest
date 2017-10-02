package org.lasalledebain.group;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.lasalledebain.MockSchema;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.index.GroupDef;
import org.lasalledebain.libris.index.GroupDefs;
import org.lasalledebain.libris.index.GroupMember;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementWriter;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;


public class MemberTest extends TestCase implements LibrisXMLConstants {

	private static final String TEST_TITLE = "testTitle";
	private static final String TEST_GROUP1 = "testGroup";
	static final String simpleMember =
		"<member group=\""+TEST_GROUP1+"\" parent=\"1\" title=\"testTitle\" />";
	static final String affiliate =
		"<member group=\""+TEST_GROUP1+"\" parent=\"3\" >" +
		"<affiliation affiliate=\"1\">"+
		"<affiliation affiliate=\"2\">"+
				"</member>";
	private GroupDefs defs;
	@Override
	protected void setUp() throws Exception {
		defs = new GroupDefs();
		defs.addGroup(new GroupDef(new MockSchema(), TEST_GROUP1, TEST_TITLE, 0));
	}
	public void testFromToXml() {
		try {
			String elementString = simpleMember;
			GroupMember gmm = parseAndCheckGroupMember(elementString);
			elementString = memberToXml(gmm);
			parseAndCheckGroupMember(elementString);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	private String memberToXml(GroupMember gmm) throws XMLStreamException,
			LibrisException {
		String elementString;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ElementWriter ew = ElementWriter.eventWriterFactory(bos);
		gmm.toXml(ew);
		ew.flush();
		elementString = bos.toString();
		return elementString;
	}
	private GroupMember parseAndCheckGroupMember(String elementString)
			throws LibrisException {
		ElementManager mgr = Utilities.makeElementManagerFromReader(new StringReader(elementString), null,
				XML_MEMBER_TAG);
		GroupMember gmm = new GroupMember(defs, null);
		gmm.fromXml(mgr);
		int actualParent = gmm.getParent();
		assertEquals("wrong id", 1, actualParent);
		assertEquals("wrong group", TEST_GROUP1, gmm.getGroupId());
		assertEquals("wrong title", TEST_TITLE, gmm.getTitle());
		return gmm;
	}
	public void testAffiliations() {
		ElementManager mgr = Utilities.makeElementManagerFromReader(new StringReader(affiliate), null,
				XML_MEMBER_TAG);
		GroupMember gmm = new GroupMember(defs, null);
		try {
			gmm.fromXml(mgr);
			assertEquals("wrong id", gmm.getParent(), 3);
			assertEquals("wrong group", gmm.getGroupId(), TEST_GROUP1);
			Iterator<Integer> expectedIds = Arrays.asList(new Integer[] {3,1,2}).iterator();
			for (int a: gmm.getAffiliations()) {
				assertTrue("too few affiliates", expectedIds.hasNext());
				 Integer expectedId = expectedIds.next();
				 assertEquals("wrong ID", expectedId.intValue(), a);
			}
		} catch (LibrisException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	public int getFieldNum() {
		return 0;
	}
	public String getId() {
		return null;
	}
	public String getTitle() {
		return null;
	}
}
