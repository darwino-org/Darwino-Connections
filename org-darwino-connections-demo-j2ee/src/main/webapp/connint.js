// 1- Remove the META-DATA tags, title and favicon
// 2- Remove the unnecessary style sheet (app specific, not used by the navbar...)
// 3- Put easy to find comments in the text to avoid HTML parsing and make it reliable
//     <!-- BEGIN HEAD -->
//     <!-- END HEAD -->
//     <!-- BEGIN BODY -->
//     <!-- END BODY -->
//     <!-- APP CONTENT -->
// 4- Login/Logout menu hooks
// 5- Login/logout page

// How does that work with Pink?

(function() {

var conn = {
	displayLCFooter: true,
	head: null,
	bodyStart: null,
	bodyEnd: null,
	doc: null,
};
window._dwoconn_ = conn;

conn.emitHeader = function emitHeader() {
	if(conn.head) {
		document.write(conn.head);
	}
};
conn.startBody = function startBody() {
	var attrs = conn.doc.body.attributes;
	for(var i=0; i<attrs.length; i++) {
		document.body.setAttribute(attrs[i].name,attrs[i].value);
	}
	if(conn.bodyStart) {
		document.write(conn.bodyStart);
	}
};
conn.endBody = function endBody() {
	if(conn.bodyEnd) {
		document.write(conn.bodyEnd);
	}
	
	// We subscribe to the logout event to redirect back to this page	
	document.addEventListener('DOMContentLoaded', function() {
		try {
			dojo.require("lconn.core.auth");
			lconn.core.auth.getLogoutUrl=function(redirectUrl) {
				var url = window.location.href;
				var logoutUrl = "/profiles/ibm_security_logout?logoutExitPage="+encodeURIComponent(url);
				return logoutUrl;
			}
		} catch(e) {
			console.log("Could not overwrite getLogoutUrl, ", e);
		}	
	}, false);

	// Cleanup the global variable
	delete window._dwoconn_;
};

function removeElement(e) {
	if(e) e.parentNode.removeChild(e);
}
function removeElements(l) {
	for(var i=0; i<l.length; i++) {
		removeElement(l.item(i));
	}
}
function loadPage() {
	function processHead(head) {
		//removeElement(head.querySelector("#favicon"))
		removeElement(head.querySelector("title"))
		removeElements(head.querySelectorAll("meta"))
		// The style bellow are specific to the app
		removeElements(head.querySelectorAll("#lotusAppStylesheet"))
		conn.head = head.innerHTML;
	}
	function processBody(body) {
		removeElements(body.querySelectorAll(".lotusTitleBar2"))
		if(!conn.displayLCFooter) {
			var footer = body.querySelector("#lotusFooter");
			removeElements(footer.querySelectorAll("ul"))
		}
		var main = body.querySelector("#lotusMain")

		var fakeContent = "!!CONTENT!!";
		main.innerHTML = fakeContent;

		var htm = body.innerHTML
		var pos = htm.indexOf(fakeContent)
		conn.bodyStart = htm.substring(0,pos);
		conn.bodyEnd = htm.substring(pos+fakeContent.length);
	}

	var xhr = new XMLHttpRequest();
	xhr.open('GET', '/search/web/jsp/toolsHomepage.jsp', false);
	xhr.send();	

	if(xhr.status===200) {
		var htmlText = xhr.responseText;

		var parser = new DOMParser();
		var doc = conn.doc = parser.parseFromString(htmlText, "text/html");

		// Extract the header
		processHead(doc.head);

		// Extract the body
		processBody(doc.body);
	} else {
		alert("Cannot connect to the IBM Connections Page:"+this.url);
	}
};

loadPage();
})();
