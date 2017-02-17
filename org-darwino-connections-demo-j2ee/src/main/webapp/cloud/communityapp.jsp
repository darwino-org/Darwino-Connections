<!DOCTYPE html>
<html>

<head>
	<title>Darwino Connections Demo</title>

	<script type="text/javascript">
		window.onload = function() {
			try {
				window.addEventListener('message', function(evt) {
					document.getElementById("contextId").innerHTML = JSON.stringify(evt.data, null, 3);
				}, false);
			} catch (e) {
				console.log(e);
			}
			parent.postMessage("appReady", "*");
		}
	</script>
</head>

<body>
	<div id="header">
    	<%@ include file="../_pages/hello.jspf" %>
	</div>
	
	<p> The following context is passed from IBM Connections: </p>	
	<pre id="contextId"></pre>	
</body>

</html>