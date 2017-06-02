package search;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import index.WebIndex;

public class WebSearch {
	private IndexReader reader;
	private IndexWriter writer;
	private IndexSearcher searcher;
	
	private static String[] field = new String[]{"title", "body", "text", "archor", "keywords"};
	static private Map<String, Float> boosts;
	static{
		boosts = new HashMap<String, Float>();
		boosts.put("title", 2.0f);
		boosts.put("body", 1.0f);
		boosts.put("text", 1.0f);
		boosts.put("archor", 4.0f);
		boosts.put("keywords", 4.0f);
	}

	public WebSearch(){		
		try{
			Directory dir = FSDirectory.open(new File(WebIndex.INDEXDIR).toPath());
			reader = DirectoryReader.open(dir);
			searcher = new IndexSearcher(reader);
			searcher.setSimilarity(new BM25Similarity());
			
			IndexWriterConfig iwc = new IndexWriterConfig(new IKAnalyzer(false));
			iwc.setSimilarity(new BM25Similarity());
			writer = new IndexWriter(dir,iwc);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public TopDocs searchQuery(String queryString, boolean flag){
		System.out.println("query=" + queryString);
		MultiFieldQueryParser parser = new MultiFieldQueryParser(field, new IKAnalyzer(flag), boosts);
		try {
			Query normalQuery = parser.parse(queryString);
			FunctionQuery pagerankQuery = new FunctionQuery(new PageRankValueScore());
			FunctionQuery clickQuery = new FunctionQuery(new ClickValueScore());
			CustomScoreQuery query = new MixScoreQuery(normalQuery, pagerankQuery, clickQuery); 
			return searcher.search(query, 100);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Document getDoc(int docid) {
		try {
			return searcher.doc(docid);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Document clickDoc(int docid){
		return getDoc(docid);
	}
	
	public static void main(String[] args){ 
		WebSearch search = new WebSearch();
		TopDocs results = search.searchQuery("Çå»ª", true);
		ScoreDoc[] hits = results.scoreDocs;
		for (ScoreDoc web : hits) {
			Document doc = search.getDoc(web.doc);
			System.out.println("doc=" + web.doc + " score="
					+ web.score + " url=" + doc.get("url"));
		}
	}
}
