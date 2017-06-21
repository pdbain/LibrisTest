package org.lasalledebain.hashtable;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.hashfile.HashBucket;
import org.lasalledebain.libris.hashfile.HashBucketFactory;
import org.lasalledebain.libris.hashfile.VariableSizeEntryHashBucket;
import org.lasalledebain.libris.hashfile.VariableSizeHashEntry;
import org.lasalledebain.libris.indexes.FileSpaceManager;

public class VariableSizeEntryHashBucketTest extends TestCase{

	private File testFile;
	private HashBucketFactory<?> bfact;
	private MockVariableSizeEntryFactory entryFactory;
	private HashBucket<VariableSizeHashEntry> buck;
	private RandomAccessFile backingStore;
	boolean ignoreUnimplemented = Boolean.getBoolean("org.lasalledebain.libris.test.IgnoreUnimplementedTests");
	FileSpaceManager mgr;
	private MockOverflowManager oversizeEntryManager;

	@Test
	public void testAddElement() {
		try {
			final int numEntries = 16;
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);
			populateBucket(numEntries, entries);
			checkEntries(buck, numEntries, entries);

		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}

	@Test
	public void testOverflow() {
		try {
			final int numEntries = 128;
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);
			int entryCount = populateBucket(numEntries, entries);
			checkEntries(buck, entryCount, entries);

		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}

	@Test
	public void testVariableSize() {
		try {
			final int numEntries = 128;
			int entryCount = 0;
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);
			int expectedOccupancy = 2;
			for (int key = 1; key < numEntries; key++ ) {
				int length = entryCount + 1;
				VariableSizeHashEntry newEntry = entryFactory.makeVariableSizeEntry(key, length);
				entries.put(new Integer(key), newEntry);
				boolean result = buck.addElement(newEntry);
				++entryCount;
				expectedOccupancy += 4 + 2 + length;
				if (expectedOccupancy <= HashBucket.getBucketSize()) {
					assertTrue("bucket add failed on key "+key, result);
				} else {
					assertFalse("overflow not detected on key "+key, result);
					break;
				}
			}
			checkEntries(buck, entryCount - 1, entries);

		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}

	@Test
	public void testExactFill() {
		try {
			final int numEntries = 128;
			int entryCount = 0;
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);
			int expectedOccupancy = 2;
			for (int key = 1; key < numEntries; key++ ) {
				int length = Math.min(32, HashBucket.getBucketSize()-(expectedOccupancy + 6));
				VariableSizeHashEntry newEntry = entryFactory.makeVariableSizeEntry(key, length);
				entries.put(new Integer(key), newEntry);
				boolean result = buck.addElement(newEntry);
				++entryCount;
				expectedOccupancy += 4 + 2 + length;
				if (expectedOccupancy >= HashBucket.getBucketSize()) {
					break;
				}
				assertTrue("bucket add failed on key "+key, result);
				assertEquals("Wrong occupancy on key "+key+": ", expectedOccupancy, buck.getOccupancy());
			}
			assertEquals("bucket not exactly filled.  Occupancy: ", expectedOccupancy, HashBucket.getBucketSize());
			checkEntries(buck, entryCount, entries);

		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}

	@Test
	public void testWriteRead() {
		try {
			final int numEntries = 16;
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);
			populateBucket(numEntries, entries);
			buck.write();
			checkEntries(buck, numEntries, entries);
			HashBucket<VariableSizeHashEntry> newBuck = (HashBucket<VariableSizeHashEntry>) bfact.createBucket(backingStore, 0, entryFactory);
			newBuck.read();
			checkEntries(newBuck, numEntries, entries);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}

	@Test
	public void testOversize() {
		try {
			final int numEntries = 16;
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);
			
			entryFactory.setOversizeThreshold(64);
			int length = 2;
			for (int key = 1; key <= numEntries; key++ ) {
				VariableSizeHashEntry newEntry = createEntry(key, length);
				entries.put(key, newEntry);
				boolean result = buck.addElement(newEntry);
				int occupancy = buck.getOccupancy();
				assertTrue("bucket add failed on key "+key, result);
				length *= 2;
			}			
			buck.write();
			checkEntries(buck, numEntries, entries);
			HashBucket<VariableSizeHashEntry> newBuck = (HashBucket<VariableSizeHashEntry>) bfact.createBucket(backingStore, 0, entryFactory);
			newBuck.read();
			checkEntries(newBuck, numEntries, entries);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}
	
	@Test
	public void testRandomSizes() {
		try {
			int numEntries = 0;
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);
			
			entryFactory.setOversizeThreshold(64);
			Random lengthGen = new Random(271828);
			boolean addMore = true;
			for (int key = 1; addMore; key++ ) {
				int range = lengthGen.nextInt(10) + 1;
				int length = Math.abs(lengthGen.nextInt() % (1 << range));
				VariableSizeHashEntry newEntry = createEntry(key, length);
				addMore = buck.addElement(newEntry);
				int occupancy = buck.getOccupancy();
				if (addMore) {
					entries.put(key, newEntry);
					++numEntries;
				}
			}			
			buck.write();
			checkEntries(buck, numEntries, entries);
			HashBucket<VariableSizeHashEntry> newBuck = (HashBucket<VariableSizeHashEntry>) bfact.createBucket(backingStore, 0, entryFactory);
			newBuck.read();
			checkEntries(newBuck, numEntries, entries);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}
	
	@Test
	public void testIterator() {
		try {
			final int numEntries = 16;
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);
			populateBucket(numEntries, entries);
			buck.write();
			checkEntries(buck, numEntries, entries);
			HashBucket<VariableSizeHashEntry> newBuck = (HashBucket<VariableSizeHashEntry>) bfact.createBucket(backingStore, 0, entryFactory);
			newBuck.read();
			int actualCount = 0;
			Iterator<VariableSizeHashEntry> iter = newBuck.iterator();
			while (iter.hasNext()) {
				VariableSizeHashEntry e = iter.next();
				assertNotNull("missing entry after "+actualCount+" entries", e);
				int key = e.getKey();
				VariableSizeHashEntry comparand = entries.get(new Integer(key));
				assertNotNull("key "+key+" missing", comparand);
				assertEquals(e, comparand);
				iter.remove();
				++actualCount ;
			}
			assertEquals("Wrong number of entries", numEntries, actualCount);
			iter = newBuck.iterator();
			assertFalse("remove didn't work", iter.hasNext());
			newBuck.write();
			newBuck = (HashBucket<VariableSizeHashEntry>) bfact.createBucket(backingStore, 0, entryFactory);
			newBuck.read();
			iter = newBuck.iterator();
			assertFalse("remove didn't work", iter.hasNext());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}

	@Test
	public void testRemoveEntries() {
		try {
			final int numEntries = 8;
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);
			
			entryFactory.setOversizeThreshold(64);
			int length = 4;
			for (int key = 1; key <= numEntries; key++ ) {
				VariableSizeHashEntry newEntry = createEntry(key, length);
				entries.put(key, newEntry);
				boolean result = buck.addElement(newEntry);
				int occupancy = buck.getOccupancy();
				assertTrue("bucket add failed on key "+key, result);
				length *= 2;
			}			
			buck.write();
			countOversize(4);
			checkEntries(buck, numEntries, entries);
			HashBucket<VariableSizeHashEntry> newBuck = (HashBucket<VariableSizeHashEntry>) bfact.createBucket(backingStore, 0, entryFactory);
			newBuck.read();
			Iterator<VariableSizeHashEntry> iter = newBuck.iterator();
			while (iter.hasNext()) { /* remove half the entries */
				VariableSizeHashEntry e = iter.next();
				iter.remove();
				iter.next();
			}
			newBuck.write();
			newBuck = (HashBucket<VariableSizeHashEntry>) bfact.createBucket(backingStore, 0, entryFactory);
			newBuck.read();
			countOversize(2);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}

	private void countOversize(int expectedCount) {
		Iterator<Long> oversizeIter;
		int oversizeEntryCount;
		oversizeIter = oversizeEntryManager.iterator();
		oversizeEntryCount = 0;
		while (oversizeIter.hasNext()) {
			++oversizeEntryCount;
			oversizeIter.next();
		}
		assertEquals("wrong number of oversize entries", expectedCount, oversizeEntryCount);
	}

	@Test
	public void testModifyEntries() {

		try {
			final int numEntries = 16;
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);
			populateBucket(numEntries, entries);
			checkEntries(buck, numEntries, entries);
			buck.write();
			{
				HashBucket<VariableSizeHashEntry> tempBuck = (HashBucket<VariableSizeHashEntry>) bfact.createBucket(backingStore, 0, entryFactory);
				tempBuck.read();
				checkEntries(tempBuck, numEntries, entries);
				VariableSizeHashEntry e = tempBuck.findEntry(3);
				byte[] dat = e.getData();
				dat[0] = (byte) 0x55;
				entries.put(3, e);
				e = tempBuck.findEntry(13);
				dat = e.getData();
				dat[0] = (byte) 0xaa;
				entries.put(13, e);
				tempBuck.setDirty(true);
				tempBuck.write();
				tempBuck.read();
				checkEntries(tempBuck, numEntries, entries);
			}		

		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}
	
	@Test
	public void testResizeEntries() {
		try {
			final int numEntries = 16;
			entryFactory.setOversizeThreshold(64);
			HashMap<Integer, VariableSizeHashEntry> entries= new HashMap<Integer, VariableSizeHashEntry>(numEntries);

			{
				HashBucket<VariableSizeHashEntry> tempBuck = (HashBucket<VariableSizeHashEntry>) bfact.createBucket(backingStore, 0, entryFactory);


				int length = 2;
				for (int key = 1; key <= numEntries; key++ ) {
					VariableSizeHashEntry newEntry = createEntry(key, length);
					entries.put(key, newEntry);
					boolean result = tempBuck.addElement(newEntry);
					int occupancy = tempBuck.getOccupancy();
					assertTrue("bucket add failed on key "+key, result);
					length *= 2;
				}			
				tempBuck.write();
			}
			countOversize(numEntries - 5);
			
			{
				HashBucket<VariableSizeHashEntry> tempBuck = (HashBucket<VariableSizeHashEntry>) bfact.createBucket(backingStore, 0, entryFactory);
				tempBuck.read();
				checkEntries(tempBuck, numEntries, entries);

				int length = 2 << numEntries;
				for (int key = 1; key <= numEntries; key++ ) {
					VariableSizeHashEntry oldEntry = entries.get(key);
					byte dat[] = new byte[length];
					for (int i = 0; i < length; ++i) {
						dat[i] = (byte) (key ^ i);
					}
					oldEntry.setData(dat);
					oldEntry.setOversize((length > 128) || (key %2) == 1);
					entries.put(key, oldEntry);
					boolean result = tempBuck.addElement(oldEntry);
					int occupancy = tempBuck.getOccupancy();
					assertTrue("bucket add failed on key "+key, result);
					length /= 2;
				}			
				tempBuck.write();
			}
			HashBucket<VariableSizeHashEntry> newBuck = (HashBucket<VariableSizeHashEntry>) bfact.createBucket(backingStore, 0, entryFactory);
			newBuck.read();
			checkEntries(newBuck, numEntries, entries);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception:"+e.getMessage());
		}
	}
	
	@Before
	public void setUp() throws Exception {
		if (null == testFile) {
			testFile = Util.makeTestFileObject("variableHashFile");
		}
		backingStore = Util.MakeHashFile(testFile);
		mgr = Utilities.makeFileSpaceManager(getName()+"_mgr");
		System.out.println("oversize entries file: "+mgr.getFilePath().getAbsolutePath());
		oversizeEntryManager = new MockOverflowManager(mgr);
		bfact = VariableSizeEntryHashBucket.getFactory(oversizeEntryManager);
		entryFactory = new MockVariableSizeEntryFactory();
		buck = (HashBucket<VariableSizeHashEntry>) bfact.createBucket(backingStore, 0, entryFactory);
	}

	@Override
	protected void tearDown() throws Exception {
		if (null != backingStore) {
			backingStore.close();
		}
		Utilities.destroyFileSpaceManager(mgr);
	}

	private int populateBucket(final int numEntries,
			HashMap<Integer, VariableSizeHashEntry> entries)
			throws DatabaseException {
		int entryCount = 0;
		int expectedOccupancy = 2;
		for (int key = 1; key <= numEntries; key++ ) {
			int length = 32;
			VariableSizeHashEntry newEntry = createEntry(key, length);
			entries.put(key, newEntry);
			boolean result = buck.addElement(newEntry);
			expectedOccupancy += 4 + 2 + length;
			if (expectedOccupancy <= HashBucket.getBucketSize()) {
				++entryCount;
				assertTrue("bucket add failed on key "+key, result);
			} else {
				assertFalse("overflow not detected on key "+key, result);
				break;
			}
		}
		return entryCount;
	}

	private VariableSizeHashEntry createEntry(int key, int length) {
		VariableSizeHashEntry newEntry = entryFactory.makeVariableSizeEntry(key, length);
		byte[] eData = newEntry.getData();
		byte cdata = (byte) Character.getNumericValue('a');
		int limit = Character.getNumericValue('z') + 1;
		for (int i = 0; i < length/2; ++i) {
			eData[2*i] = cdata;
			eData[2*i + 1] = (byte) key;
			cdata = (byte) ((cdata + 1) % limit);
		}
		return newEntry;
	}

	private void checkEntries(HashBucket<VariableSizeHashEntry> testBucket, int expectedCount, HashMap<Integer, VariableSizeHashEntry> entries) {
		int actualCount = 0;
		for (VariableSizeHashEntry e: testBucket) {
			assertNotNull("missing entry after "+actualCount+" entries", e);
			int key = e.getKey();
			VariableSizeHashEntry comparand = entries.get(new Integer(key));
			assertNotNull("key "+key+" missing", comparand);
			assertEquals(comparand, e);
			++actualCount ;
		}
		assertEquals("Wrong number of entries", expectedCount, actualCount);
	}

}
