package org.lasalledebain.libris.indexes;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

import junit.framework.TestCase;

import org.lasalledebain.Utilities;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.records.Records;
import org.lasalledebain.libris.ui.LibrisUi;

public class LibrisRecordMapTest extends TestCase {
	private File workDir;
	private String recordNames[];

	public void testBuildIndex() {
		try {
			File testDatabaseFile = Utilities.getTestDatabase(Utilities.TEST_DB1_XML_FILE);
			LibrisDatabase db = Libris.buildAndOpenDatabase(testDatabaseFile);
			final LibrisUi myUi = db.getUi();
			
			MockLibrisRecordMap recPositions = new MockLibrisRecordMap();
			db.close();
			db = myUi.openDatabase();
			Records recs = db.getDatabaseRecords();	
			int numRecs = 0;
			for (Record r: recs) {
				++numRecs;
			}
			assertEquals("number of records", 4, numRecs);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		} finally {
		}
	}
	
	public void testDatabaseGetNamedRecords() {
		try {
			File testDatabaseFile = Utilities.copyTestDatabaseFile(workDir, Utilities.DATABASE_WITH_GROUPS_XML);
			LibrisDatabase db = Libris.buildAndOpenDatabase(testDatabaseFile);
			HashSet<String> nameSet = new HashSet<String>(Arrays.asList(recordNames));
			for (Record r: db.getNamedRecords()) {
				String recName = r.getName();
				assertTrue("Unexpected record name "+recName, nameSet.contains(recName));
				nameSet.remove(recName);
			}
			if (!nameSet.isEmpty()) {
				String r = nameSet.iterator().next();
				fail("Record "+r+" missing");
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testDatabaseGetRecordByName() {
		try {
			File testDatabaseFile = Utilities.copyTestDatabaseFile(workDir, Utilities.DATABASE_WITH_GROUPS_XML);
			LibrisDatabase db = Libris.buildAndOpenDatabase(testDatabaseFile);
			
			int expectedId = 1;
			for (String recName: recordNames) {
				Record r = db.getRecord(recName);
				assertNotNull("record "+recName+" not found", r);
				int actualId = r.getRecordId();
				assertEquals("record "+recName+" wrong ID", expectedId, actualId);
				++expectedId;
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	@Override
	protected void setUp() throws Exception {
		workDir = Utilities.getTempTestDirectory();
		recordNames = new String[] {"g_pub1",
				"g_pub1_v1",
				"g_fred_foobar",
				"a_record",
				"b_record",
				"ab_record",
		"bb_record"};
	}

	@Override
	protected void tearDown() throws Exception {
		Utilities.deleteRecursively(workDir);
	
	}
}
