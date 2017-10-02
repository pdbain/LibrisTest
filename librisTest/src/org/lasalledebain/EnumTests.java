package org.lasalledebain;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;


public class EnumTests extends TestCase {
	public void testAddChoice() {
		String choices [] = {"toe", "foo", "bar"};

		EnumFieldChoices es = new EnumFieldChoices("set1");
		for (int i=0; i<choices.length; ++i) {
			try {
				es.addChoice(choices[i]);
			} catch (Exception e) {
				e.printStackTrace();
				fail("Unexpected exception");
			}
			String [] actual = es.getChoices();
			assertEquals("wrong number of choices", i+1, actual.length);
			for (int j=0; j<= i; j++) {
				assertEquals("wrong data for choice # "+j, choices[j], actual[j]);
			}
		}
		for (int j=0; j < choices.length; j++) {
			int choiceNum;
			try {
				choiceNum = es.indexFromId(choices[j]);
				assertEquals("wrong data for choice # "+j, j, choiceNum);
			} catch (FieldDataException e) {
				e.printStackTrace();
				fail();
			}
		}
		for (int j= choices.length-1; j <= 0; --j) {
			try {
				String actual = es.getChoiceId(j);
				assertEquals(choices[j], actual);
			} catch (FieldDataException e) {
				e.printStackTrace();
				fail();
			}
		}
	}
	/**
	 * @testcase testChoiceTitles
	 * @purpose test that field titles are correctly set and retrieved
	 * @method
	 * @expect
	 * @variant
	 */
	public void testChoiceTitle() {
		final String TITLE_NUMBER_2 = "title number 2";
		EnumFieldChoices es = new EnumFieldChoices("set2");
		try {
			es.addChoice("c1");
			es.addChoice("c2", TITLE_NUMBER_2);
		} catch (DatabaseException e) {			
			e.printStackTrace();
			fail("Unexpected exception");
		}
		try {
			assertEquals("c1", es.getChoiceId(0));
			assertEquals("c1", es.getChoiceValue(0));
			assertEquals("c2", es.getChoiceId(1));
			assertEquals(TITLE_NUMBER_2, es.getChoiceValue(1));
		} catch (FieldDataException e) {
			e.printStackTrace();
			fail();
		}	
	}
	
	public void testChoiceError() {
		final String TITLE_NUMBER_2 = "title number 2";
		EnumFieldChoices es = new EnumFieldChoices("set3");
		try {
			es.addChoice("c1");
			es.addChoice("c2", TITLE_NUMBER_2);
		} catch (DatabaseException e) {			
			e.printStackTrace();
			fail("Unexpected exception");
		}

		boolean excThrown = false;
		try {
			es.getChoiceId(99);
		} catch (FieldDataException e) {
			excThrown = true;
		}
		assertTrue("no exception for invalid argument to getChoiceId(99)", excThrown);
		excThrown = false;
		try {
			es.getChoiceId(-1);
		} catch (FieldDataException e) {
			excThrown = true;
		}
		assertTrue("no exception for invalid argument to getChoiceId(-1)", excThrown);
		excThrown = false;
		try {
			es.getChoiceValue(2);
		} catch (FieldDataException e) {
			excThrown = true;
		}
		assertTrue("no exception for invalid argument to getChoiceId(2)", excThrown);
		excThrown = false;
		try {
			es.indexFromId("foobar");
		} catch (FieldDataException e) {
			excThrown = true;
		}
		assertTrue("no exception for invalid argument to indexFromId", excThrown);
	}
	
	public void testXmlInput() {
		String testData = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
				"<enumset id=\"testXmlInput\">\n" +
				"\t<enumchoice id=\"xc1\" value=\"v1\"/>\n" +
				"\t<enumchoice id=\"xc2\" value=\"v2\"/>\n" +
				"\t<enumchoice id=\"xc3\" value=\"v3\"/>\n" +
				"</enumset>";
		ByteArrayInputStream xmlInput = new ByteArrayInputStream(testData.getBytes());
		EnumFieldChoices ef = new EnumFieldChoices("mySet");
		try {
			ef.fromXml(Utilities.makeElementManager(xmlInput, null, EnumFieldChoices.getXmlTag()));
			assertEquals("xc1", ef.getChoiceId(0));
			assertEquals("v2", ef.getChoiceValue(1));
			assertEquals("v3", ef.getChoiceValue(2));
			String[] cs = ef.getChoices();
			assertEquals(3, cs.length);
		} catch (LibrisException e) {
			e.printStackTrace();
			fail();
		}
	}

}
