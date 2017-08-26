package org.lasalledebain.hashtable;

import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.lasalledebain.libris.hashfile.FixedSizeEntryFactory;
import org.lasalledebain.libris.hashfile.HashEntry;

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
	public MockFixedSizeHashEntry makeEntry(int key, byte[] dat) {
		return new MockFixedSizeHashEntry(key, dat);
	}

	@Override
	public MockFixedSizeHashEntry makeEntry(int key, ByteBuffer src, int len) {
		return new MockFixedSizeHashEntry(key, src, len);
	}

	@Override
	public HashEntry makeEntry(DataInput backingStore) throws IOException {
		return new MockFixedSizeHashEntry(backingStore, length);
	}
}
