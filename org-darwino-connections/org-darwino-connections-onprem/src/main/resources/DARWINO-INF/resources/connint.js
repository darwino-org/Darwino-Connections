function() {
var conn = {
	displayLCFooter: true,
	doc: null,
};
window._dwoconn_ = conn;

conn.emitHeader = function emitHeader() {
	if(conn.head) {
		document.write(conn.head);
	}
};
conn.emitBody = function startBody() {
	if(conn.bodyStart) {
		document.write(bodyStart);
	}
	// once the banner is present subscribe to Conenctions logout to call ProjExec logout when clicking on the logout in the banner
	//subscribeToLCLogout();
};
conn.emitBody = function endBody() {
	if(conn.bodyEnd) {
		document.write(conn.bodyEnd);
	}
	// once the banner is present subscribe to Connections logout to call ProjExec logout when clicking on the logout in the banner
	//subscribeToLCLogout();

	// Cleanup the global variable
	delete window._dwoconn_;
};
conn.emitFooter = function emitFooter() {
	var divFooter = conn.footer;
	if(divFooter) {
		// check property before hidding the footer
		if(!conn.displayLCFooter) {
			divFooter.find("ul").remove()
		}
		document.write("<div class='lotusFooter'>"+divFooter.html()+"</div>");
	}
	// TODO: add logout	

};

function removeElement(e) {
	e.parentNode.removeChild(e);
}
function removeElements(l) {
	for(var i=0; i<l.length; i++) {
		removeElement(l.item(i));
	}
}
function loadPage(baseUrl) {
	function processHead(head) {
		removeElement(head.querySelector("title"))
		removeElements(head.querySelectorAll("meta")))
		//remove the style sheet explicit to the Apps module because it adds some classes that conflict with ProjExec like the lotusSearch used in the wall
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
		conn.endStart = htm.substring(pos+fakeContent.length);
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
		alert("Cannot connect to URL:"+this.url);
	}
};
}();
