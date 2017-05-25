package index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.pdfbox.contentstream.PDContentStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class PDFIndex{
	public static Document getDocument(File file){
		try {
			Document document = new Document();
			PDDocument pdf = PDDocument.load(file);
			
			PDFTextStripper stripper = new PDFTextStripper();
			String text = stripper.getText(pdf);
			if(text != null){
				String trans = new String(text.getBytes(), Detector.textCode(text));
				String linked = trans.replaceAll("\\?", "")
						.replaceAll("\n", "")
						.replaceAll("\r", "");
				System.out.println(linked);
				document.add(new TextField("text", linked, Field.Store.YES));
			}
			pdf.close();
			return document;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args){
		File file = new File("mirror/pdf/doc_2015.pdf");
		getDocument(file);
	}
}
