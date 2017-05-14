package org.lasalledebain.hashtable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Test;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.hashfile.FixedSizeEntryHashBucket;
import org.lasalledebain.libris.hashfile.HashBucket;
import org.lasalledebain.libris.hashfile.HashBucketFactory;

public class HashBucketTests extends TestCase {

	private File testFile = null;
	private HashBucketFactory bfact;

	@Test
	public void testAddEntry() {
		HashBucket buck = bfact.createBucket(null, 0, null);
		ArrayList<MockHashEntry> entries;
		try {
			entries = Util.fillBucket(buck, (byte) 1);
			Util.checkBucket(buck, entries);
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
		
	}

	@Test
	public void testReadAndWrite() {
		MockEntryFactory fact = new MockEntryFactory(10);
		RandomAccessFile hashFile = Util.MakeHashFile(testFile);
		HashBucket writeBucket = bfact.createBucket(hashFile,0,fact);
		ArrayList<MockHashEntry> entries = null;
		try {
			entries = Util.fillBucket(writeBucket, (byte) 2);
			writeBucket.write();
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
		
		HashBucket<?> readBucket = bfact.createBucket(hashFile,0,fact);
		try {
			readBucket.read();
		} catch (Exception e1) {
			e1.printStackTrace();
			fail("Unexpected exception in read hash bucket: "+e1.getMessage());
		}
		
		try {
		Util.checkBucket((HashBucket<MockHashEntry>) readBucket, entries);
			hashFile.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException closing hashFile: "+e);
		}
		testFile.delete();
	}

	@Override
	protected void setUp() throws Exception {
		if (null == testFile) {
			testFile = Util.makeTestFileObject("TestFileRecordMap");
		}
		bfact = FixedSizeEntryHashBucket.getFactory();
	}

}
