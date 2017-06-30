package org.lasalledebain.libris.indexes;

import java.io.File;
import java.util.Random;

import org.lasalledebain.Utilities;
import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.LibrisFileManager;
import org.lasalledebain.libris.exception.DatabaseException;

import junit.framework.TestCase;


public class TestFileRecordMap extends TestCase {
	File workingDirectory;
	File indexFile;
	FileRecordMap index;
	private static final int DEFAULT_NUM_ENTRIES = Integer.getInteger("libris.test.index.numentries", 25000);
	private static final long RANDOM_SEED=20111001;
	LibrisFileManager fileMgr;
	boolean ignoreUnimplemented = Boolean.getBoolean("org.lasalledebain.libris.test.IgnoreUnimplementedTests");
	@Override
	protected void setUp() throws Exception {
		
		workingDirectory = new File(System.getProperty("java.io.tmpdir"), "TestFileRecordMap");
		Utilities.deleteRecursively(workingDirectory);
		workingDirectory.mkdirs();
		indexFile = new File(workingDirectory, "testIndexFile");
		fileMgr  = new LibrisFileManager(workingDirectory, indexFile);
		FileAccessManager indexFileMgr = fileMgr.makeAccessManager(getName(), indexFile);
		index = new FileRecordMap(indexFileMgr, false);
	}

	public void testAddSimpleElements() {
		int postionStride = 16;

		try {
			index.setSize(DEFAULT_NUM_ENTRIES*2);
			populateIndex(DEFAULT_NUM_ENTRIES, postionStride);
			checkIndex(DEFAULT_NUM_ENTRIES, postionStride);
		} catch (Exception exc) {
			exc.printStackTrace();
			fail("unexpected exception: "+exc);
		}
	}

	public void testAddNonContiguousElements() {
		int positionStride = 16;
		try {
			index.setSize(DEFAULT_NUM_ENTRIES*2);
			populateIndex(DEFAULT_NUM_ENTRIES, 2, positionStride);
			checkIndex(DEFAULT_NUM_ENTRIES, 2, positionStride);
		} catch (Exception exc) {
			exc.printStackTrace();
			fail("unexpected exception: "+exc);
		}
	}

	public void testAddLongElements() {
		int positionStride = 40000;

		try {
			int idStride = 2;
			index.setSize(DEFAULT_NUM_ENTRIES*2);
			populateIndex(DEFAULT_NUM_ENTRIES, idStride, positionStride);
			checkIndex(DEFAULT_NUM_ENTRIES, idStride, positionStride);
		} catch (Exception exc) {
			exc.printStackTrace();
			fail("unexpected exception: "+exc);
		}
	}

	public void testReadOnlyElements() {
		int postionStride = 16;

		try {
			index.setSize(DEFAULT_NUM_ENTRIES*2);
			populateIndex(DEFAULT_NUM_ENTRIES, postionStride);
			index.close();
			FileAccessManager indexFileMgr = fileMgr.makeAccessManager(getName(), indexFile);
			index = new FileRecordMap(indexFileMgr, true);
			index.putRecordPosition(69,31415926535L);
		} catch (DatabaseException exc) {
			System.out.println("caught expected exception: "+exc.getMessage());
			try {
				index.close();
			} catch (DatabaseException e) {
				exc.printStackTrace();
				fail("unexpected exception: "+exc);
			}
			return;
		} catch (Exception exc) {
			exc.printStackTrace();
			fail("unexpected exception: "+exc);
		}
		fail("missed expected DatabaseException");

	}

	public void testRandomAccess() {

		int postionStride = 1;
		Random idGen;

		try {
			idGen = getRand();
			int NumEntries = 3000;
			index.setSize(NumEntries*2);
			for (int iteration = 1; iteration < NumEntries; ++iteration) {
				long posn = postionStride * iteration;
				int id = nextId(idGen);
				try {
					index.putRecordPosition(id, posn);
					if ((iteration % 1000) == 0) {
						System.out.println("id="+id+" position="+posn);
					}
					if (iteration > 1000) {
						assertEquals(6, index.getRecordPosition(1789429131));
					}
				} catch (Exception exc) {
					exc.printStackTrace();
					fail("unexpected exception: id="+id+" iteration="+iteration+",  "+exc);
				}
			}
			index.flush();
			idGen = getRand();
			for (int iteration = 1; iteration < NumEntries; ++iteration) {
				long expectedPosn = postionStride * iteration;
				int id = nextId(idGen);
				try {
					long actualPosn = index.getRecordPosition(id);
					assertEquals("Wrong position for record "+id+" ("+Integer.toHexString(id)+") , iteration "
							+iteration+" position="+Long.toHexString(expectedPosn), expectedPosn, actualPosn);
				} catch (Exception exc) {
					exc.printStackTrace();
					fail("unexpected exception: "+exc);
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			fail("unexpected exception: "+exc);
		}
	}

	/**
	 * @param idGen
	 * @return
	 */
	private int nextId(Random idGen) {
		int id;
		while ((id = idGen.nextInt()) <= 0);
		return id;
	}

	/**
	 * @return
	 */
	private Random getRand() {
		Random idGen;
		idGen = new Random(RANDOM_SEED);
		nextId(idGen);
		return idGen;
	}

	private void checkIndex(int numEntries, int positionStride) throws DatabaseException {
		checkIndex(numEntries, 1, positionStride);
	}

	private void populateIndex(int defaultNumEntries, int positionStride) throws DatabaseException {
		populateIndex(DEFAULT_NUM_ENTRIES, 1, positionStride);
	}

	/**
	 * @param numEntries
	 * @param positionStride
	 * @throws DatabaseException 
	 */
	private void checkIndex(int numEntries, int idStride, int positionStride) throws DatabaseException {
		long posn;
		for (int i = 1; i < numEntries; ++i) {
			long expectedPosn = positionStride * i;
			int id = i*idStride;
			posn = index.getRecordPosition(id);
			assertEquals("incorrect position returned for record "+id, expectedPosn, posn);
			if ((i % (numEntries/64)) == 0) {
				System.out.print("<");
			}
		}
		System.out.print('\n');
	}

	/**
	 * @param numEntries
	 * @param postionStride
	 * @throws DatabaseException 
	 */
	private void populateIndex(int numEntries, int idStride, int postionStride) throws DatabaseException {
		long posn;
		for (int i = 1; i < numEntries; ++i) {
			posn = postionStride * i;
			int id = i*idStride;
			try {
				index.putRecordPosition(id, posn);
			} catch (Exception exc) {
				exc.printStackTrace();
				fail("unexpected exception: "+exc);
			}
			if ((i % (numEntries/64)) == 0) {
				System.out.print(">");
			}
		}
		System.out.println("\nindex populated");
		index.flush();
	}

	public void testRandomIdsAndPosition() {
		if (!ignoreUnimplemented) fail("not implemented");
	}

	public void testOverflow() {
		if (!ignoreUnimplemented) fail("not implemented");
	}

	public void testUpdateEntries() {
		if (!ignoreUnimplemented) fail("not implemented");
	}

	@Override
	protected void tearDown() throws Exception {
		index.close();
		Utilities.deleteRecursively(workingDirectory);
	}


}
