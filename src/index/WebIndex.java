package index;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class WebIndex {
	private Analyzer analyzer; 
    private Directory dir;
    private IndexWriterConfig iwc;
	public WebIndex(String indexDir){
		analyzer = new IKAnalyzer();
		try{
			dir = FSDirectory.open(new File(indexDir).toPath());
			iwc = new IndexWriterConfig(analyzer);
			iwc.setSimilarity(new BM25Similarity());
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }
	
	public void buildIndex(String path){
		try{
			IndexWriter indexWriter = new IndexWriter(dir,iwc);
			indexWriter.deleteAll();
			
			File input = new File(path);
			for(File file : input.listFiles()){
				Document document = new Document();
				document.add(new StringField("file", file.getName(), Field.Store.YES));
				document.add(new FloatDocValuesField("pagerank", 1));
				System.out.println("file=" + file.getName());
				indexWriter.addDocument(document);
			}
			indexWriter.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		WebIndex index = new WebIndex("index");
		index.buildIndex("input");
	}
}
