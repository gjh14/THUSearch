<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
	request.setCharacterEncoding("utf-8");
	response.setCharacterEncoding("utf-8");
	String path = request.getContextPath();
	String webPath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort();
	String currentTag = (String)request.getAttribute("currentTag");
	String currentQuery = (String)request.getAttribute("currentQuery");
	int maxPage = 0, currentPage = 0;
	if(request.getAttribute("maxPage") != null)
		maxPage = (Integer)request.getAttribute("maxPage");
	if(request.getAttribute("currentPage") != null)
		currentPage = (Integer)request.getAttribute("currentPage"); 
%>

<!doctype html>

<head>
	<title>校园搜索: <%= currentQuery %></title>
	<meta http-equiv="Content-Type" content="text/html" charset="utf-8">
	<link type="text/css" href="/THUSearch/indexcss/st1.css" rel="stylesheet">
</head>

<body class="">
	<div class="wrap">
		<div class="header">
			<a href="http://www.tsinghua.edu.cn/" class="logo" target="_blank"></a>
			<form name="searchForm" id="searchForm" method="get" action="THUServer">
				<div class="querybox">
					<div class="qborder">
						<div class="qborder2">
							<input class="query" id="upquery" name="query" value="<%= currentQuery%>" type="text">
						</div>
					</div>
					<input value="搜索" class="sbtn1" id="searchBtn" type="submit">
					<input name ="tag" value="search" style="display:none"/>
				</div>
			</form>
		</div>
	</div>
	
	<div class="wrapper" id="wrapper">
		<div id="main" class="main">
			<div>
				<div class="results">
<% 
	int[] webDocs = (int[]) request.getAttribute("webDocs");
	String[] webUrls = (String[]) request.getAttribute("webUrls");
	String[] webEntrys = (String[]) request.getAttribute("webEntrys");
	String[] webPaths = (String[]) request.getAttribute("webPaths");
	String[] webAbsts = (String[]) request.getAttribute("webAbsts");
	String[] webVis = (String[]) request.getAttribute("webVis"); 
	String[] webImgs = (String[]) request.getAttribute("webImgs");
	if(webEntrys != null && webEntrys.length > 0){
		for(int i = 0; i < webEntrys.length; i++){
%>
					<div class="vrwrap">
						<h3 class="vrTitle">
							<a target="_blank" href="THUServer?tag=link&doc=<%= webDocs[i]%>">
								<%= webEntrys[i] %>
							</a>
						</h3>
						<div class="strBox">
<%
			if(webImgs[i] != null){
%>
							<div class="str_div">
								<a target="_blank" class="str_img size_90_90" href="<%= webImgs[i]%>">
									<img alt="" height="90" width="90" src="<%= webImgs[i]%>"
									onerror="this.parentNode.parentNode.style.display=&quot;none&quot;;this.onerror = null;">
								</a>
							</div>
<%
			}
%>
							<div class="str_info_div">
								<p class="str_info">
									<%= webAbsts[i] %>
								</p>
<%
			if(webVis[i] != null){
%>

								<p class="access_info">
									<a target="_blank" href="http://www.hehe.edu.cn/">垂直</a>
								</p>
<%
			}
%>
								<div class="fb">
									<cite> <%= webUrls[i] %> &nbsp;-&nbsp;</cite>
									<a target="_blank" href="<%= webPaths[i]%>">网页快照</a>
								</div>
							</div>
						</div>
					</div>	
<%
		}
	}else{
%>
					<div class="vrwrap">
						<h3 class="vrTitle">
							无相关内容
						</h3>
					</div>				
<%
	}
%>
				</div>
			</div>
		</div>
	</div>
	
	<div class="p">
<%
	String server = "THUServer?tag=" + currentTag + "&query=" + currentQuery;
	if(currentPage > 1){
%>
		<a href="<%= server %>&page=<%= currentPage - 1 %>" class="np">上一页</a>
<%
	}
	for(int i = Math.max(1, currentPage - 5); i < currentPage; ++i){
%>
		<a href="<%= server %>&page=<%= i %>"><%= i %></a>
<%
	}
%>
		<span><%= currentPage %></span>
<%
	for(int i = currentPage + 1; i <= Math.min(maxPage, currentPage + 5); ++i){
%>
		<a href="<%= server %>&page=<%= i %>"><%= i %></a>
<%
	}
	if(currentPage < maxPage){
%>
		<a href="<%= server %>&page=<%=currentPage + 1%>" class="np">下一页</a>
<%
	}
%>
	</div>                        
</body>
