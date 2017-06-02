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
	
	int mapLabel = 0;
	HashMap<String, Integer> numMap = new HashMap<String, Integer>();
	ArrayList<String> webList = new ArrayList<String>();
	ArrayList<ArrayList<Integer>> linkMap = new ArrayList<ArrayList<Integer>>();
	ArrayList<ArrayList<String>> archorMap = new ArrayList<ArrayList<String>>();

	public HrefAnalyzer(String web) {
		root = web;
	}

	public int allocate(String web){
		int ret = -1;
		if (numMap.containsKey(web)) {
			ret = (int) numMap.get(web);
		} else {
			numMap.put(web, mapLabel);
			ret = mapLabel++;
			webList.add(web);
			assert(webList.size() == mapLabel);
			linkMap.add(new ArrayList<Integer>());
			archorMap.add(new ArrayList<String>());
		}
		return ret;
	}
	
	public void analyze(File file, String web) {
		int webLabel = allocate(web);
		//ArrayList<Integer> linkList = linkMap.get(webLabel);

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
					
					wholePath = wholePath.replaceAll("\n", "").replaceAll("\r", "");
					if (wholePath.contains("#"))
						wholePath = wholePath.substring(0, wholePath.lastIndexOf("#"));
					if (!wholePath.contains("news.tsinghua.edu.cn"))
						continue;
					if (wholePath.startsWith("http://"))
						wholePath = wholePath.substring(7, wholePath.length());
					if (wholePath.startsWith("https://"))
						wholePath = wholePath.substring(8, wholePath.length());
					
					int linkLabel = allocate(wholePath);
					linkMap.get(webLabel).add(linkLabel);
					archorMap.get(linkLabel).add(linkText);
					if (linkLabel == 52811)
						System.err.println(linkLabel + " " + webLabel + " " + linkText + " " + linkHref + " " + wholePath);
					if (linkLabel == 52812)
						System.err.println(linkLabel + " " + webLabel + " " + linkText + " " + linkHref + " " + wholePath);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			
			//HashSet<String> lostSet = new HashSet<String>();  
			FileWriter linkWriter = new FileWriter(path + "link.txt");
			/*
			for (Entry<String, ArrayList<String>> entry : linkMap.entrySet())
				for(String link : entry.getValue()){
					linkWriter.write(numMap.get(entry.getKey()) + "\t"+ numMap.get(link) + "\n");
					if(linkMap.get(link) == null)
						lostSet.add(link);
				}
			*/
			for (int i = 0; i < linkMap.size(); ++i) {
				for (Integer j : linkMap.get(i))
					linkWriter.write(i + "\t" + j + "\n");
			}
			linkWriter.close();
			
			/*
			FileWriter lostWriter = new FileWriter(path + "lost.txt");
			for(String lost : lostSet)
				lostWriter.write(lost + "\n");
			lostWriter.close();
			*/
			
			FileWriter archorWriter = new FileWriter(path + "anchor.txt");
			/*
			for (Entry<String, ArrayList<String>> entry : archorMap.entrySet()){
				archorWriter.write(numMap.get(entry.getKey()).toString());
				for(String archor : entry.getValue())
					archorWriter.write("\t" + archor);
				archorWriter.write('\n');
			}
			*/
			for (int i = 0; i < archorMap.size(); ++i)
				if (archorMap.get(i).size() > 0) {
					archorWriter.write(i + "");
					for (String a : archorMap.get(i))
						archorWriter.write("\t" + a);
					archorWriter.write("\n");
				}
			archorWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String path = "D:/workspace/mirror__4/";
		String web = "news.tsinghua.edu.cn";
		HrefAnalyzer analyzer = new HrefAnalyzer("news.tsinghua.edu.cn");
		analyzer.search(new File(path + web), web);
		analyzer.output(path);
	}
}
