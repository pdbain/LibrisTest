package org.lasalledebain.hashtable;

import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.lasalledebain.libris.hashfile.HashEntry;
import org.lasalledebain.libris.hashfile.VariableSizeEntryFactory;
import org.lasalledebain.libris.hashfile.VariableSizeHashEntry;

public class MockVariableSizeEntryFactory implements VariableSizeEntryFactory<MockVariableSizeHashEntry> {

	private int oversizeThreshold;
	int length;
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

	public int getEntrySize() {
		return length+4;
	}

	public void setOversizeThreshold(int threshold) {
		oversizeThreshold = threshold;
	}

	public MockVariableSizeHashEntry makeEntry(int currentKey) {
		testData += currentKey;
		MockVariableSizeHashEntry result = new MockVariableSizeHashEntry(currentKey, length, testData);
		result.setOversize((oversizeThreshold > 0) && (length >= oversizeThreshold));
		return result;
	}

	@Override
	public MockVariableSizeHashEntry makeEntry(int key, byte[] dat) {
		return new MockVariableSizeHashEntry(key, dat);
	}

	@Override
	public HashEntry makeEntry(int key, ByteBuffer src, int len) {
		return new MockVariableSizeHashEntry(key, src, len);

	}

	@Override
	public HashEntry makeEntry(DataInput backingStore) {
		throw new UnsupportedOperationException();
	}

	public VariableSizeHashEntry makeVariableSizeEntry(int key, int len) {
		byte[] dat = new byte[len];
		for (int i = 0; i < len; ++i) {
			dat[i] = (byte) (key + i);
		}
		MockVariableSizeHashEntry newEntry = new MockVariableSizeHashEntry(key, dat);
		newEntry.setOversize((oversizeThreshold > 0) && (len >= oversizeThreshold));
		return newEntry;
	}
}
