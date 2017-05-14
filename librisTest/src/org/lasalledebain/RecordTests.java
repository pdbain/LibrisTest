package org.lasalledebain;

import static org.lasalledebain.Utilities.info;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import junit.framework.TestCase;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordTemplate;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.indexes.ExactKeywordList;
import org.lasalledebain.libris.indexes.PrefixKeywords;
import org.lasalledebain.libris.indexes.RecordKeywords;
import org.lasalledebain.libris.xmlUtils.ElementWriter;

public class RecordTests extends TestCase {
	private static final String FIELDNAME = "f1";

	public void testNewRecord() {
		FieldTemplate booleanTemplate = Utilities.createTemplate(FIELDNAME, FieldType.T_FIELD_BOOLEAN);
		try {
			MockSchema dbSchema = new MockSchema();
			dbSchema.addField(booleanTemplate);
			RecordTemplate rt = RecordTemplate.templateFactory(dbSchema, null);
			Record ri = rt.makeRecord(false);
			Field f = ri.getField(FIELDNAME);
			assertNull("non-default field not null", f);
			ri.addFieldValue(FIELDNAME, "true");
			f = ri.getField(FIELDNAME);
			assertTrue(f.isTrue());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}

	public void testMultiFieldRecord() {
		try {
			String fieldNames[] = {"bool", "int", "string"};
			FieldType fts[] = {FieldType.T_FIELD_BOOLEAN, FieldType.T_FIELD_INTEGER, FieldType.T_FIELD_STRING};

			RecordTemplate rt = Utilities.makeRecordTemplate(fieldNames, fts);
			Record r1 = rt.makeRecord(false);
			Record r2 = rt.makeRecord(false);
			String r1Data[] = {"true", "1", "foo"};
			String r2Data[] = {"false", "2", "bar"};
			r1.setAllFields(r1Data);
			for (int i=0; i<3; i++) {
				Field f = r2.getField(fieldNames[i]);
				assertNull("empty field not null", f);
				r2.addFieldValue(i, r2Data[i]);
			}
			for (int i = 0; i < fts.length; ++i) {
				String actual = r1.getField(fieldNames[i]).getValuesAsString();
				assertEquals("r1 data for field "+fieldNames[i], r1Data[i], actual);
				actual = r2.getField(fieldNames[i]).getValuesAsString();
				assertEquals("r2 data for field "+fieldNames[i], r2Data[i], actual);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e);
		}
	}
	/**
	 * @testcase invalidData
	 * @purpose test exceptions when adding the wrong data types
	 * @method provide invalid data to boolean, int fields
	 * @expect exception
	 * @variant
	 */
	public void testInvalidData() {
		try {
			String fieldNames[] = {"bool", "int", "string"};
			FieldType fts[] = {FieldType.T_FIELD_BOOLEAN, FieldType.T_FIELD_INTEGER, FieldType.T_FIELD_STRING};

			RecordTemplate rt = Utilities.makeRecordTemplate(fieldNames, fts);
			Record r1 = rt.makeRecord(true);
			boolean excThrown = false;
			String r1Data[] = {"1", "foo", "true"};
			for (int i = 0; i < fieldNames.length; ++i) {
				excThrown = false;
				try {
					r1.addFieldValue(fieldNames[i], r1Data[i]);
				} catch (InputException e) {
					excThrown = true;
				}
				if (FieldType.T_FIELD_STRING != fts[i]) {
					assertTrue("fieldDataException not thrown for field "+fieldNames[i], excThrown);
				}
			}
		} catch (LibrisException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	public void testXmlLoad() {
		File testDir = Utilities.getTestDataDirectory();
		File schemaFile = new File(testDir, Utilities.TEST_SCHEMA_XML_FILE);
		File recordFile = new File(testDir, Utilities.TEST_RECORD1_XML_FILE);
		try {
			Record rec = Utilities.loadRecordFromXml(schemaFile, recordFile);
			String[][] expectedIds = {
					{"titl", "Kim"},
					{"pubn", "publication"},
					{"pubr", "publisher"},
					{"volu", "100"},
					{"numb", "9"},
					{"mont", "June"},
					{"year", "1066"},
					{"abst", "abstract"},
					{"keyw", "India British Empire Russia espionage"}};
			for (String[] idValue: expectedIds) {
				Field f = rec.getField(idValue[0]);
				assertNotNull("field "+expectedIds[0]+ " not found", f);
				assertEquals("wrong value for field "+idValue[0], idValue[1], f.getValuesAsString());
			}
			String name = rec.getName();
			assertEquals("wrong name", "Record1", name);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	public void testFieldTypes() {
		File testDir = Utilities.getTestDataDirectory();
		File recordFile = new File(testDir, Utilities.TEST_RECORD2_XML_FILE);
		File schemaFile = new File(testDir, Utilities.TEST_SCHEMA2_XML_FILE);
		try {
			FieldType[] expectedFieldTypes = {FieldType.T_FIELD_AFFILIATES, FieldType.T_FIELD_AFFILIATES, FieldType.T_FIELD_STRING,
					FieldType.T_FIELD_BOOLEAN, FieldType.T_FIELD_BOOLEAN, FieldType.T_FIELD_STRING, 
					FieldType.T_FIELD_PAIR, FieldType.T_FIELD_PAIR, FieldType.T_FIELD_PAIR, FieldType.T_FIELD_INTEGER};
			Schema schem = Utilities.loadSchema(schemaFile);
			Record rec = Utilities.loadRecordFromXml(schemaFile, recordFile);
			String[] fids = schem.getFieldIds();
			StringBuilder sb  = new StringBuilder();
			sb.append("schema field ids: ");
			for (int i = 0; i < fids.length; ++i) {
				System.out.print(" ");
				String fid = fids[i];
				sb.append(fid);
				FieldType fType = schem.getFieldType(fid);
				sb.append("("+fType+")");
				assertEquals("schema: wrong types loaded", expectedFieldTypes[i], fType);
			}
			info(sb.toString());
			
			sb = new StringBuilder();
			sb.append("\nrecord field ids: ");
			fids = rec.getFieldIds();
			assertNotNull("record field list", fids);
			for (int i = 0; i < fids.length; ++i) {
				sb.append(" ");
				String fid = fids[i];
				assertNotNull("record field", fid);
				sb.append(fid);
				FieldType fType = rec.getField(fid).getType();
				FieldType sType = schem.getFieldType(fid);
				assertEquals("record: wrong types loaded", sType, fType);
			}
			info(sb.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getClass().getName());
		}
	}
	public void testFieldToXml() {
		File testDir = Utilities.getTestDataDirectory();
		File recordFile = new File(testDir, Utilities.TEST_RECORD2_XML_FILE);
		File schemaFile = new File(testDir, Utilities.TEST_SCHEMA2_XML_FILE);
		try {
			Record rec = Utilities.loadRecordFromXml(schemaFile, recordFile);
			ByteArrayOutputStream xmlOutput = new ByteArrayOutputStream();
			ElementWriter xmlWriter = ElementWriter.eventWriterFactory(xmlOutput);
			rec.toXml(xmlWriter);
			xmlWriter.flush();
			byte[] xmlText = xmlOutput.toByteArray();
			System.out.write(xmlText);
			 Utilities.loadRecordFromXml(schemaFile, new ByteArrayInputStream(xmlText), null);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getClass().getName());
		}
	}

	public void testRecordDuplicate() {
		File testDir = Utilities.getTestDataDirectory();
		File recordFile = new File(testDir, Utilities.TEST_RECORD2_XML_FILE);
		File schemaFile = new File(testDir, Utilities.TEST_SCHEMA2_XML_FILE);
		try {
			Record rec = Utilities.loadRecordFromXml(schemaFile, recordFile);
			Record otherRec = rec.duplicate();
			otherRec.setRecordId(rec.getRecordId());
			
			assertEquals("Duplicate record does not match\n", rec, otherRec);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getClass().getName());
		}
	}

	public void testRecordDuplicateWithEnums() {
		File testDir = Utilities.getTestDataDirectory();
		File recordFile = new File(testDir, Utilities.TEST_RECORD_WITH_ENUM_XML_FILE);
		File schemaFile = new File(testDir, Utilities.TEST_SCHEMA_ENUMDEFS_XML_FILE);
		try {
			Record rec = Utilities.loadRecordFromXml(schemaFile, recordFile);
			Record otherRec = rec.duplicate();
			otherRec.setRecordId(rec.getRecordId());
			
			assertEquals("Duplicate record does not match\n", rec, otherRec);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getClass().getName());
		}
	}

	public void testRecordCompareMismatch() {
		File testDir = Utilities.getTestDataDirectory();
		File record2File = new File(testDir, Utilities.TEST_RECORD2_XML_FILE);
		File record3File = new File(testDir, Utilities.TEST_RECORD3_XML_FILE);
		File schemaFile = new File(testDir, Utilities.TEST_SCHEMA2_XML_FILE);
		try {
			Record rec2 = Utilities.loadRecordFromXml(schemaFile, record2File);
			Record rec3 = Utilities.loadRecordFromXml(schemaFile, record3File);
			assertFalse("rec2 should not equal rec3", rec2.equals(rec3));
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getClass().getName());
		}
	}

	public void testRecordCompareMatch() {
		File testDir = Utilities.getTestDataDirectory();
		File record2File = new File(testDir, Utilities.TEST_RECORD2_XML_FILE);
		File record2bFile = new File(testDir, Utilities.TEST_RECORD2_XML_FILE);
		File schemaFile = new File(testDir, Utilities.TEST_SCHEMA2_XML_FILE);
		try {
			Record rec2 = Utilities.loadRecordFromXml(schemaFile, record2File);
			Record rec2b = Utilities.loadRecordFromXml(schemaFile, record2bFile);
			assertTrue("rec2 should  equal rec2b", rec2.equals(rec2b));
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getClass().getName());
		}
	}
	
	public void testFieldOrder() {
		File testDir = Utilities.getTestDataDirectory();
		File record2File = new File(testDir, Utilities.TEST_RECORD2_XML_FILE);
		File schemaFile = new File(testDir, Utilities.TEST_SCHEMA2_XML_FILE);
		try {
			Record rec = Utilities.loadRecordFromXml(schemaFile, record2File);
			Schema s = Utilities.loadSchema(schemaFile);
			String[] schemaFields = s.getFieldIds();
			String[] recordFields = rec.getFieldIds();
			int sfi = 0;
			for (String fid: recordFields) {
				while ((sfi < schemaFields.length) && (schemaFields[sfi] != fid)) {
					++sfi;
				}
				assertTrue("Incorrect field order for "+fid, sfi < schemaFields.length);
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getClass().getName());
		}
	}
	
	public void testSchemaGroups() {
		File testDir = Utilities.getTestDataDirectory();
		File schemaFile = new File(testDir, Utilities.TEST_SCHEMA2_XML_FILE);
		try {
			Schema schem = Utilities.loadSchema(schemaFile);
			String groupIds[] = {"group1", "group2"};
			Iterator<String> actualIds = schem.getGroupIds().iterator();
			
			for (int i = 0; i < groupIds.length; ++i) {
				assertTrue("too few groups", actualIds.hasNext());
				String actualId = actualIds.next();
				assertEquals("wrong ID", groupIds[i], actualId);
			}
			assertFalse("too many groups", actualIds.hasNext());

		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getClass().getName());
		}
	}
	
	public void testRecordGroups() {
		File testDir = Utilities.getTestDataDirectory();
		File recordFile = new File(testDir, Utilities.TEST_RECORD_WITH_GROUPS_XML_FILE);
		File schemaFile = new File(testDir, Utilities.TEST_SCHEMA2_XML_FILE);
		try {
			Record rec = Utilities.loadRecordFromXml(schemaFile, recordFile);
			int parent = rec.getParent(0);
			assertEquals("wrong parent for group1", 0, parent);
			int[] affiliates = rec.getAffiliates(0);
			assertTrue("Parent missing", affiliates.length == 0);
			assertEquals("wrong parent for group1", 0, parent);
			assertFalse("group1 affilates not empty", affiliates.length > 1);
			affiliates = rec.getAffiliates(1);
			parent = rec.getParent(1);
			assertEquals("wrong parent for group2", 10, parent);
			affiliates = rec.getAffiliates(1);
			Iterator<Integer> expectedAffiliates = Arrays.asList((new Integer[] {10, 20, 30})).iterator();
			for (int id: affiliates) {
				assertTrue("too many actual affiliates", expectedAffiliates.hasNext());
				int actualId = id;
				int expectedId = expectedAffiliates.next();
				assertEquals("wrong affiliate ID", expectedId, actualId);
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getClass().getName());
		}
	}

	public void testDefaultFields() {
		File testDir = Utilities.getTestDataDirectory();
		File recordFile = new File(testDir, Utilities.TEST_RECORD_WITH_MISSING_FIELDS);
		File schemaFile = new File(testDir, Utilities.SCHEMA_WITH_DEFAULTS);
		try {
			Record rec = Utilities.loadRecordFromXml(schemaFile, recordFile);
			rec.setEditable(false);
			String val = rec.getFieldValue("ID_auth").getValueAsString();
			assertEquals("author field wrong value","Auth1", val);
			val = rec.getField("ID_title").getValuesAsString();
			assertEquals("title field wrong value","Title1", val);
			Field fld = rec.getField("ID_keywords");
			assertNull("keywords field not null", fld);
			fld = rec.getField("ID_publisher");
			assertEquals("publisher field not null",null, fld);
			val = rec.getFieldValue("ID_publisher").getValueAsString();
			assertEquals("publisher field wrong value","IBM", val);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getClass().getName());
		}
	}
	
	public void testGetExactKeywords() {
		try {
			String fieldNames[] = {"s1", "s2", "s3"};
			FieldType fts[] = {FieldType.T_FIELD_STRING, FieldType.T_FIELD_STRING, FieldType.T_FIELD_STRING};

			RecordTemplate rt = Utilities.makeRecordTemplate(fieldNames, fts);
			Record rec = rt.makeRecord(true);
			String recData[] = {"The quick brown fox", "jumps over", "the lazy dog"};
			rec.setAllFields(recData);
			RecordKeywords kw = new ExactKeywordList(true);
			rec.getKeywords(new int[] {0,2}, kw);
			assertTrue("Keywords missing", kw.contains(Arrays.asList(new String[] {"The", "quick", "brown", "fox", "the", "lazy", "dog"})));
			assertFalse("Keywords wrongly includes", kw.contains(Arrays.asList(new String[] {"Quick", "jumps", "over"})));
			kw = new ExactKeywordList(false);
			rec.getKeywords(new int[] {0, 1}, kw);
			assertTrue("Keywords missing", kw.contains(Arrays.asList(new String[] {"the", "Quick", "brown", "fox", "jumps", "over"})));
			assertFalse("Keywords wrongly includes", kw.contains(Arrays.asList(new String[] {"lazy", "dog"})));
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e);
		}
	}
	
	public void testGetPrefixKeywords() {
		try {
			String fieldNames[] = {"s1", "s2", "s3"};
			FieldType fts[] = {FieldType.T_FIELD_STRING, FieldType.T_FIELD_STRING, FieldType.T_FIELD_STRING};

			RecordTemplate rt = Utilities.makeRecordTemplate(fieldNames, fts);
			Record rec = rt.makeRecord(true);
			String recData[] = {"The quick brown fox", "jumps over", "the lazy dog"};
			rec.setAllFields(recData);
			RecordKeywords kw = new PrefixKeywords(true);
			rec.getKeywords(new int[] {0,2}, kw);
			assertTrue("Keywords missing", kw.contains(Arrays.asList(new String[] {"The", "quic", "bro", "fox", "the", "lazy", "d"})));
			assertFalse("Keywords wrongly includes", kw.contains(Arrays.asList(new String[] {"Quick", "jumps", "overly"})));
			kw = new PrefixKeywords(false);
			rec.getKeywords(new int[] {0, 1}, kw);
			assertTrue("Keywords missing", kw.contains(Arrays.asList(new String[] {"the", "Qu", "brown", "fox", "jumps", "Over"})));
			assertFalse("Keywords wrongly includes", kw.contains(Arrays.asList(new String[] {"lazy", "dog"})));
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e);
		}
	}
}
