package search;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ReferenceManager;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import index.WebIndex;

public class WebSearch {
	private static String[] field = new String[]{"title", "body", "text", "archor", "keywords"};
	static private Map<String, Float> boosts;
	static{
		boosts = new HashMap<String, Float>();
		boosts.put("title", 10.0f);
		boosts.put("body", 1.0f);
		boosts.put("text", 1.0f);
		boosts.put("archor", 40.0f);
		boosts.put("keywords", 20.0f);
	}

	private IndexWriter writer = null;
	private ReferenceManager<IndexSearcher> manager = null;
	private ControlledRealTimeReopenThread<IndexSearcher> crt = null;
	private IndexSearcher searcher = null;
	
	static long period = 60000;
	private int tot, sum;
	private Timer timer = null;
			
	public WebSearch(){		
		try{
			Directory dir = FSDirectory.open(new File(WebIndex.INDEXDIR).toPath());
			IndexWriterConfig iwc = new IndexWriterConfig(new IKAnalyzer(false));
			iwc.setSimilarity(new BM25Similarity());
			writer = new IndexWriter(dir,iwc);
			
			manager = new SearcherManager(writer, true, false, new SearcherFactory());
			crt = new ControlledRealTimeReopenThread<>(writer, manager, 5.0, 0.025);
			crt.setDaemon(true);
			crt.start();
			
			manager.maybeRefresh();
			searcher = manager.acquire();
			tot = searcher.getIndexReader().maxDoc();
			LeafReader reader = manager.acquire().getIndexReader().leaves().get(0).reader();
			NumericDocValues values = DocValues.getNumeric(reader, "click");
			for(int i = 0; i < tot; ++i)
				sum += values.get(i);
			
			timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask(){
				@Override
				public void run() {
					if(writer.isOpen())
						try {
							writer.commit();
							System.out.println("Commit: " +  sum);
						} catch (IOException e) {
							e.printStackTrace();
						}
				}
			}, period, period);
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
			CustomScoreQuery query = new MixScoreQuery(normalQuery, pagerankQuery,
					clickQuery, tot, sum);
			
			manager.maybeRefresh();
			searcher = manager.acquire();
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
		System.out.println(tot + " " + ++sum);
		Document doc = getDoc(docid);
		try {
			manager.maybeRefresh();
			LeafReader reader = manager.acquire().getIndexReader().leaves().get(0).reader();
			NumericDocValues values = DocValues.getNumeric(reader, "click");
			long click = values.get(docid) + 1;
			writer.updateNumericDocValue(new Term("url", doc.get("url")), "click", click);
			System.out.println("Click " + click + " Term " + new Term("url", doc.get("url")));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return doc;
	}
	
	public void close(){
		crt.interrupt();
		crt.close();
		timer.cancel();
		try {
			writer.commit();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){ 
		WebSearch search = new WebSearch();
		TopDocs results = search.searchQuery("pdf", true);
		ScoreDoc[] hits = results.scoreDocs;
		for (ScoreDoc web : hits) {
			Document doc = search.getDoc(web.doc);
			System.out.println("doc=" + web.doc + " score="
					+ web.score + " url=" + doc.get("url"));
		}
		search.close();
	}
}
