

// vvvvvvvvvvvvv addEvents stuff from CodeMirror

// Portably register event handlers.
function RAZaddEventHandler(node, type, handler, removeFunc) {
  function wrapHandler(event) {
    handler(event || window.event);
  }
  if (typeof node.addEventListener == "function") {
    node.addEventListener(type, wrapHandler, false);
    if (removeFunc) return function() {node.removeEventListener(type, wrapHandler, false);};
  }
  else {
    node.attachEvent("on" + type, wrapHandler);
    if (removeFunc) return function() {node.detachEvent("on" + type, wrapHandler);};
  }
}

// ^^^^^^^^^^^^^^ addEvents stuff from CodeMirror

function rezoom () {
   var z = screen.width/400;

   var a1 = document.createTextNode(" screen.width="+screen.width);
   var a2 = document.createTextNode(" rezoom at="+z);
   document.body.appendChild(a1); 
   document.body.appendChild(a2); 
   
   document.body.style.zoom=z;
}

/* from http://www.quirksmode.org/quirksmode.js */

function razInvoke(url) {
	sendRequest(url, nullFunc)
}

function nullFunc(req) {
}

/* XMLHTTP */

function sendRequest(url,callback,postData) {
	var req = createXMLHTTPObject();
	if (!req) return;
	var method = (postData) ? "POST" : "GET";
	req.open(method,url,true);
	req.setRequestHeader('User-Agent','XMLHTTP/1.0');
	if (postData)
		req.setRequestHeader('Content-type','application/x-www-form-urlencoded');
	req.onreadystatechange = function () {
		if (req.readyState != 4) return;
		if (req.status != 200 && req.status != 304) {
		//	alert('HTTP error ' + req.status);
			return;
		}
		callback(req);
	}
	if (req.readyState == 4) return;
	req.send(postData);
}

function XMLHttpFactories() {
	return [
		function () {return new XMLHttpRequest()},
		function () {return new ActiveXObject("Msxml2.XMLHTTP")},
		function () {return new ActiveXObject("Msxml3.XMLHTTP")},
		function () {return new ActiveXObject("Microsoft.XMLHTTP")}
	];
}

function createXMLHTTPObject() {
	var xmlhttp = false;
	var factories = XMLHttpFactories();
	for (var i=0;i<factories.length;i++) {
		try {
			xmlhttp = factories[i]();
		}
		catch (e) {
			continue;
		}
		break;
	}
	return xmlhttp;
}

/* ***** Attach this script to the onload event ****************** */
//if (window.addEventListener) {
//  window.addEventListener('load', rezoom, false);
//} else if (window.attachEvent) {
//  window.attachEvent('onload', rezoom);
//}

