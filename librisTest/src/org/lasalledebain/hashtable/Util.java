package org.lasalledebain.hashtable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;

import org.lasalledebain.Utilities;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.hashfile.HashBucket;

public class Util extends TestCase {

	/**
	 * @param buck
	 * @param entries
	 */
	static void checkBucket(HashBucket<MockHashEntry> buck,
			ArrayList<MockHashEntry> entries) {
		Iterator<MockHashEntry> ti = entries.iterator();
		int entryCount = 0;
		for (MockHashEntry e: buck) {
			assertTrue("too many entries in the bucket", ti.hasNext());
			MockHashEntry t = ti.next();
			assertTrue("mismatch in hash entries", t.compare(e));
			++entryCount;
		}
		assertFalse("too few entries in the bucket.  Expected "+entries.size()+" got "+entryCount, ti.hasNext());
	}

	/**
	 * @param buck
	 * @param initialData
	 * @return
	 * @throws DatabaseException 
	 */
	static ArrayList<MockHashEntry> fillBucket(HashBucket<MockHashEntry> buck, byte initialData) throws DatabaseException {
		int bucketSize = HashBucket.getBucketSize();
		ArrayList<MockHashEntry> entries;
		int entryCount = 0;
		int entryLength = 10; 
		MockHashEntry newEntry = null;
		entries = new ArrayList<MockHashEntry>();
		
		do {
			if (null != newEntry) {
				entries.add(newEntry);
			}
			int occupancy = buck.getOccupancy();
			newEntry = new MockHashEntry(entryCount+1, entryLength, initialData);
			/* all buckets have at least 4 bytes */
			assertEquals("wrong value for occupancy for key "+entryCount, 
					entryCount*newEntry.getTotalLength()+4, occupancy);
			++entryCount;
			++initialData;
			boolean expectedOccupancy = entryCount <= ((bucketSize/newEntry.getTotalLength()) + 1);
			assertTrue("bucket overfilled: "+entryCount, expectedOccupancy);
		} while (buck.addElement(newEntry));
		return entries;
	}

	/**
	 * @param fileName 
	 * 
	 */
	static File makeTestFileObject(String fileName) {
		File workingDirectory = new File(System.getProperty("java.io.tmpdir"), fileName);
		Utilities.deleteRecursively(workingDirectory);
		workingDirectory.mkdirs();
		File tf = new File(workingDirectory, "testIndexFile");
		tf.deleteOnExit();
		return tf;
	}

	static RandomAccessFile MakeHashFile(File tf) {
		try {
			RandomAccessFile f = new RandomAccessFile(tf, "rw");
			tf.deleteOnExit();
			return f;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail("cannot create file "+tf.getAbsolutePath());
			return null;
		}
	}

}
