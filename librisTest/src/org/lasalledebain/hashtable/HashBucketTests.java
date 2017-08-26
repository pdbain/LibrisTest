package org.lasalledebain.hashtable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Test;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.hashfile.FixedSizeEntryHashBucket;
import org.lasalledebain.libris.hashfile.HashBucket;
import org.lasalledebain.libris.hashfile.HashBucketFactory;
import org.lasalledebain.libris.hashfile.HashEntry;
import org.lasalledebain.libris.hashfile.VariableSizeEntryHashBucket;
import org.lasalledebain.libris.indexes.FileSpaceManager;

public class HashBucketTests extends TestCase {

	private File testFile = null;

	@Test
	public void testAddEntry() {
		HashBucketFactory bfact = FixedSizeEntryHashBucket.getFactory();
		HashBucket buck = bfact.createBucket(null, 0, null);
		ArrayList<HashEntry> entries;
		try {
			entries = Util.fixedSizeFillBucket(buck, 42,(byte) 1);
			Util.checkBucket(buck, entries);
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
		
	}

	@Test
	public void testReadAndWrite() {
		HashBucketFactory bfact = FixedSizeEntryHashBucket.getFactory();
		MockFixedSizeEntryFactory fact = new MockFixedSizeEntryFactory(10);
		RandomAccessFile hashFile = Util.MakeHashFile(testFile);
		HashBucket writeBucket = bfact.createBucket(hashFile,0,fact);
		ArrayList<HashEntry> entries = null;
		try {
			entries = Util.fixedSizeFillBucket(writeBucket, 10, (byte) 2);
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
		Util.checkBucket((HashBucket<HashEntry>) readBucket, entries);
			hashFile.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException closing hashFile: "+e);
		}
		testFile.delete();
	}

	@Test
	public void testVariableSizeReadAndWrite() {
		FileSpaceManager mgr = Utilities.makeFileSpaceManager(getName()+"_mgr");
		MockOverflowManager oversizeEntryManager = new MockOverflowManager(mgr);

		HashBucketFactory bfact = VariableSizeEntryHashBucket.getFactory(oversizeEntryManager);
		MockVariableSizeEntryFactory fact = new MockVariableSizeEntryFactory(10);
		RandomAccessFile hashFile = Util.MakeHashFile(testFile);
		HashBucket writeBucket = bfact.createBucket(hashFile,0,fact);
		ArrayList<HashEntry> entries = null;
		try {
			entries = Util.variableSizeFillBucket(writeBucket, (byte) 2);
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
		Util.checkBucket((HashBucket<HashEntry>) readBucket, entries);
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
	}

}
