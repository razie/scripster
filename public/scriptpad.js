

//----------------- setting up code mirror

//var sc = Scripster.fromTextArea ('code');

//var Scripster = (function() {

var editor = 0;
var razParserFile= ["../contrib/scala/js/tokenizescala.js", "../contrib/scala/js/parsescala.js"];
var razStyleSheet= "contrib/scala/css/scalacolors-dark.css"
var razieCss="dark";

var SCRIP_RUN='/scripster/run?'

function darklight (css, dcss, lcss) {
  if (css == "light") return lcss;
  else return dcss;
}

function configIt (css,lang) {
  razieCss=css;

//  def langs = "scala,java,javascript,xml,html,css,ognl,sparql,lua,php,plsql,python,sql,whatever" 
  
  if (lang == 'scala') {
    razParserFile= ["../contrib/scala/js/tokenizescala.js", "../contrib/scala/js/parsescala.js"];
    razStyleSheet= darklight(css,"contrib/scala/css/scalacolors-dark.css", "contrib/scala/css/scalacolors-light.css")
    }
  else if (lang == 'java') {
    razParserFile= ["tokenizejavascript.js", "parsejavascript.js"];
    razStyleSheet= "css/jscolors.css"
    }
  else if (lang == 'javascript') {
    razParserFile= ["tokenizejavascript.js", "parsejavascript.js"];
    razStyleSheet= "css/jscolors.css"
    }
  else if (lang == 'xml') {
    razParserFile= ["tokenize.js", "parsexml.js"];
    razStyleSheet= "css/xmlcolors.css"
    }
  else if (lang == 'html') {
    razParserFile= ["tokenize.js", "parsehtmlmixed.js"];
    razStyleSheet= "css/xmlcolors.css"
    }
  else if (lang == 'css') {
    razParserFile= ["tokenize.js", "parsecss.js"];
    razStyleSheet= "css/csscolors.css"
    }
  else if (lang == 'sqarql') {
    razParserFile= ["tokenize.js", "parsesparql.js"];
    razStyleSheet= "css/sparqlcolors.css"
    }
  else if (lang == 'lua') {
    razParserFile= ["tokenize.js", "../contrib/lua/js/parselua.js"];
    razStyleSheet= "contrib/lua/css/luacolors.css"
    }
  else if (lang == 'php') {
    razParserFile= ["tokenize.js", "../contrib/lua/js/parsephp.js"];
    razStyleSheet= "contrib/php/css/phpcolors.css"
    }
  else if (lang == 'python') {
    razParserFile= ["tokenize.js", "../contrib/lua/js/parsepython.js"];
    razStyleSheet= "contrib/python/css/pythoncolors.css"
    }
  else if (lang == 'sql') {
    razParserFile= ["tokenize.js", "../contrib/lua/js/parsesql.js"];
    razStyleSheet= "contrib/sql/css/sqlcolors.css"
    }
  else if (lang == 'plsql') {
    razParserFile= ["tokenize.js", "../contrib/lua/js/parsesql.js"];
    razStyleSheet= "contrib/plsql/css/plsqlcolors.css"
    }
  else if (lang == 'whatever') {
    razParserFile= ["tokenizejavascript.js", "parsejavascript.js"];
    razStyleSheet= "css/jscolors.css"
    }
}

// this will invoke the config above with the right css/language combination
razSetup();

//function fromTextArea(cd) {

editor = CodeMirror.fromTextArea('code', {
    parserfile: razParserFile,
    stylesheet: "/public/CodeMirror/"+razStyleSheet,
    path: "/public/CodeMirror/js/",
    continuousScanning: 200,
    lineNumbers: false,
    initCallback: setup2
  });

//}

function setup2 () {
  editor.setUserKeyHandler (razspkey);
  mclose();
  statusx = document.getElementById('status');
  showStatus ("ready")
}

function razspkey (event) {
  if (event.keyCode == 120) { // F9
    if (event.ctrlKey) runSelection (SCRIP_RUN)
    else runLine(SCRIP_RUN)
  } else if (event.keyCode == 32 && event.ctrlKey)
     contentAssist(event)
}

function runLine (url) {
   var s1 = editor.cursorPosition(true)
   var scr = editor.lineContent(s1.line)

  showStatus ("sending line...")
   if (scr != null && scr.length > 0) 
     try {
       sendRequest(url+'language=scala&sessionId='+razSpSession+'&script='+urlescape(scr), showResult)
     } catch(e) {}
}

function runSelection (url) {
   var scr = editor.selection()

   if (scr == null || scr.length == 0) scr = editor.getCode()
   
  showStatus ("sending selection...");
   try {
     sendRequest(url+'language=scala&sessionId='+razSpSession+'&script='+urlescape(scr), showResult)
   } catch(e) {}
}

function showStatus (msg) {
   try {
//  statusx.value=htmlBody("Status: " + msg);
var ss = statusx;
  ss.firstChild.nodeValue=("Status: " + msg);
   } catch(e) {}
} 

function showResult (res) {
  var r = htmlBody(res.responseText);
  
  if (r.match("Scripster.Status:.*")) {
    showStatus (r.replace("Scripster.Status:", "..."));
    r = "";
    }
  else {
    showStatus ("got script result");
    }
    
  var txt = document.getElementById('result');
  txt.value=r;
} 

function replace(swhere,regexp,swhat) {
  var re = new RegExp(regexp, "g");
  return swhere.replace(re, swhat);
}

function htmlBody (text) {
   var s = replace (text, ".*<body[^>]*>", "");
   s = replace (s, "</body.*", "");
   return s;
}

function urlescape (s) {
    //   var ret = replace (escape(s), "\\+", "%2B");
   var ret1 = replace (encodeURI(s), "\\+", "%2B");
   return ret1;
}


//---------------- content assist

var currRequestCount = 0

function contentAssist (e) {
  mclose();
  
  var s1 = editor.cursorPosition(true)
  var scr = editor.lineContent(s1.line)
  currRequestCount += 1
  showStatus ("asking for content assist...")
  try {
    sendRequest('/scripster/options?sessionId='+razSpSession+'&pos='+s1.column+'&line='+urlescape(scr), 
                function (ret) {openWhenOptionsArrive(currRequestCount, e, ret)})
  } catch(e) {}
}

function openWhenOptionsArrive (reqCount, e, ret) {
  if (currRequestCount == reqCount) {
  try {
    var response = htmlBody(ret.responseText);
      var allitems = eval('(' + response + ')'); // TODO security risk?
      
      // use only the first... 10
      if (allitems.length > 10) {
        items = [];
        for (var k = 0; k < 10; k++) {
          items[k] = allitems[k];
          }
        items[10] = "...";
      } else {
        items = allitems
      }
      
      showStatus ("got content assist: " + allitems.length + " options");
      
      var s1 = editor.cursorPosition(true)
      var scr = editor.lineContent(s1.line)
     
      try { 
        mclose(); 
        var currLine = editor.currentLine() ;
   
        if(items != null && items[0] != undefined) {
          showMenu (currLine, scr, s1.character);
        } else {
          mclose();
        }
      } catch(e) {
        window.console.log (' >> ERROR:'+ e);
      }
    } catch(e) {
      window.console.log (' >> ERROR:'+ e);
    }
  }
  else 
  showStatus ("ignored content assist options - out of line")
}

// same code as TelnetReceiver.completion
function completion (curr, option) {
  if (curr == null || option == null || option == "" || curr.length <= 0 || curr.charAt(curr.length-1).match (/[.+-=:\*\/]/)) return option
  else {
         // find the last ID
         var i = curr.length-1
         while (i > 0 && curr.charAt(i).match("[a-zA-Z0-9]")) i -= 1
         var j = i+1
         if (i==0) j = i 
         var k = 0
         while (j < curr.length && k < option.length && curr.charAt(j) == option.charAt(k)) {
           j+=1; k +=1 
           }
         return option.substr(k, option.length)
      }
}

//------------------------- inspired from ... http://javascript-array.com/scripts/simple_drop_down_menu/

var timeout = 500;
var closetimer = 0;
var ddmenu = 0;
var items = []
var currSelected=0
var statusx = 0;

function buildMenu (currLine, line, column, current_word) {
   ddmenu = document.getElementById('m1');
   
   for (var k = 0; k < items.length; k++){
      var li  = document.createElement('a');
      var name = document.createTextNode (items[k]);
      li.appendChild (name);
      ddmenu.appendChild(li);
      if (k == 0) {
        currSelected = li
         currSelected.className = "razsel";   
      }
         
      li.onclick = li.doitman = function () {
        var cc = completion(line, this.text);
        editor.insertIntoLine (currLine, column, cc);
        
        var pos = editor.cursorPosition(true), text = editor.lineContent(pos.line);
        editor.selectLines(pos.line, text.length+1);

        // TODO problem: if you do content assist twice in a row, second time doesn't move to the end
      };
   }
}

// open hidden layer
function mopen() {  
   // cancel close timer
   mcancelclosetime();

   // close old layer
   if(ddmenu) ddmenu.style.visibility = 'hidden';

// you'd build new menu here...

   // get new layer and show it
   ddmenu= document.getElementById('m1');
   ddmenu.style.visibility = 'visible';
}

// close showed layer
function mclose() {
   if(ddmenu) {
     ddmenu.style.visibility = 'hidden';
     
     var listitems=ddmenu.getElementsByTagName("a")
     for (i=0; i<listitems.length; i++) 
       ddmenu.removeChild (listitems[i])
   }
  
   currSelected=0
   editor.ungrabKeys ();
}

// go close timer
function mclosetime() {
   closetimer = window.setTimeout(mclose, timeout);
}

// cancel close timer
function mcancelclosetime() {
   if(closetimer) {
      window.clearTimeout(closetimer);
      closetimer = null;
   }
}

// close layer when click-out
document.onclick = mclose; 


//------------------------- inspired from ... TODO get url

function showMenu (currLine, line, column, current_word) {
  editor.grabKeys (autoCompleteGrabkeys, keyEventFilter);
   
  buildMenu (currLine, line, column, current_word)

  // position it   
  var x = (column - line.length) *  9.25 + 45;
  x -= editor.frame.contentDocument.body.scrollLeft;
  var y = currLine * 16 + 8;
  y -= editor.frame.contentDocument.body.scrollTop;
  x +=  editor.frame.offsetParent.offsetLeft
  y += editor.frame.offsetParent.offsetTop

  ddmenu.style.left = x + "px";
  ddmenu.style.top = y + "px";
   
  mopen();
}
 
function keyEventFilter(keycode){   
   if (keycode === 27) return true; // ESC
   if (keycode === 37) return true; // LEFT_ARROW
   if (keycode === 38) return true; // UP_ARROW
   if (keycode === 39) return true; // RIGHT_ARROW
   if (keycode === 40) return true; // DOWN_ARROW
   if (keycode === 8) return true; // BACK SPACE
   if (keycode === 13) return true; // ENTER
   return false;
}
 
function autoCompleteGrabkeys(e){
   if (e.keyCode === 27 || e.keyCode === 37 ||  e.keyCode === 39 || e.keyCode === 8) {
    // ESC || LEFT_ARROW || RIGHT_LEFT_ARROW || BACK SPACE 
      mclose(); 
   }
   else if (e.keyCode === 40) // DOWN_ARROW
   {  
      if (currSelected && currSelected.nextSibling) {
         currSelected.className = "a";
         currSelected = currSelected.nextSibling;
         currSelected.className = "razsel";
      }
   }
   else if (e.keyCode === 38) // UP_ARROW
   { 
      if (currSelected && currSelected.previousSibling){
         currSelected.className = "a";
         currSelected = currSelected.previousSibling;
         currSelected.className = "razsel";   
      }
   }
   else if (e.keyCode === 13) // ENTER
   {
      try {
      if (currSelected) currSelected.doitman()
      } catch (e) {}
      mclose(); 
   }
}

//});

//------------------------ quote

function scripsterJump (url,lang, ok, k, ak) {
   var scr = editor.selection()

   if (scr == null || scr.length == 0) scr = editor.getCode()
   
  showStatus ("jumping with selection...");
   try {
     window.location = url+'&lang='+lang+'&api_key='+ak+'&k='+k+'&ok='+ok+'&css='+razieCss+'&script='+urlescape(scr)
   } catch(e) {}
}
