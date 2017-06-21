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
		length = -1;
	}

	int length;
	public MockFixedSizeHashEntry makeEntry() {
		return new MockFixedSizeHashEntry(length);
	}

	public int getEntrySize() {
		return length+4;
	}
}
