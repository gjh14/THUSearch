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
	<script src="indexjs/jquery.min.js"></script>
</head>

<body class="">
	<div class="wrap">
		<div class="header">
			<a href="http://www.tsinghua.edu.cn/" class="logo" target="_blank"></a>
			<form name="searchForm" id="searchForm" method="get" action="THUServer" autocomplete="off">
				<div class="querybox">
					<div class="qborder">
						<div class="qborder2">
							<input class="query" id="upquery" name="query" value="<%= currentQuery%>" type="text" autocomplete="off">
						</div>
					</div>
					<input value="搜索" class="sbtn1" id="searchBtn" type="submit">
					<input name ="tag" value="search" style="display:none"/>
					<div class="search_suggest" id="suggest"><ul></ul></div>
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
									<%= webVis[i] %>
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
	for(int i = Math.max(1, currentPage - 4); i < currentPage; ++i){
%>
		<a href="<%= server %>&page=<%= i %>"><%= i %></a>
<%
	}
%>
		<span><%= currentPage %></span>
<%
	for(int i = currentPage + 1; i <= Math.min(maxPage, currentPage + 4); ++i){
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

<script type="text/javascript">

//实现搜索输入框的输入提示js类

function oSearchSuggest(searchFuc){
	var input = $('#query');
	var suggestWrap = $('#suggest');
	var key = "";
	var init = function(){
		input.bind('keyup',sendKeyWord);
		input.bind('blur',function(){setTimeout(hideSuggest,100);})
	}
	var hideSuggest = function(){
		suggestWrap.hide();
	}
	
	//发送请求，根据关键字到后台查询

	var sendKeyWord = function(event){
		
		//键盘选择下拉项

		if(suggestWrap.css('display')=='block'&&event.keyCode == 38||event.keyCode == 40){
			var current = suggestWrap.find('li.hover');
			if(event.keyCode == 38){
				if(current.length>0){
					var prevLi = current.removeClass('hover').prev();
					if(prevLi.length>0){
						prevLi.addClass('hover');
						input.val(prevLi.html());
					}
				}else{
					var last = suggestWrap.find('li:last');
					last.addClass('hover');
					input.val(last.html());
				}
				
			}else if(event.keyCode == 40){
				if(current.length>0){
					var nextLi = current.removeClass('hover').next();
					if(nextLi.length>0){
						nextLi.addClass('hover');
						input.val(nextLi.html());
					}
				}else{
					var first = suggestWrap.find('li:first');
					first.addClass('hover');
					input.val(first.html());
				}
			}
			
		//输入字符

		}else{ 
			var valText = $.trim(input.val());
			if(valText ==''||valText==key){
				return;
			}
			searchFuc(valText);
			key = valText;
		}			
		
	}
	//请求返回后，执行数据展示

	this.dataDisplay = function(data){
		if(data.length<=0){
            suggestWrap.hide();
			return;
		}
		
		//往搜索框下拉建议显示栏中添加条目并显示

		var li;
		var tmpFrag = document.createDocumentFragment();
		suggestWrap.find('ul').html('');
		for(var i=0; i<data.length; i++){
			li = document.createElement('LI');
			li.innerHTML = data[i];
			tmpFrag.appendChild(li);
		}
		suggestWrap.find('ul').append(tmpFrag);
		suggestWrap.show();
		
		//为下拉选项绑定鼠标事件

		suggestWrap.find('li').hover(function(){
				suggestWrap.find('li').removeClass('hover');
				$(this).addClass('hover');
		
			},function(){
				$(this).removeClass('hover');
		}).bind('click',function(){
			input.val(this.innerHTML);
			suggestWrap.hide();
		});
	}
	init();
};

//实例化输入提示的JS,参数为进行查询操作时要调用的函数名

var searchSuggest =  new oSearchSuggest(sendKeyWordToBack);

//这是一个模似函数，实现向后台发送ajax查询请求，并返回一个查询结果数据，传递给前台的JS,再由前台JS来展示数据。本函数由程序员进行修改实现查询的请求

//参数为一个字符串，是搜索输入框中当前的内容
	
function sendKeyWordToBack(keyword){
	var req = new XMLHttpRequest();
	var url = "http://localhost:8080/THUSearch/servlet/THUServer";
	req.open("GET", url + "?tag=entry&query=" + keyword, true);  
	req.onreadystatechange = function(event){
			var key = req.responseText.split("\t");
			var aData = [];
			for(var i = 0; i < key.length; ++i)
				if(key[i] != "")
					aData.push(key[i]);
			searchSuggest.dataDisplay(aData);
		}  
	req.setRequestHeader("Content-Type", "text/html;charset=UTF-8");
	req.send(null);
	
/*	var aData = ["a", "b"];
	searchSuggest.dataDisplay(aData);*/
}

</script>

</body>
