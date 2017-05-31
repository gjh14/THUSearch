package index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatDocValuesField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queries.function.docvalues.LongDocValues;
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
		int cnt = 0;
		try{
			IndexWriter indexWriter = new IndexWriter(dir,iwc);
			BufferedReader reader = new BufferedReader(new FileReader(new File(path + "data.txt")));
			for(String line; (line = reader.readLine()) != null;){
				if(cnt++ < indexWriter.maxDoc())
					continue;
				if(cnt % 1000 == 0)
					System.out.println("Count " + cnt);
				String[] parts = line.split("\t");
				File file = new File(path + parts[1]);
				Document document = FileIndex.getDocument(file);
				if(document == null){
					System.err.println(cnt + " " + parts[0] + " " + parts[1] + " " + parts[2]);
					continue;
				}
				document.add(new StringField("url", parts[0], Field.Store.YES));
				document.add(new StringField("name", file.getName(), Field.Store.YES));
				document.add(new StringField("path", file.getAbsolutePath(), Field.Store.YES));
				document.add(new FloatDocValuesField("pagerank", Float.parseFloat(parts[2])));
				document.add(new NumericDocValuesField("click", 0));
				indexWriter.addDocument(document);
			}
			reader.close();
			indexWriter.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		WebIndex index = new WebIndex("index");
		index.buildIndex("D:/workspace/mirror__3/");
	}
}
