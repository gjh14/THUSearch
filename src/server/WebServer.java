package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.TopDocs;

import index.ViReader;
import index.WebIndex;
import search.WebSearch;

public class WebServer extends HttpServlet {
	private WebSearch search;
	private ViReader reader;

	public WebServer() {
		super();
		search = new WebSearch();
		reader = new ViReader(WebIndex.MIRRORDIR + "vi.txt");
	}

	@Override
	public void destroy() {
		search.close();
		super.destroy();
	}

	public void doSearch(HttpServletRequest request, HttpServletResponse response, boolean flag)
			throws ServletException, IOException{
		String queryString = request.getParameter("query");
		if(queryString == null || queryString.length() == 0){
			response.sendRedirect("../websearch.jsp");
			return;
		}
		
		String pageString = request.getParameter("page");
		int maxpage = 0;
		int page = pageString != null ? Integer.parseInt(pageString) : 1;
		
		TopDocs results = search.searchQuery(queryString, flag);
		int[] docs = null;
		String[] urls = null;
		String[] entrys = null;
		String[] absts = null;
		String[] paths = null;
		String[] imgs = null;
		String[] vis = null;
		
		if (results != null && results.scoreDocs.length > 0) {
			ArrayList<ScoreDoc> candi = new ArrayList<ScoreDoc>();
			ArrayList<String> candiEr = new ArrayList<String>();
			for (ScoreDoc it : results.scoreDocs) {
				String er = search.getDoc(it.doc).get("entry");
				boolean unique = true;
				for (String str : candiEr)
					if (str.equals(er)) {
						unique = false;
						break;
					}
				if (unique) {
					candi.add(it);
					candiEr.add(er);
				}
			}
			
			ScoreDoc[] hits = (ScoreDoc[]) candi.toArray(new ScoreDoc[candi.size()]);
			//ScoreDoc[] hits = results.scoreDocs;
			maxpage = (hits.length - 1) / 10 + 1;
			int len = Math.min(10, hits.length - (page - 1) * 10);
			
			docs = new int[len];
			urls = new String[len];
			entrys = new String[len];
			absts = new String[len];
			paths = new String[len];
			imgs = new String[len];
			vis = new String[len];
			
			for (int i = 0; i < len; ++i) {
				ScoreDoc hit = hits[10 * (page - 1) + i];
				Document doc = search.getDoc(hit.doc); 
				Lighter lighter = new Lighter(queryString, doc, flag);
				
				String url = doc.get("url");
				docs[i] = hit.doc;
				entrys[i] = lighter.getEntry();
				absts[i] = lighter.getAbst();
				urls[i] = Lighter.cut(url, 40);
				//paths[i] = doc.get("path");
				paths[i] = "/" + url;
				imgs[i] = "http://" + url.substring(0, url.indexOf('/')) + doc.get("img");
				vis[i] = reader.get(url);
				System.out.println("doc=" + hit.doc + " score=" + hit.score + " url=" + urls[i]);
			}
		} else {
			System.out.println("result null");
		}

		request.setAttribute("currentTag", "search");
		request.setAttribute("currentQuery", queryString);
		request.setAttribute("maxPage", maxpage);
		request.setAttribute("currentPage", page);
		request.setAttribute("webDocs", docs);
		request.setAttribute("webUrls", urls);
		request.setAttribute("webEntrys", entrys);
		request.setAttribute("webAbsts", absts);
		request.setAttribute("webPaths", paths);
		request.setAttribute("webImgs", imgs);
		request.setAttribute("webVis", vis);
		request.getRequestDispatcher("/webshow.jsp").forward(request, response);		
	}
	
	public void doLink(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		int docid = Integer.parseInt(request.getParameter("doc"));
		Document doc = search.clickDoc(docid);
		System.out.println("GetDoc " + docid + " " + doc.get("url"));
		String url = "http://" + doc.get("url");
		response.sendRedirect(url);
	}
	
	public void doEntry(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String query = request.getParameter("query");
		if(query != null){
			TopDocs results = search.searchEntry(query, false);
			String list = "";
			if(results != null && results.scoreDocs.length > 0){
				for(ScoreDoc doc : results.scoreDocs){
					String entry = search.getDoc(doc.doc).get("entry");
					list += (list.length() > 0 ? "\t" : "") + entry;
				}
				System.out.println(list);
				response.setContentType("text/html;charset=utf-8");
				response.getWriter().print(list);
				response.getWriter().flush();
				response.getWriter().close();
			}
		}
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		String tag = request.getParameter("tag");
		if(tag != null)
			switch(tag){
				case "search":
					doSearch(request, response, false);
					break;
				case "link":
					doLink(request, response);
					break;
				case "entry":
					doEntry(request, response);
				default:
			}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			doGet(request, response);
	}

	@Override
	public void init() throws ServletException {
	}
}
