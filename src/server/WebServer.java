package server;


import java.io.IOException;

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
		search = new WebSearch("index");
	}

	@Override
	public void destroy() {
		super.destroy();
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=utf-8");
		request.setCharacterEncoding("utf-8");
		String queryString = request.getParameter("query");
		String pageString = request.getParameter("page");
		
		int	page = pageString != null ? Integer.parseInt(pageString) : 1;
		int maxpage = 0;
		if(queryString == null)
			System.out.println("null query");
		else{
			System.out.println(queryString);
			TopDocs results = search.searchQuery(queryString);
			String[] tags = null;
			String[] abss = null;
			String[] paths = null;
			if (results != null && results.scoreDocs.length > 0) {
				ScoreDoc[] hits = results.scoreDocs;
				maxpage = (hits.length - 1) / 10 + 1;
				int len = Math.min(10, hits.length - (page - 1)* 10);
				tags = new String[len];
				abss = new String[len];
				paths = new String[len];
				for(int i = 0; i < len; ++i){
					ScoreDoc hit = hits[10 * (page - 1) + i];
					Document doc = search.getDoc(hit.doc);
					Lighter lighter = new Lighter(queryString, doc);
					tags[i] = lighter.getTag();
					abss[i] = lighter.getAbs();
					paths[i] = doc.get("file");
					System.out.println("doc=" + hit.doc + " score="
							+ hit.score + " tag=" + tags[i]);
				}
			}else{
				System.out.println("result null");
			}
			
			request.setAttribute("currentQuery",queryString);
			request.setAttribute("currentPage", page);
			request.setAttribute("maxPage", maxpage);
			request.setAttribute("webTags", tags);
			request.setAttribute("webAbss", abss); 
			request.setAttribute("webPaths", paths);
			request.getRequestDispatcher("/webshow.jsp").forward(request,
					response);
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
