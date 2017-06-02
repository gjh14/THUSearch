package index;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HrefAnalyzer {

	String root = null;
	int cnt = 0;
	
	HashMap<String, Integer> numMap = null;
	ArrayList<String> webList = null;
	HashMap<String, ArrayList<String>> linkMap = null;
	HashMap<String, ArrayList<String>> archorMap = null;

	public HrefAnalyzer(String web) {
		root = web;
		numMap = new HashMap<String, Integer>();
		webList = new ArrayList<String>();
		linkMap = new HashMap<String, ArrayList<String>>();
		archorMap = new HashMap<String, ArrayList<String>>();
	}

	public void allocate(String web){
		if(numMap.get(web) == null){
			webList.add(web);
			numMap.put(web, webList.size());
		}
	}
	
	public void analyze(File file, String web) {
		allocate(web);
		ArrayList<String> linkList = linkMap.get(web);
		if (linkList == null)
			linkList = new ArrayList<String>();
		try {
			Document htmlDoc = null;
//			System.out.println(Detector.fileCode(file));
			htmlDoc = Jsoup.parse(file, "utf-8", "");
			Elements links = htmlDoc.getElementsByTag("a");
			for (Element link : links) {
				String linkHref = link.attr("href");
				String linkText = link.text().trim();
				if (!linkHref.equals("") && !linkText.equals("")  && !linkHref.equals("#")
						&& !linkHref.startsWith("mailto:") && !linkHref.startsWith("javascript:")) {
					//System.out.println(web + " " + linkHref + " " + linkText);
					String wholePath = null;
					if(linkHref.startsWith("http://") || linkHref.startsWith("https://"))
						wholePath = linkHref;
					else if(linkHref.startsWith("/"))
						wholePath = root + linkHref;
					else
						wholePath = web.substring(0, web.lastIndexOf('/')) + "/" + linkHref;
					linkList.add(wholePath);
					
					allocate(wholePath);
					ArrayList<String> anthorList = archorMap.get(wholePath);
					if (anthorList == null)
						anthorList = new ArrayList<String>();
					anthorList.add(linkText);
					archorMap.put(wholePath, anthorList);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		linkMap.put(web, linkList);
	}

	public void search(File dir, String web) {
		if (dir.isFile()) {
			if(web.endsWith(".html")){
				if(++cnt % 100 == 0)
					System.out.println(cnt + " " + web);
				analyze(dir, web);
			}
			return;
		}
		for (File file : dir.listFiles())
			search(file, web + "/" + file.getName());
	}

	public void output(String path){
		System.out.println(numMap.size() + " " + linkMap.size() + " " + archorMap.size());
		try {
			FileWriter webWriter = new FileWriter(path + "web.txt");
			for(String web : webList)
				webWriter.write(web + "\n");
			webWriter.close();
			
			HashSet<String> lostSet = new HashSet<String>();  
			FileWriter linkWriter = new FileWriter(path + "link.txt");
			for (Entry<String, ArrayList<String>> entry : linkMap.entrySet())
				for(String link : entry.getValue()){
					linkWriter.write(numMap.get(entry.getKey()) + "\t"+ numMap.get(link) + "\n");
					if(linkMap.get(link) == null)
						lostSet.add(link);
				}
			linkWriter.close();
			
			FileWriter lostWriter = new FileWriter(path + "lost.txt");
			for(String lost : lostSet)
				lostWriter.write(lost + "\n");
			lostWriter.close();
			
			FileWriter archorWriter = new FileWriter(path + "anchor.txt");
			for (Entry<String, ArrayList<String>> entry : archorMap.entrySet()){
				archorWriter.write(numMap.get(entry.getKey()).toString());
				for(String archor : entry.getValue())
					archorWriter.write("\t" + archor);
				archorWriter.write('\n');
			}
			archorWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String path = "D:/homework/MyEclipse/resource/mirror/";
		String web = "news.tsinghua.edu.cn/publish/thunews/9656/2011";
		HrefAnalyzer analyzer = new HrefAnalyzer("news.tsinghua.edu.cn");
		analyzer.search(new File(path + web), web);
		analyzer.output(path);
	}
}
