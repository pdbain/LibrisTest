package org.lasalledebain.hashtable;

import org.lasalledebain.libris.hashfile.VariableSizeEntryFactory;
import org.lasalledebain.libris.hashfile.VariableSizeHashEntry;

public class MockEntryFactory implements VariableSizeEntryFactory<MockHashEntry> {

	private int oversizeThreshold;
	byte testData = 42;
	/**
	 * @param currentKey
	 * @param length
	 */
	public MockEntryFactory(int length) {
		this();
		this.length = length;
	}

	public MockEntryFactory() {
		length = -1;
		oversizeThreshold = -1;
	}

	int length;
	public MockHashEntry makeEntry() {
		return new MockHashEntry(length);
	}

	public VariableSizeHashEntry makeVariableSizeEntry(int key, int length) {
		final MockHashEntry result = new MockHashEntry(key, length);
		result.setOversize((oversizeThreshold > 0) && (length >= oversizeThreshold));
		return result;
	}

	public MockHashEntry makeEntry(int currentKey) {
		testData += currentKey;
		return new MockHashEntry(currentKey, length, testData);
	}

	public int getEntrySize() {
		return length+4;
	}

	public void setOversizeThreshold(int threshold) {
		oversizeThreshold = threshold;
	}
}
