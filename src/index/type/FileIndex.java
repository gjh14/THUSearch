package index.type;

import java.io.File;
import java.io.UnsupportedEncodingException;

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
	
	public static String link(String text){
		String linked = "";
		try {
			String trans = new String(text.getBytes(), "gbk");
			for(int i = 0; i < text.length(); ++i)
				if(text.charAt(i) == trans.charAt(i))
					linked += text.charAt(i);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		for(; linked.indexOf("  ") != -1; linked = linked.replaceAll("  ", " "));
		for(; linked.indexOf("\t\t") != -1; linked = linked.replaceAll("\t\t", "\t"));
		linked = linked.replaceAll("\r", "");
		for(int pos; (pos = linked.indexOf('\n')) != -1;)
			linked = linked.replaceFirst("\n", (pos > 0) && linked.charAt(pos - 1) < 256 ? " " : "");
		return linked;
	}
}
