package org.lasalledebain.hashtable;

import org.lasalledebain.libris.hashfile.VariableSizeEntryFactory;
import org.lasalledebain.libris.hashfile.VariableSizeHashEntry;

public class MockVariableSizeEntryFactory implements VariableSizeEntryFactory<MockVariableSizeHashEntry> {

	private int oversizeThreshold;
	byte testData = 42;
	/**
	 * @param currentKey
	 * @param length
	 */
	public MockVariableSizeEntryFactory(int length) {
		this();
		this.length = length;
	}

	public MockVariableSizeEntryFactory() {
		length = -1;
		oversizeThreshold = -1;
	}

	int length;
	public MockVariableSizeHashEntry makeEntry() {
		return new MockVariableSizeHashEntry(length);
	}

	public VariableSizeHashEntry makeVariableSizeEntry(int key, int length) {
		final VariableSizeHashEntry result = new MockVariableSizeHashEntry(key, length);
		result.setOversize((oversizeThreshold > 0) && (length >= oversizeThreshold));
		return result;
	}

	public MockVariableSizeHashEntry makeEntry(int currentKey) {
		testData += currentKey;
		return new MockVariableSizeHashEntry(currentKey, length, testData);
	}

	public int getEntrySize() {
		return length+4;
	}

	public void setOversizeThreshold(int threshold) {
		oversizeThreshold = threshold;
	}
}
