package org.lasalledebain.hashtable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.hashfile.FixedSizeEntryHashBucket;
import org.lasalledebain.libris.hashfile.FixedSizeHashEntry;
import org.lasalledebain.libris.hashfile.HashBucket;
import org.lasalledebain.libris.hashfile.HashFile;
import org.lasalledebain.libris.hashfile.VariableSizeEntryHashBucket;
import org.lasalledebain.libris.hashfile.VariableSizeHashEntry;
import org.lasalledebain.libris.index.AffiliateListEntry;
import org.lasalledebain.libris.index.AffiliateListEntry.AffiliateListEntryFactory;
import org.lasalledebain.libris.indexes.FileSpaceManager;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import static org.lasalledebain.Utilities.compareIntLists;
import static org.lasalledebain.Utilities.checkForDuplicates;

public class HashFileTest extends TestCase {

	private File testFileObject;
	private MockVariableSizeEntryFactory vFactory = null;
	private MockFixedSizeEntryFactory fFactory = null;
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
				LibrisDatabase.log(Level.INFO, currentKey+" entries added.  Checking...\n");
				for (VariableSizeHashEntry e: entries) {
					int key = e.getKey();
					VariableSizeHashEntry f = htable.getEntry(key);
					if (null == f) {
						LibrisDatabase.log(Level.INFO, "key="+key+" not found; ");
						printHashBuckets(key);
						LibrisDatabase.log(Level.INFO, "\n");
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

	@Test
	public void testExpand() {
		int searchKey=0;
		try {
			HashFile<VariableSizeHashEntry> htable = makeVHashTable();
			ArrayList<VariableSizeHashEntry> entries = new ArrayList<VariableSizeHashEntry>();

			LibrisDatabase.log(Level.INFO, "add first batch of entries\n");
			int lastKey = addVariableSizeEntries(htable, entries, 400, 10, true);
			LibrisDatabase.log(Level.INFO, "Expand hash file\n");
			htable.resize(1000);

			LibrisDatabase.log(Level.INFO, "Check file\n");
			for (VariableSizeHashEntry e: entries) {
				searchKey = e.getKey();
				VariableSizeHashEntry f = htable.getEntry(searchKey);
				assertNotNull("Coud not find entry "+e.getKey(), f);
			}
			LibrisDatabase.log(Level.INFO, "add second batch of entries\n");
			addVariableSizeEntries(htable, entries, 400, lastKey, true);
			LibrisDatabase.log(Level.INFO, "Check file again\n");
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
			HashFile<FixedSizeHashEntry> htable = new HashFile<FixedSizeHashEntry>(backingStore, FixedSizeEntryHashBucket.getFactory(), fFactory);
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

	public void testAffiliates() {
		try {
			FileSpaceManager mgr = Utilities.makeFileSpaceManager(getName()+"_mgr");
			MockOverflowManager oversizeEntryManager = new MockOverflowManager(mgr);
			AffiliateListEntryFactory aFact = new AffiliateListEntryFactory();
			HashFile<AffiliateListEntry> htable = new HashFile<AffiliateListEntry>(backingStore, 
					VariableSizeEntryHashBucket.getFactory(oversizeEntryManager), aFact);

			try {
				final int numEntries = 2000;
				final int affiliateScale = 50;
				Random r = new Random(314126535);
				HashMap<Integer, int[][]> entries= new HashMap<Integer, int[][]>(numEntries);
				for (int key = 1; key < numEntries; key++ ) {
					int childrenAndAffiliates[][] = new int[2][];
					AffiliateListEntry newEntry = aFact.makeEntry(key);
					double ng = r.nextGaussian();
					int numChildren = Math.abs((int) (ng * affiliateScale));
					HashSet<Integer> buffer = new HashSet<Integer>(numChildren);
					for (int i = 0; i < numChildren; ++i) {
						int newChild = r.nextInt(numEntries) + 1;
						buffer.add(newChild);
						newEntry = new AffiliateListEntry(newEntry, newChild, true);
					}
					childrenAndAffiliates[0] = new int[buffer.size()];
					int index = 0;
					for (int c: buffer) {
						childrenAndAffiliates[0][index++] = c;
					}
					Arrays.sort(childrenAndAffiliates[0]);
					buffer.clear();

					ng = r.nextGaussian();
					int numAffiliates = Math.abs((int) (ng * affiliateScale));
					for (int i = 0; i < numAffiliates; ++i) {
						int newAffiliate = r.nextInt(numEntries) + 1;
						buffer.add(newAffiliate);
						newEntry = new AffiliateListEntry(newEntry, newAffiliate, false);
					}
					childrenAndAffiliates[1] = new int[buffer.size()];
					index = 0;
					for (int c: buffer) {
						childrenAndAffiliates[1][index++] = c;
					}					
					Arrays.sort(childrenAndAffiliates[1]);
					entries.put(key, childrenAndAffiliates);
					htable.addEntry(newEntry);
				}
				for (int key = 1; key < numEntries; key++ ) {
					AffiliateListEntry e = htable.getEntry(key);
					int[][] expectedData = entries.get(key);
					assertTrue("Duplicate children", checkForDuplicates("children", e.getChildren()));
					assertTrue("Wrong children", compareIntLists("children", expectedData[0], e.getChildren()));
					assertTrue("Duplicate affiliates", checkForDuplicates("affiliates", e.getAffiliates()));
					assertTrue("Wrong affiliates", compareIntLists("affiliates", expectedData[1], e.getAffiliates()));
				}

			} catch (DatabaseException e) {
				e.printStackTrace();
				fail("unexpected exception:"+e.getMessage());
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile:"+e);
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
			LibrisDatabase.log(Level.INFO,  "resize hash table: "+requestedBuckets+" buckets");
			htable.resize(requestedBuckets);
			LibrisDatabase.log(Level.INFO,  "add entries");

			addVariableSizeEntries(htable, entries, NUM_ENTRIES, 0, true);
			LibrisDatabase.log(Level.INFO, "check entries");

			for (VariableSizeHashEntry e: entries) {
				VariableSizeHashEntry f = htable.getEntry(e.getKey());
				assertNotNull("Coud not find entry", f);
			}
			LibrisDatabase.log(Level.INFO, "checkED entries");
		} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
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

		LibrisDatabase.log(Level.INFO, "Add "+numEntries);
		for (int i=0; i<numEntries; i++) {
			MockVariableSizeHashEntry e = vFactory.makeEntry(countUp? (keyBase+i):(keyBase+numEntries-i));
			htable.addEntry(e);
			entries.add(e);
		}
		htable.flush();
		LibrisDatabase.log(Level.INFO, numEntries+" added");

		return keyBase+numEntries;
	}

	private int addFixedSizeEntries(HashFile<FixedSizeHashEntry> htable,
			ArrayList<FixedSizeHashEntry> entries, int numEntries, int keyBase, boolean countUp)
					throws IOException, DatabaseException {

		LibrisDatabase.log(Level.INFO, "Add "+numEntries);
		for (int i=0; i<numEntries; i++) {
			MockFixedSizeHashEntry e = fFactory.makeEntry(countUp? (keyBase+i):(keyBase+numEntries-i));
			htable.addEntry(e);
			entries.add(e);
		}
		htable.flush();
		LibrisDatabase.log(Level.INFO, numEntries+" added");

		return keyBase+numEntries;
	}

	private HashFile<VariableSizeHashEntry> makeVHashTable() throws IOException {
		FileSpaceManager mgr = Utilities.makeFileSpaceManager(getName()+"_mgr");
		MockOverflowManager oversizeEntryManager = new MockOverflowManager(mgr);
		HashFile<VariableSizeHashEntry> htable = 
				new HashFile<VariableSizeHashEntry>(backingStore, 
						VariableSizeEntryHashBucket.getFactory(oversizeEntryManager), vFactory);
		return htable;
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
			LibrisDatabase.log(Level.INFO, "modulus="+i+" bucket="+homeBucket+"; ");
		}
	}

	protected void setUp() throws Exception {
		LibrisDatabase.log(Level.INFO, "\nStarting "+getName());
		if (null == vFactory) {
			vFactory = new MockVariableSizeEntryFactory(28);
		}
		if (null == fFactory) {
			fFactory = new MockFixedSizeEntryFactory(28);
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
