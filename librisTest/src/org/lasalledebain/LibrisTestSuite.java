package org.lasalledebain;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.lasalledebain.group.GroupManagerTests;
import org.lasalledebain.group.GroupDefsTests;
import org.lasalledebain.group.MemberTest;
import org.lasalledebain.hashtable.HashBucketTests;
import org.lasalledebain.hashtable.VariableSizeEntryHashBucketTest;
import org.lasalledebain.libris.indexes.FileSpaceManagerTests;
import org.lasalledebain.libris.indexes.LibrisRecordMapTest;
import org.lasalledebain.libris.indexes.SortedKeyValueFileManagerTest;
import org.lasalledebain.libris.indexes.TestFileRecordMap;
import org.lasalledebain.libris.indexes.TestKeyIntegerTuple;
import org.lasalledebain.libris.ui.ImportTests;
import org.lasalledebain.libris.ui.RecordEditTests;
import org.lasalledebain.recordimport.CsvImportTest;

public class LibrisTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.lasalledebain");
		//$JUnit-BEGIN$
		suite.addTestSuite(FieldTest.class);
		suite.addTestSuite(RecordTests.class);
		suite.addTestSuite(EnumTests.class);
		suite.addTestSuite(RecordListTests.class);
		suite.addTestSuite(GuiTests.class);
		suite.addTestSuite(RecordEditTests.class);
		suite.addTestSuite(SchemaTests.class);
		suite.addTestSuite(FileSpaceManagerTests.class);
		suite.addTestSuite(LibrisRecordMapTest.class);
		suite.addTestSuite(HashBucketTests.class);
		suite.addTestSuite(VariableSizeEntryHashBucketTest.class);
		suite.addTestSuite(SortedKeyValueFileManagerTest.class);
		suite.addTestSuite(DatabaseTests.class);
		suite.addTestSuite(RecordListTests.class);
		suite.addTestSuite(JournalTest.class);
		suite.addTestSuite(ImportTests.class);
		suite.addTestSuite(CsvImportTest.class);
		suite.addTestSuite(GroupManagerTests.class);
		suite.addTestSuite(GroupDefsTests.class);
		suite.addTestSuite(MemberTest.class);
		suite.addTestSuite(TestKeyIntegerTuple.class);
		suite.addTestSuite(TestFileRecordMap.class);
		//$JUnit-END$
		return suite;
	}

}
