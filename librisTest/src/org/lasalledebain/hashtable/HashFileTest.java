package org.lasalledebain.hashtable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Random;

import org.junit.Test;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.hashfile.FixedSizeEntryHashBucket;
import org.lasalledebain.libris.hashfile.FixedSizeHashEntry;
import org.lasalledebain.libris.hashfile.HashBucket;
import org.lasalledebain.libris.hashfile.HashFile;
import org.lasalledebain.libris.hashfile.VariableSizeEntryHashBucket;
import org.lasalledebain.libris.hashfile.VariableSizeHashEntry;
import org.lasalledebain.libris.indexes.FileSpaceManager;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class HashFileTest extends TestCase {

	private File testFileObject;
	private MockVariableSizeEntryFactory vFactory = null;
	private MockFixedSizeEntryFactory FFactory = null;
	private RandomAccessFile backingStore;

	@Test
	public void testAddAndGet() {
		try {
			HashFile<VariableSizeHashEntry> htable = makeVHashTable();
			ArrayList<VariableSizeHashEntry> entries = new ArrayList<VariableSizeHashEntry>();
			
			addVariableSizeEntries(htable, entries, 32, 0, true);
			
			for (VariableSizeHashEntry e: entries) {
				VariableSizeHashEntry f = htable.getEntry(e.getKey());
				assertNotNull("Could not find entry", f);
				assertEquals("Entry mismatch", e, f);
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
	}

	@Test
	public void testOverflow() {
		try {
			HashFile<VariableSizeHashEntry> htable = makeVHashTable();
			ArrayList<VariableSizeHashEntry> entries = new ArrayList<VariableSizeHashEntry>();
			
			int currentKey = 1;
			while (currentKey < 1000) {
				currentKey = addVariableSizeEntries(htable, entries, 127, currentKey, true);
				print(currentKey+" entries added.  Checking...\n");
				for (VariableSizeHashEntry e: entries) {
					int key = e.getKey();
					VariableSizeHashEntry f = htable.getEntry(key);
					if (null == f) {
						print("key="+key+" not found; ");
						printHashBuckets(key);
						print("\n");
					}
					try {
						assertNotNull("Could not find entry "+key, f);
						assertEquals("Entry mismatch", e, f);
					} catch (AssertionFailedError a) {
						throw a;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
	}

	/**
	 * @param key
	 */
	private void printHashBuckets(int key) {
		for (int i = 1; i <= 16; i *= 2) {
			int hashedKey = HashFile.hash(key);
			int homeBucket = (int) hashedKey % (2*i);
			if (homeBucket >= i) {
				homeBucket -= i;
			}
			print("modulus="+i+" bucket="+homeBucket+"; ");
		}
	}

	@Test
	public void testExpand() {
		int searchKey=0;
		try {
			HashFile<VariableSizeHashEntry> htable = makeVHashTable();
			ArrayList<VariableSizeHashEntry> entries = new ArrayList<VariableSizeHashEntry>();
			
			print("add first batch of entries\n");
			int lastKey = addVariableSizeEntries(htable, entries, 400, 10, true);
			print("Expand hash file\n");
			htable.resize(1000);
			
			print("Check file\n");
			for (VariableSizeHashEntry e: entries) {
				searchKey = e.getKey();
				VariableSizeHashEntry f = htable.getEntry(searchKey);
				assertNotNull("Coud not find entry "+e.getKey(), f);
			}
			print("add second batch of entries\n");
			addVariableSizeEntries(htable, entries, 400, lastKey, true);
			print("Check file again\n");
			for (VariableSizeHashEntry e: entries) {
				searchKey = e.getKey();
				VariableSizeHashEntry f = htable.getEntry(searchKey);
				assertNotNull("Coud not find entry\n", f);
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		} catch (AssertionFailedError e) {
			printHashBuckets(searchKey);
			throw e;
		}
	}

	private void print(String msg) {
		System.out.print(msg);
	}

	/**
	 * @param htable
	 * @param entries
	 * @param numEntries
	 * @param keyBase TODO add keyBase
	 * @param countUp TODO add countUp
	 * @throws IOException
	 * @throws DatabaseException 
	 */
	private int addVariableSizeEntries(HashFile<VariableSizeHashEntry> htable,
			ArrayList<VariableSizeHashEntry> entries, int numEntries, int keyBase, boolean countUp)
			throws IOException, DatabaseException {
		int modulus = Math.max(1, numEntries/64);

		for (int i=0; i<numEntries; i++) {
			MockVariableSizeHashEntry e = vFactory.makeEntry(countUp? (keyBase+i):(keyBase+numEntries-i));
			htable.addEntry(e);
			entries.add(e);
			if ((i > 0) && (i%modulus == 0)) {
				print(">");
			}
		}
		htable.flush();
		print("\n"+numEntries+" added\n");

		return keyBase+numEntries;
	}

	private int addFixedSizeEntries(HashFile<FixedSizeHashEntry> htable,
			ArrayList<FixedSizeHashEntry> entries, int numEntries, int keyBase, boolean countUp)
			throws IOException, DatabaseException {
		int modulus = Math.max(1, numEntries/64);

		for (int i=0; i<numEntries; i++) {
			MockFixedSizeHashEntry e = FFactory.makeEntry(countUp? (keyBase+i):(keyBase+numEntries-i));
			htable.addEntry(e);
			entries.add(e);
			if ((i > 0) && (i%modulus == 0)) {
				print(">");
			}
		}
		htable.flush();
		print("\n"+numEntries+" added\n");

		return keyBase+numEntries;
	}

	@Test
	public void testMissing() {
		try {
			HashFile<VariableSizeHashEntry> htable = makeVHashTable();
			ArrayList<VariableSizeHashEntry> entries = new ArrayList<VariableSizeHashEntry>();

			int nextKey = addVariableSizeEntries(htable, entries, 400, 1, true);
			for (int key = nextKey; key < 1000000000; key *= 5) {
				VariableSizeHashEntry f = htable.getEntry(key);
				assertNull("Found spurious entry "+key, f);
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
	}

	@Test
	public void testReplace() {
		try {
			HashFile<VariableSizeHashEntry> htable = makeVHashTable();
			ArrayList<VariableSizeHashEntry> entries = new ArrayList<VariableSizeHashEntry>();

			int nextKey = addVariableSizeEntries(htable, entries, 400, 1, true);
			for (VariableSizeHashEntry e: entries) {
				
				byte oldData[] = e.getData();
				for (int i=0;i<oldData.length;++i) {
					oldData[i] += i;
				}
				htable.addEntry(e);
				htable.flush();
				int key = e.getKey();
				VariableSizeHashEntry f = htable.getEntry(key);
				assertNotNull("Could not find entry "+key, f);
				assertEquals("Entry mismatch", e, f);
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
	}

	@Test
	public void testAddDecreasing() {
		try {
			HashFile<VariableSizeHashEntry> htable = makeVHashTable();
			ArrayList<VariableSizeHashEntry> entries = new ArrayList<VariableSizeHashEntry>();
			
			addVariableSizeEntries(htable, entries, 1000, 0, false);
			
			for (VariableSizeHashEntry e: entries) {
				VariableSizeHashEntry f = htable.getEntry(e.getKey());
				assertNotNull("Coud not find entry", f);
				assertEquals("Entry mismatch", e, f);
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
	}

	private HashFile<VariableSizeHashEntry> makeVHashTable() throws IOException {
		FileSpaceManager mgr = Utilities.makeFileSpaceManager(getName()+"_mgr");
		MockOverflowManager oversizeEntryManager = new MockOverflowManager(mgr);
		HashFile<VariableSizeHashEntry> htable = 
				new HashFile<VariableSizeHashEntry>(backingStore, 
				VariableSizeEntryHashBucket.getFactory(oversizeEntryManager), vFactory);
		return htable;
	}

	@Test
	public void testAddRandom() {
		try {
			HashFile<VariableSizeHashEntry> htable = makeVHashTable();
			ArrayList<MockVariableSizeHashEntry> entries = new ArrayList<MockVariableSizeHashEntry>();
			int seed = 1234567;
			Random key = new Random(seed);
			for (int i=0; i<1000; i++) {				
				MockVariableSizeHashEntry me = vFactory.makeEntry(Math.abs(key.nextInt()));
				htable.addEntry(me);
				entries.add(me);
			}
						
			htable.flush();
			for (VariableSizeHashEntry e: entries) {
				VariableSizeHashEntry f = htable.getEntry(e.getKey());
				assertNotNull("Coud not find entry", f);
				assertEquals("Entry mismatch", e, f);
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile:"+e);
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
	}
	
	public void testNumEntries() {
		try {
			HashFile<FixedSizeHashEntry> htable = new HashFile<FixedSizeHashEntry>(backingStore, FixedSizeEntryHashBucket.getFactory(), FFactory);
			ArrayList<FixedSizeHashEntry> entries = new ArrayList<FixedSizeHashEntry>();

			final int NUM_ENTRIES_INCREMENT = 400;
			int lastKey = 1;
			for (int i=0; i < 20; ++i) {
				try {
					int numEntries = htable.getNumEntries();
					assertEquals("wrong number of entries", i*NUM_ENTRIES_INCREMENT, numEntries);
					lastKey = addFixedSizeEntries(htable, entries, NUM_ENTRIES_INCREMENT, lastKey, true);
				} catch (Exception e) {
					e.printStackTrace();
					fail("Unexpected exception on hashfile:"+e);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
	}

	@Test
	public void testHugeFile() {
		try {
			final int NUM_ENTRIES=100000;
			HashFile<VariableSizeHashEntry> htable = makeVHashTable();
			ArrayList<VariableSizeHashEntry> entries = new ArrayList<VariableSizeHashEntry>();
			
			int recordsPerBucket = HashBucket.BUCKET_SIZE/vFactory.getEntrySize();
			int requestedBuckets = (NUM_ENTRIES*2+recordsPerBucket-1)/recordsPerBucket;
			htable.resize(requestedBuckets);
			
			addVariableSizeEntries(htable, entries, NUM_ENTRIES, 0, true);
			
			for (VariableSizeHashEntry e: entries) {
				VariableSizeHashEntry f = htable.getEntry(e.getKey());
				assertNotNull("Coud not find entry", f);
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
	}


	protected void setUp() throws Exception {
		if (null == vFactory) {
			vFactory = new MockVariableSizeEntryFactory(28);
		}
		if (null == FFactory) {
			FFactory = new MockFixedSizeEntryFactory(28);
		}
		if (null == testFileObject) {
			testFileObject = Util.makeTestFileObject("hashFile");
		}
		testFileObject.delete();
		backingStore = new RandomAccessFile(testFileObject, "rw");
		backingStore.setLength(0);
	}

	@Override
	protected void tearDown() throws Exception {
		if (null != testFileObject) {
			testFileObject.delete();
		}
	}

}
