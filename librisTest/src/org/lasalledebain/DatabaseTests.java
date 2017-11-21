package org.lasalledebain;

import static org.lasalledebain.Utilities.DATABASE_WITH_GROUPS_XML;
import static org.lasalledebain.Utilities.checkRecords;
import static org.lasalledebain.Utilities.testLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;

import org.lasalledebain.libris.DatabaseInstance;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.LibrisMetadata;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordTemplate;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.ui.LibrisUi;
import org.lasalledebain.libris.xmlUtils.ElementManager;

import junit.framework.TestCase;

public class DatabaseTests extends TestCase {

	private static final String ID_AUTH = "ID_auth";
	private static final String ID_publisher = "ID_publisher";
	private final static String[] authors = {"", "John le Carre", "Homer", "Louise Creighton"};
	private LibrisDatabase db;
	private LibrisDatabase db2;
	// TODO override auxiliary directory
	public void testReadRecordsFromSingleFile() {
		try {
			File testDatabaseFile = Utilities.copyTestDatabaseFile(Utilities.TEST_DB1_XML_FILE);
			db =  Libris.buildAndOpenDatabase(testDatabaseFile);
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
			db = buildTestDatabase(testDatabaseFileCopy);

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

	public void testEnterRecord() {

		final int NUM_RECORDS = 3;
		try {
			File testDatabaseFileCopy = Utilities.copyTestDatabaseFile();
			db = buildTestDatabase(testDatabaseFileCopy);

			for (int i = 1; i <= NUM_RECORDS; ++i) {
				Record rec = db.getRecord(i);
				Field f = rec.getField(ID_AUTH);
				f.changeValue("new value "+i);
				testLogger.log(Level.INFO,rec.toString());
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
			File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(Utilities.DATABASE_WITH_GROUPS_AND_RECORDS_XML);
			db = buildTestDatabase(testDatabaseFileCopy);

			for (int i = 1; i <= NUM_RECORDS; ++i) {
				Record rec = db.getRecord(i);
				rec.setName("Name_"+i);
				rec.addFieldValue(ID_AUTH, "new value "+i);
				testLogger.log(Level.INFO,rec.toString());
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
			db = buildTestDatabase(testDatabaseFileCopy);
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
			db = buildTestDatabase(Utilities.copyTestDatabaseFile());
			db.close();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}

	}

	public void testMultipleRebuild() {
		try {			
			File testDatabaseFileCopy = Utilities.copyTestDatabaseFile();			
			db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
			LibrisMetadata meta = db.getMetadata();
			int numRecs = meta.getSavedRecords();
			assertEquals("Wrong number of records initial build", 4, numRecs);
			db.close();
			db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
			LibrisUi ui = db.getUi();
			db.close();

			db2 = ui.openDatabase();
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
			db = buildTestDatabase(Utilities.copyTestDatabaseFile());
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
			db2 = testUi.openDatabase();
			Record rec = db2.getRecord(1);
			Field f = rec.getField(ID_publisher);
			String actual = f.getValuesAsString();
			assertEquals("Out of range enum wrong", expected, actual);
			db2.close();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}

	}
	public void testXmlExportAll() {
		try {
			db = buildTestDatabase(Utilities.copyTestDatabaseFile());
			File workdir = Utilities.getTempTestDirectory();
			File copyDbXml = new File (workdir, "database_copy.xml");
			copyDbXml.deleteOnExit();
			FileOutputStream copyStream = new FileOutputStream(copyDbXml);
			testLogger.log(Level.INFO,getName()+": copy database to"+copyDbXml);
			db.exportDatabaseXml(copyStream, true, true, false);
			copyStream.close();

			db2 = Libris.buildAndOpenDatabase(copyDbXml);
			assertNotNull("Error rebuilding database copy", db2);
			assertTrue("database copy does not match original", db2.equals(db));
			copyDbXml.delete();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testXmlExportAllWithGroups() {
		try {
			File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(Utilities.DATABASE_WITH_GROUPS_AND_RECORDS_XML);
			LibrisDatabase db = buildTestDatabase(testDatabaseFileCopy);
			File workdir = Utilities.getTempTestDirectory();
			File copyDbXml = new File (workdir, "database_copy.xml");
			copyDbXml.deleteOnExit();
			FileOutputStream copyStream = new FileOutputStream(copyDbXml);
			testLogger.log(Level.INFO,getName()+": copy database to"+copyDbXml);
			db.exportDatabaseXml(copyStream, true, true, false);
			copyStream.close();

			LibrisDatabase dbCopy = Libris.buildAndOpenDatabase(copyDbXml);
			assertNotNull("Error rebuilding database copy", dbCopy);
			assertTrue("database copy does not match original", dbCopy.equals(db));
			db.close();
			dbCopy.close();
			Utilities.deleteRecursively(testDatabaseFileCopy);
			Utilities.deleteRecursively(copyDbXml);
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testAddRecordsToDatabase() {
		final int numRecs = 1024;
		String dbFile = DATABASE_WITH_GROUPS_XML;
		try {
			File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(dbFile);
			db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
			testLogger.log(Level.INFO, "database rebuilt");
			int lastId = db.getLastRecordId();
			for (int i = lastId+1; i <= numRecs; ++i) {
				Record rec = db.newRecord();
				int recNum = db.put(rec);
				assertEquals("wrong ID for new record",  i, recNum);
			}
			db.save();
			checkRecords(db, lastId);
		} catch (LibrisException | IOException e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}

	public void testFork() {
		try {
			File workdir = Utilities.getTempTestDirectory();
			File dbInstance = new File(workdir, "database_instance.xml");
			{
				db = buildTestDatabase(Utilities.copyTestDatabaseFile());
				File copyDbXml = new File (workdir, "database_copy.xml");
				testLogger.log(Level.INFO,getName()+": copy database to "+copyDbXml);
				copyDbXml.deleteOnExit();
				dbInstance.deleteOnExit();
				FileOutputStream instanceStream = new FileOutputStream(dbInstance);
				testLogger.log(Level.INFO,getName()+": copy database to "+dbInstance);
				db.exportDatabaseXml(instanceStream, true, true, true);
				instanceStream.close();
			}
			int lastId = db.getLastRecordId();
			db2 = Libris.buildAndOpenDatabase(dbInstance);
			assertNotNull("Error rebuilding database copy", db2);
			assertTrue("database copy does not match original", db2.equals(db));
			
			db.close();
			DatabaseInstance inst = db2.getMetadata().getInstanceInfo();
			assertNotNull("Database instance information missing", inst);
			assertEquals("Wrong base ID", lastId, inst.getRecordIdBase());
			final int numRecs = 10;
			for (int i = lastId+1; i <= numRecs; ++i) {
				Record rec = db2.newRecord();
				int recNum = db2.put(rec);
				assertEquals("wrong ID for new record",  i, recNum);
			}
			db2.save();
			checkRecords(db2, lastId);

			for (int i = 1; i <= numRecs; ++i) {
				try {
					Record r = db2.getRecord(i);
					assertNotNull("Cannot find record "+i, r);
					assertEquals("Wrong record ID",  i, r.getRecordId());
					boolean readOnly = db2.isRecordReadOnly(i);
					assertEquals("read-only status of record "+i+" incorrect", i <= lastId, readOnly);
					r.addFieldValue(ID_AUTH, "new value "+i);
					int resultId = db2.put(r);
					if (i <= lastId) {
						assertEquals("record from root database not read-only", LibrisConstants.NULL_RECORD_ID, resultId);
					} else {
						assertEquals("record from fork database wrong ID", i, resultId);
					}
				} catch (InputException e) {
					e.printStackTrace();
					fail("unexpected exception "+e.getMessage());
				}
			}
		
			db.close();
			db2.close();
			dbInstance.delete();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testIncrement() {
		try {
			File workdir = Utilities.getTempTestDirectory();
			File dbInstance = new File(workdir, "database_instance.xml");
			File dbIncrement = new File(workdir, "database_increment.xml");
			{
				db = buildTestDatabase(Utilities.copyTestDatabaseFile());
				File copyDbXml = new File (workdir, "database_copy.xml");
				testLogger.log(Level.INFO,getName()+": copy database to "+copyDbXml);
				copyDbXml.deleteOnExit();
				dbInstance.deleteOnExit();
				FileOutputStream instanceStream = new FileOutputStream(dbInstance);
				testLogger.log(Level.INFO,getName()+": copy database to "+dbInstance);
				db.exportDatabaseXml(instanceStream, true, true, true);
				instanceStream.close();
			}
			int lastId = db.getLastRecordId();
			db2 = Libris.buildAndOpenDatabase(dbInstance);
			assertNotNull("Error rebuilding database copy", db2);
			assertTrue("database copy does not match original", db2.equals(db));
			
			db.close();
			DatabaseInstance inst = db2.getMetadata().getInstanceInfo();
			assertNotNull("Database instance information missing", inst);
			assertEquals("Wrong base ID", lastId, inst.getRecordIdBase());
			final int numRecs = 10;
			for (int i = lastId+1; i <= numRecs; ++i) {
				Record rec = db2.newRecord();
				int recNum = db2.put(rec);
				assertEquals("wrong ID for new record",  i, recNum);
			}
			db2.save();
			db2.exportIncrement(new FileOutputStream(dbIncrement));
		
			db.close();
			db2.close();
			dbInstance.delete();
		} catch (Throwable e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	@Override
	protected void setUp() throws Exception {
		testLogger.log(Level.INFO,"running "+getName());
	}

	@Override
	protected void tearDown() throws Exception {
		if (null != db) {
			db.close(true);
			db = null;
		}
		if (null != db2) {
			db2.close(true);
			db2 = null;
		}
		Utilities.deleteWorkingDirectory();
	}

	private LibrisDatabase buildTestDatabase(File testDatabaseFileCopy) throws IOException {			
		db = null;
		try {
			db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
		} catch (Throwable e) {
			e.printStackTrace();
			fail("Cannot open database");
		}
		return db;
	}
}
