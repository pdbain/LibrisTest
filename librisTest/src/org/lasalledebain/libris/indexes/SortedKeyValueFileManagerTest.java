package org.lasalledebain.libris.indexes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.exception.InputException;

public class SortedKeyValueFileManagerTest extends TestCase {
	private File workDir;
	private SortedKeyValueBucketFactory<KeyIntegerTuple> bucketFactory;
	private SortedKeyValueFileManager<KeyIntegerTuple> mgr;
	private ArrayList<FileAccessManager> fileList;
	static String multiLevelTestNames[] = {"testRandomOrderManyBuckets", "testMultipleLevels"};
	static HashSet<String> multiLevelTests = new HashSet(Arrays.asList(multiLevelTestNames));
	
	@Before
	public void setUp() {
		System.out.println("Starting "+getName());
		int indexLevels = 1;
		fileList = new ArrayList<FileAccessManager>(3);
		if (multiLevelTests.contains(getName())) {
			indexLevels = 2;
		}
		workDir = Utilities.getTempTestDirectory();
		if (null == workDir) {
			fail("could not create working directory ");
		}
		FileAccessManager dataManager = new FileAccessManager(workDir, "data");
		System.out.println(dataManager.getPath());
		if (null == dataManager) {
			fail("could not create dataManager");
		}
		fileList.add(dataManager);
		for (int i = 1; i <= indexLevels; ++i) {
			FileAccessManager indexManager = new FileAccessManager(workDir, "index_"+i);
			fileList.add(indexManager);
		}
		bucketFactory = SortedKeyIntegerBucket.getFactory();
		try {
			mgr = new SortedKeyValueFileManager<KeyIntegerTuple>(fileList, bucketFactory);
			mgr.setManagerName("data");
		} catch (InputException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	@After
	public void tearDown() throws Exception {
		mgr.close();
		for (FileAccessManager m: fileList) {
			m.close();
		}
		Utilities.deleteRecursively(workDir);
	}

	@Test
	public void testAddElement() {
		int numTuples = 16;
		try {
			ArrayList<KeyIntegerTuple> tupleList = generateSequentialTuples(numTuples);			
			enterAndCheckTuples(tupleList);
		} catch (InputException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	@Test
	public void testMultipleBuckets() {
		int numTuples = 1024;
		try {
			ArrayList<KeyIntegerTuple> tupleList = generateSequentialTuples(numTuples);			
			enterAndCheckTuples(tupleList);
		} catch (InputException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	@Test
	public void testMultipleLevels() {
		int numTuples = 100000;
		try {
			ArrayList<KeyIntegerTuple> tupleList = generateSequentialTuples(numTuples);			
			enterAndCheckTuples(tupleList);
		} catch (InputException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	@Test
	public void testReverseOrder() {
		int numTuples = -16;
		try {
			ArrayList<KeyIntegerTuple> tupleList = generateSequentialTuples(numTuples);			
			enterAndCheckTuples(tupleList);
		} catch (InputException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}


	@Test
	public void testReverseOrderMultipleBuckets() {
		int numTuples = -1024;
		try {
			ArrayList<KeyIntegerTuple> tupleList = generateSequentialTuples(numTuples);			
			enterAndCheckTuples(tupleList);
		} catch (InputException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	@Test
	public void testRandomOrder() {
		int numTuples = 1024;
		try {
			ArrayList<KeyIntegerTuple> tupleList = generateRandomTuples(numTuples);			
			enterAndCheckTuples(tupleList);
		} catch (InputException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	@Test
	public void testRandomOrderManyBuckets() {
		int numTuples = 16384;
		try {
			ArrayList<KeyIntegerTuple> tupleList = generateRandomTuples(numTuples);			
			enterAndCheckTuples(tupleList);
		} catch (InputException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	@Test
	public void testCacheOverflow() {
		int numTuples = 40000;
		try {
			int buffSize = (numTuples);
			ArrayList<KeyIntegerTuple>  tupleList1 = new ArrayList<KeyIntegerTuple>(buffSize);
			for (int i = 0; i < numTuples; i += 1) {
				KeyIntegerTuple newTuple = new KeyIntegerTuple("a"+(0 + i), i);
				tupleList1.add(newTuple);
			}
			ArrayList<KeyIntegerTuple> tupleList = tupleList1;			
			enterAndCheckTuples(tupleList);
		} catch (InputException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	@Test
	public void testGetPredecessor() {
		int numTuples = 1024;
		try {
			ArrayList<KeyIntegerTuple> tupleList = generateSequentialTuples(numTuples);			
			enterTuples(tupleList);
			KeyIntegerTuple previousTuple = null;
			for (KeyIntegerTuple queryTuple: tupleList) {
				KeyValueTuple actual = mgr.getPredecessor(queryTuple.key, false);
				if (null != previousTuple) {
					assertNotNull("Tuple "+queryTuple+" missing", actual);
					assertEquals(previousTuple, actual);
				} else {
					assertNull(actual);
				}
				previousTuple = queryTuple;
			}
		} catch (InputException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	@Test
	public void testGetSuccessor() {
		int numTuples = 1024;
		try {
			ArrayList<KeyIntegerTuple> tupleList = generateSequentialTuples(numTuples);			
			enterTuples(tupleList);
			KeyIntegerTuple previousTuple = null;
			for (KeyIntegerTuple queryTuple: tupleList) {
				if (null != previousTuple) {
					KeyValueTuple actual = mgr.getSuccessor(previousTuple.key);
					assertNotNull("Tuple "+queryTuple+" missing", actual);
					assertEquals(queryTuple, actual);
				}
				previousTuple = queryTuple;
			}
		} catch (InputException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	@Test
	public void testGetByName() {
		int numTuples = 1024;
		try {
			ArrayList<KeyIntegerTuple> tupleList = generateSequentialTuples(numTuples);			
			enterTuples(tupleList);
			for (KeyIntegerTuple expectedTuple: tupleList) {
				KeyValueTuple actual = mgr.getByName(expectedTuple.key);
				assertNotNull("Tuple "+expectedTuple+" missing", actual);
				assertEquals(expectedTuple, actual);
			}
		} catch (InputException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	@Test
	public void testPrefixIterator() {
		int numTuples = 1024;
		try {
			ArrayList<KeyIntegerTuple> tupleList = generateSequentialTuples(numTuples);	
			enterTuples(tupleList);
			String prefix = "";
			char[] prefixChars = "a10234".toCharArray();
			System.out.println("test prefix \""+prefix+"\"");
			for (char c: prefixChars) {
				ArrayList<KeyIntegerTuple> expectedTuples = new ArrayList<KeyIntegerTuple>();
				for (KeyIntegerTuple t: tupleList) {
					if (t.key.startsWith(prefix)) {
						expectedTuples.add(t);
					}
				}
				Iterator<KeyIntegerTuple> actualIterator = mgr.iterator(prefix);
				for (KeyIntegerTuple expectedTuple: expectedTuples) {
					KeyValueTuple actualTuple = actualIterator.next();
					assertEquals(expectedTuple, actualTuple);
				}
				assertFalse("not all tuples retrieved", actualIterator.hasNext());
				prefix += c;
			}
		} catch (InputException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	private ArrayList<KeyIntegerTuple> generateSequentialTuples(int numTuples) throws InputException {
		int buffSize = Math.abs(numTuples);
		int start = (numTuples > 0) ? 0: (buffSize - 1);
		int end = (numTuples > 0) ? buffSize: 0;
		int increment = (numTuples > 0) ? 1: -1;
		int bias = 1;
		while (bias < buffSize) {
			bias *= 10;
		}
		
		ArrayList<KeyIntegerTuple>  tupleList = new ArrayList<KeyIntegerTuple>(buffSize);
		for (int i = start; i != end; i += increment) {
			KeyIntegerTuple newTuple = new KeyIntegerTuple("foo_"+(bias + i), i);
			tupleList.add(newTuple);
		}
		return tupleList;
	}

	private ArrayList<KeyIntegerTuple> generateRandomTuples(int numTuples) throws InputException {
		ArrayList<KeyIntegerTuple>  tupleList = new ArrayList<KeyIntegerTuple>(numTuples);
		Random r = new Random(99+numTuples);
		for (int i = 0; i != numTuples; i += 1) {
			KeyIntegerTuple newTuple = Utilities.makeRandomKeyIntegerTuple(r, Integer.toHexString(i));
			tupleList.add(newTuple);
		}
		return tupleList;
	}

	private void enterAndCheckTuples(ArrayList<KeyIntegerTuple> tupleList) throws InputException {
		SortedSet<KeyIntegerTuple> enteredTuples = enterTuples(tupleList);
		enteredTuples.addAll(tupleList);
		Iterator<KeyIntegerTuple> actualIterator = mgr.iterator();
		for (KeyIntegerTuple expectedTuple: enteredTuples) {
			KeyValueTuple actualTuple = actualIterator.next();
			assertEquals(expectedTuple, actualTuple);
		}
		assertFalse("not all tuples retrieved", actualIterator.hasNext());
	}

	private SortedSet<KeyIntegerTuple> enterTuples(
			ArrayList<KeyIntegerTuple> tupleList) throws InputException {
		SortedSet<KeyIntegerTuple>  enteredTuples = new TreeSet<KeyIntegerTuple>();
		for (KeyIntegerTuple tup: tupleList) {
			try {
				mgr.addElement(tup);
			} catch (Exception e) {
				e.printStackTrace();
				fail("error inserting "+tup);
			}
		}
		mgr.flush();
		return enteredTuples;
	}

}
