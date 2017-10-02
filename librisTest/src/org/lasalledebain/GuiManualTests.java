package org.lasalledebain;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.lasalledebain.libris.EnumFieldChoices;
import org.lasalledebain.libris.Record;
import org.lasalledebain.libris.RecordTemplate;
import org.lasalledebain.libris.Schema;
import org.lasalledebain.libris.exception.DatabaseException;
import org.lasalledebain.libris.exception.InputException;
import org.lasalledebain.libris.exception.XmlException;
import org.lasalledebain.libris.ui.EnumField;
import org.lasalledebain.libris.ui.GuiControl;
import org.lasalledebain.libris.ui.HeadlessUi;
import org.lasalledebain.libris.ui.Layout;
import org.lasalledebain.libris.ui.Layouts;
import org.lasalledebain.libris.ui.LibrisWindowedUi;
import org.lasalledebain.libris.ui.RecordWindow;
import org.lasalledebain.libris.ui.TextBox;
import org.lasalledebain.libris.xmlUtils.ElementManager;


public class GuiManualTests extends TestCase {
	private static final String LAYOUT1 = "layout1";
	private static final String LAYOUT1A = "layout1a";
	private Schema mySchema;
	private Layout myGuiLayout;
	private static LibrisWindowedUi myUi = new HeadlessUi();

	public void testWindowSanity() {
		JFrame frame = new JFrame("testWindowSanity");

		GridBagLayout panelLayout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		frame.setSize(40, 100);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel recordPanel = new JPanel(panelLayout);

		c.gridx = 0; c.gridy = 0;
		JTextArea f1 = new JTextArea("test data", 10, 25);

		JPanel p = new JPanel();
		p.setLayout(new FlowLayout());
		JLabel l = new JLabel("field");
		l.setLabelFor(f1);
		p.add(l);
		l.setVerticalTextPosition(SwingConstants.TOP);
		TitledBorder bord = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "My title", TitledBorder.LEFT, TitledBorder.TOP);
		f1.setBorder(bord);
		p.add(f1);
		panelLayout.setConstraints(p, c);
		recordPanel.add(p);
		p.setVisible(true);

		JScrollPane recordPane = new JScrollPane(recordPanel);
		frame.getContentPane().add(recordPane);
		recordPane.setVisible(true);
		frame.setVisible(true);
		guiPause(0);
		frame.dispose();
	}

	public void testControlSanity() {
		JFrame frame = new JFrame("testWindowSanity");

		GridBagLayout panelLayout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		frame.setSize(40, 100);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel recordPanel = new JPanel(panelLayout);

		c.gridx = 0; c.gridy = 0;
		GuiControl f1 = new TextBox(10, 25);

		JPanel p = new JPanel();
		p.setLayout(new FlowLayout());
		p.add(f1.getGuiComponent());
		panelLayout.setConstraints(p, c);
		recordPanel.add(f1.getGuiComponent());
		p.setVisible(true);

		JScrollPane recordPane = new JScrollPane(recordPanel);
		frame.getContentPane().add(recordPane);
		recordPane.setVisible(true);
		frame.setVisible(true);
		guiPause(0);
		frame.dispose();
	}

	public void testEnumSanity() {
		JFrame frame = new JFrame("testEnumSanity");

		GridBagLayout panelLayout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		frame.setSize(40, 100);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel recordPanel = new JPanel(panelLayout);

		c.gridx = 0; c.gridy = 0;
		GuiControl f1 = new EnumField(10, 25);
		String[] enumValues = new String[] {"one", "two", "three"};
		EnumFieldChoices choices = new EnumFieldChoices();
		try {
			choices.addChoices(enumValues);
		} catch (DatabaseException e) {			
			e.printStackTrace();
			fail("Unexpected exception");
		}
		f1.setLegalValues(choices);

		recordPanel.add(f1.getGuiComponent());

		JScrollPane recordPane = new JScrollPane(recordPanel);
		frame.getContentPane().add(recordPane);
		recordPane.setVisible(true);
		frame.setVisible(true);
		guiPause(0);
		frame.dispose();
	}

	public void testFieldTypes() {
		File testDir = Utilities.getTestDataDirectory();
		File layoutFile = new File(testDir, Utilities.LAYOUT_DECLARATIONS2_XML_FILE);
		File recordFile = new File(testDir, Utilities.TEST_RECORD2_XML_FILE);
		File schemaFile = new File(testDir, Utilities.TEST_SCHEMA2_XML_FILE);
		try {
			Schema schem = Utilities.loadSchema(schemaFile);
			Layouts myLayouts = Utilities.loadLayoutsFromXml(schem, layoutFile);
			Record rec = Utilities.loadRecordFromXml(schemaFile, recordFile);
			myGuiLayout = myLayouts.getLayout(Utilities.LAYOUT2);
			assertNotNull("could not load "+Utilities.LAYOUT2, myGuiLayout);
			RecordWindow rWindow = new RecordWindow(myUi, myGuiLayout, rec, new Point(100, 200), false, null);
			rWindow.setVisible(true);
			JFrame frame = new JFrame(getName());
			frame.add(rWindow);
			frame.setVisible(true);
			guiPause(0);
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception: "+e);
		}
	}

	public void testFieldTypes2() {
		File testDir = Utilities.getTestDataDirectory();
		File layoutFile = new File(testDir, Utilities.LAYOUT_DECLARATIONS2_XML_FILE);
		File recordFile = new File(testDir, Utilities.TEST_RECORD2_XML_FILE);
		File schemaFile = new File(testDir, Utilities.TEST_SCHEMA2_XML_FILE);
		try {
			Schema schem = Utilities.loadSchema(schemaFile);
			Layouts myLayouts = Utilities.loadLayoutsFromXml(schem, layoutFile);
			Record rec = Utilities.loadRecordFromXml(schemaFile, recordFile);
			try {
				myGuiLayout = (Layout) myLayouts.getLayout(Utilities.LAYOUT2);
			} catch (ClassCastException e) {
				fail("Cannot cast layout2 to FormLayout");
			}
			assertNotNull("could not load "+Utilities.LAYOUT2, myGuiLayout);
			RecordWindow rWindow = new RecordWindow(myUi, myGuiLayout, rec, new Point(100, 200), null);
			JFrame frame = new JFrame(getName());
			frame.add(rWindow);
			rWindow.setVisible(true);
			frame.setVisible(true);
			guiPause(0);
			frame.dispose();
		} catch (FileNotFoundException e) {
			fail(layoutFile.getAbsolutePath()+" not found");
		} catch (DatabaseException e) {
			e.printStackTrace();
			fail(layoutFile.getAbsolutePath()+" XMLStreamException "+e.getMessage());
		} catch (XmlException e) {
			fail(layoutFile.getAbsolutePath()+" InputDataException "+e.getMessage());
			e.printStackTrace();
		} catch (XMLStreamException e) {
			fail(layoutFile.getAbsolutePath()+" InputDataException "+e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	public void testRecordWindowSanity() {
		loadSchema();
		RecordTemplate template = null;
		try {
			template = RecordTemplate.templateFactory(mySchema);
			Record rec = template.makeRecord(true);
			Layouts myLayouts = loadFromLayout(rec);
			try {
				myGuiLayout = (Layout) myLayouts.getLayout(LAYOUT1);
			} catch (ClassCastException e) {
				fail("Cannot cast layout1 to FormLayout");
			}
			assertNotNull("did not load "+LAYOUT1, myGuiLayout);
			RecordWindow rWindow;
			try {
				rWindow = new RecordWindow(myUi, myGuiLayout, rec, false, null);
				rWindow.setVisible(true);
				guiPause();
			} catch (Exception e) {
				e.printStackTrace();
				fail();
			}
		} catch (InputException e1) {
			e1.printStackTrace();
			fail("exception creating record template");
		}
	}

	public void testMultipleFieldValues() {
		loadSchema();
		RecordTemplate template = null;
		try {
			template = RecordTemplate.templateFactory(mySchema);
			Record rec = template.makeRecord(true);
			Layouts myLayouts = loadFromLayout(rec);
			try {
				myGuiLayout = (Layout) myLayouts.getLayout(LAYOUT1);
			} catch (ClassCastException e) {
				fail("Cannot cast layout1 to FormLayout");
			}
			assertNotNull("did not load "+LAYOUT1, myGuiLayout);
			RecordWindow rWindow;
			try {
				rWindow = new RecordWindow(myUi, myGuiLayout, rec, false, null);
				rWindow.setVisible(true);
				guiPause();
			} catch (Exception e) {
				e.printStackTrace();
				fail();
			}
		} catch (InputException e1) {
			e1.printStackTrace();
			fail("exception creating record template");
		}
	}

	private void guiPause() {
		guiPause(2000);
	}

	void guiPause(int delay) {
		if (delay > 0)  {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				System.err.println("testcase interrupted");
			}
		} else {
			System.out.print("waiting...");
			try {
				System.in.read();
			} catch (IOException e) {
				return;
			}
		}
	}

	private Layouts loadFromLayout(Record rec) {
		File testDir = Utilities.getTestDataDirectory();
		File inputFile = new File(testDir, Utilities.LAYOUT_DECLARATIONS_XML_FILE);
		try {
			Layouts myLayouts = new Layouts(mySchema);
			ElementManager mgr = Utilities.makeElementManagerFromFile(inputFile, "layouts");
			loadSchema();
			myLayouts.fromXml(mgr);
			return myLayouts;
		} catch (DatabaseException e) {} catch (XmlException e) {
			fail(inputFile.getAbsolutePath()+" InputDataException "+e.getMessage());
			e.printStackTrace();
		} catch (InputException e) {

			e.printStackTrace();
			String msg = e.getMessage();
			fail(inputFile.getAbsolutePath()+" XMLStreamException "+msg);
		}
		return null;
	}

	public void testGuiLayoutXml() {
		File testDir = Utilities.getTestDataDirectory();
		File inputFile = new File(testDir, Utilities.LAYOUT_DECLARATIONS_XML_FILE);
		try {
			loadSchema();
			Layouts myLayouts = Utilities.loadLayoutsFromXml(mySchema, inputFile);
			String ids[] = myLayouts.getLayoutIds();
			System.err.print("Available ids: ");
			for (String s: ids) {
				System.err.print(s+" ");
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception: "+e);
		}

	}


	public void testDisplayRecord() {
		File testDir = Utilities.getTestDataDirectory();
		File layoutFile = new File(testDir, Utilities.LAYOUT_DECLARATIONS_XML_FILE);
		File recordFile = new File(testDir, Utilities.TEST_RECORD1_XML_FILE);
		File schemaFile = new File(testDir, Utilities.TEST_SCHEMA_XML_FILE);
		try {
			Schema schem = Utilities.loadSchema(schemaFile);
			Layouts myLayouts = Utilities.loadLayoutsFromXml(schem, layoutFile);
			Record rec = Utilities.loadRecordFromXml(schemaFile, recordFile);
			try {
				myGuiLayout = myLayouts.getLayout(LAYOUT1A);
			} catch (ClassCastException e) {
				fail("Cannot cast layout2 to FormLayout");
			}
			assertNotNull("could not load "+LAYOUT1A, myGuiLayout);
			RecordWindow rWindow = new RecordWindow(myUi, myGuiLayout, rec, new Point(100, 200), null);
			JFrame frame = new JFrame(getName());
			frame.add(rWindow);
			rWindow.setVisible(true);
			frame.setVisible(true);
			guiPause(0);
			frame.dispose();
		} catch (Exception e) {
			e.printStackTrace();
			fail("unexpected exception: "+e);
		} catch (Error e) {
			e.printStackTrace();
			fail();
		}



	}

	void loadSchema()  {

		File testDir = Utilities.getTestDataDirectory();
		File inputFile = new File(testDir, "schema.xml");
		try {
			mySchema = Utilities.loadSchema(inputFile);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
