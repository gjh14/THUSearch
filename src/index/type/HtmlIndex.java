package index.type;

import java.io.File;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlIndex {
	public static Document getDocument(File file){
		try {
			Document doc = new Document();
			org.jsoup.nodes.Document htmlDoc;
			htmlDoc = Jsoup.parse(file, "utf-8", "");
			String title = htmlDoc.title();
			if(title != null)
				doc.add(new TextField("title", title, Field.Store.NO));
			if(htmlDoc.body() != null){
				Element body = htmlDoc.body();				
				body.select("header").remove();
				body.select("footer").remove();
				String text = body.text();
				doc.add(new TextField("body", text, Field.Store.NO));
				
				Elements imgs = body.getElementsByTag("img");
				for(Element img : imgs){
					String src = img.attr("src");
					if(!src.equals("")){
						doc.add(new StringField("img", src, Field.Store.YES));
						break;
					}
				}
			}
			String keywords = "";
			Elements metas = htmlDoc.getElementsByTag("meta");
			for (Element link : metas) {
				String name = link.attr("name");
				String content = link.attr("content");
				if(name.equals("keywords"))
					keywords += content + " ";
			}
			doc.add(new TextField("keywords", keywords, Field.Store.NO));
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args){
		String path = "D:/workspace/mirror__4/news.tsinghua.edu.cn/publish"
				+ "/thunewsen/9707/index.html";
		Document doc = HtmlIndex.getDocument(new File(path));
		System.out.println(doc.get("body") + "\n" + doc.get("img"));
	}
}
