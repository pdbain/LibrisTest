package org.lasalledebain.hashtable;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Random;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.junit.Test;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.hashfile.FixedSizeEntryHashBucket;
import org.lasalledebain.libris.hashfile.HashBucketFactory;
import org.lasalledebain.libris.hashfile.HashFile;
import org.lasalledebain.libris.hashfile.VariableSizeEntryHashBucket;
import org.lasalledebain.libris.hashfile.VariableSizeHashEntry;
import org.lasalledebain.libris.index.AbstractHashEntry;
import org.lasalledebain.libris.index.AbstractVariableSizeHashEntry;
import org.lasalledebain.libris.indexes.FileSpaceManager;

public class VariableSizeHashFileTests extends TestCase {
	private File testFileObject;
	private MockVariableSizeEntryFactory efactory = null;

	@Test
	public void testAddAndGet() {
		println("start "+getName());
		try {
			HashFile<MockVariableSizeHashEntry> htable = new HashFile<MockVariableSizeHashEntry>(backingStore, 
					getFactory(), efactory);
			ArrayList<MockVariableSizeHashEntry> entries = new ArrayList<MockVariableSizeHashEntry>();

			addEntries(htable, entries, 32, 0, true);

			for (AbstractVariableSizeHashEntry e: entries) {
				AbstractHashEntry f = htable.getEntry(e.getKey());
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
		println("end "+getName());

	}

	@Test
	public void testOverflow() {
		println("start "+getName());
		try {
			HashFile<MockVariableSizeHashEntry> htable = new HashFile<MockVariableSizeHashEntry>(backingStore, getFactory(), efactory);
			ArrayList<MockVariableSizeHashEntry> entries = new ArrayList<MockVariableSizeHashEntry>();

			int currentKey = 1;
			while (currentKey < 1000) {
				currentKey = addEntries(htable, entries, 127, currentKey, true);
				print(currentKey+" entries added.  Checking...\n");
				for (AbstractVariableSizeHashEntry e: entries) {
					int key = e.getKey();
					AbstractHashEntry f = htable.getEntry(key);
					if (null == f) {
						print("key="+key+" not found; ");
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
		println("end "+getName());
	}

	@Test
	public void testVariableSizedEntries() {
		println("start "+getName());
		try {
			HashFile<VariableSizeHashEntry> htable = new HashFile<VariableSizeHashEntry>(backingStore, getFactory(), efactory);
			ArrayList<VariableSizeHashEntry> entries = new ArrayList<VariableSizeHashEntry>();

			int currentKey = 1;
			while (currentKey < 1000) {
				currentKey = addVariableSizeEntries(htable, entries, 127, currentKey, 1, 1024);
				print(currentKey+" entries added.  Checking...\n");
				for (VariableSizeHashEntry e: entries) {
					int key = e.getKey();
					VariableSizeHashEntry f = htable.getEntry(key);
					if (null == f) {
						print("key="+key+" not found; ");
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
		println("end "+getName());
	}

	@Test
	public void testFlush() {
		println("start "+getName());
		try {
	//s		efactory = new MockVariableSizeEntryFactory(4096);
			HashFile<VariableSizeHashEntry> htable = new HashFile<VariableSizeHashEntry>(backingStore, getFactory(), efactory);
			ArrayList<VariableSizeHashEntry> entries = new ArrayList<VariableSizeHashEntry>();

			int currentKey = 1;

			currentKey = addVariableSizeEntries(htable, entries, 20, currentKey, 1, 1024);
			htable.flush();
			HashFile<VariableSizeHashEntry> htable2 = new HashFile<VariableSizeHashEntry>(backingStore, getFactory(), efactory);
			print(currentKey+" entries added.  Checking...\n");
			for (VariableSizeHashEntry e: entries) {
				int key = e.getKey();
				VariableSizeHashEntry f = htable2.getEntry(key);
				if (null == f) {
					print("key="+key+" not found; ");
					print("\n");
				}
				try {
					assertNotNull("Could not find entry "+key, f);
					assertEquals("Entry mismatch", e, f);
				} catch (AssertionFailedError a) {
					throw a;
				}
			}
				} catch (IOException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("Unexpected exception on hashfile");
		}
		println("end "+getName());
	}

	@Test
	public void testReplace() {
		try {
			HashFile<VariableSizeHashEntry> htable = new HashFile<VariableSizeHashEntry>(backingStore, getFactory(), efactory);
			ArrayList<VariableSizeHashEntry> entries = new ArrayList<VariableSizeHashEntry>();

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

	private int addVariableSizeEntries(HashFile<VariableSizeHashEntry> htable,
			ArrayList<VariableSizeHashEntry> entries, int numEntries, int keyBase, int minimum,
			int maximum) throws DatabaseException, IOException {
		int modulus = Math.max(1, numEntries/64);
		Random r = new Random(keyBase);
		for (int i=0; i<numEntries; i++) {
			int length = r.nextInt(maximum - minimum) + minimum;
			VariableSizeHashEntry e = efactory.makeVariableSizeEntry(keyBase+i, length);
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

	private RandomAccessFile backingStore;
	private FileSpaceManager mgr;

	private int addEntries(HashFile<MockVariableSizeHashEntry> htable,
			ArrayList<MockVariableSizeHashEntry> entries, int numEntries, int keyBase, boolean countUp)
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

	protected void setUp() throws Exception {
		if (null == efactory) {
			efactory = new MockVariableSizeEntryFactory(28);
		}
		if (null == testFileObject) {
			testFileObject = Util.makeTestFileObject("hashFile");
		}
		if (!testFileObject.exists()) {
			backingStore = new RandomAccessFile(testFileObject, "rw");
		}
		mgr = Utilities.makeFileSpaceManager(getName()+"_mgr");
	}

	@Override
	protected void tearDown() throws Exception {
		if (null != testFileObject) {
			testFileObject.delete();
		}
		Utilities.destroyFileSpaceManager(mgr);
	}

	private HashBucketFactory getFactory() {
		return VariableSizeEntryHashBucket.getFactory(new MockOverflowManager(mgr));
	}

	private void print(String msg) {
		System.out.print(msg);
	}
	private void println(String msg) {
		System.out.println(msg);
	}
}
