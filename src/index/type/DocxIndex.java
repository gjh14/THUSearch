package index.type;

import java.io.File;
import java.io.FileInputStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import index.Detector;


public class DocxIndex {
	public static Document getDocument(File file){
		try {
			Document document = new Document();
			FileInputStream in = new FileInputStream(file);
			XWPFDocument docx = new XWPFDocument(in);
			XWPFWordExtractor extractor = new XWPFWordExtractor(docx);
			String text = extractor.getText();
			if(text != null){
				String trans = new String(text.getBytes(), Detector.textCode(text));
				document.add(new TextField("text", trans, Field.Store.NO));
			}
			extractor.close();
			return document;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args){
		File file = new File("mirror/docx/58eb20bc821f3.docx");
		Document doc = getDocument(file);
		System.out.println(doc.get("text"));
	}
}
