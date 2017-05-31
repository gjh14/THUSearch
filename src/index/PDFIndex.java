package index;

import java.io.File;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

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
				document.add(new TextField("text", linked, Field.Store.NO));
			}
			pdf.close();
			return document;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args){
		File file = new File("mirror/pdf/doc_2015.pdf");
		Document doc = getDocument(file);
		System.out.println(doc.get("text"));
	}
}
