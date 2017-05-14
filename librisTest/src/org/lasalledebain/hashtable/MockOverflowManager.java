package org.lasalledebain.hashtable;

import org.lasalledebain.libris.indexes.BucketOverflowFileManager;
import org.lasalledebain.libris.indexes.FileSpaceManager;

public class MockOverflowManager extends BucketOverflowFileManager {

	public MockOverflowManager(FileSpaceManager overflowFileManager) {
		super(overflowFileManager);
	}
}
