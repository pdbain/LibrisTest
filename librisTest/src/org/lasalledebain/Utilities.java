package org.lasalledebain;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.Libris;
import org.lasalledebain.libris.LibrisDatabase;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordId;
import org.lasalledebain.libris.RecordTemplate;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.Field.FieldType;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.RecordDataException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.indexes.FileSpaceManager;
import org.lasalledebain.libris.indexes.KeyIntegerTuple;
import org.lasalledebain.libris.ui.Layouts;
import org.lasalledebain.libris.ui.LibrisUi;
import org.lasalledebain.libris.xmlUtils.ElementManager;
import org.lasalledebain.libris.xmlUtils.ElementReader;
import org.lasalledebain.libris.xmlUtils.LibrisXMLConstants;
import org.lasalledebain.libris.xmlUtils.LibrisXmlFactory;
import org.lasalledebain.libris.xmlUtils.XmlShapes;
import org.lasalledebain.libris.xmlUtils.XmlShapes.SHAPE_LIST;

public class Utilities extends TestCase {
	public static final String KEYWORD_DATABASE4_XML = "KeywordDatabase4.xml";
	static final String LAYOUT_DECLARATIONS_XML_FILE = "layoutDeclarations.xml";
	public static final String TEST_RECORD1_XML_FILE = "TestRecord1.xml";
	public static final String TEST_RECORD2_XML_FILE = "TestRecord2.xml";
	public static final String TEST_RECORD_WITH_GROUPS_XML_FILE = "TestRecordWithGroups.xml";
	public static final String TEST_RECORD_WITH_MISSING_FIELDS = "TestRecordWithMissingFields.xml";
	public static final String TEST_RECORD_WITH_ENUM_XML_FILE = "TestRecordWithEnum.xml";
	public static final String TEST_DELIM_TEXT_FILE_1 = "testimport1.csv";
	public static final String TEST_DELIM_TEXT_FILE_WITH_FIELD_IDS = "testImportWithFieldIds.csv";
	public static final String TEST_RECORD3_XML_FILE = "TestRecord3.xml";
	public static final String TEST_SCHEMA_XML_FILE = "schema.xml";
	public static final String TEST_SCHEMA_ENUMDEFS_XML_FILE = "schemaWithEnumDefs.xml";
	public static final String LAYOUT_DECLARATIONS2_XML_FILE = "layoutDeclarations2.xml";
	public static final String TEST_SCHEMA2_XML_FILE = "schema2.xml";
	public static final String TEST_DB1_XML_FILE = "testDatabase1.xml";
	public final static String TEST_DB1_IMPORT_FILE ="csv_import.xml";
	public static final String TEST_DB4_XML_FILE = "testDatabase4.xml";
	public static final String TEST_DB_WITH_DEFAULTS_XML_FILE = "databaseWithDefaults.xml";
	public static final String TEST_DB_WITH_GROUPS_XML_FILE = "databaseWithGroups.xml";
	public static final String EMPTY_DATABASE_FILE = "emptyDatabase1.xml";
	public static final String SCHEMA_WITH_GROUP_DEFS_XML = "schemaWithGroupDefs.xml";
	public static final String SCHEMA_WITH_DEFAULTS = "SchemaWithDefaults.xml";
	public static final String DATABASE_WITH_GROUPS_XML = "DatabaseWithGroups.xml";
	public static final String DATABASE_WITH_GROUPS_AND_RECORDS_XML = "DatabaseWithGroupsAndRecords.xml";
	public static final String TEST_DATABASE_UNORDERED_XML = "testDatabaseUnordered.xml";
	
	static RecordTemplate makeRecordTemplate(String[] fieldNames,
			FieldType[] fts) throws DatabaseException, LibrisException {
		Schema s = new MockSchema();
		for (int i = 0; i < fts.length; ++i) {
			String id = fieldNames[i];
			FieldTemplate ft = createTemplate(id, fts[i]);
			s.addField(ft);
		}
		RecordTemplate rt = RecordTemplate.templateFactory(s);
		return rt;
	}

	public static FieldTemplate createTemplate(String id,
			Field.FieldType ft) {
		return new FieldTemplate(id, "", ft);
	}

	public static File getTestDataDirectory() {
		info(System.getProperty("user.dir"));
		File testDir = new File(System.getProperty("user.dir"), "data");
		return testDir;
	}

	public static File getTestDatabase(String dbName) {
		File testDir = new File(System.getProperty("user.dir"), "data");
		return new File(testDir, dbName);
	}

	public static File makeTestDirectory() {
		File testDirectory = new File(System.getProperty("java.io.tmpdir"), "librisTest");
		if (!testDirectory.exists()) {
			testDirectory.mkdir();
		}
		return testDirectory;
	}
	
	public static Schema loadSchema(File schemaFile) throws InputException, DatabaseException  {
		FileReader rdr;
		try {
			rdr = new FileReader(schemaFile);
			LibrisXmlFactory xmlFactory = new LibrisXmlFactory();
			ElementReader xmlReader = xmlFactory.makeReader(rdr, schemaFile.getPath());
			Schema s = new Schema();
			s.fromXml(xmlReader);
			return s;
		} catch (FileNotFoundException e) {
			throw new InputException("Cannot open "+schemaFile.getPath(), e);
		}
	}

	public static Layouts loadLayoutsFromXml(Schema schem, File inputFile)
	throws FileNotFoundException, FactoryConfigurationError,
	XMLStreamException, LibrisException, DatabaseException {
		ElementManager mgr = makeElementManagerFromFile(inputFile, "layouts");
		Layouts myLayouts = new Layouts(null);
		myLayouts.fromXml(schem, mgr);
		return myLayouts;
	}

	static Record loadRecordFromXml(File schemaFile, File recordFile)
	throws LibrisException, XMLStreamException,
	LibrisException, RecordDataException,
	FactoryConfigurationError, DatabaseException, FileNotFoundException {
		Schema s = Utilities.loadSchema(schemaFile);
		RecordTemplate rt = RecordTemplate.templateFactory(s);
		ElementManager mgr = makeElementManagerFromFile(recordFile, "record");
		Record rec = rt.makeRecord(true);
		rec.fromXml(mgr);
		return rec;
	}

	static Record loadRecordFromXml(File schemaFile, InputStream xmlStream, File sourceFile) throws 
		LibrisException, FileNotFoundException, XMLStreamException, FactoryConfigurationError {
		Schema s = Utilities.loadSchema(schemaFile);
		RecordTemplate rt = RecordTemplate.templateFactory(s);
		String sourcePath = (null == sourceFile)? null : sourceFile.getPath();
		ElementManager mgr = makeElementManagerFromInputStream(xmlStream, sourcePath, LibrisXMLConstants.XML_RECORD_TAG);
		Record rec = rt.makeRecord(true);
		rec.fromXml(mgr);
		return rec;
	}

	public static ElementManager makeElementManagerFromFile(File recordFile,
			String elementNameString) throws XmlException  {
		FileInputStream xmlInputStream;
		try {
			xmlInputStream = new FileInputStream(recordFile);
		return makeElementManagerFromInputStream(xmlInputStream, recordFile.getPath(), elementNameString);
		} catch (FileNotFoundException e) {
			throw new XmlException("error parsing "+elementNameString, e);
		}
	}

	static final String LAYOUT2 = "layout2";
	public static ElementManager makeElementManager(
			ByteArrayInputStream xmlInput, File sourceFile, String elementNameString) {
		String sourcePath = (null == sourceFile)? null : sourceFile.getPath();
		return makeElementManagerFromInputStream(xmlInput, sourcePath, elementNameString) ;
	}

	/**
	 * @param elementNameString
	 * @param xmlInputStream
	 * @return
	 */
	public static ElementManager makeElementManagerFromInputStream(
			InputStream xmlInputStream, String sourceFile, String elementNameString) {
		LibrisXmlFactory xmlFactory = new LibrisXmlFactory();
		InputStreamReader xmlInput = new InputStreamReader(xmlInputStream);
		try {
			return xmlFactory.makeLibrisElementManager(xmlInput, 
					sourceFile, elementNameString, new XmlShapes(SHAPE_LIST.DATABASE_SHAPES));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static ElementManager makeElementManagerFromReader(
			Reader xmlSourceReader, String sourceFile, String elementNameString) {
		LibrisXmlFactory xmlFactory = new LibrisXmlFactory();
		try {
			return xmlFactory.makeLibrisElementManager(xmlSourceReader, 
					sourceFile, elementNameString, new XmlShapes(SHAPE_LIST.DATABASE_SHAPES));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean deleteRecursively(File root) {
		if (null == root) {
			return false;
		}
		if (root.isDirectory()) {
			for (File f: root.listFiles()) {
				deleteRecursively(f);
			} 
		}
		return root.delete();
	}
	/**
	 * @param originalFile
	 * @param copyFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void copyFile(File originalFile, File copyFile)
			throws FileNotFoundException, IOException {
		if (copyFile.exists()) {
			copyFile.delete();
		}
		copyFile.createNewFile();
		copyFile.setWritable(true);
		FileChannel original = null; 
		FileChannel copy = null; 
		try {
			original = (new FileInputStream(originalFile)).getChannel();
			copy = (new FileOutputStream(copyFile)).getChannel();
			copy.transferFrom(original, 0, original.size());
		} finally {
			if (null != original) {
				original.close();
			}
		}
	}
	/**
	 * @return
	 * @throws IOException 
	 */
	public static File copyTestDatabaseFile() throws IOException {
		String testDbName = TEST_DB1_XML_FILE;
		File testDatabaseFileCopy = copyTestDatabaseFile(testDbName);
		return testDatabaseFileCopy;
	}

	public static File copyTestDatabaseFile(String testDbName)
			throws FileNotFoundException, IOException {
		File testDatabaseFile = getTestDatabase(testDbName);
		File testDatabaseFileCopy = new File(System.getProperty("java.io.tmpdir"), testDatabaseFile.getName());
		copyFile(testDatabaseFile, testDatabaseFileCopy);
		return testDatabaseFileCopy;
	}
	
	public static File copyTestDatabaseFile(File workdir, String testDbName)
	throws FileNotFoundException, IOException {
		File testDatabaseFile = getTestDatabase(testDbName);
		File testDatabaseFileCopy = new File(workdir, testDatabaseFile.getName());
		copyFile(testDatabaseFile, testDatabaseFileCopy);
		return testDatabaseFileCopy;
	}

	public static void deleteTestDatabaseFiles() {

		deleteTestDatabaseFiles(TEST_DB1_XML_FILE);
	}

	public static void deleteTestDatabaseFiles(final String dbName) {
		File testDatabaseFile = getTestDatabase(dbName);
		File testDatabaseFileCopy = new File(System.getProperty("java.io.tmpdir"), testDatabaseFile.getName());
		testDatabaseFileCopy.delete();
		File auxFiles = new File(testDatabaseFileCopy.getParent(), ".libris_auxfiles_"+dbName);
		if (auxFiles.exists()) {
			for (File f: auxFiles.listFiles()) {
				f.delete();
			}
			auxFiles.delete();
		}
	}
	public static File getTempTestDirectory() {
		File tempTestDirectory = new File(System.getProperty("java.io.tmpdir"), "libristest");
		if (tempTestDirectory.exists() && !deleteRecursively(tempTestDirectory)) {
			return null;
		} else if (!tempTestDirectory.mkdir()) {
			return null;
		} else {
			return tempTestDirectory;
		}
	}

	public static FileSpaceManager makeFileSpaceManager(String managerName) {
		File workDir = getTempTestDirectory();
		if (null == workDir) {
			fail("could not create working directory ");
		}
		File testFile = new File(workDir, managerName);
		if (!testFile.exists()) {
			try {
				testFile.createNewFile();
			} catch (IOException e) {
				fail("could not create "+managerName);
			}
		}
		FileSpaceManager mgr = null;
		try {
			mgr = new FileSpaceManager(testFile, false);
			mgr.reset();
		} catch (FileNotFoundException e) {
			fail("could not find "+managerName);
		} catch (LibrisException e) {
			e.printStackTrace();
			fail("error opening filespace manager "+e.getMessage());
		}
		return mgr;
	}

	public static void destroyFileSpaceManager(FileSpaceManager mgr) {
		try {
			mgr.close();
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail("error closing filespace manager");
		}
		mgr.getFilePath().delete();
	}

	public static final String ALPHANUMS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	public static final char[] alphanumChars = ALPHANUMS.toCharArray();
	public static KeyIntegerTuple makeRandomKeyIntegerTuple(Random r, String suffix) throws InputException {
		StringBuffer sb = new StringBuffer(128);
		int keySize = 0;
		while (keySize < 1) {
			keySize = Math.abs(r.nextInt()) % 64;
		}
		for (int i = 0; i < keySize; ++i) {
			int selector = Math.abs(r.nextInt()) % alphanumChars.length;
			sb.append(alphanumChars[selector]);
		}
		sb.append('_');
		sb.append(suffix);
		KeyIntegerTuple tup = new KeyIntegerTuple(sb.toString(), r.nextInt());
		return tup;
	}

	static String getRecordIdString(Record recData) {
		return RecordId.toString(recData.getRecordId());
	}
	
	static final Logger testLogger;
	static {
		testLogger = Logger.getLogger("org.lasalledebain.test");
		testLogger.setLevel(Boolean.getBoolean("org.lasalledebain.test.verbose")? Level.ALL: Level.WARNING);
	}
	
	static void info(String msg) {
		testLogger.info(msg);
	}
	
	static void warn(String msg) {
		testLogger.warning(msg);
	}

	public static LibrisDatabase buildTestDatabase(String databaseFileName)
			throws FileNotFoundException, IOException {
		File testDatabaseFileCopy = copyTestDatabaseFile(databaseFileName);			
		LibrisDatabase db = null;
		try {
			db = Libris.buildAndOpenDatabase(testDatabaseFileCopy);
			LibrisUi ui = db.getUi();
			db.close();
			db = ui.openDatabase();
		} catch (LibrisException e) {
			e.printStackTrace();
			fail("Error rebuilding database");
		}
		return db;
	}

}
