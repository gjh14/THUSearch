<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
	request.setCharacterEncoding("utf-8");
	response.setCharacterEncoding("utf-8");
	String path = request.getContextPath();
	String webPath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort();
	String currentQuery = (String)request.getAttribute("currentQuery");
	int maxPage = (Integer)request.getAttribute("maxPage");
	int currentPage = (Integer)request.getAttribute("currentPage");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>搜索<%=currentQuery%></title>
<style type="text/css">
<!--
#Layer1 {
	position:absolute;
	left:28px;
	top:26px;
	width:649px;
	height:32px;
	z-index:1;
}
#Layer2 {
	position:absolute;
	left:29px;
	top:82px;
	width:648px;
	height:602px;
	z-index:2;
}
#Layer3 {
	position:absolute;
	left:28px;
	top:697px;
	width:652px;
	height:67px;
	z-index:3;
}
-->
</style>
</head>

<body>
<div id="Layer1">
	<form id="form1" name="form1" method="get" action="THUServer">
	<label>
		<input name="query" value="<%=currentQuery%>" type="text" size="70" />
	</label>
	<label>
		<input type="submit" name="submit" value="Search" />
		<a herf="THUServer?submit=more&query=<%=currentQuery%>">更多结果</a>
	</label>
	</form>
</div>
<div id="Layer2" style="top: 82px; height: 585px;">
  <div id="webdiv">结果显示如下：
  <br>
  <Table style="left: 0px; width: 594px;">
  <% 
  	String[] webUrls = (String[]) request.getAttribute("webUrls");
  	String[] webTags = (String[]) request.getAttribute("webTags");
  	String[] webPaths = (String[]) request.getAttribute("webPaths");
  	String[] webAbss = (String[]) request.getAttribute("webAbss");
  	if(webTags != null && webTags.length>0){
  		for(int i = 0; i < webTags.length; i++){
  			System.out.println(webUrls[i] + " " + webPaths[i]);%>
  		<p>
  			<a href=<%=webUrls[i]%>><%=(currentPage - 1) * 10 + i + 1%>. <%= webTags[i] %></a>
  			 <%= webAbss[i] %>
  		</p>
  		<%}; %>
  	<%}else{ %>
  		<p><tr><h3>no such result</h3></tr></p>
  	<%}; %>
  </Table>
  </div>
  <div>
  	<p>
		<%if(currentPage > 1){ %>
			<a href="THUServer?query=<%=currentQuery%>&page=<%=currentPage - 1%>">上一页</a>
		<%}; %>
		<%for (int i = Math.max(1, currentPage - 5); i < currentPage; i++){%>
			<a href="THUServer?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a>
		<%}; %>
		<strong><%=currentPage%></strong>
		<%for (int i=currentPage + 1; i <= Math.min(maxPage, currentPage + 5); i++){ %>
			<a href="THUServer?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a>
		<%}; %>
		<%if(currentPage < maxPage){ %>
			<a href="THUServer?query=<%=currentQuery%>&page=<%=currentPage + 1%>">下一页</a>
		<%}; %>
	</p>
  </div>
</div>
<div id="Layer3" style="top: 839px; left: 27px;">
	
</div>
<div>
</div>
</body>
