package org.lasalledebain;

import java.util.Arrays;
import java.util.Iterator;

import junit.framework.TestCase;

import org.lasalledebain.libris.Field;
import org.lasalledebain.libris.FieldTemplate;
import org.lasalledebain.libris.exception.FieldDataException;
import org.lasalledebain.libris.exception.InternalError;
import org.lasalledebain.libris.field.FieldValue;


public class FieldTest extends TestCase {
	private static final String DEFAULT_DATA = "defaultData";
	public void testBoolean() {
		FieldTemplate ft = new FieldTemplate("f1", "Field 1", Field.FieldType.T_FIELD_BOOLEAN);
		try {
			Field f = ft.newField();
			try {
				f.addValue("true");
				String fd = f.getFirstFieldValue().getValueAsString();
				assertTrue("wrong result for true boolean", fd.equalsIgnoreCase("true"));
				f.changeValue("false");
				fd = f.getFirstFieldValue().getValueAsString();
				assertTrue("wrong result for true boolean", fd.equalsIgnoreCase("false"));
				boolean excThrown = false;
				try {
					f.addValue("foobar");
				} catch (FieldDataException e) {
					excThrown = true;
				}
				assertTrue("no exception on bad data", excThrown);
			} catch (FieldDataException e) {
				fail("fieldDataException");
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	public void testReadOnlyView() {
		try {
			FieldTemplate ft = Utilities.createTemplate("f1", Field.FieldType.T_FIELD_INTEGER);
			Field f = ft.newField();
			f.addIntegerValue(42);
			assertFalse("regular view  read-only", f.isReadOnly());
			FieldValue v = f.getFirstFieldValue();
			assertEquals("wrong value for integer field", 42, v.getValueAsInt());
			Field rof = f.getReadOnlyView();
			v = rof.getFirstFieldValue();
			assertEquals("wrong value for read-only field", 42, v.getValueAsInt());
			assertTrue("read-only view not read-only", rof.isReadOnly());
			boolean thrown = false;
			try {
				rof.addIntegerValue(99);
			} catch (FieldDataException e) {
				thrown = true;
			}
			assertTrue("No error addIntegerValue read-only view", thrown);

			thrown = false;
			try {
				rof.setValues(new FieldValue[0]);
			} catch (InternalError e) {
				thrown = true;
			}
			assertTrue("No error setValues(array) read-only view", thrown);


			thrown = false;
			try {
				rof.setValues(Arrays.asList((new FieldValue[0])));
			} catch (InternalError e) {
				thrown = true;
			}
			assertTrue("No error setValues(iterator) read-only view", thrown);

			thrown = false;
			try {
				rof.addValue("foo");
			} catch (FieldDataException e) {
				thrown = true;
			}
			assertTrue("No error addvalue(string) read-only view", thrown);

			thrown = false;
			try {
				rof.addValuePair(1, "foo");
			} catch (FieldDataException e) {
				thrown = true;
			}
			assertTrue("No error addvaluepair read-only view", thrown);

			thrown = false;
			try {
				rof.addValuePair("foo", "bar");
			} catch (FieldDataException e) {
				thrown = true;
			}
			assertTrue("No error addvalue(string, string) read-only view", thrown);

			thrown = false;
			try {
				rof.removeValue();
			} catch (InternalError e) {
				thrown = true;
			}
			assertTrue("No error removeValue read-only view", thrown);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}
	public void testInteger() {
		FieldTemplate ft = Utilities.createTemplate("f1", Field.FieldType.T_FIELD_INTEGER);
		try {
			int [] testData = {0, -1, 1, 1234, -5678};
			for (int i: testData) {
				Field f = ft.newField();
				String testString = Integer.toString(i);
				f.addValue(testString);
				FieldValue firstFieldValue = f.getFirstFieldValue();
				int d = firstFieldValue.getValueAsInt();
				assertEquals("mismatch in int data "+testString, i, d);
				String s = firstFieldValue.getValueAsString();
				assertEquals("mismatch in string data", testString, s);
			}
			boolean excThrown = false;
			try {
				Field f = ft.newField();
				f.addValue("foobar");
			} catch (FieldDataException e) {
				excThrown = true;
			}
			assertTrue("no exception on bad data", excThrown);
		} catch (FieldDataException e) {
			fail("fieldDataException");
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

	}
	public void testRange() {
		FieldTemplate ft = Utilities.createTemplate("f1", Field.FieldType.T_FIELD_PAIR);
		try {
			Field f = ft.newField();
			try {
				String[][] expectedData = {{"", ""}, {"testString1", ""}, {"2","3"}};
				String[] d = getValuesAsStringPair(f);
				checkValues(expectedData[0], d);
				
				f.addValue(expectedData[1][0]);
				d = getValuesAsStringPair(f);
				checkValues(expectedData[1], d);
				
				f.removeValue();
				f.addValuePair(expectedData[2][0], expectedData[2][1]);
				d = getValuesAsStringPair(f);
				checkValues(expectedData[2], d);

			} catch (Exception e) {
				e.printStackTrace();
				fail("unexpected exception "+e.getClass().getName()+": "+e.getMessage());
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	private String[] getValuesAsStringPair(Field f) {
		FieldValue v = f.getFirstFieldValue();
		String[] pair = new String[2];
		try {
			pair[0] = v.getMainValueAsString();
		} catch (FieldDataException e) {
			e.printStackTrace();
			fail();
		}
		pair[1] = v.getExtraValueAsString();
		return pair;
	}
	private void checkValues(String[] expectedData, String[] d) {
		assertEquals("wrong number of elements ", expectedData.length, d.length);
		for (int j = 0; j < d.length; ++j) {
			assertEquals("mismatch in range value: ", expectedData[j], d[j]);
		}
	}
		
	public void testStringField() {
		FieldTemplate ft = Utilities.createTemplate("f1", Field.FieldType.T_FIELD_STRING);
		try {
			Field f = ft.newField();
			try {
				String [] testData = {"The", " quick brown ", "fox jumped\nover the lazy dog"};

				for (String testString: testData) {

					f.addValue(testString);
					String d = f.getFirstFieldValue().getValueAsString();
					assertEquals("wrong result for "+testString, d, testString);
					FieldValue r = f.removeValue();
					if (null != r) {
						assertEquals("wrong result for removed value "+testString, r.getValueAsString(), testString);
					}

				}
			} catch (FieldDataException e) {
				fail("fieldDataException");
			} catch (Exception e) {
				e.printStackTrace();
				fail();
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	public void testEnumField() {
		FieldTemplate ft = Utilities.createTemplate("f1", Field.FieldType.T_FIELD_ENUM);
		String[][] valueNames = {{"toe"}, {"foo", "bar"}, {"bob", "carol", "ted", "alice"}};
		for (int valueSet = 0; valueSet < valueNames.length; ++valueSet) {
			ft.setEnumChoices(new MockEnumFieldChoices(valueNames[valueSet]));
			setAndCheckEnumData(ft, valueNames[valueSet]);
		}
	}
	
	public void testMultipleStringValues() {
		FieldTemplate ft = Utilities.createTemplate("tmsv", Field.FieldType.T_FIELD_STRING);
		try {
			Field f = ft.newField();
			try {
				String [] testData = {"one", "two", "three"};
				for (String testString: testData) {

					f.addValue(testString);
				}
				int valueCount = f.getNumberOfValues();
				assertEquals("wrong number of values", testData.length, valueCount);
				 Iterator<FieldValue> vi = f.getFieldValues().iterator();
				int i = 0;
				while (vi.hasNext()) {
					String d = vi.next().getValueAsString();
					assertEquals("wrong result for "+testData[i], d, testData[i]);
					++i;
				}
			} catch (Exception e) {
				e.printStackTrace();
				fail("unexpected exception "+e.getClass().getName()+" "+e.getMessage());
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private void setAndCheckEnumData(FieldTemplate ft, String[] valueNames) {
		try {
			Field f = ft.newField();
			for (int i = 0; i < valueNames.length; ++i) {
				f.addValue(valueNames[i]);
				FieldValue v = f.getFirstFieldValue();
				assertEquals("enum index", i, v.getValueAsInt());
				assertEquals("enum name", valueNames[i], v.getValueAsString());
				f.removeValue();
			}
			boolean excThrown = false;
			try {
				f.addValue("foobar");
			} catch (FieldDataException e) {
				excThrown = true;
			}
			assertTrue("no exception on bad data", excThrown);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
