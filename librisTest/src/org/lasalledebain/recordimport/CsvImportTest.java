package org.lasalledebain.recordimport;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.TestCase;

import org.lasalledebain.Utilities;
import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FileAccessManager;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.LibrisException;
import org.lasalledebain.libris.exception.UserErrorException;
import org.lasalledebain.libris.field.FieldValue;
import org.lasalledebain.libris.field.FieldValueStringList;
import org.lasalledebain.libris.records.DelimitedTextRecordsReader;
import org.lasalledebain.libris.records.DirectRecordImporter;
import org.lasalledebain.libris.records.FilteringRecordImporter;
import org.lasalledebain.libris.records.RecordImporter;
import org.lasalledebain.libris.util.DiagnosticDatabase;
import org.lasalledebain.libris.xmlUtils.LibrisXmlFactory;


public class CsvImportTest extends TestCase {
	private static final char CSV_COLUMN_SEP = '\t';
	private DiagnosticDatabase testDatabase;
	HashMap<String, String> valueTranslations;
	public void testArrayImport() {
		 RecordImporter imp = new DirectRecordImporter(testDatabase);
		 try {
			 convertToCsvBytes(testData1);
			 for (FieldValueStringList row[]: convertToFVSL(testData1)) {
				 Record r = imp.importRecord(row);
				 checkFields(row, r);
			 }
		 } catch (Exception e) {
			 e.printStackTrace();
			 fail(e.getMessage());
		 }
	 }

	public void testRawImport() {
		DelimitedTextRecordsReader recReader = new DelimitedTextRecordsReader(testDatabase, CSV_COLUMN_SEP);
		recReader.setSeparatorChar(CSV_COLUMN_SEP);
		try {
			Record[] recs = recReader.importRecordsToDatabase(
					new InputStreamReader(new ByteArrayInputStream(convertToCsvBytes(testData1))),
					new DirectRecordImporter(testDatabase));
			int i = 0;
			for (FieldValueStringList[] row: convertToFVSL(testData1)) {
				checkFields(row, recs[i]);
				++i;
			}
		} catch (LibrisException e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testFilteredImport() {
		try {
			FilteringRecordImporter imp = new FilteringRecordImporter(testDatabase, 
					new LibrisXmlFactory(), 
					db1Mgr);

			DelimitedTextRecordsReader recReader = new DelimitedTextRecordsReader(testDatabase, CSV_COLUMN_SEP);
			Record[] recs = recReader.importRecordsToDatabase(
					new InputStreamReader(new ByteArrayInputStream(convertToCsvBytes(testData2a))),
					imp);
			int i = 0;
			for (FieldValueStringList[] row: convertToFVSL(testData2b)) {
				checkFields(row, recs[i]);
				++i;
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}

	public void testWildcardSubstitution() {
		try {
			FilteringRecordImporter imp = new FilteringRecordImporter(testDatabase, 
					new LibrisXmlFactory(),db1Mgr);

			DelimitedTextRecordsReader recReader = new DelimitedTextRecordsReader(testDatabase, CSV_COLUMN_SEP);
			Record[] recs = recReader.importRecordsToDatabase(
					new InputStreamReader(new ByteArrayInputStream(convertToCsvBytes(testData3a))),
					imp);
			int i = 0;
			for (FieldValueStringList[] row: convertToFVSL(testData3b)) {
				checkFields(row, recs[i]);
				++i;
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception");
		}
	}
	
	@Override
	protected void setUp() throws Exception {
		testDatabase = new DiagnosticDatabase(Utilities.copyTestDatabaseFile(Utilities.TEST_DB1_XML_FILE));
		testDatabase.open();
		valueTranslations = new HashMap<String, String>();
		valueTranslations.put("ACM", "NS_acm");
		valueTranslations.put("IEEE", "NS_ieee");
		db1Mgr = testDatabase.getFileMgr().makeAccessManager(getName(), 
				new File(Utilities.getTestDataDirectory(), 
				Utilities.TEST_DB1_IMPORT_FILE));
	}

	@Override
	protected void tearDown() throws Exception {
		db1Mgr.close();
		testDatabase.close();
		Utilities.deleteTestDatabaseFiles(Utilities.TEST_DB1_XML_FILE);
	}

	private byte[] convertToCsvBytes(final String[][][] testData1)
			throws UserErrorException {
		StringBuilder sb = new StringBuilder();
		String rowSep = "";
		for (String[][] row: testData1) {
			sb.append(rowSep);
			rowSep = "\n";
			ArrayList<FieldValueStringList> fieldList = new ArrayList<FieldValueStringList>();
			boolean firstColumn = true;
			for (String[] col: row) {
				if (!firstColumn) {
					sb.append(CSV_COLUMN_SEP);
				}
				firstColumn = false;
				String valSep = "";
				for (String value: col) {
					sb.append(valSep);
					sb.append(value);
					valSep = "\u000B";
				}
				FieldValueStringList fieldValues = new FieldValueStringList(col);
				fieldList.add(fieldValues);
			}
		}
		byte[] csvBytes = sb.toString().getBytes(); //
		return csvBytes;
	}

	private ArrayList<FieldValueStringList[]> convertToFVSL(final String[][][] testData1)
	throws UserErrorException {
		ArrayList<FieldValueStringList[]> rowList = new ArrayList<FieldValueStringList[]>();
		for (String[][] row: testData1) {
			ArrayList<FieldValueStringList> fieldList = new ArrayList<FieldValueStringList>();
			for (String[] col: row) {
				FieldValueStringList fieldValues = new FieldValueStringList(col);
				fieldList.add(fieldValues);
			}
			rowList.add(fieldList.toArray(new FieldValueStringList[fieldList.size()]));
		}
		return rowList;
}

	private String translateStringToValue(String original) {
		String valueString = valueTranslations.get(original);
		return (null == valueString)? original: valueString;
	}

	private void checkFields(FieldValueStringList[] row, Record r)
			throws FieldDataException {
		int fi = 0;
		for (Field f:r.getFields()) {
			int vi = 0;
			for (FieldValue v: f.getFieldValues()) {
				String vs = translateStringToValue(v.getMainValueAsString());
				assertEquals("mismatch in field "+fi, row[fi].getValue(vi), vs);
				++vi;
			}
			++fi;
		}
	}

	String testData1[][][] = {
			{{"ID_auth_1"},
				{"ID_authors_1"},
				{"ID_title_1"},
				{"ID_pages_1"},
				{"NS_acm"},
				{"ID_keywords_1"},
				{"ID_abstract_1"},
			{"true"}},
			{{"ID_auth_2"},
				{"ID_authors_2"},
				{"ID_title_2"},
				{"ID_pages_2"},
				{"NS_ieee"},
				{"ID_keywords_2"},
				{"ID_abstract_2"},
			{"false"}
			}
	
	};
	String testData2b[][][] = {
			{{"ID_auth_1"},
				{"ID_authors_1"},
				{"ID_title_1"},
				{"ID_pages_1"},
				{"NS_acm"},
				{"ID_keywords_1"},
				{"ID_abstract_1"},
			{"true"}},
			{{"ID_auth_2"},
				{"ID_authors_2"},
				{"ID_title_2"},
				{"ID_pages_2"},
				{"NS_ieee"},
				{"ID_keywords_2"},
				{"ID_abstract_2"},
			{"true"}
			}
	
	};
	String testData2a[][][] = {
			{{"ID_auth_1"},
				{"ID_authors_1"},
				{"ID_title_1"},
				{"ID_pages_1"},
				{"ACM"},
				{"ID_keywords_1"},
				{"ID_abstract_1"},
			{"true"}},
			{{"ID_auth_2"},
				{"ID_authors_2"},
				{"ID_title_2"},
				{"ID_pages_2"},
				{"IEEE"},
				{"ID_keywords_2"},
				{"ID_abstract_2"},
			{"false"}
			}
	};
	
			String testData3a[][][] = {
					{{"ID_auth_1"},
						{"ID_authors_1"},
						{"ID_title:1"},
						{"ID_pages_1"},
						{"foo"},
						{"ID_keywords_1"},
						{"ID_abstract_1"},
					{"true"}},
					{{"ID_auth_2"},
						{"ID_authors_2"},
						{"ID_title_2"},
						{"ID_pages_2"},
						{""},
						{"ID_keywords_2"},
						{"ID_abstract_2"},
					{"false"}
					}
			};
			String testData3b[][][] = {
					{{"ID_auth_1"},
						{"ID_authors_1"},
						{"ID_title:1"},
						{"ID_pages_1"},
						{"foo"},
						{"ID_keywords_1"},
						{"ID_abstract_1"},
					{"true"}},
					{{"ID_auth_2"},
						{"ID_authors_2"},
						{"ID_title_2"},
						{"ID_pages_2"},
						{"ID_keywords_2"},
						{"ID_abstract_2"},
					{"true"}
					}
			};
			private FileAccessManager db1Mgr;
}
