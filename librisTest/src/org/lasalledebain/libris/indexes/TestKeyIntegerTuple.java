package org.lasalledebain.libris.indexes;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import junit.framework.TestCase;

import org.lasalledebain.Utilities;
import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.exception.InputException;

public class TestKeyIntegerTuple extends TestCase {
	FileAccessManager dataManager;
	private File workDir;
	private RandomAccessFile dataStream;
	private DataInput dataIp;
	
	protected void setUp() {
		workDir = Utilities.getTempTestDirectory();
		if (null == workDir) {
			fail("could not create working directory ");
		}
		dataManager = new FileAccessManager(workDir, "data");
		try {
			dataStream = dataManager.getReadWriteRandomAccessFile();
			dataIp = new DataInputStream(dataManager.getIpStream());
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		}
		if (null == dataManager) {
			fail("could not create dataManager");
		}
	}
	
	public void testNewTuple() {
		try {
		KeyValueTuple tup1 = new KeyIntegerTuple("foo", 42);
		assertEquals("wrong key length", 3+2+4, tup1.entrySize());
		KeyValueTuple tup2 = new KeyIntegerTuple(Utilities.ALPHANUMS, 123456789);
		assertEquals("wrong key length", 26+26+10+2+4, tup2.entrySize());
		KeyValueTuple tup3 = new KeyIntegerTuple("\u00ff\u0fff\u12345", 99);
		assertEquals("wrong key length", 9+2+4, tup3.entrySize());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception "+e.getMessage());
		}
	}

	public void testAddTuples() {
		try {
			KeyValueTuple tup1 = new KeyIntegerTuple("foo", 42);
			KeyValueTuple tup2 = new KeyIntegerTuple(Utilities.ALPHANUMS, 123456789);
			KeyValueTuple tup3 = new KeyIntegerTuple("\u00ff\u0fff\u12345", 99);
			tup1.write(dataStream);
			tup2.write(dataStream);
			tup3.write(dataStream);
			KeyIntegerTuple tup1a = new KeyIntegerTuple(dataIp);
			assertEquals("tuples do not match", tup1, tup1a);
			KeyIntegerTuple tup2a = new KeyIntegerTuple(dataIp);
			assertEquals("tuples do not match", tup2, tup2a);
			KeyIntegerTuple tup3a = new KeyIntegerTuple(dataIp);
			assertEquals("tuples do not match", tup3, tup3a);
		} catch (Exception e) {
			System.err.println(dataManager.getPath());
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testAddLongTuples() {
		boolean exceptionOccurred = false;
		try {
			StringBuilder b = new StringBuilder(300);
			while (b.length() < LibrisConstants.KEY_MAX_LENGTH) {
				b.append(Utilities.ALPHANUMS);
			}
			b.setLength(LibrisConstants.KEY_MAX_LENGTH+1);
			KeyValueTuple tup2 = new KeyIntegerTuple(b.toString(), 123456789);
		} catch (InputException e) {
			exceptionOccurred = true;
		}
		assertTrue("excessive length not detected", exceptionOccurred);
	}
	
	public void testAddInRandomOrder() {
		try {
			SortedKeyValueBucket<KeyIntegerTuple> bucket = addTuples(); 
			
			checkTuples(bucket);
		} catch (InputException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testGetTuples() {
		try {
			SortedKeyValueBucket<KeyIntegerTuple> bucket = addTuples();
			bucket.write(dataStream);
			SortedKeyValueBucket<KeyIntegerTuple> bucket2 = new SortedKeyIntegerBucket(dataIp);
			checkTuples(bucket2);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		} 
	}

	public void testGetTuplesByName() {
		try {
			SortedKeyValueBucket<KeyIntegerTuple> bucket = addTuples();
			String keys[] = {"aid", "all", "men", "to", "time"};
			for (String key:keys) {
				KeyIntegerTuple tup = bucket.getByName(key);
				assertEquals("wrong tuple", key, tup.key);
			}
			KeyIntegerTuple tup = bucket.getByName("foobar");
			assertNull("retrieved non-existent tuple", tup);

		} catch (InputException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testGetTuplesByPrefix() {
		try {
			SortedKeyValueBucket<KeyIntegerTuple> bucket = addTuples();
			checkPrefixIterator(bucket, new String[]{"the", "time", "to"}, "t");
			checkPrefixIterator(bucket, new String[]{"jumps"}, "jump");
			checkPrefixIterator(bucket, new String[]{}, "foobar");

		} catch (InputException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}


	public void testBucketOverflow() {
		int seed = 27182817;
		for (int i = 0; i < 32; i++) {
			fillAndTestBucket(seed * (i + 1));
			try {
				dataManager.close();
				dataStream = dataManager.getReadWriteRandomAccessFile();
				dataIp = new DataInputStream(dataManager.getIpStream());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				fail("unexpected exception");
			}
		}
	}

	private void fillAndTestBucket(int seed) {
		Random r = new Random(seed);
		Vector<KeyIntegerTuple> enteredTuples = new Vector<KeyIntegerTuple>(1000);
		boolean result = true;
		try {
			SortedKeyValueBucket<KeyIntegerTuple> bucket = new SortedKeyIntegerBucket();
			int i = 0;
			while (result) {
				KeyIntegerTuple tup = Utilities.makeRandomKeyIntegerTuple(r, Integer.toOctalString(i));
				if (result = bucket.addElement(tup)) {
					enteredTuples.add(tup);
				}
				++i;
			}
			bucket.write(dataStream);
			SortedKeyValueBucket<KeyIntegerTuple> bucket2 = new SortedKeyIntegerBucket(dataIp);
			KeyIntegerTuple[] sortedTuples = new KeyIntegerTuple[enteredTuples.size()];
			enteredTuples.toArray(sortedTuples);
			Arrays.sort(sortedTuples);
			Iterator<KeyIntegerTuple> expectedTuples = Arrays.asList(sortedTuples).iterator();
			for (KeyIntegerTuple tup: bucket2) {
				KeyIntegerTuple expectedTuple = expectedTuples.next();
				assertEquals(expectedTuple, tup);
			}
			assertFalse("wrong number of tuples", expectedTuples.hasNext());
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	private SortedKeyValueBucket<KeyIntegerTuple> addTuples()
			throws InputException {
		SortedKeyValueBucket<KeyIntegerTuple> bucket = new SortedKeyIntegerBucket();
		bucket.addElement(new KeyIntegerTuple("now", 12));
		bucket.addElement(new KeyIntegerTuple("is", 8)); 
		bucket.addElement(new KeyIntegerTuple("time", 18));
		bucket.addElement(new KeyIntegerTuple("all", 2)); 
		bucket.addElement(new KeyIntegerTuple("good", 7)); 
		bucket.addElement(new KeyIntegerTuple("men", 11));
		bucket.addElement(new KeyIntegerTuple("come", 4)); 
		bucket.addElement(new KeyIntegerTuple("to", 19));
		bucket.addElement(new KeyIntegerTuple("the", 17));
		bucket.addElement(new KeyIntegerTuple("aid", 1)); 
		bucket.addElement(new KeyIntegerTuple("of", 13));
		bucket.addElement(new KeyIntegerTuple("party", 15));
		bucket.addElement(new KeyIntegerTuple("quick", 16));
		bucket.addElement(new KeyIntegerTuple("brown", 3)); 
		bucket.addElement(new KeyIntegerTuple("for", 6)); 
		bucket.addElement(new KeyIntegerTuple("jumps", 9)); 
		bucket.addElement(new KeyIntegerTuple("over", 14)); 
		bucket.addElement(new KeyIntegerTuple("lazy", 10));
		bucket.addElement(new KeyIntegerTuple("dog", 5));
		return bucket;
	}

	private void checkTuples(SortedKeyValueBucket<KeyIntegerTuple> bucket) {
		Iterator<KeyIntegerTuple> iter = bucket.iterator();
		int expectedValue = 1;
		while (iter.hasNext()) {
			KeyIntegerTuple tup = iter.next();
			assertEquals("tuples in wrong order", expectedValue, tup.value);
			++expectedValue;
		}
	}

	private void checkPrefixIterator(
			SortedKeyValueBucket<KeyIntegerTuple> bucket,
			String[] expectedKeys, String prefix) throws InputException {
		Iterator<KeyIntegerTuple> iter = bucket.iterator(prefix);
		int tupleCount = 0;
		while (iter.hasNext()) {
			KeyIntegerTuple tup = iter.next();
			assertEquals("wrong tuple", expectedKeys[tupleCount], tup.key);
			++tupleCount;
		}				
		assertEquals("wrong number of tuples", expectedKeys.length, tupleCount);
	}

	protected void tearDown() {
		dataManager.close();
		Utilities.deleteRecursively(workDir);
	}
}
