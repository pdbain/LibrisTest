package org.lasalledebain.hashtable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.lasalledebain.libris.hashfile.HashEntry;
import org.lasalledebain.libris.index.AbstractFixedSizeHashEntry;

class MockFixedSizeHashEntry extends AbstractFixedSizeHashEntry {

	final byte data[];
	/**
	 * @param key
	 * @param length total length, including overhead and key
	 */
	public MockFixedSizeHashEntry(int key, byte dat[]) {
		super(key);
		data = Arrays.copyOf(dat, dat.length);
	}

	public MockFixedSizeHashEntry(int key, int length, byte initialData) {
		super(key);
		data = new byte[length];
		for (int i = 0; i < length; ++i) {
			data[i] = (byte) ((key * (initialData+1+i))%256);
		}
	}
	
	public MockFixedSizeHashEntry(int key, ByteBuffer src, int len) {
		super(key);
		data = new byte[len];
		src.get(data, 0, len);
	}

	public MockFixedSizeHashEntry(DataInput backingStore, int length) throws IOException {
		super(backingStore.readInt());
		data = new byte[length];
		backingStore.readFully(data);
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

/* TODO delete
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
*/
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
		MockFixedSizeHashEntry theClone = new MockFixedSizeHashEntry(key, data);
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
}
