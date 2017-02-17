<%@ page contentType="text/html" %> 

<%
response.setHeader("Pragma","no-cache");
response.setHeader("Cache-Control","no-cache");
response.setDateHeader("Expires", 0);
response.addHeader("Cache-Control","private,no-store,max-stale=0");

// check if ajax request
//see setRequestHeader("X-Requested-With", "XMLHttpRequest");
if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))){
	// ajax request: do not redirect to login page
	// must return a 401 unauthorized instead
	response.sendError(401);
}

boolean mobile=false;
javax.servlet.http.Cookie[] cookies=request.getCookies();
for (javax.servlet.http.Cookie cookie : cookies){
	if ("WASReqURL".equals(cookie.getName())){
		String wasReqUrl=cookie.getValue();

		//System.out.println("REQ URL: "+wasReqUrl);

		String match=request.getContextPath()+"/mobile";
		mobile=wasReqUrl.indexOf(match) >0;
		break;
	}
}
%>

<html>
<head>
	<title>Login</title>
</head>
<body>
<script>
if (!window.location.origin) {
	// ie
  window.location.origin = window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port: '');
}
<% if (mobile) { %>
window.location.replace(window.location.origin+'<%=request.getContextPath()+"/mobile/login.xsp"%>');
<%} else {%>
window.location.replace(window.location.origin+'/profiles/login'+ window.location.search);
<%}%>
</script>
</body>
</html>
