package org.lasalledebain;


import static org.lasalledebain.Utilities.getRecordIdString;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Before;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.ModifiedRecordList;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.LibrisUi;

public class RecordListTests extends TestCase {

	private static final String EXTRA_DATA = "extra data";
	private static final String ID_AUTH = "ID_auth";
	private static final String[] expectedIds = {"1", "2", "3", "4"};

	@Before
	public void setUp() throws Exception {
	}

	@Override
	protected void tearDown() throws Exception {
		Utilities.deleteTestDatabaseFiles();
	}

	public void testDatabaseRecordList () {
		try {
			LibrisDatabase database = Libris.buildAndOpenDatabase(Utilities.getTestDatabase(Utilities.TEST_DB1_XML_FILE));
			RecordList list = database.getRecords();
			int recordCount = 0;
			for (Record r: list) {
				String id = getRecordIdString(r);
			}
			for (Record r: list) {
				String id = getRecordIdString(r);
				assertEquals("wrong record ID", expectedIds[recordCount], id);
				recordCount++;
			}
			assertEquals("wrong number of records", expectedIds.length, recordCount);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e);
		}
	}

	public void testModifiedList () {
		try {
			LibrisDatabase database = Libris.buildAndOpenDatabase(Utilities.getTestDatabase(Utilities.TEST_DB1_XML_FILE));
			RecordList list = database.getRecords();
			ModifiedRecordList modList = new ModifiedRecordList();
			ArrayList<Record> foundRecords = new ArrayList<Record>();

			for (Record r: list) {
				foundRecords.add(r);
				modList.addRecord(r);
			}
			for (Record r: foundRecords) {
				Record rr = modList.getRecord(r.getRecordId());
				assertTrue("record not retrieved using record's own RecordId", r == rr);
			}
			for (Record r: foundRecords) {
				int oldId = r.getRecordId();
				int newId = oldId;
				assertEquals("RecordId.equals fails", newId, oldId);
				Record rr = modList.getRecord(newId);
				assertTrue("record not retrieved using new RecordId", r == rr);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e);
		}
	}
	
	public void testAddNewRecord() {

		File testDatabaseFileCopy;
		try {
			testDatabaseFileCopy = Utilities.copyTestDatabaseFile();
			LibrisDatabase db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
			final LibrisUi myUi = db.getUi();
			Record rec = db.newRecord();
			rec.addFieldValue(ID_AUTH, "new record");
			int id = db.put(rec);
			db.save();
			db = myUi.openDatabase();
			Record newRec = db.getRecord(id);
			assertEquals("New record does not match original", rec, newRec);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}
	
	public void testMultipleSaves() {

		File testDatabaseFileCopy;
		try {
			testDatabaseFileCopy = Utilities.copyTestDatabaseFile();
			LibrisDatabase db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
			final LibrisUi myUi = db.getUi();
			Record rec = db.newRecord();
			rec.addFieldValue(ID_AUTH, "new record");
			db.put(rec);
			db.save();
			db.save();
			db.save();
			db.close();
			db = myUi.openDatabase();
			RecordList list = db.getRecords();
			int recordCount = 0;
			for (@SuppressWarnings("unused") Record r: list) {
				recordCount++;
			}
			assertEquals("wrong number of records", expectedIds.length+1, recordCount);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	
	}
	
	public void testModifyRecord() {

		String shortValue = "x";
		String longvalue = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
				"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
				"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX" +
				"XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
		File testDatabaseFileCopy;
		try {
			testDatabaseFileCopy = Utilities.copyTestDatabaseFile();
			LibrisDatabase db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
			final LibrisUi myUi = db.getUi();
			for (int r=1; r <= expectedIds.length; ++r) {
				int oldId = r;
				db = myUi.openDatabase();
				Record rec = db.getRecord(oldId);
				rec.addFieldValue("ID_title", ((r % 2) == 0)? shortValue: longvalue);
				int newId = db.put(rec);
				assertEquals("new ID != oldId", newId, oldId);
				db.save();
				db.close();
				db = myUi.openDatabase();
				Record newRec = db.getRecord(newId);
				assertEquals("New record does not match original", rec, newRec);
				db.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}
	
	public void testRecordOrder() {
		String dbName = Utilities.TEST_DATABASE_UNORDERED_XML;
		File testDatabaseFileCopy;
		String expectedData[] = {null, "record1", "record2", "record3", "record4"};
		try {
			testDatabaseFileCopy = Utilities.copyTestDatabaseFile(dbName);
			LibrisDatabase db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);

			checkRecordOrder(db, expectedData);

			Record r = db.getRecord(3);
			Field f = r.getField(ID_AUTH);
			f.addValue(EXTRA_DATA);
			expectedData[3] += ", "+EXTRA_DATA;
			db.put(r);
			db.save();
			db.close();
			
			HeadlessUi ui = new HeadlessUi(testDatabaseFileCopy);
			db = new LibrisDatabase(testDatabaseFileCopy, null, ui, false);
			db.open();
			checkRecordOrder(db, expectedData);
			
			db.exportDatabaseXml(new FileOutputStream(testDatabaseFileCopy), true, true);
			db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
			checkRecordOrder(db, expectedData);
			/*
			 * load database with unordered records
			 * read records and check that they are in order
			 * edit a record to make it larger and save it
			 * read & check records
			 * save, close, and reopen database
			 * check order
			 * export database and rebuild
			 * check records
			 */
		} catch (Exception e) {
			fail("unexpected exception "+e.getMessage());
			e.printStackTrace();
		}			
	}

	private void checkRecordOrder(LibrisDatabase db, String[] expectedData)
			throws LibrisException, InputException {
		int expectedId = 1;
		for (Record r: db.getDatabaseRecords()) {
			assertEquals("Wrong record ID", expectedId, r.getRecordId());
			assertEquals("wrong data", expectedData[expectedId], r.getField(ID_AUTH).getValuesAsString());
			++expectedId;
		}
	}
}
