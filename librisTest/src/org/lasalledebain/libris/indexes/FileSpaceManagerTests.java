package org.lasalledebain.libris.indexes;


import static org.lasalledebain.Utilities.testLogger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Level;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.exception.DatabaseException;

public class FileSpaceManagerTests extends TestCase {

	private static final short SALT = 31415;
	private static final int MINIMUM_DATA_SIZE = 100;
	private File workDir;
	private File testFile;

	public void testAddData() {
		final int numEntries = 32;
		HashSet<Integer> addedEntries;
		try {
			FileSpaceManager mgr = new FileSpaceManager(testFile, false);
			mgr.reset();
			final int entrySize = 10;
			addedEntries = addEntries(numEntries, entrySize, mgr);
			checkEntries(mgr, entrySize, addedEntries);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception"+e);
		}
	}

	public void testVariableData() {
		final int numEntries = 1000; 
		HashSet<Integer> addedEntries;
		try {
			FileSpaceManager mgr = new FileSpaceManager(testFile, false);
			mgr.reset();
			Random sizeGen = new Random();
			Random dataGen = new Random();
			addedEntries = new HashSet<Integer>();
			int entrySize;
			for (int round = 0; round < 8; ++round) {
				final int salt = 1000+(round * numEntries);
				for (short id = 0; id < numEntries; ++id) {
					sizeGen.setSeed(id + salt);
					entrySize = sizeGen.nextInt(1000)+MINIMUM_DATA_SIZE;
					addEntry(entrySize, mgr, dataGen, id);
					addedEntries.add(new Integer(id));
				}

				testLogger.log(Level.INFO, getName()+": round "+round+" file size = "+mgr.getFileSize());
				mgr.flush();
				for (RecordHeader r: mgr) {
					short id = r.getInput().readShort();
					sizeGen.setSeed(id + salt);
					entrySize = sizeGen.nextInt(1000) + MINIMUM_DATA_SIZE;
					checkEntry(r.dataSize, entrySize, dataGen, r);
					final Integer integerId = new Integer(id);
					boolean wasPresent = addedEntries.remove(integerId);
					assertTrue("unexpected id "+id, wasPresent);
				}
				assertTrue("not all records added", addedEntries.isEmpty());
				deleteAllEntries(mgr);
				mgr.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception"+e);
		}
	}

	public void testDeleteAllEntries() {
		final int numEntries = 32;
		HashSet<Integer> addedEntries;
		try {
			FileSpaceManager mgr = new FileSpaceManager(testFile, false);
			mgr.reset();
			long headerFileSize = mgr.getFileSize();
			final int entrySize = 8;
			addedEntries = addEntries(numEntries, entrySize, mgr);
			long originalFileSize = mgr.getFileSize();
			assertEquals("wrong file size after initial add", headerFileSize + (numEntries * (entrySize + RecordHeader.getHeaderLength())), originalFileSize);
			checkEntries(mgr, entrySize, addedEntries);
			deleteAllEntries(mgr);
			assertEquals("wrong file size after remove add", originalFileSize, mgr.getFileSize());
			addedEntries = addEntries(numEntries, entrySize, mgr);
			assertEquals("wrong file size after reinsert add", originalFileSize, mgr.getFileSize());
			checkEntries(mgr, entrySize, addedEntries);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception"+e);
		}
	}

	private void deleteAllEntries(FileSpaceManager mgr) {
		Iterator<RecordHeader> iter = mgr.iterator();
		while (iter.hasNext()) {
			iter.next();
			iter.remove();
		}
	}

	public void testDeleteSomeEntries() {
		final int numEntries = 32;
		HashSet<Integer> addedEntries;
		try {
			FileSpaceManager mgr = new FileSpaceManager(testFile, false);
			mgr.reset();
			long headerFileSize = mgr.getFileSize();
			final int entrySize = 8;
			addedEntries = addEntries(numEntries, entrySize, mgr);
			HashSet<Integer> removedEntries = new HashSet<Integer>();
			long originalFileSize = mgr.getFileSize();
			assertEquals("wrong file size after initial add", headerFileSize + (numEntries * (entrySize + RecordHeader.getHeaderLength())), originalFileSize);
			checkEntries(mgr, entrySize, addedEntries);
			Iterator<RecordHeader> iter = mgr.iterator();
			while (iter.hasNext()) {
				RecordHeader h = iter.next();
				short headerId = h.getInput().readShort();
				testLogger.log(Level.INFO, "Keep "+headerId);
				if (iter.hasNext()) {
					h = iter.next();
				}
				headerId = h.getInput().readShort();
				Integer id = new Integer(headerId);
				testLogger.log(Level.INFO, "Remove "+headerId);
				removedEntries.add(id);
				iter.remove();
			}
			checkEntries(mgr, entrySize, addedEntries);
			assertEquals("wrong file size after remove add", originalFileSize, mgr.getFileSize());
			Random dataGen = new Random();
			for (Integer id: removedEntries) {
				addEntry(entrySize, mgr, dataGen, id.shortValue());
				addedEntries.add(id);
			}
			assertEquals("wrong file size after reinsert add", originalFileSize, mgr.getFileSize());
			checkEntries(mgr, entrySize, addedEntries);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception"+e);
		}
	}

	private void checkEntries(FileSpaceManager mgr, final int entrySize,
			HashSet<Integer> addedEntries) throws IOException {
		Random dataGen = new Random();
		for (RecordHeader r: mgr) {
			int id = checkEntry(entrySize, entrySize, dataGen, r);
			final Integer integerId = new Integer(id);
			assertTrue("Missing entry "+id, addedEntries.contains(integerId));
		}
	}

	private int checkEntry(final long l, int dataSize, Random dataGen, RecordHeader r)
			throws IOException {
		assertTrue("Wrong size", (dataSize <= r.dataSize) && (l >= r.dataSize));
		DataInput ip = r.getInput();
		int id = ip.readShort();
		dataGen.setSeed(id + SALT);
		for (int j = 2; j < dataSize; j += 2) {
			short d = ip.readShort();
			assertEquals("Wrong data at entry "+r, dataGen.nextInt() % Short.MAX_VALUE, d);
		}
		return id;
	}

	private HashSet<Integer> addEntries(final int numEntries,
			final int entrySize, FileSpaceManager mgr)
			throws DatabaseException, IOException {
		HashSet<Integer> addedEntries;
		addedEntries = new HashSet<Integer>(numEntries);
		Random dataGen = new Random();
		for (short i = 0; i < numEntries; ++i) {
			addEntry(entrySize, mgr, dataGen, i);
			addedEntries.add(new Integer(i));
		}
		return addedEntries;
	}

	private void addEntry(final int entrySize, FileSpaceManager mgr,
			Random dataGen, short id) throws DatabaseException, IOException {
		RecordHeader r = mgr.addNew(entrySize);
		DataOutput op = r.getOutput();
		op.writeShort(id);
		dataGen.setSeed(id + SALT);
		for (int j = 2; j < entrySize; j += 2) {
			op.writeShort(dataGen.nextInt() % Short.MAX_VALUE);
		}
	}

	@Before
	public void setUp() throws Exception {
		workDir = Utilities.getTempTestDirectory();
		if (null == workDir) {
			fail("could not create working directory ");
		}
		testFile = new File(workDir, "tempRecordsFile");
		if (!testFile.exists()) {
			testFile.createNewFile();
		}
	}

	@After
	public void tearDown() throws Exception {
		if (null != testFile) {
			testFile.delete();
		}
	}
}
