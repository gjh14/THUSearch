package index.type;

import java.io.File;

import org.apache.lucene.document.Document;

public class FileIndex {
	public static Document getDocument(File file){
		String name = file.getName();
		String[] token = name.split("\\.");
		switch(token[token.length - 1]){
		case "DOC":
		case "doc":
			return DocIndex.getDocument(file);
		case "DOCX":
		case "docx":
			return DocxIndex.getDocument(file);
		case "PDF":
		case "pdf":
			return PDFIndex.getDocument(file);
		default:
			return HtmlIndex.getDocument(file);
		}
	}
}
