package search;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
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

public class WebSearch {
	private IndexReader reader;
	private IndexSearcher searcher;
	private Analyzer analyzer;
	private Map<String, Float> boosts;
	private static String[] field = new String[]{"title", "body", "text"};

	public WebSearch(String indexDir){		
		analyzer = new IKAnalyzer(true);
		boosts = new HashMap<String, Float>();
		boosts.put("title", 2.0f);
		boosts.put("body", 1.0f);
		boosts.put("text", 0.9f);
		try{
			Directory dir = FSDirectory.open(new File(indexDir).toPath());
			reader = DirectoryReader.open(dir);
			searcher = new IndexSearcher(reader);
			searcher.setSimilarity(new BM25Similarity());
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public TopDocs searchQuery(String queryString){
		System.out.println("query=" + queryString);
		MultiFieldQueryParser parser = new MultiFieldQueryParser(field, analyzer, boosts);
		try {
			Query normalQuery = parser.parse(queryString);
			FunctionQuery pagerankQuery = new FunctionQuery(new PageRankValueScore());
//			FunctionQuery clickQuery = new FunctionQuery(new ClickValueScore());
//			CustomScoreQuery query = new MixScoreQuery(normalQuery, pagerankQuery, clickQuery);
			CustomScoreQuery query = new CustomScoreQuery(normalQuery, pagerankQuery); 
			return searcher.search(query, 100);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Document getDoc(int doc) {
		try {
			return searcher.doc(doc);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static void main(String[] args){ 
		WebSearch search = new WebSearch("D:/DProgram/MyEclipse/index");
		TopDocs results = search.searchQuery("Çå»ª");
		ScoreDoc[] hits = results.scoreDocs;
		for (ScoreDoc web : hits) {
			Document doc = search.getDoc(web.doc);
			System.out.println("doc=" + web.doc + " score="
					+ web.score + " url=" + doc.get("url"));
		}
	}
}
