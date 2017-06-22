package org.lasalledebain.hashtable;

import static org.junit.Assert.fail;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.lasalledebain.libris.hashfile.FixedSizeHashEntry;
import org.lasalledebain.libris.hashfile.HashEntry;
import org.lasalledebain.libris.hashfile.VariableSizeHashEntry;
import org.lasalledebain.libris.index.AbstractFixedSizeHashEntry;
import org.lasalledebain.libris.index.AbstractVariableSizeHashEntry;

class MockFixedSizeHashEntry extends AbstractFixedSizeHashEntry {

	byte data[];
	/**
	 * @param key
	 * @param length total length, including overhead and key
	 */
	public MockFixedSizeHashEntry(int key, int length) {
		this.key = key;
		data = new byte[length];
	}

	public MockFixedSizeHashEntry(int key, int length, byte initialData) {
		this(key, length);
		for (int i = 0; i < data.length; ++i) {
			data[i] = (byte) ((key * (initialData+1+i))%256);
		}
	}
	public MockFixedSizeHashEntry(int length) {
		if (length > 0) {
			data = new byte[length];
		}
	}
	
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
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

	boolean compare(MockFixedSizeHashEntry other) {
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

	public void readData(DataInput backingStore) throws IOException {
		key = backingStore.readInt();
		try {
			backingStore.readFully(data);
		} catch (EOFException e) {
			fail("unexpected end of file");
		}
	}

	public void readData(ByteBuffer buff, int length) {
		if (null == data) {
			data = new byte[length];
		}
		buff.get(data, 0, length);
	}

	public void readData(DataInput ip, int length) throws IOException {
		if (null == data) {
			data = new byte[length];
		}
		ip.readFully(data);
	}

	@Override
	public boolean equals(Object comparand) {
		if (comparand.getClass() != this.getClass()) {
			return false;
		}
		MockFixedSizeHashEntry mockCAnd = (MockFixedSizeHashEntry) comparand;
		return compare(mockCAnd);
	}

	public int getTotalLength() {
		int result = getEntryLength() + getOverheadLength();
		return result;
	}

	public int getOverheadLength() {
		return 0;
	}

	public int getEntryLength() {
		return 4  /* key */ + data.length;
	}

	public int getDataLength() {
		return data.length;
	}

	public MockFixedSizeHashEntry clone() {
		MockFixedSizeHashEntry theClone = new MockFixedSizeHashEntry(key, data.length);
		System.arraycopy(this.data, 0, theClone.data, 0, data.length);
		return theClone;
	}

	public boolean keyEquals(MockFixedSizeHashEntry other) {
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

	@Override
	public int getKey() {
		return key;
	}

	@Override
	public void setKey(int newKey) {
		key = newKey;
		
	}

	@Override
	public void setOversize(boolean oversize) {
		this.oversize = oversize;
	}

}
