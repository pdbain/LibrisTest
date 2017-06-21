package org.lasalledebain.group;


import static org.lasalledebain.Utilities.DATABASE_WITH_GROUPS_AND_RECORDS_XML;
import static org.lasalledebain.Utilities.DATABASE_WITH_GROUPS_XML;

import java.io.File;
import java.util.Iterator;

import junit.framework.TestCase;

import org.junit.After;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldValue;

public class InheritanceTest extends TestCase {
	File testDatabaseFileCopy;
	LibrisDatabase db;
	String fieldNamesAndValues[][] = {{"",""}, {"ID_publisher", "Publisher1"}, {"ID_volume", "Volume 1"}, {"ID_title", "Title1"}};

	void fetchAndCheckField(Record rec) {
		int recId = rec.getRecordId();
		try {
			for (int i = 1; i <= recId; ++i) {
				FieldValue fv;
				fv = rec.getFieldValue(fieldNamesAndValues[i][0]);
				assertEquals("record "+i, fieldNamesAndValues[i][1], fv.getMainValueAsString());
			}
		} catch (InputException e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}

	public void testSanity() {
		String dbFile = DATABASE_WITH_GROUPS_AND_RECORDS_XML;
		int parents[] = {0, 1, 1, 3, 0, 5}; 

		final String IEEE = "IEEE";
		final String ACM = "ACM";
		String expectedPubs[] = {IEEE, IEEE, IEEE, IEEE, ACM, ACM};
			setupDatabase(dbFile);
		try {
			int index = 0;
			for (Record r: db.getRecordReader()) {
				int actualParent = r.getParent(0);
				int expectedParent = parents[index];
				assertEquals("Wrong parent for "+r.toString(), expectedParent, actualParent);
				String actualPub = r.getFieldValue("ID_publisher").getMainValueAsString();
				assertEquals("Wrong publisher field for "+r.toString(), expectedPubs[index], actualPub);
				++index;
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}

	public void testInheritOneLevel() {
		String dbFile = DATABASE_WITH_GROUPS_XML;
		setupDatabase(dbFile);
		try {
			for (int i = 1; i <= 3; ++i) {
				Record rec = db.getRecord(i);
				fetchAndCheckField(rec);
			}
		} catch (InputException e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}

	private void setupDatabase(String dbFile) {
		try {
			testDatabaseFileCopy = Utilities.copyTestDatabaseFile(dbFile);
			db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
			System.out.println("database rebuilt");
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}
	
	public void testLargeInheritance() {
		 final int numRecs = 1024;
		String dbFile = DATABASE_WITH_GROUPS_XML;
		setupDatabase(dbFile);
		int lastId = db.getLastRecordId();
		int maxParent = 0;
		int minParent = numRecs;
		try {
			for (int i = lastId+1; i <= numRecs; ++i) {
				Record rec = db.newRecord();
				 maxParent = (int) Math.sqrt(i);
				 minParent = Math.min(minParent, maxParent);
				rec.setParent(0, maxParent);
				int recNum = db.put(rec);
				assertEquals("wrong ID for new record",  i, recNum);
			}
			
			db.save();
		} catch (LibrisException e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
		for (int i = minParent; i <= maxParent; i++) {
			RecordList children = db.getChildRecords(i, 0);
			assertNotNull("Record "+i+" has no children", children);
			Iterator<Record> childrenIterator = children.iterator();
			int expectedChild = i*i;
			for (int c = expectedChild; (c < (expectedChild + (2*i) + 1)) && (c < numRecs); ++c) {
				assertTrue("too few children", childrenIterator.hasNext());
				int actualChild = childrenIterator.next().getRecordId();
				assertEquals("Wrong child", c, actualChild);
			}
		}
	}
	@After
	public void tearDown() throws Exception {
		Utilities.deleteTestDatabaseFiles(DATABASE_WITH_GROUPS_XML);
	}

}
