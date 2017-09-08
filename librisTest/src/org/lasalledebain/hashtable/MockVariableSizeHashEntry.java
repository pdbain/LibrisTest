package org.lasalledebain.hashtable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.lasalledebain.libris.hashfile.HashEntry;
import org.lasalledebain.libris.hashfile.VariableSizeHashEntry;
import org.lasalledebain.libris.index.AbstractVariableSizeHashEntry;

class MockVariableSizeHashEntry extends AbstractVariableSizeHashEntry implements VariableSizeHashEntry {

	final byte data[];
	/**
	 * @param key
	 * @param length total length, including overhead and key
	 * @param dat bye value with which to populate the array 
	 */
	public MockVariableSizeHashEntry(int key, int length, byte dat) {
		super(key);
		data = new byte[length];
		for (int i = 0; i < data.length; ++i) {
			data[i] = (byte) ((key * (dat+1+i))%256);
		}
	}

	public MockVariableSizeHashEntry(int key, byte[] dat) {
		super(key);
		data = Arrays.copyOf(dat, dat.length);
	}

	public MockVariableSizeHashEntry(DataInput backingStore) throws IOException {
		super(0);
		throw new IOException("Not supported");
	}

	public MockVariableSizeHashEntry(int key, ByteBuffer src, int len) {
		super(key);
		data = new byte[len];
		src.get(data);
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("key: "); s.append(key);
		s.append("\noversize: "); s.append(oversize);
		s.append("\nlength "); s.append(data.length);
		s.append("\n");
		for (byte b: data) {
			s.append(Byte.toString(b)); s.append(' ');
		}
		return s.toString();
	}

	boolean compare(MockVariableSizeHashEntry other) {
		if (!keyEquals(other)) {
			return false;
		}
		if (data.length != other.data.length) {
			return false;
		}
		for (int i = 0; i < data.length; ++i) {
			if (data[i] != other.data[i]) {
				return false;
			}
		}
		return true;
	}
	public void writeData(DataOutput backingStore) throws IOException {
		backingStore.writeInt(key);
		backingStore.write(data);
	}

	@Override
	public boolean equals(Object comparand) {
		if (comparand.getClass() != this.getClass()) {
			return false;
		}
		MockVariableSizeHashEntry mockCAnd = (MockVariableSizeHashEntry) comparand;
		return compare(mockCAnd);
	}

	public int getDataLength() {
		return data.length;
	}

	public VariableSizeHashEntry clone() {
		MockVariableSizeHashEntry theClone = new MockVariableSizeHashEntry(key, data);
		return theClone;
	}

	public boolean keyEquals(VariableSizeHashEntry other) {
		return other.getKey() == key;
	}

	public boolean isOversize() {
		return oversize;
	}

	public int compareTo(HashEntry arg0) {
		int otherKey = arg0.getKey();
		int myKey = getKey();
		return (otherKey == myKey)? 0: ((otherKey < myKey)? -1: 1);
	}

	public Integer getIntegerKey() {
		return new Integer(key);
	}

}
