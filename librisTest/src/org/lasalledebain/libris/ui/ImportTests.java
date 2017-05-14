package org.lasalledebain.libris.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import org.lasalledebain.Utilities;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.LibrisException;

public class ImportTests extends TestCase {
	private File testDatabaseFileCopy;
	private LibrisDatabase db;
	boolean ignoreUnimplemented = Boolean.getBoolean("org.lasalledebain.libris.test.IgnoreUnimplementedTests");
	
	public void setUp() {
		testDatabaseFileCopy = null;
		try {
			testDatabaseFileCopy = Utilities.copyTestDatabaseFile();
		} catch (IOException e1) {
			e1.printStackTrace();
			fail("test database missing");
		}
	
		try {
			db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e);
		}
	}
	public void testImportBasic() {
		File importData = Utilities.getTestDatabase(Utilities.TEST_DELIM_TEXT_FILE_WITH_FIELD_IDS);
		try {
			Record[] recList = LibrisDatabase.importDelimitedTextFile(db, importData, null, ',');
			db.save();
			for (Record rec: recList) {
				System.out.print(rec.toString());
			}
			db.close();
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		} catch (LibrisException e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}
	public void testMalformedTextFile() {
		if (!ignoreUnimplemented) {
			fail("not implememented");
		}
	}
	public void testTabDelimitedTextFile() {
		if (!ignoreUnimplemented) {
			fail("not implememented");
		}
	}
	public void testCommaTextFile() {
		if (!ignoreUnimplemented) {
			fail("not implememented");
		}
	}
	public void testAssignFields() {
		if (!ignoreUnimplemented) {
			fail("not implememented");
		}
	}
	public void testUnassignedFields() {
		if (!ignoreUnimplemented) {
			fail("not implememented");
		}
	}
	public void testEmptyFile() {
		if (!ignoreUnimplemented) {
			fail("not implememented");
		}
	}
	public void testFileWithOnlyFieldIds() {
		if (!ignoreUnimplemented) {
			fail("not implememented");
		}
	}
	public void testBogusFieldIds() {
		if (!ignoreUnimplemented) {
			fail("not implememented");
		}
	}
	public void testExtraData() {
		if (!ignoreUnimplemented) {
			fail("not implememented");
		}
	}
}
