package index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatDocValuesField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import index.type.FileIndex;

public class WebIndex {
	static public String MIRRORDIR = "D:/workspace/mirror__4";
	static public String INDEXDIR = MIRRORDIR + "/index";
	 
    private Directory dir;
    private IndexWriterConfig iwc;
    
	public WebIndex(){
		try{
			dir = FSDirectory.open(new File(INDEXDIR).toPath());
			iwc = new IndexWriterConfig(new IKAnalyzer(false));
			iwc.setSimilarity(new BM25Similarity());
    	}catch(IOException e){
    		e.printStackTrace();
    	}
    }

	public void makeEntry(Document doc, String[] parts){
		String archor = "";
		String entry = "";
		for(int i = 2; i < parts.length; ++i){
			archor += parts[i] + " ";
			if(parts[i].length() < 64 && parts[i].length() > entry.length())
				entry = parts[i];
		}
		String title = doc.get("title");
		if(title != null && title.length() > 4)
			entry = title;
		if(entry.equals(""))
			entry = parts[0];
		doc.add(new TextField("archor", archor, Field.Store.NO));
		doc.add(new TextField("entry", entry, Field.Store.YES));
	}
	
	public void buildIndex(){
		int cnt = 0;
		try{
			IndexWriter indexWriter = new IndexWriter(dir,iwc);
			indexWriter.deleteAll();
			BufferedReader reader = new BufferedReader(new FileReader(
					new File(MIRRORDIR + "allinfo.txt")));
			for(String line; (line = reader.readLine()) != null;){
				if(cnt++ < indexWriter.maxDoc())
					continue;
				if(cnt % 1000 == 0)
					System.out.println("Count " + cnt);
				String[] parts = line.split("\t");
				File file = new File(MIRRORDIR + parts[0]);
				Document doc = FileIndex.getDocument(file);
				if(doc == null){
					System.err.println(cnt + " " + parts[0] + " " + parts[1]);
					continue;
				}
				makeEntry(doc, parts);
				doc.add(new StringField("name", file.getName(), Field.Store.YES));
				doc.add(new StringField("path", file.getAbsolutePath(), Field.Store.YES));
				doc.add(new StringField("url", parts[0], Field.Store.YES));
				doc.add(new FloatDocValuesField("pagerank", Float.parseFloat(parts[1])));
				doc.add(new NumericDocValuesField("click", 0));
				indexWriter.addDocument(doc);
			}
			reader.close();
			indexWriter.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		WebIndex index = new WebIndex();
		index.buildIndex();
	}
}
