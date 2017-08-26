package org.lasalledebain.group;


import static org.lasalledebain.Utilities.DATABASE_WITH_GROUPS_AND_RECORDS_XML;
import static org.lasalledebain.Utilities.DATABASE_WITH_GROUPS_XML;
import static org.lasalledebain.Utilities.testLogger;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;

import org.junit.After;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordList;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.field.FieldValue;

import junit.framework.TestCase;

public class InheritanceTest extends TestCase {
	File testDatabaseFileCopy;
	LibrisDatabase db;
	String fieldNamesAndValues[][] = {{"",""}, {"ID_publisher", "Publisher1"}, {"ID_volume", "Volume 1"}, {"ID_title", "Title1"}};

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

	public void testChildrenSanity() {
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

	public void testAddChild() {
		String dbFile = DATABASE_WITH_GROUPS_XML;
		setupDatabase(dbFile);
		int lastId = db.getLastRecordId();
		int childId = lastId+1;
		final int parentId = 2;
		try {
			Record rec = db.newRecord();
			rec.setParent(0, parentId);
			int recNum = db.put(rec);
			assertEquals("wrong ID for new record",  childId, recNum);
			checkChild(childId, parentId);

			db.save();
			checkChild(childId, parentId);
		} catch (LibrisException e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}

	public void testInheritanceSanity() {
		final int numRecs = 16;
		String dbFile = DATABASE_WITH_GROUPS_XML;
		HashMap<Integer, HashSet<Integer>> expectedChildren = new HashMap<>(numRecs);
		setupDatabase(dbFile);
		int lastId = db.getLastRecordId();
		int maxParent = 0;
		int minParent = numRecs;
		try {
			initializeExpectedChildren(expectedChildren, lastId);
			for (int i = lastId+1; i <= numRecs; ++i) {
				Record rec = db.newRecord();
				maxParent = (int) Math.sqrt(i);
				minParent = Math.min(minParent, maxParent);
				rec.setParent(0, maxParent);
				int recNum = db.put(rec);
				assertEquals("wrong ID for new record",  i, recNum);
				HashSet<Integer> s = expectedChildren.get(maxParent);
				if (null == s) {
					s = new HashSet<>();
					expectedChildren.put(maxParent, s);
				}
				s.add(recNum);
			}
			testLogger.log(Level.INFO, "Check children before save");
			checkChildren(numRecs, expectedChildren);
			db.save();
		} catch (LibrisException e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
		Utilities.checkRecords(db, lastId);
		try {
			testLogger.log(Level.INFO, "Check children after save");
			checkChildren(numRecs, expectedChildren);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception: "+e.getMessage());
		}
	}

	public void testInheritanceStressDebug() {
		final int numRecs = 1024;
		String dbFile = DATABASE_WITH_GROUPS_XML;
		setupDatabase(dbFile);
		int lastId = db.getLastRecordId();
		try {
			for (int i = lastId+1; i <= numRecs; ++i) {
				Record rec = db.newRecord();
				int recNum = db.put(rec);
				assertEquals("wrong ID for new record",  i, recNum);
			}
			db.save();
		} catch (LibrisException e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
		Utilities.checkRecords(db, lastId);
	}

	public void testInheritanceStress() {
		final int numRecs = 1024;
		String dbFile = DATABASE_WITH_GROUPS_XML;
		HashMap<Integer, HashSet<Integer>> expectedChildren = new HashMap<>(numRecs);
		setupDatabase(dbFile);
		int lastId = db.getLastRecordId();
		try {
			initializeExpectedChildren(expectedChildren, lastId);
			addChildren(numRecs, lastId, expectedChildren);
			db.save();
		} catch (LibrisException e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
		Utilities.checkRecords(db, lastId);
		try {
			checkChildren(numRecs, expectedChildren);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception: "+e.getMessage());
		}
	}

	public void testInheritanceReopenSanity() {
		final int numRecs = 16;
		String dbFile = DATABASE_WITH_GROUPS_AND_RECORDS_XML;
		HashMap<Integer, HashSet<Integer>> expectedChildren = new HashMap<>(numRecs);
		setupDatabase(dbFile);
		int lastId = db.getLastRecordId();
		try {
			initializeExpectedChildren(expectedChildren, lastId);
			addChildren(numRecs, lastId, expectedChildren);
			File builtDatabaseFile = db.getDatabaseFile();
			db.save();
			db.close();
			db = Libris.openDatabase(builtDatabaseFile, null);
			lastId = db.getLastRecordId();
			assertEquals("database has wrong number of records",numRecs, lastId);
			checkChildren(numRecs, expectedChildren);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception: "+e.getMessage());
		}
	}

	public void testInheritanceSaveReopen() {
		final int numRecs = 1024;
		String dbFile = DATABASE_WITH_GROUPS_AND_RECORDS_XML;
		HashMap<Integer, HashSet<Integer>> expectedChildren = new HashMap<>(numRecs);
		setupDatabase(dbFile);
		int lastId = db.getLastRecordId();
		int maxParent = 0;
		int minParent = numRecs;
		try {
			initializeExpectedChildren(expectedChildren, lastId);
			for (int i = lastId+1; i <= numRecs; ++i) {
				Record rec = db.newRecord();
				maxParent = (int) Math.sqrt(i);
				minParent = Math.min(minParent, maxParent);
				rec.setParent(0, maxParent);
				int recNum = db.put(rec);
				assertEquals("wrong ID for new record",  i, recNum);
				HashSet<Integer> s = expectedChildren.get(maxParent);
				if (null == s) {
					s = new HashSet<>();
					expectedChildren.put(maxParent, s);
				}
				s.add(recNum);
			}
			File builtDatabaseFile = db.getDatabaseFile();
			db.save();
			db.close();
			db = Libris.openDatabase(builtDatabaseFile, null);
			lastId = db.getLastRecordId();
			assertEquals("database has wrong number of records",numRecs, lastId);
			for (int i = minParent; i <= maxParent; ++i) {
				RecordList children;
				children = db.getChildRecords(i, 0, false);
				HashSet<Integer> childrenSet = expectedChildren.get(i);
				if (null == childrenSet) {
					assertNotNull("Record "+i+" has unexpected children");					
				} else {
					assertNotNull("Record "+i+" has no children", children);
					int childCount = childrenSet.size();
					for (Record r: children) {
						int recordId = r.getRecordId();
						assertTrue("Unexpected child "+recordId+" of record "+i, childrenSet.contains(recordId));
						--childCount;
					}
					assertTrue("Too few children for "+i, 0 == childCount);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception: "+e.getMessage());
		}
	}

	public void testLargeInheritance() {
		final int numRecs = 10000;
		String dbFile = DATABASE_WITH_GROUPS_XML;
		HashMap<Integer, HashSet<Integer>> expectedChildren = new HashMap<>(numRecs);
		setupDatabase(dbFile);
		int lastId = db.getLastRecordId();
		int maxParent = 0;
		int minParent = numRecs;
		try {
			initializeExpectedChildren(expectedChildren, lastId);
			for (int i = lastId+1; i <= numRecs; ++i) {
				Record rec = db.newRecord();
				maxParent = (int) Math.sqrt(i);
				minParent = Math.min(minParent, maxParent);
				rec.setParent(0, maxParent);
				int recNum = db.put(rec);
				assertEquals("wrong ID for new record",  i, recNum);
				HashSet<Integer> s = expectedChildren.get(maxParent);
				if (null == s) {
					s = new HashSet<>();
					expectedChildren.put(maxParent, s);
				}
				s.add(recNum);
			}
			db.save();
		} catch (LibrisException e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
		try {
			for (int i = minParent; i <= maxParent; ++i) {
				RecordList children;
				children = db.getChildRecords(i, 0, false);
				HashSet<Integer> childrenSet = expectedChildren.get(i);
				if (null == childrenSet) {
					assertNotNull("Record "+i+" has unexpected children");					
				} else {
					assertNotNull("Record "+i+" has no children", children);
					int childCount = childrenSet.size();
					for (Record r: children) {
						int recordId = r.getRecordId();
						assertTrue("Unexpected child "+recordId+" of record "+i, childrenSet.contains(recordId));
						--childCount;
					}
					assertTrue("Too few children for "+i, 0 == childCount);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception: "+e.getMessage());
		}
	}
	private void setupDatabase(String dbFile) {
		try {
			testDatabaseFileCopy = Utilities.copyTestDatabaseFile(dbFile);
			db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
			testLogger.log(Level.INFO, "database rebuilt");
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}

	private void initializeExpectedChildren(HashMap<Integer, HashSet<Integer>> expectedChildren, int lastId) {
		for (int i = 1; i <= lastId; ++i) {
			HashSet s = new HashSet<>();
			expectedChildren.put(i, s);
			RecordList children = db.getChildRecords(i, 0, false);
			for (Record c: children) {
				s.add(c.getRecordId());
			}
		}
	}

	private void addChildren(final int numRecs, int lastId, HashMap<Integer, HashSet<Integer>> expectedChildren)
			throws InputException, FieldDataException, LibrisException {
		int minParent = numRecs;
		int maxParent = 0;
		for (int i = lastId+1; i <= numRecs; ++i) {
			Record rec = db.newRecord();
			maxParent = (int) Math.sqrt(i);
			minParent = Math.min(minParent, maxParent);
			rec.setParent(0, maxParent);
			int recNum = db.put(rec);
			assertEquals("wrong ID for new record",  i, recNum);
			HashSet<Integer> s = expectedChildren.get(maxParent);
			if (null == s) {
				s = new HashSet<>();
				expectedChildren.put(maxParent, s);
			}
			s.add(recNum);
		}
	}

	private void checkChild(int childId, final int parentId) throws InputException {
	Record parentRec = db.getRecord(parentId);
	boolean found = false;
	for (Record r: db.getChildRecords(parentId, 0, false)) {
		if (r.getRecordId() == childId) {
			found = true;
			break;
		}
	}
	assertTrue("child not found", found);
	Record actualChild = db.getRecord(childId);
	assertNotNull("cannot get "+childId, actualChild);
	assertEquals("Wrong parent",  parentId, actualChild.getParent(0));
	}

	private void checkChildren(final int numRecs, HashMap<Integer, HashSet<Integer>> expectedChildren) {
		for (int i = 1; i <= numRecs; ++i) {
			RecordList children;
			children = db.getChildRecords(i, 0, false);
			HashSet<Integer> childrenSet = expectedChildren.get(i);
			if (null == childrenSet) {
				assertNotNull("Record "+i+" has unexpected children");					
			} else {
				assertNotNull("Record "+i+" has no children", children);
				int childCount = childrenSet.size();
				for (Record r: children) {
					int recordId = r.getRecordId();
					assertTrue("Unexpected child "+recordId+" of record "+i, childrenSet.contains(recordId));
					--childCount;
				}
				assertTrue("Too few children for "+i, 0 == childCount);
			}
		}
	}

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

	@After
	public void tearDown() throws Exception {
		Utilities.deleteTestDatabaseFiles(DATABASE_WITH_GROUPS_XML);
	}

}
