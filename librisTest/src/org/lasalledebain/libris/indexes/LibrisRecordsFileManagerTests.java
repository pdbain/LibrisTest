package org.lasalledebain.libris.indexes;


import static org.lasalledebain.Utilities.testLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.lasalledebain.MockSchema;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.LibrisFileManager;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.RecordTemplate;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.util.DiagnosticDatabase;

/**
 * Test the native records file alone
 *
 */
public class LibrisRecordsFileManagerTests extends TestCase {

	protected static final String ID_T_FIELD_STRING = "id_T_FIELD_STRING";
	MockSchema schem = null;
	private RecordTemplate recTemplate;
	Random rand = new Random(52748372);
	private File workDir;
	private final int NUM_ENUM_CHOICES = 10;
	private static FileAccessManager testRecordsFile;
	private File testDatabaseFile;
	LibrisFileManager fileMgr;

	public void testAddRecord() {
		LibrisRecordsFileManager recFile = makeRecFileMgr();
		try {
			for (int i= 1; i <= 4; ++i) {
				Record r = makeRandomRecord(recTemplate, i);
				recFile.putRecord(r);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}
	public void testIterator() {
		LibrisRecordsFileManager recFile = makeRecFileMgr();
		ArrayList<Record> recList = new ArrayList<Record>();
		testLogger.log(Level.INFO, "create records");
		try {
			for (int i= 1; i <= 4; ++i) {
				Record r = makeRandomRecord(recTemplate, i);
				recFile.putRecord(r);
				recList.add(r);
			}
			recFile.flush();
			readAndCompareRecords(recFile, recList);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}
	public void testRandomRecordAccess() {

		LibrisRecordsFileManager recFile = makeRecFileMgr();
		ArrayList<Record> expectedRecordList = new ArrayList<Record>();
		testLogger.log(Level.INFO, "create records");
		final int NUM_RECORDS = 100;
		try {
			for (int i= 1; i <= NUM_RECORDS; ++i) {
				Record r = makeRandomRecord(recTemplate, i);
				recFile.putRecord(r);
				expectedRecordList.add(r);
			}
			recFile.flush();
			testLogger.log(Level.INFO, "read back records");
			for (int i= 1; i < NUM_RECORDS; ++i) {
				getRecordAndCompare(recFile, expectedRecordList, i);
			}
			for (int i= NUM_RECORDS-1; i >= 1; --i) {
				getRecordAndCompare(recFile, expectedRecordList, i);
			}
			Random rg = new Random(8096);
			for (int i= NUM_RECORDS-1; i >= 1; --i) {
				int recNum = rg.nextInt(NUM_RECORDS);
				getRecordAndCompare(recFile, expectedRecordList, recNum);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}
	public void testGetNonexistentRecord() {

		LibrisRecordsFileManager recFile = makeRecFileMgr();
		ArrayList<Record> expectedRecordList = new ArrayList<Record>();
		testLogger.log(Level.INFO, "create records");
		final int NUM_RECORDS = 4;
		try {
			for (int i= 1; i <= NUM_RECORDS; ++i) {
				Record r = makeStringRecord(recTemplate, i);
				recFile.putRecord(r);
				expectedRecordList.add(r);
			}
			recFile.flush();
			int rid = NUM_RECORDS+2;
			Record r = recFile.getRecord(rid);
			assertNull("error retrieving non-existent record", r);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}
	public void testReplaceRecordInPlace() {

		LibrisRecordsFileManager recFile = makeRecFileMgr();
		ArrayList<Record> expectedRecordList = new ArrayList<Record>();
		testLogger.log(Level.INFO, "create records");
		final int NUM_RECORDS = 4;
		try {
			for (int i= 1; i <= NUM_RECORDS; ++i) {
				Record r = makeStringRecord(recTemplate, i);
				recFile.putRecord(r);
				expectedRecordList.add(r);
			}
			int i = 1;
			for (Record r: expectedRecordList) {
				Field f = r.getField(0);
				f.changeValue("short "+i++);
				recFile.putRecord(r);
			}
			recFile.flush();
			i = 1;
			readAndCompareRecords(recFile, expectedRecordList);
			for (Record r: expectedRecordList) {
				Field f = r.getField(0);
				f.changeValue("long abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz "+i++);
				recFile.putRecord(r);
			}
			Record r = makeStringRecord(recTemplate, NUM_RECORDS+1);
			recFile.putRecord(r);
			expectedRecordList.add(r);
			recFile.flush();
			readAndCompareRecords(recFile, expectedRecordList);

		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}

	public void testRandomReplaceRecordInPlace() {

		LibrisRecordsFileManager recFile = makeRecFileMgr();
		ArrayList<Record> expectedRecordList = new ArrayList<Record>();
		testLogger.log(Level.INFO, "create records");
		final int NUM_RECORDS = 100;
		Random selector = new Random(getName().hashCode());
		try {
			for (int i= 1; i <= NUM_RECORDS; ++i) {
				Record r = makeStringRecord(recTemplate, i);
				recFile.putRecord(r);
				expectedRecordList.add(r);
			}
			for (int i= 1; i <= NUM_RECORDS; ++i) {
				int rnum = selector.nextInt(NUM_RECORDS);
				Record r = expectedRecordList.get(rnum);
				String[] fids = r.getFieldIds();
				String victim = fids[i%fids.length];
				Field f = r.getField(victim);
				f.changeValue("short "+i);
				recFile.putRecord(r);
			}
			recFile.flush();
			readAndCompareRecords(recFile, expectedRecordList);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}

	public void testReplaceRecordAtEnd() {
		LibrisRecordsFileManager recFile = makeRecFileMgr();
		ArrayList<Record> expectedRecordList = new ArrayList<Record>();
		testLogger.log(Level.INFO, "create records");
		final int NUM_RECORDS = 100;
		Random selector = new Random(getName().hashCode());
		try {
			for (int i= 1; i <= NUM_RECORDS; ++i) {
				Record r = makeStringRecord(recTemplate, i);
				recFile.putRecord(r);
				expectedRecordList.add(r);
			}
			for (int i= 1; i <= NUM_RECORDS; ++i) {
				int rnum = selector.nextInt(NUM_RECORDS);
				Record r = expectedRecordList.get(rnum);
				String[] fids = r.getFieldIds();
				String victim = fids[i%fids.length];
				Field f = r.getField(victim);
				f.changeValue("short "+i);
				recFile.putRecord(r);
			}
			recFile.flush();
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}

	public void testRemoveRecord() {
		LibrisRecordsFileManager recFile = makeRecFileMgr();
		ArrayList<Record> expectedRecordList = new ArrayList<Record>();
		testLogger.log(Level.INFO, "create records");
		try {
			final int NUM_RECORDS = 5;
			for (int i= 1; i <= NUM_RECORDS; ++i) {
				Record r = makeRandomRecord(recTemplate, i);
				recFile.putRecord(r);
				expectedRecordList.add(r);
			}
			int recNums[] = {3 /* last */, 0 /* first */, 1 /* middle */, 1 /* last */, 0 /* empty */};
			for (int i: recNums) {
				Record r = expectedRecordList.get(i);
				expectedRecordList.remove(i);
				int rid = r.getRecordId();
				recFile.removeRecord(rid);
				recFile.flush();

				readAndCompareRecords(recFile, expectedRecordList);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}
	public void testReopenFile() {
		LibrisRecordsFileManager recFile = makeRecFileMgr();
		ArrayList<Record> recList = new ArrayList<Record>();
		testLogger.log(Level.INFO, "create records");
		try {
			for (int i= 1; i <= 4; ++i) {
				Record r = makeRandomRecord(recTemplate, i);
				recFile.putRecord(r);
				recList.add(r);
			}
			recFile.close();
			recFile.open();
			readAndCompareRecords(recFile, recList);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}

	public void testStandardRecord() {
		LibrisRecordsFileManager recFile = makeRecFileMgr();
		ArrayList<Record> recList = new ArrayList<Record>();
		testLogger.log(Level.INFO, "create records");
		try {
			for (int i= 1; i <= 4; ++i) {
				Record r = makeStandardRecord(recTemplate, i);
				recFile.putRecord(r);
				recList.add(r);
			}
			recFile.flush();
			readAndCompareRecords(recFile, recList);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}
	
	public void testFileBackup () {
		try {
			FileOutputStream opstream = testRecordsFile.getOpStream();
			assertFalse("read-only files should not be open", testRecordsFile.readOnlyFilesOpen());
			assertTrue("read-write files should be open", testRecordsFile.readWriteFilesOpen());
			for (int i = 0; i < 128; ++i) {
				opstream.write(i);
			}
			long expectedLength = testRecordsFile.getLength();
			testRecordsFile.releaseOpStream();
			assertFalse("read-write files should not be open", testRecordsFile.readWriteFilesOpen());
			File backup = testRecordsFile.renameToBackup(true);
			assertNotNull("backup file not created", backup);
			
			opstream = testRecordsFile.getOpStream();
			for (int i = 0; i < 256; ++i) {
				opstream.write((3 * i) % 256);
			}

			FileInputStream testIpStream = new FileInputStream(backup);
			assertEquals("backup file wrong length", backup.length(), expectedLength);
			for (int i = 0; i < 128; ++i) {
				int x = testIpStream.read();
				assertEquals("data wrong", x, i);
			}
			
			testIpStream = testRecordsFile.getIpStream();
			for (int i = 0; i < 256; ++i) {
				int x = testIpStream.read();
				assertEquals("data wrong", x, ((3 * i) % 256));
			}
			testRecordsFile.releaseIpStream(testIpStream);
			testRecordsFile.delete();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail("unexpected exception "+e);
		} catch (IOException e) {
			e.printStackTrace();
			fail("unexpected exception "+e);
		}
	}
	
	public void testBulkImportSanity() {
		try {
			ArrayList<Record> recList = new ArrayList<Record>();
			DiagnosticDatabase testDb = new DiagnosticDatabase(testDatabaseFile);
			RecordPositions recPosns = new RecordPositions(new MockLibrisRecordMap());
			FileOutputStream opStream = testRecordsFile.getOpStream();
			BulkImporter importer = new BulkImporter(schem, opStream, recPosns);
			importer.initialize();
			for (int i= 1; i <= 4; ++i) {
				Record r = makeStandardRecord(recTemplate, i);
				importer.putRecord(r);
				recList.add(r);
			}
			importer.finish(true);
			opStream.close();
			recPosns.close();
			LibrisRecordsFileManager recMgr = new LibrisRecordsFileManager(testDb, false, schem, 
					testRecordsFile, recPosns);
			readAndCompareRecords(recMgr, recList);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e);
		}
	}

	public void testBulkImportStress() {
		try {
			ArrayList<Record> recList = new ArrayList<Record>();
			DiagnosticDatabase testDb = new DiagnosticDatabase(testDatabaseFile);
			RecordPositions recPosns = new RecordPositions(new MockLibrisRecordMap());
			FileOutputStream opStream = testRecordsFile.getOpStream();
			BulkImporter importer = new BulkImporter(schem, opStream, recPosns);
			long startTime = System.currentTimeMillis();
			importer.initialize();
			int numRecs = 100000;
			for (int i= 1; i <= numRecs; ++i) {
				Record r = makeRandomRecord(recTemplate, i);
				importer.putRecord(r);
				recList.add(r);
			}
			importer.finish(true);
			opStream.close();
			recPosns.close();
			long endTime = System.currentTimeMillis();
			double elapsedTime = 0.0 + endTime - startTime;
			testLogger.log(Level.INFO, "import time="+(elapsedTime/1000)+" seconds, "+(elapsedTime/numRecs)+" ms/record");
			LibrisRecordsFileManager recMgr = new LibrisRecordsFileManager(testDb, false, schem, 
					testRecordsFile, recPosns);
			readAndCompareRecords(recMgr, recList, false);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e);
		}
	}

	private void readAndCompareRecords(LibrisRecordsFileManager recFile,
			ArrayList<Record> expectedRecords, boolean verbose) {
		testLogger.log(Level.INFO, "read back records");
		for (Record er: expectedRecords) {
			if (null == er) continue;
			int rid = er.getRecordId();
			Record ar;
			try {
				ar = recFile.getRecord(rid);
				if (null == ar) {
					fail("could not read record");
				}
				if (verbose) {
					testLogger.log(Level.INFO, "Expected record:\n"+er);
					testLogger.log(Level.INFO, "Actual record:\n"+ar);
				}
				assertEquals("record read != original", er, ar);
			} catch (InputException e) {
				e.printStackTrace();
				fail("Unexpected exception");
			}
		}
		int recordCount = recFile.countRecords();
		assertEquals("Wrong number of records", expectedRecords.size(), recordCount);
	}
	@Before
	public void setUp() throws Exception {
		testLogger.log(Level.INFO, "Starting "+getName());
		workDir = Utilities.getTempTestDirectory();
		testDatabaseFile = Utilities.getTestDatabase(Utilities.TEST_DB1_XML_FILE);
		if (null == workDir) {
			fail("could not create working directory ");
		}
		fileMgr  = new LibrisFileManager(workDir, testDatabaseFile);
		testRecordsFile = fileMgr.makeAccessManager(getName(), new File(workDir, "tempRecordsFile"));

		if (null == schem) {
			schem = makeSchemaWithAllFieldTypes();
		}
		if (null ==recTemplate) {
			recTemplate = makeRecordTemplate(schem);
		}
	}

	@After
	public void tearDown() throws Exception {
		testLogger.log(Level.INFO, "Ending "+getName());
		Utilities.deleteRecursively(workDir);
	}

	public MockSchema makeSchemaWithAllFieldTypes() {
		try {
			MockSchema s = new MockSchema();
			for (FieldType t: FieldType.values()) {
				if (FieldType.T_FIELD_UNKNOWN == t) {
					continue;
				}
				String tname = t.name();
				String fieldTitle = "name_"+tname;
				String fieldId = "id_"+tname;
				FieldTemplate f = new FieldTemplate(new MockSchema(), fieldId, fieldTitle, t);
				s.addField(f);
				if (t == FieldType.T_FIELD_ENUM) {
					String[] ec = new String[NUM_ENUM_CHOICES];
					for (int i = 0; i < NUM_ENUM_CHOICES; ++i) {
						ec[i] = tname+"_choice_"+i;
					}
					EnumFieldChoices choices = new EnumFieldChoices("enum_"+tname, ec);
					f.setEnumChoices(choices);
				}
			}
			return s;
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
		return null;
	}

	RecordTemplate makeRecordTemplate(Schema s) throws InputException {
		RecordTemplate rt = RecordTemplate.templateFactory(s);
		return rt;		
	}

	Record makeRandomRecord(RecordTemplate rt, int recId) {
		Record result = null;
		try {
			result = rt.makeRecord();
			for (String fid: rt.getFieldIds()) {
				FieldType fType = result.getFieldType(fid);
				if (FieldType.T_FIELD_UNKNOWN == fType) {
					continue;
				}
				Field f = null;
				for (int i = 0; i <= rand.nextInt(3); ++i) { 
					switch (fType) {
					case T_FIELD_BOOLEAN: int iData = rand.nextInt(2);
					if (0 == i) {
						f = result.addFieldValue(fid, rand.nextInt(2));
					} else {
						continue; /* only one boolean allowed */
					} 
					break;
					case T_FIELD_ENUM: iData = rand.nextInt(10);
					if (null == f) {
						f = result.addFieldValue(fid, iData);
					} else {
						f.addIntegerValue(iData);
					} 
					break;
					case T_FIELD_INDEXENTRY: iData = rand.nextInt(100);
					if (null == f) {
						f = result.addFieldValue(fid, Integer.toString(iData));
					} else {
						f.addIntegerValue(iData);
					} 
					break;
					case T_FIELD_INTEGER: iData = rand.nextInt();
					if (null == f) {
						f = result.addFieldValue(fid, Integer.toString(iData));
					} else {
						f.addIntegerValue(iData);
					} 
					break;
					case T_FIELD_PAIR: String sData = Integer.toString(Math.abs(rand.nextInt()));
					if (null == f) {
						f = result.addFieldValue(fid, "");
					}
					f.addValuePair("main"+sData, "extra"+sData);
					break;
					case T_FIELD_STRING: sData = "Stringdata"+Integer.toString(Math.abs(rand.nextInt()));
					if (null == f) {
						f = result.addFieldValue(fid, sData);
					} else {
						f.addValue(sData);
					} 
					break;
					}
				}
			}
			result.setRecordId(recId);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
		return result;

	}


	Record makeStandardRecord(RecordTemplate rt, int recId) {
		Record result = null;
		int fieldData = 1;
		try {
			result = rt.makeRecord();
			for (String fid: rt.getFieldIds()) {
				FieldType fType = result.getFieldType(fid);
				if (FieldType.T_FIELD_UNKNOWN == fType) {
					continue;
				}
				switch (fType) {
				case T_FIELD_BOOLEAN: {
					int iData = fieldData;
					break;
				}
				case T_FIELD_ENUM: {
					int iData = rand.nextInt(10);
					result.addFieldValue(fid, iData);
					break;
				}
				case T_FIELD_INDEXENTRY: {
					int iData = fieldData;
					result.addFieldValue(fid, Integer.toString(iData));
					break;
				} 
				case T_FIELD_INTEGER: {
					int iData = fieldData;
					result.addFieldValue(fid, Integer.toString(iData));
					break;
				} 
				case T_FIELD_PAIR: {
					String sData = Integer.toString(fieldData);
					Field f = result.addFieldValue(fid, "");
					f.addValuePair("main"+sData, "extra"+sData);
					break;
				}
				case T_FIELD_STRING: {
					String sData = "Stringdata"+Integer.toString(fieldData);
					result.addFieldValue(fid, sData);
					break;
				}
				}
			}
			result.setRecordId(recId);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
		return result;

	}

	private Record makeStringRecord(RecordTemplate rt, int recId) {
		Record result = null;
		try {
			result = rt.makeRecord();
			result.addFieldValue(ID_T_FIELD_STRING, "id_T_FIELD_STRING "+recId+" data");
			result.setRecordId(recId);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
		return result;

	}
	private LibrisRecordsFileManager makeRecFileMgr() {
		try {
			DiagnosticDatabase db = new DiagnosticDatabase(testDatabaseFile);
			RecordPositions recPosns = new RecordPositions(new MockLibrisRecordMap());
			LibrisRecordsFileManager recMgr = new LibrisRecordsFileManager(db, false, schem, testRecordsFile, recPosns);
			recMgr.reset();
			return recMgr;
		} catch (LibrisException e) {
			e.printStackTrace();
			fail("cannot open "+testRecordsFile.getPath());
		}
		return null;
	}
	private void getRecordAndCompare(LibrisRecordsFileManager recFile,
			ArrayList<Record> expectedRecordList, int i) {
		Record expectedRecord = expectedRecordList.get(i);
		int rid = expectedRecord.getRecordId();
		Record actualRecord;
		try {
			actualRecord = recFile.getRecord(rid);
			if (null == actualRecord) {
				fail("could not read record");
			}
			assertEquals("record read != original", expectedRecord, actualRecord);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception");
		}
	}
	private void readAndCompareRecords(LibrisRecordsFileManager recFile,
			ArrayList<Record> expectedRecords) {
		readAndCompareRecords(recFile, expectedRecords, true);
	}
}
