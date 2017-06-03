package server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.wltea.analyzer.lucene.IKAnalyzer;

import index.type.FileIndex;

public class Lighter {
	private int MAXLEN = 100;
	
	private Analyzer analyzer;
	private Highlighter highlighter;
	private String entry, abst;
	private int len = 0;
	
	public Lighter(String queryString, Document doc, boolean flag){
		entry = doc.get("entry");
		Document con = FileIndex.getDocument(new File(doc.get("path")));
		abst = con.get("body") != null ? con.get("body") : con.get("text");
		analyzer = new IKAnalyzer(flag);
		SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<em>", "</em>");
		try {
			Query query = new QueryParser("", analyzer).parse(queryString); 
			QueryScorer scorer = new QueryScorer(query);
			highlighter = new Highlighter(htmlFormatter, scorer);
			len = (int)(2.0 * MAXLEN * abst.length() / abst.getBytes().length);
			highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer, len));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static public List<String> getWords(String text, boolean flag){
		List<String> result = new ArrayList<String>();  
		TokenStream stream = null;
		Analyzer analyzer = new IKAnalyzer(flag);
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
		analyzer.close();
	    return result; 
	}
	
	static public String cut(String str, int limit){
		int len = str.getBytes().length;
		return len < limit ? str : str.substring(0, limit) + "...";
	}
	
	public String getEntry(){
		TokenStream tokenStream = analyzer.tokenStream("", entry);
		try {
			String css = highlighter.getBestFragment(tokenStream, entry);
			return css == null ? entry : css;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String getAbst(){
		TokenStream tokenStream = analyzer.tokenStream("", abst);
		try {
			String css = highlighter.getBestFragment(tokenStream, abst);
			return (css == null ? cut(abst, len) : css) + (abst.length() > len ? "..." : "");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static public void main(String[] args){
		for(String x : Lighter.getWords("大学新闻", false))
			System.out.println(x);
	}
}
