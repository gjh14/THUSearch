package index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
	public Document fileDocument(File file){
		String name = file.getName();
		String[] token = name.split("\\.");
		switch(token[token.length - 1]){
		case "html":
			return HtmlIndex.getDocument(file);
		case "doc":
			return DocIndex.getDocument(file);
		case "docx":
			return DocxIndex.getDocument(file);
		case "pdf":
			return PDFIndex.getDocument(file);
		default:
		}
		return null;
	}
	
	public void buildIndex(String path){
		try{
			IndexWriter indexWriter = new IndexWriter(dir,iwc);
			indexWriter.deleteAll();
			BufferedReader reader = new BufferedReader(new FileReader("data.txt"));
			for(String line; (line = reader.readLine()) != null;){
				String[] parts = line.split("\t");
				System.out.println(parts[0] + " " + parts[1] + " " + parts[2]);
				File file = new File(parts[1]);
				Document document = fileDocument(file);
				document.add(new StringField("url", parts[0], Field.Store.YES));
				document.add(new StringField("name", file.getName(), Field.Store.YES));
				document.add(new StringField("path", file.getPath(), Field.Store.YES));
				document.add(new FloatDocValuesField("pagerank", Float.parseFloat(parts[2])));
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
		index.buildIndex("input");
	}
}
