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
	
	public Lighter(String queryString, Document doc, boolean flag){
		analyzer = new IKAnalyzer(flag);
		SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<em>", "</em>");
		try {
			Query query = new QueryParser("", analyzer).parse(queryString); 
			QueryScorer scorer = new QueryScorer(query);
			highlighter = new Highlighter(htmlFormatter, scorer);
			highlighter.setTextFragmenter(new SimpleSpanFragmenter(scorer, MAXLEN));
		} catch (Exception e) {
			e.printStackTrace();
		}
		entry = doc.get("entry");
		Document con = FileIndex.getDocument(new File(doc.get("path")));
		if(con != null)
			abst = con.get("body") != null ? con.get("body") : con.get("text");
//		System.out.println(entry + " " + abst);
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
	
	public String getEntry(){
		TokenStream tokenStream = analyzer.tokenStream("", entry);
		try {
			String css = highlighter.getBestFragment(tokenStream, entry);
//			System.out.println(entry + ": " + css);
			String end = entry.length() > MAXLEN ? "..." : "";
			if(css == null)
				css = entry.length() > MAXLEN ? entry.substring(0, MAXLEN) : entry;
			return css + end;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String getAbst(){
		TokenStream tokenStream = analyzer.tokenStream("", abst);
		try {
			String css = highlighter.getBestFragment(tokenStream, abst);
//			System.out.println(abst + ": " + css);
			String end = abst.length() > MAXLEN ? "..." : "";
			if(css == null)
				css = abst.length() > MAXLEN ? abst.substring(0, MAXLEN) : abst;
			return css + end;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static public void main(String[] args){
		for(String x : Lighter.getWords("大学新闻", true))
			System.out.println(x);
	}
}
