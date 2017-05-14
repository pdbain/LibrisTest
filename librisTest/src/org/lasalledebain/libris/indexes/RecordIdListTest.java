package org.lasalledebain.libris.indexes;


import static org.lasalledebain.Utilities.DATABASE_WITH_GROUPS_XML;

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.After;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;

public class RecordIdListTest extends TestCase {
	File testDatabaseFileCopy;
	LibrisDatabase db;

	public void testNameList() {
		SortedKeyValueFileManager<KeyIntegerTuple> nameList = db.getNamedRecordIndex();
		assertNotNull("nameList null", nameList);
		ArrayList<KeyIntegerTuple> actualNames = new ArrayList<KeyIntegerTuple>();
		for(KeyIntegerTuple recName: nameList) {
			actualNames.add(recName);
		}
	}
	
	public void setUp() {
		try {
			testDatabaseFileCopy = Utilities.copyTestDatabaseFile(DATABASE_WITH_GROUPS_XML);
			db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
			System.out.println("database rebuilt");
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}
	@After
	public void tearDown() throws Exception {
		Utilities.deleteTestDatabaseFiles(DATABASE_WITH_GROUPS_XML);
	}

}
