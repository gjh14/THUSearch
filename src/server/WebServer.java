package server;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import search.WebSearch;

public class WebServer extends HttpServlet {
	private WebSearch search;

	public WebServer() {
		super();
		search = new WebSearch();
	}

	@Override
	public void destroy() {
		search.close();
		super.destroy();
	}

	public void doSearch(HttpServletRequest request, HttpServletResponse response, boolean flag)
			throws ServletException, IOException{
		String queryString = request.getParameter("query");
		System.out.println(queryString);
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
		
		if (results != null && results.scoreDocs.length > 0) {
			ScoreDoc[] hits = results.scoreDocs;
			maxpage = (hits.length - 1) / 10 + 1;
			int len = Math.min(10, hits.length - (page - 1) * 10);
			
			docs = new int[len];
			urls = new String[len];
			entrys = new String[len];
			absts = new String[len];
			paths = new String[len];
			imgs = new String[len];
			
			for (int i = 0; i < len; ++i) {
				ScoreDoc hit = hits[10 * (page - 1) + i];
				Document doc = search.getDoc(hit.doc); 
				Lighter lighter = new Lighter(queryString, doc, flag);
				
				docs[i] = hit.doc;
				entrys[i] = lighter.getEntry();
				absts[i] = lighter.getAbst();
				urls[i] = Lighter.cut(doc.get("url"), 32);
				paths[i] = doc.get("path");
				imgs[i] = doc.get("img");
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
		request.getRequestDispatcher("/webshow.jsp").forward(request, response);		
	}
	
	public void doLink(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		int docid = Integer.parseInt(request.getParameter("doc"));
		Document doc = search.clickDoc(docid);
		System.out.println("GetDoc " + docid + " " + doc.get("url"));
		String url = "http://" + doc.get("url");
		response.sendRedirect(url);
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
				default:
			}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}

	@Override
	public void init() throws ServletException {
	}
}
