package org.lasalledebain.hashtable;

import org.lasalledebain.libris.hashfile.FixedSizeEntryFactory;

public class MockFixedSizeEntryFactory implements FixedSizeEntryFactory<MockFixedSizeHashEntry> {

	byte testData = 42;
	/**
	 * @param currentKey
	 * @param length
	 */
	public MockFixedSizeEntryFactory(int length) {
		this();
		this.length = length;
	}

	public MockFixedSizeEntryFactory() {
		length = 4;
	}

	int length;
	public MockFixedSizeHashEntry makeEntry(int currentKey) {
		testData += currentKey;
		return new MockFixedSizeHashEntry(currentKey, length, testData);
	}

	public int getEntrySize() {
		return length+4;
	}

	@Override
	public MockFixedSizeHashEntry makeEntry() {
		return new MockFixedSizeHashEntry(length);
	}
}
