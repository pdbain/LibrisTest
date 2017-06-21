package org.lasalledebain.hashtable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Random;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.junit.Test;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.hashfile.FixedSizeEntryHashBucket;
import org.lasalledebain.libris.hashfile.HashFile;
import org.lasalledebain.libris.hashfile.VariableSizeHashEntry;
import org.lasalledebain.libris.index.AbstractVariableSizeHashEntry;

public class HashFileTest extends TestCase {

	private File testFileObject;
	private MockVariableSizeEntryFactory efactory = null;
	private RandomAccessFile backingStore;

	@Test
	public void testAddAndGet() {
		try {
			HashFile<VariableSizeHashEntry> htable = new HashFile<VariableSizeHashEntry>(backingStore, FixedSizeEntryHashBucket.getFactory(), efactory);
			ArrayList<VariableSizeHashEntry> entries = new ArrayList<VariableSizeHashEntry>();
			
			addEntries(htable, entries, 32, 0, true);
			
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
			HashFile<VariableSizeHashEntry> htable = new HashFile<VariableSizeHashEntry>(backingStore, FixedSizeEntryHashBucket.getFactory(), efactory);
			ArrayList<VariableSizeHashEntry> entries = new ArrayList<VariableSizeHashEntry>();
			
			int currentKey = 1;
			while (currentKey < 1000) {
				currentKey = addEntries(htable, entries, 127, currentKey, true);
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
			HashFile<VariableSizeHashEntry> htable = new HashFile<VariableSizeHashEntry>(backingStore, FixedSizeEntryHashBucket.getFactory(), efactory);
			ArrayList<VariableSizeHashEntry> entries = new ArrayList<VariableSizeHashEntry>();
			
			print("add first batch of entries\n");
			int lastKey = addEntries(htable, entries, 400, 10, true);
			print("Expand hash file\n");
			htable.setSize(1000);
			
			print("Check file\n");
			for (VariableSizeHashEntry e: entries) {
				searchKey = e.getKey();
				VariableSizeHashEntry f = htable.getEntry(searchKey);
				assertNotNull("Coud not find entry "+e.getKey(), f);
			}
			print("add second batch of entries\n");
			addEntries(htable, entries, 400, lastKey, true);
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
	private int addEntries(HashFile<VariableSizeHashEntry> htable,
			ArrayList<VariableSizeHashEntry> entries, int numEntries, int keyBase, boolean countUp)
			throws IOException, DatabaseException {
		int modulus = Math.max(1, numEntries/64);

		for (int i=0; i<numEntries; i++) {
			MockVariableSizeHashEntry e = efactory.makeEntry(countUp? (keyBase+i):(keyBase+numEntries-i));
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
			HashFile<VariableSizeHashEntry> htable = new HashFile<VariableSizeHashEntry>(backingStore, FixedSizeEntryHashBucket.getFactory(), efactory);
			ArrayList<VariableSizeHashEntry> entries = new ArrayList<VariableSizeHashEntry>();

			int nextKey = addEntries(htable, entries, 400, 1, true);
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
			HashFile<VariableSizeHashEntry> htable = new HashFile<VariableSizeHashEntry>(backingStore, FixedSizeEntryHashBucket.getFactory(), efactory);
			ArrayList<VariableSizeHashEntry> entries = new ArrayList<VariableSizeHashEntry>();

			int nextKey = addEntries(htable, entries, 400, 1, true);
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
			HashFile<VariableSizeHashEntry> htable = new HashFile<VariableSizeHashEntry>(backingStore, FixedSizeEntryHashBucket.getFactory(), efactory);
			ArrayList<VariableSizeHashEntry> entries = new ArrayList<VariableSizeHashEntry>();
			
			addEntries(htable, entries, 1000, 0, false);
			
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

	@Test
	public void testAddRandom() {
		try {
			HashFile<MockVariableSizeHashEntry> htable = new HashFile<MockVariableSizeHashEntry>(backingStore, FixedSizeEntryHashBucket.getFactory(), efactory);
			ArrayList<MockVariableSizeHashEntry> entries = new ArrayList<MockVariableSizeHashEntry>();
			int seed = 1234567;
			Random key = new Random(seed);
			for (int i=0; i<1000; i++) {				
				MockVariableSizeHashEntry me = efactory.makeEntry(Math.abs(key.nextInt()));
				htable.addEntry(me);
				entries.add(me);
			}
						
			htable.flush();
			for (AbstractVariableSizeHashEntry e: entries) {
				AbstractVariableSizeHashEntry f = htable.getEntry(e.getKey());
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
			HashFile<VariableSizeHashEntry> htable = new HashFile<VariableSizeHashEntry>(backingStore, FixedSizeEntryHashBucket.getFactory(), efactory);
			ArrayList<VariableSizeHashEntry> entries = new ArrayList<VariableSizeHashEntry>();

			final int NUM_ENTRIES_INCREMENT = 400;
			int lastKey = 1;
			for (int i=0; i < 20; ++i) {
				try {
					int numEntries = htable.getNumRecords();
					assertEquals("wrong number of entries", i*NUM_ENTRIES_INCREMENT, numEntries);
					lastKey = addEntries(htable, entries, NUM_ENTRIES_INCREMENT, lastKey, true);
				} catch (DatabaseException e) {
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
			HashFile<VariableSizeHashEntry> htable = new HashFile<VariableSizeHashEntry>(backingStore, FixedSizeEntryHashBucket.getFactory(), efactory);
			ArrayList<VariableSizeHashEntry> entries = new ArrayList<VariableSizeHashEntry>();
			htable.setSize(NUM_ENTRIES*2);
			
			addEntries(htable, entries, NUM_ENTRIES, 0, true);
			
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
		if (null == efactory) {
			efactory = new MockVariableSizeEntryFactory(28);
		}
		if (null == testFileObject) {
			testFileObject = Util.makeTestFileObject("hashFile");
		}
		testFileObject.delete();
		backingStore = new RandomAccessFile(testFileObject, "rw");
	}

	@Override
	protected void tearDown() throws Exception {
		if (null != testFileObject) {
			testFileObject.delete();
		}
	}

}
