package org.lasalledebain;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;

public class PdfTests extends TestCase {
PDFTextStripper stripper;
	public void testGetText() {
		File pdfFile = new File("/Users/pdbain/Documents/publications/chen_aberer_1999.pdf");
		try {
			PDDocument doc = PDDocument.load(pdfFile);
			int nPages = doc.getNumberOfPages();
			PDDocumentInformation docInfo = doc.getDocumentInformation();
			System.out.println("author"+docInfo.getAuthor());
			System.out.println("keywords"+docInfo.getKeywords());
			System.out.println("subject"+docInfo.getSubject());
			Set<String> mdKeys = docInfo.getMetadataKeys();
			for (String k: mdKeys) {
				System.out.println(docInfo.getCustomMetadataValue(k));
			}

			stripper = new PDFTextStripper();
			final ByteArrayOutputStream outputBuff = new ByteArrayOutputStream();
			OutputStreamWriter fileWriter = new OutputStreamWriter(outputBuff);
			
			stripper.writeText(doc, fileWriter);
			
			System.out.println(outputBuff);
			doc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
