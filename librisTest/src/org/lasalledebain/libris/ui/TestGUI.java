package org.lasalledebain.libris.ui;

import java.io.File;

import org.lasalledebain.libris.exception.LibrisException;

public class TestGUI extends LibrisGui {

	public TestGUI(File dbFile) throws LibrisException {
		super(dbFile, null, false);
	}
	public BrowserWindow getResultsWindow() {
		return resultsPanel;
	}
	public RecordDisplayPanel getDisplayPanel() {
		return displayPanel;
	}


}
