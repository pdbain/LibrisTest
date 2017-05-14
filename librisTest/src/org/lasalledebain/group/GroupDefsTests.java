package org.lasalledebain.group;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Test;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.index.GroupDef;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.xmlUtils.ElementWriter;

public class GroupDefsTests extends TestCase {

	private static final String GRP_ONE = "GRP_one";
	private static final String ID_KEYWORDS = "ID_keywords";
	private static final String ID_TITLE = "ID_title";
	private static final String ID_ISSUE = "ID_issue";
	private static final String ID_VOLUME = "ID_volume";
	private static final String ID_PUBLISHER = "ID_publisher";
	private static final String ID_AUTH = "ID_auth";

	@Test
	public void testFromXml() {
		File testDir = Utilities.getTestDataDirectory();
		File inputFile = new File(testDir, Utilities.SCHEMA_WITH_GROUP_DEFS_XML);
		try {
			Schema schema = Utilities.loadSchema(inputFile);
			checkGroups(schema);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception "+e);
		}
	}

	@Test
	public void testToXml() {
		File testDir = Utilities.getTestDataDirectory();
		File inputFile = new File(testDir, Utilities.SCHEMA_WITH_GROUP_DEFS_XML);
		try {
			Schema schema = Utilities.loadSchema(inputFile);
			File workdir = Utilities.getTempTestDirectory();
			File exportedXml = new File (workdir, "schema_copy.xml");
			exportedXml.deleteOnExit();
			FileOutputStream copyStream = new FileOutputStream(exportedXml);
			ElementWriter outWriter = ElementWriter.eventWriterFactory(copyStream);
			schema.toXml(outWriter);
			outWriter.flush();
			copyStream.close();
			schema = Utilities.loadSchema(exportedXml);
			checkGroups(schema);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception "+e);
		}
	}

	@Test
	public void testGetGroupDefs() {
		File testDir = Utilities.getTestDataDirectory();
		File inputFile = new File(testDir, Utilities.SCHEMA_WITH_GROUP_DEFS_XML);
		try {
			Schema schema = Utilities.loadSchema(inputFile);
			for (String gid: schema.getGroupIds()) {
				GroupDef g = schema.getGroupDef(gid);
				assertEquals(gid, g.getFieldId());
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception "+e);
		}
	}

	@Test
	/* 
	 * record 1: defines publisher
	 * record 2: extends R1, defines volume
	 * record 3: extends R2, defines issue
	 * record 4: extends R2, defines issue
	 * record 5: defines pub, voll, iss
	 * record 6: extends R3, defines all but pub, vol, iss
	 * record 7: extends R5, defines all but pub, vol, iss
	 * record 8: extends R4, defines all but pub
	 * record 9: extends R2 and R3, defines all but pub, vol, iss
	 */
	public void testFieldInheritance() {
		try {
			File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(Utilities.TEST_DB_WITH_GROUPS_XML_FILE);
			LibrisDatabase db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
			System.out.println("database rebuilt");
			ArrayList<Record> recList = new ArrayList<Record>();
			{
				Record curr = db.newRecord();
				Record rec1 = curr =db.newRecord();
				curr.addFieldValuePair(ID_PUBLISHER, "", "Publisher 1");
				saveRecord(db, recList, curr);

				curr = db.newRecord();
				Record rec2 = curr;
				curr.setParent(GRP_ONE, rec1);
				curr.addFieldValue(ID_VOLUME, "Volume 1");
				saveRecord(db, recList, curr);

				Record rec3 = curr = db.newRecord();
				curr.setParent(GRP_ONE, rec2);
				curr.addFieldValue(ID_ISSUE, "Issue 1");
				saveRecord(db, recList, curr);

				Record rec4 = curr = db.newRecord();
				curr.setParent(GRP_ONE, rec2);
				curr.addFieldValue(ID_ISSUE, "Issue 2");
				saveRecord(db, recList, curr);

				Record rec5 = curr = db.newRecord();
				curr.setParent(GRP_ONE, rec2);
				curr.addFieldValuePair(ID_PUBLISHER, "", "Publisher 2");
				curr.addFieldValue(ID_VOLUME, "Volume 2");
				curr.addFieldValue(ID_ISSUE, "Issue 3");
				saveRecord(db, recList, curr);

				curr = db.newRecord();
				curr.setParent(GRP_ONE, rec3);
				curr.addFieldValue(ID_AUTH, "Author 1");
				curr.addFieldValue(ID_TITLE, "Title 1");
				curr.addFieldValue(ID_KEYWORDS, "The quick brown fox");
				saveRecord(db, recList, curr);

				curr = db.newRecord();
				curr.setParent(GRP_ONE, rec5);
				curr.addFieldValue(ID_AUTH, "Author 2");
				curr.addFieldValue(ID_TITLE, "Title 2");
				curr.addFieldValue(ID_KEYWORDS, "Now is the time");
				saveRecord(db, recList, curr);

				curr = db.newRecord();
				curr.setParent(GRP_ONE, rec4);
				curr.addFieldValue(ID_AUTH, "Author 3");
				curr.addFieldValue(ID_TITLE, "Title 3");
				curr.addFieldValue(ID_KEYWORDS, "Now is the time");
				curr.addFieldValue(ID_VOLUME, "Volume 3");
				curr.addFieldValue(ID_ISSUE, "Issue 4");
				saveRecord(db, recList, curr);

				curr = db.newRecord();
				curr.setParent(GRP_ONE, rec2);
				curr.setParent(GRP_ONE, rec3);
				curr.addFieldValue(ID_AUTH, "Author 4");
				curr.addFieldValue(ID_TITLE, "Title 4");
				curr.addFieldValue(ID_KEYWORDS, "Lorem ipsum");
				saveRecord(db, recList, curr);
			}
			String[] fieldIds = db.getSchema().getFieldIds();
			for (Record r: recList) {
				System.out.println("Record "+r.getRecordId());
				int parentId = r.getParent(GRP_ONE);
				Record parent = null;
				if (!RecordId.isNull(parentId)) {
					parent = db.getRecord(parentId);
				}
				for (String fid: fieldIds) {
					FieldValue val = r.getFieldValue(fid);
					if (null != val) {
						System.out.println(fid+": "+val.getValueAsString());
					}
					if ((r.getField(fid) == null) && (null != parent)) {
						Field parentValue = parent.getField(fid);
						if (null != parentValue) {
							assertEquals("Wrong value for field "+fid, parentValue.getValuesAsString(), val.getValueAsString());
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception "+e);
		}
	}

	private void saveRecord(LibrisDatabase db, ArrayList<Record> recList,
			Record curr) throws LibrisException {
		db.put(curr);
		recList.add(curr);
	}

	private void checkGroups(Schema schema) {
		GroupDef g = schema.getGroupDef(GRP_ONE);
		assertNotNull(g);
		assertEquals(g.getFieldTitle(), "First group");
		assertEquals(g.getStructureType(), GroupDef.GroupStructure.STRUCTURE_HIERARCHICAL);
		g = schema.getGroupDef("GRP_two");
		assertNotNull(g);
		assertEquals(g.getFieldTitle(), "Second group");
		assertEquals(g.getStructureType(), GroupDef.GroupStructure.STRUCTURE_FLAT);
	}
	// TODO test with DatabaseWithGroups
}
