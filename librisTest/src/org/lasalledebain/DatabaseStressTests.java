package org.lasalledebain;

import static org.lasalledebain.Utilities.EMPTY_DATABASE_FILE;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.LibrisException;

import junit.framework.TestCase;

public class DatabaseStressTests extends TestCase {
	static final int NUM_RECORDS = Integer.getInteger("org.lasalledebain.test.numrecords", 4000);
	PrintStream out = System.out;
	private Logger testLogger;

	public void testHugeDatabase() {
		try {
			LibrisDatabase db = buildTestDatabase();
			ArrayList<Record> expectedRecords = new ArrayList<Record>(NUM_RECORDS);
			expectedRecords.add(null);

			testLogger.log(Level.INFO, "add records");
			long startTime = System.currentTimeMillis();
			for (int i = 1; i <= NUM_RECORDS; ++i) {
				Record rec = db.newRecord();
				for (int f = 0; f < 4; ++f) {
					rec.addFieldValue(0, "rec_"+i+"_field_"+f);
				}
				db.put(rec);
				assertEquals("Wrong record ID", i, rec.getRecordId());
				expectedRecords.add(rec);
				if (0 == (i % 64)) {
					db.save();
					out.print('.');
				}
			}
			long endTime = System.currentTimeMillis();
			testLogger.log(Level.INFO, "time to add: "+(endTime-startTime)/1000.0+" seconds\nCheck");
			for (int i = 1; i <= NUM_RECORDS; ++i) {
				Record actual = db.getRecord(i);
				Record expected = expectedRecords.get(i);
				assertEquals("recovered record does not match", expected, actual);
				if (0 == (i % 64)) {
					out.print('.');
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception "+e);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		Utilities.deleteTestDatabaseFiles(EMPTY_DATABASE_FILE);
	}
	private LibrisDatabase buildTestDatabase() throws IOException {
		File testDatabaseFileCopy = Utilities.copyTestDatabaseFile(EMPTY_DATABASE_FILE);			
		LibrisDatabase db = null;
		try {
			db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
		} catch (LibrisException e) {
			e.printStackTrace();
			fail("could not open database:"+e.getMessage());
		}
		testLogger.log(Level.INFO, "database rebuilt");
		return db;
	}

	@Override
	protected void setUp() throws Exception {
		testLogger = Logger.getLogger(Utilities.LIBRIS_TEST_LOGGER);
		testLogger.setLevel(Utilities.defaultLoggingLevel);
	}

}
