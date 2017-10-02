package org.lasalledebain;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.ui.Layouts;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementReader;
import org.lasalledebain.libris.xmlUtils.LibrisXmlFactory;

import junit.framework.TestCase;


public class GuiTests extends TestCase {
	private Schema mySchema;
	public void testGuiLayoutXml() {
		File testDir = Utilities.getTestDataDirectory();
		File inputFile = new File(testDir, "layoutDeclarations.xml");
		try {
			ElementManager mgr = Utilities.makeElementManagerFromFile(inputFile, "layouts");

			loadSchema();
			Layouts myLayouts = new Layouts(mySchema);
			myLayouts.fromXml(mgr);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception: "+e);
		}

	}

	public void testDisplayRecord() {
		try {
			LibrisDatabase db = buildDatabase();
			for (int i=1; i <= 3; ++i) {
				db.viewRecord(i);
			}
			db.close(true);
		} catch (Throwable e) {
			e.printStackTrace();
			fail("Unexpected exception: "+e.getMessage());
		}
		

	}

	private LibrisDatabase buildDatabase() throws IOException {
		File testDatabaseFileCopy;
		testDatabaseFileCopy = Utilities.copyTestDatabaseFile();
		LibrisDatabase db = null;
		try {
			db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Unexpected exception "+e);
		}
		return db;
	}
	
	void loadSchema()  {

		File testDir = Utilities.getTestDataDirectory();
		File inputFile = new File(testDir, "schema.xml");
		try {
			InputStreamReader xmlInput = new InputStreamReader(new FileInputStream(inputFile));
			LibrisXmlFactory xmlFactory = new LibrisXmlFactory();
			ElementReader xmlReader = xmlFactory.makeReader(xmlInput, inputFile.getPath());
			mySchema = new Schema();
			mySchema.fromXml(xmlReader);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception: "+e);
		}
	}

	@Override
	protected void setUp() throws Exception {
	}
// TODO test required fields
}
