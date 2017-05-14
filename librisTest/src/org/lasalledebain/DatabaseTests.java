package org.lasalledebain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import junit.framework.TestCase;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.LibrisMetadata;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordTemplate;
import org.lasalledebain.libris.ui.LibrisUi;
import org.lasalledebain.libris.xmlUtils.ElementManager;


public class DatabaseTests extends TestCase {

	private static final String ID_AUTH = "ID_auth";
	private static final String ID_publisher = "ID_publisher";
	private final static String[] authors = {"", "John le Carre", "Homer", "Louise Creighton"};
	private File testDatabaseFileCopy;;
// TODO override auxiliary directory
	public void testReadRecordsFromSingleFile() {
		try {
			File testDatabaseFile = Utilities.copyTestDatabaseFile(Utilities.TEST_DB1_XML_FILE);
			LibrisDatabase db =  Libris.buildAndOpenDatabase(testDatabaseFile);
			RecordTemplate rt = RecordTemplate.templateFactory(db.getSchema(), null);
			Vector<Record> recordList = new Vector<Record>();
			ElementManager librisMgr = Utilities.makeElementManagerFromFile(testDatabaseFile, "libris");
			librisMgr.parseOpenTag();
			ElementManager metadataMgr = librisMgr.nextElement();
			metadataMgr.flushElement();
			ElementManager recordsMgr = librisMgr.nextElement();
			recordsMgr.parseOpenTag();
			while (recordsMgr.hasNext()) {
				ElementManager recordMgr = recordsMgr.nextElement();
				Record rec = rt.makeRecord(true);
				rec.fromXml(recordMgr);
				recordList.add(rec);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testGetRecords() {

		final int NUM_RECORDS = 3;
		try {
			File testDatabaseFileCopy = Utilities.copyTestDatabaseFile();
			LibrisDatabase db = buildTestDatabase(testDatabaseFileCopy);
			
			for (int i = 1; i <= NUM_RECORDS; ++i) {
				Record rec = db.getRecord(i);
				assertNotNull("Cannot locate "+i, rec);
				String f = rec.getField(ID_AUTH).getValuesAsString();
				assertEquals("Authors field does not match", authors[i], f);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception "+e);
		}
	}

	private LibrisDatabase buildTestDatabase(File testDatabaseFileCopy) throws IOException {			
		LibrisDatabase db = null;
		try {
			db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
		} catch (Throwable e) {
			e.printStackTrace();
			fail("Cannot open database");
		}
		return db;
	}

	public void testEnterRecord() {

		final int NUM_RECORDS = 3;
		try {
			File testDatabaseFileCopy = Utilities.copyTestDatabaseFile();
			LibrisDatabase db = buildTestDatabase(testDatabaseFileCopy);
			
			for (int i = 1; i <= NUM_RECORDS; ++i) {
				Record rec = db.getRecord(i);
				Field f = rec.getField(ID_AUTH);
				f.changeValue("new value "+i);
				System.out.println(rec.toString());
				db.put(rec);
			}
			for (int i = 1; i <= NUM_RECORDS; ++i) {
				Record rec = db.getRecord(i);
				String f = rec.getField(ID_AUTH).getValuesAsString();
				assertEquals("Authors field does not match", "new value "+i, f);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void testEnterRecordWithGroupds() {

		final int NUM_RECORDS = 6;
		try {
			File testDatabaseFileCopy = Utilities.getTestDatabase(Utilities.DATABASE_WITH_GROUPS_AND_RECORDS_XML);
			LibrisDatabase db = buildTestDatabase(testDatabaseFileCopy);
			
			for (int i = 1; i <= NUM_RECORDS; ++i) {
				Record rec = db.getRecord(i);
				rec.setName("Name_"+i);
				rec.addFieldValue(ID_AUTH, "new value "+i);
				System.out.println(rec.toString());
				db.put(rec);
			}
			for (int i = 1; i <= NUM_RECORDS; ++i) {
				Record rec = db.getRecord(i);
				String f = rec.getField(ID_AUTH).getValuesAsString();
				assertEquals("Authors field does not match", "new value "+i, f);
				assertEquals("Wrong name", "Name_"+i, rec.getName());
			}
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	public void testOpenAndImmediatelyCloseDatabase() {
		try {
			File testDatabaseFileCopy = Utilities.copyTestDatabaseFile();
			LibrisDatabase db = buildTestDatabase(testDatabaseFileCopy);
			LibrisUi testUi = db.getUi();
			db.close();
			db = testUi.openDatabase();
			db.close();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}

	}

	public void testOpenIndexAndImmediatelyCloseDatabase() {
		try {
			LibrisDatabase db = buildTestDatabase(testDatabaseFileCopy);
			db.close();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}

	}

	public void testMultipleRebuild() {
		try {			
				File testDatabaseFileCopy = Utilities.copyTestDatabaseFile();			
				LibrisDatabase db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
				LibrisMetadata meta = db.getMetadata();
				int numRecs = meta.getSavedRecords();
				assertEquals("Wrong number of records initial build", 4, numRecs);
				db.close();
				db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
				LibrisUi ui = db.getUi();
				db.close();
			
			LibrisDatabase db2 = ui.openDatabase();
			assertEquals("Wrong number of records after rebuild", 4, db2.getMetadata().getSavedRecords());
			for (int i = 1; i < authors.length; ++i) {
				Record rec = db2.getRecord(i);
				assertNotNull("Cannot locate "+i, rec);
				String f = rec.getField(ID_AUTH).getValuesAsString();
				assertEquals("Authors field does not match", authors[i], f);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}

	}

	public void testEnumOutOfRange() {
		try {
			LibrisDatabase db = buildTestDatabase(testDatabaseFileCopy);
			File dbFile = db.getDatabaseFile();
			LibrisUi testUi = db.getUi();
			String expected = null;
			{
				Record rec = db.getRecord(1);
				Field f = rec.getField(ID_publisher);
				f.addValuePair("", "test data");
				expected = f.getValuesAsString();
				db.put(rec);
				db.save();
				db.close();
			}
			LibrisDatabase db2 = testUi.openDatabase();
			Record rec = db2.getRecord(1);
			Field f = rec.getField(ID_publisher);
			String actual = f.getValuesAsString();
			assertEquals("Out of range enum wrong", expected, actual);
			db.close();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}

	}
	public void testXmlExportAll() {
		try {
			LibrisDatabase db = buildTestDatabase(testDatabaseFileCopy);
			File workdir = Utilities.getTempTestDirectory();
			File copyDbXml = new File (workdir, "database_copy.xml");
			copyDbXml.deleteOnExit();
			FileOutputStream copyStream = new FileOutputStream(copyDbXml);
			System.out.println(getName()+": copy database to"+copyDbXml);
			db.exportDatabaseXml(copyStream, true, true);
			copyStream.close();
			
			LibrisDatabase dbCopy = Libris.buildAndOpenDatabase(copyDbXml);
			assertNotNull("Error rebuilding database copy", dbCopy);
			assertTrue("database copy does not match original", dbCopy.equals(db));
			dbCopy.close();
			db.close();
			copyDbXml.delete();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testXmlExportAllWithGroups() {
		try {
			File testDatabaseFileCopy = Utilities.getTestDatabase(Utilities.DATABASE_WITH_GROUPS_AND_RECORDS_XML);
			LibrisDatabase db = buildTestDatabase(testDatabaseFileCopy);
			File workdir = Utilities.getTempTestDirectory();
			File copyDbXml = new File (workdir, "database_copy.xml");
			copyDbXml.deleteOnExit();
			FileOutputStream copyStream = new FileOutputStream(copyDbXml);
			System.out.println(getName()+": copy database to"+copyDbXml);
			db.exportDatabaseXml(copyStream, true, true);
			copyStream.close();
			
			LibrisDatabase dbCopy = Libris.buildAndOpenDatabase(copyDbXml);
			assertNotNull("Error rebuilding database copy", dbCopy);
			assertTrue("database copy does not match original", dbCopy.equals(db));
			dbCopy.close();
			db.close();
			copyDbXml.delete();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	@Override
	protected void setUp() throws Exception {
		 testDatabaseFileCopy = Utilities.copyTestDatabaseFile();
	}

	@Override
	protected void tearDown() throws Exception {
		Utilities.deleteTestDatabaseFiles();
		if (null != testDatabaseFileCopy) {
			testDatabaseFileCopy.delete();
		}
	}
}
