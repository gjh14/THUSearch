package index;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.jsoup.Jsoup;

public class HtmlIndex {
	public static Document getDocument(File file){
		try {
			Document document = new Document();
			org.jsoup.nodes.Document htmlDoc;
			htmlDoc = Jsoup.parse(file, Detector.fileCode(file), "");
			String title = htmlDoc.title();
			if(title != null)
				document.add(new TextField("title", title, Field.Store.YES));
			if(htmlDoc.body() != null){
				String body = htmlDoc.body().text();
				if(body != null)
					document.add(new TextField("body", body, Field.Store.YES));
			}
			return document;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
