package index;

import java.io.File;
import java.io.FileInputStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.poi.hwpf.HWPFDocument;


public class DocIndex {
	public static Document getDocument(File file){
		try {
			Document document = new Document();
			FileInputStream in = new FileInputStream(file);
			HWPFDocument doc = new HWPFDocument(in);
			String text = doc.getDocumentText();
			if(text != null){
				String trans = new String(text.getBytes(), Detector.textCode(text)).replaceAll("\\?", "");
				document.add(new TextField("text", trans, Field.Store.NO));
			}
			doc.close();
			return document;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args){
		File file = new File("mirror/doc/G172560075.doc");
		Document doc = getDocument(file);
		System.out.println(doc.get("text"));
	}
}
