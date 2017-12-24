package org.lasalledebain.libris.search;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Test;
import org.lasalledebain.Utilities;
import org.lasalledebain.libris.LibrisConstants;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.indexes.BloomFilterSection;
import org.lasalledebain.libris.indexes.BloomFilterSectionEditor;
import org.lasalledebain.libris.indexes.BloomFilterSectionQuery;
import org.lasalledebain.libris.indexes.KeywordFilteredRecordIterator;
import org.lasalledebain.libris.util.Lorem;

import junit.framework.TestCase;

public class BloomFilterTest extends TestCase {

	static final String parts[] = {"zero", "one", "two", "three", "four", "five", "six", "seven", 
			"eight", "nine"};
	static final int numParts = parts.length;

	public BloomFilterTest() {
		terms = new HashSet<>(Arrays.asList("The", "quick", "brown", "fox"));
	}

	private File testDirectory;
	@Test
	public void testSanity() {
		HashSet<String> term = new HashSet<>();
		term.add("foobar");
		BloomFilterSectionEditor bf = addAndCheckTerms(term);
		term.clear();
		term.add("unexpectedword");
		assertFalse("Unexpected term found", bf.match(term));
	}

	@Test
	public void testMultipleTerms() {
		addAndCheckTerms(terms);
	}

	@Test
	public void testPrefixes() {
		File sigFileFile = new File(testDirectory, "signature_file");
		BloomFilterSectionEditor bf = null;
		try {
			RandomAccessFile sigFile = new RandomAccessFile(sigFileFile, "rw");
			bf = new BloomFilterSectionEditor(sigFile, 1, terms, 0);
			for (String t: terms) {
				for (int i = LibrisConstants.MINIMUM_TERM_LENGTH; i <= t.length(); ++i) {
					String s = t.substring(0, i);
					assertTrue("substring "+s+" of "+t+" not found", bf.match(Arrays.asList(s)));
				}
			}
		} catch (IOException e) {
			fail("reading signature file" + e.getMessage());
		}
	}

	@Test
	public void testStress() {
		HashSet<String> term = new HashSet<String>(Arrays.asList(Lorem.words));
		BloomFilterSectionEditor bf = addAndCheckTerms(term);
		term.clear();
		term.add("unexpectedword");
		assertFalse("Unexpected term found", bf.match(term));
	}

	@Test
	public void testMultipleRecords() {
		try {
			ArrayList<Set<String>> recordTermSets = new ArrayList<>();
			File sigFileFile = new File(testDirectory, "signature_file");
			RandomAccessFile sigFile = new RandomAccessFile(sigFileFile, "rw");
			makeSearchTerms(recordTermSets);
			BloomFilterSectionEditor bf = new BloomFilterSectionEditor(sigFile, 0);
			addSearchTerm(recordTermSets, bf);
			for (int recId = 1; recId<= recordTermSets.size(); ++recId) {
				bf.load(recId);
				for (int setId = 0; setId < recordTermSets.size(); ++setId) {
					Set<String> s = recordTermSets.get(setId);
					boolean found = bf.match(s);
					if (setId == recId - 1) {
						assertTrue("missing match", found);
					} else {
						if (found) {
							Set<String> orig = recordTermSets.get(recId -1);
							for (String str: s) {
								if (!orig.contains(str)) {
									assertFalse("false match recId="+recId+" set="+setId, found);
								}
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOExceptionError "+e.getMessage());
		}
	}

	@Test
	public void testQuery() {
		try {
			ArrayList<Set<String>> recordTermSets = new ArrayList<>();
			File sigFileFile = new File(testDirectory, "signature_file");
			RandomAccessFile sigFile = new RandomAccessFile(sigFileFile, "rw");
			makeSearchTerms(recordTermSets);
			BloomFilterSectionEditor bf = new BloomFilterSectionEditor(sigFile, 0);
			addSearchTerm(recordTermSets, bf);
			String searchTerm = "volutpat";
			BloomFilterSectionQuery bfq = new BloomFilterSectionQuery(sigFile, 1, Arrays.asList(searchTerm), 0);
			for (int recId = 1; recId<= recordTermSets.size(); ++recId) {
				bfq.load(recId);
				if (recordTermSets.get(recId-1).contains(searchTerm)) {
					assertTrue("record "+recId+" failed to match", bfq.match());
				} else {
					assertFalse("record "+recId+" false match", bfq.match());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOExceptionError "+e.getMessage());
		}
	}

	@Test
	public void testHigherLevels() {
		try {
			ArrayList<Set<String>> recordTermSets = new ArrayList<>();
			File sigFileFile = new File(testDirectory, "signature_file");
			int recsPerSig = BloomFilterSection.BRANCH_FACTOR;
			makeSearchTerms(recordTermSets);
			for (int level = 1; level <= BloomFilterSection.MAX_LEVEL; ++level) {
				RandomAccessFile sigFile = new RandomAccessFile(sigFileFile, "rw");
				BloomFilterSectionEditor bf = new BloomFilterSectionEditor(sigFile, level);
				{
					int recId = 1;
					for (Set<String> s: recordTermSets) {
						bf.load(recId);
						bf.addTerms(s);
						assertTrue("check immediately", bf.match(s));
						recId++;
						if ((recId % recsPerSig) == 0) {
							bf.store();
						}
					}
				}
				int searchId = 1;
				int maxRecordId = recordTermSets.size() + 1;
				int offset = 1;
				for (int recId = 0; recId<= maxRecordId; recId += recsPerSig) {
					bf.load(recId + offset++);
					if (offset >= recsPerSig) {
						offset = 0;
					}
					int searchLimit = Math.min(recId + recsPerSig, maxRecordId);
					while (searchId < searchLimit) {
						Set<String> s = recordTermSets.get(searchId - 1);
						assertTrue("missing match", bf.match(s));
						searchId++;
					}
					int otherSet = recId - recsPerSig;
					if (recId > LibrisConstants.NULL_RECORD_ID) {
						Set<String> s = recordTermSets.get(otherSet);
						assertFalse("false match", bf.match(s));

					}
				}
				recsPerSig *= BloomFilterSection.BRANCH_FACTOR;
				sigFile.close();
				sigFileFile.delete();
			}
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testFilterSanity() {
		try {
			final int numRecords = 1000;
			File sigFileFile = new File(testDirectory, "signature_file");
			RandomAccessFile sigFile = new RandomAccessFile(sigFileFile, "rw");
			BloomFilterSectionEditor bf = new BloomFilterSectionEditor(sigFile, 0);
			for (int i = 1; i <= numRecords; ++i) {
				Set<String> partWords = makeNumericWord(i);
				bf.setTerms(i, partWords);
				bf.store();
			}
			int modulus = numParts;
			for (int length = 1; length <= 3; ++length) {
				for (int recId = 1; recId <= numRecords; recId *= 7) {
					final String searchTerm = makeTerm(recId, length);
					iterateAndCheckFilter(numRecords, sigFile, recId, searchTerm, modulus);
				}
				modulus *= numParts;
			}
		} catch (IOException | UserErrorException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testFilterLevels() {
		try {
			final int numRecords = 2500;
			final int numLevels = 3;
			File sigFileFile = new File(testDirectory, "signature_file");
			for (int level = 0; level < numLevels; ++level) {
				sigFileFile.delete();
				RandomAccessFile sigFile = new RandomAccessFile(sigFileFile, "rw");
				BloomFilterSectionEditor bf = new BloomFilterSectionEditor(sigFile, level);
				for (int recId = 1; recId <= numRecords; ++recId) {
					if (!bf.isLoaded(recId)) {
						bf.store();
						bf.initialize(recId);
					}
					Set<String> partWords = makeNumericWord(recId);
					bf.addTerms(partWords);
				}
				bf.store();
				int modulus = numParts * numParts;

				for (int length = 2; length <= 3; ++length) {
					for (int recId = 1; recId <= numRecords; recId *= 3) {
						final String searchTerm = makeTerm(recId, length);
						KeywordFilteredRecordIterator mainKfri = new KeywordFilteredRecordIterator(sigFile, level, 
								Arrays.asList(searchTerm), null);
						final int recordsPerRange = mainKfri.getRecordsPerRange();
						int result;
						for (int expectedId: generateExpectedNumericIds(recId, numRecords, modulus, recordsPerRange)) {
							result = mainKfri.next(numRecords);
							assertEquals("wrong result for "+recId+" got "+result+" level="+level, expectedId, result);
						}
						result = mainKfri.next(numRecords);
						assertTrue("spurious result: got "+result, LibrisConstants.NULL_RECORD_ID == result);
					}
					modulus *= numParts;
				}
			}
		} catch (IOException | UserErrorException e) {
			fail(e.getMessage());
		}
	}

	static Iterable<Integer> generateExpectedNumericIds(int recId, int maxId, int modulus, int recordsPerRange) {
		SortedSet<Integer> result = new TreeSet<>();
		int initialId = recId % modulus;
		for (int id = initialId; id <= maxId; id += modulus) {
			int baseId = Math.max(1, id - (id % recordsPerRange));
			result.add(baseId);
		}
		return result;
	}

	public Set<String> makeNumericWord(int recId) {
		Set<String> partWords = new HashSet<>();
		partWords.add(makeTerm(recId, 1));
		partWords.add(makeTerm(recId, 2));
		partWords.add(makeTerm(recId, 3));
		return partWords;
	}

		@Test
	public void testFilterMultilevel() {
		try {
			final int numRecords = 2500;
			final int numLevels = 3;
			RandomAccessFile sigFiles[] = new RandomAccessFile[numLevels];
			BloomFilterSectionEditor bfs[] = new BloomFilterSectionEditor[numLevels];
			for (int level = 0; level < numLevels; ++level) {
				sigFiles[level] = new RandomAccessFile(new File(testDirectory, "signature_file_"+level), "rw");
				bfs[level] = new BloomFilterSectionEditor(sigFiles[level], level);
			}
			for (int recId = 1; recId <= numRecords; ++recId) {
				Set<String> partWords = makeNumericWord(recId);
				for (int level = 0; level < numLevels; ++level) {
					if (!bfs[level].isLoaded(recId)) {
						bfs[level].store();
						bfs[level].initialize(recId);
					}
					bfs[level].addTerms(partWords);
				}
			}
			for (int level = 0; level < numLevels; ++level) {
				bfs[level].store();
			}
			int modulus = numParts;

			for (int length = 1; length <= 3; ++length) {
				for (int recId = 1; recId <= numRecords; recId *= 3) {
					final String searchTerm = makeTerm(recId, length);
					KeywordFilteredRecordIterator mainKfri = null;
					for (int level = 0; level < numLevels; ++level) {
						mainKfri = new KeywordFilteredRecordIterator(sigFiles[level], level, Arrays.asList(searchTerm), mainKfri);
					}
					int result;
					for (int expectedId: generateExpectedNumericIds(recId, numRecords, modulus, 1)) {
						result = mainKfri.next(numRecords);
						assertEquals("wrong result for "+recId+" got "+result, expectedId, result);
					}
					result = mainKfri.next(numRecords);
					assertTrue("spurious result: got "+result, LibrisConstants.NULL_RECORD_ID == result);
				}
				modulus *= numParts;
			}
		} catch (IOException | UserErrorException e) {
			fail(e.getMessage());
		}
	}

	public void iterateAndCheckFilter(final int numRecords, RandomAccessFile sigFile, int recId,
			final String searchTerm, int modulus) throws UserErrorException, IOException {
		KeywordFilteredRecordIterator kfri = new KeywordFilteredRecordIterator(sigFile, 0, 
				Arrays.asList(searchTerm), null);
		int result;
		do {
			result = kfri.next(numRecords);
			assertTrue("wrong result for "+recId+" got "+result, (result % modulus) == (recId % modulus));
		} while (result + modulus <= numRecords);
		result = kfri.next(numRecords);
		assertTrue("spurious result: got "+result, LibrisConstants.NULL_RECORD_ID == result);
	}

	private final Set<String> terms;

	@Override
	protected void setUp() throws Exception {
		testDirectory = Utilities.makeTestDirectory();
	}

	@Override
	protected void tearDown() throws Exception {
		Utilities.deleteRecursively(testDirectory);
	}

	private BloomFilterSectionEditor addAndCheckTerms(Set<String> terms) {
		File sigFileFile = new File(testDirectory, "signature_file");
		BloomFilterSectionEditor bf = null;
		try {
			RandomAccessFile sigFile = new RandomAccessFile(sigFileFile, "rw");
			bf = new BloomFilterSectionEditor(sigFile, 1, terms, 0);
			bf.store();
			sigFile.close();
		} catch (IOException e) {
			fail("creating signature file" + e.getMessage());
		}
		try {
			RandomAccessFile sigFile = new RandomAccessFile(sigFileFile, "rw");
			bf = new BloomFilterSectionEditor(sigFile, 0);
			bf.load(1);
			assertTrue("Term not found", bf.match(terms));
		} catch (IOException e) {
			fail("reading signature file" + e.getMessage());
		}
		return bf;
	}

	private void addSearchTerm(ArrayList<Set<String>> recordTermSets, BloomFilterSectionEditor bf) throws IOException {
		int recId = 1;
		for (Set<String> s: recordTermSets) {
			bf.setTerms(recId, s);
			assertTrue("check immediately", bf.match(s));
			bf.store();
			recId++;
		}
	}

	private void makeSearchTerms(ArrayList<Set<String>> recordTermSets) {
		Lorem wordlistGenerator = new Lorem(2, 16, 6135969275L);
		while (wordlistGenerator.hasNext()) {
			String[] nextWords = wordlistGenerator.next();
			recordTermSets.add(new HashSet<>(Arrays.asList(nextWords)));
		}
	}

	String makeTerm(int id, int len) {
		StringBuffer buff = new StringBuffer(parts[id % numParts]);
		if (len > 1) {
			buff.append(parts[(id/numParts) % numParts]);
			if (len > 2) {
				buff.append(parts[(id/(numParts * numParts)) % numParts]);
			}
		}
		return buff.toString();
	}
}
