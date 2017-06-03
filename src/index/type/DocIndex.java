package index.type;

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
				String linked = FileIndex.link(text);
				document.add(new TextField("text", linked, Field.Store.YES));
			}
			doc.close();
			return document;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args){
		File file = new File("D:/workspace/mirror__4/news.tsinghua.edu.cn/"
				+ "publish/thunewsen/9707/20160107/58eb365878953.doc");
		Document doc = getDocument(file);
		System.out.println(doc.get("text"));
	}
}
