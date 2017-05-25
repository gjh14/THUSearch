package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class Lighter {
	private int MAXLEN = 100;
	
	private Analyzer analyzer;
	private Highlighter highlighter;
	private String tag, abs;
	
	public Lighter(String queryString, Document doc){
		analyzer = new IKAnalyzer();
		SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter(  
				"<font color=\"#FF0000\">", "</font>");
		try {
			Query query = new QueryParser("", analyzer).parse(queryString); 
			QueryScorer scorer = new QueryScorer(query);
			highlighter = new Highlighter(htmlFormatter, scorer);
			highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer, MAXLEN));
		} catch (Exception e) {
			e.printStackTrace();
		}
		String name = doc.get("name");
		String[] token = name.split("\\.");
		if(token[token.length - 1].equals("html")){
			tag = doc.get("title");
			abs = doc.get("body");
		}else{
			tag = name;
			abs = doc.get("text");
		}
	}
	
	public List<String> getWords(String text){
		List<String> result = new ArrayList<String>();  
		TokenStream stream = null;  
		try {  
			stream = analyzer.tokenStream("", text);  
	        CharTermAttribute attr = stream.addAttribute(CharTermAttribute.class);  
	        stream.reset();  
	        while(stream.incrementToken()){  
	            result.add(attr.toString());  
	        }  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }finally{  
	        if(stream != null){  
	            try {  
	                stream.close();  
	            } catch (IOException e) {  
	                e.printStackTrace();  
	            }  
	        }  
	    }  
	    return result; 
	}
	
	public String getTag(){
		TokenStream tokenStream = analyzer.tokenStream("", tag);
		try {
			String css = highlighter.getBestFragment(tokenStream, tag);
			System.out.println(tag + ": " + css);
			String end = tag.length() > MAXLEN ? "..." : "";
			if(css == null)
				css = tag.length() > MAXLEN ? tag.substring(0, MAXLEN) : tag;
			return css + end;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String getAbs(){
		TokenStream tokenStream = analyzer.tokenStream("", abs);
		try {
			String css = highlighter.getBestFragment(tokenStream, abs);
			System.out.println(abs + ": " + css);
			String end = abs.length() > MAXLEN ? "..." : "";
			if(css == null)
				css = abs.length() > MAXLEN ? abs.substring(0, MAXLEN) : abs;
			return css + end;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static public void main(String[] args){
		Document doc = new Document();
		doc.add(new StringField("file", "tmp.html", Field.Store.YES));
		doc.add(new TextField("title", "大家好", Field.Store.YES));
		doc.add(new TextField("body", "清华大学", Field.Store.YES));
		Lighter li = new Lighter("华大", doc);
		System.out.println(li.getTag() + " " + li.getAbs());
	}
}
