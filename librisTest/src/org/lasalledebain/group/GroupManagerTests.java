package org.lasalledebain.group;

import java.io.File;

import junit.framework.TestCase;

import org.junit.Test;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.indexes.GroupManager;

public class GroupManagerTests extends TestCase {

	private LibrisDatabase db;

	@Override
	protected void setUp() throws Exception {
		File testDatabaseFileCopy = Utilities.copyTestDatabaseFile();
		db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
	}

	@Test
	public void testGetRecord() {
		GroupManager mgr = db.getGroupMgr();
		try {
			Record dbRec = db.getRecord(3);
			Record gmRec = mgr.getRecord(3);
			assertEquals(dbRec, gmRec);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

}
