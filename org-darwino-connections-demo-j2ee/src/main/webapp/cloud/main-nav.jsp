<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Darwino - IBM Connections Demo</title>
</head>
<body>

<script src="<%=org.darwino.ibm.connections.ConnectionsSession.get().getConnections().getUrl()%>/navbar/banner/darwino-connections"></script>

<div id="header">
    <%@ include file="../_pages/hello.jspf" %>
</div>

</body>
</html>