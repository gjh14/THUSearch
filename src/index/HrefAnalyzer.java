package index;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class Record {
	public double pr;
	public int id;
	
	public Record(int a, double b) {
		id = a;
		pr = b;
	}
}

public class HrefAnalyzer {
	final static String pageRankFileName = "pagerank.txt";
	final static String pageRankReportName = "pagerank.log";
	final static int maxIterNum = 100;
	final static double alpha = 0.15;
	final static double dealpha = 1.0 - alpha;
	final static double eps = 1e-9;
	
	String root = null;
	int cnt = 0;
	
	int mapLabel = 0;
	HashMap<String, Integer> numMap = new HashMap<String, Integer>();
	ArrayList<String> webList = new ArrayList<String>();
	ArrayList<ArrayList<Integer>> linkMap = new ArrayList<ArrayList<Integer>>();
	ArrayList<ArrayList<String>> archorMap = new ArrayList<ArrayList<String>>();
	double[] pagerank = null;

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
					//if (linkLabel == 52811)
					//	System.err.println(linkLabel + " " + webLabel + " " + linkText + " " + linkHref + " " + wholePath);
					//if (linkLabel == 52812)
					//	System.err.println(linkLabel + " " + webLabel + " " + linkText + " " + linkHref + " " + wholePath);
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
	
	private void pageRankCalc() {
		// initiation
		pagerank = new double[mapLabel];
		double[] pin = new double[mapLabel];
		for (int i = 0; i < mapLabel; ++i)
			pagerank[i] = (double) 1.0 / mapLabel;
		
		// iteration
		for (int iter = 0; iter < maxIterNum; ++iter) {
			// prepare
			for (int i = 0; i < mapLabel; ++i)
				pin[i] = alpha / mapLabel;
			double zeroOut = 0;
			// scan
			int label = 0;
        	for (ArrayList<Integer> vec: linkMap) {
        		if (vec.size() > 0) {
	        		for (Integer it: vec) {
	        			pin[it.intValue()] += pagerank[label] * dealpha / vec.size();
	        		}
	        	} else {
	        		zeroOut += pagerank[label];
	        	}
        		++label;
        	}
        	// calculate
        	double maxDelta = 0, iterSum = 0;
        	for (int i = 0; i < mapLabel; ++i) {
        		pin[i] += zeroOut * dealpha / mapLabel;
        		maxDelta = Math.max(maxDelta, Math.abs(pagerank[i] - pin[i]));
        		iterSum += pin[i];
        		pagerank[i] = pin[i];
        	}
        	// output log
        	System.out.println("Iter " + iter + ": " + maxDelta + " " + iterSum);
        	// check EPS
        	if (maxDelta < eps) break;
		}
	}
	
	public void output(String path){
		System.out.println(numMap.size() + " " + linkMap.size() + " " + archorMap.size());
		
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		
		try {
			FileWriter infoWriter = new FileWriter(path + "allinfo.txt");
			//for(String web : webList)
			//	infoWriter.write(web + "\n");
			for (int i = 0; i < mapLabel; ++i) {
				infoWriter.write(webList.get(i));
				infoWriter.write("\t" + pagerank[i]);
				
				ArrayList<String> candi = new ArrayList<String>();
				
				for (int j = 0; j < archorMap.get(i).size(); ++j) {
					boolean unique = true;
					for (int k = 0; k < j; ++k) {
						if (archorMap.get(i).get(j).equals(archorMap.get(i).get(k))) {
							unique = false;
							break;
						}
					}
					if (unique) {
						candi.add(archorMap.get(i).get(j)
								.replaceAll("\t", "").replaceAll(" ", "")
								.replaceAll("\n", "").replaceAll("\r", "")
								.replaceAll("\\?", "")
								);
					}
				}
				
				for (int j = 0; j < candi.size(); ++j) {
					boolean unique = true;
					
					String cut = candi.get(j);
					
					if (candi.get(j).endsWith("...")) {
						cut = candi.get(j).substring(0, candi.get(j).length() - 3);
						for (int k = 0; k < candi.size(); ++k)
							if (j != k && candi.get(k).contains(cut)) {
								unique = false;
								break;
							}
					}
					
					if (unique) {
						infoWriter.write("\t" + cut);
						//if (candi.get(j).length() < 4)
						//	System.err.println(candi.get(j));
					}
				}
				
				infoWriter.write("\n");
			}
			infoWriter.close();
			
			/*
			FileWriter linkWriter = new FileWriter(path + "link.txt");
			for (int i = 0; i < linkMap.size(); ++i) {
				for (Integer j : linkMap.get(i))
					linkWriter.write(i + "\t" + j + "\n");
			}
			linkWriter.close();
			*/
			
			/*
			FileWriter archorWriter = new FileWriter(path + "anchor.txt");
			for (int i = 0; i < archorMap.size(); ++i)
				if (archorMap.get(i).size() > 0) {
					archorWriter.write(i + "");
					for (String a : archorMap.get(i))
						archorWriter.write("\t" + a);
					archorWriter.write("\n");
				}
			archorWriter.close();
			*/
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// print log info
        ArrayList<Record> log = new ArrayList<Record>();
        for (int i = 0; i < mapLabel; ++i)
        	log.add(new Record(i, pagerank[i]));
        
        Collections.sort(log, new Comparator<Record>() {
			@Override
            public int compare(Record b1, Record b2) {  
                //return b2.score - b1.score > 1e-7? 1 : -1;
            	if (b2.pr - b1.pr > 1e-7) return 1;
            	else if (b1.pr - b2.pr > 1e-7) return -1;
            	else return 0;
            }
        }); 
        
        FileWriter logWriter = null;  
        try {
        	logWriter = new FileWriter(path + pageRankReportName, false);
        	
        	for (Record it: log)
        		logWriter.write(it.id + " " + it.pr + " " + webList.get(it.id) + "\n");
        	
        	logWriter.close();
        	logWriter = null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (logWriter != null) {
                try {
                	logWriter.close();
                } catch (IOException e1) {
                	e1.printStackTrace();
                }
            }
        }
	}

	public static void main(String[] args) {
		String path = "D:/workspace/mirror__4/";
		String web = "news.tsinghua.edu.cn";
		HrefAnalyzer analyzer = new HrefAnalyzer("news.tsinghua.edu.cn");
		analyzer.search(new File(path + web), web);
		analyzer.pageRankCalc();
		analyzer.output(path);
	}
}
