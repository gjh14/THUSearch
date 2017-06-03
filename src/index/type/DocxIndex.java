package index.type;

import java.io.File;
import java.io.FileInputStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;


public class DocxIndex {
	public static Document getDocument(File file){
		try {
			Document document = new Document();
			FileInputStream in = new FileInputStream(file);
			XWPFDocument docx = new XWPFDocument(in);
			XWPFWordExtractor extractor = new XWPFWordExtractor(docx);
			String text = extractor.getText();
			if(text != null){
				String linked = FileIndex.link(text);
				document.add(new TextField("text", linked, Field.Store.NO));
			}
			extractor.close();
			return document;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args){
		File file = new File("D:/workspace/mirror__4/news.tsinghua.edu.cn/"
				+ "publish/thunewsen/9707/20160107/1475140069900.docx");
		Document doc = getDocument(file);
		System.out.println(doc.get("text"));
	}
}
